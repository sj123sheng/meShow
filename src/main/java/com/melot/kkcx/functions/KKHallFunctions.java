package com.melot.kkcx.functions;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.api.menu.sdk.dao.domain.HDRoomPoster;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.api.menu.sdk.redis.KKHallSource;
import com.melot.api.menu.sdk.service.RoomInfoService;
import com.melot.family.driver.domain.DO.ResRoomInfoDO;
import com.melot.family.driver.domain.FamilyTopConf;
import com.melot.family.driver.service.FamilyTopConfService;
import com.melot.kk.demo.api.service.NewRcmdService;
import com.melot.kk.hall.api.constant.HallRedisKeyConstant;
import com.melot.kk.hall.api.domain.*;
import com.melot.kk.hall.api.service.*;
import com.melot.kkcore.relation.api.ActorRelation;
import com.melot.kkcore.relation.api.RelationType;
import com.melot.kkcore.relation.service.ActorRelationService;
import com.melot.kkcx.service.RoomService;
import com.melot.kkcx.transform.HallRoomTF;
import com.melot.kkcx.transform.RoomTF;
import com.melot.kktv.base.CommonStateCode;
import com.melot.kktv.base.Page;
import com.melot.kktv.base.Result;
import com.melot.kktv.redis.RecommendAlgorithmSource;
import com.melot.kktv.service.UserRelationService;
import com.melot.kktv.util.*;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;
import com.melot.sdk.core.util.MelotBeanFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Title: HallFunctions
 * <p>
 * Description: KK 大厅 Functions
 * </p>
 * 
 * @author 宋建明<a href="mailto:jianming.song@melot.cn">
 * @version V1.0
 * @since 2016-3-12 上午10:02:10
 */
public class KKHallFunctions {

    private static Logger logger = Logger.getLogger(KKHallFunctions.class);

    private static final String KK_USER_ROOM_CACHE_KEY = "KKHallFunctions.getKKUserRelationRoomList.%s.%s";

    @Resource
    FamilyTopConfService familyTopConfService;

    @Resource
    private HallRoomService hallRoomService;
    
    @Resource
    private HomeService hallHomeService;
    
    @Resource
    private SysMenuService hallPartService;

    /**
     * 获取用户有关的房间列表接口（55000001）：守护+管理+关注
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public JsonObject getKKUserRelationRoomList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        int userId, appId, platform, start, offset, filter;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 0, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, 1, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, TagCodeEnum.START_MISSING, 0, Integer.MAX_VALUE);
            offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 0, TagCodeEnum.OFFSET_MISSING, 1, Integer.MAX_VALUE);
            filter = CommonUtil.getJsonParamInt(jsonObject, "filter", 15, null, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        appId = 0;

        int roomCount = 0;
        
        final String ROOM_CACHE_KEY = String.format(KK_USER_ROOM_CACHE_KEY, appId, userId);
        if (!KKHallSource.exists(ROOM_CACHE_KEY)) {
            List<String> roomJsonList = new ArrayList<>();
            try {
                List<RoomInfo> guardRoomList = null;
                List<Integer> roomIdList = new ArrayList<>();

                // 查询守护的在线主播
                // TODO 需要拆分到守护模块+主播模块
                guardRoomList = SqlMapClientHelper.getInstance(DB.SLAVE_PG).queryForList("Other.getUserGuardActors", userId);
                if (CollectionUtils.isNotEmpty(guardRoomList)) {
                    for (int i = 0; i < guardRoomList.size(); i++) {
                        roomIdList.add(guardRoomList.get(i).getActorId());
                    }

                    // 排序
                    Collections.sort(guardRoomList, new RenqiRoomComparator());

                    for (RoomInfo roomInfo : guardRoomList) {
                        // 轮播房代理房主信息替换
                        if (roomInfo.getRoomId() != null && roomInfo.getActorId().intValue() != roomInfo.getRoomId()) {
                            RoomInfo tempRoomInfo = RoomService.getRoomInfo(roomInfo.getRoomId());
                            if (tempRoomInfo != null && tempRoomInfo.getRoomSource().intValue() == 8) {
                                roomInfo.setMaxCount(tempRoomInfo.getMaxCount());
                                roomInfo.setScreenType(tempRoomInfo.getScreenType());
                                roomInfo.setRoomSource(tempRoomInfo.getRoomSource());
                                roomInfo.setRoomLock(tempRoomInfo.getRoomLock());
                                roomInfo.setRoomTheme(tempRoomInfo.getRoomTheme());
                                roomInfo.setRoomMode(tempRoomInfo.getRoomMode());
                                roomInfo.setPeopleInRoom(tempRoomInfo.getPeopleInRoom());
                            }
                        } else {
                            roomInfo.setRoomId(roomInfo.getActorId());
                        }
                        roomJsonList.add(RoomTF.roomInfoToJson(roomInfo, platform).toString());
                    }
                }

                // 取用户管理的主播
                List<ActorRelation> list = null;
                ActorRelationService actorRelationService = MelotBeanFactory.getBean("kkActorRelationService", ActorRelationService.class);
                if (actorRelationService != null) {
                    list = actorRelationService.getRelationByUserId(userId, RelationType.ADMIN.typeId());
                }
                if (CollectionUtils.isNotEmpty(list)) {
                    for (Iterator<ActorRelation> iterator = list.iterator(); iterator.hasNext();) {
                        Integer integer = iterator.next().getActorId();
                        if (roomIdList.contains(integer)) {
                            iterator.remove();
                        }
                    }

                    String[] roomIds = new String[list.size()];
                    int i = 0;
                    for (ActorRelation actorRelation : list) {
                        roomIds[i++] = String.valueOf(actorRelation.getActorId());
                    }
                    try {
                        Result<List<HallRoomInfoDTO>> roomInfosResult = hallRoomService.getLiveRooms(roomIds);
                        if (roomInfosResult != null 
                                && CommonStateCode.SUCCESS.equals(roomInfosResult.getCode()) 
                                && CollectionUtils.isNotEmpty(roomInfosResult.getData())) {
                            List<HallRoomInfoDTO> roomInfos = roomInfosResult.getData();
                            // 排序
                            Collections.sort(roomInfos, new RenqiHallRoomComparator());

                            for (HallRoomInfoDTO roomInfo : roomInfos) {
                                roomJsonList.add(HallRoomTF.roomInfoToJson(roomInfo, platform).toString());
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Fail to call firstPageHandler.getLiveRooms(" + roomIds + ").", e);
                    }
                
                }

                // 取用户关注数中的 1000 个
                Set<String> followIdSet = UserRelationService.getFollowIds(userId, 0, 1000);
                if (CollectionUtils.isNotEmpty(followIdSet)) {
                    int followId;
                    String followIdStr;
                    for (Iterator<String> iterator = followIdSet.iterator(); iterator.hasNext();) {
                        followIdStr = iterator.next();
                        followId = Integer.valueOf(followIdStr.trim());
                        if (roomIdList.contains(followId)) {
                            iterator.remove();
                        } else {
                            roomIdList.add(followId);
                        }
                    }

                    String[] roomIds = new String[followIdSet.size()];
                    int i = 0;
                    for (String string : followIdSet) {
                        roomIds[i++] = string;
                    }
                    try {
                        Result<List<HallRoomInfoDTO>> roomInfosResult = hallRoomService.getLiveRooms(roomIds);
                        if (roomInfosResult != null 
                                && CommonStateCode.SUCCESS.equals(roomInfosResult.getCode()) 
                                && CollectionUtils.isNotEmpty(roomInfosResult.getData())) {
                            List<HallRoomInfoDTO> roomInfos = roomInfosResult.getData();
                            // 排序
                            Collections.sort(roomInfos, new RenqiHallRoomComparator());

                            for (HallRoomInfoDTO roomInfo : roomInfos) {
                                roomJsonList.add(HallRoomTF.roomInfoToJson(roomInfo, platform).toString());
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Fail to call firstPageHandler.getLiveRooms(" + roomIds + ").", e);
                    }
                }
            } catch (Exception e) {
                logger.error("KKHallFunctions.getKKUserRelationRoomList(" + jsonObject.toString() + ") execute exception.", e);
            }

            if (roomJsonList.isEmpty()) {
                roomJsonList.add("null");
            }
            KKHallSource.del(ROOM_CACHE_KEY);
            KKHallSource.addSortedSet(ROOM_CACHE_KEY, roomJsonList, 120);
        }

        JsonArray roomArray = new JsonArray();

        // 起始位置从 0 开始
        start = start <= 1 ? 0 : start;

        Set<String> roomSet = KKHallSource.rangeSortedSet(ROOM_CACHE_KEY, start, start + offset - 1);
        List<Integer> roomIdList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(roomSet) && !roomSet.contains("null")) {
            if (roomCount == 0) {
                roomCount = KKHallSource.countSortedSet(ROOM_CACHE_KEY);
            }
            JsonParser jsonParser = new JsonParser();
            JsonObject roomJsonObject;
            for (String roomJson : roomSet) {
                roomJsonObject = jsonParser.parse(roomJson).getAsJsonObject();
                roomArray.add(roomJsonObject);
                roomIdList.add(roomJsonObject.get("roomId").getAsInt());
            }
        }

        if (start == 0 && roomCount < offset && (platform == PlatformEnum.WEB || roomCount > 0)) {
            JsonArray recommendRoomList = new JsonArray();
            try {
                // 将兴趣推荐作为优质主播库，从中随机推荐1~4个在播主播，推荐的主播不能与兴趣推荐前14个相同
                String recommendRoomKey = String.format(HallRedisKeyConstant.KK_FIRST_RECOMMENDED_ROOMLIST_CACHE_KEY, new Random().nextInt(10));
                Result<Page<HallRoomInfoDTO>> roomListResult = hallRoomService.getKKRecommendRooms(userId, appId, start + filter, 2 * offset, recommendRoomKey);
                if (roomListResult != null && CommonStateCode.SUCCESS.equals(roomListResult.getCode())
                        && roomListResult.getData() != null 
                        && CollectionUtils.isNotEmpty(roomListResult.getData().getList())) {
                    List<HallRoomInfoDTO> roomList = roomListResult.getData().getList();
                    for (HallRoomInfoDTO hallRoomInfoDTO : roomList) {

                        // 推荐的主播不能与兴趣推荐前14个相同，且推荐数量没超需求数量
                        if (!roomIdList.contains(hallRoomInfoDTO.getActorId()) 
                                && recommendRoomList.size() < offset - roomCount) {
                            recommendRoomList.add(HallRoomTF.roomInfoToJson(hallRoomInfoDTO, platform));
                        }
                    }
                    result.add("recommendRoomList", recommendRoomList);
                    result.addProperty("recommendRoomTotal", recommendRoomList.size());
                }
            } catch (Exception e) {
                logger.error("Fail to call firstPageHandler.getRecommendRooms(null, null, " + appId + ", " + (2 * offset) + ")", e);
            }
        }
        
        result.add("roomList", roomArray);
        result.addProperty("roomTotal", roomCount);
        result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 获取KK推荐的房间列表接口（55000002）
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject getKKRecommendedList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        int appId, start, offset, platform, userId, firstView, roomListIndex, channel;
        try {
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, TagCodeEnum.PLATFORM_MISSING, 1, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 0, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, TagCodeEnum.START_MISSING, 0, Integer.MAX_VALUE);
            offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 0, TagCodeEnum.OFFSET_MISSING, 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
            //是否第一次进来查看页面-（下拉刷新跟web端刷新页面不属于第一次进来查看  1：是第一次 0：不是第一次）
            firstView = CommonUtil.getJsonParamInt(jsonObject, "firstView", 1, null, 0, Integer.MAX_VALUE);
            //回传当时随机获取的是10个列表中的哪个列表（第一次进来查看和第一次下拉刷新是没有该值的）
            roomListIndex = CommonUtil.getJsonParamInt(jsonObject, "roomListIndex", -1, null, 0, Integer.MAX_VALUE);
            // 渠道号
            channel = CommonUtil.getJsonParamInt(jsonObject, "c", AppChannelEnum.KK, null, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        getRecommendAlgorithmB(result, appId, start, offset, platform, userId, firstView, roomListIndex);
        result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);

        return result;
    }

    // 推荐算法A
    private void getRecommendAlgorithmA(JsonObject result, int appId, int start, int offset, int platform, int userId, int firstView, int roomListIndex) {

        int roomCount;
        JsonArray roomArray = new JsonArray();
        try {

            // 从大数据推荐算法接口中获取推荐房间总数和推荐房间id列表
            NewRcmdService newRcmdService = MelotBeanFactory.getBean("newRcmdService", NewRcmdService.class);
            roomCount = newRcmdService.getRcmdActornum(userId);
            int pageIndex = start / offset + 1;
            List<Integer> roomIdList = newRcmdService.getRcmdActorList(userId, pageIndex, offset);

            // 如果大数据给的推荐主播列表总数小于等于一页显示的数量，总数从现有推荐算法中获取，剩下的推荐主播列表从现有的推荐算法中补齐
            if (CollectionUtils.isNotEmpty(roomIdList)) {
                String roomIds = StringUtils.join(roomIdList.toArray(), ",");
                Result<List<HallRoomInfoDTO>> roomInfosResult = hallRoomService.getRoomListByRoomIds(roomIds);
                if (roomInfosResult != null && CommonStateCode.SUCCESS.equals(roomInfosResult.getCode())
                        && CollectionUtils.isNotEmpty(roomInfosResult.getData())) {
                    List<HallRoomInfoDTO> roomInfos = roomInfosResult.getData();
                    if (roomCount <= offset) {
                        List<HallRoomInfoDTO> roomList = getRecommendAlgorithmB(result, appId, start, offset, platform, userId, firstView, roomListIndex);
                        // 查询第一页时 将大数据的推荐算法查询的数据插入现有的推荐房间列表中
                        if (start == 0) {
                            if(roomList == null) {
                                roomList = Lists.newArrayList();
                            }
                            for (int i = 0; i < roomInfos.size(); i++) {
                                roomList.add(i, roomInfos.get(i));
                            }
                            int size = roomList.size() < offset ? roomList.size() : offset;
                            for (int j = 0; j < size; j++) {
                                roomArray.add(HallRoomTF.roomInfoToJson(roomList.get(j), platform));
                            }
                            result.add("roomList", roomArray);
                            if(size < offset || roomList.size() - roomInfos.size() < offset) {
                                result.addProperty("roomTotal", size);
                            }
                        }
                    } else {
                        for (HallRoomInfoDTO roomInfo : roomInfos) {
                            roomArray.add(HallRoomTF.roomInfoToJson(roomInfo, platform));
                        }
                        result.add("roomList", roomArray);
                        result.addProperty("roomTotal", roomCount);
                    }
                }else {
                    getRecommendAlgorithmB(result, appId, start, offset, platform, userId, firstView, roomListIndex);
                }
            }else {
                getRecommendAlgorithmB(result, appId, start, offset, platform, userId, firstView, roomListIndex);
            }
        } catch (Exception e) {
            logger.error("Fail to call firstPageHandler.getRecommendAlgorithmA(" + userId + ", " + appId + ", " + start + ", " + offset + ")", e);
        }
    }

    // 推荐算法B
    private List<HallRoomInfoDTO> getRecommendAlgorithmB(JsonObject result, int appId, int start, int offset, int platform, int userId, int firstView, int roomListIndex) {

        int roomCount;
        JsonArray roomArray = new JsonArray();
        List<HallRoomInfoDTO> roomList = Lists.newArrayList();
        try {

            String recommendRoomKey = HallRedisKeyConstant.KK_FIRST_RECOMMENDED_ROOMLIST_CACHE_KEY;
            if(firstView == 0) {
                recommendRoomKey = HallRedisKeyConstant.KK_RELOAD_RECOMMENDED_ROOMLIST_CACHE_KEY;
            }
            if(roomListIndex == -1) {
                roomListIndex = new Random().nextInt(10);
            }
            recommendRoomKey = String.format(recommendRoomKey, roomListIndex);
            roomCount = hallRoomService.getKKRecommendRoomCount(userId, appId, recommendRoomKey).getData();
            Result<Page<HallRoomInfoDTO>> roomListResult = hallRoomService.getKKRecommendRooms(userId, appId, start, offset, recommendRoomKey);
            if (roomListResult == null 
                    || !CommonStateCode.SUCCESS.equals(roomListResult.getCode())
                    || roomListResult.getData() == null) {
                return roomList;
            }
            roomList = roomListResult.getData().getList();
            if (CollectionUtils.isNotEmpty(roomList)) {
                for (HallRoomInfoDTO roomInfo : roomList) {
                    roomArray.add(HallRoomTF.roomInfoToJson(roomInfo, platform));
                }
            }

            result.addProperty("roomListIndex", roomListIndex);
            result.add("roomList", roomArray);
            result.addProperty("roomTotal", roomCount < 1 ? roomArray.size() : roomCount);
        } catch (Exception e) {
            logger.error("Fail to call firstPageHandler.getRecommendAlgorithmB(" + userId + ", " + appId + ", " + start + ", " + offset + ")", e);
        }

        return roomList;
    }

    // 推荐算法C
    private void getRecommendAlgorithmC(JsonObject result, int appId, int start, int offset, int platform, int userId) {

        int roomCount;
        JsonArray roomArray = new JsonArray();
        try {

            String simpleRecommendedRoomKey = RecommendAlgorithmSource.SIMPLE_RECOMMENDED_ROOM_KEY;

            roomCount = hallRoomService.getKKRecommendRoomCount(userId, appId, simpleRecommendedRoomKey).getData();
            Result<Page<HallRoomInfoDTO>> roomListResult = hallRoomService.getKKRecommendRooms(userId, appId, start, offset, simpleRecommendedRoomKey);
            if (roomListResult == null 
                    || !CommonStateCode.SUCCESS.equals(roomListResult.getCode())
                    || roomListResult.getData() == null) {
                return ;
            }
            List<HallRoomInfoDTO> roomList = roomListResult.getData().getList();
            if (CollectionUtils.isNotEmpty(roomList)) {
                for (HallRoomInfoDTO roomInfo : roomList) {
                    roomArray.add(HallRoomTF.roomInfoToJson(roomInfo, platform));
                }
            }
            
            result.add("roomList", roomArray);
            result.addProperty("roomTotal", roomCount < 1 ? roomArray.size() : roomCount);
        } catch (Exception e) {
            logger.error("Fail to call firstPageHandler.getRecommendAlgorithmC(" + userId + ", " + appId + ", " + start + ", " + offset + ")", e);
        }
    }

    /**
     * 获取KK热门的房间列表接口（55000003）
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject getKKHotRoomList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        int type, gender, platform, start, offset;
        try {
            // 热门类型：1-默认排序（评分系统），2-人气，3-主播等级，4-最近开播， 5-web端默认
            type = CommonUtil.getJsonParamInt(jsonObject, "type", 2, null, 1, 5);
            // 性别： -1：全部 0：女 1：男
            gender = CommonUtil.getJsonParamInt(jsonObject, "gender", -1, null, 0, 1);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, TagCodeEnum.PLATFORM_MISSING, 1, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, TagCodeEnum.START_MISSING, 0, Integer.MAX_VALUE);
            offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 0, TagCodeEnum.OFFSET_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        // 起始位置从 0 开始
        start = start <= 1 ? 0 : start;
        JsonArray roomArray = new JsonArray();
        int roomCount = 0;
        List<HallRoomInfoDTO> roomList = null;
        Result<List<HallRoomInfoDTO>> roomListResult;

        if (type == 5) {
            // 第一页前5个栏目优先放最近开播，其后栏目用主播等级内容补充
            roomCount = hallRoomService.getHotRoomCount(4, gender).getData() + hallRoomService.getHotRoomCount(3, gender).getData();

            // 最近开播
            Set<Integer> filterIds = Sets.newHashSet();
            List<HallRoomInfoDTO> recentList = null;
            roomListResult = hallRoomService.getHotRooms(4, gender, start, offset <= 5 ? offset : 5);
            if (roomListResult != null) {
                recentList = roomListResult.getData();
            }
            if (CollectionUtils.isNotEmpty(recentList)) {
                for (HallRoomInfoDTO room : recentList) {
                    filterIds.add(room.getActorId());
                }
            }

            if (start == 0) {
                try {
                    int filterSize = 0;
                    int fillCount = 0;
                    roomList = recentList;
                    if (CollectionUtils.isNotEmpty(roomList)) {
                        filterSize = roomList.size();
                    }
                    fillCount = offset - filterSize;
                    if (roomList != null && fillCount > 0) {
                        roomListResult = hallRoomService.getFilterHotRooms(3, gender, filterIds, start, fillCount);
                        if (roomListResult != null && CollectionUtils.isNotEmpty(roomListResult.getData())) {
                            roomList.addAll(roomListResult.getData());
                        }
                    }

                    if (roomList != null && roomList.size() > 0) {
                        for (HallRoomInfoDTO roomInfo : roomList) {
                            roomArray.add(HallRoomTF.roomInfoToJson(roomInfo, platform));
                        }
                    }
                } catch (Exception e) {
                    logger.error("Fail to call firstPageHandler.getHotRooms(" + type + ", " + start + ", " + offset + ")", e);
                }
            } else {
                try {
                    roomListResult = hallRoomService.getFilterHotRooms(3, gender, filterIds, start, offset);
                    if (roomListResult != null) {
                        roomList = roomListResult.getData();
                    }
                    if (CollectionUtils.isNotEmpty(roomList)) {
                        for (HallRoomInfoDTO roomInfo : roomList) {
                            roomArray.add(HallRoomTF.roomInfoToJson(roomInfo, platform));
                        }
                    }
                } catch (Exception e) {
                    logger.error("Fail to call firstPageHandler.getHotRooms(" + type + ", " + start + ", " + offset + ")", e);
                }
            }
        } else {
            try {
                roomCount = hallRoomService.getHotRoomCount(type, gender).getData();
                roomListResult = hallRoomService.getHotRooms(type, gender, start, offset);
                if (roomListResult != null) {
                    roomList = roomListResult.getData();
                }
                if (CollectionUtils.isNotEmpty(roomList)) {
                    for (HallRoomInfoDTO roomInfo : roomList) {
                        roomArray.add(HallRoomTF.roomInfoToJson(roomInfo, platform));
                    }
                }
            } catch (Exception e) {
                logger.error("Fail to call firstPageHandler.getHotRooms(" + type + ", " + start + ", " + offset + ")", e);
            }
        }

        result.add("roomList", roomArray);
        result.addProperty("roomTotal", roomCount);
        result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);

        return result;
    }

    /**
     * 获得大厅显示板块(55000004)
     * @param jsonObject
     * @return
     */
    public JsonObject getKKHallPlateList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        int platform = 0;
        int appId = 0;
        int channel = 0;
        try {
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, TagCodeEnum.PLATFORM_MISSING, 1, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", 0, TagCodeEnum.APPID_MISSING, 0, Integer.MAX_VALUE);
            channel = CommonUtil.getJsonParamInt(jsonObject, "c", 0, TagCodeEnum.CHANNEL_MISSING, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        List<FirstPageConfDTO> tempList = null;
        try {
            Result<List<FirstPageConfDTO>> tempListResult = hallHomeService.getFistPagelist(appId, channel, platform, 0, 0, 0, true, 1, true);
            if (tempListResult != null && CommonStateCode.SUCCESS.equals(tempListResult.getCode())) {
                tempList = tempListResult.getData();
            }
            
        } catch (Exception e) {
            logger.error("Fail to call hallHomeService.getFistPagelist ", e);
        }

        if (tempList != null) {
            JsonArray plateList = new JsonArray();
            for (FirstPageConfDTO temp : tempList) {
                JsonObject json = new JsonObject();
                json.addProperty("position", temp.getSeatId());
                json.addProperty("type", temp.getSeatType());
                if (temp.getTitleName() != null) {
                    json.addProperty("title", temp.getTitleName());
                }
                json.addProperty("cdnState", temp.getCdnState());
                
                if (!StringUtil.strIsNull(temp.getIcon())) {
                    json.addProperty("icon", temp.getIcon());
                }
                
                if (!StringUtil.strIsNull(temp.getWebIcon())) {
                    json.addProperty("webIcon", temp.getWebIcon());
                }
                
                if (temp.getSubTitle() != null) {
                    json.addProperty("subTitle", temp.getSubTitle());
                }
                if (temp.getRoomTotal() != null) {
                    json.addProperty("roomTotal", temp.getRoomTotal());
                }
                if (temp.getLiveTotal() != null) {
                    json.addProperty("liveTotal", temp.getLiveTotal());
                }
                if (temp.getDetailId() != null) {
                    // 专区编号
                    json.addProperty("id", temp.getDetailId());
                }
                if (temp.getCdnState() != null) {
                    if (temp.getCdnState() > 0 && temp.getSeatType() != 3) {
                        JsonArray roomArray = new JsonArray();
                        List<HallRoomInfoDTO> roomList = temp.getRooms();
                        if (roomList != null) {
                            for (HallRoomInfoDTO roomInfo : roomList) {
                                roomArray.add(HallRoomTF.roomInfoToJson(roomInfo, platform));
                            }
                        }
                        json.add("result", roomArray);
                    } else {
                        json.add("result", new JsonArray());
                    }
                }
                plateList.add(json);
            }
            result.add("plateList", plateList);
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } else {
            result.addProperty("TagCode", TagCodeEnum.FAIL_TO_CALL_API_MENU_MODULE);
        }

        return result;
    }

    /**
     * 获取高清房55000005
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getHDRoomList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        @SuppressWarnings("unused")
        int platform, appId;
        try {
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, TagCodeEnum.PLATFORM_MISSING, 1, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        // 获取右边的推荐信息【取KK推荐的房间列表接口（55000002）】
        List<HallRoomInfoDTO> recommendedRoomList = null;
        List<Integer> recommendedRoomIdList = new ArrayList<>();
        try {
            String recommendRoomKey = String.format(HallRedisKeyConstant.KK_FIRST_RECOMMENDED_ROOMLIST_CACHE_KEY, new Random().nextInt(10));
            Result<Page<HallRoomInfoDTO>> recommendedRoomListResult = hallRoomService.getKKRecommendRooms(1, 0, 0, 9, recommendRoomKey);
            if (recommendedRoomListResult != null 
                    && CommonStateCode.SUCCESS.equals(recommendedRoomListResult.getCode())
                    && recommendedRoomListResult.getData() != null) {
                recommendedRoomList = recommendedRoomListResult.getData().getList();
            }
        } catch (Exception e) {
            logger.error("Fail to call firstPageHandler.getKKRecommendRooms(1, 0, 0, 9)", e);
        }

        // 记录需要过滤的roomId号
        if (CollectionUtils.isNotEmpty(recommendedRoomList)) {
            for (HallRoomInfoDTO roomInfo : recommendedRoomList) {
                if (roomInfo != null && roomInfo.getActorId() != null 
                        && roomInfo.getPartPosition() == null) {
                    recommendedRoomIdList.add(roomInfo.getActorId());
                }
            }
        }
        int recommendedRoomCount = recommendedRoomIdList == null ? 0 : recommendedRoomIdList.size();
        
        // 由于存在连麦房，过滤空间加大一点
        recommendedRoomCount *= 2;

        // 获取HD主播
        HallPartConfDTO sysMenu = null;
        try {
            Result<HallPartConfDTO> sysMenuResult = hallPartService.getPartList(486, 0, 0, 0, 0, 3 + recommendedRoomCount);
            if (sysMenuResult != null && CommonStateCode.SUCCESS.equals(sysMenuResult.getCode())) {
                sysMenu = sysMenuResult.getData();
            }
        } catch (Exception e) {
            logger.error("Fail to call firstPageHandler.getPartList, cataId: 486", e);
        }

        if (sysMenu != null) {
            if (sysMenu.getTitleName() != null) {
                result.addProperty("titleName", sysMenu.getTitleName());
            }
            String subTitle = sysMenu.getSubTitle();
            int roomTotal = 0;
            if (sysMenu.getLiveTotal() != null) {
                result.addProperty("liveTotal", sysMenu.getLiveTotal());
            }
            if (sysMenu.getRoomCount() != null) {
                roomTotal = sysMenu.getRoomCount().intValue();
            } else {
                roomTotal = 0;
            }
            if (subTitle != null) {
                result.addProperty("subTitle", subTitle);
            }
            result.addProperty("roomTotal", roomTotal);

            JsonArray roomArray = new JsonArray();
            List<HallRoomInfoDTO> roomList = sysMenu.getRooms();
            if (CollectionUtils.isEmpty(roomList)) {
                Result<List<HallRoomInfoDTO>> hotRoomsResult = hallRoomService.getHotRooms(1, -1, 0, 3 + recommendedRoomCount);
                if (hotRoomsResult != null && CommonStateCode.SUCCESS.equals(hotRoomsResult.getCode())) {
                    roomList = hotRoomsResult.getData();
                }
            }
            StringBuilder sb = new StringBuilder();
            if (roomList != null) {
                for (HallRoomInfoDTO roomInfo : roomList) {
                    // 添加对于右边推荐数据的过滤
                    if (CollectionUtils.isNotEmpty(recommendedRoomIdList)
                            && recommendedRoomIdList.contains(roomInfo.getRoomId()) 
                            && roomInfo.getPartPosition() == null) {
                        continue;
                    }

                    // 添加对于连麦房的过滤
                    if (roomInfo != null && roomInfo.getRoomMode() != null && roomInfo.getRoomMode().equals(100)) {
                        continue;
                    }

                    // 添加到array中
                    roomArray.add(HallRoomTF.roomInfoToJson(roomInfo, platform));
                    sb.append(roomInfo.getActorId()).append(",");

                    // 高清推荐暂时只有3个
                    if (roomArray.size() >= 3) {
                        break;
                    }
                }
            }

            // 如果roomArray数据是空的，则还原为热门的三条数据（这块没有进行数据的过滤，希望不要走到这一步···）
            if (roomArray == null || roomArray.size() == 0) {
                Result<List<HallRoomInfoDTO>> hotRoomsResult = hallRoomService.getHotRooms(1, -1, 0, 3);
                if (hotRoomsResult != null && CommonStateCode.SUCCESS.equals(hotRoomsResult.getCode())) {
                    roomList = hotRoomsResult.getData();
                }
                if (CollectionUtils.isNotEmpty(roomList)) {
                    for (HallRoomInfoDTO roomInfo : roomList) {
                        // 添加到array中
                        roomArray.add(HallRoomTF.roomInfoToJson(roomInfo, platform));
                        sb.append(roomInfo.getActorId()).append(",");
                    }
                }
            }

            // 高清房间无在播主播时用热门房间替代，无配置海报，使用前端默认海报
            if (sysMenu != null && sysMenu.getLiveTotal() != 0) {
                // TODO 高清房间海报，迁移主播模块
                String roomIds = sb.toString().substring(0, sb.toString().length() - 1);
                RoomInfoService roomInfoServie = (RoomInfoService) MelotBeanFactory.getBean("roomInfoService");
                JsonArray posterArray = new JsonArray();
                if (!StringUtil.strIsNull(roomIds)) {
                    List<HDRoomPoster> posters = roomInfoServie.getHDRoomPosterByRoomIds(roomIds);
                    if (CollectionUtils.isNotEmpty(posters)) {
                        for (HDRoomPoster hdRoomPoster : posters) {
                            JsonObject jsonObject2 = new JsonObject();
                            if (hdRoomPoster.getActorId() != null) {
                                jsonObject2.addProperty("actorId", hdRoomPoster.getActorId());;
                            }
                            if (hdRoomPoster.getPoster() != null) {
                                jsonObject2.addProperty("poster", hdRoomPoster.getPoster());
                            }
                            posterArray.add(jsonObject2);
                        }
                    }
                }
                result.add("posterList", posterArray);
            }

            result.add("roomList", roomArray);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
        } else {
            result.addProperty("TagCode", TagCodeEnum.FAIL_TO_CALL_API_MENU_MODULE);
        }

        return result;
    }
    
    /**
     * 获取新版高清房（51070101）
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getV2HDRoomList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        @SuppressWarnings("unused")
        int platform, appId;
        try {
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, TagCodeEnum.PLATFORM_MISSING, 1, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        // 获取HD主播
        HallPartConfDTO sysMenu = null;
        try {
            Result<HallPartConfDTO> sysMenuResult = hallPartService.getPartList(486, 0, 0, 0, 0, 9);
            if (sysMenuResult != null && CommonStateCode.SUCCESS.equals(sysMenuResult.getCode())) {
                sysMenu = sysMenuResult.getData();
            }
        } catch (Exception e) {
            logger.error("Fail to call hallPartService.getPartList, cataId: 486", e);
        }

        if (sysMenu != null) {
            if (sysMenu.getTitleName() != null) {
                result.addProperty("titleName", sysMenu.getTitleName());
            }
            String subTitle = sysMenu.getSubTitle();
            int roomTotal = 0;
            if (sysMenu.getLiveTotal() != null) {
                result.addProperty("liveTotal", sysMenu.getLiveTotal());
            }
            if (sysMenu.getRoomCount() != null) {
                roomTotal = sysMenu.getRoomCount().intValue();
            } else {
                roomTotal = 0;
            }
            if (subTitle != null) {
                result.addProperty("subTitle", subTitle);
            }
            result.addProperty("roomTotal", roomTotal);

            JsonArray roomArray = new JsonArray();
            List<HallRoomInfoDTO> roomList = sysMenu.getRooms();
            List<Integer> hdRoomIdList = new ArrayList<>();
            if (roomList != null) {
                for (HallRoomInfoDTO roomInfo : roomList) {
                    roomArray.add(HallRoomTF.roomInfoToJson(roomInfo, platform));
                    hdRoomIdList.add(roomInfo.getRoomId());
                }
            }

            // 高清房数据不足9条，则用热门主播补充
            if (roomArray.size() < 9) {
                Result<List<HallRoomInfoDTO>> hotRoomsResult = hallRoomService.getHotRooms(1, -1, 0, 9);
                if (hotRoomsResult != null && CommonStateCode.SUCCESS.equals(hotRoomsResult.getCode())) {
                    roomList = hotRoomsResult.getData();
                }
                if (CollectionUtils.isNotEmpty(roomList)) {
                    for (HallRoomInfoDTO roomInfo : roomList) {
                        if ((hdRoomIdList.isEmpty() || !hdRoomIdList.contains(roomInfo.getRoomId()))
                                && roomArray.size() < 9) {
                            roomArray.add(HallRoomTF.roomInfoToJson(roomInfo, platform));
                        }
                    }
                }
            }

            result.add("roomList", roomArray);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
        } else {
            result.addProperty("TagCode", TagCodeEnum.FAIL_TO_CALL_API_MENU_MODULE);
        }

        return result;
    }
    
    /**
     * 获取王牌主播（55000006）
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getTrumpRoomList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        int platform, offset, userId, channel;
        try {
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, TagCodeEnum.PLATFORM_MISSING, 1, Integer.MAX_VALUE);
            offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 3, null, 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
            // 渠道号
            channel = CommonUtil.getJsonParamInt(jsonObject, "c", AppChannelEnum.KK, null, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        JsonArray roomArray = new JsonArray();
        List<ResRoomInfoDO> resRoomInfoDOS;
        
        try {
            FamilyTopConf familyTopConf = familyTopConfService.getFamilyTopConf().getData();
            if(familyTopConf != null) {
                int familyId = familyTopConf.getFamilyId();
                resRoomInfoDOS = familyTopConfService.getAliveRoomInfoByFamilyId(familyId).getData();
                if(resRoomInfoDOS != null) {
                    for(ResRoomInfoDO resRoomInfoDO : resRoomInfoDOS) {
                        HallRoomInfoDTO roomInfo = BeanMapper.map(resRoomInfoDO, HallRoomInfoDTO.class);
                        JsonObject roomInfoJsonObject = HallRoomTF.roomInfoToJson(roomInfo, platform);
                        roomInfoJsonObject.addProperty("sideLabelContent", familyTopConf.getFamilyLabel());
                        roomInfoJsonObject.addProperty("sideLabelColor", "1");
                        roomArray.add(roomInfoJsonObject);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Fail to execute getAliveActorIdsByFamilyId", e);
        }

        if(roomArray.size() < 3) {
            try {
                Result<List<HallRoomInfoDTO>> roomListResult = hallRoomService.getTrumpRooms(offset);
                if (roomListResult != null) {
                    List<HallRoomInfoDTO> trumpRoomList = roomListResult.getData();
                    if (CollectionUtils.isNotEmpty(trumpRoomList)) {
                        for (HallRoomInfoDTO roomInfo : trumpRoomList) {
                            if(roomArray.size() >= 3) {
                                break;
                            }
                            roomArray.add(HallRoomTF.roomInfoToJson(roomInfo, platform));
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Fail to call firstPageHandler.getTrumpRooms offset:" + offset, e);
            }
        }

        result.add("roomList", roomArray);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
        return result;
    }

    /**
     * 获取大厅首页皮肤配置信息[51070102]
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getWebSkinInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        try {
            WebSkinConfService webSkinConfService = (WebSkinConfService) MelotBeanFactory.getBean("webSkinConfService");
            Result<WebSkinConf> currentWebSkinConfResult = webSkinConfService.getCurrentWebSkinConf();
            if (currentWebSkinConfResult != null 
                    && CommonStateCode.SUCCESS.equals(currentWebSkinConfResult.getCode())) {
                WebSkinConf webSkinConf = currentWebSkinConfResult.getData();
                
                // 当前没有皮肤数据
                if (webSkinConf == null) {
                    result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
                    return result;
                }
                
                result.addProperty("skinName", webSkinConf.getSkinName());
                if (!StringUtil.strIsNull(webSkinConf.getNavigationBarLogo())) {
                    result.addProperty("navigationBarLogo", webSkinConf.getNavigationBarLogo());
                }

                if (!StringUtil.strIsNull(webSkinConf.getNavigationBarNomal())) {
                    result.addProperty("navigationBarNomal", webSkinConf.getNavigationBarNomal());
                }
                
                if (!StringUtil.strIsNull(webSkinConf.getNavigationBarHover())) {
                    result.addProperty("navigationBarHover", webSkinConf.getNavigationBarHover());
                }
                
                if (!StringUtil.strIsNull(webSkinConf.getNavigationBackLeft())) {
                    result.addProperty("navigationBackLeft", webSkinConf.getNavigationBackLeft());
                }
                
                if (!StringUtil.strIsNull(webSkinConf.getNavigationBackMiddle())) {
                    result.addProperty("navigationBackMiddle", webSkinConf.getNavigationBackMiddle());
                }
                
                if (!StringUtil.strIsNull(webSkinConf.getNavigationBackRight())) {
                    result.addProperty("navigationBackRight", webSkinConf.getNavigationBackRight());
                }
                
                if (!StringUtil.strIsNull(webSkinConf.getSearchNomal())) {
                    result.addProperty("searchNomal", webSkinConf.getSearchNomal());
                }
                
                if (!StringUtil.strIsNull(webSkinConf.getSearchHover())) {
                    result.addProperty("searchHover", webSkinConf.getSearchHover());
                }
                
                if (!StringUtil.strIsNull(webSkinConf.getSearchIconNomal())) {
                    result.addProperty("searchIconNomal", webSkinConf.getSearchIconNomal());
                }
                
                if (!StringUtil.strIsNull(webSkinConf.getSearchIconHover())) {
                    result.addProperty("searchIconHover", webSkinConf.getSearchIconHover());
                }
                
                if (!StringUtil.strIsNull(webSkinConf.getSearchBox())) {
                    result.addProperty("searchBox", webSkinConf.getSearchBox());
                }
                
                if (!StringUtil.strIsNull(webSkinConf.getBackgroundBase())) {
                    result.addProperty("backgroundBase", webSkinConf.getBackgroundBase());
                }
                
                if (!StringUtil.strIsNull(webSkinConf.getBackground1920())) {
                    result.addProperty("background1920", webSkinConf.getBackground1920());
                }
                
                if (!StringUtil.strIsNull(webSkinConf.getBackground1680())) {
                    result.addProperty("background1680", webSkinConf.getBackground1680());
                }
                
                if (!StringUtil.strIsNull(webSkinConf.getBackground1440())) {
                    result.addProperty("background1440", webSkinConf.getBackground1440());
                }
                
                if (!StringUtil.strIsNull(webSkinConf.getBackgroundLink())) {
                    result.addProperty("backgroundLink", webSkinConf.getBackgroundLink());
                }
                
                if (!StringUtil.strIsNull(webSkinConf.getIndicatorNomal())) {
                    result.addProperty("indicatorNomal", webSkinConf.getIndicatorNomal());
                }
                
                if (!StringUtil.strIsNull(webSkinConf.getIndicatorHover())) {
                    result.addProperty("indicatorHover", webSkinConf.getIndicatorHover());
                }
                
                if (!StringUtil.strIsNull(webSkinConf.getIndicatorBack())) {
                    result.addProperty("indicatorBack", webSkinConf.getIndicatorBack());
                }
                
                if (!StringUtil.strIsNull(webSkinConf.getVertical())) {
                    result.addProperty("vertical", webSkinConf.getVertical());
                }
                
                if (!StringUtil.strIsNull(webSkinConf.getNavigationBackBase())) {
                    result.addProperty("navigationBackBase", webSkinConf.getNavigationBackBase());
                }
            }
            
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }
}

class RenqiRoomComparator implements Comparator<RoomInfo> {

    @Override
    public int compare(RoomInfo o1, RoomInfo o2) {
        int compareValue = 0;

        Integer peopleInRoom1 = o1.getPeopleInRoom();
        peopleInRoom1 = peopleInRoom1 == null ? 0 : peopleInRoom1;
        Integer peopleInRoom2 = o2.getPeopleInRoom();
        peopleInRoom2 = peopleInRoom2 == null ? 0 : peopleInRoom2;
        compareValue = peopleInRoom2 - peopleInRoom1;
        if (compareValue == 0) {
            Integer actorLevel1 = o1.getActorLevel();
            actorLevel1 = actorLevel1 == null ? 0 : actorLevel1;
            Integer actorLevel2 = o2.getActorLevel();
            actorLevel2 = actorLevel2 == null ? 0 : actorLevel2;
            compareValue = actorLevel2 - actorLevel1;
            if (compareValue == 0) {
                Date date1 = o1.getLiveStarttime();
                date1 = date1 == null ? new Date() : date1;
                Date date2 = o2.getLiveStarttime();
                date2 = date2 == null ? new Date() : date2;
                compareValue = (int) (date2.getTime() - date1.getTime());
            }
        }

        return compareValue;
    }

}

class RenqiHallRoomComparator implements Comparator<HallRoomInfoDTO> {

    @Override
    public int compare(HallRoomInfoDTO o1, HallRoomInfoDTO o2) {
        int compareValue = 0;

        Integer peopleInRoom1 = o1.getPeopleInRoom();
        peopleInRoom1 = peopleInRoom1 == null ? 0 : peopleInRoom1;
        Integer peopleInRoom2 = o2.getPeopleInRoom();
        peopleInRoom2 = peopleInRoom2 == null ? 0 : peopleInRoom2;
        compareValue = peopleInRoom2 - peopleInRoom1;
        if (compareValue == 0) {
            Integer actorLevel1 = o1.getActorLevel();
            actorLevel1 = actorLevel1 == null ? 0 : actorLevel1;
            Integer actorLevel2 = o2.getActorLevel();
            actorLevel2 = actorLevel2 == null ? 0 : actorLevel2;
            compareValue = actorLevel2 - actorLevel1;
            if (compareValue == 0) {
                Date date1 = o1.getLiveStarttime();
                date1 = date1 == null ? new Date() : date1;
                Date date2 = o2.getLiveStarttime();
                date2 = date2 == null ? new Date() : date2;
                compareValue = (int) (date2.getTime() - date1.getTime());
            }
        }

        return compareValue;
    }

}
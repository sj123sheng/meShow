package com.melot.kktv.action;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.common.melot_utils.StringUtils;
import com.melot.kk.module.resource.constant.ECloudTypeConstant;
import com.melot.kk.module.resource.constant.FileTypeConstant;
import com.melot.kk.module.resource.constant.ResourceStateConstant;
import com.melot.kk.module.resource.service.ResourceNewService;
import com.melot.kk.town.api.constant.UserRoleTypeEnum;
import com.melot.kk.town.api.constant.WorkCheckStatusEnum;
import com.melot.kk.town.api.constant.WorkTypeEnum;
import com.melot.kk.town.api.dto.*;
import com.melot.kk.town.api.param.TownUserInfoParam;
import com.melot.kk.town.api.param.TownWorkParam;
import com.melot.kk.town.api.service.*;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kktv.base.CommonStateCode;
import com.melot.kktv.base.Page;
import com.melot.kktv.base.Result;
import com.melot.kktv.domain.WorkVideoInfo;
import com.melot.kktv.model.Room;
import com.melot.kktv.service.UserRelationService;
import com.melot.kktv.service.WorkService;
import com.melot.kktv.util.*;
import org.apache.log4j.Logger;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static com.melot.kktv.util.ParamCodeEnum.*;

public class TownProjectFunctions {

    private static Logger logger = Logger.getLogger(TownProjectFunctions.class);

    @Resource
    TownWorkService townWorkService;

    @Resource
    TownUserService townUserService;

    @Resource
    ResourceNewService resourceNewService;

    @Resource
    KkUserService kkUserService;

    @Resource
    private TownUserRoleService townUserRoleService;

    @Resource
    private TagService tagService;

    @Resource
    private TownStarApplyInfoService townStarApplyInfoService;

    private static String SEPARATOR = "/";

    /**
     * 	获取本地新鲜的(作品、话题、直播间)列表【51120103】
     */
    public JsonObject getLocalFreshList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        String areaCode;
        int pageIndex, countPerPage;
        try {
            areaCode = CommonUtil.getJsonParamString(jsonObject, AREA_CODE.getId(), null, AREA_CODE.getErrorCode(), 1, Integer.MAX_VALUE);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 10, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {

            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error getLocalFreshList()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 发布作品【51120104】
     */
    public JsonObject publishWork(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        String areaCode, workUrl, topicName, workDesc;
        int userId, workType, topicId, mediaDur;
        try {
            areaCode = CommonUtil.getJsonParamString(jsonObject, AREA_CODE.getId(), null, AREA_CODE.getErrorCode(), 1, Integer.MAX_VALUE);
            workUrl = CommonUtil.getJsonParamString(jsonObject, WORK_URL.getId(), null, WORK_URL.getErrorCode(), 1, Integer.MAX_VALUE);
            topicName = CommonUtil.getJsonParamString(jsonObject, TOPIC_NAME.getId(), null, null, 1, Integer.MAX_VALUE);
            workDesc = CommonUtil.getJsonParamString(jsonObject, WORK_DESC.getId(), null, null, 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, USER_ID.getId(), 0, USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            workType = CommonUtil.getJsonParamInt(jsonObject, WORK_TYPE.getId(), 2, WORK_TYPE.getErrorCode(), 1, Integer.MAX_VALUE);
            topicId = CommonUtil.getJsonParamInt(jsonObject, TOPIC_ID.getId(), 0, null, 1, Integer.MAX_VALUE);
            mediaDur = CommonUtil.getJsonParamInt(jsonObject, "mediaDur", 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {

            String resIds = getResourceIds(workType, userId, mediaDur, workUrl);
            if(StringUtils.isEmpty(resIds)) {
                // 插入资源失败
                result.addProperty("TagCode", "06020009");
                return result;
            }

            TownWorkParam townWorkParam = new TownWorkParam();
            townWorkParam.setUserId(userId);
            townWorkParam.setAreaCode(areaCode);
            townWorkParam.setWorkType(workType);
            townWorkParam.setWorkDesc(workDesc);
            if(topicId > 0) {
                townWorkParam.setTopicId(topicId);
            }
            townWorkParam.setTopicName(topicName);
            String coverUrl;
            if(workType == WorkTypeEnum.IMAGE) {
                coverUrl = workUrl.split(",")[0];
                townWorkParam.setImageUrls(workUrl);
            }else {
                coverUrl = workUrl.substring(0, workUrl.lastIndexOf(".")) + ".jpg";
                townWorkParam.setVideoUrl(workUrl);
            }
            townWorkParam.setCoverUrl(coverUrl);
            townWorkParam.setResourceIds(resIds);

            Result<Boolean> publishResult = townWorkService.publishWork(townWorkParam);

            if(publishResult == null || !publishResult.getCode().equals(CommonStateCode.SUCCESS)) {
                result.addProperty("TagCode", publishResult.getCode());
                return result;
            }
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error publishWork()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 	获取话题信息【51120105】
     */
    public JsonObject getTopicInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        int topicId;
        try {
            topicId = CommonUtil.getJsonParamInt(jsonObject, TOPIC_ID.getId(), 0, TOPIC_ID.getErrorCode(), 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {

            ResTownTopicDTO townTopicDTO = townWorkService.getTopicInfo(topicId);
            if(townTopicDTO != null) {
                result.addProperty("topicName", townTopicDTO.getTopicName());
                result.addProperty("topicDesc", townTopicDTO.getTopicDesc());
                result.addProperty("coverUrl", townTopicDTO.getCoverUrl());
                Integer sponsorUserId = townTopicDTO.getSponsorUserId();
                if(sponsorUserId != null) {
                    UserProfile userProfile = kkUserService.getUserProfile(sponsorUserId);
                    if(userProfile != null) {
                        result.addProperty("sponsorGender", userProfile.getGender());
                        result.addProperty("sponsorPortrait", getPortrait(userProfile));
                        result.addProperty("sponsorNickname", userProfile.getNickName());
                    }
                    result.addProperty("sponsorUserId", sponsorUserId);
                }
                result.addProperty("participantsNum", townTopicDTO.getParticipantsNum());
                result.addProperty("workNum", townTopicDTO.getWorkNum());
                Integer firstWorkId = townTopicDTO.getSponsorFirstWorkId();
                if(firstWorkId != null) {
                    ResTownWorkDTO townWorkDTO = townWorkService.getWorkInfo(firstWorkId);
                    if(townWorkDTO != null) {
                        JsonObject sponsorFirstWork = new JsonObject();
                        sponsorFirstWork.addProperty("workId", townWorkDTO.getWorkId());
                        sponsorFirstWork.addProperty("praiseNum", townWorkDTO.getPraiseNum());
                        sponsorFirstWork.addProperty("coverUrl", townWorkDTO.getCoverUrl());
                        result.add("sponsorFirstWork", sponsorFirstWork);
                    }
                }
            }
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error getTopicInfo()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 	获取话题中的作品列表【51120106】
     */
    public JsonObject getTopicWorkList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        int topicId, workSort, pageIndex, countPerPage;
        try {
            topicId = CommonUtil.getJsonParamInt(jsonObject, TOPIC_ID.getId(), 0, TOPIC_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            workSort = CommonUtil.getJsonParamInt(jsonObject, WORK_SORT.getId(), 0, WORK_SORT.getErrorCode(), 1, Integer.MAX_VALUE);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 10, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {

            Page<ResTownWorkDTO> page = townWorkService.getTopicWorkList(topicId, workSort, pageIndex, countPerPage);
            JsonArray workList = new JsonArray();
            if(page != null && page.getList() != null && page.getList().size() > 0) {
                List<ResTownWorkDTO> townWorkDTOS = page.getList();
                for(ResTownWorkDTO townWorkDTO : townWorkDTOS) {
                    JsonObject townWork = new JsonObject();
                    townWork.addProperty("workId", townWorkDTO.getWorkId());
                    townWork.addProperty("praiseNum", townWorkDTO.getPraiseNum());
                    townWork.addProperty("viewsNum", townWorkDTO.getViewsNum());
                    townWork.addProperty("coverUrl", townWorkDTO.getCoverUrl());
                    workList.add(townWork);
                }
            }
            result.add("workList", workList);
            result.addProperty("workCount", page.getCount());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error getTopicWorkList()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 	获取我的作品列表【51120107】
     */
    public JsonObject getMyWorkList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        int userId, pageIndex, countPerPage;
        String token;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, USER_ID.getId(), 0, USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 10, null, 1, Integer.MAX_VALUE);
            token = CommonUtil.getJsonParamString(jsonObject, "token", null, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        boolean isOwner = false;
        // 查询本人作品列表需要验证token,未验证的返回错误码
        if(StringUtils.isNotEmpty(token)) {
            if (!checkTag) {
                result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
                return result;
            }
            isOwner = true;
        }

        try {

            Page<ResTownWorkDTO> page = townWorkService.getMyWorkList(userId, isOwner, pageIndex, countPerPage);
            JsonArray workList = new JsonArray();
            if(page != null && page.getList() != null && page.getList().size() > 0) {
                List<ResTownWorkDTO> townWorkDTOS = page.getList();
                for(ResTownWorkDTO townWorkDTO : townWorkDTOS) {
                    JsonObject townWork = new JsonObject();
                    townWork.addProperty("workId", townWorkDTO.getWorkId());
                    townWork.addProperty("praiseNum", townWorkDTO.getPraiseNum());
                    townWork.addProperty("viewsNum", townWorkDTO.getViewsNum());
                    townWork.addProperty("coverUrl", townWorkDTO.getCoverUrl());
                    workList.add(townWork);
                }
            }
            result.add("workList", workList);
            result.addProperty("workCount", page.getCount());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error getMyWorkList()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取用户信息(51120109)
     */
    public JsonObject getUserProfile(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }
        UserProfile userProfile = kkUserService.getUserProfile(userId);
        if(userProfile == null){
            result.addProperty("TagCode", TagCodeEnum.USER_NOT_EXIST);
            return result;
        }
        result.addProperty("userId",userProfile.getUserId());
        result.addProperty("nickname",userProfile.getNickName());
        if(userProfile.getPortrait()!=null){
            result.addProperty("portrait",ConfigHelper.getHttpdir() + userProfile.getPortrait());
        }
        result.addProperty("gender",userProfile.getGender());
        int followsCount = UserRelationService.getFollowsCount(userId);
        result.addProperty("followCount",followsCount);
        int fansCount = UserRelationService.getFansCount(userId);
        result.addProperty("fansCount",fansCount);

        TownUserInfoDTO townUserInfoDTO =  townUserService.getUserInfo(userId);
        if(townUserInfoDTO != null){
            if(townUserInfoDTO.getIntroduction()!=null){
                result.addProperty("introduction",townUserInfoDTO.getIntroduction());
            }
            if(townUserInfoDTO.getBirthday()!=null){
                result.addProperty("birthday",townUserInfoDTO.getBirthday());
            }
            List<UserTagRelationDTO> list =  tagService.getUserTagList(userId);
            if(!CollectionUtils.isEmpty(list)){
                StringBuilder tag = new StringBuilder();
                for(UserTagRelationDTO item : list){
                    tag.append(item.getTagName()).append(",");
                }
                result.addProperty("tag",tag.toString().substring(0,tag.length()-1));
            }
        }

        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 用户关注列表(51120110)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getUserFollowedList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        int userId, pageIndex, countPerPage, platform;

        //排序规则  默认:直播状态,1:关注时间
        Integer sortType = 1;

        // 获取参数
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 20, null, 1, 30);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, null, 1, Integer.MAX_VALUE);
            sortType = CommonUtil.getJsonParamInt(jsonObject, "sortType", 1, null, 0, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        }

        int pageTotal = 0;

        int followsCount = UserRelationService.getFollowsCount(userId);
        if (followsCount > 0) {
            pageTotal = (int) Math.ceil((double) followsCount / countPerPage);
        }
        result.addProperty("followsCount", followsCount);
        result.addProperty("pageTotal", pageTotal);

        if (pageTotal == 0 || pageIndex > pageTotal) {
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        }

        JsonArray jRoomList = new JsonArray();

        List<RoomInfo> roomList = null;

        //查看1000以上的关注人页按关注时间排序,没有排序必要
        if (pageIndex * countPerPage > 1000 || sortType == 1) {
            roomList = com.melot.kkcx.service.UserRelationService.getFollowByTime(userId, countPerPage, pageIndex);
        } else {
            roomList = com.melot.kkcx.service.UserRelationService.getFollowByLiveState(userId, followsCount, pageIndex, pageTotal, countPerPage, platform);
        }
        if (roomList != null) {
            List<Integer> userIdList = new ArrayList<>(roomList.size());
            for (RoomInfo roomInfo : roomList) {
                userIdList.add(roomInfo.getActorId());
            }
            Map<Integer,List<UserTagRelationDTO>> tagMap = tagService.getAllUserTagMap(userIdList);
            for (RoomInfo roomInfo : roomList) {
                JsonObject json = new JsonObject();
                json.addProperty("userId",roomInfo.getActorId());
                json.addProperty("nickname",roomInfo.getNickname());
                json.addProperty("roomId", roomInfo.getRoomId() != null ? roomInfo.getRoomId() : roomInfo.getActorId());
                json.addProperty("gender",roomInfo.getGender());
                json.addProperty("portrait_path_256", ConfigHelper.getHttpdir() + roomInfo.getPortrait()  + "!256");

                if(tagMap!=null && tagMap.containsKey(roomInfo.getActorId())){
                    List<UserTagRelationDTO> tagList = tagMap.get(roomInfo.getActorId());
                    if(!CollectionUtils.isEmpty(tagList)){
                        StringBuilder tag = new StringBuilder();
                        for(UserTagRelationDTO item : tagList){
                            tag.append(item.getTagName()).append(",");
                        }
                        json.addProperty("tag",tag.toString().substring(0,tag.length()-1));
                    }
                }
                jRoomList.add(json);
            }
        }
        result.add("roomList", jRoomList);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 用户粉丝列表(51120111)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getUserFansList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        // 该接口toke可选
        int selfTag = 0;
        if (checkTag) {
            selfTag = 1;
        }

        int userId = 0;
        int pageIndex = 1;
        int countPerPage = 20;
        int platform = 0;

        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "03040002", 1, Integer.MAX_VALUE);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 20, null, 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, null, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        int start = (pageIndex - 1) * countPerPage;
        int end = pageIndex * countPerPage - 1;
        int pageTotal = 0;

        int totalCount = UserRelationService.getFansCount(userId);
        if (totalCount > 0) {
            if (totalCount % countPerPage == 0) {
                pageTotal = (int) totalCount / countPerPage;
            } else {
                pageTotal = (int) (totalCount / countPerPage) + 1;
            }
        }
        result.addProperty("fansCount", totalCount);

        //不是自己查看仅返回粉丝数
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        }

        JsonArray jRoomList = new JsonArray();

        String fanIdsStr = UserRelationService.getFanIdsString(userId, start, end);
        if (fanIdsStr != null) {
            List<Room> roomList = getFansRoomList(fanIdsStr);
            if (roomList != null && roomList.size() > 0) {
                List<Integer> userIdList = new ArrayList<>(roomList.size());
                for (Room room : roomList) {
                    userIdList.add(room.getUserId());
                }

                Map<Integer,List<UserTagRelationDTO>> tagMap = tagService.getAllUserTagMap(userIdList);
                for (Room room : roomList) {
                    int roomId = room.getUserId();
                    JsonObject roomJson = new JsonObject();
                    roomJson.addProperty("userId",roomId);
                    roomJson.addProperty("nickname",room.getNickname());
                    roomJson.addProperty("gender",room.getGender());
                    roomJson.addProperty("portrait_path_256",ConfigHelper.getHttpdir() + room.getPortrait_path_256());

                    if(tagMap!=null && tagMap.containsKey(room.getUserId())){
                        List<UserTagRelationDTO> tagList = tagMap.get(room.getUserId());
                        if(!CollectionUtils.isEmpty(tagList)){
                            StringBuilder tag = new StringBuilder();
                            for(UserTagRelationDTO item : tagList){
                                tag.append(item.getTagName()).append(",");
                            }
                            roomJson.addProperty("tag",tag.toString().substring(0,tag.length()-1));
                        }
                    }

                    jRoomList.add(roomJson);
                }
            }
        }

        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        result.addProperty("pageTotal", pageTotal);
        result.add("roomList", jRoomList);

        return result;
    }

    private static List<Room> getFansRoomList(String fanIdsStr) {
        List<Room> roomList = new ArrayList<>();
        if (fanIdsStr != null) {
            String[] fanIdsArr = fanIdsStr.split(",");
            for (String fanId : fanIdsArr) {
                Room room = new Room();
                room.setUserId(Integer.valueOf(fanId));
                room.setMaxCount(0);
                room.setEnterConditionType("0");
                roomList.add(room);
            }
        }
        return roomList;
    }

    /**
     * 红人列表(51120108)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getStarList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        int pageIndex, countPerPage;
        String areaCode;
        try {
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 10, null, 1, Integer.MAX_VALUE);
            areaCode =  CommonUtil.getJsonParamString(jsonObject, "areaCode", null, AREA_CODE.getErrorCode(), 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }
        JsonArray jsonArray = new JsonArray();
        List<TownUserRoleDTO> list = townUserRoleService.getUserRoleList(pageIndex,countPerPage,areaCode,
                UserRoleTypeEnum.STAR);
        if(!CollectionUtils.isEmpty(list)){
            List<Integer> userIdList = new ArrayList<>(list.size());
            for(TownUserRoleDTO item : list){
                userIdList.add(item.getUserId());
            }
            Map<Integer,List<UserTagRelationDTO>> tagMap = tagService.getAllUserTagMap(userIdList);
            for(TownUserRoleDTO item : list){
                UserProfile userProfile = kkUserService.getUserProfile(item.getUserId());
                if(userProfile!=null){
                    JsonObject json = new JsonObject();
                    json.addProperty("userId",item.getUserId());
                    json.addProperty("nickname",userProfile.getNickName());
                    if(userProfile.getPortrait()!=null){
                        json.addProperty("portrait",ConfigHelper.getHttpdir() + userProfile.getPortrait());
                    }
                    if(tagMap!=null && tagMap.containsKey(item.getUserId())){
                        List<UserTagRelationDTO> tagList = tagMap.get(item.getUserId());
                        if(!CollectionUtils.isEmpty(tagList)){
                            StringBuilder tagString = new StringBuilder();
                            for(UserTagRelationDTO tag : tagList){
                                tagString.append(tag.getTagName()).append(",");
                            }
                            json.addProperty("tag",tagString.toString().substring(0,tagString.length()-1));
                        }
                    }
                    jsonArray.add(json);
                }
            }
        }
        result.add("list",jsonArray);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 	获取我的点赞作品列表【51120112】
     */
    public JsonObject getMyPraiseWorkList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        int userId, pageIndex, countPerPage;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, USER_ID.getId(), 0, USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 10, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {

            Page<ResTownWorkDTO> page = townWorkService.getMyPraiseWorkList(userId, pageIndex, countPerPage);
            JsonArray workList = new JsonArray();
            if(page != null && page.getList() != null && page.getList().size() > 0) {
                List<ResTownWorkDTO> townWorkDTOS = page.getList();
                for(ResTownWorkDTO townWorkDTO : townWorkDTOS) {
                    JsonObject townWork = new JsonObject();
                    townWork.addProperty("workId", townWorkDTO.getWorkId());
                    townWork.addProperty("praiseNum", townWorkDTO.getPraiseNum());
                    townWork.addProperty("viewsNum", townWorkDTO.getViewsNum());
                    townWork.addProperty("coverUrl", townWorkDTO.getCoverUrl());
                    workList.add(townWork);
                }
            }
            result.add("workList", workList);
            result.addProperty("workCount", page.getCount());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error getMyPraiseWorkList()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 点赞作品【51120113】
     */
    public JsonObject praiseWork(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId, workId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, USER_ID.getId(), 0, USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            workId = CommonUtil.getJsonParamInt(jsonObject, WORK_ID.getId(), 0, WORK_ID.getErrorCode(), 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {

            townWorkService.sendPraiseWorkMQ(userId, workId);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error praiseWork()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 根据话题名称搜索话题列表【51120114】
     */
    public JsonObject searchTopicList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        String areaCode, topicName;
        try {
            areaCode = CommonUtil.getJsonParamString(jsonObject, AREA_CODE.getId(), null, AREA_CODE.getErrorCode(), 1, Integer.MAX_VALUE);
            topicName = CommonUtil.getJsonParamString(jsonObject, TOPIC_NAME.getId(), null, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {

            List<ResTownTopicDTO> townTopicDTOS = townWorkService.getTopicList(areaCode, topicName);
            JsonArray topicList = new JsonArray();
            if(townTopicDTOS != null) {
                for (ResTownTopicDTO townTopicDTO : townTopicDTOS) {
                    JsonObject townTopic = new JsonObject();
                    townTopic.addProperty("topicId", townTopicDTO.getTopicId());
                    townTopic.addProperty("topicName", townTopicDTO.getTopicName());
                    topicList.add(townTopic);
                }
            }
            result.add("topicList", topicList);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error searchTopicList()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 申请红人(51120116)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject starApply(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        String areaCode;
        int userId;
        int applyType;
        String name;
        int age;
        int gender;
        String home;
        String mobilePhone;
        String profession;
        String experience;
        String reason;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "03040002", 1, Integer.MAX_VALUE);
            areaCode =  CommonUtil.getJsonParamString(jsonObject, "areaCode", null, AREA_CODE.getErrorCode(), 1, Integer.MAX_VALUE);
            applyType = CommonUtil.getJsonParamInt(jsonObject, "applyType", 0, APPLY_TYPE.getErrorCode(), 1, 5);
            name = CommonUtil.getJsonParamString(jsonObject, "name", null, NAME.getErrorCode(), 1, 200);
            age = CommonUtil.getJsonParamInt(jsonObject, "age", 0, AGE.getErrorCode(), 1, 200);
            gender = CommonUtil.getJsonParamInt(jsonObject, "gender", 0, GENDER.getErrorCode(), 0, 1);
            home =  CommonUtil.getJsonParamString(jsonObject, "home", null, HOME.getErrorCode(), 1, 500);
            mobilePhone = CommonUtil.getJsonParamString(jsonObject, "mobilePhone", null, MOBILE_PHONE.getErrorCode(), 11, 11);
            profession = CommonUtil.getJsonParamString(jsonObject, "profession", null, PROFESSION.getErrorCode(), 1, 1000);
            experience = CommonUtil.getJsonParamString(jsonObject, "experience", null, EXPERIENCE.getErrorCode(), 1, 1000);
            reason = CommonUtil.getJsonParamString(jsonObject, "reason", null, REASON.getErrorCode(), 1, 1000);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        TownStarApplyInfoDTO townStarApplyInfoDTO = new TownStarApplyInfoDTO();
        townStarApplyInfoDTO.setUserId(userId);
        townStarApplyInfoDTO.setAreaCode(areaCode);
        townStarApplyInfoDTO.setApplyType(applyType);
        townStarApplyInfoDTO.setName(name);
        townStarApplyInfoDTO.setAge(age);
        townStarApplyInfoDTO.setGender(gender);
        townStarApplyInfoDTO.setHome(home);
        townStarApplyInfoDTO.setMobilePhone(mobilePhone);
        townStarApplyInfoDTO.setProfession(profession);
        townStarApplyInfoDTO.setExperience(experience);
        townStarApplyInfoDTO.setReason(reason);
        townStarApplyInfoDTO.setCreateTime(new Date());

        Result<Boolean> applyResult = townStarApplyInfoService.addTownStarApplyInfo(townStarApplyInfoDTO);
        if(!CommonStateCode.SUCCESS.equals(applyResult.getCode())){
            if("PARAMETER_ERROR".equals(applyResult.getCode())){
                result.addProperty("TagCode",TagCodeEnum.PARAMETER_MISSING);
                return result;
            }
            if("APPLY_DATA_IS_EXIST".equals(applyResult.getCode())){
                result.addProperty("TagCode",TagCodeEnum.TOWN_APPLY_DATA_IS_EXIST);
                return result;
            }
            if(CommonStateCode.FAIL.equals(applyResult.getCode())){
                result.addProperty("TagCode",TagCodeEnum.EXECSQL_EXCEPTION);
                return result;
            }
        }
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 删除作品【51120117】
     */
    public JsonObject deleteWork(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId, workId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, USER_ID.getId(), 0, USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            workId = CommonUtil.getJsonParamInt(jsonObject, WORK_ID.getId(), 0, WORK_ID.getErrorCode(), 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {

            Result<Boolean> deleteResult = townWorkService.deletePraiseWork(userId, workId);
            if(!deleteResult.getCode().equals(CommonStateCode.SUCCESS)) {
                result.addProperty("TagCode", deleteResult.getCode());
                return result;
            }
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error deleteWork()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取作品信息【51120118】
     */
    public JsonObject getWorkInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        int userId, workId;
        String token;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, USER_ID.getId(), 0, null, 1, Integer.MAX_VALUE);
            workId = CommonUtil.getJsonParamInt(jsonObject, WORK_ID.getId(), 0, WORK_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            token = CommonUtil.getJsonParamString(jsonObject, "token", null, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        boolean isOwner = false;
        // 查询本人作品列表需要验证token,未验证的返回错误码
        if(StringUtils.isNotEmpty(token)) {
            if (!checkTag) {
                result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
                return result;
            }
            isOwner = true;
        }

        try {

            ResTownWorkDTO townWorkDTO = townWorkService.getWorkInfo(workId);
            if(townWorkDTO != null) {
                int checkStatus = townWorkDTO.getCheckStatus();
                if(isOwner) {
                    result.addProperty("workStatus", 1);
                } else if(checkStatus == WorkCheckStatusEnum.WAIT_CHECK) {
                    result.addProperty("workStatus", 2);
                } else {
                    result.addProperty("workStatus", 3);
                }
                result.addProperty("workType", townWorkDTO.getWorkType());
                result.addProperty("videoUrl", townWorkDTO.getVideoUrl());
                result.addProperty("imageUrls", townWorkDTO.getImageUrls());
                result.addProperty("topicId", townWorkDTO.getTopicId());
                result.addProperty("topicName", townWorkDTO.getTopicName());
                int workUserId = townWorkDTO.getUserId();
                result.addProperty("userId", workUserId);
                UserProfile userProfile = kkUserService.getUserProfile(workUserId);
                if(userProfile != null) {
                    result.addProperty("portrait", getPortrait(userProfile));
                }
                result.addProperty("praiseNum", townWorkDTO.getPraiseNum());
                result.addProperty("followed", UserRelationService.isFollowed(userId, workUserId));
            } else {
                result.addProperty("workStatus", 3);
            }

            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error getWorkInfo()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 取消点赞【51120119】
     */
    public JsonObject cancelPraiseWork(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId, workId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, USER_ID.getId(), 0, USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            workId = CommonUtil.getJsonParamInt(jsonObject, WORK_ID.getId(), 0, WORK_ID.getErrorCode(), 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {

            townWorkService.sendCancelPraiseWorkMQ(userId, workId);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error cancelPraiseWork()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 更新用户最近一次登录的乡镇编码【51120120】
     */
    public JsonObject updateUserLastAreaCode(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        String areaCode;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, USER_ID.getId(), 0, USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            areaCode = CommonUtil.getJsonParamString(jsonObject, AREA_CODE.getId(), null, AREA_CODE.getErrorCode(), 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {

            TownUserInfoParam userInfoParam = new TownUserInfoParam();
            userInfoParam.setUserId(userId);
            userInfoParam.setLastAreaCode(areaCode);
            townUserService.saveUserInfo(userInfoParam);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error updateUserLastAreaCode()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 下架作品【51120121】
     */
    public JsonObject offTheShelfWork(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId, workId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, USER_ID.getId(), 0, USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            workId = CommonUtil.getJsonParamInt(jsonObject, WORK_ID.getId(), 0, WORK_ID.getErrorCode(), 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {

            Result<Boolean> offReuslt = townWorkService.offTheShelfWork(userId, workId);
            if(!offReuslt.getCode().equals(CommonStateCode.SUCCESS)) {
                result.addProperty("TagCode", offReuslt.getCode());
                return result;
            }
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error offTheShelfWork()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 浏览作品(给作品浏览量加1)【51120122】
     */
    public JsonObject viewsWork(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        int workId;
        try {
            workId = CommonUtil.getJsonParamInt(jsonObject, WORK_ID.getId(), 0, WORK_ID.getErrorCode(), 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {

            townWorkService.sendViewsWorkMQ(workId);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error viewsWork()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    private String getResourceIds(int workType, int userId, int mediaDur, String workUrl) {

        String resIds = "";
        if (workType == WorkTypeEnum.VIDEO) {
            com.melot.kk.module.resource.domain.Resource resource = new com.melot.kk.module.resource.domain.Resource();
            resource.setState(ResourceStateConstant.uncheck);
            resource.setMimeType(FileTypeConstant.video);
            resource.setSpecificUrl(workUrl);
            resource.setUserId(userId);
            resource.setDuration(Long.valueOf(mediaDur));
            resource.setResType(14);
            // 获取分辨率,添加分辨率信息
            WorkVideoInfo videoInfo = WorkService.getVideoInfoByHttp(workUrl);
            if (videoInfo != null) {
                resource.setFileHeight(videoInfo.getHeight());
                resource.setFileWidth(videoInfo.getWidth());
            }
            if (!StringUtil.strIsNull(workUrl)) {
                workUrl = workUrl.replaceFirst(ConfigHelper.getHttpdir(), "");
                if(!workUrl.startsWith(SEPARATOR)) {
                    workUrl = SEPARATOR + workUrl;
                }
                workUrl = workUrl.replaceFirst("/kktv", "");
            }
            resource.seteCloudType(ECloudTypeConstant.qiniu);
            resource.setImageUrl(workUrl.substring(0, workUrl.lastIndexOf(".")) + ".jpg");
            Result<Integer> resIdResult = resourceNewService.addResource(resource);
            if(resIdResult != null && resIdResult.getCode() != null && resIdResult.getCode().equals(CommonStateCode.SUCCESS)){
                Integer resId = resIdResult.getData();
                if (resId > 0) {
                    resIds = String.valueOf(resId);
                }
            }
        } else if(workType == WorkTypeEnum.IMAGE) {
            String[] imageList = workUrl.split(",");
            List<com.melot.kk.module.resource.domain.Resource> resourceList = new ArrayList<>();
            for (int i = 0; i < imageList.length; i++) {
                String tempUrl = imageList[i];
                if (!StringUtil.strIsNull(tempUrl)) {
                    com.melot.kk.module.resource.domain.Resource resource = new com.melot.kk.module.resource.domain.Resource();
                    resource.setState(ResourceStateConstant.uncheck);
                    resource.setMimeType(FileTypeConstant.image);
                    resource.setResType(14);
                    resource.seteCloudType(ECloudTypeConstant.aliyun);
                    resource.setUserId(userId);
                    if (!StringUtil.strIsNull(tempUrl)) {
                        tempUrl = tempUrl.replaceFirst(ConfigHelper.getHttpdir(), "");
                        if (!workUrl.startsWith(SEPARATOR)) {
                            tempUrl = SEPARATOR + tempUrl;
                        }
                        tempUrl = tempUrl.replaceFirst("/kktv", "");
                    }
                    resource.setImageUrl(tempUrl);
                    resourceList.add(resource);
                }
            }
            Result<List<Integer>> resIdsResult = resourceNewService.addResources(resourceList);
            if (resIdsResult != null && resIdsResult.getCode() != null && resIdsResult.getCode().equals(CommonStateCode.SUCCESS)) {
                for (Integer i : resIdsResult.getData()) {
                    resIds = resIds + "," + i;
                }
                resIds = Pattern.compile("^,*").matcher(resIds).replaceAll("");
            }
        }
        return resIds;
    }

    private String getPortrait(UserProfile userProfile) {
        return userProfile.getPortrait() == null ? null : userProfile.getPortrait() + "!128";
    }

    /**
     * 本地红人(51120123)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject homePageStarList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        String areaCode;
        try {
            areaCode =  CommonUtil.getJsonParamString(jsonObject, "areaCode", null, AREA_CODE.getErrorCode(), 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }
        JsonArray jsonArray = new JsonArray();
        List<TownStarDTO> list = townUserRoleService.getTownStarList(areaCode,9);
        if(!CollectionUtils.isEmpty(list)){
            for(TownStarDTO item : list){
                UserProfile userProfile = kkUserService.getUserProfile(item.getUserId());
                if(userProfile!=null){
                    JsonObject json = new JsonObject();
                    json.addProperty("userId",item.getUserId());
                    json.addProperty("nickname",userProfile.getNickName());
                    if(userProfile.getPortrait()!=null){
                        json.addProperty("portrait",ConfigHelper.getHttpdir() + userProfile.getPortrait());
                    }
                    json.addProperty("tag",item.getTagName());
                    jsonArray.add(json);
                }
            }
        }
        result.add("list",jsonArray);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
}

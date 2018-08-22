package com.melot.kktv.action;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.common.melot_utils.StringUtils;
import com.melot.kk.location.api.service.LocationService;
import com.melot.kk.module.resource.constant.ECloudTypeConstant;
import com.melot.kk.module.resource.constant.FileTypeConstant;
import com.melot.kk.module.resource.constant.ResourceStateConstant;
import com.melot.kk.module.resource.service.ResourceNewService;
import com.melot.kk.town.api.constant.*;
import com.melot.kk.town.api.dto.*;
import com.melot.kk.town.api.param.TownUserInfoParam;
import com.melot.kk.town.api.param.TownWorkCommentParam;
import com.melot.kk.town.api.param.TownWorkParam;
import com.melot.kk.town.api.service.*;
import com.melot.kkcore.actor.service.ActorService;
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
import java.text.ParseException;
import java.util.*;
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

    @Resource
    private ActorService actorService;

    @Resource
    private TownMessageService townMessageService;

    @Resource
    private LocationService locationService;

    @Resource
    private TownCommentService townCommentService;

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
                        if(userProfile.getPortrait() != null) {
                            result.addProperty("sponsorPortrait", getPortrait(userProfile));
                        }
                        result.addProperty("sponsorNickname", userProfile.getNickName());
                    }
                    result.addProperty("sponsorUserId", sponsorUserId);
                }
                result.addProperty("participantsNum", townTopicDTO.getParticipantsNum());
                result.addProperty("workNum", townTopicDTO.getWorkNum());
                Integer firstWorkId = townTopicDTO.getSponsorFirstWorkId();
                if(firstWorkId != null) {
                    ResTownWorkDTO townWorkDTO = townWorkService.getWorkInfo(firstWorkId);
                    if(townWorkDTO != null && townWorkDTO.getCheckStatus() == WorkCheckStatusEnum.CHECK_PASS
                            && !townWorkDTO.getIsOffShelf()) {
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
                    townWork.addProperty("checkStatus", townWorkDTO.getCheckStatus());
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
        int targetUserId;
        String areaCode;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            targetUserId = CommonUtil.getJsonParamInt(jsonObject, "targetUserId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            areaCode =  CommonUtil.getJsonParamString(jsonObject, "areaCode", null, null, 1, 20);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }
        UserProfile loginUserProfile  = kkUserService.getUserProfile(userId);
        if(loginUserProfile == null){
            result.addProperty("TagCode", TagCodeEnum.USER_NOT_EXIST);
            return result;
        }
        UserProfile userProfile = kkUserService.getUserProfile(targetUserId);
        if(userProfile == null){
            result.addProperty("TagCode", TagCodeEnum.USER_NOT_EXIST);
            return result;
        }

        result.addProperty("userId",userProfile.getUserId());
        result.addProperty("nickname",userProfile.getNickName());
        if(userProfile.getPortrait()!=null){
            result.addProperty("portrait", this.getPortrait(userProfile));
        }
        result.addProperty("gender",userProfile.getGender());

        int followsCount = UserRelationService.getFollowsCount(targetUserId);
        result.addProperty("followCount",followsCount);

        int fansCount = UserRelationService.getFansCount(targetUserId);
        result.addProperty("fansCount",fansCount);

        boolean userFollowTarget = UserRelationService.isFollowed(userId,targetUserId);
        boolean targetFollowUser = UserRelationService.isFollowed(targetUserId,userId);
        if(userFollowTarget && targetFollowUser){
            result.addProperty("isFollow",1);
        }else{
           if(userFollowTarget){
               result.addProperty("isFollow",0);
           }else{
               result.addProperty("isFollow",-1);
           }
        }

        if(!StringUtils.isEmpty(areaCode)){
            TownUserRoleDTO townUserRoleDTO = townUserRoleService.getUserAreaRole(targetUserId,areaCode,
                    UserRoleTypeEnum.OWER);
            if(townUserRoleDTO != null){
                result.addProperty("isOwer",1);
            }else{
                result.addProperty("isOwer",0);
            }

            String areaName = locationService.getAreaNameByAreaCode(areaCode);
            if(!org.springframework.util.StringUtils.isEmpty(areaName)){
                result.addProperty("areaName",areaName);
            }
        }

        TownUserInfoDTO townUserInfoDTO =  townUserService.getUserInfo(targetUserId);
        if(townUserInfoDTO != null){
            if(townUserInfoDTO.getIntroduction()!=null){
                result.addProperty("introduction",townUserInfoDTO.getIntroduction());
            }
            if(!org.springframework.util.StringUtils.isEmpty(townUserInfoDTO.getBirthday())){
                result.addProperty("birthday",townUserInfoDTO.getBirthday());
                try {
                    Date birthDay = org.apache.commons.lang3.time.DateUtils.parseDate(townUserInfoDTO.getBirthday(),
                            "yyyy-MM-dd");
                    int age = this.getAge(birthDay);
                    result.addProperty("age",age);
                } catch (ParseException ex){
                    logger.error("parse birthday error birthday:"+townUserInfoDTO.getBirthday()+",ex:",ex);
                }
            }
            String tag = this.getUserTag(targetUserId);
            result.addProperty("tag",tag);
        }

        int unreadMsgCount = townMessageService.getUnreadMessageCount(targetUserId);
        result.addProperty("unreadMsgCount",unreadMsgCount);

        com.melot.kkcore.actor.api.RoomInfo roomInfo = actorService.getRoomInfoById(targetUserId);
        if(roomInfo != null){
            if(roomInfo.getRoomSource() != null){
                result.addProperty("roomSource",roomInfo.getRoomSource());
            }
            if(roomInfo.getLiveEndTime()!=null && roomInfo.getLiveEndTime()>0){
                result.addProperty("liveStatus",0);
            }else{
                result.addProperty("liveStatus",1);
            }
        }

        int workCount = townWorkService.getMyWorkNum(targetUserId);
        result.addProperty("workCount",workCount);

        int like = townWorkService.getMyPraiseWorkNum(targetUserId);
        result.addProperty("like",like);

        int receiveLike = townWorkService.getMyWorkPraiseNum(targetUserId);
        result.addProperty("receiveLike",receiveLike);

        result.addProperty("pathPrefix",ConfigHelper.getHttpdir());
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    private String getUserTag(int userId){
        List<UserTagRelationDTO> list =  tagService.getUserTagList(userId);
        if(!CollectionUtils.isEmpty(list)){
            StringBuilder tag = new StringBuilder();
            for(UserTagRelationDTO item : list){
                if(!org.springframework.util.StringUtils.isEmpty(item.getTagName())){
                    tag.append(item.getTagName()).append(",");
                }
            }
            return tag.toString().substring(0,tag.length()-1);
        }else{
            return "";
        }
    }

    private int getAge(Date birthday) {
        Calendar curr = Calendar.getInstance();
        Calendar born = Calendar.getInstance();
        born.setTime(birthday);
        int age = curr.get(Calendar.YEAR) - born.get(Calendar.YEAR);
        if (age <= 0) {
            return 0;
        }

        int currMonth = curr.get(Calendar.MONTH);
        int currDay = curr.get(Calendar.DAY_OF_MONTH);
        int bornMonth = born.get(Calendar.MONTH);
        int bornDay = born.get(Calendar.DAY_OF_MONTH);

        if (currMonth < bornMonth) {
            age--;
        }
        if(currMonth == bornMonth && currDay <= bornDay){
            age--;
        }

        if(age < 0){
            return 0;
        }
        int maxAge  =  120;
        if(age > maxAge){
            return maxAge;
        }
        return age;
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
                json.addProperty("portrait",  roomInfo.getPortrait()  + "!128");

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
                if(roomInfo.getRoomSource() != null){
                    json.addProperty("roomSource",roomInfo.getRoomSource());
                }

                if(roomInfo.getLiveEndtime()!=null){
                    json.addProperty("liveStatus",0);
                }else{
                    json.addProperty("liveStatus",1);
                }

                boolean isFollow = UserRelationService.isFollowed(roomInfo.getActorId(),userId);
                if(isFollow){
                    json.addProperty("isFollow",1);
                }else{
                    json.addProperty("isFollow",-1);
                }
                jRoomList.add(json);
            }
        }
        result.add("roomList", jRoomList);
        result.addProperty("pathPrefix",ConfigHelper.getHttpdir());
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
                    roomJson.addProperty("portrait", room.getPortrait_path_128());

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
                    if(room.getRoomSource() != null){
                        roomJson.addProperty("roomSource",room.getRoomSource());
                    }

                    if(room.getLiveendtime() != null){
                        roomJson.addProperty("liveStatus",0);
                    }else{
                        roomJson.addProperty("liveStatus",1);
                    }

                    boolean isFollow = UserRelationService.isFollowed(userId,room.getUserId());
                    if(isFollow){
                        roomJson.addProperty("isFollow",1);
                    }else{
                        roomJson.addProperty("isFollow",-1);
                    }

                    jRoomList.add(roomJson);
                }
            }
        }

        result.addProperty("pathPrefix",ConfigHelper.getHttpdir());
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
                        json.addProperty("portrait", this.getPortrait(userProfile));
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
                    com.melot.kkcore.actor.api.RoomInfo roomInfo = actorService.getRoomInfoById(item.getUserId());
                    if(roomInfo != null){
                        if(roomInfo.getRoomSource() != null){
                            json.addProperty("roomSource",roomInfo.getRoomSource());
                        }

                        if(roomInfo.getLiveEndTime()!=null && roomInfo.getLiveEndTime()>0){
                            json.addProperty("liveStatus",0);
                        }else{
                            json.addProperty("liveStatus",1);
                        }
                    }
                    jsonArray.add(json);
                }
            }
        }
        result.add("list",jsonArray);
        result.addProperty("pathPrefix",ConfigHelper.getHttpdir());
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

            Result<Boolean> deleteResult = townWorkService.deleteWork(userId, workId);
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
                boolean isOffShelf = townWorkDTO.getIsOffShelf();
                int workUserId = townWorkDTO.getUserId();
                if((isOwner && userId == workUserId) || (checkStatus == WorkCheckStatusEnum.CHECK_PASS && !isOffShelf)) {
                    result.addProperty("workStatus", 1);
                } else if(checkStatus == WorkCheckStatusEnum.WAIT_CHECK) {
                    result.addProperty("workStatus", 2);
                } else {
                    result.addProperty("workStatus", 3);
                }
                result.addProperty("workType", townWorkDTO.getWorkType());
                result.addProperty("isRecommend", townWorkDTO.getIsRecommend());
                result.addProperty("coverUrl", townWorkDTO.getCoverUrl());
                if(StringUtils.isNotEmpty(townWorkDTO.getVideoUrl())) {
                    result.addProperty("videoUrl", townWorkDTO.getVideoUrl());
                }
                if(StringUtils.isNotEmpty(townWorkDTO.getImageUrls())) {
                    result.addProperty("imageUrls", townWorkDTO.getImageUrls());
                }
                if(townWorkDTO.getTopicId() != null) {
                    result.addProperty("topicId", townWorkDTO.getTopicId());
                    result.addProperty("topicName", townWorkDTO.getTopicName());
                }
                result.addProperty("userId", workUserId);
                UserProfile userProfile = kkUserService.getUserProfile(workUserId);
                if(userProfile != null && userProfile.getPortrait() != null) {
                    result.addProperty("portrait", getPortrait(userProfile));
                }
                result.addProperty("praiseNum", townWorkDTO.getPraiseNum());
                int followStatus = 2;
                boolean followed = UserRelationService.isFollowed(userId, workUserId);
                boolean beFollowed = UserRelationService.isFollowed(workUserId, userId);
                if(followed && beFollowed) {
                    followStatus = 3;
                } else if(followed) {
                    followStatus = 1;
                }
                result.addProperty("followStatus", followStatus);
                boolean isPraise = false;
                if(userId > 0) {
                    isPraise = townWorkService.isPraiseWork(userId, workId);
                }
                result.addProperty("isPraise", isPraise);
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

            if (!StringUtil.strIsNull(workUrl)) {
                workUrl = workUrl.replaceFirst(ConfigHelper.getVideoURL(), "");
            }
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
            resource.seteCloudType(ECloudTypeConstant.qiniu);
            String imageUrl = workUrl.substring(0, workUrl.lastIndexOf(".")) + ".jpg";
            resource.setImageUrl(imageUrl);
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
                        if (!tempUrl.startsWith(SEPARATOR)) {
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
        List<TownStarDTO> list = townUserRoleService.getTownStarList(areaCode,10);
        if(!CollectionUtils.isEmpty(list)){
            for(TownStarDTO item : list){
                UserProfile userProfile = kkUserService.getUserProfile(item.getUserId());
                if(userProfile!=null){
                    JsonObject json = new JsonObject();
                    json.addProperty("userId",item.getUserId());
                    json.addProperty("nickname",userProfile.getNickName());
                    if(userProfile.getPortrait()!=null){
                        json.addProperty("portrait",this.getPortrait(userProfile));
                    }
                    json.addProperty("tag",item.getTagName());

                    com.melot.kkcore.actor.api.RoomInfo roomInfo = actorService.getRoomInfoById(item.getUserId());
                    if(roomInfo != null){
                        if(roomInfo.getRoomSource() != null){
                            json.addProperty("roomSource",roomInfo.getRoomSource());
                        }

                        if(roomInfo.getLiveEndTime()!=null && roomInfo.getLiveEndTime()>0){
                            json.addProperty("liveStatus",0);
                        }else{
                            json.addProperty("liveStatus",1);
                        }
                    }
                    jsonArray.add(json);
                }
            }
        }
        result.add("list",jsonArray);
        result.addProperty("pathPrefix",ConfigHelper.getHttpdir());
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 推荐作品【51120124】
     */
    public JsonObject recommendWork(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

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

            Result<Boolean> recommendReuslt = townWorkService.recommendWork(userId, workId);
            if(!recommendReuslt.getCode().equals(CommonStateCode.SUCCESS)) {
                result.addProperty("TagCode", recommendReuslt.getCode());
                return result;
            }
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error recommendWork()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取消息信息【51120125】
     */
    public JsonObject getMessageInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, USER_ID.getId(), 0, USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {

            TownMessageInfoDTO messageInfo = townMessageService.getMessageInfo(userId);
            if(messageInfo != null) {
                result.addProperty("followUnreadNum", messageInfo.getFollowUnreadNum());
                result.addProperty("praiseUnreadNum", messageInfo.getPraiseUnreadNum());
                result.addProperty("commentUnreadNum", messageInfo.getCommentUnreadNum());
                if(messageInfo.getSystemMessage() != null) {
                    JsonObject systemMessage = new JsonObject();
                    TownSystemMessageDTO systemMessageDTO = messageInfo.getSystemMessage();
                    systemMessage.addProperty("messageTitle", systemMessageDTO.getMessageTitle());
                    systemMessage.addProperty("messageContent", systemMessageDTO.getMessageContent());
                    if(messageInfo.getSystemUnreadNum() > 0) {
                        systemMessage.addProperty("unread", true);
                    }else {
                        systemMessage.addProperty("unread", false);
                    }
                    systemMessage.addProperty("sendTime", changeTimeToString(systemMessageDTO.getCreateTime()));
                    result.add("systemMessage", systemMessage);
                }
            }
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error getMessageInfo()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 	获取关注消息列表【51120126】
     */
    public JsonObject getFollowMessageList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

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

            Page<UserFollowMessageDTO> page = townMessageService.getFollowMessageList(userId, pageIndex, countPerPage);
            JsonArray messageList = new JsonArray();
            if(page != null && page.getList() != null && page.getList().size() > 0) {
                List<UserFollowMessageDTO> list = page.getList();
                for(UserFollowMessageDTO record : list) {
                    JsonObject messageJsonObject = new JsonObject();
                    int messageUserId = record.getUserId();
                    messageJsonObject.addProperty("userId", messageUserId);
                    UserProfile userProfile = kkUserService.getUserProfile(messageUserId);
                    if(userProfile != null) {
                        if(userProfile.getPortrait() != null) {
                            messageJsonObject.addProperty("portrait", getPortrait(userProfile));
                        }
                        messageJsonObject.addProperty("nickname", userProfile.getNickName());
                    }
                    messageJsonObject.addProperty("followTime", changeTimeToString(record.getFollowTime()));
                    messageList.add(messageJsonObject);
                }
            }

            result.add("messageList", messageList);
            result.addProperty("messageCount", page.getCount());
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error getFollowMessageList()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 	获取点赞消息列表【51120127】
     */
    public JsonObject getPraiseMessageList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

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

            Page<UserPraiseMessageDTO> page = townMessageService.getPraiseMessageList(userId, pageIndex, countPerPage);
            JsonArray messageList = new JsonArray();
            if(page != null && page.getList() != null && page.getList().size() > 0) {
                List<UserPraiseMessageDTO> list = page.getList();
                for(UserPraiseMessageDTO record : list) {
                    JsonObject messageJsonObject = new JsonObject();

                    int messageUserId = record.getUserId();
                    messageJsonObject.addProperty("userId", messageUserId);
                    UserProfile userProfile = kkUserService.getUserProfile(messageUserId);
                    if(userProfile != null) {
                        if(userProfile.getPortrait() != null) {
                            messageJsonObject.addProperty("portrait", getPortrait(userProfile));
                        }
                        messageJsonObject.addProperty("nickname", userProfile.getNickName());
                    }
                    messageJsonObject.addProperty("workId", record.getWorkId());
                    messageJsonObject.addProperty("coverUrl",record.getCoverUrl());
                    int praiseType = record.getPraiseType();
                    messageJsonObject.addProperty("praiseType", praiseType);
                    if(praiseType == PraiseTypeEnum.COMMENT_PRAISE) {
                        messageJsonObject.addProperty("commentId", record.getCommentId());
                        messageJsonObject.addProperty("commentMode", record.getCommentMode());
                        messageJsonObject.addProperty("commentContent",record.getCommentContent());
                        if(record.getCommentMode() == CommentModeEnum.VOICE) {
                            messageJsonObject.addProperty("voiceDuration", record.getVoiceDuration());
                        }
                    }
                    messageJsonObject.addProperty("praiseTime", changeTimeToString(record.getPraiseTime()));
                    messageList.add(messageJsonObject);
                }
            }

            result.add("messageList", messageList);
            result.addProperty("messageCount", page.getCount());
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error getPraiseMessageList()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 	获取系统消息列表【51120129】
     */
    public JsonObject getSystemMessageList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

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

            Page<TownSystemMessageDTO> page = townMessageService.getSystemMessageList(userId, pageIndex, countPerPage);
            JsonArray messageList = new JsonArray();
            if(page != null && page.getList() != null && page.getList().size() > 0) {
                List<TownSystemMessageDTO> list = page.getList();
                for(TownSystemMessageDTO record : list) {
                    JsonObject messageJsonObject = new JsonObject();

                    messageJsonObject.addProperty("messageTitle", record.getMessageTitle());
                    messageJsonObject.addProperty("messageContent",record.getMessageContent());
                    messageJsonObject.addProperty("sendTime", changeTimeToString(record.getCreateTime()));
                    messageList.add(messageJsonObject);
                }
            }

            result.add("messageList", messageList);
            result.addProperty("messageCount", page.getCount());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error getSystemMessageList()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    private String changeTimeToString(Date time) {

        String timeDesc;
        Date currentTime = DateUtils.getCurrentDate();
        long differSecond = (currentTime.getTime() - time.getTime()) / 1000;
        System.out.println(differSecond);
        if(differSecond < 60) {
            timeDesc = "刚刚";
        } else if(differSecond < 3600) {
            timeDesc = differSecond / 60 + "分钟前";
        } else if(differSecond < 3600 * 24) {
            timeDesc = differSecond / 3600 + "小时前";
        } else if(differSecond < 3600 * 48) {
            timeDesc = "昨天";
        } else if(DateUtils.getYear(currentTime) == DateUtils.getYear(time)) {
            timeDesc = DateUtils.format(time, "M-d");
        } else {
            timeDesc = DateUtils.format(time, "yyyy-M-d");
        }
        return timeDesc;
    }

    /**
     * 更新用户信息(51120130)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject updateUserProfile(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        String birthday;
        int gender;
        String introduction;
        String nickname;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "03040002", 1, Integer.MAX_VALUE);
            gender=CommonUtil.getJsonParamInt(jsonObject, "gender", -1, null, 0, 1);
            birthday = CommonUtil.getJsonParamString(jsonObject, "birthday", null, null, 1, 20);
            introduction = CommonUtil.getJsonParamString(jsonObject, "name", null, null, 1, 100);
            nickname = CommonUtil.getJsonParamString(jsonObject, "nickname", null, null, 1, 100);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        UserProfile userProfile = kkUserService.getUserProfile(userId);
        if(userProfile == null){
            result.addProperty("TagCode",TagCodeEnum.USER_NOT_EXIST);
            return result;
        }

        TownUserInfoDTO townUserInfoDTO = townUserService.getUserInfo(userId);
        if(townUserInfoDTO == null){
            result.addProperty("TagCode",TagCodeEnum.USER_NOT_EXIST);
            return result;
        }

        TownUserInfoParam townUserInfoParam = new TownUserInfoParam();
        townUserInfoParam.setUserId(userId);

        if(!org.springframework.util.StringUtils.isEmpty(birthday)){
            townUserInfoParam.setBirthday(birthday);
        }
        if(!org.springframework.util.StringUtils.isEmpty(introduction)){
            townUserInfoParam.setIntroduction(introduction);
        }
        boolean success = townUserService.saveUserInfo(townUserInfoParam);
        if(!success){
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }

        if(gender > -1){
            userProfile.setGender(gender);
            Map<String,Object> map = new HashMap<>();
            map.put("gender",gender);
            kkUserService.updateUserProfile(userId,map);
        }
        if(!org.springframework.util.StringUtils.isEmpty(nickname)){
            userProfile.setGender(gender);
            Map<String,Object> map = new HashMap<>();
            map.put("nickname",nickname);
            kkUserService.updateUserProfile(userId,map);
        }

        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 	获取热评区评论列表【51120131】
     */
    public JsonObject getHotCommentList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        int userId, workId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, USER_ID.getId(), 0, null, 1, Integer.MAX_VALUE);
            workId = CommonUtil.getJsonParamInt(jsonObject, WORK_ID.getId(), 0, WORK_ID.getErrorCode(), 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {

            List<TownWorkCommentDTO> commentDTOS = townCommentService.getHotCommentList(userId, workId);
            JsonArray commentList = new JsonArray();
            if(commentDTOS != null && commentDTOS.size() > 0) {
                for(TownWorkCommentDTO record : commentDTOS) {
                    JsonObject commentJsonObject = new JsonObject();
                    int commentUserId = record.getUserId();
                    commentJsonObject.addProperty("userId", commentUserId);
                    UserProfile userProfile = kkUserService.getUserProfile(commentUserId);
                    if(userProfile != null) {
                        if(userProfile.getPortrait() != null) {
                            result.addProperty("portrait", getPortrait(userProfile));
                        }
                        result.addProperty("nickname", userProfile.getNickName());
                    }
                    commentJsonObject.addProperty("identity", record.getIdentity());
                    commentJsonObject.addProperty("commentId", record.getCommentId());
                    commentJsonObject.addProperty("commentType", record.getCommentType());
                    commentJsonObject.addProperty("commentMode", record.getCommentMode());
                    commentJsonObject.addProperty("commentContent", record.getCommentContent());
                    if(record.getCommentMode() == CommentModeEnum.VOICE) {
                        commentJsonObject.addProperty("voiceDuration", record.getVoiceDuration());
                    }
                    if(record.getRefCommentId() != null) {
                        int refCommentUserId = record.getRefUserId();
                        commentJsonObject.addProperty("refUserId", refCommentUserId);
                        UserProfile userProfile1 = kkUserService.getUserProfile(refCommentUserId);
                        if(userProfile1 != null) {
                            result.addProperty("refNickname", userProfile1.getNickName());
                        }
                        commentJsonObject.addProperty("refCommentId", record.getRefCommentId());
                        commentJsonObject.addProperty("refCommentMode", record.getRefCommentMode());
                        commentJsonObject.addProperty("refCommentContent", record.getRefCommentContent());
                        if(record.getRefCommentMode() == CommentModeEnum.VOICE) {
                            commentJsonObject.addProperty("refVoiceDuration", record.getRefVoiceDuration());
                        }
                    }
                    commentJsonObject.addProperty("praiseNum", record.getPraiseNum());
                    commentJsonObject.addProperty("isPraise", record.getPraise());
                    commentJsonObject.addProperty("commentTime", changeTimeToString(record.getCommentTime()));

                    commentList.add(commentJsonObject);
                }
            }

            result.add("commentList", commentList);
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error getHotCommentList()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 	获取全部评论列表【51120132】
     */
    public JsonObject getCommentList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        int userId, workId, pageIndex, countPerPage;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, USER_ID.getId(), 0, null, 1, Integer.MAX_VALUE);
            workId = CommonUtil.getJsonParamInt(jsonObject, WORK_ID.getId(), 0, WORK_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 10, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {

            Page<TownWorkCommentDTO> page = townCommentService.getCommentListByWorkId(userId, workId, pageIndex, countPerPage);
            JsonArray commentList = new JsonArray();
            if(page != null && page.getList() != null && page.getList().size() > 0) {
                List<TownWorkCommentDTO> commentDTOS = page.getList();
                for(TownWorkCommentDTO record : commentDTOS) {
                    JsonObject commentJsonObject = new JsonObject();
                    int commentUserId = record.getUserId();
                    commentJsonObject.addProperty("userId", commentUserId);
                    UserProfile userProfile = kkUserService.getUserProfile(commentUserId);
                    if(userProfile != null) {
                        if(userProfile.getPortrait() != null) {
                            result.addProperty("portrait", getPortrait(userProfile));
                        }
                        result.addProperty("nickname", userProfile.getNickName());
                    }
                    commentJsonObject.addProperty("identity", record.getIdentity());
                    commentJsonObject.addProperty("commentId", record.getCommentId());
                    commentJsonObject.addProperty("commentType", record.getCommentType());
                    commentJsonObject.addProperty("commentMode", record.getCommentMode());
                    commentJsonObject.addProperty("commentContent", record.getCommentContent());
                    if(record.getCommentMode() == CommentModeEnum.VOICE) {
                        commentJsonObject.addProperty("voiceDuration", record.getVoiceDuration());
                    }
                    if(record.getRefCommentId() != null) {
                        int refCommentUserId = record.getRefUserId();
                        commentJsonObject.addProperty("refUserId", refCommentUserId);
                        UserProfile userProfile1 = kkUserService.getUserProfile(refCommentUserId);
                        if(userProfile1 != null) {
                            result.addProperty("refNickname", userProfile1.getNickName());
                        }
                        commentJsonObject.addProperty("refCommentId", record.getRefCommentId());
                        commentJsonObject.addProperty("refCommentMode", record.getRefCommentMode());
                        commentJsonObject.addProperty("refCommentContent", record.getRefCommentContent());
                        if(record.getRefCommentMode() == CommentModeEnum.VOICE) {
                            commentJsonObject.addProperty("refVoiceDuration", record.getRefVoiceDuration());
                        }
                    }
                    commentJsonObject.addProperty("praiseNum", record.getPraiseNum());
                    commentJsonObject.addProperty("isPraise", record.getPraise());
                    commentJsonObject.addProperty("commentTime", changeTimeToString(record.getCommentTime()));

                    commentList.add(commentJsonObject);
                }
            }

            result.addProperty("commentCount", page.getCount());
            result.add("commentList", commentList);
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error getCommentList()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 发布评论【51120133】
     */
    public JsonObject publishComment(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        String commentContent;
        int userId, workId, voiceDuration;
        long refCommentId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, USER_ID.getId(), 0, USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            workId = CommonUtil.getJsonParamInt(jsonObject, WORK_ID.getId(), 0, WORK_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            commentContent = CommonUtil.getJsonParamString(jsonObject, COMMENT_CONTENT.getId(), null, COMMENT_CONTENT.getErrorCode(), 1, Integer.MAX_VALUE);
            refCommentId = CommonUtil.getJsonParamLong(jsonObject, "refCommentId", 0l, null, 1, Long.MAX_VALUE);
            voiceDuration = CommonUtil.getJsonParamInt(jsonObject, "voiceDuration", 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {

            TownWorkCommentParam commentParam = new TownWorkCommentParam();
            commentParam.setUserId(userId);
            commentParam.setWorkId(workId);
            commentParam.setCommentContent(commentContent);
            if(voiceDuration > 0) {
                commentParam.setVoiceDuration(voiceDuration);
            }
            if(refCommentId > 0) {
                commentParam.setRefCommentId(refCommentId);
            }

            Result<Boolean> publishResult = townCommentService.publishComment(commentParam);

            if(publishResult == null || !publishResult.getCode().equals(CommonStateCode.SUCCESS)) {
                result.addProperty("TagCode", publishResult.getCode());
                return result;
            }
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error publishComment()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 点赞评论【51120134】
     */
    public JsonObject praiseComment(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        long commentId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, USER_ID.getId(), 0, USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            commentId = CommonUtil.getJsonParamLong(jsonObject, COMMENT_ID.getId(), 0, COMMENT_ID.getErrorCode(), 1, Long.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {

            townCommentService.praiseComment(userId, commentId);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error praiseComment()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 取消点赞评论【51120135】
     */
    public JsonObject cancelPraiseComment(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        long commentId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, USER_ID.getId(), 0, USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            commentId = CommonUtil.getJsonParamLong(jsonObject, COMMENT_ID.getId(), 0, COMMENT_ID.getErrorCode(), 1, Long.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {

            townCommentService.cancelPraiseComment(userId, commentId);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error cancelPraiseComment()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 	获取作品页面的评论列表【51120136】
     */
    public JsonObject getWorkPageCommentList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        int workId;
        try {
            workId = CommonUtil.getJsonParamInt(jsonObject, WORK_ID.getId(), 0, WORK_ID.getErrorCode(), 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {

            List<TownWorkCommentDTO> commentDTOS = townCommentService.getWorkPageCommentList(workId);
            JsonArray commentList = new JsonArray();
            if(commentDTOS != null && commentDTOS.size() > 0) {
                for(TownWorkCommentDTO record : commentDTOS) {
                    JsonObject commentJsonObject = new JsonObject();
                    int commentUserId = record.getUserId();
                    commentJsonObject.addProperty("userId", commentUserId);
                    UserProfile userProfile = kkUserService.getUserProfile(commentUserId);
                    if(userProfile != null) {
                        result.addProperty("nickname", userProfile.getNickName());
                    }
                    String identity = record.getIdentity();
                    boolean isAuthor = false;
                    if(StringUtils.isNotEmpty(identity) && identity.equals("作者")) {
                        isAuthor = true;
                    }
                    commentJsonObject.addProperty("isAuthor", isAuthor);
                    commentJsonObject.addProperty("commentId", record.getCommentId());
                    commentJsonObject.addProperty("commentMode", record.getCommentMode());
                    commentJsonObject.addProperty("commentContent", record.getCommentContent());
                    if(record.getCommentMode() == CommentModeEnum.VOICE) {
                        commentJsonObject.addProperty("voiceDuration", record.getVoiceDuration());
                    }
                    commentJsonObject.addProperty("praiseNum", record.getPraiseNum());
                    commentJsonObject.addProperty("commentTime", changeTimeToString(record.getCommentTime()));

                    commentList.add(commentJsonObject);
                }
            }

            long commentNum =  townCommentService.getCommentNum(workId);
            result.addProperty("commentNum", commentNum);
            result.add("commentList", commentList);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error getWorkPageCommentList()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 设置评论违规【51120137】
     */
    public JsonObject setCommentViolation(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        long commentId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, USER_ID.getId(), 0, USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            commentId = CommonUtil.getJsonParamLong(jsonObject, COMMENT_ID.getId(), 0, COMMENT_ID.getErrorCode(), 1, Long.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {

            Result<Boolean> setResult = townCommentService.setCommentViolation(userId, commentId);
            if(!setResult.getCode().equals(CommonStateCode.SUCCESS)) {
                result.addProperty("TagCode", setResult.getCode());
                return result;
            }
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error setCommentViolation()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取站长申请状态(51120138)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getStarApplyState(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int  userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "03040002", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }
        List<TownStarApplyInfoDTO> list =
                townStarApplyInfoService.getUserApplyInfoListByStatus(userId,TownStarCheckStatusEnum.IN_REVIEW);
        if(!CollectionUtils.isEmpty(list)){
            result.addProperty("applyId",list.get(0).getApplyId());
        }else{
            result.addProperty("applyId",0);
        }
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
}

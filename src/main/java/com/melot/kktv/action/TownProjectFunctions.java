package com.melot.kktv.action;

import com.alibaba.fastjson.support.odps.udf.JSONArrayAdd;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.common.melot_utils.StringUtils;
import com.melot.kk.town.api.constant.UserRoleTypeEnum;
import com.melot.kk.town.api.constant.WorkCheckStatusEnum;
import com.melot.kk.town.api.constant.WorkTypeEnum;
import com.melot.kk.town.api.dto.*;
import com.melot.kk.town.api.param.TownUserInfoParam;
import com.melot.kk.town.api.param.TownWorkParam;
import com.melot.kk.town.api.service.TagService;
import com.melot.kk.town.api.service.TownUserRoleService;
import com.melot.kk.town.api.service.TownUserService;
import com.melot.kk.town.api.service.TownWorkService;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kktv.base.CommonStateCode;
import com.melot.kktv.base.Page;
import com.melot.kktv.base.Result;
import com.melot.kktv.service.UserRelationService;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.TagCodeEnum;
import org.apache.log4j.Logger;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.melot.kktv.util.ParamCodeEnum.*;

public class TownProjectFunctions {

    private static Logger logger = Logger.getLogger(TownProjectFunctions.class);

    @Resource
    TownWorkService townWorkService;

    @Resource
    TownUserService townUserService;

    @Resource
    KkUserService kkUserService;

    @Resource
    private TownUserRoleService townUserRoleService;

    @Resource
    private TagService tagService;

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

        String areaCode, workUrl, topicName, workDesc, resourceIds;
        int userId, workType, topicId;
        try {
            areaCode = CommonUtil.getJsonParamString(jsonObject, AREA_CODE.getId(), null, AREA_CODE.getErrorCode(), 1, Integer.MAX_VALUE);
            workUrl = CommonUtil.getJsonParamString(jsonObject, WORK_URL.getId(), null, WORK_URL.getErrorCode(), 1, Integer.MAX_VALUE);
            topicName = CommonUtil.getJsonParamString(jsonObject, TOPIC_NAME.getId(), null, null, 1, Integer.MAX_VALUE);
            workDesc = CommonUtil.getJsonParamString(jsonObject, WORK_DESC.getId(), null, null, 1, Integer.MAX_VALUE);
            resourceIds = CommonUtil.getJsonParamString(jsonObject, RESOURCE_IDS.getId(), null, RESOURCE_IDS.getErrorCode(), 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, USER_ID.getId(), 0, USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            workType = CommonUtil.getJsonParamInt(jsonObject, WORK_TYPE.getId(), 2, WORK_TYPE.getErrorCode(), 1, Integer.MAX_VALUE);
            topicId = CommonUtil.getJsonParamInt(jsonObject, TOPIC_ID.getId(), 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {

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
            if(workType == WorkTypeEnum.image) {
                coverUrl = workUrl.split(",")[0];
                townWorkParam.setImageUrls(workUrl);
            }else {
                coverUrl = workUrl.substring(0, workUrl.lastIndexOf(".")) + ".jpg";
                townWorkParam.setVideoUrl(workUrl);
            }
            townWorkParam.setCoverUrl(coverUrl);
            townWorkParam.setResourceIds(resourceIds);

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
     * 	获取话题信息(作品、话题、直播间)列表【51120105】
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
            if(page != null && page.getCount() > 0) {
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
            if(page != null && page.getCount() > 0) {
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
     * 本地红人
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getStarList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        int userId, pageIndex, countPerPage;
        String areaCode;
        try {
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 10, null, 1, Integer.MAX_VALUE);
            areaCode =  CommonUtil.getJsonParamString(jsonObject, "areaCode", null, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }
        JsonArray jsonArray = new JsonArray();
        List<TownUserRoleDTO> list = townUserRoleService.getUserRoleList(pageIndex,countPerPage,areaCode, UserRoleTypeEnum.STAR);
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
                        json.addProperty("portrait",userProfile.getPortrait());
                    }
                    if(tagMap!=null && tagMap.containsKey(item.getUserId())){
                        List<UserTagRelationDTO> tagList = tagMap.get(item.getUserId());
                        if(!CollectionUtils.isEmpty(tagList)){
                            JsonArray tagArray  =  new JsonArray();
                            for(UserTagRelationDTO tag : tagList){
                                JsonObject tagJson = new JsonObject();
                                tagJson.addProperty("tag",tag.getTagName());
                                tagArray.add(tagJson);
                            }
                            json.add("tag",tagArray);
                        }
                    }
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
            if(page != null && page.getCount() > 0) {
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

    private String getPortrait(UserProfile userProfile) {
        return userProfile.getPortrait() == null ? null : userProfile.getPortrait() + "!128";
    }

}

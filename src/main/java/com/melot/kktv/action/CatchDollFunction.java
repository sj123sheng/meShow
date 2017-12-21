package com.melot.kktv.action;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.common.driver.base.ResultCode;
import com.melot.common.driver.domain.AgoraInfo;
import com.melot.common.driver.service.ConfigInfoService;
import com.melot.kk.doll.api.constant.CatchDollRecordStatusEnum;
import com.melot.kk.doll.api.constant.DollMachineStatusEnum;
import com.melot.kk.doll.api.constant.ExchangeStatusEnum;
import com.melot.kk.doll.api.domain.DO.*;
import com.melot.kk.doll.api.domain.queryDO.AppRecordQueryDO;
import com.melot.kk.doll.api.service.CatchDollRecordService;
import com.melot.kk.doll.api.service.DollMachineService;
import com.melot.kkcore.actor.api.RoomInfo;
import com.melot.kkcore.actor.service.ActorService;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kktv.base.CommonStateCode;
import com.melot.kktv.base.Result;
import com.melot.kktv.service.GeneralService;
import com.melot.kktv.util.*;
import com.melot.sdk.core.util.MelotBeanFactory;
import com.melot.stream.driver.service.LiveStreamConfigService;
import com.melot.stream.driver.service.domain.ClientDetail;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Title: CatchDollFunction
 * <p>
 * Description:直播抓娃娃相关接口
 * </p>
 * 
 * @author shengjian
 * @version V1.0
 * @since 2017年10月12日
 */
public class CatchDollFunction {

    private static Logger logger = Logger.getLogger(CatchDollFunction.class);
    
    private static final String KEY = "YdsSH&@#Uyh";

    /**
     * 秒 1s = 1000ms
     */
    private static final long SECOND = 1 * 1000L;

    /**
     * 分钟
     */
    private static final long MINUTE = 60 * SECOND;

    /**
     * 小时
     */
    private static final long HOUR = 60 * MINUTE;

    /**
     * 天数
     */
    private static final long DAY = 24 * HOUR;

    /**
     * 51060201
     * 监听直播精灵是否正常推流(直播精灵定时发送心跳请求)
     */
    public JsonObject monitorPushFlow(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        int dollMachineId, pushFlowStatus, cameraId;
        String sign;
        try {
            dollMachineId = CommonUtil.getJsonParamInt(jsonObject, "dollMachineId", 0, null, 1, Integer.MAX_VALUE);
            pushFlowStatus = CommonUtil.getJsonParamInt(jsonObject, "pushFlowStatus", 0, null, 1, Integer.MAX_VALUE);
            cameraId = CommonUtil.getJsonParamInt(jsonObject, "cameraId", 0, null, 1, Integer.MAX_VALUE);
            sign = CommonUtil.getJsonParamString(jsonObject, "sign", null, "05110101", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        if(dollMachineId == 0 || pushFlowStatus == 0 || cameraId == 0) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_MISSING);
            return result;
        }

        // 校验参数
        if (!checkSign(dollMachineId, cameraId , pushFlowStatus, sign)) {
            result.addProperty("TagCode", "5110901");
            return result;
        }

        try {

            DollMachineService dollMachineService = (DollMachineService) MelotBeanFactory.getBean("dollMachineService");

            dollMachineService.updateRecentHeartbeatStatus(dollMachineId, cameraId, pushFlowStatus);

            Result<DollMachineDO> dollMachineDOResult = dollMachineService.getDollMachineDOByDollMachineId(dollMachineId);
            if(!dollMachineDOResult.getCode().equals(CommonStateCode.SUCCESS) || dollMachineDOResult.getData() == null) {
                result.addProperty("TagCode", "5110902");
                return result;
            }

            DollMachineDO dollMachineDO = dollMachineDOResult.getData();
            int roomId = dollMachineDO.getRoomId();
            int primaryCameraId = dollMachineDO.getPrimaryCameraId();
            int secondaryCameraId = dollMachineDO.getSecondaryCameraId();
            Integer secondaryCameraStatus = null;
            if(cameraId == primaryCameraId) {
                secondaryCameraStatus = dollMachineService.getRecentHeartbeatStatus(dollMachineId, secondaryCameraId).getData();
            }else if(cameraId == secondaryCameraId) {
                secondaryCameraStatus = dollMachineService.getRecentHeartbeatStatus(dollMachineId, primaryCameraId).getData();
            }

            ActorService actorService = (ActorService) MelotBeanFactory.getBean("actorService");
            RoomInfo roomInfo = actorService.getRoomInfoById(roomId);
            Long liveEndTime = roomInfo.getLiveEndTime();
            boolean isLive = true;
            if(liveEndTime != null) {
                isLive = false;
            }

            if(pushFlowStatus == 1 && !isLive && secondaryCameraStatus != null && secondaryCameraStatus == 1) {
                Map<String,Object> param = Maps.newHashMap();
                param.put("liveType", LiveTypeEnum.PC);
                param.put("roomSource", 16);
                param.put("liveStartTime", DateUtils.getCurrentDate().getTime());
                param.put("liveEndTime", -1L);
                actorService.updateRoomInfoById(roomId, param);
            }else if(pushFlowStatus == 2 && isLive) {
                Map<String,Object> param = Maps.newHashMap();
                param.put("liveEndTime", DateUtils.getCurrentDate().getTime());
                actorService.updateRoomInfoById(roomId, param);
            }

            // 同步第三方娃娃机状态到系统表中
            dollMachineService.synchronizesDollMachineStatus(dollMachineId);

            //添加返回信息
            result.addProperty("result", true);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error monitorPushFlow()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取娃娃机直播间信息【51060202】
     */
    public JsonObject getDollMachineRoomInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        int dollMachineId, roomId;
        String sign;
        try {
            dollMachineId = CommonUtil.getJsonParamInt(jsonObject, "dollMachineId", 0, null, 1, Integer.MAX_VALUE);
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, null, 1, Integer.MAX_VALUE);
            sign = CommonUtil.getJsonParamString(jsonObject, "sign", null, "05110101", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        if(dollMachineId == 0 && roomId == 0) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_MISSING);
            return result;
        }

        // 校验参数
        if (!checkSign(dollMachineId, roomId, sign)) {
            result.addProperty("TagCode", "5110901");
            return result;
        }

        try {

            DollMachineService dollMachineService = (DollMachineService) MelotBeanFactory.getBean("dollMachineService");

            Result<DollMachineDO> dollMachineDOResult;
            DollMachineDO dollMachineDO;
            if(dollMachineId > 0) {
                dollMachineDOResult = dollMachineService.getDollMachineDOByDollMachineId(dollMachineId);
            }else {
                dollMachineDOResult = dollMachineService.getDollMachineDOByRoomId(roomId);
            }
            if(dollMachineDOResult.getCode().equals(CommonStateCode.SUCCESS) && dollMachineDOResult.getData() != null) {
                dollMachineDO = dollMachineDOResult.getData();
            }else {
                result.addProperty("TagCode", "5110902");
                return result;
            }

            roomId = dollMachineDO.getRoomId();

            ConfigInfoService configInfoService = MelotBeanFactory.getBean("configInfoService", ConfigInfoService.class);
            com.melot.common.driver.base.Result<AgoraInfo> agoraInfoResult = configInfoService.getAgoraInfo(roomId, 16);
            if (agoraInfoResult == null || agoraInfoResult.getCode() == null) {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            if (ResultCode.SUCCESS.equals(agoraInfoResult.getCode())) {
                AgoraInfo info = agoraInfoResult.getData();
                result.addProperty("appId", info.getAppId());
                result.addProperty("channelId", info.getChannelId());
            }else {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }

            LiveStreamConfigService liveStreamConfigService = (LiveStreamConfigService) MelotBeanFactory.getBean("liveStreamConfigService");
            ClientDetail detail = new ClientDetail();
            detail.setActorId(roomId);
            detail.setUserId(roomId);
            detail.setClientIp(GeneralService.getIpAddr(request, AppIdEnum.AMUSEMENT, PlatformEnum.WEB, null));
            detail.setCdnType(1);
            String resObj = liveStreamConfigService.getPushStreamAddress(detail);
            String pushStream = JSONObject.parseObject(resObj).get("pushStream") + "/" + JSONObject.parseObject(resObj).get("pushCode");

            result.addProperty("roomId", roomId);
            result.addProperty("pushStream", pushStream);
            result.addProperty("primaryCameraId", dollMachineDO.getPrimaryCameraId());
            result.addProperty("secondaryCameraId", dollMachineDO.getSecondaryCameraId());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error getDollMachineRoomInfo()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取娃娃机直播间详情【51060203】
     */
    public JsonObject getDollMachineRoomDetail(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        int roomId;
        try {
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        if(roomId == 0) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_MISSING);
            return result;
        }

        try {

            DollMachineService dollMachineService = (DollMachineService) MelotBeanFactory.getBean("dollMachineService");

            Result<DollMachineDO> dollMachineDOResult = dollMachineService.getDollMachineDOByRoomId(roomId);
            DollMachineDO dollMachineDO;
            if(dollMachineDOResult.getCode().equals(CommonStateCode.SUCCESS)  && dollMachineDOResult.getData() != null) {
                dollMachineDO = dollMachineDOResult.getData();
            }else {
                result.addProperty("TagCode", "5110902");
                return result;
            }

            Result<RedisDollMachineDO> redisDollMachineDOResult = dollMachineService.getRedisDollMachineDO(roomId);
            RedisDollMachineDO redisDollMachineDO;
            if(redisDollMachineDOResult.getCode().equals(CommonStateCode.SUCCESS)) {
                redisDollMachineDO = redisDollMachineDOResult.getData();
            }else {
                result.addProperty("TagCode", "5110903");
                return result;
            }

            Integer dollMachineStatus = redisDollMachineDO.getStatus();
            Integer userId = redisDollMachineDO.getRecentStartGameUserId();
            if(dollMachineStatus == null) {
                dollMachineStatus = dollMachineDO.getStatus();
            }

            if((dollMachineStatus == DollMachineStatusEnum.PLAY || dollMachineStatus == DollMachineStatusEnum.WAIT_COIN) && userId != null) {
                KkUserService userService = MelotBeanFactory.getBean("kkUserService", KkUserService.class);
                UserProfile userProfile = userService.getUserProfile(userId);
                if(userProfile != null) {
                    result.addProperty("userId", userId);
                    result.addProperty("nickName", userProfile.getNickName());
                    if(StringUtils.isNotEmpty(userProfile.getPortrait())) {
                        result.addProperty("portrait", ConfigHelper.getHttpdir() + userProfile.getPortrait());
                    }

                }
            }

            result.addProperty("dollMachineId", dollMachineDO.getDollMachineId());
            result.addProperty("dollMachineStatus", dollMachineStatus);
            result.addProperty("dollDesc", dollMachineDO.getDollDesc());
            result.addProperty("dollPictureUrl", dollMachineDO.getDollPictureUrl());
            result.addProperty("price", dollMachineDO.getPrice());
            result.addProperty("gameTime", dollMachineDO.getGameTime());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error getDollMachineRoomDetail()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }

    /**
     * 获取娃娃机直播间最近抓中记录列表(不分页 取最近10条记录)【51060204】
     */
    public JsonObject getRoomCatchRecords(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        int roomId;
        try {
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        if(roomId == 0) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_MISSING);
            return result;
        }

        try {

            CatchDollRecordService catchDollRecordService = (CatchDollRecordService) MelotBeanFactory.getBean("catchDollRecordService");

            Result<List<CatchDollRecordDO>> recentRecordsResult = catchDollRecordService.getRecentRecordsByRoomId(roomId);
            if(!recentRecordsResult.getCode().equals(CommonStateCode.SUCCESS)){
                result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }

            List<CatchDollRecordDO> catchDollRecordDOs = recentRecordsResult.getData();

            JsonArray recentRecordList = new JsonArray();
            if(catchDollRecordDOs != null) {
                for(CatchDollRecordDO catchDollRecordDO : catchDollRecordDOs) {
                    JsonObject recentRecordJson = new JsonObject();
                    recentRecordJson.addProperty("portrait", StringUtils.defaultIfEmpty(catchDollRecordDO.getPortrait(), ""));
                    recentRecordJson.addProperty("nickName", catchDollRecordDO.getNickName());
                    String catchEndTime = getCatchEndTime(catchDollRecordDO);
                    recentRecordJson.addProperty("catchEndTime", catchEndTime);
                    recentRecordList.add(recentRecordJson);
                }
            }

            result.add("catchRecords", recentRecordList);
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error getRoomCatchRecords()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }

    private String getCatchEndTime(CatchDollRecordDO catchDollRecordDO) {
        String catchEndTime = "";
        Date now = DateUtils.getCurrentDate();
        Date endTime = catchDollRecordDO.getEndTime();
        long headway = now.getTime() - endTime.getTime();
        if(headway < 60 * SECOND) {
            catchEndTime = "刚刚";
        }else if (headway < 60 * MINUTE) {
            catchEndTime = headway / MINUTE  + "分钟前";
        }else if (headway < 24 * HOUR) {
            catchEndTime = headway / HOUR  + "小时前";
        }else if (headway < 2 * DAY) {
            catchEndTime = "昨天";
        }else if (headway <= 7 * DAY) {
            catchEndTime = headway / DAY  + "天前";
        }else if (DateUtils.getYear(now) == DateUtils.getYear(endTime)){
            catchEndTime = DateUtils.format(endTime, "MM/dd");
        }else {
            catchEndTime = DateUtils.format(endTime, DateUtils.DATE_FMT_1);
        }
        return catchEndTime;
    }

    /**
     * 获取我的娃娃信息(总局数，娃娃票数量)【51060205】
     */
    public JsonObject getMyDollInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        if(userId == 0) {
            result.addProperty("TagCode", TagCodeEnum.USERID_MISSING);
            return result;
        }

        try {

            CatchDollRecordService catchDollRecordService = (CatchDollRecordService) MelotBeanFactory.getBean("catchDollRecordService");

            Result<MyDollInfoDO> myDollInfoDOResult = catchDollRecordService.getMyDollInfo(userId);
            MyDollInfoDO myDollInfoDO;
            if(myDollInfoDOResult.getCode().equals(CommonStateCode.SUCCESS)  && myDollInfoDOResult.getData() != null) {
                myDollInfoDO = myDollInfoDOResult.getData();
            }else {
                result.addProperty("TagCode", "5110902");
                return result;
            }

            result.addProperty("gameTotalCount", myDollInfoDO.getGameTotalCount());
            result.addProperty("dollTicketCount", myDollInfoDO.getDollTicketCount());
            result.addProperty("unExchangeCount", myDollInfoDO.getUnExchangeCount());
            result.addProperty("exchangedCount", myDollInfoDO.getExchangedCount());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error getMyDollInfo()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }

    /**
     * 获取我的娃娃未兑换列表【51060206】
     */
    public JsonObject getMyDollUnExchanges(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId, pageIndex, countPerPage;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 0, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 20, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        if(userId == 0) {
            result.addProperty("TagCode", TagCodeEnum.USERID_MISSING);
            return result;
        }

        try {

            CatchDollRecordService catchDollRecordService = (CatchDollRecordService) MelotBeanFactory.getBean("catchDollRecordService");

            AppRecordQueryDO queryDO = new AppRecordQueryDO();
            queryDO.setUserId(userId);
            queryDO.setExchangeStatus(ExchangeStatusEnum.UNEXCHANGE);

            queryDO.setPageIndex(pageIndex);
            queryDO.setCountPerPage(countPerPage);
            Result<CatchDollRecordPageDO> catchDollRecordPageDOResult = catchDollRecordService.getAppRecords(queryDO);

            CatchDollRecordPageDO catchDollRecordPageDO;
            if(catchDollRecordPageDOResult.getCode().equals(CommonStateCode.SUCCESS)) {
                catchDollRecordPageDO = catchDollRecordPageDOResult.getData();
            }else {
                result.addProperty("TagCode", "5110902");
                return result;
            }

            JsonArray unExchangesRecordList = new JsonArray();

            if(catchDollRecordPageDO.getCatchDollRecordDOs() != null) {
                for (CatchDollRecordDO catchDollRecordDO : catchDollRecordPageDO.getCatchDollRecordDOs()) {
                    JsonObject unExchangesRecordJson = new JsonObject();
                    unExchangesRecordJson.addProperty("catchDollRecordId", catchDollRecordDO.getCatchDollRecordId());
                    unExchangesRecordJson.addProperty("dollName", catchDollRecordDO.getDollName());
                    unExchangesRecordJson.addProperty("exchangeNum", catchDollRecordDO.getExchangeNum());
                    unExchangesRecordJson.addProperty("pictureUrl", catchDollRecordDO.getDollPictureUrl());
                    unExchangesRecordJson.addProperty("catchTime", catchDollRecordDO.getEndTime().getTime());
                    unExchangesRecordList.add(unExchangesRecordJson);
                }
            }


            result.addProperty("count", catchDollRecordPageDO.getTotalCount());
            result.add("unExchanges", unExchangesRecordList);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error getMyDollUnExchanges()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }

    /**
     * 获取我的娃娃已兑换列表【51060207】
     */
    public JsonObject getMyDollExchangeds(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId, pageIndex, countPerPage;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 0, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 20, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        if(userId == 0) {
            result.addProperty("TagCode", TagCodeEnum.USERID_MISSING);
            return result;
        }

        try {

            CatchDollRecordService catchDollRecordService = (CatchDollRecordService) MelotBeanFactory.getBean("catchDollRecordService");

            AppRecordQueryDO queryDO = new AppRecordQueryDO();
            queryDO.setUserId(userId);
            queryDO.setExchangeStatus(2);

            queryDO.setPageIndex(pageIndex);
            queryDO.setCountPerPage(countPerPage);
            Result<CatchDollRecordPageDO> catchDollRecordPageDOResult = catchDollRecordService.getAppRecords(queryDO);

            CatchDollRecordPageDO catchDollRecordPageDO;
            if(catchDollRecordPageDOResult.getCode().equals(CommonStateCode.SUCCESS)) {
                catchDollRecordPageDO = catchDollRecordPageDOResult.getData();
            }else {
                result.addProperty("TagCode", "5110902");
                return result;
            }

            JsonArray exchangedsRecordList = new JsonArray();

            if(catchDollRecordPageDO.getCatchDollRecordDOs() != null) {
                for (CatchDollRecordDO catchDollRecordDO : catchDollRecordPageDO.getCatchDollRecordDOs()) {
                    JsonObject exchangedsRecordJson = new JsonObject();

                    exchangedsRecordJson.addProperty("catchDollRecordId", catchDollRecordDO.getCatchDollRecordId());
                    exchangedsRecordJson.addProperty("dollName", catchDollRecordDO.getDollName());
                    exchangedsRecordJson.addProperty("exchangedNum", catchDollRecordDO.getExchangeNum());
                    exchangedsRecordJson.addProperty("pictureUrl", catchDollRecordDO.getDollPictureUrl());
                    exchangedsRecordJson.addProperty("catchTime", catchDollRecordDO.getEndTime().getTime());

                    exchangedsRecordJson.addProperty("exchangeStatus", catchDollRecordDO.getExchangeStatus());
                    exchangedsRecordJson.addProperty("exchangeTime", catchDollRecordDO.getExchangeTime().getTime());
                    exchangedsRecordJson.addProperty("waybillNumber", catchDollRecordDO.getWaybillNumber());
                    exchangedsRecordJson.addProperty("courierCompany", catchDollRecordDO.getCourierCompany());
                    exchangedsRecordJson.addProperty("consignee", catchDollRecordDO.getConsignee());
                    exchangedsRecordJson.addProperty("mobile", catchDollRecordDO.getMobile());
                    exchangedsRecordJson.addProperty("address", catchDollRecordDO.getAddress());

                    exchangedsRecordList.add(exchangedsRecordJson);
                }
            }


            result.addProperty("count", catchDollRecordPageDO.getTotalCount());
            result.add("exchangeds", exchangedsRecordList);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error getMyDollExchangeds()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }

    /**
     * 申请发货【51060208】
     */
    public JsonObject applyForDelivery(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int catchDollRecordId;
        String consignee, mobile, address;
        try {
            catchDollRecordId = CommonUtil.getJsonParamInt(jsonObject, "catchDollRecordId", 0, null, 1, Integer.MAX_VALUE);
            consignee = CommonUtil.getJsonParamString(jsonObject, "consignee", "", null, 1, Integer.MAX_VALUE);
            mobile = CommonUtil.getJsonParamString(jsonObject, "mobile", "", null, 1, Integer.MAX_VALUE);
            address = CommonUtil.getJsonParamString(jsonObject, "address", "", null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        if(catchDollRecordId == 0 || StringUtils.isEmpty(consignee) || StringUtils.isEmpty(mobile) || StringUtils.isEmpty(address)) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_MISSING);
            return result;
        }

        try {

            CatchDollRecordService catchDollRecordService = (CatchDollRecordService) MelotBeanFactory.getBean("catchDollRecordService");

            Result<Boolean> applyDeliveryResult = catchDollRecordService.applyDelivery(catchDollRecordId, consignee, mobile, address);

            boolean  applyResult = true;
            if(applyDeliveryResult.getCode().equals(CommonStateCode.SUCCESS)) {
                applyResult = applyDeliveryResult.getData();
            }else {
                result.addProperty("TagCode", "5110902");
                return result;
            }

            result.addProperty("result", applyResult);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error applyForDelivery()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }

    /**
     * 兑换娃娃票【51060209】
     */
    public JsonObject exchangeDollTickets(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int catchDollRecordId;
        try {
            catchDollRecordId = CommonUtil.getJsonParamInt(jsonObject, "catchDollRecordId", 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        if(catchDollRecordId == 0) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_MISSING);
            return result;
        }

        try {

            CatchDollRecordService catchDollRecordService = (CatchDollRecordService) MelotBeanFactory.getBean("catchDollRecordService");

            Result<Boolean> exchangeDollTicketResult = catchDollRecordService.exchangeDollTicket(catchDollRecordId);

            boolean exchangeResult = true;
            if(exchangeDollTicketResult.getCode().equals(CommonStateCode.SUCCESS)) {
                exchangeResult = exchangeDollTicketResult.getData();
            }else {
                result.addProperty("TagCode", "5110902");
                return result;
            }

            result.addProperty("result", exchangeResult);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error exchangeDollTickets()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }

    /**
     * 51060210
     * 投币失败
     */
    public JsonObject coinFailed(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int catchDollRecordId, userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
            catchDollRecordId = CommonUtil.getJsonParamInt(jsonObject, "catchDollRecordId", 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        if(catchDollRecordId == 0 || userId == 0) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_MISSING);
            return result;
        }

        try {

            DollMachineService dollMachineService = (DollMachineService) MelotBeanFactory.getBean("dollMachineService");
            CatchDollRecordService catchDollRecordService = (CatchDollRecordService) MelotBeanFactory.getBean("catchDollRecordService");

            Result<CatchDollRecordDO> catchDollRecordDOResult = catchDollRecordService.getCatchDollRecordDO(catchDollRecordId);
            if(catchDollRecordDOResult.getData() == null) {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            CatchDollRecordDO catchDollRecordDO = catchDollRecordDOResult.getData();
            int roomId = catchDollRecordDO.getRoomId();
            int status = catchDollRecordDO.getStatus();

            if(status == CatchDollRecordStatusEnum.NOT_COIN && userId == catchDollRecordDO.getUserId()) {
                // 更新游戏记录状态为投币失败
                catchDollRecordService.updateRecordStatus(catchDollRecordId, CatchDollRecordStatusEnum.COIN_FAIL);

                // 更新娃娃机状态为准备就绪(空闲中) 并通知房间所有用户
                dollMachineService.updateRedisDollMachineStatus(roomId, DollMachineStatusEnum.READY);
            }

            // 添加返回信息
            result.addProperty("result", true);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error coinFailed()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 开始游戏【51060211】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject play(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int roomId, userId, platform, version;
        try {
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, null, 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, null, 1, Integer.MAX_VALUE);
            version = CommonUtil.getJsonParamInt(jsonObject, "v", 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        if(roomId == 0 || userId == 0 || platform == 0) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_MISSING);
            return result;
        }

        if(version < 145 && platform == PlatformEnum.IPHONE) {
            result.addProperty("TagCode", TagCodeEnum.LOW_VERSION_EXCEPTION);
            return result;
        }

        try {

            DollMachineService dollMachineService = (DollMachineService) MelotBeanFactory.getBean("dollMachineService");

            Result<StartGameDO> startGameDOResult = dollMachineService.startGame(roomId, userId, platform);
            StartGameDO startGameDO = new StartGameDO();
            if(startGameDOResult.getCode().equals(CommonStateCode.SUCCESS) && startGameDOResult.getData() != null) {
                startGameDO = startGameDOResult.getData();
            }else if(startGameDOResult.getData() != null){
                result.addProperty("TagCode", startGameDOResult.getData().getTagCode());
                return result;
            }

            result.addProperty("wsUrl", startGameDO.getWsUrl());
            result.addProperty("catchDollRecordId", startGameDO.getCatchDollRecordId());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error play()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }

    /**
     * 51060212
     * 不想再抓(放弃再来一次)
     */
    public JsonObject giveUp(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int roomId, userId;
        try {
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, null, 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        if(roomId == 0 || userId == 0) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_MISSING);
            return result;
        }

        try {

            DollMachineService dollMachineService = (DollMachineService) MelotBeanFactory.getBean("dollMachineService");

            RedisDollMachineDO redisDollMachineDO = dollMachineService.getRedisDollMachineDO(roomId).getData();
            Integer status = redisDollMachineDO.getStatus();
            Integer recentStartGameUserId = redisDollMachineDO.getRecentStartGameUserId();
            if(status != null && status == DollMachineStatusEnum.WAIT_COIN && recentStartGameUserId != null && recentStartGameUserId == userId) {

               // 更新娃娃机缓存状态为准备就绪(空闲中) 并通知房间所有用户
               dollMachineService.updateRedisDollMachineStatus(roomId, DollMachineStatusEnum.READY);
            }

            //添加返回信息
            result.addProperty("result", true);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error giveUp()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取我的最近一次发货信息【51060213】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getMyRecentDeliveryInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        if(userId == 0) {
            result.addProperty("TagCode", TagCodeEnum.USERID_MISSING);
            return result;
        }

        try {

            CatchDollRecordService catchDollRecordService = (CatchDollRecordService) MelotBeanFactory.getBean("catchDollRecordService");

            Result<CatchDollRecordDO> recentDeliveryDOResult = catchDollRecordService.getRecentDeliverDOByUserId(userId);
            CatchDollRecordDO catchDollRecordDO = new CatchDollRecordDO();
            if(recentDeliveryDOResult.getCode().equals(CommonStateCode.SUCCESS)) {
                catchDollRecordDO = recentDeliveryDOResult.getData();
            }else {
                result.addProperty("TagCode", "5110902");
                return result;
            }

            if(catchDollRecordDO != null) {
                result.addProperty("consignee", catchDollRecordDO.getConsignee());
                result.addProperty("mobile", catchDollRecordDO.getMobile());
                result.addProperty("address", catchDollRecordDO.getAddress());
            }
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error getMyRecentDeliveryInfo()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }

    /**
     * 游戏结束 【51060214】
     * 更新娃娃机状态为游戏结束等待投币 返回游戏结果
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject gameOver(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int roomId, catchDollRecordId;
        try {
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, null, 1, Integer.MAX_VALUE);
            catchDollRecordId = CommonUtil.getJsonParamInt(jsonObject, "catchDollRecordId", 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        if(roomId == 0 || catchDollRecordId == 0) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_MISSING);
            return result;
        }

        try {

            DollMachineService dollMachineService = (DollMachineService) MelotBeanFactory.getBean("dollMachineService");

            // 获取第三方游戏记录的抓取结果
            Result<Boolean> catchResult = dollMachineService.getThirdCatchResultByCatchDollRecordId(catchDollRecordId);
            if(!catchResult.getCode().equals(CommonStateCode.SUCCESS) || catchResult.getData() == null) {
                result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }

            // 更新娃娃机状态为游戏结束等待投币
            RedisDollMachineDO redisDollMachineDO = dollMachineService.getRedisDollMachineDO(roomId).getData();
            Integer status = redisDollMachineDO.getStatus();
            if(status != null && status == DollMachineStatusEnum.PLAY) {
                dollMachineService.updateRedisDollMachineStatus(roomId, DollMachineStatusEnum.WAIT_COIN);
            }

            //添加返回信息
            result.addProperty("catchResult", catchResult.getData());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error gameOver()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 校验参数
     * @return
     */
    private static boolean checkSign(int dollMachineId, int cameraId, int pushFlowStatus, String sign) {
        StringBuilder builder = new StringBuilder();
        builder.append(KEY);
        builder.append("cameraId=");
        builder.append(cameraId);
        builder.append("&dollMachineId=");
        builder.append(dollMachineId);
        builder.append("&pushFlowStatus=");
        builder.append(pushFlowStatus);
        builder.append(KEY);

        String param = builder.toString();
        String signTemp = CommonUtil.md5(param);
        if (signTemp.equals(sign)) {
            return true;
        }
        return false;
    }

    /**
     * 校验参数
     * @return
     */
    private static boolean checkSign(int dollMachineId, int roomId, String sign) {
        StringBuilder builder = new StringBuilder();
        builder.append(KEY);
        if(dollMachineId == 0) {
            builder.append("roomId=");
            builder.append(roomId);
        }else {
            builder.append("dollMachineId=");
            builder.append(dollMachineId);
        }
        builder.append(KEY);
        
        String param = builder.toString();
        String signTemp = CommonUtil.md5(param);
        if (signTemp.equals(sign)) {
            return true;
        }
        return false;
    }

    public static void main(String[] args) {

        checkSign(0, 10002534, "123");
    }
}

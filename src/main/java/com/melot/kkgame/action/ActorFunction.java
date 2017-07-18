/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.action;

import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.api.menu.sdk.service.RoomInfoService;
import com.melot.content.config.apply.service.ApplyActorService;
import com.melot.content.config.domain.ApplyActor;
import com.melot.game.config.sdk.actor.service.ActorLiveInfoService;
import com.melot.game.config.sdk.domain.ActorLiveInfo;
import com.melot.ios.idfa.driver.UpIdfaService;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kkcx.service.GeneralService;
import com.melot.kkgame.logger.HadoopLogger;
import com.melot.kkgame.redis.ActorInfoSource;
import com.melot.kkgame.redis.GamblingSource;
import com.melot.kkgame.redis.support.RedisException;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.sdk.core.util.MelotBeanFactory;
import com.melot.stream.driver.service.LiveStreamConfigService;
import com.melot.stream.driver.service.domain.ClientDetail;

/**
 * Title: ActorFunction
 * <p>
 * Description: 主播一些状态的查询接口 
 * </p>
 * @author  姚国平<a href="mailto:guoping.yao@melot.cn">
 * @version V1.0
 * @since 2015-7-29 下午3:18:36 
 */
public class ActorFunction extends BaseAction {

    
    private static Logger logger = Logger.getLogger(ActorFunction.class);
    private ActorInfoSource actorInfoSource;
    private GamblingSource gamblingSource;
    
    private ActorLiveInfo defaultActorLiveInfo;
    
    public void setActorInfoSource(ActorInfoSource actorInfoSource) {
        this.actorInfoSource = actorInfoSource;
    }
    
    public void setGamblingSource(GamblingSource gamblingSource) {
        this.gamblingSource = gamblingSource;
    }

    /***
     *  
     *  10008000 - 获取主播当次直播的相关信息 
     * 
     */
    public JsonObject getActorInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty(TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        int userId;
        try{
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        try {
            KkUserService kkUserService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
            UserProfile userProfile = kkUserService.getUserProfile(userId);
            int actorLevel = userProfile.getActorLevel();
            
            long time = actorInfoSource.getLoudspeakerTime(userId); //获取主播在自己房间发送小喇叭次数
            result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
            result.addProperty("userId", userId);
            result.addProperty("hornTime", time);
            
            if (gamblingSource.isOnWhiteList(userId)) { //先判断主播是否在白名单
                result.addProperty("canGamble", 1);
            } else { 
                if (gamblingSource.canGambling(userId) && actorLevel > GamblingFunction.ACTOR_CAN_GAMBLE_LEVEL) { //非白名单用户必须主播等级达到10并且不在黑名单
                    result.addProperty("canGamble", 1);
                } else {
                    result.addProperty("canGamble", 0);
                }
            } 
        } catch (RedisException e) {
            logger.error("get redis error", e);
            result.addProperty(TAG_CODE, TagCodeEnum.IRREGULAR_RESULT);
            return result;
        }
        return result;
    }
    
    /**
     * 修改房间主题（10005055）
     * 
     */
    public JsonObject changeRoomTheme(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty(TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        int userId;
        String roomTheme;
        try{
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
            roomTheme = CommonUtil.getJsonParamString(jsonObject, "roomTheme", null, "05550001", 1, 50);
            if (GeneralService.hasSensitiveWords(userId, roomTheme)) {
                result.addProperty("TagCode", "05550003");
                return result;
            }
        }catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        RoomInfoService roomInfoServie = MelotBeanFactory.getBean("roomInfoService", RoomInfoService.class);
        RoomInfo roomInfo = new RoomInfo();
        roomInfo.setActorId(userId);
        roomInfo.setRoomTheme(roomTheme);
        boolean updateFlag = roomInfoServie.updateRoomInfo(roomInfo);
        if (updateFlag) {
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } else {
            result.addProperty("TagCode", TagCodeEnum.FAIL_TO_UPDATE);
        }
        return result;
    }
    
    /**
     * 获取主播屏幕分辨率信息  [fucTag=60001001]
     * 
     */
    public JsonObject getActorLiveInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        Integer userId = null;
        
        try{
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        ActorLiveInfoService actorLiveInfoService = MelotBeanFactory.getBean("actorLiveInfoService", ActorLiveInfoService.class);
        ActorLiveInfo query = actorLiveInfoService.getActorLiveInfoByActorId(userId);
        if (query == null) {
            if (defaultActorLiveInfo == null) {
                defaultActorLiveInfo = actorLiveInfoService.getActorLiveInfoByActorId(0);
            }
            logger.info("获取的是默认的屏幕分辨率设置");
            result = actorLiveInfo2Json(defaultActorLiveInfo, result);
        } else {
            result = actorLiveInfo2Json(query, result);
        }
        
        result.addProperty("actorId", userId);
        result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
        return result;
    }

    
    /**
     * 获取主播拉流地址  [fucTag=60001002]
     * 
     * 1. 先获取房间是否配置了第三方流地址
     * 
     * 2. 计算防盗链地址 http://pull-g.kktv8.com/livekktv/91524813.flv?level=5&wsSecret=b847146f1cef849e389298d6db2bc243&wsTime=1447738493
     */
    public JsonObject getActorLiveFlowAddress(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        int roomId, appId, userId, type;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 100, Integer.MAX_VALUE);
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, "03031001", 100, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 1, Integer.MAX_VALUE);
            type = CommonUtil.getJsonParamInt(jsonObject, "type", 0, null, 0, Integer.MAX_VALUE);
        } catch (Exception ex) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            LiveStreamConfigService liveStreamConfigService = (LiveStreamConfigService) MelotBeanFactory.getBean("liveStreamConfigService");
            ClientDetail detail = new ClientDetail();
            detail.setActorId(roomId);
            detail.setUserId(userId);
            detail.setClientIp(com.melot.kktv.service.GeneralService.getIpAddr(request, appId, appId, null));
            String resObj;
            
            if (type == 1) {
                resObj = liveStreamConfigService.getP2PPullStreamAddress(detail);
            }else {
                resObj = liveStreamConfigService.getPullStreamAddress(detail);
            }
            JsonParser jsonParser = new JsonParser();
            result = jsonParser.parse(resObj).getAsJsonObject();
        } catch (Exception e) {
            result.addProperty(TAG_CODE, TagCodeEnum.REDIS_ERROR);
            logger.error("RedisException error", e);
            return result;
        }
        result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     *  客户端获取推流地址   [fucTag=60001004]
     */
    public JsonObject getPushStreamAddress(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        Integer userId = null;
        if (!checkTag) {
            result.addProperty(TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        String version = null;
        Integer cdnType = null;
        int type;
        int appId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            version = CommonUtil.getJsonParamString(jsonObject, "version", null, null, 0, 40);
            cdnType = CommonUtil.getJsonParamInt(jsonObject, "cdnType", 0, null, 0, 40);
            type = CommonUtil.getJsonParamInt(jsonObject, "type", 0, null, 0, Integer.MAX_VALUE);
            if (cdnType == 0) { // 客户端第一次获取推流地址应使用上次设置的线路类别
                cdnType = null;
            }
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        if (version != null) {
            HadoopLogger.getHadoopLogger().info("actor_push_version_v1" + "^" + version);
        }
        
        //2016-3-24 非实名认证不得获取推流地址
        ApplyActorService applyActorService = MelotBeanFactory.getBean("applyActorService",ApplyActorService.class);
        ApplyActor applyActor = applyActorService.getApplyActorByActorId(userId);
        if(applyActor == null || applyActor.getStatus() < 1){
             result.addProperty(TAG_CODE, TagCodeEnum.STATE_MISSING);
             result.addProperty(ERROR_MSG, "您尚未通过实名认证，请登录官方站点完成实名认证");
             return result;
        }
        
        LiveStreamConfigService liveStreamConfigService = (LiveStreamConfigService) MelotBeanFactory.getBean("liveStreamConfigService");
        ClientDetail detail = new ClientDetail(); 
        detail.setActorId(userId); 
        detail.setUserId(userId); 
        detail.setClientIp(com.melot.kktv.service.GeneralService.getIpAddr(request, appId, appId, null)); 
        detail.setCdnType(cdnType); 
        String resObj;
        if (type == 1) {
            resObj = liveStreamConfigService.getP2PPushStringAddress(detail);
        }else {
            resObj = liveStreamConfigService.getPushStreamAddress(detail);
        }
        result = new JsonParser().parse(resObj).getAsJsonObject();
        result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
        result.addProperty(ERROR_MSG, "成功");
        return result;
    }
    
    /**
     *  直播精灵获取推流cdn类型列表   [fucTag=60001005]
     */
    public JsonObject getCdnType(JsonObject jsonObject,boolean checkTag, HttpServletRequest request){
        JsonObject result = null;
        try {
            LiveStreamConfigService liveStreamConfigService = (LiveStreamConfigService) MelotBeanFactory.getBean("liveStreamConfigService");
            String resObj = liveStreamConfigService.getSupportedCdnTypes();
            result = new JsonParser().parse(resObj).getAsJsonObject();
            result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            result = new JsonObject();
            result.addProperty(TAG_CODE, TagCodeEnum.STATE_MISSING);
        }
        return result;
    }
    
    /**
     *  ios设备上传adfa码
     *  [fucTag=60002000]
     */
    public JsonObject upAdfa(JsonObject jsonObject,boolean checkTag, HttpServletRequest request){
        JsonObject result = new JsonObject();
        int appId = 0;
        try{
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", 0, TagCodeEnum.APPID_MISSING, 0, Integer.MAX_VALUE);
            CommonUtil.getJsonParamString(jsonObject, "up", null, TagCodeEnum.PHONE_NUMBER_MISSING, 0, Integer.MAX_VALUE);
        }catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(TAG_CODE, e.getErrCode());
            return result;
        } 

        if (appId > 0) {
            try {
                String xriIp = com.melot.kktv.service.GeneralService.getIpAddr(request, appId, appId, null);
                jsonObject.addProperty("ip", xriIp);
                String ua = request.getHeader("user-agent");

                UpIdfaService upIdfaService = (UpIdfaService) MelotBeanFactory.getBean("upIdfaService");
                Integer channelId = upIdfaService.upAdfa(jsonObject, xriIp, ua);
                if (channelId != null) {
                    result.addProperty("c", channelId);
                }
                result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
            } catch (Exception e) {
                result.addProperty(TAG_CODE, TagCodeEnum.STATE_MISSING);
                logger.error("ActorFunction.upAdfa() is error !!!", e);
            }
        }

        return result;
    }
    
    private JsonObject actorLiveInfo2Json(ActorLiveInfo defaultActorLiveInfo, JsonObject result) {
        result.addProperty("ratio", defaultActorLiveInfo.getRatio());
        result.addProperty("frameRate", defaultActorLiveInfo.getFrameRate());
        result.addProperty("bitRate", defaultActorLiveInfo.getBitRate());
        return result;
    }
    
    public static Date addDays(Date date, int amount) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DAY_OF_MONTH, amount);
        return c.getTime();
    }
    
}

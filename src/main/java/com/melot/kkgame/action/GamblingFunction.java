/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.action;

import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.gambling.dao.domain.GambleChoice;
import com.melot.gambling.dao.domain.GambleInfo;
import com.melot.gambling.dao.domain.GambleOption;
import com.melot.gambling.service.GambleInfoService;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kkcx.service.GeneralService;
import com.melot.kkgame.logger.HadoopLogger;
import com.melot.kkgame.model.GambleResult;
import com.melot.kkgame.redis.GamblingSource;
import com.melot.kkgame.redis.RoomMessageSource;
import com.melot.kkgame.redis.support.RedisException;
import com.melot.kkgame.service.UserGambleService;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.CollectionUtils;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.ConstantEnum;
import com.melot.kktv.util.DateUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.module.ModuleService;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * Title: GamblingFunction
 * <p>
 * Description: 用户竞猜功能
 * </p>
 * @author  姚国平<a href="mailto:guoping.yao@melot.cn">
 * @version V1.0
 * @since 2015-8-27 上午11:57:57 
 */
public class GamblingFunction extends BaseAction{

    private static Logger logger = Logger.getLogger(GamblingFunction.class);
    
    public static final Integer ACTOR_CAN_GAMBLE_LEVEL = 10;
    
    private GamblingSource gamblingSource;
    private UserGambleService userGambleService;
    private RoomMessageSource roomMessageSource;
    
	public void setUserGambleService(UserGambleService userGambleService) {
		this.userGambleService = userGambleService;
	}

    public void setGamblingSource(GamblingSource gamblingSource) {
        this.gamblingSource = gamblingSource;
    }
    
    public void setRoomMessageSource(RoomMessageSource roomMessageSource) {
        this.roomMessageSource = roomMessageSource;
    }
    
    /**
     * 查询全部竞猜列表(20050001)
     * @param jsonObject
     * @return
     */
    public JsonObject getGamblingList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int start; // 请求开始数  
        int offset; //一次请求返回个数 
        int appId;//appId
        try {
            start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, null, 1, Integer.MAX_VALUE);
            offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 1, null, 1, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(TAG_CODE, e.getErrCode());
            return result;
        }
        GambleInfoService gambleInfoService = (GambleInfoService)ModuleService.getService("GambleInfoService");
        List<GambleInfo> list = gambleInfoService.getGambleInfoList(start, offset, appId);
        if (!CollectionUtils.isEmpty(list)) {
            int count = gambleInfoService.getCountOfGambleInfoList(appId);
            result.addProperty("count", count);
        } else {
            logger.info("there is no valiable gamble in shis time");
            result.addProperty("count", 0);
        }
        JsonArray gambleList = new JsonArray();
        JsonParser parse = new JsonParser();
        try {
            for (GambleInfo gambleInfo : list) {
                JsonObject gambleInfoObj;
                String info = gamblingSource.getGambleInfo(gambleInfo.getGambleId());
                if (info == null) {
                   gambleInfo = gambleInfoService.getGambleInfoByGambleId(gambleInfo.getGambleId());
                   gambleInfoObj = transGambleInfoToJson(gambleInfo, null);
                   gamblingSource.setGambleInfo(gambleInfo.getGambleId(), gambleInfoObj.toString());
                } else {
                   gambleInfoObj = parse.parse(info).getAsJsonObject();
                }
                gambleList.add(gambleInfoObj);
            }
        } catch (RedisException e) {
            result.addProperty(TAG_CODE, TagCodeEnum.REDIS_ERROR);
            logger.error("RedisException error",e);
            return result;
        }
        result.add("gambleList", gambleList);
        result.addProperty("currentTime", new Date().getTime());
        result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 用户参与投注(20050002)
     * @param jsonObject
     * @return
     */
    public JsonObject userBetIn(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty(TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        Integer userId; 
        Integer gambleId;
        Integer optionId;
        Integer amount;
        Integer roomId;
        int appId;
        int platform = 0;
        try {
            appId = CommonUtil.getJsonParamInt(jsonObject, "a",  AppIdEnum.GAME, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            gambleId = CommonUtil.getJsonParamInt(jsonObject, "gambleId", 0, TagCodeEnum.GAMBLEID_MISS, 1, Integer.MAX_VALUE);
            optionId = CommonUtil.getJsonParamInt(jsonObject, "optionId", 0, TagCodeEnum.OPTIONID_MISS, 1, Integer.MAX_VALUE);
            amount = CommonUtil.getJsonParamInt(jsonObject, "amount", 0, TagCodeEnum.AMOUNT_MISS, 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 2, null, 1, Integer.MAX_VALUE); //默认platform =2 android
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, null, 1, Integer.MAX_VALUE); 
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(TAG_CODE, e.getErrCode());
            return result;
        }
        com.melot.kkcore.user.service.KkUserService userService = (com.melot.kkcore.user.service.KkUserService) MelotBeanFactory.getBean("kkUserService");
        long showMoney = userService.getUserAssets(userId).getShowMoney();
        if (showMoney < amount) { //投注金额不够用于下注
            result.addProperty(TAG_CODE,TagCodeEnum.USER_MONEY_SHORTNESS);
            return result;
        }
        GambleInfoService gambleInfoService = (GambleInfoService)ModuleService.getService("GambleInfoService");
        GambleChoice gambleChoice = new GambleChoice();
        gambleChoice.setBetAmount(amount);
        gambleChoice.setGambleId(gambleId);
        gambleChoice.setOptionId(optionId);
        gambleChoice.setUserId(userId);
        gambleChoice.setBetProfit(0);
        gambleChoice.setBetTime(new Date());
        int code = gambleInfoService.addGambleChoice(gambleChoice);
        if (code < 0) {//投注失败
            result.addProperty(TAG_CODE,String.valueOf(50000000 - code));
        } else {
            HadoopLogger.gambleLog(userId, roomId ,new Date(), amount, appId, platform);
            result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
            try {
                //清空redis用户投注最新投注汇总信息;
                gamblingSource.deleteGambleInfo(gambleId);
                gamblingSource.updateUserBet(gambleId, userId);
                //清空当月投注缓存
                gamblingSource.delGambleResult(userId, DateUtil.formatDate(new Date(), "yyyyMM"));
                
                //发送投注信息到房间公聊屏
                GambleOption option = gambleInfoService.getGambleOptionByOptionId(optionId);
                UserProfile userInfo = userService.getUserProfile(userId);
                roomMessageSource.sendMsgToUserInRoom(roomId, roomMessageSource.getGambleActionMsg(userInfo.getNickName(), option.getOptionTitle(), amount));           
            } catch (RedisException e) {
                result.addProperty(TAG_CODE, TagCodeEnum.REDIS_ERROR);
                logger.error("RedisException error",e);
                return result;
            }
        }
        
        return result;
    }
    
    /**
     * 查询属于直播间的竞猜列表(20050003) 区别与20050001接口,多了roomId过滤项
     * @param jsonObject
     * @return
     */
    public JsonObject getGamblingListInRoom(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int roomId, start, offset; 
        try {
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, TagCodeEnum.ROOMID_MISSING, 1, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, null, 1, Integer.MAX_VALUE);
            offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 1, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(TAG_CODE, e.getErrCode());
            return result;
        }
        
        List<GambleInfo> list = null;
        GambleInfoService gambleInfoService = (GambleInfoService)ModuleService.getService("GambleInfoService");
        list = gambleInfoService.getvaildGambleInfoListInRoom(roomId, start, offset);
        JsonArray gambleList = new JsonArray();
        JsonParser parse = new JsonParser();
        try {
        	JsonObject gambleInfoObj;
            for (GambleInfo gambleInfo : list) {
                String info = gamblingSource.getGambleInfo(gambleInfo.getGambleId());
                if (info == null) {
                   gambleInfo = gambleInfoService.getGambleInfoByGambleId(gambleInfo.getGambleId());
                   gambleInfoObj = transGambleInfoToJson(gambleInfo, null);
                   gamblingSource.setGambleInfo(gambleInfo.getGambleId(), gambleInfoObj.toString());
                } else {
                    gambleInfoObj = parse.parse(info).getAsJsonObject();
                }
                gambleList.add(gambleInfoObj);
            }
        } catch (RedisException e) {
            result.addProperty(TAG_CODE, TagCodeEnum.REDIS_ERROR);
            logger.error("RedisException error", e);
            return result;
        }
        result.add("gambleList", gambleList);
        result.addProperty("currentTime", new Date().getTime());
        result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 获取盘口最新信息(20050004)
     * @param jsonObject
     * @return
     */
    public JsonObject getGambleInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        Integer userId;
        Integer gambleId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0,  null, 1, Integer.MAX_VALUE);
            gambleId = CommonUtil.getJsonParamInt(jsonObject, "gambleId", 0, TagCodeEnum.GAMBLEID_MISS, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(TAG_CODE, e.getErrCode());
            return result;
        }
        if (!checkTag) { //验证失败,以游客对待
            userId = null;
        }
        JsonArray lastBets = new JsonArray();
        JsonParser parse = new JsonParser();
        GambleInfoService gambleInfoService = (GambleInfoService)ModuleService.getService("GambleInfoService");
        try {
            String info = gamblingSource.getGambleInfo(gambleId);
            if (info == null) {
               result = transGambleInfoToJson(gambleInfoService.getGambleInfoByGambleId(gambleId),null);
               gamblingSource.setGambleInfo(gambleId, result.toString());
            } else {
                result = parse.parse(info).getAsJsonObject();
            }
            if (userId != null) {
                result.addProperty("winAmount", gamblingSource.getUserWinAmountInOption(gambleId, userId)); 
                String userBetResult = gamblingSource.getUserBet(gambleId, userId);
                if (userBetResult != null) {
                    result.add("userBets", parse.parse(userBetResult).getAsJsonArray());
                }
            }      
            result.addProperty("currentTime", new Date().getTime());
            Set<String>history = gamblingSource.getBetHistoryOfGamble(gambleId, 2);
            for (String string : history) {
                lastBets.add(parse.parse(string));
            }
        } catch (RedisException e) {
            result.addProperty(TAG_CODE, TagCodeEnum.REDIS_ERROR);
            logger.error("RedisException error",e);
            return result;
        }
        result.add("lastBets", lastBets);
        result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
        return result;
    }
    
    
    /**
     * 获取用户押注信息(20050005)
     * @param jsonObject
     * @return
     */
    public JsonObject getUserGamble(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		JsonObject result = new JsonObject();
		if (!checkTag) {
			result.addProperty(TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		int limit, offset, userId;
		String month;
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
			limit = CommonUtil.getJsonParamInt(jsonObject, "limit", 0, null, 1, Integer.MAX_VALUE);
			offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 0, null, 1, Integer.MAX_VALUE);
			month = CommonUtil.getJsonParamString(jsonObject, "month", "",null, 0, Integer.MAX_VALUE);
		} catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty(TAG_CODE, e.getErrCode());
			return result;
		}
		
		return userGambleService.getGambleResultJson(userId, month, offset, limit);
    }
    
    /**
     * 获取用户押注统计信息(20050006)
     * @param jsonObject
     * @return
     */
    public JsonObject getGambleResult(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		JsonObject result = new JsonObject();
		if (!checkTag) {
			result.addProperty(TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		int userId;
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
		} catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty(TAG_CODE, e.getErrCode());
			return result;
		}
		
		GambleResult gambleResult= userGambleService.getUserGambleResult(userId);
		if (gambleResult != null) {
			JsonObject jsonObj = gambleResult.toJsonObject();
			result.add("gambleResult", jsonObj);
		}
		result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 主播发起竞猜，增加新盘口(functag = 20050007)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject addGambleByAnchor(JsonObject jsonObject, boolean checkTag, HttpServletRequest request){
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty(TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        int appId;//appId
        int userId= 0, roomId = 0,comsionRate = 0;
		int isNow = 0;
        String gambleTitle;
        String gambleStartTime , gambleEndTime;
        JsonArray optionTitles;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            gambleTitle = CommonUtil.getJsonParamString(jsonObject, "gambleTitle", "defalut_gamble", TagCodeEnum.GAMBLETITLE_MISS, 1, Integer.MAX_VALUE);
            gambleStartTime = CommonUtil.getJsonParamString(jsonObject, "gambleStartTime", "", TagCodeEnum.GAMBLESTARTTIME_MISSING, 0, Integer.MAX_VALUE);
            gambleEndTime = CommonUtil.getJsonParamString(jsonObject, "gambleEndTime", "" , TagCodeEnum.GAMBLEENDTIME_MISSING, 0, Integer.MAX_VALUE);
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, TagCodeEnum.ROOMID_MISSING, 1, Integer.MAX_VALUE);
            comsionRate = CommonUtil.getJsonParamInt(jsonObject, "comsionRate", 0, null, 0, 100);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
            isNow = CommonUtil.getJsonParamInt(jsonObject, "isNow", 0, TagCodeEnum.APPID_MISSING, 0, Integer.MAX_VALUE);
            optionTitles = jsonObject.getAsJsonArray("optionTitles");
            if (optionTitles == null || optionTitles.size() <= 0) {
                throw new CommonUtil.ErrorGetParameterException(TagCodeEnum.OPTIONTITLE_MISSING);
            }
        } catch (ClassCastException e) {
            result.addProperty(TAG_CODE, TagCodeEnum.CAN_NOT_CAST_OPTIONTITLES);
            return result;
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(TAG_CODE, e.getErrCode());
            return result;
        }
        try {
            if (!gamblingSource.isOnWhiteList(userId)) { //非白名单用户
                if (!gamblingSource.canGambling(userId)) { //黑名单用户不能创建竞猜
                    result.addProperty(TAG_CODE, TagCodeEnum.ACTOR_IS_IN_BLACK_LIST);
                    return result;
                } else {
                    KkUserService kkUserService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
                    UserProfile userProfile = kkUserService.getUserProfile(userId);
                    Integer actorLevel = userProfile.getActorLevel();//非黑名单用户, 检测用户等级是否满足
                    if (actorLevel <= ACTOR_CAN_GAMBLE_LEVEL) {
                        result.addProperty(TAG_CODE, TagCodeEnum.ACTOR_LEVEL_INAVAILABLE);
                        return result;
                    }
                }
            }
        } catch (RedisException e1) {
            logger.error("RedisException", e1);
        }
        gambleTitle = GeneralService.replaceSensitiveWords(userId, gambleTitle);
        if (gambleTitle.contains("*")) { //包含敏感词
            result.addProperty(TAG_CODE, TagCodeEnum.TITLE_INCLUDE_SENSITIVE_WORDS);
            return result;
        }
        for (int i = 0; i < optionTitles.size(); i++) {
            JsonObject element = optionTitles.get(i).getAsJsonObject();
            String optionTitle = element.get("optionTitle").getAsString();
            optionTitle = GeneralService.replaceSensitiveWords(userId, optionTitle);
            if (optionTitle.contains("*")) {//包含敏感词
                if (i == 0) {
                    result.addProperty(TAG_CODE, TagCodeEnum.OPTION1_INCLUDE_SENSITIVE_WORDS);
                } else {
                    result.addProperty(TAG_CODE, TagCodeEnum.OPTION2_INCLUDE_SENSITIVE_WORDS);
                }
                return result;
            }
        }
      
        GambleInfo gambleInfo = new GambleInfo();
        gambleInfo.setAppId(appId);
        gambleInfo.setGambleType(1);
        gambleInfo.setTitle(gambleTitle);
        try {
        	if (isNow == 1) {
        		gambleInfo.setGambleStartTime(new Date());
        	} else {
        		gambleInfo.setGambleStartTime(DateUtil.parseDateTimeStringToDate(gambleStartTime, "yyyy-MM-dd HH:mm:ss"));
        	}
            gambleInfo.setGambleEndTime(DateUtil.parseDateTimeStringToDate(gambleEndTime, "yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e1) {
            result.addProperty(TAG_CODE, TagCodeEnum.STRING_CAN_NOT_PARSE_DATE);
            return result;
        }
        gambleInfo.setRoomId(roomId);
        gambleInfo.setGambleOwner(userId);
        gambleInfo.setCommissionRate(comsionRate);
        try {
            GambleInfoService gambleInfoService = (GambleInfoService)ModuleService.getService("GambleInfoService");
            Integer gambleId = gambleInfoService.saveGambleInfo(gambleInfo);
            saveGambleOptionByParseJson(gambleInfoService ,gambleId, optionTitles);
            gambleInfoService.verifyGambleInfo(gambleId, 1);
        } catch (Exception e) {
            logger.error("Fail to call gamblingFunction.addGambleByAnchor ", e);
            result.addProperty(TAG_CODE, TagCodeEnum.EXECSQL_EXCEPTION);
            return result;
        }
        result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
        
        return result;
    }
    
    /**
     * 主播取消自己发起的竞猜(functag = 20050008)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject cancelGambleByAnchor(JsonObject jsonObject, boolean checkTag, HttpServletRequest request){
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty(TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        Integer gambleId = 0, userId = 0;
        String cancelReason;
        try {
            gambleId = CommonUtil.getJsonParamInt(jsonObject, "gambleId", 0, TagCodeEnum.GAMBLEID_MISS, 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            cancelReason = CommonUtil.getJsonParamString(jsonObject, "cancelReason", "", TagCodeEnum.CANCELREASON_MISSING, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(TAG_CODE, e.getErrCode());
            return result;
        }
        
        cancelReason = GeneralService.replaceSensitiveWords(userId, cancelReason);
        if (cancelReason.contains("*")) {
            result.addProperty(TAG_CODE, TagCodeEnum.CALCELREASON_INCLUDE_SENSITIVE_WORDS);
            return result;
        }
        GambleInfoService gambleInfoService = (GambleInfoService)ModuleService.getService("GambleInfoService");
        GambleInfo gambleInfo = gambleInfoService.getGambleInfoByGambleId(gambleId);
        if (gambleInfo == null || gambleInfo.getGambleId() == null) {
            result.addProperty(TAG_CODE, TagCodeEnum.IRREGULAR_RESULT);
            return result;
        }
        if (gambleInfo.getGambleOwner() == null || !gambleInfo.getGambleOwner().equals(userId)) {
            result.addProperty(TAG_CODE, TagCodeEnum.GAMBLE_IS_NOT_COMPLY_ANCHOR);
            return result;
        }
        try {
            boolean isSuccess = gambleInfoService.cancelGambelInTime(gambleId, cancelReason);
            if (!isSuccess) {
                result.addProperty(TAG_CODE, TagCodeEnum.EXECSQL_EXCEPTION);
            } else {
                result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
            }
        } catch (Exception e) {
            logger.error("Fail to call gamblingFunction.deleteGambleByAnchor ", e);
            result.addProperty(TAG_CODE, TagCodeEnum.EXECSQL_EXCEPTION);
            return result;
        }
        return result;
    }
    
    /**
     * 主播清算自己盘口(functag = 20050009)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject clearGambleByAnchor(JsonObject jsonObject, boolean checkTag, HttpServletRequest request){
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty(TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        Integer gambleId = 0, userId = 0, optionId = 0;
        try {
            gambleId = CommonUtil.getJsonParamInt(jsonObject, "gambleId", 0, TagCodeEnum.GAMBLEID_MISS, 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            optionId = CommonUtil.getJsonParamInt(jsonObject, "optionId", 0, TagCodeEnum.OPTIONID_MISS, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(TAG_CODE, e.getErrCode());
            return result;
        }
        GambleInfoService gambleInfoService = (GambleInfoService)ModuleService.getService("GambleInfoService");
        GambleInfo gambleInfo = gambleInfoService.getGambleInfoByGambleId(gambleId);
        if (gambleInfo == null || gambleInfo.getGambleId() == null) {
            result.addProperty(TAG_CODE, TagCodeEnum.IRREGULAR_RESULT);
            return result;
        }
        if (gambleInfo.getGambleOwner() == null || !gambleInfo.getGambleOwner().equals(userId)) {
            result.addProperty(TAG_CODE, TagCodeEnum.GAMBLE_IS_NOT_COMPLY_ANCHOR);
            return result;
        }
        if (gambleInfo.getGambleState() != 2) {
            result.addProperty(TAG_CODE, TagCodeEnum.CAN_NOT_CLEAR_GAMBLE);
            return result;
        }
        if (gambleInfo.getGambleEndTime().after(new Date())) {
            result.addProperty(TAG_CODE, TagCodeEnum.ENDTIME_IS_NOT_OUTTIME);
            return result;
        }
        
        try {
            boolean isSuccess = gambleInfoService.finishGambling(gambleId, optionId);
            if (!isSuccess) {
                result.addProperty(TAG_CODE, TagCodeEnum.EXECSQL_EXCEPTION);
            } else {
                result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
            }
        } catch (Exception e) {
            logger.error("Fail to call gamblingFunction.clearGambleByAnchor ", e);
            result.addProperty(TAG_CODE, TagCodeEnum.EXECSQL_EXCEPTION);
            return result;
        }
        
        return result;
    }
    
    
    /**
     * 首页查询竞猜列表(20050010)  
     * 区别于20050001, 需要多返回房间头像
     * @param jsonObject
     * @return
     */
    public JsonObject getGamblingListForPortal(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int start; // 请求开始数  
        int offset; //一次请求返回个数 
        int appId;//appId
        try {
            start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, null, 1, Integer.MAX_VALUE);
            offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 1, null, 1, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(TAG_CODE, e.getErrCode());
            return result;
        }
        
        GambleInfoService gambleInfoService = (GambleInfoService)ModuleService.getService("GambleInfoService");
        List<GambleInfo> list = gambleInfoService.getGambleInfoList(start, offset, appId);
        if (!CollectionUtils.isEmpty(list)) {
            int count = gambleInfoService.getCountOfGambleInfoList(appId);
            result.addProperty("count", count);
        } else {
            logger.info("there is no available gamble at shis time");
            result.addProperty("count", 0);
        }
        JsonArray gambleList = new JsonArray();
        JsonParser parse = new JsonParser();
        try {
            com.melot.kkcore.user.service.KkUserService userService = (com.melot.kkcore.user.service.KkUserService) MelotBeanFactory.getBean("kkUserService");
            JsonObject gambleInfoObj = null;
            for (GambleInfo gambleInfo : list) {
                String info = gamblingSource.getGambleInfo(gambleInfo.getGambleId());
                if (info == null) {
                    gambleInfo = gambleInfoService.getGambleInfoByGambleId(gambleInfo.getGambleId());
                    gambleInfoObj = transGambleInfoToJson(gambleInfo,null);
                    gamblingSource.setGambleInfo(gambleInfo.getGambleId(), gambleInfoObj.toString());
                } else {
                    gambleInfoObj = parse.parse(info).getAsJsonObject();
                }
                if (userService != null) { //新增返回房间头像
                    UserProfile userInfo = userService.getUserProfile(gambleInfo.getRoomId());
                    if (userInfo!= null && userInfo.getPortrait() != null) {
                        gambleInfoObj.addProperty("portrait", ConfigHelper.getHttpdir() + userInfo.getPortrait());
                    } else {
                        gambleInfoObj.addProperty("portrait", ConfigHelper.getHttpdir() + ConstantEnum.DEFAULT_PORTRAIT_USER);
                    }
                }
                gambleList.add(gambleInfoObj);
            }
        } catch (RedisException e) {
            result.addProperty(TAG_CODE, TagCodeEnum.REDIS_ERROR);
            logger.error("RedisException error",e);
            return result;
        }
        result.add("gambleList", gambleList);
        
        result.addProperty("currentTime", new Date().getTime());
        result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 将一个盘口信息转化为json数据格式
     * @param gambleInfo
     * @return
     * @throws RedisException 
     * @throws RedisException 
     */
    private JsonObject transGambleInfoToJson(GambleInfo gambleInfo, JsonObject result) throws RedisException   {
    	GambleInfoService gambleInfoService =  (GambleInfoService)ModuleService.getService("GambleInfoService");
        List<GambleOption> options =gambleInfoService.getGambleOptionListByGambleId(gambleInfo.getGambleId());
        JsonObject gambleInfoObj;
        if(result == null){
            gambleInfoObj = new JsonObject();
        }else{
            gambleInfoObj = result;
        }
        gambleInfoObj.addProperty("gambleId", gambleInfo.getGambleId());
        gambleInfoObj.addProperty("title", gambleInfo.getTitle());
        gambleInfoObj.addProperty("description", gambleInfo.getGambleDesc());
        gambleInfoObj.addProperty("startTime", gambleInfo.getGambleStartTime().getTime());
        gambleInfoObj.addProperty("endTime", gambleInfo.getGambleEndTime().getTime());
        gambleInfoObj.addProperty("type", gambleInfo.getGambleType());
        gambleInfoObj.addProperty("state", gambleInfo.getGambleState());
        gambleInfoObj.addProperty("cancelReason", gambleInfo.getCancelReason() == null ? "" : gambleInfo.getCancelReason());
        gambleInfoObj.addProperty("minBetAmount", gambleInfo.getMinBetAmount());
        gambleInfoObj.addProperty("maxBetAmount", gambleInfo.getMaxBetAmount());
        gambleInfoObj.addProperty("balanceAmount", gambleInfo.getBalanceAmount()); 
        gambleInfoObj.addProperty("roomId", gambleInfo.getRoomId());
        gambleInfoObj.addProperty("owner", gambleInfo.getGambleOwner());
        if(gambleInfo.getAppId()!=null){
        	gambleInfoObj.addProperty("appId", gambleInfo.getAppId());
        }
        if(gambleInfo.getGambleState() > 1 ){
            gambleInfoObj.addProperty("winOptionId", gambleInfo.getWinOptionId());
        }
        if(gambleInfo.getLotteryTime() != null){ //开奖时间
            gambleInfoObj.addProperty("lotteryTime", gambleInfo.getLotteryTime().getTime()); 
        }
        JsonArray optionsList = new JsonArray();
        for (GambleOption gambleOption : options) {
            JsonObject optionObj = new JsonObject();
            optionObj.addProperty("optionId", gambleOption.getOptionId());
            optionObj.addProperty("optionTitle", gambleOption.getOptionTitle());
            optionObj.addProperty("optionPicPath", gambleOption.getOptionPicPath());
            optionObj.addProperty("betRate", gambleOption.getBetRate());
            optionObj.addProperty("minBetRate", gambleOption.getMinBetRate());
            optionObj.addProperty("maxBetRate", gambleOption.getMaxBetRate());
            optionObj.addProperty("profitAmount", gambleOption.getProfitAmount());
            optionsList.add(optionObj);
        }
        gambleInfoObj.add("optionList", optionsList);
        
        return gambleInfoObj;
    }
    
	private void saveGambleOptionByParseJson(GambleInfoService gambleInfoService, Integer gambleId, JsonArray optionTitles) {
		for (int i = 0; i < optionTitles.size(); i++) {
			JsonObject element = optionTitles.get(i).getAsJsonObject();
			GambleOption gambleOption = new GambleOption();
			gambleOption.setOptionTitle(element.get("optionTitle").getAsString());
			gambleOption.setGambleId(gambleId);
			gambleOption.setCreateTime(new Date());
			Integer optionId = gambleInfoService.addGambleOption(gambleOption);
			if (optionId == null || optionId <= 0) {
				logger.error("保存竞猜选项失败，请查询原因并修改，赌局盘口gambleId=" + gambleId + ", option=" + element.get("optionTitile").getAsString());
			}
		}
	}
}

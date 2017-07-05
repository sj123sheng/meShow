/**
 * This document and its contents are protected by copyright 2012 and owned by
 * Melot Inc.
 * The copying and reproduction of this document and/or its content (whether
 * wholly or partly) or any
 * incorporation of the same into any other material in any media or format of
 * any kind is strictly prohibited.
 * All rights are reserved.
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.action;

import java.util.Date;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.httpclient.util.DateUtil;
import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.game.config.sdk.domain.LotteryTicket;
import com.melot.game.config.sdk.lottery.service.LotteryTicketService;
import com.melot.game.config.sdk.utils.StringUtils;
import com.melot.kkcore.user.api.UserStaticInfo;
import com.melot.kkgame.redis.LotteryTaskSource;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.lottery.service.LotteryTicketModuleService;
import com.melot.module.ModuleService;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * Title: LotteryAction
 * <p>
 * Description: 彩票相关接口类
 * </p>
 * 
 * @author 王康鹏<a href="mailto:kangpeng.wang@melot.cn">
 * @version V1.0
 * @since 2015年5月8日 上午11:44:00
 */
public class LotteryFunction extends BaseAction {
    
    private static Logger logger = Logger.getLogger(LotteryFunction.class);
    private LotteryTaskSource lotteryTaskSource;
    
    public void setLotteryTaskSource(LotteryTaskSource lotteryTaskSource) {
        this.lotteryTaskSource = lotteryTaskSource;
    }    
    
    /**
     * 获取可选彩票列表 [fucTag="80005036"]
     * @param jsonObject
     * userId
     * @return
     */
    public JsonObject getAvailableLotteryTicketList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        try {
            Integer userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "05350001", 1, Integer.MAX_VALUE);
            LotteryTicketModuleService lotteryTicketModuleService = (LotteryTicketModuleService) ModuleService.getService("LotteryTicketModuleService");
            String lotteryId = lotteryTaskSource.hasBetIn(userId);
            if (null != lotteryId) {
                result.addProperty("hasLotteryId", lotteryId);
            } else {
                result.addProperty("hasLotteryId", 0);
            }
            Set<String> tickets = lotteryTicketModuleService.getAvailableConfLotteryTickets();

            JsonParser jsonParser = new JsonParser();
            JsonArray jsonArray = new JsonArray();
            for (String string : tickets) {
                JsonElement element = jsonParser.parse(string);
                jsonArray.add(element.getAsJsonObject());
            }
            result.addProperty("domain", "http://ures.kktv8.com/kktv/facepack/");
            result.add("lotteryTicketList", jsonArray);
            result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
		} catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            logger.error("Fail to getAvailableLotteryTicketList ", e);
            result.addProperty(TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

    }

    /**
     * 获取今日和历史获奖情况 
     * [fucTag=80005037]
     * @param jsonObject
     * @return
     * 
     */
    public JsonObject getLotteryPoolList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty(TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        Integer userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "05350001", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
			LotteryTicketModuleService lotteryTicketModuleService = (LotteryTicketModuleService) ModuleService.getService("LotteryTicketModuleService");
			result = lotteryTicketModuleService.queryLotteryInfoByUserId(userId);
		} catch (Exception e) {
			logger.error("Fail to getLotteryPoolList ", e);
		}
        
        result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 投注
     * [fucTag=80005038]
     * @return
     */
    public JsonObject addNewBetting(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty(TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        Integer userId, lotteryId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "05350001", 1, Integer.MAX_VALUE);
            lotteryId = CommonUtil.getJsonParamInt(jsonObject, "lotteryId", 0, "05350002", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        
        LotteryTicketService lotteryTicketService = MelotBeanFactory.getBean("lotteryTicketService",LotteryTicketService.class); 
        LotteryTicketModuleService lotteryTicketModuleService = (LotteryTicketModuleService) ModuleService.getService("LotteryTicketModuleService");
        //检查redis，用户是否已经投注
        if (lotteryTicketModuleService.hasBetInToDay(userId)) {//已投注
            result.addProperty(TAG_CODE, TagCodeEnum.HAS_BEEN_ASSISTANT);
            return result;
        }        
        //未绑定手机且登录方式不是第三方不予领奖
        com.melot.kkcore.user.service.KkUserService service = (com.melot.kkcore.user.service.KkUserService) MelotBeanFactory.getBean("kkUserService");
        UserStaticInfo userInfo = service.getUserStaticInfo(userId);
        if (!canLottery(userInfo)) {
            result.addProperty(TAG_CODE, TagCodeEnum.NOT_USER_PHONE_NUMBER);
            logger.error("userId["+userId+"] don't reach the condition to get a lottery ticket");
            return result;
        }
        //保存投注的奖券
        LotteryTicket lotteryTicket = new LotteryTicket();
        lotteryTicket.setStatus(0);
        lotteryTicket.setUserId(userId);
        lotteryTicket.setLotteryDay(new Date());
        lotteryTicket.setLotteryId(lotteryId);
        lotteryTicketService.saveLotteryTicket(lotteryTicket); //保存
        try {
			lotteryTicketModuleService.userBetTodayAndDelCache(userId, lotteryId, DateUtil.formatDate(new Date(),"yyyyMM"));
		} catch (Exception e) {
            logger.error("Fail to addNewBetting ", e);
		}
        
        result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     *  检查用户能否达到彩票投注的条件 
     *  用户消费等级大于等于1级, 第三方注册用户或者手机绑定用户可领取彩票
     */
    private boolean canLottery(UserStaticInfo userInfo) {
        return userInfo.getProfile().getUserLevel() > 0 || !StringUtils.isEmpty(userInfo.getProfile().getPhoneNum()) || userInfo.getRegisterInfo().getOpenPlatform() > 0;
    }
    
    /**
     *  用户查询月中彩记录 
     *  [fucTag=10005039]
     */
    public JsonObject queryLotteryHistory(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty(TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        Integer userId;
        String queryMonth;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            queryMonth = CommonUtil.getJsonParamString(jsonObject, "month", null, TagCodeEnum.GAME_QUERY_MONTH_MISSING, 0, 10);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        JsonArray jsonArray;
        LotteryTicketModuleService lotteryTicketModuleService = (LotteryTicketModuleService) ModuleService.getService("LotteryTicketModuleService");
		try {
			jsonArray = lotteryTicketModuleService.queryLotteryHistoryByMonth(userId, queryMonth);
		} catch (Exception e) {
			logger.error("Fail to queryLotteryHistory ", e);
            result.addProperty(TAG_CODE, TagCodeEnum.REDIS_ERROR);
            return result;
		}
		
        result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
        result.add("lotteryHistory", jsonArray);
        return result;
    }
    
    /**
     *  用户查询总中彩记录 
     *  [fucTag=10005040]
     */
    public JsonObject getLotteryTimes(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject(); 
        if (!checkTag) {
            result.addProperty(TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        Integer userId = null;
        LotteryTicketModuleService lotteryTicketModuleService = (LotteryTicketModuleService) ModuleService.getService("LotteryTicketModuleService");
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
        }
        
        result = lotteryTicketModuleService.getAllLotteryTimesRecord(userId);
        result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
        return result;
    }

}

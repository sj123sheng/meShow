package com.melot.kkcx.functions;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.kk.bounty.api.domain.DailyRedPacket;
import com.melot.kk.bounty.api.domain.NonDailyRedPacket;
import com.melot.kk.bounty.api.domain.UserBounty;
import com.melot.kk.bounty.api.domain.UserBountyHist;
import com.melot.kk.bounty.api.domain.base.BountyResultCode;
import com.melot.kk.bounty.api.domain.base.Page;
import com.melot.kk.bounty.api.domain.base.Result;
import com.melot.kk.bounty.api.service.BountyService;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kktv.util.*;
import com.melot.sdk.core.util.MelotBeanFactory;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Title: BountyFunctions
 * <p>
 * Description: KK 奖励金 Functions
 * </p>
 * 
 * @author 宋建明<a href="mailto:jianming.song@melot.cn">
 * @version V1.0
 * @since 2017年9月16日 上午8:59:17
 */
public class BountyFunctions {

    private static final Logger LOGGER = Logger.getLogger(BountyFunctions.class);

    /**
     * 获取用户可领取红包个数接口（52050201）
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject getRedPacketCount(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        // 安全校验
//        JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
//        if(rtJO != null) {
//            return rtJO;
//        }
        
        JsonObject result = new JsonObject();
        
        // Token 校验
//        if (!checkTag) {
//            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
//            return result;
//        }

        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            BountyService bountyService = (BountyService) MelotBeanFactory.getBean("bountyService");
            Result<Integer> countResult = bountyService.getRedPacketCount(userId);
            if (countResult == null) {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            
            if (countResult.getCode().equals(BountyResultCode.SUCCESS)) {
                result.addProperty("count", countResult.getData());
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            }else {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Module error bountyService.getRedPacketCount(%s)", userId), e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }

    /**
     * 获取用户非日常红包列表接口（52050202）
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject getNonDailyRedPacketList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        // 安全校验
//        JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
//        if(rtJO != null) {
//            return rtJO;
//        }
        
        JsonObject result = new JsonObject();
        
        // Token 校验
//        if (!checkTag) {
//            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
//            return result;
//        }

        int userId;
        int num;
        int maxRedPacketId;
        
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            num = CommonUtil.getJsonParamInt(jsonObject, "num", 20, null, 1, 30);
            maxRedPacketId = CommonUtil.getJsonParamInt(jsonObject, "maxRedPacketId", 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            BountyService bountyService = (BountyService) MelotBeanFactory.getBean("bountyService");
            Result<Page<NonDailyRedPacket>> pageResult = bountyService.getNonDailyRedPackets(userId, maxRedPacketId, num);
            if (pageResult == null) {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            
            if (BountyResultCode.ERROR_SQL.equals(pageResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
                return result;
            }
            
            if (pageResult.getCode().equals(BountyResultCode.SUCCESS)) {
                Page<NonDailyRedPacket> page = pageResult.getData();
                result.addProperty("count", page.getCount());
                result.addProperty("maxRedPacket", (Long) page.getCommonInfo().get("maxRedPacket"));
                JsonArray redPackets = new JsonArray();
                for (NonDailyRedPacket packet : page.getList()) {
                    JsonObject packetJson = new JsonObject();
                    packetJson.addProperty("redPacketId", packet.getRedPacketId());
                    packetJson.addProperty("type", packet.getRedPacketType());
                    packetJson.addProperty("amount", packet.getRedPacketAmount());
                    packetJson.addProperty("userId", packet.getFriendUserId());
                    
                    // 获取朋友用户的昵称
                    try {
                        KkUserService kkUserService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
                        UserProfile friend = kkUserService.getUserProfile(packet.getFriendUserId());
                        packetJson.addProperty("nickname", (friend == null ? "" : friend.getNickName()));
                    } catch (Exception e) {
                        LOGGER.error(String.format("Module error kkUserService.getUserProfile(%s)", packet.getFriendUserId()), e);
                        packetJson.addProperty("nickname", "");
                    }
                    
                    redPackets.add(packetJson);
                }
                result.add("redPackets", redPackets);
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            }else {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Module error bountyService.getNonDailyRedPackets(%s, %s, %s)", userId, maxRedPacketId, num), e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }

    /**
     * 获取用户日常红包列表接口（52050203）
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject getDailyRedPacketList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        // 安全校验
//        JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
//        if(rtJO != null) {
//            return rtJO;
//        }
        
        JsonObject result = new JsonObject();
        
        // Token 校验
//        if (!checkTag) {
//            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
//            return result;
//        }

        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            BountyService bountyService = (BountyService) MelotBeanFactory.getBean("bountyService");
            Result<Page<DailyRedPacket>> pageResult = bountyService.getDailyRedPackets(userId);
            if (pageResult == null) {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            
            if (BountyResultCode.ERROR_SQL.equals(pageResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
                return result;
            }
            
            if (pageResult.getCode().equals(BountyResultCode.SUCCESS)) {
                Page<DailyRedPacket> page = pageResult.getData();
                JsonArray redPackets = new JsonArray();
                result.addProperty("richLevel", (Integer)page.getCommonInfo().get("richLevel"));
                result.addProperty("newUserCount", (Integer)page.getCommonInfo().get("newUserCount"));
                result.addProperty("redPacketDate", (String)page.getCommonInfo().get("redPacketDate"));
                
                for (DailyRedPacket packet : page.getList()) {
                    JsonObject packetJson = new JsonObject();
                    packetJson.addProperty("redPacketLevel", packet.getMinRichLevel());
                    packetJson.addProperty("redPacketName", packet.getRedPacketName());
                    packetJson.addProperty("maxAmount", packet.getMaxAmount());
                    packetJson.addProperty("minRichLevel", packet.getMinRichLevel());
                    packetJson.addProperty("minNewUserCount", packet.getMinNewUserCount());
                    packetJson.addProperty("state", packet.getState());
                    packetJson.addProperty("remainingTime", packet.getRemainingTime());
                    
                    redPackets.add(packetJson);
                }
                result.add("redPackets", redPackets);
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            }else {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Module error bountyService.getDailyRedPackets(%s)", userId), e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }

    /**
     * 开启非日常红包接口（52050204）
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject openNonDailyRedPacket(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        // 安全校验
//        JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
//        if(rtJO != null) {
//            return rtJO;
//        }
        
        JsonObject result = new JsonObject();
        
        // Token 校验
//        if (!checkTag) {
//            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
//            return result;
//        }

        int userId;
        int redPacketId;
        
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            redPacketId = CommonUtil.getJsonParamInt(jsonObject, "redPacketId", 0, "5205020401", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            BountyService bountyService = (BountyService) MelotBeanFactory.getBean("bountyService");
            Result<Boolean> pageResult = bountyService.openNonDailyRedPacket(userId, redPacketId);
            if (pageResult == null) {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            
            if (BountyResultCode.ERROR_SQL.equals(pageResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
                return result;
            }
            
            if (pageResult.getCode().equals(BountyResultCode.SUCCESS)) {
                boolean isSuccess = pageResult.getData();
                if (isSuccess) {
                    result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                    return result;
                }else {
                    result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                    return result;
                }
            }else {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Module error bountyService.getRedPacketCount(%s)", userId), e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }

    /**
     * 开启日常红包接口（52050205）
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject openDailyRedPacket(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        // 安全校验
//        JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
//        if(rtJO != null) {
//            return rtJO;
//        }
        
        JsonObject result = new JsonObject();
        
        // Token 校验
//        if (!checkTag) {
//            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
//            return result;
//        }

        int userId;
        int redPacketLevel;
        
        String redPacketDate;
        
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            redPacketLevel = CommonUtil.getJsonParamInt(jsonObject, "redPacketLevel", 0, "5205020501", 1, Integer.MAX_VALUE);
            redPacketDate = CommonUtil.getJsonParamString(jsonObject, "redPacketDate", DateUtil.formatDate(new Date(), "yyyy-MM-dd"), null, 10, 10);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            BountyService bountyService = (BountyService) MelotBeanFactory.getBean("bountyService");
            Result<DailyRedPacket> pageResult = bountyService.openDailyRedPacket(userId, redPacketLevel, DateUtil.parseDateStringToDate(redPacketDate, null));
            if (pageResult == null) {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            
            if (BountyResultCode.ERROR_SQL.equals(pageResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
                return result;
            }
            
            // 过期
            if (BountyResultCode.ERROR_DAILY_EXPIRE.equals(pageResult.getCode())) {
                result.addProperty("TagCode", "5205020502");
                return result;
            }
            
            // 达到上限20元
            if (BountyResultCode.ERROR_DAILY_UPPER_LIMIT.equals(pageResult.getCode())) {
                result.addProperty("TagCode", "5205020503");
                return result;
            }
            
            // 没有该等级红包
            if (BountyResultCode.ERROR_DAILY_NO_LEVEL.equals(pageResult.getCode())) {
                result.addProperty("TagCode", "5205020504");
                return result;
            }
            
            // 正在倒计时，不允许开启红包
            if (BountyResultCode.ERROR_DAILY_TIMING.equals(pageResult.getCode())) {
                result.addProperty("TagCode", "5205020505");
                return result;
            }
            
            if (pageResult.getCode().equals(BountyResultCode.SUCCESS)) {
                DailyRedPacket packet = pageResult.getData();
                result.addProperty("amount", packet.getAmount());
                if (packet.getRemainingTime() != null) {
                    result.addProperty("remainingTime", packet.getRemainingTime());
                }
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            }else {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Module error bountyService.openDailyRedPacket(%s, %s, %s)", userId, redPacketLevel, redPacketDate), e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }

    /**
     * 获取奖励金信息接口（52050206）
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject getBountyInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        // 安全校验
//        JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
//        if(rtJO != null) {
//            return rtJO;
//        }
        
        JsonObject result = new JsonObject();
        
        // Token 校验
//        if (!checkTag) {
//            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
//            return result;
//        }

        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            BountyService bountyService = (BountyService) MelotBeanFactory.getBean("bountyService");
            Result<UserBounty> pageResult = bountyService.getUserBounty(userId);
            if (pageResult == null) {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            
            if (BountyResultCode.ERROR_SQL.equals(pageResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
                return result;
            }
            
            if (pageResult.getCode().equals(BountyResultCode.SUCCESS)) {
                UserBounty packet = pageResult.getData();
                result.addProperty("bountyAmount", packet.getAmount());
                result.addProperty("totalBountyAmount", packet.getTotalAmount());
                result.addProperty("newUserCount", packet.getNewUserCount());
                result.addProperty("cashUserCount", packet.getRechargeCount());
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            }else {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Module error bountyService.getUserBounty(%s)", userId), e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }

    /**
     * 获取奖励金流水接口（51050207）
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject getBountyHistList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        
        JsonObject result = new JsonObject();
        
        // Token 校验
//        if (!checkTag) {
//            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
//            return result;
//        }

        int userId;
        int start;
        int offset;
        
        String dataMonth;
        
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            dataMonth = CommonUtil.getJsonParamString(jsonObject, "dataMonth", DateUtil.formatDate(new Date(), "yyyy-MM"), null, 7, 7);
            start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, null, 0, Integer.MAX_VALUE);
            offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 20, null, 1, 30);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            BountyService bountyService = (BountyService) MelotBeanFactory.getBean("bountyService");
            Result<Page<UserBountyHist>> pageResult = bountyService.getUserBountyHists(userId, dataMonth, start, offset);
            if (pageResult == null) {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            
            if (BountyResultCode.ERROR_SQL.equals(pageResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
                return result;
            }
            
            if (pageResult.getCode().equals(BountyResultCode.SUCCESS)) {
                Page<UserBountyHist> packet = pageResult.getData();
                result.addProperty("count", packet.getCount());
                JsonArray bountyHists = new JsonArray();
                
                for (UserBountyHist userBountyHist : packet.getList()) {
                    JsonObject histJson = new JsonObject();
                    histJson.addProperty("amount", userBountyHist.getAmount());
                    histJson.addProperty("type", userBountyHist.getBountyType());
                    histJson.addProperty("addTime", DateUtil.formatDateTime(userBountyHist.getOpenTime(), null));
                    
                    bountyHists.add(histJson);
                }
                
                result.add("bountyHists", bountyHists);
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            }else {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Module error bountyService.getUserBounty(%s)", userId), e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }

}

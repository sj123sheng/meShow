package com.melot.kkcx.functions;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.kk.bounty.api.domain.BountyQuestion;
import com.melot.kk.bounty.api.domain.DailyRedPacket;
import com.melot.kk.bounty.api.domain.NonDailyRedPacket;
import com.melot.kk.bounty.api.domain.ShareRedPacket;
import com.melot.kk.bounty.api.domain.UserBounty;
import com.melot.kk.bounty.api.domain.UserBountyHist;
import com.melot.kk.bounty.api.domain.base.BountyResultCode;
import com.melot.kk.bounty.api.domain.base.Page;
import com.melot.kk.bounty.api.domain.base.Result;
import com.melot.kk.bounty.api.service.BountyQuestionService;
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
        JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
        if(rtJO != null) {
            return rtJO;
        }
        
        JsonObject result = new JsonObject();
        
        // Token 校验
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            BountyService bountyService = (BountyService) MelotBeanFactory.getBean("bountyService");
            Result<Integer> countResult = bountyService.getRedPacketCount(userId);
            if (countResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            
            if (countResult.getCode().equals(BountyResultCode.SUCCESS)) {
                result.addProperty("count", countResult.getData());
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
                return result;
            }else {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Module error bountyService.getRedPacketCount(%s)", userId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
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
        JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
        if(rtJO != null) {
            return rtJO;
        }
        
        JsonObject result = new JsonObject();
        
        // Token 校验
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        int num;
        int maxRedPacketId;
        
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            num = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.NUM, 20, null, 1, 30);
            maxRedPacketId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.MAX_RED_PACKET_ID, 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            BountyService bountyService = (BountyService) MelotBeanFactory.getBean("bountyService");
            Result<Page<NonDailyRedPacket>> pageResult = bountyService.getNonDailyRedPackets(userId, maxRedPacketId, num);
            if (pageResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            
            if (BountyResultCode.ERROR_SQL.equals(pageResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.EXECSQL_EXCEPTION);
                return result;
            }
            
            if (pageResult.getCode().equals(BountyResultCode.SUCCESS)) {
                Page<NonDailyRedPacket> page = pageResult.getData();
                result.addProperty(ParameterKeys.COUNT, page.getCount());
                result.addProperty("maxRedPacket", (Long) page.getCommonInfo().get("maxRedPacket"));
                JsonArray redPackets = new JsonArray();
                if (page.getList() != null) {
                    for (NonDailyRedPacket packet : page.getList()) {
                        JsonObject packetJson = new JsonObject();
                        packetJson.addProperty(ParameterKeys.RED_PACKET_ID, packet.getRedPacketId());
                        packetJson.addProperty(ParameterKeys.TYPE, packet.getRedPacketType());
                        packetJson.addProperty(ParameterKeys.AMOUNT, packet.getRedPacketAmount());
                        packetJson.addProperty(ParameterKeys.USER_ID, packet.getFriendUserId());
                        packetJson.addProperty("desc", packet.getDesc());
                        
                        // 获取朋友用户的昵称
                        try {
                            KkUserService kkUserService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
                            UserProfile friend = kkUserService.getUserProfile(packet.getFriendUserId());
                            packetJson.addProperty(ParameterKeys.NICKNAME, (friend == null ? "" : friend.getNickName()));
                        } catch (Exception e) {
                            LOGGER.error(String.format("Module error kkUserService.getUserProfile(%s)", packet.getFriendUserId()), e);
                            packetJson.addProperty(ParameterKeys.NICKNAME, "");
                        }
                        
                        redPackets.add(packetJson);
                    }
                }
                result.add("redPackets", redPackets);
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
                return result;
            }else {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Module error bountyService.getNonDailyRedPackets(%s, %s, %s)", userId, maxRedPacketId, num), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
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
        JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
        if(rtJO != null) {
            return rtJO;
        }
        
        JsonObject result = new JsonObject();
        
        // Token 校验
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            BountyService bountyService = (BountyService) MelotBeanFactory.getBean("bountyService");
            Result<Page<DailyRedPacket>> pageResult = bountyService.getDailyRedPackets(userId);
            if (pageResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            
            if (BountyResultCode.ERROR_SQL.equals(pageResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.EXECSQL_EXCEPTION);
                return result;
            }
            
            if (pageResult.getCode().equals(BountyResultCode.SUCCESS)) {
                Page<DailyRedPacket> page = pageResult.getData();
                JsonArray redPackets = new JsonArray();
                result.addProperty("richLevel", (Integer)page.getCommonInfo().get("richLevel"));
                result.addProperty("newUserCount", (Integer)page.getCommonInfo().get("newUserCount"));
                result.addProperty("redPacketDate", (String)page.getCommonInfo().get("redPacketDate"));
                
                if (page.getList() != null) {
                    for (DailyRedPacket packet : page.getList()) {
                        JsonObject packetJson = new JsonObject();
                        packetJson.addProperty("redPacketLevel", packet.getRedPacketLevel());
                        packetJson.addProperty("redPacketName", packet.getRedPacketName());
                        packetJson.addProperty("maxAmount", packet.getMaxAmount());
                        packetJson.addProperty("minRichLevel", packet.getMinRichLevel());
                        packetJson.addProperty("minNewUserCount", packet.getMinNewUserCount());
                        packetJson.addProperty("state", packet.getState());
                        packetJson.addProperty("remainingTime", packet.getRemainingTime());
                        
                        redPackets.add(packetJson);
                    }
                }
                result.add("redPackets", redPackets);
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
                return result;
            }else {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Module error bountyService.getDailyRedPackets(%s)", userId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
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
        JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
        if(rtJO != null) {
            return rtJO;
        }
        
        JsonObject result = new JsonObject();
        
        // Token 校验
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        int redPacketId;
        
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            redPacketId = CommonUtil.getJsonParamInt(jsonObject, "redPacketId", 0, "5205020401", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            BountyService bountyService = (BountyService) MelotBeanFactory.getBean("bountyService");

            Result<Boolean> successResult = bountyService.openNonDailyRedPacket(userId, redPacketId);
            if (successResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            
            if (BountyResultCode.ERROR_SQL.equals(successResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.EXECSQL_EXCEPTION);
                return result;
            }
            
            if (BountyResultCode.ERROR_NON_DAILY_INVALID.equals(successResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, "5205020402");
                return result;
            }
            
            if (successResult.getCode().equals(BountyResultCode.SUCCESS)) {
                boolean isSuccess = successResult.getData();
                if (isSuccess) {
                    result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
                    return result;
                }else {
                    result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                    return result;
                }
            }else {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Module error bountyService.getRedPacketCount(%s)", userId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
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
        JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
        if(rtJO != null) {
            return rtJO;
        }
        
        JsonObject result = new JsonObject();
        
        // Token 校验
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        int redPacketLevel;
        
        String redPacketDate;
        
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            redPacketLevel = CommonUtil.getJsonParamInt(jsonObject, "redPacketLevel", 0, "5205020501", 1, Integer.MAX_VALUE);
            redPacketDate = CommonUtil.getJsonParamString(jsonObject, "redPacketDate", DateUtil.formatDate(new Date(), "yyyy-MM-dd"), null, 10, 10);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            BountyService bountyService = (BountyService) MelotBeanFactory.getBean("bountyService");

            Result<DailyRedPacket> packetResult = bountyService.openDailyRedPacket(userId, redPacketLevel, DateUtil.parseDateStringToDate(redPacketDate, null));
            if (packetResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            
            if (BountyResultCode.ERROR_SQL.equals(packetResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.EXECSQL_EXCEPTION);
                return result;
            }
            
            // 过期
            if (BountyResultCode.ERROR_DAILY_EXPIRE.equals(packetResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, "5205020502");
                return result;
            }
            
            // 达到上限20元
            if (BountyResultCode.ERROR_DAILY_UPPER_LIMIT.equals(packetResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, "5205020503");
                return result;
            }
            
            // 没有该等级红包
            if (BountyResultCode.ERROR_DAILY_NO_LEVEL.equals(packetResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, "5205020504");
                return result;
            }
            
            // 正在倒计时，不允许开启红包
            if (BountyResultCode.ERROR_DAILY_TIMING.equals(packetResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, "5205020505");
                return result;
            }
            
            if (packetResult.getCode().equals(BountyResultCode.SUCCESS)) {
                DailyRedPacket packet = packetResult.getData();
                result.addProperty("amount", packet.getAmount());
                if (packet.getRemainingTime() != null) {
                    result.addProperty("remainingTime", packet.getRemainingTime());
                }
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
                return result;
            }else {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Module error bountyService.openDailyRedPacket(%s, %s, %s)", userId, redPacketLevel, redPacketDate), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
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
        JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
        if(rtJO != null) {
            return rtJO;
        }
        
        JsonObject result = new JsonObject();
        
        // Token 校验
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            BountyService bountyService = (BountyService) MelotBeanFactory.getBean("bountyService");
            Result<UserBounty> bountyResult = bountyService.getUserBounty(userId);
            if (bountyResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            
            if (BountyResultCode.ERROR_SQL.equals(bountyResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.EXECSQL_EXCEPTION);
                return result;
            }
            
            if (bountyResult.getCode().equals(BountyResultCode.SUCCESS)) {

                UserBounty userBounty = bountyResult.getData();
                result.addProperty("bountyAmount", userBounty.getAmount());
                result.addProperty("totalBountyAmount", userBounty.getTotalAmount());
                result.addProperty("newUserCount", userBounty.getNewUserCount());
                result.addProperty("cashUserCount", userBounty.getRechargeCount());
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);

                return result;
            }else {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Module error bountyService.getUserBounty(%s)", userId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
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
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        int start;
        int offset;
        
        String dataMonth;
        
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            dataMonth = CommonUtil.getJsonParamString(jsonObject, "dataMonth", DateUtil.formatDate(new Date(), "yyyy-MM"), null, 7, 7);
            start = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.START, 0, null, 0, Integer.MAX_VALUE);
            offset = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.OFFSET, 20, null, 1, 30);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            BountyService bountyService = (BountyService) MelotBeanFactory.getBean("bountyService");
            Result<Page<UserBountyHist>> pageResult = bountyService.getUserBountyHists(userId, dataMonth, start, offset);
            if (pageResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            
            if (BountyResultCode.ERROR_SQL.equals(pageResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.EXECSQL_EXCEPTION);
                return result;
            }
            
            if (pageResult.getCode().equals(BountyResultCode.SUCCESS)) {
                Page<UserBountyHist> page = pageResult.getData();
                result.addProperty(ParameterKeys.COUNT, page.getCount());

                JsonArray bountyHists = new JsonArray();
                if (page.getList() != null) {
                    for (UserBountyHist userBountyHist : page.getList()) {
                        JsonObject histJson = new JsonObject();
                        histJson.addProperty(ParameterKeys.AMOUNT, userBountyHist.getAmount());
                        histJson.addProperty(ParameterKeys.TYPE, userBountyHist.getBountyType());
                        histJson.addProperty("addTime", DateUtil.formatDateTime(userBountyHist.getOpenTime(), null));
                        
                        bountyHists.add(histJson);
                    }
                }
                
                result.add("bountyHists", bountyHists);
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
                return result;
            }else {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Module error bountyService.getUserBounty(%s)", userId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }

    /**
     * 获取分享红包列表（52050209）
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject getSharRedPacketList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        // 安全校验
        JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
        if(rtJO != null) {
            return rtJO;
        }
        
        JsonObject result = new JsonObject();
        
        // Token 校验
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            BountyService bountyService = (BountyService) MelotBeanFactory.getBean("bountyService");
            Result<Page<ShareRedPacket>> pageResult = bountyService.getShareRedPackets(userId);
            if (pageResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            
            if (BountyResultCode.ERROR_SQL.equals(pageResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.EXECSQL_EXCEPTION);
                return result;
            }
            
            if (pageResult.getCode().equals(BountyResultCode.SUCCESS)) {

                Page<ShareRedPacket> page = pageResult.getData();
                JsonArray redPackets = new JsonArray();
                result.addProperty("redPacketDate", (String)page.getCommonInfo().get("redPacketDate"));
                result.addProperty("maxRedPacket", (Long)page.getCommonInfo().get("maxRedPacket"));
                if (page.getList() != null) {
                    for (ShareRedPacket packet : page.getList()) {
                        JsonObject packetJson = new JsonObject();
                        packetJson.addProperty("redPacketRuleId", packet.getRuleId());
                        packetJson.addProperty("redPacketName", packet.getRedPacketName());
                        packetJson.addProperty("maxAmount", packet.getMaxAmount());
                        packetJson.addProperty("state", packet.getState());
                        
                        redPackets.add(packetJson);
                    }
                }
                result.add("redPackets", redPackets);
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);

                return result;
            }else {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Module error bountyService.getShareRedPackets(%s)", userId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }

    /**
     * 开启分享红包（52050210）
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject openSharRedPacket(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        // 安全校验
        JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
        if(rtJO != null) {
            return rtJO;
        }
        
        JsonObject result = new JsonObject();
        
        // Token 校验
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
    
        int userId;
        int redPacketRuleId;
        String redPacketDate;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            redPacketRuleId = CommonUtil.getJsonParamInt(jsonObject, "redPacketRuleId", 0, "5205021001", 1, Integer.MAX_VALUE);
            redPacketDate = CommonUtil.getJsonParamString(jsonObject, "redPacketDate", DateUtil.formatDate(new Date(), "yyyy-MM-dd"), null, 10, 10);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            BountyService bountyService = (BountyService) MelotBeanFactory.getBean("bountyService");
            Result<Long> amountResult = bountyService.openShareRedPacket(userId, redPacketRuleId, DateUtil.parseDateStringToDate(redPacketDate, null));
            if (amountResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            
            if (BountyResultCode.ERROR_SQL.equals(amountResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.EXECSQL_EXCEPTION);
                return result;
            }
            
            // 红包过期
            if (BountyResultCode.ERROR_SHARE_EXPIRE.equals(amountResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, "5205021002");
                return result;
            }
            
            // 红包没有解锁
            if (BountyResultCode.ERROR_SHARE_LOCKED.equals(amountResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, "5205021003");
                return result;
            }
            
            // 改规则不存在或者红包已经领取
            if (BountyResultCode.ERROR_SHARE_NO_RULE.equals(amountResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, "5205021004");
                return result;
            }
            
            // 达到分享红包金额上限
            if (BountyResultCode.ERROR_SHARE_UPPER_LIMIT.equals(amountResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, "5205021005");
                return result;
            }
            
            if (amountResult.getCode().equals(BountyResultCode.SUCCESS)) {
    
                Long amount = amountResult.getData();
                result.addProperty("amount", amount);
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
    
                return result;
            }else {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Module error bountyService.openShareRedPacket(%s, %s, %s)", userId, redPacketRuleId, redPacketDate), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }

    /**
     * 获取分享红包状态（52050212）
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject getSharRedPacketState(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        // 安全校验
        JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
        if(rtJO != null) {
            return rtJO;
        }
        
        JsonObject result = new JsonObject();
        
        // Token 校验
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
    
        int userId;
        int redPacketRuleId;
        String redPacketDate;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            redPacketRuleId = CommonUtil.getJsonParamInt(jsonObject, "redPacketRuleId", 0, "5205021201", 1, Integer.MAX_VALUE);
            redPacketDate = CommonUtil.getJsonParamString(jsonObject, "redPacketDate", DateUtil.formatDate(new Date(), "yyyy-MM-dd"), null, 10, 10);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            BountyService bountyService = (BountyService) MelotBeanFactory.getBean("bountyService");
            Result<ShareRedPacket> redPacketResult = bountyService.getShareRedPacketInfo(userId, redPacketRuleId, DateUtil.parseDateStringToDate(redPacketDate, null));
            if (redPacketResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            
            if (BountyResultCode.ERROR_SQL.equals(redPacketResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.EXECSQL_EXCEPTION);
                return result;
            }
            
            // 红包过期
            if (BountyResultCode.ERROR_SHARE_EXPIRE.equals(redPacketResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, "5205021202");
                return result;
            }
            
            // 红包规则不存在或者红包已经领完
            if (BountyResultCode.ERROR_SHARE_NO_RULE.equals(redPacketResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, "5205021203");
                return result;
            }
            
            if (redPacketResult.getCode().equals(BountyResultCode.SUCCESS)) {
    
                ShareRedPacket shareRedPacket = redPacketResult.getData();
                result.addProperty("state", shareRedPacket.getState());
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
    
                return result;
            }else {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Module error bountyService.getShareRedPacketInfo(%s, %s, %s)", userId, redPacketRuleId, redPacketDate), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }

    /**
     * 获取奖励金题目信息（51050211）
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject getSharQuestionInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
    
        int questionId;
        int platform;
        
        try {
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, 1, Integer.MAX_VALUE);
            questionId = CommonUtil.getJsonParamInt(jsonObject, "questionId", 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        if (platform == PlatformEnum.WEB) {
            // PC版本获取的是分享后的明细
            if (questionId == 0) {
                result.addProperty(ParameterKeys.TAG_CODE, "5105021101");
                return result;
            }
            return getBountyQuestionInfo(questionId);
        } else {
            // 客户端获取的是随机的题目
            return getRandomBountyQuestion();
        }
    }
    
    /**
     * 随机获取奖励金题目信息
     * @return
     */
    private JsonObject getRandomBountyQuestion() {
        JsonObject result = new JsonObject();
        BountyQuestionService bountyQuestionService = (BountyQuestionService) MelotBeanFactory.getBean("bountyQuestionService");
        try {
            Result<List<BountyQuestion>> bountyQuestionInfoResult = bountyQuestionService.getRadomBountyQuestion(1);
            if (bountyQuestionInfoResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            
            if (BountyResultCode.ERROR_SQL.equals(bountyQuestionInfoResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.EXECSQL_EXCEPTION);
                return result;
            }
            
            if (bountyQuestionInfoResult.getCode().equals(BountyResultCode.SUCCESS)) {
    
                List<BountyQuestion> bountyQuestions = bountyQuestionInfoResult.getData();
                if (bountyQuestions == null || bountyQuestions.isEmpty()) {
                    result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                    return result;
                }
                BountyQuestion bountyQuestion = bountyQuestions.get(0);
                
                // 组织成JSON
                bountyQuestion2Json(bountyQuestion, result, false);
                
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            }else {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Module error bountyQuestionService.getRadomBountyQuestion(%s)", 1), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }
    
    /**
     * 获取奖励金题目信息详细信息
     * @param questionId
     * @return
     */
    private JsonObject getBountyQuestionInfo(int questionId) {
        JsonObject result = new JsonObject();
        BountyQuestionService bountyQuestionService = (BountyQuestionService) MelotBeanFactory.getBean("bountyQuestionService");
        try {
            Result<BountyQuestion> bountyQuestionInfoResult = bountyQuestionService.getBountyQuestionInfo(questionId);
            if (bountyQuestionInfoResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            
            if (BountyResultCode.ERROR_SQL.equals(bountyQuestionInfoResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.EXECSQL_EXCEPTION);
                return result;
            }
            
            if (bountyQuestionInfoResult.getCode().equals(BountyResultCode.SUCCESS)) {
    
                BountyQuestion bountyQuestion = bountyQuestionInfoResult.getData();
                
                // 组织成JSON
                bountyQuestion2Json(bountyQuestion, result, true);
                
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            }else {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Module error bountyQuestionService.getBountyQuestionInfo(%s)", questionId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }
    
    /**
     * 将奖励金问题信息组织成JSON
     * @param bountyQuestion
     * @param result
     * @param needRsult
     * @throws Exception
     */
    private void bountyQuestion2Json(BountyQuestion bountyQuestion, JsonObject result, boolean needRsult) {
        result.addProperty("questionId", bountyQuestion.getQuestionId());
        result.addProperty("questionTitle", bountyQuestion.getQuestionTitle());
        result.addProperty("questionStem", bountyQuestion.getQuestionStem());
        
        JsonArray questionOption = new JsonParser().parse(bountyQuestion.getQuestionOption()).getAsJsonArray();
        result.add("questionOption", questionOption);
        
        if (needRsult) {
            JsonArray questionResult = new JsonParser().parse(bountyQuestion.getQuestionResult()).getAsJsonArray();
            result.add("questionResult", questionResult);
        }
    } 
}

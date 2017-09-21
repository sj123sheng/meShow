/**
 * This document and its contents are protected by copyright 2012 and owned by
 * Melot Inc.
 * The copying and reproduction of this document and/or its content (whether
 * wholly or partly) or any
 * incorporation of the same into any other material in any media or format of
 * any kind is strictly prohibited.
 * All rights are reserved.
 * Copyright (c) Melot Inc. 2016
 */
package com.melot.kkcx.functions;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.melot.content.config.apply.service.ApplyActorService;
import com.melot.content.config.domain.ApplyActor;
import com.melot.game.config.sdk.utils.StringUtils;
import com.melot.kkcore.account.api.ResUserBoundAccount;
import com.melot.kkcore.account.service.AccountService;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kktv.redis.HotDataSource;
import com.melot.kktv.service.GeneralService;
//import com.melot.kktv.third.wechat.SendRedPackResponse;
//import com.melot.kktv.third.wechat.WechatHttpsRequest;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.DateUtil;
import com.melot.kktv.util.CommonUtil.ErrorGetParameterException;
import com.melot.kktv.util.ParameterKeys;
import com.melot.kktv.util.SecurityFunctions;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;
import com.melot.module.ModuleService;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * Title: WechatCash
 * <p>
 * Description: 微信提现功能集合
 * </p>
 * 
 * @author 宋建明<a href="mailto:jianming.song@melot.cn">
 * @version V1.0
 * @since 2017年9月18日 上午11:19:53
 */
public class WechatCashFunctions {

//    private static final Logger logger = Logger.getLogger(WechatCashFunctions.class);
//
//    /**
//     * 微信提现权限判断,是否绑定微信及手机(80010901)
//     * @param jsonObject
//     * @param checkTag
//     * @param request
//     * @return
//     */
//    public JsonObject checkCashCompetence(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
//
//        // 安全sv验证
//        JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
//        if (rtJO != null)
//            return rtJO;
//
//        JsonObject result = new JsonObject();
//        if (!checkTag) {
//            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
//            return result;
//        }
//
//        int userId;
//        try {
//            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
//
//            // 判断是否满足最低提现要求
//            checkCashCompetence(userId);
//        } catch (CommonUtil.ErrorGetParameterException e) {
//            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
//            return result;
//        } catch (Exception e) {
//            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
//            return result;
//        }
//
//        result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
//        return result;
//    }
//
//    public static void checkCashCompetence(int userId) throws CommonUtil.ErrorGetParameterException {
//        // 判断是否绑定手机
//        KkUserService userService = MelotBeanFactory.getBean("kkUserService", KkUserService.class);
//        UserProfile userProfile = userService.getUserProfile(userId);
//        if (userProfile == null || userProfile.getIdentifyPhone() == null) {
//            throw new CommonUtil.ErrorGetParameterException(TagCodeEnum.NON_IDENTITY_PHONE);
//        }
//
//        // 判断是否绑定微信
//        AccountService accountService = MelotBeanFactory.getBean("accountService", AccountService.class);
//        ResUserBoundAccount resUserBoundAccount = accountService.getUserBoundAccount(userId);
//        if (resUserBoundAccount == null || resUserBoundAccount.getWechatNickname() == null) {
//            throw new CommonUtil.ErrorGetParameterException(TagCodeEnum.NON_BIND_WECHAT);
//        }
//    }
//
//    /**
//     * 微信公众号发红包进行奖励金提现(51050208)
//     * @param jsonObject
//     * @param checkTag
//     * @param request
//     * @return
//     */
//    public JsonObject cashBounty(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
//
//        // 安全sv验证
//        JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
//        if (rtJO != null)
//            return rtJO;
//
//        JsonObject result = new JsonObject();
//        if (!checkTag) {
//            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
//            return result;
//        }
//
//        int userId;
//        int amount;
//        int platform;
//        String unionId = null;
//        String uuid = null;
//        String ua = null;
//        JsonElement uaje = jsonObject.get("ua");
//        if (uaje != null && !uaje.isJsonNull() && !uaje.getAsString().equals("")) {
//            ua = uaje.getAsString();
//        }
//        try {
//            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
//            unionId = CommonUtil.getJsonParamString(jsonObject, ParameterKeys.UNIONID, null, TagCodeEnum.UNIONID_MISSING, 1, Integer.MAX_VALUE);
//            uuid = CommonUtil.getJsonParamString(jsonObject, ParameterKeys.UUID, null, TagCodeEnum.UUID_MISSING, 1, Integer.MAX_VALUE);
//            amount = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.AMOUNT, 0, TagCodeEnum.AMOUNT_MISSING, 1, 200);
//            platform = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.PLATFORM, 0, TagCodeEnum.PLATFORM_MISSING, 1, Integer.MAX_VALUE);
//            
//            // 判断是否满足最低提现要求
//            checkCashCompetence(userId);
//        } catch (CommonUtil.ErrorGetParameterException e) {
//            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
//            return result;
//        } catch (Exception e) {
//            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
//            return result;
//        }
//        
//        // TODO 判断 unionId 是否正确
//
//        // 获取当天已提取记录(判断当天是否超过200)
//        String key = "cashBounty_" + DateUtil.formatDate(new Date(), "yyyy-MM-dd");
//        long totalAmount = HotDataSource.incHotFieldValue(key, Integer.toString(userId), amount);
//        if (totalAmount > 200) {
//            result.addProperty(ParameterKeys.TAG_CODE, "5105020801");
//            return result;
//        }
//        
//        // TODO 调用奖励金模块减去提现金额
//        
//        
//        // TODO 调用KK微信红包模块发红包
//        // 发送红包
//        WechatHttpsRequest httpsRequest = (WechatHttpsRequest) MelotBeanFactory.getBean("wechatHttpsRequest");
//        SendRedPackResponse response = httpsRequest.sendRedPack(amount, GeneralService.getIpAddr(request, 1, platform, null), uuid, "", "", "");
//        
//        if (!"SUCCESS".equals(response.getReturn_code())) {
//            // 提交失败
//            logger.info("err_msg:" + response.getReturn_msg());
//            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.WECAHTPUBLIC_RETURN_ERR);
//            return result;
//        } else if (!"SUCCESS".equals(response.getResult_code())) {
//            if ("NOTENOUGH".equals(response.getErr_code())) {
//                // 帐号余额不足，请到商户平台充值后再重试
//                logger.info("err_code:" + response.getErr_code() + "-----err_code_des:" + response.getErr_code_des());
//                List<ExchangeHistory> exchangeHistoryListAdd = exchangeService.listExchangeHistoryRecords(exchangeResult.getHistid(), userId, null, null, null, null, null,
//                        0, 50);
//                Result addResult = incomeService.addKbi(userId, exchangeHistoryListAdd.get(0).getKbi());
//                if (addResult.getTagcode() == 100) {
//                    Result modifyResult = exchangeService.modifyExchangeHistory(exchangeResult.getHistid(), 4, "账户余额不足", "wechat001");
//                    if (modifyResult.getTagcode() == 100) {
//                        result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.WECAHTPUBLIC_NOTENOUGH_ERR);
//                        return result;
//                    } else {
//                        logger.info("modifyExchangeHistory ERROR ,histid = " + exchangeResult.getHistid());
//                    }
//                } else {
//                    logger.info("addkbi err!tagcode=" + addResult.getTagcode() + ";userId=" + userId + ";amount=" + amount / 100);
//                    result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.WECAHTPUBLIC_ADD_KBI_ERR);
//                    return result;
//                }
//            } else if ("NO_AUTH".equals(response.getErr_code())) {
//                // 发放失败，此请求可能存在风险，已被微信拦截
//                logger.info("err_code:" + response.getErr_code() + "-----err_code_des:" + response.getErr_code_des());
//                List<ExchangeHistory> exchangeHistoryListAdd = exchangeService.listExchangeHistoryRecords(exchangeResult.getHistid(), userId, null, null, null, null, null,
//                        0, 50);
//                Result addResult = incomeService.addKbi(userId, exchangeHistoryListAdd.get(0).getKbi());
//                if (addResult.getTagcode() == 100) {
//                    Result modifyResult = exchangeService.modifyExchangeHistory(exchangeResult.getHistid(), 4, "发放失败，此请求可能存在风险，已被微信拦截", "wechat001");
//                    if (modifyResult.getTagcode() == 100) {
//                        result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.WECAHTPUBLIC_NO_AUTH_ERR);
//                        return result;
//                    } else {
//                        logger.info("modifyExchangeHistory ERROR ,histid = " + exchangeResult.getHistid());
//                    }
//                } else {
//                    logger.info("addkbi err!tagcode=" + addResult.getTagcode() + ";userId=" + userId + ";amount=" + amount / 100);
//                    result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.WECAHTPUBLIC_ADD_KBI_ERR);
//                    return result;
//                }
//                
//            } else if (response.getErr_code().equals("SENDNUM_LIMIT")) {
//                // 该用户今日领取红包个数超过限制
//                logger.info("err_code:" + response.getErr_code() + "-----err_code_des:" + response.getErr_code_des());
//                List<ExchangeHistory> exchangeHistoryListAdd = exchangeService.listExchangeHistoryRecords(exchangeResult.getHistid(), userId, null, null, null, null, null,
//                        0, 50);
//                Result addResult = incomeService.addKbi(userId, exchangeHistoryListAdd.get(0).getKbi());
//                if (addResult.getTagcode() == 100) {
//                    Result modifyResult = exchangeService.modifyExchangeHistory(exchangeResult.getHistid(), 4, "该用户今日操作次数超过限制", "wechat001");
//                    if (modifyResult.getTagcode() == 100) {
//                        result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.WECAHTPUBLIC_SENDNUM_LIMIT_ERR);
//                        return result;
//                    } else {
//                        logger.info("modifyExchangeHistory ERROR ,histid = " + exchangeResult.getHistid());
//                    }
//                } else {
//                    logger.info("addkbi err!tagcode=" + addResult.getTagcode() + ";userId=" + userId + ";amount=" + amount / 100);
//                    result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.WECAHTPUBLIC_ADD_KBI_ERR);
//                    return result;
//                }
//                
//            } else {
//                logger.info("err_code:" + response.getErr_code() + "-----err_code_des:" + response.getErr_code_des());
//                Result modifyResult = exchangeService.modifyExchangeHistory(exchangeResult.getHistid(), 3, response.getErr_code_des(), "wechat001");
//                if (modifyResult.getTagcode() == 100) {
//                    result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.WECAHTPUBLIC_RESULT_ERR);
//                    return result;
//                } else {
//                    logger.info("modifyExchangeHistory ERROR ,histid = " + exchangeResult.getHistid());
//                }
//            }
//        } else {
//            logger.info("SUCCESS! send_listid = " + response.getSend_listid());
//            Result modifyResult = exchangeService.modifyExchangeHistory(exchangeResult.getHistid(), 2, "发放成功: " + mchBillno, "wechat001");
//            if (modifyResult.getTagcode() == 100) {
//                sendTimMsg(amount / 100, new Date(), userId);
//                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
//                return result;
//            } else {
//                logger.info("modifyExchangeHistory ERROR ,histid = " + exchangeResult.getHistid());
//            }
//        }
//
//        return null;
//    }
//
//    /**
//     * 查询微信账号下的用户信息(80010904) 
//     * @param jsonObject
//     * @param checkTag
//     * @param request
//     * @return
//     */
//    public JsonObject getUserInfoByUnionId(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
//
//        // 安全sv验证
//        JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
//        if (rtJO != null)
//            return rtJO;
//
//        JsonObject result = new JsonObject();
//        String unionId = null;
//        try {
//            unionId = CommonUtil.getJsonParamString(jsonObject, "unionId", null, TagCodeEnum.UNIONID_MISSING, 1, Integer.MAX_VALUE);
//        } catch (Exception e) {
//            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
//            return result;
//        }
//
//        List<Integer> userIds = SqlMapClientHelper.getInstance(DB.MASTER).queryForList("UserMessage.getUserIdsByUnionId", unionId);
//        List<JsonObject> userInfoList = new ArrayList<JsonObject>();
//        for (Integer userId : userIds) {
//            UserInfo userInfo = com.melot.kktv.service.UserService.getUserInfo(userId);
//            if (userInfo != null) {
//                JsonObject userInfoJson = new JsonObject();
//                userInfoJson.addProperty("userId", userId);
//                userInfoJson.addProperty("nickName", userInfo.getNickname());
//                userInfoJson.addProperty("portrait", userInfo.getPortrait_path());
//
//                ActorService actorService = (ActorService) ModuleService.getService("ActorService");
//                ActorInfo actorInfo = actorService.getActorInfo(userId);
//                if (actorInfo != null) {
//                    // 收入棒票
//                    Long incomeBang = actorInfo.getKbi();
//                    // 提现比例
//                    int rate = actorInfo.getRate();
//                    // 提现金额
//                    double incomeCash = (double) incomeBang / 1000 * rate / 100;
//                    userInfoJson.addProperty("incomeCash", Math.floor(incomeCash));
//                    userInfoList.add(userInfoJson);
//                } else {
//                    logger.error("fail to get actorInfo,userId=" + userId);
//                    userInfoJson.addProperty("userId", userId);
//                    userInfoJson.addProperty("nickName", userInfo.getNickname());
//                    userInfoJson.addProperty("portrait", userInfo.getPortrait_path());
//                    userInfoJson.addProperty("incomeCash", 0);
//                    userInfoList.add(userInfoJson);
//                }
//            } else {
//                logger.error("fail to get userInfo,userId=" + userId);
//            }
//        }
//        result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
//        result.addProperty("userInfo", userInfoList.toString());
//        return result;
//    }
//
//    /**
//     * 微信提现消息发送
//     * @param cash
//     * @param time
//     * @param userId
//     */
//    private void sendTimMsg(int cash, Date time, int userId) {
//        try {
//            DateFormat fs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            String fsTime = fs.format(time);
//            TimMsgList timMsgList = MelotBeanFactory.getBean("timMsgList", TimMsgList.class);
//            TimMsg timMsg = timMsgList.getList().get("weChatCash");
//            String msg = String.format(timMsg.getMsg(), cash, fsTime);
//            TimService.sendTimSysMsg(timMsg.getAdmin(), String.valueOf(userId), msg);
//        } catch (Exception ex) {
//            logger.error("fail to  sendTimMsg,cash:" + cash + ",time:" + time + ",userId:" + userId + "", ex);
//        }
//    }
}

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

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.melot.kk.bounty.api.domain.base.BountyResultCode;
import com.melot.kk.bounty.api.domain.base.Result;
import com.melot.kk.bounty.api.service.BountyService;
import com.melot.kkcore.account.api.ResUserBoundAccount;
import com.melot.kkcore.account.service.AccountService;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kktv.service.GeneralService;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.LoginTypeEnum;
import com.melot.kktv.util.ParameterKeys;
import com.melot.kktv.util.SecurityFunctions;
import com.melot.kktv.util.TagCodeEnum;
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

    private static final Logger logger = Logger.getLogger(WechatCashFunctions.class);

    /**
     * 微信提现权限判断,是否绑定微信及手机(52050209)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject checkCashCompetence(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {

        // 安全sv验证
        JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
        if (rtJO != null)
            return rtJO;

        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);

            // 判断是否满足最低提现要求
            checkCashCompetence(userId);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
        return result;
    }

    private static void checkCashCompetence(int userId) throws CommonUtil.ErrorGetParameterException {
        // 判断是否绑定微信
        AccountService accountService = (AccountService)MelotBeanFactory.getBean("kkAccountService");
        ResUserBoundAccount resUserBoundAccount = accountService.getUserBoundAccount(userId);
        if (resUserBoundAccount == null || resUserBoundAccount.getWechatNickname() == null) {
            throw new CommonUtil.ErrorGetParameterException(TagCodeEnum.NON_BIND_WECHAT);
        }
    }

    /**
     * 微信公众号发红包进行奖励金提现(52050208)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject cashBounty(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {

        // 安全sv验证
        JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
        if (rtJO != null)
            return rtJO;

        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        int amount;
        int platform;
        String unionId = null;
        String uuid = null;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            unionId = CommonUtil.getJsonParamString(jsonObject, ParameterKeys.UNIONID, null, TagCodeEnum.UNIONID_MISSING, 1, Integer.MAX_VALUE);
            uuid = CommonUtil.getJsonParamString(jsonObject, ParameterKeys.UUID, null, TagCodeEnum.UUID_MISSING, 1, Integer.MAX_VALUE);
            amount = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.AMOUNT, 0, TagCodeEnum.AMOUNT_MISSING, 1, 20000);
            platform = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.PLATFORM, 0, TagCodeEnum.PLATFORM_MISSING, 1, Integer.MAX_VALUE);
            
            // 判断是否满足最低提现要求
            checkCashCompetence(userId);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            logger.error("param error:", e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        // 判断 unionId 是否正确
        try {
            AccountService accountService = (AccountService)MelotBeanFactory.getBean("kkAccountService");
            int tempUserId = accountService.isUnionIdValid(unionId, LoginTypeEnum.WEIXIN);
            if (tempUserId != userId) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.UNIONID_ERROR);
                return result;
            }
        } catch (Exception e) {
            logger.error(String.format("Module Error: accountService.isUnionIdValid(%s, %s)", unionId, LoginTypeEnum.WEIXIN), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
        
        // 调用奖励金模块提现发红包
        String clientIP = GeneralService.getIpAddr(request, 1, platform, null);
        try {
            BountyService bountyService = (BountyService) MelotBeanFactory.getBean("bountyService");
            Result<Boolean> wechatWithdrawCash = bountyService.wechatWithdrawCash(userId, amount, uuid, clientIP);
            if (wechatWithdrawCash == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            
            // 转换错误码
            String apiErrorCode = moduleCodeToAPICode(wechatWithdrawCash.getCode());
            if (apiErrorCode != null) {
                result.addProperty(ParameterKeys.TAG_CODE, apiErrorCode);
                return result;
            }
            
            // 发送成功
            if (BountyResultCode.SUCCESS.equals(wechatWithdrawCash.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
                return result;
            } else {
                logger.error(String.format("Module Error: bountyService.wechatWithdrawCash(%s, %s, %s, %s)， result=%s"
                        , userId, amount, uuid, clientIP, new Gson().toJson(wechatWithdrawCash)));
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            
        } catch (Exception e) {
            logger.error(String.format("Module Error: bountyService.wechatWithdrawCash(%s, %s, %s, %s)", userId, amount, uuid, clientIP), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }

    /**
     * 模块错误码转API错误码
     * @param code
     * @return
     */
    private String moduleCodeToAPICode(String code) {
        // 提现金额低于底线
        if (BountyResultCode.ERROE_BOUNTY_BLL.equals(code)) {
            return "5205020801";
        }
        
        // 提现超过日常额度限制
        if (BountyResultCode.ERROE_BOUNTY_DAY_ABOVE_LIMIT.equals(code)) {
            return "5205020802";
        }
        
        // 提现超过月额度限制
        if (BountyResultCode.ERROE_BOUNTY_MONTH_ABOVE_LIMIT.equals(code)) {
            return "5205020803";
        }
        
        // 奖励金金额不够
        if (BountyResultCode.ERROE_BOUNTY_NOT_ENOUGH.equals(code)) {
            return "5205020804";
        }
        
        // 微信公众号余额不足 
        if (BountyResultCode.ERROE_BOUNTY_WECHAT_NOTENOUGH.equals(code)) {
            return TagCodeEnum.WECAHTPUBLIC_NOTENOUGH_ERR;
        }
        
        // 发放失败，此请求可能存在风险，已被微信拦截
        if (BountyResultCode.ERROE_BOUNTY_WECHAT_NO_AUTH.equals(code)) {
            return TagCodeEnum.WECAHTPUBLIC_NO_AUTH_ERR;
        }
        
        // 该用户今日操作次数超过限制 
        if (BountyResultCode.ERROE_BOUNTY_WECHAT_SENDNUM_LIMIT.equals(code)) {
            return TagCodeEnum.WECAHTPUBLIC_SENDNUM_LIMIT_ERR;
        }
        
        // 与微信通信失败
        if (BountyResultCode.ERROE_BOUNTY_SEND_FAILE.equals(code)) {
            return TagCodeEnum.WECAHTPUBLIC_RESULT_ERR;
        }
        
        // 与微信通信失败
        if (BountyResultCode.ERROE_BOUNTY_SEND_FAILE.equals(code)) {
            return TagCodeEnum.WECAHTPUBLIC_RESULT_ERR;
        }
        
        return null;
    }
}
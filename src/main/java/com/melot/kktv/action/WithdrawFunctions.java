package com.melot.kktv.action;

import com.google.gson.JsonObject;
import com.melot.common.melot_utils.StringUtils;
import com.melot.kk.userSecurity.api.constant.IdPicStatusEnum;
import com.melot.kk.userSecurity.api.constant.SignElectronicContractStatusEnum;
import com.melot.kk.userSecurity.api.constant.UserVerifyStatusEnum;
import com.melot.kk.userSecurity.api.domain.DO.ActorKbiDO;
import com.melot.kk.userSecurity.api.domain.DO.UserBankAccountDO;
import com.melot.kk.userSecurity.api.domain.DO.UserElectronicContractDO;
import com.melot.kk.userSecurity.api.domain.DO.UserVerifyDO;
import com.melot.kk.userSecurity.api.domain.param.UserBankAccountParam;
import com.melot.kk.userSecurity.api.domain.param.UserVerifyParam;
import com.melot.kk.userSecurity.api.service.ActorWithdrawService;
import com.melot.kk.userSecurity.api.service.UserBankService;
import com.melot.kk.userSecurity.api.service.UserVerifyService;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kktv.base.CommonStateCode;
import com.melot.kktv.base.Result;
import com.melot.kktv.redis.SmsSource;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.SecurityFunctions;
import com.melot.kktv.util.TagCodeEnum;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @description: 主播提现接口
 * @author: shengjian
 * @date: 2018/3/13
 * @copyright: Copyright (c)2018
 * @company: melot
 * <p>
 * Modification History:
 * Date              Author      Version     Description
 * ------------------------------------------------------------------
 * 2018/3/13           shengjian     1.0
 */
public class WithdrawFunctions {

    private static Logger logger = Logger.getLogger(WithdrawFunctions.class);

    @Resource
    KkUserService userService;

    @Resource
    UserVerifyService userVerifyService;

    @Resource
    ActorWithdrawService actorWithdrawService;

    @Resource
    UserBankService userBankService;

    /**
     * 获取用户实名认证信息【51010601】
     */
    public JsonObject getUserVerifyInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {

        JsonObject result = new JsonObject();

        // sv安全校验接口
        JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
        if(rtJO != null)
            return rtJO;

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

        try {

            Result<UserVerifyDO> userVerifyDOResult = userVerifyService.getUserVerifyDO(userId);
            if(userVerifyDOResult.getCode().equals(CommonStateCode.SUCCESS) && userVerifyDOResult.getData() != null) {

                UserVerifyDO userVerifyDO = userVerifyDOResult.getData();
                Integer signStatus = userVerifyDO.getSignElectronicContract();

                result.addProperty("userId", userVerifyDO.getUserId());
                result.addProperty("verifyStatus", userVerifyDO.getVerifyStatus());
                result.addProperty("certNo", userVerifyDO.getCertNo());
                result.addProperty("certName", userVerifyDO.getCertName());
                result.addProperty("idPicStatus", userVerifyDO.getIdPicStatus());
                result.addProperty("verifyFailReason", userVerifyDO.getVerifyFailReason());
                result.addProperty("electronicContractStatus", signStatus);
                result.addProperty("verifyMobile", userVerifyDO.getVerifyMobile());
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);

                if(signStatus == SignElectronicContractStatusEnum.WAIT_SIGN) {
                    UserElectronicContractDO userElectronicContractDO = userVerifyService.getUserElectronicContractDO(userId).getData();
                    result.addProperty("electronicContractUrl", userElectronicContractDO.getElectronicContractUrl());
                }
                return result;
            }else {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
        } catch (Exception e) {
            logger.error("Error getUserVerifyInfo()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 上传身份证照片【51010602】
     */
    public JsonObject uploadIDPhoto(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {

        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        String idPicFont, idPicBack, idPicCompose;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
            idPicFont = CommonUtil.getJsonParamString(jsonObject, "idPicFont", "", null, 1, Integer.MAX_VALUE);
            idPicBack = CommonUtil.getJsonParamString(jsonObject, "idPicBack", "", null, 1, Integer.MAX_VALUE);
            idPicCompose = CommonUtil.getJsonParamString(jsonObject, "idPicCompose", "", null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        if(StringUtils.isEmpty(idPicFont) || StringUtils.isEmpty(idPicBack) ||StringUtils.isEmpty(idPicCompose)) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_ILLEAGLE);
            return result;
        }

        try {

            String identifyPhone = userService.getUserProfile(userId).getIdentifyPhone();
            if(StringUtils.isEmpty(identifyPhone)) {
                result.addProperty("TagCode", TagCodeEnum.NON_IDENTITY_PHONE);
                return result;
            }

            UserVerifyDO userVerifyDO = userVerifyService.getUserVerifyDO(userId).getData();
            if(userVerifyDO == null || userVerifyDO.getVerifyStatus() != UserVerifyStatusEnum.VERIFY_SUCCESS) {
                result.addProperty("TagCode", TagCodeEnum.ID_NOT_IDENTIFY);
                return result;
            }
            UserVerifyParam userVerifyParam = new UserVerifyParam();
            userVerifyParam.setUserId(userId);
            userVerifyParam.setWithdrawIdPicFont(idPicFont);
            userVerifyParam.setWithdrawIdPicFont(idPicBack);
            userVerifyParam.setWithdrawIdPicCompose(idPicCompose);
            userVerifyParam.setIdPicStatus(IdPicStatusEnum.WAIT_AUDIT);
            Result<Boolean> updateResult = userVerifyService.updateUserVerify(userVerifyParam);
            result.addProperty("uploadResult", updateResult.getData());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error uploadIDPhoto()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取用户绑定银行卡信息【51010603】
     */
    public JsonObject getUserBindBankCardInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {

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

        try {

            int bindBankCard = 0;
            UserBankAccountDO userBankAccountDO = userBankService.getUserBankAccount(userId).getData();
            if(userBankAccountDO != null || StringUtils.isNotEmpty(userBankAccountDO.getBankcard())) {
                bindBankCard = 1;
                String bankcard = userBankAccountDO.getBankcard();
                String tailNumber = bankcard;
                if(bankcard.length() >= 4) {
                    tailNumber = bankcard.substring(bankcard.length()-4);
                }
                result.addProperty("bankName", userBankAccountDO.getBankname());
                result.addProperty("tailNumber", tailNumber);
            }

            result.addProperty("bindBankCard", bindBankCard);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error getUserBindBankCardInfo()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 解绑银行卡【51010604】
     */
    public JsonObject unbindBankCard(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {

        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        String verifyCode;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
            verifyCode = CommonUtil.getJsonParamString(jsonObject, "verifyCode", "", null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        // 获取用户实名认证手机号
        String identifyPhone = userService.getUserProfile(userId).getIdentifyPhone();
        if(StringUtils.isEmpty(identifyPhone)) {
            result.addProperty("TagCode", TagCodeEnum.NON_IDENTITY_PHONE);
            return result;
        }

        String data = SmsSource.getPhoneSmsData(identifyPhone, String.valueOf(35));
        if (data != null && data.equals(verifyCode)) {
            try {
                Boolean unbindResult = userBankService.unbindBankAccount(userId).getData();

                result.addProperty("unbindResult", unbindResult);
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            } catch (Exception e) {
                logger.error("Error unbindBankCard()", e);
                result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
        } else {
            result.addProperty("TagCode", TagCodeEnum.VERIFYCODE_ERROR);
            return result;
        }
    }

    /**
     * 绑定银行卡【51010605】
     */
    public JsonObject bindBankCard(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {

        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        String bankCard;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
            bankCard = CommonUtil.getJsonParamString(jsonObject, "bankCard", "", null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        if(StringUtils.isEmpty(bankCard)) {
            result.addProperty("TagCode", TagCodeEnum.BANKCARD_MISS);
            return result;
        }

        try {
            UserBankAccountParam userBankAccountParam = new UserBankAccountParam();
            userBankAccountParam.setUserId(userId);
            userBankAccountParam.setBankcard(bankCard);
            Boolean bindResult = userBankService.bindBankAccount(userBankAccountParam).getData();

            result.addProperty("bindResult", bindResult);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error bindBankCard()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取主播提现信息【51010606】
     */
    public JsonObject getActorWithdrawInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {

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

        try {

            long kBeans;
            ActorKbiDO actorKbiDO = actorWithdrawService.getActorKbiDO(userId).getData();
            if(actorKbiDO != null && actorKbiDO.getKbi() != null) {
                kBeans = actorKbiDO.getKbi();
            }else {
                result.addProperty("TagCode", TagCodeEnum.GETKBI_ERROR);
                return result;
            }

            result.addProperty("kBeans", kBeans);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error getActorWithdrawInfo()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 主播提现【51010607】
     */
    public JsonObject withdraw(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {

        JsonObject result = new JsonObject();

        // sv安全校验接口
        JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
        if(rtJO != null)
            return rtJO;

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId, withdrawAmount;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
            withdrawAmount = CommonUtil.getJsonParamInt(jsonObject, "withdrawAmount", 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            Result<Boolean> withdrawResult = actorWithdrawService.actorWithdraw(userId, withdrawAmount);
            if(withdrawResult.getCode().equals(CommonStateCode.SUCCESS) && withdrawResult.getData() != null) {

                result.addProperty("withdrawResult",  withdrawResult.getData());
            }else {
                result.addProperty("TagCode", withdrawResult.getCode());
                return result;
            }

            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error getActorWithdrawInfo()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

}

package com.melot.kktv.action;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.antgroup.zmxy.openplatform.api.response.ZhimaCustomerCertificationInitializeResponse;
import com.antgroup.zmxy.openplatform.api.response.ZhimaCustomerCertificationQueryResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.blacklist.service.BlacklistService;
import com.melot.content.config.apply.service.ApplyActorService;
import com.melot.content.config.domain.ApplyActor;
import com.melot.content.config.domain.ZmrzApply;
import com.melot.content.config.utils.Constants;
import com.melot.content.config.utils.IdPicStatusEnum;
import com.melot.content.config.utils.VerifyTypeEnum;
import com.melot.content.config.utils.ZmrzStatusEnum;
import com.melot.family.driver.domain.FamilyInfo;
import com.melot.family.driver.service.FamilyOperatorService;
import com.melot.game.config.sdk.utils.StringUtils;
import com.melot.kkcore.actor.api.ActorInfo;
import com.melot.kkcore.actor.service.ActorService;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcore.user.api.ShowMoneyHistory;
import com.melot.kkcore.user.api.UserAssets;
import com.melot.kkcore.user.api.UserStaticInfo;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kkcx.service.FamilyService;
import com.melot.kktv.redis.GiftRecordSource;
import com.melot.kktv.service.ConfigService;
import com.melot.kktv.service.UserService;
import com.melot.kkcx.model.ActorProfit;
import com.melot.kktv.third.service.ZmxyService;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.BizCodeEnum;
import com.melot.kktv.util.CollectionUtils;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.DateUtil;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.sdk.core.util.MelotBeanFactory;
import com.melot.share.driver.domain.RankData;
import com.melot.share.driver.service.ShareActivityService;

/**
 * Title: ActorFunction
 * <p>
 * Description: 主播相关API接口
 * </p>
 * 
 * @author shengjian
 * @version V1.0
 * @since 2017年8月04日
 */
public class ActorFunction {

    private static Logger logger = Logger.getLogger(ActorFunction.class);
    
    @Autowired
    private ConfigService configService;

    /**
     * 获取主播代言团列表【51020101】
     * @param jsonObject
     * @param request
     * @return
     */
    public JsonObject getRepresentList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        // 定义返回结果
        JsonObject result = new JsonObject();
        
        int actorId, userId;
        
        try {
            actorId = CommonUtil.getJsonParamInt(jsonObject, "actorId", 0, null, 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        // 获取主播列表
        try {
            ShareActivityService shareActivityService = MelotBeanFactory.getBean("shareActivityService", ShareActivityService.class);
            List<RankData> rankDataList = shareActivityService.getRankList(userId > 0 ? userId : null, actorId);
            
            if (CollectionUtils.isEmpty(rankDataList)) {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            
            JsonArray representList = new JsonArray();

            int rankDataSize = rankDataList.size();
            for (int i = 0; i < rankDataSize; i++) {

                RankData rankData = rankDataList.get(i);
                JsonObject representJson = new JsonObject();
                representJson.addProperty("userId", rankData.getUserId());
                representJson.addProperty("nickname", rankData.getNickName());
                representJson.addProperty("gender", rankData.getGender());
                representJson.addProperty("ranking", rankData.getRank());
                String identity = "";
                if(rankData.getRank() != null) {
                    switch (rankData.getRank()) {
                        case 1:
                            identity = "团长";
                            break;
                        case 2:
                            identity = "副团长";
                            break;
                        case 3:
                            identity = "副团长";
                            break;
                        default:
                            break;
                    }
                }

                representJson.addProperty("identity", identity);

                representJson.addProperty("absorbFansCount", rankData.getUserCount());
                representJson.addProperty("shareCount", rankData.getShareTimes());
                representJson.addProperty("onlookersCount", rankData.getUserUv());

                if (rankData.getPortrait() != null) {
                    String portraitAddress = rankData.getPortrait();
                    representJson.addProperty("portrait_path_original", portraitAddress);
                    representJson.addProperty("portrait_path_48", portraitAddress + "!48");
                    representJson.addProperty("portrait_path_128", portraitAddress + "!128");
                    representJson.addProperty("portrait_path_256", portraitAddress + "!256");
                    representJson.addProperty("portrait_path_272", portraitAddress + "!272");
                    representJson.addProperty("portrait_path_1280", portraitAddress + "!1280");
                    representJson.addProperty("portrait_path_400", portraitAddress + "!400");
                    representJson.addProperty("portrait_path_756", portraitAddress + "!756x567");
                }

                if(userId > 0 && i == rankDataSize - 1) {
                    result.add("mySelfRepresent", representJson);
                }else {
                    representList.add(representJson);
                }

            }


            result.add("representList", representList);
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;

        } catch (Exception e) {
            logger.error("Module Error ShareActivityService.getRankList(" + actorId + ", " + userId + ")", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 更新H5页面上的 UV 数据 (UV：独立访问用户数)【51020102】
     * @param jsonObject
     * @param request
     * @return
     */
    public JsonObject updateUV(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        // 定义返回结果
        JsonObject result = new JsonObject();

        int appId, actorId, userId;
        String deviceUId;
        try {
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 1, Integer.MAX_VALUE);
            actorId = CommonUtil.getJsonParamInt(jsonObject, "actorId", 0, null, 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
            deviceUId = CommonUtil.getJsonParamString(jsonObject, "deviceUId", "", null, 1, Integer.MAX_VALUE);
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

        // 更新UV
        try {
            ShareActivityService shareActivityService = MelotBeanFactory.getBean("shareActivityService", ShareActivityService.class);
            String clientIP = com.melot.kktv.service.GeneralService.getIpAddr(request, appId, appId, null);
            boolean updateUVResult = shareActivityService.recordUv(userId, actorId, clientIP, deviceUId);

            result.addProperty("updateUVResult", updateUVResult);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;

        } catch (Exception e) {
            logger.error("Module Error ShareActivityService.getRankList(" + actorId + ", " + userId + ")", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取h5新注册用户是否领取过新手礼包【51020103】
     * @param jsonObject
     * @param request
     * @return
     */
    public JsonObject getUserReceivedNoviceGift(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        // 定义返回结果
        JsonObject result = new JsonObject();

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

            //查询该用户是否未领取过新手礼包 true-未领取过 false-领取过
            boolean unReceivedNoviceGift = GiftRecordSource.unReceivedNoviceGift(userId);

            if(unReceivedNoviceGift) {
                GiftRecordSource.removeNoviceGift(userId);
            }

            result.addProperty("haveReceivedNoviceGift", !unReceivedNoviceGift);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;

        } catch (Exception e) {
            logger.error("Module Error ActorFunction.getUserReceivedNoviceGift(" + userId + ")", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 申请成为主播（接入芝麻认证-芝麻认证初始化接口）【52020101】
     * @param jsonObject
     * @param request
     * @return
     */
    public JsonObject applyForActor(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        // 定义返回结果
        JsonObject result = new JsonObject();
        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int bizCode, userId, appId, familyId;
        String certName, certNo, returnUrl;
        try {
            bizCode = CommonUtil.getJsonParamInt(jsonObject, "bizCode", BizCodeEnum.FACE.getId(), null, 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
            certName = CommonUtil.getJsonParamString(jsonObject, "certName", "", null, 1, Integer.MAX_VALUE);
            certNo = CommonUtil.getJsonParamString(jsonObject, "certNo", "", null, 1, Integer.MAX_VALUE);
            if (!StringUtil.strIsNull(certNo) && (certNo.length() == 15 || certNo.length() == 18)) {
                certNo = certNo.toUpperCase();
                String birthStr;
                if (certNo.length() == 15) {
                    birthStr = 19 + certNo.substring(6, 12);
                } else {
                    birthStr = certNo.substring(6, 14);
                }
                Date birthDate = DateUtil.parseDateTimeStringToDate(birthStr, "yyyyMMdd");
                if (birthDate.after(DateUtil.addOnField(new Date(), 1, -18))) {
                    result.addProperty("TagCode", "5202010101");
                    return result;
                }
            }
            returnUrl = CommonUtil.getJsonParamString(jsonObject, "returnUrl", "", null, 1, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 1, Integer.MAX_VALUE);
            familyId = CommonUtil.getJsonParamInt(jsonObject, "familyId", 0, null, 1, Integer.MAX_VALUE);
            if (familyId == 11222 && configService.getIsSpecialTime()) {
                result.addProperty("TagCode", TagCodeEnum.FUNCTAG_UNUSED_EXCEPTION);
                return result;
            }
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
            // 校验是否能成为主播
            boolean verifyResult = verifyApplyForActor(result, userId, certNo, familyId, appId);

            // 芝麻认证
            String bizNo = "";
            ZhimaCustomerCertificationInitializeResponse response = null;
            if(verifyResult) {

                response = ZmxyService.getBizNo(userId, bizCode, certName, certNo);

                if (response.isSuccess()) {
                    bizNo = response.getBizNo();
                    if (StringUtils.isEmpty(bizNo)) {
                        result.addProperty("TagCode", response.getErrorCode());
                        result.addProperty("errorMessage", response.getErrorMessage());
                        return result;
                    } else if (bizCode == 1) {
                        String verifyUrl = ZmxyService.getUrl(bizNo, returnUrl);
                        result.addProperty("verifyUrl", verifyUrl);
                    } else if (bizCode == 2) {
                        result.addProperty("merchantId", ZmxyService.MERCHANT_ID);
                    }
                    result.addProperty("bizNo", bizNo);
                } else {
                    result.addProperty("TagCode", response.getErrorCode());
                    result.addProperty("errorMessage", response.getErrorMessage());
                    return result;
                }

                // 插入一条芝麻认证记录
                ApplyActorService applyActorService = MelotBeanFactory.getBean("applyActorService", ApplyActorService.class);
                ZmrzApply zmrzApply = new ZmrzApply();
                Date now = new Date();
                zmrzApply.setBizNo(bizNo);
                zmrzApply.setUserId(userId);
                zmrzApply.setTransactionId(response.getBody());
                zmrzApply.setAppId(appId);
                zmrzApply.setStatus(ZmrzStatusEnum.WAIT_VERIFY.getId());
                zmrzApply.setCreateTime(now);
                zmrzApply.setUpdateTime(now);
                zmrzApply.setCertNo(certNo);
                zmrzApply.setCertName(certName);
                applyActorService.saveZmrzApply(zmrzApply);
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            }

            return result;

        } catch (Exception e) {
            logger.error("API Error ActorFunction.applyForActor userId:" + userId, e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 50001024-校验芝麻认证是否通过 认证成功：如果申请的是自由主播直接变成主播，如果是家族主播等待家族审核通过后变成主播 认证失败返回申请状态为空【52020102】
     * @param jsonObject
     * @param checkTag
     * @return
     */
    public JsonObject verifyAuthResult(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();
        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId,familyId,appId;
        String bizNo,certNo;
        try {
            bizNo = CommonUtil.getJsonParamString(jsonObject, "bizNo", "", null, 1, Integer.MAX_VALUE);
            certNo = CommonUtil.getJsonParamString(jsonObject, "certNo", "", null, 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            familyId = CommonUtil.getJsonParamInt(jsonObject, "familyId", 0, null, 1, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, TagCodeEnum.PARAMETER_NOTCONTAINED_FUNCTAG, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        if(StringUtils.isEmpty(bizNo) || StringUtils.isEmpty(certNo)) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_MISSING);
            return result;
        }

        // 芝麻认证校验
        ZhimaCustomerCertificationQueryResponse response = ZmxyService.getResult(bizNo);
        boolean verifyResult = false;
        String certName = "";
        if(response != null && response.isSuccess() && Boolean.parseBoolean(response.getPassed())) {

            ApplyActorService applyActorService = MelotBeanFactory.getBean("applyActorService", ApplyActorService.class);
            ZmrzApply zmrzApply = applyActorService.getZmrzApplyByBizNo(bizNo);
            String verifyCertNo = zmrzApply.getCertNo();
            certName = zmrzApply.getCertName();

            if(!StringUtils.isEmpty(verifyCertNo) && certNo.equals(verifyCertNo)) {
                verifyResult = true;
            }else {
                result.addProperty("errorMessage", "身份证号码不一致");
            }
        }else if(!response.isSuccess()) {
            result.addProperty("errorMessage", response.getErrorMessage());
        }else {
            result.addProperty("errorMessage", response.getFailedReason());
        }

        result.addProperty("verifyResult", verifyResult);

        // 更新芝麻认证记录的状态
        ApplyActorService applyActorService = MelotBeanFactory.getBean("applyActorService", ApplyActorService.class);
        int verifyStatus = verifyResult ? ZmrzStatusEnum.VERIFY_PASS.getId() : ZmrzStatusEnum.VERIFY_FAIL.getId();
        applyActorService.updateZmrzApplyStatus(bizNo, verifyStatus);

        if (!verifyResult) {
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        }

        // 插入主播申请记录
        if(!verifyAndApplyForActor(result, userId, certName, certNo, familyId, appId)) {
            return result;
        }

        if(familyId == 0) { // 自由主播
            try {
                //自动变为终审通过
                if (FamilyService.checkBecomeFamilyMember(userId, Constants.APPLY_ACTOR_OFFICIAL_CHECK_SUCCESS, appId)) {
                    result.addProperty("status", Constants.APPLY_ACTOR_OFFICIAL_CHECK_SUCCESS);
                    result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                } else {
                    result.addProperty("TagCode", TagCodeEnum.FAIL_TO_UPDATE);
                }
                return result;
            } catch (Exception e) {
                logger.error("Fail to call module", e);
                result.addProperty("TagCode", TagCodeEnum.FAIL_TO_CALL_API_CONTENT_MODULE);
                return result;
            }
        } else {  // 家族主播
            FamilyInfo familyInfo = FamilyService.getFamilyInfoByFamilyId(familyId);
            result.addProperty("status", Constants.APPLY_TEST_ACTOR_IN_FAMILY_PLAYING);
            result.addProperty("familyName", familyInfo.getFamilyName());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        }
    }

    private Boolean verifyApplyForActor(JsonObject result,int userId, String identityId, int familyId, int appId) {

        // 身份证黑名单不得申请
        BlacklistService blacklistService = (BlacklistService) MelotBeanFactory.getBean("blacklistService");
        if (blacklistService.isIdentityBlacklist(identityId)) {
            result.addProperty("TagCode", TagCodeEnum.IDENTITY_BLACK_LIST);
            return false;
        }

        UserStaticInfo userInfo = UserService.getUserStaticInfoV2(userId);
        // 用户昵称为空不能申请
        if (StringUtil.strIsNull(userInfo.getProfile().getNickName())) {
            result.addProperty("TagCode", TagCodeEnum.NICKNAME_EMPTY);
            return false;
        }

        // 游客不能申请
        if(userInfo.getRegisterInfo().getOpenPlatform() == 0 || userInfo.getRegisterInfo().getOpenPlatform() == -5) {
            result.addProperty("TagCode", TagCodeEnum.USER_IS_VISITOR);
            return false;
        }

        // 黑名单用户不能申请
        if (com.melot.kkcx.service.UserService.blackListUser(userId)) {
            result.addProperty("TagCode", TagCodeEnum.USER_IN_BLACK);
            return false;
        }

        // 家族成员不能申请成为家族主播
        if (FamilyService.isFamilyMember(userId)) {
            result.addProperty("TagCode", TagCodeEnum.MEMBER_CANT_APPLY);
            return false;
        }

        // 未绑定手机的用户不能申请
        String mobileNum = "";
        try {
            KkUserService userService = MelotBeanFactory.getBean("kkUserService", KkUserService.class);
            UserProfile userProfile = userService.getUserProfile(userId);
            if (userProfile == null || userProfile.getIdentifyPhone() == null) {
                result.addProperty("TagCode", "01200002");
                return false;
            }
            mobileNum = userProfile.getIdentifyPhone();
        } catch (Exception e) {
            logger.error("Fail to get KkUserService.getUserProfile. userId: " + userId, e);
        }

        ApplyActorService applyActorService = MelotBeanFactory.getBean("applyActorService", ApplyActorService.class);

        if (familyId > 0) {
            FamilyInfo familyInfo = FamilyService.getFamilyInfoByFamilyId(familyId);
            // 判断家族是否存在
            if (familyInfo == null) {
                result.addProperty("TagCode", TagCodeEnum.FAMILY_ISNOT_EXIST);
                return false;
            }
        }

        // 判断该身份证是否已经申请过主播 驳回状态可以重新申请
        List<ApplyActor> applies = applyActorService.getApplyActorsByParameter(identityId, mobileNum, null);
        if (applies != null && applies.size() > 0) {
            for (ApplyActor apply : applies) {
                //巡管审核驳回 或 家族驳回
                if (apply.getStatus() < 0 || apply.getStatus() == 6) {
                    continue;
                }
                if (apply.getActorId().equals(userId)) {
                    if (apply.getAppId().equals(appId)) {
                        result.addProperty("TagCode", TagCodeEnum.HAS_APPLY_PLAY);
                        return false;
                    } else {
                        result.addProperty("TagCode", TagCodeEnum.HAS_APPLY_OTHER_APP);
                        return false;
                    }
                } else if (apply.getAppId().equals(appId)) {
                    // 身份证已经存在
                    if (apply.getIdentityNumber() != null && apply.getIdentityNumber().equals(identityId)) {
                        result.addProperty("TagCode", TagCodeEnum.APPLY_IDNUM_EXISTS);
                        return false;
                    }
                    // 手机号已经存在
                    if (apply.getMobile() != null && apply.getMobile().equals(mobileNum)) {
                        result.addProperty("TagCode", TagCodeEnum.APPLY_MOBILE_EXISTS);
                        return false;
                    }
                }
            }
        }

        // 判断这个用户是否已经在申请主播并且不是驳回状态 驳回状态可以重新申请
        ApplyActor oldApplyActor = applyActorService.getApplyActorByActorId(userId);
        if (oldApplyActor != null && oldApplyActor.getStatus() >= 0 && oldApplyActor.getStatus() != 6) {
            if (oldApplyActor.getAppId().equals(appId)) {
                result.addProperty("TagCode", TagCodeEnum.HAS_APPLY_PLAY);
                return false;
            } else {
                result.addProperty("TagCode", TagCodeEnum.HAS_APPLY_OTHER_APP);
                return false;
            }
        }

        // 查看同一身份证是否有绑定的家族ID
        Integer bindfamilyId = applyActorService.getFamilyIdByIdentityNumber(identityId);
        if (bindfamilyId != null) {
            FamilyInfo otherFamilyInfo = FamilyService.getFamilyInfoByFamilyId(bindfamilyId);
            if (otherFamilyInfo != null) {
                result.addProperty("TagCode", TagCodeEnum.IDENTITY_HAS_FAMILY);
                return false;
            }
        }

        return true;
    }

    private Boolean verifyAndApplyForActor(JsonObject result,int userId, String certName, String identityId, int familyId, int appId) {

        // 如果校验成功
        if(verifyApplyForActor(result, userId, identityId, familyId, appId)) {
            KkUserService userService = MelotBeanFactory.getBean("kkUserService", KkUserService.class);
            UserProfile userProfile = userService.getUserProfile(userId);
            ApplyActor applyActor = new ApplyActor();
            applyActor.setActorId(userId);
            applyActor.setAppId(appId);
            applyActor.setRealName(certName);
            applyActor.setIdentityNumber(identityId);
            applyActor.setMobile(userProfile.getIdentifyPhone());
            applyActor.setGender(StringUtil.parseFromStr(identityId.substring(16, 17), 0) % 2);
            applyActor.setIdPicStatus(IdPicStatusEnum.UNLOAD.getId());
            applyActor.setVerifyType(VerifyTypeEnum.ZM_VERIFY.getId());
            int status = 0;
            if (familyId > 0) {
                applyActor.setApplyFamilyId(familyId);
                status = Constants.APPLY_TEST_ACTOR_IN_FAMILY_PLAYING;
            } else {
                //自由主播
                familyId = 11222;
                applyActor.setApplyFamilyId(11222);
                status = Constants.APPLY_ACTOR_INFO_CHECK_SUCCESS;
            }

            ApplyActorService applyActorService = MelotBeanFactory.getBean("applyActorService", ApplyActorService.class);
            boolean saveResult = applyActorService.saveApplyActorV2(applyActor);
            
            if (saveResult) {
                try {
                    FamilyOperatorService familyOperatorService = (FamilyOperatorService) MelotBeanFactory.getBean("familyOperatorService");
                    saveResult = familyOperatorService.checkActorApply(userId, familyId, status, null, null, appId);
                } catch (Exception e) {
                    saveResult = false;
                    logger.error("familyOperatorService.checkActorApply(" + userId + ", " + familyId + ", " + status + ") execute exception", e);
                }
            }
            
            if (saveResult) {
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            } else {
                result.addProperty("TagCode", TagCodeEnum.FAIL_SAVE_APPLY);
                return false;
            }

            return true;
        }else {
            return false;
        }

    }
    
    /**
     * 获取第三方主播开播权限（51020203）
     * 
     * @param jsonObject 请求对象
     * @param request 请求对象
     * @return 
     */
    public JsonObject getThirdPlatformActorBroadcastState(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        int userId;
        
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        int state = 0;
        try {
            ActorService actorService = (ActorService) MelotBeanFactory.getBean("actorService");
            List<ActorInfo> actors = actorService.getThirdPlatformActors(userId);
            if (actors != null && actors.size() > 0) {
                state = actors.get(0).getThirdPlatformPermission();
            }
        } catch (Exception e) {
            logger.error("ActorService.getThirdPlatformActors actorId :" + userId + "return exception.", e);
            result.addProperty("TagCode", TagCodeEnum.UNCATCHED_EXCEPTION);
            return result;
        }
       
        result.addProperty("state", state);
        result.addProperty("TagCode",  TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 获取主播收益列表（51020201）
     * 
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getProfitList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        int userId;
        int pageIndex;
        int countPerPage;
        
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 10, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            int profitListCount = com.melot.kkcx.service.UserService.getActorProfitCount(userId);
            JsonArray profitList = new JsonArray();
            if (profitListCount > 0) {
                int offset = (pageIndex - 1) * countPerPage;
                if (offset < profitListCount) {
                    List<ActorProfit> actorProfitList = com.melot.kkcx.service.UserService.getActorProfitList(userId, offset, countPerPage);
                    for (ActorProfit actorProfit : actorProfitList) {
                        JsonObject jsonObj = new JsonObject();
                        jsonObj.addProperty("liveDuration", actorProfit.getTotalLiveTime());
                        jsonObj.addProperty("kbi", actorProfit.getTotalRsv());
                        jsonObj.addProperty("month", actorProfit.getMonthTime());
                        profitList.add(jsonObj);
                    }
                }
            }
            
            result.addProperty("count", profitListCount);
            result.add("profitList", profitList);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch(Exception e) {
            logger.error("UserService.getActorProfitList(" + userId + ", " + pageIndex + ", " + countPerPage + ") return exception.", e);
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
        }
        
        return result;
    }
    
    /**
     * 主播K豆兑换秀币（51020202）
     * 
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject exchangeKbi(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        int userId;
        int exchangeAmount;
        
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            exchangeAmount = CommonUtil.getJsonParamInt(jsonObject, "exchangeAmount", 1, "5102020201", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            Long actorKbi = com.melot.kkcx.service.UserService.getActorKbi(userId);
            if (actorKbi >= exchangeAmount && com.melot.kkcx.service.UserService.exchangeKbi(userId, exchangeAmount)) {
                KkUserService kkuserService= (KkUserService) MelotBeanFactory.getBean("kkUserService");
                if (kkuserService != null) {
                    try {
                        ShowMoneyHistory hist = new ShowMoneyHistory();
                        hist.setType(11);
                        hist.setUserId(userId);
                        hist.setToUserId(userId);
                        hist.setIncomeAmount(exchangeAmount);
                        hist.setAppId(AppIdEnum.AMUSEMENT);
                        hist.setProductDesc("主播K豆兑换");
                        hist.setDtime(new Date());
                        
                        UserAssets userAset = kkuserService.addAndGetUserAssets(userId, exchangeAmount, false, hist);
                        if(userAset == null) {
                            logger.error("kkuserService.addAndGetUserAssets(" + userId + ", " + exchangeAmount + ") 秀币发放异常");
                            result.addProperty("TagCode", "5102020202");
                        } else {
                            result.addProperty("kbi", actorKbi - exchangeAmount);
                            result.addProperty("showMoney", userAset.getShowMoney());
                            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                        }
                        
                        com.melot.kkcx.service.UserService.insertKbiHist(userId, exchangeAmount);
                    } catch (Exception e) {
                        logger.error("kkuserService.addAndGetUserAssets(" + userId + ", " + exchangeAmount + ") 秀币发放异常");
                    }
                } else {
                    result.addProperty("TagCode", "5102020202");
                    logger.error("ActorFunction.exchangeKbi(" + userId + ", " + exchangeAmount + ") 兑币发放异常");
                }
            } else {
                result.addProperty("TagCode", "5102020202");
            }
        } catch(Exception e) {
            logger.error("ActorFunction.exchangeKbi(" + userId + ", " + exchangeAmount + ") return exception.", e);
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
        }
        
        return result;
    }

}

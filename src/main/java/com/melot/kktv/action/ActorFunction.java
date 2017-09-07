package com.melot.kktv.action;

import com.antgroup.zmxy.openplatform.api.response.ZhimaCustomerCertificationInitializeResponse;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.game.config.sdk.utils.StringUtils;
import com.melot.kktv.redis.GiftRecordSource;
import com.melot.kktv.third.service.ZmxyService;
import com.melot.kktv.util.*;
import com.melot.sdk.core.util.MelotBeanFactory;
import com.melot.share.driver.domain.RankData;
import com.melot.share.driver.service.ShareActivityService;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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
     * 申请成为主播（接入芝麻认证-芝麻认证初始化接口）
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

        int bizCode, userId;
        String certName, certNo, returnUrl;
        try {
            bizCode = CommonUtil.getJsonParamInt(jsonObject, "bizCode", BizCodeEnum.FACE.getId(), null, 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
            certName = CommonUtil.getJsonParamString(jsonObject, "certName", "", null, 1, Integer.MAX_VALUE);
            certNo = CommonUtil.getJsonParamString(jsonObject, "certNo", "", null, 1, Integer.MAX_VALUE);
            returnUrl = CommonUtil.getJsonParamString(jsonObject, "returnUrl", "", null, 1, Integer.MAX_VALUE);
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

        // 芝麻认证
        try {
            ZhimaCustomerCertificationInitializeResponse response =  ZmxyService.getBizNo(userId, bizCode, certName, certNo);
            if(response.isSuccess()) {
                String bizNo = response.getBizNo();

                if(StringUtils.isEmpty(bizNo)) {
                    result.addProperty("TagCode", response.getErrorCode());
                    result.addProperty("errorMessage", response.getErrorMessage());
                    return result;
                } else if(bizCode == 1) {
                    String verifyUrl = ZmxyService.getUrl(bizNo, returnUrl);
                    result.addProperty("verifyUrl", verifyUrl);
                } else if(bizCode == 2) {
                    result.addProperty("appId", ZmxyService.APP_ID);
                }
                result.addProperty("bizNo", bizNo);
            }else {
                result.addProperty("TagCode", response.getErrorCode());
                result.addProperty("errorMessage", response.getErrorMessage());
                return result;
            }

            result.addProperty("TagCode", TagCodeEnum.SUCCESS);

            return result;

        } catch (Exception e) {
            logger.error("API Error ActorFunction.applyForActor userId:" + userId, e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

}

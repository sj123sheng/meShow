package com.melot.kkcx.functions;

import com.google.gson.JsonObject;
import com.melot.kk.wechatProject.api.Service.WechatCommonService;
import com.melot.kktv.service.GeneralService;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ParameterKeys;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.module.api.exceptions.MelotModuleException;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * Title: WechatProjectFunctions
 * <p>
 * Description:微信小程序
 * </p>
 *
 * @author <a href="mailto:jisfeng123@163.com">朱宝林</a>
 * @version V1.0.0
 * @since 2018/8/28 11:50
 */
public class WechatProjectFunctions {

    private static Logger log = Logger.getLogger(WechatProjectFunctions.class);

    @Resource
    private WechatCommonService wechatCommonService;

    /**
     * 51120301
     * 记录不同渠道推广记录
     */
    public JsonObject addPromoteHistByChannel(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        String appId;
        int channel;
        String ipAddr = null;
        try {
            appId = CommonUtil.getJsonParamString(jsonObject, "appId", null, TagCodeEnum.PARAMETER_PARSE_ERROR, 1, Integer.MAX_VALUE);
            channel = CommonUtil.getJsonParamInt(jsonObject, "channel", 0, TagCodeEnum.PARAMETER_PARSE_ERROR, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        String tagCode = TagCodeEnum.MODULE_UNKNOWN_RESPCODE;
        try {
            ipAddr = GeneralService.getIpAddr(request, 1, 1, null);
            wechatCommonService.addPromoteHistByChannel(ipAddr, channel, appId);
            tagCode = TagCodeEnum.SUCCESS;
        } catch(MelotModuleException e) {
            log.info(String.format("addPromoteHistByChannel fail: ipAddr=%s, channel=%s, addId=%s", ipAddr, channel, appId), e);
            if (e.getErrCode() == 101) {
                tagCode = TagCodeEnum.INVALID_PARAMETERS;
            }
        } catch (Exception e) {
            log.error(String.format("addPromoteHistByChannel error: ipAddr=%s, channel=%s, addId=%s)", ipAddr, channel, appId), e);
        }
        result.addProperty(ParameterKeys.TAG_CODE, tagCode);
        return result;
    }

    /**
     * 51120302
     * 获取accessToken
     */
    public JsonObject getAccessToken(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        String appId;
        String appSecret;
        try {
            appId = CommonUtil.getJsonParamString(jsonObject, "appId", null, TagCodeEnum.PARAMETER_PARSE_ERROR, 1, Integer.MAX_VALUE);
            appSecret = CommonUtil.getJsonParamString(jsonObject, "appSecret", null, TagCodeEnum.PARAMETER_PARSE_ERROR, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        String tagCode = TagCodeEnum.MODULE_UNKNOWN_RESPCODE;
        try {
            String accessToken = wechatCommonService.getAccessToken(appId, appSecret);
            if (accessToken != null) {
                result.addProperty("accessToken", accessToken);
                tagCode = TagCodeEnum.SUCCESS;
            }
        } catch (Exception e) {
            log.error(String.format("getAccessToken error: appId=%s, appSecret=%s)", appId, appSecret), e);
        }
        result.addProperty(ParameterKeys.TAG_CODE, tagCode);
        return result;
    }

    /**
     * 51120303
     * 上传用户的formId
     */
    public JsonObject uploadFormId(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        String openId;
        String formId;
        try {
            openId = CommonUtil.getJsonParamString(jsonObject, "openId", null, TagCodeEnum.PARAMETER_PARSE_ERROR, 1, Integer.MAX_VALUE);
            formId = CommonUtil.getJsonParamString(jsonObject, "formId", null, TagCodeEnum.PARAMETER_PARSE_ERROR, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        String tagCode = TagCodeEnum.MODULE_UNKNOWN_RESPCODE;
        try {
            if (wechatCommonService.addFormId(openId, formId)) {
                tagCode = TagCodeEnum.SUCCESS;
            } else {
                tagCode = "5112030301";
            }
        } catch (Exception e) {
            log.error(String.format("uploadFormId error: openId=%s, formId=%s)", openId, formId), e);
        }
        result.addProperty(ParameterKeys.TAG_CODE, tagCode);
        return result;
    }

    /**
     * 51120304
     * 上传用户信息
     */
    public JsonObject uploadUserInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }

        int userId;
        String appId;
        String openId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.PARAMETER_PARSE_ERROR, 1, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamString(jsonObject, "appId", null, TagCodeEnum.PARAMETER_PARSE_ERROR, 1, Integer.MAX_VALUE);
            openId = CommonUtil.getJsonParamString(jsonObject, "openId", null, TagCodeEnum.PARAMETER_PARSE_ERROR, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        String tagCode = TagCodeEnum.MODULE_UNKNOWN_RESPCODE;
        try {
            if (wechatCommonService.addUserInfo(userId, appId, openId)) {
                tagCode = TagCodeEnum.SUCCESS;
            } else {
                tagCode = "5112030401";
            }
        } catch (Exception e) {
            log.error(String.format("uploadFormId error: userId=%s, appId=%s, openId=%s)", userId, appId, openId), e);
        }
        result.addProperty(ParameterKeys.TAG_CODE, tagCode);
        return result;
    }
}

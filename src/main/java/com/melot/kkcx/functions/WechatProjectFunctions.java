package com.melot.kkcx.functions;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.melot.common.melot_utils.StringUtils;
import com.melot.kk.wechatProject.api.service.WechatCommonService;
import com.melot.kktv.service.ConfigService;
import com.melot.kktv.service.GeneralService;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.ParameterKeys;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.kktv.util.cache.EhCache;
import com.melot.module.api.exceptions.MelotModuleException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static com.melot.kktv.util.ParamCodeEnum.PROJECT_ID;
import static com.melot.kktv.util.ParamCodeEnum.ROOM_ID;


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

    @Autowired
    private ConfigService configService;

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
        String appType;
        String openId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.INVALID_PARAMETERS, 1, Integer.MAX_VALUE);
            appType = CommonUtil.getJsonParamString(jsonObject, "appType", null, TagCodeEnum.INVALID_PARAMETERS, 1, 512);
            openId = CommonUtil.getJsonParamString(jsonObject, "openId", null, TagCodeEnum.INVALID_PARAMETERS, 1, 512);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.INVALID_PARAMETERS);
            return result;
        }


        String tagCode = TagCodeEnum.MODULE_UNKNOWN_RESPCODE;
        try {
            Map<String, String> appIds = new Gson().fromJson(configService.getWechatProjectAppIds(), new TypeToken<java.util.Map<String, String>>() {}.getType());
            String appId = appIds.get(appType);
            if (appId == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.INVALID_PARAMETERS);
                return result;
            }
            if (EhCache.getFromCache(String.format("wx_openId_%s_%s", userId, appId)) == null) {
                tagCode = wechatCommonService.addUserInfo(userId, openId, appId) ? TagCodeEnum.SUCCESS : "5112030401";
            }
        } catch (Exception e) {
            log.error(String.format("uploadFormId error: userId=%s, appType=%s, openId=%s)", userId, appType, openId), e);
        }
        result.addProperty(ParameterKeys.TAG_CODE, tagCode);
        return result;
    }

    /**
     * 51120305
     * 获取直播购小程序分享直播间二维码url
     */
    public JsonObject getLiveShopShareRoomUrl(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        int roomId;
        try {
            roomId = CommonUtil.getJsonParamInt(jsonObject, ROOM_ID.getId(), 0, ROOM_ID.getErrorCode(), 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        }


        try {
            String shopProjectShareUrl = wechatCommonService.getLiveShopProjectShareUrl(roomId);
            if(StringUtils.isNotEmpty(shopProjectShareUrl)) {
                String urlPath = ConfigHelper.getHttpdir();
                urlPath = urlPath.substring(0, urlPath.lastIndexOf("kktv"));
                result.addProperty("shareUrl", urlPath + shopProjectShareUrl);
            }
        } catch (Exception e) {
            log.error(String.format("getLiveShopShareRoomUrl error: roomId=%s)", roomId), e);
        }
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 51120306
     * 获取小程序分享二维码
     */
    public JsonObject getProjectShareUrl(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        int projectId, width;
        boolean isHyaline = false;
        String scene, page;
        try {
            projectId = CommonUtil.getJsonParamInt(jsonObject, PROJECT_ID.getId(), 0, PROJECT_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            width = CommonUtil.getJsonParamInt(jsonObject, "width", 430, null, 280, 1280);
            JsonElement booleanJe = jsonObject.get("isHyaline");
            if (booleanJe != null && !booleanJe.isJsonNull()) {
                isHyaline = booleanJe.getAsBoolean();
            }
            scene = CommonUtil.getJsonParamString(jsonObject, "scene", null, null, 0, 32);
            page = CommonUtil.getJsonParamString(jsonObject, "page", null, null, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        }


        try {
            String projectShareUrl = wechatCommonService.getProjectShareUrl(projectId, scene, page, width, isHyaline);
            if(StringUtils.isNotEmpty(projectShareUrl)) {
                String urlPath = ConfigHelper.getHttpdir();
                urlPath = urlPath.substring(0, urlPath.lastIndexOf("kktv"));
                result.addProperty("shareUrl", urlPath + projectShareUrl);
            }
        } catch (Exception e) {
            log.error(String.format("getProjectShareUrl error: projectId=%s，scene=%s，page=%s，width=%s，isHyaline=%s)", projectId, scene, page, width, isHyaline), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

}
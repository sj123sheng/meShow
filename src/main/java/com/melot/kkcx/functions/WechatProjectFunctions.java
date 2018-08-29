package com.melot.kkcx.functions;

import com.google.gson.JsonObject;
import com.melot.kk.wechatProject.api.Service.WechatPromoteService;
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
    private WechatPromoteService wechatPromoteService;

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
            wechatPromoteService.addPromoteHistByChannel(ipAddr, channel, appId);
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
}

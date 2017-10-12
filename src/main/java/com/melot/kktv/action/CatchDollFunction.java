package com.melot.kktv.action;

import com.google.gson.JsonObject;
import com.melot.common.driver.base.Result;
import com.melot.common.driver.base.ResultCode;
import com.melot.common.driver.domain.AgoraInfo;
import com.melot.common.driver.service.ConfigInfoService;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.sdk.core.util.MelotBeanFactory;
import com.melot.stream.driver.service.LiveStreamConfigService;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;

/**
 * Title: CatchDollFunction
 * <p>
 * Description:直播抓娃娃相关接口
 * </p>
 * 
 * @author shengjian
 * @version V1.0
 * @since 2017年10月12日
 */
public class CatchDollFunction {

    private static Logger logger = Logger.getLogger(CatchDollFunction.class);
    
    private static final String KEY = "YdsSH&@#Uyh";

    /**
     * 51060201
     * 监听直播精灵是否正常推流(直播精灵定时发送心跳请求)
     */
    public JsonObject monitorPushFlow(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        int dollMachineId, pushFlowStatus;
        String sign;
        try {
            dollMachineId = CommonUtil.getJsonParamInt(jsonObject, "dollMachineId", 0, null, 1, Integer.MAX_VALUE);
            pushFlowStatus = CommonUtil.getJsonParamInt(jsonObject, "pushFlowStatus", 1, null, 1, Integer.MAX_VALUE);
            sign = CommonUtil.getJsonParamString(jsonObject, "sign", null, "05110101", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        if(dollMachineId == 0) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_MISSING);
            return result;
        }

        // 校验参数
        if (!checkSign(dollMachineId, pushFlowStatus, sign)) {
            result.addProperty("TagCode", "5110901");
            return result;
        }
        
        //添加返回信息
        result.addProperty("result", true);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 获取娃娃机直播间信息【51060202】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getDollMachineRoomInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        int dollMachineId;
        String sign;
        try {
            dollMachineId = CommonUtil.getJsonParamInt(jsonObject, "dollMachineId", 0, null, 1, Integer.MAX_VALUE);
            sign = CommonUtil.getJsonParamString(jsonObject, "sign", null, "05110101", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        if(dollMachineId == 0) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_MISSING);
            return result;
        }

        // 校验参数
        if (!checkSign(dollMachineId, null, sign)) {
            result.addProperty("TagCode", "5110901");
            return result;
        }

        try {

            int roomId = 10002720;
            ConfigInfoService configInfoService = MelotBeanFactory.getBean("configInfoService", ConfigInfoService.class);
            Result<AgoraInfo> agoraInfoResult = configInfoService.getAgoraInfo(roomId, 16);
            if (agoraInfoResult == null || agoraInfoResult.getCode() == null) {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            if (ResultCode.SUCCESS.equals(agoraInfoResult.getCode())) {
                AgoraInfo info = agoraInfoResult.getData();
                result.addProperty("appId", info.getAppId());
                result.addProperty("channelId", info.getChannelId());
            }else {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }

            result.addProperty("roomId", roomId);
            result.addProperty("primaryCameraId", roomId+1);
            result.addProperty("secondaryCameraId", roomId+2);
            LiveStreamConfigService liveStreamConfigService = (LiveStreamConfigService) MelotBeanFactory.getBean("liveStreamConfigService");
            result.addProperty("pushFlowUrl", "rtmp://push.kktv8.com/livekktv");
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Module Error ConfigInfoService.getAgoraInfo()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }

    /**
     * 校验参数
     * @return
     */
    private static boolean checkSign(int dollMachineId, Integer pushFlowStatus, String sign) {
        StringBuilder builder = new StringBuilder();
        builder.append(KEY);
        builder.append("dollMachineId=");
        builder.append(dollMachineId);
        if(pushFlowStatus != null) {
            builder.append("&pushFlowStatus=");
            builder.append(pushFlowStatus);
        }
        builder.append(KEY);
        
        String param = builder.toString();
        String signTemp = CommonUtil.md5(param);
        if (signTemp.equals(sign)) {
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        StringBuilder builder = new StringBuilder();
        builder.append(KEY);
        builder.append("dollMachineId=");
        builder.append(1);

        /*builder.append("&pushFlowStatus=");
        builder.append(1);*/

        builder.append(KEY);

        String param = builder.toString();
        String signTemp = CommonUtil.md5(param);
        System.out.println(signTemp);
        System.out.println(checkSign(1, null,signTemp));
    }
}

package com.melot.kktv.action;

import com.google.gson.JsonObject;
import com.melot.common.driver.base.ResultCode;
import com.melot.common.driver.domain.AgoraInfo;
import com.melot.common.driver.service.ConfigInfoService;
import com.melot.kk.doll.api.domain.DO.DollMachineDO;
import com.melot.kk.doll.api.domain.DO.RedisDollMachineDO;
import com.melot.kk.doll.api.domain.DO.StartGameDO;
import com.melot.kk.doll.api.service.DollMachineService;
import com.melot.kktv.base.CommonStateCode;
import com.melot.kktv.base.Result;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.sdk.core.util.MelotBeanFactory;
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
        if (!checkSign(dollMachineId, 0 , pushFlowStatus, sign)) {
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

        DollMachineService dollMachineService = (DollMachineService) MelotBeanFactory.getBean("dollMachineService");

        int dollMachineId, roomId;
        String sign;
        try {
            dollMachineId = CommonUtil.getJsonParamInt(jsonObject, "dollMachineId", 0, null, 1, Integer.MAX_VALUE);
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, null, 1, Integer.MAX_VALUE);
            sign = CommonUtil.getJsonParamString(jsonObject, "sign", null, "05110101", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        if(dollMachineId == 0 && roomId == 0) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_MISSING);
            return result;
        }

        // 校验参数
        if (!checkSign(dollMachineId, roomId, null, sign)) {
            result.addProperty("TagCode", "5110901");
            return result;
        }

        try {

            Result<DollMachineDO> dollMachineDOResult;
            DollMachineDO dollMachineDO;
            if(dollMachineId > 0) {
                dollMachineDOResult = dollMachineService.getDollMachineDOByDollMachineId(dollMachineId);
            }else {
                dollMachineDOResult = dollMachineService.getDollMachineDOByRoomId(roomId);
            }
            if(dollMachineDOResult.getCode().equals(CommonStateCode.SUCCESS) && dollMachineDOResult.getData() != null) {
                dollMachineDO = dollMachineDOResult.getData();
            }else {
                result.addProperty("TagCode", "5110902");
                return result;
            }

            if(roomId == 0)
                roomId = dollMachineDO.getRoomId();

            ConfigInfoService configInfoService = MelotBeanFactory.getBean("configInfoService", ConfigInfoService.class);
            com.melot.common.driver.base.Result<AgoraInfo> agoraInfoResult = configInfoService.getAgoraInfo(roomId, 16);
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
            result.addProperty("primaryCameraId", dollMachineDO.getPrimaryCameraId());
            result.addProperty("secondaryCameraId", dollMachineDO.getSecondaryCameraId());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Module Error ConfigInfoService.getAgoraInfo()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }

    /**
     * 获取娃娃机直播间详情【51060203】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getDollMachineRoomDetail(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        DollMachineService dollMachineService = (DollMachineService) MelotBeanFactory.getBean("dollMachineService");

        int roomId;
        try {
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        if(roomId == 0) {
            result.addProperty("TagCode", TagCodeEnum.USERID_MISSING);
            return result;
        }

        try {

            Result<DollMachineDO> dollMachineDOResult = dollMachineService.getDollMachineDOByRoomId(roomId);
            DollMachineDO dollMachineDO;
            if(dollMachineDOResult.getCode().equals(CommonStateCode.SUCCESS)  && dollMachineDOResult.getData() != null) {
                dollMachineDO = dollMachineDOResult.getData();
            }else {
                result.addProperty("TagCode", "5110902");
                return result;
            }

            Result<RedisDollMachineDO> redisDollMachineDOResult = dollMachineService.getRedisDollMachineDO(roomId);
            RedisDollMachineDO redisDollMachineDO;
            if(redisDollMachineDOResult.getCode().equals(CommonStateCode.SUCCESS)) {
                redisDollMachineDO = redisDollMachineDOResult.getData();
            }else {
                result.addProperty("TagCode", "5110903");
                return result;
            }

            Integer dollMachineStatus = redisDollMachineDO.getStatus();
            if(dollMachineStatus == null)
                dollMachineStatus = dollMachineDO.getStatus();

            result.addProperty("dollMachineId", dollMachineDO.getDollMachineId());
            result.addProperty("dollMachineStatus", dollMachineStatus);
            result.addProperty("dollDesc", dollMachineDO.getDollDesc());
            result.addProperty("dollPictureUrl", dollMachineDO.getDollPictureUrl());
            result.addProperty("price", dollMachineDO.getPrice());
            result.addProperty("gameTime", dollMachineDO.getGameTime());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Module Error ConfigInfoService.getAgoraInfo()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }

    /**
     * 开始游戏【51060211】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject play(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        DollMachineService dollMachineService = (DollMachineService) MelotBeanFactory.getBean("dollMachineService");

        int roomId, userId;
        try {
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, null, 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        if(roomId == 0 || userId == 0) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_MISSING);
            return result;
        }

        try {

            Result<StartGameDO> startGameDOResult = dollMachineService.startGame(roomId, userId);
            StartGameDO startGameDO = new StartGameDO();
            if(startGameDOResult.getCode().equals(CommonStateCode.SUCCESS) && startGameDOResult.getData() != null) {
                startGameDO = startGameDOResult.getData();
            }else if(startGameDOResult.getData() != null){
                result.addProperty("TagCode", startGameDOResult.getData().getTagCode());
                return result;
            }

            result.addProperty("wsUrl", startGameDO.getWsUrl());
            result.addProperty("catchDollRecordId", startGameDO.getCatchDollRecordId());
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
    private static boolean checkSign(int dollMachineId, int roomId, Integer pushFlowStatus, String sign) {
        StringBuilder builder = new StringBuilder();
        builder.append(KEY);
        if(dollMachineId == 0) {
            builder.append("roomId=");
            builder.append(roomId);
        }else {
            builder.append("dollMachineId=");
            builder.append(dollMachineId);
            if (pushFlowStatus != null) {
                builder.append("&pushFlowStatus=");
                builder.append(pushFlowStatus);
            }
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
        //builder.append("roomId=");
        //builder.append(10002534);
        builder.append("dollMachineId=");
        builder.append(1076);
        /*builder.append("&pushFlowStatus=");
        builder.append(1);*/

        builder.append(KEY);

        String param = builder.toString();
        String signTemp = CommonUtil.md5(param);
        System.out.println(signTemp);
        //System.out.println(checkSign(0, 10002534,null,signTemp));
    }
}

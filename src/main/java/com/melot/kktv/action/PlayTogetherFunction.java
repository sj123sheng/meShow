package com.melot.kktv.action;

import com.google.gson.*;
import com.melot.kk.doll.api.constant.DollMachineStatusEnum;
import com.melot.kk.doll.api.domain.DO.RedisDollMachineDO;
import com.melot.kk.doll.api.service.DollMachineService;
import com.melot.kk.hall.api.domain.HallPartConfDTO;
import com.melot.kk.hall.api.domain.HallRoomInfoDTO;
import com.melot.kk.hall.api.service.SysMenuService;
import com.melot.kkcx.transform.HallRoomTF;
import com.melot.kkcx.util.ResultUtils;
import com.melot.kktv.base.CommonStateCode;
import com.melot.kktv.base.Result;
import com.melot.kktv.service.ConfigService;
import com.melot.kktv.util.*;
import com.melot.sdk.core.util.MelotBeanFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Title: PlayTogetherFunction
 * <p>
 * Description:一起玩大厅相关接口
 * </p>
 * 
 * @author shengjian
 * @version V1.0
 * @since 2017年10月12日
 */
public class PlayTogetherFunction {

    private static Logger logger = Logger.getLogger(PlayTogetherFunction.class);

    @Autowired
    private ConfigService configService;

    @Resource
    private SysMenuService hallPartService;

    private static String REGEX = ",";

    /**
     * 获取一起玩大厅栏目列表【51070201】
     */
    public JsonObject getTitleList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();
        int channel, platform;

        try {
            // 渠道号
            channel = CommonUtil.getJsonParamInt(jsonObject, "c", AppChannelEnum.KK, null, 0, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 1, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        try {

            String playTogetherConfig = null;
            if (platform == PlatformEnum.IPHONE) {
                playTogetherConfig = configService.getPlayIOSTogetherConfig();
            } else {
                playTogetherConfig = configService.getPlayTogetherConfig();
            }
            JsonArray roomArray = new JsonParser().parse(playTogetherConfig).getAsJsonArray();
            
            boolean isLimit = false;
            //渠道版本号限制
            String specifyChannel = configService.getSpecifyChannel().trim();
            if (!StringUtil.strIsNull(specifyChannel)) {
                String[] channels = specifyChannel.split(REGEX);
                if (channels != null && channels.length > 0) {
                    for (String speicalChannel : channels) {
                        if (Integer.valueOf(speicalChannel) == channel) {
                            isLimit = true;
                            break;
                        }
                    }
                }
            }
            if(isLimit) {
                String playTogetherSpecialChannelConfig = configService.getPlayTogetherSpecialChannelConfig();
                roomArray = new JsonParser().parse(playTogetherSpecialChannelConfig).getAsJsonArray();
            }

            result.add("cataList", roomArray);
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error getTitleList()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }

    /**
     * 获取抓娃娃栏目下的房间列表【51070202】
     */
    public JsonObject getCatchDollRoomList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        int cataId, platform, pageIndex, countPerPage;
        int appId;
        try {
            cataId = CommonUtil.getJsonParamInt(jsonObject, "cataId", 0, null, 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, TagCodeEnum.PLATFORM_MISSING, 1, Integer.MAX_VALUE);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 0, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 10, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {

            DollMachineService dollMachineService = (DollMachineService) MelotBeanFactory.getBean("dollMachineService");

            int start = (pageIndex <= 1 ? 0 : pageIndex - 1) * countPerPage;
            Result<HallPartConfDTO> partListResult = hallPartService.getPartList(cataId, 0, 0, 0, appId, start, countPerPage);
            JsonArray roomArray = new JsonArray();
            int roomTotal = 0;
            if (ResultUtils.checkResultNotNull(partListResult)) {
                HallPartConfDTO hallPartConfDTO = partListResult.getData();
                List<HallRoomInfoDTO> roomList = hallPartConfDTO.getRooms();
                if (roomList != null) {
                    for (HallRoomInfoDTO roomInfo : roomList) {
                        JsonObject roomObject = HallRoomTF.roomInfoToJson(roomInfo, platform);

                        // 获取直播间娃娃机的状态和直播间抓中娃娃的总数
                        int roomId = roomInfo.getActorId();
                        Result<RedisDollMachineDO> redisDollMachineDOResult = dollMachineService.getRedisDollMachineDO(roomId);
                        RedisDollMachineDO redisDollMachineDO;
                        if(redisDollMachineDOResult.getCode().equals(CommonStateCode.SUCCESS)) {
                            redisDollMachineDO = redisDollMachineDOResult.getData();
                        }else {
                            result.addProperty("TagCode", "5110903");
                            return result;
                        }
                        Integer dollMachineStatus = redisDollMachineDO.getStatus();
                        if(dollMachineStatus == null) {
                            dollMachineStatus = DollMachineStatusEnum.READY;
                        }else if(dollMachineStatus == DollMachineStatusEnum.WAIT_COIN) {
                            dollMachineStatus = DollMachineStatusEnum.PLAY;
                        }
                        roomObject.addProperty("dollMachineStatus", dollMachineStatus);
                        roomObject.addProperty("graspCount", redisDollMachineDO.getGraspDollCount());

                        roomArray.add(roomObject);
                    }
                }
                roomTotal = hallPartConfDTO.getRoomCount().intValue();
            }
            result.addProperty("roomTotal", roomTotal);
            result.add("roomList", roomArray);
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error getCatchDollRoomList()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }

}

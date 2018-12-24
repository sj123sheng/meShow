package com.melot.kkcx.functions;


import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.kk.hall.api.constant.QueryHallRoomInfoParam;
import com.melot.kk.hall.api.domain.HallPartConfDTO;
import com.melot.kk.hall.api.domain.HallRoomInfoDTO;
import com.melot.kk.hall.api.service.SysMenuService;
import com.melot.kk.liveshop.api.constant.LiveShopErrorMsg;
import com.melot.kk.liveshop.api.dto.ActivityConfigDTO;
import com.melot.kk.liveshop.api.dto.LiveShopPrizeDTO;
import com.melot.kk.liveshop.api.service.NewYearActivityService;
import com.melot.kkcx.transform.LiveShopActivityTF;
import com.melot.kktv.base.CommonStateCode;
import com.melot.kktv.base.Result;
import com.melot.kktv.service.ConfigService;
import com.melot.kktv.util.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LiveShopActivityFunctions {

    @Resource
    private SysMenuService hallPartService;

    @Resource
    NewYearActivityService newYearActivityService;

    @Autowired
    ConfigService configService;

    private static final String SPECIAL_REGION_ONE = "special_region_one";

    private static final String SPECIAL_REGION_TWO = "special_region_two";

    private static final String SPECIAL_REGION_THREE = "special_region_three";

    private static Logger logger = Logger.getLogger(LiveShopActivityFunctions.class);

    /**
     * 获取活动配置信息【51051001】
     */
    public JsonObject getActivityConfInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        try {

            ActivityConfigDTO activityConfigDTO = newYearActivityService.getActivityConfig();
            result.addProperty("activityStatus", activityConfigDTO.getActivityStatus());
            result.addProperty("activityPath", activityConfigDTO.getActivityPath());
            result.addProperty("activityIcon", activityConfigDTO.getActivityIcon());
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error getActivityConfInfo()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取首页是否显示抽奖弹框【51051002】
     */
    public JsonObject showActivityEntrance(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, null, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {

            boolean showActivityEntrance = newYearActivityService.getShowActivityEntrance(userId);
            result.addProperty("showActivityEntrance", showActivityEntrance);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error showActivityEntrance()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取跨年活动商家直播间列表【51051003】
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getActivitySellerList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();
        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, null, 1, Integer.MAX_VALUE);
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
//        为今天已经登录的用户增加今天的抽奖机会
        newYearActivityService.loginAddLotteryChance(userId);
        String cataIds = configService.getLiveShopActivityCataIds();
        JsonObject cataIdsJson = new JsonParser().parse(cataIds).getAsJsonObject();
        int cataIdSpcialRegionOne = cataIdsJson.get("specialRegionOne").getAsInt();
        int cataIdSpcialRegionTwo = cataIdsJson.get("specialRegionTwo").getAsInt();
        int cataIdSpcialRegionThree = cataIdsJson.get("specialRegionThree").getAsInt();
        int cataIdSpcialRegionFour = cataIdsJson.get("specialRegionFour").getAsInt();

        HallPartConfDTO hallPartConfDTOOne = null;
        HallPartConfDTO hallPartConfDTOTwo = null;
        HallPartConfDTO hallPartConfDTOThree = null;
        HallPartConfDTO hallPartConfDTOFour;

        if (specialRegionIfShow(SPECIAL_REGION_ONE)) {
            hallPartConfDTOOne = getActivityCatalogDTOByCataId(cataIdSpcialRegionOne, true, 6, null);
        }
        if (specialRegionIfShow(SPECIAL_REGION_TWO)) {
            hallPartConfDTOTwo = getActivityCatalogDTOByCataId(cataIdSpcialRegionTwo, true, 6, null);
        }
        if (specialRegionIfShow(SPECIAL_REGION_THREE)) {
            List<HallRoomInfoDTO> hallRoomInfoDTOListTwo = null;
            if (hallPartConfDTOTwo != null) {
                hallRoomInfoDTOListTwo = hallPartConfDTOTwo.getRooms();
            }
            hallPartConfDTOThree = getActivityCatalogDTOByCataId(cataIdSpcialRegionThree, true, 12, hallRoomInfoDTOListTwo);

        }

        List<HallRoomInfoDTO> repeatList = new ArrayList<>();
        if (hallPartConfDTOOne != null && hallPartConfDTOOne.getRooms() != null) {
            repeatList.addAll(hallPartConfDTOOne.getRooms());
        }
        if (hallPartConfDTOTwo != null && hallPartConfDTOTwo.getRooms() != null) {
            repeatList.addAll(hallPartConfDTOTwo.getRooms());
        }
        if (hallPartConfDTOThree != null && hallPartConfDTOThree.getRooms() != null) {
            repeatList.addAll(hallPartConfDTOThree.getRooms());
        }
        hallPartConfDTOFour = getActivityCatalogDTOByCataId(cataIdSpcialRegionFour, false, null, repeatList);

        JsonObject specialRegionOne = LiveShopActivityTF.hallPartConfDTO2Json(hallPartConfDTOOne);
        JsonObject specialRegionTwo = LiveShopActivityTF.hallPartConfDTO2Json(hallPartConfDTOTwo);
        JsonObject specialRegionThree = LiveShopActivityTF.hallPartConfDTO2Json(hallPartConfDTOThree);
        JsonObject specialRegionFour = LiveShopActivityTF.hallPartConfDTO2Json(hallPartConfDTOFour);
        result.add("hallPartConfDTOOne", specialRegionOne);
        result.add("hallPartConfDTOTwo", specialRegionTwo);
        result.add("hallPartConfDTOThree", specialRegionThree);
        result.add("hallPartConfDTOFour", specialRegionFour);
        result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
        result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 抽奖【51051004】
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject lottery(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }

        int userId;
        int shareUserId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, "53000001", 1, Integer.MAX_VALUE);
            shareUserId = CommonUtil.getJsonParamInt(jsonObject, "shareUserId", 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            Result<LiveShopPrizeDTO> lotteryResult = newYearActivityService.lottery(userId, shareUserId);
            if (lotteryResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            String codeResult = lotteryResult.getCode();
            if (!LiveShopErrorMsg.SUCCESS_CODE.equals(codeResult)) {
                result.addProperty(ParameterKeys.TAG_CODE, codeResult);
                return result;
            }
            LiveShopActivityTF.liveShopPrizeDTO2Json(result, lotteryResult.getData());
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：newYearActivityService.lottery(userId=%s, shareUserId=%s)", userId, shareUserId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * @param cataId     栏目id
     * @param ifNeedLive 是否需要开播
     * @param num        需要的数目(null表示全部)
     * @param repeatList 去重列表（去掉与该列表重复的部分，null表示不做去重）
     * @return 栏目DTO
     */
    private HallPartConfDTO getActivityCatalogDTOByCataId(Integer cataId, boolean ifNeedLive, Integer num, List<HallRoomInfoDTO> repeatList) {
        QueryHallRoomInfoParam params = new QueryHallRoomInfoParam();
        params.setCataId(cataId);
        params.setNum(num);
        Result<HallPartConfDTO> hallPartConfDTOResult = hallPartService.queryRoomList(params);
        HallPartConfDTO hallPartConfDTO = null;
        if (hallPartConfDTOResult != null && CommonStateCode.SUCCESS.equals(hallPartConfDTOResult.getCode())) {
            hallPartConfDTO = hallPartConfDTOResult.getData();
            if (num == null && hallPartConfDTO != null) {
                params.setNum(hallPartConfDTO.getRoomCount());
                hallPartConfDTOResult = hallPartService.queryRoomList(params);
                if (hallPartConfDTOResult != null && CommonStateCode.SUCCESS.equals(hallPartConfDTOResult.getCode())) {
                    hallPartConfDTO = hallPartConfDTOResult.getData();
                }
            }
        }
        if (hallPartConfDTO == null) {

            return null;
        }
        List<HallRoomInfoDTO> hallRoomInfoDTOList = hallPartConfDTO.getRooms();
        if (hallRoomInfoDTOList == null) {
            return hallPartConfDTO;
        }
        List<HallRoomInfoDTO> hallRoomInfoDTOListFinal = new ArrayList<>();

        if (repeatList != null) {
            hallRoomInfoDTOList.removeAll(repeatList);
        }

        for (int i = 0; i < hallRoomInfoDTOList.size(); i++) {
            if (num != null && i >= num) {
                break;
            }
            HallRoomInfoDTO hallRoomInfoDTO = hallRoomInfoDTOList.get(i);
            if (ifNeedLive && hallRoomInfoDTO.getLiveType() == 0) {
                break;
            }
            hallRoomInfoDTOListFinal.add(hallRoomInfoDTO);
        }
        hallPartConfDTO.setRooms(hallRoomInfoDTOListFinal);
        return hallPartConfDTO;
    }

    /**
     * 专区是否显示
     *
     * @param specialRegion 专区
     * @return true:显示 false:不显示
     */
    private boolean specialRegionIfShow(String specialRegion) {
        String showTime = configService.getLiveShopActivityCataShowTime();
        JsonObject cataIdsJson = new JsonParser().parse(showTime).getAsJsonObject();
        int time1 = cataIdsJson.get("time1").getAsInt();
        int time2 = cataIdsJson.get("time2").getAsInt();
        int hour = DateUtils.getHour(new Date());
        logger.info(String.format("time1=%s,time2=%s,hour=%s", time1, time2, hour));
        if (SPECIAL_REGION_ONE.equals(specialRegion)) {
            return hour >= time1 && hour < time2;
        } else if (SPECIAL_REGION_TWO.equals(specialRegion) || SPECIAL_REGION_THREE.equals(specialRegion)) {
            return hour >= time2;
        }
        return true;
    }
}
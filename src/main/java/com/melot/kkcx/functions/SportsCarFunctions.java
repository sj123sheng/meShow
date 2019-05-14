package com.melot.kkcx.functions;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.kk.game.sportscar.api.dto.SoldResultDTO;
import com.melot.kk.game.sportscar.api.dto.SportsCarConfigDTO;
import com.melot.kk.game.sportscar.api.dto.UpgradeResultDTO;
import com.melot.kk.game.sportscar.api.service.GameSportsCarService;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ParameterKeys;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.kktv.util.confdynamic.GiftInfoConfig;
import com.melot.module.api.exceptions.MelotModuleException;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Title: 跑车世界接口
 * <p>
 * Description:
 * </p>
 *
 * @author 王贺<a href="mailto:he.wang@melot.cn"/>
 * @version V1.0
 * @since 2019/04/15.
 */
public class SportsCarFunctions {

    private static Logger logger = Logger.getLogger(SportsCarFunctions.class);

    @Resource
    GameSportsCarService gameSportsCarService;

    @Resource
    KkUserService kkUserService;
    /**
     * 获取跑车世界配置(51140201)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getSportsCarConfig(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        List<SportsCarConfigDTO> sportsCarConfigs = gameSportsCarService.getSportsCarConfigs();

        JsonArray jsonArray = new JsonArray();
        if (sportsCarConfigs != null) {
            for (SportsCarConfigDTO configDTO : sportsCarConfigs) {
                JsonObject object = new JsonObject();
                object.addProperty("carLevel", configDTO.getCarLevel());
                object.addProperty("carName", configDTO.getCarName());
                object.addProperty("upgradeCost", configDTO.getUpgradeCost());
                object.addProperty("rewardGiftCount", configDTO.getRewardGiftCount());
                object.addProperty("rewardGiftName", GiftInfoConfig.getGiftName(configDTO.getRewardGiftId()));
                object.addProperty("upgradeRate", configDTO.getUpgradeRate());
                jsonArray.add(object);
            }
        }
        result.add("configList", jsonArray);
        result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);

        return result;
    }

    /**
     * 获取用户所处跑车阶段(51140202)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getSportsCarUserStage(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        int userId;

        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, TagCodeEnum.USERID_MISSING, 100, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        UserProfile userProfile = kkUserService.getUserProfile(userId);
        if (userProfile == null) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.USER_NOT_EXIST);
            return result;
        }

        result.addProperty("stage", gameSportsCarService.getSportsCarUserStage(userId));
        result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);

        return result;
    }

    /**
     * 升级跑车(51140203)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject upgradeSportsCar(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId, platform;

        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, TagCodeEnum.USERID_MISSING, 100, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.PLATFORM, 0, TagCodeEnum.PLATFORM_MISSING, 1, 4);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            UpgradeResultDTO upgradeResultDTO = gameSportsCarService.upgradeSportsCar(userId, platform);

            result.addProperty("stage", upgradeResultDTO.getStage());
            result.addProperty("happyTicket", upgradeResultDTO.getHappyTicket());
        } catch (MelotModuleException e) {
            result.addProperty(ParameterKeys.TAG_CODE, "51140203" + e.getErrCode());
            return result;
        }

        result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 出售跑车(51140204)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject soldSportsCar(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId, platform;

        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, TagCodeEnum.USERID_MISSING, 100, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.PLATFORM, 0, TagCodeEnum.PLATFORM_MISSING, 1, 4);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            SoldResultDTO soldResultDTO = gameSportsCarService.soldSportsCar(userId, platform);

            result.addProperty("giftId", soldResultDTO.getGiftId());
            result.addProperty("giftName", GiftInfoConfig.getGiftName(soldResultDTO.getGiftId()));
            result.addProperty("giftCount", soldResultDTO.getGiftCount());
            result.addProperty("stage", soldResultDTO.getStage());
        } catch (MelotModuleException e) {
            result.addProperty(ParameterKeys.TAG_CODE, "51140204" + e.getErrCode());
            return result;
        }

        result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
        return result;
    }
}

package com.melot.kktv.action;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.common.melot_utils.StringUtils;
import com.melot.kk.location.api.dto.AddressComponentDTO;
import com.melot.kk.location.api.dto.AreaCodeDTO;
import com.melot.kk.location.api.service.LocationService;
import com.melot.kk.town.api.param.TownUserInfoParam;
import com.melot.kk.town.api.service.TownUserService;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.TagCodeEnum;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.melot.kktv.util.ParamCodeEnum.*;

public class LocationFunctions {

    private static Logger logger = Logger.getLogger(LocationFunctions.class);

    @Resource
    LocationService locationService;

    @Resource
    TownUserService townUserService;

    /**
     * 	根据gps经纬度获取对应的地理位置信息【51120101】
     */
    public JsonObject getAddressComponent(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        int userId;
        String lat,lng, token;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, USER_ID.getId(), 0, null, 1, Integer.MAX_VALUE);
            lat = CommonUtil.getJsonParamString(jsonObject, LAT.getId(), null, LAT.getErrorCode(), 1, Integer.MAX_VALUE);
            lng = CommonUtil.getJsonParamString(jsonObject, LNG.getId(), null, LNG.getErrorCode(), 1, Integer.MAX_VALUE);
            token = CommonUtil.getJsonParamString(jsonObject, "token", null, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        boolean isOwner = false;
        // 查询本人作品列表需要验证token,未验证的返回错误码
        if(StringUtils.isNotEmpty(token)) {
            if (!checkTag) {
                result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
                return result;
            }
            isOwner = true;
        }

        try {

            AddressComponentDTO addressComponentDTO =  locationService.getAddressComponentByCoordinate(lat, lng);
            if(addressComponentDTO != null && StringUtils.isNotEmpty(addressComponentDTO.getTown())
                    && StringUtils.isNotEmpty(addressComponentDTO.getTownAreaCode())){

                if(userId > 0 && isOwner) {
                    TownUserInfoParam userInfoParam = new TownUserInfoParam();
                    userInfoParam.setUserId(userId);
                    userInfoParam.setLastAreaCode(addressComponentDTO.getTownAreaCode());
                    townUserService.saveUserInfo(userInfoParam);
                }
                result.addProperty("province", addressComponentDTO.getProvince());
                result.addProperty("city", addressComponentDTO.getCity());
                result.addProperty("district", addressComponentDTO.getDistrict());
                result.addProperty("town", addressComponentDTO.getTown());
                result.addProperty("townAreaCode", addressComponentDTO.getTownAreaCode());
            }
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error getAddressComponent()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 根据行政区划编码获取下一级的行政区划列表【51120102】
     */
    public JsonObject getNextAreaCodeList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        String areaCode;
        try {
            areaCode = CommonUtil.getJsonParamString(jsonObject, AREA_CODE.getId(), null, AREA_CODE.getErrorCode(), 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {

            List<AreaCodeDTO> areaCodeDTOList = locationService.getNextAreaCodeListByAreaCode(areaCode);
            JsonArray areaCodeList = new JsonArray();
            if(areaCodeDTOList != null){
                for(AreaCodeDTO areaCodeDTO : areaCodeDTOList) {
                    JsonObject areaCodeJson = new JsonObject();
                    areaCodeJson.addProperty("areaCode", areaCodeDTO.getAreaCode());
                    areaCodeJson.addProperty("areaName", areaCodeDTO.getAreaName());
                    areaCodeList.add(areaCodeJson);
                }
            }
            result.add("areaCodeList", areaCodeList);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error getUserGuessInfo()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

}

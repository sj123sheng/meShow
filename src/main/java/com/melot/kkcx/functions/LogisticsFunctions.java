package com.melot.kkcx.functions;

import com.google.gson.JsonObject;
import com.melot.kk.logistics.api.domain.UserAddressDO;
import com.melot.kk.logistics.api.domain.UserAddressParam;
import com.melot.kk.logistics.api.service.UserAddressService;
import com.melot.kkcx.transform.LogisticsTF;
import com.melot.kktv.base.CommonStateCode;
import com.melot.kktv.base.Page;
import com.melot.kktv.base.Result;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ParameterKeys;
import com.melot.kktv.util.TagCodeEnum;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import static com.melot.kktv.util.ParamCodeEnum.USER_ID;

public class LogisticsFunctions {

    private static Logger logger = Logger.getLogger(LogisticsFunctions.class);

    private static final int IS_DEFAULT_ADDRESS_NOT_UPDATE = 100;

    @Resource
    UserAddressService userAddressService;

    /**
     * 获取用户收货地址列表【51011401】
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getUserAddressList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }
        int userId;
        int pageIndex;
        int countPerPage;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 10, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }


        try {
            Page<UserAddressDO> page = userAddressService.pageGetUserAddressDOList(userId, pageIndex, countPerPage);
            if (page == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            LogisticsTF.pageUserAddressDO2Json(page, result);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error： userAddressService.pageGetUserAddressDOList(userId=%s, pageIndex=%s, countPerPage=%s)", userId, pageIndex, countPerPage), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取用户收货地址信息【51011402】
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getUserAddressInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }
        int userId;
        int addressId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            addressId = CommonUtil.getJsonParamInt(jsonObject, "addressId", 0, "5101140201", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }


        try {
            Result<UserAddressDO> userAddressDOResult = userAddressService.getUserAddressDOByAddressId(addressId);
            if (userAddressDOResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            UserAddressDO userAddressDO = userAddressDOResult.getData();
            if (userAddressDO == null) {
                result.addProperty(ParameterKeys.TAG_CODE, "5101140202");
                return result;
            }
            if(userAddressDO.getUserId()!=userId){
                result.addProperty(ParameterKeys.TAG_CODE, "5101140203");
                return result;
            }
            LogisticsTF.userAddressDO2Json(userAddressDO, result);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error： userAddressService.getUserAddressDOByAddressId(addressId=%s)", addressId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 添加收货地址【51011403】
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject addUserAddress(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        String consigneeName;
        String consigneeMobile;
        String province;
        String city;
        String district;
        String detailAddress;
        int isDefaultAddress;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            consigneeName = CommonUtil.getJsonParamString(jsonObject, "consigneeName", null, "5101140301", 1, Integer.MAX_VALUE);
            consigneeMobile = CommonUtil.getJsonParamString(jsonObject, "consigneeMobile", null, "5101140302", 1, Integer.MAX_VALUE);
            province = CommonUtil.getJsonParamString(jsonObject, "province", null, "5101140303", 0, Integer.MAX_VALUE);
            city = CommonUtil.getJsonParamString(jsonObject, "city", null, "5101140304", 0, Integer.MAX_VALUE);
            district = CommonUtil.getJsonParamString(jsonObject, "district", null, "5101140305", 0, Integer.MAX_VALUE);
            detailAddress = CommonUtil.getJsonParamString(jsonObject, "detailAddress", null, "5101140306", 1, Integer.MAX_VALUE);
            isDefaultAddress = CommonUtil.getJsonParamInt(jsonObject, "isDefaultAddress", 0, "5101140307", 0, 1);

        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        UserAddressParam param = new UserAddressParam();
        param.setCity(city);
        param.setConsigneeMobile(consigneeMobile);
        param.setConsigneeName(consigneeName);
        param.setDetailAddress(detailAddress);
        param.setDistrict(district);
        param.setIsDefaultAddress(isDefaultAddress);
        param.setProvince(province);
        param.setUserId(userId);
        try {
            Result<Boolean> saveModuleResult = userAddressService.saveUserAddress(param);
            if (saveModuleResult == null || !CommonStateCode.SUCCESS.equals(saveModuleResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            result.addProperty("addressId", Integer.parseInt(saveModuleResult.getMsg()));
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error: userAddressService.saveUserAddress(param=%s)", param), e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 编辑收货地址【51011404】
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject editUserAddress(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        int addressId;
        String consigneeName;
        String consigneeMobile;
        String province;
        String city;
        String district;
        String detailAddress;
        Integer isDefaultAddress;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            addressId = CommonUtil.getJsonParamInt(jsonObject, "addressId", 0, "5101140401", 1, Integer.MAX_VALUE);
            consigneeName = CommonUtil.getJsonParamString(jsonObject, "consigneeName", null, null, 1, Integer.MAX_VALUE);
            consigneeMobile = CommonUtil.getJsonParamString(jsonObject, "consigneeMobile", null, null, 1, Integer.MAX_VALUE);
            province = CommonUtil.getJsonParamString(jsonObject, "province", null, null, 1, Integer.MAX_VALUE);
            city = CommonUtil.getJsonParamString(jsonObject, "city", null, null, 1, Integer.MAX_VALUE);
            district = CommonUtil.getJsonParamString(jsonObject, "district", null, null, 1, Integer.MAX_VALUE);
            detailAddress = CommonUtil.getJsonParamString(jsonObject, "detailAddress", null, null, 1, Integer.MAX_VALUE);
            isDefaultAddress = CommonUtil.getJsonParamInt(jsonObject, "isDefaultAddress", IS_DEFAULT_ADDRESS_NOT_UPDATE, null, 0, 1);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        //isDefaultAddress不为1或0，或者未填写，则不更新改字段
        if (isDefaultAddress == IS_DEFAULT_ADDRESS_NOT_UPDATE) {
            isDefaultAddress = null;
        }
        UserAddressParam param = new UserAddressParam();
        param.setAddressId(addressId);
        param.setCity(city);
        param.setConsigneeMobile(consigneeMobile);
        param.setConsigneeName(consigneeName);
        param.setDetailAddress(detailAddress);
        param.setDistrict(district);
        param.setIsDefaultAddress(isDefaultAddress);
        param.setProvince(province);
        param.setUserId(userId);
        try {
            Result<UserAddressDO> userAddressDOResult = userAddressService.getUserAddressDOByAddressId(addressId);
            if (userAddressDOResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            UserAddressDO userAddressDO = userAddressDOResult.getData();
            if (userAddressDO == null) {
                result.addProperty(ParameterKeys.TAG_CODE, "5101140403");
                return result;
            }
            if(userAddressDO.getUserId()!=userId){
                result.addProperty(ParameterKeys.TAG_CODE, "5101140404");
                return result;
            }

            Result<Boolean> saveModuleResult = userAddressService.updateUserAddress(param);
            if (saveModuleResult == null) {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            } else if (!CommonStateCode.SUCCESS.equals(saveModuleResult.getCode())) {
                logger.error(String.format("更新地址失败,msg=%s,param=%s", saveModuleResult.getMsg(), param));
                result.addProperty("TagCode", "5101140402");
                return result;
            }
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error: userAddressService.updateUserAddress(param=%s)", param), e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 删除收货地址【51011405】
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject deleteUserAddress(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int addressId;
        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            addressId = CommonUtil.getJsonParamInt(jsonObject, "addressId", 0, "5101140501", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            Result<UserAddressDO> userAddressDOResult = userAddressService.getUserAddressDOByAddressId(addressId);
            if (userAddressDOResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            UserAddressDO userAddressDO = userAddressDOResult.getData();
            if (userAddressDO == null) {
                result.addProperty(ParameterKeys.TAG_CODE, "5101140503");
                return result;
            }
            if(userAddressDO.getUserId()!=userId){
                result.addProperty(ParameterKeys.TAG_CODE, "5101140504");
                return result;
            }

            Result<Boolean> saveModuleResult = userAddressService.deleteUserAddress(addressId);
            if (saveModuleResult == null) {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            } else if (!CommonStateCode.SUCCESS.equals(saveModuleResult.getCode())) {
                logger.error(String.format("删除失败,msg=%s,addressId=%s", saveModuleResult.getMsg(), addressId));
                result.addProperty("TagCode", "5101140502");
                return result;
            }
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error: userAddressService.deleteUserAddress(addressId=%s)", addressId), e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }
}


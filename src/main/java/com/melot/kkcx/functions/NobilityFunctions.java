package com.melot.kkcx.functions;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.kk.nobility.api.domain.NobilityInfo;
import com.melot.kk.nobility.api.domain.NobilityUserInfo;
import com.melot.kk.nobility.api.service.NobilityService;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kkcx.service.UserAssetServices;
import com.melot.kkcx.transform.NobilityTF;
import com.melot.kktv.base.CommonStateCode;
import com.melot.kktv.base.Page;
import com.melot.kktv.base.Result;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ParameterKeys;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.sdk.core.util.MelotBeanFactory;

public class NobilityFunctions {
 
    private static Logger log = Logger.getLogger(NobilityFunctions.class);
    
    /**
     * 用户是否有贵族信息[51010401]
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject getUserNobilityState(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        
        JsonObject result = new JsonObject();
        
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        NobilityService nobilityService = (NobilityService)MelotBeanFactory.getBean("nobilityService");
        try {
            Result<NobilityUserInfo> userInfoResult =  nobilityService.getNobilityUserInfo(userId);
            if (userInfoResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            if (CommonStateCode.SUCCESS.equals(userInfoResult.getCode())) {
                NobilityUserInfo nobilityUserInfo = userInfoResult.getData();
                result.addProperty("nobilityState", nobilityUserInfo.getNobilityState());
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            } else {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            }
        } catch (Exception e) {
            log.error(String.format("module error: nobilityService.getNobilityUserInfo(%s)", userId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        return result;
    }

    /**
     * 获取用户贵族详情 [51010402]
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject getUserNobilityInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        
        JsonObject result = new JsonObject();
        
        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        // 靓号转化为真实ID
        Integer realUserId = UserAssetServices.luckyIdToUserId(userId);
        if (realUserId == null) {
            realUserId = userId;
        }
        
        // 添加用户的基本信息
        try {
            KkUserService kkUserService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
            UserProfile userProfile = kkUserService.getUserProfile(realUserId);
            result.addProperty("nickname", userProfile.getNickName());
        } catch (Exception e) {
            log.error(String.format("module error: kkUserService.getUserProfile(%s)", realUserId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        
        NobilityService nobilityService = (NobilityService)MelotBeanFactory.getBean("nobilityService");
        try {
            Result<NobilityUserInfo> userInfoResult =  nobilityService.getNobilityUserInfo(realUserId);
            if (userInfoResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            if (CommonStateCode.SUCCESS.equals(userInfoResult.getCode())) {
                NobilityUserInfo nobilityUserInfo = userInfoResult.getData();
                NobilityTF.nobilityUserInfoTF(result, nobilityUserInfo);
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            } else {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            }
        } catch (Exception e) {
            log.error(String.format("module error: nobilityService.getNobilityUserInfo(%s)", realUserId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        
        if (!checkTag) {
            result.remove("userNobilityPoint");
        }
        
        return result;
    }

    /**
     * 获取贵族爵位列表[51010403]
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject getNobilityList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        
        JsonObject result = new JsonObject();
        
        NobilityService nobilityService = (NobilityService)MelotBeanFactory.getBean("nobilityService");
        try {
            Result<Page<NobilityInfo>> pageResult = nobilityService.getNobilityInfos();
            if (pageResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            
            if (CommonStateCode.SUCCESS.equals(pageResult.getCode())) {
                Page<NobilityInfo> page = pageResult.getData();
                List<NobilityInfo> list = page.getList();
                JsonArray nobilityList = new JsonArray();
                for (NobilityInfo nobilityInfo : list) {
                    JsonObject infoJson = new JsonObject();
                    infoJson.addProperty("nobilityId", nobilityInfo.getNobilityId());
                    infoJson.addProperty("nobilityName", nobilityInfo.getNobilityName());
                    infoJson.addProperty("nobilityIcon", nobilityInfo.getNobilityIcon());
                    nobilityList.add(infoJson);
                }
                result.add("nobilityList", nobilityList);
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            } else {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            }
        } catch (Exception e) {
            log.error("Module error nobilityService.getNobilityInfos(): ", e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        return result;
    }

    /**
     * 获取贵族爵位详情[51010404]
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject getNobilityInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        
        JsonObject result = new JsonObject();
        
        int nobilityId;
        try {
            nobilityId = CommonUtil.getJsonParamInt(jsonObject, "nobilityId", 0, "5101040401", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        NobilityService nobilityService = (NobilityService)MelotBeanFactory.getBean("nobilityService");
        try {
            Result<NobilityInfo> nobilityInfoResult = nobilityService.getNobilityInfo(nobilityId);
            if (nobilityInfoResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            if (CommonStateCode.SUCCESS.equals(nobilityInfoResult.getCode())) {
                NobilityInfo nobilityInfo = nobilityInfoResult.getData();
                NobilityTF.nobilityInfoTF(result, nobilityInfo);
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            } else {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            }
        } catch (Exception e) {
            log.error(String.format("module error: nobilityService.getNobilityInfo(%s)", nobilityId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        return result;
    }

    /**
     * 开通/续费爵位信息[52010405]
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject buyNobility(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        return result;
    }
}

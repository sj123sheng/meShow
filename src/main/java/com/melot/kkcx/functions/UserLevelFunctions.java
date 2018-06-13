package com.melot.kkcx.functions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.melot.kk.userLevelUp.api.dto.UserLevelGetRedEvelopeHistDTO;
import com.melot.kk.userLevelUp.api.service.UserLevelRedEvelopeService;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kktv.base.CommonStateCode;
import com.melot.kktv.base.Page;
import com.melot.kktv.base.Result;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.ParameterKeys;
import com.melot.kktv.util.SecurityFunctions;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.module.packagegift.driver.service.XmanService;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * Title: UserLevelFunctions
 * <p>
 * Description: 用户财富等级升级相关接口
 * </p>
 * 
 * @author <a href="mailto:anwen.wei@melot.cn">魏安稳</a>
 * @version V1.0
 * @since 2018年6月11日 上午10:45:52
 */
public class UserLevelFunctions {
    private static Logger logger = Logger.getLogger(UserLevelFunctions.class);
    
    @Resource
    UserLevelRedEvelopeService userLevelRedEvelopeService;
    
    @Resource
    KkUserService kkUserService;
    
    /**
     * 升级用户点击我知道了，发庆升面板【51010901】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject sendRedEvelope(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }
        int userId;
        int userLevelHistId;
        int roomId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, "5101090101", 1, Integer.MAX_VALUE);
            userLevelHistId = CommonUtil.getJsonParamInt(jsonObject, "userLevelHistId", 0, "5101090102", 1, Integer.MAX_VALUE);
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, "5101090103", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        // 神秘人直接返回成功
        if (userId <= 1127828 && userId >= 1000578 ) {
            XmanService xmanService = (XmanService) MelotBeanFactory.getBean("xmanService");
            if (xmanService.getXmanConf(userId) != null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
                return result;
            }
        }
        
        try {
            Result<Boolean> moduleResult = userLevelRedEvelopeService.sendUserLevelUpRedEvelope(userId, userLevelHistId, roomId);
            if (moduleResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            String code = moduleResult.getCode();
            if (code.equals("2")) {
                result.addProperty(ParameterKeys.TAG_CODE, "5101090104");
                return result;
            } else if (code.equals("3")) {
                result.addProperty(ParameterKeys.TAG_CODE, "5101090105");
                return result;
            } else if (code.equals("4")) {
                result.addProperty(ParameterKeys.TAG_CODE, "5101090106");
                return result;
            } else if (code.equals("5")) {
                result.addProperty(ParameterKeys.TAG_CODE, "5101090107");
                return result;
            } else if (!code.equals(CommonStateCode.SUCCESS)) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("userLevelRedEvelopeService.sendUserLevelUpRedEvelope(userId=%s, userLevelHistId=%s, roomId=%s)", userId, userLevelHistId, roomId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 升级用户添加红包金额【52010902】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject addRedEvelopeAmount(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        // 安全sv验证
        try {
            JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
            if (rtJO != null)
                return rtJO;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, "40010002");
            return result;
        }
        
        // 校验token
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }
        
        // 获取参数
        int userId;
        long addShowMoney;
        int userLevelHistId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, "5201090201", 1, Integer.MAX_VALUE);
            userLevelHistId = CommonUtil.getJsonParamInt(jsonObject, "userLevelHistId", 0, "5201090202", 1, Integer.MAX_VALUE);
            addShowMoney = CommonUtil.getJsonParamLong(jsonObject, "addShowMoney", 0, "5201090203", 1, Long.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            Result<Boolean> moduleResult = userLevelRedEvelopeService.addRedEvelopeShowMoney(userLevelHistId, userId, addShowMoney);
            if (moduleResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            String code = moduleResult.getCode();
            if (code.equals("2")) {
                result.addProperty(ParameterKeys.TAG_CODE, "5201090204");
                return result;
            } else if (code.equals("3")) {
                result.addProperty(ParameterKeys.TAG_CODE, "5201090205");
                return result;
            } else if (code.equals("4")) {
                result.addProperty(ParameterKeys.TAG_CODE, "5201090206");
                return result;
            } else if (code.equals("5")) {
                result.addProperty(ParameterKeys.TAG_CODE, "5201090207");
                return result;
            } else if (!code.equals(CommonStateCode.SUCCESS)) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("userLevelRedEvelopeService.addRedEvelopeShowMoney(userId=%s, userLevelHistId=%s, addShowMoney=%s)", userId, userLevelHistId, addShowMoney), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 查询房间红包记录接口【51010903】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getRedEvelopeRecords(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int userLevelHistId;
        int start;
        int num;
        try {
            userLevelHistId = CommonUtil.getJsonParamInt(jsonObject, "userLevelHistId", 0, "5101090301", 1, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, null, 0, Integer.MAX_VALUE);
            num = CommonUtil.getJsonParamInt(jsonObject, "num", 10, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            Result<Page<UserLevelGetRedEvelopeHistDTO>> moduleResult = userLevelRedEvelopeService.getRedEvelopeRecords(userLevelHistId, start, num);
            if (moduleResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            String code = moduleResult.getCode();
            if (!CommonStateCode.SUCCESS.equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            Page<UserLevelGetRedEvelopeHistDTO> page = moduleResult.getData();
            if (page == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            JsonArray getList = new JsonArray();
            List<UserLevelGetRedEvelopeHistDTO> list = page.getList();
            List<Integer> userIds = new ArrayList<>(list.size());
            Map<Integer, UserLevelGetRedEvelopeHistDTO> map = new HashMap<>(list.size());
            if (!CollectionUtils.isEmpty(list)) {
                for (UserLevelGetRedEvelopeHistDTO dto : list) {
                    map.put(dto.getUserId(), dto);
                    userIds.add(dto.getUserId());
                }
                List<UserProfile> userInfos = kkUserService.getUserProfileBatch(userIds);
                for (UserProfile userProfile : userInfos) {
                    JsonObject userJson = new JsonObject();
                    userJson.addProperty("userId", userProfile.getUserId());
                    userJson.addProperty("nickname", userProfile.getNickName());
                    userJson.addProperty("portrait", userProfile.getPortrait());
                    if (map.containsKey(userProfile.getUserId())) {
                        userJson.addProperty("amount", map.get(userProfile.getUserId()).getShowMoney());
                    }
                }
            }
            
            result.addProperty("count", page.getCount());
            result.add("getList", getList);
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("userLevelRedEvelopeService.getRedEvelopeRecords(userLevelHistId=%s, start=%s, num=%s)", userLevelHistId, start, num), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }
}

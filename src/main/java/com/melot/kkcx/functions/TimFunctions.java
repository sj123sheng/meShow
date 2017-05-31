/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2016
 */
package com.melot.kkcx.functions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.druid.util.StringUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.kkcore.user.api.UserInfoDetail;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kkcx.model.ActorLevel;
import com.melot.kkcx.model.ImAdmin;
import com.melot.kkcx.model.ImAdminList;
import com.melot.kkcx.model.RichLevel;
import com.melot.kkcx.service.GeneralService;
import com.melot.kkcx.service.ProfileServices;
import com.melot.kkcx.service.TimService;
import com.melot.kkcx.service.UserService;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.SecurityFunctions;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * Title: TimFunctions
 * <p>
 * Description: 
 * </p>
 * 
 * @author 林端端<a href="mailto:duanduan.lin@melot.cn">
 * @version V1.0
 * @since 2016年10月14日 下午5:41:04
 */
public class TimFunctions {

    /**
     * 腾讯IM云账号注册(50001009)
     * 
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject register(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        if (rtJO != null)
            return rtJO;

        int userId;
        
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 100, Integer.MAX_VALUE);
        } catch (Exception ex) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        KkUserService kkUserService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
        UserProfile userProfile = kkUserService.getUserProfile(userId);
        if (userProfile == null) {
            result.addProperty("TagCode", TagCodeEnum.USER_NOT_EXIST);
            return result;
        }

        // 腾讯云IM账号注册
        boolean ret = TimService.registerTim(userId + "", GeneralService.replaceSensitiveWords(userProfile.getUserId(), userProfile.getNickName()));
        if (ret) {
            // 获取腾讯云IM接口调用签名
            String sig = TimService.getTimSig(userId);
            if (StringUtils.isEmpty(sig)) {
                result.addProperty("TagCode", "01090001");
            } else {
                result.addProperty("sig", sig);
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            }
        } else {
            result.addProperty("TagCode", "01090001");
        }
        return result;
    }
    
    /**
     * im批量获取用户信息及注册(50001010)
     * 
     * @param jsonObject
     * @param request
     * @return
     */
    public JsonObject getUserProfile(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        String userId;
        int maxCount = 100;
        List<Integer> userArray = new ArrayList<Integer>();
        
        try {
            userId = CommonUtil.getJsonParamString(jsonObject, "userId", null, TagCodeEnum.USERID_MISSING, 1, maxCount * 10);
            String[] userIdArr = userId.split(",");
            if (userIdArr.length > maxCount) {
                result.addProperty("TagCode", "01100001");
                return result;
            }
            for (String userIdStr : userIdArr) {
                userArray.add(Integer.parseInt(userIdStr));
            }
        } catch (Exception ex) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        JsonArray jsonArray = new JsonArray();
        //管理员账号
        ImAdminList adminArray = MelotBeanFactory.getBean("imAdminList", ImAdminList.class);
        Map<Integer, ImAdmin> adminList = adminArray.getList();        
        
        for (Iterator<Integer> iterator = userArray.iterator(); iterator.hasNext();) {
            Integer userIdInteger = iterator.next();
            if (userIdInteger.equals(0)) {
                iterator.remove();
                continue;
            }
            if (userIdInteger < 100) {
                if (adminList.get(userIdInteger) != null) {
                    JsonObject jsonObj = new JsonObject();
                    jsonObj.addProperty("userId", userIdInteger);
                    jsonObj.addProperty("nickName", adminList.get(userIdInteger).getNickName());
                    jsonObj.addProperty("portrait", adminList.get(userIdInteger).getPortrait());
                    jsonObj.addProperty("gender", 0);
                    jsonObj.addProperty("earnTotal", 0);
                    jsonObj.addProperty("consumeTotal", 0);
                    jsonObj.addProperty("actorLevel", 0);
                    jsonObj.addProperty("richLevel", 0);
                    
                    jsonArray.add(jsonObj);
                }
                iterator.remove();
            }
        }
        
        KkUserService kkUserService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
        List<UserInfoDetail> userInfoDetailList = kkUserService.getUserDetailInfoBatch(userArray);
        
        for (Iterator<Integer> userIterator = userArray.iterator(); userIterator.hasNext();) {
            Integer userIdElement = userIterator.next();
            for (UserInfoDetail userInfoDetail : userInfoDetailList) {
                if (userInfoDetail.getProfile() != null && userIdElement.equals(userInfoDetail.getProfile().getUserId())) {
                    JsonObject jsonObj = new JsonObject();
                    jsonObj.addProperty("userId", userInfoDetail.getProfile().getUserId());
                    jsonObj.addProperty("nickName", GeneralService.replaceSensitiveWords(userInfoDetail.getProfile().getUserId(), userInfoDetail.getProfile().getNickName()));
                    jsonObj.addProperty("gender", userInfoDetail.getProfile().getGender());
                    jsonObj.addProperty("isActor", userInfoDetail.getProfile().getIsActor());
                    jsonObj.addProperty("portrait", userInfoDetail.getProfile().getPortrait());
                    jsonObj.addProperty("richLevel", userInfoDetail.getProfile().getUserLevel());
                    jsonObj.addProperty("actorLevel", userInfoDetail.getProfile().getActorLevel());
                    if (userInfoDetail.getAssets() != null) {
                        jsonObj.addProperty("earnTotal", userInfoDetail.getAssets().getEarnTotal());
                        jsonObj.addProperty("consumeTotal", userInfoDetail.getAssets().getConsumeTotal());
                        ActorLevel actorLevel = UserService.getActorLevel(userInfoDetail.getAssets().getEarnTotal());
                        jsonObj.addProperty("actorMin", actorLevel.getMinValue());
                        jsonObj.addProperty("actorMax", actorLevel.getMaxValue());
                        RichLevel richLevel = UserService.getRichLevel(userInfoDetail.getAssets().getConsumeTotal());
                        jsonObj.addProperty("richMin", richLevel.getMinValue());
                        jsonObj.addProperty("richMax", richLevel.getMaxValue());
                    }
                    
                    Integer adminType = ProfileServices.getUserAdminType(userInfoDetail.getProfile().getUserId());
                    if (adminType != null && adminType != -1) {
                        jsonObj.addProperty("siteAdmin", adminType);
                    }
                    
                    TimService.registerTim(userInfoDetail.getProfile().getUserId() + "", GeneralService.replaceSensitiveWords(userInfoDetail.getProfile().getUserId(), userInfoDetail.getProfile().getNickName()));
                    jsonArray.add(jsonObj);
                    break;
                }
            }
        }
        
        result.add("userList", jsonArray);
        result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
}

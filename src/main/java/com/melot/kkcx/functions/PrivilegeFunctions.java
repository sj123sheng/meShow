/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 * <p>
 * Copyright (c) Melot Inc. 2018
 */
package com.melot.kkcx.functions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.melot.kkcx.util.ValidTypeEnum;
import com.melot.room.pendant.domain.ReturnResult;
import com.melot.room.pendant.dto.UserPendantDTO;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.api.menu.sdk.utils.Collectionutils;
import com.melot.kk.pkgame.api.constant.ReturnResultCode;
import com.melot.kkcore.actor.api.RoomInfo;
import com.melot.kkcore.actor.service.ActorService;
import com.melot.kkcx.model.UserProp;
import com.melot.kkcx.service.ProfileServices;
import com.melot.kkcx.util.PropTypeEnum;
import com.melot.kktv.base.Result;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.ParameterKeys;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.module.packagegift.driver.domain.MagicWandRoomDTO;
import com.melot.module.packagegift.driver.domain.ResUserXman;
import com.melot.module.packagegift.driver.domain.ResXman;
import com.melot.module.packagegift.driver.domain.UserChatBubbleDTO;
import com.melot.module.packagegift.driver.domain.UserMagicWandDTO;
import com.melot.module.packagegift.driver.service.ChatBubbleService;
import com.melot.module.packagegift.driver.service.PrivilegeService;
import com.melot.module.packagegift.driver.service.XmanService;
import com.melot.room.pendant.service.PendantService;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * Title: PrivilegeFunctions
 * <p>
 * Description: 用户特权相关接口
 * </p>
 * 
 * @author 林端端<a href="mailto:duanduan.lin@melot.cn" />
 * @version V1.0
 * @since 2018年11月27日 下午2:15:56
 */
public class PrivilegeFunctions {
    
    private static Logger logger = Logger.getLogger(PrivilegeFunctions.class);
    
    @Resource
    PendantService pendantService;
    
    @Resource
    ChatBubbleService chatBubbleService;
    
    @Resource
    PrivilegeService privilegeService;
    
    @Resource
    ActorService actorService;
    
    /**
     * 获取用户魔杖详情（51011301）
     *
     * @param jsonObject 请求对象
     * @param checkTag   是否验证token标记
     * @param request    请求
     * @return 挂件详情
     */
    public JsonObject getUserMagicWand(JsonObject jsonObject, Boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            UserMagicWandDTO userMagicWandDTO = privilegeService.getUserMagicWand(userId);
            boolean hasMagicWand = false;
            if (userMagicWandDTO != null) {
                hasMagicWand = userMagicWandDTO.getHasMagicWand();
                result.addProperty("magicWandTimes", userMagicWandDTO.getMagicWandTimes());
                result.addProperty("usedTimes", userMagicWandDTO.getUsedTimes());
            }
            result.addProperty("hasMagicWand", hasMagicWand);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("PrivilegeFunctions.getUserMagicWand(" + userId + ") return exception.", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }

        return result;
    }
    
    /**
     * 使用魔杖（51011302）
     *
     * @param jsonObject 请求对象
     * @param checkTag   是否验证token标记
     * @param request    请求
     * @return 挂件详情
     */
    public JsonObject useMagicWand(JsonObject jsonObject, Boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId, magicWandId, roomId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            magicWandId = CommonUtil.getJsonParamInt(jsonObject, "magicWandId", 0, "5101130201", 1, Integer.MAX_VALUE);
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, TagCodeEnum.ROOMID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            // 神秘人不可使用魔杖
            XmanService xmanService = (XmanService) MelotBeanFactory.getBean("xmanService");
            ResUserXman resUserXman = xmanService.getResUserXmanByUserId(userId);
            if (resUserXman != null && (resUserXman.getExpireTime().getTime() >= new Date().getTime())) {
                ResXman resXman = xmanService.getResXmanByUserId(userId);
                if (resXman != null && resXman.getMysType() == 2) {
                    result.addProperty(ParameterKeys.TAG_CODE, "5101130202");
                    return result;
                }
            }
            
            RoomInfo roomInfo = actorService.getRoomInfoById(roomId);
            if (roomInfo == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.ROOMID_MISSING);
                return result;
            } else {
                if (roomInfo.getLiveEndTime() != null) {
                    result.addProperty(ParameterKeys.TAG_CODE, "5101130205");
                    return result;
                }
            }
            
            Result<Integer> resp = privilegeService.useMagicWand(userId, roomId, magicWandId);
            String returnCode = resp.getCode();
            if (ReturnResultCode.SUCCESS.getCode().equals(returnCode)) {
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            } else {
                if ("-2".equals(returnCode)) {
                    result.addProperty("TagCode", "5101130204");
                } else if ("-1".equals(returnCode)) {
                    result.addProperty("TagCode", "5101130203");
                } else if ("-3".equals(returnCode)) {
                    result.addProperty("TagCode", "5101130206");
                } else {
                    result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                }
            }
            
            int magicWandTimes = 0;
            int usedTimes = 0;
            UserMagicWandDTO userMagicWandDTO = privilegeService.getUserMagicWand(userId);
            if (userMagicWandDTO != null) {
                magicWandTimes = userMagicWandDTO.getMagicWandTimes();
                usedTimes = userMagicWandDTO.getUsedTimes();
            }
            result.addProperty("magicWandTimes", magicWandTimes);
            result.addProperty("usedTimes", usedTimes);
        } catch (Exception e) {
            logger.error("PrivilegeFunctions.operateUserProp(" + userId + ") return exception.", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }

        return result;
    }
    
    /**
     * 获取直播间魔杖使用情况（51011303）
     *
     * @param jsonObject 请求对象
     * @param checkTag   是否验证token标记
     * @param request    请求
     * @return 挂件详情
     */
    public JsonObject getMagicWandUsageSituation(JsonObject jsonObject, Boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        int roomId;
        try {
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, TagCodeEnum.ROOMID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            boolean isOpenMagicWand = false;
            MagicWandRoomDTO magicWandRoomDTO = privilegeService.getMagicWandRoom(roomId);
            if (magicWandRoomDTO != null) {
                isOpenMagicWand = magicWandRoomDTO.getIsOpenMagicWand();
                result.addProperty("magicWandId", magicWandRoomDTO.getMagicWandId());
                result.addProperty("userId", magicWandRoomDTO.getUserId());
                if (!StringUtil.strIsNull(magicWandRoomDTO.getNickname())) {
                    result.addProperty("nickname", magicWandRoomDTO.getNickname());
                }
                if (magicWandRoomDTO.getExpireTime() != null) {
                    result.addProperty("expireTime", magicWandRoomDTO.getExpireTime().getTime() - System.currentTimeMillis());
                }
            }
            result.addProperty("isOpenMagicWand", isOpenMagicWand);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("PrivilegeFunctions.getMagicWandUsageSituation(" + roomId + ") return exception.", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }

        return result;
    }
    
    /**
     * 佩戴或者取下道具（51011304）
     *
     * @param jsonObject 请求对象
     * @param checkTag   是否验证token标记
     * @param request    请求
     * @return 挂件详情
     */
    public JsonObject operateUserProp(JsonObject jsonObject, Boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId, propType, propId, operateType;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            propType = CommonUtil.getJsonParamInt(jsonObject, "propType", 0, "5101130401", 1, Integer.MAX_VALUE);
            propId = CommonUtil.getJsonParamInt(jsonObject, "propId", 0, "5101130402", 1, Integer.MAX_VALUE);
            operateType = CommonUtil.getJsonParamInt(jsonObject, "operateType", 0, "5101130403", 0, 1);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            List<UserProp> userPropList = new ArrayList<>();
            if (PropTypeEnum.PENDANT.getCode() == propType) {
                ReturnResult<List<UserPendantDTO>> userPendantResult = null;
                if (0 == operateType) {
                    userPendantResult = pendantService.cancelAdornPendant(userId, propId);
                } else if (1 == operateType) {
                    userPendantResult = pendantService.adornPendant(userId, propId);
                }
                userPropList.addAll(switchPendantToUserProp(userPendantResult.getData()));

            } else if (PropTypeEnum.CHAT_BUBBLE.getCode() == propType) {
                chatBubbleService.operateChatBubble(userId, propId, operateType);
                List<UserChatBubbleDTO> userChatBubbleDTOList = chatBubbleService.getUserChatBubbleList(userId, null);
                if (!Collectionutils.isEmpty(userChatBubbleDTOList)) {
                    for (UserChatBubbleDTO userChatBubbleDTO : userChatBubbleDTOList) {
                        userPropList.add(ProfileServices.switchBubbleToUserProp(userChatBubbleDTO));
                    }
                }
            }

            if (!Collectionutils.isEmpty(userPropList)) {
                result.add("userPropList", new JsonParser().parse(new Gson().toJson(userPropList)).getAsJsonArray());
            }
            result.addProperty("propName", PropTypeEnum.getNameByCode(propType));
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("PrivilegeFunctions.operateUserProp(" + userId + ") return exception.", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }

        return result;
    }

    /**
     * 将挂件详情转为用户道具列表
     *
     * @param userPendantDTOList 挂件列表
     * @return 用户道具列表
     */
    private List<UserProp> switchPendantToUserProp(List<UserPendantDTO> userPendantDTOList) {
        List<UserProp> userPropList = new ArrayList<>();
        for (UserPendantDTO userPendantDTO : userPendantDTOList) {
            UserProp userProp = new UserProp();
            userProp.setAppLargeUrl(userPendantDTO.getPendantBigUrl());
            userProp.setDesc(userPendantDTO.getPendantDescribe());
            userProp.setId(userPendantDTO.getPendantId());
            userProp.setIsLight(userPendantDTO.getUsed() ? 1 : 0);
            userProp.setValidType(userPendantDTO.getValidType());
            if (userPendantDTO.getValidTime() != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(userPendantDTO.getValidTime().getTime());
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                userProp.setLeftTime(calendar.getTimeInMillis() - System.currentTimeMillis());
            }
            userProp.setLevel(userPendantDTO.getLevel());
            userProp.setName(userPendantDTO.getPendantName());
            userProp.setSmallUrl(userPendantDTO.getPendantSmallUrl());
            userProp.setSubType(userPendantDTO.getPendantType());
            userProp.setType(PropTypeEnum.PENDANT.getCode());
            userProp.setWebLargeUrl(userPendantDTO.getPendantBigUrl());
            userPropList.add(userProp);
        }

        return userPropList;
    }

}

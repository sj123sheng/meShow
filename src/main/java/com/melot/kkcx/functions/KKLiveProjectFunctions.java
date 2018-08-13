package com.melot.kkcx.functions;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.kk.liveproject.api.dto.LiveProjectTaskDTO;
import com.melot.kk.liveproject.api.dto.ResPrivatePhotoDTO;
import com.melot.kk.liveproject.api.service.LiveProjectService;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ParameterKeys;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.module.api.exceptions.MelotModuleException;
import com.melot.room.gift.domain.ReturnResult;
import com.melot.room.gift.dto.CatalogGiftDTO;
import com.melot.room.gift.dto.GiftDTO;
import com.melot.room.gift.dto.RoomGiftDTO;
import com.melot.room.gift.service.RoomGiftService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Title: KKLiveProjectFunctions
 * <p>
 * Description:KK直播微信小程序API
 * </p>
 *
 * @author <a href="mailto:baolin.zhu@melot.cn">朱宝林</a>
 * @version V1.0.0
 * @since 2018/8/1 9:38
 */
public class KKLiveProjectFunctions {

    private static Logger log = Logger.getLogger(UserLevelFunctions.class);

    @Resource
    LiveProjectService liveProjectService;

    @Resource
    RoomGiftService roomGiftService;

    /**
     * 51120201
     * 获取主播私密照
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getActorPrivatePhoto(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        int actorId;
        int userId;
        try {
            actorId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.ACTOR_ID, 0, TagCodeEnum.PARAMETER_PARSE_ERROR, 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        // 如果存在userId，需检验token
        if (userId != 0 && !checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }

        List<ResPrivatePhotoDTO> privatePhotoListForUser;
        try {
            privatePhotoListForUser = liveProjectService.getPrivatePhotoListForUser(actorId, userId == 0 ? null : userId);
        } catch (Exception e) {
            log.error(String.format("module error: liveProjectService.getPrivatePhotoListForUser(actorId=%s, userId=%s)", actorId, userId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }

        if (privatePhotoListForUser != null) {
            JsonArray array = new JsonArray();
            JsonObject json;
            JsonArray helpUserIds;
            for (ResPrivatePhotoDTO resPrivatePhotoDTO : privatePhotoListForUser) {
                json = new JsonObject();
                json.addProperty("photoId", resPrivatePhotoDTO.getPhotoId());
                json.addProperty("photoTitle", resPrivatePhotoDTO.getTitleName());
                json.addProperty("level", resPrivatePhotoDTO.getPhotoLevel());
                json.addProperty("unlockPrice", resPrivatePhotoDTO.getUnlockPrice());
                json.addProperty("unlockShareNum", resPrivatePhotoDTO.getUnlockShareNum());
                json.addProperty("photoUrl", resPrivatePhotoDTO.getPhotoPath());

                helpUserIds = new JsonArray();
                // 游客不显示以下信息
                if (userId != 0) {
                    if (resPrivatePhotoDTO.getCurrentUnlockNum() != null) {
                        json.addProperty("currentUnlockNum", resPrivatePhotoDTO.getCurrentUnlockNum());
                    }
                    if (resPrivatePhotoDTO.getUnlockState() != null) {
                        json.addProperty("isUnlock", resPrivatePhotoDTO.getUnlockState() > 0);
                    }
                    if (resPrivatePhotoDTO.getHelpUnlockUserIds() != null) {
                        for (Integer helpUserId : resPrivatePhotoDTO.getHelpUnlockUserIds()) {
                            helpUserIds.add(helpUserId);
                        }
                    }
                }
                json.add("helpUserIds", helpUserIds);
                array.add(json);
            }
            result.add("photoList", array);
        }
        result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
        return result;
    }


    /**
     * 51120202
     * 检验是否支付解锁成功
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject checkUnlockAfterPay(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        String adminOrderNo;
        try {
            adminOrderNo = CommonUtil.getJsonParamString(jsonObject, "adminOrderNo", null, "5112020201", 1, 100);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        String tagCode = "5112020202";
        try {
            log.info("start check, adminOrderNo=" + adminOrderNo);
            if (liveProjectService.checkUnlockAfterPay(adminOrderNo)) {
                tagCode = TagCodeEnum.SUCCESS;
            } else {
                log.info("check fail: adminOrderNo=" + adminOrderNo);
            }
        } catch (Exception e) {
            log.error(String.format("Error:checkUnlockAfterPay(adminOrderNo=%s)", adminOrderNo), e);
            tagCode = TagCodeEnum.MODULE_UNKNOWN_RESPCODE;
        }
        result.addProperty(ParameterKeys.TAG_CODE, tagCode);
        return result;
    }


    /**
     * 51120203
     * 分享解锁私密照
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject shareUnlockActorPrivatePhoto(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }

        int histId, userId;
        try {
            histId = CommonUtil.getJsonParamInt(jsonObject, "histId", 0, TagCodeEnum.PARAMETER_PARSE_ERROR, 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, TagCodeEnum.PARAMETER_PARSE_ERROR, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        String tagCode = "5112020302";
        try {
            if (liveProjectService.shareUnlockPrivatePhoto(histId, userId)) {
                tagCode = TagCodeEnum.SUCCESS;
            } else {
                log.info(String.format("Fail:shareUnlockPrivatePhoto(histId=%s, userId=%s)", histId, userId));
            }
        } catch (MelotModuleException e) {
            log.info(String.format("Fail:shareUnlockPrivatePhoto(histId=%s, userId=%s)", histId, userId), e);
            if (e.getErrCode() == 101) {
                tagCode = "5112020301";
            } else if (e.getErrCode() == 102) {
                tagCode = TagCodeEnum.INVALID_PARAMETERS;
            } else if (e.getErrCode() == 103) {
                tagCode = "5112020302";
            }
        } catch (Exception e) {
            log.error(String.format("Error:shareUnlockPrivatePhoto(histId=%s, userId=%s)", histId, userId), e);
            tagCode = TagCodeEnum.MODULE_UNKNOWN_RESPCODE;
        }
        result.addProperty(ParameterKeys.TAG_CODE, tagCode);
        return result;
    }

    /**
     * 51120204
     * 生成分享解锁流水
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject addShareUnlockHist(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }

        int userId, actorId;
        long photoId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, TagCodeEnum.PARAMETER_PARSE_ERROR, 1, Integer.MAX_VALUE);
            actorId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.ACTOR_ID, 0, TagCodeEnum.PARAMETER_PARSE_ERROR, 1, Integer.MAX_VALUE);
            photoId = CommonUtil.getJsonParamLong(jsonObject, "photoId", 0, TagCodeEnum.PARAMETER_PARSE_ERROR, 1, Long.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        String tagCode = TagCodeEnum.MODULE_UNKNOWN_RESPCODE;
        try {
            Long histId = liveProjectService.addShareUnlockHist(userId, actorId, photoId);
            if (histId != null) {
                result.addProperty("histId", histId);
                tagCode = TagCodeEnum.SUCCESS;
            }
        } catch (MelotModuleException e) {
            log.info(String.format("Fail:addShareUnlockHist(userId=%s, actorId=%s, photoId=%s)", userId, actorId, photoId), e);
            if (e.getErrCode() == 101) {
                tagCode = TagCodeEnum.INVALID_PARAMETERS;
            } else if (e.getErrCode() == 102) {
                tagCode = "5112020401";
            }
        } catch (Exception e) {
            log.error(String.format("Error:addShareUnlockHist(userId=%s, actorId=%s, photoId=%s)", userId, actorId, photoId), e);
            tagCode = TagCodeEnum.MODULE_UNKNOWN_RESPCODE;
        }
        result.addProperty(ParameterKeys.TAG_CODE, tagCode);
        return result;
    }

    /**
     * 51120205
     * 获取微信小程序的任务配置
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getTaskConf(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        // 如果存在userId，检验token
        if (userId != 0 && !checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }

        String tagCode = TagCodeEnum.MODULE_UNKNOWN_RESPCODE;
        List<LiveProjectTaskDTO> taskConfiguration = null;
        try {
            taskConfiguration = liveProjectService.getTaskConfiguration(userId == 0 ? null : userId);
        } catch (MelotModuleException e) {
            log.info("Fail:getTaskConfiguration()", e);
            if (e.getErrCode() == 101) {
                tagCode = "5112020501";
            }
        } catch (Exception e) {
            log.error("Error:getTaskConfiguration()", e);
            tagCode = TagCodeEnum.MODULE_UNKNOWN_RESPCODE;
        }

        if (taskConfiguration != null) {
            JsonArray array = new JsonArray();
            JsonObject json = new JsonObject();
            for (LiveProjectTaskDTO liveProjectTaskDTO : taskConfiguration) {
                json.addProperty("taskId", liveProjectTaskDTO.getTaskId());
                json.addProperty("taskName", liveProjectTaskDTO.getTaskName());
                json.addProperty("rewardGiftId", liveProjectTaskDTO.getRewardGiftId());
                if (liveProjectTaskDTO.getFinish() != null) {
                    json.addProperty("isFinish", liveProjectTaskDTO.getFinish());
                }
                array.add(json);
            }
            result.add("taskList", array);
        }
        result.addProperty(ParameterKeys.TAG_CODE, tagCode);
        return result;
    }

    /**
     * 51120206
     * 完成任务
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject finishTask(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();


        // 检验token
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }

        int userId, taskId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, TagCodeEnum.PARAMETER_PARSE_ERROR, 1, Integer.MAX_VALUE);
            taskId = CommonUtil.getJsonParamInt(jsonObject, "taskId", 0, TagCodeEnum.PARAMETER_PARSE_ERROR, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        String tagCode = "5112020603";
        try {
            if (liveProjectService.finishTask(userId, taskId)) {
                tagCode = TagCodeEnum.SUCCESS;
            }
        } catch (MelotModuleException e) {
            log.info(String.format("Fail:finishTask(userId=%s, taskId=%s)", userId, taskId), e);
            if (e.getErrCode() == 101) {
                tagCode = "5112020501";
            } else if (e.getErrCode() == 102) {
                tagCode = "5112020502";
            }
        } catch (Exception e) {
            log.error("Error:getTaskConfiguration()", e);
            tagCode = TagCodeEnum.MODULE_UNKNOWN_RESPCODE;
        }
        result.addProperty(ParameterKeys.TAG_CODE, tagCode);
        return result;
    }

    /**
     * 51120207
     * 获取微信小程序礼物配置
     * 微信小程序礼物的roomSource设置为22，根据roomSource查找对应的栏目（配置只配一个栏目），该栏目配置其微信小程序的礼物列表
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getGiftConf(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        // 微信小程序的roomSource，以此获取礼物配置
        int roomSource = 22;
        String tagCode = TagCodeEnum.MODULE_UNKNOWN_RESPCODE;
        JsonArray jsonArray = new JsonArray();
        try {
            ReturnResult<RoomGiftDTO> giftResult = roomGiftService.getRoomGiftDTOByActorIdAndRoomSourceId(0, roomSource);
            if (giftResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, tagCode);
                return result;
            }

            RoomGiftDTO roomGiftDTO = giftResult.getData();
            if (roomGiftDTO == null || CollectionUtils.isEmpty(roomGiftDTO.getCatalogGiftDTOList())) {
                result.addProperty(ParameterKeys.TAG_CODE, tagCode);
                return result;
            }
            // 获取礼物配置的栏目，栏目取第一个
            CatalogGiftDTO catalogGiftDTO = roomGiftDTO.getCatalogGiftDTOList().get(0);
            if (catalogGiftDTO == null) {
                result.addProperty(ParameterKeys.TAG_CODE, tagCode);
                return result;
            }
            // 设置礼物配置
            if (CollectionUtils.isNotEmpty(catalogGiftDTO.getGiftDTOList())) {
                JsonObject json;
                for (GiftDTO giftDTO : catalogGiftDTO.getGiftDTOList()) {
                    json = new JsonObject();
                    json.addProperty("giftId", giftDTO.getGiftId());
                    jsonArray.add(json);
                }
            }
            result.add("giftArray", jsonArray);
            tagCode = TagCodeEnum.SUCCESS;
        } catch (Exception e) {
            log.error(String.format("Error:getGiftConf(jsonObject=%s, checkTag=%s, request=%s)", jsonObject, checkTag, request), e);
        }
        result.addProperty(ParameterKeys.TAG_CODE, tagCode);
        return result;
    }
}

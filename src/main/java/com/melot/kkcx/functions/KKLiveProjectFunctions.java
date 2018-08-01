package com.melot.kkcx.functions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.melot.kk.liveproject.api.dto.LiveProjectTaskDTO;
import com.melot.kk.liveproject.api.dto.ResPrivatePhotoDTO;
import com.melot.kk.liveproject.api.service.LiveProjectService;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ParameterKeys;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.module.api.exceptions.MelotModuleException;
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


    /**
     * 51050701
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
                if (resPrivatePhotoDTO.getCurrentUnlockNum() != null) {
                    json.addProperty("currentUnlockNum", resPrivatePhotoDTO.getCurrentUnlockNum());
                }
                if (resPrivatePhotoDTO.getUnlockState() != null) {
                    json.addProperty("isUnlock", resPrivatePhotoDTO.getUnlockState() > 0);
                }
                helpUserIds = new JsonArray();
                if (resPrivatePhotoDTO.getHelpUnlockUserIds() != null) {
                    for (Integer helpUserId : resPrivatePhotoDTO.getHelpUnlockUserIds()) {
                        helpUserIds.add(helpUserId);
                    }
                }
                json.add("helpUserIds", helpUserIds);
            }
            result.add("photoList", array);
        }
        result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
        return result;
    }


    /**
     * 51050702
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
            adminOrderNo = CommonUtil.getJsonParamString(jsonObject, "adminOrderNo", null, "5105070201", 1, 100);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        String tagCode = "5105070202";
        try {
            log.info("start check, adminOrderNo=" + adminOrderNo);
            if (liveProjectService.checkUnlockAfterPay(adminOrderNo)) {
                tagCode = TagCodeEnum.SUCCESS;
            }
        } catch (Exception e) {
            log.error(String.format("Error:checkUnlockAfterPay(adminOrderNo=%s)", adminOrderNo), e);
            tagCode = TagCodeEnum.MODULE_UNKNOWN_RESPCODE;
        }
        result.addProperty(ParameterKeys.TAG_CODE, tagCode);
        return result;
    }


    /**
     * 51050703
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

        String tagCode = "5105070202";
        try {
            liveProjectService.shareUnlockPrivatePhoto(histId, userId);
        } catch (MelotModuleException e) {
            log.info(String.format("Fail:shareUnlockPrivatePhoto(histId=%s, userId=%s)", histId, userId), e);
            if (e.getErrCode() == 101) {
                tagCode = "5105070301";
            } else if (e.getErrCode() == 102) {
                tagCode = TagCodeEnum.INVALID_PARAMETERS;
            } else if (e.getErrCode() == 103) {
                tagCode = "5105070302";
            }
        } catch (Exception e) {
            log.error(String.format("Error:shareUnlockPrivatePhoto(histId=%s, userId=%s)", histId, userId), e);
            tagCode = TagCodeEnum.MODULE_UNKNOWN_RESPCODE;
        }
        result.addProperty(ParameterKeys.TAG_CODE, tagCode);
        return result;
    }

    /**
     * 51050704
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
                tagCode = "5105070401";
            }
        } catch (Exception e) {
            log.error(String.format("Error:addShareUnlockHist(userId=%s, actorId=%s, photoId=%s)", userId, actorId, photoId), e);
            tagCode = TagCodeEnum.MODULE_UNKNOWN_RESPCODE;
        }
        result.addProperty(ParameterKeys.TAG_CODE, tagCode);
        return result;
    }

    /**
     * 51050705
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
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, TagCodeEnum.PARAMETER_PARSE_ERROR, 1, Integer.MAX_VALUE);
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
            taskConfiguration = liveProjectService.getTaskConfiguration();
        } catch (MelotModuleException e) {
            log.info("Fail:getTaskConfiguration()", e);
            if (e.getErrCode() == 101) {
                tagCode = "5105070501";
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
                array.add(json);
            }
            result.add("taskList", array);
        }
        result.addProperty(ParameterKeys.TAG_CODE, tagCode);
        return result;
    }

    /**
     * 51050706
     * 获取微信小程序的任务配置
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

        String tagCode = "5105070603";
        try {
            if (liveProjectService.finishTask(userId, taskId)) {
                tagCode = TagCodeEnum.SUCCESS;
            }
        } catch (MelotModuleException e) {
            log.info(String.format("Fail:finishTask(userId=%s, taskId=%s)", userId, taskId), e);
            if (e.getErrCode() == 101) {
                tagCode = "5105070501";
            } else if (e.getErrCode() == 102) {
                tagCode = "5105070502";
            }
        } catch (Exception e) {
            log.error("Error:getTaskConfiguration()", e);
            tagCode = TagCodeEnum.MODULE_UNKNOWN_RESPCODE;
        }
        result.addProperty(ParameterKeys.TAG_CODE, tagCode);
        return result;
    }
}

package com.melot.kkcx.functions;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.melot.kktv.base.Result;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.module.api.constant.ReturnCode;
import com.melot.module.api.dto.*;
import com.melot.module.api.service.DialService;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * Description:
 *
 * @author shoujian
 * Date: 2019-04-18
 * Time: 3:19 PM
 */
public class DialFunction {

    private static Logger logger = Logger.getLogger(DialFunction.class);

    @Resource
    private DialService dialService;

    /**
     * 51140101
     * 获取宝盒礼物描述
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getTreasureBoxGiftMsg(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        int giftId;
        try {
            giftId = CommonUtil.getJsonParamInt(jsonObject, "giftId", 0, "5114010101", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            Result<TreasureBoxMsgDTO> treasureBoxMsgDTOResult = dialService.getTreasureBoxGiftMsg(giftId);
            if (ReturnCode.SUCCESS.getCode().equals(treasureBoxMsgDTOResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                result.addProperty("picUrl", treasureBoxMsgDTOResult.getData().getPicUrl());
                result.addProperty("sendGiftMsgFirst", treasureBoxMsgDTOResult.getData().getSendGiftMsgFirst());
                result.addProperty("sendGiftMsgSecond", treasureBoxMsgDTOResult.getData().getSendGiftMsgSecond());
                result.add("treasureBoxList", new Gson().toJsonTree(treasureBoxMsgDTOResult.getData().getTreasureBoxList()));
            }
        } catch (Exception e) {
            logger.error("Error getTreasureBoxGiftMsg()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }

        return result;

    }

    /**
     * 51140102
     * 获取转盘播报列表
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getDialReport(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        int dialType;
        try {
            dialType = CommonUtil.getJsonParamInt(jsonObject, "dialType", 0, "5114010201", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        try {
            Result<List<DialReportDTO>> dialReportDTOListResult = dialService.getDialReport(dialType);
            if (ReturnCode.SUCCESS.getCode().equals(dialReportDTOListResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                result.add("dialReportList", new Gson().toJsonTree(dialReportDTOListResult.getData()));
            }
        } catch (Exception e) {
            logger.error("Error getTreasureBoxGiftMsg()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }

        return result;
    }

    /**
     * 51140103
     * 获取转盘配置详情
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getDialConfigDetail(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

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
            Result<DialConfigDetailDTO> dialConfigDetailDTOResult = dialService.getDialConfigDetail(userId);
            if (ReturnCode.SUCCESS.getCode().equals(dialConfigDetailDTOResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                result.addProperty("reportState", dialConfigDetailDTOResult.getData().getReportState());
                result.addProperty("happyTicketTotal", dialConfigDetailDTOResult.getData().getHappyTicketTotal());
                result.addProperty("validityTime", dialConfigDetailDTOResult.getData().getValidityTime());
                result.add("drawConfigDTOList", new Gson().toJsonTree(dialConfigDetailDTOResult.getData().getDrawConfigDTOList()));
                result.add("dialConfigDTOList", new Gson().toJsonTree(dialConfigDetailDTOResult.getData().getDialConfigDTOList()));
            }
        } catch (Exception e) {
            logger.error("Error getTreasureBoxGiftMsg()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }

        return result;
    }

    /**
     * 51140104
     * 抽奖
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject draw(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        int userId, dialType, total, platform;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            dialType = CommonUtil.getJsonParamInt(jsonObject, "dialType", 0, "5114010404", 1, Integer.MAX_VALUE);
            total = CommonUtil.getJsonParamInt(jsonObject, "total", 0, "5114010405", 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, TagCodeEnum.PLATFORM_MISSING, 1, Integer.MAX_VALUE);

        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            Result<DrawResultDTO> drawResultDTOResult = dialService.draw(userId, platform, dialType, total);
            if (ReturnCode.SUCCESS.getCode().equals(drawResultDTOResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                result.addProperty("total", drawResultDTOResult.getData().getTotal());
                result.addProperty("happyTotal", drawResultDTOResult.getData().getHappyTotal());
                result.add("dialGiftDTOList", new Gson().toJsonTree(drawResultDTOResult.getData().getDialGiftDTOList()));
            } else if (ReturnCode.ERROR_HAPPY_TICKET_NOT_ENOUGH.getCode().equals(drawResultDTOResult.getCode())) {
                result.addProperty("TagCode", "5114010401");
            } else if (ReturnCode.ERROR_HAPPY_TICKET_TIMEOUT.getCode().equals(drawResultDTOResult.getCode())) {
                result.addProperty("TagCode", "5114010402");
            } else if (ReturnCode.ERROR_HAPPY_TICKET_CONSUME.getCode().equals(drawResultDTOResult.getCode())) {
                result.addProperty("TagCode", "5114010403");
            } else if (drawResultDTOResult.getCode().equals("1011")) {
                result.addProperty("TagCode","5114010406");
            }
        } catch (Exception e) {
            logger.error("Error getTreasureBoxGiftMsg()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }

        return result;
    }

    /**
     * 51140105
     * 播报开关
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject reportSwitch(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        int userId, type;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            type = CommonUtil.getJsonParamInt(jsonObject, "type", 0, "5114010501", 0, Integer.MAX_VALUE);

        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            Result<Boolean> booleanResult = dialService.reportSwitch(userId, type);
            if (ReturnCode.SUCCESS.getCode().equals(booleanResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                result.addProperty("result", booleanResult.getData());
            }
        } catch (Exception e) {
            logger.error("Error getTreasureBoxGiftMsg()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }

        return result;
    }

    /**
     * 51140106
     * 送礼返利详情
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject sendGiftRebateDetail(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        try {
            Result<GiftRebateDetailDTO> giftRebateDetailDTOResult = dialService.sendGiftRebateDetail();
            if (ReturnCode.SUCCESS.getCode().equals(giftRebateDetailDTOResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                result.addProperty("giftId", giftRebateDetailDTOResult.getData().getGiftId());
                result.add("giftRebateDTOList", new Gson().toJsonTree(giftRebateDetailDTOResult.getData().getGiftRebateDTOList()));
            }
        } catch (Exception e) {
            logger.error("Error getTreasureBoxGiftMsg()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }

        return result;
    }

    /**
     * 51140107
     * 获取用户大事件配置详情
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getUserBigEventDetail(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

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
            Result<UserBigEventDetailDTO> userBigEventDetailDTOResult = dialService.getUserBigEventDetail(userId);
            if (ReturnCode.SUCCESS.getCode().equals(userBigEventDetailDTOResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                result.addProperty("rightsState", userBigEventDetailDTOResult.getData().getRightsState());
                result.addProperty("validContent", userBigEventDetailDTOResult.getData().getValidContent());
                result.addProperty("checkContent", userBigEventDetailDTOResult.getData().getCheckContent());
                result.addProperty("daySubmit", userBigEventDetailDTOResult.getData().getDaySubmit());
                result.addProperty("total", userBigEventDetailDTOResult.getData().getTotal());
                result.addProperty("state", userBigEventDetailDTOResult.getData().getState());
            }
        } catch (Exception e) {
            logger.error("Error getTreasureBoxGiftMsg()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }


        return result;
    }

    /**
     * 51140108
     * 修改用户大事件配置
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject changeBigEventContent(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        int userId;
        String content;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            content = CommonUtil.getJsonParamString(jsonObject, "content", null, "5114010802", 1, 30);

        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            Result<Boolean> booleanResult = dialService.changeBigEventContent(userId, content);
            if (ReturnCode.SUCCESS.getCode().equals(booleanResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                result.addProperty("result", booleanResult.getData());
            } else if (ReturnCode.ERROR_BIG_EVENT_ILLEGALITY.getCode().equals(booleanResult.getCode())) {
                result.addProperty("TagCode", "5114010801");
            } else if (ReturnCode.ERROR_BIG_EVENT_RIGHTS.getCode().equals(booleanResult.getCode())) {
                result.addProperty("TagCode", "5114010802");
            } else if (ReturnCode.ERROR_BIG_EVENT_TIMES_LIMIT.getCode().equals(booleanResult.getCode())) {
                result.addProperty("TagCode", "5114010803");
            } else if (ReturnCode.ERROR_BIG_EVENT_CONTENT_REPEAT.getCode().equals(booleanResult.getCode())) {
                result.addProperty("TagCode", "5114010804");
            }
        } catch (Exception e) {
            logger.error("Error getTreasureBoxGiftMsg()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }

        return result;
    }

    /**
     * 51140109
     * 大事件文案列表
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject listBigEventContent(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

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
            Result<List<BigEventContentDTO>> bigEventContentDTOListResult = dialService.listBigEventContent(userId);
            if (ReturnCode.SUCCESS.getCode().equals(bigEventContentDTOListResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                result.add("bigEventContentDTOList", new Gson().toJsonTree(bigEventContentDTOListResult.getData()));
            }
        } catch (Exception e) {
            logger.error("Error getTreasureBoxGiftMsg()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }

        return result;
    }

    /**
     * 51140110
     * 宝盒入口
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject treasureBoxEntrance(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        try {
            Result<List<TreasureBoxDTO>> treasureBoxDTOListResult = dialService.getTreasureBoxEntrance();
            if (ReturnCode.SUCCESS.getCode().equals(treasureBoxDTOListResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                result.add("treasureBoxDTOList", new Gson().toJsonTree(treasureBoxDTOListResult.getData()));
            }
        } catch (Exception e) {
            logger.error("Error getTreasureBoxGiftMsg()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
        return result;
    }

}

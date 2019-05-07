package com.melot.kkcx.functions;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.kk.asset.api.dto.UserHappyTicketDTO;
import com.melot.kk.asset.api.dto.UserHappyTicketHistoryDTO;
import com.melot.kk.asset.api.service.HappyTicketService;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ParameterKeys;
import com.melot.kktv.util.TagCodeEnum;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Title: 欢乐券相关接口
 * <p>
 * Description:
 * </p>
 *
 * @author 王贺<a   href = " mailto:he.wang @ melot.cn " />
 * @version V1.0
 * @since 2019/04/22.
 */
public class HappyTicketFunctions {

    @Resource
    HappyTicketService happyTicketService;

    /**
     * 获取用户欢乐券余额(51140001)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getUserHappyTickets(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

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

        UserHappyTicketDTO userHappyTicket = happyTicketService.getUserHappyTicket(userId);
        if (userHappyTicket != null) {
            result.addProperty("happyTicket", userHappyTicket.getAmount());
            result.addProperty("lastAddTime", userHappyTicket.getLastAddTime().getTime());
        }

        result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);

        return result;
    }

    /**
     * 获取用户欢乐券流水(51140002)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject listUserHappyTicketsHistory(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId, type, start, count;
        long startTime, endTime;

        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, TagCodeEnum.USERID_MISSING, 100, Integer.MAX_VALUE);
            type = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.TYPE, 1, null, 1, 2);
            start = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.START, 0, null, 0, Integer.MAX_VALUE);
            count = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.COUNT, 20, null, 0, 100);
            startTime = CommonUtil.getJsonParamLong(jsonObject, "startTime", 0, TagCodeEnum.STARTTIME_MISSING, 0, Long.MAX_VALUE);
            endTime = CommonUtil.getJsonParamLong(jsonObject, "endTime", 0, TagCodeEnum.ENDTIME_MISSING, 0, Long.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        // 间隔时间不超过90天
        if (endTime - startTime >= 90 * 24 * 60 * 60 * 1000L) {
            result.addProperty(ParameterKeys.TAG_CODE, "5114000201");
            return result;
        }

        int totalCount = happyTicketService.countUserHappyTicketHistory(userId, type, startTime, endTime);
        List<UserHappyTicketHistoryDTO> userHappyTicketHistoryDTOS = happyTicketService.listUserHappyTicketHistory(userId, type, startTime, endTime, start, count);

        JsonArray jsonArray = new JsonArray();
        for (UserHappyTicketHistoryDTO userHappyTicketHistoryDTO : userHappyTicketHistoryDTOS) {
            JsonObject object = new JsonObject();
            object.addProperty("histId", userHappyTicketHistoryDTO.getHistId());
            object.addProperty("userId", userHappyTicketHistoryDTO.getUserId());
            object.addProperty("ntype", userHappyTicketHistoryDTO.getNtype());
            object.addProperty("ntypeDesc", userHappyTicketHistoryDTO.getNtypeDesc());
            object.addProperty("amount", userHappyTicketHistoryDTO.getAmount());
            object.addProperty("dtime", userHappyTicketHistoryDTO.getDtime());
            object.addProperty("note", userHappyTicketHistoryDTO.getNote());

            jsonArray.add(object);
        }

        result.addProperty("totalCount", totalCount);
        result.add("historyList", jsonArray);
        result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);

        return result;
    }
}

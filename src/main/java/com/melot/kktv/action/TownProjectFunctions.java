package com.melot.kktv.action;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.kk.location.api.dto.AreaCodeDTO;
import com.melot.kk.town.api.param.TownWorkParam;
import com.melot.kk.town.api.service.TownWorkService;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.TagCodeEnum;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.melot.kktv.util.ParamCodeEnum.*;

public class TownProjectFunctions {

    private static Logger logger = Logger.getLogger(TownProjectFunctions.class);

    @Resource
    TownWorkService townWorkService;

    /**
     * 	获取本地新鲜的(作品、话题、直播间)列表【51120103】
     */
    public JsonObject getLocalFreshList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        String areaCode;
        int pageIndex, countPerPage;
        try {
            areaCode = CommonUtil.getJsonParamString(jsonObject, AREA_CODE.getId(), null, AREA_CODE.getErrorCode(), 1, Integer.MAX_VALUE);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 10, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {

            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error getLocalFreshList()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 发布作品【51120104】
     */
    public JsonObject publishWork(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        String areaCode, workUrl, topicName, workDesc;
        Integer userId, workType, topicId;
        try {
            areaCode = CommonUtil.getJsonParamString(jsonObject, AREA_CODE.getId(), null, AREA_CODE.getErrorCode(), 1, Integer.MAX_VALUE);
            workUrl = CommonUtil.getJsonParamString(jsonObject, WORK_URL.getId(), null, WORK_URL.getErrorCode(), 1, Integer.MAX_VALUE);
            topicName = CommonUtil.getJsonParamString(jsonObject, TOPIC_NAME.getId(), null, null, 1, Integer.MAX_VALUE);
            workDesc = CommonUtil.getJsonParamString(jsonObject, WORK_DESC.getId(), null, null, 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, USER_ID.getId(), 0, USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            workType = CommonUtil.getJsonParamInt(jsonObject, WORK_TYPE.getId(), 2, WORK_TYPE.getErrorCode(), 1, Integer.MAX_VALUE);
            topicId = CommonUtil.getJsonParamInt(jsonObject, TOPIC_ID.getId(), 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {

            TownWorkParam townWorkParam = new TownWorkParam();
            townWorkParam.setUserId(userId);
            List<AreaCodeDTO> areaCodeDTOList = townWorkService.publishWork(areaCode);
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

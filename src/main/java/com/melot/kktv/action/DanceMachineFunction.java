package com.melot.kktv.action;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.SecurityFunctions;
import com.melot.kktv.util.TagCodeEnum;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;

/**
 * Title: DanceMachineFunction
 * <p>
 * Description: 欢乐跳舞机相关接口
 * </p>
 * 
 * @author shengjian
 * @version V1.0
 * @since 2017年10月12日
 */
public class DanceMachineFunction {

    private static Logger logger = Logger.getLogger(DanceMachineFunction.class);

    /**
     * 获取准备游戏信息【51060301】
     */
    public JsonObject getReadyGameInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {

            JsonArray musicList = new JsonArray();

            result.addProperty("gameDownloadUrl", "");
            result.addProperty("gameVersion", "1.0.0");
            result.add("musicList", musicList);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error getTitleList()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }

    /**
     * 	获取歌曲排行榜【51060302】
     */
    public JsonObject getRankingList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        int pageIndex, countPerPage;
        try {
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 0, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 10, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {

            JsonArray musicList = new JsonArray();

            result.addProperty("count", 0);
            result.add("musicList", musicList);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error getCatchDollRoomList()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }

    /**
     * 获取单曲排行榜(排行榜得分前10用户)【51060303】
     */
    public JsonObject getSingleRankingList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        int musicId;
        try {
            musicId = CommonUtil.getJsonParamInt(jsonObject, "musicId", 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        if(musicId == 0) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_MISSING);
            return result;
        }

        try {


            JsonArray singleRankingList = new JsonArray();

            result.addProperty("musicId", musicId);
            result.addProperty("musicName", "");
            result.addProperty("singer", "");
            result.addProperty("musicLength", 100);
            result.add("roomList", singleRankingList);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error getCatchDollRoomList()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }

    /**
     * 获取准备游戏信息【52060304】
     */
    public JsonObject saveGameResult(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId, musicId, totalScore, combo;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
            musicId = CommonUtil.getJsonParamInt(jsonObject, "musicId", 0, null, 1, Integer.MAX_VALUE);
            totalScore = CommonUtil.getJsonParamInt(jsonObject, "totalScore", 0, null, 1, Integer.MAX_VALUE);
            combo = CommonUtil.getJsonParamInt(jsonObject, "combo", 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        if(userId == 0 || musicId == 0) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_MISSING);
            return result;
        }

        JsonObject rtJO = null;
        try {
            rtJO = SecurityFunctions.checkSignedValue(jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(rtJO != null) {
            return rtJO;
        }

        try {

            result.addProperty("ranking", 10);
            result.addProperty("newRecord", true);
            result.addProperty("percentage", 95);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error getCatchDollRoomList()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }

}

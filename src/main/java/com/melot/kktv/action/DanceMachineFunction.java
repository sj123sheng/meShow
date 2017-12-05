package com.melot.kktv.action;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.kk.dance.api.constant.DanceModeConstant;
import com.melot.kk.dance.api.domain.*;
import com.melot.kk.dance.api.service.DanceService;
import com.melot.kktv.base.CommonStateCode;
import com.melot.kktv.base.Result;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.SecurityFunctions;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.sdk.core.util.MelotBeanFactory;
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

//        int userId;
//        try {
//            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
//        } catch (CommonUtil.ErrorGetParameterException e) {
//            result.addProperty("TagCode", e.getErrCode());
//            return result;
//        } catch (Exception e) {
//            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
//            return result;
//        }

        try {
            DanceService danceService = (DanceService)MelotBeanFactory.getBean("danceService");
            Result<DanceGameInfo> danceGameInfoResult = danceService.getDanceGameInfo();
            if(danceGameInfoResult != null && danceGameInfoResult.getCode().equals(CommonStateCode.SUCCESS) && danceGameInfoResult.getData() != null){
                DanceGameInfo danceGameInfo = danceGameInfoResult.getData();
                result.addProperty("gameDownloadUrl", danceGameInfo.getGameDownloadUrl());
                result.addProperty("gameVersion", danceGameInfo.getGameVersion());
                result.add("musicList", new Gson().toJsonTree(danceGameInfo.getMusicList()));
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            }
            else {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
        } catch (Exception e) {
            logger.error("Error getReadyGameInfo()", e);
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
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 10, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
            if(pageIndex == 0){
                pageIndex = 1;
            }
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            DanceService danceService = (DanceService)MelotBeanFactory.getBean("danceService");
            Result<DanceMusicPage> danceMusicPageResult = danceService.getRankingList(countPerPage*(pageIndex -1),countPerPage);
            if(danceMusicPageResult != null && danceMusicPageResult.getCode().equals(CommonStateCode.SUCCESS) && danceMusicPageResult.getData() != null){
                DanceMusicPage danceMusicPage = danceMusicPageResult.getData();
                result.addProperty("count", danceMusicPage.getCount());
                result.add("musicList", new Gson().toJsonTree(danceMusicPage.getMusicList()));
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
                return result;
            }
            else {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }

        } catch (Exception e) {
            logger.error("Error getRankingList()", e);
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

            DanceService danceService = (DanceService)MelotBeanFactory.getBean("danceService");
            Result<DanceMusic> danceMusicResult =  danceService.getSingleRankingList(musicId);
            if(danceMusicResult != null && danceMusicResult.getCode().equals(CommonStateCode.SUCCESS) && danceMusicResult.getData() != null){
                DanceMusic danceMusic = danceMusicResult.getData();
                result.addProperty("musicId", danceMusic.getMusicId());
                result.addProperty("musicName", danceMusic.getMusicName());
                result.addProperty("singer", danceMusic.getSinger());
                result.addProperty("musicLength", danceMusic.getMusicLength());
                result.add("singleRankingList", new Gson().toJsonTree(danceMusic.getRankList()));
                result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            }
            else {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
        } catch (Exception e) {
            logger.error("Error getSingleRankingList()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }

    /**
     * 保存游戏结果【52060304】
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

//        JsonObject rtJO = null;
//        try {
//            rtJO = SecurityFunctions.checkSignedValue(jsonObject);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        if(rtJO != null) {
//            return rtJO;
//        }

        try {
            DanceService danceService = (DanceService)MelotBeanFactory.getBean("danceService");
            HisDance hisDance = new HisDance();
            hisDance.setMusicId(musicId);
            hisDance.setUserId(userId);
            hisDance.setScore(totalScore);
            hisDance.setCombo(combo);
            hisDance.setDanceMode(DanceModeConstant.Single);
            Result<DanceResult> danceResult = danceService.addHisDance(hisDance);
            if(danceResult != null && danceResult.getCode().equals(CommonStateCode.SUCCESS) && danceResult.getData() != null){
                result.addProperty("ranking", danceResult.getData().getRanking());
                result.addProperty("newRecord", danceResult.getData().getNewRecord());
                result.addProperty("percentage", danceResult.getData().getPercentage());
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            }
            else{
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            }
            return result;
        } catch (Exception e) {
            logger.error("Error saveGameResult()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }

}

package com.melot.kktv.action;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.kk.nationalPK.api.domain.DO.ConfLadderMatchDO;
import com.melot.kk.nationalPK.api.domain.DO.HistActorLadderMatchDO;
import com.melot.kk.nationalPK.api.domain.DO.ResActorLadderMatchDO;
import com.melot.kk.nationalPK.api.service.ConfLadderMatchService;
import com.melot.kk.nationalPK.api.service.HistActorLadderMatchService;
import com.melot.kk.nationalPK.api.service.ResActorLadderMatchService;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kktv.base.CommonStateCode;
import com.melot.kktv.base.Result;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.TagCodeEnum;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

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
public class HappyPKFunction {

    private static Logger logger = Logger.getLogger(HappyPKFunction.class);

    @Resource
    private ConfLadderMatchService confLadderMatchService;

    @Resource
    private ResActorLadderMatchService resActorLadderMatchService;

    @Resource
    private HistActorLadderMatchService histActorLadderMatchService;

    @Resource
    private KkUserService kkUserService;

    /**
     * 获取天梯赛当前赛季信息【51060401】
     */
    public JsonObject getCurrentSeasonInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        try {

            Result<ConfLadderMatchDO> currentSeasonConf = confLadderMatchService.getCurrentSeasonConf();
            if(currentSeasonConf.getCode().equals(CommonStateCode.SUCCESS) && currentSeasonConf.getData() != null){

                ConfLadderMatchDO confLadderMatchDO = currentSeasonConf.getData();

                result.addProperty("seasonId", confLadderMatchDO.getSeasonId());
                result.addProperty("seasonName", confLadderMatchDO.getSeasonName());
                result.addProperty("remainingTime", confLadderMatchDO.getRemainingTime());
                result.addProperty("goldPool", confLadderMatchDO.getBonusPool());
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            }else {
                result.addProperty("TagCode", TagCodeEnum.LADDER_MATCH_UN_CONF);
                return result;
            }
        } catch (Exception e) {
            logger.error("Error getCurrentSeasonInfo()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 	获取我的天梯赛当前赛季战绩【51060402】
     */
    public JsonObject getMyLadderMatchRecord(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

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

        if(userId == 0) {
            result.addProperty("TagCode", TagCodeEnum.USERID_MISSING);
            return result;
        }

        try {

            Result<ResActorLadderMatchDO> resActorLadderMatchDOResult = resActorLadderMatchService.getResActorLadderMatch(userId);
            if(resActorLadderMatchDOResult.getCode().equals(CommonStateCode.SUCCESS) && resActorLadderMatchDOResult.getData() != null){

                ResActorLadderMatchDO resActorLadderMatchDO = resActorLadderMatchDOResult.getData();
                UserProfile userProfile = kkUserService.getUserProfile(userId);

                result.addProperty("userId", userId);
                if(userProfile != null) {
                    int isActor = userProfile.getIsActor();
                    // 如果该用户不是主播 返回错误码
                    if(isActor == 0) {
                        result.addProperty("TagCode", TagCodeEnum.NOT_ACTOR);
                        return result;
                    }
                    result.addProperty("gender", userProfile.getGender());
                    result.addProperty("portrait", getPortrait(userProfile));
                    result.addProperty("nickname", userProfile.getNickName());
                }

                if(resActorLadderMatchDO != null) {
                    result.addProperty("integral", resActorLadderMatchDO.getLadderMatchIntegral());
                    result.addProperty("time", resActorLadderMatchDO.getLadderMatchTime());
                    result.addProperty("gameDan", resActorLadderMatchDO.getGameDan());
                    result.addProperty("gameDanName", resActorLadderMatchDO.getGameDanName());
                    result.addProperty("winningRate", resActorLadderMatchDO.getWinningRate());
                    result.addProperty("ranking", resActorLadderMatchDO.getRanking());
                    result.addProperty("nextLevelGameDan", resActorLadderMatchDO.getNextLevelGameDan());
                    result.addProperty("nextLevelGameDanName", resActorLadderMatchDO.getNextLevelGameDanName());
                    result.addProperty("nextLevelIntegral", resActorLadderMatchDO.getNextLevelIntegral());
                }

                result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);

                return result;
            } else {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }

        } catch (Exception e) {
            logger.error("Error getMyLadderMatchRecord()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取我的最近10场天梯赛【51060403】
     */
    public JsonObject getMyRecentLadderMatch(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

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

        if(userId == 0) {
            result.addProperty("TagCode", TagCodeEnum.USERID_MISSING);
            return result;
        }

        try {

            Result<List<HistActorLadderMatchDO>> histListResult =  histActorLadderMatchService.getRecentHistActorLadderMatchList(userId);
            if(histListResult.getCode().equals(CommonStateCode.SUCCESS) && histListResult.getData() != null){

                List<HistActorLadderMatchDO> histActorLadderMatchDOS = histListResult.getData();

                JsonArray ladderMatchRecordList = new JsonArray();
                for(HistActorLadderMatchDO histActorLadderMatchDO : histActorLadderMatchDOS) {

                    int opponentUserId = histActorLadderMatchDO.getOpponentActorId();
                    UserProfile userProfile = kkUserService.getUserProfile(opponentUserId);

                    JsonObject ladderMatchRecord = new JsonObject();

                    ladderMatchRecord.addProperty("opponentUserId", opponentUserId);
                    if(userProfile != null) {
                        ladderMatchRecord.addProperty("opponentGender", userProfile.getGender());
                        ladderMatchRecord.addProperty("opponentPortrait", getPortrait(userProfile));
                        ladderMatchRecord.addProperty("opponentNickname", userProfile.getNickName());
                    }
                    ladderMatchRecord.addProperty("ladderMatchResult", histActorLadderMatchDO.getLadderMatchResult());
                    ladderMatchRecord.addProperty("receiveScore", histActorLadderMatchDO.getReceiveScore());

                    ladderMatchRecordList.add(ladderMatchRecord);
                }
                result.add("ladderMatchRecordList", ladderMatchRecordList);

                result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            } else {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
        } catch (Exception e) {
            logger.error("Error getMyRecentLadderMatch()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取天梯榜【51060404】
     */
    public JsonObject getLadderChart(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        int pageIndex, countPerPage;
        try {
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 10, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {

            Result<List<ResActorLadderMatchDO>> resListResult =  resActorLadderMatchService.getLadderChart(pageIndex, countPerPage);
            if(resListResult.getCode().equals(CommonStateCode.SUCCESS) && resListResult.getData() != null){

                List<ResActorLadderMatchDO> resActorLadderMatchDOS = resListResult.getData();

                JsonArray ladderChart = new JsonArray();
                for(ResActorLadderMatchDO resActorLadderMatchDO : resActorLadderMatchDOS) {

                    int actorId = resActorLadderMatchDO.getActorId();
                    UserProfile userProfile = kkUserService.getUserProfile(actorId);

                    JsonObject resActorLadderMatch = new JsonObject();

                    resActorLadderMatch.addProperty("userId", actorId);
                    if(userProfile != null) {
                        resActorLadderMatch.addProperty("gender", userProfile.getGender());
                        resActorLadderMatch.addProperty("portrait", getPortrait(userProfile));
                        resActorLadderMatch.addProperty("nickname", userProfile.getNickName());
                    }
                    resActorLadderMatch.addProperty("ranking", resActorLadderMatchDO.getRanking());
                    resActorLadderMatch.addProperty("integral", resActorLadderMatchDO.getLadderMatchIntegral());
                    resActorLadderMatch.addProperty("gameDan", resActorLadderMatchDO.getGameDan());
                    resActorLadderMatch.addProperty("gameDanName", resActorLadderMatchDO.getGameDanName());
                    resActorLadderMatch.addProperty("winningRate", resActorLadderMatchDO.getWinningRate());
                    resActorLadderMatch.addProperty("time", resActorLadderMatchDO.getLadderMatchTime());

                    ladderChart.add(resActorLadderMatch);
                }
                result.add("ladderChart", ladderChart);

                result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            } else {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
        } catch (Exception e) {
            logger.error("Error getLadderChart()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取上赛季最强王者的前五名【51060405】
     */
    public JsonObject getLastSeasonStrongestKingTop5(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        try {

            Result<List<ResActorLadderMatchDO>> resListResult =  resActorLadderMatchService.getLastSeasonStrongestKingTop5();
            if(resListResult.getCode().equals(CommonStateCode.SUCCESS) && resListResult.getData() != null){

                List<ResActorLadderMatchDO> resActorLadderMatchDOS = resListResult.getData();

                JsonArray lastSeasonStrongestKingTop5 = new JsonArray();
                for(ResActorLadderMatchDO resActorLadderMatchDO : resActorLadderMatchDOS) {

                    int actorId = resActorLadderMatchDO.getActorId();
                    UserProfile userProfile = kkUserService.getUserProfile(actorId);

                    JsonObject resActorLadderMatch = new JsonObject();

                    resActorLadderMatch.addProperty("userId", actorId);
                    if(userProfile != null) {
                        resActorLadderMatch.addProperty("gender", userProfile.getGender());
                        resActorLadderMatch.addProperty("portrait", getPortrait(userProfile));
                        resActorLadderMatch.addProperty("nickname", userProfile.getNickName());
                    }
                    resActorLadderMatch.addProperty("ranking", resActorLadderMatchDO.getRanking());
                    resActorLadderMatch.addProperty("integral", resActorLadderMatchDO.getLadderMatchIntegral());
                    resActorLadderMatch.addProperty("gameDan", resActorLadderMatchDO.getGameDan());
                    resActorLadderMatch.addProperty("gameDanName", resActorLadderMatchDO.getGameDanName());
                    resActorLadderMatch.addProperty("winningRate", resActorLadderMatchDO.getWinningRate());
                    resActorLadderMatch.addProperty("time", resActorLadderMatchDO.getLadderMatchTime());
                    resActorLadderMatch.addProperty("showMoneyCount", resActorLadderMatchDO.getShowMoneyCount());

                    lastSeasonStrongestKingTop5.add(resActorLadderMatch);
                }
                result.add("lastSeasonStrongestKingTop5", lastSeasonStrongestKingTop5);

                result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            } else {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
        } catch (Exception e) {
            logger.error("Error getLadderChart()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 保存天梯记录【51060406】
     */
    public JsonObject saveLadderMatchRecord(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        int actorId, opponentActorId, time;
        long receiveShowMoney, opponentReceiveShowMoney;
        try {
            actorId = CommonUtil.getJsonParamInt(jsonObject, "actorId", 1, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
            opponentActorId = CommonUtil.getJsonParamInt(jsonObject, "opponentActorId", 0, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
            time = CommonUtil.getJsonParamInt(jsonObject, "time", 1, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
            receiveShowMoney = CommonUtil.getJsonParamLong(jsonObject, "receiveShowMoney", 0, null, Long.MIN_VALUE, Long.MAX_VALUE);
            opponentReceiveShowMoney = CommonUtil.getJsonParamLong(jsonObject, "opponentReceiveShowMoney", 0, null, Long.MIN_VALUE, Long.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {

            for(int i = 0 ; i < time ; i++) {
                histActorLadderMatchService.addHistActorLadderMatch(actorId, opponentActorId, receiveShowMoney, opponentReceiveShowMoney, 1);
            }

            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;

        } catch (Exception e) {
            logger.error("Error getLadderChart()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    private String getPortrait(UserProfile userProfile) {
        return userProfile.getPortrait() == null ? null : userProfile.getPortrait() + "!128";
    }

}

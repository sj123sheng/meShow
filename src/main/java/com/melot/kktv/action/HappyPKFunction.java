package com.melot.kktv.action;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.kk.nationalPK.api.domain.DO.*;
import com.melot.kk.nationalPK.api.service.ConfLadderMatchService;
import com.melot.kk.nationalPK.api.service.HistActorLadderMatchService;
import com.melot.kk.nationalPK.api.service.ResActorLadderMatchService;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kktv.base.CommonStateCode;
import com.melot.kktv.base.Result;
import com.melot.kktv.service.ConfigService;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.TagCodeEnum;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.melot.kktv.util.ParamCodeEnum.ACTOR_ID;

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
    
    @Autowired
    private ConfigService configService;

    private static final String REGEX = ",";

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
     * 获取主播的胜场贡献榜【51060407】
     */
    public JsonObject getWinningContributionList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        int actorId;
        try {
            actorId = CommonUtil.getJsonParamInt(jsonObject, ACTOR_ID.getId(), 0, ACTOR_ID.getErrorCode(), 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {

            Result<List<ContributionUserDO>> listResult =  histActorLadderMatchService.getMaxContributionUserList(actorId);
            if(listResult.getCode().equals(CommonStateCode.SUCCESS) && listResult.getData() != null){

                List<ContributionUserDO> contributionUserDOS = listResult.getData();

                JsonArray winningContributionList = new JsonArray();
                for(ContributionUserDO contributionUserDO : contributionUserDOS) {

                    int userId = contributionUserDO.getUserId();
                    UserProfile userProfile = kkUserService.getUserProfile(userId);

                    JsonObject jsonObject1 = new JsonObject();

                    jsonObject1.addProperty("userId", userId);
                    if(userProfile != null) {
                        jsonObject1.addProperty("gender", userProfile.getGender());
                        jsonObject1.addProperty("portrait", getPortrait(userProfile));
                        jsonObject1.addProperty("nickname", userProfile.getNickName());
                    }
                    jsonObject1.addProperty("contributionWinNum", contributionUserDO.getContributionWinNum());

                    winningContributionList.add(jsonObject1);
                }
                result.add("winningContributionList", winningContributionList);

                result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            } else {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
        } catch (Exception e) {
            logger.error("Error getWinningContributionList()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取天梯赛富豪榜【51060408】
     */
    public JsonObject getRichList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        try {

            Result<List<ConsumeUserDO>> listResult =  histActorLadderMatchService.getRichList();
            if(listResult.getCode().equals(CommonStateCode.SUCCESS) && listResult.getData() != null){

                JsonArray richList = new JsonArray();
                List<ConsumeUserDO> consumeUserDOS = listResult.getData();

                int ranking = 1;
                for(ConsumeUserDO consumeUserDO : consumeUserDOS) {

                    int userId = consumeUserDO.getUserId();
                    UserProfile userProfile = kkUserService.getUserProfile(userId);

                    JsonObject jsonObject1 = new JsonObject();

                    jsonObject1.addProperty("userId", userId);
                    if(userProfile != null) {
                        jsonObject1.addProperty("gender", userProfile.getGender());
                        jsonObject1.addProperty("portrait", getPortrait(userProfile));
                        jsonObject1.addProperty("nickname", userProfile.getNickName());
                    }
                    jsonObject1.addProperty("ranking", ++ranking);
                    jsonObject1.addProperty("consumeShowMoneyNum", consumeUserDO.getConsumeShowMoneyNum());

                    richList.add(jsonObject1);
                }
                result.add("richList", richList);

                result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            } else {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
        } catch (Exception e) {
            logger.error("Error getRichList()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    private String getPortrait(UserProfile userProfile) {
        return userProfile.getPortrait() == null ? null : userProfile.getPortrait() + "!128";
    }
    
    public JsonObject isCompere(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

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

        String pkCompereIds = configService.getPkCompereIds().trim();
        
        // 默认是不通过
        result.addProperty("isCompere", 0);
        
        // 配置文件设置为0 或者不设置 所有用户都通过
        if (StringUtil.strIsNull(pkCompereIds) || "0".equals(pkCompereIds)) {
            result.addProperty("isCompere", 1);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        }
        
        // 设置了白名单并且在白名单列表中，允许为主持人
        String[] pkCompereIdList = pkCompereIds.split(REGEX);
        if (pkCompereIdList != null && pkCompereIdList.length > 0) {
            for (String string : pkCompereIdList) {
                if (Integer.valueOf(string).equals(userId)) {
                    result.addProperty("isCompere", 1);
                    break;
                }
            }
        }
        
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
}

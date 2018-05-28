package com.melot.kktv.action;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.kk.guess.api.constant.GuessLotteryStatusEnum;
import com.melot.kk.guess.api.constant.GuessResultEnum;
import com.melot.kk.guess.api.constant.SeasonTypeEnum;
import com.melot.kk.guess.api.dto.*;
import com.melot.kk.guess.api.service.GuessAccountService;
import com.melot.kk.guess.api.service.GuessConfService;
import com.melot.kk.guess.api.service.GuessHistService;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kktv.base.CommonStateCode;
import com.melot.kktv.base.Result;
import com.melot.kktv.service.ConfigService;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.DateUtils;
import com.melot.kktv.util.TagCodeEnum;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

public class GuessFunctions {

    private static Logger logger = Logger.getLogger(GuessFunctions.class);

    @Resource
    GuessConfService guessConfService;

    @Resource
    GuessHistService guessHistService;

    @Resource
    GuessAccountService guessAccountService;

    @Resource
    KkUserService kkUserService;

    @Autowired
    ConfigService configService;

    /**
     * 	获取我的天梯赛当前赛季战绩【51050601】
     */
    public JsonObject getUserGuessHistory(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

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

            Result<List<HistUserGuessDTO>> histListResult =  guessHistService.getUserGuessHistList(userId);
            if(histListResult.getCode().equals(CommonStateCode.SUCCESS) && histListResult.getData() != null){

                List<HistUserGuessDTO> histUserGuessDTOList = histListResult.getData();

                JsonArray guessHistoryList = new JsonArray();
                for(HistUserGuessDTO histUserGuessDTO : histUserGuessDTOList) {

                    JsonObject guessHistory = new JsonObject();
                    int seasonType = histUserGuessDTO.getSeasonType();
                    Long rewardBonusAmount = histUserGuessDTO.getRewardBonusAmount();

                    guessHistory.addProperty("guessHistId", histUserGuessDTO.getGuessHistId());
                    guessHistory.addProperty("seasonId", histUserGuessDTO.getSeasonId());
                    guessHistory.addProperty("seasonType", seasonType);
                    guessHistory.addProperty("seasonName", histUserGuessDTO.getSeasonName());
                    guessHistory.addProperty("guessTime", histUserGuessDTO.getCreateTime().getTime());
                    guessHistory.addProperty("supportGuessItemName", histUserGuessDTO.getSupportGuessItemName());

                    String guessResultDesc = "-";
                    int guessResult = histUserGuessDTO.getGuessResult() == null ? 0 : histUserGuessDTO.getGuessResult();
                    if(guessResult == GuessResultEnum.NOT_SET_RESULT) {
                        guessResultDesc = "未开奖";
                    } else if(guessResult == GuessResultEnum.NOT_GUESS_RIGHT) {
                        guessResultDesc = "未猜对";
                    } else if(guessResult == GuessResultEnum.NOT_WINNING) {
                        guessResultDesc = "未中奖";
                    } else if(guessResult == GuessResultEnum.WINNING) {
                        if(seasonType == SeasonTypeEnum.CASH_FIELD && rewardBonusAmount != null) {
                            BigDecimal bd = BigDecimal.valueOf(rewardBonusAmount.doubleValue()/100);
                            bd = bd.setScale(2, RoundingMode.HALF_UP);
                            histUserGuessDTO.setRewardGoodsName(bd + "元");
                        }else {
                            guessResultDesc = histUserGuessDTO.getRewardGoodsName();
                        }
                    }
                    guessHistory.addProperty("guessResult", guessResult);
                    guessHistory.addProperty("guessResultDesc", guessResultDesc);
                    guessHistory.addProperty("deliveryStatus", histUserGuessDTO.getDeliveryStatus());

                    guessHistoryList.add(guessHistory);
                }
                result.add("guessHistoryList", guessHistoryList);
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
     * 获取用户竞猜信息【51050602】
     */
    public JsonObject getUserGuessInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

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

            Result<ResGuessAccountDTO> resGuessAccountDTOResult = guessAccountService.getGuessAccountInfo(userId);
            if(resGuessAccountDTOResult.getCode().equals(CommonStateCode.SUCCESS) && resGuessAccountDTOResult.getData() != null){

                ResGuessAccountDTO resGuessAccountDTO = resGuessAccountDTOResult.getData();
                UserProfile userProfile = kkUserService.getUserProfile(userId);
                if(userProfile != null) {
                    result.addProperty("gender", userProfile.getGender());
                    result.addProperty("portrait", getPortrait(userProfile));
                    result.addProperty("nickname", userProfile.getNickName());
                }
                result.addProperty("guessCurrencyNum", resGuessAccountDTO.getGuessCurrencyNum());
                result.addProperty("amount", resGuessAccountDTO.getAmount());
                result.addProperty("accumulateAmount", resGuessAccountDTO.getAccumulateAmount());
                result.addProperty("guessRightNum", resGuessAccountDTO.getGuessRightNum());
                result.addProperty("inviteFriendsNum", resGuessAccountDTO.getInviteFriendsNum());

                Integer guessRightNumRank = guessAccountService.getUserGuessRank(1, userId).getData();
                if(guessRightNumRank != null) {
                    result.addProperty("guessRightNumRank", guessRightNumRank);
                }
                Integer accumulateAmountRank = guessAccountService.getUserGuessRank(2, userId).getData();
                if(accumulateAmountRank != null) {
                    result.addProperty("accumulateAmountRank", accumulateAmountRank);
                }
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

    private String getPortrait(UserProfile userProfile) {
        return userProfile.getPortrait() == null ? null : userProfile.getPortrait() + "!128";
    }

    /**
     * 获取竞猜榜单【51050603】
     */
    public JsonObject getGuessRank(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        int rankType;
        try {
            rankType = CommonUtil.getJsonParamInt(jsonObject, "rankType", 1, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {

            Result<List<ResGuessAccountDTO>> rankResult =  guessAccountService.getGuessRank(rankType);
            if(rankResult.getCode().equals(CommonStateCode.SUCCESS) && rankResult.getData() != null){

                List<ResGuessAccountDTO> resActorLadderMatchDOS = rankResult.getData();

                JsonArray guessRankList = new JsonArray();
                int rank = 0;
                for(ResGuessAccountDTO resGuessAccountDTO : resActorLadderMatchDOS) {

                    JsonObject guessRank = new JsonObject();
                    int userId = resGuessAccountDTO.getUserId();
                    UserProfile userProfile = kkUserService.getUserProfile(userId);
                    guessRank.addProperty("userId", userId);
                    if(userProfile != null) {
                        guessRank.addProperty("gender", userProfile.getGender());
                        guessRank.addProperty("portrait", getPortrait(userProfile));
                        guessRank.addProperty("nickname", userProfile.getNickName());
                    }
                    guessRank.addProperty("ranking", ++rank);
                    guessRank.addProperty("guessRightNum", resGuessAccountDTO.getGuessRightNum());
                    guessRank.addProperty("accumulateAmount", resGuessAccountDTO.getAccumulateAmount());
                    guessRankList.add(guessRank);
                }

                result.add("guessRankList", guessRankList);
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
     * 获取比赛开始时间列表【51050604】
     */
    public JsonObject getMatchStartTimeList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        try {

            Result<List<MatchStartTimeDTO>> matchStartTimeListResult =  guessConfService.getMatchStartTimeList();
            if(matchStartTimeListResult.getCode().equals(CommonStateCode.SUCCESS) && matchStartTimeListResult.getData() != null){

                List<MatchStartTimeDTO> matchStartTimeDTOList = matchStartTimeListResult.getData();

                JsonArray matchStartTimeList = new JsonArray();
                for(MatchStartTimeDTO matchStartTimeDTO : matchStartTimeDTOList) {

                    JsonObject matchStartTimeJson = new JsonObject();
                    Date matchStartTime = matchStartTimeDTO.getMatchStartTime();
                    String matchStartTimeName = DateUtils.format(matchStartTime, "M月dd日");
                    matchStartTimeJson.addProperty("matchStartTime", matchStartTime.getTime());
                    matchStartTimeJson.addProperty("matchStartTimeName", matchStartTimeName);
                    matchStartTimeJson.addProperty("selected", matchStartTimeDTO.getSelected());
                    matchStartTimeList.add(matchStartTimeJson);
                }

                result.add("matchStartTimeList", matchStartTimeList);
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
     * 获取竞猜场次列表【51050605】
     */
    public JsonObject getGuessSeasonList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        int userId;
        long matchStartTime;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
            matchStartTime = CommonUtil.getJsonParamLong(jsonObject, "matchStartTime", DateUtils.getCurrentDate().getTime(), null, 1, Long.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {


            Result<List<ConfGuessSeasonDTO>> result1 =  guessConfService.getGuessSeasonConfList(new Date(matchStartTime));
            if(result1.getCode().equals(CommonStateCode.SUCCESS) && result1.getData() != null){

                List<ConfGuessSeasonDTO> confGuessSeasonDTOS = result1.getData();

                JsonArray seasonList = new JsonArray();
                for(ConfGuessSeasonDTO confGuessSeasonDTO : confGuessSeasonDTOS) {

                    JsonObject jsonObject1 = new JsonObject();

                    int seasonId = confGuessSeasonDTO.getSeasonId();
                    int seasonType = confGuessSeasonDTO.getSeasonType();
                    jsonObject1.addProperty("seasonId", seasonId);
                    jsonObject1.addProperty("seasonType",seasonType);
                    jsonObject1.addProperty("bonusAmount", confGuessSeasonDTO.getShowBonusAmount());
                    jsonObject1.addProperty("rewardGoodsName", confGuessSeasonDTO.getRewardGoodsName());
                    jsonObject1.addProperty("rewardGoodsNum", confGuessSeasonDTO.getRewardGoodsShowNum());
                    jsonObject1.addProperty("rewardGoodsUrl", confGuessSeasonDTO.getRewardGoodsUrl());
                    jsonObject1.addProperty("guessCurrencyNum", confGuessSeasonDTO.getGuessCurrencyNum());
                    if (userId > 0) {
                        HistUserGuessDTO histUserGuessDTO = guessHistService.getUserGuessHistInfo(userId, seasonId).getData();
                        if (histUserGuessDTO != null) {
                            jsonObject1.addProperty("guessBetItemId", histUserGuessDTO.getSupportGuessItemId());
                        }
                    }
                    jsonObject1.addProperty("leftGuessItemId", confGuessSeasonDTO.getLeftGuessItemId());
                    jsonObject1.addProperty("leftGuessItemName", confGuessSeasonDTO.getLeftGuessItemName());
                    jsonObject1.addProperty("leftGuessItemIcon", confGuessSeasonDTO.getLeftGuessItemIcon());
                    jsonObject1.addProperty("rightGuessItemId", confGuessSeasonDTO.getRightGuessItemId());
                    jsonObject1.addProperty("rightGuessItemName", confGuessSeasonDTO.getRightGuessItemName());
                    jsonObject1.addProperty("rightGuessItemIcon", confGuessSeasonDTO.getRightGuessItemIcon());

                    long guessStartTime = confGuessSeasonDTO.getGuessStartTime().getTime();
                    long guessEndTime = confGuessSeasonDTO.getGuessEndTime().getTime();
                    long nowTime = DateUtils.getCurrentDate().getTime();
                    long giveRewardStatus = confGuessSeasonDTO.getGiveRewardStatus();
                    int seasonStatus = 3;
                    if(nowTime < guessStartTime) {
                        seasonStatus = 1;
                    } else if(nowTime < guessEndTime) {
                        seasonStatus = 2;
                    } else if(giveRewardStatus == GuessLotteryStatusEnum.ALREADY_LOTTERY) {
                        seasonStatus = 4;
                        jsonObject1.addProperty("winGuessItemName", confGuessSeasonDTO.getWinGuessItemName());
                    }
                    jsonObject1.addProperty("guessStartTime", guessStartTime);
                    jsonObject1.addProperty("guessEndTime", guessEndTime);
                    jsonObject1.addProperty("seasonStatus", seasonStatus);

                    GuessWinningDTO guessWinningDTO = guessHistService.getGuessWinningInfo(seasonId).getData();
                    if(guessWinningDTO != null) {
                        if(seasonType == SeasonTypeEnum.CASH_FIELD) {
                            jsonObject1.addProperty("winningUserNum", guessWinningDTO.getWinningUserCount());
                        } else {
                            JsonArray winningUserList = new JsonArray();
                            List<WinningUserDTO> winningUserDTOList = guessWinningDTO.getWinningUserDTOList();
                            if(!winningUserDTOList.isEmpty()) {
                                for(WinningUserDTO winningUserDTO : winningUserDTOList) {
                                    JsonObject jsonObject2 = new JsonObject();
                                    jsonObject2.addProperty("userId", winningUserDTO.getUserId());
                                    jsonObject2.addProperty("nickname", winningUserDTO.getNickname());
                                    winningUserList.add(jsonObject2);
                                }
                            }
                            jsonObject1.add("winningUserList", winningUserList);
                        }
                    }

                    seasonList.add(jsonObject1);
                }

                result.add("seasonList", seasonList);
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
     * 用户竞猜下注【51050606】
     */
    public JsonObject guessBet(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        int userId;
        long matchStartTime;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
            matchStartTime = CommonUtil.getJsonParamLong(jsonObject, "matchStartTime", DateUtils.getCurrentDate().getTime(), null, 1, Long.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {


            Result<List<ConfGuessSeasonDTO>> result1 =  guessConfService.getGuessSeasonConfList(new Date(matchStartTime));
            if(result1.getCode().equals(CommonStateCode.SUCCESS) && result1.getData() != null){

                List<ConfGuessSeasonDTO> confGuessSeasonDTOS = result1.getData();

                JsonArray seasonList = new JsonArray();
                for(ConfGuessSeasonDTO confGuessSeasonDTO : confGuessSeasonDTOS) {

                    JsonObject jsonObject1 = new JsonObject();

                    int seasonId = confGuessSeasonDTO.getSeasonId();
                    int seasonType = confGuessSeasonDTO.getSeasonType();
                    jsonObject1.addProperty("seasonId", seasonId);
                    jsonObject1.addProperty("seasonType",seasonType);
                    jsonObject1.addProperty("bonusAmount", confGuessSeasonDTO.getShowBonusAmount());
                    jsonObject1.addProperty("rewardGoodsName", confGuessSeasonDTO.getRewardGoodsName());
                    jsonObject1.addProperty("rewardGoodsNum", confGuessSeasonDTO.getRewardGoodsShowNum());
                    jsonObject1.addProperty("rewardGoodsUrl", confGuessSeasonDTO.getRewardGoodsUrl());
                    jsonObject1.addProperty("guessCurrencyNum", confGuessSeasonDTO.getGuessCurrencyNum());
                    if (userId > 0) {
                        HistUserGuessDTO histUserGuessDTO = guessHistService.getUserGuessHistInfo(userId, seasonId).getData();
                        if (histUserGuessDTO != null) {
                            jsonObject1.addProperty("guessBetItemId", histUserGuessDTO.getSupportGuessItemId());
                        }
                    }
                    jsonObject1.addProperty("leftGuessItemId", confGuessSeasonDTO.getLeftGuessItemId());
                    jsonObject1.addProperty("leftGuessItemName", confGuessSeasonDTO.getLeftGuessItemName());
                    jsonObject1.addProperty("leftGuessItemIcon", confGuessSeasonDTO.getLeftGuessItemIcon());
                    jsonObject1.addProperty("rightGuessItemId", confGuessSeasonDTO.getRightGuessItemId());
                    jsonObject1.addProperty("rightGuessItemName", confGuessSeasonDTO.getRightGuessItemName());
                    jsonObject1.addProperty("rightGuessItemIcon", confGuessSeasonDTO.getRightGuessItemIcon());

                    long guessStartTime = confGuessSeasonDTO.getGuessStartTime().getTime();
                    long guessEndTime = confGuessSeasonDTO.getGuessEndTime().getTime();
                    long nowTime = DateUtils.getCurrentDate().getTime();
                    long giveRewardStatus = confGuessSeasonDTO.getGiveRewardStatus();
                    int seasonStatus = 3;
                    if(nowTime < guessStartTime) {
                        seasonStatus = 1;
                    } else if(nowTime < guessEndTime) {
                        seasonStatus = 2;
                    } else if(giveRewardStatus == GuessLotteryStatusEnum.ALREADY_LOTTERY) {
                        seasonStatus = 4;
                        jsonObject1.addProperty("winGuessItemName", confGuessSeasonDTO.getWinGuessItemName());
                    }
                    jsonObject1.addProperty("guessStartTime", guessStartTime);
                    jsonObject1.addProperty("guessEndTime", guessEndTime);
                    jsonObject1.addProperty("seasonStatus", seasonStatus);

                    GuessWinningDTO guessWinningDTO = guessHistService.getGuessWinningInfo(seasonId).getData();
                    if(guessWinningDTO != null) {
                        if(seasonType == SeasonTypeEnum.CASH_FIELD) {
                            jsonObject1.addProperty("winningUserNum", guessWinningDTO.getWinningUserCount());
                        } else {
                            JsonArray winningUserList = new JsonArray();
                            List<WinningUserDTO> winningUserDTOList = guessWinningDTO.getWinningUserDTOList();
                            if(!winningUserDTOList.isEmpty()) {
                                for(WinningUserDTO winningUserDTO : winningUserDTOList) {
                                    JsonObject jsonObject2 = new JsonObject();
                                    jsonObject2.addProperty("userId", winningUserDTO.getUserId());
                                    jsonObject2.addProperty("nickname", winningUserDTO.getNickname());
                                    winningUserList.add(jsonObject2);
                                }
                            }
                            jsonObject1.add("winningUserList", winningUserList);
                        }
                    }

                    seasonList.add(jsonObject1);
                }

                result.add("seasonList", seasonList);
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
	
}

package com.melot.kktv.action;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.kk.guess.api.constant.*;
import com.melot.kk.guess.api.dto.*;
import com.melot.kk.guess.api.service.GuessAccountService;
import com.melot.kk.guess.api.service.GuessConfService;
import com.melot.kk.guess.api.service.GuessHistService;
import com.melot.kk.logistics.api.domain.HistDeliveryDO;
import com.melot.kk.logistics.api.service.HistDeliveryService;
import com.melot.kkcore.actor.service.ActorService;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kktv.base.CommonStateCode;
import com.melot.kktv.base.Result;
import com.melot.kktv.service.ConfigService;
import com.melot.kktv.util.*;
import org.apache.log4j.Logger;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;

import static com.melot.kktv.util.ParamCodeEnum.*;

public class GuessFunctions {

    private static Logger logger = Logger.getLogger(GuessFunctions.class);

    @Resource
    GuessConfService guessConfService;

    @Resource
    GuessHistService guessHistService;

    @Resource
    GuessAccountService guessAccountService;

    @Resource
    HistDeliveryService histDeliveryService;

    @Resource
    KkUserService kkUserService;

    @Resource
    ConfigService configService;

    @Resource
    ActorService actorService;

    /**
     * 	获取用户竞猜历史【51050601】
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
            userId = CommonUtil.getJsonParamInt(jsonObject, USER_ID.getId(), 0, USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
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
                            guessResultDesc = bd + "元";
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
            logger.error("Error getUserGuessHistory()", e);
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
            userId = CommonUtil.getJsonParamInt(jsonObject, USER_ID.getId(), 0, USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
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
            logger.error("Error getUserGuessInfo()", e);
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
            rankType = CommonUtil.getJsonParamInt(jsonObject, RANK_TYPE.getId(), 1, RANK_TYPE.getErrorCode(), 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
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
            logger.error("Error getGuessRank()", e);
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
            logger.error("Error getMatchStartTimeList()", e);
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
            userId = CommonUtil.getJsonParamInt(jsonObject, USER_ID.getId(), 0, null, 1, Integer.MAX_VALUE);
            matchStartTime = CommonUtil.getJsonParamLong(jsonObject, MATCH_START_TIME.getId(), DateUtils.getCurrentDate().getTime(), MATCH_START_TIME.getErrorCode(), 1, Long.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {

            Result<List<ConfGuessSeasonDTO>> result1 =  guessConfService.getGuessSeasonConfListByMatchStartTime(new Date(matchStartTime));
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
                            jsonObject1.addProperty("guessResult", histUserGuessDTO.getGuessResult());
                        }
                    }
                    jsonObject1.addProperty("leftGuessItemId", confGuessSeasonDTO.getLeftGuessItemId());
                    jsonObject1.addProperty("leftGuessItemName", confGuessSeasonDTO.getLeftGuessItemName());
                    jsonObject1.addProperty("leftGuessItemIcon", confGuessSeasonDTO.getLeftGuessItemIcon());
                    jsonObject1.addProperty("rightGuessItemId", confGuessSeasonDTO.getRightGuessItemId());
                    jsonObject1.addProperty("rightGuessItemName", confGuessSeasonDTO.getRightGuessItemName());
                    jsonObject1.addProperty("rightGuessItemIcon", confGuessSeasonDTO.getRightGuessItemIcon());

                    int seasonStatus = confGuessSeasonDTO.getSeasonStatus();
                    long giveRewardStatus = confGuessSeasonDTO.getGiveRewardStatus();
                    if(seasonStatus == SeasonStatusEnum.GUESS_OVER && giveRewardStatus == GuessLotteryStatusEnum.ALREADY_LOTTERY) {
                        seasonStatus = 4;
                        jsonObject1.addProperty("winGuessItemId", confGuessSeasonDTO.getWinGuessItemId());
                        jsonObject1.addProperty("winGuessItemName", confGuessSeasonDTO.getWinGuessItemName());
                    }
                    jsonObject1.addProperty("guessStartTime", confGuessSeasonDTO.getGuessStartTime().getTime());
                    jsonObject1.addProperty("guessEndTime", confGuessSeasonDTO.getGuessEndTime().getTime());
                    jsonObject1.addProperty("seasonStatus", seasonStatus);

                    GuessWinningDTO guessWinningDTO = guessHistService.getGuessWinningInfo(seasonId).getData();
                    if(guessWinningDTO != null) {
                        if(seasonType == SeasonTypeEnum.CASH_FIELD) {
                            jsonObject1.addProperty("winningUserNum", guessWinningDTO.getWinningUserCount());
                        } else {
                            JsonArray winningUserList = new JsonArray();
                            List<WinningUserDTO> winningUserDTOList = guessWinningDTO.getWinningUserDTOList();
                            if(winningUserDTOList != null) {
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
                    GuessItemBetDTO guessItemBetDTO = guessHistService.getGuessItemBetInfo(seasonId).getData();
                    if(guessItemBetDTO != null) {
                        jsonObject1.addProperty("leftGuessItemNum", guessItemBetDTO.getLeftGuessItemNum());
                        jsonObject1.addProperty("rightGuessItemNum", guessItemBetDTO.getRightGuessItemNum());
                        jsonObject1.addProperty("drawNum", guessItemBetDTO.getDrawNum());
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
            logger.error("Error getGuessSeasonList()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 用户竞猜下注【51050606】
     */
    public JsonObject guessBet(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId, seasonId, guessBetItemId;
        Integer shareUserId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, USER_ID.getId(), 0, USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            seasonId = CommonUtil.getJsonParamInt(jsonObject, SEASON_ID.getId(), 0, SEASON_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            guessBetItemId = CommonUtil.getJsonParamInt(jsonObject, GUESS_BET_ITEM_ID.getId(), 0, GUESS_BET_ITEM_ID.getErrorCode(), 0, Integer.MAX_VALUE);
            shareUserId = CommonUtil.getJsonParamInt(jsonObject, SHARE_USER_ID.getId(), 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }


        try {

            if(shareUserId == 0) {
                shareUserId = null;
            }
            Result<Boolean> result1 =  guessHistService.guessBet(userId, seasonId, guessBetItemId, shareUserId);
            if(result1.getCode().equals(CommonStateCode.SUCCESS) && result1.getData()){

                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            } else {
                result.addProperty("TagCode", result1.getCode());
                return result;
            }
        } catch (Exception e) {
            logger.error("Error guessBet()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 用户申请发货【51050607】
     */
    public JsonObject applyDelivery(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId, guessHistId, addressId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, USER_ID.getId(), 0, USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            guessHistId = CommonUtil.getJsonParamInt(jsonObject, GUESS_HIST_ID.getId(), 0, GUESS_HIST_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            addressId = CommonUtil.getJsonParamInt(jsonObject, ADDRESS_ID.getId(), 0, ADDRESS_ID.getErrorCode(), 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {

            Result<Boolean> result1 =  guessHistService.applyDelivery(guessHistId, userId, addressId);
            if(result1.getCode().equals(CommonStateCode.SUCCESS) && result1.getData()){

                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            } else {
                result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
        } catch (Exception e) {
            logger.error("Error applyDelivery()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取发货信息【51050608】
     */
    public JsonObject getDeliveryInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int guessHistId;
        try {
            guessHistId = CommonUtil.getJsonParamInt(jsonObject, GUESS_HIST_ID.getId(), 0, GUESS_HIST_ID.getErrorCode(), 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {

            Result<HistDeliveryDO> result1 =  histDeliveryService.getHistDeliveryDO(guessHistId, 4);
            if(result1.getCode().equals(CommonStateCode.SUCCESS) && result1.getData() != null){

                HistDeliveryDO histDeliveryDO = result1.getData();

                result.addProperty("consigneeName", histDeliveryDO.getConsigneeName());
                result.addProperty("consigneeMobile", histDeliveryDO.getConsigneeMobile());
                result.addProperty("detailAddress", histDeliveryDO.getDetailAddress());
                result.addProperty("waybillNumber", histDeliveryDO.getWaybillNumber());
                result.addProperty("courierCompany", histDeliveryDO.getCourierCompany());
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            } else {
                result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
        } catch (Exception e) {
            logger.error("Error getDeliveryInfo()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取世界杯相关直播间【51050609】
     */
    public JsonObject getWorldCupRooms(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        try {

            Integer worldCupPartId = configService.getWorldCupPartId();

            Integer coinPurchaseRoomId = configService.getCoinPurchaseRoomId();

            result.addProperty("worldCupPartId", worldCupPartId);
            result.addProperty("coinPurchaseRoomId", coinPurchaseRoomId);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error getWorldCupRooms()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 查看获取竞猜币任务列表【51050610】
     */
    public JsonObject getAccessGuessCurrencyTaskList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();
        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        int userId;
        long matchStartTime;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, USER_ID.getId(), 0, USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }
        try {
            Result<List<UserGuessWaysStatusDTO>> res = guessAccountService.getUserGuessWaysStatus(userId);
            if (!ObjectUtils.isEmpty(res) && CommonStateCode.SUCCESS.equals(res.getCode())){
                List<UserGuessWaysStatusDTO> userGuessWaysStatusDTOS = res.getData();
                if (userGuessWaysStatusDTOS==null || userGuessWaysStatusDTOS.isEmpty()){
                    result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                }
                JsonArray array = new JsonArray();
                for (UserGuessWaysStatusDTO userGuessWaysStatusDTO : userGuessWaysStatusDTOS) {
                    JsonObject object = new JsonObject();
                    object.addProperty("guessTaskId", userGuessWaysStatusDTO.getGuessTaskId());
                    object.addProperty("guessTaskStatus", userGuessWaysStatusDTO.getGuessTaskStatus());
                    array.add(object);
                }
                result.add("guessTaskList", array);
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            }else {
                result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            }
            return result;
        } catch (Exception e) {
            logger.error("Error getAccessGuessCurrencyTaskList()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }
    
    /**
     * 微信公众号竞猜奖金提现接口【51050611】
     */
    public JsonObject guessWithdraw(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {

        JsonObject result = new JsonObject();

        // sv安全校验接口
        JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
        if(rtJO != null)
            return rtJO;

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId, platform;
        String uuid, unionid;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, USER_ID.getId(), 0, USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            uuid = CommonUtil.getJsonParamString(jsonObject, UUID.getId(), null, UUID.getErrorCode(), 1, Integer.MAX_VALUE);
            unionid = CommonUtil.getJsonParamString(jsonObject, UNIONID.getId(), null, UNIONID.getErrorCode(), 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, PLATFORM.getId(), 0, PLATFORM.getErrorCode(), 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {

            // 调用竞猜模块微信提现
            String clientIP = CommonUtil.getIpAddr(request);
            if(clientIP.startsWith("10.0.")) {
                clientIP = "12.0.2.0";
            }
            Result<Boolean> withdrawResult = guessAccountService.guessWithdraw(userId, uuid, unionid, clientIP);
            String code = withdrawResult.getCode();
            if(!code.equals(CommonStateCode.SUCCESS)) {

                String errorMessage = GuessResultCode.getMsg(code);
                int errorTag = 1;
                if(errorMessage.startsWith("未定义的错误码")) {
                    errorTag = 2;
                }

                result.addProperty("errorMessage", withdrawResult.getMsg());
                result.addProperty("errorTag", errorTag);
                result.addProperty("TagCode", code);
                return result;
            }

            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error guessWithdraw()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

}

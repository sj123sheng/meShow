/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2018
 */
package com.melot.kktv.action;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.melot.kk.competition.api.constant.ReturnCode;
import com.melot.kk.competition.api.dto.MatchResult;
import com.melot.kk.competition.api.service.CompetitionMatchService;
import com.melot.kk.competition.api.service.CompetitionService;
import com.melot.kk.pkgame.api.dto.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.kk.hall.api.domain.HallPartConfDTO;
import com.melot.kk.hall.api.domain.HallRoomInfoDTO;
import com.melot.kk.hall.api.service.SysMenuService;
import com.melot.kk.pkgame.api.constant.ReturnResultCode;
import com.melot.kk.pkgame.api.enums.GameEnum;
import com.melot.kk.pkgame.api.enums.UserTypeEnum;
import com.melot.kk.pkgame.api.service.MatchService;
import com.melot.kk.pkgame.api.service.PKGameService;
import com.melot.kk.pkgame.api.service.QuizPKService;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcx.service.RoomService;
import com.melot.kkcx.service.UserService;
import com.melot.kkcx.transform.HallRoomTF;
import com.melot.kkcx.transform.RoomTF;
import com.melot.kktv.base.CommonStateCode;
import com.melot.kktv.base.Result;
import com.melot.kktv.service.ConfigService;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.DateUtil;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.TagCodeEnum;

/**
 * Title: KKPlayFunctions
 * <p>
 * Description: K玩大厅
 * </p>
 * 
 * @author 林端端<a href="mailto:duanduan.lin@melot.cn" />
 * @version V1.0
 * @since 2018年9月19日 上午10:34:54
 */
public class KKPlayFunctions {
    
    private static Logger logger = Logger.getLogger(KKPlayFunctions.class);
    
    @Autowired
    private ConfigService configService;
    
    @Resource
    private SysMenuService hallPartService;
    
    @Resource
    private PKGameService pkGameService;
    
    @Resource
    private MatchService matchService;
    
    @Resource
    QuizPKService quizPKService;

    @Resource
    CompetitionMatchService competitionMatchService;

    @Resource
    CompetitionService competitionService;
    
    /**
     * 获取K玩大厅栏目列表(51070301)
     */
    public JsonObject getPartList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int platform;
        try {
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, TagCodeEnum.PLATFORM_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            JsonArray cataArray = new JsonArray();
            String kkplayConfig = configService.getKkPlayConfig();
            JsonArray configArray = new JsonParser().parse(kkplayConfig).getAsJsonArray();
            if (configArray.size() > 0) {
                for (int i=0; i < configArray.size(); i++) {
                    JsonObject jsonObj = (JsonObject) configArray.get(i);
                    JsonArray openTimes = jsonObj.getAsJsonArray("openTime");
                    Calendar calendar = Calendar.getInstance();
                    Integer hour = calendar.get(Calendar.HOUR_OF_DAY);
                    Integer minute = calendar.get(Calendar.MINUTE);
                    Integer second = calendar.get(Calendar.SECOND);
                    Long time = hour * 3600L + minute * 60 + second;
                    if(openTimes!=null){
                        for(int n=0;n<openTimes.size();n++){
                            JsonObject openTime = (JsonObject)openTimes.get(n);
                            if (openTime.get("endTime").getAsLong() >= time) {
                                jsonObj.addProperty("startTime",openTime.get("startTime").getAsLong());
                                jsonObj.addProperty("endTime",openTime.get("endTime").getAsLong());
                                break;
                            }
                            if(n == openTimes.size()-1){
                                jsonObj.addProperty("startTime",((JsonObject)openTimes.get(0)).get("startTime").getAsLong());
                                jsonObj.addProperty("endTime",((JsonObject)openTimes.get(0)).get("endTime").getAsLong());
                            }
                        }
                    }
                    //置顶时显示大海报
                    if (jsonObj.get("isTop").getAsBoolean()) {
                        jsonObj.addProperty("title_poster", jsonObj.get("full_title_poster").getAsString());
                    }
                    jsonObj.remove("full_title_poster");
                    Result<HallPartConfDTO> sysMenuResult = hallPartService.getPartList(jsonObj.get("cataId").getAsInt(), 0, 0, 0, AppIdEnum.AMUSEMENT, 0, 4);
                    if (sysMenuResult != null && CommonStateCode.SUCCESS.equals(sysMenuResult.getCode())) {
                        HallPartConfDTO sysMenu = sysMenuResult.getData();
                        if (sysMenu != null) {
                            JsonArray roomArray = new JsonArray();
                            List<HallRoomInfoDTO> roomList = sysMenu.getRooms();
                            if (roomList != null) {
                                for (HallRoomInfoDTO hallRoomInfoDTO : roomList) {
                                    roomArray.add(HallRoomTF.roomInfoToJson(hallRoomInfoDTO, platform));
                                }
                            }
                            jsonObj.add("roomList", roomArray);
                        }
                    }
                    cataArray.add(jsonObj);
                }
            }

            result.add("cataList", cataArray);
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error KKPlayFunctions.getPartList()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }
    
    /**
     * 获取K玩用户信息(51070302)
     * 
     * @param jsonObject 请求对象
     * @param checkTag 是否验证token标记
     * @return 
     */
    public JsonObject getUserKKPlayInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            UserProfile userProfile = UserService.getUserInfoNew(userId);
            if (userProfile != null) {
                result.addProperty("gender", userProfile.getGender());
                result.addProperty("nickname", userProfile.getNickName());
                if (!StringUtil.strIsNull(userProfile.getPortrait())) {
                    result.addProperty("portrait", userProfile.getPortrait());
                }
            }
            UserKkplayScoreDTO userKkplayScoreDTO = pkGameService.getUserKkplayScore(userId);
            if (userKkplayScoreDTO != null) {
                result.addProperty("score", userKkplayScoreDTO.getTotalScore());
            }
            result.addProperty("goldCoin", UserService.getUserGoldCoin(userId));
            result.addProperty("showMoney", UserService.getUserMoney(userId));
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("Error getUserKKPlayInfo()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
        
        return result;
    }
    
    /**
     * 获取K玩排行榜(51070303)
     * 
     * @param jsonObject 请求对象
     * @param checkTag 是否验证token标记
     * @return 
     */
    public JsonObject getRankList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int rankType, slotType, platform, userId, count;
        try {
            rankType = CommonUtil.getJsonParamInt(jsonObject, "rankType", 0, "5107030301", 0, Integer.MAX_VALUE);
            slotType = CommonUtil.getJsonParamInt(jsonObject, "slotType", 0, "5107030302", 0, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 1, null, 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
            count = CommonUtil.getJsonParamInt(jsonObject, "count", 10, null, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        
        try {
            List<UserKKPlayRank> userKKPlayRankList = pkGameService.getUserKKPlayRankList(rankType, slotType, count);
            JsonArray rankList = new JsonArray();
            if (!CollectionUtils.isEmpty(userKKPlayRankList)) {
                for (UserKKPlayRank userKKPlayRank : userKKPlayRankList) {
                    JsonObject jsonObj = new JsonObject();
                    int rankUserId = userKKPlayRank.getUserId();
                    jsonObj.addProperty("userId", rankUserId);
                    jsonObj.addProperty("position", userKKPlayRank.getPosition());
                    jsonObj.addProperty("score", userKKPlayRank.getScore());
                    UserProfile userProfile = UserService.getUserInfoNew(rankUserId);
                    if (userProfile != null) {
                        jsonObj.addProperty("gender", userProfile.getGender());
                        jsonObj.addProperty("nickname", userProfile.getNickName());
                        if (!StringUtil.strIsNull(userProfile.getPortrait())) {
                            jsonObj.addProperty("portrait_path_original", userProfile.getPortrait());
                        }
                    }
                    rankList.add(jsonObj);
                }
            }
            
            result.addProperty("userId", userId);
            UserKKPlayRank userKKPlayRank = pkGameService.getUserKKPlayRankInfo(userId, rankType, slotType);
            Integer position = -1;
            Long score = 0L;
            if (userKKPlayRank != null) {
                position = userKKPlayRank.getPosition();
                score = userKKPlayRank.getScore();
            }
            result.addProperty("position", position);
            result.addProperty("score", score);
            UserProfile userProfile = UserService.getUserInfoNew(userId);
            if (userProfile != null) {
                result.addProperty("gender", userProfile.getGender());
                result.addProperty("nickname", userProfile.getNickName());
                if (!StringUtil.strIsNull(userProfile.getPortrait())) {
                    result.addProperty("portrait_path_original", userProfile.getPortrait());
                }
            }
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
            result.add("rankList", rankList);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("Error getRankList()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
        
        return result;
    }
    
    /**
     * 快速匹配(51070304)
     * 
     * @param jsonObject 请求对象
     * @param checkTag 是否验证token标记
     * @return 
     */
    public JsonObject getMatchGameUser(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        int userId, gameId,competitionType=0,v=0,platform=0;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            gameId = CommonUtil.getJsonParamInt(jsonObject, "gameId", 0, "5107030401", 1, Integer.MAX_VALUE);
            if(gameId == 1){
                platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 1, null, 1, Integer.MAX_VALUE);
                v = CommonUtil.getJsonParamInt(jsonObject, "v", 0, null, 1, Integer.MAX_VALUE);
                if(platform > 1 && v < 6200){
                    result.addProperty("TagCode", TagCodeEnum.LOW_VERSION_EXCEPTION);
                    return result;
                }
                competitionType = CommonUtil.getJsonParamInt(jsonObject, "competitionType", 0, "5107030408", 1, Integer.MAX_VALUE);
            }

        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            if(gameId == 1){
                Result<MatchResult> matchResult=competitionMatchService.startMatch(userId,competitionType,1);
                if(ReturnCode.SUCCESS.getCode().equals(matchResult.getCode())){
                    MatchResult match = matchResult.getData();
                    result.addProperty("roomId",match.getRoomId());
                    result.addProperty("roomSource",match.getRoomSource());
                    result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                    return result;
                }
                else if(ReturnCode.ERROR_TICKET_NOT_ENOUGH.getCode().equals(matchResult.getCode())){
                    result.addProperty("TagCode", "5107030409");
                    return result;
                }
                else if(ReturnCode.ERROR_COMPETITION_GAMING.getCode().equals(matchResult.getCode())){
                    result.addProperty("TagCode", "5107030403");
                    return result;
                }
                else if(ReturnCode.ERROR_ON_OTHER_GAME.getCode().equals(matchResult.getCode())){
                    result.addProperty("TagCode", "5107030410");
                    return result;
                }
                else if(ReturnCode.ERROR_GAME_NOT_OPEN.getCode().equals(matchResult.getCode())){
                    result.addProperty("TagCode", "5107030411");
                    return result;
                }
                else if(ReturnCode.ERROR_COMPETITION_TIME.getCode().equals(matchResult.getCode())){
                    result.addProperty("TagCode", "5107030404");
                    return result;
                }
                else {
                    result.addProperty("TagCode", "5107030402");
                    return result;
                }
            }
            if (gameId != GameEnum.ANSWER_PK_FOUR.getCode() && gameId != GameEnum.ANSWER_PK_DOUBLE.getCode()) {
                result.addProperty("TagCode", "5107030402");
                return result;
            }
            if (!checkMatchOpenStatus(gameId)) {
                result.addProperty("TagCode", "5107030404");
            } else {
                ReturnResult<Integer> matchResp = matchService.startMatch(userId, UserTypeEnum.USER.getCode(), gameId);
                String macthCode = matchResp.getCode();
                if (ReturnResultCode.SUCCESS.getCode().equals(macthCode)) {
                    result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                } else {
                    if (ReturnResultCode.ERROR_ON_PK_STATUS.getCode().equals(macthCode) 
                            ||ReturnResultCode.ERROR_ON_MATCH_STATUS.getCode().equals(macthCode)) {
                        result.addProperty("TagCode", "5107030405");
                    } else if(ReturnResultCode.ERROR_MATCH_IN_CD.getCode().equals(macthCode)) {
                        result.addProperty("TagCode", "5107030406");
                    } else if(ReturnResultCode.ERROR_MATCH_REPEAT.getCode().equals(macthCode)) {
                        result.addProperty("TagCode", "5107030407");
                    } else if(ReturnResultCode.ERROR_IN_OTHER_MATCH.getCode().equals(macthCode)) {
                        result.addProperty("TagCode", "5107030410");
                    } else {
                        result.addProperty("TagCode", "5107030403");
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error getMatchGameUser, userId: " + userId + ", gameId: " + gameId, e);
        }
        
        return result;
    }
    
    /**
     * 获取匹配结果(51070305)
     * 
     * @param jsonObject 请求对象
     * @param checkTag 是否验证token标记
     * @return 
     */
    public JsonObject getGameMatchInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        int userId,gameId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            gameId = CommonUtil.getJsonParamInt(jsonObject, "gameId", 113, null, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        if(gameId == 1){
            Result<Integer> roomResult = competitionService.competitionRoomIdByUserId(userId);
            if(ReturnResultCode.SUCCESS.getCode().equals(roomResult.getCode())) {
                if(roomResult.getData()!=null){
                    RoomInfo roomInfo = RoomService.getRoomInfo(roomResult.getData());
                    if (roomInfo != null) {
                        result.add("roomInfo", RoomTF.roomInfoToJson(roomInfo, PlatformEnum.WEB, true));
                    }
                }
            }
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        }

        QuziPKUserDTO quziPKUserDTO = quizPKService.getUserDTO(userId);
        if (quziPKUserDTO != null) {
            result.addProperty("gameId", quziPKUserDTO.getGameId());
            result.addProperty("seatId", quziPKUserDTO.getSeatId());
            RoomInfo roomInfo = RoomService.getRoomInfo(quziPKUserDTO.getActorId());
            if (roomInfo != null) {
                result.add("roomInfo", RoomTF.roomInfoToJson(roomInfo, PlatformEnum.WEB, true)); 
            }
        }
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 取消匹配(51070307)
     * 
     * @param jsonObject 请求对象
     * @param checkTag 是否验证token标记
     * @return 
     */
    public JsonObject cancelMatchGameUser(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        int userId, gameId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            gameId = CommonUtil.getJsonParamInt(jsonObject, "gameId", 0, "5107030701", 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            matchService.endMatch(userId, UserTypeEnum.USER.getCode(), gameId);
        } catch (Exception e) {
            logger.error("cancelMatchGameUser execute exception, userId: " + userId + "gameId: " + gameId, e);
        }
        
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 游戏规则(51070308)
     * 
     * @param jsonObject 请求对象
     * @param checkTag 是否验证token标记
     * @return 
     */
    public JsonObject getGameRule(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int gameId;
        try {
            gameId = CommonUtil.getJsonParamInt(jsonObject, "gameId", 0, "5107030801", 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        String kkPlayRule = configService.getKkPlayRule();
        JsonArray ruleArray = new JsonParser().parse(kkPlayRule).getAsJsonArray();
        if (ruleArray.size() > 0) {
            for (int i=0; i < ruleArray.size(); i++) {
                JsonObject jObj = (JsonObject) ruleArray.get(i);
                if (jObj.get("gameId").getAsInt() == gameId) {
                    result.addProperty("gameRule", jObj.get("gameRule").getAsString());
                    result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                    return result;
                }
            }
        } 
        result.addProperty("TagCode", "5107030802");
        return result;
    }
    
    /**
     * 获取游戏倍数及道具配置详情(51070309)
     * 
     * @param jsonObject 请求对象
     * @param checkTag 是否验证token标记
     * @return 
     */
    public JsonObject getMultipleConf(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int gameId;
        try {
            gameId = CommonUtil.getJsonParamInt(jsonObject, "gameId", 0, "5107030901", 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
    
        try {
            List<MultipleConfDTO> multipleConfList = pkGameService.getMultipleConfList(gameId);
            if (!CollectionUtils.isEmpty(multipleConfList)) {
                result.add("multipleConf", new JsonParser().parse(JSON.toJSONString(multipleConfList)).getAsJsonArray());
                List<GamePropDTO> gamePropList = pkGameService.getGamePropList(gameId);
                if (!CollectionUtils.isEmpty(gamePropList)) {
                    result.add("gamePropConf", new JsonParser().parse(JSON.toJSONString(gamePropList)).getAsJsonArray());
                    result.addProperty("maxPropTimes", gamePropList.size());
                }
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            } else {
                result.addProperty("TagCode", "5107030902");
            }
        } catch (Exception e) {
            logger.error("Error getMultipleConf()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
        
        return result;
    }
    
    /**
     * 获取pk游戏配置(51070310)
     * 
     * @param jsonObject 请求对象
     * @param checkTag 是否验证token标记
     * @return 
     */
    public JsonObject getPKGameList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        try {
            List<GameConfDTO> gameConfList = pkGameService.getGameConfList();
            if (!CollectionUtils.isEmpty(gameConfList)) {
                Calendar calendar = Calendar.getInstance();
                Integer hour = calendar.get(Calendar.HOUR_OF_DAY);
                Integer minute = calendar.get(Calendar.MINUTE);
                Integer second = calendar.get(Calendar.SECOND);
                Long time = hour * 3600L + minute * 60 + second;
                for (GameConfDTO gameConfDTO : gameConfList) {
                    List<OpenTimeDTO> openTimeDTOList = gameConfDTO.getOpenTime();
                    if(openTimeDTOList!=null){
                        for(int i=0;i<openTimeDTOList.size();i++){
                            OpenTimeDTO openTimeDTO = openTimeDTOList.get(i);
                            if (openTimeDTO.getEndTime() >= time) {
                                gameConfDTO.setStartTime(openTimeDTO.getStartTime());
                                gameConfDTO.setEndTime(openTimeDTO.getEndTime());
                                break;
                            }
                            if(i == openTimeDTOList.size()-1){
                                gameConfDTO.setStartTime(openTimeDTOList.get(0).getStartTime());
                                gameConfDTO.setEndTime(openTimeDTOList.get(0).getEndTime());
                            }
                        }
                    }
                }
                result.add("gameConfList", new JsonParser().parse(JSON.toJSONString(gameConfList)).getAsJsonArray()); 
            }
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("Error getPKGameList()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
        
        return result;
    }

    /**
     * 电台房获取pk游戏配置(51070311)
     *
     * @param jsonObject 请求对象
     * @param checkTag 是否验证token标记
     * @return
     */
    public JsonObject getPKGameListForAudio(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        try {
            result.add("gameConfList", new JsonParser().parse(configService.getKkPlayConfigForAudio()).getAsJsonArray());
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("Error getPKGameList()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }

        return result;
    }
    
    private boolean checkMatchOpenStatus(Integer gameId) {
        boolean isOpen = false;
        try {
            List<GameConfDTO> gameConfList = pkGameService.getGameConfList();
            Calendar calendar = Calendar.getInstance();
            Integer hour = calendar.get(Calendar.HOUR_OF_DAY);
            Integer minute = calendar.get(Calendar.MINUTE);
            Integer second = calendar.get(Calendar.SECOND);
            Long time = hour * 3600L + minute * 60 + second;
            for (GameConfDTO gameConfDTO : gameConfList) {
                if (gameId.equals(gameConfDTO.getGameId())) {
                    List<OpenTimeDTO> openTimeDTOList = gameConfDTO.getOpenTime();
                    if(openTimeDTOList!=null){
                        for(OpenTimeDTO openTimeDTO:openTimeDTOList){
                            if (openTimeDTO.getStartTime() <= time && openTimeDTO.getEndTime() >= time) {
                                isOpen = true;
                                break;
                            }
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Error checkMatchOpenStatus(), gameId: " + gameId, e);
        }
        return isOpen;
    }

}

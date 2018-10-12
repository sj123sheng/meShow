/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2018
 */
package com.melot.kktv.action;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

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
import com.melot.kk.pkgame.api.dto.GameConfDTO;
import com.melot.kk.pkgame.api.dto.GamePropDTO;
import com.melot.kk.pkgame.api.dto.MultipleConfDTO;
import com.melot.kk.pkgame.api.dto.QuziPKUserDTO;
import com.melot.kk.pkgame.api.dto.ReturnResult;
import com.melot.kk.pkgame.api.dto.UserKKPlayRank;
import com.melot.kk.pkgame.api.dto.UserKkplayScoreDTO;
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
                    //置顶时显示大海报
                    if (jsonObj.get("isTop").getAsBoolean()) {
                        jsonObj.addProperty("title_poster", jsonObj.get("full_title_poster").getAsString());
                    }
                    jsonObj.remove("full_title_poster");
                    Result<HallPartConfDTO> sysMenuResult = hallPartService.getPartList(jsonObj.get("cataId").getAsInt(), 0, 0, 0, AppIdEnum.AMUSEMENT, 0, 3);
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
                result.addProperty("nickname", userProfile.getNickName());
                result.addProperty("portrait", userProfile.getPortrait());
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
                        jsonObj.addProperty("nickname", userProfile.getNickName());
                        jsonObj.addProperty("portrait_path_original", userProfile.getPortrait());
                    }
                    rankList.add(jsonObj);
                }
                
                result.addProperty("userId", userId);
                Integer position = pkGameService.getUserKKplayRanking(userId, rankType, slotType);
                result.addProperty("position", position == null ? -1 : position);
                UserKkplayScoreDTO userKkplayScoreDTO = pkGameService.getUserKkplayScore(userId);
                if (userKkplayScoreDTO != null) {
                    result.addProperty("score", userKkplayScoreDTO.getTotalScore());
                }
                UserProfile userProfile = UserService.getUserInfoNew(userId);
                if (userProfile != null) {
                    result.addProperty("nickname", userProfile.getNickName());
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
        
        int userId, gameId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            gameId = CommonUtil.getJsonParamInt(jsonObject, "gameId", 0, "5107030401", 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            if (gameId != GameEnum.ANSWER_PK_FOUR.getCode() && gameId != GameEnum.ANSWER_PK_DOUBLE.getCode()) {
                result.addProperty("TagCode", "5107030402");
                return result;
            }
            if (!checkMatchOpenStatus(gameId)) {
                result.addProperty("TagCode", "5107030404");
            } else {
                ReturnResult<Integer> matchResp = matchService.startMatch(userId, UserTypeEnum.USER.getCode(), gameId);
                if (ReturnResultCode.SUCCESS.getCode().equals(matchResp.getCode())) {
                    result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                } else {
                    result.addProperty("TagCode", "5107030403");
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

        QuziPKUserDTO quziPKUserDTO = quizPKService.getUserDTO(userId);
        if (quziPKUserDTO != null) {
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
    
    private boolean checkMatchOpenStatus(int gameId) {
        boolean result = false;
        try {
            List<GameConfDTO> gameConfList = pkGameService.getGameConfList();
            for (GameConfDTO gameConfDTO : gameConfList) {
                if (gameConfDTO.getGameId() != null && gameId == gameConfDTO.getGameId()) {
                    long currentTime = System.currentTimeMillis();
                    long dayBeginTime = DateUtil.getDayBeginTime(currentTime);
                    if (gameConfDTO.getStartTime() == null || (((dayBeginTime 
                            + gameConfDTO.getStartTime()*1000) < currentTime) 
                            && (dayBeginTime + gameConfDTO.getEndTime()*1000) > currentTime)) {
                        result = true;
                    }
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Error checkMatchOpenStatus(), gameId: " + gameId, e);
        }
        return result;
    }

}

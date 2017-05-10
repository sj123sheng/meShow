/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.api.menu.sdk.service.RoomInfoService;
import com.melot.game.config.sdk.apply.service.MatchApplyService;
import com.melot.game.config.sdk.apply.service.MatchConfigInfoService;
import com.melot.game.config.sdk.barrage.service.VideoBarrageInfoService;
import com.melot.game.config.sdk.chat.service.ChatPhraseService;
import com.melot.game.config.sdk.domain.ActorVisitingCard;
import com.melot.game.config.sdk.domain.ChatPhrase;
import com.melot.game.config.sdk.domain.ConfMatchNews;
import com.melot.game.config.sdk.domain.ExtraRoomInfo;
import com.melot.game.config.sdk.domain.MatchConfigInfo;
import com.melot.game.config.sdk.domain.MatchGroupApplyInfo;
import com.melot.game.config.sdk.domain.MatchPlayerApplyInfo;
import com.melot.game.config.sdk.domain.VideoBarrageInfo;
import com.melot.game.config.sdk.match.service.ConfMatchNewsService;
import com.melot.game.config.sdk.room.service.ActorVisitingCardService;
import com.melot.game.config.sdk.room.service.ExtraRoomInfoService;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.DateUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.kktv.util.CommonUtil.ErrorGetParameterException;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * Title: GameApiAction
 * <p>
 * Description: 游戏直播相关接口类
 * </p>
 * @author  姚国平<a href="mailto:guoping.yao@melot.cn">
 * @version V1.0
 * @since 2015-3-3 下午2:29:16 
 */
public class GameApiAction extends BaseAction {

    private static Logger logger = Logger.getLogger(GameApiAction.class);
    
    /**
     * 主播提交房间申请信息 (20020064)
     * 
     */
    public JsonObject setRoomExtraIntroduceInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request){
        JsonObject result = new JsonObject();
        
        if (!checkTag) {
            result.addProperty(TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
    
        Integer roomId = null;
        String introduce = null;
        
        try{
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
            introduce = CommonUtil.getJsonParamString(jsonObject, "introduce", "",TagCodeEnum.APPID_MISSING, 0, Integer.MAX_VALUE);
        }catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(TAG_CODE, e.getErrCode());
            return result;
        }
        try {
            ExtraRoomInfoService extraRoomInfoService = MelotBeanFactory.getBean("extraRoomInfoService",ExtraRoomInfoService.class);
            extraRoomInfoService.sumitApply(roomId, introduce, true);
        } catch (Exception e) {
            logger.error("Fail to call extraRoomInfoService.sumitApply ", e);
        }
        result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);

        return result;
    
    }
    /**
     * 根据房间roomId 获取直播自己设置的房间介绍 (20020063)
     * @param jsonObject:{roomId:10086,userId:10086}
     * @return
     */
    public JsonObject getRoomExtraIntroduceInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request){
        JsonObject result = new JsonObject();
        Integer roomId = null;
        Integer userId = null;
        
        try {
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(TAG_CODE, e.getErrCode());
            return result;
        }
        
        ExtraRoomInfo queryInfo = null;
        try {
            if (userId == 0) {
                userId = null;
            }
            ExtraRoomInfoService extraRoomInfoService = MelotBeanFactory.getBean("extraRoomInfoService",ExtraRoomInfoService.class);
            queryInfo = extraRoomInfoService.getExtraRoomInfo(roomId, userId);
        } catch (Exception e) {
            logger.error("Fail to call extraRoomInfoService.getExtraRoomInfo ", e);
        }
        if (queryInfo != null && queryInfo.getRoomInreoduce() != null) {
            result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
            result.addProperty("introduce",queryInfo.getRoomInreoduce());
            result.addProperty("status", queryInfo.getCheckedStatus());
            result.addProperty("refusedReason", queryInfo.getRefusedReason() == null ? "" : queryInfo.getRefusedReason());
        } else {
            result.addProperty(TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
        }
        
        return result;
    }
    
    /**
     * 游戏直播获取聊天热词 (20020062)
     * 
     */
    public JsonObject getChatPhrases(JsonObject jsonObject, boolean checkTag, HttpServletRequest request){
        JsonObject result = new JsonObject();
        ChatPhraseService chatPhraseService = null;
        
        try {
            chatPhraseService =  MelotBeanFactory.getBean("chatPhraseService",ChatPhraseService.class);
            List<ChatPhrase>list = chatPhraseService.getAllChatPhrases();
            if (list == null || list.size() == 0) {
                result.addProperty(TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            JsonArray jsonArray = new JsonArray();
            for (ChatPhrase chatPhrase : list) {
                JsonObject json = new JsonObject();
                json.addProperty("key_name", chatPhrase.getPhraeName());
                json.addProperty("key_value", chatPhrase.getAnimateUrl());
                jsonArray.add(json);
            }
            result.add("phrases", jsonArray);
            result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("Fail to call extraRoomInfoService.getExtraRoomInfo ", e);
        }
        
        return result;
    }
    
    
    /**
     * 根据离线视频id 获取对应的全部弹幕 (20020066)
     * @param jsonObject:{vidoeId:10086}
     * @return
     */
    public JsonObject getVideoBarrage(JsonObject jsonObject, boolean checkTag, HttpServletRequest request){
        JsonObject result = new JsonObject();
        Integer videoId = null;
        
        try {
            videoId = CommonUtil.getJsonParamInt(jsonObject, "videoId", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(TAG_CODE, e.getErrCode());
            return result;
        }
        
        List<VideoBarrageInfo> list = null;
        try {
            VideoBarrageInfoService videoBarrageInfoService = MelotBeanFactory.getBean("videoBarrageInfoService", VideoBarrageInfoService.class);
            list = videoBarrageInfoService.getVideoBarrageInfosByVideoId(videoId);
            JsonArray jsonArray = new JsonArray();
            for (VideoBarrageInfo videoBarrageInfo : list) {
                JsonObject json = new JsonObject();
                json.addProperty("barrageId", videoBarrageInfo.getId());
                json.addProperty("uploader", videoBarrageInfo.getUploadUserId());
                json.addProperty("time", videoBarrageInfo.getPointInTime());
                json.addProperty("msg", videoBarrageInfo.getBarrageMsg());
                jsonArray.add(json);
            }
            result.add("barrages", jsonArray);
            result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);     
        } catch (Exception e) {
            logger.error("Fail to call extraRoomInfoService.getExtraRoomInfo ", e);
            result.addProperty(TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
        }
        
        return result;
    }
    
    
    /**
     * 保存离线视频弹幕 (20020065)
     * {videoId:10086,msg:"158551",time:125212,uploader:12581}
     */
    public JsonObject saveVideoBarrage(JsonObject jsonObject, boolean checkTag, HttpServletRequest request){
        JsonObject result = new JsonObject();
        Integer videoId = null;
        Integer uploaderId = null;
        Integer pointTime = null;
        String message = null;
        Integer code = null;
        try {
            videoId = CommonUtil.getJsonParamInt(jsonObject, "videoId", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
            uploaderId = CommonUtil.getJsonParamInt(jsonObject, "uploader", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
            pointTime = CommonUtil.getJsonParamInt(jsonObject, "time", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
            message = CommonUtil.getJsonParamString(jsonObject, "msg", "",TagCodeEnum.APPID_MISSING, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(TAG_CODE, e.getErrCode());
            return result;
        }
        try {
            VideoBarrageInfo videoBarrageInfo = new VideoBarrageInfo();
            videoBarrageInfo.setVideoId(videoId);
            videoBarrageInfo.setBarrageMsg(message);
            videoBarrageInfo.setPointInTime(pointTime);
            videoBarrageInfo.setUploadUserId(uploaderId);
            VideoBarrageInfoService videoBarrageInfoService = MelotBeanFactory.getBean("videoBarrageInfoService", VideoBarrageInfoService.class);
            code = videoBarrageInfoService.saveVideoBarrageInfo(videoBarrageInfo);
        } catch (Exception e) {
            logger.error("Fail to call extraRoomInfoService.sumitApply ", e);
        }
        if (code != null && code > 0) {
            result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
            result.addProperty("barrageId", code);
        } else {
            result.addProperty(TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }

        return result;
    }
    
    /**
     *  获取主播设置的全部游戏名片 [fucTag=20020067]
     * 
     */
    public JsonObject getAllGameVisitingCards(JsonObject jsonObject, boolean checkTag, HttpServletRequest request){
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty(TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        ActorVisitingCardService actorVisitingCardService = null;
        Integer userId = null;        
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(TAG_CODE, e.getErrCode());
            return result;
        }
        
        try {
            actorVisitingCardService = MelotBeanFactory.getBean("actorVisitingCardService", ActorVisitingCardService.class);
            List<ActorVisitingCard>list = actorVisitingCardService.getActorVisitingCardsByActorId(userId);
            if (list == null || list.size() == 0) {
                result.addProperty(TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            JsonArray jsonArray = new JsonArray();
            for (ActorVisitingCard card : list) {
                JsonObject json = new JsonObject();
                json.addProperty("cardId", card.getCardId());
                json.addProperty("actorId", card.getActorId());
                json.addProperty("gameName", card.getGameName());
                json.addProperty("serverName", card.getServerName());
                json.addProperty("gameLevel", card.getGameLevel());
                json.addProperty("playerInfo",card.getPlayerInfo());
                json.addProperty("cardStatus", card.getCardStatus());
                jsonArray.add(json);
            }
            result.add("cards", jsonArray);
            result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("Fail to call actorVisitingCardService.getActorVisitingCardsByActorId ", e);
        }
        
        return result; 
    }
    
    
    
    
    /**
     * 保存一个主播的游戏名片 [fucTag=20020068]
     * 
     */
    public JsonObject saveGameVisitingCard(JsonObject jsonObject, boolean checkTag, HttpServletRequest request){
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty(TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        ActorVisitingCardService actorVisitingCardService = null;        
        Integer userId = null;
        String gameName = null;
        String serverName = null;
        String gameLevel = null;
        String playerInfo = null;
        Integer cardStatus = null;
        Integer code = null;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
            gameName = CommonUtil.getJsonParamString(jsonObject, "gameName", "",TagCodeEnum.APPID_MISSING, 0, Integer.MAX_VALUE);
            serverName = CommonUtil.getJsonParamString(jsonObject, "serverName", "", null, 0, Integer.MAX_VALUE);
            gameLevel = CommonUtil.getJsonParamString(jsonObject, "gameLevel", "", null, 0, Integer.MAX_VALUE);
            playerInfo = CommonUtil.getJsonParamString(jsonObject, "playerInfo", "", null, 0, Integer.MAX_VALUE);
            cardStatus = CommonUtil.getJsonParamInt(jsonObject, "cardStatus", 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(TAG_CODE, e.getErrCode());
            return result;
        }
        try {
            ActorVisitingCard actorVisitingCard = new ActorVisitingCard();
            actorVisitingCardService = MelotBeanFactory.getBean("actorVisitingCardService", ActorVisitingCardService.class);
            actorVisitingCard.setActorId(userId);
            actorVisitingCard.setGameName(gameName);
            actorVisitingCard.setServerName(serverName);
            actorVisitingCard.setGameLevel(gameLevel);
            actorVisitingCard.setPlayerInfo(playerInfo);
            actorVisitingCard.setCardStatus(cardStatus);
            code = actorVisitingCardService.saveActorVisitingCard(actorVisitingCard);
        } catch (Exception e) {
            logger.error("Fail to call actorVisitingCardService.saveActorVisitingCard ", e);
        }
        if (code != null && code > 0) {
            result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
        } else {
            result.addProperty(TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            result.addProperty("errCode", code);
        }
        
        return result;
    }
    
    /**
     * 更新一个主播的游戏名片 [fucTag=20020069]
     * 
     */
    public JsonObject updateGameVisitingCard(JsonObject jsonObject, boolean checkTag, HttpServletRequest request){
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty(TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        ActorVisitingCardService actorVisitingCardService = null;        
        Integer userId = null;
        Integer cardId = null;
        String gameName = null;
        String serverName = null;
        String gameLevel = null;
        String playerInfo = null;
        Integer cardStatus = null;
        boolean updateFlag = false;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
            cardId = CommonUtil.getJsonParamInt(jsonObject, "cardId", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
            gameName = CommonUtil.getJsonParamString(jsonObject, "gameName", "",TagCodeEnum.APPID_MISSING, 0, Integer.MAX_VALUE);
            serverName = CommonUtil.getJsonParamString(jsonObject, "serverName", null, null, 0, Integer.MAX_VALUE);
            gameLevel = CommonUtil.getJsonParamString(jsonObject, "gameLevel", null, null, 0, Integer.MAX_VALUE);
            playerInfo = CommonUtil.getJsonParamString(jsonObject, "playerInfo", null, null, 0, Integer.MAX_VALUE);
            cardStatus = CommonUtil.getJsonParamInt(jsonObject, "cardStatus", 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(TAG_CODE, e.getErrCode());
            return result;
        }
        
        try {
            ActorVisitingCard actorVisitingCard = new ActorVisitingCard();
            actorVisitingCardService = MelotBeanFactory.getBean("actorVisitingCardService", ActorVisitingCardService.class);
            actorVisitingCard.setActorId(userId);
            actorVisitingCard.setCardId(cardId);
            actorVisitingCard.setGameName(gameName);
            actorVisitingCard.setServerName(serverName);
            actorVisitingCard.setGameLevel(gameLevel);
            actorVisitingCard.setPlayerInfo(playerInfo);
            actorVisitingCard.setCardStatus(cardStatus);
            updateFlag = actorVisitingCardService.updateActorVisitingCardByCardId(actorVisitingCard);
        } catch (Exception e) {
            logger.error("Fail to call actorVisitingCardService.updateActorVisitingCardByCardId ", e);
        }
        if (updateFlag) {
            result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
        } else {
            result.addProperty(TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        
        return result;
    }
    
    
    /**
     * 删除一个主播的游戏名片 [fucTag=20020070]
     * 
     */
    public JsonObject deleteGameVisitingCard(JsonObject jsonObject, boolean checkTag, HttpServletRequest request){
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty(TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        ActorVisitingCardService actorVisitingCardService = null;       
        Integer userId = null;
        Integer cardId = null;
        boolean deleteFlag = false;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
            cardId = CommonUtil.getJsonParamInt(jsonObject, "cardId", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(TAG_CODE, e.getErrCode());
            return result;
        }
        
        try {
            actorVisitingCardService = MelotBeanFactory.getBean("actorVisitingCardService", ActorVisitingCardService.class);
            deleteFlag = actorVisitingCardService.deleteActorVisitingCardByCardId(cardId, userId);
        } catch (Exception e) {
            logger.error("Fail to call actorVisitingCardService.updateActorVisitingCardByCardId ", e);
        }
        if (deleteFlag) {
            result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
        } else {
            result.addProperty(TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        
        return result;
    }
    
    /**
     *  获取房间设置的游戏名片 [fucTag=20020071]
     * 
     */
    public JsonObject getGameVisitingCardByUserId(JsonObject jsonObject, boolean checkTag, HttpServletRequest request){
        JsonObject result = new JsonObject();
        
        ActorVisitingCardService actorVisitingCardService = null;
        Integer userId = null;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(TAG_CODE, e.getErrCode());
            return result;
        }
        try {
            actorVisitingCardService = MelotBeanFactory.getBean("actorVisitingCardService", ActorVisitingCardService.class);
            List<ActorVisitingCard>list = actorVisitingCardService.getActorVisitingCardsByActorId(userId);
            if(list == null || list.size() == 0) {
                result.addProperty(TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            ActorVisitingCard actorVisitingCard = null;
            for (ActorVisitingCard card : list) {
                if(card.getCardStatus() == 0) {
                    //非激活状态卡片不返回
                    continue;
                }
                actorVisitingCard = card;
                break;
            }
            if (actorVisitingCard != null) {
                result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
                result.addProperty("cardId", actorVisitingCard.getCardId());
                result.addProperty("actorId", actorVisitingCard.getActorId());
                result.addProperty("gameName", actorVisitingCard.getGameName());
                result.addProperty("serverName", actorVisitingCard.getServerName());
                result.addProperty("gameLevel", actorVisitingCard.getGameLevel());
                result.addProperty("playerInfo",actorVisitingCard.getPlayerInfo());
                result.addProperty("cardStatus", actorVisitingCard.getCardStatus());                
            } else {
                //未发现激活的名片
                result.addProperty(TAG_CODE, TagCodeEnum.REPORT_ERROR);
            }
        } catch (Exception e) {
            logger.error("Fail to call actorVisitingCardService.getActorVisitingCardsByActorId ", e);
        }
        return result; 
    }
    
    
    /**
     * 游戏赛区列表获取  [fucTag=20020072 ]
     * 
     */
    public JsonObject getAllMatchesByActivityId (JsonObject jsonObject, boolean checkTag, HttpServletRequest request){
        JsonObject result = new JsonObject();
        
        MatchConfigInfoService matchConfigInfoService = null;
        RoomInfoService roomInfoService = null;        
        Integer activityId = null;        
        try {
            activityId = CommonUtil.getJsonParamInt(jsonObject, "activityId", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(TAG_CODE, e.getErrCode());
            return result;
        }
        
        try {
            matchConfigInfoService = MelotBeanFactory.getBean("matchConfigInfoService", MatchConfigInfoService.class);
            roomInfoService = MelotBeanFactory.getBean("roomInfoService", RoomInfoService.class);
            
            List<MatchConfigInfo> list = matchConfigInfoService.getMatchConfigInfosByActivityId(activityId);
            if (list == null || list.size() == 0) {
                result.addProperty(TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            String roomStrings = "";
            JsonArray jsonArray = new JsonArray();
            for (MatchConfigInfo match : list) {
                roomStrings += match.getLiveRoomId()+",";
            }
            Map<Integer,RoomInfo> roomMap = getRoomInfoMapByActorIds(roomInfoService, roomStrings);           
            for (MatchConfigInfo match : list) {
                JsonObject json = new JsonObject();
                json.addProperty("matchId", match.getMatchId());
                json.addProperty("matchName",match.getMatchName());
                json.addProperty("matchAddress", match.getMatchAddress());
                json.addProperty("phone", match.getPhone());
                json.addProperty("liveRoomId", match.getLiveRoomId());
                RoomInfo roomInfo = roomMap.get(match.getLiveRoomId());
                if (roomInfo == null || !roomInfo.isOnLive()) {
                    json.addProperty("isOnLive", false);
                    json.addProperty("livePoster", roomInfo == null ? "" : roomInfo.getPoster());
                } else {
                    json.addProperty("isOnLive", true);
                    json.addProperty("livePoster", roomInfo.getLivePoster() == null ? roomInfo.getPoster() : roomInfo.getLivePoster());
                }
                jsonArray.add(json);
            }           
            result.add("matches", jsonArray);
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
            result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("Fail to call extraRoomInfoService.getExtraRoomInfo ", e);
        }
        
        return result;
    }
    
    /**
     * 查询匹配的房间列表,并转化为map 
     * 
     */
    private static Map<Integer, RoomInfo> getRoomInfoMapByActorIds (RoomInfoService roomInfoService, String roomIds){
        String queryString = roomIds.endsWith(",") ? roomIds.substring(0,roomIds.length()-1) : roomIds;
        List<RoomInfo>list = roomInfoService.getRoomListByRoomIds(queryString);
        Map<Integer,RoomInfo>result = new HashMap<Integer, RoomInfo>();
        for (RoomInfo roomInfo : list) {
            result.put(roomInfo.getActorId(), roomInfo);
        }
        return result;
    }
    
    /**
     * 获取某个赛区信息  [fucTag=20020073]
     * 
     */
    public JsonObject getMatchInfoById(JsonObject jsonObject, boolean checkTag, HttpServletRequest request){
        JsonObject result = new JsonObject();
        
        MatchConfigInfoService matchConfigInfoService = null;
        Integer activityId = null;
        Integer matchId = null;
        try {
            activityId = CommonUtil.getJsonParamInt(jsonObject, "activityId", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
            matchId = CommonUtil.getJsonParamInt(jsonObject, "matchId", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(TAG_CODE, e.getErrCode());
            return result;
        }
        try {
            matchConfigInfoService =  MelotBeanFactory.getBean("matchConfigInfoService", MatchConfigInfoService.class);
            MatchConfigInfo match = matchConfigInfoService.getMatchConfigInfoByMatchId(matchId);
            if (match == null || !activityId.equals(match.getActivityId())) {
                result.addProperty(TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
            result.addProperty("matchId", match.getMatchId());
            result.addProperty("activityId", match.getActivityId());
            result.addProperty("matchName", match.getMatchName());
            result.addProperty("matchAddress", match.getMatchAddress());
            result.addProperty("phone", match.getPhone());
            result.addProperty("type", match.getApplyType());
            result.addProperty("minPlayer", match.getMinPlayer());
            if (match.getBeginTime() != null) {
                result.addProperty("beginTime", match.getBeginTime().getTime());
            }
            if (match.getEndTime() != null) {
                result.addProperty("endTime", match.getEndTime().getTime());
            }
        } catch (Exception e) {
            logger.error("Fail to call extraRoomInfoService.getExtraRoomInfo ", e);
        }
        
        return result;
    }
    
    /**
     * 保存赛区报名信息  [fucTag=20020074 ]
     * 
     */
    public JsonObject saveMatchApply(JsonObject jsonObject, boolean checkTag, HttpServletRequest request){
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty(TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        MatchApplyService matchApplyService = null;
        Integer userId = null;
        Integer type = null;
        Integer matchId = null;
        String name = null;
        String contact = null;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
            type = CommonUtil.getJsonParamInt(jsonObject, "type", 0, TagCodeEnum.APPID_MISSING, 0, Integer.MAX_VALUE);
            matchId = CommonUtil.getJsonParamInt(jsonObject, "matchId", 0, TagCodeEnum.APPID_MISSING, 0, Integer.MAX_VALUE);
            name = CommonUtil.getJsonParamString(jsonObject, "name", "", TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(TAG_CODE, e.getErrCode());
            return result;
        }
        
        try {
            matchApplyService = MelotBeanFactory.getBean("matchApplyService", MatchApplyService.class);
            if (type == 1) { //个人报名
                MatchPlayerApplyInfo matchPlayerApplyInfo = new MatchPlayerApplyInfo();
                Integer gender = CommonUtil.getJsonParamInt(jsonObject, "gender", 0, TagCodeEnum.APPID_MISSING, 0, Integer.MAX_VALUE);
                String identity = CommonUtil.getJsonParamString(jsonObject, "identity", "",TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
                contact = CommonUtil.getJsonParamString(jsonObject, "contact", "",TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
                matchPlayerApplyInfo.setUserId(userId);
                matchPlayerApplyInfo.setMatchId(matchId);
                matchPlayerApplyInfo.setGroupType(1);
                matchPlayerApplyInfo.setGroupId(0);
                matchPlayerApplyInfo.setName(name);
                matchPlayerApplyInfo.setIdentityNum(identity);
                matchPlayerApplyInfo.setGender(gender);
                matchPlayerApplyInfo.setContactInfo(contact);
                matchApplyService.saveMatchApply(matchPlayerApplyInfo);               
            } else if (type == 2) { // 战队报名
                MatchGroupApplyInfo matchGroupApplyInfo = new MatchGroupApplyInfo();
                matchGroupApplyInfo.setMatchId(matchId);
                matchGroupApplyInfo.setGroupName(name);
                matchGroupApplyInfo.setUserId(userId);
                matchGroupApplyInfo.setContactInfo(contact);
                matchGroupApplyInfo.setMatchPlayerApplyInfos(getMatchPlayerApplyInfoByParseJson(jsonObject));             
                matchApplyService.saveMatchApply(matchGroupApplyInfo);
            } else {
                result.addProperty(TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);//报名类型不支持
                return result;
            }
        } catch (CommonUtil.ErrorGetParameterException e2) {
            result.addProperty(TAG_CODE, e2.getErrCode());
            return result;
        } catch (Exception e) {
            logger.error("Fail to call extraRoomInfoService.sumitApply ", e);
            result.addProperty(TAG_CODE, TagCodeEnum.EXECSQL_EXCEPTION);
        }
        result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);

        return result;
    }
    
    /**
     *  //获取某个赛区新闻
     *    20020075
     * 
     */
    public JsonObject getMatchNews(JsonObject jsonObject, boolean checkTag, HttpServletRequest request){
        JsonObject result = new JsonObject();
        
        ConfMatchNewsService confMatchNewsService = null;
        Integer activityId = null;
        Integer matchId = null;
        Integer start = null;
        Integer num = null;
        try {
            activityId = CommonUtil.getJsonParamInt(jsonObject, "activityId", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
            matchId = CommonUtil.getJsonParamInt(jsonObject, "matchId", 0, null, 1, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, null, 0, Integer.MAX_VALUE);
            num = CommonUtil.getJsonParamInt(jsonObject, "num", 7, null, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(TAG_CODE, e.getErrCode());
            return result;
        }
        
        try {
            matchId = matchId == 0 ? null : matchId; 
            confMatchNewsService = MelotBeanFactory.getBean("confMatchNewsService", ConfMatchNewsService.class);
            List<ConfMatchNews>list = confMatchNewsService.getConfMatchNewssByActivityIdAndMatchId(activityId, matchId, start, num);
            if (list == null || list.size() == 0) {
                result.addProperty(TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            JsonArray jsonArray = new JsonArray();
            for (ConfMatchNews confMatchNews : list) {
                JsonObject json = new JsonObject();
                json.addProperty("newsId", confMatchNews.getNewsId());
                json.addProperty("newsTime", confMatchNews.getUpdateTime().getTime());
                json.addProperty("formatNewsTime", DateUtil.formatDate(confMatchNews.getUpdateTime(), null));
                json.addProperty("title", confMatchNews.getTitle());
                json.addProperty("newsUrl", confMatchNews.getNewsUrl());
                if (confMatchNews.getContent() != null) {
                    json.addProperty("content", confMatchNews.getContent());
                }
                jsonArray.add(json);
            }
            result.addProperty("newsTotal", confMatchNewsService.getValiableNewsCount(activityId, matchId));
            result.add("news", jsonArray);
            result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("Fail to call extraRoomInfoService.getExtraRoomInfo ", e);
        }
        
        return result;
    }
    
    
    
    private static List<MatchPlayerApplyInfo> getMatchPlayerApplyInfoByParseJson(JsonObject jsonObject) throws ErrorGetParameterException{
        
        JsonArray array = jsonObject.getAsJsonArray("players");
        List<MatchPlayerApplyInfo> list = null;
        if (array == null || array.size() == 0) {
            throw new CommonUtil.ErrorGetParameterException(TagCodeEnum.APPID_MISSING);
        }
        list = new ArrayList<MatchPlayerApplyInfo>();
        for (int i=0, size = array.size(); i<size; i++) {
            JsonObject element =  array.get(i).getAsJsonObject();
            MatchPlayerApplyInfo player = new MatchPlayerApplyInfo();
            player.setName(element.get("name").getAsString());
            player.setIdentityNum(element.get("identity").getAsString());
            player.setGender(element.get("gender").getAsInt());
            player.setContactInfo(element.get("contact").getAsString());
            list.add(player);
        }
        
        return list;
    }
    
    
}

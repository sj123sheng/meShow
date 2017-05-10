/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.action;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.api.menu.sdk.dao.domain.Version;
import com.melot.api.menu.sdk.service.VersionService;
import com.melot.content.config.domain.GamePromotion;
import com.melot.content.config.domain.PromotionRoom;
import com.melot.content.config.game.service.GamePromotionService;
import com.melot.content.config.promote.service.PromotionRoomService;
import com.melot.kktv.action.IndexFunctions;
import com.melot.kktv.util.ChannelEnum;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.SecretKeyUtil;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * Title: GameHallFunction
 * <p>
 * Description: 游戏中心接口
 * </p>
 * 
 * @author 陈磊<a href="mailto:lei.chen@melot.cn">
 * @version V1.0
 * @since 2015-6-12 下午3:36:17
 */
public class GameHallFunction extends BaseAction{
    
    private static Logger logger = Logger.getLogger(GameHallFunction.class);
    
    /**
     * 游戏列表查询(20020054)
     * @param jsonObject
     * @return
     */
    public JsonObject getGameList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        int platform = 0;
        try {
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, TagCodeEnum.PLATFORM_MISSING, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        List<GamePromotion> gamePromotionList = null;
        try {
            GamePromotionService gamePromotionService = MelotBeanFactory.getBean("gamePromotionService", GamePromotionService.class);
            gamePromotionList = gamePromotionService.sortResult(gamePromotionService.getGamePromotionsByTerminal(platform));
        } catch (Exception e) {
            logger.error("Fail to call gamePromotionService.getGamePromotionsByTerminal ", e);
        }
        
        if (gamePromotionList != null) {
            JsonArray gameList = new JsonArray();
            for (GamePromotion gamePromotion : gamePromotionList) {
                JsonObject json = new JsonObject();
                json.addProperty("type", gamePromotion.getType());
                json.addProperty("recommendType", gamePromotion.getRecommendType());
                json.addProperty("id", gamePromotion.getId());
                if (gamePromotion.getSupportVersion() != null) {
                    json.addProperty("supportVersion;", gamePromotion.getSupportVersion());
                }
                json.addProperty("downloadUrl", gamePromotion.getGameUrl());
                if (gamePromotion.getPackageName() != null) {
                    json.addProperty("packageName", gamePromotion.getPackageName());
                }
                if (gamePromotion.getStartClassName() != null) {
                    json.addProperty("startName", gamePromotion.getStartClassName());
                }
                json.addProperty("name", gamePromotion.getName());
                json.addProperty("icon", gamePromotion.getIcon());
                if (gamePromotion.getDescription() != null) {
                    json.addProperty("description", gamePromotion.getDescription());
                }
                json.addProperty("onlineCount", gamePromotion.getPeopleInGame());
                gameList.add(json);
                if (gamePromotion.getAppId() != null) {
                    json.addProperty("roomSource", gamePromotion.getAppId());
                    json.addProperty("roomType", gamePromotion.getAppId());
                }
                
            }
            result.addProperty("size", gamePromotionList.size());
            result.add("gameList", gameList);
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } else {
            result.addProperty("TagCode", TagCodeEnum.FAIL_TO_CALL_API_CONTENT_MODULE);
        }
        return result;
    }   
    
    /**
     * 入口状态查询(20020055)
     * @return
     */
    public JsonObject getVesionByAC(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        int platform = 0;
        int appId = 0;
        int channel = 0;
        try {
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
            channel = CommonUtil.getJsonParamInt(jsonObject, "c", 0, TagCodeEnum.CHANNEL_MISSING, 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, TagCodeEnum.PLATFORM_MISSING, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        Version version = null;
        try {
            VersionService versionService = MelotBeanFactory.getBean("versionService", VersionService.class);
            version = versionService.getVersionByAppIdChannelId(appId, channel);
        } catch (Exception e) {
            logger.error("Fail to call versionService.getVersionByAppIdChannelId", e);
        }
        if (version != null) {
            if (version.getIsGamePromoted() == 0) {
                result.addProperty("TagCode", TagCodeEnum.UNABEL_PROMOTE_GAME);
            } else {
                List<GamePromotion> gamePromotionList = null;
                try {
                    GamePromotionService gamePromotionService = MelotBeanFactory.getBean("gamePromotionService", GamePromotionService.class);
                    gamePromotionList = gamePromotionService.getGamePromotionsByTerminal(platform);
                } catch (Exception e) {
                    logger.error("Fail to call gamePromotionService.getGamePromotionsByTerminal ", e);
                }
                if (gamePromotionList != null) {
                    result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                    int code = 0;
                    for (GamePromotion gamePromotion : gamePromotionList) {
                        code +=gamePromotion.getId();
                    }
                    result.addProperty("gameListMD5", SecretKeyUtil.encryptMD5(String.valueOf(code)));
                } else {
                    result.addProperty("TagCode", TagCodeEnum.FAIL_TO_CALL_API_CONTENT_MODULE);
                }
            }
        } else {
            result.addProperty("TagCode", TagCodeEnum.FAIL_TO_CALL_API_MENU_MODULE);
        }
        
        return result;
    }
    
    /**
     * kk游戏搜索(20020057)
     * @return
     * @throws Exception 
     */
    public JsonObject searchRoomForGame(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        String keywords = null;
        Integer start = 0;
        Integer num = 0;
        int platform = PlatformEnum.WEB;
        try {
            keywords = CommonUtil.getJsonParamString(jsonObject, "keywords", null, null , 1, 20);
            start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, null, 1, Integer.MAX_VALUE);
            num = CommonUtil.getJsonParamInt(jsonObject, "offset", 0, null, 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        if (start == 0) start = null;
        if (num == 0) num = null;
        
        JsonObject paramJsonObject = new JsonObject();
        paramJsonObject.addProperty("platform", platform);
        paramJsonObject.addProperty("fuzzyString", keywords);
        paramJsonObject.addProperty("pageNum", 0);
        paramJsonObject.addProperty("pageCount", 20);
        
        IndexFunctions indexFunctions = (IndexFunctions) MelotBeanFactory.getBean("indexFunction");
        return indexFunctions.findRoomList(paramJsonObject, checkTag, request);
    }
    
    /**
     * 获得运营编辑主播列表(20020056)
     * @param jsonObject
     * @return
     * 主页渠道号为100101, 默认获取全部主站推荐
     * 二级页面只获取当前相关推荐, 需要渠道号作为标识
     * 考虑到非主站推荐推荐过多, 现取消100101展现全部推荐
     * 
     * 
     */
    public JsonObject getBackSetRecommend (JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        int appId = 0;
        int num = 0;
        int channel = ChannelEnum.DEFAUL_WEB_CHANNEL; //web主站首页渠道号
        try {
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", 0, TagCodeEnum.APPID_MISSING, 0, Integer.MAX_VALUE);
            channel = CommonUtil.getJsonParamInt(jsonObject, "c", ChannelEnum.DEFAUL_WEB_CHANNEL, null, 0, Integer.MAX_VALUE);
            num = CommonUtil.getJsonParamInt(jsonObject, "num", 6, null, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        List<PromotionRoom> roomList = null;
        try {
            PromotionRoomService promotionRoomService = MelotBeanFactory.getBean("promotionRoomService", PromotionRoomService.class);
            roomList = promotionRoomService.getRecommondRoomsByAppIdAndType(appId, channel, num);
        } catch (Exception e) {
            logger.error("Fail to call promotionRoomService.getRecommondRoomsByAppId", e);
        }
        if (roomList != null) {
            JsonArray roomArray = new JsonArray();
            for (PromotionRoom room : roomList) {
                roomArray.add(fomateToJson(room));
            }
            result.add("roomList", roomArray);
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } else {
            result.addProperty("TagCode", TagCodeEnum.FAIL_TO_CALL_API_CONTENT_MODULE);
        }
        
        return result;
    }

    /**
     * 将PromotionRoom 转化为json
     * @param room
     * @return
     */
    private JsonObject fomateToJson(PromotionRoom room) {
        JsonObject json = new JsonObject();
        json.addProperty("roomId", room.getRoomId());
        if (room.getAppId() != null)
            json.addProperty("appId", room.getAppId());
        if (room.getRecmdReason() != null)
            json.addProperty("recmdReason", room.getRecmdReason());
        if (room.getNickname() != null)
            json.addProperty("nickname", room.getNickname());
        if (room.getPoster() != null) 
            json.addProperty("poster", room.getPoster());
        if (room.getGender() != null)
            json.addProperty("gender", room.getGender());
        if (room.getRecmdTag() != null)
            json.addProperty("roomTag", room.getRoomTag());
        if (room.getPeopleInRoom() != null)
            json.addProperty("peopleInRoom", room.getPeopleInRoom());
        return json;
    }
    
}

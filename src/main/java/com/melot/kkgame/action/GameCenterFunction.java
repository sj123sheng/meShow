/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2016
 */
package com.melot.kkgame.action;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.kktv.redis.GameRankingSource;
import com.melot.kktv.util.AppChannelEnum;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.TagCodeEnum;

/**
 * Title: GameCenterFunction
 * <p>
 * Description: 增值中心游戏接口
 * </p>
 * @author  姚国平<a href="mailto:guoping.yao@melot.cn">
 * @version V1.0
 * @since 2016-3-1 下午6:41:20 
 */
public class GameCenterFunction {

    private static Logger logger = Logger.getLogger(GameCenterFunction.class);
    
    private JsonParser parse = new JsonParser();
    
    /**
     *增值业务游戏中心版本查询(80001002)
     */
    public JsonObject getVersion(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        int appId = 0;
        int platform = 0;
        try {
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", 0, TagCodeEnum.APPID_MISSING, 0, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, TagCodeEnum.PLATFORM_MISSING, 1, Integer.MAX_VALUE);
            Map<String,String>version = GameRankingSource.getVersion(appId, platform);
            if (version == null || version.isEmpty()) {
                result.addProperty("version", "nogame");
                result.add("updateList", new JsonArray());
            } else {
                result.addProperty("version", version.get("version"));
                result.add("updateList", parse.parse(version.get("updateList")).getAsJsonArray());
            }
        } catch (CommonUtil.ErrorGetParameterException e) {
            logger.error("getVersion.ErrorGetParameterException", e);
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     *增值业务游戏中心版本查询(80001001)
     */
    public JsonObject getGameList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        int appId = 0;
        int platform = 0;
        int channel = 0;
        int versionCode = 0;               
        try {
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", 0, TagCodeEnum.APPID_MISSING, 0, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, TagCodeEnum.PLATFORM_MISSING, 1, Integer.MAX_VALUE);
            channel = CommonUtil.getJsonParamInt(jsonObject, "c", AppChannelEnum.KK, null, 0, Integer.MAX_VALUE);
            versionCode = CommonUtil.getJsonParamInt(jsonObject, "v", 0, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
            
            //oppo 渠道不返回游戏列表
            if (channel == 70220 && versionCode == 116) {
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            }
            
            String gameList = GameRankingSource.getGameList(appId, platform);
            result.addProperty("layoutType", 1);
            if (gameList == null) {
                result.add("gameList", new JsonArray());
            } else {
                JsonArray parseArray = parse.parse(gameList).getAsJsonArray();
                result.add("gameList", parseArray);
            }
        } catch (CommonUtil.ErrorGetParameterException e) {
            logger.error("getVersion.ErrorGetParameterException", e);
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
}

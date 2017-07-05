/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.action;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.kkgame.redis.RankingListSource;
import com.melot.kkgame.redis.support.RedisException;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.kktv.util.CommonUtil;

/**
 * Title: RankingListFunction
 * <p>
 * Description: kk直播榜单接口
 * </p>
 * @author  姚国平<a href="mailto:guoping.yao@melot.cn">
 * @version V1.0
 * @since 2015-6-10 下午4:31:23 
 */
public class RankingListFunction extends BaseAction{
   
    private static final Integer GIFT_RANKING_TYPE = 3;
    
    private RankingListSource rankingListSource;
    
    public void setRankingListSource(RankingListSource rankingListSource) {
        this.rankingListSource = rankingListSource;
    }

    /**
     * 获取直播平台榜单排行榜(20020076)
     * @param jsonObject 请求对象
     *  rankType:榜单类型; 0-明星排行榜; 1-富豪排行榜; 2-直播铁人榜; 3-每周礼物榜   
     *  slotType: 榜单周期: 0-日榜; 1-周榜; 2-月榜; 3-总榜  
     *  conut: 返回记录条数; 最多缓存20条  
     * @return 结果字符串
     */
    public JsonObject getRankingList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int rankType,slotType, count;
        try{
            rankType = CommonUtil.getJsonParamInt(jsonObject, "rankType", 8, TagCodeEnum.APPID_MISSING, 0, 20);
            slotType = CommonUtil.getJsonParamInt(jsonObject, "slotType", 8, TagCodeEnum.APPID_MISSING, 0, 20);
            count = CommonUtil.getJsonParamInt(jsonObject, "count", 8, null, 1, 20);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(TAG_CODE, e.getErrCode());
            return result;
        }
        
        String rankTypeKey = rankingListSource.getRankingTypeKey(rankType, slotType);
        if (rankTypeKey == null) {
            //当前榜单类型不支持
            result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
        } else {
            Set<String> rankingSet = null;
            try {
                rankingSet = rankingListSource.getRankingList(rankTypeKey);
            } catch (RedisException e) {
                result.addProperty(TAG_CODE, TagCodeEnum.IRREGULAR_RESULT);
                return result;
            }
            
            JsonParser jsonParser = new JsonParser();
            JsonArray jsonArray = new JsonArray();
            for (String string : rankingSet) {
                JsonElement element = jsonParser.parse(string);
                jsonArray.add(element.getAsJsonObject());
                if(jsonArray.size() == count){
                	break;
                }
            }
            result.add("rankList", jsonArray);
            result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
        }
        
        return result;
    }
    
    /**
     * 获取直播平台礼物排行榜(20020077)
     * @param jsonObject 请求对象
     *  slotType: 榜单周期: 0-本周; 1-上周榜;  
     * @return 结果字符串
     */
    public JsonObject getGiftRankingList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request){
        JsonObject result = new JsonObject();
        
        int slotType;
        try {
            slotType = CommonUtil.getJsonParamInt(jsonObject, "slotType", 8, TagCodeEnum.APPID_MISSING, 0, 20);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(TAG_CODE, e.getErrCode());
            return result;
        }
        
        String rankTypeKey = rankingListSource.getRankingTypeKey(GIFT_RANKING_TYPE, slotType);
        if (rankTypeKey == null) {
            //当前榜单类型不支持
            result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
        } else {
            Set<String> rankingSet = null;
            try {
                rankingSet = rankingListSource.getRankingList(rankTypeKey);
            } catch (RedisException e) {
                result.addProperty(TAG_CODE, TagCodeEnum.IRREGULAR_RESULT);
                return result;
            }
            JsonParser jsonParser = new JsonParser();
            JsonArray jsonArray = new JsonArray();
            for (String string : rankingSet) {
                JsonElement element = jsonParser.parse(string);
                jsonArray.add(element.getAsJsonObject());
            }
            result.add("rankList", jsonArray);
            result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
        }
        
        return result;
    }
    
    //
    /**
     * 获取直播平台礼物排行榜(20020078)
     * @param jsonObject 请求对象
     *  giftId: 家族礼物榜对应的礼物id  
     * @return 结果字符串
     */
    public JsonObject getFamilyRankingList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request){
        JsonObject result = new JsonObject();
        
        String giftId;
        String date;
        try {
            giftId = CommonUtil.getJsonParamString(jsonObject, "giftId", null, TagCodeEnum.APPID_MISSING, 0, 20);
            date = CommonUtil.getJsonParamString(jsonObject, "date", null, null, 0, 20);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(TAG_CODE, e.getErrCode());
            return result;
        }
        
        Set<String> rankingSet = null;
        try {
            rankingSet = rankingListSource.getFamilyRankingList(giftId, date);
        } catch (RedisException e) {
            result.addProperty(TAG_CODE, TagCodeEnum.IRREGULAR_RESULT);
            return result;
        }
        JsonParser jsonParser = new JsonParser();
        JsonArray jsonArray = new JsonArray();
        for (String string : rankingSet) {
            JsonElement element = jsonParser.parse(string);
            jsonArray.add(element.getAsJsonObject());
        }
        result.add("rankList", jsonArray);
        result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
        
        return result;
    }
    
}

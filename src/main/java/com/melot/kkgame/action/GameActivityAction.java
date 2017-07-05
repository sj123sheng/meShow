/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.action;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.kktv.redis.GameRankingSource;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.TagCodeEnum;

/**
 * Title: GameActivityAction
 * <p>
 * Description: 游戏七夕节模块信息
 * </p>
 * @author  姚国平<a href="mailto:guoping.yao@melot.cn">
 * @version V1.0
 * @since 2015-8-14 下午2:03:09 
 */
public class GameActivityAction {

    /**
     *  返回游戏家族排行榜 
     *  funcTag: 11003004
     */
    public JsonObject getFamilyRankingList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        String giftId;
        String date;
        try{
            giftId = CommonUtil.getJsonParamString(jsonObject, "giftId", null, TagCodeEnum.APPID_MISSING, 0, 20);
            date = CommonUtil.getJsonParamString(jsonObject, "date", null, null, 0, 20);
        }catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }
        
        JsonArray jsonArray = new JsonArray();
        Set<String> redisResult = GameRankingSource.getFamilyRankingList(giftId, date);
        if(redisResult != null && redisResult.size() > 0){
            JsonParser jsonParser = new JsonParser();
            for (String string : redisResult) {
                JsonElement element = jsonParser.parse(string);
                jsonArray.add(element.getAsJsonObject());
            }
        }
        
        result.add("rankList", jsonArray);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        
        return result;
    }
    
    /**
     *  返回玩家兑换信息 
     *  funcTag: 11003005
     */
    public JsonObject getActivetyExchangeInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        String giftId;
        int userId = 0;
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        try{
            giftId = CommonUtil.getJsonParamString(jsonObject, "giftId", null, TagCodeEnum.APPID_MISSING, 0, 20);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        }catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }
        JsonArray array = new JsonArray();
        String point = GameRankingSource.getExchangePoint(giftId, userId);
        if(point != null ){
            result.addProperty("point", Integer.valueOf(point));
            Map<String, String> exchangeInfo = GameRankingSource.getExchangeInfo(giftId, userId);
            if(exchangeInfo != null && !exchangeInfo.isEmpty()){
                JsonObject json = null;
                for (Map.Entry<String, String> entry : exchangeInfo.entrySet()) {  
                    json = new JsonObject();
                    json.addProperty("code", entry.getKey());
                    json.addProperty("time", entry.getValue());
                    array.add(json);
                }
            }
        }else{
            result.addProperty("point", 0);
        }
        result.add("exchange", array);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    
    /**
     *  玩家进行兑换 
     *  funcTag: 11003006
     */
    public JsonObject exchangePoint(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        int userId = 0;
        int type = 0;
        String giftId = "40000568";
        String telephone = null; //收货人电话
        String receiveAddress = null; //收货地址
        String receiveName = null; //收货人姓名
        String receiveMemo = null; //收货备注. 用于不同类型兑换的信息记录 
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        try{
            type = CommonUtil.getJsonParamInt(jsonObject, "type", 0, TagCodeEnum.APPID_MISSING, 0, 20);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            telephone = CommonUtil.getJsonParamString(jsonObject, "telephone", null, null, 0, 20);
            receiveAddress =  CommonUtil.getJsonParamString(jsonObject, "receiveAddress", null, null, 0, Integer.MAX_VALUE);
            receiveName =  CommonUtil.getJsonParamString(jsonObject, "receiveName", null, null, 0, Integer.MAX_VALUE);
            receiveMemo =  CommonUtil.getJsonParamString(jsonObject, "receiveMemo", null, null, 0, Integer.MAX_VALUE);
        }catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }
        
        int usedPoint = getExchangePoint(type);
        String currentPoint = GameRankingSource.getExchangePoint(giftId, userId);
        if(usedPoint < 1){
            //当前用户没有兑换积分或者积分不够用于兑换
            result.addProperty("TagCode", TagCodeEnum.FAILED_TO_DELETE);
            return result;
        }
        
        if(currentPoint == null || usedPoint > Integer.valueOf(currentPoint)){
            //当前用户没有兑换积分或者积分不够用于兑换
            result.addProperty("TagCode", TagCodeEnum.PERMISSION_DENIED);
            return result;
        }
        GameRankingSource.minusExchangePoint(giftId, userId, usedPoint); //扣除玩家积分
        GameRankingSource.addExchangeHist(giftId, userId, usedPoint);
        JsonObject message = new JsonObject();
        message.addProperty("userId", userId);
        message.addProperty("usedPoint", usedPoint);
        message.addProperty("telephone", telephone);
        message.addProperty("receiveAddress", receiveAddress);
        message.addProperty("receiveName", receiveName);
        message.addProperty("receiveMemo", receiveMemo);
        message.addProperty("exchangeTime", new Date().getTime());
        GameRankingSource.addExchangeLog(type, message.toString());
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    
    /**
     *  管理员查询兑换记录
     *  funcTag: 11003007
     */
    public JsonObject getExchangePointResult(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        int type = 0;
        try{
            type = CommonUtil.getJsonParamInt(jsonObject, "type", 0, TagCodeEnum.APPID_MISSING, 0, 20);
        }catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }
        JsonArray array = new JsonArray();
        Set<String>messages = GameRankingSource.getAllExchangeLog(type);
        if(messages != null && messages.size() > 0){
            JsonParser jsonParser = new JsonParser();
            for (String message : messages) {
                JsonElement element = jsonParser.parse(message);
                array.add(element.getAsJsonObject());
            }
        }
        result.add("exchange", array);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    
    private static Integer getExchangePoint(int type){
        switch(type){
            case 1: return 666;
            case 2: return 1314;
            case 3: return 1888;
            case 4: return 3344;
            case 5: return 9999;
            case 6: return 13140;
            default: return 0;
        }
    }
    
    
}

/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.redis;

import java.text.DecimalFormat;

import com.google.gson.JsonObject;
import com.melot.kkgame.redis.support.RedisException;
import com.melot.kkgame.redis.support.RedisTemplate;

/**
 * Title: MessageSource
 * <p>
 * Description: 房间消息通道
 * </p>
 * @author  姚国平<a href="mailto:guoping.yao@melot.cn">
 * @version V1.0
 * @since 2015-8-18 下午4:40:34 
 */
public class RoomMessageSource  extends RedisTemplate{

    
    private final String OUTER_MESSAGE_RECORD_LIST = "outerMessageRecordList";
    
    /**
     *  用户竞猜消息 
     * 
     */
    private final String GAMBLE_MSG_FORMAT = "<span style='color:red;'>【系统消息】</span>：<span style='color:#ff7203;'>%s</span>在竞猜中投放<span style='color:#ff7203;'>%s</span>秀币至%s选项";
    private final DecimalFormat CURRENCY_FORMAT =  new DecimalFormat("#,###");
    
    @Override
    public String getSourceName() {
        return "RoomMessageSource";
    }
    
    
    public String getGambleActionMsg(String nickname,String optionName, Integer amount){
        return String.format(GAMBLE_MSG_FORMAT, nickname,CURRENCY_FORMAT.format(amount.longValue()),optionName);
    }
    
    /***
     * 推送投注信息到相关房间
     * @throws RedisException 
     * 
     */
    public void sendMsgToUserInRoom(int roomId, String message) throws RedisException{
        JsonObject json = new JsonObject();
        json.addProperty("type", "2");
        json.addProperty("roomId", String.valueOf(roomId));
        
        JsonObject msg = new JsonObject();
        msg.addProperty("MsgTag", 30000001);
        msg.addProperty("content", message);
        json.addProperty("msg", msg.toString());
        rpush(OUTER_MESSAGE_RECORD_LIST, json.toString());
    } 
    
}

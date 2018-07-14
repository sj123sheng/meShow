/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.redis.external;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.melot.kkgame.redis.support.RedisException;
import com.melot.kkgame.redis.support.RedisTemplate;
import org.apache.log4j.Logger;

/**
 * Title: SmsSource
 * <p>
 * Description: 礼物配置信息
 * </p>
 * @author  姚国平<a href="mailto:guoping.yao@melot.cn">
 * @version V1.0
 * @since 2015-7-3 下午6:40:27 
 */
public class GiftSource extends RedisTemplate{

    private Logger logger = Logger.getLogger(GiftSource.class);
    
    private Map<Integer,GiftInfo>giftConfigs = new HashMap<Integer, GiftInfo>();
    
    private final String GIFT_LIST = "ti_gift_info";

    private final String MYSQL_LIST_NAME = "gh_mysql";
    private final String MONGO_LIST_NAME = "gh_mongo";
    private final String REDIS_LIST_NAME = "gh_redis";
    private final String MSG_ORACLE = "msg_oracle";
    
    @Override
    public String getSourceName() {
        return "GiftInfo";
    }
    
    public Set<String> getGiftList() throws RedisException{
        return zrange(GIFT_LIST , 0, -1);
    }
    
    /**
     *  将一条送礼记录赠送到redis数据库 
     */
    public void addSendGiftInfo(String sendMsg) throws RedisException{
        rpush(MYSQL_LIST_NAME, sendMsg);
        rpush(MONGO_LIST_NAME, sendMsg);
        rpush(REDIS_LIST_NAME, sendMsg);
        rpush(MSG_ORACLE, sendMsg);
        
    }
    
    public GiftInfo getGiftInfo(Integer giftId){
        GiftInfo giftInfo = giftConfigs.get(giftId);
        if(giftInfo == null || giftConfigs.isEmpty()){
            reloadGiftConfigs();
            return  giftConfigs.get(giftId);
        }
        return giftInfo;
    }
    
    private void reloadGiftConfigs(){
        try {
            JsonParser jsonParser = new JsonParser();
            Set<String>configs =  getGiftList();
            for (String string : configs) {
                JsonElement element = jsonParser.parse(string);
                GiftInfo giftInfo = GiftInfo.createGiftInfo(element.getAsJsonObject());
                giftConfigs.put(giftInfo.getGiftId(), giftInfo);
            }
        } catch (RedisException e) {
            logger.error("GiftSource.reloadGiftConfigs() execute exception.", e);
        }
    }
    
}

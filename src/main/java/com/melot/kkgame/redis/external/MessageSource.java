/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.redis.external;

import redis.clients.jedis.Jedis;

import com.melot.kkgame.redis.support.RedisCallback;
import com.melot.kkgame.redis.support.RedisException;
import com.melot.kkgame.redis.support.RedisTemplate;


/**
 * Title: SmsSource
 * <p>
 * Description: 短信redis设置
 * </p>
 * @author  姚国平<a href="mailto:guoping.yao@melot.cn">
 * @version V1.0
 * @since 2015-7-3 下午6:40:27 
 */
public class MessageSource extends RedisTemplate{

    private final String TI_MESSAGE_LIST = "ti_message_list";
    
    private final String TI_MESSAGE_VERSION = "ti_message_version";
    
    private final String TI_MESSAGE_BLACK_LIST = "black_";
    
    
    @Override
    public String getSourceName() {
        return "Messaga";
    }
    
    /**
     *  将消息插入到redis队列 
     */
    public void addBarrage(String barrage) throws RedisException{
       rpush(TI_MESSAGE_LIST, barrage);
    }
    
    public String getLastVersion() throws RedisException{
       return get(TI_MESSAGE_VERSION);
    }
    
    /**
     *  检测用户是否是在黑名单 
     * 
     */
    public boolean isForbidden(String userId) throws RedisException{
       final String key = TI_MESSAGE_BLACK_LIST + userId;
           return execute(new RedisCallback<Boolean>() {
               @Override
               public Boolean doInRedisClient(Jedis jedis) throws RedisException {
                   Long remainTime = jedis.ttl(key);
                   if(remainTime == -2 || remainTime == -1) {
                       jedis.del(key);
                       return false;
                   }else{
                       return true;
                   }
               }  
           });
    }
    
    /**
     *  添加用户到黑名单 
     * 
     */
    public Long addUserToBlackList(final String userId ,final int seconds) throws RedisException{
       return execute(new RedisCallback<Long>() {
            public Long doInRedisClient(Jedis jedis)throws RedisException{
               jedis.set(TI_MESSAGE_BLACK_LIST + userId, userId);
               return jedis.expire(TI_MESSAGE_BLACK_LIST + userId, seconds);
            }
        });
    }
    
}

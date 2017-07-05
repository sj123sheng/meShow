/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.redis.external;

import java.util.Set;

import com.melot.kkgame.redis.support.RedisCallback;
import com.melot.kkgame.redis.support.RedisException;
import com.melot.kkgame.redis.support.RedisTemplate;
import redis.clients.jedis.Jedis;

/**
 * Title: SmsSource
 * <p>
 * Description: 礼物配置信息
 * </p>
 * @author  姚国平<a href="mailto:guoping.yao@melot.cn">
 * @version V1.0
 * @since 2015-7-3 下午6:40:27 
 */
public class ConfigSource extends RedisTemplate{
    
    
    private static final String CONFIG_PREFIX = "config_";
    
    private static final String CONFIG_FLV_HASH = "config_flv_hash";
    
    @Override
    public String getSourceName() {
        return "RoomConfig";
    }
    
    /**
     *  查询房间配置的全部播放流地址 
     */
    public Set<String> getFlvList(final String roomId) throws RedisException{
        return execute(new RedisCallback<Set<String>>() {
            @Override
            public Set<String> doInRedisClient(Jedis jedis) throws RedisException {
                return jedis.smembers(CONFIG_PREFIX + roomId);
            }
        }); 
    }
    
    public Set<String> getFlvList(final int roomId) throws RedisException{
        return getFlvList(String.valueOf(roomId));
    }
    
    /**
     *  根据流地址查询对应的房间号 
     * 
     */
    public String queryRoomId(String address) throws RedisException{
        return hget(CONFIG_FLV_HASH, address);
    }
    
    /**
     *  根据roomId返回当前房间映射的推流id,应对某些房间未开播下需要额外的推流
     */
    public String fetchMappingLiveId(Integer roomId)throws RedisException{
        final String key = "live.stream.mapping";
        return hget(key, Integer.toString(roomId));
    }
    
    /**
     *  检测是否是从移动端推流 
     */
    public boolean isLiveFromMobile(Integer roomId)throws RedisException{
        final String key = "game.live.mode.mobile";
        return hget(key,Integer.toString(roomId)) != null ;
    }
    
}

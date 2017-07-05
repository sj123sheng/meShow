/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.redis;

import redis.clients.jedis.Jedis;

import com.melot.kkgame.redis.support.RedisCallback;
import com.melot.kkgame.redis.support.RedisException;
import com.melot.kkgame.redis.support.RedisTemplate;

/**
 * Title: ActorInfoSource
 * <p>
 * Description: 主播信息缓存
 * </p>
 * @author  姚国平<a href="mailto:guoping.yao@melot.cn">
 * @version V1.0
 * @since 2015-7-29 下午2:37:33 
 */
public class ActorInfoSource extends RedisTemplate{

    private static final String LOUDER_SPEAKER_PREFIX = "loudspeaker_";
    
    @Override
    public String getSourceName() {
        return "ActorInfoSource";
    }

    /**
     *  累加发送次数 
     * @throws RedisException 
     */
    public void increaseLoudspeakerTime(int roomId) throws RedisException{
        final String key = LOUDER_SPEAKER_PREFIX + roomId;
        execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedisClient(Jedis jedis) throws RedisException {
                Long remainTime = jedis.ttl(key);
                if(remainTime == -2 || remainTime == -1) {
                    jedis.del(key);
                }
                long time = jedis.incr(key);
                jedis.expireAt(key, getNextDayZeroTime().getTime()/1000);
                return time;
            }
        });
    }
    
    public boolean hasPublishToday(int roomId) throws RedisException{
        return getLoudspeakerTime(roomId) == 0l ? false : true;
    }
    
    /**
     *  返回主播用户今天发送的喇叭次数 
     * @throws RedisException 
     */
    public long getLoudspeakerTime(int roomId) throws RedisException{
        final String key = LOUDER_SPEAKER_PREFIX + roomId;
        return execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedisClient(Jedis jedis) throws RedisException {
                Long remainTime = jedis.ttl(key);
                if(remainTime == -2 || remainTime == -1) {
                    return 0l;
                }else{
                    return Long.valueOf(get(key));
                }
            }
        });
    }
    
}

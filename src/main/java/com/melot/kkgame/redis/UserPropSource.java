/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import redis.clients.jedis.Jedis;

import com.melot.kkgame.redis.support.RedisCallback;
import com.melot.kkgame.redis.support.RedisException;
import com.melot.kkgame.redis.support.RedisTemplate;

/**
 * Title: UserPropSource
 * <p>
 * Description: 用户会员相关redis
 * </p>
 * @author  姚国平<a href="mailto:guoping.yao@melot.cn">
 * @version V1.0
 * @since 2015-7-24 下午5:35:20 
 */
public class UserPropSource extends RedisTemplate{

    
 // key格式 userProp_userid_propid value propid
    private static final String USER_PROP_KEY_FORMAT = "userProp_%s_%s";
    // key格式 userPropUser_userid value propid
    private static final String USER_PROP_USER_KEY_FORMAT = "userPropUser_%s";

    
    @Override
    public String getSourceName() {
        return "UserProp";
    }
    
    public  List<Integer> getUserProp(final String userId) throws RedisException {
        final List<Integer> propList = new ArrayList<Integer>();
        if (userId == null || "".equals(userId.trim())) {
            return propList;
        }
        
        final String pattern = String.format(USER_PROP_USER_KEY_FORMAT, userId);
        execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedisClient(Jedis jedis) throws RedisException {
                Set<String> keys = jedis.smembers(pattern);
                if (keys != null && !keys.isEmpty()) {
                    for (String key : keys) {
                        if (jedis.exists(String.format(USER_PROP_KEY_FORMAT, userId, key))) {
                            propList.add(Integer.parseInt(key));
                        } else {
                            jedis.srem(pattern, key);
                        }
                    }
                }
                return null;
            }
        });
        
        return propList;
    }

}

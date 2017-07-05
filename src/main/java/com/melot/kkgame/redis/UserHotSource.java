package com.melot.kkgame.redis;

import java.util.Map;

import org.apache.log4j.Logger;

import com.melot.kkgame.redis.support.RedisCallback;
import com.melot.kkgame.redis.support.RedisException;
import com.melot.kkgame.redis.support.RedisTemplate;
import com.melot.kktv.util.ConfigHelper;

import redis.clients.jedis.Jedis;

/**
 * 用户热点数据源
 * @author Administrator
 *
 */
public class UserHotSource extends RedisTemplate{
	
    private static Logger logger = Logger.getLogger(UserHotSource.class);
    
    @Override
    public String getSourceName() {
        return "UserHot";
    }
	
	public Map<String, String> getHotData(String key)throws RedisException {
	    return hgetAll(key);
	}
	
	public  String getHotFieldValue(String key, String field)throws RedisException {
		return hget(key, field);
	}

	public void setHotFieldValue(final String key, final String field, final String value, final int seconds)throws RedisException {
	    execute(new RedisCallback<Object>() {
            public Object doInRedisClient(Jedis jedis) throws RedisException {
                jedis.hset(key, field, value);
                jedis.expire(key, seconds);
                return null;
            }        
        });
	}
	
	public void setHotFieldValue(String key,String field,String value)throws RedisException {
	    hset(key, field, value);
    }
	
	public void setHotData(final String key, final Map<String,String> hotData, final int expireTime)throws RedisException {
		execute(new RedisCallback<Object>() {
            public Object doInRedisClient(Jedis jedis) throws RedisException {
                jedis.hmset(key, hotData);
                jedis.expire(key, expireTime);
                return null;
            }        
        });
	}

	public void delHotFieldValue(String key, String field)throws RedisException {
	    hdel(key, field);
	}
	
	public long incHotFieldValue(String key, String field, int incValue)throws RedisException {
	    return hincrBy(key, field, incValue);
	}
	
	/**
     * 更新开播用户登录Token信息
     * @param userId
     * @param token
     */
	public void updateUserToken(int userId, String token) {
        try {
            setHotFieldValue(String.valueOf(userId), "token", token, ConfigHelper.getRedisUserDataExpireTime());
        } catch (RedisException e) {
            logger.error("updateUserToken", e);
        }
    }
	
	
	/**
	 * 检测用户token是否是有效的token; 
	 */
	public boolean checkToken(int userId, String token) {
	    String getToken = null;
        if (userId > 0 && token != null) {
            try {
                getToken = getHotFieldValue(String.valueOf(userId), "token");
            } catch (RedisException e) {
                return false;
            }
        }
        return getToken != null && getToken.equals(token);
     }
	    
    /**
     * 用户登出
     * @param userId
     */
    public  void logout(int userId) {
        // 清除热点用户token
        try {
            delHotFieldValue(String.valueOf(userId), "token");
        } catch (RedisException e) {
            logger.error("用户登出失败", e);
        }
    }
    
    
}

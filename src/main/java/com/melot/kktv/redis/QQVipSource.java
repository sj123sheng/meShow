package com.melot.kktv.redis;

import com.melot.kktv.util.redis.RedisConfigHelper;

import redis.clients.jedis.Jedis;

public class QQVipSource {
	
	private static final String SOURCE_NAME = "QQVip";
	// key格式 qqVip_userid
	private static final String QQ_VIP_KEY_FORMAT = "qqVip_%s";
		
	private static Jedis getInstance() {
		return RedisConfigHelper.getJedis(SOURCE_NAME);
	}
	
	private static void freeInstance(Jedis jedis) {
		RedisConfigHelper.returnJedis(SOURCE_NAME, jedis, false);
	}

	public static boolean setQQVipExpireTime(String userId, Long expireTime) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			jedis.set(QQ_VIP_KEY_FORMAT+userId, String.valueOf(expireTime.longValue()));
			jedis.expireAt(QQ_VIP_KEY_FORMAT+userId, expireTime);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return false;
	}
	
	public static Long getQQVipExpireTime(String userId) {
		Jedis jedis = null;
		Long expireTime = null;
		try {
			jedis = getInstance();
			if(jedis.exists(QQ_VIP_KEY_FORMAT+userId) 
					&& jedis.get(QQ_VIP_KEY_FORMAT+userId)!=null) {
				expireTime = Long.valueOf(jedis.get(QQ_VIP_KEY_FORMAT+userId));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return expireTime;
	}
	
	public static void delQQVipExpireTime(String userId) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			jedis.del(QQ_VIP_KEY_FORMAT+userId);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
	}
	
}

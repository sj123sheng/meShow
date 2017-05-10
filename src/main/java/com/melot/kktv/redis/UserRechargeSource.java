package com.melot.kktv.redis;

import com.melot.kktv.util.redis.RedisConfigHelper;

import redis.clients.jedis.Jedis;

public class UserRechargeSource {
	
	private static final String SOURCE_NAME = "UserRecharge";
	
	private static final String QUEUE_SUCCESS_ORDER = "success_order";
	
	private static Jedis getInstance() {
		return RedisConfigHelper.getJedis(SOURCE_NAME);
	}
	
	private static void freeInstance(Jedis jedis) {
		RedisConfigHelper.returnJedis(SOURCE_NAME, jedis, false);
	}
	
	public static void addSuccessOrder(String string) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			jedis.rpush(QUEUE_SUCCESS_ORDER, string);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
	}
	
}

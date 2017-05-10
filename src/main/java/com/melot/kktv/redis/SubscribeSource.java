package com.melot.kktv.redis;

import com.melot.kktv.util.redis.RedisConfigHelper;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

public class SubscribeSource {
	
	private static final String SOURCE_NAME = "Subscribe";
	
	// 节目订阅记录(actSub_actId)
	private static final String ACTSUB_KEY = "actSub_";
	
	private static Jedis getInstance() {
		return RedisConfigHelper.getJedis(SOURCE_NAME);
	}
	
	private static void freeInstance(Jedis jedis) {
		RedisConfigHelper.returnJedis(SOURCE_NAME, jedis, false);
	}
	
	/**
	 * 订阅预告节目
	 * @param userId 用户编号
	 * @param actId 节目编号
	 * @param startTime 节目开始时间
	 * @param endTime 节目结束时间
	 */
	public static void subPreviewAct(String userId, String actId, String actRoom, Long endTime, boolean ifWeekly) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			Transaction trans = jedis.multi();
			trans.hset(ACTSUB_KEY+actId, userId, actRoom);
			// 判断是否周期性活动
			if (!ifWeekly && endTime!=null) {
				trans.expireAt(ACTSUB_KEY+actId, endTime.longValue()/1000); // 秒级
			}
			trans.exec();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
	}
	
	/**
	 * 取消订阅预告节目
	 * @param userId 用户编号
	 * @param actId 节目编号
	 */
	public static void unsubPreviewAct(String userId, String actId) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			jedis.hdel(ACTSUB_KEY+actId, userId);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
	}
	
	/**
	 * 检查是否订阅
	 * @param userId 用户编号
	 * @param actId 节目编号
	 */
	public static boolean checkActSub(String userId, String actId) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			String value = jedis.hget(ACTSUB_KEY+actId, userId);
			if (value!=null) return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return false;
	}
	
}

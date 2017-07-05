package com.melot.kktv.redis;

import com.melot.kktv.util.redis.RedisConfigHelper;

import redis.clients.jedis.Jedis;

public class AppStatsSource {
	
	private static final String SOURCE_NAME = "AppStats";
	
	private static final String QUEUE_INSTALL_PACK = "install_pack";
	private static final String QUEUE_MOBILE_DEVICE = "mobile_device";
	
	// promote_{ipaddr+useragent}
	private static final String PROMOTE_CHANNEL_KEY = "promote_chann_%s";
	
	private static Jedis getInstance() {
		return RedisConfigHelper.getJedis(SOURCE_NAME);
	}
	
	private static void freeInstance(Jedis jedis) {
		RedisConfigHelper.returnJedis(SOURCE_NAME, jedis, false);
	}
	
	public static void addInstallPack(String string) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			jedis.rpush(QUEUE_INSTALL_PACK, string);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
	}
	
	public static void addMobileDevice(String string) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			jedis.rpush(QUEUE_MOBILE_DEVICE, string);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
	}
	
	/**
	 * 存储App渠道推广信息
	 * @param name Md5(appId+ipAddr+userAgent)
	 * @param channel
	 */
	public static void setAppPromoteInfo(String name, String channel, int seconds) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			String key = String.format(PROMOTE_CHANNEL_KEY, name);
			jedis.lpush(key, channel);
			jedis.expire(key, seconds);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
	}
	
	/**
	 * 获取App渠道推广信息
	 * @param name Md5(appId+ipAddr+userAgent)
	 * @return channel
	 */
	public static String getAppPromoteInfo(String name) {
		Jedis jedis = null;
		try {  
			jedis = getInstance();
			String key = String.format(PROMOTE_CHANNEL_KEY, name);
			return jedis.rpop(key);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);    
			}
		}
		return null;
	}
	
}

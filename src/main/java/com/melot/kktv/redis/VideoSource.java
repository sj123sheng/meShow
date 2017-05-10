package com.melot.kktv.redis;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.melot.kktv.util.redis.RedisConfigHelper;

import redis.clients.jedis.Jedis;

public class VideoSource {
	
	private static Logger logger = Logger.getLogger(VideoSource.class);
	
	private static final String SOURCE_NAME = "TempVideo";
	
	private static Jedis getInstance() {
		return RedisConfigHelper.getJedis(SOURCE_NAME);
	}
	
	private static void freeInstance(Jedis jedis) {
		RedisConfigHelper.returnJedis(SOURCE_NAME, jedis, false);
	}

	/**
	 * 查看是否存在视频缓存
	 * @param key
	 * @return
	 */
	public static boolean hasTempVideoData(String key) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			return jedis.exists(key);
		} catch (Exception e) {
			logger.error("VideoSource.hasTempVideoData error, key :" + key, e);
		} finally {
			if (jedis!=null) {
				freeInstance(jedis);
			}
		}
		return false;
	}
	
	/**
	 * 得到视频缓存
	 * @param key
	 * @param field
	 * @return
	 */
	public static String getTempVideoValue(String key, String field) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			return jedis.hget(key, field);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return null;
	}
	
	public static Map<String, String> getHotData(String key) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			Map<String, String> data = new HashMap<String, String>();
			Set<String> fieldList = jedis.hkeys(key);
			for (Iterator<String> iter = fieldList.iterator(); iter.hasNext();) {
				String field = iter.next();
				String value = jedis.hget(key, field);
				data.put(field, value);
			}
			return data;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return null;
	}
	
	public static void setHotFieldValue(String key, String field, String value, int seconds) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			jedis.hset(key, field, value);
			if (jedis.ttl(key) == -1) {
				jedis.expire(key, seconds);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
	}

}

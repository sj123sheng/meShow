package com.melot.kkcx.redis;

import java.util.Map;

import redis.clients.jedis.Jedis;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.melot.common.melot_jedis.JedisWrapper;
import com.melot.common.melot_jedis.RedisDataSourceFactory;
import com.melot.kktv.util.CollectionUtils;

public class PushMsgSource {
	
	public static final String pushMsgSourceBean = "pushMsgSource";
	
	public static final String sourceName = "push_source_list";

	/**
	 * 获取redis资源
	 * @return
	 */
	private static JedisWrapper getInstance() {
    	return new JedisWrapper(RedisDataSourceFactory.getGlobalInstance().getJedisPool(sourceName), sourceName);
	}

	public static void lpush(Map<String, Object> map) {
		if (CollectionUtils.isEmpty(map)) {
			return;
		}
		String key = "push_source_list";
		boolean errHappend = false;
		Throwable t = null;
		Jedis jedis = null;
		JedisWrapper jedisWrapper = null;
		try {
			jedisWrapper = getInstance();
			jedis = jedisWrapper.getJedis();
			String json = new Gson().toJson(map);
			jedis.lpush(key, json);
		} catch (Throwable e) {
			 errHappend = true;
			 t = e;
			 throw new JsonParseException(t);
		}finally{
			if (jedisWrapper != null && jedis != null) {
                if(errHappend){
                	jedisWrapper.returnBrokenJedis(jedis, t);
                }else{
                	jedisWrapper.returnJedis(jedis);
                }
            }
		}
	}
}

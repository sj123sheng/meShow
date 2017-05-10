package com.melot.kkgame.redis.support;

import redis.clients.jedis.Jedis;


public interface RedisCallback <T>{

	
	T doInRedisClient(Jedis jedis) throws RedisException;
	
}

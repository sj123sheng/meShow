package com.melot.kktv.redis;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisPoolAgent {
	private static Logger logger = Logger.getLogger(JedisPoolAgent.class);
	
	private HashMap<String, JedisPool> jedisClients = new HashMap<String, JedisPool>();
	
	public JedisPoolAgent(){
	}
	
	public void freeInstance(){
		//close every jedisClient(jedisPool), so, all connection will be released
		Iterator<Entry<String, JedisPool>> iter = jedisClients.entrySet().iterator(); 
		while (iter.hasNext()) {
			Entry<String, JedisPool> entry =  iter.next();
			JedisPool jedisClient = entry.getValue();
			if(jedisClient != null)
				jedisClient.destroy();
		}
	}
	
	//PAY attention: it is not thread safe.
	public JedisPool getJedisPool(final JedisPoolConfig poolConfig,
            final String host, int port, int timeout, final String password){
		if(poolConfig == null || host == null || port == 0)
			return null;
		try {
			String serverAddr = host + port + password;//as the key 
			JedisPool jedisClient = jedisClients.get(serverAddr);
			if(jedisClient == null){
				//not yet created, create it now
				jedisClient = new JedisPool(poolConfig, host, port, timeout, password);
				
				//save the created instance of jedisClient
				jedisClients.put(serverAddr, jedisClient);
			}
			return jedisClient;
		}
		catch(Exception e){
			logger.error("Exception when get JedisPool instance: " + host + " : " + port, e);
		}
		return null;
	}
	
	//PAY attention: it is not thread safe.
	public JedisPool getJedisPool(final JedisPoolConfig poolConfig,
            final String host, int port, int timeout, final String password, int db){
		if(poolConfig == null || host == null || port == 0)
			return null;
		try {
			String serverAddr = host + port + password + db;//as the key 
			JedisPool jedisClient = jedisClients.get(serverAddr);
			if(jedisClient == null){
				//not yet created, create it now
				jedisClient = new JedisPool(poolConfig, host, port, timeout, password, db);
				
				//save the created instance of jedisClient
				jedisClients.put(serverAddr, jedisClient);
			}
			return jedisClient;
		}
		catch(Exception e){
			logger.error("Exception when get JedisPool instance: " + host + " : " + port, e);
		}
		return null;
	}
	
}

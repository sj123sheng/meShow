package com.melot.kkcx.service;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;

import com.melot.kktv.redis.UserMessageSource;

public class MessageBoxServices {
	
	/** 日志记录对象 */
	private static Logger logger = Logger.getLogger(MessageBoxServices.class);
	
	//根据key：msgTotalCount , field：useId  获取对应的hashes：value
		public static int getUserMessageValue(int userId) {
			int total = 0;
			Jedis jedis = null;
			try {
				jedis = UserMessageSource.getInstance();

				//fetch the total count
				String msgTotal = jedis.hget("msgTotalCount", String.valueOf(userId));
				if(msgTotal !=null )
					total = Integer.parseInt(msgTotal);//this value not including the activity count
				return total;
			} catch (Exception e) {
				logger.error("getUserMessageValue failed when operate redis", e);
			} finally {
				if(jedis!=null) {
					UserMessageSource.freeInstance(jedis);
				}
			}
			return 0;
		}
	
}

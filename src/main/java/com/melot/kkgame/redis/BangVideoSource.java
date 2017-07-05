package com.melot.kkgame.redis;

import org.apache.log4j.Logger;
import com.melot.kkgame.redis.support.RedisException;
import com.melot.kkgame.redis.support.RedisTemplate;

public class BangVideoSource extends RedisTemplate {
	
	public static final String MAX_VIEW_PREFIX = "Bang_Max_View_";
	private Logger logger = Logger.getLogger(BangVideoSource.class);
	
	@Override
	public String getSourceName() {
		return "BangVideoSource";
	}
	
	/**
	 * 获取人数
	 * @param videoId
	 * @return
	 * @throws RedisException
	 */
	public Integer getViewCount(int videoId) {
		
		Integer viewCount = null;
		try {
			if (get(MAX_VIEW_PREFIX + videoId) != null){
				viewCount = Integer.valueOf(get(MAX_VIEW_PREFIX + videoId));
			}
		} catch (RedisException e) {
			logger.error(e);
			return null;
		}
		return viewCount;
		
	}
	
	/**
	 * 设置人数
	 * @param videoId
	 * @return
	 * @throws RedisException
	 */
	public void setViewCount(int videoId, int viewCount) {
		
		try {
			set(MAX_VIEW_PREFIX + videoId, String.valueOf(viewCount));
		} catch (RedisException e) {
			logger.error(e);
		}
		
	}
	
	/**
	 * 设置过期时间
	 * 
	 * @param actorId
	 */
	public void setExpireTime(int videoId, int expireTime) {
		try {
			expire(MAX_VIEW_PREFIX + videoId, expireTime);
		} catch (Exception ex) {
			logger.error(ex);
		}
	}
	
	/**
	 * 获取过期时间
	 * 
	 * @param actorId
	 */
	public Long getExpireTime(int videoId) {
		try {
			return ttl(MAX_VIEW_PREFIX + videoId);
		} catch (Exception ex) {
			logger.error(ex);
			return 0l;
		}
	}
}

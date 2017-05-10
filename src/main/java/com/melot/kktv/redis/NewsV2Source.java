package com.melot.kktv.redis;

import java.util.Set;

import com.melot.kktv.util.redis.RedisConfigHelper;

import redis.clients.jedis.Jedis;

import org.apache.log4j.Logger;

public class NewsV2Source {
	
	private static Logger logger = Logger.getLogger(NewsV2Source.class);
	
	private static final String SOURCE_NAME = "NewsV2";
	
	/********************************* 热门动态           ********************************************/
	private static final String HOT_NEWS = "hot_news_cache";
	
	/** 推荐动态 **/
	private static final String RECOMMEND_NEWS = "recommend_news";
	
	private static final String HOT_TOPIC = "hot_topic";
	
	private static final String HOT_COMMENT = "hot_comment";
	
	private static final String RECOMMEND_COMMENT = "recommend_comment";
	
	private static Jedis getInstance() {
		return RedisConfigHelper.getJedis(SOURCE_NAME);
	}
	
	private static void freeInstance(Jedis jedis) {
		RedisConfigHelper.returnJedis(SOURCE_NAME, jedis, false);
	}
	
	
	/****************************   新版动态         ************************************/
	public static Set<String> getHotNews(int start, int offset) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			Set<String> hot = jedis.zrevrange(HOT_NEWS, start, start + offset);
			return hot;
		} catch (Exception e) {
			logger.error("NewsSource.getHotNews exception", e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
		return null;
	}
	
	public static Set<String> getHotTopic(int start, int offset) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			Set<String> hot = jedis.zrange(HOT_TOPIC, start, offset);
			return hot;
		} catch (Exception e) {
			logger.error("NewsSource.getHotTopic exception", e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
		return null;
	}
	
	public static Set<String> getHotComment() {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			Set<String> hot = jedis.zrevrange(HOT_COMMENT, 0, -1);
			return hot;
		} catch (Exception e) {
			logger.error("NewsSource.getRecommendNews exception", e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
		return null;
	}
	
	public static Set<String> getRecommendComment(int start, int count) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			return jedis.zrange(RECOMMEND_COMMENT, start, start + count);
		} catch (Exception e) {
			logger.error("NewsSource.getRecommendComment exception", e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
		return null;
	}
	
	public static Set<String> getRecommendNews() {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			Set<String> hot = jedis.zrevrange(RECOMMEND_NEWS, 0, -1);
			return hot;
		} catch (Exception e) {
			logger.error("NewsSource.getRecommendNews exception", e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
		return null;
	}
	
	public static void delHotRef(String newsId) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			jedis.zrem(HOT_NEWS, newsId);
			jedis.zrem(RECOMMEND_NEWS, newsId);
		} catch (Exception e) {
			logger.error("NewsSource.getRecommendNews exception", e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
	}
	
	private static final String NEWS_SELF = "news_self_%s";
	
	private static final String NEWS_ALL = "news_all_%s";
	
	public static void delNews(int userId, boolean isSelf) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			jedis.del(String.format(isSelf ? NEWS_SELF : NEWS_ALL, userId));
		} catch (Exception e) {
			logger.error("NewsSource.delNews exception", e);
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
	}
	
	public static void delHot(int newsId) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			jedis.zrem(HOT_NEWS, String.valueOf(newsId));
		} catch (Exception e) {
			logger.error("NewsSource.delNews exception", e);
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
	}

}

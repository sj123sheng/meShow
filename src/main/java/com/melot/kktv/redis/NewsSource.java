package com.melot.kktv.redis;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;

import com.google.gson.Gson;
import com.melot.kktv.util.redis.RedisConfigHelper;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.Tuple;

import org.apache.log4j.Logger;

public class NewsSource {
	
	private static Logger logger = Logger.getLogger(NewsSource.class);
	
	private static final String SOURCE_NAME = "News";
	
	/** 分页筛选(推荐)动态列表  **/
	private static final String REC_NEWS_LIST = "recNewsList";
	/** 新分页筛选(推荐)动态列表  **/
	private static final String NEW_REC_NEWS_LIST = "newRecNewsList";
	/** 推荐动态索引位置 **/
	private static final String TEMP_USER_NEWS_INDEX = "tempUserNews_%s";
	/** 热拍动态无标签分页列表 **/
	private static final String REPAI_PAGE_HOT_NEWS_NO_TAG = "rePaiPageHotNewsNoTag";
	/** 新热拍动态无标签分页列表 **/
	private static final String NEW_REPAI_PAGE_HOT_NEWS_NO_TAG = "newRePaiPageHotNewsNoTag";
	/** 热拍动态带标签分页列表 **/
	private static final String REPAI_HOT_NEWS_LIST_BY_TAG = "rePaiHotNewsListByTag_%s";
	/** 热拍动态带标签分页列表 **/
	private static final String NEW_REPAI_HOT_NEWS_LIST_BY_TAG = "newRePaiHotNewsListByTag_%s";
	/** 热门动态标签 **/
	private static final String NEWS_HOT_TAGS = "newsHotTags";
	/** 人工设置动态 **/
	private static final String MANUAL_NEWS_LIST = "manualNewsList";
	/** 用户发现提醒记录(定时失效) **/
	private static final String USER_REMIND_RECORD = "user_remind_%s";
	/** 用户上次读取发现时间记录  **/
	private static final String USER_REMIND_TIME_RECORD = "user_remind_time_%s";
	/** 用户删除的动态  **/
	private static final String USER_DELETED_NEWS = "userDeletedNews_%s";
	/** 筛选用户动态（热拍）key **/
	private static final String CURRENT_NEWS_KEY = "currentNewsKey";
	
	private static int EXPIRE_TIME = 7 * 24 * 60 * 60;
 	
	private static Jedis getInstance() {
		return RedisConfigHelper.getJedis(SOURCE_NAME);
	}
	
	private static void freeInstance(Jedis jedis) {
		RedisConfigHelper.returnJedis(SOURCE_NAME, jedis, false);
	}

	/**
	 * 判断redis发现提醒记录是否存在
	 * @param userId
	 * @return
	 */
	public static boolean isDiscoverRecordExist(String userId) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			return jedis.exists(String.format(USER_REMIND_RECORD, userId));
		} catch (Exception e) {
			logger.error("NewsSource.isDiscoverRecordExist exception, userId : " + userId, e);
		} finally{
			if(jedis != null) {
				freeInstance(jedis);
			}
		}
		return false;
	}
	
	/**
	 * 获得发现提醒记录
	 * @param userId
	 * @return
	 */
	public static Map<String, String> getDiscoverRecord(String userId) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			return jedis.hgetAll(String.format(USER_REMIND_RECORD, userId));
		} catch (Exception e) {
			logger.error("NewsSource.getDiscoverRecord exception, userId : " + userId, e);
		} finally {
			if(jedis != null) {
				freeInstance(jedis);
			}
		}
		return null;
	}
	
	/**
	 * 设置发现提醒记录
	 * @param userId
	 * @param record
	 * @return
	 */
	public static boolean setDiscoverRecord(String userId, Map<String, String> record) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			jedis.hmset(String.format(USER_REMIND_RECORD, userId), record);
			jedis.expire(String.format(USER_REMIND_RECORD, userId), 30);
			return true;
		} catch (Exception e) {
			logger.error("NewsSource.setDiscoverRecord exception, userId : " + userId, e);
		} finally {
			if(jedis != null) {
				freeInstance(jedis);
			}
		}
		return false;
	}
	
	/**
	 * 获取上次读取时间
	 * @param userId
	 * @return
	 */
	public static Map<String, String> getLastDiscoverRecord(String userId) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			Map<String, String> map = new HashMap<String, String>();
			if(jedis.exists(String.format(USER_REMIND_TIME_RECORD, userId))) {
				map.put("news", jedis.hget(String.format(USER_REMIND_TIME_RECORD, userId), "news"));
				map.put("match", jedis.hget(String.format(USER_REMIND_TIME_RECORD, userId), "match"));
				return map;
			}
		} catch (Exception e) {
			logger.error("NewsSource.getLastDiscoverRecord exception, userId : " + userId, e);
		} finally {
			if(jedis != null) {
				freeInstance(jedis);
			}
		}
		return null;
	}
	
	/**
	 * 设置动态提醒时间(供调用)
	 * @return
	 */
	public static boolean setNewsRemindTime(String userId, String newTime) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			
			Transaction trans = jedis.multi();
			trans.hset(String.format(USER_REMIND_TIME_RECORD, userId), "news", newTime);
			trans.hset(String.format(USER_REMIND_RECORD, userId), "newsCnt", "0");
			trans.expire(String.format(USER_REMIND_RECORD, userId), 30);
			trans.expire(String.format(USER_REMIND_TIME_RECORD, userId), EXPIRE_TIME);
			return (trans.exec() != null);
		} catch(Exception e) {
			logger.error("NewsSource.setNewsRemindTime exception, userId : " + userId
					 + " ,newTime : " + newTime, e);
		} finally {
			if(jedis != null) {
				freeInstance(jedis);
			}
		}
		return false;
	}

}

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
	 * 设置发现缓存   setFaXianNewsList
	 * @param newsList
	 */
	public static void setFilterNewsList(Map<String, String> newsList, int includeQiniu) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			if (includeQiniu > 0) {
				jedis.hmset(NEW_REC_NEWS_LIST, newsList);
				if (jedis.ttl(NEW_REC_NEWS_LIST) == -1) {
					jedis.expire(NEW_REC_NEWS_LIST, 30); // 30s过期
				}
			} else {
				jedis.hmset(REC_NEWS_LIST, newsList);
				if (jedis.ttl(REC_NEWS_LIST) == -1) {
					jedis.expire(REC_NEWS_LIST, 30); // 30s过期
				}
			}
		} catch (Exception e) {
			logger.error("NewsSource.setFilterNewsList exception, newsList : " + new Gson().toJson(newsList), e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
	}

	/**
	 * 获取发现列表  getFaXianNewsList
	 * @param field
	 * @return
	 */
	public static String getFilterNewsList(String field, int includeQiniu) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			if (includeQiniu > 0) {
				return jedis.hget(NEW_REC_NEWS_LIST, field);
			} else {
				return jedis.hget(REC_NEWS_LIST, field);
			}
		} catch (Exception e) {
			logger.error("NewsSource.getFilterNewsList exception, field : " + field, e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
		
		return null;
	}
	
	/**
	 * 获取筛选动态总数
	 * @return
	 */
	public static int getFilterNewsCount() {
		Jedis jedis = null;
		String key = getFilterNewsKey();
		if (key == null) return 0;
		try {
			jedis = getInstance();
			delSavedNewsIds(key);
			return jedis.zcard(key).intValue();
		} catch (Exception e) {
			logger.error("NewsSource.getTotalNewsFactor exception, key : " + key, e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
		
		return 0;
	}
	
	/**
	 * 设置用户动态列表 推荐动态排序索引位置
	 * @param userId 用户Id
	 * @param newsId 动态Id
	 * @param index 列表所在位置
	 */
	public static void setTempUserNewsKey(int userId, int newsId, int index) {
		Jedis jedis = null;
		String key = String.format(TEMP_USER_NEWS_INDEX, userId);
		try {
			jedis = getInstance();
			jedis.zadd(key, index, String.valueOf(newsId));
			if (jedis.ttl(key) == -1) {
				jedis.expire(key, 300);  // 以秒为单位
			}
		} catch (Exception e) {
			logger.error("NewsSource.setTempUserNewsKey exception, userId : " + userId 
					+ " ,newsId : " + newsId + " ,index : " + index, e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
	}
	
	/**
	 * 删除用户动态列表 推荐动态排序指定索引位置
	 * @param userId 用户Id
	 * @param 索引最小值
	 */
	public static void delTempUserNewsKey(int userId, int start) {
		Jedis jedis = null;
		String key = String.format(TEMP_USER_NEWS_INDEX, userId);
		try {
			jedis = getInstance();
			jedis.zremrangeByScore(key, start, Integer.MAX_VALUE);
		} catch (Exception e) {
			logger.error("NewsSource.delTempUserNewsKey exception, userId : " + userId 
					+ " ,start : " + start, e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
	}
	
	/**
	 * 获取插入推荐动态Id
	 * @param userId 用户编号
	 * @param start 索引开始位置
	 * @return
	 */
	public static Set<String> getTempUserNewsIds(int userId, int start, int end) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			return jedis.zrangeByScore(String.format(TEMP_USER_NEWS_INDEX, userId), 
					start, end);
		} catch (Exception e) {
			logger.error("NewsSource.delTempUserNewsKey exception !", e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
		return null;
	}
	
	/**
	 * 删除用户全部推荐动态排序索引位置
	 * @param userId
	 */
	public static void delTempUserNewsKey(int userId) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			jedis.del(String.format(TEMP_USER_NEWS_INDEX, userId));
		} catch (Exception e) {
			logger.error("NewsSource.delTempUserNewsKey exception, userId : " + userId, e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
	}
	
	/**
	 * 获取推荐动态列表
	 * @param start
	 * @param end
	 * @return
	 */
	public static Set<String> getRecNewsIds(int addedCount) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			Set<String> manualSet = new LinkedHashSet<String>();
			Map<String, String> manualNews = getManualNews();
			if (manualNews != null && manualNews.size() > 0) {
				for (int i = 0; i < addedCount; i++) {
					String newsId = manualNews.get(String.valueOf(i + 1));
					if (newsId != null && !newsId.trim().isEmpty()) 
					manualSet.add(newsId);
				}
				if (manualSet.size() < addedCount && addedCount > 1) {
					String key = getFilterNewsKey();
					Set<String> hotNews = jedis.zrevrange(key, 0, addedCount - 1);
					if (hotNews != null && hotNews.size() > 0) {
						Iterator<String> it = hotNews.iterator();  
						for (int i = 0; i < addedCount && manualSet.size() < addedCount; i++) {
							while (it.hasNext() && manualSet.size() < addedCount) {  
							  String newsId = it.next();  
							  if (!manualSet.contains(newsId))
								  manualSet.add(newsId);
							}  
						}
					}
				}
				
				return manualSet;
			} else {
				String key = getFilterNewsKey();
				// 获取5条推荐动态
				if (key != null)
					return jedis.zrevrange(key, 0, addedCount);
			}
		} catch (Exception e) {
			logger.error("NewsSource.delTempUserNewsKey exception !", e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
		
		return null;
	}
	
	/**
	 * 获取内存中加入用户动态数量
	 * @param userId 用户编号
	 * @return
	 */
	public static Integer getUsedTempUserNewsCount(int userId) {
		int total = 0;
		Jedis jedis = null;
		try {
			jedis = getInstance();
			total = jedis.zcard(String.format(TEMP_USER_NEWS_INDEX, userId)).intValue();
		} catch (Exception e) {
			logger.error("NewsSource.getUsedTempUserNewsCount exception, userId : " + userId, e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
		return total;
	}
	
	/**
	 * 获取热门视频动态记录
	 * @param start
	 * @param end
	 * @return
	 */
	public static Set<Tuple> getRankOfHotVideoNews(int start, int end) {
		Set<Tuple> resultSet = null;
		Jedis jedis = null;
		try {
			jedis = getInstance();
			String key = getFilterNewsKey();
			if (key != null)
			return jedis.zrevrangeWithScores(key, start - 1, end - 1);
		} catch (Exception e) {
			logger.error("NewsSource.getRankOfHotVideoNews exception, start : " + start
					+ " ,end : " + end, e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
		
		return resultSet;
	}

	/**
	 * 设置热门动态标签
	 * @param newsHotTags
	 */
	public static void setNewsHotTags(Map<String, String> newsHotTags) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			jedis.hmset(NEWS_HOT_TAGS, newsHotTags);
		} catch (Exception e) {
			logger.error("NewsSource.setNewsHotTags exception, newsHotTags : " + new Gson().toJson(newsHotTags), e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
	}
	
	/**
	 * 删除热门动态标签
	 * @param tagId 标签Id
	 */
	public static void delNewsHotTag(int tagId) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			jedis.hdel(NEWS_HOT_TAGS, String.valueOf(tagId));
		} catch (Exception e) {
			logger.error("NewsSource.delNewsHotTag exception, tagId : " + tagId, e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
		
	} 
	
	/**
	 * 获取热门动态标签
	 * @return
	 */
	public static List<String> getHotTags() {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			return jedis.hvals(NEWS_HOT_TAGS);
		} catch (Exception e) {
			logger.error("NewsSource.getHotTags exception !", e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
		
		return null;
	}
	
	/**
	 * 设置热拍查询临时记录
	 * @param pageOftempHotNews
	 */
	public static void setRePaiPageList(Integer tagId, Map<String, String> pageOftempHotNews, int version) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			String key = null;
			if (tagId != null && tagId > 0) {
				if (version > 0) {
					key = String.format(NEW_REPAI_HOT_NEWS_LIST_BY_TAG, tagId);
					jedis.hmset(key, pageOftempHotNews);
				} else {
					key = String.format(REPAI_HOT_NEWS_LIST_BY_TAG, tagId);
					jedis.hmset(key, pageOftempHotNews);
				}
			} else {
				if (version > 0) {
					key = NEW_REPAI_PAGE_HOT_NEWS_NO_TAG;
					jedis.hmset(key, pageOftempHotNews);
				} else {
					key = REPAI_PAGE_HOT_NEWS_NO_TAG;
					jedis.hmset(key, pageOftempHotNews);
				}
			}
			if (jedis.ttl(key) == -1) {
				jedis.expire(key, 30);
			}
		} catch (Exception e) {
			logger.error("NewsSource.setRePaiPageList exception, pageOftempHotNews : " 
					+ new Gson().toJson(pageOftempHotNews), e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
	}
	
	/**
	 * 查看热拍页码是否存在
	 * @param key
	 * @return
	 */
	public static boolean isRePaiPageExits(Integer tagId, String key) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			if (tagId != null && tagId > 0) {
				return jedis.hexists(String.format(REPAI_HOT_NEWS_LIST_BY_TAG, tagId), key);
			} else {
				return jedis.hexists(REPAI_PAGE_HOT_NEWS_NO_TAG, key);
			}
		} catch (Exception e) {
			logger.error("NewsSource.isRePaiWithTagPageExits exception, key : " + key, e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
		
		return false;
	}

	/**
	 * 获取热拍查询动态列表
	 * @param field
	 * @return
	 */
	public static String getRePaiPageList(Integer tagId ,String field, int version) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			if (tagId != null && tagId > 0) {
				if (version > 0) {
					return jedis.hget(String.format(NEW_REPAI_HOT_NEWS_LIST_BY_TAG, tagId), field);
				} else {
					return jedis.hget(String.format(REPAI_HOT_NEWS_LIST_BY_TAG, tagId), field);
				}
			} else {
				if (version > 0) {
					return jedis.hget(String.format(NEW_REPAI_PAGE_HOT_NEWS_NO_TAG, tagId), field);
				} else {
					return jedis.hget(REPAI_PAGE_HOT_NEWS_NO_TAG, field);
				}
			}
		} catch (Exception e) {
			logger.error("NewsSource.getRePaiNoTagPageList exception, field : " + field, e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
		
		return null;
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

	/**
	 * 删除推荐动态、热拍动态
	 * @param newsId 动态编号
	 */
	public static void delRecAndRepaiNews(String newsId) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			String key = getFilterNewsKey();
			saveDeletedNewsId(newsId, key);
			jedis.zrem(key, newsId);
		} catch (Exception e) {
			logger.error("NewsFactorScore.delRecAndRepaiNews exception !", e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
	}
	
	/**
	 * 保存删除的动态Id 一天过期
	 * @param newsId 动态Id
	 * @param curTime 当前时间 
	 */
	public static void saveDeletedNewsId(String newsId, String curTime) {
		Jedis jedis = null;
		String key = String.format(USER_DELETED_NEWS, curTime);
		try {
			jedis = getInstance();
			jedis.sadd(key, newsId);
			if (jedis.ttl(key) == -1) {
				jedis.expire(key, 86400);
			}
		} catch (Exception e) {
			logger.error("NewsFactorScore.saveDeletedNewsId exception, newsId : " + newsId
					+ " ,curTime : " + curTime, e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
	}
	
	/**
	 * 删除热拍动态
	 * @param newsId
	 */
	public static void delRePaiNews(String newsId) {
		
		Jedis jedis = null;
		String key = getFilterNewsKey();
		try {
			jedis = getInstance();
			if (key != null)
			jedis.zrem(key, newsId);
		} catch (Exception e) {
			logger.error("NewsSource.delRePaiNews exception, newsId : " + newsId, e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
	}
	
	/**
	 * 获取动态因数总分记录 (获取筛选动态Id) *  
	 * @param start
	 * @param end
	 * @return
	 */
	public static Set<String> getFilterNewsIds(int start, int end) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			String key = getFilterNewsKey();
			return jedis.zrevrange(key, start - 1, end - 1);
		} catch (Exception e) {
			logger.error("NewsSource.getNewsFactorScore exception, start : " + start
					+ " ,end : " + end, e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
		return null;
	}
	
	public static String getFilterNewsKey() {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			return jedis.get(CURRENT_NEWS_KEY);
		} catch (Exception e) {
			logger.error("NewsSource.getFailterNewsKey exception !", e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
		
		return null;
	}
	
	/**
	 * 删除的推荐、热拍动态Id
	 * @param curTime
	 */
	private static void delSavedNewsIds(String curTime) {
		Jedis jedis = null;
		String key = String.format(USER_DELETED_NEWS, curTime);
		try {
			jedis = getInstance();
			Set<String> delNewsIds = jedis.smembers(key);
			if (delNewsIds != null && delNewsIds.size() > 0) {
				String filterkey = getFilterNewsKey();
				Iterator<String> it = delNewsIds.iterator();
				 while (it.hasNext()) {
				    String newsId = it.next();
					jedis.zrem(filterkey, newsId);
				 }
			}
		} catch (Exception e) {
			logger.error("NewsFactorScore.delSavedNewsIds exception, curTime : " + curTime, e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
	}
	
	/**
	 * 设置人工推荐动态
	 * @param news
	 */
	public static void addManualNews(Map<String, String> manualNews) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			jedis.hmset(MANUAL_NEWS_LIST, manualNews);
		} catch (Exception e) {
			logger.error("NewsSource.addManualNews exception !", e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
	}
	
	/**
	 * 获取人工推荐动态信息
	 * @return
	 */
	public static Map<String, String> getManualNews() {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			return jedis.hgetAll(MANUAL_NEWS_LIST);
		} catch (Exception e) {
			logger.error("NewsSource.getManualNews exception !", e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
		
		return null;
	}
	
	/**
	 * 更新人工推荐动态
	 * @param index
	 * @param newsId
	 */
	public static void updateManualNews(String index, String newsId) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			if (jedis.hexists(MANUAL_NEWS_LIST, index))
				jedis.hset(MANUAL_NEWS_LIST, index, newsId);
		} catch (Exception e) {
			logger.error("NewsSource.updateManualNews exception, newsId : " + newsId, e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
	}
	
	/**
	 * 删除人工推荐动态
	 * @param index
	 */
	public static void delManualNews(String index) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			if (jedis.hexists(MANUAL_NEWS_LIST, index))
				jedis.hdel(MANUAL_NEWS_LIST, index);
		} catch (Exception e) {
			logger.error("NewsSource.delManualNews exception, index : " + index, e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
	}

}

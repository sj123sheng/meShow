package com.melot.kktv.redis;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.melot.kktv.util.redis.RedisConfigHelper;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

public class WeekGiftSource {
	
private static final String SOURCE_NAME = "WeekGift";

	//key格式 weekGift_weektime_giftid
	private static final String WEEKGIFT_KEY_FORMAT = "weekGift_%s_%s";
	
	//用户送出礼物key
	private static final String WEEKGIFT_USER_KEY_FORMAT = "weekGiftUser_%s_%s";
	
	private static final String WEEKGIFT_TOTAL_RANK = "weekGiftTotalThreeRank_%s";
	
	private static Jedis getInstance() {
		return RedisConfigHelper.getJedis(SOURCE_NAME);
	}
	
	private static void freeInstance(Jedis jedis) {
		RedisConfigHelper.returnJedis(SOURCE_NAME, jedis, false);
	}
	
	public static Map<String, Object> getWeekGiftRank(String weekTime, String giftId, String userId) {
		Map<String, Object> map = null;
		Jedis jedis = null;
		try {
			jedis = getInstance();
			String key = String.format(WEEKGIFT_KEY_FORMAT, weekTime, giftId);
			Long rank = jedis.zrevrank(key, userId);
			if (rank != null && rank.longValue() <= 19l) {
				Long total = new Long(jedis.zscore(key, userId).longValue());
				Long diff = 0L;
				Long downDiff = 0L;
				String upUid = null;
				String downUid = null;
				if (rank.longValue() > 0) {
					Set<Tuple> tupleSet = jedis.zrevrangeWithScores(key, rank.longValue() - 1, rank.longValue() - 1);
					if(!tupleSet.isEmpty() && tupleSet.iterator().hasNext()) {
						Tuple tuple = tupleSet.iterator().next();
						upUid = tuple.getElement();
						diff = new Double(tuple.getScore()).longValue() - total;
					}
					tupleSet = jedis.zrevrangeWithScores(key, rank.longValue() + 1, rank.longValue() + 1);
					if(!tupleSet.isEmpty() && tupleSet.iterator().hasNext()) {
						Tuple tuple = tupleSet.iterator().next();
						downUid = tuple.getElement();
						downDiff = new Double(tuple.getScore()).longValue() - total;
					}
				} else {
					Set<Tuple> tupleSet = jedis.zrevrangeWithScores(key, rank.longValue() + 1, rank.longValue() + 1);
					if(!tupleSet.isEmpty() && tupleSet.iterator().hasNext()) {
						Tuple tuple = tupleSet.iterator().next();
						downUid = tuple.getElement();
						downDiff = new Double(tuple.getScore()).longValue() - total;
					}
				}
				Set<Tuple> tupleSet = jedis.zrevrangeWithScores(key, 0, 2);
				map = new HashMap<String, Object>();
				int count = 0;
				for (Tuple tuple : tupleSet) {
					count++;
					map.put("userId." + count, Integer.valueOf(tuple.getElement()));
					map.put("total." + count, new Double(tuple.getScore()).longValue());
				}
				map.put("rank", rank+1);
				map.put("total", total);
				map.put("diff", diff);
				map.put("upUid", upUid);
				map.put("downDiff", downDiff);
				map.put("downUid", downUid);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return map;
	}
	
	public static Map<Integer, Long> getWeekGiftRank(String weekTime, String giftId, int count) {
		Map<Integer, Long> map = null;
		Jedis jedis = null;
		try {
			jedis = getInstance();
			String key = String.format(WEEKGIFT_KEY_FORMAT, weekTime, giftId);
			Set<Tuple> tupleSet = jedis.zrevrangeWithScores(key, 0, count - 1);
			if (tupleSet != null && tupleSet.size() > 0) {
				map = new LinkedHashMap<Integer, Long>();
				for (Iterator<Tuple> iterator = tupleSet.iterator(); iterator.hasNext();) {
					Tuple tuple = iterator.next();
					map.put(Integer.valueOf(tuple.getElement()), new Double(tuple.getScore()).longValue());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return map;
	}
	
	public static Map<Integer, Long> getUserWeekGiftRank(String weekTime, String giftId, int count) {
		Map<Integer, Long> map = null;
		Jedis jedis = null;
		try {
			jedis = getInstance();
			String key = String.format(WEEKGIFT_USER_KEY_FORMAT, weekTime, giftId);
			Set<Tuple> tupleSet = jedis.zrevrangeWithScores(key, 0, count - 1);
			if (tupleSet != null && tupleSet.size() > 0) {
				map = new LinkedHashMap<Integer, Long>();
				for (Iterator<Tuple> iterator = tupleSet.iterator(); iterator.hasNext();) {
					Tuple tuple = iterator.next();
					map.put(Integer.valueOf(tuple.getElement()), new Double(tuple.getScore()).longValue());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
		return map;
	}
	
	public static List<String> getWeekGiftRankList(int start, int offset, int type) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			String key = String.format(WEEKGIFT_TOTAL_RANK, type);
			return jedis.lrange(key, start - 1, offset - 1);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
		
		return null;
	}
	
	public static void addWeekGiftRankList(String[] giftRank, int type) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			String key = String.format(WEEKGIFT_TOTAL_RANK, type);
			jedis.rpush(key, giftRank);
			jedis.expireAt(key, System.currentTimeMillis() / 1000 + 60);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
	}
	
	public static long getWeekGiftRankListCount(int type) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			String key = String.format(WEEKGIFT_TOTAL_RANK, type);
			Long result = jedis.llen(key);
			return result == null ? 0 : result.longValue();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
		
		return 0l;
	}
	
}

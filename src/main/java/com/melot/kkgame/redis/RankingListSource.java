/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.redis;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.game.config.sdk.utils.StringUtils;
import com.melot.kkgame.redis.support.RedisException;
import com.melot.kkgame.redis.support.RedisTemplate;
import com.melot.kktv.util.DateUtil;

/**
 * Title: RankingListSource
 * <p>
 * Description: Redis保存排行榜信息
 * </p>
 * 
 * @author 姚国平<a href="mailto:guoping.yao@melot.cn">
 * @version V1.0
 * @since 2015-6-11 下午2:42:23
 */
public class RankingListSource extends RedisTemplate {

	private static Logger logger = Logger.getLogger(RankingListSource.class);

	@Override
	public String getSourceName() {
		return "RankingList";
	}

	private static final Map<String, String> rankTypeMap = new HashMap<String, String>();

	private static JsonParser jsonParser = new JsonParser();

	private static final String ACTOR_DAILY_RANKING = "actor_daily_ranking";
	private static final String ACTOR_Weekly_RANKING = "actor_weekly_ranking";
	private static final String ACTOR_Monthly_RANKING = "actor_monthly_ranking";
	private static final String ACTOR_TOTAL_RANKING = "actor_total_ranking";
	private static final String RICH_DAILY_RANKING = "rich_daily_ranking";
	private static final String RICH_Weekly_RANKING = "rich_weekly_ranking";
	private static final String RICH_Monthly_RANKING = "rich_monthly_ranking";
	private static final String RICH_TOTAL_RANKING = "rich_total_ranking";

	private static final String LIVE_TIME_DAILY_RANKING = "live_time_daily_ranking";
	private static final String LIVE_TIME_Weekly_RANKING = "live_time_weekly_ranking";
	private static final String LIVE_TIME_Monthly_RANKING = "live_time_monthly_ranking";
	private static final String LIVE_TIME_TOTAL_RANKING = "live_time_total_ranking";

	private static final String GIFT_CURRENT_WEEK_RANKING = "gift_current_week_ranking";
	private static final String GIFT_LAST_WEEK_RANKING = "gift_last_week_ranking";

	private static final String FAMILY_RANKING_PREFEX = "family_ranking_";

	/**
	 * 用户某时间点是否抢到
	 */
	private static final String GODDESS_LOTTERY = "goddess_lottery";
	/**
	 * 已经参与过的用户列表
	 */
	public static final String GODDESS_PLAYED = "goddess_played_";
	/**
	 * 礼物队列
	 */
	public static final String GODDESS_GIFT = "goddess_gift_";

	/**
	 * 送礼任务队列
	 */
	private static final String GODDESS_GIFT_TASK = "goddess_gift_task";

	static {
		rankTypeMap.put("0_0", ACTOR_DAILY_RANKING);
		rankTypeMap.put("0_1", ACTOR_Weekly_RANKING);
		rankTypeMap.put("0_2", ACTOR_Monthly_RANKING);
		rankTypeMap.put("0_3", ACTOR_TOTAL_RANKING);
		rankTypeMap.put("1_0", RICH_DAILY_RANKING);
		rankTypeMap.put("1_1", RICH_Weekly_RANKING);
		rankTypeMap.put("1_2", RICH_Monthly_RANKING);
		rankTypeMap.put("1_3", RICH_TOTAL_RANKING);
		rankTypeMap.put("2_0", LIVE_TIME_DAILY_RANKING);
		rankTypeMap.put("2_1", LIVE_TIME_Weekly_RANKING);
		rankTypeMap.put("2_2", LIVE_TIME_Monthly_RANKING);
		rankTypeMap.put("2_3", LIVE_TIME_TOTAL_RANKING);

		rankTypeMap.put("3_0", GIFT_CURRENT_WEEK_RANKING);
		rankTypeMap.put("3_1", GIFT_LAST_WEEK_RANKING);

	}

	public String getRankingTypeKey(int rankType, int slotType) {
		return rankTypeMap.get(rankType + "_" + slotType);
	}

	/**
	 * 从Redis读取各类排行榜
	 */
	public Set<String> getRankingList(final String rankTypeKey) throws RedisException {
		return zrange(rankTypeKey, 0, -1);
	}

	/**
	 * 获取基于家族的礼物榜单, 若date没传,则为总榜
	 */
	public Set<String> getFamilyRankingList(final String giftId, final String date) throws RedisException {
		final String key = date == null ? FAMILY_RANKING_PREFEX + giftId : FAMILY_RANKING_PREFEX + giftId + "_" + date;
		return zrange(key, 0, -1);
	}

	/**
	 * 获取排行榜
	 * 
	 * @param key
	 * @param field
	 * @return
	 */
	public String getRankList(String key, String field) {
		if (key.startsWith("activity_")) {
			key = key.replace("activity_", "");
			return hget(key, field);
		}
		return null;
	}

	/**
	 * 当前时段是否已经参与过活动 是返回true，否返回false
	 */
	public boolean hadGoddessLottery(int userId, String type) {
		String countStr = hget(GODDESS_PLAYED + type + "_" + getTimeField(), userId + "");
		if (countStr == null) {
			return false;
		}
		int count = Integer.valueOf(countStr);
		if (count >= 3) {
			return true;
		}
		return false;
	}

	/**
	 * 当前时段参与过活动
	 * 
	 * @param userId
	 */
	public void putGoddessLottery(int userId, String type) {
		hincrBy(GODDESS_PLAYED + type + "_" + getTimeField(), userId + "", 1);
	}

	/**
	 * 查询用户是否有抽奖机会
	 * 
	 * 有抽奖机会返回大于0的值，没有返回小于0的值
	 *
	 * @param userId
	 * @return
	 */
	public int getLotteryChance(int userId, String type) {
		try {
			int needCount = Integer.valueOf(type);
			Double restCountD = zscore(GODDESS_LOTTERY, userId + "");
			if (restCountD == null) {
				return -needCount;
			} else {
				int restCount = restCountD.intValue();
				return restCount - needCount;
			}
		} catch (RedisException e) {
			logger.error(e);
		}
		return 0;
	}

	/**
	 * 抽奖操作
	 * 
	 * @param type
	 * @return
	 */
	public JsonObject doLottery(int userId, String type) {
		JsonObject jsonObj = null;
		try {
			if (getLotteryChance(userId, type) >= 0) {
				int needCount = Integer.valueOf(type);
				String giftStr = rpop(GODDESS_GIFT + getTimeField() + "_" + type);
				if (!StringUtils.isEmpty(giftStr)) {
					jsonObj = jsonParser.parse(giftStr).getAsJsonObject();
					jsonObj.addProperty("userId", userId);
					// 发布到送礼任务
					publish(GODDESS_GIFT_TASK, jsonObj.toString());
					// 标记当前时间已参与抽奖
					putGoddessLottery(userId, type);
					// 扣除礼物数
					zincrby(GODDESS_LOTTERY, -needCount, userId + "");
					return jsonObj;
				}
			}
		} catch (RedisException e) {
			logger.error(e);
		}
		return null;
	}

	/**
	 * 获取时间点
	 * 
	 * @return
	 */
	public static String getTimeField() {
		Date date = new Date();
		String dateStr = getDateStr(date);
		int timeField = DateUtil.getFieldOfDate(date, Calendar.HOUR_OF_DAY);
		if (timeField >= 1 && timeField < 6) {
			return dateStr + "_" + 1;
		} else if (timeField >= 6 && timeField < 12) {
			return dateStr + "_" + 6;
		} else if (timeField >= 12 && timeField < 18) {
			return dateStr + "_" + 12;
		} else if (timeField >= 18 && timeField < 20) {
			return dateStr + "_" + 18;
		} else if (timeField >= 20 && timeField < 22) {
			return dateStr + "_" + 20;
		} else if (timeField >= 22 && timeField < 24) {
			return dateStr + "_" + 22;
		}else if(timeField < 1){
		    dateStr = getDateStr(DateUtil.addOnField(date, Calendar.DAY_OF_MONTH, -1));
		    return dateStr + "_" + 22;
		}
		return "";
	}

	private static String getDateStr(Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(date);
	}
	
	@Override
	public Boolean sismember(String key, String member) {
		try {
			return super.sismember(key, member);
		} catch (RedisException e) {
			logger.error(e);
		}
		return false;
	}

	@Override
	public Long hset(String key, String field, String value) {
		try {
			return super.hset(key, field, value);
		} catch (RedisException e) {
			logger.error(e);
		}
		return 0L;
	}

	@Override
	public String hget(String key, String field) {
		try {
			return super.hget(key, field);
		} catch (RedisException e) {
			logger.error(e);
		}
		return null;
	}

	@Override
	public Long lpush(String key, String... strings) {
		try {
			return super.lpush(key, strings);
		} catch (RedisException e) {
			logger.error(e);
		}
		return null;
	}

	@Override
	public Long publish(String channel, String message) {
		try {
			return super.publish(channel, message);
		} catch (RedisException e) {
			logger.error(e);
		}
		return 0L;
	}

	@Override
	public Long hincrBy(String key, String field, long value) {
		try {
			return super.hincrBy(key, field, value);
		} catch (RedisException e) {
			logger.error(e);
		}
		return 0L;
	}

	@Override
	public Long hdel(String key, String... fields) {
		try {
			return super.hdel(key, fields);
		} catch (RedisException e) {
			logger.error(e);
		}
		return 0L;
	}

	@Override
	public Long sadd(String key, String... members) {
		try {
			return super.sadd(key, members);
		} catch (RedisException e) {
			logger.error(e);
		}
		return 0L;
	}
	
}

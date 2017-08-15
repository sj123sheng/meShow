package com.melot.kktv.redis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonObject;
import com.melot.kktv.util.redis.RedisConfigHelper;

import redis.clients.jedis.Jedis;

public class MatchSource {
	
	private static final String SOURCE_NAME = "Match";
	
	// 家族擂台赛本期本场比赛结果	(familyMatchPlay_period_play)
	private static final String FAMILYMATCHPLAYCACHE_KEY = "familyMatchPlay_%s_%s";
	// 家族擂台赛每期的用户结果缓存数据 familyMatchUserCache_period
	private static final String FAMILYMATCHUSERCACHE_KEY = "familyMatchUserCache_";
	// 家族擂台赛每期的主播结果缓存数据 familyMatchUserCache_period
	private static final String FAMILYMATCHACTORCACHE_KEY = "familyMatchActorCache_";
	// 家族擂台赛本期本场比赛统计
	private static final String FAMILYMATCHPLAYSTATS_KEY = "familyMatchPlayStats_%s_%s";
	// 家族擂台赛家族冠军榜结果缓存数据 familyMatchFamilyChampion
	private static final String FAMILYMATCHFAMILYCHAMPION_KEY = "familyMatchFamilyChampion";
	// 家族擂台赛富豪冠军榜结果缓存数据 familyMatchRichChampion
	private static final String FAMILYMATCHRICHCHAMPION_KEY = "familyMatchRichChampion";
	// 家族擂台赛蝉联冠军榜结果缓存数据 familyMatchContinueChampion
	private static final String FAMILYMATCHCONTINUECHAMPION_KEY = "familyMatchContinueChampion";
	
	// 活动比赛各场次缓存数据(activityMatch_activityId_matchId)
	private static final String ACTIVITYMATCH_KEY = "activityMatch_%s_%s";
	
	// 比赛报名记录(signUpMatch_userId_matchId)
	private static final String SIGNUPMATCH_KEY = "signUpMatch_%s_%s";
	// 场次参赛记录(userPlay_userId)
	private static final String USERPLAY_KEY = "userPlay_";
	// 比赛阶段投票记录(matchVote_matchId_stage)
	private static final String MATCHVOTE_KEY = "matchVote_%s_%s";
	// 比赛投票限制记录(voteRule_userId_matchId)
	private static final String VOTERULE_KEY = "voteRule_%s_%s";
	// 比赛投票队列(vote_queue)
	private static final String MATCHVOTE_QUEUE = "match_vote";
	// 比赛场次排名数据(playRank_playId)
	private static final String PLAYRANK_KEY = "playRank_";
	// 比赛场次选手信息(playUsers_playId)
	private static final String PLAYUSER_KEY = "playUsers_";
	
	/**
	 * 房间粉丝榜单缓存数据 roomFansRank_slotType_roomId
	 */
	private static final String ROOMFANSRANK_KEY = "roomFansRank_%s_%s_%s";
	
	private static Jedis getInstance() {
		return RedisConfigHelper.getJedis(SOURCE_NAME);
	}
	
	private static void freeInstance(Jedis jedis) {
		RedisConfigHelper.returnJedis(SOURCE_NAME, jedis, false);
	}
	
	/**
	 * 设置家族擂台赛用户排名缓存数据
	 * @param period
	 * @param data
	 */
	public static void setFamilyMatchUserCache(String period, String data, int seconds) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			jedis.set(FAMILYMATCHUSERCACHE_KEY+period, data);
			jedis.expire(FAMILYMATCHUSERCACHE_KEY+period, seconds);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
	}
	
	/**
	 * 获取家族擂台赛用户排名缓存数据
	 * @param period
	 * @param data
	 */
	public static String getFamilyMatchUserCache(String period) {
		Jedis jedis = null;
		String data = null;
		try {
			jedis = getInstance();
			data = jedis.get(FAMILYMATCHUSERCACHE_KEY+period);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return data;
	}
	
	/**
	 * 设置家族擂台赛主播排名缓存数据(8小时过期)
	 * @param period
	 * @param data
	 */
	public static void setFamilyMatchActorCache(String period, String data, int seconds) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			jedis.set(FAMILYMATCHACTORCACHE_KEY+period, data);
			jedis.expire(FAMILYMATCHACTORCACHE_KEY+period, seconds);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
	}
	
	/**
	 * 获取家族擂台赛主播排名缓存数据
	 * @param period
	 * @param data
	 */
	public static String getFamilyMatchActorCache(String period) {
		Jedis jedis = null;
		String data = null;
		try {
			jedis = getInstance();
			data = jedis.get(FAMILYMATCHACTORCACHE_KEY+period);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return data;
	}
	
	/**
	 * 设置家族擂台赛家族冠军榜缓存数据(8小时过期)
	 * @param data
	 */
	public static void setFamilyMatchFamilyChampion(String data, int seconds) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			jedis.set(FAMILYMATCHFAMILYCHAMPION_KEY, data);
			jedis.expire(FAMILYMATCHFAMILYCHAMPION_KEY, seconds);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
	}
	
	/**
	 * 获取家族擂台赛家族冠军榜缓存数据
	 * @param data
	 */
	public static String getFamilyMatchFamilyChampion() {
		Jedis jedis = null;
		String data = null;
		try {
			jedis = getInstance();
			data = jedis.get(FAMILYMATCHFAMILYCHAMPION_KEY);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return data;
	}
	
	/**
	 * 设置家族擂台赛富豪冠军榜缓存数据(8小时过期)
	 * @param data
	 */
	public static void setFamilyMatchRichChampion(String data, int seconds) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			jedis.set(FAMILYMATCHRICHCHAMPION_KEY, data);
			jedis.expire(FAMILYMATCHRICHCHAMPION_KEY, seconds);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
	}
	
	/**
	 * 获取家族擂台赛富豪冠军榜缓存数据
	 * @param data
	 */
	public static String getFamilyMatchRichChampion() {
		Jedis jedis = null;
		String data = null;
		try {
			jedis = getInstance();
			data = jedis.get(FAMILYMATCHRICHCHAMPION_KEY);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return data;
	}
	
	/**
	 * 设置某期某场家族擂台赛结果
	 * @param data
	 * @param period
	 * @param play
	 */
	public static void setFamilyMatchPlay(String data, String period, String play, Integer seconds) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			String pattern = String.format(FAMILYMATCHPLAYCACHE_KEY, period, play);
			jedis.set(pattern, data);
			if(seconds!=null)
				jedis.expire(pattern, seconds.intValue());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
	}
	
	/**
	 * 获取某期某场家族擂台赛结果
	 * @param period
	 * @param play
	 */
	public static String getFamilyMatchPlay(String period, String play) {
		Jedis jedis = null;
		String data = null;
		try {
			jedis = getInstance();
			String pattern = String.format(FAMILYMATCHPLAYCACHE_KEY, period, play);
			data = jedis.get(pattern);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return data;
	}
	
	/**
	 * 设置家族擂台赛蝉联冠军榜缓存数据(8小时过期)
	 * @param data
	 */
	public static void setFamilyMatchContinueChampion(String data, int seconds) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			jedis.set(FAMILYMATCHCONTINUECHAMPION_KEY, data);
			jedis.expire(FAMILYMATCHCONTINUECHAMPION_KEY, seconds);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
	}
	
	/**
	 * 获取家族擂台赛蝉联冠军榜缓存数据
	 * @param data
	 */
	public static String getFamilyMatchContinueChampion() {
		Jedis jedis = null;
		String data = null;
		try {
			jedis = getInstance();
			data = jedis.get(FAMILYMATCHCONTINUECHAMPION_KEY);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return data;
	}
	
	/**
	 * 设置某活动某场次排行榜缓存数据
	 * @param data
	 * @param activityId 活动ID
	 * @param matchId 场次ID
	 */
	public static void setActivityMatch(String activityId, String matchId, String data, int seconds) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			String pattern = String.format(ACTIVITYMATCH_KEY, activityId, matchId);
			jedis.set(pattern, data);
			jedis.expire(pattern, seconds);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
	}
	
	/**
	 * 获取某活动某场次排行榜缓存数据
	 * @param activityId 活动ID
	 * @param matchId 场次ID
	 */
	public static String getActivityMatch(String activityId, String matchId) {
		Jedis jedis = null;
		String data = null;
		try {
			jedis = getInstance();
			String pattern = String.format(ACTIVITYMATCH_KEY, activityId, matchId);
			data = jedis.get(pattern);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return data;
	}
	
	/**
	 * 检查用户是否报名比赛
	 * @param userId
	 * @param matchId
	 * @return
	 */
	public static String checkUserMatch(String userId, String matchId) {
		Jedis jedis = null;
		String data = null;
		try {
			jedis = getInstance();
			String pattern = String.format(SIGNUPMATCH_KEY, userId, matchId);
			data = jedis.get(pattern);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return data;
	}
	
	/**
	 * 设置用户比赛(报名比赛)
	 * @param userId
	 * @param matchId
	 * @param data
	 * @param seconds
	 */
	public static void signUpMatch(String userId, String matchId, String playId, long unixTime) {
		
		Jedis jedis = null;
		try {
			jedis = getInstance();
			String pattern = String.format(SIGNUPMATCH_KEY, userId, matchId);
			jedis.set(pattern, playId);
			jedis.expireAt(pattern, unixTime);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		
	}
	
	/**
	 * 参赛
	 * @param userId
	 * @param playId
	 * @param data
	 */
	public static void joinMatchPlay(String userId, String playId, String data) {
		
		Jedis jedis = null;
		try {
			jedis = getInstance();
			jedis.hset(USERPLAY_KEY+userId, playId, data);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		
	}
	
	/**
	 * 清除用户参赛场次信息
	 * @param userId
	 * @return
	 */
	public static void delUserMatchPlay(String userId) {

		Jedis jedis = null;
		try {
			jedis = getInstance();
			jedis.del(USERPLAY_KEY+ userId);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
	
	}
	
	/**
	 * 获取用户参赛场次信息
	 * @param userId
	 * @return
	 */
	public static String getUserMatchPlayInfo(String userId, String playId) {
		Jedis jedis = null;
		String data = null;
		try {
			jedis = getInstance();
			data = jedis.hget(USERPLAY_KEY+userId, playId);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return data;
	}
	
	/**
	 * 获取用户参赛场次信息
	 * @param userId
	 * @return
	 */
	public static Map<String, String> getUserMatchPlayInfo(String userId) {
		Jedis jedis = null;
		Map<String, String> map = new HashMap<String, String>();
		try {
			jedis = getInstance();
			map = jedis.hgetAll(USERPLAY_KEY+userId);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return map;
	}
	
	/**
	 * 获取用户比赛投票规则记录数据
	 * @param userId
	 * @param matchId
	 * @return
	 */
	public static Map<String, String> getUserVoteRuleInfo(String userId, String matchId) {
		Jedis jedis = null;
		Map<String, String> map = new HashMap<String, String>();
		try {
			jedis = getInstance();
			String pattern = String.format(VOTERULE_KEY, userId, matchId);
			map = jedis.hgetAll(pattern);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return map;
	}
	
	/**
	 * 获取用户比赛投票规则记录数据
	 * @param userId
	 * @param matchId
	 * @return
	 */
	public static int getUserVoteRuleInfo(String userId, String playerId, String matchId) {
		Jedis jedis = null;
		int count = 0;
		try {
			jedis = getInstance();
			String pattern = String.format(VOTERULE_KEY, userId, matchId);
			String value = jedis.hget(pattern, playerId);
			if (value!=null) count = Integer.parseInt(value);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return count;
	}
	
	/**
	 * 比赛投票规则计数
	 * @param userId
	 * @param matchId
	 * @param seconds
	 */
	public static void incUserVoteRule(String userId, String matchId, String playerId, long unixTime) {

		Jedis jedis = null;
		try {
			jedis = getInstance();
			String pattern = String.format(VOTERULE_KEY, userId, matchId);
			jedis.hincrBy(pattern, playerId, 1);
			jedis.expireAt(pattern, unixTime);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
	
	}
	
	/**
	 * 获取用户比赛投票规则记录数据
	 * @param userId
	 * @param matchId
	 * @param stage
	 * @return
	 */
	public static int getUserMatchVote(String userId, String matchId, String stage) {
		Jedis jedis = null;
		int count = 0;
		try {
			jedis = getInstance();
			String pattern = String.format(MATCHVOTE_KEY, matchId, stage);
			String value = jedis.hget(pattern, userId);
			if(value!=null) count = Integer.parseInt(value);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return count;
	}
	
	/**
	 * 比赛阶段投票记录
	 * @param userId
	 * @param matchId
	 * @param stage
	 * @param seconds
	 */
	public static int incUserMatchVote(String userId, String matchId, String stage, String playerId, long curTime, long unixTime) {

		Jedis jedis = null;
		long count = 0;
		try {
			jedis = getInstance();
			String pattern = String.format(MATCHVOTE_KEY, matchId, stage);
			count = jedis.hincrBy(pattern, userId, 1);
			jedis.expireAt(pattern, unixTime);
			// 投票记录添加到队列
			JsonObject jsonObj = new JsonObject();
			jsonObj.addProperty("userId", userId);
			jsonObj.addProperty("matchId", matchId);
			jsonObj.addProperty("stage", stage);
			jsonObj.addProperty("playerId", playerId);
			jsonObj.addProperty("time", curTime);
			jedis.rpush(MATCHVOTE_QUEUE, jsonObj.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return new Long(count).intValue();
	
	}
	
	/**
	 * 获取用户在比赛中的排名
	 * @param userId
	 * @param playId
	 * @return
	 */
	public static int getPlayUserRank(String userId, String playId) {

		Jedis jedis = null;
		Long rank = 0L;
		try {
			jedis = getInstance();
			 rank = jedis.zrevrank(PLAYRANK_KEY+playId, userId);
			 if (rank!=null) {
				 rank++;
			 } else {
				 rank = 0L;
			 }
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return rank.intValue();
	
	}
	
	/**
	 * 获取参赛选手信息
	 * @param userId
	 * @param playId
	 * @return
	 */
	public static String getPlayUserInfo(String userId, String playId) {

		Jedis jedis = null;
		String data = null;
		try {
			jedis = getInstance();
			data = jedis.hget(PLAYUSER_KEY+playId, userId);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return data;
	
	}
	
	/**
	 * 获取比赛参赛选手总数
	 * @param playId
	 * @return
	 */
	public static int getPlayPlayerCount(String playId) {
		
		Jedis jedis = null;
		long count = 0;
		try {
			jedis = getInstance();
			count = jedis.zcard(PLAYRANK_KEY+playId);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return new Long(count).intValue();
		
	}
	
	/**
	 * 获取比赛排行榜
	 * @param playId
	 * @param start
	 * @param offset
	 * @return
	 */
	public static Set<String> getPlayRankList(String playId, int start, int end) {

		Jedis jedis = null;
		Set<String> set = new HashSet<String>();
		try {
			jedis = getInstance();
			set = jedis.zrevrange(PLAYRANK_KEY+playId, start, end);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return set;
	
	}
	
	/**
	 * 保存房间粉丝榜单缓存数据
	 * @param roomId 房间编号
	 * @param slotType 榜单类型 1:周榜 2:月榜 3:总榜
	 * @param roomSource 房间来源
	 * @param seconds 过期时间
	 */
	public static void setRoomFansRankCache(String slotType, String roomId, String roomSource, String data, int seconds) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			String pattern = String.format(ROOMFANSRANK_KEY, slotType, roomId, roomSource);
			jedis.set(pattern, data);
			jedis.expire(pattern, seconds);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
	}
	
	/**
	 * 获取房间粉丝榜单缓存数据
	 * @param slotType 榜单类型 1:周榜 2:月榜 3:总榜
	 * @param roomId 房间编号
	 * @return
	 */
	public static String getRoomFansRankCache(String slotType, String roomId, String roomSource) {
		Jedis jedis = null;
		String data = null;
		try {
			jedis = getInstance();
			String pattern = String.format(ROOMFANSRANK_KEY, slotType, roomId, roomSource);
			data = jedis.get(pattern);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return data;
	}
	
	/**
	 * 设置某期某场家族擂台赛统计
	 * @param data
	 * @param period
	 * @param play
	 */
	public static void setFamilyMatchStats(String period, String play, java.util.Map<String, String> data, Integer seconds) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			String pattern = String.format(FAMILYMATCHPLAYSTATS_KEY, period, play);
			jedis.hmset(pattern, data);
			if (seconds != null)
				jedis.expire(pattern, seconds.intValue());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
	}
	
	/**
	 * 获取某期某场家族擂台赛统计
	 * @param period
	 * @param play
	 */
	public static java.util.Map<String, String> getFamilyMatchStats(String period, String play) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			String pattern = String.format(FAMILYMATCHPLAYSTATS_KEY, period, play);
			return jedis.hgetAll(pattern);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return null;
	}
	
}

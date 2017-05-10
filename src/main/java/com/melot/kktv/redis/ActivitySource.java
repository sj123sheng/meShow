package com.melot.kktv.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.melot.kktv.util.redis.RedisConfigHelper;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

public class ActivitySource {
	
	private static final String SOURCE_NAME = "Activity";
	
	private static Jedis getInstance() {
		return RedisConfigHelper.getJedis(SOURCE_NAME);
	}
	
	private static void freeInstance(Jedis jedis) {
		RedisConfigHelper.returnJedis(SOURCE_NAME, jedis, false);
	}
	
	/**
	 * 
	 * "DIMENSION_PLAY".playId.apdId
	 * 存储用户维度统计信息
	 * Map<playerId, sumVal>
	 * 
	 */
	private static final String DIMENSION_PLAY_KEY = "DIMENSION_PLAY.%s.%s";
	
	/**
	 * 获取选手场次维度统计值
	 * @param playId 场次ID
	 * @param apdId 场次维度ID
	 * @param playerId 选手ID
	 * @return 选手维度值
	 */
	public static int getPlayDimensionValue(String playId, String apdId, String playerId) {
		int sumVal = 0;
		Jedis jedis = null;
		try {
			jedis = getInstance();
			String key = String.format(DIMENSION_PLAY_KEY, playId, apdId);
			String value = jedis.hget(key, playerId);
			if (value != null) sumVal = Double.valueOf(value).intValue();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return sumVal;
	}
	
	/**
	 * 获取选手场次维度统计值
	 * @param playId 场次ID
	 * @param apdId 场次维度ID
	 * @param playerId 选手ID
	 * @return 场次维度数据
	 */
	public static Map<String, String> getAllPlayDimensionValue(String playId, String apdId) {
		Map<String, String> result = null;;
		Jedis jedis = null;
		try {
			jedis = getInstance();
			String key = String.format(DIMENSION_PLAY_KEY, playId, apdId);
			result = jedis.hgetAll(key);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return result;
	}
    
    /**
     * 
     * "RANK_PLAY".playId.rankId
     * 存储场次的榜单数据
     * SortedSet<playerInfo, score>
     * 
     */
    private static final String RANK_PLAY_KEY = "RANK_PLAY.%s.%s";
    
    /**
     * 获取榜单选手列表
     * @param playId 场次ID
     * @param rankId 榜单ID
     * @param startNum 从1开始
     * @param endNum 
     * @return mapList {"playerInfo":String,"score":Double}
     */
    public static List<Map<String, Object>> getPlayerRankList(String playId, String rankId, long startNum, long endNum) {
        List<Map<String, Object>> rankList = new ArrayList<Map<String, Object>>();
        Jedis jedis = null;
        try {
            jedis = getInstance();
            String key = String.format(RANK_PLAY_KEY, playId, rankId);
            // 获取榜单选手总数
            long sumNum = jedis.zcard(key);
            // redis 从 0 开始
            if (startNum <= sumNum) {
                if (startNum < 1) {
                    startNum = 1;
                }
                long start = startNum - 1;
                if (endNum > sumNum) {
                    endNum = sumNum;
                }
                long end = endNum - 1;
                Set<Tuple> set = jedis.zrevrangeWithScores(key, start, end);
                int i = 0;
                for (Tuple tuple : set) {
                    Double score = new Double(tuple.getScore());
                    String playerInfo = tuple.getElement();
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("playerInfo", playerInfo);
                    map.put("score", score);
                    map.put("order", ++ i);
                    rankList.add(map);
                    if (endNum > 0 && i >= endNum) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(jedis!=null) {
                freeInstance(jedis);
            }
        }
        return rankList;
    }
    
    /**
     * PLAY_PLAYER.playId
     * 场次选手列表缓存
     */
    private static final String PLAY_PLAYER_KEY = "PLAY_PLAYER.%s";
    
    /**
     * 增加用户到场次选手列表缓存中
     * @param playId 场次ID
     * @param userId 用户ID数组
     * @return true/false
     */
    public static boolean existsPlayPlayerCache(int playId) {
        Jedis jedis = null;
        try {
            jedis = getInstance();
            String key = String.format(PLAY_PLAYER_KEY, playId);
            return jedis.exists(key);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(jedis!=null) {
                freeInstance(jedis);
            }
        }
        return false;
    }
    
    /**
     * 增加用户到场次选手列表缓存中
     * @param playId 场次ID
     * @param userId 用户ID数组
     * @return true/false
     */
    public static boolean addPlayPlayerCache(int playId, String ... userId) {
        Jedis jedis = null;
        try {
            jedis = getInstance();
            String key = String.format(PLAY_PLAYER_KEY, playId);
            if (userId != null && userId.length > 0) {
                jedis.sadd(key, userId);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(jedis!=null) {
                freeInstance(jedis);
            }
        }
        return false;
    }
    
}

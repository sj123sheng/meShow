/**
 * This document and its contents are protected by copyright 2012 and owned by
 * Melot Inc.
 * The copying and reproduction of this document and/or its content (whether
 * wholly or partly) or any
 * incorporation of the same into any other material in any media or format of
 * any kind is strictly prohibited.
 * All rights are reserved.
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kktv.redis;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.Jedis;

import com.melot.kktv.util.redis.RedisConfigHelper;

/**
 * Title: GameRankingSource
 * <p>
 * Description: 游戏家族排行榜热点缓存
 * </p>
 * 
 * @author 姚国平<a href="mailto:guoping.yao@melot.cn">
 * @version V1.0
 * @since 2015-8-26 下午2:04:32
 */
public class GameRankingSource {
    private static final String SOURCE_NAME = "GameRankingList";

    private static final String FAMILY_RANKING_PREFEX = "family_ranking_";

    private static Jedis getInstance() {
        return RedisConfigHelper.getJedis(SOURCE_NAME);
    }

    private static void freeInstance(Jedis jedis) {
        RedisConfigHelper.returnJedis(SOURCE_NAME, jedis, false);
    }
    
    /**
     * 获取家族排行榜信息
     */
    public static Set<String> getFamilyRankingList(final String giftId, final String date) {
        Jedis jedis = null;
        final String key = 
                date == null ? FAMILY_RANKING_PREFEX + giftId : FAMILY_RANKING_PREFEX + giftId + "_" + date;
        try {
            jedis = getInstance();
            if (jedis != null) {
                return jedis.zrange(key , 0, -1);
            }
        } catch (Exception e) {
        } finally {
            if (jedis != null) {
                freeInstance(jedis);
            }
        }
        return null;
    }
    
    
    /**
     * 获取玩家总兑换积分
     */
    public static String getExchangePoint(final String giftId, final Integer userId) {
        Jedis jedis = null;
        final String key = "m3g_activity" + giftId + "_" + userId; 
        try {
            jedis = getInstance();
            if (jedis != null) {
                return jedis.get(key);
            }
        } catch (Exception e) {
        } finally {
            if (jedis != null) {
                freeInstance(jedis);
            }
        }
        return null;
    }
    
    public static Map<String, String> getExchangeInfo(final String giftId, final Integer userId) {
        Jedis jedis = null;
        final String key = "m3g_activity_exchange_" + giftId + "_" + userId; 
        try {
            jedis = getInstance();
            if (jedis != null) {
                return jedis.hgetAll(key);
            }
        } catch (Exception e) {
        } finally {
            if (jedis != null) {
                freeInstance(jedis);
            }
        }
        return null;
    }
    
    /**
     * 玩家进行兑换, 扣除玩家兑换积分
     */
    public static Long minusExchangePoint(final String giftId, final Integer userId, final Integer point) {
        Jedis jedis = null;
        final String key = "m3g_activity" + giftId + "_" + userId; 
        try {
            jedis = getInstance();
            if (jedis != null) {
                Long current = jedis.decrBy(key, point);
                
                return current;
                
            }
        } catch (Exception e) {
        } finally {
            if (jedis != null) {
                freeInstance(jedis);
            }
        }
        return null;
    }
    
    
    /**
     * 玩家进行兑换, 记录兑换次数
     */
    public static Long addExchangeHist(final String giftId, final Integer userId, final Integer point) {
        Jedis jedis = null;
        final String key = "m3g_activity_exchange_" + giftId + "_" + userId; 
        try {
            jedis = getInstance();
            if (jedis != null) {
               jedis.hincrBy(key, String.valueOf(point), 1);
            }
        } catch (Exception e) {
        } finally {
            if (jedis != null) {
                freeInstance(jedis);
            }
        }
        return null;
    }
    
    
    /**
     * 记录玩家兑换日志
     */
    public static Long addExchangeLog(final Integer type,String msg) {
        Jedis jedis = null;
        final String key = "m3g_activity_exchange_" + type; 
        try {
            jedis = getInstance();
            if (jedis != null) {
                return jedis.zadd(key, new Date().getTime(), msg);
            }
        } catch (Exception e) {
        } finally {
            if (jedis != null) {
                freeInstance(jedis);
            }
        }
        return null;
    }
    
    /**
     * 记录玩家兑换日志
     */
    public static Set<String> getAllExchangeLog(final Integer type) {
        Jedis jedis = null;
        final String key = "m3g_activity_exchange_" + type; 
        try {
            jedis = getInstance();
            if (jedis != null) {
                return jedis.zrevrange(key, 0,-1);
            }
        } catch (Exception e) {
        } finally {
            if (jedis != null) {
                freeInstance(jedis);
            }
        }
        return null;
    }
    
    
    /**
     * 获取游戏中心版本信息
     */
    public static Map<String,String> getVersion(int appId,int platform) {
        Jedis jedis = null;
        final String key = "game_center_version_" + appId + "_" + platform;
        try {
            jedis = getInstance();
            if (jedis != null) {
                return jedis.hgetAll(key);
            }
        } catch (Exception e) {
        } finally {
            if (jedis != null) {
                freeInstance(jedis);
            }
        }
        return null;
    }
    
    /**
     * 获取游戏中心版本信息
     */
    public static String getGameList(int appId,int platform) {
        Jedis jedis = null;
        final String key = "game_center_game_list_" + appId + "_" + platform;
        try {
            jedis = getInstance();
            if (jedis != null) {
                return jedis.get(key);
            }
        } catch (Exception e) {
        } finally {
            if (jedis != null) {
                freeInstance(jedis);
            }
        }
        return null;
    }
    
    

}

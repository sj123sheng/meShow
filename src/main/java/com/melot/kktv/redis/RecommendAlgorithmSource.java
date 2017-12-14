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

import com.melot.kktv.util.DateUtil;
import com.melot.kktv.util.redis.RedisConfigHelper;
import org.apache.commons.lang.StringUtils;
import redis.clients.jedis.Jedis;

/**
 * Title: GameRankingSource
 * <p>
 * Description: 推荐算法缓存
 * </p>
 * 
 * @author 姚国平<a href="mailto:guoping.yao@melot.cn">
 * @version V1.0
 * @since 2015-8-26 下午2:04:32
 */
public class RecommendAlgorithmSource {

    private static final String SOURCE_NAME = "RecommendAlgorithmSource";

    /**
     * 当前的推荐算法 (A-大数据算法 B-现在的推荐算法 C-按照等级和热度的推荐算法)
     */
    public static final String CURRENT_RECOMMEND_ALGORITHM = "current_recommend_algorithm";

    /**
     * 用户的推荐算法前缀
     */
    private static final String RECOMMEND_ALGORITHM_PREFEX = "recommend_algorithm_";

    /**
     * 推荐主播缓存(最原始的推荐主播列表 即直接按照S\A\B\C\D排序 等级一样按照热度排序)
     */
    public static final String SIMPLE_RECOMMENDED_ROOM_KEY = "simple_recommended_room_key";

    /**
     * 统计每种推荐算法的用户列表
     */
    public static final String RECOMMEND_ALGORITHM_KEY = "recommend_algorithm_%s_%s";

    private static Jedis getInstance() {
        return RedisConfigHelper.getJedis(SOURCE_NAME);
    }

    private static void freeInstance(Jedis jedis) {
        RedisConfigHelper.returnJedis(SOURCE_NAME, jedis, false);
    }
    
    /**
     * 获取用户的推荐算法 (A-大数据算法 B-现在的推荐算法 C-按照等级和热度的推荐算法)
     */
    public static String getUserRecommendAlgorithm(int userId) {
        Jedis jedis = null;
        String key = RECOMMEND_ALGORITHM_PREFEX + userId;
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

    /**
     * 设置用户的推荐算法 返回用户的推荐算法
     */
    public static String setUserRecommendAlgorithm(int userId) {
        Jedis jedis = null;
        String key = RECOMMEND_ALGORITHM_PREFEX + userId;
        try {
            jedis = getInstance();
            if (jedis != null) {

                String userRecommendAlgorithm = getNextRecommendAlgorithm();
                jedis.set(key, userRecommendAlgorithm);

                statisticsRecommendAlgorithm(userId, userRecommendAlgorithm, jedis);

                return userRecommendAlgorithm;
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
     * 获取当前的推荐算法
     */
    public static String getCurrentRecommendAlgorithm() {
        Jedis jedis = null;
        String key = CURRENT_RECOMMEND_ALGORITHM;
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

    /**
     * 设置当前的推荐算法
     */
    public static void setCurrentRecommendAlgorithm(String algorithm) {
        Jedis jedis = null;
        String key = CURRENT_RECOMMEND_ALGORITHM;
        try {
            jedis = getInstance();
            if (jedis != null) {
                jedis.set(key, algorithm);
            }
        } catch (Exception e) {
        } finally {
            if (jedis != null) {
                freeInstance(jedis);
            }
        }
    }

    /**
     * 获取下一次的推荐算法 同时更新当前的推荐算法
     */
    public static String getNextRecommendAlgorithm() {

        String currentRecommendAlgorithm = getCurrentRecommendAlgorithm();
        String nextRecommendAlgorithm = "";

        if(StringUtils.isEmpty(currentRecommendAlgorithm)) {

            nextRecommendAlgorithm = "A";
        }else if(currentRecommendAlgorithm.equals("A")) {

            nextRecommendAlgorithm = "B";
        }else if(currentRecommendAlgorithm.equals("B")) {

            nextRecommendAlgorithm = "C";
        }else if(currentRecommendAlgorithm.equals("C")) {

            nextRecommendAlgorithm = "A";
        }
        setCurrentRecommendAlgorithm(nextRecommendAlgorithm);

        return nextRecommendAlgorithm;
    }

    /**
     * 统计每种推荐算法的用户列表
     */
    public static void statisticsRecommendAlgorithm(int userId, String algorithm, Jedis jedis) {

        String key = String.format(RECOMMEND_ALGORITHM_KEY, algorithm, DateUtil.getDateName());
        try {

            if (jedis != null) {

                jedis.sadd(key, String.valueOf(userId));
                jedis.expire(key, 7 * 24 * 3600);
            }
        } catch (Exception e) {
        }
    }

}

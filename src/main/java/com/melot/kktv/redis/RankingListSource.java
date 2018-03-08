/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kktv.redis;

import com.melot.kktv.util.redis.RedisConfigHelper;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

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
public class RankingListSource {

	private static Logger logger = Logger.getLogger(RankingListSource.class);

    private static final String SOURCE_NAME = "RankingList";

    private static Jedis getInstance() {
        return RedisConfigHelper.getJedis(SOURCE_NAME);
    }

    private static void freeInstance(Jedis jedis) {
        RedisConfigHelper.returnJedis(SOURCE_NAME, jedis, false);
    }

	public static void hset(String key, String field, String value) {
        Jedis jedis = null;
        try {
            jedis = getInstance();
            jedis.hset(key, field, value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(jedis!=null) {
                freeInstance(jedis);
            }
        }
	}

}

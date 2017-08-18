package com.melot.kktv.redis;

import com.melot.kktv.util.redis.RedisConfigHelper;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

import java.util.Map;

public class GiftRecordSource {
	
	private static Logger logger = Logger.getLogger(GiftRecordSource.class);
	
	private static final String SOURCE_NAME = "GiftRecord";

	// 头条礼物主播半小时排行榜
	private static final String HEADLINE_RANK = "headline_rank_";
	// 总榜
	private static final String HEADLINE_TOTAL = "headline_total";

    // 阳光置顶主播半小时排行榜
    private static final String HEADLINE_SUNSHINE_RANK = "headline_sunshine_rank_";

    // 阳光置顶总榜
    private static final String HEADLINE_SUNSHINE_TOTAL = "headline_sunshine_total";
	
	// 主播收入队列
	private static final String ACTOR_INCOME_QUEUE = "actor_income_to_pg";

    // 未领取新手礼包的h5新注册用户列表
    private static final String SHAREVAULT_NEW_USER = "shareVault:new_user";
	
	private static Jedis getInstance() {
		return RedisConfigHelper.getJedis(SOURCE_NAME);
	}
	
	private static void freeInstance(Jedis jedis) {
		RedisConfigHelper.returnJedis(SOURCE_NAME, jedis, false);
	}
	
	/**
	 * 获取上头条半小时榜单
	 * 
	 * @param dateId 20140827
	 * @param timeField 09000930
	 * @return String
	 */
	public static String getHeadlineRank(String dateId, String timeField) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			return jedis.hget(HEADLINE_RANK + dateId, timeField);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
		return null;
	}
	
	/**
	 * 获得总绑
	 * @return
	 */
	public static Map<String, String> getHeadlineTotal() {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			return jedis.hgetAll(HEADLINE_TOTAL);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
		return null;
	}
    
    /**
     * 获得阳光置顶主播半小时排行榜
     * 
     * @param dateId 20140827
     * @param timeField 09000930
     * @return String
     */
    public static String getHeadlineSunshineRank(String dateId, String timeField) {
        Jedis jedis = null;
        try {
            jedis = getInstance();
            return jedis.hget(HEADLINE_SUNSHINE_RANK + dateId, timeField);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                freeInstance(jedis);
            }
        }
        return null;
    }
    
    /**
     * 获得阳光置顶总榜
     * @return
     */
    public static Map<String, String> getHeadlineSunshineTotal() {
        Jedis jedis = null;
        try {
            jedis = getInstance();
            return jedis.hgetAll(HEADLINE_SUNSHINE_TOTAL);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                freeInstance(jedis);
            }
        }
        return null;
    }
	
	/**
	 * 添加主播收入处理队列
	 * @param value
	 */
	public static void pushActorIncomeQueue(String value) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			jedis.rpush(ACTOR_INCOME_QUEUE, value);
		} catch (Exception e) {
			logger.error("fail to rpush queue actor_income_to_pg , value " + value, e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
	}

    /**
     * 查询该用户是否领取过新手礼包 true-未领取过 false-领取过
     * @return
     */
    public static boolean haveReceivedNoviceGift(Integer userId) {
        Jedis jedis = null;
        try {
            jedis = getInstance();
            return jedis.sismember(SHAREVAULT_NEW_USER, userId.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                freeInstance(jedis);
            }
        }
        return false;
    }

    /**
     * 从未领新手礼包缓存队列中删除（领取新手礼包)
     * @return
     */
    public static void removeNoviceGift(Integer userId) {
        Jedis jedis = null;
        try {
            jedis = getInstance();
            jedis.srem(SHAREVAULT_NEW_USER, userId.toString());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                freeInstance(jedis);
            }
        }
    }
	
}

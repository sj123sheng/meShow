package com.melot.kkcx.redis;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.melot.kktv.util.redis.RedisConfigHelper;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

public class UserRelationSource {
	
	private static final String SOURCE_NAME = "UserRelation";
	
	private static final String UPDATEFOLLOWRECORD_LIST = "updateFollowRecord_list";
	
	private static final String FOLLOWS_SUFFIX = "_follows";
	private static final String FANS_SUFFIX = "_fans";
	
	private static Jedis getInstance() {
		return RedisConfigHelper.getJedis(SOURCE_NAME);
	}
	
	private static void freeInstance(Jedis jedis) {
		RedisConfigHelper.returnJedis(SOURCE_NAME, jedis, false);
	}

	public static Set<String> getFollowList(String userId, long start, long end) {
		Jedis jedis = null;
		Set<String> followSet = null;
		try {
			jedis = getInstance();
			followSet = jedis.zrevrange(userId+FOLLOWS_SUFFIX, start, end);
			return followSet;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return null;
	}
	
	public static Long countFollows(String userId) {
		Jedis jedis = null;
		Long count = null;
		try {
			jedis = getInstance();
			count = jedis.zcard(userId+FOLLOWS_SUFFIX);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return count;
	}
	
	public static Set<String> getFanList(String userId, long start, long end) {
		Jedis jedis = null;
		Set<String> fanSet = null;
		try {
			jedis = getInstance();
			fanSet = jedis.zrevrange(userId+FANS_SUFFIX, start, end);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return fanSet;
	}
	
	public static Long countFans(String userId) {
		Jedis jedis = null;
		Long count = null;
		try {
			jedis = getInstance();
			count = jedis.zcard(userId+FANS_SUFFIX);
			return count;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return count;
	}
	
	public static void addFollow(String userId, String followId) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			Transaction trans = jedis.multi();
			double score = (double)(System.currentTimeMillis()/1000);
			trans.zadd(userId+FOLLOWS_SUFFIX, score, followId);
			trans.zadd(followId+FANS_SUFFIX, score, userId);
			trans.exec();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
	}
	
	public static void remFollow(String userId, String followId) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			Transaction trans = jedis.multi();
			trans.zrem(userId+FOLLOWS_SUFFIX, followId);
			trans.zrem(followId+FANS_SUFFIX, userId);
			trans.exec();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
	}
	
	public static boolean isFollowed(String userId, String followId) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			if(jedis.zrevrank(userId+FOLLOWS_SUFFIX, followId)!=null) return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return false;
	}
	
	public static void pushToRelationOracle(String value) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			jedis.rpush(UPDATEFOLLOWRECORD_LIST, value);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
	}

	private static final String ACTOR_SCORE_KEY = "ActorScore";
	
	public static void setHotData(String key, Map<String, String> hotData, int expireTime) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			jedis.hmset(key, hotData);
			jedis.expire(key, expireTime);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
	}
	
	public static String getValue(String key) {
		Jedis jedis = null;
        try {
            jedis = getInstance();
            return jedis.get(key);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(jedis!=null) {
                freeInstance(jedis);
            }
        }
        return null;
	}
	
	public static String getHotFieldValue(String key, String field) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			return jedis.hget(key, field);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return null;
	}
	
	public static List<String> getScoreByIds(String[] ids) {
    	Jedis jedis = null;
    	try {
    		jedis = getInstance();
    		return jedis.hmget(ACTOR_SCORE_KEY, ids);
    	} catch (Exception e) {
    		e.printStackTrace();
    	} finally {
    		if (jedis != null) {
    			freeInstance(jedis);
    		}
    	}
    	return null;
    }
    
    public static boolean isKeyExist(String key) {
    	Jedis jedis = null;
    	try {
    		jedis = getInstance();
    		return jedis.exists(key);
    	} catch (Exception e) {
    		e.printStackTrace();
    	} finally {
    		if (jedis != null) {
    			freeInstance(jedis);
    		}
    	}
    	return false;
    }
    
    public static boolean removeKey(String key) {
    	Jedis jedis = null;
    	try {
    		jedis = getInstance();
    		return jedis.del(key) > 0;
    	} catch (Exception e) {
    		e.printStackTrace();
    	} finally {
    		if (jedis != null) {
    			freeInstance(jedis);
    		}
    	}
    	return false;
    }
}

package com.melot.kktv.redis;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.melot.kktv.util.redis.RedisConfigHelper;

import redis.clients.jedis.Jedis;

public class PrivateLetterSource {
	
	private static final String SOURCE_NAME = "HotData";
	
	/** 私信置顶会话*/
	private static final String PRIVATE_LETTER_TOP_SESSION = "private_letter_top_session_%s";
	
	/** 私信普通会话*/
	private static final String PRIVATE_LETTER_SESSION = "private_letter_session_%s";
	
	private static Jedis getInstance() {
		return RedisConfigHelper.getJedis(SOURCE_NAME);
	}
	
	private static void freeInstance(Jedis jedis) {
		RedisConfigHelper.returnJedis(SOURCE_NAME, jedis, false);
	}
	
	
	/**
	 * 添加私信置顶会话
	 * @param userId
	 * @param value
	 * @param unixTime
	 */
    public static void setTopSession(String userId, String value) {
    	if (StringUtils.isBlank(value)) return;
    	Jedis jedis = null;
        try {
        	jedis = getInstance();
        	String key = String.format(PRIVATE_LETTER_SESSION, userId);
        	// 验证是否普通会话
        	Double zscore = jedis.zscore(key, value);
            if (zscore != null) {
            	// 删除普通会话
            	jedis.zrem(key, value);
            }else{
            	zscore = new Double(System.currentTimeMillis());
            }
            // 设置置顶会话score
        	key = String.format(PRIVATE_LETTER_TOP_SESSION, userId);
            jedis.zadd(key, zscore, value);
            jedis.expireAt(key, getSeconds(30));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(jedis!=null) {
                freeInstance(jedis);
            }
        }
    }
    
    
	/**
	 * 重置私信置顶会话列表或普通会话列表
	 * @param userId
	 * @param value
	 */
    public static void refreshSession(String userId, String value) {
    	if (StringUtils.isBlank(value)) return;
    	Jedis jedis = null;
    	String key = null;
    	long currentTime = System.currentTimeMillis();
        try {
            jedis = getInstance();
            // 设置置顶会话score
            key = String.format(PRIVATE_LETTER_TOP_SESSION, userId);
            if (jedis.zscore(key, value) != null) {
            	jedis.zadd(key, currentTime, value);
	            jedis.expireAt(key, getSeconds(30));
            }else{
	            // 设置普通会话score
	            key = String.format(PRIVATE_LETTER_SESSION, userId);
	            jedis.zadd(key, currentTime, value);
	            jedis.expireAt(key, getSeconds(30));
        	}
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(jedis!=null) {
                freeInstance(jedis);
            }
        }
    }
    
    /**
     * 获取私信置顶会话列表
     * @param userId
     * @param end
     * @return
     */
    public static LinkedList<Integer> getTopSession(String userId,int end) {
    	LinkedList<Integer> list = new LinkedList<Integer>();
    	Jedis jedis = null;
        try {
        	jedis = getInstance();
        	String redisKey = String.format(PRIVATE_LETTER_TOP_SESSION, userId);
        	removeExpireSession(redisKey);
            long size = jedis.zcard(redisKey);
            Set<String> set = jedis.zrevrange(redisKey, 0, size);
            if (set != null && set.size() > 0) {
            	for (String value : set) {
					list.add(Integer.valueOf(value));
				}
			}
            jedis.expireAt(redisKey, getSeconds(30));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(jedis!=null) {
                freeInstance(jedis);
            }
        }
        return list;
    }
    
    /**
     * 获取普通会话列表
     * @param userId
     * @param end
     * @return
     */
    public static LinkedList<Integer> getGeneralSession(String userId,int end) {
    	LinkedList<Integer> list = new LinkedList<Integer>();
    	Jedis jedis = null;
        try {
        	jedis = getInstance();
        	String redisKey = String.format(PRIVATE_LETTER_SESSION, userId);
        	removeExpireSession(redisKey);
            long size = jedis.zcard(redisKey);
            Set<String> set = jedis.zrevrange(redisKey, 0, size);
            if (set != null && set.size() > 0) {
            	for (String value : set) {
					list.add(Integer.valueOf(value));
				}
			}
            jedis.expireAt(redisKey, getSeconds(30));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(jedis!=null) {
                freeInstance(jedis);
            }
        }
        return list;
    }
   
    /**
     * 删除过期数据
     * @param key
     * @return
     */
    public static void removeExpireSession(String key){
    	Jedis jedis = null;
        try {
        	jedis = getInstance();
        	// 算出30天的时间
        	double maxScore  = new Double(System.currentTimeMillis()/1000 - 30*24*60*60)*1000;
        	jedis.zremrangeByScore(key, 0d, maxScore);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(jedis!=null) {
                freeInstance(jedis);
            }
        }
    }
    
    
    /**
     * 删除私信置顶会话列表，并添加进普通会话
     * @param userId
     * @param value
     * @return
     */
    public static long removeTopSession(String userId, String value) {
    	long size = 0;
    	Jedis jedis = null;
        try {
        	jedis = getInstance();
        	// 设置置顶会话score
        	String key = String.format(PRIVATE_LETTER_TOP_SESSION, userId);
        	Double zscore = jedis.zscore(key, value);
            if (zscore != null) {
            	jedis.zrem(key, value);
	            // 设置普通会话score
	            key = String.format(PRIVATE_LETTER_SESSION, userId);
	            jedis.zadd(key, zscore, value);
	            jedis.expireAt(key, getSeconds(30));
        	}
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(jedis!=null) {
                freeInstance(jedis);
            }
        }
        return size;
    }
     
    /**
     * 删除私信会话列表
     * @param userId
     * @return
     */
    public static long removeSession(String userId, String value) {
    	long size = 0;
    	Jedis jedis = null;
        try {
        	jedis = getInstance();
        	String key = String.format(PRIVATE_LETTER_TOP_SESSION, userId);
            size = jedis.zrem(key, value);
            
            key = String.format(PRIVATE_LETTER_SESSION, userId);
            size = jedis.zrem(key, value);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(jedis!=null) {
                freeInstance(jedis);
            }
        }
        return size;
    }
    
    
    /**
	 * 获取多少天后零点时间
	 * 
	 * @return
	 */
	public static long getSeconds(long day) {
		day = day*24*60*60;
		try {
			Date date = new Date();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");      
		    String dateStr = dateFormat.format(date);
		    long today = dateFormat.parse(dateStr).getTime()/1000;
			return today + day;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return day;
	}
	
}

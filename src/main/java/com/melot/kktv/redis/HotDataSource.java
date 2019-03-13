package com.melot.kktv.redis;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import com.melot.kkcx.model.HourRankInfo;
import com.melot.kktv.util.RankingEnum;
import com.melot.kktv.util.redis.RedisConfigHelper;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

public class HotDataSource {

	private static Logger logger = Logger.getLogger(HotDataSource.class);
	
	private static final String SOURCE_NAME = "HotData";
	
	private static Jedis getInstance() {
		return RedisConfigHelper.getJedis(SOURCE_NAME);
	}
	
	private static void freeInstance(Jedis jedis) {
		RedisConfigHelper.returnJedis(SOURCE_NAME, jedis, false);
	}
	
	public static void del(String key) {
        Jedis jedis = null;
        try {
            jedis = getInstance();
            jedis.del(key);
        } catch (Exception e) {
            logger.error("HotDataSource.del(" + "key:" + key + ") execute exception.", e);
        } finally {
            if(jedis!=null) {
                freeInstance(jedis);
            }
        }
    }

	public static Map<String, String> getHotData(String key) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			Map<String, String> data = new HashMap<String, String>();
			Set<String> fieldList = jedis.hkeys(key);
			for (Iterator<String> iter = fieldList.iterator(); iter.hasNext();) {
				String field = iter.next();
				String value = jedis.hget(key, field);
				data.put(field, value);
			}
			return data;
		} catch (Exception e) {
			logger.error("HotDataSource.getHotData(" + "key:" + key + ") execute exception.", e);
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
			logger.error("HotDataSource.getHotFieldValue(" + "key:" + key + "field:" + field + ") execute exception.", e);
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return null;
	}

	public static void setHotFieldValue(String key, String field, String value) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			jedis.hset(key, field, value);
		} catch (Exception e) {
			logger.error("HotDataSource.setHotFieldValue(" + "key:" + key + "field:" + field + "value:" + value + ") execute exception.", e);
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
	}
	
	public static void setHotFieldValue(String key, String field, String value, int seconds) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			jedis.hset(key, field, value);
			jedis.expire(key, seconds);
		} catch (Exception e) {
			logger.error("HotDataSource.setHotFieldValue(" + "key:" + key + "field:" + field + "value:" + value + "seconds:" + seconds + ") execute exception.", e);
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
	}
	
	public static String[] getHotFieldValues(String key, String[] fields) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			String[] values = new String[fields.length];
			for(int i=0; i<fields.length; i++){
				values[i] = jedis.hget(key, fields[i]);
			}
			return values; 
		} catch (Exception e) {
			logger.error("HotDataSource.getHotFieldValues(" + "key:" + key + "fields:" + fields + ") execute exception.", e);
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return null;
	}
	
	public static void setHotData(String key, Map<String, String> hotData, int expireTime) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			jedis.hmset(key, hotData);
			jedis.expire(key, expireTime);
		} catch (Exception e) {
			logger.error("HotDataSource.setHotData(" + "key:" + key + "hotData:" + hotData + "expireTime:" + expireTime + ") execute exception.", e);
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
	}
	
	public static long incHotFieldValue(String key, String field, int incValue) {
		long result = -1l;
		Jedis jedis = null;
		try {
			jedis = getInstance();
			result = jedis.hincrBy(key, field, incValue);
		} catch (Exception e) {
			logger.error("HotDataSource.incHotFieldValue(" + "key:" + key + "field:" + field + "incValue:" + incValue + ") execute exception.", e);
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return result;
	}
	
	public static boolean incHotData(String key, String[] fields, int[] incValues, int expireTime) {
		if(fields.length != incValues.length)
			return false;
		Jedis jedis = null;
		try {
			jedis = getInstance();
			for(int i=0; i<fields.length; i++){
				jedis.hincrBy(key, fields[i], incValues[i]);
			}
			jedis.expire(key, expireTime);
			return true;
		} catch (Exception e) {
			logger.error("HotDataSource.incHotData(" + "key:" + key + "fields:" + fields + "incValues:" + incValues + "expireTime:" + expireTime + ") execute exception.", e);
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return false;
	}
	
	public static void delHotData(String key, String[] fields){
		Jedis jedis = null;
		try {
			jedis = getInstance();
			for(int i=0; i<fields.length; i++){
				jedis.hdel(key, fields[i]);
			}
		} catch (Exception e) {
			logger.error("HotDataSource.delHotData(" + "key:" + key + "fields:" + fields + ") execute exception.", e);
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
	}

	public static void setTempData(String key, String value, long expireTime) {
		Jedis jedis = null;
			try {
				jedis = getInstance();
				jedis.sadd(key, value);
				if (jedis.ttl(key) == -1) {
					jedis.expireAt(key, expireTime);
				}
			} catch (Exception e) {
				logger.error("HotDataSource.setTempData(" + "key:" + key + "value:" + value + "expireTime:" + expireTime + ") execute exception.", e);
			} finally {
				if(jedis!=null) {
					freeInstance(jedis);
				}
			}
	}

	public static boolean hasTempData(String key, String userId) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			return jedis.sismember(key, userId);
		} catch (Exception e) {
			logger.error("HotDataSource.hasTempData(" + "key:" + key + "userId:" + userId + ") execute exception.", e);
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return false;
	}

	public static boolean setTempDataString(String key, String value, int seconds) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			jedis.set(key, value);
			if (seconds > 0) {
			    jedis.expire(key, seconds);
			}
			return true;
		} catch (Exception e) {
			logger.error("HotDataSource.setTempDataString(" + "key:" + key + "value:" + value + "seconds:" + seconds + ") execute exception.", e);
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return false;
	}

    public static double incTempDataString(String key, double value, int seconds) {
        Double result = null;
        Jedis jedis = null;
        try {
            jedis = getInstance();
            result = jedis.incrByFloat(key, value);
            if (seconds > 0) {
                jedis.expire(key, seconds);
            }
        } catch (Exception e) {
            logger.error("HotDataSource.incTempDataString(" + "key:" + key + "value:" + value + "seconds:" + seconds + ") execute exception.", e);
        } finally {
            if(jedis!=null) {
                freeInstance(jedis);
            }
        }
        return result == null ? 0 : result.doubleValue();
    }
	
	public static String getTempDataString(String key) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			return jedis.get(key);
		} catch (Exception e) {
			logger.error("HotDataSource.getTempDataString(" + "key:" + key + ") execute exception.", e);
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return null;
	}
    
    public static void delTempData(String key) {
        Jedis jedis = null;
        try {
            jedis = getInstance();
            jedis.del(key);
        } catch (Exception e) {
            logger.error("HotDataSource.delTempData(" + "key:" + key + ") execute exception.", e);
        } finally {
            if(jedis!=null) {
                freeInstance(jedis);
            }
        }
    }

    public static void addSortedSet(String key, List<String> list, int seconds) {
    	if (list == null || list.size() == 0) return;
    	Jedis jedis = null;
        try {
            jedis = getInstance();
            if (jedis.exists(key))
            	jedis.del(key);
            for (int i = 0; i < list.size(); i++)
            	jedis.zadd(key, i+1d, list.get(i));
            jedis.expire(key, seconds);
        } catch (Exception e) {
            logger.error("HotDataSource.addSortedSet(" + "key:" + key + "list:" + list + "seconds:" + seconds + ") execute exception.", e);
        } finally {
            if(jedis!=null) {
                freeInstance(jedis);
            }
        }
    }
    
    public static int countSortedSet(String key) {
    	int count = 0;
    	Jedis jedis = null;
        try {
            jedis = getInstance();
        	Long value = jedis.zcard(key);
        	if (value != null) {
        		count = value.intValue();
        	}
        } catch (Exception e) {
            logger.error("HotDataSource.countSortedSet(" + "key:" + key + ") execute exception.", e);
        } finally {
            if(jedis!=null) {
                freeInstance(jedis);
            }
        }
        return count;
    }
    
    public static Set<String> rangeSortedSet(String key, int start, int end) {
    	Set<String> set = null;
    	Jedis jedis = null;
        try {
            jedis = getInstance();
            if (jedis.exists(key))
            	set = jedis.zrange(key, start, end);
        } catch (Exception e) {
            logger.error("HotDataSource.rangeSortedSet(" + "key:" + key + "start:" + start + "end:" + end + ") execute exception.", e);
        } finally {
            if(jedis!=null) {
                freeInstance(jedis);
            }
        }
        return set;
    }
    
    public static long sadd(String key, int seconds, String ... members) {
        long size = 0;
        Jedis jedis = null;
        try {
            jedis = getInstance();
            size = jedis.sadd(key, members);
            
            if (seconds > 0) {
                if (jedis.ttl(key) == -1) {
                    jedis.expire(key, seconds);
                }
            } else {
                jedis.persist(key);
            }
        } catch (Exception e) {
            logger.error("HotDataSource.sadd(" + "key:" + key + "seconds:" + seconds + "members:" + members + ") execute exception.", e);
        } finally {
            if(jedis!=null) {
                freeInstance(jedis);
            }
        }
        return size;
    }
    
    public static void srem(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = getInstance();
            if (jedis.exists(key))
                jedis.srem(key, value);
        } catch (Exception e) {
            logger.error("HotDataSource.srem(" + "key:" + key + "value:" + value + ") execute exception.", e);
        } finally {
            if(jedis!=null) {
                freeInstance(jedis);
            }
        }
    }
    
    public static List<String> srandmember(String key, int count) {
        List<String> set = null;
        Jedis jedis = null;
        try {
            jedis = getInstance();
            if (jedis.exists(key))
                set = jedis.srandmember(key, count);
        } catch (Exception e) {
            logger.error("HotDataSource.srandmember(" + "key:" + key + "count:" + count + ") execute exception.", e);
        } finally {
            if(jedis!=null) {
                freeInstance(jedis);
            }
        }
        return set;
    }

    public static boolean exists(String key) {
        Jedis jedis = null;
        try {
            jedis = getInstance();
            boolean bool = jedis.exists(key).booleanValue();

            return bool;
        } catch (Exception e) {
            logger.error("HotDataSource.exists(" + "key:" + key + ") execute exception.", e);
        } finally {
            if (jedis != null) {
                freeInstance(jedis);
            }
        }
        return false;
    }
    
    public static Map<String, Double> getRevRangeWithScore(String key) {
    	Jedis jedis = null;
    	Map<String, Double> sortMap = new LinkedHashMap<String, Double>();
        try {
            jedis = getInstance();
            Set<Tuple> Tuple = jedis.zrevrangeWithScores(key, 0, 19);
            for (Tuple tuple : Tuple) {
            	sortMap.put(tuple.getElement(), tuple.getScore());
            }
            return sortMap;
        } catch (Exception e) {
            logger.error("HotDataSource.getRevRangeWithScore(" + "key:" + key + ") execute exception.", e);
        } finally {
            if (jedis != null) {
                freeInstance(jedis);
            }
        }
        return null;
    }
    
    public static HourRankInfo getHourRankInfo(int roomId) {
        Jedis jedis = null;
        HourRankInfo result = null;
        try {
            jedis = getInstance();
            String key = RankingEnum.getCollection(RankingEnum.RANKING_TYPE_HOUR, RankingEnum.RANKING_THIS_HOUR);
            Long rank = jedis.zrevrank(key, String.valueOf(roomId));
            if (rank != null) {
                result = new HourRankInfo();
                int position = rank.intValue();
                result.setPosition(position);
                result.setRoomId(roomId);
                int prePosition = position > 0 ? position - 1 : position;
                Set<Tuple> set = jedis.zrevrangeWithScores(key, prePosition, position);
                if (CollectionUtils.isNotEmpty(set)) {
                    for (Tuple tuple : set) {
                        if (roomId == Integer.parseInt(tuple.getElement())) {
                            result.setScore(tuple.getScore());
                        } else {
                            result.setPreRoomId(Integer.parseInt(tuple.getElement()));
                            result.setPreScore(tuple.getScore());
                            result.setPrePosition(prePosition);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("HotDataSource.getHourRankInfo(" + roomId + ") execute exception.", e);
        } finally {
            if (jedis != null) {
                freeInstance(jedis);
            }
        }
        
        return result;
    }
}

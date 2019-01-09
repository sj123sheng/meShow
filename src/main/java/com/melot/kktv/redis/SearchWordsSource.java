package com.melot.kktv.redis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.melot.kktv.util.redis.RedisConfigHelper;

import redis.clients.jedis.Jedis;

public class SearchWordsSource {
	
    private static Logger logger = Logger.getLogger(SearchWordsSource.class);
	
	private static final String SOURCE_NAME = "SearchWords";
	
	//key 所有热门搜索词
	private static final String SEARCHWORDS = "searchWords";
	
	//key 热门关键字
	private static final String HOTKEYWORDS = "hotWords";
	
	//key格式 热门关键字搜索结果
	private static final String HOT_SEARCHRESULT = "searchResult_%s";
	
	//key格式 热门关键字搜索结果——分页
    private static final String HOT_SEARCHRESULT_PAGE = "searchResult_page_%s_%s";
	
	private static Jedis getInstance() {
		return RedisConfigHelper.getJedis(SOURCE_NAME);
	}
	
	private static void freeInstance(Jedis jedis) {
		RedisConfigHelper.returnJedis(SOURCE_NAME, jedis, false);
	}
	
	/**
	 * 获得热门关键字列表 
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
    public static List<String> getHotKeywords(int count) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			List<String> members = jedis.srandmember(HOTKEYWORDS, count);
			if (members != null && members.size() > 0) {
				return new ArrayList(new HashSet(members));
			}
		} catch (Exception e) {
			logger.error("SearchWordsSource.getHotKeywords() execute exception", e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
		return null;
	}
	
	/**
	 * 设置热门关键字(测试用)
	 * @param searchWord
	 * @return
	 */
	public static boolean setHotKeywords(String searchWord) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			jedis.sadd(HOTKEYWORDS, searchWord);
			return true;
		} catch(Exception e) {
            logger.error("SearchWordsSource.setHotKeywords(" + searchWord + ") execute exception", e);
		} finally {
			if(jedis != null) {
				freeInstance(jedis);
			}
		}
		return false;
	}
	
	/**
	 * 增加搜索次数
	 * @param searchWord
	 * @return
	 */
	public static boolean incScore(String searchWord) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
		    jedis.zincrby(SEARCHWORDS, 1, searchWord);
		    return true;
		} catch(Exception e) {
            logger.error("SearchWordsSource.incScore(" + searchWord + ") execute exception", e);
		} finally {
			if(jedis != null) {
				freeInstance(jedis);
			}
		}
		return false;
	}
	
	/**
	 * 判断是否是热门关键字
	 * @param searchWord
	 * @return
	 */
	public static boolean isHotWord(String searchWord) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			return jedis.sismember(HOTKEYWORDS, searchWord);
		} catch(Exception e) {
            logger.error("SearchWordsSource.isHotWord(" + searchWord + ") execute exception", e);
		} finally {
			if(jedis != null) {
				freeInstance(jedis);
			}
		}
		return false;
	}
	
	/**
	 * 获得搜索结果
	 * @param searchWord
	 * @return
	 */
	public static String getSearchResult(String searchWord) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			return jedis.get(String.format(HOT_SEARCHRESULT, searchWord));
		} catch(Exception e) {
            logger.error("SearchWordsSource.getSearchResult(" + searchWord + ") execute exception", e);
		} finally {
			if(jedis != null) {
				freeInstance(jedis);
			}
		}
		return null;
	}
	
	/**
	 * 添加热门关键字搜索结果到redis中
	 * @param searchWord
	 * @param searchResult
	 * @return
	 */
	public static boolean setSearchResult(String searchWord, String searchResult) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			jedis.setex(String.format(HOT_SEARCHRESULT,searchWord), 3600, searchResult);
			return true;
		} catch(Exception e) {
            logger.error("SearchWordsSource.setSearchResult(" + searchWord + ", " + searchResult + ") execute exception", e);
		} finally {
			if(jedis != null) {
				freeInstance(jedis);
			}
		}
		return false;
	}
	
	/**
	 * 添加关键字搜索结果到redis中,分页
	 * @param searchWord
	 * @param searchResult
	 */
	public static boolean setSearchResultPage(String searchWord,int appId, List<String> searchResult) {
	    Jedis jedis = null;
	    try {
            jedis = getInstance();
            String key = String.format(HOT_SEARCHRESULT_PAGE, searchWord,appId);
            for (int i = 0; i < searchResult.size(); i++) {
                jedis.zadd(key, i + 1D, searchResult.get(i));
            }
            jedis.expire(key, 60);
            logger.info("SearchWordsSource.setSearchResult(" + searchWord+","+appId + ", " + searchResult + ")");
            return true;
        } catch (Exception e) {
            logger.error("SearchWordsSource.setSearchResult(" + searchWord +","+appId+ ", " + searchResult + ") execute exception", e);
        } finally {
            if (jedis != null) {
                freeInstance(jedis);
            }            
        }
	    return false;
	}
	
	/**
     * 获得搜索结果，分页
     * @param searchWord
     * @return
     */
	public static Set<String> getSearchResultPage(String searchWord,int appId, long start, long end) {
	    Set<String> set = null;
        Jedis jedis = null;
        try {
            jedis = getInstance();
            String key = String.format(HOT_SEARCHRESULT_PAGE, searchWord,appId);
            set = jedis.zrange(key, start, end);
        } catch (Exception e) {
            logger.error("SearchWordsSource.getSearchResult(" + searchWord +","+appId+ ") execute exception", e);
        } finally {
            if (jedis != null) {
                freeInstance(jedis);
            }            
        }
        return set;
	}
	
	public static long getSearchResultPageCount(String searchWord,int appId) {
        Jedis jedis = null;
        long count = 0;
        try {
            jedis = getInstance();
            String key = String.format(HOT_SEARCHRESULT_PAGE, searchWord,appId);
            Long tempCount = jedis.zcard(key);
            if (tempCount != null) {
                count = tempCount;
            }
        } catch (Exception e) {
            logger.error("SearchWordsSource.getSearchResult(" + searchWord +","+appId+ ") execute exception", e);
        } finally {
            if (jedis != null) {
                freeInstance(jedis);
            }            
        }
        return count;
	}
	
	public static boolean isExistSearchResultKey(String searchWord,int appId) {
        Jedis jedis = null;
        try {
            jedis = getInstance();
            String key = String.format(HOT_SEARCHRESULT_PAGE, searchWord,appId);
            return jedis.exists(key);
        } catch (Exception e) {
            logger.error("SearchWordsSource.getSearchResult(" + searchWord +","+appId+ ") execute exception", e);
        } finally {
            if (jedis != null) {
                freeInstance(jedis);
            }            
        }
        return false;
	}
	
}

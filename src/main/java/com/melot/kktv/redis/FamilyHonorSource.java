package com.melot.kktv.redis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.kkcore.user.api.UserInfoDetail;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kkcx.service.RoomService;
import com.melot.kktv.util.RankingEnum;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.redis.RedisConfigHelper;
import com.melot.sdk.core.util.MelotBeanFactory;

import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

public class FamilyHonorSource {

	private static Logger logger = Logger.getLogger(FamilyHonorSource.class);

	private static final String SOURCE_NAME = RedisServiceKey.SERVICE_SOURCE_FAMILYHONORCACHE;
    
    /** familyRank_rankType_slotType_familyId **/
    private static final String FAMILYRANKING_KEY_NEW = "familyRank_new_v2_%s_%s_%s";

    /** familyRank_rankType_slotType_familyId **/
    private static final String FAMILYRANKING_KEY_REDIS_CACHE = "familyrank_redis_cache_v2_%s_%s_%s";
	
	private static Jedis getInstance() {
		return RedisConfigHelper.getJedis(SOURCE_NAME);
	}
	
	private static void freeInstance(Jedis jedis, boolean errorFlag) {
		RedisConfigHelper.returnJedis(SOURCE_NAME, jedis, errorFlag);
	}
	
	/**
	 * 获取家族荣誉
	 * @param familyId	家族ID
	 */
	public static Map<String, Long> getFamilyHonor(String familyId) {
		Map<String, Long> result = null;
		
		Jedis jedis = null;
		boolean jedisErrorFlag = true;
		try {
			jedis = getInstance();
			
			result = new HashMap<String, Long>();
			Long consumeTotalRank = jedis.zrevrank(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_CONSUMETOTAL, familyId);
			Double consumeTotalScore = jedis.zscore(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_CONSUMETOTAL, familyId);
			if (consumeTotalRank == null || (consumeTotalScore != null && consumeTotalScore.intValue() == 0))
				consumeTotalRank = -1l;
			result.put(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_CONSUMETOTAL, consumeTotalRank + 1l);
			
			Long medalCountRank = jedis.zrevrank(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_MEDALCOUNT, familyId);
			Double medalCountScore = jedis.zscore(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_MEDALCOUNT, familyId);
			if (medalCountRank == null || (medalCountScore != null && medalCountScore.intValue() == 0))
				medalCountRank = -1l;
			result.put(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_MEDALCOUNT, medalCountRank + 1l);
			
			Long memberCountRank = jedis.zrevrank(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_MEMBERCOUNT, familyId);
			Double memberCountScore = jedis.zscore(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_MEMBERCOUNT, familyId);
			if (memberCountRank == null || (memberCountScore != null && memberCountScore.intValue() == 0))
				memberCountRank = -1l;
			result.put(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_MEMBERCOUNT, memberCountRank + 1l);
			
			Long totalLiveRank = jedis.zrevrank(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_TOTALLIVE, familyId);
			Double totalLiveScore = jedis.zscore(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_TOTALLIVE, familyId);
			if (totalLiveRank == null|| (totalLiveScore != null && totalLiveScore.intValue() == 0))
				totalLiveRank = -1l;
			result.put(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_TOTALLIVE, totalLiveRank + 1l);
			
			Long crownCountRank = jedis.zrevrank(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_CROWNCOUNT, familyId);
			Double crownCountScore = jedis.zscore(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_CROWNCOUNT, familyId);
			if (crownCountRank == null || (crownCountScore != null && crownCountScore.intValue() == 0))
				crownCountRank = -1l;
			result.put(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_CROWNCOUNT, crownCountRank + 1l);
			
			Long diamondCountRank = jedis.zrevrank(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_DIAMONDCOUNT, familyId);
			Double diamondCountScore = jedis.zscore(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_DIAMONDCOUNT, familyId);
			if (diamondCountRank == null || (diamondCountScore != null && diamondCountScore.intValue() == 0))
				diamondCountRank = -1l;
			result.put(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_DIAMONDCOUNT, diamondCountRank + 1l);
			
			Long heartCountlRank = jedis.zrevrank(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_HEARTCOUNT, familyId);
			Double heartCountlScore = jedis.zscore(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_HEARTCOUNT, familyId);
			if (heartCountlRank == null|| (heartCountlScore != null && heartCountlScore.intValue() == 0))
				heartCountlRank = -1l;
			result.put(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_HEARTCOUNT, heartCountlRank + 1l);
			
			jedisErrorFlag = false;
		} catch (Exception e) {
			logger.error("FamilyHonorSource.getFamilyHonor(" + "familyId:" + familyId + ") execute exception.", e);
		} finally {
			if(jedis!=null) {
				freeInstance(jedis, jedisErrorFlag);
			}
		}
		
		return result;
	}
	
	/**
	 * 将消费总额从高到低排列
	 * @author fenggaopan 2015年10月22日 上午11:25:19
	 * @return
	 */
	public static Map<String,Long> getConsumeTotal(Integer count) {
		Map<String,Long> consumeMap = new LinkedHashMap<String,Long>();  
		Jedis jedis = null;
		boolean jedisErrorFlag = true;
		try {
			jedis = getInstance();
			//从降序获取到所有的家族的id集合
			Set<String> familyIds = jedis.zrevrange(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_CONSUMETOTAL,0,count);
			for(String familyId : familyIds) {
				consumeMap.put(familyId,jedis.zscore(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_CONSUMETOTAL,familyId).longValue()) ;
			}
			jedisErrorFlag = false ;
		} catch (Exception e) {
			logger.error("FamilyHonorSource.getConsumeTotal(" + "count:" + count + ") execute exception.", e);
		} finally {
			if(jedis!=null) {
				freeInstance(jedis, jedisErrorFlag);
			}
		}
		return consumeMap ;
	}
	
	/**
	 * 获取勋章数量
	 * @author fenggaopan 2015年10月22日 下午4:17:12
	 * @param count 返回的勋章总数量
	 * @return 返回结果的map
	 */
	public static Map<String,Long> getMedalTotal(Integer count) {
		Map<String,Long> consumeMap = new LinkedHashMap<String,Long>();  
		Jedis jedis = null;
		boolean jedisErrorFlag = true;
		try {
			jedis = getInstance();
			//从降序获取到所有的家族的id集合
			Set<String> familyIds = jedis.zrevrange(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_MEDALCOUNT,0,count);
			for(String familyId : familyIds) {
				consumeMap.put(familyId,jedis.zscore(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_MEDALCOUNT,familyId).longValue()) ;
			}
			jedisErrorFlag = false ;
		} catch (Exception e) {
			logger.error("FamilyHonorSource.getMedalTotal(" + "count:" + count + ") execute exception.", e);
		} finally {
			if(jedis!=null) {
				freeInstance(jedis, jedisErrorFlag);
			}
		}
		return consumeMap ;
	}
	
	/**
	 * 获取超冠主播数量
	 * @author fenggaopan 2015年10月22日 下午4:17:12
	 * @param count 返回的超冠主播数量
	 * @return 返回结果的map
	 */
	public static Map<String,Long> getCrownCountTotal(Integer count) {
		Map<String,Long> consumeMap = new LinkedHashMap<String,Long>();  
		Jedis jedis = null;
		boolean jedisErrorFlag = true;
		try {
			jedis = getInstance();
			//从降序获取到所有的家族的id集合
			Set<String> familyIds = jedis.zrevrange(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_CROWNCOUNT,0,count);
			for(String familyId : familyIds) {
				consumeMap.put(familyId,jedis.zscore(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_CROWNCOUNT,familyId).longValue()) ;
			}
			jedisErrorFlag = false ;
		} catch (Exception e) {
			logger.error("FamilyHonorSource.getCrownCountTotal(" + "count:" + count + ") execute exception.", e);
		} finally {
			if(jedis!=null) {
				freeInstance(jedis, jedisErrorFlag);
			}
		}
		return consumeMap ;
	}

	/**
	 * 获取家族荣誉
	 * @param familyId	家族ID
	 * @param honorType 荣誉类型
	 */
	public static Long getFamilyHonor(String familyId, String honorType) {
		Long result = null;
		
		Jedis jedis = null;
		boolean jedisErrorFlag = true;
		try {
			jedis = getInstance();
			
			result = jedis.zrevrank(honorType, familyId) + 1l;
			
			jedisErrorFlag = false;
		} catch (Exception e) {
			logger.error("FamilyHonorSource.getFamilyHonor(" + "familyId:" + familyId + "honorType:" + honorType + ") execute exception.", e);
		} finally {
			if(jedis!=null) {
				freeInstance(jedis, jedisErrorFlag);
			}
		}
		
		return result;
	}

	/**
	 * 获取家族荣誉
	 * @param familyId	家族ID
	 */
	public static Set<String> getFamilyHonorList(String honorType, int start, int end) {
		Set<String> result = null;
		
		Jedis jedis = null;
		boolean jedisErrorFlag = true;
		try {
			jedis = getInstance();
			
			result = jedis.zrevrange(honorType, start, end);
			
			jedisErrorFlag = false;
		} catch (Exception e) {
			logger.error("FamilyHonorSource.getFamilyHonorList(" + "honorType:" + honorType + "start:" + start + "end:" + end + ") execute exception.", e);
		} finally {
			if(jedis!=null) {
				freeInstance(jedis, jedisErrorFlag);
			}
		}
		
		return result;
	}

	/**
	 * 获取家族单个荣誉下家族个数
	 * @param honorType
	 * @return
	 */
	public static long getFamilyHonorCount(String honorType) {
		Long count = null;
		
		Jedis jedis = null;
		boolean jedisErrorFlag = true;
		try {
			jedis = getInstance();
			
			count = jedis.zcard(honorType);
			
			jedisErrorFlag = false;
		} catch (Exception e) {
			logger.error("FamilyHonorSource.getFamilyHonorCount(" + "honorType:" + honorType + ") execute exception.", e);
		} finally {
			if(jedis!=null) {
				freeInstance(jedis, jedisErrorFlag);
			}
		}
		if (count != null) {
			return count.longValue();
		} else {
			return 0;
		}
	}
	
	/**
	 * 获取排行榜
	 * @param rankType
	 * @param slotType
	 * @param familyId
	 * @return
	 */
	public static String getFamilyUserRankingNew(int rankType, int slotType, int familyId) {
	    Jedis jedis = null;
        try {
        	jedis = getInstance();

        	String str = jedis.get(String.format(FAMILYRANKING_KEY_REDIS_CACHE, rankType, slotType, familyId));
        	if (StringUtils.isBlank(str)) {
        		
        		List<JsonObject> jsonArray = new ArrayList<JsonObject>();

	        	Set<String> set =  jedis.zrevrange(String.format(FAMILYRANKING_KEY_NEW, rankType, slotType, familyId), 0, 20);
	        	if (set != null && set.size() > 0) {
	        		for (String userId : set) {

	        			JsonObject result = new JsonObject();
	        			String portrait = null;
	        			int roomId = 0;
	        			String nickname = null;
	        			int gender = 0;
	        			int iconTag = 0;

	        			long total = getFamilyUserRankingTotal(rankType,slotType,familyId,userId).longValue();

	        			if (rankType == RankingEnum.RANKING_TYPE_RICH) {
	        				KkUserService userService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
	        				if (userService == null) {
								continue;
							}
	        				UserInfoDetail userInfoDetail =	userService.getUserDetailInfo(Integer.valueOf(userId));

	        				roomId = userInfoDetail.getProfile().getUserId();
	        				nickname = userInfoDetail.getProfile().getNickName();
	        				gender = userInfoDetail.getProfile().getGender();
	        				portrait = userInfoDetail.getProfile().getPortrait();

	        				result.addProperty("contribution", total);

						}else if (rankType == RankingEnum.RANKING_TYPE_ACTOR) {
							RoomInfo roomInfo = RoomService.getRoomInfo(Integer.valueOf(userId));
							if (roomInfo == null) {
								continue;
							}
							roomId = roomInfo.getActorId();
							nickname = roomInfo.getNickname();
							portrait = roomInfo.getPortrait();
							if (roomInfo.getIcon() != null) {
							    iconTag = roomInfo.getIcon();
							}
                            gender = roomInfo.getGender();

                            result.addProperty("earnTotal", total);

		                     if (roomInfo.getRoomSource() != null && roomInfo.getRoomSource() == 10 && !StringUtil.strIsNull(portrait)) {
		                         result.addProperty("poster_path_original",  portrait);
		                         result.addProperty("poster_path_1280", portrait + "!1280");
		                         result.addProperty("poster_path_290",  portrait + "!290x164");
		                         result.addProperty("poster_path_272",  portrait + "!272");
		                         result.addProperty("poster_path_128",  portrait + "!128x96");
		                         result.addProperty("poster_path_300",  portrait + "!300");
		                     } else {
		                    	 String poster = roomInfo.getPoster();
		                         if (!StringUtil.strIsNull(poster)) {
		                             result.addProperty("poster_path_original", poster);
		                             result.addProperty("poster_path_1280",  poster + "!1280");
		                             result.addProperty("poster_path_290",  poster + "!290x164");
		                             result.addProperty("poster_path_272",  poster + "!272");
		                             result.addProperty("poster_path_128",  poster + "!128x96");
		                             result.addProperty("poster_path_300",  poster + "!300");
		                         }
	                     	}
						}

	        			result.addProperty("userId", Integer.valueOf(userId));
	        			result.addProperty("roomId",roomId);
	        			result.addProperty("nickname", nickname);
	        			result.addProperty("gender", gender);
	        			result.addProperty("iconTag", iconTag);

	        		    if (!StringUtil.strIsNull(portrait)) {
	                         result.addProperty("portrait_path_original",  portrait);
	                         result.addProperty("portrait_path_48",  portrait + "!48");
	                         result.addProperty("portrait_path_128", portrait + "!128");
	                         result.addProperty("portrait_path_256", portrait + "!256");
	                         result.addProperty("portrait_path_1280", portrait + "!1280");
	                    }

	                    jsonArray.add(result);
					}
				}
	        	if (jsonArray != null && jsonArray.size() > 0 ) {
	        		str = new Gson().toJson(jsonArray);
	        		jedis.setex(String.format(FAMILYRANKING_KEY_REDIS_CACHE, rankType, slotType, familyId), 5*60, str);
				}
        	}
        	return str;
        } catch (Exception e) {
            logger.error("FamilyHonorSource.getFamilyUserRankingNew(" + "rankType:" + rankType + "slotType:" + slotType + "familyId:" + familyId + ") execute exception.", e);
        } finally {
            if (jedis != null) {
                freeInstance(jedis, false);
            }
        }
        return null;
	}
	
	/**
	 *  或对应earnTotal consumeTotal 
	 * @param rankType
	 * @param slotType
	 * @param familyId
	 * @param member
	 * @return
	 */
	public static Double getFamilyUserRankingTotal(int rankType, int slotType, int familyId,String member){
		Jedis jedis = null;
        try {
        	jedis = getInstance();
        	if (StringUtils.isNotBlank(member)) {
        	    Double score = jedis.zscore(String.format(FAMILYRANKING_KEY_NEW, rankType, slotType, familyId), member);
        	    if (score != null) {
        	        return score;
        	    }
        	}
        } catch (Exception e) {
            logger.error("FamilyHonorSource.getFamilyUserRankingTotal(" + "rankType:" + rankType + "slotType:" + slotType + "familyId:" + familyId + "member:" + member + ") execute exception.", e);
        } finally {
            if (jedis != null) {
                freeInstance(jedis, false);
            }
        }
        return 0d;
	}
}

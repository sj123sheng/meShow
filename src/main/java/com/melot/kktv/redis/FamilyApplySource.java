package com.melot.kktv.redis;

import java.util.Set;

import org.apache.log4j.Logger;

import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.redis.RedisConfigHelper;

import redis.clients.jedis.Jedis;

public class FamilyApplySource {
	
	private static Logger logger = Logger.getLogger(FamilyApplySource.class);
	
	private static final String SOURCE_NAME = "FamilyApply";
	
	// 申请加入家族Hashes	Set<applyFamilyFPHash_familyid, userid>
	private static final String APPLY_FAMILY_FP_KEY = "applyFamilyFPHash_%s";
	// 申请加入家族Hashes	Set<applyFamilyUPHash_userid, familyid>
	private static final String APPLY_FAMILY_UP_KEY = "applyFamilyUPHash_%s";
	
	// 申请退出家族主播
	private static final String FAMILY_APPLY_EXIT_ACTORS = "familyApplyExitActors";
	
	private static Jedis getInstance() {
		return RedisConfigHelper.getJedis(SOURCE_NAME);
	}
	
	private static void freeInstance(Jedis jedis) {
		RedisConfigHelper.returnJedis(SOURCE_NAME, jedis, false);
	}
	
	/**
	 * 申请加入家族
	 * @param familyId	家族ID
	 * @param userId	用户ID
	 * @param userInfo	用户信息
	 */
	public static void applyJoinFamily(String familyId, String userId) {
		if (familyId == null || "".equals(familyId.trim()) || userId == null || "".equals(userId.trim())) {
			return;
		}
		
		Jedis jedis = null;
		try {
			jedis = getInstance();
			jedis.sadd(String.format(APPLY_FAMILY_FP_KEY, familyId), userId);
			jedis.sadd(String.format(APPLY_FAMILY_UP_KEY, userId), familyId);
		} catch (Exception e) {
			logger.error("applyJoinFamily error, familyId : " + familyId + ", userId : " + userId + ", isActor", e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
	}
	
	/**
	 * 取消申请加入家族(包括拒绝加入)
	 * @param familyId	家族ID
	 * @param userId	用户ID
	 */
	public static void cancelJoinFamily(String familyId, String userId) {
		if (familyId == null || "".equals(familyId.trim()) || userId == null || "".equals(userId.trim())) {
			return;
		}
		
		Jedis jedis = null;
		try {
			jedis = getInstance();
			
			String pattern = null;
			Set<String> keySet = null;
			String otherPattern = null;
			String value = null;
			boolean oneRow = false;
			if ("*".equals(familyId.trim())) {
				pattern = String.format(APPLY_FAMILY_UP_KEY, userId);
				otherPattern = APPLY_FAMILY_FP_KEY;
				value = userId;
			} else if ("*".equals(userId.trim())) {
				pattern = String.format(APPLY_FAMILY_FP_KEY, familyId);
				otherPattern = APPLY_FAMILY_UP_KEY;
				value = familyId;
			} else {
				oneRow = true;
			}
			if (oneRow) {
				jedis.srem(String.format(APPLY_FAMILY_FP_KEY, familyId), userId);
				jedis.srem(String.format(APPLY_FAMILY_UP_KEY, userId), familyId);
			} else {
				keySet = jedis.smembers(pattern);
				if (keySet != null && !keySet.isEmpty()) {
					for (String string : keySet) {
						jedis.srem(String.format(otherPattern, string), value);
					}
					jedis.del(pattern);
				}
			}
		} catch (Exception e) {
			logger.error("cancelJoinFamily error, familyId : " + familyId + ", userId : " + userId, e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
	}
	
	/**
	 * 根据家族Id删除申请加入家族的所有用户
	 * @param familyId
	 */
	public static void delApplyJoinFamilyUsers(String familyId) {
	    if (StringUtil.strIsNull(familyId)) {
	        return;
	    }
	    Jedis jedis = null;
        try {
            jedis = getInstance();
            String pattern = String.format(APPLY_FAMILY_FP_KEY, familyId);
            /*Set<String> applyUsers = jedis.smembers(pattern);
            if (applyUsers != null && applyUsers.size() > 0) {
                for (String applyUserId : applyUsers) {
                    cancelJoinFamily(familyId, applyUserId); //TODO 可以不删， 影响应该不大
                }
            }*/
            jedis.del(pattern);
        } catch (Exception e) {
            logger.error("delApplyJoinFamilyUsers error, familyId : " + familyId, e);
        } finally {
            if (jedis != null) {
                freeInstance(jedis);
            }
        }
	}
	
	
	/**
	 * 获取用户已申请加入家族
	 * @param userId
	 * @return familyId 家族ID
	 */
	public static String getApplyFamily(String userId) {
		if (userId == null || "".equals(userId.trim())) {
			return null;
		}
		
		Jedis jedis = null;
		String familyId = null;
		try {
			jedis = getInstance();
			// pattern : applyFamilyUPHash_1000022
			String pattern = String.format(APPLY_FAMILY_UP_KEY, userId);
			// key : applyFamilyUPHash_1000022
			Set<String> familySet = jedis.smembers(pattern);
			if (familySet != null && !familySet.isEmpty()) {
				familyId = familySet.iterator().next();
			}
		} catch (Exception e) {
			logger.error("getApplyFamily error, userId : " + userId, e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
		return familyId;
	}
	
	/**
	 * 获取家族申请加入用户数
	 * @param familyId
	 * @return
	 */
	public static int getApplicantCount(String familyId) {
		if (familyId == null || "".equals(familyId.trim())) {
			return 0;
		}
		
		Jedis jedis = null;
		int count = 0; 
		try {
			jedis = getInstance();
			String pattern = String.format(APPLY_FAMILY_FP_KEY, familyId);
			Long num = jedis.scard(pattern);
			if (num != null && num > 0) {
				count = num.intValue();
			}
		} catch (Exception e) {
			logger.error("getApplicantCount error, familyId : " + familyId, e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
		return count;
	}
	
	/**
	 * 取消申请退出
	 * @param userId
	 */
	public static void calApplyFamilyExit(int userId) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			jedis.zrem(FAMILY_APPLY_EXIT_ACTORS, userId + "");
		} catch (Exception e) {
			logger.error("calApplyFamilyExit error, userId : " + userId, e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis);
			}
		}
	}
	
}

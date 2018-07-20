package com.melot.kktv.redis;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import com.melot.kktv.util.redis.RedisConfigHelper;

import redis.clients.jedis.Jedis;

public class FamilySource {
    
    private static Logger logger = Logger.getLogger(FamilySource.class); 
	
	private static final String SOURCE_NAME = "Family";
	
	// 家族成员Key		(familyMember_userid)
	private static final String FAMILY_MEMBER_KEY = "familyMember_";
	// 家族主播列表 自动排序集合 (familyRoom_familyid)
	private static final String FAMILY_ROOM_KEY = "familyRoom_";
	
	private static Jedis getInstance() {
		return RedisConfigHelper.getJedis(SOURCE_NAME);
	}
	
	private static void freeInstance(Jedis jedis) {
		RedisConfigHelper.returnJedis(SOURCE_NAME, jedis, false);
	}

	/**
	 * 设置家族成员
	 * @param familyId	家族ID
	 * @param userId	用户ID
	 */
	public static void setFamilyMember(String familyId, String userId) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			jedis.set(FAMILY_MEMBER_KEY+userId, familyId);
		} catch (Exception e) {
			logger.error("FamilySource.setFamilyMember(" + "familyId:" + familyId + "userId:" + userId + ") execute exception.", e);
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
	}
	
	/**
	 * 获取成员所属家族
	 * @param userId	用户ID
	 * @return 家族ID
	 */
	public static String getMemberFamily(String userId) {
		Jedis jedis = null;
		String familyId = null;
		try {
			jedis = getInstance();
			familyId = jedis.get(FAMILY_MEMBER_KEY+userId);
		} catch (Exception e) {
			logger.error("FamilySource.getMemberFamily(" + "userId:" + userId + ") execute exception.", e);
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return familyId;
	}

	
	/**
	 * 删除家族成员
	 * @param userId	用户ID
	 */
	public static void delFamilyMember(String userId) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			jedis.del(FAMILY_MEMBER_KEY+userId);
		} catch (Exception e) {
			logger.error("FamilySource.delFamilyMember(" + "userId:" + userId + ") execute exception.", e);
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
	}
	
	/**
	 * 从缓存数据中获取家族房间列表
	 * @param familyId
	 * @param start
	 * @param offset
	 * @return
	 */
	public static Set<String> getFamilyRoomSet(int familyId, int start, int offset) {
		Set<String> set = new HashSet<String>();
		
		Jedis jedis = null;
		try {
			jedis = getInstance();
			set = jedis.zrange(FAMILY_ROOM_KEY + familyId, start, start + offset - 1L);
		} catch (Exception e) {
			logger.error("fail to get family room list from redis sorted set, familyId " + familyId,  e);
		} finally {
            if (jedis != null) {
                freeInstance(jedis);
            }
        }
		
		return set;
	}
	
	/**
	 * 从缓存数据中获取家族房间总数
	 * @param familyId
	 * @return
	 */
	public static int countFamilyRoomSet(int familyId) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			Long value = jedis.zcard(FAMILY_ROOM_KEY + familyId);
			if (value != null) {
				return value.intValue();
			}
		} catch (Exception e) {
			logger.error("fail to get family room total count from redis sorted set, familyId " + familyId,  e);
		} finally {
            if (jedis != null) {
                freeInstance(jedis);
            }
        }
		return 0;
	}
	
}

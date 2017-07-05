package com.melot.kktv.redis;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.kktv.util.redis.RedisConfigHelper;

import redis.clients.jedis.Jedis;

public class GuessSource {
	
	private static final String SOURCE_NAME = "Guess";

	// 竞猜各选项缓存数据		key	(guess_guessid)
	private static final String GUESSOPTLIST_KEY = "guessOptList_";
	// 竞猜各选项参与人数	数据		hash	(guessOption__guessid)
	private static final String GUESSOPTCNT_KEY = "guessOptCnt_";
	// 用户竞猜选项数据	hash	(userGuess_guessid)
	private static final String USERGUESS_KEY = "userGuess_";
	
	private static Jedis getInstance() {
		return RedisConfigHelper.getJedis(SOURCE_NAME);
	}
	
	private static void freeInstance(Jedis jedis) {
		RedisConfigHelper.returnJedis(SOURCE_NAME, jedis, false);
	}
	
	/**
	 * increase user count of guess's option, key expire at setting time
	 * @param guessId
	 * @param optionId
	 * @param expireTime
	 * @return
	 */
	public static void incOptionUserCount(String guessId, String optionId, long expireTime) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			/*
			 * Control is not set to repeat timeout
			 */
			long afterIncrementValue = jedis.hincrBy(GUESSOPTCNT_KEY + guessId, optionId, 1);
			if (afterIncrementValue == 1) {
				jedis.expireAt(GUESSOPTCNT_KEY + guessId, expireTime);
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
	 * set user choosen guess's option, key expire at setting time
	 * @param userId
	 * @param guessId
	 * @param option
	 * @param expireTime
	 */
	public static void setUserOption(String userId, String guessId, String option, long expireTime) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			/*
			 * Control is not set to repeat timeout
			 */
			if (!jedis.exists(USERGUESS_KEY + guessId)) {
				jedis.expireAt(USERGUESS_KEY + guessId, expireTime);
			}
			jedis.hset(USERGUESS_KEY+guessId, userId, option);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
	}
	
	/**
	 * get user choosen guess's option
	 * @param userId
	 * @param guessId
	 * @return
	 */
	public static JsonObject getUserOption(String userId, String guessId) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			String userOption = jedis.hget(USERGUESS_KEY + guessId, userId);
			if (userOption != null) {
				return new JsonParser().parse(userOption).getAsJsonObject();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return null;
	}
	
	/**
	 * get guess all option list
	 * @param guessId
	 * @return
	 */
	public static JsonArray getOptionList(String guessId) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			String optionList = jedis.get(GUESSOPTLIST_KEY + guessId);
			if (optionList != null) {
				JsonArray optionArray = new JsonParser().parse(optionList).getAsJsonArray();
				for (int i = 0; i < optionArray.size(); i++) {
					JsonObject optionJson = optionArray.get(i).getAsJsonObject();
					if (optionJson.has("optionId") && !optionJson.get("optionId").isJsonNull()) {
						String optionId = optionJson.get("optionId").getAsString();
						String userCount = jedis.hget(GUESSOPTCNT_KEY + guessId, optionId);
						if (userCount!=null) {
							optionJson.addProperty("userCount", Integer.parseInt(userCount));
						} else {
							optionJson.addProperty("userCount", 0);
						}
					}
				}
				return optionArray;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
		return null;
	}
	
	/**
	 * set guess all option list, key expire at setting time
	 * @param guessId
	 * @param optionList
	 * @param expireTime
	 */
	public static void setOptionList(String guessId, String optionList, long expireTime) {
		Jedis jedis = null;
		try {
			jedis = getInstance();
			jedis.set(GUESSOPTLIST_KEY + guessId, optionList);
			jedis.expireAt(GUESSOPTLIST_KEY + guessId, expireTime);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
	}
	
}

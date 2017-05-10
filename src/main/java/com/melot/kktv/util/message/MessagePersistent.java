package com.melot.kktv.util.message;

import java.util.HashMap;
import java.util.Map;
import com.melot.kktv.redis.UserMessageSource;
import redis.clients.jedis.Jedis;

public class MessagePersistent {

	private static final String USER_OTHERROOMMSAGE_KEY_FORMAT = "%s_%s";
	private static final String USER_ROOMMSAGE_KEY_FORMAT = "%s_1_%s";
	// private static final String USER_MSAGESORT_KEY_FORMAT = "%s_msgsort";

	public static String getMessageKey(Message msg) {
		if (msg.getType() == Message.MSGTYPE_ROOM) {
			return String.format(USER_ROOMMSAGE_KEY_FORMAT, msg.getTo(),
					msg.getContext());
		} else {
			return String.format(USER_OTHERROOMMSAGE_KEY_FORMAT, msg.getTo(),
					msg.getType());
		}
	}

	public static boolean addMessage(Message msg) {
		// redis
		Jedis jedis = null;
		jedis = UserMessageSource.getInstance();

		try {
			String key = getMessageKey(msg);
			String sortKey = msg.getTo() + "_msgsort";
			// 1. insert the message
			Map<String, String> msgMap = new HashMap<String, String>();
			String msgOwnUserId = msg.getTo() + "";
			msgMap.put("to", msgOwnUserId);
			String msgTimeStr = jedis.hget(key, "msgtime");
			boolean bUpdateValue = true;
			if (msgTimeStr == null) {
				msgMap.put("count", "1");
			} else {
				jedis.hincrBy(key, "count", 1);// count ++
				if (msg.getMsgtime() < Long.parseLong(msgTimeStr)) {
					bUpdateValue = false;
				}
			}

			if (bUpdateValue) {
				msgMap.put("from", msg.getFrom() + "");
				msgMap.put("context", msg.getContext() + "");
				msgMap.put("message", msg.getMessage());
				msgMap.put("msgtime", msg.getMsgtime() + "");
				if(msg.getTargetuserid() != null)
					msgMap.put("target", msg.getTargetuserid() + "");
				else 
					msgMap.put("target", "");
				jedis.hmset(key, msgMap);
			}

			// 2. sort the message
			if (bUpdateValue) {
				jedis.zadd(sortKey, msg.getMsgtime(), key);
			}

			// 3. total count ++
			if (jedis.hget("msgTotalCount", msgOwnUserId) != null) {
				jedis.hincrBy("msgTotalCount", msgOwnUserId, 1);
			} else {
				jedis.hset("msgTotalCount", msgOwnUserId, "1");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (jedis != null) {
				UserMessageSource.freeInstance(jedis);
			}
		}

		return true;
	}
}

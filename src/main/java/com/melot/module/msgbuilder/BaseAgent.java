package com.melot.module.msgbuilder;

import org.apache.log4j.Logger;

import com.google.protobuf.MessageLite;
import com.melot.module.MelotResourcePoolProxy;
import com.melot.module.driver.MelotResource;
import com.melot.module.driver.MessageBuilder;
import com.melot.module.driver.exceptions.MelotModuleClientConnectionException;

public class BaseAgent {

	public final static int RESP_CODE_SUCCESS = 0;
	
	private static Logger logger = Logger.getLogger(BaseAgent.class);
	
	public static MelotResourcePoolProxy initAgent(MessageBuilder msgBuilder, String zkService, String myIp) {
		MelotResourcePoolProxy pool = null;
		if (zkService != null)
			pool = new MelotResourcePoolProxy(msgBuilder, zkService, myIp);
		return pool;
	}

	public static void destroyAgent(MelotResourcePoolProxy pool) {
		if (pool != null) {
			pool.destroy();
		}
	}

	public static MessageLite excute(MelotResourcePoolProxy pool, short msgId, MessageLite message) {
		MessageLite result = null;
		if (msgId < 1) {
			return result;
		}

		boolean bBrokenMMC = false;
		MelotResource mmc = null;
		try {
			mmc = pool.getResource();
			result = mmc.sendMessage(msgId, message);
		} catch (MelotModuleClientConnectionException e) {
			logger.error("failed dure to network error: " + mmc.getHost() + ":" + mmc.getPort(), e);
			bBrokenMMC = true;
		} catch (Exception e) {
			logger.error("Agent: [msgId= " + msgId+ "][message= " + message.toString() + "]: failed dure exception : ", e);
			bBrokenMMC = true;// any exception, the connection can not be used
								// any more actually
		} finally {
			if (bBrokenMMC) {
				pool.returnBrokenResource(mmc);
			} else {
				pool.returnResource(mmc);
			}
		}
		return result;
	}
	
}

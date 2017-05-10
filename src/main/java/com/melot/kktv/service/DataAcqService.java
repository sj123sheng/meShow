package com.melot.kktv.service;

import com.google.gson.JsonObject;
import com.melot.kktv.redis.ChannelSource;

/**
 * 数据采集服务
 * @author cj
 *
 */
public class DataAcqService {
	
	/**
	 * 用户注册成功发送消息到 register
	 * @param userId
	 * @param openPlatform
	 * @param platform
	 */
	public static void sendRegister(int userId, int openPlatform, int platform) {
		
		JsonObject json = new JsonObject();
		json.addProperty("userId", userId);
		json.addProperty("openPlatform", openPlatform);
		json.addProperty("platform", platform);
		ChannelSource.pubChannelMsg("register", json.toString());
	}
	
	/**
	 * 用户完成任务发送消息到 doTask
	 * @param userId
	 * @param taskId
	 */
	public static void sendDoTask(int userId, int taskId) {
		
		JsonObject json = new JsonObject();
		json.addProperty("userId", userId);
		json.addProperty("taskId", taskId);
		ChannelSource.pubChannelMsg("doTask", json.toString());
	}
	
	/**
	 * 用户关注主播发送消息到 doFollow 
	 * @param userId
	 * @param followedId
	 */
	public static void sendDoFollow(int userId, int followedId) {
		
		JsonObject json = new JsonObject();
		json.addProperty("userId", userId);
		json.addProperty("followedId", followedId);
		ChannelSource.pubChannelMsg("doFollow", json.toString());
	}
	
	/**
	 * 用户充值成功发送消息到 recharge	 
	 * @param userId
	 * @param money
	 */
	public static void sendRecharge(int userId, int money) {
		
		JsonObject json = new JsonObject();
		json.addProperty("userId", userId);
		json.addProperty("money", money);
		ChannelSource.pubChannelMsg("recharge", json.toString());
	}

}

package com.melot.kkcx.transform;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kktv.util.ConfigHelper;
import com.melot.module.guard.driver.domain.GoldRanking;
import com.melot.module.guard.driver.domain.GuardInfo;

public class GuardTF {
	
	public static JsonObject guardGoldRankingTF(GoldRanking rank) {
		
		JsonParser parse = new JsonParser();
		JsonObject jsonObject = new JsonObject();
		if (rank.getGender() != null) {
			jsonObject.addProperty("gender", rank.getGender());
		}
		if (rank.getGuardIcon() != null) {
			jsonObject.add("guardIcon", parse.parse(rank.getGuardIcon()).getAsJsonObject());
		}
		if (rank.getGuardName() != null) {
			jsonObject.addProperty("guardName", rank.getGuardName());
		}
		
		UserProfile user = com.melot.kktv.service.UserService.getUserInfoV2(rank.getUserId());
		if (user != null) {
		    jsonObject.addProperty("nickName", user.getNickName() == null ? rank.getNickName() : user.getNickName());
		    
		    if (user.getPortrait() != null) {
		        jsonObject.addProperty("portrait_path_original", ConfigHelper.getHttpdir() + user.getPortrait());
		        jsonObject.addProperty("portrait_path_1280", ConfigHelper.getHttpdir() + user.getPortrait() + "!1280");
		        jsonObject.addProperty("portrait_path_256", ConfigHelper.getHttpdir() + user.getPortrait() + "!256");
		        jsonObject.addProperty("portrait_path_128", ConfigHelper.getHttpdir() + user.getPortrait() + "!128");
		        jsonObject.addProperty("portrait", ConfigHelper.getHttpdir() + user.getPortrait() + "!128");
		        jsonObject.addProperty("portrait_path_48", ConfigHelper.getHttpdir() + user.getPortrait() + "!48");
            }

		}
		
		if (rank.getUserId() != null) {
			jsonObject.addProperty("userId", rank.getUserId());
		}
		if (rank.getGoldGuardIcon() != null) {
			jsonObject.add("goldGuardIcon", parse.parse(rank.getGoldGuardIcon()).getAsJsonObject());
		}
		if (rank.getGoldGuardLevel() != null) {
			jsonObject.addProperty("goldGuardLevel", rank.getGoldGuardLevel());
		}
		if (rank.getGoldGuardName() != null) {
			jsonObject.addProperty("goldGuardName", rank.getGoldGuardName());
		}
		if (rank.getConsumeCount() != null) {
			jsonObject.addProperty("consumeCount", rank.getConsumeCount());
		}
		if (rank.getGuardYearIcon() != null) {
			jsonObject.add("guardYearIcon", parse.parse(rank.getGuardYearIcon()).getAsJsonObject());
		}
		jsonObject.addProperty("guardId", rank.getGuardId());
		jsonObject.addProperty("guardLevel", rank.getGuardLevel());
		jsonObject.addProperty("guardExpireTime", rank.getGuardExpireTime());
		return jsonObject;
	}
	
	public static JsonObject userGuardTF(GuardInfo roomInfo) {
		
		JsonObject jsonObject = new JsonObject();
		if (roomInfo.getExpireTime() != null) {
			jsonObject.addProperty("expireTime", roomInfo.getExpireTime().getTime());
		}
		UserProfile userInfo = com.melot.kktv.service.UserService.getUserInfoV2(roomInfo.getActorId());
		if (userInfo != null) {
			jsonObject.addProperty("richLevel", userInfo.getUserLevel());
			jsonObject.addProperty("actorLevel", userInfo.getActorLevel());	
			jsonObject.addProperty("nickname", userInfo.getNickName());
			
		}
		jsonObject.addProperty("actorId", roomInfo.getActorId());
		jsonObject.addProperty("userId", roomInfo.getUserId());
		jsonObject.addProperty("guardId", roomInfo.getGuardId());
		
		return jsonObject;
	}
	
}
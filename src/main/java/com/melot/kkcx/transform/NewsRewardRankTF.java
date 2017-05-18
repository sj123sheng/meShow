package com.melot.kkcx.transform;

import com.google.gson.JsonObject;
import com.melot.kktv.model.NewsRewardRank;
import com.melot.kkcx.service.UserAssetServices;
import com.melot.kkcx.service.UserService;

public class NewsRewardRankTF {

	public static JsonObject toJsonObject(NewsRewardRank newsRewardRank) {
		JsonObject result = new JsonObject();
		result.addProperty("userId", newsRewardRank.getUserId());
		result.addProperty("nickname", newsRewardRank.getNickname());
		result.addProperty("rcount", newsRewardRank.getRcount());
		if (newsRewardRank.getPortrait_path_original()!=null) {
			result.addProperty("portrait_path_original", newsRewardRank.getPortrait_path_original());
		}
		// 读取明星等级
		result.addProperty("actorLevel", UserService.getActorLevel(newsRewardRank.getUserId()));
		// 读取富豪等级
		result.addProperty("richLevel", UserService.getRichLevel(newsRewardRank.getUserId()));
		// 读取靓号
		JsonObject validVirtualId =  UserAssetServices.getValidVirtualId(newsRewardRank.getUserId()); //获取用户虚拟账号
		if (validVirtualId != null) {
			if (validVirtualId.get("idType").getAsInt() == 1) {
				// 支持老版靓号
				result.addProperty("luckyId", validVirtualId.get("id").getAsInt());
			}
			result.add("validId", validVirtualId);
		}
		
		if (newsRewardRank.getRoomSource() != null) {
		    result.addProperty("roomSource", newsRewardRank.getRoomSource()); 
		}
		
		return result;
	}
	
}

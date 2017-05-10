package com.melot.kkcx.transform;

import com.google.gson.JsonObject;
import com.melot.kktv.model.NewsComment;
import com.melot.kkcx.service.UserService;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.PlatformEnum;

public class NewsCommentTF {

	/**
	 * 转成JsonObject
	 * 
	 * @return JsonObject
	 */
	public static JsonObject toJsonObject(NewsComment newsComment, int platform) {
		JsonObject jObject = new JsonObject();
		jObject.addProperty("userId", newsComment.getUserId());
		jObject.addProperty("nickname", newsComment.getNickname());
		jObject.addProperty("gender", newsComment.getGender());
		if (newsComment.getPortrait_path_original() != null) {
			if(platform == PlatformEnum.WEB) {
				jObject.addProperty("portrait_path_48", ConfigHelper.getHttpdir() + newsComment.getPortrait_path_48());
				jObject.addProperty("portrait_path_256", ConfigHelper.getHttpdir() + newsComment.getPortrait_path_256());
			} else if(platform == PlatformEnum.ANDROID) {
				jObject.addProperty("portrait_path_48", ConfigHelper.getHttpdir() + newsComment.getPortrait_path_48());
			} else if(platform == PlatformEnum.IPHONE) {
				jObject.addProperty("portrait_path_48", ConfigHelper.getHttpdir() + newsComment.getPortrait_path_48());
			} else if(platform == PlatformEnum.IPAD) {
				jObject.addProperty("portrait_path_128", ConfigHelper.getHttpdir() + newsComment.getPortrait_path_128());
			} else {
				jObject.addProperty("portrait_path_1280", ConfigHelper.getHttpdir() + newsComment.getPortrait_path_1280());
				jObject.addProperty("portrait_path_256", ConfigHelper.getHttpdir() + newsComment.getPortrait_path_256());
				jObject.addProperty("portrait_path_128", ConfigHelper.getHttpdir() + newsComment.getPortrait_path_128());
				jObject.addProperty("portrait_path_48", ConfigHelper.getHttpdir() + newsComment.getPortrait_path_48());
			}
		}
		jObject.addProperty("commentId", newsComment.getCommentId());
		jObject.addProperty("newsId", newsComment.getNewsId());
		jObject.addProperty("content", newsComment.getContent());
		jObject.addProperty("commentTime", newsComment.getCommentTime().getTime());
		// 读取明星等级
		jObject.addProperty("actorLevel", UserService.getActorLevel(newsComment.getUserId()));
		// 读取富豪等级
		jObject.addProperty("richLevel", UserService.getRichLevel(newsComment.getUserId()));
		return jObject;
	}
	
}

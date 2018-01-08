package com.melot.kkcx.transform;

import java.util.List;

import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.kktv.model.News;
import com.melot.kkcx.service.UserService;
import com.melot.kktv.util.NewsMediaTypeEnum;
import com.melot.kktv.util.PlatformEnum;

public class NewsTF {

	private static Logger logger = Logger.getLogger(NewsTF.class);
	
	/**
	 * 转成JsonObject
	 * 
	 * @param platform 平台号 1:web 2:android 3:iphone 4:ipad
	 * @param isInRoom 1:房间内 0：房间外  
	 * @return
	 */
	public static JsonObject toJsonObject(News news, int platform, int isInRoom) { //isInRoom 1:房间内 0：房间外  
		JsonObject jObject = new JsonObject();
		if (isInRoom == 0) {
			jObject.addProperty("userId", news.getUserId());
			jObject.addProperty("nickname", news.getNickname());
			if (news.getGender() != null) {
				jObject.addProperty("gender", news.getGender());
			}
			if (news.getPortrait_path_original() != null) {
				if (platform == PlatformEnum.WEB) {
					jObject.addProperty("portrait_path_256", news.getPortrait_path_256());
				} else if (platform == PlatformEnum.ANDROID) {
					jObject.addProperty("portrait_path_48",  news.getPortrait_path_48());
					jObject.addProperty("portrait_path_128", news.getPortrait_path_128());
				} else if (platform == PlatformEnum.IPHONE) {
					jObject.addProperty("portrait_path_128", news.getPortrait_path_128());
				} else if (platform == PlatformEnum.IPAD) {
					jObject.addProperty("portrait_path_128", news.getPortrait_path_128());
				} else {
					jObject.addProperty("portrait_path_1280",news.getPortrait_path_1280());
					jObject.addProperty("portrait_path_256", news.getPortrait_path_256());
					jObject.addProperty("portrait_path_128", news.getPortrait_path_128());
					jObject.addProperty("portrait_path_48",  news.getPortrait_path_48());
				}
			}
			// 读取明星等级
			jObject.addProperty("actorLevel", UserService.getActorLevel(news.getUserId()));
			// 读取富豪等级
			jObject.addProperty("richLevel", UserService.getRichLevel(news.getUserId()));
		}
		jObject.addProperty("newsId", news.getNewsId());
		jObject.addProperty("content", news.getContent());
		jObject.addProperty("publishedTime", news.getPublishedTime().getTime());
		jObject.addProperty("newsType", news.getNewsType());
		if (news.getResourceId() != null) {
			jObject.addProperty("resourceId", news.getResourceId());
		}
		if (news.getResourceUrl() != null) {
			String path_original = news.getResourceUrl();
			path_original = path_original.substring(0, path_original.length()) ;
			String path_1280 = path_original + "!1280" ;
			String path_272 = path_original + "!272" ;
			String path_128 = path_original + "!128x96" ;
			JsonObject jResourceUrl = new JsonObject();
			jResourceUrl.addProperty("path_1280", path_1280);
			jResourceUrl.addProperty("path_272", path_272);
			jResourceUrl.addProperty("path_128", path_128);
			jObject.add("resourceUrl", jResourceUrl);
		}
		jObject.addProperty("commentCount", news.getCommentCount() == null ? 0 : news.getCommentCount());
		jObject.addProperty("rewardCount", news.getRewardCount() == null ? 0 : news.getRewardCount());
		if (news.getMediaTitle() != null) {
			jObject.addProperty("mediaTitle", news.getMediaTitle());
		}
		// 解析多媒体资源   {"mediaUrl":"/2014/3/25/1008198_36000.mp4","imageUrl":"/2014/3/25/1008198_36000.jpg","mediaSize":2048,"mediaDur":60000}
		
		if (news.getMediaSource() != null) {
			try {
				JsonObject mediaSourceJson = new JsonParser().parse(news.getMediaSource()).getAsJsonObject();
				int mediaType = -1;
				if (mediaSourceJson.has("mediaType") && !mediaSourceJson.get("mediaType").isJsonNull()) {
					mediaType = mediaSourceJson.get("mediaType").getAsInt();
				}
				if (mediaSourceJson.has("imageUrl") && !mediaSourceJson.get("imageUrl").isJsonNull()) {
					// 分平台返回不同尺寸图片
					String path_original = mediaSourceJson.get("imageUrl").getAsString();
					String path_1280 = null;
					String path_720 = null;
					String path_272 = null;
					String path_128 = null;
					if (news.getMediaFrom() == null || news.getMediaFrom().intValue() == 1) {
						path_1280 = path_original + "!1280";
						path_720 = path_original + "!720";
						path_272 = path_original + "!272";
						path_128 = path_original + "!128x96";
					} else {
						path_1280 = path_original + "?imageView2/1/w/1280/h/960";
						path_720 = path_original + "?imageView2/1/w/720/h/540";
						path_272 = path_original + "?imageView2/1/w/272/h/204";
						path_128 = path_original + "?imageView2/1/w/128/h/96";
					}
					mediaSourceJson.addProperty("imageUrl_1280", path_1280);
					mediaSourceJson.addProperty("imageUrl_720", path_720);
					mediaSourceJson.addProperty("imageUrl_272", path_272);
					mediaSourceJson.addProperty("imageUrl_128", path_128);
					// 老版本返回resourceUrl
					if (news.getResourceUrl() == null && mediaType == NewsMediaTypeEnum.IMAGE) {
						JsonObject jResourceUrl = new JsonObject();
						jResourceUrl.addProperty("path_1280", path_1280);
						jResourceUrl.addProperty("path_272", path_272);
						jResourceUrl.addProperty("path_128", path_128);
						jObject.add("resourceUrl", jResourceUrl);
					}
					mediaSourceJson.remove("imageUrl");
				}
				jObject.add("mediaSource", mediaSourceJson);
			} catch (Exception e) {
				logger.error("NewsTF. fail to parse newsInfo mediaSource, mediaSource : " + news.getMediaSource());
			}
		} else if (news.getResourceUrl() != null) {
			JsonObject mediaJson = new JsonObject();
			mediaJson.addProperty("mediaType", NewsMediaTypeEnum.IMAGE);
			String path = news.getResourceUrl();
			path = path.substring(0,path.length());
			String path_1280 = path + "!1280";
			String path_720 = path + "!720";
			String path_272 = path + "!272";
			String path_128 = path + "!128x96";
			mediaJson.addProperty("imageUrl_1280", path_1280);
			mediaJson.addProperty("imageUrl_720", path_720);
			mediaJson.addProperty("imageUrl_272", path_272);
			mediaJson.addProperty("imageUrl_128", path_128);
			jObject.add("mediaSource", mediaJson);
		}
		// 增加推荐标识
		if (news.getIsRecNews() != null) {
			jObject.addProperty("isRecNews", news.getIsRecNews());
		}
		// 查看数
		int viewTimes = 0;
		if (news.getViewTimes() != null) viewTimes = news.getViewTimes();
		// 做假 viewTimes
		viewTimes = viewTimes*33 + 88;
		jObject.addProperty("viewTimes", viewTimes);
		// 增加动态图片视频来源
		jObject.addProperty("mediaFrom", news.getMediaFrom() == null ? 1 : news.getMediaFrom());
		return jObject;
	}
	
	public static JsonArray toRewardJsonObject(News news, int platform, List<News> rewardsLit) {	
		JsonArray jArray = new JsonArray();
		for(int i = 0; i < rewardsLit.size(); i++){
			JsonObject jObject = new JsonObject();
			News ns = (News) rewardsLit.get(i);
			jObject.addProperty("userId",ns.getUserId());
			if (news.getGender() != null) {
				jObject.addProperty("gender", ns.getGender());
			}
			if (ns.getPortrait_path_original() != null) {
				if(platform == PlatformEnum.WEB) {
					jObject.addProperty("portrait_path_256", ns.getPortrait_path_256());
				} else if(platform == PlatformEnum.ANDROID) {
					jObject.addProperty("portrait_path_48", ns.getPortrait_path_48());
					jObject.addProperty("portrait_path_128", ns.getPortrait_path_128());
				} else if(platform == PlatformEnum.IPHONE) {
					jObject.addProperty("portrait_path_128", ns.getPortrait_path_128());
				} else if(platform == PlatformEnum.IPAD) {
					jObject.addProperty("portrait_path_128", ns.getPortrait_path_128());
				} else {
					jObject.addProperty("portrait_path_1280",ns.getPortrait_path_1280());
					jObject.addProperty("portrait_path_256", ns.getPortrait_path_256());
					jObject.addProperty("portrait_path_128", ns.getPortrait_path_128());
					jObject.addProperty("portrait_path_48",  ns.getPortrait_path_48());
				}
			}
			jObject.addProperty("rewardCount", ns.getRewardCount());
			jArray.add(jObject);
		}
		return jArray;
	}
	
	public static JsonObject toJsonObjectForTopUsers(News news, int platform){
		JsonObject jObject = new JsonObject();
			jObject.addProperty("userId", news.getUserId());
			if (news.getGender() != null) {
				jObject.addProperty("gender", news.getGender());
			}
			jObject.addProperty("nickname", news.getNickname());
			jObject.addProperty("rewardCount", news.getRewardCount());
			if (news.getPortrait_path_original() != null) {
				if(platform == PlatformEnum.WEB) {
					jObject.addProperty("portrait_path_256", news.getPortrait_path_256());
				} else if(platform == PlatformEnum.ANDROID) {
					jObject.addProperty("portrait_path_48", news.getPortrait_path_48());
					jObject.addProperty("portrait_path_128", news.getPortrait_path_128());
				} else if(platform == PlatformEnum.IPHONE) {
					jObject.addProperty("portrait_path_128", news.getPortrait_path_128());
				} else if(platform == PlatformEnum.IPAD) {
					jObject.addProperty("portrait_path_128", news.getPortrait_path_128());
				} else {
					jObject.addProperty("portrait_path_1280", news.getPortrait_path_1280());
					jObject.addProperty("portrait_path_256", news.getPortrait_path_256());
					jObject.addProperty("portrait_path_128", news.getPortrait_path_128());
					jObject.addProperty("portrait_path_48",  news.getPortrait_path_48());
				}
			}
			// 读取明星等级
			jObject.addProperty("actorLevel", UserService.getActorLevel(news.getUserId()));
			// 读取富豪等级
			jObject.addProperty("richLevel", UserService.getRichLevel(news.getUserId()));
			return jObject;
	}
}

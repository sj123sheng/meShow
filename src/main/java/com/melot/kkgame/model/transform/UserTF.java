package com.melot.kkgame.model.transform;

import com.google.gson.JsonObject;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.kkcore.user.api.UserAssets;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kkcx.model.ActorLevel;
import com.melot.kkcx.model.RichLevel;
import com.melot.kkcx.service.UserService;
import com.melot.kkgame.model.UserModel;
import com.melot.kktv.service.RoomService;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.PlatformEnum;
import com.melot.sdk.core.util.MelotBeanFactory;

public class UserTF {
	
	
	/**
	 * Transform JavaBean UserModel To JsonObject
	 * @param UserModel user
	 * @param platform
	 * @return JsonObject userObject
	 */
	public static JsonObject userToJson(UserModel userInfo, int platform) {

		JsonObject userObject = new JsonObject();
		
		String pictureBaseDir = ConfigHelper.getHttpdir();
		if (userInfo != null && userInfo.getUserId() != null) {
			if (userInfo.getUserId() != null) {
				userObject.addProperty("userId", userInfo.getUserId());
			}
			if (userInfo.getNickname() != null) {
				userObject.addProperty("nickname", userInfo.getNickname());
			}
			if (userInfo.getSignature() != null) {
				userObject.addProperty("signature", userInfo.getSignature());
			}
			if (userInfo.getIntroduce() != null) {
				userObject.addProperty("introduce", userInfo.getIntroduce());
			}
			if (userInfo.getGender() != null) {
				userObject.addProperty("gender", userInfo.getGender());
			}
			if (userInfo.getCity() != null) {
				userObject.addProperty("city", userInfo.getCity());
			}
			if (userInfo.getBirthday() != null) {
				userObject.addProperty("birthday", userInfo.getBirthday());
			}
			if (userInfo.getPortraitPath() != null) {
				if (platform == PlatformEnum.WEB) {
					userObject.addProperty("portrait_path_256", pictureBaseDir + userInfo.getPortraitPath() + "!256");
				} else if (platform == PlatformEnum.ANDROID) {
					userObject.addProperty("portrait_path_48", pictureBaseDir + userInfo.getPortraitPath() + "!48");
					userObject.addProperty("portrait_path_128", pictureBaseDir + userInfo.getPortraitPath() + "!128");
				} else if (platform == PlatformEnum.IPHONE) {
					userObject.addProperty("portrait_path_256", pictureBaseDir + userInfo.getPortraitPath() + "!256");
					userObject.addProperty("portrait_path_128", pictureBaseDir + userInfo.getPortraitPath() + "!128");
				} else if (platform == PlatformEnum.IPAD) {
					userObject.addProperty("portrait_path_256", pictureBaseDir + userInfo.getPortraitPath() + "!256");
					userObject.addProperty("portrait_path_128", pictureBaseDir + userInfo.getPortraitPath() + "!128");
				} else {
					userObject.addProperty("portrait_path_original", pictureBaseDir + userInfo.getPortraitPath());
					userObject.addProperty("portrait_path_1280", pictureBaseDir + userInfo.getPortraitPath() + "!1280");
					userObject.addProperty("portrait_path_256", pictureBaseDir + userInfo.getPortraitPath() + "!256");
					userObject.addProperty("portrait_path_128", pictureBaseDir + userInfo.getPortraitPath() + "!128");
					userObject.addProperty("portrait_path_48", pictureBaseDir + userInfo.getPortraitPath() + "!48");
				}
			}
			if (userInfo.getFansCount() != null) {
				userObject.addProperty("fansCount", userInfo.getFansCount());
			}
			if (userInfo.getFollowCount() != null) {
				userObject.addProperty("followCount", userInfo.getFollowCount());
			}
			if (userInfo.getArea() != null) {
				userObject.addProperty("area", userInfo.getArea());
			}
			
			KkUserService kkUserService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
			UserAssets userAssets = kkUserService.getUserAssets(userInfo.getUserId());
            userObject.addProperty("earnTotal", userAssets.getEarnTotal());
            userObject.addProperty("consumeTotal", userAssets.getConsumeTotal());
            
            // 读取明星等级
            ActorLevel actorLevel = UserService.getActorLevel(userAssets.getEarnTotal());
            userObject.addProperty("actorLevel", actorLevel.getLevel());
            userObject.addProperty("actorMin", actorLevel.getMinValue());
            userObject.addProperty("actorMax", actorLevel.getMaxValue());
            
            // 读取富豪等级
            RichLevel richLevel = UserService.getRichLevel(userAssets.getConsumeTotal());
            userObject.addProperty("richLevel", richLevel.getLevel());
            userObject.addProperty("richMin", richLevel.getMinValue());
            userObject.addProperty("richMax", richLevel.getMaxValue());
            
            // 读取星级
            userObject.addProperty("starLevel", UserService.getStarLevel(userInfo.getUserId().intValue()));
			
			// 从PG主播库读取房间信息
			RoomInfo roomInfo = RoomService.getRoomInfo(userInfo.getUserId().intValue());
			if (roomInfo != null) {
				if(roomInfo.getScreenType()!=null){
					userObject.addProperty("screenType", roomInfo.getScreenType());
				}else{
					userObject.addProperty("screenType", 1);
				}
				// 0:非主播 1:主播 版本兼容
				userObject.addProperty("actorTag", 1);
				if (roomInfo.getType() != null) {
					userObject.addProperty("roomSource", roomInfo.getType());
				}
				if (roomInfo.getActorLevel() != null) {
					userObject.addProperty("actorLevel", roomInfo.getActorLevel());
				}
				if (roomInfo.getRoomMode() != null) {
					userObject.addProperty("roomMode", roomInfo.getRoomMode());
				}
				if (roomInfo.getRoomTheme() != null) {
					userObject.addProperty("roomTheme", roomInfo.getRoomTheme());
				}
				if (roomInfo.getPoster() != null) {
					if (platform == PlatformEnum.WEB) {
						userObject.addProperty("poster_path_290", pictureBaseDir + roomInfo.getPoster() + "!290x164");
						userObject.addProperty("poster_path_272", pictureBaseDir + roomInfo.getPoster() + "!272");
						userObject.addProperty("poster_path_128", pictureBaseDir + roomInfo.getPoster() + "!128x96");
						userObject.addProperty("poster_path_300", pictureBaseDir + roomInfo.getPoster() + "!300");
					} else if(platform == PlatformEnum.ANDROID || platform == PlatformEnum.IPHONE || platform == PlatformEnum.IPAD) {
						userObject.addProperty("poster_path_272", pictureBaseDir + roomInfo.getPoster() + "!272");
						userObject.addProperty("poster_path_128", pictureBaseDir + roomInfo.getPoster() + "!128x96");
						userObject.addProperty("poster_path_300", pictureBaseDir + roomInfo.getPoster() + "!300");
					} else {
						userObject.addProperty("poster_path_original", pictureBaseDir + roomInfo.getPoster());
						userObject.addProperty("poster_path_1280", pictureBaseDir + roomInfo.getPoster() + "!1280");
						userObject.addProperty("poster_path_290", pictureBaseDir + roomInfo.getPoster() + "!290x164");
						userObject.addProperty("poster_path_272", pictureBaseDir + roomInfo.getPoster() + "!272");
						userObject.addProperty("poster_path_128", pictureBaseDir + roomInfo.getPoster() + "!128x96");
						userObject.addProperty("poster_path_300", pictureBaseDir + roomInfo.getPoster() + "!300");
					}
				}
				if (roomInfo.getLiveType() != null) {
					userObject.addProperty("liveType", roomInfo.getLiveType());
				}
				if (roomInfo.getLiveStarttime() != null) {
					userObject.addProperty("livestarttime", roomInfo.getLiveStarttime().getTime());
				}
				if (roomInfo.getLiveEndtime() != null) {
					userObject.addProperty("liveendtime", roomInfo.getLiveEndtime().getTime());
				}
				if (roomInfo.getNextStarttime() != null) {
					userObject.addProperty("nextstarttime", roomInfo.getNextStarttime().getTime());
				}
			} else {
				// 0:非主播 1:主播 版本兼容
				userObject.addProperty("actorTag", 0);
				userObject.addProperty("roomSource", AppIdEnum.GAME);
			}
		}
		
		return userObject;
	}
	
}
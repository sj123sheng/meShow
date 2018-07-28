package com.melot.kkcx.service;

import com.google.gson.JsonObject;
import com.melot.kk.opus.api.constant.OpusCostantEnum;
import com.melot.kk.opus.api.domain.TempUserResource;
import com.melot.kktv.service.LiveVideoService;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.TagCodeEnum;

public class AlbumServices {

	public static JsonObject addIMChatImage(int userId, int pictureType, String path_original, String pictureName) {

		boolean flag = LiveVideoService.addPicture(userId,pictureType,path_original,pictureName);
		if(flag){
			JsonObject obj = new JsonObject();
			obj.addProperty("TagCode", TagCodeEnum.SUCCESS);
			return obj;
		}
		else {
			JsonObject obj = new JsonObject();
			obj.addProperty("TagCode", "04010010");
			return obj;
		}
	}

	public static JsonObject addPortraitNew(int resId, int userId, String path_original, String pictureName) {

		TempUserResource tempUserResource = new TempUserResource();

		// 增加TempUserResource
		tempUserResource.setAppId(AppIdEnum.AMUSEMENT);
		tempUserResource.setPath(path_original);
		tempUserResource.setUserId(userId);
		tempUserResource.setResourceType(OpusCostantEnum.CHECKING_PORTRAIT_RES_TYPE);
		tempUserResource.setResourceId(resId > 0 ? resId : null);
		int tempId = LiveVideoService.addTempUserResource(tempUserResource);
		JsonObject obj = new JsonObject();
		if (tempId > 0) {
			obj.addProperty("pictureId", tempId);
		}
		obj.addProperty("TagCode", TagCodeEnum.SUCCESS);
		// 本次请求结束
		return obj;

	}

	public static JsonObject addPictureNew(int userId, int pictureType, String path_original, String pictureName) {
		boolean flag = LiveVideoService.addPicture(userId,pictureType,OpusCostantEnum.CHECKING_PHOTO_RESOURCEURL,pictureName);
		if(flag){
			JsonObject obj = new JsonObject();
			obj.addProperty("TagCode", TagCodeEnum.SUCCESS);
			obj.addProperty("pictureId", 1);
			return obj;
		}
		else {
			JsonObject obj = new JsonObject();
			obj.addProperty("TagCode", "04010010");
			return obj;
		}
	}

	public static JsonObject addPictureNewV2(int resId, int userId, int pictureType, String path_original, String pictureName) {

		boolean flag = LiveVideoService.addPictureV2(resId,userId,pictureType,OpusCostantEnum.CHECKING_PHOTO_RESOURCEURL,pictureName);
		if(flag){
			JsonObject obj = new JsonObject();
			obj.addProperty("TagCode", TagCodeEnum.SUCCESS);
			obj.addProperty("pictureId", 1);
			return obj;
		}
		else {
			JsonObject obj = new JsonObject();
			obj.addProperty("TagCode", "04010010");
			return obj;
		}
	}

	/**
	 * 添加录屏分享
	 * @param userId
	 * @param path_original
	 * @param videoName
	 * @return
	 */
	public static JsonObject addVideoTape(int userId, String path_original, String videoName) {
		boolean flag = LiveVideoService.addVideoTape(userId,path_original,videoName);
		if(flag){
			JsonObject obj = new JsonObject();
			obj.addProperty("TagCode", TagCodeEnum.SUCCESS);
			obj.addProperty("videoTapeId", 0);
			obj.addProperty("oldPath", "");
			return obj;
		}
		else {
			JsonObject obj = new JsonObject();
			obj.addProperty("TagCode", "04010010");
			return obj;
		}
	}

}

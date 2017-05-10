package com.melot.kkcx.service;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import com.melot.kktv.service.LiveVideoService;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.CollectionEnum;
import com.melot.kktv.util.Constant;
import com.melot.kktv.util.PictureTypeEnum;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;
import com.melot.kktv.util.mongodb.CommonDB;
import com.melot.opus.domain.TempUserResource;
import com.melot.opus.util.OpusCostantEnum;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.upyun.api.UpYun;

public class AlbumServices {
	
	private static UpYun upyun = new UpYun(Constant.YOUPAI_BUCKET, Constant.YOUPAI_USER_NAME, Constant.YOUPAI_USER_PWD);
	
	/** 日志记录对象 */
	private static Logger logger = Logger.getLogger(AlbumServices.class);
	
	public static JsonObject addIMChatImage(int userId, int pictureType, String path_original, String pictureName) {
		// 调用存储过程入库
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("userId", userId);
		map.put("pictureType", pictureType);
		map.put("path_original", path_original);
		map.put("fileName", pictureName);
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Album.addPicture", map);
			String TagCode = (String) map.get("TagCode");
			if (TagCode.equals(TagCodeEnum.SUCCESS)) {
				JsonObject obj = new JsonObject();
				obj.addProperty("TagCode", TagCodeEnum.SUCCESS);
				return obj;
			} else {
				logger.error("调用存储过程(Album.addPicture)未的到正常结果(TagCode:"+TagCode+") userId:" + userId + ",pictureType:" + pictureType + ",path_original:"+ path_original);
				JsonObject obj = new JsonObject();
				obj.addProperty("TagCode", "04010010");
				// 本次请求结束
				return obj;
			}
		} catch (SQLException e) {
			logger.error("添加图片回调入库失败(userId:" + userId + ",pictureType:" + pictureType + ",path_original:"
					+ path_original + e);
			JsonObject obj = new JsonObject();
			obj.addProperty("TagCode", "04010009");
			// 本次请求结束
			return obj;
		}
	}
	
	public static JsonObject addPortraitNew(int userId, String path_original, String pictureName) {
	    
	    TempUserResource tempUserResource = new TempUserResource();
	    
        // 增加TempUserResource 
	    tempUserResource.setAppId(AppIdEnum.AMUSEMENT);
	    tempUserResource.setPath(path_original);
	    tempUserResource.setUserId(userId);
	    tempUserResource.setResourceType(OpusCostantEnum.CHECKING_PORTRAIT_RES_TYPE);
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
		
		// 调用存储过程入库
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("userId", userId);
		map.put("pictureType", pictureType);
		map.put("path_original", OpusCostantEnum.CHECKING_PHOTO_RESOURCEURL);
		map.put("fileName", pictureName);
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Album.addPicture", map);
		} catch (SQLException e) {
			logger.error("添加图片回调入库失败(userId:" + userId + ",pictureType:" + pictureType + ",path_original:"
					+ path_original + e);
			JsonObject obj = new JsonObject();
			obj.addProperty("TagCode", "04010009");
			// 本次请求结束
			return obj;
		}
		String TagCode = (String) map.get("TagCode");
		if (TagCode.equals(TagCodeEnum.SUCCESS)) {
			Integer pictureId = (Integer) map.get("pictureId");
			if (pictureType == 2) {
			    TempUserResource tempUserResource = new TempUserResource();
			    // 增加TempUserResource 
			    tempUserResource.setAppId(AppIdEnum.AMUSEMENT);
			    tempUserResource.setPath(path_original);
			    tempUserResource.setUserId(userId);
			    tempUserResource.setResourceType(OpusCostantEnum.CHECKING_PHOTO_RES_TYPE);
			    tempUserResource.setResourceId(pictureId);
			    LiveVideoService.addTempUserResource(tempUserResource);
			}
			
			Date uploadTime = new Date();
			if(map.containsKey("uploadTime")) {
				uploadTime = (Date) map.get("uploadTime");
			}
			
			// 若主播上传相册,mongodb中存储相册记录
			path_original = OpusCostantEnum.CHECKING_PHOTO_RESOURCEURL;
			if (UserService.isActor(userId)) {
				int picCount = CommonDB.getInstance(CommonDB.CACHEDB).getCollection(CollectionEnum.ACTORALBUMRECORD)
						.find(new BasicDBObject("userId", userId)).count();
				if (pictureType == PictureTypeEnum.poster && picCount > 0) {
					pictureType = PictureTypeEnum.picture;
				}
				DBObject dbObj = new BasicDBObject();
				dbObj.put("userId", userId);
				dbObj.put("photoId", pictureId);
				dbObj.put("picType", pictureType);
				dbObj.put("clicks", 0);
				dbObj.put("comments", 0);
				dbObj.put("uploadTime", uploadTime.getTime());
				dbObj.put("photo_path_original", path_original);
				CommonDB.getInstance(CommonDB.CACHEDB).getCollection(CollectionEnum.ACTORALBUMRECORD).insert(dbObj);
			}
			// 上传照片不会自动发布动态，待照片审核通过会自动发布
			/*if (pictureType == PictureTypeEnum.picture 
					|| pictureType == PictureTypeEnum.poster) {
				if (map.containsKey("newsId") && map.get("newsId") != null) {
					try {
						Integer newsId = (Integer) map.get("newsId");	
						// 将最新动态保存到Mongodb中
						JsonObject jObject = new JsonObject();
						jObject.addProperty("newsId", newsId);
						jObject.addProperty("content", "上传了一张照片");
						jObject.addProperty("publishedTime", uploadTime.getTime());
						JsonObject resourceUrl = new JsonObject();
						resourceUrl.addProperty("path_original", path_original);
						resourceUrl.addProperty("path_1280", path_original + "!1280");
						resourceUrl.addProperty("path_272", path_original + "!272");
						resourceUrl.addProperty("path_128", path_original + "!128x96");
						jObject.addProperty("resourceUrl", resourceUrl.toString());
						DBObject updateDBObj = new BasicDBObject();
						updateDBObj.put("publishedTime", uploadTime.getTime());
						updateDBObj.put("latestNews", jObject.toString());
						updateDBObj.put("newsId", newsId);
						updateDBObj.put("newsType", NewsTypeEnum.UPLOAD_PHOTO);
						updateDBObj.put("commentCount", 0); // 评论数
						updateDBObj.put("rewardCount", 0); // 打赏数
						CommonDB.getInstance(CommonDB.CACHEDB).getCollection(CollectionEnum.USERLATESTNEWS).update(
								new BasicDBObject("userId", userId), 
								new BasicDBObject("$set", updateDBObj), true, false);
					} catch (Exception e) {}
				}
			}*/
			
			JsonObject obj = new JsonObject();
			obj.addProperty("TagCode", TagCodeEnum.SUCCESS);
			obj.addProperty("pictureId", (Integer) map.get("pictureId"));
			// 本次请求结束
			return obj;
		} else {
			logger.error("调用存储过程(Album.addPicture)未的到正常结果(TagCode:"+TagCode+") userId:" + userId + ",pictureType:" + pictureType + ",path_original:"+ path_original);
			JsonObject obj = new JsonObject();
			obj.addProperty("TagCode", "04010010");
			// 本次请求结束
			return obj;
		}
	}
	
	public static JsonObject addBackgroundNew(int userId, String path_original, String pictureName) {
		
		// 调用存储过程
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("userId", userId);
		map.put("path_original", path_original);
		map.put("fileName", pictureName);
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Album.addBackground", map);
		} catch (SQLException e) {
			logger.error("添加背景图片回调入库失败(userId:" + userId +  ",path_original:" + path_original, e);
			JsonObject obj = new JsonObject();
			obj.addProperty("TagCode", "04010009");
			// 本次请求结束
			return obj;
		}
		String TagCode = (String) map.get("TagCode");
		if (TagCode.equals(TagCodeEnum.SUCCESS)) {
			
			// 从oracle中获取最新用户信息,更新到redis的userInfo信息
			JsonObject updateUserInfo = new JsonObject();
			updateUserInfo.addProperty("background", path_original);
			updateUserInfo.addProperty("background_path_original", path_original);
			ProfileServices.updateRedisUserInfo(userId, updateUserInfo);
			
			JsonObject obj = new JsonObject();
			obj.addProperty("TagCode", TagCodeEnum.SUCCESS);
			obj.addProperty("pictureId", (Integer) map.get("pictureId"));
			// 本次请求结束
	
			String picUrl = (String) map.get("oldPath");
			if (picUrl != null && !picUrl.contains("checking")) {
				// ****** 可选设置 ******
				// 切换 API 接口的域名接入点，默认为自动识别接入点
				upyun.setApiDomain(UpYun.ED_AUTO);
				// 设置连接超时时间，默认为30秒
				upyun.setTimeout(60);
				// 设置是否开启debug模式，默认不开启
				upyun.setDebug(true);
				try {
				    int i = 0;
                    boolean flag = false;
                    while (i++ < 5) {
                        if(upyun.deleteFile("kktv" + picUrl)) {
                            flag = true;
                            break;
                        }
                    }
                    if (!flag) {
                        logger.error("[AlbumServices]: Failed to delete pictures[" + "kktv" + picUrl + "] from Youpai.");
                    }
				} catch (Exception e) {
					logger.error("[AlbumServices]: Failed to delete pictures[" + "kktv" + picUrl + "] from Youpai." + e);			
				}
			}
			
			return obj;
		} else {
			logger.error("调用存储过程(Album.addBackground)未的到正常结果(TagCode:"+TagCode+") userId:" + userId + ",path_original:" + path_original);
			JsonObject obj = new JsonObject();
			obj.addProperty("TagCode", "04010010");
			// 本次请求结束
			return obj;
		}
	}
	
	
}

package com.melot.kkcx.functions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.melot.kk.module.resource.service.ResourceNewService;
import com.melot.kk.opus.api.domain.UserPicture;
import com.melot.kkcx.service.AlbumServices;
import com.melot.kkcx.service.ProfileServices;
import com.melot.kktv.action.FamilyAction;
import com.melot.kktv.base.CommonStateCode;
import com.melot.kktv.base.Result;
import com.melot.kktv.service.ConfigService;
import com.melot.kktv.service.LiveVideoService;
import com.melot.kktv.util.*;
import com.melot.module.api.exceptions.MelotModuleException;
import com.melot.module.poster.driver.domain.PosterInfo;
import com.melot.module.poster.driver.service.PosterService;
import com.melot.sdk.core.util.MelotBeanFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.List;

public class AlbumFunctions {

	/** 日志记录对象 */
	private static Logger logger = Logger.getLogger(AlbumFunctions.class);

	private static String SEPARATOR = "/";

	@Resource
	private ResourceNewService resourceNewService;

	@Autowired
	private ConfigService configService;

	/**
	 * 删除照片(10004002)
	 *
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return 结果字符串
	 */
	public JsonObject deletePhoto(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "30001007");
			return result;
		}
		// 获取参数
		JsonElement photoIdje = jsonObject.get("photoId");
		int photoId;
		if (photoIdje != null && !photoIdje.isJsonNull() && !photoIdje.getAsString().equals("")) {
			// 验证数字
			try {
				photoId = photoIdje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "04020004");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "04020003");
			return result;
		}
		boolean flag = LiveVideoService.delPicture(photoId);
		if (flag) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			return result;
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
			return result;
		}
	}

	/**
	 * 获取用户照片列表(10004003)
	 *
	 * @param jsonObject 请求对象
	 * @return 结果字符串
	 */
	public JsonObject getUserPhotoList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		// 获取参数
		JsonElement userIdje = jsonObject.get("userId");
		JsonElement pageIndexje = jsonObject.get("pageIndex");

		JsonObject result = new JsonObject();

		// 验证参数
		int userId;
		int pageIndex;
		if (userIdje != null && !userIdje.isJsonNull() && !userIdje.getAsString().equals("")) {
			// 验证数字
			try {
				userId = userIdje.getAsInt();
			} catch (Exception e) {
				result.addProperty("TagCode", "04030002");
				return result;
			}
		} else {
			result.addProperty("TagCode", "04030001");
			return result;
		}
		if (pageIndexje != null && !pageIndexje.isJsonNull() && pageIndexje.getAsInt()!=0) {
			// 验证数字
			try {
				pageIndex = pageIndexje.getAsInt();
			} catch (Exception e) {
				result.addProperty("TagCode", "04030004");
				return result;
			}
		} else {
			result.addProperty("TagCode", "04030003");
			return result;
		}

		JsonArray jPhotoList = new JsonArray();
		int pictureCount = LiveVideoService.getPictureCount(userId);
		int pageTotal = pictureCount/12;
		if(pictureCount%12 > 0){
			pageTotal = pageTotal+1;
		}
		if(pageTotal > 0 && pageTotal>= pageIndex){
            List<UserPicture> photos = LiveVideoService.getPictureList(userId,12*(pageIndex-1),12);
            for(UserPicture photo:photos){
				JsonObject jObject = new JsonObject();
				jObject.addProperty("photoId", photo.getPhotoId());
				jObject.addProperty("photoName", photo.getPhotoName());
				jObject.addProperty("photo_path_original", ConfigHelper.getHttpdir() + photo.getPhoto_path_original());
				jObject.addProperty("photo_path_1280", ConfigHelper.getHttpdir() + photo.getPhoto_path_original()+"!1280");
				jObject.addProperty("photo_path_272", ConfigHelper.getHttpdir() + photo.getPhoto_path_original()+"!272");
				jObject.addProperty("photo_path_128", ConfigHelper.getHttpdir() + photo.getPhoto_path_original()+"!128x96");
				jObject.addProperty("uploadTime", photo.getUploadTime().getTime());
				jObject.addProperty("clicks", photo.getClicks());
				jObject.addProperty("comments", photo.getComments());
				jObject.addProperty("picType", photo.getPicType());
				// 以下字段用于兼容旧接口
				if (photo.getPicType() == 1) {
					jObject.addProperty("posterTag", 0);
				} else {
					jObject.addProperty("posterTag", 1);
				}
				jObject.addProperty("description", "");
				jObject.addProperty("photoURL", ConfigHelper.getHttpdir() + photo.getPhoto_path_original());
				jObject.addProperty("webthumburl", ConfigHelper.getHttpdir() + photo.getPhoto_path_original()+"!272");
				jObject.addProperty("mobilethumburl", ConfigHelper.getHttpdir() + photo.getPhoto_path_original()+"!128");
				jPhotoList.add(jObject);
			}
		}
		// 返回结果
		result.addProperty("pageTotal", pageTotal);
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		result.add("photoList", jPhotoList);
		return result;
	}

	/**
	 * 获取用户海报列表(10004015)
	 * @param jsonObject
	 * @param checkTag
	 * @return
	 * @throws Exception
	 */
	public JsonObject getUserPosterList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception{
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}

		int userId;
		JsonObject result = new JsonObject();
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
		} catch(CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch(Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}

		JsonArray arr = new JsonArray();
		PosterService posterService = MelotBeanFactory.getBean("posterService", PosterService.class);
		if (posterService != null) {
			List<PosterInfo> posterList = posterService.getPosterList(userId);
			if (posterList != null) {
				for (PosterInfo post :posterList) {
					JsonObject json = new JsonObject();
					json.addProperty("resId", post.getResId());
					json.addProperty("state", post.getState());
					if(post.getState() == 3){
						json.addProperty("posterPath", configService.getCheckUnpassPoster());
						json.addProperty("posterPath_256", configService.getCheckUnpassPoster() + "!256");
						json.addProperty("posterPath_128", configService.getCheckUnpassPoster() + "!128");
					}
					else {
						json.addProperty("posterPath", post.getPathPrefix() + post.getUrl());
						json.addProperty("posterPath_256", post.getPathPrefix() + post.getUrl() + "!256");
						json.addProperty("posterPath_128", post.getPathPrefix() + post.getUrl() + "!128");
					}

					if (post.getReason() != null) {
						json.addProperty("reason", post.getReason());
					}
					arr.add(json);
				}
			}
			result.add("posterList", arr);
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		} else {
			result.addProperty("TagCode", "04150001");
		}

		return result;
	}

	/**
	 * 用户删除海报(10004016)
	 * @param jsonObject
	 * @param checkTag
	 * @param request
	 * @return
	 */
	public JsonObject deleteUserPoster(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}

		int userId, resId;
		JsonObject result = new JsonObject();
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
			resId = CommonUtil.getJsonParamInt(jsonObject, "resId", 0, "04160001", 1, Integer.MAX_VALUE);
		} catch(CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch(Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}

		PosterService posterService = MelotBeanFactory.getBean("posterService", PosterService.class);
		boolean flag;
		if (posterService != null) {
			try {
				flag = posterService.delPosterV2(userId, resId);
			} catch (MelotModuleException e) {
				switch (e.getErrCode()) {
					case 101:
						//resId不存在
						result.addProperty("TagCode", "04160002");
						break;

					case 102:
						//resId不属于userId
						result.addProperty("TagCode", "04160003");
						break;

					case 103:
						//resId为当前海报，不可删除
						result.addProperty("TagCode", "04160006");
						break;
				}
				return result;
			}
			if (flag == true) {
				try {
					// 将亚马逊的S3、又拍云等服务器资源文件删除
					resourceNewService.delResource(resId);
				} catch (Exception e) {
					logger.error("[AlbumFunctions]: Failed to delete poster[" + resId + "]" + e);
				}
				result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			} else {
				result.addProperty("TagCode", "04160004");
			}
		} else {
			result.addProperty("TagCode", "04160005");
		}
		return result;
	}

	/**
	 * 设置用户海报为当前海报(10004017)
	 * @param jsonObject
	 * @param checkTag
	 * @param request
	 * @return
	 */
	public JsonObject setUserPoster(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}

		int userId, resId;
		JsonObject result = new JsonObject();
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
			resId = CommonUtil.getJsonParamInt(jsonObject, "resId", 0, "04160001", 1, Integer.MAX_VALUE);
		} catch(CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch(Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}

		PosterService posterService = MelotBeanFactory.getBean("posterService", PosterService.class);
		boolean flag;
		if (posterService != null) {
			try {
				flag = posterService.setPoster(userId, resId);
			} catch (MelotModuleException e) {
				switch (e.getErrCode()) {
					case 101:
						//resId不存在
						result.addProperty("TagCode", "04170002");
						break;

					case 102:
						//resId不属于userId
						result.addProperty("TagCode", "04170003");
						break;

					case 103:
						//正在审核中，不能设为当前海报
						result.addProperty("TagCode", "04170006");
						break;

					case 104:
						//审核未通过，不能设为当前海报
						result.addProperty("TagCode", "04170007");
						break;

					case 105:
						//已经是当前海报
						result.addProperty("TagCode", "04170008");
						break;
				}
				return result;
			}
			if (flag == true) {
				result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			} else {
				result.addProperty("TagCode", "04170004");
			}
		} else {
			result.addProperty("TagCode", "04170005");
		}
		return result;
	}

	/**
	 * 	图片信息存入数据库(10004013)
	 * @param jsonObject
	 * @param checkTag
	 * @param request
	 * @return
	 */
	public JsonObject insertToDB(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception{
		JsonObject result = new JsonObject();

		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}

		// 验证参数
		int userId, familyId = 0, pictureType, appId;
		String url = null;
		String pictureName = null;

		try {
			appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 0, Integer.MAX_VALUE);
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
			pictureType = CommonUtil.getJsonParamInt(jsonObject, "pictureType", 0, "04010004", -10, Integer.MAX_VALUE);
			if (pictureType == PictureTypeEnum.family_poster) {
				familyId = CommonUtil.getJsonParamInt(jsonObject, "familyId", 0, "04010016", 1, Integer.MAX_VALUE);
			}

			url = CommonUtil.getJsonParamString(jsonObject, "url", null, "04010024", 1, Integer.MAX_VALUE);
			url = url.replaceFirst(ConfigHelper.getHttpdir(), "");
			if(!url.startsWith("/")){
				url = "/"+url;
			}
			url = url.replaceFirst("/kktv", "");
			File tempFile = new File(url);
			pictureName = tempFile.getName();
		} catch(CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch(Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		
		//特殊时期接口暂停使用（官方号不限制）
        if (configService.getIsSpecialTime() && !ProfileServices.checkIsOfficial(userId)) {
            if (pictureType == PictureTypeEnum.family_poster || pictureType == 2) {
                result.addProperty("message", "系统维护中，本功能暂时停用");
                result.addProperty("TagCode", TagCodeEnum.FUNCTAG_UNUSED_EXCEPTION);
                return result;
            } else if (pictureType == PictureTypeEnum.portrait) {
//                UserProfile userProfile = UserService.getUserInfoNew(userId);
//                if (userProfile != null && userProfile.getPortrait() != null) {
//                    result.addProperty("message", "系统维护中，本功能暂时停用");
//                    result.addProperty("TagCode", TagCodeEnum.FUNCTAG_UNUSED_EXCEPTION);
//                    return result; 
//                }
                if (ProfileServices.checkUserUpdateProfileByType(userId, "2")) {
                    result.addProperty("message", "该用户操作次数超过当日限制");
                    result.addProperty("TagCode", TagCodeEnum.FUNCTAG_LIMIT_EXCEPTION);
                    return result; 
                } else {
                    ProfileServices.setUserUpdateProfileByType(userId, "2");
                }
            }
        }

		Integer resId = 0;
        if(configService.getResourceType().contains(","+ pictureType+",")){
			com.melot.kk.module.resource.domain.Resource resource = new com.melot.kk.module.resource.domain.Resource();
			resource.setImageUrl(url);
			resource.setUserId(userId);
			resource.setResType(pictureType);
			resource.setMimeType(2);
			resource.seteCloudType(2);
			try{
				Result<Integer> r =resourceNewService.addResource(resource);
				if(r!= null && r.getCode().equals(CommonStateCode.SUCCESS) && r.getData()!= null){
					resId = r.getData();
				}
				else {
					logger.error("Failed to insert to resource DB.");
					result.addProperty("TagCode", TagCodeEnum.UNCATCHED_EXCEPTION);
				}
			}catch (Exception e){
				logger.error("Failed to insert to resource DB." + e);
				result.addProperty("TagCode", TagCodeEnum.UNCATCHED_EXCEPTION);
			}
		}


		// 0.头像 1.直播海报(弃用) 2.照片3.资源图片4.背景图
		if (pictureType == PictureTypeEnum.portrait) { // 0 : 头像
			try {
				result = AlbumServices.addPortraitNew(resId, userId, url, pictureName);
			} catch (Exception e) {
				logger.error("Failed to insert to DB.", e);
				result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			}
		} else if (pictureType == PictureTypeEnum.family_poster) { // 5:家族海报
			com.melot.family.driver.domain.FamilyPoster familyPoster = new com.melot.family.driver.domain.FamilyPoster();
			try {
				familyPoster.setPath_original(url);
				FamilyAction familyAction = MelotBeanFactory.getBean("familyFunction", FamilyAction.class);
				result = familyAction.setFamilyPoster(userId, familyId, familyPoster);
			} catch (Exception e) {
				logger.debug("Failed to insert to DB." + e);
				result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			}
		} else if (pictureType == PictureTypeEnum.imchat) { // 6:群聊
			try {
				result = AlbumServices.addIMChatImage(userId, pictureType, url, pictureName);
			} catch (Exception e) {
				logger.error("Failed to insert to DB." + e);
				result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			}
		}else if (pictureType == PictureTypeExtendEnum.video_tape) {// 9:录屏分享视频
			try {
				result = AlbumServices.addVideoTape(userId, url, pictureName);
			} catch (Exception e) {
				logger.error("Failed to insert to DB." + e);
				result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			}
		} else { // 1.直播海报(弃用) 2.照片 3.资源图片
			try {
			    if (pictureType == 3 || pictureType == 10 || pictureType == 7|| pictureType == 12|| pictureType == 13 || pictureType == 99|| pictureType == 4) {
					result.addProperty("TagCode", TagCodeEnum.SUCCESS);
					result.addProperty("pictureId", 1); // 必须返回一个值否则客户端会报错
				} else if(pictureType == 8){
					result = AlbumServices.addPictureNew(userId, pictureType, url, pictureName);
				}
				else {
					result = AlbumServices.addPictureNewV2(resId,userId, pictureType, url, pictureName);
				}
			} catch (Exception e) {
				logger.debug("Failed to insert to DB." + e);
				result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			}
		}

		return result;
	}

	/**
	 * 图片上传V2（10004022）
	 * @param jsonObject
	 * @param checkTag
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public JsonObject insertToDBV2(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception{
		JsonObject result = new JsonObject();

		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}

		// 验证参数
		int userId, pictureType, resId,familyId = 0;
		String fileUrl, pictureName;

		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
			resId = CommonUtil.getJsonParamInt(jsonObject, "resId", 0, null, 1, Integer.MAX_VALUE);
			pictureType = CommonUtil.getJsonParamInt(jsonObject, "pictureType", 0, "04010004", -10, Integer.MAX_VALUE);
			if (pictureType == PictureTypeEnum.family_poster) {
				familyId = CommonUtil.getJsonParamInt(jsonObject, "familyId", 0, "04010016", 1, Integer.MAX_VALUE);
			}
			fileUrl = CommonUtil.getJsonParamString(jsonObject, "fileUrl", null, "04010024", 1, Integer.MAX_VALUE);
			fileUrl = fileUrl.replaceFirst(ConfigHelper.getHttpdir(), "");
			if(!fileUrl.startsWith("/")){
				fileUrl = "/"+fileUrl;
			}
			fileUrl = fileUrl.replaceFirst("/kktv", "");
			pictureName = new File(fileUrl).getName();
		} catch(CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch(Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		
		//特殊时期接口暂停使用（官方号不限制）
        if (configService.getIsSpecialTime() && !ProfileServices.checkIsOfficial(userId)) {
            if (pictureType == PictureTypeEnum.family_poster || pictureType == 2) {
                result.addProperty("message", "系统维护中，本功能暂时停用");
                result.addProperty("TagCode", TagCodeEnum.FUNCTAG_UNUSED_EXCEPTION);
                return result;
            } else if (pictureType == PictureTypeEnum.portrait) {
//                UserProfile userProfile = UserService.getUserInfoNew(userId);
//                if (userProfile != null && userProfile.getPortrait() != null) {
//                    result.addProperty("message", "系统维护中，本功能暂时停用");
//                    result.addProperty("TagCode", TagCodeEnum.FUNCTAG_UNUSED_EXCEPTION);
//                    return result; 
//                }
                if (ProfileServices.checkUserUpdateProfileByType(userId, "2")) {
                    result.addProperty("message", "该用户操作次数超过当日限制");
                    result.addProperty("TagCode", TagCodeEnum.FUNCTAG_LIMIT_EXCEPTION);
                    return result; 
                } else {
                    ProfileServices.setUserUpdateProfileByType(userId, "2");
                }
            }
        }

		// 0:头像 2:相册图片
		try {
			if(!fileUrl.startsWith(SEPARATOR)) {
				fileUrl = SEPARATOR + fileUrl;
			}
			if (pictureType == PictureTypeEnum.portrait) { // 0:头像
				result = AlbumServices.addPortraitNew(resId, userId, fileUrl, pictureName);
			} else if(pictureType == 2) { // 2:相册图片
				result = AlbumServices.addPictureNewV2(resId, userId, pictureType, fileUrl, pictureName);
			} else if (pictureType == PictureTypeEnum.family_poster) { // 5:家族海报
				com.melot.family.driver.domain.FamilyPoster familyPoster = new com.melot.family.driver.domain.FamilyPoster();
				familyPoster.setPath_original(fileUrl);
				FamilyAction familyAction = MelotBeanFactory.getBean("familyFunction", FamilyAction.class);
				result = familyAction.setFamilyPoster(userId, familyId, familyPoster);
			} else if (pictureType == PictureTypeExtendEnum.video_tape) {// 9:录屏分享视频
				result = AlbumServices.addVideoTape(userId, fileUrl, pictureName);
			}
			else { // 1.直播海报(弃用) 2.照片 3.资源图片
				// 10的时候是技能服务接口，不添加到个人秀中
				if (pictureType == 3 || pictureType == 10 || pictureType == 7 || pictureType == 12|| pictureType == 13|| pictureType == 99) {
					result.addProperty("TagCode", TagCodeEnum.SUCCESS);
					result.addProperty("pictureId", 1); // 必须返回一个值否则客户端会报错
				} else if(pictureType == 8){
					result = AlbumServices.addPictureNew(userId, pictureType, fileUrl, pictureName);
				} else {
					result = AlbumServices.addPictureNewV2(resId, userId, pictureType, fileUrl, pictureName);
				}
			}
		} catch (Exception e) {
			logger.error("Failed to insert to DB." + e);
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
		}

		return result;
	}

	/**
	 * 新版海报入库V2（10004021）
	 * @param jsonObject
	 * @param checkTag
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public JsonObject insertToDBNewV2(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		JsonObject result = new JsonObject();

		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}

		int userId, resId;
		String fileUrl, pathPrefix;
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "04130001", 1, Integer.MAX_VALUE);
			resId = CommonUtil.getJsonParamInt(jsonObject, "resId", 0, "04130004", 1, Integer.MAX_VALUE);
			fileUrl = CommonUtil.getJsonParamString(jsonObject, "fileUrl", null, "04130002", 1, Integer.MAX_VALUE);
			fileUrl = fileUrl.replaceFirst(ConfigHelper.getHttpdir(), "");
			if(!fileUrl.startsWith("/")){
				fileUrl = "/"+fileUrl;
			}
			fileUrl = fileUrl.replaceFirst("/kktv", "");
			pathPrefix = ConfigHelper.getHttpdir();
		} catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch (Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}

		PosterService posterService = MelotBeanFactory.getBean("posterService", PosterService.class);

        //特殊时期用户没有上传过海报可上传一次
        if (configService.getIsSpecialTime() && !ProfileServices.checkIsOfficial(userId)) {
//            try {
//                List<PosterInfo> posterList = posterService.getPosterList(userId);
//                //海报池有可用海报
//                if (posterList != null && posterList.size() > 0) {
//                    for (PosterInfo posterInfo : posterList) {
//                        if (posterInfo.getState() != 3) {
//                            result.addProperty("TagCode", TagCodeEnum.FUNCTAG_UNUSED_EXCEPTION);
//                            return result;
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                logger.error("call PosterService getPosterList error userId:" + userId, e);
//            }
            if (ProfileServices.checkUserUpdateProfileByType(userId, "3")) {
                result.addProperty("message", "该用户操作次数超过当日限制");
                result.addProperty("TagCode", TagCodeEnum.FUNCTAG_LIMIT_EXCEPTION);
                return result; 
            } else {
                ProfileServices.setUserUpdateProfileByType(userId, "3");
            }
        }
        
		try {
			if(!fileUrl.startsWith(SEPARATOR)) {
				fileUrl = SEPARATOR + fileUrl;
			}
			String tagCode = posterService.savePosterV2(userId, resId, pathPrefix, fileUrl, 0);
			if (tagCode != null) {
				if (tagCode.equals("00000000")) {
					// 入库成功
					result.addProperty("TagCode", TagCodeEnum.SUCCESS);
					return result;
				} else if (tagCode.equals("10010001")) {
					// 非主播不能上传海报
					result.addProperty("TagCode", "04130003");
					return result;
				} else if (tagCode.equals("10010002")) {
					//海报池达到峰值
					result.addProperty("TagCode", "04120006");
					return result;
				}
			} else {
				//调用模块未得到正常结果
				result.addProperty("TagCode", "04130101");
				return result;
			}
		} catch (Exception e) {
			result.addProperty("TagCode", "04130101");
			logger.error("call PosterService savePoster error userId:" + userId + ",resId:" + resId + ",fileUrl:"+ fileUrl, e);
		}

		return result;
	}

}

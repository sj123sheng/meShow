package com.melot.kkcx.functions;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.melot.kk.module.resource.service.ResourceNewService;
import com.melot.kk.opus.api.constant.OpusCostantEnum;
import com.melot.kk.opus.api.domain.TempUserResource;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcx.service.AlbumServices;
import com.melot.kkcx.service.ProfileServices;
import com.melot.kkcx.service.UserService;
import com.melot.kktv.action.FamilyAction;
import com.melot.kktv.base.CommonStateCode;
import com.melot.kktv.base.Result;
import com.melot.kktv.model.FamilyPoster;
import com.melot.kktv.model.Photo;
import com.melot.kktv.model.PhotoComment;
import com.melot.kktv.service.ConfigService;
import com.melot.kktv.service.LiveVideoService;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.Constant;
import com.melot.kktv.util.PictureTypeEnum;
import com.melot.kktv.util.PictureTypeExtendEnum;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;
import com.melot.module.api.exceptions.MelotModuleException;
import com.melot.module.poster.driver.domain.PosterInfo;
import com.melot.module.poster.driver.domain.UpYunInfo;
import com.melot.module.poster.driver.service.PosterService;
import com.melot.sdk.core.util.MelotBeanFactory;

public class AlbumFunctions {

	/** 日志记录对象 */
	private static Logger logger = Logger.getLogger(AlbumFunctions.class);

	private static String SEPARATOR = "/";

	@Resource
	private ResourceNewService resourceNewService;

	@Autowired
	private ConfigService configService;

	@SuppressWarnings("unused")
	private static JsonObject addBackground(int userId, int fileId, String fileName, String path_original) {
		// 调用存储过程入库
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("userId", userId);
		map.put("fileId", fileId);
		map.put("fileName", fileName);
		map.put("path_original", path_original);
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Album.addBackground", map);
		} catch (SQLException e) {
			logger.error("添加背景图片回调入库失败(userId:" + userId + ",fileId:" + fileId + ",fileName:" + fileName + ",path_original:" + path_original, e);
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
			return obj;
		}
		logger.error("调用存储过程(Album.addBackground(" + new Gson().toJson(map) + "))未的到正常结果(userId:" + userId + ",fileId:" + fileId + ",fileName:" + fileName + ",path_original:" + path_original);
		JsonObject obj = new JsonObject();
		obj.addProperty("TagCode", "04010009");
		// 本次请求结束
		return obj;
	}

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
		JsonElement userIdje = jsonObject.get("userId");
		JsonElement photoIdje = jsonObject.get("photoId");

		// 验证参数
		int userId;
		int photoId;
		if (userIdje != null && !userIdje.isJsonNull() && !userIdje.getAsString().equals("")) {
			// 验证数字
			try {
				userId = userIdje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "04020002");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "04020001");
			return result;
		}
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

		// 调用存储过程得到结果
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("userId", userId);
		map.put("photoId", photoId);

		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Album.deletePhoto", map);
		} catch (SQLException e) {
			logger.error("未能正常调用存储过程", e);
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			return result;
		}
		String TagCode = (String) map.get("TagCode");
		if (TagCode.equals(TagCodeEnum.SUCCESS)) {
			TempUserResource tempUserResource = LiveVideoService.getTempUserResourceById(null, userId, OpusCostantEnum.CHECKING_PHOTO_RES_TYPE, photoId, null);
			if (tempUserResource != null) {
				LiveVideoService.delTempUserResourceById(tempUserResource.getId(), userId, OpusCostantEnum.CHECKING_PHOTO_RES_TYPE, photoId, null);
			}
			try {
				resourceNewService.delResource(photoId);
			} catch (Exception e) {
				logger.error("[AlbumFunctions]: Failed to delete photoId:[" + photoId + "]" + e);
			}
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			return result;
		} else if (TagCode.equals("03") || TagCode.equals("04")) {
			/* '03'; -- 照片不属于该用户 */
			/* '04'; -- 照片不存在 */
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "040201" + TagCode + "");
			return result;
		} else {
			// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
			logger.error("调用存储过程(Album.deletePhoto(" + new Gson().toJson(map) + "))未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
			return result;
		}
	}

	/**
	 * 删除照片V2(10004023)
	 *
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return 结果字符串
	 */
	public JsonObject deletePhotoV2(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "30001007");
			return result;
		}

		// 获取参数
		JsonElement userIdje = jsonObject.get("userId");
		JsonElement photoIdje = jsonObject.get("photoId");

		// 验证参数
		int userId;
		int photoId;
		if (userIdje != null && !userIdje.isJsonNull() && !userIdje.getAsString().equals("")) {
			// 验证数字
			try {
				userId = userIdje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "04020002");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "04020001");
			return result;
		}
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

		// 调用存储过程得到结果
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("userId", userId);
		map.put("photoId", photoId);

		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Album.deletePhoto", map);
		} catch (SQLException e) {
			logger.error("未能正常调用存储过程", e);
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			return result;
		}
		String TagCode = (String) map.get("TagCode");
		if (TagCode.equals(TagCodeEnum.SUCCESS)) {
			TempUserResource tempUserResource = LiveVideoService.getTempUserResourceById(null, userId, OpusCostantEnum.CHECKING_PHOTO_RES_TYPE, photoId, null);
			if (tempUserResource != null) {
				LiveVideoService.delTempUserResourceById(tempUserResource.getId(), userId, OpusCostantEnum.CHECKING_PHOTO_RES_TYPE, photoId, null);
			}
			try {
				// 将亚马逊的S3、又拍云等服务器资源文件删除
				resourceNewService.delResource(photoId);
			} catch (Exception e) {
				logger.error("[AlbumFunctions]: Failed to delete photoId:[" + photoId + "]" + e);
			}

			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			return result;
		} else if (TagCode.equals("03") || TagCode.equals("04")) {
			/* '03'; -- 照片不属于该用户 */
			/* '04'; -- 照片不存在 */
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "040201" + TagCode + "");
			return result;
		} else {
			// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
			logger.error("调用存储过程(Album.deletePhoto(" + new Gson().toJson(map) + "))未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
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

		// 调用存储过程得到结果
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("userId", userId);
		map.put("pageIndex", pageIndex);
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Album.getUserPhotoList", map);
		} catch (SQLException e) {
			logger.error("未能正常调用存储过程", e);
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			return result;
		}
		String TagCode = (String) map.get("TagCode");
		if (TagCode.equals(TagCodeEnum.SUCCESS)) {
			// 取出列表
			@SuppressWarnings("unchecked")
			List<Object> photoList = (ArrayList<Object>) map.get("photoList");
			for (Object object : photoList) {
				Photo photo = (Photo) object;
				JsonObject pObj = photo.toJsonObject();
				jPhotoList.add(pObj);
			}
			result.addProperty("pageTotal", (Integer) map.get("pageTotal"));
		} else if (TagCode.equals("02")) {
			/* '02';分页超出范围 */
			result.addProperty("pageTotal", (Integer) map.get("pageTotal"));
		} else {
			// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
			logger.error("调用存储过程(Album.getUserPhotoList(" + new Gson().toJson(map) + "))未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
			result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
			return result;
		}
		// 返回结果
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		result.add("photoList", jPhotoList);
		return result;
	}

	/**
	 * 评论照片
	 *
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return 结果字符串
	 */
	public JsonObject commentPhoto(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}

		// 获取参数
		JsonElement userIdje = jsonObject.get("userId");
		JsonElement photoIdje = jsonObject.get("photoId");
		JsonElement contentje = jsonObject.get("content");

		// 验证参数
		int userId;
		int photoId;
		String content;
		if (userIdje != null && !userIdje.isJsonNull() && !userIdje.getAsString().equals("")) {
			// 验证数字
			try {
				userId = userIdje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "04040002");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "04040001");
			return result;
		}
		if (photoIdje != null && !photoIdje.isJsonNull() && !photoIdje.getAsString().equals("")) {
			// 验证数字
			try {
				photoId = photoIdje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "04040004");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "04040003");
			return result;
		}
		if (contentje != null && !contentje.isJsonNull() && !contentje.getAsString().equals("")) {
			content = contentje.getAsString();
			// matchXSSTag
			if(CommonUtil.matchXSSTag(content)) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "04040006");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "04040005");
			return result;
		}

		// 调用存储过程得到结果
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("userId", userId);
		map.put("photoId", photoId);
		map.put("content", content);
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Album.commentPhoto", map);
		} catch (SQLException e) {
			logger.error("未能正常调用存储过程", e);
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			return result;
		}
		String TagCode = (String) map.get("TagCode");
		JsonObject result = new JsonObject();
		if (TagCode.equals(TagCodeEnum.SUCCESS)) {
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			return result;
		} else if (TagCode.equals("03")) {
			/* '03'; 照片不存在 */
			result.addProperty("TagCode", "040401" + TagCode + "");
			return result;
		} else {
			// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
			logger.error("调用存储过程(Album.commentPhoto(" + new Gson().toJson(map) + "))未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
			result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
			return result;
		}
	}

	/**
	 * 删除照片评论
	 *
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return 结果字符串
	 */
	public JsonObject deletePhotoComment(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}

		// 获取参数
		JsonElement userIdje = jsonObject.get("userId");
		JsonElement photoCommentIdje = jsonObject.get("photoCommentId");

		// 验证参数
		int userId;
		int photoCommentId;
		if (userIdje != null && !userIdje.isJsonNull() && !userIdje.getAsString().equals("")) {
			// 验证数字
			try {
				userId = userIdje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "04050002");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "04050001");
			return result;
		}
		if (photoCommentIdje != null && !photoCommentIdje.isJsonNull() && !photoCommentIdje.getAsString().equals("")) {
			// 验证数字
			try {
				photoCommentId = photoCommentIdje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "04050004");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "04050003");
			return result;
		}

		// 调用存储过程得到结果
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("userId", userId);
		map.put("photoCommentId", photoCommentId);
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Album.deletePhotoComment", map);
		} catch (SQLException e) {
			logger.error("未能正常调用存储过程", e);
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			return result;
		}
		String TagCode = (String) map.get("TagCode");
		JsonObject result = new JsonObject();
		if (TagCode.equals(TagCodeEnum.SUCCESS)) {
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			return result;
		} else if (TagCode.equals("03") || TagCode.equals("04")) {
			/* '03'; --评论不存在 */
			/* '03'; --用户不是照片所有者,无权限删除 */
			result.addProperty("TagCode", "040501" + TagCode + "");
			return result;
		} else {
			// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
			logger.error("调用存储过程(Album.deletePhotoComment(" + new Gson().toJson(map) + "))未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
			result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
			return result;
		}
	}

	/**
	 * 获取获取照片详情(包含所有评论)
	 *
	 * @param jsonObject 请求对象
	 * @return 结果字符串
	 */
	public JsonObject getPhotoCommentList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		// 获取参数
		JsonElement photoIdje = jsonObject.get("photoId");

		// 验证参数
		int photoId;
		if (photoIdje != null && !photoIdje.isJsonNull() && !photoIdje.getAsString().equals("")) {
			// 验证数字
			try {
				photoId = photoIdje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "04060002");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "04060001");
			return result;
		}

		// 调用存储过程得到结果
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("photoId", photoId);
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Album.getPhotoCommentList", map);
		} catch (SQLException e) {
			logger.error("未能正常调用存储过程", e);
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			return result;
		}
		String TagCode = (String) map.get("TagCode");
		if (TagCode.equals(TagCodeEnum.SUCCESS)) {
			// 取出列表
			@SuppressWarnings("unchecked")
			List<Object> commentList = (ArrayList<Object>) map.get("commentList");

			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCode);
			JsonArray jCommentList = new JsonArray();
			for (Object object : commentList) {
				jCommentList.add(((PhotoComment) object).toJsonObject());
			}
			result.add("commentList", jCommentList);
			// 返回结果
			return result;
		} else if (TagCode.equals("03")) {
			/* '03'; -- 照片不存在 */
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "040601" + TagCode + "");
			return result;
		} else {
			// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
			logger.error("调用存储过程(Album.getPhotoCommentList(" + new Gson().toJson(map) + "))未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
			return result;
		}
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
	 * 用户删除海报V2(10004020)
	 * @param jsonObject
	 * @param checkTag
	 * @param request
	 * @return
	 */
	public JsonObject deleteUserPosterV2(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
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
                UserProfile userProfile = UserService.getUserInfoNew(userId);
                if (userProfile != null && userProfile.getPortrait() != null) {
                    result.addProperty("message", "系统维护中，本功能暂时停用");
                    result.addProperty("TagCode", TagCodeEnum.FUNCTAG_UNUSED_EXCEPTION);
                    return result; 
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
		} else if (pictureType == PictureTypeEnum.background) { // 4 : 背景图
			try {
				result = AlbumServices.addBackgroundNew(userId , url, pictureName);
			} catch (Exception e) {
				logger.error("Failed to insert to DB." + e);
				result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			}
		} else if (pictureType == PictureTypeEnum.family_poster) { // 5:家族海报
			FamilyPoster familyPoster = new FamilyPoster();
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
			 // 10的时候是技能服务接口，不添加到个人秀中
			    if (pictureType == 3 || pictureType == 10 || pictureType == 7) {
					// 动态图片不保存在user_picture中
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
                UserProfile userProfile = UserService.getUserInfoNew(userId);
                if (userProfile != null && userProfile.getPortrait() != null) {
                    result.addProperty("message", "系统维护中，本功能暂时停用");
                    result.addProperty("TagCode", TagCodeEnum.FUNCTAG_UNUSED_EXCEPTION);
                    return result; 
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
				FamilyPoster familyPoster = new FamilyPoster();
				familyPoster.setPath_original(fileUrl);
				FamilyAction familyAction = MelotBeanFactory.getBean("familyFunction", FamilyAction.class);
				result = familyAction.setFamilyPoster(userId, familyId, familyPoster);
			}
			else if (pictureType == PictureTypeEnum.family_poster) { // 5:家族海报
				FamilyPoster familyPoster = new FamilyPoster();
				familyPoster.setPath_original(fileUrl);
				FamilyAction familyAction = MelotBeanFactory.getBean("familyFunction", FamilyAction.class);
				result = familyAction.setFamilyPoster(userId, familyId, familyPoster);
			}
			else if (pictureType == PictureTypeExtendEnum.video_tape) {// 9:录屏分享视频
				result = AlbumServices.addVideoTape(userId, fileUrl, pictureName);
			}
			else { // 1.直播海报(弃用) 2.照片 3.资源图片
				// 10的时候是技能服务接口，不添加到个人秀中
				if (pictureType == 3 || pictureType == 10 || pictureType == 7) {
					// 动态图片不保存在user_picture中
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
	 * 新版获取又拍云上传参数（10004018）
	 * @param jsonObject
	 * @param checkTag
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public JsonObject getUpyunUploadParamsNew(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception{
		JsonObject result = new JsonObject();

		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}

		int userId, pictureType;
		String localUrl = null;
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "04120001", 1, Integer.MAX_VALUE);
			pictureType = CommonUtil.getJsonParamInt(jsonObject, "pictureType", 0, "04120002", 1, Integer.MAX_VALUE);
			localUrl = CommonUtil.getJsonParamString(jsonObject, "localUrl", null, "04120003", 1, Integer.MAX_VALUE);
		} catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch (Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}

		UpYunInfo upYunInfo = null;
		PosterService posterService = MelotBeanFactory.getBean("posterService", PosterService.class);
		if (posterService == null) {
			//调用模块异常
			result.addProperty("TagCode", "04130100");
			return result;
		}
		try {
			upYunInfo = posterService.getUploadPosterUrl(userId, localUrl, pictureType, 0);
			if (upYunInfo != null && upYunInfo.getTagCode() != null) {
				if (upYunInfo.getTagCode().equals("10010001")) {
					//非主播
					result.addProperty("TagCode", "04120005");
					return result;
				} else if (upYunInfo.getTagCode().equals("10010002")) {
					//海报池达到峰值
					result.addProperty("TagCode", "04120006");
					return result;
				} else if (upYunInfo.getTagCode().equals("10010004")) {
					//获取文件地址失败
					result.addProperty("TagCode", "04120004");
					return result;
				}
			} else {
				//调用模块未得到正常结果
				result.addProperty("TagCode", "04130101");
				return result;
			}
		} catch (Exception e) {
			logger.error("call PosterService getUploadPosterUrl error userId:" + userId + ",pictureType:" + pictureType + ",localUrl:" + localUrl, e);
		}

		if (upYunInfo.getPolicy() != null) {
			result.addProperty("policy", upYunInfo.getPolicy());
		}
		if (upYunInfo.getSignature() != null) {
			result.addProperty("signature", upYunInfo.getSignature());
		}
		if (upYunInfo.getUrl() != null) {
			result.addProperty("url", upYunInfo.getUrl());
		}
		result.addProperty("bucket", Constant.YOUPAI_BUCKET);
		result.addProperty("domain", Constant.YOUPAI_DOMAIN);
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);

		return result;
	}

	/**
	 * 新版海报入库（10004019）
	 * @param jsonObject
	 * @param checkTag
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public JsonObject insertToDBNew(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		JsonObject result = new JsonObject();

		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}

		int userId, pictureType;
		String url = null;
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "04130001", 1, Integer.MAX_VALUE);
			pictureType = CommonUtil.getJsonParamInt(jsonObject, "pictureType", 0, "04130004", 1, Integer.MAX_VALUE);
			url = CommonUtil.getJsonParamString(jsonObject, "url", null, "04130002", 1, Integer.MAX_VALUE);
			url = url.replaceFirst(ConfigHelper.getHttpdir(), "");
			if(!url.startsWith("/")){
				url = "/"+url;
			}
			url = url.replaceFirst("/kktv", "");
		} catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch (Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		
		PosterService posterService = MelotBeanFactory.getBean("posterService", PosterService.class);
		
		//特殊时期用户没有上传过海报可上传一次（官方号不限制）
		if (configService.getIsSpecialTime() && !ProfileServices.checkIsOfficial(userId)) {
		    try {
		        List<PosterInfo> posterList = posterService.getPosterList(userId);
		        //海报池有可用海报
                if (posterList != null && posterList.size() > 0) {
                    for (PosterInfo posterInfo : posterList) {
                        if (posterInfo.getState() != 3) {
                            result.addProperty("TagCode", TagCodeEnum.FUNCTAG_UNUSED_EXCEPTION);
                            return result;
                        }
                    }
                }
		    } catch (Exception e) {
	            logger.error("call PosterService getPosterList error userId:" + userId, e);
	        }
		}

		Integer resId = 0;
		if(configService.getResourceType().contains(","+ pictureType+",")){
			com.melot.kk.module.resource.domain.Resource resource = new com.melot.kk.module.resource.domain.Resource();
			resource.setImageUrl(url);
			resource.setUserId(userId);
			resource.setResType(1);
			resource.setMimeType(2);
			resource.seteCloudType(2);
			try{
				Result<Integer> r =resourceNewService.addResource(resource);
				if(r!= null && r.getCode().equals(CommonStateCode.SUCCESS)){
					resId = r.getData();
				}
			}catch (Exception e){
				logger.debug("Failed to insert to resource DB." + e);
				result.addProperty("TagCode", TagCodeEnum.UNCATCHED_EXCEPTION);
			}
		}

		if (posterService == null) {
			//调用模块异常
			result.addProperty("TagCode", "04130100");
			return result;
		}
		try {
			String tagCode = posterService.savePosterV2(userId, resId, ConfigHelper.getHttpdir(), url, 0);;
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
			logger.error("call PosterService savePoster error userId:" + userId + ",pictureType:" + pictureType + ",url:"+ url, e);
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
            try {
                List<PosterInfo> posterList = posterService.getPosterList(userId);
                //海报池有可用海报
                if (posterList != null && posterList.size() > 0) {
                    for (PosterInfo posterInfo : posterList) {
                        if (posterInfo.getState() != 3) {
                            result.addProperty("TagCode", TagCodeEnum.FUNCTAG_UNUSED_EXCEPTION);
                            return result;
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("call PosterService getPosterList error userId:" + userId, e);
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

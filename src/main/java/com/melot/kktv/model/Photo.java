package com.melot.kktv.model;

import java.util.Date;

import com.google.gson.JsonObject;
import com.melot.kktv.util.ConfigHelper;

/**
 * 照片信息类
 * 
 * @author liyue
 * 
 */
public class Photo {
	private Integer photoId;
	private String photoName;
	private String photo_path_original;
	private String photo_path_1280;
	private String photo_path_272;
	private String photo_path_128;
	private Date uploadTime;
	private Integer clicks;
	private Integer comments;
	private Integer picType;

	/**
	 * 转成JsonObject
	 * 
	 * @return JsonObject
	 */
	public JsonObject toJsonObject() {
		JsonObject jObject = new JsonObject();
		jObject.addProperty("photoId", this.getPhotoId());
		jObject.addProperty("photoName", this.getPhotoName());
		jObject.addProperty("photo_path_original", ConfigHelper.getHttpdir() + this.getPhoto_path_original());
		jObject.addProperty("photo_path_1280", ConfigHelper.getHttpdir() + this.getPhoto_path_1280());
		jObject.addProperty("photo_path_272", ConfigHelper.getHttpdir() + this.getPhoto_path_272());
		jObject.addProperty("photo_path_128", ConfigHelper.getHttpdir() + this.getPhoto_path_128());
		jObject.addProperty("uploadTime", this.getUploadTime().getTime());
		jObject.addProperty("clicks", this.getClicks());
		jObject.addProperty("comments", this.getComments());
		jObject.addProperty("picType", this.getPicType());
		// 以下字段用于兼容旧接口
		if (this.getPicType() == 1) {
			jObject.addProperty("posterTag", 0);
		} else {
			jObject.addProperty("posterTag", 1);
		}
		jObject.addProperty("description", "");
		jObject.addProperty("photoURL", ConfigHelper.getHttpdir() + this.getPhoto_path_original());
		jObject.addProperty("webthumburl", ConfigHelper.getHttpdir() + this.getPhoto_path_272());
		jObject.addProperty("mobilethumburl", ConfigHelper.getHttpdir() + this.getPhoto_path_128());
		return jObject;
	}

	public Integer getPhotoId() {
		return photoId;
	}

	public void setPhotoId(Integer photoId) {
		this.photoId = photoId;
	}

	public String getPhotoName() {
		return photoName;
	}

	public void setPhotoName(String photoName) {
		this.photoName = photoName;
	}

	public String getPhoto_path_original() {
		return photo_path_original;
	}

	public void setPhoto_path_original(String photo_path_original) {
		this.photo_path_original = photo_path_original;
	}

	public String getPhoto_path_1280() {
		return photo_path_original + "!1280";
	}

	public void setPhoto_path_1280(String photo_path_original) {
		this.photo_path_1280 = photo_path_original + "!1280";
	}

	public String getPhoto_path_272() {
		return photo_path_original + "!272";
	}

	public void setPhoto_path_272(String photo_path_original) {
		this.photo_path_272 = photo_path_original + "!272";
	}

	public String getPhoto_path_128() {
		return photo_path_original + "!128x96";
	}

	public void setPhoto_path_128(String photo_path_original) {
		this.photo_path_128 = photo_path_original + "!128x96";
	}

	public Date getUploadTime() {
		return uploadTime;
	}

	public void setUploadTime(Date uploadTime) {
		this.uploadTime = uploadTime;
	}

	public Integer getClicks() {
		return clicks;
	}

	public void setClicks(Integer clicks) {
		this.clicks = clicks;
	}

	public Integer getComments() {
		return comments;
	}

	public void setComments(Integer comments) {
		this.comments = comments;
	}

	public Integer getPicType() {
		return picType;
	}

	public void setPicType(Integer picType) {
		this.picType = picType;
	}

}

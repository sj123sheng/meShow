package com.melot.kktv.model;

import java.util.Date;

import com.google.gson.JsonObject;
import com.melot.kktv.util.ConfigHelper;

/**
 * 照片评论信息类
 * 
 * @author liyue
 * 
 */
@SuppressWarnings("unused")
public class PhotoComment {
	private Integer commentId;
	private String content;
	private Date commentTime;
	private Integer userId;
	private String nickname;
	private String portrait_path_original;
	private String portrait_path_1280;
	private String portrait_path_256;
	private String portrait_path_128;
	private String portrait_path_48;

	/**
	 * 转成JsonObject
	 * 
	 * @return JsonObject
	 */
	public JsonObject toJsonObject() {
		JsonObject jObject = new JsonObject();
		jObject.addProperty("commentId", this.getCommentId());
		jObject.addProperty("content", this.getContent());
		if (this.getCommentTime() != null) {
			jObject.addProperty("commentTime", this.getCommentTime().getTime());
		}
		jObject.addProperty("userId", this.getUserId());
		jObject.addProperty("nickname", this.getNickname());
		if (this.getPortrait_path_original() != null) {
			jObject.addProperty("portrait_path_original", ConfigHelper.getHttpdir() + this.getPortrait_path_original());
			jObject.addProperty("portrait_path_1280", ConfigHelper.getHttpdir() + this.getPortrait_path_1280());
			jObject.addProperty("portrait_path_256", ConfigHelper.getHttpdir() + this.getPortrait_path_256());
			jObject.addProperty("portrait_path_128", ConfigHelper.getHttpdir() + this.getPortrait_path_128());
			jObject.addProperty("portrait_path_48", ConfigHelper.getHttpdir() + this.getPortrait_path_48());
			// 以下字段用于兼容旧接口
			jObject.addProperty("photomobilethumburl", ConfigHelper.getHttpdir() + this.getPortrait_path_128());
			jObject.addProperty("photowebthumburl", ConfigHelper.getHttpdir() + this.getPortrait_path_256());
		}
		return jObject;
	}

	public Integer getCommentId() {
		return commentId;
	}

	public void setCommentId(Integer commentId) {
		this.commentId = commentId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getCommentTime() {
		return commentTime;
	}

	public void setCommentTime(Date commentTime) {
		this.commentTime = commentTime;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getPortrait_path_original() {
		return portrait_path_original;
	}

	public void setPortrait_path_original(String portrait_path_original) {
		this.portrait_path_original = portrait_path_original;
	}

	public String getPortrait_path_1280() {
		return portrait_path_original + "!1280";
	}

	public void setPortrait_path_1280(String portrait_path_original) {
		this.portrait_path_1280 = portrait_path_original + "!1280";
	}

	public String getPortrait_path_256() {
		return portrait_path_original + "!256";
	}

	public void setPortrait_path_256(String portrait_path_original) {
		this.portrait_path_256 = portrait_path_original + "!256";
	}

	public String getPortrait_path_128() {
		return portrait_path_original + "!128";
	}

	public void setPortrait_path_128(String portrait_path_original) {
		this.portrait_path_128 = portrait_path_original + "!128";
	}

	public String getPortrait_path_48() {
		return portrait_path_original + "!48";
	}

	public void setPortrait_path_48(String portrait_path_original) {
		this.portrait_path_48 = portrait_path_original + "!48";
	}

}

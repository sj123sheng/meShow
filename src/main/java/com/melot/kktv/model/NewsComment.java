package com.melot.kktv.model;

import java.util.Date;

/**
 * 动态的评论类
 * 
 * @author liyue
 * 
 */
@SuppressWarnings("unused")
public class NewsComment {

	private Integer userId;
	private Integer userIdBelong;
	private Integer userIdTarget;
	private String nickname;
	private Integer gender;
	private String portrait_path_original;
	private String portrait_path_1280;
	private String portrait_path_256;
	private String portrait_path_128;
	private String portrait_path_48;
	private Integer commentId;
	private Integer newsId;
	private String content;
	private Date commentTime;
	
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
	public Integer getGender() {
		return gender;
	}

	public void setGender(Integer gender) {
		this.gender = gender;
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

	public Integer getCommentId() {
		return commentId;
	}

	public void setCommentId(Integer commentId) {
		this.commentId = commentId;
	}

	public Integer getNewsId() {
		return newsId;
	}

	public void setNewsId(Integer newsId) {
		this.newsId = newsId;
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
	
	public Integer getUserIdBelong() {
		return userIdBelong;
	}

	public void setUserIdBelong(Integer userIdBelong) {
		this.userIdBelong = userIdBelong;
	}

	public Integer getUserIdTarget() {
		return userIdTarget;
	}

	public void setUserIdTarget(Integer userIdTarget) {
		this.userIdTarget = userIdTarget;
	}
}

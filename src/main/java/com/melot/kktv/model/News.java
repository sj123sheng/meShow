package com.melot.kktv.model;

import java.util.Date;

/**
 * 动态类
 * 
 * @author liyue
 * 
 */
@SuppressWarnings("unused")
public class News {
	private Integer userId;
	private String nickname;
	private Integer gender;
	private String portrait_path_original;
	private String portrait_path_1280;
	private String portrait_path_256;
	private String portrait_path_128;
	private String portrait_path_48;
	private Integer newsId;
	private String content;
	private Date publishedTime;
	private Integer newsType;
	private Integer resourceId;
	private String resourceUrl;
	private Integer commentCount;
	private Integer rewardCount;
	private String mediaSource; // json string
	
	private Integer isRecNews; // 是否为推荐动态
	private String mediaTitle; // 视频标题
	private Integer viewTimes; // 观看次数
	
	private Integer customScore; // 用户自定义分数
	private Integer runScore; // 热拍程序计算所得分数
	
	private Integer mediaFrom; // 视频来源
	
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

	public Date getPublishedTime() {
		return publishedTime;
	}

	public void setPublishedTime(Date publishedTime) {
		this.publishedTime = publishedTime;
	}

	public Integer getNewsType() {
		return newsType;
	}

	public void setNewsType(Integer newsType) {
		this.newsType = newsType;
	}

	public Integer getResourceId() {
		return resourceId;
	}

	public void setResourceId(Integer resourceId) {
		this.resourceId = resourceId;
	}

	public String getResourceUrl() {
		return resourceUrl;
	}

	public void setResourceUrl(String resourceUrl) {
		this.resourceUrl = resourceUrl;
	}

	public Integer getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(Integer commentCount) {
		this.commentCount = commentCount;
	}

	public String getMediaSource() {
		return mediaSource;
	}

	public void setMediaSource(String mediaSource) {
		this.mediaSource = mediaSource;
	}

	public Integer getRewardCount() {
		return rewardCount;
	}

	public void setRewardCount(Integer rewardCount) {
		this.rewardCount = rewardCount;
	}

	public Integer getIsRecNews() {
		return isRecNews;
	}

	public void setIsRecNews(Integer isRecNews) {
		this.isRecNews = isRecNews;
	}

	public String getMediaTitle() {
		return mediaTitle;
	}

	public void setMediaTitle(String mediaTitle) {
		this.mediaTitle = mediaTitle;
	}

	public Integer getViewTimes() {
		return viewTimes;
	}

	public void setViewTimes(Integer viewTimes) {
		this.viewTimes = viewTimes;
	}

	public Integer getCustomScore() {
		return customScore;
	}

	public Integer getRunScore() {
		return runScore;
	}

	public void setCustomScore(Integer customScore) {
		this.customScore = customScore;
	}

	public void setRunScore(Integer runScore) {
		this.runScore = runScore;
	}

	public Integer getMediaFrom() {
		return mediaFrom;
	}

	public void setMediaFrom(Integer mediaFrom) {
		this.mediaFrom = mediaFrom;
	}
	
}

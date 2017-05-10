package com.melot.kktv.domain;

public class ActivityInfo {
	
	private Integer activityId; // 活动编号

	private String activityTitle; // 活动标题

	private String activityDesc; // 活动描述

	private String activityImg; // 活动页面图片

	private Long startTime; // 活动开始时间

	private Long endTime; // 活动结束时间

	private String bannerWeb; // 活动Web宣传图

	private String bannerAndroid; // 活动Android宣传图

	private String bannerIos; // 活动Ios宣传图

	private String detailWeb; // 活动Web详细文案

	private String detailAndroid; // 活动Android详细文案

	private String detailIos; // 活动Ios详细文案

	private String urlWeb; // 活动Web页面地址

	private String urlAndroid; // 活动Android页面地址

	private String urlIos; // 活动Ios页面地址

	public String getUrlIos() {
		return urlIos;
	}

	public void setUrlIos(String urlIos) {
		this.urlIos = urlIos;
	}

	public String getUrlAndroid() {
		return urlAndroid;
	}

	public void setUrlAndroid(String urlAndroid) {
		this.urlAndroid = urlAndroid;
	}

	public String getUrlWeb() {
		return urlWeb;
	}

	public void setUrlWeb(String urlWeb) {
		this.urlWeb = urlWeb;
	}

	public String getDetailIos() {
		return detailIos;
	}

	public void setDetailIos(String detailIos) {
		this.detailIos = detailIos;
	}

	public String getActivityTitle() {
		return activityTitle;
	}

	public void setActivityTitle(String activityTitle) {
		this.activityTitle = activityTitle;
	}

	public String getBannerAndroid() {
		return bannerAndroid;
	}

	public void setBannerAndroid(String bannerAndroid) {
		this.bannerAndroid = bannerAndroid;
	}

	public String getBannerIos() {
		return bannerIos;
	}

	public void setBannerIos(String bannerIos) {
		this.bannerIos = bannerIos;
	}

	public String getBannerWeb() {
		return bannerWeb;
	}

	public void setBannerWeb(String bannerWeb) {
		this.bannerWeb = bannerWeb;
	}

	public String getDetailWeb() {
		return detailWeb;
	}

	public void setDetailWeb(String detailWeb) {
		this.detailWeb = detailWeb;
	}

	public String getDetailAndroid() {
		return detailAndroid;
	}

	public void setDetailAndroid(String detailAndroid) {
		this.detailAndroid = detailAndroid;
	}

	public String getActivityImg() {
		return activityImg;
	}

	public void setActivityImg(String activityImg) {
		this.activityImg = activityImg;
	}

	public String getActivityDesc() {
		return activityDesc;
	}

	public void setActivityDesc(String activityDesc) {
		this.activityDesc = activityDesc;
	}

	public Integer getActivityId() {
		return activityId;
	}

	public void setActivityId(Integer activityId) {
		this.activityId = activityId;
	}

	public Long getStartTime() {
		return startTime;
	}

	public void setStartTime(Long startTime) {
		this.startTime = startTime;
	}

	public Long getEndTime() {
		return endTime;
	}

	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}
}

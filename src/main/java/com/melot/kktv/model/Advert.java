package com.melot.kktv.model;

public class Advert {
	
	private Integer userId;
	private Integer activityId;
	private String activityUrl;
	private String displayUrl;
	private Integer adType;
	
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public Integer getActivityId() {
		return activityId;
	}
	public void setActivityId(Integer activityId) {
		this.activityId = activityId;
	}
	public String getActivityUrl() {
		return activityUrl;
	}
	public void setActivityUrl(String activityUrl) {
		this.activityUrl = activityUrl;
	}
	public String getDisplayUrl() {
		return displayUrl;
	}
	public void setDisplayUrl(String displayUrl) {
		this.displayUrl = displayUrl;
	}
	public Integer getAdType() {
		return adType;
	}
	public void setAdType(Integer adType) {
		this.adType = adType;
	}

}

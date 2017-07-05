package com.melot.kktv.model;

public class SmsConfig {
	
	private Integer appId;
	private Integer channel;
	private Integer platform;
	private Integer dailyCount;
	private String message;
	private Integer smsType;
	private Integer activeTime;
	
	public Integer getAppId() {
		return appId;
	}
	public void setAppId(Integer appId) {
		this.appId = appId;
	}
	public Integer getChannel() {
		return channel;
	}
	public void setChannel(Integer channel) {
		this.channel = channel;
	}
	public Integer getPlatform() {
		return platform;
	}
	public void setPlatform(Integer platform) {
		this.platform = platform;
	}
	public Integer getDailyCount() {
		return dailyCount;
	}
	public void setDailyCount(Integer dailyCount) {
		this.dailyCount = dailyCount;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Integer getSmsType() {
		return smsType;
	}
	public void setSmsType(Integer smsType) {
		this.smsType = smsType;
	}
	public Integer getActiveTime() {
		return activeTime;
	}
	public void setActiveTime(Integer activeTime) {
		this.activeTime = activeTime;
	}
	
}

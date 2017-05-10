package com.melot.kktv.domain;

public class ActivityPlayInfo {

	private Integer activityId ; // 活动ID
	
	private Integer playId ; // 活动场次ID
	
	private String playTitle ; // 活动场次标题
	
	private Long startTime ; // 场次开始时间
	
	private Long endTime ; // 场次结束时间

	public Integer getActivityId() {
		return activityId;
	}

	public void setActivityId(Integer activityId) {
		this.activityId = activityId;
	}

	public Integer getPlayId() {
		return playId;
	}

	public void setPlayId(Integer playId) {
		this.playId = playId;
	}

	public String getPlayTitle() {
		return playTitle;
	}

	public void setPlayTitle(String playTitle) {
		this.playTitle = playTitle;
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

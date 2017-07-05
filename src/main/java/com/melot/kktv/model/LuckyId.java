package com.melot.kktv.model;

public class LuckyId {
	
	private Integer luckyId;
	private Long startTime;
	private Long endTime;
	private Long leftTime;
	
	private Integer idType; // 虚拟号类型
	private Integer isLight; // 点亮状态 
	
	public Integer getLuckyId() {
		return luckyId;
	}
	public void setLuckyId(Integer luckyId) {
		this.luckyId = luckyId;
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
	public Long getLeftTime() {
		return leftTime;
	}
	public void setLeftTime(Long leftTime) {
		this.leftTime = leftTime;
	}
	public Integer getIdType() {
		return idType;
	}
	public Integer getIsLight() {
		return isLight;
	}
	public void setIdType(Integer idType) {
		this.idType = idType;
	}
	public void setIsLight(Integer isLight) {
		this.isLight = isLight;
	}

}

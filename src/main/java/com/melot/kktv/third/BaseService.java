package com.melot.kktv.third;

public abstract class BaseService {
	
	/**
	 * 第三方名称
	 */
	private String thirdName;
	
	/**
	 * 第三方编号
	 */
	private int openPlatform;
	
	/**
	 * 开放平台 以逗号隔开 例:1,2
	 */
	private String limitPlatform;
	
	public String getThirdName() {
		return thirdName;
	}

	public void setThirdName(String thirdName) {
		this.thirdName = thirdName;
	}

	public int getOpenPlatform() {
		return openPlatform;
	}

	public void setOpenPlatform(int openPlatform) {
		this.openPlatform = openPlatform;
	}

	public String getLimitPlatform() {
		return limitPlatform;
	}

	public void setLimitPlatform(String limitPlatform) {
		this.limitPlatform = limitPlatform;
	}

	/**
	 * @param 用户唯一标识 openId
	 * @param 用户登录标识 sessionId
	 * @return
	 */
	public abstract String verifyUser(String openId, String sessionId);
	
}
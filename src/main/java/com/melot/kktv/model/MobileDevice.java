package com.melot.kktv.model;

public class MobileDevice {

	private Integer deviceId;// 设备ID
	private String imei;// 设备unique id
	private String model;// 设备型号
	private Integer osType;// 设备系统类型-0:android 1:iphone os
	private String osRelease;// 设备系统版本
	private String screenWidth;// 设备屏幕宽带
	private String screenHeight;// 设备屏幕高度

	private String installVersion;// 安装包版本
	private String installChannel;// 安装包渠道

	public Integer getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(Integer deviceId) {
		this.deviceId = deviceId;
	}

	public String getImei() {
		return imei;
	}

	public void setImei(String imei) {
		this.imei = imei;
	}

	public String getScreenWidth() {
		return screenWidth;
	}

	public void setScreenWidth(String screenWidth) {
		this.screenWidth = screenWidth;
	}

	public String getScreenHeight() {
		return screenHeight;
	}

	public void setScreenHeight(String screenHeight) {
		this.screenHeight = screenHeight;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public Integer getOsType() {
		return osType;
	}

	public void setOsType(Integer osType) {
		this.osType = osType;
	}

	public String getOsRelease() {
		return osRelease;
	}

	public void setOsRelease(String osRelease) {
		this.osRelease = osRelease;
	}

	public String getInstallVersion() {
		return installVersion;
	}

	public void setInstallVersion(String installVersion) {
		this.installVersion = installVersion;
	}

	public String getInstallChannel() {
		return installChannel;
	}

	public void setInstallChannel(String installChannel) {
		this.installChannel = installChannel;
	}

}

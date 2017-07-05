package com.melot.kktv.model;

public class Version {

	private Integer versionCode;
	private String versionName;
	private String versionDesc;
	private String versionUrl;
	private Integer versionPlat;
	private Integer versionSatus;

	public String getVersionDesc() {
		return versionDesc;
	}

	public void setVersionDesc(String versionDesc) {
		this.versionDesc = versionDesc;
	}

	public String getVersionUrl() {
		return versionUrl;
	}

	public void setVersionUrl(String versionUrl) {
		this.versionUrl = versionUrl;
	}

	public Integer getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(Integer versionCode) {
		this.versionCode = versionCode;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public Integer getVersionPlat() {
		return versionPlat;
	}

	public void setVersionPlat(Integer versionPlat) {
		this.versionPlat = versionPlat;
	}

	public Integer getVersionSatus() {
		return versionSatus;
	}

	public void setVersionSatus(Integer versionSatus) {
		this.versionSatus = versionSatus;
	}

}

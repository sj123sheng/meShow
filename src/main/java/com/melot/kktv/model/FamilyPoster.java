package com.melot.kktv.model;

import com.google.gson.JsonObject;
import com.melot.kktv.util.PlatformEnum;

/**
 * 家族海报对象
 * @author RC
 *
 */
public class FamilyPoster {
	
	private String path_original;
	private String path_174;
	private String path_222;
	private String path_270;
	
	public JsonObject toJsonObject(int platform) {
		JsonObject posterJson = new JsonObject();
		switch (platform) {
		case PlatformEnum.WEB:
			// 返回 222*148px 270*180px
			if (this.path_222 != null) { 
				posterJson.addProperty("path_222", this.path_222);
			}
			if (this.path_270 != null) {
				posterJson.addProperty("path_270", this.path_270);
			}
			break;
		case PlatformEnum.ANDROID:
			// 返回 222*148px
			if (this.path_222 != null) {
				posterJson.addProperty("path_222", this.path_222);
			}
			break;
		case PlatformEnum.IPHONE:
			// 返回 222*148px
			if (this.path_222 != null) {
				posterJson.addProperty("path_222", this.path_222);
			}
			break;
		case PlatformEnum.IPAD:
			// 返回 222*148px
			if (this.path_222 != null) {
				posterJson.addProperty("path_222", this.path_222);
			}
			break;
		default:
			// 返回 原尺寸
			if (this.path_original != null) {
				posterJson.addProperty("path_original", this.path_original);
			}
			break;
		}
		return posterJson;
	}
	
	public String getPath_original() {
		return path_original;
	}
	public void setPath_original(String path_original) {
		this.path_original = path_original;
	}
	public String getPath_174() {
		return path_original + "!174";
	}
	public void setPath_174(String path_original) {
		this.path_174 = path_original + "!174";
	}
	public String getPath_222() {
		return path_original + "!222";
	}
	public void setPath_222(String path_original) {
		this.path_222 = path_original + "!222";
	}
	public String getPath_270() {
		return  path_original + "!270";
	}
	public void setPath_270(String path_original) {
		this.path_270 = path_original + "!270";
	}
	
}

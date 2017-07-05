package com.melot.kktv.model;

import java.util.Date;
import com.google.gson.JsonObject;
import com.melot.kktv.util.PlatformEnum;

/**
 * kk小秘书类
 * 
 */
@SuppressWarnings("unused")
public class KkAssistor {
	
	private Integer id;
	private Integer userId;
	private String nickname;
	private Integer gender;
	private String portrait_path_original;
	private String portrait_path_1280;
	private String portrait_path_256;
	private String portrait_path_128;
	private String portrait_path_48;
	private Date msgtime;
	private String message;
	//private Integer isnew;

	/**
	 * 转成JsonObject
	 * 
	 * @return JsonObject
	 */
	public JsonObject toJsonObject(long lastReadTime,int platform) {
		JsonObject jObject = new JsonObject();
		jObject.addProperty("userId", this.getUserId());
		jObject.addProperty("nickname", this.getNickname());
		jObject.addProperty("gender", this.getGender());
		if (this.getPortrait_path_original() != null) {
			jObject.addProperty("portrait_path_1280", this.getPortrait_path_original() + "!1280");
			jObject.addProperty("portrait_path_256", this.getPortrait_path_original() + "!256");
			jObject.addProperty("portrait_path_128", this.getPortrait_path_original() + "!128");
			jObject.addProperty("portrait_path_48",  this.getPortrait_path_original() + "!48");		
		}
		jObject.addProperty("message", this.getMessage());
		jObject.addProperty("msgtime", this.getMsgtime().getTime());
		jObject.addProperty("id", this.getId());
		//取得redis时间戳 返回isnew
		if(lastReadTime>0){
			if(lastReadTime>this.getMsgtime().getTime()){
				jObject.addProperty("isnew", 0);
			}else{
				jObject.addProperty("isnew", 1);
			}
		}else{
			jObject.addProperty("isnew", 0);
		}
		return jObject;
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

	public Date getMsgtime() {
		return msgtime;
	}

	public void setMsgtime(Date msgtime) {
		this.msgtime = msgtime;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

}

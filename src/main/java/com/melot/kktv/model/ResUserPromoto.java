package com.melot.kktv.model;

import java.util.Date;

import com.google.gson.JsonObject;

/**
 * 用户推广记录
 * 
 * 
 */
public class ResUserPromoto {
	
	private Integer userId;
	private Integer beUserId;
	private Date dTime;

	/**
	 * 转成JsonObject
	 * 
	 * @return JsonObject
	 */
	public JsonObject toJsonObject() {
		JsonObject jObject = new JsonObject();
		
		if(null != this.userId && this.userId.intValue() > 0)
			jObject.addProperty("userId", this.getUserId());
		if(null != this.beUserId && this.beUserId.intValue() > 0)
			jObject.addProperty("beUserId", this.beUserId.intValue());
		if(null != this.dTime)
			jObject.addProperty("dTime", this.getdTime().getTime());
		return jObject;
	}

	public Integer getUserId() {
		return userId;
	}

	public Integer getBeUserId() {
		return beUserId;
	}

	public Date getdTime() {
		return dTime;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public void setBeUserId(Integer beUserId) {
		this.beUserId = beUserId;
	}

	public void setdTime(Date dTime) {
		this.dTime = dTime;
	}

}

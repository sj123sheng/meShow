package com.melot.kktv.model;

import java.util.Date;

import com.google.gson.JsonObject;

/**
 * 直播记录
 * 
 * @author liyue
 * 
 */
public class LiveRecord {
	private Date startTime;
	private Date endTime;

	/**
	 * 转成直播记录的JsonObject
	 * 
	 * @return JsonObject
	 */
	public JsonObject toJsonObject() {
		JsonObject jObject = new JsonObject();
		jObject.addProperty("startTime", this.getStartTime().getTime());
		if (this.getEndTime() != null) {
			jObject.addProperty("endTime", this.getEndTime().getTime());
		}
		return jObject;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
}

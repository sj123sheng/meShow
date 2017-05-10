package com.melot.kktv.model;

import java.util.Date;

import com.google.gson.JsonObject;

/**
 * 系统消息
 * 
 * @author Administrator
 * 
 */
public class SysMsg {
	private Integer msgId;
	private Integer userId;
	private String content;
	private Date dTime;

	/**
	 * 转成JsonObject
	 * 
	 * @return JsonObject
	 */
	public JsonObject toJsonObject() {
		JsonObject jObject = new JsonObject();
		jObject.addProperty("msgId", this.getMsgId());
		jObject.addProperty("userId", this.getUserId());
		jObject.addProperty("content", this.getContent());
		jObject.addProperty("dTime", this.getdTime().getTime());

		return jObject;
	}

	public Integer getMsgId() {
		return msgId;
	}

	public void setMsgId(Integer msgId) {
		this.msgId = msgId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getdTime() {
		return dTime;
	}

	public void setdTime(Date dTime) {
		this.dTime = dTime;
	}

}

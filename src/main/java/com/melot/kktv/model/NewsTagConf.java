package com.melot.kktv.model;

import java.io.Serializable;
import java.util.Date;

import com.google.gson.JsonObject;

/**
 * 动态标签
 * @author Administrator
 *
 */
public class NewsTagConf implements Serializable{

	private static final long serialVersionUID = 6544006840129100470L;
	
	private int tagId;
	private String tagName;
	private Integer isHot;
	private Date dtime;
	private Integer rpaiCount; // 热拍数后台需要
	
	public int getTagId() {
		return tagId;
	}
	public String getTagName() {
		return tagName;
	}
	public void setTagId(int tagId) {
		this.tagId = tagId;
	}
	public void setTagName(String tagName) {
		this.tagName = tagName;
	}
	public Date getDtime() {
		return dtime;
	}
	public void setDtime(Date dtime) {
		this.dtime = dtime;
	}
	
	public JsonObject toJsonObjectForHotTag() {
		JsonObject jsObject = new JsonObject();
		jsObject.addProperty("tagId", this.tagId);
		jsObject.addProperty("tagName", this.tagName);
		return jsObject;
	}
	public Integer getIsHot() {
		return isHot;
	}
	public void setIsHot(Integer isHot) {
		this.isHot = isHot;
	}
	public Integer getRpaiCount() {
		return rpaiCount;
	}
	public void setRpaiCount(Integer rpaiCount) {
		this.rpaiCount = rpaiCount;
	}
}

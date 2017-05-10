package com.melot.kktv.domain;

import java.util.Date;

public class TagInfo {
	
	private Integer tagId;
	
	private String tagName;
	
	private Integer tagSource;
	
	private Integer tagCreater;
	
	private Integer tagStatus;
	
	private Integer tagChecker;
	
	private Date tagCheckTime;
	
	public Integer getTagId() {
		return tagId;
	}

	public void setTagId(Integer tagId) {
		this.tagId = tagId;
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public Integer getTagSource() {
		return tagSource;
	}

	public void setTagSource(Integer tagSource) {
		this.tagSource = tagSource;
	}

	public Integer getTagCreater() {
		return tagCreater;
	}

	public void setTagCreater(Integer tagCreater) {
		this.tagCreater = tagCreater;
	}

	public Integer getTagStatus() {
		return tagStatus;
	}

	public void setTagStatus(Integer tagStatus) {
		this.tagStatus = tagStatus;
	}

	public Integer getTagChecker() {
		return tagChecker;
	}

	public void setTagChecker(Integer tagChecker) {
		this.tagChecker = tagChecker;
	}

	public Date getTagCheckTime() {
		return tagCheckTime;
	}

	public void setTagCheckTime(Date tagCheckTime) {
		this.tagCheckTime = tagCheckTime;
	}

}

package com.melot.kktv.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class GuessInfo {
	
	private String topic;
	private Integer guessId;
	private Integer type;
	private String content;
	private Long endTime;
	private Long leftTime;
	private Integer userTotalCount;
	private JsonArray options;
	private JsonObject userOption;
	
	public String getTopic() {
		return topic;
	}
	public void setTopic(String topic) {
		this.topic = topic;
	}
	public Integer getGuessId() {
		return guessId;
	}
	public void setGuessId(Integer guessId) {
		this.guessId = guessId;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Integer getUserTotalCount() {
		return userTotalCount;
	}
	public void setUserTotalCount(Integer userTotalCount) {
		this.userTotalCount = userTotalCount;
	}
	public Long getEndTime() {
		return endTime;
	}
	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}
	public Long getLeftTime() {
		return leftTime;
	}
	public void setLeftTime(Long leftTime) {
		this.leftTime = leftTime;
	}
	public JsonArray getOptions() {
		return options;
	}
	public void setOptions(JsonArray options) {
		this.options = options;
	}
	public JsonObject getUserOption() {
		return userOption;
	}
	public void setUserOption(JsonObject userOption) {
		this.userOption = userOption;
	}
	
}

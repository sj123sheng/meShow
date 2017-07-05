package com.melot.kkcx.model;

import java.util.Date;

import com.google.gson.annotations.Expose;

public class TimMsgContent {
	private long id;
	@Expose
	private int fromAccount;
	@Expose
	private int toAccount;
	private String msgType;
	@Expose
	private String text;
	private Date createTime;
	@Expose
	private long timeSpan;

	public long getTimeSpan() {
		return timeSpan;
	}

	public void setTimeSpan(long timeSpan) {
		this.timeSpan = timeSpan;
	}

	private String fromTo;

	public String getFromTo() {
		return fromTo;
	}

	public void setFromTo(String fromTo) {
		this.fromTo = fromTo;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getFromAccount() {
		return fromAccount;
	}

	public void setFromAccount(int fromAccount) {
		this.fromAccount = fromAccount;
	}

	public int getToAccount() {
		return toAccount;
	}

	public void setToAccount(int toAccount) {
		this.toAccount = toAccount;
	}

	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
}

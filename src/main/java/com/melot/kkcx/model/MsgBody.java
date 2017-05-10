package com.melot.kkcx.model;

import com.google.gson.annotations.SerializedName;

public class MsgBody {
	@SerializedName("MsgType")
	private String msgType;

	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}
	@SerializedName("MsgContent")
	private MsgContent msgContent;

	public MsgContent getMsgContent() {
		return msgContent;
	}

	public void setMsgContent(MsgContent msgContent) {
		this.msgContent = msgContent;
	}
}

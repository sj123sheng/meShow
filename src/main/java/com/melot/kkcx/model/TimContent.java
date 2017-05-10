package com.melot.kkcx.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class TimContent {
	@SerializedName("CallbackCommand")
	private String callbackCommand;
	@SerializedName("From_Account")
	private String fromAccount;
	@SerializedName("To_Account")
	private String toAccount;
	@SerializedName("MsgBody")
	private List<MsgBody> msgBody;

	public String getCallbackCommand() {
		return callbackCommand;
	}

	public void setCallbackCommand(String callbackCommand) {
		this.callbackCommand = callbackCommand;
	}

	public String getFromAccount() {
		return fromAccount;
	}

	public void setFromAccount(String fromAccount) {
		this.fromAccount = fromAccount;
	}

	public String getToAccount() {
		return toAccount;
	}

	public void setToAccount(String toAccount) {
		this.toAccount = toAccount;
	}

	public List<MsgBody> getMsgBody() {
		return msgBody;
	}

	public void setMsgBody(List<MsgBody> msgBody) {
		this.msgBody = msgBody;
	}
}

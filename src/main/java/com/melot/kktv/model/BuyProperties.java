package com.melot.kktv.model;

import java.util.Date;

import com.google.gson.JsonObject;

public class BuyProperties {
	
	private Date consumeTime;
	
	private Integer amount;
	
	private String paymentDesc;
	
	private Integer paymentMode;
	
	private Integer type;
	
	private String typeDesc;
	
	private Integer showMoney;
	
	public JsonObject toJsonObject() {
		JsonObject obj = new JsonObject();
		if (this.getAmount() != null) {
			obj.addProperty("amount", this.getAmount());
		}
		if (this.getConsumeTime() != null) {
			obj.addProperty("consumeTime", this.getConsumeTime().getTime());
		}
		if (this.getPaymentDesc() != null) {
			obj.addProperty("paymentDesc", this.getPaymentDesc());
		}
		if (this.getPaymentMode() != null) {
			obj.addProperty("paymentMode", this.getPaymentMode());
		}
		if (this.getType() != null) {
			obj.addProperty("type", this.getType());
		}
		if (this.getTypeDesc() != null) {
			obj.addProperty("typeDesc", this.getTypeDesc());
		}
		if (this.getShowMoney() != null) {
			obj.addProperty("showMoney", this.getShowMoney());
		}
		return obj;
	}
	
	public Date getConsumeTime() {
		return consumeTime;
	}

	public void setConsumeTime(Date consumeTime) {
		this.consumeTime = consumeTime;
	}

	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	public String getPaymentDesc() {
		return paymentDesc;
	}

	public void setPaymentDesc(String paymentDesc) {
		this.paymentDesc = paymentDesc;
	}

	public Integer getPaymentMode() {
		return paymentMode;
	}

	public void setPaymentMode(Integer paymentMode) {
		this.paymentMode = paymentMode;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getTypeDesc() {
		return typeDesc;
	}

	public void setTypeDesc(String typeDesc) {
		this.typeDesc = typeDesc;
	}

	public Integer getShowMoney() {
		return showMoney;
	}

	public void setShowMoney(Integer showMoney) {
		this.showMoney = showMoney;
	}
	
}

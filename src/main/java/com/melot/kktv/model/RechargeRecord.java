package com.melot.kktv.model;

import java.util.Date;

import com.google.gson.JsonObject;

/**
 * 充值记录
 * 
 * @author liyue
 * 
 */
public class RechargeRecord {

	private String orderId;
	private Date rechargeTime;
	private float amount;
	private Long miMoney;
	private Integer paymentMode;
	private Integer state;
	private Date affirmTime;
	private String errCode;
	private String modeDesc;

	/**
	 * 转成用户充值记录的JsonObject
	 * 
	 * @return JsonObject
	 */
	public JsonObject toJsonObject() {
		JsonObject jObject = new JsonObject();
		jObject.addProperty("orderId", this.getOrderId());
		jObject.addProperty("rechargeTime", this.getRechargeTime().getTime());
		jObject.addProperty("amount", this.getAmount());
		jObject.addProperty("miMoney", this.getMiMoney());
		jObject.addProperty("paymentMode", this.getPaymentMode());
		jObject.addProperty("state", this.getState());
		if (this.getState() == 1) {
			jObject.addProperty("affirmTime", this.getAffirmTime().getTime());
		}
		if (this.getErrCode() != null) {
			jObject.addProperty("errcode", this.getErrCode());
		}
		if (this.getModeDesc() != null) {
			jObject.addProperty("modeDesc", this.getModeDesc());
		}
		return jObject;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public Date getRechargeTime() {
		return rechargeTime;
	}

	public void setRechargeTime(Date rechargeTime) {
		this.rechargeTime = rechargeTime;
	}

	public float getAmount() {
		return amount;
	}

	public void setAmount(float amount) {
		this.amount = amount;
	}

	public Long getMiMoney() {
		return miMoney;
	}

	public void setMiMoney(Long miMoney) {
		this.miMoney = miMoney;
	}

	public Integer getPaymentMode() {
		return paymentMode;
	}

	public void setPaymentMode(Integer paymentMode) {
		this.paymentMode = paymentMode;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public Date getAffirmTime() {
		return affirmTime;
	}

	public void setAffirmTime(Date affirmTime) {
		this.affirmTime = affirmTime;
	}

	public String getErrCode() {
		return errCode;
	}

	public void setErrCode(String errCode) {
		this.errCode = errCode;
	}

	public String getModeDesc() {
		return modeDesc;
	}

	public void setModeDesc(String modeDesc) {
		this.modeDesc = modeDesc;
	}

}

package com.melot.kktv.model;

import java.util.Date;

import com.google.gson.JsonObject;
/**
 * 用户推广用户记录
 * 
 * 
 */
public class HisPromote {
	
	private Integer userId;
	private Integer beUserId;
	private Integer type;
	private Float point;
	private Integer rcgAmount;
	private Integer status;
	private String describe;
	private Date dTime;
	private Integer rechargeAmount;
	private String bankName;
	private String bankNo;
	private Integer total;
	
	
	
	/**
	 * 转成JsonObject
	 * 
	 * @return JsonObject
	 */
	public  JsonObject toExtJsonObject() {
		JsonObject jObject = new JsonObject();
		if(null != this.status)
			jObject.addProperty("status", this.getStatus());
		if(null != this.describe && !this.describe.trim().isEmpty())
			jObject.addProperty("describe", this.getDescribe());
		if(null != this.dTime && this.dTime.getTime() > 0)
			jObject.addProperty("dTime", this.getdTime().getTime());
		return jObject;
	}

	public JsonObject toProJsonObject() {
		JsonObject jObject = new JsonObject();
		if(null != this.beUserId && this.beUserId.intValue() > 0)
			jObject.addProperty("beUserId", this.getBeUserId());
		if(null != this.dTime && this.dTime.getTime() > 0)
			jObject.addProperty("registerTime", this.getdTime().getTime());
		//if(null != this.rechargeAmount && this.getRechargeAmount().intValue() >=0)
			jObject.addProperty("rcgAmount", this.getRechargeAmount());
		//if(null != this.point && this.point.intValue() >= 0)
			jObject.addProperty("point", this.getPoint());
		if(null != this.status)
			jObject.addProperty("status", this.getStatus());
		if(null != this.describe && !this.describe.trim().isEmpty())
			jObject.addProperty("describe", this.getDescribe());
		return jObject;
	}
	
	public Integer getUserId() {
		return userId;
	}

	public Integer getBeUserId() {
		return beUserId;
	}

	public Integer getType() {
		return type;
	}


	public Integer getRcgAmount() {
		return rcgAmount;
	}

	public Integer getStatus() {
		return status;
	}

	public String getDescribe() {
		return describe;
	}

	public Date getdTime() {
		return dTime;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public void setBeUserId(Integer beUserId) {
		this.beUserId = beUserId;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public void setRcgAmount(Integer rcgAmount) {
		this.rcgAmount = rcgAmount;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	public void setdTime(Date dTime) {
		this.dTime = dTime;
	}

	public Integer getRechargeAmount() {
		return rechargeAmount;
	}

	public void setRechargeAmount(Integer rechargeAmount) {
		this.rechargeAmount = rechargeAmount;
	}

	public String getBankName() {
		return bankName;
	}

	public String getBankNo() {
		return bankNo;
	}

	public Integer getTotal() {
		return total;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public void setBankNo(String bankNo) {
		this.bankNo = bankNo;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public Float getPoint() {
		return point;
	}

	public void setPoint(Float point) {
		this.point = point;
	}

}

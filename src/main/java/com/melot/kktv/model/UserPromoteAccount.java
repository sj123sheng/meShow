package com.melot.kktv.model;

import java.util.Date;

import com.google.gson.JsonObject;
/**
 * 用户推广个人信息
 * 
 * 
 */
public class UserPromoteAccount {
	
	private Integer userId;
	private Integer phone;
	private Integer point;
	private String bankName;
	private String bankNo;
	private String qq;
	private Date inTime;

	/**
	 * 转成JsonObject
	 * 
	 * @return JsonObject
	 */
	public JsonObject toJsonObject() {
		JsonObject jObject = new JsonObject();
		
		if(null != this.phone && this.phone.intValue() > 0)
			jObject.addProperty("phone", this.getPhone());
		if(null != this.point && this.point.intValue() > 0)
			jObject.addProperty("point", this.getPoint());
		if(null != this.bankName && !this.bankName.trim().isEmpty())
			jObject.addProperty("bankName", this.getBankName());
		if(null != this.bankNo && !this.bankNo.trim().isEmpty())
			jObject.addProperty("bankNo", this.getBankNo());
		if(null != this.qq && !this.qq.trim().isEmpty())
			jObject.addProperty("qq", this.getQq());

		return jObject;
	}

	public Integer getUserId() {
		return userId;
	}

	public Integer getPhone() {
		return phone;
	}

	public Integer getPoint() {
		return point;
	}

	public String getBankName() {
		return bankName;
	}

	public String getBankNo() {
		return bankNo;
	}

	public String getQq() {
		return qq;
	}

	public Date getInTime() {
		return inTime;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public void setPhone(Integer phone) {
		this.phone = phone;
	}

	public void setPoint(Integer point) {
		this.point = point;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public void setBankNo(String bankNo) {
		this.bankNo = bankNo;
	}

	public void setQq(String qq) {
		this.qq = qq;
	}

	public void setInTime(Date inTime) {
		this.inTime = inTime;
	}

	
}

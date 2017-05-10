package com.melot.kktv.model;

import java.util.Date;

import com.google.gson.JsonObject;

/**
 * 优惠券类
 * 
 * @author 
 * 
 */
public class CouponCode {
	
	private Integer couponType; //优惠券类型
	private Integer userId; //用户Id
	private Integer couponId; //优惠券唯一编码
	private Date expireTime; //过期时间
	private Integer cnt; //可以使用次数
	private Date updateTime; //得到优惠券时间
	private Integer beUsed; //被使用次数
	private Integer rate;//优惠券折扣 

	/**
	 * 转成JsonObject
	 * 
	 * @return JsonObject
	 */
	public JsonObject toJsonObject() {
		JsonObject jObject = new JsonObject();
		
		if (this.couponId != null && this.couponId>0) {
			jObject.addProperty("couponId", this.couponId);
		}
		if (this.expireTime != null) {
			jObject.addProperty("et", this.expireTime.getTime()-System.currentTimeMillis());
		}
		if (this.cnt != null) { 
			jObject.addProperty("cnt", this.cnt);
		}
		if(this.rate!=null && this.rate>0){
			jObject.addProperty("rate", this.rate);
		}
		return jObject;
	}

	public Integer getCouponType() {
		return couponType;
	}

	public void setCouponType(Integer couponType) {
		this.couponType = couponType;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Date getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(Date expireTime) {
		this.expireTime = expireTime;
	}

	public Integer getCnt() {
		return cnt;
	}

	public void setCnt(Integer cnt) {
		this.cnt = cnt;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public Integer getBeUsed() {
		return beUsed;
	}

	public void setBeUsed(Integer beUsed) {
		this.beUsed = beUsed;
	}

	public Integer getCouponId() {
		return couponId;
	}

	public void setCouponId(Integer couponId) {
		this.couponId = couponId;
	}

	public Integer getRate() {
		return rate;
	}

	public void setRate(Integer rate) {
		this.rate = rate;
	}

	
}

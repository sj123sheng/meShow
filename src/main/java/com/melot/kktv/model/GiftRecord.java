package com.melot.kktv.model;

import java.util.Date;

import com.google.gson.JsonObject;

/**
 * 赠送礼物记录
 * 
 * @author liyue
 * 
 */
public class GiftRecord {
	private Integer userId;
	private String userNick;
	private Integer giftId;
	private String giftName;
	private String unit;
	private Integer sendPrice;
	private Integer rsvPrice;
	private Integer count;
	private Date sendTime;

	/**
	 * 转成用户接受礼物的JsonObject
	 * 
	 * @return JsonObject
	 */
	public JsonObject toRsvJsonObject() {
		JsonObject jObject = new JsonObject();
		jObject.addProperty("senderId", this.getUserId());
		jObject.addProperty("senderNick", this.getUserNick());
		jObject.addProperty("giftId", this.getGiftId());
		jObject.addProperty("giftName", this.getGiftName());
		jObject.addProperty("unit", this.getUnit());
		jObject.addProperty("sendPrice", this.getSendPrice());
		jObject.addProperty("rsvPrice", this.getRsvPrice());
		jObject.addProperty("count", this.getCount());
		jObject.addProperty("sendTime", this.getSendTime().getTime());
		
		return jObject;
	}

	/**
	 * 转成用户送出礼物列表的JsonObject
	 * 
	 * @return JsonObject
	 */
	public JsonObject toSendJsonObject() {
		JsonObject jObject = new JsonObject();
		jObject.addProperty("receiverId", this.getUserId());
		jObject.addProperty("receiverNick", this.getUserNick());
		jObject.addProperty("giftId", this.getGiftId());
		jObject.addProperty("giftName", this.getGiftName());
		jObject.addProperty("unit", this.getUnit());
		jObject.addProperty("sendPrice", this.getSendPrice());
		jObject.addProperty("rsvPrice", this.getRsvPrice());
		jObject.addProperty("count", this.getCount());
		jObject.addProperty("sendTime", this.getSendTime().getTime());

		return jObject;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getUserNick() {
		return userNick;
	}

	public void setUserNick(String userNick) {
		this.userNick = userNick;
	}

	public Integer getGiftId() {
		return giftId;
	}

	public void setGiftId(Integer giftId) {
		this.giftId = giftId;
	}

	public String getGiftName() {
		return giftName;
	}

	public void setGiftName(String giftName) {
		this.giftName = giftName;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public Integer getSendPrice() {
		return sendPrice;
	}

	public void setSendPrice(Integer sendPrice) {
		this.sendPrice = sendPrice;
	}

	public Integer getRsvPrice() {
		return rsvPrice;
	}

	public void setRsvPrice(Integer rsvPrice) {
		this.rsvPrice = rsvPrice;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public Date getSendTime() {
		return sendTime;
	}

	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}
}

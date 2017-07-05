package com.melot.kktv.model;

import java.util.Date;

import com.google.gson.JsonObject;

/**
 * 中奖记录
 * 
 * @author liyue
 * 
 */
public class WinLotteryRecord {
	private Date time;
	private Integer giftId;
	private Integer winType;
	private Integer times;
	private Integer count;

	/**
	 * 转成直播记录的JsonObject
	 * 
	 * @return JsonObject
	 */
	public JsonObject toJsonObject() {
		JsonObject jObject = new JsonObject();
		jObject.addProperty("time", this.getTime().getTime());
		jObject.addProperty("giftId", this.getGiftId());
		jObject.addProperty("winType", this.getWinType());
		jObject.addProperty("times", this.getTimes());
		jObject.addProperty("count", this.getCount());

		return jObject;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public Integer getGiftId() {
		return giftId;
	}

	public void setGiftId(Integer giftId) {
		this.giftId = giftId;
	}

	public Integer getWinType() {
		return winType;
	}

	public void setWinType(Integer winType) {
		this.winType = winType;
	}

	public Integer getTimes() {
		return times;
	}

	public void setTimes(Integer times) {
		this.times = times;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

}

package com.melot.kktv.model;

import java.util.Date;

import com.google.gson.JsonObject;

/**
 * 荣誉类
 * 
 * @author liyue
 * 
 */
public class Honor {
	private Integer rankType;
	private Integer slotType;
	private Integer ranking;
	private Date honorTime;

	/**
	 * 转成JsonObject
	 * 
	 * @return JsonObject
	 */
	public JsonObject toJsonObject() {
		JsonObject jObject = new JsonObject();
		jObject.addProperty("rankType", this.getRankType());
		jObject.addProperty("slotType", this.getSlotType());
		jObject.addProperty("ranking", this.getRanking());
		if (this.getHonorTime() != null) {
			jObject.addProperty("honorTime", this.getHonorTime().getTime());
		}
		return jObject;
	}

	public Integer getRankType() {
		return rankType;
	}

	public void setRankType(Integer rankType) {
		this.rankType = rankType;
	}

	public Integer getSlotType() {
		return slotType;
	}

	public void setSlotType(Integer slotType) {
		this.slotType = slotType;
	}

	public Integer getRanking() {
		return ranking;
	}

	public void setRanking(Integer ranking) {
		this.ranking = ranking;
	}

	public Date getHonorTime() {
		return honorTime;
	}

	public void setHonorTime(Date honorTime) {
		this.honorTime = honorTime;
	}
}

package com.melot.kktv.model;

import com.google.gson.JsonObject;

/**
 * 道具价格
 * 
 * @author liyue
 * 
 */
public class PropPrice {

	private Integer periodOfValidity;
	private Integer propPrice;
	private Integer originalPrice;

	/**
	 * 转成JsonObject
	 * 
	 * @return JsonObject
	 */
	public JsonObject toJsonObject() {
		JsonObject jObject = new JsonObject();
		jObject.addProperty("periodOfValidity", this.getPeriodOfValidity());
		jObject.addProperty("propPrice", this.getPropPrice());
		if (this.getOriginalPrice() != null) {
			jObject.addProperty("originalPrice", this.getOriginalPrice());
		}
		return jObject;
	}

	public Integer getPeriodOfValidity() {
		return periodOfValidity;
	}

	public void setPeriodOfValidity(Integer periodOfValidity) {
		this.periodOfValidity = periodOfValidity;
	}

	public Integer getPropPrice() {
		return propPrice;
	}

	public void setPropPrice(Integer propPrice) {
		this.propPrice = propPrice;
	}

	public Integer getOriginalPrice() {
		return originalPrice;
	}

	public void setOriginalPrice(Integer originalPrice) {
		this.originalPrice = originalPrice;
	}

}

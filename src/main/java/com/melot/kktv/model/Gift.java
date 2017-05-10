package com.melot.kktv.model;

import com.google.gson.JsonObject;
import com.melot.kktv.util.ConfigHelper;

/**
 * 礼物信息
 * 
 * @author liyue
 * 
 */
public class Gift {
	private Integer catalogId;
	private String catalogName;
	private Integer giftId;
	private String giftName;
	private String unit;
	private Integer sendPrice;
	private Integer rsvPrice;

	/**
	 * 转成JsonObject
	 * 
	 * @return JsonObject
	 */
	public JsonObject toJsonObject() {
		JsonObject jObject = new JsonObject();
		jObject.addProperty("catalogId", this.getCatalogId());
		jObject.addProperty("catalogName", this.getCatalogName());
		jObject.addProperty("giftId", this.getGiftId());
		jObject.addProperty("giftName", this.getGiftName());
		jObject.addProperty("unit", this.getUnit());
		jObject.addProperty("sendPrice", this.getSendPrice());
		jObject.addProperty("rsvPrice", this.getRsvPrice());
		jObject.addProperty("giftIconAndroidResURL", ConfigHelper.getGiftIconAndroidResURL() + this.getGiftId() + ".png");
		jObject.addProperty("giftGifAndroidResURL", ConfigHelper.getGiftGifAndroidResURL() + this.getGiftId() + ".gif");
		jObject.addProperty("giftIconIphoneResURL", ConfigHelper.getGiftIconIphoneResURL() + this.getGiftId() + ".png");
		jObject.addProperty("giftGifIphoneResURL", ConfigHelper.getGiftGifIphoneResURL() + this.getGiftId() + ".gif");
		jObject.addProperty("giftSmallIconAndroidResURL", ConfigHelper.getGiftSmallIconAndroidResURL() + this.getGiftId() + ".png");
		jObject.addProperty("giftSmallIconIphoneResURL", ConfigHelper.getGiftSmallIconIphoneResURL() + this.getGiftId() + ".png");

		return jObject;
	}

	public Integer getCatalogId() {
		return catalogId;
	}

	public void setCatalogId(Integer catalogId) {
		this.catalogId = catalogId;
	}

	public String getCatalogName() {
		return catalogName;
	}

	public void setCatalogName(String catalogName) {
		this.catalogName = catalogName;
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
}

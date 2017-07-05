/**
 * Car.java
 */
package com.melot.kktv.model;

import com.google.gson.JsonObject;

/**
 * @author Zhousx
 * @version 1.0 2012-8-7 上午10:46:18
 */
public class Car {
	private int carid;
	private String name;
	private String photo;
	private String icon;
	private long orignalPrice;
	private long nowPrice;
	private int ifLimit;
	private int hasSold;
	private int category;
	private int getCondition;

	public int getCarid() {
		return carid;
	}

	public void setCarid(int carid) {
		this.carid = carid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhoto() {
		return photo;
	}

	public void setPhoto(String photo) {
		this.photo = photo;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public long getOrignalPrice() {
		return orignalPrice;
	}

	public void setOrignalPrice(long orignalPrice) {
		this.orignalPrice = orignalPrice;
	}

	public long getNowPrice() {
		return nowPrice;
	}

	public void setNowPrice(long nowPrice) {
		this.nowPrice = nowPrice;
	}

	public int getIfLimit() {
		return ifLimit;
	}

	public void setIfLimit(int ifLimit) {
		this.ifLimit = ifLimit;
	}

	public int getHasSold() {
		return hasSold;
	}

	public void setHasSold(int hasSold) {
		this.hasSold = hasSold;
	}

	public int getCategory() {
		return category;
	}

	public void setCategory(int category) {
		this.category = category;
	}

	public int getGetCondition() {
		return getCondition;
	}

	public void setGetCondition(int getCondition) {
		this.getCondition = getCondition;
	}

	/**
	 * 获取商城车市车辆列表时，转换为JsonObject
	 * 
	 * @return
	 */
	public JsonObject toJsonObjectForCarList() {
		JsonObject jo = new JsonObject();
		jo.addProperty("id", this.carid);
		jo.addProperty("name", this.name);
		jo.addProperty("photo", this.photo);
		jo.addProperty("icon", this.icon);
		jo.addProperty("origprice", this.orignalPrice);
		jo.addProperty("nowprice", this.nowPrice);
		jo.addProperty("ifLimit", this.ifLimit);

		return jo;
	}
}

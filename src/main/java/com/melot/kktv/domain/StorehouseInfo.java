package com.melot.kktv.domain;

public class StorehouseInfo {
	
	private Integer giftId;
	
	private String giftName;
	
	private Integer quantity;

	public StorehouseInfo(int giftId, String giftName, int quantity) {
		this.giftId = giftId;
		this.giftName = giftName;
		this.quantity = quantity;
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

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
}

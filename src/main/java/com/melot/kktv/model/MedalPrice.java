package com.melot.kktv.model;

import org.apache.commons.lang.builder.ToStringBuilder;

public class MedalPrice {
	
	private Integer type;
	private Integer period;
	private Integer price;

	public Integer getType() {
		return type;
	}

	public Integer getPeriod() {
		return period;
	}

	public Integer getPrice() {
		return price;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public void setPeriod(Integer period) {
		this.period = period;
	}

	public void setPrice(Integer price) {
		this.price = price;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
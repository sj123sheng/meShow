package com.melot.kktv.model;

import java.util.Date;

import com.google.gson.JsonObject;

/**
 * 消费记录
 * 
 * @author liyue
 * 
 */
public class ConsumerRecord {

	private Date consumerTime;
	private Integer consumerType;
	private Integer amount;
	private String typeDesc;
	private String product;
	private Integer count;

	/**
	 * 转成用户消费记录的JsonObject
	 * 
	 * @return JsonObject
	 */
	public JsonObject toJsonObject() {
		JsonObject jObject = new JsonObject();
		jObject.addProperty("consumerTime", this.getConsumerTime().getTime());
		jObject.addProperty("consumerType", this.getConsumerType());
		jObject.addProperty("amount", this.getAmount());
		jObject.addProperty("typeDesc", this.getTypeDesc());
		jObject.addProperty("product", this.getProduct());
		jObject.addProperty("count", this.getCount());

		return jObject;
	}

	public Date getConsumerTime() {
		return consumerTime;
	}

	public void setConsumerTime(Date consumerTime) {
		this.consumerTime = consumerTime;
	}

	public Integer getConsumerType() {
		return consumerType;
	}

	public void setConsumerType(Integer consumerType) {
		this.consumerType = consumerType;
	}

	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	public String getTypeDesc() {
		return typeDesc;
	}

	public void setTypeDesc(String typeDesc) {
		this.typeDesc = typeDesc;
	}

	public String getProduct() {
		return product;
	}

	public void setProduct(String product) {
		this.product = product;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

}

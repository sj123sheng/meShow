package com.melot.kktv.payment.domain;

/**
 * 充值信息配置
 * @author Administrator
 *
 */
public class ConfPaymentInfo {

	/**
	 * 充值类型
	 */
	private Integer paymentMode;
	
	/**
	 * 充值名称
	 */
	private String paymentName;
	
	/**
	 * 充值比例
	 */
	private Integer paymentRate;
	
	/**
	 * 充值描述
	 */
	private String paymentTip;
	
	/**
	 * 充值上限
	 */
	private Integer upLimit;
	
	/**
	 * 显示顺序
	 */
	private Integer sortIndex;
	
	public Integer getPaymentMode() {
		return paymentMode;
	}

	public void setPaymentMode(Integer paymentMode) {
		this.paymentMode = paymentMode;
	}

	public String getPaymentName() {
		return paymentName;
	}

	public void setPaymentName(String paymentName) {
		this.paymentName = paymentName;
	}

	public Integer getPaymentRate() {
		return paymentRate;
	}

	public void setPaymentRate(Integer paymentRate) {
		this.paymentRate = paymentRate;
	}

	public String getPaymentTip() {
		return paymentTip;
	}

	public void setPaymentTip(String paymentTip) {
		this.paymentTip = paymentTip;
	}

	public Integer getUpLimit() {
		return upLimit;
	}

	public void setUpLimit(Integer upLimit) {
		this.upLimit = upLimit;
	}

	public Integer getSortIndex() {
		return sortIndex;
	}

	public void setSortIndex(Integer sortIndex) {
		this.sortIndex = sortIndex;
	}

}

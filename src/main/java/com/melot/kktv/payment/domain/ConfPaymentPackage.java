package com.melot.kktv.payment.domain;

import java.util.List;

/**
 * 充值礼包配置
 * @author Administrator
 *
 */
public class ConfPaymentPackage {

	/**
	 * 充值金额区间-上界
	 */
	private Integer minAmount;
	
	/**
	 * 充值金额区间-下界
	 */
	private Integer maxAmount;
	
	/**
	 * 礼包编号
	 */
	private Integer packageId;
	
	/**
	 * 礼包编号
	 */
	private String packageNotice;
	
	/**
	 * 礼包价值
	 */
	private Integer packageWorth;
	
	/**
	 * 礼包礼物列表
	 */
	private List<PaymentPackageGift> packageGiftList;

	public int getMinAmount() {
		return minAmount;
	}

	public void setMinAmount(Integer minAmount) {
		this.minAmount = minAmount;
	}

	public int getMaxAmount() {
		return maxAmount;
	}

	public void setMaxAmount(Integer maxAmount) {
		this.maxAmount = maxAmount;
	}

	public int getPackageId() {
		return packageId;
	}

	public void setPackageId(Integer packageId) {
		this.packageId = packageId;
	}

	public String getPackageNotice() {
		return packageNotice;
	}

	public void setPackageNotice(String packageNotice) {
		this.packageNotice = packageNotice;
	}

	public Integer getPackageWorth() {
		return packageWorth;
	}

	public void setPackageWorth(Integer packageWorth) {
		this.packageWorth = packageWorth;
	}

	public List<PaymentPackageGift> getPackageGiftList() {
		return packageGiftList;
	}

	public void setPackageGiftList(List<PaymentPackageGift> packageGiftList) {
		this.packageGiftList = packageGiftList;
	}
	
}

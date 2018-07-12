package com.melot.kktv.payment.domain;

/**
 * 充值礼包礼物配置
 * @author Administrator
 *
 */
public class PaymentPackageGift {

	/**
	 * 礼物编号
	 */
	private Integer giftId;
	
	/**
	 * 礼物类型
	 */
	private Integer giftType;
	
	/**
	 * 礼物名称
	 */
	private String giftName;
	
	/**
	 * 礼物描述
	 */
	private String giftNotice;
	
	/**
	 * 礼物图标
	 */
	private String giftIcon;
	
	/**
	 * 礼物数量
	 */
	private Integer giftCount;

	public Integer getGiftId() {
		return giftId;
	}

	public void setGiftId(Integer giftId) {
		this.giftId = giftId;
	}

	public Integer getGiftType() {
		return giftType;
	}

	public void setGiftType(Integer giftType) {
		this.giftType = giftType;
	}

	public String getGiftName() {
		return giftName;
	}

	public void setGiftName(String giftName) {
		this.giftName = giftName;
	}

	public String getGiftNotice() {
		return giftNotice;
	}

	public void setGiftNotice(String giftNotice) {
		this.giftNotice = giftNotice;
	}

	public String getGiftIcon() {
		return giftIcon;
	}

	public void setGiftIcon(String giftIcon) {
		this.giftIcon = giftIcon;
	}

	public Integer getGiftCount() {
		return giftCount;
	}

	public void setGiftCount(Integer giftCount) {
		this.giftCount = giftCount;
	}
	
}

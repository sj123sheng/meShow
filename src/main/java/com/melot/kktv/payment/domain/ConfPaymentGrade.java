package com.melot.kktv.payment.domain;

/**
 * 充值等级配置
 * @author Administrator
 *
 */
public class ConfPaymentGrade {

	/**
	 * 等级区间-上界
	 */
	private Integer minGrade;
	
	/**
	 * 等级区间-下界
	 */
	private Integer maxGrade;
	
	/**
	 * 返点比例
	 */
	private Integer rate;

	/**
	 * 等级返点描述
	 */
	private String gradeTip;
	
	public Integer getMinGrade() {
		return minGrade;
	}

	public void setMinGrade(Integer minGrade) {
		this.minGrade = minGrade;
	}

	public Integer getMaxGrade() {
		return maxGrade;
	}

	public void setMaxGrade(Integer maxGrade) {
		this.maxGrade = maxGrade;
	}

	public Integer getRate() {
		return rate;
	}

	public void setRate(Integer rate) {
		this.rate = rate;
	}

	public String getGradeTip() {
		return gradeTip;
	}

	public void setGradeTip(String gradeTip) {
		this.gradeTip = gradeTip;
	}

}

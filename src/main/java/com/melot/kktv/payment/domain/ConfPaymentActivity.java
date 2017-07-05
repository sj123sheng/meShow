package com.melot.kktv.payment.domain;

/**
 * 充值活动配置
 * @author Administrator
 *
 */
public class ConfPaymentActivity {

	/**
	 * 活动编号
	 */
	private Integer activityId;
	
	/**
	 * 活动链接
	 */
	private String activityURL;
    
    /**
     * 活动链接
     */
    private String activityMobileURL;

    /**
	 * 活动海报地址
	 */
	private String topURL;
	
	/**
	 * 活动海报安卓地址
	 */
	private String topMobileURL;
	
	/**
	 * 活动海报苹果地址
	 */
	private String topMobileURLIOS;
	
	/**
	 * 是否首冲活动
	 */
	private Integer isFirstCharge;
	
	/**
	 * 活动类型 (0普通活动,1首冲活动,2充值成功后活动)
	 */
	private Integer activityType;
	
	public Integer getActivityId() {
		return activityId;
	}

	public void setActivityId(Integer activityId) {
		this.activityId = activityId;
	}

	public String getActivityURL() {
		return activityURL;
	}

	public void setActivityURL(String activityURL) {
		this.activityURL = activityURL;
	}
    
    public String getActivityMobileURL() {
        return activityMobileURL;
    }

    public void setActivityMobileURL(String activityMobileURL) {
        this.activityMobileURL = activityMobileURL;
    }

	public String getTopURL() {
		return topURL;
	}

	public void setTopURL(String topURL) {
		this.topURL = topURL;
	}

	public String getTopMobileURL() {
		return topMobileURL;
	}

	public void setTopMobileURL(String topMobileURL) {
		this.topMobileURL = topMobileURL;
	}

	public String getTopMobileURLIOS() {
		return topMobileURLIOS;
	}

	public void setTopMobileURLIOS(String topMobileURLIOS) {
		this.topMobileURLIOS = topMobileURLIOS;
	}

	public Integer getIsFirstCharge() {
		return isFirstCharge;
	}

	public void setIsFirstCharge(Integer isFirstCharge) {
		this.isFirstCharge = isFirstCharge;
	}

	public Integer getActivityType() {
		return activityType;
	}

	public void setActivityType(Integer activityType) {
		this.activityType = activityType;
	}

}
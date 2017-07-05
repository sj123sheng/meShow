package com.melot.kktv.model;

import java.util.Date;

import com.google.gson.JsonObject;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.PlatformEnum;

public class HotActivity {
	private int activityId;	// 活动编号
	private String activityTitle;	// 活动标题
	private Integer isHot;	//H/N/空
	private String activityURL;	//活动页面URL
	private String imgURL;	//活动BANNER(拼接!160)
	private Date startDate; //开始日期
	private Date endDate; //结束日期
	private Date dtime; //记录时间
	private String sharedText;
	private String sharedImgURL;
	
    private String topURL;
    private String topMobileURL;
    private String topMobileURLIOS;
	
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
    public int getActivityId() {
		return activityId;
	}
	public void setActivityId(int activityId) {
		this.activityId = activityId;
	}
	public String getActivityTitle() {
		return activityTitle;
	}
	public void setActivityTitle(String activityTitle) {
		this.activityTitle = activityTitle;
	}
	public Integer getIsHot() {
		return isHot;
	}
	public void setIsHot(Integer isHot) {
		this.isHot = isHot;
	}
	public String getActivityURL() {
		return activityURL;
	}
	public void setActivityURL(String activityURL) {
		this.activityURL = activityURL;
	}
	public String getImgURL() {
		return imgURL;
	}
	public void setImgURL(String imgURL) {
		this.imgURL = imgURL;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public Date getDtime() {
		return dtime;
	}
	public void setDtime(Date dtime) {
		this.dtime = dtime;
	}
	
	public String getSharedText() {
		return sharedText;
	}
	
	public void setSharedText(String sharedText) {
		this.sharedText = sharedText;
	}
	
	public String getSharedImgURL() {
		return sharedImgURL;
	}
	
	public void setSharedImgURL(String sharedImgURL) {
		this.sharedImgURL = sharedImgURL;
	}
	
	public JsonObject toJsonObject(int platform) {
		JsonObject jObject = new JsonObject();
		jObject.addProperty("activityId", this.getActivityId());
		jObject.addProperty("imgURL", this.getImgURL() + "!160");
		jObject.addProperty("activityTitle", this.getActivityTitle());
		if (this.activityURL.trim().equals("#")) {
			jObject.addProperty("activityURL", ConfigHelper.getActivityUrl() + this.activityId);
		} else {
			jObject.addProperty("activityURL", this.getActivityURL());
		}
		if (this.getIsHot() == null || this.getIsHot().intValue() != 1) {
			// N 上线3天内 0
			if (this.getDtime().getTime() + 3 * 24 * 3600 * 1000 < System.currentTimeMillis()) {
				// H 最多手工设置3条
				jObject.addProperty("activityTag", 2);
			} else {
				jObject.addProperty("activityTag", 0);
			}
		} else {
			// H 最多手工设置3条 1
			jObject.addProperty("activityTag", 1);
		}

        if (this.topURL != null && !this.topURL.trim().equals("")) { // 非移动终端
            jObject.addProperty("topURL", this.topURL);
        }
        if (platform == PlatformEnum.ANDROID) { // 移动终端
            if (this.topMobileURL != null && !this.topMobileURL.trim().equals(""))
                jObject.addProperty("topMobileURL", this.topMobileURL);
        } else if (platform == PlatformEnum.IPHONE) {
            if (this.topMobileURLIOS != null && !this.topMobileURLIOS.trim().equals(""))
                jObject.addProperty("topMobileURL", this.topMobileURLIOS);
        } else if (platform == PlatformEnum.IPAD) {
            if (this.topMobileURLIOS != null && !this.topMobileURLIOS.trim().equals(""))
                jObject.addProperty("topMobileURL", this.topMobileURLIOS);
        }
		
		return jObject;
	}
}

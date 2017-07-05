package com.melot.kktv.model;

import java.util.Date;

import com.google.gson.JsonObject;
import com.melot.kktv.util.ConfigHelper;

public class AllActivities {
	
	int activityId;
	String imgURL;
	String activityURL;
	String activityTitle;
	String activityDesc;
	Date startDate;
	Date endDate;
	int pageTotal;
	String activityMobileUrl;
	
	public String getImgURL() {
		return imgURL;
	}
	public void setImgURL(String imgURL) {
		this.imgURL = imgURL;
	}
	public int getPageTotal() {
		return pageTotal;
	}
	public void setPageTotal(int pageTotal) {
		this.pageTotal = pageTotal;
	}
	public int getActivityId() {
		return activityId;
	}
	public void setActivityId(int activityId) {
		this.activityId = activityId;
	}
	public String getActivityURL() {
		return activityURL;
	}
	public void setActivityURL(String activityURL) {
		this.activityURL = activityURL;
	}
	public String getActivityTitle() {
		return activityTitle;
	}
	public void setActivityTitle(String activityTitle) {
		this.activityTitle = activityTitle;
	}
	public String getActivityDesc() {
		return activityDesc;
	}
	public void setActivityDesc(String activityDesc) {
		this.activityDesc = activityDesc;
	}	
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public Date getStartDate() {
		return startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	
	public String getActivityMobileUrl() {
		return activityMobileUrl;
	}
	public void setActivityMobileUrl(String activityMobileUrl) {
		this.activityMobileUrl = activityMobileUrl;
	}
	public JsonObject toJsonObject(int platform) {
		JsonObject jObject = new JsonObject();
		jObject.addProperty("activityId", this.getActivityId());
		if (platform > 1 && this.activityMobileUrl != null) {
			jObject.addProperty("activityURL", this.getActivityMobileUrl());
		} else {
			if (this.activityURL != null && this.activityURL.trim().equals("#")) {
				jObject.addProperty("activityURL", ConfigHelper.getActivityUrl() + this.activityId);
			} else {
				jObject.addProperty("activityURL", this.getActivityURL());
			}
		}
		jObject.addProperty("activityTitle", this.getActivityTitle());
		jObject.addProperty("activityDesc", this.getActivityDesc());
		jObject.addProperty("startDate", this.getStartDate().getTime());
		jObject.addProperty("endDate", this.getEndDate().getTime());
		if (this.getImgURL()!=null) {
			jObject.addProperty("imgURL", this.getImgURL().replaceFirst("http://rescdn.kktv8.com/kktv", "")
					.replaceFirst("http://ures.kktv8.com/kktv", ""));
		} else {
			jObject.addProperty("imgURL", this.getImgURL());
		}
		long now = System.currentTimeMillis();
		if (now > endDate.getTime())
			jObject.addProperty("type", -1);
		else if (now > startDate.getTime() && now < endDate.getTime())
			jObject.addProperty("type", 1);
		else
			jObject.addProperty("type", 0);
		return jObject;
	}
}

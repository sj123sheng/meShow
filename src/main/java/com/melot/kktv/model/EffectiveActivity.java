package com.melot.kktv.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.JsonObject;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.PlatformEnum;

public class EffectiveActivity {
	
	private Integer activityId;
	private String title;
	private String message;
	private Date genDate;//the time this activity message is saved to DB
	private Date startDate;
	private Date endDate;
	private String imgUrl;
	private String activityMobileUrl;
	private String activityUrl;
	private String topUrl;
	private String topMobileUrl;
	private String topMobileUrlIOS;
	
	public Date getGenDate() {
		return genDate;
	}
	public void setGenDate(Date genDate) {
		this.genDate = genDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public String getImgUrl() {
		return imgUrl;
	}
	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}
	public String getActivityMobileUrl() {
		return activityMobileUrl;
	}
	public void setActivityMobileUrl(String activityMobileUrl) {
		this.activityMobileUrl = activityMobileUrl;
	}
	public String getActivityUrl() {
		return activityUrl;
	}
	public void setActivityUrl(String activityUrl) {
		this.activityUrl = activityUrl;
	}
	public String getTopUrl() {
		return topUrl;
	}
	public void setTopUrl(String topUrl) {
		this.topUrl = topUrl;
	}
	public String getTopMobileUrl() {
		return topMobileUrl;
	}
	public void setTopMobileUrl(String topMobileUrl) {
		this.topMobileUrl = topMobileUrl;
	}
	public String getTopMobileUrlIOS() {
		return topMobileUrlIOS;
	}
	public void setTopMobileUrlIOS(String topMobileUrlIOS) {
		this.topMobileUrlIOS = topMobileUrlIOS;
	}
	public Integer getActivityId() {
		return activityId;
	}
	public void setActivityId(Integer activityId) {
		this.activityId = activityId;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	private static SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public String getActivityDesc(){
		String s = String.format("{\"actId\":%d,\"message\":\"%s\",\"startDate\":\"%s\"}", 
				activityId, message, 
				SDF.format(startDate));
		return s;
	}
	
	public JsonObject toJsonObject(long lastReadTime, long currentTime, int platform) {
		JsonObject jObject = new JsonObject();
		jObject.addProperty("id", this.getActivityId());
		if(this.getTitle() != null)
			jObject.addProperty("title", this.getTitle());
		if(this.getMessage() != null)
			jObject.addProperty("message", this.getMessage());
		Long lStartTime =  new Long(this.getStartDate().getTime());
		jObject.addProperty("msgtime", lStartTime);
		Long lEndTime = new Long(this.getEndDate().getTime());
		jObject.addProperty("et", lEndTime);
		Long lGenTime =  new Long(this.getGenDate().getTime());
		
		jObject.addProperty("img", this.getImgUrl());
		if(platform == PlatformEnum.WEB){//if is web, use activity URL
			if (this.activityUrl == null || this.activityUrl.trim().equals("") || this.activityUrl.trim().equals("#")) {
				jObject.addProperty("activityURL", ConfigHelper.getActivityUrl() + activityId);
			} else {
				jObject.addProperty("activityURL", this.getActivityUrl());
			}
		}else {//if is not web, check if the activityMobileUrl is valid
			if(this.activityMobileUrl != null && this.activityMobileUrl.trim().length() != 0 
					&& !this.activityMobileUrl.trim().equals("#")){
				jObject.addProperty("activityURL", this.activityMobileUrl);
			}
			else{
				if (this.activityUrl == null || this.activityUrl.trim().equals("") || this.activityUrl.trim().equals("#")) {
					jObject.addProperty("activityURL", ConfigHelper.getActivityUrl() + activityId);
				} else {
					jObject.addProperty("activityURL", this.getActivityUrl());
				}
			}
		}
		if (platform == PlatformEnum.ANDROID) { // 移动终端
			if (this.topMobileUrl != null && !this.topMobileUrl.trim().equals(""))
				jObject.addProperty("topMobileURL", this.topMobileUrl);
		} else if (platform == PlatformEnum.IPHONE) {
			if (this.topMobileUrlIOS != null && !this.topMobileUrlIOS.trim().equals(""))
				jObject.addProperty("topMobileURL", this.topMobileUrlIOS);
		} else if (platform == PlatformEnum.IPAD) {
			if (this.topMobileUrlIOS != null && !this.topMobileUrlIOS.trim().equals(""))
				jObject.addProperty("topMobileURL", this.topMobileUrlIOS);
		}
		
		if(lGenTime > lastReadTime && lStartTime <= currentTime && lEndTime >= currentTime)
			jObject.addProperty("isnew", 1);
		else 
			jObject.addProperty("isnew", 0);
		return jObject;
	}
	
	public boolean isEffective(long currentTime){
		Long lStartTime =  new Long(this.getStartDate().getTime());
		Long lEndTime = new Long(this.getEndDate().getTime());
		return lStartTime <= currentTime && lEndTime >= currentTime;
	}

}

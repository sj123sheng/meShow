package com.melot.kktv.model;

import java.util.Date;

import com.google.gson.JsonObject;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.PlatformEnum;

/**
 * 活动类
 * 
 * @author liyue
 * 
 */
public class Activity {
	private Integer activityId;
	private String activityTitle;
	private String imgURL;
	private String activityURL;
	private Date dtime;
	private String content;
	private String topURL;
	private String topMobileURL;
	private String topMobileURLIOS;
	private String sharedText;
	private String sharedImgURL;
	
	/**
	 * 转成JsonObject
	 * 
	 * @return JsonObject
	 */
	public JsonObject toJsonObject(int platform) {
		JsonObject jObject = new JsonObject();
		jObject.addProperty("activityId", this.getActivityId());
		if (this.getActivityTitle() != null) {
			jObject.addProperty("activityTitle", this.getActivityTitle());
		}
		jObject.addProperty("imgURL", this.getImgURL());
		if (this.activityURL == null || this.activityURL.trim().equals("") || this.activityURL.trim().equals("#")) {
			jObject.addProperty("activityURL", ConfigHelper.getActivityUrl() + activityId);
		} else {
			jObject.addProperty("activityURL", this.getActivityURL());
		}
		if (this.getDtime() != null) {
			jObject.addProperty("activityTime", this.getDtime().getTime());
		}
		if (this.content != null && !this.content.trim().equals("")) {
			jObject.addProperty("content", this.getContent());
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
		if (this.sharedText != null && !this.sharedText.trim().isEmpty()) {
			jObject.addProperty("sharedText", this.sharedText);
		}
		if (this.sharedImgURL != null && !this.sharedImgURL.trim().isEmpty()) {
			jObject.addProperty("sharedImgURL", this.sharedImgURL);
		}
		return jObject;
	}

	public Integer getActivityId() {
		return activityId;
	}

	public void setActivityId(Integer activityId) {
		this.activityId = activityId;
	}
	
	public String getActivityTitle() {
		return activityTitle;
	}

	public void setActivityTitle(String activityTitle) {
		this.activityTitle = activityTitle;
	}

	public String getImgURL() {
		return imgURL;
	}

	public void setImgURL(String imgURL) {
		this.imgURL = imgURL;
	}

	public String getActivityURL() {
		return activityURL;
	}

	public void setActivityURL(String activityURL) {
		this.activityURL = activityURL;
	}

	public Date getDtime() {
		return dtime;
	}

	public void setDtime(Date dtime) {
		this.dtime = dtime;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
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
	
}

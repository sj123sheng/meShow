package com.melot.kktv.model;

import java.util.Date;

import com.google.gson.JsonObject;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.PlatformEnum;

/**
 * 精彩推荐
 */
public class RecommendedMsg {
	
	private Integer msgId;
	private String title;
	private String content;
	private String imgUrl;//image url for android
	private String url;//the link url
	private String imgUrlIOS;//image url for ios
	private Date startTime;
	
	private Integer activityId;
	private String message;
	private Date genDate;
	private Date endDate;
	private String activityMobileUrl;
	private String activityUrl;
	private String topUrl;
	private String topMobileUrl;
	private String topMobileUrlIOS;
	private String shareImgUrl;
	
	/** 
	 * @return JsonObject
	 */
	public JsonObject toJsonObject(long lastReadTime,int platform,long currentTime) {
	  if(null != this.getMsgId()){
		  JsonObject jObject = new JsonObject();
			jObject.addProperty("id", this.getMsgId());
			jObject.addProperty("title",this.getTitle());
			jObject.addProperty("message", this.getContent());
			jObject.addProperty("msgtime", this.getStartTime().getTime());
			if(this.url!=null)
				jObject.addProperty("activityURL", this.getUrl());
			if (platform == PlatformEnum.IPHONE || platform == PlatformEnum.IPAD) {
				if (this.imgUrlIOS != null && !this.imgUrlIOS.trim().equals(""))
					jObject.addProperty("img",  ConfigHelper.getHttpdir() + this.imgUrlIOS);
			} else {
				if (this.imgUrl!= null && !this.imgUrl.trim().equals(""))
					jObject.addProperty("img", ConfigHelper.getHttpdir() +this.imgUrl);
			}
			//取得redis时间戳 返回isnew
			if(lastReadTime>0 && lastReadTime <= this.getStartTime().getTime())
				jObject.addProperty("isnew", 1);
			else 
				jObject.addProperty("isnew", 0);
			return jObject;
	  }else{
		  JsonObject jObject = new JsonObject();
			jObject.addProperty("id", this.getActivityId());
			if(this.getTitle() != null)
				jObject.addProperty("title", this.getTitle());
			if(this.getMessage() != null)
				jObject.addProperty("message", this.getMessage());
			Long lStartTime =  new Long(this.getStartTime().getTime());
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
	}

	public boolean isEffective(long currentTime){
		Long lStartTime =  new Long(this.getStartTime().getTime());
		Long lEndTime = new Long(this.getEndDate().getTime());
		return lStartTime <= currentTime && lEndTime >= currentTime;
	}
	
	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Integer getMsgId() {
		return msgId;
	}

	public void setMsgId(Integer MsgId) {
		this.msgId = MsgId;
	}
	
	public String getImgUrl() {
		return imgUrl;
	}

	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getImgUrlIOS(){
		return this.imgUrlIOS;
	}
	
	public void setImgUrlIOS(String imgUrlIOS) {
		this.imgUrlIOS = imgUrlIOS;
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
    
    public String getShareImgUrl() {
        return shareImgUrl;
    }
    
    public void setShareImgUrl(String shareImgUrl) {
        this.shareImgUrl = shareImgUrl;
    }
	
}

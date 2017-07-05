package com.melot.kktv.domain;

import java.util.Date;

/**
 * 类UserInfo.java的实现描述：用户注册信息实体
 * 
 * @author chengqiang 2014年9月3日 上午10:38:05
 */
public class UserInfo {
	
	private Integer	 userId;
	private String nickname;
	private Integer gender;
	private String phone;
    private String signature;
    private String introduce;
    private Integer iconTag;
    private Integer actorTag;
    private String birthday;
    private Integer bloodType;
    private Integer bodyType;
    private Integer city;
    private Integer showMoney;
    private Integer liveVideoQuality;
    private Integer openPlatform;
	private Date registerTime;
    private String portrait_path;
    private String background_path;
    private Integer backgroundShow;
    private Integer backgroundScroll;
    private String noticeContent;
    private String noticeHref;
    private String greetMsg;
    private String greetMsgHref;
    
	private Integer actorLevel;
	private Integer richLevel;
	
	private String roomTheme;
	private String userPart;
	
	private Integer area;
	private Integer fansCount;
    private Integer followCount;
	
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public Integer getGender() {
		return gender;
	}
	public void setGender(Integer gender) {
		this.gender = gender;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getSignature() {
		return signature;
	}
	public void setSignature(String signature) {
		this.signature = signature;
	}
	public String getIntroduce() {
		return introduce;
	}
	public void setIntroduce(String introduce) {
		this.introduce = introduce;
	}
	public Integer getIconTag() {
		return iconTag;
	}
	public void setIconTag(Integer iconTag) {
		this.iconTag = iconTag;
	}
	public Integer getActorTag() {
		return actorTag;
	}
	public void setActorTag(Integer actorTag) {
		this.actorTag = actorTag;
	}
	public String getBirthday() {
		return birthday;
	}
	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}
	public Integer getBloodType() {
		return bloodType;
	}
	public void setBloodType(Integer bloodType) {
		this.bloodType = bloodType;
	}
	public Integer getBodyType() {
		return bodyType;
	}
	public void setBodyType(Integer bodyType) {
		this.bodyType = bodyType;
	}
	public Integer getCity() {
		return city;
	}
	public void setCity(Integer city) {
		this.city = city;
	}
	public String getPortrait_path() {
		return portrait_path;
	}
	public void setPortrait_path(String portrait_path) {
		this.portrait_path = portrait_path;
	}
	public String getBackground_path() {
		return background_path;
	}
	public void setBackground_path(String background_path) {
		this.background_path = background_path;
	}
	public Integer getBackgroundShow() {
		return backgroundShow;
	}
	public void setBackgroundShow(Integer backgroundShow) {
		this.backgroundShow = backgroundShow;
	}
	public Integer getBackgroundScroll() {
		return backgroundScroll;
	}
	public void setBackgroundScroll(Integer backgroundScroll) {
		this.backgroundScroll = backgroundScroll;
	}
	public Integer getShowMoney() {
		return showMoney;
	}
	public void setShowMoney(Integer showMoney) {
		this.showMoney = showMoney;
	}
	public String getNoticeContent() {
		return noticeContent;
	}
	public void setNoticeContent(String noticeContent) {
		this.noticeContent = noticeContent;
	}
	public String getNoticeHref() {
		return noticeHref;
	}
	public void setNoticeHref(String noticeHref) {
		this.noticeHref = noticeHref;
	}
	public String getGreetMsg() {
		return greetMsg;
	}
	public void setGreetMsg(String greetMsg) {
		this.greetMsg = greetMsg;
	}
	public String getGreetMsgHref() {
		return greetMsgHref;
	}
	public void setGreetMsgHref(String greetMsgHref) {
		this.greetMsgHref = greetMsgHref;
	}
	public Integer getLiveVideoQuality() {
		return liveVideoQuality;
	}
	public void setLiveVideoQuality(Integer liveVideoQuality) {
		this.liveVideoQuality = liveVideoQuality;
	}
	public Integer getOpenPlatform() {
		return openPlatform;
	}
	public void setOpenPlatform(Integer openPlatform) {
		this.openPlatform = openPlatform;
	}
	public Date getRegisterTime() {
		return registerTime;
	}
	public void setRegisterTime(Date registerTime) {
		this.registerTime = registerTime;
	}
	public Integer getActorLevel() {
		return actorLevel;
	}
	public void setActorLevel(Integer actorLevel) {
		this.actorLevel = actorLevel;
	}
	public Integer getRichLevel() {
		return richLevel;
	}
	public void setRichLevel(Integer richLevel) {
		this.richLevel = richLevel;
	}
    public String getRoomTheme() {
        return roomTheme;
    }
    public void setRoomTheme(String roomTheme) {
        this.roomTheme = roomTheme;
    }
    public String getUserPart() {
        return userPart;
    }
    public void setUserPart(String userPart) {
        this.userPart = userPart;
    }
    public Integer getArea() {
        return area;
    }
    public void setArea(Integer area) {
        this.area = area;
    }
    public Integer getFansCount() {
        return fansCount;
    }
    public void setFansCount(Integer fansCount) {
        this.fansCount = fansCount;
    }
    public Integer getFollowCount() {
        return followCount;
    }
    public void setFollowCount(Integer followCount) {
        this.followCount = followCount;
    }
	
}

package com.melot.kkgame.model;

import java.util.Date;

public class UserModel {
	
	/** 用户编号 */
	private Integer userId;
	
	/** 用户昵称 */
	private String nickname;
	
	/** 用户性别 */
	private Integer gender;
	
	/** 手机号码 */
	private String phone;
	
	/** 签名 */
    private String signature;
    
    /** 用户简介 */
	private String introduce;
	
	/** 地区 */
	private Integer city;
	
	/** 省份 */
	private Integer area;
	
	/** 粉丝数 */
	private Integer fansCount;
	
	/** 关注数 */
	private Integer followCount;
	
	/** 是否主播 */
    private Integer actorTag;
    
    /** 生日 */
    private String birthday;
    
    /** 秀币 */
    private Integer showMoney;
    
    /** 注册方式 */
    private Integer openPlatform;
    
    /** 注册时间 */
	private Date registerTime;
	
	/** 注册产品号 */
	private Integer appId;
	
	/** 头像 */
    private String portraitPath;
    
    /** 海报 */
	private String posterPath;
	
    /** 主播等级 */
	private Integer actorLevel;
	
	/** 富豪等级 */
	private Integer richLevel;
	
	/** 直播类型 */
	private Integer liveType;
	
	/** 开始直播时间 */
	private Date liveStartTime;
	
	/** 结束直播时间 */
	private Date liveEndTime;
	
	/** 下次直播时间 */
	private Date nextStartTime;
	
	/** 房间主题 */
	private String roomTheme;
	
	/** 房间模式 */
	private Integer roomMode;
	
	/** 主播来源 */
	private Integer roomSource;
	
	private String noticeContent;
    private String noticeHref;
    private String greetMsg;
    private String greetMsgHref;
	
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
	public Integer getCity() {
		return city;
	}
	public void setCity(Integer city) {
		this.city = city;
	}

	public String getPortraitPath() {
        return portraitPath;
    }
    public void setPortraitPath(String portraitPath) {
        this.portraitPath = portraitPath;
    }
    public String getPosterPath() {
        return posterPath;
    }
    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
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
	public Integer getLiveType() {
		return liveType;
	}
	public void setLiveType(Integer liveType) {
		this.liveType = liveType;
	}
	public Date getLiveStartTime() {
		return liveStartTime;
	}
	public void setLiveStartTime(Date liveStartTime) {
		this.liveStartTime = liveStartTime;
	}
	public Date getLiveEndTime() {
		return liveEndTime;
	}
	public void setLiveEndTime(Date liveEndTime) {
		this.liveEndTime = liveEndTime;
	}
	public Date getNextStartTime() {
		return nextStartTime;
	}
	public void setNextStartTime(Date nextStartTime) {
		this.nextStartTime = nextStartTime;
	}
	public Integer getRoomMode() {
		return roomMode;
	}
	public void setRoomMode(Integer roomMode) {
		this.roomMode = roomMode;
	}
	public Integer getRoomSource() {
		return roomSource;
	}
	public void setRoomSource(Integer roomSource) {
		this.roomSource = roomSource;
	}
	public Integer getArea() {
		return area;
	}
	public void setArea(Integer area) {
		this.area = area;
	}
	public Integer getAppId() {
		return appId;
	}
	public void setAppId(Integer appId) {
		this.appId = appId;
	}
    
}
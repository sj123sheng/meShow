package com.melot.kktv.model;

import java.util.Date;

import com.google.gson.JsonObject;
import com.melot.kkcx.service.UserService;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.PlatformEnum;

@SuppressWarnings("unused")
public class FamilyApplicant {

	private Integer familyId;
	private Integer userId;
	private String nickname;
	private String portrait_path_original;
    private String portrait_path_1280;
	private String portrait_path_256;
	private String portrait_path_128;
	private String portrait_path_48;
	private Integer actorTag;
	private Integer applyState;
	private Date applyTime;
	private Integer baseNumber;
	private Integer introductionWay;
	
	/**
	 * Convert Java Object to JsonObject
	 * @param platform
	 * @return
	 */
	public JsonObject toJsonObject(int platform) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("userId", this.userId);
		jsonObject.addProperty("nickname", this.nickname);
		jsonObject.addProperty("actorTag", this.actorTag);
		jsonObject.addProperty("applyTime", this.applyTime.getTime());
		jsonObject.addProperty("baseNumber", this.baseNumber);
		jsonObject.addProperty("introductionWay", this.introductionWay);
		if (this.portrait_path_original != null) {
			switch (platform) {
			case PlatformEnum.WEB:
				jsonObject.addProperty("portrait_path_256", this.portrait_path_original + "!256");
				break;
			case PlatformEnum.ANDROID:
			case PlatformEnum.IPHONE:
			case PlatformEnum.IPAD:
				jsonObject.addProperty("portrait_path_128", this.portrait_path_original + "!128");
				break;
			default:
				break;
			}
		}
		jsonObject.addProperty("actorLevel", UserService.getActorLevel(this.userId));
		jsonObject.addProperty("richLevel", UserService.getRichLevel(this.userId));
		jsonObject.addProperty("roomSource", AppIdEnum.AMUSEMENT);
		jsonObject.addProperty("roomType", AppIdEnum.AMUSEMENT);
		return jsonObject;
	}
	
	public Integer getFamilyId() {
		return familyId;
	}
	public void setFamilyId(Integer familyId) {
		this.familyId = familyId;
	}
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
	public String getPortrait_path_original() {
		return portrait_path_original;
	}
	public void setPortrait_path_original(String portrait_path_original) {
		this.portrait_path_original = portrait_path_original;
	}
	public String getPortrait_path_1280() {
		return portrait_path_original + "!1280";
	}
	public void setPortrait_path_1280(String portrait_path_original) {
		this.portrait_path_1280 = portrait_path_original + "!1280";
	}
	public String getPortrait_path_256() {
		return portrait_path_original + "!256";
	}
	public void setPortrait_path_256(String portrait_path_original) {
		this.portrait_path_256 = portrait_path_original + "!256";
	}
	public String getPortrait_path_128() {
		return portrait_path_original + "!128";
	}
	public void setPortrait_path_128(String portrait_path_original) {
		this.portrait_path_128 = portrait_path_original + "!128";
	}
	public String getPortrait_path_48() {
		return portrait_path_original + "!48";
	}
	public void setPortrait_path_48(String portrait_path_original) {
		this.portrait_path_48 = portrait_path_original + "!48";
	}
	public Date getApplyTime() {
		return applyTime;
	}
	public void setApplyTime(Date applyTime) {
		this.applyTime = applyTime;
	}
	public Integer getActorTag() {
		return actorTag;
	}
	public void setActorTag(Integer actorTag) {
		this.actorTag = actorTag;
	}
	public Integer getApplyState() {
		return applyState;
	}
	public void setApplyState(Integer applyState) {
		this.applyState = applyState;
	}
	public Integer getBaseNumber() {
		return baseNumber;
	}
	public void setBaseNumber(Integer baseNumber) {
		this.baseNumber = baseNumber;
	}
	public Integer getIntroductionWay() {
		return introductionWay;
	}
	public void setIntroductionWay(Integer introductionWay) {
		this.introductionWay = introductionWay;
	}
	
}

package com.melot.kktv.model;

import java.util.Date;
import java.util.Map;

import com.google.gson.JsonObject;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcx.service.UserService;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.FamilyMemberEnum;
import com.melot.kktv.util.PlatformEnum;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import org.apache.log4j.Logger;

@SuppressWarnings("unused")
public class FamilyMember {

	private static Logger logger = Logger.getLogger(FamilyMember.class);

	private Integer familyId;
	private Integer memberId;
	private Integer userId;
	private String nickname;
	private Integer actorTag;
	private Integer memberGrade;
	private Date joinDate;
	private Long joinTime;
	private String portrait_path_original;
	private String portrait_path_1280;
	private String portrait_path_256;
	private String portrait_path_128;
	private String portrait_path_48;
	private Integer actorLevel;
	private Integer richLevel;
	
    public void initJavaBean(com.melot.family.driver.domain.FamilyMember familyMember, int platform) {
        try {
            this.familyId = familyMember.getFamilyId();
            this.memberId = familyMember.getMemberId();
            this.userId = familyMember.getUserId();
            this.memberGrade = familyMember.getMemberGrade();
            if (familyMember.getJoinTime() != null) {
                this.joinTime = familyMember.getJoinTime().getTime();
            }
            UserProfile userProfile = UserService.getUserInfoNew(this.userId);
            if (userProfile != null) {
                this.nickname = userProfile.getNickName();
                this.actorTag = userProfile.getIsActor();
                this.portrait_path_original = userProfile.getPortrait();
                if (this.portrait_path_original != null) {
                    switch (platform) {
                    case PlatformEnum.WEB:
                        this.portrait_path_256 = this.portrait_path_original + "!256";
                        break;
                    case PlatformEnum.ANDROID :
                    case PlatformEnum.IPHONE:
                    case PlatformEnum.IPAD:
                        this.portrait_path_128 = this.portrait_path_original + "!128";
                        break;
                    default:
                        break;
                    }
                }
                this.actorLevel = userProfile.getActorLevel();
                this.richLevel = userProfile.getUserLevel();
            }
        } catch (Exception e) {
            logger.error("FamilyMember.initJavaBean(" + "familyMember:" + familyMember + "platform:" + platform + ") execute exception.", e);
        }
    }
	
	/**
	 * Convert Java Object to JsonObject
	 * @param platform
	 * @return
	 */
	public JsonObject toJsonObject(int platform) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("familyId", this.familyId);
		jsonObject.addProperty("userId", this.userId);
		jsonObject.addProperty("memberId", this.memberId);
		jsonObject.addProperty("nickname", this.nickname);
		jsonObject.addProperty("actorTag", this.actorTag);
		jsonObject.addProperty("memberGrade", this.memberGrade);
		jsonObject.addProperty("joinTime", this.joinDate.getTime());
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
	
	/**
	 * Convert Java Object to DBObject
	 * Save into MongoDB
	 * @return
	 */
	public DBObject toDBObject() {
		DBObject dbObject = new BasicDBObject();
		dbObject.put("familyId", this.familyId);
		dbObject.put("userId", this.userId);
		dbObject.put("memberId", this.memberId);
		dbObject.put("nickname", this.nickname);
		if (this.portrait_path_original != null) {
			dbObject.put("portrait_path_original", this.portrait_path_original);
		}
		dbObject.put("actorTag", this.actorTag);
		dbObject.put("memberGrade", this.memberGrade);
		dbObject.put("joinTime", this.joinDate.getTime());
		return dbObject;
	}
	
	public Integer getFamilyId() {
		return familyId;
	}
	public void setFamilyId(Integer familyId) {
		this.familyId = familyId;
	}
	public Integer getMemberId() {
		return memberId;
	}
	public void setMemberId(Integer memberId) {
		this.memberId = memberId;
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
	public Integer getActorTag() {
		return actorTag;
	}
	public void setActorTag(Integer actorTag) {
		this.actorTag = actorTag;
	}
	public Integer getMemberGrade() {
		return memberGrade;
	}
	public void setMemberGrade(Integer memberGrade) {
		this.memberGrade = memberGrade;
	}
	public Long getJoinTime() {
		return joinTime;
	}
	public void setJoinTime(Long joinTime) {
		this.joinTime = joinTime;
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

	public Date getJoinDate() {
		return joinDate;
	}

	public void setJoinDate(Date joinDate) {
		this.joinDate = joinDate;
	}
	
}

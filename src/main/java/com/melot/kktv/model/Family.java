package com.melot.kktv.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.melot.kktv.util.PlatformEnum;
import com.mongodb.DBObject;

public class Family {
	
	private Integer familyId;
	private String familyName;
	private Integer familyMedal;
	private JsonObject familyPoster;
	private Integer memberCount;
	private Integer actorCount;
	private Integer familyLeader;
	private String familyNotice;
	private Long createTime;
	
	private Integer maxCount;
	
	private Integer familyRoomId; //家族房Id
	
	private Integer open;
	
	public Integer getFamilyId() {
		return this.familyId;
	}

	public void setFamilyId(Integer familyId) {
		this.familyId = familyId;
	}

	public String getFamilyName() {
		return this.familyName;
	}

	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}
	
	public Integer getMemberCount() {
		return this.memberCount;
	}

	public void setMemberCount(Integer memberCount) {
		this.memberCount = memberCount;
	}
	
	public Integer getFamilyMedal() {
		return familyMedal;
	}

	public void setFamilyMedal(Integer familyMedal) {
		this.familyMedal = familyMedal;
	}

	public JsonObject getFamilyPoster() {
		return familyPoster;
	}

	public void setFamilyPoster(JsonObject familyPoster) {
		this.familyPoster = familyPoster;
	}

	public Integer getActorCount() {
		return actorCount;
	}

	public void setActorCount(Integer actorCount) {
		this.actorCount = actorCount;
	}

	public Integer getFamilyLeader() {
		return familyLeader;
	}

	public void setFamilyLeader(Integer familyLeader) {
		this.familyLeader = familyLeader;
	}
	
	public String getFamilyNotice() {
		return familyNotice;
	}

	public void setFamilyNotice(String familyNotice) {
		this.familyNotice = familyNotice;
	}

	public Long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}

	public Integer getMaxCount() {
		return maxCount;
	}

	public void setMaxCount(Integer maxCount) {
		this.maxCount = maxCount;
	}

	public Integer getFamilyRoomId() {
        return familyRoomId;
    }

    public void setFamilyRoomId(Integer familyRoomId) {
        this.familyRoomId = familyRoomId;
    }

    public Integer getOpen() {
        return open;
    }

    public void setOpen(Integer open) {
        this.open = open;
    }

    public void initJavaBean(DBObject dbObj, int platform) {
		this.familyId = (Integer) dbObj.get("familyId");
		if (dbObj.containsField("familyName")) this.familyName = (String) dbObj.get("familyName");
		if (dbObj.containsField("memberCount")) this.memberCount = (Integer) dbObj.get("memberCount");
		if (dbObj.containsField("actorCount")) this.actorCount = (Integer) dbObj.get("actorCount");
		if (dbObj.containsField("familyNotice")) this.familyNotice = (String) dbObj.get("familyNotice");
		if (dbObj.containsField("createTime")) this.createTime = (Long) dbObj.get("createTime");
		if (dbObj.containsField("familyMedal")) this.familyMedal = (Integer) dbObj.get("familyMedal");
		if (dbObj.containsField("familyLeader")) this.familyLeader = (Integer) dbObj.get("familyLeader");
		if (dbObj.containsField("maxCount")) this.maxCount = (Integer) dbObj.get("maxCount");
		if (dbObj.containsField("familyRoomId")) this.familyRoomId = (Integer) dbObj.get("familyRoomId");
		if (dbObj.containsField("open")) this.open = (Integer) dbObj.get("open");
		if (dbObj.containsField("familyPoster")) {
			String familyPosterStr = (String) dbObj.get("familyPoster");
			try {
				FamilyPoster familyPoster = new Gson().fromJson(familyPosterStr, FamilyPoster.class);
				switch (platform) {
				case PlatformEnum.WEB:
					// 返回 222*148px 270*180px
					if (familyPoster.getPath_original() != null) {
						this.familyPoster = new JsonObject();
						this.familyPoster.addProperty("path_222", familyPoster.getPath_222());
						this.familyPoster.addProperty("path_270", familyPoster.getPath_270());
					}
					break;
				case PlatformEnum.ANDROID:
					// 返回 174*116px
					if (familyPoster.getPath_original() != null) {
						this.familyPoster = new JsonObject();
						this.familyPoster.addProperty("path_174", familyPoster.getPath_174());
					}
				case PlatformEnum.IPHONE:
					// 返回 222*148px
					if (familyPoster.getPath_original() != null) {
						this.familyPoster = new JsonObject();
						this.familyPoster.addProperty("path_222", familyPoster.getPath_222());
					}
				case PlatformEnum.IPAD:
					// 返回 222*148px
					if (familyPoster.getPath_original() != null) {
						this.familyPoster = new JsonObject();
						this.familyPoster.addProperty("path_222", familyPoster.getPath_222());
					}
					break;
				default:
					// 返回 174*116px 222*148px 270*180px
					if (familyPoster.getPath_original() != null) {
						this.familyPoster = new JsonObject();
						this.familyPoster.addProperty("path_174", familyPoster.getPath_174());
						this.familyPoster.addProperty("path_222", familyPoster.getPath_222());
						this.familyPoster.addProperty("path_270", familyPoster.getPath_270());
					}
					break;
				}
			} catch (Exception e) {
			}
		}
		
	}
	
}

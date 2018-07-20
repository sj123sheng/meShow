package com.melot.kktv.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.melot.kktv.util.PlatformEnum;

@SuppressWarnings("unused")
public class FamilyMatchChampion {

	private Integer familyId;
	private String familyName;
	private String familyPoster;
	private Integer tops;// 获取冠军次数
	private Integer continues;// 蝉联冠军次数
	private Integer userId;
	private String nickname;
	private String portrait_path_original;
	private String portrait_path_1280;
	private String portrait_path_256;
	private String portrait_path_128;
	private String portrait_path_48;
	
	public JsonObject toJsonObject(int platform) {
		JsonObject jsonObject = new JsonObject();
		if(this.userId!=null) {
			jsonObject.addProperty("userId", this.userId);
		}
		if(this.nickname!=null) {
			jsonObject.addProperty("nickname", this.nickname);
		}
		if(this.familyId!=null) {
			jsonObject.addProperty("familyId", this.familyId);
			jsonObject.addProperty("familyName", this.familyName);
			try {
				
				JsonObject posterJson = new JsonObject();
				FamilyPoster familyPoster = new Gson().fromJson(this.familyPoster, FamilyPoster.class);
				switch (platform) {
				case PlatformEnum.WEB:
					// 返回 222*148px 270*180px
					if (familyPoster.getPath_original()!=null) {
						posterJson.addProperty("path_222", familyPoster.getPath_original() + "!222"); 
						posterJson.addProperty("path_270", familyPoster.getPath_original() + "!270");
					}
					break;
				case PlatformEnum.ANDROID:
					// 返回 174*116px
					if (familyPoster.getPath_original()!=null) 
						posterJson.addProperty("path_174", familyPoster.getPath_original() + "!174");
					break;
				case PlatformEnum.IPHONE:
					// 返回 222*148px
					if (familyPoster.getPath_original()!=null) 
						posterJson.addProperty("path_222", familyPoster.getPath_original() + "!222");
					break;
				case PlatformEnum.IPAD:
					// 返回 222*148px
					if (familyPoster.getPath_original()!=null) 
						posterJson.addProperty("path_222", familyPoster.getPath_original() + "!222");
					break;
				default:
					// 返回 174*116px 222*148px 270*180px
					if (familyPoster.getPath_original()!=null) {
						posterJson.addProperty("path_174", familyPoster.getPath_original() + "!174");
						posterJson.addProperty("path_222", familyPoster.getPath_original() + "!222");
						posterJson.addProperty("path_270", familyPoster.getPath_original() + "!270");
					}
					break;
				}
				jsonObject.add("familyPoster", posterJson);
			} catch (Exception e) {}
		}
		if(this.tops!=null) {
			jsonObject.addProperty("tops", this.tops);
		}
		if(this.continues!=null) {
			jsonObject.addProperty("continues", this.continues);
		}
		if(this.portrait_path_original != null) {
			switch (platform) {
			case PlatformEnum.WEB:
				jsonObject.addProperty("portrait_path_256", this.portrait_path_original+"!256");
				break;
			case PlatformEnum.ANDROID:
				jsonObject.addProperty("portrait_path_128", this.portrait_path_original+"!128");
				break;
			case PlatformEnum.IPHONE:
				jsonObject.addProperty("portrait_path_128", this.portrait_path_original+"!128");
				break;
			case PlatformEnum.IPAD:
				jsonObject.addProperty("portrait_path_128", this.portrait_path_original+"!128");
				break;
			default:
				jsonObject.addProperty("portrait_path_original", this.portrait_path_original);
				jsonObject.addProperty("portrait_path_1280", this.portrait_path_original+"!1280");
				jsonObject.addProperty("portrait_path_256", this.portrait_path_original+"!256");
				jsonObject.addProperty("portrait_path_128", this.portrait_path_original+"!128");
				jsonObject.addProperty("portrait_path_48", this.portrait_path_original+"!48");
				break;
			}
		}
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

	public String getFamilyName() {
		return familyName;
	}

	public void setFamilyName(String familyName) {
		this.familyName = familyName;
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

	public String getFamilyPoster() {
		return familyPoster;
	}

	public void setFamilyPoster(String familyPoster) {
		this.familyPoster = familyPoster;
	}

	public Integer getTops() {
		return tops;
	}

	public void setTops(Integer tops) {
		this.tops = tops;
	}

	public Integer getContinues() {
		return continues;
	}

	public void setContinues(Integer continues) {
		this.continues = continues;
	}

}

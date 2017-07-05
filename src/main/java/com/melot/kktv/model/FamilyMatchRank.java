package com.melot.kktv.model;

import com.google.gson.JsonObject;
import com.melot.kktv.action.UserAssetAction;
import com.melot.kktv.redis.MedalSource;
import com.melot.kkcx.service.UserAssetServices;
import com.melot.kkcx.service.UserService;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.PlatformEnum;

@SuppressWarnings("unused")
public class FamilyMatchRank {

	private Integer familyId;
	private String familyName;
	private Integer userId;
	private String nickname;
	private String portrait_path_original;
	private String portrait_path_1280;
	private String portrait_path_256;
	private String portrait_path_128;
	private String portrait_path_48;
	private Double giftScore;
	private Integer giftCount;
	private Double fansScore;
	private Integer fansCount;
	private Double raterScore;
	private Integer rank;
	private Double vipScore;
	private Integer vipCount;
	
	public JsonObject toJsonObject(int platform) {
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("userId", this.userId);
		jsonObject.addProperty("nickname", this.nickname);
		jsonObject.addProperty("rank", this.rank);
		if(this.familyId!=null) {
			jsonObject.addProperty("familyId", this.familyId);
			jsonObject.addProperty("familyName", familyName);
		}
		if(this.giftScore!=null) {
			jsonObject.addProperty("giftScore", this.giftScore);
		}
		if(this.giftCount!=null) {
			jsonObject.addProperty("giftCount", this.giftCount);
		}
		if(this.fansScore!=null) {
			jsonObject.addProperty("fansScore", this.fansScore);
		}
		if(this.fansCount!=null) {
			jsonObject.addProperty("fansCount", this.fansCount);
		}
		if(this.raterScore!=null) {
			jsonObject.addProperty("raterScore", this.raterScore);
		}
//		if(this.vipScore!=null) {
//			jsonObject.addProperty("vipScore", this.vipScore);
//		}
//		if(this.vipCount!=null) {
//			jsonObject.addProperty("vipCount", this.vipCount);
//		}
		
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
		
		double totalScore = 0;
		if (this.raterScore!=null) totalScore += this.raterScore.doubleValue();
		if (this.giftScore!=null) totalScore += this.giftScore.doubleValue();
		if (this.fansScore!=null) totalScore += this.fansScore.doubleValue();
//		if (this.vipScore!=null) totalScore += this.vipScore.doubleValue();
		jsonObject.addProperty("totalScore", totalScore);

		// 添加勋章信息
		jsonObject.add("userMedal", MedalSource.getUserMedalsAsJson(this.userId, platform));
		
		// 获取用户有效靓号
		JsonObject validVirtualId =  UserAssetServices.getValidVirtualId(this.userId); //获取用户虚拟账号
		if(validVirtualId != null) {
			if (validVirtualId.get("idType").getAsInt() == 1) {
				// 支持老版靓号
				jsonObject.addProperty("luckyId", validVirtualId.get("id").getAsInt());
			}
			jsonObject.add("validId", validVirtualId);
		}
		// 读取富豪等级
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
	public Double getGiftScore() {
		return giftScore;
	}
	public void setGiftScore(Double giftScore) {
		this.giftScore = giftScore;
	}
	public Double getRaterScore() {
		return raterScore;
	}
	public void setRaterScore(Double raterScore) {
		this.raterScore = raterScore;
	}
	public Integer getRank() {
		return rank;
	}
	public void setRank(Integer rank) {
		this.rank = rank;
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

	public Integer getGiftCount() {
		return giftCount;
	}

	public void setGiftCount(Integer giftCount) {
		this.giftCount = giftCount;
	}

	public Double getFansScore() {
		return fansScore;
	}

	public void setFansScore(Double fansScore) {
		this.fansScore = fansScore;
	}

	public Integer getFansCount() {
		return fansCount;
	}

	public void setFansCount(Integer fansCount) {
		this.fansCount = fansCount;
	}

	public Double getVipScore() {
		return vipScore;
	}

	public Integer getVipCount() {
		return vipCount;
	}

	public void setVipScore(Double vipScore) {
		this.vipScore = vipScore;
	}

	public void setVipCount(Integer vipCount) {
		this.vipCount = vipCount;
	}

}

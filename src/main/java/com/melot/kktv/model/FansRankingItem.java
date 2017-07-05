package com.melot.kktv.model;

import com.google.gson.JsonObject;
import com.melot.kktv.redis.MedalSource;
import com.melot.kkcx.service.UserAssetServices;
import com.melot.kkcx.service.UserService;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.PlatformEnum;

/**
 * 粉丝排行榜类
 * 
 * @author Administrator
 * 
 */
public class FansRankingItem {
	private Integer userId;
	private String nickname;
	private Integer gender;
	private String portrait;
	private Long contribution;
	private Integer roomSource;
	
	/**
	 * 转成JsonObject
	 * 
	 * @return JsonObject
	 */
	public JsonObject toJsonObject(int platform, int appId) {
		JsonObject jObject = new JsonObject();
		jObject.addProperty("userId", this.getUserId());
		jObject.addProperty("roomId", this.getUserId());
		jObject.addProperty("contribution", this.getContribution());
		if (this.getNickname() != null) {
			jObject.addProperty("nickname", this.getNickname());
		}
		if (this.getGender() != null) {
			jObject.addProperty("gender", this.getGender());
		}
		if (this.getPortrait() != null) {
			if (platform == PlatformEnum.ANDROID || platform == PlatformEnum.IPHONE || platform == PlatformEnum.IPAD) {
				jObject.addProperty("portrait_path_128", ConfigHelper.getHttpdir() + this.getPortrait() + "!128");
			} else if (platform == PlatformEnum.WEB) {
				jObject.addProperty("portrait_path_256", ConfigHelper.getHttpdir() + this.getPortrait() + "!256");
			}
		}
		// 读取明星等级
		jObject.addProperty("actorLevel", UserService.getActorLevel(this.getUserId()));
		// 读取富豪等级
		jObject.addProperty("richLevel", UserService.getRichLevel(this.getUserId()));
		// 读取靓号
		JsonObject validVirtualId =  UserAssetServices.getValidVirtualId(this.getUserId()); //获取用户虚拟账号
		if(validVirtualId != null) {
			if (validVirtualId.get("idType").getAsInt() == 1) {
				// 支持老版靓号
				jObject.addProperty("luckyId", validVirtualId.get("id").getAsInt());
			}
			jObject.add("validId", validVirtualId);
		}
		// 添加勋章信息
		jObject.add("userMedal", MedalSource.getUserMedalsAsJson(this.getUserId(), platform));
		if (this.getRoomSource() != null) {
		    jObject.addProperty("roomSource", this.getRoomSource());
		    jObject.addProperty("roomType", this.getRoomSource());
		} else {
			jObject.addProperty("roomSource", appId);
			jObject.addProperty("roomType", appId);
		}
		return jObject;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Long getContribution() {
		return contribution;
	}

	public void setContribution(Long contribution) {
		this.contribution = contribution;
	}

    public Integer getRoomSource() {
        return roomSource;
    }

    public void setRoomSource(Integer roomSource) {
        this.roomSource = roomSource;
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

	public String getPortrait() {
		return portrait;
	}

	public void setPortrait(String portrait) {
		this.portrait = portrait;
	}

}

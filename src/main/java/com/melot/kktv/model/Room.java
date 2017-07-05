package com.melot.kktv.model;

import java.util.Date;

import com.google.gson.JsonObject;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcx.service.UserAssetServices;
import com.melot.kkcx.service.UserService;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.PlatformEnum;

/**
 * 房间信息
 * 
 * @author Administrator
 * 
 */
public class Room {
	private Integer userId;
	private String nickname;
	private String signature;
	private Integer gender;
	private String portrait_path_original;
	private Integer iconTag;
	private Integer maxCount;
	private Date livestarttime;
	private Date liveendtime;
	private Date nextstarttime;
	private Integer fansCount;
	private String enterConditionType;
	private Integer city;
	private String birthday;
	private String roomTheme;
	
	private Integer roomId;
	private Integer onlineCount;
	private Integer liveType;
	private Integer actorLevel;
	private Integer richLevel;
	private Integer isRookie;
	private Integer isWeekly;
	private Integer videoLevel;
	private Integer roomMode;
	private Integer actorTag;
	private Integer roomSource;

	/**
	 * 转成JsonObject
	 * 
	 * @return JsonObject
	 */
	public JsonObject toJsonObject(Integer platform, RoomInfo roomInfo) {
		JsonObject jObject = new JsonObject();
		jObject.addProperty("userId", this.getUserId());
		jObject.addProperty("roomId", this.getUserId());
		jObject.addProperty("nickname", this.getNickname());
		jObject.addProperty("gender", this.getGender());
		if (platform.equals(PlatformEnum.WEB)) {
			if (this.getPortrait_path_original() != null) {
				jObject.addProperty("portrait_path_256", ConfigHelper.getHttpdir() + this.getPortrait_path_256());
			}
		} else if (platform.equals(PlatformEnum.ANDROID) 
				|| platform.equals(PlatformEnum.IPHONE)
				|| platform.equals(PlatformEnum.IPAD)) {
			if (this.getPortrait_path_original() != null) {
				jObject.addProperty("portrait_path_128", ConfigHelper.getHttpdir() + this.getPortrait_path_128());
			}
		}
		
		if (roomInfo != null) {
		    if (roomInfo.getScreenType() != null) {
		        jObject.addProperty("screenType", roomInfo.getScreenType());
            } else {
                jObject.addProperty("screenType", 1);
            }
		    if (roomInfo.getActorLevel() != null) {
		        jObject.addProperty("actorLevel", roomInfo.getActorLevel());
		    }
		    if (roomInfo.getRichLevel() != null) {
		        jObject.addProperty("richLevel", roomInfo.getRichLevel());
		    }
			if (roomInfo.getPeopleInRoom() != null) {
	            jObject.addProperty("onlineCount", roomInfo.getPeopleInRoom());
	        }
			if (roomInfo.getLiveType() != null) {
	            jObject.addProperty("liveType", roomInfo.getLiveType());
	        }
	        if (roomInfo.getLiveStarttime() != null) {
	            jObject.addProperty("livestarttime", roomInfo.getLiveStarttime().getTime());
	        }
	        if (roomInfo.getLiveEndtime() != null) {
	            jObject.addProperty("liveendtime", roomInfo.getLiveEndtime().getTime());
	        }
	        if (roomInfo.getNextStarttime() != null) {
	            jObject.addProperty("nextstarttime", roomInfo.getNextStarttime().getTime());
	        }
	        if (roomInfo.getRoomMode() != null) {
	        	jObject.addProperty("roomMode", roomInfo.getRoomMode());
	        }
            if (roomInfo.getRoomSource() != null) {
                jObject.addProperty("roomSource", roomInfo.getRoomSource());
            } else {
                jObject.addProperty("roomSource", AppIdEnum.AMUSEMENT);
            }
            if (roomInfo.getType() != null) {
                jObject.addProperty("roomType", roomInfo.getType());
            } else {
                jObject.addProperty("roomType", AppIdEnum.AMUSEMENT);
            }
		} else {
		    UserProfile userProfile = com.melot.kktv.service.UserService.getUserInfoV2(this.userId);
            if (userProfile != null) {
                // 读取明星等级
                jObject.addProperty("actorLevel", userProfile.getActorLevel());
                // 读取富豪等级
                jObject.addProperty("richLevel", userProfile.getUserLevel());
            }
		}
		
		// 读取星级
        jObject.addProperty("starLevel", UserService.getStarLevel(this.getUserId()));
		
		// 读取靓号
		JsonObject validVirtualId =  UserAssetServices.getValidVirtualId(this.getUserId()); //获取用户虚拟账号
		if(validVirtualId != null) {
			if (validVirtualId.get("idType").getAsInt() == 1) {
				// 支持老版靓号
				jObject.addProperty("luckyId", validVirtualId.get("id").getAsInt());
			}
			jObject.add("validId", validVirtualId);
		}
		
		return jObject;
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

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public Integer getGender() {
		return gender;
	}

	public void setGender(Integer gender) {
		this.gender = gender;
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

	public String getPortrait_path_256() {
		return portrait_path_original + "!256";
	}

	public String getPortrait_path_128() {
		return portrait_path_original + "!128";
	}

	public String getPortrait_path_48() {
		return portrait_path_original + "!48";
	}

	public Integer getIconTag() {
		return iconTag;
	}

	public void setIconTag(Integer iconTag) {
		this.iconTag = iconTag;
	}

	public Integer getMaxCount() {
		return maxCount;
	}

	public void setMaxCount(Integer maxCount) {
		this.maxCount = maxCount;
	}

	public Date getLivestarttime() {
		return livestarttime;
	}

	public void setLivestarttime(Date livestarttime) {
		this.livestarttime = livestarttime;
	}

	public Date getLiveendtime() {
		return liveendtime;
	}

	public void setLiveendtime(Date liveendtime) {
		this.liveendtime = liveendtime;
	}

	public Date getNextstarttime() {
		return nextstarttime;
	}

	public void setNextstarttime(Date nextstarttime) {
		this.nextstarttime = nextstarttime;
	}

	public Integer getFansCount() {
		return fansCount;
	}

	public void setFansCount(Integer fansCount) {
		this.fansCount = fansCount;
	}

	public String getEnterConditionType() {
		return enterConditionType;
	}

	public void setEnterConditionType(String enterConditionType) {
		this.enterConditionType = enterConditionType;
	}

	public Integer getCity() {
		return city;
	}

	public void setCity(Integer city) {
		this.city = city;
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public String getRoomTheme() {
		return roomTheme;
	}

	public void setRoomTheme(String roomTheme) {
		this.roomTheme = roomTheme;
	}
	
	public Integer getRoomId() {
		return roomId;
	}

	public void setRoomId(Integer roomId) {
		this.roomId = roomId;
	}

	public Integer getOnlineCount() {
		return onlineCount;
	}

	public void setOnlineCount(Integer onlineCount) {
		this.onlineCount = onlineCount;
	}

	public Integer getLiveType() {
		return liveType;
	}

	public void setLiveType(Integer liveType) {
		this.liveType = liveType;
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

	public Integer getIsRookie() {
		return isRookie;
	}

	public void setIsRookie(Integer isRookie) {
		this.isRookie = isRookie;
	}
	
	public Integer getIsWeekly() {
		return isWeekly;
	}

	public void setIsWeekly(Integer isWeekly) {
		this.isWeekly = isWeekly;
	}

	public Integer getVideoLevel() {
		return videoLevel;
	}

	public void setVideoLevel(Integer videoLevel) {
		this.videoLevel = videoLevel;
	}

	public Integer getRoomMode() {
		return roomMode;
	}

	public void setRoomMode(Integer roomMode) {
		this.roomMode = roomMode;
	}

    public Integer getActorTag() {
        return actorTag;
    }

    public void setActorTag(Integer actorTag) {
        this.actorTag = actorTag;
    }

    public Integer getRoomSource() {
        return roomSource;
    }

    public void setRoomSource(Integer roomSource) {
        this.roomSource = roomSource;
    }
	
}

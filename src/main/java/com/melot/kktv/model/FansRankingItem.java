package com.melot.kktv.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.kktv.redis.MedalSource;
import com.melot.kkcx.service.UserAssetServices;
import com.melot.kkcx.service.UserService;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.confdynamic.MedalConfig;
import com.melot.module.medal.driver.domain.ConfMedal;
import com.melot.module.medal.driver.domain.GsonMedalObj;
import com.melot.module.medal.driver.domain.UserActivityMedal;
import com.melot.module.medal.driver.service.ActivityMedalService;
import com.melot.module.medal.driver.service.UserMedalService;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * 粉丝排行榜类
 * 
 * @author Administrator
 * 
 */
public class FansRankingItem {
    
    /** 日志记录对象 */
    private static Logger logger = Logger.getLogger(FansRankingItem.class);
    
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
		
		// 用户可佩戴的活动勋章
        try {
            UserMedalService userMedalService = (UserMedalService) MelotBeanFactory.getBean("userMedalService");
            ActivityMedalService activityMedalService = (ActivityMedalService) MelotBeanFactory.getBean("activityMedalService");
            
            //添加充值勋章信息,充值勋章所需要的字段都放到redis中，避免二次查询数据库
            Date now = new Date();
            List<ConfMedal> medals = new ArrayList<>();
            GsonMedalObj medal = userMedalService.getMedalsByUserId(userId);
            if (medal != null ) {
                ConfMedal confMedal = null;
                    //充值勋章点亮状态lightState为1显示
                    if ((medal.getEndTime() == 0 || medal.getEndTime() > now.getTime()) && medal.getLightState() == 1) {
                        MedalInfo medalInfo = null;
                        medalInfo = MedalConfig.getMedal(medal.getMedalId());
                        if (medalInfo != null) {
                            confMedal = new ConfMedal();
                            confMedal.setBright(medal.getLightState());
                            
                            //提醒单独处理放到if判断中
                            if (medalInfo.getMedalLevel() == 8) {
                                confMedal.setMedalLevel(7);
                                confMedal.setIsTop(1);
                                confMedal.setMedalDes(medalInfo.getMedalDesc());
                            }else {
                                confMedal.setMedalLevel(medalInfo.getMedalLevel() - 1);
                                confMedal.setIsTop(0);
                                confMedal.setMedalDes(medalInfo.getMedalDesc());
                            }
                            confMedal.setMedalType(medalInfo.getMedalType());
                            confMedal.setMedalTitle(medalInfo.getMedalTitle());
                            confMedal.setMedalExpireTime(medal.getEndTime());
                            confMedal.setMedalMedalUrl(medalInfo.getMedalIcon());
                            medals.add(confMedal);
                        }
                    }
            }
            
            List<UserActivityMedal> wearList = null;
            wearList = activityMedalService.getUserWearMedals(userId);
            if (wearList != null && !wearList.isEmpty()) {
                for (UserActivityMedal userActivityMedal : wearList) {
                    if (userActivityMedal.getEndTime() == null || userActivityMedal.getEndTime().getTime() > System.currentTimeMillis()) {
                        ConfMedal confMedal = new ConfMedal();
                        confMedal.setIsTop(0);
                        confMedal.setMedalId(userActivityMedal.getMedalId());
                        confMedal.setBright(userActivityMedal.getLightState());
                        confMedal.setMedalDes(userActivityMedal.getMedalDesc() != null ? String.valueOf(new JsonParser().parse(userActivityMedal.getMedalDesc()).getAsJsonObject().get("description")) : null);
                        confMedal.setMedalType(userActivityMedal.getMedalType());
                        confMedal.setMedalTitle(userActivityMedal.getMedalTitle());
                        confMedal.setMedalExpireTime(userActivityMedal.getEndTime().getTime());
                        confMedal.setMedalMedalUrl(userActivityMedal.getMedalIcon());
                        medals.add(confMedal);
                    }
                }
            }
            
            jObject.add("userMedalList",new JsonParser().parse(new Gson().toJson(medals)).getAsJsonArray());
        } catch (Exception e) {
            logger.error("Get user[" + userId + "] medal execute exception.", e);
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

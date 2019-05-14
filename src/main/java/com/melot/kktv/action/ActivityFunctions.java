package com.melot.kktv.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.melot.kk.activity.driver.MissionService;
import com.melot.kktv.util.*;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.common.driver.service.ShareService;
import com.melot.feedback.driver.domain.Award;
import com.melot.feedback.driver.service.FeedbackService;
import com.melot.kk.activityAPI.api.dto.ShareInfoDTO;
import com.melot.kk.activityAPI.api.service.NewMissionService;
import com.melot.kk.module.report.util.CommonStateCode;
import com.melot.kk.recharge.api.service.RechargeService;
import com.melot.kkactivity.driver.domain.GameConfig;
import com.melot.kkactivity.driver.domain.GameGift;
import com.melot.kkactivity.driver.service.GameConfigService;
import com.melot.kktv.base.Result;
import com.melot.kktv.util.confdynamic.SystemConfig;
import com.melot.module.packagegift.driver.domain.RechargePackage;
import com.melot.module.packagegift.driver.service.PackageInfoService;
import com.melot.sdk.core.util.MelotBeanFactory;

public class ActivityFunctions {

	private static Logger logger = Logger.getLogger(ActivityFunctions.class);
	
	@Resource
	PackageInfoService packageInfoService;
	
	@Resource
    RechargeService rechargeService;

	@Resource
    MissionService missionService;

	@Resource
    NewMissionService newMissionService;

	/**
	 * 获取房间活动入口信息 (20010010)
	 * @param paramJsonObject
	 * @return
	 */
    public JsonObject getRoomActivityInfo(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        
        return result;
    }
    
    /**
     * 获取用户首充礼包信息(20010013)
     * @param jsonObject
     * @return
     */
    public JsonObject getUserFirstRechargePackageInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int appId, userId = 0;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            RechargePackage rechargePackage = packageInfoService.getUserFirstRechargePackageInfo(userId, appId);
            if (rechargePackage != null) {
                result.addProperty("packageId", rechargePackage.getPackageId());
                result.addProperty("status", rechargePackage.getStatus());
                result.addProperty("orderId", rechargePackage.getOrderId());
            } else {
                result.addProperty("packageId", 0);
                result.addProperty("status", 0);
            }
        } catch (Exception e) {
            logger.error("packageInfoService.getUserFirstRechargePackageInfo(userId: " + userId + ", appId: " + appId + ") execute exception: ",e);
        }
        
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 房间内活动信息展示2.0(50001002)
     * getRoomActivityDetail
     */
    public JsonObject getRoomActivityDetail(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        
        int roomId;
        int familyId;
        int platform;
        int userId;
        String version;
        
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, TagCodeEnum.ROOMID_MISSING, 1, Integer.MAX_VALUE);
            familyId = CommonUtil.getJsonParamInt(jsonObject, "familyId", 0, TagCodeEnum.FAMILYID_MISSING, 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, TagCodeEnum.PLATFORM_MISSING, 1, 10);
            version = CommonUtil.getJsonParamString(jsonObject, "version", null, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            JsonObject paramJson = new JsonObject();
            paramJson.addProperty("roomId", roomId);
            paramJson.addProperty("familyId", familyId);
            paramJson.addProperty("platform", platform);
            if (version != null) {
                paramJson.addProperty("version", version);
            }
            if (userId > 0) {
                paramJson.addProperty("userId", userId);
            }

            if (newMissionService != null) {
                result = new JsonParser().parse(newMissionService.getVisibleActivity(paramJson.toString())).getAsJsonObject();
            }
        } catch (Exception e) {
            logger.error("调用MissionService模块异常", e);
        }
        
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 获取用户已获得礼包列表(50010014)
     * @param jsonObject
     * @return
     */
    public JsonObject getUserReceivePackageList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int appId, userId = 0;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        JsonArray jsonArray = new JsonArray();
        int count = 0;
        try {
            List<RechargePackage> userRechargePackage = null;
            userRechargePackage = packageInfoService.getUserRechargePackageList(userId, appId);
            
            if (userRechargePackage != null) {
                for (RechargePackage userFrPackage : userRechargePackage) {
                    JsonObject jsonObject2 = new JsonObject();
                    jsonObject2.addProperty("packageId", userFrPackage.getPackageId());
                    jsonObject2.addProperty("status", userFrPackage.getStatus());
                    jsonObject2.addProperty("orderId", userFrPackage.getOrderId());
                    jsonObject2.addProperty("isRecive", userFrPackage.getIsRecive());
                    jsonArray.add(jsonObject2);
                }
            }
            
            Result<Map<String, Object>> resp =  rechargeService.getUserRechargingRecordCount(userId, null, null);
            if (resp != null && CommonStateCode.SUCCESS.equals(resp.getCode())) {
                Map<String, Object> map = resp.getData();
                if (map.get("count") != null) {
                    count = (int) map.get("count");
                }
            }
        } catch (Exception e) {
            logger.error("ActivityFunctions.getUserReceivePackageList() execute exception.", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
        
        result.add("packageList", jsonArray);
        result.addProperty("rechargeTimes", count);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 获取用户分享回馈明细列表(20010019)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getUserPromotionRewardList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
    	JsonObject result = new JsonObject();
    	if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
    	
    	int userId, start, end;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, "start", 1, null, 1, Integer.MAX_VALUE);
            end = CommonUtil.getJsonParamInt(jsonObject, "end", start, null, start, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        Integer count = null, invitedCount = null;
        JsonArray jsonArray = new JsonArray();
		FeedbackService feedbackService = (FeedbackService) MelotBeanFactory.getBean("feedbackService");
		try {
			count = feedbackService.getCountByUserId(userId);
			invitedCount = feedbackService.getInviteCountByUserId(userId);
		} catch (Exception e) {
			logger.error("call FeedbackService getCountByUserId catched exception, userId : " + userId, e);
		}
		if (invitedCount != null) {
			result.addProperty("invitedCount", invitedCount);
		} else {
			result.addProperty("invitedCount", 0);
		}
		if (count == null || count == 0) {
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			result.addProperty("count", 0);
			result.add("rewardList", jsonArray);
			return result;
		}
		int limit = end - start + 1;
		int offset = start - 1;
		try {
			List<Award> awardList = feedbackService.getAwardListByUserId(userId, limit, offset);
			if (awardList != null && awardList.size() > 0) {
				for (Award award : awardList) {
					JsonObject json = new JsonObject();
					json.addProperty("userId", award.getUserId());
					if (award.getNickName() != null) {
						json.addProperty("nickname", award.getNickName());
					}
					String portraitAddress;
					if (award.getPortrait() != null) {
						portraitAddress = award.getPortrait().startsWith("http://") ? award.getPortrait() : ConfigHelper.getHttpdir() + award.getPortrait();
						json.addProperty("portrait_path_original", portraitAddress);
						json.addProperty("portrait_path_48", portraitAddress + "!48");
						json.addProperty("portrait_path_128", portraitAddress + "!128");
						json.addProperty("portrait_path_256", portraitAddress + "!256");
						json.addProperty("portrait_path_272", portraitAddress + "!272");
						json.addProperty("portrait_path_1280", portraitAddress + "!1280");
						json.addProperty("portrait_path_400", portraitAddress + "!400");
						json.addProperty("portrait_path_756", portraitAddress + "!756x567");
					}
					json.addProperty("showMoney", award.getAward());
					jsonArray.add(json);
				}
			}
		} catch (Exception e) {
			count = 0;
			logger.error("call FeedbackService getAwardListByUserId catched exception, userId : " + userId + ", limit : " + limit + ", offset : " + offset, e);
		}
		
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		result.addProperty("count", count);
		result.add("rewardList", jsonArray);
		return result;
    }
    
    /**
     * 获取主播分享回馈明细列表(20010020)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getActorPromotionRewardList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
    	JsonObject result = new JsonObject();
    	if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
    	
    	int userId, start, end;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, "start", 1, null, 1, Integer.MAX_VALUE);
            end = CommonUtil.getJsonParamInt(jsonObject, "end", start, null, start, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        Integer count = null;
        JsonArray jsonArray = new JsonArray();
		FeedbackService feedbackService = (FeedbackService) MelotBeanFactory.getBean("feedbackService");
		try {
			count = feedbackService.getCountByActorId(userId);
		} catch (Exception e) {
			logger.error("call FeedbackService getCountByUserId catched exception, userId : " + userId, e);
		}
		if (count == null || count == 0) {
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			result.addProperty("count", 0);
			result.addProperty("invitedCount", 0);
			result.add("rewardList", jsonArray);
			return result;
		}
		int limit = end - start + 1;
		int offset = start - 1;
		try {
			List<Award> awardList = feedbackService.getAwardListByActorId(userId, limit, offset);
			if (awardList != null && awardList.size() > 0) {
				for (Award award : awardList) {
					JsonObject json = new JsonObject();
					json.addProperty("userId", award.getUserId());
					if (award.getNickName() != null) {
						json.addProperty("nickname", award.getNickName());
					}
					String portraitAddress;
					if (award.getPortrait() != null) {
						portraitAddress = award.getPortrait().startsWith("http://") ? award.getPortrait() : ConfigHelper.getHttpdir() + award.getPortrait();
						json.addProperty("portrait_path_original", portraitAddress);
						json.addProperty("portrait_path_48", portraitAddress + "!48");
						json.addProperty("portrait_path_128", portraitAddress + "!128");
						json.addProperty("portrait_path_256", portraitAddress + "!256");
						json.addProperty("portrait_path_272", portraitAddress + "!272");
						json.addProperty("portrait_path_1280", portraitAddress + "!1280");
						json.addProperty("portrait_path_400", portraitAddress + "!400");
						json.addProperty("portrait_path_756", portraitAddress + "!756x567");
					}
					json.addProperty("showMoney", award.getAward());
					jsonArray.add(json);
				}
			}
		} catch (Exception e) {
			count = 0;
			logger.error("call FeedbackService getAwardListByUserId catched exception, userId : " + userId + ", limit : " + limit + ", offset : " + offset, e);
		}
		
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		result.addProperty("count", count);
		result.add("rewardList", jsonArray);
		return result;
    }
    
    /**
     * 记录房间分享信息(20010021)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject recordRoomShare(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
    	JsonObject result = new JsonObject();
    	
    	int userId, sharePlatform, shareSourceId, shareType,v,platform;
        String shareReason,sharelink;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
            sharePlatform = CommonUtil.getJsonParamInt(jsonObject, "sharePlatform", 1, TagCodeEnum.ROOMID_MISSING, 1, Integer.MAX_VALUE);
            shareSourceId = CommonUtil.getJsonParamInt(jsonObject, "shareSourceId", 0, null, 1, Integer.MAX_VALUE);
            shareType = CommonUtil.getJsonParamInt(jsonObject, "shareType", 0, TagCodeEnum.ROOMID_MISSING, 1, Integer.MAX_VALUE);
        
            shareReason = CommonUtil.getJsonParamString(jsonObject, "shareReason", null, null, 1, Integer.MAX_VALUE);
            sharelink = CommonUtil.getJsonParamString(jsonObject, "sharelink", null, null, 1, Integer.MAX_VALUE);
            v = CommonUtil.getJsonParamInt(jsonObject, "v", 0, null, 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        NewMissionService newMissionService = (NewMissionService) MelotBeanFactory.getBean("newMissionService");
        if (newMissionService != null) {
        	ShareInfoDTO shareInfo = new ShareInfoDTO();
        	if (userId != 0) {
        		shareInfo.setUserId(userId);
        	}
        	shareInfo.setSharedPlatform(sharePlatform);
        	shareInfo.setPlatform(platform);
        	shareInfo.setSharedType(shareType);
        	shareInfo.setSharedSourceId(shareSourceId);
        	
        	// 分享视频动态、普通动态 title和link不能为空
            if (shareType == 6 || shareType == 5 ) {
                if ((platform == PlatformEnum.ANDROID && v > 99) || (platform == PlatformEnum.IPHONE && v > 131)) {
                    if (StringUtils.isBlank(shareReason)) {
                        shareReason = "0";
                    }
                    if (StringUtils.isBlank(sharelink)) {
                        result.addProperty("TagCode", TagCodeEnum.SHARE_LINK_IS_NULL);
                        return result;
                    }
                }
            }
            shareInfo.setShareReason(shareReason);
            shareInfo.setShareLink(sharelink);
        	
        	boolean shareResult = newMissionService.share(shareInfo);
        	result.addProperty("TagCode", shareResult? TagCodeEnum.SUCCESS : TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        } else {
        	result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
        }
        
        return result;
    }
    
    /**
     * 获取房间分享金库(20010022)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getActorShareCoffers(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
    	JsonObject result = new JsonObject();
    	
    	int roomId;
        try {
        	roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, TagCodeEnum.ROOMID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        String isopen = SystemConfig.getValue(SystemConfig.actorShareCoffer, AppIdEnum.AMUSEMENT);
        if ("-1".equals(isopen)) {
        	result.addProperty("amount", -1);
        	result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        	return result;
        }
        
        long amount = 0;
        try {
			ShareService shareService = MelotBeanFactory.getBean("shareService", ShareService.class);
			amount = shareService.getShareCoffersByRoomId(roomId);
		} catch (Exception e) {
			logger.error("call ShareService getShareCoffersByRoomId catched exception, roomId : " + roomId, e);
			result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
			return result;
		}
        amount = amount < 0 ? 0 : amount;
        amount = amount > 100000 ? 100000 : amount;
        result.addProperty("amount", amount);
        result.addProperty("limit", Integer.valueOf(SystemConfig.getValue(SystemConfig.shareCofferLimit, AppIdEnum.AMUSEMENT)));
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
    }
    
    /**
     * 获取房间粉丝回馈配置信息(50001031)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getFanFeedBackConf(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int roomId;
        try {
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, null, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            result.addProperty("fanFeedbackMax", Integer.valueOf(SystemConfig.getValue(SystemConfig.fanFeedbackMax, AppIdEnum.AMUSEMENT)));
            result.addProperty("fanFeedbackMin", Integer.valueOf(SystemConfig.getValue(SystemConfig.fanFeedbackMin, AppIdEnum.AMUSEMENT)));
            result.addProperty("fanFeedbackDayLimit", Integer.valueOf(SystemConfig.getValue(SystemConfig.fanFeedbackDayLimit, AppIdEnum.AMUSEMENT)));
            
            if (roomId > 0) {
                ShareService shareService = MelotBeanFactory.getBean("shareService", ShareService.class);
                long amount = 0;
                if ("1".equals(shareService.getFanFeedBackInfo(roomId).get("isOpen"))) {
                    int fanFeedbackEndAmount = Integer.valueOf(SystemConfig.getValue(SystemConfig.fanFeedbackEndAmount, AppIdEnum.AMUSEMENT));
                    amount = shareService.getShareCoffersByRoomId(roomId) - fanFeedbackEndAmount;
                }
                result.addProperty("fanFeedbackAmount", amount > 0 ? amount : 0);
            }
        } catch (Exception e) {
            logger.error("call ShareService getShareCoffersByRoomId catched exception, roomId : " + roomId, e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
        
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 获取平台可用游戏列表【51050101】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getGameList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int platform;
        int v;
        try {
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, null, 1, Integer.MAX_VALUE);
            v = CommonUtil.getJsonParamInt(jsonObject, "v", 0, null, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            GameConfigService gameConfigService = MelotBeanFactory.getBean("gameConfigService", GameConfigService.class);
            List<GameConfig> gameConfigs = gameConfigService.getGameList(platform, v);
            JsonArray games = new JsonArray();
            for (GameConfig gameConfig : gameConfigs) {
                JsonObject gameJson = new JsonObject();
                gameJson.addProperty("gameId", gameConfig.getGameId());
                gameJson.addProperty("gameName", gameConfig.getGameName());
                gameJson.addProperty("type", gameConfig.getType());
                gameJson.addProperty("gameIcon", gameConfig.getGameIcon());
                gameJson.addProperty("gameUrl", gameConfig.getGameUrl());
                gameJson.addProperty("helpIcon", gameConfig.getHelpIcon());
                gameJson.addProperty("helpUrl", gameConfig.getHelpUrl());
                gameJson.addProperty("helpTitle", gameConfig.getHelpTitle());
                games.add(gameJson);
            }
            result.add("games", games);
        } catch (Exception e) {
            logger.error(String.format("Module Error gameConfigService.getGameList(%s, %s)", platform, v), e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
        
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 获取游戏对应礼物列表【51050102】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getGameGiftList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int gameId;
        try {
            gameId = CommonUtil.getJsonParamInt(jsonObject, "gameId", 0, "5105010201", 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            GameConfigService gameConfigService = MelotBeanFactory.getBean("gameConfigService", GameConfigService.class);
            List<GameGift> gameConfigs = gameConfigService.getGameGiftList(gameId);
            List<Integer> giftIds = new ArrayList<>();
            for (GameGift gameGift : gameConfigs) {
                giftIds.add(gameGift.getGiftId());
            }
            result.add("giftIds", new JsonParser().parse(new Gson().toJson(giftIds)));
        } catch (Exception e) {
            logger.error(String.format("Module Error gameConfigService.getGameGiftList(%s)", gameId), e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
        
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 老版活动网关接口(88009000), 对接kk-activity-api-server
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject doActivityService(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        if (checkTag) {
            jsonObject.addProperty("isLogin", true);
        } else {
            jsonObject.addProperty("isLogin", false);
        }

        if (missionService == null) {
            JsonObject result = new JsonObject();
            result.addProperty(ParameterKeys.TAG_CODE, "40400000");
            result.addProperty("errorMsg", "missionService is missing");
            return result;
        }
        return missionService.doActivityService(jsonObject);
    }

    /**
     * 新版活动网关接口(88009001), 对接kk-activityAPI-server
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject doActivityServiceV2(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        // 将是否登录封装到jsonObject
        jsonObject.addProperty("isLogin", checkTag);

        if (newMissionService == null) {
            result.addProperty(ParameterKeys.TAG_CODE, "40400000");
            result.addProperty("errorMsg", "newMissionService is missing");
            return result;
        }

        try {
            String resultStr = newMissionService.doActivityService(jsonObject.toString());
            result = new JsonParser().parse(resultStr).getAsJsonObject();
        } catch (Exception e) {
            logger.error("ActivityFunctions.doActivityServiceV2, parameter:" + jsonObject.toString(), e);
        }

        return result;
    }
}

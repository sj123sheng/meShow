package com.melot.kktv.action;


import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcx.transform.GuardTF;
import com.melot.kkcx.transform.RoomTF;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.Constant;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.module.api.exceptions.MelotModuleException;
import com.melot.module.guard.driver.domain.ConfGuard;
import com.melot.module.guard.driver.domain.ConfGuardPrice;
import com.melot.module.guard.driver.domain.GoldRanking;
import com.melot.module.guard.driver.domain.GrantInfo;
import com.melot.module.guard.driver.domain.GsonGuardObj;
import com.melot.module.guard.driver.domain.MsgResult;
import com.melot.module.guard.driver.service.GuardService;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * Title: 房间守护的相关接口的功能类
 * <p>
 * Description:
 * </p>
 * 
 * @author 冯高攀<a href="mailto:gaopan.feng@melot.cn">
 * @version V1.0
 * @since 2015年12月26日 上午11:40:46
 */
public class GuardFunctions {

    private static Logger logger = Logger.getLogger(GuardFunctions.class);

    /**
     * 获取守护购买列表（20031001）
     * 
     * @param paramJsonObject
     *            传入参数
     * @return 返回列表对象
     */
    public JsonObject getGuardInfos(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
        @SuppressWarnings("unused")
        int platform = 0;
        JsonObject result = new JsonObject();
        try {
            platform = CommonUtil.getJsonParamInt(paramJsonObject, "platform", PlatformEnum.WEB, TagCodeEnum.PLATFORM_MISSING, Integer.MIN_VALUE, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        GuardService guardService = (GuardService) MelotBeanFactory.getBean("guardService");
        List<ConfGuard> confGuards = guardService.getGuardInfoList();
        JsonParser parse = new JsonParser();
        JsonArray array = new JsonArray();
        JsonObject json;
        List<ConfGuardPrice> guardPriceList;
        for (ConfGuard confGuard : confGuards) {
            guardPriceList = guardService.getGuardPriceListByGuardId(confGuard.getGuardId());
            if(CollectionUtils.isEmpty(guardPriceList)) {
            	continue;
            }
            json = new JsonObject();
            json.addProperty("guardId", confGuard.getGuardId());
            json.addProperty("guardName", confGuard.getGuardName());
            json.addProperty("guardLevel", confGuard.getGuardLevel());
            json.add("guardIcon", parse.parse(confGuard.getGuardIcon()).getAsJsonObject());
            json.addProperty("guardCarId", confGuard.getGuardId());
            json.add("guardCarUrl", parse.parse(confGuard.getGuardCarUrl()).getAsJsonObject());
            json.add("priceList", parse.parse(new Gson().toJson(guardPriceList)).getAsJsonArray());
            array.add(json);
        }
        result.add("guardInfos", array);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 购买守护（20031002）
     * 
     * @param paramJsonObject
     *            传入参数
     * @return 返回列表对象
     */
    public JsonObject purchaseGuard(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        int userId, actorId, period, guardId;
        try {
            //解析参数
            userId = CommonUtil.getJsonParamInt(paramJsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            actorId = CommonUtil.getJsonParamInt(paramJsonObject, "actorId", 0, TagCodeEnum.ACTORID_MISSING, 1,Integer.MAX_VALUE);
            period = CommonUtil.getJsonParamInt(paramJsonObject, "period", 0, "08210005", 1,Integer.MAX_VALUE);
            guardId = CommonUtil.getJsonParamInt(paramJsonObject, "guardId", 0, "08210008", 1,Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            GuardService guardService = (GuardService) MelotBeanFactory.getBean("guardService");
            MsgResult resultMsg = guardService.grantGuard(period, guardId, userId, actorId);
            if(resultMsg != null) {
                if(resultMsg.getTagCode().equals("00")) {
                    result.addProperty("TagCode",TagCodeEnum.SUCCESS);
                    result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
                    result.addProperty("showMoney", resultMsg.getShowMoney() != null ? resultMsg.getShowMoney() : 0);
                    result.addProperty("isRenew", resultMsg.getIsRenew());
                } else if(resultMsg.getTagCode().equals("01")) {
                    result.addProperty("TagCode" , TagCodeEnum.USER_MONEY_SHORTNESS);
                } else if(resultMsg.getTagCode().equals("02")) {
                    result.addProperty("TagCode" , "08210009");
                } else if(resultMsg.getTagCode().equals("03")) {
                    result.addProperty("TagCode","08210013");
                } else if(resultMsg.getTagCode().equals("04")) {
                    result.addProperty("TagCode" , "08210014");
                }  else if(resultMsg.getTagCode().equals("05")) {
                    result.addProperty("TagCode", "08210015");
                } else if(resultMsg.getTagCode().equals("06")) {
                    result.addProperty("TagCode", "08210016");
                } else if(resultMsg.getTagCode().equals("07")) {
                    result.addProperty("TagCode", "08210017");
                }
            }
        } catch (MelotModuleException e) {
            switch (e.getErrCode()) {
            case 102:
                logger.error(e.getMessage());
                result.addProperty("TagCode" , TagCodeEnum.USER_MONEY_SHORTNESS);
                break;

            default:
                logger.error(e.getMessage());
                result.addProperty("TagCode" , "08210013");
                break;
            }
        } catch (Exception e) {
            logger.error("Unknow exception.", e);
            result.addProperty("TagCode" , "08210013");
        }
        
        return result;
    }

    /**
     * 获取主播拥有的守护列表（20031003）
     * @param paramJsonObject
     * @return
     */
    public JsonObject getActorGuardInfos(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        int actorId = 0;
        try {
            actorId = CommonUtil.getJsonParamInt(paramJsonObject, "actorId", 0, TagCodeEnum.ACTORID_MISSING, 1,Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        GuardService guardService = (GuardService) MelotBeanFactory.getBean("guardService");
        List<GsonGuardObj> gsonObjs = guardService.getActorGuards(actorId);
        JsonParser parse = new JsonParser();
        JsonArray array = new JsonArray();
        JsonObject json = null;
        JsonObject totalJson = null;
        Integer guardLimit = guardService.getMaxCount();
        if (!CollectionUtils.isEmpty(gsonObjs)) {
            for(GsonGuardObj gsonGuardObj : gsonObjs) {
                json = new JsonObject();
                totalJson = new JsonObject();
                json.addProperty("guardId", gsonGuardObj.getGuardId());
                json.addProperty("guardName", gsonGuardObj.getGuardName());
                json.addProperty("guardLevel", gsonGuardObj.getGuardLevel());
                json.add("guardIcon", parse.parse(gsonGuardObj.getGuardIcon()).getAsJsonObject());
                json.addProperty("guardCarId", gsonGuardObj.getGuardCarId());
                json.addProperty("guardLeftTime", gsonGuardObj.getGuardLeftTime());
                if(gsonGuardObj.getGuardCarUrl()!=null) {
                	json.add("guardCarUrl", parse.parse(gsonGuardObj.getGuardCarUrl()).getAsJsonObject());
                }
                json.addProperty("guardExpireTime", gsonGuardObj.getGuardExpireTime());
                json.addProperty("nextGuardId", gsonGuardObj.getNextGuardId());
                json.addProperty("nextGuardName", gsonGuardObj.getNextGuardName());
                json.addProperty("beGuardTime", gsonGuardObj.getBeGuardTime());
                if(gsonGuardObj.getGoldGuardIcon() != null) {
                	json.add("goldGuardIcon", parse.parse(gsonGuardObj.getGoldGuardIcon()).getAsJsonObject());
                }
                if(gsonGuardObj.getGoldGuardLevel() != null) {
                	json.addProperty("goldGuardLevel", gsonGuardObj.getGoldGuardLevel());
                }
                if(gsonGuardObj.getGoldGuardName() != null) {
                	json.addProperty("goldGuardName", gsonGuardObj.getGoldGuardName());
                }
                if(gsonGuardObj.getGuardYearIcon() != null) {
                	json.add("guardYearIcon", parse.parse(gsonGuardObj.getGuardYearIcon()).getAsJsonObject());
                }
                //添加用户信息
                totalJson.add("guardInfo",json);
                
                UserProfile user = com.melot.kktv.service.UserService.getUserInfoV2(gsonGuardObj.getUserId());
                if (user != null) {
                    totalJson.addProperty("nickName", user.getNickName());
                } else {
                    totalJson.addProperty("nickName", gsonGuardObj.getNickName());
                }
                totalJson.addProperty("userId", gsonGuardObj.getUserId());
                if(gsonGuardObj.getPortrait() != null && !json.has("portrait_path_original")) {
                    totalJson.addProperty("portrait_path_original", ConfigHelper.getHttpdir() + gsonGuardObj.getPortrait());
                    totalJson.addProperty("portrait_path_1280", ConfigHelper.getHttpdir() + gsonGuardObj.getPortrait() + "!1280");
                    totalJson.addProperty("portrait_path_256", ConfigHelper.getHttpdir() + gsonGuardObj.getPortrait() + "!256");
                    totalJson.addProperty("portrait_path_128", ConfigHelper.getHttpdir() + gsonGuardObj.getPortrait() + "!128");
                    totalJson.addProperty("portrait_path_48", ConfigHelper.getHttpdir() + gsonGuardObj.getPortrait() + "!48");
                }
                totalJson.addProperty("gender", gsonGuardObj.getGender());
                array.add(totalJson);
            }
        }
        result.add("userGuardList",array);
        result.addProperty("maxCount", guardLimit);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 获取主播拥有的守护数量（51010501）
     * @param paramJsonObject
     * @return
     */
    public JsonObject getActorGuardCount(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        int actorId = 0;
        try {
            actorId = CommonUtil.getJsonParamInt(paramJsonObject, "actorId", 0, TagCodeEnum.ACTORID_MISSING, 1,Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            GuardService guardService = (GuardService) MelotBeanFactory.getBean("guardService");
            List<GsonGuardObj> actorGuardList = guardService.getActorGuards(actorId);
            int count = 0;
            if (!CollectionUtils.isEmpty(actorGuardList)) {
                count = actorGuardList.size();
            }
            result.addProperty("count", count);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        return result;
    }
    
    /**
     * 获取黄金赛周榜（20031004）
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getActorWeekRank(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
    	JsonObject result = new JsonObject();
    	int actorId = 0, start = 0, offset = 0, type = 0;
    	try {
			actorId = CommonUtil.getJsonParamInt(jsonObject, "actorId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
			start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, null, 0, Integer.MAX_VALUE);
			offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 15, null, 0, Integer.MAX_VALUE);
			type = CommonUtil.getJsonParamInt(jsonObject, "type", 1, null, 1, 2);
    	} catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
    	
    	List<GoldRanking> rankingList = null;
    	try {
    	    GuardService guardService = (GuardService) MelotBeanFactory.getBean("guardService");
    	    if (guardService != null) {
    	        rankingList = guardService.getGuardGoldRankingList(actorId, start, offset, type);
    	    }
		} catch (Exception e) {
			logger.error("GuardService.getGuardGoldRankingList( " + actorId + ", " + start + ", " + offset + ", " + type + ") execute exception.", e);
		}
    	
    	JsonArray jsonArray = new JsonArray();
    	if (rankingList != null && rankingList.size() > 0) {
    		for (GoldRanking rank : rankingList) {
    			if (rank != null) {
    				jsonArray.add(GuardTF.guardGoldRankingTF(rank));
    			}
    		}
    	}
    	
    	result.add("rankList", jsonArray);
    	result.addProperty("TagCode", TagCodeEnum.SUCCESS);
    	
    	return result;
    }
    
    /**
     * 设置守护进场车辆(20031009)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject setGuardCar(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
    	JsonObject result = new JsonObject();
    	
    	// 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
    	int userId, actorId, type;
    	try {
    		userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
    		actorId = CommonUtil.getJsonParamInt(jsonObject, "actorId", 0, TagCodeEnum.ACTORID_MISSING, 1, Integer.MAX_VALUE);
    		type = CommonUtil.getJsonParamInt(jsonObject, "type", 0, TagCodeEnum.USERID_MISSING, 0, Integer.MAX_VALUE);
    	} catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
    	
    	int state = 1;
    	try {
    	    GuardService guardService = (GuardService) MelotBeanFactory.getBean("guardService");
    	    if (guardService != null) {
    	    	state = guardService.setUserGuardCar(userId, actorId, type);
    	    }
		} catch (Exception e) {
			logger.error("GuardService.setUserGuardCar( " + userId + ", " + actorId + ", " + type + ") execute exception.", e);
		}
    	if (state == 0) {
    		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
    	} else if (state == -1) {
    		//非超级VIP不可以设置守护进场车辆
    		result.addProperty("TagCode", "08210018");
    	} else if (state == 1) {
    		//模块未响应
    		result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
    	}
    	
    	return result;
    }
    
    /**
     * 获取用户守护列表(20031010)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    @SuppressWarnings("unchecked")
	public JsonObject getUserGuardList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
    	JsonObject result = new JsonObject();
    	
    	if (!checkTag) {
    	    result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
    	}
    	
    	int userId, platform, start, offset;
    	try {
    		userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
    		start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, null, 1, Integer.MAX_VALUE);
    		offset = CommonUtil.getJsonParamInt(jsonObject, "offset", Constant.return_room_count, null, 1, Integer.MAX_VALUE);
    		platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 1, null, 1, Integer.MAX_VALUE);
    	} catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
    	
    	int state;
    	List<Integer> actorIdlist = null;
    	List<RoomInfo> roomList = null;
    	GrantInfo grantInfo = new GrantInfo();
    	JsonArray jsonArray = new JsonArray();
    	GuardService guardService = (GuardService) MelotBeanFactory.getBean("guardService");
    	try {
    	    if (guardService != null) {
    	    	grantInfo = guardService.getUserGuardedActorList(userId, start, offset);
    	    	if (grantInfo != null && grantInfo.getNumber() != null) {
    	    		result.addProperty("count", grantInfo.getNumber());
    	    		actorIdlist = (List<Integer>) grantInfo.getGrantSharingInfo();
    	    	}
    	    }
		} catch (Exception e) {
			logger.error("GuardService.getUserGuardedRoomList( " + userId + ") execute exception.", e);
		}
    	StringBuffer sb = new StringBuffer();
    	if (actorIdlist != null && actorIdlist.size() > 0) {
    		for (Integer actorId : actorIdlist) {
    			sb.append(actorId);
    			sb.append(",");
    		}
    	}
    	if (sb.length() > 0) {
    		roomList = com.melot.kktv.service.RoomService.getRoomListByRoomIds(sb.substring(0, sb.length() - 1));
    	}
    	if (roomList != null && roomList.size() > 0) {
    	    if (roomList.size() > 1) {
    	        Collections.sort(roomList, new Comparator<RoomInfo>() {
    	            public int compare(RoomInfo r1, RoomInfo r2) {
                        int dis = r2.getLiveType().compareTo(r1.getLiveType());
                        if (dis == 0) {
                            return r2.getPeopleInRoom().compareTo(r1.getPeopleInRoom());
                        } else {
                            return dis;
                        }
                    }
    	        });
            }
    	    
    		for (RoomInfo info : roomList) {
    			JsonObject obj = RoomTF.roomInfoToJson(info, platform, true);
    			if (obj != null) {
    				state = guardService.getUserGuardCarState(userId, info.getActorId());
    				obj.addProperty("isOpenGuardCar", state == 0 ? false : true);
    			}
    			jsonArray.add(obj);
    		}
    	}
    	
    	result.add("roomList", jsonArray);
    	result.addProperty("TagCode", TagCodeEnum.SUCCESS);
    	return result;
    }
    
}

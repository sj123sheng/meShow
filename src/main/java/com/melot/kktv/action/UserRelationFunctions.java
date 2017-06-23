package com.melot.kktv.action;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.api.menu.sdk.handler.FirstPageHandler;
import com.melot.api.menu.sdk.service.RoomInfoService;
import com.melot.kkcore.relation.api.ActorRelation;
import com.melot.kkcore.relation.api.RelationType;
import com.melot.kkcore.relation.service.ActorRelationService;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcx.service.GeneralService;
import com.melot.kkcx.service.RoomService;
import com.melot.kkcx.service.UserAssetServices;
import com.melot.kkcx.service.UserService;
import com.melot.kkcx.transform.RoomTF;
import com.melot.kktv.model.FansRankingItem;
import com.melot.kktv.model.Room;
import com.melot.kktv.redis.HotDataSource;
import com.melot.kktv.redis.NewsV2Source;
import com.melot.kktv.service.DataAcqService;
import com.melot.kktv.service.UserRelationService;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.Constant;
import com.melot.kktv.util.HadoopLogger;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.RankingEnum;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;
import com.melot.module.packagegift.driver.domain.ResUserXman;
import com.melot.module.packagegift.driver.domain.ResXman;
import com.melot.module.packagegift.driver.service.XmanService;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * 用户关系的接口类
 * 
 * @author LY
 * 
 */
public class UserRelationFunctions {

	/** 日志记录对象 */
	private static Logger logger = Logger.getLogger(UserRelationFunctions.class);
	
    /**
	 * 关注(10003001)
	 * 
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return 结果字符串
	 */
	public JsonObject follow(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
	    JsonObject result = new JsonObject();
	    
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}

		int userId, appId, platform, roomId;
		String followedIds, sourcesTag;
		
		// 获取参数
		try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            followedIds = CommonUtil.getJsonParamString(jsonObject, "followedIds", null, "03010003", 1, 100);
            sourcesTag = CommonUtil.getJsonParamString(jsonObject, "sourcesTag", null, null, 1, 100);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, 1, Integer.MAX_VALUE);
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, null, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

		UserProfile userInfo = com.melot.kktv.service.UserService.getUserInfoV2(userId);
        if (userInfo == null) {
        	result.addProperty("TagCode", TagCodeEnum.USER_NOT_EXIST);
            return result;
        }
        
        int mysType = 0;
        try {
            ResUserXman resUserXman = null;
            XmanService xmanService = (XmanService) MelotBeanFactory.getBean("xmanService");
            resUserXman = xmanService.getResUserXmanByUserId(userId);
            if (resUserXman != null && (resUserXman.getExpireTime().getTime() >= new Date().getTime())) {
                ResXman resXman = null;
                resXman = xmanService.getResXmanByUserId(userId);
                if (resXman != null && resXman.getMysType() == 2) {
                    mysType = 1;
                }
            }
        } catch (Exception e) {
            logger.error("xmanService.getResUserXmanByUserId(" + userId + ") execute exception.", e);
        }
        
        // 获取用户会员信息
        List<Integer> propList = new ArrayList<>();
        try {
            propList = UserService.getUserProps(userId);
        } catch (Exception e) {
            logger.error("UserService.getUserProps(" + userId + ") execute exception.", e);
        }
		
		JsonArray jsonArray = new JsonArray();
		JsonObject tempJsonObject;
		String[] fidArray = followedIds.split(",");
		for (String fid : fidArray) {
			int followedId = 0;
			try {
				followedId = Integer.parseInt(fid.trim());
			} catch (Exception e) {
//				logger.error("关注对象ID不非法.", e);
				continue;
			}
			
			if (followedId < 1) {
				continue;
			}
			
			followedId = UserAssetServices.idToUserId(followedId);
			UserProfile roomInfo = com.melot.kktv.service.UserService.getUserInfoV2(followedId);
	        if (roomInfo == null) {
	            continue;
	        }
			
			if (userId == followedId) {
//				logger.error("用户不能关注自己.");
				continue;
			}
			
			if (!UserRelationService.follow(userId, followedId)) {
//				logger.error("用户"+userId+"已经关注用户"+followedId+".");
                
                // 添加粉丝数返回
                tempJsonObject = new JsonObject();
                tempJsonObject.addProperty("followId", followedId);
                tempJsonObject.addProperty("followFansCount", UserRelationService.getFansCount(followedId));
                jsonArray.add(tempJsonObject);
                
				continue;
			}
			
			int followFansCount = UserRelationService.getFansCount(followedId);
			           
			// 添加粉丝数返回
            tempJsonObject = new JsonObject();
            tempJsonObject.addProperty("followId", followedId);
            tempJsonObject.addProperty("followFansCount", followFansCount);
            jsonArray.add(tempJsonObject);
            
            // 恶意关注 60秒内重复关注不发送房间消息
            boolean isEvil = false;
            String key = "evil_follow_" + userId + "_" + followedId;
            if (HotDataSource.getTempDataString(key) != null) {
            	isEvil = true;
            } else {
            	HotDataSource.setTempDataString(key, System.currentTimeMillis() + "", 60);
            }
            
            // 推送房间关注数变化给房间
            if (!isEvil) {
                int msgToroom = followedId;
                boolean flag = false;
                try {
                    ActorRelationService actorRelationService = (ActorRelationService) MelotBeanFactory.getBean("kkActorRelationService");
                    ActorRelation actorRelation = actorRelationService.getRelationByUserAndActor(followedId, userId, RelationType.ADMIN.typeId());
                    if (actorRelation != null) {
                        flag = true;
                    }
                } catch (Exception e) {
                    logger.error("actorRelationService.getRelationByUserAndActor(actorId:" + followedId + ",userId:" + userId + ",type:" + RelationType.ADMIN.typeId() + ") execute exception.", e);
                }
                
                if (roomId != 0) {
                    //关注主播如果是轮播房代理房主，则关注消息发轮播房间，否则发被关注主播房间
                    RoomInfoService roomInfoServie = MelotBeanFactory.getBean("roomInfoService", RoomInfoService.class);
                    RoomInfo followRoomInfo = roomInfoServie.getRoomInfoById(followedId);
                    if (followRoomInfo != null && followRoomInfo.getRoomSource() == 8 && followRoomInfo.getRoomId() == roomId) {
                        msgToroom = roomId;
                    }
                }
                
            	JsonObject msg = new JsonObject();
            	msg.addProperty("MsgTag", 10010321);
            	msg.addProperty("isRoomAdmin", flag ? 1 : 0);
            	msg.addProperty("userId", userInfo.getUserId());
            	msg.addProperty("userNick", userInfo.getNickName());
            	msg.addProperty("action", 1);
            	msg.addProperty("count", followFansCount);
            	msg.addProperty("mysType", mysType);
            	msg.addProperty("followedId", followedId);
            	msg.addProperty("followedNick", roomInfo.getNickName());
            	msg.addProperty("roomId", roomId);
            	msg.add("sPropList", new Gson().toJsonTree(propList).getAsJsonArray());
            	GeneralService.sendMsgToRoom(4, msgToroom, 0, 0, msg);
            }
            
			HadoopLogger.fansLog(userId, followedId, new Date(), appId, platform, sourcesTag);
			
			// 用户数据采集-关注
			DataAcqService.sendDoFollow(userId, followedId);
		}
		//关注时删除列表结果缓存
        com.melot.kkcx.service.UserRelationService.removeFollowedResultList(userId);
		
        result.addProperty("roomId", roomId);
		result.add("followFansList", jsonArray);
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
	}

	/**
	 * 取消关注(10003002)
	 * 
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return 结果字符串
	 */
	public JsonObject cancelFollow(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
	    JsonObject result = new JsonObject();
		
	    // 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		int userId, canceledId;
        
        // 获取参数
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            canceledId = CommonUtil.getJsonParamInt(jsonObject, "canceledId", 0, "03020004", 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        UserProfile userInfo = com.melot.kktv.service.UserService.getUserInfoV2(userId);
        if (userInfo == null) {
        	result.addProperty("TagCode", TagCodeEnum.USER_NOT_EXIST);
            return result;
        }
        
        if (!UserRelationService.cancelFollow(userId, canceledId)) {
			/* '02';未关注,不能取消 */
			result.addProperty("TagCode", "03020102");
			return result;
		}
        
		int followFansCount = UserRelationService.getFansCount(canceledId);
		int userFollowCount = UserRelationService.getFollowsCount(userId);
		
		// 恶意取消关注 60秒内重复关注不发送房间消息
        boolean isEvil = false;
        String key = "evil_cancel_follow_" + userId + "_" + canceledId;
        if (HotDataSource.getTempDataString(key) != null) {
        	isEvil = true;
        } else {
        	HotDataSource.setTempDataString(key, System.currentTimeMillis() + "", 60);
        }
        // 推送房间关注数变化给房间
        if (!isEvil) {
            boolean flag = false;
            try {
                ActorRelationService actorRelationService = (ActorRelationService) MelotBeanFactory.getBean("kkActorRelationService");
                ActorRelation actorRelation = actorRelationService.getRelationByUserAndActor(canceledId, userId, RelationType.ADMIN.typeId());
                if (actorRelation != null) {
                    flag = true;
                }
            } catch (Exception e) {
                logger.error("actorRelationService.getRelationByUserAndActor(actorId:" + canceledId + ",userId:" + userId + ",type:" + RelationType.ADMIN.typeId() + ") execute exception.", e);
            }
            
        	JsonObject msg = new JsonObject();
        	msg.addProperty("MsgTag", 10010321);
        	msg.addProperty("isRoomAdmin", flag ? 1 : 0);
        	msg.addProperty("userId", userInfo.getUserId());
        	msg.addProperty("userNick", userInfo.getNickName());
        	msg.addProperty("action", 0);
        	msg.addProperty("count", followFansCount);
        	GeneralService.sendMsgToRoom(4, canceledId, 0, 0, msg);
        }
        
        //取关时删除列表结果缓存
        com.melot.kkcx.service.UserRelationService.removeFollowedResultList(userId);
        //取关时删除动态feed
        NewsV2Source.delNews(userId, false);
        
		result.addProperty("userFollowCount", userFollowCount);
		result.addProperty("followFansCount", followFansCount);
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
	}

	/**
	 * 获取用户关注列表(10003003)
	 * 
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return 结果字符串
	 */
    public JsonObject getUserFollowedList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
	    JsonObject result = new JsonObject();
	    
	    //仅自己可查看关注列表
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        }

        @SuppressWarnings("unused")
        int userId, pageIndex, countPerPage, platform, appId;
        
        //排序规则  默认:直播状态,1:关注时间
        Integer sortType = null;
        
        // 获取参数
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 20, null, 1, 30);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, null, 1, Integer.MAX_VALUE);
            sortType = CommonUtil.getJsonParamInt(jsonObject, "sortType", 0, null, 0, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
	    
        int pageTotal = 0;
    	
    	//get total follow count 
    	int followsCount = UserRelationService.getFollowsCount(userId);
    	if (followsCount > 0) {
    	    pageTotal = (int) Math.ceil((double) followsCount / countPerPage);
    	}
    	result.addProperty("followsCount", followsCount);
        result.addProperty("pageTotal", pageTotal);
    	
    	if (pageTotal == 0 || pageIndex > pageTotal) {
    	    result.addProperty("TagCode", TagCodeEnum.SUCCESS);
    	    return result; 
    	}
        
        JsonArray jRoomList = new JsonArray();
        
    	List<RoomInfo> roomList = null;
    	
    	//查看1000以上的关注人页按关注时间排序,没有排序必要 
    	if (pageIndex * countPerPage > 1000 || sortType == 1) {
    	    roomList = com.melot.kkcx.service.UserRelationService.getFollowByTime(userId, countPerPage, pageIndex);
    	} else {
    	    roomList = com.melot.kkcx.service.UserRelationService.getFollowByLiveState(userId, followsCount, pageIndex, pageTotal, countPerPage, platform);
    	}
    	if (roomList != null) {
    	    for (RoomInfo roomInfo : roomList) {
    	    	JsonObject roomJson = com.melot.kkcx.service.UserRelationService.roomInfoTF(platform, roomInfo);
    	        jRoomList.add(roomJson);
    	    }
    	}
    	
    	result.add("roomList", jRoomList);
    	
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		
		// 返回结果
		return result;
	}

	/**
	 * 获取用户粉丝列表(10003004)
	 * 
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return 结果字符串
	 */
	public JsonObject getUserFansList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
	    JsonObject result = new JsonObject();
	    
	    // 该接口toke可选
		int selfTag = 0;// 不带token,查看他人的粉丝列表
		if (checkTag) {
			selfTag = 1;// 带有token,查看自己的粉丝列表
		} else {
		    result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		    return result;
		}
		
        int userId = 0;
        int pageIndex = 1;
        int countPerPage = 20;
        int platform = 0;
        @SuppressWarnings("unused")
        int appId = 0;
        
        // 获取参数
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "03040002", 1, Integer.MAX_VALUE);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 20, null, 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, null, 1, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
		
		int start = (pageIndex - 1) * countPerPage;
		int end = pageIndex * countPerPage - 1;
		int pageTotal = 0;
		
		int totalCount = UserRelationService.getFansCount(userId);
		if (totalCount > 0) {
			if (totalCount % countPerPage == 0) {
				pageTotal = (int) totalCount / countPerPage;
			} else {
				pageTotal = (int) (totalCount / countPerPage) + 1;
			}
		}
		result.addProperty("fansCount", totalCount);
		
		JsonArray jRoomList = new JsonArray();
		
		String fanIdsStr = UserRelationService.getFanIdsString(userId, start, end);
		if (fanIdsStr != null) {
			// 调用存储过程得到结果
			Map<Object, Object> map = new HashMap<Object, Object>();
			map.put("userId", Integer.valueOf(userId));
			map.put("fanIds", fanIdsStr);
			map.put("selfTag", selfTag);
			try {
				SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("UserRelation.getUserFansList", map);
			} catch (SQLException e) {
				logger.error("未能正常调用存储过程", e);
			}
			String TagCode = (String) map.get("TagCode");
			if (TagCode.equals(TagCodeEnum.SUCCESS)) {
				// 取出列表
				@SuppressWarnings("unchecked")
				List<Room> roomList = (ArrayList<Room>) map.get("roomList");
				List<RoomInfo> roomInfoList = null;
				if (roomList != null && roomList.size() > 0) {
					roomList = UserService.addUserExtra(roomList);
	                List<Integer> actorIds = new ArrayList<Integer>();
	                StringBuffer actorIds2 = new StringBuffer();
	                for (Room room : roomList) {
	                    if (room.getActorTag() != null && room.getActorTag().intValue() == 1) {
	                        actorIds.add(room.getUserId());
	                        actorIds2.append(room.getUserId());
	                        actorIds2.append(",");
	                    }
	                }
	                if (actorIds2.length() > 0) {
	                    roomInfoList = com.melot.kktv.service.RoomService.getRoomListByRoomIds(actorIds2.substring(0, actorIds2.length() - 1));
	                }
	                for (Room room : roomList) {
	                	int roomId = room.getUserId();
	                	JsonObject roomJson = new JsonObject();
	                	if (room.getActorTag() != null && room.getActorTag().intValue() == 1
	                			&& roomInfoList != null && roomInfoList.size() > 0) {
	                		boolean flag = false;
	                		for (RoomInfo rinfo : roomInfoList) {
	                			if (rinfo.getActorId() != null && rinfo.getActorId().intValue() == roomId) {
	                				roomJson = RoomTF.roomInfoToJson(rinfo, platform, true);
	                				roomJson.addProperty("actorTag", 1);
	                				flag = true;
	                			}
	                		}
	                		if (!flag) {
	                			roomJson = room.toJsonObject(platform, null);
	                			roomJson.addProperty("actorTag", 0);
	                		}
	                	} else {
	                		roomJson = room.toJsonObject(platform, null);
	                		roomJson.addProperty("actorTag", 0);
	                	}
	                	
	                	jRoomList.add(roomJson);
	                }   
	            }
			} else {
				// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
				logger.error("调用存储过程(UserRelation.getUserFansList)未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
			}
		}
		
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		result.addProperty("pageTotal", pageTotal);
		result.add("roomList", jRoomList);
		
		// 返回结果
		return result;
	}
	
	/**
     * 获取用户关注的ID列表(10003005)
     * 
     * @param jsonObject 请求对象
     * @return 结果字符串
     */
    public JsonObject getUserFollowedIds(JsonObject jsonObject,boolean checkTag,HttpServletRequest request) {
        
    	JsonObject result = new JsonObject();
    	
    	if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
    	
        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        JsonArray followedIds = new JsonArray();
        
		String followIdsStr = UserRelationService.getFollowIdsString(userId, 0, 500);
		if (followIdsStr != null) {
			List<Integer> followedIdList = new ArrayList<Integer>();
			String[] followedIdArr = followIdsStr.split(",");
			for (String followedId : followedIdArr) {
				try {
					int followedIdInt = Integer.valueOf(followedId);
					if (followedIdInt > 0) {
						followedIdList.add(followedIdInt);
					}
				} catch (Exception e) {
				}
			}
			if (followedIdList.size() > 0) {
				try {
					followedIds = new JsonParser().parse(new Gson().toJson(followedIdList)).getAsJsonArray();
				} catch (Exception e) {
				}
			}
		}
		
		result.add("followedIds", followedIds);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
	
	/**
	 * 判断是否关注(10003014)
	 * 
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return 结果字符串
	 */
	public JsonObject whetherFollowed(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
	    JsonObject result = new JsonObject();

        int userId, followedId;
        
        // 获取参数
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            followedId = CommonUtil.getJsonParamInt(jsonObject, "followedId", 1, "03140003", 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
	    
		if (UserRelationService.isFollowed(userId, followedId)) {
			result.addProperty("count", 1);
		} else {
			result.addProperty("count", 0);
		}
		
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
	}

	/**
	 * 获取用户未关注的主播列表(10003015)(最多10条)
	 * 
	 * @param paramJsonObject
	 * @return
	 */
	public JsonObject getNotFollowActorList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		
		JsonObject result = new JsonObject();
		
		int userId, platform, appId;
		try {
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, 1, Integer.MAX_VALUE);
			appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 0, Integer.MAX_VALUE);
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
		} catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch (Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		
		JsonArray roomArray = new JsonArray();
		
		int total = 0;
		
		// 关注数大于500不返回推荐主播
		int followsCount = UserRelationService.getFollowsCount(userId);
		if (followsCount < 500) {
			// 获取用户关注列表
			Set<String> followedIds = followsCount <= 0 ? new HashSet<String>() : UserRelationService.getFollowIds(userId, 0, -1);
			// 最多返回10个推荐主播
			int requireSize = 0;
			requireSize = 10;
			total = requireSize + followedIds.size();
			
			List<RoomInfo> roomList = null;
			try {
				FirstPageHandler firstPageHandler = MelotBeanFactory.getBean("firstPageHandler", FirstPageHandler.class);
				roomList = firstPageHandler.getRecommendRooms(null, userId, appId, total);
			} catch(Exception e) {
				logger.error("Fail to call firstPageHandler.getRecommendRooms ", e);
			}
			if (roomList != null) {
				for(RoomInfo roomInfo : roomList) {
					if (roomInfo.getActorId() != null && !followedIds.contains(roomInfo.getActorId().toString())) {
						JsonObject json = RoomTF.roomInfoToJsonTemp(roomInfo, platform);
						roomArray.add(json);
						if (roomArray.size() == requireSize) {
							break;
						}
					}
				}
				total = roomArray.size();
				result.add("roomList", roomArray);
			}
		}
		
		result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
		result.addProperty("total", total);
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		
		return result;
	}
	
	/**
	 * 授权管理
	 * 
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return 结果字符串
	 */
	@Deprecated
	public JsonObject grant(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
	    JsonObject result = new JsonObject();
		
	    // 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		int userId, grantedId;
        
        // 获取参数
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "03090002", 1, Integer.MAX_VALUE);
            grantedId = CommonUtil.getJsonParamInt(jsonObject, "grantedId", 1, "03090004", 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

		// 调用存储过程得到结果
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("userId", userId);
		map.put("grantedId", grantedId);
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("UserRelation.grant", map);
		} catch (SQLException e) {
			logger.error("未能正常调用存储过程", e);
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			return result;
		}
		
		String TagCode = (String) map.get("TagCode");
		if (TagCode.equals(TagCodeEnum.SUCCESS)) {
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			return result;
		} else {
			// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
			logger.error("调用存储过程(UserRelation.grant)未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
			result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
			return result;
		}
	}

	/**
	 * 收回管理权限
	 * 
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return 结果字符串
	 */
	@Deprecated
	public JsonObject revoke(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
	    JsonObject result = new JsonObject();
		
	    // 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
        
        int userId, revokedId;
        
        // 获取参数
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "03100002", 1, Integer.MAX_VALUE);
            revokedId = CommonUtil.getJsonParamInt(jsonObject, "revokedId", 1, "03100004", 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

		// 调用存储过程得到结果
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("userId", userId);
		map.put("revokedId", revokedId);
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("UserRelation.revoke", map);
		} catch (SQLException e) {
			logger.error("未能正常调用存储过程", e);
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			return result;
		}
		
		String TagCode = (String) map.get("TagCode");
		if (TagCode.equals(TagCodeEnum.SUCCESS)) {
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			return result;
		} else {
			// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
			logger.error("调用存储过程(UserRelation.revoke)未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
			result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
			return result;
		}
	}
	
	/**
	 * 用户取消房间管理员权限(10003020)
	 * @param jsonObject
	 * @param checkTag
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public JsonObject userCancelRoomAdmin(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
	    JsonObject result = new JsonObject();
		
	    // 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
        
        int userId, roomId;
        
        // 获取参数
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "03100002", 1, Integer.MAX_VALUE);
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 1, "03100004", 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
			ActorRelationService actorRelationService = MelotBeanFactory.getBean("kkActorRelationService", ActorRelationService.class);
			if (actorRelationService.delRelation(roomId, userId, RelationType.ADMIN.typeId()) != 0) {
				result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
				return result;
			}
		} catch (Exception e) {
			result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
			logger.error("fail to get ActorRelationService.delRelation(userId : " + userId + ", roomId : " + roomId + ")", e);
			return result;
		}
        
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
	}
	
	/**
	 * 获取用户房间的管理员列表(10003011)
	 * 
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return 结果字符串
	 */
	@SuppressWarnings("unchecked")
	public JsonObject getRoomManagerList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
	    JsonObject result = new JsonObject();
		
	    // 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
        
        int userId, pageIndex, countPerPage, platform;
        
        // 获取参数
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "03110002", 1, Integer.MAX_VALUE);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", Constant.return_room_count, null, 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, null, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
		
		List<Room> roomList = null;
		int pageTotal = 0;
		try {
			ActorRelationService actorRelationService = MelotBeanFactory.getBean("kkActorRelationService", ActorRelationService.class);
			List<ActorRelation> relationList = actorRelationService.getRelationByType(userId, RelationType.ADMIN.typeId());
			Map<String, Object> localMap = interceptAndSortList(relationList, 1, pageIndex, countPerPage);
			if (localMap != null) {
				pageTotal = (int) localMap.get("page");
				roomList = (List<Room>) localMap.get("list");
			}
		} catch (Exception e) {
			logger.error("调用关系模块出错", e);
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			return result;
		}
		
		JsonArray jRoomList = new JsonArray();
		List<RoomInfo> roomInfoList = null;
		try {
			if (roomList != null && roomList.size() > 0) {
				roomList = UserService.addUserExtra(roomList);
			    List<Integer> actorIds = new ArrayList<Integer>();
			    StringBuffer actorIds2 = new StringBuffer();
			    for (Room room : roomList) {
			        if (room.getActorTag() != null && room.getActorTag().intValue() == 1) {
			            actorIds.add(room.getUserId());
			            actorIds2.append(room.getUserId());
			            actorIds2.append(",");
			        }
			    }
			    if (actorIds2.length() > 0) {
			        roomInfoList = com.melot.kktv.service.RoomService.getRoomListByRoomIds(actorIds2.substring(0, actorIds2.length() - 1));
			    }
			    for (Room room : roomList) {
			    	int roomId = room.getUserId();
			    	if (room.getActorTag() != null && room.getActorTag().intValue() == 1
			    			&& roomInfoList != null && roomInfoList.size() > 0) {
			    		boolean flag = false;
			    		for (RoomInfo rinfo : roomInfoList) {
			    			if (rinfo.getActorId() != null && rinfo.getActorId().intValue() == roomId) {
			    				jRoomList.add(RoomTF.roomInfoToJson(rinfo, platform, true));
			    				flag = true;
			    			}
			    		}
			    		if (!flag) {
			    			jRoomList.add(room.toJsonObject(platform, null));
			    		}
			    	} else {
			    		jRoomList.add(room.toJsonObject(platform, null));
			    	}
			    }
			}
		} catch (Exception e) {
			logger.error("realtion transform room object catched exception: ", e);
			result.addProperty("TagCode", TagCodeEnum.UNCATCHED_EXCEPTION);
			return result;
		}
		
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		result.add("roomList", jRoomList);
		result.addProperty("pageTotal", pageTotal);
		// 返回结果
		return result;
		
	}
	
	private static Map<String, Object> interceptAndSortList(List<ActorRelation> list, int type, int pageIndex, int countPerPage) {
		if (list != null && list.size() > 0) {
			Map<String, Object> map = new HashMap<String, Object>();
			int start = 0, end = 1;
			if (list.size() < countPerPage * pageIndex && list.size() >= countPerPage * (pageIndex - 1)) {
				start = countPerPage * (pageIndex - 1);
				end = list.size();
			} else if (list.size() < countPerPage * (pageIndex - 1)) {
				return null;
			} else if (list.size() >= countPerPage * pageIndex) {
				start = countPerPage * (pageIndex - 1);
				end = countPerPage * pageIndex;
			}
			if (type == 2) {
				Collections.sort(list, new Comparator<ActorRelation>(){
					@Override
					public int compare(ActorRelation o1, ActorRelation o2) {
						if (o1.getCreateTime() == null || o2.getCreateTime() == null) {
							return -1;
						}
						return o2.getCreateTime().compareTo(o1.getCreateTime());
					}
					
				});
			}
			int page = list.size() / countPerPage + (list.size() % countPerPage == 0 ?  0 : 1);
			List<ActorRelation> newList = list.subList(start, end);
			List<Room> roomList = new ArrayList<Room>();
			Room room = null;
			if (type == 1) {
				for (ActorRelation ar : newList) {
					room = new Room();
					room.setUserId(ar.getRelationId());
					roomList.add(room);
				}
			} else {
				for (ActorRelation ar : newList) {
					room = new Room();
					room.setUserId(ar.getActorId());
					roomList.add(room);
				}
			}
			map.put("page", page);
			map.put("list", roomList);
			return map;
		}
		return null;
	}
	
	/**
	 * 获取用户管理的房间的列表(10003012)
	 * 
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return 结果字符串
	 */
	@SuppressWarnings("unchecked")
	public JsonObject getUserManagedRoomList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
	    JsonObject result = new JsonObject();
	    
	    if (!checkTag) { 
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        }
        
        int userId, pageIndex, countPerPage, platform;
        
        // 获取参数
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "03120002", 1, Integer.MAX_VALUE);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", Constant.return_room_count, null, 1, Constant.return_room_count);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, null, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
		
        List<Room> roomList = null;
        int pageTotal = 0;
		try {
			ActorRelationService actorRelationService = MelotBeanFactory.getBean("kkActorRelationService", ActorRelationService.class);
			List<ActorRelation> relationList = actorRelationService.getRelationByUserId(userId, RelationType.ADMIN.typeId());
			Map<String, Object> localMap = interceptAndSortList(relationList, 2, pageIndex, countPerPage);
			if (localMap != null) {
				pageTotal = (int) localMap.get("page");
				roomList = (List<Room>) localMap.get("list");
			}
		} catch (Exception e) {
			logger.error("调用关系模块出错", e);
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			return result;
		}
						
		JsonArray jRoomList = new JsonArray();
		List<RoomInfo> roomInfoList = null;
		try {
			if (roomList != null && roomList.size() > 0) {
				roomList = UserService.addUserExtra(roomList);
			    List<Integer> actorIds = new ArrayList<Integer>();
			    StringBuffer actorIds2 = new StringBuffer();
			    for (Room room : roomList) {
			        if (room.getActorTag() != null && room.getActorTag().intValue() == 1) {
			            actorIds.add(room.getUserId());
			            actorIds2.append(room.getUserId());
			            actorIds2.append(",");
			        }
			    }
			    if (actorIds2.length() > 0) {
			        roomInfoList = com.melot.kktv.service.RoomService.getRoomListByRoomIds(actorIds2.substring(0, actorIds2.length() - 1));
			    }
			    
			    if (roomList.size() > 1) {
			        Collections.sort(roomList, new Comparator<Room>() {
			            public int compare(Room r1, Room r2) {
			                Integer live1 = r1.getLiveendtime() == null ? 1 : 0;
			                Integer live2 = r2.getLiveendtime() == null ? 1 : 0;
			                return live2.compareTo(live1);
			            }
			        });
			    }
			    for (Room room : roomList) {
			    	int roomId = room.getUserId();
			    	if (room.getActorTag() != null && room.getActorTag().intValue() == 1
			    			&& roomInfoList != null && roomInfoList.size() > 0) {
			    		boolean flag = false;
			    		for (RoomInfo rinfo : roomInfoList) {
			    			if (rinfo.getActorId() != null && rinfo.getActorId().intValue() == roomId) {
			    				jRoomList.add(RoomTF.roomInfoToJson(rinfo, platform, true));
			    				flag = true;
			    			}
			    		}
			    		if (!flag) {
			    			jRoomList.add(room.toJsonObject(platform, null));
			    		}
			    	} else {
			    		jRoomList.add(room.toJsonObject(platform, null));
			    	}
			    }
			}
		} catch (Exception e) {
			logger.error("realtion transform room object catched exception: ", e);
			result.addProperty("TagCode", TagCodeEnum.UNCATCHED_EXCEPTION);
			return result;
		}
		
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		result.add("roomList", jRoomList);
		result.addProperty("pageTotal", pageTotal);
		result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
		// 返回结果
		return result;
	}

	/**
	 * 获取用户粉丝排行榜(10003013)
	 * 
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return 结果字符串
	 */
    public JsonObject getFansRankingList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		
		JsonObject result = new JsonObject();
		
		int userId, slotType, platform, appId, num;
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "03130001", 1, Integer.MAX_VALUE);
			slotType = CommonUtil.getJsonParamInt(jsonObject, "slotType", 0, "03130003", 0, Integer.MAX_VALUE);
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, null, 1, Integer.MAX_VALUE);
			appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 1, Integer.MAX_VALUE);
			num = CommonUtil.getJsonParamInt(jsonObject, "offset", 8, null, 1, Integer.MAX_VALUE);  //获取的总条数,默认不传为8条
		} catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch (Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		
		JsonArray fansList = new JsonArray();
		
		if (slotType == RankingEnum.RANKING_DAILY) {
			// 请求node-http 获取本场粉丝榜
		} else {  //周, 月, 总
			List<FansRankingItem> fansRankList = RoomService.getRoomFansRankList(userId, slotType);
	    	if (fansRankList != null) {
	    	    int size = fansRankList.size();
	    	    int total = num > size ? size : num;
	    		for (int i = 0; i < total; i++) {
	    			
	    			FansRankingItem item = fansRankList.get(i);
	    			JsonObject fansJson = item.toJsonObject(platform, appId);
	    			
	    		    fansList.add(fansJson);
	    		}
	    	}
		}
		
    	result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		result.add("fansList", fansList);
		
		return result;
	}

	/**
	 * 举报用户 (10003016)
	 * 
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return 结果字符串
	 */
	public JsonObject reportUser(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
	    JsonObject result = new JsonObject();
		
	    // 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
        
        int userId, reportId, roomId, newsId, platform;
        String reason = null;
        
        // 获取参数
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "03160002", 1, Integer.MAX_VALUE);
            reportId = CommonUtil.getJsonParamInt(jsonObject, "reportId", 1, "03160004", 1, Integer.MAX_VALUE);
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, null, 1, Integer.MAX_VALUE);
            newsId = CommonUtil.getJsonParamInt(jsonObject, "newsId", 0, null, 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, null, 1, Integer.MAX_VALUE);
            
            reason = CommonUtil.getJsonParamString(jsonObject, "reason", "", null, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

		// 调用存储过程得到结果
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("userId", userId);
		map.put("reportId", reportId);
		if (roomId > 0) {
		    map.put("roomId", roomId);
        }
        if (newsId > 0) {
        	if (StringUtil.strIsNull(reason)) {
        		result.addProperty("TagCode", "03160009"); //新增
				return result;
        	}
            map.put("newsId", newsId);
            map.put("reason", reason);
        }
        if (platform > 0) {
            map.put("platform", platform);
        }
        
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("UserRelation.reportUser", map);
		} catch (SQLException e) {
			logger.error("未能正常调用存储过程", e);
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			return result;
		}
		String TagCode = (String) map.get("TagCode");
		if (TagCode.equals(TagCodeEnum.SUCCESS)) {
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			return result;
		} else if (TagCode.equals("02")){
			result.addProperty("TagCode", "03160102"); //动态编号不存在或者动态所属用户错误
			return result;
		} else {
			// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
			logger.error("调用存储过程(UserRelation.reportUser)未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
			result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
			return result;
		}
	}

	/**
	 * 订阅房间开播推送消息(10003017)
	 * 
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return 结果字符串
	 */
	public JsonObject subscribeStartLive(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
	    JsonObject result = new JsonObject();
		
	    // 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		int roomId;
		
		// 获取参数
        try {
            CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "03170002", 1, Integer.MAX_VALUE);
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, "03170004", 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        jsonObject.addProperty("followedIds", roomId);
        return follow(jsonObject, checkTag, request);
	}
	
	/**
	 * 取消订阅房间开播推送消息(10003018)
	 * 
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return 结果字符串
	 */
	public JsonObject cancelSubscribedStartLive(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
	    JsonObject result = new JsonObject();
		
	    // 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		int roomId;
        
        // 获取参数
        try {
            CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, TagCodeEnum.ROOMID_MISSING, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        jsonObject.addProperty("canceledId", roomId);
        return cancelFollow(jsonObject, checkTag, request);
	}
	
	/**
	 * 判断是否订阅房间开播推送消息(10003019)
	 * 
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return 结果字符串
	 */
	public JsonObject whetherSubscribedStartLive(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
	    JsonObject result = new JsonObject();
		
	    // 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
        
        int roomId;
        
        // 获取参数
        try {
            CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, TagCodeEnum.ROOMID_MISSING, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        jsonObject.addProperty("followedId", roomId);
        
        result = whetherFollowed(jsonObject, checkTag, request);
        if (result.has("count")) {
            result.add("isSubscribed", result.get("count"));
            result.remove("count");
        }
        
        return result;
	}
	
	/**  
     * 计算两个日期之间相差的天数  
     * @param smdate 较小的时间 
     * @param bdate  较大的时间 
     * @return 相差天数 
     * @throws ParseException  
     */
	@SuppressWarnings("unused")
	private static int daysDiff(Date smdate, Date bdate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			smdate = sdf.parse(sdf.format(smdate));
			bdate = sdf.parse(sdf.format(bdate));
			long time1 = smdate.getTime();
			long time2 = bdate.getTime();
			long between_days = (time2 - time1) / (1000 * 3600 * 24);
			return Integer.parseInt(String.valueOf(between_days));
		} catch (ParseException e) {
			logger.error("时间转换失败", e);
		}
		return 0;
	}

}


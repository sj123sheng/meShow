package com.melot.kkcx.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.api.menu.sdk.handler.FirstPageHandler;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcx.redis.UserRelationSource;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.StringUtil;
import com.melot.sdk.core.util.MelotBeanFactory;

public class UserRelationService {

	public static List<RoomInfo> getFollowByLiveState(int userId, int followsCount, int pageIndex, int pageTotal, int countPerPage, int platform) {
		
		//first get from redis
        if (UserRelationSource.isKeyExist("follow_list_" + userId)) {
        	String json = UserRelationSource.getHotFieldValue("follow_list_" + userId, String.format("%s_%s_%s", platform, pageIndex, countPerPage));
        	if (json != null) {
        	    return new Gson().fromJson(json, new com.google.gson.reflect.TypeToken<List<RoomInfo>>(){}.getType());
        	}
        }
        if (ConfigHelper.getFollowStaticMax() < followsCount) {
        	followsCount = ConfigHelper.getFollowStaticMax();
        }
        String idStr = com.melot.kktv.service.UserRelationService.getFollowIdsString(userId, 0, followsCount - 1 > 1000 ? 1000 : followsCount - 1);
        if (StringUtil.strIsNull(idStr)) {
            return null;
        }
        int idInt, idOrder = 1;
        Map<Integer, Integer> mapUserScore = new HashMap<Integer, Integer>();
        List<Integer> idIntList = new ArrayList<Integer>();
        String[] ids = idStr.split(",");
        for (int i = 0; i < ids.length && i < 1000; i++) {
            idInt = Integer.valueOf(ids[i].trim());
            idIntList.add(idInt);
            mapUserScore.put(idInt, idOrder++);
        }
        
        TreeMap<Integer, RoomInfo> outputMap = new TreeMap<Integer, RoomInfo>();
        
        // 获取在线主播
        FirstPageHandler firstPageHandler = MelotBeanFactory.getBean("firstPageHandler", FirstPageHandler.class);
        List<RoomInfo> liveRoomInfos = firstPageHandler.getLiveRooms(ids);
        List<Integer> liveIds;
        if (liveRoomInfos != null && liveRoomInfos.size() > 0) {
            liveIds = new ArrayList<Integer>();
            for (RoomInfo roomInfo : liveRoomInfos) {
                roomInfo.setIcon(1);
                outputMap.put(roomInfo.getPeopleInRoom() == null ? 0 : 0 - roomInfo.getPeopleInRoom(), roomInfo);
                liveIds.add(roomInfo.getActorId());
            }
            
            idIntList.removeAll(liveIds);
        }
        
        // 读取用户信息
        com.melot.kkcore.user.service.KkUserService userService = (com.melot.kkcore.user.service.KkUserService) MelotBeanFactory.getBean("kkUserService");
        if (idIntList.size() > 0 && userService != null) {
            List<UserProfile> userList = userService.getUserProfileBatch(idIntList);
            if (userList != null && userList.size() > 0) {
                RoomInfo roomInfo;
                for (UserProfile userInfo : userList) {
                    if (userInfo != null) {
                        //多加主播标志
                        if (userInfo.getIsActor() == 1) {
                            roomInfo = RoomService.getRoomInfo(userInfo.getUserId());
                            if (roomInfo != null) {
                                roomInfo.setIcon(1);
                                outputMap.put(mapUserScore.get(userInfo.getUserId()), roomInfo);
                                continue;
                            }
                        }
                        
                        roomInfo = new RoomInfo();
                        roomInfo.setIcon(0);
                        roomInfo.setActorLevel(userInfo.getActorLevel());
                        roomInfo.setRichLevel(userInfo.getUserLevel());
                        
                        
                        if (userInfo.getNickName() != null) {
                            roomInfo.setNickname(userInfo.getNickName());
                        }
                        if (userInfo.getPortrait() != null) {
                            roomInfo.setPortrait(userInfo.getPortrait());
                        }
//                        if (userInfo.getPoster != null) {
//                            roomInfo.setPoster(userInfo.getPoster_path());
//                        }
                        roomInfo.setActorId(userInfo.getUserId());
                        roomInfo.setGender(userInfo.getGender());
                        
                        outputMap.put(mapUserScore.get(userInfo.getUserId()), roomInfo);
                    }
                }
            }
        }
    	
        List<RoomInfo> output = null;
    	if (outputMap.size() > 0) {
    	    List<RoomInfo> roomList = new ArrayList<RoomInfo>(outputMap.values());
    	    
    		//分页缓存
    		if (pageTotal > 0) {
    			Map<String, String> map = new HashMap<String, String>();
    			int flagPage = 0;
    			int size = outputMap.size();
    			while (flagPage < pageTotal) {
    		    	List<RoomInfo> jRoomList = new ArrayList<RoomInfo>();
    		    	int i = 0;
    		    	while (i < countPerPage) {
    		    		if (flagPage * countPerPage + i >= size) {
    		    			break;
    		    		}
    		    		int index = flagPage * countPerPage + i;
    		    		RoomInfo roomInfo = roomList.get(index);
    		    		jRoomList.add(roomInfo);
    					i ++;
    		    	}
    		    	flagPage ++;
    		    	if (flagPage == pageIndex) {
    		    		output = jRoomList;
    		    	}
    		    	map.put(String.format("%s_%s_%s", platform, flagPage, countPerPage), new Gson().toJson(jRoomList));
    			}
    			//save redis
    			UserRelationSource.setHotData("follow_list_" + userId, map, 60);
    		}
    	}
    	
    	return output;
	}
	
	public static boolean removeFollowedResultList(int userId) {
		//first get from redis
        if (UserRelationSource.isKeyExist("follow_list_" + userId)) {
        	return UserRelationSource.removeKey("follow_list_" + userId);
        }
        return true;
	}
	
    public static boolean removeManageResultList(int userId) {
        if (UserRelationSource.isKeyExist("manage_list_" + userId)) {
            return UserRelationSource.removeKey("manage_list_" + userId);
        }
        return true;
    }
	
	public static JsonObject roomInfoTF(Integer platform, RoomInfo roomInfo) {
		
		if (roomInfo == null) {
			return null;
		}
		
		JsonObject jObject = new JsonObject();
		
		if (roomInfo.getScreenType() != null) {
		    jObject.addProperty("screenType", roomInfo.getScreenType());
        } else {
            jObject.addProperty("screenType", 1);
        }

		jObject.addProperty("userId", roomInfo.getActorId());
		// 轮播房添加roomId，非轮播房正在直播的主播等于actorId
		jObject.addProperty("roomId", roomInfo.getRoomId() != null ? roomInfo.getRoomId() : roomInfo.getActorId());
		
		String nickname = roomInfo.getNickname();
		//非官方号需昵称过滤
        Integer adminType = ProfileServices.getUserAdminType(roomInfo.getActorId());
        if (adminType == null || adminType == -1) {
            nickname = GeneralService.replaceNicknameSensitiveWords(nickname);
        }
		
		jObject.addProperty("nickname", nickname);
		if (roomInfo.getGender() != null) {
			jObject.addProperty("gender", roomInfo.getGender());
	    } else {
	    	jObject.addProperty("gender", 0);
	    }
		if (platform == PlatformEnum.WEB) {
			if (roomInfo.getPortrait() != null) {
				jObject.addProperty("portrait_path_256", ConfigHelper.getHttpdir() + roomInfo.getPortrait()  + "!256");
			}
		} else if (platform.equals(PlatformEnum.ANDROID) 
				|| platform.equals(PlatformEnum.IPHONE)
				|| platform.equals(PlatformEnum.IPAD)) {
			if (roomInfo.getPortrait() != null) {
				jObject.addProperty("portrait_path_128", ConfigHelper.getHttpdir() + roomInfo.getPortrait());
			}
		}
		if (roomInfo.getPoster() != null) {
			jObject.addProperty("poster_path_272", ConfigHelper.getHttpdir() + roomInfo.getPoster() + "!272");
			jObject.addProperty("poster_path_1280", ConfigHelper.getHttpdir() + roomInfo.getPoster() + "!1280");
			jObject.addProperty("poster_path_256", ConfigHelper.getHttpdir() + roomInfo.getPoster() + "!256");
			jObject.addProperty("poster_path_300", ConfigHelper.getHttpdir() + roomInfo.getPoster() + "!300");
			jObject.addProperty("poster_path_400", ConfigHelper.getHttpdir() + roomInfo.getPoster() + "!400");
		}
		if (roomInfo.getLivePoster() != null) {
			jObject.addProperty("live_poster_272", ConfigHelper.getHttpdir() + roomInfo.getLivePoster() + "!272");
			jObject.addProperty("live_poster_256", ConfigHelper.getHttpdir() + roomInfo.getLivePoster() + "!256");
			jObject.addProperty("live_poster_300", ConfigHelper.getHttpdir() + roomInfo.getLivePoster() + "!300");
			jObject.addProperty("live_poster_400", ConfigHelper.getHttpdir() + roomInfo.getLivePoster() + "!400");
		}
	    if (roomInfo.getActorLevel() != null) {
	        jObject.addProperty("actorLevel", roomInfo.getActorLevel());
	    }
	    if (roomInfo.getRichLevel() != null) {
	        jObject.addProperty("richLevel", roomInfo.getRichLevel());
	    } else {
	        jObject.addProperty("richLevel", 0);
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
//        if (!StringUtil.strIsNull(roomInfo.getRoomTheme())) {
//            jObject.addProperty("roomTheme", roomInfo.getRoomTheme());
//        }
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
        if (roomInfo.getIcon() != null) {
        	jObject.addProperty("actorTag", roomInfo.getIcon());
        } else {
        	jObject.addProperty("actorTag", 0);
        }
        jObject.addProperty("starLevel", UserService.getStarLevel(roomInfo.getActorId()));
		// 读取靓号
		JsonObject validVirtualId =  UserAssetServices.getValidVirtualId(roomInfo.getActorId()); //获取用户虚拟账号
		if(validVirtualId != null) {
			if (validVirtualId.get("idType").getAsInt() == 1) {
				// 支持老版靓号
				jObject.addProperty("luckyId", validVirtualId.get("id").getAsInt());
			}
			jObject.add("validId", validVirtualId);
		}
		
		return jObject;
	
	}
	
	public static List<RoomInfo> getFollowByTime(int userId, int countPerPage, int pageIndex) {

		Set<String> ids = com.melot.kktv.service.UserRelationService.getFollowIds(userId, countPerPage * (pageIndex - 1), countPerPage * pageIndex - 1);
		if (ids == null || ids.size() < 1) {
            return null;
        }
		
		TreeMap<Integer, RoomInfo> outputMap = new TreeMap<Integer, RoomInfo>();
		
		Map<Integer, Integer> mapUserScore = new HashMap<Integer, Integer>();
		List<Integer> oraIds = new ArrayList<Integer>();
		int idInt, idOrder = 1;
		for (String id : ids) {
		    idInt = Integer.valueOf(id);
			oraIds.add(idInt);
			mapUserScore.put(idInt, idOrder++);
		}
		
		// 获取在线主播
		FirstPageHandler firstPageHandler = MelotBeanFactory.getBean("firstPageHandler", FirstPageHandler.class);
		List<RoomInfo> liveRoomInfos = firstPageHandler.getLiveRooms(ids.toArray(new String[0]));
		List<Integer> liveIds;
		if (liveRoomInfos != null && liveRoomInfos.size() > 0) {
		    liveIds = new ArrayList<Integer>();
		    for (RoomInfo roomInfo : liveRoomInfos) {
		        roomInfo.setIcon(1);
		        outputMap.put(mapUserScore.get(roomInfo.getActorId()), roomInfo);
		        liveIds.add(roomInfo.getActorId());
		    }
		    
		    oraIds.removeAll(liveIds);
        }
		
		com.melot.kkcore.user.service.KkUserService userService = (com.melot.kkcore.user.service.KkUserService) MelotBeanFactory.getBean("kkUserService");
    	if (oraIds.size() > 0 && userService != null) {
    		List<UserProfile> userList = userService.getUserProfileBatch(oraIds);
    		if (userList != null && userList.size() > 0) {
    		    RoomInfo roomInfo;
    			for (UserProfile userInfo : userList) {
    				if (userInfo != null) {
                        //多加主播标志
                        if (userInfo.getIsActor() == 1) {
                            roomInfo = RoomService.getRoomInfo(userInfo.getUserId());
                            if (roomInfo != null) {
                                roomInfo.setIcon(1);
                                outputMap.put(mapUserScore.get(userInfo.getUserId()), roomInfo);
                                continue;
                            }
                        }
                        
                        roomInfo = new RoomInfo();
                        roomInfo.setIcon(0);
                        roomInfo.setActorLevel(userInfo.getActorLevel());
                        roomInfo.setRichLevel(userInfo.getUserLevel());
    					
    					if (userInfo.getNickName() != null) {
    						roomInfo.setNickname(userInfo.getNickName());
    					}
    					if (userInfo.getPortrait() != null) {
    						roomInfo.setPortrait(userInfo.getPortrait());
    					}
//    					if (userInfo.getPoster_path() != null) {
//    					    roomInfo.setPoster(userInfo.getPoster_path());
//    					}
    					roomInfo.setActorId(userInfo.getUserId());
    					roomInfo.setGender(userInfo.getGender());
    					
    					outputMap.put(mapUserScore.get(userInfo.getUserId()), roomInfo);
    				}
    			}
    		}
    	}
    	
    	return outputMap.size() > 0 ? new ArrayList<RoomInfo>(outputMap.values()) : null;
	}
	
}

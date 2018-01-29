package com.melot.kktv.action;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.api.menu.sdk.dao.domain.HomePage;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.api.menu.sdk.dao.domain.SysMenu;
import com.melot.api.menu.sdk.handler.FirstPageHandler;
import com.melot.api.menu.sdk.redis.KKHallSource;
import com.melot.api.menu.sdk.service.RoomInfoService;
import com.melot.common.driver.domain.AreaNewActors;
import com.melot.common.driver.service.AreaNewActorsService;
import com.melot.content.config.domain.LiveAlbum;
import com.melot.content.config.domain.LiveVideo;
import com.melot.content.config.live.service.LiveAlbumService;
import com.melot.content.config.live.service.LiveVideoService;
import com.melot.content.config.live.upload.impl.QiniuService;
import com.melot.kkactivity.driver.domain.ActInfo;
import com.melot.kkactivity.driver.domain.KkActivity;
import com.melot.kkactivity.driver.service.KkActivityService;
import com.melot.kkcore.user.api.LastLoginInfo;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kkcx.service.ActorGiftService;
import com.melot.kkcx.service.GeneralService;
import com.melot.kkcx.service.UserAssetServices;
import com.melot.kkcx.service.UserService;
import com.melot.kkcx.transform.LiveShowTF;
import com.melot.kkcx.transform.NewsRewardRankTF;
import com.melot.kkcx.transform.RoomTF;
import com.melot.kktv.model.Activity;
import com.melot.kktv.model.HotActivity;
import com.melot.kktv.model.NewsRewardRank;
import com.melot.kktv.model.Notice;
import com.melot.kktv.model.PreviewAct;
import com.melot.kktv.model.RankUser;
import com.melot.kktv.model.Room;
import com.melot.kktv.model.WeekStarGift;
import com.melot.kktv.redis.GiftRecordSource;
import com.melot.kktv.redis.HotDataSource;
import com.melot.kktv.redis.NewsSource;
import com.melot.kktv.redis.SearchWordsSource;
import com.melot.kktv.redis.WeekGiftSource;
import com.melot.kktv.service.NewsService;
import com.melot.kktv.service.RoomService;
import com.melot.kktv.util.AppChannelEnum;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.CityUtil;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.CommonUtil.ErrorGetParameterException;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.Constant;
import com.melot.kktv.util.ConstantEnum;
import com.melot.kktv.util.DateUtil;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.RankingEnum;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.kktv.util.confdynamic.GiftInfoConfig;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * 大厅相关的接口类
 * 
 * @author LY
 * 
 */
public class IndexFunctions {

	/** 日志记录对象 */
	private static Logger logger = Logger.getLogger(IndexFunctions.class);
	
	/**
     * 正在直播的主播id
     */
    private static final String KK_LIVE_ACTOR_ID = "KKLiveActorId";

    private static AreaNewActorsService areaNewActorsService = MelotBeanFactory.getBean("areaNewActorsService", AreaNewActorsService.class);

    /**
	 * 获取大厅目录人数
	 * 
	 * @param jsonObject 请求对象
	 * @return
	 */
	public JsonObject getUserCount(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		int count = 0;
		// 当前时间的hour值
		int nowHour = DateUtil.getFieldOfDate(new Date(), Calendar.HOUR);
		// 至2013-1-1至今日期差
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		int dayAdd = 0;
		try {
			long time1 = sdf.parse("2013-1-1").getTime();
			long time2 = sdf.parse(sdf.format(new Date())).getTime();
			long between_days = (time2 - time1) / (1000 * 3600 * 24);
			dayAdd = Integer.parseInt(String.valueOf(between_days));
		} catch (Exception e) {
			dayAdd = 0;
		}
		List<Map<String, Integer>> objList = new ArrayList<Map<String, Integer>>();
		Map<String, Integer> obj02 = new HashMap<String, Integer>();
		obj02.put("startHour", 0);
		obj02.put("startNumber", 55000 + dayAdd * 1300 + 192500);
		obj02.put("endHour", 3);
		obj02.put("endNumber", 15000 + dayAdd * 350 + 8000);
		objList.add(obj02);
		Map<String, Integer> obj26 = new HashMap<String, Integer>();
		obj26.put("startHour", 3);
		obj26.put("startNumber", 15000 + dayAdd * 350 + 8000);
		obj26.put("endHour", 6);
		obj26.put("endNumber", 9500 + dayAdd * 230 + 5250);
		objList.add(obj26);
		Map<String, Integer> obj611 = new HashMap<String, Integer>();
		obj611.put("startHour", 6);
		obj611.put("startNumber", 9500 + dayAdd * 230 + 33250);
		obj611.put("endHour", 11);
		obj611.put("endNumber", 37000 +dayAdd * 900 + 129500);
		objList.add(obj611);
		Map<String, Integer> obj1115 = new HashMap<String, Integer>();
		obj1115.put("startHour", 11);
		obj1115.put("startNumber", 37000 +dayAdd * 900 + 129500);
		obj1115.put("endHour", 15);
		obj1115.put("endNumber", 48000 +dayAdd * 1300 + 168000);
		objList.add(obj1115);
		Map<String, Integer> obj1518 = new HashMap<String, Integer>();
		obj1518.put("startHour", 15);
		obj1518.put("startNumber", 48000 +dayAdd * 1300 + 168000);
		obj1518.put("endHour", 18);
		obj1518.put("endNumber", 55000 +dayAdd * 1200 + 192500);
		objList.add(obj1518);
		Map<String, Integer> obj1820 = new HashMap<String, Integer>();
		obj1820.put("startHour", 18);
		obj1820.put("startNumber", 55000 +dayAdd * 1200 + 192500);
		obj1820.put("endHour", 20);
		obj1820.put("endNumber", 60000 +dayAdd * 1400 + 210000);
		objList.add(obj1820);
		Map<String, Integer> obj2023 = new HashMap<String, Integer>();
		obj2023.put("startHour", 20);
		obj2023.put("startNumber", 60000 +dayAdd * 1400 + 210000);
		obj2023.put("endHour", 23);
		obj2023.put("endNumber", 65000 +dayAdd * 1600 + 230000);
		objList.add(obj2023);
		Map<String, Integer> obj2324 = new HashMap<String, Integer>();
		obj2324.put("startHour", 23);
		obj2324.put("startNumber", 65000 +dayAdd * 1600 + 230000);
		obj2324.put("endHour", 24);
		obj2324.put("endNumber", 55000 +dayAdd * 1300 + 192500);
		objList.add(obj2324);
		for (Map<String, Integer> map : objList) {
			int startHour = (int) map.get("startHour");
			int endHour = (int) map.get("endHour");
			int startNumber = (int) map.get("startNumber");
			int endNumber = (int) map.get("endNumber");
			if(nowHour >= startHour && nowHour < endHour) {
				String nowDate = sdf.format(new Date());
				try {
					long startTime = new SimpleDateFormat("yyyy-MM-dd HH").parse(nowDate+" "+startHour).getTime();
					long endTime = new SimpleDateFormat("yyyy-MM-dd HH").parse(nowDate+" "+endHour).getTime();
					long nowTime = System.currentTimeMillis();		
					count = (int) (startNumber + ((nowTime - startTime)*(endNumber-startNumber))/(endTime - startTime));
				} catch (Exception e) {
					count = (int) (startNumber + ((endHour - startHour)*(endNumber-startNumber))/(endHour - startHour));
				}
			}
		}
		count = count + new Random().nextInt(10000);
		JsonObject result = new JsonObject();
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		result.addProperty("count", count);
		return result;
	}

	/**
	 * 获取大厅房间列表(10002002) 目前仅支持catalogId 14-全部 17-人气 28-皇冠 24-超冠 25-巨钻 26-红星
	 * @param jsonObject
	 * @return
	 */
	public JsonObject getRoomList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		
		JsonObject result = new JsonObject();
		
		int catalogId, start, offset, platform;
		try {
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, null, 1, Integer.MAX_VALUE);
			catalogId = CommonUtil.getJsonParamInt(jsonObject, "catalogId", 14, null, 1, Integer.MAX_VALUE);
			start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, "02020005", 0, Integer.MAX_VALUE);
			offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 0, "02020003", 1, Integer.MAX_VALUE);
		} catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch (Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		
		// 定义结果
		
		int roomCount = 0;
		JsonArray jRoomList = new JsonArray();
		
		// 人气星天地
		if (catalogId == Constant.renqi_catalog_tag) {
			String ROOM_CACHE_KEY = "renqi_cache";
			roomCount = HotDataSource.countSortedSet(ROOM_CACHE_KEY);
			if (roomCount == 0) {
				List<RoomInfo> roomInfoList = new ArrayList<RoomInfo>();
//				Map<Integer, RoomInfo> roomMap = new HashMap<Integer, RoomInfo>();
				// 从pg读取正在直播房间列表 最多500
				try {
					RoomInfoService roomInfoServie = MelotBeanFactory.getBean("roomInfoService", RoomInfoService.class);
					List<RoomInfo> roomList = roomInfoServie.getLiveRoomList(AppIdEnum.AMUSEMENT, 0, 500);
					if (roomList != null && roomList.size() > 0) {
						for (RoomInfo roomInfo : roomList) {
							roomInfoList.add(roomInfo);
//							roomMap.put(roomId, roomInfo);
						}
					}
				} catch (Exception e) {
					logger.error("Fail to call roomInfoServie.getLiveRoomList", e);
				}
				
				if (roomInfoList.size() > 0) {
					// 重新对房间列表排序
					List<RoomInfo> resortedList = resortList(roomInfoList);
					List<String> strList = new ArrayList<String>();
					for (RoomInfo roomInfo : resortedList) {
						strList.add(new Gson().toJson(roomInfo));
					}
					HotDataSource.addSortedSet(ROOM_CACHE_KEY, strList, 120);
					roomCount = strList.size();
				}
			}
			if (start >= 0 && start < roomCount) {
				Set<String> set = HotDataSource.rangeSortedSet(ROOM_CACHE_KEY, start, start + offset - 1);
				if (set != null) {
					for (String json : set) {
						try {
							RoomInfo roomInfo = new Gson().fromJson(json, RoomInfo.class);
							JsonObject roomJson = RoomTF.roomInfoToJsonTemp(roomInfo, platform);
							if (roomJson.has("poster_path_1280") && roomJson.get("poster_path_1280") != null) {
								roomJson.addProperty("poster_path_1280", ConfigHelper.getHttpdir()
										+ roomJson.get("poster_path_1280").getAsString());
							}
							if (roomJson.has("poster_path_272") && roomJson.get("poster_path_272") != null) {
								roomJson.addProperty("poster_path_272", ConfigHelper.getHttpdir()
										+ roomJson.get("poster_path_272").getAsString());
							}
							if (roomJson.has("poster_path_128") && roomJson.get("poster_path_128") != null) {
								roomJson.addProperty("poster_path_128", ConfigHelper.getHttpdir()
										+ roomJson.get("poster_path_128").getAsString());
							}
							if (roomJson.has("poster_path_300") && roomJson.get("poster_path_300") != null) {
							    roomJson.addProperty("poster_path_300", ConfigHelper.getHttpdir()
							            + roomJson.get("poster_path_300").getAsString());
							}
							jRoomList.add(roomJson);
						} catch (Exception e) {
							logger.error("Fail to parse Json String to JavaBean RoomInfo ", e);
						}
					}
				}
			}
		} else if (catalogId == Constant.default_catalog_tag) {
			int cataId = 16;
			SysMenu sysMenu = null;
			try {
				FirstPageHandler firstPageHandler = MelotBeanFactory.getBean("firstPageHandler", FirstPageHandler.class);
				sysMenu = firstPageHandler.getPartList(cataId, null, null, start, offset) ;		
			} catch(Exception e) {
				logger.error("Fail to call firstPageHandler.getPartList, " + "cataId " + cataId, e);
			}
			if (sysMenu != null) {
				
				roomCount = sysMenu.getRoomCount().intValue();
				
				List<RoomInfo> roomList = sysMenu.getRooms();
				if (roomList != null) {
					for (RoomInfo roomInfo : roomList) {
						JsonObject roomJson = RoomTF.roomInfoToJsonTemp(roomInfo, platform);
						if (roomJson.has("poster_path_1280") && roomJson.get("poster_path_1280") != null) {
							roomJson.addProperty("poster_path_1280", ConfigHelper.getHttpdir()
									+ roomJson.get("poster_path_1280").getAsString());
						}
						if (roomJson.has("poster_path_272") && roomJson.get("poster_path_272") != null) {
							roomJson.addProperty("poster_path_272", ConfigHelper.getHttpdir()
									+ roomJson.get("poster_path_272").getAsString());
						}
						if (roomJson.has("poster_path_128") && roomJson.get("poster_path_128") != null) {
							roomJson.addProperty("poster_path_128", ConfigHelper.getHttpdir()
									+ roomJson.get("poster_path_128").getAsString());
						}
						if (roomJson.has("poster_path_300") && roomJson.get("poster_path_300") != null) {
						    roomJson.addProperty("poster_path_300", ConfigHelper.getHttpdir()
						            + roomJson.get("poster_path_300").getAsString());
						}
						jRoomList.add(roomJson);
					}
				}
			}
		} else {
			// 24-超冠 25-巨钻 26-红星 28-皇冠 
			Long minLevelPoint = null;
			Long maxLevelPoint = null;
			if (catalogId == 24) {
				minLevelPoint = 21l;
				maxLevelPoint = 100l;
			}
			if (catalogId == 25) {
				minLevelPoint = 7l;
				maxLevelPoint = 12l;
			}
			if (catalogId == 26) {
				minLevelPoint = 1l;
				maxLevelPoint = 7l;
			}
			if (catalogId == 28) {
				minLevelPoint = 12l;
				maxLevelPoint = 21l;
			}
			if (minLevelPoint != null && maxLevelPoint != null) {
				try {
					// 根据星级获取房间列表
		            RoomInfoService roomInfoServie = MelotBeanFactory.getBean("roomInfoService", RoomInfoService.class);
		            roomCount = roomInfoServie.getRoomCountByActorLevelPoint(AppIdEnum.AMUSEMENT, minLevelPoint, maxLevelPoint);
		            if (roomCount > 0) {
		            	List<RoomInfo> roomList = roomInfoServie.getRoomListByActorLevelPoint(AppIdEnum.AMUSEMENT, minLevelPoint, maxLevelPoint, start, offset);
			            if (roomList != null) {
							for (RoomInfo roomInfo : roomList) {
								JsonObject roomJson = RoomTF.roomInfoToJsonTemp(roomInfo, platform);
								if (roomJson.has("poster_path_1280") && roomJson.get("poster_path_1280") != null) {
									roomJson.addProperty("poster_path_1280", ConfigHelper.getHttpdir()
											+ roomJson.get("poster_path_1280").getAsString());
								}
								if (roomJson.has("poster_path_272") && roomJson.get("poster_path_272") != null) {
									roomJson.addProperty("poster_path_272", ConfigHelper.getHttpdir()
											+ roomJson.get("poster_path_272").getAsString());
								}
								if (roomJson.has("poster_path_128") && roomJson.get("poster_path_128") != null) {
									roomJson.addProperty("poster_path_128", ConfigHelper.getHttpdir()
											+ roomJson.get("poster_path_128").getAsString());
								}
								if (roomJson.has("poster_path_300") && roomJson.get("poster_path_300") != null) {
								    roomJson.addProperty("poster_path_300", ConfigHelper.getHttpdir()
								            + roomJson.get("poster_path_300").getAsString());
								}
								jRoomList.add(roomJson);
							}
						}
		            }
				} catch (Exception e) {
		           logger.error("roomInfoServie.getRoomListByActorLevelPoint, "
		           		+ "minLevelPoint : " + minLevelPoint + ", maxLevelPoint : " + maxLevelPoint, e);
		        }
			}
		}
		
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		result.addProperty("roomTotal", roomCount);
		result.add("roomList", jRoomList);	

		return result;
	}
	
	/**
	 * 随机重排List
	 * @param list
	 * @return
	 */
//	private static List<Integer> resortList(List<Integer> list) {
//		List<Integer> sortedList = new ArrayList<Integer>();
//		if(list!=null && list.size()>0) {
//			for (int i = 0; i < list.size(); i++) {
//				sortedList.add(null);
//			}
//			int[] randomSort = getRandomSort(list.size());
//			for (int i = 0; i < randomSort.length; i++) {
//				sortedList.set(i, list.get(randomSort[i]));
//			}
//		}
//		return sortedList;
//	}
	
	/**
	 * 根据关注人数排序
	 * @param list
	 * @return
	 */
	private static List<RoomInfo> resortList(List<RoomInfo> list) {
		if (list != null && list.size() > 0) {
			Comparator<RoomInfo> comparator = new Comparator<RoomInfo>() {
				public int compare(RoomInfo room1, RoomInfo room2) {
					if (room1.getPeopleInRoom().intValue() > room1.getPeopleInRoom().intValue())
			            return -1;
			        else if (room1.getPeopleInRoom().intValue() < room1.getPeopleInRoom().intValue())
			            return 1;
			        else
			            return 0;
				};
			};
			Collections.sort(list, comparator);
		}
		return list;
	}
	
	/**
	 * 对0~count个数随机重新排序
	 * @param count
	 * @return
	 */
//	private static int[] getRandomSort(int count) {
//		int[] sequence = new int[count];
//		for (int i = 0; i < count; i++) {
//			sequence[i] = i;
//		}
//		Random random = new Random();
//		for (int i = 0; i < count; i++) {
//			int p = random.nextInt(count);
//			int tmp = sequence[i];
//			sequence[i] = sequence[p];
//			sequence[p] = tmp;
//		}
//		random = null;
//		return sequence;
//	}
	
	/**
	 * 获取推荐的房间列表(10002003)
	 * 
	 * @return 结果字符串
	 */
	public JsonObject getFollowRecommendedList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		JsonObject result = new JsonObject();
		int platform = 0;
		int appId = 0;
		Integer roomId;
		Integer userId;
		int count = 0;
		try {
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, 1, Integer.MAX_VALUE);
			//默认为1
			appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 0, Integer.MAX_VALUE);
			roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, null, 1, Integer.MAX_VALUE);
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
			//默认为1
			count = CommonUtil.getJsonParamInt(jsonObject, "count", 4, null, 1, Integer.MAX_VALUE);
		} catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch (Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		
		if (roomId == 0) {
			roomId = null;
		}
		if (userId == 0) {
			userId = null;
		}
		List<RoomInfo> roomList = null;
		try {
			FirstPageHandler firstPageHandler = MelotBeanFactory.getBean("firstPageHandler", FirstPageHandler.class);
			roomList = firstPageHandler.getRecommendRooms(roomId, userId, appId, count);
		} catch(Exception e) {
			logger.error("Fail to call firstPageHandler.getRecommendRooms ", e);
		}
		if (roomList != null) {
			JsonArray roomArray = new JsonArray();
			for (RoomInfo roomInfo : roomList) {
				roomArray.add(RoomTF.roomInfoToJson(roomInfo, platform, true));
			}
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			result.add("roomList", roomArray);
		} else {
			result.addProperty("TagCode", TagCodeEnum.FAIL_TO_CALL_API_MENU_MODULE);
		}
		return result;
	}
	
	/**
	 * 获取排行榜(10002004)
	 * 
	 * @param jsonObject 请求对象
	 * @return 结果字符串
	 */
	public JsonObject getRankingList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
	    
        int rankType, slotType, count;
        
	    try {
	        rankType = CommonUtil.getJsonParamInt(jsonObject, "rankType", 0, "02040002", 0, Integer.MAX_VALUE);
	        slotType = CommonUtil.getJsonParamInt(jsonObject, "slotType", 0, "02040004", 0, Integer.MAX_VALUE);
            count = CommonUtil.getJsonParamInt(jsonObject, "count", 10, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

		if (rankType == RankingEnum.RANKING_TYPE_POPULAR) {
			rankType = RankingEnum.RANKING_TYPE_ACTOR_POPULAR;
			slotType = RankingEnum.RANKING_THIS_WEEK;
		}
		String collectionName = RankingEnum.getCollection(rankType, slotType);
		if (collectionName != null) {
	        JsonArray roomList = new JsonArray();
			KkUserService userService = MelotBeanFactory.getBean("kkUserService", KkUserService.class);
			Map<String, Double> sortMap = HotDataSource.getRevRangeWithScore(collectionName);
			if (sortMap != null && sortMap.size() > 0) {
				List<RankUser> rankUserList = new ArrayList<RankUser>();
				for (Entry<String, Double> entry : sortMap.entrySet()) {
					rankUserList.add(new RankUser(StringUtil.parseFromStr(entry.getKey(), 0), entry.getValue()));
				}
				Collections.sort(rankUserList);
				
				count = Math.min(sortMap.size(), count);
				int i = 1;
				String ids = "";
				JsonObject obj;
				int userId;
				UserProfile userProfile;
				for (RankUser rankUser : rankUserList) {
					if (i > count) {
						break;
					}
					i++;
					obj = new JsonObject();
					userId = rankUser.getUserId();
					ids += userId + ",";
					userProfile = userService.getUserProfile(userId);
					if (userProfile != null) {
						obj.addProperty("gender", userProfile.getGender());
						obj.addProperty("nickname", userProfile.getNickName());
						obj.addProperty("roomId", userId);
						obj.addProperty("userId", userId);
						obj.addProperty("actorLevel", userProfile.getActorLevel());
						obj.addProperty("richLevel", userProfile.getUserLevel());
						// 读取星级
	                    obj.addProperty("starLevel", UserService.getStarLevel(userId));
	                    if (userProfile.getPortrait() != null) {
	                    	obj.addProperty("portrait_path_original", ConfigHelper.getHttpdir() + userProfile.getPortrait());
	                    	obj.addProperty("portrait_path_48", ConfigHelper.getHttpdir() + userProfile.getPortrait() + "!48");
	                    	obj.addProperty("portrait_path_128", ConfigHelper.getHttpdir() + userProfile.getPortrait() + "!128");
	                    	obj.addProperty("portrait_path_256", ConfigHelper.getHttpdir() + userProfile.getPortrait() + "!256");
	                    	obj.addProperty("portrait_path_1280", ConfigHelper.getHttpdir() + userProfile.getPortrait() + "!1280");
	                    }
    					// 读取靓号
	                    JsonObject validVirtualId = UserAssetServices.getValidVirtualId(userId); //获取用户虚拟账号
	                    if (validVirtualId != null) {
	                        if (validVirtualId.get("idType").getAsInt() == 1) {
	                            // 支持老版靓号
	                            obj.addProperty("luckyId", validVirtualId.get("id").getAsInt());
	                        }
	                        obj.add("validId", validVirtualId);
	                    }
	                    if (rankType == 2 || rankType == 4 || rankType == 5 || rankType == 6 || rankType == 7) {
	                    	obj.addProperty("amount", rankUser.getScore().longValue());
	                    }
	                    roomList.add(obj);
					}
				}
				
				RoomInfoService roomInfoService = (RoomInfoService) MelotBeanFactory.getBean("roomInfoService");
	            if (roomInfoService != null) {
	                List<RoomInfo> roomInfoList = roomInfoService.getRoomListByRoomIds(ids);
	                if (roomInfoList != null && roomInfoList.size() > 0) {
	                    Map<Integer, RoomInfo> roomMap = new HashMap<Integer, RoomInfo>();
	                    for (RoomInfo roomInfo : roomInfoList) {
	                        roomMap.put(roomInfo.getActorId(), roomInfo);
	                    }
	                    if (roomMap.size() > 0) {
	                        for (JsonElement json : roomList) {
	                            JsonObject temp = json.getAsJsonObject();
	                            if (roomMap.containsKey(temp.get("userId").getAsInt())) {
	                                RoomInfo roomInfo = roomMap.get(temp.get("userId").getAsInt());
	                                if (roomInfo.getLiveStarttime() != null && roomInfo.getLiveEndtime() == null) {
	                                    temp.addProperty("liveType", 1);
	                                    temp.addProperty("livestarttime", roomInfo.getLiveStarttime().getTime());
	                                    temp.addProperty("onlineCount", roomInfo.getPeopleInRoom());
	                                } else {
	                                    temp.addProperty("liveType", 0);
	                                }
	                                if (roomInfo.getScreenType() != null) {
	                                    temp.addProperty("screenType", roomInfo.getScreenType());
	                                } else {
	                                    temp.addProperty("screenType", 1);
	                                }
	                                // 轮播房信息替换
	                                temp.addProperty("roomId", roomInfo.getRoomId() != null ? roomInfo.getRoomId() : roomInfo.getActorId());
	                                
	                                if (roomInfo.getType() != null) {
	                                	if (roomInfo.getRoomSource() != null) {
	                                		temp.addProperty("roomSource", roomInfo.getRoomSource());
										}else{
											temp.addProperty("roomSource", roomInfo.getType());
										}
	                                }
	                            }
	                        }
	                    }
	                }
	            }
	            result.add("roomList", roomList);
			}
		}
		
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
	}

	/**
	 * 获取活动列表(10002006)
	 * 
	 * @param jsonObject 请求对象
	 * @return 结果字符串
	 */
	public JsonObject getActivityList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
	    JsonObject result = new JsonObject();
	    
		int istop, platform, appId, channel;
		try {
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, 1, Integer.MAX_VALUE);
            istop = CommonUtil.getJsonParamInt(jsonObject, "isTop", 0, null, 0, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 0, Integer.MAX_VALUE);
            channel = CommonUtil.getJsonParamInt(jsonObject, "c", AppChannelEnum.KK, null, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
		
		// 调用存储过程得到结果
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("isTop", istop);
		map.put("platform", platform);
		map.put("appId", appId);
		map.put("channel", usePrivateChannel(channel) ? channel : 100);
		try {
		    SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Index.getActivityList", map);
		} catch (SQLException e) {
		    logger.error("未能正常调用存储过程", e);
		    result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
		    return result;
		}
		String TagCode = (String) map.get("TagCode");
		if (TagCode.equals(TagCodeEnum.SUCCESS)) {
		    result.addProperty("TagCode", TagCode);
		    
			// 取出列表
			@SuppressWarnings("unchecked")
			List<Object> activityList = (ArrayList<Object>) map.get("activityList");
			JsonArray jActivityList = new JsonArray();
			if (activityList != null && activityList.size() > 0) {
			    for (Object object : activityList) {
			        jActivityList.add(((Activity) object).toJsonObject(platform));
			    }
            }
			result.add("activityList", jActivityList);

			@SuppressWarnings("unchecked")
			List<HotActivity> hotActivityList = (ArrayList<HotActivity>) map.get("hotActivityList");
			JsonArray jHotActivityList = new JsonArray();
			if (hotActivityList != null && hotActivityList.size() > 0) {
			    for (HotActivity ha : hotActivityList) {
			        jHotActivityList.add(ha.toJsonObject(platform));
			    }
			}
			result.add("hotActivityList", jHotActivityList);
		} else {
			// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
			logger.error("调用存储过程(Index.getActivityList)未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
			result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
		}
		
		return result;
	}

	/**
	 * 获取全部活动列表(10002038)
	 * 
	 * @param jsonObject
	 * @return jsonObject
	 */
	public JsonObject getAllActivityList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
	    JsonObject result = new JsonObject();
        int type;
        int pageIndex;
        int countPerPage;

        try {
            type = CommonUtil.getJsonParamInt(jsonObject, "type", 0, null, -1, Integer.MAX_VALUE);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 10, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            KkActivityService kkActivityService = (KkActivityService) MelotBeanFactory .getBean("kkActivityService");
            int KkActivityListCount = kkActivityService.getKKActivityListCountByType(type, null, null);
            JsonArray activityList = new JsonArray();
            if (KkActivityListCount > 0) {
                List<KkActivity> KkActivityList = kkActivityService.getKKActivityByType(type, null, null, pageIndex, countPerPage);
                for (KkActivity kkActivity : KkActivityList) {
                    JsonObject jsonObj = new JsonObject();
                    jsonObj.addProperty("activityId", kkActivity.getActivityId());
                    if (kkActivity.getActivityURL() != null) {
                        jsonObj.addProperty("activityURL", kkActivity.getActivityURL());
                    }
                    if (kkActivity.getActTitle() != null) {
                        jsonObj.addProperty("activityTitle", kkActivity.getActTitle());
                    }
                    if (kkActivity.getActDesc() != null) {
                        jsonObj.addProperty("activityDesc", kkActivity.getActDesc() );
                    }
                    if (kkActivity.getStartTime() != null) {
                        jsonObj.addProperty("startDate", kkActivity.getStartTime().getTime());
                    }
                    if (kkActivity.getEndTime() != null) {
                        jsonObj.addProperty("endDate", kkActivity.getEndTime().getTime());
                    }
                    if (kkActivity.getImgURL() != null) {
                        jsonObj.addProperty("imgURL", kkActivity.getImgURL() );
                    }
                    activityList.add(jsonObj);
                }
            }
            result.add("activityList", activityList);
            result.addProperty("count", KkActivityListCount);
            result.addProperty("totalPage", KkActivityListCount%countPerPage == 0 ? KkActivityListCount/countPerPage : KkActivityListCount/countPerPage + 1);
            result.addProperty("pathPrefix", "");
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("KkActivityService.getKKActivityByType(" + type + ", " + null + ", " + null + ", " + pageIndex + ", " + countPerPage + ") return exception.", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }

        return result;		
	}
	
	/**
     *  60135: KK体育
     *  60138: KK女神计划 
     */
    private static int[]channels = new int[]{60135, 60138, 60149};

    /**
     *  选择公告, 活动是否启用针对渠道号私有 
     */
    private static boolean usePrivateChannel(int channel){
        for (int chan : channels) {
            if(channel == chan){
                return true;
            }
        }
        return false;
    }
    
	/**
	 * 获取通告列表(10002007)
	 * 
	 * @param jsonObject 请求对象
	 * @return 结果字符串
	 */
	@SuppressWarnings("unchecked")
    public JsonObject getNoticeList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		
		// define usable parameters
		int platform, appId, channel, start, offset;
		// parse the parameters
		JsonObject result = new JsonObject();
		try {
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, 1, Integer.MAX_VALUE);
			appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 1, Integer.MAX_VALUE);
			channel = CommonUtil.getJsonParamInt(jsonObject, "c", AppChannelEnum.KK, null, 1, Integer.MAX_VALUE);
			start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, null, 0, Integer.MAX_VALUE);
			offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 20, null, 0, Integer.MAX_VALUE);
		} catch(CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch(Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
        
		try {
		    // 调用存储过程得到结果
		    Map<Object, Object> map = new HashMap<Object, Object>();
		    map.put("platform", platform);
		    map.put("appId", appId);
		    map.put("channel", usePrivateChannel(channel) ? channel : 100);
		    map.put("start", start);
		    map.put("offset", offset);
		    SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Index.getNoticeList", map);
		    
			String TagCode = (String) map.get("TagCode");
			if (TagCode.equals(TagCodeEnum.SUCCESS)) {
				JsonArray jNoticeList = new JsonArray();
				Integer noticeTotal = (Integer) map.get("noticeTotal");
				if (noticeTotal != null && noticeTotal.intValue() > 0) {
					result.addProperty("noticeTotal", noticeTotal);
					// 取出列表
					List<Object> noticeList = (ArrayList<Object>) map.get("noticeList");
					for (Object object : noticeList) {
						jNoticeList.add(((Notice) object).toJsonObject());
					}
				} else {
					result.addProperty("noticeTotal", 0);
				}
				result.add("noticeList", jNoticeList);
				
				result.addProperty("TagCode", TagCode);
				
			} else {
				// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
				logger.error("调用存储过程(Index.getNoticeList)未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
				result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
			}
		} catch (SQLException e) {
			logger.error("未能正常调用存储过程", e);
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
		}
		
		return result;
	}

	/**
	 * 关键词搜索房间(接口10002008)
	 * 
	 * @param jsonObject 请求对象
	 * @return 结果字符串
	 */
	public JsonObject findRoomList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		JsonObject result = new JsonObject();
		JsonArray jRoomList = new JsonArray();
		int platform = 0;
		String fuzzyString = null;
		int pageNum, pageCount;
		try {
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
			fuzzyString = CommonUtil.getJsonParamString(jsonObject, "fuzzyString", null, null, 1, 30);
			pageNum = CommonUtil.getJsonParamInt(jsonObject, "pageNum", 0, null, 0, Integer.MAX_VALUE);
			pageCount = CommonUtil.getJsonParamInt(jsonObject, "pageCount", 0, null, 0, Integer.MAX_VALUE);
		} catch(CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch(Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		
		// 空搜索字符串返回空
		if (fuzzyString == null) {
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			result.add("roomList", jRoomList);
			result.addProperty("recordCount", 0);
			return result;
		}
		fuzzyString = fuzzyString.trim();
		
		// 判断是否为纯数字
		boolean isId = Pattern.compile(Constant.regx_user_id).matcher(fuzzyString).find();
		
		// 敏感字验证
		if (!isId && GeneralService.hasSensitiveWords(0, fuzzyString)) {
            result.addProperty("TagCode", TagCodeEnum.SENSITIVE_WORD_ERROR);
            return result;
        }

		// 如果缓存中没数据，先从oracle查询出全部数据，并添加至缓存
		if (!SearchWordsSource.isExistSearchResultKey(fuzzyString)) {
		    Map<Object, Object> map = new HashMap<Object, Object>();
		    if (isId) {
		        String idString = fuzzyString;
		        Integer tmpId = UserAssetServices.luckyIdToUserId(StringUtil.parseFromStr(idString, 0));
		        if (tmpId != null && tmpId > 0) {
		            idString = String.valueOf(tmpId);
		        }
		        map.put("fuzzyString", idString);
		    } else {
		        map.put("fuzzyString", fuzzyString);
		    }
		    try {
		        SqlMapClientHelper.getInstance(DB.BACKUP).queryForObject("Index.findRoomList", map);
		    } catch (SQLException e) {
		        logger.error("未能正常调用存储过程", e);
		        result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
		        return result;
		    }
		    List<String> newList = new ArrayList<String>();
		    String TagCode = (String) map.get("TagCode");
		    if (TagCode.equals(TagCodeEnum.SUCCESS)) {
		        // 取出列表
		        @SuppressWarnings("unchecked")
		        List<Room> roomList = (ArrayList<Room>) map.get("roomList");
		        List<RoomInfo> roomInfoList = null;
		        if (roomList != null && !roomList.isEmpty()) {
		            StringBuffer actorIds2 = new StringBuffer();
		            for (Room room : roomList) {
		                if (room != null && room.getActorTag() != null && room.getActorTag() == 1) {
		                    actorIds2.append(room.getUserId());
		                    actorIds2.append(",");
		                }
		            }
		            if (actorIds2.length() > 0) {
		                roomInfoList = RoomService.getRoomListByRoomIds(actorIds2.substring(0, actorIds2.length() - 1));
		            }
		            
		            for (Room room : roomList) {
		                if (room != null) {
		                    int roomId = room.getUserId();
		                    if (room.getActorTag() != null && room.getActorTag() == 1
		                            && roomInfoList != null && !roomInfoList.isEmpty()) {
		                        boolean include = false;
		                        for (RoomInfo rinfo : roomInfoList) {
		                            if (rinfo.getActorId() != null && rinfo.getActorId().intValue() == roomId) {
		                                newList.add(new Gson().toJson(RoomTF.roomInfoToJson(rinfo, platform, true)));
		                                include = true;
		                            }
		                        }
		                        if (!include) {
		                            newList.add(new Gson().toJson(room.toJsonObject(platform, null)));
		                        }
		                    } else {
		                        newList.add(new Gson().toJson(room.toJsonObject(platform, null)));
		                    }
		                }
		            }
		        }
		        
		        if (!newList.isEmpty()) {
		            if(!SearchWordsSource.setSearchResultPage(fuzzyString, newList)){
		                logger.error("SearchWordsSource.setSearchResult Fail to add" + fuzzyString + "searchResult to redis");
		            }
                } else {
                    result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                    result.addProperty("recordCount", 0);
                    result.add("roomList", jRoomList);
                    return result;
                }
		    } else {
		        // 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
		        logger.error("调用存储过程(Index.findRoomList)未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
		        result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
                return result;
		    }
        }
        
        long start, end;
        long recordCount = SearchWordsSource.getSearchResultPageCount(fuzzyString);
        if (recordCount > 0) {
            // pageNum和pageCount未传入，查询全部
            if (pageNum == 0 || pageCount == 0) {
                start = 0;
                end = recordCount - 1;
            } else {
                start = (pageNum - 1) * pageCount;
                end = pageNum * pageCount - 1;
            }
            Set<String> tempSet = SearchWordsSource.getSearchResultPage(fuzzyString, start, end);
            if (tempSet != null && !tempSet.isEmpty()) {
                for (String tempStr : tempSet) {
                    JsonObject jObject = new JsonParser().parse(tempStr).getAsJsonObject();
                    if (jObject != null) {
                        jRoomList.add(jObject);
                    }
                }
                
                // 搜索成功后更新关键字搜索次数
                if (pageNum ==0 || pageNum == 1) {
                    if (!SearchWordsSource.incScore(fuzzyString)) {
                        logger.error("SearchWordsSource.incScore Fail to increase score " + fuzzyString);
                    }
                }
            }
        }
        
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        result.addProperty("recordCount", recordCount);
        result.add("roomList", jRoomList);
		
        // 返回结果
        return result;
	}
	
	/**
	 * 获取公告详细(10002012)
	 * 
	 * @param paramJsonObject
	 * @return
	 */
	public JsonObject getNoticeDetail(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
		
		int noticeId = 0;
		JsonElement noticeIdje = paramJsonObject.get("noticeId");
		if (noticeIdje != null) {
			try {
				noticeId = Integer.parseInt(noticeIdje.getAsString());
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "02130002");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "02130001");
			return result;
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("noticeId", noticeId);
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Index.getNoticeDetail", map);
		} catch (Exception e) {
			logger.error("未能正常调用存储过程", e);
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			return result;
		}

		String tagCode = map.get("TagCode").toString();
		if (tagCode.equals(TagCodeEnum.SUCCESS)) {
			String title = map.get("title") == null ? "" : map.get("title").toString();
			String noticeUrl = map.get("noticeURL") == null ? "" : map.get("noticeURL").toString();
			String content = map.get("content") == null ? "" : map.get("content").toString();

			Notice notice = new Notice();
			notice.setNoticeId(noticeId);
			notice.setTitle(title);
			notice.setNoticeURL(noticeUrl);
			notice.setDtime((Date) map.get("dtime"));
			notice.setContent(content);
			JsonObject result = notice.toJsonObject();
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			return result;
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "021301" + tagCode);
			return result;
		}
	}

	/**
	 * 获取活动详细(10002013)
	 * 
	 * @param paramJsonObject
	 * @return
	 */
	public JsonObject getActivityDetail(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		int activityid = 0;
		JsonElement activityIdje = jsonObject.get("activityId");
		if (activityIdje != null) {
			try {
				activityid = Integer.parseInt(activityIdje.getAsString());
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "02120002");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "02120001");
			return result;
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("activityId", activityid);
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Index.getActivityDetail", map);
		} catch (Exception e) {
			logger.error("未能正常调用存储过程", e);
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			return result;
		}

		String tagCode = map.get("TagCode").toString();
		if (tagCode.equals(TagCodeEnum.SUCCESS)) {
			String imgUrl = map.get("imgURL") == null ? "" : map.get("imgURL").toString();
			String activityUrl = map.get("activityURL") == null ? "" : map.get("activityURL").toString();
			String content = map.get("content") == null ? "" : map.get("content").toString();
			String topUrl = map.get("topURL") == null ? "" : map.get("topURL").toString();
			String topMobileUrl = map.get("topMobileURL") == null ? "" : map.get("topMobileURL").toString();
			String topMobileUrlIOS = map.get("topMobileURLIOS") == null ? "" : map.get("topMobileURLIOS").toString();

			Activity activity = new Activity();
			activity.setActivityId(activityid);
			activity.setImgURL(imgUrl);
			activity.setActivityURL(activityUrl);
			activity.setDtime((Date) map.get("dtime"));
			activity.setContent(content);
			activity.setTopURL(topUrl);
			activity.setTopMobileURL(topMobileUrl);
			activity.setTopMobileURLIOS(topMobileUrlIOS);
			JsonObject result = activity.toJsonObject(1); // 返回详细内容,无需图片
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			return result;
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "021201" + tagCode);
			return result;
		}
	}

	/**
	 * 获取推荐的直播房间列表(10002014)
	 * 
	 * @return 结果字符串
	 */
    public JsonObject getLiveRecommendedList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		JsonObject result = new JsonObject();
		int platform = 0;
		int appId = 0;
		Integer roomId;
		Integer userId;
		int count = 0;
		try {
			// 默认为1
			appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 0, Integer.MAX_VALUE);
			roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, null, 1, Integer.MAX_VALUE);
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
			// 默认为1
			count = CommonUtil.getJsonParamInt(jsonObject, "count", 1, null, 1, Integer.MAX_VALUE);
		} catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch (Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		
		if(roomId == 0) {
			roomId = null;
		}
		if(userId == 0) {
			userId = null;
		}
		
		List<RoomInfo> roomList = null;
		try {
			FirstPageHandler firstPageHandler = MelotBeanFactory.getBean("firstPageHandler", FirstPageHandler.class);
			roomList = firstPageHandler.getRecommendRooms(roomId, userId, appId, count);
		} catch(Exception e) {
			logger.error("Fail to call firstPageHandler.getRecommendRooms ", e);
		}
		if (roomList != null) {
			JsonArray roomArray = new JsonArray();
			for(RoomInfo roomInfo : roomList) {
				roomArray.add(RoomTF.roomInfoToJson(roomInfo, platform, false));
			}
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
			result.add("roomList", roomArray);
		} else {
			result.addProperty("TagCode", TagCodeEnum.FAIL_TO_CALL_API_MENU_MODULE);
		}
		return result;
	}
	
	/**
	 * 获取房间分类信息
	 * 台湾版本在使用
	 *
	 * @return 直接返回如下
	 * {
			TagCode: "00000000",
			catalogInfo: [
				{
				  defaultTag: "全部"
				}
	        ]
	    }
	 */
	public JsonObject getCatalogInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		JsonObject result = new JsonObject();
		JsonArray catalogInfo = new JsonArray();
		JsonObject catalogObj = new JsonObject();
		catalogObj.addProperty("defaultTag", "全部");
		catalogInfo.add(catalogObj);
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		result.add("catalogInfo", catalogInfo);
		return result;
	}
	
	/**
	 * 获取本周礼物消耗排行榜(10002011)
	 * @param jsonObject
	 * @return
	 */
	public JsonObject getWeeklyGiftConsumeRankList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		JsonObject result = new JsonObject();
		
		JsonArray arr = new JsonArray();
		Map<Integer, JsonObject> allJsonMap = new HashMap<>();
		Map<Integer, JsonObject> liveJsonMap = new HashMap<>();
		List<WeekStarGift> weekStarGiftList = ActorGiftService.getWeekStarGiftList(new Date(DateUtil.getWeekBeginTime(System.currentTimeMillis() + RankingEnum.THIS_WEEK_GIFT_RANKING*7*24*3600*1000)));
		if (weekStarGiftList != null && !weekStarGiftList.isEmpty()) {
			int userId;
			JsonObject weeklyGiftJson = null;
			
			//获取正在直播的actorId
			String liveActorStr = KKHallSource.getTempDataString(KK_LIVE_ACTOR_ID);
			List<Integer> liveActorList = new ArrayList<>();
			if (liveActorStr != null) {
				String[] liveActor = liveActorStr.split(",");
				for (String actorId : liveActor) {
					liveActorList.add(Integer.valueOf(actorId));
				}
			}
			
			Integer giftId, relationGiftId, singlePrice;
			String giftName;
			Long weekTime;
			Map<Integer, Long> giftRankMap;
			for (WeekStarGift weekStarGift : weekStarGiftList) {
				giftId = weekStarGift.getGiftId();
				relationGiftId = weekStarGift.getRelationGiftId();
				giftName = weekStarGift.getGiftName();
				singlePrice = GiftInfoConfig.getGiftSendPrice(giftId);
				weekTime = weekStarGift.getStarttime().getTime();
				giftRankMap = WeekGiftSource.getWeekGiftRank(String.valueOf(weekTime), relationGiftId != null && relationGiftId > 0 ? giftId + "_" + relationGiftId : String.valueOf(giftId), 3);
				if (giftRankMap != null) {
					for (Entry<Integer, Long> entry : giftRankMap.entrySet()) {
						weeklyGiftJson = new JsonObject();
						userId = entry.getKey();
						weeklyGiftJson.addProperty("giftName", giftName);
						weeklyGiftJson.addProperty("giftCount", entry.getValue());
						weeklyGiftJson.addProperty("giftId", giftId);
						weeklyGiftJson.addProperty("giftWorth", entry.getValue() * singlePrice);
						weeklyGiftJson.addProperty("giftPic", ConfigHelper.getKkDomain() + "/icon/web/gift/png/" + giftId + ".png");
						if (liveActorList.contains(userId)) {
							//正在直播
							if (!liveJsonMap.containsKey(userId) || (liveJsonMap.containsKey(userId) && liveJsonMap.get(userId).get("giftWorth").getAsLong() < entry.getValue() * singlePrice)) {
								liveJsonMap.put(userId, weeklyGiftJson);
							}
						} else {
							//未直播
							if (!allJsonMap.containsKey(userId) || (allJsonMap.containsKey(userId) && allJsonMap.get(userId).get("giftWorth").getAsLong() < entry.getValue() * singlePrice)) {
								allJsonMap.put(userId, weeklyGiftJson);
							}
						}
					}
				}
			}
			
			RoomInfo roomInfo;
			JsonObject finalJson;
			List<Map.Entry<Integer, JsonObject>> sortList = new ArrayList<>(liveJsonMap.entrySet());
			if (!sortList.isEmpty()) {
				Collections.sort(sortList, new Comparator<Map.Entry<Integer, JsonObject>>() {
					@Override
					public int compare(Entry<Integer, JsonObject> o1, Entry<Integer, JsonObject> o2) {
						return new Long(o2.getValue().get("giftWorth").getAsLong()).compareTo(new Long(o1.getValue().get("giftWorth").getAsLong()));
					}
				});
				for (Map.Entry<Integer, JsonObject> entry : sortList) {
					roomInfo = RoomService.getRoomInfo(entry.getKey());
					if (roomInfo != null) {
						finalJson = RoomTF.roomInfoToJson(roomInfo, 1, true);
						if (roomInfo.getNickname() != null) {
							finalJson.addProperty("nickName", roomInfo.getNickname());
						}
						finalJson.addProperty("giftName", entry.getValue().get("giftName").getAsString());
						finalJson.addProperty("giftCount", entry.getValue().get("giftCount").getAsNumber());
						finalJson.addProperty("giftId", entry.getValue().get("giftId").getAsNumber());
						finalJson.addProperty("giftWorth", entry.getValue().get("giftWorth").getAsNumber());
						finalJson.addProperty("giftPic", entry.getValue().get("giftPic").getAsString());
						arr.add(finalJson);
					}
					if (arr.size() >= 3) break;
				}
			}
			
			if (arr.size() < 3) {
				List<Map.Entry<Integer, JsonObject>> allSortList = new ArrayList<>(allJsonMap.entrySet());
				Collections.sort(allSortList, new Comparator<Map.Entry<Integer, JsonObject>>() {
					@Override
					public int compare(Entry<Integer, JsonObject> o1, Entry<Integer, JsonObject> o2) {
						return new Long(o2.getValue().get("giftWorth").getAsLong()).compareTo(new Long(o1.getValue().get("giftWorth").getAsLong()));
					}
				});
				for (Map.Entry<Integer, JsonObject> entry : allSortList) {
					if (!liveJsonMap.containsKey(entry.getKey()) && liveJsonMap.get(entry.getKey()) == null) {
						roomInfo = RoomService.getRoomInfo(entry.getKey());
						if (roomInfo != null) {
							finalJson = RoomTF.roomInfoToJson(roomInfo, 1, true);
							if (roomInfo.getNickname() != null) {
								finalJson.addProperty("nickName", roomInfo.getNickname());
							}
							finalJson.addProperty("giftName", entry.getValue().get("giftName").getAsString());
							finalJson.addProperty("giftCount", entry.getValue().get("giftCount").getAsNumber());
							finalJson.addProperty("giftId", entry.getValue().get("giftId").getAsNumber());
							finalJson.addProperty("giftWorth", entry.getValue().get("giftWorth").getAsNumber());
							finalJson.addProperty("giftPic", entry.getValue().get("giftPic").getAsString());
							arr.add(finalJson);
						}
					}
					if (arr.size() >= 3) break;
				}
			}
		}
		
		result.add("rankList", arr);
		result.addProperty("position", ConfigHelper.getGiftRankPosition());
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
	}
	
	/**
	 * 获取用户本周上榜礼物列表(10005025)
	 * 
	 * @param jsonObject 请求对象
	 * @return 结果字符串
	 */
	@Deprecated
	public JsonObject getUserInGiftRankingList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        int userId, platform;
		
		try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
		
		JsonArray jUserGiftRankingList = new JsonArray();
		List<WeekStarGift> weekStarGiftList = ActorGiftService.getWeekStarGiftList(new Date(DateUtil.getWeekBeginTime(System.currentTimeMillis() + RankingEnum.THIS_WEEK_GIFT_RANKING*7*24*3600*1000)));
		if (weekStarGiftList != null && !weekStarGiftList.isEmpty()) {
		    Integer giftId, relationGiftId;
		    String giftName;
		    Long weekTime;
		    Map<String, Object> rankInfo;
			for (WeekStarGift weekStarGift : weekStarGiftList) {
				giftId = weekStarGift.getGiftId();
				relationGiftId = weekStarGift.getRelationGiftId();
				giftName = weekStarGift.getGiftName();
				weekTime = weekStarGift.getStarttime().getTime();
				rankInfo = WeekGiftSource.getWeekGiftRank(String.valueOf(weekTime), relationGiftId != null && relationGiftId > 0 ? giftId + "_" + relationGiftId : String.valueOf(giftId), String.valueOf(userId));
				if (rankInfo != null) {
					JsonArray arr = new JsonArray();
					JsonObject jsonObj = new JsonObject();
					Integer singlePrice = GiftInfoConfig.getGiftSendPrice(giftId);
					if (singlePrice != null) {
						jsonObj.addProperty("singlePrice", singlePrice);
					}
					jsonObj.addProperty("giftId", giftId);
					jsonObj.addProperty("giftName", giftName);
//					jsonObj.addProperty("giftLevel", giftLevel);
					jsonObj.addProperty("ranking", (Long) rankInfo.get("rank"));
					jsonObj.addProperty("total", (Long) rankInfo.get("total"));
					jsonObj.addProperty("userId", userId);
					jsonObj.addProperty("needMore", (Long) rankInfo.get("diff"));
					jsonObj.addProperty("upDiff", (Long) rankInfo.get("diff"));
					jsonObj.addProperty("downDiff", (Long) rankInfo.get("downDiff"));
					if (rankInfo.containsKey("upUid") && rankInfo.get("upUid") != null) {
						Integer upUidInt = Integer.valueOf(rankInfo.get("upUid").toString());
						jsonObj.addProperty("upUid", upUidInt);
						// 从redis热点数据中读取昵称
						String nickname = HotDataSource.getHotFieldValue(upUidInt.toString(), "nickname");
						if (nickname != null) jsonObj.addProperty("upNick", GeneralService.replaceSensitiveWords(upUidInt, nickname));
					}
					if (rankInfo.containsKey("downUid") && rankInfo.get("downUid")!=null) {
						Integer downUidInt = Integer.valueOf(rankInfo.get("downUid").toString());
						jsonObj.addProperty("downUid", downUidInt);
						// 从redis热点数据中读取昵称
						String nickname = HotDataSource.getHotFieldValue(downUidInt.toString(), "nickname");
						if (nickname != null) jsonObj.addProperty("downNick", GeneralService.replaceSensitiveWords(downUidInt, nickname));
					}
					for (int i = 1; i < 4; i++) {
						JsonObject json;
						if (rankInfo.containsKey("userId." + i) && rankInfo.get("userId." + i) != null) {
							Integer topUserId = (Integer) rankInfo.get("userId." + i);
							RoomInfo roomInfo = RoomService.getRoomInfo(topUserId);
							if (roomInfo != null) {
							    json = RoomTF.roomInfoToJson(roomInfo, platform, true);
								json.addProperty("ranking", i);
								json.addProperty("total", (Long) rankInfo.get("total." + i));
								json.addProperty("userId", topUserId);
								arr.add(json);
							}
						}
					}
					jsonObj.add("topGiftList", arr);
					// 以下接口用于支持mobile
					if (platform == PlatformEnum.ANDROID) {
						jsonObj.addProperty("androidIcon", ConfigHelper.getGiftIconAndroidResURL() + giftId + ".png");
					}
					if (platform == PlatformEnum.IPHONE) {
						jsonObj.addProperty("iphoneIcon", ConfigHelper.getGiftIconIphoneResURL() + giftId + ".png");
					}
					if (platform == PlatformEnum.IPAD) {
						jsonObj.addProperty("iphoneIcon", ConfigHelper.getGiftIconIphoneResURL() + giftId + ".png");
					}
					// 默认为kk唱响用户
					jsonObj.addProperty("roomSource", AppIdEnum.AMUSEMENT);
					jUserGiftRankingList.add(jsonObj);
				}
			}
		}
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		result.add("userGiftRankingList", jUserGiftRankingList);
		return result;
	}
	
	/**
	 * 获取新版主播周星争夺战(10002074)
	 * @param jsonObject
	 * @param checkTag
	 * @param request
	 * @return
	 */
	public JsonObject getNewActorWeeklyGiftReport(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		JsonObject result = new JsonObject();
		int platform;
		
		try {
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
		
		//获取正在直播的actorId
		String liveActorStr = KKHallSource.getTempDataString(KK_LIVE_ACTOR_ID);
		List<Integer> liveActorList = new ArrayList<>();
		if (liveActorStr != null) {
			String[] liveActor = liveActorStr.split(",");
			for (String actorId : liveActor) {
				liveActorList.add(Integer.valueOf(actorId));
			}
		}
		
		JsonArray rankList = new JsonArray();
		Map<Integer, JsonObject> giftMap = new HashMap<>();
		Map<Integer, JsonObject> allGiftMap = new HashMap<>();
		List<WeekStarGift> weekStarGiftList = ActorGiftService.getWeekStarGiftList(new Date(DateUtil.getWeekBeginTime(System.currentTimeMillis() + RankingEnum.THIS_WEEK_GIFT_RANKING*7*24*3600*1000)));
		if (weekStarGiftList != null && !weekStarGiftList.isEmpty()) {
		    Integer giftId, singlePrice, relationGiftId;
		    Long weekTime, tempValue;
		    String giftName;
		    Map<Integer, Long> giftRankMap;
		    int tempId;
		    long giftWorth;
		    boolean flag;
		    JsonObject tempJson;
		    List<Integer> filterGiftList = new ArrayList<Integer>();
			for (WeekStarGift weekStarGift : weekStarGiftList) {
				giftId = weekStarGift.getGiftId();
				relationGiftId = weekStarGift.getRelationGiftId();
				giftName = weekStarGift.getGiftName();
				flag = true;
				singlePrice = GiftInfoConfig.getGiftSendPrice(giftId);
				weekTime = weekStarGift.getStarttime().getTime();
				giftRankMap = WeekGiftSource.getWeekGiftRank(String.valueOf(weekTime), relationGiftId != null && relationGiftId > 0 ? giftId + "_" + relationGiftId : String.valueOf(giftId), 3);
				if (giftRankMap != null && !giftRankMap.isEmpty()) {
					for (Entry<Integer, Long> entry : giftRankMap.entrySet()) {
						tempId = entry.getKey();
						tempValue = entry.getValue();
						giftWorth = tempValue * singlePrice;
						if (liveActorList.contains(tempId)) {
							if (!giftMap.containsKey(tempId) || (giftMap.containsKey(tempId) && giftMap.get(tempId).get("giftWorth").getAsLong() < giftWorth)) {
								tempJson = new JsonObject();
								tempJson.addProperty("giftCount", tempValue);
								tempJson.addProperty("giftName", giftName);
								tempJson.addProperty("giftId", giftId);
								tempJson.addProperty("giftWorth", giftWorth);
								tempJson.addProperty("giftPic", ConfigHelper.getKkDomain() + "/icon/web/gift/png/" + giftId + ".png");
								giftMap.put(tempId, tempJson);
								filterGiftList.add(giftId);
								break;
							}
						} else if (flag && !filterGiftList.contains(giftId) && !giftMap.containsKey(tempId) && (!allGiftMap.containsKey(tempId) || (allGiftMap.containsKey(tempId) && allGiftMap.get(tempId).get("giftWorth").getAsLong() < giftWorth))) {
							tempJson = new JsonObject();
							tempJson.addProperty("giftCount", tempValue);
							tempJson.addProperty("giftName", giftName);
							tempJson.addProperty("giftId", giftId);
							tempJson.addProperty("giftWorth", giftWorth);
							tempJson.addProperty("giftPic", ConfigHelper.getKkDomain() + "/icon/web/gift/png/" + giftId + ".png");
							allGiftMap.put(tempId, tempJson);
							flag = false;
						}
					}
				}
			}
			
			JsonObject finalJson = new JsonObject();
			if (giftMap != null && giftMap.size() > 0) {
				List<Map.Entry<Integer, JsonObject>> actorSortList = new ArrayList<>(giftMap.entrySet());
				Collections.sort(actorSortList, new Comparator<Map.Entry<Integer, JsonObject>>() {
					@Override
					public int compare(Entry<Integer, JsonObject> o1, Entry<Integer, JsonObject> o2) {
						return Long.valueOf(o2.getValue().get("giftWorth").getAsLong()).compareTo(Long.valueOf(o1.getValue().get("giftWorth").getAsLong()));
					}
				});
				
				for (Map.Entry<Integer, JsonObject> entry : actorSortList) {
					finalJson = entry.getValue();
					finalJson.add("actor", RoomTF.roomInfoToJson(RoomService.getRoomInfo(entry.getKey()), platform, true));
					rankList.add(finalJson);
					if (rankList.size() >= 6) {
						break;
					}
				}
			}
			if (rankList.size() < 6) {
				List<Map.Entry<Integer, JsonObject>> allActorSortList = new ArrayList<>(allGiftMap.entrySet());
				Collections.sort(allActorSortList, new Comparator<Map.Entry<Integer, JsonObject>>() {
					@Override
					public int compare(Entry<Integer, JsonObject> o1, Entry<Integer, JsonObject> o2) {
						return Long.valueOf(o2.getValue().get("giftWorth").getAsLong()).compareTo(Long.valueOf(o1.getValue().get("giftWorth").getAsLong()));
					}
				});
				for (Map.Entry<Integer, JsonObject> entry : allActorSortList) {
					finalJson = entry.getValue();
					if (!filterGiftList.contains(finalJson.get("giftId").getAsInt())) {
						finalJson.add("actor", RoomTF.roomInfoToJson(RoomService.getRoomInfo(entry.getKey()), platform, true));
						rankList.add(finalJson);
						if (rankList.size() >= 6) {
							break;
						}
					}
				}
			}
		}
		
		result.add("rankList", rankList);
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
	}
	
	/**
	 * 获取周星主播和用户前三名排行榜(10002073)
	 * @param jsonObject
	 * @param checkTag
	 * @param request
	 * @return
	 */
	public JsonObject getWeekGiftWholeTopThreeRanking(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		JsonObject result = new JsonObject();
		int type, platform, start, offset;
		
		try {
			type = CommonUtil.getJsonParamInt(jsonObject, "type", 1, null, 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, 1, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, "start", 1, null, 1, Integer.MAX_VALUE);
            offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 10, null, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
		
		JsonArray rankList = new JsonArray();
		Map<Integer, JsonObject> giftJson = new HashMap<>();
		Map<Integer, Long> topActorMap = new HashMap<>();
		type = type == 1 ? 0 : -1;
		int count = (int) WeekGiftSource.getWeekGiftRankListCount(type);
		if (count <= 0) {
		    List<WeekStarGift> weekStarGiftList = ActorGiftService.getWeekStarGiftList(new Date(DateUtil.getWeekBeginTime(System.currentTimeMillis() + type*7*24*3600*1000)));
			if (weekStarGiftList != null && !weekStarGiftList.isEmpty()) {
			    Integer giftId, relationGiftId, singlePrice;
			    String giftName;
			    JsonObject wholeJson;
			    Long weekTime;
			    Map<Integer, Long> actorGiftRankMap;
				for (WeekStarGift weekStarGift : weekStarGiftList) {
					giftId = weekStarGift.getGiftId();
					relationGiftId = weekStarGift.getRelationGiftId();
					giftName = weekStarGift.getGiftName();
					wholeJson = new JsonObject();
					weekTime = weekStarGift.getStarttime().getTime();
					singlePrice = GiftInfoConfig.getGiftSendPrice(giftId);
					wholeJson.addProperty("giftId", giftId);
					wholeJson.addProperty("giftName", giftName);
					wholeJson.addProperty("giftPrice", singlePrice);
					wholeJson.addProperty("giftPic", ConfigHelper.getKkDomain() + "/icon/android/gift/icon/" + giftId + ".png");
					actorGiftRankMap = WeekGiftSource.getWeekGiftRank(String.valueOf(weekTime), relationGiftId != null && relationGiftId > 0 ? giftId + "_" + relationGiftId : String.valueOf(giftId), 3);
					if (actorGiftRankMap != null && !actorGiftRankMap.isEmpty()) {
						JsonArray actorArray = new JsonArray();
						JsonObject actorJson;
						for (Entry<Integer, Long> entry : actorGiftRankMap.entrySet()) {
							actorJson = new JsonObject();
							if (!topActorMap.containsKey(giftId) && topActorMap.get(giftId) == null) {
								topActorMap.put(giftId, entry.getValue() * singlePrice);
							}
							RoomInfo roomInfo = RoomService.getRoomInfo(entry.getKey());
							if (roomInfo != null) {
								actorJson = RoomTF.roomInfoToJson(roomInfo, platform, true);
								actorJson.addProperty("giftCount", entry.getValue());
								actorArray.add(actorJson);
							}
						}
						if (actorArray.size() > 0) {
							wholeJson.add("actorRankList", actorArray);
						}

						Map<Integer, Long> userGiftRankMap = WeekGiftSource.getUserWeekGiftRank(String.valueOf(weekTime), relationGiftId != null && relationGiftId > 0 ? giftId + "_" + relationGiftId : String.valueOf(giftId), 3);
						if (userGiftRankMap != null && !userGiftRankMap.isEmpty()) {

                            List<Integer> userIds  = Lists.newArrayList();
                            for (Entry<Integer, Long> entry : userGiftRankMap.entrySet()) {
                                if(entry.getKey() != null) {
                                    userIds.add(entry.getKey());
                                }
                            }

                            // 获取用户信息列表
                            KkUserService kkUserService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
                            List<UserProfile> userProfiles = kkUserService.getUserProfileBatch(userIds);
                            Map<Integer, UserProfile> userProfileMap = Maps.newHashMap();
                            if (userProfiles != null) {
                                for (UserProfile userProfile : userProfiles) {
                                    userProfileMap.put(userProfile.getUserId(), userProfile);
                                }
                            }

						    JsonArray userArray = new JsonArray();
						    for (Entry<Integer, Long> entry : userGiftRankMap.entrySet()) {
						        JsonObject userJson = new JsonObject();

						        UserProfile userProfile = userProfileMap.get(entry.getKey());
						        if (userProfile != null) {
						            userJson.addProperty("userId", entry.getKey());
						            userJson.addProperty("giftCount", entry.getValue());
						            if (userProfile.getNickName() != null) {
						                userJson.addProperty("nickname", userProfile.getNickName());
						            }
						            userJson.addProperty("richLevel", userProfile.getUserLevel());
						            String portraitAddress;
						            if (userProfile.getPortrait() != null) {
						                portraitAddress = userProfile.getPortrait().startsWith("http://") ? 
						                        userProfile.getPortrait() : ConfigHelper.getHttpdir() + userProfile.getPortrait();
						            } else {
						                portraitAddress = ConfigHelper.getHttpdir() + ConstantEnum.DEFAULT_PORTRAIT_USER;
						            }
						            userJson.addProperty("portrait_path_original", portraitAddress);
						            userJson.addProperty("portrait_path_48", portraitAddress + "!48");
						            userJson.addProperty("portrait_path_128", portraitAddress + "!128");
						            userJson.addProperty("portrait_path_256", portraitAddress + "!256");
						            userJson.addProperty("portrait_path_272", portraitAddress + "!272");
						            userJson.addProperty("portrait_path_1280", portraitAddress + "!1280");
						            userJson.addProperty("portrait_path_400", portraitAddress + "!400");
						            userJson.addProperty("portrait_path_756", portraitAddress + "!756x567");
						            userArray.add(userJson);
						        }
						    }
						    if (userArray.size() > 0) {
						        wholeJson.add("userRankList", userArray);
						    }
						}
					}
					if ((wholeJson.has("actorRankList") && wholeJson.get("actorRankList") != null) || (wholeJson.has("userRankList") && wholeJson.get("userRankList") != null)) {
						giftJson.put(giftId, wholeJson);
					}
				}
				
				List<Map.Entry<Integer, Long>> sortList = new ArrayList<>(topActorMap.entrySet());
				Collections.sort(sortList, new Comparator<Map.Entry<Integer, Long>>() {
					@Override
					public int compare(Entry<Integer, Long> o1, Entry<Integer, Long> o2) {
						return o2.getValue().compareTo(o1.getValue());
					}
				});
				List<String> valueList = new ArrayList<>();
				for (Entry<Integer, Long> entry : sortList) {
					if (giftJson.containsKey(entry.getKey())) {
						valueList.add(giftJson.get(entry.getKey()).toString());
					}
				}
				count = valueList.size();
				String[] value = new String[valueList.size()];
				value = valueList.toArray(new String[0]);
				if (value != null && value.length > 0) {
					WeekGiftSource.addWeekGiftRankList(value, type);
				}
			}
		}
		
		List<String> rankListStr = WeekGiftSource.getWeekGiftRankList(start, offset, type);
		if (rankListStr != null && !rankListStr.isEmpty()) {
		    JsonParser jsonParser = new JsonParser();
			for (String rank : rankListStr) {
				rankList.add(jsonParser.parse(rank).getAsJsonObject());
			}
		}
		
		result.add("rankList", rankList);
		result.addProperty("totalCount", count);
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
	}
	
	/**
	 * 获取推荐节目列表接口(10002030)
	 * 默认返回当天所有节目
	 * @param jsonObject
	 * @return
	 */
	public JsonObject getPreviewActList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		JsonObject result = new JsonObject();
		
		int platform, count, isHall, userId, appId;
		long startTime, endTime;
		try {
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
			count = CommonUtil.getJsonParamInt(jsonObject, "count", -1, null, -1, Integer.MAX_VALUE);
			isHall = CommonUtil.getJsonParamInt(jsonObject, "isHall", 0, null, 0, 1);
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 0, Integer.MAX_VALUE);
			appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 0, Integer.MAX_VALUE);
			startTime = CommonUtil.getJsonParamLong(jsonObject, "startTime", System.currentTimeMillis(), null, System.currentTimeMillis(), Long.MAX_VALUE);
			endTime = CommonUtil.getJsonParamLong(jsonObject, "endTime", startTime + 604800000, null, 0l, startTime + 604800000);
		} catch (ErrorGetParameterException e) {
			result.add("actList", new JsonArray());
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			return result;
		}

        if (startTime > endTime) {
            result.add("actList", new JsonArray());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        }
        
        try {
            KkActivityService kkActivityService = (KkActivityService) MelotBeanFactory.getBean("kkActivityService");
            List<PreviewAct> list= new ArrayList<PreviewAct>();
            List<ActInfo> actInfoList;
            if (appId == AppIdEnum.AMUSEMENT) {
                actInfoList = kkActivityService.getRoomPreviewActList(0, isHall, new Date(startTime), new Date(endTime), null);
                if (actInfoList != null && actInfoList.size() > 0) {
                    getPreviewActList(platform, isHall, userId, DateUtil.getDayBeginTime(System.currentTimeMillis()), actInfoList, list, null, startTime, endTime);
                }
                
                Collections.sort(list,new Comparator<PreviewAct>() {
                     @Override
                     public int compare(PreviewAct pva1, PreviewAct pva2) {
                         return (int) (pva1.getStartTime() - pva2.getStartTime());
                     }
                });
                
                List<PreviewAct> newPList = new ArrayList<PreviewAct>();
	            Map<Integer,Integer> map = new HashMap<Integer,Integer>();
	            Map<String,Integer> map2 = new HashMap<String,Integer>();
	            
	            for(int i = 0; i < list.size(); i++){
	                int aid = ((PreviewAct)(list.get(i))).getActId();
	                long stime = ((PreviewAct)(list.get(i))).getStartTime();
	                //同一Id最多重复两次
	                //过滤当前分页的重复选项（同一活动的ID选项最多只能重复2次）
	                if(map2.containsKey(aid+"_"+stime))continue;
	                if(!map.containsKey(aid)){
	                    map.put(aid, 1);
	                    map2.put(aid+"_"+stime,1);
	                    newPList.add(list.get(i));
	                }else if(map.get(aid) == 1){
	                    continue;
	                }else{
	                    map.put(aid, 1);
	                    newPList.add(list.get(i));
	                }
	            }
                
                count = Math.min(count, newPList.size());
                result.add("actList", new JsonParser().parse(new Gson().toJson(count > 0 ? newPList.subList(0, count) : newPList)));
            } else {
                result.add("actList", new JsonArray());
            }
            
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch(Exception e) {
            logger.error("KkActivityService.getRoomPreviewActList return exception.", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
		// 返回结果
		return result;
	}

    /**
     * @param platform
     * @param isHall
     * @param userId
     * @param parseTime
     * @param actInfoList
     * @param list
     * @param repeatAIds
     */
    private void getPreviewActList(int platform, int isHall, int userId, long parseTime, List<ActInfo> actInfoList, List<PreviewAct> list, List<Integer> repeatAIds, long startTime, long endTime) {
        for (ActInfo actInfo : actInfoList) {
            JsonObject actcofig = PreviewAct.toJsonObject(actInfo, isHall, platform, parseTime, startTime, endTime);
            if (actcofig != null) {
                Integer aid = actInfo.getActId();
                
                // 从pg读取房间信息
                int actRoom = Integer.parseInt(actInfo.getActRoom());
                RoomInfo roomInfo = RoomService.getRoomInfo(actRoom);
                if (roomInfo != null) {
                    if (roomInfo.getRoomMode() != null) {
                        actcofig.addProperty("roomMode", roomInfo.getRoomMode());
                    }
                    if (roomInfo.getRoomSource() != null) {
                        actcofig.addProperty("roomSource", roomInfo.getRoomSource());
                    } else {
                        actcofig.addProperty("roomSource", AppIdEnum.AMUSEMENT);
                    }
                    if (roomInfo.getType() != null) {
                        actcofig.addProperty("roomType", roomInfo.getType());
                    } else {
                        actcofig.addProperty("roomType", AppIdEnum.AMUSEMENT);
                    }
                } else {
                    continue;
                }
                
                //过滤后面分页的重复选项
                if(repeatAIds != null && repeatAIds.size() > 0 && repeatAIds.contains(aid)){
                    continue;
                }
                PreviewAct t = null;
                try {
                    Gson gson = new Gson();
                    t = gson.fromJson(actcofig, PreviewAct.class);
                    list.add(t);
                } catch (Exception e) {
                    logger.error("fail to parse json object to java bean, json -> " + actcofig.toString());
                }
              }
            }
    }
	
	/**
	 * 获取大厅各栏目信息接口(10002031)
	 * @param jsonObject
	 * @return
	 */
	public JsonObject getHallParts(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		JsonObject result = new JsonObject();
		
		int platform, appId, channel;
		try {
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, 1, Integer.MAX_VALUE);
			appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 1, Integer.MAX_VALUE);
			channel = CommonUtil.getJsonParamInt(jsonObject, "c", AppChannelEnum.KK, null, 1, Integer.MAX_VALUE);
		} catch(CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch(Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}

        List<HomePage> tempList = null;
        try {
            FirstPageHandler firstPageHandler = MelotBeanFactory.getBean("firstPageHandler", FirstPageHandler.class);
            tempList = firstPageHandler.getFistPagelist(appId, channel, platform, false);
        } catch(Exception e) {
            logger.error("Fail to call firstPageHandler.getFistPagelist ", e);
        }
        
        JsonArray plateList = new JsonArray();
        if (tempList != null) {
            for (HomePage temp : tempList) {
                JsonObject json = new JsonObject();
                if (temp.getTitleName() != null) {
                    json.addProperty("partName", temp.getTitleName());
                }
                if (temp.getLiveTotal() != null) {
                    json.addProperty("onlineCount", temp.getLiveTotal());
                } else {
                    json.addProperty("onlineCount", 0);
                }
                if (temp.getDetailId() != null) {
                    // 专区编号
                    json.addProperty("partId", temp.getDetailId());
                }
                if (temp.getCdnState() != null) {
                    if (temp.getCdnState() > 0 && temp.getSeatType() != 3) {
                        JsonArray roomArray = new JsonArray();
                        List<RoomInfo> roomList = temp.getRooms();
                        if (roomList != null) {
                            for (RoomInfo roomInfo : roomList) {
                                roomArray.add(RoomTF.roomInfoToJsonTemp(roomInfo, platform));
                            }
                        }
                        json.add("rcdRooms", roomArray);
                    } else {
                        json.add("rcdRooms", new JsonArray());
                    }
                }
                plateList.add(json);
            }
            
        }
		
		result.add("partList", plateList);
		result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		
		// 返回结果
		return result;
	}
	
	/**
	 * 获取某栏目房间列表接口(10002032)
	 * @param jsonObject
	 * @return
	 */
	public JsonObject getHallPartRoomList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		
		JsonObject result = new JsonObject();
		
		int cataId, start, offset, platform;
		try {
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, null, 1, Integer.MAX_VALUE);
			cataId = CommonUtil.getJsonParamInt(jsonObject, "partId", 0, "02320001", 1, Integer.MAX_VALUE);
			start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, "02320003", 0, Integer.MAX_VALUE);
			offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 0, "02320005", 0, Integer.MAX_VALUE);
		} catch(CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch(Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		
		int roomCount = 0;
		JsonArray jRoomList = new JsonArray();
		
		if (cataId > 0) {
			try {
				FirstPageHandler firstPageHandler = MelotBeanFactory.getBean("firstPageHandler", FirstPageHandler.class);
				SysMenu sysMenu = firstPageHandler.getPartList(cataId, null, null, start, offset);
				if (sysMenu != null) {
					roomCount = sysMenu.getRoomCount();
					List<RoomInfo> roomList = sysMenu.getRooms();
					if (roomList != null) {
						for (RoomInfo roomInfo : roomList) {
							jRoomList.add(RoomTF.roomInfoToJsonTemp(roomInfo, platform));
						}
					}
				}
			} catch(Exception e) {
				logger.error("Fail to call firstPageHandler.getPartList, " + "cataId " + cataId, e);
			}
		}
		
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		result.addProperty("roomTotal", roomCount);
		result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
		result.add("roomList", jRoomList);
		
		return result;
	}
	
	/**
	 * 获取各省份本月新增主播数(10002036)
	 * @param jsonObject
	 * @param checkTag
	 * @return
	 */
	public JsonObject getEachProvinceNewActors(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
	
		Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH) + 1;
		
		JsonElement yearje = jsonObject.get("year");
		JsonElement monthje = jsonObject.get("month");
		if (yearje != null && !yearje.isJsonNull() && yearje.getAsInt()>0) {
			try {
				year = yearje.getAsInt();
			} catch (Exception e) {
				year = now.get(Calendar.YEAR);
			}
		} else {
			year = now.get(Calendar.YEAR);
		}
		if (monthje != null && !monthje.isJsonNull() && monthje.getAsInt()>0) {
			try {
				month = monthje.getAsInt();
			} catch (Exception e) {
				month = now.get(Calendar.MONTH) + 1;
			}
		} else {
			month = now.get(Calendar.MONTH) + 1;
		}
		
		JsonObject result = new JsonObject();

        List<AreaNewActors> areaNewActorsList = areaNewActorsService.getAreaNewActorsList(year,month, null);

		JsonArray areaArray = new JsonArray();
		for (AreaNewActors areaNewActors : areaNewActorsList) {
			JsonObject areaObj = new JsonObject();
			areaObj.addProperty("areaId", areaNewActors.getAreaId());
			areaObj.addProperty("area", areaNewActors.getAreaName());
			areaObj.addProperty("add", areaNewActors.getAddCount());
			areaObj.addProperty("total", areaNewActors.getTotalCount());
			areaArray.add(areaObj);
		}
		result.add("areaList", areaArray);
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		// 返回结果
		return result;
	}
	
	/**
	 * 获取地区房间列表(10002037)
	 * @param jsonObject
	 * @return
	 */
	public JsonObject getProvinceRoomList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		
		JsonObject result = new JsonObject();
		
		int area, cityId, start, offset, platform;
		String ip;
		try {
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, null, 1, Integer.MAX_VALUE);
			area = CommonUtil.getJsonParamInt(jsonObject, "area", 0, "02370001", 1, Integer.MAX_VALUE);
			cityId = CommonUtil.getJsonParamInt(jsonObject, "cityId", 0, null, 0, Integer.MAX_VALUE);
			start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, "02370005", 0, Integer.MAX_VALUE);
			offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 0, "02370003", 1, Integer.MAX_VALUE);
			ip = CommonUtil.getJsonParamString(jsonObject, "ip", com.melot.kktv.service.GeneralService.getIpAddr(request, AppIdEnum.AMUSEMENT, platform, null), null, 0, 20);
		} catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch (Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		
		if (cityId < 1) {
            cityId = CityUtil.getCityIdByIpAddr(ip);
        }
		
		int roomCount = 0;
		JsonArray jRoomList = new JsonArray();
		try {
			// 获取地区下房间列表
            RoomInfoService roomInfoServie = MelotBeanFactory.getBean("roomInfoService", RoomInfoService.class);
            roomCount = roomInfoServie.getRoomCountByDistrict(AppIdEnum.AMUSEMENT, area);
            if (roomCount > 0) {
            	List<RoomInfo> roomList = roomInfoServie.getRoomListByDistrict(AppIdEnum.AMUSEMENT, area, cityId, start, offset);
	            if (roomList != null) {
					for (RoomInfo roomInfo : roomList) {
						JsonObject roomJson = RoomTF.roomInfoToJsonTemp(roomInfo, platform);
						jRoomList.add(roomJson);
					}
				}
            }
		} catch (Exception e) {
           logger.error("roomInfoServie.getRoomListByDistrict, area : " + area, e);
        }
		
		result.addProperty("roomTotal", roomCount);
		result.add("roomList", jRoomList);
		result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
	}
	
	/**
	 * 获取被捧场主播排行(10002039)
	 * @param jsonObject
	 * @return
	 */
	public JsonObject getRewardedActorsRankingList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

		JsonElement startTimeje = jsonObject.get("startTime");
		JsonElement endTimeje = jsonObject.get("endTime");
		JsonElement topicje = jsonObject.get("topic");
		JsonElement countje = jsonObject.get("count");
		JsonElement platformje = jsonObject.get("platform");
		JsonElement onlyVideoje = jsonObject.get("onlyVideo");
		
		long startTime = 0;
		long endTime = 0;
		String topic = null;
		Integer count = Constant.return_rank_count;
		int platform = PlatformEnum.WEB;
		int onlyVideo = 0;
		
		if (startTimeje != null && !startTimeje.isJsonNull() && startTimeje.getAsLong() > 0) {
			try {
				startTime = startTimeje.getAsLong();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "02390002");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "02390001");
			return result;
		}
		if (endTimeje != null && !endTimeje.isJsonNull() && endTimeje.getAsLong() > 0) {
			try {
				endTime = endTimeje.getAsLong();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "02390004");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "02390003");
			return result;
		}
		if (topicje != null && !topicje.isJsonNull() && !topicje.getAsString().isEmpty()) {
			try {
				topic = topicje.getAsString();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "02390006");
				return result;
			}
		}
		if (onlyVideoje != null && !onlyVideoje.isJsonNull() && !onlyVideoje.getAsString().isEmpty()) {
			try {
				onlyVideo = onlyVideoje.getAsInt();
			} catch (Exception e) {
			}
		}
		if (countje != null && !countje.isJsonNull() && countje.getAsInt()>0) {
			try {
				count = countje.getAsInt();
			} catch (Exception e) {}
		}
		if (platformje != null && !platformje.isJsonNull() && platformje.getAsInt() > 0) {
			try {
				platform = platformje.getAsInt();
			} catch (Exception e) {}
		}

		String hotDataKey = "newsRewardActorRanking" + (topic == null ? "" : topic) + startTime + endTime;
		
		JsonObject result = new JsonObject();
		JsonArray rankList = new JsonArray();
		Map<String, String> hotData = HotDataSource.getHotData(hotDataKey);
		if (hotData != null && hotData.size() > 0) {
			try {
				String newsRewardActorRanking = hotData.get("data");
				rankList = new JsonParser().parse(newsRewardActorRanking).getAsJsonArray();
			} catch (Exception e) {
			}
			if (rankList.size() > 0) {
				for (int i = 0; i < rankList.size(); i++) {
					JsonObject rankObj = rankList.get(i).getAsJsonObject();
					if (rankObj.has("portrait_path_original") && rankObj.get("portrait_path_original") != null) {
						String portrait_path_original = rankObj.get("portrait_path_original").getAsString(); 
						if (platform == PlatformEnum.WEB) {
							rankObj.addProperty("portrait_path_256", portrait_path_original + "!256");
						} else if (platform == PlatformEnum.ANDROID || platform == PlatformEnum.IPHONE || platform == PlatformEnum.IPAD) {
							rankObj.addProperty("portrait_path_128", portrait_path_original + "!128");
						} else {
							rankObj.addProperty("portrait_path_original", portrait_path_original);
							rankObj.addProperty("portrait_path_1280", portrait_path_original + "!1280");
							rankObj.addProperty("portrait_path_256", portrait_path_original + "!256");
							rankObj.addProperty("portrait_path_128", portrait_path_original + "!128");
							rankObj.addProperty("portrait_path_48", portrait_path_original + "!48");
						}
					}
				}
			}
		} else {
			try {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("startTime", new Date(startTime));
				map.put("endTime", new Date(endTime));
				map.put("topic", topic);
				map.put("count", count);
				if (onlyVideo > 0) map.put("onlyVideo", onlyVideo);
				@SuppressWarnings("unchecked")
				List<NewsRewardRank> newsRewardRankList = SqlMapClientHelper.getInstance(DB.MASTER)
						.queryForList("Index.getNewsRewardActorRankList", map);
				newsRewardRankList = UserService.addUserExtra(newsRewardRankList);
				if (newsRewardRankList != null && newsRewardRankList.size() > 0) {
					JsonArray jsonArr = new JsonArray();
					for (NewsRewardRank rank : newsRewardRankList) {
						JsonObject rankObj = NewsRewardRankTF.toJsonObject(rank);
						jsonArr.add(rankObj);
						if (rankObj.has("portrait_path_original") && rankObj.get("portrait_path_original") != null) {
							String portrait_path_original = rankObj.get("portrait_path_original").getAsString(); 
							if (platform == PlatformEnum.WEB) {
								rankObj.addProperty("portrait_path_256", portrait_path_original + "!256");
							} else if (platform == PlatformEnum.ANDROID || platform == PlatformEnum.IPHONE || platform == PlatformEnum.IPAD) {
								rankObj.addProperty("portrait_path_128", portrait_path_original + "!128");
							} else {
								rankObj.addProperty("portrait_path_original", portrait_path_original);
								rankObj.addProperty("portrait_path_1280", portrait_path_original + "!1280");
								rankObj.addProperty("portrait_path_256", portrait_path_original + "!256");
								rankObj.addProperty("portrait_path_128", portrait_path_original + "!128");
								rankObj.addProperty("portrait_path_48", portrait_path_original + "!48");
							}
						}
						rankList.add(rankObj);
					}
					hotData.put("data", jsonArr.toString());
					HotDataSource.setHotData(hotDataKey, hotData, ConfigHelper.getNewsRewardRankRefreshIntervel());
				}
			} catch (SQLException e) {
				logger.error("未能正常执行数据库语句", e);
			}
		}
		
		result.add("rankList", rankList);
		result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		
		return result;
	}
	
	/**
	 * 获取捧场用户排行(10002040)
	 * @param jsonObject
	 * @return
	 */
	public JsonObject getHaveRewardedUsersRankingList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

		JsonElement startTimeje = jsonObject.get("startTime");
		JsonElement endTimeje = jsonObject.get("endTime");
		JsonElement topicje = jsonObject.get("topic");
		JsonElement countje = jsonObject.get("count");
		JsonElement platformje = jsonObject.get("platform");
		JsonElement onlyVideoje = jsonObject.get("onlyVideo");
		
		long startTime = 0;
		long endTime = 0;
		String topic = null;
		Integer count = Constant.return_rank_count;
		int platform = PlatformEnum.WEB;
		int onlyVideo = 0;
		
		if (startTimeje != null && !startTimeje.isJsonNull() && startTimeje.getAsLong() > 0) {
			try {
				startTime = startTimeje.getAsLong();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "02400002");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "02400001");
			return result;
		}
		if (endTimeje != null && !endTimeje.isJsonNull() && endTimeje.getAsLong() > 0) {
			try {
				endTime = endTimeje.getAsLong();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "02400004");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "02400003");
			return result;
		}
		if (topicje != null && !topicje.isJsonNull() && !topicje.getAsString().isEmpty()) {
			try {
				topic = topicje.getAsString();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "02400006");
				return result;
			}
		}
		if (onlyVideoje != null && !onlyVideoje.isJsonNull() && !onlyVideoje.getAsString().isEmpty()) {
			try {
				onlyVideo = onlyVideoje.getAsInt();
			} catch (Exception e) {}
		}
		if (countje != null && !countje.isJsonNull() && countje.getAsInt() > 0) {
			try {
				count = countje.getAsInt();
			} catch (Exception e) {}
		}
		if (platformje != null && !platformje.isJsonNull() && platformje.getAsInt() > 0) {
			try {
				platform = platformje.getAsInt();
			} catch (Exception e) {}
		}
		
		String hotDataKey = "newsRewardUserRanking" + (topic == null ? "" : topic) + startTime + endTime;
		
		JsonObject result = new JsonObject();
		JsonArray rankList = new JsonArray();
		Map<String, String> hotData = HotDataSource.getHotData(hotDataKey);
		if (hotData != null && hotData.size() > 0) {
			try {
				String newsRewardActorRanking = hotData.get("data");
				rankList = new JsonParser().parse(newsRewardActorRanking).getAsJsonArray();
			} catch (Exception e) {
			}
			if (rankList.size() > 0) {
				for (int i = 0; i < rankList.size(); i++) {
					JsonObject rankObj = rankList.get(i).getAsJsonObject();
					if (rankObj.has("portrait_path_original") && rankObj.get("portrait_path_original") != null) {
						String portrait_path_original = rankObj.get("portrait_path_original").getAsString(); 
						if (platform == PlatformEnum.WEB) {
							rankObj.addProperty("portrait_path_256", portrait_path_original + "!256");
						} else if (platform == PlatformEnum.ANDROID || platform == PlatformEnum.IPHONE || platform == PlatformEnum.IPAD) {
							rankObj.addProperty("portrait_path_128", portrait_path_original + "!128");
						} else {
							rankObj.addProperty("portrait_path_original", portrait_path_original);
							rankObj.addProperty("portrait_path_1280", portrait_path_original + "!1280");
							rankObj.addProperty("portrait_path_256", portrait_path_original + "!256");
							rankObj.addProperty("portrait_path_128", portrait_path_original + "!128");
							rankObj.addProperty("portrait_path_48", portrait_path_original + "!48");
						}
					}
				}
			}
		} else {
			try {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("startTime", new Date(startTime));
				map.put("endTime", new Date(endTime));
				map.put("topic", topic);
				map.put("count", count);
				if (onlyVideo > 0) map.put("onlyVideo", onlyVideo);
				@SuppressWarnings("unchecked")
				List<NewsRewardRank> newsRewardRankList = SqlMapClientHelper.getInstance(DB.MASTER)
						.queryForList("Index.getNewsRewardUserRankList", map);
				if (newsRewardRankList != null && newsRewardRankList.size() > 0) {
					JsonArray jsonArr = new JsonArray();
					for (NewsRewardRank rank : newsRewardRankList) {
						JsonObject rankObj = NewsRewardRankTF.toJsonObject(rank);
						jsonArr.add(rankObj);
						if (rankObj.has("portrait_path_original") && rankObj.get("portrait_path_original") != null) {
							String portrait_path_original = rankObj.get("portrait_path_original").getAsString(); 
							if(platform == PlatformEnum.WEB) {
								rankObj.addProperty("portrait_path_256", portrait_path_original + "!256");
							} else if(platform == PlatformEnum.ANDROID || platform == PlatformEnum.IPHONE || platform == PlatformEnum.IPAD) {
								rankObj.addProperty("portrait_path_128", portrait_path_original + "!128");
							} else {
								rankObj.addProperty("portrait_path_original", portrait_path_original);
								rankObj.addProperty("portrait_path_1280", portrait_path_original + "!1280");
								rankObj.addProperty("portrait_path_256", portrait_path_original + "!256");
								rankObj.addProperty("portrait_path_128", portrait_path_original + "!128");
								rankObj.addProperty("portrait_path_48", portrait_path_original + "!48");
							}
						}
						rankList.add(rankObj);
					}
					hotData.put("data", jsonArr.toString());
					HotDataSource.setHotData(hotDataKey, hotData, ConfigHelper.getNewsRewardRankRefreshIntervel());
				}
			} catch (SQLException e) {
				logger.error("未能正常执行数据库语句", e);
			}
		}
		
		result.add("rankList", rankList);
		result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		
		return result;
	}
	
	/**
	 * 获取用户一定时间内收到礼物个数(10002041)
	 * @param jsonObject
	 * @return
	 */
	public JsonObject getPeriodUserGiftReceiveCount(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

		JsonElement startTimeje = jsonObject.get("startTime");
		JsonElement endTimeje = jsonObject.get("endTime");
		JsonElement giftIdje = jsonObject.get("giftId");
		JsonElement userIdje = jsonObject.get("userId");
		
		long startTime = 0;
		long endTime = 0;
		int giftId = 0;
		int userId = 0;
		
		if (startTimeje != null && !startTimeje.isJsonNull() && !startTimeje.getAsString().isEmpty()) {
			try {
				startTime = startTimeje.getAsLong();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "02410002");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "02410001");
			return result;
		}
		if (endTimeje != null && !endTimeje.isJsonNull() && !endTimeje.getAsString().isEmpty()) {
			try {
				endTime = endTimeje.getAsLong();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "02410004");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "02410003");
			return result;
		}
		if (giftIdje != null && !giftIdje.isJsonNull() && !giftIdje.getAsString().isEmpty()) {
			try {
				giftId = giftIdje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "02410005");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "02410006");
			return result;
		}
		if (userIdje != null && !userIdje.isJsonNull() && !userIdje.getAsString().isEmpty()) {
			try {
				userId = userIdje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "02410007");
				return result;
			}
		}
		
		long recvCnt = 0;
		
		String hotDataKey = "userGiftRecvCnt_"+userId+giftId+startTime+endTime;
		String value = HotDataSource.getHotFieldValue(hotDataKey, "recvCnt");
		if (value != null) {
			recvCnt = Long.parseLong(value);
		} else {
			try {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("startTime", new Date(startTime));
				map.put("endTime", new Date(endTime));
				map.put("userId", userId);
				map.put("giftId", giftId);
				Long count = (Long) SqlMapClientHelper.getInstance(DB.MASTER)
						.queryForObject("Index.getPeriodUserGiftReceiveCount", map);
				if (count != null) {
					recvCnt = count;
					HotDataSource.setHotFieldValue(hotDataKey, "recvCnt", count.toString(), 10);
				}
			} catch (SQLException e) {
				logger.error("未能正常执行数据库语句", e);
			}
		}
		
		JsonObject result = new JsonObject();
		result.addProperty("recvCnt",recvCnt);
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
	}
	
	/**
	 * 获取一定时间内礼物送出总数、打折总数和用户总数(10002042)
	 * @param jsonObject
	 * @return
	 */
	public JsonObject getPeriodGiftTotal(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
	    JsonObject result = new JsonObject();
		
		String giftIds;
		int discount;
		long startTime, endTime;
		try {
		    giftIds = CommonUtil.getJsonParamString(paramJsonObject, "giftIds", null, "02420001", 1, Integer.MAX_VALUE);
		    discount = CommonUtil.getJsonParamInt(paramJsonObject, "discount", 0, null, 1, Integer.MAX_VALUE);
		    startTime = CommonUtil.getJsonParamLong(paramJsonObject, "startTime", System.currentTimeMillis()/1000, null, 1l, Long.MAX_VALUE);
		    endTime = CommonUtil.getJsonParamLong(paramJsonObject, "endTime", 0, null, 1l, Long.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
		
		long totalGift = 0, discountGift = 0, totalAmount = 0, discountAmount = 0, totalUser = 0;
		String hotDataKey = "giftUserTotal_" + giftIds + startTime + endTime;
		Map<String, String> hotData = HotDataSource.getHotData(hotDataKey);
		if (hotData != null && hotData.containsKey("totalGift") && hotData.containsKey("totalUser") && hotData.containsKey("totalAmount")) {
			totalGift = Long.parseLong(hotData.get("totalGift"));
			discountGift = totalGift * discount / 100;
			totalAmount = Long.parseLong(hotData.get("totalAmount"));
			discountAmount = totalAmount * discount / 100;
			totalUser = Long.parseLong(hotData.get("totalUser"));
		} else {
			try {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("startTime", DateUtil.formatDateTime(new Date(startTime * 1000), "yyyy-MM-dd HH:mm:ss"));
				if (endTime > 0) {
				    map.put("endTime", DateUtil.formatDateTime(new Date(endTime * 1000), "yyyy-MM-dd HH:mm:ss"));
                }
				map.put("giftIds", giftIds);
				SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Index.getPeriodGiftTotal", map, map);
				if (map.containsKey("totalGift") && map.containsKey("totalUser")) {
					totalGift = (Long) map.get("totalGift");
					discountGift = totalGift * discount / 100;
					totalAmount = (Long) map.get("totalAmount");
					discountAmount = totalAmount * discount / 100;
					totalUser = (Long) map.get("totalUser");
					hotData = new HashMap<String, String>();
					hotData.put("totalGift", String.valueOf(totalGift));
                    hotData.put("totalAmount", String.valueOf(totalAmount));
					hotData.put("totalUser", String.valueOf(totalUser));
					HotDataSource.setHotData(hotDataKey, hotData, 10);
				}
			} catch (SQLException e) {
				logger.error("未能正常执行数据库语句", e);
			}
		}
		
		result.addProperty("totalGift", totalGift);
        result.addProperty("discountGift", discountGift);
        result.addProperty("totalAmount", totalAmount);
        result.addProperty("discountAmount", discountAmount);
		result.addProperty("totalUser", totalUser);
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
	}
	
	/**
	 * 获取半小时上头条排行榜(10002043)
	 * @param jsonObject
	 * @return
	 */
	public JsonObject getHeadLineGiftRank(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		// define usable parameters
		long startTime = 0;
		long endTime = 0;
//		int giftId = 0;
		int platform = 0;
		// parse the parameters
		JsonObject result = new JsonObject();
		try {
//			giftId = CommonUtil.getJsonParamInt(jsonObject, "giftId", 40000234, null, 1, Integer.MAX_VALUE);
			startTime = CommonUtil.getJsonParamLong(jsonObject, "startTime", 0, "02430003", 0, Long.MAX_VALUE);
			endTime = CommonUtil.getJsonParamLong(jsonObject, "endTime", 0, "02430005", 0, Long.MAX_VALUE);
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, PlatformEnum.WEB, PlatformEnum.IPAD);
		} catch(CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch(Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		String dateStr = DateUtil.formatDate(new Date(startTime), "yyyyMMdd");
		String timeField = DateUtil.formatDateTime(new Date(startTime), "HHmm")
				+ DateUtil.formatDateTime(new Date(endTime), "HHmm");
		String json = GiftRecordSource.getHeadlineRank(dateStr, timeField);
		JsonArray jsonArray = new JsonArray();
		if (json != null) {
			try {
				jsonArray = new JsonParser().parse(json).getAsJsonArray();
				for (JsonElement jsonElement : jsonArray) {
					JsonObject jObject = jsonElement.getAsJsonObject();
					int userId = jObject.get("userId").getAsInt();
					if (jObject.has("path_original")) {
						switch (platform) {
							case PlatformEnum.WEB:
								jObject.addProperty("portrait_path_128", jObject.get("path_original").getAsString()+"!128");
								break;
							case PlatformEnum.ANDROID:
								jObject.addProperty("portrait_path_128", jObject.get("path_original").getAsString()+"!128");
								break;
							case PlatformEnum.IPHONE:
								jObject.addProperty("portrait_path_128", jObject.get("path_original").getAsString()+"!128");
								break;
							case PlatformEnum.IPAD:
								jObject.addProperty("portrait_path_128", jObject.get("path_original").getAsString()+"!128");
								break;
							default:
								break;
						}
					}
					// 读取明星等级
					jObject.addProperty("actorLevel", UserService.getActorLevel(userId));
					// 读取富豪等级
					jObject.addProperty("richLevel", UserService.getRichLevel(userId));
					// 增加返回roomSource 默认为kk唱响主播
					jObject.addProperty("roomSource", AppIdEnum.AMUSEMENT);
					jObject.addProperty("roomType", AppIdEnum.AMUSEMENT);
				}
			} catch (Exception e) {
				logger.error("fail to parse json string", e);
			}
		}
		result.add("rankList", jsonArray);
		result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
	}
    
    /**
     * 获得上头条总榜(10002047)
     * @param paramJsonObject
     * @return
     */
    public JsonObject getHeadlineTotal(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        int platform = 0;
        int type = 0;
        try {
            platform = CommonUtil.getJsonParamInt(paramJsonObject, "platform", PlatformEnum.WEB, null, PlatformEnum.WEB, PlatformEnum.IPAD);
            type = CommonUtil.getJsonParamInt(paramJsonObject, "type", 0, null, 0, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        Map<String, String> map = GiftRecordSource.getHeadlineTotal();
        String json = null;
        if (map != null) {
            if(type == 0) {
                json = map.get("count");
            } else {
                json = map.get("gift");
            }
        }
        JsonArray jsonArray = new JsonArray();
        if (json != null) {
            try {
                jsonArray = new JsonParser().parse(json).getAsJsonArray();
                for (JsonElement jsonElement : jsonArray) {
                    JsonObject jObject = jsonElement.getAsJsonObject();
                    int userId = jObject.get("userId").getAsInt();
                    if (jObject.has("path_original")) {
                        switch (platform) {
                            case PlatformEnum.WEB:
                                jObject.addProperty("portrait_path_128", jObject.get("path_original").getAsString()+"!128");
                                break;
                            case PlatformEnum.ANDROID:
                                jObject.addProperty("portrait_path_128", jObject.get("path_original").getAsString()+"!128");
                                break;
                            case PlatformEnum.IPHONE:
                                jObject.addProperty("portrait_path_128", jObject.get("path_original").getAsString()+"!128");
                                break;
                            case PlatformEnum.IPAD:
                                jObject.addProperty("portrait_path_128", jObject.get("path_original").getAsString()+"!128");
                                break;
                            default:
                                break;
                        }
                    }
                    // 读取明星等级
                    jObject.addProperty("actorLevel", UserService.getActorLevel(userId));
                    // 读取富豪等级
                    jObject.addProperty("richLevel", UserService.getRichLevel(userId));
                    // 新增roomSource 默认为kk唱响主播
                    jObject.addProperty("roomSource", AppIdEnum.AMUSEMENT);
                    jObject.addProperty("roomType", AppIdEnum.AMUSEMENT);
                }
            } catch (Exception e) {
                logger.error("fail to parse json string", e);
            }
        }
        result.add("rankList", jsonArray);
        result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 获取半小时阳光上头条排行榜(10002060)
     * @param jsonObject
     * @return
     */
    public JsonObject getSunshineHeadLineGiftRank(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        long startTime = 0;
        long endTime = 0;
        int platform = 0;
        JsonObject result = new JsonObject();
        try {
            startTime = CommonUtil.getJsonParamLong(jsonObject, "startTime", 0, "02600001", 0, Long.MAX_VALUE);
            endTime = CommonUtil.getJsonParamLong(jsonObject, "endTime", 0, "02600002", 0, Long.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, PlatformEnum.WEB, PlatformEnum.IPAD);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        String dateStr = DateUtil.formatDate(new Date(startTime), "yyyyMMdd");
        String timeField = DateUtil.formatDateTime(new Date(startTime), "HHmm")
                + DateUtil.formatDateTime(new Date(endTime), "HHmm");
        String json = GiftRecordSource.getHeadlineSunshineRank(dateStr, timeField);
        JsonArray jsonArray = new JsonArray();
        if (json != null) {
            try {
                jsonArray = new JsonParser().parse(json).getAsJsonArray();
                for (JsonElement jsonElement : jsonArray) {
                    JsonObject jObject = jsonElement.getAsJsonObject();
                    int userId = jObject.get("userId").getAsInt();
                    if (jObject.has("path_original")) {
                        switch (platform) {
                            case PlatformEnum.WEB:
                                jObject.addProperty("portrait_path_128", jObject.get("path_original").getAsString()+"!128");
                                break;
                            case PlatformEnum.ANDROID:
                                jObject.addProperty("portrait_path_128", jObject.get("path_original").getAsString()+"!128");
                                break;
                            case PlatformEnum.IPHONE:
                                jObject.addProperty("portrait_path_128", jObject.get("path_original").getAsString()+"!128");
                                break;
                            case PlatformEnum.IPAD:
                                jObject.addProperty("portrait_path_128", jObject.get("path_original").getAsString()+"!128");
                                break;
                            default:
                                break;
                        }
                    }
                    // 读取明星等级
                    jObject.addProperty("actorLevel", UserService.getActorLevel(userId));
                    // 读取富豪等级
                    jObject.addProperty("richLevel", UserService.getRichLevel(userId));
                    // 增加返回roomSource 默认为kk唱响主播
                    jObject.addProperty("roomSource", AppIdEnum.AMUSEMENT);
                    jObject.addProperty("roomType", AppIdEnum.AMUSEMENT);
                }
            } catch (Exception e) {
                logger.error("fail to parse json string", e);
            }
        }
        result.add("rankList", jsonArray);
        result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 获取阳光上头条总榜(10002061)
     * @param paramJsonObject
     * @return
     */
    public JsonObject getSunshineHeadlineTotal(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        int platform = 0;
        int type = 0;
        try {
            platform = CommonUtil.getJsonParamInt(paramJsonObject, "platform", PlatformEnum.WEB, null, PlatformEnum.WEB, PlatformEnum.IPAD);
            type = CommonUtil.getJsonParamInt(paramJsonObject, "type", 0, null, 0, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        Map<String, String> map = GiftRecordSource.getHeadlineSunshineTotal();
        String json = null;
        if (map != null) {
            if(type == 0) {
                json = map.get("count");
            } else {
                json = map.get("gift");
            }
        }
        JsonArray jsonArray = new JsonArray();
        if (json != null) {
            try {
                jsonArray = new JsonParser().parse(json).getAsJsonArray();
                for (JsonElement jsonElement : jsonArray) {
                    JsonObject jObject = jsonElement.getAsJsonObject();
                    int userId = jObject.get("userId").getAsInt();
                    if (jObject.has("path_original")) {
                        switch (platform) {
                            case PlatformEnum.WEB:
                                jObject.addProperty("portrait_path_128", jObject.get("path_original").getAsString()+"!128");
                                break;
                            case PlatformEnum.ANDROID:
                                jObject.addProperty("portrait_path_128", jObject.get("path_original").getAsString()+"!128");
                                break;
                            case PlatformEnum.IPHONE:
                                jObject.addProperty("portrait_path_128", jObject.get("path_original").getAsString()+"!128");
                                break;
                            case PlatformEnum.IPAD:
                                jObject.addProperty("portrait_path_128", jObject.get("path_original").getAsString()+"!128");
                                break;
                            default:
                                break;
                        }
                    }
                    // 读取明星等级
                    jObject.addProperty("actorLevel", UserService.getActorLevel(userId));
                    // 读取富豪等级
                    jObject.addProperty("richLevel", UserService.getRichLevel(userId));
                    // 新增roomSource 默认为kk唱响主播
                    jObject.addProperty("roomSource", AppIdEnum.AMUSEMENT);
                    jObject.addProperty("roomType", AppIdEnum.AMUSEMENT);
                }
            } catch (Exception e) {
                logger.error("fail to parse json string", e);
            }
        }
        result.add("rankList", jsonArray);
        result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
	
	/**
    * 获取热门关键字列表(接口10002045)
    * @param jsonObject
    * @return
    */
    public JsonObject getHotKeywordList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
    	
    	int count = 0;
    	JsonObject result = new JsonObject();
    	try {
    		count = CommonUtil.getJsonParamInt(jsonObject, "count", 8, null, 1, Integer.MAX_VALUE);
    	} catch(CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch(Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
    	
    	List<String> hotWords = SearchWordsSource.getHotKeywords(count);
    	if (hotWords != null) {
    		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
    		result.add("hotWords", new Gson().toJsonTree(hotWords).getAsJsonArray());
    	} else {
    		result.addProperty("TagCode", TagCodeEnum.GET_HOTWORDS_FAIL);
    	}
    	
    	return result;
    	
    }
	
    /**
	 * 获取用户发现提醒个数(10002046)
	 * @param jsonObject
	 * @return
	 */
	public JsonObject getDiscoverNum(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		JsonObject result = new JsonObject();
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		int userId = 0;
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
		} catch(CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch(Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		//判断redis发现记录是否存在
		Map<String, String> map = null;
		if (NewsSource.isDiscoverRecordExist(String.valueOf(userId))) {
			if ((map = NewsSource.getDiscoverRecord(String.valueOf(userId))) != null) {
				if (map.get("newsCnt") != null) {
					result.addProperty("newsRemindCnt", Integer.valueOf(map.get("newsCnt")));
				} else {
					result.addProperty("newsRemindCnt", 0);
				}
				if (map.get("matchCnt") != null) {
					result.addProperty("matchRemindCnt", Integer.valueOf(map.get("matchCnt")));
				} else {
					result.addProperty("matchRemindCnt", 0);
				}
				result.addProperty("TagCode", TagCodeEnum.SUCCESS);
				return result;
			}
		}
		long lastNewsTime = 0l;
		long lastMatchTime = 0l;
		//读取上次读取发现提醒时间,无记录则获取上次登录时间
		if ((map = NewsSource.getLastDiscoverRecord(String.valueOf(userId))) != null) {
			lastNewsTime = Long.valueOf(map.get("news"));
			String temp = map.get("match");
			if(temp != null){
				lastMatchTime = Long.valueOf(map.get("match"));
			} else {
				lastMatchTime = lastNewsTime;
			}
		} else {
		    try {
		        KkUserService kkUserService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
		        LastLoginInfo lastLoginInfo = kkUserService.getLastLoginInfo(userId);
		        if (lastLoginInfo != null && lastLoginInfo.getLastLoginTime() > 0) {
		            lastNewsTime = lastLoginInfo.getLastLoginTime();
	                lastMatchTime = lastLoginInfo.getLastLoginTime();
		        } else {
		            result.addProperty("TagCode", TagCodeEnum.GET_USER_LOGIN_TIME_FAIL);
	                return result;
		        }
		    } catch (Exception e) {
		        logger.error("kkUserService.getLastLoginInfo(" + userId + ") execute exception.", e);
		        result.addProperty("TagCode", TagCodeEnum.GET_USER_LOGIN_TIME_FAIL);
                return result;
		    }
		}
		//根据上次读取时间查询未读新动态数量和新比赛数量
		int newsRemindCnt = NewsService.getUnReadNewsNum(userId, lastNewsTime);
		//查询结果更新到redis,并更新上次读取发现提醒时间
		Map<String, String> record = new HashMap<String, String>();
		record.put("newsCnt", String.valueOf(newsRemindCnt));
		record.put("matchCnt", "0");
		if (!NewsSource.setDiscoverRecord(String.valueOf(userId), record)) {
			logger.error("Fail to update USER_REMIND_RECORD ");
		}
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		result.addProperty("newsRemindCnt", newsRemindCnt);
		result.addProperty("matchRemindCnt", 0);
		return result;
	}
    
    /**
     * 获取kk现场（更多）(10002048)
     * @param paramJsonObject
     * @return
     */
	public JsonObject getMoreLiveAlbum(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {

		// 定义返回结果
		JsonObject result = new JsonObject();
		
		// 定义所需参数
		int pageIndex  = 0;
		int countPerPage  = 0;
		//int platform = PlatformEnum.WEB;
		// 解析参数
		try {
			pageIndex = CommonUtil.getJsonParamInt(paramJsonObject, "pageIndex", 0, null, 0, Integer.MAX_VALUE);
			countPerPage = CommonUtil.getJsonParamInt(paramJsonObject, "countPerPage", Constant.return_room_count, null, 0, Integer.MAX_VALUE);
		} catch (ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		}
		
		LiveAlbumService liveAlbumService = MelotBeanFactory.getBean("liveAlbumService", LiveAlbumService.class);
		QiniuService qiniuService = MelotBeanFactory.getBean("qiniuService", QiniuService.class);
		LiveVideoService liveVideoService = MelotBeanFactory.getBean("liveVideoService", LiveVideoService.class);
		int totalCount = liveAlbumService.getAlbumCount();
		if (totalCount > 0) {
			String pathPrefix = qiniuService.getDomain();
			List<LiveAlbum> liveAlbums = liveAlbumService.getAddedAlbumList(pageIndex, countPerPage);
			if (liveAlbums != null && liveAlbums.size() > 0) {	
				String domain = qiniuService.getDomain();
				domain = domain != null ? domain : "";
				JsonArray liveShowArray = new JsonArray();
				for (LiveAlbum lab : liveAlbums) {
					JsonObject LiveAlbumObj = new JsonObject();
					LiveAlbumObj = LiveShowTF.toInnerJsonObject(lab, pathPrefix);
					Integer albumId = lab.getAlbumId();
					try {
						if (albumId != null) {
							List<LiveVideo> liveVideos = liveVideoService.getLiveVideoList(albumId);
							if (liveVideos != null && liveVideos.size() > 0) {
								JsonArray liveVideoArr = new JsonArray();
								for (LiveVideo lv : liveVideos) {
									liveVideoArr.add(LiveShowTF.toInnerVideoobject(lv, domain));
								}
								LiveAlbumObj.add("videoList", liveVideoArr);
							}
						}
					} catch (Exception e) {
						logger.error("IndexFunctions.getMoreLiveAlbum(liveVideoService.getLiveVideoList) exception", e);
					}
					liveShowArray.add(LiveAlbumObj);
				}
				result.add("liveShowList", liveShowArray);
				result.addProperty("pageTotal", (int) Math.ceil((double) totalCount / countPerPage));
			}
		}
		
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
	}
	
	/**
	 * 获取大厅推荐kk现场 (10002049)
	 * @param paramJsonObject
	 * @return
	 */
	public JsonObject getHallLiveAlbum(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
		
		JsonObject result = new JsonObject();
		
		int liveShowCount = 0;
		JsonArray liveShowArray = new JsonArray();
		
		LiveAlbumService liveAlbumService = MelotBeanFactory.getBean("liveAlbumService", LiveAlbumService.class);
		QiniuService qiniuService = MelotBeanFactory.getBean("qiniuService", QiniuService.class);
		liveShowCount = liveAlbumService.getAlbumCount();
		if (liveShowCount > 0) {
			List<LiveAlbum> liveAlbums = liveAlbumService.getAddedAlbumList(0, 3);
			String domain = qiniuService.getDomain();
			domain = domain != null ? domain : "";
			if (liveAlbums != null && liveAlbums.size() > 0) {
				for (LiveAlbum lab : liveAlbums) {
					liveShowArray.add(LiveShowTF.toJsonObject(lab, domain));
				}
			}
		}

		result.addProperty("liveShowCount", liveShowCount);
		result.add("liveShowList", liveShowArray);
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		
		return result;
	}
}

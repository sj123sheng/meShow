package com.melot.kkcx.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.melot.kkcx.service.MessageService;
import com.melot.kkcx.service.UserService;
import com.melot.kkcx.transform.RoomTF;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.api.menu.sdk.service.RoomInfoService;
import com.melot.family.driver.domain.FamilyInfo;
import com.melot.family.driver.domain.RespMsg;
import com.melot.family.driver.service.FamilyAdminService;
import com.melot.family.driver.service.FamilyInfoService;
import com.melot.family.driver.service.FamilyOperatorService;
import com.melot.kktv.domain.Honour;
import com.melot.kktv.model.Family;
import com.melot.kktv.model.FamilyApplicant;
import com.melot.kktv.model.FamilyHonor;
import com.melot.kktv.model.FamilyMember;
import com.melot.kktv.model.FamilyPoster;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;
import com.melot.kktv.util.mongodb.CommonDB;
import com.melot.kktv.redis.FamilyApplySource;
import com.melot.kktv.redis.FamilyHonorSource;
import com.melot.kktv.redis.FamilyRoomSource;
import com.melot.kktv.redis.FamilySource;
import com.melot.kktv.redis.MedalSource;
import com.melot.kktv.redis.RedisServiceKey;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.CollectionEnum;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.Constant;
import com.melot.kktv.util.FamilyMemberEnum;
import com.melot.kktv.util.FamilyRankingEnum;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.sdk.core.util.MelotBeanFactory;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryOperators;

public class FamilyService {
	
	private static Logger logger = Logger.getLogger(FamilyService.class);
	
	/**
	 * 获取分类下家族总数
	 * @param basicDBObject
	 * @return long
	 */
	public static long getFamilyCountByType(int type) {
		long count = 0;
		if (type == FamilyRankingEnum.RANKING_TYPE_FAMILY_RICH) {
			count = FamilyHonorSource.getFamilyHonorCount(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_CONSUMETOTAL);
		} else if (type == FamilyRankingEnum.RANKING_TYPE_FAMILY_POPULAR) {
			count = FamilyHonorSource.getFamilyHonorCount(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_MEMBERCOUNT);
		} else if (type == FamilyRankingEnum.RANKING_TYPE_FAMILY_TOTALLIVE) {
			count = FamilyHonorSource.getFamilyHonorCount(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_TOTALLIVE);
		} else if (type == FamilyRankingEnum.RANKING_TYPE_FAMILY_CROWN) {
			count = FamilyHonorSource.getFamilyHonorCount(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_CROWNCOUNT);
		} else if (type == FamilyRankingEnum.RANKING_TYPE_FAMILY_DIAMOND) {
			count = FamilyHonorSource.getFamilyHonorCount(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_DIAMONDCOUNT);
		} else if (type == FamilyRankingEnum.RANKING_TYPE_FAMILY_HEART) {
			count = FamilyHonorSource.getFamilyHonorCount(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_HEARTCOUNT);
		} else if (type == FamilyRankingEnum.RANKING_TYPE_FAMILY_MEDAL) {
			count = FamilyHonorSource.getFamilyHonorCount(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_MEDALCOUNT);
		} else if (type == FamilyRankingEnum.RANKING_TYPE_FAMILY_WEEKLYCONSUME) {
			count = FamilyHonorSource.getFamilyHonorCount(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_WEEKLYCONSUME);
		}
		return count;
	}
	
	/**
	 * 根据分类分页返回家族列表
	 * @param type
	 * @param start
	 * @param end
	 * @return
	 */
	public static List<Integer> getFamilyIdListByType(int type, int start, int end) {
		List<Integer> familyIdList = new ArrayList<Integer>();
		if (type == FamilyRankingEnum.RANKING_TYPE_FAMILY_RICH) {
			/** 先查询出 排好序的familyId */
			Set<String> familySet = FamilyHonorSource.getFamilyHonorList(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_CONSUMETOTAL, start, end);
			for (String familyId : familySet) {
				familyIdList.add(Integer.parseInt(familyId));
			}
		} else if (type == FamilyRankingEnum.RANKING_TYPE_FAMILY_POPULAR) {
			Set<String> familySet = FamilyHonorSource.getFamilyHonorList(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_MEMBERCOUNT, start, end);
			for (String familyId : familySet) {
				familyIdList.add(Integer.parseInt(familyId));
			}
		} else if (type == FamilyRankingEnum.RANKING_TYPE_FAMILY_TOTALLIVE) {
			Set<String> familySet = FamilyHonorSource.getFamilyHonorList(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_TOTALLIVE, start, end);
			for (String familyId : familySet) {
				familyIdList.add(Integer.parseInt(familyId));
			}
		} else if (type == FamilyRankingEnum.RANKING_TYPE_FAMILY_CROWN) {
			Set<String> familySet = FamilyHonorSource.getFamilyHonorList(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_CROWNCOUNT, start, end);
			for (String familyId : familySet) {
				familyIdList.add(Integer.parseInt(familyId));
			}
		} else if (type == FamilyRankingEnum.RANKING_TYPE_FAMILY_DIAMOND) {
			Set<String> familySet = FamilyHonorSource.getFamilyHonorList(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_DIAMONDCOUNT, start, end);
			for (String familyId : familySet) {
				familyIdList.add(Integer.parseInt(familyId));
			}
		} else if (type == FamilyRankingEnum.RANKING_TYPE_FAMILY_HEART) {
			Set<String> familySet = FamilyHonorSource.getFamilyHonorList(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_HEARTCOUNT, start, end);
			for (String familyId : familySet) {
				familyIdList.add(Integer.parseInt(familyId));
			}
		} else if (type == FamilyRankingEnum.RANKING_TYPE_FAMILY_MEDAL) {
			Set<String> familySet = FamilyHonorSource.getFamilyHonorList(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_MEDALCOUNT, start, end);
			for (String familyId : familySet) {
				familyIdList.add(Integer.parseInt(familyId));
			}
		} else if (type == FamilyRankingEnum.RANKING_TYPE_FAMILY_WEEKLYCONSUME) {
			Set<String> familySet = FamilyHonorSource.getFamilyHonorList(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_WEEKLYCONSUME, start, end);
			for (String familyId : familySet) {
				familyIdList.add(Integer.parseInt(familyId));
			}
		}
		return familyIdList;
	}
	
	/**
	 * 刷新mongodb中家族族长及副族长信息
	 * @param familyId
	 * @return
	 */
	private static void refreshFamilyManager(int familyId) {
		// 根据家族成员刷新时间,判断是否需要刷新家族成员
		long currentTime = System.currentTimeMillis();
		BasicDBObject resultObj = (BasicDBObject) CommonDB.getInstance(CommonDB.CACHEDB).getCollection(CollectionEnum.FAMILYMEMBERREFRESHRECORD)
				.findOne(new BasicDBObject("familyId", familyId));
		long refreshTime = 0;
		if (resultObj != null) refreshTime = resultObj.getLong("refreshTime");
		if (!(refreshTime > 0 && (currentTime-refreshTime) < ConfigHelper.getPeriodFamilyMemberUpdateTime())) {
			try {
				Map<Object, Object> map = new HashMap<Object, Object>();
				map.put("familyId", familyId);
				SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Family.getFamilyManagerList", map);
				String TagCode = (String) map.get("TagCode");
				if (TagCode.equals(TagCodeEnum.SUCCESS)) {
					@SuppressWarnings("unchecked")
					List<FamilyMember> memberList = (List<FamilyMember>) map.get("memberList");
					for (FamilyMember familyMember : memberList) {
						DBObject queryObj = new BasicDBObject();
						queryObj.put("familyId", familyId);
						queryObj.put("userId", familyMember.getUserId());
						DBObject updateObj = familyMember.toDBObject();
						CommonDB.getInstance(CommonDB.COMMONDB).getCollection(CollectionEnum.FAMILYMEMBER).update(queryObj, 
								new BasicDBObject("$set", updateObj), true, false);
					}
					// 更新家族成员刷新时间
					CommonDB.getInstance(CommonDB.CACHEDB).getCollection(CollectionEnum.FAMILYMEMBERREFRESHRECORD)
						.update(new BasicDBObject("familyId", familyId), 
								new BasicDBObject("$set", new BasicDBObject("refreshTime", currentTime)),
								true, false);
				} else {
					logger.error("调用存储过程(Family.getFamilyManagerList)未的到正常结果, TagCode:" + TagCode + ",familyId:" + familyId);
				}
			} catch (SQLException e) {
				logger.error("未能正常调用存储过程", e);
			}
		}
	}
	
	/**
	 * 获取家族族长信息
	 * @param familyId
	 * @return
	 */
	public static FamilyMember getFamilyLeader(int familyId, int platform) {
		refreshFamilyManager(familyId);
		
		// 获取家族族长
		DBObject queryLeaderObj = new BasicDBObject();
		queryLeaderObj.put("familyId", familyId);
		queryLeaderObj.put("memberGrade", FamilyMemberEnum.GRADE_LEADER);
		DBObject leaderObj = (BasicDBObject) CommonDB.getInstance(CommonDB.COMMONDB)
				.getCollection(CollectionEnum.FAMILYMEMBER).findOne(queryLeaderObj);
		if (leaderObj != null) {
			FamilyMember fMember = new FamilyMember();
			fMember.initJavaBean(leaderObj, platform);
			return fMember;
		}
		return null;
	}
	
	/**
	 * 获取家族副族长
	 * @param familyId
	 * @return
	 */
	public static List<FamilyMember> getFamilyDeputy(int familyId, int platform) {
		refreshFamilyManager(familyId);
		
		List<FamilyMember> familyDeputy = new ArrayList<FamilyMember>();
		// 获取家族副族长
		DBObject queryDeputyObj = new BasicDBObject();
		queryDeputyObj.put("familyId", familyId);
		queryDeputyObj.put("memberGrade", FamilyMemberEnum.GRADE_DEPUTY);
		DBCursor deputyCur = (DBCursor) CommonDB.getInstance(CommonDB.COMMONDB).getCollection(CollectionEnum.FAMILYMEMBER)
				.find(queryDeputyObj).limit(Constant.return_deputy_count);
		for (DBObject deputyObj : deputyCur) {
			FamilyMember fMember = new FamilyMember();
			fMember.initJavaBean(deputyObj, platform);
			familyDeputy.add(fMember);
		}
		return familyDeputy;
	}
	
	/**
	 * 获取家族基本信息
	 * @param familyId
	 * @param platform
	 * @return
	 */
	public static Family getFamilyInfo(int familyId, int platform) {
		BasicDBObject dbObj = (BasicDBObject) CommonDB.getInstance(CommonDB.COMMONDB)
				.getCollection(CollectionEnum.FAMILYLIST)
				.findOne(new BasicDBObject("familyId", familyId));
		if (dbObj != null) {
			Family family = new Family();
			family.initJavaBean(dbObj, platform);
			return family;
		}
		return null;
	}
	
	
	public static JsonObject getFamilyPoster(String familyPosterStr,Integer platform){
		//方法中增加对是否是家族海报还是房间海报，两种海报的格式不同，需要区分判断。
		JsonObject familyPosterJson = null ;
		//familyPosterStr = "{"path_original":"/familyposter/2015/03/16/10/7504269_214.jpg"}";
		if(familyPosterStr != null && familyPosterStr.contains("path_original")) {
			//说明是家族海报的格式，直接使用类
			try {
				FamilyPoster familyPoster = new Gson().fromJson(familyPosterStr, FamilyPoster.class);
				switch (platform) {
				case PlatformEnum.WEB:
					// 返回 222*148px 270*180px
					if (familyPoster.getPath_original() != null) {
						familyPosterJson = new JsonObject();
						familyPosterJson.addProperty("path_222", familyPoster.getPath_222());
						familyPosterJson.addProperty("path_270", familyPoster.getPath_270());
					}
					break;
				case PlatformEnum.ANDROID:
					// 返回 174*116px
					if (familyPoster.getPath_original() != null) {
						familyPosterJson = new JsonObject();
						familyPosterJson.addProperty("path_174", familyPoster.getPath_174());
					}
				case PlatformEnum.IPHONE:
					// 返回 222*148px
					if (familyPoster.getPath_original() != null) {
						familyPosterJson = new JsonObject();
						familyPosterJson.addProperty("path_222", familyPoster.getPath_222());
					}
				case PlatformEnum.IPAD:
					// 返回 222*148px
					if (familyPoster.getPath_original() != null) {
						familyPosterJson = new JsonObject();
						familyPosterJson.addProperty("path_222", familyPoster.getPath_222());
					}
					break;
				default:
					// 返回 174*116px 222*148px 270*180px
					if (familyPoster.getPath_original() != null) {
						familyPosterJson = new JsonObject();
						familyPosterJson.addProperty("path_174", familyPoster.getPath_174());
						familyPosterJson.addProperty("path_222", familyPoster.getPath_222());
						familyPosterJson.addProperty("path_270", familyPoster.getPath_270());
					}
					break;
					
				}
			} catch (Exception e) {
			    logger.error("获取海报路径失败!",e);
			}
		}else {
			try {
				FamilyPoster familyPoster = new FamilyPoster();
				familyPoster.setPath_original(familyPosterStr);
				//FamilyPoster familyPoster = new Gson().fromJson(familyPosterStr, FamilyPoster.class);
				switch (platform) {
				case PlatformEnum.WEB:
					// 返回 222*148px 270*180px
					if (familyPosterStr!= null) {
						familyPosterJson = new JsonObject();
						familyPosterJson.addProperty("path_222", familyPoster.getPath_222());
						familyPosterJson.addProperty("path_270", familyPoster.getPath_270());
					}
					break;
				case PlatformEnum.ANDROID:
					// 返回 174*116px
					if (familyPoster.getPath_original() != null) {
						familyPosterJson = new JsonObject();
						familyPosterJson.addProperty("path_174", familyPoster.getPath_174());
					}
				case PlatformEnum.IPHONE:
					// 返回 222*148px
					if (familyPoster.getPath_original() != null) {
						familyPosterJson = new JsonObject();
						familyPosterJson.addProperty("path_222", familyPoster.getPath_222());
					}
				case PlatformEnum.IPAD:
					// 返回 222*148px
					if (familyPoster.getPath_original() != null) {
						familyPosterJson = new JsonObject();
						familyPosterJson.addProperty("path_222", familyPoster.getPath_222());
					}
					break;
				default:
					// 返回 174*116px 222*148px 270*180px
					if (familyPoster.getPath_original() != null) {
						familyPosterJson = new JsonObject();
						familyPosterJson.addProperty("path_174", familyPoster.getPath_174());
						familyPosterJson.addProperty("path_222", familyPoster.getPath_222());
						familyPosterJson.addProperty("path_270", familyPoster.getPath_270());
					}
					break;
					
				}
			} catch (Exception e) {
				logger.error("获取海报路径失败!",e);
			}
		}
		
		return familyPosterJson ;
	}
	
	/**
	 * 获取家族荣誉
	 * 
	 * @param familyId
	 * @return
	 */
	public static FamilyHonor getFamilyHonor(int familyId) {
		FamilyHonor familyHonor = null;
		Map<String, Long> familyStats = FamilyHonorSource.getFamilyHonor(String.valueOf(familyId));
		if (familyStats != null && !familyStats.isEmpty()) {
			familyHonor = new FamilyHonor();
			// 1) 获取家族消费总和 consumeTotal
			familyHonor.setConsumeTotalRank(familyStats.get(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_CONSUMETOTAL));
			// 2) 勋章成员排行榜（家族购买勋章成员数） medalCount
			familyHonor.setMedalCountRank(familyStats.get(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_MEDALCOUNT));
			// 3) 家族人气排行榜（家族成员数）memberCount
			familyHonor.setMemberCountRank(familyStats.get(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_MEMBERCOUNT));
			// 4) 直播时长排行榜（家族主播直播时长总和）totalLive
			familyHonor.setTotalLiveRank(familyStats.get(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_TOTALLIVE));
			// 5) 超冠主播排行榜（家族超冠主播数）crownCount
			familyHonor.setCrownCountRank(familyStats.get(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_CROWNCOUNT));
			// 6) 巨钻主播排行榜（家族巨钻主播数）diamondCount
			familyHonor.setDiamondCountRank(familyStats.get(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_DIAMONDCOUNT));
			// 7) 红心主播排行榜（家族红心主播数）heartCount
			familyHonor.setHeartCountRank(familyStats.get(RedisServiceKey.SERVICE_KEY_FAMILYSTATS_HEARTCOUNT));
		}
		return familyHonor;
	}
	
	/**
	 * 获取家族内排行榜
	 * @param rankType
	 * @param slotType
	 * @param familyId
	 * @return
	 */
	public static JsonArray getfamilyMemberRank(int rankType, int slotType, int familyId) {
		JsonArray roomList = new JsonArray();
//		String rankingStr = FamilyHonorSource.getFamilyUserRanking(rankType, slotType, familyId);
		
		// 新版
		String rankingStr = FamilyHonorSource.getFamilyUserRankingNew(rankType, slotType, familyId);
		if (!StringUtil.strIsNull(rankingStr)) {
			JsonArray jRoomList = new JsonArray();
			try {
				jRoomList = new JsonParser().parse(rankingStr).getAsJsonArray();
			} catch (Exception e) {
				logger.error("fail to parse string to json array", e);
			}
			if (jRoomList != null && jRoomList.size() > 0) {
				for (JsonElement jelement : jRoomList) {
					JsonObject obj = jelement.getAsJsonObject();
					if (obj.get("userId") != null) {
						int userId = obj.get("userId").getAsInt();
						// 判断是否已经退出家族
						String familyIdStr = FamilySource.getMemberFamily(String.valueOf(userId));
						if (familyIdStr == null || !familyIdStr.equals(String.valueOf(familyId)))
							continue;
						// 读取明星等级
						obj.addProperty("actorLevel", UserService.getActorLevel(userId));
						// 读取富豪等级
						obj.addProperty("richLevel", UserService.getRichLevel(userId));
						// 读取星级
						obj.addProperty("starLevel", UserService.getStarLevel(userId));
						// 读取靓号
						JsonObject validVirtualId = UserAssetServices.getValidVirtualId(userId); // 获取用户虚拟账号
						if (validVirtualId != null) {
							if (validVirtualId.get("idType").getAsInt() == 1) {
								// 支持老版靓号
								obj.addProperty("luckyId", validVirtualId.get("id").getAsInt());
							}
							obj.add("validId", validVirtualId);
						}
						// 默认kk唱响用户
						obj.addProperty("roomSource", AppIdEnum.AMUSEMENT);
						obj.addProperty("roomType", AppIdEnum.AMUSEMENT);
						roomList.add(jelement);
					}
				}
			}
		}
		return roomList;
	}
	
	/**
	 * 获取家族内房间总数
	 * @param familyId
	 * @return
	 */
	@Deprecated
	public static long getFamilyRoomTotalCount(int familyId) {
		return FamilySource.countFamilyRoomSet(familyId);
	}
	
	public static int getFamilyRoomTotalCount(int appId, int familyId) {
		RoomInfoService roomInfoServie = MelotBeanFactory.getBean("roomInfoService", RoomInfoService.class);
		return roomInfoServie.getRoomCountByFamilyId(appId, familyId);
	}
	
	/**
	 * 获取家族内直播间列表
	 * @param dbObject
	 * @param start
	 * @param offset
	 * @param platform
	 * @return
	 */
	public static JsonArray getFamilyRoomList(int familyId, int start, int offset, int platform) {
		
		// 定义返回结果
		JsonArray jRoomList = new JsonArray();
		
		List<Integer> userIdList = new ArrayList<Integer>();
		StringBuffer stringBuffer = new StringBuffer();
		
		// 读取列表
		Set<String> userIdSet = FamilySource.getFamilyRoomSet(familyId, start, offset);
		for (String userIdString : userIdSet) {
			int userId = Integer.parseInt(userIdString);
			userIdList.add(userId);
			stringBuffer.append(userId).append(",");
		}
		if (userIdList.size() > 0) {
			String userIdString = stringBuffer.toString();
			userIdString = userIdString.substring(0, userIdString.length() - 1);
			
			List<RoomInfo> roomList = RoomService.getRoomListByRoomIds(userIdString);
			if (roomList != null) {
				Map<Integer, RoomInfo> roomMap = new HashMap<Integer, RoomInfo>();
				for (RoomInfo roomInfo : roomList) {
					if (roomInfo.getActorId() != null) {
						roomMap.put(roomInfo.getActorId(), roomInfo);
					}
				}
				for (Integer roomId : userIdList) {
					if (roomMap.containsKey(roomId)) {
						jRoomList.add(RoomTF.roomInfoToJsonTemp(roomMap.get(roomId), platform));
					}
				}
			}
		}
		
		return jRoomList;
	}
	
	public static List<RoomInfo> getFamilyActorRoomList(int appId, int familyId, int start, int offset) {
		
		RoomInfoService roomInfoServie = MelotBeanFactory.getBean("roomInfoService", RoomInfoService.class);
		if (roomInfoServie != null) {
			return roomInfoServie.getRoomListByFamilyId(appId, familyId, start, offset);
			
		}
		
		return null;
	}
	
	/**
	 * 获取用户已加入家族ID
	 * @param userId
	 * @return
	 */
	public static int getUserJoinedFamilyId(int userId) {
		String familyId = FamilySource.getMemberFamily(String.valueOf(userId));
		if (familyId != null) {
			return Integer.parseInt(familyId);
		}
		return 0;
	}
	
	/**
	 * 获取用户正在申请家族ID
	 * @param userId
	 * @return
	 */
	public static int getUserApplyingFamilyId(int userId) {
		String familyId = FamilyApplySource.getApplyFamily(String.valueOf(userId));
		if (familyId != null) {
			return Integer.parseInt(familyId);
		}
		return 0;
	}
	
	/**
	 * 申请加入家族
	 * @param userId
	 * @param familyId
	 * @return
	 */
	public static Map<String, Object> applyJoinFamily(int userId, int familyId) {
		
		Map<String, Object> resMap = new HashMap<String, Object>();
		
		try {
			Map<Object, Object> map = new HashMap<Object, Object>();
			map.put("familyId", familyId);
			map.put("userId", userId);
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Family.applyJoinFamily", map);
			String TagCode = (String) map.get("TagCode");
			resMap.put("TagCode", TagCode);
			if (TagCode.equals(TagCodeEnum.SUCCESS)) {
				// 取消之前的家族加入申请
				FamilyApplySource.cancelJoinFamily("*", String.valueOf(userId));
				// 申请加入家族
				FamilyApplySource.applyJoinFamily(String.valueOf(familyId), String.valueOf(userId));
			}
		} catch (Exception e) {
			logger.error("fail to execute procedure Family.applyJoinFamily", e);
		}
		
		return resMap;
	}
	
	/**
	 * 获取用户家族成员信息
	 * @param userId
	 * @param familyId
	 * @param platform
	 * @return
	 */
	public static FamilyMember getFamilyMemberInfo(int userId, int familyId, int platform) {
		
		refreshFamilyManager(familyId);
		
		DBObject queryObj = new BasicDBObject();
		queryObj.put("familyId", familyId);
		queryObj.put("userId", userId);
		DBObject memberDBObj = CommonDB.getInstance(CommonDB.COMMONDB)
				.getCollection(CollectionEnum.FAMILYMEMBER).findOne(queryObj);
		if (memberDBObj != null) {
			FamilyMember fMember = new FamilyMember();
			fMember.initJavaBean(memberDBObj, platform);
			return fMember;
		}
		return null;
	}
	
	/**
	 * 更新家族成员个数及主播个数
	 * @param familyId
	 * @param memberCount
	 * @param actorCount
	 */
	private static void updateFamilyMemberCount(int familyId, int memberCount, int actorCount) {
		DBObject updateDBObj = new BasicDBObject();
		updateDBObj.put("memberCount", memberCount);
		updateDBObj.put("actorCount", actorCount);
		CommonDB.getInstance(CommonDB.COMMONDB).getCollection(CollectionEnum.FAMILYLIST).update(
				new BasicDBObject("familyId", familyId),
				new BasicDBObject("$set", updateDBObj),
				false, false);
		
		try {
		    FamilyAdminService familyAdminService = (FamilyAdminService) MelotBeanFactory.getBean("familyAdminService");
	        if (familyAdminService != null) {
	            FamilyInfo familyInfo = new FamilyInfo();
	            familyInfo.setFamilyId(familyId);
	            familyInfo.setMemberCount(memberCount);
	            familyInfo.setActorCount(actorCount);
	            familyAdminService.updateFamilyInfo(familyInfo);
	        }
		} catch (Exception e) {
		    logger.info("fail to familyAdminService.updateFamilyInfo()", e);
		}
	}
	
	/**
	 * 删除家族中成员
	 * @param familyId
	 * @param userId
	 */
	private static void deleteFamilyMember(int familyId, int userId) {
		DBObject queryObj = new BasicDBObject();
		queryObj.put("familyId", familyId);
		queryObj.put("userId", userId);
		CommonDB.getInstance(CommonDB.COMMONDB).getCollection(CollectionEnum.FAMILYMEMBER).remove(queryObj);
	}
	
	/**
	 * 更新mongodb的userList中familyId字段
	 * @param userId
	 * @param familyId null/integer
	 */
	private static void updateUserFamilyId(int userId, Integer familyId) {
		CommonDB.getInstance(CommonDB.COMMONDB)
				.getCollection(CollectionEnum.USERLIST)
				.update(new BasicDBObject("userId", userId),
						new BasicDBObject("$set", new BasicDBObject("familyId", familyId)), false, false);
	}
	
	/**
	 * 用户退出家族
	 * @param userId
	 * @param familyId
	 * @param familyMedal
	 * @return Map <String, Object>
	 * TagCode:返回码
	 */
	public static Map<String, Object> quitFamily(int userId, int familyId, Integer familyMedal) {
		
		Map<String, Object> resMap = new HashMap<String, Object>();
		
		try {
			Map<Object, Object> map = new HashMap<Object, Object>();
			map.put("familyId", familyId);
			map.put("userId", userId);
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Family.quitFamily", map);
			String TagCode = (String) map.get("TagCode");
			resMap.put("TagCode", TagCode);
			if (TagCode.equals(TagCodeEnum.SUCCESS)) {
				// 更新家族成员个数和家族主播个数
				Integer memberCount = (Integer) map.get("memberCount");
				Integer actorCount = (Integer) map.get("actorCount");
				if (memberCount != null && actorCount != null) {
					updateFamilyMemberCount(familyId, memberCount.intValue(), actorCount.intValue());
				}
				// 更新mongodb的userList中familyId字段
				updateUserFamilyId(userId, null);
				// 删除MONGODB副族长
				deleteFamilyMember(familyId, userId);
				// 删除REDIS用户家族
				FamilySource.delFamilyMember(String.valueOf(userId));
				// 家族勋章失效
				try {
					if (familyMedal != null) {
						MedalSource.delUserMedal(userId, familyMedal.intValue());
					}
				} catch (Exception e) {
					logger.error("FamilyService.quitFamily(delUserMedal) exception, userId : " + userId 
							+ " ,familyMedal : " + familyMedal, e);
				}
			}
		} catch (Exception e) {
			logger.error("fail to execute procedure Family.quitFamily", e);
		}
		
		return resMap;
	}
	
	/**
	 * 设置家族公告
	 * @param familyId
	 * @param notice
	 * @return Map <String, Object>
	 * TagCode:返回码
	 */
	public static Map<String, Object> setFamilyNotice(int familyId, String notice) {
		
		Map<String, Object> resMap = new HashMap<String, Object>();
		
		try {
			Map<Object, Object> map = new HashMap<Object, Object>();
			map.put("familyId", familyId);
			map.put("notice", notice);
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Family.setFamilyNotice", map);
			String TagCode = (String) map.get("TagCode");
			resMap.put("TagCode", TagCode);
			if (TagCode.equals(TagCodeEnum.SUCCESS)) {
				// 设置MONGODB中家族公告
				CommonDB.getInstance(CommonDB.COMMONDB).getCollection(CollectionEnum.FAMILYLIST)
						.update(new BasicDBObject("familyId", familyId),
								new BasicDBObject("$set",new BasicDBObject("familyNotice", notice)),
								false, false);
			}
		} catch (SQLException e) {
			logger.error("fail to execute procedure Family.setFamilyNotice", e);
		}
		
		return resMap;
	}
	
	/**
	 * 设置家族海报
	 * @param familyId
	 * @param userId
	 * @param familyPoster
	 * @return Map <String, Object>
	 * TagCode:返回码
	 */
	public static Map<String, Object> setFamilyPoster(int familyId, FamilyPoster familyPoster) {
		
		Map<String, Object> resMap = new HashMap<String, Object>();
		
		String posterJsonString = new Gson().toJson(familyPoster);
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("familyId", familyId);
		map.put("posterJsonStr", posterJsonString);
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Family.setFamilyPoster", map);
			String TagCode = (String) map.get("TagCode");
			resMap.put("TagCode", TagCode);
			if (TagCode.equals(TagCodeEnum.SUCCESS)) {
				// 更新MONGODB中的家族海报
				CommonDB.getInstance(CommonDB.COMMONDB)
						.getCollection(CollectionEnum.FAMILYLIST)
						.update(new BasicDBObject("familyId", familyId),
								new BasicDBObject("$set", new BasicDBObject("familyPoster", posterJsonString)), false, false);
			}
		} catch (SQLException e) {
			logger.error("fail to execute procedure Family.setFamilyPoster", e);
		}
		
		return resMap;
	}
	
	/**
	 * 搜索家族成员
	 * @param familyId
	 * @param pageIndex
	 * @param countPerPage
	 * @param actorTag
	 * @param fuzzyString
	 * @return Map <String, Object>
	 * TagCode:返回码
	 * pageTotal:总页数,
	 * memberList:成员列表
	 */
	public static Map<String, Object> searchFamilyMember(int familyId, int pageIndex, int countPerPage,
	        int actorTag, String fuzzyString) {
		
		Map<String, Object> resMap = new HashMap<String, Object>();
		
		try {
			Map<Object, Object> map = new HashMap<Object, Object>();
			map.put("familyId", familyId);
			map.put("pageIndex", pageIndex);
			map.put("countPerPage", countPerPage);
			if (actorTag != -1) {
			    map.put("actorTag", actorTag);
			}
			if (fuzzyString != null) {
				boolean isTrue = Pattern.compile(Constant.regx_user_id).matcher(fuzzyString).find();
				if (isTrue) {
					String idString = fuzzyString;
					Integer tempId = UserAssetServices.luckyIdToUserId(StringUtil.parseFromStr(idString, 0));
					if (tempId != null && tempId > 0) {
						idString = String.valueOf(tempId);
						map.put("idString", Integer.valueOf(idString));
					} else {
						if (idString.length() >= 7) {
							map.put("idString", Integer.valueOf(idString));
						} else {
							map.put("fuzzyString", fuzzyString);
						}
					}
				} else {
					map.put("fuzzyString", fuzzyString);
				}
			}
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Family.getFamilyMemberList", map);
			String TagCode = (String) map.get("TagCode");
			resMap.put("TagCode", TagCode);
			if (TagCode.equals(TagCodeEnum.SUCCESS)) {
				// 成员总数
				Integer total = (Integer) map.get("total");
				if (total != null && total.intValue() > 0) {
					resMap.put("pageTotal", CommonUtil.getPageTotal(total.intValue(), countPerPage));
					@SuppressWarnings("unchecked")
					List<FamilyMember> memberList = (List<FamilyMember>) map.get("memberList");
					resMap.put("memberList", memberList);
				} else {
					resMap.put("pageTotal", 0l);
				}
			}
		} catch (SQLException e) {
			logger.error("fail to execute procedure Family.getFamilyMemberList", e);
		}
		
		return resMap;
	}
	
	/**
	 * 删除家族成员
	 * 
	 * @param userId
	 * @param userIds
	 * @param family
	 * @return Map <String, Object>
	 * TagCode:返回码
	 * notPassUserids:删除失败用户
	 */
	public static Map<String, Object> removeFamilyMember(int userId, String userIds, Family family) {
		
		Map<String, Object> resMap = new HashMap<String, Object>();
		
		try {
			Map<Object, Object> map = new HashMap<Object, Object>();
			map.put("familyId", family.getFamilyId());
			map.put("userId", userId);
			map.put("userIds", userIds);
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Family.removeFamilyMember", map);
			String TagCode = (String) map.get("TagCode");
			resMap.put("TagCode", TagCode);
			if (TagCode.equals(TagCodeEnum.SUCCESS)) {
				
				FamilyMember familyMember = getFamilyMemberInfo(userId, family.getFamilyId().intValue(), 0);
				
				// 更新家族成员个数和家族主播个数
				Integer memberCount = (Integer) map.get("memberCount");
				Integer actorCount = (Integer) map.get("actorCount");
				if (memberCount != null && actorCount != null) {
					updateFamilyMemberCount(family.getFamilyId().intValue(), memberCount.intValue(), actorCount.intValue());
				}
				
				List<String> notPassUseridsList = new ArrayList<String>();
				String notPassUserids = null;
				if (map.containsKey("notPassUserids")
						&& (String) map.get("notPassUserids") != null) {
					notPassUserids = (String) map.get("notPassUserids");
					String[] notPassUseridsArr = notPassUserids.split(",");
					notPassUseridsList = Arrays.asList(notPassUseridsArr);
					resMap.put("notPassUserids", notPassUserids);
				}
				
				List<Integer> noticeIds = new LinkedList<Integer>();
				for (String uid : userIds.split(",")) {
					Integer i_userId = Integer.valueOf(uid);
					if (!String.valueOf(userId).equals(uid) && !notPassUseridsList.contains(uid)) {
						// 更新mongodb的userList
						updateUserFamilyId(i_userId, null);
						// 获取被删成员职位
						DBObject qObj = new BasicDBObject();
						qObj.put("familyId", family.getFamilyId());
						qObj.put("userId", i_userId);
						int count = CommonDB.getInstance(CommonDB.COMMONDB).getCollection(CollectionEnum.FAMILYMEMBER).find(qObj).count();
						if (count > 0) {
							// 删除MONGODB副族长
							if (familyMember.getMemberGrade().intValue() == FamilyMemberEnum.GRADE_LEADER) { 
								deleteFamilyMember(family.getFamilyId().intValue(), i_userId.intValue());
								// 删除REDIS用户家族
								FamilySource.delFamilyMember(uid);
							}
						} else {
							// 删除REDIS用户家族
							FamilySource.delFamilyMember(uid);
						}
						// 家族勋章失效
						try {
							if (family.getFamilyMedal() != null) {
								MedalSource.delUserMedal(i_userId.intValue(), family.getFamilyMedal().intValue());
							}
						} catch (Exception e) {
							logger.error("Fail to delete redis user medal Info", e);
						}
						noticeIds.add(i_userId);
					} else {
						updateUserFamilyId(i_userId, family.getFamilyId());
					}
				}
				
				// 给被踢出家族的用户发送勋章失效消息
				if (noticeIds.size() > 0) {
					MessageService.genMedalInvalidMsg(noticeIds, userId, family.getFamilyId(), family.getFamilyName());
				}
			}
		} catch (SQLException e) {
			logger.error("fail to call removeFamilyMember!", e);
		}
		
		return resMap;
	}
	
	/**
	 * 设置家族成员级别(副族长 1/普通成员 0)
	 * 
	 * @param familyId
	 * @param userId
	 * @param memberId
	 * @param memberGrade
	 * @return Map <String, Object>
	 * TagCode:返回码
	 */
	public static Map<String, Object> updateFamilyMemberGrade(int familyId, int userId, int memberId, int memberGrade) {
		
		Map<String, Object> resMap = new HashMap<String, Object>();
		
		try {
			Map<Object, Object> map = new HashMap<Object, Object>();
			map.put("familyId", familyId);
			map.put("userId", userId);
			map.put("memberId", memberId);
			map.put("memberGrade", memberGrade);
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Family.updateMemberGrade", map);
			String TagCode = (String) map.get("TagCode");
			resMap.put("TagCode", TagCode);
			if (TagCode.equals(TagCodeEnum.SUCCESS)) {
				@SuppressWarnings("unchecked")
				List<FamilyMember> memberList = (ArrayList<FamilyMember>) map.get("memberList");
				for (FamilyMember familyMember : memberList) {
					if (memberGrade == FamilyMemberEnum.GRADE_COMMON) {
						// 撤销族长权限 删除mongodb的familyMember集合记录
						DBObject qObj = new BasicDBObject();
						qObj.put("familyId", familyMember.getFamilyId());
						qObj.put("memberId", familyMember.getMemberId());
						CommonDB.getInstance(CommonDB.COMMONDB).getCollection(CollectionEnum.FAMILYMEMBER).remove(qObj);
					}
					if (memberGrade == FamilyMemberEnum.GRADE_DEPUTY ||
							memberGrade == FamilyMemberEnum.GRADE_LEADER) {
						// 更新MONGODB成员级别
						DBObject qObj = new BasicDBObject();
						qObj.put("familyId", familyMember.getFamilyId());
						qObj.put("memberId", familyMember.getMemberId());
						CommonDB.getInstance(CommonDB.COMMONDB)
								.getCollection(CollectionEnum.FAMILYMEMBER)
								.update(qObj, new BasicDBObject("$set", familyMember.toDBObject()), true, false);
					}
				}
			}
		} catch (SQLException e) {
			logger.error("fail to call updateMemberGrade!", e);
		}
		
		return resMap;
	}
	
	/**
	 * 搜索申请加入家族用户
	 * 
	 * @param familyId
	 * @param pageIndex
	 * @param countPerPage
	 * @param actorTag
	 * @param fuzzyString
	 * @return Map <String, Object>
	 * TagCode:返回码
	 * pageTotal:总页数,
	 * applicantList:成员列表
	 */
	public static Map<String, Object> searchFamilyApplicant(int familyId, int pageIndex, int countPerPage,
			int actorTag, String fuzzyString) {

		Map<String, Object> resMap = new HashMap<String, Object>();
		
		try {
			Map<Object, Object> map = new HashMap<Object, Object>();
			map.put("familyId", familyId);
			map.put("pageIndex", pageIndex);
			map.put("countPerPage", countPerPage);
			map.put("applyState", FamilyMemberEnum.STATE_APPLYING);
			if (actorTag > 0) {
				map.put("actorTag", actorTag);
			}
			if (fuzzyString != null) {
				if (Pattern.compile(Constant.regx_user_id).matcher(fuzzyString)
						.find()) {
					String idString = fuzzyString;
					Integer tempId = UserAssetServices.luckyIdToUserId(StringUtil.parseFromStr(idString, 0));
                    if (tempId != null && tempId > 0) {
                        idString = String.valueOf(tempId);
						map.put("idString", Integer.valueOf(idString));
					} else {
						if (idString.length() >= 7) {
							map.put("idString", Integer.valueOf(idString));
						} else {
							map.put("fuzzyString", fuzzyString);
						}
					}
				} else {
					map.put("fuzzyString", fuzzyString);
				}
			}
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Family.getFamilyApplicantList", map);
			String TagCode = (String) map.get("TagCode");
			resMap.put("TagCode", TagCode);
			if (TagCode.equals(TagCodeEnum.SUCCESS)) {
				
				// 成员总数
				Integer total = (Integer) map.get("total");
				if (total != null && total.intValue() > 0) {
					resMap.put("pageTotal", CommonUtil.getPageTotal(total.intValue(), countPerPage));
					@SuppressWarnings("unchecked")
					List<FamilyApplicant> applicantList = (List<FamilyApplicant>) map.get("applicantList");
					resMap.put("applicantList", applicantList);
				} else {
					resMap.put("pageTotal", 0l);
				}
				
				// 申请加入家族数据错误问题同步
				total = total == null ? 0 : total;
				Integer applyCount = FamilyApplySource.getApplicantCount(String.valueOf(familyId));
				applyCount = applyCount == null ? 0 : applyCount;
				if (applyCount.intValue() != total.intValue()) {
				    // 同步错误数据 ,以oracle为准
				    new ApplyFamilyUsersToRedis(familyId).start();
				}
			}
		} catch (SQLException e) {
			logger.error("fail to execute procedure Family.getFamilyMemberList", e);
		}

		return resMap;
	}
	
	/**
	 * 批量同意加入家族
	 * @param familyId
	 * @param userIds
	 * @param userId 组长或副组长编号
	 * @return Map <String, Object>
	 * TagCode:返回码
	 * notPassUserids:删除失败用户
	 */
	public static Map<String, Object> agreeJoinFamily(int userId, String userIds, Family family) {
		
		Map<String, Object> resMap = new HashMap<String, Object>();
		
		try {
			Map<Object, Object> map = new HashMap<Object, Object>();
			map.put("familyId", family.getFamilyId());
			map.put("userIds", userIds);
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Family.agreeJoinFamily", map);
			String TagCode = (String) map.get("TagCode");
			resMap.put("TagCode", TagCode);
			if (TagCode.equals(TagCodeEnum.SUCCESS)) {
				
				// 更新家族成员个数和家族主播个数
				Integer memberCount = (Integer) map.get("memberCount");
				Integer actorCount = (Integer) map.get("actorCount");
				if (memberCount != null && actorCount != null) {
					updateFamilyMemberCount(family.getFamilyId().intValue(), memberCount.intValue(), actorCount.intValue());
				}
				
				List<String> notPassUseridsList = new ArrayList<String>();
				if (map.containsKey("notPassUserids")
						&& (String) map.get("notPassUserids") != null) {
					String notPassUserids = (String) map.get("notPassUserids");
					String[] notPassUseridsArr = notPassUserids.split(",");
					notPassUseridsList = Arrays.asList(notPassUseridsArr);
					resMap.put("notPassUserids", notPassUserids);
				}
				
				for (String uid : userIds.split(",")) {
					Integer i_userId = Integer.valueOf(uid);
					if (!String.valueOf(userId).equals(uid) && !notPassUseridsList.contains(uid)) {
						// 更新mongodb的userList
						updateUserFamilyId(i_userId, family.getFamilyId());
						// 删除REDIS
						FamilyApplySource.cancelJoinFamily(family.getFamilyId().toString(), String.valueOf(uid));
						FamilySource.setFamilyMember(family.getFamilyId().toString(), String.valueOf(uid));
						// Mysql中插入familyUser信息
						map.put("userId", i_userId);
						
						// 主播加入家族后自动拥有家族勋章
						boolean isActor = UserService.isActor(i_userId.intValue());
						if (isActor && family.getFamilyMedal() != null) {
							MedalSource.addUserMedal(i_userId, family.getFamilyMedal(), -1);
						}
					}
				}
			}
		} catch (SQLException e) {
			logger.error("fail to call agreeJoinFamily!", e);
		}
		
		return resMap;
	}
	
	/**
	 * 批量拒绝加入家族
	 * @param familyId
	 * @param userIds
	 * @param userId 家族长或副组长编号
	 * @return Map <String, Object>
	 * TagCode:返回码
	 * notPassUserids:删除失败用户
	 */
	public static Map<String, Object> refuseJoinFamily(int userId, String userIds, Family family) {
		
		Map<String, Object> resMap = new HashMap<String, Object>();
		
		try {
			Map<Object, Object> map = new HashMap<Object, Object>();
			map.put("familyId", family.getFamilyId());
			map.put("userIds", userIds);
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Family.refuseJoinFamily", map);
			String TagCode = (String) map.get("TagCode");
			resMap.put("TagCode", TagCode);
			if (TagCode.equals(TagCodeEnum.SUCCESS)) {
				
				List<String> notPassUseridsList = new ArrayList<String>();
				if (map.containsKey("notPassUserids")
						&& (String) map.get("notPassUserids") != null) {
					String notPassUserids = (String) map.get("notPassUserids");
					String[] notPassUseridsArr = notPassUserids.split(",");
					notPassUseridsList = Arrays.asList(notPassUseridsArr);
					resMap.put("notPassUserids", notPassUserids);
				}
				for (String uid : userIds.split(",")) {
					if (!String.valueOf(userId).equals(uid) && !notPassUseridsList.contains(uid)) {
						// 删除REDIS
						FamilyApplySource.cancelJoinFamily(family.getFamilyId().toString(), String.valueOf(uid));
					}
				}
			}
		} catch (SQLException e) {
			logger.error("fail to call refuseJoinFamily!", e);
		}
		
		return resMap;
	}
	
	/**
	 * 根据 familyId 查询出 Family 信息
	 * 
	 * @param familyIdList
	 * @param platform
	 * @return familyList
	 */
	public static List<Family> getFamilyListByIds(List<Integer> familyIdList, int platform) {
		List<Family> familyList = new ArrayList<Family>();
		for (Integer familyId : familyIdList) {
		    DBObject queryObject = new BasicDBObject();
		    queryObject.put("familyId", familyId);
		    queryObject.put("open", new BasicDBObject(QueryOperators.NIN, new int[]{2}));
			DBObject dbObj = CommonDB.getInstance(CommonDB.COMMONDB).getCollection(CollectionEnum.FAMILYLIST).findOne(queryObject);
			if (dbObj != null) {
			    Family family = new Family();
			    family.initJavaBean(dbObj, platform);
			    familyList.add(family);
            }
		}
		return familyList;
	}
	
	/* ------ 申请主播获取家族相关service ------ */
	public static FamilyInfo getFamilyInfoByFamilyId(int familyId) {
		try {
			FamilyInfoService familyInfoService = (FamilyInfoService) MelotBeanFactory.getBean("newFamilyInfoService");
			if (familyInfoService == null) {
				logger.error("FamilyService.getFamilyInfoByFamilyId exception(FamilyService is null), familyId : " + familyId);
				return null;
			}
			return familyInfoService.getFamilyInfoByFamilyId(familyId);
		} catch (Exception e) {
			logger.error("FamilyService.getFamilyInfoByFamilyId exception, familyId : " + familyId);
			return null;
		}
	}

    public static FamilyInfo getFamilyInfoByFamilyId(int familyId, int appId) {
        try {
            FamilyInfoService familyInfoService = (FamilyInfoService) MelotBeanFactory.getBean("newFamilyInfoService");
            if (familyInfoService == null) {
                logger.error("FamilyService.getFamilyInfoByFamilyId exception(FamilyService is null), familyId : " + familyId);
                return null;
            }
            return familyInfoService.getNewFamilyInfoByFamilyId(familyId, appId);
        } catch (Exception e) {
            logger.error("FamilyService.getFamilyInfoByFamilyId exception, familyId : " + familyId);
            return null;
        }
    }
	
	public static List<FamilyInfo> getFamilyListByCityId(int cityId) {
		try {
			FamilyInfoService familyInfoService = (FamilyInfoService) MelotBeanFactory.getBean("newFamilyInfoService");
			if (familyInfoService == null) {
				logger.error("FamilyService.getFamilyListByCityId exception(FamilyService is null), cityId : " + cityId);
				return null;
			}
			
			return familyInfoService.getFamilyListByCityId(cityId);
		} catch (Exception e) {
			logger.error("FamilyService.getFamilyListByCityId exception, cityId : " + cityId, e);
			return null;
		}
	}
	
	/**
	 * 返回消费金额从高到低排序的list集合
	 * @author fenggaopan 2015年10月22日 上午11:37:04
	 * @param count 返回的总数
	 * @return 返回list集合
	 */
	public static List<Honour> getConsumeHonour(Integer count,Integer platform) {
		// 定义返回结果
		Honour honour = null;
		Map<String, Long> consumeMap = new LinkedHashMap<String,Long>();
		List<Honour> hounours = new ArrayList<Honour>();
		try {
			consumeMap = FamilyHonorSource.getConsumeTotal(count) ;
			for (String key : consumeMap.keySet()) {
				// 遍历，拿到key之后，从mongo中获取familyInfo
				// 获取家族基本信息
				honour = new Honour();
				FamilyInfo family = FamilyService.getFamilyInfoByFamilyId(Integer.parseInt(key));
				if(family==null) {
					continue ;
				}
				//增加勋章主题
				if(family.getMedalId()!=null) {
					JsonObject medalJsonObject = MedalSource.getMedalInfoAsJson(family.getMedalId(), platform);
		            if (medalJsonObject != null) {
		            	honour.setMedalTitle(medalJsonObject.get("medalTitle")!=null?medalJsonObject.get("medalTitle").getAsString():"");
		            }
				}
				honour.setFamilyId(family.getFamilyId());
				honour.setFamilyName(family.getFamilyName());
				honour.setTotalCount(consumeMap.get(key));
				honour.setFamilyPoster(FamilyService.getFamilyPoster(family.getFamilyPoster(), platform));
				hounours.add(honour);
			}
		} catch (Exception e) {
			logger.error("FamilyService.getConsumeHonour exception:", e);
		}
		return hounours  ;
	}
	
	/**
	 * 获取勋章数量榜单
	 * @author fenggaopan 2015年10月22日 下午4:16:16
	 * @param count 勋章数量
	 * @param platform 平台id
	 * @param result 返回的结果的json
	 * @return 返回结果
	 */
	public static List<Honour> getMedalHonour(Integer count,Integer platform) {
		// 定义返回结果
		Honour honour = null;
		Map<String, Long> consumeMap = new LinkedHashMap<String,Long>();
		List<Honour> hounours = new ArrayList<Honour>();
		try {
			consumeMap = FamilyHonorSource.getMedalTotal(count);
			for (String key : consumeMap.keySet()) {
				// 遍历，拿到key之后，从mongo中获取familyInfo
				// 获取家族基本信息
				honour = new Honour();
				FamilyInfo family = FamilyService.getFamilyInfoByFamilyId(Integer.parseInt(key));
				if(family!=null) {
					//增加勋章主题
					if(family.getMedalId()!=null) {
						JsonObject medalJsonObject = MedalSource.getMedalInfoAsJson(family.getMedalId(), platform);
			            if (medalJsonObject != null) {
			            	honour.setMedalTitle(medalJsonObject.get("medalTitle")!=null?medalJsonObject.get("medalTitle").getAsString():"");
			            }
					}
					honour.setFamilyId(family.getFamilyId());
					honour.setFamilyName(family.getFamilyName());
					honour.setTotalCount(consumeMap.get(key));
					honour.setFamilyPoster(FamilyService.getFamilyPoster(family.getFamilyPoster(), platform));
					hounours.add(honour);
				}
			}
			
			
		} catch (Exception e) {
			logger.error("FamilyService.getMedalHonour exception:", e);
		}
		return hounours ; 
	}
	
	/**
	 * 获取超冠主播榜单
	 * @author fenggaopan 2015年10月22日 下午4:16:16
	 * @param count 超冠主播数量
	 * @param platform 平台id
	 * @param result 返回的结果的json
	 * @return 返回结果
	 */
	public static List<Honour> getCrownCountHonour(Integer count,Integer platform) {
		// 定义返回结果
		Map<String, Long> consumeMap = new LinkedHashMap<String,Long>();
		Honour honour = null;
		List<Honour> hounours = new ArrayList<Honour>();
		try {
			consumeMap = FamilyHonorSource.getCrownCountTotal(count);
			for (String key : consumeMap.keySet()) {
				// 遍历，拿到key之后，从mongo中获取familyInfo
				// 获取家族基本信息
				honour = new Honour();
				FamilyInfo family = FamilyService.getFamilyInfoByFamilyId(Integer.parseInt(key));
				if (family == null) {
					continue ;
				}
				//增加勋章主题
				if(family.getMedalId()!=null) {
					JsonObject medalJsonObject = MedalSource.getMedalInfoAsJson(family.getMedalId(), platform);
		            if (medalJsonObject != null) {
		            	honour.setMedalTitle(medalJsonObject.get("medalTitle")!=null?medalJsonObject.get("medalTitle").getAsString():"");
		            }
				}
				honour.setFamilyId(family.getFamilyId());
				honour.setFamilyName(family.getFamilyName());
				honour.setTotalCount(consumeMap.get(key));
				honour.setFamilyPoster(FamilyService.getFamilyPoster(family.getFamilyPoster(), platform));
				hounours.add(honour);
			}
		} catch (Exception e) {
			logger.error("FamilyService.getMedalHonour exception:", e);
		}
		return hounours ;
	}
	
	/**
	 * 判断是否已经成为家族成员
	 * @return
	 */
	public static boolean isFamilyMember(int userId) {
		try {
			FamilyAdminService familyAdminService = (FamilyAdminService) MelotBeanFactory.getBean("familyAdminService");
			if (familyAdminService.getFamilyMemberInfo(0, userId) != null) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			logger.error("FamilyService.isFamilyMember excepton, userId: " + userId, e);
		}
		return false;
	}
	
	public static void checkBecomeFamilyMember(int userId, int status, int appId) {
		try {
			FamilyOperatorService familyOperatorService = (FamilyOperatorService) MelotBeanFactory.getBean("familyOperatorService");
			RespMsg respMsg = familyOperatorService.checkBecomeFamilyMember(userId, 0, appId, status, null, null);
			if (respMsg.getTagCode().equals("00000000")) {
				return;
			} else {
				logger.error("FamilyService.checkBecomeFamilyMember fail respTagCode :"
						+ respMsg.getTagCode() + "respMsg : " + respMsg.getRespMsg());
			}
		} catch (Exception e) {
			logger.error("FamilyService.checkBecomeFamilyMember exception, userId :" + userId, e);
		}
	}
    
    public static String checkBecomeFamilyActor(int userId, int status) {
        try {
            FamilyOperatorService familyOperatorService = (FamilyOperatorService) MelotBeanFactory.getBean("familyOperatorService");
            RespMsg respMsg = familyOperatorService.checkBecomeFamilyActor(userId, 0, AppIdEnum.AMUSEMENT, status, null, null);
            if (!respMsg.getTagCode().equals(TagCodeEnum.SUCCESS)) {
                logger.error("FamilyService.checkBecomeFamilyActor fail respTagCode :"
                        + respMsg.getTagCode() + "respMsg : " + respMsg.getRespMsg());
            }
            return respMsg.getTagCode();
        } catch (Exception e) {
            logger.error("FamilyService.checkBecomeFamilyActor exception, userId :" + userId, e);
        }
        
        return TagCodeEnum.FAIL_TO_UPDATE;
    }
	
	/**
	 * 获取首页推荐的家族房的列表
	 * @author fenggaopan 2015年10月22日 下午4:57:53
	 * @param start 起始记录数
	 * @param offset 每页返回的结果的数量
	 * @param platform 平台的id
	 * @param result 待初始化的json结果
	 */
	public static String getToppageLiveRoom(int start, int offset, int platform) {
		try {
			return FamilyRoomSource.getJsonObjectFromRedis();
		} catch (Exception e) {
			logger.error("FamilyService.getToppageLiveRoom exception:", e);
		}
		return null;
	}
	
	/**
	 * 获取用户家族名
	 * @param userId
	 * @param appId
	 * @return
	 */
	public static FamilyInfo getUserFamilyName(int userId, int appId) {
	    FamilyInfo familyInfo = null;
	    try {
	        FamilyInfoService familyInfoService = (FamilyInfoService) MelotBeanFactory.getBean("newFamilyInfoService");
	        familyInfo = familyInfoService.getFamilyInfoByUserId(userId, appId);
	        if (familyInfo != null && familyInfo.getOpen() != null && familyInfo.getOpen() != 2) {
	            return familyInfo;
            }
        } catch (Exception e) {
            logger.error("FamilyService.getUserFamilyName exception, userId :" + userId + " , appId :" + appId, e);
        }
	    
	    return null;
	}
	
	public static int getFamilyByFamilyLeader(int leaderId) {
	    try {
            return (Integer) SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Family.getFamilyByFamilyLeader", leaderId);
        } catch (SQLException e) {
            logger.error("FamilyService.getFamilyByFamilyLeader exception, leaderId :" + leaderId, e);
        }
	    return 0;
	}
	
}

/** 用户申请Id同步到redis */
class ApplyFamilyUsersToRedis extends Thread {
    
	private static Logger logger = Logger.getLogger(ApplyFamilyUsersToRedis.class);
    
    private int familyId;
    
    ApplyFamilyUsersToRedis(int familyId) {
        this.familyId = familyId;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public synchronized void start() {
        try {
            List<Integer> applyUserList = (List<Integer>) SqlMapClientHelper.getInstance(DB.MASTER).queryForList("Family.getFamilyApplyerIdList", familyId);
        
            if (applyUserList != null && applyUserList.size() > 0) {
                
                FamilyApplySource.delApplyJoinFamilyUsers(String.valueOf(familyId));
                
                for (Integer applyUserId : applyUserList) {
                    // 申请加入家族
                    FamilyApplySource.applyJoinFamily(String.valueOf(familyId), String.valueOf(applyUserId));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Family.getFamilyApplyList", e);
        }
    }
    
}

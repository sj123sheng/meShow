package com.melot.kkcx.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.api.menu.sdk.service.RoomInfoService;
import com.melot.content.config.apply.service.ApplyContractService;
import com.melot.content.config.domain.ApplyContractInfo;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kktv.model.FansRankingItem;
import com.melot.kktv.redis.GiftRecordSource;
import com.melot.kktv.redis.MatchSource;
import com.melot.kktv.util.DateUtil;
import com.melot.kktv.util.RankingEnum;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;
import com.melot.sdk.core.util.MelotBeanFactory;
import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 类说明：
 * <p>作者：宋建明<a href="mailto:jianming.song@melot.cn"></p>
 * <p>创建日期：2014-10-10</p>
 * <p>版本：V1.0</p>
 * <p>修改历史：</p>
 */
public class RoomService {

    private static Logger logger = Logger.getLogger(RoomService.class);
    
    /**
     * 获取房间粉丝榜单
     * @param roomId 房间编号
     * @param slotType 榜单类型
     * @return
     */
    @SuppressWarnings("unchecked")
	public static List<FansRankingItem> getRoomFansRankList(int roomId, int slotType) {
    	
    	List<FansRankingItem> fansList = null;
    	
    	//交友房需统计送礼给用户
        int roomSource = 0;
        RoomInfo roomInfo = com.melot.kktv.service.RoomService.getRoomInfo(roomId);
        if (roomInfo != null) {
            roomSource = roomInfo.getRoomSource();
        }
    	String data = MatchSource.getRoomFansRankCache(String.valueOf(slotType), String.valueOf(roomId), String.valueOf(roomSource));
    	if (data == null) {
    		try {
    		    Map<String, Object> map = new HashMap<>();
    		    map.put("roomId", roomId);
    		    map.put("roomSource", roomSource);
				if (slotType == RankingEnum.RANKING_WEEKLY) {
					fansList = (List<FansRankingItem>) SqlMapClientHelper.getInstance(DB.MASTER)
							.queryForList("User.getWeeklyFansRanking", map);
				}
				if (slotType == RankingEnum.RANKING_MONTHLY) {
					fansList = (List<FansRankingItem>) SqlMapClientHelper.getInstance(DB.MASTER)
							.queryForList("User.getMonthlyFansRanking", map);
				}
			} catch (SQLException e) {
				logger.error("未能正常调用SQL语句", e);	
			}
			if (fansList != null && fansList.size() > 0) {

                List<Integer> userIds  = Lists.newArrayList();
                for(FansRankingItem fansRankingItem : fansList) {
                    if(fansRankingItem.getUserId() != null) {
                        userIds.add(fansRankingItem.getUserId());
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

				for (FansRankingItem fansRank : fansList) {
				    UserProfile userProfile = userProfileMap.get(fansRank.getUserId());
					if (userProfile != null) {
						fansRank.setNickname(userProfile.getNickName());
						fansRank.setGender(userProfile.getGender());
						fansRank.setPortrait(userProfile.getPortrait());
					}
				}
				// 更新REDIS中房间粉丝榜缓存数据
				// 每日0点自动过期
				// 每1小时自动过期
				int seconds = 3600;
				Date now = new Date();
				Date nextDay = DateUtil.getNextDay(now);
				int diffSeconds = (int) ((nextDay.getTime() - now.getTime()) / 1000);
				if (diffSeconds < seconds) {
					seconds = diffSeconds;
				}
				MatchSource.setRoomFansRankCache(String.valueOf(slotType), String.valueOf(roomId), String.valueOf(roomSource),
						new Gson().toJson(fansList), seconds);
			}
    	} else {
    		try {
    			fansList = new Gson().fromJson(data,
    					new TypeToken<List<FansRankingItem>>(){}.getType());
    		} catch (Exception e) {
				logger.error("fail to parse string to java bean, json " + data, e);	
			}
    	}
    	
    	return fansList;
    }
    
    /**
     * 获取用户房间信息(pg)
     * @param userId 用户Id
     * @return
     */
    public static RoomInfo getRoomInfo(int userId) {
        if (userId <= 0) {
            return null;
        }
        try {
            RoomInfoService roomInfoServie = MelotBeanFactory.getBean("roomInfoService", RoomInfoService.class);
            return roomInfoServie.getRoomInfoById(userId);
        } catch (Exception e) {
           logger.error("RoomService.getRoomInfo, userId : " + userId, e);
           return null;
        }
    }
    
    /**
     * 获取用户房间信息(pg)
     * @param userId 用户Id
     * @return
     */
    public static RoomInfo getRoomInfoByIdInDb(int userId) {
        if (userId <= 0) {
            return null;
        }
        try {
            RoomInfoService roomInfoServie = MelotBeanFactory.getBean("roomInfoService", RoomInfoService.class);
            return roomInfoServie.getRoomInfoByIdInDb(userId);
        } catch (Exception e) {
           logger.error("RoomService.getRoomInfo, userId : " + userId, e);
           return null;
        }
    }
    
    /**
     * 来自node获取用户房间信息(pg) 原始数据不替换
     * @param userId 用户Id
     * @return
     */
    public static RoomInfo getRoomInfoByIdInDbFromNode(int userId) {
        if (userId <= 0) {
            return null;
        }
        try {
            RoomInfoService roomInfoServie = MelotBeanFactory.getBean("roomInfoService", RoomInfoService.class);
            return roomInfoServie.getRoomInfoByActoridInDbFromNode(userId);
        } catch (Exception e) {
           logger.error("RoomService.getRoomInfo, userId : " + userId, e);
           return null;
        }
    }
    
    /**
     * 获取用户房间信息(pg)
     * @param roomIds 房间Ids
     * @return
     */
    public static List<RoomInfo> getRoomListByRoomIds(String roomIds) {
        if (StringUtil.strIsNull(roomIds)) {
            return null;
        }
        try {
            RoomInfoService roomInfoServie = MelotBeanFactory.getBean("roomInfoService", RoomInfoService.class);
            return roomInfoServie.getRoomListByRoomIds(roomIds);
        } catch (Exception e) {
           logger.error("RoomService.getRoomListByRoomIds, roomIds : " + roomIds, e);
        }
        
        return null;
    }
    
    /**
     * 获取家族主播分成比例
     * @return
     */
    public static Integer getFamilyActorDistributRate(int userId, int familyId) {
    	try {
    		ApplyContractService applyContractService = MelotBeanFactory.getBean("applyContractService", ApplyContractService.class);
    		ApplyContractInfo applyContractInfo = applyContractService.getApplyContractInfoByUserIdAndFamilyId(userId, familyId);
    		if (applyContractInfo != null && applyContractInfo.getDistributRate() != null) {
    			return applyContractInfo.getDistributRate();
    		}
    	} catch (Exception e) {
    		logger.error("Fail to call ApplyContractService.getApplyContractInfoByUserIdAndFamilyId, "
    				+ "userId " + userId + " , " + familyId, e);
    	}
    	return null;
    }
	
    /**
     * 增加主播收入
     * @param userId 用户ID
     * @param actorId 主播ID
     * @param familyId 家族ID
     * @param refId 可为0
     * @param count 数量
     * @param price 单价
     * @param actorRate 
     * @param familyRate 
     * @param type 8:动态点赞(双方分成) 9:周星奖励(100%归主播) 11:办理会员奖励(100%归主播) 12:购买勋章奖励(60%归家族)
     * @return
     */
	public static void incActorIncome(int userId, int actorId, int familyId,
			int refId, int count, int price, int actorRate, int familyRate, int type, int addShowMoney) {
		JsonObject jsonObj = new JsonObject();
    	jsonObj.addProperty("userId", userId);
    	jsonObj.addProperty("actorId", actorId);
    	jsonObj.addProperty("familyId", familyId);
    	jsonObj.addProperty("giftId", refId);
    	jsonObj.addProperty("giftCount", count);
    	jsonObj.addProperty("giftPrice", price);
    	jsonObj.addProperty("addShowMoney", addShowMoney);
    	if (type == 1) {
    		// 赠送幸运礼物 主播家族收入2/3
    		jsonObj.addProperty("actorRate", Math.floor(actorRate * 2 / 3D));
    		jsonObj.addProperty("familyRate", Math.floor(familyRate * 2 / 3D));
    	} else if (type == 9) {
    		// 周星奖励 100%归主播
    		jsonObj.addProperty("actorRate", 100);
    		jsonObj.addProperty("familyRate", 0);
    	} else if (type == 10) {
    		// 家族奖励 100%归家族
    		jsonObj.addProperty("actorRate", 0);
    		jsonObj.addProperty("familyRate", 100);
    	} else if (type == 11) {
    		// 办理会员奖励 100%归主播
    		jsonObj.addProperty("actorRate", 100);
    		jsonObj.addProperty("familyRate", 0);
    	} else if (type == 12) {
    		// 购买勋章奖励 60%归家族
    		jsonObj.addProperty("actorRate", 0);
    		jsonObj.addProperty("familyRate", 60);
    	} else {
    		jsonObj.addProperty("actorRate", actorRate);
    		jsonObj.addProperty("familyRate", familyRate);
    	}
    	jsonObj.addProperty("incomeType", type);
    	jsonObj.addProperty("dtime", System.currentTimeMillis());
    	GiftRecordSource.pushActorIncomeQueue(jsonObj.toString());
    }

    /**
     * 获取家族主播信息
     * @param userId
     * @return
     */
    public static ApplyContractInfo getApplyContractInfoByUserId(Integer userId) {
        try {
            ApplyContractService applyContractService = MelotBeanFactory.getBean("applyContractService", ApplyContractService.class);
            return applyContractService.getApplyContractInfoByUserId(userId);
        } catch (Exception e) {
            logger.error("Fail to call ApplyContractService.getApplyContractInfoByUserIdAndFamilyId, "
                    + "userId " + userId, e);
        }
        return null;
    }
	
}

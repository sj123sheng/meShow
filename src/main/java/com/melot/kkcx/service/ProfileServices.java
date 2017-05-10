package com.melot.kkcx.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import com.melot.kktv.domain.UserInfo;
import com.melot.kktv.redis.HotDataSource;
import com.melot.kktv.redis.MedalSource;
import com.melot.kktv.redis.QQVipSource;
import com.melot.kktv.service.TagService;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;

public class ProfileServices {
    
    private static Logger logger = Logger.getLogger(ProfileServices.class);
    
    private static final String USER_ADMIN_KEY = "%s_adminType";
    
    private static final String USER_ROOMTYPE_KEY = "%s_roomType";
    
    private static final String ROOMSOURCE_ACTOR_KEY = "%s_roomSourceActor";
	
	/**
	 * 更新私有redis
	 * @param userId
	 * @param updateUserInfo
	 * @return
	 */
	public static JsonObject updateRedisUserInfo(Integer userId, JsonObject updateUserInfo) {
		JsonObject result = new JsonObject();
		if (updateUserInfo != null) {
			// 更新redis 活跃用户信息
			Map<String, String> newHotData = new HashMap<String, String>();
			if (updateUserInfo.get("nickname") != null) {
				newHotData.put("nickname", updateUserInfo.get("nickname").getAsString());
			}
			if (updateUserInfo.get("background")!=null) {
				newHotData.put("background", updateUserInfo.get("background").getAsString());
			}
			if (updateUserInfo.get("background_path_original")!=null) {
				newHotData.put("background_path_original", updateUserInfo.get("background_path_original").getAsString());
			}
			if (updateUserInfo.get("backgroundshow")!=null) {
				newHotData.put("backgroundshow", updateUserInfo.get("backgroundshow").getAsString());
			}
			if (updateUserInfo.get("backgroundscroll") != null) {
				newHotData.put("backgroundscroll", updateUserInfo.get("backgroundscroll").getAsString());
			}
			if (updateUserInfo.get("maxCount") != null) {
				newHotData.put("maxCount", updateUserInfo.get("maxCount").getAsString());
			}
            if (updateUserInfo.get("introduce") != null) {
                newHotData.put("introduce", updateUserInfo.get("introduce").getAsString());
            }
			newHotData.put("time", String.valueOf(System.currentTimeMillis()));
			HotDataSource.setHotData(String.valueOf(userId), newHotData, ConfigHelper.getRedisUserDataExpireTime());
		} else {
			UserInfo userInfo = UserService.getUserInfo(userId);
			if (userInfo != null) {
				// 更新redis 活跃用户信息
				Map<String, String> newHotData = new HashMap<String, String>();
				newHotData.put("userId", String.valueOf(userId));
				result.addProperty("userId", userId);
				if (userInfo.getNickname() != null) {
					result.addProperty("nickname", userInfo.getNickname());
					newHotData.put("nickname", userInfo.getNickname());
				}
				if (userInfo.getIntroduce() != null) {
				    result.addProperty("introduce", userInfo.getIntroduce());
				    newHotData.put("introduce", userInfo.getIntroduce());
				}
				if (userInfo.getIconTag() != null) {
					result.addProperty("iconTag", userInfo.getIconTag());
					newHotData.put("iconTag", userInfo.getIconTag().toString());
				}
				if (userInfo.getBackground_path() == null) {
					result.addProperty("background", 0);
					result.addProperty("background_path_original", 0);
					newHotData.put("background", String.valueOf(0));
					newHotData.put("background_path_original", String.valueOf(0));
				} else {
					String s = userInfo.getBackground_path();
					try {
						int i = Integer.parseInt(s);
						result.addProperty("background", i);
						result.addProperty("background_path_original", i);
						newHotData.put("background", String.valueOf(i));
						newHotData.put("background_path_original", String.valueOf(i));
					} catch (Exception e) {
						result.addProperty("background", ConfigHelper.getHttpdir() + s);
						result.addProperty("background_path_original", ConfigHelper.getHttpdir() + s);
						newHotData.put("background", s);
						newHotData.put("background_path_original", s);
					}
				}
				if (userInfo.getBackgroundShow() == null) {
					result.addProperty("backgroundshow", 0);
					newHotData.put("backgroundshow", String.valueOf(0));
				} else {
					result.addProperty("backgroundshow", userInfo.getBackgroundShow());
					newHotData.put("backgroundshow", userInfo.getBackgroundShow().toString());
				}
				if (userInfo.getBackgroundScroll() == null) {
					result.addProperty("backgroundscroll", 0);
					newHotData.put("backgroundscroll", String.valueOf(0));
				} else {
					result.addProperty("backgroundscroll", userInfo.getBackgroundScroll());
					newHotData.put("backgroundscroll", userInfo.getBackgroundScroll().toString());
				}
				if (userInfo.getLiveVideoQuality() != null) {
					result.addProperty("livevideoquality", userInfo.getLiveVideoQuality());
					newHotData.put("livevideoquality", userInfo.getLiveVideoQuality().toString());
				}
				if (userInfo.getNoticeContent() != null) {
					result.addProperty("noticeContent", userInfo.getNoticeContent());
					newHotData.put("noticeContent", userInfo.getNoticeContent());
				}
				if (userInfo.getNoticeHref() != null) {
					result.addProperty("noticeHref", userInfo.getNoticeHref());
					newHotData.put("noticeHref", userInfo.getNoticeHref());
				}
				if (userInfo.getGreetMsg() != null) {
					result.addProperty("welcomeMsg", userInfo.getGreetMsg());
					newHotData.put("welcomeMsg", userInfo.getGreetMsg());
				}
				if (userInfo.getGreetMsgHref() != null) {
					result.addProperty("welcomeMsgHref", userInfo.getGreetMsgHref());
					newHotData.put("welcomeMsgHref", userInfo.getGreetMsgHref());
				}
				newHotData.put("time", String.valueOf(System.currentTimeMillis()));
				
				// 获取用户家族勋章列表
				try {
				    result.addProperty("userMedal", MedalSource.getUserMedalsAsJson(userId, null).toString());
				    newHotData.put("userMedal", MedalSource.getUserMedalsAsJson(userId, null).toString());
                } catch (Exception e) {
                    logger.error("UserService.getUserMedalList(" + userId + ") execute exception.", e);
                }
				
				// 获取用户标签
				try {
				    String tags = TagService.getUserTags(userId);
				    if (!StringUtil.strIsNull(tags)) {
				        newHotData.put("tags", tags);
				    }
                } catch (Exception e) {
                    logger.error("TagService.getUserTags(" + userId + ") execute exception.", e);
                }
				
				newHotData.put("loadTime", String.valueOf(System.currentTimeMillis()));
				HotDataSource.setHotData(String.valueOf(userId), newHotData, ConfigHelper.getRedisUserDataExpireTime());
				
				result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			} else {
				return result;
			}
		}
		
		// 返回QQ会员过期时间
		try {
		    Long qqVipExpireTime = QQVipSource.getQQVipExpireTime(String.valueOf(userId));
		    if(qqVipExpireTime!=null) {
		        result.addProperty("qqVipExpireTime", qqVipExpireTime);
		    }
        } catch (Exception e) {
            logger.error("QQVipSource.getQQVipExpireTime(" + userId + ") execute exception.", e);
        }
		
		return result;
	}
	
	public static Integer getUserAdminType(int userId) {
	    Integer adminType = null;
	    try {
	        String adminKey = String.format(USER_ADMIN_KEY, userId);
	        String sadminType = HotDataSource.getTempDataString(adminKey);
	        if (sadminType == null) {
	            adminType = UserService.getUserAdminType(userId);
	            if (adminType == null) {
	                adminType = -1;
	            }
	            HotDataSource.setTempDataString(adminKey, adminType.toString(), 60);
	        } else {
	            adminType = StringUtil.parseFromStr(sadminType, 0);
	        }
	    } catch(Exception e) {
            logger.error("ProfileServices.getUserAdminType(" + userId + ") return exception.", e);
        }
        return adminType;
	}
	
    public static Integer getRoomType(int actorId) {
        Integer roomType = null;
        try {
            String roomTypeKey = String.format(USER_ROOMTYPE_KEY, actorId);
            String sroomType = HotDataSource.getTempDataString(roomTypeKey);
            if (sroomType == null) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("actorId", actorId);
                roomType = (Integer) SqlMapClientHelper.getInstance(DB.SLAVE_PG).queryForObject("Other.getroomTypeById", map);
                if (roomType == null) {
                    roomType = -1;
                }
                HotDataSource.setTempDataString(roomTypeKey, roomType.toString(), 60);
            } else {
                roomType = StringUtil.parseFromStr(sroomType, 0);
            }
        } catch(Exception e) {
            logger.error("ProfileServices.getRoomType(" + actorId + ") return exception.", e);
        }
        return roomType;
    }
    
    public static String getRoomSourceActor(int roomSource) {
        String roomSourceActors = null;
        try {
            String roomSourceKey = String.format(ROOMSOURCE_ACTOR_KEY, roomSource);
            roomSourceActors = HotDataSource.getTempDataString(roomSourceKey);
            if (roomSourceActors == null) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("roomSource", roomSource);
                @SuppressWarnings("unchecked")
                List<Integer> roomSourceActorList = SqlMapClientHelper.getInstance(DB.SLAVE_PG).queryForList("Other.getActorIdByRoomSource", map);
                if (roomSourceActorList != null && !roomSourceActorList.isEmpty()) {
                    StringBuffer sb = new StringBuffer();
                    for (Integer actorId : roomSourceActorList) {
                        sb.append(actorId).append(",");
                    }
                    roomSourceActors = sb.substring(0, sb.length() - 1);
                } else {
                    roomSourceActors = "-1";
                }
                HotDataSource.setTempDataString(roomSourceKey, roomSourceActors, 60);
            }
        } catch(Exception e) {
            logger.error("ProfileServices.getRoomSourceActor(" + roomSource + ") return exception.", e);
        }
        return roomSourceActors;
    }
	
	public static int getPasswordSafetyRank(String ps) {
		if (ps == null) {
			return -1;
		} else {
			if (Pattern.matches(isNormal, ps) || !Pattern.matches(isPureMars, ps)) {
				//强度为低
				return 1;
			} else {
				if (checkExistMore(ps)) {
					//强度为高
					return 3;
				}
				if (ps.length() >= 6 && ps.length() <= 12) {
					//强度为中
					return 2;
				}
				if (ps.length() > 12) {
					//强度为高
					return 3;
				}
			}
		}
		return -1;
	}
	
	private static boolean checkExistMore(String ps) {
		int count = 0;
		if (Pattern.matches(existUpperLetter, ps)) {
			count++;
		}
		if (Pattern.matches(existLowerLetter, ps)) {
			count++;
		}
		if (Pattern.matches(existNumber, ps)) {
			count++;
		}
		if (!Pattern.matches(existMars, ps)) {
			count++;
		}
		if (count >= 3) {
			return true;
		}
		return false;
	}
	
	private final static String isNormal = "^[A-Z]+$||^[a-z]+$||^[0-9]+$";
	
	private final static String isPureMars = ".*[a-zA-Z0-9].*";
	
	private final static String existMars = "[a-zA-Z0-9]*";
	
	private final static String existUpperLetter = ".*[A-Z].*";
	
	private final static String existLowerLetter = ".*[a-z].*";
	
	private final static String existNumber = ".*[0-9].*";
	
}

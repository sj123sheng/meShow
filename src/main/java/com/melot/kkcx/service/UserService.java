package com.melot.kkcx.service;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.common.driver.service.StarService;
import com.melot.kkcore.user.api.UserInfoDetail;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcore.user.api.UserRegistry;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kkcx.model.ActorLevel;
import com.melot.kkcx.model.RichLevel;
import com.melot.kkcx.model.StarInfo;
import com.melot.kktv.domain.UserInfo;
import com.melot.kktv.util.CollectionEnum;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConstantEnum;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;
import com.melot.kktv.util.mongodb.CommonDB;
import com.melot.module.packagegift.driver.service.VipService;
import com.melot.sdk.core.util.MelotBeanFactory;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class UserService {
	
	private static Logger logger = Logger.getLogger(UserService.class);	
    
    /**
     * 判断用户是否是真实主播
     * @param userId 用户ID
     * @return true - 是主播， false - 不是主播
     */
    public static boolean isRealActor(int userId) {
        RoomInfo roomInfo = RoomService.getRoomInfo(userId);
        if (roomInfo != null) {
            return true;
        }
        return false;
    }
	
	/**
	 * 判断用户是否带主播属性
	 * @return true/false
	 */
	public static boolean isActor(int userId) {
		return isRealActor(userId);
	}
	
	/**
	 * 获取用户昵称
	 * @param userId
	 * @return
	 */
	public static String getUserNickname(int userId) {
		String nickname = null;
		try {
		    UserProfile userProfile = com.melot.kktv.service.UserService.getUserInfoV2(userId);
		    if (userProfile != null) {
		        nickname = userProfile.getNickName();
		    }
		} catch (Exception e) {
		    logger.error("fail to get KkUserService.getUserProfile, userId: " + userId, e);
		}
		
		return nickname;
	}
	
	private static ConcurrentHashMap<Class<?>, MethodAccess> accessMap = new ConcurrentHashMap<Class<?>, MethodAccess>();
	
	private static ConcurrentHashMap<Class<?>, List<String>> methodMap = new ConcurrentHashMap<Class<?>, List<String>>();

	public static <T> List<T> addUserExtra(List<T> tList) {
		if (tList == null || tList.size() <= 0) {
			return null;
		}
		MethodAccess access = null;
		Method[] methods = null;
		List<Integer> userIdList = new ArrayList<Integer>();
		List<String> methodName = new ArrayList<String>();
		Class<?> clazz = tList.get(0).getClass();
		try {
			if (methodMap.containsKey(clazz)) {
				methodName = methodMap.get(clazz);
			} else {
				methods = clazz.getDeclaredMethods();
				for (Method method : methods) {
					methodName.add(method.getName());
				}
				methodMap.putIfAbsent(clazz, methodName);
			}
			if (accessMap.containsKey(clazz)) {
				access = accessMap.get(clazz);
			} else {
				access = MethodAccess.get(clazz);
				accessMap.putIfAbsent(clazz, access);
			}
			for (T t : tList) {
				userIdList.add((int) access.invoke(t, "getUserId"));
			}
			List<UserProfile> profileList = getUserProfileAll(userIdList);
			if (profileList != null && profileList.size() > 0) {
				Map<Integer, UserProfile> profileMap = new HashMap<Integer, UserProfile>();
				for (UserProfile userProfile : profileList) {
					profileMap.put(userProfile.getUserId(), userProfile);
				}
				for (T t : tList) {
					int userId = (int) access.invoke(t, "getUserId");
					if (profileMap.containsKey(userId) && profileMap.get(userId) != null) {
						if (methodName.contains("setActorTag")) {
							access.invoke(t, "setActorTag", profileMap.get(userId).getIsActor());
						}
						if (methodName.contains("setPortrait_path_original")) {
							access.invoke(t, "setPortrait_path_original", profileMap.get(userId).getPortrait());
						}
						if (methodName.contains("setNickname")) {
							access.invoke(t, "setNickname", profileMap.get(userId).getNickName());
						}
						if (methodName.contains("setGender")) {
							access.invoke(t, "setGender", profileMap.get(userId).getGender());
						}
					}
				}
			}
		} catch (IllegalArgumentException e) {
			logger.error("reflect java bean, " + clazz + " : {" + tList + "} catched exception", e);
		}
		return tList;
	}
	
	public static UserRegistry getUserRegistryInfo(int userId) {
		try {
			KkUserService userService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
			return userService.getUserRegistry(userId);
		} catch (Exception e) {
			logger.error("call KkUserService getUserRegistry catched exception, userId : " + userId, e);
		}
		return null;
	}
	
	/**
     * 检查用户昵称是否存在
     * @param nickname
     * @return true 存在 false 不存在
     */
    public static boolean checkNicknameRepeat(String nickname, int userId) {
        if (StringUtil.strIsNull(nickname)) {
            return true;
        }
        
        try {
            KkUserService userService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
            if (userService.isNicknameExist(nickname)) {
                UserProfile userProfile = userService.getUserProfile(userId);
                if (userProfile != null && userProfile.getNickName() != null && userProfile.getNickName().equals(nickname)) {
                    return false;
                }
                
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error("Fail to execute User.checkNickname sql, nickname " + nickname, e);
        }
        
        return true;
    }
	
	/**
	 * 从内存数据库中读出用户richLevel
	 * @param consumetotal 等级值
	 * @return
	 */
	public static RichLevel getRichLevel(long consumeTotal) {
		RichLevel richLevel = new RichLevel();
		
		BasicDBObject queryRichLevel = new BasicDBObject();
		queryRichLevel.put("minPoint", new BasicDBObject("$lte", consumeTotal));
		queryRichLevel.put("maxPoint", new BasicDBObject("$gt", consumeTotal));
		DBObject richLevelObj = CommonDB.getInstance(CommonDB.COMMONDB).getCollection(CollectionEnum.RICHLEVEL)
				.findOne(queryRichLevel);
		if (richLevelObj != null) {
			if (richLevelObj.containsField("richLevel")) {
				richLevel.setLevel(Integer.parseInt(richLevelObj.get("richLevel").toString()));
			}
			if (richLevelObj.containsField("minPoint")) {
				richLevel.setMinValue(Long.parseLong(richLevelObj.get("minPoint").toString()));
			}
			if (richLevelObj.containsField("maxPoint")) {
				richLevel.setMaxValue(Long.parseLong(richLevelObj.get("maxPoint").toString()));
			}
		}
		
		return richLevel;
	}
	
	/**
	 * 从内存数据库中读出用户actorLevel
	 * @param earntotal 等级值
	 * @return
	 */
	public static ActorLevel getActorLevel(long earnTotal) {
		ActorLevel actorLevel = new ActorLevel();
		
		BasicDBObject queryActorLevel = new BasicDBObject();
		queryActorLevel.put("minPoint", new BasicDBObject("$lte", earnTotal));
		queryActorLevel.put("maxPoint", new BasicDBObject("$gt", earnTotal));
		DBObject actorLevelObj = CommonDB.getInstance(CommonDB.COMMONDB).getCollection(CollectionEnum.ACTORLEVEL)
				.findOne(queryActorLevel);
		if (actorLevelObj != null) {
			if (actorLevelObj.containsField("actorLevel")) {
				actorLevel.setLevel(Integer.parseInt(actorLevelObj.get("actorLevel").toString()));
			}
			if (actorLevelObj.containsField("minPoint")) {
				actorLevel.setMinValue(Long.parseLong(actorLevelObj.get("minPoint").toString()));
			}
			if (actorLevelObj.containsField("maxPoint")) {
				actorLevel.setMaxValue(Long.parseLong(actorLevelObj.get("maxPoint").toString()));
			}
		}
		
		return actorLevel;
	}
    
	/**
	 * 从内存数据库中读出用户richLevel
	 * @param userId
	 * @return
	 */
	public static int getRichLevel(int userId) {
		try {
            KkUserService userService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
            UserProfile userProfile = userService.getUserProfile(userId);
            if (userProfile != null) {
                return userProfile.getUserLevel();
            }
        } catch (Exception e) {
            logger.error("fail to get KkUserService.getUserProfile, userId: " + userId, e);
        }
		return 0;
	}
	
	public static Map<String, Integer> getUserLevelAll(int userId) {
		Map<String, Integer> userLevel = null;
        try {
			KkUserService userService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
			UserProfile userProfile = userService.getUserProfile(userId);
			if (userProfile != null) {
				userLevel = new HashMap<String, Integer>();
				userLevel.put("actorLevel", userProfile.getActorLevel());
				userLevel.put("richLevel", userProfile.getUserLevel());
			}
		} catch (Exception e) {
			logger.error("fail to get KkUserService.getUserProfile, userId: " + userId, e);
		}
        return userLevel;
	}
	
	public static List<UserProfile> getUserProfileAll(List<Integer> userIds) {
        try {
			KkUserService userService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
			return userService.getUserProfileBatch(userIds);
		} catch (Exception e) {
			logger.error("fail to get KkUserService.getUserProfileBatch, userIds: " + userIds, e);
		}
        return null;
	}
	
	/**
	 * 从内存数据库中读出用户actorLevel
	 * @param userId
	 * @return
	 */
	public static int getActorLevel(int userId) {
       try {
            KkUserService userService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
            UserProfile userProfile = userService.getUserProfile(userId);
            if (userProfile != null) {
                return userProfile.getActorLevel();
            }
        } catch (Exception e) {
            logger.error("fail to get KkUserService.getUserProfile, userId: " + userId, e);
        }
        return 0;
	}
    
    /**
     * 更新 MongoDB 中 dailyCosume 当日的 consumetotal
     * @param userId
     * @return
     */
    public static boolean updateStarLevel(int userId, long consumetotal) {
    	try {
	        StarService starService = MelotBeanFactory.getBean("starService", StarService.class);
	        long newConsume =  starService.updateStarLevel(userId, consumetotal);
	        if (newConsume > 0) {
				return true;
			}
    	}catch (Exception e) {
    		 logger.error("fail to get StarService.updateStarLevel, userId: " + userId+",consumetotal:"+consumetotal, e);
    	}
        return false;
    }
    
    /**
     * 从内存数据库的dailyCosume中获取userConsume,统计用户7天总消费额计算出用户starLevel
     * @param userId
     * @return
     */
    public static int getStarLevel(int userId) {
    	int level = 0;
    	try {
    		StarService starService = MelotBeanFactory.getBean("starService", StarService.class);
    	    com.melot.common.driver.domain.StarInfo starInfo =  starService.getStarInfo(userId);
	        if (starInfo != null) {
	        	level = starInfo.getLevel();
			}
    	}catch (Exception e) {
    		 logger.error("fail to get StarService.getStarInfo, userId: " + userId, e);
    	}
        return level;
    }
    
    /**
     * 从内存数据库的dailyCosume中获取userConsume,统计用户7天总消费额计算出用户starLevel
     * @param userId
     * @return
     */
    public static StarInfo getStarInfo(int userId) {
        StarInfo starInfo = new StarInfo();
        try {
	        StarService starService = MelotBeanFactory.getBean("starService", StarService.class);
	        com.melot.common.driver.domain.StarInfo redisStarInfo =  starService.getStarInfo(userId);
	        try {
	        	if (redisStarInfo != null) {
	        		BeanUtils.copyProperties(starInfo, redisStarInfo);
				}
	        } catch (Exception e) {
                e.printStackTrace();
            }
        }catch (Exception e) {
   		 	logger.error("fail to get StarService.getStarInfo, userId: " + userId, e);
        }
        return starInfo;
    }
    
    public static UserInfoDetail getUserInfoDetail(int userId) {
    	try {
			KkUserService userService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
			return userService.getUserDetailInfo(userId);
		} catch (Exception e) {
			logger.error("call KkUserService getUserStaticInfo catched exception, userId : " + userId, e);
		}
    	return null;
    }
    
    /**
     * 功能描述：查询私有用户信息
     * 
     * @return userInfo
     */
    public static UserInfo getUserInfo(int userId) {
    	try {
    		UserInfoDetail userInfoDetail = getUserInfoDetail(userId);
			if (userInfoDetail != null) {
				UserInfo userInfo = (UserInfo) SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("User.getUserInfo", userId);
				if (userInfo == null) {
					userInfo = new UserInfo();
				}
				if (userInfoDetail.getProfile() != null ) {
					UserProfile userProfile = userInfoDetail.getProfile();
					userInfo.setNickname(userProfile.getNickName());
					userInfo.setGender(userProfile.getGender());
					userInfo.setPhone(userProfile.getPhoneNum());
					userInfo.setSignature(userProfile.getSignature());
					userInfo.setIntroduce(userProfile.getIntroduce());
					userInfo.setBirthday(userProfile.getBirthday());
					userInfo.setActorTag(userProfile.getIsActor());
					userInfo.setActorLevel(userProfile.getActorLevel());
					userInfo.setRichLevel(userProfile.getUserLevel());
				}
				if (userInfoDetail.getRegisterInfo() != null) {
					UserRegistry userRegistry = userInfoDetail.getRegisterInfo();
					userInfo.setCity(userRegistry.getCityId());
					userInfo.setRegisterTime(new Date(userRegistry.getRegisterTime()));
					userInfo.setOpenPlatform(userRegistry.getOpenPlatform());
				}
				if (userInfoDetail.getAssets() != null) {
					userInfo.setShowMoney(new Long(userInfoDetail.getAssets().getShowMoney()).intValue());
				}
				return userInfo;
			}
		} catch (Exception e) {
			 logger.error("Fail to execute getUserInfo sql, userId " + userId, e);
		}
        return null;
    }
    
    /**
     * 获取用户官方号类型
     * 
     * @param userId
     * @return
     */
    public static Integer getUserAdminType(int userId) {
        Integer adminType = null;
        try {
            adminType = (Integer) SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("User.getAdminType", userId);
        } catch (Exception e) {
            logger.error("Fail to execute getAdminType sql, userId " + userId, e);
        }
        return adminType;
    }
    
    /**
     * 功能描述：查询私有用户信息
     * 
     * @return userInfo
     */
    public static UserProfile getUserInfoNew(int userId) {
        return com.melot.kktv.service.UserService.getUserInfoV2(userId);
    }
    
    /**
     * 功能描述：查询用户密码
     * 
     * @return userInfo
     */
    public static String getUserPassword(int userId) {
        String result = null;
        try {
            KkUserService userService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
            UserRegistry userRegistry = userService.getUserRegistry(userId);
            result = userRegistry.getLoginPwd();
        } catch (Exception e) {
            logger.error("fail to get KkUserService.getUserRegistry, userId: " + userId, e);
        }
        
        return result;
    }
	
    /**
     * 获取用户上次登录时间
     * @param userId
     * @return
     */
    @SuppressWarnings("unchecked")
    public static Date getlastLoginTime(int userId) {
    	try {
    		List<Date> list = SqlMapClientHelper.getInstance(DB.MASTER).queryForList("User.getUserLastLoginTime", userId);
    		if (list != null && list.size() > 0) {
    			return list.get(0);
    		}
		} catch(Exception e) {
			logger.error("Fail to execute User.getUserLastLoginTime sql , userId " + userId, e);
		}
    	return null;
    }
    
    /**
     * 获取用户拥有会员
     * @param userId
     * @return
     */
    public static List<Integer> getUserProps(int userId) {
    	try {
			VipService vipService = (VipService) MelotBeanFactory.getBean("vipService");
			return vipService.getUserProp(userId);
		} catch (Exception e) {
			logger.error("call VipService getUserProp catched exception, userId : " + userId, e);
		}
    	return null;
    }
    
    /**
     * 判断是否能被邀请(注册流程)
     * @return
     */
    public static int canInvite(String deviceUId) {
    	if (deviceUId != null) {
    		try {
        		return (Integer) SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("User.getCanInvite", deviceUId);
        	} catch (Exception e) {
        		logger.error("UserService.canInvite( " + deviceUId + ") execute exeception", e);
        	}
    	}
    	return 0;
    }
    
    /**
     * 获取主播K豆
     * @param actorId
     * @return
     */
    public static Long getActorKbi(int actorId) {
        Long kbi = null;
        try {
            kbi = (Long) SqlMapClientHelper.getInstance(com.melot.kktv.util.DBEnum.KKCX_PG).queryForObject("Actor.getActorKbi", actorId);
        } catch (SQLException e) {
            logger.error("fail to execute sql (Actor.getActorKbi), actorId :" + actorId, e);
        }
        
        return kbi;
    }
	
	/**
	 * 是否为黑名单用户 (illeagle user)
	 * @param userId
	 * @return
	 */
	public static boolean blackListUser(int userId) {
        try {
            return (Integer) SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("User.isInUserBlackList", userId) > 0;
        } catch (SQLException e) {
            logger.error("UserService.blackListUser( " + userId + ") execute exeception", e);
        }
        
        return false;
    }
	
	public static boolean isValidOperator(int operatorId) {
		try {
            return (Integer) SqlMapClientHelper.getInstance(DB.BACKUP).queryForObject("Index.isValidOperator", operatorId) > 0;
        } catch (SQLException e) {
            logger.error("UserService.isValidOperator( " + operatorId + ") execute exeception", e);
        }
        
        return false;
	}
	
	public static String getMD5Password(String ps) {
		if (!StringUtil.strIsNull(ps) && ps.length() < ConstantEnum.passwordSize) {
			return CommonUtil.md5(ps);
		}
		return ps;
	}
	
	public static int getUserSmsSwitch(int userId) {
		Integer state = null;
		try {
			state = (Integer) SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("User.getUserSmsSwitch", userId);
		} catch (SQLException e) {
			logger.error("UserService.getUserSmsSwitch( " + userId + " ) execute exception", e);
		}
		return state != null ? state : 0;
	}
	
	public static boolean getUserSmsSwitchState(int userId) {
		return getUserSmsSwitch(userId) == 0;
	}
	
	public static boolean changeUserSmsSwitch(int userId, int state) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("userId", userId);
		map.put("state", state);
		map.put("dtime", new Date());
		try {
			if (state == 0) {
				SqlMapClientHelper.getInstance(DB.MASTER).delete("User.delUserSmsSwitch", userId);
			} else {
				SqlMapClientHelper.getInstance(DB.MASTER).insert("User.insertUserSmsSwitch", map);
			}
			SqlMapClientHelper.getInstance(DB.MASTER).insert("User.insertUserSmsSwitchRecord", map);
		} catch (SQLException e) {
			logger.error("UserService.changeUserSmsSwitch (userId : " + userId + ", state : " + state + " ) execute exception", e);
			return false;
		}
		return true;
	}
	
	public static boolean insertTempUserPassword(int userId, String password) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("userId", userId);
		map.put("password", password);
		map.put("dtime", new Date());
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).insert("User.insertUserTempPassword", map);
		} catch (SQLException e) {
			logger.error("UserService.insertTempUserPassword (userId : " + userId + ", password : " + password + " ) execute exception", e);
			return false;
		}
		return true;
	}
	
	public static boolean getGuestFirstRecord(int userId) {
		try {
			return (int) SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("User.getGuestFirstRecord", userId) > 0;
		} catch (SQLException e) {
			logger.error("UserService.getGuestFirstRecord(userId : " + userId + " ) execute exception", e);
		}
		return false;
	}
	
	public static boolean insertGuestFirstRecord(int userId, int platform, int appId, int channelId, String deviceUId, Date dtime) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("userId", userId);
		map.put("platform", platform);
		map.put("appId", appId);
		map.put("channelId", channelId);
		map.put("deviceUId", deviceUId);
		map.put("dtime", dtime);
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).insert("User.insertGuestFirstRecord", map);
		} catch (SQLException e) {
			logger.error("UserService.insertGuestFirstRecord (userId : " + userId + ", platform : " + platform + 
					", appId : " + appId + ", channelId : " + channelId + ", deviceUId : " + deviceUId + ", dtime : " + dtime + " ) execute exception", e);
			return false;
		}
		return true;
	}
}

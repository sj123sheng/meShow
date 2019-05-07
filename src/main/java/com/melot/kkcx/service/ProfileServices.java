package com.melot.kkcx.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.melot.blacklist.service.BlacklistService;
import com.melot.kk.otherlogin.api.service.OtherLoginService;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kkcx.model.CommonDevice;
import com.melot.kkcx.model.UserProp;
import com.melot.kkcx.util.PropTypeEnum;
import com.melot.kkcx.util.ValidTypeEnum;
import com.melot.kktv.domain.UserInfo;
import com.melot.kktv.redis.HotDataSource;
import com.melot.kktv.redis.QQVipSource;
import com.melot.kktv.service.AccountService;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.DateUtil;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;
import com.melot.module.packagegift.driver.domain.UserChatBubbleDTO;
import com.melot.sdk.core.util.MelotBeanFactory;

public class ProfileServices {
    
    private static Logger logger = Logger.getLogger(ProfileServices.class);
    
    private static final String USER_ADMIN_KEY = "%s_adminType";
    
    private static final String USER_ROOMTYPE_KEY = "%s_roomType";
    
    private static final String ROOMSOURCE_ACTOR_KEY = "%s_roomSourceActor";
    
    private static final String USER_COMMONDEVICE_KEY = "%s_commonDevice";
    
    private static final String USER_UPDATEPROFILE_KEY = "%s_%s_updateProfileV2";

    private static final String USER_UPDATEPROFILETIME_KEY = "%s_%s_updateProfileV2_time";

    private static final String GUEST_NAME_KEY = "%s_guestName";

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

	public static boolean checkIsOfficial(int userId) {
	    boolean result = false;
	    Integer adminType = getUserAdminType(userId);
	    if (adminType != null && adminType != -1 && adminType != 5) {
	        result = true;
	    }
        return result;
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

    @SuppressWarnings("unchecked")
    public static boolean setUserCommonDevice(int userId, String deviceUId, String deviceName, String deviceModel) {
        try {
            if (StringUtil.strIsNull(deviceUId) || StringUtil.strIsNull(deviceName) || StringUtil.strIsNull(deviceModel) || userId < 0) {
                return false;
            }

            long curTime = System.currentTimeMillis();
            long endTime = curTime + 7*24*3600*1000;
            boolean existFlag = false;
            List<CommonDevice> commonDevices = new ArrayList<CommonDevice>();
            List<CommonDevice> commonDeviceList = new ArrayList<CommonDevice>();
            String commonDeviceKey = String.format(USER_COMMONDEVICE_KEY, userId);
            String commonDeviceValue = HotDataSource.getTempDataString(commonDeviceKey);
            Gson gson = new Gson();
            if (!StringUtil.strIsNull(commonDeviceValue)) {
                commonDevices = (List<CommonDevice>) (gson.fromJson(commonDeviceValue, new TypeToken<List<CommonDevice>>(){}.getType()));
                if (commonDevices != null && commonDevices.size() > 0) {
                    for (CommonDevice commonDevice : commonDevices) {
                        if (deviceUId.equals(commonDevice.getDeviceUId())) {
                            commonDevice.setEndTime(endTime);
                            commonDeviceList.add(commonDevice);
                            existFlag = true;
                        } else if (commonDevice.getEndTime() > curTime) {
                            commonDeviceList.add(commonDevice);
                        }
                    }
                }
            }

            if (!existFlag) {
                CommonDevice commonDevice = new CommonDevice();
                commonDevice.setDeviceUId(deviceUId);
                commonDevice.setDeviceName(deviceName);
                commonDevice.setDeviceModel(deviceModel);
                commonDevice.setEndTime(endTime);
                commonDeviceList.add(commonDevice);
            }

            //常用设备最多十个
            if (commonDeviceList.size() > 10) {
                Collections.sort(commonDeviceList,new Comparator<CommonDevice>() {
                    @Override
                    public int compare(CommonDevice cdevice1, CommonDevice cdevice2) {
                        return (int) (cdevice2.getEndTime() - cdevice1.getEndTime());
                    }
               });
            }

            HotDataSource.setTempDataString(commonDeviceKey, gson.toJson(commonDeviceList.size() > 10 ? commonDeviceList.subList(0, 10) : commonDeviceList), 7*24*3600);
        } catch(Exception e) {
            logger.error("ProfileServices.setUserCommonDevice(" + userId + "," + deviceUId + ") return exception.", e);
            return false;
        }
        return true;
    }

    /**
     * @param userId
     * @param type 1:用户昵称  2：用户头像 3:海报
     */
    public static void setUserUpdateProfileByType(int userId, String type) {
        try {
            String key = String.format(USER_UPDATEPROFILE_KEY, userId, type);
            long curTime = System.currentTimeMillis();
            long endTime = DateUtil.getNextDay(new Date()).getTime();
            HotDataSource.setTempDataString(key, "1", (int) ((endTime - curTime)/1000));
        } catch(Exception e) {
            logger.error("ProfileServices.setUserUpdateProfileByType(" + userId + "," + type + ") return exception.", e);
        }
    }

    public static boolean checkUserUpdateProfileByType(int userId, String type) {
        boolean result = false;
        try {
            String key = String.format(USER_UPDATEPROFILE_KEY, userId, type);
            String isUpdate = HotDataSource.getTempDataString(key);
            if ("1".equals(isUpdate)) {
                return true;
            }
        } catch(Exception e) {
            logger.error("ProfileServices.checkUserUpdateProfileByType(" + userId + "," + type + ") return exception.", e);
        }
        return result;
    }


    /**
     * 设置游客昵称
     *
     * @param guestId
     * @param nickname
     */
    public static void setGuestNickName(int guestId, String nickname) {
        try {
            String key = String.format(GUEST_NAME_KEY, guestId);
            HotDataSource.setTempDataString(key, nickname, 10*24*3600*1000);
        } catch(Exception e) {
            logger.error("ProfileServices.setGuestNickName(" + guestId + "," + nickname + ") return exception.", e);
        }
    }

    public static String getGuestNickName(int guestId) {
        String result = null;
        try {
            String key = String.format(GUEST_NAME_KEY, guestId);
            result = HotDataSource.getTempDataString(key);
        } catch(Exception e) {
            logger.error("ProfileServices.getGuestNickName(" + guestId + ") return exception.", e);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public static boolean delUserCommonDevice(int userId, String deviceUId) {
        try {
            if (userId < 0 || StringUtil.strIsNull(deviceUId)) {
                return false;
            }

            long curTime = System.currentTimeMillis();
            long endTime = 0l;
            List<CommonDevice> commonDevices = new ArrayList<CommonDevice>();
            List<CommonDevice> commonDeviceNewList = new ArrayList<CommonDevice>();
            String commonDeviceKey = String.format(USER_COMMONDEVICE_KEY, userId);
            String commonDeviceValue = HotDataSource.getTempDataString(commonDeviceKey);
            Gson gson = new Gson();
            if (!StringUtil.strIsNull(commonDeviceValue)) {
                commonDevices = (List<CommonDevice>) (gson.fromJson(commonDeviceValue, new TypeToken<List<CommonDevice>>(){}.getType()));
                if (commonDevices != null && commonDevices.size() > 0) {
                    for (CommonDevice commonDevice : commonDevices) {
                        if (!deviceUId.equals(commonDevice.getDeviceUId()) && commonDevice.getEndTime() > curTime) {
                            commonDeviceNewList.add(commonDevice);
                            if (commonDevice.getEndTime() > endTime) {
                                endTime = commonDevice.getEndTime();
                            }
                        }
                    }

                    if (commonDeviceNewList.size() > 0) {
                        HotDataSource.setTempDataString(commonDeviceKey, gson.toJson(commonDeviceNewList), (int) (endTime - curTime)/1000);
                    } else {
                        HotDataSource.delTempData(commonDeviceKey);
                    }
                }
            }
        } catch(Exception e) {
            logger.error("ProfileServices.delUserCommonDevice(" + userId + "," + deviceUId + ") return exception.", e);
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public static List<CommonDevice> getUserCommonDevice(int userId) {
        try {
            long curTime = System.currentTimeMillis();
            String commonDeviceKey = String.format(USER_COMMONDEVICE_KEY, userId);
            String commonDeviceValue = HotDataSource.getTempDataString(commonDeviceKey);
            List<CommonDevice> commonDevices = new ArrayList<CommonDevice>();
            List<CommonDevice> result = new ArrayList<CommonDevice>();
            if (!StringUtil.strIsNull(commonDeviceValue)) {
                commonDevices = (List<CommonDevice>) (new Gson().fromJson(commonDeviceValue, new TypeToken<List<CommonDevice>>(){}.getType()));
                for (CommonDevice commonDevice : commonDevices) {
                    if (commonDevice.getEndTime() > curTime) {
                        result.add(commonDevice);
                    }
                }
            }
            return result;
        } catch(Exception e) {
            logger.error("ProfileServices.getUserCommonDevice(" + userId + ") return exception.", e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public static boolean checkUserCommonDevice(int userId, String deviceUId) {
        try {
            if (!StringUtil.strIsNull(deviceUId)) {
                String commonDeviceKey = String.format(USER_COMMONDEVICE_KEY, userId);
                String commonDeviceValue = HotDataSource.getTempDataString(commonDeviceKey);
                List<CommonDevice> commonDevices = new ArrayList<CommonDevice>();
                if (!StringUtil.strIsNull(commonDeviceValue)) {
                    commonDevices = (List<CommonDevice>) (new Gson().fromJson(commonDeviceValue, new TypeToken<List<CommonDevice>>(){}.getType()));
                    if (commonDevices != null && commonDevices.size() > 0) {
                        for (CommonDevice commonDevice : commonDevices) {
                            if (deviceUId.equals(commonDevice.getDeviceUId()) && commonDevice.getEndTime() > System.currentTimeMillis()) {
                                return true;
                            }
                        }
                    }
                }
            }
        } catch(Exception e) {
            logger.error("ProfileServices.getUserCommonDevice(" + userId + ") return exception.", e);
        }
        return false;
    }

    public static int insertChangeUserName(int userId, String nickName, int state) {
        try {
            OtherLoginService otherLoginService = (OtherLoginService) MelotBeanFactory.getBean("otherLoginService");
            return otherLoginService.insertChangeUserName(userId, nickName, state);
        } catch (Exception e) {
            logger.error("ProfileServices.insertChangeUserName(" + "userId:" + userId + "nickName:" + nickName + "state:" + state + ") execute exception.", e);
        }
        return 0;
    }

    /**
     * 认证手机号
     * @param userId 用户id
     * @param phoneNum 手机号
     * @return -1：认证失败  0：认证手机号成功 1：绑定手机号成功
     */
    public static int identifyPhone(int userId, String phoneNum) {
        int result = -1;
        try {
            BlacklistService blacklistService = (BlacklistService) MelotBeanFactory.getBean("blacklistService");
            if (blacklistService.isPhoneNumBlacklist(phoneNum)) {
                return result;
            }

            UserProfile userProfile = com.melot.kktv.service.UserService.getUserInfoV2(userId);
            if (userProfile != null && userProfile.getPhoneNum() != null) {
                return result;
            }
            com.melot.kkcore.account.service.AccountService accountService = (com.melot.kkcore.account.service.AccountService) MelotBeanFactory.getBean("kkAccountService");
            String tagCode = accountService.verifyIdentifyPhone(userId, phoneNum);
            if (tagCode.equals(TagCodeEnum.SUCCESS)) {
                result = 0;
                KkUserService userService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
                int bindUserId = userService.getUserIdByPhoneNumber(phoneNum);
                if (bindUserId == 0) {
                    String bindCode = AccountService.bindPhoneNumAccount(userId, phoneNum, 1, null, AppIdEnum.AMUSEMENT);
                    if (bindCode != null && bindCode.equals(TagCodeEnum.SUCCESS)) {
                        result = 1;
                    }
                }
            }
        } catch(Exception e) {
            logger.error("ProfileServices.identifyPhone(userId: " + userId + "phoneNum: " + phoneNum + ") return exception.", e);
        }
        return result;
    }

    public static UserProp switchBubbleToUserProp (UserChatBubbleDTO userChatBubbleDTO) {
        UserProp result = null;
        if (userChatBubbleDTO != null) {
            UserProp userProp = new UserProp();
            userProp.setId(userChatBubbleDTO.getChatBubbleId());
            userProp.setType(PropTypeEnum.CHAT_BUBBLE.getCode());
            userProp.setLevel(userChatBubbleDTO.getLevel());
            userProp.setSubType(userChatBubbleDTO.getChatBubbleType());
            userProp.setIsLight(userChatBubbleDTO.getIsEnable());
            userProp.setValidType(userChatBubbleDTO.getValidType());
            if (ValidTypeEnum.COMMON.getCode().equals(userChatBubbleDTO.getValidType())
                    && userChatBubbleDTO.getEndTime() != null) {
                userProp.setLeftTime(userChatBubbleDTO.getEndTime().getTime() - System.currentTimeMillis());
            }
            userProp.setName(userChatBubbleDTO.getChatBubbleName());
            userProp.setDesc(userChatBubbleDTO.getChatBubbleDescribe());
            userProp.setAppLargeUrl(userChatBubbleDTO.getChatBubbleAppBigUrl());
            userProp.setWebLargeUrl(userChatBubbleDTO.getChatBubbleWebBigUrl());
            userProp.setSmallUrl(userChatBubbleDTO.getChatBubbleSmallUrl());
            result = userProp;
        }
        return result;
    }

	public static int getPasswordSafetyRank(String ps) {
		if (ps == null) {
			return -1;
		} else {
			if (Pattern.matches(IS_NORMAL, ps) || !Pattern.matches(IS_PURE_MARS, ps)) {
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
		if (Pattern.matches(EXIST_UPPER_LETTER, ps)) {
			count++;
		}
		if (Pattern.matches(EXIST_LOWER_LETTER, ps)) {
			count++;
		}
		if (Pattern.matches(EXIST_NUMBER, ps)) {
			count++;
		}
		if (!Pattern.matches(EXIST_MARS, ps)) {
			count++;
		}
		if (count >= 3) {
			return true;
		}
		return false;
	}

	private final static String IS_NORMAL = "^[A-Z]+$||^[a-z]+$||^[0-9]+$";

	private final static String IS_PURE_MARS = ".*[a-zA-Z0-9].*";

	private final static String EXIST_MARS = "[a-zA-Z0-9]*";

	private final static String EXIST_UPPER_LETTER = ".*[A-Z].*";

	private final static String EXIST_LOWER_LETTER = ".*[a-z].*";

	private final static String EXIST_NUMBER = ".*[0-9].*";

    public static long incrUserUpdateProfileByType(Integer userId, String type) {
        try {
            String key = String.format(USER_UPDATEPROFILE_KEY, userId, type);
            long curTime = System.currentTimeMillis();
            long endTime = DateUtil.getNextDay(new Date()).getTime();
            return HotDataSource.incTempDataString(key,(int) ((endTime - curTime)/1000));
        } catch(Exception e) {
            logger.error("ProfileServices.setUserUpdateProfileByType(" + userId + "," + type + ") return exception.", e);
            return 0;
        }
    }
}

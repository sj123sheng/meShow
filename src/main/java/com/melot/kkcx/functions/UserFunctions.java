
package com.melot.kkcx.functions;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.blacklist.service.BlacklistService;
import com.melot.kk.logistics.api.domain.UserAddressDO;
import com.melot.kk.logistics.api.domain.UserAddressParam;
import com.melot.kk.logistics.api.service.UserAddressService;
import com.melot.kkcore.account.api.ExtendDataKeys;
import com.melot.kkcore.account.api.ResLogin;
import com.melot.kkcore.account.api.ResMobileGuestUser;
import com.melot.kkcore.account.api.ResRegister;
import com.melot.kkcore.account.api.ResResetPassword;
import com.melot.kkcore.actor.api.ActorInfoKeys;
import com.melot.kkcore.actor.service.ActorService;
import com.melot.kkcore.user.api.LastLoginInfo;
import com.melot.kkcore.user.api.ProfileKeys;
import com.melot.kkcore.user.api.UserInfoDetail;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcore.user.api.UserRegistry;
import com.melot.kkcore.user.api.UserStaticInfo;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kkcx.model.ActorLevel;
import com.melot.kkcx.model.RichLevel;
import com.melot.kkcx.service.GeneralService;
import com.melot.kkcx.service.ProfileServices;
import com.melot.kkcx.service.UserAssetServices;
import com.melot.kktv.action.IndexFunctions;
import com.melot.kktv.action.UserRelationFunctions;
import com.melot.kktv.base.CommonStateCode;
import com.melot.kktv.base.Result;
import com.melot.kktv.domain.SmsConfig;
import com.melot.kktv.model.MedalInfo;
import com.melot.kktv.redis.AppStatsSource;
import com.melot.kktv.redis.HotDataSource;
import com.melot.kktv.redis.MedalSource;
import com.melot.kktv.redis.SmsSource;
import com.melot.kktv.service.AccountService;
import com.melot.kktv.service.ConfigService;
import com.melot.kktv.service.DataAcqService;
import com.melot.kktv.service.UserRelationService;
import com.melot.kktv.service.UserService;
import com.melot.kktv.third.ThirdVerifyUtil;
import com.melot.kktv.third.service.QQService;
import com.melot.kktv.util.AppChannelEnum;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.CityUtil;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.Constant;
import com.melot.kktv.util.DateUtil;
import com.melot.kktv.util.HadoopLogger;
import com.melot.kktv.util.LoginTypeEnum;
import com.melot.kktv.util.ParameterKeys;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.SecurityFunctions;
import com.melot.kktv.util.SmsTypEnum;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.kktv.util.TextFilter;
import com.melot.kktv.util.confdynamic.MedalConfig;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;
import com.melot.module.iprepository.driver.domain.IpInfo;
import com.melot.module.iprepository.driver.service.IpRepositoryService;
import com.melot.module.medal.driver.domain.ConfMedal;
import com.melot.module.medal.driver.domain.UserActivityMedal;
import com.melot.module.medal.driver.service.ActivityMedalService;
import com.melot.module.medal.driver.service.UserMedalService;
import com.melot.module.packagegift.driver.service.VipService;
import com.melot.sdk.core.util.MelotBeanFactory;
import com.melot.sms.api.service.SmsService;

public class UserFunctions {
	
	//2017/04/06
	private static final long deadTime = 1491408000000l; 
	
	/** 日志记录对象 */
	private static Logger logger = Logger.getLogger(UserFunctions.class);
	
	@Autowired
    private ConfigService configService;
	
	@Resource
    ActorService actorService;
	
	private static final String DEFAULT_NICKNAME = "北京爷们儿";
	
	private static String REGEX = ",";
	
	/**
	 * 用户注册(10001002)
	 * 
	 * @param jsonObject 请求对象
	 * @param request 请求对象
	 * @return 注册结果
	 */
	public JsonObject register(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		JsonObject result = new JsonObject();
		
		String username = null, nickname = null, psword = null, isSafe = null, deviceUId = null, clientIp = null;
		int channelId = 0, inviterId = 0, roomFrom = 0, referrerId = 0, appId = AppIdEnum.AMUSEMENT, platform;
		int gpsCityId = 0;//客户端传递的城市ID
		try {
		    psword = CommonUtil.getJsonParamString(jsonObject, "psword", null, "01020002", 6, 40);
			if (psword.length() != 32 && (psword.length() > 16 || psword.length() < 6 || "a111111".equals(psword.trim()))) {
				// 参数psword长度不合法
				result.addProperty("TagCode", "01020006");
				return result;
			}
			username = CommonUtil.getJsonParamString(jsonObject, "username", null, "01020001", 3, 16);
			nickname = username.trim();
			// 昵称长度 最多（国内：10， 海外：20)个字符，多余的位数自动丢弃
			// 如果昵称中有数字，不管是连续还是不连续，都只保留6个，多余的丢弃
			int nickNameLength = configService.getIsAbroad() ? 20 : 10;
			int digitCount = 0;
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < nickname.length(); i++) {
				if (Character.isDigit(nickname.charAt(i))) {
					digitCount++;
					if (digitCount <= 6) {
						sb.append(nickname.charAt(i));
						if (sb.length() >= nickNameLength)
							break;
					}
				} else {
					sb.append(nickname.charAt(i));
					if (sb.length() >= nickNameLength)
						break;
				}
			}
			nickname = sb.toString();
			if (username.length() == digitCount) {
				result.addProperty("TagCode", "01020001");
				return result;
			}
			// 普通注册 若含敏感词词或短连接 直接返回异常码
			if (GeneralService.hasSensitiveWords(0, nickname) || TextFilter.isShortUrl(nickname)
					|| !TextFilter.checkSpecialUnicode(nickname)) {
				result.addProperty("TagCode", "01020001");
				return result;
			}
			//特殊时期，注册不传昵称
			if (!StringUtil.strIsNull(nickname) && configService.getIsSpecialTime()) {
			    nickname = null;
			}
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, "01020003", PlatformEnum.ANDROID, PlatformEnum.IPAD);
            isSafe = CommonUtil.getJsonParamString(jsonObject, "isSafe", null, null, 0, Integer.MAX_VALUE);
            deviceUId = CommonUtil.getJsonParamString(jsonObject, "deviceUId", null, null, 1, Integer.MAX_VALUE);
            inviterId = CommonUtil.getJsonParamInt(jsonObject, "inviterId", 0, null, 0, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 1, Integer.MAX_VALUE);
            referrerId = CommonUtil.getJsonParamInt(jsonObject, "referrerId", 0, null, 0, Integer.MAX_VALUE);
            channelId = CommonUtil.getJsonParamInt(jsonObject, "channel", 0, null, 0, Integer.MAX_VALUE);
            roomFrom = CommonUtil.getJsonParamInt(jsonObject, "roomFrom", 0, null, 0, Integer.MAX_VALUE);
            
            gpsCityId = CommonUtil.getJsonParamInt(jsonObject, "city", 0, null, 0, Integer.MAX_VALUE);
		} catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
		}
		
		String ipAddr = com.melot.kktv.service.GeneralService.getIpAddr(request, appId, platform, clientIp);
		int port = com.melot.kktv.service.GeneralService.getPort(request, appId, platform, 0);
		
		Map<String, Object> map= new HashMap<String, Object>();
		map.put(ExtendDataKeys.INVITERID.key(), inviterId);
		map.put(ExtendDataKeys.ROOMID.key(), roomFrom);
		ResRegister resRegister = AccountService.registerViaUsernamePassword(username, nickname, com.melot.kkcx.service.UserService.getMD5Password(psword),
	    		platform, referrerId, channelId, deviceUId, ipAddr, appId, port, map);
		
		if (resRegister != null && resRegister.getTagCode() != null) {
			String TagCode = (String) resRegister.getTagCode();
			if (TagCode.equals(TagCodeEnum.SUCCESS)) {
				result.addProperty("TagCode", TagCodeEnum.SUCCESS);
				if (resRegister.getUserId() != null) {
					int userId = resRegister.getUserId();
					
					com.melot.kkcx.service.UserService.insertTempUserPassword(userId, psword);
					
					result.addProperty("userId", userId);
					Long showMoney = 0l;
					if (resRegister.getShowMoney() != null) {
						showMoney = (Long) resRegister.getShowMoney();
					}
					// 更新mongodb.
					registerUserAssets(userId, showMoney);
					// 赠送道具
					/*if (channelId != 0) {
						new ChannelRegReward(userId, channelId).start();
					}*/
					// 用户数据采集-成功注册
					DataAcqService.sendRegister(userId, LoginTypeEnum.NAMEPASSWORD, platform);
					
					// 注册日志
					HadoopLogger.registerLog(userId, new Date(), platform, LoginTypeEnum.NAMEPASSWORD,
							referrerId, clientIp, channelId, deviceUId, 0, appId, SecurityFunctions.decodeED(jsonObject));
				}
				
				// 隐藏的获取推荐列表的操作
				try {
					IndexFunctions IndexFunction = MelotBeanFactory.getBean("indexFunction", IndexFunctions.class);
					JsonObject getRecommendedListResult = IndexFunction.getFollowRecommendedList(jsonObject, checkTag, request);
					result.add("getRecommendedListResult", getRecommendedListResult);
				} catch (Exception e) {
					logger.error("fail to get Recommended List, jsonObject -> " + jsonObject.toString(), e);
				}

				// 隐藏的登陆操作
				// 调用login_new得到结果
				JsonObject params = new JsonObject();
				params.addProperty("loginType", LoginTypeEnum.NAMEPASSWORD);
				params.addProperty("username", username);
				params.addProperty("psword", psword);
				params.addProperty("platform", platform);
				params.addProperty("clientIp", clientIp);
				params.addProperty("isSafe", isSafe);
				params.addProperty("city", gpsCityId);
				params.addProperty("a", appId);
				JsonObject loginResult = login_new(params, true, request);
				result.add("loginResult", loginResult);
				
				try {
				    if (loginResult != null && loginResult.has("TagCode")) {
				        String loginTagCode = loginResult.get("TagCode").getAsString();
				        if (!StringUtil.strIsNull(loginTagCode) && !TagCodeEnum.SUCCESS.equals(loginTagCode)) {
				            result.addProperty("TagCode", loginTagCode);
				        }
				    }
                } catch (Exception e) {
                }

				// 返回结果
				return result;
			} else if (TagCode.equals("03") || TagCode.equals("04") || TagCode.equals("05") || TagCode.equals("06") || TagCode.equals("07") || TagCode.equals("08")
					|| TagCode.equals("09") || TagCode.equals("10")) {
				// '03';-- 设备黑名单限制注册
				// '04';-- IP黑名单限制注册
				// '05';-- 到达单台设备注册上限
				// '06';-- 到达单IP,一小时内注册上限
				// '07';-- 到达单IP,一天内注册上限
				// '08';-- 用户名已存在
				// '09';-- 同时传入i_userId,i_deviceUId两个参数,视为恶意用户
				// '10';-- 用户已经注册,或用户ID不存在
				result.addProperty("TagCode", "010201" + TagCode + "");
				return result;
			} else {
				// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
				logger.error("调用存储过程(AccountService.registerViaUsernamePassword(" + username + ", " + nickname + ", " + psword + ", " +
                platform + ", " + referrerId + ", " + channelId + ", " + deviceUId + ", " + ipAddr + ", " +appId+ ", " +port+ "))未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
				result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
				return result;
			}
		} else {
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
		}
		return result;
	}
	
	/**
	 * 修改密码(10001005)
	 * 
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return 修改密码结果
	 */
	public JsonObject changePwd(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
	    JsonObject result = new JsonObject();
	    
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}

		// 获取参数
		JsonElement userIdje = jsonObject.get("userId");
		JsonElement oldpwdje = jsonObject.get("oldpwd");
		JsonElement newpwdje = jsonObject.get("newpwd");
		JsonElement platformje = jsonObject.get("platform");
		// 验证参数
		int userId;
		String oldpwd;
		String newpwd;
		Integer platform = null;
		if (userIdje != null && !userIdje.isJsonNull() && !userIdje.getAsString().equals("")) {
			// 验证数字
			try {
				userId = userIdje.getAsInt();
			} catch (Exception e) {
				result.addProperty("TagCode", "01050002");
				return result;
			}
		} else {
			result.addProperty("TagCode", "01050001");
			return result;
		}
		if (oldpwdje != null && !oldpwdje.isJsonNull() && !oldpwdje.getAsString().equals("")) {
			oldpwd = oldpwdje.getAsString();
		} else {
			result.addProperty("TagCode", "01050003");
			return result;
		}
		if (newpwdje != null && !newpwdje.isJsonNull() && !newpwdje.getAsString().equals("")) {
			newpwd = newpwdje.getAsString();
			
			if (newpwd.length() != 32 && (newpwd.length() > 16 || newpwd.length() < 6 || "a111111".equals(newpwd.trim()))) {
			    result.addProperty("TagCode", "01050004");
	            return result;
            }
		} else {
			result.addProperty("TagCode", "01050004");
			return result;
		}
		if (platformje != null && !platformje.isJsonNull() && !platformje.getAsString().isEmpty()) {
			try {
				platform = platformje.getAsInt();
			} catch (Exception e) {
				result.addProperty("TagCode", "01050006");
				return result;
			}
		} else {
			result.addProperty("TagCode", "01050005");
			return result;
		}
		
		int funcTag = 0;
        String parameter = request.getParameter("parameter");
        if (parameter == null) {
            JsonObject paramJsonObject = null;
            try {
                JsonParser parser = new JsonParser();
                paramJsonObject = parser.parse(parameter).getAsJsonObject();
                JsonElement FuncTagje = paramJsonObject.get("FuncTag");
                if (FuncTagje != null) {
                    funcTag = FuncTagje.getAsInt();
                }
            } catch (Exception e) {
            }
        }
		
		JsonElement usernameje = jsonObject.get("usernameEnc");
		String username = null;
		if (funcTag == 40000008) {// 安全修改密码接口
			if (usernameje != null && !usernameje.isJsonNull() && !usernameje.getAsString().equals("")) {
				username = usernameje.getAsString();
			} else {
				result.addProperty("TagCode", "41050005");
				return result;
			}
		}
		
		KkUserService kkUserService = MelotBeanFactory.getBean("kkUserService", KkUserService.class);
		UserStaticInfo staticInfo = kkUserService.getUserStaticInfo(userId);
		if (staticInfo.getProfile().getPhoneNum() == null && 
			staticInfo.getRegisterInfo().getLoginName() == null &&
			newpwd.matches("[0-9]+")) {
			result.addProperty("TagCode", "01050004");
			return result;
		}
		
		oldpwd = com.melot.kkcx.service.UserService.getMD5Password(oldpwd);
		int level = ProfileServices.getPasswordSafetyRank(newpwd);
		String TagCode = AccountService.changePassword(userId, oldpwd, com.melot.kkcx.service.UserService.getMD5Password(newpwd), platform, 
				com.melot.kktv.service.GeneralService.getIpAddr(request, AppIdEnum.AMUSEMENT, platform, null), level);
		if (TagCode.equals(TagCodeEnum.SUCCESS)) {
			com.melot.kkcx.service.UserService.insertTempUserPassword(userId, newpwd);
			
			if (funcTag == 40000008) {// 安全修改密码接口
				result.addProperty("u", username);
			}
			result.addProperty("TagCode", TagCode);
			
			//发消息通知node踢出房间
			JsonObject msgJson = new JsonObject();
			msgJson.addProperty("MsgTag", 50010105);
			msgJson.addProperty("userId", userId);
			GeneralService.sendMsgToRoom(1, 0, 0, 0, msgJson);
			
			//SVIP发送安全提醒
			VipService vipService = (VipService) MelotBeanFactory.getBean("vipService");
			if (vipService != null) {
				List<Integer> list = vipService.getUserProp(userId);
				if (list != null && list.size() > 0 && list.contains(100004)) {
					UserProfile userProfile = UserService.getUserInfoV2(userId);
					String phoneNum;
					if (userProfile != null && (phoneNum = userProfile.getPhoneNum()) != null) {
						SmsConfig smsConfig = GeneralService.getSmsMsgFormat(1, 100, platform, 17);
						if (smsConfig != null) {
						    int dailyCount = smsConfig.getDailyCount().intValue();
                            // 单日短信发送个数限制
                            int todayCount = SmsSource.getSendSmsCount(String.valueOf(17), phoneNum);
                            if (todayCount < dailyCount) {
                                String nickName = userProfile.getNickName();
                                String dateString = DateUtil.formatDateTime(new Date(), null);
                                String format = smsConfig.getMessage();
                                String message = String.format(format, nickName, userId, dateString);
//                                SmsSource.sendSms(phoneNum, String.valueOf(17), message, 1);
                                
                                SmsService smsService = MelotBeanFactory.getBean("smsService", SmsService.class);
                                smsService.sendSms(1,17,phoneNum,message,userId,platform,100);
                            }
						} else {
							logger.error("Mongodb中未找到短信配置信息! smsType:" + 17 + ", userId:" + userId + ", phoneNum:" + phoneNum);
						}
					}
				}
			} else {
				logger.error("fail to call VipService.getUserProp, userId : " + userId);
			}
			// 返回结果
			return result;
		} else if (TagCode.equals("02")) {
			/* '02';旧密码错误 */
			result.addProperty("TagCode", "010501" + TagCode + "");
			return result;
		} else if (TagCode.equals("03")) {
			// ID登录为唯一登录方式时不允许纯数字
			result.addProperty("TagCode", "01050004");
			return result;
		}  else {
			// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
			logger.error("调用存储过程(AccountService.changePassword(" + userId + ", " + oldpwd + ", " + newpwd + ", " + platform + ", "
					+ com.melot.kktv.service.GeneralService.getIpAddr(request, AppIdEnum.AMUSEMENT, platform, null) + "))未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
			result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
			return result;
		}
	}
	
	/**
	 * 生成游客用户ID(40000016)
	 * 
	 * @param jsonObject 请求对象
	 * @param request 请求对象
	 * @return
	 */
	public JsonObject genMobileGuestUser(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		
		// define usable parameters
		int platform = 0;
		int appId = 0;
		int channel = 0;
		String deviceUId = null;
		// parse the parameters
		JsonObject result = new JsonObject();
		try {
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.ANDROID, TagCodeEnum.PLATFORM_MISSING, 1, Integer.MAX_VALUE);
			appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, TagCodeEnum.APPID_MISSING, 0, Integer.MAX_VALUE);
			channel = CommonUtil.getJsonParamInt(jsonObject, "c", AppChannelEnum.KK, TagCodeEnum.CHANNEL_MISSING, 0, Integer.MAX_VALUE);
			deviceUId = CommonUtil.getJsonParamString(jsonObject, "deviceUId", null, TagCodeEnum.DEVICEUID_MISSING, 1, 150);
		} catch(CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch(Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		
		// 手机客户端IP
		String clientIp = com.melot.kktv.service.GeneralService.getIpAddr(request, appId, platform, null);
		int port = com.melot.kktv.service.GeneralService.getPort(request, appId, platform, 0);
		
		com.melot.kkcore.account.service.AccountService accountService = (com.melot.kkcore.account.service.AccountService) MelotBeanFactory.getBean("kkAccountService");
		ResMobileGuestUser resMobileGuestUser = null;
		String nickname = getDistrictNickname(clientIp);
		if (accountService != null) {
		    resMobileGuestUser = accountService.genMobileGuestUserNew(platform, deviceUId, clientIp, appId, channel, port, nickname);
		}
		if (resMobileGuestUser != null && resMobileGuestUser.getTagCode() != null) {
			String TagCode = resMobileGuestUser.getTagCode();
			if (TagCode.equals(TagCodeEnum.SUCCESS)) {
				// 拼接结果
				
				Integer userId = null;
				
				result.addProperty("TagCode", TagCodeEnum.SUCCESS);
				if (resMobileGuestUser.getUserId() != null) {
					userId = resMobileGuestUser.getUserId();
					if (!com.melot.kkcx.service.UserService.getGuestFirstRecord(userId)) {
						com.melot.kkcx.service.UserService.insertGuestFirstRecord(userId, platform, appId, channel, deviceUId, resMobileGuestUser.getTimestamp() == null ? new Date() : new Date(resMobileGuestUser.getTimestamp()));
					}
					result.addProperty("userId", resMobileGuestUser.getUserId());
				}
				if (resMobileGuestUser.getCity() != null) {
					result.addProperty("city", resMobileGuestUser.getCity());
				}
				if (resMobileGuestUser.getArea() != null) {
					result.addProperty("area", resMobileGuestUser.getArea());
				}
				if (resMobileGuestUser.getCanInvite() != null) {
					result.addProperty("canInvite", resMobileGuestUser.getCanInvite());
				}
                if (resMobileGuestUser.getTimestamp() != null) {
                    result.addProperty("timestamp", resMobileGuestUser.getTimestamp());
                }
                UserProfile userProfile = UserService.getUserInfoV2(resMobileGuestUser.getUserId());
                if (userProfile != null && !StringUtil.strIsNull(userProfile.getNickName())) {
                    nickname = userProfile.getNickName();
                } else {
                    //旧的没有昵称游客添加昵称
                    Map<String, Object> userMap = new HashMap<String, Object>();
                    userMap.put(ProfileKeys.NICKNAME.key(), nickname);
                    com.melot.kktv.service.UserService.updateUserInfoV2(userId, userMap);
                }
                result.addProperty("nickname", nickname);
				
				// 注册日志
				HadoopLogger.registerLog((userId==null?0:userId.intValue()), new Date(), platform, LoginTypeEnum.GUEST,
						0, clientIp, 0, deviceUId, 0, appId, SecurityFunctions.decodeED(jsonObject));
				
				// APP渠道推广激活日志
				String ua = CommonUtil.getUserAgent(request);
				if (clientIp != null && ua != null) {
					if (ua.indexOf("Android") > 0) {
						ua = ua.replaceFirst("U; ", "");
						ua = ua.toLowerCase();
						ua = ua.replaceFirst("build", "");
						String locale = containLocale(ua);
						if (locale != null) {
							ua = ua.replaceFirst(locale + "; ", "");
						}
						ua = replaceBlank(ua);
					}
					if (ua.indexOf("iPhone") > 0) {
						ua = ua.replaceFirst("CPU ", "");
						ua = ua.replaceFirst(" like Mac OS X", "");
						ua = ua.toLowerCase();
						String locale = containLocale(ua);
						if (locale != null) {
							ua = ua.replaceFirst("; "+ locale, "");
						}
						ua = ua.replaceAll("_", ".");
						ua = replaceBlank(ua);
					}
					String name = CommonUtil.md5(appId + clientIp + ua);
					String value = AppStatsSource.getAppPromoteInfo(name);
					if (value != null) {
						HadoopLogger.promoteActiveLog((userId==null?0:userId.intValue()),
								clientIp, ua, Integer.parseInt(value), platform, channel, new Date(), appId);
					}
				}
			} else if (TagCode.equals("03") || TagCode.equals("04") || TagCode.equals("05")
					|| TagCode.equals("06") || TagCode.equals("07")) {
				// '03';-- 设备黑名单限制注册
				// '04';-- IP黑名单限制注册
				// '05';-- 到达单台设备注册上限
				// '06';-- 到达单IP,一小时内注册上限
				// '07';-- 到达单IP,一天内注册上限
				result.addProperty("TagCode", "011101" + TagCode);
			} else {
				result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
			}
		} else {
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
		}
		
		return result;
	}
	
	/**
     * 获取游客昵称(51010105)
     * 
     * @param jsonObject 请求对象
     * @param request 请求对象
     * @return
     */
    public JsonObject getGuestName(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        
        int platform = 0;
        int appId = 0;
        int guestUid = 0;
        String clientIp = null;
        JsonObject result = new JsonObject();
        try {
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, 1, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 0, Integer.MAX_VALUE);
            guestUid = CommonUtil.getJsonParamInt(jsonObject, "guestUid", 0, null, 0, Integer.MAX_VALUE);
            clientIp = CommonUtil.getJsonParamString(jsonObject, "clientIp", null, null, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        String nickname = null;
        if (guestUid > 0) {
            nickname = ProfileServices.getGuestNickName(guestUid);
        }
        if (StringUtil.strIsNull(nickname)) {
            clientIp = com.melot.kktv.service.GeneralService.getIpAddr(request, appId, platform, clientIp);
            nickname = getDistrictNickname(clientIp);
            ProfileServices.setGuestNickName(guestUid, nickname);
        }
        result.addProperty("nickname", nickname);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
	
	private String getDistrictNickname(String ipAddr) {
	    String result = null;
	    String areaName = null;
	    String nicknamePre = DEFAULT_NICKNAME;
	    try {
	        IpRepositoryService ipRepositoryService = (IpRepositoryService) MelotBeanFactory.getBean("ipRepositoryService");
	        IpInfo ipInfo = ipRepositoryService.getIpInfo(ipAddr);
	        if (ipInfo != null) {
	            areaName = ipInfo.getProvince();
	        }
	        String[] regionNicknames = configService.getRegionNickname().split(REGEX);
	        if (!StringUtil.strIsNull(areaName)) {
	            for (String regionNickStr : regionNicknames) {
	                if (regionNickStr.contains(areaName)) {
	                    nicknamePre = regionNickStr;
	                    break;
	                }
	            }
	        } else {
                nicknamePre = regionNicknames[new Random().nextInt(regionNicknames.length -1)];
            }
	    } catch(Exception e) {
	        logger.info("UserFunctions.getDistrictNickname execute exception, ipAdrr: " + ipAddr, e);
	    }
	    
	    result = nicknamePre + CommonUtil.getRandomDigit(5);
	    return result;
	}

	/**
	 * 用户登出(10001025)
	 * 
	 * @param jsonObject 请求对象
	 * @param request 请求对象
	 * 
	 * @return
	 */
	public JsonObject logout(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
	    JsonObject result = new JsonObject();
	    
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		// 获取参数
		Integer userId = null;
		JsonElement userIdje = jsonObject.get("userId");
		// 验证参数
		if (userIdje != null && !userIdje.isJsonNull() && !userIdje.getAsString().isEmpty()) {
			// 验证数字
			try {
				userId = userIdje.getAsInt();
			} catch (Exception e) {
				result.addProperty("TagCode", "01250002");
				return result;
			}
		} else {
			result.addProperty("TagCode", "01250001");
			return result;
		}
		
		AccountService.logout(userId, AppIdEnum.AMUSEMENT);
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
	}

	/**
	 * 第三方平台用户注册(10001006)
	 * 
	 * @param jsonObject 请求对象
	 * @param request 请求对象
	 * @return 注册结果
	 */
	public JsonObject registerVia3rdPlatform(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		
		JsonObject result = new JsonObject();
		
		// define usable parameters
		int openPlatform, platform, gender, referrerId, channelId, userId, appId, inviterId, roomFrom, refRoomId;
		String uuid = null;
		int gpsCityId = 0;//客户端的定位城市信息
		// 微信 数据
		String unionid = null;
		String nickname = null;
		String deviceUId = null;
		String sessionId = null;
		String isSafe = null;
		String clientIp = null;
		// 快牙数据
		String phoneNum = null;
		try {
			appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 1, Integer.MAX_VALUE);
			openPlatform = CommonUtil.getJsonParamInt(jsonObject, "openPlatform", 0, "01060001", 1, Integer.MAX_VALUE);
			uuid = CommonUtil.getJsonParamString(jsonObject, "uuid", null, "01060003", 1, Integer.MAX_VALUE);
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, "01060004", 1, Integer.MAX_VALUE);
			if (platform != PlatformEnum.WEB && platform != PlatformEnum.ANDROID && platform != PlatformEnum.IPHONE && platform != PlatformEnum.IPAD) {
				// platform参数非法
				result.addProperty("TagCode", "01060008");
				return result;
			}
			referrerId = CommonUtil.getJsonParamInt(jsonObject, "referrerId", 0, null, 1, Integer.MAX_VALUE);
			channelId = CommonUtil.getJsonParamInt(jsonObject, "channel", 0, null, 1, Integer.MAX_VALUE);
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 0, Integer.MAX_VALUE);
			isSafe = CommonUtil.getJsonParamString(jsonObject, "isSafe", null, null, 1, Integer.MAX_VALUE);
			deviceUId = CommonUtil.getJsonParamString(jsonObject, "deviceUId", null, null, 1, Integer.MAX_VALUE);
			gender = CommonUtil.getJsonParamInt(jsonObject, "gender", 0, null, 0, Integer.MAX_VALUE);
			unionid = CommonUtil.getJsonParamString(jsonObject, "unionid", null, null, 1, Integer.MAX_VALUE);
			sessionId = CommonUtil.getJsonParamString(jsonObject, "sessionId", null, null, 1, Integer.MAX_VALUE);
			nickname = CommonUtil.getJsonParamString(jsonObject, "nickname", null, null, 1, Integer.MAX_VALUE);
			inviterId = CommonUtil.getJsonParamInt(jsonObject, "inviterId", 0, null, 0, Integer.MAX_VALUE);
			roomFrom = CommonUtil.getJsonParamInt(jsonObject, "roomFrom", 0, null, 0, Integer.MAX_VALUE);
			refRoomId = CommonUtil.getJsonParamInt(jsonObject, "refRoomId", 0, null, 0, Integer.MAX_VALUE);
			gpsCityId = CommonUtil.getJsonParamInt(jsonObject, "city", 0, null, 0, Integer.MAX_VALUE);
			//特殊时期注册不传昵称
			if (configService.getIsSpecialTime()) {
			    nickname = null;
			}
			if (!StringUtil.strIsNull(nickname)) {
				nickname = nickname.trim();
				// matchXSSTag
				if (CommonUtil.matchXSSTag(nickname)) {
					result.addProperty("TagCode", "01060012");
					return result;
				}
				// 第三方注册 若含敏感词词或短连接 重命名为"新人+6位随机数"
				if (GeneralService.hasSensitiveWords(userId, nickname) || TextFilter.isShortUrl(nickname)
						|| !TextFilter.checkSpecialUnicode(nickname)) {
					nickname = "新人" + CommonUtil.getRandomDigit(6);
				}
				// 昵称长度 最多10个字符，多余的位数自动丢弃
				// 如果昵称中有数字，不管是连续还是不连续，都只保留6个，多余的丢弃
				int nickNameLength = configService.getIsAbroad() ? 20 : 10;
				int digitCount = 0;
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < nickname.length(); i++) {
					if (Character.isDigit(nickname.charAt(i))) {
						digitCount++;
						if (digitCount <= 6) {
							sb.append(nickname.charAt(i));
							if (sb.length() >= nickNameLength)
								break;
						}
					} else {
						sb.append(nickname.charAt(i));
						if (sb.length() >= nickNameLength)
							break;
					}
				}
				nickname = sb.toString();
			}
			if (platform == PlatformEnum.WEB) {
			    clientIp = CommonUtil.getJsonParamString(jsonObject, "clientIp", CommonUtil.getIpAddr(request), null, 1, Integer.MAX_VALUE);
			    if (!ConfigHelper.getAllowedRegisterClientIp().contains(CommonUtil.getIpAddr(request))) {
			    	// web端第三方注册ip限制
			    	result.addProperty("TagCode", "01060016");
			    	return result;
			    }
			}
			phoneNum = CommonUtil.getJsonParamString(jsonObject, "phoneNum", null, null, 1, Integer.MAX_VALUE);
		} catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		
		// 若为支付宝第三方用户 判断sessionId
		String alipayUserInfo = null;
		if ((openPlatform == LoginTypeEnum.QQ || openPlatform == LoginTypeEnum.WEIBO
                || openPlatform == LoginTypeEnum.WEIXIN|| openPlatform == LoginTypeEnum.ALIPAY
                || openPlatform == LoginTypeEnum.FACEBOOK)
                && sessionId == null) {
            //老版本qq和微博不传session,过渡:兼容不做验证  2016-1-13 cj test
		    //兼容下客户端不传sessionId时,unionId传"(null)"
		    if (openPlatform == LoginTypeEnum.QQ) {
		        unionid = null;
		    }

		    if (openPlatform == LoginTypeEnum.FACEBOOK && unionid != null) {
                com.melot.kkcore.account.service.AccountService accountService = (com.melot.kkcore.account.service.AccountService) MelotBeanFactory.getBean("kkAccountService");
                String[] uuidArr = unionid.split(",");
                for (String uuidStr : uuidArr) {
                    if (accountService.isUuidValid(uuidStr, openPlatform) > 0) {
                        result.addProperty("TagCode", "01060103");
                        return result;
                    }
                }
                unionid = null;
            }
        } else {
			//Third user login verify
			if (openPlatform == LoginTypeEnum.DIDA) {
				//70091 固定channelid
				uuid = uuid.replaceFirst(String.valueOf(70091), "");
			}
			ThirdVerifyUtil thirdVerifyUtil = (ThirdVerifyUtil) MelotBeanFactory.getBean("thirdVerifyUtil");
			String ret = thirdVerifyUtil.verify(openPlatform, uuid, sessionId, platform);
			if (ret != null) {
				if (openPlatform == LoginTypeEnum.ALIPAY) {
					alipayUserInfo = ret;
				}
				if (openPlatform == LoginTypeEnum.QQ) {
				    QQService qqService = (QQService) MelotBeanFactory.getBean("qqService");
                    unionid = qqService.getUnionID(sessionId);
				}
			} else {
				result.addProperty("TagCode", "01060048");
				return result;
			}
		}
		
		String ipAddr = com.melot.kktv.service.GeneralService.getIpAddr(request, appId, platform, clientIp);
		int port = com.melot.kktv.service.GeneralService.getPort(request, appId, platform, 0);
		
		//添加alipayUserInfo参数
		Map<String, Object> map= new HashMap<String, Object>();
		map.put(ExtendDataKeys.INVITERID.key(), inviterId);
		map.put(ExtendDataKeys.ROOMID.key(), roomFrom);
		map.put("refRoomId", refRoomId);
		ResRegister resRegister = AccountService.registerViaOpenPlatform(openPlatform, uuid, unionid,nickname, gender, userId, platform, referrerId, channelId, deviceUId, ipAddr, appId, alipayUserInfo, port, map);
		if (resRegister != null && resRegister.getTagCode() != null) {
			String TagCode = (String) resRegister.getTagCode();
			if (TagCode.equals(TagCodeEnum.SUCCESS)) {
				if (resRegister.getUserId() != null) {
					int RuserId = resRegister.getUserId();
					result.addProperty("userId", RuserId);
					Long showMoney = 0l;
					if (resRegister.getShowMoney() != null) {
						showMoney = (Long) resRegister.getShowMoney();
					}
					
					if (openPlatform == LoginTypeEnum.KUAIYA && phoneNum != null) {
						Map<Object, Object> upMap = new HashMap<Object, Object>();
						upMap.put("phoneNum", phoneNum);
						upMap.put("userId", RuserId);
						upMap.put("uuid", uuid);
						SqlMapClientHelper.getInstance(DB.MASTER).update("User.updateKuaiyaUserPhoneNum", upMap);
					}
					
					if (openPlatform == LoginTypeEnum.MALA && phoneNum != null) {
					    ProfileServices.identifyPhone(RuserId, phoneNum);
					}
					
					registerUserAssets(RuserId, showMoney);
					
					// 赠送道具
					/*if (channelId != 0) {
						new ChannelRegReward(RuserId, channelId).start();
					}*/
					// 用户数据采集-成功注册
					DataAcqService.sendRegister(RuserId, openPlatform, platform);
					
					// 注册日志
					HadoopLogger.registerLog(RuserId, new Date(), platform, openPlatform,
							referrerId, ipAddr, channelId, deviceUId, 0, appId, SecurityFunctions.decodeED(jsonObject));
				}
				
				// 拼接结果
				result.addProperty("TagCode", TagCodeEnum.SUCCESS);
				result.addProperty("userId", userId);

				// 隐藏的登陆操作,调用login_new得到结果
				JsonObject params = new JsonObject();
				params.addProperty("loginType", openPlatform);
				params.addProperty("uuid", uuid);
				params.addProperty("platform", platform);
				params.addProperty("clientIp", clientIp);
				params.addProperty("isSafe", isSafe);
				params.addProperty("city", gpsCityId);
				params.addProperty("a", appId);
				JsonObject loginResult = login_new(params, true, request);
				result.add("loginResult", loginResult);
                
                try {
                    if (loginResult != null && loginResult.has("TagCode")) {
                        String loginTagCode = loginResult.get("TagCode").getAsString();
                        if (!StringUtil.strIsNull(loginTagCode) && !TagCodeEnum.SUCCESS.equals(loginTagCode)) {
                            result.addProperty("TagCode", loginTagCode);
                        }
                    }
                } catch (Exception e) {
                }
				
				// 返回结果
				return result;
			} else if (TagCode.equals("02") || TagCode.equals("04") || TagCode.equals("05") || TagCode.equals("06") || TagCode.equals("07")
					|| TagCode.equals("08") || TagCode.equals("09")) {
				// '02': uuid长度不能小于16
				// '03': uuid已存在
				// '05': 注册方式缺失
				// '06': IP黑名单限制注册
				// '07': 到达单IP,一小时内注册上限
				// '08': 到达单IP,一天内注册上限
				// '09': 封号处理
				result.addProperty("TagCode", "010601" + TagCode);
				return result;
			} else if (TagCode.equals("03")) {
			    if (openPlatform == LoginTypeEnum.WEIXIN && unionid != null) {
			        // 隐藏的登陆操作,调用login_new得到结果
	                JsonObject params = new JsonObject();
	                params.addProperty("loginType", openPlatform);
	                params.addProperty("uuid", uuid);
	                params.addProperty("unionid", unionid);
	                params.addProperty("platform", platform);
	                params.addProperty("clientIp", clientIp);
	                params.addProperty("isSafe", isSafe);
	                params.addProperty("city", gpsCityId);
	                params.addProperty("a", appId);
	                JsonObject loginResult = login_new(params, true, request);
	                result.add("loginResult", loginResult);
	                
	                try {
	                    if (loginResult != null && loginResult.has("TagCode")) {
	                        String loginTagCode = loginResult.get("TagCode").getAsString();
	                        if (!StringUtil.strIsNull(loginTagCode) && !TagCodeEnum.SUCCESS.equals(loginTagCode)) {
	                            result.addProperty("TagCode", loginTagCode);
	                        } else {
	                            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
	                            result.add("userId", loginResult.get("userId"));
	                            return result;
	                        }
	                    }
	                } catch (Exception e) {
	                }
			    }
			    result.addProperty("TagCode", "010601" + TagCode);
                return result;
			} else {
				// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
				logger.error("调用存储过程(AccountService.registerViaOpenPlatform(" + openPlatform + ", " + uuid + ", " + unionid + ", " + nickname + ", " + gender + ", " + userId + ", " + platform + ", " + referrerId + ", " + channelId + ", " + deviceUId + ", " + ipAddr + ", " + appId + ", " + alipayUserInfo + ", " + port + "))未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
				
				result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
				return result;
			}
		} else {
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
		}
		return result;
		
	}
	
	/**
	 * 用户初始化更新信息(10001008)
	 * 
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return 登录结果
	 */
	public JsonObject initUpdate(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
	    JsonObject result = new JsonObject();
	    
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		// 更新基本信息
		ProfileFunctions profileFunctions = MelotBeanFactory.getBean("profileFunction", ProfileFunctions.class);
		JsonObject updateBasicInfoResult = profileFunctions.updateUserInfo(jsonObject, checkTag, request);
		if (!updateBasicInfoResult.get("TagCode").getAsString().equals(TagCodeEnum.SUCCESS)) {
			result.addProperty("TagCode", updateBasicInfoResult.get("TagCode").getAsString());
			return result;
		}
		// 更新关注信息
		UserRelationFunctions userRelationFunctions = MelotBeanFactory.getBean("userRelationFunction", UserRelationFunctions.class);
		JsonObject updateFollowResult = userRelationFunctions.follow(jsonObject, checkTag, request);

		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		result.add("updateBasicInfoResult", updateBasicInfoResult);
		result.add("updateFollowResult", updateFollowResult);
		return result;
	}
	
   /**
    * 游客注册(40000017)
    * 
    * @param jsonObject 请求对象
    * @param request 请求对象
    * @return 注册结果
    */
   public JsonObject registerIphoneBackground(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
       JsonObject result = new JsonObject();
       
       int userId, openPlatform, platform, appId, channelId, inviterId, roomFrom;
       String password, deviceUId, isSafe;
       int gpsCityId = 0;// 客户端通过GPS给出的定位cityID
       try {
           userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
           openPlatform = CommonUtil.getJsonParamInt(jsonObject, "openPlatform", -7, "40170001", Integer.MIN_VALUE, Integer.MAX_VALUE);
           platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, TagCodeEnum.PLATFORM_MISSING, 1, Integer.MAX_VALUE);

           appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 1, Integer.MAX_VALUE);
           channelId = CommonUtil.getJsonParamInt(jsonObject, "c", AppChannelEnum.KK, null, 1, Integer.MAX_VALUE);
           inviterId = CommonUtil.getJsonParamInt(jsonObject, "inviterId", 0, null, 0, Integer.MAX_VALUE);
           roomFrom = CommonUtil.getJsonParamInt(jsonObject, "roomFrom", 0, null, 0, Integer.MAX_VALUE);

           password = CommonUtil.getJsonParamString(jsonObject, "psword", null, "40170002", 6, 32);
           deviceUId = CommonUtil.getJsonParamString(jsonObject, "deviceUId", null, TagCodeEnum.DEVICEUID_MISSING, 1, Integer.MAX_VALUE);
           isSafe = CommonUtil.getJsonParamString(jsonObject, "isSafe", null, null, 1, Integer.MAX_VALUE);
           
           gpsCityId = CommonUtil.getJsonParamInt(jsonObject, "city", 0, null, 0, Integer.MAX_VALUE);
       } catch (CommonUtil.ErrorGetParameterException e) {
           result.addProperty("TagCode", e.getErrCode());
           return result;
       } catch (Exception e) {
           result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
           return result;
       }
       
       String ipAddr = com.melot.kktv.service.GeneralService.getIpAddr(request, appId, platform, null);
       int port = com.melot.kktv.service.GeneralService.getPort(request, appId, platform, 0);
       
       Map<String, Object> map= new HashMap<String, Object>();
       map.put(ExtendDataKeys.INVITERID.key(), inviterId);
       map.put(ExtendDataKeys.ROOMID.key(), roomFrom);
       ResRegister resRegister = AccountService.registerIphoneBackground(userId, com.melot.kkcx.service.UserService.getMD5Password(password), openPlatform,
               platform, deviceUId, ipAddr, appId, channelId, port, map);
       
       if (resRegister != null && resRegister.getTagCode() != null) {
           String TagCode = (String) resRegister.getTagCode();
           if (TagCode.equals(TagCodeEnum.SUCCESS)) {
        	   com.melot.kkcx.service.UserService.insertTempUserPassword(userId, password);
        	   
               result.addProperty("TagCode", TagCodeEnum.SUCCESS);
               if (resRegister.getUserId() != null) {
                   userId = resRegister.getUserId();
                   result.addProperty("userId", userId);
                   Long showMoney = 0l;
                   if (resRegister.getShowMoney() != null) {
                       showMoney = (Long) resRegister.getShowMoney();
                   }
                   // 更新mongodb.
                   registerUserAssets(userId, showMoney);
                   
                   // 用户数据采集-成功注册
                   DataAcqService.sendRegister(userId, LoginTypeEnum.IPHONE_BACKGROUND, platform);
                   
                   // 注册日志
                   HadoopLogger.registerLog(userId, new Date(), platform, LoginTypeEnum.IPHONE_BACKGROUND,
                           0, com.melot.kktv.service.GeneralService.getIpAddr(request, appId, platform, null), channelId, deviceUId, 0, appId, SecurityFunctions.decodeED(jsonObject));
               }

               // 隐藏的登陆操作
               // 调用login_new得到结果
               JsonObject params = new JsonObject();
               params.addProperty("loginType", LoginTypeEnum.IDPASSWORD);
               params.addProperty("userId", userId);
               params.addProperty("psword", resRegister.getPassword());
               params.addProperty("platform", platform);
               params.addProperty("isSafe", isSafe);
               params.addProperty("city", gpsCityId);
               params.addProperty("a", appId);
               JsonObject loginResult = login_new(params, true, request);
               result.add("loginResult", loginResult);
               
               try {
                   if (loginResult != null && loginResult.has("TagCode")) {
                       String loginTagCode = loginResult.get("TagCode").getAsString();
                       if (!StringUtil.strIsNull(loginTagCode) && !TagCodeEnum.SUCCESS.equals(loginTagCode)) {
                           result.addProperty("TagCode", loginTagCode);
                       }
                   }
               } catch (Exception e) {
               }

               // 返回结果
               return result;
           } else if (TagCode.equals("06") || TagCode.equals("07") || TagCode.equals("08")
                   || TagCode.equals("09")) {
               /*
                *  '06';-- IP黑名单限制注册
                *  '07';-- 到达单IP,一小时内注册上限
                *  '08';-- 到达单IP,一天内注册上限
                *  '09';-- 传入的游客信息无效
               */
               result.addProperty("TagCode", "401700" + TagCode + "");
               return result;
           } else {
               // 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
               logger.error("调用存储过程(AccountService.registerIphoneBackground(" + userId + ", " + password + ", " + openPlatform + ", " + platform + ", " + deviceUId + ", " + ipAddr + ", " + appId + ", " + channelId + ", " + port + "))未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
               result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
               return result;
           }
       } else {
           result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
       }
       return result;
   }
	
	/**
	 * 根据用户ID,获取用户来自的第三方平台   客户端指定平台(openplatform参数),服务端返回该平台uuid(10001012)
	 * 
	 * @param jsonObject 请求对象
	 * @param request 请求对象
	 * @return
	 */
	public JsonObject getUserFromByUserId(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
	    JsonObject result = new JsonObject();
	    
		// 获取参数
		JsonElement userIdje = jsonObject.get("userId");

		// 验证platform参数
		int userId;
		if (userIdje != null && !userIdje.isJsonNull() && !userIdje.getAsString().equals("")) {
			// 验证数字
			try {
				userId = userIdje.getAsInt();
			} catch (Exception e) {
				result.addProperty("TagCode", "01120002");
				return result;
			}
		} else {
			result.addProperty("TagCode", "01120001");
			return result;
		}

		// 调用存储过程得到结果
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("userId", userId);
		UserRegistry userRegistry = com.melot.kkcx.service.UserService.getUserRegistryInfo(userId);
		if (userRegistry == null) {
			result.addProperty("TagCode", "01120102");
			return result;
		}
		map.put("openPlatform", userRegistry.getOpenPlatform());
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("User.getUserFromByUserId", map);
		} catch (SQLException e) {
			logger.error("未能正常调用存储过程", e);
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			return result;
		}
		String TagCode = (String) map.get("TagCode");
		if (TagCode.equals(TagCodeEnum.SUCCESS)) {
			// 定义结果并组装结果字符串
			result.addProperty("TagCode", TagCode);
			result.addProperty("openPlatform", userRegistry.getOpenPlatform());
			if ((String) map.get("uuid") != null) {
				result.addProperty("uuid", (String) map.get("uuid"));
			}
			// 返回结果
			return result;
		} else if (TagCode.equals("03")) {
			// user not 3rd platform
			result.addProperty("TagCode", "011201" + TagCode);
			// 返回结果
			return result;
		} else {
			// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
			logger.error("调用存储过程(User.getUserFromByUserId(" + new Gson().toJson(map) + "))未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
			result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
			return result;
		}
	}

	/**
	 * 新登录接口(10001013)
	 * 
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * 
	 * @return 登录结果
	 */
   public JsonObject login_new(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
	    JsonObject result = new JsonObject();
	    
        int loginType, appId, channel, inviterId, userId = 0, platform = 0, roomFrom;
        int gpsCityId;// 客户端通过GPS拿到的城市定位ID【参数里面是city】
        String isSafe, username = null, phoneNum = null, psword = null, token = null, uuid = null, unionid = null, deviceUId = null, clientIp = null, sessionId = null;
        try {
            loginType = CommonUtil.getJsonParamInt(jsonObject, "loginType", 0, "01130001", Integer.MIN_VALUE, Integer.MAX_VALUE);
            isSafe = CommonUtil.getJsonParamString(jsonObject, "isSafe", null, null, 0, 256);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 0, Integer.MAX_VALUE);
            channel = CommonUtil.getJsonParamInt(jsonObject, "c", AppChannelEnum.KK, null, 0, Integer.MAX_VALUE);
            inviterId = CommonUtil.getJsonParamInt(jsonObject, "inviterId", 0, null, 0, Integer.MAX_VALUE);
            roomFrom = CommonUtil.getJsonParamInt(jsonObject, "roomFrom", 0, null, 0, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, "01130009", 0, Integer.MAX_VALUE);
            gpsCityId = CommonUtil.getJsonParamInt(jsonObject, "city", 0, null, 0, Integer.MAX_VALUE);
            
            if (platform != PlatformEnum.WEB) {
                // 为了兼容旧版本,这里不强制要求传入设备唯一标识
                deviceUId = CommonUtil.getJsonParamString(jsonObject, "deviceUId", null, null, 0, 512);
            }
            if (platform == PlatformEnum.WEB) {
                clientIp = CommonUtil.getJsonParamString(jsonObject, "clientIp", null, null, 0, 512);
            }
            if (loginType == LoginTypeEnum.NAMEPASSWORD) {
                username = CommonUtil.getJsonParamString(jsonObject, "username", null, "01130003", 0, 512);
                psword = CommonUtil.getJsonParamString(jsonObject, "psword", null, "01130004", 0, 512);
            } else if (loginType == LoginTypeEnum.IDPASSWORD) {
                userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "01130005", 0, Integer.MAX_VALUE);
                try {
                    // luckId to userId
                    Integer t_userId = UserAssetServices.luckyIdToUserId(userId);
                    if (t_userId != null && t_userId > 0) {
                        userId = t_userId.intValue();
                    }
                } catch (Exception e) {
                    logger.error("UserAssetServices.luckyIdToUserId(" + userId + ") execute exception.", e);
                }
                psword = CommonUtil.getJsonParamString(jsonObject, "psword", null, "01130004", 0, 512);
                if (StringUtil.strIsNum(psword)) {
                    // 密码不能纯数字
                    result.addProperty("TagCode", "01130014");
                    return result;
                }
            } else if (loginType == LoginTypeEnum.PHONE) {
                phoneNum = CommonUtil.getJsonParamString(jsonObject, "phoneNum", null, "01130011", 0, 256);
                psword = CommonUtil.getJsonParamString(jsonObject, "psword", null, "01130004", 0, 512);
            } else {
                uuid = CommonUtil.getJsonParamString(jsonObject, "uuid", null, "01130008", 0, 512);
                if (loginType == LoginTypeEnum.WEIXIN || loginType == LoginTypeEnum.FACEBOOK) {
                    unionid = CommonUtil.getJsonParamString(jsonObject, "unionid", null, null, 0, 512);
                }
                if (loginType == LoginTypeEnum.QQ) {
                    sessionId = CommonUtil.getJsonParamString(jsonObject, "sessionId", null, null, 0, 512);
                    if (sessionId != null) {
                        QQService qqService = (QQService) MelotBeanFactory.getBean("qqService");
                        unionid = qqService.getUnionID(sessionId);
                    }
                }
                if (loginType == LoginTypeEnum.DIDA) {
                    //70091 固定channelid
                    uuid = uuid.replaceFirst(String.valueOf(70091), "");
                }
            }
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        String ipAddr = com.melot.kktv.service.GeneralService.getIpAddr(request, appId, platform, clientIp);
        int port = com.melot.kktv.service.GeneralService.getPort(request, appId, platform, 0);
        try {
            // 如果clientIp为null，取realIp
            if (clientIp == null && ipAddr.indexOf(",") != -1) {
                clientIp = ipAddr.substring(0, ipAddr.indexOf(","));
            }
        } catch (Exception e) {
            logger.error("get clientIp error, userId:" + userId, e);
        }

        com.melot.kkcore.account.service.AccountService accountService = (com.melot.kkcore.account.service.AccountService) MelotBeanFactory.getBean("kkAccountService");
        
        if (loginType == LoginTypeEnum.FACEBOOK && unionid != null) {
            String[] uuidArr = unionid.split(",");
            for (String uuidStr : uuidArr) {
                if (accountService.isUuidValid(uuidStr, loginType) > 0) {
                    uuid = uuidStr;
                    break;
                }
            }
        }
        // 根据登录类型初始化调用参数
        ResLogin resLogin = null;
        Map<String,Object> extendData = new HashMap<String, Object>();
        extendData.put(ExtendDataKeys.INVITERID.key(), inviterId);
        extendData.put(ExtendDataKeys.ROOMID.key(), roomFrom);
        
        psword = com.melot.kkcx.service.UserService.getMD5Password(psword);
        if (loginType == LoginTypeEnum.NAMEPASSWORD) {
            resLogin = accountService.loginViaUsernamePasswordNew(username, psword, platform, deviceUId, ipAddr, appId, channel, port, extendData);
        } else if (loginType == LoginTypeEnum.IDPASSWORD) {
            resLogin = accountService.loginViaUserIdPasswordNew(userId, psword, platform, deviceUId, ipAddr, appId, channel, port, extendData);
        } else if (loginType == LoginTypeEnum.PHONE) {
            resLogin = accountService.loginViaPhoneNumPasswordNew(phoneNum, psword, platform, deviceUId, ipAddr, appId, channel, port, extendData);
        } else {
            Transaction t;
            t = Cat.getProducer().newTransaction("MCall", "accountService.loginViaOpenPlatformNew");
            try {
                resLogin = accountService.loginViaOpenPlatformNew(loginType, uuid, unionid, platform, deviceUId, ipAddr, appId, channel, port, extendData);
                t.setStatus(Transaction.SUCCESS);
            } catch (Exception e) {
                Cat.getProducer().logError(e);
                t.setStatus(e);
            } finally {
                t.complete();
            }
        }
        
        Integer defPwd = null;
        // loginType != -1 时,调用存储过程得到userId,token
        if (loginType != -1) {
            if (resLogin != null && resLogin.getTagCode() != null) {
                String TagCode = resLogin.getTagCode();
                if (TagCode.equals(TagCodeEnum.SUCCESS)) {
                    userId = resLogin.getUserId();
                    token = resLogin.getToken();
                    // 非密码登录
                    if (loginType != LoginTypeEnum.IDPASSWORD && loginType != LoginTypeEnum.PHONE && loginType != LoginTypeEnum.NAMEPASSWORD
                            && resLogin.getPassword() != null) {
                        psword = resLogin.getPassword();
                    }
                    // ID密码登录或PhoneNum密码登录返回登录名userName
                    if (resLogin.getUsername() != null) {
                        username = resLogin.getUsername();
                    }
                    // 返回是否已修改初始密码
                    if (resLogin.getDefPwd() != null) {
                        defPwd = resLogin.getDefPwd();
                    }
                    
                    //麻辣用户认证手机号
                    if (loginType == LoginTypeEnum.MALA) {
                        phoneNum = CommonUtil.getJsonParamString(jsonObject, "phoneNum", null, null, 0, 256);
                        if (!StringUtil.strIsNull(phoneNum)) {
                            ProfileServices.identifyPhone(userId, phoneNum);
                        }
                    }
                    
                    // 注册日志
                    HadoopLogger.loginLog(userId, new Date(), platform, ipAddr, loginType, appId, SecurityFunctions.decodeED(jsonObject));
                    
                } else if (TagCode.equals("02") || TagCode.equals("07")) {
                    // '02'; 无对应用户记录,该用户未注册
                    result.addProperty("TagCode", "01070103");
                    return result;
                } else if (TagCode.equals("04") || TagCode.equals("05") || TagCode.equals("06")) {
                    // '04'; /*用户黑名单*/
                    // '05'; /*设备黑名单*/
                    // '06'; /*IP黑名单*/
                    result.addProperty("TagCode", "011301" + TagCode);
                    return result;
                } else if (TagCode.equals("08")) {
                    //封号
                    result.addProperty("TagCode", "011301" + TagCode);
                    return result;
                } else if (TagCode.equals("11")) {
                    // 手机号黑名单
                    result.addProperty("TagCode", "01130110");
                    return result;
                } else {
                    // 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
                    logger.error("调用存储过程(com.melot.kkcx.functions.UserFunctions.login_new(" + jsonObject + "))未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
                    result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
                    return result;
                }
            } else {
                result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
                return result;
            }
            
        }

        JsonObject params = new JsonObject();
        params.addProperty("userId", userId);
        params.addProperty("a", appId);
        params.addProperty("c", channel);
        params.addProperty("platform", platform);
        params.addProperty("fromLogin", 1);
        // 根据用户ID返回用户信息
        ProfileFunctions profileFunctions = MelotBeanFactory.getBean("profileFunction", ProfileFunctions.class);
        JsonObject userInfo = profileFunctions.getUserInfo(params, true, request);
        if (!userInfo.get("TagCode").getAsString().endsWith(TagCodeEnum.SUCCESS)) {
            // 获取用户信息失败.直接返回
            return userInfo;
        }

        Map<String, Integer> cityMap = null;// 记录实时的area信息
        
        //根据前端的GPS定位的cityId获取地址信息
        if (gpsCityId > 0 && GeneralService.isValidCity(gpsCityId)) {
        	cityMap = GeneralService.getCityByCityId(gpsCityId);
		}
        
		//前端没有通过GPS拿数据，根据IP地址，获取实时地址信息		
        if (cityMap == null) {
			if (ipAddr != null) {
                String cityIp = ipAddr;
                if (cityIp.indexOf(",") > 0) {
                    cityIp = cityIp.substring(cityIp.indexOf(",") + 1, cityIp.length());
                }
                cityMap = GeneralService.getIpCity(cityIp);
			}
		}
        
        //根据cityMap中的数据更新热点area信息
        try {
            if (cityMap != null && cityMap.get("area") != null && cityMap.get("city") != null) {
                Integer city = null;
                try {
                    KkUserService userService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
                    UserRegistry userRegistry = userService.getUserRegistry(userId);
                    city = userRegistry.getCityId();
                    
                    //数据库可能记录的是负值，转成绝对值后进行下一步判断
                    if (city != null){
                    	city = Math.abs(city);
                    }
                } catch (Exception e) {
                    logger.error("fail to get User Area, userId: " + userId, e);
                }
                
//                if (hotData.size() > 0 && hotData.containsKey("area") && !Integer.valueOf(hotData.get("area")).equals(citMap.get("area"))) {
                if (city != null 
                        && !city.equals(cityMap.get("city")) 
                        && !(cityMap.get("area").equals(CityUtil.getParentCityId(city)) 
                                && ((cityMap.get("area") >= 1 && cityMap.get("area") <= 3) 
                                        || cityMap.get("area").equals(30)))) {
                    //SVIP发送安全提醒
                    VipService vipService = (VipService) MelotBeanFactory.getBean("vipService");
                    if (vipService != null) {
                        List<Integer> list = vipService.getUserProp(userId);
                        if (list != null && list.size() > 0 && list.contains(100004)) {
                        	UserProfile userProfile = UserService.getUserInfoV2(userId);
                        	String phone;
                            if (userProfile != null && (phone = userProfile.getPhoneNum()) != null) {
                                SmsConfig smsConfig = GeneralService.getSmsMsgFormat(1, 100, platform, 16);
                                if (smsConfig != null) {
                                    int dailyCount = smsConfig.getDailyCount().intValue();
                                    // 单日短信发送个数限制
                                    int todayCount = SmsSource.getSendSmsCount(String.valueOf(16), phone);
                                    if (todayCount < dailyCount) {
                                        String nickName = userProfile.getNickName();
                                        String dateString = DateUtil.formatDateTime(new Date(), null);
                                        String format = smsConfig.getMessage();
                                        String message = String.format(format, nickName, userId, dateString, CityUtil.getCityName(cityMap.get("city")));
//                                        SmsSource.sendSms(phone, String.valueOf(16), message, 1);
                                        
                                        SmsService smsService = MelotBeanFactory.getBean("smsService", SmsService.class);
                                        smsService.sendSms(1,16,phone,message,userId,platform,100);
                                        
                                    }
                                } else {
                                    logger.error("Mongodb中未找到短信配置信息! smsType:" + 16 + ", userId:" + userId + ", phoneNum:" + phone);
                                }
                            }
                        }
                    } else {
                        logger.error("fail to call VipService.getUserProp, userId : " + userId);
                    }
                }
                
                // 更新主播的区域信息
                if (userInfo.has("actorTag") && userInfo.get("actorTag") != null
                        && userInfo.get("actorTag").getAsInt() == 1) {
                    
                	// 获取主播所在城市(PG)
                    RoomInfo ri = com.melot.kktv.service.RoomService.getRoomInfo(userId);
                    
                    // 如果主播的城市信息为负，表示主播主动修改的，不根据ip改变
                    if (ri != null 
                    		&& (ri.getRegisterCity() == null || ri.getRegisterCity() > 0 
                    				|| (ri.getRegisterCity() <= 0 && !GeneralService.isValidCity(ri.getRegisterCity())))) {
                        Map<String, Object> actorMap = new HashMap<>();
                        actorMap.put(ActorInfoKeys.REGISTERCITY.key(), cityMap.get("city"));
                        actorService.updateActorInfoById(userId, actorMap);
                        //更新oracleUserInfo(city)
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("cityId", cityMap.get("city"));
                        UserService.updateUserInfoV2(userId, map);
                    }
                    
                    //用实际地址覆盖数据库中地址信息
                    userInfo.addProperty("city", cityMap.get("city"));                    
                }
                //更新普通用户的区域信息
                else {
                	//获取用户信息
                	Transaction t;
                	UserInfoDetail userInfoDetail = null;
                	t = Cat.getProducer().newTransaction("MCall", "com.melot.kkcore.user.service.KkUserService.getUserDetailInfo");
        			try {
        				KkUserService kkUserService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
        				userInfoDetail = kkUserService.getUserDetailInfo(userId);
        				t.setStatus(Transaction.SUCCESS);
        			} catch (Exception e) {
        				Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
        				t.setStatus(e);
        			} finally {
        				t.complete();
        			}
        			
        			// user_info的city>0 动态更新用户所在地；city<0则表示用户手动修改过所属地址，则不再动态修改
        			if (userInfoDetail != null 
        					&& userInfoDetail.getRegisterInfo() != null 
        					&& (userInfoDetail.getRegisterInfo().getCityId() > 0 
        							|| (userInfoDetail.getRegisterInfo().getCityId() <= 0 
        								&& !GeneralService.isValidCity(userInfoDetail.getRegisterInfo().getCityId())))) {
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("cityId", cityMap.get("city"));
                        UserService.updateUserInfoV2(userId, map);
                        
                        //用实际地址覆盖数据库中地址信息
                        userInfo.addProperty("city", cityMap.get("city"));
					}
				}
            }
        } catch (Exception e) {
            logger.error("update actor city error, actorId: " + userId, e);
        }
        
        // 更新redis中用户token信息
        Map<String, String> hotData = new HashMap<String, String>();
        hotData.put("token", token);
        if (isSafe != null) {
            hotData.put("isSafe", isSafe);
        }
        if (ipAddr != null) {
            hotData.put("loginIp", ipAddr);
        }
        HotDataSource.setHotData(String.valueOf(userId), hotData, ConfigHelper.getRedisUserDataExpireTime());
        
        //更新公库中的用户token信息 TODO 这个错误需要移动到Account Module
        try {
            UserService.updateUserToken(userId, appId, token, isSafe);
        } catch (Exception e) {
            logger.error("UserService.updateUserToken(" + userId + ", " + appId + ", " + token + ", " + isSafe + ") execute exception.", e);
        }
        
        // 补全信息
        userInfo.addProperty("userId", userId);
        userInfo.addProperty("token", token);
        // 是否使用初始密码
        if (defPwd != null)
            userInfo.addProperty("defPwd", defPwd);
        if (username != null)
            userInfo.addProperty("username", username);

        // 是否已设置密码
        if (psword != null) {
            userInfo.addProperty("noPwd", false);
        } else {
            userInfo.addProperty("noPwd", true);
        }
        
        // 隐藏的调用获取用户关注列用户ID列表
        JsonObject getUserFollowedIdsResult = new JsonObject();
        try {
            String followIdsStr = UserRelationService.getFollowIdsString(userId, 0, 500);
            if (followIdsStr != null) {
                getUserFollowedIdsResult.addProperty("followedIds", followIdsStr);
                getUserFollowedIdsResult.addProperty("TagCode", TagCodeEnum.SUCCESS);
            }
        } catch (Exception e) {
            logger.error("UserRelationService.getFollowIdsString(" + userId + ", 0, -1) execute exception.", e);
        }
        userInfo.add("getUserFollowedIdsResult", getUserFollowedIdsResult);
        
        try {
            String[] sRI = HotDataSource.getHotFieldValues(String.valueOf(userId), new String[] { "RICount", "RIGiftTotal" });
            if (sRI != null && sRI.length == 2 && sRI[0] != null && sRI[1] != null) {
                userInfo.addProperty("RICount", sRI[0]);
                userInfo.addProperty("RIGiftTotal", sRI[1]);
                // remove the field in hotdata
                HotDataSource.delHotData(String.valueOf(userId), new String[] { "RICount", "RIGiftTotal" });
            }
        } catch (Exception e) {
            logger.error("Refresh RICount and RIGiftTotal execute exception.", e);
        }

        if (appId == 8) {
            // 直播精灵登录返回腾讯云sig
            try {
                String sig = com.melot.kktv.util.TlsSig.sig_gen_3rd(userId);
                if (sig != null) {
                    userInfo.addProperty("sig", sig);
                }
            } catch (Exception e) {
                logger.error("com.melot.kktv.util.TlsSig.sig_gen_3rd(" + userId + ") execute exception.", e);
            }
        }
		
		// 返回结果
		return userInfo;
	}

	/**
	 * 根据userId获取用户token(仅供Node使用,密码secretKey)(10001026)
	 * 
	 * @param jsonObject
	 * @return
	 */
	public JsonObject getUserToken(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
	    
		// 获取参数
		Integer userId = 0;
		String secretKey = null;
		JsonElement userIdje = jsonObject.get("userId");
		JsonElement secretKeyje = jsonObject.get("secretKey");
		// 验证参数
		if (userIdje != null && !userIdje.isJsonNull() && !userIdje.getAsString().isEmpty()) {
			try {
				userId = userIdje.getAsInt();
			} catch (Exception e) {
				result.addProperty("TagCode", "01260002");
				return result;
			}
		} else {
			result.addProperty("TagCode", "01260001");
			return result;
		}
		if (secretKeyje != null && !secretKeyje.isJsonNull() && !secretKeyje.getAsString().isEmpty()) {
			try {
				secretKey = secretKeyje.getAsString();
				if (!secretKey.equals(Constant.secret_key_of_access_token)) {
					result.addProperty("TagCode", "01260005");
					return result;
				}
			} catch (Exception e) {
				result.addProperty("TagCode", "01260004");
				return result;
			}
		} else {
			result.addProperty("TagCode", "01260003");
			return result;
		}
		String token = HotDataSource.getHotFieldValue(String.valueOf(userId), "token");
		if (token != null) {
			result.addProperty("token", token);
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		} else {
			result.addProperty("TagCode", "01260006");
		}
		return result;
	}
	
	/**
	 * 根据userId、token验证用户(For Node)(10001030)
	 * 
	 * @param jsonObject
	 * @return
	 */
	public JsonObject checkUserToken(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		
		JsonObject result = new JsonObject();
		
		if (checkTag) {
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		} else {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
		}
		
		return result;
	}
	
	/**
	 * 取回密码(10001029)
	 * 
	 * @created 2103-11-11 by RC
	 * @param jsonObject
	 * @return
	 */
	public JsonObject retrievePassword(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
	    JsonObject result = new JsonObject();
	    
	    String phoneNum = null, verifyCode = null, isSafe = null;
		int userId = 0, platform, versionCode;
		int gpsCityId = 0;
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 0, Integer.MAX_VALUE);
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, "01290007", 0, Integer.MAX_VALUE);
			versionCode = CommonUtil.getJsonParamInt(jsonObject, "v", 0, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
			//新版本验证手机号兼容
			if ((platform == PlatformEnum.ANDROID && versionCode >= 122) || (platform == PlatformEnum.IPHONE && versionCode >= 174)) {
			    phoneNum = CommonUtil.getJsonParamString(jsonObject, "phoneNum", null, "01290003", 0, 30);
			    phoneNum = CommonUtil.validatePhoneNum(phoneNum, "86");
			    UserProfile userProfile = UserService.getUserInfoV2(userId);
			    if (userProfile == null || !phoneNum.equals(userProfile.getIdentifyPhone())) {
			        result.addProperty("TagCode", "01290002");
                    return result;
			    }
			} else {
			    if (userId != 0) {
	                // 校验userId绑定手机号
	                phoneNum = UserService.getPhoneNumberOfUser(userId);
	                if (phoneNum == null) {
	                    result.addProperty("TagCode", "01290002");
	                    return result;
	                }
	            } else {
	                phoneNum = CommonUtil.getJsonParamString(jsonObject, "phoneNum", null, "01290003", 0, 30);
	                phoneNum = CommonUtil.validatePhoneNum(phoneNum, "86");
	                int s_userId = UserService.getUserOfPhoneNumber(phoneNum);
	                if (s_userId == 0) {
	                    result.addProperty("TagCode", "01290010");
	                    return result;
	                }
	                userId = s_userId;
	            }
			}
			if (platform != PlatformEnum.WEB && platform != PlatformEnum.ANDROID && platform != PlatformEnum.IPHONE && platform != PlatformEnum.IPAD) {
				result.addProperty("TagCode", "01290008");
				return result;
			}
			verifyCode = CommonUtil.getJsonParamString(jsonObject, "verifyCode", null, "01290005", 0, 20);
			isSafe = CommonUtil.getJsonParamString(jsonObject, "isSafe", null, null, 0, 2);
			gpsCityId = CommonUtil.getJsonParamInt(jsonObject, "city", 0, null, 0, Integer.MAX_VALUE); 
		} catch(CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch(Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		
        try {
            BlacklistService blacklistService = (BlacklistService) MelotBeanFactory.getBean("blacklistService");
            if (blacklistService.isPhoneNumBlacklist(phoneNum)) {
                result.addProperty("TagCode", "01290104");
                return result;
            }
        } catch (Exception e) {
            logger.error("fail to BlacklistService, phone: " + phoneNum, e);
        }		
        
		// 校验验证码
		String data = SmsSource.getPhoneSmsData(phoneNum, String.valueOf(SmsTypEnum.APPLY_RETRIEVE_PASSWORD));
		if (data != null && data.equals(verifyCode)) {
			// 生成并返回重置的初始密码
			Map<Object, Object> map = new HashMap<Object, Object>();
			map.put("userId", userId);
			map.put("platform", platform);
			map.put("ip", com.melot.kktv.service.GeneralService.getIpAddr(request, AppIdEnum.AMUSEMENT, platform, null));
			try {
			    SmsSource.delPhoneSmsData(phoneNum, String.valueOf(SmsTypEnum.APPLY_RETRIEVE_PASSWORD));
			    KkUserService kkUserService = MelotBeanFactory.getBean("kkUserService", KkUserService.class);
			    UserRegistry userRegistry = kkUserService.getUserRegistryOutQuest(userId, null);
			    String password = userRegistry.getLoginPwd();
			    if (StringUtil.strIsNull(password)) {
			    	ResResetPassword resetPassword = AccountService.resetPassword(userId, null, platform, com.melot.kktv.service.GeneralService.getIpAddr(request, AppIdEnum.AMUSEMENT, platform, null), 1);
			    	String TagCode = resetPassword.getTagCode();
			        if (TagCode.equals(TagCodeEnum.SUCCESS)) {
			        	password = resetPassword.getPassword();
			        } else if (TagCode.equals("02")) {
			            // 未找到该手机号用户
			            result.addProperty("TagCode", "012901" + TagCode);
			            return result;
			        } else {
			            // 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
			            logger.error("调用用户中心(AccountService.resetPassword(" + userId + ": userId" + "))未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
			            result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
			            return result;
			        }
                }
			    
				// userId password 加密返回
				try {
				    JsonObject udpdJson = new JsonObject();
				    udpdJson.addProperty("userId", userId);
				    udpdJson.addProperty("password", password);
				    String udpd = SecurityFunctions.encryptUDPD(udpdJson);
				    result.addProperty("udpd", udpd);
				} catch (Exception e) {
				    logger.error("Encrypt userId(" + userId + ") and password(" + password + ") error.", e);
				}
				
				// 隐藏的登陆操作,调用login_new得到结果
				try {
				    JsonObject params = new JsonObject();
				    params.addProperty("loginType", LoginTypeEnum.IDPASSWORD);
				    params.addProperty("userId", userId);
				    params.addProperty("phoneNum", phoneNum);
				    params.addProperty("psword", password);
				    params.addProperty("platform", platform);
				    params.addProperty("isSafe", isSafe);
				    params.addProperty("city", gpsCityId);
				    JsonObject loginResult;
				    loginResult = login_new(params, true, request);
				    result.add("loginResult", loginResult);
				} catch (Exception e) {
				    logger.error("UserId(" + userId + ") and password(" + password + ") login error.", e);
				}
				
				result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			} catch (Exception e) {
				logger.error("未能正常调用存储过程", e);
				result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			}
		} else {
			// 未找到验证码或验证码已失效
			result.addProperty("TagCode", "01290009");
		}
		
		return result;
	}

	/**
	 * 手机注册(10001031)
	 * 
	 * @created 2103-12-21 by RC
	 * @param jsonObject
	 * @return
	 */
	public JsonObject registerViaPhoneNum(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
	    JsonObject result = new JsonObject();
	    
		// 获取参数
	    Integer userId;
		int openPlatform, appId, channelId, referrerId, platform, inviterId, roomFrom, refRoomId;
		String phoneNum, verifyCode, deviceUId, isSafe, clientIp = null, deviceName, deviceModel;
		int gpsCityId = 0;//客户端定位拿到的城市ID
		try {
			openPlatform = CommonUtil.getJsonParamInt(jsonObject, "openPlatform", LoginTypeEnum.PHONE, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
			verifyCode = CommonUtil.getJsonParamString(jsonObject, "verifyCode", null, "01310004", 1, Integer.MAX_VALUE);
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, "01310006", 1, Integer.MAX_VALUE);
			if (platform == PlatformEnum.WEB) {
			    clientIp = CommonUtil.getJsonParamString(jsonObject, "clientIp", CommonUtil.getIpAddr(request), null, 0, Integer.MAX_VALUE);
			}
			deviceUId = CommonUtil.getJsonParamString(jsonObject, "deviceUId", null, null, 1, Integer.MAX_VALUE);
			referrerId = CommonUtil.getJsonParamInt(jsonObject, "referrerId", 0, null, 1, Integer.MAX_VALUE);
			channelId = CommonUtil.getJsonParamInt(jsonObject, "channel", 0, null, 1, Integer.MAX_VALUE);
			isSafe = CommonUtil.getJsonParamString(jsonObject, "isSafe", null, null, 1, Integer.MAX_VALUE);
			appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 1, Integer.MAX_VALUE);
			phoneNum = CommonUtil.getJsonParamString(jsonObject, "phoneNum", null, "01310002", 1, Integer.MAX_VALUE);
			inviterId = CommonUtil.getJsonParamInt(jsonObject, "inviterId", 0, null, 0, Integer.MAX_VALUE);
			roomFrom = CommonUtil.getJsonParamInt(jsonObject, "roomFrom", 0, null, 0, Integer.MAX_VALUE);
			refRoomId = CommonUtil.getJsonParamInt(jsonObject, "refRoomId", 0, null, 0, Integer.MAX_VALUE);
			gpsCityId = CommonUtil.getJsonParamInt(jsonObject, "city", 0, null, 0, Integer.MAX_VALUE);

			if (phoneNum != null) {
				phoneNum = CommonUtil.validatePhoneNum(phoneNum, "86");
			} else {
				result.addProperty("TagCode", "01310003");
				return result;
			}
			deviceName = CommonUtil.getJsonParamString(jsonObject, "deviceName", "", null, 1, 512);
            deviceModel = CommonUtil.getJsonParamString(jsonObject, "deviceModel", "", null, 1, 512);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
		
		try {
            BlacklistService blacklistService = (BlacklistService) MelotBeanFactory.getBean("blacklistService");
            if (blacklistService.isPhoneNumBlacklist(phoneNum)) {
                result.addProperty("TagCode", "01310108");
                return result;
            }
        } catch (Exception e) {
            logger.error("fail to BlacklistService.isPhoneNumBlacklist, phone: " + phoneNum, e);
        }
		
		String ipAddr = com.melot.kktv.service.GeneralService.getIpAddr(request, appId, platform, clientIp);
		int port = com.melot.kktv.service.GeneralService.getPort(request, appId, platform, 0);
		
		// 校验验证码
		String data = SmsSource.getPhoneSmsData(phoneNum, String.valueOf(SmsTypEnum.PHONENUM_REGISTER));
		if (data != null && data.equals(verifyCode)) {
            String password = null;
            
		    // 手机号已注册过直接登录
		    int oldUserId = UserService.getUserOfPhoneNumber(phoneNum);
		    if (oldUserId > 0) {
		        userId = oldUserId;
                result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
                
		        KkUserService kkUserService = MelotBeanFactory.getBean("kkUserService", KkUserService.class);
		        password = kkUserService.getUserRegistryOutQuest(userId, null).getLoginPwd();
		        try {
                    JsonObject params = new JsonObject();
                    params.addProperty("loginType", LoginTypeEnum.PHONE);
                    params.addProperty("userId", userId);
                    params.addProperty("phoneNum", phoneNum);
                    params.addProperty("psword", password);
                    params.addProperty("platform", platform);
                    params.addProperty("deviceUId", deviceUId);
                    params.addProperty("isSafe", isSafe);
                    params.addProperty("city", gpsCityId);
                    params.addProperty("a", appId);
                    JsonObject loginResult = login_new(params, true, request);
                    result.add("loginResult", loginResult);
                    
                    result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                    
                    try {
                        if (loginResult != null && loginResult.has("TagCode")) {
                            String loginTagCode = loginResult.get("TagCode").getAsString();
                            if (!StringUtil.strIsNull(loginTagCode) && !TagCodeEnum.SUCCESS.equals(loginTagCode)) {
                                result.addProperty("TagCode", loginTagCode);
                                return result;
                            } else {
                                //添加常用设备
                                ProfileServices.setUserCommonDevice(userId, deviceUId, deviceName, deviceModel);
                            }
                        }
                    } catch (Exception e) {
                    }
                    
                    if (!StringUtil.strIsNull(password)) {
                        try {
                            JsonObject udpdJson = new JsonObject();
                            udpdJson.addProperty("userId", userId);
                            udpdJson.addProperty("password", password);
                            String udpd = SecurityFunctions.encryptUDPD(udpdJson);
                            result.addProperty("udpd", udpd);
                        } catch (Exception e) {
                            logger.error("Encrypt userId(" + userId + ") and password(" + password + ") error.", e);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Auto login error, jsonObject:" + jsonObject, e);
                }
		        return result;
            }
		    
			// 注册用户 手机注册类型用户 绑定手机号
		    Map<String, Object> registerMap = new HashMap<String, Object>();
		    registerMap.put(ExtendDataKeys.INVITERID.key(), inviterId);
		    registerMap.put(ExtendDataKeys.ROOMID.key(), roomFrom);
		    registerMap.put("refRoomId", refRoomId);
			ResRegister resRegister = AccountService.registerViaPhoneNum(phoneNum, userId,
					platform, referrerId, channelId, deviceUId, ipAddr, appId, port, registerMap);
			if (resRegister != null && resRegister.getTagCode() != null) {
				String TagCode = resRegister.getTagCode();
				if (TagCode == null) {
					result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
					return result;
				}
				if (TagCode.equals(TagCodeEnum.SUCCESS)) {
					if (resRegister.getUserId() != null) {
						userId = (Integer) resRegister.getUserId();
						Long showMoney = 0l;
						if (resRegister.getShowMoney() != null) {
							showMoney = (Long) resRegister.getShowMoney();
						}
						registerUserAssets(userId, showMoney);
						if (resRegister.getPassword() != null) {
							password = (String) resRegister.getPassword();
						}
						// userId password 加密返回
						try {
							JsonObject udpdJson = new JsonObject();
							udpdJson.addProperty("userId", userId);
							udpdJson.addProperty("password", password);
							String udpd = SecurityFunctions.encryptUDPD(udpdJson);
							result.addProperty("udpd", udpd);
						} catch (Exception e) {
							logger.error("Encrypt userId(" + userId + ") and password(" + password + ") error.", e);
						}
					}
					
					// 隐藏的获取推荐列表的操作
					try {
						IndexFunctions IndexFunction = MelotBeanFactory.getBean("indexFunction", IndexFunctions.class);
						JsonObject getRecommendedListResult = IndexFunction.getFollowRecommendedList(jsonObject, checkTag, request);
						result.add("getRecommendedListResult", getRecommendedListResult);
					} catch (Exception e) {
						logger.error("Get follow recommended list error, jsonObject:" + jsonObject, e);
					}

					// 隐藏的登陆操作,调用login_new得到结果
					try {
						JsonObject params = new JsonObject();
						params.addProperty("loginType", LoginTypeEnum.PHONE);
						params.addProperty("userId", userId);
                        params.addProperty("phoneNum", phoneNum);
						params.addProperty("psword", password);
						params.addProperty("platform", platform);
						params.addProperty("deviceUId", deviceUId);
						params.addProperty("isSafe", isSafe);
						params.addProperty("a", appId);
						params.addProperty("city", gpsCityId);
						JsonObject loginResult = login_new(params, true, request);
						result.add("loginResult", loginResult);
		                
		                try {
		                    if (loginResult != null && loginResult.has("TagCode")) {
		                        String loginTagCode = loginResult.get("TagCode").getAsString();
		                        if (!StringUtil.strIsNull(loginTagCode) && !TagCodeEnum.SUCCESS.equals(loginTagCode)) {
		                            result.addProperty("TagCode", loginTagCode);
		                            return result;
		                        }
		                    }
		                } catch (Exception e) {
		                }
					} catch (Exception e) {
						logger.error("Auto login error, jsonObject:" + jsonObject, e);
					}

					result.addProperty("canInvite", com.melot.kkcx.service.UserService.canInvite(deviceUId));


//					// 发送短信到客户端 短信内容:userId+passoword
//					SmsConfig smsConfig = GeneralService.getSmsMsgFormat(appId, channel, platform, SmsTypEnum.INSTANT_REGISTER);
//					if (smsConfig != null) {
//						String format = smsConfig.getMessage();
//						String message = String.format(format, userId, password);
//						SmsSource.sendSms(phoneNum, String.valueOf(SmsTypEnum.INSTANT_REGISTER), message, appId);
//					} else {
//						logger.error("Mongodb中未找到短信配置信息! smsType:" + SmsTypEnum.INSTANT_REGISTER + ", userId:" + userId + ", password:" + password
//								+ ", phoneNum:" + phoneNum);
//					}

					// 设置用户绑定手机号
					// SmsSource.setBoundPhoneNum(phoneNum, String.valueOf(userId));
					
					// 认证手机号
					try {
					    com.melot.kkcore.user.service.KkUserService userService = (com.melot.kkcore.user.service.KkUserService) MelotBeanFactory.getBean("kkUserService");
					    Map<String, Object> map = new HashMap<String, Object>();
                        map.put(ProfileKeys.IDENTIFY_PHONE.key(), phoneNum);
                        int tagCode = userService.updateUserProfile(userId, map);
                        if (tagCode != 0) {
                            logger.error("call procedure User.identifyPhone catched exception, userId : " + userId + ", phoneNum : " + phoneNum);
                        }
					} catch (Exception e) {
						logger.error("call procedure User.identifyPhone catched exception, userId : " + userId + ", phoneNum : " + phoneNum, e);
					}
					
					/*if (channelId != 0) {
						new ChannelRegReward(userId, channelId).start();
					}*/

					// 用户数据采集-成功注册
					DataAcqService.sendRegister(userId, openPlatform, platform);
					
					// 注册日志
					HadoopLogger.registerLog(userId, new Date(), platform, LoginTypeEnum.PHONE,
							referrerId, ipAddr, channelId, deviceUId, 0, appId, SecurityFunctions.decodeED(jsonObject));
					
					//添加常用设备
					ProfileServices.setUserCommonDevice(userId, deviceUId, deviceName, deviceModel);
					result.addProperty("TagCode", TagCodeEnum.SUCCESS);

				} else if (TagCode.equals("03") || TagCode.equals("02") || TagCode.equals("04") || TagCode.equals("05") || TagCode.equals("06")) {
					// '03': 手机号已被其他用户绑定
					// '02': 用户ID已经激活
					// '04': IP黑名单限制注册
					// '05': 到达单IP,一小时内注册上限
					// '06': 到达单IP,一天内注册上限
					result.addProperty("TagCode", "013101" + TagCode + "");
				} else if (TagCode.equals("11")) {
				    result.addProperty("TagCode", "01310108");
				} else {
					// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
					logger.error("调用存储过程(AccountService.registerViaPhoneNum(" + phoneNum + ", " + userId + ", " + platform + ", " + referrerId + ", " + channelId + ", " + deviceUId + ", " + ipAddr + ", " + appId + ", " + port + "))未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
					result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
				}
			} else {
				result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			}
		} else {
			// 未找到验证码或验证码已失效
			result.addProperty("TagCode", "01310012");
		}
		return result;
	}

	/**
	 * 用户设置密码(10001032)
	 * 
	 * @created 2103-12-24 by RC
	 * @param jsonObject
	 * @param checkTag
	 * @param request
	 * @return
	 */
	public JsonObject setPassword(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
	    JsonObject result = new JsonObject();
	    
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		String psword = null;
		int userId, platform;
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "01320001", 0, Integer.MAX_VALUE);
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, "01320004", 0, Integer.MAX_VALUE);
			if (platform != PlatformEnum.WEB && platform != PlatformEnum.ANDROID && platform != PlatformEnum.IPHONE && platform != PlatformEnum.IPAD) {
				result.addProperty("TagCode", "01320005");
				return result;
			}
			psword = CommonUtil.getJsonParamString(jsonObject, "psword", null, "01320003", 0, 40);
			if (psword.length() != 32 && (psword.length() > 16 || psword.length() < 6 || "a111111".equals(psword.trim()))) {
				result.addProperty("TagCode", "01320003");
				return result;
			}
		} catch(CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch(Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		
		try {
			KkUserService kkUserService = MelotBeanFactory.getBean("kkUserService", KkUserService.class);
			UserStaticInfo staticInfo = kkUserService.getUserStaticInfo(userId);
			if (staticInfo.getProfile().getPhoneNum() == null && 
				staticInfo.getRegisterInfo().getLoginName() == null &&
				psword.matches("[0-9]+")) {
				result.addProperty("TagCode", "01320003");
				return result;
			}
			int level = ProfileServices.getPasswordSafetyRank(psword);
			ResResetPassword resetPassword = AccountService.resetPassword(userId, com.melot.kkcx.service.UserService.getMD5Password(psword), platform, 
					com.melot.kktv.service.GeneralService.getIpAddr(request, AppIdEnum.AMUSEMENT, platform, null), level);
			String TagCode = resetPassword.getTagCode();
			if (TagCode.equals(TagCodeEnum.SUCCESS)) {
				com.melot.kkcx.service.UserService.insertTempUserPassword(userId, psword);
				result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			} else if (TagCode.equals("02")) {
				// 未找到该用户
				result.addProperty("TagCode", "013201" + TagCode);
			} else if (TagCode.equals("03")) {
				// ID登录为唯一登录方式时不允许纯数字
				result.addProperty("TagCode", "01320003");
			} else {
				// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
				logger.error("调用用户中心(AccountService.resetPassword( " + "userId :" + userId  + ")未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
				result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
			}
		} catch (Exception e) {
			logger.error("未能正常调用用户中心", e);
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
		}

		// 返回结果
		return result;
	}
	
	public JsonObject loginViaPasswordNew(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		JsonObject result = new JsonObject();
		
		String deviceUId = null, clientIp = null, username = null, psword = null, phoneNum = null, token = null, isSafe, deviceName, deviceModel;
		int userId = 0, platform, appId, channel, inviterId, fromVerifyLogin, loginType = 0, roomFrom;
		int gpsCityId = 0;// 前端通过GPS获取的地址信息【参数里面是city】
		try {
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, "01130009", 0, Integer.MAX_VALUE);
			if (platform == PlatformEnum.WEB) {
                clientIp = CommonUtil.getJsonParamString(jsonObject, "clientIp", null, null, 0, 512);
            } else {
                deviceUId = CommonUtil.getJsonParamString(jsonObject, "deviceUId", null, null, 0, 512);
            }
			appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 0, Integer.MAX_VALUE);
			channel = CommonUtil.getJsonParamInt(jsonObject, "c", AppChannelEnum.KK, null, 0, Integer.MAX_VALUE);
            inviterId = CommonUtil.getJsonParamInt(jsonObject, "inviterId", 0, null, 0, Integer.MAX_VALUE);
            roomFrom = CommonUtil.getJsonParamInt(jsonObject, "roomFrom", 0, null, 0, Integer.MAX_VALUE);
            isSafe = CommonUtil.getJsonParamString(jsonObject, "isSafe", null, null, 1, Integer.MAX_VALUE);
            
			fromVerifyLogin = CommonUtil.getJsonParamInt(jsonObject, "fromVerifyLogin", 0, null, 0, 2);
			gpsCityId = CommonUtil.getJsonParamInt(jsonObject, "city", 0, null, 0, Integer.MAX_VALUE);
			if (fromVerifyLogin == 1) {
				userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "01130005", 0, Integer.MAX_VALUE);
			}
            
            loginType = CommonUtil.getJsonParamInt(jsonObject, "loginType", 0, "01130001", Integer.MIN_VALUE, Integer.MAX_VALUE);
			if (loginType == LoginTypeEnum.NAMEPASSWORD) {
                username = CommonUtil.getJsonParamString(jsonObject, "username", null, "01130003", 0, 512);
            } else if (loginType == LoginTypeEnum.IDPASSWORD) {
                userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "01130005", 0, Integer.MAX_VALUE);
                try {
                    // luckId to userId
                    Integer t_userId = UserAssetServices.luckyIdToUserId(userId);
                    if (t_userId != null && t_userId > 0) {
                        userId = t_userId.intValue();
                    }
                } catch (Exception e) {
                    logger.error("UserAssetServices.luckyIdToUserId(" + userId + ") execute exception.", e);
                }
                if (StringUtil.strIsNum(psword)) {
                    // 密码不能纯数字
                    result.addProperty("TagCode", "01130014");
                    return result;
                }
            } else if (loginType == LoginTypeEnum.PHONE) {
            	phoneNum = CommonUtil.getJsonParamString(jsonObject, "phoneNum", null, "01130011", 0, 256);
            }
			psword = CommonUtil.getJsonParamString(jsonObject, "psword", null, "01130004", 0, 512);
            deviceName = CommonUtil.getJsonParamString(jsonObject, "deviceName", "", null, 1, 512);
            deviceModel = CommonUtil.getJsonParamString(jsonObject, "deviceModel", "", null, 1, 512);
		} catch(CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch(Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		
		psword = com.melot.kkcx.service.UserService.getMD5Password(psword);
		ResLogin resLogin = null;
		String ipAddr = com.melot.kktv.service.GeneralService.getIpAddr(request, appId, platform, clientIp);
    	int port = com.melot.kktv.service.GeneralService.getPort(request, appId, platform, 0);
    	if (ipAddr.indexOf(",") > 0) {
    		ipAddr = ipAddr.substring(ipAddr.indexOf(",") + 1, ipAddr.length());
        }
    	
    	Map<String, Integer> cityMap = null;// 记录实时区域信息

    	//根据前端的GPS定位的cityId获取地址信息
        if (gpsCityId > 0 && GeneralService.isValidCity(gpsCityId)) {
        	cityMap = GeneralService.getCityByCityId(gpsCityId);
		}
        
        //GPS获取地址信息失败，根据IP地址获取实时区域信息
        if (cityMap == null && ipAddr != null) {
        	cityMap = GeneralService.getIpCity(ipAddr);
		}
		
    	com.melot.kkcore.account.service.AccountService accountService = (com.melot.kkcore.account.service.AccountService) MelotBeanFactory.getBean("kkAccountService");
		try {
			resLogin = accountService.verifyLoginPassword(userId, username, phoneNum, psword, ipAddr, deviceUId);
		} catch (Exception e) {
			logger.error("call accountService.verifyLoginPassword catched exception, loginType : " + loginType + ", userId : " + userId 
					+ ", username : " + username + ", deviceUId : " + deviceUId + ", ipAddr : " + ipAddr + ", phoneNum : " + phoneNum
					+ ", psword : " + psword, e);
		}
		if (checkLogin(result, resLogin, loginType, jsonObject) != null) {
			return result;
		}
		userId = resLogin.getUserId();
		if (fromVerifyLogin == 0 && com.melot.kkcx.service.UserService.getUserSmsSwitchState(userId) 
		        && !ProfileServices.checkUserCommonDevice(userId, deviceUId)) {
			//异常登录判断
			KkUserService kkUserService = MelotBeanFactory.getBean("kkUserService", KkUserService.class);
			UserProfile userProfile = kkUserService.getUserProfile(userId);
			if (userProfile != null && (userProfile.getPhoneNum() != null || userProfile.getIdentifyPhone() != null)) {
				LastLoginInfo lastLoginInfo = kkUserService.getLastLoginInfo(userId);
				IpRepositoryService ipRepositoryService = MelotBeanFactory.getBean("ipRepositoryService", IpRepositoryService.class);
				boolean flag = ipRepositoryService.compareIps(ipAddr, lastLoginInfo.getLastLoginIp());
				
				boolean checkFlag = false;
				if (lastLoginInfo == null|| lastLoginInfo.getLastLoginIp() == null || lastLoginInfo.getLastLoginPlatform() < 1) {
					if (System.currentTimeMillis() > deadTime) {
						checkFlag = true;
					}
				} else {
					if ((lastLoginInfo.getLastLoginPlatform() == 1 || platform == 1)) {
						if (!flag) {
							checkFlag = true;
						}
					} else {
						if (deviceUId == null || !deviceUId.equals(lastLoginInfo.getLastLoginDeviceUId())) {
							checkFlag = true;
						}
					}
				}
				if (checkFlag) {
					result.addProperty("TagCode", "00402101");
					result.addProperty("userId", userId);
					result.addProperty("phoneNum", userProfile.getPhoneNum() != null ? userProfile.getPhoneNum() : userProfile.getIdentifyPhone());
					return result;
				}
			}
		}
			
		Map<String,Object> extendData = new HashMap<String, Object>();
		extendData.put(ExtendDataKeys.INVITERID.key(), inviterId);
		extendData.put(ExtendDataKeys.ROOMID.key(), roomFrom);
		try {
			resLogin = accountService.loginViaWithoutPwd(loginType, userId, platform, deviceUId, ipAddr, appId, channel, port, extendData);
		} catch (Exception e) {
			logger.error("call accountService.loginViaWithoutPwd catched exception, loginType : " + loginType + ", userId : " + userId 
					+ ", platform : " + platform + ", deviceUId : " + deviceUId + ", ipAddr : " + ipAddr + ", appId : " + appId
					+ ", channel : " + channel + ", port : " + port + ", extendData : " + new Gson().toJson(extendData), e);
		}
		if (checkLogin(result, resLogin, loginType, jsonObject) != null) {
			return result;
		}
		
		Integer defPwd = null;
		userId = resLogin.getUserId();
        token = resLogin.getToken();
        // ID密码登录或PhoneNum密码登录返回登录名userName
        if (resLogin.getUsername() != null) {
            username = resLogin.getUsername();
        }
        // 返回是否已修改初始密码
        if (loginType == LoginTypeEnum.PHONE && resLogin.getDefPwd() != null) {
            defPwd = resLogin.getDefPwd();
        }
        
        //短信校验登录添加常用设备
        if (deviceUId != null && fromVerifyLogin == 1) {
            ProfileServices.setUserCommonDevice(userId, deviceUId, deviceName, deviceModel);
        }
        
        // 注册日志
        HadoopLogger.loginLog(userId, new Date(), platform, ipAddr, loginType, appId, SecurityFunctions.decodeED(jsonObject));
        
        JsonObject params = new JsonObject();
        params.addProperty("userId", userId);
        params.addProperty("a", appId);
        params.addProperty("c", channel);
        params.addProperty("platform", platform);
        params.addProperty("fromLogin", 1);
        // 根据用户ID返回用户信息
        ProfileFunctions profileFunctions = MelotBeanFactory.getBean("profileFunction", ProfileFunctions.class);
        JsonObject userInfo = profileFunctions.getUserInfo(params, true, request);
        if (!userInfo.get("TagCode").getAsString().endsWith(TagCodeEnum.SUCCESS)) {
            // 获取用户信息失败.直接返回
            return userInfo;
        }
        
        // 登录时根据ip地址返回所在城市 更新热点area信息
        try {
            if (cityMap != null && cityMap.get("area") != null && cityMap.get("city") != null) {
                // 主播号
                if (userInfo.has("actorTag") && userInfo.get("actorTag") != null
                        && userInfo.get("actorTag").getAsInt() == 1) {
                    
                	// 获取主播所在城市(PG)
                    RoomInfo ri = com.melot.kktv.service.RoomService.getRoomInfo(userId);
                    
                    // 如果主播的城市信息为负，表示主播主动修改的，不根据ip改变
                    if (ri != null && (ri.getRegisterCity() == null 
                    		|| ri.getRegisterCity() > 0 
                    		|| (ri.getRegisterCity() <= 0 
                    				&& !GeneralService.isValidCity(ri.getRegisterCity())))) {
                        Map<String, Object> actorMap = new HashMap<>();
                        actorMap.put(ActorInfoKeys.REGISTERCITY.key(), cityMap.get("city"));
                        actorService.updateActorInfoById(userId, actorMap);
                        
                        //更新oracleUserInfo(city)
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("cityId", cityMap.get("city"));
                        UserService.updateUserInfoV2(userId, map);
                    }
                    
                    // 返回的地址信息为实时地址信息
                    userInfo.addProperty("city", cityMap.get("city"));
                }
                //普通用户
                else {
                	//获取用户信息
                	Transaction t;
                	UserInfoDetail userInfoDetail = null;
                	t = Cat.getProducer().newTransaction("MCall", "com.melot.kkcore.user.service.KkUserService.getUserDetailInfo");
        			try {
        				KkUserService kkUserService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
        				userInfoDetail = kkUserService.getUserDetailInfo(userId);
        				t.setStatus(Transaction.SUCCESS);
        			} catch (Exception e) {
        				Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
        				t.setStatus(e);
        			} finally {
        				t.complete();
        			}
        			
        			// user_info的city>0 动态更新用户所在地【返回实时地址信息】；city<0则表示用户手动修改过所属地址，则不再动态修改【返回数据库中地址信息】
        			if (userInfoDetail != null 
        					&& userInfoDetail.getRegisterInfo() != null 
        					&& (userInfoDetail.getRegisterInfo().getCityId() > 0 
        							||(userInfoDetail.getRegisterInfo().getCityId() <= 0 
        									&& !GeneralService.isValidCity(userInfoDetail.getRegisterInfo().getCityId())))) {
                        Map<String, Object> map = new HashMap<String, Object>();
                        map.put("cityId", cityMap.get("city"));
                        UserService.updateUserInfoV2(userId, map);
                        
                        //用实际地址覆盖数据库中地址信息
                        userInfo.addProperty("city", cityMap.get("city"));
					}
				}
            }
        } catch (Exception e) {
            logger.error("update actor city error, actorId: " + userId, e);
        }
        
        // 更新redis中用户token信息
        Map<String, String> hotData = new HashMap<String, String>();
        hotData.put("token", token);
        if (isSafe != null) {
            hotData.put("isSafe", isSafe);
        }
        if (ipAddr != null) {
            hotData.put("loginIp", ipAddr);
        }
        HotDataSource.setHotData(String.valueOf(userId), hotData, ConfigHelper.getRedisUserDataExpireTime());
        
        try {
            UserService.updateUserToken(userId, appId, token, isSafe);
        } catch (Exception e) {
            logger.error("UserService.updateUserToken(" + userId + ", " + appId + ", " + token + ", " + isSafe + ") execute exception.", e);
        }
        
        // 补全信息
        userInfo.addProperty("userId", userId);
        userInfo.addProperty("token", token);
        // 是否使用初始密码
        if (defPwd != null)
            userInfo.addProperty("defPwd", defPwd);
        if (username != null)
            userInfo.addProperty("username", username);
        
        // 是否已设置密码
        if (psword != null) {
            userInfo.addProperty("noPwd", false);
        } else {
            userInfo.addProperty("noPwd", true);
        }
        
        // 隐藏的调用获取用户关注列用户ID列表
        JsonObject getUserFollowedIdsResult = new JsonObject();
        try {
            String followIdsStr = UserRelationService.getFollowIdsString(userId, 0, 500);
            if (followIdsStr != null) {
                getUserFollowedIdsResult.addProperty("followedIds", followIdsStr);
                getUserFollowedIdsResult.addProperty("TagCode", TagCodeEnum.SUCCESS);
            }
        } catch (Exception e) {
            logger.error("UserRelationService.getFollowIdsString(" + userId + ", 0, -1) execute exception.", e);
        }
        userInfo.add("getUserFollowedIdsResult", getUserFollowedIdsResult);
        
        try {
            String[] sRI = HotDataSource.getHotFieldValues(String.valueOf(userId), new String[] { "RICount", "RIGiftTotal" });
            if (sRI != null && sRI.length == 2 && sRI[0] != null && sRI[1] != null) {
                userInfo.addProperty("RICount", sRI[0]);
                userInfo.addProperty("RIGiftTotal", sRI[1]);
                // remove the field in hotdata
                HotDataSource.delHotData(String.valueOf(userId), new String[] { "RICount", "RIGiftTotal" });
            }
        } catch (Exception e) {
            logger.error("Refresh RICount and RIGiftTotal execute exception.", e);
        }
        
        if (appId == 8) {
            // 直播精灵登录返回腾讯云sig
            try {
                String sig = com.melot.kktv.util.TlsSig.sig_gen_3rd(userId);
                if (sig != null) {
                    userInfo.addProperty("sig", sig);
                }
            } catch (Exception e) {
                logger.error("com.melot.kktv.util.TlsSig.sig_gen_3rd(" + userId + ") execute exception.", e);
            }
        }
		
		// 返回结果
		return userInfo;
	}
	
	/**
     * 获取用户基础信息(51010101)
     * 
     * @param jsonObject 请求对象
     * @param checkTag 是否验证token标记
     * @return 登录结果
     */
    public JsonObject getUserBasicInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        // 获取参数
        // 定义结果并组装json对象形式的返回结果
        JsonObject result = new JsonObject();

        int userId = 0, platform = 0, appId = 0, channel = 0, b = 0, fromLogin = 0;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "05010001", 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, PlatformEnum.WEB, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 0, Integer.MAX_VALUE);
            channel = CommonUtil.getJsonParamInt(jsonObject, "c", AppChannelEnum.KK, null, 0, Integer.MAX_VALUE);
            b = CommonUtil.getJsonParamInt(jsonObject, "b", 0, null, 0, Integer.MAX_VALUE);
            fromLogin = CommonUtil.getJsonParamInt(jsonObject, "fromLogin", 0, null, 0, 1);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        Transaction t;

        // 获取公有属性
        UserInfoDetail userInfoDetail = null;
        t = Cat.getProducer().newTransaction("MCall", "com.melot.kkcore.user.service.KkUserService.getUserDetailInfo");
        try {
            KkUserService kkUserService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
            userInfoDetail = kkUserService.getUserDetailInfo(userId);
            t.setStatus(Transaction.SUCCESS);
        } catch (Exception e) {
            Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
            t.setStatus(e);
        } finally {
            t.complete();
        }
        if (userInfoDetail == null || userInfoDetail.getRegisterInfo() == null) {
            JsonObject reResult = new JsonObject();
            reResult.addProperty("TagCode", TagCodeEnum.USER_NOT_EXIST);
            return reResult;
        }
        result.addProperty("gender", userInfoDetail.getProfile().getGender());
        result.addProperty("cityId", Math.abs(userInfoDetail.getRegisterInfo().getCityId()));

        Integer area = CityUtil.getParentCityIdNoDefault(userInfoDetail.getRegisterInfo().getCityId());
        if (area != null) {
            result.addProperty("area", area);
        }
        if (userInfoDetail.getProfile().getNickName() != null) {
            t = Cat.getProducer().newTransaction("MCall", "GeneralService.replaceSensitiveWords");
            try {
                result.addProperty("nickname", GeneralService.replaceSensitiveWords(userId, userInfoDetail.getProfile().getNickName()));
                t.setStatus(Transaction.SUCCESS);
            } catch (Exception e) {
                Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                t.setStatus(e);
            } finally {
                t.complete();
            }
        }
        if (userInfoDetail.getProfile().getBirthday() != null) {
            result.addProperty("birthday", userInfoDetail.getProfile().getBirthday());
        }

        try {
            long consumeTotal = userInfoDetail.getAssets() == null ? 0 : userInfoDetail.getAssets().getConsumeTotal();
            long earnTotal = userInfoDetail.getAssets() == null ? 0 : userInfoDetail.getAssets().getEarnTotal();

            // 读取明星等级
            ActorLevel actorLevel = null;
            t = Cat.getProducer().newTransaction("MCall", "UserService.getActorLevel");
            try {
                actorLevel = com.melot.kkcx.service.UserService.getActorLevel(earnTotal);
                t.setStatus(Transaction.SUCCESS);
            } catch (Exception e) {
                Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                t.setStatus(e);
            } finally {
                t.complete();
            }
            if (actorLevel != null) {
                result.addProperty("actorLevel", actorLevel.getLevel());
            }

            // 读取富豪等级
            RichLevel richLevel = null;
            t = Cat.getProducer().newTransaction("MCall", "UserService.getRichLevel");
            try {
                richLevel = com.melot.kkcx.service.UserService.getRichLevel(consumeTotal);
                t.setStatus(Transaction.SUCCESS);
            } catch (Exception e) {
                Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                t.setStatus(e);
            } finally {
                t.complete();
            }
            if (richLevel != null) {
                result.addProperty("richLevel", richLevel.getLevel());
            }

        } catch (Exception e) {
            logger.error("UserService.getUserInfoFromMongo(" + userId + ") execute exception.", e);
        }

        result.addProperty("pathPrefix", ConfigHelper.getHttpdir());

        // 获取用户会员信息
        JsonArray propArray = new JsonArray();
        try {
            List<Integer> propList = null;
            t = Cat.getProducer().newTransaction("MCall", "UserService.getUserProps");
            try {
                propList = com.melot.kkcx.service.UserService.getUserProps(userId);
                t.setStatus(Transaction.SUCCESS);
            } catch (Exception e) {
                Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                t.setStatus(e);
            } finally {
                t.complete();
            }
            if (propList != null) {
                for (Integer propId : propList) {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("propId", propId);
                    propArray.add(obj);
                }
            }
        } catch (Exception e) {
            logger.error("UserService.getUserProps(" + userId + ") execute exception.", e);
        }
        result.add("props", propArray);

        if (userInfoDetail.getProfile().getPortrait() != null && !result.has("portrait_path_original")) {
            result.addProperty("portrait_path_128", userInfoDetail.getProfile().getPortrait() + "!128");
        }

        // 添加家族勋章信息
        t = Cat.getProducer().newTransaction("MRedis", "MedalSource.getUserMedalsAsJson");
        try {
            result.add("userMedal", MedalSource.getUserMedalsAsJson(userId, platform));
            t.setStatus(Transaction.SUCCESS);
        } catch (Exception e) {
            logger.error("MedalSource.getUserMedalsAsJson(" + userId + ") execute exception.", e);
            Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
            t.setStatus(e);
        } finally {
            t.complete();
        }

        // 获取爵位勋章
        try {
            ActivityMedalService activityMedalService = (ActivityMedalService) MelotBeanFactory.getBean("activityMedalService");
            // 添加充值勋章信息,充值勋章所需要的字段都放到redis中，避免二次查询数据库
            UserMedalService userMedalService = (UserMedalService) MelotBeanFactory.getBean("userMedalService");
            com.melot.module.medal.driver.domain.GsonMedalObj medal = null;
            t = Cat.getProducer().newTransaction("MCall", "userMedalService.getMedalsByUserId");
            try {
                medal = userMedalService.getMedalsByUserId(userId);
                t.setStatus(Transaction.SUCCESS);
            } catch (Exception e) {
                Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                t.setStatus(e);
            } finally {
                t.complete();
            }
            Date now = new Date();
            List<ConfMedal> medals = new ArrayList<ConfMedal>();
            if (medal != null) {
                ConfMedal confMedal = null;
                if (medal.getEndTime() > now.getTime()) { // 如果没有过期的话，才显示出来
                    MedalInfo medalInfo = null;
                    t = Cat.getProducer().newTransaction("MCall", "MedalConfig.getMedal");
                    try {
                        medalInfo = MedalConfig.getMedal(medal.getMedalId());
                        t.setStatus(Transaction.SUCCESS);
                    } catch (Exception e) {
                        Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                        t.setStatus(e);
                    } finally {
                        t.complete();
                    }
                    if (medalInfo != null) {
                        confMedal = new ConfMedal();

                        confMedal.setBright(medal.getLightState());

                        // 提醒单独处理放到if判断中
                        if (medalInfo.getMedalLevel() == 8) {
                            confMedal.setMedalLevel(7);
                            confMedal.setIsTop(1);
                            confMedal.setMedalDes(medalInfo.getMedalDesc());
                        } else {
                            confMedal.setMedalLevel(medalInfo.getMedalLevel() - 1);
                            confMedal.setIsTop(0);
                            confMedal.setMedalDes(medalInfo.getMedalDesc());
                        }
                        confMedal.setMedalId(medalInfo.getMedalId());
                        confMedal.setMedalType(medalInfo.getMedalType());
                        confMedal.setMedalTitle(medalInfo.getMedalTitle());
                        confMedal.setMedalExpireTime(medal.getEndTime());
                        confMedal.setMedalMedalUrl(medalInfo.getMedalIcon());

                        // 点亮的勋章
                        if (confMedal.getBright() != 0) {
                            medals.add(confMedal);
                        }
                    }
                }
            }

            List<UserActivityMedal> wearList = null;
            t = Cat.getProducer().newTransaction("MCall", "activityMedalService.getUserWearMedals");
            try {
                wearList = activityMedalService.getUserWearMedals(userId);
                t.setStatus(Transaction.SUCCESS);
            } catch (Exception e) {
                Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                t.setStatus(e);
            } finally {
                t.complete();
            }
            if (wearList != null && wearList.size() > 0) {
                for (UserActivityMedal userActivityMedal : wearList) {
                    ConfMedal confMedal = new ConfMedal();
                    confMedal.setIsTop(0);
                    confMedal.setMedalId(userActivityMedal.getMedalId());
                    confMedal.setBright(userActivityMedal.getLightState());
                    confMedal.setMedalDes(userActivityMedal.getMedalDesc() != null ? String
                            .valueOf(new JsonParser().parse(userActivityMedal.getMedalDesc()).getAsJsonObject().get("description")) : null);
                    confMedal.setMedalType(userActivityMedal.getMedalType());
                    confMedal.setMedalTitle(userActivityMedal.getMedalTitle());
                    confMedal.setMedalExpireTime(userActivityMedal.getEndTime().getTime());
                    confMedal.setMedalMedalUrl(userActivityMedal.getMedalIcon());
                    medals.add(confMedal);
                }
            }
            // 直播精灵过滤充值勋章信息
            if (appId != 8) {
                result.add("userMedalList", new JsonParser().parse(new Gson().toJson(medals)).getAsJsonArray());
            }
        } catch (Exception e) {
            logger.error("Get user[" + userId + "] medal execute exception.", e);
        }
        result.addProperty("userId", userId);
        result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        // 返回结果
        return result;
    }
    
    /**
     * 获取用户默认收货地址【51010103】
     * 没有返回SUCCESS 但没有其他字段
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getUserAddressInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        int userId = 0;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        try {
            UserAddressService userAddressService = (UserAddressService) MelotBeanFactory.getBean("userAddressService");
            Result<UserAddressDO> moduleResult = userAddressService.getUserDefaultAddressDOByUserId(userId);
            if (moduleResult == null || !CommonStateCode.SUCCESS.equals(moduleResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            UserAddressDO userAddressDO = moduleResult.getData();
            if (userAddressDO != null) {
                result.addProperty("addressId", userAddressDO.getAddressId());
                result.addProperty("consigneeName", userAddressDO.getConsigneeName());
                result.addProperty("consigneeMobile", userAddressDO.getConsigneeMobile());
                result.addProperty("province", userAddressDO.getProvince());
                result.addProperty("city", userAddressDO.getCity());
                result.addProperty("district", userAddressDO.getDistrict());
                result.addProperty("detailAddress", userAddressDO.getDetailAddress());
            }
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error: userAddressService.getUserDefaultAddressDOByUserId(userId=%s)", userId), e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }
    
    /**
     * 设置用户收货地址信息【51010102】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject setUserAddressInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        int userId = 0;
        int addressId = 0;
        String consigneeName;
        String consigneeMobile;
        String province;
        String city;
        String district;
        String detailAddress;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            addressId = CommonUtil.getJsonParamInt(jsonObject, "addressId", 0, null, 1, Integer.MAX_VALUE);
            consigneeName = CommonUtil.getJsonParamString(jsonObject, "consigneeName", null, "5101010201", 1 , Integer.MAX_VALUE);
            consigneeMobile = CommonUtil.getJsonParamString(jsonObject, "consigneeMobile", null, "5101010202", 1 , Integer.MAX_VALUE);
            province = CommonUtil.getJsonParamString(jsonObject, "province", null, null, 0 , Integer.MAX_VALUE);
            city = CommonUtil.getJsonParamString(jsonObject, "city", null, null, 0 , Integer.MAX_VALUE);
            district = CommonUtil.getJsonParamString(jsonObject, "district", null, null, 0 , Integer.MAX_VALUE);
            detailAddress = CommonUtil.getJsonParamString(jsonObject, "detailAddress", null, "51010102", 1 , Integer.MAX_VALUE);
            
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            UserAddressService userAddressService = (UserAddressService) MelotBeanFactory.getBean("userAddressService");
            
            // 设置默认
            UserAddressParam param = new UserAddressParam();
            param.setAddressId(addressId <= 0 ? null : addressId);
            param.setCity(city);
            param.setConsigneeMobile(consigneeMobile);
            param.setConsigneeName(consigneeName);
            param.setDetailAddress(detailAddress);
            param.setDistrict(district);
            param.setIsDefaultAddress(1);
            param.setProvince(province);
            param.setUserId(userId);
            
            Result<Boolean> saveModuleResult = userAddressService.saveUserAddress(param);
            if (saveModuleResult == null || !CommonStateCode.SUCCESS.equals(saveModuleResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            
            // 获取ID
            Result<UserAddressDO> moduleResult = userAddressService.getUserDefaultAddressDOByUserId(userId);
            if (moduleResult == null || !CommonStateCode.SUCCESS.equals(moduleResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            UserAddressDO userAddressDO = moduleResult.getData();
            if (userAddressDO != null) {
                result.addProperty("addressId", userAddressDO.getAddressId());
            } else {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error: userAddressService.getUserDefaultAddressDOByUserId(userId=%s)", userId), e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }
	private static JsonObject checkLogin(JsonObject result, ResLogin resLogin, int loginType, JsonObject jsonObject) {
		if (loginType != -1 && resLogin != null && resLogin.getTagCode() != null) {
        	String TagCode = resLogin.getTagCode();
        	if (TagCode.equals(TagCodeEnum.SUCCESS)) {
        		return null;
        	} else if (TagCode.equals("02") || TagCode.equals("07")) {
                // '02'; 无对应用户记录,该用户未注册
                result.addProperty("TagCode", "01070103");
                return result;
            } else if (TagCode.equals("04") || TagCode.equals("05") || TagCode.equals("06")) {
                // '04'; /*用户黑名单*/
                // '05'; /*设备黑名单*/
                // '06'; /*IP黑名单*/
                result.addProperty("TagCode", "011301" + TagCode);
                return result;
            } else if (TagCode.equals("08")) {
                //封号
                result.addProperty("TagCode", "011301" + TagCode);
                return result;
            } else if (TagCode.equals("11")) {
                // 手机号黑名单
                result.addProperty("TagCode", "01130110");
                return result;
            } else {
                // 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
                logger.error("调用存储过程未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
                result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
                return result;
            }
		} else {
        	result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
            return result;
        }
	}
	
	private static String containLocale(String str) {
		String locales = "zh-tw,zh-cn,fr-ca,fr-fr,de-de,it-it,ja-jp,ko-kr,en-ca,en-gb,en-us,zh_tw,zh_cn,zh,fr_ca,fr_fr,fr,de_de,de,it_it,it,ja_jp,ja,ko_kr,ko,en_ca,en_gb,en_us,en";
		String[] array = locales.split(",");
		for (String locale : array) {
			if (str.toLowerCase().indexOf(locale) > 0) {
				return locale;
			}
		}
		return null;
	}
	
	private static String replaceBlank(String str) {
        String dest = "";
        if (str!=null) {
            Pattern p = Pattern.compile("\\s*|\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }
	
	private static void registerUserAssets(int userId, Long showMoney) {
	    try{
	        KkUserService userService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
	        userService.insertUserAssets(userId);
	        if (showMoney != null && showMoney > 0) {
	            userService.addAndGetUserAssets(userId, showMoney, false, null);
            }
	    } catch (Exception e) {
	        logger.error(" com.melot.kkcx.functions.UserFunctions.UserAssets(" + userId + ", " + showMoney + ") execute exception.", e);
        }
	}
	
}

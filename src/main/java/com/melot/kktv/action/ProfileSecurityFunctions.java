package com.melot.kktv.action;

import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;

import com.melot.kk.module.resource.domain.QiNiuTokenConf;
import com.melot.kk.module.resource.service.ResourceNewService;
import org.apache.log4j.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.melot.blacklist.service.BlacklistService;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcx.functions.ProfileFunctions;
import com.melot.kkcx.functions.UserFunctions;
import com.melot.kkcx.service.GeneralService;
import com.melot.kktv.redis.HotDataSource;
import com.melot.kktv.redis.SmsSource;
import com.melot.kktv.service.AccountService;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.CommonUtil.ErrorGetParameterException;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.LoginTypeEnum;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.SecurityFunctions;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.qiniu.common.QiniuService;
import com.melot.sdk.core.util.MelotBeanFactory;

public class ProfileSecurityFunctions {

	private static Logger loginLogger = Logger.getLogger("loginLogger");

	private static JsonObject setUserNameAndPassword(JsonObject jsonObject, String up){

		//根据不同的platform,对password做URLDecode : Fix issue : The android client will urlencode the password
		JsonElement platformje = jsonObject.get("platform");
		int platform = -1;
		if (platformje != null) {
			try {
				platform = platformje.getAsInt();
			} catch (Exception e) {
			}
		}
		if (platform == -1 || platform > PlatformEnum.IPAD) {
			return null;
		}

		String userName = null;
		String password = null;
		String psw = null;
		String un  = null;
		int l = up.length();
		int u = up.indexOf("u=");
		int p = up.indexOf("&p=");
		if(u == 0 && p != -1){
			un = up.substring(2, p);
			psw = up.substring(p+3, l);
			try {
				userName = URLDecoder.decode(un, "utf-8");
				if(platform != PlatformEnum.WEB)
					password = URLDecoder.decode(psw, "utf-8");
				else
					password = psw;
			} catch (Exception e) {
			}
		}
		if(userName == null || password == null){
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "40020002");
			return result;
		}

		jsonObject.addProperty("usernameEnc", un);
		jsonObject.addProperty("username", userName);
		jsonObject.addProperty("passwordEnc", psw);
		jsonObject.addProperty("psword", password);
		return null;//null means no error
	}

	private static JsonObject setUserIdAndPassword(JsonObject jsonObject, String up){
		JsonElement platformje = jsonObject.get("platform");
		int platform = -1;
		if (platformje != null) {
			try {
				platform = platformje.getAsInt();
			} catch (Exception e) {
			}
		}
		if (platform == -1 || platform > PlatformEnum.IPAD) {
			return null;
		}

		int uid = 0;
		String psw = null;
		String un  = null;
		int l = up.length();
		int u = up.indexOf("u=");
		int p = up.indexOf("&p=");
		if(u == 0 && p != -1){
			un = up.substring(2, p);
			psw = up.substring(p+3, l);
			try {
				String uidStr = URLDecoder.decode(un, "utf-8");
				uid = new Integer(uidStr);
				if(platform != PlatformEnum.WEB)
					psw = URLDecoder.decode(psw, "utf-8");
			} catch (Exception e) {
			}
		}
		if(uid == 0 || psw == null){
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "40020002");
			return result;
		}

		jsonObject.addProperty("userId", uid);
		jsonObject.addProperty("psword", psw);
		return null;
	}

	private static JsonObject setPhonenumAndPassword(JsonObject jsonObject, String up){
		JsonElement platformje = jsonObject.get("platform");
		int platform = -1;
		if (platformje != null) {
			try {
				platform = platformje.getAsInt();
			} catch (Exception e) {
			}
		}
		if (platform == -1 || platform > PlatformEnum.IPAD) {
			return null;
		}
		String phoneNum = null;
		String psw = null;
		String un  = null;
		int l = up.length();
		int u = up.indexOf("u=");
		int p = up.indexOf("&p=");
		if(u == 0 && p != -1){
			un = up.substring(2, p);
			psw = up.substring(p+3, l);
			try {
				phoneNum = URLDecoder.decode(un, "utf-8");
				if(platform != PlatformEnum.WEB)
					psw = URLDecoder.decode(psw, "utf-8");
			} catch (Exception e) {
			}
		}
		if(phoneNum == null || psw == null){
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "40020002");
			return result;
		}

		jsonObject.addProperty("phoneNum", phoneNum);
		jsonObject.addProperty("psword", psw);
		return null;
	}

	/**
	 * 安全注册(40000001)
	 *
	 * @param jsonObject 请求对象
	 * @param request 请求对象
	 * @return 注册结果
	 */
	public JsonObject register(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
		if(rtJO != null)
			return rtJO;

		String up = SecurityFunctions.decodeUserNameAndPassword(jsonObject);
		if(up == null){
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "40020001");
			return result;
		}

		rtJO = ProfileSecurityFunctions.setUserNameAndPassword(jsonObject, up);
		if(rtJO != null)
			return rtJO;

		//是否为安全登录（额外添加属性）
		jsonObject.addProperty("isSafe", "1");

		//签名和解码正确，调用我们原来的接口
		UserFunctions userFunctions = MelotBeanFactory.getBean("userFunction", UserFunctions.class);
		return userFunctions.register(jsonObject, checkTag, request);
	}

	/**
	 * 安全修改密码(40000008)
	 *
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return 修改密码结果
	 */
	public JsonObject changePwd(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
		if(rtJO != null)
			return rtJO;

		String up = SecurityFunctions.decodeUserNameAndPassword(jsonObject);
		if(up == null){
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "40020001");
			return result;
		}

		rtJO = ProfileSecurityFunctions.setUserNameAndPassword(jsonObject, up);
		if(rtJO != null)
			return rtJO;


		String newPassword = SecurityFunctions.decodeNewPassword(jsonObject);
		if(newPassword == null){
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "40020002");
			return result;
		}

		jsonObject.addProperty("newpwd", newPassword);//set the decoded uuid
		JsonElement oldPwdJE = jsonObject.get("psword");
		jsonObject.addProperty("oldpwd", oldPwdJE.getAsString());

		//签名和解码正确，调用我们原来的接口
		UserFunctions userFunctions = MelotBeanFactory.getBean("userFunction", UserFunctions.class);
		return userFunctions.changePwd(jsonObject, checkTag, request);
	}

	/**
	 * 安全登录接口(40000002)
	 *
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 *
	 * @return 登录结果
	 */
	public JsonObject login_new(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		long startTime = System.currentTimeMillis();

		JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
		if(rtJO != null)
			return rtJO;

		JsonElement loginTypeje = jsonObject.get("loginType");
		int loginType = 0;
		if (loginTypeje != null) {
			try {
				loginType = loginTypeje.getAsInt();
			} catch (Exception e) {
			}
		}
		if (loginType == 0) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "40020003");
			return result;
		}

		if(loginType > 0){
			//第三方登录
			String uuid = SecurityFunctions.decodeUUID(jsonObject);
			if(uuid == null){
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "40020004");
				return result;
			}
			jsonObject.addProperty("uuid", uuid);//set the decoded uuid

			if (loginType == LoginTypeEnum.WEIXIN || loginType == LoginTypeEnum.FACEBOOK) {
				String unionid = SecurityFunctions.decodeUnionId(jsonObject);
				if (unionid != null){
					jsonObject.addProperty("unionid", unionid);//set the decoded unionid
				}
			}
		} else if(loginType == LoginTypeEnum.NAMEPASSWORD){
			//用户名密码
			String up = SecurityFunctions.decodeUserNameAndPassword(jsonObject);
			if(up == null){
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "40020001");
				return result;
			}

			rtJO = ProfileSecurityFunctions.setUserNameAndPassword(jsonObject, up);
			if(rtJO != null)
				return rtJO;
		} else if(loginType == LoginTypeEnum.IDPASSWORD){
			//用户ID密码
			String up = SecurityFunctions.decodeUserNameAndPassword(jsonObject);
			if(up == null){
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "40020001");
				return result;
			}

			rtJO = ProfileSecurityFunctions.setUserIdAndPassword(jsonObject, up);
			if(rtJO != null)
				return rtJO;
		} else if(loginType == LoginTypeEnum.PHONE){
			try {
				int platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, "01130009", 1, Integer.MAX_VALUE);
				if (PlatformEnum.WEB == platform) {
					JsonObject result = new JsonObject();
					result.addProperty("TagCode", TagCodeEnum.FUNCTAG_UNUSED_EXCEPTION);
					return result;
				}
			} catch(CommonUtil.ErrorGetParameterException e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", e.getErrCode());
				return result;
			} catch(Exception e) {
//				JsonObject result = new JsonObject();
//	            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
//	            return result;
			}

			//手机号密码
			String up = SecurityFunctions.decodeUserNameAndPassword(jsonObject);
			if(up == null){
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "40020001");
				return result;
			}

			rtJO = ProfileSecurityFunctions.setPhonenumAndPassword(jsonObject, up);
			if(rtJO != null)
				return rtJO;
		}else if(loginType == LoginTypeEnum.IDENTITY){
			//身份注册
			String up = SecurityFunctions.decodeUserNameAndPassword(jsonObject);
			if(up == null){
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "40020001");
				return result;
			}
			rtJO = ProfileSecurityFunctions.setPhonenumAndPassword(jsonObject, up);
			if(rtJO != null)
				return rtJO;
		}
		//是否为安全登录（额外添加属性）
		jsonObject.addProperty("isSafe", "1");
		//签名和解码正确，调用我们原来的接口
		UserFunctions userFunctions = MelotBeanFactory.getBean("userFunction", UserFunctions.class);
		JsonObject resultJsonObject = userFunctions.login_new(jsonObject, checkTag, request);
		loginLogger.debug("com.melot.kktv.action.ProfileSecurityFunctions.login_new use time " + (System.currentTimeMillis() - startTime) + " ms .....");
		return resultJsonObject;
	}

	/**
	 * 生成游客用户ID(40000016)
	 *
	 * @param jsonObject 请求对象
	 * @param request 请求对象
	 * @return
	 */
	public JsonObject genMobileGuestUser(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
		if(rtJO != null)
			return rtJO;

		//签名正确，调用我们原来的接口
		com.melot.kkcx.functions.UserFunctions userFunctions = MelotBeanFactory.getBean("userFunction", com.melot.kkcx.functions.UserFunctions.class);
		return userFunctions.genMobileGuestUser(jsonObject, checkTag, request);
	}

	/**
	 * 游客注册(40000017)
	 *
	 * @param jsonObject 请求对象
	 * @param request 请求对象
	 * @return 注册结果
	 */
	public JsonObject registerIphoneBackground(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
		if(rtJO != null)
			return rtJO;

		String up = SecurityFunctions.decodeUserNameAndPassword(jsonObject);
		if(up == null){
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "40020001");
			return result;
		}

		rtJO = ProfileSecurityFunctions.setUserIdAndPassword(jsonObject, up);;
		if(rtJO != null)
			return rtJO;

		//是否为安全登录（额外添加属性）
		jsonObject.addProperty("isSafe", "1");

		//签名和解码正确，调用我们原来的接口
		UserFunctions userFunctions = MelotBeanFactory.getBean("userFunction", UserFunctions.class);
		return userFunctions.registerIphoneBackground(jsonObject, checkTag, request);
	}

	/**
	 * 领取任务奖励(40000006)
	 *
	 * @param jsonObject 请求对象
	 * @param request 请求对象
	 * @return
	 */
	public JsonObject getReward(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
		if(rtJO != null)
			return rtJO;

		//签名正确，调用我们原来的接口
		ProfileFunctions profileFunctions = MelotBeanFactory.getBean("profileFunction", ProfileFunctions.class);
		return profileFunctions.getReward(jsonObject, checkTag, request);
	}

	/**
	 * 七日签到抽奖(40000023)
	 *
	 * @param jsonObject 请求对象
	 * @param request 请求对象
	 * @return
	 */
	public JsonObject weeklyLottery(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
		if (rtJO != null) {
			return rtJO;
		}

		ProfileFunctions profileFunctions = MelotBeanFactory.getBean("profileFunction", ProfileFunctions.class);
		return profileFunctions.weeklyLottery(jsonObject, checkTag, request);
	}

	/**
	 * 安全第三方平台用户注册(40000003)
	 *
	 * @param jsonObject 请求对象
	 * @param request 请求对象
	 * @return 注册结果
	 */
	public JsonObject registerVia3rdPlatform(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
		if(rtJO != null)
			return rtJO;

		String uuid = SecurityFunctions.decodeUUID(jsonObject);
		if(uuid == null){
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "40020001");
			return result;
		}

		jsonObject.addProperty("uuid", uuid);//set the decoded uuid

		JsonElement loginTypeje = jsonObject.get("openPlatform");
		int loginType = 0;
		if (loginTypeje != null) {
			try {
				loginType = loginTypeje.getAsInt();
			} catch (Exception e) {
			}
		}
		if (loginType == LoginTypeEnum.WEIXIN || loginType == LoginTypeEnum.FACEBOOK) {
			String unionid = SecurityFunctions.decodeUnionId(jsonObject);
			if (unionid != null){
				jsonObject.addProperty("unionid", unionid);//set the decoded unionid
			}
		}

		//是否为安全登录（额外添加属性）
		jsonObject.addProperty("isSafe", "1");

		//签名正确，调用我们原来的接口
		UserFunctions userFunctions = MelotBeanFactory.getBean("userFunction", UserFunctions.class);
		return userFunctions.registerVia3rdPlatform(jsonObject, checkTag, request);
	}

	/**
	 * 找回密码(40000010)
	 *
	 * @created 2103-11-11 by RC
	 * @param jsonObject
	 * @return
	 */
	public JsonObject retrievePassword(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
		if(rtJO != null)
			return rtJO;

		//是否为安全登录（额外添加属性）
		jsonObject.addProperty("isSafe", "1");

		//签名正确，调用我们原来的接口
		UserFunctions userFunctions = MelotBeanFactory.getBean("userFunction", UserFunctions.class);
		return userFunctions.retrievePassword(jsonObject, checkTag, request);
	}

	/**
	 * 手机注册(40000011)
	 *
	 * @param jsonObject
	 * @return
	 */
	public JsonObject registerViaPhoneNum(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
		if(rtJO != null)
			return rtJO;

		//是否为安全登录（额外添加属性）
		jsonObject.addProperty("isSafe", "1");

		//签名正确，调用我们原来的接口
		UserFunctions userFunctions = MelotBeanFactory.getBean("userFunction", UserFunctions.class);
		return userFunctions.registerViaPhoneNum(jsonObject, checkTag, request);
	}

	/**
	 * 用户设置密码(40000012)
	 *
	 * @param jsonObject
	 * @param checkTag
	 * @param request
	 * @return
	 */
	public JsonObject setPassword(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
		if(rtJO != null)
			return rtJO;

		//用户ID密码
		String up = SecurityFunctions.decodeUserNameAndPassword(jsonObject);
		if(up == null){
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "40020001");
			return result;
		}

		rtJO = ProfileSecurityFunctions.setUserIdAndPassword(jsonObject, up);
		if(rtJO != null)
			return rtJO;


		//签名和解码正确，调用我们原来的接口
		UserFunctions userFunctions = MelotBeanFactory.getBean("userFunction", UserFunctions.class);
		return userFunctions.setPassword(jsonObject, checkTag, request);
	}

	/**
	 *  登陆合并 (40000015)
	 * @param jsonObject
	 * @param checkTag
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public JsonObject uniteLogin(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
		if(rtJO != null)
			return rtJO;

		JsonObject result = new JsonObject();

		String up = SecurityFunctions.decodeUserNameAndPassword(jsonObject);
		if(up == null) {
			result.addProperty("TagCode", "40020001");
			return result;
		}

		int loginType = 0;
		String inputStr = up.substring(2, up.indexOf("&p="));
		// 判断纯数字
		if (inputStr.matches("[0-9]*")) {
			loginType = LoginTypeEnum.IDPASSWORD;
			// 判断是否为手机号
			if (inputStr.matches("^(13|14|15|17|18)\\d{9}$")) {
				loginType = LoginTypeEnum.PHONE;

				try {
					int platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, "01130009", 1, Integer.MAX_VALUE);
					if (PlatformEnum.WEB == platform) {
						result.addProperty("TagCode", TagCodeEnum.FUNCTAG_UNUSED_EXCEPTION);
						return result;
					}
				} catch(CommonUtil.ErrorGetParameterException e) {
					result.addProperty("TagCode", e.getErrCode());
					return result;
				} catch(Exception e) {
//					JsonObject result = new JsonObject();
//		            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
//		            return result;
				}
			}
		} else {
			loginType = LoginTypeEnum.NAMEPASSWORD;
		}
		if(loginType == LoginTypeEnum.NAMEPASSWORD) {
			//用户名密码
			rtJO = ProfileSecurityFunctions.setUserNameAndPassword(jsonObject, up);
			if(rtJO != null)
				return rtJO;
		} else if(loginType == LoginTypeEnum.IDPASSWORD) {
			//用户ID密码
			rtJO = ProfileSecurityFunctions.setUserIdAndPassword(jsonObject, up);
			if(rtJO != null)
				return rtJO;
		} else if(loginType == LoginTypeEnum.PHONE) {
			//手机号密码
			rtJO = ProfileSecurityFunctions.setPhonenumAndPassword(jsonObject, up);
			if(rtJO != null)
				return rtJO;
		} else {
			result.addProperty("TagCode", "40020003");
			return result;
		}

		//是否为安全登录（额外添加属性）
		jsonObject.addProperty("isSafe", "1");
		jsonObject.addProperty("loginType", loginType);
		//签名和解码正确，调用我们原来的接口
		UserFunctions userFunctions = MelotBeanFactory.getBean("userFunction", UserFunctions.class);
		return userFunctions.login_new(jsonObject, checkTag, request);
	}

	/**
	 * 新版通过密码验证登录(40000021)
	 * @param jsonObject
	 * @param checkTag
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public JsonObject loginViaPasswordNew(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
		if (rtJO != null) {
			return rtJO;
		}

		JsonObject result = new JsonObject();
		String up = SecurityFunctions.decodeUserNameAndPassword(jsonObject);
		if(up == null) {
			result.addProperty("TagCode", "40020001");
			return result;
		}
		int loginType = 0;
		String inputStr = up.substring(2, up.indexOf("&p="));
		// 判断纯数字
		if (inputStr.matches("[0-9]*")) {
			loginType = LoginTypeEnum.IDPASSWORD;
			// 判断是否为手机号
			if (inputStr.matches("^(13|14|15|17|18)\\d{9}$")) {
				loginType = LoginTypeEnum.PHONE;
			}
		} else {
			loginType = LoginTypeEnum.NAMEPASSWORD;
		}
		if(loginType == LoginTypeEnum.NAMEPASSWORD) {
			//用户名密码
			rtJO = ProfileSecurityFunctions.setUserNameAndPassword(jsonObject, up);
			if(rtJO != null)
				return rtJO;
		} else if(loginType == LoginTypeEnum.IDPASSWORD) {
			//用户ID密码
			rtJO = ProfileSecurityFunctions.setUserIdAndPassword(jsonObject, up);
			if(rtJO != null)
				return rtJO;
		} else if(loginType == LoginTypeEnum.PHONE) {
			try {
				int platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, "01130009", 1, Integer.MAX_VALUE);
				if (PlatformEnum.WEB == platform) {
					result.addProperty("TagCode", TagCodeEnum.FUNCTAG_UNUSED_EXCEPTION);
					return result;
				}
			} catch(CommonUtil.ErrorGetParameterException e) {
				result.addProperty("TagCode", e.getErrCode());
				return result;
			} catch(Exception e) {
//				JsonObject result = new JsonObject();
//	            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
//	            return result;
			}
			//手机号密码
			rtJO = ProfileSecurityFunctions.setPhonenumAndPassword(jsonObject, up);
			if(rtJO != null)
				return rtJO;
		} else {
			result.addProperty("TagCode", "40020003");
			return result;
		}

		jsonObject.addProperty("loginType", loginType);
		jsonObject.addProperty("isSafe", 1);
		UserFunctions userFunctions = MelotBeanFactory.getBean("userFunction", UserFunctions.class);
		return userFunctions.loginViaPasswordNew(jsonObject, checkTag, request);
	}

	/**
	 * 新版通过手机验证登录(40000022)
	 * @param jsonObject
	 * @param checkTag
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public JsonObject loginViaVerifyNew(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
		if (rtJO != null) {
			return rtJO;
		}

		JsonObject result = new JsonObject();
		String verifyCode, phoneNum;
		try {
			verifyCode = CommonUtil.getJsonParamString(jsonObject, "verifyCode", null, "01440002", 1, Integer.MAX_VALUE);
			phoneNum = CommonUtil.getJsonParamString(jsonObject, "phoneNum", null, "01130011", 0, 256);
		} catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch (Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}

		String data = SmsSource.getPhoneSmsData(phoneNum, String.valueOf(29));
		if (data != null && data.equals(verifyCode)) {
			String up = SecurityFunctions.decodeUserNameAndPassword(jsonObject);
			if(up == null) {
				result.addProperty("TagCode", "40020001");
				return result;
			}
			int loginType = 0;
			String inputStr = up.substring(2, up.indexOf("&p="));
			// 判断纯数字
			if (inputStr.matches("[0-9]*")) {
				loginType = LoginTypeEnum.IDPASSWORD;
				// 判断是否为手机号
				if (inputStr.matches("^(13|14|15|17|18)\\d{9}$")) {
					loginType = LoginTypeEnum.PHONE;
				}
			} else {
				loginType = LoginTypeEnum.NAMEPASSWORD;
			}
			if(loginType == LoginTypeEnum.NAMEPASSWORD) {
				//用户名密码
				rtJO = ProfileSecurityFunctions.setUserNameAndPassword(jsonObject, up);
				if(rtJO != null)
					return rtJO;
			} else if(loginType == LoginTypeEnum.IDPASSWORD) {
				//用户ID密码
				rtJO = ProfileSecurityFunctions.setUserIdAndPassword(jsonObject, up);
				if(rtJO != null)
					return rtJO;
			} else if(loginType == LoginTypeEnum.PHONE) {
				//手机号密码
				rtJO = ProfileSecurityFunctions.setPhonenumAndPassword(jsonObject, up);
				if(rtJO != null)
					return rtJO;
			} else {
				result.addProperty("TagCode", "40020003");
				return result;
			}

			jsonObject.addProperty("loginType", loginType);
			jsonObject.addProperty("isSafe", 1);
			jsonObject.addProperty("fromVerifyLogin", 1);
			UserFunctions userFunctions = MelotBeanFactory.getBean("userFunction", UserFunctions.class);
			return userFunctions.loginViaPasswordNew(jsonObject, checkTag, request);
		}

		result.addProperty("TagCode", "00402201");
		return result;
	}

	/**
	 * 抢红包安全验证接口(40000018)
	 * @param jsonObject
	 * @param checkTag
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public JsonObject grabRedEvelopeViaIdentity(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
		if (rtJO != null) {
			return rtJO;
		}

		//是否为安全登录（额外添加属性）
		jsonObject.addProperty("isSafe", "1");

		//签名正确，调用我们原来的接口
		RedEnvelopeFunctions redEnvelopeFunctions = MelotBeanFactory.getBean("redEnvelopeFunction", RedEnvelopeFunctions.class);
		return redEnvelopeFunctions.grabRedEvelope(jsonObject, checkTag, request);
	}

	/**
	 * 通过type验证手机(40000024)
	 * @param jsonObject
	 * @param checkTag
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public JsonObject identifyPhoneByType(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
		if (rtJO != null) {
			return rtJO;
		}

		JsonObject result = new JsonObject();
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}

		String phoneNum, verifyCode;
		int type, userId;
		try {
			phoneNum = CommonUtil.getJsonParamString(jsonObject, "phoneNum", null, "01440001", 1, Integer.MAX_VALUE);
			if (phoneNum != null) phoneNum = CommonUtil.validatePhoneNum(phoneNum, "86");
			verifyCode = CommonUtil.getJsonParamString(jsonObject, "verifyCode", null, "01440002", 1, Integer.MAX_VALUE);
			type = CommonUtil.getJsonParamInt(jsonObject, "type", 0, "01042401", 1, Integer.MAX_VALUE);
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
		} catch(CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch(Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}

		String data = SmsSource.getPhoneSmsData(phoneNum, String.valueOf(type));
		if (data != null && data.equals(verifyCode)) {
			if (type == 30) {
				HotDataSource.setTempDataString("identifyFormalPhone_" + userId, "1", 300);
			}
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		} else {
			result.addProperty("TagCode", "01042402");
		}

		return result;
	}

	/**
	 * 认证手机号(40000019 或 40000025)
	 * @param jsonObject
	 * @param checkTag
	 * @param request
	 * @return
	 */
	public JsonObject identifyPhone(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
		if (rtJO != null) {
			return rtJO;
		}

		JsonObject result = new JsonObject();
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}

		String phoneNum, verifyCode;
		int userId, appId, platform, functag;
		try {
			functag = CommonUtil.getJsonParamInt(jsonObject, "FuncTag", 0, null, 1, Integer.MAX_VALUE);
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
			phoneNum = CommonUtil.getJsonParamString(jsonObject, "phoneNum", null, "01440001", 1, Integer.MAX_VALUE);
			if (phoneNum != null) phoneNum = CommonUtil.validatePhoneNum(phoneNum, "86");
			verifyCode = CommonUtil.getJsonParamString(jsonObject, "verifyCode", null, "01440002", 1, Integer.MAX_VALUE);
			appId = CommonUtil.getJsonParamInt(jsonObject, "a", StringUtil.parseFromStr(MelotBeanFactory.getBean("appId", String.class), AppIdEnum.AMUSEMENT), null, 1, Integer.MAX_VALUE);
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, TagCodeEnum.PLATFORM_MISSING, 1, Integer.MAX_VALUE);
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
				result.addProperty("TagCode", "01440005");
				return result;
			}
		} catch (Exception e) {
		}

		String bindPhoneNum = null;
		String identifyPhone = null;
		UserProfile userProfile = com.melot.kktv.service.UserService.getUserInfoV2(userId);
		if (userProfile != null) {
			bindPhoneNum = userProfile.getPhoneNum();
			identifyPhone = userProfile.getIdentifyPhone();
		}
		if (identifyPhone != null && (functag == 40000025 && !"1".equals(HotDataSource.getTempDataString("identifyFormalPhone_" + userId)))) {
			result.addProperty("TagCode", "01440009");
			return result;
		}
		String data = SmsSource.getPhoneSmsData(phoneNum, String.valueOf(20));
		if (data != null && data.equals(verifyCode)) {
			try {
				com.melot.kkcore.user.service.KkUserService userService = (com.melot.kkcore.user.service.KkUserService) MelotBeanFactory.getBean("kkUserService");
				int bindUserId = userService.getUserIdByPhoneNumber(phoneNum);
				if (bindPhoneNum != null && bindPhoneNum.equals(phoneNum)) {
					//手机号已绑定验证过当前账号
					result.addProperty("TagCode", "01440007");
					return result;
				}
				if ((bindPhoneNum != null || (identifyPhone != null && bindPhoneNum == null && identifyPhone.equals(phoneNum))) && bindUserId != 0) {
					//验证失败（手机号不可被当前账户绑定）
					result.addProperty("TagCode", "01440008");
					return result;
				}

				com.melot.kkcore.account.service.AccountService accountService = (com.melot.kkcore.account.service.AccountService) MelotBeanFactory.getBean("kkAccountService");
				String tagCode = accountService.verifyIdentifyPhone(userId, phoneNum);
				if (tagCode.equals(TagCodeEnum.SUCCESS)) {
					if (bindPhoneNum != null && bindPhoneNum != phoneNum) {
						if (!AccountService.unbindPhoneAccount(userId, bindPhoneNum, CommonUtil.getIpAddr(request))) {
							result.addProperty("TagCode", "01440006");
							return result;
						}
					}
					String bindCode = AccountService.bindPhoneNumAccount(userId, phoneNum, platform, CommonUtil.getIpAddr(request), appId);
					if (bindCode != null && bindCode.equals(TagCodeEnum.SUCCESS)) {
						result.addProperty("TagCode", TagCodeEnum.SUCCESS);
					} else {
						//验证手机号成功，绑定手机号失败
						result.addProperty("TagCode", "01440006");
					}
				} else if (tagCode.equals("02")) {
					//手机号认证超过10个账号
					result.addProperty("TagCode", "01440004");
				} else {
					result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
				}
			} catch (Exception e) {
				loginLogger.error("call procedure User.identifyPhone catched exception, userId : " + userId + ", phoneNum : " + phoneNum, e);
				result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			}
		} else {
			result.addProperty("TagCode", "01440003");
			return result;
		}

		return result;
	}

	/**
	 * 发送短信接口(40000020)
	 * @param jsonObject
	 * @param checkTag
	 * @param request
	 * @return
	 */
	public JsonObject sendSMS(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
		if (rtJO != null) {
			return rtJO;
		}

		//是否为安全接口（额外添加属性）
		jsonObject.addProperty("isSafe", "1");

		//签名正确，调用我们原来的接口
		com.melot.kktv.action.UserFunctions publicUserFunction = MelotBeanFactory.getBean("publicUserFunction", com.melot.kktv.action.UserFunctions.class);
		return publicUserFunction.sendSMS(jsonObject, checkTag, request);
	}



	/**
	 * 获取七牛上传Token (40000026) 走安全签名，允许游客上传资源
	 * @param jsonObject
	 * @param checkTag
	 * @return
	 */
	@SuppressWarnings("unused")
	public JsonObject getUploadToken(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {

		JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
		if (rtJO != null) {
			return rtJO;
		}
		// 定义返回结果
		JsonObject result = new JsonObject();

		int userId, resType, newsType, resumeUp, mimeType, vframeSeconds, appId = AppIdEnum.GAME, transcoding, opusState, newsState;
		String videoTitle = null, videoContent = null, uuid;
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
			resumeUp = CommonUtil.getJsonParamInt(jsonObject, "resumeUp", 0, null, 1, Integer.MAX_VALUE);
			mimeType = CommonUtil.getJsonParamInt(jsonObject, "mimeType", 0, null, 1, Integer.MAX_VALUE);
			videoContent = CommonUtil.getJsonParamString(jsonObject, "videoContent", null, null, 1, 500);
			vframeSeconds = CommonUtil.getJsonParamInt(jsonObject, "vframeSeconds", 0, null, 1, Integer.MAX_VALUE);
			appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
			//original = CommonUtil.getJsonParamInt(jsonObject, "original", 1, null, 1, Integer.MAX_VALUE);
			if (ConfigHelper.getMoreAppFlag() == 0) {
				appId = CommonUtil.getJsonParamInt(jsonObject, "a", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
				if (!GeneralService.isLegalAppId(appId)) {
					throw new ErrorGetParameterException(TagCodeEnum.APPID_MISSING);
				}
			}
			videoTitle = CommonUtil.getJsonParamString(jsonObject, "videoTitle", null, null, 1, 50);
			transcoding = CommonUtil.getJsonParamInt(jsonObject, "transcoding", 0, null, 0, 1);
			uuid = CommonUtil.getJsonParamString(jsonObject, "uuid", null, null, 1, 40);
			opusState = CommonUtil.getJsonParamInt(jsonObject, "opusState", 0, null, 1, 9);
			newsState = CommonUtil.getJsonParamInt(jsonObject, "newsState", 0, null, 1, 9);
		} catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch (Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		String key = QiniuService.getFileName(appId, userId, null);
		try {
			QiNiuTokenConf qiNiuTokenConf = new QiNiuTokenConf();
			qiNiuTokenConf.setUserId(userId);
			qiNiuTokenConf.setKey(key);
			qiNiuTokenConf.setVframeSeconds(vframeSeconds);
			qiNiuTokenConf.setAppId(appId);
			qiNiuTokenConf.setMimeType(mimeType);
			qiNiuTokenConf.setResumeUp(resumeUp);
			if (videoTitle != null) {
				qiNiuTokenConf.setVideoTitle(videoTitle);
			}
			if (videoContent != null) {
				qiNiuTokenConf.setVideoContent(videoContent);
			}
			qiNiuTokenConf.setFunctag(20002051);
			if (!StringUtil.strIsNull(ConfigHelper.getKkApiAddress())) {
				qiNiuTokenConf.setApiAddress(ConfigHelper.getKkApiAddress());
			}

			qiNiuTokenConf.setNewsState(newsState);
			qiNiuTokenConf.setOpusState(opusState);
			qiNiuTokenConf.setTranscoding(transcoding);
			if (!StringUtil.strIsNull(uuid)) {
				qiNiuTokenConf.setUuid(uuid);
			}
			String token = getQinuUploadToken(qiNiuTokenConf);
			if (token != null && !token.trim().isEmpty()) {
				result.addProperty("upToken", token);
				result.addProperty("key", key);
				result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			} else {
				result.addProperty("TagCode", TagCodeEnum.GET_UPLOAD_TOKEN_FAIL);
			}
		} catch (Exception e) {
			loginLogger.error("ProfileSecurityFunctions.getUploadToken error , userId : " + userId, e);
			result.addProperty("TagCode", TagCodeEnum.GET_UPLOAD_TOKEN_FAIL);
		}

		return result;
	}

	/**
	 * 图片信息、视频存入数据库
	 * @param jsonObject
	 * @param checkTag
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public JsonObject insertToDB(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception{

		JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
		if (rtJO != null) {
			return rtJO;
		}

		com.melot.kkcx.functions.AlbumFunctions albumFunctions = MelotBeanFactory.getBean("albumFunction", com.melot.kkcx.functions.AlbumFunctions.class);
		return albumFunctions.insertToDB(jsonObject, true, request);
	}

	/**
	 * 获取七牛上传视频token
	 * @param qiNiuTokenConf
	 * @return
	 */
	private static String getQinuUploadToken(QiNiuTokenConf qiNiuTokenConf) {
		ResourceNewService resourceNewService = (ResourceNewService)MelotBeanFactory.getBean("resourceNewService");
		if (resourceNewService != null) {
			return resourceNewService.getUpLoadTokenByDomain(qiNiuTokenConf);
		}
		return null;
	}
}
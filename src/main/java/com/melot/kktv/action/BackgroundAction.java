package com.melot.kktv.action;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.kk.module.report.service.FeedbackInfoService;
import com.melot.kk.otherlogin.api.dto.InstallPack;
import com.melot.kk.otherlogin.api.dto.MobileDevice;
import com.melot.kk.otherlogin.api.service.OtherLoginService;
import com.melot.kktv.util.*;
import com.melot.sdk.core.util.MelotBeanFactory;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * 运营后台需要的接口
 * @author RC
 *
 */
public class BackgroundAction {

	/** 日志记录对象 */
	private static Logger logger = Logger.getLogger(BackgroundAction.class);
	
	/**
	 * 用户反馈接口(10007001)
	 * 
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return 登录结果
	 */
	public JsonObject feedback(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}

		JsonObject result = new JsonObject();
		// 获取参数
		JsonElement userIdje = jsonObject.get("userId");
		JsonElement contentje = jsonObject.get("content");
		JsonElement platformje = jsonObject.get("platform");
		JsonElement appIdje = jsonObject.get("a");
		JsonElement channelje = jsonObject.get("c");
		// 验证参数
		int userId;
		int platform;
		String content;
		int appId = AppIdEnum.AMUSEMENT;
		int channel = AppChannelEnum.KK;
		String note;
		if (userIdje != null && !userIdje.isJsonNull() && !userIdje.getAsString().equals("")) {
			// 验证数字
			try {
				userId = userIdje.getAsInt();
			} catch (Exception e) {
				result.addProperty("TagCode", "07010002");
				return result;
			}
		} else {
			result.addProperty("TagCode", "07010001");
			return result;
		}
		if (contentje != null && !contentje.isJsonNull() && !contentje.getAsString().equals("")) {
			content = contentje.getAsString();
		} else {
			result.addProperty("TagCode", "07010003");
			return result;
		}
		if (platformje != null && !platformje.isJsonNull() && !platformje.getAsString().equals("")) {
			// 验证数字
			try {
				platform = platformje.getAsInt();
			} catch (Exception e) {
				result.addProperty("TagCode", "07010005");
				return result;
			}
		} else {
			result.addProperty("TagCode", "07010004");
			return result;
		}
		if (appIdje != null && !appIdje.isJsonNull() && !appIdje.getAsString().trim().isEmpty()) {
			try {
				appId = appIdje.getAsInt();
			} catch (Exception e) {
				appId = AppIdEnum.AMUSEMENT;
			}
		}
		if (channelje != null && !channelje.isJsonNull() && !channelje.getAsString().trim().isEmpty()) {
			try {
				channel = channelje.getAsInt();
			} catch (Exception e) {
				channel = AppChannelEnum.KK;
			}
		}
		
		try {
			note = CommonUtil.getJsonParamString(jsonObject, "note", null, null, 1, 500);
		} catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch(Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}


//		boolean flag = com.melot.kktv.service.GeneralService.feedback(userId, content, platform, appId, channel, note);
		FeedbackInfoService feedbackInfoService = (FeedbackInfoService)MelotBeanFactory.getBean("feedbackInfoService");
		boolean flag = feedbackInfoService.feedbackNew(userId, content, platform, appId, channel, note);
		if (flag) {
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		} else {
			result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
		}
		
		return result;
	}
	
	/**
	 * 上传用户设备信息接口(10007003)
	 * 
	 * @param jsonObject 请求对象
	 * @return 登录结果
	 */
	public JsonObject addUserDeviceInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		JsonObject result = new JsonObject();

		JsonElement userIdje = jsonObject.get("userId");
		JsonElement imeije = jsonObject.get("imei");
		JsonElement modelje = jsonObject.get("model");
		JsonElement screenWidthje = jsonObject.get("screenWidth");
		JsonElement screenHeightje = jsonObject.get("screenHeight");
		JsonElement osReleaseje = jsonObject.get("release");
		JsonElement osVerje = jsonObject.get("osVer");
		JsonElement osTypeje = jsonObject.get("platform");
		JsonElement ipVersionje = jsonObject.get("ipVersion");
		JsonElement ipChannelje = jsonObject.get("ipSource");
		JsonElement macje = jsonObject.get("mac");
		JsonElement appIdje = jsonObject.get("a");
		JsonElement deviceUIdje = jsonObject.get("deviceUId");
		JsonElement brand = jsonObject.get("brand");

		OtherLoginService otherLoginService = (OtherLoginService) MelotBeanFactory.getBean("otherLoginService");
		
		JsonObject mobileDeviceJsonObj = new JsonObject();
		JsonObject installPackJsonObj = new JsonObject();
		
		boolean mobileDeviceFlag = true;
		
		// 验证参数
		try {
			if (userIdje == null || userIdje.getAsInt() <= 0) {
				result.addProperty("TagCode", "07030001");
				return result;
			}
			mobileDeviceJsonObj.addProperty("userId", userIdje.getAsInt());
			installPackJsonObj.addProperty("userId", userIdje.getAsInt());
		} catch (Exception e) {
			result.addProperty("TagCode", "07030001");
			return result;
		}
		try {
			if (osTypeje == null || osTypeje.getAsInt() <= 0) {
				result.addProperty("TagCode", "07030006");
				return result;
			}
			mobileDeviceJsonObj.addProperty("osType", osTypeje.getAsInt());
			installPackJsonObj.addProperty("platform", osTypeje.getAsInt());
		} catch (Exception e) {
			result.addProperty("TagCode", "07030006");
			return result;
		}
		if (imeije == null || imeije.getAsString().isEmpty()) {
//			result.addProperty("TagCode", "07030002");
//			return result;
		    
		    mobileDeviceFlag = false;
		} else {
		    mobileDeviceJsonObj.addProperty("imei", imeije.getAsString());
		}
		if (modelje == null || modelje.getAsString().isEmpty()) {
//			result.addProperty("TagCode", "07030003");
//			return result;
		    
		    mobileDeviceFlag = false;
		} else {
		    mobileDeviceJsonObj.addProperty("model", modelje.getAsString());
		}
		if (screenWidthje == null || screenWidthje.getAsString().isEmpty()) {
//			result.addProperty("TagCode", "07030004");
//			return result;
            
            mobileDeviceFlag = false;
		} else {
		    mobileDeviceJsonObj.addProperty("screenWidth", screenWidthje.getAsString());
		}
		if (screenHeightje == null || screenHeightje.getAsString().isEmpty()) {
//			result.addProperty("TagCode", "07030005");
//			return result;
            
            mobileDeviceFlag = false;
		} else {
		    mobileDeviceJsonObj.addProperty("screenHeight", screenHeightje.getAsString());
		}
		if (osReleaseje == null || osReleaseje.getAsString().isEmpty()) {
			if (osVerje == null || osVerje.getAsString().isEmpty()) {
//				result.addProperty("TagCode", "07030007");
//				return result;
	            
	            mobileDeviceFlag = false;
			} else {
				mobileDeviceJsonObj.addProperty("osRelease", osVerje.getAsString());
			}
		} else {
			mobileDeviceJsonObj.addProperty("osRelease", osReleaseje.getAsString());
		}
		if (macje != null && !macje.getAsString().isEmpty()) {
			mobileDeviceJsonObj.addProperty("mac", macje.getAsString());
		}
		if (brand != null && !brand.getAsString().isEmpty()) {
			mobileDeviceJsonObj.addProperty("brand", brand.getAsString());
		}
		
		try {
			if (ipVersionje == null || ipVersionje.getAsInt() <= 0) {
				result.addProperty("TagCode", "07030008");
				return result;
			}
			installPackJsonObj.addProperty("ipVersion", ipVersionje.getAsInt());
		} catch (Exception e) {
			result.addProperty("TagCode", "07030008");
			return result;
		}
		try {
			if (ipChannelje == null || ipChannelje.getAsInt() <= 0) {
				result.addProperty("TagCode", "07030009");
				return result;
			}
			installPackJsonObj.addProperty("ipChannel", ipChannelje.getAsInt());
		} catch (Exception e) {
			result.addProperty("TagCode", "07030009");
			return result;
		}
		
		int appId = AppIdEnum.AMUSEMENT;
		try {
			appId = appIdje.getAsInt();
		} catch (Exception e) {
			appId = AppIdEnum.AMUSEMENT;
		}
		
		String deviceUId = "";
		try {
            if (deviceUIdje != null && !deviceUIdje.getAsString().isEmpty()) {
                deviceUId = deviceUIdje.getAsString();
                mobileDeviceJsonObj.addProperty("deviceUId", deviceUId);
            }
        } catch (Exception e) {
        }
		
		Date currentDate = new Date();
		mobileDeviceJsonObj.addProperty("dtime", currentDate.getTime());
		installPackJsonObj.addProperty("dtime", currentDate.getTime());

		otherLoginService.addInstallPack(toInstallPack(installPackJsonObj.toString()));
		
		if (mobileDeviceFlag) {
		    otherLoginService.addMobileDevice(toMobileDevice(mobileDeviceJsonObj.toString()));
        }
		
		// 安装包安装日志
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("deviceUId", deviceUId);
		HadoopLogger.installLog(installPackJsonObj.get("userId").getAsInt(),
				installPackJsonObj.get("ipVersion").getAsInt(),
				installPackJsonObj.get("ipChannel").getAsInt(), currentDate, 0,
				installPackJsonObj.get("platform").getAsInt(), appId, map);
		
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
	}

	private MobileDevice toMobileDevice(String json) {
		MobileDevice mobileDevice = null;
		try {
			if (new JsonParser().parse(json).isJsonObject()) {
				JsonObject jsonObj = new JsonParser().parse(json).getAsJsonObject();
				mobileDevice = new MobileDevice();
				if (jsonObj.has("userId")) {
					mobileDevice.setUserId(jsonObj.get("userId").getAsInt());
				}
				if (jsonObj.has("imei")) {
					mobileDevice.setImei(jsonObj.get("imei").getAsString());
				}
				if (jsonObj.has("mac")) {
					mobileDevice.setMac(jsonObj.get("mac").getAsString());
				}
				if (jsonObj.has("model")) {
					mobileDevice.setModel(jsonObj.get("model").getAsString());
				}
				if (jsonObj.has("osRelease")) {
					mobileDevice.setOsRelease(jsonObj.get("osRelease").getAsString());
				}
				if (jsonObj.has("osType")) {
					mobileDevice.setOsType(jsonObj.get("osType").getAsInt());
				}
				if (jsonObj.has("screenHeight")) {
					mobileDevice.setScreenHeight(jsonObj.get("screenHeight").getAsString());
				}
				if (jsonObj.has("screenWidth")) {
					mobileDevice.setScreenWidth(jsonObj.get("screenWidth").getAsString());
				}
				if (jsonObj.has("dtime")) {
					mobileDevice.setDtime(new Date(jsonObj.get("dtime").getAsLong()));
				}
				if (jsonObj.has("deviceUId")) {
					mobileDevice.setDeviceUId(jsonObj.get("deviceUId").getAsString());
				}
				if (jsonObj.has("brand")) {
					mobileDevice.setBrand(jsonObj.get("brand").getAsString());
				}
			}
		} catch (Exception e) {
			logger.error("BackgroundAction.toMobileDevice(" + "json:" + json + ") execute exception.", e);
		}
		return mobileDevice;
	}

	private InstallPack toInstallPack(String json) {
		InstallPack installPack = null;
		try {
			if (new JsonParser().parse(json).isJsonObject()) {
				JsonObject jsonObj = new JsonParser().parse(json).getAsJsonObject();
				installPack = new InstallPack();
				if (jsonObj.has("userId")) {
					installPack.setUserId(jsonObj.get("userId").getAsInt());
				}
				if (jsonObj.has("ipChannel")) {
					installPack.setIpChannel(jsonObj.get("ipChannel").getAsInt());
				}
				if (jsonObj.has("ipVersion")) {
					installPack.setIpVersion(jsonObj.get("ipVersion").getAsInt());
				}
				if (jsonObj.has("platform")) {
					installPack.setPlatform(jsonObj.get("platform").getAsInt());
				}
				if (jsonObj.has("dtime")) {
					installPack.setDtime(jsonObj.get("dtime").getAsLong());
				}
			}
		} catch (Exception e) {
			logger.error("BackgroundAction.toInstallPack(" + "json:" + json + ") execute exception.", e);
		}
		return installPack;
	}



}

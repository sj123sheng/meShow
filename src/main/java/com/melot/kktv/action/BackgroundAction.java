package com.melot.kktv.action;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.melot.kk.module.report.service.FeedbackInfoService;
import com.melot.kktv.redis.AppStatsSource;
import com.melot.kktv.util.AppChannelEnum;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.HadoopLogger;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.sdk.core.util.MelotBeanFactory;


/**
 * 运营后台需要的接口
 * @author RC
 *
 */
public class BackgroundAction {
    
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
		
		AppStatsSource.addInstallPack(installPackJsonObj.toString());
		
		if (mobileDeviceFlag) {
		    AppStatsSource.addMobileDevice(mobileDeviceJsonObj.toString());
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

}

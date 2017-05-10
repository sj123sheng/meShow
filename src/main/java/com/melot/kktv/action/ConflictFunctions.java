package com.melot.kktv.action;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonObject;
import com.melot.kkgame.action.HallFunction;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.sdk.core.util.MelotBeanFactory;

public class ConflictFunctions {
    
	public JsonObject con20010301(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		
		JsonObject result = new JsonObject();
		
		int appId;
		try {
			appId = CommonUtil.getJsonParamInt(jsonObject, "a", 1, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
  	    } catch (CommonUtil.ErrorGetParameterException e) {
  			result.addProperty("TagCode", e.getErrCode());
  			return result;
  		} catch (Exception e) {
  			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
  			return result;
  		}
		
		if (appId == 2) {
			HallFunction hallFunctions = MelotBeanFactory.getBean("gameHallAction", HallFunction.class);
			result = hallFunctions.getHallPlateList(jsonObject, checkTag, request);
		} else {
		    HallFunctions hallFunctions = MelotBeanFactory.getBean("hallFunction", HallFunctions.class);
		    result = hallFunctions.getHallPlateList(jsonObject, checkTag, request);
		}
		
		return result;
	}
	
	public JsonObject con20010401(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		
		JsonObject result = new JsonObject();
		
		int appId;
		try {
			appId = CommonUtil.getJsonParamInt(jsonObject, "a", 1, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
  	    } catch (CommonUtil.ErrorGetParameterException e) {
  			result.addProperty("TagCode", e.getErrCode());
  			return result;
  		} catch (Exception e) {
  			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
  			return result;
  		}
		
		if (appId == 2) {
            HallFunction hallFunctions = MelotBeanFactory.getBean("gameHallAction", HallFunction.class);
            result = hallFunctions.getActorNearby(jsonObject, checkTag, request);
		} else {
		    NewsFunctions newsFunction = MelotBeanFactory.getBean("newsFunction", NewsFunctions.class);
		    try {
		        result = newsFunction.getUserNewsListNew(jsonObject, checkTag, request);
		    } catch (Exception e) {
		    }
		}
		
		return result;
	}
	
	public JsonObject con10006001(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		
		JsonObject result = new JsonObject();
		
		int appId;
		long startTime;
		try {
			appId = CommonUtil.getJsonParamInt(jsonObject, "a", 1, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
			startTime = CommonUtil.getJsonParamLong(jsonObject, "startTime", -1, null, 0, Integer.MAX_VALUE);
  	    } catch (CommonUtil.ErrorGetParameterException e) {
  			result.addProperty("TagCode", e.getErrCode());
  			return result;
  		} catch (Exception e) {
  			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
  			return result;
  		}
		
		com.melot.kkcx.functions.ProfileFunctions profileFunctions = MelotBeanFactory.getBean("profileFunction", com.melot.kkcx.functions.ProfileFunctions.class);
		if (appId == 2 && startTime == -1) {
			result = profileFunctions.updateUserInfo(jsonObject, checkTag, request);
		} else {
		    result = profileFunctions.getUserRsvGiftList(jsonObject, checkTag, request);
		}
		
		return result;
	}
}

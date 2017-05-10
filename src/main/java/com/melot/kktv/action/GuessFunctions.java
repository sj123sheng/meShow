package com.melot.kktv.action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.kktv.model.GuessInfo;
import com.melot.kktv.model.UserGuessInfo;
import com.melot.kkcx.service.GuessService;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.TagCodeEnum;

public class GuessFunctions {
	
	/**
	 * 用户获取指定个数的开放竞猜的列表
	 * @param jsonObject
	 * @param checkTag
	 * @return
	 * @throws Exception
	 */
	public JsonObject getGuessList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
	    // 该接口需要验证token,未验证的返回错误码
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
	    
		int platform = 0;
		String topic = null;
		int count = 0;
		int type = 0;
		int userId = 0;
		//parse the parameters
		try{
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, PlatformEnum.WEB, PlatformEnum.IPAD);
			topic = CommonUtil.getJsonParamString(jsonObject, "topic", null, "40010101", 1, 20);
			count = CommonUtil.getJsonParamInt(jsonObject, "count", 0, "40010102", 1, Integer.MAX_VALUE);
			type = CommonUtil.getJsonParamInt(jsonObject, "type", 0, "40010103", 1, Integer.MAX_VALUE);
			
			// if has param userId & token, check user token
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
		}catch(CommonUtil.ErrorGetParameterException e){
			result.addProperty("TagCode", e.getErrCode());
			return result;
		}catch(Exception e){
			result.addProperty("TagCode", "40010104");
			return result;
		}
		
		//call the real function
		List<GuessInfo> guessList = GuessService.getGuessList(userId, topic, count, type, platform);
		if (guessList!=null && guessList.size()>0) {
			result.add("guessList", new JsonParser().parse(new Gson().toJson(guessList)).getAsJsonArray());
			result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
		}
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
	}
	
	/**
	 * 用户下注一个竞猜选项(40010002)
	 * @param jsonObject
	 * @param checkTag
	 * @return
	 * @throws Exception
	 *//*
	public JsonObject betGuessOption(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		int userId = 0;
		int guessId = 0;
		int optionId = 0;
		int showMoney = 0;
		//parse the parameters
		JsonObject result = new JsonObject();
		try{
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "40010201", 1, Integer.MAX_VALUE);
			guessId = CommonUtil.getJsonParamInt(jsonObject, "guessId", 0, "40010201", 1, Integer.MAX_VALUE);
			optionId = CommonUtil.getJsonParamInt(jsonObject, "optionId", 0, "40010201", 1, Integer.MAX_VALUE);
			showMoney = CommonUtil.getJsonParamInt(jsonObject, "showMoney", 0, "40010201", 0, Integer.MAX_VALUE);
		}catch(CommonUtil.ErrorGetParameterException e){
			result.addProperty("TagCode", e.getErrCode());
			return result;
		}catch(Exception e){
			result.addProperty("TagCode", "40010207");
			return result;
		}
		
		//call the real function
		int iRet = GuessService.betGuessOption(userId, guessId, optionId, showMoney);
		result.addProperty("TagCode", String.format("%08d", iRet));
		return result;
	}*/
	
	/**
	 * 获取用户的竞猜列表(40010003)
	 * @param jsonObject
	 * @param checkTag
	 * @return
	 * @throws Exception
	 */
	public JsonObject getUserGuessList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		int userId = 0;
		int platform = 0;
		
		//parse the parameters
		JsonObject result = new JsonObject();
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "40010301", 1, Integer.MAX_VALUE);
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, PlatformEnum.WEB, PlatformEnum.IPAD);
		} catch(CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch(Exception e) {
			result.addProperty("TagCode", "40010302");
			return result;
		}
		
		//call the real function
		List<UserGuessInfo> userGuessList = GuessService.getUserGuessList(userId, platform);
		if (userGuessList!=null && userGuessList.size()>0) {
			result.add("userGuessList", new JsonParser().parse(new Gson().toJson(userGuessList)).getAsJsonArray());
			result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
		}
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
	}
	
}

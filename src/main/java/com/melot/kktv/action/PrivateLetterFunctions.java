package com.melot.kktv.action;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.kktv.util.CommonUtil.ErrorGetParameterException;
import com.melot.letter.driver.domain.PrivateLetterSysConfig;
import com.melot.letter.driver.service.PrivateLetterService;
import com.melot.sdk.core.util.MelotBeanFactory;


/**
 * Title: PrivateLetterFunctions
 * Description: 用户私信配置API接口
 * @author  董毅<a href="mailto:yi.dong@melot.cn">
 * @version V1.0
 * @since 2016-12-15 17:36:53 
 */
public class PrivateLetterFunctions {
	
	private Logger logger = Logger.getLogger(PrivateLetterFunctions.class);
	
	private JsonParser jsonParser = new JsonParser();
	
	private static final String TOP_SESSION_KEY = "topSession";
	
	private static final String GENERAL_SESSION_KEY = "generalSession";
	
	/**
	 * 设置用户私信配置(55001001) ok
	 * @param jsonObject
	 * @return
	 */
	public JsonObject setPrivateLetterConfig(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		
		// 定义使用的参数
		int userId = 0;
		int receiveType = 0;
		int richLevel = 0;
		int actorLevel = 0;
		// 定义返回结果
		JsonObject result = new JsonObject();
		
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		// 解析参数
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
			receiveType = CommonUtil.getJsonParamInt(jsonObject, "receiveType", 1, "55100001", 0, Integer.MAX_VALUE);
			richLevel = CommonUtil.getJsonParamInt(jsonObject, "richLevel", 1, null, 0, Integer.MAX_VALUE);
			actorLevel = CommonUtil.getJsonParamInt(jsonObject, "actorLevel", 1, null, 0, Integer.MAX_VALUE);
		} catch (ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		}
		
		boolean status = true;
		try{
			// 调用模块接口
			PrivateLetterService privateLetterService = (PrivateLetterService)MelotBeanFactory.getBean("privateLetterService");
			status = privateLetterService.setPrivateLetterConfig(userId, receiveType, richLevel, actorLevel);
		} catch (Exception e) {
			logger.error("PrivateLetterService.setPrivateLetterConfig is errer!", e);
		}
		// 返回结果
		result.addProperty("TagCode", status?TagCodeEnum.SUCCESS:"55100002");
		return result;
		
	}
	
	
	/**
	 * 获取用户私信配置(55001002) ok
	 * @param jsonObject
	 * @return
	 */
	public JsonObject getPrivateLetterConfig(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		
		// 定义使用的参数
		int userId = 0;
		// 定义返回结果
		JsonObject result = new JsonObject();
		
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		// 解析参数
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
		} catch (ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		}
		
		// 调用模块接口
		PrivateLetterService privateLetterService = (PrivateLetterService)MelotBeanFactory.getBean("privateLetterService");
		
		PrivateLetterSysConfig privateLetterSysConfig = privateLetterService.getPrivateLetterSysConfig(userId);
		if (privateLetterSysConfig!=null) {
			result = jsonParser.parse(new Gson().toJson(privateLetterSysConfig)).getAsJsonObject();
		}
		
		// 返回结果
		result.addProperty("TagCode", result.has("userId")?TagCodeEnum.SUCCESS:"55100003");
		return result;
		
	}
	
	/**
	 * 获取用户置顶私信会话列表(55001003) ok
	 * @param jsonObject
	 * @return
	 */
	public JsonObject getPrivateSessionList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		
		// 定义使用的参数
		int userId = 0;
		// 定义返回结果
		JsonObject result = new JsonObject();
		
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		// 解析参数
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
		} catch (ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		}
		
		Map<String,JsonArray> mapSession = getPrivateSession(userId);
		// 置顶会话列表
		if (mapSession.containsKey(TOP_SESSION_KEY)) {
			result.add("sessionList",mapSession.get(TOP_SESSION_KEY));
		}
		// 普通会话列表
		if (mapSession.containsKey(GENERAL_SESSION_KEY)) {
			result.add("generalSessionList",mapSession.get(GENERAL_SESSION_KEY));
		}
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
	}
	
	/**
	 * 设置用户置顶私信会话(55001004) ok
	 * @param jsonObject
	 * @return
	 */
	public JsonObject setTopPrivateSession(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		
		// 定义使用的参数
		int userId = 0;
		int topUserId = 0;
		// 定义返回结果
		JsonObject result = new JsonObject();
		
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		// 解析参数
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
			topUserId = CommonUtil.getJsonParamInt(jsonObject, "topUserId", 0, TagCodeEnum.TOP_USERID_MISSING, 1, Integer.MAX_VALUE);
		} catch (ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		}
		
		// 调用模块接口
        PrivateLetterService privateLetterService = (PrivateLetterService)MelotBeanFactory.getBean("privateLetterService");
        privateLetterService.setTopPrivateSession(userId, topUserId);
        
		result.addProperty("TagCode",TagCodeEnum.SUCCESS);
		return result;
		
	}
	
	/**
	 * 获取私信会话列表
	 * @param userId
	 * @return
	 */
	private Map<String,JsonArray> getPrivateSession(Integer userId){
		Map<String, JsonArray> map = new HashMap<String, JsonArray>();
		try {
		    
		 // 调用模块接口
	        PrivateLetterService privateLetterService = (PrivateLetterService)MelotBeanFactory.getBean("privateLetterService");
		    
			LinkedList<Integer> toplist  = privateLetterService.getTopSession(userId,100);
			if (toplist != null && toplist.size() > 0) {
				map.put(TOP_SESSION_KEY, new Gson().toJsonTree(toplist).getAsJsonArray());
			}
			LinkedList<Integer> generalList  = privateLetterService.getGeneralSession(userId, 500);
			if (generalList != null && generalList.size() > 0) {
				map.put(GENERAL_SESSION_KEY, new Gson().toJsonTree(generalList).getAsJsonArray());
			}
		} catch (Exception e) {
			logger.error("PrivateLetterFunctions.getPrivateSession is error:"+e);
		}
		return map;
	}
	
	/**
	 * 移除用户置顶私信会话(55001005) 删除置顶会话，并转移到普通会话
	 * @param jsonObject
	 * @return
	 */
	public JsonObject removeTopPrivateSession(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		
		// 定义使用的参数
		int userId = 0;
		int topUserId = 0;
		// 定义返回结果
		JsonObject result = new JsonObject();
		
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		// 解析参数
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
			topUserId = CommonUtil.getJsonParamInt(jsonObject, "topUserId", 0, TagCodeEnum.TOP_USERID_MISSING, 1, Integer.MAX_VALUE);
		} catch (ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		}
		 // 调用模块接口
        PrivateLetterService privateLetterService = (PrivateLetterService)MelotBeanFactory.getBean("privateLetterService");
		
        privateLetterService.removeTopPrivateSession(userId, topUserId);
		
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
		
	}
	
	/**
	 * 刷新私信会话(55001006) ok
	 * @param jsonObject
	 * @return
	 */
	public JsonObject refreshPrivateSession(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		
		// 定义使用的参数
		int userId = 0;
		int toUserId = 0;
		// 定义返回结果
		JsonObject result = new JsonObject();
		
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		// 解析参数
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
			toUserId = CommonUtil.getJsonParamInt(jsonObject, "toUserId", 0, TagCodeEnum.TOP_USERID_MISSING, 1, Integer.MAX_VALUE);
		} catch (ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		}
		
		   // 调用模块接口
        PrivateLetterService privateLetterService = (PrivateLetterService)MelotBeanFactory.getBean("privateLetterService");
        
        privateLetterService.refreshPrivateSession(userId, toUserId);
		
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
		
	}
	
	/**
	 * 移除用户私信会话(55001007) 直接删除置顶会话，和普通会话
	 * @param jsonObject
	 * @return
	 */
	public JsonObject removePrivateSession(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		
		// 定义使用的参数
		int userId = 0;
		int toUserId = 0;
		// 定义返回结果
		JsonObject result = new JsonObject();
		
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		// 解析参数
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
			toUserId = CommonUtil.getJsonParamInt(jsonObject, "toUserId", 0, TagCodeEnum.TOP_USERID_MISSING, 1, Integer.MAX_VALUE);
		} catch (ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		}
		
		// 调用模块接口
        PrivateLetterService privateLetterService = (PrivateLetterService)MelotBeanFactory.getBean("privateLetterService");
        
        privateLetterService.removePrivateSession(userId, toUserId);
		
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
		
	}
}

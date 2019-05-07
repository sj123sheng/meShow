package com.melot.kktv.action;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.reflect.TypeToken;
import com.melot.kk.config.api.constant.BootMessageTypeEnum;
import com.melot.kk.config.api.domain.ConfBootMessageDTO;
import com.melot.kk.config.api.service.ConfigInfoService;
import com.melot.kktv.redis.HotDataSource;
import com.melot.kktv.util.StringUtil;
import com.melot.letter.driver.domain.HistPrivateLetter;
import com.melot.letter.driver.domain.HistPrivateLetterWarn;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.kkcore.actor.api.RoomInfo;
import com.melot.kkcore.actor.service.ActorService;
import com.melot.kktv.base.CommonStateCode;
import com.melot.kktv.base.Result;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ParameterKeys;
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

	private static final String IM_AUTOSEND_ACTOR_WHITELIST_KEY = "im_autosend_actor_whitelist";

	private static final String IM_AUTOSEND_MSGLIST_KEY = "im_autosend_msglist_%s_%s_%s";

	private static final String IM_AUTOSEND_TIME_KEY = "im_autosend_time_%s_%s";
	
	/**
	 * 设置用户私信配置(55010001) ok
	 * @param jsonObject
	 * @return
	 */
	public JsonObject setPrivateLetterConfig(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		
//		// 定义使用的参数
//		int userId = 0;
//		int receiveType = 0;
//		int richLevel = 0;
//		int actorLevel = 0;
//		// 定义返回结果
		JsonObject result = new JsonObject();
//
//		// 该接口需要验证token,未验证的返回错误码
//		if (!checkTag) {
//			result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
//			return result;
//		}
//
//		// 解析参数
//		try {
//			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
//			receiveType = CommonUtil.getJsonParamInt(jsonObject, "receiveType", 1, "55100001", 0, Integer.MAX_VALUE);
//			richLevel = CommonUtil.getJsonParamInt(jsonObject, "richLevel", 1, null, 0, Integer.MAX_VALUE);
//			actorLevel = CommonUtil.getJsonParamInt(jsonObject, "actorLevel", 1, null, 0, Integer.MAX_VALUE);
//		} catch (ErrorGetParameterException e) {
//			result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
//			return result;
//		}
//
//		boolean status = true;
//		try{
//			// 调用模块接口
//			PrivateLetterService privateLetterService = (PrivateLetterService)MelotBeanFactory.getBean("privateLetterService");
//			status = privateLetterService.setPrivateLetterConfig(userId, receiveType, richLevel, actorLevel);
//		} catch (Exception e) {
//			logger.error("PrivateLetterService.setPrivateLetterConfig is errer!", e);
//		}
		// 返回结果
		result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
		return result;
		
	}
	
	
	/**
	 * 获取用户私信配置(55010002) ok
	 * @param jsonObject
	 * @return
	 */
	public JsonObject getPrivateLetterConfig(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		
//		// 定义使用的参数
//		int userId = 0;
//		// 定义返回结果
		JsonObject result = new JsonObject();
//
//		// 该接口需要验证token,未验证的返回错误码
//		if (!checkTag) {
//			result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
//			return result;
//		}
//
//		// 解析参数
//		try {
//			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
//		} catch (ErrorGetParameterException e) {
//			result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
//			return result;
//		}
//
//		// 调用模块接口
//		PrivateLetterService privateLetterService = (PrivateLetterService)MelotBeanFactory.getBean("privateLetterService");
//
//		PrivateLetterSysConfig privateLetterSysConfig = privateLetterService.getPrivateLetterSysConfig(userId);
//		if (privateLetterSysConfig!=null) {
//			result = jsonParser.parse(new Gson().toJson(privateLetterSysConfig)).getAsJsonObject();
//		}
		
		// 返回结果
		result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
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
			result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		// 解析参数
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
		} catch (ErrorGetParameterException e) {
			result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
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
		result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
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
			result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		// 解析参数
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
			topUserId = CommonUtil.getJsonParamInt(jsonObject, "topUserId", 0, TagCodeEnum.TOP_USERID_MISSING, 1, Integer.MAX_VALUE);
		} catch (ErrorGetParameterException e) {
			result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
			return result;
		}
		
		// 调用模块接口
        PrivateLetterService privateLetterService = (PrivateLetterService)MelotBeanFactory.getBean("privateLetterService");
        privateLetterService.setTopPrivateSession(userId, topUserId);
        
		result.addProperty(ParameterKeys.TAG_CODE,TagCodeEnum.SUCCESS);
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
			result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		// 解析参数
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
			topUserId = CommonUtil.getJsonParamInt(jsonObject, "topUserId", 0, TagCodeEnum.TOP_USERID_MISSING, 1, Integer.MAX_VALUE);
		} catch (ErrorGetParameterException e) {
			result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
			return result;
		}
		 // 调用模块接口
        PrivateLetterService privateLetterService = (PrivateLetterService)MelotBeanFactory.getBean("privateLetterService");
		
        privateLetterService.removeTopPrivateSession(userId, topUserId);
		
		result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
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
			result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		// 解析参数
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
			toUserId = CommonUtil.getJsonParamInt(jsonObject, "toUserId", 0, TagCodeEnum.TOP_USERID_MISSING, 1, Integer.MAX_VALUE);
		} catch (ErrorGetParameterException e) {
			result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
			return result;
		}
		
		   // 调用模块接口
        PrivateLetterService privateLetterService = (PrivateLetterService)MelotBeanFactory.getBean("privateLetterService");
        
        privateLetterService.refreshPrivateSession(userId, toUserId);
		
		result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
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
			result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		// 解析参数
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
			toUserId = CommonUtil.getJsonParamInt(jsonObject, "toUserId", 0, TagCodeEnum.TOP_USERID_MISSING, 1, Integer.MAX_VALUE);
		} catch (ErrorGetParameterException e) {
			result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
			return result;
		}
		
		// 调用模块接口
        PrivateLetterService privateLetterService = (PrivateLetterService)MelotBeanFactory.getBean("privateLetterService");
        
        privateLetterService.removePrivateSession(userId, toUserId);
		
		result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
		return result;
	}
	
    /**
     * 主播群发IM消息【51110101】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject massSendIMInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
            
            // 定义使用的参数
            int userId = 0;
            String text = "";
            String image = "";
            // 定义返回结果
            JsonObject result = new JsonObject();
            
            // 该接口需要验证token,未验证的返回错误码
            if (!checkTag) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
                return result;
            }
            
            try {
                userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
                text = CommonUtil.getJsonParamString(jsonObject, "text", null, "5111010101", 0, Integer.MAX_VALUE);
                image = CommonUtil.getJsonParamString(jsonObject, "image", null, null, 0, Integer.MAX_VALUE);
            } catch (ErrorGetParameterException e) {
                result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
                return result;
            }
            
            if (com.melot.kkcx.service.GeneralService.hasSensitiveWords(userId, text)) {
                result.addProperty(ParameterKeys.TAG_CODE, "5111010103");
                return result;
            }
            
            try {
                ActorService actorService = (ActorService) MelotBeanFactory.getBean("actorService");
                RoomInfo roomInfo = actorService.getRoomInfoById(userId);
                if (roomInfo == null || roomInfo.getLiveEndTime() != null) {
                    result.addProperty(ParameterKeys.TAG_CODE, "5111010104");
                    return result;
                }
            } catch (Exception e) {
                logger.error(String.format("actorService.getRoomInfoById(%s)", userId), e);
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            try {
                PrivateLetterService privateLetterService = (PrivateLetterService) MelotBeanFactory.getBean("privateLetterService");
                Result<Boolean> massSendIMInfoResult = privateLetterService.massSendIMInfo(userId, text, image);
                if (massSendIMInfoResult == null || CommonStateCode.FAIL.equals(massSendIMInfoResult.getCode())) {
                    result.addProperty(ParameterKeys.TAG_CODE, "5111010102");
                } else {
                    result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
                }
            } catch (Exception e) {
                logger.error(String.format("Module Error privateLetterService.massSendIMInfo(%s, %s, %s)", userId, text, image), e);
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            }
            
            return result;
    }


    /**
     * 获取主播倒计时时间【51110102】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getNextCountdown(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
            
            // 定义使用的参数
            int userId = 0;
            // 定义返回结果
            JsonObject result = new JsonObject();
            
            // 该接口需要验证token,未验证的返回错误码
            if (!checkTag) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
                return result;
            }
            
            try {
                userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            } catch (ErrorGetParameterException e) {
                result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
                return result;
            }
            
            try {
                PrivateLetterService privateLetterService = (PrivateLetterService) MelotBeanFactory.getBean("privateLetterService");
                Result<Long> countdownResult = privateLetterService.getNextSendTime(userId);
                if (countdownResult == null || CommonStateCode.FAIL.equals(countdownResult.getCode())) {
                    result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                } else {
                    long countdown = countdownResult.getData() / 1000;
                    result.addProperty("countdown", countdown < 0 ? 0 : countdown);
                    result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
                }
            } catch (Exception e) {
                logger.error(String.format("Module Error privateLetterService.getNextSendTime(%s)", userId), e);
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            }
            
            return result;
    }

	public JsonObject sendIMToNewUser(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		JsonObject result = new JsonObject();

		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
			return result;
		}

		int userId,actorId,num,gender;
		String deviceUId;
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
			actorId = CommonUtil.getJsonParamInt(jsonObject, "actorId", 0, "5111010301", 1, Integer.MAX_VALUE);
			num = CommonUtil.getJsonParamInt(jsonObject, "num", 1, null, 1, Integer.MAX_VALUE);
			gender = CommonUtil.getJsonParamInt(jsonObject, "actorGender", 0, null, 0, Integer.MAX_VALUE);
			deviceUId = CommonUtil.getJsonParamString(jsonObject, "deviceUId", null, "5111010304", 0, Integer.MAX_VALUE);
		} catch(Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}

		try{
			if(checkWhiteList(actorId)){
				if(num > 1){
					num = 2;
				}
				String msg = getIMMessage(num,gender,deviceUId);
				if(checkTime(deviceUId,num)) {
					if (!StringUtil.strIsNull(msg)) {
						PrivateLetterService privateLetterService = (PrivateLetterService) MelotBeanFactory.getBean("privateLetterService");
						privateLetterService.sendOneIMInfo(actorId, userId, msg, "");
						result.addProperty("TagCode", TagCodeEnum.SUCCESS);
					}
					else{
						result.addProperty("TagCode", "5111010305");

					}
				}
				else {
					result.addProperty("TagCode", "5111010302");
				}
			}
			else{
				result.addProperty("TagCode", "5111010303");
			}

		}
		catch (Exception e){
			logger.error("Error sendIMToNewUser()", e);
			result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
		}
		return result;
	}

	private boolean checkWhiteList(int actorId) {
    	if(!HotDataSource.exists(IM_AUTOSEND_ACTOR_WHITELIST_KEY)){
			ConfigInfoService configInfoService = (ConfigInfoService) MelotBeanFactory.getBean("configInfoService");
			List<String> whiteList = configInfoService.getWhiteListByType(2);
			HotDataSource.sadd(IM_AUTOSEND_ACTOR_WHITELIST_KEY,5*3600,whiteList.toArray(new String[whiteList.size()]));
		}
		return HotDataSource.hasTempData(IM_AUTOSEND_ACTOR_WHITELIST_KEY,actorId+"");
	}

	public boolean checkTime(String deviceUId,int num){
		String key = String.format(IM_AUTOSEND_TIME_KEY,deviceUId,num);
		return HotDataSource.incTempDataString(key,24*3600)<=num*3;
	}

	private String getIMMessage(int num,int gender,String deviceUId){
    	String key = String.format(IM_AUTOSEND_MSGLIST_KEY,deviceUId,num,gender);
		if(!HotDataSource.exists(key)){
			ConfigInfoService configInfoService = (ConfigInfoService) MelotBeanFactory.getBean("configInfoService");
			List<ConfBootMessageDTO> msgList = configInfoService.listConfBootMessagesByType(BootMessageTypeEnum.PRIVATE_LETTER,num,gender);
			if(msgList !=null && msgList.size()>=0){
				String msg = msgList.get(0).getContent();
				msgList.remove(0);
				HotDataSource.setTempDataString(key,new Gson().toJson(msgList),24*3600);
				return msg;
			}
		}
		else {
			String msgs = HotDataSource.getTempDataString(key);
			if(!StringUtil.strIsNull(msgs)){
				List<ConfBootMessageDTO> msgList = new Gson().fromJson(msgs, new TypeToken<List<ConfBootMessageDTO>>(){}.getType());
				if(msgList !=null && msgList.size()>0){
					String msg = msgList.get(0).getContent();
					msgList.remove(0);
					HotDataSource.setTempDataString(key,new Gson().toJson(msgList),24*3600);
					return msg;
				}
			}
		}
		return null;
	}


}

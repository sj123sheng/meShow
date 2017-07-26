package com.melot.kkcx.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.melot.chat.domain.PrivateLetter;
import com.melot.chat.service.PrivateChatAnalyzerService;
import com.melot.client.api.TimSystemService;
import com.melot.game.config.sdk.utils.StringUtils;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcx.redis.PushMsgSource;
import com.melot.kkcx.redis.UserRelationSource;
import com.melot.kkcx.util.Constant;
import com.melot.kktv.redis.HotDataSource;
import com.melot.kktv.util.CollectionUtils;
import com.melot.kktv.util.HttpClient;
import com.melot.letter.driver.service.PrivateLetterService;
import com.melot.module.ModuleService;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * Title: TimService
 * <p>
 * Description: 腾讯云服务
 * </p>
 * 
 * @author 林端端<a href="mailto:duanduan.lin@melot.cn">
 * @version V1.0
 * @since 2016年10月14日 下午5:11:45
 */
public class TimService {
    
    private static Logger logger = Logger.getLogger(TimService.class);
    
    /**
     * 腾讯云账号唯一标示前缀
     */
    public static final String TIM_IDENTIFIER_PREFIX = "bang_";
    
    /**
     * 腾讯云IM账号注册
     * 
     * @param userId
     * @param nickname
     * @return
     */
    public static boolean registerTim(String userId, String nickname) {
        try {
            String isTimRegister = HotDataSource.getHotFieldValue(userId, "isTimRegister");
            if (StringUtils.isEmpty(isTimRegister)) {
                String identifier = TIM_IDENTIFIER_PREFIX + userId;
                TimSystemService timService = (TimSystemService) ModuleService.getService("TimSystemService");
                String ret = timService.accountImport(identifier, nickname, "");
                if (ret.equalsIgnoreCase("ok")) {
                    HotDataSource.setHotFieldValue(userId, "isTimRegister", "1");
//                    TimMsgList timMsgList = MelotBeanFactory.getBean("timMsgList", TimMsgList.class);
//                    TimMsg timMsg = timMsgList.getList().get("register");
//                    SendTimMsgService.add(identifier, timMsg.getAdmin(), timMsg.getMsg());
                    return true;
                } else {
                    logger.error("Fail to call im interface, userId " + userId + ", ret " + ret);
                }
            } else {
                return true;
            }
        } catch (Exception e) {
            logger.error("Fail to call im interface to register, userId " + userId, e);
        }
        return false;
    }

    /**
     * 获取腾讯云IM接口调用签名
     * 
     * @param userId
     * @return
     */
    public static String getTimSig(int userId) {
        String sig = null;
        try {
            String identifier = TIM_IDENTIFIER_PREFIX + userId;
            TimSystemService timService = (TimSystemService) ModuleService.getService("TimSystemService");
            sig = timService.getUserSig(identifier);
        } catch (Exception e) {
            logger.error("Fail to call im interface to get signature, userId " + userId, e);
        }
        return sig;
    }
    
    /**
	 * 推送给客户户端
	 * 
	 * @param appId
	 * @param fromAccount
	 * @param toAccount
	 * @param type
	 * @param time
	 * @param text
	 */
	public static void pushMsg(int appId, int fromAccount, int toAccount, int type, Date time, String text) {
		if (StringUtils.isEmpty(text) || fromAccount <= 0 || toAccount <= 0) {
			return;
		}
		UserProfile userProfile = com.melot.kktv.service.UserService.getUserInfoV2(fromAccount);
		if (userProfile != null) {
			Map<String, Object> map = getMsgMap(appId, fromAccount, toAccount, userProfile.getNickName(), type, time, text);
			if (!CollectionUtils.isEmpty(map)) {
				Map<Integer, Integer> filter = getGroupFilter();
				if (!filter.containsKey(fromAccount)) {
					boolean isOnline = isOnline(toAccount);
					if(userProfile.getIsActor() == 1){
						if(!isOnline){
							PushMsgSource.lpush(map);
						}
						logger.info("pushMsg toAccount:"+toAccount+",fromAccount:"+fromAccount+",isOnline:"+isOnline+",isActor:"+userProfile.getIsActor()+",msg:"+new Gson().toJson(map)+"");
					}else{
						boolean fromAccountFollow = UserRelationSource.isFollowed(String.valueOf(fromAccount), String.valueOf(toAccount));
						boolean toAccountFollow = UserRelationSource.isFollowed(String.valueOf(toAccount), String.valueOf(fromAccount));
						if (fromAccountFollow && toAccountFollow && !isOnline) {
							PushMsgSource.lpush(map);
						} else if (toAccountFollow && !isOnline) {
							PushMsgSource.lpush(map);
						}
						logger.info("pushMsg,toAccount:" + toAccount + ",fromAccount:" + fromAccount + ",isOnline:" + isOnline + ",fromAccountFollow:" + fromAccountFollow + ",toAccountFollow:" + toAccountFollow + ",msg:" + new Gson().toJson(map) + "");
					}
				}
			}
		}
	}

	private static boolean isOnline(int toAccount) {
		String sig = getTimSig(10000);
		String admin = "bang_10000";
		String sdkAppid = Constant.TIM_SdkAppId;
		String identifier = Constant.TIM_IDENTIFIER_PREFIX + toAccount;
		String json = "{\"To_Account\": [\"" + identifier + "\"]}";
		int randomNum = getRandomNumber(1, 9999999);
		String strURL = String.format("https://console.tim.qq.com/v4/openim/querystate?usersig=%s&identifier=%s&sdkappid=%s&random=%s&contenttype=json", sig, admin, sdkAppid, String.valueOf(randomNum));
		String result = HttpClient.doPostBody(strURL, json);
		logger.info("user online,toAccount:" + toAccount + ", admin sig:" + sig + ",result:" + result + "");
		if (!result.equalsIgnoreCase("error")) {
			String online = getUserOnLine(result);
			logger.info("user online,toAccount:" + toAccount + ", isOnline:" + online + "");
			return online.equalsIgnoreCase("Online");
		} else {
			return false;
		}
	}

	private static String getUserOnLine(String json) {
		if (StringUtils.isEmpty(json)) {
			return "Online";
		}
		Gson gson = new Gson();
		int errorCode = 0;
		String queryResult = "";
		try {
			Map<String, Object> map = gson.fromJson(json, new TypeToken<Map<String, Object>>() {
			}.getType());
			for (Entry<String, Object> entry : map.entrySet()) {
				if (entry.getKey().equalsIgnoreCase("ErrorCode")) {
					errorCode = (int) Double.parseDouble(entry.getValue().toString());
				}
				if (entry.getKey().equalsIgnoreCase("QueryResult")) {
					queryResult = entry.getValue().toString();
				}
			}
			if (errorCode == 0) {
				JsonArray array = gson.fromJson(queryResult, JsonArray.class);
				if (array != null && array.size() > 0) {
					for (JsonElement jsonElement : array) {
						Map<String, Object> resultMap = gson.fromJson(jsonElement, new TypeToken<Map<String, Object>>() {
						}.getType());
						for (Entry<String, Object> item : resultMap.entrySet()) {
							if (item.getKey().equalsIgnoreCase("State")) {
								return item.getValue().toString();
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			logger.error("Fail to getUserOnLine json:" + json + ",error", ex);
		}
		return "Online";
	}

	private static int getRandomNumber(int min, int max) {
		Random random = new Random();
		int result = random.nextInt(max) % (max - min + 1) + min;
		return result;
	}

	private static Map<String, Object> getMsgMap(int appId, int fromAccount, int toAccount, String sendName, int type, Date time, String text) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("type", type);
		map.put("appId", appId);
		map.put("stamp", time.getTime());
		map.put("from", fromAccount);
		map.put("to", toAccount);
		map.put("senderName", sendName);
		map.put("content", text);
		return map;
	}
    
	private static Map<Integer, Integer> getGroupFilter() {
		int size = 99;
		Map<Integer, Integer> map = new HashMap<Integer, Integer>(size);
		for (int i = 1; i <= size; i++) {
			map.put(i, i);
		}
		return map;
	}
	
    /**
     * 是否包含敏感字
     * @param userId
     * @param receiveId
     * @param word
     * @return true 包含 false 不包含
     */
    public static boolean hasSensitiveWords(int userId, int receiveId, String word){
	      if (word!=null && word!="") {
	          try {
	        	  
	//        	      查询是否官方号 大于等于0是 -1 不是 getUserAdminType
	        	  Integer adminType =  ProfileServices.getUserAdminType(userId);
	        	  if (adminType != null && adminType >=0 ) {
	        		  return false;
	        	  }
	        	  PrivateChatAnalyzerService  chatAnalyzerService = (PrivateChatAnalyzerService ) MelotBeanFactory.getBean("privateChatAnalyzerService");
	              if (chatAnalyzerService != null) {
	            	  PrivateLetter  privateLetter = chatAnalyzerService.checkPrivateLetter(userId, receiveId, word);
	            	  if(privateLetter != null && privateLetter.isContains()){
	            		  return true;
	            	  }
	              }
	          } catch (Exception e) {
	               logger.error("ChatAnalyzerService.checkPhrase(" + userId + ", " + word + ") execute exception.", e);
	           }
	        }
	        return false;
	  }
	
	/**
	 * 用户私信发送检测
	 * @param userId 用户ID
	 * @param roomId 直播ID
	 * @param msgType text:文本消息; image:图片消息
	 * @return
	 */
	public static String checkPrivateLetterWords(int userId, int receiveId, String msgType) {
		try {
		    PrivateLetterService privateLetterService = (PrivateLetterService) MelotBeanFactory.getBean("privateLetterService");
		    if (privateLetterService != null) {
		    	if (msgType.equalsIgnoreCase("TIMTextElem")) {
		    		msgType = "text";
		    	}else{
		    		msgType = "image";
		    	}
		        String resultCode = privateLetterService.checkSendPrivateLetter(userId, receiveId, msgType);
		        if (!StringUtils.isEmpty(resultCode)) {
		            return resultCode;
		        }
		    }
		} catch (Exception e) {
		    logger.error("ChatAnalyzerService.checkPhrase(" + userId + ", " + msgType + ") execute exception.", e);
		}
		return "0";
	}
}

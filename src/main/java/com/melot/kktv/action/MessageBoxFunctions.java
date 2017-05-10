package com.melot.kktv.action;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kkcx.service.MessageBoxServices;
import com.melot.kkcx.service.UserService;
import com.melot.kktv.model.KkAssistor;
import com.melot.kktv.model.NewsComment;
import com.melot.kktv.model.EffectiveActivity;
import com.melot.kktv.model.KkSystemNotice;
import com.melot.kktv.model.RecommendedMessage;
import com.melot.kktv.redis.HotDataSource;
import com.melot.kktv.redis.UserMessageSource;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.message.Message;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.db.SqlMapClientHelper;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.sdk.core.util.MelotBeanFactory;

public class MessageBoxFunctions {

	/** 日志记录对象 */
	private static Logger logger = Logger.getLogger(MessageBoxFunctions.class);
	
	private static final int BACCHDELETE_ROOMMESSAGE=1;
	private static final int SINGLEDELETE =2;
	
	private static ActivityMessage actMessage = new ActivityMessage();
	private static RecommendMessage recMessage = new RecommendMessage();
	
	/**
	 * 获取用户消息列表(10006101)
	 * 
	 * @param jsonObject 请求对象
	 * @return 结果字符串
	 */
	public JsonObject getUserMessageList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
	    JsonObject result = new JsonObject();
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		int userId = 0, platform = 0, perPageCount = 0, currentPage = 0, appId = 0, maxType = 0;
		try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "10006102", 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
            perPageCount = CommonUtil.getJsonParamInt(jsonObject, "perPageCount", 10, "10006104", 1, Integer.MAX_VALUE);
            currentPage = CommonUtil.getJsonParamInt(jsonObject, "curPage", 1, "10006106", 1, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 1, Integer.MAX_VALUE);
            maxType = CommonUtil.getJsonParamInt(jsonObject, "maxType", Message.MSGTYPE_ACTIVE, null, 0, Integer.MAX_VALUE);
            if(maxType > Message.MSGTYPE_RECOMMENDED) maxType = Message.MSGTYPE_RECOMMENDED;
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
		
		ArrayList<Long> lRetCount = new ArrayList<Long>();
		//获取用户消息列表
		JsonArray jsonMsgList = getUserMessageListInternal(String.valueOf(userId), perPageCount, currentPage, lRetCount, maxType, appId);
		JsonObject jsonObj = null;
		Map<Integer, String> fromMap = new HashMap<Integer, String>();
		Map<Integer, String> contextMap = new HashMap<Integer, String>();
	    //遍历用户消息列表，找出舞台消息，并根据消息中from提供的userId从hotData中找出该userId对应的nickname和portrait_path_48
		for (Iterator<JsonElement> jslt = jsonMsgList.iterator(); jslt.hasNext();){
			try {
				jsonObj = jslt.next().getAsJsonObject();
				int msgType = 0;
				int from = 0;
				int context = 0;
				try {
					msgType = jsonObj.get("msgType").getAsInt();
					from = jsonObj.get("from").getAsInt();
					JsonElement contextJe = jsonObj.get("context");
					if (contextJe != null && !contextJe.isJsonNull())
						context = contextJe.getAsInt();
				} catch (Exception e) {
					logger.error("get message type or message from  failed "+e);
				}
				if (msgType != 0 && from != 0) {
					try {
						if (msgType == Message.MSGTYPE_ROOM || msgType == Message.MSGTYPE_DYNAMIC) {

 						    if (fromMap.get(from) != null) {
						        JsonObject temp = new JsonParser().parse(fromMap.get(from)).getAsJsonObject();
                                if (temp.get("nickname") != null) {
                                    jsonObj.addProperty("nickname", temp.get("nickname").toString());
                                }
                                jsonObj.addProperty("gender", temp.get("gender").toString());
                                if (temp.get("portrait_path_128") != null) {
                                    jsonObj.addProperty("portrait_path_128", temp.get("portrait_path_128").toString());
                                }
                                if (temp.get("portrait_path_48") != null) {
                                    jsonObj.addProperty("portrait_path_48", temp.get("portrait_path_48").toString());
                                }
                            } else {
                                try {
                                    KkUserService userService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
                                    UserProfile fromUserProfile = userService.getUserProfile(from);
                                    if (fromUserProfile != null) {
                                        JsonObject temp = new JsonObject();
                                        if (fromUserProfile.getNickName() != null) {
                                            jsonObj.addProperty("nickname", fromUserProfile.getNickName());
                                            temp.addProperty("nickname", fromUserProfile.getNickName());
                                        }
                                        jsonObj.addProperty("gender", fromUserProfile.getGender());
                                        temp.addProperty("gender", fromUserProfile.getGender());
                                        if ((platform == PlatformEnum.IPHONE || platform == PlatformEnum.IPAD) && fromUserProfile.getPortrait() != null) {
                                            jsonObj.addProperty("portrait_path_128", fromUserProfile.getPortrait() + "!128");
                                            jsonObj.addProperty("portrait_path_48", fromUserProfile.getPortrait() + "!48");
                                            temp.addProperty("portrait_path_128", fromUserProfile.getPortrait() + "!128");
                                            temp.addProperty("portrait_path_48", fromUserProfile.getPortrait() + "!48");
                                        }
                                        fromMap.put(from, temp.toString());
                                    }
                                } catch (Exception e) {
                                    logger.error("fail to get KkUserService.getUserProfile, userId: " + from, e);
                                }
                            }
							if (context != 0) {
								//get the room id
	                            if (contextMap.get(context) != null) {
	                                JsonObject temp = new JsonParser().parse(contextMap.get(context)).getAsJsonObject();
	                                if (temp.get("roomname") != null) {
	                                    jsonObj.addProperty("roomname", temp.get("roomname").toString());
	                                }
	                                jsonObj.addProperty("roomgender", temp.get("roomgender").toString());
	                                if (temp.get("roomportrait_path_128") != null) {
	                                    jsonObj.addProperty("roomportrait_path_128", temp.get("roomportrait_path_128").toString());
	                                }
	                                if (temp.get("roomportrait_path_48") != null) {
	                                    jsonObj.addProperty("roomportrait_path_48", temp.get("roomportrait_path_48").toString());
	                                }
	                            } else {
	                                try {
	                                    KkUserService userService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
	                                    UserProfile contextUserProfile = userService.getUserProfile(context);
	                                    if (contextUserProfile != null) {
	                                        JsonObject temp = new JsonObject();
	                                        if (contextUserProfile.getNickName() != null) {
	                                            jsonObj.addProperty("roomname", contextUserProfile.getNickName());
	                                            temp.addProperty("roomname", contextUserProfile.getNickName());
	                                        }
	                                        jsonObj.addProperty("roomgender", contextUserProfile.getGender());
	                                        temp.addProperty("roomgender", contextUserProfile.getGender());
	                                        if ((platform == PlatformEnum.IPHONE || platform == PlatformEnum.IPAD) && contextUserProfile.getPortrait() != null) {
	                                            jsonObj.addProperty("roomportrait_path_128", contextUserProfile.getPortrait() + "!128");
	                                            jsonObj.addProperty("roomportrait_path_48", contextUserProfile.getPortrait() + "!48");
	                                            temp.addProperty("roomportrait_path_128", contextUserProfile.getPortrait() + "!128");
	                                            temp.addProperty("roomportrait_path_48", contextUserProfile.getPortrait() + "!48");
	                                        }
	                                        contextMap.put(context, temp.toString());
	                                    }
	                                } catch (Exception e) {
	                                    logger.error("fail to get KkUserService.getUserProfile, userId: " + context, e);
	                                }
	                            }
							}
						}
					} catch (Exception e) {
						logger.error("when messageType is stageMsg, get userInfo failed " + e);
					}
				}
			} catch (Exception e) {
				logger.error("message jsonObject transfer failed " + e);
			}
		}
		result.addProperty("TagCode", "00000000");
		result.add("messageList", jsonMsgList);
		result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
		if (lRetCount.size() == 1) {
			Long lCount = lRetCount.get(0);
			result.addProperty("total", lCount);
			if (lCount == 0) {
				//we reset the total count here 
				setUserMessageValue(userId, lCount.intValue());
			}
		}
		return result;
	}

	/**
	 * 客户端定时刷新用户消息总数(10006102)
	 * 
	 * @param jsonObject 请求对象
	 * @return 结果字符串
	 */
	public JsonObject refreshToGetMsgTotal(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		if (!checkTag) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		// 获取参数
		JsonElement userIdje = jsonObject.get("userId");
		JsonElement platformje = jsonObject.get("platform");
		JsonElement appIdje = jsonObject.get("a");
		
		// 验证参数
		int userId;
		int platform=0;
		int appId = AppIdEnum.AMUSEMENT; // 默认KK娱乐
		if (userIdje != null && !userIdje.isJsonNull() && userIdje.getAsInt()>0) {
			// 验证数字
			try {
				userId = userIdje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "10006107");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "10006108");
			return result;
		}
		if (platformje != null && !platformje.isJsonNull() && platformje.getAsInt()>0) {
			// 验证数字
			try {
				platform = platformje.getAsInt();
			} catch (Exception e) {
				logger.error("get platform failed"+e);
				platform = 0;
			}
		}
		if (appIdje != null && !appIdje.isJsonNull() && appIdje.getAsInt() > 0) {
			// 验证数字
			try {
				appId = appIdje.getAsInt();
			} catch (Exception e) {
				logger.error("get appId failed"+e);
				appId = AppIdEnum.AMUSEMENT;
			}
		}
		//苹果台湾版本 去掉新鲜播报和活动通知 platform == 316  为台湾版本
		//KK游戏APP 去掉新鲜播报和活动通知 appId == 2 为KK游戏
		if(platform != PlatformEnum.IPHONE_GAMAGIC && appId != AppIdEnum.GAME) {
			Jedis jedis = null;
			try {
				jedis = UserMessageSource.getInstance();
				//try to generate the activity message and the recommended message if needed
				actMessage.generateActivityMessages(userId, jedis);
				//only new versions of client would give the parameter named maxType.
				JsonElement maxTypeje = jsonObject.get("maxType");
				if (maxTypeje != null && !maxTypeje.isJsonNull() )
					recMessage.generateRecommendedMessages(userId, jedis);
			} catch (Exception e) {
				logger.error("generateActivityMessages or generateRecommendedMessages failed while operating redis", e);
			} finally {
				if(jedis!=null) {
					UserMessageSource.freeInstance(jedis);
				}
			}
		}

		int msgTotalCount = MessageBoxServices.getUserMessageValue(userId);
		
	    //老版兼容新版，去除新版点赞消息的数目
        int praiseCount = 0;
        Jedis jedis = null;
        try {
            jedis = UserMessageSource.getInstance();
            String keyTemp = String.valueOf(userId) + "_msgsort";
            Set<String> msgsortValueList = jedis.zrange(keyTemp, Long.MIN_VALUE,Long.MAX_VALUE);
            for (Iterator<String> iter = msgsortValueList.iterator(); iter.hasNext();) {
                String value = iter.next();
                int msgtype = 0;
                try {
                    msgtype = Integer.parseInt(value.split("_", 3)[1]);
                } catch (Exception e) {
                    logger.error("get messageType failed", e);
                }
                if (msgtype == Message.MSGTYPE_PRAISE) {
                    try {
                        Map<String, String> map = jedis.hgetAll(value);
                        praiseCount = Integer.parseInt(map.get("count"));
                        break;
                    } catch (Exception e) {
                        logger.error("get messageType failed", e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("generatePraiseMessages or generateDynamicMessages failed when operate redis", e);
        } finally {
            if (jedis != null)
                UserMessageSource.freeInstance(jedis);
        }
		
        if (praiseCount > 0 && msgTotalCount > praiseCount) {
            msgTotalCount = msgTotalCount - praiseCount;
        }
		JsonObject result=new JsonObject();
		result.addProperty("TagCode", "00000000");
		result.addProperty("msgTotalCount", Integer.valueOf(msgTotalCount));
		return result;
	}

	/**
	 * 删除消息，包括单一删除和全部删除(10006103)
	 * 
	 * @param jsonObject 请求对象
	 * @return 结果字符串
	 */
	public JsonObject deleteMsg(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		if (!checkTag) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		// 获取参数
		JsonElement userIdje = jsonObject.get("userId");
		JsonElement deleteTypeje=jsonObject.get("deleteType");
		JsonElement msgTypeje = jsonObject.get("msgType");
		JsonElement contextje = jsonObject.get("context");
		// 验证参数
		int userId;
		int deleteType=0;
		int msgType=0;
		int context=0;
		if (userIdje != null && !userIdje.isJsonNull() && userIdje.getAsInt()>0) {
			// 验证数字
			try {
				userId = userIdje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "10006109");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "10006110");
			return result;
		}
		if (deleteTypeje != null && !deleteTypeje.isJsonNull() && deleteTypeje.getAsInt()>0) {
			// 验证数字
			try {
				deleteType = deleteTypeje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "10006113");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "10006114");
			return result;
		}
		
		if(msgTypeje!=null&&!msgTypeje.isJsonNull()&&msgTypeje.getAsInt()>0){
			try{
				msgType=msgTypeje.getAsInt();
			}catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "10006111");
				return result;
			}	
		}
		
		if(contextje!=null&&!contextje.isJsonNull()&&contextje.getAsInt()>0){
			try{
				context=contextje.getAsInt();
			}catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "10006112");
				return result;
			}	
		}
		
		JsonObject result=new JsonObject();
		Jedis jedis=UserMessageSource.getInstance();
		if(jedis == null){
			result.addProperty("TagCode", "10006119");//批量删除失败
			return result;
		}

		try{
			//单一删除消息
			if(deleteType==MessageBoxFunctions.SINGLEDELETE){
				//处理舞台消息
				if(msgType!=0 && msgType==Message.MSGTYPE_ROOM){
					if(context!=0){
						try{
							MessageBoxFunctions.deleteSingleUserMessage(jedis, userId, msgType, context, null);
						}catch (Exception e) {
							logger.error("single delete failed "+e);
							result.addProperty("TagCode", "10006116");//单一删除失败
							return result;
						}
					}else{
						result.addProperty("TagCode", "10006115"); // 10006115 : 删除舞台消息时，缺少关联的context
						return result;
					}
				}
			}else if(deleteType==MessageBoxFunctions.BACCHDELETE_ROOMMESSAGE){
				try{
					MessageBoxFunctions.deleteAllUserStageMessage(jedis, userId);
				}catch (Exception e) {
					logger.error("batch delete failed "+e);
					result.addProperty("TagCode", "10006117");//批量删除失败
					return result;
				}
			}else{
//				logger.error("the deleteType is unvalid");
				result.addProperty("TagCode", "10006118");//
				return result;
			}
		}catch(Exception e){
			logger.error("Process delete message error", e);
		}finally{
			UserMessageSource.freeInstance(jedis);
		}
		
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
	}

	
	/**
	 * 遍历消息(10006104)
	 * fetchMessage
	 */
	public JsonObject fetchMessage(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		if (!checkTag) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
	
		int msgType;
		int userId;
		int platform;
		int perPageCount;
		int curPage;
		long startTime = 0;
		long lrtFromClient = 0;//last read time passed from client
		
		JsonObject result = new JsonObject();
		try{
			msgType = CommonUtil.getJsonParamInt(jsonObject, "msgType", 0, "10006120", Message.MSGTYPE_KKASSISTANT, Message.MSGTYPE_LAST);//not include stage message
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "10006121", 1, Integer.MAX_VALUE);
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, "10006122", 1, 10);
			perPageCount = CommonUtil.getJsonParamInt(jsonObject, "perPageCount", 20, null, 1, 30);//This param is optional, so pass null
			curPage = CommonUtil.getJsonParamInt(jsonObject, "curPage", 0, "10006123", 1, 100);
			startTime = CommonUtil.getJsonParamLong(jsonObject, "startTime", 0, null , 0, Long.MAX_VALUE);//this param is optional
			lrtFromClient = CommonUtil.getJsonParamLong(jsonObject, "lastReadTime", 0, null , 0, Long.MAX_VALUE);//this param is optional
		}catch(CommonUtil.ErrorGetParameterException e){
			result.addProperty("TagCode", e.getErrCode());
			return result;
		}catch(Exception e){
			result.addProperty("TagCode", "10001111");
			return result;
		}
		
		long retLastReadTime = 0;
		long lastReadTime = ConfigHelper.getInitLastReadTime();//1386000000000L;
		Jedis jedis = null;
		try {
			jedis = UserMessageSource.getInstance();
			if(lrtFromClient == 0){
				String strLastReadTime = MessageBoxFunctions.getLastTime(jedis, userId, msgType, "read");
				if(strLastReadTime != null)
					lastReadTime = Long.parseLong(strLastReadTime);
			}else{
				lastReadTime = lrtFromClient;
			}

			//delete the aggregated message info in the redis if startTime is not passed(=0)
			if(startTime == 0){
				startTime = getLastMessageGenerateTime(jedis);
				if(curPage == 1){
					MessageBoxFunctions.deleteSingleUserMessage(jedis, userId, msgType, 0, null);
					long lCurTime = new Date().getTime();
					if(lCurTime > startTime ){
						lCurTime = startTime;
					}
					MessageBoxFunctions.setLastTime(jedis, userId, msgType, "read", lCurTime);//update the last readtime
					retLastReadTime = lastReadTime;
				}
			}
		
		} catch (Exception e) {
			logger.error("fetchMessage failed when operate redis", e);
		} finally {
			if(jedis!=null) {
				UserMessageSource.freeInstance(jedis);
			}
		}

		switch(msgType){
		case Message.MSGTYPE_DYNAMIC: 
			result = MessageBoxFunctions.getUserNewsCommentMsg(userId, platform, perPageCount, curPage, startTime, lastReadTime);
			break;
		case Message.MSGTYPE_KKASSISTANT:
			result = MessageBoxFunctions.getKkAssistorRecord(userId,perPageCount,curPage,startTime,platform,lastReadTime);
			break;
		case Message.MSGTYPE_SYSTEM:
			result = MessageBoxFunctions.getKkSystemRecord(userId,perPageCount,curPage,startTime,platform,lastReadTime);
			break;
		case Message.MSGTYPE_ACTIVE:
			result = actMessage.fetchMessage(userId, platform, perPageCount, curPage, startTime, lastReadTime);
			break;
		case Message.MSGTYPE_RECOMMENDED:
			result = recMessage.fetchRecommendedMsg(userId, perPageCount, curPage, startTime, platform, lastReadTime);
			break;
		}
		if(retLastReadTime != 0)
			result.addProperty("lastReadTime", retLastReadTime);
		return result;
	}
	
	private static void setUserMessageValue(int userId, int value){
		Jedis jedis = null;
		try {
			jedis = UserMessageSource.getInstance();

			jedis.hset("msgTotalCount", String.valueOf(userId), String.valueOf(value));
		} catch (Exception e) {
			logger.error("setUserMessageValue failed when operate redis", e);
		} finally {
			if(jedis!=null) {
				UserMessageSource.freeInstance(jedis);
			}
		}
	}
	
	//根据key：userId 获取其包含的信息列表Sort Set
	private static JsonArray getUserMessageListInternal(String key, int prePageCount, 
			int currentPage, ArrayList<Long> retCount, int maxType, int appId){
		Jedis jedis=null;
		try{
			jedis=UserMessageSource.getInstance();
			JsonArray jsonArray=new JsonArray();
			String keyTemp=key+"_msgsort";
			Long lTotal = jedis.zcount(keyTemp, Double.MIN_VALUE, Double.MAX_VALUE);
			Set<String> msgsortValueList= jedis.zrange(keyTemp,(currentPage-1)*prePageCount, currentPage*prePageCount-1);
			for(Iterator<String> iter=msgsortValueList.iterator();iter.hasNext();){
				String value=iter.next();
				int msgtype = 0;
				try{
					msgtype=Integer.parseInt( value.split("_", 3)[1] );
				}catch(Exception e){
					logger.error("get messageType failed", e);
				}
				if (msgtype == 0)
					continue;
				if (msgtype == Message.MSGTYPE_RECOMMENDED && maxType < Message.MSGTYPE_RECOMMENDED){
					if(lTotal!=null)
						lTotal--;
					continue;
				}
				
				//老版兼容新版，去除新版的点赞消息
                if (msgtype == Message.MSGTYPE_PRAISE){
                    MessageBoxFunctions.deleteSingleUserMessage(jedis, Integer.parseInt(key), Message.MSGTYPE_PRAISE, 0, null);
                    if(lTotal!=null)
                        lTotal--;
                    continue;
                }
				
				JsonObject jsonObject= new JsonObject();
				try{
					Map<String, String> map=jedis.hgetAll(value);
					if(msgtype == Message.MSGTYPE_RECOMMENDED){
						Map<String, String> msgMap = new HashMap<String, String>();
						msgMap =  recMessage.getLastRecommendedMsg();
						jsonObject.addProperty("message", msgMap.get("message")); 
					}else{
						jsonObject.addProperty("message", map.get("message"));
					}
					jsonObject.addProperty("to", Integer.parseInt(map.get("to")));
					jsonObject.addProperty("count",Integer.parseInt(map.get("count")));
					jsonObject.addProperty("msgtime",map.get("msgtime"));
					jsonObject.addProperty("from", Integer.parseInt(map.get("from")));
					String strContext = map.get("context");
					if(strContext != null && !strContext.isEmpty())
						jsonObject.addProperty("context",Integer.parseInt(strContext));
					String strTarget = map.get("target");
					if(strTarget != null && !strTarget.isEmpty())
						jsonObject.addProperty("target",Integer.parseInt(strTarget));
				}catch (Exception e) {
					logger.error("get message hashes failed "+e);
				}
				jsonObject.addProperty("msgType", msgtype);
				jsonArray.add(jsonObject);
			}
			if(lTotal != null)
				retCount.add(lTotal);
			return jsonArray;
		}catch (Exception e) {
			logger.error("getUserMessageList failed when operate redis", e);
		}finally{
			if(jedis!=null)
				UserMessageSource.freeInstance(jedis);
		}
		return null;
	}
	
	private static JsonObject getUserNewsCommentMsg(int userId, int platform, int perPageCount, int currentPage, long startTime,long lastReadTime)throws Exception{
		Date startTimer=new Date(startTime);
		Date lastReadTimer=new Date(lastReadTime);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("userId", userId);
		map.put("lastReadTime", lastReadTimer);
		map.put("startTime", startTimer);
		map.put("start", (currentPage - 1) * perPageCount);
		map.put("offset", perPageCount);
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("UserMessage.getUserNewsCommentMsg", map);
		} catch (SQLException e) {
			logger.error("未能正常调用存储过程", e);
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			return result;
		}
		String TagCode = (String) map.get("TagCode");
		if (TagCode.equals(TagCodeEnum.SUCCESS)) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCode);
			JsonArray jCommentList = new JsonArray();
			@SuppressWarnings("unchecked")
			List<NewsComment> commentList = (ArrayList<NewsComment>) map.get("commentList");
			commentList = UserService.addUserExtra(commentList);
			
			for (Object object : commentList) {
				JsonObject jsonObj=new JsonObject();
				NewsComment obj=(NewsComment)object;
				jsonObj.addProperty("userId",obj.getUserId());
				jsonObj.addProperty("nickname",obj.getNickname());
				jsonObj.addProperty("gender", obj.getGender());
				try{
					MessageBoxFunctions.getPortraitByPlatform(platform,obj,jsonObj);
				}catch (Exception e) {
					logger.error("get the portrait failed"+e);
				}
				jsonObj.addProperty("message",obj.getContent());
				if(lastReadTime<=obj.getCommentTime().getTime()){
					jsonObj.addProperty("isnew", Integer.valueOf(1));
				}else{
					jsonObj.addProperty("isnew", Integer.valueOf(0));
				}
				if(obj.getUserIdTarget() != null && obj.getUserIdTarget() != 0){
					jsonObj.addProperty("target", obj.getUserIdTarget());
				}
				
				jsonObj.addProperty("msgtime",obj.getCommentTime().getTime());
				jsonObj.addProperty("id", obj.getCommentId());
				jsonObj.addProperty("newsid", obj.getNewsId());
				jCommentList.add(jsonObj);
			}
			result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
			result.add("messageList", jCommentList);
			int total=(Integer)map.get("commentTotal");
			if(currentPage==1)
				result.addProperty("total", total);
			// 返回结果
			return result;
		} else if (TagCode.equals("02") || TagCode.equals("03")) {
			/* '02';分页超出范围 , '04';传入的参数perPageCount invalid */
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			result.add("commentList", new JsonArray());
			result.addProperty("total", 0);
			return result;
		}else{
			// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
			logger.error("调用存储过程(UserMessage.getUserNewsCommentMsg)未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + map.toString());
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
			return result;
		}
	}
	
	private static String getUserMessageRedisKey(int userId, int typeMsg, int roomId){
		if(typeMsg == Message.MSGTYPE_ROOM){
			return String.format("%d_1_%d", userId, roomId);
		}
		else if(typeMsg < Message.MSGTYPE_LAST){
			return String.format("%d_%d", userId, typeMsg);
		}
		return null;
	}
	
	private static String getUserMessageSortSetRedisKey(int userId){
		return String.format("%d_msgsort", userId);
	}
	
	private static boolean deleteSingleUserMessage(Jedis jedis, int userId, int typeMsg, int roomId, String keyMsg0){
		if(jedis == null)
			return false;
		
		String keyMsg = null;
		if(keyMsg0 != null)
			keyMsg = keyMsg0;
		else
			keyMsg = getUserMessageRedisKey(userId, typeMsg, roomId);
		if(keyMsg == null)
			return false;
		
		try{
			//remove it form sorted set
			jedis.zrem(getUserMessageSortSetRedisKey(userId), keyMsg);
			
			//decrease the total number
			String userIdStr = String.valueOf(userId);
			String countStr = jedis.hget(keyMsg, "count");
			int count = 0;
			if(countStr != null)
				count = Integer.parseInt(countStr);
			int countTotal = 0;
			String countTotalStr = jedis.hget("msgTotalCount", userIdStr);
			if(countTotalStr != null)
				countTotal = Integer.parseInt(countTotalStr);
			if(countTotal < count){
				jedis.hset("msgTotalCount",userIdStr , "0");
			}
			else if(count > 0){
				jedis.hincrBy("msgTotalCount", userIdStr, -count);
			}
			
			//remove the hash
			jedis.del(keyMsg);
			
			return true;
		}catch(Exception e){
			logger.error("deleteSingleUserMessage exception", e);
		}
		
		return false;
	}
	
	private static boolean deleteAllUserStageMessage(Jedis jedis, int userId){//delete all room message
		if(jedis == null)
			return false;

		try{
			//get all user stage message types
			ArrayList<String> toRemoveList = new ArrayList<String>();
			Set<String> userMsgTypes = jedis.zrange(getUserMessageSortSetRedisKey(userId), 0, -1);
			String stageKeyType = String.format("%d_%d_", userId, 1);
			for(Iterator<String> iter=userMsgTypes.iterator();iter.hasNext();){
				String valueTemp=iter.next();
				if(valueTemp.contains(stageKeyType)){
					toRemoveList.add(valueTemp);
				}
			}
			
			//remove all that need to be removed
			for(int i=0; i<toRemoveList.size(); i++){
				deleteSingleUserMessage(jedis, userId, Message.MSGTYPE_ROOM, 0, toRemoveList.get(i));
			}
			
			return true;
		}catch(Exception e){
			logger.error("deleteSingleUserMessage exception", e);
		}
		
		return false;
	}
	
	@SuppressWarnings("unused")
	private static boolean deleteUserMessage(int userId, int deleteType, int typeMsg, int roomId){
		Jedis jedis = UserMessageSource.getInstance();
		if(jedis != null){
			try{
				if(deleteType == MessageBoxFunctions.BACCHDELETE_ROOMMESSAGE){
					deleteAllUserStageMessage(jedis, userId);
				}
				else{
					deleteSingleUserMessage(jedis, userId, typeMsg, roomId, null);
				}
				return true;
			}catch(Exception e){
				logger.error("ActivityMessage.getUnreadCount error", e);
			}finally{
				UserMessageSource.freeInstance(jedis);
			}
		}
		return false;
	}
	
	private static String CONFIG_MESSAGEGENERATOR = "messageGeneratorLastCheck";
	private static long getLastMessageGenerateTime(Jedis jedis) {
		if (jedis != null) {
			try {
				String sLastTime = jedis.get(CONFIG_MESSAGEGENERATOR);
				if (sLastTime != null) {
					long startTime = Long.parseLong(sLastTime);
					return startTime;
				}
			} catch (Exception e) {
			}
		}
		return System.currentTimeMillis();
	}
	
	//根据platform来获取相应大小的porat
	private static JsonObject getPortraitByPlatform(int platform,NewsComment object,JsonObject jObject){
		if (object.getPortrait_path_original() != null) {
			if(platform == PlatformEnum.WEB) {
				jObject.addProperty("portrait_path_256", object.getPortrait_path_256());
			} else if(platform == PlatformEnum.ANDROID) {
				jObject.addProperty("portrait_path_48", object.getPortrait_path_48());
			} else if(platform == PlatformEnum.IPHONE) {
				jObject.addProperty("portrait_path_128", object.getPortrait_path_128());
			} else if(platform == PlatformEnum.IPAD) {
				jObject.addProperty("portrait_path_128", object.getPortrait_path_128());
			} else {
				jObject.addProperty("portrait_path_1280", object.getPortrait_path_1280());
				jObject.addProperty("portrait_path_256", object.getPortrait_path_256());
				jObject.addProperty("portrait_path_128",  object.getPortrait_path_128());
				jObject.addProperty("portrait_path_48", object.getPortrait_path_48());
			}
		}
		return jObject;
	}
	
	private static boolean setUserMessages(Jedis jedis, int userId, Map<String, String> msgMap, int newCount, int msgType){
		if(jedis == null || userId == 0 || msgMap == null || msgMap.size() == 0)
			return false;
		
		String msgKey = MessageBoxFunctions.getUserMessageRedisKey(userId, msgType, 0);
		String sortKey = MessageBoxFunctions.getUserMessageSortSetRedisKey(userId);

		//1. set the hash data
		int oldCount = 0;
		String strCount = jedis.hget(msgKey, "count");
		if(strCount != null){
			try{
				oldCount = Integer.parseInt(strCount);
			}catch(Exception e){
			}
		}
		if(newCount > 0)
		{
			jedis.hmset(msgKey, msgMap);
			//2. sort the message
			jedis.zadd(sortKey, Double.parseDouble(msgMap.get("msgtime")), msgKey);
		}
		else{
			MessageBoxFunctions.deleteSingleUserMessage(jedis, userId, msgType, 0, null);//delete the message if the count is 0
		}
		
		//3. total count ++
		String msgOwnUserId = msgMap.get("to");
		if (jedis.hget("msgTotalCount", msgOwnUserId) != null) {
			if(newCount != oldCount)
				jedis.hincrBy("msgTotalCount", msgOwnUserId, newCount - oldCount);
		} else {
			jedis.hset("msgTotalCount", msgOwnUserId, String.valueOf(newCount));
		}
		return true;
	}
	
	private static final String REDISKEY_LASTTIME = "effLastTime";
	private static String getLastTime(Jedis jedis, int userId, int msgType, String typeName){
		try{
			return jedis.hget(REDISKEY_LASTTIME, String.format("%d_%d_%s", userId, msgType, typeName));
		}catch(Exception e){
			logger.error("ActivityMessage.getUnreadCount error", e);
		}
		return null;
	}
	private static void setLastTime(Jedis jedis, int userId, int msgType, String typeName, long time){
		try{
			jedis.hset(REDISKEY_LASTTIME, String.format("%d_%d_%s", userId, msgType, typeName), String.valueOf(time));
		}catch(Exception e){
			logger.error("ActivityMessage.getUnreadCount error", e);
		}
	}
	
	static class ActivityMessage{
		public static final String REDISKEY_EFFECTIVEACTIVITIES = "effectiveActivities";
		public static final String REDISKEY_EFFECTIVEACTIVITYIDS = "effectiveActivityIds";
		public static final String REDISKEY_EFFACTREADIDS = "effActReadIds_%d";

		private static long TIME_LAST_RESTORE = 0;
		public static String[] idListArrayEffective = new String[]{};
		
		private String[] getEffectiveActivityIds(Jedis jedis){
			//This function only load new activity list every one day
			long queryTimestamp = System.currentTimeMillis(); 
			//check if the queryTime is beyond 1 day
			if(TIME_LAST_RESTORE == 0)
				TIME_LAST_RESTORE = queryTimestamp;
			else{
				if(queryTimestamp - TIME_LAST_RESTORE < ConfigHelper.getCycleEffRedisActIds())
					return idListArrayEffective;
				TIME_LAST_RESTORE = queryTimestamp;
			}

			String idList = jedis.get(REDISKEY_EFFECTIVEACTIVITYIDS);
			if(idList != null)
				idListArrayEffective = idList.split(":");
			else
				idListArrayEffective = new String[]{};
			return idListArrayEffective;
		}
		
		public void generateActivityMessages(int userId, Jedis jedisIn){
			Jedis jedisCreated = null;
			Jedis jedis = null;
			if(jedisIn == null){
				jedisCreated = UserMessageSource.getInstance();
				jedis = jedisCreated;
			}
			else {
				jedis = jedisIn;
			}
			if(jedis != null){
				try{
					long queryTimestamp = System.currentTimeMillis();
					long lastGenTime = 0L;
					try{
						lastGenTime = Long.parseLong(getLastTime(jedis, userId, Message.MSGTYPE_ACTIVE, "Generate"));
					}catch(Exception e){
					}
					if(lastGenTime == 0)
						lastGenTime = queryTimestamp;
					else{
						if(queryTimestamp - lastGenTime < ConfigHelper.getCycleGenAct())
							return;//We only generate new activity message once every day.
						lastGenTime = queryTimestamp;
					}
					setLastTime(jedis, userId, Message.MSGTYPE_ACTIVE, "Generate", lastGenTime);
					
					//now, seems we need to generate the activity message
					String[] idListArray = getEffectiveActivityIds(jedis);
					if(idListArray == null || idListArray.length == 0 || (idListArray.length == 1 && idListArray[0].isEmpty())){
						//no effective activity message
						MessageBoxFunctions.deleteSingleUserMessage(jedis, userId, Message.MSGTYPE_ACTIVE, 0, null);
						return;
					}
					
					//fetch last time info
					List<String> eaLastValues = jedis.hmget(REDISKEY_EFFECTIVEACTIVITIES, 
							"id", "message","msgtime");
					if(eaLastValues.size() != 3 || 
							eaLastValues.get(0) == null || 
							eaLastValues.get(1) == null || 
							eaLastValues.get(2) == null){
//						logger.error("eaLastValues should be 3");
						return;
					}

					//the activity message to be saved
					Map<String, String> msgMap = new HashMap<String, String>();
					msgMap.put("to", String.valueOf(userId));
					msgMap.put("from", idListArray[0]);
					msgMap.put("context", "");
					msgMap.put("message", eaLastValues.get(1));
					msgMap.put("msgtime", eaLastValues.get(2));
					
					String idListRead = jedis.get(String.format(REDISKEY_EFFACTREADIDS, userId));
					if(idListRead == null){
						msgMap.put("count", String.valueOf(idListArray.length));
						MessageBoxFunctions.setUserMessages(jedis, userId, msgMap, idListArray.length, Message.MSGTYPE_ACTIVE);
						return;
					}
					
					String[] idListReadArray = null;
					idListReadArray = idListRead.split(":");

					int c = idListArray.length;
					for(int i=0; i<idListArray.length; i++){
						if(idListArray[i].isEmpty())
							continue;
						for(int j=0; j<idListReadArray.length; j++){
							if(idListReadArray[j].isEmpty())
								continue;
							if(idListArray[i].compareTo(idListReadArray[j]) == 0)
								c--;
						}
					}
					msgMap.put("count", String.valueOf(c));
					MessageBoxFunctions.setUserMessages(jedis, userId, msgMap, c, Message.MSGTYPE_ACTIVE);

				}catch(Exception e){
					logger.error("ActivityMessage.generateActivityMessages error", e);
				}finally{
					if(jedisCreated != null)
						UserMessageSource.freeInstance(jedisCreated);
				}
			}
		}

		//fetch activity message
		public JsonObject fetchMessage(int userId, int platform, int perPageCount, int curPage, 
				long startTime, long lastReadTime){
			
			//SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date dateStartTime = new Date(startTime);

			Map<String, Object> map = new HashMap<String, Object>();
			map.put("startDate", dateStartTime);
			map.put("start", (curPage - 1) * perPageCount);
			map.put("offset", perPageCount);
			try {
				SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("EffectiveActivities.getEffectiveActivities", map);
			} catch (SQLException e) {
				logger.error("未能正常调用存储过程", e);
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
				return result;
			}
			
			String TagCode = (String) map.get("TagCode");
			if (TagCode.equals(TagCodeEnum.SUCCESS)) {
				// 取出列表
				@SuppressWarnings("unchecked")
				List<EffectiveActivity> recordList = (ArrayList<EffectiveActivity>) map.get("actList");

				long currentTime = new Date().getTime();
				String idReadList = new String();
				
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", TagCode);
				result.addProperty("total", (Integer) map.get("actTotal"));
				//result.addProperty("actPageTotal", recordList.size());
				JsonArray jRecordList = new JsonArray();
				for (EffectiveActivity object : recordList) {
					jRecordList.add(object.toJsonObject(lastReadTime, currentTime, platform));
					if(object.isEffective(currentTime)){
						idReadList += object.getActivityId() + ":";
					}
				}
				result.add("messageList", jRecordList);
				
				//update the read-ed message id list in the redis
				Jedis jedis = null;
				try {
					jedis = UserMessageSource.getInstance();
					
					String[] idListArray = getEffectiveActivityIds(jedis);
					if(idListArray != null && idListArray.length > 0){
						for(int i=0; i<idListArray.length; i++){
							if(idReadList.contains(idListArray[i]))
								continue;
							idReadList += idListArray[i] + ":";
						}
					}
					jedis.set(String.format(REDISKEY_EFFACTREADIDS, userId), idReadList);
				} catch (Exception e) {
					logger.error("fetchMessage failed when operate redis", e);
				} finally {
					if(jedis!=null) {
						UserMessageSource.freeInstance(jedis);
					}
				}
	
				// 返回结果
				return result;
			} else if (TagCode.equals("02") || TagCode.equals("03")) {
				/* '02';分页超出范围 */
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", TagCodeEnum.SUCCESS);
				result.addProperty("total", (Integer) map.get("actTotal"));
				result.addProperty("actPageTotal", 0);
				result.add("actList", new JsonArray());
				// 返回结果
				return result;
			} else {
				// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
				logger.error("调用存储过程(EffectiveActivities.getEffectiveActivities)未的到正常结果,TagCode:" + TagCode + ",map:" + map.toString());
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
				return result;
			}
		}
		
	}
	
	/**
	 * get the KK Asstant Record
	 * 
	 */
	@SuppressWarnings("unchecked")
	private static JsonObject getKkAssistorRecord(int userId, int perPageCount,
			int curPage, long startTime,int platform,long lastReadTime) {

		Date beginTime = new Date(lastReadTime);
		Date endTime = new Date(startTime);
		// get the record from oracle by call stored procedure
		if (curPage <= 0) {
			curPage = 1;
		}

		int min = (curPage - 1) * perPageCount;
		int max = curPage * perPageCount;
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("userId", userId);
		map.put("startTime", beginTime);
		map.put("endTime", endTime);
		map.put("min", min);
		map.put("max", max);
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject(
					"KkAssistor.getKkAssistorNoticeList", map);
		} catch (SQLException e) {
			logger.error("未能正常调用存储过程", e);
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			return result;
		}

		String TagCode = (String) map.get("TagCode");
		JsonArray jsonArr = new JsonArray();
		if (TagCode.equals(TagCodeEnum.SUCCESS)) {
			JsonObject result = new JsonObject();
			List<KkAssistor> kNotice = (List<KkAssistor>) map.get("kkNotices");
			if (kNotice != null && kNotice.size() > 0) {
				for (KkAssistor k : kNotice) {
					jsonArr.add(new JsonParser().parse(new Gson().toJson(k
							.toJsonObject(lastReadTime,platform))));
				}

			}
			result.addProperty("TagCode", TagCode);
			if(min==0)
				result.addProperty("total", (Integer) map.get("totalNotices"));
			result.add("messageList", jsonArr);
			result.addProperty("pathPrefix",ConfigHelper.getHttpdir());
			return result;
		}else if (TagCode.equals("02") || TagCode.equals("03")) {
			/* '02';分页超出范围 */
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			result.addProperty("total", (Integer) map.get("totalNotices"));
			result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
			result.add("messageList", new JsonArray());
			// 返回结果
			return result;
		}else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);// fail to call stored  procedure
			return result;
		}

	}
	
	/**
	 * get the KK System Record
	 * 
	 */
	@SuppressWarnings("unchecked")
	private static JsonObject getKkSystemRecord(int userId, int perPageCount,
			int curPage, long startTime, int platform,long lastReadTime) {
		
		Date beginTime = new Date(lastReadTime);
		Date endTime = new Date(startTime);
		// get the record from oracle by call stored procedure
		if (curPage <= 0) {
			curPage = 1;
		}
		int min = (curPage - 1) * perPageCount;
		int max = curPage * perPageCount;
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("userId", userId);
		map.put("startTime", beginTime);
		map.put("endTime", endTime);
		map.put("min", min);
		map.put("max", max);
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject(
					"KkSystemNotice.getKkSystemNoticeList", map);
		} catch (SQLException e) {
 			logger.error("未能正常调用存储过程", e);
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			return result;
		}

		String TagCode = (String) map.get("TagCode");
		JsonArray jsonArr = new JsonArray();
		if (TagCode.equals(TagCodeEnum.SUCCESS)) {
			JsonObject result = new JsonObject();
			List<KkSystemNotice> kSysNotice = (List<KkSystemNotice>) map.get("kkSysNotices");
			if (kSysNotice != null && kSysNotice.size() > 0) {
				kSysNotice = UserService.addUserExtra(kSysNotice);
				for (KkSystemNotice k : kSysNotice) {
					jsonArr.add(new JsonParser().parse(new Gson().toJson(k
							.toJsonObject(lastReadTime,platform))));
				}

			}
			result.addProperty("TagCode", TagCode);
			if(min==0)
				result.addProperty("total", (Integer) map.get("totalSysNotices"));
			result.add("messageList", jsonArr);
			result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
			return result;
		}else if (TagCode.equals("02") || TagCode.equals("03")) {
			/* '02';分页超出范围 */
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			result.addProperty("total", (Integer) map.get("totalSysNotices"));
			//result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
			result.add("messageList", new JsonArray());
			// 返回结果
			return result;
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);// fail to call stored procedure
			return result;
		}

	}
	
	/**
	 * 获取消息推送开关设置
	 */
	public JsonObject getMessageSwitchSet(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		if (!checkTag) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		// 获取参数
		Integer userId;
		JsonObject result=new JsonObject();
		try{
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "10006124", 1, Integer.MAX_VALUE);
		}catch(CommonUtil.ErrorGetParameterException e){
			result.addProperty("TagCode", e.getErrCode());
			return result;
		}catch(Exception e){
			result.addProperty("TagCode", "10001111");
			return result;
		}
		
		String[] msgBtnTypes={"amBtn","vmBtn"};
		String[] btnValues = HotDataSource.getHotFieldValues(String.valueOf(userId), msgBtnTypes);
		String tagCode = TagCodeEnum.SUCCESS;

		if(btnValues != null && btnValues.length == 2&&btnValues[0] != null && btnValues[1] !=null){
			result.addProperty("TagCode",tagCode);
			result.addProperty("amBtn", Integer.parseInt(btnValues[0]));
			result.addProperty("vmBtn", Integer.parseInt(btnValues[1]));
		}else{
			result.addProperty("TagCode", tagCode);
			result.addProperty("amBtn", 1);
			result.addProperty("vmBtn", 1);
	    }
		return result;
	}
	
	/**
	 *设置消息推送开关
	 */
	public JsonObject setMessageSwitch(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		if (!checkTag) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		// 获取参数
		int userId;
		int amBtn = 0;
		int vmBtn = 0;
		// 验证参数
		JsonObject result=new JsonObject();
		try{
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "10006126", 1, Integer.MAX_VALUE);
			if(jsonObject.get("amBtn") != null){
				amBtn = CommonUtil.getJsonParamInt(jsonObject, "amBtn", 0, "10006128", 1, 2);
			}
			if(jsonObject.get("vmBtn") != null){
				vmBtn = CommonUtil.getJsonParamInt(jsonObject, "vmBtn", 0, "10006129", 1, 2);
			}
		}catch(CommonUtil.ErrorGetParameterException e){
			result.addProperty("TagCode", e.getErrCode());
			return result;
		}catch(Exception e){
			result.addProperty("TagCode", "10001111");
			return result;
		}
		
		try{
			if(amBtn != 0){
				HotDataSource.setHotFieldValue(String.valueOf(userId), "amBtn", String.valueOf(amBtn));
			}
			if(vmBtn != 0){
				HotDataSource.setHotFieldValue(String.valueOf(userId), "vmBtn", String.valueOf(vmBtn));
			}
		}catch (Exception e) {
			result.addProperty("TagCode", "10001113");//设置过程出现异常
			return result;
		}
		result.addProperty("TagCode",TagCodeEnum.SUCCESS);
		return result;
	}
	
	static class RecommendMessage{
			
		public static final String REDISKEY_LASTRECOMMENDMSG = "lastRecommendMsg";
		public static final String REDISKEY_TOTALRECOMMENDMSG = "totalRecommendMsg";
		
		private Map<String,String> getLastRecommendedMsg(Jedis jedis){	
			//Get the latest recommended message from redis.
			String msgId = jedis.hget(REDISKEY_LASTRECOMMENDMSG, "id");
			String msg = jedis.hget(REDISKEY_LASTRECOMMENDMSG, "message");
			String msgTime = jedis.hget(REDISKEY_LASTRECOMMENDMSG, "msgtime");
			Map<String,String> msgMap = new HashMap<String,String>();
			msgMap.put("msgid", msgId);
			msgMap.put("message", msg);
			msgMap.put("msgtime", msgTime);
			return msgMap;
		}
		
		public Map<String,String> getLastRecommendedMsg(){
			Jedis jedis = null; 
			jedis = UserMessageSource.getInstance();
			if(jedis != null){
				try{
					return getLastRecommendedMsg(jedis);
				}catch(Exception e){
					logger.error("Errors occurred in RecommendedMessage.getLastRecommendedMsg static", e);
				}finally{
					UserMessageSource.freeInstance(jedis);
				}
			}
			return null;
		}
		
		private int getRecommendedMsgGross(Jedis jedis){
			String val = jedis.get(REDISKEY_TOTALRECOMMENDMSG);
			if (val!=null) {
				int msgGross = Integer.valueOf(val);
				return msgGross;
			}
			return 0;
		}
		
		public void generateRecommendedMessages(int userId, Jedis jedisIn){
			Jedis jedisCreated = null;
			Jedis jedis = null;
			if(jedisIn == null){
				jedisCreated = UserMessageSource.getInstance();
				jedis = jedisCreated;
			}
			else {
				jedis = jedisIn;
			}
			if(jedis != null){
				try{
					long queryTimestamp = System.currentTimeMillis();
					long lastGenTime = 0L;
					try{
						lastGenTime = Long.parseLong(getLastTime(jedis, userId, Message.MSGTYPE_RECOMMENDED, "Generate"));
					}catch(Exception e){
					}
					if(lastGenTime == 0)
						lastGenTime = queryTimestamp;
					else{
						if(queryTimestamp - lastGenTime < ConfigHelper.getCycleGenAct())
							return;
						lastGenTime = queryTimestamp;
					}
					setLastTime(jedis, userId, Message.MSGTYPE_RECOMMENDED, "Generate", lastGenTime);
					
					int c = recMessage.getRecommendedMsgGross(jedis);
					int readCount = 0;
					try{
						String readCountStr = getLastTime(jedis, userId, Message.MSGTYPE_RECOMMENDED, "readCount");
						if(readCountStr != null)
							readCount = Integer.parseInt(readCountStr);
						else {
							long lCurTime = new Date().getTime();
							readCount = c;//第1次(可能是新注册用户)，标记所有消息为已读
							MessageBoxFunctions.setLastTime(jedis, userId, Message.MSGTYPE_RECOMMENDED, "read", lCurTime);//update the last readtime
						}
					}catch(Exception e){
					}
					//Get the latest recommended message and the gross of recommended messages.
					Map<String, String> msgMap = new HashMap<String, String>();
					msgMap = recMessage.getLastRecommendedMsg(jedis);
					msgMap.put("message", "");//we will not save the message, to reduce the memory use of redis
					msgMap.put("to", String.valueOf(userId));
					msgMap.put("from", msgMap.get("msgid"));
					msgMap.put("context", "");
					c -= readCount;
					if(c < 0)
						c = 0;
					msgMap.put("count", String.valueOf(c));
					MessageBoxFunctions.setUserMessages(jedis, userId, msgMap, c, Message.MSGTYPE_RECOMMENDED);

				}catch(Exception e){
					logger.error("Errors occurred in RecommendedMessage.generateRecommendedMessages", e);
				}finally{
					if(jedisCreated != null)
						UserMessageSource.freeInstance(jedisCreated);
				}
			}
		}
		
		/**
		 *获取精彩推荐 
		 * 
		 */
		public JsonObject fetchRecommendedMsg(int userId, int perPageCount,
				int curPage, long startTime, int platform,long lastReadTime) {
			Date endTime = new Date(startTime);
			if (curPage <= 0) {
				curPage = 1;
			}
			int min = (curPage - 1) * perPageCount;
			int max = curPage * perPageCount;
			Map<Object, Object> map = new HashMap<Object, Object>();
			
			map.put("startTime", endTime);
			map.put("start", min);
			map.put("offset", max);
			try {
				SqlMapClientHelper.getInstance(DB.MASTER).queryForObject(
						"RecommendedMessage.getRecommendedMessages", map);
			} catch (SQLException e) {
	 			logger.error("未能正常调用存储过程", e);
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
				return result;
			}

			String TagCode = (String) map.get("TagCode");
			JsonArray jsonArr = new JsonArray();
			if (TagCode.equals(TagCodeEnum.SUCCESS)) {
				JsonObject result = new JsonObject();
				@SuppressWarnings("unchecked")
				List<RecommendedMessage> kRecommendedMsg = (ArrayList<RecommendedMessage>) map.get("rcmList");
				if (kRecommendedMsg != null && kRecommendedMsg.size() > 0) {
					for (RecommendedMessage k : kRecommendedMsg) {
						jsonArr.add(new JsonParser().parse(new Gson().toJson(k
								.toJsonObject(lastReadTime, platform))));
					}

				}
				
				//update the read count.
				Jedis jedis = null;
				try {
					jedis = UserMessageSource.getInstance();
					int recTotal = recMessage.getRecommendedMsgGross(jedis);
					setLastTime(jedis, userId, Message.MSGTYPE_RECOMMENDED, "readCount", recTotal);
				} catch (Exception e) {
					logger.error("Failed to set read count", e);
				} finally {
					if(jedis!=null) {
						UserMessageSource.freeInstance(jedis);
					}
				}
				
				//return the result
				result.addProperty("TagCode", TagCode);
				if(min==0)
					result.addProperty("total", (Integer) map.get("rcmTotal"));
				result.add("messageList", jsonArr);
				return result;

			}else if (TagCode.equals("02") || TagCode.equals("03")) {
				/* '02';分页超出范围 */
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", TagCodeEnum.SUCCESS);
				result.addProperty("total", (Integer) map.get("totalSysNotices"));
				//result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
				result.add("messageList", new JsonArray());
				// 返回结果
				return result;
			} else {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);// fail to call stored procedure
				return result;
			}
		}
	}
	
}

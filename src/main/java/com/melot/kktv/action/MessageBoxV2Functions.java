package com.melot.kktv.action;

import com.google.gson.*;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcx.service.MessageBoxServices;
import com.melot.kkcx.service.UserService;
import com.melot.kktv.model.*;
import com.melot.kktv.redis.HotDataSource;
import com.melot.kktv.redis.UserMessageSource;
import com.melot.kktv.util.*;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;
import com.melot.kktv.util.message.Message;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.*;

public class MessageBoxV2Functions {

    /** 日志记录对象 */
    private static Logger logger = Logger.getLogger(MessageBoxV2Functions.class);
    
    private static final int BACCHDELETE_ROOMMESSAGE = 1;
    private static final int SINGLEDELETE = 2;
    
    private static ActivityMessage actMessage = new ActivityMessage();
    private static RecommendMessage recMessage = new RecommendMessage();
    
    /**
     * 获取用户消息列表(50006101)
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
        
        int userId;
        int perPageCount;
        int currentPage;
        int appId;
        int maxType;
        int praiseState;
        int comState;
        
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            perPageCount = CommonUtil.getJsonParamInt(jsonObject, "perPageCount", 20, "10006104", 1, 30);//This param is optional, so pass null
            currentPage = CommonUtil.getJsonParamInt(jsonObject, "curPage", 0, "10006106", 1, 100);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 0, Integer.MAX_VALUE);
            maxType = CommonUtil.getJsonParamInt(jsonObject, "maxType", Message.MSGTYPE_SYSTEM, null, 0, Integer.MAX_VALUE);
            praiseState = CommonUtil.getJsonParamInt(jsonObject, "praiseState", 1, null, 0, Integer.MAX_VALUE);
            comState = CommonUtil.getJsonParamInt(jsonObject, "comState", 1, null, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        ArrayList<Long> lRetCount = new ArrayList<Long>();
        //获取用户消息列表
        JsonArray jsonMsgList = getUserMessageListInternal(String.valueOf(userId), perPageCount, currentPage, lRetCount, maxType, appId, praiseState, comState);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
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
     * 客户端定时刷新用户消息总数(50006102)
     * 
     * @param jsonObject 请求对象
     * @return 结果字符串
     */
    public JsonObject refreshToGetMsgTotal(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        int userId;
        int platform;
        int appId; // 默认KK娱乐
        int praiseState;
        int comState;
        
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, null, 1, 10);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 0, Integer.MAX_VALUE);
            praiseState = CommonUtil.getJsonParamInt(jsonObject, "praiseState", 1, null, 0, Integer.MAX_VALUE);
            comState = CommonUtil.getJsonParamInt(jsonObject, "comState", 1, null, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        //苹果台湾版本 去掉新鲜播报和活动通知 platform == 316  为台湾版本
        //KK游戏APP 去掉新鲜播报和活动通知 appId == 2 为KK游戏
        if (platform != PlatformEnum.IPHONE_GAMAGIC && appId != AppIdEnum.GAME) {
            Jedis jedis = null;
            try {
                jedis = UserMessageSource.getInstance();
                
                //try to generate the activity message and the recommended message if needed
                actMessage.generateActivityMessages(userId, jedis);
                
                //only new versions of client would give the parameter named maxType.
                JsonElement maxTypeje = jsonObject.get("maxType");
                if (maxTypeje != null && !maxTypeje.isJsonNull()) {
                    recMessage.generateRecommendedMessages(userId, jedis);
                }
            } catch (Exception e) {
                logger.error("generateActivityMessages or generateRecommendedMessages failed while operating redis", e);
            } finally {
                if (jedis != null) {
                    UserMessageSource.freeInstance(jedis);
                }
            }
        }

        int msgTotalCount = MessageBoxServices.getUserMessageValue(userId);
        
        //根据推送提醒判断是否推送点赞及动态回复
        int msgCount = 0;
        if (praiseState == 0 || comState == 0) {
            Jedis jedis = null;
            try {
                jedis = UserMessageSource.getInstance();
                String keyTemp = String.valueOf(userId) + "_msgsort";
                Set<String> msgsortValueList = jedis.zrange(keyTemp, Long.MIN_VALUE, Long.MAX_VALUE);
                for (Iterator<String> iter = msgsortValueList.iterator(); iter.hasNext();) {
                    String value=iter.next();
                    int msgtype = 0;
                    try {
                        msgtype = Integer.parseInt(value.split("_", 3)[1]);
                    } catch (Exception e) {
                        logger.error("get messageType failed", e);
                    }
                    if ((msgtype == Message.MSGTYPE_PRAISE && praiseState == 0) ||
                       (msgtype == Message.MSGTYPE_DYNAMIC && comState == 0) || msgtype == Message.MSGTYPE_KKASSISTANT) {
                       try {
                           Map<String, String> map = jedis.hgetAll(value);
                           msgCount += Integer.parseInt(map.get("count"));
                       } catch (Exception e) {
                           logger.error("get messageType failed", e);
                       }
                    }
                }
            } catch (Exception e) {
                logger.error("generatePraiseMessages or generateDynamicMessages failed when operate redis", e);
            } finally {
                if (jedis != null) {
                    UserMessageSource.freeInstance(jedis);
                }
            }
            
            msgTotalCount = msgTotalCount - msgCount;
        }
        
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        result.addProperty("msgTotalCount", Integer.valueOf(msgTotalCount));
        return result;
    }

    /**
     * 删除消息，包括单一删除和全部删除
     * 
     * @param jsonObject 请求对象
     * @return 结果字符串
     */
    public JsonObject deleteMsg(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        // 获取参数
        JsonElement userIdje = jsonObject.get("userId");
        JsonElement deleteTypeje = jsonObject.get("deleteType");
        JsonElement msgTypeje = jsonObject.get("msgType");
        JsonElement contextje = jsonObject.get("context");
        
        // 验证参数
        int userId;
        int deleteType = 0;
        int msgType = 0;
        int context = 0;
        if (userIdje != null && !userIdje.isJsonNull() && userIdje.getAsInt() > 0) {
            // 验证数字
            try {
                userId = userIdje.getAsInt();
            } catch (Exception e) {
                result.addProperty("TagCode", "10006109");
                return result;
            }
        } else {
            result.addProperty("TagCode", "10006110");
            return result;
        }
        if (deleteTypeje != null && !deleteTypeje.isJsonNull() && deleteTypeje.getAsInt() > 0) {
            // 验证数字
            try {
                deleteType = deleteTypeje.getAsInt();
            } catch (Exception e) {
                result.addProperty("TagCode", "10006113");
                return result;
            }
        } else {
            result.addProperty("TagCode", "10006114");
            return result;
        }
        if (msgTypeje != null && !msgTypeje.isJsonNull() && msgTypeje.getAsInt() > 0) {
            try {
                msgType = msgTypeje.getAsInt();
            } catch (Exception e) {
                result.addProperty("TagCode", "10006111");
                return result;
            }   
        }
        if (contextje != null && !contextje.isJsonNull() && contextje.getAsInt() > 0) {
            try {
                context = contextje.getAsInt();
            } catch (Exception e) {
                result.addProperty("TagCode", "10006112");
                return result;
            }   
        }
        
        Jedis jedis = UserMessageSource.getInstance();
        if (jedis == null) {
            result.addProperty("TagCode", "10006119");//批量删除失败
            return result;
        }

        try {
            //单一删除消息
            if (deleteType == MessageBoxV2Functions.SINGLEDELETE) {
                //处理舞台消息
                if (msgType != 0 && msgType == Message.MSGTYPE_ROOM) {
                    if (context != 0) {
                        try {
                            MessageBoxV2Functions.deleteSingleUserMessage(jedis, userId, msgType, context, null);
                        } catch (Exception e) {
                            logger.error("single delete failed " + e);
                            result.addProperty("TagCode", "10006116");//单一删除失败
                            return result;
                        }
                    } else {
                        result.addProperty("TagCode", "10006115"); // 10006115 : 删除舞台消息时，缺少关联的context
                        return result;
                    }
                }
            } else if (deleteType == MessageBoxV2Functions.BACCHDELETE_ROOMMESSAGE) {
                try {
                    MessageBoxV2Functions.deleteAllUserStageMessage(jedis, userId);
                } catch (Exception e) {
                    logger.error("batch delete failed " + e);
                    result.addProperty("TagCode", "10006117");//批量删除失败
                    return result;
                }
            } else {
                logger.error("the deleteType is unvalid");
                result.addProperty("TagCode", "10006118");
                return result;
            }
        } catch (Exception e) {
            logger.error("Process delete message error", e);
        } finally {
            UserMessageSource.freeInstance(jedis);
        }
        
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 获取用户最新遍历消息时间(50006103)
     * getUserLastReadTime
     */
    public JsonObject getUserLastReadTime(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        int msgType;
        int userId;
        long lastReadTime = 0;
        
        try {
            msgType = CommonUtil.getJsonParamInt(jsonObject, "msgType", 0, "10006120", Message.MSGTYPE_DYNAMIC, Message.MSGTYPE_PRAISE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "10006121", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", "10001111");
            return result;
        }
        
        Jedis jedis = null;
        try {
            jedis = UserMessageSource.getInstance();
            String strLastReadTime = MessageBoxV2Functions.getLastTime(jedis, userId, msgType, "read");
            if (strLastReadTime != null) {
                lastReadTime = Long.parseLong(strLastReadTime);
            }
        } catch (Exception e) {
            logger.error("getUserLastReadTime failed when operate redis", e);
        } finally {
            if (jedis != null) {
                UserMessageSource.freeInstance(jedis);
            }
        }
        
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        result.addProperty("lastReadTime", lastReadTime);
        return result;
    }
    
    /**
     * 遍历消息(50006104)
     * fetchMessage
     */
    public JsonObject fetchMessage(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
    
        int msgType;
        int userId;
        int platform;
        int perPageCount;
        int curPage;
        long startTime;
        long lrtFromClient;//last read time passed from client
        
        try {
            msgType = CommonUtil.getJsonParamInt(jsonObject, "msgType", 0, "10006120", Message.MSGTYPE_KKASSISTANT, Message.MSGTYPE_PRAISE);//not include stage message
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "10006121", 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, "10006122", 1, 10);
            perPageCount = CommonUtil.getJsonParamInt(jsonObject, "perPageCount", 20, null, 1, 30);//This param is optional, so pass null
            curPage = CommonUtil.getJsonParamInt(jsonObject, "curPage", 0, "10006123", 1, 100);
            startTime = CommonUtil.getJsonParamLong(jsonObject, "startTime", 0, null , 0, Long.MAX_VALUE);//this param is optional
            lrtFromClient = CommonUtil.getJsonParamLong(jsonObject, "lastReadTime", 0, null , 0, Long.MAX_VALUE);//this param is optional
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", "10001111");
            return result;
        }
        
        long retLastReadTime = 0;
        long lastReadTime = ConfigHelper.getInitLastReadTime();//1386000000000L;
        Jedis jedis = null;
        try {
            jedis = UserMessageSource.getInstance();
            if (lrtFromClient == 0) {
                String strLastReadTime = MessageBoxV2Functions.getLastTime(jedis, userId, msgType, "read");
                if (strLastReadTime != null) {
                    lastReadTime = Long.parseLong(strLastReadTime);
                 }
            } else {
                lastReadTime = lrtFromClient;
            }
            if (startTime == 0 || curPage == 1) {
                startTime = getLastMessageGenerateTime(jedis);
            }
            
            //delete the aggregated message info in the redis if startTime is not passed(=0)
            if (curPage == 1) {
                MessageBoxV2Functions.deleteSingleUserMessage(jedis, userId, msgType, 0, null);
                //新鲜播报与活动通知合并，删除时需同时删除
                if (msgType == Message.MSGTYPE_RECOMMENDED) {
                    MessageBoxV2Functions.deleteSingleUserMessage(jedis, userId, Message.MSGTYPE_ACTIVE, 0, null);
                }
                long lCurTime = new Date().getTime();
                if (lCurTime > startTime) {
                    lCurTime = startTime;
                }
                MessageBoxV2Functions.setLastTime(jedis, userId, msgType, "read", lCurTime);//update the last readtime
                retLastReadTime = lCurTime;
            }
        } catch (Exception e) {
            logger.error("fetchMessage failed when operate redis", e);
        } finally {
            if (jedis != null) {
                UserMessageSource.freeInstance(jedis);
            }
        }

        switch (msgType) {
        case Message.MSGTYPE_DYNAMIC: 
             result = MessageBoxV2Functions.getUserNewsCommentMsg(userId, platform, perPageCount, curPage, startTime, lastReadTime);
             break;
        case Message.MSGTYPE_SYSTEM:
             result = MessageBoxV2Functions.getKkSystemRecord(userId, perPageCount, curPage, startTime, platform, lastReadTime);
             break;
        case Message.MSGTYPE_RECOMMENDED:
             result = recMessage.fetchRecommendedMsg(userId, perPageCount, curPage, startTime, platform, lastReadTime);
             break;
        case Message.MSGTYPE_PRAISE: 
             result = MessageBoxV2Functions.getNewsPraiseMsg(userId, platform, perPageCount, curPage, startTime, lastReadTime);
             break;
        }
        
        if (retLastReadTime != 0) {
            result.addProperty("lastReadTime", retLastReadTime);
        }

        if (curPage == 1) {
            result.addProperty("startTime", startTime);
        }            
        return result;
    }
    
    /**
     * 获取新鲜播报消息 HTML 富文本内容 (50006106)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject getRecommendedMsgHtml(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        
        int msgId;
        
        try {
            msgId = CommonUtil.getJsonParamInt(jsonObject, "msgId", 0, "50610601", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            String htmlData = (String) SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("RecommendedMessage.getRecommendedMsgHtml", msgId);
            result.addProperty("htmlData", htmlData);
            
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("未能正常调用存储过程 RecommendedMessage.getRecommendedMsgHtml", e);
            result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
        }
        
        return result;
    }
    
    private static void setUserMessageValue(int userId, int value) {
        Jedis jedis = null;
        try {
            jedis = UserMessageSource.getInstance();

            jedis.hset("msgTotalCount", String.valueOf(userId), String.valueOf(value));
        } catch (Exception e) {
            logger.error("setUserMessageValue failed when operate redis", e);
        } finally {
            if (jedis != null) {
                UserMessageSource.freeInstance(jedis);
            }
        }
    }
    
    //根据key：userId 获取其包含的信息列表Sort Set
    private static JsonArray getUserMessageListInternal(String key, int prePageCount, int currentPage, ArrayList<Long> retCount, int maxType, int appId, int praiseState, int comState) {
        Jedis jedis = null;
        try {
            jedis = UserMessageSource.getInstance();
            JsonArray jsonArray = new JsonArray();
            String keyTemp = key + "_msgsort";
            Long lTotal = jedis.zcount(keyTemp, Double.MIN_VALUE, Double.MAX_VALUE);
            Set<String> msgsortValueList = jedis.zrange(keyTemp, (currentPage - 1) * prePageCount, currentPage * prePageCount - 1);
            for (Iterator<String> iter = msgsortValueList.iterator(); iter.hasNext();) {
                 String value=iter.next();
                 int msgtype = 0;
                 try {
                     msgtype = Integer.parseInt(value.split("_", 3)[1]);
                 } catch (Exception e) {
                     logger.error("get messageType failed", e);
                 }
                 if (msgtype == 0) {
                     continue;
                 }
                 if (msgtype == Message.MSGTYPE_RECOMMENDED && maxType < Message.MSGTYPE_RECOMMENDED) {
                     if (lTotal != null) {
                         lTotal--;
                     }
                     continue;
                 }
                 
                 //去除老接口残留的kk小秘书消息缓存
                 if (msgtype == Message.MSGTYPE_KKASSISTANT) {
                     MessageBoxV2Functions.deleteSingleUserMessage(jedis, Integer.parseInt(key), Message.MSGTYPE_KKASSISTANT, 0, null);
                     if (lTotal != null) {
                         lTotal--;
                     }
                     continue;                    
                 }
                 
                 //去除点赞消息
                 if (msgtype == Message.MSGTYPE_PRAISE && praiseState == 0) {
                     MessageBoxV2Functions.deleteSingleUserMessage(jedis, Integer.parseInt(key), Message.MSGTYPE_PRAISE, 0, null);
                     if (lTotal != null) {
                         lTotal--;
                     }
                     continue;                    
                 }
                 
                 //去除动态回复消息
                 if (msgtype == Message.MSGTYPE_DYNAMIC && comState == 0) {
                     MessageBoxV2Functions.deleteSingleUserMessage(jedis, Integer.parseInt(key), Message.MSGTYPE_DYNAMIC, 0, null);
                     if (lTotal != null) {
                         lTotal--;
                     }
                     continue;                    
                 }
                
                 JsonObject jsonObject= new JsonObject();
                 try {
                     Map<String, String> map = jedis.hgetAll(value);
                     //合并新鲜播报与活动通知
                     if (msgtype == Message.MSGTYPE_RECOMMENDED) {
                         Map<String, String> msgMap = new HashMap<String, String>();
                         msgMap = recMessage.getLastRecommendedMsg();
                         for (Iterator<String> actIter = msgsortValueList.iterator(); actIter.hasNext();) {
                             String msgsort = actIter.next();
                             int messagetype = 0;
                             try {
                                 messagetype = Integer.parseInt(msgsort.split("_", 3)[1]);
                             } catch (Exception e) {
                                 logger.error("get messageType failed", e);
                             }
                             if (messagetype == Message.MSGTYPE_ACTIVE) {
                                 Map<String, String> activeMap = jedis.hgetAll(msgsort);
                                 int recCount = Integer.parseInt(map.get("count")) + Integer.parseInt(activeMap.get("count"));
                                 map.put("count", String.valueOf(recCount));
                                 if (Long.parseLong(activeMap.get("msgtime")) > Long.parseLong(map.get("msgtime"))) {
                                     map.put("to", activeMap.get("to"));
                                     map.put("msgtime", activeMap.get("msgtime"));
                                     map.put("from", activeMap.get("from"));
                                     msgMap.put("message", activeMap.get("message"));
                                     map.put("context", activeMap.get("context"));
                                     map.put("target", activeMap.get("target"));
                                 }
                                 break;
                             }
                        }
                        jsonObject.addProperty("message", msgMap.get("message"));
                   } else if (msgtype == Message.MSGTYPE_ACTIVE) {//只有活动通知时将其合进新鲜播报
                        boolean flag = true;
                        for (Iterator<String> recIter = msgsortValueList.iterator(); recIter.hasNext();) {
                            String msgsort = recIter.next();
                            int messagetype = 0;
                            try {
                                messagetype = Integer.parseInt(msgsort.split("_", 3)[1]);
                            } catch (Exception e) {
                                logger.error("get messageType failed", e);
                            }
                            if (messagetype == Message.MSGTYPE_RECOMMENDED) {
                                flag = false;
                                break;
                            }
                        }
                        if (flag) {
                            msgtype = Message.MSGTYPE_RECOMMENDED;
                            jsonObject.addProperty("message", map.get("message"));
                        } else {
                            continue;
                        }
                    } else if (msgtype == Message.MSGTYPE_DYNAMIC || msgtype == Message.MSGTYPE_PRAISE) {//动态回复或赞消息添加昵称
                        String fuserId = map.get("from");
                        if (null != fuserId) {
                            UserProfile userInfo = com.melot.kktv.service.UserService.getUserInfoV2(Integer.parseInt(fuserId));
                            if (null != userInfo) {
                                jsonObject.addProperty("nickname", userInfo.getNickName());
                            } else {
                                jsonObject.addProperty("nickname", "");
                            }
                        } else {
                            jsonObject.addProperty("nickname", "");
                        }
                        jsonObject.addProperty("message", map.get("message"));
                    } else {
                        jsonObject.addProperty("message", map.get("message"));
                    }
                    jsonObject.addProperty("to", Integer.parseInt(map.get("to")));
                    jsonObject.addProperty("count", Integer.parseInt(map.get("count")));
                    jsonObject.addProperty("msgtime", map.get("msgtime"));
                    jsonObject.addProperty("from", Integer.parseInt(map.get("from")));
                    String strContext = map.get("context");
                    if (strContext != null && !strContext.isEmpty()) {
                        jsonObject.addProperty("context", Integer.parseInt(strContext));
                    }
                    String strTarget = map.get("target");
                    if (strTarget != null && !strTarget.isEmpty()) {
                        jsonObject.addProperty("target", Integer.parseInt(strTarget));
                    }
                } catch (Exception e) {
                    logger.error("get message hashes failed " + e);
                }
                jsonObject.addProperty("msgType", msgtype);
                jsonArray.add(jsonObject);
            }
            if (lTotal != null) {
                retCount.add(lTotal);
            }
            return jsonArray;
        } catch (Exception e) {
            logger.error("getUserMessageList failed when operate redis", e);
        } finally {
            if (jedis != null) {
                UserMessageSource.freeInstance(jedis);
            }
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    private static JsonObject getUserNewsCommentMsg(int userId, int platform, int perPageCount, int currentPage, long startTime, long lastReadTime) throws Exception {
        JsonObject result = new JsonObject();
        Date startTimer = new Date(startTime);
        Date lastReadTimer = new Date(lastReadTime);
        
        int commentTotalCount = 0;
        List<NewsComment> newsCommentList = null;
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("userId", userId);
        map.put("lastReadTime", lastReadTimer);
        map.put("startTime", startTimer);
        map.put("start", (currentPage - 1) * perPageCount);
        map.put("offset", perPageCount);
        try {
            newsCommentList = (List<NewsComment>) SqlMapClientHelper.getInstance(DBEnum.KKCX_PG).queryForList("NewsMessage.getNewsComment", map);
        } catch (SQLException e) {
            logger.error(e);
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
            return result;
        }
        if (newsCommentList.size() > 0) {
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            JsonArray jCommentList = new JsonArray();
            for (NewsComment obj : newsCommentList) {
                JsonObject jsonObj = new JsonObject();
                UserProfile userInfo = com.melot.kktv.service.UserService.getUserInfoV2(obj.getUserId());
                if (userInfo != null) {
                    jsonObj.addProperty("nickname", userInfo.getNickName());
                    jsonObj.addProperty("gender", userInfo.getGender());
                    if (null != userInfo.getPortrait()) {
                        jsonObj.addProperty("portrait_path", userInfo.getPortrait());
                        jsonObj.addProperty("portrait_path_128", userInfo.getPortrait() + "!128");
                    }
                }
                jsonObj.addProperty("userId", obj.getUserId());
                jsonObj.addProperty("message", "发起了短评");
                jsonObj.addProperty("commentContent", obj.getContent());
                if (lastReadTime <= obj.getCommentTime().getTime()) {
                    jsonObj.addProperty("isnew", Integer.valueOf(1));
                } else {
                    jsonObj.addProperty("isnew", Integer.valueOf(0));
                }
                if (obj.getUserIdTarget() != null && obj.getUserIdTarget() != 0) {
                    jsonObj.addProperty("target", obj.getUserIdTarget());
                }

                jsonObj.addProperty("msgtime", obj.getCommentTime().getTime());
                jsonObj.addProperty("id", obj.getCommentId());
                jsonObj.addProperty("newsId", obj.getNewsId());
                jCommentList.add(jsonObj);
            }
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
            result.add("messageList", jCommentList);
            if (currentPage == 1) {
                try {
                    commentTotalCount = (Integer) SqlMapClientHelper.getInstance(DBEnum.KKCX_PG).queryForObject("NewsMessage.getNewsCommentCount", map);
                } catch (SQLException e) {
                    logger.error(e);
                    JsonObject resultEx = new JsonObject();
                    resultEx.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
                    return resultEx;
                }
                result.addProperty("total", commentTotalCount);
            }
            return result;
         } else {
             result.addProperty("TagCode", TagCodeEnum.SUCCESS);
             result.add("messageList", new JsonArray());
             result.addProperty("total", 0);
             return result;
         }
    }
    
    @SuppressWarnings("unchecked")
    private static JsonObject getNewsPraiseMsg(int userId, int platform, int perPageCount, int currentPage, long startTime, long lastReadTime) throws Exception {
        JsonObject result = new JsonObject();
        Date startTimer = new Date(startTime);
        Date lastReadTimer = new Date(lastReadTime);
        int praiseTotalCount = 0;
        List<NewsPraise> newsPraiseList = null;
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("userId", userId);
        map.put("lastReadTime", lastReadTimer);
        map.put("startTime", startTimer);
        map.put("start", (currentPage - 1) * perPageCount);
        map.put("offset", perPageCount);
        try {
            newsPraiseList = (List<NewsPraise>) SqlMapClientHelper.getInstance(DBEnum.KKCX_PG).queryForList("NewsMessage.getNewsPraise", map);
        } catch (SQLException e) {
            logger.error(e);
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
            return result;
        }
        if (newsPraiseList.size() > 0) {
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            JsonArray jPraiseList = new JsonArray();
            for (NewsPraise obj : newsPraiseList) {
                JsonObject jsonObj = new JsonObject();
                UserProfile userInfo = com.melot.kktv.service.UserService.getUserInfoV2(obj.getUserId());
                if (userInfo != null) {
                    jsonObj.addProperty("nickname", userInfo.getNickName());
                    jsonObj.addProperty("gender", userInfo.getGender());
                    if (null != userInfo.getPortrait()) {
                        jsonObj.addProperty("portrait_path", userInfo.getPortrait());
                        jsonObj.addProperty("portrait_path_128", userInfo.getPortrait() + "!128");
                    }
                }
                jsonObj.addProperty("userId", obj.getUserId());
                if (userId == obj.getUserIdBelong()) {
                    jsonObj.addProperty("message","赞了您动态下的短评");
                } else {
                    jsonObj.addProperty("message","赞了您发起的短评");
                }
                jsonObj.addProperty("commentContent", obj.getCommentContent());
                if (lastReadTime <= obj.getPraiseTime().getTime()) {
                    jsonObj.addProperty("isnew", Integer.valueOf(1));
                } else {
                    jsonObj.addProperty("isnew", Integer.valueOf(0));
                }
                if (obj.getUserIdTarget() != null && obj.getUserIdTarget() != 0) {
                    jsonObj.addProperty("target", obj.getUserIdTarget());
                }
                jsonObj.addProperty("msgtime", obj.getPraiseTime().getTime());
                jsonObj.addProperty("id", obj.getHistId());
                jsonObj.addProperty("commentId", obj.getCommentId());
                jsonObj.addProperty("newsId", obj.getNewsId());
                jPraiseList.add(jsonObj);
            }
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
            result.add("messageList", jPraiseList);
            if (currentPage == 1) {
                try {
                    praiseTotalCount = (Integer) SqlMapClientHelper.getInstance(DBEnum.KKCX_PG).queryForObject("NewsMessage.getNewsPraiseCount", map);
                } catch (SQLException e) {
                    logger.error(e);
                    JsonObject resultEx = new JsonObject();
                    resultEx.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
                    return resultEx;
                }
                result.addProperty("total", praiseTotalCount);
            }
            return result;
        } else {
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            result.add("messageList", new JsonArray());
            result.addProperty("total", 0);
            return result;
        }
    }
    
    private static String getUserMessageRedisKey(int userId, int typeMsg, int roomId){
        if (typeMsg == Message.MSGTYPE_ROOM) {
            return String.format("%d_1_%d", userId, roomId);
        } else if (typeMsg <= Message.MSGTYPE_PRAISE) {
            return String.format("%d_%d", userId, typeMsg);
        }
        return null;
    }
    
    private static String getUserMessageSortSetRedisKey(int userId){
        return String.format("%d_msgsort", userId);
    }
    
    private static boolean deleteSingleUserMessage(Jedis jedis, int userId, int typeMsg, int roomId, String keyMsg0){
        if (jedis == null) {
            return false;
        }
        
        String keyMsg = null;
        if (keyMsg0 != null) {
            keyMsg = keyMsg0;
        } else {
            keyMsg = getUserMessageRedisKey(userId, typeMsg, roomId);
        }
        if (keyMsg == null) {
            return false;
        }
        
        try {
            //remove it form sorted set
            jedis.zrem(getUserMessageSortSetRedisKey(userId), keyMsg);
            
            //decrease the total number
            String userIdStr = String.valueOf(userId);
            String countStr = jedis.hget(keyMsg, "count");
            int count = 0;
            if (countStr != null) {
                count = Integer.parseInt(countStr);
            }
            int countTotal = 0;
            String countTotalStr = jedis.hget("msgTotalCount", userIdStr);
            if (countTotalStr != null) {
                countTotal = Integer.parseInt(countTotalStr);
            }
            if (countTotal < count) {
                jedis.hset("msgTotalCount",userIdStr , "0");
            } else if (count > 0) {
                jedis.hincrBy("msgTotalCount", userIdStr, -count);
            }
            
            //remove the hash
            jedis.del(keyMsg);
            
            return true;
        } catch (Exception e) {
            logger.error("deleteSingleUserMessage exception", e);
        }
        
        return false;
    }
    
    private static boolean deleteAllUserStageMessage(Jedis jedis, int userId){//delete all room message
        if (jedis == null) {
            return false;
        }

        try {
            //get all user stage message types
            ArrayList<String> toRemoveList = new ArrayList<String>();
            Set<String> userMsgTypes = jedis.zrange(getUserMessageSortSetRedisKey(userId), 0, -1);
            String stageKeyType = String.format("%d_%d_", userId, 1);
            for (Iterator<String> iter = userMsgTypes.iterator(); iter.hasNext();) {
                String valueTemp = iter.next();
                if (valueTemp.contains(stageKeyType)) {
                    toRemoveList.add(valueTemp);
                }
            }
            
            //remove all that need to be removed
            for (int i = 0; i<toRemoveList.size(); i++) {
                deleteSingleUserMessage(jedis, userId, Message.MSGTYPE_ROOM, 0, toRemoveList.get(i));
            }
            
            return true;
        } catch (Exception e) {
            logger.error("deleteSingleUserMessage exception", e);
        }
        
        return false;
    }
    
    @SuppressWarnings("unused")
    private static boolean deleteUserMessage(int userId, int deleteType, int typeMsg, int roomId) {
        Jedis jedis = UserMessageSource.getInstance();
        if (jedis != null) {
            try {
                if (deleteType == MessageBoxV2Functions.BACCHDELETE_ROOMMESSAGE) {
                    deleteAllUserStageMessage(jedis, userId);
                } else {
                    deleteSingleUserMessage(jedis, userId, typeMsg, roomId, null);
                }
                return true;
            } catch (Exception e) {
                logger.error("ActivityMessage.getUnreadCount error", e);
            } finally {
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
    
    private static boolean setUserMessages(Jedis jedis, int userId, Map<String, String> msgMap, int newCount, int msgType) {
        if(jedis == null || userId == 0 || msgMap == null || msgMap.size() == 0) {
            return false;
        }
        
        String msgKey = MessageBoxV2Functions.getUserMessageRedisKey(userId, msgType, 0);
        String sortKey = MessageBoxV2Functions.getUserMessageSortSetRedisKey(userId);

        //1. set the hash data
        int oldCount = 0;
        String strCount = jedis.hget(msgKey, "count");
        if (strCount != null) {
            try {
                oldCount = Integer.parseInt(strCount);
            } catch (Exception e) {
            }
        }
        if (newCount > 0) {
            jedis.hmset(msgKey, msgMap);
            //2. sort the message
            jedis.zadd(sortKey, Double.parseDouble(msgMap.get("msgtime")), msgKey);
        } else {
            MessageBoxV2Functions.deleteSingleUserMessage(jedis, userId, msgType, 0, null);//delete the message if the count is 0
        }
        
        //3. total count ++
        String msgOwnUserId = msgMap.get("to");
        if (jedis.hget("msgTotalCount", msgOwnUserId) != null) {
            if (newCount != oldCount) {
                jedis.hincrBy("msgTotalCount", msgOwnUserId, newCount - oldCount);
            }
        } else {
            jedis.hset("msgTotalCount", msgOwnUserId, String.valueOf(newCount));
        }
        return true;
    }
    
    private static final String REDISKEY_LASTTIME = "effLastTime";
    private static String getLastTime(Jedis jedis, int userId, int msgType, String typeName) {
        try {
            return jedis.hget(REDISKEY_LASTTIME, String.format("%d_%d_%s", userId, msgType, typeName));
        } catch (Exception e) {
            logger.error("ActivityMessage.getUnreadCount error", e);
        }
        return null;
    }
    private static void setLastTime(Jedis jedis, int userId, int msgType, String typeName, long time) {
        try {
            jedis.hset(REDISKEY_LASTTIME, String.format("%d_%d_%s", userId, msgType, typeName), String.valueOf(time));
        } catch (Exception e) {
            logger.error("ActivityMessage.getUnreadCount error", e);
        }
    }
    
    static class ActivityMessage {
        public static final String REDISKEY_EFFECTIVEACTIVITIES = "effectiveActivities";
        public static final String REDISKEY_EFFECTIVEACTIVITYIDS = "effectiveActivityIds";
        public static final String REDISKEY_EFFACTREADIDS = "effActReadIds_%d";

        private static long TIME_LAST_RESTORE = 0;
        public static String[] idListArrayEffective = new String[]{};
        
        private String[] getEffectiveActivityIds(Jedis jedis) {
            //This function only load new activity list every one day
            long queryTimestamp = System.currentTimeMillis(); 
            //check if the queryTime is beyond 1 day
            if (TIME_LAST_RESTORE == 0) {
                TIME_LAST_RESTORE = queryTimestamp;
            } else {
                if (queryTimestamp - TIME_LAST_RESTORE < ConfigHelper.getCycleEffRedisActIds()) {
                    return idListArrayEffective;
                }
                TIME_LAST_RESTORE = queryTimestamp;
            }

            String idList = jedis.get(REDISKEY_EFFECTIVEACTIVITYIDS);
            if (idList != null) {
                idListArrayEffective = idList.split(":");
            } else {
                idListArrayEffective = new String[]{};
            }
            return idListArrayEffective;
        }
        
        public void generateActivityMessages(int userId, Jedis jedisIn) {
            Jedis jedisCreated = null;
            Jedis jedis = null;
            if (jedisIn == null) {
                jedisCreated = UserMessageSource.getInstance();
                jedis = jedisCreated;
            } else {
                jedis = jedisIn;
            }
            if (jedis != null) {
                try {
                    long queryTimestamp = System.currentTimeMillis();
                    long lastGenTime = 0L;
                    try {
                        lastGenTime = Long.parseLong(getLastTime(jedis, userId, Message.MSGTYPE_ACTIVE, "Generate"));
                    } catch (Exception e) {
                    }
                    if (lastGenTime == 0) {
                        lastGenTime = queryTimestamp;
                    } else {
                        if (queryTimestamp - lastGenTime < ConfigHelper.getCycleGenAct()) {
                            return;//We only generate new activity message once every day.
                        }
                        lastGenTime = queryTimestamp;
                    }
                    setLastTime(jedis, userId, Message.MSGTYPE_ACTIVE, "Generate", lastGenTime);
                    
                    //now, seems we need to generate the activity message
                    String[] idListArray = getEffectiveActivityIds(jedis);
                    if (idListArray == null || idListArray.length == 0 || (idListArray.length == 1 && idListArray[0].isEmpty())) {
                        //no effective activity message
                        MessageBoxV2Functions.deleteSingleUserMessage(jedis, userId, Message.MSGTYPE_ACTIVE, 0, null);
                        return;
                    }
                    
                    //fetch last time info
                    List<String> eaLastValues = jedis.hmget(REDISKEY_EFFECTIVEACTIVITIES, "id", "message","msgtime");
                    if (eaLastValues.size() != 3 || eaLastValues.get(0) == null || 
                        eaLastValues.get(1) == null || eaLastValues.get(2) == null) {
//                        logger.error("eaLastValues should be 3");
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
                    if (idListRead == null) {
                        msgMap.put("count", String.valueOf(idListArray.length));
                        MessageBoxV2Functions.setUserMessages(jedis, userId, msgMap, 0, Message.MSGTYPE_ACTIVE);
                        return;
                    }
                    
                    String[] idListReadArray = null;
                    idListReadArray = idListRead.split(":");

                    int c = idListArray.length;
                    for (int i=0; i<idListArray.length; i++) {
                        if (idListArray[i].isEmpty()) {
                            continue;
                        }
                        for (int j=0; j<idListReadArray.length; j++) {
                            if (idListReadArray[j].isEmpty()) {
                                continue;
                            }
                            if (idListArray[i].compareTo(idListReadArray[j]) == 0){
                                  c--;
                            }
                        }
                    }
                    msgMap.put("count", String.valueOf(c));
                    MessageBoxV2Functions.setUserMessages(jedis, userId, msgMap, c, Message.MSGTYPE_ACTIVE);
                } catch (Exception e) {
                    logger.error("ActivityMessage.generateActivityMessages error", e);
                } finally {
                    if (jedisCreated != null) {
                          UserMessageSource.freeInstance(jedisCreated);
                    }
                }
            }
         }

        //fetch activity message
        public JsonObject fetchMessage(int userId, int platform, int perPageCount, int curPage, long startTime, long lastReadTime) {
            
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
                     if (object.isEffective(currentTime)) {
                         idReadList += object.getActivityId() + ":";
                     }
                }
                result.add("messageList", jRecordList);
                
                //update the read-ed message id list in the redis
                Jedis jedis = null;
                try {
                     jedis = UserMessageSource.getInstance();
                    
                     String[] idListArray = getEffectiveActivityIds(jedis);
                     if (idListArray != null && idListArray.length > 0) {
                         for (int i=0; i<idListArray.length; i++) {
                             if (idReadList.contains(idListArray[i])) {
                                 continue;
                             }
                             idReadList += idListArray[i] + ":";
                         }
                     }
                     jedis.set(String.format(REDISKEY_EFFACTREADIDS, userId), idReadList);
                 } catch (Exception e) {
                     logger.error("fetchMessage failed when operate redis", e);
                 } finally {
                     if (jedis != null) {
                         UserMessageSource.freeInstance(jedis);
                     }
                 }
                 return result;
              } else if (TagCode.equals("02") || TagCode.equals("03")) {
                  /* '02';分页超出范围 */
                  JsonObject result = new JsonObject();
                  result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                  result.addProperty("total", (Integer) map.get("actTotal"));
                  result.addProperty("actPageTotal", 0);
                  result.add("actList", new JsonArray());
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
     * get the KK System Record 
     * 
     */
    @SuppressWarnings("unchecked")
    private static JsonObject getKkSystemRecord(int userId, int perPageCount, int curPage, long startTime, int platform,long lastReadTime) {
        
        JsonObject result = new JsonObject();
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
            SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("KkSystemNotice.getKkSystemNoticeList", map);
        } catch (SQLException e) {
            logger.error("未能正常调用存储过程", e);
            result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
            return result;
        }

        String TagCode = (String) map.get("TagCode");
        JsonArray jsonArr = new JsonArray();
        if (TagCode.equals(TagCodeEnum.SUCCESS)) {
            List<KkSystemNotice> kSysNotice = (List<KkSystemNotice>) map.get("kkSysNotices");
            if (kSysNotice != null && kSysNotice.size() > 0) {
            	kSysNotice = UserService.addUserExtra(kSysNotice);
                for (KkSystemNotice k : kSysNotice) {
                    jsonArr.add(new JsonParser().parse(new Gson().toJson(k.toJsonObject(lastReadTime,platform))));
                }
            }
            result.addProperty("TagCode", TagCode);
            if (min==0) {
                result.addProperty("total", (Integer) map.get("totalSysNotices"));
            }
            result.add("messageList", jsonArr);
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
            return result;
        } else if (TagCode.equals("02") || TagCode.equals("03")) {
            /* '02';分页超出范围 */
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            result.addProperty("total", (Integer) map.get("totalSysNotices"));
            //result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
            result.add("messageList", new JsonArray());
            return result;
        } else {
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
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "10006124", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", "10001111");
            return result;
        }
        
        String[] msgBtnTypes = {"amBtn", "vmBtn"};
        String[] btnValues = HotDataSource.getHotFieldValues(String.valueOf(userId), msgBtnTypes);
        String tagCode = TagCodeEnum.SUCCESS;

        if (btnValues != null && btnValues.length == 2&&btnValues[0] != null && btnValues[1] !=null) {
            result.addProperty("TagCode", tagCode);
            result.addProperty("amBtn", Integer.parseInt(btnValues[0]));
            result.addProperty("vmBtn", Integer.parseInt(btnValues[1]));
        } else {
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
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        // 获取参数
        int userId;
        int amBtn = 0;
        int vmBtn = 0;
        // 验证参数
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "10006126", 1, Integer.MAX_VALUE);
            if (jsonObject.get("amBtn") != null) {
                amBtn = CommonUtil.getJsonParamInt(jsonObject, "amBtn", 0, "10006128", 1, 2);
            }
            if (jsonObject.get("vmBtn") != null) {
                vmBtn = CommonUtil.getJsonParamInt(jsonObject, "vmBtn", 0, "10006129", 1, 2);
            }
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", "10001111");
            return result;
        }
        
        try {
            if (amBtn != 0) {
                HotDataSource.setHotFieldValue(String.valueOf(userId), "amBtn", String.valueOf(amBtn));
            }
            if (vmBtn != 0) {
                HotDataSource.setHotFieldValue(String.valueOf(userId), "vmBtn", String.valueOf(vmBtn));
            }
        } catch (Exception e) {
            result.addProperty("TagCode", "10001113");//设置过程出现异常
            return result;
        }
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    static class RecommendMessage {
            
        public static final String REDISKEY_LASTRECOMMENDMSG = "lastRecommendMsg";
        public static final String REDISKEY_TOTALRECOMMENDMSG = "totalRecommendMsg";
        
        private Map<String,String> getLastRecommendedMsg(Jedis jedis) {  
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
        
        public Map<String,String> getLastRecommendedMsg() {
            Jedis jedis = null; 
            jedis = UserMessageSource.getInstance();
            if (jedis != null) {
                try {
                    return getLastRecommendedMsg(jedis);
                } catch(Exception e) {
                    logger.error("Errors occurred in RecommendedMessage.getLastRecommendedMsg static", e);
                } finally {
                    UserMessageSource.freeInstance(jedis);
                }
            }
            return null;
        }
        
        private int getRecommendedMsgGross(Jedis jedis) {
            String val = jedis.get(REDISKEY_TOTALRECOMMENDMSG);
            if (val!=null) {
                int msgGross = Integer.valueOf(val);
                return msgGross;
            }
            return 0;
        }
        
        public void generateRecommendedMessages(int userId, Jedis jedisIn) {
            Jedis jedisCreated = null;
            Jedis jedis = null;
            if (jedisIn == null) {
                jedisCreated = UserMessageSource.getInstance();
                jedis = jedisCreated;
            } else {
                jedis = jedisIn;
            }
            if (jedis != null) {
                try {
                    long queryTimestamp = System.currentTimeMillis();
                    long lastGenTime = 0L;
                    try {
                        lastGenTime = Long.parseLong(getLastTime(jedis, userId, Message.MSGTYPE_RECOMMENDED, "Generate"));
                    } catch (Exception e) {
                    }
                    if (lastGenTime == 0) {
                        lastGenTime = queryTimestamp;
                    } else {
                        if (queryTimestamp - lastGenTime < ConfigHelper.getCycleGenAct()) {
                            return;
                        }
                        lastGenTime = queryTimestamp;
                    }
                    setLastTime(jedis, userId, Message.MSGTYPE_RECOMMENDED, "Generate", lastGenTime);
                    
                    int c = recMessage.getRecommendedMsgGross(jedis);
                    int readCount = 0;
                    try {
                        String readCountStr = getLastTime(jedis, userId, Message.MSGTYPE_RECOMMENDED, "readCount");
                        if (readCountStr != null) {
                            readCount = Integer.parseInt(readCountStr);
                        } else {
                            long lCurTime = new Date().getTime();
                            readCount = c;//第1次(可能是新注册用户)，标记所有消息为已读
                            MessageBoxV2Functions.setLastTime(jedis, userId, Message.MSGTYPE_RECOMMENDED, "read", lCurTime);//update the last readtime
                        }
                    } catch (Exception e) {
                    }
                    //Get the latest recommended message and the gross of recommended messages.
                    Map<String, String> msgMap = new HashMap<String, String>();
                    msgMap = recMessage.getLastRecommendedMsg(jedis);
                    msgMap.put("message", "");//we will not save the message, to reduce the memory use of redis
                    msgMap.put("to", String.valueOf(userId));
                    msgMap.put("from", msgMap.get("msgid"));
                    msgMap.put("context", "");
                    c -= readCount;
                    if (c < 0) {
                        c = 0;
                    }
                    msgMap.put("count", String.valueOf(c));
                    MessageBoxV2Functions.setUserMessages(jedis, userId, msgMap, c, Message.MSGTYPE_RECOMMENDED);

                } catch (Exception e) {
                    logger.error("Errors occurred in RecommendedMessage.generateRecommendedMessages", e);
                } finally {
                    if (jedisCreated != null) {
                        UserMessageSource.freeInstance(jedisCreated);
                    }
                }
            }
        }
        
        /**
         *获取精彩推荐 
         * 
         */
        public JsonObject fetchRecommendedMsg(int userId, int perPageCount, int curPage, long startTime, int platform, long lastReadTime) {
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
                SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("RecommendedMessage.getRecommendedMsgs", map);
            } catch (SQLException e) {
                logger.error("未能正常调用存储过程", e);
                JsonObject result = new JsonObject();
                result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
                return result;
            }
            String TagCode = (String) map.get("TagCode");
            JsonArray jsonArr = new JsonArray();
            if (TagCode.equals(TagCodeEnum.SUCCESS)) {
                long currentTime = new Date().getTime();
                String idReadList = new String();
                JsonObject result = new JsonObject();
                @SuppressWarnings("unchecked")
                List<RecommendedMsg> kRecommendedMsg = (ArrayList<RecommendedMsg>) map.get("rcmList");
                if (kRecommendedMsg != null && kRecommendedMsg.size() > 0) {
                    for (RecommendedMsg k : kRecommendedMsg) {
                        jsonArr.add(new JsonParser().parse(new Gson().toJson(k.toJsonObject(lastReadTime, platform,currentTime))));
                        
                        //消息类型若是活动通知，需添加到已读活动id
                        if (null != k.getActivityId() && k.isEffective(currentTime)) {
                            idReadList += k.getActivityId() + ":";
                        }
                    }
                }
                
                Jedis jedis = null;
                try {
                    jedis = UserMessageSource.getInstance();
                    
                    //update the read count.
                    int recTotal = recMessage.getRecommendedMsgGross(jedis);
                    setLastTime(jedis, userId, Message.MSGTYPE_RECOMMENDED, "readCount", recTotal);
                    
                    //update the read-ed message id list in the redis
                    String[] idListArray = actMessage.getEffectiveActivityIds(jedis);
                    if (idListArray != null && idListArray.length > 0) {
                        for (int i=0; i < idListArray.length; i++) {
                             if (idReadList.contains(idListArray[i])) {
                                 continue;
                             }
                             idReadList += idListArray[i] + ":";
                        }
                    }
                    jedis.set(String.format(ActivityMessage.REDISKEY_EFFACTREADIDS, userId), idReadList);
                } catch (Exception e) {
                    logger.error("fetchMessage failed when operate redis", e);
                } finally {
                    if (jedis != null) {
                        UserMessageSource.freeInstance(jedis);
                    }
                }
                
                //return the result
                result.addProperty("TagCode", TagCode);
                if (min == 0) {
                    result.addProperty("total", (Integer) map.get("rcmTotal"));
                }
                result.add("messageList", jsonArr);
                return result;

            } else if (TagCode.equals("02") || TagCode.equals("03")) {
                /* '02';分页超出范围 */
                JsonObject result = new JsonObject();
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                result.addProperty("total", (Integer) map.get("rcmTotal"));
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
    
    /**
     * 标记所有消息为已读(50006105)
     * fetchMessage
     */
    public JsonObject markAllMessagesRead(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
    
        int userId;
        long startTime;
        long lastReadTime = 0;
        
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        Jedis jedis = null;
        try {
            jedis = UserMessageSource.getInstance();
            startTime = getLastMessageGenerateTime(jedis);
            long lCurTime = new Date().getTime();
            if (lCurTime > startTime) {
                lCurTime = startTime;
            }
            //所有消息类型
            int [] messageType = {Message.MSGTYPE_DYNAMIC, Message.MSGTYPE_SYSTEM, Message.MSGTYPE_RECOMMENDED, Message.MSGTYPE_PRAISE};
            //删除缓存消息列表消息
            for (int msgType : messageType) {
                MessageBoxV2Functions.deleteSingleUserMessage(jedis, userId, msgType, 0, null);
                //新鲜播报与活动通知合并，删除时需同时删除
                if (msgType == Message.MSGTYPE_RECOMMENDED) {
                    MessageBoxV2Functions.deleteSingleUserMessage(jedis, userId, Message.MSGTYPE_ACTIVE, 0, null);
                    int recTotal = recMessage.getRecommendedMsgGross(jedis);
                    setLastTime(jedis, userId, Message.MSGTYPE_RECOMMENDED, "readCount", recTotal);
                }
                MessageBoxV2Functions.setLastTime(jedis, userId, msgType, "read", lCurTime); //更新最后一次读消息时间
            }
            lastReadTime = lCurTime;
        } catch (Exception e) {
            logger.error("markAllMessagesRead failed when operate redis", e);
            result.addProperty("TagCode", "06050001");
        } finally {
            if (jedis != null) {
                UserMessageSource.freeInstance(jedis);
            }
        }
        
        if (lastReadTime != 0) {
            result.addProperty("lastReadTime", lastReadTime);
        }
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
}

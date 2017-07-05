/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.action;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.api.menu.sdk.dao.RoomSubCatalogDao;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.api.menu.sdk.utils.Collectionutils;
import com.melot.game.config.sdk.domain.ShareRecordInfo;
import com.melot.game.config.sdk.domain.UserSignInfo;
import com.melot.game.config.sdk.shared.service.ShareRecordService;
import com.melot.game.config.sdk.sign.service.UserSignInfoService;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kkcx.service.UserAssetServices;
import com.melot.kkcx.service.UserService;
import com.melot.kkcx.transform.RoomTF;
import com.melot.kkgame.logger.HadoopLogger;
import com.melot.kkgame.redis.SignInTaskSource;
import com.melot.kkgame.redis.support.RedisException;
import com.melot.kktv.model.Room;
import com.melot.kktv.service.RoomService;
import com.melot.kktv.service.UserRelationService;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * Title: GameTaskAction
 * <p>
 * Description: 直播系统任务系统接口
 * </p>
 * @author  姚国平<a href="mailto:guoping.yao@melot.cn">
 * @version V1.0
 * @since 2015-4-8 下午3:22:23 
 */
public class GameTaskAction extends BaseAction{
    
    /**
     *
     */
    private static final int DEFAULT_ALL_ACTOR_PART_ID = 46;
    private static Logger logger = Logger.getLogger(GameTaskAction.class);
    private static final String PIC_DOMAIN = "http://ures.kktv8.com/kktv";
    private static final Integer DEFALUT_START_PAGE = 1;
    private SignInTaskSource signInTaskSource;
    
    public void setSignInTaskSource(SignInTaskSource signInTaskSource) {
        this.signInTaskSource = signInTaskSource;
    }
    
    /**
     * 用户点击签到  [fucTag=20040001]
     * 
     */
    public JsonObject userSignIn(JsonObject jsonObject,boolean checkTag, HttpServletRequest request){
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty(TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        Integer roomId = null;
        Integer userId = null;
        int platform = 0;
        int appId = 0;
        UserSignInfoService userSignInfoService = null;
        try {
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.GAME, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 2, null, 1, Integer.MAX_VALUE); //默认platform =2 android
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(TAG_CODE, e.getErrCode());
            return result;
        }
        
        userId = UserAssetServices.idToUserId(userId);
        roomId = UserAssetServices.idToUserId(roomId);
        
        UserProfile userInfo = com.melot.kktv.service.UserService.getUserInfoV2(userId);
        if (userInfo == null) {
            result.addProperty("TagCode", TagCodeEnum.USER_NOT_EXIST);
            return result;
        }
        
        UserProfile roomInfo = com.melot.kktv.service.UserService.getUserInfoV2(roomId);
        if (roomInfo == null) {
            result.addProperty("TagCode", TagCodeEnum.USER_NOT_EXIST);
            return result;
        }
        
        boolean signFlag = false;
        try {
            userSignInfoService = MelotBeanFactory.getBean("userSignInfoService", UserSignInfoService.class);
            if (userId != roomId) {
                if (!UserRelationService.follow(userId, roomId)) {
//                    logger.error("用户"+userId+"已经关注用户"+roomId+".");
                } else {
                    int followFansCount = UserRelationService.getFansCount(roomId);
                    JsonObject tempJsonObject = new JsonObject();
                    // 添加粉丝数返回
                    tempJsonObject.addProperty("followId", roomId);
                    tempJsonObject.addProperty("followFansCount", followFansCount);                
                    // 推送房间关注数变化给房间
                    JsonObject msg = new JsonObject();
                    msg.addProperty("MsgTag", 10010321);
                    msg.addProperty("count", followFansCount);
                    com.melot.kkgame.service.RoomService.sendMsgToRoom(4, roomId, 0, msg);
                }
            }
            signFlag = userSignInfoService.toSignIn(userId, roomId);
        } catch (Exception e) {
            logger.error("Fail to call extraRoomInfoService.sumitApply ", e);
        }
        try {
            if (signFlag) {
                //update redis;
                logger.info("userId["+userId+"] sign in,time="+new Date());
                UserSignInfo userSignInfo = userSignInfoService.getUserSignInfoByUserIdAndRoomId(userId, roomId);
                int totalSign = userSignInfo.getAgainTimes();
                int rankInRoom = userSignInfoService.getRankignInRoom(userId, roomId); //获取当前用户在房间的签到排名
                signInTaskSource.userSignInToday(userId, roomId, userSignInfo.getAgainTimes(), rankInRoom);
                signInTaskSource.userFirstSignInToday(userId);
                Calendar cal = GregorianCalendar.getInstance();
                logger.info("签到流水表记录操作：所需参数：userid:"+ userId +","+ "rooomId:" +roomId+", dYear:"+cal.get(Calendar.YEAR)+", dMonth:"+cal.get(Calendar.MONTH));
                HadoopLogger.signInLog(userId, roomId, new Date(), appId, platform);
                
                result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
                result.addProperty("signTimes",totalSign );
                result.addProperty("signRank",rankInRoom );
            } else {
                //调用发生错误
                if(signInTaskSource.hasSignIn(userId, roomId) != null){
                    //本日已经签到
                    result.addProperty(TAG_CODE, TagCodeEnum.GET_USER_LOGIN_TIME_FAIL);
                } else {
                    result.addProperty(TAG_CODE, TagCodeEnum.IRREGULAR_RESULT);
                }
            }
        } catch (RedisException e) {
            result.addProperty(TAG_CODE, TagCodeEnum.PROCEDURE_EXCEPTION);
            logger.error("调用redis发生异常", e);
        }
      
        return result;
    }
    
    /**
     * 房间签到排行榜  [fucTag=20040002]
     * 
     */
    public JsonObject getRoomSignRankingList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request){
        JsonObject result = new JsonObject();
        
        Integer roomId = null;
        Integer num = null;
        try {
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
            num = CommonUtil.getJsonParamInt(jsonObject, "num", 8, null, 0, 20);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(TAG_CODE, e.getErrCode());
            return result;
        }
        
        try {
            Set<String>rankSet = signInTaskSource.getRoomSignRankingList(roomId);
            if (rankSet == null || rankSet.size() == 0) { //redis未获取到数据
            	UserSignInfoService userSignInfoService = MelotBeanFactory.getBean("userSignInfoService", UserSignInfoService.class);
                List<UserSignInfo>list = userSignInfoService.getUserSignRankingList(roomId,num);
                if (list == null || list.size() == 0 ) {//未获取到签到列表
                    result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
                    result.add("rankList", new JsonArray());
                    return result;
                }
                JsonArray jsonArray = new JsonArray();
                for (UserSignInfo userSignInfo : list) {
                    JsonObject json = new JsonObject();
                    KkUserService kkUserService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
                    UserProfile userProfile = kkUserService.getUserProfile(userSignInfo.getUserId());
                    json.addProperty("userId", userSignInfo.getUserId());
                    json.addProperty("signTimes", userSignInfo.getAgainTimes());
                    json.addProperty("nickName", userProfile.getNickName());
                    json.addProperty("portraitPath",userProfile.getPortrait()==null? null:PIC_DOMAIN + userProfile.getPortrait());
                    json.addProperty("richLevel", userProfile.getUserLevel());
                    jsonArray.add(json);
                }
                signInTaskSource.addRoomSingRankingList(roomId, jsonArray);//添加列表到redis;
                rankSet = signInTaskSource.getRoomSignRankingList(roomId); //重新获取热点信息
            }
            //构建接口返回
            JsonParser jsonParser = new JsonParser();
            JsonArray jsonArray = new JsonArray();
            for (String string : rankSet) {
                JsonElement element = jsonParser.parse(string);
                jsonArray.add(element.getAsJsonObject());
                if(jsonArray.size() == num){
                    break;
                }
            }
            result.add("rankList", jsonArray);
            result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("Fail to call userSignInfoService.getUserSignRankingList ", e);
        }
        return result;
    }
    
    /**
     * 获取用户在房间今日是否签到状态   [fucTag=20040003]
     * 
     */
    public JsonObject getUserSignInState (JsonObject jsonObject, boolean checkTag, HttpServletRequest request){
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty(TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        Integer roomId = null;
        Integer userId = null;
        try{
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, TagCodeEnum.ROOMID_MISSING, 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        }catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(TAG_CODE, e.getErrCode());
            return result;
        }
        
        String signInfo = null;
        try {
            signInfo = signInTaskSource.hasSignIn(userId, roomId);
        } catch (Exception e) {
            logger.error("Fail to call extraRoomInfoService.sumitApply ", e);
        }
        result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
        if (signInfo != null) {
            String[] infos = signInfo.split("_");
            result.addProperty("state", 1);
            result.addProperty("signTimes", infos[0]);
            result.addProperty("signRank", infos[1]);           
        } else {
            result.addProperty("state", 0);
        }
        
        return result;
    }
    
    /**
     * 用户分享任务[funcTag=20040004]
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject share(JsonObject jsonObject, boolean checkTag, HttpServletRequest request){
    	JsonObject result = new JsonObject();
    	
    	Integer userId=null;
    	Integer platform=null;
    	Integer sharedType=null;
    	Integer sharedSourceId=null;
    	Integer sharedPlatform=null;
    	String shareLink="";
    	String shareTitle="";
    	String shareReason="";
    	Date shareTime = new Date();
    	try {
    		userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
    		platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
    		sharedType = CommonUtil.getJsonParamInt(jsonObject, "sharedType", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
    		sharedSourceId = CommonUtil.getJsonParamInt(jsonObject, "sharedSourceId", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
    		shareLink = CommonUtil.getJsonParamString(jsonObject, "shareLink", "",TagCodeEnum.APPID_MISSING, 0, Integer.MAX_VALUE);
    		shareTitle = CommonUtil.getJsonParamString(jsonObject, "shareTitle", "",TagCodeEnum.APPID_MISSING, 0, Integer.MAX_VALUE);
    		shareReason = CommonUtil.getJsonParamString(jsonObject, "shareReason", "",TagCodeEnum.APPID_MISSING, 0, Integer.MAX_VALUE);
    		sharedPlatform = CommonUtil.getJsonParamInt(jsonObject, "sharedPlatform", 1,TagCodeEnum.APPID_MISSING, 0, Integer.MAX_VALUE);
    	} catch (CommonUtil.ErrorGetParameterException e) {
    		result.addProperty(TAG_CODE, e.getErrCode());
	    	return result;
    	}
    	
    	ShareRecordInfo shareRecord= new ShareRecordInfo();
    	shareRecord.setUserId(userId);
    	shareRecord.setPlatform(platform);
    	shareRecord.setSharedType(sharedType);
    	shareRecord.setSharedSourceId(sharedSourceId);
    	shareRecord.setShareLink(shareLink);
    	shareRecord.setShareTitle(shareTitle);
    	shareRecord.setShareReason(shareReason);
    	shareRecord.setShareTime(shareTime);
    	shareRecord.setSharedPlatform(sharedPlatform);
    	ShareRecordService shareRecordService = MelotBeanFactory.getBean("shareRecordService", ShareRecordService.class);
    	try {
    		if (!signInTaskSource.isHaveShareInTaskValue(userId)) {
    			shareRecordService.saveShareRecordInfo(shareRecord);
    			signInTaskSource.setShareVieldValue(userId);
    		}
    		result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
    	} catch (Exception e) {
            logger.error("Fail to call shareRecordService.saveShareRecordInfo ", e);
        }
    	
    	return result;
    }
    
    /**
     * 用户分享任务查询[funcTag=20040005]
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject checkShareState(JsonObject jsonObject, boolean checkTag, HttpServletRequest request){
    	JsonObject result = new JsonObject();
    	
    	Integer userId = null;
    	try {
    		userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
    	} catch (CommonUtil.ErrorGetParameterException e) {
    		result.addProperty(TAG_CODE, e.getErrCode());
	    	return result;
    	}
    	
    	try {
    		result.addProperty("state",signInTaskSource.isHaveShareInTaskValue(userId) ? "1":"0");
        	result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
    	} catch (Exception e) {
            logger.error("Fail to call shareRecordService.saveShareRecordInfo ", e);
        }
    	
    	return result;
    }
    
    /**
     * 用户今日是否签到 [fucTag=20040007]
     * 如未签到则推荐最近签过的房间进行签到 
     * 
     */
   public JsonObject getInterestSignRoomByTodayRoomState(JsonObject jsonObject, boolean checkTag, HttpServletRequest request){
       JsonObject result = new JsonObject();
       if (!checkTag) {
            result.addProperty(TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
       
       Integer userId = null;
       UserSignInfoService userSignInfoService = null;
       int platform;
       try {
           userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
           platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, 1, Integer.MAX_VALUE);
       } catch (CommonUtil.ErrorGetParameterException e) {
           result.addProperty(TAG_CODE, e.getErrCode());
           return result;
       }
       
       String signInfo = null;
       try {
           signInfo = signInTaskSource.oneUserHasSignInToday(userId);
       } catch (RedisException e) {
           logger.error("调用redis发生异常", e);
       }
       if (signInfo != null) {
           result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
           result.addProperty("signState", 1);
       } else {
           JsonArray roomArray = new JsonArray();
           result.addProperty("signState", 0);
           userSignInfoService = MelotBeanFactory.getBean("userSignInfoService", UserSignInfoService.class);
           List<UserSignInfo> list = userSignInfoService.getUserSignInfoListByUserId(userId, 10);
           if (!Collectionutils.isEmpty(list)) {
               Collections.shuffle(list);
               RoomInfo roomInfo = RoomService.getRoomInfo(list.get(0).getRoomId());
               JsonObject json = RoomTF.roomInfoToJsonTemp(roomInfo, platform);
               roomArray.add(json);
           } else {
               RoomSubCatalogDao roomSubCatalogDao = MelotBeanFactory.getBean("roomSubCatalogDao", RoomSubCatalogDao.class);
               List<RoomInfo> roomList = roomSubCatalogDao.getPartRoomList(DEFAULT_ALL_ACTOR_PART_ID, 0, 1);
               JsonObject json = RoomTF.roomInfoToJsonTemp(roomList.get(0), platform);
               roomArray.add(json);
           }
           
           result.add("roomList", roomArray);
           result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
           result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
       }
       
       return result;
   }
   
   
   /**
    * 用户分享推荐 [fucTag=20040008]
    * 分享顺序为 关注正在直播的 > 关注未直播 > 人气主播(cataId = 46)
    */
    public JsonObject getSharingFollowList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request){
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty(TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        Integer userId, pageIndex = DEFALUT_START_PAGE, countPerPage, platform;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 0, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 8, null, 1, 20);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(TAG_CODE, e.getErrCode());
            return result;
        }
        
        List<RoomInfo> goalRoomlist = null ;
        JsonArray jRoomList = new JsonArray();
        goalRoomlist = getUserFollowRooms(userId, pageIndex, countPerPage);
        // 调用存储过程得到结果
        for (RoomInfo roomInfo : goalRoomlist) {
            JsonObject json = RoomTF.roomInfoToJson(roomInfo, platform, true);
            jRoomList.add(json);
        }
        if (jRoomList.size() < countPerPage) {  //应为推荐了未开播等原因, 需要补足
            RoomSubCatalogDao roomSubCatalogDao = MelotBeanFactory.getBean("roomSubCatalogDao", RoomSubCatalogDao.class);
            List<RoomInfo> topActors = com.melot.kkgame.service.RoomService.getTopActors(roomSubCatalogDao);
            for (RoomInfo roomInfo : topActors) {
                if (!goalRoomlist.contains(roomInfo)) {
                    JsonObject json = RoomTF.roomInfoToJson(roomInfo, platform, true);
                    jRoomList.add(json);
                }
                if (jRoomList.size() == countPerPage) {
                    break;
                }
            }
        }
        
        result.add("roomList", jRoomList);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     *  获取用户关注主播房间 
     */
    @SuppressWarnings("unchecked")
    private List<RoomInfo> getUserFollowRooms(int userId,int pageIndex, int followNum){
        int followsCount = UserRelationService.getFollowsCount(userId);
        if(followsCount == 0 ){
            return new ArrayList<RoomInfo>();
        }
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put("userId", userId);
        map.put("pageIndex", pageIndex);
        map.put("countPerPage", followNum);
        try {
            SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("UserRelation.getUserFollowedList", map);
        } catch (SQLException e) {
            logger.error("未能正常调用存储过程", e);
        }
        if (TagCodeEnum.SUCCESS.equals(map.get("TagCode"))) {
            List<Room> roomList = (ArrayList<Room>) map.get("roomList");
            if (roomList != null && roomList.size() > 0) {
            	roomList = UserService.addUserExtra(roomList);
                List<Integer> actorIds = new ArrayList<Integer>();
                StringBuffer actorIds2 = new StringBuffer();
                for (Room room : roomList) {
                    if (room.getActorTag() != null && room.getActorTag().intValue() == 1) {
                        actorIds.add(room.getUserId());
                        actorIds2.append(room.getUserId());
                        actorIds2.append(",");
                    }
                }
                if (actorIds2.length() > 0) {
                  return  RoomService.getRoomListByRoomIds(actorIds2.substring(0, actorIds2.length() - 1));
                }
            }
        }
        return new ArrayList<RoomInfo>();
    }
}

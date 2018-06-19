/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.action;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import com.melot.kktv.util.*;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.game.config.sdk.domain.GiftStarRecord;
import com.melot.game.config.sdk.gift.service.GiftStarRecordService;
import com.melot.kkgame.domain.GiftInfo;
import com.melot.kkgame.redis.GiftStarSource;
import com.melot.kkgame.redis.support.RedisException;
import com.melot.kkgame.service.KgGiftService;

/**
 * Title: WeekStarFunction
 * <p>
 * Description: 周星礼物相关接口
 * </p>
 * @author  姚国平<a href="mailto:guoping.yao@melot.cn">
 * @version V1.0
 * @since 2015-10-13 上午10:47:41 
 */
public class WeekStarFunction extends BaseAction{

    private static Logger logger = Logger.getLogger(WeekStarFunction.class);
    
    private GiftStarRecordService giftStarRecordService;
    private GiftStarSource giftStarSource;

    private String curentDay;
    private List<GiftInfo> giftConfigCache = null;
    
    /**
     * 查询主播上周获得的周星(20080001)
     * @param jsonObject
     * @return
     */
	public JsonObject getActorStartInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		JsonObject result = new JsonObject();

		int roomId;
		try {
			roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
		} catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch (Exception e) {
			logger.error("getActorStartInfo", e);
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		
		JsonArray honorList = new JsonArray();
		Calendar ca = Calendar.getInstance();
		ca.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		ca.add(Calendar.DATE, -7);
		String dateStr = DateUtil.formatDate(ca.getTime(), "yyyyMMdd");
		List<GiftStarRecord> giftList = giftStarRecordService.getGiftStarRecordsByParameters(roomId, 1, 1, dateStr);
		if (giftList != null && giftList.size() > 0) {
			for (GiftStarRecord gift : giftList) {
				JsonObject jsonObj = new JsonObject();
				jsonObj.addProperty("giftId", gift.getGiftId());
				jsonObj.addProperty("giftName", gift.getGiftName());
				honorList.add(jsonObj);
			}
		}
		result.addProperty("roomId", roomId);
		result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
		result.add("honorList", honorList);
		
		return result;
	}
    
    
    /**
     * 查询单个周星礼物的排行信息(20080002)
     * @param jsonObject
     * @return
     */
	public JsonObject getWeekGiftRankInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		JsonObject result = new JsonObject();
		
		int userId; // 查询当前用户作为消费者财富榜排行位置
		int roomId; // 当前所在房间, 用于查询房间主播所在排行榜榜单位置
		int giftId;
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
			roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
			giftId = CommonUtil.getJsonParamInt(jsonObject, "giftId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
		} catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch (Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		
		Calendar ca = Calendar.getInstance();
		ca.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
		String dateStr = DateUtil.formatDate(ca.getTime(), "yyyyMMdd");
		try {
			if (checkTag) {
				Map<String, Integer> map = getRankMap(dateStr, 2, giftId, userId);
				if (map != null) {
					result.addProperty("userId", userId);
					result.addProperty("userRank", map.get("rank"));
					result.addProperty("userDistance", map.get("distance"));
				}
			}
			Map<String, Integer> map = getRankMap(dateStr, 1, giftId, roomId);
			if (map != null) {
				result.addProperty("roomId", roomId);
				result.addProperty("roomRank", map.get("rank"));
				result.addProperty("roomDistance", map.get("distance"));
			}
		} catch (RedisException e) {
			logger.error("RedisException error", e);
		}

		result.addProperty("giftId", giftId);
		result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
		return result;
	}
    
    
    /**
     * 获取主播本周周星礼物的排行信息(20080003)
     * @param jsonObject
     * @return
     */
	public JsonObject getActorWeekGiftRankInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		JsonObject result = new JsonObject();
		int userId; // 当前所在房间, 用于查询房间主播所在排行榜榜单位置
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
		} catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		}
		
		JsonArray jsonArray = null;
		try {
		    jsonArray = generateUserGiftRankingList(userId);
		} catch (Exception e) {
			logger.error("Fail to call getActorWeekGiftRankInfo cache, ");
		}
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		result.add("userGiftRankingList", jsonArray);

		return result;
	}
    
    /**
     *  查询获取周星数据 
     * 
     */
    private JsonArray generateUserGiftRankingList(Integer userId){
        JsonArray jsonArray  = new JsonArray();
        //先获取本周配置的周星礼物
        Calendar ca = Calendar.getInstance();
        ca.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        String dateStr = DateUtil.formatDate(ca.getTime(), "yyyyMMdd");
        List<GiftInfo>gifts = getGiftConfigCache(dateStr);
        if (CollectionUtils.isEmpty(gifts)) {
            return jsonArray;
        }
        JsonObject json = null;
        try {
            List<JsonObject>jsons = new ArrayList<JsonObject>();
            for (GiftInfo giftInfo : gifts) {
                Double score = giftStarSource.getUserScore(dateStr, 1, giftInfo.getGiftId(), userId);
                if(score == null){ //当前用户没有上榜
                   continue;
                }
                json = new JsonObject();
                int point = score.intValue();
                List<GiftStarRecord> list= giftStarSource.getRecordFromRedis(dateStr, 1, giftInfo.getGiftId(), point + 1);
                json.addProperty("ranking", 1);
                if(CollectionUtils.isEmpty(list)){  //当前为第一名
                    json.addProperty("ranking", 1);
                    json.addProperty("upDiff", 0);
                    json.addProperty("needMore", 0);
                }else{
                    int ranking =  list.size() + 1;
                    if(ranking > 100 ){
                        continue; //超过20 不返回
                    }
                    json.addProperty("ranking",ranking);
                    json.addProperty("needMore", list.get(list.size()-1).getGiftCount() - point);
                    json.addProperty("upDiff", list.get(list.size()-1).getGiftCount() - point);
                }
                json.addProperty("total", point);
                json.addProperty("userId", userId);
                json.addProperty("giftId", giftInfo.getGiftId());
                json.addProperty("giftName", giftInfo.getGiftName());
                json.addProperty("androidIcon", ConfigHelper.getKkDomain() + "/icon/android/gift/icon/" + giftInfo.getGiftId() + ".png");
                json.addProperty("iphoneIcon", ConfigHelper.getKkDomain() + "/icon/iphone/gift/icon/" + giftInfo.getGiftId() + ".png");
                jsons.add(json);
            }
            Collections.sort(jsons, new Comparator<JsonObject>() {
            	@Override
                public int compare(JsonObject o1, JsonObject o2) {
                    if(o1.get("ranking").getAsInt() >= o2.get("ranking").getAsInt() ){
                        return 1;
                    }
                    return 0;
                }
            });
            
            for (JsonObject jsonObject : jsons) {
                jsonArray.add(jsonObject);
            }
        } catch (RedisException e) {
            e.printStackTrace();
        }
        return jsonArray;
        
    }
    
    
    
    /**
     *  获取当前时间配置的 周星礼物列表
     * 
     */
    private List<GiftInfo> getGiftConfigCache(String dateString){
        if( !dateString.equals(curentDay) || CollectionUtils.isEmpty(giftConfigCache)){
            giftConfigCache = KgGiftService.getWeekStarGiftInfoByTime(new Date());
            curentDay = dateString;
        }
        return giftConfigCache;
    }
    
    /**
     *  解析获取当前redis排行榜列表 
     * 
     */
    private Map<String, Integer>  getRankMap(String dateStr,Integer userType, Integer giftId,Integer userId) throws RedisException{
        Double score = giftStarSource.getUserScore(dateStr, userType, giftId, userId);
        if(score == null){ //当前用户没有上榜
            return null;
        }
        int point = score.intValue();
        Map<String, Integer> map = new HashMap<String, Integer>();
        List<GiftStarRecord> list= giftStarSource.getRecordFromRedis(dateStr, userType, giftId, point + 1);
        if(CollectionUtils.isEmpty(list)){
            //当前为第一名
            map.put("rank", 1);
            map.put("distance", 0);
        }else{
            map.put("rank", list.size() + 1 );
            map.put("distance", list.get(list.size()-1).getGiftCount() - point);
        }
        return map;
    }
    
	/**
	 * @param giftStarRecordService the giftStarRecordService to set
	 */
	public void setGiftStarRecordService(GiftStarRecordService giftStarRecordService) {
		this.giftStarRecordService = giftStarRecordService;
	}
	/**
	 * @param giftStarSource the giftStarSource to set
	 */
	public void setGiftStarSource(GiftStarSource giftStarSource) {
		this.giftStarSource = giftStarSource;
	}

}

/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.action.sport;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.game.config.sdk.domain.EventPreviewInfo;
import com.melot.game.config.sdk.event.service.EventPreviewInfoService;
import com.melot.kkgame.action.BaseAction;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.DateUtil;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * Title: EventFunction
 * <p>
 * Description: 
 * </p>
 * 
 * @author 陈磊<a href="mailto:lei.chen@melot.cn">
 * @version V1.0
 * @since 2015-9-2 上午9:56:42
 */
public class EventFunction extends BaseAction{

    private static final Logger logger = Logger.getLogger(EventFunction.class);
    private static final Integer USEABLE_IN_OPEN = 1;
    
    /** 30天的毫秒数 */
    private static final long MILLISECONDS_IN_MONTH = 60 * 60 * 24 * 30 * 1000l;
    
    
    /**
     * 根据赛程主键eventId获取预告节目详情
     * @param jsonObject:{eventId:130}
     * @return
     * functag:20060001
     */
    public JsonObject getEventPreviewInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request){
        JsonObject result = new JsonObject();
        
        Integer eventId = null;
        try {
            eventId = CommonUtil.getJsonParamInt(jsonObject, "eventId", 0, TagCodeEnum.EVENT_ID_MISSING, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(TAG_CODE, e.getErrCode());
            return result;
        }
        
        EventPreviewInfo queryInfo = null;
        try {
        EventPreviewInfoService eventPreviewInfoService = MelotBeanFactory.getBean("eventPreviewInfoService", EventPreviewInfoService.class);
        queryInfo = eventPreviewInfoService.getEventPreviewInfoByEventId(eventId);
        } catch (Exception e) {
            logger.error("Fail to call eventPreviewInfoService.getEventPreviewInfoByEventId ", e);
            result.addProperty(TAG_CODE, TagCodeEnum.IRREGULAR_RESULT);
            return result;
        }
        if (queryInfo != null) {
            result = EventPreviewInfo.object2json(queryInfo);
            result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
        } else {
            result.addProperty(TAG_CODE, TagCodeEnum.QUERY_RETURN_NULL);
        }
        
        return result;
    }
    
    /**
     * 根据当前时间 获取预告节目详情列表
     * @param jsonObject:{startTime:直播开播时间的毫秒数， endTime:直播结束时间的毫秒数}
     * @return
     * functag:20060002
     */
    public JsonObject getEventPreviewInfoByTime(JsonObject jsonObject,boolean checkTag, HttpServletRequest request){
        JsonObject result = new JsonObject();
        
        long startTime = 0l;
        long endTime = 0l;
        int channelId = 0;
        try {
            startTime = CommonUtil.getJsonParamLong(jsonObject, "startTime", System.currentTimeMillis(), null, 0, Long.MAX_VALUE);
            endTime = CommonUtil.getJsonParamLong(jsonObject, "endTime", startTime + MILLISECONDS_IN_MONTH , null, 0, Long.MAX_VALUE);
            channelId = CommonUtil.getJsonParamInt(jsonObject, "c", 0, TagCodeEnum.CHANNEL_MISSING, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(TAG_CODE, e.getErrCode());
            return result;
        }
        
        if (endTime < startTime) {
            result.addProperty(TAG_CODE, TagCodeEnum.TIME_ISNOT_COMPLY);
            return result;
        }
        List<EventPreviewInfo> queryList = null;
        Date tempStart = new Date(startTime);
        Date tempEnd = new Date(endTime);
        Map<String, List<EventPreviewInfo>> matchMaps;
       
        try {
            EventPreviewInfoService eventPreviewInfoService = MelotBeanFactory.getBean("eventPreviewInfoService", EventPreviewInfoService.class);
            queryList = eventPreviewInfoService.getEventPreviewInfosByTime(tempStart, tempEnd, USEABLE_IN_OPEN, channelId);
            if (queryList == null) {
                result.add("eventList", new JsonArray());
                result.addProperty(TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
        } catch (Exception e) {
            logger.error("Fail to call eventPreviewInfoService.getEventPreviewInfosByTime ", e);
            result.addProperty(TAG_CODE, TagCodeEnum.QUERY_RETURN_NULL);
            return result;
        }
        try {
            matchMaps = groupMap(queryList);
        } catch (ParseException e) {
            logger.error("Parse Date failed", e);
            result.addProperty(TAG_CODE, e.getMessage());
            return result;
        }
        JsonArray array = new JsonArray();
        Set<Map.Entry<String, List<EventPreviewInfo>>> entry = matchMaps.entrySet();
        for (Map.Entry<String, List<EventPreviewInfo>> oneList : entry) {
            JsonArray array1 = new JsonArray();
            for (EventPreviewInfo info:oneList.getValue()) {
                array1.add(EventPreviewInfo.object2json(info));
            }
            JsonObject oneOfList = new JsonObject();
            oneOfList.add(oneList.getKey(), array1);
            array.add(oneOfList);
        }
        
        result.add("eventList", array);
        result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
        return result;
    }
    
    private Map<String, List<EventPreviewInfo>> groupMap(List<EventPreviewInfo> orderList) throws ParseException{
        Map<String, List<EventPreviewInfo>> matchMaps = new LinkedHashMap<String, List<EventPreviewInfo>>();
        List<EventPreviewInfo> tempEventList = null;
        for (EventPreviewInfo match : orderList) {
            String startStr = DateUtil.formatDate(match.getStartTime(), "yyyyMMdd");
            if(!matchMaps.containsKey(startStr)){
                tempEventList = new ArrayList<EventPreviewInfo>(16);
                tempEventList.add(match);
                matchMaps.put(startStr, tempEventList);
            }else{
                matchMaps.get(startStr).add(match);
            }
        }
        return matchMaps;
    }
}

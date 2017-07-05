/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.action.sport;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.melot.game.config.sdk.domain.EventNews;
import com.melot.game.config.sdk.event.service.EventNewsService;
import com.melot.kkgame.action.BaseAction;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.kktv.util.CommonUtil;

/**
 * Title: EventNewsFunction
 * <p>
 * Description: 
 * </p>
 * 
 * @author 王康鹏<a href="mailto:kangpeng.wang@melot.cn">
 * @version V1.0
 * @since 2015年9月8日 上午11:10:55
 */
public class EventNewsFunction extends BaseAction{
	private static Logger logger = Logger.getLogger(EventNewsFunction.class);
	
	private EventNewsService eventNewsService;

	/**
	 * @param eventNewsService the eventNewsService to set
	 */
	public void setEventNewsService(EventNewsService eventNewsService) {
		this.eventNewsService = eventNewsService;
	}
	
	/**
	 * 获取赛事新闻(20070001)
	 * @param jsonObject
	 * @param checkTag
	 * @param request
	 * @return
	 */
	public JsonObject getEventNewsList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		JsonObject result = new JsonObject();

		int eventType, start, isShow = 1, num;
		try {
			eventType = CommonUtil.getJsonParamInt(jsonObject, "eventType", 0, TagCodeEnum.EVENT_TYPE_MISSING, 0, Integer.MAX_VALUE);
			start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, null, 1, Integer.MAX_VALUE);
			num = CommonUtil.getJsonParamInt(jsonObject, "num", 10, null, 1, Integer.MAX_VALUE);
			JsonElement isShowJe = jsonObject.get("isShow");
			if (isShowJe != null && !isShowJe.isJsonNull()) {
				isShow = isShowJe.getAsInt();
			}
		} catch (Exception e) {
			logger.error("method getEventNewsList call fail... detail: " + jsonObject.toString());
			result.addProperty(TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		
		int totalCount = eventNewsService.getEventNewsCountByParameters(isShow, null, null, eventType);
		JsonArray jsonArray = new JsonArray();
		if (totalCount > 0) {
			List<EventNews> eventNewsList = eventNewsService.getEventNewsByParameters(isShow, null, null, eventType, start, num);
			if (eventNewsList != null && eventNewsList.size() > 0) {
				for (EventNews news : eventNewsList) {
					news.setContent(null);
					JsonObject jsonObj = eventNewsToJson(news);
					jsonArray.add(jsonObj);
				}
			}
		}
		result.add("eventNewsList", jsonArray);
		result.addProperty("totalCount", totalCount);
		result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);

		return result;
	}
	
	/**
	 * 获取赛事新闻详情(20070002)
	 * @param jsonObject
	 * @param checkTag
	 * @param request
	 * @return
	 */
	public JsonObject getEventNewsById(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
		int eventId;
		try {
			eventId = CommonUtil.getJsonParamInt(jsonObject, "eventId", 0, TagCodeEnum.EVENT_ID_MISSING, 0, Integer.MAX_VALUE);
		} catch (Exception e) {
        	logger.error("method getEventNewsById call fail... detail: "+jsonObject.toString());
        	result.addProperty(TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
		
		EventNews eventNews = eventNewsService.getEventNewsByEventId(eventId);
		if (eventNews != null) {
			JsonObject jsonObj = eventNewsToJson(eventNews);
			result.add("eventNews", jsonObj);
		}
		result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
		
		return result;
	}
	
	/**
	 * eventNews 转成json对象
	 * @param eventNews
	 * @return
	 */
	private JsonObject eventNewsToJson(EventNews eventNews) {
		if (eventNews == null) {
			return null;
		}
		JsonObject newsObj = new JsonObject();
		if (eventNews.getEventId() != null) {
			newsObj.addProperty("eventId", eventNews.getEventId());
		}
		if (eventNews.getIsShow() != null) {
			newsObj.addProperty("isShow", eventNews.getIsShow());
		}
		if (eventNews.getEventTitle() != null) {
			newsObj.addProperty("eventTitle", eventNews.getEventTitle());
		}
		if (eventNews.getEventAbstract() != null) {
			newsObj.addProperty("eventAbstract", eventNews.getEventAbstract());
		}
		if (eventNews.getEventType() != null) {
			newsObj.addProperty("eventType", eventNews.getEventType());
		}
		if (eventNews.getContent() != null) {
			newsObj.addProperty("content", eventNews.getContent());
		}
		if (eventNews.getImgUrl() != null) {
			newsObj.addProperty("imgUrl", eventNews.getImgUrl());
		}
		if (!StringUtil.strIsNull(eventNews.getEventUrl())) {
			newsObj.addProperty("eventUrl", eventNews.getEventUrl());
		}
		if (eventNews.getNewsRef() != null) {
			newsObj.addProperty("newsRef", eventNews.getNewsRef());
		}
		if (eventNews.getTopIndex() != null) {
			newsObj.addProperty("topIndex", eventNews.getTopIndex());
		}
		if (eventNews.getEndTime() != null) {
			newsObj.addProperty("endTime", eventNews.getEndTime().getTime());
		}
		if (eventNews.getBeginTime() != null) {
			newsObj.addProperty("beginTime", eventNews.getBeginTime().getTime());
		}
		if (eventNews.getDtime() != null) {
			newsObj.addProperty("dtime", eventNews.getDtime().getTime());
		}
		return newsObj;
	}	
	
}

/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2016
 */
package com.melot.kkgame.action;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.kkgame.redis.RankingListSource;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.TagCodeEnum;

/**
 * Title: EventFunction.java
 * <p>
 * Description: 活动类
 * </p>
 * 
 * @author 王康鹏<a href="mailto:kangpeng.wang@melot.cn">
 * @version V1.0
 * @since 2016年2月16日 上午10:51:02
 */
public class RankFunction extends BaseAction {

	private RankingListSource rankingListSource;

	private static JsonParser parser = new JsonParser();

	public void setRankingListSource(RankingListSource rankingListSource) {
		this.rankingListSource = rankingListSource;
	}

	/**
	 * 获取排名 (88008800)
	 * 
	 * @param jsonObject
	 * @param checkTag
	 * @param request
	 * @return
	 */
	public JsonObject getRanking(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		JsonObject result = new JsonObject();
		String eventTag, boardType;
		int start, offset;
		try {
			eventTag = CommonUtil.getJsonParamString(jsonObject, "eventTag", "", null, 0, Integer.MAX_VALUE);
			boardType = CommonUtil.getJsonParamString(jsonObject, "boardType", "", null, 0, Integer.MAX_VALUE);
			start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, null, 1, Integer.MAX_VALUE);
			offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 1, null, 1, Integer.MAX_VALUE);
		} catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty(TAG_CODE, e.getErrCode());
			return result;
		}
		if (start > 0 && offset > 1) {
			boardType = boardType + "_" + start + "_" + offset;
		}
		String boardStr = rankingListSource.getRankList(eventTag, boardType);
		if (!StringUtil.strIsNull(boardStr)) {
			JsonArray jsonArray = parser.parse(boardStr).getAsJsonArray();
			result.add("rankList", jsonArray);
		} else {
			result.add("rankList", new JsonArray());
		}
		result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
		return result;
	}
}

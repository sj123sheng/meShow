/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2016
 */
package com.melot.kkgame.action;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonObject;
import com.melot.kkgame.redis.RankingListSource;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.DateUtil;
import com.melot.kktv.util.TagCodeEnum;

/**
 * Title: ActivityFunction.java
 * <p>
 * Description: 活动相关
 * </p>
 * 
 * @author 王康鹏<a href="mailto:kangpeng.wang@melot.cn">
 * @version V1.0
 * @since 2016年2月29日 下午4:31:30
 */
public class ActivityFunction extends BaseAction {

	private RankingListSource rankingListSource;

	private static long startTime ;
	private static long endTime;
	
	static{
	    try {
            startTime = DateUtil.parseDateTimeStringToLong("2016-3-6 0:00:00", "yyyy-MM-dd HH:mm:ss");
            endTime = DateUtil.parseDateTimeStringToLong("2016-3-10 0:00:00", "yyyy-MM-dd HH:mm:ss");
        } catch (Exception e) {
            startTime = 1457193600000l;
            endTime = 1457539200000l;
        }
	}
	
	
	/**
	 * 时间还没到
	 */
	private static final String NOT_IN_TIME = "88000001";
	/**
	 * 本时间段已经参与过一次
	 */
	private static final String HAD_LOTTERY = "88000002";
	/**
	 * 没有抽取机会
	 */
	private static final String HAS_NO_CHANCE = "88000003";
	/**
	 * 礼物抽取完毕，抽奖结束
	 */
	private static final String OUT_OF_SOCKET = "88000004";

	public void setRankingListSource(RankingListSource rankingListSource) {
		this.rankingListSource = rankingListSource;
	}

	public JsonObject doLottery(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		JsonObject result = new JsonObject();
		if (!checkTag) {
			result.addProperty(TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		int userId;
		// 999,8888
		String type;
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
			type = CommonUtil.getJsonParamString(jsonObject, "type", null, "", 1, 40);
		} catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty(TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		// 0.是否在活动时间内
		if (!checkTime(new Date().getTime())) {
			result.addProperty(TAG_CODE, NOT_IN_TIME);
			return result;
		}
		// 1.查询用户当前时间是否已经玩过三次
		if (rankingListSource.hadGoddessLottery(userId, type)) {
			result.addProperty(TAG_CODE, HAD_LOTTERY);
			return result;
		}
		// 2.查询是否还有剩余
		int needCount = rankingListSource.getLotteryChance(userId, type);
		if (needCount < 0) {
			result.addProperty("needMore", -needCount);
			result.addProperty(TAG_CODE, HAS_NO_CHANCE);
			return result;
		}
		// 3.抽取礼包
		JsonObject giftObj = rankingListSource.doLottery(userId, type);
		if (giftObj == null) {
			result.addProperty(TAG_CODE, OUT_OF_SOCKET);
			return result;
		} else {
			result.add("giftDetail", giftObj);
		}
		result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
		return result;
	}

	/**
	 * 检查时间
	 * 
	 * @param timestamp
	 * @return
	 */
	private boolean checkTime(long timestamp) {
		if (timestamp >= startTime && timestamp < endTime) {
			return true;
		}
		return false;
	}
}

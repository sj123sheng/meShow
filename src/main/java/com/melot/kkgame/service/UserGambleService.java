/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.service;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.kkgame.model.GambleResult;
import com.melot.kkgame.model.UserGamble;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.kktv.util.DBEnum;
import com.melot.kktv.util.DateUtil;
import com.melot.kktv.util.db.SqlMapClientHelper;

/**
 * Title: GambleResultService
 * <p>
 * Description:
 * </p>
 * 
 * @author 王康鹏<a href="mailto:kangpeng.wang@melot.cn">
 * @version V1.0
 * @since 2015年9月16日 下午2:32:04
 */
public class UserGambleService {

	private static Logger logger = Logger.getLogger(UserGambleService.class);

	/**
	 * 分页查询
	 * 
	 * @param userId
	 * @param startTime
	 * @param endTime
	 * @param offset
	 * @param limit
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<UserGamble> getUserGamble(int userId, Date startTime, Date endTime, int offset, int limit) {
		List<UserGamble> userGambleList = null;
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("userId", userId);
		map.put("startTime", startTime);
		map.put("endTime", endTime);
		map.put("offset", offset);
		map.put("limit", limit);
		try {
			userGambleList = (List<UserGamble>) SqlMapClientHelper.getInstance(DBEnum.KKGAME_PG).queryForList("UserGamble.getUserGamble", map);
		} catch (SQLException e) {
			logger.error(e);
		}
		return userGambleList;
	}

	/**
	 * 计数
	 * 
	 * @param userId
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	public int getUserGambleCount(int userId, Date startTime, Date endTime) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("userId", userId);
		map.put("startTime", startTime);
		map.put("endTime", endTime);
		try {
			return (Integer) SqlMapClientHelper.getInstance(DBEnum.KKGAME_PG).queryForObject("UserGamble.getUserGambleCount", map);
		} catch (SQLException e) {
			logger.error(e);
		}
		return 0;
	}

	/**
	 * 获取用户结果
	 * 
	 * @param userId
	 * @return
	 */
	public GambleResult getUserGambleResult(int userId) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("userId", userId);
		try {
			return (GambleResult) SqlMapClientHelper.getInstance(DBEnum.KKGAME_PG).queryForObject("UserGamble.getGambleResult", map);
		} catch (SQLException e) {
			logger.error(e);
		}
		return null;
	}

	public JsonObject getGambleResultJson(int userId, String month, int offset, int limit) {
		JsonObject result = new JsonObject();
		int totalCount = 0;
		JsonArray jsonArray = new JsonArray();
		Date btime = null;
		Date etime = null;
		if (month != null && !StringUtils.isEmpty(month)) {
			btime = DateUtil.parseDateStringToDate(month, "yyyyMM");
			etime = DateUtil.addOnField(btime, Calendar.MONTH, 1);
		}
		totalCount = this.getUserGambleCount(userId, btime, etime);
		if (totalCount > 0) {
			List<UserGamble> userGambleList = this.getUserGamble(userId, btime, etime, offset, limit);
			for (UserGamble gamble : userGambleList) {
				jsonArray.add(gamble.toJsonObject());
			}
		}
		result.addProperty("totalCount", totalCount);
		result.add("gambleList", jsonArray);
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
	}
}

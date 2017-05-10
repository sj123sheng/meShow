package com.melot.kkcx.service;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.melot.kktv.model.FamilyMatchRank;
import com.melot.kktv.redis.MatchSource;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;

public class FamilyMatchService {
	
	private static Logger logger = Logger.getLogger(FamilyMatchService.class);
	
	/**
	 * 获取家族比赛主播排名(oracle)
	 * @param period	比赛周期
	 * @param play	比赛场次
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<FamilyMatchRank> getFamilyMatchActorRank(Integer period, Integer play) {
		try {
			Map<Object, Object> map = new HashMap<Object, Object>();
			map.put("period", period);
			if (play != null) map.put("play", play);
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Family.getFamilyMatchActorRank", map);
			String TagCode = (String) map.get("TagCode");
			if (TagCode.equals(TagCodeEnum.SUCCESS)) {
				if (map.containsKey("rankList") && map.get("rankList")!=null) {
					return (List<FamilyMatchRank>) map.get("rankList");
				} else {
					logger.error("调用存储过程得到rankList为null, period:" + period + ", play:" + play);
				}
			} else {
				logger.error("调用存储过程(Family.getFamilyMatchActorRank)未的到正常结果,TagCode:" + TagCode + ", period:" + period + ", play:" + play);
			}
		} catch (SQLException e) {
			logger.error("未能正常调用存储过程, period:" + period + ", play:" + play, e);
		}
		return null;
	}
	
	/**
	 * 获取家族比赛用户排名(oracle)
	 * @param period	比赛周期
	 * @param play	比赛场次
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<FamilyMatchRank> getFamilyMatchUserRank(Integer period, Integer play) {
		try {
			Map<Object, Object> map = new HashMap<Object, Object>();
			map.put("period", period);
			if (play != null) map.put("play", play);
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Family.getFamilyMatchUserRank", map);
			String TagCode = (String) map.get("TagCode");
			if (TagCode.equals(TagCodeEnum.SUCCESS)) {
				if(map.containsKey("rankList") && map.get("rankList")!=null) {
					return (List<FamilyMatchRank>) map.get("rankList");
				} else {
					logger.error("调用存储过程得到rankList为null, period:" + period + ", play:" + play);
				}
			} else {
				logger.error("调用存储过程(Family.getFamilyMatchUserRank)未的到正常结果,TagCode:" + TagCode + ", period:" + period + ", play:" + play);
			}
		} catch (SQLException e) {
			logger.error("未能正常调用存储过程, period:" + period + ", play:" + play, e);
		}
		return null;
	}
	
	/**
	 * 获取整期家族比赛冠军(场次默认100)
	 * @param period	比赛周期
	 * @return
	 */
	public static FamilyMatchRank getFamilyMatchChampion(Integer period) {
		List<FamilyMatchRank> rankList = null;
		String data = MatchSource.getFamilyMatchPlay(String.valueOf(period), "100");
		if (data != null) {
			try {
				rankList = new Gson().fromJson(data,
						new TypeToken<List<FamilyMatchRank>>(){}.getType());
			} catch (Exception e) {
				rankList = null;
			}
		}
		if (rankList == null || rankList.size() == 0) {
			data = MatchSource.getFamilyMatchActorCache(String.valueOf(period));
			if (data != null) {
				try {
					rankList = new Gson().fromJson(data,
							new TypeToken<List<FamilyMatchRank>>(){}.getType());
				} catch (Exception e) {
					rankList = null;
				}
			}
		}
		if (rankList == null || rankList.size() == 0) {
			rankList = getFamilyMatchActorRank(period, null);
		}
		if (rankList != null && rankList.size() > 0) {
			return rankList.get(0);
		} else {
			return null;
		}
	}
	
}

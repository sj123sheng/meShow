/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import redis.clients.jedis.Tuple;

import com.melot.game.config.sdk.domain.GiftStarRecord;
import com.melot.kkgame.redis.support.RedisException;
import com.melot.kkgame.redis.support.RedisTemplate;

/**
 * Title: GiftStarSource.java
 * <p>
 * Description: 
 * </p>
 * 
 * @author 王康鹏<a href="mailto:kangpeng.wang@melot.cn">
 * @version V1.0
 * @since 2015年10月14日 下午6:43:35
 */
public class GiftStarSource extends RedisTemplate{
	
	public static final String STAR_KEY="giftStar_";

	private static Logger logger = Logger.getLogger(GiftStarSource.class);

	@Override
	public String getSourceName() {
		return "GiftStarSource";
	}
	
	public Double getUserScore(String dateStr,int type,int giftId, int userId) throws RedisException{
	   final String key = STAR_KEY + giftId + "_"+dateStr+"_" + type;
	   return zscore(key, String.valueOf(userId));
	}

	public List<GiftStarRecord> getRecordFromRedis(String dateStr,int type,int giftId, int min){
		Set<Tuple> tuples;
		try {
			tuples = this.zrevrangeByScoreWithScores(STAR_KEY + giftId + "_"+dateStr+"_" + type, Long.MAX_VALUE, min);
			if(tuples!=null && tuples.size()>0){
				List<GiftStarRecord> recordList= new ArrayList<GiftStarRecord>();
				int i = 1;
				for(Tuple tupleb:tuples){
					Integer userId = Integer.valueOf(tupleb.getElement());
					GiftStarRecord giftStarRecord = new GiftStarRecord();
					giftStarRecord.setGiftCount((int)tupleb.getScore());
					giftStarRecord.setGiftId(Integer.valueOf(giftId));
					giftStarRecord.setRangeNum(i);
					giftStarRecord.setRangeType(Integer.valueOf(type));
					giftStarRecord.setUserId(userId);
					giftStarRecord.setWeekStartDay(dateStr);
					recordList.add(giftStarRecord);
					i++;
				}
				return recordList;
			}
		} catch (RedisException e) {
			logger.error("GiftStarSource.getRecordFromRedis(" + "dateStr:" + dateStr + "type:" + type + "giftId:" + giftId + "min:" + min + ") execute exception.", e);
		}
		return null;
	}
	
}

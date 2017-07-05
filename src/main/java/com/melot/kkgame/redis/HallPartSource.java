/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.redis;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import com.melot.kkgame.redis.support.RedisCallback;
import com.melot.kkgame.redis.support.RedisException;
import com.melot.kkgame.redis.support.RedisTemplate;

/**
 * Title: HallPartSource
 * <p>
 * Description: 最新直播读取
 * </p>
 * 
 * @author 陈磊<a href="mailto:lei.chen@melot.cn">
 * @version V1.0
 * @since 2015-11-2 上午11:01:38
 */
public class HallPartSource extends RedisTemplate{

    /** 大厅最新开播房间存储*/
    private static final String HALL_LASTER_ROOM_ZADD = "hall_laster_room_zadd";
    
    /** 大厅最新开播房间存储(男)*/
    private static final String HALL_LASTER_ROOM_ZADD_MAN = "hall_laster_man_room_zadd";
    
    /** 大厅最新开播房间存储(女) */
    private static final String HALL_LASTER_ROOM_ZADD_LADY = "hall_laster_lady_room_zadd";
    
    /** 省市id缓存前缀 */
    private static final String PROVINCE_PREFIX = "province_";
    
    /** 市区id前缀 */
    private static final String DISTRICT_PREFIX= "district_";
    
    private static final String GAME_CACHE_ROOM_PREFIX = "game_cache_roomid_";
    private static final int FIVE_MINUTE_SERCONDS = 5 * 30;
    
    /** 地区缓存时间 */
    private static final int CACHE_QUERY_AREA_TIME = 24 * 60 * 60; 
    
    @Override
    public String getSourceName() {
        return "GamblingSource";
    }
    
    /**
     * 得到最新开播房间按开播时间最新到老排列的列表
     * @param start
     * @param offset
     * @return
     * @throws RedisException
     */
    public Map<Integer, Long> getLaststrLiveRoomsInOrder(int start, int offset, int gender) throws RedisException{
        Map<Integer, Long> roomMap = new LinkedHashMap<Integer, Long>();
        Set<Tuple> liveRooms = null;
        if(gender == -1){
            liveRooms = zrevrangeByScoreWithScores(HALL_LASTER_ROOM_ZADD, Long.MAX_VALUE, 0);
        }else if (gender == 1) {
            liveRooms = zrevrangeByScoreWithScores(HALL_LASTER_ROOM_ZADD_MAN, Long.MAX_VALUE, 0);
        }else {
            liveRooms = zrevrangeByScoreWithScores(HALL_LASTER_ROOM_ZADD_LADY, Long.MAX_VALUE, 0);
        }
        for (Tuple tuple : liveRooms) {
            if((start--)>0){
               continue; 
            }
            String roomIdStr = tuple.getElement();
            Integer roomId = Integer.parseInt(roomIdStr);
            roomMap.put(roomId, (long)tuple.getScore());
            if(roomMap.size() == offset){
                break;
            }
        }
        return roomMap;
    }
    
    public String getLiveResRoomInfo(final int roomId) throws RedisException{
        return get(GAME_CACHE_ROOM_PREFIX+roomId);
    }
    
    public String setLiveRoomInfo(final int roomId, final String jsonObjectRoomInfo) throws RedisException{
        return execute( new RedisCallback<String>() {
            public String doInRedisClient(Jedis jedis) throws RedisException{
                return jedis.setex(GAME_CACHE_ROOM_PREFIX+roomId, FIVE_MINUTE_SERCONDS, jsonObjectRoomInfo);
            }
        });
    }
    
    public Long getLiveTotalCount(final int gender) throws RedisException{
        return execute( new RedisCallback<Long>() {
            public Long doInRedisClient(Jedis jedis) throws RedisException{
                long count = 0l;
                if(gender == -1){
                    count = jedis.zcount(HALL_LASTER_ROOM_ZADD, 0, Long.MAX_VALUE);
                }else if (gender == 1) {
                    count = jedis.zcount(HALL_LASTER_ROOM_ZADD_MAN, 0, Long.MAX_VALUE);
                }else {
                    count = jedis.zcount(HALL_LASTER_ROOM_ZADD_LADY, 0, Long.MAX_VALUE);
                }
                return count;
            }
        });
    }
    
    public String setProvince(final int provinceId, final String provinceName) throws RedisException{
        return execute( new RedisCallback<String>() {
            public String doInRedisClient(Jedis jedis) throws RedisException{
                return jedis.setex(PROVINCE_PREFIX + provinceName, CACHE_QUERY_AREA_TIME, provinceId + "");
            }
        });
    }
    
    public String getProvinceId(final String provinceName) throws RedisException{
        return get(PROVINCE_PREFIX + provinceName);
    }
    
    public String setDistrict(final int districtId, final String districtName) throws RedisException{
        return execute( new RedisCallback<String>() {
            public String doInRedisClient(Jedis jedis) throws RedisException{
                return jedis.setex(DISTRICT_PREFIX + districtName, CACHE_QUERY_AREA_TIME, districtId + "");
            }
        });
    }
    
    public String getDistrictId(final String districtName) throws RedisException{
        return get(DISTRICT_PREFIX + districtName);
    }
}

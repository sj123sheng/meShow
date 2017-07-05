/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.redis;

import java.util.Map;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.melot.kkgame.domain.LotteryInfo;
import com.melot.kkgame.redis.support.RedisCallback;
import com.melot.kkgame.redis.support.RedisException;
import com.melot.kkgame.redis.support.RedisTemplate;

/**
 * Title: SignInTaskSource
 * 
 * <p>
 * Description: redis for lottery
 *  
 * </p>
 * @author  姚国平<a href="mailto:guoping.yao@melot.cn">
 * @version V1.0
 * @since 2015-4-8 下午4:35:16 
 */
public class LotteryTaskSource extends RedisTemplate{

    @Override
    public String getSourceName() {
        return "SignInTask";
    }
    
    /** 用户领取奖票 */
    private final String LOTTERY_KEY = "lottery";
    
    /** 配置的可领取彩票列表 */
    private final String LOTTERY_TICKEST_LIST = "lottery_tickest_list";
    
    /** 彩票中奖记录缓存key前缀 */
    private final String QUERY_LOTTERY_PREFIX = "lottery_history_";
    
    private final String  LOTTERY_PROFILE_PREFIX= "lottery_profile_";
    
    /** 用户月中奖记录缓存时间, 修改为1天 */
    private final int CACHE_QUERY_LOTTERY_TIME = 86400 * 1; 
    
    /**
     * 用户当日领取彩票
     * @throws RedisException 
     */
    public void userBetToday(final Integer userId,final Integer lotteryId) throws RedisException{
        execute(new RedisCallback<Long>() {
            public Long doInRedisClient(Jedis jedis) throws RedisException {
                Long remainTime = jedis.ttl(LOTTERY_KEY);
                if(remainTime == -2 || remainTime == -1) {
                    jedis.del(LOTTERY_KEY);
                    jedis.hset(LOTTERY_KEY, String.valueOf(userId),String.valueOf(lotteryId));
                   return jedis.expireAt(LOTTERY_KEY, getNextDayZeroTime().getTime()/1000); //设置过期时间,次日凌晨
                }else{
                    return jedis.hset(LOTTERY_KEY, String.valueOf(userId),String.valueOf(lotteryId));
                } 
            }
        });
        deleteLotteryInfo(userId); //删除用户总中奖缓存记录
    }
    
    /**
     * 判断用户今日是否已经领奖
     */
    public  String hasBetIn(Integer userId)throws RedisException{
        return hget(LOTTERY_KEY, userId.toString());
    }

    
    /**
     *  设置可用的奖券列表
     */
    public void addLotteryTickets(final JsonArray LotteryTickets)throws RedisException{
        if(LotteryTickets == null || LotteryTickets.size() < 1){
            return;
        }
        final String key = LOTTERY_TICKEST_LIST;
        execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedisClient(Jedis jedis) throws RedisException {
                Transaction trans = jedis.multi();
                for (int i = 0, size = LotteryTickets.size(); i < size; i++) {
                    JsonElement element = LotteryTickets.get(i);
                    trans.zadd(key, i, element.getAsJsonObject().toString());
                }
                trans.exec();
                return null;
            }
        });
    }
    
    /**
     *  清除用户月投注记录缓存 
     *  用户当月每日投注造成记录实时性,需要重置cache
     */
    public void deleteCurrentMonthLotteryHistory(final Integer userId, final String queryMonth) throws RedisException{
        final String key = QUERY_LOTTERY_PREFIX + userId + "_" + queryMonth;
        del(key);
    }
    
    /**
     *  缓存用户月彩票中奖记录,    用户查询的当月记录随着投注将重置缓存记录,顾延长cache时间
     */
    public void cacheLotteryHistory(final Integer userId, final String queryMonth,final JsonArray lotteryHistory) throws RedisException{
        if(lotteryHistory == null || lotteryHistory.size() < 1){
            return;
        }
        final String key = QUERY_LOTTERY_PREFIX + userId + "_" + queryMonth;
        execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedisClient(Jedis jedis) throws RedisException {
                Transaction trans = jedis.multi();
                for (int i = 0, size = lotteryHistory.size(); i < size; i++) {
                    JsonElement element = lotteryHistory.get(i);
                    trans.zadd(key, i, element.getAsJsonObject().toString());
                }
                trans.exec();
                jedis.expire(key, CACHE_QUERY_LOTTERY_TIME);
                return null;
            }
        });
    }
    
    /**
     * 获取中奖记录缓存 
     */
    public Set<String> getLotteryHistory(final Integer userId, final String queryMonth) throws RedisException{
        final String key = QUERY_LOTTERY_PREFIX + userId + "_" + queryMonth;
        return execute(new RedisCallback<Set<String>>() {
            @Override
            public Set<String> doInRedisClient(Jedis jedis) throws RedisException {
                Long remainTime = jedis.ttl(key);
                if(remainTime == -2 || remainTime == -1) {
                    return null;
               }else{
                   return zrange(key, 0, -1);
               }
            }
         });
    }
    
    
    /**
     *  根据房间号查询房间签到排行榜 
     *  @param roomId: 房间id
     *  @return 房间内签到排行榜
     * @throws RedisException 
     */
    public  Set<String> getLotteryTickets() throws RedisException{
        final String key = LOTTERY_TICKEST_LIST;
        return zrange(key, 0, -1);
    }
    
    /**
     *  缓存用户中采统计信息 
     * @throws RedisException 
     * 
     */
    public void cacheLotteryInfo(LotteryInfo lotteryInfo) throws RedisException{
        final String key = LOTTERY_PROFILE_PREFIX + lotteryInfo.getUserId();
        hset(key, "lotteryTime", String.valueOf(lotteryInfo.getLotteryTime()));
        hset(key, "winLotteryTime",String.valueOf(lotteryInfo.getWinLotteryTime()));
        hset(key, "winProfit", String.valueOf(lotteryInfo.getWinProfit()));
    }
    
    public Map<String, String> getLotteryInfo(Integer userId) throws RedisException{
        final String key = LOTTERY_PROFILE_PREFIX + userId;
        return hgetAll(key);
    }
    
    public void deleteLotteryInfo(Integer userId) throws RedisException{
        final String key = LOTTERY_PROFILE_PREFIX + userId;
        del(key);
    }
    
}

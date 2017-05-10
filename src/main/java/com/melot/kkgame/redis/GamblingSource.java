/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.redis;

import java.util.Set;

import redis.clients.jedis.Jedis;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.kkgame.redis.support.RedisCallback;
import com.melot.kkgame.redis.support.RedisException;
import com.melot.kkgame.redis.support.RedisTemplate;

/**
 * Title: GamblingSource
 * <p>
 * Description: 需要保存到redis的竞猜信息
 * </p>
 * @author  姚国平<a href="mailto:guoping.yao@melot.cn">
 * @version V1.0
 * @since 2015-7-31 下午2:53:33 
 */
public class GamblingSource extends RedisTemplate{

    /** 盘口保存信息前缀 */
    private static final String GAMBLING_INFO_PREFIX = "gambling_info_";
    
    /** 盘口选项前缀 */
    private static final String GAMBLING_OPTION_PREFIX = "gambling_option_";
    
    private static final String GAMBLING_BETIN_PREFIX = "gambling_bet_history_";
    
    private static final String GAMBLING_RELATION_PREFIX = "gambling_relation_";
    
    private static final String USER_BET_CACHE_PREFIX = "bet_";
    
    private static final String INFO_FIELD = "info";
    
    private static final String GAMBLING_RESULT = "gambling_result_";
    
    private static final String  ADFA_HANDLER_LIST = "ios_adfa_handle_list";
    
    /***
     *  主播能否发起竞猜的管理名单 
     *  若在集合结果则说明该主播不可发起竞猜
     */
    private static final String GAMBLE_ACTOR_BLACK_LIST = "gambling_actor_black_list";
    
    private static final String GAMBLE_ACTOR_WHITE_LIST = "gambling_actor_white_list";
    
    
    @Override
    public String getSourceName() {
        return "GamblingSource";
    }
    
    public Set<String> getBetHistoryOfGamble(Integer gambleId, int recordCount)throws RedisException{
        final String key =  GAMBLING_BETIN_PREFIX + gambleId;
        return zrevrange(key , 0, recordCount);
    }
    
    
    /**
     *  返回用户在盘口赢取的金额
     */
    public Integer getUserWinAmountInOption(Integer gambleId, Integer userId)throws RedisException{
        final String key =  GAMBLING_INFO_PREFIX + gambleId;
        String amount =  hget(key, String.valueOf(userId));
        return amount == null ? 0 : Integer.parseInt(amount);
    }
    
    /**
     *  返回用户在投注项上的投注金额 
     */
    public Integer getUserBetAmountInOption(Integer optionId, Integer userId)throws RedisException{
        final String key =  GAMBLING_OPTION_PREFIX + optionId;
        String amount =  hget(key, String.valueOf(userId));
        return amount == null ? 0 : Integer.parseInt(amount);
    }
    
    public Integer getUserBetAmountInOption(String optionId, Integer userId)throws RedisException{
        final String key =  GAMBLING_OPTION_PREFIX + optionId;
        String amount =  hget(key, String.valueOf(userId));
        return amount == null ? 0 : Integer.parseInt(amount);
    }
    
    
    /**
     *  缓存api获取的盘口信息 
     */
    public void setGambleInfo(Integer gambleId, String info) throws RedisException{
        final String key =  GAMBLING_INFO_PREFIX + gambleId; 
        hset(key, INFO_FIELD, info);
    }
    
    /**
     * 获取缓存的盘口信息
     */
    public String getGambleInfo(Integer gambleId) throws RedisException{
        final String key =  GAMBLING_INFO_PREFIX + gambleId; 
        return hget(key, INFO_FIELD);
    }
    
    /**
     *  用户下注后删除盘口的接口返回缓存,需要重新生成缓存 
     */
    public void deleteGambleInfo(Integer gambleId) throws RedisException{
        final String key =  GAMBLING_INFO_PREFIX + gambleId; 
        hdel(key, INFO_FIELD);
    }
    
    /**
     *  获取用户在盘口上的总投注信息 
     */
    public String getUserBet(Integer gambleId, Integer userId) throws RedisException{
        final String key =  GAMBLING_INFO_PREFIX + gambleId; 
        String msg =  hget(key, USER_BET_CACHE_PREFIX + userId);
        if(msg == null){
            updateUserBet(gambleId, userId);
            msg =  hget(key, USER_BET_CACHE_PREFIX + userId);
        }
        return msg;
    }
    
    /**
     * 更新用户盘口总投注信息
     */
    public void updateUserBet(Integer gambleId, Integer userId) throws RedisException{
        final String key =  GAMBLING_INFO_PREFIX + gambleId; 
        Set<String> options =  smembers(GAMBLING_RELATION_PREFIX+gambleId);
        JsonArray lastBets = new JsonArray();
        for (String string : options) { //遍历删除盘口对应的全部投注项
            JsonObject json = new JsonObject();
            Integer value = getUserBetAmountInOption(string, userId);
            json.addProperty("optionId", string);
            json.addProperty("betAmount", value);
            lastBets.add(json);
        }
        hset(key, USER_BET_CACHE_PREFIX + userId,lastBets.toString());
        
    }
    
    /**
     * 缓存用户押注结果
     * @param userId
     * @param pageIndex
     * @param offset
     * @param gambleResult
     * @throws RedisException
     */
    public void setGambleResult(Integer userId,String dateStr,Integer limit ,Integer offset,String gambleResult) throws RedisException{
		final String key = GAMBLING_RESULT + userId+"_"+dateStr;
		final String field = limit + "_" + offset;
		hset(key,field,gambleResult);
    }
    
    /**
     * 缓存用户押注结果带过期时间 
     * @param userId
     * @param dateStr
     * @param limit
     * @param offset
     * @param gambleResult
     * @param seconds
     * @throws RedisException
     */
    public void setGambleResult(final Integer userId,final String dateStr,final Integer limit ,final Integer offset,final String gambleResult,final int seconds) throws RedisException{
		final String key = GAMBLING_RESULT + userId+"_"+dateStr;
		final String field = limit + "_" + offset;
		execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedisClient(Jedis jedis) throws RedisException {
                Long remaintime = jedis.ttl(key);
                if(remaintime == -2 || remaintime == -1){
                    jedis.del(key);
                }
                jedis.hset(key, field, gambleResult);
                jedis.expire(key, seconds);
                return null;
            }
        });
    }
    
    /**
     * 用户押注结果
     * @param userId
     * @param pageIndex
     * @param offset
     * @return
     * @throws RedisException
     */
    public String getGambleResult(Integer userId,String dateStr,Integer limit ,Integer offset) throws RedisException{
		final String key = GAMBLING_RESULT + userId + "_" + dateStr;
		final String field = limit + "_" + offset;
    	return hget(key, field);
    }
    
    /**
     * 删除用户押注结果
     * @param userId
     * @param pageIndex
     * @param offset
     * @throws RedisException
     */
    public void delGambleResult(Integer userId,String dateStr) throws RedisException{
    	final String key = GAMBLING_RESULT + userId + "_"+dateStr;
		del(key);
    }
    
    
    /***
     *  返回主播能否发起竞猜 
     *  若主播id在 GAMBLE_ACTOR_BLACK_LIST 则不允许发起竞猜
     *  @throws RedisException 
     */
    public boolean canGambling(Integer actorId) throws RedisException{
        return !sismember(GAMBLE_ACTOR_BLACK_LIST,String.valueOf(actorId)).booleanValue();
    }
    
    /***
     *  返回主播是否在白名单, 白名单用户不受主播等级和是否在黑名单限制 
     */
    public boolean isOnWhiteList(Integer actorId)throws RedisException{
        return sismember(GAMBLE_ACTOR_WHITE_LIST,String.valueOf(actorId)).booleanValue();
    }
    
    public Long saveAdfaInfo(JsonObject json) throws RedisException{
        return lpush(ADFA_HANDLER_LIST, json.toString());
    }
    
}

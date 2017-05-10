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
import redis.clients.jedis.Transaction;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.melot.kkgame.redis.support.RedisCallback;
import com.melot.kkgame.redis.support.RedisException;
import com.melot.kkgame.redis.support.RedisTemplate;

/**
 * Title: SignInTaskSource
 * <p>
 * Description: redis for signIn 直播签到
 *  
 * </p>
 * @author  姚国平<a href="mailto:guoping.yao@melot.cn">
 * @version V1.0
 * @since 2015-4-8 下午4:35:16 
 */
public class SignInTaskSource extends RedisTemplate{

    @Override
    public String getSourceName() {
        return "SignInTask";
    }
    
    /** 用户今天是否签到过标记(唯一)*/
    private static final String USER_SIGN_IN_TASK_TODAY = "user_sign_in_today";
    
    /** 房间今日签到key前缀 */
    private static final String SIGN_IN_TASK_PREFIX = "sign_in_";
    
    /** 房间签到排行版key前缀 */
    private static final String SIGN_IN_RANKING_PREFIX = "sign_ranking_";
    
    /** 分享记录前缀 */
    private static final String SIGN_IN_SHARE_PREFIX = "sign_sharing_in_task";
    
    /** 签到排行榜失效时间 */
    private static final int RANKING_EXPIRE_TIME = 30;
    
    /**
     * 用户今日签到,将用户签到信息保存到redis, 包含信息包括
     * @param totalTime连续签到次数, @param rankInRoom所在房间签到排名  
     * @throws RedisException 
     */
    public void userSignInToday(final Integer userId,final Integer roomId, final Integer totalTime, final Integer rankInRoom) throws RedisException{
        final String key = SIGN_IN_TASK_PREFIX+roomId;
        execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedisClient(Jedis jedis) throws RedisException {
                Long remainTime = jedis.ttl(key);
                if(remainTime == -2 || remainTime == -1) {
                    jedis.del(key);
                    jedis.hset(key, String.valueOf(userId), totalTime+"_"+rankInRoom);
                    jedis.expireAt(key, getNextDayZeroTime().getTime()/1000); //设置过期时间,次日凌晨
                }else{
                    jedis.hset(key, String.valueOf(userId), totalTime+"_"+rankInRoom);
                } 
                return null;
            }
        });
    }
    
    /**
     * 用户今日首次签到，做好标记 
     */
    public void userFirstSignInToday(final Integer userId) throws RedisException{
        
        execute(new RedisCallback<Long>() {
            @Override
            public Long doInRedisClient(Jedis jedis) throws RedisException {
                Long remaintime = jedis.ttl(USER_SIGN_IN_TASK_TODAY);
                if(remaintime == -2 || remaintime == -1){
                    jedis.del(USER_SIGN_IN_TASK_TODAY);
                    jedis.hset(USER_SIGN_IN_TASK_TODAY, String.valueOf(userId), "signToday");
                    jedis.expireAt(USER_SIGN_IN_TASK_TODAY, getNextDayZeroTime().getTime()/1000);//设置过期时间，次日凌晨
                }else{
                    jedis.hset(USER_SIGN_IN_TASK_TODAY, String.valueOf(userId), "");
                }
                return null;
            }
        });
    }
    
    /**
     * 某用户今日是否有进行过签到
     * @param userId 无论是那个房间，只是此user签不签到有关，因此只需要usserId
     * @return 返回签到标识标示是否签到
     * @throws RedisException 
     */
    public String oneUserHasSignInToday(Integer userId) throws RedisException{
        String key = USER_SIGN_IN_TASK_TODAY;
        return hget(key, String.valueOf(userId));
    }
    
    /**
     * 判断用户今日是否已经签到 
     */
    public String hasSignIn(Integer userId,Integer roomId)throws RedisException{
        String key = SIGN_IN_TASK_PREFIX+roomId;
        return hget(key, userId.toString());
    }

    /**
     *  根据房间号查询房间签到排行榜 
     *  @param roomId: 房间id
     *  @return 房间内签到排行榜
     */
    public Set<String> getRoomSignRankingList(Integer roomId)throws RedisException{
        String key = SIGN_IN_RANKING_PREFIX+roomId;
        return zrange(key, 0, -1);
    }
    
    /**
     *  设置签到排行榜到redis 
     *  @param roomId: 设置排行榜的房间id
     *  @param rankingList:需要缓存的签到排行榜
     */
    public void addRoomSingRankingList(Integer roomId,final JsonArray rankingList)throws RedisException{
        if(rankingList == null || rankingList.size() < 1){
            return;
        }
        final String key = SIGN_IN_RANKING_PREFIX+roomId;
        execute(new RedisCallback<Long>() {
            public Long doInRedisClient(Jedis jedis) throws RedisException {
                Transaction trans = jedis.multi();
                for (int i = 0, size = rankingList.size(); i < size; i++) {
                    JsonElement element = rankingList.get(i);
                    trans.zadd(key, i, element.getAsJsonObject().toString());
                }
                trans.exec();
                return jedis.expire(key, RANKING_EXPIRE_TIME);
            }
        });        
    }
    
    /**
     *  设置分享签到 
     *  @param userId: 设置key后缀的userid
     *  @param shareId:需要缓存的分享记录id
     */
    public void setShareVieldValue(final Integer userId) throws RedisException{
         execute(new RedisCallback<Long>() {
            public Long doInRedisClient(Jedis jedis) throws RedisException{
                long remaintime = jedis.ttl(SIGN_IN_SHARE_PREFIX);
                if(remaintime == -1 || remaintime == -2){
                    jedis.del(SIGN_IN_SHARE_PREFIX);
                }
                jedis.sadd(SIGN_IN_SHARE_PREFIX, userId+"");
                jedis.expireAt(SIGN_IN_SHARE_PREFIX, getNextDayZeroTime().getTime()/1000);
                return null;
            }
        });
    }
    
    /**
     *  判断是否在分享缓存集合里存在该条记录
     *  @param userId: 设置key后缀的userid
     *  @param shareId:需要缓存的分享记录id
     * @throws RedisException 
     */
    public boolean isHaveShareInTaskValue(final Integer userId) throws RedisException{
        return sismember(SIGN_IN_SHARE_PREFIX, userId+"");
    }
    
    /**
     * 返回userI为key的集合数据
     * @param userId
     * @return
     */
    public Set<String> getShareInTaskValueSet(final Integer userId) throws RedisException{
        return smembers(SIGN_IN_SHARE_PREFIX+userId);
    }
}

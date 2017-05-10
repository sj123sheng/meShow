/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kktv.redis;

import java.util.Map;
import java.util.Set;
import redis.clients.jedis.Jedis;
import com.melot.kktv.util.redis.RedisConfigHelper;

/**
 * Title: SignInTaskSource
 * <p>
 * Description: redis for 端午节
 *  
 * </p>
 * @author  姚国平<a href="mailto:guoping.yao@melot.cn">
 * @version V1.0
 * @since 2015-4-8 下午4:35:16 
 */
public abstract class DragonBoatSource {
    
    private static final String SOURCE_NAME = "DragonBoat";
    
    private static final String QIXI_USER_PREFIX = "qixi_user_";
    private static final String QIXI_COUPON_CODE = "qixi_coupon_code";
    
    private static final String QIXI_LOTTERY_RESULT = "qixi_lottery_result";
    
    
    private static Jedis getInstance() {
        return RedisConfigHelper.getJedis(SOURCE_NAME);
    }
    
    private static void freeInstance(Jedis jedis) {
        RedisConfigHelper.returnJedis(SOURCE_NAME, jedis, false);
    }
    
    /**
     *  获取用户七夕活动数据信息 
     */
    public static Map<String,String> getUserInfo(Integer userId){
        Jedis jedis = null;
        final String key = QIXI_USER_PREFIX + userId;
        try{
            jedis = getInstance();
            if(jedis != null){
               return jedis.hgetAll(key);
            }
        }catch (Exception e) {
        }finally{
            if(jedis != null){
                freeInstance(jedis);
            }
        }
        return null;
    }
    
    /***
     *  从redis获取兑换码 
     */
    public static String getCouponCode(){
        Jedis jedis = null;
        try{
            jedis = getInstance();
            if(jedis != null){
               return jedis.lpop(QIXI_COUPON_CODE);
            }
        }catch (Exception e) {
        }finally{
            if(jedis != null){
                freeInstance(jedis);
            }
        }
        return null;
    }
    
    /*** 从redis获取剩余优惠券  */
    public static Long getNumberOfExistCouponCode(){
        Jedis jedis = null;
        try{
            jedis = getInstance();
            if(jedis != null){
               return jedis.llen(QIXI_COUPON_CODE);
            }
        }catch (Exception e) {
        }finally{
            if(jedis != null){
                freeInstance(jedis);
            }
        }
        return 0l;
    }
    
    /**
     * 更新用户礼物总数, 同时更新送礼总价值 
     * 
     */
    public static void sendCouponCode(Integer userId, String code){
        Jedis jedis = null;
        final String key = QIXI_USER_PREFIX + userId;
        try{
            jedis = getInstance();
            if(jedis != null){
               jedis.hset(key, "code", code);
            }
        }catch (Exception e) {
        }finally{
            if(jedis != null){
               freeInstance(jedis);
            }
        }
    }
    
    
    public static Set<String> qixiLotteryResult(){
        Jedis jedis = null;
        try{
            jedis = getInstance();
            if(jedis != null){
               return jedis.zrevrange(QIXI_LOTTERY_RESULT , 0, -1);
            }
        }catch (Exception e) {
        }finally{
            if(jedis != null){
               freeInstance(jedis);
            }
        }
        return null;
    }
    
}

/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.redis;

import redis.clients.jedis.Jedis;

import com.melot.kkgame.redis.support.RedisCallback;
import com.melot.kkgame.redis.support.RedisException;
import com.melot.kkgame.redis.support.RedisTemplate;

/**
 * Title: SmsSource
 * <p>
 * Description: 短信redis设置
 * </p>
 * @author  姚国平<a href="mailto:guoping.yao@melot.cn">
 * @version V1.0
 * @since 2015-7-3 下午6:40:27 
 */
public class SmsSource extends RedisTemplate{

    @Override
    public String getSourceName() {
        return "Sms";
    }

    // ["18657134568","测试短信"]
    private static final String SENDSMS_QUEUE_NAME = "sms_send";
    // 当日一个手机号下行短信个数 smsCount_phonenum_smsType
    private static final String SMSCOUNT_KEY_FORMAT = "smsCount_%s";
    // 申请一键注册上行短信格式 regSms_phonenum
    private static final String REGSMS_KEY = "regSms_";
    // 短信验证码临时存储 用于判断是否有效 smsVerifyCode_phonenum_smsType
    private static final String SMSVERIFYCODE_KEY_FORMAT = "smsVerifyCode_%s_%s";
    // Android/Iphone/Ipad用户手机号存储 通过短信获取
    private static final String USERPHONENUM_KEY = "userPhoneNum";
    // 已绑定手机号
    private static final String BOUNDPHONENUM_KEY = "pn_";
    
    
    /**
     * 获取手机号发送短信总数
     * @param phoneNum
     * @return 当天已请求发送短信数量
     * @throws RedisException 
     */
    public int getSendSmsCount(String smsType, String phoneNum) throws RedisException {
        String code = hget(String.format(SMSCOUNT_KEY_FORMAT, phoneNum), smsType);
        return code == null ? 0 : Integer.parseInt(code) ;
    }
    
    /**
     * 请求发送短信
     * @param phonenum 手机号
     * @param message 短信内容
     * @throws RedisException 
     */
    public void sendSms(String phoneNum, String smsType, String message, int appId) throws RedisException {
        rpush(SENDSMS_QUEUE_NAME, "[\""+phoneNum+"\",\""+smsType+"\",\""+message+"\",\""+appId+"\"]");
    }
    
    /**
     * android/iphone/ipad 从redis中获取手机号
     * @param userId
     * @return
     * @throws RedisException 
     */
    public  String getRegisterPhoneNum(String userId) throws RedisException {
        String phoneNum = hget(USERPHONENUM_KEY, userId);
        hdel(USERPHONENUM_KEY, userId);
        return phoneNum;
    
    }
    
    /**
     * 验证一键注册上行短信
     * @param phonenum
     * @param platform
     * @param timestamp
     * @param userId
     * @return
     * @throws RedisException 
     */
    public boolean checkRegisterSms(String phoneNum, String platform, String timestamp, String userId) throws RedisException {
        
        String regsmsKey = REGSMS_KEY + phoneNum;
        String check = platform + timestamp + userId;
        if(check.equals(get(regsmsKey))) {
            del(regsmsKey);
            return true;
        }
        return false;
    
    }
    
    /**
     * 创建各业务短信验证码/随机密码等数据
     * @param phoneNum
     * @param smsType
     * @throws RedisException 
     */
    public void createPhoneSmsData(final String phoneNum, final String smsType,final String value,final int seconds) throws RedisException {
        execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedisClient(Jedis jedis) throws RedisException {
                String pattern = String.format(SMSVERIFYCODE_KEY_FORMAT, phoneNum, smsType);
                jedis.set(pattern, value);
                jedis.expire(pattern, seconds);
                return null;
            }
        });
    }
    
    /**
     * 获取各业务短信验证码/随机密码等数据
     * @param phoneNum
     * @param smsType
     * @return
     * @throws RedisException 
     */
    public String getPhoneSmsData(String phoneNum, String smsType) throws RedisException {
            String pattern = String.format(SMSVERIFYCODE_KEY_FORMAT, phoneNum, smsType);
            return get(pattern);
    }
    
    /**
     * 删除各业务短信验证码/随机密码等数据
     * @param phoneNum
     * @param smsType
     * @return
     * @throws RedisException 
     */
    public void delPhoneSmsData(String phoneNum, String smsType) throws RedisException {
        String pattern = String.format(SMSVERIFYCODE_KEY_FORMAT, phoneNum, smsType);
        del(pattern);
    }
    
    /**
     * 解除手机号绑定,清除所有该手机号相关记录
     * @param phoneNum
     * @throws RedisException 
     */
    public void clearUserSms(String phoneNum) throws RedisException {
        // smsCount_%s 自动过期,不删除 
        // smsVerifyCode_%s_%s 自动过期,不删除 
        del( BOUNDPHONENUM_KEY + phoneNum );
    
    }
    
    /**
     * 从redis中获取已绑定手机号
     * @param phoneNum
     * @return userId
     * @throws RedisException 
     */
    public String getBoundPhoneNum(String phoneNum) throws RedisException {

        return get( BOUNDPHONENUM_KEY + phoneNum );
    }
    
    /**
     * 设置用户绑定手机号
     * @param phoneNum
     * @param userId
     * @throws RedisException 
     */
    @Deprecated
    public void setBoundPhoneNum(String phoneNum, String userId) throws RedisException {
        set( BOUNDPHONENUM_KEY + phoneNum, userId);
    }
    
    
    
}

/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2017
 */
package com.melot.kkcx.util;


import com.melot.kkcx.model.NewUserTaskReward;

import java.util.HashMap;
import java.util.Map;

/**
 * Title: AwardConfig
 * <p>
 * Description: TODO
 * </p>
 * @author  姚国平<a href="mailto:guoping.yao@melot.cn">
 * @version V1.0
 * @since 2017-12-7 下午2:04:09 
 */
public abstract class AwardConfig {

    private static final Map<String, NewUserTaskReward> configs = new HashMap<String, NewUserTaskReward>();
    
    static final int FREE_GIFT_SUGAR = 40001239; //棒棒糖（奖品）
    static final int FREE_GIFT_GOLD  = 40001238; //金条（奖品）
    static final int FREE_GIFT_LOVE  = 40001240; //爱神弓箭（奖品）   
    
    static{
        /* 每日登录奖励 */
        configs.put("10000001",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_SUGAR,5));
        configs.put("10000002",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_SUGAR,10));
        configs.put("10000003",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_SUGAR,20));
        configs.put("10000004",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_SUGAR,50));
        configs.put("10000005",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_GOLD,1));
        
        /* 累计充值奖励 */
        configs.put("11000050",new NewUserTaskReward(AwardType.VIP,0,3));
        configs.put("11000100",new NewUserTaskReward(AwardType.VIP,0,3));
        configs.put("11000200",new NewUserTaskReward(AwardType.VIP,0,5));
        configs.put("11000300",new NewUserTaskReward(AwardType.VIP,0,5));
        configs.put("11000500",new NewUserTaskReward(AwardType.CAR,1555,3));
        configs.put("11000700",new NewUserTaskReward(AwardType.CAR,1555,3));
        configs.put("11001000",new NewUserTaskReward(AwardType.CAR,1555,5));
        configs.put("11001500",new NewUserTaskReward(AwardType.CAR,1555,5));
        configs.put("11002000",new NewUserTaskReward(AwardType.CAR,1555,7));
        configs.put("11003000",new NewUserTaskReward(AwardType.CAR,1555,7));
        
        /*  关注类奖励  */
        configs.put("12000001",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_SUGAR,5));
        configs.put("12000005",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_SUGAR,5));
        configs.put("12000010",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_SUGAR,5));
        configs.put("12000020",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_SUGAR,5));
        configs.put("12000030",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_SUGAR,5));
        configs.put("12000040",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_LOVE,10));
        configs.put("12000050",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_LOVE,10));
        
        /* 分享类奖励 */
        configs.put("13000001",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_SUGAR,5));
        configs.put("13000002",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_SUGAR,5));
        configs.put("13000003",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_SUGAR,5));
        configs.put("13000005",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_SUGAR,5));
        configs.put("13000007",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_SUGAR,5));
        configs.put("13000010",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_SUGAR,5));
        configs.put("13000015",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_LOVE,10));
        configs.put("13000020",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_LOVE,10));
        
        /* 成为主播管理 */
        configs.put("14000001",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_SUGAR,5)); //棒棒糖（奖品）
        configs.put("14000002",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_SUGAR,5));
        configs.put("14000003",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_SUGAR,5));
        configs.put("14000004",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_SUGAR,5));
        configs.put("14000005",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_SUGAR,5));
        configs.put("14000007",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_LOVE,10)); //爱神弓箭（奖品）
        configs.put("14000010",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_LOVE,10));
        
        /*  座驾任务奖励  */
        configs.put("15000001",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_SUGAR,5));
        configs.put("15000002",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_SUGAR,5));
        configs.put("15000003",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_LOVE,10));
        configs.put("15000004",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_LOVE,10));
        configs.put("15000005",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_LOVE,10));
        
        /* 勋章任务奖励 */
        configs.put("16000001",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_SUGAR,5));
        configs.put("16000002",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_LOVE,10));
        configs.put("16000003",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_LOVE,10));
        
        /* 财富等级奖励 */
        configs.put("17000001",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_LOVE,10));
        configs.put("17000002",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_LOVE,10));
        configs.put("17000003",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_LOVE,10));
        configs.put("17000004",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_LOVE,10));
        
        
     /* TODO 半价购买*/
        configs.put("17000005",new NewUserTaskReward(6,40000105,1)); 
        configs.put("17000006",new NewUserTaskReward(6,40001236,1));
        configs.put("17000007",new NewUserTaskReward(6,40000111,1));
        configs.put("17000008",new NewUserTaskReward(6,40000349,1));
        configs.put("17000009",new NewUserTaskReward(6,40000300,1));
        configs.put("17000010",new NewUserTaskReward(6,40000871,1));
        
        /* 守护类奖励  */
        configs.put("18000001",new NewUserTaskReward(AwardType.MEDAL,10017,7));
        configs.put("18000002",new NewUserTaskReward(AwardType.MEDAL,10017,7));
        configs.put("18000003",new NewUserTaskReward(AwardType.MEDAL,10017,7));
        configs.put("18000004",new NewUserTaskReward(AwardType.MEDAL,10017,7));
        configs.put("18000006",new NewUserTaskReward(AwardType.MEDAL,10017,14));
        configs.put("18000009",new NewUserTaskReward(AwardType.MEDAL,10017,21));
        configs.put("18000012",new NewUserTaskReward(AwardType.MEDAL,10017,21));
        
        /* 上跑道次数  */
        configs.put("19000001",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_LOVE,100)); //爱神弓箭（奖品）
        configs.put("19000002",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_LOVE,100));
        configs.put("19000003",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_LOVE,100));
        configs.put("19000004",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_LOVE,100));
        configs.put("19000005",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_LOVE,100));
     /* TODO 半价购买 */ 
        configs.put("19000007",new NewUserTaskReward(6,1,1));
        configs.put("19000010",new NewUserTaskReward(6,1,1));
      

        /* 幸运中奖 */
        configs.put("20000010",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_LOVE,10));
        configs.put("20000020",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_LOVE,10));
        configs.put("20000050",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_LOVE,10));
        configs.put("20000100",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_LOVE,20));
        configs.put("20000200",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_LOVE,50));
        configs.put("20000500",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_LOVE,100));
        configs.put("20001500",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_LOVE,200));
        
        
        /* 累计送礼天数 */
        configs.put("21000001",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_SUGAR,5));
        configs.put("21000002",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_SUGAR,5));
        configs.put("21000003",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_SUGAR,5));
        configs.put("21000004",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_SUGAR,5));//棒棒糖（奖品）
        configs.put("21000005",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_LOVE,10));//爱神弓箭（奖品）
        
        
        /* 累计观看时长 */
        configs.put("22000001",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_SUGAR,5));
        configs.put("22000005",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_SUGAR,5));
        configs.put("22000010",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_SUGAR,5));
        configs.put("22000030",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_SUGAR,5));
        configs.put("22000060",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_LOVE,10));
        configs.put("22000120",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_LOVE,10));
        
        /* 累计观看直播间数 */
        configs.put("23000001",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_SUGAR,5));
        configs.put("23000002",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_SUGAR,5));
        configs.put("23000003",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_SUGAR,5));
        configs.put("23000005",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_SUGAR,5));
        configs.put("23000007",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_LOVE,10));
        configs.put("23000010",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_LOVE,10));
        
        /* 送礼种类 */
        configs.put("24000001",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_SUGAR,5));
        configs.put("24000002",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_SUGAR,5));
        configs.put("24000003",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_LOVE,10));
        configs.put("24000004",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_LOVE,20));
        configs.put("24000005",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_LOVE,50));
        configs.put("24000006",new NewUserTaskReward(AwardType.GIFT,FREE_GIFT_LOVE,100));
        
        /* 宝箱 闯关勇士勋章 */
        configs.put("25000001",new NewUserTaskReward(6,10016,30));
        configs.put("25000002",new NewUserTaskReward(AwardType.CAR,1597,30));

        /* 半价抢购 */
        configs.put("26000001",new NewUserTaskReward(AwardType.DISCOUNT,40000316,1));
        configs.put("26000002",new NewUserTaskReward(AwardType.DISCOUNT,40000104,1));
        configs.put("26000005",new NewUserTaskReward(AwardType.DISCOUNT,40000105,1));
        configs.put("26000006",new NewUserTaskReward(AwardType.DISCOUNT,40001236,1));
        configs.put("26000007",new NewUserTaskReward(AwardType.DISCOUNT,40000111,1));
        configs.put("26000008",new NewUserTaskReward(AwardType.DISCOUNT,40000349,1));
        configs.put("26000009",new NewUserTaskReward(AwardType.DISCOUNT,40000300,1));
        configs.put("26000010",new NewUserTaskReward(AwardType.DISCOUNT,40000871,1));

        
    }
    /**
     *  更具任务号配置任务奖励 
     */
    public static NewUserTaskReward getConfig(String taskCode){
       return configs.get(taskCode);
    }
    
}

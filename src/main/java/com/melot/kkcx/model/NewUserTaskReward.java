/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2017
 */
package com.melot.kkcx.model;

import com.melot.kkcx.util.AwardType;

/**
 * Title: NewUserTaskReward
 * <p>
 * Description: TODO
 * </p>
 * @author  姚国平<a href="mailto:guoping.yao@melot.cn">
 * @version V1.0
 * @since 2017-12-7 下午2:01:37 
 */
public class NewUserTaskReward {
    
    /** 奖励类别
     *  awardType
     *       1 礼物
     *       2 座驾
     *       3 勋章
     *       4 vip
     *       5 svip   
     *       7 半价抢购
     */
    private int awardType;
    
    /** 奖励物id, 如礼物id */
    private int awardId;
    
    /** 奖励份数 */
    private int amount;

    public NewUserTaskReward(int awardType, int awardId, int amount) {
        this.awardType = awardType;
        this.awardId = awardId;
        this.amount = amount;
    }
    
    public NewUserTaskReward(AwardType typeEnum, int awardId, int amount) {
        this.awardType = typeEnum.getType();
        this.awardId = awardId;
        this.amount = amount;
    }

    public int getAwardType() {
        return awardType;
    }

    public void setAwardType(int awardType) {
        this.awardType = awardType;
    }

    public int getAwardId() {
        return awardId;
    }

    public void setAwardId(int awardId) {
        this.awardId = awardId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
    
}

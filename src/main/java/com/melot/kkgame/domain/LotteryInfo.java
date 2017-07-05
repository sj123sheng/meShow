/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.domain;

/**
 * Title: LotteryInfo
 * <p>
 * Description: 用户中彩信息
 * </p>
 * @author  姚国平<a href="mailto:guoping.yao@melot.cn">
 * @version V1.0
 * @since 2015-8-12 上午10:12:26 
 */
public class LotteryInfo {
    
    private Integer userId;
    
    /** 参与彩票次数 */
    private Integer lotteryTime;
    
    /** 获得中奖次数 */
    private Integer winLotteryTime;
    
    /** 获得总收入 */
    private Integer winProfit;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getLotteryTime() {
        return lotteryTime;
    }

    public void setLotteryTime(Integer lotteryTime) {
        this.lotteryTime = lotteryTime;
    }

    public Integer getWinLotteryTime() {
        return winLotteryTime;
    }

    public void setWinLotteryTime(Integer winLotteryTime) {
        this.winLotteryTime = winLotteryTime;
    }

    public Integer getWinProfit() {
        return winProfit;
    }

    public void setWinProfit(Integer winProfit) {
        this.winProfit = winProfit;
    }
    

}

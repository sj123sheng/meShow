/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2017
 */
package com.melot.kkcx.model;

import java.util.Date;

/**
 * Title: ActorProfit
 * <p>
 * Description: ActorProfit
 * </p>
 * 
 * @author 林端端<a href="mailto:duanduan.lin@melot.cn" />
 * @version V1.0
 * @since 2017年10月17日 上午11:31:07
 */
public class ActorProfit {

    //直播总时长（单位： 分钟）
    private int totalLiveTime;
    
    //当月收益
    private int totalRsv;
    
    //月份时间（格式： 2017.08）
    private String monthTime;
    
    //直播时长当月最后记录时间
    private Date recordDate;

    public int getTotalLiveTime() {
        return totalLiveTime;
    }
    
    public void setTotalLiveTime(int totalLiveTime) {
        this.totalLiveTime = totalLiveTime;
    }
    
    public int getTotalRsv() {
        return totalRsv;
    }
    
    public void setTotalRsv(int totalRsv) {
        this.totalRsv = totalRsv;
    }
    
    public String getMonthTime() {
        return monthTime;
    }
    
    public void setMonthTime(String monthTime) {
        this.monthTime = monthTime;
    }

    public Date getRecordDate() {
        return recordDate;
    }
    
    public void setRecordDate(Date recordDate) {
        this.recordDate = recordDate;
    }
    
}

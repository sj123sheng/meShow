package com.melot.kkcx.model;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * Title: 家族信息实体类
 * <p>
 * Description:  mapping table [cx_family_info]
 * </p>
 * 
 * @author 冯占飞<a href="mailto:zhanfei.feng@melot.cn">
 * @version V1.0
 * @since 2015年3月26日 下午5:21:33
 */
public class RecentFamilyMatch implements Serializable{
	
    private static final long serialVersionUID = 99297718739529261L;

    /** 本期期数 */
	private Integer thisPeriod;
	
	/** 本期最近的场次 */
	private Integer thisPlay;
	
	/** 本期最近的场次结束时间 */
	private Date thisEndTime;
	
	/** 上期期数 */
	private Integer lastPeriod;

    public Integer getThisPeriod() {
        return thisPeriod;
    }

    public void setThisPeriod(Integer thisPeriod) {
        this.thisPeriod = thisPeriod;
    }

    public Integer getThisPlay() {
        return thisPlay;
    }

    public void setThisPlay(Integer thisPlay) {
        this.thisPlay = thisPlay;
    }

    public Date getThisEndTime() {
        return thisEndTime;
    }

    public void setThisEndTime(Date thisEndTime) {
        this.thisEndTime = thisEndTime;
    }

    public Integer getLastPeriod() {
        return lastPeriod;
    }

    public void setLastPeriod(Integer lastPeriod) {
        this.lastPeriod = lastPeriod;
    }
}

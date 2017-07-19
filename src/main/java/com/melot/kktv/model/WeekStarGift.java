/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2017
 */
package com.melot.kktv.model;

import java.util.Date;

/**
 * Title: WeekStarGift
 * <p>
 * Description: 
 * </p>
 * 
 * @author 林端端<a href="mailto:duanduan.lin@melot.cn">
 * @version V1.0
 * @since 2017年4月20日 下午3:19:07
 */
public class WeekStarGift {
    
    private Integer giftId;
    
    private Integer relationGiftId;

    private Integer glevel;
    
    private String giftName;
    
    private Date starttime;

    public Integer getGiftId() {
        return giftId;
    }

    public void setGiftId(Integer giftId) {
        this.giftId = giftId;
    }
    
    public Integer getRelationGiftId() {
        return relationGiftId;
    }
    
    public void setRelationGiftId(Integer relationGiftId) {
        this.relationGiftId = relationGiftId;
    }

    public String getGiftName() {
        return giftName;
    }

    public void setGiftName(String giftName) {
        this.giftName = giftName;
    }

    public Integer getGlevel() {
        return glevel;
    }

    public void setGlevel(Integer glevel) {
        this.glevel = glevel;
    }

    public Date getStarttime() {
        return starttime;
    }

    public void setStarttime(Date starttime) {
        this.starttime = starttime;
    }
    
}

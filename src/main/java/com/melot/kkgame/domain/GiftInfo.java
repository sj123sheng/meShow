package com.melot.kkgame.domain;

/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */

import java.io.Serializable;

/**
 * Title: GiftInfo
 * Description: 礼物信息
 * @author  姚国平<a href="mailto:guoping.yao@melot.cn">
 * @version V1.0
 * @since 2015-12-17 上午10:29:51 
 */
public class GiftInfo implements Serializable{
    
    private static final long serialVersionUID = 8328357142358804247L;
    
    private Integer giftId;
    private String giftName;
    private Integer catalogId;
    private Integer isExclusive;
    private String unit;
    private Integer sendPrice;
    private Integer giftType;
    private Integer rsvPrice;
    private Integer luxury;
    private Integer valid;
    private Integer odds;
    private String desc;
    private Integer belong;
    private String catalogName;
    private Integer addRich;
    private Integer addActor;
    private Integer rsvType;
    public Integer getGiftId() {
        return giftId;
    }
    public void setGiftId(Integer giftId) {
        this.giftId = giftId;
    }
    public String getGiftName() {
        return giftName;
    }
    public void setGiftName(String giftName) {
        this.giftName = giftName;
    }
    public Integer getCatalogId() {
        return catalogId;
    }
    public void setCatalogId(Integer catalogId) {
        this.catalogId = catalogId;
    }
    public Integer getIsExclusive() {
        return isExclusive;
    }
    public void setIsExclusive(Integer isExclusive) {
        this.isExclusive = isExclusive;
    }
    public String getUnit() {
        return unit;
    }
    public void setUnit(String unit) {
        this.unit = unit;
    }
    public Integer getSendPrice() {
        return sendPrice;
    }
    public void setSendPrice(Integer sendPrice) {
        this.sendPrice = sendPrice;
    }
    public Integer getGiftType() {
        return giftType;
    }
    public void setGiftType(Integer giftType) {
        this.giftType = giftType;
    }
    public Integer getRsvPrice() {
        return rsvPrice;
    }
    public void setRsvPrice(Integer rsvPrice) {
        this.rsvPrice = rsvPrice;
    }
    public Integer getLuxury() {
        return luxury;
    }
    public void setLuxury(Integer luxury) {
        this.luxury = luxury;
    }
    public Integer getValid() {
        return valid;
    }
    public void setValid(Integer valid) {
        this.valid = valid;
    }
    public Integer getOdds() {
        return odds;
    }
    public void setOdds(Integer odds) {
        this.odds = odds;
    }
    public String getDesc() {
        return desc;
    }
    public void setDesc(String desc) {
        this.desc = desc;
    }
    public Integer getBelong() {
        return belong;
    }
    public void setBelong(Integer belong) {
        this.belong = belong;
    }
    public String getCatalogName() {
        return catalogName;
    }
    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }
    public Integer getAddRich() {
        return addRich;
    }
    public void setAddRich(Integer addRich) {
        this.addRich = addRich;
    }
    public Integer getAddActor() {
        return addActor;
    }
    public void setAddActor(Integer addActor) {
        this.addActor = addActor;
    }
    public Integer getRsvType() {
        return rsvType;
    }
    public void setRsvType(Integer rsvType) {
        this.rsvType = rsvType;
    }
    
    
}

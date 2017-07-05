/**
 * This document and its contents are protected by copyright 2012 and owned by
 * Melot Inc.
 * The copying and reproduction of this document and/or its content (whether
 * wholly or partly) or any
 * incorporation of the same into any other material in any media or format of
 * any kind is strictly prohibited.
 * All rights are reserved.
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.redis.external;

import java.util.Date;

import com.google.gson.JsonObject;
import com.melot.kkcore.user.api.UserProfile;

/**
 * Title: GiftInfo
 * <p>
 * Description: 礼物信息
 * </p>
 * 
 * @author 姚国平<a href=mailto:guoping.yao@melot.cn>
 * @version V1.0
 * @since 2015-7-14 下午2:55:37
 */
public class GiftInfo {

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

    public GiftInfo() {
    }
    
    public static GiftInfo createGiftInfo(JsonObject json) {
        GiftInfo gift = new GiftInfo();
        gift.setGiftId(json.get("giftId").getAsInt());
        gift.setGiftName(json.get("giftName").getAsString());
        gift.setCatalogId(json.get("catalogId").getAsInt());
        gift.setIsExclusive(json.get("isExclusive").getAsInt());
        gift.setUnit(json.get("unit").getAsString());
        gift.setSendPrice(json.get("sendPrice").getAsInt());
        gift.setGiftType(json.get("giftType").getAsInt());
        gift.setRsvPrice(json.get("rsvPrice").getAsInt());
        gift.setLuxury(json.get("luxury").getAsInt());
        gift.setValid(json.get("valid").getAsInt());
        gift.setOdds(json.get("odds").getAsInt());
        gift.setDesc(json.get("desc").getAsString());
        gift.setBelong(json.get("belong").getAsInt());
        gift.setCatalogName(json.get("catalogName").getAsString());
        gift.setAddRich(json.get("addRich").getAsInt());
        gift.setAddActor(json.get("addActor").getAsInt());
        gift.setRsvType(json.get("rsvType").getAsInt());
        return gift;
    }
    
    public JsonObject toJsonObject(Integer roomId,Integer count,UserProfile sender, UserProfile receiver){
        if(sender == null || receiver == null){
            return null;
        }
        //sendGift :10010208
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("MsgTag", "10010208");
        jsonObject.addProperty("roomId", roomId);
        jsonObject.addProperty("sUserId", sender.getUserId());
        jsonObject.addProperty("sNickname", sender.getNickName());
        jsonObject.addProperty("sPortrait", sender.getPortrait());
        jsonObject.addProperty("sRichLevel", sender.getUserLevel());
        jsonObject.addProperty("dUserId", receiver.getUserId());
        jsonObject.addProperty("dNickname", receiver.getNickName());
        jsonObject.addProperty("giftId", this.getGiftId()); 
        jsonObject.addProperty("giftCount", count);
        jsonObject.addProperty("giftName", this.getGiftName());
        jsonObject.addProperty("sendPrice", this.getSendPrice());
        jsonObject.addProperty("rsvPrice", this.getRsvPrice());
        jsonObject.addProperty("unit", this.getUnit());
        jsonObject.addProperty("luxury", this.getLuxury());
        jsonObject.addProperty("time", new Date().getTime());
        
        return jsonObject;
    }
    
    
}

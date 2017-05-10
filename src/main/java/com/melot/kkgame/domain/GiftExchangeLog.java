/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.domain;

import java.io.Serializable;
import java.util.Date;
import com.google.gson.JsonObject;
import com.melot.kktv.util.StringUtil;

/**
 * Title: GiftExchangeLog
 * <p>
 * Description: 礼物送礼记录
 * </p>
 * @author  姚国平<a href="mailto:guoping.yao@melot.cn">
 * @version V1.0
 * @since 2015-6-11 下午2:10:20 
 */
public class GiftExchangeLog implements Serializable{

    private static final long serialVersionUID = -3492706353949676459L;

    /** 主键 */
    private int recordId;
    
    /** 赠送人编号 */
    private int userId;
    
    /** 赠送人昵称 */
    private String senderName;
    
    /** 收礼人编号 */
    private int toUserId;
    
    /** 收礼人昵称 */
    private String receiverName;
    
    /** 房间编号 */
    private int roomId;
    
    /** 礼物编号 */
    private int giftId;
    
    /** 收到个数 */
    private int amount;
    
    /** 礼物名称 */
    private String giftName;
    
    /** 礼物单价 */
    private int giftPrice;

    /** 主播实际收到价格 */
    private int rsvPrice;
    
    /** 礼物单位 */
    private String unit;
    
    /** 收送礼时间 */
    private Date dayTime;

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getToUserId() {
        return toUserId;
    }

    public void setToUserId(int toUserId) {
        this.toUserId = toUserId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getGiftId() {
        return giftId;
    }

    public void setGiftId(int giftId) {
        this.giftId = giftId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getGiftName() {
        return giftName;
    }

    public void setGiftName(String giftName) {
        this.giftName = giftName;
    }

    public int getGiftPrice() {
        return giftPrice;
    }

    public void setGiftPrice(int giftPrice) {
        this.giftPrice = giftPrice;
    }

    public int getRsvPrice() {
        return rsvPrice;
    }

    public void setRsvPrice(int rsvPrice) {
        this.rsvPrice = rsvPrice;
    }

    public Date getDayTime() {
        return dayTime;
    }

    public void setDayTime(Date dayTime) {
        this.dayTime = dayTime;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    /**
     * 转成JsonObject
     */
    public JsonObject toJsonObject() {
        JsonObject result = new JsonObject();
        if (this.getUserId()!= 0) {
            result.addProperty("senderId", getUserId());
        }
      
        if (!StringUtil.strIsNull(senderName)) {
            result.addProperty("senderName", getSenderName());
        }
        
        if (this.getToUserId()!= 0) {
            result.addProperty("receiverId", getToUserId());
        }
      
        if (!StringUtil.strIsNull(receiverName)) {
            result.addProperty("receiverName", getReceiverName());
        }
       
        if (this.getGiftId()!= 0) {
            result.addProperty("giftId", getGiftId());
        }
        if (!StringUtil.strIsNull(giftName)) {
            result.addProperty("giftName", getGiftName());
        }
        result.addProperty("amount", getAmount());
        result.addProperty("giftPrice", getGiftPrice());
        result.addProperty("rsvPrice", getRsvPrice());
        if(dayTime != null){
        result.addProperty("sendTime", getDayTime().getTime());
        }
        if (!StringUtil.strIsNull(unit)) {
            result.addProperty("unit", getUnit());
        }
        return result;
    }
}

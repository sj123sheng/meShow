/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2018
 */
package com.melot.kktv.domain;

import java.io.Serializable;

import com.google.gson.JsonObject;

/**
 * Title: FreshItem
 * <p>
 * Description: FreshItem
 * </p>
 * 
 * @author 林端端<a href="mailto:duanduan.lin@melot.cn" />
 * @version V1.0
 * @since 2018年8月22日 上午10:04:13
 */
public class FreshItem implements Serializable{
    
    private static final long serialVersionUID = -2180184872917100218L;

    private int type;
    
    private int id;
    
    private boolean isHot;
    
    private String coverUrl;
    
    private long publishTime;
    
    private int userId;
    
    private int gender;
    
    private String portrait;
    
    private String nickname;
    
    private int viewsNum;
    
    private String desc;
    
    public int getType() {
        return type;
    }
    
    public void setType(int type) {
        this.type = type;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public boolean isHot() {
        return isHot;
    }
    
    public void setHot(boolean isHot) {
        this.isHot = isHot;
    }
    
    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public long getPublishTime() {
        return publishTime;
    }
    
    public void setPublishTime(long publishTime) {
        this.publishTime = publishTime;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getGender() {
        return gender;
    }
    
    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getPortrait() {
        return portrait;
    }
    
    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }
    
    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    
    public int getViewsNum() {
        return viewsNum;
    }
    
    public void setViewsNum(int viewsNum) {
        this.viewsNum = viewsNum;
    }
    
    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
    
    public JsonObject toJsonObject(FreshItem freshItem) {
        JsonObject result = new JsonObject();
        result.addProperty("type", freshItem.getType());
        result.addProperty("id", freshItem.getId());
        result.addProperty("isHot", freshItem.isHot);
        result.addProperty("coverUrl", freshItem.getCoverUrl());
        result.addProperty("publishTime", freshItem.getPublishTime());
        result.addProperty("userId", freshItem.getUserId());
        result.addProperty("gender", freshItem.getGender());
        if (freshItem.getPortrait() != null) {
            result.addProperty("portrait", freshItem.getPortrait());
        }
        if (freshItem.getNickname() != null) {
            result.addProperty("nickname", freshItem.getNickname());
        }
        result.addProperty("viewsNum", freshItem.getViewsNum());
        result.addProperty("desc", freshItem.getDesc());
        return result;
    }
    
}

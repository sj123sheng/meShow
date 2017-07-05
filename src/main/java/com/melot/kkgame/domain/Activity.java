/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.domain;

import java.util.Date;

import com.google.gson.JsonObject;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.PlatformEnum;

/**
 * Title: Activity
 * <p>
 * Description: 活动表实体对象
 * </p>
 * @author  姚国平<a href="mailto:guoping.yao@melot.cn">
 * @version V1.0
 * @since 2015-8-5 下午4:16:35 
 */
public class Activity {
    
    private static final int SECONDS_OF_THREE_DAYS = 3 * 24 * 3600 * 1000;
    
    private Integer activityId;
    private String imgURL;
    private String activityURL;
    private Date dtime;
    private String content;
    private Integer isShow;
    private Integer isTop;
    private String topURL;
    private String topMobileURL;
    private Date startDate; //开始日期
    private Date endDate; //结束日期
    private String topMobileURLIOS;
    private String activityMobileURL;
    private Integer isPushMessage;
    private String message;
    private Integer isHot;  
    private String activityTitle;
    private String sharedText;
    private String sharedImgURL;
    
    public Integer getActivityId() {
        return activityId;
    }
    public void setActivityId(Integer activityId) {
        this.activityId = activityId;
    }
    public String getImgURL() {
        return imgURL;
    }
    public void setImgURL(String imgURL) {
        this.imgURL = imgURL;
    }
    public String getActivityURL() {
        return activityURL;
    }
    public void setActivityURL(String activityURL) {
        this.activityURL = activityURL;
    }
    public Date getDtime() {
        return dtime;
    }
    public void setDtime(Date dtime) {
        this.dtime = dtime;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }
    public Integer getIsShow() {
        return isShow;
    }
    public void setIsShow(Integer isShow) {
        this.isShow = isShow;
    }
    public Integer getIsTop() {
        return isTop;
    }
    public void setIsTop(Integer isTop) {
        this.isTop = isTop;
    }
    public String getTopURL() {
        return topURL;
    }
    public void setTopURL(String topURL) {
        this.topURL = topURL;
    }
    public String getTopMobileURL() {
        return topMobileURL;
    }
    public void setTopMobileURL(String topMobileURL) {
        this.topMobileURL = topMobileURL;
    }
    public Date getStartDate() {
        return startDate;
    }
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    public Date getEndDate() {
        return endDate;
    }
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
    public String getTopMobileURLIOS() {
        return topMobileURLIOS;
    }
    public void setTopMobileURLIOS(String topMobileURLIOS) {
        this.topMobileURLIOS = topMobileURLIOS;
    }
    public String getActivityMobileURL() {
        return activityMobileURL;
    }
    public void setActivityMobileURL(String activityMobileURL) {
        this.activityMobileURL = activityMobileURL;
    }
    public Integer getIsPushMessage() {
        return isPushMessage;
    }
    public void setIsPushMessage(Integer isPushMessage) {
        this.isPushMessage = isPushMessage;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public Integer getIsHot() {
        return isHot;
    }
    public void setIsHot(Integer isHot) {
        this.isHot = isHot;
    }
    public String getActivityTitle() {
        return activityTitle;
    }
    public void setActivityTitle(String activityTitle) {
        this.activityTitle = activityTitle;
    }
    public String getSharedText() {
        return sharedText;
    }
    public void setSharedText(String sharedText) {
        this.sharedText = sharedText;
    }
    public String getSharedImgURL() {
        return sharedImgURL;
    }
    public void setSharedImgURL(String sharedImgURL) {
        this.sharedImgURL = sharedImgURL;
    }

    /**
     * 转成JsonObject
     */
    public JsonObject toJsonObject(int platform) {
        JsonObject result = new JsonObject();
        result.addProperty("activityId", this.getActivityId());
        if (this.getActivityTitle() != null) {
            result.addProperty("activityTitle", getActivityTitle());
        }
        result.addProperty("imgURL", getImgURL());
        if (StringUtil.strIsNull(activityURL) || this.activityURL.trim().equals("#")) {
            result.addProperty("activityURL", ConfigHelper.getActivityUrl() + activityId);
        } else {
            result.addProperty("activityURL", getActivityURL());
        }
        if (this.getDtime() != null) {
            result.addProperty("activityTime", getDtime().getTime());
        }
        if (!StringUtil.strIsNull(content)) {
            result.addProperty("content", getContent());
        }
        if (!StringUtil.strIsNull(topURL)) { // 非移动终端
            result.addProperty("topURL", getTopURL());
        }
        if (platform == PlatformEnum.ANDROID) { // 移动终端
            if (!StringUtil.strIsNull(topMobileURL)){
                result.addProperty("topMobileURL", getTopMobileURL());
            }
        } else if (platform == PlatformEnum.IPHONE || platform == PlatformEnum.IPAD) {
            if (!StringUtil.strIsNull(topMobileURLIOS)){
                result.addProperty("topMobileURL", getTopMobileURLIOS());
            }
        }
        if (!StringUtil.strIsNull(sharedText)) {
            result.addProperty("sharedText", getSharedText());
        }
        if (!StringUtil.strIsNull(sharedImgURL)) {
            result.addProperty("sharedImgURL", getSharedImgURL());
        }
        return result;
    }
    
    /**
     *  将对象转化为热门活动 json
     */
    public JsonObject toHotJsonObject() {
        JsonObject result = new JsonObject();
        result.addProperty("activityId", getActivityId());
        result.addProperty("imgURL", getImgURL() + "!160");
        result.addProperty("activityTitle", this.getActivityTitle());
        if (activityURL.trim().equals("#")) {
            result.addProperty("activityURL", ConfigHelper.getActivityUrl() + activityId);
        } else {
            result.addProperty("activityURL", this.getActivityURL());
        }
        if (getIsHot() == null || getIsHot().intValue() != 1) {
            if (this.getDtime().getTime() + SECONDS_OF_THREE_DAYS < System.currentTimeMillis()) {
                result.addProperty("activityTag", 2);// H 最多手工设置3条
            } else {
                result.addProperty("activityTag", 0);
            }
        } else {
            result.addProperty("activityTag", 1);// H 最多手工设置3条 1
        }
        return result;
    }
    
}

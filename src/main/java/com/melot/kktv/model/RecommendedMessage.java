package com.melot.kktv.model;

import java.util.Date;

import com.google.gson.JsonObject;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.PlatformEnum;

/**
 * 精彩推荐
 */
public class RecommendedMessage {

    private Integer msgId;
    
    private String title;
    
    private String content;
    
    private String imgUrl;// image url for android
    
    private String url;// the link url
    
    private String imgUrlIOS;// image url for ios
    
    private Date startTime;
    
    private Integer rwn;

    /**
     * @return JsonObject
     */
    public JsonObject toJsonObject(long lastReadTime, int platform) {

        JsonObject jObject = new JsonObject();
        jObject.addProperty("id", this.getMsgId());
        jObject.addProperty("title", this.getTitle());
        jObject.addProperty("message", this.getContent());
        jObject.addProperty("msgtime", this.getStartTime().getTime());
        if (this.url != null) {
            jObject.addProperty("activityURL", this.getUrl());
        }
        // jObject.addProperty("rwn", this.getRwn());
        if (platform == PlatformEnum.IPHONE || platform == PlatformEnum.IPAD) {
            if (this.imgUrlIOS != null && !this.imgUrlIOS.trim().equals("")) {
                jObject.addProperty("img", ConfigHelper.getHttpdir()
                        + this.imgUrlIOS);
            }
        } else {
            if (this.imgUrl != null && !this.imgUrl.trim().equals("")) {
                jObject.addProperty("img", ConfigHelper.getHttpdir()
                        + this.imgUrl);
            }
        }
        // 取得redis时间戳 返回isnew
        if (lastReadTime > 0 && lastReadTime <= this.getStartTime().getTime()) {
            jObject.addProperty("isnew", 1);
        } else {
            jObject.addProperty("isnew", 0);
        }
        
        return jObject;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getMsgId() {
        return msgId;
    }

    public void setMsgId(Integer MsgId) {
        this.msgId = MsgId;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getImgUrlIOS() {
        return this.imgUrlIOS;
    }

    public void setImgUrlIOS(String imgUrlIOS) {
        this.imgUrlIOS = imgUrlIOS;
    }

    public Integer getRwn() {
        return rwn;
    }

    public void setRwn(Integer rwn) {
        this.rwn = rwn;
    }

}

package com.melot.kkcx.model;

import java.util.Date;

/**
 * Title: Sticker
 * <p>
 * Description: 
 * </p>
 * 
 * @author 林端端<a href="mailto:duanduan.lin@melot.cn">
 * @version V1.0
 * @since 2017年2月20日 下午4:43:28
 */
public class Sticker {

    private int sId;
    
    private int platform;
    
    private String sName;
    
    private String previewUrl;
    
    private String sUrl;
    
    private String extraValue;
    
    private Date dtime;

    public int getsId() {
        return sId;
    }

    public void setsId(int sId) {
        this.sId = sId;
    }
    
    public int getPlatform() {
        return platform;
    }

    public void setPlatform(int platform) {
        this.platform = platform;
    }

    public String getsName() {
        return sName;
    }

    public void setsName(String sName) {
        this.sName = sName;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public String getsUrl() {
        return sUrl;
    }

    public void setsUrl(String sUrl) {
        this.sUrl = sUrl;
    }

    public String getExtraValue() {
        return extraValue;
    }

    public void setExtraValue(String extraValue) {
        this.extraValue = extraValue;
    }

    public Date getDtime() {
        return dtime;
    }

    public void setDtime(Date dtime) {
        this.dtime = dtime;
    }
    
}

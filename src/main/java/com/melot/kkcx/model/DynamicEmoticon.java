package com.melot.kkcx.model;

import java.util.Date;

/**
 * Title: DynamicEmoticon
 * <p>
 * Description: 
 * </p>
 * 
 * @author 林端端<a href="mailto:duanduan.lin@melot.cn">
 * @version V1.0
 * @since 2017年2月20日 下午4:30:02
 */
public class DynamicEmoticon {
    
    private int eId;
    
    private String eName;
    
    private String emoticonUrl;
    
    private String previewUrl;
    
    //1:全部 2：PC 3：app
    private int platform;
    
    private String extarValue;
    
    private String desc;
    
    private Date dtime;
    
    private int isOn;

    public int geteId() {
        return eId;
    }

    public void seteId(int eId) {
        this.eId = eId;
    }

    public String getEmoticonUrl() {
        return emoticonUrl;
    }

    public void setEmoticonUrl(String emoticonUrl) {
        this.emoticonUrl = emoticonUrl;
    }

    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    public int getPlatform() {
        return platform;
    }

    public void setPlatform(int platform) {
        this.platform = platform;
    }

    public String getExtarValue() {
        return extarValue;
    }

    public void setExtarValue(String extarValue) {
        this.extarValue = extarValue;
    }

    public Date getDtime() {
        return dtime;
    }

    public void setDtime(Date dtime) {
        this.dtime = dtime;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String geteName() {
        return eName;
    }

    public void seteName(String eName) {
        this.eName = eName;
    }

    public int getIsOn() {
        return isOn;
    }

    public void setIsOn(int isOn) {
        this.isOn = isOn;
    }

}

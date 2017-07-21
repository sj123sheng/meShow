package com.melot.kktv.model;

import java.util.Date;

/**
 * 
 * Title: VideoTape
 * <p>
 * Description: 录屏分享视频
 * </p>
 * 
 * @author 董毅<a href="mailto:yi.dong@melot.cn" />
 * @version V1.0
 * @since 2017-7-12 上午11:46:34
 */
public class VideoTape {

    private Integer videoTapeId;
    
    private Integer userId;
    
    private String filename;
    
    private String path_original;
    
    private Integer useable;
    
    private Date uploadTime;

    private Integer checkid;
    
    
    /**
     * @return the checkid
     */
    public Integer getCheckid() {
        return checkid;
    }


    
    /**
     * @param checkid the checkid to set
     */
    public void setCheckid(Integer checkid) {
        this.checkid = checkid;
    }


    /**
     * @return the videoTapeId
     */
    public Integer getVideoTapeId() {
        return videoTapeId;
    }

    
    /**
     * @param videoTapeId the videoTapeId to set
     */
    public void setVideoTapeId(Integer videoTapeId) {
        this.videoTapeId = videoTapeId;
    }

    
    /**
     * @return the userId
     */
    public Integer getUserId() {
        return userId;
    }

    
    /**
     * @param userId the userId to set
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    
    /**
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }

    
    /**
     * @param filename the filename to set
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    
    /**
     * @return the path_original
     */
    public String getPath_original() {
        return path_original;
    }

    
    /**
     * @param path_original the path_original to set
     */
    public void setPath_original(String path_original) {
        this.path_original = path_original;
    }

    
    /**
     * @return the useable
     */
    public Integer getUseable() {
        return useable;
    }

    
    /**
     * @param useable the useable to set
     */
    public void setUseable(Integer useable) {
        this.useable = useable;
    }

    
    /**
     * @return the uploadTime
     */
    public Date getUploadTime() {
        return uploadTime;
    }

    
    /**
     * @param uploadTime the uploadTime to set
     */
    public void setUploadTime(Date uploadTime) {
        this.uploadTime = uploadTime;
    }
    
}

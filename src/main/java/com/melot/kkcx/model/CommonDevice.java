/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2017
 */
package com.melot.kkcx.model;


/**
 * Title: CommonDevice
 * <p>
 * Description: 常用设备
 * </p>
 * 
 * @author 林端端<a href="mailto:duanduan.lin@melot.cn" />
 * @version V1.0
 * @since 2017年6月15日 下午5:30:19
 */
public class CommonDevice {
    
    private String deviceUId;
    
    private String deviceName;
    
    private String deviceModel;
    
    private long endTime;

    public String getDeviceUId() {
        return deviceUId;
    }
    
    public void setDeviceUId(String deviceUId) {
        this.deviceUId = deviceUId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}

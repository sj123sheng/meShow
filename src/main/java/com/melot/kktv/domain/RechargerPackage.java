/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2016
 */
package com.melot.kktv.domain;

/**
 * Title: RechargerPackage
 * <p>
 * Description: 
 * </p>
 * 
 * @author 褚菲<a href="mailto:fei.chu@melot.cn">
 * @version V1.0
 * @since 2016年10月11日 下午1:52:36
 */
public class RechargerPackage {
    
    private Integer packageId;
    
    private Integer status;
    
    private String orderId;

    private Integer isRecive;
    
    public Integer getPackageId() {
        return packageId;
    }

    public void setPackageId(Integer packageId) {
        this.packageId = packageId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Integer getIsRecive() {
        return isRecive;
    }

    public void setIsRecive(Integer isRecive) {
        this.isRecive = isRecive;
    }
    
}

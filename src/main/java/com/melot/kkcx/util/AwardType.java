/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2017
 */
package com.melot.kkcx.util;

/**
 * Title: AwardType
 * <p>
 * Description: TODO
 * </p>
 * @author  姚国平<a href="mailto:guoping.yao@melot.cn">
 * @version V1.0
 * @since 2017-12-26 上午10:15:13 
 */
public enum AwardType {

    GIFT(1), CAR(2), MEDAL(3), VIP(4), SVIP(5),DISCOUNT(7);
    private int type;
    private AwardType(int type) {
        this.type = type;
    }
    public int getType() {
        return type;
    }
}

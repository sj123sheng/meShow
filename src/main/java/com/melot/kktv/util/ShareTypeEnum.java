/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kktv.util;

/**
 * Title: ShareTypeEnum
 * <p>
 * Description: ShareTypeEnum
 * </p>
 * @author  姚国平<a href="mailto:guoping.yao@melot.cn">
 * @version V1.0
 * @since 2015-9-16 下午6:01:34 
 */
public enum ShareTypeEnum {

    WEIXIN_FRIEND(1), //微信好友
    WEIXIN_MOMENTS(2), //微信朋友圈
    QQ(3),  //qq好友
    Qzone(4),  //QQ空间
    SINA_WEIBO(5), //新浪微博
    RENREN(6),  //人人
    TENCENT_WEIBO(7), //腾讯微博
    BAIDU_BAR(8);  //百度贴吧
    
    int type;
    private ShareTypeEnum(int type) {
        this.type = type;
    }
    
}

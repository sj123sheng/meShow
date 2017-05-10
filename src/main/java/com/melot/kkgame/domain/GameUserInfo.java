/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.domain;

import java.io.Serializable;

/**
 * Title: GameUserInfo
 * <p>
 * Description: KKGame主播用户信息 
 * mapping to table <kkgame.kg_user_info>
 * </p>
 * @author  姚国平<a href="mailto:guoping.yao@melot.cn">
 * @version V1.0
 * @since 2015-3-6 上午10:43:29 
 */
public class GameUserInfo implements Serializable{
    
    private static final long serialVersionUID = -4306062326458308778L;

    private Integer userId;
    
    private Integer sendValue;
    
    private Integer receiveValue;
    
    private Integer receiveCloud;

    private Integer sendCloud;

    private Integer actorLevel;
    
    private Integer opusCout;
    
    public GameUserInfo(){
        this.receiveCloud = Integer.valueOf(0);
        this.actorLevel = Integer.valueOf(0);
    }
    
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getSendValue() {
        return sendValue;
    }

    public void setSendValue(Integer sendValue) {
        this.sendValue = sendValue;
    }

    public Integer getReceiveValue() {
        return receiveValue;
    }

    public void setReceiveValue(Integer receiveValue) {
        this.receiveValue = receiveValue;
    }
    

    public Integer getReceiveCloud() {
        return receiveCloud;
    }

    public void setReceiveCloud(Integer receiveCloud) {
        this.receiveCloud = receiveCloud;
    }

    public Integer getSendCloud() {
        return sendCloud;
    }

    public void setSendCloud(Integer sendCloud) {
        this.sendCloud = sendCloud;
    }

    public Integer getActorLevel() {
        return actorLevel;
    }

    public void setActorLevel(Integer actorLevel) {
        this.actorLevel = actorLevel;
    }

    public Integer getOpusCout() {
        return opusCout;
    }

    public void setOpusCout(Integer opusCout) {
        this.opusCout = opusCout;
    }
    
}

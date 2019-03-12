/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2019
 */
package com.melot.kkcx.model;

import java.io.Serializable;

/**
 * Title: HourRankInfo
 * <p>
 * Description: 小时榜排名信息
 * </p>
 * 
 * @author 林端端<a href="mailto:duanduan.lin@melot.cn" />
 * @version V1.0
 * @since 2019年1月9日 下午4:36:26
 */
public class HourRankInfo implements Serializable {

    private static final long serialVersionUID = 4469586165923450270L;
    
    private int roomId;
    
    private double score;
    
    private int position;
    
    private int preRoomId;
    
    private double preScore;
    
    private int prePosition;

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPreRoomId() {
        return preRoomId;
    }

    public void setPreRoomId(int preRoomId) {
        this.preRoomId = preRoomId;
    }

    public double getPreScore() {
        return preScore;
    }

    public void setPreScore(double preScore) {
        this.preScore = preScore;
    }

    public int getPrePosition() {
        return prePosition;
    }

    public void setPrePosition(int prePosition) {
        this.prePosition = prePosition;
    }

}

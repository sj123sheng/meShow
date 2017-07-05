/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.domain;

import java.util.Date;

/**
 * Title: KgFamilyInfo
 * <p>
 * </p>
 * @author  姚国平<a href="mailto:guoping.yao@melot.cn">
 * @version V1.0
 * @since 2015-12-9 上午10:27:31 
 */
public class KgFamilyInfo {

    private Integer familyId;
    private String familyName;
    private Integer familyLeader;
    private String familyPoster;
    private Integer medalId;
    private String familyNotice;
    private Integer actorCount;
    private Integer memberCount;
    private Date createTime;
    private Integer maxCount;
    private Integer open;
    private Integer actorLimit;
    private Integer type;
    private Integer operatorId;
    public Integer getFamilyId() {
        return familyId;
    }
    public void setFamilyId(Integer familyId) {
        this.familyId = familyId;
    }
    public String getFamilyName() {
        return familyName;
    }
    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }
    public Integer getFamilyLeader() {
        return familyLeader;
    }
    public void setFamilyLeader(Integer familyLeader) {
        this.familyLeader = familyLeader;
    }
    public String getFamilyPoster() {
        return familyPoster;
    }
    public void setFamilyPoster(String familyPoster) {
        this.familyPoster = familyPoster;
    }
    public Integer getMedalId() {
        return medalId;
    }
    public void setMedalId(Integer medalId) {
        this.medalId = medalId;
    }
    public String getFamilyNotice() {
        return familyNotice;
    }
    public void setFamilyNotice(String familyNotice) {
        this.familyNotice = familyNotice;
    }
    public Integer getActorCount() {
        return actorCount;
    }
    public void setActorCount(Integer actorCount) {
        this.actorCount = actorCount;
    }
    public Integer getMemberCount() {
        return memberCount;
    }
    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }
    public Date getCreateTime() {
        return createTime;
    }
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    public Integer getMaxCount() {
        return maxCount;
    }
    public void setMaxCount(Integer maxCount) {
        this.maxCount = maxCount;
    }
    public Integer getOpen() {
        return open;
    }
    public void setOpen(Integer open) {
        this.open = open;
    }
    public Integer getActorLimit() {
        return actorLimit;
    }
    public void setActorLimit(Integer actorLimit) {
        this.actorLimit = actorLimit;
    }
    public Integer getType() {
        return type;
    }
    public void setType(Integer type) {
        this.type = type;
    }
    public Integer getOperatorId() {
        return operatorId;
    }
    public void setOperatorId(Integer operatorId) {
        this.operatorId = operatorId;
    }
    
    
    
    
}

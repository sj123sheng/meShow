/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kktv.domain;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Title: AnnualRankFamily
 * <p>
 * Description: 
 * </p>
 * 
 * @author 宋建明<a href="mailto:jianming.song@melot.cn">
 * @version V1.0
 * @since 2015-1-6 下午8:37:27
 */
public class AnnualRankFamily {
    private int familyId;
    private String familyName;
    private String familyPortrait;
    private long score;
    private JsonArray userRank = new JsonArray();
    
    public void addUserRank(JsonObject jsonObject) {
        userRank.add(jsonObject);
    }
    
    public int getFamilyId() {
        return familyId;
    }
    
    public void setFamilyId(int familyId) {
        this.familyId = familyId;
    }
    
    public String getFamilyName() {
        return familyName;
    }
    
    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }
    
    public String getFamilyPortrait() {
        return familyPortrait;
    }
    
    public void setFamilyPortrait(String familyPortrait) {
        this.familyPortrait = familyPortrait;
    }
    
    public long getScore() {
        return score;
    }
    
    public void setScore(long score) {
        this.score = score;
    }
}

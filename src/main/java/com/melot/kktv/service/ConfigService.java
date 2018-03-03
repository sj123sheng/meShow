package com.melot.kktv.service;

import com.baidu.disconf.client.common.annotations.DisconfFile;
import com.baidu.disconf.client.common.annotations.DisconfFileItem;
import org.springframework.stereotype.Service;

/**
 * @description: ConfigService
 * @author: shengjian
 * @date: 2017/8/25
 * @copyright: Copyright (c)2017
 * @company: melot
 * <p>
 * Modification History:
 * Date              Author      Version     Description
 * ------------------------------------------------------------------
 * 2017/8/25           shengjian     1.0
 */
@Service
@DisconfFile(filename = "autoConfig.properties", targetDirPath = "conf")
public class ConfigService {

    private String challengerFamily;

    private String trumpFamily;

    private String trumpFamilyIds;

    private String goldMedalFamily;

    private String goldMedalFamilyIds;
    
    //限制渠道及版本号
    private String limitCvs;

    private boolean isAbroad;

    //是否特殊时期
    private boolean isSpecialTime;
    
    /**
     * 是否关闭自由主播申请
     */
    private boolean isCloseFreeApply;

    /**
     * 需要接入资源模块的资源类型
     */
    private String resourceType;

    private String checkUnpassPoster;

    /**
     * 一起玩大厅配置信息
     */
    private String playTogetherConfig;

    /**
     * 推荐列表 为了对比多个推荐算法的优劣 用于测试的渠道号列表
     */
    private String recommendChannelIds;

    //是否关闭手机号校验  1：关闭 
    private String closeCheckPhone;

    /**
     * 推荐列表 为了对比多个推荐算法的优劣 指定开始注册时间后的用户用于测试 时间戳(单位:毫秒)
     */
    private String recommendRegistrationTime;

    /**
     * 抓娃娃栏目id
     */
    private String catchDollCataId;

    /**
     * 一起玩大厅特殊渠道配置
     */
    private String playTogetherSpecialChannelConfig;
    
    /**
     * 贵族生效时间
     */
    private long startTime;

    @DisconfFileItem(name = "playTogether.specialChannel.config", associateField = "playTogetherSpecialChannelConfig")
    public String getPlayTogetherSpecialChannelConfig() {
        return playTogetherSpecialChannelConfig;
    }

    @DisconfFileItem(name = "catchDoll.cataId", associateField = "catchDollCataId")
    public String getCatchDollCataId() {
        return catchDollCataId;
    }

    @DisconfFileItem(name = "recommend.channelIds", associateField = "recommendChannelIds")
    public String getRecommendChannelIds() {
        return recommendChannelIds;
    }

    @DisconfFileItem(name = "recommend.registrationTime", associateField = "recommendRegistrationTime")
    public String getRecommendRegistrationTime() {
        return recommendRegistrationTime;
    }

    @DisconfFileItem(name = "playTogether.config", associateField = "playTogetherConfig")
    public String getPlayTogetherConfig() {
        return playTogetherConfig;
    }

    @DisconfFileItem(name = "challenger.family", associateField = "challengerFamily")
    public String getChallengerFamily() {
        return challengerFamily;
    }

    public void setChallengerFamily(String challengerFamily) {
        this.challengerFamily = challengerFamily;
    }

    @DisconfFileItem(name = "trump.family", associateField = "trumpFamily")
    public String getTrumpFamily() {
        return trumpFamily;
    }

    public void setTrumpFamily(String trumpFamily) {
        this.trumpFamily = trumpFamily;
    }

    @DisconfFileItem(name = "trump.familyIds", associateField = "trumpFamilyIds")
    public String getTrumpFamilyIds() {
        return trumpFamilyIds;
    }

    public void setTrumpFamilyIds(String trumpFamilyIds) {
        this.trumpFamilyIds = trumpFamilyIds;
    }

    @DisconfFileItem(name = "goldMedal.family", associateField = "goldMedalFamily")
    public String getGoldMedalFamily() {
        return goldMedalFamily;
    }

    public void setGoldMedalFamily(String goldMedalFamily) {
        this.goldMedalFamily = goldMedalFamily;
    }

    @DisconfFileItem(name = "goldMedal.familyIds", associateField = "goldMedalFamilyIds")
    public String getGoldMedalFamilyIds() {
        return goldMedalFamilyIds;
    }

    public void setGoldMedalFamilyIds(String goldMedalFamilyIds) {
        this.goldMedalFamilyIds = goldMedalFamilyIds;
    }
    
    @DisconfFileItem(name = "global.limitCvs", associateField = "limitCvs")
    public String getLimitCvs() {
        return limitCvs;
    }

    public void setLimitCvs(String limitCvs) {
        this.limitCvs = limitCvs;
    }

    @DisconfFileItem(name = "global.isSpecialTime", associateField = "isSpecialTime")
    public boolean getIsSpecialTime() {
        return isSpecialTime;
    }

    public void setIsSpecialTime(boolean isSpecialTime) {
        this.isSpecialTime = isSpecialTime;
    }
    
    @DisconfFileItem(name = "global.isCloseFreeApply", associateField = "isCloseFreeApply")
    public boolean getIsCloseFreeApply() {
        return isCloseFreeApply;
    }

    public void setIsCloseFreeApply(boolean isCloseFreeApply) {
        this.isCloseFreeApply = isCloseFreeApply;
    }

    @DisconfFileItem(name = "global.isAbroad", associateField = "isAbroad")
    public boolean getIsAbroad() {
        return isAbroad;
    }

    public void setIsAbroad(boolean isAbroad) {
        this.isAbroad = isAbroad;
    }

    @DisconfFileItem(name = "global.resourceType", associateField = "resourceType")
    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    @DisconfFileItem(name = "poster.checkunpass", associateField = "checkUnpassPoster")
    public String getCheckUnpassPoster() {
        return checkUnpassPoster;
    }

    public void setCheckUnpassPoster(String checkUnpassPoster) {
        this.checkUnpassPoster = checkUnpassPoster;
    }

    @DisconfFileItem(name = "global.closeCheckPhone", associateField = "closeCheckPhone")
    public String getCloseCheckPhone() {
        return closeCheckPhone;
    }
    
    public void setCloseCheckPhone(String closeCheckPhone) {
        this.closeCheckPhone = closeCheckPhone;
    }
    
    @DisconfFileItem(name = "nobility.startTime", associateField = "startTime")
    public long getStartTime() {
        return startTime;
    }
    
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    
}

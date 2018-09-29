package com.melot.kktv.service;

import org.springframework.stereotype.Service;

import com.baidu.disconf.client.common.annotations.DisconfFile;
import com.baidu.disconf.client.common.annotations.DisconfFileItem;

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

    private String annualFamily;

    private String annualFamilyIds;
    
    private String goldFamily;

    private String goldFamilyIds;
    
    private String trumpFamily;

    private String trumpFamilyIds;
    
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
     * 一起玩大厅配置信息（IOS）
     */
    private String playIOSTogetherConfig;

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

    /**
     * 即开彩web配置
     */
    private String lotteryWebConfig;
    
    /**
     * 即开彩APP配置
     */
    private String lotteryAppConfig;
    
    private String lotteryAppHalfBanner;
    
    /**
     * 地域昵称
     */
    private String regionNickname;
    
    /**
     * 即开彩banner文案
     */
    private String lotteryContent;
    
    /**
     * 一起玩大厅特殊渠道号
     */
    private String specifyChannel;
    
    /**
     * 三人欢乐PK主持人ID
     */
    private String pkCompereIds;

    /**
     * 世界杯竞猜栏目编号
     */
    private Integer worldCupPartId;

    /**
     * 世界杯纪念币购买直播间id
     */
    private Integer coinPurchaseRoomId;

    /**
     * 神秘人默认头像[没有域名部分]
     */
    private String xmanPortrait;

    /**
     * 区域化附近栏目同城距离起始值，单位米
     */
    private int nearbyStartDistance;

    /**
     * 区域化附近栏目中间距离，单位米
     */
    private int nearbyMiddleDistance;

    /**
     * 小于nearbyMiddleDistance，距离间隔值，单位米
     */
    private int nearbyDistanceBeforeInterval;

    /**
     * 大于nearbyMiddleDistance，距离间隔值，单位米
     */
    private int nearbyDistanceAfterInterval;

    /**
     * 区域化获取回放视频的时长下限,单位秒
     */
    private int replayVedioLowerDuration;

    /**
     * 区域化获取回放视频的时间限制,单位天
     */
    private int replayVedioLimitDay;
    
    /**
     * 首冲banner配置
     */
    private String chargeBanner;

    private String roundTimeStart;

    private Integer specialLevel;

    private String userLevelString;

    /**
     * 微信小程序appId定义
     */
    private String wechatProjectAppIds;

    private Integer baiduChannel;

    private String baiduChannelUrl;
    
    /**
     * K玩大厅游戏规则
     */
    private String kkPlayRule;
    
    /**
     * K玩大厅天梯赛配置
     */
    private String kkPlaySeasonConf;
    
    /**
     * K玩大厅栏目
     */
    private String kkPlayConfig;

    @DisconfFileItem(name = "nearbyStartDistance", associateField = "nearbyStartDistance")
    public int getNearbyStartDistance() {
        return nearbyStartDistance;
    }

    @DisconfFileItem(name = "nearbyMiddleDistance", associateField = "nearbyMiddleDistance")
    public int getNearbyMiddleDistance() {
        return nearbyMiddleDistance;
    }

    @DisconfFileItem(name = "nearbyDistanceBeforeInterval", associateField = "nearbyDistanceBeforeInterval")
    public int getNearbyDistanceBeforeInterval() {
        return nearbyDistanceBeforeInterval;
    }

    @DisconfFileItem(name = "nearbyDistanceAfterInterval", associateField = "nearbyDistanceAfterInterval")
    public int getNearbyDistanceAfterInterval() {
        return nearbyDistanceAfterInterval; 
    }

    @DisconfFileItem(name = "world.cup.partId", associateField = "worldCupPartId")
    public Integer getWorldCupPartId() {
        return worldCupPartId;
    }

    @DisconfFileItem(name = "coin.purchase.roomId", associateField = "coinPurchaseRoomId")
    public Integer getCoinPurchaseRoomId() {
        return coinPurchaseRoomId;
    }

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
    
    @DisconfFileItem(name = "playIOSTogetherConfig.config", associateField = "playIOSTogetherConfig")
    public String getPlayIOSTogetherConfig() {
        return playIOSTogetherConfig;
    }

    @DisconfFileItem(name = "challenger.family", associateField = "challengerFamily")
    public String getChallengerFamily() {
        return challengerFamily;
    }

    public void setChallengerFamily(String challengerFamily) {
        this.challengerFamily = challengerFamily;
    }

    @DisconfFileItem(name = "annual.family", associateField = "annualFamily")
    public String getAnnualFamily() {
        return annualFamily;
    }

    public void setAnnualFamily(String annualFamily) {
        this.annualFamily = annualFamily;
    }

    @DisconfFileItem(name = "annual.familyIds", associateField = "annualFamilyIds")
    public String getAnnualFamilyIds() {
        return annualFamilyIds;
    }

    public void setAnnualFamilyIds(String annualFamilyIds) {
        this.annualFamilyIds = annualFamilyIds;
    }
    
    @DisconfFileItem(name = "goldMedal.family", associateField = "goldFamily")
    public String getGoldFamily() {
        return goldFamily;
    }

    @DisconfFileItem(name = "goldMedal.familyIds", associateField = "goldFamilyIds")
    public String getGoldFamilyIds() {
        return goldFamilyIds;
    }
    
    @DisconfFileItem(name = "trump.family", associateField = "trumpFamily")
    public String getTrumpFamily() {
        return trumpFamily;
    }

    @DisconfFileItem(name = "trump.familyIds", associateField = "trumpFamilyIds")
    public String getTrumpFamilyIds() {
        return trumpFamilyIds;
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
    
    @DisconfFileItem(name = "lottery.lotteryWebConfig", associateField = "lotteryWebConfig")
    public String getLotteryWebConfig() {
        return lotteryWebConfig;
    }

    public void setLotteryWebConfig(String lotteryWebConfig) {
        this.lotteryWebConfig = lotteryWebConfig;
    }
    
    @DisconfFileItem(name = "lottery.lotteryAppConfig", associateField = "lotteryAppConfig")
    public String getLotteryAppConfig() {
        return lotteryAppConfig;
    }

    public void setLotteryAppConfig(String lotteryAppConfig) {
        this.lotteryAppConfig = lotteryAppConfig;
    }
    
    @DisconfFileItem(name = "lottery.lotteryAppHalfBanner", associateField = "lotteryAppHalfBanner")
    public String getLotteryAppHalfBanner() {
        return lotteryAppHalfBanner;
    }

    @DisconfFileItem(name = "lottery.lotteryContent", associateField = "lotteryContent")
    public String getLotteryContent() {
        return lotteryContent;
    }

    public void setLotteryContent(String lotteryContent) {
        this.lotteryContent = lotteryContent;
    }
    
    @DisconfFileItem(name = "global.regionNickname", associateField = "regionNickname")
    public String getRegionNickname() {
        return regionNickname;
    }

    public void setRegionNickname(String regionNickname) {
        this.regionNickname = regionNickname;
    }
    
    @DisconfFileItem(name = "playTogether.specifyChannel", associateField = "specifyChannel")
    public String getSpecifyChannel() {
        return specifyChannel;
    }

    public void setSpecifyChannel(String specifyChannel) {
        this.specifyChannel = specifyChannel;
    }

    @DisconfFileItem(name = "happyPK.pkCompereIds", associateField = "pkCompereIds")
    public String getPkCompereIds() {
        return pkCompereIds;
    }
    
    public void setPkCompereIds(String pkCompereIds) {
        this.pkCompereIds = pkCompereIds;
    }
    
    @DisconfFileItem(name = "xman.xmanPortrait", associateField = "xmanPortrait")
    public String getXmanPortrait() {
        return xmanPortrait;
    }
    
    public void setXmanPortrait(String xmanPortrait) {
        this.xmanPortrait = xmanPortrait;
    }

    @DisconfFileItem(name = "replayVedioLowerDuration", associateField = "replayVedioLowerDuration")
    public int getReplayVedioLowerDuration() {
        return replayVedioLowerDuration;
    }

    @DisconfFileItem(name = "replayVedioLimitDay", associateField = "replayVedioLimitDay")
    public int getReplayVedioLimitDay() {
        return replayVedioLimitDay;
    }
    
    @DisconfFileItem(name = "chargeBanner", associateField = "chargeBanner")
    public String getChargeBanner() {
        return chargeBanner;
    }

    @DisconfFileItem(name = "roundTimeStart", associateField = "roundTimeStart")
    public String getRoundTimeStart() {
        return roundTimeStart;
    }

    public void setRoundTimeStart(String roundTimeStart) {
        this.roundTimeStart = roundTimeStart;
    }

    @DisconfFileItem(name = "specialLevel", associateField = "specialLevel")
    public Integer getSpecialLevel() {
        return specialLevel;
    }

    public void setSpecialLevel(Integer specialLevel) {
        this.specialLevel = specialLevel;
    }

    @DisconfFileItem(name = "userLevelString", associateField = "userLevelString")
    public String getUserLevelString() {
        return userLevelString;
    }

    public void setUserLevelString(String userLevelString) {
        this.userLevelString = userLevelString;
    }
    
    @DisconfFileItem(name = "wechatProjectAppIds", associateField = "wechatProjectAppIds")
    public String getWechatProjectAppIds() {
        return wechatProjectAppIds; 
    }

    @DisconfFileItem(name = "baiduChannel", associateField = "baiduChannel")
    public Integer getBaiduChannel() {
        return baiduChannel;
    }

    public void setBaiduChannel(Integer baiduChannel) {
        this.baiduChannel = baiduChannel;
    }

    @DisconfFileItem(name = "baiduChannelUrl", associateField = "baiduChannelUrl")
    public String getBaiduChannelUrl() {
        return baiduChannelUrl;
    }

    public void setBaiduChannelUrl(String baiduChannelUrl) {
        this.baiduChannelUrl = baiduChannelUrl;
    }
    
    @DisconfFileItem(name = "kkPlayRule", associateField = "kkPlayRule")
    public String getKkPlayRule() {
        return kkPlayRule;
    }
    
    @DisconfFileItem(name = "kkPlayConfig", associateField = "kkPlayConfig")
    public String getKkPlayConfig() {
        return kkPlayConfig;
    }
    
    @DisconfFileItem(name = "kkPlaySeasonConf", associateField = "kkPlaySeasonConf")
    public String getKkPlaySeasonConf() {
        return kkPlaySeasonConf;
    }
    
}

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
@DisconfFile(filename = "autoConfig.properties", copy2TargetDirPath = "conf")
public class ConfigService {

    private String challengerFamily;

    private String trumpFamily;

    private String trumpFamilyIds;

    private String goldMedalFamily;

    private String goldMedalFamilyIds;

    private boolean isAbroad;

    //是否关闭手机号校验  1：关闭
    private String closeCheckPhone;

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

    @DisconfFileItem(name = "global.closeCheckPhone", associateField = "closeCheckPhone")
    public String getCloseCheckPhone() {
        return closeCheckPhone;
    }

    public void setCloseCheckPhone(String closeCheckPhone) {
        this.closeCheckPhone = closeCheckPhone;
    }

    @DisconfFileItem(name = "global.isAbroad", associateField = "isAbroad")
    public boolean getIsAbroad() {
        return isAbroad;
    }

    public void setIsAbroad(boolean isAbroad) {
        this.isAbroad = isAbroad;
    }

}

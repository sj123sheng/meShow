package com.melot.kktv.util.confdynamic;

import org.apache.log4j.Logger;

import com.melot.kktv.model.MedalInfo;
import com.melot.module.medal.driver.domain.ConfMedal;
import com.melot.module.medal.driver.service.ConfMedalService;
import com.melot.sdk.core.util.MelotBeanFactory;

public class MedalConfig {
    
    private MedalConfig() {}
	private static Logger logger = Logger.getLogger(MedalConfig.class);

	public static MedalInfo getMedal(int medalId){
	    try {
            MedalInfo medal = new MedalInfo();
            ConfMedalService confMedalService = (ConfMedalService) MelotBeanFactory.getBean("confMedalService");
            ConfMedal confMedalInfo = confMedalService.getConfMedalInfo(medalId);
            if (confMedalInfo == null) {
                return null;
            }
            medal.setMedalId(medalId);
            medal.setMedalIcon(confMedalInfo.getMedalMedalUrl());
            medal.setMedalType(confMedalInfo.getMedalType());
            if (confMedalInfo.getMedalRefid() != null) {
                medal.setMedalRefId(confMedalInfo.getMedalRefid());
            }
            medal.setMedalTitle(confMedalInfo.getMedalTitle());
            if(confMedalInfo.getMedalLevel() != null) {
                medal.setMedalLevel(confMedalInfo.getMedalLevel());
            }
            if(confMedalInfo.getMedalDes() != null) {
                medal.setMedalDesc(confMedalInfo.getMedalDes());
            }
            return medal;
        } catch (Exception e) {
            logger.error("getMedal(medalId=" + medalId + ")", e);
            return null;
        }
	}
}

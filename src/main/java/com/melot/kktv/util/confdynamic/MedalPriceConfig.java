package com.melot.kktv.util.confdynamic;

import java.util.List;

import org.apache.log4j.Logger;

import com.beust.jcommander.internal.Lists;
import com.melot.api.menu.sdk.utils.Collectionutils;
import com.melot.kktv.model.MedalPrice;
import com.melot.module.medal.driver.domain.MedalPriceConf;
import com.melot.module.medal.driver.service.ConfMedalService;
import com.melot.sdk.core.util.MelotBeanFactory;

public class MedalPriceConfig {
	private static Logger logger = Logger.getLogger(MedalPriceConfig.class);
	
	private MedalPriceConfig() {}
	
	public static List<MedalPrice> getMedalPriceListByType(int type){
	    try {
            ConfMedalService confMedalService = (ConfMedalService) MelotBeanFactory.getBean("confMedalService");
            List<MedalPriceConf> medalPriceConfs = confMedalService.getMedalPriceConf(type, 0);
            if (Collectionutils.isEmpty(medalPriceConfs)) {
                return null;
            }
            List<MedalPrice> medalPrices = Lists.newArrayList();
            for (MedalPriceConf medalPriceConf : medalPriceConfs) {
                MedalPrice medalPrice = new MedalPrice();
                medalPrice.setPeriod(medalPriceConf.getPeriod());
                medalPrice.setPrice(medalPriceConf.getPrice().intValue());
                medalPrices.add(medalPrice);
            }
            
            return medalPrices;
        } catch (Exception e) {
            logger.error("getMedalPriceListByType(type=" + type + ")", e);
            return null;
        }
	}
	
	
}

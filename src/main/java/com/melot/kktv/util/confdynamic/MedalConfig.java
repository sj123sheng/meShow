package com.melot.kktv.util.confdynamic;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.melot.kktv.redis.HotDataSource;
import com.melot.kktv.util.StringUtil;
import org.apache.log4j.Logger;

import com.melot.kktv.model.MedalInfo;
import com.melot.module.medal.driver.domain.ConfMedal;
import com.melot.module.medal.driver.service.ConfMedalService;
import com.melot.sdk.core.util.MelotBeanFactory;

public class MedalConfig {
    
    private MedalConfig() {}
	private static Logger logger = Logger.getLogger(MedalConfig.class);

    private static final String CACHE_KEY = "medalConfig_%s";

	public static MedalInfo getMedal(int medalId){
	    try {
	        String cacheKey = String.format(CACHE_KEY, medalId);
            String cacheValue = HotDataSource.getTempDataString(cacheKey);
            Gson gson = new Gson();
            if (StringUtil.strIsNull(cacheValue)) {
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
                HotDataSource.setTempDataString(cacheKey, gson.toJson(medal), 180);
                return medal;
            } else {
                return gson.fromJson(cacheValue, new TypeToken<MedalInfo>(){}.getType());
            }
        } catch (Exception e) {
            logger.error("getMedal(medalId=" + medalId + ")", e);
            return null;
        }
	}
}

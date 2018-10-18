package com.melot.kktv.util.confdynamic;

import com.melot.kktv.redis.HotDataSource;
import org.apache.log4j.Logger;

import com.melot.kk.config.api.domain.ConfSystemInfo;
import com.melot.kk.config.api.service.ConfigInfoService;
import com.melot.sdk.core.util.MelotBeanFactory;

import java.util.Objects;

public class SystemConfig {
    
    private static Logger logger = Logger.getLogger(SystemConfig.class);
    
    private SystemConfig() {}

	public static final java.lang.String versionSeverURL = "versionSeverURL";

	public static final java.lang.String versionKuaiYaSeverURL = "versionKuaiYaSeverURL";
	
	public static final java.lang.String versionPapaShowSeverURL = "versionPapaShowSeverURL";

	public static final java.lang.String activityMatchRankingET = "activityMatchRankingET";

	public static final java.lang.String familyMatchActorRankingET = "familyMatchActorRankingET";

	public static final java.lang.String matchVoteWebIconURL = "matchVoteWebIconURL";

	public static final java.lang.String matchVoteAndroidIconURL = "matchVoteAndroidIconURL";

	public static final java.lang.String matchVoteIphoneIconURL = "matchVoteIphoneIconURL";

	public static final java.lang.String matchVoteWebIconURLForCCTV = "matchVoteWebIconURLForCCTV";

	public static final java.lang.String matchVoteAndroidIconURLForCCTV = "matchVoteAndroidIconURLForCCTV";

	public static final java.lang.String matchVoteIphoneIconURLForCCTV = "matchVoteIphoneIconURLForCCTV";

	public static final java.lang.String userFinishedTaskRecordET = "userFinishedTaskRecordET";

	public static final java.lang.String maxViewedQueryCount = "maxViewedQueryCount";
	
	public static final String actorShareCoffer = "actorShareCoffer";
	
	public static final String shareCofferLimit = "shareCofferLimit";
	
	public static final String fanFeedbackMax = "fanFeedbackMax";
	
	public static final String fanFeedbackMin = "fanFeedbackMin";
	
	public static final String fanFeedbackDayLimit = "fanFeedbackDayLimit";
	
	public static final String fanFeedbackStartAmount = "fanFeedbackStartAmount";
	
	public static final String fanFeedbackEndAmount = "fanFeedbackEndAmount";

	private static final String CACHE_KEY = "systemConfig_%s";

	public static final String CACHE_NOT_EXIST_VALUE = "cache_not_exist_value";
	
	public static String getValue(String key, int appId) {
	    try {
	    	String cacheKey = String.format(CACHE_KEY, key);
			String fromCache = HotDataSource.getTempDataString(cacheKey);
			if (Objects.equals(CACHE_NOT_EXIST_VALUE, fromCache)) {
				// 如果是本身配置不存在的，返回null
				return null;
			}
			if (fromCache != null) {
				// 如果缓存配置存在，返回缓存结果
				return fromCache;
			}
			// 如果缓存失效
			ConfigInfoService configInfoService = (ConfigInfoService) MelotBeanFactory.getBean("configInfoService");
	        ConfSystemInfo confSystemInfo = configInfoService.getConfSystemInfoByKeyAndAppID(key, appId);
	        if (confSystemInfo == null) {
	        	// 配置不存在设置缓存特殊值
				HotDataSource.setTempDataString(cacheKey, CACHE_NOT_EXIST_VALUE, 180);
	            return null;
	        }
			String value = confSystemInfo.getcValue();
			HotDataSource.setTempDataString(cacheKey, value, 180);
	        return value;
		} catch (Exception e) {
            logger.error("getValue(key=" + key + ", appId=" + appId + ")", e);
            return null;
        }
	}
}

package com.melot.kktv.util.confdynamic;

import com.melot.module.config.Config;

public class SystemConfig {

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
	
	public static final String TABLENAME = "CONF_SYSTEM";

	public static String getValue(String key, int appId) {
		Object result = Config.find(TABLENAME, key, "VALUE", appId);
		if (result == null || "".equals(result))
			return null;
		return (String) result;
	}
}

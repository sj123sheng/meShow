package com.melot.kktv.util.confdynamic;

import java.util.Map;

import com.melot.module.config.Config;

public class ThirdAppConfig {
	public static final String TABLENAME = "CONF_THIRD";
	
	@SuppressWarnings("unchecked")
	public static Map<String, Object> getThirdInfo(int openPlatform) {
		return Config.find(TABLENAME, openPlatform);
	}
}

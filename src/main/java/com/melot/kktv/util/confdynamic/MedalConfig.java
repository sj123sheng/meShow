package com.melot.kktv.util.confdynamic;

import java.math.BigDecimal;
import java.util.Map;

import com.melot.kktv.model.MedalInfo;
import com.melot.module.config.Config;

public class MedalConfig {
	public static final String TABLENAME = "CONF_MEDAL";

	public static String getIcon(int medalId){
		BigDecimal keyValue = new BigDecimal(medalId + "");
		Object result = Config.find(TABLENAME, keyValue, "MEDAL_ICON");
		if(result == null || "".equals(result))	return null;
		return (String) result;
	}
	
	public static BigDecimal getType(int medalId){
		BigDecimal keyValue = new BigDecimal(medalId + "");
		Object result = Config.find(TABLENAME, keyValue, "MEDAL_TYPE");
		if(result == null || "".equals(result))	return null;
		return new BigDecimal(result + "");
	}
	
	public static BigDecimal getRefId(int medalId){
		BigDecimal keyValue = new BigDecimal(medalId + "");
		Object result = Config.find(TABLENAME, keyValue, "MEDAL_REFID");
		if(result == null || "".equals(result))	return null;
		return new BigDecimal(result + "");
	}

	public static String getTitle(int medalId){
		BigDecimal keyValue = new BigDecimal(medalId + "");
		Object result = Config.find(TABLENAME, keyValue, "MEDAL_TITLE");
		if(result == null || "".equals(result))	return null;
		return (String) result;
	}
	
	public static BigDecimal getLevel(int medalLevel) {
		BigDecimal keyValue = new BigDecimal(medalLevel + "");
		Object result = Config.find(TABLENAME, keyValue, "NEXT_LEVELMEDAL_ID");
		if(result == null || "".equals(result))	return null;
		return new BigDecimal(result + "");
	}

	@SuppressWarnings("unchecked")
	public static MedalInfo getMedal(int medalId){
		BigDecimal keyValue = new BigDecimal(medalId + "");
		Map<String, Object> resultMap = Config.find(TABLENAME, keyValue);
		if (resultMap == null || resultMap.size() == 0)	return null;
		MedalInfo medal = new MedalInfo();
		medal.setMedalId(medalId);
		medal.setMedalIcon((String)resultMap.get("MEDAL_ICON"));
		medal.setMedalType(new Integer(resultMap.get("MEDAL_TYPE") + ""));
		if (resultMap.get("MEDAL_REFID") != null) {
		    medal.setMedalRefId(new Integer(resultMap.get("MEDAL_REFID") + ""));
        }
		medal.setMedalTitle((String)resultMap.get("MEDAL_TITLE"));
		if(resultMap.get("NEXT_LEVELMEDAL_ID") != null) {
			medal.setMedalLevel(Integer.valueOf(resultMap.get("NEXT_LEVELMEDAL_ID")+""));
		}
		if(resultMap.get("MEDAL_DESC") != null) {
			medal.setMedalDesc((String)resultMap.get("MEDAL_DESC"));
		}
		return medal;
	}
}

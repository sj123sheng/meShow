package com.melot.kktv.util.confdynamic;

import java.math.BigDecimal;

import com.melot.module.config.Config;

public class GiftInfoConfig {
	
	public static final String TABLENAME = "GIFT_INFO";
	
	public static Integer getGiftSendPrice(int giftId) {
		Object result = Config.find(TABLENAME, giftId, "SENDPRICE");
		if (result == null || "".equals(result)) return null;
		return new BigDecimal(result + "").intValue();
	}
	
	public static String getGiftName(int giftId) {
	    BigDecimal keyValue = new BigDecimal(giftId + "");
        Object result = Config.find(TABLENAME, keyValue, "GIFTNAME");
        if (result == null || "".equals(result)) return null;
        return (String) result;
    }
}

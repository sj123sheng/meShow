package com.melot.kktv.util.confdynamic;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.melot.kktv.redis.HotDataSource;
import com.melot.kktv.util.StringUtil;
import com.melot.room.gift.constant.ReturnResultCode;
import com.melot.room.gift.domain.ReturnResult;
import com.melot.room.gift.dto.GiftDTO;
import com.melot.room.gift.service.GiftListService;
import com.melot.sdk.core.util.MelotBeanFactory;

public class GiftInfoConfig {
	
    private static Logger logger = Logger.getLogger(GiftInfoConfig.class);
    
    private static final String GIFTINFO_KEY = "%s_giftInfo";
	
	public static Integer getGiftSendPrice(int giftId) {
	    Integer result = null;
	    GiftDTO giftDTO = getGiftInfo(giftId);
	    if (giftDTO != null) {
	        result = giftDTO.getSendPrice();
	    }
		return result;
	}
	
	public static String getGiftName(int giftId) {
	    String result = null;
        GiftDTO giftDTO = getGiftInfo(giftId);
        if (giftDTO != null) {
            result = giftDTO.getGiftName();
        }
        return result;
    }
	
	public static GiftDTO getGiftInfo(int giftId) {
	    GiftDTO result = null;
	    try {
	        Gson gson = new Gson();
	        String giftInfoKey = String.format(GIFTINFO_KEY, giftId);
	        String giftInfoStr = HotDataSource.getTempDataString(giftInfoKey);
	        if (StringUtil.strIsNull(giftInfoStr)) {
	            GiftListService giftListService = (GiftListService) MelotBeanFactory.getBean("giftListService");
	            ReturnResult<GiftDTO> returnResult = giftListService.getGift(giftId);
	            if (returnResult != null && ReturnResultCode.SUCCESS.getCode().equals(returnResult.getCode())) {
	                result = returnResult.getData();
	                if (result != null) {
	                    HotDataSource.setTempDataString(giftInfoKey, gson.toJson(result), 180);
	                }
	            }
	        } else {
	            result = (gson.fromJson(giftInfoStr, new TypeToken<GiftDTO>(){}.getType()));
	        }
	    } catch (Exception e) {
	        logger.error("GiftInfoConfig.getGiftInfo execute exception, giftId: " + giftId, e);
	    }
	    return result;
	}
}

package com.melot.kktv.util.confdynamic;

import java.util.ArrayList;
import java.util.List;

import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;
import com.melot.module.config.Config;
import com.melot.module.config.model.MelotConfig;

public class InitConfig {

	public static void init(String path){
		List<MelotConfig> list = new ArrayList<MelotConfig>();
		
		MelotConfig medalConfig = new MelotConfig();
		medalConfig.setTableName("CONF_MEDAL");
		medalConfig.setKeyName("MEDAL_ID");
		medalConfig.setResult("MEDAL_ICON,MEDAL_TYPE,MEDAL_TITLE,MEDAL_REFID,MEDAL_DESC,NEXT_LEVELMEDAL_ID");
		list.add(medalConfig);
		
		MelotConfig medalPriceConfig = new MelotConfig();
		medalPriceConfig.setTableName("CONF_MEDALPRICE");
		medalPriceConfig.setKeyName("TYPE");
		medalPriceConfig.setResult("PERIOD,PRICE");
		medalPriceConfig.setQueryName("PERIOD");
		list.add(medalPriceConfig);
		
		MelotConfig systemConfig = new MelotConfig();
		systemConfig.setTableName("CONF_SYSTEM");
		systemConfig.setKeyName("KEY");
		systemConfig.setResult("VALUE,APPID");
		systemConfig.setQueryName("APPID");
		list.add(systemConfig);
		
		MelotConfig carConfig = new MelotConfig();
		carConfig.setTableName("CAR_INFO");
		carConfig.setKeyName("CARID");
		carConfig.setResult("CARID,NAME,PHOTO,ICON,NOWPRICE,MONTHSOLD,MONTHPRICE,IFLIMIT,HASSOLD,OPENSOLD,GETCONDITION");
        list.add(carConfig);
		
        MelotConfig giftInfoConfig = new MelotConfig();
        giftInfoConfig.setTableName("GIFT_INFO");
        giftInfoConfig.setKeyName("GIFTID");
        giftInfoConfig.setResult("SENDPRICE,GIFTNAME");
        list.add(giftInfoConfig);
        
        MelotConfig thirdConfig = new MelotConfig();
        thirdConfig.setTableName("CONF_THIRD");
        thirdConfig.setKeyName("OPENPLATFORM");
        thirdConfig.setResult("URL,KEY,DESCRIBE");
        list.add(thirdConfig);
        
		Config.init(list, path, SqlMapClientHelper.getInstance(DB.MASTER));
		
	}
	
}

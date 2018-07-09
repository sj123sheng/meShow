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

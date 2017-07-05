package com.melot.kktv.util.confdynamic;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.melot.kktv.model.MedalPrice;
import com.melot.module.config.Config;
import com.melot.module.config.model.ResultModel;

public class MedalPriceConfig {
	public static final String TABLENAME = "CONF_MEDALPRICE";

	public static BigDecimal getPrice(long type, int period){
		BigDecimal keyValue = new BigDecimal(type + "");
		BigDecimal queryValue = new BigDecimal(period + "");
		Object result = Config.find(TABLENAME, keyValue, "PRICE", queryValue);
		if(result == null || "".equals(result))	return null;
		return new BigDecimal(result + "");
	}
	
	@SuppressWarnings("unchecked")
	public static List<MedalPrice> getMedalPriceListByType(int type){
		List<MedalPrice> medalPriceList = new ArrayList<MedalPrice>();
		Map<Object, ResultModel> resultMap = (Map<Object, ResultModel>)Config.find(TABLENAME, type);
		if(resultMap == null || resultMap.size() == 0)	return null;
		for(Object key : resultMap.keySet()){
			ResultModel rm = resultMap.get(key);
			MedalPrice mp = new MedalPrice();
//			mp.setType(new BigDecimal(rm.get("TYPE") + ""));
			mp.setPeriod(new Integer(rm.get("PERIOD") + ""));
			mp.setPrice(new Integer(rm.get("PRICE") + ""));
			medalPriceList.add(mp);
		}
		return medalPriceList;
	}
	
	
}

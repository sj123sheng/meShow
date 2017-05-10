package com.melot.kktv.util.confdynamic;

import java.math.BigDecimal;
import java.util.Map;

import com.melot.kktv.domain.CarConfigInfo;
import com.melot.module.config.Config;

public class CarConfig {
	public static final String TABLENAME = "CAR_INFO";

	@SuppressWarnings("unchecked")
    public static CarConfigInfo getUserCar(int carId){
        BigDecimal keyValue = new BigDecimal(carId + "");
        Map<String, Object> resultMap = Config.find(TABLENAME, keyValue);
        if (resultMap == null || resultMap.size() == 0) {
            return null;
        }
        CarConfigInfo car = new CarConfigInfo();
        car.setId(carId);
        if (resultMap.get("NAME") != null) {
            car.setName((String)resultMap.get("NAME"));
        }
        if (resultMap.get("ICON") != null) {
            car.setIcon((String)resultMap.get("ICON")); 
        }
        if (resultMap.get("PHOTO") != null) {
            car.setPhoto((String)resultMap.get("PHOTO"));
        }
        if (resultMap.get("NOWPRICE") != null) {
            car.setPrice(new Integer(resultMap.get("NOWPRICE") + ""));
        }
        if (resultMap.get("MONTHSOLD") != null) {
            car.setMonthly(new Integer(resultMap.get("MONTHSOLD") + ""));
        }
        if (resultMap.get("MONTHPRICE") != null) {
            car.setMonthPrice(new Integer(resultMap.get("MONTHPRICE") + ""));
        }
        if (resultMap.get("IFLIMIT") != null) {
            car.setIfLimit(new Integer(resultMap.get("IFLIMIT") + ""));
        }
        if (resultMap.get("HASSOLD") != null) {
            car.setHasSold(new Integer(resultMap.get("HASSOLD") + ""));
        }
        if (resultMap.get("OPENSOLD") != null) {
            car.setOpenSold(new Integer(resultMap.get("OPENSOLD") + ""));
        }
        if (resultMap.get("GETCONDITION") != null) {
            car.setGetCondition(new Integer(resultMap.get("GETCONDITION") + ""));
        }
        return car;
    }
	
}

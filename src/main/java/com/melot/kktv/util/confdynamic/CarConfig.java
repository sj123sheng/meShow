package com.melot.kktv.util.confdynamic;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.melot.kktv.domain.CarConfigInfo;
import com.melot.kktv.redis.HotDataSource;
import com.melot.kktv.util.StringUtil;
import com.melot.module.packagegift.driver.domain.CarInfo;
import com.melot.module.packagegift.driver.service.CarService;
import com.melot.sdk.core.util.MelotBeanFactory;

public class CarConfig {
    
    private static Logger logger = Logger.getLogger(CarConfig.class);
    
    private static final String CARINFO_KEY = "%s_carInfo";

    public static CarConfigInfo getUserCar (int carId) {   
        CarConfigInfo result = new CarConfigInfo();
        try {
            Gson gson = new Gson();
            String carInfoKey = String.format(CARINFO_KEY, carId);
            String carInfoStr = HotDataSource.getTempDataString(carInfoKey);
            CarInfo carInfo = null;
            if (StringUtil.strIsNull(carInfoStr)) {
                CarService carService = (CarService) MelotBeanFactory.getBean("carService");
                carInfo = carService.getCarInfoById(carId);
                if (carInfo != null) {
                    HotDataSource.setTempDataString(carInfoKey, gson.toJson(carInfo), 180);
                }
            } else {
                carInfo = (gson.fromJson(carInfoStr, new TypeToken<CarInfo>(){}.getType()));
            }
            
            result.setId(carId);
            if (carInfo.getName() != null) {
                result.setName(carInfo.getName());
            }
            if (carInfo.getIcon() != null) {
                result.setIcon(carInfo.getIcon() ); 
            }
            if (carInfo.getPhoto() != null) {
                result.setPhoto(carInfo.getPhoto());
            }
            if (carInfo.getPrice() != null) {
                result.setPrice(carInfo.getPrice());
            }
            if (carInfo.getMonthly() != null) {
                result.setMonthly(carInfo.getMonthly());
            }
            if (carInfo.getMonthPrice() != null) {
                result.setMonthPrice(carInfo.getMonthPrice() );
            }
            if (carInfo.getIfLimit() != null) {
                result.setIfLimit(carInfo.getIfLimit());
            }
            if (carInfo.getHasSold() != null) {
                result.setHasSold(carInfo.getHasSold());
            }
            if (carInfo.getOpenSold() != null) {
                result.setOpenSold(carInfo.getOpenSold());
            }
            if (carInfo.getGetCondition() != null) {
                result.setGetCondition(carInfo.getGetCondition() );
            }
        } catch (Exception e) {
            logger.error("CarConfig.getUserCar execute exception, carId: " + carId, e);
        }
        
        return result;
    }
	
}

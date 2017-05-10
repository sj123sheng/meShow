package com.melot.kktv.action;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.kktv.domain.CarPriceInfo;
import com.melot.kkcx.service.AssetService;
import com.melot.kktv.util.AppChannelEnum;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.Constant;
import com.melot.kktv.util.HadoopLogger;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.module.api.exceptions.MelotModuleException;
import com.melot.module.msgbuilder.BaseAgent;
import com.melot.module.packagegift.driver.domain.CarInfo;
import com.melot.module.packagegift.driver.domain.CarPage;
import com.melot.module.packagegift.driver.domain.CarPrice;
import com.melot.module.packagegift.driver.domain.InsertCarMap;
import com.melot.module.packagegift.driver.domain.ResVip;
import com.melot.module.packagegift.driver.service.CarService;
import com.melot.module.packagegift.driver.service.VipService;
import com.melot.sdk.core.util.MelotBeanFactory;

public class AssetFunctions {
	
	private static Logger logger = Logger.getLogger(AssetFunctions.class);
	
	/**
	 * 获取汽车列表(20010101)
	 * @param jsonObject
	 * @return
	 * @throws Exception
	 */
	public JsonObject getCarList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		// define usable parameters
		int platform = 0;
		int monthSold = 0; //月供汽车
		int pageIndex = 1;
		int countPerPage = Constant.return_car_count;
		// parse the parameters
		JsonObject result = new JsonObject();
		try {
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, PlatformEnum.WEB, PlatformEnum.IPAD);
			monthSold = CommonUtil.getJsonParamInt(jsonObject, "monthSold", 0, null, 0, Integer.MAX_VALUE);
			pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
			countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", Constant.return_car_count, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
		} catch(CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch(Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		// call module service interface
		CarService carService = (CarService) MelotBeanFactory.getBean("carService");
		CarPage resp = carService.getCarList(monthSold, pageIndex, countPerPage);
		if (resp != null) {
		    int pageTotal = resp.getPageTotal();
		    if (pageTotal > 0) {
		        JsonArray carJsonArr = new JsonArray();
		        try {
		            List<CarInfo> carList = resp.getCars();
		            for (CarInfo car : carList) {
		                CarInfo carInfo = new CarInfo();
		                carInfo.setId(car.getId());
		                carInfo.setName(car.getName());
		                carInfo.setPrice(car.getPrice());
		                if (platform == PlatformEnum.ANDROID) {
		                    carInfo.setPhoto(ConfigHelper.getParkCarAndroidResURL() + car.getPhoto());
		                    carInfo.setIcon(ConfigHelper.getParkLogoAndroidResURL() + car.getIcon());
		                } else if (platform == PlatformEnum.IPHONE) {
		                    carInfo.setPhoto(ConfigHelper.getParkCarResURL() + car.getPhoto());
		                    carInfo.setIcon(ConfigHelper.getParkLogoResURL() + car.getIcon());
		                } else if (platform == PlatformEnum.IPAD) {
		                    carInfo.setPhoto(ConfigHelper.getParkCarResURL() + car.getPhoto());
		                    carInfo.setIcon(ConfigHelper.getParkLogoResURL() + car.getIcon());
		                } else {
		                    carInfo.setPhoto(car.getPhoto());
		                    carInfo.setIcon(car.getIcon());
		                }
		                JsonObject carJson = new JsonParser().parse(new Gson().toJson(carInfo)).getAsJsonObject();
		                carJsonArr.add(carJson);
		            }
		        } catch (Exception e) {
		            logger.error("fail to parse java object to json object.", e);
		        }
		        result.add("carList", carJsonArr);
		    }
		    result.addProperty("pageTotal", pageTotal);
		    result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		    if (platform == PlatformEnum.WEB) {
		        // 获取汽车月供价格
		    	List<CarPrice> carList = carService.getCarDiscountList();
		        if (carList != null) {
		            JsonArray priceJsonArr = new JsonArray();
		            try {
		                for (CarPrice carPrice : carList) {
		                    CarPriceInfo carPriceInfo = new CarPriceInfo();
		                    carPriceInfo.setMonth(carPrice.getMonth());
		                    carPriceInfo.setDiscount(carPrice.getDiscount());
		                    JsonObject carJson = new JsonParser().parse(new Gson().toJson(carPriceInfo)).getAsJsonObject();
		                    priceJsonArr.add(carJson);
		                }
		            } catch (Exception e) {
		                logger.error("fail to parse java object to json object.", e);
		            }
		            result.add("carPriceList", priceJsonArr);
		        }
		    }
		} else {
			result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
		}
		return result;
	}
	
	/**
	 * 获取汽车月价格区间(20010103)
	 * @param jsonObject
	 * @return
	 * @throws Exception
	 */
	public JsonObject getCarMonthlyPriceList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		JsonObject result = new JsonObject();
		// call module service interface
		CarService carService = (CarService) MelotBeanFactory.getBean("carService");
		List<CarPrice> carList = carService.getCarDiscountList();
		if (carList != null) {
		    JsonArray carPriceJsonArr = new JsonArray();
		    try {
		        for (CarPrice carPrice : carList) {
		            CarPriceInfo carPriceInfo = new CarPriceInfo();
		            carPriceInfo.setMonth(carPrice.getMonth());
		            carPriceInfo.setDiscount(carPrice.getDiscount());
		            JsonObject carJson = new JsonParser().parse(new Gson().toJson(carPriceInfo)).getAsJsonObject();
		            carPriceJsonArr.add(carJson);
		        }
		    } catch (Exception e) {
		        logger.error("fail to parse java object to json object.", e);
		    }
		    result.add("carPriceList", carPriceJsonArr);
		    result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		} else {
			result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
		}
		return result;
	}

	/**
	 * 按月购买/续费汽车(20010105)
	 * @param jsonObject
	 * @return
	 * @throws Exception
	 */
	public JsonObject buyCarMonthly(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
	 // 该接口需要验证token,未验证的返回错误码
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
	    
		// define usable parameters
		int userId = 0;
		int carId = 0;
		int months = 0;
		int referrerId = 0;
		int toUserId = 0;
		// parse the parameters
		try{
			// check user token
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
			carId = CommonUtil.getJsonParamInt(jsonObject, "carId", 0, TagCodeEnum.PRODUCT_ID_MISSING, 1, Integer.MAX_VALUE);
			months = CommonUtil.getJsonParamInt(jsonObject, "months", 0, TagCodeEnum.MONTHS_MISSING, 1, Integer.MAX_VALUE);
			referrerId = CommonUtil.getJsonParamInt(jsonObject, "referrerId", 0, null, 1, Integer.MAX_VALUE);
			toUserId = CommonUtil.getJsonParamInt(jsonObject, "toUserId", 0, null, 1, Integer.MAX_VALUE);
		} catch(CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch(Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		// call module service interface
        InsertCarMap resp = null;
        try {
    		CarService carService = (CarService) MelotBeanFactory.getBean("carService");
            if (toUserId > 0) {
                resp = carService.insertBuyCarV2(userId, carId, referrerId, months, toUserId);
            } else {
                resp = carService.insertBuyCar(userId, carId, referrerId, months);
            }
        } catch (MelotModuleException e) {
            switch (e.getErrCode()) {
            case 101:
                // 用户余额不足
                result.addProperty("TagCode", TagCodeEnum.USER_MONEY_SHORTNESS);
                break;
                
            case 104:
                // 用户不存在
                result.addProperty("TagCode", TagCodeEnum.USER_NOT_EXIST);
                break;
            
            case 102:
                // 汽车不存在
                result.addProperty("TagCode", TagCodeEnum.PRODUCT_NOT_EXIST);
                break;
                
            case 105:
                // 汽车月价格不存在
                result.addProperty("TagCode", TagCodeEnum.MONTH_PRICE_NOT_EXIST);
                break;
                
            case 103:
                // 购买汽车失败
                result.addProperty("TagCode", TagCodeEnum.BUY_PRODUCT_FAILED);
                break;
                
            case 110:
                //此车辆不能续费
                result.addProperty("TagCode", TagCodeEnum.CAR_CAN_NOT_RENEW);
                break;
                
            default:
                result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                break;
            }
            return result;
        } catch (Exception e) {
            logger.error("call CarService.insertBuyCar exception, ", e);
        }
		if (resp != null) {
		    result.addProperty("ucId", resp.getUcId());
		    result.addProperty("endTime", resp.getEndTime());
		    result.addProperty("TagCode", TagCodeEnum.SUCCESS); 
		} else {
			result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
		}
		
		return result;
	}

	/**
	 * 购买道具(10005010)
	 * @param jsonObject
	 * @param checkTag
	 * @return
	 * @throws Exception
	 */
	public JsonObject buyProp(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
	       
	       // 该接口需要验证token,未验证的返回错误码
	        JsonObject result = new JsonObject();
	        if (!checkTag) {
	            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
	            return result;
	        }
	        
	        // 定义所需参数
	        int userId = 0;
	        int propId = 0;
	        int periodOfValidity = 0;
	        int referrerId = 0;
	        int platform = 0;
	        int appId = 0;
	        int channel = 0;
	        Integer friendId = 0;
	        // 解析参数
	        try{
	            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "05100001", 1, Integer.MAX_VALUE);
	            propId = CommonUtil.getJsonParamInt(jsonObject, "propId", 0, "05100003", 1, Integer.MAX_VALUE);
	            periodOfValidity = CommonUtil.getJsonParamInt(jsonObject, "periodOfValidity", 0, "05100005", -1, Integer.MAX_VALUE);
	            referrerId = CommonUtil.getJsonParamInt(jsonObject, "referrerId", 0, null, 1, Integer.MAX_VALUE);
	            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, 1, Integer.MAX_VALUE);
	            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 1, Integer.MAX_VALUE);
	            channel = CommonUtil.getJsonParamInt(jsonObject, "c", AppChannelEnum.KK, null, 1, Integer.MAX_VALUE);
	            friendId = CommonUtil.getJsonParamInt(jsonObject, "friendId", 0, null, 1, Integer.MAX_VALUE);
	        } catch(CommonUtil.ErrorGetParameterException e) {
	            result.addProperty("TagCode", e.getErrCode());
	            return result;
	        } catch(Exception e) {
	            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
	            return result;
	        }
	        
	        ResVip resp = AssetService.buyVip(userId, propId, periodOfValidity, referrerId, friendId);
	        if (resp != null) {
	            if (BaseAgent.RESP_CODE_SUCCESS == resp.getRespCode()) {
	                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
	                HadoopLogger.buyPropLog(userId, referrerId, propId, periodOfValidity, new Date(), platform, appId, channel);
	            } else {
	                if (-1 == resp.getRespCode()) {
	                    // 用户不存在
	                    result.addProperty("TagCode", TagCodeEnum.USER_NOT_EXIST);
	                } else if(-2 == resp.getRespCode()) {
	                    // 用户余额不足
	                    result.addProperty("TagCode", "05100106");
	                } else if(-3 == resp.getRespCode()) {
	                    // SQL异常
	                    result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
	                } else if(-4 == resp.getRespCode()) {
	                    // 道具不存在,或道具有效时长不符合要求
	                    result.addProperty("TagCode", "05100104");
	                } else if (-5 == resp.getRespCode()){
	                    // 用户已经是该道具的终身用户,无需继续购买,请选择商城其他道具
                        result.addProperty("TagCode", "05100105");
	                } else if(-6 == resp.getRespCode()) {
	                    // 购买道具失败(不满足购买条件)
	                    result.addProperty("TagCode", TagCodeEnum.NOT_MEET_CONDITION);
	                } else {
	                    result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
	                }
	            }
	        } else {
	            result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
	        }
	        
	        return result;
	    }
	
	/**
	 * 获取超级vip小喇叭相关信息(10005072)
	 * @param jsonObject
	 * @param checkTag
	 * @param request
	 * @return
	 */
	public JsonObject getSvipSpeaklouderCount(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		JsonObject result = new JsonObject();
      
        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        VipService vipService = (VipService) MelotBeanFactory.getBean("vipService");
        if (vipService != null) {
        	Integer count = vipService.getSvipDailyLoudspeakerCount(userId);
        	if (count == null) {
        		result.addProperty("usedCount", 0);
        	} else {
        		result.addProperty("usedCount", count);
        	}        	
        } else {
        	result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
        	return result;
        }
        result.addProperty("totalCount", 3);
        result.addProperty("originalPrice", 10000);
        result.addProperty("nowPrice", 8000);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        
        return result;
	}

    /**
     * 获取座驾详细信息50005001
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getCarInfoDetail(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int carId;
        try {
            carId = CommonUtil.getJsonParamInt(jsonObject, "carId", 0, "05010001", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        CarService carService = (CarService) MelotBeanFactory.getBean("carService");
        CarInfo carInfo = carService.getCarInfoById(carId);
        if (carInfo != null) {
            result.addProperty("carId", carId);
            result.addProperty("carName", carInfo.getName());
            result.addProperty("photo", carInfo.getPhoto());
            result.addProperty("monthPrice", carInfo.getMonthPrice() != null ? carInfo.getMonthPrice() : 0);
            
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } else {
            result.addProperty("TagCode", "05010002");
        }
        
        return result;
    }
    
}

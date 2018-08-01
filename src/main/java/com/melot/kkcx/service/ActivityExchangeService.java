package com.melot.kkcx.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.melot.kktv.domain.CarInfo;
import com.melot.kktv.domain.ExpConfInfo;
import com.melot.kktv.domain.StorehouseInfo;
import com.melot.kktv.redis.HotDataSource;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;
import com.melot.module.packagegift.driver.domain.InsertCarMap;
import com.melot.module.packagegift.driver.domain.ResVip;
import com.melot.module.packagegift.driver.service.CarService;
import com.melot.module.packagegift.driver.service.VipService;
import com.melot.sdk.core.util.MelotBeanFactory;

public class ActivityExchangeService {
	
	private static Logger logger = Logger.getLogger(ActivityExchangeService.class);
	
	private static final String CACHE_KEY = "Exchange.Cache.%s";
	
	/**
	 * 判断库存是否满足表达式输入(兑换条件)
	 * @param userId
	 * @param expIn
	 * @return
	 */
	public static boolean isSatisfy(int userId, ExpConfInfo expConf) {
		int giftId = 0;
	    int num = 0;
	    int type = 0;
		JsonArray optionArray = new JsonParser().parse(expConf.getExpIn()).getAsJsonArray();
		for (int i = 0; i < optionArray.size(); i++) {
			JsonObject expInJson = optionArray.get(i).getAsJsonObject();
			try{
			    giftId = CommonUtil.getJsonParamInt(expInJson, "id", 0, null, 1, Integer.MAX_VALUE);
			    num = CommonUtil.getJsonParamInt(expInJson, "num", 0, null, 1, Integer.MAX_VALUE);
			    type = CommonUtil.getJsonParamInt(expInJson, "type", 0, null, 1, Integer.MAX_VALUE);
			} catch(Exception e) {
			    logger.error("fail to parse expIn parameter", e);	
				return false;
			}
			if (num > 0) {
			    try {
		            switch (type) {
		            case 1:// 礼物
		                List<StorehouseInfo> list = StorehouseService.getUserGiftCount(userId, String.valueOf(giftId));
		                if (list != null && list.size() > 0) {
		                    int quanlity = list.get(0).getQuantity();
		                    if(quanlity >= num){
		                        continue;
		                    }
		                }
		                return false;
		                
		            case 2:
		                // 暂时不支持扣VIP
                        return false;
		                
		            case 3:
		                // 暂时不支持扣座驾
                        return false;
		                
		            case 4:
		                //扣秀币
		                if (num > com.melot.kktv.service.UserService.getUserShowMoney(userId)) {
		                    return false;
		                }
		                break;
		                
		            default:
		                return false;
		            }
		        } catch (Exception e) {
		            logger.error("ActivityExchangeService.isSatisfy(" + userId + ", " + (expConf == null ? "" : new Gson().toJson(expConf)) + ") Exception", e);
		        }
			}
		}
		return true;
	}
	
	/**
	 * 判断用户是否禁止重复兑换
	 * @param userId
	 * @param expId
	 * @return
	 */
	public static boolean checkNoRepeatExchange(int userId, int expId) {
	    return HotDataSource.hasTempData(String.format(CACHE_KEY, expId), String.valueOf(userId));
	}
	
	private static void setExchangeCache(int userId, ExpConfInfo expConf) {
	    long limitTime = expConf.getLimitTime();
	    if (limitTime > 0) {
	        HotDataSource.setTempData(String.format(CACHE_KEY, expConf.getExpId()), String.valueOf(userId), (System.currentTimeMillis() / 1000) + limitTime);
        }
	}
	
	/**
	 * 送扣库存/送车/送会员
	 * @param isIn
	 * @param userId
	 * @param giftDesc
	 * @param tempExpData
	 * @return
	 */
	private static boolean giftInOut(boolean isIn, int userId,String giftDesc,TempExpData tempExpData) {
		
	    int giftType = tempExpData.getType();
	    int giftId = tempExpData.getId();
	    int num = tempExpData.getNum();
	    String giftName = tempExpData.getName();
	    
		if (userId < 1 || giftType < 1 || giftId < 1 || giftName == null || "".equals(giftName.trim())) {
			return false;
		}
		
		if (num < 1) {
			return true;
		}
		
		if (!isIn) {
			num = - num;
		}
		
		boolean result = false;
		
		try {
			switch (giftType) {
			case 1:
				//送礼物
				result = StorehouseService.addUserGift(userId, giftId, num, giftName, 7, giftDesc);
				break;
				
			case 2:
				// 暂时不支持扣回VIP
				if (num < 0) {
					break;
				}
				
				//送会员
				VipService vipService = (VipService) MelotBeanFactory.getBean("vipService");
				ResVip resVip = vipService.insertSendVip(userId, num, giftId, 7, giftDesc);
				if (resVip.getRespCode() == 0) {
					result = true;
				}
				break;
				
			case 3:
				// 暂时不支持扣回座驾
				if (num < 0) {
					break;
				}
				
				//送座驾
				CarService carService = (CarService) MelotBeanFactory.getBean("carService");
				InsertCarMap insertCarMap = carService.insertSendCar(userId, giftId, num, 11, giftDesc);
				if (insertCarMap != null && insertCarMap.getEndTime() > 0) {
				    result = true;
				}
				break;
                
            case 4:
                //扣秀币
                result = com.melot.kktv.service.UserService.incUserShowMoneyV2(userId, num, false, null);
                break;
				
			default:
				break;
			}
		} catch (Exception e) {
    		logger.error("ActivityExchangeService.giftInOut(" + isIn + ", " + userId + ", " + giftType + ", " + giftId + ", " + num + ", " + giftName + ", " + giftDesc + ") Exception", e);
		}
		return result;
	}
	
	private class TempExpData {
		private int type;
		private int id;
		private String name;
		private int num;
		
		/**
		 * @param giftType
		 * @param giftId
		 * @param giftName
		 * @param num
		 */
	
		public int getNum() {
			return num;
		}

		public int getType() {
			return type;
		}

		public int getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		
	}

}

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
	 * 获得兑换配置信息
	 * @param expId
	 * @return expConfInfo
	 */
	public static ExpConfInfo getExpConfInfo(int expId) {
		if (expId < 1) {
			return null;
		}
		
		Map<String,String> expData = HotDataSource.getHotData("exchange_expression_" + String.valueOf(expId));
    	ExpConfInfo expConfInfo = null;
    	//解析兑换表达式是否存在redis,无则从oracle查出
    	if (expData != null && expData.size() > 0) {
    		expConfInfo = new ExpConfInfo();
    		expConfInfo.setExpId(expId);
    		expConfInfo.setExpName(expData.get("expName"));
    		expConfInfo.setExpIn(expData.get("expIn"));
    		expConfInfo.setExpOut(expData.get("expOut"));
    		expConfInfo.setExpireTime(new Date(Long.valueOf(expData.get("expireTime"))));
    		expConfInfo.setLimitTime(Long.valueOf(expData.get("limitTime")));
    	} else {
    		//从oracle查出exp信息并放入redis
    		try{
    			expConfInfo = (ExpConfInfo) SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Exchange.getExpInfo",expId);
    		}catch(Exception e){
    			logger.error("fail to execute getExpInfo sql",e);
    		}
    		if (expConfInfo != null) {
    		    if (expConfInfo.getExpireTime().before(new Date())) {
    		        return null;
                }
    		    
    			if (expData == null) {
					expData = new HashMap<String, String>();
				}
    			expData.put("expId",String.valueOf(expConfInfo.getExpId()));
    			expData.put("expName",expConfInfo.getExpName());
    			expData.put("expIn",expConfInfo.getExpIn());
    			expData.put("expOut", expConfInfo.getExpOut());
    			expData.put("expireTime", String.valueOf(expConfInfo.getExpireTime().getTime()));
    			expData.put("limitTime", String.valueOf(expConfInfo.getLimitTime()));
    			int expireTime =  (int) ((expConfInfo.getExpireTime().getTime() - System.currentTimeMillis()) / 1000);
    			HotDataSource.setHotData("exchange_expression_" + String.valueOf(expId), expData, expireTime);
    		}
    		
    	}
    	return expConfInfo;	
	}
	
	/**
	 * 判断限量车辆是否还有库存()
	 * @return
	 */
	public static boolean isGiftRemain(ExpConfInfo expConfInfo) {
		JsonArray expOutArray = new JsonParser().parse(expConfInfo.getExpOut()).getAsJsonArray();
		List<TempExpData> outList = new Gson().fromJson(expOutArray, new TypeToken<List<TempExpData>>(){}.getType());
		for (TempExpData outExpData : outList) {
			//type表示兑换车辆
			if(outExpData.getType() != 3) {
				continue;
			}
			try {
				CarInfo carInfo = (CarInfo) SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Resource.getCarRemain", outExpData.getId());
				//若为0表示非限量
				if(carInfo.getTotal() == 0) {
					continue;
				}
				if (carInfo.getRemain() == 0) {
					return false;
				}
			} catch (Exception e) {
				logger.error("Fail to execute Resource.getCarRemain sql", e);
				return false;
			}
		}	
		return true;
	}
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
	 * 兑换
	 * @param userId
	 * @param expIn
	 * @param expOut
	 * @return true/false
	 */
	public static boolean activityExchange(int userId, ExpConfInfo expConf) {
		if (userId < 1 || expConf.getExpIn() == null || "".equals(expConf.getExpIn().trim()) || expConf.getExpOut() == null
				|| "".equals(expConf.getExpOut().trim()) || expConf.getExpName() == null || "".equals(expConf.getExpName().trim())) {
			return false;
		}

		boolean result = true;
		// 缓存兑换的礼物数据
		List<TempExpData> addTempExpDatas = new ArrayList<TempExpData>();
		List<TempExpData> reduceTempExpDatas = new ArrayList<TempExpData>();
		
		JsonArray expInArray = new JsonParser().parse(expConf.getExpIn()).getAsJsonArray();
		List<TempExpData> inList = new Gson().fromJson(expInArray, new TypeToken<List<TempExpData>>(){}.getType());
		if (inList == null || inList.size() == 0) {
			return false;
		}
		for (TempExpData tempExpData : inList) {
			// 扣库存
			if (!giftInOut(false, userId, expConf.getExpName(), tempExpData)) {
				result = false;
				break;
			}
			// 缓存兑换的礼物数据，如果后续发生兑换失败的异常则加回库存
			addTempExpDatas.add(tempExpData);
		}
		
		if (result) {
		    JsonArray expOutArray = new JsonParser().parse(expConf.getExpOut()).getAsJsonArray();
		    List<TempExpData> outList = new Gson().fromJson(expOutArray, new TypeToken<List<TempExpData>>(){}.getType());
		    if (inList == null || inList.size() == 0) {
		        return false;
		    }
		    for (TempExpData tempExpData : outList) {
		        // 加库存
		        if (!giftInOut(true, userId, expConf.getExpName(), tempExpData)) {
		            result = false;
		            break;
		        }
		        // 缓存兑换的礼物数据，如果后续发生兑换失败的异常则减回库存
		        reduceTempExpDatas.add(tempExpData);
		    }
		    if (result) {
                setExchangeCache(userId, expConf);
            }
        }
		// 判断执行成功与否，如果失败则读取缓存并恢复库存
		if (!result) {
			List<TempExpData> toAddData = new ArrayList<TempExpData>();
			List<TempExpData> toReduceData = new ArrayList<TempExpData>();
			if (addTempExpDatas.size() > 0) {
				for (TempExpData tempExpData : addTempExpDatas) {
					if (!giftInOut(true, userId, expConf.getExpName(), tempExpData)) {
						toAddData.add(tempExpData);
					}
				}
			}
			if (reduceTempExpDatas.size() > 0) {
				for (TempExpData tempExpData : reduceTempExpDatas) {
					if (!giftInOut(false, userId, expConf.getExpName(), tempExpData)) {
						toReduceData.add(tempExpData);
					}
				}
				// 剩余未恢复的库存通过输出日志，方便后续手动恢复
			}
			if (toAddData.size() > 0) {
				logger.error("ActivityExchangeService.activityExchange(" + userId + ", " + new Gson().toJson(expConf) + ")"
						+ " FailedToReturn: " + new Gson().toJson(toAddData));
			}
			if (toReduceData.size() > 0) {
				logger.error("ActivityExchangeService.activityExchange(" + userId + ", " + new Gson().toJson(expConf) + ")"
						+ " FailedToReturn: " + new Gson().toJson(toReduceData));
			}
		} else {
			try {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("userId", userId);
				map.put("expId", expConf.getExpId());
				SqlMapClientHelper.getInstance(DB.MASTER).insert("Exchange.insertHistExchange", map);
			} catch (SQLException e) {
				logger.error("Fail to execute sql", e);
			}
		}
		return result;
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

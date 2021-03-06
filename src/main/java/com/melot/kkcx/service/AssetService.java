package com.melot.kkcx.service;

import java.util.List;

import org.apache.log4j.Logger;

import com.melot.module.api.exceptions.MelotModuleException;
import com.melot.module.packagegift.driver.domain.Prop;
import com.melot.module.packagegift.driver.domain.PropPrice;
import com.melot.module.packagegift.driver.domain.ResVip;
import com.melot.module.packagegift.driver.service.CarService;
import com.melot.module.packagegift.driver.service.VipService;
import com.melot.sdk.core.util.MelotBeanFactory;
 
public class AssetService {
	
	private static Logger logger = Logger.getLogger(AssetService.class);
    
    /**
     * 获取VIP信息列表
     * @return
     */
    public static List<Prop> getVipList() {
    	VipService vipService = (VipService) MelotBeanFactory.getBean("vipService");
        try {
            return vipService.getPropList();
        } catch (Exception e) {
            logger.error("VipService.getVipList exception!", e);
        }
        
        return null;
    }
    
    /**
     * 获取VIP 价格列表 
     * @return
     */
    public static List<PropPrice> getVipPriceList(Integer propId) {
    	VipService vipService = (VipService) MelotBeanFactory.getBean("vipService");
        try {
            return vipService.getPropPriceList(propId);
        } catch (Exception e) {
            logger.error("VipService.getVipPriceList exception!", e);
        }
        
        return null;
    }
    
    /**
     * 赠送汽车
     * @param carId 汽车Id
     * @param userId 用户Id
     * @param days 赠送天数
     * @param type 赠送类型
     * @param sendDesc 赠送描述
     */
    public static void sendCarToUser(int carId, int userId, int days, int type, String sendDesc) {
		try {
			CarService carService = (CarService) MelotBeanFactory.getBean("vipService");
			carService.insertSendCar(userId, carId, days, type, sendDesc);
		} catch (MelotModuleException e) {
			switch (e.getErrCode()) {
			case 106:
//				logger.error("fail to send car, carId : " + carId + " ,userId : " + userId + " ,type : " + type + ", 售完（超过最大限制）");
				break;
				
			case 107:
//				logger.error("已经是永久汽车");
				break;
			
			case 108:
//				logger.error("赠送汽车异常");
				break;
				
			case 102:
//				logger.error("获取汽车相关信息异常");
				break;
				
			case 104:
//				logger.error("用户不存在");
				break;
				
			default:
				break;
			}
		} catch (Exception e) {
			logger.error("CarService.insertSendCar exception, userId : " + userId
                    + " ,carId : " + carId
                    + " ,days : " + days
                    + " ,type : " + type, e);
		}
    }
    
    /**
     * 购买道具（VIP）
     * @param userId 用户Id
     * @param propId 道具Id
     * @param periodOfValidity 购买时长（月，-1为终身vip） 
     * @param referrerId
     * @return
     */
    public static ResVip buyVip(int userId, int propId, int periodOfValidity, Integer referrerId, Integer friendId) {
    	try {
			VipService vipService = (VipService) MelotBeanFactory.getBean("vipService");
			if (friendId != null && friendId > 0) {
			    return vipService.insertBuyVipForNew(userId, periodOfValidity, propId, referrerId, friendId);
            } else {
                return vipService.insertBuyVip(userId, periodOfValidity, propId, referrerId);
            }
		} catch (Exception e) {
			logger.error("VipService.buyVip exception!", e);
		}
    	return null;
    }
    
    /**
     * 根据道具Id获取道具信息，道具按等级从高到低排序
     * @param propIds
     * @return
     */
    public static List<Prop> getVipListByPropIds(String propIds) {
        try {
        	VipService vipService = (VipService) MelotBeanFactory.getBean("vipService");
        	return vipService.getPropListByPropIds(propIds);
        } catch (Exception e) {
            logger.error("VipService.getVipListByPropIds exception, propIds : " + propIds, e);
        }
        
        return null;
    }
    
}
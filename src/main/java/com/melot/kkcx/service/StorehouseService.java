package com.melot.kkcx.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.melot.kktv.domain.StorehouseInfo;
import com.melot.module.driver.msgbuilder.StorehouseAgent;
import com.melot.server.protobuf.GiftwareProtos;

/**
 * 类说明：库存操作服务
 * <p>作者：宋建明<a href="mailto:jianming.song@melot.cn"></p>
 * <p>创建日期：2014-8-27</p>
 * <p>版本：V1.0</p>
 * <p>修改历史：</p>
 */
public class StorehouseService {

	private static Logger logger = Logger.getLogger(StorehouseService.class);

	/**
	 * 获取用户库存礼物中指定礼物的数量
	 * @param userId 用户ID
	 * @param giftId 礼物ID
	 * @return 数量
	 */
	public static List<StorehouseInfo> getUserGiftCount(int userId, String giftId) {
		if (userId < 1 ) {
			return null;
		}
		
		List<StorehouseInfo> storehouseInfoList = new ArrayList<StorehouseInfo>();
		try {
		    StorehouseAgent agent = new StorehouseAgent();
		    GiftwareProtos.GiftResult giftResult = agent.getGiftListInUserStore(userId);
		    if (giftResult != null && giftResult.getRet() == 0) {
		        List<GiftwareProtos.Gift> list = giftResult.getGiftList();
		        if (list != null && list.size() > 0) {
		        	//当giftId 是由多个礼物ID用逗号拼接起来的值时，返回多个礼物的库存信息，当giftId 为空时，返回用户的所有库存信息
			        if(giftId == null || "".equals(giftId.trim())) {
			        	for (GiftwareProtos.Gift gift : list) {
			        		storehouseInfoList.add(new StorehouseInfo(gift.getId(),gift.getName(),gift.getQuantity()));
			        	}
			        } else {
			        	if(giftId.indexOf(',') > 0) {
			        		String[] tempStr = giftId.split(",");
				        	Set<String> strSet = new HashSet<String>();
				        	CollectionUtils.addAll(strSet, tempStr);   
				        	for (GiftwareProtos.Gift gift : list) {
				        		if(strSet.contains(String.valueOf(gift.getId()))) {
				        			storehouseInfoList.add(new StorehouseInfo(gift.getId(),gift.getName(),gift.getQuantity()));
				        		}
				        	}
			        	} else {
			        		for (GiftwareProtos.Gift gift : list) {
				        		if(gift.getId() == Integer.valueOf(giftId)) {
				        			storehouseInfoList.add(new StorehouseInfo(gift.getId(),gift.getName(),gift.getQuantity()));
				        			break;
				        		}
				        	}
			        	}
			        	
			        }
			        
		        }
		    } else {
		        logger.error("StorehouseService.getUserGiftCount(" + userId + ", " + giftId + ") method call StorehouseAgent.getGiftListInUserStore(" + userId + ") Error: " + (giftResult == null ? "" : new Gson().toJson(giftResult).toString()));
		    }
        } catch (Exception e) {
            logger.error("StorehouseService.getUserGiftCount(" + userId + ", " + giftId + ") execute exception.", e);
        }
		
		return storehouseInfoList;
	}
	
	/**
	 * 增加用户库存
	 * @param userId 用户ID
	 * @param giftId 礼物ID
	 * @param count 增加的数量：当该值小于0时表明是减少用户礼物库存
	 * @param giftName 里面名称
	 * @param giftResource 变更理由代号
	 * @param giftDesc 变更描述
	 * @return true - 成功， false - 失败
	 */
	public static boolean addUserGift(int userId, int giftId, int count, String giftName, int giftResource, String giftDesc) {
		if (userId < 1 || giftId < 1 || count == 0 || giftResource < 1) {
			return false;
		}
		
		boolean retResult = false;
		
		if (giftName == null) {
            giftName = "";
        }
		
		try {
		    StorehouseAgent agent = new StorehouseAgent();
		    GiftwareProtos.GiftResult result = agent.addGiftToUserStore(userId, giftId, count, giftName, giftResource, giftDesc);
		    if (result != null && result.getRet() == 0) {
		        retResult = true;
		    } else {
		        logger.error("StorehouseService.addUserGift(" + userId + ", " + giftId + ", " + count + ", " + giftName + ", " + giftResource + ", " + giftDesc + ") method call StorehouseAgent.addGiftToUserStore(" + userId + ", " + giftId + ", " + count + ", " + giftName + ", " + giftResource + ", " + giftDesc + ") Error: " + (result == null ? "" : new Gson().toJson(result).toString()));
		    }
        } catch (Exception e) {
            logger.error("StorehouseService.addUserGift(" + userId + ", " + giftId + ", " + count + ", " + giftName + ", " + giftResource + ", " + giftDesc + ") execute exception.", e);
        }
		
		return retResult;
	}
}

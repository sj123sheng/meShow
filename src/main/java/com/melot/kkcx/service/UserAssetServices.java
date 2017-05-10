package com.melot.kkcx.service;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.asset.driver.service.AssetService;
import com.melot.kktv.redis.HotDataSource;
import com.melot.kktv.util.DateUtil;
import com.melot.kktv.util.StringUtil;
import com.melot.sdk.core.util.MelotBeanFactory;

public class UserAssetServices {
	
	private static String userValidIdCache = "UserAssetAction.userValidIdCache.";
	
	private static Logger logger = Logger.getLogger(UserAssetServices.class);
	
	/**
	 * 用户靓号转化为用户编号(10005047)
     * @param paramJsonObject
     * @return
     */
    public static Integer luckyIdToUserId(int virtualId) {
        if (virtualId < 1) {
            return null;
        }
        
        // 调用虚拟号模块
        try {
            AssetService assetService = MelotBeanFactory.getBean("assetService", AssetService.class);
            Integer userId = assetService.luckyIdToUserId(virtualId);
            
            if (userId == null || userId < 1) {
                return null;
            } else {
                return userId;
            }
        } catch (Exception e) {
            logger.error("fail to assetService.luckyIdToUserId, virtualId: " + virtualId, e);
        }
        
        return null;
    }
    
    /**
     * 用户靓号转化为用户编号
     * @param paramJsonObject
     * @return
     */
    public static Integer idToUserId(int id) {
        if (id < 1) {
            return null;
        }
        
        // 调用虚拟号模块
        try {
            AssetService assetService = MelotBeanFactory.getBean("assetService", AssetService.class);
            Integer userId = assetService.luckyIdToUserId(id);
            if (userId == null || userId < 1) {
                return id;
            } else {
                return userId;
            }
        } catch (Exception e) {
            logger.error("fail to assetService.luckyIdToUserId, id: " + id, e);
        }
        
        return id;
    }
	
	/**
	 * 获取激活的虚拟账号
	 * @param userId
	 * @return
	 */
	public static JsonObject getValidVirtualId(Integer userId) {
		JsonObject jObject = null;
		if (userId == null || userId < 1) {
            return jObject;
        }
        
        String cacheResult = HotDataSource.getTempDataString(userValidIdCache + userId);
        if (!StringUtil.strIsNull(cacheResult)) {
            if ("null".equals(cacheResult.trim())) {
                return null;
            }
            
            return new JsonParser().parse(cacheResult).getAsJsonObject();
        }
		
        boolean hasValidVirtualId = false;
        com.melot.asset.driver.domain.ResVirtualIdInfo resVirtualIdInfo = null;
        try {
            AssetService assetService = MelotBeanFactory.getBean("assetService", AssetService.class);
            resVirtualIdInfo = assetService.getValiVirtualId(userId);
        } catch (Exception e) {
            logger.error("fail to assetService.getValiVirtualId, userId: " + userId, e);
        }
        
		if (resVirtualIdInfo != null) {
		    jObject = new JsonObject();
		    jObject.addProperty("id", resVirtualIdInfo.getVirtualId());
		    if (resVirtualIdInfo.getIdType() == 2 || resVirtualIdInfo.getIdType() > 4) {
		        jObject.addProperty("idType", 2);
            } else {
                jObject.addProperty("idType", 1);
            }
		    jObject.addProperty("isLight", resVirtualIdInfo.getIsLight());
		    // 尊号靓号在亮的状态下都是有效的
		    if (resVirtualIdInfo.getIsLight() != null && resVirtualIdInfo.getIsLight() == 1) {
		        hasValidVirtualId = true;
		    }

            jObject.addProperty("newIdType", resVirtualIdInfo.getIdType());
		    jObject.addProperty("endTime", resVirtualIdInfo.getEndTime());
		    jObject.addProperty("idState", resVirtualIdInfo.getIdState());
		    jObject.addProperty("isEnable", resVirtualIdInfo.getIsEnable());
		    jObject.addProperty("remainDays", resVirtualIdInfo.getRemainDays());
		    
		    // 返回靓号背景icon
		    jObject.addProperty("backIcon", resVirtualIdInfo.getBackIcon());
		    
		    // 靓号类型，1-黑色靓号，2-紫色靓号，3-红色靓号，4-橙色靓号
		    jObject.addProperty("iconType", resVirtualIdInfo.getIconType());
		    
        }
		
		if (hasValidVirtualId) {
		    HotDataSource.setTempDataString(userValidIdCache + userId, jObject.toString(), (int) (DateUtil.getDayBeginTime(System.currentTimeMillis()) / 1000 + 24 * 3600 - System.currentTimeMillis() / 1000));
		} else {
		    HotDataSource.setTempDataString(userValidIdCache + userId, "null", 24 * 3600);
		    jObject = null;
		}
		return jObject;
	}
	
}

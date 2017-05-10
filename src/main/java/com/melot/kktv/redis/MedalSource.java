package com.melot.kktv.redis;

import java.util.List;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.kktv.model.MedalInfo;
import com.melot.kktv.model.MedalPrice;
import com.melot.kktv.util.confdynamic.MedalConfig;
import com.melot.kktv.util.confdynamic.MedalPriceConfig;
import com.melot.module.medal.driver.domain.UserFamilyMedal;
import com.melot.module.medal.driver.service.FamilyMedalService;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * 类说明：
 * <p>作者：宋建明<a href="mailto:jianming.song@melot.cn"></p>
 * <p>创建日期：2014-5-27</p>
 * <p>版本：V1.0</p>
 * <p>修改历史：</p>
 */
public class MedalSource {

    private static Logger logger = Logger.getLogger(MedalSource.class);
    
	/**
	 * 添加用户勋章
	 * @param userId 用户ID
	 * @param medalId 勋章ID
	 * @param days 赠送天数，-1永久
	 * @return true-添加成功，false-添加失败
	 */
	public static boolean addUserMedal(int userId, int medalId, int days) {
	    try {
            FamilyMedalService familyMedalService = (FamilyMedalService) MelotBeanFactory.getBean("familyMedalService");
            return familyMedalService.sendFamilyMedal(userId + "", medalId, days);
        } catch (Exception e) {
            logger.error("fail to send user family medal, userId: " + userId + ", medalId: " + medalId + ", days: " + days, e);
        }
	    
		return true;
	}
	
	/**
	 * 删除用户勋章
	 * @param userId 用户ID
	 * @param medalId 勋章ID
	 * @return true-删除成功，false-删除失败
	 */
	public static boolean delUserMedal(int userId, int medalId) {
	    try {
            FamilyMedalService familyMedalService = (FamilyMedalService) MelotBeanFactory.getBean("familyMedalService");
            return familyMedalService.deleteUserFamilyMedal(userId, medalId);
        } catch (Exception e) {
            logger.error("fail to delete user family medal, userId: " + userId + ", medalId: " + medalId, e);
        }
		
		return true;
	}
	
	/**
     * 获取用户家族勋章
     * @param userId 用户ID
     * @param platform 平台标识，用于筛选不同平台的图标
     * @return
     */
    public static JsonArray getUserMedalsAsJson(int userId, Integer platform) {
        JsonArray jsonArray = new JsonArray();
        JsonObject jsonObject = new JsonObject();
        try {
            FamilyMedalService familyMedalService = (FamilyMedalService) MelotBeanFactory.getBean("familyMedalService");
            UserFamilyMedal userFamilyMedal = familyMedalService.getUserFamilyMedal(userId);
            if (userFamilyMedal != null) {
                jsonObject.addProperty("medalId", userFamilyMedal.getMedalId());
                jsonObject.addProperty("medalTitle", userFamilyMedal.getMedalTitle());
                jsonObject.addProperty("medalType", userFamilyMedal.getMedalType());
                jsonObject.addProperty("familyId", userFamilyMedal.getFamilyId());
                jsonObject.addProperty("leftTime", userFamilyMedal.getEndTime() - System.currentTimeMillis());
                jsonArray.add(jsonObject);
            }
        } catch (Exception e) {
            logger.error("fail to get user family medal, userId: " + userId);
        }
        
        return jsonArray;
    }
	
	/**
	 * 获取过滤后的勋章信息
	 * @param medal 勋章对象
	 * @param platform 平台标识，用于筛选不同平台的图标
	 * @return
	 */
	private static MedalInfo getMedalInfo(MedalInfo medalInfo, Integer platform) {
		return medalInfo;
	}
	
	/**
	 * 根据平台号获取家族勋章  
	 * @param medalId 勋章ID
	 * @param platform 平台号
	 * @return
	 */
	public static MedalInfo getFamilyMedal(int medalId, int platform) {
		MedalInfo medalInfo = MedalConfig.getMedal(medalId);	
		medalInfo.setMedalPrice(MedalPriceConfig.getMedalPriceListByType(medalInfo.getMedalType()));
		return medalInfo;
	}
	
	/**
	 * 获取勋章信息的 json 对象
	 * @param medalId 勋章ID
	 * @param platform 平台标识，用于筛选不同平台的图标
	 * @return
	 */
	public static JsonObject getMedalInfoAsJson(int medalId, Integer platform) {
		return getMedalInfoAsJson(MedalConfig.getMedal(medalId), platform);
	}
	
	/**
	 * 获取勋章信息的 json 对象
	 * @param medal 勋章对象
	 * @param platform 平台标识，用于筛选不同平台的图标
	 * @return
	 */
	public static JsonObject getMedalInfoAsJson(MedalInfo medalInfo, Integer platform) {
		if (medalInfo == null) {
			return null;
		}
		JsonObject jsonObject = new JsonObject();
		
		medalInfo = getMedalInfo(medalInfo, platform);
		if(medalInfo.getMedalType() != 2) {
			jsonObject.addProperty("medalId", medalInfo.getMedalId());
			jsonObject.addProperty("medalTitle", medalInfo.getMedalTitle());
			jsonObject.addProperty("medalType", medalInfo.getMedalType());
			List<MedalPrice> medalPrice = MedalPriceConfig.getMedalPriceListByType(medalInfo.getMedalType());
			jsonObject.add("medalPrice", medalPrice != null ? new Gson().toJsonTree(medalPrice).getAsJsonArray() : null);
			jsonObject.addProperty("familyId", medalInfo.getMedalRefId());
			return jsonObject;
		}else {
			return null ;
		}
		
	}
}

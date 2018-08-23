package com.melot.kktv.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcx.service.AssetService;
import com.melot.kkcx.service.FamilyService;
import com.melot.kktv.model.Family;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.ConstantEnum;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.module.packagegift.driver.domain.Prop;
import com.melot.module.packagegift.driver.domain.PropPrice;
import com.melot.module.packagegift.driver.domain.UserTicketInfo;
import com.melot.module.packagegift.driver.service.TicketService;
import com.melot.module.packagegift.util.GiftPackageEnum;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * 系统资源相关的接口类
 * 
 * @author LY
 * 
 */
public class ResourceFunctions {

	/** 日志记录对象 */
	private static Logger logger = Logger.getLogger(ResourceFunctions.class);

	/**
	 * 获取道具资源列表
	 * Sql Resource.getPropList（可删除）
	 * @param jsonObject 请求对象
	 * @return 结果字符串
	 */
	public JsonObject getPropList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		// 调用存储过程得到结果
		JsonObject result = new JsonObject();
		List<Prop> pList = AssetService.getVipList();
		if (pList != null && pList.size() > 0) {
		    JsonArray jPropList = new JsonArray();
		    for (Prop prop : pList) {
		        int propId = prop.getPropId();
		        List<PropPrice> priceList = AssetService.getVipPriceList(propId);
		        JsonObject record = new JsonObject();
                record.addProperty("propId", propId);
                record.addProperty("propName", prop.getPropName());
                record.addProperty("propPrivilege", prop.getPropPrivilege());
                record.addProperty("buyCondition", prop.getBuyCondition());
                if (priceList != null && priceList.size() > 0) {
                    record.addProperty("propPriceList", new Gson().toJson(priceList));
                }
                if (!StringUtil.strIsNull(prop.getPropFileName())) {
                    record.addProperty("propFileName", ConfigHelper.getVipWebResURL() + prop.getPropFileName());
                }
                
                jPropList.add(record);
		    }
		    result.add("propList", jPropList);
		    result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		} else {
		    result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
		}
		
        // 返回结果
        return result;
	}

	/**
	 * 获取道具价格列表
	 * sql Resource.getPropPriceList 可删 
	 * @param jsonObject 请求对象
	 * @return 结果字符串
	 */
	public JsonObject getPropPriceList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		// 获取参数
		JsonElement propIdje = jsonObject.get("propId");
		JsonObject result = new JsonObject();
		// 验证参数
		int propId;
		if (propIdje != null && !propIdje.isJsonNull() && !propIdje.getAsString().equals("")) {
			// 验证数字
			try {
				propId = propIdje.getAsInt();
			} catch (Exception e) {
				result.addProperty("TagCode", "09020002");
				return result;
			}
		} else {
			result.addProperty("TagCode", "09020001");
			return result;
		}
		
		List<PropPrice> pList = AssetService.getVipPriceList(propId);
		if (pList != null && pList.size() > 0) {
		    JsonArray jPropList = new JsonArray();
		    for (PropPrice pp : pList) {
		        jPropList.add(new Gson().toJsonTree(pp));
		    }
		    result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            result.add("propPriceList", jPropList);
		} else {
		    result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
		}
		
		// 返回结果
        return result;
	}

	/**
	 * 获取年度盛典结果信息接口（10009011）
	 * @param paramJsonObject
	 * @return
	 */
    public JsonObject getAnnualInfo(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
	    JsonObject result = new JsonObject();
	    int platform;
        try {
            platform = CommonUtil.getJsonParamInt(paramJsonObject, "platform", PlatformEnum.WEB, null, PlatformEnum.WEB, PlatformEnum.IPAD);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        Gson gson = new Gson();
        
        String annualActor = ConfigHelper.getAnnualActor();
        if (!StringUtil.strIsNull(annualActor)) {
            Map<String, Long> actorMap = gson.fromJson(annualActor, new TypeToken<Map<String, Long>>(){}.getType());
            if (actorMap != null) {
                JsonArray actorArray = new JsonArray();
                for (String actorId : actorMap.keySet()) {
                    UserProfile actorInfo = com.melot.kktv.service.UserService.getUserInfoV2(Integer.parseInt(actorId));
                    
                    JsonObject actorObject = new JsonObject();
                    actorObject.addProperty("actorId", Integer.valueOf(actorId));
                    actorObject.addProperty("actorName", actorInfo.getNickName());
                    actorObject.addProperty("annualScore", actorMap.get(actorId));
                    
                    // 区分平台返回
                    switch (platform) {
                    case PlatformEnum.WEB:
                        if (actorInfo.getPortrait() != null)
                            actorObject.addProperty("portraitPath_256", actorInfo.getPortrait() + ConstantEnum.portrait_path_256_suffix);
                        break;
                    case PlatformEnum.ANDROID:
                    case PlatformEnum.IPHONE:
                    case PlatformEnum.IPAD:
                        if (actorInfo.getPortrait() != null)
                            actorObject.addProperty("portraitPath_128", actorInfo.getPortrait() + ConstantEnum.portrait_path_128_suffix);
                        break;
                    default:
                        break;
                    }
                    
                    actorArray.add(actorObject);
                }
                result.add("annualActor", actorArray);
            }
        }
        
        String annualFamily = ConfigHelper.getAnnualFamily();
        if (!StringUtil.strIsNull(annualFamily)) {
            Map<String, Long> familyMap = gson.fromJson(annualFamily, new TypeToken<Map<String, Long>>(){}.getType());
            if (familyMap != null) {
                List<Integer> familyIdList = new ArrayList<Integer>();
                for (String familyId : familyMap.keySet()) {
                    familyIdList.add(Integer.valueOf(familyId));
                }
                
                List<Family> families = FamilyService.getFamilyListByIds(familyIdList, platform);
                if (families != null) {
                    JsonArray familyArray = new JsonArray();
                    for (Family family : families) {
                        JsonObject familyObject = new JsonObject();
                        familyObject.addProperty("familyId", family.getFamilyId());
                        familyObject.addProperty("familyName", family.getFamilyName());
                        familyObject.addProperty("annualScore", familyMap.get(String.valueOf(family.getFamilyId())));
                        
                        familyObject.add("familyPoster", family.getFamilyPoster());
                        
                        familyArray.add(familyObject);
                    }
                    result.add("annualFamily", familyArray);
                }
            }
        }
        
        result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        
	    return result;
	}

	/**
	 * 获取用户剩余券数量(10006060)
	 * @param jsonObject
	 * @param token
	 * @return
	 */
	public JsonObject getUserRemainTickets(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
	    // 该接口需要验证token,未验证的返回错误码
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId = 0;
        String ticketIds = null;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            ticketIds = CommonUtil.getJsonParamString(jsonObject, "ticketIds", null, "60600001", 0, 64);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        TicketService ticketService = (TicketService) MelotBeanFactory.getBean("ticketService");
        List<UserTicketInfo> userRemainTicketList = ticketService.getUserRemainTickets(userId, ticketIds);
        
        JsonArray list = new JsonArray();
        if (userRemainTicketList != null && userRemainTicketList.size() > 0) {
			for (UserTicketInfo userRemainTicket : userRemainTicketList) {
				JsonObject jObject = new JsonObject();
				jObject.addProperty("utId", userRemainTicket.getUtId());
				jObject.addProperty("ticketId", userRemainTicket.getTicketId());
				jObject.addProperty("remainCount", userRemainTicket.getRemainCount());
				list.add(jObject);
			}
		}
        result.add("list", list);
        result.addProperty("userId", userId);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        
        return result;
	}

	/**
	 * 用户点歌扣券(10006061)
	 * @param jsonObject
	 * @param checkTag
	 * @return
	 */
	public JsonObject selectSong(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		// 该接口需要验证token,未验证的返回错误码
		JsonObject result = new JsonObject();
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}

		int userId, ticketId;
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
			ticketId = CommonUtil.getJsonParamInt(jsonObject, "ticketId", 0, "60610002", 1, Integer.MAX_VALUE);
		} catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch (Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}

		try {
		    TicketService ticketService = (TicketService) MelotBeanFactory.getBean("ticketService");

	        //扣除用户的券ID对应的数量1
	        boolean flag = ticketService.insertUseTicket(userId, ticketId, GiftPackageEnum.TICKET_USE, 1, "用户" + userId + "使用点歌券" + ticketId);
	        if (!flag) {
	            //扣券操作失败
	            result.addProperty("TagCode", "60610003");
	            return result;
	        }
		} catch (Exception e) {
		    logger.error("ticketService.insertUseTicket execute exception, userId: " + userId + ", tickerId: " + ticketId, e);
		    result.addProperty("TagCode", "60610003");
            return result;
		}

		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
	}

}

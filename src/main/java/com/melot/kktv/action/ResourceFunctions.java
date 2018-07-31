package com.melot.kktv.action;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcx.service.AssetService;
import com.melot.kkcx.service.FamilyService;
import com.melot.kkcx.service.UserService;
import com.melot.kktv.domain.CarInfo;
import com.melot.kktv.model.Car;
import com.melot.kktv.model.Family;
import com.melot.kktv.model.Gift;
import com.melot.kktv.redis.HotDataSource;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.Constant;
import com.melot.kktv.util.ConstantEnum;
import com.melot.kktv.util.DateUtil;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;
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
	 * 商城车市获取车辆列表
	 * 
	 * @param paramJsonObject
	 * @return 车辆列表
	 */
	public JsonObject getCarList(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
		// 获取参数
		JsonElement pageIndexje = paramJsonObject.get("pageIndex");
		JsonElement numPerPageje = paramJsonObject.get("numPerPage");
		JsonElement orderByje = paramJsonObject.get("orderBy");
		JsonElement sortModeje = paramJsonObject.get("sortMode");
		JsonElement categoryje = paramJsonObject.get("category");
		JsonElement platformje = paramJsonObject.get("platform");

		int pageIndex = 1; // 页码
		int numPerPage = 16; // 每页显示数量
		int orderBy = 0; // 按哪列排序
		String sortMode = "asc"; // 升序/降序
		int category = -1; // 分类
		int platform = PlatformEnum.WEB;
		// 验证参数
		if (pageIndexje != null) {
			try {
				pageIndex = pageIndexje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", 05110001);
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", 05110001);
			return result;
		}

		if (numPerPageje != null) {
			try {
				numPerPage = numPerPageje.getAsInt();
				if (numPerPage < 0) {
					JsonObject result = new JsonObject();
					result.addProperty("TagCode", 05110002);
					return result;
				}
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", 05110002);
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", 05110002);
			return result;
		}

		if (orderByje != null) {
			try {
				orderBy = orderByje.getAsInt();
			} catch (Exception e) {
				orderBy = 0;
			}
		}

		if (categoryje != null) {
			try {
				category = categoryje.getAsInt();
			} catch (Exception e) {
				category = -1;
			}
		}

		if (sortModeje != null) {
			sortMode = sortModeje.getAsString();
			if (!sortMode.equals("asc") && !sortMode.equals("desc")) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", 05110003);
				return result;
			}
		}

		if (platformje != null) {
			try {
				platform = platformje.getAsInt();
			} catch (Exception e) {
				platform = PlatformEnum.WEB;
			}
		}

		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("pageIndex", pageIndex);
		map.put("numPerPage", numPerPage);
		map.put("orderBy", orderBy);
		map.put("sortMode", sortMode);
		map.put("category", category);

		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Profile.getCarList", map);
		} catch (Exception e) {
			logger.error("未能正常调用存储过程", e);
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			return result;
		}

		String tagCode = map.get("TagCode").toString();
		String pageTotal = map.get("PageTotal").toString();
		// 返回结果
		JsonObject result = new JsonObject();
		if (tagCode.equals(TagCodeEnum.SUCCESS)) {
			result.addProperty("TagCode", tagCode);
			result.addProperty("pageTotal", pageTotal);

			@SuppressWarnings("unchecked")
			List<Car> list = (List<Car>) map.get("CarList");

			JsonArray ja = new JsonArray();
			for (Car car : list) {
				if (platform == PlatformEnum.ANDROID) {
					car.setPhoto(ConfigHelper.getParkCarAndroidResURL() + car.getPhoto());
					car.setIcon(ConfigHelper.getParkLogoAndroidResURL() + car.getIcon());
				} else if (platform == PlatformEnum.IPHONE) {
					car.setPhoto(ConfigHelper.getParkCarResURL() + car.getPhoto());
					car.setIcon(ConfigHelper.getParkLogoResURL() + car.getIcon());
				} else if (platform == PlatformEnum.IPAD) {
					car.setPhoto(ConfigHelper.getParkCarResURL() + car.getPhoto());
					car.setIcon(ConfigHelper.getParkLogoResURL() + car.getIcon());
				}
				ja.add(car.toJsonObjectForCarList());
			}
			result.add("CarList", ja);
		} else if (tagCode.equals("02")) {
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			result.addProperty("pageTotal", pageTotal);
			result.add("CarList", new JsonArray());
		} else if (tagCode.equals("03")) {
			result.addProperty("TagCode", "05110004");
		} else {
			logger.error("调用存储过程(Profile.getCarList)未的到正常结果,TagCode:" + tagCode + ",jsonObject:" + paramJsonObject.toString());
			result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
		}

		return result;
	}
	
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

		int roomId, userId, songId, ticketId, xmanid = 0;
		try {
			roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, TagCodeEnum.ROOMID_MISSING, 1, Integer.MAX_VALUE);
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
			songId = CommonUtil.getJsonParamInt(jsonObject, "songId", 0, "60610001", 1, Integer.MAX_VALUE);
			ticketId = CommonUtil.getJsonParamInt(jsonObject, "ticketId", 0, "60610002", 1, Integer.MAX_VALUE);
			xmanid = CommonUtil.getJsonParamInt(jsonObject, "xmanid", 0, null, 1, Integer.MAX_VALUE);
		} catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch (Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}

		TicketService ticketService = (TicketService) MelotBeanFactory.getBean("ticketService");

		//扣除用户的券ID对应的数量1
		boolean flag = ticketService.insertUseTicket(userId, ticketId, GiftPackageEnum.TICKET_USE, 1, "用户" + userId + "使用点歌券" + ticketId);
		if (!flag) {
			//扣券操作失败
			result.addProperty("TagCode", "60610003");
			return result;
		}

		//调用存储过程来完成插表记录
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("roomId", roomId);
		map.put("userId", userId);
		map.put("songId", songId);
		map.put("ticketId", ticketId);
		if (xmanid != 0) {
			map.put("xmanid", xmanid);
		}
		map.put("dtime", new Date());
		boolean ret = false;
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Other.selectSong", map);
			String tagCode = (String) map.get("TagCode");
			if ("01".equals(tagCode)) {
				//点歌存储过程失败，将扣除的券加1
				ret = ticketService.insertSendTicket(userId, ticketId, GiftPackageEnum.TICKET_SEND, 1, "用户" + userId + "点歌操作失败退回已扣除点歌券", 0);
				if (!ret) {
					logger.error("用户:" + userId + "，点歌失败，券" + ticketId + "，已扣除1张，退回失败");
				}
				// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
				logger.error("调用存储过程(Other.selectSong)未的到正常结果,TagCode:" + tagCode + ",jsonObject:"
						+ jsonObject.toString());
				result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
			} else {
				//点歌存储过程成功
				result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			}
		} catch (SQLException e) {
			//点歌存储过程执行异常，将扣除的券加1
			ret = ticketService.insertSendTicket(userId, ticketId, GiftPackageEnum.TICKET_SEND, 1, "用户" + userId + "点歌操作异常退回已扣除点歌券", 0);
			if (!ret) {
				logger.error("用户:" + userId + "，点歌异常，券" + ticketId + "，已扣除1张，退回失败");
			}
			logger.error("未能正常调用存储过程", e);
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
		}

		return result;
	}


}

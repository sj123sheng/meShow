package com.melot.kktv.action;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.melot.api.menu.sdk.utils.Collectionutils;
import com.melot.asset.driver.domain.ConfVirtualId;
import com.melot.asset.driver.domain.PageConfVirtualId;
import com.melot.asset.driver.domain.ReletVirtualIdConfig;
import com.melot.asset.driver.domain.ResVirtualIdInfo;
import com.melot.asset.driver.service.AssetService;
import com.melot.asset.driver.service.VirtualIdService;
import com.melot.kk.hall.api.domain.HallRoomInfoDTO;
import com.melot.kk.hall.api.service.DefaultPartService;
import com.melot.kkcore.user.api.ResourceType;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcore.user.api.UserResource;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kkcx.model.UserProp;
import com.melot.kkcx.service.ProfileServices;
import com.melot.kkcx.service.StorehouseService;
import com.melot.kkcx.service.UserAssetServices;
import com.melot.kkcx.util.PropTypeEnum;
import com.melot.kktv.domain.CarConfigInfo;
import com.melot.kktv.domain.CarPriceInfo;
import com.melot.kktv.domain.StorehouseInfo;
import com.melot.kktv.model.MedalInfo;
import com.melot.kktv.redis.HotDataSource;
import com.melot.kktv.service.UserService;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.kktv.util.confdynamic.CarConfig;
import com.melot.kktv.util.confdynamic.MedalConfig;
import com.melot.module.api.exceptions.MelotModuleException;
import com.melot.module.medal.driver.domain.UserActivityMedal;
import com.melot.module.medal.driver.domain.UserWearMedalListModel;
import com.melot.module.medal.driver.service.ActivityMedalService;
import com.melot.module.medal.driver.service.UserMedalService;
import com.melot.module.packagegift.driver.domain.CarPrice;
import com.melot.module.packagegift.driver.domain.Prop;
import com.melot.module.packagegift.driver.domain.UserCarInfo;
import com.melot.module.packagegift.driver.domain.UserChatBubbleDTO;
import com.melot.module.packagegift.driver.domain.UserVip;
import com.melot.module.packagegift.driver.service.CarService;
import com.melot.module.packagegift.driver.service.ChatBubbleService;
import com.melot.module.packagegift.driver.service.VipService;
import com.melot.room.pendant.domain.ReturnResult;
import com.melot.room.pendant.dto.UserPendantDTO;
import com.melot.room.pendant.service.PendantService;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * 个人财产(道具汽车靓号)相关接口
 * @author RC
 *
 */
public class UserAssetAction {

	/** 日志记录对象 */
	private static Logger logger = Logger.getLogger(UserAssetAction.class);

	private static String luckyIdKey = "UserAssetAction.getVirtualIdlist.luckyIdList";

	private static String userValidIdCache = "UserAssetAction.userValidIdCache.";
	
	@Resource
	ChatBubbleService chatBubbleService;
	
	@Resource
	DefaultPartService defaultPartService;

	/**
	 * 获取用户所拥有的财产列表(VIP 车 门票)(10005019)
	 *
	 * @param paramJsonObject
	 * @return 用户财产列表
	 */
	public JsonObject getUserAssetList(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
		JsonObject result = new JsonObject();
		
        Integer userId;

        try {
            userId = CommonUtil.getJsonParamInt(paramJsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        List<UserProp> userPropList = new ArrayList<>();
        try {
            PendantService pendantService = (PendantService) MelotBeanFactory.getBean("pendantService");
            ReturnResult<List<UserPendantDTO>> pendantDTOResult = pendantService.listByUserId(userId);

            if ("0".equals(pendantDTOResult.getCode())) {
                List<UserPendantDTO> userPendantDTOList = pendantDTOResult.getData();
                for (UserPendantDTO userPendantDTO : userPendantDTOList) {
                    UserProp userProp = new UserProp();
                    userProp.setId(userPendantDTO.getPendantId());
                    userProp.setDesc(userPendantDTO.getPendantDescribe());
                    userProp.setIsLight(userPendantDTO.getUsed() ? 1 : 0);
                    userProp.setAppLargeUrl(userPendantDTO.getPendantBigUrl());
                    userProp.setWebLargeUrl(userPendantDTO.getPendantBigUrl());
                    userProp.setSmallUrl(userPendantDTO.getPendantSmallUrl());
                    userProp.setValidType(userPendantDTO.getValidType());
                    if (userPendantDTO.getValidTime() != null) {
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(userPendantDTO.getValidTime().getTime());
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                        userProp.setLeftTime(calendar.getTimeInMillis() - System.currentTimeMillis());
                    }
                    userProp.setLevel(userPendantDTO.getLevel());
                    userProp.setName(userPendantDTO.getPendantName());
                    userProp.setType(PropTypeEnum.PENDANT.getCode());
                    userPropList.add(userProp);
                }
            }
        } catch (Exception e) {
            logger.error("pendantService.listByUserId(" + userId + ") return exception.", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        
        try {
            //获取用户聊天气泡
            List<UserChatBubbleDTO> userChatBubbleDTOList = chatBubbleService.getUserChatBubbleList(userId, null);
            if (!Collectionutils.isEmpty(userChatBubbleDTOList)) {
                for (UserChatBubbleDTO userChatBubbleDTO : userChatBubbleDTOList) {
                    userPropList.add(ProfileServices.switchBubbleToUserProp(userChatBubbleDTO));
                }
            }
        } catch (Exception e) {
            logger.error("NodeFunctions.getUserInfoForNode execute pendantService.getUserPendant(" + userId + ") exception.", e);
        }

        result.add("userPropList", new JsonParser().parse(new Gson().toJson(userPropList)).getAsJsonArray());
        
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		result.add("props", getUserPropList(paramJsonObject, checkTag, request));
		result.add("cars", getUserCarList(paramJsonObject, checkTag, request));
        result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
        return result;
    }

	/**
     * 获取用户道具列表 (10005009)
	 *
	 * @param jsonObject 请求对象
	 * @return json对象形式的返回结果
	 */
	public JsonObject getUserPropList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		JsonObject result = new JsonObject();

		int userId, platform;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 1, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        List<Prop> propList = null;
        Prop prop = null;
        JsonArray jUserPropList = new JsonArray();
        VipService vipService = (VipService) MelotBeanFactory.getBean("vipService");
        List<UserVip> vipList = vipService.getUserVip(userId);
        if (vipList != null && vipList.size() > 0) {
        	for (UserVip vip : vipList) {
        		JsonObject obj = new JsonObject();
        		propList = vipService.getPropListByPropIds(String.valueOf(vip.getPropId()));
        		if (propList != null && propList.size() > 0) {
        			prop = propList.get(0);
        			if (prop.getPropId() != null) {
        				obj.addProperty("propId", prop.getPropId());
        			}
        			if (prop.getPropFileName() != null) {
        				if (platform == PlatformEnum.ANDROID) {
        					prop.setPropFileName(ConfigHelper.getVipAndroidResURL() + prop.getPropFileName());
        				} else if (platform == PlatformEnum.IPHONE) {
        					prop.setPropFileName(ConfigHelper.getVipResURL() + prop.getPropFileName());
        				} else if (platform == PlatformEnum.IPAD) {
        					prop.setPropFileName(ConfigHelper.getVipResURL() + prop.getPropFileName());
        				} else if (platform == PlatformEnum.WEB) {
        					prop.setPropFileName(ConfigHelper.getVipWebResURL() + prop.getPropFileName());
        				}
        				obj.addProperty("propFileName", prop.getPropFileName());
    				}
        			if (prop.getPropName() != null) {
        				obj.addProperty("propName", prop.getPropName());
        			}
        			if (prop.getPropPrivilege() != null) {
        				obj.addProperty("propPrivilege", prop.getPropPrivilege());
        			}
        			if (vip.getLeftTime() == -1) {
        				obj.addProperty("leftTime", -1);
        				jUserPropList.add(obj);
        			} else {
        				long leftTime = 0;
        				if (vip.getLeftTime() - new Date().getTime() > 0) {
        					leftTime = vip.getLeftTime() - new Date().getTime();
        					obj.addProperty("leftTime", leftTime);
        					jUserPropList.add(obj);
        				}
        			}
        		}
        	}
        }
        result.add("userPropList", jUserPropList);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
	}

	/**
	 * 获取用户所拥有的车辆列表(10005013)
	 *
	 * @param paramJsonObject
	 * @return 用户的车辆列表
	 */
	public JsonObject getUserCarList(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
	    JsonObject result = new JsonObject();
		// 获取参数
		int userId, platform;
		try {
            userId = CommonUtil.getJsonParamInt(paramJsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(paramJsonObject, "platform", PlatformEnum.WEB, null, 1, Integer.MAX_VALUE);
		} catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

		CarService carService = (CarService) MelotBeanFactory.getBean("carService");
		// 从redis中获取用户车辆列表
		JsonArray jsonArray = new JsonArray();
		List<UserCarInfo> userCarList = carService.getUserCar(userId);
		if(userCarList != null && userCarList.size() > 0) {
			for (UserCarInfo userCarMap : userCarList) {
			    JsonObject jsonObject = new JsonObject();
			    CarConfigInfo carInfo = CarConfig.getUserCar(userCarMap.getId().intValue());
			    if (carInfo != null && carInfo.getGetCondition() != null && carInfo.getGetCondition() == 1) {
			        jsonObject.addProperty("canRenew", false);
                } else {
                    jsonObject.addProperty("canRenew", true);
                }
			    jsonObject.addProperty("carId", userCarMap.getId());
			    jsonObject.addProperty("ucId", userCarMap.getUcId());
			    jsonObject.addProperty("name", userCarMap.getName());
			    jsonObject.addProperty("leftTime", userCarMap.getLeftTime());
			    jsonObject.addProperty("origPrice", userCarMap.getPrice());
			    if (carInfo != null) {
			        jsonObject.addProperty("nowPrice", carInfo.getMonthPrice() != null ? carInfo.getMonthPrice() : 0);
			    }
				if (platform == PlatformEnum.ANDROID) {
				    jsonObject.addProperty("icon", ConfigHelper.getParkLogoAndroidResURL() + userCarMap.getIcon());
				} else if (platform == PlatformEnum.IPHONE) {
				    jsonObject.addProperty("icon", ConfigHelper.getParkLogoResURL() + userCarMap.getIcon());
				} else if (platform == PlatformEnum.IPAD) {
				    jsonObject.addProperty("icon", ConfigHelper.getParkLogoResURL() + userCarMap.getIcon());
				} else {
				    jsonObject.addProperty("icon", userCarMap.getIcon());
				}
				if (platform == PlatformEnum.ANDROID) {
				    jsonObject.addProperty("photo", ConfigHelper.getParkCarAndroidResURL() + userCarMap.getPhoto());
				} else if (platform == PlatformEnum.IPHONE) {
				    jsonObject.addProperty("photo", ConfigHelper.getParkCarResURL() + userCarMap.getPhoto());
				} else if (platform == PlatformEnum.IPAD) {
				    jsonObject.addProperty("photo", ConfigHelper.getParkCarResURL() + userCarMap.getPhoto());
				} else {
				    jsonObject.addProperty("photo", userCarMap.getPhoto());
				}
				jsonArray.add(jsonObject);
			}

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

			// 从redis中获取进场车辆
			String ucId = carService.getEnterRoomCar(userId);
            result.addProperty("ucId", StringUtil.parseFromStr(ucId, -1));
		}
		result.add("CarList", jsonArray);
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
	}

	/**
	 * 设置用户进入房间显示的座驾(10005033)
	 * @param jsonObject
	 * @param checkTag
	 * @return
	 */
	public JsonObject setUserEntranceCar(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		JsonObject result = new JsonObject();

		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		Integer userId, ucId;
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
			ucId = CommonUtil.getJsonParamInt(jsonObject, "ucId", 0, "05330002", -1, Integer.MAX_VALUE);
		} catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch (Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}

		CarService carService = (CarService) MelotBeanFactory.getBean("carService");
		if(carService.updateSetEntranceCar(userId, ucId)) {
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		} else {
			// user car not exist
			result.addProperty("TagCode", "05330102");
		}

		return result;
	}

	/**
	 * 查询虚拟号列表（10005059）
	 *
	 * @param paramJsonObject
	 * @return
	 */
    public JsonObject getVirtualIdlist(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        int count, startNum, endNum;

        try {
            count = CommonUtil.getJsonParamInt(paramJsonObject, "count", 10, "05590001", 1, Integer.MAX_VALUE);
            startNum = CommonUtil.getJsonParamInt(paramJsonObject, "startNum", 1, "05590002", 1, Integer.MAX_VALUE);
            endNum = CommonUtil.getJsonParamInt(paramJsonObject, "endNum", 1, "05590003", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        Gson gson = new Gson();

        List<String> luckyIdList = HotDataSource.srandmember(luckyIdKey + "." + startNum + "-" + endNum, count);
        if (luckyIdList == null || luckyIdList.size() < count) {
            HotDataSource.del(luckyIdKey + "." + startNum + "-" + endNum);

            /*
             * 每次从模块获取靓号500个，然后存入缓存5分钟，再接口调用时随机返回count个
             */
            List<ConfVirtualId> getVirtualIdList = null;
            try {
                VirtualIdService virtualIdService = MelotBeanFactory.getBean("virtualIdService", VirtualIdService.class);
                getVirtualIdList = virtualIdService.getVirtualIdList(startNum, endNum, 1);
            } catch (Exception e) {
                logger.error("virtualIdService.getVirtualIdList failed", e);
            }
            if (getVirtualIdList != null && getVirtualIdList.size() > 0) {
                String[] values = new String[getVirtualIdList.size()];
                int i = 0;
                for (ConfVirtualId confVirtualId : getVirtualIdList) {
                    values[i++] = gson.toJson(confVirtualId);
                }
                HotDataSource.sadd(luckyIdKey + "." + startNum + "-" + endNum, 5 * 60, values);

                luckyIdList = HotDataSource.srandmember(luckyIdKey + "." + startNum + "-" + endNum, count);
            }
        }

        JsonArray luckyIdArray = new JsonArray();
        if (luckyIdList != null && luckyIdList.size() > 0) {
            JsonObject jsonObject;
            for (String str : luckyIdList) {
                ConfVirtualId confVirtualId = gson.fromJson(str, TypeToken.get(ConfVirtualId.class).getType());

                jsonObject = new JsonObject();
                jsonObject.addProperty("id", confVirtualId.getLuckyId());
                jsonObject.addProperty("idType", confVirtualId.getLuckidType());
                jsonObject.addProperty("price", confVirtualId.getPrice());
                jsonObject.addProperty("useTicket", confVirtualId.getUseTicket());

                // 返回靓号背景icon
                jsonObject.addProperty("backIcon", confVirtualId.getBackIcon());

                luckyIdArray.add(jsonObject);
            }
        }

        result.add("luckyIdList", luckyIdArray);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);

        return result;
    }

    /**
     * 查询用户号券余额接口（10005037）
     *
     * @param paramJsonObject
     * @param checkTag
     * @return
     */
    public JsonObject getIdTicketCount(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(paramJsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        String cacheResult = HotDataSource.getTempDataString("UserAssetAction.getIdTicketCount." + userId);
        if (!StringUtil.strIsNull(cacheResult)) {
            return new JsonParser().parse(cacheResult).getAsJsonObject();
        }

        // 调用虚拟号模块
        com.melot.asset.driver.domain.ResGetIdTicketCount resGetIdTicketCount = null;
        try {
            AssetService assetService = MelotBeanFactory.getBean("assetService", AssetService.class);
            resGetIdTicketCount = assetService.getIdTicketCount(userId);
        } catch (Exception e) {
            logger.error("assetService.getIdTicketCount failed, userId: " + userId, e);
        }
        if (resGetIdTicketCount != null) {
            result.addProperty("luckyTicketCount", resGetIdTicketCount.getLuckyTicketCount());
            result.addProperty("goldTicketCount", resGetIdTicketCount.getGoldTicketCount());
            result.addProperty("silverTicketCount", resGetIdTicketCount.getSilverTicketCount());
        } else {
            result.addProperty("luckyTicketCount", 0);
            result.addProperty("goldTicketCount", 0);
            result.addProperty("silverTicketCount", 0);
        }

        result.addProperty("TagCode", TagCodeEnum.SUCCESS);

        HotDataSource.setTempDataString("UserAssetAction.getIdTicketCount." + userId, result.toString(), 15);

        return result;
    }

    /**
     * 购买号码接口（10005040）
     *
     * @param paramJsonObject
     * @param checkTag
     * @return
     */
    public JsonObject buyVirtualId(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId, virtualId, ticketType, sendUserId;
        try {
            userId = CommonUtil.getJsonParamInt(paramJsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            virtualId = CommonUtil.getJsonParamInt(paramJsonObject, "virtualId", 0, "05400001", 1, Integer.MAX_VALUE);
            ticketType = CommonUtil.getJsonParamInt(paramJsonObject, "ticketType", 0, "05400002", -1, 0);
            sendUserId = CommonUtil.getJsonParamInt(paramJsonObject, "sendUserId", 0, null, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        //如果赠送他人，检验被赠送人id的有效性
        if (sendUserId > 0) {
            UserProfile sendUser = com.melot.kktv.service.UserService.getUserInfoV2(sendUserId);
            if (sendUser == null) {
                result.addProperty("TagCode", "05620008");
                return result;
            }
        }

        // 调用虚拟号模块
        try {
            VirtualIdService virtualIdService = MelotBeanFactory.getBean("virtualIdService", VirtualIdService.class);
            long endTime = virtualIdService.buyVirtualId(userId, virtualId, ticketType, sendUserId);

            result.addProperty("endTime", endTime);
            result.addProperty("showMoney", UserService.getUserShowMoney(userId));

            com.melot.asset.driver.domain.ResVirtualIdInfo resVirtualIdInfo = new com.melot.asset.driver.domain.ResVirtualIdInfo();
            resVirtualIdInfo.setVirtualId(virtualId);
            resVirtualIdInfo.setIdType(1);

            int virtualIdLength = String.valueOf(virtualId).length();
            long startNum = Long.valueOf("1" + "000000000000000000000000".substring(0, virtualIdLength - 1));
            long endNum = Long.valueOf("1" + "000000000000000000000000".substring(0, virtualIdLength)) - 1;

            // 删除redis中已被购买的虚拟号缓存
            HotDataSource.del(luckyIdKey + "." + startNum + "-" + endNum);

            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch (MelotModuleException e) {
            int errCode = e.getErrCode();
            if (errCode > 0) {
                result.addProperty("TagCode", "05" + errCode);
            } else {
                result.addProperty("TagCode", "05400006");
            }
        	logger.error("buy lucky ID catched exception", e);
        } catch (Exception e) {
        	logger.error("buy lucky ID catched exception", e);
            result.addProperty("TagCode", "05400006");
            return result;
        }

        return result;
    }

    /**
     * 保号接口（10005041）
     *
     * @param paramJsonObject
     * @param checkTag
     * @return
     */
    public JsonObject reletVirtualId(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId, virtualId, periodCount, type;
        try {
            userId = CommonUtil.getJsonParamInt(paramJsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            virtualId = CommonUtil.getJsonParamInt(paramJsonObject, "virtualId", 0, "05410001", 1, Integer.MAX_VALUE);
            periodCount = CommonUtil.getJsonParamInt(paramJsonObject, "periodCount", 0, "05410002", 1, Integer.MAX_VALUE);
            type = CommonUtil.getJsonParamInt(paramJsonObject, "type", 0, null, 0, 1);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        // 调用虚拟号模块
        try {
            VirtualIdService virtualIdService = MelotBeanFactory.getBean("virtualIdService", VirtualIdService.class);
            long endTime = virtualIdService.reletVirtualId(userId, virtualId, periodCount, type);

            // 删除有效靓号缓存
            HotDataSource.del(userValidIdCache + userId);

            result.addProperty("endTime", endTime);
            result.addProperty("showMoney", UserService.getUserShowMoney(userId));
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch (MelotModuleException e) {
            int errCode = e.getErrCode();
            if (errCode > 0) {
                result.addProperty("TagCode", "05" + errCode);
            } else {
                result.addProperty("TagCode", "05410007");
            }
        } catch (Exception e) {
            result.addProperty("TagCode", "05410007");
            return result;
        }

        return result;
    }

	/**
	 * 获取用户所拥有的靓号列表(10005045)
	 *
	 * @param paramJsonObject
	 * @return 用户的靓号列表
	 */
	public JsonObject getUserLuckyIdList(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
	    JsonObject result = new JsonObject();

        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(paramJsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        String cacheResult = HotDataSource.getTempDataString("UserAssetAction.getUserLuckyIdList." + userId);
        if (!StringUtil.strIsNull(cacheResult)) {
            return new JsonParser().parse(cacheResult).getAsJsonObject();
        }

        JsonArray jsonArray = new JsonArray();
        long leftTime;

        // 调用虚拟号模块
        List<com.melot.asset.driver.domain.ResVirtualIdInfo> list = null;
        try {
            AssetService assetService = MelotBeanFactory.getBean("assetService", AssetService.class);
            list = assetService.getUserVirtualIdList(userId);
        } catch (Exception e) {
            logger.error("assetService.getUserVirtualIdList failed, userId: " + userId, e);
        }

        // 获取靓号续费配置信息
        ReletVirtualIdConfig reletVirtualIdConfig = null;
        try {
            VirtualIdService virtualIdService = MelotBeanFactory.getBean("virtualIdService", VirtualIdService.class);
            reletVirtualIdConfig = virtualIdService.getReletVirtualIdConfig();
        } catch (Exception e) {
            logger.error("virtualIdService.getReletVirtualIdConfig failed", e);
        }

        if (list != null && list.size() > 0) {
            for (com.melot.asset.driver.domain.ResVirtualIdInfo resVirtualIdInfo : list) {
                if (resVirtualIdInfo.getIdType() == 2 || resVirtualIdInfo.getIdType() > 4) {
                    continue;
                }

                JsonObject luckyIdJson = new JsonObject();
                luckyIdJson.addProperty("luckyId", resVirtualIdInfo.getVirtualId());

                leftTime = resVirtualIdInfo.getEndTime() * 1000 - System.currentTimeMillis();
                leftTime = leftTime < 1 ? 0 : leftTime;
                luckyIdJson.addProperty("leftTime", leftTime);

                luckyIdJson.addProperty("isLight", resVirtualIdInfo.getIsLight());
                luckyIdJson.addProperty("idType", resVirtualIdInfo.getIdType());
                luckyIdJson.addProperty("endTime", resVirtualIdInfo.getEndTime());
                luckyIdJson.addProperty("idState", resVirtualIdInfo.getIdState());
                luckyIdJson.addProperty("isEnable", resVirtualIdInfo.getIsEnable());
                luckyIdJson.addProperty("remainDays", resVirtualIdInfo.getRemainDays());

                // 该靓号续费的配置信息
                if (reletVirtualIdConfig != null) {
                    if (resVirtualIdInfo.getVirtualId() >= 100000 && resVirtualIdInfo.getVirtualId() < 1000000) {
                        luckyIdJson.addProperty("reletPrice", reletVirtualIdConfig.getLucky6ReletPrice());
                        luckyIdJson.addProperty("reletTicket", reletVirtualIdConfig.getLucky6ReletTicket());
                    } else if (resVirtualIdInfo.getVirtualId() >= 1000000 && resVirtualIdInfo.getVirtualId() < 10000000) {
                        luckyIdJson.addProperty("reletPrice", reletVirtualIdConfig.getLucky7ReletPrice());
                        luckyIdJson.addProperty("reletTicket", reletVirtualIdConfig.getLucky7ReletTicket());
                    } else if (resVirtualIdInfo.getVirtualId() >= 10000000 && resVirtualIdInfo.getVirtualId() < 100000000) {
                        luckyIdJson.addProperty("reletPrice", reletVirtualIdConfig.getLucky8ReletPrice());
                        luckyIdJson.addProperty("reletTicket", reletVirtualIdConfig.getLucky8ReletTicket());
                    } else if (resVirtualIdInfo.getVirtualId() >= 10000 && resVirtualIdInfo.getVirtualId() < 100000) {
                        luckyIdJson.addProperty("reletPrice", reletVirtualIdConfig.getLucky5ReletPrice());
                        luckyIdJson.addProperty("reletTicket", reletVirtualIdConfig.getLucky5ReletTicket());
                    } else if (resVirtualIdInfo.getVirtualId() >= 1000 && resVirtualIdInfo.getVirtualId() < 10000) {
                        luckyIdJson.addProperty("reletPrice", reletVirtualIdConfig.getLucky4ReletPrice());
                        luckyIdJson.addProperty("reletTicket", reletVirtualIdConfig.getLucky4ReletTicket());
                    }
                }

                // 返回靓号背景icon
                luckyIdJson.addProperty("backIcon", resVirtualIdInfo.getBackIcon());

                jsonArray.add(luckyIdJson);

                if (resVirtualIdInfo.getIsEnable() == 2) {
                    result.addProperty("validLuckyId", resVirtualIdInfo.getVirtualId());
                }
            }
        }

		result.add("luckyIdList", jsonArray);
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);

        HotDataSource.setTempDataString("UserAssetAction.getUserLuckyIdList." + userId, result.toString(), 15);
		return result;
	}

    /** 获取用户所拥有的登陆账号（靓号、尊号、帝号...）(10005052)
     *
     * @param paramJsonObject 请求json对象
     * @return 用户关联的虚拟账号
     * @throws Exception
     *
     */
    public JsonObject getUserVirtualIdList(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(paramJsonObject, "userId", 0, "05520002", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        String cacheResult = HotDataSource.getTempDataString("UserAssetAction.getUserVirtualIdList." + userId);
        if (!StringUtil.strIsNull(cacheResult)) {
            return new JsonParser().parse(cacheResult).getAsJsonObject();
        }

        JsonArray jsonArray = new JsonArray();
        long leftTime;

        // 调用虚拟号模块
        List<com.melot.asset.driver.domain.ResVirtualIdInfo> list = null;
        try {
            AssetService assetService = MelotBeanFactory.getBean("assetService", AssetService.class);
            list = assetService.getUserVirtualIdList(userId);
        } catch (Exception e) {
            logger.error("assetService.getUserVirtualIdList failed, userId: " + userId, e);
        }

        // 获取靓号续费配置信息
        ReletVirtualIdConfig reletVirtualIdConfig = null;
        try {
            VirtualIdService virtualIdService = MelotBeanFactory.getBean("virtualIdService", VirtualIdService.class);
            reletVirtualIdConfig = virtualIdService.getReletVirtualIdConfig();
        } catch (Exception e) {
            logger.error("virtualIdService.getReletVirtualIdConfig failed", e);
        }

        if (list != null && list.size() > 0) {
            for (com.melot.asset.driver.domain.ResVirtualIdInfo resVirtualIdInfo : list) {
                JsonObject luckyIdJson = new JsonObject();
                luckyIdJson.addProperty("id", resVirtualIdInfo.getVirtualId());

                leftTime = resVirtualIdInfo.getEndTime() * 1000 - System.currentTimeMillis();
                leftTime = leftTime < 1 ? 0 : leftTime;
                luckyIdJson.addProperty("leftTime", leftTime);

                luckyIdJson.addProperty("isLight", resVirtualIdInfo.getIsLight());

                if (resVirtualIdInfo.getIdType() == 2 || resVirtualIdInfo.getIdType() > 4) {
                    luckyIdJson.addProperty("idType", 2);
                } else {
                    luckyIdJson.addProperty("idType", 1);
                }
                luckyIdJson.addProperty("newIdType", resVirtualIdInfo.getIdType());
                luckyIdJson.addProperty("endTime", resVirtualIdInfo.getEndTime());
                luckyIdJson.addProperty("idState", resVirtualIdInfo.getIdState());
                luckyIdJson.addProperty("isEnable", resVirtualIdInfo.getIsEnable());
                luckyIdJson.addProperty("remainDays", resVirtualIdInfo.getRemainDays());
                if (resVirtualIdInfo.getIsRenew() != null && resVirtualIdInfo.getIsRenew() == 0) {
                    luckyIdJson.addProperty("isRenew", 0);
                } else {
                    luckyIdJson.addProperty("isRenew", 1);
                }


                // 该靓号续费的配置信息
                if (reletVirtualIdConfig != null) {
                    if (resVirtualIdInfo.getVirtualId() >= 100000 && resVirtualIdInfo.getVirtualId() < 1000000) {
                        luckyIdJson.addProperty("reletPrice", reletVirtualIdConfig.getLucky6ReletPrice());
                        luckyIdJson.addProperty("reletTicket", reletVirtualIdConfig.getLucky6ReletTicket());
                    } else if (resVirtualIdInfo.getVirtualId() >= 1000000 && resVirtualIdInfo.getVirtualId() < 10000000) {
                        luckyIdJson.addProperty("reletPrice", reletVirtualIdConfig.getLucky7ReletPrice());
                        luckyIdJson.addProperty("reletTicket", reletVirtualIdConfig.getLucky7ReletTicket());

                    } else if (resVirtualIdInfo.getVirtualId() >= 10000000 && resVirtualIdInfo.getVirtualId() < 100000000) {
                        luckyIdJson.addProperty("reletPrice", reletVirtualIdConfig.getLucky8ReletPrice());
                        luckyIdJson.addProperty("reletTicket", reletVirtualIdConfig.getLucky8ReletTicket());
                    } else if (resVirtualIdInfo.getVirtualId() >= 10000 && resVirtualIdInfo.getVirtualId() < 100000) {
                        luckyIdJson.addProperty("reletPrice", reletVirtualIdConfig.getLucky5ReletPrice());
                        luckyIdJson.addProperty("reletTicket", reletVirtualIdConfig.getLucky5ReletTicket());
                    } else if (resVirtualIdInfo.getVirtualId() >= 1000 && resVirtualIdInfo.getVirtualId() < 10000) {
                        luckyIdJson.addProperty("reletPrice", reletVirtualIdConfig.getLucky4ReletPrice());
                        luckyIdJson.addProperty("reletTicket", reletVirtualIdConfig.getLucky4ReletTicket());
                    }
                }

                // 返回靓号背景icon
                luckyIdJson.addProperty("backIcon", resVirtualIdInfo.getBackIcon());
                luckyIdJson.addProperty("iconType", resVirtualIdInfo.getIconType());

                jsonArray.add(luckyIdJson);

                if (resVirtualIdInfo.getIsEnable() == 2) {
                    // 获取激活虚拟账号
                    result.add("validId", luckyIdJson);
                }
            }
        }

        result.add("idList", jsonArray);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);

        HotDataSource.setTempDataString("UserAssetAction.getUserVirtualIdList." + userId, result.toString(), 15);

        return result;
    }

	/**
	 * 激活用户虚拟号(靓号/尊号)(10005046)
	 * @param paramJsonObject
	 * @param checkTag
	 * @return
	 */
	public JsonObject enableUserLuckyId(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
	    JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId, virtualId;
        try {
            userId = CommonUtil.getJsonParamInt(paramJsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            virtualId = CommonUtil.getJsonParamInt(paramJsonObject, "luckyId", 0, "05460001", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        // 调用虚拟号模块
        try {
            AssetService assetService = MelotBeanFactory.getBean("assetService", AssetService.class);
            assetService.enableUserLuckyId(userId, virtualId);

            HotDataSource.del(userValidIdCache + userId);
            HotDataSource.del("UserAssetAction.getUserVirtualIdList." + userId);
            HotDataSource.del("UserAssetAction.getUserLuckyIdList." + userId);

            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch (MelotModuleException e) {
            int errCode = e.getErrCode();
            if (errCode > 0) {
                result.addProperty("TagCode", "05" + errCode);
            } else {
                result.addProperty("TagCode", "05460004");
            }
        } catch (Exception e) {
            result.addProperty("TagCode", "05460004");
            return result;
        }

		return result;
	}

	/**
	 * 用户靓号转化为用户编号(10005047)
	 * @param paramJsonObject
	 * @return
	 */
	public JsonObject luckyIdToUserId(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
	    JsonObject result = new JsonObject();

        int virtualId;
        try {
            virtualId = CommonUtil.getJsonParamInt(paramJsonObject, "luckyId", 0, "05470001", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        // 调用虚拟号模块
        Integer userId = UserAssetServices.luckyIdToUserId(virtualId);

        if (userId == null || userId < 1) {
            result.addProperty("userId", virtualId);
        } else {
            result.addProperty("userId", userId);
        }
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);

        return result;
	}

	/**
	 * 获取用户库存信息 10005053
	 * @param jsonObject
	 * @return
	 */
	public JsonObject getUserGiftQuanlity(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		int userId = 0;
		String giftId = null;
		JsonObject result = new JsonObject();
		try{
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
		    giftId = CommonUtil.getJsonParamString(jsonObject, "giftId", null, null, 0, 300);
		} catch(CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch(Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		//调用库存模块读取用户的指定礼物ID的库存
		List<StorehouseInfo> storehouseInfolist = StorehouseService.getUserGiftCount(userId, giftId);
		if(storehouseInfolist != null) {
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			result.add("storehouseInfo", new Gson().toJsonTree(storehouseInfolist).getAsJsonArray());
			return result;
		}
		result.addProperty("TagCode",TagCodeEnum.USER_GIFT_NOT_EXIST);
		return result;
	}

    /**
     * 根据虚拟号类型随机获取虚拟号列表(10005060)
     * @param jsonObject
     * @return
     */
    public JsonObject getRandomVirtualIdlist(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
    	JsonObject result = new JsonObject();

    	int idType, count = 0;
    	try{
    		idType = CommonUtil.getJsonParamInt(jsonObject, "idType", 0, "05600001", 1, Integer.MAX_VALUE);
    		count = CommonUtil.getJsonParamInt(jsonObject, "count", 0, "05600002", 1, Integer.MAX_VALUE);
		} catch(CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch(Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}

    	JsonArray idList = new JsonArray();

    	// 调用模块模块虚拟号列表
    	List<ResVirtualIdInfo> list = null;
    	try {
    	    AssetService assetService = MelotBeanFactory.getBean("assetService", AssetService.class);
    	    list = assetService.getRandomVirtualIdList(idType, count);
        } catch (Exception e) {
            logger.error("assetService.getRandomVirtualIdList failed", e);
        }
    	if (list != null && list.size() > 0) {
    	    for (ResVirtualIdInfo resVirtualIdInfo : list) {
    	        JsonObject jObject = new JsonObject();
    	        jObject.addProperty("id", resVirtualIdInfo.getVirtualId());
    	        jObject.addProperty("idType", resVirtualIdInfo.getIdType());
    	        jObject.addProperty("price", resVirtualIdInfo.getPrice());
    	        jObject.addProperty("discount", resVirtualIdInfo.getDiscount());
    	        jObject.addProperty("useTicket", resVirtualIdInfo.getUseTicket());

    	        // 返回靓号背景icon
    	        jObject.addProperty("backIcon", resVirtualIdInfo.getBackIcon());

    	        idList.add(jObject);
    	    }
    	}

    	result.add("idList", idList);
    	result.addProperty("TagCode", TagCodeEnum.SUCCESS);

    	return result;
    }

    /**
     * 校验虚拟号是否可以购买(10005061)
     * @param jsonObject
     * @return
     */
    public JsonObject checkVirtualIdCanSell(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
    	JsonObject result = new JsonObject();

    	int virtualId = 0;
    	try{
    		virtualId = CommonUtil.getJsonParamInt(jsonObject, "virtualId", 0, "05610001", 1, Integer.MAX_VALUE);
		} catch(CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch(Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}

    	// 调用模块查询 virtualId 是否可以购买
    	ResVirtualIdInfo resVirtualIdInfo = null;
    	try {
    	    AssetService assetService = MelotBeanFactory.getBean("assetService", AssetService.class);
    	    resVirtualIdInfo = assetService.getVirtualIdInfo(virtualId);
        } catch (Exception e) {
            logger.error("assetService.getVirtualIdInfo failed, virtualId: " + virtualId, e);
        }
    	// 无效
    	if (resVirtualIdInfo == null || resVirtualIdInfo.getIdType() == null || resVirtualIdInfo.getIdType() < 1) {
            result.addProperty("TagCode", "05610002");
            return result;
        }

    	// 已售出
    	if (resVirtualIdInfo.getIsEnable() != null && resVirtualIdInfo.getIsEnable() == 1) {
    	    result.addProperty("TagCode", "05610003");
            return result;
        }

        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        result.addProperty("virtualId", virtualId);
        result.addProperty("idType", resVirtualIdInfo.getIdType());
        result.addProperty("isUsed", resVirtualIdInfo.getIsEnable());
        result.addProperty("price", resVirtualIdInfo.getPrice());
        result.addProperty("discount", resVirtualIdInfo.getDiscount());
        result.addProperty("useTicket", resVirtualIdInfo.getUseTicket());

    	return result;
    }

    /**
     * 新版购买虚拟号接口(10005062)
     * @param jsonObject
     * @param checkTag
     * @return
     */
    public JsonObject buyVirtualId_V2(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		JsonObject result = new JsonObject();
/*    	if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
	
    	int userId, virtualId1, userId1, virtualId2, userId2, buyType, useTicket = 0, idType;
		try{
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
			virtualId1 = CommonUtil.getJsonParamInt(jsonObject, "virtualId1", 0, "05620001", 1, Integer.MAX_VALUE);
			userId1 = CommonUtil.getJsonParamInt(jsonObject, "userId1", 0, "05620002", 1, Integer.MAX_VALUE);
			virtualId2 = CommonUtil.getJsonParamInt(jsonObject, "virtualId2", 0, null, 1, Integer.MAX_VALUE);
			userId2 = CommonUtil.getJsonParamInt(jsonObject, "userId2", 0, null, 1, Integer.MAX_VALUE);
			buyType = CommonUtil.getJsonParamInt(jsonObject, "buyType", 1, null, 1, 2);
			useTicket = CommonUtil.getJsonParamInt(jsonObject, "useTicket", 0, null, 1, Integer.MAX_VALUE);
			idType = CommonUtil.getJsonParamInt(jsonObject, "idType", 0, null, 1, Integer.MAX_VALUE);
		} catch(CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch(Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
    	
		// 调用虚拟号模块 查询 userId1 对应的真实用户ID，并判断用户ID是否有效
        Integer luckyId1 = UserAssetServices.luckyIdToUserId(userId1);
        if (luckyId1 != null && luckyId1 > 0) {
            userId1 = luckyId1;
        }
		UserProfile userinfo1 = com.melot.kktv.service.UserService.getUserInfo(userId1);
		if (userinfo1 == null) {
			result.addProperty("TagCode", "05620008");
			return result;
		}
		
		if (userId2 > 0) {
		    // 调用虚拟号模块 查询 userId2 对应的真实用户ID，并判断用户ID是否有效
	        Integer luckyId2 = UserAssetServices.luckyIdToUserId(userId2);
	        if (luckyId1 != null && luckyId1 > 0) {
	            userId2 = luckyId2;
	        }
	        UserProfile userinfo2 = com.melot.kktv.service.UserService.getUserInfo(userId2);
	        if (userinfo2 == null) {
	            result.addProperty("TagCode", "05620008");
	            return result;
	        }
        }

		AssetService assetService = null;
		try {
		    assetService = MelotBeanFactory.getBean("assetService", AssetService.class);
		} catch (Exception e) {
		    logger.error("failed", e);
		    result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
		    return result;
		}
		
		// 判断号码类型是匹配OK
		if (idType > 0) {
		    ResVirtualIdInfo resVirtualIdInfo = assetService.getVirtualIdInfo(virtualId1);
            if (resVirtualIdInfo == null || resVirtualIdInfo.getIdType() == null || resVirtualIdInfo.getIdType() < 1) {
                result.addProperty("TagCode", "05620003");
                return result;
            }
            
            if (resVirtualIdInfo.getIdType() != idType) {
                result.addProperty("TagCode", "05620010");
                return result;
            }
        }
		
		long showMoney = 0;
		int luckyTicketCount = 0;
		int goldTicketCount = 0;
		
		// 调用模块购买虚拟号
        try {
            ResBuyVirtualIdV2 resBuyVirtualIdV2 = assetService.buyVirtualIdV2(userId, virtualId1, userId1, virtualId2, userId2, buyType, useTicket);

            if (resBuyVirtualIdV2 != null) {
                showMoney = resBuyVirtualIdV2.getShowMoney();
                luckyTicketCount = resBuyVirtualIdV2.getLuckyTicketCount();
                goldTicketCount = resBuyVirtualIdV2.getGoldTicketCount();
            }
        } catch (MelotModuleException e) {
            
             * 05620003：购买的virtualId无效
             * 05620004：购买的virtualId已出售
             * 05620005：秀币余额不足
             * 05620006：券余额不足
             * 05620007：购买失败
             
            int errCode = e.getErrCode();
            if (errCode > 0) {
                result.addProperty("TagCode", "05" + errCode);
                return result;
            } else {
                result.addProperty("TagCode", "05400006");
                return result;
            }
        } catch (Exception e) {
            result.addProperty("TagCode", "05400006");
            return result;
        }
        
        result.addProperty("showMoney", showMoney);
        result.addProperty("luckyTicketCount", luckyTicketCount);
        result.addProperty("goldTicketCount", goldTicketCount);*/
        result.addProperty("TagCode", TagCodeEnum.FUNCTAG_INVALID_EXCEPTION);

    	return result;
    }

    /**
     * 模糊搜索虚拟号(10005063)
     * @param jsonObject
     * @return
     */
    public JsonObject fuzzyQueryVirtualId(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        String virtualId;
        int pageNum, pageCount;
        int len, low, high, order;

        try {
            virtualId = CommonUtil.getJsonParamString(jsonObject, "virtualId", null, null, 0, 32);
            len = CommonUtil.getJsonParamInt(jsonObject, "length", 0, null, 0, Integer.MAX_VALUE);
            low = CommonUtil.getJsonParamInt(jsonObject, "low", 0, null, 0, Integer.MAX_VALUE);
            high = CommonUtil.getJsonParamInt(jsonObject, "high", 0, null, 0, Integer.MAX_VALUE);
            order = CommonUtil.getJsonParamInt(jsonObject, "order", 0, null, 0, Integer.MAX_VALUE);
            pageNum = CommonUtil.getJsonParamInt(jsonObject, "pageNum", 1, "05630002", 1, Integer.MAX_VALUE);
            pageCount = CommonUtil.getJsonParamInt(jsonObject, "pageCount", 1, "05630003", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        if (virtualId != null && virtualId.replace(" ", "").length() < 3) {
            result.addProperty("TagCode", "05630004");
            return result;
        }

        //调用模块的模糊搜索虚拟号列表
        PageConfVirtualId pageConfVirtualId = null;

        try {
            VirtualIdService virtualIdService = MelotBeanFactory.getBean("virtualIdService", VirtualIdService.class);
            pageConfVirtualId = virtualIdService.getFuzzyVirtualIds(virtualId, 1, len, low * 1000, high * 1000, order, pageNum, pageCount);
        } catch (Exception e) {
            logger.error("virtualIdService.getFuzzyVirtualIds failed, param: [" + virtualId + ", 1, " + len + ", " + low * 1000 + ", " + high * 1000 + ", " + order + ", " + pageNum + ", " + pageCount + "]", e);
        }

        JsonArray jsonArray = new JsonArray();
        int pageTotal = 0;
        if (pageConfVirtualId != null && pageConfVirtualId.getList() != null && pageConfVirtualId.getList().size() > 0) {
            pageTotal = pageConfVirtualId.getPageTotal();
            for (ConfVirtualId confVirtualId : pageConfVirtualId.getList()) {
                jsonObject = new JsonObject();
                jsonObject.addProperty("id", confVirtualId.getLuckyId());
                jsonObject.addProperty("idType", confVirtualId.getLuckidType());
                jsonObject.addProperty("price", confVirtualId.getPrice());
                jsonObject.addProperty("rmb", confVirtualId.getPrice() / 1000);
                jsonObject.addProperty("useTicket", confVirtualId.getUseTicket());

                // 返回靓号背景icon
                jsonObject.addProperty("backIcon", confVirtualId.getBackIcon());
                jsonObject.addProperty("iconType", confVirtualId.getIconType());

                jsonArray.add(jsonObject);
            }
        }

        result.add("idList", jsonArray);
        result.addProperty("pageTotal", pageTotal);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);

        return result;
    }

    /**
     * 根据虚拟号id获取靓号详细信息(10005067)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getVirtualIdDetail(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        int virtualId;
        try {
            virtualId = CommonUtil.getJsonParamInt(jsonObject, "virtualId", 0, "05670001", 0, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        // 调用模块的搜索虚拟号
        ConfVirtualId confVirtualId = null;
        // 该靓号续费的配置信息
        ReletVirtualIdConfig reletVirtualIdConfig = null;

        try {
            VirtualIdService virtualIdService = MelotBeanFactory.getBean("virtualIdService", VirtualIdService.class);
            confVirtualId = virtualIdService.getConfVirtualIdById(virtualId);
            reletVirtualIdConfig = virtualIdService.getReletVirtualIdConfig();
        } catch (Exception e) {
            logger.error("virtualIdService.getConfVirtualIdById or virtualIdService.getReletVirtualIdConfig failed, virtualId" + virtualId, e);
        }

        if (confVirtualId != null) {
            result.addProperty("id", confVirtualId.getLuckyId());
            result.addProperty("idType", confVirtualId.getLuckidType());
            result.addProperty("price", confVirtualId.getPrice());
            result.addProperty("rmb", confVirtualId.getPrice() != null ? confVirtualId.getPrice() / 1000 : null);
            result.addProperty("useTicket", confVirtualId.getUseTicket());
            result.addProperty("backIcon", confVirtualId.getBackIcon());
            result.addProperty("iconType", confVirtualId.getIconType());
            if (reletVirtualIdConfig != null) {
                if (confVirtualId.getLuckyId() >= 100000 && confVirtualId.getLuckyId() < 1000000) {
                    result.addProperty("reletPrice", reletVirtualIdConfig.getLucky6ReletPrice());
                    result.addProperty("reletTicket", reletVirtualIdConfig.getLucky6ReletTicket());
                } else if (confVirtualId.getLuckyId() >= 1000000 && confVirtualId.getLuckyId() < 10000000) {
                    result.addProperty("reletPrice", reletVirtualIdConfig.getLucky7ReletPrice());
                    result.addProperty("reletTicket", reletVirtualIdConfig.getLucky7ReletTicket());
                } else if (confVirtualId.getLuckyId() >= 10000000 && confVirtualId.getLuckyId() < 100000000) {
                    result.addProperty("reletPrice", reletVirtualIdConfig.getLucky8ReletPrice());
                    result.addProperty("reletTicket", reletVirtualIdConfig.getLucky8ReletTicket());
                } else if (confVirtualId.getLuckyId() >= 10000 && confVirtualId.getLuckyId() < 100000) {
                    result.addProperty("reletPrice", reletVirtualIdConfig.getLucky5ReletPrice());
                    result.addProperty("reletTicket", reletVirtualIdConfig.getLucky5ReletTicket());
                } else if (confVirtualId.getLuckyId() >= 1000 && confVirtualId.getLuckyId() < 10000) {
                    result.addProperty("reletPrice", reletVirtualIdConfig.getLucky4ReletPrice());
                    result.addProperty("reletTicket", reletVirtualIdConfig.getLucky4ReletTicket());
                }
            }
        }

        result.addProperty("TagCode", TagCodeEnum.SUCCESS);

        return result;
    }

    /**
     * 取消默认虚拟号(10005064)
     * @param jsonObject
     * @param checkTag
     * @return
     */
    public JsonObject cancelDefaultVirtualId(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId, virtualId;

        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            virtualId = CommonUtil.getJsonParamInt(jsonObject, "virtualId", 0, "05640001", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        // 调用模块方法，将用户虚拟号改为未激活状态
        String tagCode = null;
        try {
            VirtualIdService virtualIdService = MelotBeanFactory.getBean("virtualIdService", VirtualIdService.class);
            tagCode = virtualIdService.updateVirtualIdEnable(userId, virtualId, 1);

            HotDataSource.del(userValidIdCache + userId);
            HotDataSource.del("UserAssetAction.getUserVirtualIdList." + userId);
            HotDataSource.del("UserAssetAction.getUserLuckyIdList." + userId);
        } catch (MelotModuleException e) {
            int errCode = e.getErrCode();
            if (errCode > 0) {
                result.addProperty("TagCode", "05640" + errCode);
                return result;
            } else {
                result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }

        result.addProperty("TagCode", tagCode);

        return result;
    }

    /**
     * 佩戴/取消佩戴用户活动勋章或充值勋章(10005065)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject operatorUserActivityMedal(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId, medalId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            medalId = CommonUtil.getJsonParamInt(jsonObject, "medalId", 0, "05650001", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        // 调用模块
        ActivityMedalService activityMedalService = (ActivityMedalService) MelotBeanFactory.getBean("activityMedalService");

        try {
            //贵族勋章不允许取消佩戴
            if (medalId >= 157 && medalId <= 163) {
                result.addProperty("TagCode", "05650004");
                return result;
            }

            UserWearMedalListModel userWearMedalListModel = activityMedalService.updateOperatorUserMedal(userId, medalId);

            List<UserActivityMedal> wearList = userWearMedalListModel.getWearList();
            List<UserActivityMedal> noWearList = userWearMedalListModel.getNoWearList();

            JsonArray wears = new JsonArray();
            JsonArray nowears = new JsonArray();

            try {
                //添加充值勋章信息
                UserMedalService userMedalService = (UserMedalService) MelotBeanFactory.getBean("userMedalService");
                com.melot.module.medal.driver.domain.GsonMedalObj medal = userMedalService.getMedalsByUserId(userId);
                Date now = new Date();
                if (medal != null) {
                    if (medal.getEndTime() > now.getTime()) { // 如果没有过期的话，才显示出来
                        JsonObject jObject = new JsonObject();
                        MedalInfo medalInfo = MedalConfig.getMedal(medal.getMedalId());

                        jObject.addProperty("medalId", medalInfo.getMedalId());
                        jObject.addProperty("medalTitle", medalInfo.getMedalTitle());
                        jObject.addProperty("medalType", medalInfo.getMedalType());
                        jObject.addProperty("medalIcon", medalInfo.getMedalIcon());
                        jObject.addProperty("medalDesc", medalInfo.getMedalDesc());
                        jObject.addProperty("medalRefId", medalInfo.getMedalRefId());
                        jObject.addProperty("endTime", medal.getEndTime());
                        jObject.addProperty("lightState", medal.getLightState());

                        //充值勋章不点亮放入可配置勋章列表
                        if (medal.getLightState() == 0) {
                            nowears.add(jObject);
                        } else {
                            wears.add(jObject);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Get user[" + userId + "] medal execute exception.", e);
            }

            if (wearList != null && wearList.size() > 0) {
                for (UserActivityMedal userActivityMedal : wearList) {
                    JsonObject jObject = new JsonObject();
                    jObject.addProperty("medalId", userActivityMedal.getMedalId());
                    jObject.addProperty("medalTitle", userActivityMedal.getMedalTitle());
                    jObject.addProperty("medalType", userActivityMedal.getMedalType());
                    jObject.addProperty("medalIcon", userActivityMedal.getMedalIcon());
                    jObject.add("medalDesc", userActivityMedal.getMedalDesc() != null ? new JsonParser().parse(userActivityMedal.getMedalDesc()).getAsJsonObject().get("description") : null);
                    jObject.addProperty("medalRefId", userActivityMedal.getMedalRefId() != null ? userActivityMedal.getMedalRefId() : 0);
                    jObject.addProperty("endTime", userActivityMedal.getEndTime().getTime());
                    jObject.addProperty("lightState", userActivityMedal.getLightState());
                    wears.add(jObject);
                }
            }

            if (noWearList != null && noWearList.size() > 0) {
                for (UserActivityMedal userActivityMedal : noWearList) {
                    JsonObject jObject = new JsonObject();
                    jObject.addProperty("medalId", userActivityMedal.getMedalId());
                    jObject.addProperty("medalTitle", userActivityMedal.getMedalTitle());
                    jObject.addProperty("medalType", userActivityMedal.getMedalType());
                    jObject.addProperty("medalIcon", userActivityMedal.getMedalIcon());
                    jObject.add("medalDesc", userActivityMedal.getMedalDesc() != null ? new JsonParser().parse(userActivityMedal.getMedalDesc()).getAsJsonObject().get("description") : null);
                    jObject.addProperty("medalRefId", userActivityMedal.getMedalRefId() != null ? userActivityMedal.getMedalRefId() : 0);
                    jObject.addProperty("endTime", userActivityMedal.getEndTime().getTime());
                    jObject.addProperty("lightState", userActivityMedal.getLightState());
                    nowears.add(jObject);
                }
            }

            result.add("wearMedalList", wears);
            result.add("noWearMedalList", nowears);
            result.addProperty("userId", userWearMedalListModel.getUserId());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch (MelotModuleException e) {
            int errCode = e.getErrCode();
            switch (errCode) {
            case 101:
                result.addProperty("TagCode", "05650003");
                break;
            case 102:
                result.addProperty("TagCode" , "05650002");
                break;
            case 103:
                result.addProperty("TagCode" , "05650004");
                break;
            default:
                result.addProperty("TagCode" , TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                break;
            }
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            logger.error("ActivityMedalService.updateOperatorUserMedal(" + userId + ", " + medalId + ") execute exception.", e);
        }

        return result;
    }

    /**
     * 获取房间荣誉墙(10005066)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getRoomHonorWall(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        int roomId;
        try {
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, TagCodeEnum.ROOMID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        // 调用模块
        KkUserService userService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
        List<UserResource> list = userService.getUserResourceForType(roomId, ResourceType.KK_MEDAL.typeId());

        JsonArray honors = new JsonArray();
        if (list != null && list.size() > 0) {
            // 先按获得时间倒序
            if (list.size() > 1) {
                Collections.sort(list, new Comparator<UserResource>() {
                    public int compare(UserResource o1, UserResource o2) {
                        Integer u1 = (int) o1.getId();
                        Integer u2 = (int) o2.getId();
                        return u2.compareTo(u1);
                    }
                });
            }
            
            for (UserResource userActivityMedal : list) {
                MedalInfo medalInfo = MedalConfig.getMedal(userActivityMedal.getResourceId());
                if (medalInfo != null && medalInfo.getMedalType() == 4 && userActivityMedal.getEndTime() > System.currentTimeMillis()) {
                    JsonObject jObject = new JsonObject();
                    jObject.addProperty("medalId", userActivityMedal.getResourceId());
                    jObject.addProperty("medalTitle", medalInfo.getMedalTitle());
                    jObject.addProperty("medalType", medalInfo.getMedalType());
                    jObject.addProperty("medalIcon", medalInfo.getMedalIcon() != null ? medalInfo.getMedalIcon() : null);
                    jObject.add("medalDesc", medalInfo.getMedalDesc() != null ? new JsonParser().parse(medalInfo.getMedalDesc()).getAsJsonObject().get("description") : null);
                    jObject.addProperty("medalRefId", medalInfo.getMedalRefId() != null ? medalInfo.getMedalRefId() : null);
                    jObject.addProperty("endTime", userActivityMedal.getEndTime());
                    jObject.addProperty("lightState", userActivityMedal.getIsEnable());
                    honors.add(jObject);
                }
            }
        }
        
        //金牌艺人（栏目titleId=1598）添加金牌艺人勋章
        HallRoomInfoDTO queryCondition = new HallRoomInfoDTO();
        queryCondition.setQueryRoomIds(String.valueOf(roomId));
        List<HallRoomInfoDTO> hallRoomInfoDTOs = defaultPartService.getPartRoomListForManage(1598, 0, 1, queryCondition);
        if (!Collectionutils.isEmpty(hallRoomInfoDTOs)) {
            JsonObject jObject = new JsonObject();
            jObject.addProperty("medalTitle", "金牌艺人");
            jObject.addProperty("medalType", 4);
            JsonObject jObj = new JsonObject();
            jObj.addProperty("web", ConfigHelper.getHttpdir() + "/activitymedal/web/goldmedal_web.png");
            jObj.addProperty("phone_large", ConfigHelper.getHttpdir() + "/activitymedal/phone/large/goldmedal_phone_large.png");
            jObj.addProperty("phone_small", ConfigHelper.getHttpdir() +"/activitymedal/phone/small/goldmedal_phone_small.png");
            jObject.addProperty("medalIcon", jObj.toString());
            jObject.addProperty("medalDesc", "金牌艺人");
            jObject.addProperty("lightState", 1);
            honors.add(jObject);
        }

        result.add("medalList", honors);
        result.addProperty("roomId", roomId);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);

        return result;
    }

}

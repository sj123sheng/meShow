package com.melot.kktv.action;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.kktv.lottery.arithmetic.LotteryArithmetic;
import com.melot.kktv.lottery.arithmetic.LotteryArithmeticCache;
import com.melot.kktv.lottery.service.UserLotteryService;
import com.melot.kktv.redis.HotDataSource;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.SecurityFunctions;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.TagCodeEnum;

/**
 * 类说明：用户抽奖功能
 * <p>作者：宋建明<a href="mailto:jianming.song@melot.cn"></p>
 * <p>创建日期：2014-4-25</p>
 * <p>版本：V1.0</p>
 * <p>修改历史：</p>
 */
public class UserLotteryPrizeFunctions {
	
	/**
	 * 校验参数
	 * @param paramJsonObject 参数 Json对象
	 * @param funcTag 方法标识
	 * @return 结果 Json 对象
	 */
	private static JsonObject checkParam(JsonObject paramJsonObject, String funcTag, boolean checkTag) {
		JsonObject result = new JsonObject();
		
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
		
		int userId = 0;
		String lotteryId = null;
		try {
	        userId = CommonUtil.getJsonParamInt(paramJsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
	        lotteryId = CommonUtil.getJsonParamString(paramJsonObject, "lotteryId", null, funcTag + "01", 1, 30);
	        
	        // 根据userId判断是否已绑定手机号,Redis的热点数据中查询phoneNum
	        String userPhoneNum = com.melot.kktv.service.UserService.getPhoneNumberOfUser(userId);
	        if (!StringUtil.strIsNull(userPhoneNum)) {
	            result.addProperty("userPhone", userPhoneNum);
	        } else {
	            result.addProperty("userPhone", "");
	        }
	        
	        result.addProperty("userId", userId);
	        result.addProperty("lotteryId", lotteryId);
		} catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
		
		return result;
	}
	
	/**
	 * 抽奖 (10010001)
	 * @param paramJsonObject 参数 Json对象
	 * @param checkTag Token 是否校验通过：true - 校验同， false - 没有校验通过
	 * @return 结果 Json 对象
	 * @throws Exception 
	 */
	public JsonObject lottery(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		JsonObject rtJO = SecurityFunctions.checkSignedValue(paramJsonObject);
		if(rtJO != null) {
			return rtJO;
		}
		
		JsonObject result = checkParam(paramJsonObject, "100100", checkTag);
        if (result.get("TagCode") != null) {
            return result;
        }
		
		String prizeId;
		try {
		    prizeId = CommonUtil.getJsonParamString(paramJsonObject, "prizeId", null, null, 0, 30);
		} catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
		
		String lotteryId = result.remove("lotteryId").getAsString();
		int userId = result.remove("userId").getAsInt();
		String userPhone = result.remove("userPhone").getAsString();
		userPhone = StringUtil.strIsNull(userPhone) ? null : userPhone;
		
		Map<String, Object> prize = LotteryArithmetic.lottery(lotteryId, userId, userPhone, prizeId);
		if (prize == null) {
			result.addProperty("TagCode", "10010005");
        } else if (LotteryArithmeticCache.SERVICE_KEY_LOTTERY_NO_PHONE_ERROR_STR.equals(prize.get(LotteryArithmeticCache.SERVICE_KEY_giftId))) {
            result.addProperty("TagCode", "10010003");
		} else if (LotteryArithmeticCache.SERVICE_KEY_LOTTERY_QUOTA_ERROR.equals(prize.get(LotteryArithmeticCache.SERVICE_KEY_giftId))
		        || LotteryArithmeticCache.SERVICE_KEY_LOTTERY_REMAIN_ERROR.equals(prize.get(LotteryArithmeticCache.SERVICE_KEY_giftId))) {
			result.addProperty("TagCode", "10010004");
		} else {
			result.addProperty("TagCode", "00000000");
			result.addProperty("prizeId", (String) prize.get(LotteryArithmeticCache.SERVICE_KEY_giftId));
			result.addProperty("prizeName", (String) prize.get(LotteryArithmeticCache.SERVICE_KEY_giftName));
			result.addProperty("prizeCount", (Integer) prize.get(LotteryArithmeticCache.SERVICE_KEY_giftCount));
		}
		
		return result;
	}

	/**
	 * 获取奖品 (10010002)
	 * @param paramJsonObject 参数 Json对象
     * @param checkTag Token 是否校验通过：true - 校验同， false - 没有校验通过
	 * @return 结果 Json 对象
	 * @throws Exception 
	 */
	public JsonObject getPrize(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		JsonObject rtJO = SecurityFunctions.checkSignedValue(paramJsonObject);
		if(rtJO != null) {
			return rtJO;
		}

		JsonObject result = checkParam(paramJsonObject, "100200", checkTag);
		if (result.get("TagCode") != null) {
			return result;
		}

        String lotteryId = result.remove("lotteryId").getAsString();
        int userId = result.remove("userId").getAsInt();
        String userPhone = result.remove("userPhone").getAsString();
        userPhone = StringUtil.strIsNull(userPhone) ? null : userPhone;
        
		String prizeId;
        try {
            prizeId = CommonUtil.getJsonParamString(paramJsonObject, "prizeId", null, "10020004", 1, 30);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        Map<String, Object> prize = LotteryArithmetic.checkUserReceptLottery(lotteryId, userId, userPhone, prizeId);
        if (prize == null) {
            result.addProperty("TagCode", "10020006");
        } else if (LotteryArithmeticCache.SERVICE_KEY_LOTTERY_NO_PHONE_ERROR_STR.equals(prize.get(LotteryArithmeticCache.SERVICE_KEY_giftId))) {
            result.addProperty("TagCode", "10020003");
        } else if (LotteryArithmeticCache.SERVICE_KEY_LOTTERY_QUOTA_ERROR.equals(prize.get(LotteryArithmeticCache.SERVICE_KEY_giftId))
                || LotteryArithmeticCache.SERVICE_KEY_LOTTERY_REMAIN_ERROR.equals(prize.get(LotteryArithmeticCache.SERVICE_KEY_giftId))) {
            result.addProperty("TagCode", "10020005");
        } else {
            result.addProperty("TagCode", "00000000");
            result.addProperty("prizeId", (String) prize.get(LotteryArithmeticCache.SERVICE_KEY_giftId));
            result.addProperty("prizeName", (String) prize.get(LotteryArithmeticCache.SERVICE_KEY_giftName));
            result.addProperty("prizeCount", (Integer) prize.get(LotteryArithmeticCache.SERVICE_KEY_giftCount));
        }
		
		return result;
	}

	/**
	 * 分享抽奖活动 (10010003)
	 * @param paramJsonObject 参数 Json对象
	 * @return 结果 Json 对象
	 */
	public JsonObject shareLottery(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
		JsonObject result = checkParam(paramJsonObject, "100300", checkTag);
		if (result.get("TagCode") != null) {
			return result;
		}

        String lotteryId = result.remove("lotteryId").getAsString();
        int userId = result.remove("userId").getAsInt();
        String userPhone = result.remove("userPhone").getAsString();
        userPhone = StringUtil.strIsNull(userPhone) ? null : userPhone;

		LotteryArithmetic.refreshInfo(lotteryId, userId, userPhone, "TotalShareCount");
		
		result.addProperty("TagCode", "00000000");
		
		return result;
	}
	
	/**
	 * 验证用户是否可以抽奖 (10010004)
	 * @param paramJsonObject 参数 Json对象
	 * @return 结果 Json 对象
	 */
	public JsonObject canLottery(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
		JsonObject result = checkParam(paramJsonObject, "100400", checkTag);
		if (result.get("TagCode") != null) {
			return result;
		}

        String lotteryId = result.remove("lotteryId").getAsString();
        int userId = result.remove("userId").getAsInt();
        String userPhone = result.remove("userPhone").getAsString();
        userPhone = StringUtil.strIsNull(userPhone) ? null : userPhone;

		Map<String, Long> map = LotteryArithmetic.checkUserLotteryQuota(lotteryId, userId, userPhone);
		if (map == null) {
			result.addProperty("TagCode", "10040004");
		} else if (map.containsKey(LotteryArithmeticCache.SERVICE_KEY_LOTTERY_NO_PHONE_ERROR_STR)) {
            result.addProperty("TagCode", "10040003");
        } else {
			result.addProperty("TagCode", "00000000");
			for (String key : map.keySet()) {
	            result.addProperty(key, map.get(key));
            }
		}
		
		return result;
	}

	/**
	 * 获取中奖用户列表 (10010005)
	 * @param paramJsonObject
	 * @return
	 */
	public JsonObject getUserLotteryList(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
		JsonObject result = new JsonObject();;
		
		String lotteryId;
		int count = 20;
		try {
            lotteryId = CommonUtil.getJsonParamString(paramJsonObject, "lotteryId", null, "10050001", 1, 60);
			count = CommonUtil.getJsonParamInt(paramJsonObject, "count", 1, "10050002", 1, Integer.MAX_VALUE);
		} catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
		
		lotteryId = "%" + lotteryId + "%";
		
		List<Map<String, String>> list = UserLotteryService.getUserLotteryPrizeInfos(lotteryId, count);
		if (list != null && !list.isEmpty()) {
			JsonArray jsonArray = new JsonArray();
			JsonObject jsonObject;
			JsonParser parser = new JsonParser();
			JsonObject prizeEntryJson;
			for (Map<String, String> userLotteryPrizeInfo : list) {
				prizeEntryJson = parser.parse(userLotteryPrizeInfo.get("prizeEntry")).getAsJsonObject();
				jsonObject = new JsonObject();
				jsonObject.addProperty("userId", userLotteryPrizeInfo.get("userId"));
				jsonObject.addProperty("userName", HotDataSource.getHotFieldValue(userLotteryPrizeInfo.get("userId"), "nickname"));
				jsonObject.addProperty("prizeName", prizeEntryJson.get("prizeName").getAsString());
				jsonObject.addProperty("count", prizeEntryJson.get("value").getAsInt());
				jsonArray.add(jsonObject);
			}
			
			result.add("lotteryList", jsonArray);
		}
		
		result.addProperty("TagCode", "00000000");
		return result;
	}

}

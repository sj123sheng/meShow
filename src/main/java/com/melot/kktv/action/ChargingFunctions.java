package com.melot.kktv.action;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.melot.kk.module.report.util.CommonStateCode;
import com.melot.kk.recharge.api.dto.RecharingRecordDto;
import com.melot.kk.recharge.api.service.RechargeService;
import com.melot.kktv.base.Result;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.DateUtil;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.TagCodeEnum;

/**
 * 充值接口类
 * 
 * @author LY
 * 
 */
public class ChargingFunctions {

	/** 日志记录对象 */
	private static Logger logger = Logger.getLogger(ChargingFunctions.class);
	
	@Resource
	RechargeService rechargeService;

	/**
	 * 获取充值的说明信息
	 * @param jsonObject
	 * @param checkTag
	 * @param request
	 * @return
	 */
	public JsonObject getChargeInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		int appId, platform;
		try {
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, 1, Integer.MAX_VALUE);
        } catch(Exception e) {
			JsonObject result = new JsonObject();
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
		JsonObject result = getChargeConfigInfo(appId,platform);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
	}

	/**
	 * 获得充值的说明信息 写死
	 * @param appId
	 * @param platform
	 * @return
	 */
	private JsonObject getChargeConfigInfo(int appId,int platform){
		JsonObject result = new JsonObject();
		if(appId == 1){
			result.addProperty("ratio", 0.0);
			if(platform == 1 || platform == 2){
				result.addProperty("content", " ");
			}
			else if(platform == 3 || platform == 4){
				result.addProperty("content", "提示：人民币和秀币的兑换比例为1:700");
			}
		}
        return result;
	}

	/**
	 * 获取用户充值记录列表
	 * 
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return json对象形式的返回结果
	 */
	public JsonObject getChargeRecordList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		// 该接口需要验证token,未验证的返回错误码
		JsonObject result = new JsonObject();
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		int userId = 0, pageIndex = 1;
        long startTime = 0, endTime = 0;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "05050001", 1, Integer.MAX_VALUE);
            startTime = CommonUtil.getJsonParamLong(jsonObject, "startTime", 0, "05050003", DateUtil.getDayBeginTime(System.currentTimeMillis()) - 180 * 24 * 3600 * 1000l, Long.MAX_VALUE);
            endTime = CommonUtil.getJsonParamLong(jsonObject, "endTime", 0, "05050005", startTime, Long.MAX_VALUE);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
        }
        
        try {
            int count = 0;
            BigDecimal chargeTotal = new BigDecimal(0);;
            JsonArray jRecordList = new JsonArray();
            Date startDate = new Date(startTime);
            Date endDate = new Date(endTime);
            Result< Map<String,Object>> resp = rechargeService.getUserRechargingExceeptProductRecordCount(userId, startDate, endDate);
            if (resp != null && CommonStateCode.SUCCESS.equals(resp.getCode())) {
                Map<String, Object> map = resp.getData();
                if (map.get("count") != null) {
                    count =  (int) map.get("count");
                }
                if (map.get("showmoney") != null) {
                    chargeTotal = (BigDecimal) map.get("showmoney");
                }
                
                if (count > 0) {
                    Result<List<RecharingRecordDto>> rechargingResp = rechargeService.getUserRechargingExceeptProductRecords(userId, startDate, endDate, (pageIndex -1) * 20, 20);
                    if (rechargingResp != null && CommonStateCode.SUCCESS.equals(rechargingResp.getCode())) {
                        List<RecharingRecordDto> recharingList = rechargingResp.getData();
                        if (!CollectionUtils.isEmpty(recharingList)) {
                            for (RecharingRecordDto recharingRecordDto : recharingList) {
                                JsonObject jObject = new JsonObject();
                                jObject.addProperty("orderId", recharingRecordDto.getOrderid());
                                jObject.addProperty("rechargeTime", recharingRecordDto.getRechargetime().getTime());
                                jObject.addProperty("amount", recharingRecordDto.getAmount());
                                jObject.addProperty("miMoney", recharingRecordDto.getMimoney());
                                jObject.addProperty("paymentMode", recharingRecordDto.getPaymentmode());
                                jObject.addProperty("state", recharingRecordDto.getState());
                                if (recharingRecordDto.getState() == 1) {
                                    jObject.addProperty("affirmTime", recharingRecordDto.getAffirmtime().getTime());
                                }
                                if (recharingRecordDto.getErrcode() != null) {
                                    jObject.addProperty("errcode", recharingRecordDto.getErrcode());
                                }
                                if (recharingRecordDto.getPaymentname() != null) {
                                    jObject.addProperty("modeDesc", recharingRecordDto.getPaymentname());
                                }
                                jRecordList.add(jObject);
                            }
                        }
                    }
                }
            }
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            result.addProperty("pageTotal", (int) Math.ceil((double) count/20));
            result.addProperty("chargeTotal", chargeTotal);
            result.add("recordList", jRecordList);
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            logger.error("ChargingFunctions.getChargeRecordList execute exception: ", e);
        }
        
        return result;
    }
	
	/**
	 * 用户是否充值过秀币(10005036)
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return
	 */
	public JsonObject whetherRecharged(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
	    JsonObject result = new JsonObject();
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		// 获取参数
		JsonElement userIdje = jsonObject.get("userId");

		// 验证参数
		Integer userId;
		if (userIdje != null && !userIdje.isJsonNull() && !userIdje.getAsString().isEmpty()) {
			// 验证数字
			try {
				userId = userIdje.getAsInt();
			} catch (Exception e) {
				result.addProperty("TagCode", "05360002");
				return result;
			}
		} else {
			result.addProperty("TagCode", "05360001");
			return result;
		}

        try {
            int count = 0;
            Result<Map<String, Object>> resp =  rechargeService.getUserRechargingRecordCount(userId, null, null);
            if (resp != null && CommonStateCode.SUCCESS.equals(resp.getCode())) {
                Map<String, Object> map = resp.getData();
                if (map.get("count") != null) {
                    count = (int) map.get("count");
                }
            }
            result.addProperty("isRecharged", count > 0 ? 1 : 0);
        } catch (Exception e) {
            logger.error("rechargeService.getUserRechargingRecordCount execute exception, userId: " + userId, e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }

        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
	}
	
}

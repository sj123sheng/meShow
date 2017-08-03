package com.melot.kktv.action;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.melot.kktv.model.RechargeRecord;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.DateUtil;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;

/**
 * 充值接口类
 * 
 * @author LY
 * 
 */
public class ChargingFunctions {

	/** 日志记录对象 */
	private static Logger logger = Logger.getLogger(ChargingFunctions.class);

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
        
		// 调用存储过程得到结果
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("userId", userId);
		map.put("startTime", new Date(startTime));
		map.put("endTime", new Date(endTime));
		map.put("pageIndex", pageIndex);
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Profile.getUserChargeList", map);
		} catch (SQLException e) {
			logger.error("未能正常调用存储过程", e);
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			return result;
		}
		String TagCode = (String) map.get("TagCode");
		if (TagCode.equals(TagCodeEnum.SUCCESS)) {
			// 取出列表
			@SuppressWarnings("unchecked")
			List<Object> recordList = (ArrayList<Object>) map.get("recordList");
			result.addProperty("TagCode", TagCode);
			result.addProperty("pageTotal", (Integer) map.get("pageTotal"));
			result.addProperty("chargeTotal", (Long) map.get("chargeTotal"));
			JsonArray jRecordList = new JsonArray();
			for (Object object : recordList) {
				jRecordList.add(((RechargeRecord) object).toJsonObject());
			}
			result.add("recordList", jRecordList);
			// 返回结果
			return result;
		} else if (TagCode.equals("02")) {
			/* '02';分页超出范围 */
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			result.addProperty("pageTotal", 0);
			result.add("recordList", new JsonArray());
			return result;
		} else {
			// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
			logger.error("调用存储过程(Profile.getUserChargeList)未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
			result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
			return result;
		}
	}
	
	/**
	 * 用户是否充值过秀币(10005036)
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return
	 */
	public JsonObject whetherRecharged(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			JsonObject result = new JsonObject();
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
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "05360002");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "05360001");
			return result;
		}

		// 调用存储过程得到结果
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("userId", userId);
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Profile.whetherRecharged", map);
		} catch (SQLException e) {
			logger.error("未能正常调用存储过程", e);
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			return result;
		}
		String TagCode = (String) map.get("TagCode");
		if (TagCode.equals("02")) {
			// 未充值
			JsonObject result = new JsonObject();
			result.addProperty("isRecharged", 0);
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			
			return result;
		} else if(TagCode.equals("03")) {
			// 已充值
			JsonObject result = new JsonObject();
			result.addProperty("isRecharged", 1);
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			return result;
		} else {
			// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
			logger.error("调用存储过程(Profile.whetherRecharged)未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
			return result;
		}
	}
	
}

package com.melot.kkcx.functions;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.HadoopLogger;
import com.melot.kktv.util.SecurityFunctions;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;

public class MobileFunctions {
	
	/** 日志记录对象 */
	private static Logger logger = Logger.getLogger(MobileFunctions.class);
//	private static final Logger statsLogger = Logger.getLogger("statsLogger");
	
	/**
	 * 游客登录（10007006）
	 * @param jsonObject
	 * @return
	 */
	public JsonObject guestLogin(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		
		JsonElement appIdje = jsonObject.get("a");
		JsonElement userIdje = jsonObject.get("userId");
		JsonElement platformje = jsonObject.get("platform");
		JsonElement channelje = jsonObject.get("channel");
		int appId = 0;
		int userId = 0;
		int platform = 0;
		int channel = 0;
		if (appIdje != null && !appIdje.isJsonNull() && !appIdje.getAsString().isEmpty()) {
			try {
				appId = appIdje.getAsInt();
			} catch (Exception e) {
	            appId = AppIdEnum.AMUSEMENT;
			}
		} else {
		    appId = AppIdEnum.AMUSEMENT;
		}
		if (userIdje != null && !userIdje.isJsonNull() && !userIdje.getAsString().isEmpty()) {
			try {
				userId = userIdje.getAsInt();
			} catch (Exception e) {
			}
		}
		if (platformje != null && !platformje.isJsonNull() && !platformje.getAsString().isEmpty()) {
			try {
				platform = platformje.getAsInt();
			} catch (Exception e) {
			}
		}
		if (channelje != null && !channelje.isJsonNull() && !channelje.getAsString().isEmpty()) {
			try {
				channel = channelje.getAsInt();
			} catch (Exception e) {
			}
		}
		
		String clientIp = CommonUtil.getIpAddr(request);
		Date curDate = new Date();
//		String curTime = new Long(curDate.getTime()/1000).toString();
//		
//		StringBuffer sb = new StringBuffer();
//		sb.append(userId).append("|");
//		sb.append(platform).append("|");
//		sb.append(channel).append("|");
//		sb.append(clientIp).append("|");
//		sb.append(curTime);
//		statsLogger.info(sb.toString());
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("userId", userId);
		map.put("appId", appId);
		map.put("platform", platform);
		map.put("channel", channel);
		map.put("ipaddr", clientIp);
		map.put("dtime", new Date());
		try {
            SqlMapClientHelper.getInstance(DB.BACKUP).queryForObject("Mobile.addGuestLoginInfo", map);
            String TagCode = (String) map.get("TagCode");
            if (!TagCode.equals(TagCodeEnum.SUCCESS)) {
                logger.error("调用存储过程(Mobile.addGuestLoginInfo(" + new Gson().toJson(map) + "))未得到正常结果,TagCode:" + TagCode);
            }
        } catch (Exception e) {
            logger.error("Mobile.addGuestLoginInfo(" + new Gson().toJson(map) + ") execute exception.", e);
        }
		
		HadoopLogger.loginLog(userId, curDate, platform, clientIp, 0, appId, SecurityFunctions.decodeED(jsonObject));
		
		JsonObject result = new JsonObject();
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
	
	}
	
}

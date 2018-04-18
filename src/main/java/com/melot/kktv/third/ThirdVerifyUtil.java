package com.melot.kktv.third;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.kktv.util.confdynamic.ThirdAppConfig;

public class ThirdVerifyUtil {
	
	private static Logger logger = Logger.getLogger(ThirdVerifyUtil.class);
	
	private Set<Integer> ignoreSet;
	
	private Map<Integer, BaseService> serviceMap;
	
	public ThirdVerifyUtil(Map<Integer, BaseService> serviceMap, Set<Integer> ignoreSet) {
		this.serviceMap = serviceMap;
		this.ignoreSet = ignoreSet;
	}

	public String verify(int openPlatform, String openId, String sessionId, int platform) {
		if (ignoreSet.contains(openPlatform)) {
			return TagCodeEnum.SUCCESS;
		}
		
		if (serviceMap != null) {
			BaseService service = serviceMap.get(openPlatform);
			if (service != null && service.getOpenPlatform() == openPlatform) {
				//platform verify
				if (service.getLimitPlatform() != null && Integer.parseInt(service.getLimitPlatform()) != platform) {
					return null;
				}
				try {
					String ret = service.verifyUser(openId, sessionId);
					if (ret != null ) {
						return TagCodeEnum.SUCCESS;
					}
				} catch (Exception e) {
					logger.error("fail to execute servicename : " + service.getThirdName(), e);
				}
				
				return null;
			}
		}
		
		//新版第三方校验
		Map<String, Object> confMap = ThirdAppConfig.getThirdInfo(openPlatform);
		if (confMap != null && confMap.size() > 0) {
			String url = (String) confMap.get("URL");
			String key = (String) confMap.get("KEY");
			String desc = (String) confMap.get("DESCRIBE");
			String param = constructParam(openId, sessionId, key);
			JsonObject result = doGet(param, url, desc);
			if (result != null && checkResult(result)) {
				return TagCodeEnum.SUCCESS;
			} else {
				logger.warn("call third verify service return null or return unexpected, url : " + url + param + ", result : " + result);
				return null;
			}
		}
		
		return null;
	}
	
	private JsonObject doGet(String param, String serverUrl, String desc) {
		HttpURLConnection url_con = null;
		Transaction t = Cat.getProducer().newTransaction("MCall", "ThirdVerifyUtil.doGet");
		try {
			URL url = new URL(serverUrl + param);
			url_con = (HttpURLConnection) url.openConnection();
            url_con.setRequestMethod("GET");
            url_con.setConnectTimeout(10000);
            url_con.setReadTimeout(5000);
            url_con.setDoInput(true);
            
            InputStream in = url_con.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            StringBuffer tempStr = new StringBuffer();
            String tempLine = null;
            while ((tempLine = rd.readLine()) != null) {
                tempStr.append(tempLine);
            }
            t.setStatus(Transaction.SUCCESS);
            return new JsonParser().parse(tempStr.toString()).getAsJsonObject();
		} catch (Exception e) {
			logger.error(desc + "服务端验证用户请求异常", e);
			t.setStatus(e);
		} finally {
            if (url_con != null) {
                url_con.disconnect();
            }
            t.complete();
        }
		return null;
	}
	
	private static String constructParam(String openId, String sessionId, String key) {
		long btime = System.currentTimeMillis() / 1000;
		String bsign = CommonUtil.md5(openId + sessionId + btime + key).toUpperCase();
		return "?uuid=" + openId + "&sessionId=" + sessionId + "&time=" + btime + "&sign=" + bsign;
	}
	
	private static boolean checkResult(JsonObject result) {
		logger.info(result);
		if (result.get("status") != null && result.get("status").getAsInt() == 0) return true;
		return false;
	}
}
package com.melot.kktv.third.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.kktv.third.BaseService;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.TagCodeEnum;

public class TongChengService extends BaseService {
	
	private static Logger logger = Logger.getLogger(TongChengService.class);
	
	// release http://v4.uc108.com/api/mobile/verifyaccount
	// test http://v4.uc108.org:1505/api/mobile/verifyaccount
	private static final String TongChengUrl= "http://v.tcy365.com/api/mobile/verifyaccount";
	
	private static final String key = "UbEfQ1tquGAxM2iv77Br7uH0GpsOrDnZ";
	
	public String verifyUser(String uuid, String sessionId) {
		HttpURLConnection url_con = null;
		try {
			long time = new Date().getTime()/1000L;
			String sign = CommonUtil.md5(uuid + time + key).toUpperCase();
			String param = "?userId=" + uuid + "&time=" + time + "&sign=" + sign; 			
			URL url = new URL(TongChengUrl + param);			
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
            JsonObject jsonObj = new JsonParser().parse(tempStr.toString()).getAsJsonObject();
            if (jsonObj.get("status") != null && jsonObj.get("status").getAsInt() == 1) {
            	return TagCodeEnum.SUCCESS;
            } else {
            	logger.error("同城游服务端验证用户失败, respose:" + jsonObj.toString());
            }
            rd.close();
            in.close();
		} catch (Exception e) {
			logger.error("同城游服务端验证用户请求异常", e);
		} finally {
            if (url_con != null) url_con.disconnect();
        }
		return null;
	}
	
}

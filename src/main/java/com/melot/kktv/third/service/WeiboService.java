package com.melot.kktv.third.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.kktv.third.BaseService;
import com.melot.kktv.util.TagCodeEnum;

public class WeiboService extends BaseService {
	
	private static Logger logger = Logger.getLogger(WeiboService.class);

	private String serverUrl;
	
	@Override
	public String verifyUser(String openId, String sessionId) {
		HttpURLConnection url_con = null;
		try {
			String param = "?access_token=" + sessionId; 			
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
            JsonObject jsonObj = new JsonParser().parse(tempStr.toString()).getAsJsonObject();
            if (jsonObj.get("uid") != null && jsonObj.get("uid").getAsString().equals(openId)) {
            	return TagCodeEnum.SUCCESS;
            } else {
            	logger.error("weibo服务端验证用户失败, respose:" + jsonObj.toString());
            }
            rd.close();
            in.close();
		} catch (Exception e) {
			logger.error("weibo服务端验证用户请求异常", e);
		} finally {
            if (url_con != null) url_con.disconnect();
        }
		return null;
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}
	
}

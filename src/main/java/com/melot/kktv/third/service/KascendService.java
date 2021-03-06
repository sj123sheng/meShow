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

public class KascendService extends BaseService{
	
	private static Logger logger = Logger.getLogger(KascendService.class);
	
	private static String appid = "338c81e835ecc0fa";
	private static String appsecret = "1aeaf2a338c81e835ecc0fa433fad3fb";
	
	private String serverUrl;
	
	/**
	 * @param sessionkey 用户登录标识	
	 * @param openid 开讯用户唯一标识
	 * @return
	 */
	public String verifyUser(String openid, String sessionkey) {
		boolean isValid = false;
		HttpURLConnection url_con = null;
        try {
			String queryParams = "?kasappid=" + appid + "&kasappsecret=" + appsecret
        			+ "&userid=" + openid + "&usersessionid=" + sessionkey;
        	logger.info("KascendService verifyUser : " + serverUrl + queryParams);
            URL url = new URL(serverUrl + queryParams);
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
			if(jsonObj.has("rc")&&jsonObj.get("rc").getAsInt()!=0)
				logger.error("开讯服务端验证用户失败, respose:" + jsonObj.toString());
			else
            	isValid = true;
            rd.close();
            in.close();
        } catch (Exception e) {
        	logger.error("开讯服务端验证用户请求异常", e);
        } finally {
            if (url_con != null) url_con.disconnect();
        }
        if (isValid) {
        	return TagCodeEnum.SUCCESS;
        } else {
        	return null;
        }
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}
	
}

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

public class QQService extends BaseService {
	
	private static Logger logger = Logger.getLogger(QQService.class);

	private static final String appId = "100288580";
	
	private String serverUrl;
	
	private String unionServerUrl;
	
	public String verifyUser(String uuid, String sessionId) {
		HttpURLConnection url_con = null;
		try {
			String param = "?access_token=" + sessionId + "&oauth_consumer_key=" + appId + "&openid=" + uuid + "&format=json"; 			
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
            if (jsonObj.get("ret") != null && jsonObj.get("ret").getAsInt() == 0) {
            	return TagCodeEnum.SUCCESS;
            } else {
            	logger.error("qq服务端验证用户失败, respose:" + jsonObj.toString());
            }
            rd.close();
            in.close();
		} catch (Exception e) {
			logger.error("qq服务端验证用户请求异常", e);
		} finally {
            if (url_con != null) url_con.disconnect();
        }
		return null;
	}
	
    public String getUnionID(String sessionId) {
        HttpURLConnection url_con = null;
        try {
            String param = "?access_token=" + sessionId + "&unionid=1";          
            URL url = new URL(unionServerUrl + param);           
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
            JsonObject jsonObj = new JsonParser().parse(tempStr.toString().substring(tempStr.indexOf("{"), tempStr.lastIndexOf("}") + 1)).getAsJsonObject();
            if (jsonObj.get("unionid") != null) {
                return jsonObj.get("unionid").getAsString();
            } else {
                logger.error("qq服务端获取unionID失败, respose:" + jsonObj.toString());
            }
            rd.close();
            in.close();
        } catch (Exception e) {
            logger.error("qq服务端获取unionID请求异常", e);
        } finally {
            if (url_con != null) url_con.disconnect();
        }
        return null;
    }

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}
    
    public void setUnionServerUrl(String unionServerUrl) {
        this.unionServerUrl = unionServerUrl;
    }
	
}

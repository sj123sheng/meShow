package com.melot.kktv.third.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.kktv.third.BaseService;
import com.melot.kktv.util.TagCodeEnum;

public class QunduiService extends BaseService {
	
	private static Logger logger = Logger.getLogger(QunduiService.class);
	
	private String serverUrl;
	
	public String verifyUser(String uuid, String sessionID) {
		
		HttpURLConnection url_con = null;
		String qunduiUrl = serverUrl + "?sessionID=" + sessionID;
		try {
            URL url = new URL(qunduiUrl);
            url_con = (HttpURLConnection) url.openConnection();
            url_con.setRequestMethod("POST");
            url_con.setConnectTimeout(10000);
            url_con.setReadTimeout(5000);
            url_con.setDoInput(true);
            url_con.setDoOutput(true);
            
            OutputStream outStream = url_con.getOutputStream();
            outStream.flush();
            outStream.close();
            
            InputStream in = url_con.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            StringBuffer tempStr = new StringBuffer();
            String tempLine = rd.readLine();
            while (tempLine != null) {
                tempStr.append(tempLine);
                tempLine = rd.readLine();
            }
            String resData = tempStr.toString();
            try {
            	JsonObject resJson = new JsonParser().parse(resData).getAsJsonObject();
    			int status = resJson.get("code").getAsInt();
    			if(status!=0) {
    				logger.error("群队用户验证失败, Response Data: " + resData);
    				return TagCodeEnum.SUCCESS;
    			}
			} catch (Exception e) {
				logger.error("群队用户验证失败, Response Data: " + resData, e);
				return null;
			}
            rd.close();
            in.close();
        } catch (Exception e) {
        	logger.error("群队用户验证请求异常", e);
        	return null;
        } finally {
            if (url_con != null) url_con.disconnect();
        }
        return TagCodeEnum.SUCCESS;
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}
	
}

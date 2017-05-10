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

public class DidaService extends BaseService {
	
	private static Logger logger = Logger.getLogger(DidaService.class);
	
	private static final String DIDA_SERVICE_URL = "http://api.170ds.com/wlappserv/f/api/user/verifyUserLogin";

	/**
	 * @param requestData
     * channerId   渠道ID                     String
	 * uid         用户ID                     String
	 * tokenID     用户的tokenID              String
	 * @return
	 * 返回结果：
	 * resultCode	响应码(1 验证成功 -1 失败)	Int
	 * 
	 * {
     *  "resultCode":"1"
     * }
	 */
	public String verifyUser(String uid, String tokenID) {
		boolean isValid = false;
		HttpURLConnection url_con = null;
        try {
        	StringBuffer sb = new StringBuffer();
        	sb.append("?id=").append(uid);
        	sb.append("&sessionID=").append(tokenID);
        	
            URL url = new URL(DIDA_SERVICE_URL + sb.toString());
            url_con = (HttpURLConnection) url.openConnection();
            url_con.setRequestMethod("GET");
            url_con.setConnectTimeout(10000);
            url_con.setReadTimeout(5000);
            url_con.setDoInput(true);
            
            InputStream in = url_con.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            StringBuffer resposeStr = new StringBuffer();
            String tempLine = null;
            while ((tempLine = rd.readLine()) != null) {
            	resposeStr.append(tempLine);
            }
            JsonObject jsonObj = new JsonParser().parse(resposeStr.toString()).getAsJsonObject();
            // 1 成功 -1 失败
            int resultCode = Integer.parseInt(jsonObj.get("resultCode").getAsString());
            if(resultCode == 1) {
            	isValid = true;
            } else {
            	logger.error("Dida 服务端验证用户失败, respose:" + resposeStr);
            }
            rd.close();
            in.close();
        } catch (Exception e) {
        	logger.error("Dida 服务端验证用户请求异常", e);
        } finally {
            if (url_con != null) {
                url_con.disconnect();
            }
        }
        
        if (isValid) {
        	return TagCodeEnum.SUCCESS;
        } else {
        	return null;
        }
	}
	
}

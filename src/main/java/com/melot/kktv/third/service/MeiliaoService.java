package com.melot.kktv.third.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import org.apache.log4j.Logger;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.kktv.third.BaseService;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.TagCodeEnum;

public class MeiliaoService extends BaseService {
	
	private static Logger logger = Logger.getLogger(MeiliaoService.class);
	
	public static final String serverUrl = "http://sns.meituliaoliao.com/usr/chk4kk";
	
	/**
	 * @param u 美聊用户唯一标识
	 * @return
	 */
	public String verifyUser(String u, String sessionId) {
		boolean isValid = false;
		HttpURLConnection url_con = null;
		Transaction t = Cat.getProducer().newTransaction("MCall", "MeiliaoService.verifyUser");
        try {
        	String ts = StringUtil.formatDateTime(new Date());
        	String s = CommonUtil.md5(u+ts+u).toLowerCase();
        	String queryParams = "?u=" + u + "&ts=" + ts+ "&s=" + s;
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
            t.setStatus(Transaction.SUCCESS);
            JsonObject jsonObj = new JsonParser().parse(tempStr.toString()).getAsJsonObject();		
			if(jsonObj.has("code")&&jsonObj.get("code").getAsInt() != 0)
				logger.error("美聊服务端验证用户失败, respose:" + jsonObj.toString());
			else
            	isValid = true;
            rd.close();
            in.close();
        } catch (Exception e) {
        	logger.error("美聊服务端验证用户请求异常", e);
        	t.setStatus(e);
        } finally {
            if (url_con != null) {
                url_con.disconnect();
            }
            t.complete();
        }
        if (isValid) {
        	return TagCodeEnum.SUCCESS;
        } else {
        	return null;
        }
	}
	
}

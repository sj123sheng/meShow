package com.melot.kktv.third.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.Logger;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.kktv.third.BaseService;
import com.melot.kktv.util.TagCodeEnum;

/**
 * Title: GoogleService
 * <p>
 * Description: GoogleService
 * </p>
 * 
 * @author 林端端<a href="mailto:duanduan.lin@melot.cn" />
 * @version V1.0
 * @since 2017年8月28日 下午2:44:35
 */
public class GoogleService extends BaseService{
    
    private static Logger logger = Logger.getLogger(GoogleService.class);
    
    private String serverUrl;
    
    public String verifyUser(String uuid, String sessionId) {
        HttpURLConnection url_con = null;
        Transaction t = Cat.getProducer().newTransaction("MCall", "GoogleService.verifyUser");
        try {
            String param = "?id_token=" + sessionId;
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
            JsonObject jsonObj = new JsonParser().parse(tempStr.toString()).getAsJsonObject();
            if (jsonObj.get("sub").getAsString().equals(uuid)) {
                return TagCodeEnum.SUCCESS;
            } else {
                logger.error("google服务端验证用户失败, response:" + jsonObj.toString());
            }
            rd.close();
            in.close();
        } catch (Exception e) {
            logger.error("google服务端验证用户请求异常", e);
            t.setStatus(e);
        } finally {
            if (url_con != null) {
                url_con.disconnect();
            }
            t.complete();
        }
        return null;
    }
    
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }
}


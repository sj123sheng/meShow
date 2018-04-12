package com.melot.kktv.third.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;

import org.apache.log4j.Logger;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.kktv.third.BaseService;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.TagCodeEnum;

/**
 * Title: HaofangService
 * <p>
 * Description: 
 * </p>
 * 
 * @author 林端端<a href="mailto:duanduan.lin@melot.cn">
 * @version V1.0
 * @since 2016年9月6日 上午11:38:01
 */
public class HaofangService extends BaseService{
    
    private static Logger logger = Logger.getLogger(HaofangService.class);
    
    private String serverUrl;
    
    private static final String key = "hf*(%#)##%@%%()(##$^";

    /**
     * @param sessionid 调用接口凭证
     * @param uuid 用户唯一标识
     * @return 
     * 
     */
    @Override
    public String verifyUser(String uuid, String sessionId) {
        HttpURLConnection url_con = null;
        Transaction t = Cat.getProducer().newTransaction("MCall", "HaofangService.verifyUser");
        try {
            int version = 1;
            String appid = "kk";
            long time = new Date().getTime()/1000L;
            String sign = CommonUtil.md5(version + appid + sessionId + time + key).toLowerCase();
            String param = "op=CheckWebUserLogin&version=" + version + "&appid=" + appid + "&timestamp=" + time
                    + "&ticket=" + URLEncoder.encode(sessionId, "utf-8") + "&sign=" + sign;       
            
            URL url = new URL(serverUrl);            
            url_con = (HttpURLConnection) url.openConnection();
            url_con.setRequestMethod("POST");
            url_con.setConnectTimeout(10000);
            url_con.setReadTimeout(5000);
            url_con.setDoInput(true);         
            url_con.setDoOutput(true);
            
            PrintWriter out = new PrintWriter(url_con.getOutputStream());
            out.print(param);
            out.flush();
            
            InputStream in = url_con.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            StringBuffer tempStr = new StringBuffer();
            String tempLine = null;
            while ((tempLine = rd.readLine()) != null) {
                tempStr.append(tempLine);
            }
            t.setStatus(Transaction.SUCCESS);

            JsonObject jsonObj = new JsonParser().parse(tempStr.toString()).getAsJsonObject();
            if (jsonObj.get("Result") != null && jsonObj.get("Result").getAsInt() == 0 && jsonObj.get("Data") != null 
                    && jsonObj.get("Data").getAsJsonObject().get("userid").getAsString().equals(uuid)) {
                return TagCodeEnum.SUCCESS;
            } else {
                logger.error("浩方服务端验证用户失败, response:" + jsonObj.toString());
            }
            
            out.close();
            rd.close();
            in.close();
        } catch (Exception e) {
            logger.error("浩方服务端验证用户请求异常", e);
            t.setStatus(e);
        } finally {
            if (url_con != null) {
                url_con.disconnect();
            }
            t.complete();
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

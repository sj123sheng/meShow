/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2018
 */
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

/**
 * Title: ShenTianService
 * <p>
 * Description: ShenTianService
 * </p>
 * 
 * @author 林端端<a href="mailto:duanduan.lin@melot.cn" />
 * @version V1.0
 * @since 2018年1月17日 下午4:35:13
 */
public class ShenTianService extends BaseService{
    
    private static Logger logger = Logger.getLogger(ShenTianService.class);
    
    private String serverUrl;
    
    private static final String KEY = "f30f0f82dsewleld29dwe0qleperjgpw";
    
    private static final String SECRET = "kke_s13_ld^4ew11e";

    /**
     * @param sessionid 调用接口凭证
     * @param uuid 用户唯一标识
     * @return 
     * 
     */
    @Override
    public String verifyUser(String uuid, String sessionId) {
        HttpURLConnection url_con = null;
        Transaction t = Cat.getProducer().newTransaction("MCall", "ShenTianService.verifyUser");
        try {
            long time = new Date().getTime();
            String params = "app_id=" + KEY + "&timestamp=" + time + "&token=" + sessionId + "&uid=" + uuid;
            String sign = CommonUtil.md5(params + SECRET).toLowerCase();
            URL url = new URL(serverUrl + "?" + params + "&sign=" + sign);            
            url_con = (HttpURLConnection) url.openConnection();
            url_con.setRequestMethod("GET");
            url_con.setConnectTimeout(10000);
            url_con.setReadTimeout(5000);
            url_con.setDoInput(true);
            
            InputStream in = url_con.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            StringBuilder tempStr = new StringBuilder();
            String tempLine = null;
            while ((tempLine = rd.readLine()) != null) {
                tempStr.append(tempLine);
            }   
            t.setStatus(Transaction.SUCCESS);
            JsonObject jsonObj = new JsonParser().parse(tempStr.toString()).getAsJsonObject();
            if (jsonObj.get("result") != null && !StringUtil.strIsNull(jsonObj.get("result").getAsJsonObject().get("data").getAsString())) {
                JsonObject dataJson = new JsonParser().parse(jsonObj.get("result").getAsJsonObject().get("data").getAsString()).getAsJsonObject();
                if (uuid.equals(dataJson.get("uid").getAsString())) {
                    return TagCodeEnum.SUCCESS;
                }
            } else {
                logger.error("盛天服务端验证用户失败, response:" + jsonObj.toString());
            }
            
            rd.close();
            in.close();
        } catch (Exception e) {
            logger.error("盛天服务端验证用户请求异常", e);
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

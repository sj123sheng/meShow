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
import java.io.PrintWriter;
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
import com.melot.kktv.util.TagCodeEnum;

/**
 * Title: TvHongbaoService
 * <p>
 * Description: TvHongbaoService
 * </p>
 * 
 * @author 林端端<a href="mailto:duanduan.lin@melot.cn" />
 * @version V1.0
 * @since 2018年1月17日 下午4:35:13
 */
public class TvHongbaoService extends BaseService{
    
    private static Logger logger = Logger.getLogger(TvHongbaoService.class);
    
    private String serverUrl;
    
    private static final String KEY = "topdabd1f237df5628aa11bf7bf9a683";
    
    private static final String SECRET = "a3dab829bbbe7d7159acfcff59961508e48f327700af3c1b071212b1a99d3c99";
    
    private static final String NONCE_KEY = "tvHb_%s";

    /**
     * @param sessionid 调用接口凭证
     * @param uuid 用户唯一标识
     * @return 
     * 
     */
    @Override
    public String verifyUser(String uuid, String sessionId) {
        HttpURLConnection url_con = null;
        Transaction t = Cat.getProducer().newTransaction("MCall", "TvHongbaoService.verifyUser");
        try {
            long time = new Date().getTime()/1000L;
            String nonce = String.format(NONCE_KEY, time);
            String sign = CommonUtil.md5(KEY + sessionId + uuid + nonce + time + SECRET).toLowerCase();
            JsonObject jObj = new JsonObject();
            jObj.addProperty("key", KEY);
            jObj.addProperty("tvmid", uuid);
            jObj.addProperty("token", sessionId);
            jObj.addProperty("nonce", nonce);
            jObj.addProperty("timestamp", time);
            jObj.addProperty("sign", sign);
            
            URL url = new URL(serverUrl + "/public/user/CheckToken");            
            url_con = (HttpURLConnection) url.openConnection();
            url_con.setRequestMethod("POST");
            url_con.setConnectTimeout(10000);
            url_con.setReadTimeout(5000);
            url_con.setDoInput(true);         
            url_con.setDoOutput(true);
            
            PrintWriter out = new PrintWriter(url_con.getOutputStream());
            out.print(jObj);
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
            if (jsonObj.get("status").getAsInt() == 0) {
                return TagCodeEnum.SUCCESS;
            } else {
                logger.error("电视红包服务端验证用户失败, response:" + jsonObj.toString());
            }
            
            out.close();
            rd.close();
            in.close();
        } catch (Exception e) {
            logger.error("电视红包服务端验证用户请求异常", e);
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

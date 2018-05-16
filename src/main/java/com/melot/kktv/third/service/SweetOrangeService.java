package com.melot.kktv.third.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.kktv.third.BaseService;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.DateUtil;
import com.melot.kktv.util.TagCodeEnum;

public class SweetOrangeService extends BaseService {
	
	private static Logger logger = Logger.getLogger(SweetOrangeService.class);
	
	public static final String SERVER_URL = "http://tc.tcl.com/SweetOrangeInvoker/userValidated";
	
	// 固定值，甜橙为kk唱响提供唯一token
	public static String ACCESS_TOKEN = "0140cfc9177d5764b15ff9ae950a3a0c";
	
	// 请求头:tcSysId = 1446773082462 (必填项，为固定值)
	public static String TC_SYS_ID = "1446773082462";
	
	// 请求头:method = post (必填项，为固定值)
	public static String REQUEST_METHOD = "POST";
	
	/**
	 * @param userToken 甜橙每次调用详情为合作方传入的用户唯一token
	 * @param userId 甜橙为合作方分配系统ID
	 * @return true/false
	 * 
	 */
	public String verifyUser(String userId, String userToken) {
		boolean isValid = false;
		Transaction t = Cat.getProducer().newTransaction("MCall", "SweetOrangeService.verifyUser");
		CloseableHttpResponse response = null;
		try(CloseableHttpClient httpClient =HttpClients.createDefault();) {
			String approve = CommonUtil.md5(userId + userToken + ACCESS_TOKEN);
			String param = "{\"approve\":\"" + approve + "\",\"userId\":\"" + userId + "\",\"userToken\":\"" + userToken + "\"}";
			
        	HttpPost post = new HttpPost(SERVER_URL);
            String nowdate = DateUtil.formatDateTime(new Date(), "yyyy-MM-dd HH:mm:ss");
            // 设置通用的请求属性,post请求头
            post.addHeader("reqTime", nowdate);
            post.addHeader("method", REQUEST_METHOD);
            /**
             * 对外ip
             * 202.107.192.59
             * 202.107.192.50
             * 115.238.54.43
             * 115.238.54.34
             */
            String userFromIP = "202.107.192.59";
            String sign = TC_SYS_ID + ACCESS_TOKEN + nowdate + REQUEST_METHOD + userFromIP + param;
            sign = CommonUtil.md5(sign);
            post.addHeader("userFromIP",userFromIP);
            post.addHeader("tcSysId", TC_SYS_ID);
            post.addHeader("sign", sign);
            post.addHeader("ACCESS_TOKE", ACCESS_TOKEN);
            
            // body体传输的参数
			StringEntity entity = new StringEntity(param, "UTF-8");
			entity.setContentType("application/json");

			post.setEntity(entity);

			response = httpClient.execute(post);
			// 返回的结果
			HttpEntity httpEntity = response.getEntity();
			InputStream input = httpEntity.getContent();
			
			StringBuilder out = new StringBuilder();
			InputStreamReader inread = new InputStreamReader(input, "UTF-8");
			
			char[] b = new char[4096];
			for (int n; (n = inread.read(b)) != -1;) {
				out.append(new String(b, 0, n));
			}
			
			 t.setStatus(Transaction.SUCCESS);
			JsonObject jsonObj = new JsonParser().parse(out.toString()).getAsJsonObject();
			if (jsonObj.has("result") && jsonObj.get("result").getAsString().equals("OK")) {
				isValid = true;
			} else {
				logger.info("甜橙服务端验证用户失败，response " + jsonObj.toString());
			}
			
			inread.close();
			input.close();
			EntityUtils.consume(httpEntity);
        } catch (Exception e) {
        	logger.error("甜橙服务端验证用户请求异常", e);
        	t.setStatus(e);
        } finally {
            t.complete();
            if(response != null){
            	try {
					response.close();
				} catch (IOException e) {
					logger.error("甜橙服务端验证用户请求异常", e);
				}
            }
        }
		if (isValid) {
        	return TagCodeEnum.SUCCESS;
        } else {
        	return null;
        }
	}
	
}

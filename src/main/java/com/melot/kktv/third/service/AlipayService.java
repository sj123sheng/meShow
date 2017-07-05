package com.melot.kktv.third.service;

import java.net.URLEncoder;

import org.apache.log4j.Logger;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.request.AlipayUserUserinfoShareRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import com.alipay.api.response.AlipayUserUserinfoShareResponse;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.kktv.third.BaseService;

public class AlipayService extends BaseService {
	
	private static Logger logger = Logger.getLogger(AlipayService.class);
	
	// 支付宝网关地址
	public static final String serverUrl = "https://openapi.alipay.com/gateway.do";
	// 字符集格式
	public static final String charset = "utf-8";
	// 返回结果格式：xml、json;
	public static final String format = "json";
	// 应用 ID
	public static final String appId = "2015011500025631";
	// 合作身份者ID，以2088开头由16位纯数字组成的字符串
	public static String partner = "2088801680006641";
	// 商户的私钥
	public static String privateKey = "MIIBVQIBADANBgkqhkiG9w0BAQEFAASCAT8wggE7AgEAAkEAspsDjVbJgmClTpHbF7Zpcd5czYjdHdX0iFq41xOdsPlb50HAjJRpn4d3+O056ckujvOywHTGK7dpxJH3iPhYsQIDAQABAkB0HjQBABuimBLhMhKvJ/kF/vt25TRc4W1J+TGTIz2ygFWfK0Bcdc3WqQSQiyccsUl40s+RABBGn5PGSNU8+KABAiEA416DZYqvKsXzi5XI6xIk3zwBKgcB2X9U99apKIvC5OECIQDJGI1UtmUKGDb+BKVFeGumpb1SlD98cmrwNjigCq4d0QIhAL3toIM88xAe5U+vonNC58wWWr6ZteDSbc7N4OY+wWHhAiBSADUBai7hYuZpxrMuFOCw37zHw7Pvpyh8M1/7wt8lQQIhAMfL+lAVnvPM55TVt9+mL3yoJ4+bJALCGMAW0iKhGOXt";
	// 支付宝的公钥，无需修改该值
	public static String ali_public_key = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQEB/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";
	
	/**
	 * 验证客户端auth token
	 * {
		    "alipay_system_oauth_token_response": {
		        "access_token": "publicpBa869cad0990e4e17a57ecf7c5469a4b2",
		        "alipay_user_id": "CogmJcgdBQvCr1jdSWXmMul3JwoaXmLBrbkSty7ivP0SaOemwnUkRdOm5AITm38701",
		        "expires_in": 300,
		        "re_expires_in": 300,
		        "refresh_token": "publicpB0ff17e364f0743c79b0b0d7f55e20bfc"
		    },
		    "sign": "xDffQVBBelDiY/FdJi4/a2iQV1I7TgKDFf/9BUCe6+l1UB55YDOdlCAir8CGlTfa0zLYdX0UaYAa43zY2jLhCTDG+d6EjhCBWsNY74yTdiM95kTNsREgAt4PkOkpsbyZVXdLIShxLFAqI49GIv82J3YtzBcVDDdDeqFcUhfasII="
		}
	 * @param authCode
	 * @return
	 */
	private static String getToken(String authCode) {
		
		// 请求对象
		AlipaySystemOauthTokenRequest req = new AlipaySystemOauthTokenRequest();
		req.setCode(authCode);
		// GrantType 传固定值 authorization_code
		req.setGrantType("authorization_code");

		AlipayClient client = new DefaultAlipayClient(serverUrl, appId, privateKey, format, charset);
		// 返回结果对象
		AlipaySystemOauthTokenResponse res;
		try {
			res = client.execute(req);
			if (res != null && res.getBody() != null) {
				return res.getBody();
			}
		} catch (AlipayApiException e) {
			logger.error("fail to call alipay server to get token", e);
		}

		return null;
	}

	/**
	 * 获取支付宝用户信息
	 * {
		    "alipay_user_userinfo_share_response": {
		        "user_type_value": "2",
		        "is_licence_auth": "F",
		        "is_certified": "T",
		        "is_student_certified": "F",
		        "is_bank_auth": "T",
		        "is_mobile_auth": "T",
		        "user_id": "V19gzIiYIxB3UUI53HYzhrLwvmB2gn7Hq+gli13ARAKCHzUwVlGM0Rw-ahGYBcj401",
		        "user_status": "T",
		        "is_id_auth": "T"
		    },
		    "sign": "Jbht57w3YSNpQMGUoxXYapvDNoZlpLvlg19sobMjJ3XJB8Wfg9vH/9jS7DaPfP6hk6IgSGRPC0xCCZnwnFrmVjWTkQ3LqXuTlYzymonfb+FMLssCMmQSA6vFa5tn3WBHBxYCMLqd/nvG7v96Bbs0/q5b7zZzrmVgZquE21g8et8="
		}
	 * @param accessToken
	 * @return
	 */
	private static String getInfo(String accessToken) {
		
		// 请求对象
		AlipayUserUserinfoShareRequest req = new AlipayUserUserinfoShareRequest();
		req.setProdCode("WAP_FAST_LOGIN");

		AlipayClient client = new DefaultAlipayClient(serverUrl, appId, privateKey, format, charset);
		// 返回结果对象
		AlipayUserUserinfoShareResponse res;
		try {
			res = client.execute(req, accessToken);
			if (res != null && res.getBody() != null) {
				return res.getBody();
			}
		} catch (AlipayApiException e) {
			logger.error("fail to call alipay server to get user info", e);
		}

		return null;
	}
	
	@Override
	public String verifyUser(String openId, String sessionId) {
		
		String tokenString = getToken(sessionId);
		if (tokenString != null) {
			String accessToken = null;
			String alipayUserId = null;
			try {
				JsonObject tokenJson = new JsonParser().parse(tokenString).getAsJsonObject()
						.get("alipay_system_oauth_token_response").getAsJsonObject();
				accessToken = tokenJson.get("access_token").getAsString();
				alipayUserId = tokenJson.get("alipay_user_id").getAsString();
				alipayUserId = URLEncoder.encode(alipayUserId, "UTF-8");
			} catch (Exception e) {
				logger.error("fail to parse string to jsonobject, tokenString " + tokenString);
			}
			if (accessToken != null && alipayUserId.equals(openId)) {
				String infoString = getInfo(accessToken);
				if (infoString != null) {
					try {
						JsonObject infoJson = new JsonParser().parse(infoString).getAsJsonObject()
								.get("alipay_user_userinfo_share_response").getAsJsonObject();
						return infoJson.toString();
					} catch (Exception e) {
						logger.error("fail to parse string to jsonobject, infoString " + infoString);
					}
				}
			}
		}
		
		return null;
	}
	
}

package com.unionpay.upmp.sdk.service;

import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import com.melot.kktv.util.ConfigHelper;
import com.unionpay.upmp.sdk.util.HttpUtil;
import com.unionpay.upmp.sdk.util.UpmpCore;

/**
 * 类名：接口处理核心类 功能：组转报文请求，发送报文，解析应答报文 版本：1.0 日期：2012-10-11 作者：中国银联UPMP团队 版权：中国银联 说明：以下代码只是为了方便商户测试而提供的样例代码，商户可以根据自己的需要，按照技术文档编写,并非一定要使用该代码。该代码仅供参考。
 * */
public class UpmpService {

	private static Logger logger = Logger.getLogger(UpmpService.class);

	/**
	 * 银联支付订单推送请求
	 * 
	 * @param amount
	 * @return
	 */
	public static String callUpTradeUrl(String amount, String orderNumber, String orderTime) {
		// 请求要素
		Map<String, String> req = new HashMap<String, String>();
		req.put("backEndUrl", ConfigHelper.getUpBackendUrl());// 通知URL
		req.put("charset", "UTF-8");// 字符编码
		req.put("transType", "01");// 交易类型
		req.put("orderCurrency", "156");// 交易币种
		req.put("version", ConfigHelper.getUpVersion());// 协议版本
		req.put("merId", ConfigHelper.getUpMerchantId());// 商户代码
		req.put("orderAmount", amount);// 订单金额
		req.put("orderDescription", ConfigHelper.getUpOrderDescription());// 订单描述
		req.put("orderNumber", orderNumber);// 订单号
		req.put("orderTime", orderTime);// 交易时间
		// req.put("frontEndUrl", UpmpConfig.MER_FRONT_END_URL);// 前台通知URL
//		req.put("merReserved", "");// 商户保留域
		// req.put("orderTimeout", "20141201100000");// 订单超时时间
		//req.put("sysReseverd", "");// 系统保留域

		Map<String, String> resp = new HashMap<String, String>();
		boolean success = UpmpService.trade(req, resp);
		// 商户的业务逻辑
		if (success && resp.containsKey("tn")) {
			return resp.get("tn");
		} else {
			// 错误应答码
			String respCode = resp.get("respCode");
			// 错误提示信息
			String msg = resp.get("respMsg");
			logger.error("银联支付订单推送请求异常[" + "respCode:" + respCode + "]:" + msg);
		}
		return null;
	}

	/**
	 * 银联支付交易信息查询
	 * 
	 * @param amount
	 * @return
	 */
	public static boolean callUpQueryUrl(String orderNumber) {
		// 请求要素
		Map<String, String> req = new HashMap<String, String>();
		req.put("charset", "UTF-8");// 字符编码
		req.put("transType", "01");// 交易类型
		req.put("version", ConfigHelper.getUpVersion());// 协议版本
		req.put("merId", ConfigHelper.getUpMerchantId());// 商户代码
		req.put("orderNumber", orderNumber);// 订单号
		req.put("orderTime", orderNumber.substring(0, orderNumber.length() - 6));// 交易时间
		// req.put("merReserved", "");// 商户保留域
		// req.put("sysReseverd", "");// 系统保留域

		Map<String, String> resp = new HashMap<String, String>();
		boolean success = UpmpService.query(req, resp);
		// 商户的业务逻辑
		if (success) {
			return success;
		} else {
			// 错误应答码
			String respCode = resp.get("respCode");
			// 错误提示信息
			String msg = resp.get("respMsg");
			logger.error("银联支付交易信息查询异常[" + "respCode:" + respCode + "]:" + msg);
		}
		return false;
	}

	/**
	 * 交易接口处理
	 * 
	 * @param req 请求要素
	 * @param resp 应答要素
	 * @return 是否成功
	 */
	public static boolean trade(Map<String, String> req, Map<String, String> resp) {
		String nvp = buildReq(req, resp);
		String respString = HttpUtil.post(ConfigHelper.getUpTradeUrl(), nvp);
		return verifyResponse(respString, resp);
	}

	/**
	 * 交易查询处理
	 * 
	 * @param req 请求要素
	 * @param resp 应答要素
	 * @return 是否成功
	 */
	public static boolean query(Map<String, String> req, Map<String, String> resp) {
		String nvp = buildReq(req, resp);
		String respString = HttpUtil.post(ConfigHelper.getUpQueryUrl(), nvp);
		return verifyResponse(respString, resp);
	}

	/**
	 * 拼接保留域
	 * 
	 * @param req 请求要素
	 * @return 保留域
	 */
	public static String buildReserved(Map<String, String> req) {
		StringBuilder merReserved = new StringBuilder();
		merReserved.append("{");
		merReserved.append(UpmpCore.createLinkString(req, false, false));
		merReserved.append("}");
		return merReserved.toString();
	}

	/**
	 * 拼接请求字符串
	 * 
	 * @param req 请求要素
	 * @param resp 应答要素
	 * @return 请求字符串
	 */
	public static String buildReq(Map<String, String> req, Map<String, String> resp) {
		// 生成签名结果
		String signature = UpmpCore.buildSignature(req);

		// 签名结果与签名方式加入请求提交参数组中
		req.put("signature", signature);
		req.put("signMethod", "MD5");

		return UpmpCore.createLinkString(req, false, true);
	}

	/**
	 * 异步通知消息验证
	 * 
	 * @param para 异步通知消息
	 * @return 验证结果
	 */
	public static boolean verifySignature(Map<String, String> para) {

		String signature = UpmpCore.buildSignature(para);
		String respSignature = para.get("signature");
		if (null != respSignature && respSignature.equals(signature)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 应答解析
	 * 
	 * @param respString 应答报文
	 * @param resp 应答要素
	 * @return 应答是否成功
	 */
	private static boolean verifyResponse(String respString, Map<String, String> resp) {
		if (respString != null && !"".equals(respString)) {
			// 请求要素
			Map<String, String> para;
			try {
				para = UpmpCore.parseQString(respString);
			} catch (Exception e) {
				return false;
			}
			boolean signIsValid = verifySignature(para);

			if (signIsValid) {
				String respCode = para.get("respCode");
				if (ConfigHelper.getUpResponseCodeSuccess().equals(respCode)) {
					resp.putAll(para);
					return true;
				}
				resp.put("respCode", respCode);
				resp.put("respMsg", para.get("respMsg"));
			} else {
				return false;
			}

		}
		return false;
	}
}

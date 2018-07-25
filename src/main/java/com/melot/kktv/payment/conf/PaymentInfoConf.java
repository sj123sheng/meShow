package com.melot.kktv.payment.conf;

import java.util.List;

import org.apache.log4j.Logger;

import com.melot.kk.module.report.util.CommonStateCode;
import com.melot.kk.recharge.api.dto.ConfPaymentInfoDto;
import com.melot.kk.recharge.api.service.RechargeService;
import com.melot.kktv.base.Result;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * 充值类型配置类
 * @author Administrator
 *
 */
public class PaymentInfoConf {

	private static Logger logger = Logger.getLogger(PaymentInfoConf.class);
	
	/**
	 * 获取充值类型列表
	 * @param appId
	 * @return
	 */
	public static List<ConfPaymentInfoDto> getPaymentList(int appId, int version) {
	    List<ConfPaymentInfoDto> result = null;
	    try {
	        RechargeService rechargeService = (RechargeService) MelotBeanFactory.getBean("rechargeService");
	        Result<List<ConfPaymentInfoDto>> resp = rechargeService.getConfPaymentInfos(appId, version, null);
	        if (resp != null && CommonStateCode.SUCCESS.equals(resp.getCode())) {
	            result = resp.getData();
	        }
	    } catch (Exception e) {
	        logger.error("fail to query payment info config", e);
	    }
		
		return result;
	}
	
}
package com.melot.kktv.payment.conf;

import java.util.List;

import org.apache.log4j.Logger;

import com.melot.module.packagegift.driver.domain.ConfPaymentGrade;
import com.melot.module.packagegift.driver.service.PackageInfoService;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * 充值等级配置类
 * @author Administrator
 *
 */
public class PaymentGradeConf {

	private static Logger logger = Logger.getLogger(PaymentGradeConf.class);
	
	/**
	 * 获取充值等级列表
	 * @param appId
	 * @return
	 */
	public static List<ConfPaymentGrade> getGradeList(int appId) {
		List<ConfPaymentGrade> data = null;
		try {
		    PackageInfoService packageInfoService = (PackageInfoService) MelotBeanFactory.getBean("packageInfoService");
		    data = packageInfoService.getGradeList(appId);
		} catch (Exception e) {
		    logger.error("packageInfoService.getGradeList execute fail, appId: " + appId, e);
		}
		
		return data;
	}
	
}
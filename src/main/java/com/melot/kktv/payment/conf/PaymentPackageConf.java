package com.melot.kktv.payment.conf;

import java.util.List;

import org.apache.log4j.Logger;

import com.melot.module.packagegift.driver.domain.ConfPaymentPackage;
import com.melot.module.packagegift.driver.service.PackageInfoService;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * 充值礼包配置类
 * @author Administrator
 *
 */
public class PaymentPackageConf {

	private static Logger logger = Logger.getLogger(PaymentPackageConf.class);
	
	/**
	 * 获取充值礼包列表
	 * @param appId
	 * @return
	 */
	public static List<ConfPaymentPackage> getPackageList(int appId) {
		
		List<ConfPaymentPackage> data = null;
		
		try {
		    PackageInfoService packageInfoService = (PackageInfoService) MelotBeanFactory.getBean("packageInfoService");
		    data = packageInfoService.getConfPackgeList(appId);
		} catch (Exception e) {
		    logger.error("packageInfoService.getConfPackgeList execute fail, appId" + appId, e);
		}
		
		return data;
	}
	
}
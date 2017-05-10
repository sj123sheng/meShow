package com.melot.kktv.payment.conf;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.util.CollectionUtils;

import com.melot.kktv.payment.domain.ConfPaymentPackage;
import com.melot.kktv.payment.domain.PaymentPackageGift;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;
import com.melot.module.packagegift.driver.domain.PackageDetail;
import com.melot.module.packagegift.driver.domain.PackageInfo;
import com.melot.module.packagegift.driver.domain.PagePackageDetail;
import com.melot.module.packagegift.driver.service.PackageDetailService;
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
	@SuppressWarnings("unchecked")
	public static List<ConfPaymentPackage> getPackageList(int appId) {
		
		List<ConfPaymentPackage> data = null;
		
		try {
		    data = (List<ConfPaymentPackage>)  SqlMapClientHelper.getInstance(DB.BACKUP)
		            .queryForList("Payment.getConfPackgeList", appId);
		} catch (SQLException e) {
		    logger.error("fail to query payment package config", e);
		}
		if (!CollectionUtils.isEmpty(data)) {
		    PackageInfoService packageInfoService = (PackageInfoService) MelotBeanFactory.getBean("packageInfoService");
		    PackageDetailService packageDetailService = (PackageDetailService) MelotBeanFactory.getBean("packageDetailService");
		    
		    for (ConfPaymentPackage confPaymentPackage : data) {
		        PackageInfo packageInfo = packageInfoService.getPackageInfoById(confPaymentPackage.getPackageId());
		        if (packageInfo != null) {
		            confPaymentPackage.setPackageNotice(packageInfo.getPackageNotice());
		            confPaymentPackage.setPackageWorth(packageInfo.getPackageWorth());
		            
		            PagePackageDetail page = packageDetailService.getPackageDetail(
		                    confPaymentPackage.getPackageId(), 0, null, 0, 10);
		            if (page != null && page.getDatas() != null) {
		                
		                List<PaymentPackageGift> packageGiftList = new ArrayList<PaymentPackageGift>();
		                
		                List<PackageDetail> detailList = page.getDatas();
		                for (PackageDetail detail : detailList) {
		                    PaymentPackageGift packageGift = new PaymentPackageGift();
		                    packageGift.setGiftId(detail.getGiftId());
		                    packageGift.setGiftName(detail.getGiftName());
		                    packageGift.setGiftIcon(detail.getGiftIcon());
		                    packageGift.setGiftType(detail.getGiftType());
		                    packageGift.setGiftNotice(detail.getGiftNotice());
		                    packageGift.setGiftCount(detail.getGiftCount());
		                    packageGiftList.add(packageGift);
		                }
		                
		                confPaymentPackage.setPackageGiftList(packageGiftList);
		            }
		        }
		    }
		}
		
		return data;
	}
	
}
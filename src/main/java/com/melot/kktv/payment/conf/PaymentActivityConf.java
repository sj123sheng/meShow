package com.melot.kktv.payment.conf;

import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import com.melot.kktv.payment.domain.ConfPaymentActivity;
import com.melot.kktv.util.db.SqlMapClientHelper;

/**
 * 充值活动配置类
 * @author Administrator
 *
 */
public class PaymentActivityConf {

	private static Logger logger = Logger.getLogger(PaymentActivityConf.class);
	
	/**
	 * 获取充值活动列表
	 * @param appId
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<ConfPaymentActivity> getActivityList(int appId) {
		
		List<ConfPaymentActivity> data = null;
		
		try {
		    data = (List<ConfPaymentActivity>)  SqlMapClientHelper.getInstance(com.melot.kktv.util.DBEnum.KKCX_PG)
		            .queryForList("Activity.getConfActivityList", appId);
		} catch (SQLException e) {
		    logger.error("fail to query payment activity config", e);
		}
		
		return data;
	}
	
}

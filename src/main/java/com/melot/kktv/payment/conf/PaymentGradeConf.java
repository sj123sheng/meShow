package com.melot.kktv.payment.conf;

import java.sql.SQLException;
import java.util.List;

import org.apache.log4j.Logger;

import com.melot.kktv.payment.domain.ConfPaymentGrade;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;

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
	@SuppressWarnings("unchecked")
	public static List<ConfPaymentGrade> getGradeList(int appId) {
		
		List<ConfPaymentGrade> data = null;
		
		try {
		    data = (List<ConfPaymentGrade>)  SqlMapClientHelper.getInstance(DB.BACKUP)
		            .queryForList("Payment.getConfGradeList", appId);
		} catch (SQLException e) {
		    logger.error("fail to query payment grade config", e);
		}
		
		return data;
	}
	
}
package com.melot.kktv.payment.conf;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.melot.kktv.payment.domain.ConfPaymentInfo;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;

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
	@SuppressWarnings("unchecked")
	public static List<ConfPaymentInfo> getPaymentList(int appId, int version) {
		
		List<ConfPaymentInfo> data = null;
		
		try {
		    Map<String, Object> map = new HashMap<String, Object>();
		    map.put("appId", appId);
		    map.put("version", version);
		    data = (List<ConfPaymentInfo>)  SqlMapClientHelper.getInstance(DB.BACKUP)
		            .queryForList("Payment.getConfPaymentList", map);
		} catch (SQLException e) {
		    logger.error("fail to query payment info config", e);
		}
		
		return data;
	}
	
}
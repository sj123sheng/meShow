package com.melot.kktv.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;

public class ConsumeService {
	
	private static Logger logger = Logger.getLogger(ConsumeService.class);
	
    /**
     * 更新举报消费记录
     */
    public static boolean insertReportHistory(int reportId, int userId, int amount, int status) {
    	try {
    		Map<String, Object> map = new HashMap<String, Object>();
    		map.put("reportId", reportId);
    		map.put("userId", userId);
    		map.put("amount", amount);
    		map.put("status", status);
    		SqlMapClientHelper.getInstance(DB.MASTER).insert("User.insertReportHistory", map);
    		return true;
    	} catch (Exception e) {
    		logger.error("fail to execute sql User.insertReportHistory", e);
    		return false;
    	}
    }
}

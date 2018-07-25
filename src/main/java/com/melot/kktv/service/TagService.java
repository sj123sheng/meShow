package com.melot.kktv.service;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ibatis.sqlmap.client.SqlMapSession;
import com.melot.kkcx.service.GeneralService;
import com.melot.kktv.domain.TagInfo;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;
import com.melot.kktv.redis.HotDataSource;

public class TagService {
	
	private static Logger logger = Logger.getLogger(TagService.class);

	/**
	 * 读取用户标签
	 * @param userId 用户ID
	 * @return 标签字符串拼接起来的值
	 */
	public static String getUserTags(int userId) {
		String tagString = null;
    	try {
    		tagString =  (String) SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Tag.getUserTags", userId);
    	} catch(Exception e) {
    		logger.error("Fail to execute getUserTag sql", e);
    	}
		return tagString;
	}
	
}

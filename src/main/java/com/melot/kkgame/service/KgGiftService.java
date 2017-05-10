/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.service;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.melot.kkgame.domain.GiftInfo;
import com.melot.kktv.util.DBEnum;
import com.melot.kktv.util.db.SqlMapClientHelper;

/**
 * Title: KgGiftService
 * <p>
 * Description: TODO
 * </p>
 * @author  姚国平<a href="mailto:guoping.yao@melot.cn">
 * @version V1.0
 * @since 2015-12-30 上午11:11:09 
 */
public class KgGiftService {
    
    
    private static Logger logger = Logger.getLogger(KgGiftService.class);
    
    /**
     *  根据时间查询当周配置的周星 
     */
    @SuppressWarnings("unchecked")
    public static List<GiftInfo> getWeekStarGiftInfoByTime(Date date){
        List<GiftInfo>list  = null;
        try {
            list = SqlMapClientHelper.getInstance(DBEnum.KKGAME_PG).queryForList("User.getWeekStarGiftInfoByTime", date);
        } catch (SQLException e) {
            logger.error("Fail to execute getUserActorInfoByUserId sql, userId " + date);
        }
        return list;
    }
    
}

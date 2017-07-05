package com.melot.kktv.service;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.melot.kktv.redis.HotDataSource;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;

/**
 * 类说明：数据统计服务
 * <p>作者：宋建明<a href="mailto:jianming.song@melot.cn"></p>
 * <p>创建日期：2014-9-12</p>
 * <p>版本：V1.0</p>
 * <p>修改历史：</p>
 */
public class StatisticsServices {
    
    private static Logger logger = Logger.getLogger(StatisticsServices.class);
    
    /**
     * 获取指定礼物指定时间区间内送礼总秀币值
     * @param giftId 礼物ID
     * @param startTime 开始时间，不能为空
     * @param endTime 结束时间
     * @return 送礼总秀币值
     */
    public static long getConsumGiftPool(int giftId, Date startTime, Date endTime) {
        if (startTime == null) {
            startTime = new Date();
        }
        
        String value = HotDataSource.getHotFieldValue("StatisticsServices.getConsumGiftPool", String.valueOf(giftId));
        if (!StringUtil.strIsNull(value)) {
            return StringUtil.parseFromStr(value, 0l);
        }
        
        Map<String, Object> map = new HashMap<String, Object>();
        
        map.put("giftId", giftId);
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        
        try {
            Long result = (Long) SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Profile.getConsumGiftPool", map);
            if (result != null) {
                HotDataSource.setHotFieldValue("StatisticsServices.getConsumGiftPool", String.valueOf(giftId), String.valueOf(result), 5 * 30);
                return result;
            }
        } catch (SQLException e) {
            logger.error("StatisticsServices.getConsumGiftPool(" + giftId + ", " + startTime + ", " + endTime + ") exception", e);
        }
        return 0;
    }
}

package com.melot.kkcx.service;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.melot.kktv.model.WeekStarGift;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;

/**
 * Title: ActorGiftService
 * <p>
 * Description: 
 * </p>
 * 
 * @author 林端端<a href="mailto:duanduan.lin@melot.cn">
 * @version V1.0
 * @since 2016年8月30日 下午18:19:42
 */
public class ActorGiftService {
    
    private static Logger logger = Logger.getLogger(ActorGiftService.class);
    
    @SuppressWarnings("unchecked")
    public static List<WeekStarGift> getWeekStarGiftList(Date weekTime) {
        List<WeekStarGift> weekStarGiftList = null;
        try {
            weekStarGiftList = (List<WeekStarGift>) SqlMapClientHelper.getInstance(DB.MASTER).queryForList("ActorGift.getWeekStarGiftList", weekTime);
        } catch (Exception e) {
            logger.error("getWeekStarGiftList: ", e);
        }
        return weekStarGiftList;
    }

}

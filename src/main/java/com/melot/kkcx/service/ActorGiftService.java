package com.melot.kkcx.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.melot.kktv.model.ActorGift;
import com.melot.kktv.model.WeekStarGift;
import com.melot.kktv.util.TagCodeEnum;
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
    
    /**
     * 获取主播个性礼物
     * @param userId 主播id
     * @return 
     */
    public static JsonArray getActorPersonalizedGiftList(int userId) {
        JsonArray result = new JsonArray();
        
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put("actorId", userId);
        try {
            SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("ActorGift.getActorGiftList", map);
        } catch (SQLException e) {
            logger.error("获取主播个性礼物失败(actorId:" + userId, e);
            return result;
        }
        String TagCode = (String) map.get("TagCode");
        if (TagCode.equals(TagCodeEnum.SUCCESS)) {
            @SuppressWarnings("unchecked")
            List <ActorGift> actorGiftList = (List <ActorGift>) map.get("actorGiftList");
            ArrayList<Integer> giftList = new ArrayList<Integer>();
            for (ActorGift actorGift : actorGiftList) {
                giftList.add(actorGift.getGiftId());
            }
            result = new Gson().toJsonTree(giftList).getAsJsonArray();
            return result;
        } 
        logger.error("获取主播个性礼物失败(actorId:" + userId);
        return result;
    }
    
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

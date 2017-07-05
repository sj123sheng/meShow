package com.melot.kktv.lottery;

import org.apache.log4j.Logger;

import com.melot.kktv.lottery.arithmetic.LotteryArithmeticCache;
import com.melot.kktv.lottery.schedule.UserLotteryPrizeInDB;
import com.melot.kktv.redis.HotDataSource;
import com.melot.kktv.util.StringUtil;
import com.melot.module.packagegift.driver.service.TicketService;
import com.melot.module.packagegift.util.GiftPackageEnum;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * Title: LovesAt2015
 * <p>
 * Description: 
 * </p>
 * 
 * @author 宋建明<a href="mailto:jianming.song@melot.cn">
 * @version V1.0
 * @since 2015-12-21 下午5:38:14
 */
public class LovesAt2016 implements LotteryInterface {
    
    private static final long serialVersionUID = 7530994943261925044L;

    private Logger log = Logger.getLogger(LovesAt2016.class);
    
    public final static String LOVES_K_SEND_COUNT = "Loves.K.Send.Count";
    public final static String LOVES_HAS_K = "Loves.Has.K";
    
    public final static String LOVES_BANNER_SEND_COUNT = "Loves.Banner.Send.Count";
    public final static String LOVES_HAS_BANNER = "Loves.Has.Banner";

    public final static String LOVES_REPLACE_COUNT = "Loves.Replace.Count.Cache";
    
    /* (non-Javadoc)
     * @see com.melot.kktv.lottery.LotteryInterface#calUserRemain(java.lang.String, int, java.lang.String, long)
     */
    @Override
    public int calUserRemain(String lotteryId, int userId, String userPhone, long count) {
        return 0;
    }

    /* (non-Javadoc)
     * @see com.melot.kktv.lottery.LotteryInterface#getPrize(java.lang.String, int, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public boolean getPrize(String lotteryId, int userId, String userPhone,
            String prizeId, String prizeName, int prizeType, int prizeValue) {
        boolean result = false;
        
        lotteryId = "情人节抽奖";
        
        switch (prizeType) {
        case 0: // 2-1000秀币，3-1000K豆，4-喇叭券一张，5-谢谢参与，6-大K宝一个，7-Banner推荐1小时
            switch (StringUtil.parseFromStr(prizeId, 0)) {
            case 4:
                // 喇叭券发放
                TicketService ticketService = (TicketService) MelotBeanFactory.getBean("ticketService");
                if (ticketService.insertSendTicket(userId, 100001, GiftPackageEnum.TICKET_SEND, 1, "Lottery activity[" + lotteryId + "] award prize", 0)) {
                    result = true;
                } else {
                    log.error("LovesAt2016.insertUseTicket(" + userId + ", " + 100001 + ", " + GiftPackageEnum.TICKET_SEND + ", " + 1 + ", " + "\"Lottery activity[" + lotteryId + "] award prize\") 赠送喇叭券失败！！！！！！！！！");
                    return false;
                }
                break;
                
            case 6:
                if (HotDataSource.hasTempData(LOVES_HAS_K, userId + "") || HotDataSource.incTempDataString(LOVES_K_SEND_COUNT, 1, 10 * 24 * 3600) > 20) {
                    // 中大K宝超过20个用情人节专属座驾替代
                    prizeId = "1361";
                    prizeName = "情人节专属座驾一辆（有效期1天）";
                    prizeType = 3;
                    prizeValue = 1;
                    
                    HotDataSource.incHotData(LOVES_REPLACE_COUNT, new String[]{userId + ""}, new int[]{1}, 10 * 24 * 3600);
                } else {
                    HotDataSource.setTempData(LOVES_HAS_K, userId + "", System.currentTimeMillis() / 1000 + 10 * 24 * 3600);
                    result = true;
                }
                break;
                
            case 7:
                if (HotDataSource.hasTempData(LOVES_HAS_BANNER, userId + "") || HotDataSource.incTempDataString(LOVES_BANNER_SEND_COUNT, 1, 10 * 24 * 3600) > 3) {
                    // 中过Banner或中Banner数超过3个用情人节专属座驾替代
                    prizeId = "1361";
                    prizeName = "情人节专属座驾一辆（有效期1天）";
                    prizeType = 3;
                    prizeValue = 1;
                    
                    HotDataSource.incHotData(LOVES_REPLACE_COUNT, new String[]{userId + ""}, new int[]{1}, 10 * 24 * 3600);
                } else {
                    HotDataSource.setTempData(LOVES_HAS_BANNER, userId + "", System.currentTimeMillis() / 1000 + 10 * 24 * 3600);
                    result = true;
                }
                break;

            default:
                result = true;
                break;
            }
            break;
            
        case 3: // 座驾
            prizeId = "1361";
            break;

        default:
            return false;
        }
        
        if (prizeType == 3) {
            result = LotteryArithmeticCache.awardPrize(userId, prizeType, Integer.valueOf(prizeId), prizeValue, prizeName, lotteryId);
        }
        if (result) {
            // 中奖明细入 Oracle 表
            String prizeEntry = "{\"prizeName\":\"" + prizeName + "\", \"value\":" + prizeValue + "}";
            UserLotteryPrizeInDB.add(String.valueOf(userId), userPhone, lotteryId, prizeId, prizeEntry);
        }
        
        return result;
    }

    /* (non-Javadoc)
     * @see com.melot.kktv.lottery.LotteryInterface#receivePrize(java.lang.String, int, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public boolean receivePrize(String lotteryId, int userId, String userPhone,
            String prizeId, String prizeName, int prizeType, int prizeValue) {
        return false;
    }

}

package com.melot.kktv.lottery;

import org.apache.log4j.Logger;

import com.melot.kktv.lottery.arithmetic.LotteryArithmeticCache;
import com.melot.kktv.lottery.schedule.UserLotteryPrizeInDB;
import com.melot.kktv.redis.HotDataSource;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.StringUtil;
import com.melot.module.ModuleService;
import com.melot.module.packagegift.driver.service.TicketService;
import com.melot.module.packagegift.util.GiftPackageEnum;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * Title: ChristmasAt2015
 * <p>
 * Description: 
 * </p>
 * 
 * @author 宋建明<a href="mailto:jianming.song@melot.cn">
 * @version V1.0
 * @since 2015-12-21 下午5:38:14
 */
public class ChristmasAt2015 implements LotteryInterface {
    
    private static final long serialVersionUID = 7530994943261925944L;

    private Logger log = Logger.getLogger(ChristmasAt2015.class);
    
    public final static String CHRISTMAS_LABA_SEND_COUNT = "Christmas.Laba.Send.Count";

    public final static String CHRISTMAS_GAME_REPLACE_COUNT = "Christmas.Game.Replace.Count.Cache";
    
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
        
        lotteryId = "圣诞节抽奖";
        
        switch (prizeType) {
        case 0: // 喇叭券
            if ((AppIdEnum.GAME + "").equals(userPhone.trim())) {
                // 直播中喇叭券用5个圣诞铃铛代替
                prizeId = "40000586";
                prizeName = "圣诞铃铛5个";
                prizeType = 1;
                prizeValue = 5;
                
                HotDataSource.incHotData(CHRISTMAS_GAME_REPLACE_COUNT, new String[]{userId + ""}, new int[]{1}, 10 * 24 * 3600);
                break;
            }
            
            if (HotDataSource.incTempDataString(CHRISTMAS_LABA_SEND_COUNT, 1, 10 * 24 * 3600) > 60) {
                // 唱响中喇叭券中奖最多 60 次，超过用5个苹果代替
                prizeId = "40000589";
                prizeName = "苹果5个";
                prizeType = 1;
                prizeValue = 5;
                
                HotDataSource.incHotData(CHRISTMAS_GAME_REPLACE_COUNT, new String[]{userId + ""}, new int[]{1}, 10 * 24 * 3600);
                break;
            }
            
            // 喇叭券发放
            TicketService ticketService = (TicketService) MelotBeanFactory.getBean("ticketService");
            if (ticketService.insertSendTicket(userId, 100001, GiftPackageEnum.TICKET_SEND, 1, "Lottery activity[" + lotteryId + "] award prize", 0)) {
                // 中奖明细入 Oracle 表
                String prizeEntry = "{\"prizeName\":\"" + prizeName + "\", \"value\":" + prizeValue + "}";
                UserLotteryPrizeInDB.add(String.valueOf(userId), userPhone, lotteryId, "100001", prizeEntry);
                
                return true;
            } else {
                log.error("ChristmasAt2015.insertUseTicket(" + userId + ", " + 100001 + ", " + GiftPackageEnum.TICKET_SEND + ", " + 1 + ", " + "\"Lottery activity[" + lotteryId + "] award prize\") 赠送喇叭券失败！！！！！！！！！");
                return false;
            }
            
        case 1: // 礼物
            switch (StringUtil.parseFromStr(prizeId, 0)) {
            case 1: // 苹果
            case 2: // 苹果
                if ((AppIdEnum.GAME + "").equals(userPhone.trim())) { // 直播
                    prizeId = "40000585";
                } else {
                    prizeId = "40000589";
                }
                break;
                
            case 3: // 圣诞铃铛
                if ((AppIdEnum.GAME + "").equals(userPhone.trim())) { // 直播
                    prizeId = "40000586";
                } else {
                    prizeId = "40000590";
                }
                break;
                
            case 4: // 圣诞袜
                if ((AppIdEnum.GAME + "").equals(userPhone.trim())) { // 直播
                    prizeId = "40000587";
                } else {
                    prizeId = "40000591";
                }
                break;

            default:
                return false;
            }
            break;
            
        case 3: // 座驾
            if ((AppIdEnum.GAME + "").equals(userPhone.trim())) {
                // 直播中座驾用5个苹果代替
                prizeId = "40000585";
                prizeName = "苹果5个";
                prizeType = 1;
                prizeValue = 5;
                
                HotDataSource.incHotData(CHRISTMAS_GAME_REPLACE_COUNT, new String[]{userId + ""}, new int[]{1}, 10 * 24 * 3600);
                break;
            }
            
            prizeId = "1080";
            break;

        default:
            return false;
        }
        
        if ((AppIdEnum.GAME + "").equals(userPhone.trim())) {
            com.melot.storehouse.service.StorehouseService storehouseService = (com.melot.storehouse.service.StorehouseService) ModuleService.getService("StorehouseService");
            if (storehouseService != null) {
                com.melot.storehouse.domain.RespMsg respMsg = storehouseService.addUserStorehouse(Integer.valueOf(prizeId), userId, prizeValue, 7, "Lottery activity[" + lotteryId + "] award prize");
                if (respMsg != null && respMsg.getRespCode() == 0) {
                    result = true;
                }
            }
        } else {
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

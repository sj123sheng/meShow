package com.melot.kktv.lottery;

import java.util.List;

import org.apache.log4j.Logger;

import com.melot.asset.driver.service.AssetService;
import com.melot.kkcx.service.StorehouseService;
import com.melot.kktv.domain.StorehouseInfo;
import com.melot.kktv.lottery.arithmetic.LotteryArithmeticCache;
import com.melot.kktv.lottery.schedule.UserLotteryPrizeInDB;
import com.melot.kktv.redis.HotDataSource;
import com.melot.kktv.util.DateUtil;
import com.melot.kktv.util.StringUtil;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * 类说明：
 * <p>作者：宋建明<a href="mailto:jianming.song@melot.cn"></p>
 * <p>创建日期：2014-12-25</p>
 * <p>版本：V1.0</p>
 * <p>修改历史：</p>
 */
public class KkThree implements LotteryInterface {
    
    private static final long serialVersionUID = 7530994943261925943L;

    private Logger log = Logger.getLogger(KkThree.class);
    
    public final static String KKTHREE_HAS_GOODS_CACHE = "KkThree.HasGoods.Cache";
    public final static String KKTHREE_HAS_TICKET_CACHE = "KkThree.HasTicket.Cache";
    
    public final static String KKTHREE_IPHONE_SEND_COUNT = "KkThree.iPhone.Send.Count";
    public final static String KKTHREE_KBAO_SEND_COUNT = "KkThree.KBao.Send.Count";

    public final static String KKTHREE_REPEAT_COUNT_CACHE = "KkThree.Repeat.Count.Cache";
    
    /* (non-Javadoc)
     * @see com.melot.kktv.lottery.LotteryInterface#calUserRemain(java.lang.String, int, java.lang.String, long)
     */
    @Override
    public int calUserRemain(String lotteryId, int userId, String userPhone, long count) {
        /*
         * KK三周年庆典 礼物ID 礼物名称
         */
        final int redGiftId = 40000511;
        final int violetGiftId = 40000513;
        final int pinkGiftId = 40000512;
        
        final String redGiftName = "红色糖果", violetGiftName = "紫色糖果", pinkGiftName = "粉色糖果";
        
        int redGiftCount = 0, violetGiftCount = 0, pinkGiftCount = 0;
        
        List<StorehouseInfo> list = StorehouseService.getUserGiftCount(userId, redGiftId + "," + violetGiftId + "," + pinkGiftId);
        if (list != null && list.size() > 0) {
            for (StorehouseInfo storehouseInfo : list) {
                switch (storehouseInfo.getGiftId().intValue()) {
                case redGiftId:
                    redGiftCount = storehouseInfo.getQuantity();
                    break;
                    
                case violetGiftId:
                    violetGiftCount = storehouseInfo.getQuantity();
                    break;
                    
                case pinkGiftId:
                    pinkGiftCount = storehouseInfo.getQuantity();
                    break;
                    
                default:
                    break;
                }
            }
        }
        
        if (count > 0 && redGiftCount >= count && violetGiftCount >= count && pinkGiftCount >= count) {
            String giftDesc = "KK三周年庆典(抽奖)";
            if (StorehouseService.addUserGift(userId, redGiftId, (int) -count, redGiftName, 7, giftDesc)) {
                if (StorehouseService.addUserGift(userId, violetGiftId, (int) -count, violetGiftName, 7, giftDesc)) {
                    if (StorehouseService.addUserGift(userId, pinkGiftId, (int) -count, pinkGiftName, 7, giftDesc)) {
                        redGiftCount = (int) (redGiftCount - count);
                        violetGiftCount = (int) (violetGiftCount - count);
                        pinkGiftCount = (int) (pinkGiftCount - count);
                    } else {
                        StorehouseService.addUserGift(userId, redGiftId, (int) count, redGiftName, 7, giftDesc);
                        StorehouseService.addUserGift(userId, violetGiftId, (int) count, violetGiftName, 7, giftDesc);

                        log.error("KkThree.calUserRemain(" + lotteryId + ", " + userId + ", " + userPhone + ", " + count + ") 扣除粉色糖果失败！！！！！！！！！");
                    }
                } else {
                    StorehouseService.addUserGift(userId, redGiftId, (int) count, redGiftName, 7, giftDesc);
                    log.error("KkThree.calUserRemain(" + lotteryId + ", " + userId + ", " + userPhone + ", " + count + ") 扣除紫色糖果失败！！！！！！！！！");
                }
            } else {
                log.error("KkThree.calUserRemain(" + lotteryId + ", " + userId + ", " + userPhone + ", " + count + ") 扣除红色糖果失败！！！！！！！！！");
            }
        }
        
        return Math.min(Math.min(redGiftCount, violetGiftCount), pinkGiftCount);
    }

    /* (non-Javadoc)
     * @see com.melot.kktv.lottery.LotteryInterface#getPrize(java.lang.String, int, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public boolean getPrize(String lotteryId, int userId, String userPhone,
            String prizeId, String prizeName, int prizeType, int prizeValue) {
        boolean result = false;
        
        lotteryId = "KK三周年庆典";
        
        switch (prizeType) {
        case 0: // prizeId: 1-iPhone 2-大K宝 3-靓号券
            final long expireTime = DateUtil.parseDateStringToLong("2015-10-01", "yyyy-MM-dd") / 1000;
            
            boolean isRepeat = false;
            switch (StringUtil.parseFromStr(prizeId, 0)) {
            case 1:
                if (HotDataSource.hasTempData(KKTHREE_HAS_GOODS_CACHE, String.valueOf(userId))
                        || HotDataSource.incTempDataString(KKTHREE_IPHONE_SEND_COUNT, 1, 20 * 24 * 3600) > 1) {
                    isRepeat = true;
                    HotDataSource.incHotData(KKTHREE_REPEAT_COUNT_CACHE, new String[]{userId + ""}, new int[]{1}, 20 * 24 * 3600);
                    
                    log.error("User [" + userId + "] has repeat lottery iPhone !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                } else {
                    HotDataSource.setTempData(KKTHREE_HAS_GOODS_CACHE, String.valueOf(userId), expireTime);
                }
                break;
                
            case 2:
                if (HotDataSource.hasTempData(KKTHREE_HAS_GOODS_CACHE, String.valueOf(userId))
                        || HotDataSource.incTempDataString(KKTHREE_KBAO_SEND_COUNT, 1, 20 * 24 * 3600) > 99) {
                    isRepeat = true;
                    HotDataSource.incHotData(KKTHREE_REPEAT_COUNT_CACHE, new String[]{userId + ""}, new int[]{1}, 20 * 24 * 3600);
                    
                    log.error("User [" + userId + "] has repeat lottery big k bao !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                } else {
                    HotDataSource.setTempData(KKTHREE_HAS_GOODS_CACHE, String.valueOf(userId), expireTime);
                }
                break;
                
            case 3:
                if (HotDataSource.hasTempData(KKTHREE_HAS_TICKET_CACHE, String.valueOf(userId))) {
                    isRepeat = true;
                    HotDataSource.incHotData(KKTHREE_REPEAT_COUNT_CACHE, new String[]{userId + ""}, new int[]{1}, 20 * 24 * 3600);
                    
                    log.error("User [" + userId + "] has repeat lottery luckyid ticket !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                } else {
                    AssetService assetService = (AssetService) MelotBeanFactory.getBean("assetService");
                    if (assetService == null) {
                        return false;
                    }
                    
                    if (!assetService.addTicket(userId, 1, 0, 0, 0, 7, lotteryId)) {
                        return false;
                    }
                    
                    HotDataSource.setTempData(KKTHREE_HAS_TICKET_CACHE, String.valueOf(userId), expireTime);
                }
                break;

            default:
                return false;
            }
            
            if (isRepeat) {
                prizeId = "40000251";
                prizeName = "3个KK生日蛋糕";
                prizeType = 1;
                prizeValue = 3;
            }
            
            break;
            
        case 1: // 礼物
            break;
            
        case 2: // VIP
            prizeId = "100001";
            break;
            
        case 3: // 座驾
            break;

        default:
            return false;
        }
        
        result = LotteryArithmeticCache.awardPrize(userId, prizeType, Integer.valueOf(prizeId), prizeValue, prizeName, lotteryId);
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

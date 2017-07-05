/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2016
 */
package com.melot.kktv.lottery;

import java.util.Date;

import com.melot.kkcore.user.api.ShowMoneyHistory;
import com.melot.kktv.lottery.schedule.UserLotteryPrizeInDB;
import com.melot.kktv.service.UserService;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.module.packagegift.driver.domain.InsertCarMap;
import com.melot.module.packagegift.driver.domain.ResVip;
import com.melot.module.packagegift.driver.service.CarService;
import com.melot.module.packagegift.driver.service.VipService;
import com.melot.sdk.core.util.MelotBeanFactory;
import com.melot.sunshine.service.SunshineService;

/**
 * Title: WeeklyCheckIn
 * <p>
 * Description: 
 * </p>
 * 
 * @author 林端端<a href="mailto:duanduan.lin@melot.cn">
 * @version V1.0
 * @since 2016年12月30日 上午9:35:34
 */
public class WeeklyCheckIn implements LotteryInterface {

    private static final long serialVersionUID = 8374211988696182429L;
    
    /* (non-Javadoc)
     * @see com.melot.kktv.lottery.LotteryInterface#calUserRemain(java.lang.String, int, java.lang.String, long)
     */
    @Override
    public int calUserRemain(String lotteryId, int userId, String userPhone,
            long count) {
        return 1;
    }

    /* (non-Javadoc)
     * @see com.melot.kktv.lottery.LotteryInterface#getPrize(java.lang.String, int, java.lang.String, java.lang.String, java.lang.String, int, int)
     */
    @Override
    public boolean getPrize(String lotteryId, int userId, String userPhone,
            String prizeId, String prizeName, int prizeType, int prizeValue) {
        boolean result = false;
        
        lotteryId = "7日签到抽奖";
        
        switch (prizeType) {
        case 0: // 5-10W秀币，6-1W秀币，7-1000秀币，8-200秀币，9-阳光*30
            switch (StringUtil.parseFromStr(prizeId, 0)) {
            case 5:
                // 10W秀币
                ShowMoneyHistory showMoney10wHistory = new ShowMoneyHistory();
                showMoney10wHistory.setCount(1);
                showMoney10wHistory.setDtime(new Date());
                showMoney10wHistory.setIncomeAmount(prizeValue);
                showMoney10wHistory.setToUserId(userId);
                showMoney10wHistory.setType(7);
                showMoney10wHistory.setProductDesc("7日签到抽奖");
                result = UserService.incUserShowMoneyV2(userId, prizeValue, false, showMoney10wHistory);
                break;
                
            case 6:
                //1W秀币
                ShowMoneyHistory showMoney1wHistory = new ShowMoneyHistory();
                showMoney1wHistory.setCount(1);
                showMoney1wHistory.setDtime(new Date());
                showMoney1wHistory.setIncomeAmount(prizeValue);
                showMoney1wHistory.setToUserId(userId);
                showMoney1wHistory.setType(7);
                showMoney1wHistory.setProductDesc("7日签到抽奖");
                result = UserService.incUserShowMoneyV2(userId, prizeValue, false, showMoney1wHistory);
                break;
                
            case 7:
                //1k秀币
                ShowMoneyHistory showMoney1kHistory = new ShowMoneyHistory();
                showMoney1kHistory.setCount(1);
                showMoney1kHistory.setDtime(new Date());
                showMoney1kHistory.setIncomeAmount(prizeValue);
                showMoney1kHistory.setToUserId(userId);
                showMoney1kHistory.setType(7);
                showMoney1kHistory.setProductDesc("7日签到抽奖");
                result = UserService.incUserShowMoneyV2(userId, prizeValue, false, showMoney1kHistory);
                break;
                
            case 8:
                //200秀币
                ShowMoneyHistory showMoneyHistory = new ShowMoneyHistory();
                showMoneyHistory.setCount(1);
                showMoneyHistory.setDtime(new Date());
                showMoneyHistory.setIncomeAmount(prizeValue);
                showMoneyHistory.setToUserId(userId);
                showMoneyHistory.setType(7);
                showMoneyHistory.setProductDesc("7日签到抽奖");
                result = UserService.incUserShowMoneyV2(userId, prizeValue, false, showMoneyHistory);
                break;
                  
            case 9:
                //阳光*30
                SunshineService sunshineService = (SunshineService) MelotBeanFactory.getBean("sunshineService");
                if (TagCodeEnum.SUCCESS.equals(sunshineService.sendUserSunShine(userId, prizeValue, 3))){
                    result = true;
                }    
                break;

            default:
                result = false;
                break;
            }
            break;
            
        case 2: //VIP
            switch (StringUtil.parseFromStr(prizeId, 0)) {
            case 100004:
                VipService svipService = (VipService) MelotBeanFactory.getBean("vipService");
                ResVip sresVip = svipService.insertSendVipV2(userId, prizeValue, 100004, 7, "7日签到抽奖赠送3天SVIP", 0);
                if (sresVip != null && (sresVip.getRespCode() == VipService.AssetModule_TAG_CODE_SUCCESS ||
                    sresVip.getRespCode() == VipService.SendVipHandler_RESP_CODE_LIFE_TIME_FOREVER)) {
                    result = true;
                }
                break;
            
            case 100001:
                VipService vipService = (VipService) MelotBeanFactory.getBean("vipService");
                ResVip resVip = vipService.insertSendVipV2(userId, prizeValue, 100001, 7, "7日签到抽奖赠送3天VIP", 0);
                if (resVip != null && (resVip.getRespCode() == VipService.AssetModule_TAG_CODE_SUCCESS ||
                    resVip.getRespCode() == VipService.SendVipHandler_RESP_CODE_LIFE_TIME_FOREVER)) {
                    result = true;
                }
                break;
                
            default:
                break;
            }
            break;
            
        case 3: // 座驾
            CarService carService = (CarService) MelotBeanFactory.getBean("carService");
            InsertCarMap insertCarMap = carService.insertSendCar(userId, Integer.parseInt(prizeId), prizeValue, 7, "7日签到抽奖赠送座驾");
            if (insertCarMap != null && insertCarMap.getEndTime() > 0) {
                result = true;
            }
            break;

        default:
            return false;
        }
        
        if (result) {
            // 中奖明细入 Oracle 表
            String prizeEntry = "{\"prizeName\":\"" + prizeName + "\", \"value\":" + prizeValue + "}";
            UserLotteryPrizeInDB.add(String.valueOf(userId), userPhone, lotteryId, prizeId, prizeEntry);
        }
        
        return result;
    }

    /* (non-Javadoc)
     * @see com.melot.kktv.lottery.LotteryInterface#receivePrize(java.lang.String, int, java.lang.String, java.lang.String, java.lang.String, int, int)
     */
    @Override
    public boolean receivePrize(String lotteryId, int userId, String userPhone,
            String prizeId, String prizeName, int prizeType, int prizeValue) {
        return false;
    }

}

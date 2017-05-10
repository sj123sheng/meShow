package com.melot.kkcx.model;

import java.util.Map;

/**
 * Title: LotteryPrizeList
 * <p>
 * Description: 
 * </p>
 * 
 * @author 林端端<a href="mailto:duanduan.lin@melot.cn">
 * @version V1.0
 * @since 2017年1月12日 下午3:59:45
 */
public class LotteryPrizeList {

    private Map<Integer, LotteryPrize> list;

    public Map<Integer, LotteryPrize> getList() {
        return list;
    }

    public void setList(Map<Integer, LotteryPrize> list) {
        this.list = list;
    }

}

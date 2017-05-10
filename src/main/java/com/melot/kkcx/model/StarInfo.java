package com.melot.kkcx.model;

/**
 * Title: StarInfo
 * <p>
 * Description: 
 * </p>
 * 
 * @author 林端端<a href="mailto:duanduan.lin@melot.cn">
 * @version V1.0
 * @since 2017年3月15日 下午2:34:42
 */
public class StarInfo {
    
    private int level = 0;
    
    private long minValue = 0L;
    
    private long maxVale = 0L;;
    
    private long userConsume = 0L;

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public long getMinValue() {
        return minValue;
    }

    public void setMinValue(long minValue) {
        this.minValue = minValue;
    }

    public long getMaxVale() {
        return maxVale;
    }

    public void setMaxVale(long maxVale) {
        this.maxVale = maxVale;
    }

    public long getUserConsume() {
        return userConsume;
    }

    public void setUserConsume(long userConsume) {
        this.userConsume = userConsume;
    }
    
}

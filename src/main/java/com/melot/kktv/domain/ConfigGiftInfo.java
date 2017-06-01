package com.melot.kktv.domain;

import java.util.List;

import com.melot.room.gift.domain.GiftIconUrl;
import com.melot.room.gift.domain.GiftInfo;
import com.melot.room.gift.domain.GiftListResourceURL;

/**
 * Title: ConfigGiftInfo
 * <p>
 * Description: 礼物相关的配置信息
 * </p>
 * 
 * @author 魏安稳<a href="mailto:anwen.wei@melot.cn" />
 * @version V1.0
 * @since 2017年5月25日 上午10:31:27
 */
public class ConfigGiftInfo {
    
    /**
     * 配置信息总的版本号
     */
    Long giftVersion;
    
    /**
     * 资源信息配置信息
     */
    GiftListResourceURL giftListResourceURL;
    
    /**
     * 礼物信息
     */
    List<GiftInfo> giftList;
    
    /**
     * 角标信息
     */
    List<GiftIconUrl> iconUrl;

    public Long getGiftVersion() {
        return giftVersion;
    }

    public void setGiftVersion(Long giftVersion) {
        this.giftVersion = giftVersion;
    }

    public GiftListResourceURL getGiftListResourceURL() {
        return giftListResourceURL;
    }

    public void setGiftListResourceURL(GiftListResourceURL giftListResourceURL) {
        this.giftListResourceURL = giftListResourceURL;
    }

    public List<GiftInfo> getGiftList() {
        return giftList;
    }

    public void setGiftList(List<GiftInfo> giftList) {
        this.giftList = giftList;
    }

    public List<GiftIconUrl> getIconUrl() {
        return iconUrl;
    }
    
    public void setIconUrl(List<GiftIconUrl> iconUrl) {
        this.iconUrl = iconUrl;
    }
}

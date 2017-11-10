/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2017
 */
package com.melot.kkcx.functions;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.kkcx.service.UserService;
import com.melot.kktv.base.Result;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.module.packagegift.driver.domain.CatalogGift;
import com.melot.module.packagegift.driver.domain.ShopCatalog;
import com.melot.module.packagegift.driver.domain.UserMultiAsset;
import com.melot.module.packagegift.driver.service.MallService;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * Title: MallFunctions
 * <p>
 * Description: 钻石商城
 * </p>
 * 
 * @author 林端端<a href="mailto:duanduan.lin@melot.cn" />
 * @version V1.0
 * @since 2017年10月30日 下午5:29:20
 */
public class MallFunctions {
    
    private Logger logger = Logger.getLogger(MallFunctions.class);
    
    /**
     * 购买游戏勋章 (51030101)
     * 
     * @param jsonObject 请求对象
     * @param checkTag 是否验证token标记
     * @return 
     */
    public JsonObject buyGameMedal(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        int userId, amount, medalId;
        
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            amount = CommonUtil.getJsonParamInt(jsonObject, "amount", 0, "5103010101", 1, Integer.MAX_VALUE);
            medalId = CommonUtil.getJsonParamInt(jsonObject, "medalId", 0, "5103010102", 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            MallService mallService = (MallService) MelotBeanFactory.getBean("mallService");
            Result<UserMultiAsset> resp = mallService.buyGameMedal(userId, medalId, amount);
            if ("0".equals(resp.getCode())) {
                UserMultiAsset userMultiAsset = resp.getData();
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                result.addProperty("showMoney", userMultiAsset.getShowMoney());
                result.addProperty("gameMoney", userMultiAsset.getGameMoney());
            } else if ("4".equals(resp.getCode())){
                result.addProperty("TagCode", TagCodeEnum.USER_MONEY_SHORTNESS);
            } else {
                result.addProperty("TagCode", "5103010103");
            }
        } catch (Exception e) {
            logger.error("mallService.buyGameMedal(" + userId + ", " + medalId + ", " + amount + ") return exception.", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        
        return result;
    }
    
    /**
     * 获取商城栏目列表 (51030102)
     * 
     * @param jsonObject 请求对象
     * @param checkTag 是否验证token标记
     * @return 
     */
    public JsonObject getGameMallCataList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int cataType;
        
        try {
            cataType = CommonUtil.getJsonParamInt(jsonObject, "cataType", 1, null, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            MallService mallService = (MallService) MelotBeanFactory.getBean("mallService");
            List<ShopCatalog> shopCatalogList = mallService.getShopCatalogList(null, null, null, null, cataType, 1, 50);
            JsonArray cataList = new JsonArray();
            if (shopCatalogList != null) {
                for (ShopCatalog shopCatalog : shopCatalogList) {
                    JsonObject jsonObj = new JsonObject();
                    jsonObj.addProperty("cataId", shopCatalog.getCatalogId());
                    jsonObj.addProperty("cataName", shopCatalog.getCatalogName());
                    if (shopCatalog.getDescription() != null) {
                        jsonObj.addProperty("description", shopCatalog.getDescription());
                    }
                    jsonObj.addProperty("position", shopCatalog.getPosition());
                    if (shopCatalog.getIsDefault() != null) {
                        jsonObj.addProperty("isDefault", shopCatalog.getIsDefault());
                    }
                    cataList.add(jsonObj);
                }
            }
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            result.add("cataList", cataList);
        } catch (Exception e) {
            logger.error("mallService.getShopCatalogList(" + cataType + ") return exception.", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        
        return result;
    }
    
    /**
     * 获取商城栏目礼物列表 (51030103)
     * 
     * @param jsonObject 请求对象
     * @param checkTag 是否验证token标记
     * @return 
     */
    public JsonObject getGameGiftList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        int cataId, pageIndex, countPerPage;
        
        JsonObject result = new JsonObject();
        try {
            cataId = CommonUtil.getJsonParamInt(jsonObject, "cataId", 1, "5103010301", 1, Integer.MAX_VALUE);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 10, null, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            MallService mallService = (MallService) MelotBeanFactory.getBean("mallService");
            JsonArray giftList = new JsonArray();
            int count = mallService.getCatalogGiftListCount(cataId, null);
            if (count > 0) {
                List<CatalogGift> catalogGiftList = mallService.getCatalogGiftList(cataId, null, pageIndex, countPerPage);
                if (catalogGiftList != null) {
                    for (CatalogGift catalogGift : catalogGiftList) {
                        JsonObject jsonObj = new JsonObject();
                        jsonObj.addProperty("giftId", catalogGift.getGiftId());
                        jsonObj.addProperty("giftName", catalogGift.getGiftName());
                        jsonObj.addProperty("conversionPrice", catalogGift.getPrice());
                        jsonObj.addProperty("conversionUnit", catalogGift.getUnit());
                        giftList.add(jsonObj);
                    }
                }
            }
           
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            result.addProperty("count", count);
            result.add("giftList", giftList);
        } catch (Exception e) {
            logger.error("mallService.getCatalogGiftList(" + cataId + "," + pageIndex + ", " + countPerPage + ") return exception.", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        
        return result;
    }
    
    /**
     * 获取当前用户游戏币 (51030104)
     * 
     * @param jsonObject 请求对象
     * @param checkTag 是否验证token标记
     * @return 
     */
    public JsonObject getUserGameMoney(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        int userId;
        
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        result.addProperty("gameMoney", UserService.getUserGameMoney(userId));
        
        return result;
    }
    
    /**
     * 钻石商城 兑换礼物 (51030105)
     * 
     * @param jsonObject 请求对象
     * @param checkTag 是否验证token标记
     * @return 
     */
    public JsonObject exchangeGameGift(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        int userId, giftId, amount;
        
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            giftId = CommonUtil.getJsonParamInt(jsonObject, "giftId", 0, "5103010501", 1, Integer.MAX_VALUE);
            amount = CommonUtil.getJsonParamInt(jsonObject, "amount", 1, null, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            MallService mallService = (MallService) MelotBeanFactory.getBean("mallService");
            Result<UserMultiAsset> resp = mallService.exchangeGameGift(userId, giftId, amount);
            if ("0".equals(resp.getCode())) {
                UserMultiAsset userMultiAsset = resp.getData();
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                result.addProperty("gameMoney", userMultiAsset.getGameMoney());
            } else if ("3".equals(resp.getCode())) {
                result.addProperty("TagCode", "5103010502");
            } else if ("4".equals(resp.getCode())) {
                result.addProperty("TagCode", "5103010503");
            } else {
                result.addProperty("TagCode", "5103010504");
            }
            
        } catch (Exception e) {
            logger.error("mallService.exchangeGameGift(" + userId + "," + giftId + ", " + amount + ") return exception.", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        
        return result;
    }

}

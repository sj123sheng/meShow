/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2017
 */
package com.melot.kkcx.functions;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.melot.kktv.util.ParameterKeys;
import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.goldcoin.domain.GoldcoinHistory;
import com.melot.goldcoin.domain.UserGoldAssets;
import com.melot.goldcoin.service.GoldcoinService;
import com.melot.kkcx.service.UserService;
import com.melot.kktv.base.Result;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.module.medal.driver.service.ActivityMedalService;
import com.melot.module.packagegift.driver.domain.CatalogGift;
import com.melot.module.packagegift.driver.domain.InsertCarMap;
import com.melot.module.packagegift.driver.domain.MallProp;
import com.melot.module.packagegift.driver.domain.ShopCatalog;
import com.melot.module.packagegift.driver.domain.UserMultiAsset;
import com.melot.module.packagegift.driver.service.CarService;
import com.melot.module.packagegift.driver.service.MallService;
import com.melot.sdk.core.util.MelotBeanFactory;
import com.melot.storehouse.domain.RespMsg;
import com.melot.storehouse.service.StorehouseService;

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
        result.addProperty("showMoney", UserService.getUserMoney(userId));
        
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
    
    /**
     * 获取当前用户金币 (51030106)
     * 
     * @param jsonObject 请求对象
     * @param checkTag 是否验证token标记
     * @return 
     */
    public JsonObject getUserGoldCoin(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
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
        result.addProperty("goldCoin", UserService.getUserGoldCoin(userId));
        
        return result;
    }
    
    /**
     * 金币商城 兑换道具 (51030107)
     * 
     * @param jsonObject 请求对象
     * @param checkTag 是否验证token标记
     * @return 
     */
    public JsonObject exchangeCoinProp(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        int userId, propId, propType, amount;
        
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            propId = CommonUtil.getJsonParamInt(jsonObject, "propId", 0, "5103010701", 1, Integer.MAX_VALUE);
            propType = CommonUtil.getJsonParamInt(jsonObject, "propType", 0, "5103010702", 1, 3);
            amount = CommonUtil.getJsonParamInt(jsonObject, "amount", 1, null, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            long singlePrice = 0;
            long requiredCoin = 0;
            long goldCoin = UserService.getUserGoldCoin(userId);
            GoldcoinService goldcoinService = (GoldcoinService) MelotBeanFactory.getBean("goldcoinService");
            if (propType == 1) {
                //兑换礼物
                if (propId == 40001157) {
                    singlePrice = 50;
                } else if (propId == 40001155) {
                    singlePrice = 66;
                } else if (propId == 40001156) {
                    singlePrice = 100;
                } else if (propId == 40001084) {
                    singlePrice = 200;
                } else if (propId == 40001085) {
                    singlePrice = 20000;
                } else if (propId == 40001036) {
                    singlePrice = 100000;
                } else {
                    result.addProperty("TagCode", "5103010703");
                    return result;
                }
            } else if (propType == 2){
                //兑换勋章
                if (propId == 4235) {
                    singlePrice = 1500;
                } else {
                    result.addProperty("TagCode", "5103010703");
                    return result;
                }
            } else {
                //兑换座驾
                if (propId == 1555) {
                    singlePrice = 150000;
                } else {
                    result.addProperty("TagCode", "5103010703");
                    return result;
                }
            }
            
            requiredCoin = singlePrice * amount;
            if (goldCoin >= requiredCoin) {
                GoldcoinHistory goldCoinHistory = new GoldcoinHistory();
                goldCoinHistory.setAppid(AppIdEnum.AMUSEMENT);
                goldCoinHistory.setPlatform(1);
                goldCoinHistory.setNtype(2);
                goldCoinHistory.setUserid(userId);
                goldCoinHistory.setConsAmount((int) requiredCoin);
                goldCoinHistory.setDtime(new Date());
                goldCoinHistory.setTypedesc("金币兑换道具");
                UserGoldAssets userGoldAssets = goldcoinService.decUserGoldAssets(userId, requiredCoin, goldCoinHistory);
                if (userGoldAssets == null) {
                    result.addProperty("TagCode", "5103010704");
                } else {
                    if (propType == 1) {
                        StorehouseService storehouseService = (StorehouseService) MelotBeanFactory.getBean("storehouseService");
                        RespMsg respMsg = storehouseService.addUserStorehouse(propId, userId, amount, 16, "金币兑换礼物");
                        if (respMsg == null || respMsg.getRespCode() != 0) {
                            logger.error("用户" + userId + "金币兑换添加库存礼物[" + propId + "]失败，数量为：" + amount);
                            result.addProperty("TagCode", "5103010705");
                            return result;
                        }
                    } else if (propType == 2) {
                        ActivityMedalService activityMedalService = (ActivityMedalService) MelotBeanFactory.getBean("activityMedalService");
                        if (!activityMedalService.insertOperatorSendActivityMedalNew(String.valueOf(userId), propId, 0, 1, 0, 1, "金币兑换勋章", 0)) {
                            logger.error("用户" + userId + "金币兑换添加勋章[" + propId + "]失败，数量为：" + amount);
                            result.addProperty("TagCode", "5103010705");
                            return result;
                        }
                    } else {
                        CarService carService = (CarService) MelotBeanFactory.getBean("carService");
                        InsertCarMap insertCarMap = carService.insertSendCar(userId, propId, 15, 7, "金币兑换座驾");
                        if (insertCarMap == null || insertCarMap.getEndTime() <= 0) {
                            logger.error("用户userId: " + userId + "金币兑换座驾：carId: " + propId + ",days: 15 失败");
                            result.addProperty("TagCode", "5103010705");
                            return result;
                        }
                    }
                    result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                    result.addProperty("goldCoin", userGoldAssets.getGoldCoin());
                }
            } else {
                result.addProperty("TagCode", "5103010704");
            }
        } catch (Exception e) {
            logger.error("MallFunctions.exchangeCoinProp(userId: " + userId + ", propId: " + propId + ", propType:" + propType + ", amount:" + amount + ") return exception.", e);
            result.addProperty("TagCode", "5103010705");
        }
        
        return result;
    }
    
    /**
     * 获取金币商城道具列表 (51030108)
     * 
     * @param jsonObject 请求对象
     * @param checkTag 是否验证token标记
     * @return 
     */
    public JsonObject getCoinPropList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        int pageIndex, countPerPage;
        
        JsonObject result = new JsonObject();
        try {
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
            JsonArray propList = new JsonArray();
            int count = mallService.getMallPropListCount(null, null, 1);
            if (count > 0) {
                List<MallProp> mallPropList = mallService.getMallPropList(null, null, 1, pageIndex, countPerPage);
                if (mallPropList != null) {
                    for (MallProp mallProp : mallPropList) {
                        JsonObject jsonObj = new JsonObject();
                        jsonObj.addProperty("propId", mallProp.getPropId());
                        jsonObj.addProperty("propType", mallProp.getPropType());
                        jsonObj.addProperty("propName", mallProp.getPropName());
                        jsonObj.addProperty("vaildDay", mallProp.getVaildDay());
                        jsonObj.addProperty("price", mallProp.getPrice());
                        jsonObj.addProperty("defaultAmount", mallProp.getDefaultAmount());
                        if (mallProp.getPosition() != null) {
                            jsonObj.addProperty("position", mallProp.getPosition());
                        }
                        if (mallProp.getIconUrl() != null) {
                            jsonObj.addProperty("iconUrl", mallProp.getIconUrl());
                        }
                        propList.add(jsonObj);
                    }
                }
            }
           
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            result.addProperty("count", count);
            result.add("propList", propList);
        } catch (Exception e) {
            logger.error("mallService.getMallPropList(null, null, " + pageIndex + ", " + countPerPage + ") return exception.", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        
        return result;
    }
    
    /**
     * 兑换道具（金币商城 新版） (51030109)
     * 
     * @param jsonObject 请求对象
     * @param checkTag 是否验证token标记
     * @return 
     */
    public JsonObject exchangeGoldCoinProp(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        int userId, propId, propType, amount;
        
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            propId = CommonUtil.getJsonParamInt(jsonObject, "propId", 0, "5103010901", 1, Integer.MAX_VALUE);
            propType = CommonUtil.getJsonParamInt(jsonObject, "propType", 0, "5103010902", 0, 4);
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
            long resp = mallService.exchangeCoinProp(userId, propId, propType, amount);
            if (resp >= 0) {
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                result.addProperty("goldCoin", resp);
            } else if (resp == -2) {
                result.addProperty("TagCode", "5103010903");
            } else if (resp == -3) {
                result.addProperty("TagCode", "5103010904");
            } else {
                result.addProperty("TagCode", "5103010905");
            }
        } catch (Exception e) {
            logger.error("mallService.exchangeCoinProp(userId: " + userId + ", propId: " + propId + ", propType:" + propType + ", amount:" + amount + ") return exception.", e);
            result.addProperty("TagCode", "5103010905");
        }
        
        return result;
    }

    /**
     * 获取当前用户货币的自动转换状态(51030110)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getUserAutoExchangeState(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;

        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            MallService mallService = (MallService) MelotBeanFactory.getBean("mallService");
            result.addProperty("showMoneyToGameMoney", mallService.getGameMoneyAutoExchangeState(userId));
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("mallService.getGameMoneyAutoExchangeState(userId: " + userId + ") return exception.", e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }

        return result;
    }

    /**
     * 修改当前用户货币的自动转换状态(51030111)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject changeUserAutoExchangeState(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId, showMoneyToGameMoney;

        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            showMoneyToGameMoney = CommonUtil.getJsonParamInt(jsonObject, "showMoneyToGameMoney", 0, null, 0 ,1);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            MallService mallService = (MallService) MelotBeanFactory.getBean("mallService");
            boolean tag = mallService.updateGameMoneyAutoExchangeState(userId, showMoneyToGameMoney);
            if (tag) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            } else {
                result.addProperty(ParameterKeys.TAG_CODE, "5103011101");
            }

        } catch (Exception e) {
            logger.error("mallService.getGameMoneyAutoExchangeState(userId: " + userId + ") return exception.", e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }

        return result;
    }
}

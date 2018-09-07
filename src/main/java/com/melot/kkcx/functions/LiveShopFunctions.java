package com.melot.kkcx.functions;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.melot.asset.driver.domain.ConfVirtualId;
import com.melot.asset.driver.service.VirtualIdService;
import com.melot.kk.liveshop.api.constant.LiveShopErrorMsg;
import com.melot.kk.liveshop.api.constant.LiveShopTransactionType;
import com.melot.kk.liveshop.api.constant.SellerApplyCheckStatusEnum;
import com.melot.kk.liveshop.api.dto.*;
import com.melot.kk.liveshop.api.service.SellerApplyInfoService;
import com.melot.kktv.util.*;
import com.melot.module.api.exceptions.MelotModuleException;
import com.melot.module.packagegift.driver.service.XmanService;
import com.melot.sdk.core.util.MelotBeanFactory;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.kk.liveshop.api.service.LiveShopService;
import com.melot.kk.logistics.api.domain.UserAddressDO;
import com.melot.kk.logistics.api.service.UserAddressService;
import com.melot.kk.recharge.api.dto.ConfPaymentInfoDto;
import com.melot.kk.recharge.api.service.RechargeService;
import com.melot.kkcore.account.service.AccountSecurityService;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kkcx.transform.LiveShopTF;
import com.melot.kktv.base.CommonStateCode;
import com.melot.kktv.base.Page;
import com.melot.kktv.base.Result;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class LiveShopFunctions {

    private static Logger logger = Logger.getLogger(LiveShopFunctions.class);
    
    private static final String PARAM_START = "start";
    private static final String PARAM_ORDER_NO = "orderNo";
    

    @Resource
    LiveShopService liveShopService;
    
    @Resource
    UserAddressService userAddressService;
    
    @Resource
    RechargeService rechargeService;
    
    @Resource
    KkUserService kkUserService;

    @Resource
    private SellerApplyInfoService sellerApplyInfoService;
    
    /**
     * 主播生成订单【51060502】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject actorAddOrder(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }
        int userId;
        int auctionId;
        int resourceId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, "5106050201", 1, Integer.MAX_VALUE);
            auctionId = CommonUtil.getJsonParamInt(jsonObject, "auctionId", 0, "5106050202", 1, Integer.MAX_VALUE);
            resourceId = CommonUtil.getJsonParamInt(jsonObject, "resourceId", 0, "5106050203", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            // 订单渠道只有app
            Result<String> addOrderResult = liveShopService.addAuctionOrder(userId, auctionId, resourceId, 1);
            if (addOrderResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            String code = addOrderResult.getCode();
            if ("2".equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, "5106050204");
                return result;
            } else if ("3".equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, "5106050205");
                return result;
            } else if ("4".equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, "5106050206");
                return result;
            } else if ("5".equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, "5106050207");
                return result;
            } else if ("6".equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, "5106050208");
                return result;
            } else if (!CommonStateCode.SUCCESS.equals(code)){
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            result.addProperty(PARAM_ORDER_NO, addOrderResult.getData());
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：liveShopService.addAuctionOrder(userId=%s, auctionId=%s, resourceId=%s)", userId, auctionId, resourceId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取订单详情【51060503】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getOrderInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }
        int userId;
        String orderNo;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, "5106050301", 1, Integer.MAX_VALUE);
            orderNo = CommonUtil.getJsonParamString(jsonObject, PARAM_ORDER_NO, "", "5106050302", 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            Result<LiveShopOrderDTO> moduleResult = liveShopService.getOrderInfo(orderNo);
            if (moduleResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            String code = moduleResult.getCode();
            if ("2".equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, "5106050303");
                return result;
            } else if (!CommonStateCode.SUCCESS.equals(code) || moduleResult.getData() == null){
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            LiveShopOrderDTO orderDTO = moduleResult.getData();
            // 获取主播客服ID
            List<Integer> subShopIds = Lists.newArrayList();
            try {
                subShopIds = liveShopService.getSubShopIds(orderDTO.getActorId());
            } catch (Exception e) {
                logger.error("liveShopService.getSubShopIds(" + orderDTO.getActorId() + ")", e);
                subShopIds = Lists.newArrayList();
            }
            if (!orderDTO.getActorId().equals(userId)
                    && !orderDTO.getUserId().equals(userId)
                    && (subShopIds.isEmpty() || !subShopIds.contains(userId))) {
                result.addProperty(ParameterKeys.TAG_CODE, "5106050303");
                return result;
            }
            UserAddressDO addressDO = null;
            Result<UserAddressDO> addressResult = userAddressService.getUserDefaultAddressDOByUserId(orderDTO.getUserId());
            if (addressResult != null && CommonStateCode.SUCCESS.equals(addressResult.getCode())) {
                addressDO = addressResult.getData();
            }
            
            LiveShopTF.orderInfo2Json(result, orderDTO, addressDO, subShopIds);
            
            // 支付方式
            if (orderDTO.getPaymentMode() != null && orderDTO.getPaymentMode() > 0) {
                Result<ConfPaymentInfoDto> rechargeResult = rechargeService.getConfPaymentInfoV2(1, orderDTO.getPaymentMode());
                if (rechargeResult != null && CommonStateCode.SUCCESS.equals(rechargeResult.getCode())) {
                    result.addProperty("paymentName", rechargeResult.getData().getPaymentName());
                }
            }
            
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：liveShopService.getOrderInfo(orderNo=%s)", orderNo), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取订单列表【51060504】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getOrders(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }
        
        int userId;
        int state;
        int type;
        int start;
        int num;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, "5106050401", 1, Integer.MAX_VALUE);
            state = CommonUtil.getJsonParamInt(jsonObject, "state", 0, null, 0, Integer.MAX_VALUE);
            type = CommonUtil.getJsonParamInt(jsonObject, "type", 1, null, 0, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, PARAM_START, 0, null, 0, Integer.MAX_VALUE);
            num = CommonUtil.getJsonParamInt(jsonObject, "num", 20, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            Result<Page<LiveShopOrderDTO>> moduleResult = liveShopService.getOrders(state, userId, type, start, num);
            
            if (moduleResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            String code = moduleResult.getCode();
            if (!CommonStateCode.SUCCESS.equals(code) || moduleResult.getData() == null){
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            Page<LiveShopOrderDTO> page = moduleResult.getData();
            result.addProperty("count", page.getCount());
            JsonArray orders = new JsonArray();
            if (page.getList() == null) {
                result.add("orders", orders);
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
                return result;
            }
            
            for (LiveShopOrderDTO liveShopOrderDTO : page.getList()) {
                JsonObject orderDTOJson = new JsonObject();
                LiveShopTF.orderInfo2Json(orderDTOJson, liveShopOrderDTO, null);
                // 移除不必要的字段
                orderDTOJson.remove("refundInfo");
                orderDTOJson.remove("expressInfo");
                List<Integer> subShopIds = new ArrayList<>();
                int relationId;
                UserProfile userProfile;
                // 我是买家，联系人为商家
                if (type == 1) {
                    relationId = liveShopOrderDTO.getActorId();
                    userProfile = kkUserService.getUserProfile(relationId);
                    
                    // 商家的有客服号，这个使用客服号替换联系人ID
                    try {
                        subShopIds = liveShopService.getSubShopIds(liveShopOrderDTO.getActorId());
                    } catch (Exception e) {
                        logger.error("liveShopService.getSubShopIds(" + liveShopOrderDTO.getActorId() + ");", e);
                    }                    
                    JsonArray subShopIdArray = new JsonArray();
                    if (CollectionUtils.isNotEmpty(subShopIds)) {
                        for (Integer subShopId : subShopIds) {
                            subShopIdArray.add(subShopId);
                        }
                        orderDTOJson.add("subShopIds", subShopIdArray);
                        relationId = subShopIds.get(0);
                        orderDTOJson.addProperty("sellerId", relationId);// 覆盖掉sellerId，为安卓做兼容
                    }
                } else {
                    relationId = liveShopOrderDTO.getUserId();
                    userProfile = kkUserService.getUserProfile(relationId);
                }
                orderDTOJson.addProperty("relationId", relationId);
                orderDTOJson.addProperty("nickname", userProfile.getNickName());
                
                // 获取支付
                if (liveShopOrderDTO.getPaymentMode() != null && liveShopOrderDTO.getPaymentMode() > 0) {
                    Result<ConfPaymentInfoDto> rechargeResult = rechargeService.getConfPaymentInfoV2(1, liveShopOrderDTO.getPaymentMode());
                    if (rechargeResult != null && CommonStateCode.SUCCESS.equals(rechargeResult.getCode())) {
                        orderDTOJson.addProperty("paymentName", rechargeResult.getData().getPaymentName());
                    }
                }
                
                orders.add(orderDTOJson);
            }
            
            result.add("orders", orders);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：liveShopService.getOrders(state=%s, userId=%s, type=%s, start=%s, num=%s)", state, userId, type, start, num), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 商家发货【51060505】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject deliveryOrder(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }
        
        int userId;
        String orderNo;
        String waybillNumber;
        String courierCompany;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, "5106050501", 1, Integer.MAX_VALUE);
            orderNo = CommonUtil.getJsonParamString(jsonObject, PARAM_ORDER_NO, null, "5106050502", 1, Integer.MAX_VALUE);
            waybillNumber = CommonUtil.getJsonParamString(jsonObject, "waybillNumber", null, "5106050503", 1, Integer.MAX_VALUE);
            courierCompany = CommonUtil.getJsonParamString(jsonObject, "courierCompany", null, "5106050504", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            Result<Boolean> moduleResult = liveShopService.deliveryOrder(userId, orderNo, waybillNumber, courierCompany);
            if (moduleResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            String code = moduleResult.getCode();
            if ("2".equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, "5106050505");
                return result;
            } else  if ("3".equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, "5106050506");
                return result;
            } else  if ("4".equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, "5106050507");
                return result;
            } else if (!CommonStateCode.SUCCESS.equals(code) || moduleResult.getData() == null){
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：liveShopService.deliveryOrder(userId=%s, orderNo=%s, waybillNumber=%s, courierCompany=%s)", userId, orderNo, waybillNumber, courierCompany), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 用户设置收货地址【51060506】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject applySendOrder(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }
        
        int userId;
        String orderNo;
        int addressId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, "5106050601", 1, Integer.MAX_VALUE);
            orderNo = CommonUtil.getJsonParamString(jsonObject, PARAM_ORDER_NO, null, "5106050602", 1, Integer.MAX_VALUE);
            addressId = CommonUtil.getJsonParamInt(jsonObject, "addressId", 0, "5106050603", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            Result<Boolean> moduleResult = liveShopService.applyDelivery(userId, orderNo, addressId);
            if (moduleResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            String code = moduleResult.getCode();
            if ("2".equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, "5106050604");
                return result;
            } else  if ("3".equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, "5106050605");
                return result;
            } else if (!CommonStateCode.SUCCESS.equals(code) || moduleResult.getData() == null){
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：liveShopService.applyDelivery(userId=%s, orderNo=%s, addressId=%s)", userId, orderNo, addressId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 用户申请退款【51060507】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject applyRefund(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }
        
        int userId;
        String orderNo;
        long refundPrice;
        String refundDesc;
        String resourceIds;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, "5106050701", 1, Integer.MAX_VALUE);
            orderNo = CommonUtil.getJsonParamString(jsonObject, PARAM_ORDER_NO, null, "5106050702", 1, Integer.MAX_VALUE);
            refundPrice = CommonUtil.getJsonParamLong(jsonObject, "refundPrice", 0, "5106050703", 1, Long.MAX_VALUE);
            refundDesc = CommonUtil.getJsonParamString(jsonObject, "refundDesc", null, null, 0, Integer.MAX_VALUE);
            resourceIds = CommonUtil.getJsonParamString(jsonObject, "resourceIds", null, null, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            Result<Boolean> moduleResult = liveShopService.applyRefund(userId, orderNo, refundPrice, refundDesc, resourceIds);
            if (moduleResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            String code = moduleResult.getCode();
            if ("2".equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, "5106050704");
                return result;
            } else  if ("3".equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, "5106050705");
                return result;
            } else if (!CommonStateCode.SUCCESS.equals(code) || moduleResult.getData() == null){
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：liveShopService.applyRefund(userId=%s, orderNo=%s, refundPrice=%s, refundDesc=%s, resourceIds=%s)", userId, orderNo, refundPrice, refundDesc, resourceIds), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 用户确认收货【51060509】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject receiveOrder(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }
        int userId;
        String orderNo;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, "5106050901", 1, Integer.MAX_VALUE);
            orderNo = CommonUtil.getJsonParamString(jsonObject, PARAM_ORDER_NO, null, "5106050902", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            Result<Boolean> moduleResult = liveShopService.receiveOrder(userId, orderNo);
            if (moduleResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            String code = moduleResult.getCode();
            if ("2".equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, "5106050903");
                return result;
            } else  if ("3".equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, "5106050904");
                return result;
            } else if (!CommonStateCode.SUCCESS.equals(code) || moduleResult.getData() == null){
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：receiveOrder(userId=%s, orderNo=%s)", userId, orderNo), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 校验是否支付成功【51060510】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject checkOrderState(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        String adminOrderNo;
        try {
            adminOrderNo = CommonUtil.getJsonParamString(jsonObject, "adminOrderNo", null, "5106051001", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            Result<Boolean> moduleResult = liveShopService.orderIsPaid(adminOrderNo);
            if (moduleResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            String code = moduleResult.getCode();
            if ("2".equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, "5106051002");
                return result;
            } else if (!CommonStateCode.SUCCESS.equals(code) || moduleResult.getData() == null){
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：liveShopService.orderIsPaid(adminOrderNo=%s)", adminOrderNo), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取商品列表【51060511】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getProducts(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int distributorId;
        int start;
        int num;
        try {
            distributorId = CommonUtil.getJsonParamInt(jsonObject, "distributorId", 0, TagCodeEnum.ERROR_DISTRIBUTOR_ID, 1, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, PARAM_START, 0, null, 0, Integer.MAX_VALUE);
            num = CommonUtil.getJsonParamInt(jsonObject, "num", 20, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            Result<Page<LiveShopProductDTO>> moduleResult = liveShopService.getProductsByDistributorId(distributorId, start, num);

            if (moduleResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            String code = moduleResult.getCode();
            if (LiveShopErrorMsg.NOT_DISTRIBUTOR_CODE.equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.NOT_DISTRIBUTOR);
                return result;
            }
            if (!CommonStateCode.SUCCESS.equals(code) || moduleResult.getData() == null){
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            Page<LiveShopProductDTO> page = moduleResult.getData();
            result.addProperty("count", page.getCount());
            JsonArray orders = new JsonArray();
            if (page.getList() == null) {
                result.add("products", orders);
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
                return result;
            }
            
            for (LiveShopProductDTO productDTO : page.getList()) {
                JsonObject productJson = new JsonObject();
                productJson.addProperty("productId", productDTO.getProductId());
                if (productDTO.getResourceUrl() != null) {
                    productJson.addProperty("pictureUrl", productDTO.getResourceUrl() + "!174");
                }
                orders.add(productJson);
            }
            
            result.add("products", orders);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：liveShopService.getProductsByDistributorId(distributorId=%s, start=%s, num=%s)", distributorId, start, num), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取商品详情【51060512】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getProductInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        int productId;
        try {
            productId = CommonUtil.getJsonParamInt(jsonObject, "productId", 0, TagCodeEnum.ERROR_PRODUCT_ID, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            Result<LiveShopProductDTO> moduleResult = liveShopService.getProductInfoByProductId(productId);
            if (moduleResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            String code = moduleResult.getCode();
            if (LiveShopErrorMsg.NOT_EXIST_PRODUCT_CODE.equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.NOT_EXIST_PRODUCT);
                return result;
            } else if (!CommonStateCode.SUCCESS.equals(code) || moduleResult.getData() == null){
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            LiveShopProductDTO productDTO = moduleResult.getData();
            
            LiveShopTF.product2Json(result, productDTO);
            
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：liveShopService.getProductInfoByProductId(productId=%s)", productId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 生成分销商品订单【51060513】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject addDistributorOrder(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }
        int userId;
        int distributorId;
        int productId;
        int count;
        int addressId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, "5106051301", 1, Integer.MAX_VALUE);
            distributorId = CommonUtil.getJsonParamInt(jsonObject, "distributorId", 0, "5106051302", 0, Integer.MAX_VALUE);
            productId = CommonUtil.getJsonParamInt(jsonObject, "productId", 0, "5106051303", 1, Integer.MAX_VALUE);
            count = CommonUtil.getJsonParamInt(jsonObject, "count", 0, "5106051304", 1, Integer.MAX_VALUE);
            addressId = CommonUtil.getJsonParamInt(jsonObject, "addressId", 0, "5106051305", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            // 订单渠道只有app
            Result<String> addOrderResult = liveShopService.addDistributorOrder(userId, distributorId, productId, count, addressId,1);
            if (addOrderResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            String code = addOrderResult.getCode();
            if (LiveShopErrorMsg.NOT_MATCH_DISTRIBUTOR_PRODUCT_CODE.equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.NOT_MATCH_DISTRIBUTOR_PRODUCT);
                return result;
            } else if (LiveShopErrorMsg.NOT_VALID_PRODUCT_CODE.equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.NOT_VALID_PRODUCT);
                return result;
            } else if (LiveShopErrorMsg.STOCK_NOT_FULL_CODE.equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.STOCK_NOT_FULL);
                return result;
            } else if (!CommonStateCode.SUCCESS.equals(code)){
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            result.addProperty(PARAM_ORDER_NO, addOrderResult.getData());
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：liveShopService.addDistributorOrder(userId=%s, distributorId=%s, productId=%s, count=%s, addressId=%s)", userId, distributorId, productId, count, addressId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 商家向买家发送订单[51060514]
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject sendOrder(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }
        int userId;
        int buyerId;
        int resourceId;
        String productName;
        long productPrice;
        long expressMoney;
        int channel;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, "5106051401", 0, Integer.MAX_VALUE);
            buyerId = CommonUtil.getJsonParamInt(jsonObject, "buyerId", 0, "5106051402", 0, Integer.MAX_VALUE);
            resourceId = CommonUtil.getJsonParamInt(jsonObject, "resourceId", 0, "5106051403", 0, Integer.MAX_VALUE);
            productName = CommonUtil.getJsonParamString(jsonObject, "productName", null, "5106051404", 1, Integer.MAX_VALUE);
            productPrice = CommonUtil.getJsonParamLong(jsonObject, "productPrice", 0, "5106051405", 1, 99999999L);
            expressMoney = CommonUtil.getJsonParamLong(jsonObject, "expressMoney", 0, null, 0, 99999L);
            channel = CommonUtil.getJsonParamInt(jsonObject, "channel", 1, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        LiveShopProductDTO productDTO = new LiveShopProductDTO();
        try {
            productDTO.setResourceId(resourceId);
            productDTO.setExpressPrice(expressMoney);
            productDTO.setProductPrice(productPrice);
            productDTO.setProductName(productName);
            Result<String> addOrderResult = liveShopService.addOrderByActor(userId, productDTO, buyerId, channel);
            if (addOrderResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            String code = addOrderResult.getCode();
            if (LiveShopErrorMsg.NOT_SALE_ACTOR_CODE.equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.NOT_SALE_ACTOR);
                return result;
            } else if (LiveShopErrorMsg.NOT_USER_CODE.equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.NOT_USER);
                return result;
            } else if (!CommonStateCode.SUCCESS.equals(code)){
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            result.addProperty(PARAM_ORDER_NO, addOrderResult.getData());
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error(String.format("Module Error：liveShopService.addOrderByActor(userId=%s, productDTO=%s, buyerId=%s)", userId, productDTO, buyerId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        return result;
    }

    /**
     * 商家查看余额[51060515]
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getBalance(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }
        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, "5106051501", 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        try {
            Result<LiveShopBalanceInfoDTO> getBalanceResult = liveShopService.getMyBalance(userId);
            if (getBalanceResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            String code = getBalanceResult.getCode();
            if (LiveShopErrorMsg.NOT_HAS_BALANCE_ACTOR_CODE.equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.NOT_HAS_BALANCE_ACTOR);
                return result;
            } else if (!CommonStateCode.SUCCESS.equals(code) || getBalanceResult.getData() == null){
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            LiveShopBalanceInfoDTO balanceInfoDTO = getBalanceResult.getData();
            result.addProperty("balance", balanceInfoDTO.getBalance());
            result.addProperty("waitPayBalance", balanceInfoDTO.getWaitPayBalance());
            result.addProperty("waitDeliverBalance", balanceInfoDTO.getWaitDeliverBalance());
            result.addProperty("waitReceiveBalance", balanceInfoDTO.getWaitReceiveBalance());
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error(String.format("Module Error：liveShopService.getMyBalance(userId=%s)", userId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        return result;
    }

    /**
     * 商家提现余额[52060516]
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject cashApply(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        // sv安全校验接口
        try{
            JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
            if (rtJO != null) {
                return rtJO;
            }
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }
        int userId;
        long money;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, "5206051601", 0, Integer.MAX_VALUE);
            money = CommonUtil.getJsonParamLong(jsonObject, "money", 0, "5206051602", 0, Long.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        try {
            Result<Boolean> applyCrashResult = liveShopService.applyCrash(userId, money);
            if (applyCrashResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            String code = applyCrashResult.getCode();
            if (LiveShopErrorMsg.NOT_HAS_BALANCE_ACTOR_CODE.equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, "5206051603");
                return result;
            } else if (LiveShopErrorMsg.ERROR_WITHDRAW_MONEY_CODE.equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.ERROR_WITHDRAW_MONEY);
                return result;
            } else if (LiveShopErrorMsg.NOT_BIND_BANK_ACCOUNT_CODE.equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.NOT_BIND_BANK_ACCOUNT);
                return result;
            } else if (!CommonStateCode.SUCCESS.equals(code)){
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error(String.format("Module Error：liveShopService.applyCrash(userId=%s, money=%s)", userId, money), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        return result;
    }

    /**
     * 商家查询自己的交易明细[51060517]
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getTransactionDetail(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        // 检验token
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }
        int userId;
        int start;
        int num;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, "5106051701", 0, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.START, 0, null, 0, Integer.MAX_VALUE);
            num = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.NUM, 20, null, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        try {
            Result<Page<LiveShopTransactionDetailsDTO>> transactionDetailsResult = liveShopService.getTransactionDetails(userId, start, num);
            if (transactionDetailsResult == null || transactionDetailsResult.getData() == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            String code = transactionDetailsResult.getCode();
            if (LiveShopErrorMsg.NOT_HAS_BALANCE_ACTOR_CODE.equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.NOT_HAS_BALANCE_ACTOR);
                return result;
            } else if (!CommonStateCode.SUCCESS.equals(code)){
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            Page<LiveShopTransactionDetailsDTO> page = transactionDetailsResult.getData();
            result.addProperty("count", page.getCount());
            JsonArray jsonArray = new JsonArray();
            if (CollectionUtils.isNotEmpty(page.getList())) {
                for (LiveShopTransactionDetailsDTO liveShopTransactionDetailsDTO : page.getList()) {
                    JsonObject ele = new JsonObject();
                    ele.addProperty("transactionType", liveShopTransactionDetailsDTO.getTransactionType());
                    if (LiveShopTransactionType.WITHDRAW.equals(liveShopTransactionDetailsDTO.getTransactionType())) {
                        ele.addProperty("status", liveShopTransactionDetailsDTO.getStatus());
                    }
                    ele.addProperty("money", liveShopTransactionDetailsDTO.getMoney());
                    ele.addProperty("change", liveShopTransactionDetailsDTO.getChange());
                    ele.addProperty("date", liveShopTransactionDetailsDTO.getAddTime().getTime());
                    jsonArray.add(ele);
                }
            }
            result.add("transactionDetails", jsonArray);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error(String.format("Module Error：liveShopService.getTransactionDetails(userId=%s, start=%s, num=%s)", userId, start, num), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        return result;
    }

    /**
     * 判断是否是卖家（是否有余额）[51060518]
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject isSaleActor(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        // 检验token
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }
        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, "5106051801", 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        try {
            Result<Boolean> isSaleActorResult = liveShopService.isSaleActor(userId);
            if (isSaleActorResult == null || isSaleActorResult.getData() == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            result.addProperty("isSaleActor", isSaleActorResult.getData());
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error(String.format("Module Error：liveShopService.isSaleActor(userId=%s)", userId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        return result;
    }

    /**
     * 获取商家信息[51060519]
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getSaleActorInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        // 检验token
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }
        int userId;
        int type;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, "5106051901", 0, Integer.MAX_VALUE);
            type = CommonUtil.getJsonParamInt(jsonObject, "type", 1, "5106051902", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        try {
            Result<LiveShopInfoDTO> shopInfoResult = liveShopService.getShopInfo(userId, type);
            if (shopInfoResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            if (shopInfoResult.getData() == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.NOT_EXIST_SALE_ACTOR);
                return result;
            }
            result.addProperty("mobileNo", shopInfoResult.getData().getMobileNo());
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error(String.format("Module Error：liveShopService.isSaleActor(userId=%s)", userId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        return result;
    }
    
    /**
     * 获取卖家客服号ID列表【51060520】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getSubShopIds(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        int actorId;
        try {
            actorId = CommonUtil.getJsonParamInt(jsonObject, "actorId", 0, "5106052001", 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        try {
            List<Integer> subShopIds = liveShopService.getSubShopIds(actorId);
            JsonArray subShopIdArray = new JsonArray();
            if (CollectionUtils.isNotEmpty(subShopIds)) {
                for (Integer subShopId : subShopIds) {
                    subShopIdArray.add(subShopId);
                }
            }
            result.add("subShopIds", subShopIdArray);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error(String.format("Module Error：liveShopService.getSubShopIds(userId=%s)", actorId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        return result;
    }

    /**
     * 获取卖家客服号信息列表【51060521】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getSubShopInfos(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        // 检验token
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }
        int userId;
        int start;
        int offset;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, "5106052101", 0, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, null, 0, Integer.MAX_VALUE);
            offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 20, null, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        try {
            Page<UserProfile> page = liveShopService.getSubShopInfos(userId, start, offset);
            if (page == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            result.addProperty("count", page.getCount());
            JsonArray subShopInfos = new JsonArray();
            List<UserProfile> list = page.getList();
            if (CollectionUtils.isNotEmpty(list)) {
                for (UserProfile userProfile : list) {
                    JsonObject json = new JsonObject();
                    json.addProperty("userId", userProfile.getUserId());
                    json.addProperty("nickname", userProfile.getNickName());
                    if (!StringUtil.strIsNull(userProfile.getPortrait())) {
                        json.addProperty("portrait", ConfigHelper.getHttpdir() + userProfile.getPortrait());
                    }
                    json.addProperty("gender", userProfile.getGender());
                    json.addProperty("richLevel", userProfile.getUserLevel());
                    
                    subShopInfos.add(json);
                }
            }
            result.add("subShopInfos", subShopInfos);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error(String.format("Module Error：liveShopService.getSubShopInfos(userId=%s, start=%s, offset=%s)", 
                    userId, start, offset), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        return result;
    }

    /**
     * 商家设置客服号【52060522】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject addSubShopInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        // 安全接口
        JsonObject rtJO;
        try {
            rtJO = SecurityFunctions.checkSignedValue(jsonObject);
            if (rtJO != null){
                return rtJO;
            }
        } catch (Exception e) {
            logger.error("SecurityFunctions.checkSignedValue(" + jsonObject + ")", e);
            result.addProperty(ParameterKeys.TAG_CODE, "40010001");
            return result;
        }
        
        // 检验token
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }
        int userId;
        int subShopId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, "5206052201", 0, Integer.MAX_VALUE);
            subShopId = CommonUtil.getJsonParamInt(jsonObject, "subShopId", 1, "5206052202", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        // 客服号不能是神秘人
        try {
            XmanService xmanService = (XmanService)MelotBeanFactory.getBean("xmanService");
            if (subShopId <= 1127828 
                    && subShopId >= 1000578 
                    && xmanService.getXmanConf(subShopId) != null) {
                result.addProperty(ParameterKeys.TAG_CODE, "5206052205");
                return result;
            }
        } catch (Exception e) {
            logger.error("xmanService.getXmanConf(" + subShopId + ")", e);
        }
        
        // 客服号不能是靓号
        try {
            VirtualIdService virtualIdService = (VirtualIdService)MelotBeanFactory.getBean("virtualIdService");
            ConfVirtualId confVirtualId = virtualIdService.getConfVirtualIdById(subShopId);
            if (confVirtualId != null) {
                result.addProperty(ParameterKeys.TAG_CODE, "5206052205");
                return result;
            }
        } catch (Exception e) {
            logger.error("virtualIdService.getConfVirtualIdById(" + subShopId + ")", e);
        }
        
        // 客服号不能被封号
        try {
            AccountSecurityService accountSecurityService = (AccountSecurityService) MelotBeanFactory.getBean("accountSecurityService");
            boolean lock = accountSecurityService.isLock(subShopId);
            if (lock) {
                result.addProperty(ParameterKeys.TAG_CODE, "5206052205");
                return result;
            }
        } catch (Exception e) {
            logger.error("accountSecurityService.isLock(" + subShopId + ")", e);
        }
        
        try {
            boolean isSuccess = liveShopService.addSubShopId(userId, Arrays.asList(subShopId));
            if (isSuccess) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            } else {
                result.addProperty(ParameterKeys.TAG_CODE, "5206052203");
            }
        } catch (MelotModuleException e) {
            logger.info(String.format("Module Error：liveShopService.addSubShopId(userId=%s, subShopId=%s)", userId, subShopId), e);
            int errCode = e.getErrCode();
            if (errCode == 1) {
                // 主播不是直播购主播
                result.addProperty(ParameterKeys.TAG_CODE, "5206052204");
            } else if (errCode == 3) {
                // 客服号不合法
                result.addProperty(ParameterKeys.TAG_CODE, "5206052205");
            } else {
                // 数据库处理异常
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            }
        } catch (Exception e) {
            logger.error(String.format("Module Error：liveShopService.addSubShopId(userId=%s, subShopId=%s)", userId, subShopId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        return result;
    }

    /**
     * 商家删除客服号【52060523】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject delSubShopInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        // 安全接口
        JsonObject rtJO;
        try {
            rtJO = SecurityFunctions.checkSignedValue(jsonObject);
            if (rtJO != null){
                return rtJO;
            }
        } catch (Exception e) {
            logger.error("SecurityFunctions.checkSignedValue(" + jsonObject + ")", e);
            result.addProperty(ParameterKeys.TAG_CODE, "40010001");
            return result;
        }
        
        // 检验token
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }
        int userId;
        int subShopId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, "5206052301", 0, Integer.MAX_VALUE);
            subShopId = CommonUtil.getJsonParamInt(jsonObject, "subShopId", 1, "5206052302", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        try {
            boolean isSuccess = liveShopService.deleteSubShopId(userId, subShopId);
            if (isSuccess) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            } else {
                result.addProperty(ParameterKeys.TAG_CODE, "5206052303");
            }
        } catch (Exception e) {
            logger.error(String.format("Module Error：liveShopService.deleteSubShopId(userId=%s, subShopId=%s)", userId, subShopId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        return result;
    }

    /**
     * 判断是否允许添加客服[51060524]
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject canAddSubShop(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        // 检验token
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }
        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, "5106052401", 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        try {
            boolean canAddSubShop = liveShopService.canAddSubShop(userId);
            result.addProperty("canAddSubShop", canAddSubShop);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error(String.format("Module Error：liveShopService.canAddSubShop(userId=%s)", userId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        return result;
    }

    /**
     * 登记商家信息[51060525]
     */
    public JsonObject registerSaleActorInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        // 检验token
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }
        int userId;
        String phoneNo;
        String wechatName;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, TagCodeEnum.INVALID_PARAMETERS, 0, Integer.MAX_VALUE);
            phoneNo = CommonUtil.getJsonParamString(jsonObject, "phoneNo", null, TagCodeEnum.INVALID_PARAMETERS, 11, 11);
            wechatName = CommonUtil.getJsonParamString(jsonObject, "wechatName", null, TagCodeEnum.INVALID_PARAMETERS, 1, 100);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.INVALID_PARAMETERS);
            return result;
        }

        String tagCode = TagCodeEnum.MODULE_UNKNOWN_RESPCODE;
        try {
            // 渠道：2表示微信小程序, 暂时只有微信小程序
            if (liveShopService.registerSaleActorInfo(userId, wechatName, phoneNo, 2)) {
                tagCode = TagCodeEnum.SUCCESS;
            } else {
                tagCode = "5106052502";
            }
        } catch (MelotModuleException e) {
            logger.info(String.format("Fail:registerSaleActorInfo(userId=%s, phoneNo=%s)", userId, phoneNo), e);
            if (e.getErrCode() == 101) {
                tagCode = TagCodeEnum.INVALID_PARAMETERS;
            } else if (e.getErrCode() == 102) {
                tagCode = "5106052501";
            } else if (e.getErrCode() == 103) {
                tagCode = "5106052503";
            }
        } catch (Exception e) {
            logger.error(String.format("Error:registerSaleActorInfo(userId=%s, phoneNo=%s)", userId, phoneNo), e);
        }
        result.addProperty(ParameterKeys.TAG_CODE, tagCode);
        return result;
    }

    /**
     * 获取订单的数量（待支付、待发货、待收货、退款/售后），商家的话多显示销售的订单[51060526]
     */
    public JsonObject getOrderCount(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        // 检验token
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }
        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, TagCodeEnum.INVALID_PARAMETERS, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.INVALID_PARAMETERS);
            return result;
        }

        String tagCode = TagCodeEnum.MODULE_UNKNOWN_RESPCODE;
        try {
            Map<Integer, LiveShopOrderCountInfoDTO> orderCountInfo = liveShopService.getOrderCountInfo(userId);
            if (orderCountInfo.containsKey(1)) {
                result.add("buyOrderCount", getOrderCountJson(orderCountInfo.get(1)));
            }
            if (orderCountInfo.containsKey(2)) {
                result.add("saleOrderCount", getOrderCountJson(orderCountInfo.get(2)));
            }
            tagCode = TagCodeEnum.SUCCESS;
        } catch (MelotModuleException e) {
            logger.info(String.format("Fail:getOrderCount(userId=%s)", userId), e);
            if (e.getErrCode() == 101) {
                tagCode = TagCodeEnum.INVALID_PARAMETERS;
            }
        } catch (Exception e) {
            logger.error(String.format("Error:getOrderCount(userId=%s)", userId), e);
        }
        result.addProperty(ParameterKeys.TAG_CODE, tagCode);
        return result;
    }

    private JsonObject getOrderCountJson(LiveShopOrderCountInfoDTO orderCountInfoDTO) {
        JsonObject json = new JsonObject();
        json.addProperty("waitPayOrderCount", orderCountInfoDTO.getWaitPayOrderCount());
        json.addProperty("waitDeliverOrderCount", orderCountInfoDTO.getWaitDeliverOrderCount());
        json.addProperty("waitReceiveOrderCount", orderCountInfoDTO.getWaitReceiveOrderCount());
        json.addProperty("refundOrderCount", orderCountInfoDTO.getRefundOrderCount());
        return json;
    }

    /**
     * 获取首个待支付订单的信息[52060527]
     */
    public JsonObject getFirstWaitPayOrder(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        // 检验token
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }
        int userId, actorId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, TagCodeEnum.INVALID_PARAMETERS, 0, Integer.MAX_VALUE);
            actorId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.ACTOR_ID, 0, TagCodeEnum.INVALID_PARAMETERS, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.INVALID_PARAMETERS);
            return result;
        }

        String tagCode = TagCodeEnum.MODULE_UNKNOWN_RESPCODE;
        try {
            // 获取首个待支付手动发送订单
            LiveShopOrderDTO firstWaitPayOrder = liveShopService.getFirstWaitPayOrder(userId, actorId, 3);
            if (firstWaitPayOrder != null) {
                result.addProperty("orderId", firstWaitPayOrder.getOrderId());
                result.addProperty("expireTime", firstWaitPayOrder.getExpiryTime().getTime());
            }
            tagCode = TagCodeEnum.SUCCESS;
        } catch (Exception e) {
            logger.error(String.format("Error:getOrderCount(userId=%s)", userId), e);
        }
        result.addProperty(ParameterKeys.TAG_CODE, tagCode);
        return result;
    }

    /**
     * 判断用户是否可以在小程序上登记商家信息[52060531]
     */
    public JsonObject canRegisterInProject(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        // 检验token
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }
        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, TagCodeEnum.INVALID_PARAMETERS, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.INVALID_PARAMETERS);
            return result;
        }

        String tagCode = TagCodeEnum.MODULE_UNKNOWN_RESPCODE;
        try {
            boolean canRegister = liveShopService.canRegister(userId);
            result.addProperty("canRegister", canRegister);
            tagCode = TagCodeEnum.SUCCESS;
        } catch (Exception e) {
            logger.error(String.format("Error:getOrderCount(userId=%s)", userId), e);
        }
        result.addProperty(ParameterKeys.TAG_CODE, tagCode);
        return result;
    }

    /**
     * 获取申请状态(51060529)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getSellerApplyStatus(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int  userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "03040002", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        List<SellerApplyInfoDTO> list = sellerApplyInfoService.getUserSellerApplyInfoList(userId);
        if(!CollectionUtils.isEmpty(list)){
            if(this.isExistApplyStatus(list, SellerApplyCheckStatusEnum.PASS.value())){
                result.addProperty("status",1);
            } else if(this.isExistApplyStatus(list,SellerApplyCheckStatusEnum.IN_REVIEW.value())){
                result.addProperty("status",0);
            } else if(this.isExistApplyStatus(list,SellerApplyCheckStatusEnum.UN_PASS.value())){
                result.addProperty("status",-1);
                String errorReason = this.getLatestErrorReason(list);
                if(!StringUtils.isEmpty(errorReason)){
                    result.addProperty("errorReason",errorReason);
                }
            }
        } else {
            result.addProperty("status",-2);
        }

        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    private boolean isExistApplyStatus(List<SellerApplyInfoDTO> list,int checkStatus){
        for(SellerApplyInfoDTO item : list){
            if(item.getCheckStatus() == checkStatus){
                return true;
            }
        }
        return false;
    }

    private String getLatestErrorReason(List<SellerApplyInfoDTO> list){
        for(SellerApplyInfoDTO item : list){
            if(item.getCheckStatus() == SellerApplyCheckStatusEnum.UN_PASS.value()){
                return item.getAuditReason();
            }
        }
        return "";
    }

    /**
     * 入驻申请(51060530)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject applyForSeller(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int  userId;
        int inviter;
        int applyType;
        String name;
        String mobilePhone;
        String idCardFront;
        String idCardReverse;
        int mainCategoryId;
        String lessCategoryIds;
        String businessLicense;
        String foodLicense;
        String itemImg;
        String shopImg;

        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "03040002", 1, Integer.MAX_VALUE);
            inviter = CommonUtil.getJsonParamInt(jsonObject, "inviter", 0, null, 1, Integer.MAX_VALUE);
            applyType = CommonUtil.getJsonParamInt(jsonObject, "applyType", 1, TagCodeEnum.INVALID_PARAMETERS, 1, 2);
            name = CommonUtil.getJsonParamString(jsonObject, "name", null, TagCodeEnum.INVALID_PARAMETERS, 1, 200);
            mobilePhone = CommonUtil.getJsonParamString(jsonObject, "mobilePhone", null, TagCodeEnum.MOBILENUM_MISSING, 11, 11);
            idCardFront = CommonUtil.getJsonParamString(jsonObject, "idCardFront", null, TagCodeEnum.INVALID_PARAMETERS, 1, 100);
            idCardReverse = CommonUtil.getJsonParamString(jsonObject, "idCardReverse", null, TagCodeEnum.INVALID_PARAMETERS, 1, 100);
            mainCategoryId = CommonUtil.getJsonParamInt(jsonObject, "mainCategoryId", 0, TagCodeEnum.INVALID_PARAMETERS, 1, 1000);
            lessCategoryIds = CommonUtil.getJsonParamString(jsonObject, "lessCategoryIds", null, null, 1, 200);
            businessLicense = CommonUtil.getJsonParamString(jsonObject, "businessLicense", null, TagCodeEnum.INVALID_PARAMETERS, 1, 100);
            foodLicense = CommonUtil.getJsonParamString(jsonObject, "foodLicense", null, null, 1, 100);
            itemImg = CommonUtil.getJsonParamString(jsonObject, "itemImg", null, null, 1, 100);
            shopImg = CommonUtil.getJsonParamString(jsonObject, "shopImg", null, null, 1, 100);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        SellerApplyInfoDTO sellerApplyInfoDTO = new SellerApplyInfoDTO();
        sellerApplyInfoDTO.setUserId(userId);
        sellerApplyInfoDTO.setInviter(inviter);
        sellerApplyInfoDTO.setApplyType(applyType);
        sellerApplyInfoDTO.setApplyName(name);
        sellerApplyInfoDTO.setMobilePhone(mobilePhone);
        sellerApplyInfoDTO.setIdcardFront(idCardFront);
        sellerApplyInfoDTO.setIdcardReverse(idCardReverse);
        sellerApplyInfoDTO.setBusinesslicense(businessLicense);
        if(!StringUtils.isEmpty(itemImg)){
            sellerApplyInfoDTO.setItemImg(itemImg);
        }
        if(!StringUtils.isEmpty(shopImg)){
            sellerApplyInfoDTO.setShopImg(shopImg);
        }

        sellerApplyInfoDTO.setMainCategory("1");
        if(!StringUtils.isEmpty(lessCategoryIds)){
            sellerApplyInfoDTO.setLessCategoryIds(lessCategoryIds);
        }

        Result<Boolean> resultCode  = sellerApplyInfoService.sellerApply(sellerApplyInfoDTO);
        if(!CommonStateCode.SUCCESS.equals(resultCode.getCode())){
            if("APPLY_EXIST".equals(resultCode.getCode())){
                result.addProperty("TagCode",TagCodeEnum.APPLY_INFO_EXIST);
                return result;
            } else if("PARAMETERS_ERROR".equals(resultCode.getCode())){
                result.addProperty("TagCode",TagCodeEnum.PARAMETER_MISSING);
                return result;
            } else if("SQL_ERROR".equals(resultCode.getCode())){
                result.addProperty("TagCode",TagCodeEnum.EXECSQL_EXCEPTION);
                return result;
            }
        }
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
}

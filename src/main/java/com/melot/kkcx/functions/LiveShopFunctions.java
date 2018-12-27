package com.melot.kkcx.functions;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.melot.asset.driver.domain.ConfVirtualId;
import com.melot.asset.driver.service.VirtualIdService;
import com.melot.kk.liveshop.api.constant.*;
import com.melot.kk.liveshop.api.dto.*;
import com.melot.kk.liveshop.api.service.*;
import com.melot.kk.location.api.service.LocationService;
import com.melot.kk.logistics.api.domain.HistDeliveryDO;
import com.melot.kk.logistics.api.domain.UserAddressDO;
import com.melot.kk.logistics.api.service.HistDeliveryService;
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
import com.melot.kktv.domain.WorkVideoInfo;
import com.melot.kktv.service.WorkService;
import com.melot.kktv.util.*;
import com.melot.module.api.exceptions.MelotModuleException;
import com.melot.module.packagegift.driver.service.XmanService;
import com.melot.sdk.core.util.MelotBeanFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.util.*;

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

    @Resource
    ConfItemCatService confItemCatService;

    @Resource
    ConfSubShopService confSubShopService;

    @Resource
    ProductService productService;

    @Resource
    OrderService orderService;

    @Resource
    HistDeliveryService histDeliveryService;

    @Resource
    private CouponService couponService;

    @Resource
    private LocationService locationService;
    
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
                    productJson.addProperty("pictureUrl", productDTO.getResourceUrl());
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
            LiveShopProductDetailDTO productDetailDTO = productService.getProductDetail(productId);
            if (productDetailDTO == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.NOT_EXIST_PRODUCT);
                return result;
            }
            Integer deleteFlag = productDetailDTO.getDeleteFlag();
            if(deleteFlag != null && deleteFlag == 0) {
                result.addProperty("isValid", -1);
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
                return result;
            }
            LiveShopTF.product2Json(result, productDetailDTO);
            ConfItemCatDTO confItemCatDTO = confItemCatService.getConfItemCategory(productDetailDTO.getCatId());
            if(confItemCatDTO != null) {
                result.addProperty("catName", confItemCatDTO.getCatName());
            }

            UserProfile userProfile = kkUserService.getUserProfile(productDetailDTO.getActorId());
            if(userProfile != null){
                JsonObject json = new JsonObject();
                json.addProperty("userId",userProfile.getUserId());
                json.addProperty("nickname",userProfile.getNickName());
                if(userProfile.getPortrait() != null){
                    json.addProperty("portrait",ConfigHelper.getHttpdir() + userProfile.getPortrait());
                }
                json.addProperty("gender",userProfile.getGender());
                ProductManagerInfoDTO productManagerInfoDTO = productService.getProductManagerInfo(userProfile.getUserId());
                if(productDetailDTO != null){
                    json.addProperty("sellingProductNum",productManagerInfoDTO.getSellingProductCount());
                }
                result.add("supplier",json);
            }
            
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

            long waitSettleBalance = orderService.getWaitSettleBalance(userId);
            result.addProperty("waitSettleBalance", waitSettleBalance);

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
                    int transactionType = liveShopTransactionDetailsDTO.getTransactionType();
                    String transactionDesc = null;
                    if (transactionType == LiveShopTransactionType.WITHDRAW) {
                        ele.addProperty("status", liveShopTransactionDetailsDTO.getStatus());
                        String bankcard = liveShopTransactionDetailsDTO.getBankAccount();
                        String tailNumber = bankcard;
                        if(bankcard.length() >= 4) {
                            tailNumber = bankcard.substring(bankcard.length()-4);
                        }
                        transactionDesc = "提现到" + liveShopTransactionDetailsDTO.getBankName() + "(" + tailNumber +")";
                    } else if(transactionType == LiveShopTransactionType.PAYMENT) {
                        transactionDesc = "货款";
                    } else if(transactionType == LiveShopTransactionType.REFUND) {
                        transactionDesc = "退款";
                    } else if(transactionType == LiveShopTransactionType.COMMISSION_EXPENSE) {
                        transactionDesc = "佣金支出";
                    } else if(transactionType == LiveShopTransactionType.COMMISSION_INCOME) {
                        transactionDesc = "佣金收入";
                    }
                    ele.addProperty("transactionType", transactionType);
                    ele.addProperty("transactionDesc", transactionDesc);
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
                result.addProperty("orderNo", firstWaitPayOrder.getOrderNo());
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

        boolean isCustomerService = confSubShopService.isCustomerService(userId);
        if(isCustomerService){
            result.addProperty("TagCode",TagCodeEnum.CUSTOMER_SERVICE_CAN_NOT_APPLY);
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

        int userId;
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
        String provinceCode;
        String cityCode;


        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, ParamCodeEnum.USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            inviter = CommonUtil.getJsonParamInt(jsonObject, "inviter", 0, null, 1, Integer.MAX_VALUE);
            applyType = CommonUtil.getJsonParamInt(jsonObject, "applyType", 1, TagCodeEnum.INVALID_PARAMETERS, 1, 2);
            name = CommonUtil.getJsonParamString(jsonObject, "name", null, TagCodeEnum.INVALID_PARAMETERS, 1, 200);
            mobilePhone = CommonUtil.getJsonParamString(jsonObject, "mobilePhone", null, TagCodeEnum.MOBILENUM_MISSING, 11, 11);
            idCardFront = CommonUtil.getJsonParamString(jsonObject, "idCardFront", null, TagCodeEnum.INVALID_PARAMETERS, 1, 1000);
            idCardReverse = CommonUtil.getJsonParamString(jsonObject, "idCardReverse", null, TagCodeEnum.INVALID_PARAMETERS, 1, 1000);
            mainCategoryId = CommonUtil.getJsonParamInt(jsonObject, "mainCategoryId", 0, TagCodeEnum.INVALID_PARAMETERS, 1, 1000);
            lessCategoryIds = CommonUtil.getJsonParamString(jsonObject, "lessCategoryIds", null, null, 0, 200);
            businessLicense = CommonUtil.getJsonParamString(jsonObject, "businessLicense", null, null, 1, 1000);
            foodLicense = CommonUtil.getJsonParamString(jsonObject, "foodLicense", null, null, 1, 1000);
            itemImg = CommonUtil.getJsonParamString(jsonObject, "itemImg", null, null, 0, 1000);
            shopImg = CommonUtil.getJsonParamString(jsonObject, "shopImg", null, null, 0, 1000);
            provinceCode = CommonUtil.getJsonParamString(jsonObject, "provinceCode", null, null, 0, 20);
            cityCode = CommonUtil.getJsonParamString(jsonObject, "cityCode", null, null, 0, 20);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        int businessType = 2;
        if (applyType == businessType) {
            if (StringUtils.isEmpty(businessLicense)) {
                result.addProperty("TagCode", TagCodeEnum.BUSINESSLICENSE_IS_EMPTY);
                return result;
            }
        }

        SellerApplyInfoV2DTO sellerApplyInfoV2DTO = new SellerApplyInfoV2DTO();
        sellerApplyInfoV2DTO.setUserId(userId);
        sellerApplyInfoV2DTO.setInviter(inviter);
        sellerApplyInfoV2DTO.setApplyType(applyType);
        sellerApplyInfoV2DTO.setApplyName(name);
        sellerApplyInfoV2DTO.setMobilePhone(mobilePhone);
        sellerApplyInfoV2DTO.setIdcardFront(idCardFront);
        sellerApplyInfoV2DTO.setIdcardReverse(idCardReverse);
        sellerApplyInfoV2DTO.setBusinesslicense(businessLicense);
        if (!StringUtils.isEmpty(provinceCode)) {
            sellerApplyInfoV2DTO.setProvinceCode(provinceCode);
        }
        if (!StringUtils.isEmpty(cityCode)) {
            sellerApplyInfoV2DTO.setCityCode(cityCode);
        }
        if (!StringUtils.isEmpty(itemImg)) {
            sellerApplyInfoV2DTO.setItemImg(itemImg);
        }
        if (!StringUtils.isEmpty(shopImg)) {
            sellerApplyInfoV2DTO.setShopImg(shopImg);
        }
        if (!StringUtils.isEmpty(foodLicense)) {
            sellerApplyInfoV2DTO.setFoodLicense(foodLicense);
        }

        ConfItemCatDTO mainCategory = confItemCatService.getConfItemCategory(mainCategoryId);
        if (mainCategory != null) {
            sellerApplyInfoV2DTO.setMainCategoryId(mainCategory.getCatId());
            if (mainCategory.getCatLevel() > 1) {
                ConfItemCatDTO parentCategory = confItemCatService.getConfItemCategory(mainCategory.getParentCatId());
                if (parentCategory != null) {
                    sellerApplyInfoV2DTO.setMainCategoryName(parentCategory.getCatName().concat("(")
                            .concat(mainCategory.getCatName()).concat(")"));
                }
            } else {
                sellerApplyInfoV2DTO.setMainCategoryName(mainCategory.getCatName());
            }
        }

        String lessCategoryName = this.getLessCategoryName(lessCategoryIds);
        if (!StringUtils.isEmpty(lessCategoryName)) {
            String trimLessCategoryId = this.trimEnd(lessCategoryIds, ",");
            sellerApplyInfoV2DTO.setLessCategoryId(trimLessCategoryId);

            String trimLessCategoryName = this.trimEnd(lessCategoryName, ",");
            sellerApplyInfoV2DTO.setLessCategoryName(trimLessCategoryName);
        }

        Result<Boolean> resultCode = sellerApplyInfoService.sellerApplyV2(sellerApplyInfoV2DTO);
        if (!CommonStateCode.SUCCESS.equals(resultCode.getCode())) {
            if ("APPLY_EXIST".equals(resultCode.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.APPLY_INFO_EXIST);
                return result;
            } else if ("PARAMETERS_ERROR".equals(resultCode.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.PARAMETER_MISSING);
                return result;
            } else if ("SQL_ERROR".equals(resultCode.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
                return result;
            } else if ("IS_CUSTOMER_SERVICE".equals(resultCode.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.CUSTOMER_SERVICE_CAN_NOT_APPLY);
                return result;
            }
        }
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    private String getLessCategoryName(String lessCategoryIds){
        if(StringUtils.isEmpty(lessCategoryIds)){
            return "";
        }
        String[] array = lessCategoryIds.split(",");
        if(array!=null && array.length > 0){
            StringBuilder categoryName = new StringBuilder();
            for(String item : array){
                if(NumberUtils.isDigits(item)){
                    ConfItemCatDTO category = confItemCatService.getConfItemCategory(Integer.parseInt(item));
                    if(category != null){
                        if(category.getCatLevel() > 1){
                            ConfItemCatDTO parentCategory =
                                    confItemCatService.getConfItemCategory(category.getParentCatId());
                            if(parentCategory != null){
                                categoryName.append(parentCategory.getCatName()).append("(")
                                        .append(category.getCatName()).append(")").append(",");
                            }
                        } else {
                            categoryName.append(category.getCatName()).append(",");
                        }
                    }
                }
            }
            return categoryName.toString();
        }else{
            return "";
        }
    }

    private  String trimEnd(String target, String separator) {
        if (StringUtils.isEmpty(target) || StringUtils.isEmpty(separator)) {
            return "";
        }
        int minLength = 1;
        if (target.length() > minLength) {
            int index = target.length() - 1;
            char lastChar = target.charAt(index);
            if (lastChar == separator.charAt(0)) {
                return target.substring(0, index);
            } else {
                return target;
            }
        } else {
            return target;
        }
    }

    /**
     * 获取审核未通过的信息(51060532)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getUserLatestUnPassApply(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, ParamCodeEnum.USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }
        SellerApplyInfoV2DTO sellerApplyInfoV2DTO = this.getUserLatestUnPassApply(userId);
        if (sellerApplyInfoV2DTO != null) {
            result.addProperty("applyId", sellerApplyInfoV2DTO.getApplyId());
            if (!StringUtils.isEmpty(sellerApplyInfoV2DTO.getAuditReason())) {
                result.addProperty("auditReason", sellerApplyInfoV2DTO.getAuditReason());
            }
            result.addProperty("checkStatus", sellerApplyInfoV2DTO.getCheckStatus());
            result.addProperty("userId", sellerApplyInfoV2DTO.getUserId());
            result.addProperty("applyName", sellerApplyInfoV2DTO.getApplyName());
            result.addProperty("applyType", sellerApplyInfoV2DTO.getApplyType());
            if (!StringUtils.isEmpty(sellerApplyInfoV2DTO.getBusinesslicense())) {
                result.addProperty("businesslicense", this.getImgPath(sellerApplyInfoV2DTO.getBusinesslicense()));
            }
            if (!StringUtils.isEmpty(sellerApplyInfoV2DTO.getFoodLicense())) {
                result.addProperty("foodLicense", this.getImgPath(sellerApplyInfoV2DTO.getFoodLicense()));
            }
            if (!StringUtils.isEmpty(sellerApplyInfoV2DTO.getIdcardFront())) {
                result.addProperty("idcardFront", this.getImgPath(sellerApplyInfoV2DTO.getIdcardFront()));
            }
            if (!StringUtils.isEmpty(sellerApplyInfoV2DTO.getIdcardReverse())) {
                result.addProperty("idcardReverse", this.getImgPath(sellerApplyInfoV2DTO.getIdcardReverse()));
            }
            result.addProperty("createTime", sellerApplyInfoV2DTO.getCreateTime().getTime());
            if (sellerApplyInfoV2DTO.getInviter() !=null && sellerApplyInfoV2DTO.getInviter() > 0) {
                result.addProperty("inviter", sellerApplyInfoV2DTO.getInviter());
            }
            if (!StringUtils.isEmpty(sellerApplyInfoV2DTO.getItemImg())) {
                result.addProperty("itemImg", this.getImgPath(sellerApplyInfoV2DTO.getItemImg()));
            }
            if (!StringUtils.isEmpty(sellerApplyInfoV2DTO.getShopImg())) {
                result.addProperty("shopImg", this.getImgPath(sellerApplyInfoV2DTO.getShopImg()));
            }
            result.addProperty("mainCategoryId", sellerApplyInfoV2DTO.getMainCategoryId());
            if (!StringUtils.isEmpty(sellerApplyInfoV2DTO.getLessCategoryId())) {
                result.addProperty("lessCategoryIds", sellerApplyInfoV2DTO.getLessCategoryId());
            }
            result.addProperty("mobilePhone", sellerApplyInfoV2DTO.getMobilePhone());
            if (!StringUtils.isEmpty(sellerApplyInfoV2DTO.getProvinceCode())) {
                result.addProperty("provinceCode", sellerApplyInfoV2DTO.getProvinceCode());
            }
            if (!StringUtils.isEmpty(sellerApplyInfoV2DTO.getCityCode())) {
                result.addProperty("cityCode", sellerApplyInfoV2DTO.getCityCode());
            }
            String locationDesc = this.switchCodeToLocationDesc(sellerApplyInfoV2DTO.getProvinceCode(),
                    sellerApplyInfoV2DTO.getCityCode());
            if (locationDesc != null) {
                result.addProperty("locationDesc", locationDesc);
            }
        }
        result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    private String getImgPath(String url){
       if(StringUtils.isEmpty(url)){
           return "";
       }else{
           return url.replaceAll(ConfigHelper.getHttpdir(),"");
       }
    }

    private SellerApplyInfoV2DTO getUserLatestUnPassApply(int userId){
        List<SellerApplyInfoV2DTO> list = sellerApplyInfoService.getUserSellerApplyInfoListV2(userId);
        if(CollectionUtils.isEmpty(list)){
            return null;
        } else {
            for(SellerApplyInfoV2DTO item : list){
                if(item.getCheckStatus() == SellerApplyCheckStatusEnum.UN_PASS.value()){
                    return item;
                }
            }
            return null;
        }
    }

    private String switchCodeToLocationDesc(String provinceCode, String cityCode) {
        String provinceName = locationService.getAreaNameByAreaCode(provinceCode);
        String cityName = locationService.getAreaNameByAreaCode(cityCode);
        if (StringUtils.isEmpty(provinceName)) {
            return null;
        } else if (StringUtils.isEmpty(cityName)) {
            return provinceName;
        } else {
            return provinceName.concat(cityName);
        }
    }

    /**
     * 获取商品品类列表(51060528)
     * @return
     */
    public JsonObject getItemCatList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        try {
            List<ConfItemCatDTO> list = confItemCatService.getItemCatList();
            if (CollectionUtils.isEmpty(list)) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            JsonArray itemCatList = new JsonArray();
            JsonArray secondItemCatList;
            List<ConfItemCatDTO> secondList;
            for (ConfItemCatDTO record : list) {
                JsonObject itemCatJson = new JsonObject();
                itemCatJson.addProperty("catId", record.getCatId());
                itemCatJson.addProperty("catName", record.getCatName());
                secondList = record.getSecondLevelItemCatList();
                if(CollectionUtils.isNotEmpty(secondList)) {
                    secondItemCatList = new JsonArray();
                    for(ConfItemCatDTO secondRecord : secondList) {
                        JsonObject secondItemCatJson = new JsonObject();
                        secondItemCatJson.addProperty("catId", secondRecord.getCatId());
                        secondItemCatJson.addProperty("catName", secondRecord.getCatName());
                        secondItemCatJson.addProperty("specialPermission", secondRecord.getSpecialPermission());
                        secondItemCatList.add(secondItemCatJson);
                    }
                    itemCatJson.add("secondItemCatList", secondItemCatList);
                }

                itemCatList.add(itemCatJson);
            }

            result.add("itemCatList", itemCatList);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            logger.error("Module Error：getItemCatList()", e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        return result;
    }

    /**
     * 根据卖家ID获取商品列表(51060533)
     * @return
     */
    public JsonObject getProductsByMerchantId(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        int merchantId;
        try {
            merchantId = CommonUtil.getJsonParamInt(jsonObject, "merchantId", 0, TagCodeEnum.ERROR_MERCHANT_ID, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {

            List<LiveShopProductDTO> list = liveShopService.getProductsByMerchantId(merchantId);

            if (CollectionUtils.isEmpty(list)) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }

            JsonArray products = new JsonArray();
            for (LiveShopProductDTO productDTO : list) {
                JsonObject productJson = new JsonObject();
                productJson.addProperty("productId", productDTO.getProductId());
                productJson.addProperty("productName", productDTO.getProductName());
                products.add(productJson);
            }

            result.add("products", products);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：liveShopService.getProductsByMerchantId(%s)", merchantId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 生成H5分销商品订单【51060534】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject addH5DistributorOrder(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }
        int userId;
        int productId;
        long orderMoney;
        int addressId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, "5106051301", 1, Integer.MAX_VALUE);
            productId = CommonUtil.getJsonParamInt(jsonObject, "productId", 0, "5106051303", 1, Integer.MAX_VALUE);
            orderMoney = CommonUtil.getJsonParamLong(jsonObject, "orderMoney", 0l, TagCodeEnum.ERROR_ORDER_MONEY, 1, Long.MAX_VALUE);
            addressId = CommonUtil.getJsonParamInt(jsonObject, "addressId", 0, "5106051305", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            // 订单渠道只有h5
            Result<String> addOrderResult = liveShopService.addH5DistributorOrder(userId, productId, orderMoney, addressId);
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
            } else if (!CommonStateCode.SUCCESS.equals(code)){
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            result.addProperty(PARAM_ORDER_NO, addOrderResult.getData());
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：liveShopService.addH5DistributorOrder(userId=%s, productId=%s, orderMoney=%s, addressId=%s)", userId, productId, orderMoney, addressId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取一级商品品类列表(51060539)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getFirstLevelItemCats(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        int sellerId;

        try {
            sellerId = CommonUtil.getJsonParamInt(jsonObject, "sellerId", 0, "03040002", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }
        JsonArray jsonArray = new JsonArray();
        List<ConfItemCatDTO> list = sellerApplyInfoService.getUserApplyCategoryList(sellerId);
        if(!CollectionUtils.isEmpty(list)){
            for(ConfItemCatDTO item : list){
                JsonObject json = new JsonObject();
                json.addProperty("catId",item.getCatId());
                json.addProperty("catName",item.getCatName());
                jsonArray.add(json);
            }
        }
        result.add("itemCatList",jsonArray);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 添加商品(51060540)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject saveProduct(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        String productBannerUrls;
        int catId;
        String productName;
        long productPrice;
        int stockNum;
        long expressPrice;
        String productDetailDesc;
        String productDetailUrls;
        int supportReturn;
        int supportDistribution;
        int distributorCommissionRate;

        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "03040002", 1, Integer.MAX_VALUE);

            productBannerUrls = CommonUtil.getJsonParamString(jsonObject, "productBannerUrls", null,
                    ParamCodeEnum.PRODUCT_BANNER_URLS.getErrorCode(), 1, 5000);

            catId = CommonUtil.getJsonParamInt(jsonObject, "catId", 0,
                    ParamCodeEnum.CAT_ID.getErrorCode(), 1, Integer.MAX_VALUE);

            productName = CommonUtil.getJsonParamString(jsonObject, "productName", null,
                    ParamCodeEnum.PRODUCT_NAME.getErrorCode(), 1, 500);

            productPrice = CommonUtil.getJsonParamLong(jsonObject, "productPrice", 0,
                    ParamCodeEnum.PRODUCT_PRICE.getErrorCode(), 1, Long.MAX_VALUE);

            stockNum = CommonUtil.getJsonParamInt(jsonObject, "stockNum", 0,
                    ParamCodeEnum.STOCK_NUM.getErrorCode(), 1, Integer.MAX_VALUE);

            expressPrice = CommonUtil.getJsonParamInt(jsonObject, "expressPrice", 0,
                    ParamCodeEnum.EXPRESS_PRICE.getErrorCode(), 0, Integer.MAX_VALUE);

            productDetailDesc = CommonUtil.getJsonParamString(jsonObject, "productDetailDesc", null,
                    null, 0, 8000);

            productDetailUrls = CommonUtil.getJsonParamString(jsonObject, "productDetailUrls",
                    null, null, 0, 5000);

            supportReturn = CommonUtil.getJsonParamInt(jsonObject, "supportReturn", 0,
                    ParamCodeEnum.SUPPORT_RETURN.getErrorCode(), 0, 1);

            supportDistribution = CommonUtil.getJsonParamInt(jsonObject, "supportDistribution", 0,
                    ParamCodeEnum.SUPPORT_DISTRIBUTION.getErrorCode(), 0, 1);

            distributorCommissionRate = CommonUtil.getJsonParamInt(jsonObject, "distributorCommissionRate",
                    0, null, 0, 100);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        LiveShopProductDetailDTO liveShopProductDetailDTO = new LiveShopProductDetailDTO();
        liveShopProductDetailDTO.setActorId(userId);
        liveShopProductDetailDTO.setCatId(catId);
        liveShopProductDetailDTO.setExpressPrice(expressPrice);
        if(!StringUtils.isEmpty(productDetailDesc)){
            liveShopProductDetailDTO.setProductDetailDesc(productDetailDesc);
        }
        liveShopProductDetailDTO.setProductPrice(productPrice);
        liveShopProductDetailDTO.setStockNum(stockNum);
        liveShopProductDetailDTO.setSupportReturn(supportReturn);
        liveShopProductDetailDTO.setProductName(productName);
        liveShopProductDetailDTO.setSupportDistribution(supportDistribution);
        if(supportDistribution > 0){
            if(distributorCommissionRate <= 0){
                result.addProperty("TagCode", ParamCodeEnum.DISTRIBUTOR_COMMISSIONRATE.getErrorCode());
                return result;
            } else {
                liveShopProductDetailDTO.setDistributorCommissionRate(distributorCommissionRate);
            }
        }
        List<LiveShopProductPictureDTO> imgList = this.getProductImgList(productBannerUrls,1);
        if(!CollectionUtils.isEmpty(imgList)){
            liveShopProductDetailDTO.setResourceUrl(imgList.get(0).getResourceUrl());
            liveShopProductDetailDTO.setProductPictureDTOList(imgList);
        }
        if(!StringUtils.isEmpty(productDetailUrls)){
            List<LiveShopProductPictureDTO> detailList = this.getProductImgList(productDetailUrls,2);
            if(!CollectionUtils.isEmpty(detailList)){
                liveShopProductDetailDTO.setProductPictureDTODetailList(detailList);
            }
        }

        Result<Boolean> proResult = productService.addProduct(liveShopProductDetailDTO);
        if (CommonStateCode.SUCCESS.equals(proResult.getCode())){
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } else {
            if ("RATE_ERROR".equals(proResult.getCode())){
                result.addProperty("TagCode", TagCodeEnum.RATE_ERROR);
                return result;
            } else if ("NOT_SELLER".equals(proResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.NOT_EXIST_SALE_ACTOR);
                return result;
            } else if ("SQL_ERROR".equals(proResult.getCode())){
                result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
                return result;
            } else {
                result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
        }
    }

    private List<LiveShopProductPictureDTO> getProductImgList(String url,int picType){
        if(StringUtils.isEmpty(url)){
            return null;
        }
        List<LiveShopProductPictureDTO> list = new ArrayList<>();
        String[] array = url.split(",");
        if(array!=null && array.length>0){
            for(int i=0; i<array.length;i++){
                String item = array[i];
                LiveShopProductPictureDTO liveShopProductPictureDTO = new LiveShopProductPictureDTO();
                if(item.indexOf("!") > 0) {
                    item = item.substring(0, item.lastIndexOf("!"));
                }
                liveShopProductPictureDTO.setResourceUrl(item);
                liveShopProductPictureDTO.setPictureType(picType);
                liveShopProductPictureDTO.setSortNo(i+1);
                // 获取图片宽高信息
                WorkVideoInfo imageInfo = WorkService.getImageInfoByHttp(item);
                if (imageInfo != null) {
                    liveShopProductPictureDTO.setPictureHeight(imageInfo.getHeight());
                    liveShopProductPictureDTO.setPictureWidth(imageInfo.getWidth());
                }

                list.add(liveShopProductPictureDTO);
            }
        }
        return list;
    }

    /**
     * 修改商品(51060552)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject updateProduct(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        int productId;
        String productBannerUrls;
        int catId;
        String productName;
        long productPrice;
        int stockNum;
        long expressPrice;
        String productDetailDesc;
        String productDetailUrls;
        int supportReturn;
        int distributorCommissionRate;

        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "03040002", 1, Integer.MAX_VALUE);

            productId = CommonUtil.getJsonParamInt(jsonObject, "productId", 0, ParamCodeEnum.PRODUCT_ID.getErrorCode(), 1, Integer.MAX_VALUE);

            productBannerUrls = CommonUtil.getJsonParamString(jsonObject, "productBannerUrls", null,
                    null, 0, 5000);

            catId = CommonUtil.getJsonParamInt(jsonObject, "catId", 0,
                    null, 0, Integer.MAX_VALUE);

            productName = CommonUtil.getJsonParamString(jsonObject, "productName", null,
                    null, 0, 500);

            productPrice = CommonUtil.getJsonParamLong(jsonObject, "productPrice", 0,
                    null, 1, Long.MAX_VALUE);

            stockNum = CommonUtil.getJsonParamInt(jsonObject, "stockNum", 0,
                    null, 1, Integer.MAX_VALUE);

            expressPrice = CommonUtil.getJsonParamInt(jsonObject, "expressPrice", -1,
                    null, 0, Integer.MAX_VALUE);

            productDetailDesc = CommonUtil.getJsonParamString(jsonObject, "productDetailDesc", null,
                    null, 0, 8000);

            productDetailUrls = CommonUtil.getJsonParamString(jsonObject, "productDetailUrls",
                    null, null, 0, 5000);

            supportReturn = CommonUtil.getJsonParamInt(jsonObject, "supportReturn", -1,
                    null, 0, 1);

            distributorCommissionRate = CommonUtil.getJsonParamInt(jsonObject, "distributorCommissionRate",
                    0, null, 0, 100);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        LiveShopProductDetailDTO liveShopProductDetailDTO = productService.getProductDetail(productId);
        if(liveShopProductDetailDTO == null){
            result.addProperty("TagCode", TagCodeEnum.NOT_EXIST_PRODUCT);
            return result;
        }
        if(userId != liveShopProductDetailDTO.getActorId()){
            result.addProperty("TagCode", TagCodeEnum.PERMISSIONS_ERROR);
            return result;
        }

        if(catId > 0){
            liveShopProductDetailDTO.setCatId(catId);
        }
        if(expressPrice >= 0){
            liveShopProductDetailDTO.setExpressPrice(expressPrice);
        }
        if(!StringUtils.isEmpty(productDetailDesc)){
            liveShopProductDetailDTO.setProductDetailDesc(productDetailDesc);
        }
        if(productPrice > 0){
            liveShopProductDetailDTO.setProductPrice(productPrice);
        }
        if(stockNum > 0){
            liveShopProductDetailDTO.setStockNum(stockNum);
        }
        if(supportReturn >= 0){
            liveShopProductDetailDTO.setSupportReturn(supportReturn);
        }
        if(!StringUtils.isEmpty(productName)){
            liveShopProductDetailDTO.setProductName(productName);
        }
        if(distributorCommissionRate > 0 && liveShopProductDetailDTO.getSupportDistribution() == 1) {
            liveShopProductDetailDTO.setDistributorCommissionRate(distributorCommissionRate);
        }

        List<LiveShopProductPictureDTO> imgList = this.getProductImgList(productBannerUrls,1);
        if(!CollectionUtils.isEmpty(imgList)){
            String url = imgList.get(0).getResourceUrl();
            if(url.indexOf("!") > 0) {
                url = url.substring(0, url.lastIndexOf("!"));
            }
            liveShopProductDetailDTO.setResourceUrl(url);
            liveShopProductDetailDTO.setProductPictureDTOList(imgList);
        }
        if(!StringUtils.isEmpty(productDetailUrls)){
            List<LiveShopProductPictureDTO> detailList = this.getProductImgList(productDetailUrls,2);
            if(!CollectionUtils.isEmpty(detailList)){
                liveShopProductDetailDTO.setProductPictureDTODetailList(detailList);
            }
        }

        Result<Boolean> proResult = productService.updateProduct(productId, liveShopProductDetailDTO);
        if(CommonStateCode.SUCCESS.equals(proResult.getCode())){
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } else {
            if ("PRODUCT_NOT_EXIST".equals(proResult.getCode())){
                result.addProperty("TagCode", TagCodeEnum.NOT_EXIST_PRODUCT);
                return result;
            } else if ("PRODUCT_NOT_AVAILABLE".equals(proResult.getCode())){
                result.addProperty("TagCode", TagCodeEnum.NOT_VALID_PRODUCT);
                return result;
            } else if ("NOT_SELLER".equals(proResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.NOT_EXIST_SALE_ACTOR);
                return result;
            } else if ("SQL_ERROR".equals(proResult.getCode())){
                result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
                return result;
            } else {
                result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
        }
    }

    /**
     * 代理商品(51060541)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject proxyProduct(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        int productId;

        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "03040002", 1, Integer.MAX_VALUE);
            productId = CommonUtil.getJsonParamInt(jsonObject, "productId", 0, TagCodeEnum.INVALID_PARAMETERS, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        Result<Boolean> resultCode = productService.proxyProduct(userId,productId);
        if("PRODUCT_NOT_EXIST".equals(resultCode.getCode())){
            result.addProperty("TagCode", TagCodeEnum.NOT_EXIST_PRODUCT);
            return result;
        }
        if("PRODUCT_NOT_AVAILABLE".equals(resultCode.getCode())){
            result.addProperty("TagCode", TagCodeEnum.NOT_VALID_PRODUCT);
            return result;
        }
        if("PRODUCT_NOT_SUPPORT_DISTRIBUTION".equals(resultCode.getCode())){
            result.addProperty("TagCode", TagCodeEnum.PRODUCT_NOT_SUPPORT_DISTRIBUTION);
            return result;
        }
        if("CAN_NOT_PROXY_SELF_PRODUCT".equals(resultCode.getCode())){
            result.addProperty("TagCode", TagCodeEnum.PROXY_USER_ERROR);
            return result;
        }
        if("PRODUCT_ALREADY_PROXY".equals(resultCode.getCode())){
            result.addProperty("TagCode", TagCodeEnum.PRODUCT_ALREADY_PROXY);
            return result;
        }
        if("SQL_ERROR".equals(resultCode.getCode())){
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
            return result;
        }
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 获取货源商品列表【51060535】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getSourceProducts(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        int userId;
        int start;
        int num;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParamCodeEnum.USER_ID.getId(), 0, ParamCodeEnum.USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
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
            Page<SourceProductInfoDTO> page = productService.getSourceProducts(userId, start, num);

            if (page == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            result.addProperty("count", page.getCount());
            JsonArray products = new JsonArray();
            if (page.getList() == null) {
                result.add("products", products);
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
                return result;
            }

            for (SourceProductInfoDTO productDTO : page.getList()) {
                UserProfile userProfile = kkUserService.getUserProfile(productDTO.getSupplierId());
                if(userProfile != null){
                    JsonObject productJson = new JsonObject();
                    productJson.addProperty("productId", productDTO.getProductId());
                    productJson.addProperty("productName", productDTO.getProductName());
                    productJson.addProperty("productPrice", productDTO.getProductPrice());
                    productJson.addProperty("productUrl", productDTO.getProductUrl() + "!256");
                    productJson.addProperty("stockNum", productDTO.getStockNum());
                    productJson.addProperty("proxy", productDTO.getProxy());
                    productJson.addProperty("distributorCommissionRate", productDTO.getDistributorCommissionRate());
                    productJson.addProperty("distributorCommissionAmount", productDTO.getDistributorCommissionAmount());
                    productJson.addProperty("supplierId", productDTO.getSupplierId());
                    productJson.addProperty("supplierNickname", productDTO.getSupplierNickname());
                    if(userProfile.getPortrait() != null){
                        productJson.addProperty("supplierPortrait",userProfile.getPortrait());
                    }
                    productJson.addProperty("supplierIdGender",userProfile.getGender());

                    products.add(productJson);
                }
            }

            result.add("products", products);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：liveShopService.getSellingProducts(userId=%s, start=%s, num=%s)", userId, start, num), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

	/**
     * 获取用户在售商品列表【51060536】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getSellingProducts(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        int userId;
        int start;
        int num;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParamCodeEnum.USER_ID.getId(), 0, ParamCodeEnum.USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
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
            Page<LiveShopProductInfoDTO> page = productService.getProducts(userId, LiveShopProductState.IS_VALID, start, num);

            if (page == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            result.addProperty("count", page.getCount());
            JsonArray products = new JsonArray();
            if (page.getList() == null) {
                result.add("products", products);
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
                return result;
            }

            for (LiveShopProductInfoDTO productDTO : page.getList()) {
                JsonObject productJson = new JsonObject();
                productJson.addProperty("productId", productDTO.getProductId());
                productJson.addProperty("productName", productDTO.getProductName());
                productJson.addProperty("productPrice", productDTO.getProductPrice());
                productJson.addProperty("productUrl", productDTO.getProductUrl() + "!256");
                productJson.addProperty("stockNum", productDTO.getStockNum());
                productJson.addProperty("owner", productDTO.getOwner());
                productJson.addProperty("supportReturn", productDTO.getSupportReturn());
                if(productDTO.getDistributorCommissionRate() != null) {
                    productJson.addProperty("distributorCommissionRate", productDTO.getDistributorCommissionRate());
                    productJson.addProperty("distributorCommissionAmount", productDTO.getDistributorCommissionAmount());
                }

                products.add(productJson);
            }

            result.add("products", products);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：liveShopService.getSellingProducts(userId=%s, start=%s, num=%s)", userId, start, num), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

	/**
     * 获取用户已下架商品列表【51060546】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getOffShelvesProducts(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        int start;
        int num;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParamCodeEnum.USER_ID.getId(), 0, ParamCodeEnum.USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
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
            Page<LiveShopProductInfoDTO> page = productService.getProducts(userId, LiveShopProductState.NOT_VALID, start, num);

            if (page == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            result.addProperty("count", page.getCount());
            JsonArray products = new JsonArray();
            if (page.getList() == null) {
                result.add("products", products);
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
                return result;
            }

            for (LiveShopProductInfoDTO productDTO : page.getList()) {
                JsonObject productJson = new JsonObject();
                productJson.addProperty("productId", productDTO.getProductId());
                productJson.addProperty("productName", productDTO.getProductName());
                productJson.addProperty("productPrice", productDTO.getProductPrice());
                productJson.addProperty("productUrl", productDTO.getProductUrl() + "!256");
                productJson.addProperty("stockNum", productDTO.getStockNum());
                productJson.addProperty("owner", productDTO.getOwner());
                productJson.addProperty("supportReturn", productDTO.getSupportReturn());
                if(productDTO.getDistributorCommissionRate() != null) {
                    productJson.addProperty("distributorCommissionRate", productDTO.getDistributorCommissionRate());
                    productJson.addProperty("distributorCommissionAmount", productDTO.getDistributorCommissionAmount());
                }

                products.add(productJson);
            }

            result.add("products", products);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：liveShopService.getOffShelvesProducts(userId=%s, start=%s, num=%s)", userId, start, num), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取用户是否显示货架【51060537】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getShowShelfStatus(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParamCodeEnum.USER_ID.getId(), 0, ParamCodeEnum.USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {
            int showShelfStatus = productService.getShowShelfStatus(userId);

            result.addProperty("showShelfStatus", showShelfStatus);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：liveShopService.getShowShelfStatus(userId=%s)", userId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 设置用户是否显示货架【51060538】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject setUpShowShelfStatus(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId, showShelfStatus;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParamCodeEnum.USER_ID.getId(), 0, ParamCodeEnum.USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            showShelfStatus = CommonUtil.getJsonParamInt(jsonObject, ParamCodeEnum.SHOW_SHELF_STATUS.getId(), 0, ParamCodeEnum.SHOW_SHELF_STATUS.getErrorCode(), 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {
            boolean setUpResult = productService.setUpShowShelfStatus(userId, showShelfStatus);

            result.addProperty("setUpResult", setUpResult);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：liveShopService.getShowShelfStatus(userId=%s)", userId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取用户商品管理页信息【51060545】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getProductManagerInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParamCodeEnum.USER_ID.getId(), 0, ParamCodeEnum.USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {
            ProductManagerInfoDTO productManagerInfoDTO = productService.getProductManagerInfo(userId);

            if(productManagerInfoDTO != null) {
                result.addProperty("sellingProjectNum", productManagerInfoDTO.getSellingProductCount());
                result.addProperty("offShelvesProjectNum", productManagerInfoDTO.getOffShelvesProductCount());
            }
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：liveShopService.getProductManagerInfo(userId=%s)", userId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取用户订单管理页信息【51060547】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getOrderManagerInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParamCodeEnum.USER_ID.getId(), 0, ParamCodeEnum.USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {
            OrderManagerInfoDTO orderManagerInfoDTO = orderService.getOrderManagerInfo(userId);

            if(orderManagerInfoDTO != null) {
                result.addProperty("processingOrderNum", orderManagerInfoDTO.getProcessingOrderNum());
                result.addProperty("waitShipOrderNum", orderManagerInfoDTO.getWaitShipOrderNum());
                result.addProperty("waitPayOrderNum", orderManagerInfoDTO.getWaitPayOrderNum());
                result.addProperty("waitReciveOrderNum", orderManagerInfoDTO.getWaitReciveOrderNum());
                result.addProperty("refundingOrderNum", orderManagerInfoDTO.getRefundingOrderNum());
            }
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：liveShopService.getOrderManagerInfo(userId=%s)", userId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取用户分销订单明细【51060548】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getDistributionOrders(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId, state, start, num;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParamCodeEnum.USER_ID.getId(), 0, ParamCodeEnum.USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            state = CommonUtil.getJsonParamInt(jsonObject, ParamCodeEnum.STATE.getId(), 0, ParamCodeEnum.STATE.getErrorCode(), 0, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, PARAM_START, 0, null, 0, Integer.MAX_VALUE);
            num = CommonUtil.getJsonParamInt(jsonObject, "num", 20, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {
            Page<LiveShopOrderV2DTO> page = orderService.getDistributionOrders(state, userId, start, num);

            if(page != null) {
                result.addProperty("count", page.getCount());
                List<LiveShopOrderV2DTO> list = page.getList();
                JsonArray orders = new JsonArray();
                if(CollectionUtils.isNotEmpty(list)) {
                    for (LiveShopOrderV2DTO orderV2DTO : list) {
                        JsonObject orderJson = new JsonObject();
                        JsonArray products = new JsonArray();
                        orderJson.addProperty("orderNo", orderV2DTO.getOrderNo());
                        UserProfile userProfile = kkUserService.getUserProfile(orderV2DTO.getActorId());
                        if(userProfile != null) {
                            orderJson.addProperty("ownShopNickname", userProfile.getNickName());
                        }
                        orderJson.addProperty("payMoney", orderV2DTO.getPayMoney());
                        orderJson.addProperty("orderState", orderV2DTO.getOrderState());
                        orderJson.addProperty("settleStatus", orderV2DTO.getSettleStatus());
                        orderJson.addProperty("addTime", orderV2DTO.getAddTime().getTime());
                        for(LiveShopOrderItemV2DTO itemV2DTO : orderV2DTO.getOrderItems()) {
                            JsonObject itemJson = new JsonObject();
                            itemJson.addProperty("productId", itemV2DTO.getProductId());
                            itemJson.addProperty("productName", itemV2DTO.getProductName());
                            itemJson.addProperty("productUrl", itemV2DTO.getResourceUrl() + "!256");
                            itemJson.addProperty("distributorCommissionRate", itemV2DTO.getDistributorCommissionRate());
                            itemJson.addProperty("distributorCommissionAmount", itemV2DTO.getDistributorCommissionAmount());
                            products.add(itemJson);
                        }
                        orderJson.add("products", products);

                        orders.add(orderJson);
                    }

                    result.add("orders", orders);
                }
            }
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：liveShopService.getDistributionOrders(userId=%s, state=%s)", userId, state), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 提交订单【51060549】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject submitOrder(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }

        int userId, sellerId, productId, count, addressId, orderChannel;
        String couponCode;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, "5106051301", 1, Integer.MAX_VALUE);
            sellerId = CommonUtil.getJsonParamInt(jsonObject, "sellerId", 0, "5106051310", 0, Integer.MAX_VALUE);
            productId = CommonUtil.getJsonParamInt(jsonObject, "productId", 0, "5106051303", 1, Integer.MAX_VALUE);
            count = CommonUtil.getJsonParamInt(jsonObject, "count", 0, "5106051304", 1, Integer.MAX_VALUE);
            addressId = CommonUtil.getJsonParamInt(jsonObject, "addressId", 0, "5106051305", 1, Integer.MAX_VALUE);
            orderChannel = CommonUtil.getJsonParamInt(jsonObject, "orderChannel", 0, "5106051311", 1, Integer.MAX_VALUE);
            couponCode = CommonUtil.getJsonParamString(jsonObject, "couponCode", "", null, 0, 100);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        }

        try {

            Result<String> addOrderResult = orderService.submitOrderV2(userId, sellerId, productId, count, addressId,orderChannel,couponCode);
            if (addOrderResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            String code = addOrderResult.getCode();
            if (LiveShopErrorMsg.DELETED_PRODUCT_CODE.equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.DELETED_PRODUCT);
                return result;
            } else if (LiveShopErrorMsg.NOT_VALID_PRODUCT_CODE.equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.NOT_VALID_PRODUCT);
                return result;
            } else if (LiveShopErrorMsg.STOCK_NOT_FULL_CODE.equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.STOCK_NOT_FULL);
                return result;
            } else if("NOT_ACTOR_COUPON".equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.NOT_ACTOR_COUPON);
                return result;
            } else if("COUPON_EXPIRED_OR_AMOUNT_ERROR".equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.COUPON_EXPIRED_OR_AMOUNT_ERROR);
                return result;
            } else if("COUPON_NOT_EXIST".equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.NOT_EXIST_COUPON);
                return result;
            } else if("DISABLE".equals(code)) {
                result.addProperty("TagCode", TagCodeEnum.COUPON_DISABLE);
                return result;
            } else if("AMOUNT_ERROR".equals(code)) {
                result.addProperty("TagCode",TagCodeEnum.AMOUNT_ERROR);
                return result;
            }  else if (!CommonStateCode.SUCCESS.equals(code)){
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            result.addProperty(PARAM_ORDER_NO, addOrderResult.getData());
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：liveShopService.submitOrder(userId=%s, sellerId=%s, productId=%s, count=%s, addressId=%s, orderChannel=%s)",
                    userId, sellerId, productId, count, addressId, orderChannel), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 支付订单(校验收货地址是否填写 0元订单直接支付【51060599】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject payOrder(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }

        int userId;
        String orderNo;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, "5106051301", 1, Integer.MAX_VALUE);
            orderNo = CommonUtil.getJsonParamString(jsonObject, "orderNo", "", "5106050302", 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        }

        try {

            Result<Integer> payOrderResult = orderService.payOrder(userId, orderNo);
            if (payOrderResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            String code = payOrderResult.getCode();
            if("2".equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, "5106050303");
                return result;
            } else if("4".equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, "5106050304");
                return result;
            } else if (!CommonStateCode.SUCCESS.equals(code)){
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            result.addProperty("orderState", payOrderResult.getData());
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：liveShopService.payOrder(userId=%s, orderNo=%s)",
                    userId, orderNo), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取订单列表V2【51060550】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getOrdersV2(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId, state, start, num, type;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParamCodeEnum.USER_ID.getId(), 0, ParamCodeEnum.USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            state = CommonUtil.getJsonParamInt(jsonObject, ParamCodeEnum.STATE.getId(), 0, null, 0, Integer.MAX_VALUE);
            type = CommonUtil.getJsonParamInt(jsonObject, "type", 1, null, 0, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, PARAM_START, 0, null, 0, Integer.MAX_VALUE);
            num = CommonUtil.getJsonParamInt(jsonObject, "num", 20, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {
            Page<LiveShopOrderV3DTO> page = orderService.getOrdersV2(state, userId, type, start, num);

            if(page != null) {
                result.addProperty("count", page.getCount());
                List<LiveShopOrderV3DTO> list = page.getList();
                JsonArray orders = new JsonArray();
                if(CollectionUtils.isNotEmpty(list)) {
                    for (LiveShopOrderV3DTO orderV2DTO : list) {
                        JsonObject orderJson = new JsonObject();
                        JsonArray products = new JsonArray();
                        orderJson.addProperty("orderNo", orderV2DTO.getOrderNo());
                        orderJson.addProperty("orderMoney", orderV2DTO.getOrderMoney());
                        orderJson.addProperty("expressMoney", orderV2DTO.getExpressMoney());
                        orderJson.addProperty("sellerNickname", orderV2DTO.getSellerNickname());
                        orderJson.addProperty("orderType", orderV2DTO.getOrderType());
                        orderJson.addProperty("orderState", orderV2DTO.getOrderState());
                        orderJson.addProperty("waitPayMoney", orderV2DTO.getWaitPayMoney());
                        orderJson.addProperty("payMoney", orderV2DTO.getPayMoney());
                        orderJson.addProperty("refundMoney", orderV2DTO.getRefundMoney());
                        if(orderV2DTO.getCouponAmount() != null){
                            orderJson.addProperty("couponAmount",orderV2DTO.getCouponAmount());
                        }

                        for(LiveShopOrderItemV2DTO itemV2DTO : orderV2DTO.getOrderItems()) {
                            JsonObject itemJson = new JsonObject();
                            if(itemV2DTO.getProductId() != null) {
                                itemJson.addProperty("productId", itemV2DTO.getProductId());
                            }
                            itemJson.addProperty("productName", itemV2DTO.getProductName());
                            itemJson.addProperty("productUrl", itemV2DTO.getResourceUrl() + "!256");
                            itemJson.addProperty("productPrice", itemV2DTO.getProductPrice());
                            itemJson.addProperty("productCount", itemV2DTO.getProductCount());
                            if(itemV2DTO.getSupportReturn() != null) {
                                itemJson.addProperty("supportReturn", itemV2DTO.getSupportReturn());
                            }
                            if(itemV2DTO.getDistributorCommissionAmount() != null) {
                                itemJson.addProperty("distributorCommissionAmount", itemV2DTO.getDistributorCommissionAmount());
                            }
                            products.add(itemJson);
                        }
                        orderJson.add("products", products);

                        // 获取收货人姓名
                        Result<HistDeliveryDO> deliveryDOResult = histDeliveryService.getHistDeliveryDO(orderV2DTO.getOrderId(), 3);
                        if (deliveryDOResult != null && deliveryDOResult.getData() != null) {
                            HistDeliveryDO histDeliveryDO = deliveryDOResult.getData();
                            orderJson.addProperty("consigneeName", histDeliveryDO.getConsigneeName());
                        }

                        orders.add(orderJson);
                    }

                    result.add("orders", orders);
                }
            }
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：liveShopService.getOrdersV2(userId=%s, state=%s)", userId, state), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取订单详情V2【51060551】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getOrderInfoV2(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
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
            LiveShopOrderV3DTO orderV2DTO = orderService.getOrderInfoV2(orderNo);
            if (orderV2DTO == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            // 获取主播客服ID
            List<Integer> subShopIds;
            try {
                subShopIds = liveShopService.getSubShopIds(orderV2DTO.getActorId());
            } catch (Exception e) {
                logger.error("liveShopService.getSubShopIds(" + orderV2DTO.getActorId() + ")", e);
                subShopIds = Lists.newArrayList();
            }
            if (!orderV2DTO.getActorId().equals(userId)
                    && !orderV2DTO.getUserId().equals(userId)
                    && (subShopIds.isEmpty() || !subShopIds.contains(userId))) {
                result.addProperty(ParameterKeys.TAG_CODE, "5106050303");
                return result;
            }
            UserAddressDO addressDO = null;
            Result<UserAddressDO> addressResult = userAddressService.getUserDefaultAddressDOByUserId(orderV2DTO.getUserId());
            if (addressResult != null && CommonStateCode.SUCCESS.equals(addressResult.getCode())) {
                addressDO = addressResult.getData();
            }

            LiveShopTF.orderV2Info2Json(result, orderV2DTO, addressDO, subShopIds);

            // 获取供货商手机号
            Result<LiveShopInfoDTO> shopInfoResult = liveShopService.getShopInfo(orderV2DTO.getActorId(), LiveShopMode.AUCTION_SHOP);
            if (shopInfoResult != null && shopInfoResult.getData() != null && shopInfoResult.getData().getMobileNo() != null) {
                result.addProperty("supplierMobile", shopInfoResult.getData().getMobileNo());
            }

            // 支付方式
            if (orderV2DTO.getPaymentMode() != null && orderV2DTO.getPaymentMode() > 0) {
                Result<ConfPaymentInfoDto> rechargeResult = rechargeService.getConfPaymentInfoV2(1, orderV2DTO.getPaymentMode());
                if (rechargeResult != null && CommonStateCode.SUCCESS.equals(rechargeResult.getCode())) {
                    result.addProperty("paymentName", rechargeResult.getData().getPaymentName());
                }
            }

            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：liveShopService.getOrderInfoV2(userId=%s, orderNo=%s)", userId, orderNo), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取商家服务页信息【51060553】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getSellerManagerInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParamCodeEnum.USER_ID.getId(), 0, ParamCodeEnum.USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        try {
            SellerManagerInfoDTO sellerManagerInfoDTO = orderService.getSellerManagerInfo(userId);

            if(sellerManagerInfoDTO != null) {
                result.addProperty("processingOrderNum", sellerManagerInfoDTO.getProcessingOrderNum());
                result.addProperty("unreadMessageNum", sellerManagerInfoDTO.getUnreadMessageNum());
            }
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：liveShopService.getSellerManagerInfo(userId=%s)", userId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 下架商品(51060542)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject offTheShelfProduct(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        int userId;
        int productId;

        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "03040002", 1, Integer.MAX_VALUE);
            productId = CommonUtil.getJsonParamInt(jsonObject, "productId", 0, TagCodeEnum.INVALID_PARAMETERS, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        Result<Boolean> resultCode = productService.offTheShelfProduct(userId,productId);
        if("PRODUCT_NOT_EXIST".equals(resultCode.getCode())){
            result.addProperty("TagCode", TagCodeEnum.NOT_EXIST_PRODUCT);
            return result;
        }
        if("NO_PERMISSIONS".equals(resultCode.getCode())){
            result.addProperty("TagCode", TagCodeEnum.PERMISSIONS_ERROR);
            return result;
        }
        if("SQL_ERROR".equals(resultCode.getCode())){
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
            return result;
        }
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 上架商品(51060543)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject onTheShelfProduct(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        int userId;
        int productId;

        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "03040002", 1, Integer.MAX_VALUE);
            productId = CommonUtil.getJsonParamInt(jsonObject, "productId", 0, TagCodeEnum.INVALID_PARAMETERS, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        Result<Boolean> resultCode = productService.onTheShelfProduct(userId,productId);
        if("PRODUCT_NOT_EXIST".equals(resultCode.getCode())){
            result.addProperty("TagCode", TagCodeEnum.NOT_EXIST_PRODUCT);
            return result;
        }
        if("NO_PERMISSIONS".equals(resultCode.getCode())){
            result.addProperty("TagCode", TagCodeEnum.PERMISSIONS_ERROR);
            return result;
        }
        if ("NOT_SELLER".equals(resultCode.getCode())) {
            result.addProperty("TagCode", TagCodeEnum.NOT_EXIST_SALE_ACTOR);
            return result;
        }
        if("SQL_ERROR".equals(resultCode.getCode())){
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
            return result;
        }
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 删除商品(51060544)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject removeProduct(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        int userId;
        int productId;

        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "03040002", 1, Integer.MAX_VALUE);
            productId = CommonUtil.getJsonParamInt(jsonObject, "productId", 0, TagCodeEnum.INVALID_PARAMETERS, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        LiveShopProductDetailDTO liveShopProductDetailDTO = productService.getProductDetail(productId);
        if(liveShopProductDetailDTO == null){
            result.addProperty("TagCode", TagCodeEnum.NOT_EXIST_PRODUCT);
            return result;
        }

        Result<Boolean> resultCode = productService.deleteProduct(userId, productId);
        if("SQL_ERROR".equals(resultCode.getCode())){
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
            return result;
        }
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 生成优惠券(51060554)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject addShopCoupon(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        int  couponType;
        long couponAmount;
        int couponCount;
        int userLimitCount;
        long reductionAmount;
        Integer couponId = null;
        long startTime;
        long endTime;
        long receiveStartTime;
        long receiveEndTime;

        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0,
                    ParamCodeEnum.USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);

            JsonElement couponIdElement = jsonObject.get("couponId");
            if(couponIdElement != null){
                couponId = couponIdElement.getAsInt();
            }

            couponType = CommonUtil.getJsonParamInt(jsonObject, "couponType", 0,
                    ParamCodeEnum.COUPON_TYPE.getErrorCode(), 0, 10);

            couponCount = CommonUtil.getJsonParamInt(jsonObject, "couponCount", 0,
                    TagCodeEnum.COUNT_ERROR, 1, Integer.MAX_VALUE);

            couponAmount = CommonUtil.getJsonParamLong(jsonObject, "couponAmount", 0,
                    ParamCodeEnum.COUPON_AMOUNT.getErrorCode(), 1, Long.MAX_VALUE);

            JsonElement element = jsonObject.get("reductionAmount");
            if(element == null){
                result.addProperty("TagCode",ParamCodeEnum.REDUCTION_AMOUNT.getErrorCode());
                return result;
            } else {
                reductionAmount = element.getAsLong();
            }

            userLimitCount = CommonUtil.getJsonParamInt(jsonObject, "userLimitCount", 0,
                    ParamCodeEnum.USER_LIMIT_COUNT.getErrorCode(), 1, 1000);

            startTime =  CommonUtil.getJsonParamLong(jsonObject, "startTime", 0,
                    ParamCodeEnum.START_TIME.getErrorCode(), 1, Long.MAX_VALUE);

            endTime = CommonUtil.getJsonParamLong(jsonObject, "endTime", 0,
                    ParamCodeEnum.END_TIME.getErrorCode(), 1, Long.MAX_VALUE);

            receiveStartTime = CommonUtil.getJsonParamLong(jsonObject, "receiveStartTime", 0,
                    ParamCodeEnum.RECEIVE_START_TIME.getErrorCode(), 1, Long.MAX_VALUE);

            receiveEndTime = CommonUtil.getJsonParamLong(jsonObject, "receiveEndTime", 0,
                    ParamCodeEnum.RECEIVE_END_TIME.getErrorCode(), 1, Long.MAX_VALUE);

        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        Date startTimeDate = new Date(startTime);
        Date endTimeDate  = new Date(endTime);
        Date receiveStartTimeDate = new Date(receiveStartTime);
        Date receiveEndTimeDate = new Date(receiveEndTime);

        ResCouponInfoDTO resCouponInfoDTO = new ResCouponInfoDTO();
        resCouponInfoDTO.setUserLimitCount(userLimitCount);
        resCouponInfoDTO.setStartTime(startTimeDate);
        resCouponInfoDTO.setEndTime(endTimeDate);
        resCouponInfoDTO.setReceiveStartTime(receiveStartTimeDate);
        resCouponInfoDTO.setReceiveEndTime(receiveEndTimeDate);
        resCouponInfoDTO.setReductionAmount(reductionAmount);
        resCouponInfoDTO.setCouponAmount(couponAmount);
        resCouponInfoDTO.setUserId(userId);
        resCouponInfoDTO.setdType(couponType);
        resCouponInfoDTO.setCouponCount(couponCount);

        if (couponId != null) {
            if(couponId <= 0){
                result.addProperty("TagCode", TagCodeEnum.NOT_EXIST_COUPON);
                return result;
            }
            Result<Boolean> couponResult = couponService.updateCouponInfo(couponId, userId, resCouponInfoDTO);
            if (CommonStateCode.SUCCESS.equals(couponResult.getCode())) {
                productService.setUpShowShelfStatus(userId,1);
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            } else {
                if("NOT_EXIST".equals(couponResult.getCode())) {
                    result.addProperty("TagCode", TagCodeEnum.NOT_EXIST_COUPON);
                    return result;
                } else if("EXPIRED".equals(couponResult.getCode())) {
                    result.addProperty("TagCode", TagCodeEnum.EXPIRED);
                    return result;
                } else if("SQL_ERROR".equals(couponResult.getCode())) {
                    result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
                    return result;
                } else if ("NO_PERMISSIONS".equals(couponResult.getCode())) {
                    result.addProperty("TagCode",TagCodeEnum.PERMISSIONS_ERROR);
                    return result;
                } else if("AMOUNT_ERROR".equals(couponResult.getCode())) {
                    result.addProperty("TagCode",TagCodeEnum.AMOUNT_ERROR);
                    return result;
                } else if("COUNT_ERROR".equals(couponResult.getCode())) {
                    result.addProperty("TagCode", TagCodeEnum.COUNT_ERROR);
                    return result;
                } else if("DISABLE".equals(couponResult.getCode())) {
                    result.addProperty("TagCode", TagCodeEnum.COUPON_DISABLE);
                    return result;
                } else if ("TIME_ERROR".equals(couponResult.getCode())) {
                    result.addProperty("TagCode", TagCodeEnum.TIME_ERROR);
                    return result;
                } else {
                    result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                    return result;
                }
            }
        } else {
            Result<Boolean> couponResult = couponService.addCouponInfo(resCouponInfoDTO);
            if (CommonStateCode.SUCCESS.equals(couponResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            } else {
                if("TIME_ERROR".equals(couponResult.getCode())) {
                    result.addProperty("TagCode", TagCodeEnum.TIME_ERROR);
                    return result;
                } else if("AMOUNT_ERROR".equals(couponResult.getCode())) {
                    result.addProperty("TagCode", TagCodeEnum.AMOUNT_ERROR);
                    return result;
                } else if("COUNT_ERROR".equals(couponResult.getCode())) {
                    result.addProperty("TagCode", TagCodeEnum.COUNT_ERROR);
                    return result;
                } else if("SQL_ERROR".equals(couponResult.getCode())) {
                    result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
                    return result;
                } else {
                    result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                    return result;
                }
            }
        }
    }

    /**
     * 获取优惠券详情(51060555)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getShopCouponDetail(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        int couponId;

        try {
            couponId = CommonUtil.getJsonParamInt(jsonObject, "couponId", 0, TagCodeEnum.PARAMETER_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        ResCouponInfoDTO resCouponInfoDTO = couponService.getResCouponInfo(couponId);
        if(resCouponInfoDTO == null){
            result.addProperty("TagCode", TagCodeEnum.NOT_EXIST_COUPON);
            return result;
        }
        result.addProperty("couponId",resCouponInfoDTO.getCouponId());
        result.addProperty("couponType",resCouponInfoDTO.getdType());
        result.addProperty("usingType",resCouponInfoDTO.getUsingType());
        result.addProperty("couponAmount",resCouponInfoDTO.getCouponAmount());
        result.addProperty("couponCount",resCouponInfoDTO.getCouponCount());
        result.addProperty("userLimitCount",resCouponInfoDTO.getUserLimitCount());
        result.addProperty("reductionAmount",resCouponInfoDTO.getReductionAmount());
        result.addProperty("startTime",resCouponInfoDTO.getStartTime().getTime());
        result.addProperty("endTime",resCouponInfoDTO.getEndTime().getTime());
        result.addProperty("receiveStartTime",resCouponInfoDTO.getReceiveStartTime().getTime());
        result.addProperty("receiveEndTime",resCouponInfoDTO.getReceiveEndTime().getTime());

        result.addProperty("TagCode",TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 领取优惠券(51060556)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject receiveCoupon(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        int couponId;

        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, ParamCodeEnum.USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            couponId = CommonUtil.getJsonParamInt(jsonObject, "couponId", 0, TagCodeEnum.PARAMETER_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        Result<Boolean> couponResult = couponService.receiveCoupon(userId,couponId);
        if (CommonStateCode.SUCCESS.equals(couponResult.getCode())) {
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            int status = couponService.getUserReceiveCouponStatus(couponId,userId);
            result.addProperty("status",status);
            return result;
        } else {
            if("NOT_EXIST".equals(couponResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.NOT_EXIST_COUPON);
                return result;
            } else if("EXPIRED".equals(couponResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.EXPIRED);
                return result;
            } else if("CAN_NOT_RECEIVE_SELF_COUPON".equals(couponResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.CAN_NOT_RECEIVE_SELF_COUPON);
                return result;
            } else if("EXCEED_COUNT".equals(couponResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.EXCEED_COUNT);
                return result;
            } else if("EXCEED_USER_LIMIT".equals(couponResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.EXCEED_USER_LIMIT_COUNT);
                return result;
            } else if("REPEAT_RECEIVE".equals(couponResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.REPEAT_RECEIVE);
                return result;
            } else if("DISABLE".equals(couponResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.COUPON_DISABLE);
                return result;
            } else if ("SQL_ERROR".equals(couponResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
                return result;
            } else {
                result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
        }
    }

    /**
     * 获取用户优惠券数量(51060557)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getUserAllCouponCount(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, ParamCodeEnum.USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        UserCouponCountDTO userCouponCountDTO = couponService.getUserAllCouponCount(userId);
        if(userCouponCountDTO != null){
            result.addProperty("unusedCount",userCouponCountDTO.getUserUnusedCount());
            result.addProperty("haveBeenUsedCount",userCouponCountDTO.getUserHaveBeenUsedCount());
            result.addProperty("haveExpiredCount",userCouponCountDTO.getUserHaveExpiredCount());
        }
        result.addProperty("TagCode",TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 获取用户优惠券(51060558)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getUserCoupon(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        int userId;
        int state;
        int pageIndex;
        int countPerPage;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, ParamCodeEnum.USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            state = CommonUtil.getJsonParamInt(jsonObject, "state", 0, TagCodeEnum.PARAMETER_MISSING, -1, 10);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 0, TagCodeEnum.PARAMETER_MISSING, 1, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 0, TagCodeEnum.PARAMETER_MISSING, 1, 200);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        JsonArray jsonArray = new JsonArray();
        Page<ResCouponInfoDTO> page = couponService.getUserCouponList(userId,state,pageIndex,countPerPage);
        if(!CollectionUtils.isEmpty(page.getList())){
            for(ResCouponInfoDTO item : page.getList()){
                UserProfile userProfile = kkUserService.getUserProfile(item.getUserId());
                if(userProfile != null){
                    JsonObject json = new JsonObject();
                    json.addProperty("shopRoomId",userProfile.getUserId());
                    json.addProperty("shopNickname",userProfile.getNickName());
                    json.addProperty("couponAmount",item.getCouponAmount());
                    json.addProperty("couponType",item.getdType());
                    json.addProperty("reductionAmount",item.getReductionAmount());
                    json.addProperty("startTime",item.getStartTime().getTime());
                    json.addProperty("endTime",item.getEndTime().getTime());
                    jsonArray.add(json);
                }
            }
        }
        result.addProperty("count",page.getCount() != null ? page.getCount() : 0);
        result.add("list",jsonArray);
        result.addProperty("TagCode",TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 获取商家管理列表优惠券数量(51060563)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getSellerAllCouponManageCount(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, ParamCodeEnum.USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        UserCouponCountDTO userCouponCountDTO = couponService.getSellerAllCouponManageCount(userId);
        if(userCouponCountDTO != null){
            result.addProperty("receivingCount",userCouponCountDTO.getSellerReceivingCount());
            result.addProperty("beReceiveCount",userCouponCountDTO.getSellerBeReceiveCount());
            result.addProperty("receivedCount",userCouponCountDTO.getSellerReceivedCount());
            result.addProperty("expiredCount",userCouponCountDTO.getSellerExpiredCount());
        }
        result.addProperty("TagCode",TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 获取商家管理列表优惠券(51060559)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getSellerCouponManageList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        int userId;
        int state;
        int pageIndex;
        int countPerPage;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, ParamCodeEnum.USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            state = CommonUtil.getJsonParamInt(jsonObject, "state", 0, TagCodeEnum.PARAMETER_MISSING, 0, 10);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 0, TagCodeEnum.PARAMETER_MISSING, 1, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 0, TagCodeEnum.PARAMETER_MISSING, 1, 200);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        JsonArray jsonArray = new JsonArray();
        Page<ResCouponInfoDTO> page = couponService.getSellerCouponManageList(userId,state,pageIndex,countPerPage);
        if(!CollectionUtils.isEmpty(page.getList())){
            List<Integer> couponList;
            Map<Integer,Integer> usedMap = null;
            Map<Integer,Integer> receivedMap = null;
            if(state != SellerCouponState.BE_RECEIVE){
                couponList = new ArrayList<>(page.getList().size());
                for (ResCouponInfoDTO item: page.getList()) {
                    couponList.add(item.getCouponId());
                }
                usedMap = couponService.getCouponUsedCountByState(couponList,CouponUsingState.HAS_BEEN_USED);
                receivedMap = couponService.getCouponReceivedCountMap(couponList);
            }

            for(ResCouponInfoDTO item : page.getList()){
                JsonObject json = new JsonObject();
                json.addProperty("couponId",item.getCouponId());
                json.addProperty("couponAmount",item.getCouponAmount());
                json.addProperty("couponType",item.getdType());
                json.addProperty("reductionAmount",item.getReductionAmount());
                json.addProperty("startTime",item.getStartTime().getTime());
                json.addProperty("endTime",item.getEndTime().getTime());
                json.addProperty("receiveStartTime",item.getReceiveStartTime().getTime());
                json.addProperty("receiveEndTime",item.getReceiveEndTime().getTime());
                json.addProperty("userLimitCount",item.getUserLimitCount());
                json.addProperty("couponCount",item.getCouponCount());
                if(state == SellerCouponState.BE_RECEIVE){
                    json.addProperty("receiveCount",0);
                    json.addProperty("usedCount",0);
                } else {
                    if(!org.springframework.util.CollectionUtils.isEmpty(usedMap)){
                        if(usedMap.containsKey(item.getCouponId())){
                            json.addProperty("usedCount",usedMap.get(item.getCouponId()));
                        } else {
                            json.addProperty("usedCount",0);
                        }
                    } else {
                        json.addProperty("usedCount",0);
                    }
                    if(!org.springframework.util.CollectionUtils.isEmpty(receivedMap)){
                        if(receivedMap.containsKey(item.getCouponId())){
                            json.addProperty("receiveCount",receivedMap.get(item.getCouponId()));
                        } else {
                            json.addProperty("receiveCount",0);
                        }
                    } else {
                        json.addProperty("receiveCount",0);
                    }
                }
                jsonArray.add(json);
            }
        }
        result.add("list",jsonArray);
        result.addProperty("count",page.getCount() != null ? page.getCount() : 0);
        result.addProperty("TagCode",TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 停用优惠券(51060560)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject disableCoupon(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        int userId;
        int couponId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, ParamCodeEnum.USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            couponId = CommonUtil.getJsonParamInt(jsonObject, "couponId", 0, TagCodeEnum.PARAMETER_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        Result<Boolean> couponResult = couponService.disableCoupon(userId,couponId);
        if(CommonStateCode.SUCCESS.equals(couponResult.getCode())){
            result.addProperty("TagCode",TagCodeEnum.SUCCESS);
            return result;
        } else {
            if("NOT_EXIST".equals(couponResult.getCode())){
                result.addProperty("TagCode",TagCodeEnum.NOT_EXIST_COUPON);
                return result;
            } else if("NO_PERMISSIONS".equals(couponResult.getCode())){
                result.addProperty("TagCode",TagCodeEnum.PERMISSIONS_ERROR);
                return result;
            } else if("SQL_ERROR".equals(couponResult.getCode())){
                result.addProperty("TagCode",TagCodeEnum.EXECSQL_EXCEPTION);
                return result;
            } else {
                result.addProperty("TagCode",TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
        }
    }

    /**
     * 获取商家优惠券(51060561)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getSellerCoupon(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        int userId;
        int sellerId;
        int pageIndex;
        int countPerPage;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 0, Integer.MAX_VALUE);
            sellerId = CommonUtil.getJsonParamInt(jsonObject, "sellerId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 0, "03040002", 1, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 0, "03040002", 1, 200);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }
        JsonArray jsonArray = new JsonArray();
        Page<ResCouponInfoDTO> page = couponService.getSellerCouponList(sellerId,pageIndex,countPerPage);
        if(!CollectionUtils.isEmpty(page.getList())){
            Map<Integer,Integer> userReceivedCouponCount = null;
            if(userId > 0){
                List<Integer> list = new ArrayList<>(page.getList().size());
                for(ResCouponInfoDTO item : page.getList()){
                    list.add(item.getCouponId());
                }
                userReceivedCouponCount = couponService.getUserReceivedCouponCount(userId,list);
            }

            for(ResCouponInfoDTO item : page.getList()){
                JsonObject json = new JsonObject();
                json.addProperty("couponId",item.getCouponId());
                json.addProperty("couponAmount",item.getCouponAmount());
                json.addProperty("couponType",item.getdType());
                json.addProperty("reductionAmount",item.getReductionAmount());
                json.addProperty("startTime",item.getStartTime().getTime());
                json.addProperty("endTime",item.getEndTime().getTime());
                if(item.getCouponState() == 1){
                    json.addProperty("state",2);
                } else {
                    if(userReceivedCouponCount != null){
                        Integer receivedCount = userReceivedCouponCount.get(item.getCouponId());
                        if(receivedCount != null && receivedCount >= item.getUserLimitCount()){
                            json.addProperty("state",1);
                        } else {
                            json.addProperty("state",0);
                        }
                    } else {
                        json.addProperty("state",0);
                    }
                }
                jsonArray.add(json);
            }
        }

        result.addProperty("count",page.getCount() != null ? page.getCount() : 0);
        result.add("list",jsonArray);
        result.addProperty("TagCode",TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 用户在当前直播间所能领取优惠券总额(51060562)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getLiveRoomUserCouponAmount(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        int userId;
        int roomId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 0, Integer.MAX_VALUE);
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        long amount = couponService.getLiveRoomUserCouponAmount(roomId,userId);
        result.addProperty("amount",amount);
        result.addProperty("TagCode",TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 获取商品详情页优惠券列表(51060564)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getProductDetailCouponList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        int userId;
        int productId;
        int sellerId;
        int pageIndex;
        int countPerPage;

        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 0, Integer.MAX_VALUE);
            productId = CommonUtil.getJsonParamInt(jsonObject, "productId", 0, TagCodeEnum.PARAMETER_MISSING, 1, Integer.MAX_VALUE);
            sellerId = CommonUtil.getJsonParamInt(jsonObject, "sellerId", 0, TagCodeEnum.PARAMETER_MISSING, 1, Integer.MAX_VALUE);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 0, TagCodeEnum.PARAMETER_MISSING, 1, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 0, TagCodeEnum.PARAMETER_MISSING, 1, 200);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        LiveShopProductDetailDTO liveShopProductDetailDTO = productService.getProductDetail(productId);
        if(liveShopProductDetailDTO == null){
            result.addProperty("TagCode",TagCodeEnum.NOT_EXIST_PRODUCT);
            return result;
        }

        JsonArray jsonArray = new JsonArray();
        Page<ResCouponInfoDTO> page = couponService.getSellerCouponList(liveShopProductDetailDTO.getActorId(),
                pageIndex, countPerPage);
        if (!CollectionUtils.isEmpty(page.getList())) {
            Map<Integer,Integer> userReceivedCouponCount = null;
            if (userId > 0) {
                List<Integer> list = new ArrayList<>(page.getList().size());
                for (ResCouponInfoDTO item : page.getList()) {
                    list.add(item.getCouponId());
                }
                userReceivedCouponCount = couponService.getUserReceivedCouponCount(userId, list);
            }
            for (ResCouponInfoDTO item : page.getList()) {
                JsonObject json = new JsonObject();
                json.addProperty("couponId",item.getCouponId());
                json.addProperty("couponAmount", item.getCouponAmount());
                json.addProperty("couponType", item.getdType());
                json.addProperty("reductionAmount", item.getReductionAmount());
                json.addProperty("startTime", item.getStartTime().getTime());
                json.addProperty("endTime", item.getEndTime().getTime());
                if (item.getCouponState() == 1) {
                    json.addProperty("state", 2);
                } else {
                    if(userReceivedCouponCount != null){
                        Integer receivedCount = userReceivedCouponCount.get(item.getCouponId());
                        if(receivedCount != null && receivedCount >= item.getUserLimitCount()){
                            json.addProperty("state",1);
                        } else {
                            json.addProperty("state",0);
                        }
                    } else {
                        json.addProperty("state",0);
                    }
                }
                jsonArray.add(json);
            }
        }
        result.addProperty("count", page.getCount() != null ? page.getCount() : 0);
        result.add("list", jsonArray);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 获取下单优惠券列表(51060565)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getOrderCouponList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }

        int userId;
        int pageIndex;
        int countPerPage;
        int productId;
        int productCount;
        int type = 0;
        int sellerId = 0;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, ParamCodeEnum.USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 0, TagCodeEnum.PARAMETER_MISSING, 1, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 0, TagCodeEnum.PARAMETER_MISSING, 1, 200);
            productId = CommonUtil.getJsonParamInt(jsonObject, "productId", 0, TagCodeEnum.PARAMETER_MISSING, 1, Integer.MAX_VALUE);
            productCount = CommonUtil.getJsonParamInt(jsonObject, "productCount", 0, TagCodeEnum.PARAMETER_MISSING, 1, 10000);
            type = CommonUtil.getJsonParamInt(jsonObject, "type", 0, TagCodeEnum.PARAMETER_MISSING, 0, 1);
            sellerId = CommonUtil.getJsonParamInt(jsonObject, "sellerId", 0, TagCodeEnum.PARAMETER_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        LiveShopProductDetailDTO liveShopProductDetailDTO = productService.getProductDetail(productId);
        if(liveShopProductDetailDTO == null){
            result.addProperty("TagCode",TagCodeEnum.NOT_EXIST_PRODUCT);
            return result;
        }
        JsonArray jsonArray = new JsonArray();
        long productAmount =  liveShopProductDetailDTO.getProductPrice() * productCount;
        Page<CouponOrderDTO> page;
        if(type == 1){
             page = couponService.getOrderAvailableCouponList(liveShopProductDetailDTO.getActorId(),userId,
                    productAmount, pageIndex,countPerPage);
        } else {
             page = couponService.getOrderDisabledCouponList(liveShopProductDetailDTO.getActorId(),userId,productAmount,
                     pageIndex,countPerPage);
        }
        if(!CollectionUtils.isEmpty(page.getList())){
            for(CouponOrderDTO item : page.getList()){
                JsonObject json = new JsonObject();
                json.addProperty("couponAmount",item.getCouponAmount());
                json.addProperty("couponType",item.getdType());
                json.addProperty("reductionAmount",item.getReductionAmount());
                json.addProperty("startTime",item.getStartTime().getTime());
                json.addProperty("endTime",item.getEndTime().getTime());
                json.addProperty("couponCode",item.getCouponCode());
                json.addProperty("state",type);
                jsonArray.add(json);
            }
        }
        result.addProperty("count",page.getCount() != null ? page.getCount() : 0);
        result.add("list",jsonArray);
        result.addProperty("TagCode",TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 获取下单优惠券可用数量(51060566)
     */
    public JsonObject getOrderCouponUserUsableCount(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }

        int userId;
        int productId;
        int productCount;
        int sellerId = 0;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, ParamCodeEnum.USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            productId = CommonUtil.getJsonParamInt(jsonObject, "productId", 0, TagCodeEnum.PARAMETER_MISSING, 1, Integer.MAX_VALUE);
            productCount = CommonUtil.getJsonParamInt(jsonObject, "productCount", 0, TagCodeEnum.PARAMETER_MISSING, 1, 10000);
            sellerId = CommonUtil.getJsonParamInt(jsonObject, "sellerId", 0, TagCodeEnum.PARAMETER_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        LiveShopProductDetailDTO liveShopProductDetailDTO = productService.getProductDetail(productId);
        if(liveShopProductDetailDTO == null){
            result.addProperty("TagCode",TagCodeEnum.NOT_EXIST_PRODUCT);
            return result;
        }
        long productAmount = liveShopProductDetailDTO.getProductPrice() * productCount;
        int count = couponService.getOrderAvailableCouponCount(liveShopProductDetailDTO.getActorId(),userId,productAmount);
        result.addProperty("count",count);
        result.addProperty("TagCode",TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 是否在该直播间第一次点击想要购买(51060567)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject isFirstTimeToBuy(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        int userId = 0;
        int roomId = 0;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0,
                    ParamCodeEnum.USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0,
                    ParamCodeEnum.ROOM_ID.getErrorCode(), 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
        }

        int state = couponService.isFirstTimeToBuy(userId, roomId);
        result.addProperty("state", state);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 更新订单优惠券(51060568)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject updateOrderCoupon(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        int userId;
        String couponCode;
        String orderNo;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, ParamCodeEnum.USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            couponCode = CommonUtil.getJsonParamString(jsonObject, "couponCode", "", TagCodeEnum.PARAMETER_MISSING, 1, 100);
            orderNo = CommonUtil.getJsonParamString(jsonObject, "orderNo", "", TagCodeEnum.PARAMETER_MISSING, 1, 500);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        Result<Boolean> couponResult = orderService.updateOrderCoupon(userId,orderNo,couponCode);
        if(CommonStateCode.SUCCESS.equals(couponResult.getCode())){
            result.addProperty("TagCode",TagCodeEnum.SUCCESS);
            return result;
        } else {
            String code = couponResult.getCode();
            if("NOT_EXIST".equals(code)){
                result.addProperty("TagCode",TagCodeEnum.NOT_EXIST_COUPON);
                return result;
            } else if("SQL_ERROR".equals(code)){
                result.addProperty("TagCode",TagCodeEnum.EXECSQL_EXCEPTION);
                return result;
            } else if("NOT_ACTOR_COUPON".equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.NOT_ACTOR_COUPON);
                return result;
            } else if("COUPON_NOT_EXIST".equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.NOT_EXIST_COUPON);
                return result;
            } else if("DISABLE".equals(code)) {
                result.addProperty("TagCode", TagCodeEnum.COUPON_DISABLE);
                return result;
            } else if("AMOUNT_ERROR".equals(code)) {
                result.addProperty("TagCode",TagCodeEnum.AMOUNT_ERROR);
                return result;
            } else if("NOT_WAIT_PAY".equals(code)) {
                result.addProperty("TagCode",TagCodeEnum.ORDER_STATE_ERROR);
                return result;
            } else if("HAS_BEEN_COUPON_CODE".equals(code)) {
                result.addProperty("TagCode",TagCodeEnum.HAS_BEEN_COUPON_CODE);
                return result;
            } else {
                result.addProperty("TagCode",TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
        }
    }
}

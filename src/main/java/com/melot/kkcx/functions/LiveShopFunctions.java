package com.melot.kkcx.functions;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.melot.kk.liveshop.api.constant.LiveShopErrorMsg;
import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.kk.liveshop.api.dto.LiveShopOrderDTO;
import com.melot.kk.liveshop.api.dto.LiveShopProductDTO;
import com.melot.kk.liveshop.api.service.LiveShopService;
import com.melot.kk.logistics.api.domain.UserAddressDO;
import com.melot.kk.logistics.api.service.UserAddressService;
import com.melot.kk.recharge.api.dto.ConfPaymentInfoDto;
import com.melot.kk.recharge.api.service.RechargeService;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kkcx.transform.LiveShopTF;
import com.melot.kktv.base.CommonStateCode;
import com.melot.kktv.base.Page;
import com.melot.kktv.base.Result;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ParameterKeys;
import com.melot.kktv.util.TagCodeEnum;

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
            Result<String> addOrderResult = liveShopService.addAuctionOrder(userId, auctionId, resourceId);
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
            if (!orderDTO.getActorId().equals(userId)
                    && !orderDTO.getUserId().equals(userId)) {
                result.addProperty(ParameterKeys.TAG_CODE, "5106050303");
                return result;
            }
            UserAddressDO addressDO = null;
            Result<UserAddressDO> addressResult = userAddressService.getUserDefaultAddressDOByUserId(orderDTO.getUserId());
            if (addressResult != null && CommonStateCode.SUCCESS.equals(addressResult.getCode())) {
                addressDO = addressResult.getData();
            }
            
            LiveShopTF.orderInfo2Json(result, orderDTO, addressDO);
            
            // 支付方式
            if (orderDTO.getPaymentMode() != null && orderDTO.getPaymentMode() > 0) {
                Result<ConfPaymentInfoDto> rechargeResult = rechargeService.getConfPaymentInfo(orderDTO.getPaymentMode());
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
                
                int relationId = liveShopOrderDTO.getUserId().equals(userId) 
                        ? liveShopOrderDTO.getActorId() : liveShopOrderDTO.getUserId();
                UserProfile userProfile = kkUserService.getUserProfile(relationId);
                orderDTOJson.addProperty("relationId", relationId);
                orderDTOJson.addProperty("nickname", userProfile.getNickName());
                
                // 获取支付
                if (liveShopOrderDTO.getPaymentMode() != null && liveShopOrderDTO.getPaymentMode() > 0) {
                    Result<ConfPaymentInfoDto> rechargeResult = rechargeService.getConfPaymentInfo(liveShopOrderDTO.getPaymentMode());
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
            Result<String> addOrderResult = liveShopService.addDistributorOrder(userId, distributorId, productId, count, addressId);
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
}

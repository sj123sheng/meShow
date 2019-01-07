package com.melot.kkcx.transform;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.kk.liveshop.api.constant.LiveShopOrderState;
import com.melot.kk.liveshop.api.dto.*;
import com.melot.kk.logistics.api.domain.HistDeliveryDO;
import com.melot.kk.logistics.api.domain.UserAddressDO;
import com.melot.kktv.util.StringUtil;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

public class LiveShopTF {

    private LiveShopTF() {}
    
    public static void orderInfo2Json(JsonObject result, LiveShopOrderDTO orderDTO, UserAddressDO addressDO) {
        result.addProperty("sellerId", orderDTO.getActorId());
        result.addProperty("buyerId", orderDTO.getUserId());
        
        result.addProperty("orderNo", orderDTO.getOrderNo());
        result.addProperty("expressMoney", orderDTO.getExpressMoney());
        result.addProperty("orderMoney", orderDTO.getOrderMoney());
        result.addProperty("orderType", orderDTO.getOrderType());
        
        result.addProperty("distributorId", orderDTO.getDistributorId() == null ? 0 : orderDTO.getDistributorId());
        if (orderDTO.getOrderState().equals(LiveShopOrderState.WAIT_RETURN)) {
            // 管理后台挂起的订单，做为申请退款的订单处理
            result.addProperty("orderState", LiveShopOrderState.APPLY_REFUND);
        } else {
            result.addProperty("orderState", orderDTO.getOrderState());
        }
        result.addProperty("addTime", orderDTO.getAddTime().getTime());
        
        if (addressDO != null && 
                !StringUtil.strIsNull(orderDTO.getConsigneeName())) {
            JsonObject addrInfo = new JsonObject();
            addrInfo.addProperty("addressId", addressDO.getAddressId());
            addrInfo.addProperty("consigneeName", orderDTO.getConsigneeName());
            addrInfo.addProperty("consigneeMobile", orderDTO.getConsigneeMobile());
            addrInfo.addProperty("detailAddress", orderDTO.getDetailAddress());
            result.add("addrInfo", addrInfo);
        }
        
        if (!StringUtil.strIsNull(orderDTO.getWaybillNumber())
                && !StringUtil.strIsNull(orderDTO.getCourierCompany())) {
            JsonObject expressInfo = new JsonObject();
            expressInfo.addProperty("waybillNumber", orderDTO.getWaybillNumber());
            expressInfo.addProperty("courierCompany", orderDTO.getCourierCompany());
            result.add("expressInfo", expressInfo);
        }
        
        if (orderDTO.getApplyRefundMoney() > 0) {
            JsonObject refundInfo = new JsonObject();
            refundInfo.addProperty("refundPrice", orderDTO.getApplyRefundMoney());
            if (orderDTO.getApplyRefundDesc() != null) {
                refundInfo.addProperty("refundDesc", orderDTO.getApplyRefundDesc());
            }
            
            JsonArray refundUrls = new JsonArray();
            if (orderDTO.getOrderPictures() != null) {
                for (LiveShopOrderPictureDTO pictureDTO : orderDTO.getOrderPictures()) {
                    JsonObject urlJson = new JsonObject();
                    urlJson.addProperty("phone_big", pictureDTO.getResourceUrl());
                    urlJson.addProperty("phone_small", pictureDTO.getResourceUrl() + "!256");
                    refundUrls.add(urlJson);
                }
            }
            refundInfo.add("refundUrls", refundUrls);
            result.add("refundInfo", refundInfo);
        }
        
        JsonArray products = new JsonArray();
        if (CollectionUtils.isNotEmpty(orderDTO.getOrderItems())) {
            for (LiveShopOrderItemDTO itemDTO : orderDTO.getOrderItems()) {
                JsonObject product = new JsonObject();
                if (itemDTO.getProductId() != null) {
                    product.addProperty("productId", itemDTO.getProductId());
                }
                if (itemDTO.getProductSpec() != null) {
                    product.addProperty("productSpec", itemDTO.getProductSpec());
                }
                if (itemDTO.getResourceUrl() != null) {
                    product.addProperty("productUrl", itemDTO.getResourceUrl() + "!256");
                    product.addProperty("productUrl_big", itemDTO.getResourceUrl());
                }
                product.addProperty("productName", itemDTO.getProductName());
                product.addProperty("productPrice", itemDTO.getProductPrice());
                product.addProperty("productCount", itemDTO.getProductCount());
                products.add(product);
            }
            result.add("products", products);
        }
    }

    /**
     * 商品信息转JSON
     * @param result
     * @param productDTO
     */
    public static void product2Json(JsonObject result, LiveShopProductDetailDTO productDTO) {
        result.addProperty("productId", productDTO.getProductId());
        result.addProperty("productName", productDTO.getProductName());
        result.addProperty("productPrice", productDTO.getProductPrice());
        result.addProperty("expressPrice", productDTO.getExpressPrice());
        if (productDTO.getProductSpec() != null) {
            result.addProperty("productSpec", productDTO.getProductSpec());
        }
        if (productDTO.getProductDetailDesc() != null) {
            result.addProperty("productDetailDesc", productDTO.getProductDetailDesc());
        }
        result.addProperty("stockNum", productDTO.getStockNum());
        result.addProperty("actorId", productDTO.getActorId());
        result.addProperty("isValid", productDTO.getIsValid());
        result.addProperty("catId", productDTO.getCatId());
        result.addProperty("supportReturn", productDTO.getSupportReturn());
        result.addProperty("supportDistribution", productDTO.getSupportDistribution());
        if(productDTO.getDistributorCommissionRate() != null) {
            result.addProperty("distributorCommissionRate", productDTO.getDistributorCommissionRate());
        }

        // banner图
        JsonArray productBannerUrls = new JsonArray();
        List<LiveShopProductPictureDTO> productPictureDTOList = productDTO.getProductPictureDTOList();
        if (productPictureDTOList != null) {
            for (LiveShopProductPictureDTO productPictureDTO : productPictureDTOList) {
                JsonObject json = new JsonObject();
                json.addProperty("productUrl", productPictureDTO.getResourceUrl() + "!640x640");
                json.addProperty("productUrlBig", productPictureDTO.getResourceUrl() + "!1280");
                productBannerUrls.add(json);
            }
            result.add("productBannerUrls", productBannerUrls);
        }

        // 详情图
        JsonArray productDetailUrls = new JsonArray();
        List<LiveShopProductPictureDTO> productPictureDTODetailList = productDTO.getProductPictureDTODetailList();
        if (productPictureDTODetailList != null) {
            for (LiveShopProductPictureDTO liveShopProductPictureDTO : productPictureDTODetailList) {
                JsonObject json = new JsonObject();
                json.addProperty("productDetailUrl", liveShopProductPictureDTO.getResourceUrl() + "!512");
                json.addProperty("pictureWidth", liveShopProductPictureDTO.getPictureWidth());
                json.addProperty("pictureHeight", liveShopProductPictureDTO.getPictureHeight());
                productDetailUrls.add(json);

            }
            result.add("productDetailUrls", productDetailUrls);
        }
    }

    public static void orderInfo2Json(JsonObject result, LiveShopOrderDTO orderDTO, UserAddressDO addressDO, List<Integer> subShopIds) {
        orderInfo2Json(result, orderDTO, addressDO);
        JsonArray subShopArray = new JsonArray();
        if (CollectionUtils.isNotEmpty(subShopIds)) {
            for (Integer subShopId : subShopIds) {
                subShopArray.add(subShopId);
            }
            result.addProperty("sellerId", subShopIds.get(0));
        }
        result.add("subShopIds", subShopArray);
    }

    public static void orderV2Info2Json(JsonObject result, LiveShopOrderV3DTO orderDTO, HistDeliveryDO histDeliveryDO, List<Integer> subShopIds) {
        orderV2Info2Json(result, orderDTO, histDeliveryDO);
        JsonArray subShopArray = new JsonArray();
        if (CollectionUtils.isNotEmpty(subShopIds)) {
            for (Integer subShopId : subShopIds) {
                subShopArray.add(subShopId);
            }
            //result.addProperty("sellerId", subShopIds.get(0));
        }
        result.add("subShopIds", subShopArray);
    }

    public static void orderV2Info2Json(JsonObject result, LiveShopOrderV3DTO orderDTO, HistDeliveryDO histDeliveryDO) {

        result.addProperty("buyerId", orderDTO.getUserId());
        result.addProperty("sellerId", orderDTO.getSellerId());
        result.addProperty("sellerNickname", orderDTO.getSellerNickname());

        result.addProperty("orderNo", orderDTO.getOrderNo());
        result.addProperty("expressMoney", orderDTO.getExpressMoney());
        result.addProperty("orderMoney", orderDTO.getOrderMoney());
        result.addProperty("orderType", orderDTO.getOrderType());

        result.addProperty("waitPayMoney", orderDTO.getWaitPayMoney());
        result.addProperty("payMoney", orderDTO.getPayMoney());
        result.addProperty("refundMoney", orderDTO.getRefundMoney());

        result.addProperty("orderChannel", orderDTO.getOrderChannel());

        if(orderDTO.getCouponAmount() != null){
            result.addProperty("couponAmount",orderDTO.getCouponAmount());
        }

        if (orderDTO.getOrderState().equals(LiveShopOrderState.WAIT_RETURN)) {
            // 管理后台挂起的订单，做为申请退款的订单处理
            result.addProperty("orderState", LiveShopOrderState.APPLY_REFUND);
        } else {
            result.addProperty("orderState", orderDTO.getOrderState());
        }
        result.addProperty("addTime", orderDTO.getAddTime().getTime());
        if(orderDTO.getPayTime() != null) {
            result.addProperty("payTime", orderDTO.getPayTime().getTime());
        }
        if(orderDTO.getSendTime() != null) {
            result.addProperty("sendTime", orderDTO.getSendTime().getTime());
        }
        if(orderDTO.getReceiveTime() != null) {
            result.addProperty("receiveTime", orderDTO.getReceiveTime().getTime());
        }

        if (histDeliveryDO != null) {
            //拼接地址
            String province = histDeliveryDO.getProvince() == null ? "" : histDeliveryDO.getProvince();
            String city = histDeliveryDO.getCity() == null ? "" : histDeliveryDO.getCity();
            String district = histDeliveryDO.getDistrict() == null ? "" : histDeliveryDO.getDistrict();
            String detailAddress = histDeliveryDO.getDetailAddress() == null ? "" : histDeliveryDO.getDetailAddress();
            detailAddress = province + city + district + detailAddress;

            JsonObject addrInfo = new JsonObject();
            addrInfo.addProperty("addressId", histDeliveryDO.getAddressId());
            addrInfo.addProperty("consigneeName", histDeliveryDO.getConsigneeName());
            addrInfo.addProperty("consigneeMobile", histDeliveryDO.getConsigneeMobile());
            addrInfo.addProperty("detailAddress", detailAddress);
            result.add("addrInfo", addrInfo);
        }

        if (!StringUtil.strIsNull(orderDTO.getWaybillNumber())
                && !StringUtil.strIsNull(orderDTO.getCourierCompany())) {
            JsonObject expressInfo = new JsonObject();
            expressInfo.addProperty("waybillNumber", orderDTO.getWaybillNumber());
            expressInfo.addProperty("courierCompany", orderDTO.getCourierCompany());
            result.add("expressInfo", expressInfo);
        }

        int orderState = orderDTO.getOrderState();
        if (orderDTO.getApplyRefundMoney() > 0 || orderState == 5 || orderState == 6 || orderState == 8) {
            JsonObject refundInfo = new JsonObject();
            refundInfo.addProperty("refundPrice", orderDTO.getApplyRefundMoney());
            if (orderDTO.getApplyRefundDesc() != null) {
                refundInfo.addProperty("refundDesc", orderDTO.getApplyRefundDesc());
            }

            JsonArray refundUrls = new JsonArray();
            if (orderDTO.getOrderPictures() != null) {
                for (LiveShopOrderPictureDTO pictureDTO : orderDTO.getOrderPictures()) {
                    JsonObject urlJson = new JsonObject();
                    urlJson.addProperty("phone_big", pictureDTO.getResourceUrl());
                    urlJson.addProperty("phone_small", pictureDTO.getResourceUrl() + "!256");
                    refundUrls.add(urlJson);
                }
            }
            refundInfo.add("refundUrls", refundUrls);
            result.add("refundInfo", refundInfo);
        }

        JsonArray products = new JsonArray();
        if (CollectionUtils.isNotEmpty(orderDTO.getOrderItems())) {
            for (LiveShopOrderItemV2DTO itemDTO : orderDTO.getOrderItems()) {
                JsonObject product = new JsonObject();
                if (itemDTO.getProductId() != null) {
                    product.addProperty("productId", itemDTO.getProductId());
                }
                if (itemDTO.getResourceUrl() != null) {
                    product.addProperty("productUrl", itemDTO.getResourceUrl() + "!256");
                    product.addProperty("productUrl_big", itemDTO.getResourceUrl());
                }
                product.addProperty("productName", itemDTO.getProductName());
                product.addProperty("productPrice", itemDTO.getProductPrice());
                product.addProperty("productCount", itemDTO.getProductCount());
                if (itemDTO.getSupportReturn() != null) {
                    product.addProperty("supportReturn", itemDTO.getSupportReturn());
                }
                if (itemDTO.getDistributorCommissionRate() != null) {
                    product.addProperty("distributorCommissionRate", itemDTO.getDistributorCommissionRate());
                }
                if (itemDTO.getDistributorCommissionAmount() != null) {
                    product.addProperty("distributorCommissionAmount", itemDTO.getDistributorCommissionAmount());
                }
                products.add(product);
            }
            result.add("products", products);
        }
    }
}

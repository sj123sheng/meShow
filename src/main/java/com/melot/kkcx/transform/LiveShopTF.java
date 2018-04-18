package com.melot.kkcx.transform;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.kk.liveshop.api.constant.LiveShopOrderState;
import com.melot.kk.liveshop.api.dto.*;
import com.melot.kk.logistics.api.domain.UserAddressDO;
import com.melot.kktv.util.StringUtil;

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
            refundInfo.addProperty("refundDesc", orderDTO.getApplyRefundDesc());
            
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
        for (LiveShopOrderItemDTO itemDTO : orderDTO.getOrderItems()) {
            JsonObject product = new JsonObject();
            product.addProperty("productId", itemDTO.getProductId());
            product.addProperty("productName", itemDTO.getProductName());
            product.addProperty("productUrl", itemDTO.getResourceUrl() + "!256");
            product.addProperty("productUrl_big", itemDTO.getResourceUrl());
            product.addProperty("productPrice", itemDTO.getProductPrice());
            product.addProperty("productCount", itemDTO.getProductCount());
//            product.addProperty("productSpec", itemDTO.getProductSpec());
            products.add(product);
        }
        result.add("products", products);
    }

    /**
     * 商品信息转JSON
     * @param result
     * @param productDTO
     */
//    public static void product2Json(JsonObject result, LiveShopProductDTO productDTO) {
//        result.addProperty("productId", productDTO.getProductId());
//        result.addProperty("productName", productDTO.getProductName());
//        result.addProperty("productPrice", productDTO.getProductPrice());
//        result.addProperty("expressPrice", productDTO.getExpressPrice());
//        if (productDTO.getProductSpec() != null) {
//            result.addProperty("productSpec", productDTO.getProductSpec());
//        }
//        if (productDTO.getProductDetailDesc() != null) {
//            result.addProperty("productDetailDesc", productDTO.getProductDetailDesc());
//        }
//        result.addProperty("stockNum", productDTO.getStockNum());
//        result.addProperty("actorId", productDTO.getActorId());
//        JsonArray productBannerUrls = new JsonArray();
//        JsonArray productDetailUrls = new JsonArray();
//        List<LiveShopProductPictureDTO> productPictureDTOList = productDTO.getProductPictureDTOList();
//
//        for (LiveShopProductPictureDTO productPictureDTO : productPictureDTOList) {
//            // 1-显示图片，2-详情图片
//            if (productPictureDTO.getPictureType() == 1) {
//                productBannerUrls.add(productPictureDTO.getResourceUrl());
//            } else {
//                productDetailUrls.add(productPictureDTO.getResourceUrl());
//            }
//        }
//        result.add("productBannerUrls", productBannerUrls);
//        result.add("productDetailUrls", productDetailUrls);
//    }
}

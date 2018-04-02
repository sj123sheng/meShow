package com.melot.kkcx.transform;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.kk.liveshop.api.dto.LiveShopOrderDTO;
import com.melot.kk.liveshop.api.dto.LiveShopOrderItemDTO;
import com.melot.kk.liveshop.api.dto.LiveShopOrderPictureDTO;
import com.melot.kk.logistics.api.domain.UserAddressDO;
import com.melot.kktv.util.StringUtil;

public class LiveShopTF {

    private LiveShopTF() {}
    
    public static void orderInfo2Json(JsonObject result, LiveShopOrderDTO orderDTO, UserAddressDO addressDO) {
        result.addProperty("shopUserId", orderDTO.getActorId());
        result.addProperty("payerId", orderDTO.getUserId());
        
        result.addProperty("orderNo", orderDTO.getOrderNo());
        result.addProperty("expressMoney", orderDTO.getExpressMoney());
        result.addProperty("orderMoney", orderDTO.getOrderMoney());
        result.addProperty("orderState", orderDTO.getOrderState());
        result.addProperty("addTime", orderDTO.getAddTime().getTime());
        
        if (addressDO != null) {
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
                    refundUrls.add(pictureDTO.getResourceUrl());
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
            product.addProperty("productUrl", itemDTO.getResourceUrl());
            product.addProperty("productPrice", itemDTO.getProductPrice());
            product.addProperty("productCount", itemDTO.getProductCount());
            products.add(product);
        }
        result.add("products", products);
    }

}

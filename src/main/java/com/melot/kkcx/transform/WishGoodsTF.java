package com.melot.kkcx.transform;

import com.google.gson.JsonObject;
import com.melot.kk.crowdfunding.api.dto.ActorWishGoodsDTO;
import com.melot.kk.crowdfunding.api.dto.WishGoodsInfoDTO;

public class WishGoodsTF {
    private WishGoodsTF(){}
    
    public static JsonObject wishGoods2Json(WishGoodsInfoDTO wishGoodsInfoDTO) {
        JsonObject wishGoodsInfoJson = new JsonObject();
        wishGoodsInfoJson.addProperty("wishGoodsId", wishGoodsInfoDTO.getWishGoodsId());
        wishGoodsInfoJson.addProperty("wishGoodsName", wishGoodsInfoDTO.getWishGoodsName());
        wishGoodsInfoJson.addProperty("wishGoodsPrice", wishGoodsInfoDTO.getWishGoodsPrice());
        wishGoodsInfoJson.addProperty("state", 0);
        
        String wishGoodsIcon = wishGoodsInfoDTO.getWishGoodsIcon();
        JsonObject wishGoodsIconJson = new JsonObject();
        wishGoodsIconJson.addProperty("web", wishGoodsIcon);
        wishGoodsIconJson.addProperty("phone", wishGoodsIcon);
        wishGoodsInfoJson.add("wishGoodsIcon", wishGoodsIconJson);
        
        return wishGoodsInfoJson;
    }
    
    public static JsonObject actorWishGoods2Json(ActorWishGoodsDTO actorWishGoodsDTO) {
        JsonObject actorWishGoodsJson = wishGoods2Json(actorWishGoodsDTO.getWishGoodsInfoDTO());
        actorWishGoodsJson.addProperty("userWishGoodsId", actorWishGoodsDTO.getConfActorWishId());
        actorWishGoodsJson.addProperty("goodsCount", actorWishGoodsDTO.getWishOrderCount());
        actorWishGoodsJson.addProperty("wishCount", actorWishGoodsDTO.getWishCount());
        return actorWishGoodsJson;
    }
}

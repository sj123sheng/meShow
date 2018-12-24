package com.melot.kkcx.transform;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.kk.hall.api.domain.HallPartConfDTO;
import com.melot.kk.hall.api.domain.HallRoomInfoDTO;
import com.melot.kk.liveshop.api.dto.LiveShopPrizeDTO;

import java.util.List;

public class LiveShopActivityTF {

    public static JsonObject hallPartConfDTO2Json(HallPartConfDTO hallPartConfDTO) {
        JsonObject jsonObject = new JsonObject();
        if (hallPartConfDTO == null) {
            return jsonObject;
        }
        jsonObject.addProperty("cataName", hallPartConfDTO.getTitleName());
        jsonObject.addProperty("post", hallPartConfDTO.getPosterPic());
        JsonArray roomList = new JsonArray();
        List<HallRoomInfoDTO> hallRoomInfoDTOList = hallPartConfDTO.getRooms();
        if (hallRoomInfoDTOList != null) {
            for (HallRoomInfoDTO hallRoomInfoDTO : hallRoomInfoDTOList) {
                JsonObject roomInfo = HallRoomTF.roomInfoToJson(hallRoomInfoDTO, 0);
                roomList.add(roomInfo);
            }
        }
        jsonObject.add("roomList", roomList);
        return jsonObject;
    }

    public static void liveShopPrizeDTO2Json(JsonObject result, LiveShopPrizeDTO liveShopPrizeDTO) {
        result.addProperty("prizeId", liveShopPrizeDTO.getPrizeId());
        result.addProperty("prizeName", liveShopPrizeDTO.getPrizeName());
        result.addProperty("prizeType", liveShopPrizeDTO.getPrizeType());
        result.addProperty("orderNo", liveShopPrizeDTO.getOrderNo());
        result.addProperty("sellerId", liveShopPrizeDTO.getSellerId());
        result.addProperty("sellerNickname", liveShopPrizeDTO.getSellerNickname());
    }
}


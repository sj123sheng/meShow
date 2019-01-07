package com.melot.kkcx.transform;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.kk.logistics.api.domain.UserAddressDO;
import com.melot.kktv.base.Page;

public class LogisticsTF {
    private LogisticsTF() {
    }

    public static void pageUserAddressDO2Json(Page<UserAddressDO> page, JsonObject result) {
        result.addProperty("count", page.getCount());
        JsonArray jsonArray = new JsonArray();
        if (page.getList() != null) {
            for (UserAddressDO userAddressDO : page.getList()) {
                //拼接地址
                String province = userAddressDO.getProvince() == null ? "" : userAddressDO.getProvince();
                String city = userAddressDO.getCity() == null ? "" : userAddressDO.getCity();
                String district = userAddressDO.getDistrict() == null ? "" : userAddressDO.getDistrict();
                String detailAddress = province + city + district + userAddressDO.getDetailAddress();

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("addressId", userAddressDO.getAddressId());
                jsonObject.addProperty("consigneeName", userAddressDO.getConsigneeName());
                jsonObject.addProperty("consigneeMobile", userAddressDO.getConsigneeMobile());
                jsonObject.addProperty("detailAddress", detailAddress);
                jsonObject.addProperty("isDefaultAddress", userAddressDO.getIsDefaultAddress());
                jsonArray.add(jsonObject);
            }
        }
        result.add("userAddressList", jsonArray);
    }

    public static void userAddressDO2Json(UserAddressDO userAddressDO, JsonObject result) {
        result.addProperty("addressId", userAddressDO.getAddressId());
        result.addProperty("consigneeName", userAddressDO.getConsigneeName());
        result.addProperty("consigneeMobile", userAddressDO.getConsigneeMobile());
        if (userAddressDO.getProvince() != null) {
            result.addProperty("province", userAddressDO.getProvince());
        }
        if (userAddressDO.getCity() != null) {
            result.addProperty("city", userAddressDO.getCity());
        }
        if (userAddressDO.getDistrict() != null) {
            result.addProperty("district", userAddressDO.getDistrict());
        }
        result.addProperty("detailAddress", userAddressDO.getDetailAddress());
        result.addProperty("isDefaultAddress", userAddressDO.getIsDefaultAddress());
    }
}


package com.melot.kkcx.transform;

import java.util.Date;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.kk.nobility.api.constant.NobilityStateEnum;
import com.melot.kk.nobility.api.domain.NobilityInfo;
import com.melot.kk.nobility.api.domain.NobilityUserInfo;
import com.melot.kk.nobility.api.domain.PowerInfo;

public class NobilityTF {
    
    private static final long DAY = 1000 * 60 * 60 * 24;

    private NobilityTF(){}
    
    /**
     * 组装用户爵位基本信息
     * @param result
     * @param nobilityUserInfo
     */
    public static void nobilityUserInfoTF(JsonObject result, NobilityUserInfo nobilityUserInfo) {
        result.addProperty("nobilityState", nobilityUserInfo.getNobilityState());
        Integer leftTime;
        if (nobilityUserInfo.getNobilityState() == NobilityStateEnum.STATE_VALID_NOBILITY) {
            Date validTime = nobilityUserInfo.getValidTime();
            leftTime = (int) ((validTime.getTime() - System.currentTimeMillis()) / DAY);
        } else if (nobilityUserInfo.getNobilityState() == NobilityStateEnum.STATE_PROTECTED_NOBILITY) {
            Date validTime = nobilityUserInfo.getProtectedTime();
            leftTime = (int) ((validTime.getTime() - System.currentTimeMillis()) / DAY);
        } else {
            return;
        }
        result.addProperty("userNobilityPoint", nobilityUserInfo.getUserNobilityPoint());
        result.addProperty("leftTime", leftTime);
        
        nobilityInfoTF(result, nobilityUserInfo.getNobilityInfo(), false);
    }

    /**
     * 组装爵位信息【带有所有的权限】
     * @param result
     * @param nobilityInfo
     */
    public static void nobilityInfoTF(JsonObject result, NobilityInfo nobilityInfo) {
        nobilityInfoTF(result, nobilityInfo, true);
    }
    
    /**
     * 组装爵位信息
     * @param result
     * @param nobilityInfo
     * @param withNoAuthPower
     */
    private static void nobilityInfoTF(JsonObject result, 
            NobilityInfo nobilityInfo, boolean withNoAuthPower) {
        result.addProperty("nobilityId", nobilityInfo.getNobilityId());
        result.addProperty("nobilityName", nobilityInfo.getNobilityName());
        result.addProperty("nobilityLevel", nobilityInfo.getNobilityLevel());
        result.addProperty("openActorRebate", nobilityInfo.getOpenActorRebate());
        result.addProperty("renewUserRebate", nobilityInfo.getRenewUserRebate());
        result.addProperty("renewUserProportion", nobilityInfo.getRenewUserProportion());
        
        JsonArray powers = new JsonArray();
        for (PowerInfo powerInfo : nobilityInfo.getPowerInfos()) {
            JsonObject power = new JsonObject();
            if (!withNoAuthPower && powerInfo.getPowerState() == 0) {
                continue;
            }
            power.addProperty("powerId", powerInfo.getPowerId());
            power.addProperty("powerName", powerInfo.getPowerName());
            power.addProperty("powerDesc", powerInfo.getPowerDesc());
            power.addProperty("powerIcon", powerInfo.getPowerIcon());
            power.addProperty("powerState", powerInfo.getPowerState());
            powers.add(power);
        }
        
        result.add("powers", powers);
    }
}

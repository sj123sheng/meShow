package com.melot.kkcx.functions;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.melot.common.driver.domain.ActorGift;
import com.melot.common.driver.service.RoomExtendConfService;
import com.melot.sdk.core.util.MelotBeanFactory;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;
import com.melot.room.gift.constant.ReturnResultCode;
import com.melot.room.gift.domain.ReturnResult;
import com.melot.room.gift.dto.ActorGiftDTO;
import com.melot.room.gift.service.ActorPersonalizedGiftService;

/**
 * Title: ActorGiftFunctions
 * <p>
 * Description: 主播个性礼物 Functions
 * </p>
 * 
 * @author 林端端<a href="mailto:duanduan.lin@melot.cn">
 * @version V1.0
 * @since 2016年8月25日 下午4:33:35
 */
public class ActorGiftFunctions {
    
    /** 日志记录对象 */
    private static Logger logger = Logger.getLogger(ActorGiftFunctions.class);
    
    private static String REGEX = ",";
    
    @Resource
    ActorPersonalizedGiftService actorPersonalizedGiftService;
    
    /**
     * 获取主播个性礼物（50001005）
     * 
     * @param jsonObject 请求对象
     * @param request 请求对象
     * @return 
     */
    public JsonObject getActorPersonalizedGiftList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        
        int userId;
        
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            RoomExtendConfService roomExtendConfService = (RoomExtendConfService) MelotBeanFactory.getBean("roomExtendConfService");
            List<ActorGift> giftList = roomExtendConfService.getActorPersonalizedGiftList(userId);
            if (giftList != null){
                List<Integer> giftIds = new ArrayList<>();
                for (ActorGift actorGift : giftList){
                    giftIds.add(actorGift.getGiftId());
                }
                result.add("actorGiftList", new Gson().toJsonTree(giftIds).getAsJsonArray());
            }
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
        return result;
    }
    
    /**
     * 添加主播个性礼物（50001006）
     * 
     * @param jsonObject 请求对象
     * @param request 请求对象
     * @return 
     */
    public JsonObject addActorPersonalizedGift(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        int userId;
        String giftIds;
        
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            giftIds = CommonUtil.getJsonParamString(jsonObject, "giftId", null, "01060003", 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            List<Integer> giftIdList = new ArrayList<>();
            String[] giftIdStr = giftIds.split(REGEX);
            for (String giftId : giftIdStr) {
                giftIdList.add(Integer.valueOf(giftId));
            }
            RoomExtendConfService roomExtendConfService = (RoomExtendConfService) MelotBeanFactory.getBean("roomExtendConfService");
            roomExtendConfService.addActorPersonalizedGift(userId, giftIdList);
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
        
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 删除主播个性礼物（50001007）
     * 
     * @param jsonObject 请求对象
     * @param request 请求对象
     * @return 
     */
    public JsonObject deleteActorPersonalizedGift(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        int userId;
        int giftId;
        
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.ACTORID_MISSING, 1, Integer.MAX_VALUE);
            giftId = CommonUtil.getJsonParamInt(jsonObject, "giftId", 0, "01070003", 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            RoomExtendConfService roomExtendConfService = (RoomExtendConfService) MelotBeanFactory.getBean("roomExtendConfService");
            roomExtendConfService.deleteActorPersonalizedGift(userId, giftId);
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
        
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 删除已下架的用户个性礼物（50001008）
     * 
     * @param jsonObject 请求对象
     * @param request 请求对象
     * @return 
     */
    public JsonObject deleteActorPersonalizedGiftByGiftId(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        int giftId;
        
        try {
            giftId = CommonUtil.getJsonParamInt(jsonObject, "giftId", 0, "01080003", 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            RoomExtendConfService roomExtendConfService = (RoomExtendConfService) MelotBeanFactory.getBean("roomExtendConfService");
            roomExtendConfService.deleteActorPersonalizedGiftByGiftId(giftId);
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
        
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 获取房间礼物特效接口（50001012）
     * 
     * @param jsonObject 请求对象
     * @param request 请求对象
     * @return 
     */
    public JsonObject getAnimations(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        JsonArray animations = new JsonArray();
        JsonObject itemJson = new JsonObject();
        itemJson.addProperty("giftId", 40000794);
        itemJson.addProperty("animationPackName", "40000794_cat");
        itemJson.addProperty("animationPackVersion", 1);
        itemJson.addProperty("animationPackUrl", "http://mres.kktv8.com/kktv/animation/40000794_cat.zip");
        JsonObject itemJson1 = new JsonObject();
        itemJson1.addProperty("giftId", 40000796);
        itemJson1.addProperty("animationPackName", "40000796_tear");
        itemJson1.addProperty("animationPackVersion", 1);
        itemJson1.addProperty("animationPackUrl", "http://mres.kktv8.com/kktv/animation/40000796_tear.zip");
        JsonObject itemJson2 = new JsonObject();
        itemJson2.addProperty("giftId", 40000893);
        itemJson2.addProperty("animationPackName", "40000893_hat");
        itemJson2.addProperty("animationPackVersion", 1);
        itemJson2.addProperty("animationPackUrl", "http://mres.kktv8.com/kktv/animation/40000893_hat.zip");
        JsonObject itemJson3 = new JsonObject();
        itemJson3.addProperty("giftId", 40000914);
        itemJson3.addProperty("animationPackName", "40000914_garland");
        itemJson3.addProperty("animationPackVersion", 1);
        itemJson3.addProperty("animationPackUrl", "http://mres.kktv8.com/kktv/animation/40000914_garland.zip");
        JsonObject itemJson4 = new JsonObject();
        itemJson4.addProperty("giftId", 40000915);
        itemJson4.addProperty("animationPackName", "40000915_icecream");
        itemJson4.addProperty("animationPackVersion", 2);
        itemJson4.addProperty("animationPackUrl", ConfigHelper.getKkDomain() + "/animation/40000915_icecream.zip");
        JsonObject itemJson5 = new JsonObject();
        itemJson5.addProperty("giftId", 40000916);
        itemJson5.addProperty("animationPackName", "40000916_meet");
        itemJson5.addProperty("animationPackVersion", 1);
        itemJson5.addProperty("animationPackUrl", "http://mres.kktv8.com/kktv/animation/40000916_meet.zip");
        JsonObject itemJson6 = new JsonObject();
        itemJson6.addProperty("giftId", 40000917);
        itemJson6.addProperty("animationPackName", "40000917_headphone");
        itemJson6.addProperty("animationPackVersion", 1);
        itemJson6.addProperty("animationPackUrl", "http://mres.kktv8.com/kktv/animation/40000917_headphone.zip");
        JsonObject itemJson7 = new JsonObject();
        itemJson7.addProperty("giftId", 40000918);
        itemJson7.addProperty("animationPackName", "40000918_bangbangjun");
        itemJson7.addProperty("animationPackVersion", 1);
        itemJson7.addProperty("animationPackUrl", "http://mres.kktv8.com/kktv/animation/40000918_bangbangjun.zip");
        JsonObject itemJson8 = new JsonObject();
        itemJson8.addProperty("giftId", 40000919);
        itemJson8.addProperty("animationPackName", "40000919_bunny");
        itemJson8.addProperty("animationPackVersion", 1);
        itemJson8.addProperty("animationPackUrl", "http://mres.kktv8.com/kktv/animation/40000919_bunny.zip");
        JsonObject itemJson9 = new JsonObject();
        itemJson9.addProperty("giftId", 40000921);
        itemJson9.addProperty("animationPackName", "40000921_kiss");
        itemJson9.addProperty("animationPackVersion", 1);
        itemJson9.addProperty("animationPackUrl", "http://mres.kktv8.com/kktv/animation/40000921_kiss.zip");
        animations.add(itemJson);
        animations.add(itemJson1);
        animations.add(itemJson2);
        animations.add(itemJson3);
        animations.add(itemJson4);
        animations.add(itemJson5);
        animations.add(itemJson6);
        animations.add(itemJson7);
        animations.add(itemJson8);
        animations.add(itemJson9);
        result.add("animations", animations);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 获取房间礼物特效接口（50001014）
     * 
     * @param jsonObject 请求对象
     * @param request 请求对象
     * @return 
     */
    @SuppressWarnings("unchecked")
    public JsonObject getNewAnimations(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int platform;
        
        try {
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.ANDROID, null, PlatformEnum.ANDROID, PlatformEnum.IPHONE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        JsonArray animations = new JsonArray();
        List<Integer> giftIdList = new ArrayList<Integer>();
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put("invideo", 1);
        map.put("appid", 1);
        try {
            giftIdList = (List<Integer>) SqlMapClientHelper.getInstance(DB.MASTER).queryForList("ActorGift.getAnimationGifts", map);
        } catch (SQLException e) {
            logger.error("获取特效礼物失败" , e);
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
            return result;
        }
        for (int giftId : giftIdList) {
            JsonObject itemJson = new JsonObject();
            itemJson.addProperty("giftId", giftId);
            itemJson.addProperty("animationPackName", String.valueOf(giftId));
            itemJson.addProperty("animationPackVersion", 1);
            if (platform == PlatformEnum.ANDROID) {
                itemJson.addProperty("animationPackUrl", ConfigHelper.getKkDomain() + "/icon/android/gift/zip/" + giftId + ".zip");
            } else {
                itemJson.addProperty("animationPackUrl", ConfigHelper.getKkDomain() + "/icon/iphone/gift/zip/" + giftId + ".zip");
            }
            animations.add(itemJson);
        }
        result.add("animations", animations);
        
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

}

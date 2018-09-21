/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2018
 */
package com.melot.kktv.action;

import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.kk.hall.api.domain.HallPartConfDTO;
import com.melot.kk.hall.api.domain.HallRoomInfoDTO;
import com.melot.kk.hall.api.service.SysMenuService;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcx.service.UserService;
import com.melot.kkcx.transform.HallRoomTF;
import com.melot.kktv.base.CommonStateCode;
import com.melot.kktv.base.Result;
import com.melot.kktv.service.ConfigService;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.TagCodeEnum;

/**
 * Title: KKPlayFunctions
 * <p>
 * Description: K玩大厅
 * </p>
 * 
 * @author 林端端<a href="mailto:duanduan.lin@melot.cn" />
 * @version V1.0
 * @since 2018年9月19日 上午10:34:54
 */
public class KKPlayFunctions {
    
    private static Logger logger = Logger.getLogger(KKPlayFunctions.class);
    
    @Autowired
    private ConfigService configService;
    
    @Resource
    private SysMenuService hallPartService;
    
    /**
     * 获取K玩大厅栏目列表(51070301)
     */
    public JsonObject getPartList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int platform;
        try {
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, TagCodeEnum.PLATFORM_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            JsonArray cataArray = new JsonArray();
            String kkplayConfig = configService.getKkPlayConfig();
            JsonArray configArray = new JsonParser().parse(kkplayConfig).getAsJsonArray();
            if (configArray.size() > 0) {
                for (int i=0; i < configArray.size(); i++) {
                    JsonObject jsonObj = (JsonObject) configArray.get(i);
                    Result<HallPartConfDTO> sysMenuResult = hallPartService.getPartList(jsonObj.get("cataId").getAsInt(), 0, 0, 0, AppIdEnum.AMUSEMENT, 0, 3);
                    if (sysMenuResult != null && CommonStateCode.SUCCESS.equals(sysMenuResult.getCode())) {
                        HallPartConfDTO sysMenu = sysMenuResult.getData();
                        if (sysMenu != null) {
                            JsonArray roomArray = new JsonArray();
                            List<HallRoomInfoDTO> roomList = sysMenu.getRooms();
                            if (roomList != null) {
                                for (HallRoomInfoDTO hallRoomInfoDTO : roomList) {
                                    roomArray.add(HallRoomTF.roomInfoToJson(hallRoomInfoDTO, platform));
                                }
                            }
                            jsonObj.add("roomList", roomArray);
                        }
                    }
                    cataArray.add(jsonObj);
                }
            }

            result.add("cataList", cataArray);
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Error KKPlayFunctions.getPartList()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }
    
    /**
     * 获取K玩用户信息(51070302)
     * 
     * @param jsonObject 请求对象
     * @param checkTag 是否验证token标记
     * @return 
     */
    public JsonObject getUserKKPlayInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
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
        UserProfile userProfile = UserService.getUserInfoNew(userId);
        if (userProfile != null) {
            result.addProperty("nickname", userProfile.getNickName());
            result.addProperty("portrait", userProfile.getPortrait());
        }
        result.addProperty("goldCoin", UserService.getUserGoldCoin(userId));
        result.addProperty("showMoney", UserService.getUserMoney(userId));
        //TODO
        result.addProperty("score", 777);
        result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 获取K玩排行榜(51070303)
     * 
     * @param jsonObject 请求对象
     * @param checkTag 是否验证token标记
     * @return 
     */
    public JsonObject getRankList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int rankType, slotType, platform, userId, count;
        try {
            rankType = CommonUtil.getJsonParamInt(jsonObject, "rankType", 0, "5107030301", 0, Integer.MAX_VALUE);
            slotType = CommonUtil.getJsonParamInt(jsonObject, "slotType", 0, "5107030302", 0, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 1, null, 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
            count = CommonUtil.getJsonParamInt(jsonObject, "count", 10, null, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        //TODO 排行榜
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 快速匹配(51070304)
     * 
     * @param jsonObject 请求对象
     * @param checkTag 是否验证token标记
     * @return 
     */
    public JsonObject getMatchGameUser(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        int userId, gameId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            gameId = CommonUtil.getJsonParamInt(jsonObject, "gameId", 0, "5107030401", 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        //TODO
        
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 获取匹配结果(51070305)
     * 
     * @param jsonObject 请求对象
     * @param checkTag 是否验证token标记
     * @return 
     */
    public JsonObject getGameMatchInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
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

        //TODO
        
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 取消匹配(51070307)
     * 
     * @param jsonObject 请求对象
     * @param checkTag 是否验证token标记
     * @return 
     */
    public JsonObject cancelMatchGameUser(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        int userId, gameId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            gameId = CommonUtil.getJsonParamInt(jsonObject, "gameId", 0, "5107030401", 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        //TODO
        
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 游戏规则(51070308)
     * 
     * @param jsonObject 请求对象
     * @param checkTag 是否验证token标记
     * @return 
     */
    public JsonObject getGameRule(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int gameId;
        try {
            gameId = CommonUtil.getJsonParamInt(jsonObject, "gameId", 0, "5107030801", 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        String kkPlayRule = configService.getKkPlayRule();
        JsonArray ruleArray = new JsonParser().parse(kkPlayRule).getAsJsonArray();
        if (ruleArray.size() > 0) {
            for (int i=0; i < ruleArray.size(); i++) {
                JsonObject jObj = (JsonObject) ruleArray.get(i);
                if (jObj.get("gameId").getAsInt() == gameId) {
                    result.addProperty("gameRule", jObj.get("gameRule").getAsString());
                    result.addProperty("gameDetail", jObj.get("gameDetail").getAsString());
                    result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                    return result;
                };
            }
        }
        
        result.addProperty("TagCode", "5107030802");
        return result;
    }
    
    /**
     * 获取pk游戏配置(51070310)
     * 
     * @param jsonObject 请求对象
     * @param checkTag 是否验证token标记
     * @return 
     */
    public JsonObject getPKGameList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        String kkPlayGameConf = configService.getKkPlayGameConf();
        JsonArray gameConfList = new JsonParser().parse(kkPlayGameConf).getAsJsonArray();
        result.add("gameConfList", gameConfList);
        result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    public static void main(String[] args) {
        JsonArray jsonArray = new JsonArray();
        JsonObject jsonObj = new JsonObject();
//        jsonObj.addProperty("cataId", 3961);
//        jsonObj.addProperty("title", "骰子游戏");
//        jsonObj.addProperty("subTitle", "4v4团战");
//        jsonObj.addProperty("title_poster", "/poster/offical/national_pk_test.jpg");
//        jsonObj.addProperty("position", 1);
//        jsonObj.addProperty("gameId", 1);
        
        jsonObj.addProperty("title", "斗地主");
        jsonObj.addProperty("desc", "三人PK，经典玩法");
        jsonObj.addProperty("icon", "sssss");
        jsonObj.addProperty("roomMode", 111);
        JsonObject jsonObj1 = new JsonObject();
        jsonObj1.addProperty("gameId", 1);
        jsonObj1.addProperty("title", "骰子团战");
        jsonObj1.addProperty("desc", "4V4团队PK");
        jsonObj1.addProperty("icon", "ttt");
        jsonObj1.addProperty("roomMode", 112);
        jsonArray.add(jsonObj);
        jsonArray.add(jsonObj1);
        System.out.println(jsonArray.toString());
    }

}

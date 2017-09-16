package com.melot.kkcx.functions;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.api.menu.sdk.dao.domain.HDRoomPoster;
import com.melot.api.menu.sdk.dao.domain.HomePage;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.api.menu.sdk.dao.domain.SysMenu;
import com.melot.api.menu.sdk.handler.FirstPageHandler;
import com.melot.api.menu.sdk.redis.KKHallSource;
import com.melot.api.menu.sdk.service.RoomInfoService;
import com.melot.kkcore.relation.api.ActorRelation;
import com.melot.kkcore.relation.api.RelationType;
import com.melot.kkcore.relation.service.ActorRelationService;
import com.melot.kkcx.service.RoomService;
import com.melot.kkcx.transform.RoomTF;
import com.melot.kktv.service.UserRelationService;
import com.melot.kktv.util.*;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;
import com.melot.sdk.core.util.MelotBeanFactory;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Title: BountyFunctions
 * <p>
 * Description: KK 奖励金 Functions
 * </p>
 * 
 * @author 宋建明<a href="mailto:jianming.song@melot.cn">
 * @version V1.0
 * @since 2017年9月16日 上午8:59:17
 */
public class BountyFunctions {

    private static final Logger LOGGER = Logger.getLogger(BountyFunctions.class);

    /**
     * 获取用户可领取红包个数接口（52050201）
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject getRedPacketCount(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        // 安全校验
        JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
        if(rtJO != null) {
            return rtJO;
        }
        
        JsonObject result = new JsonObject();
        
        // Token 校验
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        /*
         * TODO 
         * 调用奖励金模块获取用户当前可领红包数 
         * 参数：
         *   userId 用户ID 
         */

        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 获取用户非日常红包列表接口（52050202）
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject getNonDailyRedPacketList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        // 安全校验
        JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
        if(rtJO != null) {
            return rtJO;
        }
        
        JsonObject result = new JsonObject();
        
        // Token 校验
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        int num;
        int maxRedPacketId;
        
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            num = CommonUtil.getJsonParamInt(jsonObject, "num", 20, null, 1, 30);
            maxRedPacketId = CommonUtil.getJsonParamInt(jsonObject, "maxRedPacketId", 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        /*
         * TODO 
         * 调用奖励金模块获取非日常红包列表
         * 参数：
         *   userId 用户ID 
         *   maxRedPacketId 客户端已获取的最大红包ID
         *   num 需要返回的数量
         */

        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 获取用户日常红包列表接口（52050203）
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject getDailyRedPacketList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        // 安全校验
        JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
        if(rtJO != null) {
            return rtJO;
        }
        
        JsonObject result = new JsonObject();
        
        // Token 校验
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        /*
         * TODO 
         * 调用奖励金模块获取用户日常红包列表
         * 参数：
         *   userId 用户ID 
         */

        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 开启非日常红包接口（52050204）
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject openNonDailyRedPacket(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        // 安全校验
        JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
        if(rtJO != null) {
            return rtJO;
        }
        
        JsonObject result = new JsonObject();
        
        // Token 校验
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        int redPacketId;
        
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            redPacketId = CommonUtil.getJsonParamInt(jsonObject, "redPacketId", 0, "5205020401", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        /*
         * TODO 
         * 调用奖励金模块开启日常红包
         * 参数：
         *   userId 用户ID 
         *   redPacketId 红包ID
         */

        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 开启日常红包接口（52050205）
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject openDailyRedPacket(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        // 安全校验
        JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
        if(rtJO != null) {
            return rtJO;
        }
        
        JsonObject result = new JsonObject();
        
        // Token 校验
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        int redPacketLevel;
        
        String redPacketDate;
        
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            redPacketLevel = CommonUtil.getJsonParamInt(jsonObject, "redPacketLevel", 0, "5205020501", 1, Integer.MAX_VALUE);
            redPacketDate = CommonUtil.getJsonParamString(jsonObject, "redPacketDate", DateUtil.formatDate(new Date(), "yyyy-MM-dd"), null, 10, 10);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        /*
         * TODO 
         * 调用奖励金模块开启日常红包 
         * 参数：
         *   userId 用户ID 
         *   redPacketLevel 要开启的红包等级
         *   redPacketDate 红包所属日期，字符串格式 yyyy-mm-dd
         */

        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 获取奖励金信息接口（52050206）
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject getBountyInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        // 安全校验
        JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
        if(rtJO != null) {
            return rtJO;
        }
        
        JsonObject result = new JsonObject();
        
        // Token 校验
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        /*
         * TODO 
         * 调用奖励金模块获取用户奖励金信息 
         * 参数：
         *   userId 用户ID 
         */

        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 获取奖励金流水接口（51050207）
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject getBountyHistList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        
        JsonObject result = new JsonObject();
        
        // Token 校验
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        int start;
        int offset;
        
        String dataMonth;
        
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            dataMonth = CommonUtil.getJsonParamString(jsonObject, "dataMonth", DateUtil.formatDate(new Date(), "yyyy-MM"), null, 7, 7);
            start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, null, 0, Integer.MAX_VALUE);
            offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 20, null, 1, 30);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        /*
         * TODO 
         * 调用奖励金模块获取用户奖励金流水  
         * 参数：
         *   userId 用户ID 
         *   dataMonth 数据月份 
         *   start 起始值 
         *   offset 列表数量 
         */

        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

}

package com.melot.kktv.action;

import com.google.gson.*;
import com.melot.common.driver.base.Result;
import com.melot.common.driver.base.ResultCode;
import com.melot.common.driver.domain.AgoraInfo;
import com.melot.common.driver.domain.ConfigInfo;
import com.melot.common.driver.domain.FriendEmoticon;
import com.melot.common.driver.service.ConfigInfoService;
import com.melot.common.driver.service.FriendEmoticonService;
import com.melot.kkcx.transform.FriendEmoticonTF;
import com.melot.kktv.domain.ConfigGiftInfo;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.room.gift.constant.ReturnResultCode;
import com.melot.room.gift.domain.GiftIconUrl;
import com.melot.room.gift.domain.GiftInfo;
import com.melot.room.gift.domain.GiftListResourceURL;
import com.melot.room.gift.domain.ReturnResult;
import com.melot.room.gift.service.GiftListService;
import com.melot.sdk.core.util.MelotBeanFactory;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Title: ConfigFunctions
 * <p>
 * Description:配置信息管理相关接口 
 * </p>
 * 
 * @author 魏安稳<a href="mailto:anwen.wei@melot.cn"/>
 * @version V1.0
 * @since 2017年5月17日 下午3:45:11
 */
public class ConfigFunctions {

    private static Logger logger = Logger.getLogger(ConfigFunctions.class);
    
    private static final String KEY = "YdsSH&@#Uyh";

    private static final String SPLIT = ",";

    /**
     * 50001101
     * 根据KEY获取相关配置信息
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getConfigInfoByKey(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        String key;
        int platform = 0;
        try {
            key = CommonUtil.getJsonParamString(jsonObject, "key", null, "05110101", 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, PlatformEnum.WEB, PlatformEnum.IPAD);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        // 根据模块获取配置信息
        ConfigInfo configInfo;
        try {
            ConfigInfoService configInfoService = (ConfigInfoService) MelotBeanFactory.getBean("configInfoService");
            configInfo = configInfoService.getConfigInfoByKey(key);
            if (configInfo == null) {
                result.addProperty("TagCode", TagCodeEnum.CONFIG_KEY_NOT_EXIST);
                return result;
            }
        } catch (Exception e) {
            logger.error("Module Error ConfigInfoService.getConfigInfoByKey(" + key + ")", e);
            result.addProperty("TagCode", TagCodeEnum.CONFIG_KEY_NOT_EXIST);
            return result;
        }
        
        // 处理value，如果是JsonObject转化为Array返回
        JsonParser parser = new JsonParser();
        try {
            String value = "";
            if (PlatformEnum.WEB == platform) {
                value = configInfo.getConfigValueWeb();
            }else {
                value = configInfo.getConfigValueApp();
            }
            JsonElement valueElement = parser.parse(value);
            if (valueElement.isJsonObject()) {
                JsonObject valueObject = valueElement.getAsJsonObject();
                
                JsonArray valueArray = new JsonArray();
                valueArray.add(valueObject);
                result.add("value", valueArray);
            }

            if (valueElement.isJsonArray()) {
                JsonArray valueArray = valueElement.getAsJsonArray();
                result.add("value", valueArray);
            }
        } catch (Exception e) {
            logger.error("the value of key:" + key + " is not a json!", e);
            result.addProperty("TagCode", TagCodeEnum.CONFIG_KEY_NOT_EXIST);
            return result;
        }
        
        //添加其他的返回信息
        result.addProperty("key", configInfo.getConfigKey());
        result.addProperty("version", configInfo.getVersion());
        result.addProperty("describe", configInfo.getConfigDesc());
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 50001102
     * 根据KEY获取配置信息的版本号
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getVersionByKey(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        String key;
        try {
            key = CommonUtil.getJsonParamString(jsonObject, "key", null, "05110101", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            ConfigInfoService configInfoService = (ConfigInfoService) MelotBeanFactory.getBean("configInfoService");
            Long version = configInfoService.getVersionByKey(key);
            if (version == null) {
                result.addProperty("TagCode", TagCodeEnum.CONFIG_KEY_NOT_EXIST);
                return result;
            }
            result.addProperty("version", version);
            result.addProperty("key", key);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Module Error ConfigInfoService.getVersionByKey(" + key + ")", e);
            result.addProperty("TagCode", TagCodeEnum.CONFIG_KEY_NOT_EXIST);
            return result;
        }
    }

    /**
     * 50001103
     * 获取礼物相关的配置信息
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getGiftConfigInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        // 获取版本号参数
        int version = 0;
        
        try {
            version = CommonUtil.getJsonParamInt(jsonObject, "version", 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        // 组装数据
        ConfigGiftInfo configGiftInfo = new ConfigGiftInfo();
        
        try {
            // 获取版本号
            GiftListService giftListService = MelotBeanFactory.getBean("giftListService", GiftListService.class);
            ReturnResult<Integer> giftVersionResult = giftListService.getGiftListVersion();
            
            if (giftVersionResult == null) {
                result.addProperty("TagCode", TagCodeEnum.GIFT_MODULE_NULL);
                return result;
            }
            
            if (!ReturnResultCode.SUCCESS.equals(giftVersionResult.getCode())) {
                result.addProperty("TagCode", returnCodeToTagCode(giftVersionResult.getCode()));
                return result;
            }
            
            Integer giftVersion = giftVersionResult.getData();
            
            if (giftVersion == null) {
                result.addProperty("TagCode", TagCodeEnum.GIFT_VERSION_IS_NULL);
                return result;
            }
            
            if (version > 0 && version == giftVersion.intValue()) {
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            }
            
            configGiftInfo.setGiftVersion(giftVersionResult.getData());
        } catch (Exception e) {
            logger.error("Module Error GiftListService.getGiftListVersion()", e);
            result.addProperty("TagCode", TagCodeEnum.GIFT_MODULE_NULL);
            return result;
        }
        
        try {
            // 获取url拼接信息
            GiftListService giftListService = MelotBeanFactory.getBean("giftListService", GiftListService.class);
            ReturnResult<GiftListResourceURL> giftListResourceURLResult = giftListService.getGiftListResourceURL();
            if (giftListResourceURLResult == null) {
                result.addProperty("TagCode", TagCodeEnum.GIFT_MODULE_NULL);
                return result;
            }
            
            if (!ReturnResultCode.SUCCESS.equals(giftListResourceURLResult.getCode())) {
                result.addProperty("TagCode", returnCodeToTagCode(giftListResourceURLResult.getCode()));
                return result;
            }
            configGiftInfo.setGiftListResourceURL(giftListResourceURLResult.getData());
            
        } catch (Exception e) {
            logger.error("Module Error GiftListService.getGiftListResourceURL()", e);
            result.addProperty("TagCode", TagCodeEnum.GIFT_MODULE_NULL);
            return result;
        }
        
        try {
            // 获取礼物列表
            GiftListService giftListService = MelotBeanFactory.getBean("giftListService", GiftListService.class);
            ReturnResult<List<GiftInfo>> allGiftsResult;
            
            if (version > 0) {
                allGiftsResult = giftListService.listGiftWithVersion(version);
            }else {
                allGiftsResult = giftListService.listAllGift();
            }
            
            if (allGiftsResult == null) {
                result.addProperty("TagCode", TagCodeEnum.GIFT_MODULE_NULL);
                return result;
            }
            
            if (!ReturnResultCode.SUCCESS.equals(allGiftsResult.getCode())) {
                result.addProperty("TagCode", returnCodeToTagCode(allGiftsResult.getCode()));
                return result;
            }
            configGiftInfo.setGiftList(allGiftsResult.getData());
            
        } catch (Exception e) {
            logger.error("Module Error GiftListService.listAllGift()", e);
            result.addProperty("TagCode", TagCodeEnum.GIFT_MODULE_NULL);
            return result;
        }
        
        try {
            // 获取角标信息
            GiftListService giftListService = MelotBeanFactory.getBean("giftListService", GiftListService.class);
            ReturnResult<List<GiftIconUrl>> allGiftIconsResult = giftListService.listGiftIconUrl();
            if (allGiftIconsResult == null) {
                result.addProperty("TagCode", TagCodeEnum.GIFT_MODULE_NULL);
                return result;
            }
            
            if (!ReturnResultCode.SUCCESS.equals(allGiftIconsResult.getCode())) {
                result.addProperty("TagCode", returnCodeToTagCode(allGiftIconsResult.getCode()));
                return result;
            }
            configGiftInfo.setIconUrl(allGiftIconsResult.getData());
        } catch (Exception e) {
            logger.error("Module Error GiftListService.listGiftIconUrl()", e);
            result.addProperty("TagCode", TagCodeEnum.GIFT_MODULE_NULL);
            return result;
        }
        
        // 以后可能会单独开一个接口获取这个值，暂时是不用返回该信息
//        try {
//            // 获取VR使用的grammarId（这块暂时不做强制有数据）
//            GiftListService giftListService = MelotBeanFactory.getBean("giftListService", GiftListService.class);
//            ReturnResult<String> grammarIdResult = giftListService.getGiftNameListGrammarId();
//            if (grammarIdResult == null) {
//                
//            }else if (!ReturnResultCode.SUCCESS.equals(grammarIdResult.getCode())) {
//                
//            }else {
//                configGiftInfo.setGrammarId(grammarIdResult.getData());
//            }
//        } catch (Exception e) {
//            logger.error("Module Error GiftListService.listGiftIconUrl()", e);
//        }
        result = new JsonParser().parse(new Gson().toJson(configGiftInfo)).getAsJsonObject();
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 50001104
     * 获取交友房的特殊表情
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getFriendEmoticonConfigInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        int platform = 0;
        try {
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, PlatformEnum.WEB, PlatformEnum.IPAD);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        // 获取所有的交友房表情信息
        List<FriendEmoticon> emoticonList;
        try {
            FriendEmoticonService friendEmoticonService = MelotBeanFactory.getBean("friendEmoticonService", FriendEmoticonService.class);
            emoticonList = friendEmoticonService.getAllEmoticons();
        } catch (Exception e) {
            logger.error("Module Error FriendEmoticonService.getAllEmoticons()", e);
            result.addProperty("TagCode", TagCodeEnum.EMOTICON_NOT_FIND);
            return result;
        }
        
        if (emoticonList == null || emoticonList.isEmpty()) {
            result.addProperty("TagCode", TagCodeEnum.EMOTICON_NOT_FIND);
            return result;
        }
        
        //根据平台拼接前台所需的字段
        JsonArray emoticons = new JsonArray();
        for (FriendEmoticon friendEmoticon : emoticonList) {
            
            // 需要注意这里需要加一下valid的过滤（数据库是直接全部返回的）
            if (0 == friendEmoticon.getValid()) {
                continue;
            }
            
            JsonObject emoticon = FriendEmoticonTF.toJsonObject(friendEmoticon, platform);
            emoticons.add(emoticon);
        }
        result.addProperty("domain", ConfigHelper.getKkDomain());
        result.add("emoticons", emoticons);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 获取声网相关信息【50001109】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getAgoraInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        int roomId;
        int roomSource;
        String sign;
        try {
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, TagCodeEnum.PARAMETER_PARSE_ERROR, 1, Integer.MAX_VALUE);
            roomSource = CommonUtil.getJsonParamInt(jsonObject, "roomSource", 0, TagCodeEnum.PARAMETER_PARSE_ERROR, 1, Integer.MAX_VALUE);
            sign = CommonUtil.getJsonParamString(jsonObject, "sign", null, TagCodeEnum.PARAMETER_PARSE_ERROR, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        // 校验参数
        if (!checkSign(roomId, roomSource, sign)) {
            result.addProperty("TagCode", "5110901");
            return result;
        }
        try {
            ConfigInfoService configInfoService = MelotBeanFactory.getBean("configInfoService", ConfigInfoService.class);
            Result<AgoraInfo> agoraInfoResult = configInfoService.getAgoraInfo(roomId, roomSource);
            if (agoraInfoResult == null || agoraInfoResult.getCode() == null) {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            if (ResultCode.SUCCESS.equals(agoraInfoResult.getCode())) {
                AgoraInfo info = agoraInfoResult.getData();
                result.addProperty("appId", info.getAppId());
                result.addProperty("channelId", info.getChannelId());
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;                
            }else {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
        } catch (Exception e) {
            logger.error("Module Error ConfigInfoService.getAgoraInfo()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }        
    }

    /**
     * VR主播获取grammarId信息(50001111)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getGrammarId(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        try {
            // 获取VR使用的grammarId（这块暂时不做强制有数据）
            GiftListService giftListService = MelotBeanFactory.getBean("giftListService", GiftListService.class);
            ReturnResult<String> grammarIdResult = giftListService.getGiftNameListGrammarId();
            if (grammarIdResult != null && ReturnResultCode.SUCCESS.equals(grammarIdResult.getCode())) {
                result.addProperty("grammarId", grammarIdResult.getData());
            }else {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
        } catch (Exception e) {
            logger.error("Module Error GiftListService.listGiftIconUrl()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    private String returnCodeToTagCode (String returnCode){
        if (returnCode.equals(ReturnResultCode.ERROR_PARMETER)) {
            return TagCodeEnum.GIFT_MODULE_ERROR_PARMETER;
        }
        if (returnCode.equals(ReturnResultCode.ERROR_SQL)) {
            return TagCodeEnum.GIFT_MODULE_ERROR_SQL;
        }
        if (returnCode.equals(ReturnResultCode.ERROR_REQUEST_TIMEOUT)) {
            return TagCodeEnum.GIFT_MODULE_ERROR_REQUEST_TIMEOUT;
        }
        return TagCodeEnum.GIFT_MODULE_ERROR_UNDEFINED;
    }
    
    /**
     * 校验参数
     * @return
     */
    private boolean checkSign(int roomId, int roomSource, String sign) {
        StringBuilder builder = new StringBuilder();
        builder.append(KEY);
        builder.append("roomId=");
        builder.append(roomId);
        builder.append("&roomSource=");
        builder.append(roomSource);
        builder.append(KEY);
        
        String param = builder.toString();
        String signTemp = CommonUtil.md5(param);
        if (signTemp.equals(sign)) {
            return true;
        }
        return false;
    }
}

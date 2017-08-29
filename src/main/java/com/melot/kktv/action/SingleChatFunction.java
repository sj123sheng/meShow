package com.melot.kktv.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.api.menu.sdk.service.RoomInfoService;
import com.melot.singlechat.driver.base.Result;
import com.melot.singlechat.driver.base.ResultCode;
import com.melot.singlechat.driver.domain.HistSingleChatInfo;
import com.melot.singlechat.driver.domain.PageSingleChatLabel;
import com.melot.singlechat.driver.domain.PageSingleChatServer;
import com.melot.singlechat.driver.domain.SingleChatActorInfo;
import com.melot.singlechat.driver.domain.SingleChatLabel;
import com.melot.singlechat.driver.domain.SingleChatRoomInfo;
import com.melot.singlechat.driver.domain.SingleChatServer;
import com.melot.singlechat.driver.domain.SingleChatServerPrice;
import com.melot.singlechat.driver.domain.SingleChatUserInfo;
import com.melot.singlechat.driver.service.SingleChatServerService;
import com.melot.singlechat.driver.service.SingleChatService;
import com.melot.kk.activity.driver.MissionService;
import com.melot.kkcore.user.api.UserAssets;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kkcx.transform.RoomTF;
import com.melot.kktv.model.transform.SingleChatServerTF;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.module.packagegift.driver.domain.UserTicketInfo;
import com.melot.module.packagegift.driver.service.TicketService;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * Title: SingleChatFunction
 * <p>
 * Description: 1v1视频聊天相关API接口
 * </p>
 * 
 * @author 魏安稳<a href="mailto:anwen.wei@melot.cn"/>
 * @version V1.0
 * @since 2017年6月28日 下午2:43:17
 */
public class SingleChatFunction {
    private static Logger logger = Logger.getLogger(ConfigFunctions.class);
    
    private static final String ERROR_USER_IS_FORBIDEN = "05110801";
    private static final String ERROR_ACTOR_CAN_NOT_CHAT = "05110802";
    private static final String ERROR_USER_LACK_MONEY = "05110803";
    private static final String ERROR_ACTOR_IS_CHATTING = "05110804";
    private static final String ERROR_ACTOR_IS_END = "05110805";
    
    /**
     * 获取大厅主播列表【50001105】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getActorList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        // 定义返回结果

        JsonObject result = new JsonObject();
        
        int num, platform, appId, userId;
        String actorIds;
        
        try {
            num = CommonUtil.getJsonParamInt(jsonObject, "num", 3, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, PlatformEnum.WEB, PlatformEnum.IPAD);
            actorIds = CommonUtil.getJsonParamString(jsonObject, "actorIds", null, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        List<String> actors = new ArrayList<>();
        try {
            if (actorIds != null) {
                 actors = Arrays.asList(actorIds.split(","));
            }
            if (userId > 0) {
                
                //Arrays.asList() 返回java.util.Arrays$ArrayList， 而不是ArrayList。
                actors = new ArrayList<>(actors);
                actors.add(String.valueOf(userId));
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        // 获取主播列表
        int getCount = num + actors.size();
        try {
            SingleChatService singleChatService = MelotBeanFactory.getBean("singleChatService", SingleChatService.class);
            String clientIP = com.melot.kktv.service.GeneralService.getIpAddr(request, appId, appId, null);
            Result<ArrayList<SingleChatRoomInfo>> priceResult = singleChatService.getActorList(getCount, clientIP, userId);
            
            if (priceResult == null || priceResult.getCode() == null) {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            
            JsonArray rooms = new JsonArray();
            if (ResultCode.SUCCESS.equals(priceResult.getCode())) {
                List<SingleChatRoomInfo> singleChatRoomInfos = priceResult.getData();
                
                for (SingleChatRoomInfo singleChatRoomInfo : singleChatRoomInfos) {
                    if (rooms.size() >= num) {
                        break;
                    }
                    if (singleChatRoomInfo == null 
                            || singleChatRoomInfo.getRoomInfo() == null 
                            || actors.contains(String.valueOf(singleChatRoomInfo.getRoomInfo().getActorId()))) {
                        continue;
                    }
                    RoomInfo roomInfo = singleChatRoomInfo.getRoomInfo();
                    JsonObject roomJson = RoomTF.roomInfoToJson(roomInfo, platform, false, true);
                    roomJson.addProperty("price", singleChatRoomInfo.getSingleChatPrice());
                    roomJson.addProperty("liveStream", singleChatRoomInfo.getLiveStream());
                    rooms.add(roomJson);
                }
                
                result.add("rooms", rooms);
                result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            }
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        } catch (Exception e) {
            logger.error("Module Error SingleChatService.getActorList(" + getCount + ")", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }
    
    /**
     * 获取主播的1v1视频收费（每分钟）【50001106】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getActorPrice(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        // 定义返回结果
        JsonObject result = new JsonObject();
        
        int actorId;
        
        try {
            actorId = CommonUtil.getJsonParamInt(jsonObject, "actorId", 0, TagCodeEnum.ACTOR_ID_MISSING, Integer.MIN_VALUE, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            SingleChatService singleChatService = MelotBeanFactory.getBean("singleChatService", SingleChatService.class);
            Result<SingleChatActorInfo> actorInfoResult = singleChatService.getActorInfo(actorId);
            
            if (actorInfoResult == null || actorInfoResult.getCode() == null) {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            
            if (ResultCode.ERROR_CONFIG.equals(actorInfoResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.SINGLE_CHAT_CONFIG_ERROR);
                return result;
            }
            
            if (ResultCode.SUCCESS.equals(actorInfoResult.getCode())) {
                Integer price = actorInfoResult.getData().getPrice();
                
                Integer actorRate = actorInfoResult.getData().getActorRate();
                result.addProperty("price", price);
                result.addProperty("income", price * actorRate / 100);
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            }
            
            result.addProperty("TagCode", TagCodeEnum.SINGLE_CHAT_CONFIG_ERROR);
            return result;
        } catch (Exception e) {
            logger.error("Module Error SingleChatService.getActorPrice(" + actorId + ")", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }
    
    /**
     * 校验用户是否有开播权限【50001107】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject checkActor(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        // 定义返回结果
        JsonObject result = new JsonObject();
        
        int actorId;
        
        try {
            actorId = CommonUtil.getJsonParamInt(jsonObject, "actorId", 0, TagCodeEnum.ACTOR_ID_MISSING, Integer.MIN_VALUE, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
       
        try {
            SingleChatService singleChatService = MelotBeanFactory.getBean("singleChatService", SingleChatService.class);
            Result<Boolean> isActorResult = singleChatService.checkActor(actorId);
            
            if (isActorResult == null || isActorResult.getCode() == null) {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            
            if (ResultCode.SUCCESS.equals(isActorResult.getCode())) {
                Boolean isActor = isActorResult.getData();
                if (isActor) {
                    result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                    return result;
                }
            }
            
            if (ResultCode.ERROR_FORBIDEN_BY_INSPECTION.equals(isActorResult.getCode())) {
                result.addProperty("TagCode", "05110806");
                return result;
            }
            
            result.addProperty("TagCode", TagCodeEnum.SINGLE_CHAT_NOT_ACTOR);
            return result;
        } catch (Exception e) {
            logger.error("Module Error SingleChatService.checkActor(" + actorId + ")", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
    }
    
    /**
     * 校验用户能否接入1v1视频聊天【50001108】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject checkUser(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int userId;
        int actorId;
        int v;
        int platform;
        
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, Integer.MIN_VALUE, Integer.MAX_VALUE);
            actorId = CommonUtil.getJsonParamInt(jsonObject, "actorId", 0, TagCodeEnum.ACTOR_ID_MISSING, Integer.MIN_VALUE, Integer.MAX_VALUE);
            v = CommonUtil.getJsonParamInt(jsonObject, "v", 0, null, 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        // 校验用户是不是被封号
        try {
            SingleChatService singleChatService = MelotBeanFactory.getBean("singleChatService", SingleChatService.class);
            Result<Boolean> checkUserResult = singleChatService.checkUser(userId);
            if (checkUserResult == null || checkUserResult.getCode() == null) {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            if (ResultCode.ERROR_USER_NO_MOBILE.equals(checkUserResult.getCode())) {
                // 安卓版本在101以下的，IOS的133，提醒用户更新手机版本
                if ((PlatformEnum.ANDROID == platform && v <= 101)
                        ||(PlatformEnum.IPHONE == platform && v < 133)) {
                    result.addProperty("TagCode", "20001006");
                    return result;
                }else {
                    result.addProperty("TagCode", "05110807");
                    return result;
                }
            }
            if (ResultCode.ERROR_FORBIDEN_BY_INSPECTION.equals(checkUserResult.getCode())) {
                result.addProperty("TagCode", "05110806");
                return result;
            }
            if (ResultCode.SUCCESS.equals(checkUserResult.getCode())) {
                boolean canChat = checkUserResult.getData();
                if (!canChat) {
                    result.addProperty("TagCode", ERROR_USER_IS_FORBIDEN);
                    return result;
                }
            }
        } catch (Exception e) {
            logger.error("Module Error SingleChatService.checkActor(" + userId + ")", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
        
        // 校验用户请求的主播有没有开播权限
        JsonObject checkActorResult = checkActor(jsonObject, checkTag, request);
        if (!TagCodeEnum.SUCCESS.equals(checkActorResult.get("TagCode").getAsString())) {
            logger.info("checkActor(" + actorId + ")" + checkActorResult);
            result.addProperty("TagCode", ERROR_ACTOR_CAN_NOT_CHAT);
            return result;
        }
        
        // 校验主播处于什么状态
        try {
            SingleChatService singleChatService = MelotBeanFactory.getBean("singleChatService", SingleChatService.class);
            Result<HistSingleChatInfo> getSingleChatInfoByActorIdResult = singleChatService.getSingleChatInfoByActorId(actorId);
            if (getSingleChatInfoByActorIdResult == null || getSingleChatInfoByActorIdResult.getCode() == null) {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            if (ResultCode.SUCCESS.equals(getSingleChatInfoByActorIdResult.getCode())) {
                HistSingleChatInfo info = getSingleChatInfoByActorIdResult.getData();
                if (info == null) {
                    result.addProperty("TagCode", ERROR_ACTOR_IS_END);
                    return result;
                }
                
                if (info.getState() == 1) {
                    result.addProperty("TagCode", ERROR_ACTOR_IS_CHATTING);
                    return result;
                }
            }
            
        } catch (Exception e) {
            logger.error("Module Error SingleChatService.getSingleChatInfoByActorId(" + actorId + ")", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
        
        // 获取用户试用券数量
        int ticketNum = 0;
        try {
            SingleChatService singleChatService = MelotBeanFactory.getBean("singleChatService", SingleChatService.class);
            Result<SingleChatUserInfo> getUserInfoResult = singleChatService.getUserInfo(userId);
            if (getUserInfoResult != null 
                    && getUserInfoResult.getCode() != null 
                    && ResultCode.SUCCESS.equals(getUserInfoResult.getCode())) {
                ticketNum = getUserInfoResult.getData().getTicketNum();
            }
        } catch (Exception e) {
            logger.error("Module Error: singleChatService.getUserInfo(" + userId + ")", e);
        }
        
        // 用户能否对该主播使用券
        boolean canUseTicket = false;
        try {
            SingleChatService singleChatService = MelotBeanFactory.getBean("singleChatService", SingleChatService.class);
            Result<Boolean> canUseChatTicketResult = singleChatService.canUseChatTicket(userId, actorId, 100005);
            if (canUseChatTicketResult != null 
                    && canUseChatTicketResult.getCode() != null 
                    && ResultCode.SUCCESS.equals(canUseChatTicketResult.getCode())) {
                canUseTicket = canUseChatTicketResult.getData();
            }
        } catch (Exception e) {
            logger.error("Module Error: singleChatService.getUserInfo(" + userId + ")", e);
        }
        
        // 获取主播开播的单位时间收入
        Integer price = 0;
        try {
            SingleChatService singleChatService = MelotBeanFactory.getBean("singleChatService", SingleChatService.class);
            Result<Integer> priceResult = singleChatService.getActorPrice(actorId);
            
            if (priceResult == null || priceResult.getCode() == null) {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            
            if (ResultCode.SUCCESS.equals(priceResult.getCode())) {
                price = priceResult.getData();
            } else {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
        } catch (Exception e) {
            logger.error("Module Error SingleChatService.getActorPrice(" + actorId + ")", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
        
        // 校验用户有没有足够的秀币【默认用户是没有足够秀币的】
        if (ticketNum <= 0 || !canUseTicket) {
            // 获取用户秀币数，并判断够不够
            try {
                KkUserService kkUserService = MelotBeanFactory.getBean("kkUserService", KkUserService.class);
                UserAssets userAssets = kkUserService.getUserAssets(userId);
                if (userAssets == null || userAssets.getShowMoney() < price) {
                    result.addProperty("TagCode", ERROR_USER_LACK_MONEY);
                    return result;
                }
            } catch (Exception e) {
                logger.error("Module Error KkUserService.getUserAssets(" + userId + ")", e);
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
        }
        
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        result.addProperty("canUseTicket", canUseTicket);
        result.addProperty("ticketNum", ticketNum);
        result.addProperty("price", price);
        return result;
    }
    
    /**
     * 根据用户判断1v1大厅是否显示bar【50001110】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject checkBar(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int userId;
//        String token;
        
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, Integer.MIN_VALUE, Integer.MAX_VALUE);
//            token = CommonUtil.getJsonParamString(jsonObject, "token", null, TagCodeEnum.TOKEN_INCORRECT, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        // 获取用户有的券数
        Integer ticketNum = -1;
        try {
            TicketService ticketService = MelotBeanFactory.getBean("ticketService", TicketService.class);
            
            // 获取用户券的数量
            List<UserTicketInfo>  ticketInfos = ticketService.getUserRemainTickets(userId, String.valueOf(100005));
            if (ticketInfos != null && !ticketInfos.isEmpty() 
                    && ticketInfos.get(0) != null && ticketInfos.get(0).getRemainCount() >= 0) {
                ticketNum = ticketInfos.get(0).getRemainCount();
                result.addProperty("ticketNum", ticketNum);
            }
        } catch (Exception e) {
            logger.error(String.format("Module error :ticketService.getUserRemainTickets(%s,%s)", userId, 100005), e);
        }
        
        // 需要跟活动沟通如何校验用户大厅界面是否显示，以及显示的url
        JsonObject json = new JsonObject();
        try {
            int flag = 0;
            String url = "";
            String desc = ""; 
            
            json.addProperty("type", 148);
            json.addProperty("userId", userId );
            json.addProperty("key", "451a1sd8asd1asda6fdas89aw9");
            MissionService missionService = MelotBeanFactory.getBean("missionService", MissionService.class);
            JsonObject activeInfo = missionService.doActivityService(json);
            logger.info("MissionService.doActivityService(" + json + "):" + activeInfo);
            if (activeInfo != null) {
                if (!activeInfo.get("flag").isJsonNull()) {
                    flag = activeInfo.get("flag").getAsInt();
                }
                if (!activeInfo.get("url").isJsonNull()) {
                    url = activeInfo.get("url").getAsString();
                }
                if (!activeInfo.get("desc").isJsonNull()) {
                    desc = activeInfo.get("desc").getAsString();
                }
            }
            if (!StringUtil.strIsNull(url)) {
                if (StringUtil.strIsNull(desc)) {
                    desc = MelotBeanFactory.getBean("singleChatActiveDefaultDesc", String.class);
                }
                result.addProperty("desc", desc);
                result.addProperty("url", url);
            }
        } catch (Exception e) {
            logger.error(String.format("Module error: MissionService.doActivityService(%s)", json), e);
        }
        
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 主播发布技能服务【51060101】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject publishServer(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        int userId;
        int serverId;
        int typeId;
        int price;
        int unit;
        String content;
        String labels;
        String resVideo;
        String resAudio;
        String resImage;
        
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "5106010101", Integer.MIN_VALUE, Integer.MAX_VALUE);
            serverId = CommonUtil.getJsonParamInt(jsonObject, "serverId", 0, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
            typeId = CommonUtil.getJsonParamInt(jsonObject, "typeId", 0, "5106010102", Integer.MIN_VALUE, Integer.MAX_VALUE);
            price = CommonUtil.getJsonParamInt(jsonObject, "price", 0, "5106010103", Integer.MIN_VALUE, Integer.MAX_VALUE);
            unit = CommonUtil.getJsonParamInt(jsonObject, "unit", 0, "5106010104", Integer.MIN_VALUE, Integer.MAX_VALUE);
            
            content = CommonUtil.getJsonParamString(jsonObject, "content", null, "5106010105", 0, Integer.MAX_VALUE);
            labels = CommonUtil.getJsonParamString(jsonObject, "labels", null, "5106010106", 0, Integer.MAX_VALUE);
            resVideo = CommonUtil.getJsonParamString(jsonObject, "resVideo", null, null, 0, Integer.MAX_VALUE);
            resAudio = CommonUtil.getJsonParamString(jsonObject, "resAudio", null, null, 0, Integer.MAX_VALUE);
            resImage = CommonUtil.getJsonParamString(jsonObject, "resImage", null, null, 0, Integer.MAX_VALUE);
            
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        // 处理视频资源域名
        if (!StringUtil.strIsNull(resVideo)) {
            resVideo = replaceDomain(resVideo, ConfigHelper.getVideoURL(), "");
            resVideo = replaceDomain(resVideo, "/kktv", "");
        }
        
        // 处理音频资源域名
        if (!StringUtil.strIsNull(resAudio)) {
            resAudio = replaceDomain(resAudio, ConfigHelper.getMediahttpdir(), "");
            resAudio = replaceDomain(resAudio, "/kktv", "");
        }
        
        // 处理图片资源域名
        if (!StringUtil.strIsNull(resImage)) {
            resImage = replaceDomain(resImage, ConfigHelper.getHttpdir(), "");
            resImage = replaceDomain(resImage, "/kktv", "");
        }
        
        SingleChatServer singleChatServer = new SingleChatServer();
        singleChatServer.setUserId(userId);
        singleChatServer.setServerId(serverId);
        singleChatServer.setTypeId(typeId);
        singleChatServer.setPrice(price);
        singleChatServer.setUnit(unit);
        
        singleChatServer.setContent(content);
        singleChatServer.setLabels(labels);
        singleChatServer.setResVideo(resVideo);
        singleChatServer.setResAudio(resAudio);
        singleChatServer.setResImage(resImage);
        
        try {
            SingleChatServerService singleChatServerService = MelotBeanFactory.getBean("singleChatServerService", SingleChatServerService.class);
            Result<Integer> saveResult = singleChatServerService.saveSingleChatServer(singleChatServer);
            if (saveResult == null) {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            
            //SQL异常
            if (ResultCode.ERROR_SQL.equals(saveResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
                return result;
            }
            
            // 模块异常
            if (ResultCode.ERROR_MODULE.equals(saveResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            
            // 有敏感字
            if (ResultCode.ERROR_HAVE_SENSITIVE_WORDS.equals(saveResult.getCode())) {
                result.addProperty("TagCode", "5106010107");
                return result;
            }
            
            // 有无效标签
            if (ResultCode.ERROR_HAVE_INVALID_LABELS.equals(saveResult.getCode())) {
                result.addProperty("TagCode", "5106010108");
                return result;
            }
            
            if (ResultCode.SUCCESS.equals(saveResult.getCode())) {
                Integer id = saveResult.getData();
                if (id == null || id <= 0) {
                    result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
                    return result;
                }
                result.addProperty("serverId", id);
            }else {
                // 模块的异常没有被捕获
                result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                logger.error(saveResult);
                return result;
            }
            
        } catch (Exception e) {
            logger.error("Module Error singleChatServerService.saveSingleChatServer(" + singleChatServer + ")", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
        
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 获取主播的技能服务详情【51060102】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getServerInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int typeId;
        int actorId;
        
        try {
            typeId = CommonUtil.getJsonParamInt(jsonObject, "typeId", 0, "5106010201", Integer.MIN_VALUE, Integer.MAX_VALUE);
            actorId = CommonUtil.getJsonParamInt(jsonObject, "actorId", 0, "5106010202", Integer.MIN_VALUE, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            SingleChatServerService singleChatServerService = MelotBeanFactory.getBean("singleChatServerService", SingleChatServerService.class);
            Result<SingleChatServer> serverResult = singleChatServerService.getSingleChatServerInfo(typeId, actorId);
            if (serverResult == null) {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            if (ResultCode.SUCCESS.equals(serverResult.getCode())) {
                SingleChatServer singleChatServer = serverResult.getData();
                if (singleChatServer == null) {
                    result.addProperty("state", -1);
                    result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                    return result;
                }
                
                if (singleChatServer.getState() == 3) {
                    result.addProperty("state", 3);
                    result.addProperty("checkContent", singleChatServer.getCheckContent() == null ? "" : singleChatServer.getCheckContent());
                    result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                    return result;
                }
                
                result = SingleChatServerTF.serverInfoToJson(singleChatServer);

                // 获取开播状态
                try {
                    SingleChatService singleChatService = MelotBeanFactory.getBean("singleChatService", SingleChatService.class);
                    Result<HistSingleChatInfo> infoResult = singleChatService.getSingleChatInfoByActorId(singleChatServer.getUserId());
                    if (infoResult == null || infoResult.getData() == null) {
                        result.addProperty("actorState", 2);
                    }else {
                        result.addProperty("actorState", infoResult.getData().getState());
                    }
                    
                } catch (Exception e) {
                    logger.error("Module Error singleChatService.getSingleChatInfoByActorId(" + singleChatServer.getUserId() + ")", e);
                    result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                    return result;
                }
            }else {
                // 模块的异常没有被捕获
                result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                logger.error(serverResult);
                return result;
            }
        } catch (Exception e) {
            logger.error("Module Error SingleChatServerService.getSingleChatServerInfo(" + typeId + "," + actorId + ")", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
        
        result.addProperty("mediaPathPrefix", ConfigHelper.getVideoURL());
        result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 删除主播技能服务【51060103】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject delServer(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        int serverId;
        int userId;
        
        try {
            serverId = CommonUtil.getJsonParamInt(jsonObject, "serverId", 0, "5106010301", Integer.MIN_VALUE, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "5106010302", Integer.MIN_VALUE, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            SingleChatServerService singleChatServerService = MelotBeanFactory.getBean("singleChatServerService", SingleChatServerService.class);
            Result<Integer> delResult = singleChatServerService.delSingleChatServer(userId, serverId);
            if (delResult == null) {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            if (ResultCode.SUCCESS.equals(delResult.getCode())) {
                Integer count = delResult.getData();
                if (count == null || count < 0) {
                    result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
                    return result;
                }
            }else {
                // 模块的异常没有被捕获
                result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                logger.error(delResult);
                return result;
            }
        } catch (Exception e) {
            logger.error("Module Error SingleChatServerService.delSingleChatServer(" + userId + "," + serverId + ")", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 获取大厅技能服务列表【51060104】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getServerList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int typeId;
        int start;
        int offset;
        
        try {
            typeId = CommonUtil.getJsonParamInt(jsonObject, "typeId", 0, "5106010401", Integer.MIN_VALUE, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
            offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 20, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            SingleChatServerService singleChatServerService = MelotBeanFactory.getBean("singleChatServerService", SingleChatServerService.class);
            Result<PageSingleChatServer> pageServerListResult = singleChatServerService.getSingleChatServers(typeId, 1, start, offset);
            if (pageServerListResult == null) {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            if (ResultCode.SUCCESS.equals(pageServerListResult.getCode())) {
                PageSingleChatServer pageSingleChatServer = pageServerListResult.getData();
                List<SingleChatServer> serverList = new ArrayList<>();
                result.addProperty("count", pageSingleChatServer.getCount());
                
                if (pageSingleChatServer.getServers() != null) {
                    serverList = pageSingleChatServer.getServers();
                }
                
                JsonArray servers = new JsonArray();
                for (SingleChatServer singleChatServer : serverList) {
                    JsonObject serverJson = new JsonObject();
                    serverJson.addProperty("serverId", singleChatServer.getServerId());
                    serverJson.addProperty("typeId", singleChatServer.getTypeId());
                    serverJson.addProperty("actorId", singleChatServer.getUserId());
                    serverJson.addProperty("price", singleChatServer.getPrice());
                    serverJson.addProperty("unit", singleChatServer.getUnit());
                    
                    // 获取开播状态
                    try {
                        SingleChatService singleChatService = MelotBeanFactory.getBean("singleChatService", SingleChatService.class);
                        Result<HistSingleChatInfo> infoResult = singleChatService.getSingleChatInfoByActorId(singleChatServer.getUserId());
                        if (infoResult == null || infoResult.getData() == null) {
                            serverJson.addProperty("actorState", 2);
                        }else {
                            serverJson.addProperty("actorState", infoResult.getData().getState());
                        }
                        
                    } catch (Exception e) {
                        logger.error("Module Error singleChatService.getSingleChatInfoByActorId(" + typeId + ")", e);
                        result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                        return result;
                    }
                    
                    // 获取用户昵称信息
                    try {
                        KkUserService kkUserService = MelotBeanFactory.getBean("kkUserService", KkUserService.class);
                        UserProfile userProfile = kkUserService.getUserProfile(singleChatServer.getUserId());
                        if (userProfile != null && userProfile.getNickName() != null) {
                            serverJson.addProperty("nickname", userProfile.getNickName());
                        }else {
                            serverJson.addProperty("nickname", "");
                        }
                    } catch (Exception e) {
                        logger.error("Module Error KkUserService.getUserProfile(" + singleChatServer.getUserId() + ")", e);
                        result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                        return result;
                    }
                    
                    // 获取用户海报信息
                    try {
                        RoomInfoService roomInfoService = MelotBeanFactory.getBean("roomInfoService", RoomInfoService.class);
                        RoomInfo roomInfo = roomInfoService.getRoomInfoById(singleChatServer.getUserId());
                        if (roomInfo != null && roomInfo.getPoster() != null) {
                            serverJson.addProperty("posterPath_256", roomInfo.getPoster() + "!256");
                        }else {
                            serverJson.addProperty("posterPath_256", "");
                        }
                            
                    } catch (Exception e) {
                        logger.error("Module Error roomInfoService.getRoomInfoById(" + singleChatServer.getUserId() + ")", e);
                        result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                        return result;
                    }
                    servers.add(serverJson);
                }
                
                result.add("servers", servers);
            }else {
                // 模块的异常没有被捕获
                result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                logger.error(pageServerListResult);
                return result;
            }
        } catch (Exception e) {
            logger.error("Module Error SingleChatServerService..getSingleChatServers(" + typeId + "," + 1 + "," + start + "," + offset + ")", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
        
        result.addProperty("mediaPathPrefix", ConfigHelper.getVideoURL());
        result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 获取主播标签【51060105】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getServerLables(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int typeId;
        int actorId;
        
        try {
            typeId = CommonUtil.getJsonParamInt(jsonObject, "typeId", 0, "5106010501", Integer.MIN_VALUE, Integer.MAX_VALUE);
            actorId = CommonUtil.getJsonParamInt(jsonObject, "actorId", 0, "5106010502", Integer.MIN_VALUE, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            SingleChatServerService singleChatServerService = MelotBeanFactory.getBean("singleChatServerService", SingleChatServerService.class);
            Result<SingleChatServer> serverResult = singleChatServerService.getSingleChatServerInfo(typeId, actorId);
            if (serverResult == null) {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            if (ResultCode.SUCCESS.equals(serverResult.getCode())) {
                SingleChatServer server = serverResult.getData();
                List<SingleChatLabel> labelList = new ArrayList<>();
                if (server != null && server.getLabelInfos() != null) {
                    labelList = server.getLabelInfos();
                }
                String labelStr = new Gson().toJson(labelList);
                
                JsonArray labels = new JsonParser().parse(labelStr).getAsJsonArray();
                result.add("labels", labels);
            }else {
                // 模块的异常没有被捕获
                result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                logger.error(serverResult);
                return result;
            }
            
            
        } catch (Exception e) {
            logger.error("Module Error SingleChatServerService.getSingleChatServerInfo(" + typeId + "," + actorId + ")", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
        
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 获取主播审核失败的技能服务数量【51060106】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getInvalidServerCount(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        int typeId;
        int userId;
        
        try {
            typeId = CommonUtil.getJsonParamInt(jsonObject, "typeId", 0, "5106010601", Integer.MIN_VALUE, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "5106010602", Integer.MIN_VALUE, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        // 获取状态
        try {
            SingleChatServerService singleChatServerService = MelotBeanFactory.getBean("singleChatServerService", SingleChatServerService.class);
            Result<SingleChatServer> serverResult = singleChatServerService.getSingleChatServerInfo(typeId, userId);
            if (serverResult == null) {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            if (ResultCode.SUCCESS.equals(serverResult.getCode())) {
                SingleChatServer server = serverResult.getData();
                if (server == null) {
                    result.addProperty("count", 0);
                    result.addProperty("checkState", -1);
                    result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                    return result;
                }
                result.addProperty("checkState", server.getState());
            }else {
                // 模块的异常没有被捕获
                result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                logger.error(serverResult);
                return result;
            }
        } catch (Exception e) {
            logger.error("Module Error SingleChatServerService.getSingleChatServerInfo(" + typeId + "," + userId + ")", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
        
        // 获取审核失败的数量
        try {
            SingleChatServerService singleChatServerService = MelotBeanFactory.getBean("singleChatServerService", SingleChatServerService.class);
            Result<Integer> countResult = singleChatServerService.countInvalidServer(typeId, userId);
            if (countResult == null) {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            if (ResultCode.SUCCESS.equals(countResult.getCode())) {
                result.addProperty("count", countResult.getData());
            }else {
                // 模块的异常没有被捕获
                result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                logger.error(countResult);
                return result;
            }
        } catch (Exception e) {
            logger.error("Module Error SingleChatServerService.countInvalidServer(" + typeId + "," + userId + ")", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
        
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 获取默认服务金额【51060107】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getDefultPrice(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int typeId;
        
        try {
            typeId = CommonUtil.getJsonParamInt(jsonObject, "typeId", 0, "5106010701", Integer.MIN_VALUE, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            SingleChatServerService singleChatServerService = MelotBeanFactory.getBean("singleChatServerService", SingleChatServerService.class);
            Result<SingleChatServerPrice> priceResult = singleChatServerService.getDefaultServerPrice(typeId);
            if (priceResult == null) {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            
            if (ResultCode.ERROR_CONFIG.equals(priceResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.CONFIG_KEY_NOT_EXIST);
                return result;
            }
            
            if (ResultCode.SUCCESS.equals(priceResult.getCode())) {
                SingleChatServerPrice price = priceResult.getData();
                result.addProperty("price", price.getPrice());
                result.addProperty("unit", price.getUnit());
            }else {
                result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                logger.error(priceResult);
                return result;
            }
        } catch (Exception e) {
            logger.error("Module Error SingleChatServerService.getDefaultServerPrice(" + typeId + ")", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
        
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 获取1v1技能服务标签列表【51060108】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getServerLabels(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int typeId;
        
        try {
            typeId = CommonUtil.getJsonParamInt(jsonObject, "typeId", 0, "5106010701", Integer.MIN_VALUE, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            SingleChatServerService singleChatServerService = MelotBeanFactory.getBean("singleChatServerService", SingleChatServerService.class);
            Result<PageSingleChatLabel> pageResult = singleChatServerService.getAllSingleChatLabels(typeId, null, null);
            if (pageResult == null) {
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            
            if (ResultCode.SUCCESS.equals(pageResult.getCode())) {
                ArrayList<SingleChatLabel> labelList = pageResult.getData().getLabels();
                JsonArray labels = new JsonArray();
                for (SingleChatLabel label : labelList) {
                    JsonObject labelJson = new JsonObject();
                    labelJson.addProperty("id", label.getId());
                    labelJson.addProperty("name", label.getName());
                    
                    labels.add(labelJson);
                }
                
                result.add("labels", labels);
            }else {
                result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                logger.error(pageResult);
                return result;
            }
        } catch (Exception e) {
            logger.error("Module Error SingleChatServerService.getDefaultServerPrice(" + typeId + ")", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
        
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 替换域名
     * @param urls  逗号隔开的url列表
     * @param from  需要替换的字符
     * @param to    替换后的字符
     * @return
     */
    private String replaceDomain(String urls, String from, String to) {
        if (StringUtil.strIsNull(urls)) {
            return urls;
        }
        List<String>urlList =  Arrays.asList(urls.split(","));
        StringBuilder builder = new StringBuilder();
        for (String url : urlList) {
            builder.append(url.replaceFirst(from, to)).append(",");
        }
        
        return builder.toString();
    }
}

package com.melot.kktv.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.singlechat.driver.base.Result;
import com.melot.singlechat.driver.base.ResultCode;
import com.melot.singlechat.driver.domain.HistSingleChatInfo;
import com.melot.singlechat.driver.domain.SingleChatActorInfo;
import com.melot.singlechat.driver.domain.SingleChatRoomInfo;
import com.melot.singlechat.driver.domain.SingleChatUserInfo;
import com.melot.singlechat.driver.service.SingleChatService;
import com.melot.kk.activity.driver.MissionService;
import com.melot.kkcore.user.api.UserAssets;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kkcx.transform.RoomTF;
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
        
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, Integer.MIN_VALUE, Integer.MAX_VALUE);
            actorId = CommonUtil.getJsonParamInt(jsonObject, "actorId", 0, TagCodeEnum.ACTOR_ID_MISSING, Integer.MIN_VALUE, Integer.MAX_VALUE);
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
        String token;
        
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, Integer.MIN_VALUE, Integer.MAX_VALUE);
            token = CommonUtil.getJsonParamString(jsonObject, "token", null, TagCodeEnum.TOKEN_INCORRECT, 1, Integer.MAX_VALUE);
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
            json.addProperty("token", token);
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
            if (flag == 1) {
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
}

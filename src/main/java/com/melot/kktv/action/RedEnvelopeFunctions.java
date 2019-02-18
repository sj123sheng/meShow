package com.melot.kktv.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import com.melot.kkcore.actor.api.RoomInfo;
import com.melot.kkcore.actor.service.ActorService;
import com.melot.kktv.service.UserService;
import com.melot.kktv.util.*;
import com.melot.redenvelopers.driver.domain.*;
import com.melot.redenvelopers.driver.service.RedEnvelopersService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kktv.redis.HotDataSource;
import com.melot.kktv.service.GeneralService;
import com.melot.module.api.exceptions.MelotModuleException;


/**
 * 
 * Title: 红包接口功能实现类
 * <p>
 * Description: 
 * </p>
 *
 * @author <a href="mailto:baolin.zhu@melot.cn">朱宝林</a>
 * @version V1.1
 * @since 2018/6/27 15:30
 *
 * history
 * author 冯高攀<a href="mailto:gaopan.feng@melot.cn">
 * version V1.0
 * since 2016年1月15日 上午10:16:30
 */
public class RedEnvelopeFunctions {
    
    private static Logger logger = Logger.getLogger(RedEnvelopeFunctions.class);

    @Resource
    private RedEnvelopersService redEnvelopersService;

    @Resource
    private ActorService actorService;
    /**
     * 获取红包配置信息和房主红包金库秀币总额(20031005)
     * 
     * @param paramJsonObject 参数对象
     * @param checkTag 是否校验token
     * @param request 请求
     * @return 返回结果
     */
    public JsonObject getActorEvelopeCoffers(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        //解析参数
        int roomId;
        try {
            roomId = CommonUtil.getJsonParamInt(paramJsonObject, "roomId", 0, TagCodeEnum.ROOMID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            long amount = 0;
            if (roomId > 0) {
                 amount = redEnvelopersService.getActorCoffersAmount(roomId);
            }
            RedEnvelopersConfigModelExtend redEvelopModel = redEnvelopersService.getRedEnvelopersConfigModelV2();
            result.addProperty("amount", amount);
            result.addProperty("minCount", redEvelopModel.getMinCount());
            result.addProperty("maxCount", redEvelopModel.getMaxCount());
            result.addProperty("maxMoney", redEvelopModel.getMaxMoney());
            result.addProperty("validTime", redEvelopModel.getValidTime());
            result.addProperty("timelag", redEvelopModel.getTimelag());
            result.addProperty("minRichLevel", redEvelopModel.getMinRichLevel());
            result.addProperty("maxCoffers", redEvelopModel.getMaxCoffers());
            result.addProperty("needSecret", redEvelopModel.getNeedSecret());
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
        } catch(Exception e) {
            logger.error(String.format("Module Error: redEnvelopersService, roomId=%s", roomId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        
        return result;
    }
    
    /**
     * 发送红包接口(20031006)
     * 
     * @param paramJsonObject 参数对象
     * @param checkTag 是否验证token
     * @param request 请求
     * @return 返回抢红包结果
     */
    public JsonObject sendRedEvelope(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        //解析参数
        int userId, roomId, count, sendSpeak, appId, isDelay,v,platform;
        long amount, actorCoffers;
        String secretKey;
        try {
            //解析参数
            userId = CommonUtil.getJsonParamInt(paramJsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            roomId = CommonUtil.getJsonParamInt(paramJsonObject, "roomId", 0, TagCodeEnum.ROOMID_MISSING, 1, Integer.MAX_VALUE);
            amount = CommonUtil.getJsonParamLong(paramJsonObject, "amount", 0, "31060001", 1, Integer.MAX_VALUE);
            count = CommonUtil.getJsonParamInt(paramJsonObject, "count", 0, "31060002", 1, Integer.MAX_VALUE);
            actorCoffers = CommonUtil.getJsonParamLong(paramJsonObject, "actorCoffers", 0, null, 1, Integer.MAX_VALUE);
            sendSpeak = CommonUtil.getJsonParamInt(paramJsonObject, "sendSpeak", 0, null, 1, Integer.MAX_VALUE);
            isDelay = CommonUtil.getJsonParamInt(paramJsonObject, "isDelay", 0, null, 0, 1);
            appId = CommonUtil.getJsonParamInt(paramJsonObject, "a", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
            secretKey = CommonUtil.getJsonParamString(paramJsonObject, "secretKey", "", null, 1, 36);
            v = CommonUtil.getJsonParamInt(paramJsonObject, "v", 0, null, 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(paramJsonObject, "platform", PlatformEnum.WEB, null, PlatformEnum.WEB, PlatformEnum.IPAD);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        if(platform > 1 && v < 6210 && StringUtil.strIsNull(secretKey)){
            result.addProperty("TagCode", TagCodeEnum.LOW_VERSION_EXCEPTION);
            return result;
        }
        
        // 判断房间是否开播，未开播的房间不能发红包
        try {
            RoomInfo roomInfo = actorService.getRoomInfoById(roomId);
            boolean isNotLive = !(roomInfo != null && ((roomInfo.getLiveType() != null && roomInfo.getLiveType() >= 1) || roomInfo.getLiveEndTime() == null));
            if (isNotLive) {
                // 未开播的房间不能发红包
                result.addProperty(ParameterKeys.TAG_CODE, "31060008");
                return result;
            }
        } catch (Exception e) {
            logger.error(String.format("Module Error: actorService.getRoomInfoById(roomId=%s)", roomId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }

        try {
            // 判断是否绑定神秘人
            String mysTypeStr = HotDataSource.getHotFieldValue(String.valueOf(userId), "mysType");
            if (StringUtil.parseFromStr(mysTypeStr, 0) == 2) {
                result.addProperty(ParameterKeys.TAG_CODE, "31060004");
                return result;
            }
        } catch (Exception e) {
            logger.error(String.format("Redis Error: HotDataSource.getHotFieldValue(key=%s, field=mysType)", userId));
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }

        // 判断红包总金额是否大于红包个数，如果不是返回错误
        if (amount < count) {
            // 红包总金额不能小于红包个数
            result.addProperty(ParameterKeys.TAG_CODE, "31060009");
            return result;
        } else if (amount < 1000) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.LOW_VERSION_EXCEPTION);
            return result;
        }
        
        //调用模块方法，兼容老版本，如果是喇叭红包，默认延时
        if (sendSpeak == 1) {
            isDelay = 1;
        }
        RedEnvelopersInfoModelExtend redEnvelopersParam = new RedEnvelopersInfoModelExtend();
        redEnvelopersParam.setUserId(userId);
        redEnvelopersParam.setRoomId(roomId);
        redEnvelopersParam.setAmount(amount);
        redEnvelopersParam.setCount(count);
        redEnvelopersParam.setActorCoffers(actorCoffers);
        redEnvelopersParam.setHasSpeak(sendSpeak);
        redEnvelopersParam.setIsDelay(isDelay);
        redEnvelopersParam.setSecretKey(secretKey);
        try {
            if (userId != roomId && actorCoffers > 0) {
                // 不是房主不能使用红包金库
                result.addProperty(ParameterKeys.TAG_CODE, "31060003");
                return result;
            }
            long money = redEnvelopersService.insertSendRedEnvelopersV2(redEnvelopersParam, appId);
            result.addProperty("money", money);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
        } catch(MelotModuleException e) {
            int errCode = e.getErrCode();
            switch (errCode) {
                case 101:
                    // 参数非法
                    result.addProperty(ParameterKeys.TAG_CODE , "2003100602");
                    break;
                case 105:
                    // 用户财富等级未达到发送红包最低限制
                    result.addProperty(ParameterKeys.TAG_CODE , "31060004");
                    break;
                case 108:
                    // 秀币不足
                    result.addProperty(ParameterKeys.TAG_CODE , "31060005");
                    break;
                case 107:
                    // 主播红包金库不足
                    result.addProperty(ParameterKeys.TAG_CODE , "31060006");
                    break;
                case 104:
                    // 红包总金额不能超过 999999 秀币,且大于等于1000
                    result.addProperty(ParameterKeys.TAG_CODE , "31060007");
                    break;
                case 103:
                    // 红包总金额不能小于红包个数
                    result.addProperty(ParameterKeys.TAG_CODE , "31060009");
                    break;
                case 106:
                    result.addProperty(ParameterKeys.TAG_CODE , "2003100601");
                    break;
                case 111:
                    result.addProperty(ParameterKeys.TAG_CODE , TagCodeEnum.LOW_VERSION_EXCEPTION);
                    break;
                default:
                    logger.error(String.format("模块返回其他异常：errCode=%s, message=%s", errCode, e.getMessage()));
                    result.addProperty(ParameterKeys.TAG_CODE , TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                    break;
            }
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            logger.error(String.format("Module Error: redEnvelopersService.insertSendRedEnvelopers(redEnvelopersParam=%s, appId=%s)",
                    redEnvelopersParam, appId), e);
        }
        return result;
    }

    /**
     * 抢红包接口(20031007)
     * 
     * @param paramJsonObject 参数对象
     * @param checkTag 是否校验token
     * @param request 请求
     * @return 返回结果
     */
    public JsonObject grabRedEvelope(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        //验证token
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
      
        //解析参数
        int userId, roomId, appId, platform,v;
        String sendId,secretKey;
        try {
            userId = CommonUtil.getJsonParamInt(paramJsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            roomId = CommonUtil.getJsonParamInt(paramJsonObject, "roomId", 0, TagCodeEnum.ROOMID_MISSING, 1, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(paramJsonObject, "a", AppIdEnum.AMUSEMENT, null, 1, Integer.MAX_VALUE);
            sendId = CommonUtil.getJsonParamString(paramJsonObject, "sendId", "", "31070001", 1, 36);
            platform = CommonUtil.getJsonParamInt(paramJsonObject, "platform", PlatformEnum.WEB, null, PlatformEnum.WEB, PlatformEnum.IPAD);
            secretKey = CommonUtil.getJsonParamString(paramJsonObject, "secretKey", "", null, 1, 36);
            v = CommonUtil.getJsonParamInt(paramJsonObject, "v", 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        if(platform > 1 && v < 6210 && StringUtil.strIsNull(secretKey)){
            result.addProperty("TagCode", TagCodeEnum.LOW_VERSION_EXCEPTION);
            return result;
        }
        
        JsonArray redEvelopArray = new JsonArray();
        try {
            String userIp = GeneralService.getIpAddr(request, appId, platform, null);
            Map<String, Object> extraParams = new HashMap<>(2);
            extraParams.put("userIp", userIp);
            extraParams.put("secretKey",secretKey);
            //调用模块
            CurrentGetRedEnvelopersModel evelopModel = redEnvelopersService.insertGetRedEnvelopers(userId, roomId, sendId, extraParams);
            
            // 设置抢到的红包金额和用户当前秀币额
            if (evelopModel.getAmount() < 0) {
                switch (evelopModel.getAmount().intValue()) {
                case -102:
                    // 红包已抢完
                    result.addProperty(ParameterKeys.TAG_CODE , "31070003");
                    break;
                case -103:
                    // 红包已过期
                    result.addProperty(ParameterKeys.TAG_CODE , "31070004");
                    break;
                case -105:
                    // 不能重复领取同一个红包
                    result.addProperty(ParameterKeys.TAG_CODE , "31070006");
                    break;
                default:
                    // 红包已被抢完
                    result.addProperty(ParameterKeys.TAG_CODE , "31070003");
                    break;
                }
            } else {
                result.addProperty("getAmount", evelopModel.getAmount());
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            }
            result.addProperty("money", evelopModel.getShowMoney());
            //设置发红包的人信息
            RedEnvelopersInfoModel evelopInfoModel = evelopModel.getRedEnvelopersInfo();
            result.addProperty("sendId", evelopInfoModel.getSendId());
            result.addProperty("userId", evelopInfoModel.getUserId());
            
            UserProfile sendUser = UserService.getUserInfoV2(evelopInfoModel.getUserId());
            result.addProperty("nickName", sendUser.getNickName());
            
            if (sendUser.getPortrait() != null) {
                result.addProperty("portrait_path_original", ConfigHelper.getHttpdir() + sendUser.getPortrait());
                result.addProperty("portrait_path_1280", ConfigHelper.getHttpdir() + sendUser.getPortrait() + "!1280");
                result.addProperty("portrait_path_256", ConfigHelper.getHttpdir() + sendUser.getPortrait() + "!256");
                result.addProperty("portrait_path_128", ConfigHelper.getHttpdir() + sendUser.getPortrait() + "!128");
                result.addProperty("portrait_path_48", ConfigHelper.getHttpdir() + sendUser.getPortrait() + "!48");
            }
            
            result.addProperty("gender", sendUser.getGender());
            result.addProperty("amount", evelopInfoModel.getAmount());
            result.addProperty("count", evelopInfoModel.getCount());
            result.addProperty("state", evelopInfoModel.getState());
            result.addProperty("dtime", evelopInfoModel.getDtime());
            
            //添加红包详情参数,抢红包的列表
            List<GetRedEnvelopersModel> getEvelopesList = evelopInfoModel.getGetList();
            if (getEvelopesList != null && getEvelopesList.size() > 0) {
                JsonObject evelopJson = null;
                for (GetRedEnvelopersModel redEvelopModel : getEvelopesList) {
                    evelopJson = new JsonObject();
                    evelopJson.addProperty("userId", redEvelopModel.getUserId());
                    
                    if (redEvelopModel.getNickname() != null) {
                    	evelopJson.addProperty("nickName", redEvelopModel.getNickname());
                    } else {
                        UserProfile user = UserService.getUserInfoV2(redEvelopModel.getUserId());
                        if (user != null) {
                            evelopJson.addProperty("nickName", user.getNickName());
                        }
                    }
                    
                    evelopJson.addProperty("dtime", redEvelopModel.getDtime());
                    evelopJson.addProperty("amount", redEvelopModel.getAmount());
                    redEvelopArray.add(evelopJson);
                }
                result.add("getList", redEvelopArray);
            }
        } catch(MelotModuleException e) {
            int errCode = e.getErrCode();
            switch (errCode) {
            case 101:
                // 所抢红包无效
                result.addProperty(ParameterKeys.TAG_CODE , "31070002");
                break;
            case 102:
                // 红包已被抢完
                result.addProperty(ParameterKeys.TAG_CODE , "31070003");
                break;
            case 103:
                // 红包已过期
                result.addProperty(ParameterKeys.TAG_CODE , "31070004");
                break;
            case 104:
                // 财富等级三级或以上才能抢红包，返回这个错误码时需要返回最低可抢红包的财富等级
                result.addProperty("minRichLevel", StringUtil.parseFromStr(e.getMessage(), 0));
                result.addProperty(ParameterKeys.TAG_CODE , "31070007");
                break;
            case 105:
                // 已领取过该红包
                result.addProperty(ParameterKeys.TAG_CODE, "31070006");
                break;
            case 106:
                // 非vip用户每天最多只能抢到3个红包
                result.addProperty(ParameterKeys.TAG_CODE, "31070008");
                break;
            case 107:
                // 	普通VIP用户每天最多只能抢50个红包
                result.addProperty(ParameterKeys.TAG_CODE, "31070009");
                break;
            case 108:
                result.addProperty(ParameterKeys.TAG_CODE, "31070010");
                break;
            default:
                result.addProperty(ParameterKeys.TAG_CODE , TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                break;
            }
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            
            logger.error("RedEnvelopersService.insertGetRedEnvelopers("
                    + userId + ", " + roomId + ", " + sendId
                    + ") execute exception.", e);
        }
        
        return result;
    }

    /**
     * 查询房间红包记录接口(20031008)
     * 
     * @param paramJsonObject 参数对象
     * @param checkTag 是否需要校验
     * @param request 请求
     * @return 返回结果
     */
    public JsonObject getRoomRedEvelopeRecords(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        //解析参数
        int roomId;
        try {
            roomId = CommonUtil.getJsonParamInt(paramJsonObject, "roomId", 0, TagCodeEnum.ROOMID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        JsonArray listArray = new JsonArray();
        
        //调用模块
        try {
            List<RedEnvelopersInfoModel> redInfoModels = redEnvelopersService.getRoomRedEnvelopersList(roomId);
            JsonObject evelopJson;
            JsonArray redEvelops;
            List<GetRedEnvelopersModel> list;
            JsonObject redEvelopJson;
            
            if (redInfoModels != null && redInfoModels.size() > 0) {
                for (RedEnvelopersInfoModel redEvelop : redInfoModels) {
                    evelopJson = new JsonObject();
                    evelopJson.addProperty("sendId", redEvelop.getSendId());
                    evelopJson.addProperty("userId", redEvelop.getUserId());
                    
                    UserProfile sendUser = UserService.getUserInfoV2(redEvelop.getUserId());
                    evelopJson.addProperty("nickName", sendUser.getNickName());
                    
                    if (sendUser.getPortrait() != null) {
                        evelopJson.addProperty("portrait_path_original", ConfigHelper.getHttpdir() + sendUser.getPortrait());
                        evelopJson.addProperty("portrait_path_1280", ConfigHelper.getHttpdir() + sendUser.getPortrait() + "!1280");
                        evelopJson.addProperty("portrait_path_256", ConfigHelper.getHttpdir() + sendUser.getPortrait() + "!256");
                        evelopJson.addProperty("portrait_path_128", ConfigHelper.getHttpdir() + sendUser.getPortrait() + "!128");
                        evelopJson.addProperty("portrait_path_48", ConfigHelper.getHttpdir() + sendUser.getPortrait() + "!48");
                    }
                    
                    evelopJson.addProperty("gender", sendUser.getGender());
                    evelopJson.addProperty("amount", redEvelop.getAmount());
                    evelopJson.addProperty("count", redEvelop.getCount());
                    evelopJson.addProperty("state", redEvelop.getState());
                    evelopJson.addProperty("dtime", redEvelop.getDtime());
                    
                    list = redEvelop.getGetList();
                    if (list != null && list.size() > 0) {
                        redEvelops = new JsonArray();
                        for (GetRedEnvelopersModel redEvelopModel : list) {
                            redEvelopJson = new JsonObject();
                            
                            redEvelopJson.addProperty("userId", redEvelopModel.getUserId());
                            
                            if (redEvelopModel.getNickname() != null) {
                                redEvelopJson.addProperty("nickName", redEvelopModel.getNickname());
                            } else {
                                UserProfile user = UserService.getUserInfoV2(redEvelopModel.getUserId());
                            	if (user != null) {
                            		redEvelopJson.addProperty("nickName", user.getNickName());
                            	}
                            }
                            
                            redEvelopJson.addProperty("dtime", redEvelopModel.getDtime());
                            redEvelopJson.addProperty("amount", redEvelopModel.getAmount());
                            redEvelops.add(redEvelopJson);
                        }
                        evelopJson.add("getList", redEvelops);
                    }
                    
                    listArray.add(evelopJson);
                }
            }
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            
            logger.error("RedEnvelopersService.getRoomRedEnvelopersList("
                    + roomId + ") execute exception.", e);
        }
        
        result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
        result.add("list", listArray);
        
        return result;
    }

    /**
     * 查询房间延时红包记录接口(51011102)
     *
     * @param paramJsonObject   参数对象
     * @param checkTag          是否需要校验
     * @param request           请求
     * @return                  返回结果
     */
    public JsonObject getDelayRedEnvelopeRecord(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        // 解析参数
        int roomId,userId;
        try {
            roomId = CommonUtil.getJsonParamInt(paramJsonObject, "roomId", 0, TagCodeEnum.ROOMID_MISSING, 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(paramJsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        JsonArray listArray = new JsonArray();
        //调用模块
        try {
            List<RedEnvelopersInfoModelExtend> redInfoModels = redEnvelopersService.getDelayRedEnveloperListByRoomId(roomId);
            if (CollectionUtils.isNotEmpty(redInfoModels)) {
                JsonObject jsonObject;
                for (RedEnvelopersInfoModelExtend redInfoModel : redInfoModels) {
                    jsonObject = new JsonObject();
                    jsonObject.addProperty("sendId", redInfoModel.getSendId());
                    jsonObject.addProperty("userId", redInfoModel.getUserId());
                    UserProfile user = UserService.getUserInfoV2(redInfoModel.getUserId());
                    if (user != null) {
                        jsonObject.addProperty("nickName", user.getNickName());
                    }
                    jsonObject.addProperty("amount", redInfoModel.getAmount());
                    jsonObject.addProperty("dtime", redInfoModel.getDtime());
                    jsonObject.addProperty("redEnveloperName", redInfoModel.getRedEnveloperName());
                    jsonObject.addProperty("deadLine", redInfoModel.getDeadLine());
                    if(checkTag&&(userId==roomId||userId == redInfoModel.getUserId())){
                        jsonObject.addProperty("secretKey", redInfoModel.getSecretKey());
                    }
                    listArray.add(jsonObject);
                }
            }
        } catch (Exception e) {
            logger.error(String.format("Error:redEnvelopersService.getDelayRedEnveloperListByRoomId(roomId=%s)", roomId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
        result.addProperty("systemTime", System.currentTimeMillis());
        result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
        result.add("list", listArray);
        return result;
    }
}

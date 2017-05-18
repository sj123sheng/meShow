package com.melot.kktv.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kktv.redis.HotDataSource;
import com.melot.kktv.service.GeneralService;
import com.melot.kktv.service.RoomService;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.module.api.exceptions.MelotModuleException;
import com.melot.redenvelopers.driver.domain.CurrentGetRedEnvelopersModel;
import com.melot.redenvelopers.driver.domain.GetRedEnvelopersModel;
import com.melot.redenvelopers.driver.domain.RedEnvelopersConfigModel;
import com.melot.redenvelopers.driver.domain.RedEnvelopersInfoModel;
import com.melot.redenvelopers.driver.service.RedEnvelopersService;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * 
 * Title: 红包接口功能实现类
 * <p>
 * Description: 
 * </p>
 * 
 * @author 冯高攀<a href="mailto:gaopan.feng@melot.cn">
 * @version V1.0
 * @since 2016年1月15日 上午10:16:30
 */
public class RedEnvelopeFunctions {
    
    private static Logger logger = Logger.getLogger(RedEnvelopeFunctions.class);
    
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
        int userId, roomId;
        try {
            userId = CommonUtil.getJsonParamInt(paramJsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
            roomId = CommonUtil.getJsonParamInt(paramJsonObject, "roomId", 0, TagCodeEnum.ROOMID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        //判断userId==roomId，如果相等，否则amount为0，参数就是roomId,只有房主有权限
        try {
            RedEnvelopersService redEnvelopersService = (RedEnvelopersService) MelotBeanFactory.getBean("redEnvelopersService");
            long amount = 0;
            if (roomId > 0) {
                 amount = redEnvelopersService.getActorCoffersAmount(roomId);
            }
            RedEnvelopersConfigModel redEvelopModel = redEnvelopersService.getRedEnvelopersConfigModel();
            result.addProperty("amount", amount);
            result.addProperty("minCount", redEvelopModel.getMinCount());
            result.addProperty("maxCount", redEvelopModel.getMaxCount());
            result.addProperty("maxMoney", redEvelopModel.getMaxMoney());
            result.addProperty("validTime", redEvelopModel.getValidTime());
            result.addProperty("timelag", redEvelopModel.getTimelag());
            result.addProperty("minRichLevel", redEvelopModel.getMinRichLevel());
            result.addProperty("maxCoffers", redEvelopModel.getMaxCoffers());
            
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch(Exception e) {
            logger.error("RedEnvelopersService execute (" + userId + ", " + roomId + ") return exception.", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
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
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        //解析参数
        int userId, roomId, amount, count, actorCoffers, sendSpeak, appId;
        try {
            //解析参数
            userId = CommonUtil.getJsonParamInt(paramJsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            roomId = CommonUtil.getJsonParamInt(paramJsonObject, "roomId", 0, TagCodeEnum.ROOMID_MISSING, 1, Integer.MAX_VALUE);
            amount = CommonUtil.getJsonParamInt(paramJsonObject, "amount", 0, "31060001", 1, Integer.MAX_VALUE);
            count = CommonUtil.getJsonParamInt(paramJsonObject, "count", 0, "31060002", 1, Integer.MAX_VALUE);
            actorCoffers = CommonUtil.getJsonParamInt(paramJsonObject, "actorCoffers", 0, null, 1, Integer.MAX_VALUE);
            sendSpeak = CommonUtil.getJsonParamInt(paramJsonObject, "sendSpeak", 0, null, 1, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(paramJsonObject, "a", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        // 判断房间是否开播，未开播的房间不能发红包
        RoomInfo roomInfo = RoomService.getRoomInfo(roomId);
        if (!(roomInfo != null && ((roomInfo.getLiveType() != null && roomInfo.getLiveType() >= 1) || roomInfo.isOnLive()))) {
            result.addProperty("TagCode", "31060008");
            return result;
        }
        
        // 判断是否绑定神秘人
        String mysTypeStr = HotDataSource.getHotFieldValue(String.valueOf(userId), "mysType");
        if (StringUtil.parseFromStr(mysTypeStr, 0) == 2) {
            result.addProperty("TagCode", "31060004");
            return result;
        }
        
        // 判断红包总金额是否大于红包个数，如果不是返回错误
        if (amount < count) {
            result.addProperty("TagCode", "31060009");
            return result;
        }
        
        //调用模块方法
        try {
            RedEnvelopersService redEnvelopersService = (RedEnvelopersService) MelotBeanFactory.getBean("redEnvelopersService");
            if (userId != roomId && actorCoffers > 0) {
                result.addProperty("TagCode", "31060003");
                return result;
            }
            
            long money = redEnvelopersService.insertSendRedEnvelopers(userId, roomId, amount, count, actorCoffers, sendSpeak, appId);
            result.addProperty("money", money);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch(MelotModuleException e) {
            int errCode = e.getErrCode();
            switch (errCode) {
            case 101:
                result.addProperty("TagCode" , "31060004");
                break;
            case 102:
                result.addProperty("TagCode" , "31060005");
                break;
            case 103:
                result.addProperty("TagCode" , "31060006");
                break;
            case 104:
                result.addProperty("TagCode" , "31060007");
                break;
            case 105:
                result.addProperty("TagCode" , "31060009");
                break;
            default:
                result.addProperty("TagCode" , TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                break;
            }
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            
            logger.error("RedEnvelopersService.insertSendRedEnvelopers("
                    + userId + ", " + roomId + ", " + amount + ", " + count
                    + ", " + actorCoffers + ", " + sendSpeak
                    + ") execute exception.", e);
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
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
      
        //解析参数
        int userId, roomId, appId, platform;
        String sendId;
        try {
            userId = CommonUtil.getJsonParamInt(paramJsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            roomId = CommonUtil.getJsonParamInt(paramJsonObject, "roomId", 0, TagCodeEnum.ROOMID_MISSING, 1, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(paramJsonObject, "a", AppIdEnum.AMUSEMENT, null, 1, Integer.MAX_VALUE);
            sendId = CommonUtil.getJsonParamString(paramJsonObject, "sendId", "", "31070001", 1, 36);
            platform = CommonUtil.getJsonParamInt(paramJsonObject, "platform", PlatformEnum.WEB, null, PlatformEnum.WEB, PlatformEnum.IPAD);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        String userIp = GeneralService.getIpAddr(request, appId, platform, null);
        Map<String, Object> extraParams = new HashMap<String, Object>();
        extraParams.put("userIp", userIp);
        
        //调用模块
        JsonArray redEvelopArray = new JsonArray();
        try {
            RedEnvelopersService redEnvelopersService = (RedEnvelopersService) MelotBeanFactory.getBean("redEnvelopersService");
            CurrentGetRedEnvelopersModel evelopModel = redEnvelopersService.insertGetRedEnvelopers(userId, roomId, sendId, extraParams);
            
            // 设置抢到的红包金额和用户当前秀币额
            if (evelopModel.getAmount() < 0) {
                switch (evelopModel.getAmount().intValue()) {
                case -102: // 红包已抢完
                    result.addProperty("TagCode" , "31070003");
                    break;
                    
                case -103: // 红包已过期
                    result.addProperty("TagCode" , "31070004");
                    break;
                    
                case -105: // 不能重复领取同一个红包
                    result.addProperty("TagCode" , "31070006");
                    break;

                default:
                    result.addProperty("TagCode" , "31070003");
                    break;
                }
            } else {
                result.addProperty("getAmount", evelopModel.getAmount());
                
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            }
            result.addProperty("money", evelopModel.getShowMoney());
            
            //设置发红包的人信息
            RedEnvelopersInfoModel evelopInfoModel = evelopModel.getRedEnvelopersInfo();
            result.addProperty("sendId", evelopInfoModel.getSendId());
            result.addProperty("userId", evelopInfoModel.getUserId());
            
            UserProfile sendUser = com.melot.kktv.service.UserService.getUserInfoV2(evelopInfoModel.getUserId());
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
                        UserProfile user = com.melot.kktv.service.UserService.getUserInfoV2(redEvelopModel.getUserId());
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
                result.addProperty("TagCode" , "31070002");
                break;
            case 102:
                result.addProperty("TagCode" , "31070003");
                break;
            case 103:
                result.addProperty("TagCode" , "31070004");
                break;
            case 104:
                result.addProperty("minRichLevel", StringUtil.parseFromStr(e.getMessage(), 0));
                result.addProperty("TagCode" , "31070007");
                break;
            case 105:
                result.addProperty("TagCode", "31070006");
                break;
            case 106:
                result.addProperty("TagCode", "31070008");
                break;
            case 107:
                result.addProperty("TagCode", "31070009");
                break;
            default:
                result.addProperty("TagCode" , TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                break;
            }
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            
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
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        JsonArray listArray = new JsonArray();
        
        //调用模块
        try {
            RedEnvelopersService redEnvelopersService = (RedEnvelopersService) MelotBeanFactory.getBean("redEnvelopersService");
            List<RedEnvelopersInfoModel> redInfoModels = redEnvelopersService.getRoomRedEnvelopersList(roomId);
            JsonObject evelopJson = null;
            JsonArray redEvelops;
            List<GetRedEnvelopersModel> list;
            JsonObject redEvelopJson;
            
            if (redInfoModels != null && redInfoModels.size() > 0) {
                for (RedEnvelopersInfoModel redEvelop : redInfoModels) {
                    evelopJson = new JsonObject();
                    evelopJson.addProperty("sendId", redEvelop.getSendId());
                    evelopJson.addProperty("userId", redEvelop.getUserId());
                    
                    UserProfile sendUser = com.melot.kktv.service.UserService.getUserInfoV2(redEvelop.getUserId());
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
                                UserProfile user = com.melot.kktv.service.UserService.getUserInfoV2(redEvelopModel.getUserId());
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
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            
            logger.error("RedEnvelopersService.getRoomRedEnvelopersList("
                    + roomId + ") execute exception.", e);
        }
        
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        result.add("list", listArray);
        
        return result;
    }
}

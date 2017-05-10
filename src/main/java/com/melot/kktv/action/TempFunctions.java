package com.melot.kktv.action;

//import com.melot.sdk.core.util.MelotBeanFactory;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Random;
//
//import javax.servlet.http.HttpServletRequest;
//
//import org.apache.log4j.Logger;
//
//import com.google.gson.Gson;
//import com.google.gson.JsonArray;
//import com.google.gson.JsonObject;
//import com.google.gson.JsonParser;
//import com.melot.api.menu.sdk.dao.RoomSubCatalogDao;
//import com.melot.api.menu.sdk.dao.domain.RoomInfo;
//import com.melot.api.menu.sdk.dao.domain.RoomSubCatalog;
//import com.melot.kkcore.user.api.UserProfile;
//import com.melot.kkcx.service.RoomService;
//import com.melot.kkcx.service.StorehouseService;
//import com.melot.kktv.domain.StorehouseInfo;
//import com.melot.kktv.lottery.ChristmasAt2015;
//import com.melot.kktv.lottery.KkThree;
//import com.melot.kktv.lottery.LovesAt2016;
//import com.melot.kktv.lottery.arithmetic.LotteryArithmetic;
//import com.melot.kktv.lottery.arithmetic.LotteryArithmeticCache;
//import com.melot.kktv.redis.ActivitySource;
//import com.melot.kktv.redis.HotDataSource;
//import com.melot.kktv.service.UserService;
//import com.melot.kktv.util.AppIdEnum;
//import com.melot.kktv.util.CommonUtil;
//import com.melot.kktv.util.ConfigHelper;
//import com.melot.kktv.util.DateUtil;
//import com.melot.kktv.util.StringUtil;
//import com.melot.kktv.util.TagCodeEnum;
//import com.melot.kktv.util.db.DB;
//import com.melot.kktv.util.db.SqlMapClientHelper;
//import com.melot.module.ModuleService;
//import com.melot.module.packagegift.driver.domain.UserVip;
//import com.melot.module.packagegift.driver.service.VipService;
//import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * Title: TempFunctions
 * <p>
 * Description: 临时接口 Function 集合
 * </p>
 * 
 * @author 宋建明<a href="mailto:jianming.song@melot.cn">
 * @version V1.0
 * @since 2015-6-11 下午2:14:32
 */
public class TempFunctions {

//    /** 日志记录对象 */
//    private static Logger logger = Logger.getLogger(TempFunctions.class);
//	
//    /* ----------------------------------临时接口:2015年 靓号&尊号超市推广 活动---------------------------------------- */
//    
//    /**
//     * 2015年 靓号&尊号超市推广 活动（10007010）
//     * @param paramJsonObject
//     * @param checkTag
//     * @return
//     * @throws Exception
//     */
//    public JsonObject luckyIdPopul(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
//        JsonObject result = new JsonObject();
//        if (!checkTag) {
//            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
//            return result;
//        }
//        
//        /*
//         * 2015年 靓号&尊号超市推广 活动： 活动时间范围限制
//         * 2015-06-27: 1435334400
//         */
//        long endTime = DateUtil.parseDateStringToLong("2015-08-08", "yyyy-MM-dd");
//        if (System.currentTimeMillis() >= endTime) {
//            result.addProperty("TagCode", TagCodeEnum.NOT_ACTIVITY_TIME);
//            return result;
//        }
//        
//        int userId = 0;
//        try {
//            userId = CommonUtil.getJsonParamInt(paramJsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
//        } catch (CommonUtil.ErrorGetParameterException e) {
//            result.addProperty("TagCode", e.getErrCode());
//            return result;
//        } catch (Exception e) {
//            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
//            return result;
//        }
//        
//        // 判断是否是 VIP 用户
//        boolean hasValidVip = false;
//        VipService vipService = (VipService) MelotBeanFactory.getBean("vipService");
//        List<UserVip> userVips = vipService.getUserVip(userId);
//        if (userVips != null && userVips.size() > 0) {
//            for (UserVip userVip : userVips) {
//                if (userVip.getLeftTime() == - 1 || userVip.getLeftTime() - System.currentTimeMillis() > 0) {
//                    hasValidVip = true;
//                    break;
//                }
//            }
//        }
//        if (!hasValidVip) {
//            result.addProperty("TagCode", "07100003");
//            return result;
//        }
//        
//        String key = "TempFunctions.luckyIdPopul";
//        if (HotDataSource.hasTempData(key, String.valueOf(userId))) {
//            result.addProperty("TagCode", "07100001");
//            return result;
//        }
//        
//        // addTicket(int userId, int luckyCount, int goldCount, int silverCount, int amount, int presentType, String describe)
//        if(((com.melot.asset.driver.service.AssetService) MelotBeanFactory.getBean("assetService"))
//            .addTicket(userId, 1, 0, 0, 0, 7, "2015年靓号&尊号超市推广活动赠送")) {
//            HotDataSource.setTempData(key, String.valueOf(userId), DateUtil.parseDateStringToLong("2016-01-01", "yyyy-MM-dd") / 1000);
//            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
//        } else {
//            result.addProperty("TagCode", "07100002");
//        }
//        
//        return result;
//    }
//
//    /* ----------------------------------临时接口:2015年 520 活动---------------------------------------- */
//    
//    /* ----------------------------------临时接口:大粽情人活动---------------------------------------- */
//    
//    public JsonObject dumplingActivity(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
//        JsonObject result = new JsonObject();
//        /*  int[] eventCodes = {250,500,1500,2500,9000,15000,25000,40000}; 
//        JsonParser jsonParser = new JsonParser();
//        JsonArray array = new JsonArray();
//        for (int eventCode : eventCodes) {
//           String json =  DragonBoatSource.getRoomSignRankingList(eventCode);
//           if(json == null ){
//               continue;
//           }
//           array.add(jsonParser.parse(json).getAsJsonArray());
//           
//        }
//        
//        String totalCount = DragonBoatSource.getGiftCount();
//        if(totalCount == null){
//            result.addProperty("totalCount", 0);
//        }else{
//            result.addProperty("totalCount", Integer.valueOf(totalCount));
//        }
//        
//        result.add("lottery", array);
//        */
//        result.addProperty("TagCode", TagCodeEnum.FUNCTAG_INVALID_EXCEPTION);
//        return result;
//    }
//    /* ----------------------------------临时接口:大粽情人活动---------------------------------------- */
//    
//    /* ----------------------------------临时接口:唱响CBD 活动---------------------------------------- */
//    
//    /**
//     * 唱响CBD 活动（10007012）
//     * @param paramJsonObject
//     * @param checkTag
//     * @return
//     * @throws Exception
//     */
//    public JsonObject kkcxCBD(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
//        JsonObject result = new JsonObject();
//
//        long startTime, endTime;
//        try {
//            startTime = CommonUtil.getJsonParamLong(paramJsonObject, "startTime", System.currentTimeMillis()/1000, null, 1l, Long.MAX_VALUE);
//            endTime = CommonUtil.getJsonParamLong(paramJsonObject, "endTime", 0, null, 1l, Long.MAX_VALUE);
//        } catch (CommonUtil.ErrorGetParameterException e) {
//            result.addProperty("TagCode", e.getErrCode());
//            return result;
//        } catch (Exception e) {
//            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
//            return result;
//        }
//        
//        String key = "TempFunctions.kkcxCBD";
//        String cacheResult = HotDataSource.getTempDataString(key + "." + startTime + "-" + endTime);
//        if (!StringUtil.strIsNull(cacheResult)) {
//            return new JsonParser().parse(cacheResult).getAsJsonObject();
//        }
//        
//        JsonArray giftList = new JsonArray();
//        
//        Map<String, Date> paraMap = new HashMap<String, Date>();
//        paraMap.put("startTime", new Date(startTime * 1000l));
//        if (endTime > 0) {
//            paraMap.put("endTime", new Date(endTime * 1000l));
//        }
//        @SuppressWarnings("unchecked")
//        List<Map<String, Long>> list = SqlMapClientHelper.getInstance(DB.MASTER).queryForList("Index.getTimeGiftTotal", paraMap);
//        if (list != null && list.size() > 0) {
//            JsonObject jsonObject;
//            Long giftId;
//            String giftName;
//            for (Map<String, Long> map : list) {
//                giftId = map.get("giftId");
//                
//                giftName = null;
//                switch (giftId.intValue()) {
//                case 40000471:
//                    giftName = "朝阳律办";
//                    break;
//
//                case 40000472:
//                    giftName = "传媒商会";
//                    break;
//
//                case 40000473:
//                    giftName = "海归志愿者";
//                    break;
//
//                case 40000474:
//                    giftName = "绝味鸭脖";
//                    break;
//
//                case 40000475:
//                    giftName = "银松传媒";
//                    break;
//
//                case 40000476:
//                    giftName = "优客工厂";
//                    break;
//
//                case 40000477:
//                    giftName = "远行地产";
//                    break;
//
//                case 40000478:
//                    giftName = "中国海盟";
//                    break;
//
//                default:
//                    break;
//                }
//                if (giftName == null) {
//                    continue;
//                }
//                
//                jsonObject = new JsonObject();
//                jsonObject.addProperty("giftId", giftId);
//                jsonObject.addProperty("giftName", giftName);
//                jsonObject.addProperty("giftCount", map.get("giftCount"));
//                jsonObject.addProperty("giftOrder", map.get("giftOrder"));
//                
//                giftList.add(jsonObject);
//            }
//        }
//        
//        result.add("gfitList", giftList);
//        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
//        
//        HotDataSource.setTempDataString(key + "." + startTime + "-" + endTime, result.toString(), 30);
//        return result;
//    }
//
//    /* ----------------------------------临时接口:唱响CBD 活动---------------------------------------- */
//    
//    /**
//     * 获取2015年度盛典主播荣耀榜（10007013）
//     * @param paramJsonObject
//     * @return
//     * @throws Exception
//     */
//    @SuppressWarnings("unchecked")
//    public JsonObject annualActorHonorRank(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
//        JsonObject result = new JsonObject();
//        
//        long startTime, endTime;
//        int freeGift, payGift, rowCount;
//        try {
//            startTime = CommonUtil.getJsonParamLong(paramJsonObject, "startTime", 0, "07130001", 1l, Long.MAX_VALUE);
//            endTime = CommonUtil.getJsonParamLong(paramJsonObject, "endTime", 0, "07130002", 1l, Long.MAX_VALUE);
//            freeGift = CommonUtil.getJsonParamInt(paramJsonObject, "freeGift", 0, "07130003", 1, Integer.MAX_VALUE);
//            payGift = CommonUtil.getJsonParamInt(paramJsonObject, "payGift", 0, "07130004", 1, Integer.MAX_VALUE);
//            rowCount = CommonUtil.getJsonParamInt(paramJsonObject, "rowCount", 0, "07130005", 1, 100);
//        } catch (CommonUtil.ErrorGetParameterException e) {
//            result.addProperty("TagCode", e.getErrCode());
//            return result;
//        } catch (Exception e) {
//            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
//            return result;
//        }
//        
//        String key = "TempFunctions.annualActorHonorRank.Cache";
//        String field = startTime + "-" + endTime + "-" + freeGift + "-" + payGift + "-" + rowCount;
//        String cacheResult = HotDataSource.getHotFieldValue(key, field);
//        if (!StringUtil.strIsNull(cacheResult)) {
//            return new JsonParser().parse(cacheResult).getAsJsonObject();
//        }
//        
//        Map<String, Object> paraMap = new HashMap<String, Object>();
//        paraMap.put("startTime", new Date(startTime));
//        paraMap.put("endTime", new Date(endTime));
//        paraMap.put("freeGift", freeGift);
//        paraMap.put("payGift", payGift);
//        paraMap.put("rowCount", rowCount);
//        
//        JsonArray rankList = new JsonArray();
//        Gson gson = new Gson();
//        try {
//            List<Map<String, Object>> list = SqlMapClientHelper.getInstance(DB.MASTER).queryForList("Index.getActorRank", paraMap);
//            if (list != null && list.size() > 0) {
//                int userId;
//                UserProfile userInfo;
//                for (Map<String, Object> map : list) {
//                    userId = (Integer) map.get("userId");
//                    
//                    userInfo = com.melot.kktv.service.UserService.getUserInfoV2(userId);
//                    
//                    map.put("nickname", userInfo.getNickName());
//                    
//                    if (userInfo.getPortrait() != null) {
//                        map.put("portrait_path_original", ConfigHelper.getHttpdir() + userInfo.getPortrait());
//                        map.put("portrait_path_1280", ConfigHelper.getHttpdir() + userInfo.getPortrait() + "!1280");
//                        map.put("portrait_path_256", ConfigHelper.getHttpdir() + userInfo.getPortrait() + "!256");
//                        map.put("portrait_path_128", ConfigHelper.getHttpdir() + userInfo.getPortrait() + "!128");
//                        map.put("portrait_path_48", ConfigHelper.getHttpdir() + userInfo.getPortrait() + "!48");
//                    }
//                    
//                    rankList.add(gson.toJsonTree(map));
//                }
//            }
//        } catch (Exception e) {
//            logger.error("SqlMapClientHelper.getInstance(DB.MASTER).queryForList(\"Index.getActorRank\", " + gson.toJson(paraMap) + ")", e);
//        }
//        
//        result.add("rankList", rankList);
//        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
//        HotDataSource.setHotFieldValue(key, field, result.toString(), 60);
//        
//        return result;
//    }
//    
//    /**
//     * 世界说投票接口（10007014）
//     * @param paramJsonObject
//     * @return
//     * @throws Exception
//     */
//    public JsonObject kwVote(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
//        JsonObject result = new JsonObject();
//        
//        /*int userId = 0;
//        int playId = 0;
//        int playerId = 0;
//        String token;
//        try{
//            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
//            token = CommonUtil.getJsonParamString(jsonObject, "kwToken", null, TagCodeEnum.TOKEN_INCORRECT, 1, Integer.MAX_VALUE);
//            playId = CommonUtil.getJsonParamInt(jsonObject, "playId", 0, TagCodeEnum.PLAY_ID_MISSING, 1, Integer.MAX_VALUE);
//            playerId = CommonUtil.getJsonParamInt(jsonObject, "playerId", 0, TagCodeEnum.PLAYER_ID_MISSINF, 1, Integer.MAX_VALUE);
//        } catch(CommonUtil.ErrorGetParameterException e) {
//            result.addProperty("TagCode", e.getErrCode());
//            return result;
//        } catch(Exception e) {
//            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
//            return result;
//        }
//        
//        // check kwToken
//        com.melot.kkcore.account.service.AccountService accountService = (com.melot.kkcore.account.service.AccountService) MelotBeanFactory.getBean("kkAccountService");
//        String kwToken = accountService.getUserToken(userId, AppIdEnum.KKWORLD);
//        if (kwToken == null || !kwToken.equals(token)) {
//            result.addProperty("TagCode", TagCodeEnum.TOKEN_INCORRECT);
//            return result;
//        }
//        
//        // call module service interface
//        InterfaceVoteRestInfo voteRestInfo = MsgClientInterfaceActivityAgent.votePlay(userId, playId, playerId);
//        if (voteRestInfo != null) {
//            if (voteRestInfo.getResponseBaseInfo().getTagCode() == MsgClientInterfaceActivityAgent.RESP_CODE_SUCCESS) {
//                result.addProperty("restPlayVotes", voteRestInfo.getRestPlayTickets());
//                if (playerId > 0) {
//                    result.addProperty("restPlayerVotes", voteRestInfo.getRestPlayerTickets());
//                }
//                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
//            } else {
//                if (voteRestInfo.getResponseBaseInfo().getTagCode()
//                        == VotePlayRespCode.RESP_CODE_FAILED_ACTIVITY_NONE) {
//                    // 活动场次不存在
//                    result.addProperty("TagCode", TagCodeEnum.PLAY_NOT_EXIST);
//                } else if(voteRestInfo.getResponseBaseInfo().getTagCode()
//                        == VotePlayRespCode.RESP_CODE_FAILED_NOT_IN_VOTE_TIME) {
//                    // 当前时间无法投票
//                    result.addProperty("TagCode", TagCodeEnum.CANNOT_VOTE_NOW);
//                } else if(voteRestInfo.getResponseBaseInfo().getTagCode()
//                        == VotePlayRespCode.RESP_CODE_FAILED_OVER_PLAY_MAX_VOTES) {
//                    // 已达到一个场次可投票数上限
//                    result.addProperty("TagCode", TagCodeEnum.OVER_PLAY_MAX_VOTES);
//                } else if(voteRestInfo.getResponseBaseInfo().getTagCode()
//                        == VotePlayRespCode.RESP_CODE_FAILED_OVER_PLAYER_MAX_VOTES) {
//                    // 已达到一个选手可被投票数上限
//                    result.addProperty("TagCode", TagCodeEnum.OVER_PLAYER_MAX_VOTES);
//                } else if(voteRestInfo.getResponseBaseInfo().getTagCode()
//                        == VotePlayRespCode.RESP_CODE_FAILED_VOTE_ERROR) {
//                    // 场次投票失败
//                    result.addProperty("TagCode", TagCodeEnum.VOTE_FAILED);
//                } else {
//                    result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
//                }
//            }
//        } else {
//            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
//        }*/
//        result.addProperty("TagCode", TagCodeEnum.FUNCTAG_INVALID_EXCEPTION);
//        
//        return result;
//    }
//    
//    /**
//     * KK三周年庆活动抽奖接口（10007015）
//     * @param paramJsonObject
//     * @param checkTag
//     * @return
//     * @throws Exception
//     */
//    public JsonObject lotteryAtKkThree(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
//        JsonObject result = new JsonObject();
//        if (!checkTag) {
//            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
//            return result;
//        }
//        
//        /*
//         * KK三周年庆活动： 活动时间范围限制 2015-09-08 ~ 2015-09-16
//         */
//        long curTime = System.currentTimeMillis();
//        if (curTime < DateUtil.parseDateStringToLong("2015-09-08", "yyyy-MM-dd") 
//                || curTime >= DateUtil.parseDateStringToLong("2015-09-16", "yyyy-MM-dd")) {
//            result.addProperty("TagCode", TagCodeEnum.NOT_ACTIVITY_TIME);
//            return result;
//        }
//        
//        String lotteryId = "KK_Three_Anniversary";
//        
//        // KK三周年庆活动： 开始时间控制
//        int userId = 0;
//        String giftName = null;
//        try {
//            userId = CommonUtil.getJsonParamInt(paramJsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
//            giftName = CommonUtil.getJsonParamString(paramJsonObject, "giftName", null, null, 1, Integer.MAX_VALUE);
//        } catch (CommonUtil.ErrorGetParameterException e) {
//            result.addProperty("TagCode", e.getErrCode());
//            return result;
//        } catch (Exception e) {
//            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
//            return result;
//        }
//        
//        try {
//            Map<String, Object> retMap = LotteryArithmetic.lottery(lotteryId, userId, null, giftName);
//            if (retMap != null && !retMap.isEmpty() 
//                    && !LotteryArithmeticCache.SERVICE_KEY_LOTTERY_NO_PHONE_ERROR_STR.equals(retMap.get(LotteryArithmeticCache.SERVICE_KEY_giftId))
//                    && !LotteryArithmeticCache.SERVICE_KEY_LOTTERY_QUOTA_ERROR.equals(retMap.get(LotteryArithmeticCache.SERVICE_KEY_giftId))
//                    && !LotteryArithmeticCache.SERVICE_KEY_LOTTERY_REMAIN_ERROR.equals(retMap.get(LotteryArithmeticCache.SERVICE_KEY_giftId))) {
//                String prizeId = (String) retMap.get(LotteryArithmeticCache.SERVICE_KEY_giftId);
//                
//                // KK三周年庆活动： prizeId: 1-iPhone 2-大K宝 3-靓号券
//                if (HotDataSource.incHotFieldValue(KkThree.KKTHREE_REPEAT_COUNT_CACHE, userId + "", 0) >= 1) {
//                    result.addProperty("prizeId", "40000251");
//                    result.addProperty("prizeName", "3个KK生日蛋糕");
//                    result.addProperty("prizeCount", 3);
//                    
//                    HotDataSource.incHotFieldValue(KkThree.KKTHREE_REPEAT_COUNT_CACHE, userId + "", -1);
//                } else {
//                    result.addProperty("prizeId", prizeId);
//                    result.addProperty("prizeName", (String) retMap.get(LotteryArithmeticCache.SERVICE_KEY_giftName));
//                    result.addProperty("prizeCount", (Integer) retMap.get(LotteryArithmeticCache.SERVICE_KEY_giftCount));
//                }
//                
//                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
//            } else {
//                if (retMap != null && !retMap.isEmpty() 
//                    && LotteryArithmeticCache.SERVICE_KEY_LOTTERY_REMAIN_ERROR.equals(retMap.get(LotteryArithmeticCache.SERVICE_KEY_giftId))) {
//                    result.addProperty("TagCode", "07150002");
//                } else {
//                    result.addProperty("TagCode", "07150001");
//                }
//            }
//        } catch (Exception e) {
//            result.addProperty("TagCode", "07150001");
//        }
//        
//        result.add("quota", getGiftCount(userId));
//        
//        return result;
//    }
//    
//    /**
//     * KK三周年庆活动领取红色糖果接口（10007016）
//     * @param paramJsonObject
//     * @param checkTag
//     * @return
//     * @throws Exception
//     */
//    public JsonObject addRedGiftAtKkThree(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
//        JsonObject result = new JsonObject();
//        if (!checkTag) {
//            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
//            return result;
//        }
//        
//        /*
//         * KK三周年庆活动： 活动时间范围限制 2015-09-08 ~ 2015-09-16
//         */
//        long curTime = System.currentTimeMillis();
//        if (curTime < DateUtil.parseDateStringToLong("2015-09-08", "yyyy-MM-dd") 
//                || curTime >= DateUtil.parseDateStringToLong("2015-09-16", "yyyy-MM-dd")) {
//            result.addProperty("TagCode", TagCodeEnum.NOT_ACTIVITY_TIME);
//            return result;
//        }
//        
//        String key = "KK_Three_Anniversary.addRedGiftAtKkThree." + DateUtil.formatDate(new Date(), "yyyy-MM-dd");
//        long expireTime = DateUtil.parseDateStringToLong("2015-10-01", "yyyy-MM-dd") / 1000;
//        
//        // KK三周年庆活动： 开始时间控制
//        int userId = 0;
//        try {
//            userId = CommonUtil.getJsonParamInt(paramJsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
//        } catch (CommonUtil.ErrorGetParameterException e) {
//            result.addProperty("TagCode", e.getErrCode());
//            return result;
//        } catch (Exception e) {
//            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
//            return result;
//        }
//        
//        if (HotDataSource.hasTempData(key, String.valueOf(userId))) {
//            result.addProperty("TagCode", "07160001");
//        } else {
//            // KK三周年庆活动 红色糖果ID
//            final int redGiftId = 40000511;
//            if (StorehouseService.addUserGift(userId, redGiftId, 1, "红色糖果", 7, "KK三周年庆典(领取)")) {
//                HotDataSource.setTempData(key, String.valueOf(userId), expireTime);
//                
//                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
//            } else {
//                result.addProperty("TagCode", "07160002");
//            }
//        }
//        
//        result.add("quota", getGiftCount(userId));
//        
//        return result;
//    }
//    
//    private static JsonObject getGiftCount(int userId) {
//        // KK三周年庆典 三种糖果礼物ID
//        final int redGiftId = 40000511;
//        final int violetGiftId = 40000513;
//        final int pinkGiftId = 40000512;
//        
//        int redGiftCount = 0, violetGiftCount = 0, pinkGiftCount = 0;
//        
//        List<StorehouseInfo> list = StorehouseService.getUserGiftCount(userId, redGiftId + "," + violetGiftId + "," + pinkGiftId);
//        if (list != null && list.size() > 0) {
//            for (StorehouseInfo storehouseInfo : list) {
//                switch (storehouseInfo.getGiftId().intValue()) {
//                case redGiftId:
//                    redGiftCount = storehouseInfo.getQuantity();
//                    break;
//                    
//                case violetGiftId:
//                    violetGiftCount = storehouseInfo.getQuantity();
//                    break;
//                    
//                case pinkGiftId:
//                    pinkGiftCount = storehouseInfo.getQuantity();
//                    break;
//                    
//                default:
//                    break;
//                }
//            }
//        }
//        
//        JsonObject jsonObject = new JsonObject();
//        jsonObject.addProperty("redGiftCount", redGiftCount);
//        jsonObject.addProperty("violetGiftCount", violetGiftCount);
//        jsonObject.addProperty("pinkGiftCount", pinkGiftCount);
//        
//        return jsonObject;
//    }
//    
//    /**
//     * 2015 中秋节免费领取月饼两盘接口（10007017）
//     * @param paramJsonObject
//     * @param checkTag
//     * @return
//     * @throws Exception
//     */
//    public JsonObject getMooncakeAtMidautumn(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
//        JsonObject result = new JsonObject();
//        if (!checkTag) {
//            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
//            return result;
//        }
//        
//        /*
//         * 2015 中秋节活动： 活动时间范围限制 2015-09-25 ~ 2015-10-02
//         */
//        long curTime = System.currentTimeMillis();
//        if (curTime < DateUtil.parseDateStringToLong("2015-09-25", "yyyy-MM-dd") 
//                || curTime >= DateUtil.parseDateStringToLong("2015-10-02", "yyyy-MM-dd")) {
//            result.addProperty("TagCode", TagCodeEnum.NOT_ACTIVITY_TIME);
//            return result;
//        }
//        
//        // KK三周年庆活动： 开始时间控制
//        int userId = 0;
//        try {
//            userId = CommonUtil.getJsonParamInt(paramJsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
//        } catch (CommonUtil.ErrorGetParameterException e) {
//            result.addProperty("TagCode", e.getErrCode());
//            return result;
//        } catch (Exception e) {
//            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
//            return result;
//        }
//        
//        // 判断用户是否已经领取过
//        String key = "TempFunctions.getMooncakeAtMidautumn";
//        if (HotDataSource.hasTempData(key, String.valueOf(userId))) {
//            result.addProperty("TagCode", "07170001");
//            return result;
//        }
//        
//        // 判断用户是否绑定手机号码
//        if (StringUtil.strIsNull(UserService.getPhoneNumberOfUser(userId))) {
//            result.addProperty("TagCode", "07170002");
//            return result;
//        }
//        
//        final int giftId = 10000009;
//        long expireTime = DateUtil.parseDateStringToLong("2015-10-05", "yyyy-MM-dd") / 1000;
//        if (StorehouseService.addUserGift(userId, giftId, 2, "月饼", 7, "2015 中秋节免费领取月饼两盘")) {
//            HotDataSource.setTempData(key, String.valueOf(userId), expireTime);
//            
//            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
//        } else {
//            result.addProperty("TagCode", "07170003");
//        }
//        
//        return result;
//    }
//    
//    /**
//     * 男人装获取电子杂志激活码接口（10007018）
//     * @param paramJsonObject
//     * @param checkTag
//     * @return
//     * @throws Exception
//     */
//    public JsonObject getUserEncode(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
//        JsonObject result = new JsonObject();
//        if (!checkTag) {
//            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
//            return result;
//        }
//        
//        /*
//         * 男人装活动： 活动时间范围限制 2015-11-10 ~ 
//         */
//        long curTime = System.currentTimeMillis();
//        if (curTime < DateUtil.parseDateStringToLong("2015-11-10", "yyyy-MM-dd")) {
//            result.addProperty("TagCode", TagCodeEnum.NOT_ACTIVITY_TIME);
//            return result;
//        }
//        
//        int userId = 0;
//        try {
//            userId = CommonUtil.getJsonParamInt(paramJsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
//        } catch (CommonUtil.ErrorGetParameterException e) {
//            result.addProperty("TagCode", e.getErrCode());
//            return result;
//        } catch (Exception e) {
//            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
//            return result;
//        }
//        
//        Map<String, Object> paramMap = new HashMap<String, Object>();
//        paramMap.put("userId", userId);
//        try {
//            SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Other.getUserEncode", paramMap);
//            String encode = (String) paramMap.get("encode");
//            if (encode == null || "04".equals(encode)) {
//                result.addProperty("TagCode", "07180004");
//            } else if ("01".equals(encode)) {
//                result.addProperty("TagCode", "07180001");
//            } else if ("02".equals(encode)) {
//                result.addProperty("TagCode", "07180002");
//            } else if ("03".equals(encode)) {
//                result.addProperty("TagCode", "07180003");
//            } else {
//                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
//                result.addProperty("encode", encode);
//            }
//        } catch (Exception e) {
//            result.addProperty("TagCode", "07180004");
//        }
//        
//        return result;
//    }
//    
//    /**
//     * 男人装获取电子杂志激活码接口（10007019）
//     * @param paramJsonObject
//     * @param checkTag
//     * @return
//     * @throws Exception
//     */
//    public JsonObject getUserHadEncode(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
//        JsonObject result = new JsonObject();
//        if (!checkTag) {
//            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
//            return result;
//        }
//        
//        int userId = 0;
//        try {
//            userId = CommonUtil.getJsonParamInt(paramJsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
//        } catch (CommonUtil.ErrorGetParameterException e) {
//            result.addProperty("TagCode", e.getErrCode());
//            return result;
//        } catch (Exception e) {
//            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
//            return result;
//        }
//        
//        try {
//            String encode = (String) SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Other.getUserHadEncode", userId);
//            if (StringUtil.strIsNull(encode)) {
//                result.addProperty("TagCode", "07190001");
//            } else {
//                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
//                result.addProperty("encode", encode);
//            }
//        } catch (Exception e) {
//            result.addProperty("TagCode", "07190001");
//        }
//        
//        return result;
//    }
//    
//    private static Map<String, Integer> maxCountMap = new HashMap<String, Integer>();
//    static {
//        maxCountMap.put("10_0", 1050);
//        maxCountMap.put("10_12", 700);
//        maxCountMap.put("10_15", 1050);
//        maxCountMap.put("10_18", 1400);
//        maxCountMap.put("10_20", 1750);
//        maxCountMap.put("10_22", 1400);
//
//        maxCountMap.put("21_0", 300);
//        maxCountMap.put("21_12", 200);
//        maxCountMap.put("21_15", 300);
//        maxCountMap.put("21_18", 400);
//        maxCountMap.put("21_20", 500);
//        maxCountMap.put("21_22", 400);
//
//        maxCountMap.put("22_0", 150);
//        maxCountMap.put("22_12", 100);
//        maxCountMap.put("22_15", 150);
//        maxCountMap.put("22_18", 200);
//        maxCountMap.put("22_20", 250);
//        maxCountMap.put("22_22", 200);
//    }
//    /**
//     * 2015年度盛典巅峰赛用户抽奖接口（10007020）
//     * @param paramJsonObject
//     * @param checkTag
//     * @return
//     * @throws Exception
//     */
//    public JsonObject lotteryAtFinals(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
//        JsonObject result = new JsonObject();
//        if (!checkTag) {
//            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
//            return result;
//        }
//        
//		/*
//		 * 2015年度盛典巅峰赛用户抽奖：活动时间范围限制 2015-12-8 ~ 2015-12-24
//		 */
//		long curTime = System.currentTimeMillis();
//		if (curTime < DateUtil.parseDateStringToLong("2015-12-8", "yyyy-MM-dd")
//				|| curTime >= DateUtil.parseDateStringToLong("2015-12-24", "yyyy-MM-dd")) {
//			result.addProperty("TagCode", TagCodeEnum.NOT_ACTIVITY_TIME);
//			return result;
//		}
//        
//        int userId = 0;
//        try {
//            userId = CommonUtil.getJsonParamInt(paramJsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
//        } catch (CommonUtil.ErrorGetParameterException e) {
//            result.addProperty("TagCode", e.getErrCode());
//            return result;
//        } catch (Exception e) {
//            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
//            return result;
//        }
//        
//        long currentTime = System.currentTimeMillis();
//        String todayStr = DateUtil.formatDate(new Date(currentTime), "yyyy-MM-dd");
//        
//        // 判断用户今天是否已经抽过奖了
//        String key_alreadyLottery = "lotteryAtFinals.alreadyLottery." + todayStr;
//        if (HotDataSource.hasTempData(key_alreadyLottery, String.valueOf(userId))) {
//            result.addProperty("TagCode", "07200001");
//            return result;
//        }
//        
//        // 判断用户今天是否赠送过 20 张付费家族票 40000508
//        boolean canLottery = false;
//        try {
//            long giftCount = (Long) SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Resource.selectUserSendFamilyTicketCount", userId);
//            if (giftCount >= 20) {
//                canLottery = true;
//            }
//        } catch (Exception e) {
//            logger.error("Resource.selectUserSendFamilyTicketCount(" + userId + ") execute exception", e);
//        }
//        if (!canLottery) {
//            result.addProperty("TagCode", "07200002");
//            return result;
//        }
//        
//        // 获取用的财富等级
//        int richLevel = com.melot.kkcx.service.UserService.getRichLevel(userId);
//        
//        int richLottery, lotteryCount;
//        if (richLevel < 11) {
//            richLottery = 10;
//            lotteryCount = new Random().nextInt(1) + 1;
//        } else if (richLevel < 22) {
//            richLottery = 21;
//            lotteryCount = new Random().nextInt(1) + 3;
//        } else {
//            richLottery = 22;
//            lotteryCount = new Random().nextInt(2) + 5;
//        }
//        
//        int hour = DateUtil.getFieldOfDate(new Date(currentTime), Calendar.HOUR_OF_DAY);
//        int hourLottery;
//        if (hour < 12) {
//            hourLottery = 0;
//        } else if (hour < 15) {
//            hourLottery = 12;
//        } else if (hour < 18) {
//            hourLottery = 15;
//        } else if (hour < 20) {
//            hourLottery = 18;
//        } else if (hour < 22) {
//            hourLottery = 20;
//        } else {
//            hourLottery = 22;
//        }
//        
//        // 判断当前时段家族票是否被抽完了
//        String key_alreadyLotteryCount = "lotteryAtFinals.alreadyLotteryCount." + todayStr;
//        int maxCount = maxCountMap.get(richLottery + "_" + hourLottery);
//        if (HotDataSource.incHotFieldValue(key_alreadyLotteryCount, richLottery + "_" + hourLottery, lotteryCount) >= maxCount) {
//            HotDataSource.incHotFieldValue(key_alreadyLotteryCount, richLottery + "_" + hourLottery, 0 - lotteryCount);
//            result.addProperty("TagCode", "07200003");
//            return result;
//        }
//        
//        if (!StorehouseService.addUserGift(userId, 40000508, lotteryCount, "", 7, "2015年度盛典巅峰赛")) {
//            HotDataSource.incHotFieldValue(key_alreadyLotteryCount, richLottery + "_" + hourLottery, 0 - lotteryCount);
//            result.addProperty("TagCode", "07200003");
//            return result;
//        }
//        
//        HotDataSource.setTempData(key_alreadyLottery, String.valueOf(userId), DateUtil.parseDateStringToLong("2016-01-01", "yyyy-MM-dd") / 1000);
//        
//        result.addProperty("lotteryCount", lotteryCount);
//        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
//        
//        return result;
//    }
//    
//    /*
//     * 晋级的 24 名家族
//     */
//    private static List<String> familyList = new ArrayList<String>();
//    static {
//        familyList.addAll(Arrays.asList(("10783,11101,10955,10471,10060"
//                + "," + "11073,10040,10981,11010,11013"
//                + "," + "11199,10009,11155,10520"
//                + "," + "10004,10591,10165"
//                + "," + "11111,10992"
//                + "," + "10360"
//                + "," + "11085,11525,11585,10605").split(",")));
//    }
//    /**
//     * 2015年度盛典巅峰赛单项报名接口（10007021）
//     * @param paramJsonObject
//     * @param checkTag
//     * @return
//     * @throws Exception
//     */
//    public JsonObject applyFinalsTeam(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
//        JsonObject result = new JsonObject();
//        if (!checkTag) {
//            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
//            return result;
//        }
//        
//		/*
//		 * 2015年度盛典巅峰赛主播报名：活动时间范围限制 2015-12-08 ~ 2015-12-19
//		 */
//		long curTime = System.currentTimeMillis();
//		if (curTime < DateUtil.parseDateStringToLong("2015-12-8", "yyyy-MM-dd")
//				|| curTime >= DateUtil.parseDateStringToLong("2015-12-19", "yyyy-MM-dd")) {
//			result.addProperty("TagCode", TagCodeEnum.NOT_ACTIVITY_TIME);
//			return result;
//		}
//        
//        /*
//         * teamId: 1 - KK神曲王， 2 - KK劲爆王， 3 - KK搞笑王， 4 - KK舞王， 5 - KK淘气王
//         */
//        int userId = 0, teamId;
//        try {
//            userId = CommonUtil.getJsonParamInt(paramJsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
//            teamId = CommonUtil.getJsonParamInt(paramJsonObject, "teamId", 0, "07210001", 1, 5);
//        } catch (CommonUtil.ErrorGetParameterException e) {
//            result.addProperty("TagCode", e.getErrCode());
//            return result;
//        } catch (Exception e) {
//            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
//            return result;
//        }
//        
//        // 判断是否为主播
//        RoomInfo roomInfo = RoomService.getRoomInfo(userId);
//        if (roomInfo == null || roomInfo.getActorId() == null || roomInfo.getActorId() < 1) {
//            result.addProperty("TagCode", "07210002");
//            return result;
//        }
//        
//        // 判断有没有报过名
//        if (HotDataSource.hasTempData("TempFunctions.applyFinalsTeam", userId + "")) {
//            result.addProperty("TagCode", "07210003");
//            return result;
//        }
//        
//        /*
//         * 非晋级的 24 名家族的主播不能报名 “1 - KK神曲王， 2 - KK劲爆王， 3 - KK搞笑王， 4 - KK舞王”
//         * 
//         * 24 名家族内的主播要是想报名 “1 - KK神曲王， 2 - KK劲爆王， 3 - KK搞笑王， 4 - KK舞王” 其中一项，必须本身属于对应的栏目
//         * 
//         * 平台所有主播可以报名 “5 - KK淘气王”
//         */
//        boolean checkRoomSub = false;
//        if (teamId != 5) {
//            // 判断该主播是否属于晋级的24名家族
//            if (!(roomInfo.getFamilyId() != null && familyList.contains(String.valueOf(roomInfo.getFamilyId())))) {
//                result.addProperty("TagCode", "07210004");
//                return result;
//            }
//            
//            // 获取该主播所在的栏目
//            RoomSubCatalogDao roomSubCatalogDao = MelotBeanFactory.getBean("roomSubCatalogDao", RoomSubCatalogDao.class);
//            if (roomSubCatalogDao != null) {
//                List<RoomSubCatalog> list = roomSubCatalogDao.getRoomSubCatalogByRoomId(userId);
//                if (list != null && list.size() > 0) {
//                    for (RoomSubCatalog roomSubCatalog : list) {
//                        if (teamId == 1 && roomSubCatalog.getSubcatlgId() == 31) {
//                            checkRoomSub = true;
//                            break;
//                        } else if (teamId == 2 && roomSubCatalog.getSubcatlgId() == 22) {
//                            checkRoomSub = true;
//                            break;
//                        } else if (teamId == 3 && roomSubCatalog.getSubcatlgId() == 156) {
//                            checkRoomSub = true;
//                            break;
//                        } else if (teamId == 4 && roomSubCatalog.getSubcatlgId() == 120) {
//                            checkRoomSub = true;
//                            break;
//                        }
//                    }
//                }
//            }
//            
//            if (!checkRoomSub) {
//                result.addProperty("TagCode", "07210005");
//                return result;
//            }
//        }
//        
//        int playId;
//        switch (teamId) {
//        case 1:
//            playId = 1275;
//            break;
//            
//        case 2:
//            playId = 1276;
//            break;
//            
//        case 3:
//            playId = 1277;
//            break;
//            
//        case 4:
//            playId = 1278;
//            break;
//
//        default:
//            playId = 1279;
//            break;
//        }
//        
//        try {
//            Map<String, Object> map = new HashMap<String, Object>();
//            map.put("playId", playId);
//            map.put("userId", userId);
//            Integer apuId = (Integer) SqlMapClientHelper.getInstance(DB.MASTER).insert("Other.insertPlayUser", map);
//            if (!(apuId > 0)) {
//                result.addProperty("TagCode", "07210006");
//            } else {
//                ActivitySource.addPlayPlayerCache(playId, userId + "");
//                
//                HotDataSource.setTempData("TempFunctions.applyFinalsTeam", userId + "", DateUtil.parseDateStringToLong("2016-01-01", "yyyy-MM-dd"));
//                
//                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
//            }
//        } catch (Exception e) {
//            result.addProperty("TagCode", "07210006");
//        }
//        
//        return result;
//    }
//    
//    /**
//     * 2015年双12抽奖接口（10007022）
//     * @param paramJsonObject
//     * @param checkTag
//     * @return
//     * @throws Exception
//     */
//    public JsonObject lotteryAt12(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
//        JsonObject result = new JsonObject();
//        if (!checkTag) {
//            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
//            return result;
//        }
//        
//		/*
//		 * 2015年双12抽奖：活动时间范围限制 2015-12-12 ~ 2015-12-15
//		 */
//		long curTime = System.currentTimeMillis();
//		if (curTime < DateUtil.parseDateStringToLong("2015-12-12", "yyyy-MM-dd")
//				|| curTime >= DateUtil.parseDateStringToLong("2015-12-15", "yyyy-MM-dd")) {
//			result.addProperty("TagCode", TagCodeEnum.NOT_ACTIVITY_TIME);
//			return result;
//		}
//        
//        int userId = 0;
//        try {
//            userId = CommonUtil.getJsonParamInt(paramJsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
//        } catch (CommonUtil.ErrorGetParameterException e) {
//            result.addProperty("TagCode", e.getErrCode());
//            return result;
//        } catch (Exception e) {
//            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
//            return result;
//        }
//        
//        long currentTime = System.currentTimeMillis();
//        String todayStr = DateUtil.formatDate(new Date(currentTime), "yyyy-MM-dd");
//        
//        // 判断用户今天是否已经抽过奖了
//        String key_alreadyLottery = "lotteryAt12.alreadyLottery." + todayStr;
//        if (HotDataSource.hasTempData(key_alreadyLottery, String.valueOf(userId))) {
//            result.addProperty("TagCode", "07220001");
//            return result;
//        }
//        
//        // 判断用户是否送出礼物价值达到12,120秀币及以上
//        long giftAmount = 0;
//        Map<String, Object> paraMap = new HashMap<String, Object>();
//        try {
//            paraMap.put("userId", userId);
//            paraMap.put("startTime", new Date(DateUtil.getDayBeginTime(System.currentTimeMillis())));
//            paraMap.put("endTime", DateUtil.getNextDay(new Date(DateUtil.getDayBeginTime(System.currentTimeMillis()))));
//            
//            giftAmount = (Long) SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Temp.getUserSendGiftAmount", paraMap);
//        } catch (Exception e) {
//            logger.error("Temp.getUserSendGiftAmount(" + new Gson().toJson(paraMap) + ") execute exception", e);
//        }
//        if (giftAmount < 12120) {
//            result.addProperty("TagCode", "07220002");
//            return result;
//        }
//        
//        final int giftId_pay = 40000508;
//        final int giftId_free = 40000509;
//        
//        final String giftName_pay = "家族付费票";
//        final String giftName_free = "家族免费票";
//        
//        int lotteryCount, giftId;
//        String giftName;
//        if (giftAmount <= 50000) {
//            lotteryCount = 1;
//            giftId = giftId_free;
//            giftName = giftName_free;
//        } else if (giftAmount <= 150000) {
//            lotteryCount = new Random().nextInt(10);
//            if (lotteryCount > 7) {
//                lotteryCount = 1;
//                giftId = giftId_pay;
//                giftName = giftName_pay;
//            } else {
//                lotteryCount = 2;
//                giftId = giftId_free;
//                giftName = giftName_free;
//            }
//        } else {
//            lotteryCount = new Random().nextInt(10);
//            if (lotteryCount > 5) {
//                lotteryCount = 2;
//                giftId = giftId_pay;
//                giftName = giftName_pay;
//            } else {
//                lotteryCount = 5;
//                giftId = giftId_free;
//                giftName = giftName_free;
//            }
//        }
//        
//        if (!StorehouseService.addUserGift(userId, giftId, lotteryCount, "", 7, "2015年双12")) {
//            result.addProperty("TagCode", "07220003");
//            return result;
//        }
//        
//        HotDataSource.setTempData(key_alreadyLottery, String.valueOf(userId), DateUtil.parseDateStringToLong("2016-01-01", "yyyy-MM-dd") / 1000);
//        
//        result.addProperty("lotteryCount", lotteryCount);
//        result.addProperty("giftId", giftId);
//        result.addProperty("giftName", giftName);
//        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
//        
//        return result;
//    }
//    
//    /**
//     * 2015年圣诞节登录领奖接口（10007023）
//     * @param paramJsonObject
//     * @param checkTag
//     * @return
//     * @throws Exception
//     */
//    public JsonObject getGiftAt2015Christmas(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
//        JsonObject result = new JsonObject();
//        if (!checkTag) {
//            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
//            return result;
//        }
//        
//		/*
//		 * 2015年圣诞节登录领奖：活动时间范围限制 2015-12-24 ~ 2015-12-29
//		 */
//		long curTime = System.currentTimeMillis();
//		if (curTime < DateUtil.parseDateStringToLong("2015-12-24", "yyyy-MM-dd")
//				|| curTime >= DateUtil.parseDateStringToLong("2015-12-30", "yyyy-MM-dd")) {
//			result.addProperty("TagCode", TagCodeEnum.NOT_ACTIVITY_TIME);
//			return result;
//		}
//        
//        int userId, a;
//        try {
//            userId = CommonUtil.getJsonParamInt(paramJsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
//            a = CommonUtil.getJsonParamInt(paramJsonObject, "a", 0, TagCodeEnum.APPID_MISSING, 1, 2);
//        } catch (CommonUtil.ErrorGetParameterException e) {
//            result.addProperty("TagCode", e.getErrCode());
//            return result;
//        } catch (Exception e) {
//            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
//            return result;
//        }
//        
//        // 判断用户今天是否已经领过奖了
//        String key_alreadyGet = "getGiftAt2015Christmas.alreadyGet";
//        if (HotDataSource.hasTempData(key_alreadyGet, String.valueOf(userId))) {
//            result.addProperty("TagCode", "07230001");
//            return result;
//        }
//        
//        // 2015年圣诞节登录领取苹果的礼物ID
//        final int gift_apple_kkcx = 40000589;
//        final int gift_apple_kkgame = 40000585;
//        
//        // 判断用户是唱响操作还是直播操作，并分别给库存
//        boolean successFlag = false;
//        if (a == AppIdEnum.GAME) {
//            com.melot.storehouse.service.StorehouseService storehouseService = (com.melot.storehouse.service.StorehouseService) ModuleService.getService("StorehouseService");
//            if (storehouseService != null) {
//                com.melot.storehouse.domain.RespMsg respMsg = storehouseService.addUserStorehouse(gift_apple_kkgame, userId, 2, 7, "2015年圣诞节直播登录领奖");
//                if (respMsg != null && respMsg.getRespCode() == 0) {
//                    successFlag = true;
//                    result.addProperty("prizeId", gift_apple_kkgame);
//                }
//            }
//        } else if (a == AppIdEnum.AMUSEMENT) {
//            if (StorehouseService.addUserGift(userId, gift_apple_kkcx, 2, "", 7, "2015年圣诞节唱响登录领奖")) {
//                successFlag = true;
//                result.addProperty("prizeId", gift_apple_kkcx);
//            }
//        }
//        
//        if (successFlag) {
//            result.addProperty("prizeName", "苹果2个");
//            result.addProperty("prizeCount", 2);
//            
//            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
//            HotDataSource.setTempData(key_alreadyGet, String.valueOf(userId), DateUtil.parseDateStringToLong("2016-02-01", "yyyy-MM-dd") / 1000);
//        } else {
//            result.addProperty("TagCode", "07230002");
//        }
//        
//        return result;
//    }
//    
//    /**
//     * 2015年圣诞节抽奖接口（10007024）
//     * @param paramJsonObject
//     * @param checkTag
//     * @return
//     * @throws Exception
//     */
//    public JsonObject lotteryAt2015Christmas(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
//        JsonObject result = new JsonObject();
//        if (!checkTag) {
//            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
//            return result;
//        }
//        
//		/*
//		 * 2015年圣诞节登录领奖：活动时间范围限制 2015-12-24 ~ 2015-12-29
//		 */
//		long curTime = System.currentTimeMillis();
//		if (curTime < DateUtil.parseDateStringToLong("2015-12-24", "yyyy-MM-dd")
//				|| curTime >= DateUtil.parseDateStringToLong("2015-12-30", "yyyy-MM-dd")) {
//			result.addProperty("TagCode", TagCodeEnum.NOT_ACTIVITY_TIME);
//			return result;
//		}
//        
//        int userId, a;
//        String prizeId;
//        try {
//            userId = CommonUtil.getJsonParamInt(paramJsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
//            a = CommonUtil.getJsonParamInt(paramJsonObject, "a", 0, TagCodeEnum.APPID_MISSING, 1, 2);
//            
//            prizeId = CommonUtil.getJsonParamString(paramJsonObject, "prizeId", null, null, 0, 30);
//        } catch (CommonUtil.ErrorGetParameterException e) {
//            result.addProperty("TagCode", e.getErrCode());
//            return result;
//        } catch (Exception e) {
//            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
//            return result;
//        }
//        
//        // 判断用户是否送出礼物价值达到50,000秀币及以上
//        long giftAmount = 0;
//        Map<String, Object> paraMap = new HashMap<String, Object>();
//        try {
//            paraMap.put("userId", userId);
//            paraMap.put("startTime", new Date(DateUtil.getDayBeginTime(System.currentTimeMillis())));
//            paraMap.put("endTime", DateUtil.getNextDay(new Date(DateUtil.getDayBeginTime(System.currentTimeMillis()))));
//            
//            giftAmount = (Long) SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Temp.getUserSendGiftAmount", paraMap);
//        } catch (Exception e) {
//            logger.error("Temp.getUserSendGiftAmount(" + new Gson().toJson(paraMap) + ") execute exception", e);
//        }
//        if (giftAmount < 50000) {
//            result.addProperty("TagCode", "07240001");
//            return result;
//        }
//        
//        String awardRulesId = "lotteryAt2015Christmas";
//        
//        // 刷新抽奖缓存
//        LotteryArithmetic.refreshUserTotalCanLottery(awardRulesId, userId, null, giftAmount, false);
//        
//        // 计算用户
//        if (LotteryArithmeticCache.calUserRemain(awardRulesId, userId, null, 0) >= 50000) {
//            Map<String, Object> retMap = LotteryArithmetic.lottery(awardRulesId, userId, a + "", prizeId);
//            if (retMap != null && !retMap.isEmpty() 
//                    && !LotteryArithmeticCache.SERVICE_KEY_LOTTERY_NO_PHONE_ERROR_STR.equals(retMap.get(LotteryArithmeticCache.SERVICE_KEY_giftId))
//                    && !LotteryArithmeticCache.SERVICE_KEY_LOTTERY_QUOTA_ERROR.equals(retMap.get(LotteryArithmeticCache.SERVICE_KEY_giftId))
//                    && !LotteryArithmeticCache.SERVICE_KEY_LOTTERY_REMAIN_ERROR.equals(retMap.get(LotteryArithmeticCache.SERVICE_KEY_giftId))) {
//                prizeId = (String) retMap.get(LotteryArithmeticCache.SERVICE_KEY_giftId);
//                
//                if (HotDataSource.incHotFieldValue(ChristmasAt2015.CHRISTMAS_GAME_REPLACE_COUNT, userId + "", 0) >= 1) {
//                    if ("6".equals(prizeId)) {
//                        if (a == 2) {
//                            result.addProperty("prizeId", "3");
//                            result.addProperty("prizeName", "圣诞铃铛5个");
//                            result.addProperty("prizeCount", 5);
//                        } else {
//                            result.addProperty("prizeId", "2");
//                            result.addProperty("prizeName", "苹果5个");
//                            result.addProperty("prizeCount", 5);
//                        }
//                    } else {
//                        result.addProperty("prizeId", "2");
//                        result.addProperty("prizeName", "苹果5个");
//                        result.addProperty("prizeCount", 5);
//                    }
//                    
//                    HotDataSource.incHotFieldValue(ChristmasAt2015.CHRISTMAS_GAME_REPLACE_COUNT, userId + "", -1);
//                } else {
//                    result.addProperty("prizeId", prizeId);
//                    result.addProperty("prizeName", (String) retMap.get(LotteryArithmeticCache.SERVICE_KEY_giftName));
//                    result.addProperty("prizeCount", (Integer) retMap.get(LotteryArithmeticCache.SERVICE_KEY_giftCount));
//                }
//                
//                result.addProperty("quota", LotteryArithmeticCache.calUserRemain(awardRulesId, userId, null, 50000 - 1) / 50000);
//                
//                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
//                
//            } else {
//                result.addProperty("TagCode", "07240001");
//            }
//        } else {
//            result.addProperty("TagCode", "07240001");
//        }
//        
//        return result;
//    }
//    
//    /**
//     * 2015年圣诞节获取剩余抽奖次数接口（10007025）
//     * @param paramJsonObject
//     * @param checkTag
//     * @return
//     * @throws Exception
//     */
//    public JsonObject getLotteryQuotaAt2015Christmas(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
//        JsonObject result = new JsonObject();
//        if (!checkTag) {
//            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
//            return result;
//        }
//        
//		/*
//		 * 2015年圣诞节登录领奖：活动时间范围限制 2015-12-24 ~ 2015-12-29
//		 */
//		long curTime = System.currentTimeMillis();
//		if (curTime < DateUtil.parseDateStringToLong("2015-12-24", "yyyy-MM-dd")
//				|| curTime >= DateUtil.parseDateStringToLong("2015-12-30", "yyyy-MM-dd")) {
//			result.addProperty("TagCode", TagCodeEnum.NOT_ACTIVITY_TIME);
//			return result;
//		}
//        
//        int userId;
//        try {
//            userId = CommonUtil.getJsonParamInt(paramJsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
//        } catch (CommonUtil.ErrorGetParameterException e) {
//            result.addProperty("TagCode", e.getErrCode());
//            return result;
//        } catch (Exception e) {
//            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
//            return result;
//        }
//        
//        int quota = 0;
//        
//        // 判断用户是否送出礼物价值达到50,000秀币及以上
//        long giftAmount = 0;
//        Map<String, Object> paraMap = new HashMap<String, Object>();
//        try {
//            paraMap.put("userId", userId);
//            paraMap.put("startTime", new Date(DateUtil.getDayBeginTime(System.currentTimeMillis())));
//            paraMap.put("endTime", DateUtil.getNextDay(new Date(DateUtil.getDayBeginTime(System.currentTimeMillis()))));
//            
//            giftAmount = (Long) SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Temp.getUserSendGiftAmount", paraMap);
//        } catch (Exception e) {
//            logger.error("Temp.getUserSendGiftAmount(" + new Gson().toJson(paraMap) + ") execute exception", e);
//        }
//        if (giftAmount >= 50000) {
//            String awardRulesId = "lotteryAt2015Christmas";
//            
//            // 刷新抽奖缓存
//            LotteryArithmetic.refreshUserTotalCanLottery(awardRulesId, userId, null, giftAmount, false);
//            
//            // 计算用户剩余抽奖次数
//            quota = (int) (LotteryArithmeticCache.calUserRemain(awardRulesId, userId, null, 0) / 50000);
//        }
//
//        result.addProperty("quota", quota);
//        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
//        
//        return result;
//    }
//    
//    /**
//     * 2016年情人节抽奖接口（10007026）
//     * @param paramJsonObject
//     * @param checkTag
//     * @return
//     * @throws Exception
//     */
//    public JsonObject lotteryAt2016Loves(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
//        JsonObject result = new JsonObject();
//        if (!checkTag) {
//            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
//            return result;
//        }
//        
//        /*
//         * 2016年情人节抽奖：活动时间范围限制 2016-02-13 ~ 2016-02-18
//         */
//        Date startTime = DateUtil.parseDateStringToDate("2016-02-13", "yyyy-MM-dd");
//        Date endTime = DateUtil.parseDateStringToDate("2016-02-18", "yyyy-MM-dd");
//        long curTime = System.currentTimeMillis();
//        if (curTime < startTime.getTime()
//                || curTime >= endTime.getTime()) {
//            result.addProperty("TagCode", TagCodeEnum.NOT_ACTIVITY_TIME);
//            return result;
//        }
//        
//        int userId, a;
//        String prizeId;
//        try {
//            userId = CommonUtil.getJsonParamInt(paramJsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
//            a = CommonUtil.getJsonParamInt(paramJsonObject, "a", 0, TagCodeEnum.APPID_MISSING, 1, 2);
//            
//            prizeId = CommonUtil.getJsonParamString(paramJsonObject, "prizeId", null, null, 0, 30);
//        } catch (CommonUtil.ErrorGetParameterException e) {
//            result.addProperty("TagCode", e.getErrCode());
//            return result;
//        } catch (Exception e) {
//            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
//            return result;
//        }
//        
//        // 判断用户是否为唱响主播，如果不是则返回错误
//        RoomInfo roomInfo = RoomService.getRoomInfo(userId);
//        if (roomInfo == null || roomInfo.getType() == null || roomInfo.getType() != 1) {
//            result.addProperty("TagCode", "07260001");
//            return result;
//        }
//        
//        // 判断用户是否送出520个“爱的抱抱”礼物
//        final int giftId = 40000621;
//        final int minGiftCount = 520;
//        
//        Long giftCount = 0l;
//        Map<String, Object> paraMap = new HashMap<String, Object>();
//        try {
//            paraMap.put("userId", userId);
//            paraMap.put("giftId", giftId);
//            paraMap.put("startTime", startTime);
//            paraMap.put("endTime", endTime);
//            giftCount = (Long) SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Index.getPeriodUserGiftReceiveCount", paraMap);
//        } catch (Exception e) {
//            logger.error("Index.getPeriodUserGiftReceiveCount(" + new Gson().toJson(paraMap) + ") execute exception", e);
//        }
//        
//        if (giftCount == null || giftCount < minGiftCount) {
//            result.addProperty("TagCode", "07260002");
//            return result;
//        }
//        
//        String awardRulesId = "lotteryAt2016Loves";
//        
//        // 刷新抽奖缓存
//        LotteryArithmetic.refreshUserTotalCanLottery(awardRulesId, userId, null, giftCount, false);
//        
//        // 计算用户
//        if (LotteryArithmeticCache.calUserRemain(awardRulesId, userId, null, 0) >= minGiftCount) {
//            Map<String, Object> retMap = LotteryArithmetic.lottery(awardRulesId, userId, a + "", prizeId);
//            if (retMap != null && !retMap.isEmpty() 
//                    && !LotteryArithmeticCache.SERVICE_KEY_LOTTERY_NO_PHONE_ERROR_STR.equals(retMap.get(LotteryArithmeticCache.SERVICE_KEY_giftId))
//                    && !LotteryArithmeticCache.SERVICE_KEY_LOTTERY_QUOTA_ERROR.equals(retMap.get(LotteryArithmeticCache.SERVICE_KEY_giftId))
//                    && !LotteryArithmeticCache.SERVICE_KEY_LOTTERY_REMAIN_ERROR.equals(retMap.get(LotteryArithmeticCache.SERVICE_KEY_giftId))) {
//                prizeId = (String) retMap.get(LotteryArithmeticCache.SERVICE_KEY_giftId);
//                
//                if (HotDataSource.incHotFieldValue(LovesAt2016.LOVES_REPLACE_COUNT, userId + "", 0) >= 1) {
//                    // 中大K宝超过20个或中过Banner或中Banner数超过3个用情人节专属座驾替代
//                    result.addProperty("prizeId", "1");
//                    result.addProperty("prizeName", "情人节专属座驾一辆（有效期1天）");
//                    result.addProperty("prizeCount", 1);
//                    
//                    HotDataSource.incHotFieldValue(LovesAt2016.LOVES_REPLACE_COUNT, userId + "", -1);
//                } else {
//                    result.addProperty("prizeId", prizeId);
//                    result.addProperty("prizeName", (String) retMap.get(LotteryArithmeticCache.SERVICE_KEY_giftName));
//                    result.addProperty("prizeCount", (Integer) retMap.get(LotteryArithmeticCache.SERVICE_KEY_giftCount));
//                }
//                
//                result.addProperty("quota", LotteryArithmeticCache.calUserRemain(awardRulesId, userId, null, minGiftCount - 1) / minGiftCount);
//                
//                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
//                
//            } else {
//                result.addProperty("TagCode", "07260003");
//                
//                if (retMap == null) {
//                    LotteryArithmeticCache.calUserRemain(awardRulesId, userId, null, - 1);
//                }
//            }
//        } else {
//            result.addProperty("TagCode", "07260003");
//        }
//        
//        return result;
//    }
//    
//    /**
//     * 2016年情人节获取剩余抽奖次数接口（10007027）
//     * @param paramJsonObject
//     * @param checkTag
//     * @return
//     * @throws Exception
//     */
//    public JsonObject getLotteryQuotaAt2016Loves(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
//        JsonObject result = new JsonObject();
//        if (!checkTag) {
//            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
//            return result;
//        }
//        
//      /*
//       * 2016年情人节抽奖：活动时间范围限制 2016-02-13 ~ 2016-02-18
//       */
//      Date startTime = DateUtil.parseDateStringToDate("2016-02-13", "yyyy-MM-dd");
//      Date endTime = DateUtil.parseDateStringToDate("2016-02-18", "yyyy-MM-dd");
//      long curTime = System.currentTimeMillis();
//      if (curTime < startTime.getTime()
//              || curTime >= endTime.getTime()) {
//          result.addProperty("TagCode", TagCodeEnum.NOT_ACTIVITY_TIME);
//          return result;
//      }
//        
//        int userId;
//        try {
//            userId = CommonUtil.getJsonParamInt(paramJsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
//        } catch (CommonUtil.ErrorGetParameterException e) {
//            result.addProperty("TagCode", e.getErrCode());
//            return result;
//        } catch (Exception e) {
//            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
//            return result;
//        }
//        
//        int quota = 0;
//        
//        // 判断用户是否为主播
//        if (com.melot.kkcx.service.UserService.isActor(userId)) {
//            // 判断用户是否送出520个“爱的抱抱”礼物
//            final int giftId = 40000621;
//            final int minGiftCount = 520;
//            
//            Long giftCount = 0l;
//            Map<String, Object> paraMap = new HashMap<String, Object>();
//            try {
//                paraMap.put("userId", userId);
//                paraMap.put("giftId", giftId);
//                paraMap.put("startTime", startTime);
//                paraMap.put("endTime", endTime);
//                giftCount = (Long) SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Index.getPeriodUserGiftReceiveCount", paraMap);
//            } catch (Exception e) {
//                logger.error("Index.getPeriodUserGiftReceiveCount(" + new Gson().toJson(paraMap) + ") execute exception", e);
//            }
//            
//            if (giftCount != null && giftCount >= minGiftCount) {
//                String awardRulesId = "lotteryAt2016Loves";
//                
//                // 刷新抽奖缓存
//                LotteryArithmetic.refreshUserTotalCanLottery(awardRulesId, userId, null, giftCount, false);
//                
//                // 计算用户剩余抽奖次数
//                quota = (int) (LotteryArithmeticCache.calUserRemain(awardRulesId, userId, null, 0) / minGiftCount);
//            }
//        }
//
//        result.addProperty("quota", quota);
//        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
//        
//        return result;
//    }
    
}

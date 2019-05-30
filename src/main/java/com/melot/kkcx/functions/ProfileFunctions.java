package com.melot.kkcx.functions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.api.menu.sdk.utils.Collectionutils;
import com.melot.blacklist.service.BlacklistService;
import com.melot.common.driver.domain.GiftRecord;
import com.melot.common.driver.domain.GiftRecordDTO;
import com.melot.common.driver.service.GiftHistoryService;
import com.melot.family.driver.domain.FamilyInfo;
import com.melot.family.driver.domain.DO.UserApplyActorDO;
import com.melot.family.driver.service.UserApplyActorService;
import com.melot.kk.hall.api.domain.HallRoomInfoDTO;
import com.melot.kk.module.report.util.CommonStateCode;
import com.melot.kk.opus.api.constant.OpusCostantEnum;
import com.melot.kk.recharge.api.dto.HistBuyProductRechargeDto;
import com.melot.kk.recharge.api.service.RechargeService;
import com.melot.kk.showmoney.api.dto.PageGameMoneyHistory;
import com.melot.kk.showmoney.api.dto.PageShowMoneyHistory;
import com.melot.kk.showmoney.api.dto.ShowMoneyHistory;
import com.melot.kk.showmoney.api.service.ShowMoneyService;
import com.melot.kk.town.api.constant.TownTaskStatusEnum;
import com.melot.kk.userSecurity.api.domain.DO.UserVerifyDO;
import com.melot.kk.userSecurity.api.service.UserVerifyService;
import com.melot.kkcore.actor.api.RoomInfoKeys;
import com.melot.kkcore.actor.service.ActorService;
import com.melot.kkcore.user.api.GameMoneyHistory;
import com.melot.kkcore.user.api.ProfileKeys;
import com.melot.kkcore.user.api.UserInfoDetail;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcore.user.api.UserRegistry;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kkcx.model.ActorLevel;
import com.melot.kkcx.model.CommonDevice;
import com.melot.kkcx.model.RichLevel;
import com.melot.kkcx.model.StarInfo;
import com.melot.kkcx.model.UserProp;
import com.melot.kkcx.service.FamilyService;
import com.melot.kkcx.service.GeneralService;
import com.melot.kkcx.service.MessageBoxServices;
import com.melot.kkcx.service.ProfileServices;
import com.melot.kkcx.service.UserAssetServices;
import com.melot.kkcx.service.UserService;
import com.melot.kkcx.transform.HallRoomTF;
import com.melot.kkcx.util.PropTypeEnum;
import com.melot.kkgame.redis.LiveTypeSource;
import com.melot.kktv.base.Result;
import com.melot.kktv.model.Family;
import com.melot.kktv.model.MedalInfo;
import com.melot.kktv.redis.HotDataSource;
import com.melot.kktv.redis.MedalSource;
import com.melot.kktv.redis.QQVipSource;
import com.melot.kktv.service.ConfigService;
import com.melot.kktv.service.LiveVideoService;
import com.melot.kktv.service.UserRelationService;
import com.melot.kktv.util.AppChannelEnum;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.CityUtil;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.DateUtil;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.kktv.util.TextFilter;
import com.melot.kktv.util.confdynamic.MedalConfig;
import com.melot.module.medal.driver.domain.ConfMedal;
import com.melot.module.medal.driver.domain.UserActivityMedal;
import com.melot.module.medal.driver.service.ActivityMedalService;
import com.melot.module.medal.driver.service.UserMedalService;
import com.melot.module.packagegift.driver.domain.ResUserXman;
import com.melot.module.packagegift.driver.domain.ResXman;
import com.melot.module.packagegift.driver.domain.UserChatBubbleDTO;
import com.melot.module.packagegift.driver.service.ChatBubbleService;
import com.melot.module.packagegift.driver.service.XmanService;
import com.melot.module.packagegift.util.PropEnum;
import com.melot.room.gift.dto.GiftInfoDTO;
import com.melot.room.gift.service.RoomGiftService;
import com.melot.room.live.record.constant.PageResult;
import com.melot.room.live.record.domain.ReturnResult;
import com.melot.room.live.record.dto.HistActorLiveDTO;
import com.melot.room.live.record.service.LiveRecordService;
import com.melot.room.pendant.dto.UserPendantDTO;
import com.melot.room.pendant.service.PendantService;
import com.melot.sdk.core.util.MelotBeanFactory;


public class ProfileFunctions {

    /**
     * 日志记录对象
     */
    private static Logger logger = Logger.getLogger(ProfileFunctions.class);

    @Autowired
    private ConfigService configService;

    @Resource
    UserApplyActorService userApplyActorService;

    @Resource
    UserVerifyService userVerifyService;

    @Resource
    ActorService actorService;

    @Resource
    LiveRecordService liveRecordService;

    @Resource
    RechargeService rechargeService;

    @Resource
    ChatBubbleService chatBubbleService;
    
    @Resource
    BlacklistService blacklistService;

    private LiveTypeSource liveTypeSource;

    public void setLiveTypeSource(LiveTypeSource liveTypeSource) {
        this.liveTypeSource = liveTypeSource;
    }

    /**
     * 获取用户信息(10005001)
     *
     * @param jsonObject 请求对象
     * @param checkTag   是否验证token标记
     * @return 登录结果
     */
    public JsonObject getUserInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        // 获取参数
        // 定义结果并组装json对象形式的返回结果
        JsonObject result = new JsonObject();

        int userId = 0, platform = 0, appId = 0, channel = 0, b = 0, fromLogin = 0;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "05010001", 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, PlatformEnum.WEB, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 0, Integer.MAX_VALUE);
            channel = CommonUtil.getJsonParamInt(jsonObject, "c", AppChannelEnum.KK, null, 0, Integer.MAX_VALUE);
            b = CommonUtil.getJsonParamInt(jsonObject, "b", 0, null, 0, Integer.MAX_VALUE);
            fromLogin = CommonUtil.getJsonParamInt(jsonObject, "fromLogin", 0, null, 0, 1);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        Transaction t;

        XmanService xmanService = (XmanService) MelotBeanFactory.getBean("xmanService");
        if (fromLogin == 0) {
            if (userId <= 1127828 && userId >= 1000578) {
                // 判断用户ID是否为神秘人ID
                t = Cat.getProducer().newTransaction("MCall", "xmanService.getXmanConf");
                try {
                    if (xmanService.getXmanConf(userId) != null) {
                        result.addProperty("TagCode", TagCodeEnum.USER_NOT_EXIST);
                        return result;
                    }
                    t.setStatus(Transaction.SUCCESS);
                } catch (Exception e) {
                    logger.error("Check user[" + userId + "] is xman execute exception.", e);
                    Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                    t.setStatus(e);
                } finally {
                    t.complete();
                }
            }
        }

        // 获取用户有效靓号
        try {
            JsonObject validVirtualId = null;
            t = Cat.getProducer().newTransaction("MCall", "UserAssetServices.getValidVirtualId");
            try {
                validVirtualId = UserAssetServices.getValidVirtualId(userId); //获取用户虚拟账号
                t.setStatus(Transaction.SUCCESS);
            } catch (Exception e) {
                Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                t.setStatus(e);
            } finally {
                t.complete();
            }
            if (validVirtualId != null) {
                if (validVirtualId.has("idType") && validVirtualId.get("idType").getAsInt() == 1) {
                    // 支持老版靓号
                    result.addProperty("luckyId", validVirtualId.get("id").getAsInt());
                }
                result.add("validId", validVirtualId);
            }
        } catch (Exception e) {
            logger.error("UserAssetServices.getValidVirtualId(" + userId + ") execute exception.", e);
        }

        //获取私有属性
        Map<String, String> hotData = null;
        t = Cat.getProducer().newTransaction("MRedis", "HotDataSource.getHotData(userId)");
        try {
            hotData = HotDataSource.getHotData(String.valueOf(userId));
            t.setStatus(Transaction.SUCCESS);
        } catch (Exception e) {
            Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
            t.setStatus(e);
        } finally {
            t.complete();
        }
        boolean bValidHotData = false;
        if (hotData != null && hotData.size() > 0) {
            if (hotData.containsKey("loadTime"))//loadTime is only set after loaded from oracle
            {
                bValidHotData = true;
            }
        }
        if (bValidHotData) {
            result.addProperty("userId", userId);
            if (hotData.containsKey("iconTag")) {
                result.addProperty("iconTag", Integer.valueOf(hotData.get("iconTag")));
            }
            if (hotData.containsKey("background") && hotData.containsKey("background_path_original")) {
                try {
                    Integer background = Integer.parseInt(hotData.get("background"));
                    Integer background_path_original = Integer.parseInt(hotData.get("background_path_original"));
                    result.addProperty("background", background);
                    result.addProperty("background_path_original", background_path_original);
                } catch (Exception e) {
                    result.addProperty("background", ConfigHelper.getHttpdir() + hotData.get("background"));
                    result.addProperty("background_path_original", ConfigHelper.getHttpdir() + hotData.get("background_path_original"));
                }
            }
            if (hotData.containsKey("backgroundshow")) {
                result.addProperty("backgroundshow", hotData.get("backgroundshow"));
            }
            if (hotData.containsKey("backgroundscroll")) {
                result.addProperty("backgroundscroll", hotData.get("backgroundscroll"));
            }
            if (hotData.containsKey("livevideoquality") && !StringUtil.strIsNull(hotData.get("livevideoquality"))) {
                result.addProperty("livevideoquality", Long.valueOf(hotData.get("livevideoquality")));
            }
            if (hotData.containsKey("tags")) {
                result.addProperty("tags", hotData.get("tags"));
            }

            /*
             * 判断该用户是否拥有神秘人标志
             * 0:无法隐身 1:未隐身 2:已隐身
             */
            try {
                ResUserXman resUserXman = null;
                t = Cat.getProducer().newTransaction("MCall", "xmanService.getResUserXmanByUserId");
                try {
                    resUserXman = xmanService.getResUserXmanByUserId(userId);
                    t.setStatus(Transaction.SUCCESS);
                } catch (Exception e) {
                    Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                    t.setStatus(e);
                } finally {
                    t.complete();
                }
                // 如果用户神秘人权限已过期
                if (resUserXman == null || (resUserXman.getExpireTime().getTime() < new Date().getTime())) {
                    result.addProperty("mysType", 0);
                } else {
                    ResXman resXman = null;
                    t = Cat.getProducer().newTransaction("MCall", "xmanService.getResXmanByUserId");
                    try {
                        resXman = xmanService.getResXmanByUserId(userId);
                        t.setStatus(Transaction.SUCCESS);
                    } catch (Exception e) {
                        Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                        t.setStatus(e);
                    } finally {
                        t.complete();
                    }
                    if (resXman != null && resXman.getMysType() == 2) {
                        result.addProperty("mysType", 2);
                    } else {
                        // 获取用户神秘人权限信息，如果有效权限，初始状态1
                        result.addProperty("mysType", 1);
                    }
                    // 获取用户神秘人权限过期时间
                    result.addProperty("mysExpireTime", resUserXman.getExpireTime().getTime());
                }
            } catch (Exception e) {
                logger.error("xmanService.getResUserXmanByUserId(" + userId + ") execute exception.", e);
            }

            // 更新redis的更新时间 过期时间7天
            Map<String, String> newTime = new HashMap<String, String>();
            newTime.put("time", String.valueOf(System.currentTimeMillis()));

            t = Cat.getProducer().newTransaction("MRedis", "HotDataSource.setHotData(userId, newTime, expireTime)");
            try {
                HotDataSource.setHotData(String.valueOf(userId), newTime, ConfigHelper.getRedisUserDataExpireTime());
                t.setStatus(Transaction.SUCCESS);
            } catch (Exception e) {
                Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                t.setStatus(e);
            } finally {
                t.complete();
            }
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } else {
            // 从oracle中获取最新用户信息,更新到redis的userInfo信息
            t = Cat.getProducer().newTransaction("MCall", "ProfileServices.updateRedisUserInfo");
            try {
                result = ProfileServices.updateRedisUserInfo(userId, null);
                t.setStatus(Transaction.SUCCESS);
            } catch (Exception e) {
                Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                t.setStatus(e);
            } finally {
                t.complete();
            }
        }

        if (result != null && result.has("TagCode") && result.get("TagCode").getAsString().equals(TagCodeEnum.SUCCESS)) {
            //获取公有属性
            UserInfoDetail userInfoDetail = null;
            t = Cat.getProducer().newTransaction("MCall", "com.melot.kkcore.user.service.KkUserService.getUserDetailInfo");
            try {
                KkUserService kkUserService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
                userInfoDetail = kkUserService.getUserDetailInfo(userId);
                t.setStatus(Transaction.SUCCESS);
            } catch (Exception e) {
                Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                t.setStatus(e);
            } finally {
                t.complete();
            }
            if (userInfoDetail == null || userInfoDetail.getRegisterInfo() == null) {
                JsonObject reResult = new JsonObject();
                reResult.addProperty("TagCode", TagCodeEnum.USER_NOT_EXIST);
                return reResult;
            }
            result.addProperty("openPlatform", userInfoDetail.getRegisterInfo().getOpenPlatform());
            result.addProperty("actorTag", userInfoDetail.getProfile().getIsActor());
            result.addProperty("gender", userInfoDetail.getProfile().getGender());
            result.addProperty("city", Math.abs(userInfoDetail.getRegisterInfo().getCityId()));
            result.addProperty("registerTime", userInfoDetail.getRegisterInfo().getRegisterTime());

            Integer adminType = ProfileServices.getUserAdminType(userId);
            if (adminType != null && adminType != -1) {
                result.addProperty("siteAdmin", adminType);
            }

            Integer area = CityUtil.getParentCityIdNoDefault(userInfoDetail.getRegisterInfo().getCityId());
            if (area != null) {
                result.addProperty("area", area);
            }
            result.addProperty("fansCount", UserRelationService.getFansCount(userId));
            if (userInfoDetail.getProfile().getNickName() != null) {
                t = Cat.getProducer().newTransaction("MCall", "GeneralService.replaceSensitiveWords");
                try {
                    String nickname = userInfoDetail.getProfile().getNickName();
                    //非官方号昵称需敏感词过滤
                    if (adminType == null || adminType == -1) {
                        nickname = GeneralService.replaceNicknameSensitiveWords(nickname);
                    }
                    result.addProperty("nickname", nickname);
                    t.setStatus(Transaction.SUCCESS);
                } catch (Exception e) {
                    Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                    t.setStatus(e);
                } finally {
                    t.complete();
                }
            }
            if (userInfoDetail.getProfile().getSignature() != null) {
                result.addProperty("signature", userInfoDetail.getProfile().getSignature());
            }
            if (userInfoDetail.getProfile().getIntroduce() != null) {
                result.addProperty("introduce", userInfoDetail.getProfile().getIntroduce());
            }
            if (userInfoDetail.getProfile().getBirthday() != null) {
                result.addProperty("birthday", userInfoDetail.getProfile().getBirthday());
            }
            if (userInfoDetail.getProfile().getExtendData() != null) {
                String emailAddress = (String) userInfoDetail.getProfile().getExtendData().get("emailAddress");
                if (!StringUtil.strIsNull(emailAddress)) {
                    result.addProperty("emailAddress", emailAddress);
                }
            }

            try {
                long consumeTotal = userInfoDetail.getAssets() == null ? 0 : userInfoDetail.getAssets().getConsumeTotal();
                long earnTotal = userInfoDetail.getAssets() == null ? 0 : userInfoDetail.getAssets().getEarnTotal();
                result.addProperty("earnTotal", earnTotal);
                result.addProperty("consumeTotal", consumeTotal);

                // 读取明星等级
                ActorLevel actorLevel = null;
                t = Cat.getProducer().newTransaction("MCall", "UserService.getActorLevel");
                try {
                    actorLevel = UserService.getActorLevel(earnTotal);
                    t.setStatus(Transaction.SUCCESS);
                } catch (Exception e) {
                    Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                    t.setStatus(e);
                } finally {
                    t.complete();
                }
                if (actorLevel != null) {
                    result.addProperty("actorLevel", actorLevel.getLevel());
                    result.addProperty("actorMin", actorLevel.getMinValue());
                    result.addProperty("actorMax", actorLevel.getMaxValue());
                }

                // 读取富豪等级
                RichLevel richLevel = null;
                t = Cat.getProducer().newTransaction("MCall", "UserService.getRichLevel");
                try {
                    richLevel = UserService.getRichLevel(consumeTotal);
                    t.setStatus(Transaction.SUCCESS);
                } catch (Exception e) {
                    Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                    t.setStatus(e);
                } finally {
                    t.complete();
                }
                if (richLevel != null) {
                    result.addProperty("richLevel", richLevel.getLevel());
                    result.addProperty("richMin", richLevel.getMinValue());
                    result.addProperty("richMax", richLevel.getMaxValue());
                }

                // 读取星级
                t = Cat.getProducer().newTransaction("MCall", "UserService.getStarLevel");
                try {
                    StarInfo starInfo = UserService.getStarInfo(userId);
                    result.addProperty("starLevel", starInfo.getLevel());
                    result.addProperty("starMin", starInfo.getMinValue());
                    result.addProperty("starMax", starInfo.getMaxVale());
                    result.addProperty("weeklyConsume", starInfo.getUserConsume());
                    t.setStatus(Transaction.SUCCESS);
                } catch (Exception e) {
                    Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                    t.setStatus(e);
                } finally {
                    t.complete();
                }
            } catch (Exception e) {
                logger.error("UserService.getUserInfoFromMongo(" + userId + ") execute exception.", e);
            }

            List<UserProp> userPropList = new ArrayList<>();
            try {
                PendantService pendantService = (PendantService) MelotBeanFactory.getBean("pendantService");
                com.melot.room.pendant.domain.ReturnResult<UserPendantDTO> pendantDTOResult = pendantService.getUserPendant(userId);
                if (pendantDTOResult != null && "0".equals(pendantDTOResult.getCode()) && pendantDTOResult.getData() != null) {
                    UserPendantDTO userPendantDTO = pendantDTOResult.getData();
                    UserProp userProp = new UserProp();
                    userProp.setId(userPendantDTO.getPendantId());
                    userProp.setType(PropTypeEnum.PENDANT.getCode());
                    userProp.setLevel(userPendantDTO.getLevel());
                    userProp.setSubType(userPendantDTO.getPendantType());
                    userProp.setIsLight(userPendantDTO.getUsed() ? 1 : 0);
                    userProp.setValidType(userPendantDTO.getValidType());
                    if (userPendantDTO.getValidTime() !=null){
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(userPendantDTO.getValidTime().getTime());
                        calendar.set(Calendar.HOUR_OF_DAY, 0);
                        calendar.set(Calendar.MINUTE, 0);
                        calendar.set(Calendar.SECOND, 0);
                        calendar.set(Calendar.MILLISECOND, 0);
                        userProp.setLeftTime(calendar.getTimeInMillis() - System.currentTimeMillis());
                    }

                    userProp.setName(userPendantDTO.getPendantName());
                    userProp.setDesc(userPendantDTO.getPendantDescribe());
                    userProp.setAppLargeUrl(userPendantDTO.getPendantBigUrl());
                    userProp.setWebLargeUrl(userPendantDTO.getPendantBigUrl());
                    userProp.setSmallUrl(userPendantDTO.getPendantSmallUrl());
                    userPropList.add(userProp);
                }
            } catch (Exception e) {
                logger.error("NodeFunctions.getUserInfoForNode execute pendantService.getUserPendant(" + userId + ") exception.", e);
            }

            try {
                //获取用户聊天气泡
                List<UserChatBubbleDTO> userChatBubbleDTOList = chatBubbleService.getUserChatBubbleList(userId, PropEnum.ENABLE);
                if (!Collectionutils.isEmpty(userChatBubbleDTOList)) {
                    for (UserChatBubbleDTO userChatBubbleDTO : userChatBubbleDTOList) {
                        userPropList.add(ProfileServices.switchBubbleToUserProp(userChatBubbleDTO));
                    }
                }
            } catch (Exception e) {
                logger.error("NodeFunctions.getUserInfoForNode execute chatBubbleService.getUserChatBubbleList(" + userId + ") exception.", e);
            }
            
            if (!Collectionutils.isEmpty(userPropList)) {
                result.add("userPropList", new JsonParser().parse(new Gson().toJson(userPropList)).getAsJsonArray());
            }

            // web端不返回
            if (platform != PlatformEnum.WEB) {
                result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
                result.addProperty("mediaPathPrefix", ConfigHelper.getMediahttpdir()); // 多媒体前缀
                result.addProperty("videoPathPrefix", ConfigHelper.getVideoURL());//七牛前缀
			    /*try {
			        JsonObject latestNews = null;
		        	t = Cat.getProducer().newTransaction("MCall", "NewsService.getUserLatestNews");
					try {
						latestNews = NewsService.getUserLatestNews(userId);
						t.setStatus(Transaction.SUCCESS);
					} catch (Exception e) {
						Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
						t.setStatus(e);
					} finally {
						t.complete();
					}
			        if (latestNews != null) {
			            result.add("latestNews", latestNews);
			            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
			            result.addProperty("mediaPathPrefix", ConfigHelper.getMediahttpdir()); // 多媒体前缀
						result.addProperty("videoPathPrefix", ConfigHelper.getVideoURL());//七牛前缀
			        }
                } catch (Exception e) {
                    logger.error("NewsService.getUserLatestNews(" + userId + ") execute exception.", e);
                }*/
            }

            // 获取用户会员信息
            JsonArray propArray = new JsonArray();
            try {
                List<Integer> propList = null;
                t = Cat.getProducer().newTransaction("MCall", "UserService.getUserProps");
                try {
                    propList = UserService.getUserProps(userId);
                    t.setStatus(Transaction.SUCCESS);
                } catch (Exception e) {
                    Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                    t.setStatus(e);
                } finally {
                    t.complete();
                }
                if (propList != null) {
                    for (Integer propId : propList) {
                        JsonObject obj = new JsonObject();
                        obj.addProperty("propId", propId);
                        propArray.add(obj);
                    }
                }
            } catch (Exception e) {
                logger.error("UserService.getUserProps(" + userId + ") execute exception.", e);
            }
            result.add("props", propArray);

            // 调用时带有token 说明是本人调用,需要返回用户秀币
            if (checkTag || channel == 70542 || channel == 559) {
                result.addProperty("followCount", UserRelationService.getFollowsCount(userId));
                if (userInfoDetail.getProfile().getPhoneNum() != null) {
                    result.addProperty("phoneNum", userInfoDetail.getProfile().getPhoneNum());
                }
                if (userInfoDetail.getProfile().getIdentifyPhone() != null) {
                    result.addProperty("identifyPhone", userInfoDetail.getProfile().getIdentifyPhone());
                } else if (userInfoDetail.getProfile().getPhoneNum() != null) {
                    result.addProperty("identifyPhone", userInfoDetail.getProfile().getPhoneNum());
                }

                // 获取用户个推客户端ID(现改为从redis读取)
                String gtClientId = null;
                t = Cat.getProducer().newTransaction("MCall", "com.melot.kktv.service.UserService.getGtClient");
                try {
                    gtClientId = com.melot.kktv.service.UserService.getGtClient(userId, appId, channel, b);
                    t.setStatus(Transaction.SUCCESS);
                } catch (Exception e) {
                    logger.error("com.melot.kktv.service.UserService.getGtClient(" + userId + ", " + appId + ", " + channel + ", " + b + ") execute exception.", e);
                    Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                    t.setStatus(e);
                } finally {
                    t.complete();
                }
                if (gtClientId != null) {
                    result.addProperty("gtClientId", gtClientId);
                }

                // 获取用户秀币余额
                long money = 0;
                t = Cat.getProducer().newTransaction("MCall", "com.melot.kktv.service.UserService.getUserShowMoney");
                try {
                    money = com.melot.kktv.service.UserService.getUserShowMoney(userId);
                    t.setStatus(Transaction.SUCCESS);
                } catch (Exception e) {
                    logger.error("com.melot.kktv.service.UserService.getUserShowMoney(" + userId + ") execute exception.", e);
                    Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                    t.setStatus(e);
                } finally {
                    t.complete();
                }
                result.addProperty("money", money);

                //获取用户密码安全等级
                int rank = -1;
                try {
                    t = Cat.getProducer().newTransaction("MCall", "KkUserService.getUserRegistry");
                    try {
                        KkUserService kkUserService = MelotBeanFactory.getBean("kkUserService", KkUserService.class);
                        UserRegistry userRegistry = kkUserService.getUserRegistry(userId);
                        rank = userRegistry.getPwdSafeLevel();
                        t.setStatus(Transaction.SUCCESS);
                    } catch (Exception e) {
                        Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                        t.setStatus(e);
                    } finally {
                        t.complete();
                    }
                } catch (Exception e) {
                    logger.error("Get user[" + userId + "] passwordSafetyRank execute exception.", e);
                }
                if (rank != -1) {
                    result.addProperty("passwordSafetyRank", rank == 0 ? 1 : rank);
                }

                // 获取用户所有门票 TODO 暂时不用此功能 modified by songjm at 2016-05-19
                JsonArray ticketArray = new JsonArray();
//				EntranceTicketService ticketService = (EntranceTicketService) MelotBeanFactory.getBean("entranceTicketService");
//				PageEntranceTicket ticket = ticketService.getUserTicketListForApi(userId, null, null, null, null, 1, 0, 20);
//				if (ticket != null && ticket.getEntranceTicket() != null && ticket.getEntranceTicket().size() > 0) {
//					for (EntranceTicket ticketInfo : ticket.getEntranceTicket()) {
//						
//						JsonObject ticketJson = new JsonObject();
//						
//						ticketJson.addProperty("ticketId", ticketInfo.getTicketId());
//						if (ticketInfo.getIcon() != null) {
//							ticketJson.addProperty("icon", ConfigHelper.getHttpdir() + ticketInfo.getIcon());
//						}
//						ticketJson.addProperty("price", ticketInfo.getPrice());
//						ticketJson.addProperty("startTime", ticketInfo.getStartTime().getTime());
//						ticketJson.addProperty("endTime", ticketInfo.getEndTime().getTime());
//						
//						ticketArray.add(ticketJson);
//					}
//				}
                result.add("tickets", ticketArray);

                int msgTotalCount = 0;
                t = Cat.getProducer().newTransaction("MCall", "MessageBoxServices.getUserMessageValue");
                try {
                    msgTotalCount = MessageBoxServices.getUserMessageValue(userId);
                    t.setStatus(Transaction.SUCCESS);
                } catch (Exception e) {
                    logger.error("MessageBoxServices.getUserMessageValue(" + userId + ") execute exception.", e);
                    Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                    t.setStatus(e);
                } finally {
                    t.complete();
                }
                result.addProperty("msgTotalCount", msgTotalCount);

                boolean checkPortrait = false;
                t = Cat.getProducer().newTransaction("MCall", "LiveVideoService.checkingPortrait");
                try {
                    checkPortrait = LiveVideoService.checkingPortrait(userId);
                    t.setStatus(Transaction.SUCCESS);
                } catch (Exception e) {
                    logger.error("LiveVideoService.checkingPortrait(" + userId + ") execute exception.", e);
                    Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                    t.setStatus(e);
                } finally {
                    t.complete();
                }
                if (checkPortrait) {
                    String path = OpusCostantEnum.CHECKING_PORTRAIT_RESOURCEURL;
                    result.addProperty("portrait_path_original", ConfigHelper.getHttpdir() + path);
                    result.addProperty("portrait_path_1280", ConfigHelper.getHttpdir() + path + "!1280");
                    result.addProperty("portrait_path_256", ConfigHelper.getHttpdir() + path + "!256");
                    result.addProperty("portrait_path_128", ConfigHelper.getHttpdir() + path + "!128");
                    result.addProperty("portrait_path_48", ConfigHelper.getHttpdir() + path + "!48");
                }
            }
            if (userInfoDetail.getProfile().getPortrait() != null && !result.has("portrait_path_original")) {
                result.addProperty("portrait_path_original", ConfigHelper.getHttpdir() + userInfoDetail.getProfile().getPortrait());
                result.addProperty("portrait_path_1280", ConfigHelper.getHttpdir() + userInfoDetail.getProfile().getPortrait() + "!1280");
                result.addProperty("portrait_path_256", ConfigHelper.getHttpdir() + userInfoDetail.getProfile().getPortrait() + "!256");
                result.addProperty("portrait_path_128", ConfigHelper.getHttpdir() + userInfoDetail.getProfile().getPortrait() + "!128");
                result.addProperty("portrait_path_48", ConfigHelper.getHttpdir() + userInfoDetail.getProfile().getPortrait() + "!48");
            }

            if (platform != PlatformEnum.WEB) {
                // 隐藏的调用获取用户照片列表接口
                try {
                    JsonObject param1 = new JsonObject();
                    param1.addProperty("userId", userId);
                    param1.addProperty("pageIndex", 1);
                    param1.addProperty("platform", platform);
                    AlbumFunctions albumFunctions = MelotBeanFactory.getBean("albumFunction", AlbumFunctions.class);
                    JsonObject getPhotoListResult = null;
                    t = Cat.getProducer().newTransaction("MCall", "albumFunctions.getUserPhotoList");
                    try {
                        getPhotoListResult = albumFunctions.getUserPhotoList(param1, checkTag, request);
                        t.setStatus(Transaction.SUCCESS);
                    } catch (Exception e) {
                        Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                        t.setStatus(e);
                    } finally {
                        t.complete();
                    }
                    if (getPhotoListResult != null) {
                        result.add("getPhotoListResult", getPhotoListResult);
                    }
                } catch (Exception e) {
                    logger.error("albumFunctions.getUserPhotoList(" + userId + ") execute exception.", e);
                }
            }

            // 返回QQ会员过期时间
            Long qqVipExpireTime = null;
            t = Cat.getProducer().newTransaction("MRedis", "QQVipSource.getQQVipExpireTime");
            try {
                qqVipExpireTime = QQVipSource.getQQVipExpireTime(String.valueOf(userId));
                t.setStatus(Transaction.SUCCESS);
            } catch (Exception e) {
                logger.error("QQVipSource.getQQVipExpireTime(" + userId + ") execute exception.", e);
                Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                t.setStatus(e);
            } finally {
                t.complete();
            }
            if (qqVipExpireTime != null) {
                result.addProperty("qqVipExpireTime", qqVipExpireTime);
            }

            // 添加勋章信息
            t = Cat.getProducer().newTransaction("MRedis", "MedalSource.getUserMedalsAsJson");
            try {
                result.add("userMedal", MedalSource.getUserMedalsAsJson(userId, platform));
                t.setStatus(Transaction.SUCCESS);
            } catch (Exception e) {
                logger.error("MedalSource.getUserMedalsAsJson(" + userId + ") execute exception.", e);
                Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                t.setStatus(e);
            } finally {
                t.complete();
            }

            try {
                ActivityMedalService activityMedalService = (ActivityMedalService) MelotBeanFactory.getBean("activityMedalService");
                JsonArray nowears = new JsonArray();
                //添加充值勋章信息,充值勋章所需要的字段都放到redis中，避免二次查询数据库
                UserMedalService userMedalService = (UserMedalService) MelotBeanFactory.getBean("userMedalService");
                com.melot.module.medal.driver.domain.GsonMedalObj medal = null;
                t = Cat.getProducer().newTransaction("MCall", "userMedalService.getMedalsByUserId");
                try {
                    medal = userMedalService.getMedalsByUserId(userId);
                    t.setStatus(Transaction.SUCCESS);
                } catch (Exception e) {
                    Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                    t.setStatus(e);
                } finally {
                    t.complete();
                }
                Date now = new Date();
                List<ConfMedal> medals = new ArrayList<ConfMedal>();
                if (medal != null) {
                    ConfMedal confMedal = null;
                    if (medal.getEndTime() > now.getTime()) { // 如果没有过期的话，才显示出来
                        MedalInfo medalInfo = null;
                        t = Cat.getProducer().newTransaction("MCall", "MedalConfig.getMedal");
                        try {
                            medalInfo = MedalConfig.getMedal(medal.getMedalId());
                            t.setStatus(Transaction.SUCCESS);
                        } catch (Exception e) {
                            Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                            t.setStatus(e);
                        } finally {
                            t.complete();
                        }
                        if (medalInfo != null) {
                            confMedal = new ConfMedal();

                            confMedal.setBright(medal.getLightState());

                            //提醒单独处理放到if判断中
                            if (medalInfo.getMedalLevel() == 8) {
                                confMedal.setMedalLevel(7);
                                confMedal.setIsTop(1);
                                confMedal.setMedalDes(medalInfo.getMedalDesc());
                            } else {
                                confMedal.setMedalLevel(medalInfo.getMedalLevel() - 1);
                                confMedal.setIsTop(0);
                                confMedal.setMedalDes(medalInfo.getMedalDesc());
                            }
                            confMedal.setMedalId(medalInfo.getMedalId());
                            confMedal.setMedalType(medalInfo.getMedalType());
                            confMedal.setMedalTitle(medalInfo.getMedalTitle());
                            confMedal.setMedalExpireTime(medal.getEndTime());
                            confMedal.setMedalMedalUrl(medalInfo.getMedalIcon());

                            //充值勋章不点亮放入可配置勋章列表
                            if (confMedal.getBright() == 0) {
                                nowears.add(confMedal.toJsonObject());
                            } else {
                                medals.add(confMedal);
                            }
                        }
                    }
                }

                //异常短信开关
                result.addProperty("loginSmsSwitch", UserService.getUserSmsSwitch(userId));

                //可佩带勋章仅自己可见
                if (checkTag) {
                    // 用户可佩戴的活动勋章
                    List<UserActivityMedal> noWearList = null;
                    t = Cat.getProducer().newTransaction("MCall", "activityMedalService.getUserNoWearMedals");
                    try {
                        noWearList = activityMedalService.getUserNoWearMedals(userId);
                        t.setStatus(Transaction.SUCCESS);
                    } catch (Exception e) {
                        Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                        t.setStatus(e);
                    } finally {
                        t.complete();
                    }
                    if (noWearList != null && noWearList.size() > 0) {
                        for (UserActivityMedal userActivityMedal : noWearList) {
                            JsonObject jObject = new JsonObject();
                            jObject.addProperty("medalId", userActivityMedal.getMedalId());
                            jObject.addProperty("medalTitle", userActivityMedal.getMedalTitle());
                            jObject.addProperty("medalType", userActivityMedal.getMedalType());
                            jObject.addProperty("medalIcon", userActivityMedal.getMedalIcon());
                            jObject.add("medalDesc", new JsonParser().parse(userActivityMedal.getMedalDesc()).getAsJsonObject().get("description"));
                            jObject.addProperty("medalRefId", userActivityMedal.getMedalRefId());
                            jObject.addProperty("endTime", userActivityMedal.getEndTime().getTime());
                            jObject.addProperty("lightState", userActivityMedal.getLightState());
                            nowears.add(jObject);
                        }
                    }
                }

                List<UserActivityMedal> wearList = null;
                t = Cat.getProducer().newTransaction("MCall", "activityMedalService.getUserWearMedals");
                try {
                    wearList = activityMedalService.getUserWearMedals(userId);
                    t.setStatus(Transaction.SUCCESS);
                } catch (Exception e) {
                    Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                    t.setStatus(e);
                } finally {
                    t.complete();
                }
                if (wearList != null && wearList.size() > 0) {
                    for (UserActivityMedal userActivityMedal : wearList) {
                        ConfMedal confMedal = new ConfMedal();
                        confMedal.setIsTop(0);
                        confMedal.setMedalId(userActivityMedal.getMedalId());
                        confMedal.setBright(userActivityMedal.getLightState());
                        confMedal.setMedalDes(userActivityMedal.getMedalDesc() != null ? String.valueOf(new JsonParser().parse(userActivityMedal.getMedalDesc()).getAsJsonObject().get("description")) : null);
                        confMedal.setMedalType(userActivityMedal.getMedalType());
                        confMedal.setMedalTitle(userActivityMedal.getMedalTitle());
                        confMedal.setMedalExpireTime(userActivityMedal.getEndTime().getTime());
                        confMedal.setMedalMedalUrl(userActivityMedal.getMedalIcon());
                        medals.add(confMedal);
                    }
                }

                // 直播精灵过滤充值勋章信息
                if (appId != 8) {
                    result.add("userMedalList", new JsonParser().parse(new Gson().toJson(medals)).getAsJsonArray());

                    //可佩带勋章仅自己可见
                    if (checkTag) {
                        result.add("noWearMedalList", nowears);
                    }
                }
            } catch (Exception e) {
                logger.error("Get user[" + userId + "] medal execute exception.", e);
            }

            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
            // 获取房间信息
            try {
                if (result.has("actorTag") && result.get("actorTag") != null) {
                    int actorTag = result.get("actorTag").getAsInt();
                    if (actorTag == 1) {
                        //获取实名认证状态
                        UserApplyActorDO applyActor = null;
                        t = Cat.getProducer().newTransaction("MCall", "userApplyActorService.getUserApplyActorDO");
                        try {
                            applyActor = userApplyActorService.getUserApplyActorDO(userId).getData();
                            t.setStatus(Transaction.SUCCESS);
                        } catch (Exception e) {
                            logger.error("Fail to call ApplyActorService.getApplyActorByActorId ", e);
                            Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                            t.setStatus(e);
                        } finally {
                            t.complete();
                        }
                        if (applyActor != null && applyActor.getStatus() != null) {
                            result.addProperty("identityStatus", applyActor.getStatus());
                        }
                        // 从PG读取
                        RoomInfo roomInfo = null;
                        t = Cat.getProducer().newTransaction("MCall", "com.melot.kktv.service.RoomService.getRoomInfo");
                        try {
                            roomInfo = com.melot.kktv.service.RoomService.getRoomInfo(userId);
                            t.setStatus(Transaction.SUCCESS);
                        } catch (Exception e) {
                            Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                            t.setStatus(e);
                        } finally {
                            t.complete();
                        }
                        if (roomInfo != null) {
                            result.addProperty("roomId", roomInfo.getRoomId() != null ? roomInfo.getRoomId() : roomInfo.getActorId());
                            if (checkTag) {
                                Long actorKb = null;
                                t = Cat.getProducer().newTransaction("MCall", "com.melot.kkcx.service.UserService.getActorKbi");
                                try {
                                    actorKb = com.melot.kkcx.service.UserService.getActorKbi(userId);
                                    t.setStatus(Transaction.SUCCESS);
                                } catch (Exception e) {
                                    Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                                    t.setStatus(e);
                                } finally {
                                    t.complete();
                                }
                                actorKb = (actorKb == null || actorKb < 0) ? 0 : actorKb;
                                result.addProperty("kbi", actorKb);
                            }
                            if (roomInfo.getScreenType() != null) {
                                result.addProperty("screenType", roomInfo.getScreenType());
                            } else {
                                result.addProperty("screenType", 1);
                            }

                            if (roomInfo.getLiveType() != null) {
                                result.addProperty("liveType", roomInfo.getLiveType());
                            }
                            if (roomInfo.getLiveStarttime() != null) {
                                result.addProperty("livestarttime", roomInfo.getLiveStarttime().getTime());
                            }
                            if (roomInfo.getLiveEndtime() != null) {
                                result.addProperty("liveendtime", roomInfo.getLiveEndtime().getTime());
                            }
                            if (roomInfo.getNextStarttime() != null) {
                                result.addProperty("nextstarttime", roomInfo.getNextStarttime().getTime());
                            }
                            if (roomInfo.getPoster() != null) {
                                result.addProperty("poster_path_original", ConfigHelper.getHttpdir() + roomInfo.getPoster());
                                result.addProperty("poster_path_1280", ConfigHelper.getHttpdir() + roomInfo.getPoster() + "!1280");
                                result.addProperty("poster_path_290", ConfigHelper.getHttpdir() + roomInfo.getPoster() + "!290x164");
                                result.addProperty("poster_path_272", ConfigHelper.getHttpdir() + roomInfo.getPoster() + "!272");
                                result.addProperty("poster_path_128", ConfigHelper.getHttpdir() + roomInfo.getPoster() + "!128x96");
                                result.addProperty("poster_path_300", ConfigHelper.getHttpdir() + roomInfo.getPoster() + "!300");
                            }
                            if (roomInfo.getRoomTheme() != null) {
                                result.addProperty("roomTheme", roomInfo.getRoomTheme());
                            }
                            if (roomInfo.getRoomMode() != null) {
                                // 房间模式 0:普通房 1:演艺房 2:游戏房 3 ：唱响家族房
                                int roomMode = roomInfo.getRoomMode();
                                result.addProperty("roomMode", roomMode);
                                if (roomMode == 3) {
                                    // 家族房增加家族id返回
                                    result.addProperty("familyId", roomInfo.getFamilyId());
                                }
                                if (roomInfo.getRoomMode().intValue() == 1) {
                                    result.addProperty("videoLevel", 1);
                                }
                                if (roomInfo.getRoomMode().intValue() == 2) {
                                    result.addProperty("userPart", "[107]");
                                    result.addProperty("videoLevel", 1);
                                }
                            }
                            if (roomInfo.getRoomSource() != null) {
                                result.addProperty("roomSource", roomInfo.getRoomSource());
                            } else {
                                result.addProperty("roomSource", AppIdEnum.AMUSEMENT);
                            }
                            if (roomInfo != null && roomInfo.getType() != null) {
                                result.addProperty("roomType", roomInfo.getType());
                                // 临时增加（主播勋章问题）
                                if (roomInfo.getType().intValue() == AppIdEnum.AMUSEMENT &&
                                        result.get("userMedal").getAsJsonArray().size() == 0) {
                                    // 官方家族没有勋章
                                    if (roomInfo.getFamilyId() != null && roomInfo.getFamilyId() != 11222 && roomInfo.getFamilyId() != 12345) {
                                        Family family = null;
                                        t = Cat.getProducer().newTransaction("MCall", "FamilyService.getFamilyInfo");
                                        try {
                                            family = FamilyService.getFamilyInfo(roomInfo.getFamilyId(), 1);
                                            t.setStatus(Transaction.SUCCESS);
                                        } catch (Exception e) {
                                            Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                                            t.setStatus(e);
                                        } finally {
                                            t.complete();
                                        }
                                        if (family != null && family.getFamilyMedal() != null && family.getOpen() != null && family.getOpen() != 2) {
                                            result.addProperty("familyName", family.getFamilyName());
                                            t = Cat.getProducer().newTransaction("MRedis", "MedalSource.addUserMedal");
                                            try {
                                                //MedalSource.addUserMedal(userId, family.getFamilyMedal(), -1);
                                                t.setStatus(Transaction.SUCCESS);
                                            } catch (Exception e) {
                                                Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                                                t.setStatus(e);
                                            } finally {
                                                t.complete();
                                            }
                                            t = Cat.getProducer().newTransaction("MRedis", "MedalSource.getUserMedalsAsJson(");
                                            try {
                                                result.add("userMedal", MedalSource.getUserMedalsAsJson(userId, null));
                                                t.setStatus(Transaction.SUCCESS);
                                            } catch (Exception e) {
                                                Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                                                t.setStatus(e);
                                            } finally {
                                                t.complete();
                                            }
                                        }
                                    }
                                }
                            } else {
                                result.addProperty("roomType", AppIdEnum.AMUSEMENT);
                            }
                            if (roomInfo.getPeopleInRoom() != null) {
                                result.addProperty("onlineCount", roomInfo.getPeopleInRoom());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Get user[" + userId + "] actor info execute exception.", e);
            }

            try {
                // 获取用户家族名
                if (!result.has("familyName")) {
                    FamilyInfo familyInfo = null;
                    t = Cat.getProducer().newTransaction("MCall", "FamilyService.getUserFamilyName");
                    try {
                        familyInfo = FamilyService.getUserFamilyName(userId, 1);
                        t.setStatus(Transaction.SUCCESS);
                    } catch (Exception e) {
                        Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                        t.setStatus(e);
                    } finally {
                        t.complete();
                    }
                    if (familyInfo != null && familyInfo.getFamilyName() != null && familyInfo.getFamilyId() != 11222 && familyInfo.getFamilyId() != 12345) {
                        result.addProperty("familyName", familyInfo.getFamilyName());
                    }
                }
            } catch (Exception e) {
                logger.error("Get familyName execute exception, userId :" + userId, e);
            }
        } else {
            result.addProperty("TagCode", TagCodeEnum.USER_NOT_EXIST);
        }

        // 返回结果
        return result;
    }

    /**
     * 根据用户ID或靓号ID获取用户信息 (10005043)
     *
     * @param jsonObject 请求对象
     * @return 登录结果
     */
    public JsonObject getUserInfoById(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        int luckyId = 0;
        try {
            luckyId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        // 调用虚拟号模块
        try {
            Integer userId = UserAssetServices.luckyIdToUserId(luckyId);
            if (userId != null && userId > 0) {
                luckyId = userId;
            }
        } catch (Exception e) {
        }

        jsonObject.addProperty("userId", luckyId);
        return getUserInfo(jsonObject, false, request);
    }

    /**
     * 获取用户直播状态(10005042)
     *
     * @param jsonObject 请求对象
     * @return
     */
    public JsonObject getUserLiveState(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "05420002", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        RoomInfo roomInfo = com.melot.kktv.service.RoomService.getRoomInfo(userId);
        if (roomInfo != null) {
            if (roomInfo.getScreenType() != null) {
                result.addProperty("screenType", roomInfo.getScreenType());
            } else {
                result.addProperty("screenType", 1);
            }

            if (roomInfo.getLiveType() != null) {
                result.addProperty("liveType", roomInfo.getLiveType());
            }
            if (roomInfo.getLiveStarttime() != null) {
                result.addProperty("livestarttime", roomInfo.getLiveStarttime().getTime());
            }
            if (roomInfo.getLiveEndtime() != null) {
                result.addProperty("liveendtime", roomInfo.getLiveEndtime().getTime());
            }
            if (roomInfo.getNextStarttime() != null) {
                result.addProperty("nextstarttime", roomInfo.getNextStarttime().getTime());
            }
            if (roomInfo.getRoomSource() != null) {
                result.addProperty("roomSource", roomInfo.getRoomSource());
            }
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } else {
            result.addProperty("TagCode", "05420003");
        }

        return result;
    }

    /**
     * 更新用户信息(10005002)
     *
     * @param jsonObject 请求对象
     * @param checkTag   是否验证token标记
     * @return 更新结果
     */
    public JsonObject updateUserInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        // 获取参数
        JsonElement userIdje = jsonObject.get("userId");
        JsonElement nicknameje = jsonObject.get("nickname");
        JsonElement genderje = jsonObject.get("gender");
        JsonElement emailAddressje = jsonObject.get("emailAddress");
        JsonElement birthdayje = jsonObject.get("birthday");
        JsonElement cityje = jsonObject.get("city");
        JsonElement signatureje = jsonObject.get("signature");
        JsonElement introduceje = jsonObject.get("introduce");

        // 验证参数
        int userId;
        if (userIdje != null && !userIdje.isJsonNull() && !userIdje.getAsString().equals("")) {
            // 验证数字
            try {
                userId = userIdje.getAsInt();
            } catch (Exception e) {
                result.addProperty("TagCode", "05020002");
                return result;
            }
        } else {
            result.addProperty("TagCode", "05020001");
            return result;
        }
        // 更新头像
//		JsonElement photoje = jsonObject.get("photo");
//		if (photoje != null && !photoje.isJsonNull() && !photoje.getAsString().isEmpty()) {
//			new SaveRemotePortrait(userId, photoje.getAsString()).start();
//		}
        Map<String, Object> userMap = new HashMap<String, Object>();
        int flag = 0;
        String nickname = null;
        Integer gender = null;
        String emailAddress = null;
        String introduce = null;
        String tagCode = TagCodeEnum.SUCCESS;
        boolean isNickNameChange = false;
        if (nicknameje != null && !nicknameje.isJsonNull() && !nicknameje.getAsString().trim().isEmpty()) {
            nickname = nicknameje.getAsString().trim();
            // filter matchXSSTag,sensitive word,short url
            if (CommonUtil.matchXSSTag(nickname)
                    || TextFilter.isShortUrl(nickname)
                    || !TextFilter.checkSpecialUnicode(nickname) || GeneralService.nicknameHasSensitiveWords(nickname)) {
                result.addProperty("TagCode", "05020004");
                return result;
            }

            // 昵称长度 最多10个字符，多余的位数自动丢弃
            // 如果昵称中有数字，不管是连续还是不连续，都只保留6个，多余的丢弃
            int digitCount = 0;
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < nickname.length(); i++) {
                if (Character.isDigit(nickname.charAt(i))) {
                    digitCount++;
                    if (digitCount <= 6) {
                        sb.append(nickname.charAt(i));
                        if (sb.length() >= 10) {
                            break;
                        }
                    }
                } else {
                    sb.append(nickname.charAt(i));
                    if (sb.length() >= 10) {
                        break;
                    }
                }
            }
            nickname = sb.toString();
        }
        if (genderje != null && !(genderje.isJsonNull() || genderje.getAsString().equals(""))) {
            try {
                gender = genderje.getAsInt();
            } catch (Exception e) {
                result.addProperty("TagCode", "05020003");
                return result;
            }
            userMap.put(ProfileKeys.GENDER.key(), gender);
            flag++;
        }
        if (emailAddressje != null && !(emailAddressje.isJsonNull() || emailAddressje.getAsString().equals(""))) {
            try {
                emailAddress = emailAddressje.getAsString();
            } catch (Exception e) {
                result.addProperty("TagCode", "05020009");
                return result;
            }
            Map<String, Object> extendDataMap = new HashMap<>();
            extendDataMap.put("emailAddress", emailAddress);
            userMap.put("extendData", extendDataMap);
            flag++;
        }
        if (birthdayje != null && !(birthdayje.isJsonNull() || birthdayje.getAsString().equals(""))) {
            userMap.put(ProfileKeys.BIRTHDAY.key(), birthdayje.getAsString());
            flag++;
        }
        if (cityje != null && !(cityje.isJsonNull() || cityje.getAsString().equals(""))) {
            int city;
            try {
                city = cityje.getAsInt();
                //无效的city信息
                if (!GeneralService.isValidCity(city)) {
                    result.addProperty("TagCode", "05020007");
                    return result;
                }
            } catch (Exception e) {
                result.addProperty("TagCode", "05020007");
                return result;
            }

            //用户手动修改档案中地址信息后，地址信息记录为负，表示登录的时候不再动态更新该用户地址
            userMap.put("cityId", city * -1);

            flag++;
        }
        if (signatureje != null && !signatureje.isJsonNull()) {
            // matchXSSTag
            if (CommonUtil.matchXSSTag(signatureje.getAsString())) {
                result.addProperty("TagCode", "05020008");
                return result;
            }
            String signature = GeneralService.replaceSensitiveWords(userId, signatureje.getAsString());
            userMap.put(ProfileKeys.SIGNATURE.key(), signature);
            flag++;
        }
        //官方号特殊时期不限制
        if (introduceje != null && !introduceje.isJsonNull() &&
                (!configService.getIsSpecialTime() || (configService.getIsSpecialTime() && ProfileServices.checkIsOfficial(userId)))) {
            introduce = introduceje.getAsString();
            introduce = GeneralService.replaceSensitiveWords(userId, introduce);
            userMap.put("introduce", introduce);
            flag++;
        }
        if (nickname != null) {
            isNickNameChange = UserService.checkNicknameChange(nickname, userId);
            if (UserService.checkNicknameRepeat(nickname, userId)) {
                // 昵称重复
                result.addProperty("TagCode", "05020102");
                return result;
            } else {
                if (isNickNameChange) {
                    //特殊时期 官方号无需过滤
                    if (configService.getIsSpecialTime() && !ProfileServices.checkIsOfficial(userId)) {
//                        UserRegistry userRegistry = UserService.getUserRegistryInfo(userId);
//                        if (userRegistry != null && !ProfileServices.checkUserUpdateProfileByType(userId, "nickName")) {
//                            //特殊时期昵称修改需前置审核
//                            ProfileServices.insertChangeUserName(userId, nickname, 3);
//                            ProfileServices.setUserUpdateProfileByType(userId, "nickName");
//                        }
                        tagCode = TagCodeEnum.NICKNAME_PENDINGAUDIT;
                    } else {
                        userMap.put(ProfileKeys.NICKNAME.key(), nickname);
                        flag++;
                    }
                }
            }
        }

        result.addProperty("TagCode", tagCode);
        if (flag > 0) {
            int TagCode = com.melot.kktv.service.UserService.updateUserInfoV2(userId, userMap);
            if (TagCode != 0) {
                logger.error("调用存储过程(com.melot.kktv.service.UserService.updateUserInfo(" + userId + "," + new Gson().toJson(userMap) + "))未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
                result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
                return result;
            }

            //非特殊时期插入昵称后置审核记录
            if (!configService.getIsSpecialTime() && isNickNameChange) {
                ProfileServices.insertChangeUserName(userId, nickname, 0);
            }
            // 删除HotData缓存
            HotDataSource.del(userId + "");
            ProfileServices.updateRedisUserInfo(userId, null);
        }
        // 返回结果
        return result;
    }

    /**
     * 获取用户荣誉列表
     *
     * @param jsonObject 请求对象
     * @return json对象形式的返回结果
     */
    public JsonObject getHonorList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        JsonArray jHonorList = new JsonArray();
        result.add("honorList", jHonorList);
        return result;
    }

    /**
     * 获取用户送出的礼物列表
     *
     * @param jsonObject 请求对象
     * @param checkTag   是否验证token标记
     * @return json对象形式的返回结果
     */
    public JsonObject getUserSendGiftList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId, pageIndex;
        long startTime, endTime;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, "05070007", 1, Integer.MAX_VALUE);
            startTime = CommonUtil.getJsonParamLong(jsonObject, "startTime", 0, "10006004", DateUtil.getDayBeginTime(System.currentTimeMillis()) - 180 * 24 * 3600 * 1000L, Long.MAX_VALUE);
            endTime = CommonUtil.getJsonParamLong(jsonObject, "endTime", 0, "10006006", startTime, Long.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            GiftHistoryService giftHistoryService = (GiftHistoryService) MelotBeanFactory.getBean("giftHistoryService");
            GiftRecordDTO giftdata = giftHistoryService.getUserSendGiftList(userId, startTime, endTime, pageIndex);
            if (giftdata != null) {
                String TagCode = giftdata.getTagcode();
                if (TagCode.equals(TagCodeEnum.SUCCESS)) {
                    List<GiftRecord> recordList = giftdata.getRecordList();
                    recordList = UserService.addUserExtra(recordList);

                    result.addProperty("TagCode", TagCode);
                    result.addProperty("pageTotal", giftdata.getPageTotal());

                    JsonArray jRecordList = new JsonArray();
                    if (recordList != null) {
                        List<Integer> giftIdList;//需拼接信息的礼物id
                        Map<Integer, GiftInfoDTO> giftInfoMap = new HashMap<>();
                        Set<Integer> tempSet = new HashSet<>();
                        for (GiftRecord record : recordList) {
                            if (record.getGiftName() == null) {
                                tempSet.add(record.getGiftId());
                            }
                        }
                        giftIdList = new ArrayList<>(tempSet);

                        if (giftIdList.size() > 0) {
                            //查询并存储拼接信息
                            RoomGiftService roomGiftService = (RoomGiftService) MelotBeanFactory.getBean("roomGiftService");
                            com.melot.room.gift.domain.ReturnResult<List<GiftInfoDTO>> returnResultOfIds = roomGiftService.listGiftWithGiftIds(giftIdList);
                            if (returnResultOfIds.getCode().equals("0")) {
                                for (GiftInfoDTO giftInfoTemp : returnResultOfIds.getData()) {
                                    giftInfoMap.put(giftInfoTemp.getGiftId(), giftInfoTemp);
                                }
                            } else {
                                logger.error("调用模块返回不成功:RoomGiftService.listGiftWithGiftIds() return code: " + returnResultOfIds.getCode() + "desc: " + returnResultOfIds.getDesc());
                            }
                        }

                        for (GiftRecord record : recordList) {
                            if (record.getGiftName() == null && giftInfoMap.containsKey(record.getGiftId())) {
                                GiftInfoDTO temp = giftInfoMap.get(record.getGiftId());
                                record.setGiftName(temp.getGiftName());
                                record.setUnit(temp.getUnit());
                                record.setSendPrice(temp.getSendPrice());
                                record.setRsvPrice(temp.getRsvPrice());
                            }
                            jRecordList.add(toSendJsonObject(record));
                        }
                    }
                    result.add("recordList", jRecordList);

                    // 返回结果
                    return result;
                } else if (TagCode.equals("02")) {
                    /* '02';分页超出范围 */
                    result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                    result.addProperty("pageTotal", giftdata.getPageTotal());
                    result.add("recordList", new JsonArray());

                    // 返回结果
                    return result;
                } else {
                    // 模块内部抛异常
                    logger.error("模块内部抛异常:GiftHistoryService.getUserSendGiftList(" + "userId:" + userId + "startTime:" + startTime + "endTime:" + endTime + "pageIndex:" + pageIndex + ") execute exception.");
                    result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                    return result;
                }
            } else {
                //模块返回空数据
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
        } catch (Exception e) {
            logger.error("ProfileFunctions.getUserSendGiftList execute exception.", e);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        }
    }

    /**
     * 获取用户收到的礼物列表
     *
     * @param jsonObject 请求对象
     * @return 结果字符串
     */
    public JsonObject getUserRsvGiftList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        int userId, pageIndex;
        long startTime, endTime;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, "10006008", 1, Integer.MAX_VALUE);
            startTime = CommonUtil.getJsonParamLong(jsonObject, "startTime", 0, "10006004", DateUtil.getDayBeginTime(System.currentTimeMillis()) - 180 * 24 * 3600 * 1000L, Long.MAX_VALUE);
            endTime = CommonUtil.getJsonParamLong(jsonObject, "endTime", 0, "10006006", startTime, Long.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            GiftHistoryService giftHistoryService = (GiftHistoryService) MelotBeanFactory.getBean("giftHistoryService");
            GiftRecordDTO giftdata = giftHistoryService.getUserRsvGiftList(userId, startTime, endTime, pageIndex);
            if (giftdata != null) {
                String TagCode = giftdata.getTagcode();
                if (TagCode.equals(TagCodeEnum.SUCCESS)) {
                    List<GiftRecord> recordList = giftdata.getRecordList();

                    result.addProperty("TagCode", TagCode);
                    result.addProperty("pageTotal", giftdata.getPageTotal());

                    JsonArray jRecordList = new JsonArray();
                    if (recordList != null) {
                        List<Integer> giftIdList;//需拼接信息的礼物id
                        Map<Integer, GiftInfoDTO> giftInfoMap = new HashMap<>();
                        Set<Integer> tempSet = new HashSet<>();
                        for (GiftRecord record : recordList) {
                            if (record.getGiftName() == null) {
                                tempSet.add(record.getGiftId());
                            }
                        }
                        giftIdList = new ArrayList<>(tempSet);

                        if (giftIdList.size() > 0) {
                            //查询并存储拼接信息
                            RoomGiftService roomGiftService = (RoomGiftService) MelotBeanFactory.getBean("roomGiftService");
                            com.melot.room.gift.domain.ReturnResult<List<GiftInfoDTO>> returnResultOfIds = roomGiftService.listGiftWithGiftIds(giftIdList);
                            if (returnResultOfIds.getCode().equals("0")) {
                                for (GiftInfoDTO giftInfoTemp : returnResultOfIds.getData()) {
                                    giftInfoMap.put(giftInfoTemp.getGiftId(), giftInfoTemp);
                                }
                            } else {
                                logger.error("调用模块返回不成功:RoomGiftService.listGiftWithGiftIds() return code: " + returnResultOfIds.getCode() + "desc: " + returnResultOfIds.getDesc());
                            }
                        }

                        for (GiftRecord record : recordList) {
                            if (record.getGiftName() == null && giftInfoMap.containsKey(record.getGiftId())) {
                                GiftInfoDTO temp = giftInfoMap.get(record.getGiftId());
                                record.setGiftName(temp.getGiftName());
                                record.setUnit(temp.getUnit());
                                record.setSendPrice(temp.getSendPrice());
                                record.setRsvPrice(temp.getRsvPrice());
                            }
                            JsonObject recObj = toRsvJsonObject(record);
                            // 默认为kk唱响用户
                            recObj.addProperty("roomSource", AppIdEnum.AMUSEMENT);
                            recObj.addProperty("roomType", AppIdEnum.AMUSEMENT);
                            jRecordList.add(recObj);
                        }
                    }
                    result.add("recordList", jRecordList);

                    // 返回结果
                    return result;
                } else if (TagCode.equals("02")) {
                    /* '02';分页超出范围 */
                    result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                    result.addProperty("pageTotal", giftdata.getPageTotal());
                    result.add("recordList", new JsonArray());
                    return result;
                } else {
                    // 模块内部抛异常
                    logger.error("模块内部抛异常:GiftHistoryService.getUserRsvGiftList(" + "userId:" + userId + "startTime:" + startTime + "endTime:" + endTime + "pageIndex:" + pageIndex + ") execute exception.");
                    result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                    return result;
                }
            } else {
                //模块返回空数据
                result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
        } catch (Exception e) {
            logger.error("ProfileFunctions.getUserSendGiftList execute exception.", e);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        }
    }

    /**
     * 转成用户接受礼物的JsonObject
     *
     * @return JsonObject
     */
    private JsonObject toRsvJsonObject(GiftRecord record) {
        JsonObject jObject = new JsonObject();
        if (record.getXmanNick() != null) {
            jObject.addProperty("senderNick", record.getXmanNick());
        } else {
            jObject.addProperty("senderNick", UserService.getUserInfoNew(record.getUserId()).getNickName());
        }
        if (record.getXmanId() != null) {
            jObject.addProperty("senderId", record.getXmanId());
        } else {
            jObject.addProperty("senderId", record.getUserId());
        }
        jObject.addProperty("giftId", record.getGiftId());
        jObject.addProperty("giftName", record.getGiftName());
        jObject.addProperty("unit", record.getUnit());
        jObject.addProperty("sendPrice", record.getSendPrice());
        jObject.addProperty("rsvPrice", record.getRsvPrice());
        jObject.addProperty("count", record.getCount());
        jObject.addProperty("sendTime", record.getSendTime().getTime());

        return jObject;
    }

    /**
     * 转成用户送出礼物列表的JsonObject
     *
     * @return JsonObject
     */
    private JsonObject toSendJsonObject(GiftRecord record) {
        JsonObject jObject = new JsonObject();
        if (record.getXmanNick() != null) {
            jObject.addProperty("receiverNick", record.getXmanNick());
        } else {
            jObject.addProperty("receiverNick", UserService.getUserInfoNew(record.getUserId()).getNickName());
        }
        if (record.getXmanId() != null) {
            jObject.addProperty("receiverId", record.getXmanId());
        } else {
            jObject.addProperty("receiverId", record.getUserId());
        }
        jObject.addProperty("giftId", record.getGiftId());
        jObject.addProperty("giftName", record.getGiftName());
        jObject.addProperty("unit", record.getUnit());
        jObject.addProperty("sendPrice", record.getSendPrice());
        jObject.addProperty("rsvPrice", record.getRsvPrice());
        jObject.addProperty("count", record.getCount());
        jObject.addProperty("sendTime", record.getSendTime().getTime());

        //送礼类型 0:非库存礼物  1:库存礼物 2：钻石礼物
        int sendType = 0;
        if (record.getPrice() != null && record.getPrice() >= 100) {
            sendType = Integer.valueOf(String.valueOf(record.getPrice()).substring(2, 3));
        }
        jObject.addProperty("sendType", sendType);

        return jObject;
    }

    /**
     * 获取用户直播记录列表
     *
     * @param jsonObject 请求对象
     * @return json对象形式的返回结果
     */
    public JsonObject getUserLiveList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        // 验证参数
        int userId, pageIndex;
        long startTime, endTime;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "05160002", 1, Integer.MAX_VALUE);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, "05160008", 1, Integer.MAX_VALUE);
            startTime = CommonUtil.getJsonParamLong(jsonObject, "startTime", 0, "05160004", 1, Long.MAX_VALUE);
            endTime = CommonUtil.getJsonParamLong(jsonObject, "endTime", 0, "05160006", startTime, Long.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            ReturnResult<PageResult<HistActorLiveDTO>> resp = liveRecordService.pageActorLiveDTO(userId, new Date(startTime), new Date(endTime), pageIndex, 6);
            if (resp != null && "0".equals(resp.getCode())) {
                PageResult<HistActorLiveDTO> pageResult = resp.getData();
                List<HistActorLiveDTO> histActorLiveList = pageResult.getItems();
                JsonArray jRecordList = new JsonArray();
                if (histActorLiveList != null) {
                    for (HistActorLiveDTO histActorLiveDTO : histActorLiveList) {
                        JsonObject jObject = new JsonObject();
                        long liveEndTime = System.currentTimeMillis();
                        long liveStartTime = histActorLiveDTO.getStartTime().getTime();
                        jObject.addProperty("startTime", liveStartTime);
                        if (histActorLiveDTO.getEndTime() != null) {
                            liveEndTime = histActorLiveDTO.getEndTime().getTime();
                            jObject.addProperty("endTime", liveEndTime);
                        }
                        if (liveEndTime > liveStartTime) {
                            long duration = (liveEndTime - liveStartTime) / (60 * 1000);
                            jObject.addProperty("duration", duration);
                            jObject.addProperty("isValid", duration < 10 ? 0 : 1);
                        }
                        jRecordList.add(jObject);
                    }
                }

                result.add("recordList", jRecordList);
                result.addProperty("pageTotal", pageResult.getPageCount());
            }
        } catch (Exception e) {
            logger.error("liveRecordService.pageActorLiveDTO(" + userId + ", " + new Date(startTime) + ", " + new Date(endTime) + ", " + pageIndex + ", 6)", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }

        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 修改房间主题（10005055）
     *
     * @param request
     * @param checkTag
     * @return
     */
    public JsonObject changeRoomTheme(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId = 0;
        String roomTheme = null;

        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            roomTheme = CommonUtil.getJsonParamString(jsonObject, "roomTheme", "", null, 1, 40);
            if (GeneralService.hasSensitiveWords(userId, roomTheme)) {
                result.addProperty("TagCode", "05550003");
                return result;
            }
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        //特殊时期接口暂停使用 （官方号不限制）
        if (configService.getIsSpecialTime() && !ProfileServices.checkIsOfficial(userId)) {
            result.addProperty("TagCode", TagCodeEnum.FUNCTAG_UNUSED_EXCEPTION);
            return result;
        }

        Map<String, Object> map = new HashMap<>();
        map.put(RoomInfoKeys.ROOMTHEME.key(), roomTheme);
        if (actorService.updateRoomInfoById(userId, map) == 1) {
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);

            // 通知Node房间刷新缓存
            JsonObject nodeMsgJson = new JsonObject();
            try {
                JsonObject roomInfoJson = new JsonObject();
                roomInfoJson.addProperty("roomTheme", roomTheme);

                nodeMsgJson.addProperty("MsgTag", 10000010);
                nodeMsgJson.add("roomInfo", roomInfoJson);

                GeneralService.sendMsgToRoom(4, userId, 0, 0, nodeMsgJson);
            } catch (Exception e) {
                String errorMsg = String.format("发送房间通知失败：GeneralService.sendMsgToRoom(4, %s, 0, 0, %s)", userId, nodeMsgJson.toString());
                logger.error(errorMsg, e);
            }

            // 向房间发通知
            JsonObject msgJson = new JsonObject();
            try {
                msgJson.addProperty("MsgTag", 10010823);
                msgJson.addProperty("roomTheme", roomTheme);

                GeneralService.sendMsgToRoom(2, userId, 0, 0, msgJson);
            } catch (Exception e) {
                String errorMsg = String.format("发送房间通知失败：GeneralService.sendMsgToRoom(2, %s, 0, 0, %s)", userId, msgJson.toString());
                logger.error(errorMsg, e);
            }

        } else {
            result.addProperty("TagCode", "05550002");
        }

        return result;
    }

    /**
     * 设置隐身 （10005057）
     *
     * @param request
     * @param checkTag
     * @return
     */
    public JsonObject setUserStealth(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        int userId = 0;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
        }
        try {
            XmanService xmanService = (XmanService) MelotBeanFactory.getBean("xmanService");
            ResUserXman resUserXman = xmanService.getResUserXmanByUserId(userId);
            // 用户没有神秘人权限
            if (resUserXman == null) {
                result.addProperty("TagCode", "05570001");
                return result;
            }

            // 用户神秘人权限已过期
            if (resUserXman.getExpireTime().getTime() < new Date().getTime()) {
                result.addProperty("TagCode", "05570002");
                return result;
            }

            //设置、取消隐身
            if (!xmanService.updateStealth(userId)) {
                result.addProperty("TagCode", TagCodeEnum.OPETATE_STEALTH_FAIL);
                return result;
            }

            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取用户实名认证信息 （10006058）
     *
     * @param request
     * @param checkTag
     * @return
     */
    public JsonObject getCertificationInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        int userId = 0;
        int appId = 0;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
        }
        try {
            UserApplyActorDO applyActor = userApplyActorService.getUserApplyActorDO(userId).getData();
            UserVerifyDO userVerifyDO = userVerifyService.getUserVerifyDO(userId).getData();
            result.addProperty("userId", userId);

            if (appId == AppIdEnum.GAME) {
                if (applyActor == null) { //未提交审核
                    result.addProperty("status", -2);
                    result.addProperty("state", -2);
                    result.add("typeList", liveTypeSource.getAllHashLiveMap());
                } else {
                    result.addProperty("status", applyActor.getStatus());
                    result.addProperty("state", applyActor.getStatus());
                    if (userVerifyDO != null) {
                        result.addProperty("name", checkNullString(userVerifyDO.getCertName()));
                        result.addProperty("identity", checkNullString(userVerifyDO.getCertNo()));
                        result.addProperty("mobile", checkNullString(userVerifyDO.getVerifyMobile()));
                    }

                    if (!StringUtil.strIsNull(applyActor.getIntroduce())) {
                        String[] introduces = applyActor.getIntroduce().split(",");
                        result.addProperty("introduce", introduces[0]);  //主播提交后会默认在后面添加46栏目,即"xx,46" 需要去掉后面的
                    } else {
                        result.addProperty("introduce", "");
                    }

                    if (applyActor.getStatus() == -1) {
                        result.addProperty("refuseReason", checkNullString(applyActor.getCheckReason()));
                        result.add("typeList", liveTypeSource.getAllHashLiveMap()); //页面-1也需要返回typeList
                    }
                }
            } else {
                if (applyActor == null) { //未提交审核
                    result.addProperty("state", -2);
                } else {
                    result.addProperty("state", applyActor.getStatus());
                    if (userVerifyDO != null) {
                        result.addProperty("name", checkNullString(userVerifyDO.getCertName()));
                        result.addProperty("identity", checkNullString(userVerifyDO.getCertNo()));
                        result.addProperty("mobile", checkNullString(userVerifyDO.getVerifyMobile()));
                    }
                    if (applyActor.getStatus() == -1) {
                        result.addProperty("refuseReason", checkNullString(applyActor.getCheckReason()));
                    }
                }
            }

        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
            return result;
        }
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;

    }

    private static String checkNullString(String string) {
        return string == null ? "" : string;
    }

    /**
     * 获取用户粉丝数和关注数
     *
     * @param jsonObject
     * @return
     */
    public JsonObject getUserFansCountAndfollowCount(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        int userId = 0;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        result.addProperty("fansCount", UserRelationService.getFansCount(userId));
        result.addProperty("followsCount", UserRelationService.getFollowsCount(userId));

        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 获取配置的勋章的列表类型
     *
     * @param paramJsonObject 参数，传递的勋章的类型
     * @return
     * @author fenggaopan
     * @since 2015-12-03 15:13:00
     */
    public JsonObject getMedalListByType(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        try {
            UserMedalService userMedalService = (UserMedalService) MelotBeanFactory.getBean("userMedalService");
            List<ConfMedal> medals = userMedalService.getAllRechargeMedaList();
            JsonArray jsonArray = new JsonArray();
            JsonObject medalJson = null;
            JsonParser jsonParser = new JsonParser();
            for (ConfMedal medal : medals) {
                medalJson = new JsonObject();
                medalJson.addProperty("medalTitle", medal.getMedalTitle());
                medalJson.addProperty("medalLevel", medal.getMedalLevel() - 1);
                medalJson.add("medalMedalUrl", jsonParser.parse(medal.getMedalMedalUrl()).getAsJsonObject());
                medalJson.addProperty("minAmount", medal.getMinAmount());
                medalJson.addProperty("maxAmount", medal.getMaxAmount());
                jsonArray.add(medalJson);
            }
            result.add("medalInfos", jsonArray);
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
        } catch (Exception e) {
            logger.error(e);
        }
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 获取用户秀币消费列表(10006063)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getUserShowMoneyConsumeList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        int userId, start, offset;
        long startTime, endTime;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            startTime = CommonUtil.getJsonParamLong(jsonObject, "startTime", 0, "10006004", DateUtil.getDayBeginTime(System.currentTimeMillis()) - 180 * 24 * 3600 * 1000L, Long.MAX_VALUE);
            endTime = CommonUtil.getJsonParamLong(jsonObject, "endTime", 0, "10006006", startTime, Long.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, "10006008", 0, Integer.MAX_VALUE);
            offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 10, "10006008", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        JsonArray moneyList = new JsonArray();
        ShowMoneyService showmoneyService = (ShowMoneyService) MelotBeanFactory.getBean("showMoneyService");
        List<ShowMoneyHistory> list = new ArrayList<ShowMoneyHistory>();
        if (showmoneyService != null) {
            PageShowMoneyHistory pageShowMoneyHistory = showmoneyService.getUserShowMoneyConsume(userId, startTime, endTime, start, offset);
            if (pageShowMoneyHistory != null) {
                list = pageShowMoneyHistory.getPageList();
                result.addProperty("listCount", pageShowMoneyHistory.getPageCount());
                if (list != null && list.size() > 0) {

                    List<Integer> userIds = Lists.newArrayList();
                    for (ShowMoneyHistory hist : list) {
                        if (hist.getToUserId() != null) {
                            userIds.add(hist.getToUserId());
                        }
                    }

                    // 获取用户信息列表
                    KkUserService kkUserService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
                    List<UserProfile> userProfiles = kkUserService.getUserProfileBatch(userIds);
                    Map<Integer, UserProfile> userProfileMap = Maps.newHashMap();
                    if (userProfiles != null) {
                        for (UserProfile userProfile : userProfiles) {
                            userProfileMap.put(userProfile.getUserId(), userProfile);
                        }
                    }

                    for (ShowMoneyHistory hist : list) {
                        JsonObject moneyObj = new JsonObject();
                        if (hist.getConsumeAmount() != null) {
                            moneyObj.addProperty("amount", hist.getConsumeAmount());
                        }
                        if (hist.getToUserId() != null && hist.getToUserId() > 0) {

                            UserProfile userProfile = userProfileMap.get(hist.getToUserId());
                            if (userProfile != null && userProfile.getNickName() != null) {
                                moneyObj.addProperty("nickname", userProfile.getNickName());
                            }
                            moneyObj.addProperty("toUserId", hist.getToUserId());
                        }
                        if (hist.getXmanDesc() != null && hist.getXmanDesc().contains("dxmanId")) {
                            try {
                                JsonObject xmanDesc = new JsonParser().parse(hist.getXmanDesc()).getAsJsonObject();
                                if (xmanDesc.has("dxmanId") && xmanDesc.get("dxmanId") != null) {
                                    moneyObj.addProperty("toUserId", xmanDesc.get("dxmanId").getAsInt());
                                    moneyObj.addProperty("nickname", "神秘人" + xmanDesc.get("dxmanId").getAsInt() % 1000);
                                }
                            } catch (Exception e) {
                            }
                        }
                        if (hist.getTypeDesc() != null) {
                            moneyObj.addProperty("typeDesc", hist.getTypeDesc());
                        }
                        if (hist.getDtime() != null) {
                            moneyObj.addProperty("time", hist.getDtime().getTime());
                        }
                        moneyList.add(moneyObj);
                    }

                }
            }
        } else {
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }

        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        result.add("consumeList", moneyList);
        return result;
    }

    /**
     * 获取用户秀币收入列表(10006064)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getUserShowMoneyIncomeList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId, start, offset;
        long startTime, endTime;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            startTime = CommonUtil.getJsonParamLong(jsonObject, "startTime", 0, "10006004", DateUtil.getDayBeginTime(System.currentTimeMillis()) - 180 * 24 * 3600 * 1000L, Long.MAX_VALUE);
            endTime = CommonUtil.getJsonParamLong(jsonObject, "endTime", 0, "10006006", startTime, Long.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, "10006008", 0, Integer.MAX_VALUE);
            offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 10, "10006008", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        JsonArray moneyList = new JsonArray();
        List<ShowMoneyHistory> list = new ArrayList<ShowMoneyHistory>();
        ShowMoneyService showmoneyService = (ShowMoneyService) MelotBeanFactory.getBean("showMoneyService");
        if (showmoneyService != null) {
            PageShowMoneyHistory pageShowMoneyHistory = showmoneyService.getUserShowMoneyIncome(userId, startTime, endTime, start, offset);
            if (pageShowMoneyHistory != null) {
                list = pageShowMoneyHistory.getPageList();
                result.addProperty("listCount", pageShowMoneyHistory.getPageCount());
                if (list != null && list.size() > 0) {

                    List<Integer> userIds = Lists.newArrayList();
                    for (ShowMoneyHistory hist : list) {
                        if (hist.getUserId() != null) {
                            userIds.add(hist.getUserId());
                        }
                    }

                    // 获取用户信息列表
                    KkUserService kkUserService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
                    List<UserProfile> userProfiles = kkUserService.getUserProfileBatch(userIds);
                    Map<Integer, UserProfile> userProfileMap = Maps.newHashMap();
                    if (userProfiles != null) {
                        for (UserProfile userProfile : userProfiles) {
                            userProfileMap.put(userProfile.getUserId(), userProfile);
                        }
                    }

                    for (ShowMoneyHistory hist : list) {
                        JsonObject moneyObj = new JsonObject();
                        if (hist.getIncomeAmount() != null) {
                            moneyObj.addProperty("amount", hist.getIncomeAmount());
                        }
                        if (hist.getUserId() != null && hist.getUserId() > 0) {

                            UserProfile userProfile = userProfileMap.get(hist.getUserId());
                            if (userProfile != null && userProfile.getNickName() != null) {
                                moneyObj.addProperty("nickname", userProfile.getNickName());
                            }
                            moneyObj.addProperty("fromUserId", hist.getUserId());
                        }
                        if (hist.getXmanDesc() != null && hist.getXmanDesc().contains("sxmanId")) {
                            try {
                                JsonObject xmanDesc = new JsonParser().parse(hist.getXmanDesc()).getAsJsonObject();
                                if (xmanDesc.has("sxmanId") && xmanDesc.get("sxmanId") != null) {
                                    moneyObj.addProperty("fromUserId", xmanDesc.get("sxmanId").getAsInt());
                                    moneyObj.addProperty("nickname", "神秘人" + xmanDesc.get("sxmanId").getAsInt() % 1000);
                                }
                            } catch (Exception e) {
                            }
                        }
                        if (hist.getTypeDesc() != null) {
                            moneyObj.addProperty("typeDesc", hist.getTypeDesc());
                        }
                        if (hist.getDtime() != null) {
                            moneyObj.addProperty("time", hist.getDtime().getTime());
                        }
                        moneyList.add(moneyObj);
                    }
                }
            }
        } else {
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }

        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        result.add("incomeList", moneyList);
        return result;
    }

    /**
     * 获取用户现金购买道具列表(10006065)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getUserBuyPropertiesList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId, pageIndex;
        long startTime, endTime;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            startTime = CommonUtil.getJsonParamLong(jsonObject, "startTime", 0, "10006004", DateUtil.getDayBeginTime(System.currentTimeMillis()) - 180 * 24 * 3600 * 1000L, Long.MAX_VALUE);
            endTime = CommonUtil.getJsonParamLong(jsonObject, "endTime", 0, "10006006", startTime, Long.MAX_VALUE);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            int count = 0;
            JsonArray jRecordList = new JsonArray();
            Date startDate = new Date(startTime);
            Date endDate = new Date(endTime);
            Result<Map<String, Object>> resp = rechargeService.getUserBuyProductRecordCount(userId, startDate, endDate);
            if (resp != null && CommonStateCode.SUCCESS.equals(resp.getCode())) {
                Map<String, Object> map = resp.getData();
                if (map.get("count") != null) {
                    count = (int) map.get("count");
                }
                if (count > 0) {
                    Result<List<HistBuyProductRechargeDto>> histResp = rechargeService.getUserBuyProductRecords(userId, startDate, endDate, (pageIndex - 1) * 20, 20);
                    if (histResp != null && CommonStateCode.SUCCESS.equals(histResp.getCode())) {
                        List<HistBuyProductRechargeDto> histBuyProductRechargeList = histResp.getData();
                        if (!Collectionutils.isEmpty(histBuyProductRechargeList)) {
                            for (HistBuyProductRechargeDto histBuyProductRechargeDto : histBuyProductRechargeList) {
                                JsonObject jsonObj = new JsonObject();
                                if (histBuyProductRechargeDto.getAmount() != null) {
                                    jsonObj.addProperty("amount", histBuyProductRechargeDto.getAmount());
                                }
                                if (histBuyProductRechargeDto.getRechargeTime() != null) {
                                    jsonObj.addProperty("consumeTime", histBuyProductRechargeDto.getRechargeTime().getTime());
                                }
                                if (histBuyProductRechargeDto.getPaymentName() != null) {
                                    jsonObj.addProperty("paymentDesc", histBuyProductRechargeDto.getPaymentName());
                                }
                                if (histBuyProductRechargeDto.getPaymentMode() != null) {
                                    jsonObj.addProperty("paymentMode", histBuyProductRechargeDto.getPaymentMode());
                                }
                                if (histBuyProductRechargeDto.getType() != null) {
                                    jsonObj.addProperty("type", histBuyProductRechargeDto.getType());
                                }
                                if (histBuyProductRechargeDto.getDescribe() != null) {
                                    jsonObj.addProperty("typeDesc", histBuyProductRechargeDto.getDescribe());
                                }
                                if (histBuyProductRechargeDto.getMimoney() != null) {
                                    jsonObj.addProperty("showMoney", histBuyProductRechargeDto.getMimoney());
                                }
                                jRecordList.add(jsonObj);
                            }
                        }
                    }
                }
            }
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            result.addProperty("pageTotal", (int) Math.ceil((double) count / 20));
            result.add("recordList", jRecordList);
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            logger.error("ProfileFunctions.getUserBuyPropertiesList execute exception: ", e);
        }

        return result;
    }

    /**
     * 用户切换异常短信开关(10006066)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject userSmsSwitch(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId, state;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            state = CommonUtil.getJsonParamInt(jsonObject, "state", 0, null, 0, 1);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        int formalState = UserService.getUserSmsSwitch(userId);
        if (state == formalState) {
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        }

        if (UserService.changeUserSmsSwitch(userId, state)) {
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        }
        result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
        return result;
    }

    /**
     * 删除常用设备(50001028)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject delCommonDevice(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        String deviceUId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            deviceUId = CommonUtil.getJsonParamString(jsonObject, "deviceUId", "", "01280001", 1, 40);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        ProfileServices.delUserCommonDevice(userId, deviceUId);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 获取常用设备(50001029)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getCommonDeviceList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
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

        List<CommonDevice> commonDeviceList = ProfileServices.getUserCommonDevice(userId);
        if (commonDeviceList != null && commonDeviceList.size() > 0) {
            JsonArray jsonArray = new JsonArray();
            for (CommonDevice commonDevice : commonDeviceList) {
                JsonObject jsonObj = new JsonObject();
                jsonObj.addProperty("deviceUId", commonDevice.getDeviceUId());
                jsonObj.addProperty("deviceName", commonDevice.getDeviceName());
                jsonObj.addProperty("deviceModel", commonDevice.getDeviceModel());
                jsonArray.add(jsonObj);
            }
            result.add("deviceList", jsonArray);
        }

        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 获取用户游戏币消费列表(51010104)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getUserGameMoneyConsumeList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        int userId, start, offset;
        long startTime, endTime;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            startTime = CommonUtil.getJsonParamLong(jsonObject, "startTime", 0, "5101010401", DateUtil.getDayBeginTime(System.currentTimeMillis()) - 180 * 24 * 3600 * 1000L, Long.MAX_VALUE);
            endTime = CommonUtil.getJsonParamLong(jsonObject, "endTime", 0, "5101010402", startTime, Long.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, null, 0, Integer.MAX_VALUE);
            offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 10, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        JsonArray moneyList = new JsonArray();
        ShowMoneyService showmoneyService = (ShowMoneyService) MelotBeanFactory.getBean("showMoneyService");
        List<GameMoneyHistory> list = new ArrayList<GameMoneyHistory>();
        if (showmoneyService != null) {
            PageGameMoneyHistory pageGameMoneyHistory = showmoneyService.getUserGameMoneyConsume(userId, startTime, endTime, start, offset);
            if (pageGameMoneyHistory != null) {
                list = pageGameMoneyHistory.getPageList();
                result.addProperty("listCount", pageGameMoneyHistory.getPageCount());
                if (list != null && list.size() > 0) {

                    List<Integer> userIds = Lists.newArrayList();
                    for (GameMoneyHistory hist : list) {
                        if (hist.getToUserId() != null) {
                            userIds.add(hist.getToUserId());
                        }
                    }

                    // 获取用户信息列表
                    KkUserService kkUserService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
                    List<UserProfile> userProfiles = kkUserService.getUserProfileBatch(userIds);
                    Map<Integer, UserProfile> userProfileMap = Maps.newHashMap();
                    if (userProfiles != null) {
                        for (UserProfile userProfile : userProfiles) {
                            userProfileMap.put(userProfile.getUserId(), userProfile);
                        }
                    }

                    for (GameMoneyHistory hist : list) {
                        JsonObject moneyObj = new JsonObject();
                        if (hist.getConsumeAmount() != null) {
                            moneyObj.addProperty("amount", hist.getConsumeAmount());
                        }
                        if (hist.getToUserId() != null && hist.getToUserId() > 0) {

                            UserProfile userProfile = userProfileMap.get(hist.getToUserId());
                            if (userProfile != null && userProfile.getNickName() != null) {
                                moneyObj.addProperty("nickname", userProfile.getNickName());
                            }
                            moneyObj.addProperty("toUserId", hist.getToUserId());
                        }
                        if (hist.getXmanDesc() != null && hist.getXmanDesc().contains("dxmanId")) {
                            try {
                                JsonObject xmanDesc = new JsonParser().parse(hist.getXmanDesc()).getAsJsonObject();
                                if (xmanDesc.has("dxmanId") && xmanDesc.get("dxmanId") != null) {
                                    moneyObj.addProperty("toUserId", xmanDesc.get("dxmanId").getAsInt());
                                    moneyObj.addProperty("nickname", "神秘人" + xmanDesc.get("dxmanId").getAsInt() % 1000);
                                }
                            } catch (Exception e) {
                            }
                        }
                        if (hist.getTypeDesc() != null) {
                            moneyObj.addProperty("typeDesc", hist.getTypeDesc());
                        }
                        if (hist.getDtime() != null) {
                            moneyObj.addProperty("time", hist.getDtime().getTime());
                        }
                        moneyList.add(moneyObj);
                    }

                }
            }
        } else {
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }

        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        result.add("consumeList", moneyList);
        return result;
    }
    
    /**
     * 获取用户是否开启同城 (51010112)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getSameCityStatus(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

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

        boolean isOpen = true;
        Map<Integer, Boolean> resp = blacklistService.isSameCityBlacklist(Arrays.asList(userId));
        if (resp != null && resp.get(userId)) {
            isOpen = false;
        }
        
        result.addProperty("isOpen", isOpen);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 开启 / 关闭 同城 (51010113)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject operateSameCity(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId, operateType;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            operateType = CommonUtil.getJsonParamInt(jsonObject, "operateType", 0, "5101011301", 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        if (operateType == 0) {
            blacklistService.addSameCityBlacklist(String.valueOf(userId), new Date());
        } else {
            blacklistService.removeSameCityBlacklist(String.valueOf(userId));
        }
        
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

}

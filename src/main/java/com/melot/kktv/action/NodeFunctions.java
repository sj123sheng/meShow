package com.melot.kktv.action;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.api.menu.sdk.dao.domain.RoomExtraInfo;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.api.menu.sdk.service.RoomInfoService;
import com.melot.api.menu.sdk.utils.Collectionutils;
import com.melot.api.menu.sdk.utils.RoomInfoUtils;
import com.melot.common.driver.domain.CharmUserInfo;
import com.melot.common.driver.domain.WeekGiftRank;
import com.melot.common.driver.service.RoomExtendConfService;
import com.melot.common.driver.service.ShareService;
import com.melot.content.config.domain.ApplyContractInfo;
import com.melot.family.driver.domain.FamilyInfo;
import com.melot.family.driver.domain.DO.UserApplyActorDO;
import com.melot.family.driver.service.UserApplyActorService;
import com.melot.kk.config.api.domain.ConfSystemInfo;
import com.melot.kk.config.api.service.ConfigInfoService;
import com.melot.kkcore.actor.api.ActorInfo;
import com.melot.kkcore.actor.service.ActorService;
import com.melot.kkcore.user.api.UserInfoDetail;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kkcx.model.ActorLevel;
import com.melot.kkcx.model.RichLevel;
import com.melot.kkcx.model.UserProp;
import com.melot.kkcx.service.FamilyService;
import com.melot.kkcx.service.GeneralService;
import com.melot.kkcx.service.ProfileServices;
import com.melot.kkcx.service.RoomService;
import com.melot.kkcx.service.UserAssetServices;
import com.melot.kkcx.service.UserService;
import com.melot.kkcx.util.PropTypeEnum;
import com.melot.kkcx.util.ValidTypeEnum;
import com.melot.kktv.model.FansRankingItem;
import com.melot.kktv.model.MedalInfo;
import com.melot.kktv.redis.HotDataSource;
import com.melot.kktv.redis.MedalSource;
import com.melot.kktv.redis.QQVipSource;
import com.melot.kktv.service.UserRelationService;
import com.melot.kktv.util.AppChannelEnum;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.CityUtil;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.Constant;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.kktv.util.confdynamic.MedalConfig;
import com.melot.module.guard.driver.domain.GsonGuardObj;
import com.melot.module.guard.driver.service.GuardService;
import com.melot.module.medal.driver.domain.ConfMedal;
import com.melot.module.medal.driver.domain.GsonMedalObj;
import com.melot.module.medal.driver.domain.UserActivityMedal;
import com.melot.module.medal.driver.service.ActivityMedalService;
import com.melot.module.medal.driver.service.UserMedalService;
import com.melot.module.packagegift.driver.domain.ResUserXman;
import com.melot.module.packagegift.driver.domain.ResXman;
import com.melot.module.packagegift.driver.domain.UserChatBubbleDTO;
import com.melot.module.packagegift.driver.domain.XmanConf;
import com.melot.module.packagegift.driver.domain.XmanUserInfo;
import com.melot.module.packagegift.driver.service.ChatBubbleService;
import com.melot.module.packagegift.driver.service.XmanService;
import com.melot.module.packagegift.util.PropEnum;
import com.melot.room.gift.constant.ReturnResultCode;
import com.melot.room.gift.domain.ReturnResult;
import com.melot.room.gift.dto.ActorGiftDTO;
import com.melot.room.gift.service.ActorPersonalizedGiftService;
import com.melot.room.pendant.dto.UserPendantDTO;
import com.melot.room.pendant.service.PendantService;
import com.melot.sdk.core.util.MelotBeanFactory;

public class NodeFunctions {

    private static Logger logger = Logger.getLogger(NodeFunctions.class);

    @Resource
    UserApplyActorService userApplyActorService;

    @Resource
    ActorPersonalizedGiftService actorPersonalizedGiftService;
    
    @Resource
    ChatBubbleService chatBubbleService;

    /**
     * 获取用户信息(For Node)(10005044)
     *
     * @param jsonObject 请求对象
     * @return 登录结果
     */
    public JsonObject getUserInfoForNode(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        @SuppressWarnings("unused")
        int userId, roomId, appId, channel = 0;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "05010001", 1, Integer.MAX_VALUE);
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, null, 1, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 0, Integer.MAX_VALUE);
            channel = CommonUtil.getJsonParamInt(jsonObject, "c", AppChannelEnum.KK, null, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        Transaction t;

        try {
            XmanService xmanService = (XmanService) MelotBeanFactory.getBean("xmanService");
            // 判断用户ID是否为神秘人ID
            if (userId <= 1127828 && userId >= 1000578) {
                XmanConf xmanConf = null;
                t = Cat.getProducer().newTransaction("MCall", "xmanService.getXmanConf");
                try {
                    xmanConf = xmanService.getXmanConf(userId);
                    t.setStatus(Transaction.SUCCESS);
                } catch (Exception e) {
                    Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                    t.setStatus(e);
                } finally {
                    t.complete();
                }
                if (xmanConf != null) {
                    result.addProperty("userId", userId);
                    result.addProperty("isMys", 1);
                    result.addProperty("nickname", "神秘人" + (userId % 1000));
                    result.add("propList", new JsonArray());
                    result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                    return result;
                }
            }

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
            if (resUserXman == null || (resUserXman.getExpireTime().getTime() < new Date().getTime())) {
                result.addProperty("mysType", 0);
            } else {
                //判断该用户是否拥有神秘人标志，如果有则随机分配一个神秘人帐号信息给该用户，从而在返回用户信息中添加mysInfo字段，里面包含神们人ID的用户信息
                t = Cat.getProducer().newTransaction("MCall", "xmanService.getResXmanByUserId");
                ResXman resXman = null;
                try {
                    resXman = xmanService.getResXmanByUserId(userId);
                    t.setStatus(Transaction.SUCCESS);
                } catch (Exception e) {
                    Cat.getProducer().logError(e);
                    t.setStatus(e);
                } finally {
                    t.complete();
                }
                if (resXman != null && resXman.getMysType() == 2) {
                    result.addProperty("mysType", resXman.getMysType());
                    XmanUserInfo mysInfo = null;
                    t = Cat.getProducer().newTransaction("MCall", "xmanService.updateGetMysteryInfo");
                    try {
                        mysInfo = xmanService.updateGetMysteryInfo(userId);
                        t.setStatus(Transaction.SUCCESS);
                    } catch (Exception e) {
                        Cat.getProducer().logError(e);
                        t.setStatus(e);
                    } finally {
                        t.complete();
                    }
                    if (mysInfo != null) {
                        JsonObject mysInfoJson = new JsonObject();
                        mysInfoJson.addProperty("userId", mysInfo.getUserId());
                        mysInfoJson.addProperty("isMys", 1);
                        mysInfoJson.addProperty("nickname", mysInfo.getNickname());
                        mysInfoJson.addProperty("gender", mysInfo.getGender());
                        mysInfoJson.addProperty("actorTag", mysInfo.getActorTag());
                        mysInfoJson.addProperty("openPlatform", mysInfo.getOpenPlatform());
                        mysInfoJson.addProperty("richLevel", Constant.xman_richLevel);
                        mysInfoJson.addProperty("actorLevel", Constant.xman_actorLevel);
                        mysInfoJson.addProperty("starLevel", Constant.xman_starLevel);
                        if (mysInfo.getPortrait_path() != null) {
                            mysInfoJson.addProperty("portrait_path_original", ConfigHelper.getHttpdir() + mysInfo.getPortrait_path());
                            mysInfoJson.addProperty("portrait_path_1280", ConfigHelper.getHttpdir() + (mysInfo.getPortrait_path()) + "!1280");
                            mysInfoJson.addProperty("portrait_path_256", ConfigHelper.getHttpdir() + (mysInfo.getPortrait_path()) + "!256");
                            mysInfoJson.addProperty("portrait_path_128", ConfigHelper.getHttpdir() + (mysInfo.getPortrait_path()) + "!128");
                            mysInfoJson.addProperty("portrait_path_48", ConfigHelper.getHttpdir() + (mysInfo.getPortrait_path()) + "!48");

                            mysInfoJson.addProperty("portrait", mysInfo.getPortrait_path());
                        }

                        // 获取神秘人会员信息
                        JsonArray propArray = new JsonArray();
                        try {
                            List<Integer> propList = null;
                            t = Cat.getProducer().newTransaction("MCall", "UserService.getUserProps");
                            try {
                                propList = UserService.getUserProps(mysInfo.getUserId());
                                t.setStatus(Transaction.SUCCESS);
                            } catch (Exception e) {
                                Cat.getProducer().logError(e);
                                t.setStatus(e);
                            } finally {
                                t.complete();
                            }
                            if (propList != null) {
                                propArray = (JsonArray) new JsonParser().parse(new Gson().toJson(propList));
                            }
                        } catch (Exception e) {
                            logger.error("UserService.getUserProps(" + mysInfo.getUserId() + ") execute exception.", e);
                        }
                        mysInfoJson.add("propList", propArray);

                        // 获取神秘人勋章信息
                        mysInfoJson.add("userMedal", new JsonArray());

                        result.add("mysInfo", mysInfoJson);
                    }
                } else {
                    result.addProperty("mysType", 1);
                }
            }
        } catch (Exception e) {
            logger.error("Get user[" + userId + "] xman info execute exception.", e);
        }

        // 获取用户有效账号
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
                if (validVirtualId.get("idType").getAsInt() == 1) {
                    // 支持老版靓号
                    result.addProperty("luckyId", validVirtualId.get("id").getAsInt());
                }
                result.add("validId", validVirtualId);
            }
        } catch (Exception e) {
            logger.error("UserService.getUserProps(" + userId + ") execute exception.", e);
        }

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

        // 获取私有信息 获取redis活跃用户信息
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
                bValidHotData = true;
        }
        if (bValidHotData) {
            result.addProperty("userId", userId);

            ActorService actorService = (ActorService) MelotBeanFactory.getBean("actorService");
            com.melot.kkcore.actor.api.RoomInfo roomInfo = actorService.getRoomInfoById(userId);
            ActorInfo actorInfo = actorService.getActorInfoById(userId);
            if (roomInfo != null) {
                if (!StringUtil.strIsNull(roomInfo.getNoticeContent())) {
                    result.addProperty("noticeContent", roomInfo.getNoticeContent());
                }
                if (!StringUtil.strIsNull(roomInfo.getNoticeHref())) {
                    result.addProperty("noticeHref", roomInfo.getNoticeHref());
                }
            }
            if (actorInfo != null) {
                if (!StringUtil.strIsNull(actorInfo.getGreetMsg())) {
                    result.addProperty("welcomeMsg", actorInfo.getGreetMsg());
                }
                if (!StringUtil.strIsNull(actorInfo.getGreetMsgHref())) {
                    result.addProperty("welcomeMsgHref", actorInfo.getGreetMsgHref());
                }
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
        if (result.has("TagCode") && result.get("TagCode").getAsString().equals(TagCodeEnum.SUCCESS)) {
            // 返回公有属性
            result.addProperty("gender", userInfoDetail.getProfile().getGender());
            result.addProperty("actorTag", userInfoDetail.getProfile().getIsActor());
            result.addProperty("fansCount", UserRelationService.getFansCount(userId));
            result.addProperty("followCount", UserRelationService.getFollowsCount(userId));
            result.addProperty("openPlatform", userInfoDetail.getRegisterInfo().getOpenPlatform());
            result.addProperty("registerTime", userInfoDetail.getRegisterInfo().getRegisterTime());
            if (userInfoDetail.getProfile().getPhoneNum() != null) {
                result.addProperty("phone", userInfoDetail.getProfile().getPhoneNum());
            }
            if (userInfoDetail.getProfile().getPortrait() != null) {
                result.addProperty("portrait_path_original", ConfigHelper.getHttpdir() + userInfoDetail.getProfile().getPortrait());
                result.addProperty("portrait_path_1280", ConfigHelper.getHttpdir() + userInfoDetail.getProfile().getPortrait() + "!1280");
                result.addProperty("portrait_path_256", ConfigHelper.getHttpdir() + userInfoDetail.getProfile().getPortrait() + "!256");
                result.addProperty("portrait_path_128", ConfigHelper.getHttpdir() + userInfoDetail.getProfile().getPortrait() + "!128");
                result.addProperty("portrait_path_48", ConfigHelper.getHttpdir() + userInfoDetail.getProfile().getPortrait() + "!48");

                result.addProperty("portrait", userInfoDetail.getProfile().getPortrait());
            }
            if (userInfoDetail.getProfile().getNickName() != null) {
                String nickname = userInfoDetail.getProfile().getNickName();
                //非官方号需昵称过滤
                Integer adminType = ProfileServices.getUserAdminType(userId);
                if (adminType == null || adminType == -1) {
                    nickname = GeneralService.replaceNicknameSensitiveWords(nickname);
                }
                result.addProperty("nickname", nickname);
            }
            if (userInfoDetail.getProfile().getIdentifyPhone() != null) {
                result.addProperty("identifyPhone", userInfoDetail.getProfile().getIdentifyPhone());
            } else if (userInfoDetail.getProfile().getPhoneNum() != null) {
                result.addProperty("identifyPhone", userInfoDetail.getProfile().getPhoneNum());
            }
            result.addProperty("city", Math.abs(userInfoDetail.getRegisterInfo().getCityId()));
            String cityName = null;
            t = Cat.getProducer().newTransaction("MCall", "CityUtil.getCityName");
            try {
                cityName = CityUtil.getCityName(userInfoDetail.getRegisterInfo().getCityId());
                t.setStatus(Transaction.SUCCESS);
            } catch (Exception e) {
                Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                t.setStatus(e);
            } finally {
                t.complete();
            }
            // 北京 天津 上海 重庆
            if (cityName != null) {
                if (cityName.indexOf(" ") > 0) {
                    String province = cityName.split("\\s+")[0].trim();
                    if (province.equals("北京") || province.equals("天津") || province.equals("上海") || province.equals("重庆")) {
                        cityName = province;
                    } else {
                        cityName = cityName.split("\\s+")[1].trim();
                    }
                }
                result.addProperty("cityName", cityName);
            }

            Integer adminType = ProfileServices.getUserAdminType(userId);
            if (adminType != null && adminType != -1) {
                result.addProperty("siteAdmin", adminType);
            }

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
                result.addProperty("starLevel", UserService.getStarLevel(userId));
                t.setStatus(Transaction.SUCCESS);
            } catch (Exception e) {
                Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                t.setStatus(e);
            } finally {
                t.complete();
            }

            // 获取用户会员信息
            List<Integer> plist = new ArrayList<Integer>();
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
                    for (Integer p : propList) {
                        plist.add(p);
                    }
                }

                // 获取用户所有门票 TODO 暂时不用此功能 modified by songjm at 2016-05-19
//			    EntranceTicketService enterTicketService = (EntranceTicketService) MelotBeanFactory.getBean("entranceTicketService");
//			    PageEntranceTicket ticket = enterTicketService.getUserTicketListForApi(userId, null, null, null, null, 1, 0, 20);
//			    if (ticket != null && ticket.getEntranceTicket() != null && ticket.getEntranceTicket().size() > 0) {
//			        for (EntranceTicket ticketInfo : ticket.getEntranceTicket()) {
//			            plist.add(ticketInfo.getTicketId());
//			        }
//			    }
            } catch (Exception e) {
                logger.error("UserService.getUserProps(" + userId + ") execute exception.", e);
            }
            JsonArray propArray = (JsonArray) new JsonParser().parse(new Gson().toJson(plist));
            result.add("propList", propArray);

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
                result.add("userMedal", MedalSource.getUserMedalsAsJson(userId, null));
                t.setStatus(Transaction.SUCCESS);
            } catch (Exception e) {
                logger.error("MedalSource.getUserMedalsAsJson(" + userId + ") execute exception.", e);
                Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                t.setStatus(e);
            } finally {
                t.complete();
            }

            // 用户可佩戴的活动勋章
            try {
                ActivityMedalService activityMedalService = (ActivityMedalService) MelotBeanFactory.getBean("activityMedalService");

                //添加充值勋章信息,充值勋章所需要的字段都放到redis中，避免二次查询数据库
                Date now = new Date();
                UserMedalService userMedalService = (UserMedalService) MelotBeanFactory.getBean("userMedalService");
                List<ConfMedal> medals = new ArrayList<ConfMedal>();
                com.melot.module.medal.driver.domain.GsonMedalObj medal = null;
                t = Cat.getProducer().newTransaction("MCall", "userMedalService.getMedalsByUserId(");
                try {
                    medal = userMedalService.getMedalsByUserId(userId);
                    t.setStatus(Transaction.SUCCESS);
                } catch (Exception e) {
                    Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                    t.setStatus(e);
                } finally {
                    t.complete();
                }
                if (medal != null) {
                    ConfMedal confMedal = null;
                    //充值勋章点亮状态lightState为1显示
                    if ((medal.getEndTime() == 0 || medal.getEndTime() > now.getTime()) && medal.getLightState() == 1) {
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
                            confMedal.setMedalType(medalInfo.getMedalType());
                            confMedal.setMedalTitle(medalInfo.getMedalTitle());
                            confMedal.setMedalExpireTime(medal.getEndTime());
                            confMedal.setMedalMedalUrl(medalInfo.getMedalIcon());
                            medals.add(confMedal);
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
                        if (userActivityMedal.getEndTime() == null || userActivityMedal.getEndTime().getTime() > System.currentTimeMillis()) {
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
                }

                result.add("userMedalList", new JsonParser().parse(new Gson().toJson(medals)).getAsJsonArray());
            } catch (Exception e) {
                logger.error("Get user[" + userId + "] medal execute exception.", e);
            }

            //添加用户对应的房间的守护
            try {
                if (roomId > 0) {
                    GuardService guardService = (GuardService) MelotBeanFactory.getBean("guardService");
                    GsonGuardObj guard = null;
                    t = Cat.getProducer().newTransaction("MCall", "guardService.getUserGuardByActorId");
                    try {
                        guard = guardService.getUserGuardByActorId(roomId, userId);
                        t.setStatus(Transaction.SUCCESS);
                    } catch (Exception e) {
                        Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                        t.setStatus(e);
                    } finally {
                        t.complete();
                    }
                    if (guard != null) {
                        JsonParser parse = new JsonParser();
                        JsonObject json = new JsonObject();
                        json.addProperty("guardId", guard.getGuardId());
                        json.addProperty("guardName", guard.getGuardName());
                        json.addProperty("guardLevel", guard.getGuardLevel());
                        json.add("guardIcon", parse.parse(guard.getGuardIcon()).getAsJsonObject());
                        json.addProperty("guardCarId", guard.getGuardCarId());
                        if (guard.getGuardCarUrl() != null) {
                            json.add("guardCarUrl", parse.parse(guard.getGuardCarUrl()).getAsJsonObject());
                        }
                        json.addProperty("guardExpireTime", guard.getGuardExpireTime());
                        json.addProperty("nextGuardId", guard.getNextGuardId());
                        json.addProperty("nextGuardName", guard.getNextGuardName());
                        json.addProperty("beGuardTime", guard.getBeGuardTime());
                        if (guard.getGoldGuardIcon() != null) {
                            json.add("goldGuardIcon", parse.parse(guard.getGoldGuardIcon()).getAsJsonObject());
                        }
                        if (guard.getGoldGuardLevel() != null) {
                            json.addProperty("goldGuardLevel", guard.getGoldGuardLevel());
                        }
                        if (guard.getGoldGuardName() != null) {
                            json.addProperty("goldGuardName", guard.getGoldGuardName());
                        }
                        if (guard.getGuardYearIcon() != null) {
                            json.add("guardYearIcon", parse.parse(guard.getGuardYearIcon()).getAsJsonObject());
                        }
                        //增加守护座驾进场显示类型，1为显示
                        if (guardService.getUserGuardCarState(userId, roomId) == 1) {
                            result.addProperty("guardCarType", 1);
                        }
                        //添加用户信息
                        result.add("guardInfo", json);
                    }
                }
            } catch (Exception e) {
                logger.error("NodeFunctions.getUserInfoForNode execute GuardService.getUserGuardByActorId(" + roomId + ", " + userId + ") exception.", e);
            }

            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());

            List<UserProp> userPropList = new ArrayList<>();
            try {
                PendantService pendantService = (PendantService) MelotBeanFactory.getBean("pendantService");
                com.melot.room.pendant.domain.ReturnResult<UserPendantDTO> pendantDTOResult = pendantService.getUserPendant(userId);
                System.out.println("userPendantDTO = " + pendantDTOResult.toString());
                if ("0".equals(pendantDTOResult.getCode()) && pendantDTOResult.getData() != null) {
                    UserPendantDTO userPendantDTO = pendantDTOResult.getData();
                    UserProp userProp = new UserProp();
                    userProp.setId(userPendantDTO.getPendantId());
                    userProp.setType(PropTypeEnum.PENDANT.getCode());
                    userProp.setSubType(userPendantDTO.getPendantType());
                    userProp.setLevel(userPendantDTO.getLevel());
                    userProp.setIsLight(userPendantDTO.getUsed() ? 1 : 0);
                    userProp.setValidType(userPendantDTO.getValidType());
                    if (userPendantDTO.getValidTime() != null) {
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


            // 获取房间信息
            if (result.has("actorTag") && result.get("actorTag") != null) {
                int actorTag = result.get("actorTag").getAsInt();
                if (actorTag == 1) {
                    //获取实名认证状态
                    UserApplyActorDO applyActor = null;
                    try {
                        t = Cat.getProducer().newTransaction("MCall", "userApplyActorService.getUserApplyActorDO");
                        try {
                            applyActor = userApplyActorService.getUserApplyActorDO(userId).getData();
                            t.setStatus(Transaction.SUCCESS);
                        } catch (Exception e) {
                            Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                            t.setStatus(e);
                        } finally {
                            t.complete();
                        }
                    } catch (Exception e) {
                        logger.error("Fail to call ApplyActorService.getApplyActorByActorId ", e);
                    }
                    if (applyActor != null && applyActor.getStatus() != null) {
                        result.addProperty("identityStatus", applyActor.getStatus());
                    }
                    // 从PG读取
                    RoomInfo roomInfo = null;
                    t = Cat.getProducer().newTransaction("MCall", "RoomService.getRoomInfo");
                    try {
                        roomInfo = RoomService.getRoomInfoByIdInDb(userId);
                        t.setStatus(Transaction.SUCCESS);
                    } catch (Exception e) {
                        Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                        t.setStatus(e);
                    } finally {
                        t.complete();
                    }
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

                        String portrait = roomInfo.getPortrait();
                        if (roomInfo.getRoomSource() != null && roomInfo.getRoomSource() == 10 && !StringUtil.strIsNull(portrait)) {
                            result.addProperty("poster_path_original", ConfigHelper.getHttpdir() + portrait);
                            result.addProperty("poster_path_1280", ConfigHelper.getHttpdir() + portrait + "!1280");
                            result.addProperty("poster_path_290", ConfigHelper.getHttpdir() + portrait + "!290x164");
                            result.addProperty("poster_path_272", ConfigHelper.getHttpdir() + portrait + "!272");
                            result.addProperty("poster_path_128", ConfigHelper.getHttpdir() + portrait + "!128x96");
                            result.addProperty("poster_path_300", ConfigHelper.getHttpdir() + portrait + "!300");
                        } else {
                            String livePoster = roomInfo.getLivePoster();
                            String poster = roomInfo.getPoster();
                            if (roomInfo.getRoomSource() != null && roomInfo.getRoomSource().equals(2)) {
                                // 直播主播有动态海报,采用动态海报
                                livePoster = livePoster == null ? poster : livePoster;
                                poster = livePoster;
                            } else {
                                poster = poster == null ? livePoster : poster;
                                livePoster = null;
                            }
                            if (!StringUtil.strIsNull(livePoster)) {
                                result.addProperty("live_poster_original", ConfigHelper.getHttpdir() + livePoster);
                                result.addProperty("live_poster_1280", ConfigHelper.getHttpdir() + livePoster + "!1280");
                                result.addProperty("live_poster_290", ConfigHelper.getHttpdir() + livePoster + "!290x164");
                                result.addProperty("live_poster_272", ConfigHelper.getHttpdir() + livePoster + "!272");
                                result.addProperty("live_poster_128", ConfigHelper.getHttpdir() + livePoster + "!128x96");
                                result.addProperty("live_poster_300", ConfigHelper.getHttpdir() + livePoster + "!300");
                            }
                            if (!StringUtil.strIsNull(poster)) {
                                result.addProperty("poster_path_original", ConfigHelper.getHttpdir() + poster);
                                result.addProperty("poster_path_1280", ConfigHelper.getHttpdir() + poster + "!1280");
                                result.addProperty("poster_path_290", ConfigHelper.getHttpdir() + poster + "!290x164");
                                result.addProperty("poster_path_272", ConfigHelper.getHttpdir() + poster + "!272");
                                result.addProperty("poster_path_128", ConfigHelper.getHttpdir() + poster + "!128x96");
                                result.addProperty("poster_path_300", ConfigHelper.getHttpdir() + poster + "!300");
                            }
                        }

                        if (roomInfo.getRoomTheme() != null) {
                            result.addProperty("roomTheme", roomInfo.getRoomTheme());
                        }
                        if (roomInfo.getRoomMode() != null) {
                            // 房间模式 0:普通房 1:演艺房 2:游戏房 3:唱响家族房
                            result.addProperty("roomMode", roomInfo.getRoomMode());
                            if (roomInfo.getRoomMode().intValue() == 1) {
                                result.addProperty("videoLevel", 1);
                            }
                            if (roomInfo.getRoomMode().intValue() == 2) {
                                result.addProperty("userPart", "[107]");
                                result.addProperty("videoLevel", 1);
                            }
                        }

                        if (roomInfo.getType() != null) {
                            result.addProperty("roomType", roomInfo.getType());
                        } else {
                            result.addProperty("roomType", AppIdEnum.AMUSEMENT);
                        }
                        if (roomInfo.getRoomSource() != null) {
                            result.addProperty("roomSource", roomInfo.getRoomSource());
                        } else {
                            result.addProperty("roomSource", result.get("roomType").getAsInt());
                        }

                        int familyId = 0;
                        int actorRate = 0;
                        int familyRate = 0;
                        int officialRate = 40;
                        try {
                            ConfigInfoService configInfoService = (ConfigInfoService) MelotBeanFactory.getBean("configInfoService");
                            ConfSystemInfo confSystemInfo = configInfoService.getConfSystemInfoByKey("official_rate");
                            if (confSystemInfo != null && confSystemInfo.getcValue() != null) {
                                officialRate = Integer.valueOf(confSystemInfo.getcValue());
                            }
                        } catch (Exception e) {
                            logger.error("configInfoService.getConfSystemInfoByKey(actor_rate) execute exception", e);
                        }
                        if (roomInfo.getFamilyId() != null && roomInfo.getFamilyId().intValue() > 0) {
                            familyId = roomInfo.getFamilyId().intValue();
                            if (familyId == 12345) {
                                actorRate = 100 - officialRate;
                                familyRate = 0;
                            } else {
                                // 获取家族主播分成比例
                                Integer distributRate = null;
                                t = Cat.getProducer().newTransaction("MCall", "RoomService.getFamilyActorDistributRate");
                                try {
                                    distributRate = RoomService.getFamilyActorDistributRate(userId, familyId);
                                    t.setStatus(Transaction.SUCCESS);
                                } catch (Exception e) {
                                    Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                                    t.setStatus(e);
                                } finally {
                                    t.complete();
                                }
                                if (distributRate != null && distributRate.intValue() > 0) {
                                    actorRate = distributRate.intValue();
                                    familyRate = 100 - officialRate - actorRate;
                                }
                                // 家族房家族得60%
                                if (actorRate == 0 && roomInfo.getRoomMode() != null && roomInfo.getRoomMode().intValue() == 3) {
                                    familyRate = 100 - officialRate;
                                }
                            }
                        }
                        result.addProperty("actorRate", actorRate);
                        result.addProperty("familyRate", familyRate);
                        result.addProperty("familyId", familyId);
                    }
                }
            }
        }

        // 视频区域默认海报  WEB
        result.addProperty("videoWebPoster_480", ConfigHelper.getHttpdir() + "/picture/offical/wdisplayImage.png");
        // Andriod
        result.addProperty("videoAdrPoster_480", ConfigHelper.getHttpdir() + "/picture/offical/wdisplayImage.png");
        // Iphone
        result.addProperty("videoIphPoster_640", ConfigHelper.getHttpdir() + "/picture/offical/wdisplayImage.png");

        // 返回结果
        return result;
    }

    /**
     * 获取房间类型信息(For Node)(10001053)
     *
     * @param jsonObject 请求对象
     * @return 登录结果
     */
    public JsonObject getRoomTypeForNode(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
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

        int roomType = AppIdEnum.AMUSEMENT, roomSource = AppIdEnum.AMUSEMENT, roomId = userId, screenType = 1;
        int actorTag = 0;

        // 从PG读取
        RoomInfo roomInfo = null;
        Transaction t = Cat.getProducer().newTransaction("MCall", "RoomService.getRoomInfo");
        try {
            roomInfo = RoomService.getRoomInfoByIdInDb(userId);
            t.setStatus(Transaction.SUCCESS);
        } catch (Exception e) {
            Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
            t.setStatus(e);
        } finally {
            t.complete();
        }
        if (roomInfo != null) {
            if (roomInfo.getType() != null) {
                roomType = roomInfo.getType();
                actorTag = 1;

                if (roomInfo.getRoomSource() != null) {
                    roomSource = roomInfo.getRoomSource();
                } else {
                    roomSource = roomType;
                }
            }

            if (roomInfo.getRoomId() != null) {
                roomId = roomInfo.getRoomId();
            }

            if (roomInfo.getScreenType() != null) {
                screenType = roomInfo.getScreenType();
            }
        }

        result.addProperty("roomType", roomType);
        result.addProperty("screenType", screenType);
        result.addProperty("roomSource", roomSource);
        result.addProperty("roomId", roomId);
        result.addProperty("actorTag", actorTag);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);

        return result;
    }

    /**
     * 获取房间信息(For Node)(10001052)
     *
     * @param jsonObject 请求对象
     * @return 登录结果
     */
    public JsonObject getRoomInfoForNode(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        // 获取参数
        JsonElement userIdje = jsonObject.get("userId");

        // 验证参数
        Integer userId;
        int roomMode = 0;
        if (userIdje != null && !userIdje.isJsonNull() && userIdje.getAsInt() > 0) {
            // 验证数字
            try {
                userId = userIdje.getAsInt();
            } catch (Exception e) {
                JsonObject result = new JsonObject();
                result.addProperty("TagCode", "05010002");
                return result;
            }
        } else {
            JsonObject result = new JsonObject();
            result.addProperty("TagCode", "05010001");
            return result;
        }
        // 定义结果并组装json对象形式的返回结果
        JsonObject result = new JsonObject();

        Transaction t;

        //获取公有属性
        UserProfile userInfo = null;
        t = Cat.getProducer().newTransaction("MCall", "com.melot.kktv.service.UserService.getUserInfo");
        try {
            userInfo = com.melot.kktv.service.UserService.getUserInfoV2(userId);
            t.setStatus(Transaction.SUCCESS);
        } catch (Exception e) {
            Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
            t.setStatus(e);
        } finally {
            t.complete();
        }
        if (userInfo == null) {
            result.addProperty("TagCode", TagCodeEnum.USER_NOT_EXIST);
            return result;
        }

        // 获取redis活跃用户信息
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
                bValidHotData = true;
        }
        if (bValidHotData) {
            result.addProperty("userId", userId);

            ActorService actorService = (ActorService) MelotBeanFactory.getBean("actorService");
            com.melot.kkcore.actor.api.RoomInfo roomInfo = actorService.getRoomInfoById(userId);
            ActorInfo actorInfo = actorService.getActorInfoById(userId);
            if (roomInfo != null) {
                if (!StringUtil.strIsNull(roomInfo.getNoticeContent())) {
                    result.addProperty("noticeContent", roomInfo.getNoticeContent());
                }
                if (!StringUtil.strIsNull(roomInfo.getNoticeHref())) {
                    result.addProperty("noticeHref", roomInfo.getNoticeHref());
                }
            }
            if (actorInfo != null) {
                if (!StringUtil.strIsNull(actorInfo.getGreetMsg())) {
                    result.addProperty("welcomeMsg", actorInfo.getGreetMsg());
                }
                if (!StringUtil.strIsNull(actorInfo.getGreetMsgHref())) {
                    result.addProperty("welcomeMsgHref", actorInfo.getGreetMsgHref());
                }
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

        if (result.has("TagCode") && result.get("TagCode").getAsString().equals(TagCodeEnum.SUCCESS)) {

            Integer actorTag = userInfo.getIsActor();
            result.addProperty("actorTag", userInfo.getIsActor());

            t = Cat.getProducer().newTransaction("MCall", "com.melot.kktv.service.UserRelationService.getFansCount");
            try {
                result.addProperty("fansCount", com.melot.kktv.service.UserRelationService.getFansCount(userId));
                t.setStatus(Transaction.SUCCESS);
            } catch (Exception e) {
                Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                t.setStatus(e);
            } finally {
                t.complete();
            }

            t = Cat.getProducer().newTransaction("MCall", "com.melot.kktv.service.UserRelationService.getFollowsCount");
            try {
                result.addProperty("followCount", com.melot.kktv.service.UserRelationService.getFollowsCount(userId));
                t.setStatus(Transaction.SUCCESS);
            } catch (Exception e) {
                Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                t.setStatus(e);
            } finally {
                t.complete();
            }

            // 未申请
            int applyStatus = -2;
            // 不是试播
            int tryLiving = 0;
            // 是否可以直播
            int canLive = 0;
            // 不允许直播原因 默认 非主播
            int denyReason = 1;

            int liveType = 0;
            int videoLevel = 0;
            int roomSource = AppIdEnum.AMUSEMENT, roomType = AppIdEnum.AMUSEMENT, roomId = userId;

            // 从PG读取
            RoomInfo roomInfo = null;
            t = Cat.getProducer().newTransaction("MCall", "RoomService.getRoomInfo");
            try {
                roomInfo = RoomInfoUtils.getInitialRoomInfoByActorid(userId);
                t.setStatus(Transaction.SUCCESS);
            } catch (Exception e) {
                Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                t.setStatus(e);
            } finally {
                t.complete();
            }
            if (roomInfo != null) {
                // 轮播房添加roomId字段，非正在直播默认等于userId
//		    	if (roomInfo.getRoomId() != null) {
//		    		roomId = roomInfo.getRoomId();
//				}
                if (roomInfo.getScreenType() != null) {
                    result.addProperty("screenType", roomInfo.getScreenType());
                } else {
                    result.addProperty("screenType", 1);
                }

                if (roomInfo.getLiveType() != null) {
                    liveType = roomInfo.getLiveType().intValue();
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

                String portrait = roomInfo.getPortrait();
                if (!StringUtil.strIsNull(portrait)) {
                    result.addProperty("portrait_path_original", ConfigHelper.getHttpdir() + portrait);
                    result.addProperty("portrait_path_48", ConfigHelper.getHttpdir() + portrait + "!48");
                    result.addProperty("portrait_path_128", ConfigHelper.getHttpdir() + portrait + "!128");
                    result.addProperty("portrait_path_256", ConfigHelper.getHttpdir() + portrait + "!256");
                    result.addProperty("portrait_path_1280", ConfigHelper.getHttpdir() + portrait + "!1280");
                }
                if (roomInfo.getRoomSource() != null && roomInfo.getRoomSource() == 10 && !StringUtil.strIsNull(portrait)) {
                    result.addProperty("poster_path_original", ConfigHelper.getHttpdir() + portrait);
                    result.addProperty("poster_path_1280", ConfigHelper.getHttpdir() + portrait + "!1280");
                    result.addProperty("poster_path_290", ConfigHelper.getHttpdir() + portrait + "!290x164");
                    result.addProperty("poster_path_272", ConfigHelper.getHttpdir() + portrait + "!272");
                    result.addProperty("poster_path_128", ConfigHelper.getHttpdir() + portrait + "!128x96");
                    result.addProperty("poster_path_300", ConfigHelper.getHttpdir() + portrait + "!300");
                } else {
                    String livePoster = roomInfo.getLivePoster();
                    String poster = roomInfo.getPoster();
                    if (roomInfo.getRoomSource() != null && roomInfo.getRoomSource().equals(2)) {
                        // 直播主播有动态海报,采用动态海报
                        livePoster = livePoster == null ? poster : livePoster;
                        poster = livePoster;
                    } else {
                        poster = poster == null ? livePoster : poster;
                        livePoster = null;
                    }
                    if (!StringUtil.strIsNull(livePoster)) {
                        result.addProperty("live_poster_original", ConfigHelper.getHttpdir() + livePoster);
                        result.addProperty("live_poster_1280", ConfigHelper.getHttpdir() + livePoster + "!1280");
                        result.addProperty("live_poster_290", ConfigHelper.getHttpdir() + livePoster + "!290x164");
                        result.addProperty("live_poster_272", ConfigHelper.getHttpdir() + livePoster + "!272");
                        result.addProperty("live_poster_128", ConfigHelper.getHttpdir() + livePoster + "!128x96");
                        result.addProperty("live_poster_300", ConfigHelper.getHttpdir() + livePoster + "!300");
                    }
                    if (!StringUtil.strIsNull(poster)) {
                        result.addProperty("poster_path_original", ConfigHelper.getHttpdir() + poster);
                        result.addProperty("poster_path_1280", ConfigHelper.getHttpdir() + poster + "!1280");
                        result.addProperty("poster_path_290", ConfigHelper.getHttpdir() + poster + "!290x164");
                        result.addProperty("poster_path_272", ConfigHelper.getHttpdir() + poster + "!272");
                        result.addProperty("poster_path_128", ConfigHelper.getHttpdir() + poster + "!128x96");
                        result.addProperty("poster_path_300", ConfigHelper.getHttpdir() + poster + "!300");
                    }
                }

                if (roomInfo.getRoomTheme() != null) {
                    t = Cat.getProducer().newTransaction("MCall", "GeneralService.replaceSensitiveWords");
                    try {
                        result.addProperty("roomTheme", GeneralService.replaceSensitiveWords(roomInfo.getActorId(), roomInfo.getRoomTheme()));
                        t.setStatus(Transaction.SUCCESS);
                    } catch (Exception e) {
                        Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                        t.setStatus(e);
                    } finally {
                        t.complete();
                    }
                }

                if (roomInfo.getRoomMode() != null) {
                    // 房间模式 0:普通房 1:演艺房 2:游戏房
                    roomMode = roomInfo.getRoomMode().intValue();
                    if (roomMode == 3) {
                        Map<String, String> newHotData = new HashMap<String, String>();
                        newHotData.put("isFamilyRoom", "1");

                        t = Cat.getProducer().newTransaction("MRedis", "HotDataSource.setHotData(userId, newHotData, expirtTime)");
                        try {
                            HotDataSource.setHotData(String.valueOf(userId), newHotData, ConfigHelper.getRedisUserDataExpireTime());
                            t.setStatus(Transaction.SUCCESS);
                        } catch (Exception e) {
                            Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                            t.setStatus(e);
                        } finally {
                            t.complete();
                        }
                    }
                    if (roomMode == 1) {
                        videoLevel = 1;
                    } else if (roomMode == 2) {
                        result.addProperty("userPart", "[107]");
                        videoLevel = 1;
                    } else {
                        videoLevel = 0;
                    }
                }

                if (roomInfo.getType() != null) {
                    roomType = roomInfo.getType().intValue();
                    if (roomInfo.getType().intValue() == 10) {
                        // 棒主播
                        int botCount = 10;
                        if (roomInfo.getIsGood() != null) {
                            // 普主播
                            if (roomInfo.getIsGood() == -1) botCount = 12;
                            // 良主播
                            if (roomInfo.getIsGood() == 0) botCount = 14;
                            // 优主播
                            if (roomInfo.getIsGood() == 1) botCount = 22;
                        }
                        result.addProperty("botCount", botCount);
                    }

                    if (roomInfo.getRoomSource() != null) {
                        roomSource = roomInfo.getRoomSource();
                    } else {
                        roomSource = roomInfo.getType().intValue();
                    }
                }

                int familyId = 0;
                int actorRate = 0;
                int familyRate = 0;
                int officialRate = 40;
                try {
                    ConfigInfoService configInfoService = (ConfigInfoService) MelotBeanFactory.getBean("configInfoService");
                    ConfSystemInfo confSystemInfo = configInfoService.getConfSystemInfoByKey("official_rate");
                    if (confSystemInfo != null && confSystemInfo.getcValue() != null) {
                        officialRate = Integer.valueOf(confSystemInfo.getcValue());
                    }
                } catch (Exception e) {
                    logger.error("configInfoService.getConfSystemInfoByKey(actor_rate) execute exception", e);
                }

                if (roomType == 12) {
                    actorRate = 100 - officialRate;
                    familyId = 12345;
                } else {
                    ApplyContractInfo applyContractInfo = null;
                    t = Cat.getProducer().newTransaction("MCall", "RoomService.getApplyContractInfoByUserId");
                    try {
                        applyContractInfo = RoomService.getApplyContractInfoByUserId(userId);
                        t.setStatus(Transaction.SUCCESS);
                    } catch (Exception e) {
                        Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                        t.setStatus(e);
                    } finally {
                        t.complete();
                    }
                    if (applyContractInfo != null && applyContractInfo.getFamilyId() != null &&
                            applyContractInfo.getFamilyId().intValue() > 0 && roomType == AppIdEnum.AMUSEMENT) {
                        familyId = applyContractInfo.getFamilyId().intValue();

                        if (applyContractInfo.getDistributRate() != null && applyContractInfo.getDistributRate().intValue() > 0) {
                            // 获取家族主播分成比例
                            actorRate = applyContractInfo.getDistributRate().intValue();
                        }

                        // 家族房家族得60%
                        if (actorRate == 0 && roomInfo.getRoomMode() != null && roomInfo.getRoomMode().intValue() == 3) {
                            familyRate = 100 - officialRate;
                        } else {
                            // 判断家族是否在本APP上开通，如未开通则家族分成为 0
                            FamilyInfo familyInfo = null;
                            t = Cat.getProducer().newTransaction("MCall", "FamilyService.getFamilyInfoByFamilyId");
                            try {
                                familyInfo = FamilyService.getFamilyInfoByFamilyId(familyId, roomType);
                                t.setStatus(Transaction.SUCCESS);
                            } catch (Exception e) {
                                Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                                t.setStatus(e);
                            } finally {
                                t.complete();
                            }
                            if (familyInfo != null && familyInfo.getAssess() != null && familyInfo.getAssess() == 1) {
                                familyRate = 100 - officialRate - actorRate;
                            }
                        }
                    }
                }
                result.addProperty("actorRate", actorRate);
                result.addProperty("familyRate", familyRate);
                result.addProperty("familyId", roomInfo.getFamilyId());
            }

            // 获取主播申请信息
            UserApplyActorDO applyInfo = null;
            t = Cat.getProducer().newTransaction("MCall", "userApplyActorService.getActorApplyInfo");
            try {
                applyInfo = userApplyActorService.getUserApplyActorDO(userId).getData();
                t.setStatus(Transaction.SUCCESS);
            } catch (Exception e) {
                Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                t.setStatus(e);
            } finally {
                t.complete();
            }
            if (applyInfo != null && applyInfo.getStatus() != null) {
                applyStatus = applyInfo.getStatus().intValue();
                if (applyInfo.getStatus().intValue() == 5) {
                    // 可以试播
                    tryLiving = 1;
                    // 可以直播
                    canLive = 1;
                    denyReason = 0;
                }
                if (actorTag != null && actorTag.intValue() == 1) {
                    if (applyInfo.getStatus().intValue() == 1 || applyInfo.getStatus().intValue() == 14) {
                        // 可以直播
                        canLive = 1;
                        denyReason = 0;
                    }
                }
            }

            // 1 官方家族主播 2 非主播
            result.addProperty("denyReason", denyReason);

            result.addProperty("applyStatus", applyStatus);
            result.addProperty("tryLiving", tryLiving);
            result.addProperty("canLive", canLive);

            result.addProperty("roomMode", roomMode);
            result.addProperty("liveType", liveType);
            result.addProperty("videoLevel", videoLevel);
            result.addProperty("roomSource", roomSource);
            result.addProperty("roomId", roomId);
            result.addProperty("roomType", roomType);
        }

        // ly[0:普通房， 1 ：  家族房]
        if (hotData != null && hotData.containsKey("isFamilyRoom") && !StringUtil.strIsNull(hotData.get("isFamilyRoom"))
                && Integer.parseInt(hotData.get("isFamilyRoom")) == 1) {
            result.addProperty("isFamilyRoom", 1);
        } else {
            result.addProperty("isFamilyRoom", 0);
        }

        // 添加粉丝周榜和月榜返回
        List<FansRankingItem> fansRankList = null;
        t = Cat.getProducer().newTransaction("MCall", "RoomService.getRoomFansRankList(userId, 1)");
        try {
            fansRankList = RoomService.getRoomFansRankList(userId, 1);
            t.setStatus(Transaction.SUCCESS);
        } catch (Exception e) {
            Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
            t.setStatus(e);
        } finally {
            t.complete();
        }
        if (fansRankList != null && fansRankList.size() > 0) {
            result.addProperty("weekChampionUserId", fansRankList.get(0).getUserId());
        }

        t = Cat.getProducer().newTransaction("MCall", "RoomService.getRoomFansRankList(userId, 2)");
        try {
            fansRankList = RoomService.getRoomFansRankList(userId, 2);
            t.setStatus(Transaction.SUCCESS);
        } catch (Exception e) {
            Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
            t.setStatus(e);
        } finally {
            t.complete();
        }
        if (fansRankList != null && fansRankList.size() > 0) {
            result.addProperty("monthChampionUserId", fansRankList.get(0).getUserId());
        }

        //主播个性礼物
        t = Cat.getProducer().newTransaction("MCall", "ActorGiftService.getActorPersonalizedGiftList");
        try {
            ReturnResult<List<ActorGiftDTO>> resp = actorPersonalizedGiftService.getActorPersonalizedGiftList(userId);
            if (resp != null && ReturnResultCode.SUCCESS.getCode().equals(resp.getCode())) {
                List<ActorGiftDTO> actorGiftList = resp.getData();
                if (actorGiftList != null && !actorGiftList.isEmpty()) {
                    ArrayList<Integer> giftList = new ArrayList<Integer>();
                    for (ActorGiftDTO actorGift : actorGiftList) {
                        giftList.add(actorGift.getGiftId());
                    }
                    result.add("actorGiftList", new Gson().toJsonTree(giftList).getAsJsonArray());
                }
            }
            t.setStatus(Transaction.SUCCESS);
        } catch (Exception e) {
            logger.error("ActorGiftService.getActorPersonalizedGiftList(" + userId + ") execute exception.", e);
            Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
            t.setStatus(e);
        } finally {
            t.complete();
        }

        // 获取主播房间机器人参数配置信息 @modified by songjm at 2016-10-14
        try {
            RoomInfoService roomInfoServie = MelotBeanFactory.getBean("roomInfoService", RoomInfoService.class);
            RoomExtraInfo extraInfo = null;
            t = Cat.getProducer().newTransaction("MCall", "roomInfoServie.getRoomExtraInfo");
            try {
                extraInfo = roomInfoServie.getRoomExtraInfo(userId, "robot_multi");
                t.setStatus(Transaction.SUCCESS);
            } catch (Exception e) {
                Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
                t.setStatus(e);
            } finally {
                t.complete();
            }
            if (extraInfo != null) {
                result.addProperty("robot_multi", Integer.valueOf(extraInfo.getExtraValue()));
            }
        } catch (Exception e) {
            logger.error("RoomInfoService.getRoomExtraInfo(" + userId + ", robot_multi) execute exception.", e);
        }

        // 返回结果
        return result;
    }

    /**
     * 根据userId获取用户token(仅供Node使用,密码secretKey)(10001026)
     *
     * @param jsonObject
     * @return
     */
    public JsonObject getUserToken(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        int userId, appId;
        String secretKey = null;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "01260001", 1, Integer.MAX_VALUE);
            secretKey = CommonUtil.getJsonParamString(jsonObject, "secretKey", null, "01260003", 1, Integer.MAX_VALUE);
            if (!secretKey.equals(Constant.secret_key_of_access_token)) {
                result.addProperty("TagCode", "01260005");
                return result;
            }
            appId = CommonUtil.getJsonParamInt(jsonObject, "appId", AppIdEnum.AMUSEMENT, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        String token = com.melot.kktv.service.UserService.getUserToken(userId, appId);
        if (token != null) {
            result.addProperty("token", token);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } else {
            result.addProperty("TagCode", "01260006");
        }

        return result;
    }

    /**
     * 根据userId、token验证用户(For Node)(10001030)
     *
     * @param jsonObject
     * @return
     */
    public JsonObject checkUserToken(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        if (checkTag) {
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } else {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
        }

        return result;
    }

    /**
     * 获取上周周星礼物第一名排行榜(10001044)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getLastWeekGiftRanking(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        JsonArray jsonArray = new JsonArray();

        try {
            RoomExtendConfService roomExtendConfService = (RoomExtendConfService) MelotBeanFactory.getBean("roomExtendConfService");
            List<WeekGiftRank> weekGiftRankList = roomExtendConfService.getLastWeekGiftRanking();
            if (!weekGiftRankList.isEmpty()) {
                for (WeekGiftRank weekGiftRank : weekGiftRankList) {
                    JsonObject json = new JsonObject();
                    json.addProperty("userId", weekGiftRank.getUserId());
                    json.addProperty("count", weekGiftRank.getCount());
                    json.addProperty("giftId", weekGiftRank.getGiftId());
                    jsonArray.add(json);
                }
            }
        } catch (Exception e) {
            logger.error("call roomExtendConfService getLastWeekGiftRanking catched exception", e);
        }

        result.add("rankList", jsonArray);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 获取本周周星礼物(10001045)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getCurrentWeekGift(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        try {
            RoomExtendConfService roomExtendConfService = (RoomExtendConfService) MelotBeanFactory.getBean("roomExtendConfService");
            List<Integer> giftIds = roomExtendConfService.getCurrentWeekGift();
            if (!giftIds.isEmpty()) {
                result.add("giftIds", new JsonParser().parse(giftIds.toString()).getAsJsonArray());
            }
        } catch (Exception e) {
            logger.error("call roomExtendConfService getCurrentWeekGift catched exception", e);
        }

        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 获取房间扩展配置信息(50001030)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getRoomExtendConfInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        int roomId;
        try {
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, TagCodeEnum.ROOMID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            ShareService shareService = MelotBeanFactory.getBean("shareService", ShareService.class);
            result.addProperty("isFanFeedBack", "1".equals(shareService.getFanFeedBackInfo(roomId).get("isOpen")) ? 1 : 0);
        } catch (Exception e) {
            logger.error("call ShareService getFanFeedBackInfo catched exception, roomId : " + roomId, e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }

        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 获取房间魅力榜(50001032)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getRoomCharmList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        int roomId, offset;
        try {
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, TagCodeEnum.ROOMID_MISSING, 1, Integer.MAX_VALUE);
            offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 10, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        JsonArray jsonArray = new JsonArray();
        try {
            RoomExtendConfService roomExtendConfService = (RoomExtendConfService) MelotBeanFactory.getBean("roomExtendConfService");
            List<CharmUserInfo> charmList = roomExtendConfService.getRoomCharmList(roomId, offset);
            if (!charmList.isEmpty()) {
                for (CharmUserInfo charmUserInfo : charmList) {
                    JsonObject jsonObj = new JsonObject();
                    int userId = charmUserInfo.getUserId();
                    jsonObj.addProperty("userId", userId);
                    jsonObj.addProperty("nickname", charmUserInfo.getNickname());
                    jsonObj.addProperty("gender", charmUserInfo.getGender());
                    jsonObj.addProperty("charmValue", charmUserInfo.getCharmValue());
                    if (charmUserInfo.getPortrait() != null) {
                        jsonObj.addProperty("portrait", ConfigHelper.getHttpdir() + charmUserInfo.getPortrait());
                    }
                    jsonObj.addProperty("actorLevel", charmUserInfo.getActorLevel());
                    jsonObj.addProperty("richLevel", charmUserInfo.getRichLevel());

                    // 添加勋章列表
                    // 用户可佩戴的活动勋章
                    try {
                        UserMedalService userMedalService = (UserMedalService) MelotBeanFactory.getBean("userMedalService");
                        ActivityMedalService activityMedalService = (ActivityMedalService) MelotBeanFactory.getBean("activityMedalService");

                        //添加充值勋章信息,充值勋章所需要的字段都放到redis中，避免二次查询数据库
                        Date now = new Date();
                        List<ConfMedal> medals = new ArrayList<>();
                        GsonMedalObj medal = userMedalService.getMedalsByUserId(userId);
                        if (medal != null) {
                            ConfMedal confMedal = null;
                            //充值勋章点亮状态lightState为1显示
                            if ((medal.getEndTime() == 0 || medal.getEndTime() > now.getTime()) && medal.getLightState() == 1) {
                                MedalInfo medalInfo = null;
                                medalInfo = MedalConfig.getMedal(medal.getMedalId());
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
                                    confMedal.setMedalType(medalInfo.getMedalType());
                                    confMedal.setMedalTitle(medalInfo.getMedalTitle());
                                    confMedal.setMedalExpireTime(medal.getEndTime());
                                    confMedal.setMedalMedalUrl(medalInfo.getMedalIcon());
                                    medals.add(confMedal);
                                }
                            }
                        }

                        List<UserActivityMedal> wearList = null;
                        wearList = activityMedalService.getUserWearMedals(userId);
                        if (wearList != null && !wearList.isEmpty()) {
                            for (UserActivityMedal userActivityMedal : wearList) {
                                if (userActivityMedal.getEndTime() == null || userActivityMedal.getEndTime().getTime() > System.currentTimeMillis()) {
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
                        }

                        jsonObj.add("userMedalList", new JsonParser().parse(new Gson().toJson(medals)).getAsJsonArray());
                    } catch (Exception e) {
                        logger.error("Get user[" + userId + "] medal execute exception.", e);
                    }

                    jsonArray.add(jsonObj);
                }
            }
        } catch (Exception e) {
            logger.error("call roomExtendConfService getRoomCharmList catched exception, roomId : " + roomId, e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }

        result.add("charmList", jsonArray);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
}
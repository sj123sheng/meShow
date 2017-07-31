package com.melot.kkcx.functions;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.content.config.apply.service.ApplyActorService;
import com.melot.content.config.domain.ApplyActor;
import com.melot.family.driver.domain.FamilyInfo;
import com.melot.feedback.driver.service.FeedbackService;
import com.melot.kkcore.user.api.ProfileKeys;
import com.melot.kkcore.user.api.UserInfoDetail;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcore.user.api.UserRegistry;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kkcx.model.ActorLevel;
import com.melot.kkcx.model.CommonDevice;
import com.melot.kkcx.model.LotteryPrize;
import com.melot.kkcx.model.LotteryPrizeList;
import com.melot.kkcx.model.RichLevel;
import com.melot.kkcx.model.StarInfo;
import com.melot.kkcx.service.FamilyService;
import com.melot.kkcx.service.GeneralService;
import com.melot.kkcx.service.MessageBoxServices;
import com.melot.kkcx.service.ProfileServices;
import com.melot.kkcx.service.UserAssetServices;
import com.melot.kkcx.service.UserService;
import com.melot.kkgame.domain.GameUserInfo;
import com.melot.kkgame.redis.LiveTypeSource;
import com.melot.kktv.domain.mongo.MongoRoom;
import com.melot.kktv.lottery.arithmetic.LotteryArithmetic;
import com.melot.kktv.lottery.arithmetic.LotteryArithmeticCache;
import com.melot.kktv.model.BuyProperties;
import com.melot.kktv.model.ConsumerRecord;
import com.melot.kktv.model.Family;
import com.melot.kktv.model.GiftRecord;
import com.melot.kktv.model.Honor;
import com.melot.kktv.model.LiveRecord;
import com.melot.kktv.model.MedalInfo;
import com.melot.kktv.model.Task;
import com.melot.kktv.model.WinLotteryRecord;
import com.melot.kktv.redis.HotDataSource;
import com.melot.kktv.redis.MedalSource;
import com.melot.kktv.redis.QQVipSource;
import com.melot.kktv.service.LiveVideoService;
import com.melot.kktv.service.NewsService;
import com.melot.kktv.service.UserRelationService;
import com.melot.kktv.util.AppChannelEnum;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.CityUtil;
import com.melot.kktv.util.CollectionEnum;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.DateUtil;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.kktv.util.TextFilter;
import com.melot.kktv.util.confdynamic.MedalConfig;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;
import com.melot.kktv.util.mongodb.CommonDB;
import com.melot.module.api.exceptions.MelotModuleException;
import com.melot.module.medal.driver.domain.ConfMedal;
import com.melot.module.medal.driver.domain.UserActivityMedal;
import com.melot.module.medal.driver.service.ActivityMedalService;
import com.melot.module.medal.driver.service.UserMedalService;
import com.melot.module.packagegift.driver.domain.ResUserXman;
import com.melot.module.packagegift.driver.domain.ResXman;
import com.melot.module.packagegift.driver.service.XmanService;
import com.melot.module.task.driver.domain.ConfTaskReward;
import com.melot.module.task.driver.domain.GetUserTaskListResp;
import com.melot.module.task.driver.domain.GetUserTaskRewardResp;
import com.melot.module.task.driver.domain.UserTask;
import com.melot.module.task.driver.service.TaskInterfaceService;
import com.melot.opus.util.OpusCostantEnum;
import com.melot.sdk.core.util.MelotBeanFactory;
import com.melot.showmoney.driver.domain.PageShowMoneyHistory;
import com.melot.showmoney.driver.domain.ShowMoneyHistory;
import com.melot.showmoney.driver.service.ShowMoneyService;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class ProfileFunctions {
	
	/** 日志记录对象 */
	private static Logger logger = Logger.getLogger(ProfileFunctions.class);

	private LiveTypeSource liveTypeSource;

    public void setLiveTypeSource(LiveTypeSource liveTypeSource) {
        this.liveTypeSource = liveTypeSource;
    }

    /**
	 * 获取用户信息(10005001)
	 * 
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
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
				bValidHotData = true;
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
			if (hotData.containsKey("livevideoquality")) {
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
		    
		    Integer area = CityUtil.getParentCityIdNoDefault(userInfoDetail.getRegisterInfo().getCityId());
		    if (area != null) {
		        result.addProperty("area", area);
            }
		    result.addProperty("fansCount", UserRelationService.getFansCount(userId));
		    if (userInfoDetail.getProfile().getNickName() != null) {
	        	t = Cat.getProducer().newTransaction("MCall", "GeneralService.replaceSensitiveWords");
				try {
					result.addProperty("nickname", GeneralService.replaceSensitiveWords(userId, userInfoDetail.getProfile().getNickName()));
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
		    
		    Integer adminType = ProfileServices.getUserAdminType(userId);
            if (adminType != null && adminType != -1) {
                result.addProperty("siteAdmin", adminType);
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
			
			// web端不返回 
			if (platform != PlatformEnum.WEB) {
			    try {
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
                }
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
			if (checkTag) {
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
			        param1.addProperty("pageIndex", -1);
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
			            	}else {
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
			        result.add("userMedalList",new JsonParser().parse(new Gson().toJson(medals)).getAsJsonArray());
			        
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
						ApplyActor applyActor = null;
						t = Cat.getProducer().newTransaction("MCall", "applyActorService.getApplyActorByActorId");
						try {
							ApplyActorService applyActorService = MelotBeanFactory.getBean("applyActorService", ApplyActorService.class);
							applyActor = applyActorService.getApplyActorByActorId(userId);
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
                            result.addProperty("roomId", roomInfo.getRoomId() != null  ? roomInfo.getRoomId() : roomInfo.getActorId());
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
			                    if (roomInfo.getType().intValue() == AppIdEnum.GAME) {
			                        GameUserInfo gameUserInfo = null;
			        	        	t = Cat.getProducer().newTransaction("MCall", "com.melot.kkgame.service.UserService.getUserActorInfoByUserId");
			        				try {
			        					gameUserInfo = com.melot.kkgame.service.UserService.getUserActorInfoByUserId(userId);
			        					t.setStatus(Transaction.SUCCESS);
			        				} catch (Exception e) {
			        					Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
			        					t.setStatus(e);
			        				} finally {
			        					t.complete();
			        				}
			                        
			        				if (gameUserInfo != null) {
			        					// 新增用户作品数
			        					result.addProperty("opusCount", gameUserInfo.getOpusCout() == null ? 0 : gameUserInfo.getOpusCout());
									}
			                    }
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
			                					MedalSource.addUserMedal(userId, family.getFamilyMedal(), -1);
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
	 * @param checkTag 是否验证token标记
	 * @return 更新结果
	 */
	public JsonObject updateUserInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}

		// 获取参数
		JsonElement userIdje = jsonObject.get("userId");
		JsonElement nicknameje = jsonObject.get("nickname");
		JsonElement genderje = jsonObject.get("gender");
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
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "05020002");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "05020001");
			return result;
		}
		// 更新头像
//		JsonElement photoje = jsonObject.get("photo");
//		if (photoje != null && !photoje.isJsonNull() && !photoje.getAsString().isEmpty()) {
//			new SaveRemotePortrait(userId, photoje.getAsString()).start();
//		}
		MongoRoom mRoom = new MongoRoom();
		Map<String, Object> userMap = new HashMap<String, Object>();
		int flag = 0;
		String nickname = null;
		Integer gender = null;
		String introduce = null;
		if (nicknameje != null && !nicknameje.isJsonNull() && !nicknameje.getAsString().trim().isEmpty()) {
			nickname = nicknameje.getAsString().trim();
			// filter matchXSSTag,sensitive word,short url
			if (CommonUtil.matchXSSTag(nickname)
					||TextFilter.isShortUrl(nickname)
					|| !TextFilter.checkSpecialUnicode(nickname) || GeneralService.hasSensitiveWords(userId, nickname)) {
				JsonObject result = new JsonObject();
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
		    		if(digitCount<=6) {
		    			sb.append(nickname.charAt(i));
		    			if(sb.length()>=10) break;
		    		}
		    	} else {
		    		sb.append(nickname.charAt(i));
		    		if(sb.length()>=10) break;
		    	}
		    }
		    nickname = sb.toString();
			mRoom.setNickname(nickname);
			userMap.put(ProfileKeys.NICKNAME.key(), nickname);
		}
		if (genderje != null && !(genderje.isJsonNull() || genderje.getAsString().equals(""))) {
			try {
			    gender = genderje.getAsInt();
			} catch (Exception e) {
			    JsonObject result = new JsonObject();
			    result.addProperty("TagCode", "05020003");
			    return result;
			}
			mRoom.setGender(gender);
			userMap.put(ProfileKeys.GENDER.key(), gender);
			flag++;
		}
		if (birthdayje != null && !(birthdayje.isJsonNull() || birthdayje.getAsString().equals(""))) {
			mRoom.setBirthday(birthdayje.getAsString());
			userMap.put(ProfileKeys.BIRTHDAY.key(), birthdayje.getAsString());
			flag++;
		}
		if (cityje != null && !(cityje.isJsonNull() || cityje.getAsString().equals(""))) {
			int city;
			try {
			    city = cityje.getAsInt();
			    //无效的city信息
			    if (!GeneralService.isValidCity(city)) {
			    	JsonObject result = new JsonObject();
				    result.addProperty("TagCode", "05020007");
				    return result;
				}
			} catch (Exception e) {
			    JsonObject result = new JsonObject();
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
			    JsonObject result = new JsonObject();
			    result.addProperty("TagCode", "05020008");
			    return result;
			}
			String signature = GeneralService.replaceSensitiveWords(userId, signatureje.getAsString());
			mRoom.setSignature(signature);
			userMap.put(ProfileKeys.SIGNATURE.key(), signature);
			flag++;
		}
		if (introduceje != null && !introduceje.isJsonNull()) {
			introduce = introduceje.getAsString();
			introduce = GeneralService.replaceSensitiveWords(userId, introduce);
			userMap.put("introduce", introduce);
			flag++;
		}
		
		JsonObject result = new JsonObject();
		
		if (nickname != null) {
			if (UserService.checkNicknameRepeat(nickname, userId)) {
				// 昵称重复
				result.addProperty("TagCode", "05020102");
				return result;
			}
			flag++;
		}
		
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		if (flag > 0) {
		    int TagCode = com.melot.kktv.service.UserService.updateUserInfoV2(userId, userMap);
            if (TagCode != 0) {
                logger.error("调用存储过程(com.melot.kktv.service.UserService.updateUserInfo(" + userId + "," + new Gson().toJson(userMap) + "))未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
                result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
                return result;
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
		// 获取参数
		JsonElement userIdje = jsonObject.get("userId");

		// 验证参数
		int userId;
		if (userIdje != null && !userIdje.isJsonNull() && !userIdje.getAsString().equals("")) {
			// 验证数字
			try {
				userId = userIdje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "05030002");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "05030001");
			return result;
		}

		// 调用存储过程得到结果
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("userId", userId);
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Profile.getHonorList", map);
		} catch (SQLException e) {
			logger.error("未能正常调用存储过程", e);
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			return result;
		}
		String TagCode = (String) map.get("TagCode");
		if (TagCode.equals(TagCodeEnum.SUCCESS)) {
			// 取出列表
			@SuppressWarnings("unchecked")
			List<Object> honorList = (ArrayList<Object>) map.get("honorList");

			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCode);
			JsonArray jHonorList = new JsonArray();
			for (Object object : honorList) {
				jHonorList.add(((Honor) object).toJsonObject());
			}
			result.add("honorList", jHonorList);

			// 返回结果
			return result;
		} else {
			// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
			logger.error("调用存储过程(Profile.getHonorList(" + new Gson().toJson(map) + "))未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
			return result;
		}
	}

	/**
	 * 获取用户消费记录列表
	 * 
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return json对象形式的返回结果
	 */
	public JsonObject getUserConsumerList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}

		// 获取参数
		JsonElement userIdje = jsonObject.get("userId");
		JsonElement startTimeje = jsonObject.get("startTime");
		JsonElement endTimeje = jsonObject.get("endTime");
		JsonElement pageIndexje = jsonObject.get("pageIndex");

		// 验证参数
		int userId;
		long startTime;
		long endTime;
		int pageIndex;
		if (userIdje != null && !userIdje.isJsonNull() && !userIdje.getAsString().equals("")) {
			// 验证数字
			try {
				userId = userIdje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "05060002");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "05060001");
			return result;
		}
		if (startTimeje != null && !startTimeje.isJsonNull() && !startTimeje.getAsString().equals("")) {
			// 验证数字
			try {
				startTime = startTimeje.getAsLong();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "05060004");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "05060003");
			return result;
		}
		if (endTimeje != null && !endTimeje.isJsonNull() && !endTimeje.getAsString().equals("")) {
			// 验证数字
			try {
				endTime = endTimeje.getAsLong();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "05060006");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "05060005");
			return result;
		}
		if (pageIndexje != null && !pageIndexje.isJsonNull() && !pageIndexje.getAsString().equals("")) {
			// 验证数字
			try {
				pageIndex = pageIndexje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "05060008");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "05060007");
			return result;
		}

		// 调用存储过程得到结果
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("userId", userId);
		map.put("startTime", new Date(startTime));
		map.put("endTime", new Date(endTime));
		map.put("pageIndex", pageIndex);
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Profile.getUserConsumerList", map);
		} catch (SQLException e) {
			logger.error("未能正常调用存储过程", e);
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			return result;
		}
		String TagCode = (String) map.get("TagCode");
		if (TagCode.equals(TagCodeEnum.SUCCESS)) {
			// 取出列表
			@SuppressWarnings("unchecked")
			List<Object> recordList = (ArrayList<Object>) map.get("recordList");

			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCode);
			result.addProperty("consumerTotal", (Long) map.get("consumerTotal"));
			result.addProperty("pageTotal", (Integer) map.get("pageTotal"));
			JsonArray jRecordList = new JsonArray();
			for (Object object : recordList) {
				jRecordList.add(((ConsumerRecord) object).toJsonObject());
			}
			result.add("recordList", jRecordList);

			// 返回结果
			return result;
		} else if (TagCode.equals("02")) {
			/* '02';分页超出范围 */
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			if (map.get("consumerTotal") != null) {
				result.addProperty("consumerTotal", (Integer) map.get("consumerTotal"));
			} else {
				result.addProperty("consumerTotal", 0);
			}
			result.addProperty("pageTotal", (Integer) map.get("pageTotal"));
			result.add("recordList", new JsonArray());

			// 返回结果
			return result;
		} else {
			// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
			logger.error("调用存储过程(Profile.getUserConsumerList(" + new Gson().toJson(map) + "))未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
			return result;
		}
	}
	
	/**
	 * 获取用户送出的礼物列表
	 * 
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
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
            startTime = CommonUtil.getJsonParamLong(jsonObject, "startTime", 0, "10006004", DateUtil.getDayBeginTime(System.currentTimeMillis()) - 180 * 24 * 3600 * 1000l, Long.MAX_VALUE);
            endTime = CommonUtil.getJsonParamLong(jsonObject, "endTime", 0, "10006006", startTime, Long.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
		
		// 调用存储过程得到结果
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("userId", userId);
		map.put("startTime", new Date(startTime));
		map.put("endTime", new Date(endTime));
		map.put("pageIndex", pageIndex);
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Profile.getUserSendGiftList", map);
		} catch (SQLException e) {
			logger.error("未能正常调用存储过程", e);
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			return result;
		}
		String TagCode = (String) map.get("TagCode");
		if (TagCode.equals(TagCodeEnum.SUCCESS)) {
			// 取出列表
			@SuppressWarnings("unchecked")
			List<GiftRecord> recordList = (ArrayList<GiftRecord>) map.get("recordList");
			recordList = UserService.addUserExtra(recordList);
			
			result.addProperty("TagCode", TagCode);
			result.addProperty("pageTotal", (Integer) map.get("pageTotal"));
			JsonArray jRecordList = new JsonArray();
			if (recordList != null) {
				for (Object object : recordList) {
					jRecordList.add(((GiftRecord) object).toSendJsonObject());
				}
			}
			result.add("recordList", jRecordList);

			// 返回结果
			return result;
		} else if (TagCode.equals("02")) {
			/* '02';分页超出范围 */
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			result.addProperty("pageTotal", (Integer) map.get("pageTotal"));
			result.add("recordList", new JsonArray());

			// 返回结果
			return result;
		} else {
			// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
			logger.error("调用存储过程(Profile.getUserSendGiftList(" + new Gson().toJson(map) + "))未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
			result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
			return result;
		}
	}

	/**
	 * 获取用户收到的礼物列表
	 * 
	 * @param jsonObject 请求对象
	 * @return 结果字符串
	 */
	@SuppressWarnings("unchecked")
    public JsonObject getUserRsvGiftList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
	    JsonObject result = new JsonObject();
	    
	    int userId, pageIndex;
	    long startTime, endTime;
		try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, "10006008", 1, Integer.MAX_VALUE);
            startTime = CommonUtil.getJsonParamLong(jsonObject, "startTime", 0, "10006004", DateUtil.getDayBeginTime(System.currentTimeMillis()) - 180 * 24 * 3600 * 1000l, Long.MAX_VALUE);
            endTime = CommonUtil.getJsonParamLong(jsonObject, "endTime", 0, "10006006", startTime, Long.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

		// 调用存储过程得到结果
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("userId", userId);
		map.put("startTime", new Date(startTime));
		map.put("endTime", new Date(endTime));
		map.put("pageIndex", pageIndex);
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Index.getUserRsvGiftList", map);
		} catch (SQLException e) {
			logger.error("未能正常调用存储过程", e);
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			return result;
		}
		String TagCode = (String) map.get("TagCode");
		if (TagCode.equals(TagCodeEnum.SUCCESS)) {
			// 取出列表
			List<Object> recordList = (ArrayList<Object>) map.get("recordList");

			result.addProperty("TagCode", TagCode);
			result.addProperty("pageTotal", (Integer) map.get("pageTotal"));
			JsonArray jRecordList = new JsonArray();
			for (Object object : recordList) {
			    JsonObject recObj = ((GiftRecord) object).toRsvJsonObject();
			    // 默认为kk唱响用户
			    recObj.addProperty("roomSource", AppIdEnum.AMUSEMENT);
                recObj.addProperty("roomType", AppIdEnum.AMUSEMENT);
				jRecordList.add(recObj);
			}
			result.add("recordList", jRecordList);

			// 返回结果
			return result;
		} else if (TagCode.equals("02")) {
			/* '02';分页超出范围 */
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			result.addProperty("pageTotal", 0);
			result.add("recordList", new JsonArray());
			return result;
		} else {
			// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
			logger.error("调用存储过程(Index.getUserRsvGiftList(" + new Gson().toJson(map) + "))未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
			result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
			return result;
		}
	}
	
	/**
	 * 获取用户直播记录列表
	 * 
	 * @param jsonObject 请求对象
	 * @return json对象形式的返回结果
	 */
	public JsonObject getUserLiveList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		// 获取参数
		JsonElement userIdje = jsonObject.get("userId");
		JsonElement startTimeje = jsonObject.get("startTime");
		JsonElement endTimeje = jsonObject.get("endTime");
		JsonElement pageIndexje = jsonObject.get("pageIndex");

		// 验证参数
		int userId;
		long startTime;
		long endTime;
		int pageIndex;
		if (userIdje != null && !userIdje.isJsonNull() && !userIdje.getAsString().equals("")) {
			// 验证数字
			try {
				userId = userIdje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "05160002");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "05160001");
			return result;
		}
		if (startTimeje != null && !startTimeje.isJsonNull() && !startTimeje.getAsString().equals("")) {
			// 验证数字
			try {
				startTime = startTimeje.getAsLong();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "05160004");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "05160003");
			return result;
		}
		if (endTimeje != null && !endTimeje.isJsonNull() && !endTimeje.getAsString().equals("")) {
			// 验证数字
			try {
				endTime = endTimeje.getAsLong();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "05160006");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "05160005");
			return result;
		}
		if (pageIndexje != null && !pageIndexje.isJsonNull() && !pageIndexje.getAsString().equals("")) {
			// 验证数字
			try {
				pageIndex = pageIndexje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "05160008");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "05160007");
			return result;
		}

		// 调用存储过程得到结果
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("userId", userId);
		map.put("startTime", new Date(startTime));
		map.put("endTime", new Date(endTime));
		map.put("pageIndex", pageIndex);
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Profile.getUserLiveList", map);
		} catch (SQLException e) {
			logger.error("未能正常调用存储过程", e);
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			return result;
		}
		String TagCode = (String) map.get("TagCode");
		if (TagCode.equals(TagCodeEnum.SUCCESS)) {
			// 取出列表
			@SuppressWarnings("unchecked")
			List<Object> recordList = (ArrayList<Object>) map.get("recordList");

			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCode);
			result.addProperty("pageTotal", (Integer) map.get("pageTotal"));
			JsonArray jRecordList = new JsonArray();
			for (Object object : recordList) {
				jRecordList.add(((LiveRecord) object).toJsonObject());
			}
			result.add("recordList", jRecordList);

			// 返回结果
			return result;
		} else if (TagCode.equals("02")) {
			/* '02';分页超出范围 */
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			result.addProperty("pageTotal", (Integer) map.get("pageTotal"));
			result.add("recordList", new JsonArray());

			// 返回结果
			return result;
		} else {
			// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
			logger.error("调用存储过程(Profile.getUserLiveList(" + new Gson().toJson(map) + "))未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
			return result;
		}
	}

	/**
	 * 获取新注册任务列表(10005020)
	 * 
	 * @param paramJsonObject
	 * @return 新用户任务列表
	 */
	public JsonObject getNewUserTaskList(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
	    JsonObject result = new JsonObject();
	    
	    // 获取参数
        int userId, platform, appId, channelKey;
        try {
            userId = CommonUtil.getJsonParamInt(paramJsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(paramJsonObject, "platform", PlatformEnum.WEB, null, 1, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(paramJsonObject, "a", AppIdEnum.AMUSEMENT, null, 1, Integer.MAX_VALUE);
            channelKey = CommonUtil.getJsonParamInt(paramJsonObject, "ck", 0, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
	    
        Map<String, Object> map = getUserTaskList(userId, platform, channelKey, appId, 0);
        if (map != null && map.size() > 0) {
            if (userId > 0) {
                if (map.containsKey("checkDays") && ((Integer) map.get("checkDays")) > 0) {
                    result.addProperty("checkedDays", (Integer) map.get("checkDays"));
                    
                    if (map.containsKey("checkinReward") && map.get("checkinReward") != null) {
                        result.add("checkinReward", (JsonElement) map.get("checkinReward"));
                    }
                }
                if (map.containsKey("replenishDays")) {
                    int replenishDays = (Integer) map.get("replenishDays");
                    result.addProperty("replenishDays", replenishDays);
                    result.addProperty("replenishMoney", replenishDays*50);
                }
                if (map.containsKey("weeklyCheckinReward")) {
                    result.add("weeklyCheckinReward", (JsonElement) map.get("weeklyCheckinReward"));
                }
                if (map.containsKey("signInDays")) {
                    result.add("signInDays", (JsonElement) map.get("signInDays"));
                }
            }
            
            if (map.containsKey("weeklyCheckedDays")) {
                result.addProperty("weeklyCheckedDays", (Integer) map.get("weeklyCheckedDays"));
            }
            if (map.containsKey("indexDay")) {
                result.addProperty("indexDay", (Integer) map.get("indexDay"));
            }
            if (map.containsKey("firstRechargeReward")) {
                result.add("firstRechargeReward", (JsonElement) map.get("firstRechargeReward"));
            }
            if (map.containsKey("taskList")) {
                result.add("newUserTaskList", (JsonElement) map.get("taskList"));
            }
        }
		
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
	}

    /**
     * 获取用户任务列表
     * 读取mongodb所有任务/redis已完成任务/oracle已领取任务
     * @param userId
     * @param platform
     * @param channelKey
     * @return
     */
    private static Map<String, Object> getUserTaskList(int userId, int platform, int channelKey, int appId, int rewardTaskId) {
        Map<String, Object> result = new HashMap<String, Object>();
        
        if (platform == PlatformEnum.IPHONE_GAMAGIC) {
            platform = PlatformEnum.IPHONE;
        }
        
        TaskInterfaceService taskInterfaceService = (TaskInterfaceService) MelotBeanFactory.getBean("taskInterfaceService");
        GetUserTaskListResp resp = null;
        try {
            resp = taskInterfaceService.getUserTaskList(userId, platform, appId);
        } catch (MelotModuleException e) {
            
        } catch (Exception e) {
            logger.error("TaskInterfaceService.getgetUserTaskList(" + userId + ", " + platform + ", " + appId + ") execute exception", e);
            return result;
        }
        if (resp != null) {
            List<UserTask> list = resp.getUserTasks();
            if (list != null && list.size() > 0) {
                JsonArray taskArr = new JsonArray();
                Task task;
                for (UserTask userTask : list) {
                    task = new Task();
                    if (userTask.getTaskOrder() != null) {
                        task.setOrder(userTask.getTaskOrder());
                    }
                    if (userTask.getTaskName() != null) {
                        task.setTaskDesc(userTask.getTaskName());
                    }
                    if (userTask.getTaskId() != null) {
                        task.setTaskId(userTask.getTaskId());
                        
                        if (rewardTaskId > 0 && rewardTaskId == userTask.getTaskId()) {
                            task.setStatus(2);
                        } else {
                            if (userTask.getStatus() != null) {
                                task.setStatus(userTask.getStatus());
                            }
                        }
                    }
                    if (userTask.getTaskReward() != null) {
                        task.setTaskReward(userTask.getTaskReward());
                    }
                    if (userTask.getVersionCode() != null) {
                        task.setVersionCode(userTask.getVersionCode());
                    }
                    if (userTask.getTaskId() != null && userTask.getTaskId() == 10000014) {
                    	Integer kbi = null;
                    	try {
                    		FeedbackService feedbackService = (FeedbackService) MelotBeanFactory.getBean("feedbackService");
							kbi = feedbackService.getTotalProfitByUserId(userId);
						} catch (Exception e) {
							logger.error("call FeedbackService getTotalProfitByUserId, userId : " + userId, e);
						}
                    	task.setGetMoney((kbi == null || kbi < 0) ? 0 : kbi);
                    } else if (userTask.getGetMoney() != null) {
                        task.setGetMoney(userTask.getGetMoney());
                    }
                    if (userTask.getGetMoney() != null) {
                        task.setGetMoney(userTask.getGetMoney());
                    }
                    
                    taskArr.add(task.toJsonObject());
                }
                result.put("taskList", taskArr);
            }
            
            result.put("checkDays", resp.getCheckedDays());
            
            List<ConfTaskReward> checkinRewardList = resp.getCheckinReward();
            if (checkinRewardList != null && checkinRewardList.size() > 0) {
                JsonArray checkinReward = new JsonArray();
                for (ConfTaskReward confTaskReward : checkinRewardList) {
                    JsonObject reward = new JsonObject();
                    reward.addProperty(String.valueOf(confTaskReward.getContiniuDays()), confTaskReward.getRewardCount());
                    checkinReward.add(reward);
                }
                
                result.put("checkinReward", checkinReward);
            }
            
            
            List<ConfTaskReward> firstRechargeRewardList = resp.getFirstRechargeReward();
            if (firstRechargeRewardList != null && firstRechargeRewardList.size() > 0) {
                JsonArray firstRechargeReward = new JsonArray();
                for (ConfTaskReward confTaskReward : firstRechargeRewardList) {
                    JsonObject rewardObj = new JsonObject();
                    rewardObj.addProperty("icon", confTaskReward.getRewardIcon());
                    rewardObj.addProperty("desc", confTaskReward.getRewardDesc());
                    firstRechargeReward.add(rewardObj);
                }
                
                result.put("firstRechargeReward", firstRechargeReward);
            }
            
            //7日签到
            result.put("weeklyCheckedDays", resp.getWeeklyCheckedDays());
            result.put("indexDay", resp.getIndexDay());
            result.put("replenishDays", resp.getReplenishDays());
            List<ConfTaskReward> weeklyCheckinRewardList = resp.getWeeklyCheckinReward();
            if (weeklyCheckinRewardList != null && weeklyCheckinRewardList.size() > 0) {
                JsonArray weeklyCheckinReward = new JsonArray();
                for (ConfTaskReward confTaskReward : weeklyCheckinRewardList) {
                    JsonObject reward = new JsonObject();
                    reward.addProperty(String.valueOf(confTaskReward.getContiniuDays()), confTaskReward.getRewardDesc());
                    weeklyCheckinReward.add(reward);
                }
                
                result.put("weeklyCheckinReward", weeklyCheckinReward);
            }
            String weeklyCheckInStr = resp.getSignInDays();
            if (weeklyCheckInStr != null) {
                String[] weeklyCheckInDays = weeklyCheckInStr.split(",");
                ArrayList<Integer> signInDaysList = new ArrayList<Integer>();
                for (String day : weeklyCheckInDays) {
                    signInDaysList.add(Integer.parseInt(day));
                }
                result.put("signInDays", new Gson().toJsonTree(signInDaysList).getAsJsonArray());
            }
        }
        
        return result;
    }

	/**
	 * 完成任务列表中的任务(10005021)
	 * 
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return
	 */
	public JsonObject finishTask(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
	    JsonObject result = new JsonObject();
		
	    // 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		// 获取参数
        int userId, taskId, platform, appId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "05210002", 1, Integer.MAX_VALUE);
            taskId = CommonUtil.getJsonParamInt(jsonObject, "taskId", 0, "05210004", 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, 1, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

		// 获取参数
		JsonElement ckje = jsonObject.get("ck");
		
		Integer channelKey = 1;// 完成任务 放开任务列表
		if (ckje != null && !ckje.isJsonNull() && !ckje.getAsString().isEmpty()) {
			try {
				channelKey = Integer.valueOf(ckje.getAsString());
			} catch (Exception e) {
				channelKey = null;
			}
		}
		
		if (taskId == 10000014) {
			//邀请好友任务不可完成
			result.addProperty("TagCode", "05210007");
			return result;
		}
		TaskInterfaceService taskInterfaceService = (TaskInterfaceService) MelotBeanFactory.getBean("taskInterfaceService");
        try {
            long taskCount = taskInterfaceService.finishUserTask(userId, taskId, platform, appId);
            if (taskId == 10000015) {
                result.addProperty("sunShineCount", taskCount);
            }
        } catch (MelotModuleException e) {
            switch (e.getErrCode()) {
//            case 101:
//                // 该任务不存在
//                result.addProperty("TagCode", "05210104");
//                break;
                
            case 102:
                // 没有绑定手机号码
                result.addProperty("TagCode", "05210006");
                break;

            case 103:
                // 该任务已完成或已领取任务奖励
                result.addProperty("TagCode", "05210103");
                break;

            case 104:
                // 调用存储过程异常(查询任务是否已领奖) 
                result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
                break;

            case 105:
                // 调用存储过程未得到正常结果(赠送阳光入库) 
                result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
                break;

            default:
                // 调用存储过程未得到正常结果(详情查看日志) 
                result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
                break;
            }
            
            return result;
        } catch (Exception e) {
            // 调用存储过程未得到正常结果(详情查看日志) 
            result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
            return result;
        }
        
        Map<String, Object> map = getUserTaskList(userId, platform, channelKey, appId, 0);
        result.add("newUserTaskList", (JsonElement) map.get("taskList"));
        
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 7日签到补签(50005002)
     * 
     * @param jsonObject 请求对象
     * @param checkTag 是否验证token标记
     * @return
     */
    public JsonObject replenishSignIn(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        // 获取参数
        int userId, appId, platform;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        int sunShineCount = 0;
        TaskInterfaceService taskInterfaceService = (TaskInterfaceService) MelotBeanFactory.getBean("taskInterfaceService");
        try {
            sunShineCount = (int) taskInterfaceService.replenishSignIn(userId, appId, platform);
        } catch (MelotModuleException e) {
            switch (e.getErrCode()) {
            case 101:
                // 用户没有需要补签的天数
                result.addProperty("TagCode", "05020001");
                break;
                
            case 102:
                // 扣除秀币失败
                result.addProperty("TagCode", "05020002");
                break;

            default:
                // 调用存储过程未得到正常结果(详情查看日志) 
                result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
                break;
            }
            
            return result;
        } catch (Exception e) {
            // 调用存储过程未得到正常结果(详情查看日志) 
            result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
            return result;
        }
        
        Map<String, Object> map = getUserTaskList(userId, platform, 0, appId, 0);
        result.add("newUserTaskList", (JsonElement) map.get("taskList"));
        result.addProperty("sunShineCount", sunShineCount);
        
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 7日签到抽奖(50005003)
     * 
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject weeklyLottery(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        // 获取参数
        int userId;
        boolean isDraw = false;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            UserProfile userProfile = com.melot.kktv.service.UserService.getUserInfoV2(userId);
            if (userProfile == null || userProfile.getIdentifyPhone() == null) {
                result.addProperty("TagCode", "05030003");
            } else {
                TaskInterfaceService taskInterfaceService = (TaskInterfaceService) MelotBeanFactory.getBean("taskInterfaceService");
                isDraw = taskInterfaceService.isDraw(userId);
                if (isDraw && TagCodeEnum.SUCCESS.equals(taskInterfaceService.updateDraw(userId))){
                    String awardRulesId = "lotteryAtweeklyCheckIn";
                    
                    //抽奖
                    Map<String, Object> retMap = LotteryArithmetic.lottery(awardRulesId, userId, null, null);
                    if (retMap != null && !retMap.isEmpty()) {
                        int prizeId = Integer.parseInt((String) retMap.get(LotteryArithmeticCache.SERVICE_KEY_giftId));
                        result.addProperty("prizeId", prizeId);
                        result.addProperty("prizeName", (String) retMap.get(LotteryArithmeticCache.SERVICE_KEY_giftName));
                        result.addProperty("prizeCount", (Integer) retMap.get(LotteryArithmeticCache.SERVICE_KEY_giftCount));
                        LotteryPrizeList lotteryPrizeArray = MelotBeanFactory.getBean("lotteryPrizeList", LotteryPrizeList.class);
                        Map<Integer, LotteryPrize> lotteryPrizeList = lotteryPrizeArray.getList();
                        result.addProperty("prizeIcon", lotteryPrizeList.get(prizeId).getPrizeIcon());
                        result.addProperty("prizeDesc", lotteryPrizeList.get(prizeId).getPrizeDesc());
                        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                    } else {
                        result.addProperty("TagCode", "05030002");
                    }
                } else {
                    result.addProperty("TagCode", "05030001");
                }
            }
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        
        return result;
    }
    
    /**
     * 7日签到抽奖资格校验(50005004)
     * 
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject isWeeklyLotteryDraw(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        // 获取参数
        int userId;
        boolean isDraw;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            TaskInterfaceService taskInterfaceService = (TaskInterfaceService) MelotBeanFactory.getBean("taskInterfaceService");
            isDraw = taskInterfaceService.isDraw(userId);
            result.addProperty("isDraw", isDraw ? 1:0);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        
        return result;
	}
	
	/**
	 * 领取任务奖励(10005022)
	 * 
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return
	 */
	public JsonObject getReward(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
	    JsonObject result = new JsonObject();
		
	    // 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		// 获取参数
		int userId, taskId, platform, appId, channelKey;
		try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "05220002", 1, Integer.MAX_VALUE);
            taskId = CommonUtil.getJsonParamInt(jsonObject, "taskId", 0, "05220004", 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, 1, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 1, Integer.MAX_VALUE);
            channelKey = CommonUtil.getJsonParamInt(jsonObject, "ck", 1, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
		
		if (taskId == 10000014) {
			result.addProperty("TagCode", "05220107");
			return result;
		}
		TaskInterfaceService taskInterfaceService = (TaskInterfaceService) MelotBeanFactory.getBean("taskInterfaceService");
		GetUserTaskRewardResp resp;
        try {
            resp = taskInterfaceService.updateUserTaskReward(userId, taskId, platform, appId, appId == AppIdEnum.AMUSEMENT ? true : false);
        } catch (MelotModuleException e) {
            switch (e.getErrCode()) {
            case 101:
                // 没有验证手机号码
                result.addProperty("TagCode", "05220012");
                break;
                
            case 102:
                // 不存在该任务
                result.addProperty("TagCode", "05220102");
                break;

            case 103:
                // 任务未完成
                result.addProperty("TagCode", "05220103");
                break;

            case 105:
                // 奖励已经发放
                result.addProperty("TagCode", "05220104");
                break;

            case 106:
                // 调用存储过程未得到正常结果(详情查看日志) 
                result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
                break;

            default:
                // 调用存储过程未得到正常结果(详情查看日志) 
                result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
                break;
            }
            
            return result;
        } catch (Exception e) {
            // 调用存储过程未得到正常结果(详情查看日志) 
            result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
            return result;
        }
        
        result.addProperty("getMoney", resp.getShowMoney());
        result.addProperty("getVip", resp.getPropId());
        result.addProperty("getCar", resp.getCarId());
        
        Map<String, Object> map = getUserTaskList(userId, platform, channelKey, appId, taskId);
        result.add("newUserTaskList", (JsonElement) map.get("taskList"));
        
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
	}
    
    /**
	 * 获取用户中奖记录列表
	 * 
	 * @param jsonObject 请求对象
	 * @return json对象形式的返回结果
	 */
	public JsonObject getUserWinLotteryList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		// 获取参数
		JsonElement userIdje = jsonObject.get("userId");
		JsonElement startTimeje = jsonObject.get("startTime");
		JsonElement endTimeje = jsonObject.get("endTime");
		JsonElement pageIndexje = jsonObject.get("pageIndex");
		JsonElement countPerPageje = jsonObject.get("countPerPage");

		// 验证参数
		int userId;
		long startTime;
		long endTime;
		int pageIndex;
		int countPerPage;
		if (userIdje != null && !userIdje.isJsonNull() && !userIdje.getAsString().equals("")) {
			// 验证数字
			try {
				userId = userIdje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "05240002");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "05240001");
			return result;
		}
		if (startTimeje != null && !startTimeje.isJsonNull() && !startTimeje.getAsString().equals("")) {
			// 验证数字
			try {
				startTime = startTimeje.getAsLong();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "05240004");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "05240003");
			return result;
		}
		if (endTimeje != null && !endTimeje.isJsonNull() && !endTimeje.getAsString().equals("")) {
			// 验证数字
			try {
				endTime = endTimeje.getAsLong();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "05240006");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "05240005");
			return result;
		}
		if (pageIndexje != null && !pageIndexje.isJsonNull() && !pageIndexje.getAsString().equals("")) {
			// 验证数字
			try {
				pageIndex = pageIndexje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "05240008");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "05240007");
			return result;
		}
		if (countPerPageje != null && !countPerPageje.isJsonNull() && !countPerPageje.getAsString().equals("")) {
			// 验证数字
			try {
				countPerPage = countPerPageje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "05240010");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "05240009");
			return result;
		}

		// 调用存储过程得到结果
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("userId", userId);
		map.put("startTime", new Date(startTime));
		map.put("endTime", new Date(endTime));
		map.put("pageIndex", pageIndex);
		map.put("countPerPage", countPerPage);
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Profile.getUserWinLotteryList", map);
		} catch (SQLException e) {
			logger.error("未能正常调用存储过程", e);
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			return result;
		}
		String TagCode = (String) map.get("TagCode");
		if (TagCode.equals(TagCodeEnum.SUCCESS)) {
			// 取出列表
			@SuppressWarnings("unchecked")
			List<Object> recordList = (ArrayList<Object>) map.get("recordList");

			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCode);
			result.addProperty("pageTotal", (Integer) map.get("pageTotal"));
			JsonArray jRecordList = new JsonArray();
			for (Object object : recordList) {
				jRecordList.add(((WinLotteryRecord) object).toJsonObject());
			}
			result.add("recordList", jRecordList);

			// 返回结果
			return result;
		} else if (TagCode.equals("02")) {
			/* '02';分页超出范围 */
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			result.addProperty("pageTotal", (Integer) map.get("pageTotal"));
			result.add("recordList", new JsonArray());

			// 返回结果
			return result;
		} else {
			// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
			logger.error("调用存储过程(Profile.getUserWinLotteryList(" + new Gson().toJson(map) + "))未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
			return result;
		}
	}
	
	/**
	 * 获取用户系统管理员类型 4:官,5:代(改成售),7:巡, 8:技, 9:运 (10005039)
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return
	 */
	public JsonObject getUserAdminType(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		
		JsonObject result = new JsonObject();
		
		JsonElement userIdje = jsonObject.get("userId");
		Integer userId = null;
		if (userIdje != null && !userIdje.isJsonNull() && !userIdje.getAsString().isEmpty()) {
			try {
				userId = userIdje.getAsInt();
			} catch (Exception e) {
				result.addProperty("TagCode", "05390002");
				return result;
			}
		} else {
			result.addProperty("TagCode", "05390001");
			return result;
		}
		
		Integer adminType = 0;
		DBObject dbObj = CommonDB.getInstance(CommonDB.COMMONDB).getCollection(CollectionEnum.SITEADMINLIST)
				.findOne(new BasicDBObject("userId", userId));
		if (dbObj!=null && dbObj.containsField("admin")) {
			if (dbObj.get("admin") instanceof Double) {
				adminType = Integer.valueOf(((Double) dbObj.get("admin")).intValue());
			}
			if (dbObj.get("admin") instanceof Integer) {
				adminType = (Integer) dbObj.get("admin");
			}
			if (dbObj.get("admin") instanceof String) {
				adminType = Integer.parseInt((String) dbObj.get("admin"));
			}
		}
		result.addProperty("adminType", adminType);
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
		
	}

    /**
     * 修改房间主题（10005055）
     * @param paramJsonObject
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
        
        RoomInfo roomInfo = new RoomInfo();
    	roomInfo.setActorId(userId);
		roomInfo.setRoomTheme(roomTheme);
		
		if (com.melot.kktv.service.RoomService.updateRoomInfo(roomInfo)) {
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
     * @param paramJsonObject
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
     * @param paramJsonObject
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
        try{
            ApplyActorService applyActorService = MelotBeanFactory.getBean("applyActorService", ApplyActorService.class);
            ApplyActor applyActor = applyActorService.getApplyActorByActorId(userId);
            result.addProperty("userId", userId);
            
            if (appId == AppIdEnum.GAME) {
                if (applyActor == null) { //未提交审核
                    result.addProperty("status", -2);
                    result.addProperty("state", -2);
                    result.add("typeList", liveTypeSource.getAllHashLiveMap());
                } else {
                    result.addProperty("status", applyActor.getStatus());
                    result.addProperty("state", applyActor.getStatus());
                    result.addProperty("name", checkNullString(applyActor.getRealName()));
                    result.addProperty("identity", checkNullString(applyActor.getIdentityNumber()));
                    result.addProperty("mobile", checkNullString(applyActor.getMobile()));
                    result.addProperty("qq", checkNullString(applyActor.getQqNumber()));
                    
                    if (!StringUtil.strIsNull(applyActor.getIntroduce())) {
                        String[]introduces = applyActor.getIntroduce().split(",");
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
                    result.addProperty("name", checkNullString(applyActor.getRealName()));
                    result.addProperty("identity", checkNullString(applyActor.getIdentityNumber()));
                    result.addProperty("mobile", checkNullString(applyActor.getMobile()));
                    result.addProperty("qq", checkNullString(applyActor.getQqNumber()));
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
    
    private static String checkNullString(String string){
        return string == null ? "" : string;
    }
    
    /**
     * 获取用户粉丝数和关注数
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
     * @param paramJsonObject 参数，传递的勋章的类型
     * @author fenggaopan 
     * @since 2015-12-03 15:13:00
     * @return 
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
		    result.add("medalInfos",jsonArray);
			result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
		} catch (Exception e) {
			logger.error(e);
		}
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result ;
	}
	
	/**
	 * 获取用户秀币消费列表(10006063)
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
            startTime = CommonUtil.getJsonParamLong(jsonObject, "startTime", 0, "10006004", DateUtil.getDayBeginTime(System.currentTimeMillis()) - 180 * 24 * 3600 * 1000l, Long.MAX_VALUE);
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
        			for (ShowMoneyHistory hist : list) {
        				JsonObject moneyObj = new JsonObject();
        				if (hist.getConsumeAmount() != null) {
        					moneyObj.addProperty("amount", hist.getConsumeAmount());
        				}
        				if (hist.getToUserId() != null && hist.getToUserId() > 0) {
        				    KkUserService kkUserService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
        				    UserProfile userProfile = kkUserService.getUserProfile(hist.getToUserId());
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
            startTime = CommonUtil.getJsonParamLong(jsonObject, "startTime", 0, "10006004", DateUtil.getDayBeginTime(System.currentTimeMillis()) - 180 * 24 * 3600 * 1000l, Long.MAX_VALUE);
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
        			for (ShowMoneyHistory hist : list) {
        				JsonObject moneyObj = new JsonObject();
        				if (hist.getIncomeAmount() != null) {
        					moneyObj.addProperty("amount", hist.getIncomeAmount());
        				}
        				if (hist.getUserId() != null && hist.getUserId() > 0) {
        				    KkUserService kkUserService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
        				    UserProfile userProfile = kkUserService.getUserProfile(hist.getUserId());
        					if (userProfile != null && userProfile.getNickName()!= null) {
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
            startTime = CommonUtil.getJsonParamLong(jsonObject, "startTime", 0, "10006004", DateUtil.getDayBeginTime(System.currentTimeMillis()) - 180 * 24 * 3600 * 1000l, Long.MAX_VALUE);
            endTime = CommonUtil.getJsonParamLong(jsonObject, "endTime", 0, "10006006", startTime, Long.MAX_VALUE);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put("userId", userId);
		map.put("startTime", new Date(startTime));
		map.put("endTime", new Date(endTime));
		map.put("pageIndex", pageIndex);
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Profile.getUserBuyPropertiesList", map);
		} catch (SQLException e) {
			logger.error("未能正常调用存储过程", e);
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			return result;
		}
		String TagCode = (String) map.get("TagCode");
		if (TagCode.equals(TagCodeEnum.SUCCESS)) {
			// 取出列表
			@SuppressWarnings("unchecked")
			List<Object> recordList = (ArrayList<Object>) map.get("recordList");
			result.addProperty("TagCode", TagCode);
			result.addProperty("pageTotal", (Integer) map.get("pageTotal"));
			JsonArray jRecordList = new JsonArray();
			for (Object object : recordList) {
				jRecordList.add(((BuyProperties) object).toJsonObject());
			}
			result.add("recordList", jRecordList);
			// 返回结果
			return result;
		} else if (TagCode.equals("02")) {
			/* '02';分页超出范围 */
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			result.addProperty("pageTotal", 0);
			result.add("recordList", new JsonArray());
			return result;
		} else {
			// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
			logger.error("调用存储过程(Profile.getUserChargeList)未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
			result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
			return result;
		}
	}
	
	/**
	 * 用户切换异常短信开关(10006066)
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
        
        if(UserService.changeUserSmsSwitch(userId, state)) {
        	result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        	return result;
        }
        result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
    	return result;
	}
	
	/**
     * 设置常用设备(50001027)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject setCommonDevice(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int userId;
        String deviceUId, deviceName, deviceModel;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            deviceUId = CommonUtil.getJsonParamString(jsonObject, "deviceUId", "", null, 1, 40);
            deviceName = CommonUtil.getJsonParamString(jsonObject, "deviceName", "", null, 1, 40);
            deviceModel = CommonUtil.getJsonParamString(jsonObject, "deviceModel", "", null, 1, 40);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        ProfileServices.setUserCommonDevice(userId, deviceUId, deviceName, deviceModel);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 删除常用设备(50001028)
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
	
}

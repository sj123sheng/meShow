package com.melot.kktv.action;

import com.chinacreator.videoalliance.util.ChinaUnicomEnum;
import com.chinacreator.videoalliance.util.DesUtil;
import com.dianping.cat.Cat;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.blacklist.service.BlacklistService;
import com.melot.common.melot_utils.StringUtils;
import com.melot.content.config.domain.GalleryInfo;
import com.melot.content.config.domain.GalleryOrderRecord;
import com.melot.content.config.domain.RecordProcessedRecord;
import com.melot.content.config.facepack.service.GalleryInfoService;
import com.melot.content.config.facepack.service.GalleryOrderRecordService;
import com.melot.content.config.live.upload.impl.YouPaiService;
import com.melot.content.config.report.service.RecordProcessedRecordService;
import com.melot.family.driver.constant.UserApplyActorStatusEnum;
import com.melot.family.driver.domain.DO.BrokerageFirmDO;
import com.melot.family.driver.domain.DO.UserApplyActorDO;
import com.melot.family.driver.domain.FamilyInfo;
import com.melot.family.driver.service.UserApplyActorService;
import com.melot.kk.module.report.dbo.ReportFlowRecord;
import com.melot.kk.module.report.service.ReportFlowService;
import com.melot.kk.module.report.util.CommonStateCode;
import com.melot.kk.module.report.util.Result;
import com.melot.kk.userSecurity.api.constant.IdPicStatusEnum;
import com.melot.kk.userSecurity.api.constant.UserVerifyStatusEnum;
import com.melot.kk.userSecurity.api.constant.UserVerifyTypeEnum;
import com.melot.kk.userSecurity.api.domain.DO.UserVerifyDO;
import com.melot.kk.userSecurity.api.domain.param.UserVerifyParam;
import com.melot.kk.userSecurity.api.service.UserVerifyService;
import com.melot.kkcore.user.api.ShowMoneyHistory;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcore.user.api.UserStaticInfo;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kkcx.model.DynamicEmoticon;
import com.melot.kkcx.model.Sticker;
import com.melot.kkcx.service.FamilyService;
import com.melot.kkcx.service.ProfileServices;
import com.melot.kkcx.service.RoomService;
import com.melot.kkgame.redis.ActorInfoSource;
import com.melot.kktv.model.ResCuSpOrder;
import com.melot.kktv.redis.HotDataSource;
import com.melot.kktv.service.ConfigService;
import com.melot.kktv.service.ConsumeService;
import com.melot.kktv.service.GeneralService;
import com.melot.kktv.service.UserService;
import com.melot.kktv.util.*;
import com.melot.kktv.util.CommonUtil.ErrorGetParameterException;
import com.melot.kktv.util.confdynamic.SystemConfig;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;
import com.melot.module.packagegift.driver.service.TicketService;
import com.melot.module.packagegift.driver.service.VipService;
import com.melot.module.packagegift.util.GiftPackageEnum;
import com.melot.sdk.core.util.MelotBeanFactory;
import com.melot.stream.driver.service.LiveStreamConfigService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.*;

/**
 * 其他相关的接口类
 * 
 * @author LY
 * 
 */
public class OtherFunctions {

	/** 日志记录对象 */
	private static Logger logger = Logger.getLogger(OtherFunctions.class);
    
	private static Logger showMoneyLogger = Logger.getLogger("showMoneyLogger");
	
    /** 小喇叭自动通过审核 */
    private static final int SPEAK_STATE_AUTO_COMMIT = 1;
    
    private static final int SEND_LOUDER_SPEAKER_COST = 10 * 1000;
    
    private static final String ORDER_KEY = "kkorder_%s_%s";
    
    @Autowired
    private ConfigService configService;

    @Resource
    UserApplyActorService userApplyActorService;

    @Resource
    UserVerifyService userVerifyService;
    
    @SuppressWarnings("unused")
    private ActorInfoSource actorInfoSource;

    public void setActorInfoSource(ActorInfoSource actorInfoSource) {
        this.actorInfoSource = actorInfoSource;
    }
    
    /**
     * 通用接口过期实现
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject lowVersionExcute(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        result.addProperty("TagCode", TagCodeEnum.LOW_VERSION_EXCEPTION);
        return result;
    }

	/**
	 * 发送小喇叭(20000002)
	 * 
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return 发送结果
	 */
	public JsonObject speakToTotalStation(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		
	    JsonObject result = new JsonObject();
		
	    // 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		// 新增喇叭发送方式参数sendType(0-秀币发送，1-券发送)，可空，默认0
		int sendType, userId, roomId, ticketId = 0, appId = 1;
		String content, href, nickName = "";
		
		TicketService ticketService = (TicketService) MelotBeanFactory.getBean("ticketService");

		try{
			sendType = CommonUtil.getJsonParamInt(jsonObject, "sendType", 0, null, 0, 1);
	    	roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, TagCodeEnum.ROOMID_MISSING, 1, Integer.MAX_VALUE);
	        userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
	        content = CommonUtil.getJsonParamString(jsonObject, "content", null, "20020001", 0, 256);		
	        href = CommonUtil.getJsonParamString(jsonObject, "href", null, null, 0, 256);
	        appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 0, Integer.MAX_VALUE);
	        //当发送方式是券发送时（sendType=1），ticketId必填
	        if (sendType == 1) {
	        	ticketId = CommonUtil.getJsonParamInt(jsonObject, "ticketId", 0, "20020005", 1, Integer.MAX_VALUE);
			}
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
		
		UserProfile userInfo = com.melot.kktv.service.UserService.getUserInfoV2(userId);
		nickName = userInfo != null ? userInfo.getNickName() : HotDataSource.getHotFieldValue(String.valueOf(userId), "nickname");
		content = com.melot.kkcx.service.GeneralService.replaceSensitiveWords(userId, content);
		if (content.length() > 40) {
		    content = content.substring(0, 40);
        }
		
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("userId", userId);
		map.put("roomId", roomId);
		map.put("content", content);
		map.put("nickName", nickName);
		map.put("href", href);
        map.put("appId", appId);
		
		int state = 0;
		//state改为0，表示需要后台审核喇叭内容，1表示不需要审核
		map.put("state", state);
		
		// type: 1-普通喇叭，2-红包喇叭
        map.put("type", 1);
		
		boolean flag = false, vipFlag = false;
		
		Integer count = null;
		//喇叭券秀币价值
		long costMoney = SEND_LOUDER_SPEAKER_COST;
		
		VipService vipService = (VipService) MelotBeanFactory.getBean("vipService");
		if (sendType == 0) {
			//超级会员每天3个8折喇叭优惠
			List<Integer> list = vipService.getUserProp(userId);
			if (list != null && list.size() > 0 && list.contains(100004)) {
			    count = vipService.getSvipDailyLoudspeakerCount(userId);
				if (count == null || count < 3) {
					costMoney = (long) (costMoney * 0.8);
					vipService.setSvipDailyLoudspeakerCount(userId, 1);
					vipFlag = true;
				}
			} 
            //使用秀币发送喇叭，先扣秀币
            long showMoney = com.melot.kktv.service.UserService.getUserShowMoney(userId);
            //秀币金额不足
            if (showMoney < costMoney) {
                result.addProperty("TagCode", "20020002");
                return result;
            }
            // 扣秀币，加流水
            ShowMoneyHistory showMoneyHistory = new ShowMoneyHistory();
            try {
                showMoneyHistory.setUserId(userId);
                showMoneyHistory.setCount(1);
                showMoneyHistory.setDtime(new Date());
                showMoneyHistory.setConsumeAmount((int) costMoney);
                showMoneyHistory.setType(24);
                showMoneyHistory.setProductDesc("发送小喇叭");
                
                KkUserService userService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
                if (userService.addAndGetUserAssets(userId, - costMoney, true, showMoneyHistory) == null) {
                    result.addProperty("TagCode", "20020003");
                    return result;
                }
                
                // 更新用户闪星
                com.melot.kkcx.service.UserService.updateStarLevel(userId, costMoney);
            } catch (Exception e) {
                logger.error("用户：" + userId + "，发送小喇叭, 扣除秀币和增加秀币消费流水执行异常", e);
                Cat.logError("addAndGetUserAssets failed, userId: " + userId + ", price: -" + costMoney + ", showMoneyHistory: " + new Gson().toJson(showMoneyHistory), e);
                result.addProperty("TagCode", "20020003");
                return result;
            }
            
            logger.info("用户[" + userId + "]在房间[" + roomId + "]发送了一个小喇叭,花费[" + costMoney + "]");
//            }
			map.put("costMoney", ((Number) costMoney).intValue());
		} else if (sendType == 1) {
			//使用喇叭券发送喇叭，先扣除券
		    try {
		        flag = ticketService.insertUseTicket(userId, ticketId, GiftPackageEnum.TICKET_USE, 1, "用户" + userId + "使用喇叭券" + ticketId);
		    } catch (Exception e) {
		        logger.info("使用喇叭券扣券异常", e);
		    }
			
			if (!flag) {
				result.addProperty("TagCode", "20020004");
				return result;
			}
			map.put("costMoney", 0);
			map.put("ticketId", ticketId);
		}
		
		//调用发送喇叭存储过程 P_LOUDSPEAKER
		boolean ret = false;
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Other.loudSpeaker", map);
			
			String TagCode = (String) map.get("TagCode");
			if (!TagCodeEnum.SUCCESS.equals(TagCode)) {
				if (sendType == 1) {
					//发送喇叭存储过程失败，将扣除的券加1
					ret = ticketService.insertSendTicket(userId, ticketId, GiftPackageEnum.TICKET_SEND, 1, "用户" + userId + "发送喇叭操作失败退回已扣除喇叭券", 0);
					if (!ret) {
						logger.error("用户:" + userId + "，发送喇叭失败，券" + ticketId + "，已扣除1张，退回失败");
					}
				} else if (sendType == 0 && costMoney > 0) {
					if (vipFlag == true && count != null && count < 3) {
					    vipService.setSvipDailyLoudspeakerCount(userId, -1);
					}
					// 发送喇叭存储过程失败，将扣除的秀币返还
					showMoneyLogger.info("Failed: 发送喇叭失败，需返还秀币{userId: " + userId + ", showMoney: " + costMoney);
	                logger.error("执行过程中发生异常,返还已扣除的秀币, userId " + userId + " showMoney : " + costMoney);
				}
				logger.error("调用存储过程(Other.loudSpeaker)未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
				result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);				
			} else {
			    if (map.get("state").equals(SPEAK_STATE_AUTO_COMMIT)) {
			        //喇叭信息存入 Redis 通道，以便房间可以取到喇叭信息显示在房间上
			        //喇叭内容必须先通过后台审核，取消直接推消息渠道
			        Map<String, Object> messageMap = new HashMap<String, Object>();
			        messageMap.put("nickname", nickName);
			        messageMap.put("portrait", UserService.getUserInfoV2(userId).getPortrait());
			        messageMap.put("richLevel", com.melot.kkcx.service.UserService.getRichLevel(userId));
			        messageMap.put("propList", com.melot.kkcx.service.UserService.getUserProps(userId));
			        messageMap.put("content", content);
			        messageMap.put("userId", userId);
			        messageMap.put("roomId", roomId);
			        messageMap.put("href", href);
			        messageMap.put("time", System.currentTimeMillis());
			        messageMap.put("appId", appId);
			        
			        RoomInfo roomInfo = com.melot.kktv.service.RoomService.getRoomInfo(roomId);
			        if (roomInfo != null) {
			            if (roomInfo.getScreenType() != null) {
			                messageMap.put("screenType", roomInfo.getScreenType());
			            }
			            if (roomInfo.getRoomSource() != null) {
			                messageMap.put("roomSource", roomInfo.getRoomSource());
			            } else {
			                messageMap.put("roomSource", AppIdEnum.AMUSEMENT);
			            }
			            if (roomInfo.getType() != null) {
			                messageMap.put("roomType", roomInfo.getType());
			            } else {
			                messageMap.put("roomType", AppIdEnum.AMUSEMENT);
			            }
			        }
			        
			        JsonArray array = new JsonArray();
			        array.add(new Gson().toJsonTree(messageMap));
			        String message = "{\"MsgTag\":50010101,\"MsgList\":" + array.toString() +"}";
			        com.melot.kkcx.service.GeneralService.sendMsgToRoom(1, 0, 0, 0, appId, message);
	            }
			    
			    result.addProperty("state", state);
				result.addProperty("money", com.melot.kktv.service.UserService.getUserShowMoney(userId));
				result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			}
		} catch (Exception e) {
			if (sendType == 1) {
				//发送喇叭存储过程执行异常，将扣除的券加1
				ret = ticketService.insertSendTicket(userId, ticketId, GiftPackageEnum.TICKET_SEND, 1, "用户" + userId + "发送喇叭操作异常退回已扣除喇叭券", 0);
				if (!ret) {
					logger.error("用户:" + userId + "，发送喇叭异常，券" + ticketId + "，已扣除1张，退回失败");
				}
			} else if (sendType == 0 && costMoney > 0) {
				if (vipFlag == true && count != null && count < 3) {
				    vipService.setSvipDailyLoudspeakerCount(userId, -1);
				}
				// 发送喇叭存储过程失败，将扣除的秀币返还
				showMoneyLogger.info("Failed: 发送喇叭失败，续返还秀币{userId: " + userId + ", showMoney: " + costMoney);
                logger.error("执行过程中发生异常,返还已扣除的秀币, userId " + userId + " showMoney : " + costMoney);
			}
			
			logger.error("未能正常调用存储过程", e);
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
		}

		return result;
	}
	
	/**
	 * 获取联通免流量访问地址(20000004)
	 * @param jsonObject
	 * @return
	 * @throws Exception
	 */
	public JsonObject getChinaUnicomFreeFlowAccessUrl(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		
		// 安全sv验证
		JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
		if(rtJO != null) return rtJO;
		
		// 定义使用的参数
		String usermob = null;
		String reqIp = null;
		List<String> flowurls = null;
		// 定义返回结果
		JsonObject result = new JsonObject();
		// 解析参数
		try {
			// 获取IP	
			reqIp = convert3GIP(CommonUtil.getIpAddr(request));
			if (reqIp == null) {
				// 无效ip地址
				result.addProperty("TagCode", "20040007");
				return result;
			}
			String encryptUsermob = CommonUtil.getJsonParamString(jsonObject, "usermob", null, "20040001", 1, 64);
			try {
				usermob = DesUtil.decode(
						encryptUsermob,
						ConfigHelper.getChinaUnicomPassword());
			} catch (Exception e) {}
			if (usermob == null) {
				// usermob解密失败
				result.addProperty("TagCode", "20040004");
				return result;
			}
			
			String flowurl = CommonUtil.getJsonParamString(jsonObject, "flowurl", null, "20040002", 1, 500);
			try {
				TypeToken<List<String>> typeToken = new TypeToken<List<String>>(){};
				flowurls = new Gson().fromJson(flowurl, typeToken.getType());
			} catch (Exception e) {
				// flow解析错误
				result.addProperty("TagCode", "20040006");
				return result;
			}
		} catch (ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		}
		// 判断usermob是否已经订购
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("usermob", usermob);
			map.put("spid", ConfigHelper.getChinaUnicomSpid());
			map.put("ordertype", ChinaUnicomEnum.ORDER_TYPE_MONTH);
			ResCuSpOrder resCuSporder = (ResCuSpOrder) SqlMapClientHelper.getInstance(DB.MASTER)
					.queryForObject("Other.selectResCuSpOrder", map);
			if (resCuSporder != null) {
				if (resCuSporder.getType() == 0 || (resCuSporder.getType() == 1
						&& resCuSporder.getEndtime().getTime() > System.currentTimeMillis())) {
					// 主播编号为偶数取第一个地址 奇数取第二个地址
					List<String> freeurls = new ArrayList<String>();
					LiveStreamConfigService liveStreamConfigService = (LiveStreamConfigService) MelotBeanFactory.getBean("liveStreamConfigService");
					List<String> urls = liveStreamConfigService.getFreeUnicomServer(flowurls);
					for (String url : urls) {
						if (url != null) {
							freeurls.add(url);
						}
					}
					// 用户使用流量信息
					if (resCuSporder.getStatstime() != null && resCuSporder.getFlowbyte() != null) {
						result.addProperty("statstime", resCuSporder.getStatstime());
						result.addProperty("flowbyte", resCuSporder.getFlowbyte());
					}
					
					result.addProperty("TagCode", TagCodeEnum.SUCCESS);
					result.add("freeurl", new JsonParser().parse(new Gson().toJson(freeurls)).getAsJsonArray());
				} else {
					result.addProperty("TagCode", "20040005");
				}
			} else {
				result.addProperty("TagCode", "20040005");
			}
		} catch (Exception e) {
			logger.error("fail to excute sql, usermob " + usermob, e);
			result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
		}
		
		return result;
	}
	
	/**
	 * 查询是否联通3G及手机伪码订购关系(20000005)
	 * @param jsonObject
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public JsonObject getChinaUnicomSpOrderState(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		
		// 定义返回结果
		JsonObject result = new JsonObject();
		
		// 选填
		String usermob = null;
		String reqIp = null;
		int openLimit = 1;
		int platform = PlatformEnum.ANDROID;
		// 解析参数
		try {
			// 获取IP	
			reqIp = convert3GIP(CommonUtil.getIpAddr(request));
			if (reqIp == null) {
				// 无效ip地址
				result.addProperty("TagCode", "20050002");
				return result;
			}
			String encryptUsermob = CommonUtil.getJsonParamString(jsonObject, "usermob", null, null, 1, 64);
			if (encryptUsermob != null) {
				try {
					usermob = DesUtil.decode(
							encryptUsermob,
							ConfigHelper.getChinaUnicomPassword());
				} catch (Exception e) {}
				if (usermob == null) {
					// usermob解密失败
					result.addProperty("TagCode", "20050001");
					return result;
				}
			}
			// openLimit 是否省份限制
			openLimit = CommonUtil.getJsonParamInt(jsonObject, "openLimit", 1, null, 0, 1);
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.ANDROID, null, 1, Integer.MAX_VALUE);
		} catch (ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		}
		
		try {
			// 验证是否联通3G
			Map<String, Object> map1 = new HashMap<String, Object>();
			map1.put("reqIp", reqIp);
			map1.put("openLimit", openLimit);
			map1.put("platform", platform);
			Integer ret = (Integer) SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Other.isCu3gUser", map1);
			if (ret != null && ret.intValue() > 0) {
				result.addProperty("is3g", 1);// 是3G
			} else {
				result.addProperty("is3g", 0);// 非3G
			}
			
			// 若手机伪码不为空,查询该手机伪码订购关系
			if (usermob != null) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("usermob", usermob);
				map.put("spid", ConfigHelper.getChinaUnicomSpid());
				map.put("ordertype", ChinaUnicomEnum.ORDER_TYPE_MONTH);
				ResCuSpOrder resCuSporder = (ResCuSpOrder) SqlMapClientHelper.getInstance(DB.MASTER)
						.queryForObject("Other.selectResCuSpOrder", map);
				if (resCuSporder != null) {
					int type = resCuSporder.getType();
					// 失效时间
					// 当type=1时不为空
					// 当type=0，ordertype=0、2、3时，不为空，ordertype=1时，为空
					if (!(resCuSporder.getType() == 0 && resCuSporder.getOrdertype() == 1)) {
						// 退订关系下返回sp业务到期时间
						result.addProperty("spDeadline", resCuSporder.getEndtime().getTime());
					}
					// TODO
					result.addProperty("spType", 1);
					result.addProperty("spState", type);
					
					// 用户使用流量信息
					if (resCuSporder.getStatstime() != null && resCuSporder.getFlowbyte() != null) {
						result.addProperty("statstime", resCuSporder.getStatstime());
						result.addProperty("flowbyte", resCuSporder.getFlowbyte());
					}
				}
			}
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		} catch (Exception e) {
			logger.error("fail to excute sql, request ip " + reqIp + " usermob " + usermob, e);
			result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
		}
		
		return result;
	}
	
	/**
	 * IP转化:去除分隔符.,第2,3,4段不足3位用0补全
	 * @param inIp
	 * @return outIp
	 */
	private static String convert3GIP(String inIp) {
		String outIp = null;
		if (inIp != null && inIp.indexOf(".") > 0) {
			String[] ipArr = inIp.split("\\.");
			if (ipArr.length == 4) {
				outIp = ipArr[0].trim();
				for (int i = 1; i < 4; i++) {
					if (ipArr[i].trim().length() == 1) {
						outIp = outIp + "00" + ipArr[i].trim();
					} else if (ipArr[i].trim().length() == 2) {
						outIp = outIp + "0" + ipArr[i].trim();
					} else {
						outIp = outIp + ipArr[i].trim();
					}
				}
			}
		}
		return outIp;
	}

	/**
	 *	获取所有可用表情列表及用户购买状态(20000006)
	 */
	public JsonObject getAllEmoList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		if (!checkTag) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
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
		
		List<GalleryInfo> galleryList = null;
		try {
			GalleryInfoService galleryInfoService = MelotBeanFactory.getBean("galleryInfoService", GalleryInfoService.class);
			galleryList = galleryInfoService.getAllAvailableList();
		} catch (Exception e) {
			logger.error("Fail to call GalleryInfoService.getAllAvailableList ", e);
		}	
		List<GalleryOrderRecord> galleryOrderRecordList = null;
		try {
			GalleryOrderRecordService galleryOrderRecordService = MelotBeanFactory.getBean("galleryOrderRecordService", GalleryOrderRecordService.class);
			galleryOrderRecordList = galleryOrderRecordService.getGalleryOrderRecordByUserId(userId);
		} catch (Exception e) {
			logger.error("Fail to call GalleryInfoService.getGalleryOrderRecordByUserId ", e);
		}	
		if (galleryList != null && galleryOrderRecordList != null) {
			JsonArray faceArray = new JsonArray();
			for(GalleryInfo galleryInfo : galleryList) {
				JsonObject json = new JsonObject();
				json.addProperty("facePackId", galleryInfo.getId());
				json.addProperty("name", galleryInfo.getTitle());
				if (galleryInfo.getSubtitle() != null) {
					json.addProperty("subName", galleryInfo.getSubtitle());
				}
				if (galleryInfo.getSmallPic() != null) {
					json.addProperty("icon_60", galleryInfo.getSmallPic());
				}
				if (galleryInfo.getBigPic() != null) {
					json.addProperty("icon_120", galleryInfo.getBigPic());
				}
				if (galleryInfo.getIcon() != null) {
					json.addProperty("icon", galleryInfo.getIcon());
				}
				if (galleryInfo.getDescription() != null) {
					json.addProperty("desc", galleryInfo.getDescription());
				}
				if (galleryInfo.getPrice() != null) {
					json.addProperty("price", galleryInfo.getPrice());
				}
				if (galleryInfo.getSize() != null) {
					json.addProperty("size", galleryInfo.getSize());
				}
				json.addProperty("md5", galleryInfo.getCheckCode());
				json.addProperty("filePath", galleryInfo.getGalleryPath());
				json.addProperty("creatTime", galleryInfo.getCreateTime().getTime());
				if (galleryInfo.getIsShow() != null) {
					json.addProperty("isShow", galleryInfo.getIsShow() );
				}
				int isOrder = 0;
				long buyTime = 0l;
				for (GalleryOrderRecord galleryOrderRecord : galleryOrderRecordList) {
					if (galleryOrderRecord.getFacePackId().equals(galleryInfo.getId())) {
						isOrder = 1;
						buyTime = galleryOrderRecord.getCreateTime().getTime();
						break;
					}
				}
				json.addProperty("isOrder", isOrder);
				json.addProperty("buyTime", buyTime);
				faceArray.add(json);
			}
			result.add("facepacks", faceArray);
			YouPaiService youPaiService = MelotBeanFactory.getBean("youpaiService", YouPaiService.class);
			result.addProperty("picFile", youPaiService.getDomain("gif"));
			result.addProperty("zipFile", youPaiService.getDomain("zip"));
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		} else {
			result.addProperty("TagCode", TagCodeEnum.FAIL_TO_CALL_API_CONTENT_MODULE);
		}
		return result;
	}

	/**
	 * 用户举报接口(20000008)
	 * @return
	 */
	public JsonObject commitReport(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		JsonObject result = new JsonObject();
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		int userId = 0; //举报人
		String nickname = null; //举报人当前昵称
		int toUserId = 0; //被举报人id
		String toNickname = null; //被举报人昵称
		int reportType = 0; //违规类型: 1-淫秽色情; 2-涉黄涉毒; 3-宗教言论; 4-其他
	    int userType = 0;//举报对象类型: 1-主播(房间); 2-用户  
	    String reason = null;//举报原因
	    String evidenceUrls = null; //图片凭证, 多张图片"," 隔开
	    try {
	    	userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
	    	nickname = CommonUtil.getJsonParamString(jsonObject, "nickname", null, TagCodeEnum.NICKNAME_MISSING, 1, 20);
	    	toUserId = CommonUtil.getJsonParamInt(jsonObject, "toUserId", 0, TagCodeEnum.TOUSERID_MISSING, 1, Integer.MAX_VALUE);
	    	toNickname = CommonUtil.getJsonParamString(jsonObject, "toNickname", null, TagCodeEnum.TONICKNAME_MISSING, 1, 20);
	    	reportType = CommonUtil.getJsonParamInt(jsonObject, "reportType", 0, TagCodeEnum.REPORTTYPE_MISSING, 1, Integer.MAX_VALUE);
	    	userType = CommonUtil.getJsonParamInt(jsonObject, "userType", 0, TagCodeEnum.USERTYPE_MISSING, 1, Integer.MAX_VALUE);
	    	reason = CommonUtil.getJsonParamString(jsonObject, "reason", null, null, 1, 50);
	    	evidenceUrls = CommonUtil.getJsonParamString(jsonObject, "evidenceUrls", null, TagCodeEnum.EVIDENCEURLS_MISSING, 1, 500);
	    	//举报类型为其他时reason必填
		    if (reportType == 4 && reason == null) {
		    	result.addProperty("TagCode", TagCodeEnum.REASON_MISSING);
		    	return result;
		    }
	    } catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch (Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
	    
//	    long showMoney = com.melot.kktv.service.UserService.getUserShowMoney(userId);
//	    if (showMoney < Constant.report_money) {
//	    	result.addProperty("TagCode", TagCodeEnum.USER_MONEY_SHORTNESS);
//	    	return result;
//	    }
	    
	    Integer reportId = GeneralService.roomReport(userId, nickname, toUserId, toNickname, reportType, userType, reason, evidenceUrls);
	    if (reportId != null && reportId > 0) {
			// 插流水
			ConsumeService.insertReportHistory(reportId, userId, 0, 0);
			//插入秀币流水
//			ShowMoneyHistory showMoneyHistory = new ShowMoneyHistory();
//            try {
//		        showMoneyHistory.setUserId(userId);
//		        showMoneyHistory.setCount(1);
//		        showMoneyHistory.setDtime(new Date());
//		        showMoneyHistory.setConsumeAmount(Constant.report_money);
//		        showMoneyHistory.setType(26);
//		        showMoneyHistory.setProductDesc("" + reportId);
//		        
//                KkUserService userService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
//                if (userService.addAndGetUserAssets(userId, - Constant.report_money, false, showMoneyHistory) == null) {
//                    showMoneyLogger.info("Failed: 举报扣秀币失败" + new Gson().toJson(showMoneyHistory));
//                }
//            } catch (Exception e) {
//                showMoneyLogger.error("Error: 举报扣秀币失败" + new Gson().toJson(showMoneyHistory));
//                Cat.logError("addAndGetUserAssets failed, userId: " + userId + ", price: -" + Constant.report_money + ", showMoneyHistory: " + new Gson().toJson(showMoneyHistory), e);
//                logger.error("增加ShowMoneyHistory 执行异常", e);
//            }
			logger.info("CommitReport api : userId" + userId + "pay" + Constant.report_money + "to report toUerId" + toUserId);
	    	result.addProperty("TagCode", TagCodeEnum.SUCCESS);
	    } else {
	    	result.addProperty("TagCode", TagCodeEnum.REPORT_ERROR);
	    }
	    
	    return result;
	}

	/**
	 * 提交举报V2 （51090101）
	 * @param jsonObject
	 * @param checkTag
	 * @param request
	 * @return
	 */
	public JsonObject commitReportV2(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		String tagCode_prefix = "51090101";
		JsonObject result = new JsonObject();
		List<Integer> a = Lists.newArrayList();
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		try {
			int userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, tagCode_prefix + "01", 1, Integer.MAX_VALUE);
			String nickname = CommonUtil.getJsonParamString(jsonObject, "nickname", null, tagCode_prefix + "02", 1, 20);
			int toUserId = CommonUtil.getJsonParamInt(jsonObject, "toUserId", 0, tagCode_prefix + "03", 1, Integer.MAX_VALUE);
			String toNickname = CommonUtil.getJsonParamString(jsonObject, "toNickname", null, tagCode_prefix + "04", 1, 20);
			int reportType = CommonUtil.getJsonParamInt(jsonObject, "reportType", 0, null, 1, Integer.MAX_VALUE);
			int reportTag = CommonUtil.getJsonParamInt(jsonObject, "reportTag", 0, tagCode_prefix + "05", 1, 6);
			String reason = CommonUtil.getJsonParamString(jsonObject, "reason", null, null, 1, 50);
			String evidenceUrls = CommonUtil.getJsonParamString(jsonObject, "evidenceUrls", null, null, 1, 500);
			int roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, null, 1, Integer.MAX_VALUE);
			int newsId = CommonUtil.getJsonParamInt(jsonObject, "newsId", 0, null, 1, Integer.MAX_VALUE);
			if(reportTag == 2 && roomId == 0){
				result.addProperty("TagCode", tagCode_prefix + "06");
				return result;
			}
			else if(reportTag == 3 && newsId == 0){
				result.addProperty("TagCode", tagCode_prefix + "07");
				return result;
			}
			ReportFlowService reportFlowService = (ReportFlowService)MelotBeanFactory.getBean("reportFlowService");
			ReportFlowRecord reportFlowRecord = new ReportFlowRecord();
			reportFlowRecord.setUserId(userId);
			reportFlowRecord.setUserName(nickname);
			reportFlowRecord.setBeUserId(toUserId);
			reportFlowRecord.setBeUserName(toNickname);
			reportFlowRecord.setReportTag(reportTag);
			reportFlowRecord.setReportType(reportType);
			reportFlowRecord.setReportReason(reason);
			reportFlowRecord.setEvidenceUrls(evidenceUrls);
			reportFlowRecord.setRoomId(roomId);
			reportFlowRecord.setNewsId(newsId);
			Result<Boolean> flag = reportFlowService.saveReportFlowRecord(reportFlowRecord);
//			Integer reportId = GeneralService.roomReport(userId, nickname, toUserId, toNickname, reportType,1, reason, evidenceUrls);
			if (flag.getCode().equals(CommonStateCode.SUCCESS) && flag.getData()) {
				// 插流水，现在已不用
//				ConsumeService.insertReportHistory(reportId, userId, 0, 0);
				logger.info("CommitReportV2 api : userId" + userId + "to report toUserId" + toUserId);
				result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			} else {
				result.addProperty("TagCode", tagCode_prefix + "08");
			}
			return result;
		} catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch (Exception e) {
			result.addProperty("TagCode", tagCode_prefix + "08");
			return result;
		}
	}

	/**
	 * 获得举报处理结果列表接口(20000009)
	 */
	public JsonObject getCommitRecordList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		JsonObject result = new JsonObject();
		int start, offset;
		try {
			start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, null, 0, Integer.MAX_VALUE);
			offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 10, null, 1, Integer.MAX_VALUE);
		} catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch (Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		
		List<RecordProcessedRecord> recordList = null; 
		try {
			RecordProcessedRecordService recordProcessedRecordService = MelotBeanFactory.getBean("recordProcessedRecordService", RecordProcessedRecordService.class);
			recordList = recordProcessedRecordService.getHistRecordWeiGuiList(start, offset);
		} catch (Exception e) {
			logger.error("Fail to call recordProcessedRecordService.getProcessedRecordList", e);
		}
		
		JsonArray recordArray = new JsonArray();
		
		if (recordList != null) {
			for (RecordProcessedRecord record : recordList) {
				JsonObject json = new JsonObject();
				json.addProperty("userId", record.getBeUserId());
				json.addProperty("nickname", record.getBeUserName());
				json.addProperty("processDesc", record.getReportMemo());
				if(record.getIllegalType() != null) {
					switch(record.getIllegalType()) {
					case 1:
						json.addProperty("recordstr", String.format(Constant.remind_demo, DateUtil.formatDate(record.getEndTime(), null)));
						break;
					case 2:
						json.addProperty("recordstr", String.format(Constant.warn_demo, DateUtil.formatDate(record.getEndTime(), null)));
						break;
					case 3:
						json.addProperty("recordstr", String.format(Constant.limit_demo, DateUtil.formatDate(record.getEndTime(), null)));
						break;
					case 4:
						json.addProperty("recordstr", String.format(Constant.seal_demo, DateUtil.formatDate(record.getEndTime(), null)));
						break;
					case 5:
						json.addProperty("recordstr", String.format(Constant.reduce_money, DateUtil.formatDate(record.getEndTime(), null)));
						break;
					}
				}
				
				//add违规处理结果返回
				if(record.getIllegalType() == 3){ //限播处理才返回
    				if (record.getReliveTime() == null) {
    					json.addProperty("reOpenstr", Constant.REPORT_FOREVER);
    				} else {
    					json.addProperty("reOpenstr", String.format(Constant.REPORT_CANCEL, DateUtil.formatDate(record.getReliveTime(), null)));
    				}
				}
				recordArray.add(json);
			}
		}
		
		result.add("recordList", recordArray);
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		
		return result;
	}
    
 
    /* ----------------------- 申请家族主播流程相关接口 ----------------------- */
    
    /**
     * 20010501-获取申请主播状态信息
     * @param jsonObject
     * @param checkTag
     * @return
     */
    public JsonObject getApplyForActorInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

		JsonObject result = new JsonObject();
		if (!checkTag) {
	        result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
	        return result;
		}
		
		int userId, appId;
		try {
	    	userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
	    } catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch (Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
        
		// 用户无效不能申请
		UserStaticInfo userInfo = UserService.getUserStaticInfoV2(userId);
        if (userInfo == null) {
            result.addProperty("TagCode", TagCodeEnum.USER_NOT_EXIST);
            return result;
        }
        
        // 用户昵称为空不能申请
        if (StringUtil.strIsNull(userInfo.getProfile().getNickName())) {
            result.addProperty("TagCode", TagCodeEnum.NICKNAME_EMPTY);
            return result;
        }
        
        // 游客不能申请
        if(userInfo.getRegisterInfo().getOpenPlatform() == 0 
                || userInfo.getRegisterInfo().getOpenPlatform() == -5) {
            result.addProperty("TagCode", TagCodeEnum.USER_IS_VISITOR);
            return result;
        }
        
        // 黑名单用户不能申请
        if (com.melot.kkcx.service.UserService.blackListUser(userId)) {
            result.addProperty("TagCode", TagCodeEnum.USER_IN_BLACK);
            return result;
        }

        UserApplyActorDO applyActor;
        UserVerifyDO userVerifyDO;
		try {
			applyActor = userApplyActorService.getUserApplyActorDO(userId).getData();
            userVerifyDO = userVerifyService.getUserVerifyDO(userId).getData();
		} catch (Exception e) {
			logger.error("Fail to call ApplyActorService.getApplyActorByActorId ", e);
			result.addProperty("TagCode", TagCodeEnum.FAIL_TO_CALL_API_CONTENT_MODULE);
			return result;
		}	
		
		try {
		    if (userVerifyDO != null && StringUtils.isNotEmpty(userVerifyDO.getCertNo())) {
		        //身份证黑名单不得申请
		        BlacklistService blacklistService = (BlacklistService) MelotBeanFactory.getBean("blacklistService");
		        boolean flag = blacklistService.isIdentityBlacklist(userVerifyDO.getCertNo());
		        if (flag) {
		            result.addProperty("TagCode", TagCodeEnum.IDENTITY_BLACK_LIST);
		            return result;
		        }
		    }
        } catch (Exception e) {
            logger.error("Fail to check user is IdentityBlacklist, userId: " + userId, e);
        }

		// 如果申请 是在30天以上之前创建的，那么过期作废，并删除记录
		if (applyActor != null
		        && applyActor.getStatus() != null 
		        && applyActor.getStatus() < UserApplyActorStatusEnum.BECOME_ACTOR_SUCCESS
		        && DateUtil.addOnField(applyActor.getCreateTime(), Calendar.DATE, 30).getTime() < System.currentTimeMillis()) {
			try {
				if (userApplyActorService.deleteUserApplyActor(userId).getData()) {
					// 过期并删除成功
					result.addProperty("TagCode", TagCodeEnum.APPLY_OUT_DATE_DELETE);
					return result;
				} else {
					logger.error("Fail to call applyActorService.deleteApplyActorByActorId (userId:) " + userId);
				}
			} catch (Exception e) {
				logger.error("Fail to call applyActorService.deleteApplyActorByActorId (userId:) " + userId, e);
			}
			// 过期并删除失败
			result.addProperty("TagCode", TagCodeEnum.APPLY_OUT_DATE_DELETE_FAIL);
			return result;
		}
		
		if (applyActor != null && applyActor.getStatus() != null) {

		    int status = applyActor.getStatus();
			if (status == -1) {
			    // 普通用户如果是家族成员不能申请
	            if (userInfo.getProfile().getIsActor() != 1) {
	                JsonObject tempResult = checkUserIsFamilyMember(userId);
	                if (tempResult != null) {
	                    return tempResult;
	                }
	            }

	            if(userVerifyDO != null) {
                    // -1:实名认证审核驳回
                    result.addProperty("realName", userVerifyDO.getCertName());
                    result.addProperty("identityNumber", userVerifyDO.getCertNo());
                    result.addProperty("identityPictureOnHand", userVerifyDO.getIdPicOnHand());
                    if (userVerifyDO.getIdPicFont() != null) {
                        result.addProperty("identityPictureFont", userVerifyDO.getIdPicFont());
                    }
                    if (userVerifyDO.getIdPicBack() != null) {
                        result.addProperty("identityPictureBack", userVerifyDO.getIdPicBack());
                    }
                    if (userVerifyDO.getGender() != null) {
                        result.addProperty("gender", userVerifyDO.getGender());
                    }
                    if (userVerifyDO.getVerifyMobile() != null) {
                        result.addProperty("mobile", userVerifyDO.getVerifyMobile());
                    }
                }
				if (applyActor.getOperatorId() != null) {
				    result.addProperty("operatorId", applyActor.getOperatorId());
                }
				if (applyActor.getApplyFamilyId() != null) {
                    result.addProperty("familyId", applyActor.getApplyFamilyId());
                }
				if (applyActor.getBrokerageFirmId() != null) {
				    result.addProperty("brokerageFirm", applyActor.getBrokerageFirmId());
				}
			} else {
				// 返回家族信息
			    Integer familyId = null;
			    if (status != UserApplyActorStatusEnum.FAMILY_AUDIT_REJECT
			            && applyActor.getApplyFamilyId() != null) {
			        familyId = applyActor.getApplyFamilyId();
                } else if(userVerifyDO != null) {
                    // 查看统一身份证是否有绑定的家族ID
                    try {
                        familyId = userVerifyService.getFamilyIdByCertNo(userVerifyDO.getCertNo()).getData();
                    } catch (Exception e) {
                    }
                }
			    if (familyId != null && familyId > 0) {
			        FamilyInfo familyInfo = FamilyService.getFamilyInfoByFamilyId(familyId);
			        if (familyInfo != null) {
			            JsonObject applyFamily = new JsonObject();
			            applyFamily.addProperty("familyId", familyInfo.getFamilyId());
			            applyFamily.addProperty("familyName", familyInfo.getFamilyName());
			            result.add("applyFamily", applyFamily);
			        }
                }
				
				if (status == UserApplyActorStatusEnum.FAMILY_AUDIT_PASS) {
				    // 7:家族试播通过，待用户确认协议
				    
				    // 获取分成比例
					Integer distributRate = RoomService.getFamilyActorDistributRate(userId, applyActor.getApplyFamilyId());
				    if (distributRate != null) {
				        result.addProperty("distributRate", distributRate);
				    }
				}
			}
			
			// 状态 -1:实名认证巡管审核驳回 6:家族审核驳回 返回驳回原因
			if ((status == UserApplyActorStatusEnum.TOURING_AUDIT_REJECT || status == UserApplyActorStatusEnum.FAMILY_AUDIT_REJECT)
					&& StringUtils.isNotEmpty(applyActor.getCheckReason())) {
				result.addProperty("checkReason", applyActor.getCheckReason());
			}
			if (applyActor.getUpdateTime() != null) {
				result.addProperty("updateTime", applyActor.getUpdateTime().getTime());
			}

			// 实名认证方式返回 默认 1-巡管认证
            if(userVerifyDO != null) {
                result.addProperty("verifyType", userVerifyDO.getVerifyType() == null ? UserVerifyTypeEnum.TOURING_VERIFY : userVerifyDO.getVerifyType());
            }else {
                result.addProperty("verifyType", UserVerifyTypeEnum.TOURING_VERIFY);
            }
			
			result.addProperty("status", status);
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			return result;
		} else {
	        // 普通用户如果是家族成员不能申请
	        if (userInfo.getProfile().getIsActor() != 1) {
	            JsonObject tempResult = checkUserIsFamilyMember(userId);
	            if (tempResult != null) {
                    return tempResult;
                }
	        }
	        
			//未申请
			result.addProperty("TagCode", TagCodeEnum.HASNOT_APPLY);
			return result;
		}
    }
    
    private JsonObject checkUserIsFamilyMember(int userId) {
        JsonObject result = null;
        
        int joinFamilyId = FamilyService.getUserJoinedFamilyId(userId);
        if (joinFamilyId > 0) {
            FamilyInfo familyInfo = FamilyService.getFamilyInfoByFamilyId(joinFamilyId);
            if (familyInfo != null) {
                result = new JsonObject();
                
                JsonObject applyFamily = new JsonObject();
                applyFamily.addProperty("familyId", familyInfo.getFamilyId());
                applyFamily.addProperty("familyName", familyInfo.getFamilyName());
                result.add("applyFamily", applyFamily);
                result.addProperty("TagCode", TagCodeEnum.MEMBER_CANT_APPLY);
            }
        }
        
        return result;
    }
    
    /**
     * 50001020-申请主播
     * @param jsonObject
     * @param checkTag
     * @return
     */
    public JsonObject applyForActor(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        int userId, gender, appId, operatorId, familyId, isOk;
        String realName, identityId, mobileNum, qqNum, wechatNum, idPicOnHand, idPicFont, idPicBack;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
            realName = CommonUtil.getJsonParamString(jsonObject, "realName", null, TagCodeEnum.REALNAME_MISSING, 1, configService.getIsAbroad() ? 128 : 20);
            identityId = CommonUtil.getJsonParamString(jsonObject, "identityNumber", null, TagCodeEnum.IDENTITY_MISSING, 1, configService.getIsAbroad() ? 64 : 50);
            idPicOnHand =  CommonUtil.getJsonParamString(jsonObject, "identityPictureOnHand", null, TagCodeEnum.IDPICONHAND_MISSING, 1, 200);
            
            gender = CommonUtil.getJsonParamInt(jsonObject, "gender", -1, null, 0, Integer.MAX_VALUE);
            if (gender < 0 && identityId.length() == 18) {
                gender = StringUtil.parseFromStr(identityId.substring(16, 17), 0) % 2;
            }
            mobileNum = CommonUtil.getJsonParamString(jsonObject, "mobile", null, null, 5 , configService.getIsAbroad() ?  32 : 20);
            qqNum =  CommonUtil.getJsonParamString(jsonObject, "qqNumber", null, null, 1, 50);
            wechatNum =  CommonUtil.getJsonParamString(jsonObject, "wechatNumber", null, null, 1, 50);
            
            // 添加 身份证正面照图片（Bang必传） 和 运营ID
            idPicFont =  CommonUtil.getJsonParamString(jsonObject, "identityPictureFont", null, null, 1, 200);
            idPicBack =  CommonUtil.getJsonParamString(jsonObject, "identityPictureBack", null, null, 1, 200);
            operatorId = CommonUtil.getJsonParamInt(jsonObject, "operatorId", 0, null, 1, Integer.MAX_VALUE);
            familyId = CommonUtil.getJsonParamInt(jsonObject, "familyId", 0, null, 1, Integer.MAX_VALUE);
            //特殊时期自由主播暂停申请
            if (familyId == 11222 && configService.getIsCloseFreeApply()) {
                result.addProperty("TagCode", TagCodeEnum.FUNCTAG_UNUSED_EXCEPTION);
                return result;
            }
            isOk = CommonUtil.getJsonParamInt(jsonObject, "isOk", 0, TagCodeEnum.ISOK_MISSIING, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            //不同意相关协议无法申请
            if (isOk != 1) {
                result.addProperty("TagCode", "01200004");
                return result;
            }
            
            //运营不存在
            if (operatorId != 0 && !com.melot.kkcx.service.UserService.isValidOperator(operatorId)) {
                result.addProperty("TagCode", "01200005");
                return result;
            }
            
            //身份证黑名单不得申请
            BlacklistService blacklistService = (BlacklistService) MelotBeanFactory.getBean("blacklistService");
            boolean flag = blacklistService.isIdentityBlacklist(identityId);
            if (flag) {
                result.addProperty("TagCode", TagCodeEnum.IDENTITY_BLACK_LIST);
                return result;
            }
            
            // 用户无效不能申请
            UserStaticInfo userInfo = UserService.getUserStaticInfoV2(userId);
            if (userInfo == null) {
                result.addProperty("TagCode", TagCodeEnum.USER_NOT_EXIST);
                return result;
            }
            
            // 用户昵称为空不能申请
            if (StringUtil.strIsNull(userInfo.getProfile().getNickName())) {
                result.addProperty("TagCode", TagCodeEnum.NICKNAME_EMPTY);
                return result;
            }
            
            // 游客不能申请
            if(userInfo.getRegisterInfo().getOpenPlatform() == 0 || userInfo.getRegisterInfo().getOpenPlatform() == -5) {
                result.addProperty("TagCode", TagCodeEnum.USER_IS_VISITOR);
                return result;
            }
            
            // 黑名单用户不能申请
            if (com.melot.kkcx.service.UserService.blackListUser(userId)) {
                result.addProperty("TagCode", TagCodeEnum.USER_IN_BLACK);
                return result;
            }

            // 家族成员不能申请成为家族主播
            if (FamilyService.isFamilyMember(userId)) {
                result.addProperty("TagCode", TagCodeEnum.MEMBER_CANT_APPLY);
                return result;
            }
            
            // 未绑定手机的用户不能申请
            try {
                KkUserService userService = MelotBeanFactory.getBean("kkUserService", KkUserService.class);
                UserProfile userProfile = userService.getUserProfile(userId);
                if (userProfile == null || userProfile.getIdentifyPhone() == null && !"1".equals(configService.getCloseCheckPhone())) {
                  result.addProperty("TagCode", "01200002");
                  return result;
                } else {
                    mobileNum = userProfile.getIdentifyPhone();
                }
            } catch (Exception e) {
                logger.error("Fail to get KkUserService.getUserProfile. userId: " + userId, e);
            }
            
            if (familyId > 0) {
                FamilyInfo familyInfo = FamilyService.getFamilyInfoByFamilyId(familyId);
                // 判断家族是否存在
                if (familyInfo == null) {
                    result.addProperty("TagCode", TagCodeEnum.FAMILY_ISNOT_EXIST);
                    return result;
                }
            }
            
            List<UserVerifyDO> userVerifyDOS = userVerifyService.getUserVerifyDOsByCertNoOrVerifyMobile(identityId, mobileNum).getData();
            if (userVerifyDOS != null && userVerifyDOS.size() > 0) {
                for (UserVerifyDO userVerifyDO : userVerifyDOS) {

                    int verifyUserId = userVerifyDO.getUserId();
                    UserApplyActorDO userApplyActorDO = userApplyActorService.getUserApplyActorDO(verifyUserId).getData();
                    //巡管审核驳回 或 家族驳回
                    if (userApplyActorDO == null || userApplyActorDO.getStatus() < 0 || userApplyActorDO.getStatus() == 6) {
                        continue;
                    }
                    
                    if (verifyUserId == userId) {
                        result.addProperty("TagCode", TagCodeEnum.HAS_APPLY_PLAY);
                        return result;
                    } else {
                        // 身份证已经存在
                        if (userVerifyDO.getCertNo() != null && userVerifyDO.getCertNo().equals(identityId)) {
                            result.addProperty("TagCode", TagCodeEnum.APPLY_IDNUM_EXISTS);
                            return result;
                        }
                        // 手机号已经存在
                        if (userVerifyDO.getVerifyMobile() != null && mobileNum != null && userVerifyDO.getVerifyMobile().equals(mobileNum)) {
                            result.addProperty("TagCode", TagCodeEnum.APPLY_MOBILE_EXISTS);
                            return result;
                        }
                    }
                }
            }
            
            // 查看同一身份证是否有绑定的家族ID
            Integer bindfamilyId = userVerifyService.getFamilyIdByCertNo(identityId).getData();
            if (bindfamilyId != null) {
                FamilyInfo otherFamilyInfo = FamilyService.getFamilyInfoByFamilyId(bindfamilyId);
                if (otherFamilyInfo != null) {
                    result.addProperty("TagCode", TagCodeEnum.IDENTITY_HAS_FAMILY);
                    result.addProperty("familyId", bindfamilyId);
                    result.addProperty("familyName", otherFamilyInfo.getFamilyName());
                    return result;
                }
            }
            
            UserApplyActorDO oldApplyActor = userApplyActorService.getUserApplyActorDO(userId).getData();
            if (oldApplyActor != null && oldApplyActor.getStatus() != null && oldApplyActor.getStatus() >= 0 && oldApplyActor.getStatus() != 6) {
                result.addProperty("TagCode", TagCodeEnum.HAS_APPLY_PLAY);
                return result;
            }

            KkUserService userService = MelotBeanFactory.getBean("kkUserService", KkUserService.class);
            UserProfile userProfile = userService.getUserProfile(userId);

            if (familyId <= 0) {
                //自由主播 家族id为官方家族id:11222
                familyId = 11222;
            }

            Boolean userApplyActorResult = userApplyActorService.userApplyActor(userId, familyId, UserApplyActorStatusEnum.TOURING_AUDITING, operatorId).getData();
            boolean saveResult = false;
            if(userApplyActorResult) {
                // 更新用户实名认证信息 认证通过
                UserVerifyParam userVerifyParam = new UserVerifyParam();
                userVerifyParam.setUserId(userId);
                userVerifyParam.setVerifyType(UserVerifyTypeEnum.TOURING_VERIFY);
                userVerifyParam.setVerifyStatus(UserVerifyStatusEnum.WAIT_VERIFY);
                userVerifyParam.setCertNo(identityId);
                userVerifyParam.setCertName(realName);

                userVerifyParam.setIdPicFont(idPicFont);
                userVerifyParam.setIdPicBack(idPicBack);
                userVerifyParam.setIdPicOnHand(idPicOnHand);

                userVerifyParam.setVerifyMobile(userProfile.getIdentifyPhone());
                userVerifyParam.setGender(StringUtil.parseFromStr(identityId.substring(16, 17), 0) % 2);
                userVerifyParam.setIdPicStatus(IdPicStatusEnum.NOT_UPLOADED);
                userVerifyParam.setSignElectronicContract(0);

                saveResult = userVerifyService.updateUserVerify(userVerifyParam).getData();
            }


            if (saveResult) {
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            } else {
                result.addProperty("TagCode", TagCodeEnum.FAIL_SAVE_APPLY);
                return result;
            }
        } catch (Exception e) {
            logger.error("Fail to call applyActorService.getApplyActorByActorId", e);
            result.addProperty("TagCode", TagCodeEnum.FAIL_SAVE_APPLY);
        }
        
        return result;
    }
    
    /**
     * 50001022-取消主播申请
     * @param jsonObject
     * @param checkTag
     * @return
     */
    public JsonObject cancelApplyForActor(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        Integer userId = null;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {

            UserApplyActorDO userApplyActorDO = userApplyActorService.getUserApplyActorDO(userId).getData();

            if (userApplyActorDO != null && userApplyActorDO.getStatus() != null) {

                Integer status = userApplyActorDO.getStatus();
                if (status < UserApplyActorStatusEnum.BECOME_ACTOR_SUCCESS) {
                    if (userApplyActorService.deleteUserApplyActor(userId).getData()) {
                        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                        return result;
                    } else {
                        result.addProperty("TagCode", TagCodeEnum.FAIL_TO_DELETE);
                        return result;
                    }
                } else {
                    result.addProperty("TagCode", TagCodeEnum.HAS_CHECKING_PASS);
                    return result;
                }
            } else {
                result.addProperty("TagCode", "01220001");
                return result;
            }
        } catch (Exception e) {
            logger.error("Fail to call applyActorService.getActorApplyStatus", e);
            result.addProperty("TagCode", TagCodeEnum.FAIL_TO_CALL_API_CONTENT_MODULE);
            return result;
        }
        
    }
    
    /**
     * 50001024-确认成为主播
     * @param jsonObject
     * @param checkTag
     * @return
     */
    public JsonObject confirmBecomeActor(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        Integer userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        if (FamilyService.isFamilyMember(userId)) {
            result.addProperty("TagCode", TagCodeEnum.MEMBER_CANT_APPLY);
            return result;
        }
        
        try {

            UserApplyActorDO oldApplyActor = userApplyActorService.getUserApplyActorDO(userId).getData();
            if (oldApplyActor == null) {
                result.addProperty("TagCode", TagCodeEnum.HASNOT_APPLY);
                return result;
            }
            Integer status = oldApplyActor.getStatus();
            if (status == UserApplyActorStatusEnum.FAMILY_AUDIT_PASS) {

                if (userApplyActorService.auditUserApplyActor(userId, UserApplyActorStatusEnum.CONFIRM_FAMILY_INFO, null, null).getData()) {
                    //更新12成功之后自动变为终审通过
                    if (FamilyService.checkBecomeFamilyMember(userId, UserApplyActorStatusEnum.BECOME_ACTOR_SUCCESS, 1)){
                       result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                    } else {
                        result.addProperty("TagCode", TagCodeEnum.FAIL_TO_UPDATE);
                    }
                    return result;
                } else {
                    result.addProperty("TagCode", TagCodeEnum.FAIL_TO_UPDATE);
                    return result;
                }
            } else if (status > UserApplyActorStatusEnum.FAMILY_AUDIT_PASS) {
                result.addProperty("TagCode", TagCodeEnum.HAS_AGREE_SIGNED);
                return result;
            } else {
                result.addProperty("TagCode", TagCodeEnum.HASNOT_PASS_PLAYINGTEST);
                return result;
            }
        } catch (Exception e) {
            logger.error("Fail to call module", e);
            result.addProperty("TagCode", TagCodeEnum.FAIL_TO_CALL_API_CONTENT_MODULE);
            return result;
        }
    }
    
    /**
     * 校验运营是否合法（50001025）
     * @param jsonObject
     * @param checkTag
     * @return
     */
    public JsonObject checkOperator(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int operatorId;
        try {
            operatorId = CommonUtil.getJsonParamInt(jsonObject, "operatorId", 0, "01250001", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        result.addProperty("isValid", com.melot.kkcx.service.UserService.isValidOperator(operatorId) ? 1: 0);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 获取经济公司信息（50001026）
     * 
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getBrokerageFirmInfoList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        try {
            List<BrokerageFirmDO> brokerageFirmInfoList = userApplyActorService.getBrokerageFirmInfoList().getData();
            if (brokerageFirmInfoList != null && brokerageFirmInfoList.size() != 0) {
                JsonArray brokerageFirmArray = new JsonArray();
                for (BrokerageFirmDO brokerageFirm : brokerageFirmInfoList) {
                    JsonObject jsonObj = new JsonObject();
                    jsonObj.addProperty("firmId", brokerageFirm.getFirmId());
                    jsonObj.addProperty("firmName", brokerageFirm.getFirmName());
                    brokerageFirmArray.add(jsonObj);
                }
                result.add("brokerageFirmInfoList", brokerageFirmArray);
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            } else {
                result.addProperty("TagCode", "01260001");
            }
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.FAIL_TO_CALL_API_CONTENT_MODULE);
        }
        
        return result;
    }
    
    
    
    /* ----------------------- 申请家族主播流程相关接口 ----------------------- */
    
    /**
     * 获取动态表情贴图列表（50001016）
     * 
     * @param jsonObject 请求对象
     * @param request 请求对象
     * @return 
     */
    @SuppressWarnings("unchecked")
    public JsonObject getEmoticonList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        
        int platform;
        
        try {
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 2, null, -1, 2);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        List<DynamicEmoticon> emoticonList;
        JsonArray jEmoticonList = new JsonArray();
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("platform", platform);
            emoticonList = (List<DynamicEmoticon>) SqlMapClientHelper.getInstance(DBEnum.KKCX_PG).queryForList("Emoticon.getDynamicEmoticon", map);
        } catch (SQLException e) {
            logger.error("获取动态表情贴图列表(platform:" + platform, e);
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
            return result;
        }
       
        for (DynamicEmoticon dynamicEmoticon : emoticonList) {
            JsonObject jsonObj = new JsonObject();
            jsonObj.addProperty("emoticonId", dynamicEmoticon.geteId());
            if (dynamicEmoticon.geteName() != null) {
                jsonObj.addProperty("emoticonName", dynamicEmoticon.geteName());
            }
            if (dynamicEmoticon.getEmoticonUrl() != null) {
                jsonObj.addProperty("emoticonUrl", dynamicEmoticon.getEmoticonUrl());
            }
            if (dynamicEmoticon.getPreviewUrl() != null) {
                jsonObj.addProperty("previewUrl", dynamicEmoticon.getPreviewUrl());
            }
            if (dynamicEmoticon.getDesc() != null) {
                jsonObj.addProperty("desc", dynamicEmoticon.getDesc());
            }
            if (dynamicEmoticon.getDtime() != null) {
                jsonObj.addProperty("dtime", dynamicEmoticon.getDtime().getTime()); 
            }
            jEmoticonList.add(jsonObj);
        }
        
        result.add("emoticonList", jEmoticonList);
        result.addProperty("TagCode",  TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 获取静态贴纸列表（50001017）
     * 
     * @param jsonObject 请求对象
     * @param request 请求对象
     * @return 
     */
    @SuppressWarnings("unchecked")
    public JsonObject getStickerList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        
        int platform;
        
        try {
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 2, null, -1, 2);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        List<Sticker> stickerList;
        JsonArray jEmoticonList = new JsonArray();
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("platform", platform);
            stickerList = (List<Sticker>) SqlMapClientHelper.getInstance(DBEnum.KKCX_PG).queryForList("Emoticon.getStickerList", map);
        } catch (SQLException e) {
            logger.error("获取静态贴纸列表(platform:" + platform, e);
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
            return result;
        }
       
        for (Sticker sticker : stickerList) {
            JsonObject jsonObj = new JsonObject();
            jsonObj.addProperty("stickerId", sticker.getsId());
            if (sticker.getsName() != null) {
                jsonObj.addProperty("stickerName", sticker.getsName());
            }
            if (sticker.getPreviewUrl() != null) {
                jsonObj.addProperty("previewUrl", sticker.getPreviewUrl()); 
            }
            if (sticker.getsUrl() != null) {
                jsonObj.addProperty("stickerUrl", sticker.getsUrl());
            }
            if (sticker.getDesc() != null) {
                jsonObj.addProperty("desc", sticker.getDesc());
            }
            if (sticker.getDtime() != null) {
                jsonObj.addProperty("dtime", sticker.getDtime().getTime());
            }
            jEmoticonList.add(jsonObj);
        }
        
        result.add("stickerList", jEmoticonList);
        result.addProperty("TagCode",  TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 获取主播开播权限（50001019）
     * 
     * @param jsonObject 请求对象
     * @param request 请求对象
     * @return 
     */
    public JsonObject getActorBroadcastState(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        
        @SuppressWarnings("unused")
        int roomId;
        int actorId;
        
        try {
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 1, null, 1, Integer.MAX_VALUE);
            actorId = CommonUtil.getJsonParamInt(jsonObject, "actorId", 1, TagCodeEnum.ACTORID_MISSING, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            String broadcastTypeStr = SystemConfig.getValue("broadcastAuthority_type", AppIdEnum.AMUSEMENT);
            if (broadcastTypeStr != null) {
                int roomType = ProfileServices.getRoomType(actorId);
                String[] broadcastAllType = broadcastTypeStr.split(",");
                int isBroadcast = 0;
                for (String broadcastType : broadcastAllType) {
                    isBroadcast = isBroadcastByType(broadcastType, roomType);
                    if (isBroadcast < 0) {
                        result.addProperty(broadcastType, isBroadcastBySource(actorId, Integer.valueOf(broadcastType)));
                    } else {
                        result.addProperty(broadcastType, isBroadcast);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("ActorFunction.getActorBroadcastState actorId :" + actorId + "return exception.", e);
            result.addProperty("TagCode", TagCodeEnum.UNCATCHED_EXCEPTION);
            return result;
        }
       
        result.addProperty("TagCode",  TagCodeEnum.SUCCESS);
        return result;
    }
    
    private int isBroadcastBySource(int roomId, int roomSource) {
        int result = 0;
        String roomSourceActorStr = ProfileServices.getRoomSourceActor(roomSource);
        if (roomSourceActorStr != null) {
            String[] roomSourceActors = roomSourceActorStr.split(",");
            for (String ractorId : roomSourceActors) {
                if (roomId == Integer.valueOf(ractorId)) {
                    result = 1;
                    break;
                }
            }
        }
        return result;
    }
    
    private int isBroadcastByType(String broadcastType, int roomType) {
        int result = 0;
        String broadcastTypeStr = SystemConfig.getValue(String.format("broadcastAuthority_%s", broadcastType), AppIdEnum.AMUSEMENT);
        if (broadcastTypeStr != null) {
            String[] broadcastTypeList = broadcastTypeStr.split(",");
            for (String type : broadcastTypeList) {
                if (Integer.valueOf(type) == -1) {
                    result = -1;
                    break;
                } else if (roomType == Integer.valueOf(type)) {
                    result = 1;
                    break;
                }
            }
        }
        return result;
    }
    
    /**
     * iphone/ipad 递交版本接口(10007005)
     * 
     * @param jsonObject 请求对象
     * @return 标记信息
     */
    public JsonObject appleSubmitVersion(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {

        String version;
        int platform, appId, channel, hullId;
        
        JsonObject result = new JsonObject();
        try {
            version = CommonUtil.getJsonParamString(jsonObject, "version", null, null, 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, "07020005", 1, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", 0, null, 1, Integer.MAX_VALUE);
            if (appId == 0) {
                appId = StringUtil.parseFromStr(MelotBeanFactory.getBean("appId", String.class), AppIdEnum.AMUSEMENT);
            }
            channel = CommonUtil.getJsonParamInt(jsonObject, "c", 0, null, 1, Integer.MAX_VALUE);
            if (channel == 0) {
                channel = StringUtil.parseFromStr(MelotBeanFactory.getBean("channelId", String.class), AppChannelEnum.KK);
            }
            hullId = CommonUtil.getJsonParamInt(jsonObject, "b", 0, null, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("appId", appId);
            map.put("channel", channel);
            map.put("platform", platform);
            map.put("hullId", hullId);
            map.put("version", version);
            version = (String) SqlMapClientHelper.getInstance(DBEnum.KKCX_PG).queryForObject("Other.getAppleSubmitVersion", map);
        } catch (SQLException e) {
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
            return result;
        }
        
        result.addProperty("version", version);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 校验敏感词模块【51090201】
     * 
     * @param jsonObject 请求对象
     * @return 标记信息
     */
    public JsonObject checkPhrase(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        String content;
        int userId;
        
        JsonObject result = new JsonObject();
        try {
            content = CommonUtil.getJsonParamString(jsonObject, "content", null, null, 0, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 0, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        if (com.melot.kkcx.service.GeneralService.hasSensitiveWords(userId, content)) {
            result.addProperty("TagCode", "5109020101");
            return result;
        }
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
}

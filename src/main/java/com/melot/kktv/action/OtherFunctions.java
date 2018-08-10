package com.melot.kktv.action;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.dianping.cat.Cat;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import com.melot.family.driver.domain.FamilyInfo;
import com.melot.family.driver.domain.DO.BrokerageFirmDO;
import com.melot.family.driver.domain.DO.UserApplyActorDO;
import com.melot.family.driver.service.UserApplyActorService;
import com.melot.kk.config.api.domain.OpenPageDO;
import com.melot.kk.config.api.service.OpenPageService;
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
import com.melot.kktv.base.Page;
import com.melot.kktv.redis.HotDataSource;
import com.melot.kktv.service.ConfigService;
import com.melot.kktv.service.UserService;
import com.melot.kktv.util.AppChannelEnum;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.Constant;
import com.melot.kktv.util.DBEnum;
import com.melot.kktv.util.DateUtil;
import com.melot.kktv.util.ParameterKeys;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.SecretKeyUtil;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.kktv.util.confdynamic.SystemConfig;
import com.melot.kktv.util.db.SqlMapClientHelper;
import com.melot.module.packagegift.driver.domain.LoudSpeakerHistory;
import com.melot.module.packagegift.driver.domain.RechargePackage;
import com.melot.module.packagegift.driver.service.MallService;
import com.melot.module.packagegift.driver.service.PackageInfoService;
import com.melot.module.packagegift.driver.service.TicketService;
import com.melot.module.packagegift.driver.service.VipService;
import com.melot.module.packagegift.util.GiftPackageEnum;
import com.melot.sdk.core.util.MelotBeanFactory;
import com.melot.video.driver.domain.SearchVideoParams;
import com.melot.video.driver.domain.VideoInfo;
import com.melot.video.driver.service.VideoInfoServiceNew;

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
	
    private static final int SEND_LOUDER_SPEAKER_COST = 10 * 1000;
    
    @Autowired
    private ConfigService configService;
    
    @Resource
    OpenPageService openPageService;

    @Resource
    UserApplyActorService userApplyActorService;

    @Resource
    UserVerifyService userVerifyService;
    
    @Resource
    MallService mallService;

    @Resource
	VideoInfoServiceNew videoInfoServiceNew;
    
    @Resource
    PackageInfoService packageInfoService;

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
			costMoney = 0;
		}
		
		try {
		    LoudSpeakerHistory loudSpeakerHistory = new LoudSpeakerHistory();
		    loudSpeakerHistory.setRoomId(roomId);
		    loudSpeakerHistory.setUserId(userId);
		    loudSpeakerHistory.setContent(content);
		    loudSpeakerHistory.setAmount((int) costMoney);
		    loudSpeakerHistory.setHref(href);
		    loudSpeakerHistory.setNickname(nickName);
		    loudSpeakerHistory.setState(0);
		    loudSpeakerHistory.setTicketId(ticketId);
		    loudSpeakerHistory.setAppId(AppIdEnum.AMUSEMENT);
		    loudSpeakerHistory.setType(1);
		    
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
            
		    int sendCode = mallService.sendMsgToRoom(1, 0, 0, 0, appId, message, 0, loudSpeakerHistory);
		    boolean ret = false;
		    if (sendCode == 0) {
		        result.addProperty("state", 0);
                result.addProperty("money", com.melot.kktv.service.UserService.getUserShowMoney(userId));
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		    } else {
		        logger.error("用户发送喇叭失败，userId：" + userId);
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
                    // 发送喇叭失败，将扣除的秀币返还
                    showMoneyLogger.info("Failed: 发送喇叭失败，需返还秀币{userId: " + userId + ", showMoney: " + costMoney);
                }
                result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
		    }
		} catch (Exception e) {
		    logger.error("发送喇叭异常：", e);
		}

		return result;
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
			if (flag.getCode().equals(CommonStateCode.SUCCESS) && flag.getData()) {
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
						default:
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
    
    /**
     * 获取开屏页【51090202】
     * 
     * @param jsonObject 请求对象
     * @return 开屏页相关信息
     */
    public JsonObject getOpenPage(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
    	JsonObject result = new JsonObject();
    	int platform;
    	try {
			
    	platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, "5109020201", 1, Integer.MAX_VALUE);
		} catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
            return result;
		} catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
    	OpenPageDO openPageDO = openPageService.getOpenPage();
    	if(openPageDO != null){
    		result.addProperty("pageId", openPageDO.getPageId());
    		result.addProperty("startTime", openPageDO.getStartTime().getTime());
    		result.addProperty("endTime", openPageDO.getEndTime().getTime());
    		if(!StringUtil.strIsNull(openPageDO.getPageTitle())){
    			result.addProperty("pageTitle", openPageDO.getPageTitle());
    		}
    		if(!StringUtil.strIsNull(openPageDO.getButtonWord())){
    			result.addProperty("buttonWord", openPageDO.getButtonWord());
    		}
    		if(!StringUtil.strIsNull(openPageDO.getLinkH5Addr())){
    			result.addProperty("linkH5Addr", openPageDO.getLinkH5Addr());
    		} else if(openPageDO.getRoomId() != 0){
    			result.addProperty("linkH5Addr", "http://www.kktv5.com/m/?roomid="+openPageDO.getRoomId());
    		}
    		if(!StringUtil.strIsNull(openPageDO.getPageVideo())){
    			result.addProperty("pageVideo", openPageDO.getPageVideo());
    		} else if(platform == 2 && !StringUtil.strIsNull(openPageDO.getPageImgAndr())){
    			result.addProperty("pageImgAndr", openPageDO.getPageImgAndr());
    		} else if(platform == 3 && !StringUtil.strIsNull(openPageDO.getPageImgIos1()) && !StringUtil.strIsNull(openPageDO.getPageImgIos2()) && !StringUtil.strIsNull(openPageDO.getPageImgIos3())){
    			result.addProperty("pageImgIos1", openPageDO.getPageImgIos1());
    			result.addProperty("pageImgIos2", openPageDO.getPageImgIos2());
    			result.addProperty("pageImgIos3", openPageDO.getPageImgIos3());
    		} else{
    			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
    		}
    	} else{
    		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
    	}
    	result.addProperty("TagCode", TagCodeEnum.SUCCESS);
    	return result;
    }
    
    /**
     * 加密用户登录账户(51090302)
     * 
     * @param jsonObject 请求对象
     * @return 标记信息
     */
    public JsonObject encryptAccount(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        int thirdAppId;
        String token;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, Integer.MIN_VALUE, Integer.MAX_VALUE);
            token = CommonUtil.getJsonParamString(jsonObject, "token", null, null, 1, 500);
            thirdAppId = CommonUtil.getJsonParamInt(jsonObject, "thirdAppId", 0, "5109030201", Integer.MIN_VALUE, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            String thirdAppKey = null;
            if (!ConfigHelper.getThirdAppKey().isEmpty()) {
                thirdAppKey = ConfigHelper.getThirdAppKey().get(String.valueOf(thirdAppId));
            }
            
            if (StringUtil.strIsNull(thirdAppKey)) {
                result.addProperty("TagCode", "5109030202");
                return result;
            }
            
            String userIdString = SecretKeyUtil.encodeDES(Integer.toString(userId), thirdAppKey);
            String tokenString = encryptToken(token, thirdAppKey);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            result.addProperty("userId", userIdString);
            result.addProperty("token", tokenString);
            return result;
        } catch (Exception e) {
            logger.error("【账户加密失败】userId=" + userId + ",token=" + token, e);
            result.addProperty("TagCode", "5105030103");
            return result;
        }
    }
    
    private String encryptToken(String token, String key) {
        if(StringUtil.strIsNull(token)){
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(token.substring(0,2)).append(CommonUtil.md5(token.substring(2) + key));
        return stringBuilder.toString();
    }
    
    /**
     * 获取即开彩banner信息(51090303)
     * 
     * @param jsonObject 请求对象
     * @return 标记信息
     */
    public JsonObject getLotteryInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        int platform, version;
        try {
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 1, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
            version = CommonUtil.getJsonParamInt(jsonObject, "v", 0, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        String lotteryConfig = null;
        if (platform == PlatformEnum.WEB) {
            lotteryConfig = configService.getLotteryWebConfig();
            result = new JsonParser().parse(lotteryConfig).getAsJsonObject();
        } else {
            lotteryConfig= configService.getLotteryAppConfig();
            result = new JsonParser().parse(lotteryConfig).getAsJsonObject();
            if ((platform == PlatformEnum.ANDROID && version >= 128) || (platform == PlatformEnum.IPHONE && version >= 183)) {
                result.addProperty("lotteryBannerUrl", configService.getLotteryAppHalfBanner());
            }
        }
        result.addProperty("lotteryContent", configService.getLotteryContent());
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 获取充值活动banner(51090304)
     * 
     * @param jsonObject 请求对象
     * @return 标记信息
     */
    public JsonObject getChargeBanner(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        JsonObject result = new JsonObject();

        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        boolean isComplete = false;
        if (userId > 0) {
            int count = 0;
            List<RechargePackage> userRechargePackage = packageInfoService.getUserRechargePackageList(userId, AppIdEnum.AMUSEMENT);
            if (!CollectionUtils.isEmpty(userRechargePackage)) {
                for (RechargePackage rechargePackage : userRechargePackage) {
                    if (rechargePackage.getStatus() > 0) {
                        count ++;
                    }
                }
                if (count == 4) {
                    isComplete = true;
                }
            }
        }
        
        if (!isComplete) {
            String chargeBanner = configService.getChargeBanner();
            result = new JsonParser().parse(chargeBanner).getAsJsonObject();
        }
        result.addProperty("isComplete", isComplete ? 1 : 0);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

	/**
	 * 获取主播已发布视频列表(for用户)
	 * 86010001
	 */
	public JsonObject getLaunchedVideoList(JsonObject jsonObject,boolean checkTag, HttpServletRequest request){
		JsonObject result = new JsonObject();
		int actorId = 0;
		int start;
		int num;
		//校验token
		try {
			actorId = CommonUtil.getJsonParamInt(jsonObject, "actorId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
			start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, null, 1, Integer.MAX_VALUE);
			num = CommonUtil.getJsonParamInt(jsonObject, "offset", 10, null, 1, Integer.MAX_VALUE);
		} catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch (Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		// 设置查询参数
		SearchVideoParams params = new SearchVideoParams();
		params.setActorId(actorId);
		params.setStart(start);
		params.setNum(num);
		params.setLowerDuration(configService.getReplayVedioLowerDuration());
		params.setDaysLimit(configService.getReplayVedioLowerDuration());
		Page<VideoInfo> page = null;
		try {
			page = videoInfoServiceNew.getPublishVideoList(params);
		} catch (Exception e) {
			logger.error(String.format("Error:getLaunchedVideoList(jsonObject=%s, checkTag=%s, request=%s)", jsonObject, checkTag, request), e);
			result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
			return result;
		}
		JsonArray videoListArray = new JsonArray();
		if (page != null) {
			long count = page.getCount();
			if (CollectionUtils.isNotEmpty(page.getList())) {
				for (VideoInfo videoInfo : page.getList()) {
					videoListArray.add(videoInfos2Json(videoInfo));
				}
			}
			result.add("videoInfoList", videoListArray);
			result.addProperty("count", count);
		}
		result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
		return result;
	}

	private JsonObject videoInfos2Json(VideoInfo video){
		JsonObject json = new JsonObject();
		if(video.getActorId() != null){
			json.addProperty("actorId", video.getActorId());
		}
		if(video.getCdnType() != null){
			json.addProperty("cdnType", video.getCdnType());
		}
		json.addProperty("duration", video.getDuration());
		if(video.getFileName() != null){
			json.addProperty("fileName", video.getFileName());
		}
		if( video.getFileUrl() != null){
			json.addProperty("fileUrl", video.getFileUrl());
		}
		if( video.getStartTime() != null){
			json.addProperty("startTime", video.getStartTime().getTime());
		}
		if( video.getEndTime() != null){
			json.addProperty("endTime", video.getEndTime().getTime());
		}

		if( video.getTitle() != null){
			json.addProperty("title", video.getTitle());
		}
		if( video.getPoster() != null){
			json.addProperty("poster", video.getPoster());
		}
		json.addProperty("likeCount", video.getLikeCount());
		json.addProperty("hateCount", video.getHateCount());
		json.addProperty("shareTime", video.getShareTime());

		return json;
	}
}

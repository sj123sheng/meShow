package com.melot.kktv.action;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.melot.content.config.domain.GalleryInfo;
import com.melot.content.config.domain.GalleryOrderRecord;
import com.melot.content.config.facepack.service.GalleryInfoService;
import com.melot.content.config.facepack.service.GalleryOrderRecordService;
import com.melot.kkcore.user.api.ShowMoneyHistory;
import com.melot.kktv.service.ConsumeService;
import com.melot.kktv.service.StatisticsServices;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * 用户消费相关接口
 * @author RC
 *
 */
public class ConsumAction {
	
	/** 日志记录对象 */
	private static Logger logger = Logger.getLogger(ConsumAction.class);
    
    private static Logger showMoneyLogger = Logger.getLogger("showMoneyLogger");
	
	/**
	 * 购买表情包(20000007)
	 * @param jsonObject
	 * @return
	 */
	public JsonObject buyFacePack(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		if(!checkTag) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		JsonObject result = new JsonObject();
		int userId = 0;
		int clientPrice = 0;
		int facePackId = 0;
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
			clientPrice = CommonUtil.getJsonParamInt(jsonObject, "clientPrice", 0, null, 0, Integer.MAX_VALUE);
			facePackId = CommonUtil.getJsonParamInt(jsonObject, "facePackId", 0, TagCodeEnum.FACEPACKID_MISSING, 1, Integer.MAX_VALUE);
		} catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch (Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		
		int price = 0;
		try {
			GalleryInfoService galleryInfoService = MelotBeanFactory.getBean("galleryInfoService", GalleryInfoService.class);
			GalleryInfo galleryInfo = galleryInfoService.getGalleryInfoById(facePackId);
			//判断是否存在该表情包
			if(galleryInfo != null && galleryInfo.getPrice() != null) {
				price = galleryInfo.getPrice();
				if (price != clientPrice) {
					result.addProperty("newPrice", price);
					result.addProperty("TagCode", TagCodeEnum.PRICE_CHANGE);
					return result;
				}
			} else {
				result.addProperty("TagCode", TagCodeEnum.UNUSEABLE_FACEPACKID);
				return result;
			}
		
			//非免费判断余额,若余额足则扣款插流水。
			if (price > 0) {
			    long showMoney = com.melot.kktv.service.UserService.getUserShowMoney(userId);
			    if(showMoney < price) {
					result.addProperty("TagCode", TagCodeEnum.SHOWMONEY_LESS);
					return result;
				}
			} 
			//绑定购买记录
			GalleryOrderRecord galleryOrderRecord = new GalleryOrderRecord();
			galleryOrderRecord.setFacePackId(facePackId);
			galleryOrderRecord.setUserId(userId);
			GalleryOrderRecordService galleryOrderRecordService = MelotBeanFactory.getBean("galleryOrderRecordService", GalleryOrderRecordService.class);
			Integer iRet = galleryOrderRecordService.saveGalleryOrderRecord(galleryOrderRecord);
			
			if(iRet > 0) {
				if (price > 0) {
					ConsumeService.insertConsumeHistory(userId, 11, price, 1, "购买表情包", String.format("[%d]%s", galleryInfo.getId(), galleryInfo.getTitle()));
					//入秀币流水记录
					ShowMoneyHistory showMoneyHistory = new ShowMoneyHistory();
					try {
						showMoneyHistory.setUserId(userId);
						showMoneyHistory.setCount(1);
						showMoneyHistory.setDtime(new Date());
						showMoneyHistory.setConsumeAmount(price);
						showMoneyHistory.setType(25);
						showMoneyHistory.setProductDesc("" + facePackId);
						com.melot.kkcore.user.service.KkUserService kkuserService = (com.melot.kkcore.user.service.KkUserService) MelotBeanFactory.getBean("kkUserService");
						long consume = -price;
						if (kkuserService.addAndGetUserAssets(userId, consume, true, showMoneyHistory) == null) {
						    logger.error("addAndGetUserAssets(" + new Gson().toJson(showMoneyHistory) + ") 执行失败");
						    showMoneyLogger.info("Failed: 购买表情包扣秀币失败 " + new Gson().toJson(showMoneyHistory));
                        }
					} catch (Exception e) {
						logger.error("updateUserShowMoney(" + new Gson().toJson(showMoneyHistory) + ") 执行异常", e);
						showMoneyLogger.info("Error: 购买表情包扣秀币异常 " + new Gson().toJson(showMoneyHistory));
					}
				}
				result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			} else if (iRet == -1) {
				result.addProperty("TagCode", TagCodeEnum.REPEAT_BING);
				return result;
			} else {
				result.addProperty("TagCode", TagCodeEnum.BIND_FAIL);
				return result;
			}
		} catch (Exception e) {
			logger.error("Fail to call galleryInfoService.getGalleryInfoById"
					+ "or galleryOrderRecordService.saveGalleryOrderRecord ", e);
			result.addProperty("TagCode", TagCodeEnum.FAIL_TO_CALL_API_CONTENT_MODULE);
		}
		return result;
	}
	
}

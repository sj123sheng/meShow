package com.melot.kktv.action;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import com.melot.kkcx.service.MessageService;
import com.melot.kkcx.service.UserService;
import com.melot.kkcx.transform.TicketTF;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.module.packagegift.driver.domain.EntranceTicket;
import com.melot.module.packagegift.driver.service.EntranceTicketService;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * 类TicketAction.java的实现描述：购票设计--门票action
 * @author ChengQiang 2014年11月4日 上午9:21:07
 */
public class TicketAction {
	
	/**
	 * 日志记录
	 */
	private static Logger logger = Logger.getLogger(TicketAction.class);
		
	/**
	 * 查询门票信息(10005049)
	 * @param jsonObject 请求对象
	 * @return JsonObject 结果字符串
	 */
	public JsonObject getTicketInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		// 返回
		JsonObject result =new JsonObject();
		
		// 获取/验证参数
		Integer ticketId = null;
		Integer platform = null;
		try {
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, PlatformEnum.WEB, PlatformEnum.IPAD);
			ticketId = CommonUtil.getJsonParamInt(jsonObject, "ticketId", 0, TagCodeEnum.TICKETID_MISSING, 1, Integer.MAX_VALUE);
		} catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch (Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		// 如果redis为空，从oracle中查询保存redis
		EntranceTicketService entranceTicketService = (EntranceTicketService) MelotBeanFactory.getBean("entranceTicketService");
		EntranceTicket ticketInfo = entranceTicketService.getTicketById(ticketId, null, 1);
		if (ticketInfo != null) {
			JsonObject ticketJson = TicketTF.ticketToJson(ticketInfo, platform);
			if (ticketJson != null) {
				result.add("ticketInfo", ticketJson);
			}
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		} else {
			// 门票不存在
			result.addProperty("TagCode", TagCodeEnum.TICKET_NOT_EXIST);
		}
		
		return result;
	}
	
	/**
	 * 获取房间门票信息(For Node)(10005058)
	 * 
	 * @return JsonObject 返回
	 */
	public JsonObject getTicketInfoByRoomId(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		// 返回result
		JsonObject result = new JsonObject();
		// 获取/验证参数
		@SuppressWarnings("unused")
        int roomId = 0;
		try {
			roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
		}  catch(CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch(Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
//		EntranceTicketService entranceTicketService = (EntranceTicketService) MelotBeanFactory.getBean("entranceTicketService");
//        EntranceTicket ticketInfo = entranceTicketService.getTicketById(null, roomId, 1);
//		if (ticketInfo != null) {
//			result.add("ticketInfo", TicketTF.ticketToJson(ticketInfo));
//			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
//		} else {
//			// 门票不存在
			result.addProperty("TagCode", TagCodeEnum.TICKET_NOT_EXIST);
//		}
		
		return result;
	}
	
	/**
	 * 购买门票(10005048)
	 * @param jsonObject 参数
	 * @return JsonObject 返回
	 */
	public JsonObject buyTicket(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		// 获取参数
		// 返回result
		JsonObject result =new JsonObject();
		// 用户id
		Integer userId = null;
		// 被赠送人id
		Integer ownerId = null;
		// 门票id
		Integer ticketId = null;
		// 推荐人id
		Integer referrerId = null;
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
			ownerId = CommonUtil.getJsonParamInt(jsonObject, "ownerId", 0, TagCodeEnum.OWNERID_MISSING, 1, Integer.MAX_VALUE);
			ticketId = CommonUtil.getJsonParamInt(jsonObject, "ticketId", 0, TagCodeEnum.TICKETID_MISSING, 1, Integer.MAX_VALUE);
			referrerId = CommonUtil.getJsonParamInt(jsonObject, "referrerId", 0, null, 1, Integer.MAX_VALUE);
		}  catch(CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		}catch(Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		
		EntranceTicketService entranceTicketService = (EntranceTicketService) MelotBeanFactory.getBean("entranceTicketService");
		int tag = entranceTicketService.insertBuyTicket(userId, ownerId, ticketId, referrerId);
		switch (tag) {
		case 0:
		    EntranceTicket ticketInfo = entranceTicketService.getTicketById(ticketId, null, 1);
			// 购票成功
			String nickname = UserService.getUserNickname(userId);
			// 用户获赠门票系统提醒
			boolean flag = MessageService.sendTicketMessage(userId, nickname,
					ticketInfo.getTicketId(), ticketInfo.getTitle(),
					new Date(ticketInfo.getStartTime().getTime()), ticketInfo.getRoomIds());
			if (!flag) {
				logger.error("fail to send user ticket system message");
			}
			result.addProperty("showMoney", com.melot.kktv.service.UserService.getUserShowMoney(userId));
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			break;
		case -1:
			result.addProperty("TagCode", TagCodeEnum.BUY_TICKET_FAILED);
			break;
		case -2:
			result.addProperty("TagCode", TagCodeEnum.BUY_TICKET_FAILED);
			break;
		case -3:
			result.addProperty("TagCode", TagCodeEnum.TICKET_NOT_EXIST);
			break;
		case -4:
			result.addProperty("TagCode", TagCodeEnum.USER_MONEY_SHORTNESS);
			break;
		case -5:
			result.addProperty("TagCode", TagCodeEnum.TICKET_HAS_BOUGHT);
			break;
		case -6:
			result.addProperty("TagCode", TagCodeEnum.TICKET_ACTIVITY_END);
			break;
		case -7:
			result.addProperty("TagCode", TagCodeEnum.MAX_TICKET_COUNT_LIMIT);
			break;
		default:
			logger.error("TicketAction.buyTicket exception, tag : " + tag
					+ " ,userId : " + userId
					+ " ,ownerId : " + ownerId
					+ " ,ticketId : " + ticketId);
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			break;
		}
		
		return result;
	}
	
}

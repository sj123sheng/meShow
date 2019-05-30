package com.melot.kkcx.action;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.melot.kktv.service.ConfigService;
import com.melot.letter.driver.domain.CheckResult;
import com.melot.letter.driver.domain.HistPrivateLetter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.melot.kkcx.model.MsgBody;
import com.melot.kkcx.model.TimContent;
import com.melot.kkcx.service.TimService;
import com.melot.kktv.util.CollectionUtils;
import com.melot.letter.driver.service.PrivateLetterService;
import com.melot.sdk.core.util.MelotBeanFactory;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

public class TimMsgAction extends ActionSupport {

	private static final long serialVersionUID = -8850731119336469628L;
	private Logger logger = Logger.getLogger(TimMsgAction.class);

	private ConfigService configService;

	{
		configService = MelotBeanFactory.getBean("configService", ConfigService.class);
	}

	/**
	 * 腾讯云单聊发送消息回调
	 * 
	 * @return
	 */
	public String timMsgCallback() {
		ActionContext ctx = ActionContext.getContext();
		HttpServletRequest request = (HttpServletRequest) ctx.get(ServletActionContext.HTTP_REQUEST);
		HttpServletResponse response = (HttpServletResponse) ctx.get(ServletActionContext.HTTP_RESPONSE);
		response.setCharacterEncoding("UTF-8");
		response.setHeader("Content-type", "text/html;charset=UTF-8");
		PrintWriter out = null;
		try {
			out = response.getWriter();
		} catch (IOException e) {
			logger.error("在action中从response获取writer时发生异常.", e);
			return null;
		}

		JsonObject result = new JsonObject();
		if (request == null) {
			result.addProperty("ActionStatus", "FAIL");
			result.addProperty("ErrorCode", 1);
			result.addProperty("ErrorInfo", "request为空");
			out.println(result.toString());
			return null;
		}

		@SuppressWarnings("unchecked")
		Map<String, String[]> params = request.getParameterMap();

		if (params != null && params.size() > 0) {
			StringBuilder queryString = new StringBuilder();
			for (String key : params.keySet()) {
				for (String value : params.get(key)) {
					queryString.append(key).append(":").append(value).append(",");
				}
			}
			logger.info("timMsgCallback parameters:" + queryString + "");
		}

		List<String> msgTypeList = new ArrayList<String>();
		msgTypeList.add("TIMTextElem");
		msgTypeList.add("TIMImageElem");
		msgTypeList.add("TIMCustomElem");

		String callbackCommand = request.getParameter("CallbackCommand");
		String msgBody = "";
		try {
			msgBody = getBodyString(request.getReader());
		} catch (IOException e) {
			logger.error("timMsgCallback:", e);
			result.addProperty("ActionStatus", "FAIL");
			result.addProperty("ErrorCode", 1);
			result.addProperty("ErrorInfo", "获取MogBody错误");
			out.println(result.toString());
			return null;
		}

		if (StringUtils.isEmpty(msgBody)) {
			result.addProperty("ActionStatus", "FAIL");
			result.addProperty("ErrorCode", 1);
			result.addProperty("ErrorInfo", "MsgBody为空");
			out.println(result.toString());
			return null;
		}

		TimContent timContent = new TimContent();
		try {
			timContent = new Gson().fromJson(msgBody, TimContent.class);
		} catch (JsonParseException ex) {
			logger.error("timMsgCallback,Fail to convert json,param:" + msgBody + "", ex);
			result.addProperty("ActionStatus", "FAIL");
			result.addProperty("ErrorCode", 1);
			result.addProperty("ErrorInfo", "json转换错误");
			out.println(result.toString());
			return null;
		}
		if (timContent != null && !CollectionUtils.isEmpty(timContent.getMsgBody())) {
			int type = 9;
			int fromUserId, toUserId;
			boolean isCheck = true;
			
			if (StringUtils.isBlank(callbackCommand)) {
				callbackCommand = StringUtils.isNotBlank(timContent.getCallbackCommand())?timContent.getCallbackCommand():null;
			}
			if (StringUtils.isNotBlank(callbackCommand) && callbackCommand.equalsIgnoreCase("C2C.CallbackAfterSendMsg")) {
				isCheck = false;
			}
			
			for (MsgBody item : timContent.getMsgBody()) {
				if (item.getMsgContent() != null && msgTypeList.contains(item.getMsgType())) {
					Date time = new Date();
					fromUserId = getBangUserId(timContent.getFromAccount());
					toUserId = getBangUserId(timContent.getToAccount());
					
					if (fromUserId >= 100 && isCheck) {

						if(configService.getIsSpecialTime() && item.getMsgType().equalsIgnoreCase("TIMImageElem")){
							result.addProperty("ActionStatus", "FAIL");
							result.addProperty("ErrorCode", configService.getPrivateLetterErrorCode());
							result.addProperty("ErrorInfo", "系统维护中，暂不支持发送图片");
							out.println(result.toString());
							return null;
						}
						
						HistPrivateLetter histPrivateLetter = new HistPrivateLetter();
						histPrivateLetter.setUserId(fromUserId);
						histPrivateLetter.setToUserId(toUserId);
						histPrivateLetter.setLetterTime(new Date());
						histPrivateLetter.setIp(request.getParameter("ClientIP"));
						if (item.getMsgType().equalsIgnoreCase("TIMTextElem")) {
							histPrivateLetter.setLetterType(1);
							histPrivateLetter.setLetterContent(item.getMsgContent().getText());
						}
						else if(item.getMsgType().equalsIgnoreCase("TIMImageElem")){
							histPrivateLetter.setLetterType(2);
							histPrivateLetter.setLetterContent(item.getMsgContent().getImageInfoArray().get(0).getUrl());
						}
						else {
							histPrivateLetter.setLetterType(3);
						}

						CheckResult checkResult = TimService.checkPrivateLetterWords(histPrivateLetter);
						if(checkResult!= null){
							result.addProperty("ActionStatus", checkResult.getActionStatus());
							result.addProperty("ErrorCode", checkResult.getErrorCode());
							result.addProperty("ErrorInfo", checkResult.getErrorInfo());
							out.println(result.toString());
							return null;
						}
//						String resultCode = TimService.checkPrivateLetterWords(fromUserId, toUserId);
//						if (!"0".equals(resultCode)) {
//							result.addProperty("ActionStatus", "OK");
//							result.addProperty("ErrorCode", ErrorCode);
//							result.addProperty("ErrorInfo", resultCode);
//							out.println(result.toString());
//							return null;
//						}
//
//						// 文本关键字校验
//						if (item.getMsgType().equalsIgnoreCase("TIMTextElem")) {
//							if(TimService.hasSensitiveWords(fromUserId, toUserId, item.getMsgContent().getText())) {
//								result.addProperty("ActionStatus", "FAIL");
//								result.addProperty("ErrorCode", 120002);
//								result.addProperty("ErrorInfo", "内容包含敏感信息，发送失败");
//								out.println(result.toString());
//								return null;
//							}
//						}
					}
					
					// 刷新私信列表
                    if (!isCheck) {
                        TimService.pushMsg(10, fromUserId, toUserId, type, time, item.getMsgContent().getText());
                        try {
                            // 调用模块接口
                            PrivateLetterService privateLetterService = (PrivateLetterService)MelotBeanFactory.getBean("privateLetterService");
                            privateLetterService.refreshPrivateSession(toUserId, fromUserId);
                        } catch (Exception e) {
                            logger.error("timMsgCallback execute refreshPrivateSession catched exception", e);
                        }                        
                    }
				}
			}
		} else {
			result.addProperty("ActionStatus", "FAIL");
			result.addProperty("ErrorCode", 1);
			result.addProperty("ErrorInfo", "MsgBody为空");
			out.println(result.toString());
			return null;
		}
		result.addProperty("ActionStatus", "OK");
		result.addProperty("ErrorCode", 0);
		result.addProperty("ErrorInfo", "");
		out.println(result.toString());
		return null;
	}

	private String getBodyString(BufferedReader br) {
		if (br == null) {
			return "";
		}
		String inputLine;
		String str = "";
		try {
			while ((inputLine = br.readLine()) != null) {
				str += inputLine;
			}
			br.close();
		} catch (IOException e) {
			logger.error("timMsgCallback:", e);
		}
		return str;
	}

	private int getBangUserId(String account) {
		if (StringUtils.isEmpty(account)) {
			return 0;
		}
		try {
			return Integer.parseInt(account.replace("bang_", ""));
		} catch (NumberFormatException ex) {
			logger.info("timMsgCallback,getBangUserId,account:" + account + "", ex);
			return 0;
		}
	}
	
}
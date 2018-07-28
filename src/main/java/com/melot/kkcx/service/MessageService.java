package com.melot.kkcx.service;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import com.melot.kktv.redis.HotDataSource;
import com.melot.sdk.core.util.MelotBeanFactory;

public class MessageService {
	
	/** 日志记录对象 */
	private static Logger logger = Logger.getLogger(MessageService.class);
	
	/**
	 * 勋章过期-系统提醒
	 * @param noticeIds
	 * @param userId
	 * @param familyId
	 * @param familyName
	 * @return
	 */
	public static int genMedalInvalidMsg(List<Integer> noticeIds, int userId, int familyId, String familyName) {
		int result = 0;
		if(userId == 0 || noticeIds.isEmpty()) {
			result = -1;
		}
		String nickName = HotDataSource.getHotFieldValue(String.valueOf(userId), "nickname");
		String desc = "由于您被" + nickName + "移出" + familyName + "家族，您的家族勋章已经失效。";
		com.melot.common.driver.service.MessageService messageService 
		    = (com.melot.common.driver.service.MessageService) MelotBeanFactory.getBean("messageService");

		if(!CollectionUtils.isEmpty(noticeIds)){
			for(Integer item : noticeIds){
				try {
					messageService.addSystemMessage(item, 0, 8, "勋章失效提醒", desc);
				} catch (Exception e) {
					logger.error("messageService.addSystemMessage(userId = " + userId
							+ ", refId = 0, type = 8, title = \"勋章失效提醒\", desc" + desc
							+ ")", e);
				}
			}
		}
		return result;
	}

}

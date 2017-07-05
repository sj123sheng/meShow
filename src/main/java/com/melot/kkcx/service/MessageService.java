package com.melot.kkcx.service;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.melot.kktv.redis.HotDataSource;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;
import com.melot.kktv.util.message.Message;
import com.melot.kktv.util.message.MessagePersistent;

public class MessageService {
	
	/** 日志记录对象 */
	private static Logger logger = Logger.getLogger(MessageService.class);
	
	/**
	 * 新注册用户-系统提醒
	 * @param jsonObject
	 * @param userId
	 * @param userName
	 * @return
	 */
	public static int genNewRegisteredUserMsg(JsonObject jsonObject, int userId, String userName){
		if(userId == 0 )//|| userName == null
			return -1;
		
		//check if this is a safe registration.
		JsonElement FuncTagje = jsonObject.get("FuncTag");
		if (FuncTagje == null)
			return -1;
		int funcTag = 0;
		try {
			funcTag = FuncTagje.getAsInt();
		} catch (Exception e) {
		}
		if(funcTag == 0 || funcTag < 40000000)
			return -1;

		String desc;
		if(userName != null){
			desc = "HI，"+ userName +"，欢迎来到KK唱响大家庭！以后看到有新消息提醒也要进来看看哦，也许会有意外惊喜等着您！";
		}else{
			desc = "HI，欢迎来到KK唱响大家庭！以后看到有新消息提醒也要进来看看哦，也许会有意外惊喜等着您！";
		}
		//1. insert into systemmessage table
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("userId", userId);
		map.put("desc", desc);
		map.put("title", "恭喜您注册成功");
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("UserMessage.addSystemMessage", map);
		} catch (SQLException e) {
			logger.error("未能正常调用存储过程 UserMessage.addSystemMessage", e);
			return -2;
		}
		String TagCode = (String) map.get("TagCode");
		if (TagCode.equals(TagCodeEnum.SUCCESS)) {
			//2. insert into message system
			Message msg = new Message();
			msg.setContext(0);
			msg.setFrom(0);
			msg.setMsgtime((new Date()).getTime());
			msg.setTo(userId);
			msg.setMessage(desc);
			msg.setType(Message.MSGTYPE_SYSTEM);
			MessagePersistent.addMessage(msg);
			return 0;
		} else {
			logger.error("调用存储过程未的到正常结果 UserMessage.addSystemMessage,TagCode:" + TagCode + ",userId:" + userId);
			return -3;
		}
	}
	
	/**
	 * 初始密码-系统提醒 
	 */
	public static int genDefPwdNotice(JsonObject jsonObject, int userId, String userName){
		if(userId == 0 )//|| userName == null
			return -1;
		
		//check if this is a safe registration.
		JsonElement FuncTagje = jsonObject.get("FuncTag");
		if (FuncTagje == null)
			return -1;
		int funcTag = 0;
		try {
			funcTag = FuncTagje.getAsInt();
		} catch (Exception e) {
		}
		if(funcTag == 0 || funcTag < 40000000)
			return -1;

		String desc = "尊敬的用户，您好！您的ID号为" + userId + "，初始密码为[******]。您可以使用ID号进行登录。请及时在“设置-修改密码”中修改您的初始密码，以保证您的账户安全。";
		
		//1. insert into systemmessage table
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("userId", userId);
		map.put("desc", desc);
		map.put("title", "初始密码");
		map.put("type", Message.MSGTYPE_DEFPWD);
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("UserMessage.addSystemMessage", map);
		} catch (SQLException e) {
			logger.error("未能正常调用存储过程 UserMessage.addSystemMessage", e);
			return -2;
		}
		String TagCode = (String) map.get("TagCode");
		if (TagCode.equals(TagCodeEnum.SUCCESS)) {
			//2. insert into message system
			Message msg = new Message();
			msg.setContext(0);
			msg.setFrom(0);
			msg.setMsgtime((new Date()).getTime());
			msg.setTo(userId);
			msg.setMessage(desc);
			msg.setType(Message.MSGTYPE_SYSTEM);
			MessagePersistent.addMessage(msg);
			return 0;
		} else {
			logger.error("调用存储过程未的到正常结果 UserMessage.addSystemMessage,TagCode:" + TagCode + ",userId:" + userId);
			return -3;
		}
	}
	
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
		if(userId == 0 || noticeIds.isEmpty())
			result = -1;
		String nickName = HotDataSource.getHotFieldValue(String.valueOf(userId), "nickname");
		String desc = "由于您被" + nickName + "移出" + familyName + "家族，您的家族勋章已经失效。";
		Iterator<Integer> it = noticeIds.iterator(); 
		SqlMapClient smc = SqlMapClientHelper.getInstance(DB.MASTER);
		try {
			smc.startTransaction();
			smc.startBatch();
			while(it.hasNext()){
				// Insert the message into the message system
				Message msg = new Message();
				msg.setContext(0);
				msg.setFrom(0);
				msg.setMsgtime((new Date()).getTime());
				msg.setTo(userId);
				msg.setMessage(desc);
				msg.setType(Message.MSGTYPE_SYSTEM);
				MessagePersistent.addMessage(msg);
				// Add to the batch
				userId = it.next();
				Map<Object, Object> map = new HashMap<Object, Object>();
				map.put("userId", userId);
				map.put("refId", 0);
				map.put("desc", desc);
				map.put("type", 8);
				map.put("title", "勋章失效提醒");
				smc.insert("UserMessage.insertSystemMessageMsg", map);				
			}
			// Execute the batch 
			smc.executeBatch();
			smc.commitTransaction();
		} catch (SQLException e) {
			logger.error("fail to execute UserMessage.insertSystemMessageMsg ", e);
			result = -2;
		} finally {
			try {
				smc.endTransaction();
			} catch (SQLException e) {
				logger.error("fail to end transanction successfully, at [genMedalInvalidMsg] ", e);
			}
		}
		return result;
	}

	/**
	 * 赠送门票-系统提醒
	 * @param userId
	 * @param ticketId
	 * @param desc
	 * @return
	 */
	public static boolean sendTicketMessage(int userId, String nickname,
			int ticketId, String ticketTitle, Date startTime, String roomIds) {
		
		String desc = nickname + "("+userId+")" + "，赠送了您一张" + "“" + ticketTitle + "”" + "门票," + "请于" +
				StringUtil.formatDate(startTime) + ",进入直播间：" + roomIds + "进行观看!";
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("title", "获赠门票提醒");
		map.put("desc", desc);
		map.put("userId", userId);
		map.put("refId", ticketId);
		map.put("type", 12);
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).insert("UserMessage.insertSystemMessageMsg", map);
			// insert into redis message system
			Message msg = new Message();
			msg.setContext(0);
			msg.setFrom(0);
			msg.setMsgtime((new Date()).getTime());
			msg.setTo(userId);
			msg.setMessage(desc);
			msg.setType(Message.MSGTYPE_SYSTEM);
			MessagePersistent.addMessage(msg);
			return true;
		} catch (SQLException e) {
			logger.error("fail to execute UserMessage.insertSystemMessageMsg ", e);
		}
		return false;
	}
	
}

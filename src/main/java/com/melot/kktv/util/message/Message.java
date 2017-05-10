package com.melot.kktv.util.message;

public class Message {
	
	/**
	 * 房间消息
	 */
	public static final int MSGTYPE_ROOM 			= 1;
	/**
	 * KK小秘书
	 */
	public static final int MSGTYPE_KKASSISTANT 	= 2;
	/**
	 * 动态消息
	 */
	public static final int MSGTYPE_DYNAMIC 		= 3;
	/**
	 * 系统消息
	 */
	public static final int MSGTYPE_SYSTEM 			= 4;
	/**
	 * 活动消息
	 */
	public static final int MSGTYPE_ACTIVE 			= 5;
	/**
	 * 新鲜播报消息
	 */
	public static final int MSGTYPE_RECOMMENDED		= 6;
	/**
	 * 默认密码消息
	 */
	public static final int MSGTYPE_DEFPWD			= 7;
	/**
	 * 消息类型最大数值
	 */
	public static final int MSGTYPE_LAST			= 8;
	/**
	 * 点赞消息
	 */
	public static final int MSGTYPE_PRAISE			= 9;
	
	private int type;
	private int count;
	private int from;
	private int to;
	private int context;
	private String message;
	private long msgtime;
	private Integer targetuserid; 
	
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public int getFrom() {
		return from;
	}
	public void setFrom(int from) {
		this.from = from;
	}
	public int getTo() {
		return to;
	}
	public void setTo(int to) {
		this.to = to;
	}
	public int getContext() {
		return context;
	}
	public void setContext(int context) {
		this.context = context;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public long getMsgtime() {
		return msgtime;
	}
	public void setMsgtime(long msgtime) {
		this.msgtime = msgtime;
	}
	public Integer getTargetuserid() {
		return targetuserid;
	}
	public void setTargetuserid(Integer targetuserid) {
		this.targetuserid = targetuserid;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	
	public String toString(){
		return "msgtime:" + getMsgtime() + " from:" + getFrom() + " to:" + getTo() 
				+ " target:" + getTargetuserid() 
				+ " message:" + getMessage();
	}
}

package com.melot.kktv.model;

import java.util.Date;

import com.google.gson.JsonObject;

/**
 * kk系统消息
 * 
 */
public class KkSystemNotice {
	
	private Integer userId;
	private Long id;
	private String title;
	private String describe;
	private Date msgtime;
	private String nickname;
	private Integer type;
	
	/**
	 * 转成JsonObject
	 * 
	 * @return JsonObject
	 */
	public JsonObject toJsonObject(long lastReadTime,int platform) {
		JsonObject jObject = new JsonObject();
		jObject.addProperty("title",this.getTitle());
		jObject.addProperty("id", this.getId());
		jObject.addProperty("type", this.getType());
		jObject.addProperty("describe", this.getDescribe());
		jObject.addProperty("nickname", this.getNickname());
		jObject.addProperty("msgtime", this.getMsgtime().getTime());
		//取得redis时间戳 返回isnew
		if(lastReadTime>0 && lastReadTime <= this.getMsgtime().getTime())
			jObject.addProperty("isnew", 1);
		else 
			jObject.addProperty("isnew", 0);
		return jObject;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public Date getMsgtime() {
		return msgtime;
	}

	public void setMsgtime(Date msgtime) {
		this.msgtime = msgtime;
	}

	public String getDescribe() {
		return describe;
	}
	
	public void setDescribe(String describe) {
		this.describe = describe;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

}

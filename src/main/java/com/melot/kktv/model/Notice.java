package com.melot.kktv.model;

import java.util.Date;

import com.google.gson.JsonObject;

/**
 * 通告类
 * 
 * @author liyue
 * 
 */
public class Notice {
	private Integer noticeId;
	private String title;
	private String noticeURL;
	private Date dtime;
	private String content;

	/**
	 * 转成JsonObject
	 * 
	 * @return JsonObject
	 */
	public JsonObject toJsonObject() {
		JsonObject jObject = new JsonObject();
		jObject.addProperty("noticeId", this.getNoticeId());
		jObject.addProperty("title", this.getTitle());
		if (this.noticeURL == null || this.noticeURL.trim().equals("") || this.noticeURL.trim().equals("#")) {
			//jObject.addProperty("noticeURL", ConfigHelper.getNoticeUrl() + this.getNoticeId());
		} else {
			jObject.addProperty("noticeURL", this.getNoticeURL());
		}
		if (this.getDtime() != null) {
			jObject.addProperty("noticeTime", this.getDtime().getTime());
		}
		if (this.content != null && !this.content.trim().equals("")) {
			jObject.addProperty("content", this.getContent());
		}
		return jObject;
	}

	public Integer getNoticeId() {
		return noticeId;
	}

	public void setNoticeId(Integer noticeId) {
		this.noticeId = noticeId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getNoticeURL() {
		return noticeURL;
	}

	public void setNoticeURL(String noticeURL) {
		this.noticeURL = noticeURL;
	}

	public Date getDtime() {
		return dtime;
	}

	public void setDtime(Date dtime) {
		this.dtime = dtime;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}

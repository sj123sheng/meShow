package com.melot.kktv.model;

import java.io.Serializable;
import java.util.Date;

public class NewsTagRes implements Serializable {
	
	private static final long serialVersionUID = -8637700831670497580L;
	
	private int newsId;
	private int tagId;
	private int aid;
	private Date dtime;
	
	public int getNewsId() {
		return newsId;
	}
	public int getTagId() {
		return tagId;
	}
	public Date getDtime() {
		return dtime;
	}
	public void setNewsId(int newsId) {
		this.newsId = newsId;
	}
	public void setTagId(int tagId) {
		this.tagId = tagId;
	}
	public void setDtime(Date dtime) {
		this.dtime = dtime;
	}
	public int getAid() {
		return aid;
	}
	public void setAid(int aid) {
		this.aid = aid;
	}
}

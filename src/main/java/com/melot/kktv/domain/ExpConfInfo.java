package com.melot.kktv.domain;

import java.util.Date;

public class ExpConfInfo {
	
	private Integer expId;
	
	private String expName;
	
	private String expIn;
	
	private String expOut;
	
	private Date expireTime;
	
	private Date confTime;
	
	private Long limitTime;
	
	public String getExpIn() {
		return expIn;
	}
	
	public void setExpIn(String expIn) {
		this.expIn = expIn;
	}
	
	public String getExpOut() {
		return expOut;
	}
	
	public void setExpOut(String expOut) {
		this.expOut = expOut;
	}
	
	public String getExpName() {
		return expName;
	}
	
	public void setExpName(String expName) {
		this.expName = expName;
	}
	
	public Integer getExpId() {
		return expId;
	}
	
	public void setExpId(Integer expId) {
		this.expId = expId;
	}

	public Date getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(Date expireTime) {
		this.expireTime = expireTime;
	}

	public Date getConfTime() {
		return confTime;
	}

	public void setConfTime(Date confTime) {
		this.confTime = confTime;
	}

    public Long getLimitTime() {
        return limitTime;
    }

    public void setLimitTime(Long limitTime) {
        this.limitTime = limitTime;
    }
	
}

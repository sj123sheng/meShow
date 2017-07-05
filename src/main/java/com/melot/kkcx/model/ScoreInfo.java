package com.melot.kkcx.model;

import java.util.Date;

public class ScoreInfo {
	private Integer actorId;
	private Date starttime;
	private Date endtime;
	private Date nextStarttime;
	private Integer onlineCount;
	private int score;
	public Integer getActorId() {
		return actorId;
	}
	public void setActorId(Integer actorId) {
		this.actorId = actorId;
	}
	public Date getStarttime() {
		return starttime;
	}
	public void setStarttime(Date starttime) {
		this.starttime = starttime;
	}
	public Date getEndtime() {
		return endtime;
	}
	public void setEndtime(Date endtime) {
		this.endtime = endtime;
	}
	public Date getNextStarttime() {
		return nextStarttime;
	}
	public void setNextStarttime(Date nextStarttime) {
		this.nextStarttime = nextStarttime;
	}
	public Integer getOnlineCount() {
		return onlineCount;
	}
	public void setOnlineCount(Integer onlineCount) {
		this.onlineCount = onlineCount;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
}

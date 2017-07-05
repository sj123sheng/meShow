package com.melot.kkgame.model;

import java.util.Date;

public class RoomModel {
	
	private Integer roomId;
	
	private Integer actorLevel;
	
	private String roomPoster;
	
	private Integer roomMode;
	
	private Integer liveType;
	
	private Date liveStartTime;
	
	private Date liveEndTime;
	
	private Date nextStartTime;
	
	private String roomTheme;
	
	private Integer roomSource;

	public Integer getRoomId() {
		return roomId;
	}

	public void setRoomId(Integer roomId) {
		this.roomId = roomId;
	}

	public Integer getActorLevel() {
		return actorLevel;
	}

	public void setActorLevel(Integer actorLevel) {
		this.actorLevel = actorLevel;
	}

	public String getRoomPoster() {
		return roomPoster;
	}

	public void setRoomPoster(String roomPoster) {
		this.roomPoster = roomPoster;
	}

	public Integer getRoomMode() {
		return roomMode;
	}

	public void setRoomMode(Integer roomMode) {
		this.roomMode = roomMode;
	}

	public Integer getLiveType() {
		return liveType;
	}

	public void setLiveType(Integer liveType) {
		this.liveType = liveType;
	}

	public Date getLiveStartTime() {
		return liveStartTime;
	}

	public void setLiveStartTime(Date liveStartTime) {
		this.liveStartTime = liveStartTime;
	}

	public Date getLiveEndTime() {
		return liveEndTime;
	}

	public void setLiveEndTime(Date liveEndTime) {
		this.liveEndTime = liveEndTime;
	}

	public Date getNextStartTime() {
		return nextStartTime;
	}

	public void setNextStartTime(Date nextStartTime) {
		this.nextStartTime = nextStartTime;
	}

	public String getRoomTheme() {
		return roomTheme;
	}

	public void setRoomTheme(String roomTheme) {
		this.roomTheme = roomTheme;
	}

	public Integer getRoomSource() {
		return roomSource;
	}

	public void setRoomSource(Integer roomSource) {
		this.roomSource = roomSource;
	}
	
}

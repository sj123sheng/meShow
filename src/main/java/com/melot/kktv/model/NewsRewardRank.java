package com.melot.kktv.model;

public class NewsRewardRank {
	
	Integer userId;
	String nickname;
	String portrait_path_original;
	Integer rcount;
	Integer roomSource;
	
	public Integer getRcount() {
		return rcount;
	}

	public void setRcount(Integer rcount) {
		this.rcount = rcount;
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

	public String getPortrait_path_original() {
		return portrait_path_original;
	}

	public void setPortrait_path_original(String portrait_path_original) {
		this.portrait_path_original = portrait_path_original;
	}

    public Integer getRoomSource() {
        return roomSource;
    }

    public void setRoomSource(Integer roomSource) {
        this.roomSource = roomSource;
    }
	
}

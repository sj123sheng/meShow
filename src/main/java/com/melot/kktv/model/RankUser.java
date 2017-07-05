package com.melot.kktv.model;

public class RankUser implements Comparable<RankUser> {

	private Integer userId;
	
	private Double score;

	@Override
	public int compareTo(RankUser o) {
		return o.getScore().compareTo(this.score) == 0 ? this.getUserId().compareTo(o.userId) : o.getScore().compareTo(this.score);
	}

	public RankUser() {
		super();
	}

	public RankUser(Integer userId, Double score) {
		super();
		this.userId = userId;
		this.score = score;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Double getScore() {
		return score;
	}

	public void setScore(Double score) {
		this.score = score;
	}
}

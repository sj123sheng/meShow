package com.melot.kktv.model;

public class UserGuessInfo {
	
	private Integer guessId;
	private String guessContent;
	private Integer userOpt;
	private String uOptContent;
	private Integer correctOpt;
	private String cOptContent;
	private Integer guessState;
	private Integer costAmount;
	private Integer winAmount;
	private Long endTime;
	
	public Integer getGuessId() {
		return guessId;
	}
	public void setGuessId(Integer guessId) {
		this.guessId = guessId;
	}
	public String getGuessContent() {
		return guessContent;
	}
	public void setGuessContent(String guessContent) {
		this.guessContent = guessContent;
	}
	public Integer getGuessState() {
		return guessState;
	}
	public void setGuessState(Integer guessState) {
		this.guessState = guessState;
	}
	public Integer getWinAmount() {
		return winAmount;
	}
	public void setWinAmount(Integer winAmount) {
		this.winAmount = winAmount;
	}
	public Long getEndTime() {
		return endTime;
	}
	public void setEndTime(Long endTime) {
		this.endTime = endTime;
	}
	public Integer getUserOpt() {
		return userOpt;
	}
	public void setUserOpt(Integer userOpt) {
		this.userOpt = userOpt;
	}
	public String getuOptContent() {
		return uOptContent;
	}
	public void setuOptContent(String uOptContent) {
		this.uOptContent = uOptContent;
	}
	public Integer getCorrectOpt() {
		return correctOpt;
	}
	public void setCorrectOpt(Integer correctOpt) {
		this.correctOpt = correctOpt;
	}
	public String getcOptContent() {
		return cOptContent;
	}
	public void setcOptContent(String cOptContent) {
		this.cOptContent = cOptContent;
	}
	public Integer getCostAmount() {
		return costAmount;
	}
	public void setCostAmount(Integer costAmount) {
		this.costAmount = costAmount;
	}
	
}

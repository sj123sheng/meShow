/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.model;

import java.util.Date;

import com.google.gson.JsonObject;

/**
 * Title: UserGamble
 * <p>
 * Description:
 * </p>
 * 
 * @author 王康鹏<a href="mailto:kangpeng.wang@melot.cn">
 * @version V1.0
 * @since 2015年9月16日 下午2:46:26
 */
public class UserGamble {

	/**
	 * 记录id
	 */
	private int histId;
	/**
	 * 用户id
	 */
	private int userId;
	/**
	 * 押注id
	 */
	private int optionId;
	/**
	 * 赌注名称
	 */
	private String optionTitle;
	/**
	 * 盘口id
	 */
	private int gambleId;
	/**
	 * 盘口名称
	 */
	private String gambleTitle;
	/**
	 * 下注金额
	 */
	private int amount;
	/**
	 * 赔率
	 */
	private double betRate;
	/**
	 * 押注成功收益
	 */
	private int winAmount;
	/**
	 * 盘口状态
	 */
	private int gambleState;
	/**
	 * 房间id
	 */
	private int roomId;
	private Date createTime;
	private Date updateTime;

	/**
	 * @return the gambleState
	 */
	public int getGambleState() {
		return gambleState;
	}

	/**
	 * @param gambleState the gambleState to set
	 */
	public void setGambleState(int gambleState) {
		this.gambleState = gambleState;
	}

	/**
	 * @return the histId
	 */
	public int getHistId() {
		return histId;
	}

	/**
	 * @param histId
	 *            the histId to set
	 */
	public void setHistId(int histId) {
		this.histId = histId;
	}

	/**
	 * @return the userId
	 */
	public int getUserId() {
		return userId;
	}

	/**
	 * @param userId
	 *            the userId to set
	 */
	public void setUserId(int userId) {
		this.userId = userId;
	}

	/**
	 * @return the optionId
	 */
	public int getOptionId() {
		return optionId;
	}

	/**
	 * @param optionId
	 *            the optionId to set
	 */
	public void setOptionId(int optionId) {
		this.optionId = optionId;
	}

	/**
	 * @return the optionTitle
	 */
	public String getOptionTitle() {
		return optionTitle;
	}

	/**
	 * @param optionTitle
	 *            the optionTitle to set
	 */
	public void setOptionTitle(String optionTitle) {
		this.optionTitle = optionTitle;
	}

	/**
	 * @return the gambleId
	 */
	public int getGambleId() {
		return gambleId;
	}

	/**
	 * @param gambleId
	 *            the gambleId to set
	 */
	public void setGambleId(int gambleId) {
		this.gambleId = gambleId;
	}

	/**
	 * @return the gambleTitle
	 */
	public String getGambleTitle() {
		return gambleTitle;
	}

	/**
	 * @param gambleTitle
	 *            the gambleTitle to set
	 */
	public void setGambleTitle(String gambleTitle) {
		this.gambleTitle = gambleTitle;
	}

	/**
	 * @return the amount
	 */
	public int getAmount() {
		return amount;
	}

	/**
	 * @param amount
	 *            the amount to set
	 */
	public void setAmount(int amount) {
		this.amount = amount;
	}

	/**
	 * @return the betRate
	 */
	public double getBetRate() {
		return betRate;
	}

	/**
	 * @param betRate
	 *            the betRate to set
	 */
	public void setBetRate(double betRate) {
		this.betRate = betRate;
	}

	/**
	 * @return the winAmount
	 */
	public int getWinAmount() {
		return winAmount;
	}

	/**
	 * @param winAmount
	 *            the winAmount to set
	 */
	public void setWinAmount(int winAmount) {
		this.winAmount = winAmount;
	}

	/**
	 * @return the createTime
	 */
	public Date getCreateTime() {
		return createTime;
	}

	/**
	 * @param createTime
	 *            the createTime to set
	 */
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	/**
	 * @return the updateTime
	 */
	public Date getUpdateTime() {
		return updateTime;
	}

	/**
	 * @param updateTime
	 *            the updateTime to set
	 */
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	/**
	 * @return the roomId
	 */
	public int getRoomId() {
		return roomId;
	}

	/**
	 * @param roomId the roomId to set
	 */
	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}

	public JsonObject toJsonObject() {
		JsonObject jsonObject = new JsonObject();
		if (histId > 0) {
			jsonObject.addProperty("histId", histId);
		}
		if (userId > 0) {
			jsonObject.addProperty("userId", userId);
		}
		if (optionId > 0) {
			jsonObject.addProperty("optionId", optionId);
		}
		if (optionTitle != null) {
			jsonObject.addProperty("optionTitle", optionTitle);
		}
		if (gambleTitle != null) {
			jsonObject.addProperty("gambleTitle", gambleTitle);
		}
		jsonObject.addProperty("amount", amount);
		jsonObject.addProperty("betRate", betRate);
		jsonObject.addProperty("winAmount", winAmount);
		jsonObject.addProperty("gambleState", gambleState);
		jsonObject.addProperty("roomId", roomId);
		if (createTime != null) {
			jsonObject.addProperty("createTime", createTime.getTime());
		}
		if (updateTime != null) {
			jsonObject.addProperty("updateTime", updateTime.getTime());
		}
		return jsonObject;
	}

}

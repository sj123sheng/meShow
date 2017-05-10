/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.model;

import com.google.gson.JsonObject;

/**
 * Title: TODO
 * <p>
 * Description:
 * </p>
 * 
 * @author 王康鹏<a href="mailto:kangpeng.wang@melot.cn">
 * @version V1.0
 * @since 2015年9月16日 下午3:21:48
 */
public class GambleResult {
	/**
	 * 用户id
	 */
	private int userId;
	/**
	 * 投注数
	 */
	private int totalCount;
	/**
	 * 赢局数
	 */
	private int winCount;

	/**
	 * 总收益
	 */
	private int totalWin;
	
	/**
	 * @return the totalWin
	 */
	public int getTotalWin() {
		return totalWin;
	}

	/**
	 * @param totalWin the totalWin to set
	 */
	public void setTotalWin(int totalWin) {
		this.totalWin = totalWin;
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
	 * @return the totalCount
	 */
	public int getTotalCount() {
		return totalCount;
	}

	/**
	 * @param totalCount
	 *            the totalCount to set
	 */
	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	/**
	 * @return the winCount
	 */
	public int getWinCount() {
		return winCount;
	}

	/**
	 * @param winCount
	 *            the winCount to set
	 */
	public void setWinCount(int winCount) {
		this.winCount = winCount;
	}

	public JsonObject toJsonObject(){
		JsonObject jsonObject = new JsonObject(); 
		if(userId>0){
			jsonObject.addProperty("userId", userId);
		}
		jsonObject.addProperty("totalCount", totalCount);
		jsonObject.addProperty("winCount", winCount);
		jsonObject.addProperty("totalWin", totalWin);
		return jsonObject;
	}
}

package com.melot.kktv.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * 类BuyTicketHistory.java的实现描述：购票记录表
 * 
 * @author Administrator 2014年11月11日 下午1:17:48
 */
public class BuyTicketHistory implements Serializable {
	/**
	 * 序列默认序号
	 */
	private static final long serialVersionUID = -7634617588035034595L;
	private Integer histId;
	/**
	 * 购买人ID
	 */
	private Integer userId;
	/**
	 * 门票ID
	 */
	private Integer ticketId;
	/**
	 * 拥有人ID
	 */
	private Integer ownerId;
	/**
	 * 介绍人ID
	 */
	private Integer referrerId;
	/**
	 * 购买时间
	 */
	private Date buyTime;
	/**
	 * 0：赠送 1：购买
	 */
	private Integer ticket_Type;

	public Integer getHistId() {
		return histId;
	}

	public void setHistId(Integer histId) {
		this.histId = histId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getTicketId() {
		return ticketId;
	}

	public void setTicketId(Integer ticketId) {
		this.ticketId = ticketId;
	}

	public Integer getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(Integer ownerId) {
		this.ownerId = ownerId;
	}

	public Integer getReferrerId() {
		return referrerId;
	}

	public void setReferrerId(Integer referrerId) {
		this.referrerId = referrerId;
	}

	public Date getBuyTime() {
		return buyTime;
	}

	public void setBuyTime(Date buyTime) {
		this.buyTime = buyTime;
	}

	public Integer getTicket_Type() {
		return ticket_Type;
	}

	public void setTicket_Type(Integer ticket_Type) {
		this.ticket_Type = ticket_Type;
	}

}

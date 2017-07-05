package com.melot.kktv.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * 类HistTicketOpt.java的实现描述：门票审核记录实体
 * 
 * @author Administrator 2014年11月10日 下午1:16:27
 */
public class HistTicketOpt implements Serializable {
	/**
	 * 序列默认序号
	 */
	private static final long serialVersionUID = 6742478959940476104L;
	/**
	 * 流水id
	 */
	private Integer histId;
	/**
	 * 门票id
	 */
	private Integer ticketId;
	/**
	 * 操作描述
	 */
	private String optDesc;
	/**
	 * 操作类型1.审核通过。2.拒绝。3.终止
	 */
	private String optCoLumn;
	/**
	 * 后台用户账号
	 */
	private Integer aId;
	/**
	 * 修改时间
	 */
	private Date dTime;

	public Integer getHistId() {
		return histId;
	}

	public void setHistId(Integer histId) {
		this.histId = histId;
	}

	public Integer getTicketId() {
		return ticketId;
	}

	public void setTicketId(Integer ticketId) {
		this.ticketId = ticketId;
	}


	public String getOptCoLumn() {
		return optCoLumn;
	}

	public void setOptCoLumn(String optCoLumn) {
		this.optCoLumn = optCoLumn;
	}

	public Integer getaId() {
		return aId;
	}
	public String getOptDesc() {
		return optDesc;
	}

	public void setOptDesc(String optDesc) {
		this.optDesc = optDesc;
	}

	public void setaId(Integer aId) {
		this.aId = aId;
	}

	public Date getdTime() {
		return dTime;
	}

	public void setdTime(Date dTime) {
		this.dTime = dTime;
	}
}

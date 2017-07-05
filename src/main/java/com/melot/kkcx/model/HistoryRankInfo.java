package com.melot.kkcx.model;

import java.io.Serializable;

/**
 * Title: 用户排名以及主播排名信息
 * <p>
 * Description: 
 * </p>
 * 
 * @author 林端端<a href="mailto:duanduan.lin@melot.cn">
 * @version V1.0
 * @since 2017年4月6日 下午16:22:42
 */
public class HistoryRankInfo implements Serializable{

    private static final long serialVersionUID = 5307173220247073592L;

    private Integer userId;
	
	private String name;
	
	private Long amount;
	
	public HistoryRankInfo(Integer userId) {
		super();
		this.userId = userId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getAmount() {
		return amount;
	}

	public void setAmount(Long amount) {
		this.amount = amount;
	}
}

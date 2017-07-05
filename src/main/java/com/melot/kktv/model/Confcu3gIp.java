package com.melot.kktv.model;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * 类UnicomIp.java的实现描述：联通IP地址实体
 * 
 * @author chengqiang 2014年9月22日 下午1:29:07
 */
public class Confcu3gIp implements Serializable {
	/**
	 * 序列编号
	 */
	private static final long serialVersionUID = 7592631975125102535L;
	/**
	 * ip起始段
	 */
	private long ipbegin;
	/**
	 * ip结束段
	 */
	private long ipend;
	/**
	 * 所在身份
	 */
	private String province;
	/**
	 * 号段是否有效 0:无效 1:有效
	 */
	private int isopen;
	/**
	 * 号码段加入时间
	 */
	private Date opertime;

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}
 

	public int getIsopen() {
		return isopen;
	}

	public void setIsopen(int isopen) {
		this.isopen = isopen;
	}

 
	public long getIpbegin() {
		return ipbegin;
	}

	public void setIpbegin(long ipbegin) {
		this.ipbegin = ipbegin;
	}

	public long getIpend() {
		return ipend;
	}

	public void setIpend(long ipend) {
		this.ipend = ipend;
	}

	public Date getOpertime() {
		return opertime;
	}

	public void setOpertime(Date opertime) {
		this.opertime = opertime;
	}

}

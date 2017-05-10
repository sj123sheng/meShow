package com.melot.kktv.model;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * 类UserUnicomOrderer.java的实现描述：用户联通订购关系实体
 * 
 * @author chengqiang 2014年9月22日 下午1:34:34
 */
public class ResCuSpOrder implements Serializable {
	/**
	 * 序列编号
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 流水表主键
	 */
	private int histid;
	/**
	 * 手机伪码
	 */
	private String usermob;
	/**
	 * SP业务ID
	 */
	private String spid;
	/**
	 * 操作类型 0:订购 1:退订
	 */
	private int type;
	/**
	 * 订购类型
		0 按内容订购， 1 包月订购，
		2 体验卡订购， 3 WO卡订购
	 */
	private int ordertype;
	/**
	 * 更新时间
	 */
	private Date dtime;
	/**
	 * 到期时间
	 */
	private Date endtime;
	/**
	 * 用户使用流量统计的时间
	 */
	private String statstime;
	/**
	 * 用户已使用流量
	 */
	private String flowbyte;
	
	public String getUsermob() {
		return usermob;
	}
	public void setUsermob(String usermob) {
		this.usermob = usermob;
	}
	public String getSpid() {
		return spid;
	}
	public void setSpid(String spid) {
		this.spid = spid;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public Date getDtime() {
		return dtime;
	}
	public void setDtime(Date dtime) {
		this.dtime = dtime;
	}
	public Date getEndtime() {
		return endtime;
	}
	public void setEndtime(Date endtime) {
		this.endtime = endtime;
	}
	public int getOrdertype() {
		return ordertype;
	}
	public void setOrdertype(int ordertype) {
		this.ordertype = ordertype;
	}
	public int getHistid() {
		return histid;
	}
	public void setHistid(int histid) {
		this.histid = histid;
	}
	public String getStatstime() {
		return statstime;
	}
	public void setStatstime(String statstime) {
		this.statstime = statstime;
	}
	public String getFlowbyte() {
		return flowbyte;
	}
	public void setFlowbyte(String flowbyte) {
		this.flowbyte = flowbyte;
	}

}

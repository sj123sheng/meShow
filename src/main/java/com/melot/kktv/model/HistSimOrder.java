package com.melot.kktv.model;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * 类UnicomSynchronous.java的实现描述：用户联通订购同步关系表
 * 
 * @author chengqiang 2014年9月22日 下午1:45:51
 */
public class HistSimOrder implements Serializable {
	/**
	 * 默认序号
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 记录ID
	 */
	private int histid;
	/**
	 * 用户手机伪码
	 */
	private String usermob;
	/**
	 * 内容提供商id
	 */
	private String cpid;
	/**
	 * sp业务id
	 */
	private String spid;
	/**
	 * 操作类型 0:订购 1:退订
	 */
	private int type;
	/**
	 * 订购时间
	 */
	private Date ordertime;
	/**
	 * 退订时间 当type为1不为空
	 */
	private Date canceltime;
	/**
	 * 失效时间
	 * 当type=1,则不为空,当type=0,ordertype=0,2,3时,不为空,ordertype=1时,为空
	 */
	private Date endtime;
	/**
	 * 所属省份
	 */
	private int channelcode;
	/**
	 * 用户所属省份
	 */
	private String province;
	/**
	 * 用户所属地市
	 */
	private String area;
	/**
	 * 订购类型：1包月订购，2体验卡订购，3WO卡订购
	 */
	private int ordertype;
	/**
	 * 视频编码
	 */
	private String videoid;
	public int getHistid() {
		return histid;
	}
	public void setHistid(int histid) {
		this.histid = histid;
	}
	public String getUsermob() {
		return usermob;
	}
	public void setUsermob(String usermob) {
		this.usermob = usermob;
	}
	public String getCpid() {
		return cpid;
	}
	public void setCpid(String cpid) {
		this.cpid = cpid;
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
	public Date getOrdertime() {
		return ordertime;
	}
	public void setOrdertime(Date ordertime) {
		this.ordertime = ordertime;
	}
	public Date getCanceltime() {
		return canceltime;
	}
	public void setCanceltime(Date canceltime) {
		this.canceltime = canceltime;
	}
	public Date getEndtime() {
		return endtime;
	}
	public void setEndtime(Date endtime) {
		this.endtime = endtime;
	}
	public int getChannelcode() {
		return channelcode;
	}
	public void setChannelcode(int channelcode) {
		this.channelcode = channelcode;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
	public int getOrdertype() {
		return ordertype;
	}
	public void setOrdertype(int ordertype) {
		this.ordertype = ordertype;
	}
	public String getVideoid() {
		return videoid;
	}
	public void setVideoid(String videoid) {
		this.videoid = videoid;
	}

}

package com.melot.kktv.domain;

import java.io.Serializable;

import com.google.gson.JsonObject;

/**
 * 预告节目的类
 * <p></p>
 * @author fenggaopan 2015年10月21日 下午1:51:36
 * @version V1.0   
 * @modificationHistory=========================逻辑或功能性重大变更记录
 * @modify by user: {修改人} 2015年10月21日
 * @modify by reason:{方法名}:{原因}
 */
public class PreviewAct implements Serializable, Comparable<PreviewAct> {
	
	/**
     * 序列化ID
     */
    private static final long serialVersionUID = 1L;
	private String familyName;   //家族名称
	private Integer familyId ;  //家族Id
	private String actBanner ;  //海报
	private JsonObject familyPoster ;
	private Integer totalNumber ;  //房间人家总数
	private String status ; //直播状态
	private String roomTheme ;//房间主题
	private Integer statusCode ; //状态码
	private Integer  roomId ; 
	
    public Integer getRoomId() {
    	return roomId;
    }
    
    public void setRoomId(Integer roomId) {
    	this.roomId = roomId;
    }

	public Integer getStatusCode() {
    	return statusCode;
    }
	
    public void setStatusCode(Integer statusCode) {
    	this.statusCode = statusCode;
    }

	@Override
    public String toString() {
	    return "PreviewAct [familyName=" + familyName + ", familyId=" + familyId + ", actBanner=" + actBanner
	            + ", totalNumber=" + totalNumber + ", status=" + status + ", roomTheme=" + roomTheme + "]";
    }

	public String getFamilyName() {
    	return familyName;
    }
	
    public void setFamilyName(String familyName) {
    	this.familyName = familyName;
    }
	
    public Integer getFamilyId() {
    	return familyId;
    }
	
    public void setFamilyId(Integer familyId) {
    	this.familyId = familyId;
    }
    
    public String getActBanner() {
    	return actBanner;
    }
	
    public void setActBanner(String actBanner) {
    	this.actBanner = actBanner;
    }
	
    public JsonObject getFamilyPoster() {
    	return familyPoster;
    }
	
    public void setFamilyPoster(JsonObject familyPoster) {
    	this.familyPoster = familyPoster;
    }

	public Integer getTotalNumber() {
    	return totalNumber;
    }
	
    public void setTotalNumber(Integer totalNumber) {
    	this.totalNumber = totalNumber;
    }
	
    public String getStatus() {
    	return status;
    }
	
    public void setStatus(String status) {
    	this.status = status;
    }
	
    public String getRoomTheme() {
    	return roomTheme;
    }
	
    public void setRoomTheme(String roomTheme) {
    	this.roomTheme = roomTheme;
    }

	public int compareTo(PreviewAct o) {
	    // TODO Auto-generated method stub
	    return 0;
    }

}

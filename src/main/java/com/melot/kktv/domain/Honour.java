package com.melot.kktv.domain;

import java.io.Serializable;

import com.google.gson.JsonObject;

/**
 * 家族荣誉对象类
 * <p></p>
 * @author fenggaopan 2015年10月22日 上午11:11:24
 * @version V1.0   
 * @modificationHistory=========================逻辑或功能性重大变更记录
 * @modify by user: {修改人} 2015年10月22日
 * @modify by reason:{方法名}:{原因}
 */
@SuppressWarnings("serial")
public class Honour implements Serializable{
	
	@Override
    public String toString() {
	    return "Honour [familyId=" + familyId + ", familyName=" + familyName + ", familyPoster=" + familyPoster
	            + ", totalCount=" + totalCount + "]";
    }

	private Integer familyId ;  //家族id
	private String  familyName ;  //家族名称
	private JsonObject familyPoster; //家族海报
	private Long  totalCount ;  //荣誉总数
	private String medalTitle ; //勋章主题
	
	public Honour(){}
	
    public Honour(Integer familyId, String familyName, JsonObject familyPoster, Long totalCount ,String medalTitle) {
	    super();
	    this.familyId = familyId;
	    this.familyName = familyName;
	    this.familyPoster = familyPoster;
	    this.totalCount = totalCount;
	    this.medalTitle = medalTitle ;
    }

	public Integer getFamilyId() {
    	return familyId;
    }
	
    public void setFamilyId(Integer familyId) {
    	this.familyId = familyId;
    }
	
    public String getFamilyName() {
    	return familyName;
    }
	
    public void setFamilyName(String familyName) {
    	this.familyName = familyName;
    }
	
    public JsonObject getFamilyPoster() {
    	return familyPoster;
    }
	
    public void setFamilyPoster(JsonObject familyPoster) {
    	this.familyPoster = familyPoster;
    }
	
    public Long getTotalCount() {
    	return totalCount;
    }
	
    public void setTotalCount(Long totalCount) {
    	this.totalCount = totalCount;
    }
    
    public String getMedalTitle() {
        return medalTitle;
    }
    
    public void setMedalTitle(String medalTitle) {
        this.medalTitle = medalTitle;
    }

}

package com.melot.kktv.model;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

public class MedalInfo {
	
	private Integer medalId;
	private String medalTitle;
	private Integer medalType;
	private Integer medalRefId;
	private String MedalIcon;
	private Integer medalLevel ;
	private String medalDesc ;
	public String getMedalDesc() {
		return medalDesc;
	}

	public void setMedalDesc(String medalDesc) {
		this.medalDesc = medalDesc;
	}

	private List<MedalPrice> medalPrice;
	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public Integer getMedalLevel() {
		return medalLevel;
	}

	public void setMedalLevel(Integer medalLevel) {
		this.medalLevel = medalLevel;
	}

	private String icon ;

	public Integer getMedalId() {
		return medalId;
	}

	public String getMedalTitle() {
		return medalTitle;
	}

	public Integer getMedalType() {
		return medalType;
	}

	public Integer getMedalRefId() {
		return medalRefId;
	}

	public String getMedalIcon() {
		return MedalIcon;
	}

	public List<MedalPrice> getMedalPrice() {
		return medalPrice;
	}

	public void setMedalId(Integer medalId) {
		this.medalId = medalId;
	}

	public void setMedalTitle(String medalTitle) {
		this.medalTitle = medalTitle;
	}

	public void setMedalType(Integer medalType) {
		this.medalType = medalType;
	}

	public void setMedalRefId(Integer medalRefId) {
		this.medalRefId = medalRefId;
	}

	public void setMedalIcon(String medalIcon) {
		MedalIcon = medalIcon;
	}

	public void setMedalPrice(List<MedalPrice> medalPrice) {
		this.medalPrice = medalPrice;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}


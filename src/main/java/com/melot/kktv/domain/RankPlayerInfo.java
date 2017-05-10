package com.melot.kktv.domain;

import java.util.List;

public class RankPlayerInfo extends UserInfo {
	
	private Integer rank; //榜单排名
	private Double score; //榜单总分
	private List<RankDimensionInfo> dimensions; //榜单选手各维度信息 
	
	public Integer getRank() {
		return rank;
	}
	public void setRank(Integer rank) {
		this.rank = rank;
	}
	public Double getScore() {
		return score;
	}
	public void setScore(Double score) {
		this.score = score;
	}
	public List<RankDimensionInfo> getDimensions() {
		return dimensions;
	}
	public void setDimensions(List<RankDimensionInfo> dimensions) {
		this.dimensions = dimensions;
	}
	
}
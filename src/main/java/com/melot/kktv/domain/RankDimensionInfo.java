package com.melot.kktv.domain;

public class RankDimensionInfo {

    private int apdId;
    private String dimensionName;
	private long dimensionValue;
	private double dimensionScore;

    public int getApdId() {
        return apdId;
    }
    public void setApdId(int apdId) {
        this.apdId = apdId;
    }
	public String getDimensionName() {
		return dimensionName;
	}
	public void setDimensionName(String dimensionName) {
		this.dimensionName = dimensionName;
	}
	public long getDimensionValue() {
		return dimensionValue;
	}
	public void setDimensionValue(long dimensionValue) {
		this.dimensionValue = dimensionValue;
	}
	public double getDimensionScore() {
		return dimensionScore;
	}
	public void setDimensionScore(double dimensionScore) {
		this.dimensionScore = dimensionScore;
	}
	
}

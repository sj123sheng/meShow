package com.melot.kkcx.model;

import com.google.gson.annotations.SerializedName;

public class ImageInfoArray {
	@SerializedName("Type")
	private int type;
	@SerializedName("Size")
	private int size;
	@SerializedName("Width")
	private int width;
	@SerializedName("Height")
	private int height;
	@SerializedName("URL")
	private String url;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}

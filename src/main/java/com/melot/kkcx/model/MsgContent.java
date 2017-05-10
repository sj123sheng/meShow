package com.melot.kkcx.model;

import java.util.List;
import com.google.gson.annotations.SerializedName;

public class MsgContent {
	@SerializedName("Text")
	private String text;
	@SerializedName("UUID")
	private String uuId;
    @SerializedName("ImageFormat")
	private int imageFormat;
	@SerializedName("ImageInfoArray")
	private List<ImageInfoArray> imageInfoArray;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getUuId() {
		return uuId;
	}

	public void setUuId(String uuId) {
		this.uuId = uuId;
	}

	public int getImageFormat() {
		return imageFormat;
	}

	public void setImageFormat(int imageFormat) {
		this.imageFormat = imageFormat;
	}

	public List<ImageInfoArray> getImageInfoArray() {
		return imageInfoArray;
	}

	public void setImageInfoArray(List<ImageInfoArray> imageInfoArray) {
		this.imageInfoArray = imageInfoArray;
	}
}

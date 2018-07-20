package com.melot.kktv.model;

/**
 * 图片地址包装类
 * 
 * @author Administrator
 * 
 */
@SuppressWarnings("unused")
public class PictureURL {
	private int fileId;
	private String fileName;
	private String path_original;
	private String path_1280;
	private String path_256;
	private String path_128;
	private String path_48;

	public int getFileId() {
		return fileId;
	}

	public void setFileId(int fileId) {
		this.fileId = fileId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getPath_original() {
		return path_original;
	}

	public void setPath_original(String path_original) {
		this.path_original = path_original;
	}

	public String getPath_1280() {
		return path_original + "!1280";
	}

	public void setPath_1280(String path_original) {
		this.path_1280 = path_original + "!1280";
	}

	public String getPath_256() {
		return path_original + "!256";
	}

	public void setPath_256(String path_original) {
		this.path_256 = path_original + "!256";
	}

	public String getPath_128() {
		return path_original + "!128x96";
	}

	public void setPath_128(String path_original) {
		this.path_128 = path_original + "!128x96";
	}

	public String getPath_48() {
		return path_original + "!48";
	}

	public void setPath_48(String path_original) {
		this.path_48 = path_original + "!48";
	}

}

package com.melot.kkcx.functions;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;

import com.google.gson.JsonObject;
import com.melot.content.config.version.domain.VersionInfo;
import com.melot.content.config.version.service.VersionInfoService;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.sdk.core.util.MelotBeanFactory;
import com.opensymphony.xwork2.ActionSupport;

/**
 * 通用action(测试页面使用)
 * 
 * @author LY
 * 
 */
public class CommonAction extends ActionSupport {

	private static final long serialVersionUID = -6039218676168188116L;

	/** 日志记录对象 */
	private static Logger logger = Logger.getLogger(CommonAction.class);

	private File inputFile;
	private String inputFileFileName;
	private Integer versionCode;
	private String versionName;
	private String versionDesc;
	private String versionUrl;
	private Integer versionStatus;
	private Integer versionPlatform;
	private Integer versionAppId;
	private Integer versionHullId;
	private Integer versionChannel;
	private String resMsg;

	private Integer userId;

	public String uploadApk() {
		if (versionName == null || versionName.isEmpty()) {
			resMsg = "请填写正确版本名称!";
			return SUCCESS;
		}
		if (versionDesc == null || versionDesc.isEmpty()) {
			resMsg = "版本描述不能为空!";
			return SUCCESS;
		}
		if (versionPlatform == null || versionPlatform.intValue() <= 0) {
			resMsg = "请选择正确平台编号!";
			return SUCCESS;
		}
		if (versionPlatform == PlatformEnum.ANDROID) {
			if (versionUrl == null || versionUrl.isEmpty()) {
				resMsg = "请填写正确下载地址!";
				return SUCCESS;
			}
		}
		if (versionStatus == null || versionStatus.intValue() <= 0) {
			resMsg = "请选择正确升级方式!";
			return SUCCESS;
		}
		if (versionCode == null || versionCode.intValue() <= 0) {
			resMsg = "请填写正确版本编号!";
			return SUCCESS;
		}
		if (versionAppId == null || versionAppId.intValue() <= 0) {
			resMsg = "请选择正确产品编号!";
			return SUCCESS;
		}
		if (versionHullId == null || versionHullId.intValue() <= 0) {
			versionHullId = 0;
		}
		if (versionChannel == null || versionChannel.intValue() <= 0) {
			versionChannel = 0;
		}
		
		VersionInfo version = new VersionInfo();
		version.setVersionUrl(versionUrl);
		version.setVersionCode(versionCode);
		version.setVersionName(versionName);
		version.setVersionDesc(versionDesc);
		version.setVersionPlatform(versionPlatform);
		version.setVersionStatus(versionStatus);
		version.setVersionAppId(versionAppId);
		version.setVersionHullId(versionHullId);
		version.setVersionChannel(versionChannel);
		try {
			VersionInfoService versionInfoService = MelotBeanFactory.getBean("versionInfoService", VersionInfoService.class);
			Integer versionId = versionInfoService.createVersion(version);
			if (versionId > 0) {
				resMsg = "Successful Create version";
			} else {
				resMsg = "Fail To Create version";
			}
		} catch(Exception e) {
			logger.error("Fail to call versionService.createVersion, version:" + version.toString(), e);
			resMsg = e.getMessage();
		}
		return SUCCESS;
	}

	public String uploadCrash() {

		ServletActionContext.getResponse().setCharacterEncoding("utf-8");
		PrintWriter out = null;
		try {

			out = ServletActionContext.getResponse().getWriter();
		} catch (IOException e) {
			// 记录日志
			logger.error("在action中从response获取writer时发生异常.", e);
			// 本次请求结束
			return null;
		}

		// 定义返回结果
		JsonObject resJson = new JsonObject();

		try {
			// 验证用户名是否可用

			// 判断上传文件是否有漏传或未上传
			if (inputFile == null) {
				resJson.addProperty("ErrorMsg", "Upload file is invalid.");
				// 输出响应结果
				out.write(resJson.toString());
				out.flush();
				out.close();
				// 本次请求结束
				return null;
			}
			// 判断apk文件大小是否符合要求
			if (inputFile.length() > (30 * 1024 * 1024) || inputFile.length() <= 0) {
				resJson.addProperty("ErrorMsg", "Upload file size is invalid.");
				// 输出响应结果
				out.write(resJson.toString());
				out.flush();
				out.close();
				// 本次请求结束
				return null;
			}
			String fileName = userId + "_" + System.currentTimeMillis();
			String crashUrl = CommonUtil.processUploadedFile(inputFile, fileName, ConfigHelper.getCrashSavePath());// 获取savePath
			if (crashUrl != null) {
				resJson.addProperty("TagCode", TagCodeEnum.SUCCESS);
			} else {
				resJson.addProperty("ErrorMsg", "Upload file is failed.");
			}
		} catch (Exception e) {
			logger.error("Exception in action [CommonAction.uploadCrash]: " + e.getMessage());
			// 产生了消息处理函数未捕获的异常,记录到日志了.
			resJson.addProperty("ErrorMsg", "Upload file is failed.");
		}

		// 输出响应结果
		out.write(resJson.toString());
		out.flush();
		out.close();
		// 本次请求结束
		return null;
	}

	public File getInputFile() {
		return inputFile;
	}

	public void setInputFile(File inputFile) {
		this.inputFile = inputFile;
	}

	public String getInputFileFileName() {
		return inputFileFileName;
	}

	public void setInputFileFileName(String inputFileFileName) {
		this.inputFileFileName = inputFileFileName;
	}

	public String getResMsg() {
		return resMsg;
	}

	public void setResMsg(String resMsg) {
		this.resMsg = resMsg;
	}

	public String getVersionDesc() {
		return versionDesc;
	}

	public void setVersionDesc(String versionDesc) {
		this.versionDesc = versionDesc;
	}

	public Integer getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(Integer versionCode) {
		this.versionCode = versionCode;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public Integer getVersionStatus() {
		return versionStatus;
	}

	public void setVersionStatus(Integer versionStatus) {
		this.versionStatus = versionStatus;
	}

	public Integer getVersionPlatform() {
		return versionPlatform;
	}

	public void setVersionPlatform(Integer versionPlatform) {
		this.versionPlatform = versionPlatform;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getVersionAppId() {
		return versionAppId;
	}

	public void setVersionAppId(Integer versionAppId) {
		this.versionAppId = versionAppId;
	}

	public Integer getVersionChannel() {
		return versionChannel;
	}

	public void setVersionChannel(Integer versionChannel) {
		this.versionChannel = versionChannel;
	}

	public String getVersionUrl() {
		return versionUrl;
	}

	public void setVersionUrl(String versionUrl) {
		this.versionUrl = versionUrl;
	}

	public Integer getVersionHullId() {
		return versionHullId;
	}

	public void setVersionHullId(Integer versionHullId) {
		this.versionHullId = versionHullId;
	}

}

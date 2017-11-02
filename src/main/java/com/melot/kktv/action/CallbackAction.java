package com.melot.kktv.action;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.kkcx.service.AlbumServices;
import com.melot.kktv.model.FamilyPoster;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.PictureTypeEnum;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.sdk.core.util.MelotBeanFactory;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 回调接口
 *
 * @author LY
 *
 */
public class CallbackAction extends ActionSupport {

	private static final long serialVersionUID = 1179753241062911064L;

	/** 日志记录对象 */
	private Logger logger = Logger.getLogger(CallbackAction.class);

	/** 应用预设返回值 */
	private String callback;
	/** 文件系统返回参数 */
	private String fsParams;

	private void returnFunction(PrintWriter out, String message) {
		// 输出响应结果
		out.println(message);
		out.flush();
		out.close();
		return;
	}

	public String execute() {
		logger.debug("callback:" + callback);
		logger.debug("fsParams:" + fsParams);

		ActionContext ctx = ActionContext.getContext();
		HttpServletResponse response = (HttpServletResponse) ctx.get(ServletActionContext.HTTP_RESPONSE);
		response.setCharacterEncoding("UTF-8");// 必须放在PrintWriter前
		PrintWriter out = null;
		try {
			out = response.getWriter();
		} catch (IOException e) {
			logger.error("从response获取PrintWriter对象时发生异常.", e);
			return null;
		}

		// 定义回传消息
		String writeBackMsg = "";

		// 验证接收信息
		if (callback == null || fsParams == null) {
			writeBackMsg = "{\"TagCode\":\"04010009\"}";// 文件服务器回调失败
			// 输出响应结果
			returnFunction(out, writeBackMsg);
			logger.debug("some parameter is null.");
			return null;
		}

		int userId = 0;
		Integer familyId = null;
		int pictureType = -1;
		try {
			JsonParser parser = new JsonParser();
			JsonObject paramsJson = parser.parse(callback).getAsJsonObject();
			userId = paramsJson.get("userId").getAsInt();
			if(paramsJson.get("familyId")!=null) {
				familyId = paramsJson.get("familyId").getAsInt();
			}
			pictureType = paramsJson.get("pictureType").getAsInt();
		} catch (Exception e) {
			writeBackMsg = "{\"TagCode\":\"04010009\"}";// 文件服务器回调失败
			// 输出响应结果
			returnFunction(out, writeBackMsg);
			logger.debug("parameter callback illegal.");
			return null;
		}

		// 0.头像 1.直播海报(弃用) 2.照片3.资源图片4.背景图
		if (pictureType == PictureTypeEnum.portrait) { // 0 : 头像
			String fileName;
			String path_original;
			try {
				JsonParser parser = new JsonParser();
				JsonObject fsParamsJson = parser.parse(fsParams).getAsJsonObject();

				fileName = fsParamsJson.get("fileName").getAsString();
				path_original = fsParamsJson.get("fileUrl").getAsString();

			} catch (Exception e) {
				writeBackMsg = "{\"TagCode\":\"04010009\"}" + e ;// 文件服务器回调失败
				// 输出响应结果
				returnFunction(out, writeBackMsg);
				logger.debug("parameter fsParams illegal.");
				return null;
			}

			// 入库
			// 输出响应结果
			out.println(AlbumServices.addPortraitNew(0, userId, path_original, fileName).toString());
			out.flush();
			out.close();
		} else if (pictureType == PictureTypeEnum.background) { // 4 : 背景图
			String fileName;
			String path_original;

			try {
				JsonParser parser = new JsonParser();
				JsonObject fsParamsJson = parser.parse(fsParams).getAsJsonObject();
				fileName = fsParamsJson.get("fileName").getAsString();
				path_original = fsParamsJson.get("fileUrl").getAsString();
			} catch (Exception e) {
				writeBackMsg = "{\"TagCode\":\"04010009\"}";// 文件服务器回调失败
				// 输出响应结果
				returnFunction(out, writeBackMsg);
				logger.debug("parameter fsParams illegal.");
				return null;
			}

			// 入库
			// 输出响应结果
			out.println(AlbumServices.addBackgroundNew(userId,  path_original , fileName).toString());
			out.flush();
			out.close();

		} else if (pictureType == PictureTypeEnum.family_poster) { // 5:家族海报
			FamilyPoster familyPoster = new FamilyPoster();
			try {
				JsonParser parser = new JsonParser();
				JsonObject fsParamsJson = parser.parse(fsParams).getAsJsonObject();
				familyPoster.setPath_original(fsParamsJson.get("fileUrl").getAsString());
			} catch (Exception e) {
				writeBackMsg = "{\"TagCode\":\"04010009\"}";// 文件服务器回调失败
				// 输出响应结果
				returnFunction(out, writeBackMsg);
				logger.debug("parameter fsParams illegal.");
				return null;
			}

			// 入库
			// 输出响应结果
			FamilyAction familyAction = MelotBeanFactory.getBean("familyFunction", FamilyAction.class);
			out.println(familyAction.setFamilyPoster(userId, familyId, familyPoster).toString());
			out.flush();
			out.close();

		} else if (pictureType == PictureTypeEnum.apply || pictureType == PictureTypeEnum.report) { // 7:举报 8:申请公开直播
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			out.println(result.toString());
			out.flush();
			out.close();
		} else { // 1.直播海报(弃用) 2.照片3.资源图片
			String fileName;
			String path_original;

			try {
				JsonParser parser = new JsonParser();
				JsonObject fsParamsJson = parser.parse(fsParams).getAsJsonObject();

				fileName = fsParamsJson.get("fileName").getAsString();
				path_original = fsParamsJson.get("fileUrl").getAsString();
				path_original = path_original.replaceFirst(ConfigHelper.getHttpdir(), "");
				path_original = path_original.replaceFirst("/kktv", "");
			} catch (Exception e) {
				writeBackMsg = "{\"TagCode\":\"04010009\"}";// 文件服务器回调失败
				// 输出响应结果
				returnFunction(out, writeBackMsg);
				logger.debug("parameter fsParams illegal.");
				return null;
			}

			// 入库
			// 输出响应结果
			out.println(AlbumServices.addPictureNew(userId, pictureType, path_original, fileName).toString());
			out.flush();
			out.close();

		}
		// 本次请求结束
		return null;
	}

	public String getCallback() {
		return callback;
	}

	public void setCallback(String callback) {
		this.callback = callback;
	}

	public String getFsParams() {
		return fsParams;
	}

	public void setFsParams(String fsParams) {
		this.fsParams = fsParams;
	}

}

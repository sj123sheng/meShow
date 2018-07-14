package com.melot.kktv.action;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.TagCodeEnum;

import javax.servlet.http.HttpServletRequest;

/**
 * 动态的接口类
 * 
 * @author LY
 * 
 */
public class NewsFunctions {

	/**
	 * 获取用户动态列表(20010401)
	 * @param jsonObject 请求对象
	 * @return 结果字符串
	 */
	public JsonObject getUserNewsListNew(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		JsonObject result = new JsonObject();
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		result.addProperty("pageTotal", 0);
		JsonArray jNewsList = new JsonArray();

		// 返回结果
		result.add("newsList", jNewsList);
		result.addProperty("pathPrefix", ConfigHelper.getHttpdir()); // 图片前缀
		result.addProperty("mediaPathPrefix", ConfigHelper.getMediahttpdir()); // 多媒体前缀
		result.addProperty("qiniuPathPrefix", ConfigHelper.getVideoURL()); // 七牛前缀
		return result;
	}
	
	
}
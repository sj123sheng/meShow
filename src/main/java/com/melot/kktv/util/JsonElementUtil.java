/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kktv.util;

import com.google.gson.JsonElement;

/**
 * Title: JsonElementUtil.java
 * <p>
 * Description:
 * </p>
 * 
 * @author 王康鹏<a href="mailto:kangpeng.wang@melot.cn">
 * @version V1.0
 * @since 2015年11月16日 下午2:14:02
 */
public class JsonElementUtil {
	/**
	 * 检验jsonElement内String类型
	 * 
	 * @param jsonElement
	 * @return
	 */
	public static boolean validateStr(JsonElement jsonElement) {
		return validateJsonEle(jsonElement) && !jsonElement.getAsString().equals("");
	}

	/**
	 * 检验jsonElement内int类型
	 * 
	 * @param jsonElement
	 * @return
	 */
	public static boolean validateInt(JsonElement jsonElement) {
		return validateJsonEle(jsonElement) && jsonElement.getAsInt() > 0;
	}

	/**
	 * 检验jsonElement
	 * 
	 * @param jsonElement
	 * @return
	 */
	public static boolean validateJsonEle(JsonElement jsonElement) {
		return jsonElement != null && !jsonElement.isJsonNull();
	}
}

/**
 * 
 */
package com.melot.kkgame.action;

import com.google.gson.JsonObject;
import com.melot.kktv.util.TagCodeEnum;


/**
 * Title: BaseAction
 * <p>
 * Description: 基类
 * </p>
 * 
 * @author 王康鹏<a href="mailto:kangpeng.wang@melot.cn">
 * @version V1.0
 * @since 2015年5月14日上午11:44:27
 */
public abstract class BaseAction {
	/**
	 * TagCode
	 */
	protected static final String TAG_CODE = "TagCode";
	
	protected static final String ERROR_MSG = "errorMsg";
	
	public static JsonObject successResult(){
		JsonObject result = new JsonObject();
		result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
		return result;
	}
}

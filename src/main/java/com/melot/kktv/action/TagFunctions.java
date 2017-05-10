package com.melot.kktv.action;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.JsonObject;
import com.melot.kktv.domain.TagInfo;
import com.melot.kktv.service.TagService;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.TagCodeEnum;

public class TagFunctions {
	
	/**
	 * 贴标签(20010201)
	 * @param jsonObject
	 * @return
	 * @throws Exception
	 */
	public JsonObject userTagging(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		// define usable parameters
		int userId = 0;
		String tagName = null;
		int taggedUser = 0;
		int userRole = 1; // 默认用户角色 1
		// parse the parameters
		JsonObject result = new JsonObject();
		try{
			// check user
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
			userRole = CommonUtil.getJsonParamInt(jsonObject, "userRole", 1, null, 1, Integer.MAX_VALUE);
			// check user or admin
			if (userRole == 1) {
				// check token
			    if (!checkTag) {
		            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
		            return result;
		        }
			}
			tagName = CommonUtil.getJsonParamString(jsonObject, "tagName", null, TagCodeEnum.TAG_NAME_MISSING, 1, 20);
			tagName = tagName.trim();
			if (tagName.length() > 8) {
				result.addProperty("TagCode", TagCodeEnum.TAG_NAME_OVERLENGTH);
				return result;
			}
			taggedUser = CommonUtil.getJsonParamInt(jsonObject, "taggedUser", 0, TagCodeEnum.TAGGED_USERID_MISSING, 1, Integer.MAX_VALUE);
		} catch(CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch(Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		// call module service interface
		TagInfo tagInfo = TagService.getTagInfo(0, tagName);
		int tagId = 0;
		if (tagInfo == null) {
			tagId = TagService.createTag(userId, userRole, tagName);
			if (tagId == 0) {
				result.addProperty("TagCode", TagCodeEnum.CREATE_TAG_FAIULED);
				return result;
			}
		} else {
			tagId = tagInfo.getTagId();
			if (TagService.checkUserTag(taggedUser, tagId)) {
				result.addProperty("TagCode", TagCodeEnum.USER_TAG_EXIST);
				return result;
			}
		}
		boolean flag = TagService.tagging(userId, userRole, taggedUser, tagId, tagName);
		if (flag) {
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		} else {
			result.addProperty("TagCode", TagCodeEnum.TAGGING_FAIULED);
		}
		
		return result;
	}
	
	/**
	 * 用户删除被贴标签(20010202)
	 * @param jsonObject
	 * @return
	 * @throws Exception
	 */
	public JsonObject deleteUserTag(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
	    // 该接口需要验证token,未验证的返回错误码
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
	    
	    // define usable parameters
		int userId = 0;
		String tagName = null;
		// parse the parameters
		try{
			// check user
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
			tagName = CommonUtil.getJsonParamString(jsonObject, "tagName", null, TagCodeEnum.TAG_NAME_MISSING, 1, 20);
			tagName = tagName.trim();
		} catch(CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch(Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		// call module service interface
		TagInfo tagInfo = TagService.getTagInfo(0, tagName);
		if (tagInfo != null) {
			if(TagService.checkUserTag(userId, tagInfo.getTagId())){
				if (TagService.deleteTagUser(tagInfo.getTagId(), tagInfo.getTagName(), userId)) {
					result.addProperty("TagCode", TagCodeEnum.SUCCESS);
				} else {
					result.addProperty("TagCode", TagCodeEnum.DELETE_USER_TAG_FAILED);
				}
			}else{
				result.addProperty("TagCode", TagCodeEnum.USER_TAG_NOT_EXIST);
			}
		} else {
			result.addProperty("TagCode", TagCodeEnum.TAG_NOT_EXIST);
		}
		return result;
	}
	
}
/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.action.external;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.melot.game.config.sdk.apply.service.MatchApplyService;
import com.melot.game.config.sdk.domain.MatchGroupApplyInfo;
import com.melot.game.config.sdk.domain.MatchPlayerApplyInfo;
import com.melot.kkgame.action.BaseAction;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.CommonUtil.ErrorGetParameterException;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * Title: GameApplyFunction
 * <p>
 * Description: 赛事报名接口
 * </p>
 * 
 * @author 王康鹏<a href="mailto:kangpeng.wang@melot.cn">
 * @version V1.0
 * @since 2015年9月24日 上午10:52:28
 */
public class GameApplyFunction extends BaseAction {

	/**
	 * 电子邮件已存在
	 */
	private static final String EMAIL_EXISTS = "20020079001";
	/**
	 * 身份证号已存在
	 */
	private static final String ID_EXISTS = "20020079002";
	/**
	 * 手机号已存在
	 */
	private static final String MOBILE_EXISTS = "20020079003";
	
	/**
	 * qq已存在
	 */
	private static final String QQ_NUM_EXISTS = "20020079004";
	/**
	 * 护照已存在
	 */
	private static final String PASSPORT_EXISTS = "20020079005";
	
	/**
	 * 人数不够
	 */
	private static final String MEMBER_NOT_ENOUGH = "20020079006";
	/**
	 * 身份证或者护照不存在
	 */
	private static final String ID_OR_PASSPORT_NOT_EXIST="20020079007";
	/**
	 * 身份证护照重复提交
	 */
	private static final String ID_OR_PASSPORT_CONFLICTED="20020079008";
	
	/**
	 * 保存赛区报名信息 [fucTag=20020079]
	 * 
	 */
	public JsonObject saveMatchApply(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		JsonObject result = new JsonObject();
	    if (!checkTag) {
	    	result.addProperty(TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
	    	return result;
	    }
	    
		Integer userId = null;
		Integer type = null;
		Integer matchId = null;
		String name = null;
		String identityNum = null;
		String qqNum = null;
		String mobile = null;
		String email = null;
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
			type = CommonUtil.getJsonParamInt(jsonObject, "type", 0, TagCodeEnum.APPID_MISSING, 0, Integer.MAX_VALUE);
			matchId = CommonUtil.getJsonParamInt(jsonObject, "matchId", 0, TagCodeEnum.APPID_MISSING, 0, Integer.MAX_VALUE);
			name = CommonUtil.getJsonParamString(jsonObject, "name", "", TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
		} catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty(TAG_CODE, e.getErrCode());
			return result;
		}
		
		try {
			MatchApplyService matchApplyService = MelotBeanFactory.getBean("matchApplyService", MatchApplyService.class);
			if (type == 1) { // 炉石报名 战网（邮箱） qq
				identityNum = CommonUtil.getJsonParamString(jsonObject, "identityNum", "", TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
				qqNum = CommonUtil.getJsonParamString(jsonObject, "qqNum", "", TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
				mobile = CommonUtil.getJsonParamString(jsonObject, "mobile", "", TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
				email = CommonUtil.getJsonParamString(jsonObject, "email", "", TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
				MatchPlayerApplyInfo matchPlayerApplyInfo = new MatchPlayerApplyInfo();
				matchPlayerApplyInfo.setUserId(userId);
				// 同match不能重复
				matchPlayerApplyInfo.setMatchId(matchId);
				matchPlayerApplyInfo.setGroupType(1);
				matchPlayerApplyInfo.setGroupId(0);
				matchPlayerApplyInfo.setName(name);
				matchPlayerApplyInfo.setIdentityNum(identityNum);
				matchPlayerApplyInfo.setQqNum(qqNum);
				matchPlayerApplyInfo.setMobile(mobile);
				matchPlayerApplyInfo.setEmail(email);
				if (!matchApplyService.checkEmail(matchPlayerApplyInfo.getEmail(), matchPlayerApplyInfo.getMatchId())) {
					return ErrorMessage(EMAIL_EXISTS, matchPlayerApplyInfo.getEmail());
				}
				if (!matchApplyService.checkQQNum(matchPlayerApplyInfo.getQqNum(), matchPlayerApplyInfo.getMatchId())) {
					return ErrorMessage(QQ_NUM_EXISTS, matchPlayerApplyInfo.getQqNum());
				}
				if (!matchApplyService.checkMobile(matchPlayerApplyInfo.getMobile(), matchId)) {
					return ErrorMessage(MOBILE_EXISTS, matchPlayerApplyInfo.getMobile());
				}
				//检查身份证和护照
				if (matchPlayerApplyInfo.getIdentityNum() != null) {
					if (!matchApplyService.checkIdentityNum(matchPlayerApplyInfo.getIdentityNum(), matchPlayerApplyInfo.getMatchId())) {
						return ErrorMessage(ID_EXISTS, matchPlayerApplyInfo.getIdentityNum());
					}
				} else if (matchPlayerApplyInfo.getPassport() != null) {
					if (!matchApplyService.checkPassport(matchPlayerApplyInfo.getPassport(), matchPlayerApplyInfo.getMatchId())) {
						return ErrorMessage(PASSPORT_EXISTS, matchPlayerApplyInfo.getPassport());
					}	
				} else {
					result.addProperty(TAG_CODE, ID_OR_PASSPORT_NOT_EXIST);
					return result;
				}
				matchApplyService.saveMatchApply(matchPlayerApplyInfo);
			} else if (type == 2) { // 英雄联盟报名 身份证 qq
				MatchGroupApplyInfo matchGroupApplyInfo = new MatchGroupApplyInfo();
				matchGroupApplyInfo.setMatchId(matchId);
				matchGroupApplyInfo.setGroupName(name);
				matchGroupApplyInfo.setUserId(userId);
				List<MatchPlayerApplyInfo> matchPlayerList = getMatchPlayerFromJson(jsonObject);
				if (matchPlayerList==null || matchPlayerList.size() < 5) {
					result.addProperty(TAG_CODE, MEMBER_NOT_ENOUGH);
					return result;
				}
				//检查身份证护照是否重复
				Set<String> filter = new HashSet<String>();
				for (MatchPlayerApplyInfo player : matchPlayerList) {
					//检查身份证和护照
					if (player.getPassport() != null) {
						filter.add(player.getPassport());
						if (!matchApplyService.checkPassport(player.getPassport(), matchId)) {
							return ErrorMessage(PASSPORT_EXISTS, player.getPassport());
						}	
					} else if (player.getIdentityNum() != null) {
						filter.add(player.getIdentityNum());
						if (!matchApplyService.checkIdentityNum(player.getIdentityNum(), matchId)) {
							return ErrorMessage(ID_EXISTS, player.getIdentityNum());
						}
					} else {
						result.addProperty(TAG_CODE, ID_OR_PASSPORT_NOT_EXIST);
						return result;
					}
					if (!matchApplyService.checkMobile(player.getMobile(), matchId)) {
						return ErrorMessage(MOBILE_EXISTS, player.getMobile());
					}
					if (!matchApplyService.checkQQNum(player.getQqNum(), matchId)) {
						return ErrorMessage(QQ_NUM_EXISTS, player.getQqNum());
					}
				}
				//护照或者身份证有重复
				if (filter.size() != matchPlayerList.size()) {
					result.addProperty(TAG_CODE, ID_OR_PASSPORT_CONFLICTED);
					return result;
				}
				matchGroupApplyInfo.setMatchPlayerApplyInfos(matchPlayerList);
				matchApplyService.saveMatchApply(matchGroupApplyInfo);
			} else {
				result.addProperty(TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);// 报名类型不支持
				return result;
			}
		} catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty(TAG_CODE, e.getErrCode());
			return result;
		}
		result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
		return result;
	}
	
	/**
	 * 获取赛区报名信息 [fucTag=20020080]
	 * @param jsonObject
	 * @param checkTag
	 * @param request
	 * @return
	 */
	public JsonObject getMatchApply(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		JsonObject result = new JsonObject();
		if (!checkTag) {
	    	result.addProperty(TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
	    	return result;
	    }
		
		Integer userId = null;
		Integer type = null;
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
			type = CommonUtil.getJsonParamInt(jsonObject, "type", 0, TagCodeEnum.APPID_MISSING, 0, Integer.MAX_VALUE);
		} catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty(TAG_CODE, e.getErrCode());
			return result;
		}
		MatchApplyService matchApplyService = MelotBeanFactory.getBean("matchApplyService", MatchApplyService.class);
		List<MatchPlayerApplyInfo> matchPlayerList =null;
		JsonArray jsonArray = new JsonArray();
		if (type == 1) { // 炉石报名
			matchPlayerList = matchApplyService.getLatestMatchApplyByUserId(userId, type);
			if(matchPlayerList!=null && matchPlayerList.size() == 1){
				MatchPlayerApplyInfo matchPlayer = matchPlayerList.get(0);
				JsonObject playerJson = new JsonObject();
				if (matchPlayer.getApplyId() != null) {
					playerJson.addProperty("applyId", matchPlayer.getApplyId());
				}
				if (matchPlayer.getApplyTime() != null) {
					playerJson.addProperty("applyTime", matchPlayer.getApplyTime().getTime());
				}
				if (matchPlayer.getUserId() != null) {
					playerJson.addProperty("userId", matchPlayer.getUserId());
				}
				if (matchPlayer.getMatchId() != null) {
					playerJson.addProperty("matchId", matchPlayer.getMatchId());
				}
				if (matchPlayer.getGroupType() != null) {
					playerJson.addProperty("groupType", matchPlayer.getGroupType());
				}
				if (matchPlayer.getName() != null) {
					playerJson.addProperty("name", matchPlayer.getName());
				}
				if (matchPlayer.getIdentityNum() != null) {
					playerJson.addProperty("identityNum", matchPlayer.getIdentityNum());
				}
				if (matchPlayer.getPassport() != null) {
					playerJson.addProperty("passport", matchPlayer.getPassport());
				}
				if (matchPlayer.getQqNum() != null) {
					playerJson.addProperty("qqNum", matchPlayer.getQqNum());
				}
				if (matchPlayer.getEmail() != null) {
					playerJson.addProperty("email", matchPlayer.getEmail());
				}
				if (matchPlayer.getMobile() != null) {
					playerJson.addProperty("mobile", matchPlayer.getMobile());
				}
			    jsonArray.add(playerJson);
			    result.add("player", jsonArray);
			}
		} else if (type == 2) {// 英雄联盟报名
			matchPlayerList = matchApplyService.getLatestMatchApplyByUserId(userId, type);
			if (matchPlayerList != null && matchPlayerList.size() > 0) {
				Integer groupId = matchPlayerList.get(0).getGroupId();
				for (int i = matchPlayerList.size()-1; i >= 0; i--) {
					JsonObject playerJson = new JsonObject();
					MatchPlayerApplyInfo matchPlayerApplyInfo = matchPlayerList.get(i);
					if (matchPlayerApplyInfo.getApplyId() != null) {
						playerJson.addProperty("applyId", matchPlayerApplyInfo.getApplyId());
					}
					if (matchPlayerApplyInfo.getApplyTime() != null) {
						playerJson.addProperty("applyTime", matchPlayerApplyInfo.getApplyTime().getTime());
					}
					if (matchPlayerApplyInfo.getUserId() != null) {
						playerJson.addProperty("userId", matchPlayerApplyInfo.getUserId());
					}
					if (matchPlayerApplyInfo.getMatchId() != null) {
						playerJson.addProperty("matchId", matchPlayerApplyInfo.getMatchId());
					}
					if (matchPlayerApplyInfo.getGroupType() != null) {
						playerJson.addProperty("groupType", matchPlayerApplyInfo.getGroupType());
					}
					if (matchPlayerApplyInfo.getIdentityNum() != null) {
						playerJson.addProperty("identityNum", matchPlayerApplyInfo.getIdentityNum());
					}
					if (matchPlayerApplyInfo.getQqNum() != null) {
						playerJson.addProperty("qqNum", matchPlayerApplyInfo.getQqNum());
					}
					if (matchPlayerApplyInfo.getPassport() != null) {
						playerJson.addProperty("passport", matchPlayerApplyInfo.getPassport());
					}
					if (matchPlayerApplyInfo.getName() != null) {
						playerJson.addProperty("name", matchPlayerApplyInfo.getName());
					}
					if (matchPlayerApplyInfo.getMobile() != null) {
						playerJson.addProperty("mobile", matchPlayerApplyInfo.getMobile());
					}
					jsonArray.add(playerJson);
				}
				result.add("player", jsonArray);
				MatchGroupApplyInfo groupApply = matchApplyService.getGroupApply(groupId);
				if (groupApply != null) {
					JsonObject groupJson = new JsonObject();
					if (groupApply.getGroupName() != null) {
						groupJson.addProperty("groupName", groupApply.getGroupName());
					}
					if (groupApply.getGroupId() != null) {
						groupJson.addProperty("groupId", groupApply.getGroupId());
					}
					if (groupApply.getMatchId() != null) {
						groupJson.addProperty("matchId", groupApply.getMatchId());
					}
					if (groupApply.getUserId() != null) {
						groupJson.addProperty("userId", groupApply.getUserId());
					}
					result.add("group", groupJson);
				}
			}
		} else {
			result.addProperty(TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);// 报名类型不支持
			return result;
		}
		result.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
		return result;
	}

	/**
	 * 将JsonObject对象转成List
	 * 
	 * @param jsonObject
	 * @return
	 * @throws ErrorGetParameterException
	 */
	private List<MatchPlayerApplyInfo> getMatchPlayerFromJson(JsonObject jsonObject) throws ErrorGetParameterException {
		JsonArray array = jsonObject.getAsJsonArray("players");
		if (array != null && array.size() > 0) {
			Gson gson = new Gson();
			return gson.fromJson(array, new TypeToken<List<MatchPlayerApplyInfo>>() {}.getType());
		} else {
			throw new CommonUtil.ErrorGetParameterException(TagCodeEnum.APPID_MISSING);
		}
	}
	
	/**
	 * 错误对象
	 * @param code
	 * @param msg
	 * @return
	 */
	public JsonObject ErrorMessage(String code, String msg) {
		JsonObject messageObj = new JsonObject();
		messageObj.addProperty(TAG_CODE, TagCodeEnum.SUCCESS);
		JsonObject errorObj = new JsonObject();
		errorObj.addProperty("code", code);
		errorObj.addProperty("msg", msg);
		messageObj.add("error", errorObj);
		return messageObj;
	}
	
}

package com.melot.kktv.action;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.blacklist.service.BlacklistService;
import com.melot.content.config.apply.service.ApplyActorService;
import com.melot.content.config.domain.ApplyActor;
import com.melot.family.driver.domain.ActorTransferHistV2;
import com.melot.family.driver.domain.ApplyFamilyHist;
import com.melot.family.driver.domain.FamilyInfo;
import com.melot.family.driver.domain.RespMsg;
import com.melot.family.driver.service.FamilyAdminNewService;
import com.melot.family.driver.service.FamilyInfoService;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcx.model.RecentFamilyMatch;
import com.melot.kkcx.service.*;
import com.melot.kkcx.transform.RoomTF;
import com.melot.kktv.domain.Honour;
import com.melot.kktv.domain.PreviewAct;
import com.melot.kktv.model.*;
import com.melot.kktv.redis.FamilyApplySource;
import com.melot.kktv.redis.HotDataSource;
import com.melot.kktv.redis.MatchSource;
import com.melot.kktv.redis.MedalSource;
import com.melot.kktv.util.*;
import com.melot.kktv.util.CommonUtil.ErrorGetParameterException;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;
import com.melot.module.medal.driver.domain.ResultByFamilyMedal;
import com.melot.module.medal.driver.service.FamilyMedalService;
import com.melot.sdk.core.util.MelotBeanFactory;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.*;

/**
 * 家族相关接口
 * @author RC
 *
 */
public class FamilyAction {
	
	private static Logger logger = Logger.getLogger(FamilyAction.class);
	
	/* ----------------------- 家族首页 ----------------------- */
	
	private static Long EXPIRE_TIME_OF_FAMILY_LIST_CACHE = null;
	private static List<Integer> FAMILYID_JSON_ARRAY_CACHE = new ArrayList<Integer>();
	
	/**
	 * 获取家族列表(10008001 ok)
	 * @param jsonObject
	 * @return
	 */
	public JsonObject getFamilyList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		
		// 定义使用的参数
		int platform = 0;
		int start = 0;
		int end = 0;
		
		// 定义返回结果
		JsonObject result = new JsonObject();
		// 解析参数
		try {
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
			start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, null, 0, Integer.MAX_VALUE);
			int offset = CommonUtil.getJsonParamInt(jsonObject, "offset", Constant.return_family_count, null, 0, Integer.MAX_VALUE);
			end = start + offset;
		} catch (ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		}

        //获取最近2期家族擂台赛信息
        RecentFamilyMatch recentFamilyMatch = getRecentFamilyMatch();

        // 获取上期家族比赛冠军
		Integer lastChampionFamilyId = null;
		if (recentFamilyMatch != null) {
			Integer period = recentFamilyMatch.getLastPeriod();
			FamilyMatchRank familyMatchRank = FamilyMatchService.getFamilyMatchChampion(period);
			Family lastChampionFamily = FamilyService.getFamilyInfo(familyMatchRank.getFamilyId().intValue(), platform);
			// 获取上期冠军家族当前消费榜排名
			if (lastChampionFamily != null && lastChampionFamily.getFamilyId() != null) {
				lastChampionFamilyId = lastChampionFamily.getFamilyId();
			}
		}
		
		// 判断家族列表缓存是否失效或是否未缓存
		long currentTime = System.currentTimeMillis();
		if (FAMILYID_JSON_ARRAY_CACHE.size() == 0 || EXPIRE_TIME_OF_FAMILY_LIST_CACHE == null
				|| currentTime > EXPIRE_TIME_OF_FAMILY_LIST_CACHE.longValue()) {
			// 重新缓存
			List<Integer> familyIdList = FamilyService.getFamilyIdListByType(FamilyRankingEnum.RANKING_TYPE_FAMILY_WEEKLYCONSUME, 0, -1);
			if (familyIdList != null && familyIdList.size() > 0) {
				FAMILYID_JSON_ARRAY_CACHE.clear();
				EXPIRE_TIME_OF_FAMILY_LIST_CACHE = null;
				if (lastChampionFamilyId != null) {
					FAMILYID_JSON_ARRAY_CACHE.add(lastChampionFamilyId);
				}
				for (Integer familyId : familyIdList) {
					if (lastChampionFamilyId == null
							|| familyId.intValue() != lastChampionFamilyId.intValue()) {
						FAMILYID_JSON_ARRAY_CACHE.add(familyId);
					}
				}
				// 缓存5分钟
				EXPIRE_TIME_OF_FAMILY_LIST_CACHE = new Long(currentTime + 5*60*1000);
			}
		}
		
		// 查询家族总数
		int familyTotal = FAMILYID_JSON_ARRAY_CACHE.size();
		result.addProperty("familyTotal", familyTotal);
		// 获取家族列表
		JsonArray familyJsonArray = new JsonArray();
		if (familyTotal > 0) {
			List<Integer> familyIdList = FAMILYID_JSON_ARRAY_CACHE.subList(start, ((end>=familyTotal)?(familyTotal):end));
			List<Family> familyList = FamilyService.getFamilyListByIds(familyIdList, platform);
			if (familyList != null && familyList.size() > 0) {
				try {
					JsonArray jFamilyList = new JsonParser().parse(new Gson().toJson(familyList)).getAsJsonArray();
					for (int i = 0; i < jFamilyList.size(); i++) {
						JsonObject familyObj = jFamilyList.get(i).getAsJsonObject();
						int familyId = familyObj.get("familyId").getAsInt();
						// 返回字段值修改
						if (familyObj.has("familyLeader")) {
							FamilyMember leaderMember = 
									FamilyService.getFamilyLeader(familyId, platform);
							if (leaderMember != null) {
								familyObj.addProperty("familyLeader", leaderMember.getNickname());
							} else {
								familyObj.remove("familyLeader");
							}
						}
						
						//增加勋章
						JsonElement familyMedal = familyObj.get("familyMedal");
						if(familyMedal!=null) {
							Integer medalId = familyMedal.getAsInt();
							if(medalId>0) {
								try {
					                 JsonObject medalJsonObject = MedalSource.getMedalInfoAsJson(medalId, platform);
					                  if (medalJsonObject != null) {
					                	  familyObj.add("medalTitle" ,medalJsonObject.get("medalTitle"));
					                  }
					             } catch (Exception e) {
					                  logger .error("Fail to get medal info", e);
					             }
							}
						}
						
						// 删除属性不用判断其中是否存在
						familyObj.remove("familyNotice");
						familyObj.remove("createTime");
						familyObj.remove("maxCount");
						familyJsonArray.add(familyObj);
					}
				} catch (Exception e) {
					logger.error("Fail to parse string to json array", e);
				}
			}
		}
		
		result.add("familyList", familyJsonArray);
		result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
	}

	/**
     * 获取最近2期家族擂台赛信息
     */
    private RecentFamilyMatch getRecentFamilyMatch() {
        String data = MatchSource.getRecentFamilyMatch();
        RecentFamilyMatch recentFamilyMatch = null;
        if (data != null) {
            try {
                TypeToken<RecentFamilyMatch> typeToken = new TypeToken<RecentFamilyMatch>(){};
                recentFamilyMatch = new Gson().fromJson(data, typeToken.getType());
            } catch (Exception e) {
            }
        }
        return recentFamilyMatch;
    }

	/* ----------------------- 家族主页 ----------------------- */
	
	/**
	 * 获取家族基本信息(10008002) ok
	 * 
	 * @param jsonObject 请求对象
	 * @return 登录结果
	 */
	public JsonObject getFamilyInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		
		// 定义使用的参数
		int  familyId = 0;
		int platform = 0;
		
		// 定义返回结果
		JsonObject result = new JsonObject();
		
		// 解析参数
		try {
			familyId = CommonUtil.getJsonParamInt(jsonObject, "familyId", 0, "08020002", 1, Integer.MAX_VALUE);
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
		} catch (ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		}
		
		// 获取家族基本信息
		Family family = FamilyService.getFamilyInfo(familyId, platform);
		if (family == null || family.getOpen() == null || family.getOpen() == 2 || family.getFamilyId() == 11222) {
			result.addProperty("TagCode", "08020003");
			return result;
		}
		try {
			JsonObject familyObject = new JsonParser().parse(new Gson().toJson(family)).getAsJsonObject();
			familyObject.remove("familyMedal");
			result = familyObject;
			
			if (!result.has("familyRoomId") || result.get("familyRoomId").isJsonNull() || StringUtil.strIsNull(result.get("familyRoomId").getAsString())) {
			    result.addProperty("familyRoomId", 0);
            }
		} catch (Exception e) {
			logger.error("Fail to parse java object to json object, familyId " + familyId, e);
			result.addProperty("TagCode", "08020003");
			return result;
		}
		
		// 获取正在申请加入家族用户数
		result.addProperty("applyCount", FamilyApplySource.getApplicantCount(String.valueOf(familyId)));
				
		// 获取家族勋章
		if(family.getFamilyMedal() != null) {
			try {
				JsonObject medalJsonObject = MedalSource.getMedalInfoAsJson(family.getFamilyMedal(), platform);
				if (medalJsonObject != null) {
					result.add("medalInfo", medalJsonObject);
				}
			} catch (Exception e) {
				logger.error("Fail to get medal info", e);
			}
		}
		
		// 获取家族族长
		FamilyMember familyLeader = FamilyService.getFamilyLeader(familyId, platform);
		if (familyLeader != null) {
			try {
				JsonObject jFamilyLeader = new JsonParser().parse(new Gson().toJson(familyLeader)).getAsJsonObject();
				result.add("familyLeader", jFamilyLeader);
			} catch (Exception e) {
				logger.error("Fail to parse java object to json object", e);
			}
		}
		
		// 获取家族副族长列表
		List<FamilyMember> familyDeputy = FamilyService.getFamilyDeputy(familyId, platform);
		if (familyDeputy != null && familyDeputy.size() > 0) {
			try {
				JsonArray jFamilyDeputy = new JsonParser().parse(new Gson().toJson(familyDeputy)).getAsJsonArray();
				result.add("familyDeputy", jFamilyDeputy);
			} catch (Exception e) {
				logger.error("Fail to parse java object to json array", e);
			}
		}
		
		// 获取家族荣誉
		FamilyHonor familyHonor = FamilyService.getFamilyHonor(familyId);
		if (familyHonor != null) {
			try {
				JsonObject jFamilyHonor = new JsonParser().parse(new Gson().toJson(familyHonor)).getAsJsonObject();
				result.add("familyHonor", jFamilyHonor);
			} catch (Exception e) {
				logger.error("Fail to parse java object to json object", e);
			}
		}
		
		// 返回结果
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
		return result;
	}
	
	/**
	 * 获取家族内排行榜(10008003) ok
	 * @param jsonObject
	 * @return
	 */
	public JsonObject getFamilyRankingList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		
		// 定义使用的参数
		int  familyId = 0;
		int rankType = 0;
		int slotType = 0;
		
		// 定义返回结果
		JsonObject result = new JsonObject();
		
		// 解析参数
		try {
			familyId = CommonUtil.getJsonParamInt(jsonObject, "familyId", 0, "08030002", 1, Integer.MAX_VALUE);
			rankType = CommonUtil.getJsonParamInt(jsonObject, "rankType", 0, "08030004", 0, Integer.MAX_VALUE);
			slotType = CommonUtil.getJsonParamInt(jsonObject, "slotType", 0, "08030006", 0, Integer.MAX_VALUE);
		} catch (ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		}
		
		// 读取家族内排行榜
		JsonArray jsonArray = FamilyService.getfamilyMemberRank(rankType, slotType, familyId);
		//if (jsonArray != null && jsonArray.size() > 0) { 
		    // 客户端修改
			result.add("roomList", jsonArray);
			result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
		//}
		
		// 返回结果
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
		
	}
	
	/**
	 * 获取家族内主播列表(10008004) ok
	 * @param jsonObject
	 * @return
	 */
	public JsonObject getFamilyRoomList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		
		// 定义使用的参数
		int familyId = 0;
		int platform = 0;
		int start = 0;
		int offset = 0;
		
		// 定义返回结果
		JsonObject result = new JsonObject();
		
		// 解析参数
		try {
			familyId = CommonUtil.getJsonParamInt(jsonObject, "familyId", 0, "08040002", 0, Integer.MAX_VALUE);
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, "08030006", Integer.MIN_VALUE, Integer.MAX_VALUE);
			start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, null, 0, Integer.MAX_VALUE);
			offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 0, "08040004", 0, Integer.MAX_VALUE);
		} catch (ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		}
		
		// 获取家族内房间总数
		int roomCount = FamilyService.getFamilyRoomTotalCount(AppIdEnum.AMUSEMENT, familyId);
		
		result.addProperty("roomTotal", roomCount);
		
		JsonArray jsonArray = new JsonArray();
		if (roomCount > 0) {
			// 获取家族内直播间列表
			List<RoomInfo> roomInfos = FamilyService.getFamilyActorRoomList(AppIdEnum.AMUSEMENT, familyId, start, offset);
			if (roomInfos != null && roomInfos.size() > 0) {
			    for (RoomInfo roomInfo : roomInfos) {
			        try {
			            jsonArray.add(RoomTF.roomInfoToJson(roomInfo, platform)); 
                    } catch (Exception e) {
                        logger.error("FamilyAction.getFamilyRoomList(excepiton)", e);
                    }
                }
			}
		}
		
		result.add("roomList", jsonArray);
		result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		
		return result;
	}
	
	/**
	 * 申请加入家族(10008005) ok
	 * @param jsonObject
	 * @param checkTag
	 * @return
	 */
	public JsonObject applyJoinFamily(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		// 定义使用的参数
		int familyId = 0;
		int userId = 0;
		
		// 定义返回结果
		JsonObject result = new JsonObject();
		
		// 解析参数
		try {
			familyId = CommonUtil.getJsonParamInt(jsonObject, "familyId", 0, "08050002", 0, Integer.MAX_VALUE);
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "08050004", 0, Integer.MAX_VALUE);
		} catch (ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		}
		
		boolean isActor = UserService.isActor(userId);
		if (isActor) {
			// 主播不能申请加入家族
			result.addProperty("TagCode", "08050007");
			return result;
		}
		
		// 判断用户是否已所属家族,只能属于一个家族
		if (FamilyService.getUserJoinedFamilyId(userId) == 0) {
			// 判断用户是否已申请加入家族
			int applyingFid = FamilyService.getUserApplyingFamilyId(userId);
			if (applyingFid > 0 && applyingFid == familyId) {
				// 只能一次申请加入家族	
				result.addProperty("TagCode", "08050006");
				return result;
			} else {
				// 申请加入家族
				Map<String, Object> retMap = FamilyService.applyJoinFamily(userId, familyId);
				if (retMap != null) {
					String TagCode = (String) retMap.get("TagCode");
					if(TagCode.equals(TagCodeEnum.SUCCESS)) {
						result.addProperty("TagCode", TagCodeEnum.SUCCESS);
					} else if(TagCode.equals("02")) {
						// 家族不存在
						result.addProperty("TagCode", "080501"+TagCode);
					} else if(TagCode.equals("03")) {
						// 已成为家族成员
						result.addProperty("TagCode", "080501"+TagCode);
					} else if(TagCode.equals("04")) {
						//  家族成员数达到上限
						result.addProperty("TagCode", "080501"+TagCode);
					} else {
						result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
					}
				} else {
					result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
				}
			}
		} else {
			// 已加入一个家族
			result.addProperty("TagCode", "08050005");
		}
		
		return result;
	}

	/**
	 * 获取用户家族信息(10008006) ok
	 * (若已加入,返回加入家族信息;若未加入已申请,返回已申请家族信息;若未加入未申请,返回未加入且未申请标志)
	 * @param jsonObject
	 * @param checkTag
	 * @return
	 */
	public JsonObject getUserFamily(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		// 定义使用的参数
		int userId = 0;
		
		// 定义返回结果
		JsonObject result = new JsonObject();
		
		// 解析参数
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "08060002", 1, Integer.MAX_VALUE);
		} catch (ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		}
		
		// 判断用户是否已加入家族
		int familyId = FamilyService.getUserJoinedFamilyId(userId);
		if (familyId > 0) {
			FamilyMember familyMember = FamilyService.getFamilyMemberInfo(userId, familyId, 0);
			if(familyMember != null) {
				result.addProperty("memberGrade", familyMember.getMemberGrade());
			}
			// 已加入
			result.addProperty("memberState", FamilyMemberEnum.STATE_JOINED);
			
			result.addProperty("quitState", 0); // 已经退出或未申请退出
		
		} else {
			// 判断用户是否已申请加入家族
			familyId = FamilyService.getUserApplyingFamilyId(userId);
			if (familyId > 0) {
				// 申请中
				result.addProperty("memberState", FamilyMemberEnum.STATE_APPLYING);
			} else {
				// 未申请
				result.addProperty("memberState", FamilyMemberEnum.STATE_UNAPPLIED);
			}
		}
		
		// 获取家族信息
		if (familyId > 0) {
			result.addProperty("familyId", Integer.valueOf(familyId));
			// 获取家族基本信息
			Family family = FamilyService.getFamilyInfo(familyId, 0);
			if (family != null) {
				result.addProperty("familyName", family.getFamilyName());
			}
		}
		
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
	}
	
	/**
	 * 退出家族(10008007) ok
	 * @param jsonObject
	 * @param checkTag
	 * @return
	 */
	public JsonObject quitFamily(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		// 定义使用的参数
		int userId = 0;
		int familyId = 0;
		
		// 定义返回结果
		JsonObject result = new JsonObject();
		
		// 解析参数
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "08070002", 1, Integer.MAX_VALUE);
			familyId = CommonUtil.getJsonParamInt(jsonObject, "familyId", 0, "08070004", 1, Integer.MAX_VALUE);
		} catch (ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		}
		
		// 获取用户家族
		int joinedFid = FamilyService.getUserJoinedFamilyId(userId);
		if (joinedFid == 0 || joinedFid != familyId) {
			// 用户未加入过家族
			result.addProperty("TagCode", "08070005");
			return result;
		}
		// 获取家族基本信息
		Family family = FamilyService.getFamilyInfo(familyId, 0);
		if (family == null) {
			// 用户家族已解散
			result.addProperty("TagCode", "08070006");
			return result;
		}
		if(family.getFamilyLeader() == null
				|| family.getFamilyLeader().intValue() == userId) {
			// 该家族无族长 或 用户为家族族长,不能退出家族
			result.addProperty("TagCode", "08070007");
			return result;
		}
		
		// 普通用户直接删除
		if(!UserService.isActor(userId)) {
			// 删除用户家族关系
			Map<String, Object> retMap = FamilyService.quitFamily(userId, familyId, family.getFamilyMedal());
			if (retMap != null) {
				String TagCode = (String) retMap.get("TagCode");
				if(TagCode.equals(TagCodeEnum.SUCCESS)) {
					result.addProperty("TagCode", TagCodeEnum.SUCCESS);
				} else if(TagCode.equals("02")) {
					// 家族不存在
					result.addProperty("TagCode", "080701"+TagCode);
				} else if(TagCode.equals("03")) {
					// 用户不存在或非该家族成员
					result.addProperty("TagCode", "080701"+TagCode);
				} else if(TagCode.equals("04")) {
					// 家族长不能随便退出家族
					result.addProperty("TagCode", "080701"+TagCode);
				} else {
					result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
				}
			} else {
				result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			}
		} else {
			// 主播不能退出家族
			result.addProperty("TagCode", "08070008");
		}
		
		return result;
	}

	/**
	 * 取消退出家族(10008022)
	 * @param jsonObject
	 * @param checkTag
	 * @return
	 */
	public JsonObject cancelApplyExitFamily(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		
		// 定义返回的结果
		JsonObject result = new JsonObject();
		
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		// 定义使用参数
		Integer userId = null;
		// 验证参数
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "08220001", 1, Integer.MAX_VALUE);
		} catch (ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} 
		
		FamilyApplySource.calApplyFamilyExit(userId);
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		
		return result;
	}
	
	/* ----------------------- 家族管理 ----------------------- */
	
	/**
	 * 设置家族公告(10008008) ok
	 * 权限:族长副族长
	 * @param jsonObject
	 * @param checkTag
	 * @return
	 */
	public JsonObject setFamilyNote(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
	    JsonObject result = new JsonObject();

		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		// 定义使用的参数
		int userId = 0;
		int familyId = 0;
		String notice = null;
		
		// 解析参数
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "08080002", 1, Integer.MAX_VALUE);
			familyId = CommonUtil.getJsonParamInt(jsonObject, "familyId", 0, "08080004", 1, Integer.MAX_VALUE);
			notice = CommonUtil.getJsonParamString(jsonObject, "notice", null, "08080006", 1, 300);
		} catch (ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		}
		
		// 获取用户家族成员信息
		FamilyMember familyMember = FamilyService.getFamilyMemberInfo(userId, familyId, 0);
		if (familyMember != null) {
			int memberGrade = familyMember.getMemberGrade();
			if (memberGrade == FamilyMemberEnum.GRADE_DEPUTY 
					|| memberGrade == FamilyMemberEnum.GRADE_LEADER) {
				// 设置家族公告
				Map<String, Object> retMap = FamilyService.setFamilyNotice(familyId, notice);
				if (retMap != null) {
					String TagCode = (String) retMap.get("TagCode");
					if (TagCode.equals(TagCodeEnum.SUCCESS)) {
						result.addProperty("TagCode", TagCodeEnum.SUCCESS);
					} else if(TagCode.equals("02")) {
						// 家族不存在
						result.addProperty("TagCode", "080801"+TagCode);
					} else if(TagCode.equals("03")) {
						// notice无效
						result.addProperty("TagCode", "080801"+TagCode);
					} else {
						result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
					}
				} else {
					result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
				}
			} else {
				// 用户非该家族族长也非该家族副族长
				result.addProperty("TagCode", "08080008");
			}
		} else {
			// 用户非该家族成员
			result.addProperty("TagCode", "08080007");
		}
		
		return result;
	}
	
	/**
	 * 设置家族海报(10008009) ok
	 * 权限:族长副族长
	 * @param jsonObject
	 * @param checkTag
	 * @return
	 */
	public JsonObject setFamilyPoster(Integer userId, Integer familyId, FamilyPoster familyPoster) {
		
		JsonObject result = new JsonObject();
		
		// 获取用户家族成员信息
		FamilyMember familyMember = FamilyService.getFamilyMemberInfo(userId, familyId, 0);
		if (familyMember != null) {
			int memberGrade = familyMember.getMemberGrade();
			if (memberGrade == FamilyMemberEnum.GRADE_DEPUTY 
					|| memberGrade == FamilyMemberEnum.GRADE_LEADER) {
				// 替换又拍云海报
				Map<String, Object> retMap = FamilyService.setFamilyPoster(familyId, familyPoster);
				if (retMap != null) {
					String TagCode = (String) retMap.get("TagCode");
					if (TagCode.equals(TagCodeEnum.SUCCESS)) {
						result.addProperty("TagCode", TagCodeEnum.SUCCESS);
					} else if(TagCode.equals("02")) {
						// 家族不存在
						result.addProperty("TagCode", "080901"+TagCode);
					} else if(TagCode.equals("03")) {
						// familyPoster无效
						result.addProperty("TagCode", "080901"+TagCode);
					} else {
						result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
					}
				} else {
					result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
				}
			} else {
				// 用户非该家族族长也非该家族副族长
				result.addProperty("TagCode", "08090002");
			}
		} else {
			// 用户非该家族成员
			result.addProperty("TagCode", "08090001");
		}
		return result;
	}
	
	/**
	 * 搜索家族成员(10008010) ok
	 * 权限:族长副族长
	 * 1)	显示:职务(族长/副族长)、用户类型(主播/用户)、加入时间、用户头像、用户昵称、用户明星或财富等级
	 * 2)	排序:职务/加入时间/用户类型
	 * 3)	每页显示10条
	 * @param jsonObject
	 * @param checkTag
	 * @return
	 */
	public JsonObject searchFamilyMember(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		// 定义使用的参数
		int userId = 0;
		int familyId = 0;
		int pageIndex = 0;
		int countPerPage = 0;
		int actorTag = 0;
		String fuzzyString = null;
		int platform = 0;
		
		// 定义返回结果
		JsonObject result = new JsonObject();
		
		// 解析参数
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "08100002", 1, Integer.MAX_VALUE);
			familyId = CommonUtil.getJsonParamInt(jsonObject, "familyId", 0, "08100004", 1, Integer.MAX_VALUE);
			pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
			countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", Constant.return_member_count, null, 1, Integer.MAX_VALUE);
			actorTag = CommonUtil.getJsonParamInt(jsonObject, "actorTag", -1, null, 0, Integer.MAX_VALUE);
			fuzzyString = CommonUtil.getJsonParamString(jsonObject, "fuzzyString", null, null, 1, 30);
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, null, 1, Integer.MAX_VALUE);
		} catch (ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		}
		
		// 获取用户家族成员信息
		FamilyMember familyMember = FamilyService.getFamilyMemberInfo(userId, familyId, 0);
		if (familyMember != null) {
			int memberGrade = familyMember.getMemberGrade();
			if (memberGrade == FamilyMemberEnum.GRADE_DEPUTY 
					|| memberGrade == FamilyMemberEnum.GRADE_LEADER) {
				
				// 搜索家族成员
				Map<String, Object> retMap = FamilyService.searchFamilyMember(
						familyId, pageIndex, countPerPage, actorTag, fuzzyString);
				if (retMap != null) {
					String TagCode = (String) retMap.get("TagCode");
					if (TagCode.equals(TagCodeEnum.SUCCESS)) {
						Long pageTotal = (Long) retMap.get("pageTotal");
						if (pageTotal != null && pageTotal.intValue() > 0) {
							// 返回条件下搜索到的成员总数
							result.addProperty("pageTotal", pageTotal);
							// 返回条件下成员列表
							@SuppressWarnings("unchecked")
							List<FamilyMember> memberList = (List<FamilyMember>) retMap.get("memberList");
							if (memberList != null && memberList.size() > 0) {
								JsonArray jMemberList = new JsonArray();
								for (FamilyMember member : memberList) {
									jMemberList.add(member.toJsonObject(platform));
								}
								result.add("memberList", jMemberList);
								result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
							}
						}
						result.addProperty("TagCode", TagCodeEnum.SUCCESS);
					} else {
						result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
					}
				} else {
					result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
				}
				
			} else {
				// 用户非该家族族长也非该家族副族长
				result.addProperty("TagCode", "08100006");
			}
		} else {
			// 用户非该家族成员
			result.addProperty("TagCode", "08100005");
		}
		
		return result;
	}
	
	/**
	 * 批量删除家族成员(10008011) ok
	 * 权限:族长副族长
	 * @param jsonObject
	 * @param checkTag
	 * @return
	 */
	public JsonObject removeFamilyMember(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		// 定义使用的参数
		int userId = 0;
		int familyId = 0;
		String userIds = null;
		
		// 定义返回结果
		JsonObject result = new JsonObject();
		
		// 解析参数
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "08110002", 1, Integer.MAX_VALUE);
			familyId = CommonUtil.getJsonParamInt(jsonObject, "familyId", 0, "08110004", 1, Integer.MAX_VALUE);
			userIds = CommonUtil.getJsonParamString(jsonObject, "userIds", null, "08110006", 1, 100);
		} catch (ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		}
		
		// 获取家族信息 
		Family family = FamilyService.getFamilyInfo(familyId, 0);
		if (family == null) {
			result.addProperty("TagCode", "08110004");
			return result;
		}
		
		// 校验userIds
		try {
			Set<Integer> set = new HashSet<Integer>();
			// 验证成员批量ID以,隔开 且都为数字,去重
			String[] strArr = userIds.split(",");
			userIds = "";
			for (String str : strArr) {
				if(!set.contains(Integer.parseInt(str))) {
					userIds = userIds + "," + str;
					set.add(Integer.parseInt(str));
				}
			}
			if(!userIds.isEmpty() && userIds.startsWith(",")) {
				userIds = userIds.substring(1, userIds.length());
			}
		} catch (Exception e) {
			result.addProperty("TagCode", "08110006");
			return result;
		}
		
		// 获取用户家族成员信息
		FamilyMember familyMember = FamilyService.getFamilyMemberInfo(userId, familyId, 0);
		if (familyMember != null) {
			int memberGrade = familyMember.getMemberGrade();
			if(memberGrade==FamilyMemberEnum.GRADE_DEPUTY 
					|| memberGrade==FamilyMemberEnum.GRADE_LEADER) {
				// 删除家族成员
				Map<String, Object> retMap = FamilyService.removeFamilyMember(userId, userIds, family);
				if (retMap != null) {
					String TagCode = (String) retMap.get("TagCode");
					if (TagCode.equals(TagCodeEnum.SUCCESS)) {
						if(retMap.containsKey("notPassUserids") && retMap.get("notPassUserids") != null) {
							result.addProperty("notPassUserids", (String) retMap.get("notPassUserids"));
						}
						result.addProperty("TagCode", TagCodeEnum.SUCCESS);
					} else if(TagCode.equals("02")) {
						// 家族不存在
						result.addProperty("TagCode", "081101"+TagCode);
					} else if(TagCode.equals("03")) {
						// userIds无效
						result.addProperty("TagCode", "081101"+TagCode);
					} else if(TagCode.equals("04")) {
						// userIds中有数据库不存在的用户
						result.addProperty("TagCode", "081101"+TagCode);
					} else if(TagCode.equals("05")) {
						// 该成员无权限或该成员不存在
						result.addProperty("TagCode", "081101"+TagCode);
					} else if(TagCode.equals("06")) {
						// 不能删除自己
						result.addProperty("TagCode", "081101"+TagCode);
					} else if(TagCode.equals("07")) {
						// userIds中存在非该家族成员用户
						result.addProperty("TagCode", "081101"+TagCode);
					} else if(TagCode.equals("08")) {
						// 只有族长可以删除副族长
						result.addProperty("TagCode", "081101"+TagCode);
					} else {
						result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
					}
				} else {
					result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
				}
			} else {
				// 用户非该家族族长也非该家族副族长
				result.addProperty("TagCode", "08110008");
			}
		} else {
			// 用户非该家族成员
			result.addProperty("TagCode", "08110007");
		}
		
		return result;
	}
	
	/**
	 * 设置家族副族长(10008012) ok
	 * 权限:族长
	 * @param jsonObject
	 * @param checkTag
	 * @return
	 */
	public JsonObject setFamilyDeputy(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		// 定义使用的参数
		int userId = 0;
		int familyId = 0;
		int memberId = 0;
		
		// 定义返回结果
		JsonObject result = new JsonObject();
		
		// 解析参数
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "08120002", 1, Integer.MAX_VALUE);
			familyId = CommonUtil.getJsonParamInt(jsonObject, "familyId", 0, "08120004", 1, Integer.MAX_VALUE);
			memberId = CommonUtil.getJsonParamInt(jsonObject, "memberId", 0, "08120006", 1, Integer.MAX_VALUE);
		} catch (ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		}
		
		// 获取用户家族成员信息
		FamilyMember familyMember = FamilyService.getFamilyMemberInfo(userId, familyId, 0);
		if (familyMember != null) {
			int memberGrade = familyMember.getMemberGrade();
			if (memberGrade == FamilyMemberEnum.GRADE_LEADER) {
				// 设置家族副组长
				Map<String, Object> retMap = FamilyService.updateFamilyMemberGrade(familyId, userId, memberId, FamilyMemberEnum.GRADE_DEPUTY);
				if (retMap != null) {
					String TagCode = (String) retMap.get("TagCode");
					if (TagCode.equals(TagCodeEnum.SUCCESS)) {
						result.addProperty("TagCode", TagCodeEnum.SUCCESS);
					} else if(TagCode.equals("02")) {
						// 家族成员不存在
						result.addProperty("TagCode", "081201"+TagCode);
					} else if(TagCode.equals("03")) {
						// 家族不存在
						result.addProperty("TagCode", "081201"+TagCode);
					} else if(TagCode.equals("04")) {
						// 家族族长不能被设置为副族长
						result.addProperty("TagCode", "081201"+TagCode);
					} else if(TagCode.equals("05")) {
						// 一个家族只能设置6个副族长
						result.addProperty("TagCode", "081201"+TagCode);
					} else if(TagCode.equals("06")) {
						// 家族族长才能设置/取消副族长
						result.addProperty("TagCode", "081201"+TagCode);
					} else {
						result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
					}
				} else {
					result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
				}
			} else {
				// 用户非该家族族长
				result.addProperty("TagCode", "08120008");
			}
		} else {
			// 用户非该家族成员
			result.addProperty("TagCode", "08120007");
		}
		
		return result;
	}
	
	/**
	 * 撤销家族副族长(10008013) ok
	 * 权限:族长
	 * @param jsonObject
	 * @param checkTag
	 * @return
	 */
	public JsonObject revokeFamilyDeputy(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		// 定义使用的参数
		int userId = 0;
		int familyId = 0;
		int memberId = 0;
		
		// 定义返回结果
		JsonObject result = new JsonObject();
		
		// 解析参数
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "08130002", 1, Integer.MAX_VALUE);
			familyId = CommonUtil.getJsonParamInt(jsonObject, "familyId", 0, "08130004", 1, Integer.MAX_VALUE);
			memberId = CommonUtil.getJsonParamInt(jsonObject, "memberId", 0, "08130006", 1, Integer.MAX_VALUE);
		} catch (ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		}		
		
		// 获取用户家族成员信息
		FamilyMember familyMember = FamilyService.getFamilyMemberInfo(userId, familyId, 0);
		if (familyMember != null) {
			int memberGrade = familyMember.getMemberGrade();
			if (memberGrade == FamilyMemberEnum.GRADE_LEADER) {
				// 取消设置家族副组长
				Map<String, Object> retMap = FamilyService.updateFamilyMemberGrade(familyId, userId, memberId, FamilyMemberEnum.GRADE_COMMON);
				if (retMap != null) {
					String TagCode = (String) retMap.get("TagCode");
					if (TagCode.equals(TagCodeEnum.SUCCESS)) {
						result.addProperty("TagCode", TagCodeEnum.SUCCESS);
					} else if(TagCode.equals("02")) {
						// 家族成员不存在
						result.addProperty("TagCode", "081301"+TagCode);
					} else if(TagCode.equals("03")) {
						// 家族不存在
						result.addProperty("TagCode", "081301"+TagCode);
					} else if(TagCode.equals("04")) {
						// 家族族长不能被设置为副族长
						result.addProperty("TagCode", "081301"+TagCode);
					} else if(TagCode.equals("05")) {
						// 一个家族只能设置6个副族长
						result.addProperty("TagCode", "081301"+TagCode);
					} else if(TagCode.equals("06")) {
						// 家族族长才能设置/取消副族长
						result.addProperty("TagCode", "081301"+TagCode);
					} else {
						result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
					}
				} else {
					result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
				}
			} else {
				// 用户非该家族族长
				result.addProperty("TagCode", "08130008");
			}
		} else {
			// 用户非该家族成员
			result.addProperty("TagCode", "08130007");
		}
		
		return result;
	}
	
	/**
	 * 搜索申请加入家族用户(10008014) ok
	 * 1)	显示: 用户类型(主播/用户)、申请时间、用户头像、用户昵称、用户明星或财富等级
	 * 2)	排序: 申请时间/用户类型
	 * 3)	每页显示10条
	 * @param jsonObject
	 * @param checkTag
	 * @return
	 */
	public JsonObject searchFamilyApplicant(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		// 定义使用的参数
		int userId = 0;
		int familyId = 0;
		int pageIndex = 0;
		int countPerPage = 0;
		int actorTag = 0;
		String fuzzyString = null;
		int platform = 0;
		
		// 定义返回结果
		JsonObject result = new JsonObject();
		
		// 解析参数
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "08140002", 1, Integer.MAX_VALUE);
			familyId = CommonUtil.getJsonParamInt(jsonObject, "familyId", 0, "08140004", 1, Integer.MAX_VALUE);
			pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
			countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", Constant.return_member_count, null, 1, Integer.MAX_VALUE);
			actorTag = CommonUtil.getJsonParamInt(jsonObject, "actorTag", 0, null, 0, Integer.MAX_VALUE);
			fuzzyString = CommonUtil.getJsonParamString(jsonObject, "fuzzyString", null, null, 1, 30);
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, null, 1, Integer.MAX_VALUE);
		} catch (ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		}
		
		// 获取用户家族成员信息
		FamilyMember familyMember = FamilyService.getFamilyMemberInfo(userId, familyId, 0);
		if (familyMember != null) {
			int memberGrade = familyMember.getMemberGrade();
			if (memberGrade == FamilyMemberEnum.GRADE_DEPUTY 
					|| memberGrade == FamilyMemberEnum.GRADE_LEADER) {
				
				// 不返回主播
				if (actorTag == 1) {
					result.add("applicantList", new JsonArray());
					result.addProperty("TagCode", TagCodeEnum.SUCCESS);
				} else {
					// 搜索家族申请人
					Map<String, Object> retMap = FamilyService.searchFamilyApplicant(
							familyId, pageIndex, countPerPage, actorTag, fuzzyString);
					if (retMap != null) {
						String TagCode = (String) retMap.get("TagCode");
						if (TagCode.equals(TagCodeEnum.SUCCESS)) {
							Long pageTotal = (Long) retMap.get("pageTotal");
							if (pageTotal != null && pageTotal.intValue() > 0) {
								// 返回条件下搜索到的成员总数
								result.addProperty("pageTotal", pageTotal);
								// 返回条件下成员列表
								@SuppressWarnings("unchecked")
								List<FamilyApplicant> applicantList = (List<FamilyApplicant>) retMap.get("applicantList");
								if (applicantList != null && applicantList.size() > 0) {
									JsonArray jApplicantList = new JsonArray();
									for (FamilyApplicant applicant : applicantList) {
										jApplicantList.add(applicant.toJsonObject(platform));
									}
									result.add("applicantList", jApplicantList);
								}
							}
							result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
							result.addProperty("TagCode", TagCodeEnum.SUCCESS);
						} else {
							result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
						}
					} else {
						result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
					}
				}
			} else {
				// 用户非该家族族长也非该家族副族长
				result.addProperty("TagCode", "08140006");
			}
		} else {
			// 用户非该家族成员
			result.addProperty("TagCode", "08140005");
		}
		
		return result;
	}
	
	/**
	 * 批量同意加入家族(10008015) ok
	 * 权限:族长副族长
	 * @param jsonObject
	 * @param checkTag
	 * @return
	 */
	public JsonObject agreeJoinFamily(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		// 定义使用的参数
		int userId = 0;
		int familyId = 0;
		String userIds = null;
		
		// 定义返回结果
		JsonObject result = new JsonObject();
		
		// 解析参数
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "08150002", 1, Integer.MAX_VALUE);
			familyId = CommonUtil.getJsonParamInt(jsonObject, "familyId", 0, "08150004", 1, Integer.MAX_VALUE);
			userIds = CommonUtil.getJsonParamString(jsonObject, "userIds", null, "08150006", 1, Integer.MAX_VALUE);
		} catch (ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		}
		
		// 获取家族信息 
		Family family = FamilyService.getFamilyInfo(familyId, 0);
		if (family == null) {
			result.addProperty("TagCode", "08150004");
			return result;
		}
		
		// 校验userIds
		try {
			// 限制批量处理用户数量
			Set<Integer> set = new HashSet<Integer>();
			// 验证成员批量ID以,隔开 且都为数字,去重
			String[] strArr = userIds.split(",");
			userIds = "";
			for (String str : strArr) {
				if(!set.contains(Integer.parseInt(str))) {
					userIds = userIds + "," + str;
					set.add(Integer.parseInt(str));
				}
			}
			if(!userIds.isEmpty() && userIds.startsWith(",")) {
				userIds = userIds.substring(1, userIds.length());
			}
		} catch (Exception e) {
			result.addProperty("TagCode", "08150006");
			return result;
		}
		
		// 获取用户家族成员信息
		FamilyMember familyMember = FamilyService.getFamilyMemberInfo(userId, familyId, 0);
		if (familyMember != null) {
			int memberGrade = familyMember.getMemberGrade();
			if (memberGrade == FamilyMemberEnum.GRADE_DEPUTY 
						|| memberGrade == FamilyMemberEnum.GRADE_LEADER) {
				// 批量同意加入家族
				Map<String, Object> retMap = FamilyService.agreeJoinFamily(userId, userIds, family);
				if (retMap != null) {
					String TagCode = (String) retMap.get("TagCode");
					if (TagCode.equals(TagCodeEnum.SUCCESS)) {
						if(retMap.containsKey("notPassUserids") && retMap.get("notPassUserids") != null) {
							result.addProperty("notPassUserids", (String) retMap.get("notPassUserids"));
						}
						result.addProperty("TagCode", TagCodeEnum.SUCCESS);
					} else if(TagCode.equals("02")) {
						// 家族不存在
						result.addProperty("TagCode", "081501"+TagCode);
					} else if(TagCode.equals("03")) {
						// userIds无效
						result.addProperty("TagCode", "081501"+TagCode);
					} else if(TagCode.equals("04")) {
						// userIds中存在已加入其他家族的userId
						result.addProperty("TagCode", "081501"+TagCode);
					} else if(TagCode.equals("05")) {
						// userIds中有数据库不存在的用户
						result.addProperty("TagCode", "081501"+TagCode);
					} else if(TagCode.equals("06")) {
						// userIds中存在已申请加入其他家族的userId
						result.addProperty("TagCode", "081501"+TagCode);
					} else if(TagCode.equals("07")) {
						// userIds中存在未申请加入该家族的userId
						result.addProperty("TagCode", "081501"+TagCode);
					} else if(TagCode.equals("08")) {
						// 家族成员已达到上限
						result.addProperty("TagCode", "081501"+TagCode);
					} else {
						result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
					}
				} else {
					result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
				}
			} else {
				// 用户非该家族族长也非该家族副族长
				result.addProperty("TagCode", "08150008");
			}
		} else {
			// 用户非该家族成员
			result.addProperty("TagCode", "08150007");
		}
		
		return result;
	}
	
	/**
	 * 批量拒绝加入家族(10008016) ok
	 * 权限:族长副族长
	 * @param jsonObject
	 * @param checkTag
	 * @return
	 */
	public JsonObject refuseJoinFamily(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		
		// 定义使用的参数
		int userId = 0;
		int familyId = 0;
		String userIds = null;
		
		// 定义返回结果
		JsonObject result = new JsonObject();
		
		// 解析参数
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "08160002", 1, Integer.MAX_VALUE);
			familyId = CommonUtil.getJsonParamInt(jsonObject, "familyId", 0, "08160004", 1, Integer.MAX_VALUE);
			userIds = CommonUtil.getJsonParamString(jsonObject, "userIds", null, "08160006", 1, 100);
		} catch (ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		}
		
		// 获取家族信息 
		Family family = FamilyService.getFamilyInfo(familyId, 0);
		if (family == null) {
			result.addProperty("TagCode", "08160004");
			return result;
		}
		
		// 校验userIds
		try {
			// 限制批量处理用户数量
			Set<Integer> set = new HashSet<Integer>();
			// 验证成员批量ID以,隔开 且都为数字,去重
			String[] strArr = userIds.split(",");
			userIds = "";
			for (String str : strArr) {
				if(!set.contains(Integer.parseInt(str))) {
					userIds = userIds + "," + str;
					set.add(Integer.parseInt(str));
				}
			}
			if(!userIds.isEmpty() && userIds.startsWith(",")) {
				userIds = userIds.substring(1, userIds.length());
			}
		} catch (Exception e) {
			result.addProperty("TagCode", "08160006");
			return result;
		}
		
		// 获取用户家族成员信息
		FamilyMember familyMember = FamilyService.getFamilyMemberInfo(userId, familyId, 0);
		if (familyMember != null) {
			int memberGrade = familyMember.getMemberGrade();
			if(memberGrade==FamilyMemberEnum.GRADE_DEPUTY 
					|| memberGrade==FamilyMemberEnum.GRADE_LEADER) {
				// 批量拒绝加入家族
				Map<String, Object> retMap = FamilyService.refuseJoinFamily(userId, userIds, family);
				if (retMap != null) {
					String TagCode = (String) retMap.get("TagCode");
					if (TagCode.equals(TagCodeEnum.SUCCESS)) {
						if(retMap.containsKey("notPassUserids") && retMap.get("notPassUserids") != null) {
							result.addProperty("notPassUserids", (String) retMap.get("notPassUserids"));
						}
						result.addProperty("TagCode", TagCodeEnum.SUCCESS);
					} else if(TagCode.equals("02")) {
						// 家族不存在
						result.addProperty("TagCode", "081601"+TagCode);
					} else if(TagCode.equals("03")) {
						// userIds无效
						result.addProperty("TagCode", "081601"+TagCode);
					} else if(TagCode.equals("04")) {
						// userIds中存在已加入家族的userId
						result.addProperty("TagCode", "081601"+TagCode);
					} else if(TagCode.equals("05")) {
						// userIds中存在未申请加入该家族或已被该家族家族长拒绝的userId
						result.addProperty("TagCode", "081601"+TagCode);
					} else {
						result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
					}
				} else {
					result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
				}
			} else {
				// 用户非该家族族长也非该家族副族长
				result.addProperty("TagCode", "08160008");
			}
		} else {
			// 用户非该家族成员
			result.addProperty("TagCode", "08160007");
		}
		
		return result;
	}
	
	/**
	 * 获取本期/上期家族擂台赛主播结果(10008017)
	 * @param jsonObject
	 * @return
	 */
	@SuppressWarnings("resource")
    public JsonObject getFamilyMatchActorResult(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

		// 获取参数
		JsonElement platformje = jsonObject.get("platform");
		JsonElement periodTypeje = jsonObject.get("periodType");
		int platform = 0;
		int periodType = 0;// 默认本期
		// 验证参数
		if (platformje != null && !platformje.isJsonNull() && platformje.getAsInt()>0) {
			try {
				platform = platformje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "08170002");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "08170001");
			return result;
		}
		if (periodTypeje != null && !periodTypeje.isJsonNull()) {
			try {
				periodType = periodTypeje.getAsInt();
			} catch (Exception e) {
				periodType = 0;
			}
		}
		
		//  定义结果
		JsonObject result = new JsonObject();
		JsonArray rankArr = new JsonArray();

        //获取最近2期家族擂台赛信息
        RecentFamilyMatch recentFamilyMatch = getRecentFamilyMatch();

		if (periodType == 0) {
			// 本期
			if (recentFamilyMatch != null) {
				List<FamilyMatchRank> rankList = null;
				Integer v_period = recentFamilyMatch.getThisPeriod();
				Integer v_play = recentFamilyMatch.getThisPlay();
				Date v_endTime = recentFamilyMatch.getThisEndTime();

				String data = MatchSource.getFamilyMatchPlay(String.valueOf(v_period), String.valueOf(v_play));
				if (data != null) {
					try {
						TypeToken<List<FamilyMatchRank>> typeToken = new TypeToken<List<FamilyMatchRank>>(){};
						rankList = new Gson().fromJson(data, typeToken.getType());
						// 返回客户端数据
						rankArr = new JsonArray();
						for (FamilyMatchRank fmRank : rankList) {
							JsonObject rankJson = fmRank.toJsonObject(platform);
							rankArr.add(rankJson);
						}
					} catch (Exception e) {
						rankList = null;
					}
				}
				// 获取家族比赛时间统计
				Map<String, String> statsMap = MatchSource.getFamilyMatchStats(String.valueOf(v_period), String.valueOf(v_play));
				if (statsMap != null) {
					if (statsMap.get("updateTime") != null) {
						result.addProperty("updateTime", Long.parseLong(statsMap.get("updateTime")));
					} else {
						result.addProperty("updateTime", System.currentTimeMillis());
					}
					if (statsMap.get("endTime") != null) {
						result.addProperty("endTime", Long.parseLong(statsMap.get("endTime")));
					} else {
						result.addProperty("endTime", v_endTime.getTime());
					}
				}
			}
		}
		if (periodType == -1) {
			// 上期
            if (recentFamilyMatch != null) {
                Integer v_last_period = recentFamilyMatch.getLastPeriod();
                // 上期数据8小时更新一次 redis中读取缓存
                List<FamilyMatchRank> rankList = null;
                String data = MatchSource.getFamilyMatchActorCache(String.valueOf(v_last_period));
                if (data != null) {
                    try {
                        TypeToken<List<FamilyMatchRank>> typeToken = new TypeToken<List<FamilyMatchRank>>() {
                        };
                        rankList = new Gson().fromJson(data, typeToken.getType());
                        // 返回客户端数据
                        rankArr = new JsonArray();
                        for (FamilyMatchRank fmRank : rankList) {
                            JsonObject rankJson = fmRank.toJsonObject(platform);
                            rankArr.add(rankJson);
                        }
                    } catch (Exception e) {
                        rankList = null;
                    }
                }
            }
		}
		// 返回结果
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
		result.add("rankList", rankArr);
		return result;
	}
	
	/**
	 * 获取本期/上期家族擂台赛用户结果(10008018)
	 * @param jsonObject
	 * @return
	 */
	@SuppressWarnings("resource")
    public JsonObject getFamilyMatchUserResult(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		// 获取参数
		JsonElement platformje = jsonObject.get("platform");
		JsonElement periodTypeje = jsonObject.get("periodType");
		int platform = 0;
		int periodType = 0;// 默认本期
		// 验证参数
		if (platformje != null && !platformje.isJsonNull() && platformje.getAsInt()>0) {
			try {
				platform = platformje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "08180002");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "08180001");
			return result;
		}
		if (periodTypeje != null && !periodTypeje.isJsonNull()) {
			try {
				periodType = periodTypeje.getAsInt();
			} catch (Exception e) {
				periodType = 0;
			}
		}
		
		//  定义结果
		JsonObject result = new JsonObject();
		JsonArray rankArr = new JsonArray();

        //获取最近2期家族擂台赛信息
        RecentFamilyMatch recentFamilyMatch = getRecentFamilyMatch();
        Integer v_period = null;
        if(recentFamilyMatch != null) {
            if (periodType == 0) {
                v_period = recentFamilyMatch.getThisPeriod();
            }
            if (periodType == -1) {
                v_period = recentFamilyMatch.getLastPeriod();
            }
        }
		// period startTime endTime giftId userCount
		if (v_period != null) {
			// 本期数据1分钟更新一次 上期8小时更新一次  redis中读取缓存
			List<FamilyMatchRank> rankList = null;
			String data = MatchSource.getFamilyMatchUserCache(String.valueOf(v_period));
			if (data != null) {
				try {
					TypeToken<List<FamilyMatchRank>> typeToken = new TypeToken<List<FamilyMatchRank>>(){};
					rankList = new Gson().fromJson(data, typeToken.getType());
					// 返回客户端数据
					rankArr = new JsonArray();
					for (FamilyMatchRank fmRank : rankList) {
						JsonObject rankJson = fmRank.toJsonObject(platform);
						rankArr.add(rankJson);
					}
				} catch (Exception e) {

				}
			}
		}
		
		// 返回结果
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
		result.add("rankList", rankArr);
		return result;
	
	}

	/**
	 * 获取家族擂台赛冠军榜(1:家族 2:富豪 3:用户)(10008019)
	 * @param jsonObject
	 * @return
	 */
	@SuppressWarnings("unchecked")
    public JsonObject getFamilyMatchChampion(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		// 获取参数
		JsonElement platformje = jsonObject.get("platform");
		JsonElement typeje = jsonObject.get("type");
		int platform = 0;
		int type = 0;
		// 验证参数
		if (platformje != null && !platformje.isJsonNull() && platformje.getAsInt()>0) {
			try {
				platform = platformje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "08190002");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "08190001");
			return result;
		}
		if (typeje != null && !typeje.isJsonNull() && typeje.getAsInt()>0) {
			try {
				type = typeje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "08190004");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "08190003");
			return result;
		}
		
		//  定义结果
		JsonObject result = new JsonObject();
		JsonArray rankArr = new JsonArray();
		
		if (type == 3) {
		    List<FamilyMatchRank> rankList = null;
		    
		    String key = "FamilyAction.getFamilyMatchChampion(" + type + ")";
		    String cacheValue = HotDataSource.getTempDataString(key);
		    if (!StringUtil.strIsNull(cacheValue)) {
		        rankList = new Gson().fromJson(cacheValue, new TypeToken<List<FamilyMatchRank>>(){}.getType());
            } else {
                Map<Object, Object> map = new HashMap<Object, Object>();
                try {
                    SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Family.getFamiluMatchUserChampion", map);
                    
                    String TagCode = (String) map.get("TagCode");
                    if (TagCode.equals(TagCodeEnum.SUCCESS)) {
                        if(map.containsKey("rankList") && map.get("rankList") != null) {
                            rankList = (List<FamilyMatchRank>) map.get("rankList");
                            
                            HotDataSource.setTempDataString(key, new Gson().toJson(rankList), 5 * 60);
                        }
                    }
                } catch (Exception e) {
                    logger.error("未能正常调用存储过程", e);
                }
            }
		    if (rankList != null && rankList.size() > 0) {
		        JsonObject rankJson;
		        for (FamilyMatchRank familyMatchRank : rankList) {
		            rankJson = familyMatchRank.toJsonObject(platform);
		            rankArr.add(rankJson);
		        }
		    }
        } else {
            // 上期
            //获取最近2期家族擂台赛信息
            RecentFamilyMatch recentFamilyMatch = getRecentFamilyMatch();
            Integer v_last_period = null;
            if(recentFamilyMatch != null) {
                v_last_period = recentFamilyMatch.getLastPeriod();

                if (type == 1) {
                    // 家族冠军榜
                    // 8小时更新一次  redis中读取缓存
                    String data = MatchSource.getFamilyMatchFamilyChampion();
                    if (data != null) {
                        try {
                            rankArr = new JsonParser().parse(data).getAsJsonArray();
                        } catch (Exception e) {
                            rankArr = null;
                        }
                    }
                    if (rankArr == null || rankArr.size() == 0) {
                        // oracle 读取
                        try {
                            Map<Object, Object> map = new HashMap<Object, Object>();
                            map.put("period", v_last_period);
                            SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Family.getFamilyMatchFamilyChampion", map);
                            String TagCode = (String) map.get("TagCode");
                            if (TagCode.equals(TagCodeEnum.SUCCESS)) {
                                if (map.containsKey("rankList") && map.get("rankList") != null) {
                                    List<FamilyMatchChampion> rankList = (ArrayList<FamilyMatchChampion>) map.get("rankList");
                                    rankArr = new JsonArray();
                                    for (FamilyMatchChampion fmChampion : rankList) {
                                        JsonObject rankJson = fmChampion.toJsonObject(platform);
                                        rankArr.add(rankJson);
                                    }
                                    // 保存redis存储 8小时过期
                                    MatchSource.setFamilyMatchFamilyChampion(rankArr.toString(), ConfigHelper.getFamilyMatchCacheRefreshExpireTime());
                                } else {
                                    logger.error("调用存储过程得到rankList为null,jsonObject:" + jsonObject.toString());
                                }
                            } else {
                                logger.error("调用存储过程(Family.getFamilyMatchFamilyChampion)未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
                            }
                        } catch (SQLException e) {
                            logger.error("未能正常调用存储过程", e);
                        }
                    }
                }
                if (type == 2) {
                    // 富豪冠军榜
                    // 8小时更新一次  redis中读取缓存
                    String data = MatchSource.getFamilyMatchRichChampion();
                    if (data != null) {
                        try {
                            rankArr = new JsonParser().parse(data).getAsJsonArray();
                        } catch (Exception e) {
                            rankArr = null;
                        }
                    }
                    if (rankArr == null || rankArr.size() == 0) {
                        // oracle 读取
                        try {
                            Map<Object, Object> map = new HashMap<Object, Object>();
                            map.put("period", v_last_period);
                            SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Family.getFamilyMatchRichChampion", map);
                            String TagCode = (String) map.get("TagCode");
                            if (TagCode.equals(TagCodeEnum.SUCCESS)) {
                                if (map.containsKey("rankList") && map.get("rankList") != null) {
                                    List<FamilyMatchChampion> rankList = (ArrayList<FamilyMatchChampion>) map.get("rankList");
                                    rankArr = new JsonArray();
                                    for (FamilyMatchChampion fmChampion : rankList) {
                                        JsonObject rankJson = fmChampion.toJsonObject(platform);

                                        // 获取用户有效靓号
                                        JsonObject validVirtualId = UserAssetServices.getValidVirtualId(fmChampion.getUserId()); //获取用户虚拟账号
                                        if (validVirtualId != null) {
                                            if (validVirtualId.get("idType").getAsInt() == 1) {
                                                // 支持老版靓号
                                                rankJson.addProperty("luckyId", validVirtualId.get("id").getAsInt());
                                            }
                                            rankJson.add("validId", validVirtualId);
                                        }

                                        // 读取富豪等级
                                        rankJson.addProperty("richLevel", UserService.getRichLevel(fmChampion.getUserId()));
                                        rankJson.addProperty("roomSource", AppIdEnum.AMUSEMENT);
                                        rankJson.addProperty("roomType", AppIdEnum.AMUSEMENT);
                                        rankArr.add(rankJson);
                                    }
                                    // 保存redis存储 8小时过期
                                    MatchSource.setFamilyMatchRichChampion(rankArr.toString(), ConfigHelper.getFamilyMatchCacheRefreshExpireTime());
                                } else {
                                    logger.error("调用存储过程得到rankList为null,jsonObject:" + jsonObject.toString());
                                }
                            } else {
                                logger.error("调用存储过程(Family.getFamilyMatchRichChampion)未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
                            }
                        } catch (SQLException e) {
                            logger.error("未能正常调用存储过程", e);
                        }
                    }
                }
            }
        }
		

		// 返回结果
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
		result.add("rankList", rankArr);
		return result;
	
	}

	/**
	 * 获取家族擂台赛蝉联冠军(10008020)
	 * @param jsonObject
	 * @return
	 */
	public JsonObject getFamilyMatchContinueChampion(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		// 获取参数
		JsonElement platformje = jsonObject.get("platform");
		int platform = 0;
		// 验证参数
		if (platformje != null && !platformje.isJsonNull() && platformje.getAsInt()>0) {
			try {
				platform = platformje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "08200002");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "08200001");
			return result;
		}
		
		//  定义结果
		JsonObject result = new JsonObject();
		JsonObject championObj = null;
		String data = MatchSource.getFamilyMatchContinueChampion();
		if(data!=null) {
			try {
				championObj = new JsonParser().parse(data).getAsJsonObject();
			} catch (Exception e) {
				championObj = null;
			}
		}
		if (championObj == null) {
			// oracle 读取
			try {
				Map<Object, Object> map = new HashMap<Object, Object>();
				SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Family.getFamiluMatchContinueChampion", map);
				String TagCode = (String) map.get("TagCode");
				if (TagCode.equals(TagCodeEnum.SUCCESS)) {
					if(map.containsKey("rankList") && map.get("rankList")!=null) {
						@SuppressWarnings("unchecked")
						List<FamilyMatchChampion> rankList = (ArrayList<FamilyMatchChampion>) map.get("rankList");
						FamilyMatchChampion fmChampion = rankList.get(0);
						championObj = fmChampion.toJsonObject(platform);
						
						// 获取用户有效靓号
						JsonObject validVirtualId =  UserAssetServices.getValidVirtualId(fmChampion.getUserId()); //获取用户虚拟账号
						if(validVirtualId != null) {
							if (validVirtualId.get("idType").getAsInt()==1) {
								// 支持老版靓号
								championObj.addProperty("luckyId", validVirtualId.get("id").getAsInt());
							}
							championObj.add("validId", validVirtualId);
						}
						
						// 读取富豪等级
						championObj.addProperty("richLevel", UserService.getRichLevel(fmChampion.getUserId()));
						
						// 添加勋章信息
						championObj.add("userMedal", MedalSource.getUserMedalsAsJson(fmChampion.getUserId(), platform));
						
						championObj.addProperty("roomSource", AppIdEnum.AMUSEMENT);
						championObj.addProperty("roomType", AppIdEnum.AMUSEMENT);
						// 保存redis存储 8小时过期
						MatchSource.setFamilyMatchContinueChampion(championObj.toString(), ConfigHelper.getFamilyMatchCacheRefreshExpireTime());
					} else {
						logger.error("调用存储过程得到rankList为null,jsonObject:" + jsonObject.toString());
					}
				} else {
					logger.error("调用存储过程(Family.getFamiluMatchContinueChampion)未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
				}
			} catch (SQLException e) {
				logger.error("未能正常调用存储过程", e);
			}
		}
		
		// 返回结果
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
		result.add("champion", championObj);
		return result;
	}
	
	/**
	 * Generate result JsonObject to caller
	 * @param strErrCode
	 * @return result JsonObject with errcode property
	 */
	public JsonObject generateResultJsonObject(String strErrCode, Long showMoneyBalance)
	{
		JsonObject result = new JsonObject();
		result.addProperty("TagCode", strErrCode);
		if (showMoneyBalance!=null)
			result.addProperty("showMoney", showMoneyBalance);
		return result;
	}
	
	/**
	 * 购买家族勋章(10008021)
	 * @param jsonObject with properties: userId, familyId, period(how many months),medalId
	 * @return result JasonObject
	 */
	public JsonObject buyFamilyMedal(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
		JsonObject result = new JsonObject();
		
	    // 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			return generateResultJsonObject(TagCodeEnum.TOKEN_NOT_CHECKED, null);
		}

		int userId, familyId, period, medalId;
		try {
            userId = CommonUtil.getJsonParamInt(paramJsonObject, "userId", 0, "08210001", 1, Integer.MAX_VALUE);
            familyId = CommonUtil.getJsonParamInt(paramJsonObject, "familyId", 1, "08210003", 1, Integer.MAX_VALUE);
            period = CommonUtil.getJsonParamInt(paramJsonObject, "period", 1, "08210005", 1, Integer.MAX_VALUE);
            medalId = CommonUtil.getJsonParamInt(paramJsonObject, "medalId", 1, "08210007", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
		
		ResultByFamilyMedal resultByFamilyMedal = null;
		try {
            FamilyMedalService familyMedalService = (FamilyMedalService) MelotBeanFactory.getBean("familyMedalService");
            resultByFamilyMedal = familyMedalService.buyFamilyMedal(userId, familyId, period, medalId);
        } catch (Exception e) {
            logger.error("fail to buy family medal, userId: " + userId + ", familyId: " + familyId + ", period: " + period + ", medalId: " + medalId, e);
        }
		
        // 增加家族收益
        try {
            if (resultByFamilyMedal != null && resultByFamilyMedal.getTagCode().equals(TagCodeEnum.SUCCESS)) {
                RoomService.incActorIncome(userId, 0, familyId, medalId, 1, resultByFamilyMedal.getCost().intValue(), 0, 60, 12, 0);
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                result.addProperty("showMoney", resultByFamilyMedal.getShowMoney());
            } else {
                result.addProperty("TagCode", resultByFamilyMedal.getTagCode());
            }
        } catch (Exception e) {
            logger.error("增加家族收益异常, userId: " + userId + ", familyId: " + familyId + ", refId: " + medalId + ", count: 1"
                    + ", price: " + resultByFamilyMedal.getCost() + ", actorRate: 0, familyRate: 60, type:12, addShowMoney: 0", e);
        }
		
		return result;
	}
	
	/**
	 * 获取首页推荐家族房列表(10008023)
	 * @author fenggaopan 2015年10月22日 上午10:51:12
	 * @param paramJsonObject 参数值
	 * @return 返回结果参数
	 */
	public JsonObject getFamilyLiveRoom(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
	    JsonObject result = new JsonObject();
	    
		int platform, offset, start = 0;
		try {
 			platform = CommonUtil.getJsonParamInt(paramJsonObject, "platform", PlatformEnum.WEB, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
			offset = CommonUtil.getJsonParamInt(paramJsonObject, "offset", 0, "08020002", 1, Integer.MAX_VALUE);
			start = CommonUtil.getJsonParamInt(paramJsonObject, "start", 0, "08020002", 0, Integer.MAX_VALUE);
		} catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        List<PreviewAct> acts = new ArrayList<PreviewAct>();
        
		//拿到起始的数量后，进行获取，从redis中直接获取
		String familyLiveRoomStr = FamilyService.getToppageLiveRoom(start, offset , platform);
		acts = new Gson().fromJson(familyLiveRoomStr, new TypeToken<List<PreviewAct>>(){}.getType());
		for(PreviewAct act:acts) {
		    act.setFamilyPoster(FamilyService.getFamilyPoster(act.getActBanner(), platform));
		    act.setActBanner(null);
		}
		acts = PaginationUtil.pagination(start, offset, acts);
		if (!com.melot.kktv.util.StringUtil.strIsNull(familyLiveRoomStr)) {
		    result.add("familyRoomList", new JsonParser().parse(new Gson().toJson(acts)).getAsJsonArray());
		} else {
		    result.add("familyRoomList", new JsonArray());
		}
		result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		
		return result;
	}
	
	/**
	 * 获取家族主页家族房列表(10008024)
	 * @author fenggaopan 2015年10月22日 上午10:52:43
	 * @param paramJsonObject 参数值
	 * @return 返回结果
	 */
	public JsonObject getLiveRoomInFamilyPage(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
		int platform = 0;
		int offset = 0;// 每页请求的数量
		int start = 0; // 起始记录数
		List<PreviewAct> acts = new ArrayList<PreviewAct>();
		// 定义返回结果
		JsonObject result = new JsonObject();
		// 解析参数
		try {
 			platform = CommonUtil.getJsonParamInt(paramJsonObject, "platform", PlatformEnum.WEB, null, Integer.MIN_VALUE,
			        Integer.MAX_VALUE);
			offset = CommonUtil.getJsonParamInt(paramJsonObject, "offset", 0, "08240001", 1, Integer.MAX_VALUE);
			start = CommonUtil.getJsonParamInt(paramJsonObject, "start", 0, "08240002", 0, Integer.MAX_VALUE);
			//拿到起始的数量后，进行获取，从redis中直接获取
			String familyLiveRoomStr = FamilyService.getToppageLiveRoom(start, offset , platform);
			if (!StringUtil.strIsNull(familyLiveRoomStr)) {
			    acts = new Gson().fromJson(familyLiveRoomStr, new TypeToken<List<PreviewAct>>(){}.getType());
            }
			
			if (acts != null && acts.size() > 0) {
			    for(PreviewAct act:acts) {
			        act.setFamilyPoster(FamilyService.getFamilyPoster(act.getActBanner(), platform));
			        act.setActBanner(null);
			    }
			    acts = PaginationUtil.pagination(start, offset, acts);
			    if (!com.melot.kktv.util.StringUtil.strIsNull(familyLiveRoomStr)) {
			        result.add("familyRoomList", new JsonParser().parse(new Gson().toJson(acts)).getAsJsonArray());
			    } else {
			        result.add("familyRoomList", new JsonArray());
			    }
            }
			result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		} catch (ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
		} catch (Exception e) {
		    logger.error("FamilyAction.getLiveRoomInFamilyPage(" + paramJsonObject.toString() + ") execute exception.", e);
            result.addProperty("TagCode", "08240003");
        }
		
		return result;
	}
	
	/**
	 * 获取家族荣誉榜单，勋章成员，超冠主播信息()
	 * @author fenggaopan 2015年10月22日 上午10:53:28
	 * @param paramJsonObject
	 * @return
	 */
	public JsonObject getHonourRankList(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
		// 定义使用的参数
		int platform = 0;
		int rankType = 0;// rankType是1的时候返回消费榜，rankType是2的时候返回勋章榜，rankType是3的时候返回超冠主播榜
		int count = 0; // 请求的榜单的个数
		// 定义返回结果
		JsonObject result = new JsonObject();
		// 解析参数
		try {
			platform = CommonUtil.getJsonParamInt(paramJsonObject, "platform", PlatformEnum.WEB, null, Integer.MIN_VALUE,
			        Integer.MAX_VALUE);
			rankType = CommonUtil.getJsonParamInt(paramJsonObject, "rankType", 0, "08020002", 1, Integer.MAX_VALUE);
			count = CommonUtil.getJsonParamInt(paramJsonObject, "count", 0, "08020002", 1, Integer.MAX_VALUE);
		} catch (ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		}
		
		JsonArray familyJsonArray = new JsonArray();
		
		switch (rankType) {
			case 1:
				// 获取消费榜
				// 调用redis获取到从高到底排序
				try {
					List<Honour> honurs = FamilyService.getConsumeHonour(count,platform);
					for (Honour honour:honurs) {
						// 遍历，拿到key之后，从mongo中获取familyInfo
						// 获取家族基本信息
						familyJsonArray.add(new JsonParser().parse(new Gson().toJson(honour)).getAsJsonObject());
					}
				} catch (Exception e) {
					logger.error("FamilyService.getConsumeHonour exception:", e);
				}
				break;
			
			case 2:
				// 获取勋章榜
				try {
					List<Honour> honours = FamilyService.getMedalHonour(count,platform);
					for (Honour honour:honours) {
						familyJsonArray.add(new JsonParser().parse(new Gson().toJson(honour)).getAsJsonObject());
					}
				} catch (Exception e) {
					logger.error("FamilyService.getMedalHonour exception:", e);
				}
				break;
			
			case 3:
				// 获取超冠主播榜
				try {
					List<Honour> honours = FamilyService.getCrownCountHonour(count,platform);
					for (Honour honour:honours) {
						familyJsonArray.add(new JsonParser().parse(new Gson().toJson(honour)).getAsJsonObject());
					}
				} catch (Exception e) {
					logger.error("FamilyService.getMedalHonour exception:", e);
				}
				break;
		}
		result.add("honourRank",familyJsonArray);
		result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
	}

	/**
	 * 更新家族房信息roomLock字段
	 * @author fenggaopan 2015年10月28日 下午2:52:01
	 * @param paramJsonObject
	 * @return
	 */
	public JsonObject updateRoomInfo(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
	    //锁状态(0-不加 1-加锁)
		JsonObject result = new JsonObject();
		int roomId, roomLock;
		try {
			roomId = CommonUtil.getJsonParamInt(paramJsonObject, "roomId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
			roomLock = CommonUtil.getJsonParamInt(paramJsonObject, "roomLock", 0, TagCodeEnum.ROOMLOCK_MISSING, 0, 10);
		} catch (CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch (Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}
		RoomInfo roomInfo =  new RoomInfo();
		roomInfo.setActorId(roomId);
		roomInfo.setRoomLock(roomLock);
		if (RoomService.updateRoomInfo(roomInfo)) {
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		} else {
			result.addProperty("TagCode", TagCodeEnum.CHANGE_FAIL);
		}
		return result;
    }
	
	/**
	 * 创建家族申请50008026
	 * @param paramJsonObject
	 * @param checkTag
	 * @param request
	 * @return
	 */
	public JsonObject applyFamily(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
	    JsonObject result = new JsonObject();
	    
	    if (!checkTag) {
	        result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
	    
	    int userId, type, companyType, operatorId, cityId;
	    String familyName, familyLeaderName, qq, phone, password, address, idNum, idNumImg, profileImg, manager, businessLicense, businessLicenseImg, bank, bankUser, bankCard, familyNotice;
	    String companyName = null; 
	    
	    try {
	        userId = CommonUtil.getJsonParamInt(paramJsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
	        type = CommonUtil.getJsonParamInt(paramJsonObject, "type", 0, "08260001", 1, 2);
	        companyType = CommonUtil.getJsonParamInt(paramJsonObject, "companyType", 0, "08260002", 1, 2);
	        operatorId = CommonUtil.getJsonParamInt(paramJsonObject, "operatorId", 0, "08260003", 1, Integer.MAX_VALUE);
	        cityId = CommonUtil.getJsonParamInt(paramJsonObject, "cityId", 0, "08260025", 1, Integer.MAX_VALUE);
	        familyName = CommonUtil.getJsonParamString(paramJsonObject, "familyName", null, "08260004", 1, 30);
	        familyLeaderName = CommonUtil.getJsonParamString(paramJsonObject, "familyLeaderName", null, "08260005", 1, 30);
	        qq = CommonUtil.getJsonParamString(paramJsonObject, "qq", null, "08260006", 1, 30);
	        phone = CommonUtil.getJsonParamString(paramJsonObject, "phone", null, "08260007", 1, 30);
	        password = CommonUtil.getJsonParamString(paramJsonObject, "password", null, "08260008", 1, 64);
	        address = CommonUtil.getJsonParamString(paramJsonObject, "address", null, null, 1, 100);
	        idNum = CommonUtil.getJsonParamString(paramJsonObject, "idNum", null, "08260010", 1, 18);
	        idNumImg = CommonUtil.getJsonParamString(paramJsonObject, "idNumImg", null, "08260011", 1, 500);
	        profileImg = CommonUtil.getJsonParamString(paramJsonObject, "profileImg", null, "08260012", 1, 500);
	        if (companyType == 2) {
	            companyName = CommonUtil.getJsonParamString(paramJsonObject, "companyName", null, "08260013", 1, 64);
            }
	        manager = CommonUtil.getJsonParamString(paramJsonObject, "manager", null, "08260014", 1, 64);
	        businessLicense = CommonUtil.getJsonParamString(paramJsonObject, "businessLicense", null, "08260015", 1, 100);
	        businessLicenseImg = CommonUtil.getJsonParamString(paramJsonObject, "businessLicenseImg", null, "08260016", 1, 500);
	        bank = CommonUtil.getJsonParamString(paramJsonObject, "bank", null, "08260017", 1, 100);
	        bankUser = CommonUtil.getJsonParamString(paramJsonObject, "bankUser", null, "08260018", 1, 30);
	        bankCard = CommonUtil.getJsonParamString(paramJsonObject, "bankCard", null, "08260019", 1, 30);
	        familyNotice = CommonUtil.getJsonParamString(paramJsonObject, "familyNotice", null, null, 1, 100);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
	    
	    ApplyFamilyHist applyfamilyhist = new ApplyFamilyHist();
	    applyfamilyhist.setUserId(userId);
	    applyfamilyhist.setFamilyName(familyName);
	    applyfamilyhist.setFamilyLeaderName(familyLeaderName);
	    applyfamilyhist.setQq(qq);
	    applyfamilyhist.setPhone(phone);
	    applyfamilyhist.setPassword(password);
	    applyfamilyhist.setAddress(address);
	    applyfamilyhist.setCompanyType(companyType);
	    applyfamilyhist.setCompanyName(companyName);
	    applyfamilyhist.setBusinessLicense(businessLicense);
	    applyfamilyhist.setBusinessLicenseImg(businessLicenseImg);
	    applyfamilyhist.setManager(manager);
	    applyfamilyhist.setBank(bank);
	    applyfamilyhist.setBankUser(bankUser);
	    applyfamilyhist.setBankCard(bankCard);
	    applyfamilyhist.setOperatorId(operatorId);
	    applyfamilyhist.setIdNum(idNum);
	    applyfamilyhist.setIdNumImg(idNumImg);
	    applyfamilyhist.setProfileImg(profileImg);
	    applyfamilyhist.setType(type);
	    applyfamilyhist.setFamilyNotice(familyNotice == null ? "欢迎加入KK大家族!" : familyNotice);
	    applyfamilyhist.setCityId(cityId);
	    FamilyAdminNewService familyAdminNewService = (FamilyAdminNewService) MelotBeanFactory.getBean("familyAdminNewService");
	    try {
	        RespMsg respMsg = familyAdminNewService.applyFamily(applyfamilyhist);
	        String tagCode = respMsg.getTagCode();
	        if (tagCode.equals(TagCodeEnum.SUCCESS)) {
	            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            } else if (tagCode.equals("10000033")) {
                // 家族名称重复
                result.addProperty("TagCode", "08260020");
            } else if (tagCode.equals("10000034")) {
                // 身份证号重复
                result.addProperty("TagCode", "08260021");
            } else if (tagCode.equals("10000035")) {
                // 营业执照名称重复
                result.addProperty("TagCode", "08260022");
            } else if (tagCode.equals("10000036")) {
                // 手机号重复
                result.addProperty("TagCode", "08260023");
            } else if (tagCode.equals("10000037")) {
                // 用户余额不足
                result.addProperty("TagCode", "08260024");
            } else if (tagCode.equals("10000038")) {
                // 主播不能申请家族
                result.addProperty("TagCode", "08260026");
            } else if (tagCode.equals("10000017")) {
                // 用户已存在未审核的申请记录
                result.addProperty("TagCode", "08260027");
            } else if (tagCode.equals("10000023")) {
                // 用户已加入家族
                result.addProperty("TagCode", "08260028");
            } else if (tagCode.equals("10000039")) {
                // 运营不存在
                result.addProperty("TagCode", "08260029");
            } else if (tagCode.equals("10000040")) {
                // 用户是已冻结家族的家族长，家族解散前不能创建家族
                result.addProperty("TagCode", "08260030");
            } else if (tagCode.equals("10000042")) { 
                // 开户名和企业名称不同
                result.addProperty("TagCode", "08260031");
	        } else if (tagCode.equals("10000043")) {
	            // 家族公告过长
	            result.addProperty("TagCode", "08260032");
	        } else {
                result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            }
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.UNCATCHED_EXCEPTION);
        }
	    
	    return result;
	}
	
	/**
	 * 获取用户最近一次申请创建家族的信息(50008027)
	 * @param paramJsonObject
	 * @param checkTag
	 * @param request
	 * @return
	 */
	public JsonObject getUserApplyFamilyHist(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
	    JsonObject result = new JsonObject();
	    
	    int userId;
	    
	    try {
            userId = CommonUtil.getJsonParamInt(paramJsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
	    
	    FamilyAdminNewService familyAdminNewService = (FamilyAdminNewService) MelotBeanFactory.getBean("familyAdminNewService");
	    ApplyFamilyHist applyFamilyHist = familyAdminNewService.getLastApplyFamilyHistByUserId(userId);
	    if (applyFamilyHist != null && applyFamilyHist.getState() != null) {
            if (applyFamilyHist.getState().intValue() == 2) {
                result.addProperty("reason", applyFamilyHist.getNote() != null ? applyFamilyHist.getNote() : "运营审核未通过");
            } else if (applyFamilyHist.getState().intValue() == 1) {
                // 如果用户之前申请过创建家族且成功，但是解散了
                if (FamilyService.getFamilyByFamilyLeader(userId) < 1) {
                    result.addProperty("TagCode", "08270001");
                    return result;
                }
            }
            result.addProperty("state", applyFamilyHist.getState());
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } else {
            result.addProperty("TagCode", "08270001");
        }
	    
	    return result;
	}
	
	/**
	 * 获取申请家族信息(50008028)
	 * @param jsonObject
	 * @param checkTag
	 * @param request
	 * @return
	 */
	public JsonObject getApplyForFamilyInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        int userId, appId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        // 用户无效不能申请
        UserProfile userInfo = com.melot.kktv.service.UserService.getUserInfoV2(userId);
        if (userInfo == null) {
            result.addProperty("TagCode", TagCodeEnum.USER_NOT_EXIST);
            return result;
        }
        
        // 黑名单用户不能申请
        if (com.melot.kkcx.service.UserService.blackListUser(userId)) {
            result.addProperty("TagCode", TagCodeEnum.USER_IN_BLACK);
            return result;
        }

        int actorTag = (userInfo.getIsActor() == 1) ? 1 : 0;
        
        ApplyActor applyActor = null;
        try {
            ApplyActorService applyActorService = MelotBeanFactory.getBean("applyActorService", ApplyActorService.class);
            applyActor = applyActorService.getApplyActorByActorId(userId);
        } catch (Exception e) {
            logger.error("Fail to call ApplyActorService.getApplyActorByActorId ", e);
        }
        //身份证黑名单不得申请
        try {
            if (applyActor != null && applyActor.getIdentityNumber() != null) {
                BlacklistService blacklistService = (BlacklistService) MelotBeanFactory.getBean("blacklistService");
                if (blacklistService.isIdentityBlacklist(applyActor.getIdentityNumber())) {
                    result.addProperty("TagCode", TagCodeEnum.IDENTITY_BLACK_LIST);
                    return result;
                }
            } 
        } catch (Exception e) {
            logger.error("Fail to check user is IdentityBlacklist, userId: " + userId, e);
        }
        
        if (actorTag == 0) {
            // 如果用户正在申请成为主播，不能创建家族
            if (applyActor != null && applyActor.getStatus() != null && applyActor.getStatus().intValue() != -1) {
                result.addProperty("TagCode", "08280005");
                return result;
            }
        }
        
        // 获取用户已加入家族
        int familyId = FamilyService.getUserJoinedFamilyId(userId);
        if (familyId > 0 && familyId != 11222) {
            FamilyInfo family = FamilyService.getFamilyInfoByFamilyId(familyId, appId);
            Integer open = null;
            Integer familyLeader = null;
            if (family != null && family.getOpen() != null && family.getFamilyLeader() != null) {
                open = family.getOpen();
                familyLeader = family.getFamilyLeader();
            }
            // 家族长
            if (familyLeader != null && familyLeader.intValue() == userId) {
                // 正常,返回我的家族
                if (open != null && open.intValue() != 2) {
                    result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                } else {
                    // 家族冻结（主播），主播不能创建家族
                    if (actorTag == 1) {
                        result.addProperty("TagCode", "08280001");
                    } else {
                        // 家族冻结（用户），多少天后才能创建家族
                        int days = 90;
                        if (family.getUpdateOpenTime() != null) {
                            days = (int) (90 - Math.ceil((System.currentTimeMillis() - family.getUpdateOpenTime().getTime()) / (1000 * 60 * 60 * 24)));
                        }
                        result.addProperty("days", days);
                        result.addProperty("TagCode", "08280002");
                    }
                }
            } else {
                // 非家族长
                if (actorTag == 1) {
                    ActorTransferHistV2 actorTransferHist = null;
                    try {
                        FamilyAdminNewService familyAdminNewService = (FamilyAdminNewService) MelotBeanFactory.getBean("familyAdminNewService");
                        actorTransferHist = familyAdminNewService.getLastActorTransferHistById(userId, null, "0");
                    } catch (Exception e) {
                        logger.error("Fail to getLastActorTransferHistById, actorId: " + userId, e);
                    }
                    
                    if (actorTransferHist != null) {
                        // 家族主播转会中,显示我的家族，不能查看
                        result.addProperty("TagCode", "08280003");
                        return result;
                    }
                    // 正常,返回我的家族
                    if (open != null && open.intValue() != 2) {
                        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                    } else {
                        // 家族冻结（主播），可转会不能创建
                        result.addProperty("TagCode", "08280004");
                    }
                } else {
                    // 正常,返回我的家族
                    if (open != null && open.intValue() != 2) {
                        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                    } else {
                        // 家族冻结（用户），可以
                        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                    }
                }
            }
        } else {
            // 自由主播,主播不能创建家族
            if (actorTag == 1) {
                result.addProperty("TagCode", "08280001");
            } else {
                // 对于家族长是用户，家族解散中
                Integer frozenFamilyId = null;
                try {
                    frozenFamilyId = (Integer) SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Family.getFrozenFamilyUserById", userId);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                
                if (frozenFamilyId != null) {
                    // 获取家族冻结时间
                    int days = 90;
                    FamilyInfo family = FamilyService.getFamilyInfoByFamilyId(frozenFamilyId.intValue(), appId);
                    if (family != null && family.getUpdateOpenTime() != null) {
                        days = (int) (90 - Math.ceil((System.currentTimeMillis() - family.getUpdateOpenTime().getTime()) / (1000 * 60 * 60 * 24)));
                    }
                    result.addProperty("days", days);
                    result.addProperty("TagCode", "08280002");
                } else {
                    // 普通用户,可以
                    result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                }
            }
        }
        
        return result;
    }
	
    /**
     * 根据家族id模糊查询（50001023）
     * 
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getFamilyInfoList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int familyId, pageIndex, countPerPage;
        
        try {
            familyId = CommonUtil.getJsonParamInt(jsonObject, "familyId", 0, null, 0, Integer.MAX_VALUE);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 5, null, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            FamilyInfoService familyInfoService = (FamilyInfoService) MelotBeanFactory.getBean("newFamilyInfoService");
            int familyCount = familyInfoService.getFamilyInfoListCount(familyId);
            if (familyCount > 0) {
                List<FamilyInfo> familyInfoList = familyInfoService.getFamilyInfoListByFamilyIdKey(familyId, pageIndex, countPerPage);
                if (familyInfoList != null && familyInfoList.size() > 0) {
                    JsonArray familyInfoArray = new JsonArray();
                    for (FamilyInfo familyInfo : familyInfoList) {
                        JsonObject jsonObj = new JsonObject();
                        jsonObj.addProperty("familyId", familyInfo.getFamilyId());
                        jsonObj.addProperty("familyName", familyInfo.getFamilyName());
                        familyInfoArray.add(jsonObj);
                    }
                    result.add("familyInfoList", familyInfoArray);
                }
                result.addProperty("count", familyCount);
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            } else {
                result.addProperty("TagCode", "01230001");
            }
        } catch(Exception e) {
            logger.error("familyInfoService.getFamilyInfoListByFamilyIdKey(" + familyId + ") return exception.", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        
        return result;
    }
	
}
package com.melot.kktv.action;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.common.driver.service.ShareService;
import com.melot.feedback.driver.domain.Award;
import com.melot.feedback.driver.service.FeedbackService;
import com.melot.kk.activity.driver.MissionService;
import com.melot.kk.activity.driver.domain.ShareInfo;
import com.melot.kkactivity.driver.domain.GameConfig;
import com.melot.kkactivity.driver.domain.GameGift;
import com.melot.kkactivity.driver.service.GameConfigService;
import com.melot.kktv.domain.RechargerPackage;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.kktv.util.confdynamic.SystemConfig;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;
import com.melot.module.packagegift.driver.domain.ResVip;
import com.melot.module.packagegift.driver.service.VipService;
import com.melot.sdk.core.util.MelotBeanFactory;

public class ActivityFunctions {

	private static Logger logger = Logger.getLogger(ActivityFunctions.class);

    /**
     * 获取用户周期内投票信息接口返回码
     */
    public class GetUserVoteInfoRespCode {
        public final static int RESP_CODE_FAILED_ACTIVITY_NONE = -1;
    }

    /**
     * 场次投票接口返回码
     */
    public class VotePlayRespCode {
        public final static int RESP_CODE_FAILED_ACTIVITY_NONE = -1;
        public final static int RESP_CODE_FAILED_NOT_IN_VOTE_TIME = -2;
        public final static int RESP_CODE_FAILED_OVER_PLAY_MAX_VOTES = -3;
        public final static int RESP_CODE_FAILED_OVER_PLAYER_MAX_VOTES = -4;
        public final static int RESP_CODE_FAILED_VOTE_ERROR = -5;
    }

    /**
     * 成为选手拉票助理接口返回码
     */
    public class BePlayerAsstRespCode {
        public final static int RESP_CODE_NOT_A_HOST = -1;
        public final static int RESP_CODE_ALREADY_A_ASSISTANT = -2;
        public final static int RESP_CODE_NO_ASSISTANT_SLOT_AVAILABLE = -3;
        public final static int RESP_CODE_FAILED_TO_ADD_ASSIST = -4;
        public final static int RESP_CODE_UNKNOWN_ERROR = -5;
        public final static int RESP_CODE_PLAYER_CANNOT_BE_ASSISTANT = -6;
    }
	
	/**
	 * 获取活动信息(20010001)
	 * @param jsonObject
	 * @return
	 * @throws Exception
	 */
	public JsonObject getActivityInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
	    JsonObject result = new JsonObject();
	    result.addProperty("TagCode", TagCodeEnum.FUNCTAG_INVALID_EXCEPTION);
	    return result;
	    
//		// define usable parameters
//		int platform = 0;
//		int activityId = 0;
//		// parse the parameters
//		JsonObject result = new JsonObject();
//		try {
//			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, PlatformEnum.WEB, PlatformEnum.IPAD);
//			activityId = CommonUtil.getJsonParamInt(jsonObject, "activityId", 0, TagCodeEnum.ACTIVITY_ID_MISSING, 1, Integer.MAX_VALUE);
//		} catch(CommonUtil.ErrorGetParameterException e) {
//			result.addProperty("TagCode", e.getErrCode());
//			return result;
//		} catch(Exception e) {
//			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
//			return result;
//		}
//		
//		// call module service interface
//		InterfaceActivity activityInfo = MsgClientInterfaceActivityAgent.getActivityInfo(activityId, platform);
//		if (activityInfo != null) {
//			if (activityInfo.getResponseBaseInfo().getTagCode() == MsgClientInterfaceActivityAgent.RESP_CODE_SUCCESS) {
//				try {
//					// 定义返回json对象
//					JsonObject activityJson = new JsonObject();
//					activityJson.addProperty("activityId", activityInfo.getActivityId());
//					activityJson.addProperty("startTime", activityInfo.getEndTime());
//					activityJson.addProperty("endTime", activityInfo.getStartTime());
//					if (activityInfo.getActivityTitle() != null) {
//						activityJson.addProperty("activityDesc", activityInfo.getActivityTitle());
//					}
//					if (activityInfo.getActivityDesc() != null) {
//						activityJson.addProperty("activityTitle", activityInfo.getActivityDesc());
//					}
//					if (activityInfo.getActivityImg() != null) {
//						activityJson.addProperty("activityImg", activityInfo.getActivityImg());
//					}
//					// 区分平台返回
//					switch (platform) {
//					case PlatformEnum.WEB:
//						if (activityInfo.getDetailWeb() != null)
//							activityJson.addProperty("detail", activityInfo.getDetailWeb());
//						if (activityInfo.getUrlWeb() != null)
//							activityJson.addProperty("url", activityInfo.getUrlWeb());
//						if (activityInfo.getBannerWeb() != null)
//							activityJson.addProperty("banner", activityInfo.getBannerWeb());
//						break;
//					case PlatformEnum.ANDROID:
//						if (activityInfo.getDetailAndroid() != null)
//							activityJson.addProperty("detail", activityInfo.getDetailAndroid());
//						if (activityInfo.getUrlAndroid() != null)
//							activityJson.addProperty("url", activityInfo.getUrlAndroid());
//						if (activityInfo.getBannerAndroid() != null)
//							activityJson.addProperty("banner", activityInfo.getBannerAndroid());
//						break;
//					case PlatformEnum.IPHONE:
//					case PlatformEnum.IPAD:
//						if (activityInfo.getDetailIos() != null)
//							activityJson.addProperty("detail", activityInfo.getDetailIos());
//						if (activityInfo.getUrlIos() != null)
//							activityJson.addProperty("url", activityInfo.getUrlIos());
//						if (activityInfo.getBannerIos() != null)
//							activityJson.addProperty("banner", activityInfo.getBannerIos());
//						break;
//					default:
//						break;
//					}
//					result.add("activity", activityJson);
//				} catch (Exception e) {
//					logger.error("fail to parse java object to json object.", e);
//				}
//				result.addProperty("TagCode", TagCodeEnum.SUCCESS);
//			} else {
//				result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
//			}
//		} else {
//		    result.add("activity", null);
//		    result.addProperty("TagCode", TagCodeEnum.SUCCESS);
//		}
//		return result;
	}
	
	/**
	 * 获取活动场次列表(20010002)
	 * @param jsonObject
	 * @return
	 * @throws Exception
	 */
	public JsonObject getActivityPlayList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        result.addProperty("TagCode", TagCodeEnum.FUNCTAG_INVALID_EXCEPTION);
        return result;
        
//		// define usable parameters
//		int platform = 0, activityId = 0;
//		// parse the parameters
//		JsonObject result = new JsonObject();
//		try{
//			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, PlatformEnum.WEB, PlatformEnum.IPAD);
//			activityId = CommonUtil.getJsonParamInt(jsonObject, "activityId", 0, TagCodeEnum.ACTIVITY_ID_MISSING, 1, Integer.MAX_VALUE);
//		} catch(CommonUtil.ErrorGetParameterException e) {
//			result.addProperty("TagCode", e.getErrCode());
//			return result;
//		} catch(Exception e) {
//			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
//			return result;
//		}
//		
//		// call module service interface
//		List<InterfaceActivityPlay> activityPlays = MsgClientInterfaceActivityAgent.getActivityPlayInfo(activityId, platform);
//		if (activityPlays != null && activityPlays.size() > 0) {
//			if (activityPlays.get(0).getTagCode() == MsgClientInterfaceActivityAgent.RESP_CODE_SUCCESS) {
//				JsonArray activityPlayList = new JsonArray();
//				try {
//					for (InterfaceActivityPlay play : activityPlays) {
//						JsonObject playJson = new JsonParser().parse(new Gson().toJson(play)).getAsJsonObject();
//						activityPlayList.add(playJson);
//					}
//				} catch (Exception e) {
//					logger.error("fail to parse java object to json object.", e);
//				}
//				result.add("activityPlayList", activityPlayList);
//				result.addProperty("TagCode", TagCodeEnum.SUCCESS);
//			} else {
//				result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
//			}
//		} else {
//		    result.add("activityPlayList", null);
//		    result.addProperty("TagCode", TagCodeEnum.SUCCESS);
//		}
//		return result;
	}
	
	/**
	 * 获取活动场次榜单(20010003)
	 * @param jsonObject
	 * @return
	 * @throws Exception
	 */
	public JsonObject getActivityPlayRank(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        result.addProperty("TagCode", TagCodeEnum.FUNCTAG_INVALID_EXCEPTION);
        return result;
        
//		// define usable parameters
//		int platform = 0;
//		int playId = 0;
//		int rankType = 0;
//		int startNum = 0;
//		int rankCnt = 0;
//		// parse the parameters
//		JsonObject result = new JsonObject();
//		try {
//			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, PlatformEnum.WEB, PlatformEnum.IPAD);
//			playId = CommonUtil.getJsonParamInt(jsonObject, "playId", 0, TagCodeEnum.PLAY_ID_MISSING, 1, Integer.MAX_VALUE);
//			rankType = CommonUtil.getJsonParamInt(jsonObject, "rankType", 0, TagCodeEnum.RANK_TYPE_MISSING, 0, Integer.MAX_VALUE);
//			startNum = CommonUtil.getJsonParamInt(jsonObject, "startNum", 1, null, 1, Integer.MAX_VALUE);
//			rankCnt = CommonUtil.getJsonParamInt(jsonObject, "rankCnt", ConstantEnum.return_rank_count, null, 1, Integer.MAX_VALUE);
//		} catch(CommonUtil.ErrorGetParameterException e) {
//			result.addProperty("TagCode", e.getErrCode());
//			return result;
//		} catch(Exception e) {
//			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
//			return result;
//		}
//		
//		// call module service interface
//		List<InterfaceRankPlayer> rankPlayers = MsgClientInterfaceActivityAgent.getPlayRankList(playId, rankType, startNum, rankCnt);
//		if (rankPlayers != null && rankPlayers.size() > 0) {
//			if (rankPlayers.get(0).getTagCode() == MsgClientInterfaceActivityAgent.RESP_CODE_SUCCESS) {
//				JsonArray rankPlayerList = new JsonArray();
//				try {
//					for (InterfaceRankPlayer player : rankPlayers) {
//						RankPlayerInfo playerInfo = rankPlayerToDomain(player);
//						// 定义返回json对象
//						JsonObject playerJson = new JsonObject();
//						playerJson.addProperty("userId", playerInfo.getUserId());
//						playerJson.addProperty("nickname", playerInfo.getNickname());
//						playerJson.addProperty("gender", playerInfo.getGender());
//						playerJson.addProperty("actorLevel", playerInfo.getActorLevel());
//						playerJson.addProperty("richLevel", playerInfo.getRichLevel());
//						playerJson.addProperty("rank", playerInfo.getRank());
//						playerJson.addProperty("score", playerInfo.getScore());
//						if (playerInfo.getDimensions() != null && playerInfo.getDimensions().size() > 0) {
//						    JsonArray dimensionsJson = new JsonParser().parse(new Gson().toJson(playerInfo.getDimensions())).getAsJsonArray();
//						    playerJson.add("dimensions", dimensionsJson);
//                        } else {
//                            playerJson.addProperty("dimensions", "");
//                        }
//						// 区分平台返回
//						switch (platform) {
//						case PlatformEnum.WEB:
//							if (playerInfo.getPortrait_path() != null)
//								playerJson.addProperty("portraitPath_256",
//										playerInfo.getPortrait_path() + ConstantEnum.portrait_path_256_suffix);
//							break;
//						case PlatformEnum.ANDROID:
//						case PlatformEnum.IPHONE:
//						case PlatformEnum.IPAD:
//							if (playerInfo.getPortrait_path() != null)
//								playerJson.addProperty("portraitPath_128",
//										playerInfo.getPortrait_path() + ConstantEnum.portrait_path_128_suffix);
//							break;
//						default:
//							break;
//						}
//						// 临时返回数据 争奇斗艳 20140725
//						// 用户选手投票信息
////						int userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
////						if (userId > 0) {
////							MsgActivity.GetUserVoteInfoResp getUserVoteInfoResp = ActivityAgent
////									.getUserVoteInfo(userId, playId, playerInfo.getUserId());
////							if (resp != null && resp.getRespMsg().getRespCode() == BaseAgent.RESP_CODE_SUCCESS) {
////								playerJson.addProperty("restPlayVotes", getUserVoteInfoResp.getRestPlayTickets());
////								playerJson.addProperty("restPlayerVotes", getUserVoteInfoResp.getRestPlayerTickets());
////							}
////						}
//						// 选手拉票助理
////						MsgActivity.GetPlayerAsstListResp getPlayerAsstListResp = ActivityAgent
////								.getPlayerAsstList(playId, playerInfo.getUserId());
////						if (resp != null && resp.getRespMsg().getRespCode() == BaseAgent.RESP_CODE_SUCCESS) {
////							JsonArray playerAsstList = new JsonArray();
////							try {
////								List<MsgActivity.PlayerAsst> asstList = getPlayerAsstListResp.getPlayerAsstList();
////								for (MsgActivity.PlayerAsst asst : asstList) {
////									UserInfo asstInfo = MsgClientInterfaceActivityAgent.playerAsstToDomain(asst);
////									// 定义返回json对象
////									JsonObject asstJson = new JsonObject();
////									asstJson.addProperty("userId", asstInfo.getUserId());
////									asstJson.addProperty("nickname", asstInfo.getNickname());
////									asstJson.addProperty("gender", asstInfo.getGender());
////									// 区分平台返回
////									switch (platform) {
////									case PlatformEnum.WEB:
////									case PlatformEnum.ANDROID:
////									case PlatformEnum.IPHONE:
////									case PlatformEnum.IPAD:
////										if (asstInfo.getPortrait_path() != null)
////											asstJson.addProperty("portraitPath_128",
////													asstInfo.getPortrait_path() + ConstantEnum.portrait_path_128_suffix);
////										break;
////									default:
////										break;
////									}
////									playerAsstList.add(asstJson);
////								}
////							} catch (Exception e) {
////								logger.error("fail to parse java object to json object.", e);
////							}
////							playerJson.add("playerAsstList", playerAsstList);
////						}
//						// 临时返回数据 争奇斗艳 20140725
//						rankPlayerList.add(playerJson);
//					}
//				} catch (Exception e) {
//					logger.error("fail to parse java object to json object.", e);
//				}
//				result.add("rankPlayerList", rankPlayerList);
//				result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
//				result.addProperty("TagCode", TagCodeEnum.SUCCESS);
//			} else {
//				result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
//			}
//		} else {
//		    result.add("rankPlayerList", null);
//            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
//		}
//		return result;
	}
	
	/**
     * 模块对象转化
     */
//	private static RankPlayerInfo rankPlayerToDomain(InterfaceRankPlayer rankPlayer) {
//        RankPlayerInfo rankPlayerInfo = null;
//        try {
//            String playerInfoJson = rankPlayer.getPlayerInfo();
//            rankPlayerInfo = new Gson().fromJson(playerInfoJson,
//                    RankPlayerInfo.class);
//        } catch (Exception e) {
//            return null;
//        }
//        try {
//            String dimensionsJson = rankPlayer.getDimensionInfo();
//            List<RankDimensionInfo> dimensions = new Gson().fromJson(
//                    dimensionsJson, new TypeToken<List<RankDimensionInfo>>() {
//                    }.getType());
//            rankPlayerInfo.setDimensions(dimensions);
//        } catch (Exception e) {
//        }
//        
//        rankPlayerInfo.setRank(rankPlayer.getRank());
//        rankPlayerInfo.setScore(rankPlayer.getScore());
//        return rankPlayerInfo;
//    }
	
	/**
	 * 获取活动场次用户投票信息(20010004)
	 * @param jsonObject
	 * @return
	 * @throws Exception
	 */
	public JsonObject getActivityPlayUserVoteInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        result.addProperty("TagCode", TagCodeEnum.FUNCTAG_INVALID_EXCEPTION);
        return result;
        
//	    // 该接口需要验证token,未验证的返回错误码
//        JsonObject result = new JsonObject();
//        if (!checkTag) {
//            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
//            return result;
//        }
//	    
//		int userId = 0;
//		int playId = 0;
//		int playerId = 0;
//		try{
//			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
//			playId = CommonUtil.getJsonParamInt(jsonObject, "playId", 0, TagCodeEnum.PLAY_ID_MISSING, 1, Integer.MAX_VALUE);
//			playerId = CommonUtil.getJsonParamInt(jsonObject, "playerId", 0, null, 1, Integer.MAX_VALUE);
//		} catch(CommonUtil.ErrorGetParameterException e) {
//			result.addProperty("TagCode", e.getErrCode());
//			return result;
//		} catch(Exception e) {
//			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
//			return result;
//		}
//		
//		// call module service interface
//		InterfaceVoteRestInfo voteRestInfo = MsgClientInterfaceActivityAgent.getUserVoteInfo(userId, playId, playerId);
//		if (voteRestInfo != null) {
//			if (voteRestInfo.getResponseBaseInfo().getTagCode() == MsgClientInterfaceActivityAgent.RESP_CODE_SUCCESS) {
//				result.addProperty("restPlayVotes", voteRestInfo.getRestPlayTickets());
//				if (playerId > 0) {
//					result.addProperty("restPlayerVotes", voteRestInfo.getRestPlayerTickets());
//				}
//				result.addProperty("TagCode", TagCodeEnum.SUCCESS);
//			} else {
//				if (voteRestInfo.getResponseBaseInfo().getTagCode()
//						== GetUserVoteInfoRespCode.RESP_CODE_FAILED_ACTIVITY_NONE) {
//					result.addProperty("TagCode", TagCodeEnum.ACTIVITY_NOT_EXIST);
//				} else {
//					result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
//				}
//			}
//		} else {
//			result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
//		}
//		return result;
	}
	
	/**
	 * 为活动场次选手投票(20010005)
	 * @param jsonObject
	 * @return
	 * @throws Exception
	 */
	public JsonObject voteForActivityPlayPlayer(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        result.addProperty("TagCode", TagCodeEnum.FUNCTAG_INVALID_EXCEPTION);
        return result;
        
//	    // 该接口需要验证token,未验证的返回错误码
//	    JsonObject result = new JsonObject();
//        if (!checkTag) {
//            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
//            return result;
//        }
//	    
//		// define usable parameters
//		int userId = 0;
//		int playId = 0;
//		int playerId = 0;
//		// parse the parameters
//		try{
//			// check user token
//			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
//			playId = CommonUtil.getJsonParamInt(jsonObject, "playId", 0, TagCodeEnum.PLAY_ID_MISSING, 1, Integer.MAX_VALUE);
//			playerId = CommonUtil.getJsonParamInt(jsonObject, "playerId", 0, TagCodeEnum.PLAYER_ID_MISSINF, 1, Integer.MAX_VALUE);
//		} catch(CommonUtil.ErrorGetParameterException e) {
//			result.addProperty("TagCode", e.getErrCode());
//			return result;
//		} catch(Exception e) {
//			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
//			return result;
//		}
//		
//		// call module service interface
//		InterfaceVoteRestInfo voteRestInfo = MsgClientInterfaceActivityAgent.votePlay(userId, playId, playerId);
//		if (voteRestInfo != null) {
//			if (voteRestInfo.getResponseBaseInfo().getTagCode() == MsgClientInterfaceActivityAgent.RESP_CODE_SUCCESS) {
//				result.addProperty("restPlayVotes", voteRestInfo.getRestPlayTickets());
//				if (playerId > 0) {
//					result.addProperty("restPlayerVotes", voteRestInfo.getRestPlayerTickets());
//				}
//				result.addProperty("TagCode", TagCodeEnum.SUCCESS);
//			} else {
//				if (voteRestInfo.getResponseBaseInfo().getTagCode()
//						== VotePlayRespCode.RESP_CODE_FAILED_ACTIVITY_NONE) {
//					// 活动场次不存在
//					result.addProperty("TagCode", TagCodeEnum.PLAY_NOT_EXIST);
//				} else if(voteRestInfo.getResponseBaseInfo().getTagCode()
//						== VotePlayRespCode.RESP_CODE_FAILED_NOT_IN_VOTE_TIME) {
//					// 当前时间无法投票
//					result.addProperty("TagCode", TagCodeEnum.CANNOT_VOTE_NOW);
//				} else if(voteRestInfo.getResponseBaseInfo().getTagCode()
//						== VotePlayRespCode.RESP_CODE_FAILED_OVER_PLAY_MAX_VOTES) {
//					// 已达到一个场次可投票数上限
//					result.addProperty("TagCode", TagCodeEnum.OVER_PLAY_MAX_VOTES);
//				} else if(voteRestInfo.getResponseBaseInfo().getTagCode()
//						== VotePlayRespCode.RESP_CODE_FAILED_OVER_PLAYER_MAX_VOTES) {
//					// 已达到一个选手可被投票数上限
//					result.addProperty("TagCode", TagCodeEnum.OVER_PLAYER_MAX_VOTES);
//				} else if(voteRestInfo.getResponseBaseInfo().getTagCode()
//						== VotePlayRespCode.RESP_CODE_FAILED_VOTE_ERROR) {
//					// 场次投票失败
//					result.addProperty("TagCode", TagCodeEnum.VOTE_FAILED);
//				} else {
//					result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
//				}
//			}
//		} else {
//			result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
//		}
//		return result;
	}
	
	/**
	 * 获取活动场次选手拉票助理(20010006)
	 * @param jsonObject
	 * @return
	 * @throws Exception
	 */
	public JsonObject getActivityPlayPlayerAsstList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        result.addProperty("TagCode", TagCodeEnum.FUNCTAG_INVALID_EXCEPTION);
        return result;
        
//		// define usable parameters
//		int platform = 0;
//		int playId = 0;
//		int playerId = 0;
//		//parse the parameters
//		JsonObject result = new JsonObject();
//		try{
//			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, PlatformEnum.WEB, PlatformEnum.IPAD);
//			playId = CommonUtil.getJsonParamInt(jsonObject, "playId", 0, TagCodeEnum.PLAY_ID_MISSING, 1, Integer.MAX_VALUE);
//			playerId = CommonUtil.getJsonParamInt(jsonObject, "playerId", 0, TagCodeEnum.PLAYER_ID_MISSINF, 1, Integer.MAX_VALUE);
//		} catch(CommonUtil.ErrorGetParameterException e) {
//			result.addProperty("TagCode", e.getErrCode());
//			return result;
//		} catch(Exception e) {
//			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
//			return result;
//		}
//		
//		// call module service interface
//		List<InterfacePlayerAsstInfo> playerAsstInfos = MsgClientInterfaceActivityAgent.getPlayerAsstList(playId, playerId);
//		if (playerAsstInfos != null && playerAsstInfos.size() > 0) {
//			if (playerAsstInfos.get(0).getResponseBaseInfo().getTagCode() == MsgClientInterfaceActivityAgent.RESP_CODE_SUCCESS) {
//				JsonArray playerAsstList = new JsonArray();
//				try {
//					for (InterfacePlayerAsstInfo playerAsst : playerAsstInfos) {
//						UserInfo asstInfo = playerAsstToDomain(playerAsst);
//						// 定义返回json对象
//						JsonObject asstJson = new JsonObject();
//						asstJson.addProperty("userId", asstInfo.getUserId());
//						asstJson.addProperty("nickname", asstInfo.getNickname());
//						asstJson.addProperty("gender", asstInfo.getGender());
//						asstJson.addProperty("actorLevel", asstInfo.getActorLevel());
//						asstJson.addProperty("richLevel", asstInfo.getRichLevel());
//						// 区分平台返回
//						switch (platform) {
//						case PlatformEnum.WEB:
//							if (asstInfo.getPortrait_path() != null)
//								asstJson.addProperty("portraitPath_256",
//										asstInfo.getPortrait_path() + ConstantEnum.portrait_path_256_suffix);
//							break;
//						case PlatformEnum.ANDROID:
//						case PlatformEnum.IPHONE:
//						case PlatformEnum.IPAD:
//							if (asstInfo.getPortrait_path() != null)
//								asstJson.addProperty("portraitPath_128",
//										asstInfo.getPortrait_path() + ConstantEnum.portrait_path_128_suffix);
//							break;
//						default:
//							break;
//						}
//						playerAsstList.add(asstJson);
//					}
//				} catch (Exception e) {
//					logger.error("fail to parse java object to json object.", e);
//				}
//				result.add("playerAsstList", playerAsstList);
//				result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
//				result.addProperty("TagCode", TagCodeEnum.SUCCESS);
//			} else {
//				result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
//			}
//		} else {
//		    result.add("playerAsstList", null);
//            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
//		}
//		return result;
	}

    /**
     * 模块对象转化
     */
//	private static UserInfo playerAsstToDomain(InterfacePlayerAsstInfo playerAsst) {
//        UserInfo userInfo = new UserInfo();
//        userInfo.setUserId(playerAsst.getUserId());
//        userInfo.setNickname(playerAsst.getNickname());
//        userInfo.setGender(playerAsst.getGender());
//        userInfo.setPortrait_path(playerAsst.getPortrait_path());
//        return userInfo;
//    }
	
	/**
	 * 成为活动场次选手拉票助理(20010007)
	 * @param jsonObject
	 * @return
	 * @throws Exception
	 */
	public JsonObject beActivityPlayPlayerAsst(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        result.addProperty("TagCode", TagCodeEnum.FUNCTAG_INVALID_EXCEPTION);
        return result;
        
//	    // 该接口需要验证token,未验证的返回错误码
//        JsonObject result = new JsonObject();
//        if (!checkTag) {
//            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
//            return result;
//        }
//	    
//		int userId = 0;
//		int playId = 0;
//		int playerId = 0;
//		try{
//			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
//			playId = CommonUtil.getJsonParamInt(jsonObject, "playId", 0, TagCodeEnum.PLAY_ID_MISSING, 1, Integer.MAX_VALUE);
//			playerId = CommonUtil.getJsonParamInt(jsonObject, "playerId", 0, TagCodeEnum.PLAYER_ID_MISSINF, 1, Integer.MAX_VALUE);
//		} catch(CommonUtil.ErrorGetParameterException e) {
//			result.addProperty("TagCode", e.getErrCode());
//			return result;
//		} catch(Exception e) {
//			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
//			return result;
//		}
//		
//		// call module service interface
//		ResponsBaseInfo resp = MsgClientInterfaceActivityAgent.bePlayerAsstResp(userId, playId, playerId);
//		if (resp != null) {
//			if (resp.getTagCode() == MsgClientInterfaceActivityAgent.RESP_CODE_SUCCESS) {
//				result.addProperty("TagCode", TagCodeEnum.SUCCESS);
//			} else {
//				if (resp.getTagCode()
//						== BePlayerAsstRespCode.RESP_CODE_ALREADY_A_ASSISTANT) {
//					// 已经成为拉票助理
//					result.addProperty("TagCode", TagCodeEnum.HAS_BEEN_ASSISTANT);
//				} else if(resp.getTagCode()
//						== BePlayerAsstRespCode.RESP_CODE_FAILED_TO_ADD_ASSIST) {
//					// 添加拉票助理失败
//					result.addProperty("TagCode", TagCodeEnum.ADD_ASSISTANT_FAILED);
//				} else if(resp.getTagCode()
//						== BePlayerAsstRespCode.RESP_CODE_NO_ASSISTANT_SLOT_AVAILABLE) {
//					// 已到达一个选手助理数上限
//					result.addProperty("TagCode", TagCodeEnum.OVER_PLAYER_ASSISTANT_MAX_COUNT);
//				} else if(resp.getTagCode()
//						== BePlayerAsstRespCode.RESP_CODE_NOT_A_HOST) {
//					// 拉票助理必须是主播
//					result.addProperty("TagCode", TagCodeEnum.ASSISTANT_MUST_BE_ACTOR);
//				} else if(resp.getTagCode()
//						== BePlayerAsstRespCode.RESP_CODE_PLAYER_CANNOT_BE_ASSISTANT) {
//					// 选手不能成为拉票助理
//					result.addProperty("TagCode", TagCodeEnum.PLAYER_CANNOT_BE_ASSISTANT);
//				} else if(resp.getTagCode()
//						== BePlayerAsstRespCode.RESP_CODE_UNKNOWN_ERROR) {
//					// 未知错误
//					result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
//				} else {
//					result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
//				}
//			}
//		} else {
//			result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
//		}
//		return result;
	}

    /**
     * 获取用户排名（20010009）
     * @param paramJsonObject
     * @return
     */
    public JsonObject getUserRankOrder(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        result.addProperty("TagCode", TagCodeEnum.FUNCTAG_INVALID_EXCEPTION);
        return result;
        
//        JsonObject result = new JsonObject();
//        int userId, platform, maxOrder;
//        JsonArray playRankArray = null;
//        try {
//            platform = CommonUtil.getJsonParamInt(paramJsonObject, "platform", PlatformEnum.WEB, null, PlatformEnum.WEB, PlatformEnum.IPAD);
//            userId = CommonUtil.getJsonParamInt(paramJsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
//            maxOrder = CommonUtil.getJsonParamInt(paramJsonObject, "maxOrder", 0, "10090001", 1, Integer.MAX_VALUE);
//            
//            playRankArray = paramJsonObject.getAsJsonArray("playRankList");
//            if (playRankArray == null || playRankArray.size() < 1) {
//                throw new CommonUtil.ErrorGetParameterException("10090002");
//            }
//        } catch (CommonUtil.ErrorGetParameterException e) {
//            result.addProperty("TagCode", e.getErrCode());
//            return result;
//        } catch (Exception e) {
//            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
//            return result;
//        }
//        
//        Gson gson = new Gson();
//        JsonObject jsonObject;
//        String playId, rankId, portraitPath;
//        RankPlayerInfo rankPlayerInfo = null;
//        int order = 0;
//        double score = -1d;
//        Map<String, Object> beforeUserMap = null, tempMap = null;
//        JsonArray jsonArray = new JsonArray();
//        for (JsonElement jsonElement : playRankArray) {
//            order = 0;
//            score = -1d;
//            
//            jsonObject = jsonElement.getAsJsonObject();
//            playId = jsonObject.get("playId").getAsString();
//            rankId = jsonObject.get("rankId").getAsString();
//            
//            List<Map<String, Object>> list = ActivitySource.getPlayerRankList(playId, rankId, 1, maxOrder);
//            if (list != null && list.size() > 0) {
//                for (Map<String, Object> map : list) {
//                    order ++;
//                    if (map.containsKey("playerInfo")) {
//                        tempMap = gson.fromJson(((String) map.get("playerInfo")), new TypeToken<Map<String, Object>>(){}.getType());
//                        if (tempMap != null && tempMap.containsKey("userInfo")) {
//                            rankPlayerInfo = gson.fromJson(((String) tempMap.get("userInfo")), new TypeToken<RankPlayerInfo>(){}.getType());
//                            if (rankPlayerInfo != null && rankPlayerInfo.getUserId() == userId) {
//                                
//                                jsonObject.addProperty("userId", rankPlayerInfo.getUserId());
//                                jsonObject.addProperty("nickname", rankPlayerInfo.getNickname());
//                                portraitPath = rankPlayerInfo.getPortrait_path();
//                                if (portraitPath != null && !"".equals(portraitPath.trim())) {
//                                    // 区分平台返回
//                                    switch (platform) {
//                                    case PlatformEnum.WEB:
//                                        jsonObject.addProperty("portraitPath_256", portraitPath + ConstantEnum.portrait_path_256_suffix);
//                                        break;
//                                    case PlatformEnum.ANDROID:
//                                    case PlatformEnum.IPHONE:
//                                    case PlatformEnum.IPAD:
//                                        jsonObject.addProperty("portraitPath_128", portraitPath + ConstantEnum.portrait_path_128_suffix);
//                                        break;
//                                    default:
//                                        break;
//                                    }
//                                }
//
//                                jsonObject.addProperty("order", order);
//                                score = (Double) map.get("score");
//                                jsonObject.addProperty("score", score);
//                                break;
//                            }
//                        }
//                    }
//                    beforeUserMap = map;
//                }
//            }
//            // 不在榜单内
//            if (order < 1 || score == -1) {
//                continue;
//            }
//            
//            if (order == 1) {
//                jsonObject.addProperty("beforeUser", "");
//            } else {
//                tempMap = gson.fromJson(((String) beforeUserMap.get("playerInfo")), new TypeToken<Map<String, Object>>(){}.getType());
//                if (tempMap != null) {
//                    rankPlayerInfo = gson.fromJson(((String) tempMap.get("userInfo")), new TypeToken<RankPlayerInfo>(){}.getType());
//                    if (rankPlayerInfo != null) {
//                        JsonObject beforeUserJson = new JsonObject();
//                        beforeUserJson.addProperty("userId", rankPlayerInfo.getUserId());
//                        beforeUserJson.addProperty("nickname", rankPlayerInfo.getNickname());
//                        beforeUserJson.addProperty("score", ((Double) beforeUserMap.get("score")).longValue());
//                        beforeUserJson.addProperty("order", order - 1);
//                        
//                        portraitPath = rankPlayerInfo.getPortrait_path();
//                        if (portraitPath != null && !"".equals(portraitPath.trim())) {
//                            // 区分平台返回
//                            switch (platform) {
//                            case PlatformEnum.WEB:
//                                beforeUserJson.addProperty("portraitPath_256", portraitPath + ConstantEnum.portrait_path_256_suffix);
//                                break;
//                            case PlatformEnum.ANDROID:
//                            case PlatformEnum.IPHONE:
//                            case PlatformEnum.IPAD:
//                                beforeUserJson.addProperty("portraitPath_128", portraitPath + ConstantEnum.portrait_path_128_suffix);
//                                break;
//                            default:
//                                break;
//                            }
//                        }
//                        
//                        jsonObject.add("beforeUser", beforeUserJson);
//                    } else {
//                        jsonObject.addProperty("beforeUser", "");
//                    }
//                } else {
//                    jsonObject.addProperty("beforeUser", "");
//                }
//            }
//            jsonArray.add(jsonObject);
//        }
//        if (jsonArray.size() < 1) {
//            result.addProperty("userRankList", "");
//        } else {
//            result.add("userRankList", jsonArray);
//            result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
//        }
//
//        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
//        
//        return result;
    }
	
    /**
     * 获取日半价周星礼物用户\主播榜单（20010008）
     * @param jsonObject
     * @return
     * @throws Exception
     */
    public JsonObject getDayHalfGiftRank(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		
		JsonObject result = new JsonObject();
		result.addProperty("TagCode", TagCodeEnum.FUNCTAG_INVALID_EXCEPTION);
		
//		int rankType, pageIndex, pageCount;
//		String giftDate;
//		try {
//		    rankType = CommonUtil.getJsonParamInt(jsonObject, "rankType", 0, null, 0, 1);
//		    giftDate = CommonUtil.getJsonParamString(jsonObject, "giftDate", null, "10080001", 10, 10);
//		    pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
//		    pageCount = CommonUtil.getJsonParamInt(jsonObject, "pageCount", 10, null, 1, Integer.MAX_VALUE);
//        } catch (CommonUtil.ErrorGetParameterException e) {
//            result.addProperty("TagCode", e.getErrCode());
//            return result;
//        } catch (Exception e) {
//            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
//            return result;
//        }
//		
//		List<Map<String, Object>> rankList = null;
//		Gson gson = new Gson();
//		
//		String cacheKey = "ActivityFunctions.getDayHalfGiftRank." + rankType + "." + giftDate;
//		String cacheValue = HotDataSource.getTempDataString(cacheKey);
//		if (!StringUtil.strIsNull(cacheValue)) {
//		    rankList = gson.fromJson(cacheValue, new TypeToken<List<Map<String, Object>>>(){}.getType());
//        } else {
//            // 获取缓存中的半价礼物ID
//            String halfGiftIds = HotDataSource.getTempDataString("ActivityFunctions.getDayHalfGiftRank.halfGiftIds");
//            if (!StringUtil.strIsNull(halfGiftIds)) {
//                Map<String, Object> map = new HashMap<String, Object>();
//                map.put("startTime", DateUtil.parseDateStringToDate(giftDate, "yyyy-MM-dd"));
//                map.put("endTime", DateUtil.addOnField(DateUtil.parseDateStringToDate(giftDate, "yyyy-MM-dd"), Calendar.DATE, 1));
//                map.put("giftIds", halfGiftIds);
//                
//                try {
//                    if (rankType == 0) {
//                        rankList = SqlMapClientHelper.getInstance(DB.MASTER).queryForList("Resource.getDayHalfActorGiftRank", map);
//                    } else {
//                        rankList = SqlMapClientHelper.getInstance(DB.MASTER).queryForList("Resource.getDayHalfUserGiftRank", map);
//                    }
//                    
//                    if (rankList != null && rankList.size() > 0) {
//                        HotDataSource.setTempDataString(cacheKey, gson.toJson(rankList), 60);
//                    } else {
//                        HotDataSource.setTempDataString(cacheKey, "     ", 60);
//                    }
//                } catch (Exception e) {
//                    result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
//                    return result;
//                }
//            }
//        }
//		
//		int count = 0;
//		JsonArray rankPlayerList = new JsonArray();
//		if (rankList != null && rankList.size() > 0) {
//		    count = rankList.size();
//		    
//		    int startIndex = (pageIndex - 1) * pageCount;
//		    int endIndex = pageIndex * pageCount - 1;
//		    
//		    Map<String, Object> rankInfo;
//		    for (int i = 0; i < rankList.size(); i++) {
//		        if (i < startIndex) {
//                    continue;
//                }
//		        
//		        if (i > endIndex) {
//                    break;
//                }
//		        
//		        rankInfo = rankList.get(i);
//		        
//		        JsonObject playerJson = new JsonObject();
//		        int userId = ((Double) rankInfo.get("userId")).intValue();
//		        playerJson.addProperty("giftId", ((Double) rankInfo.get("giftId")).intValue());
//		        playerJson.addProperty("giftName", (String) rankInfo.get("giftName"));
//		        playerJson.addProperty("userId", userId);
//		        playerJson.addProperty("nickname", HotDataSource.getHotFieldValue(String.valueOf(userId), "nickname"));
//		        playerJson.addProperty("actorLevel", UserService.getActorLevel(userId));
//		        playerJson.addProperty("richLevel", UserService.getRichLevel(userId));
//		        playerJson.addProperty("giftCount", ((Double) rankInfo.get("giftCount")).longValue());
//		        
//		        String portraitPath = null;
//		        try {
//		            KkUserService userService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
//		            UserProfile userProfile = userService.getUserProfile(userId);
//		            if (userProfile != null) {
//		                portraitPath = userProfile.getPortrait();
//		            }
//		        } catch (Exception e) {
//		            logger.error("fail to get KkUserService.getUserProfile, userId: " + userId, e);
//		        }
//		        if (!StringUtil.strIsNull(portraitPath)) {
//		            playerJson.addProperty("portraitPath", portraitPath);
//		            playerJson.addProperty("portraitPath_128", portraitPath + "!128");
//		            playerJson.addProperty("portraitPath_256", portraitPath + "!256");
//                }
//		        
//		        rankPlayerList.add(playerJson);
//		    }
//		}
//		
//		result.addProperty("count", count);
//		result.add("rankPlayerList", rankPlayerList);
//        result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
//        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        
		return result;
	}

	/**
	 * 获取房间活动入口信息 (20010010)
	 * @param paramJsonObject
	 * @return
	 */
    public JsonObject getRoomActivityInfo(JsonObject paramJsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
//        int platform, userId = 0, familyId = 0;
//        try {
//            userId = CommonUtil.getJsonParamInt(paramJsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
//            familyId = CommonUtil.getJsonParamInt(paramJsonObject, "familyId", 0, null, 1, Integer.MAX_VALUE);
//            platform = CommonUtil.getJsonParamInt(paramJsonObject, "platform", PlatformEnum.WEB, null, PlatformEnum.WEB, PlatformEnum.IPAD);
//        } catch (CommonUtil.ErrorGetParameterException e) {
//            result.addProperty("TagCode", e.getErrCode());
//            return result;
//        } catch (Exception e) {
//            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
//            return result;
//        }
//        
//        if (familyId == 0) {
//            RoomInfo roomInfo = RoomService.getRoomInfo(userId);
//            if (roomInfo != null && roomInfo.getFamilyId() != null) {
//                familyId = roomInfo.getFamilyId();
//            }
//        }
//        
//        String cacheKey = "getRoomActivityInfo_%s_%s_%s";
//        String cacheValue = HotDataSource.getTempDataString(String.format(cacheKey, userId, familyId, platform));
//        if (!StringUtil.strIsNull(cacheValue)) {
//            return new JsonParser().parse(cacheValue).getAsJsonObject();
//        }
//        
//        
//        // 获取房间活动入口信息接口
//        RoomShowActivity interfaceActivity = MsgClientInterfaceActivityAgent.getNeedRoomShowActivity(userId, familyId);
//        
//        if (interfaceActivity != null ) {
//            int giftId = StringUtil.parseFromStr(interfaceActivity.getShowGift(), 0);
//            if (giftId > 0) {
//                result.addProperty("giftId", giftId);
//            } else {
//                result.addProperty("giftId", 0);
//            }
//            
//            switch (platform) {
//            case PlatformEnum.ANDROID:
//            	if (!StringUtil.strIsNull(interfaceActivity.getIcon())) {
//                    result.addProperty("activityIcon", ConfigHelper.getHttpdir() + interfaceActivity.getIcon() + "android.jpg");
//                } else {
//                    result.addProperty("activityIcon", "");
//                }
//                
//                if (giftId > 0) {
//                    result.addProperty("androidIcon", ConfigHelper.getGiftIconAndroidResURL() + giftId + ".png");
//                }
//                
//                result.addProperty("activityUrl", interfaceActivity.getUrlAndroid());
//                break;
//                
//            case PlatformEnum.IPHONE:
//            case PlatformEnum.IPAD:
//            case PlatformEnum.IPHONE_GAMAGIC:
//                if (!StringUtil.strIsNull(interfaceActivity.getIcon())) {
//                    result.addProperty("activityIcon", ConfigHelper.getHttpdir() + interfaceActivity.getIcon() + "ios.jpg");
//                } else {
//                    result.addProperty("activityIcon", "");
//                }
//                
//                if (giftId > 0) {
//                    result.addProperty("iphoneIcon", ConfigHelper.getGiftIconIphoneResURL() + giftId + ".png");
//                }
//                
//                result.addProperty("activityUrl", interfaceActivity.getUrlIos());
//                break;
//                
//            default:
//                if (!StringUtil.strIsNull(interfaceActivity.getIcon())) {
//                    result.addProperty("activityIcon", ConfigHelper.getHttpdir() + interfaceActivity.getIcon() + "web.jpg");
//                } else {
//                    result.addProperty("activityIcon", "");
//                }
//                result.addProperty("activityUrl", interfaceActivity.getUrlWeb());
//                break;
//            }
//            
//            result.addProperty("statisticUnit", interfaceActivity.getStatisticUnit());
//            result.addProperty("backgroundRgb", interfaceActivity.getBackgroundRgb());
//            result.addProperty("textRgb", interfaceActivity.getTextRgb());
//            result.addProperty("roomShowType", interfaceActivity.getRoomShowType());
//            result.addProperty("order", interfaceActivity.getOrder());
//            result.addProperty("totalCount", interfaceActivity.getTotalCount());
//            result.addProperty("diffCount", interfaceActivity.getDiffCount());
//            result.addProperty("familyOrder", interfaceActivity.getFamilyOrder());
//            result.addProperty("familyTotalCount", interfaceActivity.getFamilyTotalCount());
//            result.addProperty("familyDiffCount", interfaceActivity.getFamilyDiffCount());
//        }

        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        
        // 缓存
//        HotDataSource.setTempDataString(String.format(cacheKey, userId, familyId, platform), result.toString(), 5);
        
        return result;
    }
    
    /**
     * 获取用户首充礼包信息(20010013)
     * @param jsonObject
     * @return
     */
    @SuppressWarnings("unchecked")
    public JsonObject getUserFirstRechargePackageInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int appId, userId = 0;
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
        
        Map<String, Integer> map = new HashMap<String, Integer>();
        map.put("userId", userId);
        map.put("appId", appId);
        Map<String, Object> userFrPackage = null;
        try {
            userFrPackage = (Map<String, Object>) SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Other.getUserFirstRechargePackageInfo", map);
        } catch (SQLException e) {
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
            return result;
        }
        
        if (userFrPackage != null) {
            result.addProperty("packageId", Integer.parseInt(String.valueOf(userFrPackage.get("PACKAGEID"))));
            result.addProperty("status", Integer.parseInt(String.valueOf(userFrPackage.get("STATUS"))));
            result.addProperty("orderId", String.valueOf(userFrPackage.get("ORDERID")));
        } else {
            result.addProperty("packageId", 0);
            result.addProperty("status", 0);
        }
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 用户领取首充礼包(20010012)
     * @param jsonObject
     * @return
     */
	public JsonObject receiveUserFirstRechargePackage(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
//        int packageId, appId, userId = 0;
//        try {
//            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
//            appId = CommonUtil.getJsonParamInt(jsonObject, "a", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
//            packageId = CommonUtil.getJsonParamInt(jsonObject, "packageId", 0, "10120001", 1, Integer.MAX_VALUE);
//        } catch (CommonUtil.ErrorGetParameterException e) {
//            result.addProperty("TagCode", e.getErrCode());
//            return result;
//        } catch (Exception e) {
//            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
//            return result;
//        }
//        
//        // 校验用户的首充礼包ID是否为传入的 packageId
//        Map<String, Integer> map = new HashMap<String, Integer>();
//        map.put("userId", userId);
//        map.put("appId", appId);
//        Map<String, Object> userFrPackage = null;
//        try {
//        	//首先查询用户礼包信息
//            userFrPackage = (Map<String, Object>) SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Other.getUserFirstRechargePackageInfo", map);
//        } catch (SQLException e) {
//            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
//            return result;
//        }
//        
//        // 如果用户不存在首充礼包
//        if (userFrPackage == null) {
//            result.addProperty("TagCode", "10120002");
//            return result;
//		}
//        
//        // 如果用户的首充礼包ID不是传入的 packageId
//        if (packageId != Integer.parseInt(String.valueOf(userFrPackage.get("PACKAGEID")))) {
//            result.addProperty("TagCode", "10120003");
//            return result;
//		}
//        
//        // 如果用户的首充礼包ID已领取过
//        if (Integer.parseInt(String.valueOf(userFrPackage.get("STATUS"))) > 0) {
//            result.addProperty("TagCode", "10120005");
//            return result;
//        }
//
//        // 如果用户的首充礼包ID和传入的 packageId相同
//        HistPackageDealService histPackageDealService = (HistPackageDealService) MelotBeanFactory.getBean("histPackageDealService");
//        boolean flag = histPackageDealService.insertSendPackage(userId, packageId, 1, GiftPackageEnum.PACKAGE_SEND, "首充活动赠送礼包", 0);
//        if (!flag) {
//            result.addProperty("TagCode", "10120004");
//            return result;
//        }
//        
//        // 首充礼包领取成功，修改礼包状态
//        map.put("packageId", packageId);
//        map.put("status", 1);
//        try {
//            SqlMapClientHelper.getInstance(DB.MASTER).update("Other.updateFirstRechargePackageStatus", map);
//        } catch (SQLException e) {
//            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
//            return result;
//        }
//        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        result.addProperty("TagCode", TagCodeEnum.FUNCTAG_INVALID_EXCEPTION);
        return result;
    }
    
    /**
     * 兑吧新用户注册VIP礼包赠送(50001001)
     * @param jsonObject
     * @return
     */
    public JsonObject addDuibaNewUserPresent(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int userId, chanelId;
        String phoneNum;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            phoneNum = CommonUtil.getJsonParamString(jsonObject, "phoneNum", null, "01010006", 1, Integer.MAX_VALUE);
            chanelId = CommonUtil.getJsonParamInt(jsonObject, "c", 0, TagCodeEnum.CHANNEL_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        // 校验用户是否为兑吧注册新用户且未领取过礼物
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put("userId", userId);
        map.put("phoneNum", phoneNum);
        map.put("chanelId", chanelId);
        try {
            SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Temp.checkDuibaNewuser", map);
        } catch (SQLException e) {
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
            return result;
        }
        String TagCode = (String) map.get("TagCode");
        // 兑吧注册新用户赠送三天VIP
        if (TagCode.equals(TagCodeEnum.SUCCESS)) {
            //送三天VIP
            String presentDesc = "兑吧注册新用户赠送3天VIP";
            VipService vipService = (VipService) MelotBeanFactory.getBean("vipService");
            ResVip resVip = vipService.insertSendVipV2(userId, 3, 100001, 7, presentDesc, chanelId);
            if (resVip != null && (resVip.getRespCode() == VipService.AssetModule_TAG_CODE_SUCCESS ||
                resVip.getRespCode() == VipService.SendVipHandler_RESP_CODE_LIFE_TIME_FOREVER)) {
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            } else {
                result.addProperty("TagCode", "01010005");
                return result;
            }
        } else {
            result.addProperty("TagCode", "010100" + TagCode);
            return result;
        }
    }    
	
    /**
     * 房间内活动信息展示2.0(50001002)
     * getRoomActivityDetail
     */
    public JsonObject getRoomActivityDetail(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        
        int roomId;
        int familyId;
        int platform;
        String version;
        
        try {
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, TagCodeEnum.ROOMID_MISSING, 1, Integer.MAX_VALUE);
            familyId = CommonUtil.getJsonParamInt(jsonObject, "familyId", 0, TagCodeEnum.FAMILYID_MISSING, 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, TagCodeEnum.PLATFORM_MISSING, 1, 10);
            version = CommonUtil.getJsonParamString(jsonObject, "version", null, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            JsonObject paramJson = new JsonObject();
            paramJson.addProperty("roomId", roomId);
            paramJson.addProperty("familyId", familyId);
            paramJson.addProperty("platform", platform);
            if (version != null) {
                paramJson.addProperty("version", version);
            }
            MissionService missionService = (MissionService) MelotBeanFactory.getBean("missionService");
            result = missionService.getVisiableActivityByVersion(paramJson);
        } catch (Exception e) {
            logger.error("调用MissionService模块异常", e);
        }
        
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 第三方新用户注册VIP礼包赠送(50001011)
     * @param jsonObject
     * @return
     */
    public JsonObject addThirdChannelNewUserPresent(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int userId;
        String phoneNum;
        
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            phoneNum = CommonUtil.getJsonParamString(jsonObject, "phoneNum", null, "01110006", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        // 校验用户是否为第三方注册新用户且未领取过礼物
        Map<Object, Object> map = new HashMap<Object, Object>();
        map.put("userId", userId);
        map.put("phoneNum", phoneNum);
        
        try {
            SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Temp.checkThirdChannelNewUser", map);
        } catch (SQLException e) {
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
            return result;
        }
        String TagCode = (String) map.get("TagCode");
        // 第三方注册新用户赠送三天VIP
        if (TagCode.equals(TagCodeEnum.SUCCESS)) {
            int channelId = (Integer) map.get("channelId");
            String presentDesc = "渠道号:" + channelId + "第三方注册新用户赠送3天VIP";
            VipService vipService = (VipService) MelotBeanFactory.getBean("vipService");
            ResVip resVip = vipService.insertSendVipV2(userId, 3, 100001, 7, presentDesc, channelId);
            if (resVip != null && (resVip.getRespCode() == VipService.AssetModule_TAG_CODE_SUCCESS ||
                resVip.getRespCode() == VipService.SendVipHandler_RESP_CODE_LIFE_TIME_FOREVER)) {
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            } else {
                result.addProperty("TagCode", "01110005");
                return result;
            }
        } else {
            result.addProperty("TagCode", "011100" + TagCode);
            return result;
        }
    }    
    
    /**
     * 获取用户已获得礼包列表(50010014)
     * @param jsonObject
     * @return
     */
    @SuppressWarnings("unchecked")
    public JsonObject getUserReceivePackageList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int appId, userId = 0;
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
        
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("userId", userId);
        param.put("appId", appId);
        try {
            SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Other.getUserRechargePackageList", param);
        } catch (SQLException e) {
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
            return result;
        }

        List<RechargerPackage> userRechargePackage = (List<RechargerPackage>) param.get("list");
        JsonArray jsonArray = new JsonArray();
        if (userRechargePackage != null) {
            for (RechargerPackage userFrPackage : userRechargePackage) {
                JsonObject jsonObject2 = new JsonObject();
                jsonObject2.addProperty("packageId", userFrPackage.getPackageId());
                jsonObject2.addProperty("status", userFrPackage.getStatus());
                jsonObject2.addProperty("orderId", userFrPackage.getOrderId());
                jsonObject2.addProperty("isRecive", userFrPackage.getIsRecive());
                jsonArray.add(jsonObject2);
            }
        }
        
        // 获取用户充值次数
        param.clear();
        param.put("userId", userId);
        int count = 0;
        try {
            SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("Other.getUserRechargeTimes", param);
            count = param.get("count") != null ? (Integer) param.get("count") : 0;
        } catch (SQLException e) {
            logger.error("fail to execute Other.getUserRechargeTimes, userId: " + userId, e);
        }
        
        result.add("packageList", jsonArray);
        result.addProperty("rechargeTimes", count);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 获取用户分享回馈明细列表(20010019)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getUserPromotionRewardList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
    	JsonObject result = new JsonObject();
    	if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
    	
    	int userId, start, end;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, "start", 1, null, 1, Integer.MAX_VALUE);
            end = CommonUtil.getJsonParamInt(jsonObject, "end", start, null, start, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        Integer count = null, invitedCount = null;
        JsonArray jsonArray = new JsonArray();
		FeedbackService feedbackService = (FeedbackService) MelotBeanFactory.getBean("feedbackService");
		try {
			count = feedbackService.getCountByUserId(userId);
			invitedCount = feedbackService.getInviteCountByUserId(userId);
		} catch (Exception e) {
			logger.error("call FeedbackService getCountByUserId catched exception, userId : " + userId, e);
		}
		if (invitedCount != null) {
			result.addProperty("invitedCount", invitedCount);
		} else {
			result.addProperty("invitedCount", 0);
		}
		if (count == null || count == 0) {
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			result.addProperty("count", 0);
			result.add("rewardList", jsonArray);
			return result;
		}
		int limit = end - start + 1;
		int offset = start - 1;
		try {
			List<Award> awardList = feedbackService.getAwardListByUserId(userId, limit, offset);
			if (awardList != null && awardList.size() > 0) {
				for (Award award : awardList) {
					JsonObject json = new JsonObject();
					json.addProperty("userId", award.getUserId());
					if (award.getNickName() != null) {
						json.addProperty("nickname", award.getNickName());
					}
					String portraitAddress;
					if (award.getPortrait() != null) {
						portraitAddress = award.getPortrait().startsWith("http://") ? award.getPortrait() : ConfigHelper.getHttpdir() + award.getPortrait();
						json.addProperty("portrait_path_original", portraitAddress);
						json.addProperty("portrait_path_48", portraitAddress + "!48");
						json.addProperty("portrait_path_128", portraitAddress + "!128");
						json.addProperty("portrait_path_256", portraitAddress + "!256");
						json.addProperty("portrait_path_272", portraitAddress + "!272");
						json.addProperty("portrait_path_1280", portraitAddress + "!1280");
						json.addProperty("portrait_path_400", portraitAddress + "!400");
						json.addProperty("portrait_path_756", portraitAddress + "!756x567");
					}
					json.addProperty("showMoney", award.getAward());
					jsonArray.add(json);
				}
			}
		} catch (Exception e) {
			count = 0;
			logger.error("call FeedbackService getAwardListByUserId catched exception, userId : " + userId + ", limit : " + limit + ", offset : " + offset, e);
		}
		
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		result.addProperty("count", count);
		result.add("rewardList", jsonArray);
		return result;
    }
    
    /**
     * 获取主播分享回馈明细列表(20010020)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getActorPromotionRewardList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
    	JsonObject result = new JsonObject();
    	if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
    	
    	int userId, start, end;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, "start", 1, null, 1, Integer.MAX_VALUE);
            end = CommonUtil.getJsonParamInt(jsonObject, "end", start, null, start, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        Integer count = null;
        JsonArray jsonArray = new JsonArray();
		FeedbackService feedbackService = (FeedbackService) MelotBeanFactory.getBean("feedbackService");
		try {
			count = feedbackService.getCountByActorId(userId);
		} catch (Exception e) {
			logger.error("call FeedbackService getCountByUserId catched exception, userId : " + userId, e);
		}
		if (count == null || count == 0) {
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			result.addProperty("count", 0);
			result.addProperty("invitedCount", 0);
			result.add("rewardList", jsonArray);
			return result;
		}
		int limit = end - start + 1;
		int offset = start - 1;
		try {
			List<Award> awardList = feedbackService.getAwardListByActorId(userId, limit, offset);
			if (awardList != null && awardList.size() > 0) {
				for (Award award : awardList) {
					JsonObject json = new JsonObject();
					json.addProperty("userId", award.getUserId());
					if (award.getNickName() != null) {
						json.addProperty("nickname", award.getNickName());
					}
					String portraitAddress;
					if (award.getPortrait() != null) {
						portraitAddress = award.getPortrait().startsWith("http://") ? award.getPortrait() : ConfigHelper.getHttpdir() + award.getPortrait();
						json.addProperty("portrait_path_original", portraitAddress);
						json.addProperty("portrait_path_48", portraitAddress + "!48");
						json.addProperty("portrait_path_128", portraitAddress + "!128");
						json.addProperty("portrait_path_256", portraitAddress + "!256");
						json.addProperty("portrait_path_272", portraitAddress + "!272");
						json.addProperty("portrait_path_1280", portraitAddress + "!1280");
						json.addProperty("portrait_path_400", portraitAddress + "!400");
						json.addProperty("portrait_path_756", portraitAddress + "!756x567");
					}
					json.addProperty("showMoney", award.getAward());
					jsonArray.add(json);
				}
			}
		} catch (Exception e) {
			count = 0;
			logger.error("call FeedbackService getAwardListByUserId catched exception, userId : " + userId + ", limit : " + limit + ", offset : " + offset, e);
		}
		
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		result.addProperty("count", count);
		result.add("rewardList", jsonArray);
		return result;
    }
    
    /**
     * 记录房间分享信息(20010021)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject recordRoomShare(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
    	JsonObject result = new JsonObject();
    	
    	int userId, sharePlatform, shareSourceId, shareType,v,platform;
        String shareReason,sharelink;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
            sharePlatform = CommonUtil.getJsonParamInt(jsonObject, "sharePlatform", 1, TagCodeEnum.ROOMID_MISSING, 1, Integer.MAX_VALUE);
            shareSourceId = CommonUtil.getJsonParamInt(jsonObject, "shareSourceId", 0, TagCodeEnum.ROOMID_MISSING, 1, Integer.MAX_VALUE);
            shareType = CommonUtil.getJsonParamInt(jsonObject, "shareType", 0, TagCodeEnum.ROOMID_MISSING, 1, Integer.MAX_VALUE);
        
            shareReason = CommonUtil.getJsonParamString(jsonObject, "shareReason", null, null, 1, Integer.MAX_VALUE);
            sharelink = CommonUtil.getJsonParamString(jsonObject, "sharelink", null, null, 1, Integer.MAX_VALUE);
            v = CommonUtil.getJsonParamInt(jsonObject, "v", 0, null, 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        MissionService missionService = MelotBeanFactory.getBean("missionService", MissionService.class);
        if (missionService != null) {
        	ShareInfo shareInfo = new ShareInfo();
        	if (userId != 0) {
        		shareInfo.setUserId(userId);
        	}
        	shareInfo.setSharedPlatform(sharePlatform);
        	shareInfo.setSharedType(shareType);
        	shareInfo.setSharedSourceId(shareSourceId);
        	
        	// 分享视频动态、普通动态 title和link不能为空
            if (shareType == 6 || shareType == 5 ) {
                if ((platform == PlatformEnum.ANDROID && v > 99) || (platform == PlatformEnum.IPHONE && v > 131)) {
                    if (StringUtils.isBlank(shareReason)) {
                        shareReason = "0";
                    }
                    if (StringUtils.isBlank(sharelink)) {
                        result.addProperty("TagCode", TagCodeEnum.SHARE_LINK_IS_NULL);
                        return result;
                    }
                }
            }
            shareInfo.setShareReason(shareReason);
            shareInfo.setShareLink(sharelink);
        	
        	JsonObject json = missionService.share(shareInfo);
        	if (json != null && json.get("TagCode").getAsString().equals("00000000")) {
        		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        	} else {
        		result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        	}
        } else {
        	result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
        }
        
        return result;
    }
    
    /**
     * 获取房间分享金库(20010022)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getActorShareCoffers(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
    	JsonObject result = new JsonObject();
    	
    	int roomId;
        try {
        	roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, TagCodeEnum.ROOMID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        String isopen = SystemConfig.getValue(SystemConfig.actorShareCoffer, AppIdEnum.AMUSEMENT);
        if ("-1".equals(isopen)) {
        	result.addProperty("amount", -1);
        	result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        	return result;
        }
        
        long amount = 0;
        try {
			ShareService shareService = MelotBeanFactory.getBean("shareService", ShareService.class);
			amount = shareService.getShareCoffersByRoomId(roomId);
		} catch (Exception e) {
			logger.error("call ShareService getShareCoffersByRoomId catched exception, roomId : " + roomId, e);
			result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
			return result;
		}
        amount = amount < 0 ? 0 : amount;
        amount = amount > 100000 ? 100000 : amount;
        result.addProperty("amount", amount);
        result.addProperty("limit", Integer.valueOf(SystemConfig.getValue(SystemConfig.shareCofferLimit, AppIdEnum.AMUSEMENT)));
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
    }
    
    /**
     * 获取房间粉丝回馈配置信息(50001031)
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getFanFeedBackConf(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int roomId;
        try {
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, null, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            result.addProperty("fanFeedbackMax", Integer.valueOf(SystemConfig.getValue(SystemConfig.fanFeedbackMax, AppIdEnum.AMUSEMENT)));
            result.addProperty("fanFeedbackMin", Integer.valueOf(SystemConfig.getValue(SystemConfig.fanFeedbackMin, AppIdEnum.AMUSEMENT)));
            result.addProperty("fanFeedbackDayLimit", Integer.valueOf(SystemConfig.getValue(SystemConfig.fanFeedbackDayLimit, AppIdEnum.AMUSEMENT)));
            
            if (roomId > 0) {
                ShareService shareService = MelotBeanFactory.getBean("shareService", ShareService.class);
                long amount = 0;
                if ("1".equals(shareService.getFanFeedBackInfo(roomId).get("isOpen"))) {
                    int fanFeedbackEndAmount = Integer.valueOf(SystemConfig.getValue(SystemConfig.fanFeedbackEndAmount, AppIdEnum.AMUSEMENT));
                    amount = shareService.getShareCoffersByRoomId(roomId) - fanFeedbackEndAmount;
                }
                result.addProperty("fanFeedbackAmount", amount > 0 ? amount : 0);
            }
        } catch (Exception e) {
            logger.error("call ShareService getShareCoffersByRoomId catched exception, roomId : " + roomId, e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
        
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 获取平台可用游戏列表【51050101】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getGameList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int platform;
        int v;
        try {
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, null, 1, Integer.MAX_VALUE);
            v = CommonUtil.getJsonParamInt(jsonObject, "v", 0, null, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            GameConfigService gameConfigService = MelotBeanFactory.getBean("gameConfigService", GameConfigService.class);
            List<GameConfig> gameConfigs = gameConfigService.getGameList(platform, v);
            JsonArray games = new JsonParser().parse(new Gson().toJson(gameConfigs)).getAsJsonArray();
            result.add("games", games);
        } catch (Exception e) {
            logger.error(String.format("Module Error gameConfigService.getGameList(%s, %s)", platform, v), e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
        
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 获取游戏对应礼物列表【51050102】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getGameGiftList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int gameId;
        try {
            gameId = CommonUtil.getJsonParamInt(jsonObject, "gameId", 0, "5105010201", 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            GameConfigService gameConfigService = MelotBeanFactory.getBean("gameConfigService", GameConfigService.class);
            List<GameGift> gameConfigs = gameConfigService.getGameGiftList(gameId);
            List<Integer> giftIds = new ArrayList<>();
            for (GameGift gameGift : gameConfigs) {
                giftIds.add(gameGift.getGiftId());
            }
            result.add("giftIds", new JsonParser().parse(new Gson().toJson(giftIds)));
        } catch (Exception e) {
            logger.error(String.format("Module Error gameConfigService.getGameGiftList(%s)", gameId), e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_RETURN_NULL);
            return result;
        }
        
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
}

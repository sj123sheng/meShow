package com.melot.kktv.action;

import com.google.common.collect.Maps;
import com.google.gson.*;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.content.config.domain.ApplyContractInfo;
import com.melot.family.driver.domain.FamilyInfo;
import com.melot.kk.opus.api.constant.OpusCostantEnum;
import com.melot.kkcore.user.api.ShowMoneyHistory;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcx.service.FamilyService;
import com.melot.kkcx.service.RoomService;
import com.melot.kkcx.service.UserService;
import com.melot.kkcx.transform.NewsCommentTF;
import com.melot.kkcx.transform.NewsTF;
import com.melot.kktv.model.News;
import com.melot.kktv.model.NewsComment;
import com.melot.kktv.model.NewsTagConf;
import com.melot.kktv.model.SysMsg;
import com.melot.kktv.redis.NewsSource;
import com.melot.kktv.service.LiveVideoService;
import com.melot.kktv.service.NewsService;
import com.melot.kktv.util.*;
import com.melot.kktv.util.CommonUtil.ErrorGetParameterException;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;
import com.melot.news.model.NewsInfo;
import com.melot.sdk.core.util.MelotBeanFactory;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.util.*;

/**
 * 动态的接口类
 * 
 * @author LY
 * 
 */
public class NewsFunctions {

	/** 日志记录对象 */
	private static Logger logger = Logger.getLogger(NewsFunctions.class);

	/***************************************************** 旧版动态   **************************************************************/
	/**
	 * 删除动态(10006003)
	 * 
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return 结果字符串
	 */
	public JsonObject deleteNews(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}
		// 获取参数
		JsonElement userIdje = jsonObject.get("userId");
		JsonElement newsIdje = jsonObject.get("newsId");

		// 验证参数
		int userId;
		int newsId;
		if (userIdje != null && !userIdje.isJsonNull() && !userIdje.getAsString().equals("")) {
			// 验证数字
			try {
				userId = userIdje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "06030002");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "06030001");
			return result;
		}
		if (newsIdje != null && !newsIdje.isJsonNull() && !newsIdje.getAsString().equals("")) {
			// 验证数字
			try {
				newsId = newsIdje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "06030004");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "06030003");
			return result;
		}

		// 调用存储过程得到结果
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("userId", userId);
		map.put("newsId", newsId);
		News news = null;
		try {
			news = (News)SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("News.getNewsInfo", newsId);
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("News.deleteNews", map);
		} catch (SQLException e) {
			logger.error("未能正常调用存储过程", e);
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			return result;
		}
		String TagCode = (String) map.get("TagCode");
		if (TagCode.equals(TagCodeEnum.SUCCESS)) {
		    // 删除临时待审核的文件
		    LiveVideoService.delTempUserResourceById(null, userId, OpusCostantEnum.CHECKING_NEWS_RES_TYPE, newsId, null);
			// 删除七牛或又拍云文件
		    String lastNewsMediaSourceStr = news.getMediaSource();
		    int newsType = news.getNewsType() == null ? 0 : news.getNewsType();
		    if (newsType != NewsTypeEnum.UPLOAD_PHOTO && !StringUtil.strIsNull(lastNewsMediaSourceStr)) {
		        JsonObject lastNewsMediaSourceObj = new JsonParser().parse(lastNewsMediaSourceStr).getAsJsonObject();
		        if (lastNewsMediaSourceObj.has("imageUrl")) {
		            String imageUrl = lastNewsMediaSourceObj.get("imageUrl").getAsString();
		            if (!StringUtil.strIsNull(imageUrl) && !OpusCostantEnum.CHECKING_NEWS_RESOURCEURL.equals(imageUrl)) {
		                NewsService.delNewsOnThirdPart(news);
		            }
		        }
		    }

            // 删除redis中的缓存（推荐动态与热拍动态）
            NewsSource.delRecAndRepaiNews(String.valueOf(newsId));

			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "" + TagCode + "");
			return result;
		} else if (TagCode.equals("03") || TagCode.equals("04")) {
			/* '03'; -- 动态不存在 */
			/* '04'; -- 动态不属于该用户 */
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "060301" + TagCode + "");
			return result;
		} else {
			// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
			logger.error("调用存储过程未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
			return result;
		}
	}

	/**
	 * 获取用户动态列表(10006004)
	 * 
	 * @param jsonObject 请求对象
	 * @return 结果字符串
	 */
	public JsonObject getUserNewsList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		
		JsonObject result = new JsonObject();
		// 定义所需参数
		int userId;
		int pageIndex = 1;
		int countPerPage = Constant.return_news_count;
		int platform = 0;
		int inRoom = 0;
		int version = 1;
		// 解析参数
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 1, "06040001", 1, Integer.MAX_VALUE);
			pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
			countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", Constant.return_news_count, null, 1, Integer.MAX_VALUE);
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
			inRoom = CommonUtil.getJsonParamInt(jsonObject, "inRoom", 0, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
			version = CommonUtil.getJsonParamInt(jsonObject, "version", 1, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
		} catch (ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		}

		List<News> checkList = new ArrayList<News>();
		HashSet<Integer> checkIds = new HashSet<Integer>();
		if (checkTag) {
			List<NewsInfo> newsList = NewsService.getSelfNewsList(userId, (pageIndex - 1) * countPerPage, countPerPage, 1, 0);
			if (newsList != null && newsList.size() > 0) {
				for (NewsInfo newsInfo : newsList) {
					if (newsInfo.getState() == 3) {
						News news = new News();
						news.setNewsId(newsInfo.getNewsId());
						news.setNewsType(newsInfo.getNewsType());
						news.setMediaFrom(1);
						news.setMediaSource("{\"mediaType\":1,\"imageUrl\":\"/picture/offical/checkingpicture.jpg\"}");
						news.setPublishedTime(newsInfo.getPublishedTime());
						news.setContent(newsInfo.getContent());
						news.setUserId(newsInfo.getUserId());
						checkIds.add(newsInfo.getNewsId());
						checkList.add(news);
					}
				}
			}
		}
		
		// 调用存储过程得到结果
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("userId", userId);
		map.put("inRoom", inRoom); 
		map.put("pageIndex", pageIndex);
		map.put("countPerPage", countPerPage);
		map.put("version", version);
		map.put("platform", platform);
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("News.getUserNewsList", map);
		} catch (SQLException e) {
			logger.error("未能正常调用存储过程", e);
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			return result;
		}
		String TagCode = (String) map.get("TagCode");
		if (TagCode.equals(TagCodeEnum.SUCCESS)) {
			// 取出列表
			@SuppressWarnings("unchecked")
			List<News> newsList = (ArrayList<News>) map.get("newsList");
			newsList = UserService.addUserExtra(newsList);
			result.addProperty("TagCode", TagCode);
			result.addProperty("pageTotal", (Integer) map.get("pageTotal"));
			JsonArray jNewsList = new JsonArray();
			
			if (newsList != null) {
				for (Object object : newsList) {
					if (checkIds != null && checkIds.size() > 0) {
						if (checkIds.contains(((News) object).getNewsId())) {
							checkIds.remove(((News) object).getNewsId());
						}
					}
					break;
				}
			}
			int flag = checkIds.size();
			if (newsList != null) {
				for (Object object : newsList) {
					if (flag ++ < newsList.size()) {
						jNewsList.add(NewsTF.toJsonObject((News) object, platform, inRoom));
					}
				}
			}
			
			if (checkIds.size() > 0) {
				for (News news : checkList) {
					if (checkIds.contains(news.getNewsId())) {
						jNewsList.add(NewsTF.toJsonObject(news, platform, inRoom));
					}
				}
			}
			
			// 返回结果
			result.add("newsList", jNewsList);
			result.addProperty("pathPrefix", ConfigHelper.getHttpdir()); // 图片前缀
			result.addProperty("mediaPathPrefix", ConfigHelper.getMediahttpdir()); // 多媒体前缀
			return result;
		} else if (TagCode.equals("02")) {
			/* '02';分页超出范围 */
			result.addProperty("TagCode", "060401" + TagCode);
			return result;
		} else {
			// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
			logger.error("调用存储过程未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
			result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
			return result;
		}
	}

	/**
	 * 打赏动态(10006013)
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return 结果字符串
	 */
	public JsonObject addRewardOnNews(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}

		// 获取参数
		JsonElement userIdje = jsonObject.get("userId");
		JsonElement newsIdje = jsonObject.get("newsId");
		JsonElement rewardCountje = jsonObject.get("rewardCount"); 
		// 验证参数
		int userId;
		int newsId;
		int rewardCount = 0;
		int rewardCost = 0;
		int totalCost = 0;
		String rewardText = "";
		if (userIdje != null && !userIdje.isJsonNull() && !userIdje.getAsString().equals("")) {
			// 验证数字
			try {
				userId = userIdje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "06130002"); //用户编号解析失败
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "06130001");  //用户Id缺失
			return result;
		}
		
		if (newsIdje != null && !newsIdje.isJsonNull() && !newsIdje.getAsString().equals("")) {
			// 验证数字
			try {
				newsId = newsIdje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "06130004"); //解析动态Id异常
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "06130003"); //动态Id缺失
			return result;
		}
		if (rewardCountje != null && !rewardCountje.getAsString().isEmpty() && !rewardCountje.isJsonNull() && rewardCountje.getAsInt() > 0) {
			rewardCount = rewardCountje.getAsInt();
			Map<String,Object> rewardMap = getRewardDBObj(rewardCount);
			rewardCost = (Integer) rewardMap.get("rewardCost");
			if(null != rewardMap.get("rewardDes")){
				rewardText = (String) rewardMap.get("rewardDes");
				totalCost = rewardCost * rewardCount;
				long showMoney = com.melot.kktv.service.UserService.getUserShowMoney(userId);
				if (showMoney < totalCost) {
					JsonObject result = new JsonObject();
					result.addProperty("showMoney", showMoney); // 剩余秀币
					result.addProperty("TagCode", "06130007"); // 余额不足
					return result;
				}
			} else {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "06130006"); //打赏rewardCount错误
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "06130005");  //打赏rewardCount缺失
			return result;
		}
		
		JsonObject result = new JsonObject();
		// 调用存储过程得到结果
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("userId", userId);
		map.put("rewardCount", rewardCount);
		map.put("rewardCost", rewardCost);
		map.put("rewardText", rewardText);
		map.put("newsId", newsId);
		// [打赏文案]，[打赏icon]x[打赏数量]
		try {
			// 打赏   用户秀币、评论流水等都在其中完成
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("News.addRewardOnNews", map);
			String TagCode = (String) map.get("TagCode");
			if (TagCode.equals(TagCodeEnum.SUCCESS)) {
				Integer getMoney = (Integer) map.get("getMoney");
				// 动态捧场 主播
				int ownuserId = (Integer) map.get("ownNewsUser");
				// 若动态发布人是主播 则打赏额度按照分成比例增加主播及家族收入
				RoomInfo roomInfo = RoomService.getRoomInfo(ownuserId);
				boolean flag = false;
				if (roomInfo != null && roomInfo.getType().intValue() == AppIdEnum.AMUSEMENT) {
					int familyId = 0;
					int actorRate = 0;
					int familyRate = 0;
					ApplyContractInfo applyContractInfo = RoomService.getApplyContractInfoByUserId(ownuserId);
					if (applyContractInfo != null && applyContractInfo.getFamilyId() != null && 
		                    applyContractInfo.getFamilyId().intValue() > 0 && roomInfo.getType().intValue() == AppIdEnum.AMUSEMENT) {
						
						if (applyContractInfo.getDistributRate() != null && applyContractInfo.getDistributRate().intValue() > 0) {
	                        // 获取家族主播分成比例
	                        actorRate = applyContractInfo.getDistributRate().intValue();
	                    }
						
						familyId = applyContractInfo.getFamilyId().intValue();
						FamilyInfo familyInfo = FamilyService.getFamilyInfoByFamilyId(familyId, roomInfo.getType().intValue());
                        if (familyInfo != null && familyInfo.getAssess() != null && familyInfo.getAssess() == 1) {
                            familyRate = 60 - actorRate;
                        }
					}
					RoomService.incActorIncome(userId, ownuserId, familyId,
							newsId, rewardCount, rewardCost, actorRate,
							familyRate, 8, getMoney);
					flag = true;
				}
				
				ShowMoneyHistory showMoneyHistory = new ShowMoneyHistory();
				try {
				    com.melot.kkcore.user.service.KkUserService kkuserService = (com.melot.kkcore.user.service.KkUserService) MelotBeanFactory.getBean("kkUserService");
					showMoneyHistory.setConsumeAmount(totalCost);
					showMoneyHistory.setUserId(userId);
					showMoneyHistory.setType(38);
					showMoneyHistory.setProductDesc("" + newsId);
					showMoneyHistory.setDtime(new Date());
					long consumeAmount = -totalCost;
					if (kkuserService.addAndGetUserAssets(userId, consumeAmount, true, showMoneyHistory) == null) {
                        logger.error("addAndGetUserAssets(" + new Gson().toJson(showMoneyHistory) + ") 执行失败");
                        logger.info("Failed: 打赏动态扣除秀币失败 " + new Gson().toJson(showMoneyHistory));
                    }
					if (!flag) {
					    long incomeAmount = getMoney;
					    ShowMoneyHistory showMoneyHistoryIncome = new ShowMoneyHistory();
					    showMoneyHistoryIncome.setToUserId(ownuserId);
					    showMoneyHistoryIncome.setIncomeAmount(getMoney);
					    showMoneyHistoryIncome.setType(38);
					    showMoneyHistoryIncome.setProductDesc("" + newsId);
					    showMoneyHistoryIncome.setDtime(new Date());
					    if (kkuserService.addAndGetUserAssets(ownuserId, incomeAmount, true, showMoneyHistoryIncome) == null) {
	                        logger.error("addAndGetUserAssets(" + new Gson().toJson(showMoneyHistoryIncome) + ") 执行失败");
	                        logger.info("Failed: 打赏动态添加秀币失败 " + new Gson().toJson(showMoneyHistoryIncome));
	                    }
					}
				} catch (Exception e) {
					logger.error("call ShowMoneyService.updateUserShowMoney catched exception : " + new Gson().toJson(showMoneyHistory), e);
				}
				
				// 获取当前用户秀币余额
				long showmoney = com.melot.kktv.service.UserService.getUserShowMoney(userId);
				result.addProperty("showMoney", showmoney);
				result.addProperty("rewardCount", (Integer)map.get("rewardCount"));
				result.addProperty("commentCount", (Integer)map.get("commentCount"));
				result.addProperty("TagCode", TagCode);
				result.addProperty("commentId", (Integer) map.get("rewardId"));
				return result;
			} else if(TagCode.equals("03")) {
				logger.error("要打赏的动态不存在 ,TagCode:" + TagCode + ",newsId:" + newsId);
				result.addProperty("TagCode", "061301"+TagCode);
				return result;
			} else if(TagCode.equals("04")) {
				logger.error("打赏数量错误,TagCode:" + TagCode + ",rewardCost:" + rewardCost);
				result.addProperty("TagCode", "061301"+TagCode);
				return result;
			} else {
				logger.error("打赏动态失败, userId: "+userId+", newsId:"+newsId);
				result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
				return result;
			} 
		} catch (SQLException e) {
			logger.error("未能正常调用存储过程", e);
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			return result;
		}
		
	}

	/**
	 * 根据不同的rewardCount值获取RewardDBObj(写死)
	 * @param rewardCount
	 * @return
	 */
	private Map<String,Object> getRewardDBObj(int rewardCount){
		Map<String,Object> map = Maps.newHashMap();
		map.put("rewardCount",rewardCount);
		map.put("rewardCost",100);
		if(rewardCount == 1){
			map.put("rewardDes","捧个人场");
		}else if(rewardCount == 10){
			map.put("rewardDes","捧个钱场");
		}else if(rewardCount == 888){
			map.put("rewardDes","包个大礼");
		}else if(rewardCount == 9999){
			map.put("rewardDes","包个全场");
		}
		return map;
	}

	/**
	 * 删除评论
	 * 
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return 结果字符串
	 */
	public JsonObject deleteNewsComment(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}

		// 获取参数
		JsonElement userIdje = jsonObject.get("userId");
		JsonElement commentIdje = jsonObject.get("commentId");

		// 验证参数
		int userId;
		int commentId;
		if (userIdje != null && !userIdje.isJsonNull() && !userIdje.getAsString().equals("")) {
			// 验证数字
			try {
				userId = userIdje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "06060002");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "06060001");
			return result;
		}
		if (commentIdje != null && !commentIdje.isJsonNull() && !commentIdje.getAsString().equals("")) {
			// 验证数字
			try {
				commentId = commentIdje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "06060004");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "06060003");
			return result;
		}

		// 调用存储过程得到结果
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("userId", userId);
		map.put("commentId", commentId);
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("News.deleteNewsComment", map);
		} catch (SQLException e) {
			logger.error("未能正常调用存储过程", e);
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			return result;
		}
		String TagCode = (String) map.get("TagCode");
		if (TagCode.equals(TagCodeEnum.SUCCESS)) {
			
			Integer newsId = (Integer) map.get("newsId");
			
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCode);
			return result;
		} else if (TagCode.equals("03") || TagCode.equals("04") || TagCode.equals("05")) {
			/* '03'; -- 评论不存在 */
			/* '04'; -- 评论的动态不存在 */
			/* '05'; -- 动态不属于该用户 */
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "060601" + TagCode + "");
			return result;
		} else {
			// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
			logger.error("调用存储过程未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
			return result;
		}
	}

	/**
	 * 获取动态的评论列表(10006007)
	 * 
	 * @param jsonObject 请求对象
	 * @return 结果字符串
	 */
	public JsonObject getUserNewsCommentList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		// 获取参数
		JsonElement newsIdje = jsonObject.get("newsId");
		JsonElement startje = jsonObject.get("start");
		JsonElement offsetje = jsonObject.get("offset");
		JsonElement platformje = jsonObject.get("platform");

		// 验证参数
		int newsId;
		int start = 0;
		int offset = Constant.return_comment_count;
		int platform = 0;
		if (newsIdje != null && !newsIdje.isJsonNull() && newsIdje.getAsInt()>0) {
			// 验证数字
			try {
				newsId = newsIdje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "06070002");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "06070001");
			return result;
		}
		if (platformje != null && !platformje.isJsonNull() && platformje.getAsInt()>0) {
			// 验证数字
			try {
				platform = platformje.getAsInt();
			} catch (Exception e) {
				platform = 0;
			}
		}
		if (startje != null && !startje.isJsonNull() && startje.getAsInt()>0) {
			// 验证数字
			try {
				start = startje.getAsInt();
			} catch (Exception e) {
				start = 0;
			}
		}
		if (offsetje != null && !offsetje.isJsonNull() && offsetje.getAsInt()>0) {
			// 验证数字
			try {
				offset = offsetje.getAsInt();
			} catch (Exception e) {
				offset = Constant.return_comment_count;
			}
		}
		if(platform == PlatformEnum.WEB) offset = -1;
		
		JsonObject result = new JsonObject();
		
		// 调用存储过程得到结果
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("newsId", newsId);
		map.put("start", start);
		map.put("offset", offset);
		
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("News.getUserNewsCommentList", map);
			String TagCode = (String) map.get("TagCode");
			if (TagCode.equals(TagCodeEnum.SUCCESS)) {
				// 返回结果
				result.addProperty("TagCode", TagCode);
				JsonArray jCommentList = new JsonArray();
				@SuppressWarnings("unchecked")
				List<NewsComment> commentList = (ArrayList<NewsComment>) map.get("commentList");
				commentList = UserService.addUserExtra(commentList);
				if (commentList != null) {
					for (Object object : commentList) {
						jCommentList.add(NewsCommentTF.toJsonObject((NewsComment) object, platform));
					}
				}
				result.add("commentList", jCommentList);
				result.addProperty("commentTotal", (Integer) map.get("commentTotal"));
				result.addProperty("pathPrefix", "");
			} else if (TagCode.equals("02")) {
				/* '02';分页超出范围 */
				result.addProperty("TagCode", "06070005");
				result.add("commentList", new JsonArray());
			} else if (TagCode.equals("03")) {
			    // 动态不存在
			    result.addProperty("TagCode", "06070006");
                result.add("commentList", new JsonArray());
            } else {
				// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
				logger.error("调用存储过程未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
				result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
			}
		} catch (SQLException e) {
			logger.error("未能正常调用存储过程", e);
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
		}
		return result;
	}

	/**
	 * 获取消息提醒简要信息
	 * 
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return 结果字符串
	 */
	public JsonObject getUserMessage(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}

		// 获取参数
		JsonElement userIdje = jsonObject.get("userId");

		// 验证参数
		int userId;
		if (userIdje != null && !userIdje.isJsonNull() && !userIdje.getAsString().equals("")) {
			// 验证数字
			try {
				userId = userIdje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "06080002");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "06080001");
			return result;
		}

		// 调用存储过程得到结果
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("userId", userId);
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("News.getUserMessage", map);
		} catch (SQLException e) {
			logger.error("未能正常调用存储过程", e);
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			return result;
		}
		String TagCode = (String) map.get("TagCode");
		if (TagCode.equals(TagCodeEnum.SUCCESS)) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "" + TagCode + "");
			result.addProperty("commentCount", (Integer) map.get("commentCount"));
			result.addProperty("newFansCount", (Integer) map.get("newFansCount"));
			result.addProperty("sysMsgCount", (Integer) map.get("sysMsgCount"));
			return result;
		} else {
			// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
			logger.error("调用存储过程未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
			return result;
		}
	}

	/**
	 * 新评论提醒列表
	 * 
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return 结果字符串
	 */
	public JsonObject getNewCommentedNewsList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}

		// 获取参数
		JsonElement userIdje = jsonObject.get("userId");
		JsonElement pageIndexje = jsonObject.get("pageIndex");
		JsonElement platformje = jsonObject.get("platform");
		// 验证参数
		int userId;
		int pageIndex;
		int platform = 0;
		if (userIdje != null && !userIdje.isJsonNull() && !userIdje.getAsString().equals("")) {
			// 验证数字
			try {
				userId = userIdje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "06090002");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "06090001");
			return result;
		}
		if (pageIndexje != null && !pageIndexje.isJsonNull() && !pageIndexje.getAsString().equals("")) {
			// 验证数字
			try {
				pageIndex = pageIndexje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "06090004");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "06090003");
			return result;
		}
		if (platformje != null && !platformje.isJsonNull() && platformje.getAsInt()>0) {
			// 验证数字
			try {
				platform = platformje.getAsInt();
			} catch (Exception e) {
				platform = 0;
			}
		}
		// 调用存储过程得到结果
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("userId", userId);
		map.put("pageIndex", pageIndex);
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("News.getUserBeenCommentedList", map);
		} catch (SQLException e) {
			logger.error("未能正常调用存储过程", e);
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			return result;
		}
		String TagCode = (String) map.get("TagCode");
		if (TagCode.equals(TagCodeEnum.SUCCESS)) {
			// 取出列表
			@SuppressWarnings("unchecked")
			List<NewsComment> commentList = (ArrayList<NewsComment>) map.get("commentList");
			commentList = UserService.addUserExtra(commentList);
			
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCode);
			result.addProperty("pageTotal", (Integer) map.get("pageTotal"));
			JsonArray jNewsList = new JsonArray();
			if (commentList != null) {
				for (Object object : commentList) {
					jNewsList.add(NewsCommentTF.toJsonObject((NewsComment) object, platform));
				}
			}
			result.add("commentList", jNewsList);
			result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
			// 返回结果
			return result;
		} else if (TagCode.equals("02")) {
			/* '02';分页超出范围 */
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			result.add("newsList", new JsonArray());
			return result;
		} else {
			// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
			logger.error("调用存储过程未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
			return result;
		}
	}

	/**
	 * 新系统列表
	 * 
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return 结果字符串
	 */
	public JsonObject getUserSysMsgList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}

		// 获取参数
		JsonElement userIdje = jsonObject.get("userId");

		// 验证参数
		int userId;
		if (userIdje != null && !userIdje.isJsonNull() && !userIdje.getAsString().equals("")) {
			// 验证数字
			try {
				userId = userIdje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "06100002");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "06100001");
			return result;
		}

		// 调用存储过程得到结果
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("userId", userId);
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("News.getUserSysMsgList", map);
		} catch (SQLException e) {
			logger.error("未能正常调用存储过程", e);
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			return result;
		}
		String TagCode = (String) map.get("TagCode");
		if (TagCode.equals(TagCodeEnum.SUCCESS)) {
			// 取出列表
			@SuppressWarnings("unchecked")
			List<Object> messageList = (ArrayList<Object>) map.get("messageList");

			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCode);
			JsonArray jSysMsgList = new JsonArray();
			for (Object object : messageList) {
				jSysMsgList.add(((SysMsg) object).toJsonObject());
			}
			result.add("messageList", jSysMsgList);

			// 返回结果
			return result;
		} else {
			// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
			logger.error("调用存储过程未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
			return result;
		}
	}

	/**
	 * 清除系统消息
	 * 
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return 结果字符串
	 */
	public JsonObject clearSysMsg(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		// 该接口需要验证token,未验证的返回错误码
		if (!checkTag) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
			return result;
		}

		// 获取参数
		JsonElement userIdje = jsonObject.get("userId");
		JsonElement msgIdje = jsonObject.get("msgId");

		// 验证参数
		int userId;
		int msgId;
		if (userIdje != null && !userIdje.isJsonNull() && !userIdje.getAsString().equals("")) {
			// 验证数字
			try {
				userId = userIdje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "06110002");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "06110001");
			return result;
		}
		if (msgIdje != null && !msgIdje.isJsonNull() && !msgIdje.getAsString().equals("")) {
			// 验证数字
			try {
				msgId = msgIdje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "06110004");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "06110003");
			return result;
		}

		// 调用存储过程得到结果
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("userId", userId);
		map.put("msgId", msgId);

		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("News.clearSysMsg", map);
		} catch (SQLException e) {
			logger.error("未能正常调用存储过程", e);
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			return result;
		}
		String TagCode = (String) map.get("TagCode");
		if (TagCode.equals(TagCodeEnum.SUCCESS)) {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
			return result;
		} else if (TagCode.equals("03") || TagCode.equals("04")) {
			/* '03'; -- 系统不属于该用户 */
			/* '04'; -- 系统不存在 */
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "061101" + TagCode + "");
			return result;
		} else {
			// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
			logger.error("调用存储过程未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
			return result;
		}
	}

	/**
	 * 根据动态Id获取动态详细信息(10006012)
	 * 
	 * @param jsonObject 请求对象
	 * @param checkTag 是否验证token标记
	 * @return 结果字符串
	 */
	@SuppressWarnings("unchecked")
    public JsonObject getNewsInfoByNewsId(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		
		// 获取参数
		JsonElement newsIdje = jsonObject.get("newsId");
		JsonElement platformje = jsonObject.get("platform");
		JsonElement versionje = jsonObject.get("version");
		// 验证参数
		Integer newsId;
		int platform = 0;
		int version = 1;
		
		if (newsIdje != null && !newsIdje.isJsonNull() && !newsIdje.getAsString().equals("")) {
			// 验证数字
			try {
				newsId = newsIdje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "06120001");
				return result;
			}
		} else {
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", "06120002");
			return result;
		}
		
		if (platformje != null && !platformje.isJsonNull() && platformje.getAsInt()>0) {
			// 验证数字
			try {
				platform = platformje.getAsInt();
			} catch (Exception e) {
				platform = 0;
			}
		}

		if (versionje != null && !versionje.isJsonNull() && versionje.getAsInt()>0) {
			// 验证数字
			try {
				version = versionje.getAsInt();
			} catch (Exception e) {
				version = 1;
			}
		}
		
		Map<String, Object> map=new HashMap<String, Object>();
		map.put("newsId", Integer.valueOf(newsId));
		map.put("version", version);
		
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("News.getNewsInfoByNewsId", map);
			String TagCode = (String) map.get("TagCode");
			if (TagCode.equals(TagCodeEnum.SUCCESS)) {
				// 取出列表
				try {
					List<News> newsInfoList = (ArrayList<News>)map.get("newsInfo");
					List<News> rewardsInfoList = (ArrayList<News>) map.get("rewardInfo");
					News newsInfo = (News) newsInfoList.get(0);
					if (newsInfo != null && newsInfo.getUserId() != null) {
						UserProfile userProfile = UserService.getUserInfoNew(newsInfo.getUserId());
						if (userProfile != null) {
							if (userProfile.getNickName() != null) {
								newsInfo.setNickname(userProfile.getNickName());
							}
							if (userProfile.getPortrait() != null) {
								newsInfo.setPortrait_path_original(userProfile.getPortrait());
							}
						}
						newsInfo.setGender(userProfile.getGender());
					}
					
					rewardsInfoList = UserService.addUserExtra(rewardsInfoList);
					
					JsonObject result = NewsTF.toJsonObject(newsInfo, platform, 0);
					int totalRewarders = (Integer)map.get("totalRewarders");
					result.addProperty("totalRewarders", totalRewarders);
					//打赏top 4
					if(rewardsInfoList != null && rewardsInfoList.size() > 0) {
						JsonArray ns = NewsTF.toRewardJsonObject(newsInfo, platform, rewardsInfoList);
						result.add("newsRewardList", ns);   
					}
					if (newsInfo != null && (newsInfo.getMediaFrom() == null || newsInfo.getMediaFrom().intValue() == 1)) {
						result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
						result.addProperty("mediaPathPrefix", ConfigHelper.getMediahttpdir()); // 多媒体前缀
					} else {
						result.addProperty("pathPrefix", ConfigHelper.getVideoURL());
						result.addProperty("mediaPathPrefix", ConfigHelper.getVideoURL());
					}
					result.addProperty("TagCode", TagCode);
					return result;
				} catch(Exception e) {
					logger.error("newsInfo数据转化失败", e);
					JsonObject result = new JsonObject();
					result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
					return result;
				}
			} else if (TagCode.equals("02")) {
				/* 数据库中不存在与该newsId关联的记录*/
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "06120003");
				return result;
			} else {
				// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
				logger.error("调用存储过程未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
				return result;
			}
		} catch (SQLException e) {
			logger.error("未能正常调用存储过程", e);
			JsonObject result = new JsonObject();
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			return result;
		}
	}
	
	/**
	 * 获取动态列表 10006014(动态关注 + kk推荐)
	 * @param paramJsonObject
	 * @return
	 */
	public JsonObject getRecommendNewsList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		// 定义使用的参数
		int countPerPage = 0;
		int pageIndex = 0;
		int platform = 0;
		int pageTotal = 0;
		int userId = 0;
		// 定义返回结果
		JsonObject result = new JsonObject();
		try {
			// 解析参数
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 0, Integer.MAX_VALUE);
			countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", Constant.return_news_count, null, 1, 50);
			pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
		} catch(CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch(Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}

		List<News> newsList = new ArrayList<News>();
		if (userId > 0) {
			// 获取关注用户动态总数
			int recCount = NewsService.getRecAddNewsCount(ConfigHelper.getRecNewsAddedCount());
			int totalCount = NewsService.getTotalFocusUserNews(userId, 0) + recCount;
			pageTotal = (int) Math.ceil((double) totalCount / countPerPage);
		} else {
			// 获取推荐动态总数
			int totalCount = NewsService.getHotNewsTotalCount();
			pageTotal = (int) Math.ceil((double) totalCount / countPerPage);
		}
		
		newsList = getUserNewsList(userId, 0, pageIndex, countPerPage);
		if (newsList != null && newsList.size() > 0) {
			JsonArray jNewsList = new JsonArray();
			for (News news : newsList) {
				jNewsList.add(NewsTF.toJsonObject((News) news, platform, 0));
			}
			result.add("newsList", jNewsList);
			result.addProperty("pageTotal", pageTotal);
			// 图片前缀
			result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
			// 多媒体前缀
			result.addProperty("mediaPathPrefix", ConfigHelper.getMediahttpdir());
		}
		if (pageIndex <= 1 && userId > 0) {
			NewsSource.setNewsRemindTime(String.valueOf(userId), String.valueOf(System.currentTimeMillis()));
		}
		if (pageIndex > pageTotal && pageTotal > 0) {
			result.addProperty("TagCode", TagCodeEnum.OVER_PAGGING_RANGE);
		} else {
			// 返回结果
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		}
		
		return result;
	}
	
	/**
	 * 获取打赏Top 20 列表(10006015)
	 * 
	 * @param paramJsonObject
	 * @return
	 */
	public JsonObject getRewardNewsTopList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		
		JsonElement newsIdje = jsonObject.get("newsId");
		JsonElement platformje = jsonObject.get("platform");

		int platform = 0;
		//Integer userId = null;
		Integer newsId = null;
		if (platformje != null && !platformje.isJsonNull() && platformje.getAsInt()>0) {
			// 验证数字
			try {
				platform = platformje.getAsInt();
			} catch (Exception e) {
				platform = 0;
			}
		}
		
		if (newsIdje != null && !newsIdje.isJsonNull() && newsIdje.getAsInt()>0) {
			// 验证数字
			try {
				newsId = newsIdje.getAsInt();
			} catch (Exception e) {
				JsonObject result = new JsonObject();
				result.addProperty("TagCode", "06150002"); //解析newsId失败 
				return result;
			}
		} else {
			   JsonObject result = new JsonObject();
			   result.addProperty("TagCode", "06150001"); //解析newsId缺失
			   return result;
		}
		
		JsonObject result = new JsonObject();
		//调用存储过程获取打赏top20  返回用户昵称、打赏数、用户Id、用户头像
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("newsId", newsId);
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("News.getTopUsersOnRewardNews", map);
			String TagCode = (String) map.get("TagCode");
			if (TagCode.equals(TagCodeEnum.SUCCESS)) {
				result.addProperty("TagCode", TagCodeEnum.SUCCESS);
				// 取出列表
				@SuppressWarnings("unchecked")
				List<News> rewardUserList = (ArrayList<News>) map.get("topRewardUsers");
				rewardUserList = UserService.addUserExtra(rewardUserList);
				
				result.addProperty("TagCode", TagCode);
				JsonArray jRecordList = new JsonArray();
				if (rewardUserList != null) {
					for (Object object : rewardUserList) {
						jRecordList.add(NewsTF.toJsonObjectForTopUsers((News) object, platform));
					}
				}
				result.addProperty("pathPrefix", ConfigHelper.getHttpdir()); // 图片前缀
				result.add("topUserList", jRecordList);
			} else if (TagCode.equals("02")) {
				/* '02';用户动态编号有误  */
				result.addProperty("TagCode", "061501"+TagCode);
			} else {
				// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
				logger.error("调用存储过程未的到正常结果,TagCode:" + TagCode + ",jsonObject:" + jsonObject.toString());
				result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
			}
		} catch (SQLException e) {
			logger.error("未能正常调用存储过程", e);
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
		}
		// 返回结果
		return result;
	}
	
	/**
	 * 获取动态打赏列表(10006016)
	 * @return
	 */
	public JsonObject getNewsRewardList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		
		JsonObject result = new JsonObject();
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		// 返回打赏列表  只在第一页返回打赏列表信息
		result.addProperty("rewardCost", 10);
		result.add("rewardList", getRewards());
		return result;
	}

	/**
	 * 获得打赏列表（写死）
	 * @return
	 */
	private JsonArray getRewards(){
		JsonArray rewardArray = new JsonArray();
		rewardArray.add(reward(1,"捧个人场"));
		rewardArray.add(reward(10,"捧个钱场"));
		rewardArray.add(reward(888,"包个大礼"));
		rewardArray.add(reward(9999,"包个全场"));
		return rewardArray;
	}

	private JsonObject reward(int rewardCount,String rewardDes){
		JsonObject rewardObj = new JsonObject();
		rewardObj.addProperty("rewardCount",rewardCount);
		rewardObj.addProperty("rewardDes",rewardDes);
		return rewardObj;
	}
	
	/**
	 * 获取热门视频动态列表(10006017)
	 * @return
	 */
	public JsonObject getHotMediaNewsList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		
		// 定义使用的参数
		int tagId = 0, pageIndex = 0, totalCount = 0,
				platform = PlatformEnum.WEB, countPerPage = Constant.return_news_count;
		// 定义返回结果
		JsonObject result = new JsonObject();
		
		// 解析参数
		try {
			tagId = CommonUtil.getJsonParamInt(jsonObject, "tagId", 0, null, 0, Integer.MAX_VALUE);
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
			pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
			countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", Constant.return_news_count, null, 0, Integer.MAX_VALUE);
		} catch (ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		}

		JsonArray jNewsList = new JsonArray();
		List<News> newsList = new ArrayList<News>();
		// 获取热拍总数
		totalCount = NewsService.getRePaiCount(tagId == 0 ? null : tagId, NewsMediaTypeEnum.VIDEO, null, null, null);
		if (totalCount > 0) {
			result.addProperty("totalPage", (int) Math.ceil((double) totalCount / countPerPage));
			newsList = NewsService.getRePaiHotNewsList(tagId, pageIndex, countPerPage, NewsMediaTypeEnum.VIDEO,
					null, null, null, 0);
		}
		if (newsList != null && newsList.size() > 0) {
			if (newsList != null && newsList.size() > 0) {
				for (News news : newsList) {
					jNewsList.add(NewsTF.toJsonObject(news, platform, 0));
				}
			}
			result.add("newsList", jNewsList);
			// 图片前缀
			result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
			// 多媒体前缀
			result.addProperty("mediaPathPrefix", ConfigHelper.getMediahttpdir());
		}
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
	}

	/**
	 * 获取热门视频动态列表(10006018)
	 * @return
	 */
	public JsonObject getHotNewsTagList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		
		// 定义使用的参数
		// int platform = PlatformEnum.WEB; // 预留字段
		int more = 0;
		// 定义返回结果
		JsonObject result = new JsonObject();
		// 解析参数
		try {
			more = CommonUtil.getJsonParamInt(jsonObject, "more", 0, null, 0, 1);
		} catch (ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		}
		
		List<NewsTagConf> newsList = NewsService.getConfNewsTagList(more);
		if (newsList != null && newsList.size() > 0) {
			result.add("tagList", new Gson().toJsonTree(newsList).getAsJsonArray());
		}
	
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
	}
	
	/**
	 * 4.0 获取用户动态列表
	 * @param userId 用户编号
	 * @param includeQiniu 是否包含七牛视频  0 不包含 , 1： 包含
	 * @param pageIndex 起始页
	 * @param countPerPage 每页显示数量
	 * @return
	 */
	private static List<News> getUserNewsList(int userId, int includeQiniu, int pageIndex, int countPerPage) {
		List<News> newsList = null;
		// 推荐动态总数
		int totalCount = 0;
		int start = (pageIndex - 1) * countPerPage;
		int end = pageIndex * countPerPage;
		try {
			// 游客
			if (userId == 0) {
				totalCount = NewsService.getHotNewsTotalCount();
				if (totalCount > 0) {
					int pageTotal = (int) Math.ceil((double) totalCount / countPerPage);
					if (pageIndex <= pageTotal) {
						newsList = NewsService.getRecNewsList(start + 1, end, includeQiniu);
					}
				}
			} else {
				// 非游客,获取推荐动态
				Set<String> newsIdSet= NewsSource.getRecNewsIds(ConfigHelper.getRecNewsAddedCount());
				List<News> listRecNews = NewsService.getNewsList(newsIdSet, includeQiniu);
				if (listRecNews != null && listRecNews.size() > 1) {
					List<News> manualNewsList = new ArrayList<News>();
					// 推荐动态排序
					Iterator<String> it = newsIdSet.iterator();
					while (it.hasNext()){
						String newsId = it.next();
						for (News lrcNew : listRecNews) {
							if (newsId != null && newsId.equals(lrcNew.getNewsId().toString())) {
								manualNewsList.add(lrcNew);
								break;
							}
						}
					}
					listRecNews = manualNewsList;
				}
				// 用户 返回关注加推荐动态 
				if (pageIndex == 1) {
					// 如果查询为第一页如果为清空redis记录
					NewsSource.delTempUserNewsKey(userId);
				} else {
					// 删除前几页已经插入的推荐动态
					if (listRecNews != null && listRecNews.size() > 0) {
					Set<String> newsScore = NewsSource.getTempUserNewsIds(userId, 0, start);
					if (newsScore != null && newsScore.size() > 0) {
						List<String> newsIdList = new ArrayList<String>();
						for (Iterator<String> iterator = newsScore.iterator(); iterator.hasNext();) {
							newsIdList.add(iterator.next());
						}
						int i = 0;
						for (;i < listRecNews.size();) {
								if (newsIdList.contains(String.valueOf(listRecNews.get(i).getNewsId()))) {
									listRecNews.remove(listRecNews.get(i));
									i = 0;
									continue;
								}
								i++;
						    }
						}
					}
					// 删除reids已经插入推荐的动态
					NewsSource.delTempUserNewsKey(userId, start);
				}
			
				// 获取用户关注动态先获取(前几页)已经加入推荐动态个数
				int addCount = NewsSource.getUsedTempUserNewsCount(userId);
				if (addCount > 0) {
					start -= addCount;
					int leftCount = end - addCount;
					end = leftCount > 0 ? leftCount : end;
				}
				// 获取用户关注动态
				List<News> listNews = NewsService.getUserFocusNews(userId, includeQiniu, start, end);
				int size = listNews == null ? 0 : listNews.size();
				newsList = new ArrayList<News>();
				if (listNews != null && listNews.size() > 0) {
					// 推荐动态中删除推荐动态和用户关注动态的交集
					listRecNews.removeAll(listNews);
					int recSize = listRecNews.size();
					if (recSize > 0) {
						// 存在推荐动态
						if (size > 2) {
							// 第一条推荐动态从索引为3-6的位置开始
							int index = start + (int)(Math.random() * 4) + 3;
							// 推荐动态插入索引位置
							int j = start + 1;
							for (int i = 0; i < countPerPage; i++) {
								if (newsList.size() < countPerPage) {
									// 获取 2~5之间随机数
									if (index == j) {
										if (listRecNews.size() > 0) {
											News news = listRecNews.get(0);
											// 更新至redis
											NewsSource.setTempUserNewsKey(userId, news.getNewsId(), index);
											// 删除 news
											listRecNews.remove(news);
											news.setIsRecNews(1);
											newsList.add(news);
											j++;
											index = (int)(Math.random() * 3) + 3 + j;
										}
									}
									if (newsList.size() < countPerPage && listNews.size() > i) {
										newsList.add(listNews.get(i));
										j++;
									}
								}
							}
						} else {
							// listNews + 相应的推荐动态
							for (News nl : listNews) {
								newsList.add(nl);
							}
							if (size < countPerPage) {
								int diff = countPerPage - size;
								diff = diff > recSize ? recSize : diff;
								for (int i = 0; i < diff; i++) {
									News news = listRecNews.get(i);
									news.setIsRecNews(1);
									newsList.add(news);
									// 更新至redis
									NewsSource.setTempUserNewsKey(userId, news.getNewsId(), size + 1 + i);
								}
							}
						}					
					} else {
						// 不存在推荐动态
						if (listNews != null && listNews.size() > 0) {
						  for (News nl : listNews) {
							  newsList.add(nl);
						  }
						}
					}
				} else {
					totalCount = NewsService.getRecAddNewsCount(ConfigHelper.getRecNewsAddedCount());
					if (totalCount > 0 && pageIndex == 1) {
						return NewsService.getHotNewsList(1, end, ConfigHelper.getRecNewsAddedCount(), includeQiniu);	
					}
				}
			}
		} catch (Exception e) {
			logger.error("NewsFunctions.getUserNewsList exception, userId : " + userId + " ,pageIndex : " 
					+ pageIndex + " ,countPerPage : " + countPerPage, e);

		}
		
		return newsList;
	}
	
	/**
	 * 获取用户动态列表(20010401)
	 * @param jsonObject 请求对象
	 * @return 结果字符串
	 */
	public JsonObject getUserNewsListNew(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
		
		JsonObject result = new JsonObject();
		// 定义所需参数
		int userId =1, pageIndex = 1, platform = PlatformEnum.ANDROID, inRoom = 0, 
				countPerPage = Constant.return_news_count;
		// 解析参数
		try {
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 1, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
			pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
			countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", Constant.return_news_count, null, 1, Integer.MAX_VALUE);
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.ANDROID, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
			inRoom = CommonUtil.getJsonParamInt(jsonObject, "inRoom", 0, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
		} catch (ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		}

		List<News> checkList = new ArrayList<News>();
		HashSet<Integer> checkIds = new HashSet<Integer>();
		if (checkTag) {
			List<NewsInfo> newsList = NewsService.getSelfNewsList(userId, (pageIndex - 1) * countPerPage, countPerPage, 1, 0);
			if (newsList != null && newsList.size() > 0) {
				for (NewsInfo newsInfo : newsList) {
					if (newsInfo.getState() == 3) {
						News news = new News();
						news.setNewsId(newsInfo.getNewsId());
						news.setNewsType(newsInfo.getNewsType());
						news.setMediaFrom(1);
						news.setMediaSource("{\"mediaType\":1,\"imageUrl\":\"/picture/offical/checkingpicture.jpg\"}");
						news.setPublishedTime(newsInfo.getPublishedTime());
						news.setContent(newsInfo.getContent());
						news.setUserId(newsInfo.getUserId());
						checkIds.add(newsInfo.getNewsId());
						checkList.add(news);
					}
				}
			}
		}
		
		// 调用存储过程得到结果
		Map<Object, Object> map = new HashMap<Object, Object>();
		map.put("userId", userId);
		map.put("inRoom", inRoom); 
		map.put("pageIndex", pageIndex);
		map.put("countPerPage", countPerPage);
		map.put("platform", platform);
		if (checkTag) {
			map.put("isSelf", 1);
		} else {
			map.put("isSelf", 0);
		}
		try {
			SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("News.getUserNewsListNew", map);
		} catch (SQLException e) {
			logger.error("NewsFunctions.getUserNewsListNew(p_getUserNewsListNew) exception", e);
			result.addProperty("TagCode", TagCodeEnum.PROCEDURE_EXCEPTION);
			return result;
		}
		String TagCode = (String) map.get("TagCode");
		
		if (TagCode.equals(TagCodeEnum.SUCCESS)) {
			// 取出列表
			@SuppressWarnings("unchecked")
			List<News> newsList = (ArrayList<News>) map.get("newsList");
			newsList = UserService.addUserExtra(newsList);
			result.addProperty("TagCode", TagCode);
			result.addProperty("pageTotal", (Integer) map.get("pageTotal"));
			JsonArray jNewsList = new JsonArray();
			
			if (newsList != null) {
				for (Object object : newsList) {
					if (checkIds != null && checkIds.size() > 0) {
						if (checkIds.contains(((News) object).getNewsId())) {
							checkIds.remove(((News) object).getNewsId());
						}
					}
					break;
				}
			}
			int flag = checkIds.size();
			for (Object object : newsList) {
				if (flag ++ < newsList.size()) {
					jNewsList.add(NewsTF.toJsonObject((News) object, platform, inRoom));
				}
			}
			
			if (checkIds.size() > 0) {
				for (News news : checkList) {
					if (checkIds.contains(news.getNewsId())) {
						jNewsList.add(NewsTF.toJsonObject(news, platform, inRoom));
					}
				}
			}

			// 返回结果
			result.add("newsList", jNewsList);
			result.addProperty("pathPrefix", ConfigHelper.getHttpdir()); // 图片前缀
			result.addProperty("mediaPathPrefix", ConfigHelper.getMediahttpdir()); // 多媒体前缀
			result.addProperty("qiniuPathPrefix", ConfigHelper.getVideoURL()); // 七牛前缀
			return result;
		} else if (TagCode.equals("02")) {
			/* '02';分页超出范围 */
			result.addProperty("TagCode", "200104" + TagCode);
			return result;
		} else {
			// 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
			logger.error("NewsFunctions.getUserNewsListNew(p_getUserNewsListNew) exception, TagCode:" + TagCode 
					+ " ,jsonObject:" + jsonObject.toString());
			result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
			return result;
		}
	}
	
	/**
	 * 获取推荐动态（用户关注）列表 （20010402）
	 * @param paramJsonObject
	 * @return
	 */
	public JsonObject getRecNewsList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		// 定义使用的参数
		int countPerPage = 0, pageIndex = 0, platform = 0, pageTotal = 0, userId = 0;
		// 定义返回结果
		JsonObject result = new JsonObject();
		try {
			// 解析参数
			userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 1, TagCodeEnum.USERID_MISSING, 0, Integer.MAX_VALUE);
			countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", Constant.return_news_count, null, 1, 50);
			pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
		} catch(CommonUtil.ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		} catch(Exception e) {
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			return result;
		}

		List<News> newsList = new ArrayList<News>();
		// 获取关注用户动态总数
		int recCount = NewsService.getRecAddNewsCount(ConfigHelper.getRecNewsAddedCount());
		int totalCount = NewsService.getTotalFocusUserNews(userId, 1) + recCount;
		pageTotal = (int) Math.ceil((double) totalCount / countPerPage);
		newsList = getUserNewsList(userId, 1, pageIndex, countPerPage);
		if (newsList != null && newsList.size() > 0) {
			JsonArray jNewsList = new JsonArray();
			for (News news : newsList) {
				jNewsList.add(NewsTF.toJsonObject((News) news, platform, 0));
			}
			result.add("newsList", jNewsList);
			result.addProperty("pageTotal", pageTotal);
			// 图片前缀
			result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
			// 多媒体前缀
			result.addProperty("mediaPathPrefix", ConfigHelper.getMediahttpdir());
			// 七牛地址前缀
			result.addProperty("qiniuPathPrefix", ConfigHelper.getVideoURL());
		}
		if (pageIndex <= 1 && userId > 0) {
			NewsSource.setNewsRemindTime(String.valueOf(userId), String.valueOf(System.currentTimeMillis()));
		}
		if (pageIndex > pageTotal && pageTotal > 0) {
			result.addProperty("TagCode", TagCodeEnum.OVER_PAGGING_RANGE);
		} else {
			// 返回结果
			result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		}
		
		return result;
	}
	
	/**
	 * 获取热门视频动态列表(20010403)
	 * @return
	 */
	public JsonObject getHotMediaNewsListNew(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		
		// 定义使用的参数
		int tagId = 0, pageIndex = 0, totalCount = 0,
				platform = PlatformEnum.WEB, countPerPage = Constant.return_news_count;
		// 定义返回结果
		JsonObject result = new JsonObject();
		
		// 解析参数
		try {
			tagId = CommonUtil.getJsonParamInt(jsonObject, "tagId", 0, null, 0, Integer.MAX_VALUE);
			platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
			pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
			countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", Constant.return_news_count, null, 0, Integer.MAX_VALUE);
		} catch (ErrorGetParameterException e) {
			result.addProperty("TagCode", e.getErrCode());
			return result;
		}

		JsonArray jNewsList = new JsonArray();
		List<News> newsList = new ArrayList<News>();
		// 获取热拍总数
		totalCount = NewsService.getRePaiCount(tagId == 0 ? null : tagId, NewsMediaTypeEnum.VIDEO, null, null, null);
		if (totalCount > 0) {
			result.addProperty("totalPage", (int) Math.ceil((double) totalCount / countPerPage));
			newsList = NewsService.getRePaiHotNewsList(tagId, pageIndex, countPerPage, NewsMediaTypeEnum.VIDEO,
					null, null, null, 1);
		}
		if (newsList != null && newsList.size() > 0) {
			if (newsList != null && newsList.size() > 0) {
				for (News news : newsList) {
					jNewsList.add(NewsTF.toJsonObject(news, platform, 0));
				}
			}
			result.add("newsList", jNewsList);
			// 图片前缀
			result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
			// 多媒体前缀
			result.addProperty("mediaPathPrefix", ConfigHelper.getMediahttpdir());
			// 七牛前缀
			result.addProperty("qiniuPathPrefix", ConfigHelper.getVideoURL());
		}
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
	}
	
	
}
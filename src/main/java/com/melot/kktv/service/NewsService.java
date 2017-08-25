package com.melot.kktv.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.ibatis.sqlmap.client.SqlMapSession;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcore.user.api.UserStaticInfo;
import com.melot.kkcx.service.RoomService;
import com.melot.kkcx.service.UserService;
import com.melot.kktv.model.News;
import com.melot.kktv.model.NewsTagConf;
import com.melot.kktv.model.NewsTagRes;
import com.melot.kktv.redis.NewsSource;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.Constant;
import com.melot.kktv.util.DateUtil;
import com.melot.kktv.util.NewsMediaTypeEnum;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;
import com.melot.news.domain.NewsCommentHist;
import com.melot.news.model.NewsInfo;
import com.melot.news.model.WhiteUser;
import com.melot.qiniu.common.QiniuService;
import com.melot.resource.domain.Resource;
import com.melot.sdk.core.util.MelotBeanFactory;
import com.upyun.api.UpYun;

import redis.clients.jedis.Tuple;

public class NewsService {
	
	private static Logger logger = Logger.getLogger(NewsService.class);

	/**
	 * 获取发现动态
	 * @param start 起始位置
	 * @param end 结束位置
	 * @return
	 */
	public static List<News> getHotNewsList(int start, int end, int addedCount, int includeQiniu) {
		// 更新redis数据
		String value = NewsSource.getFilterNewsList(start + "_" + end, includeQiniu);
		if (value == null || value.trim().isEmpty()) {
			return updateToRedis(start, end, addedCount, includeQiniu);
		} else {
			try {
				return new Gson().fromJson(value, new TypeToken<List<News>>(){}.getType());
			} catch (Exception e) {
				logger.error("NewsService.getRePaiHotNewsList(parse value to List<News>) exception, value : " + value, e);
				return null;
			}
		}
	}
	
	/**
	 * 获取插入推荐动态数量
	 * @param count
	 * @return
	 */
	public static int getRecAddNewsCount(int count) {
		Set<String> recNewsSet = NewsSource.getRecNewsIds(ConfigHelper.getRecNewsAddedCount());
		if (recNewsSet != null) {
			return recNewsSet.size();
		} else {
			return 0;
		}
	}
	
	/**
	 *  获取用户关注动态
	 * @param userId 用户Id
	 * @param platform 平台Id
	 * @param min 起始位置
	 * @param max 结束位置
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<News> getUserFocusNews(int userId, int includeQiniu, int min, int max) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("userId", userId);
		map.put("min", min);
		map.put("max", max);
		map.put("includeQiniu", includeQiniu);
		try {
			List<News> newsList = SqlMapClientHelper.getInstance(DB.MASTER).queryForList("News.getFocusUserNewsList", map);
			return UserService.addUserExtra(newsList);
		} catch (Exception e) {
			logger.error("NewsService.getUserFocusNews exception, userId : " + userId 
					+ " ,min : " + min + " ,max : " + max, e);
			
			return null;
		}
	}
	
	/**
	 * 获取筛选动态总个数(首页)
	 * @return
	 */
	public static int getHotNewsTotalCount() {
		return NewsSource.getFilterNewsCount();
	}
	
	/**
	 * 获取关注用户（自己）动态总数
	 * @param userId 用户Id
	 * @param includeQiniu 是否包含七牛视频（0:不包含，1：包含）
	 * @return
	 */
	public static int getTotalFocusUserNews(int userId, int includeQiniu) {
		try {
			if (userId > 0) {
				Map<String, Integer> map = new HashMap<String, Integer>();
				map.put("userId", userId);
				map.put("includeQiniu", includeQiniu);
				return (Integer)SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("News.getTotalFocusUserNews", map);
			}
		} catch (Exception e) {
			logger.error("NewsService.getTotalFocusUserNews exception, userId : " + userId, e);
		}
		
		return 0;
	}
	
	/**
	 * 获取关注用户（自己）动态总数 (新)
	 * @param userId 用户Id
	 * @return
	 */
	public static int getTotalFocusUserNewsNew(int userId) {
		try {
			if (userId > 0) {
				return (Integer)SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("News.getTotalFocusUserNewsNew", userId);
			}
		} catch (Exception e) {
			logger.error("NewsService.getTotalFocusUserNewsNew exception, userId : " + userId, e);
		}
		
		return 0;
	}
	
	/**
	 * 更新动态至redis
	 * @param start
	 * @param end
	 */
	@SuppressWarnings("unchecked")
	private static List<News> updateToRedis(int start, int end, int addedCount, int includeQiniu) {
		String key = start + "_" + end;
			Set<String> newsScore = NewsSource.getRecNewsIds(addedCount);
			if (newsScore != null && newsScore.size() > 0) {
				Map<String, String> newsmap = new HashMap<String, String>();
				List<String> newsIdList = new ArrayList<String>();
				for (Iterator<String> iterator = newsScore.iterator(); iterator.hasNext();) {
					newsIdList.add(iterator.next());
				}
				try {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("newsIdList", newsIdList);
					map.put("includeQiniu", includeQiniu);
					List<News> newsList = SqlMapClientHelper.getInstance(DB.MASTER)
							.queryForList("News.getHotFactorNewsList", map);
					newsList = UserService.addUserExtra(newsList);
					if (newsList != null && newsList.size() > 0) {
						newsmap.put(key, new Gson().toJson(newsList));
						NewsSource.setFilterNewsList(newsmap, includeQiniu);
						return newsList;
					}
				} catch (Exception e) {
					logger.error("NewsService.updateToRedis(getHotFactorNewsList) exception, newsIdList: " + new Gson().toJson(newsIdList), e);
				}
			}
		 
		return null;
	}
	
	/**
	 * 获取推荐动态（旧版）
	 * @param start 起始位置
	 * @param end 结束位置
	 * @return
	 */
	public static List<News> getRecNewsList(int start, int end, int includeQiniu) {
		// 更新redis数据
		String value = NewsSource.getFilterNewsList(start + "_" + end, includeQiniu);
		if (value == null || value.trim().isEmpty()) {
			return syncFilterNewsList(start, end, includeQiniu);
		} else {
			try {
				return new Gson().fromJson(value, new TypeToken<List<News>>(){}.getType());
			} catch (Exception e) {
				logger.error("NewsService.getRecNewsList(parse value to List<News>) exception, value : " + value, e);
				return null;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private static List<News> syncFilterNewsList(int start, int end, int includeQiniu) {
		String key = start + "_" + end;
		Set<String> newsScore = NewsSource.getFilterNewsIds(start, end);
		if (newsScore != null && newsScore.size() > 0) {
			Map<String, String> newsmap = new HashMap<String, String>();
			List<String> newsIdList = new ArrayList<String>();
			for (Iterator<String> iterator = newsScore.iterator(); iterator.hasNext();) {
				newsIdList.add(iterator.next());
			}
			List<News> newslist = new ArrayList<News>();
			try {
				Map<String, Object> paramsMap = new HashMap<String, Object>();
				paramsMap.put("newsIdList", newsIdList);
				paramsMap.put("includeQiniu", includeQiniu);
				newslist = SqlMapClientHelper.getInstance(DB.MASTER)
						.queryForList("News.getHotFactorNewsList", paramsMap);
				newslist = UserService.addUserExtra(newslist);
			} catch (Exception e) {
				logger.error("getHotFactorNewsList(syncFilterNewsList) error ! ", e);
			}
			if (newslist != null && newslist.size() > 0) {
				List<News> sortedNews = new ArrayList<News>();
				newsmap.put(key, new Gson().toJson(newslist));
				Map<String, News> map = new HashMap<String, News>();
				for (News news : newslist) {
					map.put(news.getNewsId().toString(), news);
				}
				// 排序
				for (String newsId : newsScore) {
					if (map.containsKey(newsId)) {
						sortedNews.add(map.get(newsId));
					}
				}
				if (sortedNews != null && sortedNews.size() > 0) {
					newsmap.put(key, new Gson().toJson(sortedNews));
					NewsSource.setFilterNewsList(newsmap, includeQiniu);
					return sortedNews;
				}
			}
		}
		
		return null;
	}
	
	/**
	 *  获取动态列表
	 * @param newsScore
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<News> getNewsList(Set<String> newsScore, int includeQiniu){
		List<String> newsIdList = new ArrayList<String>();
		List<News> newlist = new ArrayList<News>();
		try {
			if (newsScore != null && newsScore.size() > 0) {
				for (Iterator<String> iterator = newsScore.iterator(); iterator.hasNext();) {
					newsIdList.add(iterator.next());
				}
			}
			if (newsIdList.size() > 0) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("newsIdList", newsIdList);
				map.put("includeQiniu", includeQiniu);
				newlist = SqlMapClientHelper.getInstance(DB.MASTER).queryForList(
						"News.getHotFactorNewsList", map);
				newlist = UserService.addUserExtra(newlist);
			}
		} catch (Exception e) {
			logger.error("NewsService.getNewsList error ! ", e);
		}
		
		return newlist;
	}
	
	/**
	 * 编辑动态视频标题 
	 * @param newsId 动态Id
	 * @param newsTitle 动态标题
	 * @return
	 */
	public static boolean editNewsTitle(int newsId, String newsTitle){
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("newsId", newsId);
			map.put("newsTitle", newsTitle);
			return SqlMapClientHelper.getInstance(DB.MASTER).update(
					"News.updateVideoTitle", map) == 1;
		} catch (Exception e) {
			logger.error("NewsService.updateVideoTitle exception, newsId : " 
					+ newsId + " ,newsTitle : " + newsTitle, e);
		}
		
		return false;
	}
	
	/**
	 * 获取某动态贴标签数量
	 * @param newsId
	 * @param tagId
	 * @return
	 */
	public static int getPaseCountOnNews(int newsId, Integer tagId) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		try {
			map.put("newsId", newsId);
			map.put("tagId", tagId);
			return (Integer)SqlMapClientHelper.getInstance(DB.MASTER).queryForObject(
					"NewsTagRes.selectPaseTagCountOnNews", map);
		} catch (Exception e) {
			logger.error("NewsService.getPaseCountOnNews exception, newsId : " 
					+ newsId + " ,tagId : " + tagId, e);
			
			return 0;
		}
	}
	
	/**
	 * 动态贴标签
	 * @param newsId 动态Id
	 * @param tagId 标签Id
	 * @param aid 后台贴标签用户aid
	 * @return
	 */
	public static boolean taggingOnNews(int newsId, int tagId, int aid) {
		try {
			// 判断标签是否存在
			NewsTagConf newsTagConf = getNewsTagInfoByIdOrName(tagId, null);
			if (newsTagConf != null) {
				// 判断是否已经贴过该标签
				if (getPaseCountOnNews(newsId, tagId) == 0) {
					NewsTagRes newsTagRes = new NewsTagRes();
					newsTagRes.setTagId(tagId);
					newsTagRes.setNewsId(newsId);
					newsTagRes.setAid(aid);
					newsTagRes.setDtime(new Date());
					SqlMapClientHelper.getInstance(DB.MASTER).insert(
							"NewsTagRes.tagOnNews", newsTagRes);
					return true;
				}
			}
		} catch (Exception e) {
			logger.error("NewsService.taggingOnNews exception, newsId : "
						+ newsId + " ,tagId : " + tagId + " ,aid : " + aid, e);
		}
		
		return false;
	}
	
	/**
	 * 动态删除标签
	 * @param newsId 动态Id
	 * @param tagId 标签Id
	 * @param mediaType 多媒体动态类型（3：视频（热拍））
	 * @return
	 */
	public static boolean deltagOnNews(int newsId, int tagId, int mediaType) {
		try {
			Map<String, Integer> map = new HashMap<String, Integer>();
			map.put("tagId", tagId);
			map.put("newsId", newsId);
			int tagCount = getRePaiCount(tagId, mediaType, null, null, null);
			if(tagCount == 1) {
				// 此标签没有用过 则同时删除该标签 
				SqlMapSession session = SqlMapClientHelper.getInstance(DB.MASTER).openSession();
				try {
					session.startTransaction();
					session.delete("NewsTagRes.deltagOnNews", map);
					session.delete("NewsTagConf.delConfNewsTagById", tagId);
					
					session.commitTransaction();
				} catch (Exception e) {
					logger.error("NewsService.deltagOnNews (session) exception", e);
				} finally {
					try {
						session.endTransaction();
					} catch (SQLException e) {
						logger.error("NewsService.deltagOnNews (Fail to end transaction) exception", e);
					} finally {
						session.close();
					}
				}
			} else if (tagCount > 1) {
				SqlMapClientHelper.getInstance(DB.MASTER).delete(
						"NewsTagRes.deltagOnNews", map);
			} else {
				return false;
			}
			
			return true;
		} catch (Exception e) {
			logger.error("NewsService.deltagOnNews exception, newsId : "
					+ newsId + " ,tagId : " + tagId, e);
		}
		
		return false;
	}

	/**
	 * 获取某热拍总数
	 * @param tagId 标签Id
	 * @param mediaType 视频类型
     * @param newsId 动态编号
     * @param keywords 动态关键字
     * @param userId 用户Id
	 * @return
	 */
	public static int getRePaiCount(Integer tagId, Integer mediaType, Integer newsId, String keywords, Integer userId) {
		if (tagId != null || newsId != null 
				|| keywords != null || userId != null) {
			// 从oracle取得总数
			Map<String, Object> map = new HashMap<String, Object>();
			try {
				map.put("tagId", tagId);
				map.put("mediaType", mediaType);
				map.put("newsId", newsId);
				map.put("keywords", keywords);
				map.put("userId", userId);
				return (Integer)SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("News.getHotNewsCount", map);
			} catch (Exception e) {
				logger.error("NewsService.getHotNewsCount exception, tagId : " + tagId, e);
			}
		} else {
			// 从redis取得总数
			return NewsSource.getFilterNewsCount();
		}
		
		return 0;
	}
	
	/**
	 * 获取热门视频动态总数
	 * @return
	 */
	/*public static int getCountOfHotVideoNews() {
		return NewsSource.getCountRankOfHotVideoNews();
	}*/
	
	/**
	 * 获取热门动态标签
	 * @param more 0: 热门标签；1:更多标签（返回50个）
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<NewsTagConf> getConfNewsTagList(int more) {
		if (more == 0) {
			List<String> resList = NewsSource.getHotTags();
			// 返回全部热门标签
			if (resList != null && resList.size() > 0) {
				return new Gson().fromJson(resList.toString(), new TypeToken<List<NewsTagConf>>(){}.getType());
			}
		} else {
			// 返回更多标签（被打标签数量最多的前50个）
			try {
				return SqlMapClientHelper.getInstance(DB.MASTER).queryForList("NewsTagConf.getMoreNewsTag");
			} catch (Exception e) {
				logger.error("NewsService.getConfNewsTagList, more : " + more, e);
			}
		}
		
		return null;
	}  
	
	/**
	 * 添加动态标签
	 * @param tagName
	 * @return
	 */
	public static boolean addConfNewsTag(String tagName) {
		try {
			// 判断标签是否已经存在
			if (getNewsTagInfoByIdOrName(null, tagName) == null) {
				NewsTagConf newsTagConf = new NewsTagConf();
				newsTagConf.setDtime(new Date());
				newsTagConf.setIsHot(0);
				newsTagConf.setTagName(tagName);
				return (Integer)SqlMapClientHelper.getInstance(DB.MASTER).insert("NewsTagConf.addNewsTag", newsTagConf) > 0;
			}	
		} catch (Exception e) {
			logger.error("NewsService.addConfNewsTag exception, tagName : " + tagName, e);
		}
		
		return false;
	}
	
	/**
	 * 删除动态标签
	 * @param tagId 标签Id
	 * @return
	 */
	public static boolean delConfNewstagById(int tagId) {
		SqlMapSession session = SqlMapClientHelper.getInstance(DB.MASTER).openSession();
		try {
			session.startTransaction();
			// 删除oracle标签
			session.delete("NewsTagConf.delConfNewsTagById", tagId);
			Map<String, Integer> map = new HashMap<String, Integer>();
			map.put("tagId", tagId);
			// 删除此标签关联的动态的记录信息
			session.delete("NewsTagRes.deltagOnNews", map);
			// 删除redis
			NewsSource.delNewsHotTag(tagId);
			session.commitTransaction();
			return true;
		} catch (Exception e) {
			logger.error("NewsService.delConfNewstag exception, tagId : " + tagId, e);
		} finally {
			try {
				session.endTransaction();
			} catch (SQLException e) {
				logger.error("NewsService.delConfNewstagById (Fail to end transaction) exception", e);
			} finally {
				session.close();
			}
		}
		
		return false;
	}
	
	/**
	 * 修改标签名称
	 * @param tagId 标签编号
	 * @param tagName 标签名称
	 */
	public static void updateTagName(int tagId, String tagName) {
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("tagId", tagId);
			map.put("tagName", tagName);
			SqlMapClientHelper.getInstance(DB.MASTER).insert("NewsTagConf.updateNewsTagById", map);
			// 判断是否为热门标签
			NewsTagConf newsTagConf = getNewsTagInfoByIdOrName(tagId, tagName);
			if (newsTagConf != null && newsTagConf.getIsHot() == 1) {
				Map<String, String> hotTags = new HashMap<String, String>();
				hotTags.put(String.valueOf(tagId),
						new Gson().toJson(newsTagConf.toJsonObjectForHotTag()));
				NewsSource.setNewsHotTags(hotTags);
			}
		} catch (Exception e) {
			logger.error("NewsService.updateTagName exception, tagId : " 
					+ tagId + " ,tagName : " + tagName, e);
		}
	}
	
	/**
	 * 根据编号或名称获取标签信息
	 * @param tagId 标签Id
	 * @param tagName 标签名称
	 * @return
	 */
	public static NewsTagConf getNewsTagInfoByIdOrName(Integer tagId, String tagName) {
		try {
			if (tagId == null && (tagName == null || tagName.trim().isEmpty())) {
				return null;
			}
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("tagId", tagId);
			map.put("tagName", tagName);
			return (NewsTagConf) SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("NewsTagConf.getNewsTagInfoByIdOrName", map);
		} catch (Exception e) {
			logger.error("NewsService.getNewsTagInfoById exception, tagId : " 
					+ tagId + " ,tagName : " + tagName, e);
			
			return null;
		}
	}

	/**
	 * 获取标签列表
	 * @param tagId 标签Id
	 * @param tagName 标签名称
	 * @param isHot 是否热门
	 * @param startTime 最小创建时间
	 * @param endTime 最大创建时间
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<NewsTagConf> getNewsTagList(Integer tagId, String tagName, Integer isHot, Date startTime, Date endTime) {
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("tagId", tagId);
			map.put("tagName", tagName);
			map.put("isHot", isHot);
			map.put("startTime", startTime);
			map.put("endTime", endTime);
			return (List<NewsTagConf>) SqlMapClientHelper.getInstance(DB.MASTER).queryForList("NewsTagConf.getNewsTagList", map);
		} catch (Exception e) {
			logger.error("NewsService.getNewsTagList exception, tagId : " 
					+ tagId + " ,tagName : " + tagName + " ,isHot : " + isHot 
					+ " ,startTime : " + startTime + " ,endTime : " + endTime, e);
			
			return null;
		}
	}
	
	/**
	 * 获取标签总数
	 * @param tagId 标签Id
	 * @param tagName 标签名称
	 * @param isHot 是否热门
	 * @param startTime 最小创建时间
	 * @param endTime 最大创建时间
	 * @return
	 */
	public static int getNewsTagCount(Integer tagId, String tagName, Integer isHot, Date startTime, Date endTime) {
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("tagId", tagId);
			map.put("tagName", tagName);
			map.put("isHot", isHot);
			map.put("startTime", startTime);
			map.put("endTime", endTime);
			return (Integer) SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("NewsTagConf.getNewsTagCount", map);
		} catch (Exception e) {
			logger.error("NewsService.getNewsTagCount exception, tagId : " 
					+ tagId + " ,tagName : " + tagName + " ,isHot : " + isHot 
					+ " ,startTime : " + startTime + " ,endTime : " + endTime, e);
		}
		
	 return 0;	
	}
	
	/**
	 * 设置、取消 热门   动态标签(已经设置过再次设置则会取消)
	 * @param tagId 标签Id
	 * @return
	 */
	public static boolean setHotNewsTagState(int tagId) {
		// 判断tagId 是否存在
		NewsTagConf newTagInfo = getNewsTagInfoByIdOrName(tagId, null);
		if (newTagInfo != null) {
			try {
				Map<String, Object> parmsmap = new HashMap<String, Object>();
				parmsmap.put("tagId", tagId);
				parmsmap.put("isHot", 1);
				if (SqlMapClientHelper.getInstance(DB.MASTER).update(
						"NewsTagConf.updateNewsTagById", parmsmap) == 1) {
					if (newTagInfo.getIsHot() != 1) {
						// redis 中插入该标签
						Map<String, String> map = new HashMap<String, String>();
						map.put(String.valueOf(tagId),
								new Gson().toJson(newTagInfo.toJsonObjectForHotTag()));
						NewsSource.setNewsHotTags(map);
					} else {
						// redis删除该热门标签
						NewsSource.delNewsHotTag(tagId);
					}
					
					return true;
				}
			} catch (Exception e) {
				logger.error("NewsService.setHotNewsTagState exception, tagId : " + tagId, e);
			}
		}

		return false;
	}
	
	/**
	 * 获取热拍列表
	 * @param tagId 标签Id
	 * @param pageIndex 起始页
	 * @param countPerPage 每页显示条数
	 * @param mediaType 多媒体类型
	 * @param newsId 动态编号
	 * @param userId 用户编号
	 * @param keywords 关键字
	 * @return
	 */
	public static List<News> getRePaiHotNewsList(Integer tagId, int pageIndex, int countPerPage,
			Integer mediaType, Integer newsId, Integer userId, String keywords, int includeQiniu) {
		int start = (pageIndex - 1) * countPerPage + 1;
		int end = pageIndex * countPerPage;
		String value = null;
		if ((newsId == null || newsId == 0) && (userId == null || userId == 0) 
				&& (keywords == null || keywords.trim().isEmpty())) {
			value = NewsSource.getRePaiPageList(tagId, start + "_" + end, includeQiniu);
		}
		// 更新redis数据
		if (value == null || value.trim().isEmpty()) {
			return syncRePaiNewsList(tagId, start, end, mediaType, newsId, userId, keywords, includeQiniu);
		} else {
			try {
				return new Gson().fromJson(value, new TypeToken<List<News>>(){}.getType());
			} catch (Exception e) {
				logger.error("NewsService.getRePaiHotNewsList(pase value to List<News>) exception,  value : " + value, e);
				return null;
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private static List<News> syncRePaiNewsList(Integer tagId, int start, int end, 
			Integer mediaType, Integer newsId, Integer userId, String keywords, int includeQiniu) {
		String key = start + "_" + end;
		List<News> newsList = new ArrayList<News>();
		 try {
			 Map<String, Object> map = new HashMap<String, Object>();
			 map.put("includeQiniu", includeQiniu);
			 if ((tagId != null && tagId > 0) || (newsId != null && newsId > 0)
					 || (userId != null && userId > 0)
					 	|| (keywords != null && !keywords.trim().isEmpty())) {
				 map.put("mediaType", mediaType);
				 map.put("min", start - 1);
				 map.put("max", end);
				 map.put("tagId", tagId);
				 map.put("newsId", newsId);
				 map.put("keywords", keywords);
				 map.put("userId", userId);
				 newsList = SqlMapClientHelper.getInstance(DB.MASTER).queryForList("News.getRePaiNewsInfoByParams", map);
				 newsList = UserService.addUserExtra(newsList);
			 } else {
				 Set<Tuple> tupleSet = NewsSource.getRankOfHotVideoNews(start, end);
				 List<Integer> newsIdList = new ArrayList<Integer>();
				 Map<Integer, Double> newsScoreMap = new HashMap<Integer, Double>();
				   if (!tupleSet.isEmpty() && tupleSet.iterator().hasNext()) {
					   for (Tuple tp : tupleSet) {
						   int newsid = Integer.parseInt(tp.getElement());
						   double score = Double.valueOf(tp.getScore()).longValue();
						   newsIdList.add(newsid);
						   newsScoreMap.put(newsid, score);
					   }
					}
					if (newsIdList.size() > 0) {
						map.put("newsIdList", newsIdList);
						try {
							newsList = SqlMapClientHelper.getInstance(DB.MASTER).queryForList("News.getHotFactorNewsList", map);
							newsList = UserService.addUserExtra(newsList);
							// 排序
							List<News> newsListNew = new ArrayList<News>();
							for (Integer newsid : newsIdList) {
								for (int i = 0; i < newsList.size(); i++) {
									News news = newsList.get(i);
									if (newsid.intValue() == news.getNewsId()) {
										news.setRunScore(newsScoreMap.get(news.getNewsId()).intValue());
										newsListNew.add(news);
										newsList.remove(news);
										i = 0;
									}
								}
							}
							newsList = newsListNew;
						} catch (Exception e) {
							logger.error("getHotFactorNewsList error ! ", e);
						}
					}
			 }
			 if (newsList.size() > 0
					 && (newsId == null || newsId == 0) 
					 && (userId == null || userId == 0)
					 && (keywords == null || keywords.trim().isEmpty())) {
				 Map<String, String> newsmap = new HashMap<String, String>();
					newsmap.put(key, new Gson().toJson(newsList));
					NewsSource.setRePaiPageList(tagId, newsmap, includeQiniu);
					return newsList;
				}
		} catch (Exception e) {
			logger.error("NewsService.updateRePaiToRedis exception,  tagId : " + tagId
					+ " , start : " + start + " ,end : " + end, e);
		}

		return newsList;
	}
	
	/**
	 * 根据上次读取时间获得未读新动态数量
	 * @param lastNewsTime
	 * @return
	 */
	public static int getUnReadNewsNum(int userId, long lastNewsTime) {
		try {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("userId", userId);
			map.put("lastNewsTime", DateUtil.formatDateTime(new Date(lastNewsTime), "yyyyMMddHHmmss"));
			int num = (Integer) SqlMapClientHelper.getInstance(DB.MASTER).queryForObject("News.getNewsNumbyLasttime", map);
			if (num > 0) {
				return num;
			}
		} catch(Exception e) {
			logger.error("NewsService.getUnReadNewsNum, userId :" + userId
					 + " ,lastNewsTime : " + lastNewsTime, e);
		}
		return 0;
	}
    
	/**
	 * (添加、修改)人工推荐动态
	 * @param index
	 * @param newsId
	 */
	public static void updateManualNews(String index, String newsId) {
		if (index != null) {
			newsId = newsId == null ? "" : newsId;
			Map<String, String> manualNews = new HashMap<String, String>();
			manualNews.put(index, newsId);
			NewsSource.addManualNews(manualNews);
		}
	}
	
	/**
	 * 获取人工推荐动态
	 * @return
	 */
	@SuppressWarnings("unchecked")
    public static Map<Integer, News> getManualNews() {
		Map<Integer, News> resMap = new HashMap<Integer, News>();
			Map<String, String> manualNewsMap = NewsSource.getManualNews();
			if (manualNewsMap != null && manualNewsMap.size() > 0) {
				List<Integer> manualNewIds = new ArrayList<Integer>(); 
				for (Map.Entry<String, String> entry : manualNewsMap.entrySet()) { 
					try {
						manualNewIds.add(Integer.parseInt(entry.getValue()));
					} catch (Exception e) { }
				}
				if (manualNewIds.size() > 0) {
					try {
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("newsIdList", manualNewIds);
						map.put("includeQiniu", 1);
						List<News> manualNewsList = SqlMapClientHelper.getInstance(DB.MASTER).queryForList("News.getHotFactorNewsList", map);
						manualNewsList = UserService.addUserExtra(manualNewsList);
						if (manualNewsList != null && manualNewsList.size() > 0) {
							for (Map.Entry<String, String> entry : manualNewsMap.entrySet()) {
								String newsIdStr = entry.getValue();
								if (newsIdStr != null && !newsIdStr.trim().isEmpty()) {
									int newsId = Integer.parseInt(newsIdStr);
									for (News manualNews : manualNewsList) {
										if (newsId == manualNews.getNewsId()) {
											resMap.put(Integer.parseInt(entry.getKey()), manualNews);
										}
									}
								}
							}
						}
					} catch (SQLException e) {
						logger.error("NewsService.getManualNews exception !", e);
					}
				}
				
			}
		
		return resMap;
	}
	
	/**
	 * 删除人工推荐动态
	 * @param newsId
	 */
	public static void delManualNews(String index) {
		NewsSource.delManualNews(index);
	}
	
	/**
	 * 获取筛选动态key
	 * @return
	 */
	public static String getFilterNewsKey() {
		return NewsSource.getFilterNewsKey();
	}
	
	/**
	 * 删除七牛、又拍云上的文件
	 * @param news 动态信息
	 */
	public static void delNewsOnThirdPart(News news) {
		if (news != null && news.getMediaSource() != null) {
			try {
				JsonObject mediaSourceJson = new JsonParser().parse(news.getMediaSource()).getAsJsonObject();
				String mediaUrl = null;
				if (mediaSourceJson.has("mediaUrl") && !mediaSourceJson.get("mediaUrl").isJsonNull()) {
					mediaUrl = mediaSourceJson.get("mediaUrl").getAsString();
				}
				String imageUrl = null;
				if (mediaSourceJson.has("imageUrl") && !mediaSourceJson.get("imageUrl").isJsonNull()) {
					imageUrl = mediaSourceJson.get("imageUrl").getAsString();
				}
				Integer mediafrom = news.getMediaFrom();
				if (mediafrom != null && mediafrom.intValue() == 2) {
					// 删除七牛文件
					QiniuService qiniuService = new QiniuService(ConfigHelper.getAccessKey(), ConfigHelper.getSecretKey());
					if (!StringUtil.strIsNull(mediaUrl) && !qiniuService.deleteFile(ConfigHelper.getBucket(), mediaUrl)) {
						logger.error("NewsService.delNewsOnThirdPart exception, newsId : " + news.getNewsId());
					}
					if (!StringUtil.strIsNull(imageUrl) && !qiniuService.deleteFile(ConfigHelper.getBucket(), imageUrl)) {
						logger.error("NewsService.delNewsOnThirdPart exception, newsId : " + news.getNewsId());
					}
				} else {
					// 删除又拍云文件
					if (!StringUtil.strIsNull(imageUrl) && !imageUrl.contains("checking")) {
						UpYun upyun = new UpYun(Constant.YOUPAI_BUCKET, Constant.YOUPAI_USER_NAME, Constant.YOUPAI_USER_PWD);
						// ****** 可选设置 ******
						// 切换 API 接口的域名接入点，默认为自动识别接入点
						upyun.setApiDomain(UpYun.ED_AUTO);
						// 设置连接超时时间，默认为30秒
						upyun.setTimeout(60);
						// 设置是否开启debug模式，默认不开启
						upyun.setDebug(true);
						try {
						    int i = 0;
	                        boolean flag = false;
	                        while (i++ < 5) {
	                            if(upyun.deleteFile("kktv" + imageUrl)) {
	                                flag = true;
	                                break;
	                            }
	                        }
	                        if (!flag) {
	                            logger.error("[NewsService]: Failed to delete pictures[" + "kktv" + imageUrl + "] from Youpai.");
	                        }
						} catch (Exception e) {
							logger.error("[NewsService]: Failed to delete pictures[" + "kktv" + imageUrl + "] from Youpai." + e);			
						}
					}
					if (!StringUtil.strIsNull(mediaUrl) && !mediaUrl.contains("checking")) {
						UpYun upyun = new UpYun(Constant.YOUPAI_FILES_BUCKET, Constant.YOUPAI_FILES_USER_NAME, Constant.YOUPAI_FILES_USER_PWD);
						// ****** 可选设置 ******
						// 切换 API 接口的域名接入点，默认为自动识别接入点
						upyun.setApiDomain(UpYun.ED_AUTO);
						// 设置连接超时时间，默认为30秒
						upyun.setTimeout(60);
						// 设置是否开启debug模式，默认不开启
						upyun.setDebug(true);
						try {
							if(!upyun.deleteFile("kktv" + mediaUrl)) {
								logger.error("[NewsService]: Failed to delete files from Youpai.");
							}
						} catch (Exception e) {
							logger.error("[NewsService]: Failed to delete files from Youpai." + e);			
						}
					}
				}
			} catch (Exception e) {
				logger.error("NewsService.delNewsOnThirdPart exception, newsId : " + news.getNewsId(), e);
			}
		}
	}
	
	public static int addNews(NewsInfo newsInfo, String topic) {
		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
		if (newsService != null) {
			return newsService.addNews(newsInfo, topic);
		}
		return 0;
	}
	
	/**
	 * 删除动态
	 * @return
	 */
	public static boolean deleteNews(int newsId, int userId) {
		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
		if (newsService != null) {
			return newsService.delNewsInfo(newsId, userId);
		}
		return false;
	}
	
	/**
	 * 获取动态信息
	 * @param newsId
	 * @return
	 */
	public static NewsInfo getNewsInfoById(int newsId, int userId) {
		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
		if (newsService != null) {
			return newsService.getNewsInfoByNewsId(newsId, userId);
		}
		return null;
	}
	
	public static NewsInfo getNewsInfoByNewsIdForState(int newsId, int userId) {
		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
		if (newsService != null) {
			return newsService.getNewsInfoByNewsIdForState(newsId, userId);
		}
		return null;
	}
	
	/**
	 * 添加评论(打赏到pg)
	 * @param newsCommentHist
	 * @return
	 */
	public static int addCommentPg(NewsCommentHist newsCommentHist) {
		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
		if (newsService == null) {
			return 0;
		}
		int commentId = newsService.addNewsComment(newsCommentHist);
		return commentId;
	}
	
	/**
	 * 获取动态评论
	 * @param commentId
	 * @return
	 */
	public static NewsCommentHist getComment(int commentId) {
		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
		if (newsService != null) {
			return newsService.getComment(commentId);
		}
		return null;
	}
	
	/**
	 * 评论是否存在
	 * @param newsId
	 * @param content
	 * @return
	 */
	public static int ifCommentExist(int newsId, String content) {
		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
		if (newsService != null) {
			return newsService.ifCommentExist(newsId, content);
		}
		return 0;
	}
	
	public static int addPraise(int userId, int commentId) {
		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
		if (newsService != null) {
			return newsService.addCommentPraise(userId, commentId);
		}
		return 0;
	}
	
	public static int cancelPraise(int userId, int commentId) {
		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
		if (newsService != null) {
			return newsService.cancelCommentPraise(userId, commentId);
		}
		return 0;
	}
	
	public static List<NewsCommentHist> getRecommendComment(int topicId) {
		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
		if (newsService != null) {
			return newsService.getRecommendComment(topicId);
		}
		return null;
	}
	
	public static int addNewsPraise(int userId, int newsId) {
		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
		if (newsService != null) {
			return newsService.addNewsPraise(userId, newsId);
		}
		return 0;
	}
	
	public static int cancelNewsPraise(int userId, int newsId) {
		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
		if (newsService != null) {
			return newsService.cancelNewsPraise(userId, newsId);
		}
		return 0;
	}
	
	/**
	 * 删除动态评论
	 * @param commentId
	 * @return
	 */
	public static boolean deleteComment(int commentId, int newsId) {
		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
		if (newsService != null) {
			return newsService.deleteComment(commentId, newsId);
		}
		return false;
	}

	/**
	 * 获取用户动态总数
	 * @param userId
	 * @param isSelf
	 * @return
	 */
	public static int getNewsCountByUserId(int userId, int isSelf) {
		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
		if (newsService != null) {
			return newsService.getSelfNewsCount(userId, isSelf);
		}
		return 0;
	}
	
	/**
	 * 获取个人动态列表
	 * @param userId
	 * @param start
	 * @param offset
	 * @param isSelf
	 * @return
	 */
	public static List<NewsInfo> getSelfNewsList(int userId, int start, int offset, int isSelf,int seeUserId) {
		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
		if (newsService != null) {
			return newsService.getSelfNewsList(userId, start, offset, isSelf, seeUserId);
		}
		return null;
	}
	
	/**
	 * 
	 * @param newsInfo
	 * @param platform
	 * @param flag 是否查询主播头像相关信息
	 * @return
	 */
	public static JsonObject getNewResourceJson(NewsInfo newsInfo, int platform, boolean flag) {
		JsonObject json = new JsonObject();
		json.addProperty("userId", newsInfo.getUserId());
		UserStaticInfo us = UserService.getStaticInfo(newsInfo.getUserId());
		if (us != null) {
			if (us.getProfile() != null) {
				// 读取明星等级
				json.addProperty("actorLevel", us.getProfile().getActorLevel());
				// 读取富豪等级
				json.addProperty("richLevel", us.getProfile().getUserLevel());
			}
			if (us.getRegisterInfo() != null) {
				json.addProperty("city", Math.abs(us.getRegisterInfo().getCityId()));
			}
		}
		json.addProperty("newsId", newsInfo.getNewsId());
		if (newsInfo.getContent() != null) {
			json.addProperty("content", newsInfo.getContent());
		}
		json.addProperty("publishedTime", newsInfo.getPublishedTime().getTime());
		json.addProperty("newsType", newsInfo.getNewsType());
		if (newsInfo.getTopic() != null) {
			json.addProperty("topic", newsInfo.getTopic());
		}
		if (newsInfo.getTopicId() != null) {
			json.addProperty("topicId", newsInfo.getTopicId());
		}

		json.addProperty("commentCount", NewsService.getCommentCount(newsInfo.getNewsId()));

		if (newsInfo.getCommentMsg() != null && !newsInfo.getCommentMsg().trim().isEmpty()) {
			String commentMsg = newsInfo.getCommentMsg();
			JsonArray commentList = new JsonParser().parse(commentMsg).getAsJsonArray();
			for (JsonElement je: commentList) {
				JsonObject temp = je.getAsJsonObject();
				if (temp.has("portrait_path")) {
					temp.addProperty("portrait_path_128", temp.get("portrait_path").getAsString() + "!128x96");
				}
			}
			json.add("commentList", commentList);
		}
		if (newsInfo.getCommentPraise() != null) {
			json.addProperty("newsPraiseCount", newsInfo.getCommentPraise());
		} else {
			json.addProperty("newsPraiseCount", 0);
		}
		if (newsInfo.getIsPraise() != null) {
			json.addProperty("isPraise", newsInfo.getIsPraise());
		}
		if (newsInfo.getRefVideo() != null) {
			//{"mediaUrl":"/2014/3/25/1008198_36000.mp4","imageUrl":"/2014/3/25/1008198_36000.jpg","mediaSize":2048,"mediaDur":60000}
			JsonObject mediaSourceJson = new JsonObject();
			int resId = Integer.valueOf(Pattern.compile("\\{|\\}").matcher(newsInfo.getRefVideo()).replaceAll(""));
			Resource resVideo = ResourceService.getResource(resId, 3);
			int mediaFrom = 2;
			if (resVideo != null && resVideo.getTitle() != null) {
				mediaSourceJson.addProperty("mediaFrom", Integer.valueOf(resVideo.getTitle()));
				mediaFrom = Integer.valueOf(resVideo.getTitle());
			}
			if (resVideo != null && resVideo.getImageUrl() != null) {
				if (mediaFrom == 1) {
					// 分平台返回不同尺寸图片
					String path_original = resVideo.getImageUrl();
					String path_1280 = null;
					String path_720 = null;
					String path_400 = null;
					String path_272 = null;
					String path_128 = null;
					path_1280 = path_original + "!1280";
					path_720 = path_original + "!720";
					path_400 = path_original + "!400";
					path_272 = path_original + "!272";
					path_128 = path_original + "!128x96";
					mediaSourceJson.addProperty("imageUrl_1280", path_1280);
					mediaSourceJson.addProperty("imageUrl_720", path_720);
					mediaSourceJson.addProperty("imageUrl_400", path_400);
					mediaSourceJson.addProperty("imageUrl_272", path_272);
					mediaSourceJson.addProperty("imageUrl_128", path_128);
					mediaSourceJson.addProperty("imageUrl", path_400);
				} else {
					// 分平台返回不同尺寸图片
					String path_original = resVideo.getImageUrl();
					String path_1280 = null;
					String path_720 = null;
					String path_272 = null;
					String path_128 = null;
					String path_400 = null;
					Integer high = resVideo.getFileHeight();
					Integer width = resVideo.getFileWidth();
					if (high != null && width != null && high > 0 && width > 0) {
						int phigh = (400 * high) / width;
						mediaSourceJson.addProperty("imageUrl", path_original + "?imageView2/1/w/400/h/" + phigh);
					} else {
						mediaSourceJson.addProperty("imageUrl", path_original + "?imageView2/1/w/400/h/400");
					}
					path_1280 = path_original;
					path_720 = path_original;
					path_400 = path_original + "?imageView2/1/w/400/h/300";
					path_272 = path_original;
					path_128 = path_original;
					mediaSourceJson.addProperty("imageUrl_1280", path_1280);
					mediaSourceJson.addProperty("imageUrl_720", path_720);
					mediaSourceJson.addProperty("imageUrl_400", path_400);
					mediaSourceJson.addProperty("imageUrl_272", path_272);
					mediaSourceJson.addProperty("imageUrl_128", path_128);
					//mediaSourceJson.remove("imageUrl");
				}
			}
			if (resVideo != null) {
				if (resVideo.getDuration() != null) {
					mediaSourceJson.addProperty("mediaDur", resVideo.getDuration());
				}
				if (resVideo.getSpecificUrl() != null) {
					mediaSourceJson.addProperty("mediaUrl", resVideo.getSpecificUrl());
				}
				if (resVideo.getFileHeight() != null) {
					mediaSourceJson.addProperty("mediaHeight", resVideo.getFileHeight());
				}
				if (resVideo.getFileWidth() != null) {
					mediaSourceJson.addProperty("mediaWidth", resVideo.getFileWidth());
				}
			}
			
			json.add("mediaSource", mediaSourceJson);
		} else if (newsInfo.getRefImage() != null) {
			List<Resource> resImage = ResourceService.getResourceList(Pattern.compile("\\{|\\}").matcher(newsInfo.getRefImage()).replaceAll(""));
			if (resImage != null && resImage.size() > 0) {
				JsonArray picArray = new JsonArray();
				for (Resource resource : resImage) {
					JsonObject mediaJson = new JsonObject();
					mediaJson.addProperty("mediaType", NewsMediaTypeEnum.IMAGE);
					String path = resource.getImageUrl();
					path = path.substring(0,path.length());
					String path_1280 = path + "!1280";
					String path_720 = path + "!720";
					String path_400 = path + "!400";
					String path_272 = path + "!272";
					String path_128 = path + "!128x96";
					mediaJson.addProperty("imageUrl_1280", path_1280);
					mediaJson.addProperty("imageUrl_720", path_720);
					mediaJson.addProperty("imageUrl_400", path_400);
					mediaJson.addProperty("imageUrl_272", path_272);
					mediaJson.addProperty("imageUrl_128", path_128);
					mediaJson.addProperty("imageUrl", path_400);
					picArray.add(mediaJson);
				}
				json.add("picArray", picArray);
			}
		}
		
		/*// 解析多媒体资源   {"mediaUrl":"/2014/3/25/1008198_36000.mp4","imageUrl":"/2014/3/25/1008198_36000.jpg","mediaSize":2048,"mediaDur":60000}
		if (newsInfo.getRefImage() != null) {
			ResourceService.getResource(Integer.valueOf(newsInfo.getRefImage()), 1);
			
		}*/
		/*if (newsInfo.getRefAudio() != null) {
			JsonObject mediaSourceJson = new JsonObject();
			Resource resAudio = ResourceService.getResource(Integer.valueOf(newsInfo.getRefAudio()), 2);
			if (resAudio.getImageUrl() != null) {
				// 分平台返回不同尺寸图片
				String path_original = resAudio.getImageUrl();
				String path_1280 = null;
				String path_720 = null;
				String path_272 = null;
				String path_128 = null;
				path_1280 = path_original + "!1280";
				path_720 = path_original + "!720";
				path_272 = path_original + "!272";
				path_128 = path_original + "!128x96";
				mediaSourceJson.addProperty("imageUrl_1280", path_1280);
				mediaSourceJson.addProperty("imageUrl_720", path_720);
				mediaSourceJson.addProperty("imageUrl_272", path_272);
				mediaSourceJson.addProperty("imageUrl_128", path_128);
				mediaSourceJson.remove("imageUrl");
			}
			if (resAudio.getDuration() != null) {
				mediaSourceJson.addProperty("mediaDur", resAudio.getDuration());
			}
			if (resAudio.getSpecificUrl() != null) {
				mediaSourceJson.addProperty("mediaUrl", resAudio.getSpecificUrl());
			}
			json.add("mediaSource", mediaSourceJson);
		}*/
		
		if (flag) {
			RoomInfo actorInfo = RoomService.getRoomInfo(newsInfo.getUserId());
			if (actorInfo != null) {
				json.addProperty("nickname", actorInfo.getNickname());
				if (actorInfo.getGender() != null) {
					json.addProperty("gender", actorInfo.getGender());
				}
				if (actorInfo.getPortrait() != null) {
					if (platform == PlatformEnum.WEB) {
						json.addProperty("portrait_path_256", actorInfo.getPortrait() + "!256");
					} else if (platform == PlatformEnum.ANDROID) {
						json.addProperty("portrait_path_48",  actorInfo.getPortrait() + "!48");
						json.addProperty("portrait_path_128", actorInfo.getPortrait() + "!128");
					} else if (platform == PlatformEnum.IPHONE) {
						json.addProperty("portrait_path_128", actorInfo.getPortrait() + "!128");
					} else if (platform == PlatformEnum.IPAD) {
						json.addProperty("portrait_path_128", actorInfo.getPortrait() + "!128");
					} else {
						json.addProperty("portrait_path_1280",actorInfo.getPortrait() + "!1280");
						json.addProperty("portrait_path_256", actorInfo.getPortrait() + "!256");
						json.addProperty("portrait_path_128", actorInfo.getPortrait() + "!128");
						json.addProperty("portrait_path_48",  actorInfo.getPortrait() + "!48");
					}
				}
				json.addProperty("actorTag", 1);
				json.addProperty("isLive", (actorInfo.getLiveStarttime() != null && actorInfo.getLiveEndtime() == null) ? 1 : 0);
				json.addProperty("roomSource", actorInfo.getRoomSource());
				json.addProperty("screenType", actorInfo.getScreenType());
			} else {
				UserProfile userInfo = com.melot.kktv.service.UserService.getUserInfoV2(newsInfo.getUserId());
				if (userInfo != null) {
					json.addProperty("nickname", userInfo.getNickName());
					json.addProperty("gender", userInfo.getGender());
					json.addProperty("actorTag", 0);
					if (userInfo.getPortrait() != null) {
						if (platform == PlatformEnum.WEB) {
							json.addProperty("portrait_path_256", userInfo.getPortrait() + "!256");
						} else if (platform == PlatformEnum.ANDROID) {
							json.addProperty("portrait_path_48",  userInfo.getPortrait() + "!48");
							json.addProperty("portrait_path_128", userInfo.getPortrait() + "!128");
						} else if (platform == PlatformEnum.IPHONE) {
							json.addProperty("portrait_path_128", userInfo.getPortrait() + "!128");
						} else if (platform == PlatformEnum.IPAD) {
							json.addProperty("portrait_path_128", userInfo.getPortrait() + "!128");
						} else {
							json.addProperty("portrait_path_1280", userInfo.getPortrait() + "!1280");
							json.addProperty("portrait_path_256", userInfo.getPortrait() + "!256");
							json.addProperty("portrait_path_128", userInfo.getPortrait() + "!128");
							json.addProperty("portrait_path_48", userInfo.getPortrait() + "!48");
						}
					}
				} 
			}
		}
		
		return json;
	}
	
	public static List<NewsInfo> getNewsListByTopicId(int topicId, int sortType, int start, int offset, int seeUserId) {
		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
		if (newsService != null) {
			return newsService.getNewsListByTopicId(topicId, sortType, start, offset, seeUserId);
		}
		return null;
	}
	
	public static int getTopicNewsCount(int topicId) {
		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
		if (newsService != null) {
			return newsService.getNewsListCountByTopicId(topicId);
		}
		return 0;
	}
	
	public static int getCommentNewsCount(String content) {
		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
		if (newsService != null) {
			return newsService.getNewsListCountByCommentContent(content);
		}
		return 0;
	}

	
	public static List<NewsInfo> getNewsListByComment(String content, int sortType, int start, int offset, int seeUserId) {
		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
		if (newsService != null) {
			return newsService.getNewsListByComment(content, sortType, start, offset, seeUserId);
		}
		return null;
	}
	
	public static List<NewsInfo> getStateByNewsIds(String newsIds) {
		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
		if (newsService != null) {
			return newsService.getStateByNewsIds(newsIds);
		}
		return null;
	}
	
	public static List<NewsInfo> getFollowNewsList(int userId, int start, int offset) {
		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
		if (newsService != null) {
			return newsService.getUserAndFollowedNewsList(userId, start, offset);
		}
		return null;
	}

	public static List<NewsCommentHist> getCommentList(int newsId, int start, int offset, int userId) {
		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
		if (newsService != null) {
			return newsService.getCommentList(newsId, start, offset, userId);
		}
		return null;
	}
	
	public static int getCommentCount(int newsId) {
		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
		if (newsService != null) {
			return newsService.getCommentcount(newsId);
		}
		return 0;
	}
	
	public static List<NewsInfo> getPraiseAndTopicMsg(List<NewsInfo> newsList, int userId) {
		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
		if (newsService != null) {
			return newsService.getPraiseAndTopicMsg(newsList, userId);
		}
		return null;
	}
	
	public static boolean isWhiteUser(int userId) { 
		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
		if (newsService != null) {
			List<WhiteUser> list = newsService.getWhiteUserList(0, 0);
			for (WhiteUser whiteUser : list) {
				if (whiteUser.getUserId() == userId) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static Set<String> getPopularTopic(int start, int offset) {
		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
		if (newsService != null) {
			return newsService.getPopularTopic(start, offset);
		}
		return null;
	}

	
}

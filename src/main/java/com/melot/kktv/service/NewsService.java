package com.melot.kktv.service;

import com.google.gson.*;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.kk.module.resource.domain.Resource;
import com.melot.kk.module.resource.service.ResourceNewService;
import com.melot.kk.userSecurity.api.domain.DO.UserVerifyDO;
import com.melot.kk.userSecurity.api.service.UserVerifyService;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcore.user.api.UserStaticInfo;
import com.melot.kkcx.service.RoomService;
import com.melot.kkcx.service.UserService;
import com.melot.kktv.base.CommonStateCode;
import com.melot.kktv.base.Page;
import com.melot.kktv.base.Result;
import com.melot.kktv.util.NewsMediaTypeEnum;
import com.melot.kktv.util.PlatformEnum;
import com.melot.news.domain.NewsCommentHist;
import com.melot.news.model.NewsInfo;
import com.melot.news.model.NewsTopic;
import com.melot.news.model.WhiteUser;
import com.melot.sdk.core.util.MelotBeanFactory;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;


public class NewsService {

	private static Logger logger = Logger.getLogger(NewsService.class);


	/**
	 * 根据上次读取时间获得未读新动态数量
	 * @param lastNewsTime
	 * @return
	 */
	public static int getUnReadNewsNum(int userId, long lastNewsTime) {
		return 0;
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
		if (newsInfo.getNewsTitle() != null) {
			json.addProperty("newsTitle", newsInfo.getNewsTitle());
		}
		if(newsInfo.getPublishedTime() != null){
			json.addProperty("publishedTime", newsInfo.getPublishedTime().getTime());
		}
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

			ResourceNewService resourceNewService = (ResourceNewService) MelotBeanFactory.getBean("resourceNewService");
			Resource resVideo = resourceNewService.getResourceById(resId).getData();
			int mediaFrom = 2;
//			if (resVideo != null && resVideo.getTitle() != null) {
			mediaSourceJson.addProperty("mediaFrom", mediaFrom);
//				mediaFrom = Integer.valueOf(resVideo.getTitle());
//			}
			if (resVideo != null && resVideo.getImageUrl() != null) {
				if (mediaFrom == 1) {
					// 分平台返回不同尺寸图片
					String path_original = resVideo.getImageUrl();
					String path_1280 = null;
					String path_720 = null;
					String path_400 = null;
					String path_300 = null;
					String path_272 = null;
					String path_128 = null;
					path_1280 = path_original + "!1280";
					path_720 = path_original + "!720";
					path_400 = path_original + "!400";
					path_300 = path_original + "!300";
					path_272 = path_original + "!272";
					path_128 = path_original + "!128x96";
					mediaSourceJson.addProperty("imageUrl_1280", path_1280);
					mediaSourceJson.addProperty("imageUrl_720", path_720);
					mediaSourceJson.addProperty("imageUrl_400", path_400);
					mediaSourceJson.addProperty("imageUrl_300", path_300);
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
					String path_300 = null;
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
					path_300 = path_original + "?imageView2/1/w/300/h/300";
					path_272 = path_original;
					path_128 = path_original;
					mediaSourceJson.addProperty("imageUrl_1280", path_1280);
					mediaSourceJson.addProperty("imageUrl_720", path_720);
					mediaSourceJson.addProperty("imageUrl_400", path_400);
					mediaSourceJson.addProperty("imageUrl_300", path_300);
					mediaSourceJson.addProperty("imageUrl_272", path_272);
					mediaSourceJson.addProperty("imageUrl_128", path_128);
					//mediaSourceJson.remove("imageUrl");
				}
			}
			if (resVideo != null) {
				mediaSourceJson.addProperty("mediaType", NewsMediaTypeEnum.VIDEO);
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
		} else if (newsInfo.getRefAudio() != null) {
			JsonObject mediaSourceJson = new JsonObject();
			ResourceNewService resourceNewService = (ResourceNewService) MelotBeanFactory.getBean("resourceNewService");
			Resource resAudio = resourceNewService.getResourceById(Integer.valueOf(Pattern.compile("\\{|\\}").matcher(newsInfo.getRefAudio()).replaceAll(""))).getData();
			if(resAudio != null){
				mediaSourceJson.addProperty("mediaFrom", 2);
				mediaSourceJson.addProperty("mediaType", NewsMediaTypeEnum.AUDIO);
				if (resAudio.getSpecificUrl() != null) {
					mediaSourceJson.addProperty("mediaUrl", resAudio.getSpecificUrl());
				}
				mediaSourceJson.addProperty("mediaState", resAudio.getState());
				if (newsInfo.getRefImage() != null){
					Resource resImage = resourceNewService.getResourceById(Integer.valueOf(Pattern.compile("\\{|\\}").matcher(newsInfo.getRefImage()).replaceAll(""))).getData();
					if(resImage!= null){
						String path_original = resImage.getImageUrl();
						String path_1280 = null;
						String path_720 = null;
						String path_400 = null;
						String path_300 = null;
						String path_272 = null;
						String path_128 = null;
						path_1280 = path_original + "!1280";
						path_720 = path_original + "!720";
						path_400 = path_original + "!400";
						path_300 = path_original + "!300";
						path_272 = path_original + "!272";
						path_128 = path_original + "!128x96";
						mediaSourceJson.addProperty("imageUrl_1280", path_1280);
						mediaSourceJson.addProperty("imageUrl_720", path_720);
						mediaSourceJson.addProperty("imageUrl_400", path_400);
						mediaSourceJson.addProperty("imageUrl_300", path_300);
						mediaSourceJson.addProperty("imageUrl_272", path_272);
						mediaSourceJson.addProperty("imageUrl_128", path_128);
						mediaSourceJson.addProperty("imageUrl", path_400);
						mediaSourceJson.addProperty("imageState", resImage.getState());
					}
				}
			}
			json.add("mediaSource", mediaSourceJson);
		}
		else if (newsInfo.getRefImage() != null) {
			ResourceNewService resourceNewService = (ResourceNewService) MelotBeanFactory.getBean("resourceNewService");
			List<Resource> resImage = resourceNewService.getResourcesByIds(Pattern.compile("\\{|\\}").matcher(newsInfo.getRefImage()).replaceAll("")).getData();
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

	public static List<NewsInfo> getNewsListByResType(int userId, int newsType, int start, int offset) {
		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
		if (newsService != null) {
			return newsService.getNewsListByNewsType(userId, newsType, start, offset);
		}
		return null;
	}

	public static int getNewsCountByResType(int userId, int newsType, int state) {
		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
		if (newsService != null) {
			return newsService.getNewsInfoCount(userId, newsType, null, state);
		}
		return 0;
	}

	public static List<NewsInfo> getNewsListAndPraiseByResType(int actorId, int userId, int newsType, int start, int offset) {
		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
		if (newsService != null) {
			return newsService.getNewsListAndPraiseByNewsType(actorId, userId, newsType, start, offset);
		}
		return null;
	}

	public static Page<NewsInfo> getByTypeAndUserId(int type,Integer userId,Integer start, Integer num){
		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
		if (newsService != null) {
			Result<Page<NewsInfo>> result = newsService.getByTypeAndUserId(type,userId,start,num);
			if(result != null && result.getCode() != null && result.getCode().equals(CommonStateCode.SUCCESS)){
				return result.getData();
			}
			else {
				logger.error("【分页获取" + userId + "的"+type+"类型的动态失败】");
			}
		}
		return null;
	}

	public static Page<NewsInfo> getHotAudioNews(Integer start, Integer num){
		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
		if (newsService != null) {
			Result<Page<NewsInfo>> result = newsService.getHotAudioNews(start,num);
			if(result != null && result.getCode() != null && result.getCode().equals(CommonStateCode.SUCCESS)){
				return result.getData();
			}
			else {
				logger.error("【分页获取热门音频动态失败】start="+start+"，num="+num);
			}
		}
		return null;
	}

	public static boolean addNewsMediaPlay(Integer userId, Integer newsId){
		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
		if (newsService != null) {
			Result<Integer> result = newsService.addNewsMediaPlay(userId,newsId);
			if(result != null && result.getCode() != null && result.getCode().equals(CommonStateCode.SUCCESS)){
				return result.getData() > 0;
			}
			else {
				logger.error("【新增播放次数失败】userId="+userId+"，newsId="+newsId);
			}
		}
		return false;
	}

	public static boolean editNews(NewsInfo newsInfo){
		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
		if (newsService != null) {
			Result<Boolean> result = newsService.editNews(newsInfo);
			if(result != null && result.getCode() != null && result.getCode().equals(CommonStateCode.SUCCESS)){
				return result.getData();
			}
			else {
				logger.error("【更新动态失败】"+ new Gson().toJson(newsInfo));
			}
		}
		return false;
	}

	public static boolean isAudioWhiteUser(int userId) {
		UserProfile userProfile = UserService.getUserInfoNew(userId);
		if(userProfile != null && userProfile.getIsActor() == 1){
			UserVerifyService userVerifyService = (UserVerifyService)MelotBeanFactory.getBean("userVerifyService");
			Result<UserVerifyDO> result = userVerifyService.getUserVerifyDO(userId);
			if(result != null && result.getCode() != null && result.getCode().equals(CommonStateCode.SUCCESS)){
				UserVerifyDO userVerifyDO = result.getData();
				if(userVerifyDO!=null&&userVerifyDO.getVerifyStatus() == 2){
					return true;
				}
			}
		}

//		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
//		if (newsService != null) {
//			Result<Boolean> result = newsService.isAudioWhiteUser(userId);
//			if(result != null && result.getCode() != null && result.getCode().equals(CommonStateCode.SUCCESS)){
//				return result.getData();
//			}
//			else {
//				logger.error("【判断是否是音频动态白名单失败】userId="+userId);
//			}
//		}
		return false;
	}

	public static List<NewsInfo> getVideoHall(int appId, int start, int offset) {
		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
		if (newsService != null) {
			Result<List<NewsInfo>> result = newsService.getVideoHall(appId,start,offset);
			if(result != null && result.getCode() != null && result.getCode().equals(CommonStateCode.SUCCESS)){
				return result.getData();
			}
			else {
				logger.error("【分页获取视频大厅失败】appId="+appId+",start="+start+"，offset="+offset);
			}
		}
		return null;
	}

	public static List<NewsTopic> getTopicHall(int appId, int start, int offset) {
		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
		if (newsService != null) {
			Result<List<NewsTopic>> result = newsService.getHallTopicListForApp(appId,start,offset);
			if(result != null && result.getCode() != null && result.getCode().equals(CommonStateCode.SUCCESS)){
				return result.getData();
			}
			else {
				logger.error("【分页获取话题大厅失败】appId="+appId+",start="+start+"，offset="+offset);
			}
		}
		return null;
	}

	public static List<NewsInfo> getNewsListsByTopicId(int topicId, int start, int offset) {
		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
		if (newsService != null) {
			Result<List<NewsInfo>> result = newsService.getNewsListsByTopicId(topicId,start,offset);
			if(result != null && result.getCode() != null && result.getCode().equals(CommonStateCode.SUCCESS)){
				return result.getData();
			}
			else {
				logger.error("【获取话题关联视频失败】topicId="+topicId+",start="+start+"，offset="+offset);
			}
		}
		return null;
	}

	public static List<NewsTopic> getHotTopicList(int appId, int start, int offset) {
		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
		if (newsService != null) {
			Result<List<NewsTopic>> result = newsService.getHotTopicListForApp(appId,start,offset);
			if(result != null && result.getCode() != null && result.getCode().equals(CommonStateCode.SUCCESS)){
				return result.getData();
			}
			else {
				logger.error("【分页获取热门话题失败】appId="+appId+",start="+start+"，offset="+offset);
			}
		}
		return null;
	}

	public static NewsTopic getTopicByTopicId(int topicId) {
		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
		if (newsService != null) {
			Result<NewsTopic> result = newsService.getNewsTopicById(topicId);
			if(result != null && result.getCode() != null && result.getCode().equals(CommonStateCode.SUCCESS)){
				return result.getData();
			}
			else {
				logger.error("【获取话题信息失败】topicId="+topicId);
			}
		}
		return null;
	}

	public static NewsTopic getTopicByContent(int appId,String content) {
		com.melot.news.service.NewsService newsService = (com.melot.news.service.NewsService) MelotBeanFactory.getBean("newsCenter");
		if (newsService != null) {
			Result<NewsTopic> result = newsService.getTopicByAppIdAndContent(appId,content);
			if(result != null && result.getCode() != null && result.getCode().equals(CommonStateCode.SUCCESS)){
				return result.getData();
			}
			else {
				logger.error("【获取话题信息失败】appId="+appId+",content="+content);
			}
		}
		return null;
	}


}

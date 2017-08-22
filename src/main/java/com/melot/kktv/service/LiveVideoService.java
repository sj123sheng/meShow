package com.melot.kktv.service;

import java.util.List;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.melot.module.ModuleService;
import com.melot.opus.domain.HistUserNewsAppreciate;
import com.melot.opus.domain.TempUserResource;
import com.melot.opus.domain.UserNews;
import com.melot.opus.domain.UserNewsFolder;
import com.melot.opus.service.VideoService;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * 
 * Title: 七牛视频相关服务
 * <p>
 * Description: 
 * </p>
 * 
 * @author 冯占飞<a href="mailto:zhanfei.feng@melot.cn">
 * @version V1.0
 * @since 2015年3月6日 上午9:57:06
 */
public class LiveVideoService {
	
	private static Logger logger = Logger.getLogger(LiveVideoService.class);
	
	/**
	 * 更新作品（动态）观看次数
	 * @param newsId 作品（动态）Id
	 * @return
	 */
	public static boolean updateNewsVidewTimes(int newsId) {
		try {
			VideoService videoService = (VideoService) MelotBeanFactory.getBean("videoService");
			if (videoService == null) {
				logger.error("NewsService.updateNewsVidewTimes exception(videoService is null), newsId : " + newsId);
				return false;
			}
			return videoService.updateNewsViewTimes(newsId);
		} catch (Exception e) {
			logger.error("LiveVideoService.updateNewsVidewTimes exception, newsId : " + newsId, e);
		}
		
		return false;
	} 
	
	/**
	 * 获取用户作品总数
	 * @param userId 用户Id
	 * @param appId 渠道Id 
	 * @param resType 资源类型1：作品
	 * @param checkTag 是否本人
	 * @return
	 */
	public static int getOpusCount (int userId, int appId, int resType, boolean checkTag) {
		try {
			if (userId <= 0 || appId <= 0 || resType <= 0) {
				return 0;
			}
			VideoService videoService = (VideoService) MelotBeanFactory.getBean("videoService");
			return videoService.getUserNewsFolderCount(userId, appId, resType, checkTag);
		} catch (Exception e) {
			logger.error("LiveVideoService.getOpusCount exception, userId : " + userId
					+ " ,appId : " + appId
					+ " ,resType : " + resType
					+ " ,checkTag :" + checkTag,e);
		}
		
		return 0;
	}
	
	/**
	 * 获取用户作品列表
	 * @param userId 用户Id
	 * @param appId 渠道Id
	 * @param resType 资源类型
	 * @param pageIndex 起始页
	 * @param countPerPage 每页显示条数
	 * @param checkTag 是否本人
	 * @return
	 */
	@Deprecated
	public static List<UserNews> getOpusList (int userId, int appId, int resType, int pageIndex, int countPerPage, boolean checkTag) {
		if (userId <= 0 || appId <= 0 || resType <= 0) {
			return null;
		}
		try {
		    VideoService videoService = (VideoService) MelotBeanFactory.getBean("videoService");
			return videoService.getUserNewsFolderList(userId, appId, resType, pageIndex, countPerPage, checkTag);
		} catch (Exception e) {
			logger.error("LiveVideoService.getOpusList exception, userId : " + userId
					+ " ,appId : " + appId
					+ " ,resType : " + resType
					+ " ,pageIndex : " + pageIndex
					+ " ,countPerPage : " + countPerPage
					+ " ,checkTag :" + checkTag,e);
		}
		return null;
	}

    /**
     * 发布动态
     * @param userNews
     * @return
     */
    public static int addNews(UserNews userNews) {
        try {
            VideoService videoService = (VideoService) MelotBeanFactory.getBean("videoService");
            if (videoService != null) {
                return videoService.addNewsAndGetNewsId(userNews);
            } else {
                logger.error("LiveVideoService.addNews exception (videoService is null) !!!");
            }
        } catch (Exception e) {
            logger.error("LiveVideoService.addNews exception (get VideoService exception), userNews : " + new Gson().toJson(userNews), e);
        }
        
        return 0;
    }
    
    /**
     * 添加动态至作品表
     * @return
     */
    public static boolean addNewsToNewsFolder(UserNewsFolder userNewsFolder) {
        if (userNewsFolder == null || userNewsFolder.getAppId() < 0 ||
                userNewsFolder.getNewsId() < 0 || userNewsFolder.getUserId() < 0) {
            logger.error("LiveVideoService.addNewsToNewsFolder exception, userNewsFolder : " + new Gson().toJson(userNewsFolder));
            return false;
        }
        try {
            VideoService videoService = (VideoService) MelotBeanFactory.getBean("videoService");
            if (videoService != null) {
                return videoService.addNewsToNewsFolder(userNewsFolder);
            } else {
                logger.error("LiveVideoService.addNewsToNewsFolder exception (videoService is null)");
            }
        } catch (Exception e) {
            logger.error("LiveVideoService.addNewsToNewsFolder exception(get VideoService exception), userNewsFolder : " + new Gson().toJson(userNewsFolder), e);
        }
        
        return false;
    }
    
    /**
     * 获取作品总数
     * @param userId 用户Id
     * @param appId 渠道Id
     * @param resType 资源类型 1：作品
     * @param isSelf 是否本人调用
     * @return
     */
    public static int getUserOpusCount(int userId, int appId, int resType, boolean isSelf){
        if (userId <= 0 || appId < 0 || resType < 0) {
            return 0;
        }
        try {
            VideoService videoService = (VideoService) MelotBeanFactory.getBean("videoService");
            if (videoService != null) {
                return videoService.getUserNewsFolderCount(userId, appId, resType, isSelf);
            } else {
                logger.error("LiveVideoService.getUserOpusCount exception (videoService is null)");
            }
        } catch (Exception e) {
            logger.error("LiveVideoService.getUserOpusCount exception (get VideoService exception), userId : " + userId
                    + " ,appId : " + appId
                    + " ,resType : " + resType
                    + " ,isSelf : " + isSelf, e);
        }
        
        return 0;
    }
    
    /**
     * 获取用户审核通过的作品
     * @param userId 用户Id
     * @param appId 渠道Id
     * @param resType 资源类型 1：作品
     * @return
     */
    public static int getCheckedUserOpusCount(int userId, int appId, int resType) {
        if (userId <= 0 || appId < 0 || resType < 0) {
            return 0;
        }
        try {
            VideoService videoService = (VideoService) MelotBeanFactory.getBean("videoService");
            if (videoService != null) {
                return videoService.getCheckedUserNewsFolderCount(userId, appId, resType);
            } else {
                logger.error("LiveVideoService.getCheckedUserOpusCount exception (videoService is null)");
            }
        } catch (Exception e) {
            logger.error("LiveVideoService.getCheckedUserOpusCount exception (get VideoService exception), userId : " + userId
                    + " ,appId : " + appId
                    + " ,resType : " + resType, e);
        }
       
        return 0;
    }
    
    /**
     * 获取作品列表
     * @param userId 用户Id
     * @param appId 渠道Id
     * @param resType 资源类型 1：作品
     * @param pageIndex 起始页
     * @param countPerPage 结束页
     * @param isSelf 是否本人调用
     * @return
     */
    public static List<UserNews> getUserOpus(int userId, int appId, int resType, int pageIndex, int countPerPage, boolean isSelf, int sortType){
        if (userId <= 0 || appId < 0) {
            return null;
        }
        try {
            VideoService videoService = (VideoService) MelotBeanFactory.getBean("videoService");
            if (videoService != null) {
                return videoService.getUserNewsFolderListBySortType(userId, appId, resType, pageIndex, countPerPage, isSelf, sortType);
            } else {
                logger.error("LiveVideoService.getUserOpus exception (videoService is null)");
            }
        } catch (Exception e) {
            logger.error("LiveVideoService.getUserOpus exception (get VideoService exception), userId : " + userId
                    + " ,appId : " + appId
                    + " ,resType : " + resType
                    + " ,pageIndex : " + pageIndex
                    + " ,countPerPage : " + countPerPage
                    + " ,isSelf : " + isSelf, e);
        }
        
         return null;
    }
    
    /**
     * 查看作品信息
     * @param newsId 动态Id
     * @return
     */
    @SuppressWarnings("deprecation")
    public static UserNews getUserOpusById(int newsId) {
        if (newsId <= 0) {
            return null;
        }
        try {
            VideoService videoService = (VideoService) MelotBeanFactory.getBean("videoService");
            if (videoService != null) {
                return videoService.getNewsInfoById(newsId);
            } else {
                logger.error("LiveVideoService.getUserOpusById exception (videoService is null)");
            }
        } catch (Exception e) {
            logger.error("LiveVideoService.getUserOpusById exception (get VideoService exception), newsId : " + newsId, e);
        }
        
        return null;
    }
    
    /**
     * 查看作品打赏列表
     * @param newsId 动态Id
     * @param count 查看条数
     * @return
     */
    public static List<UserNews> getNewsRewardInfo(int newsId, int count) {
        if (newsId <= 0 || count <= 0) {
            return null;
        }
        try {
            VideoService videoService = (VideoService) MelotBeanFactory.getBean("videoService");
            if (videoService != null) {
                return videoService.getNewsRewardInfo(newsId, count);
            } else {
                logger.error("LiveVideoService.getNewsRewardInfo exception (videoService is null)");
            }
        } catch (Exception e) {
            logger.error("LiveVideoService.getNewsRewardInfo exception (get VideoService exception), newsId : " + newsId
                    + " ,count : " + count, e);
        }
        
        return null;
    }
    
    /**
     * 获取作品打赏总数
     * @param newsId 动态Id
     * @return
     */
    public static int getRewardCount (int newsId){
        if (newsId < 0) {
            logger.error("LiveVideoService.getRewardCount exception, newsId : " + newsId);
            return 0;
        }
        try {
            VideoService videoService = (VideoService) MelotBeanFactory.getBean("videoService");
            if (videoService != null) {
                return videoService.getNewsRewardCount(newsId);
            } else {
                logger.error("LiveVideoService.getRewardCount exception (videoService is null)");
            }
        } catch (Exception e) {
            logger.error("LiveVideoService.getRewardCount exception (get VideoService exception), newsId : " + newsId, e);
        }
        
        return 0;
    }
    
    /**
     * 更新作品查看次数
     * @param newsId
     * @return
     */
    public static boolean updateNewsViewTimes(int newsId) {
        if (newsId < 0) {
            logger.error("LiveVideoService.updateNewsViewTimes exception, newsId : " + newsId);
            return false;
        }
        try {
            VideoService videoService = (VideoService) MelotBeanFactory.getBean("videoService");
            if (videoService != null) {
                return videoService.updateNewsViewTimes(newsId);
            } else {
                logger.error("LiveVideoService.updateNewsViewTimes exception (videoService is null)");
            }
        } catch (Exception e) {
            logger.error("LiveVideoService.updateNewsViewTimes exception (get VideoService exception), newsId : " + newsId, e);
        }
        
        return false;
    }
    
    /**
     * 删除用户作品
     * @param newsId 动态Id
     * @param userId 用户Id
     * @param appId appId
     * @param resType 资源类型1：作品，2：热拍
     * @return
     */
    public static boolean delUserNewsFolder(int newsId, int userId, int appId, int resType) {
        if (newsId <= 0 || userId <= 0 || appId < 0 || resType < 0) {
            logger.error("LiveVideoService.delUserNewsFolder exception, newsId : " + newsId
                    + " ,userId : " + userId
                    + " ,appId : " + appId
                    + " ,resType : " + resType);
            
            return false;
        }
        try {
            VideoService videoService = (VideoService) MelotBeanFactory.getBean("videoService");
            if (videoService != null) {
                return videoService.delNewsFolder(newsId, userId, appId, resType);
            } else {
                logger.error("LiveVideoService.delUserNewsFolder exception (videoService is null)");
            }
        } catch (Exception e) {
            logger.error("LiveVideoService.delUserNewsFolder exception(get VideoService exception), newsId : " + newsId
                    + " ,userId : " + userId
                    + " ,appId : " + appId
                    + " ,resType : " + resType, e);
        }
        
        return false;
    }
    
    /**
     * 更新用户动态
     * @param userNews
     */
    public static boolean updateUserNews(UserNews userNews){
        if (userNews == null || userNews.getUserId() == null 
                || userNews.getUserId().intValue() <= 0 
                || userNews.getNewsId() == null
                || userNews.getNewsId().intValue() <= 0) {
            logger.error("LiveVideoService.updateUserNews exception, userNews : " + new Gson().toJson(userNews));
            return false;
        }
        try {
            VideoService videoService = (VideoService) MelotBeanFactory.getBean("videoService");
            if (videoService != null) {
                return videoService.updateUserNews(userNews);
            } else {
                logger.error("LiveVideoService.updateUserNews exception (videoService is null)");
            }
        } catch (Exception e) {
            logger.error("LiveVideoService.updateUserNews exception(get VideoService exception), userNews : " + new Gson().toJson(userNews), e);
        }
        
        return false;
    }
    
    /**
     * 更新用户作品
     * @param userNewsFolder
     */
    public static boolean updateUserNewsFolder(UserNewsFolder userNewsFolder) {
        if (userNewsFolder == null || userNewsFolder.getNewsId() == null 
                || userNewsFolder.getNewsId().intValue() < 0
                || userNewsFolder.getUserId() == null
                || userNewsFolder.getUserId().intValue() <= 0) {
            logger.error("LiveVideoService.updateUserNewsFolder exception, userNewsFolder : " + new Gson().toJson(userNewsFolder));
            
            return false;
        }
        try {
            VideoService videoService = (VideoService) MelotBeanFactory.getBean("videoService");
            if (videoService != null) {
                return videoService.updateUserNewsFolder(userNewsFolder);
            } else {
                logger.error("LiveVideoService.updateUserNewsFolder exception (videoService is null)");
            }
        } catch (Exception e) {
            logger.error("LiveVideoService.updateUserNewsFolder exception(get VideoService exception), userNewsFolder : " + new Gson().toJson(userNewsFolder), e);
        }
        
        return false;
    }
    
    /**
     * 获取视频点赞状态
     * @param userId
     * @param newsId
     * @return
     */
    public static boolean getUserAppreciateNewsState(int userId, int newsId) {
        if (userId <= 0 || newsId <= 0) {
            return true;
        }
        
        try {
            VideoService videoService = (VideoService) MelotBeanFactory.getBean("videoService");
            if (videoService != null) {
                return videoService.getUserAppreciateNewsState(userId, newsId);
            } else {
                logger.error("LiveVideoService.getUserAppreciateNewsState exception (videoService is null)");
            }
        } catch (Exception e) {
             logger.error("LiveVideoService.getUserAppreciateNewsState exception(get VideoService exception), userId : " + userId
                     + " newsId : " + newsId, e);
        }
        
        return true;
    }
    
    /**
     * 添加点赞记录
     * @param histUserNewsAppreciate
     * @return
     */
    public static boolean appreciateUserNews(HistUserNewsAppreciate histUserNewsAppreciate) {
        if (histUserNewsAppreciate == null
                || histUserNewsAppreciate.getNewsId() == null
                     || histUserNewsAppreciate.getUserId() == null) {
            return false;
        }
        
        try {
            VideoService videoService = (VideoService) MelotBeanFactory.getBean("videoService");
            if (videoService != null) {
                return videoService.appreciateUserNews(histUserNewsAppreciate);
            } else {
                logger.error("LiveVideoService.appreciateUserNews exception (videoService is null)");
            }
        } catch (Exception e) {
            logger.error("LiveVideoService.appreciateUserNews exception(get VideoService exception), HistUserNewsAppreciate : " + new Gson().toJson(histUserNewsAppreciate), e);
        }
        
        return false;
    }
    
    /**
     * 更新动态审核状态
     * @param userId 用户Id
     * @param newsId 动态Id
     * @param checkId 审核人Id
     * @param state 审核状态
     * @return
     */
    public static boolean updateNewsState(int userId, int newsId, int checkId, int state) {
        if (newsId <= 0 || state > 9 || state < 0) {
            return false;
        }
        try {
            VideoService videoService = (VideoService) MelotBeanFactory.getBean("videoService");
            if (videoService != null) {
                return videoService.updateNewsState(userId, newsId, checkId, state);
            } else {
                logger.error("LiveVideoService.updateNewsState exception (videoService is null)");
            }
        } catch (Exception e) {
            logger.error("LiveVideoService.updateNewsState exception, userId : " + userId
                    + " newsId : " + newsId
                    + " checkId : " + checkId
                    + " state : " + state, e);
        }
        return false;
    }
    
    /**
     * 获取用户临时资源信息
     * @param id
     * @param userId
     * @param status
     * @return
     */
    public static TempUserResource getTempUserResourceById(Integer id, Integer userId, Integer resType, Integer resId, Integer status) {
        try {
            VideoService videoService = (VideoService) MelotBeanFactory.getBean("videoService");
            if (videoService != null) {
                return videoService.getTempUserResourceById(id, userId, resType, resId, status);
            } else {
                logger.error("LiveVideoService.getTempUserResourceById exception (videoService is null)");
            }
        } catch (Exception e) {
            logger.error("LiveVideoService.getTempUserResourceById exception, userId : " + userId
                    + " id : " + id
                    + " userId : " + userId
                    + " status : " + status, e);
        }
        return null;
    }
    
    /**
     * 添加临时资源表
     * @param tempUserResource
     * @return
     */
    public static int addTempUserResource(TempUserResource tempUserResource) {
        try {
            VideoService videoService = (VideoService) MelotBeanFactory.getBean("videoService");
            if (videoService != null) {
               return videoService.addTempUserResource(tempUserResource);
            } else {
                logger.error("LiveVideoService.addTempUserResource exception (videoService is null)");
            }
        } catch (Exception e) {
            logger.error("LiveVideoService.addTempUserResource exception, tempUserResource : " 
                            + new Gson().toJson(tempUserResource), e);
        }
        return 0;
    }
    
    /**
     * 获取用户临时资源列表
     * @param userId
     * @param resType
     * @param status
     * @return
     */
    public static List<TempUserResource> getTempUserResByUserId(int userId, Integer resType, Integer status) {
        try {
            VideoService videoService = (VideoService) MelotBeanFactory.getBean("videoService");
            if (videoService != null) {
               return videoService.getTempUserResByUserId(userId, resType, status);
            } else {
                logger.error("LiveVideoService.getTempUserResByUserId exception (videoService is null)");
            }
        } catch (Exception e) {
            logger.error("LiveVideoService.getTempUserResByUserId exception, userId : " 
                            + userId
                            + " ,resType : " + resType
                            + " ,status :" + status, e);
        }
        return null;
    }
    
    /**
     * 删除用户临时资源文件
     * @param id
     * @param userId
     * @param status
     */
    public static void delTempUserResourceById(Integer id, Integer userId, Integer resType, Integer resId, Integer status) {
        try {
            VideoService videoService = (VideoService) MelotBeanFactory.getBean("videoService");
            if (videoService != null) {
               videoService.delTempUserResourceById(id, userId, resType, resId, status);
            } else {
                logger.error("LiveVideoService.delTempUserResourceById exception (videoService is null)");
            }
        } catch (Exception e) {
            logger.error("LiveVideoService.delTempUserResourceById exception, id : " 
                            + id
                            + " ,userId : " + userId
                            + " ,status :" + status, e);
        }
    }
    
    /**
     * 检测是否有头像审核
     * @param userId
     * @return
     */
    public static boolean checkingPortrait(int userId) {
        try {
            VideoService videoService = (VideoService) MelotBeanFactory.getBean("videoService");
            if (videoService != null) {
               return videoService.checkingPortrait(userId);
            } else {
                logger.error("LiveVideoService.delTempUserResourceById exception (videoService is null)");
            }
        } catch (Exception e) {
            logger.error("LiveVideoService.delTempUserResourceById exception, userId : " + userId, e);
        }
        return false;
    }
}

package com.melot.kktv.service;

import com.google.common.collect.Lists;
import com.melot.kk.opus.api.domain.TempUserResource;
import com.melot.kk.opus.api.domain.UserPicture;
import com.melot.kk.opus.api.service.VideoService;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.melot.sdk.core.util.MelotBeanFactory;

import java.util.List;

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
     * 检测是否有头像审核
     * @param userId
     * @return
     */
    public static boolean checkingPortrait(int userId) {
        try {
            VideoService videoService = (VideoService) MelotBeanFactory.getBean("videoService");
            if (videoService != null) {
               return videoService.checkingPortrait(userId);
            }
        } catch (Exception e) {
            logger.error("videoService.checkingPortrait execute exception, userId : " + userId, e);
        }
        return false;
    }

    public static boolean delPicture(int photoId) {
        try {
            VideoService videoService = (VideoService) MelotBeanFactory.getBean("videoService");
            if (videoService != null) {
                return videoService.deletePicture(Lists.newArrayList(photoId));
            }
        } catch (Exception e) {
            logger.error("videoService.delPicture execute exception, photoId : " + photoId, e);
        }
        return false;
    }

    public static int getPictureCount(int userId) {
        try {
            VideoService videoService = (VideoService) MelotBeanFactory.getBean("videoService");
            if (videoService != null) {
                return videoService.getPictureCount(userId);
            }
        } catch (Exception e) {
            logger.error("videoService.getPictureCount execute exception, userId : " + userId, e);
        }
        return 0;
    }

    public static List<UserPicture> getPictureList(int userId,int start,int offset) {
        try {
            VideoService videoService = (VideoService) MelotBeanFactory.getBean("videoService");
            if (videoService != null) {
                return videoService.getPictureList(userId,start,offset);
            }
        } catch (Exception e) {
            logger.error("videoService.getPictureCount execute exception, userId : " + userId, e);
        }
        return null;
    }

    public static boolean addPicture(int userId, int pictureType, String path_original, String pictureName) {
        try {
            VideoService videoService = (VideoService) MelotBeanFactory.getBean("videoService");
            if (videoService != null) {
                return videoService.addPicture(userId,pictureType,path_original,pictureName);
            }
        } catch (Exception e) {
            logger.error("videoService.addPicture execute exception.", e);
        }
        return false;
    }

    public static boolean addPictureV2(int resId,int userId, int pictureType, String path_original, String pictureName) {
        try {
            VideoService videoService = (VideoService) MelotBeanFactory.getBean("videoService");
            if (videoService != null) {
                return videoService.addPictureV2(resId,userId,pictureType,path_original,pictureName);
            }
        } catch (Exception e) {
            logger.error("videoService.addPictureV2 execute exception.", e);
        }
        return false;
    }

    public static boolean addVideoTape(int userId, String path_original, String videoName) {
        try {
            VideoService videoService = (VideoService) MelotBeanFactory.getBean("videoService");
            if (videoService != null) {
                return videoService.addVideoTape(userId,path_original,videoName);
            }
        } catch (Exception e) {
            logger.error("videoService.addVideoTape execute exception.", e);
        }
        return false;
    }




}

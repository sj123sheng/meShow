package com.melot.kktv.service;

import com.melot.kk.opus.api.domain.TempUserResource;
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
            }
        } catch (Exception e) {
            logger.error("videoService.checkingPortrait execute exception, userId : " + userId, e);
        }
        return false;
    }
}

package com.melot.kktv.action;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import com.melot.kk.module.resource.domain.QiNiuTokenConf;
import com.melot.kk.module.resource.service.ResourceNewService;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.game.config.sdk.barrage.service.VideoBarrageInfoService;
import com.melot.game.config.sdk.domain.VideoBarrageInfo;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcx.service.GeneralService;
import com.melot.kkcx.transform.NewsTF;
import com.melot.kkgame.redis.BangVideoSource;
import com.melot.kktv.service.LiveVideoService;
import com.melot.kktv.service.UserService;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.CommonUtil.ErrorGetParameterException;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.Constant;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.kktv.util.db.DB;
import com.melot.kktv.util.db.SqlMapClientHelper;
import com.melot.qiniu.common.QiniuService;
import com.melot.sdk.core.util.MelotBeanFactory;
import com.melot.video.driver.domain.VideoInfo;
import com.melot.video.driver.service.VideoInfoService;

/**
 * 
 * Title:
 * <p>
 * Description: 用户作品Service
 * </p>
 * 
 * @author 冯占飞<a href="mailto:zhanfei.feng@melot.cn">
 * @version V1.0
 * @since 2015年4月3日 下午5:10:37
 */
public class VideoFunctions {
	
	private static Logger logger = Logger.getLogger(VideoFunctions.class);
	
	private BangVideoSource bangVideoSource;
	
	public void setBangVideoSource(BangVideoSource bangVideoSource) {
		this.bangVideoSource = bangVideoSource;
	}

	/**
     * 获取七牛上传Token (20002050)
     * @param jsonObject
     * @param checkTag
     * @return
     */
    @SuppressWarnings("unused")
    public JsonObject getUploadTokenNew(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        // 定义返回结果
        JsonObject result = new JsonObject();
        
        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        int userId, resType, newsType, resumeUp, mimeType, vframeSeconds, appId = AppIdEnum.GAME, transcoding, opusState, newsState;
        String videoTitle = null, videoContent = null, uuid;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            resumeUp = CommonUtil.getJsonParamInt(jsonObject, "resumeUp", 0, null, 1, Integer.MAX_VALUE);
            mimeType = CommonUtil.getJsonParamInt(jsonObject, "mimeType", 0, null, 1, Integer.MAX_VALUE);
            videoContent = CommonUtil.getJsonParamString(jsonObject, "videoContent", null, null, 1, 500);
            vframeSeconds = CommonUtil.getJsonParamInt(jsonObject, "vframeSeconds", 0, null, 1, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
            //original = CommonUtil.getJsonParamInt(jsonObject, "original", 1, null, 1, Integer.MAX_VALUE);
            if (ConfigHelper.getMoreAppFlag() == 0) {
                appId = CommonUtil.getJsonParamInt(jsonObject, "a", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
                if (!GeneralService.isLegalAppId(appId)) {
                    throw new ErrorGetParameterException(TagCodeEnum.APPID_MISSING);
                }
            }
            videoTitle = CommonUtil.getJsonParamString(jsonObject, "videoTitle", null, null, 1, 50);
            transcoding = CommonUtil.getJsonParamInt(jsonObject, "transcoding", 0, null, 0, 1);
            uuid = CommonUtil.getJsonParamString(jsonObject, "uuid", null, null, 1, 40);
            opusState = CommonUtil.getJsonParamInt(jsonObject, "opusState", 0, null, 1, 9);
            newsState = CommonUtil.getJsonParamInt(jsonObject, "newsState", 0, null, 1, 9);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        String key = QiniuService.getFileName(appId, userId, null);
        try {
            QiNiuTokenConf qiNiuTokenConf = new QiNiuTokenConf();
            qiNiuTokenConf.setUserId(userId);
            qiNiuTokenConf.setKey(key);
            qiNiuTokenConf.setVframeSeconds(vframeSeconds);
            qiNiuTokenConf.setAppId(appId);
            qiNiuTokenConf.setMimeType(mimeType);
            qiNiuTokenConf.setResumeUp(resumeUp);
            if (videoTitle != null) {
                qiNiuTokenConf.setVideoTitle(videoTitle);
            }
            if (videoContent != null) {
                qiNiuTokenConf.setVideoContent(videoContent);
            }
            qiNiuTokenConf.setFunctag(20002051);
            if (!StringUtil.strIsNull(ConfigHelper.getKkApiAddress())) {
                qiNiuTokenConf.setApiAddress(ConfigHelper.getKkApiAddress());
            }

            qiNiuTokenConf.setNewsState(newsState);
            qiNiuTokenConf.setOpusState(opusState);
            qiNiuTokenConf.setTranscoding(transcoding);
            if (!StringUtil.strIsNull(uuid)) {
                qiNiuTokenConf.setUuid(uuid);
            }
            String token = getQinuUploadToken(qiNiuTokenConf);
            if (token != null && !token.trim().isEmpty()) {
                result.addProperty("upToken", token);
                result.addProperty("key", key);
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            } else {
                result.addProperty("TagCode", TagCodeEnum.GET_UPLOAD_TOKEN_FAIL);   
            }
        } catch (Exception e) {
            logger.error("GameVideoAction.getUploadToken error , userId : " + userId, e);
            result.addProperty("TagCode", TagCodeEnum.GET_UPLOAD_TOKEN_FAIL);   
        }
        
        return result;
    }
    
	/**
	 * 上传作品七牛回调接口(20002051)
	 * @param jsonObject
	 * @return
	 */
	@SuppressWarnings("unused")
    public JsonObject callbackForUploadVideoNew(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
		// 定义所需参数
		int userId = 0, newsType, appId = AppIdEnum.AMUSEMENT, resType, newsState, opusState;
		long videoDuration;
		String videoCover = null, videoTitle = null, videoContent = null, videoSource = null, key = null, mimeType, uuid;
		QiniuService qiniuService = new QiniuService(ConfigHelper.getAccessKey(), ConfigHelper.getSecretKey());
		// 定义返回结果
		JsonObject result = new JsonObject();
		// 解析参数
		try{
			String contentType = request.getHeader("content-type");
		    contentType = contentType == null? "application/x-www-form-urlencoded" : contentType;
		    String sign = qiniuService.sign(ConfigHelper.getKkApiAddress(), contentType, "parameter=" + request.getParameter("parameter"));
		    String authorization = "QBox " + sign;
            String sign1 = qiniuService.sign(
                    ConfigHelper.getKkApiAddress(),
                    contentType,
                    "parameter=" + request.getParameter("parameter")
                                    .replace("video/", "video%2F")
                                    .replace("image/", "image%2F")
                                    .replace("audio/", "audio%2F")
                                    .replace("application/", "application%2F"));
		    String authorization1 = "QBox " + sign1;
		    key = CommonUtil.getJsonParamString(jsonObject, "key", null, TagCodeEnum.GET_KEY_FAIL, 1, 30);
		    videoSource = key;
		    videoCover = key.replace(".mp4", ".jpg");
		    if (authorization.equals(request.getHeader("authorization")) || authorization1.equals(request.getHeader("authorization"))) {
//		        logger.error("success ===" + request.getParameter("parameter") + "=====" + contentType + "=====" + request.getHeader("authorization") + "=====" + sign + "=====" + authorization + "=====" + sign1 + "=====" + authorization1);
		    } else {
		        // 删除上传的文件
		        qiniuService.deleteFile(ConfigHelper.getBucket(), key);
		        qiniuService.deleteFile(ConfigHelper.getBucket(), videoCover);
//			    logger.error("failed ===" + request.getParameter("parameter") + "=====" + contentType + "=====" + request.getHeader("authorization") + "=====" + sign + "=====" + authorization + "=====" + sign1 + "=====" + authorization1);
			    result.addProperty("TagCode", TagCodeEnum.UNCATCHED_EXCEPTION);
			    return result;
		    }
		    userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
			mimeType = CommonUtil.getJsonParamString(jsonObject, "mimeType", null, null, 1, 40);
			opusState = CommonUtil.getJsonParamInt(jsonObject, "opusState", 0, null, 1, 9);
            newsState = CommonUtil.getJsonParamInt(jsonObject, "newsState", 0, null, 1, 9);
			uuid = CommonUtil.getJsonParamString(jsonObject, "uuid", null, null, 1, 40);
			if (mimeType.startsWith("image")) {
			    // 上传封面不做任何操作
			    result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            }
			String durationStr = CommonUtil.getJsonParamString(jsonObject, "duration", null, null, 1, Integer.MAX_VALUE);
			if (durationStr == null) {
			    videoDuration = qiniuService.getVideoDuration(ConfigHelper.getDomainURL(), videoSource);
            } else {
                videoDuration = ((Double) StringUtil.parseFromStr(durationStr, 0.00)).longValue();
            }
			if (videoDuration < 1) {
			    qiniuService.deleteFile(ConfigHelper.getBucket(), key);
                qiniuService.deleteFile(ConfigHelper.getBucket(), videoCover);
			    throw new ErrorGetParameterException(TagCodeEnum.GET_DURATION_FAIL);
            }
			videoContent = CommonUtil.getJsonParamString(jsonObject, "videoContent", null, null, 1, 500);
			newsType = CommonUtil.getJsonParamInt(jsonObject, "newsType", 0, null, 1, Integer.MAX_VALUE);
			if (ConfigHelper.getMoreAppFlag() == 0) {
				appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 1, Integer.MAX_VALUE);
				if (!GeneralService.isLegalAppId(appId)) {
				     qiniuService.deleteFile(ConfigHelper.getBucket(), key);
	                 qiniuService.deleteFile(ConfigHelper.getBucket(), videoCover);
					 throw new ErrorGetParameterException(TagCodeEnum.APPID_MISSING);
				}
			}
			
			videoTitle = CommonUtil.getJsonParamString(jsonObject, "videoTitle", null, null, 1, 40);

			// 过滤敏感字符或短连接
			videoTitle = GeneralService.replaceSensitiveWords(userId, videoTitle);

            /*UserNewsFolder userNewsFolder = new UserNewsFolder();
            userNewsFolder.setAppId(appId);
            userNewsFolder.setNewsId(0);
            userNewsFolder.setState(0);
            userNewsFolder.setUserId(userId);
            if (!StringUtil.strIsNull(uuid)) {
                userNewsFolder.setUuid(uuid);
            }
            // 添加用户作品
            LiveVideoService.addNewsToNewsFolder(userNewsFolder);*/
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            result.addProperty("key", key);
            result.addProperty("newsId", 0);
            return result;
		} catch(CommonUtil.ErrorGetParameterException e) {
		    qiniuService.deleteFile(ConfigHelper.getBucket(), key);
            qiniuService.deleteFile(ConfigHelper.getBucket(), videoCover);
			result.addProperty("TagCode", e.getErrCode());
			logger.error("VideoFunctions.callbackForUploadVideos exception, userId : " + userId
					+ " ,videoSource : " + videoSource
					+ " ,videoCover : " + videoCover
					+ " ,TagCode :" + e.getErrCode(), e);
			return result;
		} catch(Exception e) {
		    qiniuService.deleteFile(ConfigHelper.getBucket(), key);
            qiniuService.deleteFile(ConfigHelper.getBucket(), videoCover);
			result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
			logger.error("VideoFunctions.callbackForUploadVideos exception, userId : " + userId
					+ " ,videoSource : " + videoSource
					+ " ,videoCover : " + videoCover, e);
            return result;
		}
	}
	
	/**
	 * 获取老棒视频列表(10002056)
	 * @param jsonObject
	 * @param checkTag
	 * @param request
	 * @return
	 */
	public JsonObject getVideoListByUserId(JsonObject jsonObject,boolean checkTag, HttpServletRequest request){
		JsonObject result = new JsonObject();
		
		int actorId = 0;
		try {
			actorId = CommonUtil.getJsonParamInt(jsonObject, "actorId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
        	logger.error(e);
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
        	logger.error(e);
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
		
    	VideoInfoService videoInfoService = (VideoInfoService) MelotBeanFactory.getBean("videoInfoService");
		List<VideoInfo> vodeoInfoList = videoInfoService.getVideoListByActorId(actorId);
		JsonArray videoListArray = new JsonArray();
		if(vodeoInfoList != null && vodeoInfoList.size() > 0){
			videoListArray = videoInfos2Json(videoListArray,vodeoInfoList);
		}
		result.add("videoInfoList", videoListArray);
		result.addProperty("videoCount", vodeoInfoList.size());
		result.addProperty("TagCode", TagCodeEnum.SUCCESS);
		return result;
	}
	
	/**
	 * 获取老棒视频详情(10002057)
	 * @param jsonObject
	 * @param checkTag
	 * @param request
	 * @return
	 */
	public JsonObject getVideoDetailByVideoId(JsonObject jsonObject,boolean checkTag, HttpServletRequest request){
		JsonObject result = new JsonObject();
		
		Integer videoId = null;
		int barrageTag;
		try{
            videoId = CommonUtil.getJsonParamInt(jsonObject, "videoId", 0, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
            barrageTag = CommonUtil.getJsonParamInt(jsonObject, "barrageTag", 0, null, 0, Integer.MAX_VALUE);
        }catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }
		
		VideoInfoService videoInfoService = (VideoInfoService) MelotBeanFactory.getBean("videoInfoService");
		List<VideoInfo> vodeoInfoList = videoInfoService.getVideoListByIds(String.valueOf(videoId));
		if (vodeoInfoList != null && vodeoInfoList.size() == 1){
			result.addProperty("fileName", vodeoInfoList.get(0).getFileName());
			result.addProperty("fileUrl", vodeoInfoList.get(0).getFileUrl());

        	if (bangVideoSource.getViewCount(videoId) != null) {
        		Long expireTime = bangVideoSource.getExpireTime(videoId);
        		result.addProperty("viewCount", bangVideoSource.getViewCount(videoId));
        		bangVideoSource.setViewCount(videoId, bangVideoSource.getViewCount(videoId) + 1);
        		bangVideoSource.setExpireTime(videoId, expireTime.intValue());
        		
        	} else {
        		Random random=new Random();
        		int viewCount = (random.nextInt(5) + 1) * 30 + random.nextInt(10);
        		result.addProperty("viewCount", viewCount);
        		bangVideoSource.setViewCount(videoId, viewCount);
        		bangVideoSource.setExpireTime(videoId, 1209600);
        	}
        	result.addProperty("screenType", 2);
        
            //选取海报
            String postUrl = getPosterUrl(vodeoInfoList.get(0).getActorId(), vodeoInfoList.get(0).getStartTime(), vodeoInfoList.get(0).getEndTime());
            if (postUrl != null){
            	result.addProperty("posterUrl", postUrl);
            }
			result.addProperty("actorId", vodeoInfoList.get(0).getActorId());
			UserProfile userProfile = UserService.getUserInfoV2(vodeoInfoList.get(0).getActorId());
			if (userProfile != null) {
				result.addProperty("nickname", userProfile.getNickName());
				if (userProfile.getPortrait() != null){
					result.addProperty("portrait", ConfigHelper.getHttpdir() + userProfile.getPortrait());
				}
			}
		} else {
			result.addProperty("TagCode", "30002504");
			return result;
		}
		
		if (barrageTag != 1) {
			try {
				VideoBarrageInfoService videoBarrageInfoService = MelotBeanFactory.getBean("videoBarrageInfoService", VideoBarrageInfoService.class);
				List<VideoBarrageInfo> list = videoBarrageInfoService.getVideoBarrageInfosByVideoId(videoId);
				JsonArray jsonArray = new JsonArray();
				for (VideoBarrageInfo videoBarrageInfo : list) {
					JsonObject json = new JsonObject();
					json.addProperty("userId", videoBarrageInfo.getUploadUserId());
					UserProfile userProfile = UserService.getUserInfoV2(vodeoInfoList.get(0).getActorId());
					if (userProfile != null) {
						json.addProperty("nickname", userProfile.getNickName());
					}
					json.addProperty("time", videoBarrageInfo.getPointInTime());
					json.addProperty("msg", videoBarrageInfo.getBarrageMsg());
					jsonArray.add(json);
				}
				result.add("chatList", jsonArray);
				int barrageCount = getBarrageCount(videoId);
				result.addProperty("barrageCount", barrageCount);
				result.addProperty("TagCode", TagCodeEnum.SUCCESS);
	        } catch (Exception e) {
                logger.error("Fail to call videoBarrageInfoService.getVideoBarrageInfosByVideoId ", e);
                result.addProperty("TagCode", "20001006");
            }
		} else {
			int barrageCount = getBarrageCount(videoId);
			result.addProperty("barrageCount", barrageCount);
		}
		
		return result;
	}
	
	/**
	 * 获取弹幕数量
	 * 
	 * @param videoId
	 * @return
	 */
	private static int getBarrageCount(int videoId) {
		try {
			return (Integer) SqlMapClientHelper.getInstance(com.melot.kktv.util.DBEnum.KKGAME_PG).queryForObject("UserGamble.getBarrageCount", videoId);
		} catch (SQLException e) {
			logger.error(e);
		}
		return 0;
	}
	
	private JsonArray videoInfos2Json(JsonArray videoListArray, List<VideoInfo> videoInfoList){
        JsonObject oneVideoInfo;
        for (VideoInfo video: videoInfoList) {
        	oneVideoInfo = new JsonObject();
        	if(video.getVideoId() != null) {
            	oneVideoInfo.addProperty("videoId", video.getVideoId());
            }
            if(video.getActorId() != null) {
            	oneVideoInfo.addProperty("actorId", video.getActorId());
            }
            if(video.getCdnType() != null) {
            	oneVideoInfo.addProperty("cdnType", video.getCdnType());
            }
            oneVideoInfo.addProperty("duration", video.getDuration());
            
            //选取海报
            String postUrl = getPosterUrl(video.getActorId(), video.getStartTime(), video.getEndTime());
            if (postUrl != null) {
            	 oneVideoInfo.addProperty("posterUrl", postUrl);
            }

        	if (bangVideoSource.getViewCount(video.getVideoId()) != null) {
        		oneVideoInfo.addProperty("viewCount", bangVideoSource.getViewCount(video.getVideoId()));
        	} else {
        		Random random=new Random();
        		int viewCount = (random.nextInt(5) + 1) * 30 + random.nextInt(10);
        		oneVideoInfo.addProperty("viewCount", viewCount);
        		bangVideoSource.setViewCount(video.getVideoId(), viewCount);
        		bangVideoSource.setExpireTime(video.getVideoId(), 1209600);
        	}
    		oneVideoInfo.addProperty("screenType", 2);    
            
            if (video.getFileName() != null) {
            	oneVideoInfo.addProperty("fileName", video.getFileName());
            }
            if ( video.getFileUrl() != null) {
            	oneVideoInfo.addProperty("fileUrl", video.getFileUrl());
            }
            if (video.getPersistentId() != null) {
            	oneVideoInfo.addProperty("persistentId", video.getPersistentId());
            }
            if ( video.getStartTime() != null) {
            	oneVideoInfo.addProperty("startTime", video.getStartTime().getTime());
            }
            if ( video.getEndTime() != null) {
            	oneVideoInfo.addProperty("endTime", video.getEndTime().getTime());
            }
            if (video.getUpdateTime() != null) {
            	oneVideoInfo.addProperty("persistentId", video.getUpdateTime().getTime());
            }
            videoListArray.add(oneVideoInfo);
        }
      return videoListArray;
	}
	
	private String getPosterUrl(int actorId, Date startTime, Date endTime) {
		String postUrl = null;
		try {
			Map<Object, Object> map = new HashMap<Object, Object>();
			map.put("actorId", actorId);
			map.put("startTime", startTime);
			map.put("endTime", endTime);
			postUrl = (String) SqlMapClientHelper.getInstance(DB.SLAVE_PG).queryForObject("Other.getPosterUrl", map);
		} catch (SQLException e) {
			logger.error(e);
			return null;
		}
		return postUrl;
	}
	
    /**
     * 获取七牛上传视频token
     * @param qiNiuTokenConf
     * @return
     */
    private static String getQinuUploadToken(QiNiuTokenConf qiNiuTokenConf) {
        ResourceNewService resourceNewService = (ResourceNewService)MelotBeanFactory.getBean("resourceNewService");
        if (resourceNewService != null) {
            return resourceNewService.getUpLoadTokenByDomain(qiNiuTokenConf);
        }
        return null;
    }
    
}

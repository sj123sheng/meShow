/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.action;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.kkgame.model.transform.UserNewsTF;
import com.melot.kktv.service.LiveVideoService;
import com.melot.kktv.service.RoomService;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.Constant;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.opus.domain.HistUserNewsAppreciate;
import com.melot.opus.domain.UserNews;
import com.melot.opus.domain.UserNewsFolder;

/**
 * 
 * Title: 游戏视频相关接口
 * <p>
 * Description: 
 * </p>
 * 
 * @author 冯占飞<a href="mailto:zhanfei.feng@melot.cn">
 * @version V1.0
 * @since 2015年3月17日 上午11:06:08
 */
public class GameVideoAction {

    private static Logger logger = Logger.getLogger(GameVideoAction.class);
    
    /**
     * 获取游戏离线视频列表 (20030001)
     * @param jsonObject
     * @param checkTag
     * @return
     */
     public JsonObject getGameVideoList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request){
         
         // 定义所需参数
         int userId, pageIndex, platform, resType, sortType, 
         countPerPage = Constant.return_video_count, appId = AppIdEnum.GAME;
         // 定义返回结果
         JsonObject result = new JsonObject();
         // 解析参数
         try {
             userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
             appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.GAME, null, 0, Integer.MAX_VALUE);
             pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
             resType = CommonUtil.getJsonParamInt(jsonObject, "resType", 1, null, 1, Integer.MAX_VALUE);
             platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, 1, Integer.MAX_VALUE);
             sortType = CommonUtil.getJsonParamInt(jsonObject, "sortType", 1, null, 1, Integer.MAX_VALUE);
             countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", Constant.return_video_count, null, 1, Integer.MAX_VALUE);
         } catch (CommonUtil.ErrorGetParameterException e) {
             result.addProperty("TagCode", e.getErrCode());
             return result;
         }
         
         // 定义返回结果
         int totalCount = 0;
         JsonArray jsonArray = new JsonArray();
         // 只有游戏主播才会返回
         RoomInfo roomInfo = RoomService.getRoomInfo(userId);
         if (roomInfo != null && roomInfo.getType() != null && roomInfo.getType() == AppIdEnum.GAME) {
             totalCount = LiveVideoService.getUserOpusCount(userId, appId, resType, checkTag);
             if (totalCount > 0) {
                 List<UserNews> userNewsFolerList = LiveVideoService.getUserOpus(userId, appId, resType, pageIndex, countPerPage, checkTag, sortType);
                 if (userNewsFolerList != null && userNewsFolerList.size() > 0) {
                     for (UserNews userNews : userNewsFolerList) {
                         JsonObject obj = UserNewsTF.toOPusJsonObject(userNews, platform);
                         if (obj != null) {
                             jsonArray.add(obj);
                         }
                     }
                 }
             }
         }
         result.addProperty("totalCount", totalCount);
         result.add("videoList", jsonArray);
         result.addProperty("pathPrefix", ConfigHelper.getVideoURL());
         result.addProperty("TagCode", TagCodeEnum.SUCCESS);

         return result;
     }
     
     /**
      * （更新）作品 20030002
      * @param jsonObject
      * @param checkTag
      * @return
      */
     public JsonObject saveGameVideo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request){
         
         // 定义所需参数
         int userId, newsId, original;
         String videoTitle, videoContent;
         // 定义返回结果
         JsonObject result = new JsonObject();
         // 解析参数
         try {
             userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
             newsId = CommonUtil.getJsonParamInt(jsonObject, "newsId", 0, TagCodeEnum.GET_NEWSID_FAIL, 1, Integer.MAX_VALUE);
             original = CommonUtil.getJsonParamInt(jsonObject, "original", 1, null, 1, Integer.MAX_VALUE);
             videoTitle = CommonUtil.getJsonParamString(jsonObject, "videoTitle", null, TagCodeEnum.GET_VIDEOTITLE_FAIL, 1, 50);
             videoContent = CommonUtil.getJsonParamString(jsonObject, "videoContent", null, null, 1, 500);
         } catch (CommonUtil.ErrorGetParameterException e) {
             result.addProperty("TagCode", e.getErrCode());
             return result;
         }
         
         // 更新user_news
         UserNews userNews = new UserNews();
         userNews.setUserId(userId);
         userNews.setNewsId(newsId);
         if (!StringUtil.strIsNull(videoTitle)) {
             userNews.setMediaTitle(videoTitle);
         }
         if (!StringUtil.strIsNull(videoContent)) {
             userNews.setContent(videoContent);
         }
         if (!LiveVideoService.updateUserNews(userNews)) {
             logger.error("GameVideoAction.saveGameVideo(LiveVideoService.updateUserNews(userNews)) exception, userNews : " 
                     + new Gson().toJson(userNews));
         }
         
         // 更新user_news_folder
         UserNewsFolder userNewsFolder = new UserNewsFolder();
         userNewsFolder.setUserId(userId);
         userNewsFolder.setNewsId(newsId);
         userNewsFolder.setOriginal(original);
         if (!LiveVideoService.updateUserNewsFolder(userNewsFolder)) {
             logger.error("GameVideoAction.saveGameVideo(LiveVideoService.updateUserNewsFolder(userNewsFolder)) exception, userNewsFolder : " 
                     + new Gson().toJson(userNewsFolder));

         }
         
         result.addProperty("TagCode", TagCodeEnum.SUCCESS);
         return result;
         
     }
 
     /**
      * 根据Id 获取作品详细信息 (20030003)
      * @param jsonObject
      * @param checkTag
      * @return
      */
     public JsonObject getGameVideoInfoById(JsonObject jsonObject, boolean checkTag, HttpServletRequest request){
         
         // 定义所需参数
         int userId, newsId, platform, appId = AppIdEnum.GAME;
         // 定义返回结果
         JsonObject result = new JsonObject();
         // 解析参数
         try {
             userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
             newsId = CommonUtil.getJsonParamInt(jsonObject, "newsId", 0, TagCodeEnum.GET_NEWSID_FAIL, 1, Integer.MAX_VALUE);
             appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.GAME, null, 0, Integer.MAX_VALUE);
             platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, 1, Integer.MAX_VALUE);
         } catch (CommonUtil.ErrorGetParameterException e) {
             result.addProperty("TagCode", e.getErrCode());
             return result;
         }
         
         UserNews userNews = LiveVideoService.getUserOpusById(newsId);
         if (userNews != null && appId == userNews.getAppId().intValue()) {
             int state = userNews.getOpusState();
            
             /**
              *  资源审核状态1:未审核,2:审核通过,3:审核不通过,4:用户删除,5:官方录制,6:后台删除
              *  token不通过只能查看审核通过的, token验证通过则需判断是不是本人的作品
              */ 
            if ((state != 4 && state != 6)
                    && ((state == 2) || (checkTag && userNews
                            .getUserId().intValue() == userId))) {
                result = UserNewsTF.toOPusJsonObject(userNews, platform);
                if (userId <= 1 || !LiveVideoService.getUserAppreciateNewsState(userId, newsId)) {
                    result.addProperty("appreciateState", 0);
                } else {
                    result.addProperty("appreciateState", 1);
                }
                result.addProperty("userId", userNews.getUserId());
                result.addProperty("state", userNews.getOpusState());
                result.addProperty("pathPrefix", ConfigHelper.getVideoURL());
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            } else {
                result.addProperty("TagCode", TagCodeEnum.GET_USERVIDEO_FAIL);
            }
         } else {
             result.addProperty("TagCode", TagCodeEnum.GET_USERVIDEO_FAIL);
         }
         
         return result;
     }
     
     /**
      * 游戏视频点赞 (20030004)
      * @param jsonObject
      * @param checkTag
      * @return
      */
     public JsonObject appreciateUserNewsById(JsonObject jsonObject, boolean checkTag, HttpServletRequest request){
         // 该接口需要验证token,未验证的返回错误码
         if (!checkTag) {
             JsonObject result = new JsonObject();
             result.addProperty("TagCode", TagCodeEnum.TOKEN_INCORRECT);
             return result;
         }
         // 定义所需参数
         int userId, newsId, platform;
         // 定义返回结果
         JsonObject result = new JsonObject();
         // 解析参数
         try {
             userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
             newsId = CommonUtil.getJsonParamInt(jsonObject, "newsId", 0, TagCodeEnum.GET_NEWSID_FAIL, 1, Integer.MAX_VALUE);
             platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, 1, Integer.MAX_VALUE);
         } catch (CommonUtil.ErrorGetParameterException e) {
             result.addProperty("TagCode", e.getErrCode());
             return result;
         }
         // 查询点赞状态
         if (!LiveVideoService.getUserAppreciateNewsState(userId, newsId)) {
             HistUserNewsAppreciate histUserNewsAppreciate = new HistUserNewsAppreciate(); 
             histUserNewsAppreciate.setDtime(new Date());
             histUserNewsAppreciate.setNewsId(newsId);
             histUserNewsAppreciate.setPlatform(platform);
             histUserNewsAppreciate.setUserId(userId);
             if (LiveVideoService.appreciateUserNews(histUserNewsAppreciate)) {
                 result.addProperty("TagCode", TagCodeEnum.SUCCESS);
             } else {
                 result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
             }
         } else {
             result.addProperty("TagCode", TagCodeEnum.NEWS_ALREDY_APPRECIATE);
         }
         
         return result;
     }
     
}

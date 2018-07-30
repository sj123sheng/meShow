package com.melot.kktv.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.melot.kkcore.user.api.UserRegistry;
import com.melot.news.model.NewsTopic;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.melot.api.menu.sdk.dao.domain.RoomInfo;
import com.melot.kk.module.resource.constant.ECloudTypeConstant;
import com.melot.kk.module.resource.constant.FileTypeConstant;
import com.melot.kk.module.resource.constant.ResTypeConstant;
import com.melot.kk.module.resource.constant.ResourceStateConstant;
import com.melot.kk.module.resource.domain.Resource;
import com.melot.kk.module.resource.service.ResourceNewService;
import com.melot.kk.opus.api.constant.OpusCostantEnum;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcx.service.GeneralService;
import com.melot.kkcx.service.RoomService;
import com.melot.kkcx.service.UserService;
import com.melot.kktv.base.CommonStateCode;
import com.melot.kktv.base.Page;
import com.melot.kktv.base.Result;
import com.melot.kktv.redis.NewsSource;
import com.melot.kktv.redis.NewsV2Source;
import com.melot.kktv.service.ConfigService;
import com.melot.kktv.service.NewsService;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.CommonUtil.ErrorGetParameterException;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.Constant;
import com.melot.kktv.util.NewsMediaTypeEnum;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.SecurityFunctions;
import com.melot.kktv.util.StringUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.news.domain.NewsCommentHist;
import com.melot.news.model.NewsInfo;

import redis.clients.jedis.Tuple;

public class NewsV2Functions {

    private static Logger logger = Logger.getLogger(NewsV2Functions.class);

    private static String SEPARATOR = "/";

    @javax.annotation.Resource
    ResourceNewService resourceNewService;

    @Autowired
    private ConfigService configService;
    /**
     * 发布动态(20006002)
     *
     * @param jsonObject
     *            请求对象
     * @param checkTag
     *            是否验证token标记
     * @return 结果字符串
     */
    public JsonObject addNews(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        // 验证参数
        int userId, mediaType, appId, platform;
        @SuppressWarnings("unused")
        String content = null, mediaUrl = null,mediaMd5 = null, imageUrl = null,imageMd5 = null, newsTitle = null, topic = null;
        Integer mediaDur = null, resType = null;
        try {
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, null, 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "06020001", 1, Integer.MAX_VALUE);
            // 媒体类型
            mediaType = CommonUtil.getJsonParamInt(jsonObject, "mediaType", 0, null, 1, Integer.MAX_VALUE);
            content = CommonUtil.getJsonParamString(jsonObject, "content", null, null, 1, 500);
            if (content != null) {
                if (CommonUtil.matchXSSTag(content)) {
                    throw new CommonUtil.ErrorGetParameterException("06020007");
                }
                content = GeneralService.replaceSensitiveWords(userId, content);
            }

            mediaUrl = CommonUtil.getJsonParamString(jsonObject, "mediaUrl", null, null, 1, Integer.MAX_VALUE);
            if (!StringUtil.strIsNull(mediaUrl)) {
                mediaUrl = mediaUrl.replaceFirst(ConfigHelper.getMediahttpdir(), "");
                mediaUrl = mediaUrl.replaceFirst("/kktv", "");
            }
            mediaMd5 = CommonUtil.getJsonParamString(jsonObject, "mediaMd5", null, null, 1, Integer.MAX_VALUE);

            imageUrl = CommonUtil.getJsonParamString(jsonObject, "imageUrl", null, null, 1, Integer.MAX_VALUE);
            imageMd5 = CommonUtil.getJsonParamString(jsonObject, "imageMd5", null, null, 1, Integer.MAX_VALUE);

            mediaDur = CommonUtil.getJsonParamInt(jsonObject, "mediaDur", -1, null, 1, Integer.MAX_VALUE);
            if (mediaDur > 7200) {
                mediaDur = mediaDur / 1000;
            } else if (mediaDur == -1) {
                mediaDur = 0;
            }

            newsTitle = CommonUtil.getJsonParamString(jsonObject, "newsTitle", null, null, 1, 40);
            if (newsTitle != null) {
                if (CommonUtil.matchXSSTag(newsTitle)) {
                    throw new CommonUtil.ErrorGetParameterException("06020007");
                }
                newsTitle = GeneralService.replaceSensitiveWords(userId, newsTitle);
            }
            resType = CommonUtil.getJsonParamInt(jsonObject, "newsType", 8, null, 1, Integer.MAX_VALUE);

            topic = CommonUtil.getJsonParamString(jsonObject, "topic", null, null, 1, 10);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        //特殊时期修改接口停用
        if (configService.getIsSpecialTime()) {
            if (!UserService.checkUserIdentify(userId)) {
                result.addProperty("TagCode", TagCodeEnum.FUNCTAG_UNUSED_EXCEPTION);
                return result;
            }
        }
        // 不是主播不可发动态
        /*
         * UserProfile userProfile = com.melot.kktv.service.UserService
         * .getUserInfoV2(userId); if (userProfile == null ||
         * userProfile.getIsActor() == 0) { result.addProperty("TagCode",
         * TagCodeEnum.FUNCTAG_UNUSED_EXCEPTION); return result; }
         */

        if(mediaType == NewsMediaTypeEnum.AUDIO){
            boolean flag = NewsService.isAudioWhiteUser(userId);
            if(!flag){
                result.addProperty("TagCode", "06020011");
                return result;
            }
        }

        NewsInfo newsInfo = new NewsInfo();
        newsInfo.setUserId(userId);
        newsInfo.setNewsType(resType);
        newsInfo.setPublishedTime(new Date());
        if (content != null) {
            newsInfo.setContent(content);
        }
        if (newsTitle != null) {
            newsInfo.setNewsTitle(newsTitle);
        }
        newsInfo.setPlatform(platform);
        newsInfo.setAppId(appId);
        // 校验媒体类型
        if (mediaType > NewsMediaTypeEnum.VIDEO) {
            result.addProperty("TagCode", "06020010");
            return result;
        }

        if (mediaType == NewsMediaTypeEnum.VIDEO) {
            Resource resource = new Resource();
            resource.setState(ResourceStateConstant.uncheck);
            resource.setMimeType(FileTypeConstant.video);
            resource.setSpecificUrl(mediaUrl);
            resource.setUserId(userId);
            resource.setDuration(Long.valueOf(mediaDur));
            resource.setResType(ResTypeConstant.resource);
            if(!StringUtil.strIsNull(mediaMd5)){
                resource.setMd5(mediaMd5);
            }
            // 获取分辨率,添加分辨率信息
            VideoInfo videoInfo = getVideoInfoByHttp(mediaUrl);
            if (videoInfo != null) {
                resource.setFileHeight(videoInfo.getHeight());
                resource.setFileWidth(videoInfo.getWidth());
            }
            if (!StringUtil.strIsNull(imageUrl)) {
                imageUrl = imageUrl.replaceFirst(ConfigHelper.getHttpdir(), "");
                if(!imageUrl.startsWith(SEPARATOR)) {
                    imageUrl = SEPARATOR + imageUrl;
                }
                imageUrl = imageUrl.replaceFirst("/kktv", "");
            }
            resource.seteCloudType(ECloudTypeConstant.qiniu);
            resource.setImageUrl(imageUrl != null ? imageUrl : Pattern.compile("mp4$").matcher(mediaUrl).replaceAll("jpg"));
            Result<Integer> resIdResult = resourceNewService.addResource(resource);
            if(resIdResult != null && resIdResult.getCode() != null && resIdResult.getCode().equals(CommonStateCode.SUCCESS)){
                Integer resId = resIdResult.getData();
                if (resId > 0) {
                    newsInfo.setRefVideo(String.valueOf(resId));
                } else {
                    // 插入资源失败
                    result.addProperty("TagCode", "06020009");
                    return result;
                }
            }else {
                // 插入资源失败
                result.addProperty("TagCode", "06020009");
                return result;
            }

        } else if(mediaType == NewsMediaTypeEnum.IMAGE){
            // multi picture
            String[] imageList = imageUrl.split(",");
            String[] md5List = new String[imageList.length];
            if(!StringUtil.strIsNull(imageMd5)){
                md5List = imageMd5.split(",");
            }
            List<Resource> resourceList = new ArrayList<Resource>();
            for (int i = 0;i<imageList.length;i++) {
                String tempUrl = imageList[i];
                if(!StringUtil.strIsNull(tempUrl)){
                    Resource resource = new Resource();
                    resource.setState(ResourceStateConstant.uncheck);
                    resource.setMimeType(FileTypeConstant.image);
                    resource.setResType(ResTypeConstant.resource);
                    resource.seteCloudType(ECloudTypeConstant.aliyun);
                    resource.setUserId(userId);
                    if(md5List.length >= i+1){
                        resource.setMd5(md5List[i]);
                    }
                    if (!StringUtil.strIsNull(tempUrl)) {
                        tempUrl = tempUrl.replaceFirst(ConfigHelper.getHttpdir(), "");
                        if(!imageUrl.startsWith(SEPARATOR)) {
                            tempUrl = SEPARATOR + tempUrl;
                        }
                        tempUrl = tempUrl.replaceFirst("/kktv", "");
                    }
                    resource.setImageUrl(tempUrl);
                    resourceList.add(resource);
                }
            }
            Result<List<Integer>> resIdsResult = resourceNewService.addResources(resourceList);
            if(resIdsResult != null && resIdsResult.getCode() != null && resIdsResult.getCode().equals(CommonStateCode.SUCCESS)){
                String resIds = "";
                for (Integer i : resIdsResult.getData()) {
                    resIds = resIds + "," + i;
                }
                resIds =  Pattern.compile("^,*").matcher(resIds).replaceAll("");
                if (resIds != null) {
                    newsInfo.setRefImage(resIds);
                } else {
                    // 插入资源失败
                    result.addProperty("TagCode", "06020009");
                    return result;
                }
            }
            else {
                // 插入资源失败
                result.addProperty("TagCode", "06020009");
                return result;
            }

        }
        else if(mediaType == NewsMediaTypeEnum.AUDIO){
            Resource audio = new Resource();
            audio.setState(ResourceStateConstant.uncheck);
            audio.setMimeType(FileTypeConstant.audio);
            audio.setSpecificUrl(mediaUrl);
            audio.setUserId(userId);
            audio.setDuration(Long.valueOf(mediaDur));
            audio.setResType(ResTypeConstant.resource);
            audio.seteCloudType(ECloudTypeConstant.qiniu);
            if(!StringUtil.strIsNull(mediaMd5)){
                audio.setMd5(mediaMd5);
            }
            Result<Integer> audioResult = resourceNewService.addResource(audio);
            if(audioResult != null && audioResult.getCode() != null && audioResult.getCode().equals(CommonStateCode.SUCCESS)){
                Integer resId = audioResult.getData();
                if (resId > 0) {
                    newsInfo.setRefAudio(String.valueOf(resId));
                } else {
                    // 插入资源失败
                    result.addProperty("TagCode", "06020009");
                    return result;
                }
            }else {
                // 插入资源失败
                result.addProperty("TagCode", "06020009");
                return result;
            }
            if (!StringUtil.strIsNull(imageUrl)) {
                Resource resource = new Resource();
                resource.setState(ResourceStateConstant.uncheck);
                resource.setMimeType(FileTypeConstant.image);
                resource.setResType(ResTypeConstant.resource);
                resource.seteCloudType(ECloudTypeConstant.aliyun);
                resource.setUserId(userId);
                imageUrl = imageUrl.replaceFirst(ConfigHelper.getHttpdir(), "");
                if(!imageUrl.startsWith(SEPARATOR)) {
                    imageUrl = SEPARATOR + imageUrl;
                }
                imageUrl = imageUrl.replaceFirst("/kktv", "");
                resource.setImageUrl(imageUrl);
                if(!StringUtil.strIsNull(imageMd5)){
                    resource.setMd5(imageMd5);
                }
                Result<Integer> imageResult = resourceNewService.addResource(resource);
                if(imageResult != null && imageResult.getCode() != null && imageResult.getCode().equals(CommonStateCode.SUCCESS)){
                    Integer resId = imageResult.getData();
                    if (resId > 0) {
                        newsInfo.setRefImage(String.valueOf(resId));
                    } else {
                        // 插入资源失败
                        result.addProperty("TagCode", "06020009");
                        return result;
                    }
                }else {
                    // 插入资源失败
                    result.addProperty("TagCode", "06020009");
                    return result;
                }
            }
        }

        if (NewsService.isWhiteUser(userId)) {
            newsInfo.setState(1);
            if(mediaType == NewsMediaTypeEnum.AUDIO && !StringUtil.strIsNull(newsInfo.getRefAudio())){
                resourceNewService.checkResource(Lists.newArrayList(Integer.parseInt(newsInfo.getRefAudio()),Integer.parseInt(newsInfo.getRefImage())),ResourceStateConstant.checkpass,"动态白名单",1);
            }
            else if(mediaType == NewsMediaTypeEnum.VIDEO && !StringUtil.strIsNull(newsInfo.getRefVideo())){
                resourceNewService.checkResource(Lists.newArrayList(Integer.parseInt(newsInfo.getRefVideo())),ResourceStateConstant.checkpass,"动态白名单",1);
            }
            else if(mediaType == NewsMediaTypeEnum.IMAGE && !StringUtil.strIsNull(newsInfo.getRefImage())){
                List<Integer> resIds = Lists.newArrayList();
                for(String id:newsInfo.getRefImage().split(",")){
                    if(!StringUtil.strIsNull(id)){
                        resIds.add(Integer.parseInt(id));
                    }
                }
                resourceNewService.checkResource(resIds,ResourceStateConstant.checkpass,"动态白名单",1);
            }
        }

        int newsId = NewsService.addNews(newsInfo, topic);
        if (newsId > 0) {
            result.addProperty("newsId", newsId);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } else {
            result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
            return result;
        }
    }

    public JsonObject editNews(JsonObject jsonObject, boolean checkTag) throws Exception {
        String functag = "51100102";
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        int newsId,userId;
        String content = null, mediaUrl = null,mediaMd5=null,imageMd5=null, imageUrl = null, newsTitle = null;
        try {
            newsId = CommonUtil.getJsonParamInt(jsonObject, "newsId", 0, functag + "01", 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, functag + "02", 1, Integer.MAX_VALUE);
            content = CommonUtil.getJsonParamString(jsonObject, "content", null, null, 1, 500);
            if (content != null) {
                if (CommonUtil.matchXSSTag(content)) {
                    throw new CommonUtil.ErrorGetParameterException(functag+"03");
                }
                content = GeneralService.replaceSensitiveWords(userId, content);
            }
            mediaUrl = CommonUtil.getJsonParamString(jsonObject, "mediaUrl", null, null, 1, Integer.MAX_VALUE);
            imageUrl = CommonUtil.getJsonParamString(jsonObject, "imageUrl", null, null, 1, Integer.MAX_VALUE);
            newsTitle = CommonUtil.getJsonParamString(jsonObject, "newsTitle", null, null, 1, 40);
            if (newsTitle != null) {
                if (CommonUtil.matchXSSTag(newsTitle)) {
                    throw new CommonUtil.ErrorGetParameterException(functag+"03");
                }
                newsTitle = GeneralService.replaceSensitiveWords(userId, newsTitle);
            }
            mediaMd5 = CommonUtil.getJsonParamString(jsonObject, "mediaMd5", null, null, 1, Integer.MAX_VALUE);

            imageMd5 = CommonUtil.getJsonParamString(jsonObject, "imageMd5", null, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        NewsInfo oldNewsInfo = NewsService.getNewsInfoByNewsIdForState(newsId, 0);
        if (oldNewsInfo == null) {
            result.addProperty("TagCode", functag+"06");
            return result;
        }
        if (oldNewsInfo.getState() != 2) {
            result.addProperty("TagCode", functag+"07");
            return result;
        }
        if (oldNewsInfo.getUserId() != userId) {
            result.addProperty("TagCode", functag+"08");
            return result;
        }

        NewsInfo newsInfo = new NewsInfo();
        newsInfo.setNewsId(newsId);
        newsInfo.setUserId(userId);
        if (content != null) {
            newsInfo.setContent(content);
        }else{
            newsInfo.setContent("");
        }
        if (newsTitle != null) {
            newsInfo.setNewsTitle(newsTitle);
        }
        else {
            newsInfo.setNewsTitle("");
        }
        boolean needCheck = false;
        if(!StringUtil.strIsNull(mediaUrl) && !StringUtil.strIsNull(imageUrl)){
            needCheck = true;
        }
        if (!StringUtil.strIsNull(mediaUrl)) {
            Resource audio = new Resource();
            audio.setState(ResourceStateConstant.uncheck);
            audio.setMimeType(FileTypeConstant.audio);
            audio.setSpecificUrl(mediaUrl);
            audio.setUserId(userId);
            audio.setResType(ResTypeConstant.resource);
            audio.seteCloudType(ECloudTypeConstant.qiniu);
            if(!StringUtil.strIsNull(mediaMd5)){
                audio.setMd5(mediaMd5);
            }
            Result<Integer> audioResult = resourceNewService.addResource(audio);
            if(audioResult != null && audioResult.getCode() != null && audioResult.getCode().equals(CommonStateCode.SUCCESS)){
                Integer resId = audioResult.getData();
                if (resId > 0) {
                    newsInfo.setRefAudio(String.valueOf(resId));
                } else {
                    // 插入资源失败
                    result.addProperty("TagCode", "06020009");
                    return result;
                }
            }else {
                // 插入资源失败
                result.addProperty("TagCode", "06020009");
                return result;
            }
            if(!needCheck){
                Resource image = resourceNewService.getResourceById(Integer.parseInt(getRegexAdmin(oldNewsInfo.getRefImage()))).getData();
                if(image != null && (image.getState() == ResourceStateConstant.uncheck ||image.getState() == ResourceStateConstant.checkpass)){
                    needCheck = true;
                }
            }
        }
        if (!StringUtil.strIsNull(imageUrl)) {
            imageUrl = imageUrl.replaceFirst(ConfigHelper.getHttpdir(), "");
            if(!imageUrl.startsWith(SEPARATOR)) {
                imageUrl = SEPARATOR + imageUrl;
            }
            imageUrl = imageUrl.replaceFirst("/kktv", "");
            Resource resource = new Resource();
            resource.setState(ResourceStateConstant.uncheck);
            resource.setMimeType(FileTypeConstant.image);
            resource.setResType(ResTypeConstant.resource);
            resource.seteCloudType(ECloudTypeConstant.aliyun);
            resource.setUserId(userId);
            resource.setImageUrl(imageUrl);
            if(!StringUtil.strIsNull(imageMd5)){
                resource.setMd5(imageMd5);
            }
            Result<Integer> imageResult = resourceNewService.addResource(resource);
            if(imageResult != null && imageResult.getCode() != null && imageResult.getCode().equals(CommonStateCode.SUCCESS)){
                Integer resId = imageResult.getData();
                if (resId > 0) {
                    newsInfo.setRefImage(String.valueOf(resId));
                } else {
                    // 插入资源失败
                    result.addProperty("TagCode", functag + "04");
                    return result;
                }
            }else {
                // 插入资源失败
                result.addProperty("TagCode", functag + "04");
                return result;
            }
            if(!needCheck){
                Resource audio = resourceNewService.getResourceById(Integer.parseInt(getRegexAdmin(oldNewsInfo.getRefAudio()))).getData();
                if(audio != null && (audio.getState() == ResourceStateConstant.uncheck ||audio.getState() == ResourceStateConstant.checkpass)){
                    needCheck = true;
                }
            }
        }
        if(needCheck){
            newsInfo.setState(3);
        }
        else {
            newsInfo.setState(oldNewsInfo.getState());
        }
        boolean flag = NewsService.editNews(newsInfo);
        if(flag){
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        }
        else{
            result.addProperty("TagCode", functag + "05");
        }
        return result;

    }

    public JsonObject getMyAudioList(JsonObject jsonObject, boolean checkTag) throws Exception {
        String functag = "51100103";
        JsonObject result = new JsonObject();
//        if (!checkTag) {
//            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
//            return result;
//        }
        int userId,start,offset;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 1, functag +"01", 1, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
            offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 10, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
        } catch (ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }
        Page<NewsInfo> page = NewsService.getByTypeAndUserId(NewsMediaTypeEnum.AUDIO,userId,start,offset);
        if(page != null){
            result.addProperty("countTotal",page.getCount());
            List<NewsInfo> newsInfoList = page.getList();
            JsonArray jNewsList = new JsonArray();
            if(newsInfoList != null) {
                for (NewsInfo newsInfo : newsInfoList) {
                    JsonObject json = new JsonObject();
                    json.addProperty("newsId", newsInfo.getNewsId());
                    json.addProperty("content", newsInfo.getContent());
                    json.addProperty("newsTitle", newsInfo.getNewsTitle());
                    json.addProperty("state", newsInfo.getState());
                    json.addProperty("praiseNum", newsInfo.getNewsPraise());
                    json.addProperty("commentNum", newsInfo.getCommentNum());
                    json.addProperty("playTimes", newsInfo.getPlayNum());
                    String imageUrl = configService.getCheckUnpassPoster();
                    if (newsInfo.getState() == 1 && newsInfo.getRefImage() != null) {
                        int resId = Integer.valueOf(Pattern.compile("\\{|\\}").matcher(newsInfo.getRefImage()).replaceAll(""));
                        Result<Resource> imageResult = resourceNewService.getResourceById(resId);
                        if (imageResult != null && imageResult.getCode() != null && imageResult.getCode().equals(CommonStateCode.SUCCESS)) {
                            Resource image = imageResult.getData();
                            if (image != null) {
                                if (image.getState() == ResourceStateConstant.checkpass) {
                                    imageUrl = image.getImageUrl();
                                }
                            }
                        }
                    } else if (newsInfo.getState() == 3) {
                        imageUrl = ConfigHelper.getHttpdir() + OpusCostantEnum.CHECKING_NEWS_RESOURCEURL;
                    }
                    String path_1280 = imageUrl + "!1280";
                    String path_720 = imageUrl + "!720";
                    String path_400 = imageUrl + "!400";
                    String path_272 = imageUrl + "!272";
                    String path_128 = imageUrl + "!128x96";
                    json.addProperty("imageUrl_1280", path_1280);
                    json.addProperty("imageUrl_720", path_720);
                    json.addProperty("imageUrl_400", path_400);
                    json.addProperty("imageUrl_272", path_272);
                    json.addProperty("imageUrl_128", path_128);
                    json.addProperty("imageUrl", imageUrl);
                    jNewsList.add(json);
                }
            }
            result.add("newsList", jNewsList);
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir()); // 图片前缀
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        }
        else{
            result.addProperty("TagCode", functag + "02");
        }
        return result;
    }

    public JsonObject getPopularAudioList(JsonObject jsonObject, boolean checkTag) throws Exception {
        String functag = "51100105";
        JsonObject result = new JsonObject();
//        if (!checkTag) {
//            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
//            return result;
//        }
        int pageIndex,countPerPage;
        try {
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 10, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
        } catch (ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }
        Page<NewsInfo> page = NewsService.getHotAudioNews((pageIndex-1)*countPerPage,countPerPage);
        if(page != null){
            result.addProperty("countTotal",page.getCount());
            List<NewsInfo> newsInfoList = page.getList();
            JsonArray jNewsList = new JsonArray();
            if(newsInfoList != null){
                for(NewsInfo newsInfo:newsInfoList){
                    JsonObject json = new JsonObject();
                    json.addProperty("newsId",newsInfo.getNewsId());
                    json.addProperty("content",newsInfo.getContent());
                    json.addProperty("newsTitle",newsInfo.getNewsTitle());
                    json.addProperty("playTimes",newsInfo.getPlayNum());
                    String imageUrl = configService.getCheckUnpassPoster();
                    if(newsInfo.getRefImage() != null){
                        int resId = Integer.valueOf(Pattern.compile("\\{|\\}").matcher(newsInfo.getRefImage()).replaceAll(""));
                        Result<Resource> imageResult = resourceNewService.getResourceById(resId);
                        if(imageResult != null && imageResult.getCode() != null && imageResult.getCode().equals(CommonStateCode.SUCCESS)){
                            Resource image = imageResult.getData();
                            if(image != null){
                                imageUrl = image.getImageUrl();
                            }
                        }
                    }
                    String path_1280 = imageUrl + "!1280";
                    String path_720 = imageUrl + "!720";
                    String path_400 = imageUrl + "!400";
                    String path_272 = imageUrl + "!272";
                    String path_128 = imageUrl + "!128x96";
                    json.addProperty("imageUrl_1280", path_1280);
                    json.addProperty("imageUrl_720", path_720);
                    json.addProperty("imageUrl_400", path_400);
                    json.addProperty("imageUrl_272", path_272);
                    json.addProperty("imageUrl_128", path_128);
                    json.addProperty("imageUrl", imageUrl);
                    jNewsList.add(json);
                }
            }
            result.add("newsList", jNewsList);
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir()); // 图片前缀
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        }
        else{
            result.addProperty("TagCode", functag + "02");
        }
        return result;
    }


    public JsonObject addPlayTimes(JsonObject jsonObject, boolean checkTag) throws Exception {
        String functag = "52100104";
        JsonObject result = new JsonObject();
        JsonObject rtJO = null;
        try {
            rtJO = SecurityFunctions.checkSignedValue(jsonObject);
        } catch (Exception e) {
            logger.error("NewsV2Functions.addPlayTimes(" + "jsonObject:" + jsonObject + "checkTag:" + checkTag + ") execute exception.", e);
        }
        if(rtJO != null) {
            return rtJO;
        }

        int userId,newsId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
            newsId = CommonUtil.getJsonParamInt(jsonObject, "newsId", 0, functag+"01", Integer.MIN_VALUE, Integer.MAX_VALUE);
        } catch (ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        //如果是游客就不添加播放记录
        UserRegistry userRegistry = UserService.getUserRegistryInfo(userId);
        if(userRegistry == null||userRegistry.getOpenPlatform()==0||userRegistry.getOpenPlatform()==-5||userRegistry.getOpenPlatform()==-7){
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        }

        boolean flag = NewsService.addNewsMediaPlay(userId,newsId);
        if(flag){
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        }
        else{
            result.addProperty("TagCode", functag + "02");
        }
        return result;
    }

    /**
     * 获取大厅推荐话题列表（51100108）
     * @param jsonObject
     * @param checkTag
     * @return
     */
    public JsonObject getHallTopicList(JsonObject jsonObject, boolean checkTag) {
        JsonObject result = new JsonObject();
        // 定义所需参数
        int appId;
        // 解析参数
        try {
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
        } catch (ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }
        List<NewsTopic> topicList = NewsService.getTopicHall(appId,0,Integer.MAX_VALUE);
        JsonArray jsonArray = new JsonArray();
        for(NewsTopic newsTopic:topicList){
            JsonObject topic = new JsonObject();
            topic.addProperty("topicId",newsTopic.getTopicId());
            topic.addProperty("content",newsTopic.getContent());
            List<NewsInfo> newsList = NewsService.getNewsListsByTopicId(newsTopic.getTopicId(),0,4);
            JsonArray newsJsonArray = new JsonArray();
            for(NewsInfo newsInfo:newsList){
                JsonObject news = new JsonObject();
                news.addProperty("newsId",newsInfo.getNewsId());
                news.addProperty("userId",newsInfo.getUserId());
                news.addProperty("praiseNum",newsInfo.getNewsPraise());
                RoomInfo actorInfo = RoomService.getRoomInfo(newsInfo.getUserId());
                if (actorInfo != null) {
                    news.addProperty("nickname", actorInfo.getNickname());
                }
                if(newsInfo.getRefVideo()!=null){
                    int resId = Integer.valueOf(Pattern.compile("\\{|\\}").matcher(newsInfo.getRefVideo()).replaceAll(""));
                    Resource resVideo = resourceNewService.getResourceById(resId).getData();
                    if(resVideo != null){
                        news.addProperty("imageUrl", resVideo.getImageUrl());
                        news.addProperty("mediaUrl", resVideo.getSpecificUrl());
                    }
                }
                newsJsonArray.add(news);
            }
            topic.add("newsList",newsJsonArray);
            jsonArray.add(topic);
        }
        result.add("topicList", jsonArray);
        result.addProperty("pathPrefix", ConfigHelper.getHttpdir()); // 图片前缀
        result.addProperty("mediaPathPrefix", ConfigHelper.getMediahttpdir()); // 多媒体前缀
        result.addProperty("videoPathPrefix", ConfigHelper.getVideoURL());// 七牛前缀
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    public JsonObject checkAudioWhiteUser(JsonObject jsonObject, boolean checkTag) throws Exception {
        String functag = "51100106";
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, functag+"01", Integer.MIN_VALUE, Integer.MAX_VALUE);
        } catch (ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }
        boolean flag = NewsService.isAudioWhiteUser(userId);
        result.addProperty("isWhiteUser", flag);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 删除动态(20006003)
     *
     * @param jsonObject
     *            请求对象
     * @param checkTag
     *            是否验证token标记
     * @return 结果字符串
     */
    public JsonObject deleteNews(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            JsonObject result = new JsonObject();
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        // 验证参数
        int userId;
        int newsId;

        JsonObject result = new JsonObject();
        // 解析参数
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 1, "06030002", 1, Integer.MAX_VALUE);
            newsId = CommonUtil.getJsonParamInt(jsonObject, "newsId", 1, "06030004", 1, Integer.MAX_VALUE);
        } catch (ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }
        NewsInfo newsInfo = NewsService.getNewsInfoByNewsIdForState(newsId, 0);
        if (newsInfo == null || (newsInfo.getState() != 1 && StringUtil.strIsNull(newsInfo.getRefAudio()))||
                (!StringUtil.strIsNull(newsInfo.getRefAudio())&&newsInfo.getState() == 3)) {
            result.addProperty("TagCode", "06030103");
            return result;
        }
        if (newsInfo.getUserId() != userId) {
            result.addProperty("TagCode", "06030104");
            return result;
        }

        if (NewsService.deleteNews(newsId, userId)) {
            NewsV2Source.delHotRef(String.valueOf(newsId));
            // NewsV2Source.delHot(newsId);
            if (newsInfo.getRefVideo() != null) {
                resourceNewService.delResource(Integer.valueOf(Pattern.compile("\\{|\\}").matcher(newsInfo.getRefVideo()).replaceAll("")));
            } else if (newsInfo.getRefImage() != null) {
                String[] resIds = Pattern.compile("\\{|\\}").matcher(newsInfo.getRefImage()).replaceAll("").split(",");
                for (String resId : resIds) {
                    resourceNewService.delResource(Integer.valueOf(resId));
                }
            }
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } else {
            result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
            return result;
        }

    }

    /**
     * 获取个人动态列表(20006004)
     *
     * @param jsonObject
     *            请求对象
     * @return 结果字符串
     */
    public JsonObject getUserNewsList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {

        JsonObject result = new JsonObject();
        // 定义所需参数
        int userId, orderId;
        int start = 0;
        int offset = Constant.return_news_count;
        int platform = 0;
        // 新加参数,老版本默认传0
        int isSelf = 0;
        // 解析参数
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
            orderId = CommonUtil.getJsonParamInt(jsonObject, "orderId", 0, "06040001", 1, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, null, 0, Integer.MAX_VALUE);
            offset = CommonUtil.getJsonParamInt(jsonObject, "offset", Constant.return_news_count, null, 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
        } catch (ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        isSelf = userId == orderId ? 1 : 0;
        int count = NewsService.getNewsCountByUserId(orderId, isSelf);
        if (count > 0 && start >= count) {
            /* '02';分页超出范围 */
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        }

        if (count > 0) {
            // 登录返回是否点赞过
            List<NewsInfo> newsList = NewsService.getSelfNewsList(orderId, start, offset, isSelf, checkTag ? userId : 0);
            if (newsList != null && newsList.size() > 0) {
                JsonArray jNewsList = new JsonArray();
                for (NewsInfo newsInfo : newsList) {
                    JsonObject json = NewsService.getNewResourceJson(newsInfo, platform, false);
                    jNewsList.add(json);
                }

                RoomInfo actorInfo = RoomService.getRoomInfo(orderId);
                if (actorInfo != null) {
                    result.addProperty("nickname", actorInfo.getNickname());
                    if (actorInfo.getGender() != null) {
                        result.addProperty("gender", actorInfo.getGender());
                    }
                    if (actorInfo.getPortrait() != null) {
                        if (platform == PlatformEnum.WEB) {
                            result.addProperty("portrait_path_256", actorInfo.getPortrait() + "!256");
                        } else if (platform == PlatformEnum.ANDROID) {
                            result.addProperty("portrait_path_48", actorInfo.getPortrait() + "!48");
                            result.addProperty("portrait_path_128", actorInfo.getPortrait() + "!128");
                        } else if (platform == PlatformEnum.IPHONE) {
                            result.addProperty("portrait_path_128", actorInfo.getPortrait() + "!128");
                        } else if (platform == PlatformEnum.IPAD) {
                            result.addProperty("portrait_path_128", actorInfo.getPortrait() + "!128");
                        } else {
                            result.addProperty("portrait_path_1280", actorInfo.getPortrait() + "!1280");
                            result.addProperty("portrait_path_256", actorInfo.getPortrait() + "!256");
                            result.addProperty("portrait_path_128", actorInfo.getPortrait() + "!128");
                            result.addProperty("portrait_path_48", actorInfo.getPortrait() + "!48");
                        }
                    }
                    result.addProperty("actorLevel", actorInfo.getActorLevel());
                    result.addProperty("richLevel", actorInfo.getRichLevel());
                    // 直播状态
                    result.addProperty("isLive", actorInfo.getLiveStarttime() != null && actorInfo.getLiveEndtime() == null ? 1 : 0);
                    result.addProperty("roomSource", actorInfo.getRoomSource());
                    result.addProperty("screenType", actorInfo.getScreenType());
                    result.addProperty("actorTag", 1);
                } else {
                    UserProfile userInfo = com.melot.kktv.service.UserService.getUserInfoV2(orderId);
                    if (userInfo != null) {
                        result.addProperty("nickname", userInfo.getNickName());
                        result.addProperty("gender", userInfo.getGender());
                        result.addProperty("actorLevel", userInfo.getActorLevel());
                        result.addProperty("richLevel", userInfo.getUserLevel());
                        result.addProperty("actorTag", 0);
                        if (userInfo.getPortrait() != null) {
                            if (platform == PlatformEnum.WEB) {
                                result.addProperty("portrait_path_256", userInfo.getPortrait() + "!256");
                            } else if (platform == PlatformEnum.ANDROID) {
                                result.addProperty("portrait_path_48", userInfo.getPortrait() + "!48");
                                result.addProperty("portrait_path_128", userInfo.getPortrait() + "!128");
                            } else if (platform == PlatformEnum.IPHONE) {
                                result.addProperty("portrait_path_128", userInfo.getPortrait() + "!128");
                            } else if (platform == PlatformEnum.IPAD) {
                                result.addProperty("portrait_path_128", userInfo.getPortrait() + "!128");
                            } else {
                                result.addProperty("portrait_path_1280", userInfo.getPortrait() + "!1280");
                                result.addProperty("portrait_path_256", userInfo.getPortrait() + "!256");
                                result.addProperty("portrait_path_128", userInfo.getPortrait() + "!128");
                                result.addProperty("portrait_path_48", userInfo.getPortrait() + "!48");
                            }
                        }
                    }
                }
                // 返回关注关系
                result.addProperty("isFollowed", com.melot.kktv.service.UserRelationService.isFollowed(userId, orderId) ? 1 : 0);
                result.add("newsList", jNewsList);
                result.addProperty("countTotal", count);
                result.addProperty("pathPrefix", ConfigHelper.getHttpdir()); // 图片前缀
                result.addProperty("mediaPathPrefix", ConfigHelper.getMediahttpdir()); // 多媒体前缀
                result.addProperty("videoPathPrefix", ConfigHelper.getVideoURL());// 七牛前缀
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            } else {
                result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
                return result;
            }
        } else {
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        }
    }

    /**
     * 评论动态(20006005)
     *
     * @param jsonObject
     *            请求对象
     * @param checkTag
     *            是否验证token标记
     * @return 结果字符串
     */
    public JsonObject addNewsComment(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {

        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        //特殊时期接口暂停使用
        if (configService.getIsSpecialTime()) {
            result.addProperty("TagCode", TagCodeEnum.FUNCTAG_UNUSED_EXCEPTION);
            return result;
        }

        // 定义所需参数
        int userId, newsId, toUserId, platform;
        String content;
        // 解析参数
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "06050001", 1, Integer.MAX_VALUE);
            newsId = CommonUtil.getJsonParamInt(jsonObject, "newsId", 0, "06050003", 1, Integer.MAX_VALUE);
            toUserId = CommonUtil.getJsonParamInt(jsonObject, "toUserId", 0, null, 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, null, 1, Integer.MAX_VALUE);
            content = CommonUtil.getJsonParamString(jsonObject, "content", null, "06050005", 1, 250);
            // matchXSSTag,字符长度要小于等于7
            if (CommonUtil.matchXSSTag(content) || content.length() > 7) {
                result.addProperty("TagCode", "06050006");
                return result;
            }
            if (GeneralService.hasSensitiveWords(userId, content)) {
                result.addProperty("TagCode", "06050009");
            }
            content = GeneralService.replaceSensitiveWords(userId, content);
        } catch (ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        if (!UserService.checkUserIdentify(userId)) {
            result.addProperty("TagCode", TagCodeEnum.FUNCTAG_UNUSED_EXCEPTION);
            return result;
        }

        // 1.if newsId exist ? o_tagCode := '03'; -- 动态不存在
        NewsInfo newsInfo = NewsService.getNewsInfoById(newsId, 0);

        if (newsInfo == null) {
            result.addProperty("TagCode", "06050008");
            return result;
        }
        int oldId = NewsService.ifCommentExist(newsId, content);
        if (oldId > 0) {
            result.addProperty("TagCode", "06050007");
            result.addProperty("commentId", oldId);
            return result;
        }

        // 2.insert comment
        NewsCommentHist newsCommentHist = new NewsCommentHist();
        newsCommentHist.setNewsId(newsId);
        if (toUserId > 0) {
            newsCommentHist.setUserId(toUserId);
        }
        newsCommentHist.setUserId(userId);
        newsCommentHist.setContent(content);
        newsCommentHist.setPlatform(platform);
        int commentId = NewsService.addCommentPg(newsCommentHist);
        if (commentId > 0) {
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            result.addProperty("commentId", commentId);
            return result;
        } else {
            // 调用存储过程未的到正常结果,TagCode:"+TagCode+",记录到日志了.
            logger.error("调用存储过程未的到正常结果 jsonObject:" + jsonObject.toString());
            result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
            return result;
        }
    }

    /**
     * 点赞接口(20006019)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject addPraise(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        // 定义所需参数
        int userId, commentId;
        // 解析参数
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "06190001", 1, Integer.MAX_VALUE);
            commentId = CommonUtil.getJsonParamInt(jsonObject, "commentId", 0, "06190003", 1, Integer.MAX_VALUE);
        } catch (ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        NewsCommentHist newsCommentHist = NewsService.getComment(commentId);
        if (newsCommentHist == null) {
            result.addProperty("TagCode", "06190002");
            return result;
        }

        /*
         * if (newsCommentHist.getUserId() == userId) {
         * result.addProperty("TagCode", "06190005"); return result; }
         */

        int histId = NewsService.addPraise(userId, commentId);
        if (histId > 0) {
            result.addProperty("histId", histId);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } else {
            result.addProperty("TagCode", "06190004");
        }
        return result;
    }

    /**
     * 获取推荐短评(20006021)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject getRecommendTopicOrComment(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        // 定义所需参数
        int type, count;
        // 解析参数
        try {
            // 0-短评, 1-话题
            type = CommonUtil.getJsonParamInt(jsonObject, "type", 0, null, 0, Integer.MAX_VALUE);
            count = CommonUtil.getJsonParamInt(jsonObject, "count", 6, null, 0, Integer.MAX_VALUE);
        } catch (ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        if (type == 0) {
            Set<String> commentList = NewsV2Source.getRecommendComment(0, count - 1);
            JsonArray commentArray = new JsonArray();
            for (String temp : commentList) {
                JsonObject json = new JsonObject();
                json.addProperty("content", temp);
                commentArray.add(json);
            }
            result.add("commentList", commentArray);
        } else {
            Set<String> topicList = NewsV2Source.getHotTopic(0, count - 1);
            JsonArray topicArray = new JsonArray();
            for (String temp : topicList) {
                JsonObject json = new JsonParser().parse(temp).getAsJsonObject();
                topicArray.add(json);
            }
            result.add("topicList", topicArray);
        }
        result.addProperty("pathPrefix", ConfigHelper.getHttpdir()); // 图片前缀
        result.addProperty("mediaPathPrefix", ConfigHelper.getMediahttpdir()); // 多媒体前缀
        result.addProperty("videoPathPrefix", ConfigHelper.getVideoURL());// 七牛前缀
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 热门短评换一换接口(20006020)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject changeHotComments(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        // 定义所需参数
        @SuppressWarnings("unused")
        int userId;
        // 解析参数
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
        } catch (ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        // 获取热门短评,6条
        Set<String> commentList = NewsV2Source.getHotComment();
        if (commentList != null && commentList.size() > 0) {
            int length = commentList.size();
            JsonArray hotCommentList = new JsonArray();
            if (length <= 12) {
                for (String temp : commentList) {
                    JsonObject json = new JsonParser().parse(temp).getAsJsonObject();
                    hotCommentList.add(json);
                }
            } else {
                Set<Integer> set = new HashSet<Integer>();
                String[] list = commentList.toArray(new String[] {});
                while (set.size() < 12) {
                    int i = (int) (Math.random() * length);
                    if (!set.contains(i)) {
                        hotCommentList.add(new JsonParser().parse(list[i]).getAsJsonObject());
                        set.add(i);
                    }
                }
            }
            result.add("hotCommentList", hotCommentList);
        }
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }



    /**
     * 获取热门动态(20006022)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject getHotNews(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        // 定义所需参数
        int userId, start, offset, type, appId;
        // 解析参数
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, null, 1, Integer.MAX_VALUE);
            offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 10, null, 1, Integer.MAX_VALUE);
            // 1-video 0-default news
            type = CommonUtil.getJsonParamInt(jsonObject, "type", 0, null, 1, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
        } catch (ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        int topicSize = 0, containSize = 0;
        long timestamp = System.currentTimeMillis();

        if (type > 0) {
            Set<Tuple> hotTopic = NewsV2Source.getVideoTopic(start, offset);
            if (hotTopic != null && hotTopic.size() > 0) {
                containSize = hotTopic.size();
                JsonArray hotTopicList = new JsonArray();
                for (Tuple tuple : hotTopic) {
                    try {
                        JsonObject json = new JsonParser().parse(tuple.getElement()).getAsJsonObject();
                        int count = NewsService.getTopicNewsCount(json.get("topicId").getAsInt());
                        json.addProperty("countTotal", count);
                        json.addProperty("position", tuple.getScore());
                        if (json.has("imageUrl")) {
                            String imageUrl = json.get("imageUrl").getAsString();
                            imageUrl = imageUrl.replaceFirst(ConfigHelper.getHttpdir(), "");
                            json.addProperty("imageUrl", imageUrl + "!400");
                        }
                        hotTopicList.add(json);
                    } catch (Exception e) {}
                }
                result.add("hotTopicList", hotTopicList);
            }

            // 获取实际所属分页
            Set<Tuple> hotTopicSize = NewsV2Source.getVideoTopic(0, start);
            if (hotTopicSize != null) {
                topicSize = hotTopicSize.size();
            }
        }

        if(type == 0){
            Set<String> hotNews = NewsV2Source.getHotNews(start - topicSize, offset - containSize, type);
            if (hotNews != null && hotNews.size() > 0) {
                JsonArray hotNewsList = new JsonArray();
                getJson(checkTag, userId, offset - containSize, hotNews, hotNewsList);
                result.add("hotNewsList", hotNewsList);
            }
        }else if(type == 1) {
            if(appId == 1){
                Set<String> hotNews = NewsV2Source.getHotNews(start - topicSize, offset - containSize, type);
                if (hotNews != null && hotNews.size() > 0) {
                    JsonArray hotNewsList = new JsonArray();
                    getJson(checkTag, userId, offset - containSize, hotNews, hotNewsList);
                    if (type == 1 && hotNewsList.size() < (offset - containSize)) {
                        int size = hotNewsList.size();
                        if (size > 0) {
                            // 去最后一条热门视频的时间去sort set补充视频。
                            timestamp = hotNewsList.get(size - 1).getAsJsonObject().get("publishedTime").getAsLong();
                            Set<String> otherNews = NewsV2Source.getVideoByTime(timestamp, 0, offset - containSize);
                            getJson(checkTag, userId, offset - containSize, otherNews, hotNewsList);
                        }
                    }
                    result.add("hotNewsList", hotNewsList);
                } else {
                    if (type == 1) {
                        JsonArray hotNewsList = new JsonArray();
                        Set<String> otherNews = NewsV2Source.getVideoByTime(timestamp, start - topicSize, offset - containSize);
                        getJson(checkTag, userId, offset - containSize, otherNews, hotNewsList);
                        result.add("hotNewsList", hotNewsList);
                    }
                }
            } else {
                List<NewsInfo> videoNewsList = NewsService.getVideoHall(appId,start,offset);
                JsonArray hotNewsList = new JsonArray();
                for(NewsInfo news:videoNewsList){
                    JsonObject json = NewsService.getNewResourceJson(news, 1, true);
                    hotNewsList.add(json);
                }
                result.add("hotNewsList", hotNewsList);
            }
        }

        result.addProperty("pathPrefix", ConfigHelper.getHttpdir()); // 图片前缀
        result.addProperty("mediaPathPrefix", ConfigHelper.getMediahttpdir()); // 多媒体前缀
        result.addProperty("videoPathPrefix", ConfigHelper.getVideoURL());// 七牛前缀
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 获取话题相关动态(20006023)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject getTopicPage(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        // 定义所需参数
        int topicId, userId, start, offset, sortType, platform, v,appId;
        // 解析参数
        try {
            sortType = CommonUtil.getJsonParamInt(jsonObject, "sortType", 0, null, 0, Integer.MAX_VALUE);
            topicId = CommonUtil.getJsonParamInt(jsonObject, "topicId", 0, "06230001", 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, null, 0, Integer.MAX_VALUE);
            offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 10, null, 0, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
            v = CommonUtil.getJsonParamInt(jsonObject, "v", 0, null, 0, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
        } catch (ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        int count = NewsService.getTopicNewsCount(topicId);
        if (count > 0 && start >= count) {
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        }

        if(appId != 1){
            NewsTopic newsTopic = NewsService.getTopicByTopicId(topicId);
            if(newsTopic!= null){
                result.addProperty("imageUrl",newsTopic.getImageUrl()+"!400");
                result.addProperty("introduction", newsTopic.getDescribe());
                if(newsTopic.getForAdmin()!=null && newsTopic.getForAdmin()==0){
                    result.addProperty("canDiscuss",true);
                }
                else {
                    result.addProperty("canDiscuss",false);
                }
            }
        }
        else {
            // 若是推荐话题则返回图片和简介
            Set<String> hotTopic = NewsService.getPopularTopic(0, -1);
            if (hotTopic != null && hotTopic.size() > 0) {
                for (String str : hotTopic) {
                    try {
                        JsonObject json = new JsonParser().parse(str).getAsJsonObject();
                        if (json.get("topicId").getAsInt() == topicId) {
                            if (json.has("imageUrl")) {
                                String imageUrl = json.get("imageUrl").getAsString();
                                imageUrl = imageUrl.replaceFirst(ConfigHelper.getHttpdirUp(), ConfigHelper.getHttpdir());
                                if (v > 130) {
                                    imageUrl = imageUrl.replaceFirst(ConfigHelper.getHttpdirUp(), "");
                                    imageUrl = imageUrl.replaceFirst(ConfigHelper.getHttpdir(), "");
                                }
                                result.addProperty("imageUrl", imageUrl + "!400");
                            }
                            result.addProperty("introduction", json.get("introduction").getAsString());
                            break;
                        }
                    } catch (Exception e) {
                        logger.error("NewsV2Functions.getTopicPage(" + "jsonObject:" + jsonObject + "checkTag:" + checkTag + "request:" + request + ") execute exception.", e);
                    }
                }
            }
        }
        if (count > 0) {
            List<NewsInfo> newsList = NewsService.getNewsListByTopicId(topicId, sortType, start, offset, checkTag ? userId : 0);
            if (newsList != null && newsList.size() > 0) {
                JsonArray jNewsList = new JsonArray();
                for (NewsInfo newsInfo : newsList) {
                    JsonObject json = NewsService.getNewResourceJson(newsInfo, platform, true);
                    json.addProperty("isFollowed", com.melot.kktv.service.UserRelationService.isFollowed(userId, newsInfo.getUserId()) ? 1 : 0);
                    jNewsList.add(json);
                }
                result.add("newsList", jNewsList);
                result.addProperty("countTotal", count);
                result.addProperty("pathPrefix", ConfigHelper.getHttpdir()); // 图片前缀
                result.addProperty("mediaPathPrefix", ConfigHelper.getMediahttpdir()); // 多媒体前缀
                result.addProperty("videoPathPrefix", ConfigHelper.getVideoURL());// 七牛前缀
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            } else {
                result.addProperty("pathPrefix", ConfigHelper.getHttpdir()); // 图片前缀
                result.addProperty("mediaPathPrefix", ConfigHelper.getMediahttpdir()); // 多媒体前缀
                result.addProperty("videoPathPrefix", ConfigHelper.getVideoURL());// 七牛前缀
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            }
        } else {
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir()); // 图片前缀
            result.addProperty("mediaPathPrefix", ConfigHelper.getMediahttpdir()); // 多媒体前缀
            result.addProperty("videoPathPrefix", ConfigHelper.getVideoURL());// 七牛前缀
            result.addProperty("countTotal", count);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        }

    }

    /**
     * 获取短评相关动态(20006024)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject getCommentPage(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        // 定义所需参数
        int userId, start, offset, platform, sortType;
        String content;
        // 解析参数
        try {
            content = CommonUtil.getJsonParamString(jsonObject, "content", null, "06240001", 1, 7);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 0, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, null, 0, Integer.MAX_VALUE);
            offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 10, null, 0, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
            sortType = CommonUtil.getJsonParamInt(jsonObject, "sortType", 0, null, 0, Integer.MAX_VALUE);
        } catch (ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        int count = NewsService.getCommentNewsCount(content);
        if (count > 0 && start >= count) {
            result.addProperty("TagCode", "06240002");
            return result;
        }

        if (count > 0) {
            List<NewsInfo> newsList = NewsService.getNewsListByComment(content, sortType, start, offset, checkTag ? userId : 0);
            if (newsList != null && newsList.size() > 0) {
                JsonArray jNewsList = new JsonArray();
                for (NewsInfo newsInfo : newsList) {
                    JsonObject json = NewsService.getNewResourceJson(newsInfo, platform, true);
                    json.addProperty("isFollowed", com.melot.kktv.service.UserRelationService.isFollowed(userId, newsInfo.getUserId()) ? 1 : 0);
                    jNewsList.add(json);
                }

                result.add("newsList", jNewsList);
                result.addProperty("countTotal", count);
                result.addProperty("pathPrefix", ConfigHelper.getHttpdir()); // 图片前缀
                result.addProperty("mediaPathPrefix", ConfigHelper.getMediahttpdir()); // 多媒体前缀
                result.addProperty("videoPathPrefix", ConfigHelper.getVideoURL());// 七牛前缀
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            } else {
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            }
        } else {
            result.addProperty("countTotal", count);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        }
    }

    /**
     * 获取动态审核状态(20006025)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject getStateByNewId(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        // 定义所需参数
        @SuppressWarnings("unused")
        int userId, platform;
        String newsIds;
        // 解析参数
        try {
            newsIds = CommonUtil.getJsonParamString(jsonObject, "newsIds", null, "06250001", 1, Integer.MAX_VALUE);
        } catch (ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        List<NewsInfo> newsList = NewsService.getStateByNewsIds(newsIds);
        if (newsList == null) {
            result.addProperty("TagCode", "06250002");
            return result;
        }
        JsonArray stateArray = new JsonArray();
        for (NewsInfo newsInfo : newsList) {
            if (newsInfo.getState() != null) {
                JsonObject json = new JsonObject();
                json.addProperty("newsId", newsInfo.getNewsId());
                json.addProperty("state", newsInfo.getState());
                stateArray.add(json);
            }
        }
        result.add("stateArray", stateArray);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 动态点赞接口(20006026)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject addNewsPraise(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        // 定义所需参数
        int userId, newsId;
        // 解析参数
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "06260001", 1, Integer.MAX_VALUE);
            newsId = CommonUtil.getJsonParamInt(jsonObject, "newsId", 0, "06260003", 1, Integer.MAX_VALUE);
        } catch (ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        int ret = NewsService.addNewsPraise(userId, newsId);
        if (ret > 0) {
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } else {
            // 点赞失败
            result.addProperty("TagCode", "06260002");
        }
        return result;
    }

    /**
     * 取消动态点赞接口(20006027)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject cancelNewsPraise(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        // 定义所需参数
        int userId, newsId;
        // 解析参数
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "06270001", 1, Integer.MAX_VALUE);
            newsId = CommonUtil.getJsonParamInt(jsonObject, "newsId", 0, "06270003", 1, Integer.MAX_VALUE);
        } catch (ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        int ret = NewsService.cancelNewsPraise(userId, newsId);
        if (ret >= 0) {
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } else {
            // 取消点赞失败
            result.addProperty("TagCode", "06270002");
        }
        return result;
    }

    /**
     * 取消点赞接口(20006028)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject cancelPraise(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        // 定义所需参数
        int userId, commentId;
        // 解析参数
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "06280001", 1, Integer.MAX_VALUE);
            commentId = CommonUtil.getJsonParamInt(jsonObject, "commentId", 0, "06280003", 1, Integer.MAX_VALUE);
        } catch (ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        int ret = NewsService.cancelPraise(userId, commentId);
        if (ret > 0) {
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } else {
            result.addProperty("TagCode", "06280002");
        }
        return result;
    }

    /**
     * 根据话题id获取推荐短评(20006029)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject getCommentListByTopic(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        // 定义所需参数
        int topicId;
        // 解析参数
        try {
            topicId = CommonUtil.getJsonParamInt(jsonObject, "topicId", 0, "06290001", 1, Integer.MAX_VALUE);
        } catch (ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        List<NewsCommentHist> cList = NewsService.getRecommendComment(topicId);
        if (cList != null && cList.size() > 0) {
            JsonArray jsonArray = new JsonArray();
            for (NewsCommentHist temp : cList) {
                JsonObject json = new JsonObject();
                json.addProperty("content", temp.getContent());
                jsonArray.add(json);
            }
            result.add("commentList", jsonArray);
        } else {
            Set<String> commentList = NewsV2Source.getRecommendComment(0, 10);
            JsonArray commentArray = new JsonArray();
            for (String temp : commentList) {
                JsonObject json = new JsonObject();
                json.addProperty("content", temp);
                commentArray.add(json);
            }
            result.add("commentList", commentArray);
        }
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 根据newsType获取动态(20006030)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject getNewsListByNewsType(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        // 定义所需参数
        int userId, newsType, start, offset, state, platform, actorId = 0;
        // 解析参数
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 0, Integer.MAX_VALUE);
            actorId = CommonUtil.getJsonParamInt(jsonObject, "actorId", 0, null, 0, Integer.MAX_VALUE);
            newsType = CommonUtil.getJsonParamInt(jsonObject, "newsType", 10, null, 0, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, null, 0, Integer.MAX_VALUE);
            offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 20, null, 1, Integer.MAX_VALUE);
            state = CommonUtil.getJsonParamInt(jsonObject, "state", 1, null, 0, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
        } catch (ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        if (userId == 0 && actorId == 0) {
            result.addProperty("TagCode", "06300001");
            return result;
        }

        if (actorId == 0) {
            //老版参数兼容,之前userId作为actorId使用,且不传actorId
            actorId = userId;
        }

        int count = NewsService.getNewsCountByResType(actorId, newsType, state);
        if (count > 0) {
            List<NewsInfo> newsList;
            if (checkTag) {
                newsList = NewsService.getNewsListAndPraiseByResType(actorId, userId, newsType, start, offset);
            } else {
                newsList = NewsService.getNewsListByResType(actorId, newsType, start, offset);
            }
            if (newsList != null && newsList.size() > 0) {
                JsonArray jNewsList = new JsonArray();
                for (NewsInfo newsInfo : newsList) {
                    JsonObject json = NewsService.getNewResourceJson(newsInfo, platform, false);
                    jNewsList.add(json);
                }


                result.add("newsList", jNewsList);

            }
        }

        RoomInfo actorInfo = RoomService.getRoomInfo(actorId);
        if (actorInfo != null) {
            result.addProperty("nickname", actorInfo.getNickname());
            if (actorInfo.getGender() != null) {
                result.addProperty("gender", actorInfo.getGender());
            }
            if (actorInfo.getPortrait() != null) {
                if (platform == PlatformEnum.WEB) {
                    result.addProperty("portrait_path_256", actorInfo.getPortrait() + "!256");
                } else if (platform == PlatformEnum.ANDROID) {
                    result.addProperty("portrait_path_48", actorInfo.getPortrait() + "!48");
                    result.addProperty("portrait_path_128", actorInfo.getPortrait() + "!128");
                } else if (platform == PlatformEnum.IPHONE) {
                    result.addProperty("portrait_path_128", actorInfo.getPortrait() + "!128");
                } else if (platform == PlatformEnum.IPAD) {
                    result.addProperty("portrait_path_128", actorInfo.getPortrait() + "!128");
                } else {
                    result.addProperty("portrait_path_1280", actorInfo.getPortrait() + "!1280");
                    result.addProperty("portrait_path_256", actorInfo.getPortrait() + "!256");
                    result.addProperty("portrait_path_128", actorInfo.getPortrait() + "!128");
                    result.addProperty("portrait_path_48", actorInfo.getPortrait() + "!48");
                }
            }
            result.addProperty("actorLevel", actorInfo.getActorLevel());
            result.addProperty("richLevel", actorInfo.getRichLevel());
            // 直播状态
            result.addProperty("isLive", actorInfo.getLiveStarttime() != null && actorInfo.getLiveEndtime() == null ? 1 : 0);
            result.addProperty("roomSource", actorInfo.getRoomSource());
            result.addProperty("screenType", actorInfo.getScreenType());
            result.addProperty("actorTag", 1);
        } else {
            UserProfile userInfo = com.melot.kktv.service.UserService.getUserInfoV2(actorId);
            if (userInfo != null) {
                result.addProperty("nickname", userInfo.getNickName());
                result.addProperty("gender", userInfo.getGender());
                result.addProperty("actorLevel", userInfo.getActorLevel());
                result.addProperty("richLevel", userInfo.getUserLevel());
                result.addProperty("actorTag", 0);
                if (userInfo.getPortrait() != null) {
                    if (platform == PlatformEnum.WEB) {
                        result.addProperty("portrait_path_256", userInfo.getPortrait() + "!256");
                    } else if (platform == PlatformEnum.ANDROID) {
                        result.addProperty("portrait_path_48", userInfo.getPortrait() + "!48");
                        result.addProperty("portrait_path_128", userInfo.getPortrait() + "!128");
                    } else if (platform == PlatformEnum.IPHONE) {
                        result.addProperty("portrait_path_128", userInfo.getPortrait() + "!128");
                    } else if (platform == PlatformEnum.IPAD) {
                        result.addProperty("portrait_path_128", userInfo.getPortrait() + "!128");
                    } else {
                        result.addProperty("portrait_path_1280", userInfo.getPortrait() + "!1280");
                        result.addProperty("portrait_path_256", userInfo.getPortrait() + "!256");
                        result.addProperty("portrait_path_128", userInfo.getPortrait() + "!128");
                        result.addProperty("portrait_path_48", userInfo.getPortrait() + "!48");
                    }
                }
            }
        }

        result.addProperty("pathPrefix", ConfigHelper.getHttpdir()); // 图片前缀
        result.addProperty("mediaPathPrefix", ConfigHelper.getMediahttpdir()); // 多媒体前缀
        result.addProperty("videoPathPrefix", ConfigHelper.getVideoURL());// 七牛前缀
        result.addProperty("countTotal", count);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 删除评论(20006006)
     *
     * @param jsonObject
     *            请求对象
     * @param checkTag
     *            是否验证token标记
     * @return 结果字符串
     */
    public JsonObject deleteNewsComment(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {

        JsonObject result = new JsonObject();

        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        // 定义所需参数
        int userId, commentId;
        // 解析参数
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "06060001", 1, Integer.MAX_VALUE);
            commentId = CommonUtil.getJsonParamInt(jsonObject, "commentId", 0, "06060003", 1, Integer.MAX_VALUE);
        } catch (ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }
        // 1.if the commentId exist
        NewsCommentHist newsCommentHist = NewsService.getComment(commentId);
        if (newsCommentHist == null) {
            result.addProperty("TagCode", "06060103");
            return result;
        }
        // 2.if the news exist
        NewsInfo newsInfo = NewsService.getNewsInfoById(newsCommentHist.getNewsId(), 0);
        if (newsInfo == null) {
            result.addProperty("TagCode", "06060104");
            return result;
        }
        // 3.if the userId is news or comment owner
        if (userId != newsCommentHist.getUserId() && userId != newsInfo.getUserId()) {
            result.addProperty("TagCode", "06060105");
            return result;
        }
        // 4.delete the comment
        if (!NewsService.deleteComment(commentId, newsInfo.getNewsId())) {
            result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
            return result;
        }

        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 获取评论(20006007)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject getNewsComment(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();

        // 验证参数
        int userId;
        int newsId;
        int start;
        int offset;

        // 解析参数
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
            newsId = CommonUtil.getJsonParamInt(jsonObject, "newsId", 1, "06070002", 1, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, "06070003", 0, Integer.MAX_VALUE);
            offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 1, "06070004", 1, Integer.MAX_VALUE);
        } catch (ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        List<NewsCommentHist> commentList = NewsService.getCommentList(newsId, start, offset, userId);
        if (commentList != null && commentList.size() > 0) {
            JsonArray commentArray = new JsonArray();
            for (NewsCommentHist temp : commentList) {
                JsonObject json = new JsonObject();
                json.addProperty("userId", temp.getUserId());
                json.addProperty("commentId", temp.getCommentId());
                json.addProperty("content", temp.getContent());
                json.addProperty("praiseNum", temp.getPraiseNum());
                json.addProperty("isPraise", temp.getIsPraise() == null ? 0 : temp.getIsPraise());
                json.addProperty("portrait_path", temp.getPortrait_path());
                json.addProperty("nickName", temp.getNickName());
                json.addProperty("commentTime", temp.getCommentTime().getTime());
                commentArray.add(json);
            }
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir()); // 图片前缀
            result.addProperty("mediaPathPrefix", ConfigHelper.getMediahttpdir()); // 多媒体前缀
            result.addProperty("videoPathPrefix", ConfigHelper.getVideoURL());// 七牛前缀
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            result.add("commentList", commentArray);
        } else if (start == 0) {
            result.addProperty("TagCode", "06070006");
        } else {
            result.addProperty("TagCode", "00000000");
        }
        return result;
    }

    /**
     * 根据动态Id获取动态详细信息(20006012)
     *
     * @param jsonObject
     *            请求对象
     * @param checkTag
     *            是否验证token标记
     * @return 结果字符串
     */
    public JsonObject getNewsInfoByNewsId(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {

        // 验证参数
        Integer newsId, userId;
        int platform = 0;

        JsonObject result = new JsonObject();
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
            newsId = CommonUtil.getJsonParamInt(jsonObject, "newsId", 0, "06120001", 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, null, 1, Integer.MAX_VALUE);
        } catch (ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        NewsInfo newsInfo = NewsService.getNewsInfoByNewsIdForState(newsId, checkTag ? userId : 0);
        if (newsInfo == null) {
            result.addProperty("TagCode", "06120003");
            return result;
        }
        result = NewsService.getNewResourceJson(newsInfo, platform, false);
        RoomInfo actorInfo = RoomService.getRoomInfo(newsInfo.getUserId());
        if (actorInfo != null) {
            result.addProperty("nickname", actorInfo.getNickname());
            if (actorInfo.getGender() != null) {
                result.addProperty("gender", actorInfo.getGender());
            }
            if (actorInfo.getPortrait() != null) {
                if (platform == PlatformEnum.WEB) {
                    result.addProperty("portrait_path_256", actorInfo.getPortrait() + "!256");
                } else if (platform == PlatformEnum.ANDROID) {
                    result.addProperty("portrait_path_48", actorInfo.getPortrait() + "!48");
                    result.addProperty("portrait_path_128", actorInfo.getPortrait() + "!128");
                } else if (platform == PlatformEnum.IPHONE) {
                    result.addProperty("portrait_path_128", actorInfo.getPortrait() + "!128");
                } else if (platform == PlatformEnum.IPAD) {
                    result.addProperty("portrait_path_128", actorInfo.getPortrait() + "!128");
                } else {
                    result.addProperty("portrait_path_1280", actorInfo.getPortrait() + "!1280");
                    result.addProperty("portrait_path_256", actorInfo.getPortrait() + "!256");
                    result.addProperty("portrait_path_128", actorInfo.getPortrait() + "!128");
                    result.addProperty("portrait_path_48", actorInfo.getPortrait() + "!48");
                }
            }
            result.addProperty("actorLevel", actorInfo.getActorLevel());
            result.addProperty("richLevel", actorInfo.getRichLevel());
            // 直播状态
            result.addProperty("isLive", actorInfo.getLiveStarttime() != null && actorInfo.getLiveEndtime() == null ? 1 : 0);
            result.addProperty("roomSource", actorInfo.getRoomSource());
            result.addProperty("screenType", actorInfo.getScreenType());
            result.addProperty("actorTag", 1);
        } else {
            UserProfile userInfo = com.melot.kktv.service.UserService.getUserInfoV2(newsInfo.getUserId());
            if (userInfo != null) {
                result.addProperty("nickname", userInfo.getNickName());
                result.addProperty("gender", userInfo.getGender());
                result.addProperty("actorLevel", userInfo.getActorLevel());
                result.addProperty("richLevel", userInfo.getUserLevel());
                result.addProperty("actorTag", 0);
                if (userInfo.getPortrait() != null) {
                    if (platform == PlatformEnum.WEB) {
                        result.addProperty("portrait_path_256", userInfo.getPortrait() + "!256");
                    } else if (platform == PlatformEnum.ANDROID) {
                        result.addProperty("portrait_path_48", userInfo.getPortrait() + "!48");
                        result.addProperty("portrait_path_128", userInfo.getPortrait() + "!128");
                    } else if (platform == PlatformEnum.IPHONE) {
                        result.addProperty("portrait_path_128", userInfo.getPortrait() + "!128");
                    } else if (platform == PlatformEnum.IPAD) {
                        result.addProperty("portrait_path_128", userInfo.getPortrait() + "!128");
                    } else {
                        result.addProperty("portrait_path_1280", userInfo.getPortrait() + "!1280");
                        result.addProperty("portrait_path_256", userInfo.getPortrait() + "!256");
                        result.addProperty("portrait_path_128", userInfo.getPortrait() + "!128");
                        result.addProperty("portrait_path_48", userInfo.getPortrait() + "!48");
                    }
                }
            }
        }
        // 返回关注关系
        result.addProperty("isFollowed", com.melot.kktv.service.UserRelationService.isFollowed(userId, newsInfo.getUserId()) ? 1 : 0);
        result.addProperty("pathPrefix", ConfigHelper.getHttpdir()); // 图片前缀
        result.addProperty("mediaPathPrefix", ConfigHelper.getMediahttpdir()); // 多媒体前缀
        result.addProperty("videoPathPrefix", ConfigHelper.getVideoURL());// 七牛前缀
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;

    }

    /**
     * 获取热门动态列表(20000402)
     *
     * @return
     */
    public JsonObject getHotMediaNewsList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {

        // 定义使用的参数
        @SuppressWarnings("unused")
        int userId = 0, pageIndex = 0, totalCount = 0, platform = PlatformEnum.WEB, countPerPage = Constant.return_news_count
                , v = 0,appId= 0;
        // 定义返回结果
        JsonObject result = new JsonObject();

        // 解析参数
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 0, Integer.MAX_VALUE);
            v = CommonUtil.getJsonParamInt(jsonObject, "v", 0, null, 0, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
        } catch (ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        //区域化版本通过这个接口拿热门话题
        if(appId != 1){
            List<NewsTopic> hotTopics = NewsService.getHotTopicList(appId,0,8);
            result.add("hotTopicList", new Gson().toJsonTree(hotTopics));
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir()); // 图片前缀
            result.addProperty("mediaPathPrefix", ConfigHelper.getMediahttpdir()); // 多媒体前缀
            result.addProperty("videoPathPrefix", ConfigHelper.getVideoURL());// 七牛前缀
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        }
        // 获取热门话题,返回8条
        Set<String> hotTopic = NewsV2Source.getHotTopic(0, -1);
        if (hotTopic != null && hotTopic.size() > 0) {
            JsonArray hotTopicList = new JsonArray();
            for (String str : hotTopic) {
                try {
                    JsonObject json = new JsonParser().parse(str).getAsJsonObject();
                    if (json.has("imageUrl")) {
                        String imageUrl = json.get("imageUrl").getAsString();
                        imageUrl = imageUrl.replaceFirst(ConfigHelper.getHttpdirUp(), ConfigHelper.getHttpdir());
                        if (v > 130) {
                            imageUrl = imageUrl.replaceFirst(ConfigHelper.getHttpdirUp(), "");
                            imageUrl = imageUrl.replaceFirst(ConfigHelper.getHttpdir(), "");
                        }
                        json.addProperty("imageUrl", imageUrl + "!400");
                    }
                    hotTopicList.add(json);
                } catch (Exception e) {}
            }
            result.add("hotTopicList", hotTopicList);
        }

        // 获取热门动态,2条
        Set<String> hotNews = NewsV2Source.getHotNews(0, 10, 0);
        if (hotNews != null && hotNews.size() > 0) {
            JsonArray hotNewsList = new JsonArray();
            for (String str : hotNews) {
                try {
                    NewsInfo newsInfo = NewsService.getNewsInfoById(Integer.valueOf(str), checkTag ? userId : 0);
                    if (newsInfo == null) {
                        continue;
                    }
                    JsonObject json = NewsService.getNewResourceJson(newsInfo, 1, true);
                    json.addProperty("isFollowed", com.melot.kktv.service.UserRelationService.isFollowed(userId, newsInfo.getUserId()) ? 1 : 0);
                    hotNewsList.add(json);
                    if (hotNewsList.size() == 2) {
                        break;
                    }
                } catch (Exception e) {
                    logger.error("NewsV2Functions.getHotMediaNewsList(" + "jsonObject:" + jsonObject + "checkTag:" + checkTag + "request:" + request + ") execute exception.", e);
                }
            }
            result.add("hotNewsList", hotNewsList);
        }

        // 获取推荐动态,返回n <= 20条, 随意返回一条
        Set<String> recommendNews = NewsV2Source.getRecommendNews();
        @SuppressWarnings({ "unchecked", "rawtypes" })
        List rlist = new ArrayList(recommendNews);
        if (recommendNews != null && recommendNews.size() > 0) {
            JsonArray recommendList = new JsonArray();
            int size = rlist.size();
            int flag = 3;
            while (flag-- > 0) {
                int i = new Random().nextInt(size);
                String str = (String) rlist.get(i);
                try {
                    NewsInfo newsInfo = new Gson().fromJson(str, new TypeToken<NewsInfo>() {}.getType());
                    // 判断是否被删除了
                    if (newsInfo == null) {
                        continue;
                    }
                    NewsInfo stateInfo = NewsService.getNewsInfoById(newsInfo.getNewsId(), checkTag ? userId : 0);
                    if (stateInfo == null) {
                        continue;
                    }
                    int orderId = newsInfo.getUserId();
                    JsonObject json = NewsService.getNewResourceJson(stateInfo, 1, true);
                    json.addProperty("isFollowed", com.melot.kktv.service.UserRelationService.isFollowed(userId, orderId) ? 1 : 0);
                    recommendList.add(json);
                    break;
                } catch (Exception e) {}
            }
            result.add("recommendNewsList", recommendList);
        }

        // 获取热门短评,6条
        Set<String> commentList = NewsV2Source.getHotComment();
        if (commentList != null && commentList.size() > 0) {
            int length = commentList.size();
            JsonArray hotCommentList = new JsonArray();
            if (length <= 12) {
                for (String temp : commentList) {
                    JsonObject json = new JsonParser().parse(temp).getAsJsonObject();
                    hotCommentList.add(json);
                }
            } else {
                Set<Integer> set = new HashSet<Integer>();
                String[] list = commentList.toArray(new String[] {});
                while (set.size() < 12) {
                    int i = (int) (Math.random() * length);
                    if (!set.contains(i)) {
                        hotCommentList.add(new JsonParser().parse(list[i]).getAsJsonObject());
                        set.add(i);
                    }
                }
                result.add("hotCommentList", hotCommentList);
            }
        }

        result.addProperty("pathPrefix", ConfigHelper.getHttpdir()); // 图片前缀
        result.addProperty("mediaPathPrefix", ConfigHelper.getMediahttpdir()); // 多媒体前缀
        result.addProperty("videoPathPrefix", ConfigHelper.getVideoURL());// 七牛前缀
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 获取推荐动态（用户关注）列表 （20000403）
     *
     * @param
     * @return
     */
    public JsonObject getRecNewsList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        // 定义使用的参数
        int start = 0, offset = 0, platform = 0, userId = 0;
        // 定义返回结果
        JsonObject result = new JsonObject();
        try {
            // 解析参数
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 1, TagCodeEnum.USERID_MISSING, 0, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, null, 0, Integer.MAX_VALUE);
            offset = CommonUtil.getJsonParamInt(jsonObject, "offset", Constant.return_news_count, null, 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", 0, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        List<NewsInfo> newsList = NewsService.getFollowNewsList(userId, start, offset);
        if (newsList != null && newsList.size() > 0) {
            JsonArray jNewsList = new JsonArray();
            for (NewsInfo newsInfo : newsList) {
                JsonObject json = NewsService.getNewResourceJson(newsInfo, platform, true);
                jNewsList.add(json);
            }

            if (start <= 0 && userId > 0) {
                NewsSource.setNewsRemindTime(String.valueOf(userId), String.valueOf(System.currentTimeMillis()));
            }

            result.addProperty("isLastPage", false);
            result.add("newsList", jNewsList);
            result.addProperty("countTotal", newsList.size());
            result.addProperty("pathPrefix", ConfigHelper.getHttpdir()); // 图片前缀
            result.addProperty("mediaPathPrefix", ConfigHelper.getMediahttpdir()); // 多媒体前缀
            result.addProperty("videoPathPrefix", ConfigHelper.getVideoURL());// 七牛前缀
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } else {
            // 判断下页是否有数据
            List<NewsInfo> list = NewsService.getFollowNewsList(userId, start + offset, offset);
            if (list != null && list.size() > 0) {
                result.addProperty("isLastPage", false);
            } else {
                result.addProperty("isLastPage", true);
            }
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        }

    }

    /**
     * 模糊搜索动态(20000404)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject searchNewsList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        String key;
        try {
            key = CommonUtil.getJsonParamString(jsonObject, "key", null, "00440001", 0, 10);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        if (StringUtil.strIsNull(key)) {
            result.addProperty("TagCode", "00440002");
            return result;
        }

        /*
         * com.melot.news.service.NewsService newsService =
         * (com.melot.news.service.NewsService)
         * ModuleService.getService("NewsService"); List<SearchNewsUser> list =
         * newsService.getNewsListByKey(key);
         * JsonArray jsonArray = new JsonArray(); if (list != null &&
         * list.size() > 0) { for (SearchNewsUser temp : list) { JsonObject
         * jObject = new JsonObject(); if (temp.getUserId() != null) {
         * jObject.addProperty("userId", temp.getUserId()); } if
         * (temp.getActorLevel() != null) { jObject.addProperty("actorLevel",
         * temp.getActorLevel()); } if (temp.getRichLevel() != null) {
         * jObject.addProperty("richLevel", temp.getRichLevel()); } if
         * (temp.getActorTag() != null) { jObject.addProperty("actorTag",
         * temp.getActorTag()); if (temp.getActorTag() == 1 &&
         * temp.getLiveType() != null) { jObject.addProperty("liveType",
         * temp.getLiveType()); } } if (temp.getOnlineCount() != null) {
         * jObject.addProperty("onlineCount", temp.getOnlineCount()); } if
         * (temp.getLastTime() != null) { jObject.addProperty("lastTime",
         * temp.getLastTime().getTime()); } if (temp.getPortrait_path() != null)
         * { String portraitAddress = temp.getPortrait_path();
         * jObject.addProperty("portrait_path_original", portraitAddress);
         * jObject.addProperty("portrait_path_48", portraitAddress + "!48");
         * jObject.addProperty("portrait_path_128", portraitAddress + "!128");
         * jObject.addProperty("portrait_path_256", portraitAddress + "!256");
         * jObject.addProperty("portrait_path_272", portraitAddress + "!272");
         * jObject.addProperty("portrait_path_1280", portraitAddress + "!1280");
         * jObject.addProperty("portrait_path_400", portraitAddress + "!400");
         * jObject.addProperty("portrait_path_756", portraitAddress +
         * "!756x567"); } if (temp.getNickname() != null) {
         * jObject.addProperty("nickname", temp.getNickname()); } if
         * (temp.getRoomSource() != null) { jObject.addProperty("roomSource",
         * temp.getRoomSource()); } if (temp.getScreenType() != null) {
         * jObject.addProperty("screenType", temp.getScreenType()); } if
         * (temp.getComments() != null) { List<String> comments =
         * temp.getComments(); JsonArray jArray = new JsonArray(); if (comments
         * != null && comments.size() > 0) { for (String string : comments) {
         * JsonObject jObject2 = new JsonObject();
         * jObject2.addProperty("comment", string); jArray.add(jObject2); } }
         * jObject.add("comments", jArray); } jsonArray.add(jObject); } }
         */

        result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        result.add("data", null);

        return result;
    }

    /**
     * 獲取熱門搜索(20000405)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getHotWordList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        /*
         * com.melot.news.service.NewsService newsService =
         * (com.melot.news.service.NewsService)
         * ModuleService.getService("NewsService"); List<String> list =
         * newsService.getHotKeyWord();
         * JsonArray jsonArray = new JsonArray(); if (list != null &&
         * list.size() > 0) { for (String str : list) { JsonObject jObject = new
         * JsonObject(); jObject.addProperty("hotWord", str);
         * jsonArray.add(jObject); } }
         */

        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        // result.add("hotWords", null);

        return result;
    }

    /**
     * 获取热门话题列表(分数排序)(20000406)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getPopularTopicList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        int start;
        int offset;
        int v,appId;
        try {
            start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, null, 0, Integer.MAX_VALUE);
            offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 1, null, 1, Integer.MAX_VALUE);
            v = CommonUtil.getJsonParamInt(jsonObject, "v", 1, null, 1, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, TagCodeEnum.APPID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        if(appId !=1){
            if(appId != 1){
                List<NewsTopic> hotTopics = NewsService.getHotTopicList(appId,start,offset);
                result.add("hotTopicList", new Gson().toJsonTree(hotTopics));
                result.addProperty("pathPrefix", ConfigHelper.getHttpdir()); // 图片前缀
                result.addProperty("mediaPathPrefix", ConfigHelper.getMediahttpdir()); // 多媒体前缀
                result.addProperty("videoPathPrefix", ConfigHelper.getVideoURL());// 七牛前缀
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            }
        }

        Set<String> topicList = NewsService.getPopularTopic(start, offset);
        JsonArray topicArray = new JsonArray();
        if (topicList != null && topicList.size() > 0) {
            for (String temp : topicList) {
                JsonObject json = new JsonParser().parse(temp).getAsJsonObject();
                if (json.has("imageUrl")) {
                    String imageUrl = json.get("imageUrl").getAsString();
                    imageUrl = imageUrl.replaceFirst(ConfigHelper.getHttpdirUp(), ConfigHelper.getHttpdir());
                    if (v > 130) {
                        imageUrl = imageUrl.replaceFirst(ConfigHelper.getHttpdirUp(), "");
                        imageUrl = imageUrl.replaceFirst(ConfigHelper.getHttpdir(), "");
                    }
                    json.addProperty("imageUrl", imageUrl + "!400");
                }
                topicArray.add(json);
            }
        }
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        result.addProperty("pathPrefix", ConfigHelper.getHttpdir()); // 图片前缀
        result.addProperty("mediaPathPrefix", ConfigHelper.getMediahttpdir()); // 多媒体前缀
        result.addProperty("videoPathPrefix", ConfigHelper.getVideoURL());// 七牛前缀
        result.add("topicList", topicArray);
        return result;
    }


    public String getRegexAdmin(String str) {
        return Pattern.compile("\\{|\\}|^,*").matcher(str).replaceAll("");
    }

    private void getJson(boolean checkTag, int userId, int offset, Set<String> hotNews, JsonArray hotNewsList) {
        for (String str : hotNews) {
            try {
                NewsInfo newsInfo = NewsService.getNewsInfoById(Integer.valueOf(str), checkTag ? userId : 0);
                if (newsInfo == null) {
                    continue;
                }
                JsonObject json = NewsService.getNewResourceJson(newsInfo, 1, true);
                json.addProperty("isFollowed", com.melot.kktv.service.UserRelationService.isFollowed(userId, newsInfo.getUserId()) ? 1 : 0);
                hotNewsList.add(json);
                if (hotNewsList.size() == offset) {
                    break;
                }
            } catch (Exception e) {
                logger.error("NewsV2Functions.getJson(" + "checkTag:" + checkTag + "userId:" + userId + "offset:" + offset + "hotNews:" + hotNews + "hotNewsList:" + hotNewsList + ") execute exception.", e);
            }
        }
    }


    private static VideoInfo getVideoInfoByHttp(String videoUrl) {
        videoUrl = new StringBuilder().append(ConfigHelper.getVideoURL()).append(videoUrl).append("?avinfo").toString();
        VideoInfo videoInfo = null;
        @SuppressWarnings("deprecation")
        CloseableHttpClient httpClient = new DefaultHttpClient();
        HttpGet get = new HttpGet(videoUrl);
        HttpResponse res = null;
        try {
            res = httpClient.execute(get);
            if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(res.getEntity());
                JSONObject jObject = JSON.parseObject(result);
                if (jObject.containsKey("streams")) {
                    JSONArray jArray = jObject.getJSONArray("streams");
                    if (jArray.size() > 0) {
                        for (Object temp : jArray) {
                            JSONObject js = (JSONObject) temp;
                            if (js.containsKey("codec_name") && js.get("codec_name").equals("h264")) {
                                videoInfo = new VideoInfo();
                                videoInfo.setWidth((int) js.get("width"));
                                videoInfo.setHeight((int) js.get("height"));
                                if (js.containsKey("tags")) {
                                    JSONObject tag = js.getJSONObject("tags");
                                    if (tag.containsKey("rotate")) {
                                        String rotate = (String) tag.get("rotate");
                                        if (rotate.equals("90") || rotate.equals("270")) {
                                            // 是否旋转
                                            videoInfo.setHeight((int) js.get("width"));
                                            videoInfo.setWidth((int) js.get("height"));
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("NewsV2Functions.getVideoInfoByHttp(" + "videoUrl:" + videoUrl + ") execute exception.", e);
        } catch (IllegalStateException e) {
            logger.error("NewsV2Functions.getVideoInfoByHttp(" + "videoUrl:" + videoUrl + ") execute exception.", e);
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    logger.error("NewsV2Functions.getVideoInfoByHttp(" + "videoUrl:" + videoUrl + ") execute exception.", e);
                }
            }
        }
        return videoInfo;
    }

    static class VideoInfo {

        private int width;

        private int height;

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }
    }
}

package com.melot.kkcx.functions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.melot.content.config.apply.service.ApplyActorService;
import com.melot.content.config.domain.ApplyActor;
import com.melot.kk.activity.domain.FamilyDefineActivity;
import com.melot.kk.activity.service.FamilyDefineActivityService;
import com.melot.kk.activity.service.GiftHistoryBoardService;
import com.melot.kkactivity.driver.domain.ActEpisode;
import com.melot.kkactivity.driver.domain.ActInfo;
import com.melot.kkactivity.driver.domain.CarouselBanner;
import com.melot.kkactivity.driver.domain.KkBanner;
import com.melot.kkactivity.driver.domain.KkPreviewAct;
import com.melot.kkactivity.driver.domain.StarActor;
import com.melot.kkactivity.driver.service.KkActivityService;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcx.model.ActivityEmoticon;
import com.melot.kkcx.model.ActorNotice;
import com.melot.kkcx.model.HistoryRankInfo;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.DBEnum;
import com.melot.kktv.util.DateUtil;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.kktv.util.confdynamic.GiftInfoConfig;
import com.melot.kktv.util.db.SqlMapClientHelper;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * Title: ActivityCenterFunctions
 * <p>
 * Description: 活动中心Functions
 * </p>
 * 
 * @author 林端端<a href="mailto:duanduan.lin@melot.cn">
 * @version V1.0
 * @since 2016年9月29日 下午2:01:28
 */
public class ActivityCenterFunctions {
    
    /** 日志记录对象 */
    private static Logger logger = Logger.getLogger(ActivityCenterFunctions.class);
    
    /**
     * 获取通栏轮播图列表（50002001）
     * 
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getCarouselBannerList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int type;
        
        try {
            type = CommonUtil.getJsonParamInt(jsonObject, "type", 1, "02010001", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            KkActivityService kkActivityService = (KkActivityService) MelotBeanFactory.getBean("kkActivityService");
            List<CarouselBanner> bannerList = kkActivityService.getCarouselBannerByType(type);
            JsonArray carouselBannerList = new JsonArray();
            for (CarouselBanner banner : bannerList) {
                JsonObject jsonObj = new JsonObject();
                jsonObj.addProperty("bannerId", banner.getBannerId());
                jsonObj.addProperty("position", banner.getPosition());
                if (banner.getBannerPath() != null) {
                    jsonObj.addProperty("bannerPath", banner.getBannerPath());
                }
                if (banner.getLinkPath() != null) {
                    jsonObj.addProperty("linkPath", banner.getLinkPath());
                }
                carouselBannerList.add(jsonObj);
            }
            result.add("carouselBannerList", carouselBannerList);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch(Exception e) {
            logger.error("KkActivityService.getCarouselBannerByType(" + type + ") return exception.", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        
        return result;
    }
    
    /**
     * 获取专区banner（50002002）
     * 
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getKKBannerList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        //位置类型 0：全部 1：首页 2：热门
        int locationType;
        //banner类型 1：web 2：APP
        int bannerType;
        int channelId;
        int start;
        int offset;
        
        try {
            locationType = CommonUtil.getJsonParamInt(jsonObject, "locationType", 0, "02020001", 0, Integer.MAX_VALUE);
            bannerType = CommonUtil.getJsonParamInt(jsonObject, "bannerType", 1, "02020002", 1, Integer.MAX_VALUE);
            channelId = CommonUtil.getJsonParamInt(jsonObject, "c", 1, null, 1, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, null, 0, Integer.MAX_VALUE);
            offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 10, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            KkActivityService kkActivityService = (KkActivityService) MelotBeanFactory.getBean("kkActivityService");
            List<KkBanner> kkBannerList = kkActivityService.getKKBannersByType(bannerType, locationType, channelId, start, offset);
            JsonArray bannerList = new JsonArray();
            //置顶位置排序
            for (int i = 0; i < kkBannerList.size(); i++){
                KkBanner banner = kkBannerList.get(i);
                //置顶位置未配置banner时，用最新上架banner填充
                if (banner.getPosition() == null || banner.getPosition() == i +1) {
                    bannerList.add(banner.toJsonObject());
                } else {
                    for (int j = i + 1; j < kkBannerList.size();) {
                        if (kkBannerList.get(j).getPosition() == null) {
                            bannerList.add(kkBannerList.get(j).toJsonObject());
                            kkBannerList.remove(j);
                            if (banner.getPosition() == j + 1) {
                                break;
                            }
                        } else {
                            j = j + 1;
                        }
                    }
                    bannerList.add(banner.toJsonObject());
                }
            }
            result.add("bannerList", bannerList);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch(Exception e) {
            logger.error("KkActivityService.getKKBannersByType(" + bannerType + ", " + locationType + ", " + channelId + ", " + start + ", " + offset + ") return exception.", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        
        return result;
    }
    
    /**
     * 获取节目列表（50002003）
     * 
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getPreviewActList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int type;
        int pageIndex;
        int countPerPage;
        
        try {
            type = CommonUtil.getJsonParamInt(jsonObject, "type", 1, "02030001", 1, Integer.MAX_VALUE);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 10, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            KkActivityService kkActivityService = (KkActivityService) MelotBeanFactory.getBean("kkActivityService");
            int kkPreviewActListCount = kkActivityService.getPreviewActListCountByType(type);
            JsonArray previewActList = new JsonArray();
            if (kkPreviewActListCount > 0) {
                List<KkPreviewAct> kkPreviewActList = kkActivityService.getPreviewActListByType(type, pageIndex, countPerPage);
                for (KkPreviewAct kkPreviewAct : kkPreviewActList) {
                    JsonObject jsonObj = new JsonObject();
                    jsonObj.addProperty("actId", kkPreviewAct.getActId());
                    jsonObj.addProperty("actTitle", kkPreviewAct.getActTitle());
                    if (kkPreviewAct.getPoster() != null) {
                        jsonObj.addProperty("poster", kkPreviewAct.getPoster());
                    }
                    if (kkPreviewAct.getStartTime() != null) {
                        jsonObj.addProperty("startTime", kkPreviewAct.getStartTime().getTime());
                    }
                    if (kkPreviewAct.getEndTime() != null) {
                        jsonObj.addProperty("endTime", kkPreviewAct.getEndTime().getTime());
                        //KK出品已结束节目返回总剧集数
                        if (type == 1 && kkPreviewAct.getEndTime().getTime() < System.currentTimeMillis()) {
                            jsonObj.addProperty("episodeCount", kkActivityService.getActEpisodeListCount(kkPreviewAct.getActId()));
                        }
                    }
                    if (kkPreviewAct.getActURL() != null) {
                        jsonObj.addProperty("actURL", kkPreviewAct.getActURL());
                    }
                    if (kkPreviewAct.getActRoom() != null) {
                        jsonObj.addProperty("actRoom", kkPreviewAct.getActRoom());
                    }
                    if (kkPreviewAct.getVideoURL() != null) {
                        jsonObj.addProperty("videoURL", kkPreviewAct.getVideoURL());
                    }
                    if (kkPreviewAct.getEpisodeStartTime() != null) {
                        jsonObj.addProperty("episodeStartTime", kkPreviewAct.getEpisodeStartTime().getTime());
                    }
                    if (kkPreviewAct.getEpisodeEndTime() != null) {
                        jsonObj.addProperty("episodeEndTime", kkPreviewAct.getEpisodeEndTime().getTime());
                    }
                    if (kkPreviewAct.getBackground() != null) {
                        jsonObj.addProperty("background", kkPreviewAct.getBackground());
                    }
                    previewActList.add(jsonObj);
                }
            }
            result.addProperty("count", kkPreviewActListCount);
            result.add("previewActList", previewActList);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch(Exception e) {
            logger.error("KkActivityService.getPreviewActListByType(" + type + ", " + pageIndex + ", " + countPerPage + ") return exception.", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        
        return result;
    }
    
    /**
     * 获取节目剧集（50002004）
     * 
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getActEpisodeList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int actId;
        int pageIndex;
        int countPerPage;
        
        try {
            actId = CommonUtil.getJsonParamInt(jsonObject, "actId", 1, "02040001", 1, Integer.MAX_VALUE);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 10, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            KkActivityService kkActivityService = (KkActivityService) MelotBeanFactory.getBean("kkActivityService");
            int actEpisodeListCount = kkActivityService.getActEpisodeListCount(actId);
            JsonArray episodeList = new JsonArray();
            if (actEpisodeListCount > 0) {
                List<ActEpisode> actEpisodeList = kkActivityService.getActEpisodeListByType(actId, pageIndex, countPerPage);
                for (ActEpisode actEpisode : actEpisodeList) {
                    JsonObject jsonObj = new JsonObject();
                    jsonObj.addProperty("episodeId", actEpisode.getEpisodeId());
                    if (actEpisode.getEpisodeTitle() != null) {
                        jsonObj.addProperty("episodeTitle", actEpisode.getEpisodeTitle());
                    }
                    if (actEpisode.getEpisodeNumber() != null) {
                        jsonObj.addProperty("episodeNumber", actEpisode.getEpisodeNumber());
                    }
                    if (actEpisode.getPoster() != null) {
                        jsonObj.addProperty("poster", actEpisode.getPoster());
                    }
                    if (actEpisode.getStartTime() != null) {
                        jsonObj.addProperty("startTime", actEpisode.getStartTime().getTime());
                    }
                    if (actEpisode.getEndTime() != null) {
                        jsonObj.addProperty("endTime", actEpisode.getEndTime().getTime());
                    }
                    if (actEpisode.getEpisodeURL() != null) {
                        jsonObj.addProperty("episodeURL", actEpisode.getEpisodeURL());
                    }
                    episodeList.add(jsonObj);
                }
            }
            result.addProperty("count", actEpisodeListCount);
            result.add("episodeList", episodeList);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch(Exception e) {
            logger.error("KkActivityService.getActEpisodeListByType(" + actId + ", " + pageIndex + ", " + countPerPage + ") return exception.", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        
        return result;
    }
    
    /**
     * 获取星主播（50002005）
     * 
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getStarActorList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int pageIndex;
        int countPerPage;
        
        try {
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 10, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            KkActivityService kkActivityService = (KkActivityService) MelotBeanFactory.getBean("kkActivityService");
            int startActorListCount = kkActivityService.getStarActorListCount(null, null);
            JsonArray startActorList = new JsonArray();
            if (startActorListCount > 0) {
                List<StarActor> actorList = kkActivityService.getStarActorList(null, null, pageIndex, countPerPage);
                for (StarActor starActor : actorList) {
                    JsonObject jsonObj = new JsonObject();
                    jsonObj.addProperty("sId", starActor.getSid());
                    jsonObj.addProperty("articleTitle", starActor.getArticleTitle());
                    jsonObj.addProperty("articleDesc", starActor.getArticleDesc());
                    jsonObj.addProperty("cover", starActor.getCover());
                    jsonObj.addProperty("dtime", starActor.getDtime().getTime());
                    startActorList.add(jsonObj);
                }
            }
            result.addProperty("count", startActorListCount);
            result.add("startActorList", startActorList);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch(Exception e) {
            logger.error("KkActivityService.getStartActorList(" + "null, null, " + pageIndex + ", " + countPerPage + ") return exception.", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        
        return result;
    }
    
    /**
     * 获取星主播信息（50002006）
     * 
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getStarActorInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int sId;
        
        try {
            sId = CommonUtil.getJsonParamInt(jsonObject, "sId", 1, "02060001", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            KkActivityService kkActivityService = (KkActivityService) MelotBeanFactory.getBean("kkActivityService");
            StarActor starActor = kkActivityService.getStarActorById(sId);
            if (starActor != null) {
                result.addProperty("sId", starActor.getSid());
                result.addProperty("articleTitle", starActor.getArticleTitle());
                result.addProperty("articleDesc", starActor.getArticleDesc());
                result.addProperty("cover", starActor.getCover());
                result.addProperty("content", starActor.getContent());
                result.addProperty("author", starActor.getAuthor());
                result.addProperty("dtime", starActor.getDtime().getTime());
            }
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch(Exception e) {
            logger.error("KkActivityService.getStarActorById(" + sId + ") return exception.", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        
        return result;
    }
    
    /**
     * 获取推荐星主播列表（50002007）
     * 
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getRecommendStarActorList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int sId;
        int offset;
        
        try {
            sId = CommonUtil.getJsonParamInt(jsonObject, "sId", 1, "02070001", 1, Integer.MAX_VALUE);
            offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 6, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            KkActivityService kkActivityService = (KkActivityService) MelotBeanFactory.getBean("kkActivityService");
            JsonArray startActorList = new JsonArray();
            List<StarActor> actorList = kkActivityService.getRecommendStarActorList(sId, offset);
            for (StarActor starActor : actorList) {
                JsonObject jsonObj = new JsonObject();
                jsonObj.addProperty("sId", starActor.getSid());
                jsonObj.addProperty("articleTitle", starActor.getArticleTitle());
                jsonObj.addProperty("articleDesc", starActor.getArticleDesc());
                jsonObj.addProperty("cover", starActor.getCover());
                jsonObj.addProperty("dtime", starActor.getDtime().getTime());
                startActorList.add(jsonObj);
            }
            result.add("startActorList", startActorList);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch(Exception e) {
            logger.error("KkActivityService.getRecommendStarActorList(" + sId + ", " + offset + ") return exception.", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        
        return result;
    }
    
    /**
     * 获取精彩现场列表（50002008）
     * 
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getStarSceneList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int pageIndex;
        int countPerPage;
        
        try {
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 10, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            KkActivityService kkActivityService = (KkActivityService) MelotBeanFactory.getBean("kkActivityService");
            int kkPreviewActListCount = kkActivityService.getEndedPreviewActListCount(3);
            JsonArray previewActList = new JsonArray();
            if (kkPreviewActListCount > 0) {
                List<KkPreviewAct> kkPreviewActList = kkActivityService.getEndedPreviewActList(3, pageIndex, countPerPage);
                for (KkPreviewAct kkPreviewAct : kkPreviewActList) {
                    JsonObject jsonObj = new JsonObject();
                    jsonObj.addProperty("actId", kkPreviewAct.getActId());
                    jsonObj.addProperty("actTitle", kkPreviewAct.getActTitle());
                    if (kkPreviewAct.getPoster() != null) {
                        jsonObj.addProperty("poster", kkPreviewAct.getPoster());
                    }
                    if (kkPreviewAct.getActURL() != null) {
                        jsonObj.addProperty("actURL", kkPreviewAct.getActURL());
                    }
                    if (kkPreviewAct.getVideoURL() != null) {
                        jsonObj.addProperty("videoURL", kkPreviewAct.getVideoURL());
                    }
                    if (kkPreviewAct.getBackground() != null) {
                        jsonObj.addProperty("background", kkPreviewAct.getBackground());
                    }
                    previewActList.add(jsonObj);
                }
            }
            result.addProperty("count", kkPreviewActListCount);
            result.add("previewActList", previewActList);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch(Exception e) {
            logger.error("KkActivityService.getEndedPreviewActList(3, " + pageIndex + ", " + countPerPage + ") return exception.", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        
        return result;
    }
    
    /**
     * 获取房间节目预告（50002009）
     * 
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getRoomPreview(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int roomId;
        
        try {
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 1, "02090001", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            KkActivityService kkActivityService = (KkActivityService) MelotBeanFactory.getBean("kkActivityService");
            List<ActInfo> actInfoList = kkActivityService.getRoomPreviewAct(roomId);
            int isWeekly = 0;
            if (actInfoList != null && actInfoList.size() > 0) {
                result.addProperty("actId", actInfoList.get(0).getActId());
                if (actInfoList.get(0).getActTitle() != null) {
                    result.addProperty("actTitle", actInfoList.get(0).getActTitle());
                }
                if (actInfoList.get(0).getActRoom() != null) {
                    result.addProperty("actRoom", actInfoList.get(0).getActRoom());
                }
                if (actInfoList.get(0).getActUrl() != null) {
                    result.addProperty("actUrl", actInfoList.get(0).getActUrl());
                }
                if (actInfoList.get(0).getActDesc() != null) {
                    result.addProperty("actDesc", actInfoList.get(0).getActDesc());
                }
                if (actInfoList.get(0).getActStartTime() == null) {
                    isWeekly = 1;
                }
                result.addProperty("isWeekly", isWeekly);
                long recentStartTime = 0;
                long startTime = 0;
                if (isWeekly == 1) {//周期性节目 
                    JsonArray jsonArray = new JsonArray();
                    for (ActInfo actInfo : actInfoList) {
                        JsonObject weeklyJson = new JsonObject();
                        weeklyJson.addProperty("dayweek", actInfo.getDayWeek());
                        weeklyJson.addProperty("startTime", actInfo.getSDTime());
                        jsonArray.add(weeklyJson);
                        startTime = DateUtil.getWeekBeginTime(System.currentTimeMillis()) + actInfo.getDayWeek()*24*3600*1000 + actInfo.getSDTime();
                        if (startTime < System.currentTimeMillis()) {
                            startTime = startTime + 7*24*3600*1000;
                        }
                        if (recentStartTime > startTime || recentStartTime == 0) {
                            recentStartTime = startTime;
                        }
                    }
                    result.add("weeklyTime", jsonArray);
                    result.addProperty("recentStartTime", recentStartTime);
                } else {//普通节目
                    result.addProperty("recentStartTime", actInfoList.get(0).getActStartTime().getTime());
                }
            }
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch(Exception e) {
            logger.error("KkActivityService.getRoomPreview(roomId, " + roomId + ") return exception.", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        
        return result;
    }
    
    /**
     * 获取家族活动列表（50002010）
     * 
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getFamilyActivityList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int familyId;
        int pageIndex;
        int countPerPage;
        
        try {
            familyId = CommonUtil.getJsonParamInt(jsonObject, "familyId", 1, TagCodeEnum.FAMILYID_MISSING, 1, Integer.MAX_VALUE);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 1, null, 1, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 10, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            FamilyDefineActivityService familyDefineActivityService = (FamilyDefineActivityService) MelotBeanFactory.getBean("familyDefineActivityService");
            int count = familyDefineActivityService.getFamilyDefineActivityListCount(familyId, 1, null, null, null);
            if (count > 0) {
                List<FamilyDefineActivity> familyDefineActivityList = familyDefineActivityService.getFamilyDefineActivityList(familyId, 1, null, null, null, countPerPage, (pageIndex - 1) * countPerPage);
                JsonArray familyActivityList = new JsonArray();
                for (FamilyDefineActivity familyDefineActivity : familyDefineActivityList) {
                    JsonObject jsonObj = new JsonObject();
                    jsonObj.addProperty("activityId", familyDefineActivity.getActivityId());
                    if (familyDefineActivity.getPosterUrl() != null) {
                        jsonObj.addProperty("posterUrl", familyDefineActivity.getPosterUrl());
                    }
                    if (familyDefineActivity.getActivityTitle() != null) {
                        jsonObj.addProperty("activityTitle", familyDefineActivity.getActivityTitle());
                    }
                    if (familyDefineActivity.getStartTime() != null) {
                        jsonObj.addProperty("startTime", familyDefineActivity.getStartTime().getTime());
                    }
                    if (familyDefineActivity.getEndTime() != null) {
                        jsonObj.addProperty("endTime", familyDefineActivity.getEndTime().getTime());
                    }
                    if (familyDefineActivity.getDescription() != null) {
                        jsonObj.addProperty("desc", familyDefineActivity.getDescription());
                    }
                    familyActivityList.add(jsonObj);
                }
                result.add("familyActivityList", familyActivityList);
            }
            result.addProperty("count", count);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch(Exception e) {
            logger.error("FamilyDefineActivityService.getFamilyDefineActivityList familyId :" + familyId + "return exception.", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        
        return result;
    }
    
    /**
     * 获取家族活动详情（50002011）
     * 
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getFamilyActivityInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int activityId;
        
        try {
            activityId = CommonUtil.getJsonParamInt(jsonObject, "activityId", 1, "02110001", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            FamilyDefineActivityService familyDefineActivityService = (FamilyDefineActivityService) MelotBeanFactory.getBean("familyDefineActivityService");
            FamilyDefineActivity familyDefineActivity = familyDefineActivityService.getFamilyDefineActivityByActivityId(activityId);
            if (familyDefineActivity != null) {
                if (familyDefineActivity.getActivityTitle() != null) {
                    result.addProperty("activityTitle", familyDefineActivity.getActivityTitle());
                }
                if (familyDefineActivity.getStartTime() != null) {
                    result.addProperty("startTime", familyDefineActivity.getStartTime().getTime());
                }
                if (familyDefineActivity.getEndTime() != null) {
                    result.addProperty("endTime", familyDefineActivity.getEndTime().getTime());
                }
                if (familyDefineActivity.getBonusEvent() != null) {
                    result.addProperty("bonusEvent", familyDefineActivity.getBonusEvent());
                }
                if (familyDefineActivity.getDescription() != null) {
                    result.addProperty("desc", familyDefineActivity.getDescription());
                }
                if (familyDefineActivity.getFrontUrl() != null) {
                    result.addProperty("frontUrl", familyDefineActivity.getFrontUrl());
                }
                if (familyDefineActivity.getJoinActor() != null) {
                    JsonArray jsonArray = new JsonArray();
                    String[] joinActorStr = familyDefineActivity.getJoinActor().split(";");
                    for (String actorId : joinActorStr) {
                        JsonObject jsonObj = new JsonObject();
                        jsonObj.addProperty("actorId", Integer.valueOf(actorId));
                        UserProfile userProfile = com.melot.kktv.service.UserService.getUserInfoV2(Integer.valueOf(actorId));
                        if (userProfile != null) {
                            if (userProfile.getNickName() != null) {
                                jsonObj.addProperty("nickName", userProfile.getNickName());
                            }
                            if (userProfile.getPortrait() != null) {
                                jsonObj.addProperty("portrait", ConfigHelper.getHttpdir() + userProfile.getPortrait());
                            }
                        }
                        jsonArray.add(jsonObj);
                    }
                    result.add("joinActorList", jsonArray);
                }
                if (familyDefineActivity.getJoinGift() != null) {
                    JsonArray jsonArray = new JsonArray();
                    String[] joinGiftStr = familyDefineActivity.getJoinGift().split(";");
                    for (String giftId: joinGiftStr) {
                        JsonObject jsonObj = new JsonObject();
                        jsonObj.addProperty("giftId", Integer.valueOf(giftId));
                        jsonObj.addProperty("giftName", GiftInfoConfig.getGiftName(Integer.valueOf(giftId)));
                        jsonArray.add(jsonObj);
                    }
                    result.add("joinGiftList", jsonArray);
                }
                
                GiftHistoryBoardService giftHistoryBoardService = (GiftHistoryBoardService) MelotBeanFactory.getBean("giftHistoryBoardService");
                String familyGiftHistoryStr = giftHistoryBoardService.getFamilyGiftHistoryBoardByActivityId(activityId);
                if (familyGiftHistoryStr != null) {
                    JsonArray actorRankList = new JsonArray();
                    @SuppressWarnings("unchecked")
                    List<HistoryRankInfo> actorGiftHistoryList = (List<HistoryRankInfo>) (new Gson().fromJson(familyGiftHistoryStr, new TypeToken<List<HistoryRankInfo>>(){}.getType()));
                    for (HistoryRankInfo giftHistory : actorGiftHistoryList) {
                        JsonObject jsonObj = new JsonObject();
                        UserProfile userProfile = com.melot.kktv.service.UserService.getUserInfoV2(giftHistory.getUserId());
                        if (userProfile != null) {
                            if (userProfile.getNickName() != null) {
                                jsonObj.addProperty("nickName", userProfile.getNickName());
                            }
                            if (userProfile.getPortrait() != null) {
                                jsonObj.addProperty("portrait", ConfigHelper.getHttpdir() + userProfile.getPortrait());
                            }
                        }
                        jsonObj.addProperty("userId", giftHistory.getUserId());
                        jsonObj.addProperty("amount", giftHistory.getAmount());
                        actorRankList.add(jsonObj);
                    }
                    result.add("actorRankList", actorRankList);
                }
                
                String userGiftHistoryStr = giftHistoryBoardService.getUserGiftHistoryBoardByActivityId(activityId);
                if (userGiftHistoryStr != null) {
                    JsonArray userRankList = new JsonArray();
                    @SuppressWarnings("unchecked")
                    List<HistoryRankInfo> userGiftHistoryList = (List<HistoryRankInfo>) (new Gson().fromJson(userGiftHistoryStr, new TypeToken<List<HistoryRankInfo>>(){}.getType()));
                    for (HistoryRankInfo giftHistory : userGiftHistoryList) {
                        JsonObject jsonObj = new JsonObject();
                        UserProfile userProfile = com.melot.kktv.service.UserService.getUserInfoV2(giftHistory.getUserId());
                        if (userProfile != null) {
                            if (userProfile.getNickName() != null) {
                                jsonObj.addProperty("nickName", userProfile.getNickName());
                            }
                            if (userProfile.getPortrait() != null) {
                                jsonObj.addProperty("portrait", ConfigHelper.getHttpdir() + userProfile.getPortrait());
                            }
                        }
                        jsonObj.addProperty("userId", giftHistory.getUserId());
                        jsonObj.addProperty("amount", giftHistory.getAmount());
                        userRankList.add(jsonObj);
                    }
                    result.add("userRankList", userRankList);
                }
            }
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch(Exception e) {
            logger.error("FamilyDefineActivityService.getFamilyActivityInfo activityId :" + activityId + "return exception.", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        
        return result;
    }
    
    /**
     * 获取关键字表情列表（50002012）
     * 
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    @SuppressWarnings("unchecked")
    public JsonObject getKeywordEmoticonList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        List<ActivityEmoticon> activityEmoticonList;
        try {
            activityEmoticonList = (List<ActivityEmoticon>) SqlMapClientHelper.getInstance(DBEnum.KKCX_PG).queryForList("Emoticon.getactivityEmoticonList");
        } catch(Exception e) {
            logger.error("ActivityCenterFunctions.getKeywordEmoticonList() return exception.", e);
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
            return result;
        }
        
        JsonArray emoticonList = new JsonArray();
        for (ActivityEmoticon activityEmoticon : activityEmoticonList) {
            JsonObject jsonObj = new JsonObject();
            jsonObj.addProperty("desc", activityEmoticon.getDescribe());
            jsonObj.addProperty("keyword", activityEmoticon.getKeywords());
            jsonObj.addProperty("emoticonURL", activityEmoticon.getIcons());
            jsonObj.addProperty("startTime", activityEmoticon.getBeginTime().getTime());
            if (activityEmoticon.getEndTime() != null) {
                jsonObj.addProperty("endTime", DateUtil.getNextDay(activityEmoticon.getEndTime()).getTime() - 1000);
            }
            emoticonList.add(jsonObj);
        }
        result.add("emoticonList", emoticonList);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        
        return result;
    }
    
    /**
     * 获取主播活动提醒（50002013）
     * 
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    @SuppressWarnings("unchecked")
    public JsonObject getActorNotice(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        // 获取参数
        int userId;
        int type;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            type = CommonUtil.getJsonParamInt(jsonObject, "type", 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        UserProfile userProfile = com.melot.kktv.service.UserService.getUserInfoV2(userId);
        if (userProfile == null || userProfile.getIsActor() != 1) {
            result.addProperty("TagCode", "02130001");
            return result;
        }

        List<ActorNotice> actorNoticeList;
        try {
            Map<String, Object> map = new HashMap<>();
            if (type > 0) {
                map.put("type", type);
            }
            actorNoticeList = (List<ActorNotice>) SqlMapClientHelper.getInstance(DBEnum.KKCX_PG).queryForList("Emoticon.getactorNoticeList", map);
        } catch (Exception e) {
            logger.error("ActivityCenterFunctions.getActorNotice() return exception.", e);
            result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
            return result;
        }

        JsonArray noticeList = new JsonArray();
        for (ActorNotice actorNotice : actorNoticeList) {
            JsonObject jsonObj = new JsonObject();
            jsonObj.addProperty("id", actorNotice.getId());
            jsonObj.addProperty("title", actorNotice.getTitle());
            jsonObj.addProperty("content", actorNotice.getContent());
            jsonObj.addProperty("linkURL", actorNotice.getLinkURL());
            jsonObj.addProperty("picture", actorNotice.getPicture());
            jsonObj.addProperty("type", actorNotice.getType());
            noticeList.add(jsonObj);
        }
        result.add("noticeList", noticeList);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);

        return result;
    }
}

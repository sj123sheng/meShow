package com.melot.kkcx.functions;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.kk.quiz.api.domain.QuizActivity;
import com.melot.kk.quiz.api.domain.QuizActivityRoomInfo;
import com.melot.kk.quiz.api.domain.QuizRankingList;
import com.melot.kk.quiz.api.domain.QuizUserInfo;
import com.melot.kk.quiz.api.domain.base.Page;
import com.melot.kk.quiz.api.service.QuizActivityService;
import com.melot.kktv.base.CommonStateCode;
import com.melot.kktv.base.Result;
import com.melot.kktv.service.ConfigService;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ParameterKeys;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * Title: QuizFunctions
 * <p>
 * Description: KK答题比赛相关接口
 * </p>
 * 
 * @author <a href="mailto:anwen.wei@melot.cn">魏安稳</a>
 * @version V1.0
 * @since 2017年12月21日 上午10:18:42
 */
public class QuizFunctions {
    private static final Logger log = Logger.getLogger(QuizFunctions.class);
    
    @Autowired
    private ConfigService configService;
    
    /**
     * 获取答题活动信息【51050401】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject getQuizActivityInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        int isShow = configService.getIsShow();
        result.addProperty("isShow", isShow);
        
        if (isShow == 0) {
            return result;
        }
        
        result.addProperty("activityPicture", configService.getActivityPicture());
        result.addProperty("activityThumbnail", configService.getActivityThumbnail());
        QuizActivityService quizActivityService = (QuizActivityService) MelotBeanFactory.getBean("quizActivityService");
        try {
            Result<QuizActivity> quizActivityResult = quizActivityService.getQuizActivity();
            if (quizActivityResult != null && !CommonStateCode.SUCCESS.equals(quizActivityResult.getCode())) {
                QuizActivity quizActivity = quizActivityResult.getData();
                if (quizActivity == null) {
                    result.addProperty("bonus", 1000000);
                } else {
                    result.addProperty("bonus", quizActivity.getBonus());
                    result.addProperty("systemTime", System.currentTimeMillis());
                    if (quizActivity.getState() == 1) {
                        result.addProperty("nextTime", 0);
                    } else {
                        result.addProperty("nextTime", quizActivity.getStartTime().getTime());
                    }
                }
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            }else {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
            }
        } catch (Exception e) {
            log.error("Module Error:", e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        return result;
    }
    
    /**
     * 获取答题活动状态，进行中：返回房间地址等信息；未开始：返回预告信息【51050402】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject getQuizInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        // 传了userId的需要校验token信息
        if (userId > 0 && !checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }
        QuizActivityService quizActivityService = (QuizActivityService) MelotBeanFactory.getBean("quizActivityService");
        try {
            Result<QuizActivity> quizActivityResult = quizActivityService.getQuizActivity();
            if (quizActivityResult == null || !CommonStateCode.SUCCESS.equals(quizActivityResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            QuizActivity quizActivity = quizActivityResult.getData();
            result.addProperty("activityState", quizActivity.getState());
            if (quizActivity.getState() == 0) {
                JsonObject noticeInfo = new JsonObject();
                // 组装活动预告信息
                noticeInfo.addProperty("activityPoster", quizActivity.getQuizActivityPoster());
                noticeInfo.addProperty("activityName", quizActivity.getQuizActivityTitle());
                noticeInfo.addProperty("nextTime", quizActivity.getStartTime().getTime());
                noticeInfo.addProperty("bonus", quizActivity.getBonus());
                
                // 组装用户信息
                if (userId > 0) {
                    Result<QuizUserInfo> quizUserInfoResult = quizActivityService.getQuizUserInfo(userId);
                    if (quizUserInfoResult != null && CommonStateCode.SUCCESS.equals(quizUserInfoResult.getCode())) {
                        QuizUserInfo quizUserInfo = quizUserInfoResult.getData();
                        noticeInfo.addProperty("amount", quizUserInfo.getAmount());
                        noticeInfo.addProperty("totalAmount", quizUserInfo.getTotalAmount());
                        noticeInfo.addProperty("revivalCount", quizUserInfo.getRevivalCount());
                    }
                    // 获取排名信息
                    Result<QuizRankingList> userQuizRankingResult = quizActivityService.getUserQuizRanking(0, userId);
                    if (userQuizRankingResult != null && CommonStateCode.SUCCESS.equals(userQuizRankingResult.getCode())) {
                        QuizRankingList quizRankingList = userQuizRankingResult.getData();
                        noticeInfo.addProperty("weeklyRanking", quizRankingList.getRanking());
                    }
                }
                result.add("noticeInfo", noticeInfo);
            } else {
                JsonObject roomInfo = new JsonObject();
                // 组装活动信息
                roomInfo.addProperty("activityPoster", quizActivity.getQuizActivityPoster());
                roomInfo.addProperty("activityName", quizActivity.getQuizActivityTitle());
                
                // 房间地址信息
                Result<QuizActivityRoomInfo> quizRoomInfoResult = quizActivityService.getQuizActivityRoomInfo();
                if (quizRoomInfoResult != null && CommonStateCode.SUCCESS.equals(quizRoomInfoResult.getCode())) {
                    QuizActivityRoomInfo quizActivityRoomInfo = quizRoomInfoResult.getData();
                    roomInfo.addProperty("liveAddress", quizActivityRoomInfo.getLivePushAddr());
                    roomInfo.addProperty("nodeAddress", quizActivityRoomInfo.getSocketAddr());
                }
                result.add("roomInfo", roomInfo);
            }
        } catch (Exception e) {
            log.error("Module Error:", e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        
        result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 获取答题比赛榜单【51050403】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject getQuizRankingList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        int userId;
        int type;
        int start;
        int num;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, null, 1, Integer.MAX_VALUE);
            type = CommonUtil.getJsonParamInt(jsonObject, "type", 0, null, 0, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, "start", 0, null, 0, Integer.MAX_VALUE);
            num = CommonUtil.getJsonParamInt(jsonObject, "num", 0, null, 20, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        QuizActivityService quizActivityService = (QuizActivityService) MelotBeanFactory.getBean("quizActivityService");
        try {
            Result<Page<QuizRankingList>> rankingListResult = quizActivityService.getQuizActivityRankingList(type, start, num);
            if (rankingListResult != null && CommonStateCode.SUCCESS.equals(rankingListResult.getCode())) {
                Page<QuizRankingList> page = rankingListResult.getData();
                if (page != null && page.getList() != null && !page.getList().isEmpty()) {
                    JsonArray quizRankingList = new JsonParser().parse(new Gson().toJson(page.getList())).getAsJsonArray();
                    result.addProperty("count", page.getCount());
                    result.add("quizRankingList", quizRankingList);
                } else {
                    result.addProperty("count", 0);
                }
            }else {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            
            if (userId > 0) {
                Result<QuizRankingList> userRankingResult = quizActivityService.getUserQuizRanking(0, userId);
                if (userRankingResult != null && CommonStateCode.SUCCESS.equals(userRankingResult.getCode())) {
                    QuizRankingList userRanking = userRankingResult.getData();
                    result.addProperty("nickname", userRanking.getNickname());
                    result.addProperty("portrait", userRanking.getPortrait());
                    result.addProperty("ranking", userRanking.getRanking());
                }else {
                    result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                    return result;
                }
            }
            
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            log.error("Module Error:", e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        
        return result;
    }
    
    /**
     * 获取答题比赛预告信息[H5用]【51050404】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject getQuizNextInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        
        QuizActivityService quizActivityService = (QuizActivityService) MelotBeanFactory.getBean("quizActivityService");
        try {
            Result<QuizActivity> quizActivityResult = quizActivityService.getNextQuizActivity();
            if (quizActivityResult != null && CommonStateCode.SUCCESS.equals(quizActivityResult.getCode())) {
                QuizActivity quizActivity = quizActivityResult.getData();
                if (quizActivity == null) {
                    result.addProperty("bonus", 1000000);
                } else {
                    result.addProperty("bonus", quizActivity.getBonus());
                    result.addProperty("nextTime", quizActivity.getStartTime().getTime());
                    result.addProperty("activityName", quizActivity.getQuizActivityTitle());
                    result.addProperty("activityPoster", quizActivity.getQuizActivityPoster());
                }
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            }else {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
            }
        } catch (Exception e) {
            log.error("Module Error:", e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        return result;
    }
}

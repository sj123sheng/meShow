package com.melot.kkcx.functions;

import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.google.gson.JsonObject;
import com.ibatis.sqlmap.client.SqlMapClient;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ParameterKeys;
import com.melot.kktv.util.StringUtil;
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
    
    private static final Logger LOGGER = Logger.getLogger(QuizFunctions.class);
    
    /**
     * 获取答题用户信息【51050405】
     * 
     *      userId
     *      nickname
     *      portrait
     *      gender
     *      registerTime
     *      parentId
     * 
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject getQuizUserInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        Transaction t;

        // 获取用户基本信息
        UserProfile userProfile = null;
        t = Cat.getProducer().newTransaction("MCall", "com.melot.kkcore.user.service.KkUserService.getUserProfile");
        try {
            KkUserService kkUserService = (KkUserService) MelotBeanFactory.getBean("kkUserService");
            userProfile = kkUserService.getUserProfile(userId);
            t.setStatus(Transaction.SUCCESS);
        } catch (Exception e) {
            Cat.getProducer().logError(e);// 用log4j记录系统异常，以便在Logview中看到此信息
            t.setStatus(e);
        } finally {
            t.complete();
        }
        if (userProfile == null || StringUtil.strIsNull(userProfile.getNickName())) {
            JsonObject reResult = new JsonObject();
            reResult.addProperty("TagCode", TagCodeEnum.USER_NOT_EXIST);
            return reResult;
        }
        
        result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
        result.addProperty("userId", userId);
        result.addProperty("nickname", userProfile.getNickName());
        if (userProfile.getPortrait() != null) {
            result.addProperty("portrait", userProfile.getPortrait());
        }
        result.addProperty("gender", userProfile.getGender());
        
        // 获取用户注册的分享者信息
        try {
            SqlMapClient kkgameSqlMapClient = (SqlMapClient) MelotBeanFactory.getBean("sqlMapClient_pg_kkgame");
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) kkgameSqlMapClient.queryForObject("User.getUserFeedbackRelationInfo", userId);
            if (map != null && !map.isEmpty()) {
                if (map.containsKey("parentId") && map.get("parentId") != null) {
                    result.addProperty("parentId", (Integer) map.get("parentId"));
                }
                if (map.containsKey("registerTime") && map.get("registerTime") != null) {
                    result.addProperty("registerTime", ((Date) map.get("registerTime")).getTime());
                }
            }
        } catch (Exception e) {
            LOGGER.error("getUserFeedbackRelationInfo exception: " + userId, e);
        }
        
        return result;
    }
    
}

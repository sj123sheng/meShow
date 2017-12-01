package com.melot.kkcx.functions;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.kkactivity.driver.domain.NewUserTask;
import com.melot.kkactivity.driver.domain.NewUserTaskGift;
import com.melot.kkactivity.driver.service.NewUserTaskService;
import com.melot.kktv.base.CommonStateCode;
import com.melot.kktv.base.Result;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ParameterKeys;
import com.melot.kktv.util.SecurityFunctions;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * Title: NewUserTaskFunctions
 * <p>
 * Description: 新手引导相关接口
 * </p>
 * 
 * @author <a href="mailto:anwen.wei@melot.cn">魏安稳</a>
 * @version V1.0
 * @since 2017年11月29日 下午6:13:17
 */
public class NewUserTaskFunctions {
    private static final Logger LOGGER = Logger.getLogger(NewUserTaskFunctions.class);
    
    /**
     * 获取用户列表[52050301]
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject getNewUserTasks(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        // 安全校验
        JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
        if(rtJO != null) {
            return rtJO;
        }
        
        JsonObject result = new JsonObject();
        
        // Token 校验
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
        
        JsonArray newUserTasks = new JsonArray();
        try {
            NewUserTaskService newUserTaskService = (NewUserTaskService) MelotBeanFactory.getBean("newUserTaskService");
            Result<List<NewUserTask>> taskResut = newUserTaskService.getNewUserTasksByUserId(userId);
            
            if (taskResut == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
            
            if (CommonStateCode.SUCCESS.equals(taskResut.getCode())) {
                List<NewUserTask> tasks = taskResut.getData();
                for (NewUserTask newUserTask : tasks) {
                    JsonObject task = new JsonObject();
                    task.addProperty("newUserTaskId", newUserTask.getNewUserTaskId());
                    task.addProperty("state", newUserTask.getState());
                    
                    newUserTasks.add(task);
                }
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Module Error:newUserTaskService.getNewUserTasksByUserId(%s)", userId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
        
        result.add("newUserTasks", newUserTasks);
        result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 用户完成任务【52050302】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject finishNewUserTask(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        // 安全校验
        JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
        if(rtJO != null) {
            return rtJO;
        }
        
        JsonObject result = new JsonObject();
        
        // Token 校验
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        int newUserTaskId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            newUserTaskId = CommonUtil.getJsonParamInt(jsonObject, "newUserTaskId", 0, "5205030201", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            NewUserTaskService newUserTaskService = (NewUserTaskService) MelotBeanFactory.getBean("newUserTaskService");
            Result<List<NewUserTaskGift>> taskGiftResut = newUserTaskService.finishNewUserTask(userId, newUserTaskId);
            if (taskGiftResut == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            String code = taskGiftResut.getCode();
            if ("02".equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, "5205030202");
                return result;
            } else if ("01".equals(code)) {
                result.addProperty(ParameterKeys.TAG_CODE, "5205030203");
                return result;
            } else if (CommonStateCode.SUCCESS.equals(code)) {
                List<NewUserTaskGift> newUserTaskGifts = taskGiftResut.getData();
                JsonArray gifts = new JsonArray();
                for (NewUserTaskGift newUserTaskGift : newUserTaskGifts) {
                    JsonObject gift = new JsonObject();
                    gift.addProperty("giftId", newUserTaskGift.getGiftId());
                    gift.addProperty("count", newUserTaskGift.getCount());
                    
                    gifts.add(gift);
                }
                
                result.add("gifts", gifts);
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
                return result;
            } else {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }
        } catch (Exception e) {
            LOGGER.error(String.format("Module Error:newUserTaskService.finishNewUserTask(%s, %s)", userId, newUserTaskId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

}

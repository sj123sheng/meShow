/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2017
 */
package com.melot.kkcx.functions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.goldcoin.domain.RespUserPrize;
import com.melot.goldcoin.domain.UserLotteryPrize;
import com.melot.goldcoin.service.GoldcoinService;
import com.melot.kktv.model.Task;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.PlatformEnum;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.module.api.exceptions.MelotModuleException;
import com.melot.module.task.driver.domain.ConfCheckInReward;
import com.melot.module.task.driver.domain.ConfTaskReward;
import com.melot.module.task.driver.domain.GetUserTaskListResp;
import com.melot.module.task.driver.domain.GetUserTaskRewardResp;
import com.melot.module.task.driver.domain.UserTask;
import com.melot.module.task.driver.service.TaskInterfaceService;
import com.melot.sdk.core.util.MelotBeanFactory;

/**
 * Title: UserTaskFunctions
 * <p>
 * Description: UserTaskFunctions
 * </p>
 * 
 * @author 林端端<a href="mailto:duanduan.lin@melot.cn" />
 * @version V1.0
 * @since 2017年11月27日 下午4:37:34
 */
public class UserTaskFunctions {
    
    private static Logger logger = Logger.getLogger(UserTaskFunctions.class);
    
    /**
     * 获取用户任务列表(51010301)
     * 
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getUserTaskList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        // 获取参数
        int userId, platform, appId, versionCode;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, null, 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, 1, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 1, Integer.MAX_VALUE);
            versionCode = CommonUtil.getJsonParamInt(jsonObject, "v", 0, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        result = getUserTaskInfoList(userId, platform, appId, versionCode);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 完成任务列表中的任务(51010302)
     * 
     * @param jsonObject 请求对象
     * @param checkTag 是否验证token标记
     * @return
     */
    public JsonObject finishTask(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        // 获取参数
        int userId, taskId, platform, appId, versionCode;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            taskId = CommonUtil.getJsonParamInt(jsonObject, "taskId", 0, "5101030201", 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, 1, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 1, Integer.MAX_VALUE);
            versionCode = CommonUtil.getJsonParamInt(jsonObject, "v", 0, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        TaskInterfaceService taskInterfaceService = (TaskInterfaceService) MelotBeanFactory.getBean("taskInterfaceService");
        try {
            taskInterfaceService.finishUserTask(userId, taskId, platform, appId);
        } catch (MelotModuleException e) {
            switch (e.getErrCode()) {
            case 101:
                // 该任务不存在
                result.addProperty("TagCode", "5101030204");
                break;
            
            case 102:
                // 该任务不可完成
                result.addProperty("TagCode", "5101030203");
                break;

            case 103:
                // 该任务已完成
                result.addProperty("TagCode", "5101030202");
                break;

            default:
                // 调用存储过程未得到正常结果(详情查看日志) 
                result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
                break;
            }
            
            return result;
        } catch (Exception e) {
            // 调用存储过程未得到正常结果(详情查看日志) 
            result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
            return result;
        }
        
        result = getUserTaskInfoList(userId, platform, appId, versionCode);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    /**
     * 领取任务奖励(51010303)
     * 
     * @param jsonObject 请求对象
     * @param checkTag 是否验证token标记
     * @return
     */
    public JsonObject getReward(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        // 该接口需要验证token,未验证的返回错误码
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        // 获取参数
        int userId, taskId, platform, appId, versionCode;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "05220002", 1, Integer.MAX_VALUE);
            taskId = CommonUtil.getJsonParamInt(jsonObject, "taskId", 0, "05220004", 1, Integer.MAX_VALUE);
            platform = CommonUtil.getJsonParamInt(jsonObject, "platform", PlatformEnum.WEB, null, 1, Integer.MAX_VALUE);
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 1, Integer.MAX_VALUE);
            versionCode = CommonUtil.getJsonParamInt(jsonObject, "v", 0, null, Integer.MIN_VALUE, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        TaskInterfaceService taskInterfaceService = (TaskInterfaceService) MelotBeanFactory.getBean("taskInterfaceService");
        GetUserTaskRewardResp resp;
        try {
            resp = taskInterfaceService.updateUserTaskReward(userId, taskId, platform, appId, false);
        } catch (MelotModuleException e) {
            switch (e.getErrCode()) {
                
            case 102:
                // 不存在该任务
                result.addProperty("TagCode", "5101030302");
                break;

            case 103:
                // 任务未完成
                result.addProperty("TagCode", "5101030303");
                break;

            case 105:
                // 奖励已经发放
                result.addProperty("TagCode", "5101030304");
                break;

            default:
                // 调用存储过程未得到正常结果(详情查看日志) 
                result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
                break;
            }
            
            return result;
        } catch (Exception e) {
            // 调用存储过程未得到正常结果(详情查看日志) 
            result.addProperty("TagCode", TagCodeEnum.IRREGULAR_RESULT);
            return result;
        }
        
        result = getUserTaskInfoList(userId, platform, appId, versionCode);
        result.addProperty("goldCoin", resp.getGoldCoin());
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
    
    private JsonObject getUserTaskInfoList(int userId, int platform, int appId, int versionCode) {
        JsonObject result = new JsonObject();
        TaskInterfaceService taskInterfaceService = (TaskInterfaceService) MelotBeanFactory.getBean("taskInterfaceService");
        GetUserTaskListResp resp = null;
        try {
            resp = taskInterfaceService.getUserTaskList(userId, platform, appId, versionCode);
        } catch (MelotModuleException e) {
            
        } catch (Exception e) {
            logger.error("TaskInterfaceService.getgetUserTaskList(" + userId + ", " + platform + ", " + appId + ") execute exception", e);
            return result;
        }
        
        if (resp != null) {
            List<UserTask> list = resp.getUserTasks();
            if (list != null && list.size() > 0) {
                JsonArray taskArr = new JsonArray();
                Task task;
                for (UserTask userTask : list) {
                    task = new Task();
                    if (userTask.getTaskOrder() != null) {
                        task.setOrder(userTask.getTaskOrder());
                    }
                    if (userTask.getTaskName() != null) {
                        task.setTaskDesc(userTask.getTaskName());
                    }
                    if (userTask.getTaskId() != null) {
                        task.setTaskId(userTask.getTaskId());
                        if (userTask.getStatus() != null) {
                            task.setStatus(userTask.getStatus());
                        }
                    }
                    if (userTask.getTaskReward() != null) {
                        if (userTask.getTaskId() == 10000027 || userTask.getTaskId() == 10000028) {
                            task.setTaskReward(userTask.getGetGoldCoin() + userTask.getTaskReward());
                        } else {
                            task.setTaskReward(userTask.getTaskReward());
                        }
                    }
                    if (userTask.getVersionCode() != null) {
                        task.setVersionCode(userTask.getVersionCode());
                    }
                    if (userTask.getGetGoldCoin() != null) {
                        task.setGetGoldCoin(userTask.getGetGoldCoin());
                    }
                    
                    taskArr.add(task.toJsonObject());
                }
                result.add("userTaskList", taskArr);
            }
            
            for (Map.Entry<String, ConfCheckInReward> entry : resp.getCheckinRewardList().entrySet()) {
                JsonObject jObj = new JsonObject();
                String taskId = entry.getKey();
                
                ConfCheckInReward confCheckInReward = entry.getValue();
                JsonArray checkinReward = new JsonArray();
                for (ConfTaskReward confTaskReward : confCheckInReward.getCheckinReward()) {
                    JsonObject reward = new JsonObject();
                    reward.addProperty(String.valueOf(confTaskReward.getContiniuDays()), confTaskReward.getRewardCount());
                    checkinReward.add(reward);
                }
                jObj.add("checkinReward", checkinReward);
                String checkInStr = confCheckInReward.getSignInDays();
                if (checkInStr != null) {
                    String[] weeklyCheckInDays = checkInStr.split(",");
                    ArrayList<Integer> signInDaysList = new ArrayList<Integer>();
                    for (String day : weeklyCheckInDays) {
                        signInDaysList.add(Integer.parseInt(day));
                    }
                    jObj.add("signInDays", new Gson().toJsonTree(signInDaysList).getAsJsonArray());
                }
                jObj.addProperty("checkedDays", confCheckInReward.getCheckedDays());
                jObj.addProperty("indexDay", confCheckInReward.getIndexDay());
                jObj.addProperty("isSuspend", confCheckInReward.getSignOffDays());
                result.add("checkIn_" + taskId + "_info", jObj);
            }
        }
        return result;
    }
    
    /**
     * 任务抽奖(充值)(51010304)
     * 
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject lottery(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        // 获取参数
        int userId;
        boolean isDraw = false;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            TaskInterfaceService taskInterfaceService = (TaskInterfaceService) MelotBeanFactory.getBean("taskInterfaceService");
            isDraw = taskInterfaceService.isDraw(userId);
            if (isDraw && TagCodeEnum.SUCCESS.equals(taskInterfaceService.updateDraw(userId))){
                GoldcoinService goldcoinService = (GoldcoinService) MelotBeanFactory.getBean("goldcoinService");
                RespUserPrize respUserPrize = goldcoinService.lottery(userId);
                if (respUserPrize!= null && respUserPrize.getTagCode().equals(TagCodeEnum.SUCCESS)) {
                    result.addProperty("prizeId", respUserPrize.getPrizeId());
                    result.addProperty("prizeName", respUserPrize.getPrizeName());
                    result.addProperty("prizeCount", respUserPrize.getPrizeCount());
                    result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                } else {
                    result.addProperty("TagCode", "5101030402");
                }
            } else {
                result.addProperty("TagCode", "5101030401");
            }
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        
        return result;
    }
    
    /**
     * 充值抽奖资格校验(51010306)
     * 
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject isChargeLotteryDraw(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        
        // 获取参数
        int userId;
        boolean isDraw;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            TaskInterfaceService taskInterfaceService = (TaskInterfaceService) MelotBeanFactory.getBean("taskInterfaceService");
            isDraw = taskInterfaceService.isDraw(userId);
            result.addProperty("isDraw", isDraw ? 1:0);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        
        return result;
    }
    
    /**
     * 获取充值抽奖列表 (51010307)
     * 
     * @param jsonObject 请求对象
     * @param checkTag 是否验证token标记
     * @return 
     */
    public JsonObject getLotteryPrizeList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        int count;
        
        JsonObject result = new JsonObject();
        try {
            count = CommonUtil.getJsonParamInt(jsonObject, "count", 10, null, 1, Integer.MAX_VALUE);
        } catch(CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        } catch(Exception e) {
            result.addProperty("TagCode", TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            GoldcoinService goldcoinService = (GoldcoinService) MelotBeanFactory.getBean("goldcoinService");
            JsonArray lotteryPrizeList = new JsonArray();
            List<UserLotteryPrize> userLotteryPrizeList = goldcoinService.getUserLotteryPrizeList(count);
            if (userLotteryPrizeList != null) {
                for (UserLotteryPrize userLotteryPrize : userLotteryPrizeList) {
                    JsonObject jsonObj = new JsonObject();
                    jsonObj.addProperty("userId", userLotteryPrize.getUserId());
                    jsonObj.addProperty("prizeId", userLotteryPrize.getPrizeId());
                    jsonObj.addProperty("prizeCount", userLotteryPrize.getPrizeCount());
                    jsonObj.addProperty("prizeName", userLotteryPrize.getPrizeName());
                    if (userLotteryPrize.getPrizeDesc() != null) {
                        jsonObj.addProperty("prizeDesc", userLotteryPrize.getPrizeDesc());
                    }
                    jsonObj.addProperty("lotteryDate", userLotteryPrize.getLotteryDate().getTime());
                    lotteryPrizeList.add(jsonObj);
                }
            }
           
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            result.add("lotteryPrizeList", lotteryPrizeList);
        } catch (Exception e) {
            logger.error("goldcoinService.getUserLotteryPrizeList(" + count + ") return exception.", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        
        return result;
    }

}

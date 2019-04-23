package com.melot.kkcx.functions;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.common.melot_utils.CollectionUtils;
import com.melot.kk.config.api.domain.ConfBootMessageDTO;
import com.melot.kk.config.api.service.ConfigInfoService;
import com.melot.kk.recharge.api.service.RechargeService;
import com.melot.kkactivity.driver.domain.NewUserTask;
import com.melot.kkactivity.driver.domain.NewUserTaskGift;
import com.melot.kkactivity.driver.service.NewUserTaskService;
import com.melot.kkcore.user.api.ShowMoneyHistory;
import com.melot.kkcore.user.api.UserAssets;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kktv.base.CommonStateCode;
import com.melot.kktv.base.Result;
import com.melot.kktv.redis.HotDataSource;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ParameterKeys;
import com.melot.kktv.util.SecurityFunctions;
import com.melot.kktv.util.TagCodeEnum;
import com.melot.sdk.core.util.MelotBeanFactory;
import com.melot.storehouse.domain.RespMsg;
import com.melot.storehouse.service.StorehouseService;

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
    
    private final String EXCHANGE_NEWUSERGIFT_KEY = "exchangeNewUserGift_%s";
    
    @Resource
    RechargeService rechargeService;
    
    @Resource
    KkUserService kkUserService;
    
    @Resource
    StorehouseService storehouseService;
    
    @Resource
    ConfigInfoService configInfoService;
    
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
    
    /**
     * 兑换新手礼物（51050302）
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject exchangeNewUserGift(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
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
        
        try {
            int count = (int) HotDataSource.incTempDataString(String.format(EXCHANGE_NEWUSERGIFT_KEY, userId), 1, 30*24*60*60);
            if (count <= 1) {
                int requiredMoney = 1000;
                ShowMoneyHistory showMoneyHistory = new ShowMoneyHistory();
                long showMoneyBalance = kkUserService.getUserAssets(userId).getShowMoney();
                if (showMoneyBalance <= 0 || requiredMoney <= 0 || showMoneyBalance < requiredMoney) {
                    result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.USER_MONEY_SHORTNESS);
                    return result;
                }
                showMoneyHistory.setConsumeAmount((int) requiredMoney);
                showMoneyHistory.setUserId(userId);
                showMoneyHistory.setType(101);
                showMoneyHistory.setDtime(new Date());
                showMoneyHistory.setProductDesc("新手首冲礼物");
                UserAssets userAssets = kkUserService.decUserAssets(userId, requiredMoney, 0, showMoneyHistory);
                if (userAssets == null) {
                    LOGGER.error("用户" + userId + "兑换新手礼物扣[" + requiredMoney + "]秀币, 添加秀币流水失败");
                    result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.USER_MONEY_SHORTNESS);
                    return result;
                } else {
                    int giftId = 40002578;
                    RespMsg respMsg = storehouseService.addUserStorehouse(giftId, userId, 1, 15, "新手首冲兑换礼物");
                    if (respMsg == null || respMsg.getRespCode() != 0) {
                        LOGGER.error("用户" + userId + "新手首冲兑换礼物[" + giftId + "]失败");
                    } else {
                        result.addProperty("giftId", giftId);
                        result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
                    }
                }
            } else {
                result.addProperty(ParameterKeys.TAG_CODE, "5105030201");
            }
        } catch (Exception e) {
            LOGGER.error(String.format("NewUserTaskFunctions.exchangeNewUserGift(%s)", userId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        
        return result;
    }
    
    /**
     * 获取新手引导消息文案(51050303)
     * 
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getNewUserBootMessageList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        JsonObject result = new JsonObject();
        
        try {
            JsonArray bootMessageList = new JsonArray();
            List<ConfBootMessageDTO> confBootMessageDTOList = configInfoService.listConfBootMessages();
            if (CollectionUtils.isNotEmpty(confBootMessageDTOList)) {
                for (ConfBootMessageDTO confBootMessageDTO : confBootMessageDTOList) {
                    JsonObject jsonObj = new JsonObject();
                    jsonObj.addProperty("content", confBootMessageDTO.getContent());
                    jsonObj.addProperty("messageType", confBootMessageDTO.getMessageType());
                    jsonObj.addProperty("messageOrder", confBootMessageDTO.getMessageOrder());
                    jsonObj.addProperty("gender", confBootMessageDTO.getGender());
                    bootMessageList.add(jsonObj);
                }
            }
            
            result.add("bootMessageList", bootMessageList);
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            LOGGER.error("Error getNewUserBootMessageList()", e);
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        return result;
    }

}

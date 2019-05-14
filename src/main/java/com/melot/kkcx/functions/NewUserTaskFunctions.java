package com.melot.kkcx.functions;

import java.util.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.melot.activity.common.driver.service.ActivityCommonService;
import com.melot.kkcore.user.api.UserRegistry;
import com.melot.kkcore.user.api.exception.ShowMoneyLackException;
import com.melot.kkcx.model.NewUserTaskReward;
import com.melot.kkcx.util.AwardConfig;
import com.melot.kkgame.redis.RankingListSource;
import com.melot.kkgame.redis.support.RedisException;
import com.melot.kktv.util.confdynamic.GiftInfoConfig;
import com.melot.room.gift.dto.GiftDTO;
import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.common.melot_utils.CollectionUtils;
import com.melot.kk.config.api.domain.ConfBootMessageDTO;
import com.melot.kk.config.api.service.ConfigInfoService;
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
import org.springframework.beans.factory.annotation.Autowired;

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

    /**
     * 记录用户任务的汇总信息
     */
    public static final String NEW_COMER_TASK_INFO_PREX = "new_comer:task_info_";

    /**
     * 记录用户任务的单个完成或者领取信息
     * stat: 0 未完成
     * 1 已完成,未领取奖励
     * 2 已领取奖励
     */
    public static final String NEW_COMER_TASK_DETAIL_PREX = "new_comer:task_detail_";

    /**
     * 一周(毫秒)
     */
    private static final long ONE_WEEK_IN_MILLS = 1000L * 7 * 24 * 3600;

    private static final String[] GIFT_HOLDERS = {"40000099", "40000102", "40001025", "40000306", "40001237"};

    private static final Set<String> GIFT_HOLDER = new HashSet<String>() {
        {
            add("40000099");
            add("40000102");
            add("40001025");
            add("40000306");
            add("40001237");
        }
    };

    private static final Map<Integer, GiftDTO> GIFT_VALUE = new HashMap<Integer, GiftDTO>();

    Random random = new Random();

    @Resource
    KkUserService kkUserService;

    @Resource
    StorehouseService storehouseService;

    @Resource
    ConfigInfoService configInfoService;

    @Autowired
    RankingListSource rankingListSource;

    @Resource
    ActivityCommonService activityCommonService;

    /**
     * 获取用户列表(52050301)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject getNewUserTasks(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        // 安全校验
        JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
        if (rtJO != null) {
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
     * 用户完成任务(52050302)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     * @throws Exception
     */
    public JsonObject finishNewUserTask(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
        // 安全校验
        JsonObject rtJO = SecurityFunctions.checkSignedValue(jsonObject);
        if (rtJO != null) {
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
     * 兑换新手礼物(51050302)
     *
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
            int count = (int) HotDataSource.incTempDataString(String.format(EXCHANGE_NEWUSERGIFT_KEY, userId), 1, 30 * 24 * 60 * 60);
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
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
        } catch (Exception e) {
            LOGGER.error("Error getNewUserBootMessageList()", e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
        }
        return result;
    }

    /**
     * 用户7天新手任务,获取任务状态信息初始化游戏(88009100)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getTaskInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) throws Exception {
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

        UserRegistry userRegistry = kkUserService.getUserRegistry(userId);
        if (userRegistry == null || (System.currentTimeMillis() - userRegistry.getRegisterTime() > ONE_WEEK_IN_MILLS)) {
            //用户注册时间超过7天
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.AMOUNT_MISSING);
            result.addProperty("ErrMsg", "register time lager than 7days");
            return result;
        }

        JsonObject details = new JsonObject();

        Map<String, String> taskStat = rankingListSource.hgetAll(NEW_COMER_TASK_INFO_PREX + userId);
        transformTaskStat(taskStat, result);
        Map<String, String> taskDetail = rankingListSource.hgetAll(NEW_COMER_TASK_DETAIL_PREX + userId);
        for (Map.Entry<String, String> entry : taskDetail.entrySet()) {
            details.addProperty(entry.getKey(), Integer.valueOf(entry.getValue()));
        }

        result.add("details", details);
        result.addProperty("registerTime", userRegistry.getRegisterTime());
        result.addProperty("currentTime", System.currentTimeMillis());
        result.addProperty("actorId", getSuggestActorId());
        result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);

        return result;
    }

    /**
     * 用户领取奖励(88009101)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getPrize(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        int userId;
        String taskCode;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            taskCode = CommonUtil.getJsonParamString(jsonObject, "taskCode", null, TagCodeEnum.USERID_MISSING, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        UserRegistry userRegistry = kkUserService.getUserRegistry(userId);
        if (userRegistry == null || (System.currentTimeMillis() - userRegistry.getRegisterTime() > ONE_WEEK_IN_MILLS)) {
            //用户注册时间超过7天
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.AMOUNT_MISSING);
            result.addProperty("ErrMsg", "register time lager than 7days");
            return result;
        }

        String detailKey = NEW_COMER_TASK_DETAIL_PREX + userId;
        //获取任务状态码
        String flag = rankingListSource.hget(detailKey, taskCode);
        //添加奖励项
        NewUserTaskReward newUserTaskReward = AwardConfig.getConfig(taskCode);

        if (!"1".equals(flag) || newUserTaskReward == null) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.WECAHTPUBLIC_RETURN_ERR);
            result.addProperty("ErrMsg", "task state error");
            return result;
        }

        //任务状态置为2
        rankingListSource.hset(detailKey, taskCode, "2");
        sendAwardLog(userId, Integer.valueOf(taskCode));
        int type = newUserTaskReward.getAwardType();
        if (type < 7) {
            //增加用户积分
            rankingListSource.hincrBy(NEW_COMER_TASK_INFO_PREX + userId, "point", 1);
            int awardId = newUserTaskReward.getAwardId();
            int amount = newUserTaskReward.getAmount();
            switch (type) {
                case 1:
                    activityCommonService.addStoreHouse(awardId, userId, amount, "新用户任务[" + taskCode + "]奖励", "new_comer");
                    break;
                case 2:
                    activityCommonService.sendCar(userId, awardId, amount, "新用户任务[" + taskCode + "]奖励", "new_comer");
                    break;
                case 3:
                    activityCommonService.sendMedal(userId, awardId, amount, "新用户任务[" + taskCode + "]奖励", "new_comer");
                    break;
                case 4:
                    activityCommonService.insertSendVip(userId, amount, 1, "新用户任务[" + taskCode + "]奖励", "new_comer");
                    break;
                case 5:
                    activityCommonService.insertSendVip(userId, amount, 2, "新用户任务[" + taskCode + "]奖励", "new_comer");
                    break;
                default:
                    break;
            }
        }

        result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
        return result;
    }


    /**
     * 用户7天新手任务,使用秀币获取库存礼物(88009102)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject buyHalfGift(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        int userId;
        String taskCode;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            taskCode = CommonUtil.getJsonParamString(jsonObject, "taskCode", null, TagCodeEnum.USERID_MISSING, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        UserRegistry userRegistry = kkUserService.getUserRegistry(userId);
        if (userRegistry == null || (System.currentTimeMillis() - userRegistry.getRegisterTime() > ONE_WEEK_IN_MILLS)) {
            //用户注册时间超过7天
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.AMOUNT_MISSING);
            result.addProperty("ErrMsg", "register time lager than 7days");
            return result;
        }
        String detailKey = NEW_COMER_TASK_DETAIL_PREX + userId;
        //查询奖励代号
        NewUserTaskReward newUserTaskReward = AwardConfig.getConfig(taskCode);
        boolean buyFlag = false;
        if (newUserTaskReward != null && newUserTaskReward.getAwardType() == 7) {
            //半价奖励权,只能购买一次
            //获取任务状态码
            String flag = rankingListSource.hget(detailKey, taskCode);
            if ("1".equals(flag)) {
                buyFlag = buyGift(userId, newUserTaskReward.getAwardId());
                //购买成功, 防止扣钱失败造成任务状态异常
                if (buyFlag) {
                    //任务状态置为2
                    rankingListSource.hset(detailKey, taskCode, "2");
                    sendAwardLog(userId, Integer.valueOf(taskCode));
                }
            } else {
                //任务状态错误
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.WECAHTPUBLIC_RETURN_ERR);
                result.addProperty("ErrMsg", "gift only can be buy one times");
                return result;
            }
        } else { //直接购买礼物
            if (!GIFT_HOLDER.contains(taskCode)) {
                //任务状态错误
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.WECAHTPUBLIC_RETURN_ERR);
                result.addProperty("ErrMsg", "giftid not permission");
                return result;
            } else {
                buyFlag = buyGift(userId, Integer.valueOf(taskCode));
            }
        }
        if (buyFlag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
        } else { //购买失败,金额不足
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.WECAHTPUBLIC_RESULT_ERR);
            result.addProperty("ErrMsg", "lack money");
        }

        return result;
    }

    /**
     * 用户领取积分宝箱(88009103)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getBox(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }
        int userId;
        String taskCode;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            taskCode = CommonUtil.getJsonParamString(jsonObject, "taskCode", null, TagCodeEnum.USERID_MISSING, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        UserRegistry userRegistry = kkUserService.getUserRegistry(userId);
        if (userRegistry == null || (System.currentTimeMillis() - userRegistry.getRegisterTime() > ONE_WEEK_IN_MILLS)) {
            //用户注册时间超过7天
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.AMOUNT_MISSING);
            result.addProperty("ErrMsg", "register time lager than 7days");
            return result;
        }

        String detailKey = NEW_COMER_TASK_DETAIL_PREX + userId;
        //获取任务状态码
        String flag = rankingListSource.hget(detailKey, taskCode);
        //添加奖励项
        NewUserTaskReward newUserTaskReward = AwardConfig.getConfig(taskCode);
        //任务未领取状态,可以领取奖励
        if (!"2".equals(flag)) {
            int requirPoint = 1000;
            if ("25000001".equals(taskCode)) {
                requirPoint = 50;
            } else if ("25000002".equals(taskCode)) {
                requirPoint = 99;
            }
            String infoKey = NEW_COMER_TASK_INFO_PREX + userId;
            String pointInRedis = rankingListSource.hget(infoKey, "point");
            int point = pointInRedis == null ? 0 : Integer.valueOf(pointInRedis);
            if (point < requirPoint) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.WECAHTPUBLIC_RESULT_ERR);
                result.addProperty("ErrMsg", "lack point");
                return result;
            }
            //任务状态置为2
            rankingListSource.hset(detailKey, taskCode, "2");
            int awardId = newUserTaskReward.getAwardId();
            int amount = newUserTaskReward.getAmount();
            //闯关勇士勋章（30天）
            if ("25000001".endsWith(taskCode)) {
                activityCommonService.sendMedal(userId, awardId, amount, "新用户任务[" + taskCode + "]奖励", "new_comer");
            }
            //闯关勇士座驾（30天）
            else if ("25000002".endsWith(taskCode)) {
                activityCommonService.sendCar(userId, awardId, amount, "新用户任务[" + taskCode + "]奖励", "new_comer");
            }
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
        } else {
            //任务状态错误
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.WECAHTPUBLIC_RETURN_ERR);
            result.addProperty("ErrMsg", "task state error");
        }

        return result;
    }

    /**
     * 购买礼物
     */
    private boolean buyGift(int userId, int giftId) {
        if (!GIFT_VALUE.containsKey(giftId)) {
            GIFT_VALUE.put(giftId, GiftInfoConfig.getGiftInfo(giftId));
        }
        boolean flag = true;
        int consumeMoney = GIFT_VALUE.get(giftId).getSendPrice();
        try {
            ShowMoneyHistory showMoneyHistory = new ShowMoneyHistory();
            showMoneyHistory.setUserId(userId);
            showMoneyHistory.setConsumeAmount(consumeMoney);
            showMoneyHistory.setToUserId(0);
            showMoneyHistory.setIncomeAmount(0);
            showMoneyHistory.setType(55);
            showMoneyHistory.setTypeDesc("新手7天任务,购买礼物[" + giftId + "]");
            kkUserService.decUserAssets(userId, consumeMoney, 0, showMoneyHistory);
            activityCommonService.addStoreHouse(giftId, userId, 1, "新用户任务,购买[" + giftId + "]礼物", "new_comer");
        } catch (ShowMoneyLackException e) {
            flag = false;
            LOGGER.error("call kkUserService decUserAssets fail,ShowMoneyLack, userId :" + userId + ", del money :" + consumeMoney, e);
        }
        return flag;
    }

    /**
     * 获取任务完成进度信息
     */
    private void transformTaskStat(Map<String, String> taskStat, JsonObject result) {
        result.addProperty("point", getAsInt(taskStat, "point"));//累计积分
        result.addProperty("recharge", getAsInt(taskStat, "recharge"));//累计充值
        result.addProperty("followCount", getAsInt(taskStat, "followCount"));//累计关注主播数
        result.addProperty("shareCount", getAsInt(taskStat, "shareCount"));//累计分享直播间数
        result.addProperty("managerCount", getAsInt(taskStat, "managerCount"));//累计成为主播管理数
        result.addProperty("carCount", getAsInt(taskStat, "carCount"));//累计获得座驾数
        result.addProperty("medalCount", getAsInt(taskStat, "medalCount"));//累计获得勋章数
        result.addProperty("richLevel", getAsInt(taskStat, "richLevel"));//累计获得勋章数
        int guardDay = getAsInt(taskStat, "guard");//累计开通守护天数
        result.addProperty("guard", (int) (guardDay / 30));//累计开通守护月
        result.addProperty("runway", getAsInt(taskStat, "runway"));//累计上跑道次数
        result.addProperty("onlineTime", getAsInt(taskStat, "onlineTime"));//累计观看时长(分)
        result.addProperty("watchCount", getAsInt(taskStat, "watchCount"));//累计观看超过10分钟的主播间数
        int giftPeriods = getAsInt(taskStat, "giftPeriods"); //送礼天详情
        result.addProperty("giftDays", getBit(giftPeriods));
        result.addProperty("giftDaysDetail", getBitDetail(giftPeriods));
        int loginPeriods = getAsInt(taskStat, "loginPeriods"); //登陆天详情
        result.addProperty("loginDetail", getBitDetail(loginPeriods));
    }

    private static int getAsInt(Map<String, String> map, String key) {
        return map.containsKey(key) ? Integer.parseInt(map.get(key)) : 0;
    }

    private int getBit(int input) {
        int count = 0;
        while (input > 0) {
            input = input & (input - 1);
            count++;
        }
        return count;
    }

    private String getBitDetail(int input) {
        final int length = 8;
        StringBuilder details = new StringBuilder(length);
        String temp = Integer.toBinaryString(input);
        for (int i = 0; i < length - temp.length(); i++) {
            details.append("0");
        }
        details.append(temp);
        return details.toString();
    }

    private int getSuggestActorId() {
        try {
            Set<String> actors = rankingListSource.smembers("new_comer:live_actor");
            if (actors != null && actors.size() > 0) {
                int rn = random.nextInt(actors.size());
                int i = 0;
                for (String e : actors) {
                    if (i == rn) {
                        return Integer.valueOf(e);
                    }
                    i++;
                }
            }
        } catch (RedisException e) {
            LOGGER.error(e);
        }
        return 7530131;
    }

    private void sendAwardLog(int userId, int taskCode) {
        JsonObject log = new JsonObject();
        log.addProperty("dtime", System.currentTimeMillis());
        log.addProperty("userId", userId);
        log.addProperty("taskCode", taskCode);
        rankingListSource.lpush("new-comer-award-log-list", log.toString());
    }
}

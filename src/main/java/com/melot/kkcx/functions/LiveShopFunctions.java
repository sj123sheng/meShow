package com.melot.kkcx.functions;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import com.melot.kk.liveshop.api.service.LiveShopService;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ParameterKeys;
import com.melot.kktv.util.TagCodeEnum;

public class LiveShopFunctions {

    private static Logger logger = Logger.getLogger(LiveShopFunctions.class);
    
    private static final String PARAM_START = "start";

    @Resource
    LiveShopService liveShopService;
    /**
     * 主播生成订单【51060502】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject actorAddOrder(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int actorId;
        int start;
        int num;
        try {
            actorId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.ACTOR_ID, 0, "5105050101", 1, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, PARAM_START, 0, null, 0, Integer.MAX_VALUE);
            num = CommonUtil.getJsonParamInt(jsonObject, "num", 20, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：crowdFundingService.getUnSelectGoods(actorId=%s, start=%s, num=%s);", actorId, start, num), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取订单详情【51060503】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getOrderInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int actorId;
        int start;
        int num;
        try {
            actorId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.ACTOR_ID, 0, "5105050101", 1, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, PARAM_START, 0, null, 0, Integer.MAX_VALUE);
            num = CommonUtil.getJsonParamInt(jsonObject, "num", 20, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：crowdFundingService.getUnSelectGoods(actorId=%s, start=%s, num=%s);", actorId, start, num), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取订单列表【51060504】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getOrders(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int actorId;
        int start;
        int num;
        try {
            actorId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.ACTOR_ID, 0, "5105050101", 1, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, PARAM_START, 0, null, 0, Integer.MAX_VALUE);
            num = CommonUtil.getJsonParamInt(jsonObject, "num", 20, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：crowdFundingService.getUnSelectGoods(actorId=%s, start=%s, num=%s);", actorId, start, num), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 商家发货【51060505】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject deliveryOrder(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int actorId;
        int start;
        int num;
        try {
            actorId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.ACTOR_ID, 0, "5105050101", 1, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, PARAM_START, 0, null, 0, Integer.MAX_VALUE);
            num = CommonUtil.getJsonParamInt(jsonObject, "num", 20, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：crowdFundingService.getUnSelectGoods(actorId=%s, start=%s, num=%s);", actorId, start, num), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 用户设置收货地址【51060506】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject applySendOrder(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int actorId;
        int start;
        int num;
        try {
            actorId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.ACTOR_ID, 0, "5105050101", 1, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, PARAM_START, 0, null, 0, Integer.MAX_VALUE);
            num = CommonUtil.getJsonParamInt(jsonObject, "num", 20, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：crowdFundingService.getUnSelectGoods(actorId=%s, start=%s, num=%s);", actorId, start, num), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 用户申请退款【51060507】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject applyRefund(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int actorId;
        int start;
        int num;
        try {
            actorId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.ACTOR_ID, 0, "5105050101", 1, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, PARAM_START, 0, null, 0, Integer.MAX_VALUE);
            num = CommonUtil.getJsonParamInt(jsonObject, "num", 20, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：crowdFundingService.getUnSelectGoods(actorId=%s, start=%s, num=%s);", actorId, start, num), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 用户确认收货【51060509】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject receiveOrder(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int actorId;
        int start;
        int num;
        try {
            actorId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.ACTOR_ID, 0, "5105050101", 1, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, PARAM_START, 0, null, 0, Integer.MAX_VALUE);
            num = CommonUtil.getJsonParamInt(jsonObject, "num", 20, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：crowdFundingService.getUnSelectGoods(actorId=%s, start=%s, num=%s);", actorId, start, num), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 校验是否支付成功【51060510】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject checkOrderState(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int actorId;
        int start;
        int num;
        try {
            actorId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.ACTOR_ID, 0, "5105050101", 1, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, PARAM_START, 0, null, 0, Integer.MAX_VALUE);
            num = CommonUtil.getJsonParamInt(jsonObject, "num", 20, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：crowdFundingService.getUnSelectGoods(actorId=%s, start=%s, num=%s);", actorId, start, num), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

}

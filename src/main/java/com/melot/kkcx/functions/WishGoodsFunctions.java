package com.melot.kkcx.functions;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.melot.kk.crowdfunding.api.constant.WishOrderEnum;
import com.melot.kk.crowdfunding.api.dto.ActorWishGoodsDTO;
import com.melot.kk.crowdfunding.api.dto.ActorWishOrderCountDTO;
import com.melot.kk.crowdfunding.api.dto.ActorWishOrderDTO;
import com.melot.kk.crowdfunding.api.dto.BuyWishConfigInfoDTO;
import com.melot.kk.crowdfunding.api.dto.UserWishHistDTO;
import com.melot.kk.crowdfunding.api.dto.WishGoodsInfoDTO;
import com.melot.kk.crowdfunding.api.service.CrowdFundingService;
import com.melot.kk.recharge.api.dto.ConfPaymentInfoDto;
import com.melot.kkcore.actor.api.ActorInfo;
import com.melot.kkcore.actor.service.ActorService;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kkcx.transform.WishGoodsTF;
import com.melot.kktv.base.CommonStateCode;
import com.melot.kktv.base.Page;
import com.melot.kktv.base.Result;
import com.melot.kktv.payment.conf.PaymentInfoConf;
import com.melot.kktv.util.AppIdEnum;
import com.melot.kktv.util.CommonUtil;
import com.melot.kktv.util.ConfigHelper;
import com.melot.kktv.util.DateUtil;
import com.melot.kktv.util.ParameterKeys;
import com.melot.kktv.util.TagCodeEnum;

/**
 * Title: WishGoodsFunctions
 * <p>
 * Description: 心愿商品【众筹】相关接口
 * </p>
 * 
 * @author <a href="mailto:anwen.wei@melot.cn">魏安稳</a>
 * @version V1.0
 * @since 2018年3月13日 下午2:17:50
 */
public class WishGoodsFunctions {
    private static Logger logger = Logger.getLogger(WishGoodsFunctions.class);
    
    @Resource
    private CrowdFundingService crowdFundingService;
    
    @Resource
    private ActorService actorService;
    
    @Resource
    private KkUserService kkUserService;
    
    private static final String PARAM_START = "start";
    private static final String PARAM_COUNT = "count";
    private static final String PARAM_WISH_GOODS_ID = "wishGoodsId";
    private static final String PARAM_WISH_COUNT = "wishCount";
    private static final String PARAM_WISH_GOODS_RICH_LIST = "wishGoodsRichList";
    private static final String PARAM_WISH_GOODS_LIST = "wishGoodsList";
    
    /**
     * 主播获取所有没有勾选的心愿商品列表【51050501】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getAllWishGoods(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
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
            Result<Page<WishGoodsInfoDTO>> moduleResult = crowdFundingService.getUnSelectGoods(actorId, start, num);
            if (moduleResult == null || !CommonStateCode.SUCCESS.equals(moduleResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            Page<WishGoodsInfoDTO> page = moduleResult.getData();
            result.addProperty(PARAM_COUNT, page.getCount());
            JsonArray wishGoodsList = new JsonArray();
            if (page.getList() == null) {
                result.add(PARAM_WISH_GOODS_LIST, wishGoodsList);
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
                return result;
            }
            for (WishGoodsInfoDTO wishGoodsInfoDTO : page.getList()) {
                JsonObject wishGoodsInfoJson = WishGoodsTF.wishGoods2Json(wishGoodsInfoDTO);
                wishGoodsList.add(wishGoodsInfoJson);
            }
            result.add(PARAM_WISH_GOODS_LIST, wishGoodsList);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：crowdFundingService.getUnSelectGoods(actorId=%s, start=%s, num=%s);", actorId, start, num), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 主播编辑我的心愿【51050502】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject setUserWishGoods(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }
        int userId;
        int wishGoodsId;
        int type;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            wishGoodsId = CommonUtil.getJsonParamInt(jsonObject, PARAM_WISH_GOODS_ID, 0, "5105050201", 0, Integer.MAX_VALUE);
            type = CommonUtil.getJsonParamInt(jsonObject, "type", 1, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            // 移除
            if (type == 2) {
                Result<Boolean> moduleResult = crowdFundingService.unSelectGoods(userId, wishGoodsId);
                if (moduleResult == null) {
                    result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                    return result;
                }
                if ("2".equals(moduleResult.getCode())) {
                    result.addProperty(ParameterKeys.TAG_CODE, "5105050202");
                    return result;
                } else if ("3".equals(moduleResult.getCode())) {
                    result.addProperty(ParameterKeys.TAG_CODE, "5105050203");
                    return result;
                } else if (!CommonStateCode.SUCCESS.equals(moduleResult.getCode())){
                    result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                    return result;
                }
            // 勾选
            }else {
                Result<Boolean> moduleResult = crowdFundingService.selectGoods(userId, wishGoodsId);
                if (moduleResult == null) {
                    result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                    return result;
                }
                if ("2".equals(moduleResult.getCode())) {
                    result.addProperty(ParameterKeys.TAG_CODE, "5105050207");
                    return result;
                } else if ("3".equals(moduleResult.getCode())) {
                    result.addProperty(ParameterKeys.TAG_CODE, "5105050205");
                    return result;
                } else if ("4".equals(moduleResult.getCode())) {
                    result.addProperty(ParameterKeys.TAG_CODE, "5105050206");
                    return result;
                } else if (!CommonStateCode.SUCCESS.equals(moduleResult.getCode())){
                    result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                    return result;
                }
            }
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：setUserWishGoods(userId=%s, wishGoodsId=%s, type=%s)", userId, wishGoodsId, type), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取主播的心愿列表【51050503】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getUserWishGoods(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        String code01 = "5105050301";
        JsonObject result = new JsonObject();
        
        int actorId;
        int hasBar;
        int start;
        int num;
        try {
            actorId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.ACTOR_ID, 0, code01, 1, Integer.MAX_VALUE);
            hasBar = CommonUtil.getJsonParamInt(jsonObject, "hasBar", 0, null, 0, Integer.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, PARAM_START, 0, null, 0, Integer.MAX_VALUE);
            num = CommonUtil.getJsonParamInt(jsonObject, "num", 0, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        try {
            ActorInfo actorInfo = actorService.getActorInfoById(actorId);
            if (actorInfo == null) {
                result.addProperty(ParameterKeys.TAG_CODE, code01);
                return result;
            }
            result.addProperty(ParameterKeys.ACTOR_ID, actorId);
            result.addProperty(ParameterKeys.NICKNAME, actorInfo.getNickName());
        } catch (Exception e) {
            logger.error("Module Error: actorService.getActorInfoById(" + actorId + ")", e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
        
        try {
            Result<Page<ActorWishGoodsDTO>> moduleResult = crowdFundingService.pageGetSelectGoods(actorId,start,num);
            if (moduleResult == null || !CommonStateCode.SUCCESS.equals(moduleResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            Page<ActorWishGoodsDTO> page = moduleResult.getData();
            result.addProperty("maxWishGoodsCount", Integer.parseInt(moduleResult.getMsg()));
            result.addProperty("count",page.getCount());
            JsonArray wishGoodsList = new JsonArray();
            if (page.getList() == null) {
                result.add(PARAM_WISH_GOODS_LIST, wishGoodsList);
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
                return result;
            }
            for (ActorWishGoodsDTO actorWishGoodsDTO : page.getList()) {
                JsonObject actorWishGoodsJson = WishGoodsTF.actorWishGoods2Json(actorWishGoodsDTO);
                if (hasBar == 0) {
                    actorWishGoodsJson.remove(PARAM_WISH_COUNT);
                }
                wishGoodsList.add(actorWishGoodsJson);
            }
            result.add(PARAM_WISH_GOODS_LIST, wishGoodsList);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error("Module Error：crowdFundingService.getSelectGoods(" + actorId + ")", e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取心愿详情【51050504】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getWishGoodsInfo(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }
        
        int actorId;
        int wishGoodsId;
        try {
            actorId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.ACTOR_ID, 0, "5105050401", 1, Integer.MAX_VALUE);
            wishGoodsId = CommonUtil.getJsonParamInt(jsonObject, PARAM_WISH_GOODS_ID, 0, "5105050402", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        ActorInfo actorInfo = null;
        try {
            actorInfo = actorService.getActorInfoById(actorId);
            if (actorInfo == null) {
                result.addProperty(ParameterKeys.TAG_CODE, "5105050401");
                return result;
            }
        } catch (Exception e) {
            logger.error("Module Error: ", e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
        
        try {
            Result<ActorWishGoodsDTO> moduleResult = crowdFundingService.getWishGoodsInfo(actorId, wishGoodsId);
            if (moduleResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            if ("2".equals(moduleResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, "5105050403");
                return result;
            } else if (!CommonStateCode.SUCCESS.equals(moduleResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            ActorWishGoodsDTO actorWishGoodsDTO = moduleResult.getData();
            result = WishGoodsTF.actorWishGoods2Json(actorWishGoodsDTO);
            result.addProperty(ParameterKeys.ACTOR_ID, actorId);
            result.addProperty(ParameterKeys.NICKNAME, actorInfo.getNickName());
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：crowdFundingService.getWishGoodsInfo(actorId=%s, wishGoodsId=%s)", actorId, wishGoodsId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取心愿贡献榜单【51050505】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getWishGoodsRichList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int actorId;
        int wishGoodsId;
        int start;
        int num;
        try {
            actorId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.ACTOR_ID, 0, "5105050501", 1, Integer.MAX_VALUE);
            wishGoodsId = CommonUtil.getJsonParamInt(jsonObject, PARAM_WISH_GOODS_ID, 0, "5105050502", 1, Integer.MAX_VALUE);
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
            Result<Page<UserWishHistDTO>> moduleResult = crowdFundingService.getWishGoodsRankingList(actorId, wishGoodsId, start, num);
            if (moduleResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            if ("2".equals(moduleResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, "5105050503");
                return result;
            } else if (!CommonStateCode.SUCCESS.equals(moduleResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            Page<UserWishHistDTO> page = moduleResult.getData();
            result.addProperty(PARAM_COUNT, page.getCount());
            JsonArray wishGoodsRichList = new JsonArray();
            if (page.getList() == null) {
                result.add(PARAM_WISH_GOODS_RICH_LIST, wishGoodsRichList);
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
                return result;
            }
            for (UserWishHistDTO userWishHistDTO : page.getList()) {
                UserProfile userProfile = kkUserService.getUserProfile(userWishHistDTO.getUserId());
                JsonObject userWishHistJson = new JsonObject();
                userWishHistJson.addProperty(ParameterKeys.USER_ID, userProfile.getUserId());
                userWishHistJson.addProperty(ParameterKeys.NICKNAME, userProfile.getNickName());
                userWishHistJson.addProperty("gender", userProfile.getGender());
                String portrait = userProfile.getPortrait();
                if (portrait != null && !portrait.startsWith(ConfigHelper.getHttpdir())
                        && !portrait.startsWith(ConfigHelper.getHttpdirUp())) {
                    portrait = ConfigHelper.getHttpdir() + portrait;
                }
                userWishHistJson.addProperty("portrait", portrait);
                userWishHistJson.addProperty(PARAM_WISH_COUNT, userWishHistDTO.getWishCount());
                
                wishGoodsRichList.add(userWishHistJson);
            }
            result.add(PARAM_WISH_GOODS_RICH_LIST, wishGoodsRichList);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：crowdFundingService.getWishGoodsRankingList(actorId=%s, wishGoodsId=%s, start=%s, num=%s)", actorId, wishGoodsId, start, num), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取主播心愿订单【51050507】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getUserWishOrders(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }
        int userId;
        int state;
        int start;
        int num;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, ParameterKeys.USER_ID, 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            state = CommonUtil.getJsonParamInt(jsonObject, "state", 0, null, 0, Integer.MAX_VALUE);
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
            Result<Page<ActorWishOrderDTO>> moduleResult = crowdFundingService.getActorWishOrders(userId, state, start, num);
            if (moduleResult == null || !CommonStateCode.SUCCESS.equals(moduleResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            Page<ActorWishOrderDTO> page = moduleResult.getData();
            result.addProperty(PARAM_COUNT, page.getCount());
            JsonArray wishOrders = new JsonArray();
            if (page.getList() == null) {
                result.add("wishOrders", wishOrders);
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
                return result;
            }
            for (ActorWishOrderDTO actorWishOrderDTO : page.getList()) {
                JsonObject actorWishOrderJson = new JsonObject();
                actorWishOrderJson.addProperty("wishOrderId", actorWishOrderDTO.getWishOrderId());
                actorWishOrderJson.addProperty(PARAM_WISH_GOODS_ID, actorWishOrderDTO.getWishGoodsId());
                actorWishOrderJson.addProperty("wishGoodsName", actorWishOrderDTO.getWishGoodsName());
                actorWishOrderJson.addProperty("wishGoodsPrice", actorWishOrderDTO.getWishGoodsPrice());
                actorWishOrderJson.addProperty("goodsCount", actorWishOrderDTO.getWishGoodsCount());
                actorWishOrderJson.addProperty("state", actorWishOrderDTO.getOrderState());
                actorWishOrderJson.addProperty("type", actorWishOrderDTO.getOrderType());
                
                JsonObject wishGoodsIcon = new JsonObject();
                wishGoodsIcon.addProperty("web", actorWishOrderDTO.getWishGoodsIcon());
                wishGoodsIcon.addProperty("phone", actorWishOrderDTO.getWishGoodsIcon());
                actorWishOrderJson.add("wishGoodsIcon", wishGoodsIcon);
                
                if (actorWishOrderDTO.getOrderType().equals(WishOrderEnum.ORDER_TYPE_REAL) 
                        && actorWishOrderDTO.getOrderState() > WishOrderEnum.ORDER_STATE_WAIT_APPLY) {
                    JsonObject addrInfo = new JsonObject();
                    addrInfo.addProperty("consigneeName", actorWishOrderDTO.getConsigneeName());
                    addrInfo.addProperty("consigneeMobile", actorWishOrderDTO.getConsigneeMobile());
                    addrInfo.addProperty("detailAddress", actorWishOrderDTO.getDetailAddress());
                    actorWishOrderJson.add("addrInfo", addrInfo);
                    actorWishOrderJson.addProperty("waybillNumber", actorWishOrderDTO.getWaybillNumber());
                    actorWishOrderJson.addProperty("courierCompany", actorWishOrderDTO.getCourierCompany());
                    Long sendTime = null;
                    if (actorWishOrderDTO.getSendTime() != null) {
                        sendTime = actorWishOrderDTO.getSendTime().getTime();
                    }
                    actorWishOrderJson.addProperty("sendTime", sendTime);
                }
                if (actorWishOrderDTO.getOrderType().equals(WishOrderEnum.ORDER_TYPE_VIRTUAL) 
                        && actorWishOrderDTO.getOrderState().equals(WishOrderEnum.ORDER_STATE_HAS_SEND)) {
                    actorWishOrderJson.addProperty("goodsUrl", actorWishOrderDTO.getWishGoodsUrl());
                    actorWishOrderJson.addProperty("goodsDesc", actorWishOrderDTO.getWishGoodsDesc());
                }
                actorWishOrderJson.addProperty("addTime", actorWishOrderDTO.getAddTime().getTime());
                
                wishOrders.add(actorWishOrderJson);
            }
            result.add("wishOrders", wishOrders);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：crowdFundingService.getActorWishOrders(userId=%s, state=%s, start=%s, num=%s)", userId, state, start, num), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 主播申请发货【51050508】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject applyDelivery(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }
        int userId;
        int wishOrderId;
        int addressId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, "5105050801", 1, Integer.MAX_VALUE);
            wishOrderId = CommonUtil.getJsonParamInt(jsonObject, "wishOrderId", 0, "5105050802", 1, Integer.MAX_VALUE);
            addressId = CommonUtil.getJsonParamInt(jsonObject, "addressId", 0, "5105050803", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            Result<Boolean> moduleResult = crowdFundingService.applyDelivery(userId, wishOrderId, addressId);
            if (moduleResult == null || !CommonStateCode.SUCCESS.equals(moduleResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：crowdFundingService.applyDelivery(userId=%s, wishOrderId=%s, addressId=%s)", userId, wishOrderId, addressId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取心愿账单记录【51050509】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getWishBill(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }
        
        int userId;
        int start;
        int offset;
        long startTime;
        long endTime;
        int type;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            startTime = CommonUtil.getJsonParamLong(jsonObject, "startTime", getMonthFirstDay(), null, DateUtil.getDayBeginTime(System.currentTimeMillis()) - 180 * 24 * 3600 * 1000L, Long.MAX_VALUE);
            endTime = CommonUtil.getJsonParamLong(jsonObject, "endTime", getMonthLastDay(startTime), null, startTime, Long.MAX_VALUE);
            start = CommonUtil.getJsonParamInt(jsonObject, PARAM_START, 0, null, 0, Integer.MAX_VALUE);
            offset = CommonUtil.getJsonParamInt(jsonObject, "offset", 10, null, 1, Integer.MAX_VALUE);
            type = CommonUtil.getJsonParamInt(jsonObject, "type", 1, null, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            Result<Page<UserWishHistDTO>> moduleResult = crowdFundingService.getUserWishBill(userId, start, offset, startTime, endTime, type);
            if (moduleResult == null || !CommonStateCode.SUCCESS.equals(moduleResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            Page<UserWishHistDTO> page = moduleResult.getData();
            result.addProperty(PARAM_COUNT, page.getCount());
            JsonArray wishGoodsList = new JsonArray();
            if (page.getList() == null) {
                result.add(PARAM_WISH_GOODS_LIST, wishGoodsList);
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
                return result;
            }
            for (UserWishHistDTO userWishHistDTO : page.getList()) {
                JsonObject userWishHistJson = new JsonObject();
                int relationId = 0;
                String nickname = "";
                if (type == 1) {
                    relationId = userWishHistDTO.getActorId();
                    nickname = actorService.getActorInfoById(relationId).getNickName();
                } else {
                    relationId = userWishHistDTO.getUserId();
                    nickname = kkUserService.getUserProfile(relationId).getNickName();
                }
                userWishHistJson.addProperty(PARAM_WISH_GOODS_ID, userWishHistDTO.getConfActorWishId());
                userWishHistJson.addProperty("wishGoodsName", userWishHistDTO.getWishGoodsName());
                userWishHistJson.addProperty("sendTime", userWishHistDTO.getAddTime().getTime());
                userWishHistJson.addProperty("relationId", relationId);
                userWishHistJson.addProperty(ParameterKeys.NICKNAME, nickname);
                userWishHistJson.addProperty(PARAM_WISH_COUNT, userWishHistDTO.getWishCount());
                JsonObject wishGoodsIcon = new JsonObject();
                wishGoodsIcon.addProperty("web", userWishHistDTO.getWishGoodsIcon());
                wishGoodsIcon.addProperty("phone", userWishHistDTO.getWishGoodsIcon());
                userWishHistJson.add("wishGoodsIcon", wishGoodsIcon);
                wishGoodsList.add(userWishHistJson);
            }
            result.add(PARAM_WISH_GOODS_LIST, wishGoodsList);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：crowdFundingService.getUserWishBill(userId=%s, start=%s, offset=%s, startTime=%s, endTime=%s, type=%s)"
                    , userId, start, offset, startTime, endTime, type)
                    , e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取心愿支付相关配置【51050510】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getBuyWishConfig(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        int appId;
        int version;
        
        try {
            appId = CommonUtil.getJsonParamInt(jsonObject, "a", AppIdEnum.AMUSEMENT, null, 1, 5);
            version = CommonUtil.getJsonParamInt(jsonObject, "version", 0, null, 0, Integer.MAX_VALUE);
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            Result<BuyWishConfigInfoDTO> moduleResult = crowdFundingService.getBuyWishConfigInfo();
            if (moduleResult == null || !CommonStateCode.SUCCESS.equals(moduleResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            BuyWishConfigInfoDTO buyWishConfigInfoDTO = moduleResult.getData();
            JsonArray selectWishList = new Gson().toJsonTree(buyWishConfigInfoDTO.getSelectCount()).getAsJsonArray();
            result.add("selectWishList", selectWishList);
            
            // 返回充值类型配置
            List<ConfPaymentInfoDto> paymentList = PaymentInfoConf.getPaymentList(appId, version);
            JsonArray paymentConfigs = new JsonArray();
            if (paymentList != null) {
                for (ConfPaymentInfoDto confPaymentInfo : paymentList) {
                    if (buyWishConfigInfoDTO.getPaymentModes().contains(confPaymentInfo.getPaymentMode().intValue())) {
                        JsonObject confPaymentInfoJson = new JsonParser().parse(new Gson().toJson(confPaymentInfo)).getAsJsonObject();
                        paymentConfigs.add(confPaymentInfoJson);
                    }
                }
            }
            result.add("paymentConfigs", paymentConfigs);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：getBuyWishConfig(appId=%s, version=%s)", appId, version), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 判断心愿是否充值成功【51050511】
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject checkWishBillState(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        
        String orderNo;
        try {
            orderNo = CommonUtil.getJsonParamString(jsonObject, "orderNo", null, "5105051101", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }
        
        try {
            Result<Boolean> moduleResult = crowdFundingService.checkOrder(orderNo);
            if (moduleResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            
            if (CommonStateCode.SUCCESS.equals(moduleResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            } else if ("2".equals(moduleResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, "5105051101");
            } else {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            }
            
            return result;
        } catch (Exception e) {
            logger.error("Module Error：crowdFundingService.checkOrder(" + orderNo + ");", e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获取心愿商品详情【51050512】
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getWishGoods(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        int wishGoodsId;
        try {
            wishGoodsId = CommonUtil.getJsonParamInt(jsonObject, PARAM_WISH_GOODS_ID, 0, "5105051201", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            Result<WishGoodsInfoDTO> moduleResult = crowdFundingService.getWishGoodsInfoDTO(wishGoodsId);
            if (moduleResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }

            if ("2".equals(moduleResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, "5105051202");
                return result;
            } else if (!CommonStateCode.SUCCESS.equals(moduleResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            WishGoodsInfoDTO wishGoodsInfoDTO = moduleResult.getData();
            result = WishGoodsTF.wishGoodsInfoDTO2Json(wishGoodsInfoDTO);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            result.addProperty("expressPrice",0);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：crowdFundingService.getWishGoodsInfo(wishGoodsId=%s)", wishGoodsId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 查询许愿瓶是否有红点【51050513】
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject queryIsRedPoint(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }

        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            boolean moduleResult = crowdFundingService.queryIsRedPoint(userId);
            int isRedPoint = moduleResult ? 1 : 0;
            result.addProperty("isRedPoint", isRedPoint);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：crowdFundingService.queryIsRedPoint(userId=%s)", userId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 查询库存是否充足【51050514】
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject queryIsStockEnough(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        int actorId;
        int wishGoodsId;
        int wishCount;
        try {
            actorId = CommonUtil.getJsonParamInt(jsonObject, "actorId", 0, "5105051401", 1, Integer.MAX_VALUE);
            wishGoodsId = CommonUtil.getJsonParamInt(jsonObject, "wishGoodsId", 0, "5105051402", 1, Integer.MAX_VALUE);
            wishCount = CommonUtil.getJsonParamInt(jsonObject, "wishCount", 0, "5105051403", 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            Result<Boolean> moduleResult = crowdFundingService.queryIsStockEnough(actorId, wishGoodsId, wishCount);
            if (moduleResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            } else if ("1".equals(moduleResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, "5105051406");
                return result;
            } else if ("2".equals(moduleResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, "5105051404");
                return result;
            } else if ("3".equals(moduleResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, "5105051405");
                result.addProperty("maxWishCount", Integer.parseInt(moduleResult.getMsg()));
                return result;
            } else if (!CommonStateCode.SUCCESS.equals(moduleResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：crowdFundingService.getNeedActorLevel()"), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获得主播各种类型的心愿订单数量【51050515】
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getUserWishOrderCount(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }

        int userId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty(ParameterKeys.TAG_CODE, e.getErrCode());
            return result;
        } catch (Exception e) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.PARAMETER_PARSE_ERROR);
            return result;
        }

        try {
            Result<ActorWishOrderCountDTO> moduleResult = crowdFundingService.getActorWishOrderCount(userId);
            if (moduleResult == null) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_RETURN_NULL);
                return result;
            }

            if (!CommonStateCode.SUCCESS.equals(moduleResult.getCode())) {
                result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
            ActorWishOrderCountDTO wishOrderCountDTO = moduleResult.getData();
            result.addProperty("unApplyCount", wishOrderCountDTO.getUnApplyCount());
            result.addProperty("unSendCount", wishOrderCountDTO.getUnSendCount());
            result.addProperty("hasSendCount", wishOrderCountDTO.getHasSendCount());
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：crowdFundingService.getActorWishOrderCount(userId=%s)", userId), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
    }

    /**
     * 获得解锁许愿瓶功能所需明星等级【51050516】
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getNeedActorLevel(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        try {
            int needActorLevel = crowdFundingService.getNeedActorLevel();

            result.addProperty("needActorLevel", needActorLevel);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            logger.error(String.format("Module Error：crowdFundingService.getNeedActorLevel()"), e);
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.MODULE_UNKNOWN_RESPCODE) ;
            return result;
        }
    }
    
    private static long getMonthFirstDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }
    
    private static long getMonthLastDay(long startTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(startTime));
        
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTimeInMillis();
    }
}

package com.melot.kkcx.functions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.melot.activity.common.driver.domain.MessageInfo;
import com.melot.activity.common.driver.service.ActivityCommonService;
import com.melot.kk.liveshop.api.constant.DrawEndStatus;
import com.melot.kk.liveshop.api.dto.*;
import com.melot.kk.liveshop.api.service.LiveShopDrawService;
import com.melot.kk.liveshop.api.service.ProductService;
import com.melot.kkcore.user.api.UserProfile;
import com.melot.kkcore.user.service.KkUserService;
import com.melot.kkcx.service.GeneralService;
import com.melot.kktv.base.CommonStateCode;
import com.melot.kktv.base.Page;
import com.melot.kktv.base.Result;
import com.melot.kktv.domain.WorkVideoInfo;
import com.melot.kktv.service.WorkService;
import com.melot.kktv.util.*;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class LiveShopDrawFunctions {
    @Resource
    private KkUserService kkUserService;

    @Resource
    private ProductService productService;

    @Resource
    private LiveShopDrawService drawService;

    @Resource
    private ActivityCommonService activityCommonService;

    /**
     * 保存抽奖(51130101)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject saveDraw(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        Integer drawId = null;
        String drawName;
        String description;
        int prizeCount;
        long price;
        int numberOfDrawing;
        long expressPrice;
        int isGroup;
        long startTime;
        long drawingTime;
        String drawImg;
        String prizeImg;

        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, ParamCodeEnum.USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            JsonElement drawIdElement = jsonObject.get("drawId");
            if (drawIdElement != null) {
                drawId = drawIdElement.getAsInt();
            }
            drawName = CommonUtil.getJsonParamString(jsonObject, "drawName", null, ParamCodeEnum.DRAW_NAME.getErrorCode(), 1, 40);
            description = CommonUtil.getJsonParamString(jsonObject, "description", null, null, 0, 1000);
            prizeCount = CommonUtil.getJsonParamInt(jsonObject, "prizeCount", 0, ParamCodeEnum.PRIZE_COUNT.getErrorCode(), 1, 100);
            JsonElement priceJson = jsonObject.get("price");
            if (priceJson == null) {
                result.addProperty("TagCode", ParamCodeEnum.PRICE.getErrorCode());
                return result;
            }
            price = priceJson.getAsLong();
            startTime = CommonUtil.getJsonParamLong(jsonObject, "startTime", 0, ParamCodeEnum.START_TIME.getErrorCode(), 1, Long.MAX_VALUE);
            drawingTime = CommonUtil.getJsonParamLong(jsonObject, "drawingTime", 0, ParamCodeEnum.DRAWING_TIME.getErrorCode(), 1, Long.MAX_VALUE);
            numberOfDrawing = CommonUtil.getJsonParamInt(jsonObject, "numberOfDrawing", 0, ParamCodeEnum.NUMBER_OF_DRAWING.getErrorCode(), 1, 999999);
            expressPrice = CommonUtil.getJsonParamLong(jsonObject, "expressPrice", 0, ParamCodeEnum.EXPRESS_PRICE.getErrorCode(), 100, Long.MAX_VALUE);
            isGroup = CommonUtil.getJsonParamInt(jsonObject, "isGroup", 0, ParamCodeEnum.IS_GROUP.getErrorCode(), 0, 1);
            drawImg = CommonUtil.getJsonParamString(jsonObject, "drawImg", null, ParamCodeEnum.DRAW_IMG.getErrorCode(), 1, 5000);
            prizeImg = CommonUtil.getJsonParamString(jsonObject, "prizeImg", null, null, 0, 5000);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        if (GeneralService.hasSensitiveWords(userId, description)) {
            result.addProperty("TagCode", TagCodeEnum.DESCRIPTION_HAS_SENSITIVE_WORD);
            return result;
        }

        if (GeneralService.hasSensitiveWords(userId, drawName)) {
            result.addProperty("TagCode", TagCodeEnum.DRAW_NAME_HAS_SENSITIVE_WORD);
            return result;
        }

        if (drawId != null) {
            if (drawId <= 0) {
                result.addProperty("TagCode", TagCodeEnum.NOT_EXIST);
                return result;
            }
            String tagCode = this.updateDraw(userId, drawId, drawName, description, prizeCount, price, numberOfDrawing,
                    expressPrice, isGroup, startTime, drawingTime, drawImg, prizeImg);
            result.addProperty("TagCode", tagCode);
            return result;
        } else {
            LiveShopProductDetailDTOV2 productDetailDTO = new LiveShopProductDetailDTOV2();
            productDetailDTO.setActorId(userId);
            productDetailDTO.setProductName(drawName);
            if (!StringUtils.isEmpty(description)) {
                productDetailDTO.setProductDetailDesc(description);
            }
            productDetailDTO.setExpressPrice(expressPrice);
            productDetailDTO.setStockNum(prizeCount);
            List<LiveShopProductPictureDTO> imgList = this.getProductImgList(drawImg, 1);
            if (!CollectionUtils.isEmpty(imgList)) {
                productDetailDTO.setResourceUrl(imgList.get(0).getResourceUrl());
                productDetailDTO.setProductPictureDTOList(imgList);
            }
            if (!StringUtils.isEmpty(prizeImg)) {
                List<LiveShopProductPictureDTO> detailList = this.getProductImgList(prizeImg, 2);
                if (!CollectionUtils.isEmpty(detailList)) {
                    productDetailDTO.setProductPictureDTODetailList(detailList);
                }
            }

            ResDrawInfoDTO resDrawInfoDTO = new ResDrawInfoDTO();
            resDrawInfoDTO.setUserId(userId);
            resDrawInfoDTO.setPrizeCount(prizeCount);
            resDrawInfoDTO.setDrawPrice(price);
            resDrawInfoDTO.setNumberOfDrawing(numberOfDrawing);
            resDrawInfoDTO.setIsGroup(isGroup);
            Date startTimeDate = new Date(startTime);
            resDrawInfoDTO.setStartTime(startTimeDate);
            Date drawingTimeDate = new Date(drawingTime);
            resDrawInfoDTO.setDrawingTime(drawingTimeDate);

            Result<Boolean> drawResult = drawService.addDraw(resDrawInfoDTO, productDetailDTO);
            if (CommonStateCode.SUCCESS.equals(drawResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.SUCCESS);
                return result;
            } else {
                if ("SQL_ERROR".equals(drawResult.getCode())) {
                    result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
                    return result;
                } else if ("NOT_SELLER".equals(drawResult.getCode())) {
                    result.addProperty("TagCode", TagCodeEnum.NOT_EXIST_SALE_ACTOR);
                    return result;
                } else if ("RATE_ERROR".equals(drawResult.getCode())) {
                    result.addProperty("TagCode", TagCodeEnum.RATE_ERROR);
                    return result;
                } else if ("DRAW_PRICE_ERROR".equals(drawResult.getCode())) {
                    result.addProperty("TagCode", TagCodeEnum.AMOUNT_ERROR);
                    return result;
                } else if ("PRIZE_COUNT_ERROR".equals(drawResult.getCode())) {
                    result.addProperty("TagCode", TagCodeEnum.COUNT_ERROR);
                    return result;
                } else if ("NUMBER_OF_DRAWING_ERROR".equals(drawResult.getCode())) {
                    result.addProperty("TagCode", TagCodeEnum.COUNT_ERROR);
                    return result;
                } else if ("CAN_NOT_GROUP".equals(drawResult.getCode())) {
                    result.addProperty("TagCode", TagCodeEnum.CAN_NOT_GROUP);
                    return result;
                } else if ("TIME_ERROR".equals(drawResult.getCode())) {
                    result.addProperty("TagCode", TagCodeEnum.TIME_ERROR);
                    return result;
                } else if ("LESS_CURRENT_TIME".equals(drawResult.getCode())) {
                    result.addProperty("TagCode", TagCodeEnum.LESS_CURRENT_TIME);
                    return result;
                } else {
                    result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                    return result;
                }
            }
        }
    }

    private String updateDraw(int userId, Integer drawId, String drawName, String description, int prizeCount, long price,
            int numberOfDrawing, long expressPrice, int isGroup, long startTime, long drawingTime, String drawImg, String prizeImg) {
        ResDrawInfoDTO drawInfo = drawService.getDrawInfo(drawId);
        if (drawInfo == null) {
            return TagCodeEnum.NOT_EXIST;
        }
        int productId = drawInfo.getProductId();
        LiveShopProductDetailDTO liveShopProductDetailDTO = productService.getProductDetail(productId);
        if (liveShopProductDetailDTO == null) {
            return TagCodeEnum.PRODUCT_NOT_EXIST;
        }

        liveShopProductDetailDTO.setProductName(drawName);
        liveShopProductDetailDTO.setProductDetailDesc(description);
        liveShopProductDetailDTO.setStockNum(prizeCount);
        liveShopProductDetailDTO.setExpressPrice(expressPrice);

        List<LiveShopProductPictureDTO> imgList = this.getProductImgList(drawImg, 1);
        if (!CollectionUtils.isEmpty(imgList)) {
            String url = imgList.get(0).getResourceUrl();
            if (url.indexOf("!") > 0) {
                url = url.substring(0, url.lastIndexOf("!"));
            }
            liveShopProductDetailDTO.setResourceUrl(url);
            liveShopProductDetailDTO.setProductPictureDTOList(imgList);
        }
        if (!StringUtils.isEmpty(prizeImg)) {
            List<LiveShopProductPictureDTO> detailList = this.getProductImgList(prizeImg, 2);
            if (!CollectionUtils.isEmpty(detailList)) {
                liveShopProductDetailDTO.setProductPictureDTODetailList(detailList);
            }
        }

        Result<Boolean> productResult = productService.updateProduct(productId, liveShopProductDetailDTO);
        if (CommonStateCode.SUCCESS.equals(productResult.getCode())) {
            drawInfo.setPrizeCount(prizeCount);
            drawInfo.setDrawPrice(price);
            drawInfo.setNumberOfDrawing(numberOfDrawing);
            drawInfo.setIsGroup(isGroup);
            Date startTimeDate = new Date(startTime);
            drawInfo.setStartTime(startTimeDate);
            Date drawingTimeDate = new Date(drawingTime);
            drawInfo.setDrawingTime(drawingTimeDate);

            Result<Boolean> drawResult = drawService.updateDraw(drawId, userId, drawInfo);
            if (CommonStateCode.SUCCESS.equals(drawResult.getCode())) {
                return TagCodeEnum.SUCCESS;
            } else {
                if ("SQL_ERROR".equals(drawResult.getCode())) {
                    return TagCodeEnum.EXECSQL_EXCEPTION;
                } else if ("TIME_ERROR".equals(drawResult.getCode())) {
                    return TagCodeEnum.TIME_ERROR;
                } else if ("LESS_CURRENT_TIME".equals(drawResult.getCode())) {
                    return TagCodeEnum.LESS_CURRENT_TIME;
                } else if ("OVER_TIME".equals(drawResult.getCode())) {
                    return TagCodeEnum.TIME_ERROR;
                } else if ("END_STATUS".equals(drawResult.getCode())) {
                    return TagCodeEnum.END_STATUS;
                } else if ("NOT_EXIST".equals(drawResult.getCode())) {
                    return TagCodeEnum.NOT_EXIST;
                } else if ("PRIZE_COUNT_ERROR".equals(drawResult.getCode())) {
                    return TagCodeEnum.COUNT_ERROR;
                } else if ("NUMBER_OF_DRAWING_ERROR".equals(drawResult.getCode())) {
                    return TagCodeEnum.COUNT_ERROR;
                } else if ("CAN_NOT_GROUP".equals(drawResult.getCode())) {
                    return TagCodeEnum.CAN_NOT_GROUP;
                } else if ("DRAW_PRICE_ERROR".equals(drawResult.getCode())) {
                    return TagCodeEnum.AMOUNT_ERROR;
                } else if ("NO_PERMISSIONS".equals(drawResult.getCode())) {
                    return TagCodeEnum.PERMISSIONS_ERROR;
                } else {
                    return TagCodeEnum.MODULE_UNKNOWN_RESPCODE;
                }
            }
        } else {
            if ("PRODUCT_NOT_EXIST".equals(productResult.getCode())) {
                return TagCodeEnum.PRODUCT_NOT_EXIST;
            } else if ("PRODUCT_NOT_AVAILABLE".equals(productResult.getCode())) {
                return TagCodeEnum.NOT_VALID_PRODUCT;
            } else if ("NOT_SELLER".equals(productResult.getCode())) {
                return TagCodeEnum.NOT_EXIST_SALE_ACTOR;
            } else if ("SQL_ERROR".equals(productResult.getCode())) {
                return TagCodeEnum.EXECSQL_EXCEPTION;
            } else {
                return TagCodeEnum.MODULE_UNKNOWN_RESPCODE;
            }
        }
    }

    private List<LiveShopProductPictureDTO> getProductImgList(String url, int picType) {
        if (StringUtils.isEmpty(url)) {
            return null;
        }
        List<LiveShopProductPictureDTO> list = new ArrayList<>();
        String[] array = url.split(",");
        if (array != null && array.length > 0) {
            for (int i = 0; i < array.length; i++) {
                String item = array[i];
                LiveShopProductPictureDTO liveShopProductPictureDTO = new LiveShopProductPictureDTO();
                if (item.indexOf("!") > 0) {
                    item = item.substring(0, item.lastIndexOf("!"));
                }
                liveShopProductPictureDTO.setResourceUrl(item);
                liveShopProductPictureDTO.setPictureType(picType);
                liveShopProductPictureDTO.setSortNo(i + 1);
                // 获取图片宽高信息
                WorkVideoInfo imageInfo = WorkService.getImageInfoByHttp(item);
                if (imageInfo != null) {
                    liveShopProductPictureDTO.setPictureHeight(imageInfo.getHeight());
                    liveShopProductPictureDTO.setPictureWidth(imageInfo.getWidth());
                }
                list.add(liveShopProductPictureDTO);
            }
        }
        return list;
    }

    /**
     * 主播端抽奖列表(51130102)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getActorDrawList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        int pageIndex;
        int countPerPage;

        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, ParamCodeEnum.USER_ID.getErrorCode(), 0, Integer.MAX_VALUE);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 0, TagCodeEnum.PARAMETER_MISSING, 1, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 0, TagCodeEnum.PARAMETER_MISSING, 1, 200);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }
        JsonArray jsonArray = new JsonArray();
        Page<ResDrawInfoDTO> drawPage = drawService.getSellerAllDrawList(userId, pageIndex, countPerPage);
        if (!CollectionUtils.isEmpty(drawPage.getList())) {
            for (ResDrawInfoDTO resDrawInfoDTO : drawPage.getList()) {
                LiveShopProductDetailDTO productDetailDTO = productService.getProductDetail(resDrawInfoDTO.getProductId());
                if (productDetailDTO != null) {
                    JsonObject json = new JsonObject();
                    json.addProperty("drawId", resDrawInfoDTO.getDrawId());
                    int status = drawService.getDrawStatus(resDrawInfoDTO.getEndStatus(), resDrawInfoDTO.getStartTime(),
                            resDrawInfoDTO.getDrawingTime());
                    json.addProperty("status", status);
                    json.addProperty("drawName", productDetailDTO.getProductName());
                    json.addProperty("prizeCount", resDrawInfoDTO.getPrizeCount());
                    json.addProperty("startTime", resDrawInfoDTO.getStartTime().getTime());
                    json.addProperty("drawingTime", resDrawInfoDTO.getDrawingTime().getTime());
                    json.addProperty("isGroup", resDrawInfoDTO.getIsGroup());
                    JsonArray bannerArray = new JsonArray();
                    if (!CollectionUtils.isEmpty(productDetailDTO.getProductPictureDTOList())) {
                        for (LiveShopProductPictureDTO item : productDetailDTO.getProductPictureDTOList()) {
                            JsonObject productJson = new JsonObject();
                            productJson.addProperty("productUrl", item.getResourceUrl() + "!640x640");
                            productJson.addProperty("productUrlBig", item.getResourceUrl() + "!1280");
                            bannerArray.add(productJson);
                            break;
                        }
                    }
                    json.add("productImg", bannerArray);
                    jsonArray.add(json); 
                }
            }
        }
        result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
        result.add("list", jsonArray);
        result.addProperty("count", drawPage.getCount() != null ? drawPage.getCount() : 0);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 主播端获取抽奖详情(51130103)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getActorDrawDetail(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        if (!checkTag) {
            result.addProperty("TagCode", TagCodeEnum.TOKEN_NOT_CHECKED);
            return result;
        }

        int userId;
        int drawId;
        try {
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, ParamCodeEnum.USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            drawId = CommonUtil.getJsonParamInt(jsonObject, "drawId", 0, TagCodeEnum.PARAMETER_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }
        ResDrawInfoDTO resDrawInfoDTO = drawService.getDrawInfo(drawId);
        if (resDrawInfoDTO == null) {
            result.addProperty("TagCode", TagCodeEnum.NOT_EXIST);
            return result;
        }
        result.addProperty("actorId", resDrawInfoDTO.getUserId());
        if (resDrawInfoDTO.getUserId() != userId) {
            result.addProperty("TagCode", TagCodeEnum.PERMISSIONS_ERROR);
            return result;
        }
        LiveShopProductDetailDTO productDetailDTO = productService.getProductDetail(resDrawInfoDTO.getProductId());
        if (productDetailDTO == null) {
            result.addProperty("TagCode", TagCodeEnum.NOT_EXIST);
            return result;
        }

        JsonArray bannerArray = new JsonArray();
        if (!CollectionUtils.isEmpty(productDetailDTO.getProductPictureDTOList())) {
            for (LiveShopProductPictureDTO item : productDetailDTO.getProductPictureDTOList()) {
                JsonObject json = new JsonObject();
                json.addProperty("productUrl", item.getResourceUrl() + "!640x640");
                json.addProperty("productUrlBig", item.getResourceUrl() + "!1280");
                bannerArray.add(json);
            }
        }
        result.add("drawImgUrlsBannerUrls", bannerArray);

        result.addProperty("isGroup", resDrawInfoDTO.getIsGroup());
        result.addProperty("drawName", productDetailDTO.getProductName());
        result.addProperty("prizeCount", resDrawInfoDTO.getPrizeCount());
        result.addProperty("drawPrice", resDrawInfoDTO.getDrawPrice());
        result.addProperty("expressPrice", productDetailDTO.getExpressPrice());
        result.addProperty("startTime", resDrawInfoDTO.getStartTime().getTime());
        result.addProperty("drawingTime", resDrawInfoDTO.getDrawingTime().getTime());
        result.addProperty("numberOfDrawing", resDrawInfoDTO.getNumberOfDrawing());
        int status = drawService.getDrawStatus(resDrawInfoDTO.getEndStatus(), resDrawInfoDTO.getStartTime(),
                resDrawInfoDTO.getDrawingTime());
        result.addProperty("status", status);

        if (resDrawInfoDTO.getEndStatus() == DrawEndStatus.OVER) {
            JsonArray luckUserArray = new JsonArray();
            List<HistUserDrawDTO> luckUserList = drawService.getLuckUserList(drawId);
            if (!CollectionUtils.isEmpty(luckUserList)) {
                for (HistUserDrawDTO item : luckUserList) {
                    UserProfile userProfile = kkUserService.getUserProfile(item.getUserId());
                    if (userProfile != null) {
                        JsonObject json = new JsonObject();
                        json.addProperty("userId", item.getUserId());
                        if (userProfile.getPortrait() != null) {
                            json.addProperty("portrait", userProfile.getPortrait());
                        }
                        json.addProperty("nickname", userProfile.getNickName());
                        json.addProperty("gender", userProfile.getGender());
                        luckUserArray.add(json);
                    }
                }
            }
            result.add("luckUsers", luckUserArray);
        }

        String drawDesc = productDetailDTO.getProductDetailDesc();
        if (!StringUtils.isEmpty(drawDesc)) {
            result.addProperty("drawDesc", productDetailDTO.getProductDetailDesc());
        }
        JsonArray descArray = new JsonArray();
        if (!CollectionUtils.isEmpty(productDetailDTO.getProductPictureDTODetailList())) {
            for (LiveShopProductPictureDTO item : productDetailDTO.getProductPictureDTODetailList()) {
                JsonObject json = new JsonObject();
                json.addProperty("productDetailUrl", item.getResourceUrl() + "!512");
                json.addProperty("pictureWidth", item.getPictureWidth());
                json.addProperty("pictureHeight", item.getPictureHeight());
                descArray.add(json);
            }
        }
        result.add("prizeImgs", descArray);
        result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 获取参与抽奖人员列表(51130104)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getDrawUserList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();

        int drawId;
        int pageIndex;
        int countPerPage;

        try {
            drawId = CommonUtil.getJsonParamInt(jsonObject, "drawId", 0, TagCodeEnum.PARAMETER_MISSING, 1, Integer.MAX_VALUE);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 0, TagCodeEnum.PARAMETER_MISSING, 1, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 0, TagCodeEnum.PARAMETER_MISSING, 1, 200);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        JsonArray jsonArray = new JsonArray();
        Page<HistUserDrawDTO> page = drawService.getDrawOfUserList(drawId, pageIndex, countPerPage);
        if (page != null) {
            result.addProperty("count", page.getCount());
            if (!CollectionUtils.isEmpty(page.getList())) {
                for (HistUserDrawDTO item : page.getList()) {
                    UserProfile userProfile = kkUserService.getUserProfile(item.getUserId());
                    if (userProfile != null) {
                        JsonObject json = new JsonObject();
                        json.addProperty("userId", item.getUserId());
                        json.addProperty("nickname", userProfile.getNickName());
                        json.addProperty("gender", userProfile.getGender());
                        if (userProfile.getPortrait() != null) {
                            json.addProperty("portrait", userProfile.getPortrait());
                        }
                        json.addProperty("time", item.getCreateTime().getTime());
                        jsonArray.add(json);
                    }
                }
            }
        }
        result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
        result.add("list", jsonArray);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 直播间抽奖个数(51130105)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getLiveRoomDrawCount(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        int roomId;

        try {
            roomId = CommonUtil.getJsonParamInt(jsonObject, "roomId", 0, ParamCodeEnum.USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }
        ResDrawInfoDTO resDrawInfoDTO = drawService.getUserCurrentDraw(roomId);
        if (resDrawInfoDTO != null) {
            result.addProperty("drawId", resDrawInfoDTO.getDrawId());
        } else {
            result.addProperty("drawId", 0);
        }
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 直播间中奖结果(51130106)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getLiveRoomLuckUserList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        int userId;
        int drawId;

        try {
            drawId = CommonUtil.getJsonParamInt(jsonObject, "drawId", 0, TagCodeEnum.PARAMETER_MISSING, 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, ParamCodeEnum.USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        ResDrawInfoDTO resDrawInfoDTO = drawService.getDrawInfo(drawId);
        if (resDrawInfoDTO == null) {
            result.addProperty("TagCode", TagCodeEnum.NOT_EXIST);
            return result;
        }
        LiveShopProductDetailDTO liveShopProductDetailDTO = productService.getProductDetail(resDrawInfoDTO.getProductId());
        if (liveShopProductDetailDTO == null) {
            result.addProperty("TagCode", TagCodeEnum.NOT_EXIST_PRODUCT);
            return result;
        }

        result.addProperty("prizeDesc", liveShopProductDetailDTO.getProductName());
        result.addProperty("prizeCount", resDrawInfoDTO.getPrizeCount());

        HistUserDrawDTO isLuck = drawService.isLuckUser(drawId, userId);
        if (isLuck != null) {
            result.addProperty("isLuck", 1);
            if (!StringUtils.isEmpty(isLuck.getOrderNo())) {
                result.addProperty("orderNo", isLuck.getOrderNo());
            }
        } else {
            result.addProperty("isLuck", 0);
        }

        JsonArray jsonArray = new JsonArray();
        List<HistUserDrawDTO> list = drawService.getLuckUserList(drawId);
        if (!CollectionUtils.isEmpty(list)) {
            for (HistUserDrawDTO item : list) {
                UserProfile userProfile = kkUserService.getUserProfile(item.getUserId());
                if (userProfile != null) {
                    JsonObject json = new JsonObject();
                    json.addProperty("userId", item.getUserId());
                    json.addProperty("nickname", userProfile.getNickName());
                    json.addProperty("gender", userProfile.getGender());
                    if (userProfile.getPortrait() != null) {
                        json.addProperty("portrait", userProfile.getPortrait());
                    }
                    jsonArray.add(json);
                }
            }
        }
        result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
        result.add("list", jsonArray);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 用户抽奖详情(51130107)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getUserDrawDetail(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }

        int userId;
        int drawId;
        int groupId;

        try {
            drawId = CommonUtil.getJsonParamInt(jsonObject, "drawId", 0, TagCodeEnum.PARAMETER_MISSING, 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, TagCodeEnum.USERID_MISSING, 1, Integer.MAX_VALUE);
            groupId = CommonUtil.getJsonParamInt(jsonObject, "groupId", 0, null, 0, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }
        ResDrawInfoDTO resDrawInfoDTO = drawService.getDrawInfo(drawId);
        if (resDrawInfoDTO == null) {
            result.addProperty("TagCode", TagCodeEnum.NOT_EXIST);
            return result;
        }
        result.addProperty("actorId", resDrawInfoDTO.getUserId());
        LiveShopProductDetailDTO liveShopProductDetailDTO = productService.getProductDetail(resDrawInfoDTO.getProductId());
        if (liveShopProductDetailDTO == null) {
            result.addProperty("TagCode", TagCodeEnum.NOT_EXIST);
            return result;
        }

        HistUserDrawDTO histUserDrawDTO = drawService.getUserDraw(drawId, userId);
        if (histUserDrawDTO == null) {
            result.addProperty("isUserIn", 0);
            if (groupId <= 0) {
                result.addProperty("groupStatus", 1);
            } else {
                int groupUserCount = drawService.getGroupUserCount(groupId);
                if (groupUserCount >= resDrawInfoDTO.getPrizeCount()) {
                    result.addProperty("groupStatus", 1);
                } else {
                    result.addProperty("groupStatus", 0);
                }
            }
        } else {
            result.addProperty("groupId", histUserDrawDTO.getGroupId());
            result.addProperty("isUserIn", 1);
            int groupUserCount = drawService.getGroupUserCount(histUserDrawDTO.getGroupId());
            if (groupUserCount >= resDrawInfoDTO.getPrizeCount()) {
                result.addProperty("groupStatus", 1);
            } else {
                result.addProperty("groupStatus", 0);
            }
        }

        JsonArray bannerArray = new JsonArray();
        if (!CollectionUtils.isEmpty(liveShopProductDetailDTO.getProductPictureDTOList())) {
            for (LiveShopProductPictureDTO item : liveShopProductDetailDTO.getProductPictureDTOList()) {
                JsonObject json = new JsonObject();
                json.addProperty("productUrl", item.getResourceUrl() + "!640x640");
                json.addProperty("productUrlBig", item.getResourceUrl() + "!1280");
                bannerArray.add(json);
            }
        }
        result.add("drawImgUrlsBannerUrls", bannerArray);

        JsonArray descArray = new JsonArray();
        if (!CollectionUtils.isEmpty(liveShopProductDetailDTO.getProductPictureDTODetailList())) {
            for (LiveShopProductPictureDTO item : liveShopProductDetailDTO.getProductPictureDTODetailList()) {
                JsonObject json = new JsonObject();
                json.addProperty("productDetailUrl", item.getResourceUrl() + "!512");
                json.addProperty("pictureWidth", item.getPictureWidth());
                json.addProperty("pictureHeight", item.getPictureHeight());
                descArray.add(json);
            }
        }
        result.add("prizeImg", descArray);

        result.addProperty("isGroup", resDrawInfoDTO.getIsGroup());
        result.addProperty("drawName", liveShopProductDetailDTO.getProductName());
        if (!StringUtils.isEmpty(liveShopProductDetailDTO.getProductDetailDesc())) {
            result.addProperty("drawDesc", liveShopProductDetailDTO.getProductDetailDesc());
        }
        result.addProperty("prizeCount", resDrawInfoDTO.getPrizeCount());
        result.addProperty("drawPrice", resDrawInfoDTO.getDrawPrice());
        result.addProperty("expressPrice", liveShopProductDetailDTO.getExpressPrice());
        result.addProperty("startTime", resDrawInfoDTO.getStartTime().getTime());

        result.addProperty("numberOfDrawing", resDrawInfoDTO.getNumberOfDrawing());
        result.addProperty("drawingTime", resDrawInfoDTO.getDrawingTime().getTime());
        int status = drawService.getDrawStatus(resDrawInfoDTO.getEndStatus(), resDrawInfoDTO.getStartTime(),
                resDrawInfoDTO.getDrawingTime());
        result.addProperty("status", status);

        if (resDrawInfoDTO.getEndStatus() == DrawEndStatus.OVER) {
            JsonArray luckUserArray = new JsonArray();
            List<HistUserDrawDTO> luckUserList = drawService.getLuckUserList(drawId);
            if (!CollectionUtils.isEmpty(luckUserList)) {
                for (HistUserDrawDTO item : luckUserList) {
                    UserProfile userProfile = kkUserService.getUserProfile(item.getUserId());
                    if (userProfile != null) {
                        JsonObject json = new JsonObject();
                        json.addProperty("userId", item.getUserId());
                        if (userProfile.getPortrait() != null) {
                            json.addProperty("portrait", userProfile.getPortrait());
                        }
                        json.addProperty("nickname", userProfile.getNickName());
                        json.addProperty("gender", userProfile.getGender());
                        luckUserArray.add(json);
                    }
                }
            }
            HistUserDrawDTO isLuckUser = drawService.isLuckUser(drawId, userId);
            if (isLuckUser != null) {
                result.addProperty("isLuck", 1);
                result.addProperty("orderNo", isLuckUser.getOrderNo());
            } else {
                result.addProperty("isLuck", 0);
            }
            result.add("luckUsers", luckUserArray);
        }
        result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    /**
     * 参与抽奖(51130108)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject draw(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }

        int userId;
        int drawId;
        int groupId;
        int isGroup;

        try {
            drawId = CommonUtil.getJsonParamInt(jsonObject, "drawId", 0, TagCodeEnum.PARAMETER_MISSING, 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, ParamCodeEnum.USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            groupId = CommonUtil.getJsonParamInt(jsonObject, "groupId", 0, null, 1, Integer.MAX_VALUE);
            isGroup = CommonUtil.getJsonParamInt(jsonObject, "isGroup", 0, TagCodeEnum.PARAMETER_MISSING, 0, 1);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        Result<Integer> drawResult = drawService.draw(userId, drawId, groupId, isGroup == 1 ? true : false);
        if (drawResult == null) {
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
        if (CommonStateCode.SUCCESS.equals(drawResult.getCode())) {
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            result.addProperty("groupId", drawResult.getData());
            UserProfile userProfile = kkUserService.getUserProfile(userId);
            if (userProfile != null) {
                List<MessageInfo> list = new ArrayList<>();
                list.add(new MessageInfo("" + userProfile.getNickName() + "参与了直播抽奖！", 1, null, null));
                activityCommonService.pubshMessageByLink(list, 1, 0);
            }
            return result;
        } else {
            if ("NOT_EXIST".equals(drawResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.NOT_EXIST);
                return result;
            } else if ("OVER_TIME".equals(drawResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.TIME_ERROR);
                return result;
            } else if ("CAN_NOT_GROUP".equals(drawResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.CAN_NOT_GROUP);
                return result;
            } else if ("HAS_BEEN_DRAW".equals(drawResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.HAS_BEEN_DRAW);
                return result;
            } else if ("DRAW_ID_ERROR".equals(drawResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.DRAW_ID_NOT_MATCH);
                return result;
            } else if ("HAS_BEEN_GROUP".equals(drawResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.HAS_BEEN_GROUP);
                return result;
            } else if ("EXCEED_GROUP_COUNT".equals(drawResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.EXCEED_GROUP_COUNT);
                return result;
            } else if ("CAN_NOT_IN_SELF_DRAW".equals(drawResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.CAN_NOT_IN_SELF_DRAW);
                return result;
            } else if ("SQL_ERROR".equals(drawResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
                return result;
            } else {
                result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
        }
    }

    /**
     * 获取我的抽奖(51130109)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getMyDrawList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }

        int userId;
        int type;
        int pageIndex;
        int countPerPage;

        try {
            type = CommonUtil.getJsonParamInt(jsonObject, "type", 0, TagCodeEnum.PARAMETER_MISSING, 0, 10);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, ParamCodeEnum.USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
            pageIndex = CommonUtil.getJsonParamInt(jsonObject, "pageIndex", 0, TagCodeEnum.PARAMETER_MISSING, 1, Integer.MAX_VALUE);
            countPerPage = CommonUtil.getJsonParamInt(jsonObject, "countPerPage", 0, TagCodeEnum.PARAMETER_MISSING, 1, 200);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        JsonArray jsonArray = new JsonArray();
        Page<HistUserDrawDTO> page;
        if (type == 0) {
            page = drawService.getUserPartakeDrawList(userId, pageIndex, countPerPage);
            if (!CollectionUtils.isEmpty(page.getList())) {
                List<Integer> drawIdList = new ArrayList<>();
                for (HistUserDrawDTO item : page.getList()) {
                    drawIdList.add(item.getDrawId());
                }
                Map<Integer, ResDrawInfoDTO> map = drawService.getResDrawInfoBatchMap(drawIdList);
                if (!CollectionUtils.isEmpty(map)) {
                    for (Integer item : drawIdList) {
                        ResDrawInfoDTO resDrawInfoDTO = map.get(item);
                        if (resDrawInfoDTO != null) {
                            LiveShopProductDetailDTO liveShopProductDetailDTO = productService.getProductDetail(resDrawInfoDTO.getProductId());
                            if (liveShopProductDetailDTO != null) {
                                JsonObject json = this.getResDrawInfoJson(resDrawInfoDTO, liveShopProductDetailDTO,
                                        page.getList());
                                jsonArray.add(json);
                            }
                        }
                    }
                }
            }
        } else {
            page = drawService.getUserWinningList(userId, pageIndex, countPerPage);
            if (!CollectionUtils.isEmpty(page.getList())) {
                List<Integer> drawIdList = new ArrayList<>();
                for (HistUserDrawDTO item : page.getList()) {
                    drawIdList.add(item.getDrawId());
                }
                Map<Integer, ResDrawInfoDTO> map = drawService.getResDrawInfoBatchMap(drawIdList);
                if (!CollectionUtils.isEmpty(map)) {
                    for (Integer item : drawIdList) {
                        ResDrawInfoDTO resDrawInfoDTO = map.get(item);
                        if (resDrawInfoDTO != null) {
                            LiveShopProductDetailDTO liveShopProductDetailDTO = productService.getProductDetail(resDrawInfoDTO.getProductId());
                            if (liveShopProductDetailDTO != null) {
                                JsonObject json = this.getResDrawInfoJson(resDrawInfoDTO, liveShopProductDetailDTO,
                                        page.getList());
                                jsonArray.add(json);
                            }
                        }
                    }
                }
            }
        }

        result.add("list", jsonArray);
        result.addProperty("count", page.getCount());
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }

    private JsonObject getResDrawInfoJson(ResDrawInfoDTO resDrawInfoDTO,
            LiveShopProductDetailDTO liveShopProductDetailDTO, List<HistUserDrawDTO> list) {
        JsonObject json = new JsonObject();
        json.addProperty("drawId", resDrawInfoDTO.getDrawId());
        json.addProperty("prizeName", liveShopProductDetailDTO.getProductName());
        if (resDrawInfoDTO.getEndStatus() == DrawEndStatus.OVER) {
            json.addProperty("status", 1);
        } else {
            json.addProperty("status", 0);
        }
        json.addProperty("prizeCount", resDrawInfoDTO.getPrizeCount());
        json.addProperty("time", resDrawInfoDTO.getDrawingTime().getTime());
        json.addProperty("isGroup", resDrawInfoDTO.getIsGroup());
        if (liveShopProductDetailDTO.getResourceUrl() != null) {
            json.addProperty("productImg", liveShopProductDetailDTO.getResourceUrl());
        }
        String orderNo = this.getOrderNo(list, resDrawInfoDTO.getDrawId());
        if (!StringUtils.isEmpty(orderNo)) {
            json.addProperty("orderNo", orderNo);
        }
        return json;
    }

    private String getOrderNo(List<HistUserDrawDTO> list, int drawId) {
        if (CollectionUtils.isEmpty(list)) {
            return "";
        }
        for (HistUserDrawDTO item : list) {
            if (item.getDrawId() == drawId) {
                return item.getOrderNo();
            }
        }
        return "";
    }

    /**
     * 停用抽奖(51130110)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject disableDraw(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        if (!checkTag) {
            result.addProperty(ParameterKeys.TAG_CODE, TagCodeEnum.TOKEN_INCORRECT);
            return result;
        }

        int userId;
        int drawId;

        try {
            drawId = CommonUtil.getJsonParamInt(jsonObject, "drawId", 0, TagCodeEnum.PARAMETER_MISSING, 1, Integer.MAX_VALUE);
            userId = CommonUtil.getJsonParamInt(jsonObject, "userId", 0, ParamCodeEnum.USER_ID.getErrorCode(), 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }

        Result<Boolean> drawResult = drawService.disableDraw(drawId, userId);
        if (drawResult == null) {
            result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
            return result;
        }
        if (CommonStateCode.SUCCESS.equals(drawResult.getCode())) {
            result.addProperty("TagCode", TagCodeEnum.SUCCESS);
            return result;
        } else {
            if ("NOT_EXIST".equals(drawResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.NOT_EXIST);
                return result;
            } else if ("OVER_TIME".equals(drawResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.TIME_ERROR);
                return result;
            } else if ("NO_PERMISSIONS".equals(drawResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.PERMISSIONS_ERROR);
                return result;
            } else if ("SQL_ERROR".equals(drawResult.getCode())) {
                result.addProperty("TagCode", TagCodeEnum.EXECSQL_EXCEPTION);
                return result;
            } else {
                result.addProperty("TagCode", TagCodeEnum.MODULE_UNKNOWN_RESPCODE);
                return result;
            }
        }
    }

    /**
     * 获取战队内人员列表(51130111)
     *
     * @param jsonObject
     * @param checkTag
     * @param request
     * @return
     */
    public JsonObject getDrawGroupList(JsonObject jsonObject, boolean checkTag, HttpServletRequest request) {
        JsonObject result = new JsonObject();
        int groupId;

        try {
            groupId = CommonUtil.getJsonParamInt(jsonObject, "groupId", 0, TagCodeEnum.PARAMETER_MISSING, 1, Integer.MAX_VALUE);
        } catch (CommonUtil.ErrorGetParameterException e) {
            result.addProperty("TagCode", e.getErrCode());
            return result;
        }
        int count = 0;
        JsonArray groupArray = new JsonArray();
        List<Integer> groupList = drawService.getGroupUserListByGroupId(groupId);
        if (!CollectionUtils.isEmpty(groupList)) {
            for (Integer item : groupList) {
                UserProfile userProfile = kkUserService.getUserProfile(item);
                if (userProfile != null) {
                    JsonObject json = new JsonObject();
                    json.addProperty("userId", item);
                    if (userProfile.getPortrait() != null) {
                        json.addProperty("portrait", userProfile.getPortrait());
                    }
                    json.addProperty("gender", userProfile.getGender());
                    groupArray.add(json);
                }
                count = groupList.size();
            }
        }
        result.addProperty("pathPrefix", ConfigHelper.getHttpdir());
        result.add("groupUsers", groupArray);
        result.addProperty("count", count);
        result.addProperty("TagCode", TagCodeEnum.SUCCESS);
        return result;
    }
}

package com.melot.kktv.util;

import com.google.common.collect.Maps;
import com.google.gson.Gson;

import java.util.Map;

/**
 * Description: 参数常量和错误码枚举定义
 *
 * @author:     shengjian
 * @version:    1.0
 * Filename:    ParamCodeEnum.java
 * Create at:   2018-05-29
 *
 * Copyright:   Copyright (c)2015
 * Company:     melot
 *
 * Modification History:
 * Date              Author      Version     Description
 * ------------------------------------------------------------------
 * 2018-05-29      shengjian      1.0         1.0 Version
 */
public enum ParamCodeEnum {

    USER_ID("userId", "53000001"),
    RANK_TYPE("rankType", "53000002"),
    MATCH_START_TIME("matchStartTime", "53000003"),
    SEASON_ID("seasonId", "53000004"),
    GUESS_BET_ITEM_ID("guessBetItemId", "53000005"),
    SHARE_USER_ID("shareUserId", "53000006"),
    GUESS_HIST_ID("guessHistId", "53000007"),
    ADDRESS_ID("addressId", "53000008"),
    UUID("uuid", "53000009"),
    PLATFORM("platform", "53000010"),
    UNIONID("unionid", "53000011"),
    ACTOR_ID("actorId", "53000012"),
    SEASON_TYPE("seasonType", "53000013"),
    LAT("lat", "53000014"),
    LNG("lng", "53000015"),
    AREA_CODE("areaCode", "53000016"),
    WORK_URL("workUrl", "53000017"),
    WORK_TYPE("workType", "53000018"),
    TOPIC_NAME("topicName", "53000019"),
    WORK_DESC("workDesc", "53000020"),
    TOPIC_ID("topicId", "53000021"),
    WORK_ID("workId", "53000022"),
    RESOURCE_IDS("resourceIds", "53000023"),
    WORK_SORT("workSort", "53000024"),
    APPLY_TYPE("applyType","53000025"),
    NAME("name","53000026"),
    AGE("age","53000027"),
    GENDER("gender","53000028"),
    HOME("home","53000029"),
    MOBILE_PHONE("mobilePhone","53000030"),
    PROFESSION("profession","53000031"),
    EXPERIENCE("experience","53000032"),
    REASON("reason","53000033"),
    COMMENT_CONTENT("commentContent","53000034"),
    COMMENT_ID("commentId","53000035"),
    WORK_LIST_TYPE("workListType","53000036"),
    ROOM_ID("roomId","53000037"),
    PRODUCT_NAME("productName","53000038"),
    PRODUCT_PRICE("productPrice","53000039"),
    STOCK_NUM("stockNum","53000040"),
    EXPRESS_PRICE("expressPrice","53000041"),
    CAT_ID("catId","53000042"),
    PRODUCT_BANNER_URLS("productBannerUrls","53000043"),
    PRODUCT_ID("productId","53000044"),
    SUPPORT_RETURN("supportReturn","53000044"),
    SUPPORT_DISTRIBUTION("supportDistribution","53000045"),
    SHOW_SHELF_STATUS("showShelfStatus","53000046"),
    STATE("state","53000047"),
    DISTRIBUTOR_COMMISSIONRATE("distributorCommissionRate","53000048"),
    PROJECT_ID("projectId","53000049"),
    COUPON_TYPE("couponType","53000050"),
    COUPON_AMOUNT("couponAmount","53000051"),
    USER_LIMIT_COUNT("user_limit_count","53000052"),
    REDUCTION_AMOUNT("reductionAmount","53000053"),
    START_TIME("startTime","53000054"),
    END_TIME("endTime","53000055"),
    RECEIVE_START_TIME("receiveStartTime","53000056"),
    RECEIVE_END_TIME("receiveEndTime","53000057"),
    USING_TYPE("usingType","53000058"),
    COUPON_COUNT("couponCount","53000059"),
    DRAW_NAME("drawName","53000060"),
    PRIZE_COUNT ("prizeCount","53000061"),
    PRICE ("price","53000062"),
    NUMBER_OF_DRAWING ("numberOfDrawing","53000063"),
    IS_GROUP ("isGroup","53000064"),
    DRAWING_TIME ("drawingTime","53000065"),
    DRAW_IMG ("drawImg","53000066");


    private String id;
    private String errorCode;

    ParamCodeEnum(String id, String errorCode) {
        this.id = id;
        this.errorCode = errorCode;
    }

    public static void main(String[] args) {
        System.out.println(RANK_TYPE.getId() + RANK_TYPE.getName());
        System.out.println(new Gson().toJson(paramCodeEnumIdMap));
        System.out.println(new Gson().toJson(paramCodeEnumNameMap));
        System.out.println(new Gson().toJson(ParamCodeEnum.values()));
        System.out.println(ParamCodeEnum.parseId("userId").getErrorCode());
        System.out.println(ParamCodeEnum.parseName("USER_ID").getId());
    }

    private static Map<String, ParamCodeEnum> paramCodeEnumIdMap = Maps.newHashMap();
    private static Map<String, ParamCodeEnum> paramCodeEnumNameMap = Maps.newHashMap();

    static {
        for (ParamCodeEnum paramCodeEnum : ParamCodeEnum.values()) {
            paramCodeEnumNameMap.put(paramCodeEnum.name().toLowerCase(), paramCodeEnum);
            paramCodeEnumIdMap.put(paramCodeEnum.id, paramCodeEnum);
        }
    }

    public static ParamCodeEnum parseId(String id) {
        return paramCodeEnumIdMap.get(id);
    }

    public static ParamCodeEnum parseName(String name) {
        return (null != name) ? paramCodeEnumNameMap.get(name.toLowerCase()) : null;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getName() {
        return this.name();
    }

    public String getId() {
        return id;
    }
}

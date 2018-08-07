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
    desc("desc", "53000020"),
    TOPIC_ID("topicId", "53000021"),
    WORK_ID("workId", "53000022");


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

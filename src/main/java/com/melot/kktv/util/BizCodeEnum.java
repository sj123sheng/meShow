package com.melot.kktv.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Description: BizCodeEnum 芝麻认证场景码
 *
 * @author:     shengjian
 * @version:    1.0
 * Filename:    BizCodeEnum.java
 * Create at:   2016-08-02
 *
 * Copyright:   Copyright (c)2015
 * Company:     songxiaocai
 *
 * Modification History:
 * Date              Author      Version     Description
 * ------------------------------------------------------------------
 * 2016-08-02      zhengshutian    1.0         1.0 Version
 */
public enum BizCodeEnum {

    FACE(1, "FACE"),   //多因子活体人脸认证
    FACE_SDK(2, "FACE_SDK"),    //SDK活体人脸认证
    SMART_FACE(3, "SMART_FACE");     //多因子快捷活体人脸认证

    private int id;
    private String value;
    private static List<Integer> allFlags = Lists.newArrayList();

    BizCodeEnum(int id, String value) {
        this.id = id;
        this.value = value;
    }

    public static void main(String[] args) {
        System.out.println(BizCodeEnum.parseId(1).getValue());
    }

    private static Map<Integer, BizCodeEnum> bizCodeEnumIdMap = Maps.newHashMap();
    private static Map<String, BizCodeEnum> bizCodeEnumNameMap = Maps.newHashMap();

    static {
        for (BizCodeEnum bizCodeEnumEnum : BizCodeEnum.values()) {
            bizCodeEnumNameMap.put(bizCodeEnumEnum.name().toLowerCase(), bizCodeEnumEnum);
            bizCodeEnumIdMap.put(bizCodeEnumEnum.id, bizCodeEnumEnum);
            allFlags.add(bizCodeEnumEnum.getId());
        }
    }

    public static BizCodeEnum parseId(int id) {
        return bizCodeEnumIdMap.get(id);
    }

    public static BizCodeEnum parseName(String name) {
        return (null != name) ? bizCodeEnumNameMap.get(name.toLowerCase()) : null;
    }

    public String getValue() {
        return value;
    }

    public String getName() {
        return this.name();
    }

    public int getId() {
        return id;
    }

    public static List<Integer> getAllFlags() {
        return allFlags;
    }

    public static List<String> getNames() {
        List<String> names = new ArrayList<String>();
        for (BizCodeEnum bizCodeEnumEnum : BizCodeEnum.values()) {
            names.add(bizCodeEnumEnum.name());
        }
        return names;
    }
}

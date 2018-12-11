package com.melot.kkcx.util;

/**
 * Created with IntelliJ IDEA.
 * Description:
 *
 * @author shoujian
 * Date: 2018-11-23
 * Time: 10:26 AM
 */
public enum ValidTypeEnum {

    /**
     * 道具类型
     */
    COMMON(1, "普通"),
    INDEFINITELY(2, "无限期");

    private Integer code;

    private String name;

    ValidTypeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public Integer getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }

    public static String getNameByCode(Integer code) {
        for (ValidTypeEnum item : ValidTypeEnum.values()) {
            if (item.getCode().equals(code)) {
                return item.getName();
            }
        }
        return null;
    }

    public static ValidTypeEnum getByCode(Integer code) {
        for (ValidTypeEnum item : ValidTypeEnum.values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }

    public static ValidTypeEnum valueOf(Integer code) {
        for (ValidTypeEnum item : ValidTypeEnum.values()) {
            if (code.equals(item.getCode())) {
                return item;
            }
        }
        return null;
    }

}

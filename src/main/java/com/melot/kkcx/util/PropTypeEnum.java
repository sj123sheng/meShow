package com.melot.kkcx.util;

/**
 * Created with IntelliJ IDEA.
 * Description:
 *
 * @author shoujian
 * Date: 2018-11-23
 * Time: 10:26 AM
 */
public enum PropTypeEnum {

    PENDANT(8, "挂件"),
    
    CHAT_BUBBLE(9, "气泡");

    private Integer code;

    private String name;

    PropTypeEnum(Integer code, String name) {
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
        for (PropTypeEnum item : PropTypeEnum.values()) {
            if (item.getCode().equals(code)) {
                return item.getName();
            }
        }
        return null;
    }

    public static PropTypeEnum getByCode(Integer code) {
        for (PropTypeEnum item : PropTypeEnum.values()) {
            if (item.getCode().equals(code)) {
                return item;
            }
        }
        return null;
    }

    public static PropTypeEnum valueOf(Integer code) {
        for (PropTypeEnum item : PropTypeEnum.values()) {
            if (code.equals(item.getCode())) {
                return item;
            }
        }
        return null;
    }

}

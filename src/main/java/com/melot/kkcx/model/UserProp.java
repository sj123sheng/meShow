package com.melot.kkcx.model;

/**
 * Created with IntelliJ IDEA.
 * Description:
 *
 * @author shoujian
 * Date: 2018-11-23
 * Time: 9:44 AM
 */
public class UserProp {

    /**
     * 道具id
     */
    private Integer id;

    /**
     * 道具类型： 8.挂件 9.气泡
     */
    private Integer type;

    /**
     * 道具类别 挂件：1.普通挂件 2.活动挂件 3.闪星挂件； 气泡: 1.普通气泡 2.闪星气泡
     */
    private Integer subType;

    /**
     * 挂件等级
     */
    private Integer level;

    /**
     * 是否点亮
     */
    private Integer isLight;

    /**
     * 有效类型： 1普通 2无限期 3保持
     */
    private Integer validType;

    /**
     * 剩余时间
     */
    private Long leftTime;

    /**
     * 道具名称
     */
    private String name;

    /**
     * 道具描述
     */
    private String desc;

    /**
     * app大图标链接
     */
    private String appLargeUrl;

    /**
     * web大图标链接
     */
    private String webLargeUrl;

    /**
     * 小图标链接
     */
    private String smallUrl;

    @Override
    public String toString() {
        return "UserProp{" +
                "id=" + id +
                ", type=" + type +
                ", subType=" + subType +
                ", level=" + level +
                ", isLight=" + isLight +
                ", validType=" + validType +
                ", leftTime=" + leftTime +
                ", name='" + name + '\'' +
                ", desc='" + desc + '\'' +
                ", appLargeUrl='" + appLargeUrl + '\'' +
                ", webLargeUrl='" + webLargeUrl + '\'' +
                ", smallUrl='" + smallUrl + '\'' +
                '}';
    }

    public String getAppLargeUrl() {
        return appLargeUrl;
    }

    public void setAppLargeUrl(String appLargeUrl) {
        this.appLargeUrl = appLargeUrl;
    }

    public String getWebLargeUrl() {
        return webLargeUrl;
    }

    public void setWebLargeUrl(String webLargeUrl) {
        this.webLargeUrl = webLargeUrl;
    }

    public Integer getSubType() {
        return subType;
    }

    public void setSubType(Integer subType) {
        this.subType = subType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getIsLight() {
        return isLight;
    }

    public void setIsLight(Integer isLight) {
        this.isLight = isLight;
    }

    public Integer getValidType() {
        return validType;
    }

    public void setValidType(Integer validType) {
        this.validType = validType;
    }

    public Long getLeftTime() {
        return leftTime;
    }

    public void setLeftTime(Long leftTime) {
        this.leftTime = leftTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }


    public String getSmallUrl() {
        return smallUrl;
    }

    public void setSmallUrl(String smallUrl) {
        this.smallUrl = smallUrl;
    }
}

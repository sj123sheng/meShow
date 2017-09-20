package com.melot.kktv.util;

/**
 * Title: RequestParameterKeys
 * <p>
 * Description: 参数常量集合
 * </p>
 * 
 * @author 宋建明<a href="mailto:jianming.song@melot.cn">
 * @version V1.0
 * @since 2017年9月18日 上午11:24:52
 */
public class ParameterKeys {

    private ParameterKeys() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * 响应结果代码
     */
    public static final String TAG_CODE = "TagCode";

    /**
     * 产品编号
     */
    public static final String APP_ID = "a";

    /**
     * 渠道编号
     */
    public static final String APP_CHANNEL = "c";

    /**
     * 套壳号
     */
    public static final String HULLID = "b";

    /**
     * 平台号
     */
    public static final String PLATFORM = "platform";

    /**
     * 一次性请求个数
     */
    public static final String NUM = "num";

    /**
     * 分页请求参数-起始数
     */
    public static final String START = "start";

    /**
     * 分页请求参数-一次请求个数
     */
    public static final String COUNT = "count";

    /**
     * 分页请求参数-一次请求个数
     */
    public static final String OFFSET = "offset";

    /**
     * 用户编号
     */
    public static final String USER_ID = "userId";

    /**
     * 房间编号
     */
    public static final String ROOM_ID = "roomId";

    /**
     * 第三方标识
     */
    public static final String UUID = "uuid";

    /**
     * 第三方联合标识
     */
    public static final String UNIONID = "unionid";
    
    /**
     * 昵称
     */
    public static final String NICKNAME = "nickname";
    
    /**
     * 类型
     */
    public static final String TYPE = "type";
    
    /**
     * 金额
     */
    public static final String AMOUNT = "amount";
    
    // 奖励金参数 ==========================================
    /**
     * 红包ID
     */
    public static final String RED_PACKET_ID = "redPacketId";
    
    /**
     * 最大红包ID
     */
    public static final String MAX_RED_PACKET_ID = "maxRedPacketId";
    
    // 奖励金参数 ==========================================

}

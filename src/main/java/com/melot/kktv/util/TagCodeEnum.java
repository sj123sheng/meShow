package com.melot.kktv.util;

public class TagCodeEnum {

	/** 处理正确，无错误返回 */
	public static final String SUCCESS = "00000000";

	/** 消息处理函数未捕获的异常(详情查看日志) */
	public static final String UNCATCHED_EXCEPTION = "20001001";

	/** 调用存储过程异常(详情查看日志) */
	public static final String PROCEDURE_EXCEPTION = "20001002";

	/** 调用存储过程未得到正常结果(详情查看日志) */
	public static final String IRREGULAR_RESULT = "20001003";

	/** 执行数据库语句异常 */
	public static final String EXECSQL_EXCEPTION = "20001004";

	/** 结束数据库操作事务异常 */
	public static final String ENDTRANSACTION_EXCEPTION = "20001005";

    /** 版本过低异常 */
    public static final String LOW_VERSION_EXCEPTION = "20001006";

    /** 接口暂停使用 */
    public static final String FUNCTAG_UNUSED_EXCEPTION = "20001007";

    /** 请求无效 */
    public static final String REQUEST_INVALID = "20001008";

    /** 接口过期异常 */
    public static final String FUNCTAG_INVALID_EXCEPTION = "20001010";
    
    /**  该用户操作次数超过当日限制*/
    public static final String FUNCTAG_LIMIT_EXCEPTION = "20001011";

	/** 参数中不包含parameter */
	public static final String PARAMETER_MISSING = "30001000";

	/** 参数parameter值格式不对,不能转化成json对象 */
	public static final String PARAMETER_NOTJSON = "30001001";

	/** 参数parameter值格式不对,不包含FuncTag字段 */
	public static final String PARAMETER_NOTCONTAINED_FUNCTAG = "30001002";

	/** 参数parameter值格式不对,FuncTag字段对应的值不能转化为整数类型 */
	public static final String FUNCTAG_NOTINTEGER = "30001003";

	/** 参数parameter值格式不对,userId缺失 */
	public static final String USERID_MISSING = "30001004";

	/** token验证失败 */
	public static final String TOKEN_INCORRECT = "30001005";

	/** 参数parameter值格式不对,FuncTag字段对应的整数值类型后台尚未处理 */
	public static final String UNPROCESSED_FUNCTAG = "30001006";

	/** 没有验证token(参数中不存在userId或token) */
	public static final String TOKEN_NOT_CHECKED = "30001007";

	/** ip没有权限访问接口 */
	public static final String IP_ACCESS_FORBIDDEN = "30001008";

	/** 接口已废弃 */
	public static final String ABANDONED_FUNCTAG = "30001009";

	/** 从又拍云删除图片失败 */
	public static final String FAILED_TO_DELETE = "30001010";

	/** 参数解析错误 */
	public static final String PARAMETER_PARSE_ERROR = "30001011";

	/** 超过分页范围 */
	public static final String OVER_PAGGING_RANGE = "30001012";

	/** 参数phoneNum缺失 */
	public static final String PHONE_NUMBER_MISSING = "30001013";

	/** 参数platform缺失 */
	public static final String PLATFORM_MISSING = "30001014";

	/** 参数start缺失 */
	public static final String START_MISSING = "30001015";

	/** 参数offset缺失 */
	public static final String OFFSET_MISSING = "30001016";

	/** 参数appId缺失 */
	public static final String APPID_MISSING = "30001017";
	
	/** 参数channel缺失*/
	public static final String CHANNEL_MISSING = "30001018";
	
	/** 参数deviceUId缺失*/
	public static final String DEVICEUID_MISSING = "30001019";
  
    /** 参数gender缺失 */
    public static final String GAME_GENDER_MISSING = "30001020";

    /** 参数applyname缺失 */
    public static final String GAME_APPLYNAME_MISSING = "30001021";
    
    /** 参数identity缺失*/
    public static final String GAME_IDENTITY_MISSING = "30001022";

    /** 某个picture类的参数缺失 */
    public static final String GAME_PICTURE_MISSING = "30001023";

    /** 参数qqnumber缺失 */
    public static final String GAME_QQNUM_MISSING = "30001024";
  
    /** 参数queryMonth缺失 */
    public static final String GAME_QUERY_MONTH_MISSING = "30001026";
	
	/** 参数roomId缺失*/
	public static final String ROOMID_MISSING = "30001022";
	
	/** 用户不存在 */
	public static final String USER_NOT_EXIST = "30002001";

	/** 用户秀币余额不足 */
	public static final String USER_MONEY_SHORTNESS = "30002002";
	
	/** 不在活动时间范围内 */
	public static final String NOT_ACTIVITY_TIME = "30001020";
	
	/** 获取时间(戳)失败 */
	public static final String GET_TIME_FAIL = "30001021";
	
	/** 权限不足 */
	public static final String PERMISSION_DENIED = "30001022";
	
	/** 获取相关信息失败 */
    public static final String GET_RELATED_INFO_FAIL = "30001023";
    
    /** 参数解析不合法 */
    public static final String PARAMETER_ILLEAGLE = "30001024";
    
    /** 对象存在重叠 */
    public static final String OBJECT_OVERLAP  = "30001025";
    
    /** 超过限制数量 */
    public static final String OVER_LIMIT_COUNT  = "30001026";

    /** 参数eventId缺失 */
    public static final String EVENT_ID_MISSING = "30001027";

    /** 参数event_start_time缺失 */
    public static final String EVENT_START_TIME_MISSING = "30001028";

    /** 参数event_end_time缺失 */
    public static final String EVENT_END_TIME_MISSING = "30001029";

    /** 参数state_missing缺失 */
    public static final String STATE_MISSING = "30001030";

    /** 用户昵称为空 */
    public static final String NICKNAME_EMPTY = "30001031";

    /** 用户为游客 */
    public static final String USER_IS_VISITOR = "30001032";

    /** 黑名单用户 */
    public static final String USER_IN_BLACK = "30001033";

    /** 同一身份证 直播主播和唱响主播不能共存 */
    public static final String USER_IN_GAME_KKCX_FORBID = "30001034";

    /** 参数 uuid 缺失 */
    public static final String UUID_MISSING = "30001035";

    /** 参数 unionid 缺失 */
    public static final String UNIONID_MISSING = "30001036";

    /** 参数 unionid 错误 */
    public static final String UNIONID_ERROR = "30001037";

    /** 未认证手机号 */
    public static final String NON_IDENTITY_PHONE = "30001038";

    /** 未绑定微信 */
    public static final String NON_BIND_WECHAT = "30001039";

    /** 参数 amount 缺失 */
    public static final String AMOUNT_MISSING = "30001040";

    /** 微信公众号支付通信标识错误 */
    public static final String WECAHTPUBLIC_RETURN_ERR = "30001041";
    
    /** 微信公众号支付交易标识错误 */
    public static final String WECAHTPUBLIC_RESULT_ERR = "30001042";
    
    /** 微信公众号余额不足 */
    public static final String WECAHTPUBLIC_NOTENOUGH_ERR = "30001043";
    
    /** 发放失败，此请求可能存在风险，已被微信拦截 */
    public static final String WECAHTPUBLIC_NO_AUTH_ERR = "30001044";
    
    /** 该用户今日操作次数超过限制 */
    public static final String WECAHTPUBLIC_SENDNUM_LIMIT_ERR = "30001045";
    
    /** 微信提现接口关闭 */
    public static final String WECAHTPUBLIC_CLOSED = "30001046";
    
    /** 用户昵称待审 */
    public static final String NICKNAME_PENDINGAUDIT = "30001047";
	
	/** 模块服务返回空数据 */
	public static final String MODULE_RETURN_NULL = "10000000";

	/** 模块服务返回未知响应码 */
	public static final String MODULE_UNKNOWN_RESPCODE = "10000001";

    /** 未查询任何数据 */
    public static final String QUERY_RETURN_NULL = "40040001";
	
	/* 活动模块异常码 */
	public static final String ACTIVITY_ID_MISSING = "10010001";
	public static final String PLAY_ID_MISSING = "10010002";
	public static final String PLAYER_ID_MISSINF = "10010003";
	public static final String RANK_TYPE_MISSING = "10010004";
	public static final String ACTIVITY_NOT_EXIST = "10010005";

	public static final String PLAY_NOT_EXIST = "10010006";
	public static final String CANNOT_VOTE_NOW = "10010007";
	public static final String OVER_PLAY_MAX_VOTES = "10010008";
	public static final String OVER_PLAYER_MAX_VOTES = "10010009";
	public static final String VOTE_FAILED = "10010010";

	public final static String HAS_BEEN_ASSISTANT = "10010011";
	public final static String ADD_ASSISTANT_FAILED = "10010012";
	public final static String OVER_PLAYER_ASSISTANT_MAX_COUNT = "10010013";
	public final static String ASSISTANT_MUST_BE_ACTOR = "10010014";
	public final static String PLAYER_CANNOT_BE_ASSISTANT = "10010015";

	/* 商场模块异常码 */
	public final static String MONTHS_MISSING = "10010101";
	public final static String MONTH_PRICE_NOT_EXIST = "10010102";
	public final static String PRODUCT_ID_MISSING = "10010103";
	public final static String PRODUCT_NOT_EXIST = "10010104";
	public final static String BUY_PRODUCT_FAILED = "10010105";
	public final static String CAR_CAN_NOT_RENEW = "10010107";
	/** 不满足（购买）条件*/
	public final static String NOT_MEET_CONDITION = "10010106";

	/* 标签服务异常码 */
	public final static String TAG_NAME_MISSING = "10010201";
	public final static String TAGGED_USERID_MISSING = "10010202";
	public final static String USER_TAG_EXIST = "10010203";
	public final static String TAGGING_FAIULED = "10010204";
	public final static String DELETE_USER_TAG_FAILED = "10010205";
	public final static String TAG_NOT_EXIST = "10010206";
	public final static String CREATE_TAG_FAIULED = "10010207";
	public final static String TAG_NAME_OVERLENGTH = "10010208";
	public final static String USER_TAG_NOT_EXIST = "10010209";

	/* 兑换礼物服务异常码 */
	public final static String USER_GIFT_NOT_EXIST = "10010301";
	public final static String EXPID_NOT_EXIST = "10010302";
	public final static String STOREHOUSE_LACK = "10010303";
	public final static String EXCHANGE_FAIL = "10010304";
	public final static String CAR_LACK = "10010305";
	public final static String EXCHANGE_FORBID_REPEAT = "10010306";

	/* 账号服务异常码 */
	public static final String NOT_USER_PHONE_NUMBER = "10010401";
	public static final String NONE_OTHER_BOUND_ACCOUNT = "10010402";
	public static final String UNBIND_PHONE_NUMBER_FAIULED = "10010403";

	/* 高清游戏视频异常码 */
	public static final String GET_USERVIDEO_FAIL = "10010501"; // 获取视频信息失败或视频不存在
	public static final String GET_USERVIDEO_COVER_FAIL = "10010502"; // 获取视频封面失败
	public static final String UPDATE_TITLE_FAIL = "10010503"; // 更新标题失败
	public static final String DELETE_VIDEO_FAIL = "10010504"; // 删除视频失败
	public static final String FAIL_GET_VIDEO_LIST = "10010505"; // 转换视频列表失败
	public static final String NOT_PASS_VIDEO_CHECK = "10010506"; // 视频审核未通过
	public static final String GET_UPLOAD_TOKEN_FAIL = "10010507"; // 获取上传token失败
	public static final String GET_UPLOAD_FILETYPE_FAIL = "10010508"; // 获取上传文件类型失败
	public static final String GET_NEWSTYPE_FAIL = "10010509"; // 获取动态类型失败
	public static final String GET_KEY_FAIL = "10010510"; // 获取KEY失败
	public static final String SAVE_VIDEO_FAIL = "10010511"; // 保存视频失败
	public static final String GET_RESTYPE_FAIL = "10010512"; // 获取资源类型失败
	public static final String GET_VIDEOTITLE_FAIL = "10010513"; // 获取视频名称失败
	public static final String GET_VIDEOCONTENT_FAIL = "10010514"; // 获取视频内容（描述）失败
	public static final String NOT_OWN_THEVIDEO = "10010515"; // 视频不属于此用户
	public static final String GET_DURATION_FAIL = "10010516"; // 获取时长失败
    public static final String GET_RESUMEUP_FAIL = "10010517"; // 获取上传方式失败
    public static final String GET_MIMETYPE_FAIL = "10010518"; // 获取上传类型（图片、视频） 失败
    public static final String GET_USERCONTENT_FAIL = "10010519"; // 获取视频描述失败
    public static final String GET_NEWSID_FAIL = "10010520"; // 获取视频Id失败
    
    public static final String NEWS_ALREDY_APPRECIATE = "10010530"; // 视频已被点赞

	/* 热门关键字搜索异常码 */
	public static final String GET_HOTWORDS_FAIL = "10010601";
	public static final String SENSITIVE_WORD_ERROR = "10010602";

	/* 法拉利活动异常码 */
	public static final String CATEGORY_MISSING = "10010701";
	public static final String GET_CATEGORY_CARINFO_FAIL = "10010702";

	/* 发现异常码 */
	public static final String GET_USER_LOGIN_TIME_FAIL = "10010801";

	/* 新版大厅参数异常码 */
	public static final String CATAID_MISSING = "10010901";
	public static final String FAIL_TO_CALL_API_MENU_MODULE = "10010902";
	
	/*----------购票模块异常码------------*/
	/* 参数ticketId缺失 */
	public static final String TICKETID_MISSING = "10011001";
	/* 门票不存在 */
	public static final String TICKET_NOT_EXIST = "10011002";
	/* 用户已经预览过节目 */
	public static final String TICKET_ACT_HAS_PREVIEWED = "10011003";
	/* 设置预览节目失败 */
	public static final String TICKET_ACT_PREVIEW_FAILED = "10011004";
	/* 用户未拥有门票 */
//	public static final String USER_HAS_NO_TICKETS = "10011005";
	/* 参数ownerId缺失 */
	public static final String OWNERID_MISSING = "10011006";
	/* 参数referrerId缺失 */
	public static final String REFERRERID_MISSING = "10011007";
	/* 门票活动已经结束 */
	public static final String TICKET_ACTIVITY_END = "10011008";
	/* 用户已购买节目门票 */
	public static final String TICKET_HAS_BOUGHT = "10011009";
	/* 购买门票失败 */
	public static final String BUY_TICKET_FAILED = "10011010";
	/* 可购门票已达到上限 */
	public static final String MAX_TICKET_COUNT_LIMIT = "10011011";
	
	/*神秘人异常码 */
	public static final String NOT_KINGID = "10012001";
	public static final String OPETATE_STEALTH_FAIL = "10012002";
	
	/*-----------群组异常码----------*/
	public static final String FAIL_TO_CALL_API_CONTENT_MODULE = "10013001";
	public static final String FACEPACKID_MISSING = "10013002";
	public static final String UNUSEABLE_FACEPACKID = "10013003";
	public static final String SHOWMONEY_LESS = "10013004";
	public static final String BIND_FAIL = "10013005";
	public static final String REPEAT_BING = "10013006";
	public static final String PRICE_CHANGE = "10013007";
	
	/*--------游戏房间标签异常码------- */
	public static final String ACTORID_MISSING = "10014001";
	public static final String FAIL_TO_UPDATE_GAME_TAG = "10014002";
	public static final String GAME_TAG_MISSING = "10014003";
	
	/*------- 游戏中心异常码 --------*/
	public static final String UNABEL_PROMOTE_GAME = "10015002";
	
	
	/*------- 举报异常码 ------- */
	public static final String NICKNAME_MISSING = "10016001";
	public static final String TOUSERID_MISSING = "10016002";
	public static final String TONICKNAME_MISSING = "10016003";
	public static final String REPORTTYPE_MISSING = "10016004";
	public static final String USERTYPE_MISSING = "10016005";
	public static final String REASON_MISSING = "10016006";
	public static final String EVIDENCEURLS_MISSING = "10016007";
	public static final String REPORT_ERROR = "10016008";
	
	/*-------家族异常码---------*/
	/* 家族Id缺失 */
	public static final String FAMILYID_MISSING = "10008001";
	
	/* ------主播申请异常码------ */
	public static final String HAS_APPLY_OTHER_APP = "02050101";
	public static final String APPLY_OUT_DATE_DELETE = "02050102";
	public static final String APPLY_OUT_DATE_DELETE_FAIL = "02050103";
	
	public static final String GENDER_MISSING  = "02050201";
	public static final String REALNAME_MISSING  = "02050202";
	public static final String IDENTITY_MISSING  = "02050203";
	public static final String MOBILENUM_MISSING  = "02050204";
	public static final String IDPICONHAND_MISSING  = "02050205";
	public static final String FAIL_SAVE_APPLY = "02050208";
	
	public static final String APPLY_IDNUM_EXISTS = "02050209";
	public static final String APPLY_MOBILE_EXISTS = "02050210";
	public static final String APPLY_QQNUM_EXISTS = "02050211";
	
	public static final String HASNOT_APPLY = "02050301";
	public static final String CHECKING = "02050302";
	public static final String HAS_APPLY = "02050303";
	public static final String FAIL_TO_UPDATE = "02050304";
	
	public static final String FAIL_TO_DELETE = "02050401";
	public static final String HAS_CHECKING_PASS = "02050402";
	
	public static final String FAMILY_ISNOT_EXIST = "02050601";
	public static final String HAS_NOT_CHECKING_PASS = "02050602";
	public static final String HAS_APPLY_PLAY = "02050603";
	public static final String IDENTITY_BLACK_LIST = "02050606";
	
	/** 家族成员不能申请成为家族主播  */
	public static final String MEMBER_CANT_APPLY = "02050604";
    
	/** 一个身份证只能归宿一个家族 */
    public static final String IDENTITY_HAS_FAMILY = "02050605";
	
	public static final String CURRENT_STATE_CANT_OPERATION = "02050701";
	
	public static final String ISOK_MISSIING = "02050801";
	public static final String MISSING_BANK_INFO = "02050802";
	public static final String HAS_AGREE_SIGNED = "02050803";
	public static final String HASNOT_PASS_PLAYINGTEST = "02050804";
	
	/**room房间lock状态丢失**/
	public static final String ROOMLOCK_MISSING = "03040401";
	
	/** 修改roomLock失败 **/
	public static final String CHANGE_FAIL = "03040402";

    /** 图片集相册 */
    public static final String ALBUMID_MISSING = "03040002";

    /** ---- 投注模块 ------ */
    public static final String GAMBLEID_MISS = "03010004";
    public static final String OPTIONID_MISS = "03010005";
    public static final String AMOUNT_MISS = "03010006";
    public static final String GAMBLETITLE_MISS = "03010007";
    public static final String OPTIONTITLE_MISSING = "03010010";
    public static final String GAMBLESTARTTIME_MISSING = "03010011";
    public static final String GAMBLEENDTIME_MISSING = "03010012";
    public static final String GAMBLE_IS_NOT_COMPLY_ANCHOR = "03010013";
    public static final String CAN_NOT_CLEAR_GAMBLE = "03010015";
    public static final String CAN_NOT_CAST_OPTIONTITLES = "03010016";
    public static final String ENDTIME_IS_NOT_OUTTIME = "03010017";
    public static final String CANCELREASON_MISSING = "03010018";
    public static final String ACTOR_IS_IN_BLACK_LIST = "03010019";
    public static final String ACTOR_LEVEL_INAVAILABLE = "03010020";
    public static final String TITLE_INCLUDE_SENSITIVE_WORDS = "03010021";
    public static final String STRING_CAN_NOT_PARSE_DATE = "03010022";
    public static final String OPTION1_INCLUDE_SENSITIVE_WORDS = "03010023";
    public static final String OPTION2_INCLUDE_SENSITIVE_WORDS = "03010024";
    public static final String CALCELREASON_INCLUDE_SENSITIVE_WORDS = "03010025";
	
    /** ---- 赛事预告------ */
    /** 开播时间大于结束时间，不符合规律 */
    public static final String TIME_ISNOT_COMPLY = "03021001";

    /*-------赛事新闻-------*/
    public static final String EVENT_TYPE_MISSING = "03030001";
    public static final String REDIS_ERROR = "50300001";
	
    /** 10006059资料审核 -start- */
    /**
     * 用户id已经存在
     */
    public static final String APPLY_ID_EXISTS = "1000605901";
    /**
     * 手机已经存在
     */
    public static final String APPLY_PHONE_EXISTS = "1000605902";
    /**
     * qq号已经存在
     */
    public static final String APPLY_QQ_EXISTS = "1000605903";

    /** 10006059资料审核 -end- */
    
    /** 置顶用户ID无效 */
    public static final String TOP_USERID_MISSING = "05510001";
    
    /** 配置信息不存在 */
    public static final String CONFIG_KEY_NOT_EXIST = "05110102";
    
    /*---------礼物配置信息相关错误码-------------*/
    
    /** 模块没有数据返回 */
    public static final String GIFT_MODULE_NULL = "05110301";
    
    /** 对应{com.melot.room.gift.constant.ReturnResultCode#ERROR_PARMETER}*/
    public static final String GIFT_MODULE_ERROR_PARMETER = "05110302";
    
    /** 对应{com.melot.room.gift.constant.ReturnResultCode#ERROR_SQL*/
    public static final String GIFT_MODULE_ERROR_SQL = "05110303";
    
    /** 对应{com.melot.room.gift.constant.ReturnResultCode#ERROR_REQUEST_TIMEOUT}*/
    public static final String GIFT_MODULE_ERROR_REQUEST_TIMEOUT = "05110304";
    
    /** 我们不知道的module错误*/
    public static final String GIFT_MODULE_ERROR_UNDEFINED = "05110305";
    
    /** 没有成功获取版本号*/
    public static final String GIFT_VERSION_IS_NULL = "05110306";
    
    /*-------------------END----------------------*/
    
    /** 交友房表情信息获取失败 */
    public static final String EMOTICON_NOT_FIND = "05110401";

    /** 1v1视频，没有输入主播ID*/
    public static final String ACTOR_ID_MISSING = "05110601";
    
    /** 1v1视频，配置信息有误 */
    public static final String SINGLE_CHAT_CONFIG_ERROR = "05110602";
    
    /** 用户没有权限开播*/
    public static final String SINGLE_CHAT_NOT_ACTOR = "05110701";
    
    
    /*------分享接口 20010021 -------*/
    /**分享平台 为空*/
    public static final String SHARE_PLATFORM_IS_NULL = "02012104";
    /**直播间ID 或 资源ID 为空*/
    public static final String SHARE_SOURCE_ID_IS_NULL = "02012102";
    /**分享类型为空*/
    public static final String SHARE_TYPE_IS_NULL = "02012105";
    /**录屏分享资源为空*/
    public static final String VIDEO_TAPE_NOT_EXIST = "02012103";
    /**分享话题为空*/
    public static final String SHARE_REASON_IS_NULL = "02012106";
    /**分享连接为空*/
    public static final String SHARE_LINK_IS_NULL = "02012107";
    /*-------------------END----------------------*/

    /*------全民PK-天梯赛接口 510604 -------*/
    /**天梯赛活动已下架*/
    public static final String LADDER_MATCH_UNDERCARRIAGE = "5106040101";
    /**天梯赛活动暂未配置*/
    public static final String LADDER_MATCH_UN_CONF = "5106040102";
    /**不是主播*/
    public static final String NOT_ACTOR = "5106040201";

    /*------芝麻认证接口 52020102 -------*/
    /**身份证号码不一致*/
    public static final String ID_NOT_MATCH = "5202010201";
    /**根据bizNo获取芝麻认证信息错误*/
    public static final String GET_VERIFY_INFO_ERROR = "5202010202";
    /**获取bizNo错误*/
    public static final String GET_BIZNO_ERROR = "5202010203";

    /*------提现接口 -------*/
    /**未实名认证*/
    public static final String ID_NOT_IDENTIFY = "53010601";
    /**短信验证码不匹配或已过有效期*/
    public static final String VERIFYCODE_ERROR = "53010602";
    /** 参数bankCard缺失 */
    public static final String BANKCARD_MISS = "53010603";
    /** 获取k豆出错 */
    public static final String GETKBI_ERROR = "53010604";
    /** 身份证照片已上传 */
    public static final String IDPHOTO_UPLOADED_ERROR = "53010605";


	/*------直播购接口 -------*/
	/** 分销id非法 */
	public static final String ERROR_DISTRIBUTOR_ID = "5106051101";
	/** 分销id不存在 */
	public static final String NOT_DISTRIBUTOR = "5106051102";

	/** 商品id非法 */
	public static final String ERROR_PRODUCT_ID = "5106051201";
	/** 商品id不存在 */
	public static final String NOT_EXIST_PRODUCT = "5106051202";

	/** 分销商与商品不匹配 */
	public static final String NOT_MATCH_DISTRIBUTOR_PRODUCT = "5106051306";
	/** 商品已下架 */
	public static final String NOT_VALID_PRODUCT = "5106051307";
	/** 商品库存不足 */
	public static final String STOCK_NOT_FULL = "5106051308";

	/** 该id不是商家，没有权限*/
	public static final String NOT_SALE_ACTOR = "5106051406";
	/** 该id不是用户*/
	public static final String NOT_USER = "5106051407";

	/** 该id没有余额 */
	public static final String NOT_HAS_BALANCE_ACTOR = "5106051502";


	/** 提现金额不符合规则 */
	public static final String ERROR_WITHDRAW_MONEY = "5106051604";
	/** 未绑定银行卡 */
	public static final String NOT_BIND_BANK_ACCOUNT = "5106051605";

	/** 商家信息不存在 */
	public static final String NOT_EXIST_SALE_ACTOR = "5106051903";
}

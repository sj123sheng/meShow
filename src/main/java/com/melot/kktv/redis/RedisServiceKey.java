package com.melot.kktv.redis;

/**
 * 类说明：
 * <p>作者：宋建明<a href="mailto:jianming.song@melot.cn"></p>
 * <p>创建日期：2014-5-28</p>
 * <p>版本：V1.0</p>
 * <p>修改历史：</p>
 */
public class RedisServiceKey {

	/*
	 * Redis Source Service
	 */
	public static final String SERVICE_SOURCE_FAMILYHONORCACHE = "FamilyHonorCache"; // 家族荣誉Redis缓存
	
	/*
	 * Redis Key
	 */
	public static final String SERVICE_KEY_FAMILYSTATS_CONSUMETOTAL = "familyStats.consumeTotal.new_v2";
	public static final String SERVICE_KEY_FAMILYSTATS_MEDALCOUNT = "familyStats.medalCount_v2";
	public static final String SERVICE_KEY_FAMILYSTATS_MEMBERCOUNT = "familyStats.memberCount_v2";
	public static final String SERVICE_KEY_FAMILYSTATS_TOTALLIVE = "familyStats.totalLive_v2";
	public static final String SERVICE_KEY_FAMILYSTATS_CROWNCOUNT = "familyStats.crownCount_v2";
	public static final String SERVICE_KEY_FAMILYSTATS_DIAMONDCOUNT = "familyStats.diamondCount_v2";
	public static final String SERVICE_KEY_FAMILYSTATS_HEARTCOUNT = "familyStats.heartCount_v2";
	public static final String SERVICE_KEY_FAMILYSTATS_WEEKLYCONSUME = "familyStats.weeklyConsume_v2";
}

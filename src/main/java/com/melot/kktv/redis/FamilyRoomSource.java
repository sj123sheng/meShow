package com.melot.kktv.redis;

import java.util.List;

import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

import com.google.gson.Gson;
import com.melot.kktv.domain.PreviewAct;
import com.melot.kktv.util.redis.RedisConfigHelper;

/**
 * 专门用来操作家族房的redis工具类
 * <p></p>
 * @author fenggaopan 2015年10月21日 上午11:03:36
 * @version V1.0   
 * @modificationHistory=========================逻辑或功能性重大变更记录
 * @modify by user: {修改人} 2015年10月21日
 * @modify by reason:{方法名}:{原因}
 */
public class FamilyRoomSource {

	private static Logger logger = Logger.getLogger(FamilyRoomSource.class);
	
	public static final String FAMILY_ROOM_SOURCE = "famlilyRoom";
	
	private static Jedis getInstance() {
		return RedisConfigHelper.getJedis(FAMILY_ROOM_SOURCE);
	}
	
	/**
	 * 释放redis对象的资源
	 * @author fenggaopan 2015年10月21日 上午11:18:31
	 * @param jedis jedis对象
	 * @param errorFlag 是否出错
	 */
	/*private static void freeInstance(Jedis jedis, boolean errorFlag) {
		RedisConfigHelper.returnJedis(FAMILY_ROOM_SOURCE, jedis, errorFlag);
	}*/
	
	private static void freeInstance(Jedis jedis) {
		RedisConfigHelper.returnJedis(FAMILY_ROOM_SOURCE, jedis, false);
	}
	
	/**
	 * 将roomInfo的集合给保存到redis的缓存中去
	 * @author fenggaopan 2015年10月21日 上午11:19:01
	 * @param roomInfos room的list集合
	 */
	public static void saveTopRoomInfoToRedis(List<PreviewAct> roomInfos) {
		
		PreviewAct info1 = new PreviewAct();
		info1.setActBanner("破放手");
		PreviewAct info2 = new PreviewAct();
		info2.setFamilyName("法国飞");
		PreviewAct info3 = new PreviewAct();
		info3.setRoomTheme("跳舞");
		PreviewAct info4 = new PreviewAct();
		info4.setStatus("正在直播");
		
		roomInfos.add(info1);
		roomInfos.add(info2);
		roomInfos.add(info3);
		roomInfos.add(info4);
		
		Jedis jedis = null ;
		try {
			jedis = getInstance();
			jedis.set(FAMILY_ROOM_SOURCE,new Gson().toJson(roomInfos));
		}catch(Exception e) {
			
		}finally{
			if(jedis!=null) {
				freeInstance(jedis);
			}
		}
	}
	
	/**
	 * 返回家族直播房的信息
	 * @author fenggaopan 2015年10月22日 下午5:08:54
	 * @return 返回json
	 */
	public static String getJsonObjectFromRedis() {
		Jedis jedis = null ;
		try{
			jedis = getInstance();
			return jedis.get(FAMILY_ROOM_SOURCE);
		} catch(Exception e ) {
			logger.error("FamilyRoomSource.getJsonObjectFromRedis() execute exception.", e);
		} finally {
			if (jedis != null) {
				freeInstance(jedis) ;
			}
		}
		return null;
	}
	
}

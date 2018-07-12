package com.melot.kkcx.redis;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.melot.common.melot_jedis.JedisWrapper;
import com.melot.common.melot_jedis.RedisDataSourceFactory;
import redis.clients.jedis.Jedis;

/**
 * Title:
 * <p>
 * Description:
 * </p>
 *
 * @author 冯涛<a href="mailto:tao.feng@melot.cn"/>
 * @version V1.0
 * @since 2018/2/6.
 */
public class PeopleCountSource {
    private static final String GAME_BLACK_LIST = "game:black_list";

    public static final String SOURCENAME = "peopleCount";

    private PeopleCountSource(){}

    /**
     * 获取redis资源
     * @return
     */
    private static JedisWrapper getInstance() {
        return new JedisWrapper(RedisDataSourceFactory.getGlobalInstance().getJedisPool(SOURCENAME), SOURCENAME);
    }

    /**
     *	用户是否能参与游戏
     *	用户不在名单或者用户解禁时间已经过了则能参与游戏
     *
     *	构建缓存减少redis读写压力
     */
    public static boolean canPlay(int userId){
        boolean flag = false;
        String userKey =  userId + "";
        boolean errHappend = false;
        Throwable t = null;
        Jedis jedis = null;
        JedisWrapper jedisWrapper = null;
        try {
			jedisWrapper = getInstance();
			jedis = jedisWrapper.getJedis();
			Double limitTime = jedis.zscore(GAME_BLACK_LIST, userKey);
			if (limitTime == null) {
				limitTime = new Double(0);
			}
			flag = limitTime.longValue() < System.currentTimeMillis();
        } catch (Throwable e) {
            errHappend = true;
            t = e;
            throw new JsonParseException(t);
        }finally{
            if (jedisWrapper != null && jedis != null) {
                if(errHappend){
                    jedisWrapper.returnBrokenJedis(jedis, t);
                }else{
                    jedisWrapper.returnJedis(jedis);
                }
            }
        }
        return flag;
    }
}

package com.melot.kkgame.redis.external;


import com.melot.kkgame.redis.support.RedisException;
import com.melot.kkgame.redis.support.RedisTemplate;

public class LevelSource extends RedisTemplate{
	
	private static final String UPGRADE_LIST = "upgrade_list";

	@Override
    public String getSourceName() {
        return "Level";
    }
	
	/**
	 * 添加升级信息到队列
	 * @return
	 * @throws RedisException 
	 */
	public void pushToList(String msg) throws RedisException {
	    rpush(UPGRADE_LIST, msg);
	}

}

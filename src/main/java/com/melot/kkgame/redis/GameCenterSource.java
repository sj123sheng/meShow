/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2016
 */
package com.melot.kkgame.redis;

import java.util.Map;

import com.melot.kkgame.redis.support.RedisException;
import com.melot.kkgame.redis.support.RedisTemplate;

/**
 * Title: GameCenterSource
 * @author  姚国平<a href="mailto:guoping.yao@melot.cn">
 * @version V1.0
 * @since 2016-3-1 下午6:49:09 
 */
public class GameCenterSource extends RedisTemplate{

    
    public static final String GAME_LIST_KEY = "game_center_game_list_";

    @Override
    public String getSourceName() {
        return "RankingList";
    }

    public Map<String,String>getVersion( int appId,int platform) throws RedisException{
        final String key = "game_center_version_" + appId + "_" + platform;
        return hgetAll(key);
    }

    /***
     *  获取配置的游戏列表 
     */
    public String getGameList(int appId,int platform) throws RedisException{
       return get(GAME_LIST_KEY + appId + "_" + platform);
    }
    
}

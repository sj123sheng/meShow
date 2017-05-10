/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kkgame.redis;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.melot.kkgame.redis.support.RedisException;
import com.melot.kkgame.redis.support.RedisTemplate;

/**
 * Title: LiveTypeSource
 * <p>
 * Description: 
 * </p>
 * 
 * @author 陈磊<a href="mailto:lei.chen@melot.cn">
 * @version V1.0
 * @since 2015-7-16 上午10:23:28
 */
public class LiveTypeSource extends RedisTemplate{

    private static final String LIVE_TYPE_KEY = "live_type";
    
    @Override
    public String getSourceName() {
        return "RankingList";
    }
    
    
    //TODO: 基于首字母排序
    public JsonArray getAllHashLiveMap() throws RedisException{
        JsonArray typeArray = new JsonArray();
        Map<String, String> map = hgetAll(LIVE_TYPE_KEY);
        Map<String,String>sortMap = new TreeMap<String, String>(new ChineseCompara<String>());
        
        for (Map.Entry<String, String> entry : map.entrySet()) {
            sortMap.put(entry.getValue(), entry.getKey());
        }
        
        for (Map.Entry<String, String> entry : sortMap.entrySet()) {
            JsonObject json = new JsonObject();
            json.addProperty("cataId", entry.getValue());
            json.addProperty("cataName", entry.getKey());
            typeArray.add(json);
        }
        return typeArray;
    }
    
   @SuppressWarnings("hiding")
   final class ChineseCompara<String> implements Comparator<String>{
        @Override
        public int compare(Object o1, Object o2) {
            Comparator<Object> cmp = Collator.getInstance(Locale.CHINESE);
            int code = cmp.compare(o1, o2);
            if(code < 0 ){
                return -1;
            }
            return code == 0 ? 0 : 1;
        }
        
        
    }
    
    
    
    
    
    
    
    
}

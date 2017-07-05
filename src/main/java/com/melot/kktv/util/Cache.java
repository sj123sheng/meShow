/**
 * This document and its contents are protected by copyright 2012 and owned by Melot Inc.
 * The copying and reproduction of this document and/or its content (whether wholly or partly) or any
 * incorporation of the same into any other material in any media or format of any kind is strictly prohibited.
 * All rights are reserved.
 *
 * Copyright (c) Melot Inc. 2015
 */
package com.melot.kktv.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;

/**
 * Title: Cache
 * <p>
 * Description: 基于内存的缓存
 * </p>
 * @author  姚国平<a href="mailto:guoping.yao@melot.cn">
 * @version V1.0
 * @since 2015-2-11 上午11:00:24 
 */
public class Cache {

    private Logger logger = Logger.getLogger(Cache.class);
    
    private long cachetime; //缓存有效时间, 单位为毫秒
    private final Map<String, CacheObject> respository;
    private final ReadWriteLock readWriteLock;
    
    public Cache(long cachetime) {
        this.cachetime = cachetime;
        readWriteLock = new ReentrantReadWriteLock();
        respository = new HashMap<String, CacheObject>();
    }

    public Object getObject(String key) {
        readWriteLock.readLock().lock();
        try {
            CacheObject cj = respository.get(key);
            if (cj != null && !cj.isExpired(cachetime)) {
                return cj.getObject();
            }
        } finally {
            readWriteLock.readLock().unlock();
        }
        logger.info("cache fail,key["+key+"]");
        return null;
    }

    public Object getObject(String key, long cachetime) {
        readWriteLock.readLock().lock();
        try {
            CacheObject cj = respository.get(key);
            if (cj != null && !cj.isExpired(cachetime)) {
                return cj.getObject();
            }
        } finally {
            readWriteLock.readLock().unlock();
        }
        return null;
    }

    public Object getNoExpiredObject(String key) {
        readWriteLock.readLock().lock();
        try {
            CacheObject cj = respository.get(key);
            if (cj != null) {
                return cj.getObject();
            }
        } finally {
            readWriteLock.readLock().unlock();
        }
        return null;
    }

    public void insertObject(String key, Object object) {
        readWriteLock.writeLock().lock();
        try {
            if (object != null && key != null) {
                respository.put(key, new CacheObject(object));
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }
    }

    /**
     * 真正缓存对象 
     * 
     * 
     */
    static class CacheObject {
        private final Object object;
        private final long createTime;

        CacheObject(Object obj) {
            createTime = System.currentTimeMillis();
            this.object = obj;
        }

        public boolean isExpired(long timeout) {
            return (System.currentTimeMillis() - this.createTime) >= timeout;
        }

        public Object getObject() {
            return object;
        }
    }
    
}

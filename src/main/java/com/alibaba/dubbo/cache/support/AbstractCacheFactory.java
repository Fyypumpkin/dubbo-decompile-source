/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.cache.support;

import com.alibaba.dubbo.cache.Cache;
import com.alibaba.dubbo.cache.CacheFactory;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractCacheFactory
implements CacheFactory {
    private final ConcurrentMap<String, Cache> caches = new ConcurrentHashMap<String, Cache>();

    @Override
    public Cache getCache(URL url, Invocation invocation) {
        String key = (url = url.addParameter("method", invocation.getMethodName())).toFullString();
        Cache cache = (Cache)this.caches.get(key);
        if (cache == null) {
            this.caches.put(key, this.createCache(url));
            cache = (Cache)this.caches.get(key);
        }
        return cache;
    }

    protected abstract Cache createCache(URL var1);
}


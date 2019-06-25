/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.cache.support.lru;

import com.alibaba.dubbo.cache.Cache;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.LRUCache;
import java.util.Map;

public class LruCache
implements Cache {
    private final Map<Object, Object> store;

    public LruCache(URL url) {
        int max = url.getParameter("cache.size", 1000);
        this.store = new LRUCache<Object, Object>(max);
    }

    @Override
    public void put(Object key, Object value) {
        this.store.put(key, value);
    }

    @Override
    public Object get(Object key) {
        return this.store.get(key);
    }
}


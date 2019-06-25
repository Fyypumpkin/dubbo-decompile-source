/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.cache.support.threadlocal;

import com.alibaba.dubbo.cache.Cache;
import com.alibaba.dubbo.common.URL;
import java.util.HashMap;
import java.util.Map;

public class ThreadLocalCache
implements Cache {
    private final ThreadLocal<Map<Object, Object>> store = new ThreadLocal<Map<Object, Object>>(){

        @Override
        protected Map<Object, Object> initialValue() {
            return new HashMap<Object, Object>();
        }
    };

    public ThreadLocalCache(URL url) {
    }

    @Override
    public void put(Object key, Object value) {
        this.store.get().put(key, value);
    }

    @Override
    public Object get(Object key) {
        return this.store.get().get(key);
    }

}


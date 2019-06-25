/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.cache.support.lru;

import com.alibaba.dubbo.cache.Cache;
import com.alibaba.dubbo.cache.support.AbstractCacheFactory;
import com.alibaba.dubbo.cache.support.lru.LruCache;
import com.alibaba.dubbo.common.URL;

public class LruCacheFactory
extends AbstractCacheFactory {
    @Override
    protected Cache createCache(URL url) {
        return new LruCache(url);
    }
}


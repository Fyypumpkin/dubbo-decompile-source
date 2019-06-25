/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.cache.support.jcache;

import com.alibaba.dubbo.cache.Cache;
import com.alibaba.dubbo.cache.support.AbstractCacheFactory;
import com.alibaba.dubbo.cache.support.jcache.JCache;
import com.alibaba.dubbo.common.URL;

public class JCacheFactory
extends AbstractCacheFactory {
    @Override
    protected Cache createCache(URL url) {
        return new JCache(url);
    }
}


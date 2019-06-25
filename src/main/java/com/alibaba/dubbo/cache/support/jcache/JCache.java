/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  javax.cache.Cache
 *  javax.cache.CacheException
 *  javax.cache.CacheManager
 *  javax.cache.Caching
 *  javax.cache.configuration.Configuration
 *  javax.cache.configuration.Factory
 *  javax.cache.configuration.MutableConfiguration
 *  javax.cache.expiry.CreatedExpiryPolicy
 *  javax.cache.expiry.Duration
 *  javax.cache.spi.CachingProvider
 */
package com.alibaba.dubbo.cache.support.jcache;

import com.alibaba.dubbo.cache.Cache;
import com.alibaba.dubbo.common.URL;
import java.util.concurrent.TimeUnit;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.Configuration;
import javax.cache.configuration.Factory;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;

public class JCache
implements Cache {
    private final javax.cache.Cache<Object, Object> store;

    public JCache(URL url) {
        String method = url.getParameter("method", "");
        String key = url.getAddress() + "." + url.getServiceKey() + "." + method;
        String type = url.getParameter("jcache");
        CachingProvider provider = type == null || type.length() == 0 ? Caching.getCachingProvider() : Caching.getCachingProvider((String)type);
        CacheManager cacheManager = provider.getCacheManager();
        javax.cache.Cache cache = cacheManager.getCache(key);
        if (cache == null) {
            try {
                MutableConfiguration config = new MutableConfiguration().setTypes(Object.class, Object.class).setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf((Duration)new Duration(TimeUnit.MILLISECONDS, (long)url.getMethodParameter(method, "cache.write.expire", 60000)))).setStoreByValue(false).setManagementEnabled(true).setStatisticsEnabled(true);
                cache = cacheManager.createCache(key, (Configuration)config);
            }
            catch (CacheException e) {
                cache = cacheManager.getCache(key);
            }
        }
        this.store = cache;
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


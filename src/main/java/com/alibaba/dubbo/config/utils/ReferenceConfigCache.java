/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.config.utils;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.config.ReferenceConfig;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ReferenceConfigCache {
    public static final String DEFAULT_NAME = "_DEFAULT_";
    public static final KeyGenerator DEFAULT_KEY_GENERATOR = new KeyGenerator(){

        @Override
        public String generateKey(ReferenceConfig<?> referenceConfig) {
            String iName = referenceConfig.getInterface();
            if (StringUtils.isBlank(iName)) {
                Class<?> clazz = referenceConfig.getInterfaceClass();
                iName = clazz.getName();
            }
            if (StringUtils.isBlank(iName)) {
                throw new IllegalArgumentException("No interface info in ReferenceConfig" + referenceConfig);
            }
            StringBuilder ret = new StringBuilder();
            if (!StringUtils.isBlank(referenceConfig.getGroup())) {
                ret.append(referenceConfig.getGroup()).append("/");
            }
            ret.append(iName);
            if (!StringUtils.isBlank(referenceConfig.getVersion())) {
                ret.append(":").append(referenceConfig.getVersion());
            }
            return ret.toString();
        }
    };
    static final ConcurrentMap<String, ReferenceConfigCache> cacheHolder = new ConcurrentHashMap<String, ReferenceConfigCache>();
    private final String name;
    private final KeyGenerator generator;
    ConcurrentMap<String, ReferenceConfig<?>> cache = new ConcurrentHashMap();

    private ReferenceConfigCache(String name, KeyGenerator generator) {
        this.name = name;
        this.generator = generator;
    }

    public static ReferenceConfigCache getCache() {
        return ReferenceConfigCache.getCache(DEFAULT_NAME);
    }

    public static ReferenceConfigCache getCache(String name) {
        return ReferenceConfigCache.getCache(name, DEFAULT_KEY_GENERATOR);
    }

    public static ReferenceConfigCache getCache(String name, KeyGenerator keyGenerator) {
        ReferenceConfigCache cache = (ReferenceConfigCache)cacheHolder.get(name);
        if (cache != null) {
            return cache;
        }
        cacheHolder.putIfAbsent(name, new ReferenceConfigCache(name, keyGenerator));
        return (ReferenceConfigCache)cacheHolder.get(name);
    }

    public <T> T get(ReferenceConfig<T> referenceConfig) {
        String key = this.generator.generateKey(referenceConfig);
        ReferenceConfig config = (ReferenceConfig)this.cache.get(key);
        if (config != null) {
            return config.get();
        }
        this.cache.putIfAbsent(key, referenceConfig);
        config = (ReferenceConfig)this.cache.get(key);
        return config.get();
    }

    void destroyKey(String key) {
        ReferenceConfig config = (ReferenceConfig)this.cache.remove(key);
        if (config == null) {
            return;
        }
        config.destroy();
    }

    public <T> void destroy(ReferenceConfig<T> referenceConfig) {
        String key = this.generator.generateKey(referenceConfig);
        this.destroyKey(key);
    }

    public void destroyAll() {
        HashSet set = new HashSet(this.cache.keySet());
        for (String key : set) {
            this.destroyKey(key);
        }
    }

    public String toString() {
        return "ReferenceConfigCache(name: " + this.name + ")";
    }

    public static interface KeyGenerator {
        public String generateKey(ReferenceConfig<?> var1);
    }

}


/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.utils;

import com.alibaba.dubbo.common.utils.ConcurrentHashSet;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationCacheHelper {
    private static final Map<String, Set<String>> ignoredRegisters = new ConcurrentHashMap<String, Set<String>>();

    public static Set<String> findIgnoredApplication(String key) {
        return ignoredRegisters.get(key);
    }

    public static synchronized void cacheIgnoredApplication(String key, Set<String> value) {
        ConcurrentHashSet<String> cached = new ConcurrentHashSet<String>();
        cached.addAll(value);
        ignoredRegisters.put(key, cached);
    }
}


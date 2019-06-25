/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.registry.redis;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.registry.Registry;
import com.alibaba.dubbo.registry.RegistryFactory;
import com.alibaba.dubbo.registry.redis.RedisRegistry;

public class RedisRegistryFactory
implements RegistryFactory {
    @Override
    public Registry getRegistry(URL url) {
        return new RedisRegistry(url);
    }
}


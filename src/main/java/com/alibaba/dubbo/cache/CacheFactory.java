/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.cache;

import com.alibaba.dubbo.cache.Cache;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Adaptive;
import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.rpc.Invocation;

@SPI(value="lru")
public interface CacheFactory {
    @Adaptive(value={"cache"})
    public Cache getCache(URL var1, Invocation var2);
}


/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.filter.tps;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;

public interface TPSLimiter {
    public boolean isAllowable(URL var1, Invocation var2);
}


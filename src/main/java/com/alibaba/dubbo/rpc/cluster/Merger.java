/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster;

import com.alibaba.dubbo.common.extension.SPI;

@SPI
public interface Merger<T> {
    public /* varargs */ T merge(T ... var1);
}


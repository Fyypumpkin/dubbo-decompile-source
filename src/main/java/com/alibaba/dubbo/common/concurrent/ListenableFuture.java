/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.concurrent;

import java.util.concurrent.Executor;
import java.util.concurrent.Future;

public interface ListenableFuture<V>
extends Future<V> {
    public void addListener(Runnable var1, Executor var2);

    public void addListener(Runnable var1);
}


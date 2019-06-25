/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc;

import com.alibaba.dubbo.rpc.Invoker;

public interface Exporter<T> {
    public Invoker<T> getInvoker();

    public void unexport();
}


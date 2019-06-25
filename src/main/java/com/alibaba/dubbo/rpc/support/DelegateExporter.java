/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.support;

import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invoker;

public class DelegateExporter<T>
implements Exporter<T> {
    private final Exporter<T> exporter;

    public DelegateExporter(Exporter<T> exporter) {
        if (exporter == null) {
            throw new IllegalArgumentException("exporter can not be null");
        }
        this.exporter = exporter;
    }

    @Override
    public Invoker<T> getInvoker() {
        return this.exporter.getInvoker();
    }

    @Override
    public void unexport() {
        this.exporter.unexport();
    }
}


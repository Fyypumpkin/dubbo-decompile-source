/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.listener;

import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.ExporterListener;
import com.alibaba.dubbo.rpc.RpcException;

public abstract class ExporterListenerAdapter
implements ExporterListener {
    @Override
    public void exported(Exporter<?> exporter) throws RpcException {
    }

    @Override
    public void unexported(Exporter<?> exporter) throws RpcException {
    }
}


/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc;

import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.RpcException;

@SPI
public interface ExporterListener {
    public void exported(Exporter<?> var1) throws RpcException;

    public void unexported(Exporter<?> var1);
}


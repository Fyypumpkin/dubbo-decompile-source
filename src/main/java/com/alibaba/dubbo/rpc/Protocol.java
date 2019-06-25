/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Adaptive;
import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;

@SPI(value="dubbo")
public interface Protocol {
    public int getDefaultPort();

    @Adaptive
    public <T> Exporter<T> export(Invoker<T> var1) throws RpcException;

    @Adaptive
    public <T> Invoker<T> refer(Class<T> var1, URL var2) throws RpcException;

    public void destroy();

    default public void destroyServer() {
    }
}


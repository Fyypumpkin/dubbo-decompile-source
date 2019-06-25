/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.config.invoker;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.config.ServiceConfig;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;

public class DelegateProviderMetaDataInvoker<T>
implements Invoker {
    protected final Invoker<T> invoker;
    private ServiceConfig metadata;

    public DelegateProviderMetaDataInvoker(Invoker<T> invoker, ServiceConfig metadata) {
        this.invoker = invoker;
        this.metadata = metadata;
    }

    @Override
    public Class<T> getInterface() {
        return this.invoker.getInterface();
    }

    @Override
    public URL getUrl() {
        return this.invoker.getUrl();
    }

    @Override
    public boolean isAvailable() {
        return this.invoker.isAvailable();
    }

    @Override
    public Result invoke(Invocation invocation) throws RpcException {
        return this.invoker.invoke(invocation);
    }

    @Override
    public void destroy() {
        this.invoker.destroy();
    }

    public ServiceConfig getMetadata() {
        return this.metadata;
    }
}


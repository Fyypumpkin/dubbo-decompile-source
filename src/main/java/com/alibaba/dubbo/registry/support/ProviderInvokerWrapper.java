/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.registry.support;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;

public class ProviderInvokerWrapper<T>
implements Invoker {
    private Invoker<T> invoker;
    private URL originUrl;
    private URL registryUrl;
    private URL providerUrl;
    private volatile boolean isReg;

    public ProviderInvokerWrapper(Invoker<T> invoker, URL registryUrl, URL providerUrl) {
        this.invoker = invoker;
        this.originUrl = URL.valueOf(invoker.getUrl().toFullString());
        this.registryUrl = URL.valueOf(registryUrl.toFullString());
        this.providerUrl = providerUrl;
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

    public URL getOriginUrl() {
        return this.originUrl;
    }

    public URL getRegistryUrl() {
        return this.registryUrl;
    }

    public URL getProviderUrl() {
        return this.providerUrl;
    }

    public Invoker<T> getInvoker() {
        return this.invoker;
    }

    public boolean isReg() {
        return this.isReg;
    }

    public void setReg(boolean reg) {
        this.isReg = reg;
    }
}


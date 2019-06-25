/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster.directory;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.Router;
import com.alibaba.dubbo.rpc.cluster.directory.AbstractDirectory;
import java.util.List;

public class StaticDirectory<T>
extends AbstractDirectory<T> {
    private final List<Invoker<T>> invokers;

    public StaticDirectory(List<Invoker<T>> invokers) {
        this(null, invokers, null);
    }

    public StaticDirectory(List<Invoker<T>> invokers, List<Router> routers) {
        this(null, invokers, routers);
    }

    public StaticDirectory(URL url, List<Invoker<T>> invokers) {
        this(url, invokers, null);
    }

    public StaticDirectory(URL url, List<Invoker<T>> invokers, List<Router> routers) {
        super(url == null && invokers != null && invokers.size() > 0 ? invokers.get(0).getUrl() : url, routers);
        if (invokers == null || invokers.size() == 0) {
            throw new IllegalArgumentException("invokers == null");
        }
        this.invokers = invokers;
    }

    @Override
    public Class<T> getInterface() {
        return this.invokers.get(0).getInterface();
    }

    @Override
    public boolean isAvailable() {
        if (this.isDestroyed()) {
            return false;
        }
        for (Invoker<T> invoker : this.invokers) {
            if (!invoker.isAvailable()) continue;
            return true;
        }
        return false;
    }

    @Override
    public void destroy() {
        if (this.isDestroyed()) {
            return;
        }
        super.destroy();
        for (Invoker<T> invoker : this.invokers) {
            invoker.destroy();
        }
        this.invokers.clear();
    }

    @Override
    protected List<Invoker<T>> doList(Invocation invocation) throws RpcException {
        return this.invokers;
    }
}


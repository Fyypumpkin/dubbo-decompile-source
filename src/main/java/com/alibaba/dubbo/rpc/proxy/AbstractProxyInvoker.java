/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.proxy;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcResult;
import java.lang.reflect.InvocationTargetException;

public abstract class AbstractProxyInvoker<T>
implements Invoker<T> {
    private final T proxy;
    private final Class<T> type;
    private final URL url;

    public AbstractProxyInvoker(T proxy, Class<T> type, URL url) {
        if (proxy == null) {
            throw new IllegalArgumentException("proxy == null");
        }
        if (type == null) {
            throw new IllegalArgumentException("interface == null");
        }
        if (!type.isInstance(proxy)) {
            throw new IllegalArgumentException(proxy.getClass().getName() + " not implement interface " + type);
        }
        this.proxy = proxy;
        this.type = type;
        this.url = url;
    }

    @Override
    public Class<T> getInterface() {
        return this.type;
    }

    @Override
    public URL getUrl() {
        return this.url;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void destroy() {
    }

    @Override
    public Result invoke(Invocation invocation) throws RpcException {
        try {
            return new RpcResult(this.doInvoke(this.proxy, invocation.getMethodName(), invocation.getParameterTypes(), invocation.getArguments()));
        }
        catch (InvocationTargetException e) {
            return new RpcResult(e.getTargetException());
        }
        catch (Throwable e) {
            throw new RpcException("Failed to invoke remote proxy method " + invocation.getMethodName() + " to " + this.getUrl() + ", cause: " + e.getMessage(), e);
        }
    }

    protected abstract Object doInvoke(T var1, String var2, Class<?>[] var3, Object[] var4) throws Throwable;

    public String toString() {
        return this.getInterface() + " -> " + this.getUrl() == null ? " " : this.getUrl().toString();
    }
}


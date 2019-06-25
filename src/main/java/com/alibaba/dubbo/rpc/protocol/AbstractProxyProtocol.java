/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.protocol;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.rpc.Exporter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.ProxyFactory;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.protocol.AbstractExporter;
import com.alibaba.dubbo.rpc.protocol.AbstractInvoker;
import com.alibaba.dubbo.rpc.protocol.AbstractProtocol;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractProxyProtocol
extends AbstractProtocol {
    private final List<Class<?>> rpcExceptions = new CopyOnWriteArrayList();
    private ProxyFactory proxyFactory;

    public AbstractProxyProtocol() {
    }

    public /* varargs */ AbstractProxyProtocol(Class<?> ... exceptions) {
        for (Class<?> exception : exceptions) {
            this.addRpcException(exception);
        }
    }

    public void addRpcException(Class<?> exception) {
        this.rpcExceptions.add(exception);
    }

    public void setProxyFactory(ProxyFactory proxyFactory) {
        this.proxyFactory = proxyFactory;
    }

    public ProxyFactory getProxyFactory() {
        return this.proxyFactory;
    }

    @Override
    public <T> Exporter<T> export(Invoker<T> invoker) throws RpcException {
        final String uri = AbstractProxyProtocol.serviceKey(invoker.getUrl());
        Exporter exporter = (Exporter)this.exporterMap.get(uri);
        if (exporter != null) {
            return exporter;
        }
        final Runnable runnable = this.doExport(this.proxyFactory.getProxy(invoker), invoker.getInterface(), invoker.getUrl());
        exporter = new AbstractExporter<T>(invoker){

            @Override
            public void unexport() {
                super.unexport();
                AbstractProxyProtocol.this.exporterMap.remove(uri);
                if (runnable != null) {
                    try {
                        runnable.run();
                    }
                    catch (Throwable t) {
                        this.logger.warn(t.getMessage(), t);
                    }
                }
            }
        };
        this.exporterMap.put(uri, exporter);
        return exporter;
    }

    @Override
    public <T> Invoker<T> refer(final Class<T> type, final URL url) throws RpcException {
        final Invoker<T> tagert = this.proxyFactory.getInvoker(this.doRefer(type, url), type, url);
        AbstractInvoker invoker = new AbstractInvoker<T>(type, url){

            @Override
            protected Result doInvoke(Invocation invocation) throws Throwable {
                try {
                    Result result = tagert.invoke(invocation);
                    Throwable e = result.getException();
                    if (e != null) {
                        for (Class rpcException : AbstractProxyProtocol.this.rpcExceptions) {
                            if (!rpcException.isAssignableFrom(e.getClass())) continue;
                            throw AbstractProxyProtocol.this.getRpcException(type, url, invocation, e);
                        }
                    }
                    return result;
                }
                catch (RpcException e) {
                    if (e.getCode() == 0) {
                        e.setCode(AbstractProxyProtocol.this.getErrorCode(e.getCause()));
                    }
                    throw e;
                }
                catch (Throwable e) {
                    throw AbstractProxyProtocol.this.getRpcException(type, url, invocation, e);
                }
            }
        };
        this.invokers.add(invoker);
        return invoker;
    }

    protected RpcException getRpcException(Class<?> type, URL url, Invocation invocation, Throwable e) {
        RpcException re = new RpcException("Failed to invoke remote service: " + type + ", method: " + invocation.getMethodName() + ", cause: " + e.getMessage(), e);
        re.setCode(this.getErrorCode(e));
        return re;
    }

    protected int getErrorCode(Throwable e) {
        return 0;
    }

    protected abstract <T> Runnable doExport(T var1, Class<T> var2, URL var3) throws RpcException;

    protected abstract <T> T doRefer(Class<T> var1, URL var2) throws RpcException;

}


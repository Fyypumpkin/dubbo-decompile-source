/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.protocol.thrift;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.utils.AtomicPositiveInteger;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.TimeoutException;
import com.alibaba.dubbo.remoting.exchange.ExchangeClient;
import com.alibaba.dubbo.remoting.exchange.ResponseFuture;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.protocol.AbstractInvoker;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

public class ThriftInvoker<T>
extends AbstractInvoker<T> {
    private final ExchangeClient[] clients;
    private final AtomicPositiveInteger index = new AtomicPositiveInteger();
    private final ReentrantLock destroyLock = new ReentrantLock();
    private final Set<Invoker<?>> invokers;

    public ThriftInvoker(Class<T> service, URL url, ExchangeClient[] clients) {
        this(service, url, clients, null);
    }

    public ThriftInvoker(Class<T> type, URL url, ExchangeClient[] clients, Set<Invoker<?>> invokers) {
        super(type, url, new String[]{"interface", "group", "token", "timeout"});
        this.clients = clients;
        this.invokers = invokers;
    }

    @Override
    protected Result doInvoke(Invocation invocation) throws Throwable {
        RpcInvocation inv = (RpcInvocation)invocation;
        String methodName = invocation.getMethodName();
        inv.setAttachment("path", this.getUrl().getPath());
        inv.setAttachment("class.name.generator", this.getUrl().getParameter("class.name.generator", "dubbo"));
        ExchangeClient currentClient = this.clients.length == 1 ? this.clients[0] : this.clients[this.index.getAndIncrement() % this.clients.length];
        try {
            int timeout = this.getUrl().getMethodParameter(methodName, "timeout", 1000);
            RpcContext.getContext().setFuture(null);
            return (Result)currentClient.request(inv, timeout).get();
        }
        catch (TimeoutException e) {
            throw new RpcException(2, e.getMessage(), e);
        }
        catch (RemotingException e) {
            throw new RpcException(1, e.getMessage(), e);
        }
    }

    @Override
    public boolean isAvailable() {
        if (!super.isAvailable()) {
            return false;
        }
        for (ExchangeClient client : this.clients) {
            if (!client.isConnected() || client.hasAttribute("channel.readonly")) continue;
            return true;
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void destroy() {
        if (super.isDestroyed()) {
            return;
        }
        this.destroyLock.lock();
        try {
            if (super.isDestroyed()) {
                return;
            }
            super.destroy();
            if (this.invokers != null) {
                this.invokers.remove(this);
            }
            for (ExchangeClient client : this.clients) {
                try {
                    client.close();
                }
                catch (Throwable t) {
                    this.logger.warn(t.getMessage(), t);
                }
            }
        }
        finally {
            this.destroyLock.unlock();
        }
    }
}


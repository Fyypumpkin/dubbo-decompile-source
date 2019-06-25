/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.protocol.dubbo;

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
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.protocol.AbstractInvoker;
import com.alibaba.dubbo.rpc.protocol.dubbo.FutureAdapter;
import com.alibaba.dubbo.rpc.support.RpcUtils;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.locks.ReentrantLock;

public class DubboInvoker<T>
extends AbstractInvoker<T> {
    private final ExchangeClient[] clients;
    private final AtomicPositiveInteger index = new AtomicPositiveInteger();
    private final String version;
    private final ReentrantLock destroyLock = new ReentrantLock();
    private final Set<Invoker<?>> invokers;

    public DubboInvoker(Class<T> serviceType, URL url, ExchangeClient[] clients) {
        this(serviceType, url, clients, null);
    }

    public DubboInvoker(Class<T> serviceType, URL url, ExchangeClient[] clients, Set<Invoker<?>> invokers) {
        super(serviceType, url, new String[]{"interface", "group", "token", "timeout"});
        this.clients = clients;
        this.version = url.getParameter("version", "0.0.0");
        this.invokers = invokers;
    }

    @Override
    protected Result doInvoke(Invocation invocation) throws Throwable {
        RpcInvocation inv = (RpcInvocation)invocation;
        String methodName = RpcUtils.getMethodName(invocation);
        inv.setAttachment("path", this.getUrl().getPath());
        if (null == inv.getAttachment("version")) {
            inv.setAttachment("version", this.version);
        }
        ExchangeClient currentClient = this.clients.length == 1 ? this.clients[0] : this.clients[this.index.getAndIncrement() % this.clients.length];
        try {
            boolean isAsync = RpcUtils.isAsync(this.getUrl(), invocation);
            boolean isOneway = RpcUtils.isOneway(this.getUrl(), invocation);
            int timeout = this.getUrl().getMethodParameter(methodName, "timeout", 1000);
            if (isOneway) {
                boolean isSent = this.getUrl().getMethodParameter(methodName, "sent", false);
                currentClient.send(inv, isSent);
                RpcContext.getContext().setFuture(null);
                return new RpcResult();
            }
            if (isAsync) {
                ResponseFuture future = currentClient.request(inv, timeout);
                RpcContext.getContext().setFuture(new FutureAdapter(future));
                return new RpcResult();
            }
            RpcContext.getContext().setFuture(null);
            return (Result)currentClient.request(inv, timeout).get();
        }
        catch (TimeoutException e) {
            throw new RpcException(2, "Invoke remote method timeout. method: " + invocation.getMethodName() + ", provider: " + this.getUrl() + ", cause: " + e.getMessage(), e);
        }
        catch (RejectedExecutionException e) {
            throw new RpcException(8, "Failed to invoke remote method: " + invocation.getMethodName() + ", provider: " + this.getUrl() + ", cause: " + e.getMessage(), e);
        }
        catch (RemotingException e) {
            throw new RpcException(1, "Failed to invoke remote method: " + invocation.getMethodName() + ", provider: " + this.getUrl() + ", cause: " + e.getMessage(), e);
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


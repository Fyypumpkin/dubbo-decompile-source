/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster.support;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.Directory;
import com.alibaba.dubbo.rpc.cluster.LoadBalance;
import com.alibaba.dubbo.rpc.cluster.support.AbstractClusterInvoker;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ForkingClusterInvoker<T>
extends AbstractClusterInvoker<T> {
    private final ExecutorService executor = Executors.newCachedThreadPool(new NamedThreadFactory("forking-cluster-timer", true));

    public ForkingClusterInvoker(Directory<T> directory) {
        super(directory);
    }

    @Override
    public Result doInvoke(final Invocation invocation, List<Invoker<T>> invokers, LoadBalance loadbalance) throws RpcException {
        List selected;
        this.checkInvokers(invokers, invocation);
        int forks = this.getUrl().getParameter("forks", 2);
        int timeout = this.getUrl().getParameter("timeout", 1000);
        if (forks <= 0 || forks >= invokers.size()) {
            selected = invokers;
        } else {
            selected = new ArrayList();
            for (int i = 0; i < forks; ++i) {
                Invoker invoker = this.select(loadbalance, invocation, invokers, selected);
                if (selected.contains(invoker)) continue;
                selected.add(invoker);
            }
        }
        RpcContext.getContext().setInvokers(selected);
        final AtomicInteger count = new AtomicInteger();
        final LinkedBlockingQueue ref = new LinkedBlockingQueue();
        for (final Invoker<?> invoker : selected) {
            this.executor.execute(new Runnable(){

                @Override
                public void run() {
                    block2 : {
                        try {
                            Result result = invoker.invoke(invocation);
                            ref.offer(result);
                        }
                        catch (Throwable e) {
                            int value = count.incrementAndGet();
                            if (value < selected.size()) break block2;
                            ref.offer(e);
                        }
                    }
                }
            });
        }
        try {
            Object ret = ref.poll(timeout, TimeUnit.MILLISECONDS);
            if (ret instanceof Throwable) {
                Throwable e = (Throwable)ret;
                throw new RpcException(e instanceof RpcException ? ((RpcException)e).getCode() : 0, "Failed to forking invoke provider " + selected + ", but no luck to perform the invocation. Last error is: " + e.getMessage(), e.getCause() != null ? e.getCause() : e);
            }
            return (Result)ret;
        }
        catch (InterruptedException e) {
            throw new RpcException("Failed to forking invoke provider " + selected + ", but no luck to perform the invocation. Last error is: " + e.getMessage(), (Throwable)e);
        }
    }

}


/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster.support;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.cluster.Directory;
import com.alibaba.dubbo.rpc.cluster.LoadBalance;
import com.alibaba.dubbo.rpc.cluster.support.AbstractClusterInvoker;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class FailbackClusterInvoker<T>
extends AbstractClusterInvoker<T> {
    private static final Logger logger = LoggerFactory.getLogger(FailbackClusterInvoker.class);
    private static final long RETRY_FAILED_PERIOD = 5000L;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2, new NamedThreadFactory("failback-cluster-timer", true));
    private volatile ScheduledFuture<?> retryFuture;
    private final ConcurrentMap<Invocation, AbstractClusterInvoker<?>> failed = new ConcurrentHashMap();

    public FailbackClusterInvoker(Directory<T> directory) {
        super(directory);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void addFailed(Invocation invocation, AbstractClusterInvoker<?> router) {
        if (this.retryFuture == null) {
            FailbackClusterInvoker failbackClusterInvoker = this;
            synchronized (failbackClusterInvoker) {
                if (this.retryFuture == null) {
                    this.retryFuture = this.scheduledExecutorService.scheduleWithFixedDelay(new Runnable(){

                        @Override
                        public void run() {
                            try {
                                FailbackClusterInvoker.this.retryFailed();
                            }
                            catch (Throwable t) {
                                logger.error("Unexpected error occur at collect statistic", t);
                            }
                        }
                    }, 5000L, 5000L, TimeUnit.MILLISECONDS);
                }
            }
        }
        this.failed.put(invocation, router);
    }

    void retryFailed() {
        if (this.failed.size() == 0) {
            return;
        }
        for (Map.Entry<Invocation, AbstractClusterInvoker<?>> entry : new HashMap(this.failed).entrySet()) {
            Invocation invocation = entry.getKey();
            Invoker invoker = entry.getValue();
            try {
                invoker.invoke(invocation);
                this.failed.remove(invocation);
            }
            catch (Throwable e) {
                logger.error("Failed retry to invoke method " + invocation.getMethodName() + ", waiting again.", e);
            }
        }
    }

    @Override
    protected Result doInvoke(Invocation invocation, List<Invoker<T>> invokers, LoadBalance loadbalance) throws RpcException {
        try {
            this.checkInvokers(invokers, invocation);
            Invoker<T> invoker = this.select(loadbalance, invocation, invokers, null);
            return invoker.invoke(invocation);
        }
        catch (Throwable e) {
            logger.error("Failback to invoke method " + invocation.getMethodName() + ", wait for retry in background. Ignored exception: " + e.getMessage() + ", ", e);
            this.addFailed(invocation, this);
            return new RpcResult();
        }
    }

}


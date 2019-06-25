/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster.loadbalance;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.cluster.LoadBalance;
import java.util.List;

public abstract class AbstractLoadBalance
implements LoadBalance {
    @Override
    public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) {
        if (invokers == null || invokers.size() == 0) {
            return null;
        }
        if (invokers.size() == 1) {
            return invokers.get(0);
        }
        return this.doSelect(invokers, url, invocation);
    }

    protected abstract <T> Invoker<T> doSelect(List<Invoker<T>> var1, URL var2, Invocation var3);

    protected int getWeight(Invoker<?> invoker, Invocation invocation) {
        long timestamp;
        int weight = invoker.getUrl().getMethodParameter(invocation.getMethodName(), "weight", 100);
        if (weight > 0 && (timestamp = invoker.getUrl().getParameter("timestamp", 0L)) > 0L) {
            int uptime = (int)(System.currentTimeMillis() - timestamp);
            int warmup = invoker.getUrl().getParameter("warmup", 600000);
            if (uptime > 0 && uptime < warmup) {
                weight = AbstractLoadBalance.calculateWarmupWeight(uptime, warmup, weight);
            }
        }
        return weight;
    }

    static int calculateWarmupWeight(int uptime, int warmup, int weight) {
        int ww = (int)((float)uptime / ((float)warmup / (float)weight));
        return ww < 1 ? 1 : (ww > weight ? weight : ww);
    }
}


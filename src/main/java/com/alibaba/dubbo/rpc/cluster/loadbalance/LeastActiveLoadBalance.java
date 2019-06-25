/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster.loadbalance;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcStatus;
import com.alibaba.dubbo.rpc.cluster.loadbalance.AbstractLoadBalance;
import java.util.List;
import java.util.Random;

public class LeastActiveLoadBalance
extends AbstractLoadBalance {
    public static final String NAME = "leastactive";
    private final Random random = new Random();

    @Override
    protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation) {
        int length = invokers.size();
        int leastActive = -1;
        int leastCount = 0;
        int[] leastIndexs = new int[length];
        int totalWeight = 0;
        int firstWeight = 0;
        boolean sameWeight = true;
        for (int i = 0; i < length; ++i) {
            Invoker<T> invoker = invokers.get(i);
            int active = RpcStatus.getStatus(invoker.getUrl(), invocation.getMethodName()).getActive();
            int weight = invoker.getUrl().getMethodParameter(invocation.getMethodName(), "weight", 100);
            if (leastActive == -1 || active < leastActive) {
                leastActive = active;
                leastCount = 1;
                leastIndexs[0] = i;
                totalWeight = weight;
                firstWeight = weight;
                sameWeight = true;
                continue;
            }
            if (active != leastActive) continue;
            leastIndexs[leastCount++] = i;
            totalWeight += weight;
            if (!sameWeight || i <= 0 || weight == firstWeight) continue;
            sameWeight = false;
        }
        if (leastCount == 1) {
            return invokers.get(leastIndexs[0]);
        }
        if (!sameWeight && totalWeight > 0) {
            int offsetWeight = this.random.nextInt(totalWeight);
            for (int i = 0; i < leastCount; ++i) {
                int leastIndex = leastIndexs[i];
                if ((offsetWeight -= this.getWeight(invokers.get(leastIndex), invocation)) > 0) continue;
                return invokers.get(leastIndex);
            }
        }
        return invokers.get(leastIndexs[this.random.nextInt(leastCount)]);
    }
}


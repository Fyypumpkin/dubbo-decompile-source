/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster.loadbalance;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.cluster.loadbalance.AbstractLoadBalance;
import java.util.List;
import java.util.Random;

public class RandomLoadBalance
extends AbstractLoadBalance {
    public static final String NAME = "random";
    private final Random random = new Random();

    @Override
    protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation) {
        int length = invokers.size();
        int totalWeight = 0;
        boolean sameWeight = true;
        for (int i = 0; i < length; ++i) {
            int weight = this.getWeight(invokers.get(i), invocation);
            totalWeight += weight;
            if (!sameWeight || i <= 0 || weight == this.getWeight(invokers.get(i - 1), invocation)) continue;
            sameWeight = false;
        }
        if (totalWeight > 0 && !sameWeight) {
            int offset = this.random.nextInt(totalWeight);
            for (int i = 0; i < length; ++i) {
                if ((offset -= this.getWeight(invokers.get(i), invocation)) >= 0) continue;
                return invokers.get(i);
            }
        }
        return invokers.get(this.random.nextInt(length));
    }
}


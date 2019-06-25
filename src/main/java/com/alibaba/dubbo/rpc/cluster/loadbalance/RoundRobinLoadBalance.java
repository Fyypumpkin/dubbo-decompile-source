/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster.loadbalance;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.AtomicPositiveInteger;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.cluster.loadbalance.AbstractLoadBalance;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RoundRobinLoadBalance
extends AbstractLoadBalance {
    public static final String NAME = "roundrobin";
    private final ConcurrentMap<String, AtomicPositiveInteger> sequences = new ConcurrentHashMap<String, AtomicPositiveInteger>();
    private final ConcurrentMap<String, AtomicPositiveInteger> weightSequences = new ConcurrentHashMap<String, AtomicPositiveInteger>();

    @Override
    protected <T> Invoker<T> doSelect(List<Invoker<T>> invokers, URL url, Invocation invocation) {
        AtomicPositiveInteger sequence;
        String key = invokers.get(0).getUrl().getServiceKey() + "." + invocation.getMethodName();
        int length = invokers.size();
        int maxWeight = 0;
        int minWeight = Integer.MAX_VALUE;
        for (int i = 0; i < length; ++i) {
            int weight = this.getWeight(invokers.get(i), invocation);
            maxWeight = Math.max(maxWeight, weight);
            minWeight = Math.min(minWeight, weight);
        }
        if (maxWeight > 0 && minWeight < maxWeight) {
            AtomicPositiveInteger weightSequence = (AtomicPositiveInteger)this.weightSequences.get(key);
            if (weightSequence == null) {
                this.weightSequences.putIfAbsent(key, new AtomicPositiveInteger());
                weightSequence = (AtomicPositiveInteger)this.weightSequences.get(key);
            }
            int currentWeight = weightSequence.getAndIncrement() % maxWeight;
            ArrayList<Invoker<T>> weightInvokers = new ArrayList<Invoker<T>>();
            for (Invoker<T> invoker : invokers) {
                if (this.getWeight(invoker, invocation) <= currentWeight) continue;
                weightInvokers.add(invoker);
            }
            int weightLength = weightInvokers.size();
            if (weightLength == 1) {
                return (Invoker)weightInvokers.get(0);
            }
            if (weightLength > 1) {
                invokers = weightInvokers;
                length = invokers.size();
            }
        }
        if ((sequence = (AtomicPositiveInteger)this.sequences.get(key)) == null) {
            this.sequences.putIfAbsent(key, new AtomicPositiveInteger());
            sequence = (AtomicPositiveInteger)this.sequences.get(key);
        }
        return invokers.get(sequence.getAndIncrement() % length);
    }
}


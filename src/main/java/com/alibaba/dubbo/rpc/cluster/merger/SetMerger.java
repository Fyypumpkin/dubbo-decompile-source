/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster.merger;

import com.alibaba.dubbo.rpc.cluster.Merger;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SetMerger
implements Merger<Set<?>> {
    public /* varargs */ Set<Object> merge(Set<?> ... items) {
        HashSet<Object> result = new HashSet<Object>();
        for (Set<?> item : items) {
            if (item == null) continue;
            result.addAll(item);
        }
        return result;
    }
}


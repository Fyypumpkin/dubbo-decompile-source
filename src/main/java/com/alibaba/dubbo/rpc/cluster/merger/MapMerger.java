/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster.merger;

import com.alibaba.dubbo.rpc.cluster.Merger;
import java.util.HashMap;
import java.util.Map;

public class MapMerger
implements Merger<Map<?, ?>> {
    public /* varargs */ Map<?, ?> merge(Map<?, ?> ... items) {
        if (items.length == 0) {
            return null;
        }
        HashMap result = new HashMap();
        for (Map<?, ?> item : items) {
            if (item == null) continue;
            result.putAll(item);
        }
        return result;
    }
}


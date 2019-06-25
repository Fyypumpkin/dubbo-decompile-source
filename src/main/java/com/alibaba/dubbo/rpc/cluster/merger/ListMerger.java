/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster.merger;

import com.alibaba.dubbo.rpc.cluster.Merger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ListMerger
implements Merger<List<?>> {
    public /* varargs */ List<Object> merge(List<?> ... items) {
        ArrayList<Object> result = new ArrayList<Object>();
        for (List<?> item : items) {
            if (item == null) continue;
            result.addAll(item);
        }
        return result;
    }
}


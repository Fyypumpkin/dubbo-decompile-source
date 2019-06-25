/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster.merger;

import com.alibaba.dubbo.rpc.cluster.Merger;

public class LongArrayMerger
implements Merger<long[]> {
    public /* varargs */ long[] merge(long[] ... items) {
        int total = 0;
        for (long[] array : items) {
            total += array.length;
        }
        long[] result = new long[total];
        int index = 0;
        long[][] arrl = items;
        int array = arrl.length;
        for (int i = 0; i < array; ++i) {
            long[] array2;
            for (long item : array2 = arrl[i]) {
                result[index++] = item;
            }
        }
        return result;
    }
}


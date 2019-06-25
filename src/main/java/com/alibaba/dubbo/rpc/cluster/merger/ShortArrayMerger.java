/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster.merger;

import com.alibaba.dubbo.rpc.cluster.Merger;

public class ShortArrayMerger
implements Merger<short[]> {
    public /* varargs */ short[] merge(short[] ... items) {
        int total = 0;
        for (short[] array : items) {
            total += array.length;
        }
        short[] result = new short[total];
        int index = 0;
        short[][] arrs = items;
        int array = arrs.length;
        for (int i = 0; i < array; ++i) {
            short[] array2;
            for (short item : array2 = arrs[i]) {
                result[index++] = item;
            }
        }
        return result;
    }
}


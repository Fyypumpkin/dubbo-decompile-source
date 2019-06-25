/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster.merger;

import com.alibaba.dubbo.rpc.cluster.Merger;

public class IntArrayMerger
implements Merger<int[]> {
    public /* varargs */ int[] merge(int[] ... items) {
        int totalLen = 0;
        for (int[] item : items) {
            totalLen += item.length;
        }
        int[] result = new int[totalLen];
        int index = 0;
        int[][] arrn = items;
        int item = arrn.length;
        for (int i = 0; i < item; ++i) {
            int[] item2;
            for (int i2 : item2 = arrn[i]) {
                result[index++] = i2;
            }
        }
        return result;
    }
}


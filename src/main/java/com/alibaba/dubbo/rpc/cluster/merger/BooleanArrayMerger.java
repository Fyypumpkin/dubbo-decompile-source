/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster.merger;

import com.alibaba.dubbo.rpc.cluster.Merger;

public class BooleanArrayMerger
implements Merger<boolean[]> {
    public /* varargs */ boolean[] merge(boolean[] ... items) {
        int totalLen = 0;
        for (boolean[] array : items) {
            totalLen += array.length;
        }
        boolean[] result = new boolean[totalLen];
        int index = 0;
        boolean[][] arrbl = items;
        int array = arrbl.length;
        for (int i = 0; i < array; ++i) {
            boolean[] array2;
            for (boolean item : array2 = arrbl[i]) {
                result[index++] = item;
            }
        }
        return result;
    }
}


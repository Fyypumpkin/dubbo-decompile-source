/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster.merger;

import com.alibaba.dubbo.rpc.cluster.Merger;

public class DoubleArrayMerger
implements Merger<double[]> {
    public /* varargs */ double[] merge(double[] ... items) {
        int total = 0;
        for (double[] array : items) {
            total += array.length;
        }
        double[] result = new double[total];
        int index = 0;
        double[][] arrd = items;
        int array = arrd.length;
        for (int i = 0; i < array; ++i) {
            double[] array2;
            for (double item : array2 = arrd[i]) {
                result[index++] = item;
            }
        }
        return result;
    }
}


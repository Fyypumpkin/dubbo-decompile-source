/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster.merger;

import com.alibaba.dubbo.rpc.cluster.Merger;

public class FloatArrayMerger
implements Merger<float[]> {
    public /* varargs */ float[] merge(float[] ... items) {
        int total = 0;
        for (float[] array : items) {
            total += array.length;
        }
        float[] result = new float[total];
        int index = 0;
        float[][] arrf = items;
        int array = arrf.length;
        for (int i = 0; i < array; ++i) {
            float[] array2;
            for (float item : array2 = arrf[i]) {
                result[index++] = item;
            }
        }
        return result;
    }
}


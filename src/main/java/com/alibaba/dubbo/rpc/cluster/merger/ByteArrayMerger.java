/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster.merger;

import com.alibaba.dubbo.rpc.cluster.Merger;

public class ByteArrayMerger
implements Merger<byte[]> {
    public /* varargs */ byte[] merge(byte[] ... items) {
        int total = 0;
        for (byte[] array : items) {
            total += array.length;
        }
        byte[] result = new byte[total];
        int index = 0;
        byte[][] arrby = items;
        int array = arrby.length;
        for (int i = 0; i < array; ++i) {
            byte[] array2;
            for (byte item : array2 = arrby[i]) {
                result[index++] = item;
            }
        }
        return result;
    }
}


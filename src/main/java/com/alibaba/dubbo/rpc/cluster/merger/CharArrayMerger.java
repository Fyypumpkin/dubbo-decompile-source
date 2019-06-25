/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster.merger;

import com.alibaba.dubbo.rpc.cluster.Merger;

public class CharArrayMerger
implements Merger<char[]> {
    public /* varargs */ char[] merge(char[] ... items) {
        int total = 0;
        for (char[] array : items) {
            total += array.length;
        }
        char[] result = new char[total];
        int index = 0;
        char[][] arrc = items;
        int array = arrc.length;
        for (int i = 0; i < array; ++i) {
            char[] array2;
            for (char item : array2 = arrc[i]) {
                result[index++] = item;
            }
        }
        return result;
    }
}


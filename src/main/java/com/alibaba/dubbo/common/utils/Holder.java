/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.utils;

public class Holder<T> {
    private volatile T value;

    public void set(T value) {
        this.value = value;
    }

    public T get() {
        return this.value;
    }
}


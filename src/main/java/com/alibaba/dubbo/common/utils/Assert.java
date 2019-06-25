/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.utils;

public abstract class Assert {
    protected Assert() {
    }

    public static void notNull(Object obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
    }
}


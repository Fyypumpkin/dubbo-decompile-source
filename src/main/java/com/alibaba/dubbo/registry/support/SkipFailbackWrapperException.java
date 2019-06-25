/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.registry.support;

public class SkipFailbackWrapperException
extends RuntimeException {
    public SkipFailbackWrapperException(Throwable cause) {
        super(cause);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return null;
    }
}


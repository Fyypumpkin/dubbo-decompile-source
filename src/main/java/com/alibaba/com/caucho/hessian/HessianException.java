/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian;

public class HessianException
extends RuntimeException {
    public HessianException() {
    }

    public HessianException(String message) {
        super(message);
    }

    public HessianException(String message, Throwable rootCause) {
        super(message, rootCause);
    }

    public HessianException(Throwable rootCause) {
        super(rootCause);
    }
}


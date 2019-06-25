/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.com.caucho.hessian.io;

import java.io.IOException;

public class IOExceptionWrapper
extends IOException {
    private Throwable _cause;

    public IOExceptionWrapper(Throwable cause) {
        super(cause.toString());
        this._cause = cause;
    }

    public IOExceptionWrapper(String msg, Throwable cause) {
        super(msg);
        this._cause = cause;
    }

    @Override
    public Throwable getCause() {
        return this._cause;
    }
}


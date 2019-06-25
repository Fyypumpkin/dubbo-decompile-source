/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.exchange;

public interface ResponseCallback {
    public void done(Object var1);

    public void caught(Throwable var1);
}


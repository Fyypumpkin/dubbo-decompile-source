/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.exchange.support;

import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.exchange.ResponseCallback;
import com.alibaba.dubbo.remoting.exchange.ResponseFuture;

public class SimpleFuture
implements ResponseFuture {
    private final Object value;

    public SimpleFuture(Object value) {
        this.value = value;
    }

    @Override
    public Object get() throws RemotingException {
        return this.value;
    }

    @Override
    public Object get(int timeoutInMillis) throws RemotingException {
        return this.value;
    }

    @Override
    public void setCallback(ResponseCallback callback) {
        callback.done(this.value);
    }

    @Override
    public boolean isDone() {
        return true;
    }
}


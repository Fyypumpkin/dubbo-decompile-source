/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.exchange;

import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.exchange.ResponseCallback;

public interface ResponseFuture {
    public Object get() throws RemotingException;

    public Object get(int var1) throws RemotingException;

    public void setCallback(ResponseCallback var1);

    public boolean isDone();
}


/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.exchange;

import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.exchange.ExchangeHandler;
import com.alibaba.dubbo.remoting.exchange.ResponseFuture;

public interface ExchangeChannel
extends Channel {
    public ResponseFuture request(Object var1) throws RemotingException;

    public ResponseFuture request(Object var1, int var2) throws RemotingException;

    public ExchangeHandler getExchangeHandler();

    @Override
    public void close(int var1);
}


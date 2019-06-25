/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.exchange.support;

import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.exchange.ExchangeChannel;
import com.alibaba.dubbo.remoting.exchange.ExchangeHandler;
import com.alibaba.dubbo.remoting.telnet.support.TelnetHandlerAdapter;

public abstract class ExchangeHandlerAdapter
extends TelnetHandlerAdapter
implements ExchangeHandler {
    @Override
    public Object reply(ExchangeChannel channel, Object msg) throws RemotingException {
        return null;
    }
}


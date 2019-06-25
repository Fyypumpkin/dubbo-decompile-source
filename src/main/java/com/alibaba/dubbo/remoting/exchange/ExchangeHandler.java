/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.exchange;

import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.exchange.ExchangeChannel;
import com.alibaba.dubbo.remoting.telnet.TelnetHandler;

public interface ExchangeHandler
extends ChannelHandler,
TelnetHandler {
    public Object reply(ExchangeChannel var1, Object var2) throws RemotingException;
}


/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.exchange;

import com.alibaba.dubbo.remoting.Server;
import com.alibaba.dubbo.remoting.exchange.ExchangeChannel;
import java.net.InetSocketAddress;
import java.util.Collection;

public interface ExchangeServer
extends Server {
    public Collection<ExchangeChannel> getExchangeChannels();

    public ExchangeChannel getExchangeChannel(InetSocketAddress var1);
}


/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.exchange;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Adaptive;
import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.exchange.ExchangeClient;
import com.alibaba.dubbo.remoting.exchange.ExchangeHandler;
import com.alibaba.dubbo.remoting.exchange.ExchangeServer;

@SPI(value="header")
public interface Exchanger {
    @Adaptive(value={"exchanger"})
    public ExchangeServer bind(URL var1, ExchangeHandler var2) throws RemotingException;

    @Adaptive(value={"exchanger"})
    public ExchangeClient connect(URL var1, ExchangeHandler var2) throws RemotingException;
}


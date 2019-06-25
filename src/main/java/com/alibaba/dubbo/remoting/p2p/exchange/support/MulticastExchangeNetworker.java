/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.p2p.exchange.support;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.p2p.exchange.ExchangeGroup;
import com.alibaba.dubbo.remoting.p2p.exchange.ExchangeNetworker;
import com.alibaba.dubbo.remoting.p2p.exchange.support.MulticastExchangeGroup;

public class MulticastExchangeNetworker
implements ExchangeNetworker {
    @Override
    public ExchangeGroup lookup(URL url) throws RemotingException {
        return new MulticastExchangeGroup(url);
    }
}


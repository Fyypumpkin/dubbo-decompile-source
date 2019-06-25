/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.p2p.exchange;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.p2p.exchange.ExchangeGroup;

public interface ExchangeNetworker {
    public ExchangeGroup lookup(URL var1) throws RemotingException;
}


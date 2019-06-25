/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.p2p.exchange;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.exchange.ExchangeHandler;
import com.alibaba.dubbo.remoting.p2p.Group;
import com.alibaba.dubbo.remoting.p2p.exchange.ExchangePeer;

public interface ExchangeGroup
extends Group {
    public ExchangePeer join(URL var1, ExchangeHandler var2) throws RemotingException;
}


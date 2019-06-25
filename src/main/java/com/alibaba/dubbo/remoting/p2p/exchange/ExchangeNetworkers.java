/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.p2p.exchange;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.exchange.ExchangeHandler;
import com.alibaba.dubbo.remoting.p2p.exchange.ExchangeGroup;
import com.alibaba.dubbo.remoting.p2p.exchange.ExchangeNetworker;
import com.alibaba.dubbo.remoting.p2p.exchange.ExchangePeer;

public class ExchangeNetworkers {
    public static ExchangePeer join(String group, String peer, ExchangeHandler handler) throws RemotingException {
        return ExchangeNetworkers.join(URL.valueOf(group), URL.valueOf(peer), handler);
    }

    public static ExchangePeer join(URL group, URL peer, ExchangeHandler handler) throws RemotingException {
        return ExchangeNetworkers.lookup(group).join(peer, handler);
    }

    public static ExchangeGroup lookup(String group) throws RemotingException {
        return ExchangeNetworkers.lookup(URL.valueOf(group));
    }

    public static ExchangeGroup lookup(URL group) throws RemotingException {
        ExchangeNetworker networker = ExtensionLoader.getExtensionLoader(ExchangeNetworker.class).getExtension(group.getProtocol());
        return networker.lookup(group);
    }
}


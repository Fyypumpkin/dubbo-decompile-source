/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.p2p;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.p2p.Group;
import com.alibaba.dubbo.remoting.p2p.Networker;
import com.alibaba.dubbo.remoting.p2p.Peer;

public class Networkers {
    public static Peer join(String group, String peer, ChannelHandler handler) throws RemotingException {
        return Networkers.join(URL.valueOf(group), URL.valueOf(peer), handler);
    }

    public static Peer join(URL group, URL peer, ChannelHandler handler) throws RemotingException {
        return Networkers.lookup(group).join(peer, handler);
    }

    public static Group lookup(String group) throws RemotingException {
        return Networkers.lookup(URL.valueOf(group));
    }

    public static Group lookup(URL group) throws RemotingException {
        Networker networker = ExtensionLoader.getExtensionLoader(Networker.class).getExtension(group.getProtocol());
        return networker.lookup(group);
    }
}


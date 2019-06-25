/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.p2p;

import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.Server;

public interface Peer
extends Server {
    public void leave() throws RemotingException;
}


/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.p2p;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.p2p.Peer;

public interface Group {
    public URL getUrl();

    public Peer join(URL var1, ChannelHandler var2) throws RemotingException;

    public void leave(URL var1) throws RemotingException;

    public void close();
}


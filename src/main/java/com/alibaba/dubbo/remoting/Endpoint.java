/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;
import java.net.InetSocketAddress;

public interface Endpoint {
    public URL getUrl();

    public ChannelHandler getChannelHandler();

    public InetSocketAddress getLocalAddress();

    public void send(Object var1) throws RemotingException;

    public void send(Object var1, boolean var2) throws RemotingException;

    public void close();

    public void close(int var1);

    public boolean isClosed();
}


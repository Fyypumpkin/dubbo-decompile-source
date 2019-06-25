/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.transport.grizzly;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Client;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.Server;
import com.alibaba.dubbo.remoting.Transporter;
import com.alibaba.dubbo.remoting.transport.grizzly.GrizzlyClient;
import com.alibaba.dubbo.remoting.transport.grizzly.GrizzlyServer;

public class GrizzlyTransporter
implements Transporter {
    public static final String NAME = "grizzly";

    @Override
    public Server bind(URL url, ChannelHandler listener) throws RemotingException {
        return new GrizzlyServer(url, listener);
    }

    @Override
    public Client connect(URL url, ChannelHandler listener) throws RemotingException {
        return new GrizzlyClient(url, listener);
    }
}


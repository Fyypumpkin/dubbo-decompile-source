/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.transport.mina;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Client;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.Server;
import com.alibaba.dubbo.remoting.Transporter;
import com.alibaba.dubbo.remoting.transport.mina.MinaClient;
import com.alibaba.dubbo.remoting.transport.mina.MinaServer;

public class MinaTransporter
implements Transporter {
    public static final String NAME = "mina";

    @Override
    public Server bind(URL url, ChannelHandler handler) throws RemotingException {
        return new MinaServer(url, handler);
    }

    @Override
    public Client connect(URL url, ChannelHandler handler) throws RemotingException {
        return new MinaClient(url, handler);
    }
}


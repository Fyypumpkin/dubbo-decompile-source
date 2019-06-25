/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Adaptive;
import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Client;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.Server;

@SPI(value="netty")
public interface Transporter {
    @Adaptive(value={"server", "transporter"})
    public Server bind(URL var1, ChannelHandler var2) throws RemotingException;

    @Adaptive(value={"client", "transporter"})
    public Client connect(URL var1, ChannelHandler var2) throws RemotingException;
}


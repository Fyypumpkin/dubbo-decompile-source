/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting;

import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.RemotingException;

@SPI
public interface ChannelHandler {
    public void connected(Channel var1) throws RemotingException;

    public void disconnected(Channel var1) throws RemotingException;

    public void sent(Channel var1, Object var2) throws RemotingException;

    public void received(Channel var1, Object var2) throws RemotingException;

    public void caught(Channel var1, Throwable var2) throws RemotingException;
}


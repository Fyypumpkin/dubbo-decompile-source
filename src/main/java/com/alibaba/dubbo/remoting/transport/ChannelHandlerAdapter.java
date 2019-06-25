/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.transport;

import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;

public class ChannelHandlerAdapter
implements ChannelHandler {
    @Override
    public void connected(Channel channel) throws RemotingException {
    }

    @Override
    public void disconnected(Channel channel) throws RemotingException {
    }

    @Override
    public void sent(Channel channel, Object message) throws RemotingException {
    }

    @Override
    public void received(Channel channel, Object message) throws RemotingException {
    }

    @Override
    public void caught(Channel channel, Throwable exception) throws RemotingException {
    }
}


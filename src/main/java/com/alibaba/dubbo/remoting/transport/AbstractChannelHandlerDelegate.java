/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.transport;

import com.alibaba.dubbo.common.utils.Assert;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.transport.ChannelHandlerDelegate;

public abstract class AbstractChannelHandlerDelegate
implements ChannelHandlerDelegate {
    protected ChannelHandler handler;

    protected AbstractChannelHandlerDelegate(ChannelHandler handler) {
        Assert.notNull(handler, "handler == null");
        this.handler = handler;
    }

    @Override
    public ChannelHandler getHandler() {
        if (this.handler instanceof ChannelHandlerDelegate) {
            return ((ChannelHandlerDelegate)this.handler).getHandler();
        }
        return this.handler;
    }

    @Override
    public void connected(Channel channel) throws RemotingException {
        this.handler.connected(channel);
    }

    @Override
    public void disconnected(Channel channel) throws RemotingException {
        this.handler.disconnected(channel);
    }

    @Override
    public void sent(Channel channel, Object message) throws RemotingException {
        this.handler.sent(channel, message);
    }

    @Override
    public void received(Channel channel, Object message) throws RemotingException {
        this.handler.received(channel, message);
    }

    @Override
    public void caught(Channel channel, Throwable exception) throws RemotingException {
        this.handler.caught(channel, exception);
    }
}


/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.transport;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;
import java.net.InetSocketAddress;

public class ChannelDelegate
implements Channel {
    private transient Channel channel;

    public ChannelDelegate() {
    }

    public ChannelDelegate(Channel channel) {
        this.setChannel(channel);
    }

    public Channel getChannel() {
        return this.channel;
    }

    public void setChannel(Channel channel) {
        if (channel == null) {
            throw new IllegalArgumentException("channel == null");
        }
        this.channel = channel;
    }

    @Override
    public URL getUrl() {
        return this.channel.getUrl();
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return this.channel.getRemoteAddress();
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return this.channel.getChannelHandler();
    }

    @Override
    public boolean isConnected() {
        return this.channel.isConnected();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return this.channel.getLocalAddress();
    }

    @Override
    public boolean hasAttribute(String key) {
        return this.channel.hasAttribute(key);
    }

    @Override
    public void send(Object message) throws RemotingException {
        this.channel.send(message);
    }

    @Override
    public Object getAttribute(String key) {
        return this.channel.getAttribute(key);
    }

    @Override
    public void setAttribute(String key, Object value) {
        this.channel.setAttribute(key, value);
    }

    @Override
    public void send(Object message, boolean sent) throws RemotingException {
        this.channel.send(message, sent);
    }

    @Override
    public void removeAttribute(String key) {
        this.channel.removeAttribute(key);
    }

    @Override
    public void close() {
        this.channel.close();
    }

    @Override
    public void close(int timeout) {
        this.channel.close(timeout);
    }

    @Override
    public boolean isClosed() {
        return this.channel.isClosed();
    }
}


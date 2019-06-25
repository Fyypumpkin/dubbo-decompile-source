/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  io.netty.channel.Channel
 *  io.netty.channel.ChannelFuture
 *  io.netty.util.Attribute
 *  io.netty.util.AttributeKey
 */
package com.alibaba.dubbo.remoting.transport.netty4;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.transport.AbstractChannel;
import io.netty.channel.ChannelFuture;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class NettyChannel
extends AbstractChannel {
    private static final Logger logger = LoggerFactory.getLogger(NettyChannel.class);
    private static final ConcurrentMap<io.netty.channel.Channel, NettyChannel> channelMap = new ConcurrentHashMap<io.netty.channel.Channel, NettyChannel>();
    private final io.netty.channel.Channel channel;
    private final Map<String, Object> attributes = new ConcurrentHashMap<String, Object>();

    private NettyChannel(io.netty.channel.Channel channel, URL url, ChannelHandler handler) {
        super(url, handler);
        if (channel == null) {
            throw new IllegalArgumentException("netty channel == null;");
        }
        this.channel = channel;
    }

    static NettyChannel getOrAddChannel(io.netty.channel.Channel ch, URL url, ChannelHandler handler) {
        if (ch == null) {
            return null;
        }
        NettyChannel ret = (NettyChannel)channelMap.get((Object)ch);
        if (ret == null) {
            NettyChannel nettyChannel = new NettyChannel(ch, url, handler);
            if (ch.isActive()) {
                ret = channelMap.putIfAbsent(ch, nettyChannel);
            }
            if (ret == null) {
                ret = nettyChannel;
            }
        }
        return ret;
    }

    public static NettyChannel getChannel(io.netty.channel.Channel ch) {
        return (NettyChannel)channelMap.get((Object)ch);
    }

    public static void removeChannelIfDisconnected(io.netty.channel.Channel ch) {
        if (ch != null && !ch.isActive()) {
            channelMap.remove((Object)ch);
        }
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return (InetSocketAddress)this.channel.localAddress();
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return (InetSocketAddress)this.channel.remoteAddress();
    }

    @Override
    public boolean isConnected() {
        return !this.isClosed() && this.channel.isActive();
    }

    @Override
    public void send(Object message, boolean sent) throws RemotingException {
        super.send(message, sent);
        boolean success = true;
        int timeout = 0;
        try {
            Throwable cause;
            ChannelFuture future = this.channel.writeAndFlush(message);
            if (sent) {
                timeout = this.getUrl().getPositiveParameter("timeout", 1000);
                success = future.await((long)timeout);
            }
            if ((cause = future.cause()) != null) {
                throw cause;
            }
        }
        catch (Throwable e) {
            throw new RemotingException(this, "Failed to send message " + message + " to " + this.getRemoteAddress() + ", cause: " + e.getMessage(), e);
        }
        if (!success) {
            throw new RemotingException((Channel)this, "Failed to send message " + message + " to " + this.getRemoteAddress() + "in timeout(" + timeout + "ms) limit");
        }
    }

    @Override
    public void close() {
        try {
            super.close();
        }
        catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            NettyChannel.removeChannelIfDisconnected(this.channel);
        }
        catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            this.attributes.clear();
        }
        catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            if (logger.isInfoEnabled()) {
                logger.info("Close netty channel " + (Object)this.channel);
            }
            this.channel.close();
        }
        catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    @Override
    public boolean hasAttribute(String key) {
        return this.attributes.containsKey(key);
    }

    @Override
    public Object getAttribute(String key) {
        return this.attributes.get(key);
    }

    @Override
    public void setAttribute(String key, Object value) {
        if (value == null) {
            this.attributes.remove(key);
        } else {
            this.attributes.put(key, value);
        }
    }

    @Override
    public void removeAttribute(String key) {
        this.attributes.remove(key);
    }

    public boolean hasNettyAttribute(AttributeKey<?> key) {
        return this.channel.attr(key).get() != null;
    }

    public <T> T getNettyAttribute(AttributeKey<T> key) {
        return (T)this.channel.attr(key).get();
    }

    public <T> void setNettyAttribute(AttributeKey<T> key, T value) {
        this.channel.attr(key).set(value);
    }

    public <T> void removeNettyAttribute(AttributeKey<T> key) {
        this.channel.attr(key).set(null);
    }

    public io.netty.channel.Channel getWrappedChannel() {
        return this.channel;
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + (this.channel == null ? 0 : this.channel.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        NettyChannel other = (NettyChannel)obj;
        return !(this.channel == null ? other.channel != null : !this.channel.equals((Object)other.channel));
    }

    @Override
    public String toString() {
        return "NettyChannel [channel=" + (Object)this.channel + "]";
    }
}


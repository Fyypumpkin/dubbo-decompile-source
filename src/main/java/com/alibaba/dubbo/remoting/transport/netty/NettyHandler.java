/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.jboss.netty.channel.Channel
 *  org.jboss.netty.channel.ChannelHandler
 *  org.jboss.netty.channel.ChannelHandler$Sharable
 *  org.jboss.netty.channel.ChannelHandlerContext
 *  org.jboss.netty.channel.ChannelStateEvent
 *  org.jboss.netty.channel.ExceptionEvent
 *  org.jboss.netty.channel.MessageEvent
 *  org.jboss.netty.channel.SimpleChannelHandler
 */
package com.alibaba.dubbo.remoting.transport.netty;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.transport.netty.NettyChannel;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;

@ChannelHandler.Sharable
public class NettyHandler
extends SimpleChannelHandler {
    private final Map<String, Channel> channels = new ConcurrentHashMap<String, Channel>();
    private final URL url;
    private final ChannelHandler handler;

    public NettyHandler(URL url, ChannelHandler handler) {
        if (url == null) {
            throw new IllegalArgumentException("url == null");
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler == null");
        }
        this.url = url;
        this.handler = handler;
    }

    public Map<String, Channel> getChannels() {
        return this.channels;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.getChannel(), this.url, this.handler);
        try {
            if (channel != null) {
                this.channels.put(NetUtils.toAddressString((InetSocketAddress)ctx.getChannel().getRemoteAddress()), channel);
            }
            this.handler.connected(channel);
        }
        finally {
            NettyChannel.removeChannelIfDisconnected(ctx.getChannel());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.getChannel(), this.url, this.handler);
        try {
            this.channels.remove(NetUtils.toAddressString((InetSocketAddress)ctx.getChannel().getRemoteAddress()));
            this.handler.disconnected(channel);
        }
        finally {
            NettyChannel.removeChannelIfDisconnected(ctx.getChannel());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.getChannel(), this.url, this.handler);
        try {
            this.handler.received(channel, e.getMessage());
        }
        finally {
            NettyChannel.removeChannelIfDisconnected(ctx.getChannel());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        super.writeRequested(ctx, e);
        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.getChannel(), this.url, this.handler);
        try {
            this.handler.sent(channel, e.getMessage());
        }
        finally {
            NettyChannel.removeChannelIfDisconnected(ctx.getChannel());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.getChannel(), this.url, this.handler);
        try {
            this.handler.caught(channel, e.getCause());
        }
        finally {
            NettyChannel.removeChannelIfDisconnected(ctx.getChannel());
        }
    }
}


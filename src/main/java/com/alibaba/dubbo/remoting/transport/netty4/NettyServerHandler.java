/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  io.netty.channel.Channel
 *  io.netty.channel.ChannelDuplexHandler
 *  io.netty.channel.ChannelHandler
 *  io.netty.channel.ChannelHandler$Sharable
 *  io.netty.channel.ChannelHandlerContext
 *  io.netty.channel.ChannelPromise
 */
package com.alibaba.dubbo.remoting.transport.netty4;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.transport.netty4.NettyChannel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ChannelHandler.Sharable
public class NettyServerHandler
extends ChannelDuplexHandler {
    private final Map<String, Channel> channels = new ConcurrentHashMap<String, Channel>();
    private final URL url;
    private final ChannelHandler handler;

    public NettyServerHandler(URL url, ChannelHandler handler) {
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

    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), this.url, this.handler);
        try {
            if (channel != null) {
                this.channels.put(NetUtils.toAddressString((InetSocketAddress)ctx.channel().remoteAddress()), channel);
            }
            this.handler.connected(channel);
        }
        finally {
            NettyChannel.removeChannelIfDisconnected(ctx.channel());
        }
    }

    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), this.url, this.handler);
        try {
            this.channels.remove(NetUtils.toAddressString((InetSocketAddress)ctx.channel().remoteAddress()));
            this.handler.disconnected(channel);
        }
        finally {
            NettyChannel.removeChannelIfDisconnected(ctx.channel());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), this.url, this.handler);
        try {
            this.handler.received(channel, msg);
        }
        finally {
            NettyChannel.removeChannelIfDisconnected(ctx.channel());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);
        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), this.url, this.handler);
        try {
            this.handler.sent(channel, msg);
        }
        finally {
            NettyChannel.removeChannelIfDisconnected(ctx.channel());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        NettyChannel channel = NettyChannel.getOrAddChannel(ctx.channel(), this.url, this.handler);
        try {
            this.handler.caught(channel, cause);
        }
        finally {
            NettyChannel.removeChannelIfDisconnected(ctx.channel());
        }
    }
}


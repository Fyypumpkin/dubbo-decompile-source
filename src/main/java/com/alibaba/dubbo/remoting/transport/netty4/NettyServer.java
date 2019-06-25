/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  io.netty.bootstrap.AbstractBootstrap
 *  io.netty.bootstrap.ServerBootstrap
 *  io.netty.buffer.PooledByteBufAllocator
 *  io.netty.channel.Channel
 *  io.netty.channel.ChannelFuture
 *  io.netty.channel.ChannelHandler
 *  io.netty.channel.ChannelInitializer
 *  io.netty.channel.ChannelOption
 *  io.netty.channel.ChannelPipeline
 *  io.netty.channel.EventLoopGroup
 *  io.netty.channel.socket.SocketChannel
 *  io.netty.util.concurrent.DefaultThreadFactory
 *  io.netty.util.concurrent.Future
 */
package com.alibaba.dubbo.remoting.transport.netty4;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ExecutorUtil;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Codec2;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.Server;
import com.alibaba.dubbo.remoting.transport.AbstractServer;
import com.alibaba.dubbo.remoting.transport.dispatcher.ChannelHandlers;
import com.alibaba.dubbo.remoting.transport.netty4.NettyCodecAdapter;
import com.alibaba.dubbo.remoting.transport.netty4.NettyServerHandler;
import com.alibaba.dubbo.remoting.transport.netty4.NettySupport;
import io.netty.bootstrap.AbstractBootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.Future;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

public class NettyServer
extends AbstractServer
implements Server {
    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);
    protected Map<String, Channel> channels;
    protected ServerBootstrap bootstrap;
    protected io.netty.channel.Channel channel;
    protected EventLoopGroup bossGroup;
    protected EventLoopGroup workerGroup;
    protected NettySupport nettySupport;

    public NettyServer(URL url, ChannelHandler handler) throws RemotingException {
        super(url, ChannelHandlers.wrap(handler, ExecutorUtil.setThreadName(url, "DubboServerHandler")));
    }

    @Override
    protected void doOpen() throws Throwable {
        this.nettySupport = new NettySupport(this.getUrl());
        this.bootstrap = new ServerBootstrap();
        this.bossGroup = this.nettySupport.eventLoopGroup(1, (ThreadFactory)new DefaultThreadFactory("NettyServerBoss", true));
        this.workerGroup = this.nettySupport.eventLoopGroup((ThreadFactory)new DefaultThreadFactory("NettyServerWorker", true));
        final NettyServerHandler nettyServerHandler = new NettyServerHandler(this.getUrl(), this);
        this.channels = nettyServerHandler.getChannels();
        ((ServerBootstrap)this.bootstrap.group(this.bossGroup, this.workerGroup).channel(this.nettySupport.serverChannel())).childOption(ChannelOption.TCP_NODELAY, (Object)Boolean.TRUE).childOption(ChannelOption.SO_REUSEADDR, (Object)Boolean.TRUE).childOption(ChannelOption.ALLOCATOR, (Object)PooledByteBufAllocator.DEFAULT).childHandler((io.netty.channel.ChannelHandler)new ChannelInitializer<SocketChannel>(){

            protected void initChannel(SocketChannel ch) throws Exception {
                NettyCodecAdapter adapter = new NettyCodecAdapter(NettyServer.this.getCodec(), NettyServer.this.getUrl(), NettyServer.this);
                ch.pipeline().addLast("decoder", adapter.getDecoder()).addLast("encoder", adapter.getEncoder()).addLast("handler", (io.netty.channel.ChannelHandler)nettyServerHandler);
            }
        });
        ChannelFuture channelFuture = this.bootstrap.bind((SocketAddress)this.getBindAddress());
        channelFuture.syncUninterruptibly();
        this.channel = channelFuture.channel();
    }

    @Override
    protected void doClose() throws Throwable {
        try {
            if (this.channel != null) {
                this.channel.close();
            }
        }
        catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            Collection<Channel> channels = this.getChannels();
            if (channels != null && channels.size() > 0) {
                for (Channel channel : channels) {
                    try {
                        channel.close();
                    }
                    catch (Throwable e) {
                        logger.warn(e.getMessage(), e);
                    }
                }
            }
        }
        catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            if (this.bootstrap != null) {
                this.bossGroup.shutdownGracefully();
                this.workerGroup.shutdownGracefully();
            }
        }
        catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            if (this.channels != null) {
                this.channels.clear();
            }
        }
        catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
    }

    @Override
    public Collection<Channel> getChannels() {
        HashSet<Channel> chs = new HashSet<Channel>();
        for (Channel channel : this.channels.values()) {
            if (channel.isConnected()) {
                chs.add(channel);
                continue;
            }
            this.channels.remove(NetUtils.toAddressString(channel.getRemoteAddress()));
        }
        return chs;
    }

    @Override
    public Channel getChannel(InetSocketAddress remoteAddress) {
        return this.channels.get(NetUtils.toAddressString(remoteAddress));
    }

    @Override
    public boolean isBound() {
        return this.channel.isActive();
    }

}


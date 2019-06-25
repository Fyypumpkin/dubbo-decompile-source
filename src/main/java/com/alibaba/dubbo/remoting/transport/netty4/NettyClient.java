/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  io.netty.bootstrap.AbstractBootstrap
 *  io.netty.bootstrap.Bootstrap
 *  io.netty.buffer.PooledByteBufAllocator
 *  io.netty.channel.Channel
 *  io.netty.channel.ChannelFuture
 *  io.netty.channel.ChannelHandler
 *  io.netty.channel.ChannelInitializer
 *  io.netty.channel.ChannelOption
 *  io.netty.channel.ChannelPipeline
 *  io.netty.channel.EventLoopGroup
 *  io.netty.util.concurrent.DefaultThreadFactory
 */
package com.alibaba.dubbo.remoting.transport.netty4;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.Version;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Codec2;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.transport.AbstractClient;
import com.alibaba.dubbo.remoting.transport.netty4.NettyChannel;
import com.alibaba.dubbo.remoting.transport.netty4.NettyClientHandler;
import com.alibaba.dubbo.remoting.transport.netty4.NettyCodecAdapter;
import com.alibaba.dubbo.remoting.transport.netty4.NettySupport;
import io.netty.bootstrap.AbstractBootstrap;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class NettyClient
extends AbstractClient {
    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);
    protected Bootstrap bootstrap;
    protected volatile io.netty.channel.Channel channel;
    protected NettySupport nettySupport;
    private static final ConcurrentHashMap<Class, EventLoopGroup> eventLoopGroups = new ConcurrentHashMap();

    public NettyClient(URL url, ChannelHandler handler) throws RemotingException {
        super(url, NettyClient.wrapChannelHandler(url, handler));
    }

    @Override
    protected void doOpen() throws Throwable {
        final NettyClientHandler nettyClientHandler = new NettyClientHandler(this.getUrl(), this);
        this.nettySupport = new NettySupport(this.getUrl());
        this.bootstrap = new Bootstrap();
        ((Bootstrap)((Bootstrap)((Bootstrap)((Bootstrap)this.bootstrap.group(this.eventLoopGroup())).option(ChannelOption.SO_KEEPALIVE, (Object)true)).option(ChannelOption.TCP_NODELAY, (Object)true)).option(ChannelOption.ALLOCATOR, (Object)PooledByteBufAllocator.DEFAULT)).channel(this.nettySupport.clientChannel());
        if (this.getConnectTimeout() < 3000) {
            this.bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (Object)3000);
        } else {
            this.bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (Object)this.getConnectTimeout());
        }
        this.bootstrap.handler((io.netty.channel.ChannelHandler)new ChannelInitializer(){

            protected void initChannel(io.netty.channel.Channel ch) throws Exception {
                NettyCodecAdapter adapter = new NettyCodecAdapter(NettyClient.this.getCodec(), NettyClient.this.getUrl(), NettyClient.this);
                ch.pipeline().addLast("decoder", adapter.getDecoder()).addLast("encoder", adapter.getEncoder()).addLast("handler", (io.netty.channel.ChannelHandler)nettyClientHandler);
            }
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    protected EventLoopGroup eventLoopGroup() {
        EventLoopGroup group = eventLoopGroups.get(this.nettySupport.clientChannel());
        if (group != null) {
            return group;
        }
        ConcurrentHashMap<Class, EventLoopGroup> concurrentHashMap = eventLoopGroups;
        synchronized (concurrentHashMap) {
            group = eventLoopGroups.get(this.nettySupport.clientChannel());
            if (group != null) {
                return group;
            }
            group = this.nettySupport.eventLoopGroup(Constants.DEFAULT_IO_THREADS, (ThreadFactory)new DefaultThreadFactory("NettyClientWorker", true));
            eventLoopGroups.put(this.nettySupport.clientChannel(), group);
            return group;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void doConnect() throws Throwable {
        block21 : {
            long start = System.currentTimeMillis();
            ChannelFuture future = this.bootstrap.connect((SocketAddress)this.getConnectAddress());
            try {
                boolean ret = future.awaitUninterruptibly((long)this.getConnectTimeout(), TimeUnit.MILLISECONDS);
                if (ret && future.isSuccess()) {
                    io.netty.channel.Channel newChannel = future.channel();
                    try {
                        io.netty.channel.Channel oldChannel = this.channel;
                        if (oldChannel == null) break block21;
                        try {
                            if (logger.isInfoEnabled()) {
                                logger.info("Close old netty channel " + (Object)oldChannel + " on create new netty channel " + (Object)newChannel);
                            }
                            oldChannel.close();
                            break block21;
                        }
                        finally {
                            NettyChannel.removeChannelIfDisconnected(oldChannel);
                        }
                    }
                    finally {
                        if (this.isClosed()) {
                            try {
                                if (logger.isInfoEnabled()) {
                                    logger.info("Close new netty channel " + (Object)newChannel + ", because the client closed.");
                                }
                                newChannel.close();
                            }
                            finally {
                                this.channel = null;
                                NettyChannel.removeChannelIfDisconnected(newChannel);
                            }
                        } else {
                            this.channel = newChannel;
                        }
                    }
                }
                if (future.cause() != null) {
                    throw new RemotingException(this, "client(url: " + this.getUrl() + ") failed to connect to server " + this.getRemoteAddress() + ", error message is:" + future.cause().getMessage(), future.cause());
                }
                throw new RemotingException((Channel)this, "client(url: " + this.getUrl() + ") failed to connect to server " + this.getRemoteAddress() + " client-side timeout " + this.getConnectTimeout() + "ms (elapsed: " + (System.currentTimeMillis() - start) + "ms) from netty client " + NetUtils.getLocalHost() + " using dubbo version " + Version.getVersion());
            }
            finally {
                if (!this.isConnected()) {
                    // empty if block
                }
            }
        }
    }

    @Override
    protected void doDisConnect() throws Throwable {
        try {
            NettyChannel.removeChannelIfDisconnected(this.channel);
        }
        catch (Throwable t) {
            logger.warn(t.getMessage());
        }
    }

    @Override
    protected void doClose() throws Throwable {
    }

    @Override
    protected Channel getChannel() {
        io.netty.channel.Channel c = this.channel;
        if (c == null || !c.isActive()) {
            return null;
        }
        return NettyChannel.getOrAddChannel(c, this.getUrl(), this);
    }

}


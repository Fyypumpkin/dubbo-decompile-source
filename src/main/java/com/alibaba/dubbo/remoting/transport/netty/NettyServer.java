/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.jboss.netty.bootstrap.ServerBootstrap
 *  org.jboss.netty.channel.Channel
 *  org.jboss.netty.channel.ChannelFactory
 *  org.jboss.netty.channel.ChannelFuture
 *  org.jboss.netty.channel.ChannelHandler
 *  org.jboss.netty.channel.ChannelPipeline
 *  org.jboss.netty.channel.ChannelPipelineFactory
 *  org.jboss.netty.channel.Channels
 *  org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory
 */
package com.alibaba.dubbo.remoting.transport.netty;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ExecutorUtil;
import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Codec2;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.Server;
import com.alibaba.dubbo.remoting.transport.AbstractServer;
import com.alibaba.dubbo.remoting.transport.dispatcher.ChannelHandlers;
import com.alibaba.dubbo.remoting.transport.netty.NettyCodecAdapter;
import com.alibaba.dubbo.remoting.transport.netty.NettyHandler;
import com.alibaba.dubbo.remoting.transport.netty.NettyHelper;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

public class NettyServer
extends AbstractServer
implements Server {
    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);
    private Map<String, Channel> channels;
    private ServerBootstrap bootstrap;
    private org.jboss.netty.channel.Channel channel;

    public NettyServer(URL url, ChannelHandler handler) throws RemotingException {
        super(url, ChannelHandlers.wrap(handler, ExecutorUtil.setThreadName(url, "DubboServerHandler")));
    }

    @Override
    protected void doOpen() throws Throwable {
        NettyHelper.setNettyLoggerFactory();
        ExecutorService boss = Executors.newCachedThreadPool(new NamedThreadFactory("NettyServerBoss", true));
        ExecutorService worker = Executors.newCachedThreadPool(new NamedThreadFactory("NettyServerWorker", true));
        NioServerSocketChannelFactory channelFactory = new NioServerSocketChannelFactory((Executor)boss, (Executor)worker, this.getUrl().getPositiveParameter("iothreads", Constants.DEFAULT_IO_THREADS));
        this.bootstrap = new ServerBootstrap((ChannelFactory)channelFactory);
        final NettyHandler nettyHandler = new NettyHandler(this.getUrl(), this);
        this.channels = nettyHandler.getChannels();
        this.bootstrap.setPipelineFactory(new ChannelPipelineFactory(){

            public ChannelPipeline getPipeline() {
                NettyCodecAdapter adapter = new NettyCodecAdapter(NettyServer.this.getCodec(), NettyServer.this.getUrl(), NettyServer.this);
                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("decoder", adapter.getDecoder());
                pipeline.addLast("encoder", adapter.getEncoder());
                pipeline.addLast("handler", (org.jboss.netty.channel.ChannelHandler)nettyHandler);
                return pipeline;
            }
        });
        int backlog = this.getUrl().getParameter("backlog", 512);
        this.bootstrap.setOption("backlog", (Object)backlog);
        this.channel = this.bootstrap.bind((SocketAddress)this.getBindAddress());
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
                this.bootstrap.releaseExternalResources();
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
        return this.channel.isBound();
    }

}


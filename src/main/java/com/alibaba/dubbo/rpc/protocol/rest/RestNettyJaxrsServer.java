/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.jboss.netty.bootstrap.ServerBootstrap
 *  org.jboss.netty.channel.Channel
 *  org.jboss.netty.channel.ChannelFactory
 *  org.jboss.netty.channel.ChannelPipelineFactory
 *  org.jboss.netty.channel.group.ChannelGroup
 *  org.jboss.netty.channel.group.DefaultChannelGroup
 *  org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory
 *  org.jboss.resteasy.core.Dispatcher
 *  org.jboss.resteasy.core.SynchronousDispatcher
 *  org.jboss.resteasy.plugins.server.embedded.SecurityDomain
 *  org.jboss.resteasy.plugins.server.netty.HttpsServerPipelineFactory
 *  org.jboss.resteasy.plugins.server.netty.NettyJaxrsServer
 *  org.jboss.resteasy.plugins.server.netty.RequestDispatcher
 *  org.jboss.resteasy.spi.ResteasyDeployment
 *  org.jboss.resteasy.spi.ResteasyProviderFactory
 */
package com.alibaba.dubbo.rpc.protocol.rest;

import com.alibaba.dubbo.rpc.protocol.rest.ResteasyHttpServerPipelineFactory;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.net.ssl.SSLContext;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.plugins.server.embedded.SecurityDomain;
import org.jboss.resteasy.plugins.server.netty.HttpsServerPipelineFactory;
import org.jboss.resteasy.plugins.server.netty.NettyJaxrsServer;
import org.jboss.resteasy.plugins.server.netty.RequestDispatcher;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jboss.resteasy.spi.ResteasyProviderFactory;

public class RestNettyJaxrsServer
extends NettyJaxrsServer {
    private int ioWorkerCount = Runtime.getRuntime().availableProcessors() * 2;
    private SSLContext sslContext;
    private int executorThreadCount = 16;
    private int maxRequestSize = 10485760;
    private boolean isKeepAlive = true;
    static final ChannelGroup allChannels = new DefaultChannelGroup("RestNettyJaxrsServer");
    private int backlog = 512;

    public int getBacklog() {
        return this.backlog;
    }

    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    public void setIoWorkerCount(int ioWorkerCount) {
        this.ioWorkerCount = ioWorkerCount;
    }

    public void setExecutorThreadCount(int executorThreadCount) {
        this.executorThreadCount = executorThreadCount;
    }

    public void setMaxRequestSize(int maxRequestSize) {
        this.maxRequestSize = maxRequestSize;
    }

    public void setKeepAlive(boolean isKeepAlive) {
        this.isKeepAlive = isKeepAlive;
    }

    public void start() {
        this.deployment.start();
        RequestDispatcher dispatcher = new RequestDispatcher((SynchronousDispatcher)this.deployment.getDispatcher(), this.deployment.getProviderFactory(), this.domain);
        this.bootstrap = new ServerBootstrap((ChannelFactory)new NioServerSocketChannelFactory((Executor)Executors.newCachedThreadPool(), (Executor)Executors.newCachedThreadPool(), this.ioWorkerCount));
        this.bootstrap.setOption("backlog", (Object)this.backlog);
        ResteasyHttpServerPipelineFactory factory = this.sslContext == null ? new ResteasyHttpServerPipelineFactory(dispatcher, this.root, this.executorThreadCount, this.maxRequestSize, this.isKeepAlive) : new HttpsServerPipelineFactory(dispatcher, this.root, this.executorThreadCount, this.maxRequestSize, this.isKeepAlive, this.sslContext);
        this.bootstrap.setPipelineFactory((ChannelPipelineFactory)factory);
        this.channel = this.bootstrap.bind((SocketAddress)new InetSocketAddress(this.port));
        allChannels.add((Object)this.channel);
    }
}


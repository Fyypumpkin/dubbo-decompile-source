/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  io.netty.channel.EventLoopGroup
 *  io.netty.channel.epoll.EpollEventLoopGroup
 *  io.netty.channel.epoll.EpollServerSocketChannel
 *  io.netty.channel.epoll.EpollSocketChannel
 *  io.netty.channel.nio.NioEventLoopGroup
 *  io.netty.channel.socket.nio.NioServerSocketChannel
 *  io.netty.channel.socket.nio.NioSocketChannel
 */
package com.alibaba.dubbo.remoting.transport.netty4;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.transport.netty4.AbstractSupport;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.util.concurrent.ThreadFactory;

public class NettySupport
extends AbstractSupport {
    public NettySupport(URL url) {
        super(url);
    }

    public EventLoopGroup eventLoopGroup(ThreadFactory threadFactory) {
        return this.epoll ? this.epollEventLoopGroup(threadFactory) : this.nioEventLoopGroup(threadFactory);
    }

    public EventLoopGroup eventLoopGroup(int nThreads, ThreadFactory threadFactory) {
        return this.epoll ? this.epollEventLoopGroup(nThreads, threadFactory) : this.nioEventLoopGroup(nThreads, threadFactory);
    }

    public Class serverChannel() {
        return this.epoll ? EpollServerSocketChannel.class : NioServerSocketChannel.class;
    }

    public Class clientChannel() {
        return this.epoll ? EpollSocketChannel.class : NioSocketChannel.class;
    }
}


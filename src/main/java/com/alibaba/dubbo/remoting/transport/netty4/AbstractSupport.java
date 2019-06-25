/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  io.netty.channel.epoll.Epoll
 *  io.netty.channel.epoll.EpollEventLoopGroup
 *  io.netty.channel.nio.NioEventLoopGroup
 *  io.netty.util.internal.SystemPropertyUtil
 */
package com.alibaba.dubbo.remoting.transport.netty4;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.internal.SystemPropertyUtil;
import java.util.Locale;
import java.util.concurrent.ThreadFactory;

public abstract class AbstractSupport {
    private static final Logger logger = LoggerFactory.getLogger(AbstractSupport.class);
    protected URL url;
    protected int nThreads;
    protected boolean epoll;

    public AbstractSupport(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("url == null");
        }
        this.url = url;
        this.nThreads = url.getPositiveParameter("iothreads", Constants.DEFAULT_IO_THREADS);
        this.epoll = url.getParameter("epoll", false) && this.epollAvailable();
    }

    public NioEventLoopGroup nioEventLoopGroup(ThreadFactory threadFactory) {
        return new NioEventLoopGroup(this.nThreads, threadFactory);
    }

    public NioEventLoopGroup nioEventLoopGroup(int nThreads, ThreadFactory threadFactory) {
        return new NioEventLoopGroup(nThreads, threadFactory);
    }

    public EpollEventLoopGroup epollEventLoopGroup(ThreadFactory threadFactory) {
        return new EpollEventLoopGroup(this.nThreads, threadFactory);
    }

    public EpollEventLoopGroup epollEventLoopGroup(int nThreads, ThreadFactory threadFactory) {
        return new EpollEventLoopGroup(nThreads, threadFactory);
    }

    public URL getUrl() {
        return this.url;
    }

    protected boolean supportEpoll() {
        return this.epoll;
    }

    private boolean epollAvailable() {
        boolean linux = SystemPropertyUtil.get((String)"os.name", (String)"").toLowerCase(Locale.US).contains("linux");
        if (linux && logger.isDebugEnabled()) {
            logger.debug("Platform: Linux");
        }
        return linux && Epoll.isAvailable();
    }
}


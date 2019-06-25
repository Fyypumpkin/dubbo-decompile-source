/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.transport;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.store.DataStore;
import com.alibaba.dubbo.common.utils.ExecutorUtil;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.Server;
import com.alibaba.dubbo.remoting.transport.AbstractEndpoint;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

public abstract class AbstractServer
extends AbstractEndpoint
implements Server {
    private static final Logger logger = LoggerFactory.getLogger(AbstractServer.class);
    private InetSocketAddress localAddress = this.getUrl().toInetSocketAddress();
    private InetSocketAddress bindAddress;
    private int accepts;
    private int idleTimeout = 600;
    protected static final String SERVER_THREAD_POOL_NAME = "DubboServerHandler";
    ExecutorService executor;

    public AbstractServer(URL url, ChannelHandler handler) throws RemotingException {
        super(url, handler);
        String host = url.getParameter("anyhost", false) || NetUtils.isInvalidLocalHost(this.getUrl().getHost()) ? "0.0.0.0" : this.getUrl().getHost();
        this.bindAddress = new InetSocketAddress(host, this.getUrl().getPort());
        this.accepts = url.getParameter("accepts", 0);
        this.idleTimeout = url.getParameter("idle.timeout", 600000);
        try {
            this.doOpen();
            if (logger.isInfoEnabled()) {
                logger.info("Start " + this.getClass().getSimpleName() + " bind " + this.getBindAddress() + ", export " + this.getLocalAddress());
            }
        }
        catch (Throwable t) {
            throw new RemotingException(url.toInetSocketAddress(), null, "Failed to bind " + this.getClass().getSimpleName() + " on " + this.getLocalAddress() + ", cause: " + t.getMessage(), t);
        }
        this.executor = (ExecutorService)ExtensionLoader.getExtensionLoader(DataStore.class).getDefaultExtension().get(Constants.EXECUTOR_SERVICE_COMPONENT_KEY, Integer.toString(url.getPort()));
    }

    protected abstract void doOpen() throws Throwable;

    protected abstract void doClose() throws Throwable;

    @Override
    public void reset(URL url) {
        if (url == null) {
            return;
        }
        try {
            int a;
            if (url.hasParameter("accepts") && (a = url.getParameter("accepts", 0)) > 0) {
                this.accepts = a;
            }
        }
        catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
        try {
            if (url.hasParameter("idle.timeout") && (t = url.getParameter("idle.timeout", 0)) > 0) {
                this.idleTimeout = t;
            }
        }
        catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
        try {
            if (url.hasParameter("threads") && this.executor instanceof ThreadPoolExecutor && !this.executor.isShutdown()) {
                ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor)this.executor;
                int threads = url.getParameter("threads", 0);
                int max = threadPoolExecutor.getMaximumPoolSize();
                int core = threadPoolExecutor.getCorePoolSize();
                if (threads > 0 && (threads != max || threads != core)) {
                    if (threads < core) {
                        threadPoolExecutor.setCorePoolSize(threads);
                        if (core == max) {
                            threadPoolExecutor.setMaximumPoolSize(threads);
                        }
                    } else {
                        threadPoolExecutor.setMaximumPoolSize(threads);
                        if (core == max) {
                            threadPoolExecutor.setCorePoolSize(threads);
                        }
                    }
                }
            }
        }
        catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
        super.setUrl(this.getUrl().addParameters(url.getParameters()));
    }

    @Override
    public void send(Object message, boolean sent) throws RemotingException {
        Collection<Channel> channels = this.getChannels();
        for (Channel channel : channels) {
            if (!channel.isConnected()) continue;
            channel.send(message, sent);
        }
    }

    @Override
    public void close() {
        if (logger.isInfoEnabled()) {
            logger.info("Close " + this.getClass().getSimpleName() + " bind " + this.getBindAddress() + ", export " + this.getLocalAddress());
        }
        ExecutorUtil.shutdownNow(this.executor, 100);
        try {
            super.close();
        }
        catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            this.doClose();
        }
        catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
    }

    @Override
    public void close(int timeout) {
        ExecutorUtil.gracefulShutdown(this.executor, timeout);
        this.close();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return this.localAddress;
    }

    public InetSocketAddress getBindAddress() {
        return this.bindAddress;
    }

    public int getAccepts() {
        return this.accepts;
    }

    public int getIdleTimeout() {
        return this.idleTimeout;
    }

    @Override
    public void connected(Channel ch) throws RemotingException {
        Collection<Channel> channels = this.getChannels();
        if (this.accepts > 0 && channels.size() > this.accepts) {
            logger.error("Close channel " + ch + ", cause: The server " + ch.getLocalAddress() + " connections greater than max config " + this.accepts);
            ch.close();
            return;
        }
        super.connected(ch);
    }

    @Override
    public void disconnected(Channel ch) throws RemotingException {
        Collection<Channel> channels = this.getChannels();
        if (channels.size() == 0) {
            logger.warn("All clients has discontected from " + ch.getLocalAddress() + ". You can graceful shutdown now.");
        }
        super.disconnected(ch);
    }
}


/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.transport.dispatcher;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.store.DataStore;
import com.alibaba.dubbo.common.threadpool.ThreadPool;
import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.transport.ChannelHandlerDelegate;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WrappedChannelHandler
implements ChannelHandlerDelegate {
    protected static final Logger logger = LoggerFactory.getLogger(WrappedChannelHandler.class);
    protected static final ExecutorService SHARED_EXECUTOR = Executors.newCachedThreadPool(new NamedThreadFactory("DubboSharedHandler", true));
    protected final ExecutorService executor;
    protected final ChannelHandler handler;
    protected final URL url;

    public WrappedChannelHandler(ChannelHandler handler, URL url) {
        this.handler = handler;
        this.url = url;
        this.executor = (ExecutorService)ExtensionLoader.getExtensionLoader(ThreadPool.class).getAdaptiveExtension().getExecutor(url);
        String componentKey = Constants.EXECUTOR_SERVICE_COMPONENT_KEY;
        if ("consumer".equalsIgnoreCase(url.getParameter("side"))) {
            componentKey = "consumer";
        }
        DataStore dataStore = ExtensionLoader.getExtensionLoader(DataStore.class).getDefaultExtension();
        dataStore.put(componentKey, Integer.toString(url.getPort()), this.executor);
    }

    public void close() {
        try {
            if (this.executor instanceof ExecutorService) {
                this.executor.shutdown();
            }
        }
        catch (Throwable t) {
            logger.warn("fail to destroy thread pool of server: " + t.getMessage(), t);
        }
    }

    @Override
    public void connected(Channel channel) throws RemotingException {
        this.handler.connected(channel);
    }

    @Override
    public void disconnected(Channel channel) throws RemotingException {
        this.handler.disconnected(channel);
    }

    @Override
    public void sent(Channel channel, Object message) throws RemotingException {
        this.handler.sent(channel, message);
    }

    @Override
    public void received(Channel channel, Object message) throws RemotingException {
        this.handler.received(channel, message);
    }

    @Override
    public void caught(Channel channel, Throwable exception) throws RemotingException {
        this.handler.caught(channel, exception);
    }

    public ExecutorService getExecutor() {
        return this.executor;
    }

    @Override
    public ChannelHandler getHandler() {
        if (this.handler instanceof ChannelHandlerDelegate) {
            return ((ChannelHandlerDelegate)this.handler).getHandler();
        }
        return this.handler;
    }

    public URL getUrl() {
        return this.url;
    }
}


/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.transport;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.Version;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.store.DataStore;
import com.alibaba.dubbo.common.utils.ExecutorUtil;
import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Client;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.transport.AbstractEndpoint;
import com.alibaba.dubbo.remoting.transport.dispatcher.ChannelHandlers;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractClient
extends AbstractEndpoint
implements Client {
    private static final Logger logger = LoggerFactory.getLogger(AbstractClient.class);
    protected static final String CLIENT_THREAD_POOL_NAME = "DubboClientHandler";
    private static final AtomicInteger CLIENT_THREAD_POOL_ID = new AtomicInteger();
    private final Lock connectLock = new ReentrantLock();
    private static final ScheduledThreadPoolExecutor reconnectExecutorService = new ScheduledThreadPoolExecutor(2, new NamedThreadFactory("DubboClientReconnectTimer", true));
    private volatile ScheduledFuture<?> reconnectExecutorFuture = null;
    protected volatile ExecutorService executor;
    private final boolean send_reconnect;
    private final AtomicInteger reconnect_count = new AtomicInteger(0);
    private final AtomicBoolean reconnect_error_log_flag = new AtomicBoolean(false);
    private final int reconnect_warning_period;
    private long lastConnectedTime = System.currentTimeMillis();
    private final long shutdown_timeout;

    public AbstractClient(URL url, ChannelHandler handler) throws RemotingException {
        super(url, handler);
        this.send_reconnect = url.getParameter("send.reconnect", false);
        this.shutdown_timeout = url.getParameter("shutdown.timeout", 900000);
        this.reconnect_warning_period = url.getParameter("reconnect.waring.period", 1800);
        try {
            this.doOpen();
        }
        catch (Throwable t) {
            this.close();
            throw new RemotingException(url.toInetSocketAddress(), null, "Failed to start " + this.getClass().getSimpleName() + " " + NetUtils.getLocalAddress() + " connect to the server " + this.getRemoteAddress() + ", cause: " + t.getMessage(), t);
        }
        try {
            this.connect();
            if (logger.isInfoEnabled()) {
                logger.info("Start " + this.getClass().getSimpleName() + " " + NetUtils.getLocalAddress() + " connect to the server " + this.getRemoteAddress());
            }
        }
        catch (RemotingException t) {
            if (url.getParameter("check", true)) {
                this.close();
                throw t;
            }
            logger.warn("Failed to start " + this.getClass().getSimpleName() + " " + NetUtils.getLocalAddress() + " connect to the server " + this.getRemoteAddress() + " (check == false, ignore and retry later!), cause: " + t.getMessage(), t);
        }
        catch (Throwable t) {
            this.close();
            throw new RemotingException(url.toInetSocketAddress(), null, "Failed to start " + this.getClass().getSimpleName() + " " + NetUtils.getLocalAddress() + " connect to the server " + this.getRemoteAddress() + ", cause: " + t.getMessage(), t);
        }
        this.executor = (ExecutorService)ExtensionLoader.getExtensionLoader(DataStore.class).getDefaultExtension().get("consumer", Integer.toString(url.getPort()));
        ExtensionLoader.getExtensionLoader(DataStore.class).getDefaultExtension().remove("consumer", Integer.toString(url.getPort()));
    }

    protected static ChannelHandler wrapChannelHandler(URL url, ChannelHandler handler) {
        url = ExecutorUtil.setThreadName(url, CLIENT_THREAD_POOL_NAME);
        url = url.addParameterIfAbsent("threadpool", "cached");
        return ChannelHandlers.wrap(handler, url);
    }

    private synchronized void initConnectStatusCheckCommand() {
        int reconnect = AbstractClient.getReconnectParam(this.getUrl());
        if (reconnect > 0 && (this.reconnectExecutorFuture == null || this.reconnectExecutorFuture.isCancelled())) {
            Runnable connectStatusCheckCommand = new Runnable(){

                @Override
                public void run() {
                    block7 : {
                        try {
                            if (AbstractClient.this.isClosed()) {
                                ScheduledFuture future = AbstractClient.this.reconnectExecutorFuture;
                                if (future != null && !future.isCancelled()) {
                                    future.cancel(true);
                                }
                                return;
                            }
                            if (!AbstractClient.this.isConnected()) {
                                AbstractClient.this.connect();
                            } else {
                                AbstractClient.this.lastConnectedTime = System.currentTimeMillis();
                            }
                        }
                        catch (Throwable t) {
                            String errorMsg = "client reconnect to " + AbstractClient.this.getUrl().getAddress() + " find error . url: " + AbstractClient.this.getUrl();
                            if (System.currentTimeMillis() - AbstractClient.this.lastConnectedTime > AbstractClient.this.shutdown_timeout && !AbstractClient.this.reconnect_error_log_flag.get()) {
                                AbstractClient.this.reconnect_error_log_flag.set(true);
                                logger.error(errorMsg, t);
                                return;
                            }
                            if (AbstractClient.this.reconnect_count.getAndIncrement() % AbstractClient.this.reconnect_warning_period != 0) break block7;
                            logger.warn(errorMsg, t);
                        }
                    }
                }
            };
            this.reconnectExecutorFuture = reconnectExecutorService.scheduleWithFixedDelay(connectStatusCheckCommand, reconnect, reconnect, TimeUnit.MILLISECONDS);
        }
    }

    private static int getReconnectParam(URL url) {
        int reconnect;
        String param = url.getParameter("reconnect");
        if (param == null || param.length() == 0 || "true".equalsIgnoreCase(param)) {
            reconnect = 2000;
        } else if ("false".equalsIgnoreCase(param)) {
            reconnect = 0;
        } else {
            try {
                reconnect = Integer.parseInt(param);
            }
            catch (Exception e) {
                throw new IllegalArgumentException("reconnect param must be nonnegative integer or false/true. input is:" + param);
            }
            if (reconnect < 0) {
                throw new IllegalArgumentException("reconnect param must be nonnegative integer or false/true. input is:" + param);
            }
        }
        return reconnect;
    }

    private synchronized void destroyConnectStatusCheckCommand() {
        try {
            if (this.reconnectExecutorFuture != null && !this.reconnectExecutorFuture.isDone()) {
                this.reconnectExecutorFuture.cancel(true);
                reconnectExecutorService.purge();
            }
        }
        catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
    }

    protected ExecutorService createExecutor() {
        return Executors.newCachedThreadPool(new NamedThreadFactory(CLIENT_THREAD_POOL_NAME + CLIENT_THREAD_POOL_ID.incrementAndGet() + "-" + this.getUrl().getAddress(), true));
    }

    public InetSocketAddress getConnectAddress() {
        return new InetSocketAddress(NetUtils.filterLocalHost(this.getUrl().getHost()), this.getUrl().getPort());
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        Channel channel = this.getChannel();
        if (channel == null) {
            return this.getUrl().toInetSocketAddress();
        }
        return channel.getRemoteAddress();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        Channel channel = this.getChannel();
        if (channel == null) {
            return InetSocketAddress.createUnresolved(NetUtils.getLocalHost(), 0);
        }
        return channel.getLocalAddress();
    }

    @Override
    public boolean isConnected() {
        Channel channel = this.getChannel();
        if (channel == null) {
            return false;
        }
        return channel.isConnected();
    }

    @Override
    public Object getAttribute(String key) {
        Channel channel = this.getChannel();
        if (channel == null) {
            return null;
        }
        return channel.getAttribute(key);
    }

    @Override
    public void setAttribute(String key, Object value) {
        Channel channel = this.getChannel();
        if (channel == null) {
            return;
        }
        channel.setAttribute(key, value);
    }

    @Override
    public void removeAttribute(String key) {
        Channel channel = this.getChannel();
        if (channel == null) {
            return;
        }
        channel.removeAttribute(key);
    }

    @Override
    public boolean hasAttribute(String key) {
        Channel channel = this.getChannel();
        if (channel == null) {
            return false;
        }
        return channel.hasAttribute(key);
    }

    @Override
    public void send(Object message, boolean sent) throws RemotingException {
        Channel channel;
        if (this.send_reconnect && !this.isConnected()) {
            this.connect();
        }
        if ((channel = this.getChannel()) == null || !channel.isConnected()) {
            throw new RemotingException((Channel)this, "message can not send, because channel is closed . url:" + this.getUrl());
        }
        channel.send(message, sent);
    }

    protected void connect() throws RemotingException {
        this.connectLock.lock();
        try {
            if (this.isConnected()) {
                return;
            }
            this.initConnectStatusCheckCommand();
            this.doConnect();
            if (!this.isConnected()) {
                throw new RemotingException((Channel)this, "Failed connect to server " + this.getRemoteAddress() + " from " + this.getClass().getSimpleName() + " " + NetUtils.getLocalHost() + " using dubbo version " + Version.getVersion() + ", cause: Connect wait timeout: " + this.getTimeout() + "ms.");
            }
            if (logger.isInfoEnabled()) {
                logger.info("Successed connect to server " + this.getRemoteAddress() + " from " + this.getClass().getSimpleName() + " " + NetUtils.getLocalHost() + " using dubbo version " + Version.getVersion() + ", channel is " + this.getChannel());
            }
            this.reconnect_count.set(0);
            this.reconnect_error_log_flag.set(false);
        }
        catch (RemotingException e) {
            throw e;
        }
        catch (Throwable e) {
            throw new RemotingException(this, "Failed connect to server " + this.getRemoteAddress() + " from " + this.getClass().getSimpleName() + " " + NetUtils.getLocalHost() + " using dubbo version " + Version.getVersion() + ", cause: " + e.getMessage(), e);
        }
        finally {
            this.connectLock.unlock();
        }
    }

    public void disconnect() {
        this.connectLock.lock();
        try {
            this.destroyConnectStatusCheckCommand();
            try {
                Channel channel = this.getChannel();
                if (channel != null) {
                    channel.close();
                }
            }
            catch (Throwable e) {
                logger.warn(e.getMessage(), e);
            }
            try {
                this.doDisConnect();
            }
            catch (Throwable e) {
                logger.warn(e.getMessage(), e);
            }
        }
        finally {
            this.connectLock.unlock();
        }
    }

    @Override
    public void reconnect() throws RemotingException {
        if (!this.isConnected()) {
            this.connectLock.lock();
            try {
                if (!this.isConnected()) {
                    this.disconnect();
                    this.connect();
                }
            }
            finally {
                this.connectLock.unlock();
            }
        }
    }

    @Override
    public void close() {
        try {
            super.close();
        }
        catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            if (this.executor != null) {
                ExecutorUtil.shutdownNow(this.executor, 100);
            }
        }
        catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            this.disconnect();
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

    public String toString() {
        return this.getClass().getName() + " [" + this.getLocalAddress() + " -> " + this.getRemoteAddress() + "]";
    }

    protected abstract void doOpen() throws Throwable;

    protected abstract void doClose() throws Throwable;

    protected abstract void doConnect() throws Throwable;

    protected abstract void doDisConnect() throws Throwable;

    protected abstract Channel getChannel();

}


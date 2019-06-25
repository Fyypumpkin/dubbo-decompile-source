/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.exchange.support.header;

import com.alibaba.dubbo.common.Parameters;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Client;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.exchange.ExchangeChannel;
import com.alibaba.dubbo.remoting.exchange.ExchangeClient;
import com.alibaba.dubbo.remoting.exchange.ExchangeHandler;
import com.alibaba.dubbo.remoting.exchange.ResponseFuture;
import com.alibaba.dubbo.remoting.exchange.support.header.HeaderExchangeChannel;
import com.alibaba.dubbo.remoting.exchange.support.header.HeartBeatTask;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class HeaderExchangeClient
implements ExchangeClient {
    private static final Logger logger = LoggerFactory.getLogger(HeaderExchangeClient.class);
    private static final ScheduledThreadPoolExecutor scheduled = new ScheduledThreadPoolExecutor(2, new NamedThreadFactory("dubbo-remoting-client-heartbeat", true));
    private ScheduledFuture<?> heatbeatTimer;
    private int heartbeat;
    private int heartbeatTimeout;
    private final Client client;
    private final ExchangeChannel channel;

    public HeaderExchangeClient(Client client) {
        String dubbo;
        if (client == null) {
            throw new IllegalArgumentException("client == null");
        }
        this.client = client;
        this.channel = new HeaderExchangeChannel(client);
        this.heartbeat = client.getUrl().getParameter("heartbeat", (dubbo = client.getUrl().getParameter("dubbo")) != null && dubbo.startsWith("1.0.") ? 60000 : 0);
        this.heartbeatTimeout = client.getUrl().getParameter("heartbeat.timeout", this.heartbeat * 3);
        if (this.heartbeatTimeout < this.heartbeat * 2) {
            throw new IllegalStateException("heartbeatTimeout < heartbeatInterval * 2");
        }
        this.startHeatbeatTimer();
    }

    @Override
    public ResponseFuture request(Object request) throws RemotingException {
        return this.channel.request(request);
    }

    @Override
    public URL getUrl() {
        return this.channel.getUrl();
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return this.channel.getRemoteAddress();
    }

    @Override
    public ResponseFuture request(Object request, int timeout) throws RemotingException {
        return this.channel.request(request, timeout);
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return this.channel.getChannelHandler();
    }

    @Override
    public boolean isConnected() {
        return this.channel.isConnected();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return this.channel.getLocalAddress();
    }

    @Override
    public ExchangeHandler getExchangeHandler() {
        return this.channel.getExchangeHandler();
    }

    @Override
    public void send(Object message) throws RemotingException {
        this.channel.send(message);
    }

    @Override
    public void send(Object message, boolean sent) throws RemotingException {
        this.channel.send(message, sent);
    }

    @Override
    public boolean isClosed() {
        return this.channel.isClosed();
    }

    @Override
    public void close() {
        this.doClose();
        this.channel.close();
    }

    @Override
    public void close(int timeout) {
        this.doClose();
        this.channel.close(timeout);
    }

    @Override
    public void reset(URL url) {
        this.client.reset(url);
    }

    @Deprecated
    @Override
    public void reset(Parameters parameters) {
        this.reset(this.getUrl().addParameters(parameters.getParameters()));
    }

    @Override
    public void reconnect() throws RemotingException {
        this.client.reconnect();
    }

    @Override
    public Object getAttribute(String key) {
        return this.channel.getAttribute(key);
    }

    @Override
    public void setAttribute(String key, Object value) {
        this.channel.setAttribute(key, value);
    }

    @Override
    public void removeAttribute(String key) {
        this.channel.removeAttribute(key);
    }

    @Override
    public boolean hasAttribute(String key) {
        return this.channel.hasAttribute(key);
    }

    private void startHeatbeatTimer() {
        this.stopHeartbeatTimer();
        if (this.heartbeat > 0) {
            this.heatbeatTimer = scheduled.scheduleWithFixedDelay(new HeartBeatTask(new HeartBeatTask.ChannelProvider(){

                @Override
                public Collection<Channel> getChannels() {
                    return Collections.singletonList(HeaderExchangeClient.this);
                }
            }, this.heartbeat, this.heartbeatTimeout), this.heartbeat, this.heartbeat, TimeUnit.MILLISECONDS);
        }
    }

    private void stopHeartbeatTimer() {
        block3 : {
            if (this.heatbeatTimer != null && !this.heatbeatTimer.isCancelled()) {
                try {
                    this.heatbeatTimer.cancel(true);
                    scheduled.purge();
                }
                catch (Throwable e) {
                    if (!logger.isWarnEnabled()) break block3;
                    logger.warn(e.getMessage(), e);
                }
            }
        }
        this.heatbeatTimer = null;
    }

    private void doClose() {
        this.stopHeartbeatTimer();
    }

    public String toString() {
        return "HeaderExchangeClient [channel=" + this.channel + "]";
    }

}


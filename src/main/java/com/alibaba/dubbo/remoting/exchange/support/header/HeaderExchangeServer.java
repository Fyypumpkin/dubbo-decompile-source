/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.exchange.support.header;

import com.alibaba.dubbo.common.Parameters;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.Version;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.Server;
import com.alibaba.dubbo.remoting.exchange.ExchangeChannel;
import com.alibaba.dubbo.remoting.exchange.ExchangeServer;
import com.alibaba.dubbo.remoting.exchange.Request;
import com.alibaba.dubbo.remoting.exchange.support.header.HeaderExchangeChannel;
import com.alibaba.dubbo.remoting.exchange.support.header.HeartBeatTask;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class HeaderExchangeServer
implements ExchangeServer {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(1, new NamedThreadFactory("dubbo-remoting-server-heartbeat", true));
    private ScheduledFuture<?> heatbeatTimer;
    private int heartbeat;
    private int heartbeatTimeout;
    private final Server server;
    private volatile boolean closed = false;

    public HeaderExchangeServer(Server server) {
        if (server == null) {
            throw new IllegalArgumentException("server == null");
        }
        this.server = server;
        this.heartbeat = server.getUrl().getParameter("heartbeat", 0);
        this.heartbeatTimeout = server.getUrl().getParameter("heartbeat.timeout", this.heartbeat * 3);
        if (this.heartbeatTimeout < this.heartbeat * 2) {
            throw new IllegalStateException("heartbeatTimeout < heartbeatInterval * 2");
        }
        this.startHeatbeatTimer();
    }

    public Server getServer() {
        return this.server;
    }

    @Override
    public boolean isClosed() {
        return this.server.isClosed();
    }

    private boolean isRunning() {
        Collection<Channel> channels = this.getChannels();
        for (Channel channel : channels) {
            if (!channel.isConnected()) continue;
            return true;
        }
        return false;
    }

    @Override
    public void close() {
        this.doClose();
        this.server.close();
    }

    @Override
    public void close(int timeout) {
        if (timeout > 0) {
            long max = timeout;
            long start = System.currentTimeMillis();
            if (this.getUrl().getParameter("channel.readonly.send", true) && !"nova".equals(this.getUrl().getProtocol())) {
                this.sendChannelReadOnlyEvent();
            }
            while (this.isRunning() && System.currentTimeMillis() - start < max) {
                try {
                    Thread.sleep(10L);
                }
                catch (InterruptedException e) {
                    this.logger.warn(e.getMessage(), e);
                }
            }
        }
        this.doClose();
        this.server.close(timeout);
    }

    private void sendChannelReadOnlyEvent() {
        Request request = new Request();
        request.setEvent("R");
        request.setTwoWay(false);
        request.setVersion(Version.getVersion());
        Collection<Channel> channels = this.getChannels();
        for (Channel channel : channels) {
            try {
                if (!channel.isConnected()) continue;
                channel.send(request, this.getUrl().getParameter("channel.readonly.sent", true));
            }
            catch (RemotingException e) {
                this.logger.warn("send connot write messge error.", e);
            }
        }
    }

    private void doClose() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        this.stopHeartbeatTimer();
        try {
            this.scheduled.shutdown();
        }
        catch (Throwable t) {
            this.logger.warn(t.getMessage(), t);
        }
    }

    @Override
    public Collection<ExchangeChannel> getExchangeChannels() {
        ArrayList<ExchangeChannel> exchangeChannels = new ArrayList<ExchangeChannel>();
        Collection<Channel> channels = this.server.getChannels();
        if (channels != null && channels.size() > 0) {
            for (Channel channel : channels) {
                exchangeChannels.add(HeaderExchangeChannel.getOrAddChannel(channel));
            }
        }
        return exchangeChannels;
    }

    @Override
    public ExchangeChannel getExchangeChannel(InetSocketAddress remoteAddress) {
        Channel channel = this.server.getChannel(remoteAddress);
        return HeaderExchangeChannel.getOrAddChannel(channel);
    }

    @Override
    public Collection<Channel> getChannels() {
        return this.getExchangeChannels();
    }

    @Override
    public Channel getChannel(InetSocketAddress remoteAddress) {
        return this.getExchangeChannel(remoteAddress);
    }

    @Override
    public boolean isBound() {
        return this.server.isBound();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return this.server.getLocalAddress();
    }

    @Override
    public URL getUrl() {
        return this.server.getUrl();
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return this.server.getChannelHandler();
    }

    @Override
    public void reset(URL url) {
        this.server.reset(url);
        try {
            if (url.hasParameter("heartbeat") || url.hasParameter("heartbeat.timeout")) {
                int h = url.getParameter("heartbeat", this.heartbeat);
                int t = url.getParameter("heartbeat.timeout", h * 3);
                if (t < h * 2) {
                    throw new IllegalStateException("heartbeatTimeout < heartbeatInterval * 2");
                }
                if (h != this.heartbeat || t != this.heartbeatTimeout) {
                    this.heartbeat = h;
                    this.heartbeatTimeout = t;
                    this.startHeatbeatTimer();
                }
            }
        }
        catch (Throwable t) {
            this.logger.error(t.getMessage(), t);
        }
    }

    @Deprecated
    @Override
    public void reset(Parameters parameters) {
        this.reset(this.getUrl().addParameters(parameters.getParameters()));
    }

    @Override
    public void send(Object message) throws RemotingException {
        if (this.closed) {
            throw new RemotingException(this.getLocalAddress(), null, "Failed to send message " + message + ", cause: The server " + this.getLocalAddress() + " is closed!");
        }
        this.server.send(message);
    }

    @Override
    public void send(Object message, boolean sent) throws RemotingException {
        if (this.closed) {
            throw new RemotingException(this.getLocalAddress(), null, "Failed to send message " + message + ", cause: The server " + this.getLocalAddress() + " is closed!");
        }
        this.server.send(message, sent);
    }

    private void startHeatbeatTimer() {
        this.stopHeartbeatTimer();
        if (this.heartbeat > 0) {
            this.heatbeatTimer = this.scheduled.scheduleWithFixedDelay(new HeartBeatTask(new HeartBeatTask.ChannelProvider(){

                @Override
                public Collection<Channel> getChannels() {
                    return Collections.unmodifiableCollection(HeaderExchangeServer.this.getChannels());
                }
            }, this.heartbeat, this.heartbeatTimeout), this.heartbeat, this.heartbeat, TimeUnit.MILLISECONDS);
        }
    }

    private void stopHeartbeatTimer() {
        try {
            ScheduledFuture<?> timer = this.heatbeatTimer;
            if (timer != null && !timer.isCancelled()) {
                timer.cancel(true);
            }
        }
        catch (Throwable t) {
            this.logger.warn(t.getMessage(), t);
        }
        finally {
            this.heatbeatTimer = null;
        }
    }

}


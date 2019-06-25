/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.exchange.support.header;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.exchange.ExchangeChannel;
import com.alibaba.dubbo.remoting.exchange.ExchangeHandler;
import com.alibaba.dubbo.remoting.exchange.Request;
import com.alibaba.dubbo.remoting.exchange.Response;
import com.alibaba.dubbo.remoting.exchange.ResponseFuture;
import com.alibaba.dubbo.remoting.exchange.support.DefaultFuture;
import java.net.InetSocketAddress;

final class HeaderExchangeChannel
implements ExchangeChannel {
    private static final Logger logger = LoggerFactory.getLogger(HeaderExchangeChannel.class);
    private static final String CHANNEL_KEY = HeaderExchangeChannel.class.getName() + ".CHANNEL";
    private final Channel channel;
    private volatile boolean closed = false;

    HeaderExchangeChannel(Channel channel) {
        if (channel == null) {
            throw new IllegalArgumentException("channel == null");
        }
        this.channel = channel;
    }

    static HeaderExchangeChannel getOrAddChannel(Channel ch) {
        if (ch == null) {
            return null;
        }
        HeaderExchangeChannel ret = (HeaderExchangeChannel)ch.getAttribute(CHANNEL_KEY);
        if (ret == null) {
            ret = new HeaderExchangeChannel(ch);
            if (ch.isConnected()) {
                ch.setAttribute(CHANNEL_KEY, ret);
            }
        }
        return ret;
    }

    static void removeChannelIfDisconnected(Channel ch) {
        if (ch != null && !ch.isConnected()) {
            ch.removeAttribute(CHANNEL_KEY);
        }
    }

    @Override
    public void send(Object message) throws RemotingException {
        this.send(message, this.getUrl().getParameter("sent", false));
    }

    @Override
    public void send(Object message, boolean sent) throws RemotingException {
        if (this.closed) {
            throw new RemotingException(this.getLocalAddress(), null, "Failed to send message " + message + ", cause: The channel " + this + " is closed!");
        }
        if (message instanceof Request || message instanceof Response || message instanceof String) {
            this.channel.send(message, sent);
        } else {
            Request request = new Request();
            request.setVersion("2.0.0");
            request.setTwoWay(false);
            request.setData(message);
            this.channel.send(request, sent);
        }
    }

    @Override
    public ResponseFuture request(Object request) throws RemotingException {
        return this.request(request, this.channel.getUrl().getPositiveParameter("timeout", 1000));
    }

    @Override
    public ResponseFuture request(Object request, int timeout) throws RemotingException {
        if (this.closed) {
            throw new RemotingException(this.getLocalAddress(), null, "Failed to send request " + request + ", cause: The channel " + this + " is closed!");
        }
        Request req = new Request();
        req.setVersion("2.0.0");
        req.setTwoWay(true);
        req.setData(request);
        DefaultFuture future = new DefaultFuture(this.channel, req, timeout);
        try {
            this.channel.send(req);
        }
        catch (RemotingException e) {
            future.cancel();
            throw e;
        }
        return future;
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }

    @Override
    public void close() {
        try {
            this.channel.close();
        }
        catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
    }

    @Override
    public void close(int timeout) {
        if (this.closed) {
            return;
        }
        this.closed = true;
        if (timeout > 0) {
            long start = System.currentTimeMillis();
            while (DefaultFuture.hasFuture(this) && System.currentTimeMillis() - start < (long)timeout) {
                try {
                    Thread.sleep(10L);
                }
                catch (InterruptedException e) {
                    logger.warn(e.getMessage(), e);
                }
            }
        }
        this.close();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return this.channel.getLocalAddress();
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return this.channel.getRemoteAddress();
    }

    @Override
    public URL getUrl() {
        return this.channel.getUrl();
    }

    @Override
    public boolean isConnected() {
        return this.channel.isConnected();
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return this.channel.getChannelHandler();
    }

    @Override
    public ExchangeHandler getExchangeHandler() {
        return (ExchangeHandler)this.channel.getChannelHandler();
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

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + (this.channel == null ? 0 : this.channel.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        HeaderExchangeChannel other = (HeaderExchangeChannel)obj;
        return !(this.channel == null ? other.channel != null : !this.channel.equals(other.channel));
    }

    public String toString() {
        return this.channel.toString();
    }
}


/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.glassfish.grizzly.Connection
 *  org.glassfish.grizzly.Grizzly
 *  org.glassfish.grizzly.GrizzlyFuture
 *  org.glassfish.grizzly.attributes.Attribute
 *  org.glassfish.grizzly.attributes.AttributeBuilder
 *  org.glassfish.grizzly.attributes.AttributeStorage
 */
package com.alibaba.dubbo.remoting.transport.grizzly;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.transport.AbstractChannel;
import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.Grizzly;
import org.glassfish.grizzly.GrizzlyFuture;
import org.glassfish.grizzly.attributes.Attribute;
import org.glassfish.grizzly.attributes.AttributeBuilder;
import org.glassfish.grizzly.attributes.AttributeStorage;

final class GrizzlyChannel
extends AbstractChannel {
    private static final Logger logger = LoggerFactory.getLogger(GrizzlyChannel.class);
    private static final String CHANNEL_KEY = GrizzlyChannel.class.getName() + ".CHANNEL";
    private static final Attribute<GrizzlyChannel> ATTRIBUTE = Grizzly.DEFAULT_ATTRIBUTE_BUILDER.createAttribute(CHANNEL_KEY);
    private final Connection<?> connection;

    private GrizzlyChannel(Connection<?> connection, URL url, ChannelHandler handler) {
        super(url, handler);
        if (connection == null) {
            throw new IllegalArgumentException("grizzly connection == null");
        }
        this.connection = connection;
    }

    static GrizzlyChannel getOrAddChannel(Connection<?> connection, URL url, ChannelHandler handler) {
        if (connection == null) {
            return null;
        }
        GrizzlyChannel ret = (GrizzlyChannel)ATTRIBUTE.get(connection);
        if (ret == null) {
            ret = new GrizzlyChannel(connection, url, handler);
            if (connection.isOpen()) {
                ATTRIBUTE.set(connection, (Object)ret);
            }
        }
        return ret;
    }

    static void removeChannelIfDisconnectd(Connection<?> connection) {
        if (connection != null && !connection.isOpen()) {
            ATTRIBUTE.remove(connection);
        }
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return (InetSocketAddress)this.connection.getPeerAddress();
    }

    @Override
    public boolean isConnected() {
        return this.connection.isOpen();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return (InetSocketAddress)this.connection.getLocalAddress();
    }

    @Override
    public void send(Object message, boolean sent) throws RemotingException {
        super.send(message, sent);
        int timeout = 0;
        try {
            GrizzlyFuture future = this.connection.write(message);
            if (sent) {
                timeout = this.getUrl().getPositiveParameter("timeout", 1000);
                future.get((long)timeout, TimeUnit.MILLISECONDS);
            }
        }
        catch (TimeoutException e) {
            throw new RemotingException(this, "Failed to send message " + message + " to " + this.getRemoteAddress() + "in timeout(" + timeout + "ms) limit", (Throwable)e);
        }
        catch (Throwable e) {
            throw new RemotingException(this, "Failed to send message " + message + " to " + this.getRemoteAddress() + ", cause: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        try {
            super.close();
        }
        catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            GrizzlyChannel.removeChannelIfDisconnectd(this.connection);
        }
        catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            if (logger.isInfoEnabled()) {
                logger.info("Close grizzly channel " + this.connection);
            }
            this.connection.close();
        }
        catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    @Override
    public boolean hasAttribute(String key) {
        return this.getAttribute(key) == null;
    }

    @Override
    public Object getAttribute(String key) {
        return Grizzly.DEFAULT_ATTRIBUTE_BUILDER.createAttribute(key).get(this.connection);
    }

    @Override
    public void setAttribute(String key, Object value) {
        Grizzly.DEFAULT_ATTRIBUTE_BUILDER.createAttribute(key).set(this.connection, value);
    }

    @Override
    public void removeAttribute(String key) {
        Grizzly.DEFAULT_ATTRIBUTE_BUILDER.createAttribute(key).remove(this.connection);
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + (this.connection == null ? 0 : this.connection.hashCode());
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
        GrizzlyChannel other = (GrizzlyChannel)obj;
        return !(this.connection == null ? other.connection != null : !this.connection.equals(other.connection));
    }

    @Override
    public String toString() {
        return "GrizzlyChannel [connection=" + this.connection + "]";
    }
}


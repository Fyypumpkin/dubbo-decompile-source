/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.apache.mina.common.CloseFuture
 *  org.apache.mina.common.IoSession
 *  org.apache.mina.common.WriteFuture
 */
package com.alibaba.dubbo.remoting.transport.mina;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.transport.AbstractChannel;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import org.apache.mina.common.CloseFuture;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.WriteFuture;

final class MinaChannel
extends AbstractChannel {
    private static final Logger logger = LoggerFactory.getLogger(MinaChannel.class);
    private static final String CHANNEL_KEY = MinaChannel.class.getName() + ".CHANNEL";
    private final IoSession session;

    private MinaChannel(IoSession session, URL url, ChannelHandler handler) {
        super(url, handler);
        if (session == null) {
            throw new IllegalArgumentException("mina session == null");
        }
        this.session = session;
    }

    static MinaChannel getOrAddChannel(IoSession session, URL url, ChannelHandler handler) {
        if (session == null) {
            return null;
        }
        MinaChannel ret = (MinaChannel)session.getAttribute(CHANNEL_KEY);
        if (ret == null) {
            MinaChannel old;
            ret = new MinaChannel(session, url, handler);
            if (session.isConnected() && (old = (MinaChannel)session.setAttribute(CHANNEL_KEY, (Object)ret)) != null) {
                session.setAttribute(CHANNEL_KEY, (Object)old);
                ret = old;
            }
        }
        return ret;
    }

    static void removeChannelIfDisconnectd(IoSession session) {
        if (session != null && !session.isConnected()) {
            session.removeAttribute(CHANNEL_KEY);
        }
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return (InetSocketAddress)this.session.getLocalAddress();
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return (InetSocketAddress)this.session.getRemoteAddress();
    }

    @Override
    public boolean isConnected() {
        return this.session.isConnected();
    }

    @Override
    public void send(Object message, boolean sent) throws RemotingException {
        super.send(message, sent);
        boolean success = true;
        int timeout = 0;
        try {
            WriteFuture future = this.session.write(message);
            if (sent) {
                timeout = this.getUrl().getPositiveParameter("timeout", 1000);
                success = future.join((long)timeout);
            }
        }
        catch (Throwable e) {
            throw new RemotingException(this, "Failed to send message " + message + " to " + this.getRemoteAddress() + ", cause: " + e.getMessage(), e);
        }
        if (!success) {
            throw new RemotingException((Channel)this, "Failed to send message " + message + " to " + this.getRemoteAddress() + "in timeout(" + timeout + "ms) limit");
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
            MinaChannel.removeChannelIfDisconnectd(this.session);
        }
        catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            if (logger.isInfoEnabled()) {
                logger.info("CLose mina channel " + (Object)this.session);
            }
            this.session.close();
        }
        catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }

    @Override
    public boolean hasAttribute(String key) {
        return this.session.containsAttribute(key);
    }

    @Override
    public Object getAttribute(String key) {
        return this.session.getAttribute(key);
    }

    @Override
    public void setAttribute(String key, Object value) {
        this.session.setAttribute(key, value);
    }

    @Override
    public void removeAttribute(String key) {
        this.session.removeAttribute(key);
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + (this.session == null ? 0 : this.session.hashCode());
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
        MinaChannel other = (MinaChannel)obj;
        return !(this.session == null ? other.session != null : !this.session.equals((Object)other.session));
    }

    @Override
    public String toString() {
        return "MinaChannel [session=" + (Object)this.session + "]";
    }
}


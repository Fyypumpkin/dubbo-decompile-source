/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.transport;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Endpoint;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.transport.ChannelHandlerDelegate;

public abstract class AbstractPeer
implements Endpoint,
ChannelHandler {
    private final ChannelHandler handler;
    private volatile URL url;
    private volatile boolean closed;

    public AbstractPeer(URL url, ChannelHandler handler) {
        if (url == null) {
            throw new IllegalArgumentException("url == null");
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler == null");
        }
        this.url = url;
        this.handler = handler;
    }

    @Override
    public void send(Object message) throws RemotingException {
        this.send(message, this.url.getParameter("sent", false));
    }

    @Override
    public void close() {
        this.closed = true;
    }

    @Override
    public void close(int timeout) {
        this.close();
    }

    @Override
    public URL getUrl() {
        return this.url;
    }

    protected void setUrl(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("url == null");
        }
        this.url = url;
    }

    @Override
    public ChannelHandler getChannelHandler() {
        if (this.handler instanceof ChannelHandlerDelegate) {
            return ((ChannelHandlerDelegate)this.handler).getHandler();
        }
        return this.handler;
    }

    @Deprecated
    public ChannelHandler getHandler() {
        return this.getDelegateHandler();
    }

    public ChannelHandler getDelegateHandler() {
        return this.handler;
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }

    @Override
    public void connected(Channel ch) throws RemotingException {
        if (this.closed) {
            return;
        }
        this.handler.connected(ch);
    }

    @Override
    public void disconnected(Channel ch) throws RemotingException {
        this.handler.disconnected(ch);
    }

    @Override
    public void sent(Channel ch, Object msg) throws RemotingException {
        if (this.closed) {
            return;
        }
        this.handler.sent(ch, msg);
    }

    @Override
    public void received(Channel ch, Object msg) throws RemotingException {
        if (this.closed) {
            return;
        }
        this.handler.received(ch, msg);
    }

    @Override
    public void caught(Channel ch, Throwable ex) throws RemotingException {
        this.handler.caught(ch, ex);
    }
}


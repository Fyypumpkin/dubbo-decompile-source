/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.http.support;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.http.HttpHandler;
import com.alibaba.dubbo.remoting.http.HttpServer;
import java.net.InetSocketAddress;

public abstract class AbstractHttpServer
implements HttpServer {
    private final URL url;
    private final HttpHandler handler;
    private volatile boolean closed;

    public AbstractHttpServer(URL url, HttpHandler handler) {
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
    public HttpHandler getHttpHandler() {
        return this.handler;
    }

    @Override
    public URL getUrl() {
        return this.url;
    }

    @Override
    public void reset(URL url) {
    }

    @Override
    public boolean isBound() {
        return true;
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return this.url.toInetSocketAddress();
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
    public boolean isClosed() {
        return this.closed;
    }
}


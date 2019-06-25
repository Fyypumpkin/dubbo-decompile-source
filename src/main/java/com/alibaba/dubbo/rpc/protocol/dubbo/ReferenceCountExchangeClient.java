/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.protocol.dubbo;

import com.alibaba.dubbo.common.Parameters;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.exchange.ExchangeClient;
import com.alibaba.dubbo.remoting.exchange.ExchangeHandler;
import com.alibaba.dubbo.remoting.exchange.ResponseFuture;
import com.alibaba.dubbo.rpc.protocol.dubbo.LazyConnectExchangeClient;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class ReferenceCountExchangeClient
implements ExchangeClient {
    private ExchangeClient client;
    private final URL url;
    private final AtomicInteger refenceCount = new AtomicInteger(0);
    private final ConcurrentMap<String, LazyConnectExchangeClient> ghostClientMap;

    public ReferenceCountExchangeClient(ExchangeClient client, ConcurrentMap<String, LazyConnectExchangeClient> ghostClientMap) {
        this.client = client;
        this.refenceCount.incrementAndGet();
        this.url = client.getUrl();
        if (ghostClientMap == null) {
            throw new IllegalStateException("ghostClientMap can not be null, url: " + this.url);
        }
        this.ghostClientMap = ghostClientMap;
    }

    @Override
    public void reset(URL url) {
        this.client.reset(url);
    }

    @Override
    public ResponseFuture request(Object request) throws RemotingException {
        return this.client.request(request);
    }

    @Override
    public URL getUrl() {
        return this.client.getUrl();
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return this.client.getRemoteAddress();
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return this.client.getChannelHandler();
    }

    @Override
    public ResponseFuture request(Object request, int timeout) throws RemotingException {
        return this.client.request(request, timeout);
    }

    @Override
    public boolean isConnected() {
        return this.client.isConnected();
    }

    @Override
    public void reconnect() throws RemotingException {
        this.client.reconnect();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        return this.client.getLocalAddress();
    }

    @Override
    public boolean hasAttribute(String key) {
        return this.client.hasAttribute(key);
    }

    @Override
    public void reset(Parameters parameters) {
        this.client.reset(parameters);
    }

    @Override
    public void send(Object message) throws RemotingException {
        this.client.send(message);
    }

    @Override
    public ExchangeHandler getExchangeHandler() {
        return this.client.getExchangeHandler();
    }

    @Override
    public Object getAttribute(String key) {
        return this.client.getAttribute(key);
    }

    @Override
    public void send(Object message, boolean sent) throws RemotingException {
        this.client.send(message, sent);
    }

    @Override
    public void setAttribute(String key, Object value) {
        this.client.setAttribute(key, value);
    }

    @Override
    public void removeAttribute(String key) {
        this.client.removeAttribute(key);
    }

    @Override
    public void close() {
        this.close(0);
    }

    @Override
    public void close(int timeout) {
        if (this.refenceCount.decrementAndGet() <= 0) {
            if (timeout == 0) {
                this.client.close();
            } else {
                this.client.close(timeout);
            }
            this.client = this.replaceWithLazyClient();
        }
    }

    private LazyConnectExchangeClient replaceWithLazyClient() {
        URL lazyUrl = this.url.addParameter("connect.lazy.initial.state", Boolean.FALSE).addParameter("reconnect", Boolean.FALSE).addParameter("send.reconnect", Boolean.TRUE.toString()).addParameter("warning", Boolean.TRUE.toString()).addParameter("lazyclient_request_with_warning", true).addParameter("_client_memo", "referencecounthandler.replacewithlazyclient");
        String key = this.url.getAddress();
        LazyConnectExchangeClient gclient = (LazyConnectExchangeClient)this.ghostClientMap.get(key);
        if (gclient == null || gclient.isClosed()) {
            gclient = new LazyConnectExchangeClient(lazyUrl, this.client.getExchangeHandler());
            this.ghostClientMap.put(key, gclient);
        }
        return gclient;
    }

    @Override
    public boolean isClosed() {
        return this.client.isClosed();
    }

    public void incrementAndGetCount() {
        this.refenceCount.incrementAndGet();
    }
}


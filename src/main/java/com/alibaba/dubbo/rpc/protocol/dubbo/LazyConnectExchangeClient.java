/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.protocol.dubbo;

import com.alibaba.dubbo.common.Parameters;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.exchange.ExchangeClient;
import com.alibaba.dubbo.remoting.exchange.ExchangeHandler;
import com.alibaba.dubbo.remoting.exchange.Exchangers;
import com.alibaba.dubbo.remoting.exchange.ResponseFuture;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class LazyConnectExchangeClient
implements ExchangeClient {
    private static final Logger logger = LoggerFactory.getLogger(LazyConnectExchangeClient.class);
    private final URL url;
    private final ExchangeHandler requestHandler;
    private volatile ExchangeClient client;
    private final Lock connectLock = new ReentrantLock();
    private final boolean initialState;
    protected final boolean requestWithWarning;
    static final String REQUEST_WITH_WARNING_KEY = "lazyclient_request_with_warning";
    private AtomicLong warningcount = new AtomicLong(0L);

    public LazyConnectExchangeClient(URL url, ExchangeHandler requestHandler) {
        this.url = url.addParameter("send.reconnect", Boolean.TRUE.toString());
        this.requestHandler = requestHandler;
        this.initialState = url.getParameter("connect.lazy.initial.state", true);
        this.requestWithWarning = url.getParameter(REQUEST_WITH_WARNING_KEY, false);
    }

    private void initClient() throws RemotingException {
        if (this.client != null) {
            return;
        }
        if (logger.isInfoEnabled()) {
            logger.info("Lazy connect to " + this.url);
        }
        this.connectLock.lock();
        try {
            if (this.client != null) {
                return;
            }
            this.client = Exchangers.connect(this.url, this.requestHandler);
        }
        finally {
            this.connectLock.unlock();
        }
    }

    @Override
    public ResponseFuture request(Object request) throws RemotingException {
        this.warning(request);
        this.initClient();
        return this.client.request(request);
    }

    @Override
    public URL getUrl() {
        return this.url;
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        if (this.client == null) {
            return InetSocketAddress.createUnresolved(this.url.getHost(), this.url.getPort());
        }
        return this.client.getRemoteAddress();
    }

    @Override
    public ResponseFuture request(Object request, int timeout) throws RemotingException {
        this.warning(request);
        this.initClient();
        return this.client.request(request, timeout);
    }

    private void warning(Object request) {
        if (this.requestWithWarning) {
            if (this.warningcount.get() % 5000L == 0L) {
                logger.warn(new IllegalStateException("safe guard client , should not be called ,must have a bug."));
            }
            this.warningcount.incrementAndGet();
        }
    }

    @Override
    public ChannelHandler getChannelHandler() {
        this.checkClient();
        return this.client.getChannelHandler();
    }

    @Override
    public boolean isConnected() {
        if (this.client == null) {
            return this.initialState;
        }
        return this.client.isConnected();
    }

    @Override
    public InetSocketAddress getLocalAddress() {
        if (this.client == null) {
            return InetSocketAddress.createUnresolved(NetUtils.getLocalHost(), 0);
        }
        return this.client.getLocalAddress();
    }

    @Override
    public ExchangeHandler getExchangeHandler() {
        return this.requestHandler;
    }

    @Override
    public void send(Object message) throws RemotingException {
        this.initClient();
        this.client.send(message);
    }

    @Override
    public void send(Object message, boolean sent) throws RemotingException {
        this.initClient();
        this.client.send(message, sent);
    }

    @Override
    public boolean isClosed() {
        if (this.client != null) {
            return this.client.isClosed();
        }
        return true;
    }

    @Override
    public void close() {
        if (this.client != null) {
            this.client.close();
        }
    }

    @Override
    public void close(int timeout) {
        if (this.client != null) {
            this.client.close(timeout);
        }
    }

    @Override
    public void reset(URL url) {
        this.checkClient();
        this.client.reset(url);
    }

    @Deprecated
    @Override
    public void reset(Parameters parameters) {
        this.reset(this.getUrl().addParameters(parameters.getParameters()));
    }

    @Override
    public void reconnect() throws RemotingException {
        this.checkClient();
        this.client.reconnect();
    }

    @Override
    public Object getAttribute(String key) {
        if (this.client == null) {
            return null;
        }
        return this.client.getAttribute(key);
    }

    @Override
    public void setAttribute(String key, Object value) {
        this.checkClient();
        this.client.setAttribute(key, value);
    }

    @Override
    public void removeAttribute(String key) {
        this.checkClient();
        this.client.removeAttribute(key);
    }

    @Override
    public boolean hasAttribute(String key) {
        if (this.client == null) {
            return false;
        }
        return this.client.hasAttribute(key);
    }

    private void checkClient() {
        if (this.client == null) {
            throw new IllegalStateException("LazyConnectExchangeClient state error. the client has not be init .url:" + this.url);
        }
    }
}


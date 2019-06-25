/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.exchange.support;

import com.alibaba.dubbo.common.Parameters;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.exchange.ExchangeChannel;
import com.alibaba.dubbo.remoting.exchange.ExchangeServer;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Map;

public class ExchangeServerDelegate
implements ExchangeServer {
    private transient ExchangeServer server;

    public ExchangeServerDelegate() {
    }

    public ExchangeServerDelegate(ExchangeServer server) {
        this.setServer(server);
    }

    public ExchangeServer getServer() {
        return this.server;
    }

    public void setServer(ExchangeServer server) {
        this.server = server;
    }

    @Override
    public boolean isBound() {
        return this.server.isBound();
    }

    @Override
    public void reset(URL url) {
        this.server.reset(url);
    }

    @Deprecated
    @Override
    public void reset(Parameters parameters) {
        this.reset(this.getUrl().addParameters(parameters.getParameters()));
    }

    @Override
    public Collection<Channel> getChannels() {
        return this.server.getChannels();
    }

    @Override
    public Channel getChannel(InetSocketAddress remoteAddress) {
        return this.server.getChannel(remoteAddress);
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
    public InetSocketAddress getLocalAddress() {
        return this.server.getLocalAddress();
    }

    @Override
    public void send(Object message) throws RemotingException {
        this.server.send(message);
    }

    @Override
    public void send(Object message, boolean sent) throws RemotingException {
        this.server.send(message, sent);
    }

    @Override
    public void close() {
        this.server.close();
    }

    @Override
    public boolean isClosed() {
        return this.server.isClosed();
    }

    @Override
    public Collection<ExchangeChannel> getExchangeChannels() {
        return this.server.getExchangeChannels();
    }

    @Override
    public ExchangeChannel getExchangeChannel(InetSocketAddress remoteAddress) {
        return this.server.getExchangeChannel(remoteAddress);
    }

    @Override
    public void close(int timeout) {
        this.server.close(timeout);
    }
}


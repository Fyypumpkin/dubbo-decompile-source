/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.transport;

import com.alibaba.dubbo.common.Parameters;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Client;
import com.alibaba.dubbo.remoting.RemotingException;
import java.net.InetSocketAddress;
import java.util.Map;

public class ClientDelegate
implements Client {
    private transient Client client;

    public ClientDelegate() {
    }

    public ClientDelegate(Client client) {
        this.setClient(client);
    }

    public Client getClient() {
        return this.client;
    }

    public void setClient(Client client) {
        if (client == null) {
            throw new IllegalArgumentException("client == null");
        }
        this.client = client;
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
    public URL getUrl() {
        return this.client.getUrl();
    }

    @Override
    public InetSocketAddress getRemoteAddress() {
        return this.client.getRemoteAddress();
    }

    @Override
    public void reconnect() throws RemotingException {
        this.client.reconnect();
    }

    @Override
    public ChannelHandler getChannelHandler() {
        return this.client.getChannelHandler();
    }

    @Override
    public boolean isConnected() {
        return this.client.isConnected();
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
    public void send(Object message) throws RemotingException {
        this.client.send(message);
    }

    @Override
    public Object getAttribute(String key) {
        return this.client.getAttribute(key);
    }

    @Override
    public void setAttribute(String key, Object value) {
        this.client.setAttribute(key, value);
    }

    @Override
    public void send(Object message, boolean sent) throws RemotingException {
        this.client.send(message, sent);
    }

    @Override
    public void removeAttribute(String key) {
        this.client.removeAttribute(key);
    }

    @Override
    public void close() {
        this.client.close();
    }

    @Override
    public void close(int timeout) {
        this.client.close(timeout);
    }

    @Override
    public boolean isClosed() {
        return this.client.isClosed();
    }
}


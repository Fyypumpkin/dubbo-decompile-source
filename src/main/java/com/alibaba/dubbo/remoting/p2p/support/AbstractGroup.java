/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.p2p.support;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Client;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.Server;
import com.alibaba.dubbo.remoting.Transporters;
import com.alibaba.dubbo.remoting.p2p.Group;
import com.alibaba.dubbo.remoting.p2p.Peer;
import com.alibaba.dubbo.remoting.p2p.support.ServerPeer;
import com.alibaba.dubbo.remoting.transport.ChannelHandlerDispatcher;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractGroup
implements Group {
    protected static final Logger logger = LoggerFactory.getLogger(AbstractGroup.class);
    protected final URL url;
    protected final Map<URL, Server> servers = new ConcurrentHashMap<URL, Server>();
    protected final Map<URL, Client> clients = new ConcurrentHashMap<URL, Client>();
    protected final ChannelHandlerDispatcher dispatcher = new ChannelHandlerDispatcher();

    public AbstractGroup(URL url) {
        if (url == null) {
            throw new IllegalArgumentException("url == null");
        }
        this.url = url;
    }

    @Override
    public URL getUrl() {
        return this.url;
    }

    @Override
    public void close() {
        for (URL url : new ArrayList<URL>(this.servers.keySet())) {
            try {
                this.leave(url);
            }
            catch (Throwable t) {
                logger.error(t.getMessage(), t);
            }
        }
        for (URL url : new ArrayList<URL>(this.clients.keySet())) {
            try {
                this.disconnect(url);
            }
            catch (Throwable t) {
                logger.error(t.getMessage(), t);
            }
        }
    }

    @Override
    public Peer join(URL url, ChannelHandler handler) throws RemotingException {
        Server server = this.servers.get(url);
        if (server == null) {
            server = Transporters.bind(url, handler);
            this.servers.put(url, server);
            this.dispatcher.addChannelHandler(handler);
        }
        return new ServerPeer(server, this.clients, this);
    }

    @Override
    public void leave(URL url) throws RemotingException {
        Server server = this.servers.remove(url);
        if (server != null) {
            server.close();
        }
    }

    protected Client connect(URL url) throws RemotingException {
        if (this.servers.containsKey(url)) {
            return null;
        }
        Client client = this.clients.get(url);
        if (client == null) {
            client = Transporters.connect(url, this.dispatcher);
            this.clients.put(url, client);
        }
        return client;
    }

    protected void disconnect(URL url) throws RemotingException {
        Client client = this.clients.remove(url);
        if (client != null) {
            client.close();
        }
    }
}


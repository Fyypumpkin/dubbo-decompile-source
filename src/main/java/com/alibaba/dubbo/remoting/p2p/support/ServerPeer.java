/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.p2p.support;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.Client;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.Server;
import com.alibaba.dubbo.remoting.p2p.Group;
import com.alibaba.dubbo.remoting.p2p.Peer;
import com.alibaba.dubbo.remoting.transport.ServerDelegate;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class ServerPeer
extends ServerDelegate
implements Peer {
    private static final Logger logger = LoggerFactory.getLogger(ServerPeer.class);
    private final Map<URL, Client> clients;
    private final Group group;

    public ServerPeer(Server server, Map<URL, Client> clients, Group group) {
        super(server);
        this.clients = clients;
        this.group = group;
    }

    @Override
    public void leave() throws RemotingException {
        this.group.leave(this.getUrl());
    }

    @Override
    public void close() {
        try {
            this.leave();
        }
        catch (RemotingException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public Collection<Channel> getChannels() {
        Collection<Channel> channels = super.getChannels();
        if (this.clients.size() > 0) {
            channels = channels == null ? new ArrayList<Channel>() : new ArrayList<Channel>(channels);
            channels.addAll(this.clients.values());
        }
        return channels;
    }

    @Override
    public Channel getChannel(InetSocketAddress remoteAddress) {
        String host = remoteAddress.getAddress() != null ? remoteAddress.getAddress().getHostAddress() : remoteAddress.getHostName();
        int port = remoteAddress.getPort();
        Channel channel = super.getChannel(remoteAddress);
        if (channel == null) {
            for (Map.Entry<URL, Client> entry : this.clients.entrySet()) {
                URL url = entry.getKey();
                if (!url.getIp().equals(host) || url.getPort() != port) continue;
                return entry.getValue();
            }
        }
        return channel;
    }

    @Override
    public void send(Object message) throws RemotingException {
        this.send(message, this.getUrl().getParameter("sent", false));
    }

    @Override
    public void send(Object message, boolean sent) throws RemotingException {
        Throwable last = null;
        try {
            super.send(message, sent);
        }
        catch (Throwable t) {
            last = t;
        }
        for (Client client : this.clients.values()) {
            try {
                client.send(message, sent);
            }
            catch (Throwable t) {
                last = t;
            }
        }
        if (last != null) {
            if (last instanceof RemotingException) {
                throw (RemotingException)last;
            }
            if (last instanceof RuntimeException) {
                throw (RuntimeException)last;
            }
            throw new RuntimeException(last.getMessage(), last);
        }
    }
}


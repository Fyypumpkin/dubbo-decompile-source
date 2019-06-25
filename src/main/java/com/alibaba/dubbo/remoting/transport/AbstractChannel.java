/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.transport;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.transport.AbstractPeer;
import java.net.InetSocketAddress;

public abstract class AbstractChannel
extends AbstractPeer
implements Channel {
    public AbstractChannel(URL url, ChannelHandler handler) {
        super(url, handler);
    }

    @Override
    public void send(Object message, boolean sent) throws RemotingException {
        if (this.isClosed()) {
            throw new RemotingException((Channel)this, "Failed to send message " + (message == null ? "" : message.getClass().getName()) + ":" + message + ", cause: Channel closed. channel: " + this.getLocalAddress() + " -> " + this.getRemoteAddress());
        }
    }

    public String toString() {
        return this.getLocalAddress() + " -> " + this.getRemoteAddress();
    }
}


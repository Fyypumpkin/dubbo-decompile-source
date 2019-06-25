/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.apache.mina.common.IoHandlerAdapter
 *  org.apache.mina.common.IoSession
 */
package com.alibaba.dubbo.remoting.transport.mina;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.transport.mina.MinaChannel;
import org.apache.mina.common.IoHandlerAdapter;
import org.apache.mina.common.IoSession;

public class MinaHandler
extends IoHandlerAdapter {
    private final URL url;
    private final ChannelHandler handler;

    public MinaHandler(URL url, ChannelHandler handler) {
        if (url == null) {
            throw new IllegalArgumentException("url == null");
        }
        if (handler == null) {
            throw new IllegalArgumentException("handler == null");
        }
        this.url = url;
        this.handler = handler;
    }

    public void sessionOpened(IoSession session) throws Exception {
        MinaChannel channel = MinaChannel.getOrAddChannel(session, this.url, this.handler);
        try {
            this.handler.connected(channel);
        }
        finally {
            MinaChannel.removeChannelIfDisconnectd(session);
        }
    }

    public void sessionClosed(IoSession session) throws Exception {
        MinaChannel channel = MinaChannel.getOrAddChannel(session, this.url, this.handler);
        try {
            this.handler.disconnected(channel);
        }
        finally {
            MinaChannel.removeChannelIfDisconnectd(session);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void messageReceived(IoSession session, Object message) throws Exception {
        MinaChannel channel = MinaChannel.getOrAddChannel(session, this.url, this.handler);
        try {
            this.handler.received(channel, message);
        }
        finally {
            MinaChannel.removeChannelIfDisconnectd(session);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void messageSent(IoSession session, Object message) throws Exception {
        MinaChannel channel = MinaChannel.getOrAddChannel(session, this.url, this.handler);
        try {
            this.handler.sent(channel, message);
        }
        finally {
            MinaChannel.removeChannelIfDisconnectd(session);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        MinaChannel channel = MinaChannel.getOrAddChannel(session, this.url, this.handler);
        try {
            this.handler.caught(channel, cause);
        }
        finally {
            MinaChannel.removeChannelIfDisconnectd(session);
        }
    }
}


/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.exchange.support;

import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.exchange.ExchangeChannel;
import com.alibaba.dubbo.remoting.exchange.ExchangeHandler;
import com.alibaba.dubbo.remoting.exchange.support.Replier;
import com.alibaba.dubbo.remoting.exchange.support.ReplierDispatcher;
import com.alibaba.dubbo.remoting.telnet.TelnetHandler;
import com.alibaba.dubbo.remoting.telnet.support.TelnetHandlerAdapter;
import com.alibaba.dubbo.remoting.transport.ChannelHandlerDispatcher;

public class ExchangeHandlerDispatcher
implements ExchangeHandler {
    private final ReplierDispatcher replierDispatcher;
    private final ChannelHandlerDispatcher handlerDispatcher;
    private final TelnetHandler telnetHandler;

    public ExchangeHandlerDispatcher() {
        this.replierDispatcher = new ReplierDispatcher();
        this.handlerDispatcher = new ChannelHandlerDispatcher();
        this.telnetHandler = new TelnetHandlerAdapter();
    }

    public ExchangeHandlerDispatcher(Replier<?> replier) {
        this.replierDispatcher = new ReplierDispatcher(replier);
        this.handlerDispatcher = new ChannelHandlerDispatcher();
        this.telnetHandler = new TelnetHandlerAdapter();
    }

    public /* varargs */ ExchangeHandlerDispatcher(ChannelHandler ... handlers) {
        this.replierDispatcher = new ReplierDispatcher();
        this.handlerDispatcher = new ChannelHandlerDispatcher(handlers);
        this.telnetHandler = new TelnetHandlerAdapter();
    }

    public /* varargs */ ExchangeHandlerDispatcher(Replier<?> replier, ChannelHandler ... handlers) {
        this.replierDispatcher = new ReplierDispatcher(replier);
        this.handlerDispatcher = new ChannelHandlerDispatcher(handlers);
        this.telnetHandler = new TelnetHandlerAdapter();
    }

    public ExchangeHandlerDispatcher addChannelHandler(ChannelHandler handler) {
        this.handlerDispatcher.addChannelHandler(handler);
        return this;
    }

    public ExchangeHandlerDispatcher removeChannelHandler(ChannelHandler handler) {
        this.handlerDispatcher.removeChannelHandler(handler);
        return this;
    }

    public <T> ExchangeHandlerDispatcher addReplier(Class<T> type, Replier<T> replier) {
        this.replierDispatcher.addReplier(type, replier);
        return this;
    }

    public <T> ExchangeHandlerDispatcher removeReplier(Class<T> type) {
        this.replierDispatcher.removeReplier(type);
        return this;
    }

    @Override
    public Object reply(ExchangeChannel channel, Object request) throws RemotingException {
        return this.replierDispatcher.reply(channel, request);
    }

    @Override
    public void connected(Channel channel) {
        this.handlerDispatcher.connected(channel);
    }

    @Override
    public void disconnected(Channel channel) {
        this.handlerDispatcher.disconnected(channel);
    }

    @Override
    public void sent(Channel channel, Object message) {
        this.handlerDispatcher.sent(channel, message);
    }

    @Override
    public void received(Channel channel, Object message) {
        this.handlerDispatcher.received(channel, message);
    }

    @Override
    public void caught(Channel channel, Throwable exception) {
        this.handlerDispatcher.caught(channel, exception);
    }

    @Override
    public String telnet(Channel channel, String message) throws RemotingException {
        return this.telnetHandler.telnet(channel, message);
    }
}


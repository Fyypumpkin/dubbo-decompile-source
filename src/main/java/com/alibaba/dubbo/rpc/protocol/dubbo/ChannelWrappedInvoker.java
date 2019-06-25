/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.protocol.dubbo;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Client;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.TimeoutException;
import com.alibaba.dubbo.remoting.exchange.ResponseFuture;
import com.alibaba.dubbo.remoting.exchange.support.header.HeaderExchangeClient;
import com.alibaba.dubbo.remoting.transport.ClientDelegate;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.RpcInvocation;
import com.alibaba.dubbo.rpc.RpcResult;
import com.alibaba.dubbo.rpc.protocol.AbstractInvoker;
import java.net.InetSocketAddress;

class ChannelWrappedInvoker<T>
extends AbstractInvoker<T> {
    private final Channel channel;
    private final String serviceKey;

    public ChannelWrappedInvoker(Class<T> serviceType, Channel channel, URL url, String serviceKey) {
        super(serviceType, url, new String[]{"group", "token", "timeout"});
        this.channel = channel;
        this.serviceKey = serviceKey;
    }

    @Override
    protected Result doInvoke(Invocation invocation) throws Throwable {
        RpcInvocation inv = (RpcInvocation)invocation;
        inv.setAttachment("path", this.getInterface().getName());
        inv.setAttachment("callback.service.instid", this.serviceKey);
        HeaderExchangeClient currentClient = new HeaderExchangeClient(new ChannelWrapper(this.channel));
        try {
            if (this.getUrl().getMethodParameter(invocation.getMethodName(), "async", false)) {
                currentClient.send(inv, this.getUrl().getMethodParameter(invocation.getMethodName(), "sent", false));
                return new RpcResult();
            }
            int timeout = this.getUrl().getMethodParameter(invocation.getMethodName(), "timeout", 1000);
            if (timeout > 0) {
                return (Result)currentClient.request(inv, timeout).get();
            }
            return (Result)currentClient.request(inv).get();
        }
        catch (RpcException e) {
            throw e;
        }
        catch (TimeoutException e) {
            throw new RpcException(2, e.getMessage(), e);
        }
        catch (RemotingException e) {
            throw new RpcException(1, e.getMessage(), e);
        }
        catch (Throwable e) {
            throw new RpcException(e.getMessage(), e);
        }
    }

    @Override
    public void destroy() {
    }

    public static class ChannelWrapper
    extends ClientDelegate {
        private final Channel channel;
        private final URL url;

        public ChannelWrapper(Channel channel) {
            this.channel = channel;
            this.url = channel.getUrl().addParameter("codec", "dubbo");
        }

        @Override
        public URL getUrl() {
            return this.url;
        }

        @Override
        public ChannelHandler getChannelHandler() {
            return this.channel.getChannelHandler();
        }

        @Override
        public InetSocketAddress getLocalAddress() {
            return this.channel.getLocalAddress();
        }

        @Override
        public void close() {
            this.channel.close();
        }

        @Override
        public boolean isClosed() {
            return this.channel == null ? true : this.channel.isClosed();
        }

        @Override
        public void reset(URL url) {
            throw new RpcException("ChannelInvoker can not reset.");
        }

        @Override
        public InetSocketAddress getRemoteAddress() {
            return this.channel.getLocalAddress();
        }

        @Override
        public boolean isConnected() {
            return this.channel == null ? false : this.channel.isConnected();
        }

        @Override
        public boolean hasAttribute(String key) {
            return this.channel.hasAttribute(key);
        }

        @Override
        public Object getAttribute(String key) {
            return this.channel.getAttribute(key);
        }

        @Override
        public void setAttribute(String key, Object value) {
            this.channel.setAttribute(key, value);
        }

        @Override
        public void removeAttribute(String key) {
            this.channel.removeAttribute(key);
        }

        @Override
        public void reconnect() throws RemotingException {
        }

        @Override
        public void send(Object message) throws RemotingException {
            this.channel.send(message);
        }

        @Override
        public void send(Object message, boolean sent) throws RemotingException {
            this.channel.send(message, sent);
        }
    }

}


/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.transport.dispatcher.all;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.ExecutionException;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.exchange.Request;
import com.alibaba.dubbo.remoting.exchange.Response;
import com.alibaba.dubbo.remoting.transport.dispatcher.ChannelEventRunnable;
import com.alibaba.dubbo.remoting.transport.dispatcher.WrappedChannelHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

public class AllChannelHandler
extends WrappedChannelHandler {
    public AllChannelHandler(ChannelHandler handler, URL url) {
        super(handler, url);
    }

    @Override
    public void connected(Channel channel) throws RemotingException {
        ExecutorService cexecutor = this.getExecutorService();
        try {
            cexecutor.execute(new ChannelEventRunnable(channel, this.handler, ChannelEventRunnable.ChannelState.CONNECTED));
        }
        catch (Throwable t) {
            throw new ExecutionException((Object)"connect event", channel, this.getClass() + " error when process connected event .", t);
        }
    }

    @Override
    public void disconnected(Channel channel) throws RemotingException {
        ExecutorService cexecutor = this.getExecutorService();
        try {
            cexecutor.execute(new ChannelEventRunnable(channel, this.handler, ChannelEventRunnable.ChannelState.DISCONNECTED));
        }
        catch (Throwable t) {
            throw new ExecutionException((Object)"disconnect event", channel, this.getClass() + " error when process disconnected event .", t);
        }
    }

    @Override
    public void received(Channel channel, Object message) throws RemotingException {
        ExecutorService cexecutor = this.getExecutorService();
        try {
            cexecutor.execute(new ChannelEventRunnable(channel, this.handler, ChannelEventRunnable.ChannelState.RECEIVED, message));
        }
        catch (Throwable t) {
            Request request;
            if (message instanceof Request && t instanceof RejectedExecutionException && (request = (Request)message).isTwoWay()) {
                String msg = "Server side(" + this.url.getIp() + "," + this.url.getPort() + ") thread pool is exhausted ,detail msg:" + t.getMessage();
                Response response = new Response(request.getId(), request.getVersion());
                response.setStatus((byte)100);
                response.setErrorMessage(msg);
                channel.send(response);
                return;
            }
            throw new ExecutionException(message, channel, this.getClass() + " error when process received event .", t);
        }
    }

    @Override
    public void caught(Channel channel, Throwable exception) throws RemotingException {
        ExecutorService cexecutor = this.getExecutorService();
        try {
            cexecutor.execute(new ChannelEventRunnable(channel, this.handler, ChannelEventRunnable.ChannelState.CAUGHT, exception));
        }
        catch (Throwable t) {
            throw new ExecutionException((Object)"caught event", channel, this.getClass() + " error when process caught event .", t);
        }
    }

    private ExecutorService getExecutorService() {
        ExecutorService cexecutor = this.executor;
        if (cexecutor == null || cexecutor.isShutdown()) {
            cexecutor = SHARED_EXECUTOR;
        }
        return cexecutor;
    }
}


/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.transport.dispatcher.message;

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

public class MessageOnlyChannelHandler
extends WrappedChannelHandler {
    public MessageOnlyChannelHandler(ChannelHandler handler, URL url) {
        super(handler, url);
    }

    @Override
    public void received(Channel channel, Object message) throws RemotingException {
        ExecutorService cexecutor = this.executor;
        if (cexecutor == null || cexecutor.isShutdown()) {
            cexecutor = SHARED_EXECUTOR;
        }
        try {
            cexecutor.execute(new ChannelEventRunnable(channel, this.handler, ChannelEventRunnable.ChannelState.RECEIVED, message));
        }
        catch (Throwable t) {
            Request request;
            if (message instanceof Request && t instanceof RejectedExecutionException && (request = (Request)message).isTwoWay()) {
                String msg = "Server side(" + this.url.getIp() + "," + this.url.getPort() + ") threadpool is exhausted ,detail msg:" + t.getMessage();
                Response response = new Response(request.getId(), request.getVersion());
                response.setStatus((byte)100);
                response.setErrorMessage(msg);
                channel.send(response);
                return;
            }
            throw new ExecutionException(message, channel, this.getClass() + " error when process received event .", t);
        }
    }
}


/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.transport.dispatcher.connection;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.threadpool.support.AbortPolicyWithReport;
import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.ExecutionException;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.exchange.Request;
import com.alibaba.dubbo.remoting.exchange.Response;
import com.alibaba.dubbo.remoting.transport.dispatcher.ChannelEventRunnable;
import com.alibaba.dubbo.remoting.transport.dispatcher.WrappedChannelHandler;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ConnectionOrderedChannelHandler
extends WrappedChannelHandler {
    protected final ThreadPoolExecutor connectionExecutor;
    private final int queuewarninglimit;

    public ConnectionOrderedChannelHandler(ChannelHandler handler, URL url) {
        super(handler, url);
        String threadName = url.getParameter("threadname", "Dubbo");
        this.connectionExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(url.getPositiveParameter("connect.queue.capacity", Integer.MAX_VALUE)), new NamedThreadFactory(threadName, true), new AbortPolicyWithReport(threadName, url));
        this.queuewarninglimit = url.getParameter("connect.queue.warning.size", 1000);
    }

    @Override
    public void connected(Channel channel) throws RemotingException {
        try {
            this.checkQueueLength();
            this.connectionExecutor.execute(new ChannelEventRunnable(channel, this.handler, ChannelEventRunnable.ChannelState.CONNECTED));
        }
        catch (Throwable t) {
            throw new ExecutionException((Object)"connect event", channel, this.getClass() + " error when process connected event .", t);
        }
    }

    @Override
    public void disconnected(Channel channel) throws RemotingException {
        try {
            this.checkQueueLength();
            this.connectionExecutor.execute(new ChannelEventRunnable(channel, this.handler, ChannelEventRunnable.ChannelState.DISCONNECTED));
        }
        catch (Throwable t) {
            throw new ExecutionException((Object)"disconnected event", channel, this.getClass() + " error when process disconnected event .", t);
        }
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

    @Override
    public void caught(Channel channel, Throwable exception) throws RemotingException {
        ExecutorService cexecutor = this.executor;
        if (cexecutor == null || cexecutor.isShutdown()) {
            cexecutor = SHARED_EXECUTOR;
        }
        try {
            cexecutor.execute(new ChannelEventRunnable(channel, this.handler, ChannelEventRunnable.ChannelState.CAUGHT, exception));
        }
        catch (Throwable t) {
            throw new ExecutionException((Object)"caught event", channel, this.getClass() + " error when process caught event .", t);
        }
    }

    private void checkQueueLength() {
        if (this.connectionExecutor.getQueue().size() > this.queuewarninglimit) {
            logger.warn(new IllegalThreadStateException("connectionordered channel handler `queue size: " + this.connectionExecutor.getQueue().size() + " exceed the warning limit number :" + this.queuewarninglimit));
        }
    }
}


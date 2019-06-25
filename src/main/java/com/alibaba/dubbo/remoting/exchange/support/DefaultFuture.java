/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.exchange.support;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.TimeoutException;
import com.alibaba.dubbo.remoting.exchange.Request;
import com.alibaba.dubbo.remoting.exchange.Response;
import com.alibaba.dubbo.remoting.exchange.ResponseCallback;
import com.alibaba.dubbo.remoting.exchange.ResponseFuture;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DefaultFuture
implements ResponseFuture {
    private static final Logger logger = LoggerFactory.getLogger(DefaultFuture.class);
    private static final Map<Long, Channel> CHANNELS = new ConcurrentHashMap<Long, Channel>();
    private static final Map<Long, DefaultFuture> FUTURES = new ConcurrentHashMap<Long, DefaultFuture>();
    private final long id;
    private final Channel channel;
    private final Request request;
    private final int timeout;
    private final Lock lock = new ReentrantLock();
    private final Condition done = this.lock.newCondition();
    private final long start = System.currentTimeMillis();
    private volatile long sent;
    private volatile Response response;
    private volatile ResponseCallback callback;

    public DefaultFuture(Channel channel, Request request, int timeout) {
        this.channel = channel;
        this.request = request;
        this.id = request.getId();
        this.timeout = timeout > 0 ? timeout : channel.getUrl().getPositiveParameter("timeout", 1000);
        FUTURES.put(this.id, this);
        CHANNELS.put(this.id, channel);
    }

    @Override
    public Object get() throws RemotingException {
        return this.get(this.timeout);
    }

    @Override
    public Object get(int timeout) throws RemotingException {
        if (timeout <= 0) {
            timeout = 1000;
        }
        if (!this.isDone()) {
            long start = System.currentTimeMillis();
            this.lock.lock();
            try {
                while (!this.isDone()) {
                    this.done.await(timeout, TimeUnit.MILLISECONDS);
                    if (!this.isDone() && System.currentTimeMillis() - start <= (long)timeout) continue;
                    break;
                }
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            finally {
                this.lock.unlock();
            }
            if (!this.isDone()) {
                throw new TimeoutException(this.sent > 0L, this.channel, this.getTimeoutMessage(false));
            }
        }
        return this.returnFromResponse();
    }

    public void cancel() {
        Response errorResult = new Response(this.id);
        errorResult.setErrorMessage("request future has been canceled.");
        this.response = errorResult;
        FUTURES.remove(this.id);
        CHANNELS.remove(this.id);
    }

    @Override
    public boolean isDone() {
        return this.response != null;
    }

    @Override
    public void setCallback(ResponseCallback callback) {
        if (this.isDone()) {
            this.invokeCallback(callback);
        } else {
            boolean isdone;
            isdone = false;
            this.lock.lock();
            try {
                if (!this.isDone()) {
                    this.callback = callback;
                } else {
                    isdone = true;
                }
            }
            finally {
                this.lock.unlock();
            }
            if (isdone) {
                this.invokeCallback(callback);
            }
        }
    }

    private void invokeCallback(ResponseCallback c) {
        ResponseCallback callbackCopy = c;
        if (callbackCopy == null) {
            throw new NullPointerException("callback cannot be null.");
        }
        c = null;
        Response res = this.response;
        if (res == null) {
            throw new IllegalStateException("response cannot be null. url:" + this.channel.getUrl());
        }
        if (res.getStatus() == 20) {
            try {
                callbackCopy.done(res.getResult());
            }
            catch (Exception e) {
                logger.error("callback invoke error .reasult:" + res.getResult() + ",url:" + this.channel.getUrl(), e);
            }
        } else if (res.getStatus() == 30 || res.getStatus() == 31) {
            try {
                TimeoutException te = new TimeoutException(res.getStatus() == 31, this.channel, res.getErrorMessage());
                callbackCopy.caught(te);
            }
            catch (Exception e) {
                logger.error("callback invoke error ,url:" + this.channel.getUrl(), e);
            }
        } else if (res.getStatus() == 100) {
            try {
                RejectedExecutionException re = new RejectedExecutionException(res.getErrorMessage());
                callbackCopy.caught(re);
            }
            catch (Exception e) {
                logger.error("callback invoke error ,url:" + this.channel.getUrl(), e);
            }
        } else {
            try {
                RuntimeException re = new RuntimeException(res.getErrorMessage());
                callbackCopy.caught(re);
            }
            catch (Exception e) {
                logger.error("callback invoke error ,url:" + this.channel.getUrl(), e);
            }
        }
    }

    private Object returnFromResponse() throws RemotingException {
        Response res = this.response;
        if (res == null) {
            throw new IllegalStateException("response cannot be null");
        }
        if (res.getStatus() == 20) {
            return res.getResult();
        }
        if (res.getStatus() == 30 || res.getStatus() == 31) {
            throw new TimeoutException(res.getStatus() == 31, this.channel, res.getErrorMessage());
        }
        if (res.getStatus() == 100) {
            throw new RejectedExecutionException(res.getErrorMessage());
        }
        throw new RemotingException(this.channel, res.getErrorMessage());
    }

    private long getId() {
        return this.id;
    }

    private Channel getChannel() {
        return this.channel;
    }

    private boolean isSent() {
        return this.sent > 0L;
    }

    public Request getRequest() {
        return this.request;
    }

    private int getTimeout() {
        return this.timeout;
    }

    private long getStartTimestamp() {
        return this.start;
    }

    public static DefaultFuture getFuture(long id) {
        return FUTURES.get(id);
    }

    public static boolean hasFuture(Channel channel) {
        return CHANNELS.containsValue(channel);
    }

    public static void sent(Channel channel, Request request) {
        DefaultFuture future = FUTURES.get(request.getId());
        if (future != null) {
            future.doSent();
        }
    }

    private void doSent() {
        this.sent = System.currentTimeMillis();
    }

    public static void received(Channel channel, Response response) {
        try {
            DefaultFuture future = FUTURES.remove(response.getId());
            if (future != null) {
                future.doReceived(response);
            } else if (logger.isDebugEnabled()) {
                logger.debug("The timeout response finally returned at " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()) + ", response " + response + (channel == null ? "" : new StringBuilder().append(", channel: ").append(channel.getLocalAddress()).append(" -> ").append(channel.getRemoteAddress()).toString()));
            }
        }
        finally {
            CHANNELS.remove(response.getId());
        }
    }

    private void doReceived(Response res) {
        this.lock.lock();
        try {
            this.response = res;
            if (this.done != null) {
                this.done.signal();
            }
        }
        finally {
            this.lock.unlock();
        }
        if (this.callback != null) {
            this.invokeCallback(this.callback);
        }
    }

    private String getTimeoutMessage(boolean scan) {
        long nowTimestamp = System.currentTimeMillis();
        return (this.sent > 0L ? "Waiting server-side response timeout" : "Sending request timeout in client-side") + (scan ? " by scan timer" : "") + ". start time: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(this.start)) + ", end time: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date()) + "," + (this.sent > 0L ? new StringBuilder().append(" client elapsed: ").append(this.sent - this.start).append(" ms, server elapsed: ").append(nowTimestamp - this.sent).toString() : new StringBuilder().append(" elapsed: ").append(nowTimestamp - this.start).toString()) + " ms, timeout: " + this.timeout + " ms, request: " + this.request + ", channel: " + this.channel.getLocalAddress() + " -> " + this.channel.getRemoteAddress();
    }

    static {
        Thread th = new Thread((Runnable)new RemotingInvocationTimeoutScan(), "DubboResponseTimeoutScanTimer");
        th.setDaemon(true);
        th.start();
    }

    private static class RemotingInvocationTimeoutScan
    implements Runnable {
        private RemotingInvocationTimeoutScan() {
        }

        @Override
        public void run() {
            do {
                try {
                    do {
                        for (DefaultFuture future : FUTURES.values()) {
                            if (future == null || future.isDone() || System.currentTimeMillis() - future.getStartTimestamp() <= (long)future.getTimeout()) continue;
                            Response timeoutResponse = new Response(future.getId());
                            timeoutResponse.setStatus(future.isSent() ? (byte)31 : 30);
                            timeoutResponse.setErrorMessage(future.getTimeoutMessage(true));
                            DefaultFuture.received(future.getChannel(), timeoutResponse);
                        }
                        Thread.sleep(30L);
                    } while (true);
                }
                catch (Throwable e) {
                    logger.error("Exception when scan the timeout invocation of remoting.", e);
                    continue;
                }
                break;
            } while (true);
        }
    }

}


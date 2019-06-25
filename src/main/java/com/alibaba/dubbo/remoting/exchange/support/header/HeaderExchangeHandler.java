/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.exchange.support.header;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.ExecutionException;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.exchange.ExchangeChannel;
import com.alibaba.dubbo.remoting.exchange.ExchangeHandler;
import com.alibaba.dubbo.remoting.exchange.Request;
import com.alibaba.dubbo.remoting.exchange.Response;
import com.alibaba.dubbo.remoting.exchange.support.DefaultFuture;
import com.alibaba.dubbo.remoting.exchange.support.header.HeaderExchangeChannel;
import com.alibaba.dubbo.remoting.exchange.support.header.HeartbeatHandler;
import com.alibaba.dubbo.remoting.transport.ChannelHandlerDelegate;
import com.alibaba.dubbo.rpc.RpcResult;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class HeaderExchangeHandler
implements ChannelHandlerDelegate {
    protected static final Logger logger = LoggerFactory.getLogger(HeaderExchangeHandler.class);
    public static String KEY_READ_TIMESTAMP = HeartbeatHandler.KEY_READ_TIMESTAMP;
    public static String KEY_WRITE_TIMESTAMP = HeartbeatHandler.KEY_WRITE_TIMESTAMP;
    private final ExchangeHandler handler;

    public HeaderExchangeHandler(ExchangeHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("handler == null");
        }
        this.handler = handler;
    }

    void handlerEvent(Channel channel, Request req) throws RemotingException {
        if (req.getData() != null && req.getData().equals("R")) {
            channel.setAttribute("channel.readonly", Boolean.TRUE);
        }
    }

    Response handleRequest(ExchangeChannel channel, Request req) throws RemotingException {
        Response res = new Response(req.getId(), req.getVersion());
        res.setClientPort(req.getClientPort());
        res.setClientIp(req.getClientIp());
        res.setServiceName(req.getServiceName());
        res.setMethodName(req.getMethodName());
        if (req.isBroken()) {
            Object data = req.getData();
            String msg = data == null ? null : (data instanceof Throwable ? StringUtils.toString((Throwable)data) : data.toString());
            res.setErrorMessage("Fail to decode request due to: " + msg);
            res.setStatus((byte)40);
            return res;
        }
        Object msg = req.getData();
        try {
            Object result = this.handler.reply(channel, msg);
            res.setStatus((byte)20);
            res.setResult(result);
        }
        catch (Throwable e) {
            RpcResult result = new RpcResult();
            result.setException(e);
            res.setResult(result);
            res.setStatus((byte)70);
            res.setErrorMessage(StringUtils.toString(e));
        }
        return res;
    }

    static void handleResponse(Channel channel, Response response) throws RemotingException {
        if (response != null && !response.isHeartbeat()) {
            DefaultFuture.received(channel, response);
        }
    }

    @Override
    public void connected(Channel channel) throws RemotingException {
        channel.setAttribute(KEY_READ_TIMESTAMP, System.currentTimeMillis());
        channel.setAttribute(KEY_WRITE_TIMESTAMP, System.currentTimeMillis());
        HeaderExchangeChannel exchangeChannel = HeaderExchangeChannel.getOrAddChannel(channel);
        try {
            this.handler.connected(exchangeChannel);
        }
        finally {
            HeaderExchangeChannel.removeChannelIfDisconnected(channel);
        }
    }

    @Override
    public void disconnected(Channel channel) throws RemotingException {
        channel.setAttribute(KEY_READ_TIMESTAMP, System.currentTimeMillis());
        channel.setAttribute(KEY_WRITE_TIMESTAMP, System.currentTimeMillis());
        HeaderExchangeChannel exchangeChannel = HeaderExchangeChannel.getOrAddChannel(channel);
        try {
            this.handler.disconnected(exchangeChannel);
        }
        finally {
            HeaderExchangeChannel.removeChannelIfDisconnected(channel);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void sent(Channel channel, Object message) throws RemotingException {
        Throwable exception;
        exception = null;
        try {
            channel.setAttribute(KEY_WRITE_TIMESTAMP, System.currentTimeMillis());
            HeaderExchangeChannel exchangeChannel = HeaderExchangeChannel.getOrAddChannel(channel);
            try {
                this.handler.sent(exchangeChannel, message);
            }
            finally {
                HeaderExchangeChannel.removeChannelIfDisconnected(channel);
            }
        }
        catch (Throwable t) {
            exception = t;
        }
        if (message instanceof Request) {
            Request request = (Request)message;
            DefaultFuture.sent(channel, request);
        }
        if (exception != null) {
            if (exception instanceof RuntimeException) {
                throw (RuntimeException)exception;
            }
            if (exception instanceof RemotingException) {
                throw (RemotingException)exception;
            }
            throw new RemotingException(channel.getLocalAddress(), channel.getRemoteAddress(), exception.getMessage(), exception);
        }
    }

    private static boolean isClientSide(Channel channel) {
        InetSocketAddress address = channel.getRemoteAddress();
        URL url = channel.getUrl();
        return url.getPort() == address.getPort() && NetUtils.filterLocalHost(url.getIp()).equals(NetUtils.filterLocalHost(address.getAddress().getHostAddress()));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void received(Channel channel, Object message) throws RemotingException {
        channel.setAttribute(KEY_READ_TIMESTAMP, System.currentTimeMillis());
        HeaderExchangeChannel exchangeChannel = HeaderExchangeChannel.getOrAddChannel(channel);
        try {
            if (message instanceof Request) {
                Request request = (Request)message;
                if (request.isEvent()) {
                    this.handlerEvent(channel, request);
                } else if (request.isTwoWay()) {
                    Response response = this.handleRequest(exchangeChannel, request);
                    channel.send(response);
                } else {
                    this.handler.received(exchangeChannel, request.getData());
                }
            } else if (message instanceof Response) {
                HeaderExchangeHandler.handleResponse(channel, (Response)message);
            } else if (message instanceof String) {
                if (HeaderExchangeHandler.isClientSide(channel)) {
                    Exception e = new Exception("Dubbo client can not supported string message: " + message + " in channel: " + channel + ", url: " + channel.getUrl());
                    logger.error(e.getMessage(), e);
                } else {
                    String echo = this.handler.telnet(channel, (String)message);
                    if (echo != null && echo.length() > 0) {
                        channel.send(echo);
                    }
                }
            } else {
                this.handler.received(exchangeChannel, message);
            }
        }
        finally {
            HeaderExchangeChannel.removeChannelIfDisconnected(channel);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public void caught(Channel channel, Throwable exception) throws RemotingException {
        ExecutionException e;
        Object msg;
        Request req;
        if (exception instanceof ExecutionException && (msg = (e = (ExecutionException)exception).getRequest()) instanceof Request && (req = (Request)msg).isTwoWay() && !req.isHeartbeat()) {
            Response res = new Response(req.getId(), req.getVersion());
            res.setStatus((byte)80);
            res.setErrorMessage(StringUtils.toString(e));
            res.setClientIp(req.getClientIp());
            res.setClientPort(req.getClientPort());
            channel.send(res);
            return;
        }
        HeaderExchangeChannel exchangeChannel = HeaderExchangeChannel.getOrAddChannel(channel);
        try {
            this.handler.caught(exchangeChannel, exception);
        }
        finally {
            HeaderExchangeChannel.removeChannelIfDisconnected(channel);
        }
    }

    @Override
    public ChannelHandler getHandler() {
        if (this.handler instanceof ChannelHandlerDelegate) {
            return ((ChannelHandlerDelegate)((Object)this.handler)).getHandler();
        }
        return this.handler;
    }
}


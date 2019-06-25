/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.transport.dispatcher;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;

public class ChannelEventRunnable
implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ChannelEventRunnable.class);
    private final ChannelHandler handler;
    private final Channel channel;
    private final ChannelState state;
    private final Throwable exception;
    private final Object message;

    public ChannelEventRunnable(Channel channel, ChannelHandler handler, ChannelState state) {
        this(channel, handler, state, null);
    }

    public ChannelEventRunnable(Channel channel, ChannelHandler handler, ChannelState state, Object message) {
        this(channel, handler, state, message, null);
    }

    public ChannelEventRunnable(Channel channel, ChannelHandler handler, ChannelState state, Throwable t) {
        this(channel, handler, state, null, t);
    }

    public ChannelEventRunnable(Channel channel, ChannelHandler handler, ChannelState state, Object message, Throwable exception) {
        this.channel = channel;
        this.handler = handler;
        this.state = state;
        this.message = message;
        this.exception = exception;
    }

    @Override
    public void run() {
        switch (this.state) {
            case CONNECTED: {
                try {
                    this.handler.connected(this.channel);
                }
                catch (Exception e) {
                    logger.warn("ChannelEventRunnable handle " + (Object)((Object)this.state) + " operation error, channel is " + this.channel, e);
                }
                break;
            }
            case DISCONNECTED: {
                try {
                    this.handler.disconnected(this.channel);
                }
                catch (Exception e) {
                    logger.warn("ChannelEventRunnable handle " + (Object)((Object)this.state) + " operation error, channel is " + this.channel, e);
                }
                break;
            }
            case SENT: {
                try {
                    this.handler.sent(this.channel, this.message);
                }
                catch (Exception e) {
                    logger.warn("ChannelEventRunnable handle " + (Object)((Object)this.state) + " operation error, channel is " + this.channel + ", message is " + this.message, e);
                }
                break;
            }
            case RECEIVED: {
                try {
                    this.handler.received(this.channel, this.message);
                }
                catch (Exception e) {
                    logger.warn("ChannelEventRunnable handle " + (Object)((Object)this.state) + " operation error, channel is " + this.channel + ", message is " + this.message, e);
                }
                break;
            }
            case CAUGHT: {
                try {
                    this.handler.caught(this.channel, this.exception);
                }
                catch (Exception e) {
                    logger.warn("ChannelEventRunnable handle " + (Object)((Object)this.state) + " operation error, channel is " + this.channel + ", message is: " + this.message + ", exception is " + this.exception, e);
                }
                break;
            }
            default: {
                logger.warn("unknown state: " + (Object)((Object)this.state) + ", message is " + this.message);
            }
        }
    }

    public static enum ChannelState {
        CONNECTED,
        DISCONNECTED,
        SENT,
        RECEIVED,
        CAUGHT;
        
    }

}


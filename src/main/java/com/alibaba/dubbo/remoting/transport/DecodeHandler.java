/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.transport;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Decodeable;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.exchange.Request;
import com.alibaba.dubbo.remoting.exchange.Response;
import com.alibaba.dubbo.remoting.transport.AbstractChannelHandlerDelegate;

public class DecodeHandler
extends AbstractChannelHandlerDelegate {
    private static final Logger log = LoggerFactory.getLogger(DecodeHandler.class);

    public DecodeHandler(ChannelHandler handler) {
        super(handler);
    }

    @Override
    public void received(Channel channel, Object message) throws RemotingException {
        if (message instanceof Decodeable) {
            this.decode(message);
        }
        if (message instanceof Request) {
            this.decode(((Request)message).getData());
        }
        if (message instanceof Response) {
            this.decode(((Response)message).getResult());
        }
        this.handler.received(channel, message);
    }

    private void decode(Object message) {
        block4 : {
            if (message != null && message instanceof Decodeable) {
                try {
                    ((Decodeable)message).decode();
                    if (log.isDebugEnabled()) {
                        log.debug(new StringBuilder(32).append("Decode decodeable message ").append(message.getClass().getName()).toString());
                    }
                }
                catch (Throwable e) {
                    if (!log.isWarnEnabled()) break block4;
                    log.warn(new StringBuilder(32).append("Call Decodeable.decode failed: ").append(e.getMessage()).toString(), e);
                }
            }
        }
    }
}


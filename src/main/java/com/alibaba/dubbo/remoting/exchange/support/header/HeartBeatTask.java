/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.exchange.support.header;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.Client;
import com.alibaba.dubbo.remoting.exchange.Request;
import com.alibaba.dubbo.remoting.exchange.support.header.HeaderExchangeHandler;
import java.net.InetSocketAddress;
import java.util.Collection;

final class HeartBeatTask
implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(HeartBeatTask.class);
    private ChannelProvider channelProvider;
    private int heartbeat;
    private int heartbeatTimeout;

    HeartBeatTask(ChannelProvider provider, int heartbeat, int heartbeatTimeout) {
        this.channelProvider = provider;
        this.heartbeat = heartbeat;
        this.heartbeatTimeout = heartbeatTimeout;
    }

    @Override
    public void run() {
        try {
            long now = System.currentTimeMillis();
            for (Channel channel : this.channelProvider.getChannels()) {
                if (channel.isClosed()) continue;
                try {
                    Long lastRead = (Long)channel.getAttribute(HeaderExchangeHandler.KEY_READ_TIMESTAMP);
                    Long lastWrite = (Long)channel.getAttribute(HeaderExchangeHandler.KEY_WRITE_TIMESTAMP);
                    if (lastRead != null && now - lastRead > (long)this.heartbeat || lastWrite != null && now - lastWrite > (long)this.heartbeat) {
                        Request req = new Request();
                        req.setVersion("2.0.0");
                        req.setTwoWay(true);
                        req.setEvent(Request.HEARTBEAT_EVENT);
                        channel.send(req);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Send heartbeat to remote channel " + channel.getRemoteAddress() + ", cause: The channel has no data-transmission exceeds a heartbeat period: " + this.heartbeat + "ms");
                        }
                    }
                    if (lastRead == null || now - lastRead <= (long)this.heartbeatTimeout) continue;
                    logger.warn("Close channel " + channel + ", because heartbeat read idle time out: " + this.heartbeatTimeout + "ms");
                    if (channel instanceof Client) {
                        try {
                            ((Client)channel).reconnect();
                        }
                        catch (Exception req) {}
                        continue;
                    }
                    channel.close();
                }
                catch (Throwable t) {
                    logger.warn("Exception when heartbeat to remote channel " + channel.getRemoteAddress(), t);
                }
            }
        }
        catch (Throwable t) {
            logger.warn("Unhandled exception when heartbeat, cause: " + t.getMessage(), t);
        }
    }

    static interface ChannelProvider {
        public Collection<Channel> getChannels();
    }

}


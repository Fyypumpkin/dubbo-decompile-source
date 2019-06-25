/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.transport;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.serialize.Serialization;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.Codec2;
import com.alibaba.dubbo.remoting.transport.CodecSupport;
import com.alibaba.dubbo.remoting.transport.ExceedPayloadLimitException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public abstract class AbstractCodec
implements Codec2 {
    private static final Logger logger = LoggerFactory.getLogger(AbstractCodec.class);

    protected Serialization getSerialization(Channel channel) {
        return CodecSupport.getSerialization(channel.getUrl());
    }

    protected static void checkPayload(Channel channel, long size) throws IOException {
        int payload = 8388608;
        if (channel != null && channel.getUrl() != null) {
            payload = channel.getUrl().getParameter("payload", 8388608);
        }
        if (payload > 0 && size > (long)payload) {
            ExceedPayloadLimitException e = new ExceedPayloadLimitException("Data length too large: " + size + ", max payload: " + payload + ", channel: " + channel);
            logger.error(e);
            throw e;
        }
    }

    protected static void checkPayload(Channel channel, long size, Object payloadObj) throws IOException {
        int payload = 8388608;
        if (channel != null && channel.getUrl() != null) {
            payload = channel.getUrl().getParameter("payload", 8388608);
        }
        if (payload > 0 && size > (long)payload) {
            IOException e = new IOException("Data length too large: " + size + ", max payload: " + payload + ", channel: " + channel + ", " + payloadObj);
            logger.error(e);
            throw e;
        }
    }

    protected boolean isClientSide(Channel channel) {
        String side = (String)channel.getAttribute("side");
        if ("client".equals(side)) {
            return true;
        }
        if ("server".equals(side)) {
            return false;
        }
        InetSocketAddress address = channel.getRemoteAddress();
        URL url = channel.getUrl();
        boolean client = url.getPort() == address.getPort() && NetUtils.filterLocalHost(url.getIp()).equals(NetUtils.filterLocalHost(address.getAddress().getHostAddress()));
        channel.setAttribute("side", client ? "client" : "server");
        return client;
    }

    protected boolean isServerSide(Channel channel) {
        return !this.isClientSide(channel);
    }
}


/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.transport;

import com.alibaba.dubbo.common.Parameters;
import com.alibaba.dubbo.common.Resetable;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Codec;
import com.alibaba.dubbo.remoting.Codec2;
import com.alibaba.dubbo.remoting.transport.AbstractPeer;
import com.alibaba.dubbo.remoting.transport.codec.CodecAdapter;
import java.net.InetSocketAddress;
import java.util.Map;

public abstract class AbstractEndpoint
extends AbstractPeer
implements Resetable {
    private static final Logger logger = LoggerFactory.getLogger(AbstractEndpoint.class);
    private Codec2 codec;
    private int timeout;
    private int connectTimeout;

    public AbstractEndpoint(URL url, ChannelHandler handler) {
        super(url, handler);
        this.codec = AbstractEndpoint.getChannelCodec(url);
        this.timeout = url.getPositiveParameter("timeout", 1000);
        this.connectTimeout = url.getPositiveParameter("connect.timeout", 3000);
    }

    @Override
    public void reset(URL url) {
        if (this.isClosed()) {
            throw new IllegalStateException("Failed to reset parameters " + url + ", cause: Channel closed. channel: " + this.getLocalAddress());
        }
        try {
            int t;
            if (url.hasParameter("heartbeat") && (t = url.getParameter("timeout", 0)) > 0) {
                this.timeout = t;
            }
        }
        catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
        try {
            if (url.hasParameter("connect.timeout") && (t = url.getParameter("connect.timeout", 0)) > 0) {
                this.connectTimeout = t;
            }
        }
        catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
        try {
            if (url.hasParameter("codec")) {
                this.codec = AbstractEndpoint.getChannelCodec(url);
            }
        }
        catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }
    }

    @Deprecated
    public void reset(Parameters parameters) {
        this.reset(this.getUrl().addParameters(parameters.getParameters()));
    }

    protected Codec2 getCodec() {
        return this.codec;
    }

    protected int getTimeout() {
        return this.timeout;
    }

    protected int getConnectTimeout() {
        return this.connectTimeout;
    }

    protected static Codec2 getChannelCodec(URL url) {
        String codecName = url.getParameter("codec", "telnet");
        if (ExtensionLoader.getExtensionLoader(Codec2.class).hasExtension(codecName)) {
            return ExtensionLoader.getExtensionLoader(Codec2.class).getExtension(codecName);
        }
        return new CodecAdapter(ExtensionLoader.getExtensionLoader(Codec.class).getExtension(codecName));
    }
}


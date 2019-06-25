/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.glassfish.grizzly.Connection
 *  org.glassfish.grizzly.GrizzlyFuture
 *  org.glassfish.grizzly.IOStrategy
 *  org.glassfish.grizzly.NIOTransportBuilder
 *  org.glassfish.grizzly.Processor
 *  org.glassfish.grizzly.filterchain.Filter
 *  org.glassfish.grizzly.filterchain.FilterChain
 *  org.glassfish.grizzly.filterchain.FilterChainBuilder
 *  org.glassfish.grizzly.filterchain.TransportFilter
 *  org.glassfish.grizzly.nio.transport.TCPNIOTransport
 *  org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder
 *  org.glassfish.grizzly.strategies.SameThreadIOStrategy
 *  org.glassfish.grizzly.threadpool.ThreadPoolConfig
 */
package com.alibaba.dubbo.remoting.transport.grizzly;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Codec2;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.transport.AbstractClient;
import com.alibaba.dubbo.remoting.transport.grizzly.GrizzlyChannel;
import com.alibaba.dubbo.remoting.transport.grizzly.GrizzlyCodecAdapter;
import com.alibaba.dubbo.remoting.transport.grizzly.GrizzlyHandler;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.GrizzlyFuture;
import org.glassfish.grizzly.IOStrategy;
import org.glassfish.grizzly.NIOTransportBuilder;
import org.glassfish.grizzly.Processor;
import org.glassfish.grizzly.filterchain.Filter;
import org.glassfish.grizzly.filterchain.FilterChain;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.strategies.SameThreadIOStrategy;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;

public class GrizzlyClient
extends AbstractClient {
    private static final Logger logger = LoggerFactory.getLogger(GrizzlyClient.class);
    private TCPNIOTransport transport;
    private volatile Connection<?> connection;

    public GrizzlyClient(URL url, ChannelHandler handler) throws RemotingException {
        super(url, handler);
    }

    @Override
    protected void doOpen() throws Throwable {
        FilterChainBuilder filterChainBuilder = FilterChainBuilder.stateless();
        filterChainBuilder.add((Filter)new TransportFilter());
        filterChainBuilder.add((Filter)new GrizzlyCodecAdapter(this.getCodec(), this.getUrl(), this));
        filterChainBuilder.add((Filter)new GrizzlyHandler(this.getUrl(), this));
        TCPNIOTransportBuilder builder = TCPNIOTransportBuilder.newInstance();
        ThreadPoolConfig config = builder.getWorkerThreadPoolConfig();
        config.setPoolName("DubboClientHandler").setQueueLimit(-1).setCorePoolSize(0).setMaxPoolSize(Integer.MAX_VALUE).setKeepAliveTime(60L, TimeUnit.SECONDS);
        builder.setTcpNoDelay(true).setKeepAlive(true).setConnectionTimeout(this.getTimeout()).setIOStrategy((IOStrategy)SameThreadIOStrategy.getInstance());
        this.transport = builder.build();
        this.transport.setProcessor((Processor)filterChainBuilder.build());
        this.transport.start();
    }

    @Override
    protected void doConnect() throws Throwable {
        this.connection = (Connection)this.transport.connect((SocketAddress)this.getConnectAddress()).get((long)this.getUrl().getPositiveParameter("timeout", 1000), TimeUnit.MILLISECONDS);
    }

    @Override
    protected void doDisConnect() throws Throwable {
        try {
            GrizzlyChannel.removeChannelIfDisconnectd(this.connection);
        }
        catch (Throwable t) {
            logger.warn(t.getMessage());
        }
    }

    @Override
    protected void doClose() throws Throwable {
        try {
            this.transport.stop();
        }
        catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
    }

    @Override
    protected Channel getChannel() {
        Connection<?> c = this.connection;
        if (c == null || !c.isOpen()) {
            return null;
        }
        return GrizzlyChannel.getOrAddChannel(c, this.getUrl(), this);
    }
}


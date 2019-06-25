/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.glassfish.grizzly.IOStrategy
 *  org.glassfish.grizzly.NIOTransportBuilder
 *  org.glassfish.grizzly.Processor
 *  org.glassfish.grizzly.filterchain.Filter
 *  org.glassfish.grizzly.filterchain.FilterChain
 *  org.glassfish.grizzly.filterchain.FilterChainBuilder
 *  org.glassfish.grizzly.filterchain.TransportFilter
 *  org.glassfish.grizzly.nio.transport.TCPNIOServerConnection
 *  org.glassfish.grizzly.nio.transport.TCPNIOTransport
 *  org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder
 *  org.glassfish.grizzly.strategies.SameThreadIOStrategy
 *  org.glassfish.grizzly.threadpool.ThreadPoolConfig
 */
package com.alibaba.dubbo.remoting.transport.grizzly;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Codec2;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.transport.AbstractServer;
import com.alibaba.dubbo.remoting.transport.grizzly.GrizzlyCodecAdapter;
import com.alibaba.dubbo.remoting.transport.grizzly.GrizzlyHandler;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import org.glassfish.grizzly.IOStrategy;
import org.glassfish.grizzly.NIOTransportBuilder;
import org.glassfish.grizzly.Processor;
import org.glassfish.grizzly.filterchain.Filter;
import org.glassfish.grizzly.filterchain.FilterChain;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.nio.transport.TCPNIOServerConnection;
import org.glassfish.grizzly.nio.transport.TCPNIOTransport;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.strategies.SameThreadIOStrategy;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;

public class GrizzlyServer
extends AbstractServer {
    private static final Logger logger = LoggerFactory.getLogger(GrizzlyServer.class);
    private final Map<String, Channel> channels = new ConcurrentHashMap<String, Channel>();
    private TCPNIOTransport transport;

    public GrizzlyServer(URL url, ChannelHandler handler) throws RemotingException {
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
        config.setPoolName("DubboServerHandler").setQueueLimit(-1);
        String threadpool = this.getUrl().getParameter("threadpool", "limited");
        if ("limited".equals(threadpool)) {
            int threads = this.getUrl().getPositiveParameter("threads", 200);
            config.setCorePoolSize(threads).setMaxPoolSize(threads).setKeepAliveTime(0L, TimeUnit.SECONDS);
        } else if ("cached".equals(threadpool)) {
            int threads = this.getUrl().getPositiveParameter("threads", Integer.MAX_VALUE);
            config.setCorePoolSize(0).setMaxPoolSize(threads).setKeepAliveTime(60L, TimeUnit.SECONDS);
        } else {
            throw new IllegalArgumentException("Unsupported threadpool type " + threadpool);
        }
        builder.setKeepAlive(true).setReuseAddress(false).setIOStrategy((IOStrategy)SameThreadIOStrategy.getInstance());
        this.transport = builder.build();
        this.transport.setProcessor((Processor)filterChainBuilder.build());
        this.transport.bind((SocketAddress)this.getBindAddress());
        this.transport.start();
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
    public boolean isBound() {
        return !this.transport.isStopped();
    }

    @Override
    public Collection<Channel> getChannels() {
        return this.channels.values();
    }

    @Override
    public Channel getChannel(InetSocketAddress remoteAddress) {
        return this.channels.get(NetUtils.toAddressString(remoteAddress));
    }

    @Override
    public void connected(Channel ch) throws RemotingException {
        this.channels.put(NetUtils.toAddressString(ch.getRemoteAddress()), ch);
        super.connected(ch);
    }

    @Override
    public void disconnected(Channel ch) throws RemotingException {
        this.channels.remove(NetUtils.toAddressString(ch.getRemoteAddress()));
        super.disconnected(ch);
    }
}


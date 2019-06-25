/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.apache.mina.common.DefaultIoFilterChainBuilder
 *  org.apache.mina.common.IoFilter
 *  org.apache.mina.common.IoHandler
 *  org.apache.mina.common.IoSession
 *  org.apache.mina.common.ThreadModel
 *  org.apache.mina.filter.codec.ProtocolCodecFactory
 *  org.apache.mina.filter.codec.ProtocolCodecFilter
 *  org.apache.mina.transport.socket.nio.SocketAcceptor
 *  org.apache.mina.transport.socket.nio.SocketAcceptorConfig
 */
package com.alibaba.dubbo.remoting.transport.mina;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ExecutorUtil;
import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Codec2;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.transport.AbstractServer;
import com.alibaba.dubbo.remoting.transport.dispatcher.ChannelHandlers;
import com.alibaba.dubbo.remoting.transport.mina.MinaChannel;
import com.alibaba.dubbo.remoting.transport.mina.MinaCodecAdapter;
import com.alibaba.dubbo.remoting.transport.mina.MinaHandler;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.apache.mina.common.DefaultIoFilterChainBuilder;
import org.apache.mina.common.IoFilter;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.ThreadModel;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketAcceptor;
import org.apache.mina.transport.socket.nio.SocketAcceptorConfig;

public class MinaServer
extends AbstractServer {
    private static final Logger logger = LoggerFactory.getLogger(MinaServer.class);
    private SocketAcceptor acceptor;

    public MinaServer(URL url, ChannelHandler handler) throws RemotingException {
        super(url, ChannelHandlers.wrap(handler, ExecutorUtil.setThreadName(url, "DubboServerHandler")));
    }

    @Override
    protected void doOpen() throws Throwable {
        this.acceptor = new SocketAcceptor(this.getUrl().getPositiveParameter("iothreads", Constants.DEFAULT_IO_THREADS), (Executor)Executors.newCachedThreadPool(new NamedThreadFactory("MinaServerWorker", true)));
        SocketAcceptorConfig cfg = this.acceptor.getDefaultConfig();
        cfg.setThreadModel(ThreadModel.MANUAL);
        this.acceptor.getFilterChain().addLast("codec", (IoFilter)new ProtocolCodecFilter((ProtocolCodecFactory)new MinaCodecAdapter(this.getCodec(), this.getUrl(), this)));
        this.acceptor.bind((SocketAddress)this.getBindAddress(), (IoHandler)new MinaHandler(this.getUrl(), this));
    }

    @Override
    protected void doClose() throws Throwable {
        try {
            if (this.acceptor != null) {
                this.acceptor.unbind((SocketAddress)this.getBindAddress());
            }
        }
        catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
    }

    @Override
    public Collection<Channel> getChannels() {
        Set sessions = this.acceptor.getManagedSessions((SocketAddress)this.getBindAddress());
        HashSet<Channel> channels = new HashSet<Channel>();
        for (IoSession session : sessions) {
            if (!session.isConnected()) continue;
            channels.add(MinaChannel.getOrAddChannel(session, this.getUrl(), this));
        }
        return channels;
    }

    @Override
    public Channel getChannel(InetSocketAddress remoteAddress) {
        Set sessions = this.acceptor.getManagedSessions((SocketAddress)this.getBindAddress());
        for (IoSession session : sessions) {
            if (!session.getRemoteAddress().equals(remoteAddress)) continue;
            return MinaChannel.getOrAddChannel(session, this.getUrl(), this);
        }
        return null;
    }

    @Override
    public boolean isBound() {
        return this.acceptor.isManaged((SocketAddress)this.getBindAddress());
    }
}


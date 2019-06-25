/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.apache.mina.common.CloseFuture
 *  org.apache.mina.common.ConnectFuture
 *  org.apache.mina.common.DefaultIoFilterChainBuilder
 *  org.apache.mina.common.IoFilter
 *  org.apache.mina.common.IoFuture
 *  org.apache.mina.common.IoFutureListener
 *  org.apache.mina.common.IoHandler
 *  org.apache.mina.common.IoSession
 *  org.apache.mina.common.ThreadModel
 *  org.apache.mina.filter.codec.ProtocolCodecFactory
 *  org.apache.mina.filter.codec.ProtocolCodecFilter
 *  org.apache.mina.transport.socket.nio.SocketConnector
 *  org.apache.mina.transport.socket.nio.SocketConnectorConfig
 *  org.apache.mina.transport.socket.nio.SocketSessionConfig
 */
package com.alibaba.dubbo.remoting.transport.mina;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.Version;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.remoting.Channel;
import com.alibaba.dubbo.remoting.ChannelHandler;
import com.alibaba.dubbo.remoting.Codec2;
import com.alibaba.dubbo.remoting.RemotingException;
import com.alibaba.dubbo.remoting.transport.AbstractClient;
import com.alibaba.dubbo.remoting.transport.mina.MinaChannel;
import com.alibaba.dubbo.remoting.transport.mina.MinaCodecAdapter;
import com.alibaba.dubbo.remoting.transport.mina.MinaHandler;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.mina.common.CloseFuture;
import org.apache.mina.common.ConnectFuture;
import org.apache.mina.common.DefaultIoFilterChainBuilder;
import org.apache.mina.common.IoFilter;
import org.apache.mina.common.IoFuture;
import org.apache.mina.common.IoFutureListener;
import org.apache.mina.common.IoHandler;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.ThreadModel;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.SocketConnector;
import org.apache.mina.transport.socket.nio.SocketConnectorConfig;
import org.apache.mina.transport.socket.nio.SocketSessionConfig;

public class MinaClient
extends AbstractClient {
    private static final Logger logger = LoggerFactory.getLogger(MinaClient.class);
    private static final Map<String, SocketConnector> connectors = new ConcurrentHashMap<String, SocketConnector>();
    private String connectorKey;
    private SocketConnector connector;
    private volatile IoSession session;

    public MinaClient(URL url, ChannelHandler handler) throws RemotingException {
        super(url, MinaClient.wrapChannelHandler(url, handler));
    }

    @Override
    protected void doOpen() throws Throwable {
        this.connectorKey = this.getUrl().toFullString();
        SocketConnector c = connectors.get(this.connectorKey);
        if (c != null) {
            this.connector = c;
        } else {
            this.connector = new SocketConnector(Constants.DEFAULT_IO_THREADS, (Executor)Executors.newCachedThreadPool(new NamedThreadFactory("MinaClientWorker", true)));
            SocketConnectorConfig cfg = this.connector.getDefaultConfig();
            cfg.setThreadModel(ThreadModel.MANUAL);
            cfg.getSessionConfig().setTcpNoDelay(true);
            cfg.getSessionConfig().setKeepAlive(true);
            int timeout = this.getTimeout();
            cfg.setConnectTimeout(timeout < 1000 ? 1 : timeout / 1000);
            this.connector.getFilterChain().addLast("codec", (IoFilter)new ProtocolCodecFilter((ProtocolCodecFactory)new MinaCodecAdapter(this.getCodec(), this.getUrl(), this)));
            connectors.put(this.connectorKey, this.connector);
        }
    }

    @Override
    protected void doConnect() throws Throwable {
        ConnectFuture future = this.connector.connect((SocketAddress)this.getConnectAddress(), (IoHandler)new MinaHandler(this.getUrl(), this));
        long start = System.currentTimeMillis();
        final AtomicReference exception = new AtomicReference();
        final CountDownLatch finish = new CountDownLatch(1);
        future.addListener(new IoFutureListener(){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            public void operationComplete(IoFuture future) {
                block20 : {
                    try {
                        if (!future.isReady()) break block20;
                        IoSession newSession = future.getSession();
                        try {
                            IoSession oldSession = MinaClient.this.session;
                            if (oldSession == null) break block20;
                            try {
                                if (logger.isInfoEnabled()) {
                                    logger.info("Close old mina channel " + (Object)oldSession + " on create new mina channel " + (Object)newSession);
                                }
                                oldSession.close();
                            }
                            finally {
                                MinaChannel.removeChannelIfDisconnectd(oldSession);
                            }
                        }
                        finally {
                            if (MinaClient.this.isClosed()) {
                                try {
                                    if (logger.isInfoEnabled()) {
                                        logger.info("Close new mina channel " + (Object)newSession + ", because the client closed.");
                                    }
                                    newSession.close();
                                }
                                finally {
                                    MinaClient.this.session = null;
                                    MinaChannel.removeChannelIfDisconnectd(newSession);
                                }
                            } else {
                                MinaClient.this.session = newSession;
                            }
                        }
                    }
                    catch (Exception e) {
                        exception.set(e);
                    }
                    finally {
                        finish.countDown();
                    }
                }
            }
        });
        try {
            finish.await(this.getTimeout(), TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e) {
            throw new RemotingException(this, "client(url: " + this.getUrl() + ") failed to connect to server " + this.getRemoteAddress() + " client-side timeout " + this.getTimeout() + "ms (elapsed: " + (System.currentTimeMillis() - start) + "ms) from netty client " + NetUtils.getLocalHost() + " using dubbo version " + Version.getVersion() + ", cause: " + e.getMessage(), (Throwable)e);
        }
        Throwable e = (Throwable)exception.get();
        if (e != null) {
            throw e;
        }
    }

    @Override
    protected void doDisConnect() throws Throwable {
        try {
            MinaChannel.removeChannelIfDisconnectd(this.session);
        }
        catch (Throwable t) {
            logger.warn(t.getMessage());
        }
    }

    @Override
    protected void doClose() throws Throwable {
    }

    @Override
    protected Channel getChannel() {
        IoSession s = this.session;
        if (s == null || !s.isConnected()) {
            return null;
        }
        return MinaChannel.getOrAddChannel(s, this.getUrl(), this);
    }

}


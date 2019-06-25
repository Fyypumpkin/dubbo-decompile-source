/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.jboss.netty.bootstrap.ClientBootstrap
 *  org.jboss.netty.channel.Channel
 *  org.jboss.netty.channel.ChannelFactory
 *  org.jboss.netty.channel.ChannelFuture
 *  org.jboss.netty.channel.ChannelHandler
 *  org.jboss.netty.channel.ChannelPipeline
 *  org.jboss.netty.channel.ChannelPipelineFactory
 *  org.jboss.netty.channel.Channels
 *  org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory
 */
package com.alibaba.dubbo.remoting.transport.netty;

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
import com.alibaba.dubbo.remoting.transport.netty.NettyChannel;
import com.alibaba.dubbo.remoting.transport.netty.NettyCodecAdapter;
import com.alibaba.dubbo.remoting.transport.netty.NettyHandler;
import com.alibaba.dubbo.remoting.transport.netty.NettyHelper;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

public class NettyClient
extends AbstractClient {
    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);
    private static final ChannelFactory channelFactory = new NioClientSocketChannelFactory((Executor)Executors.newCachedThreadPool(new NamedThreadFactory("NettyClientBoss", true)), (Executor)Executors.newCachedThreadPool(new NamedThreadFactory("NettyClientWorker", true)), Constants.DEFAULT_IO_THREADS);
    private ClientBootstrap bootstrap;
    private volatile org.jboss.netty.channel.Channel channel;

    public NettyClient(URL url, ChannelHandler handler) throws RemotingException {
        super(url, NettyClient.wrapChannelHandler(url, handler));
    }

    @Override
    protected void doOpen() throws Throwable {
        NettyHelper.setNettyLoggerFactory();
        this.bootstrap = new ClientBootstrap(channelFactory);
        this.bootstrap.setOption("keepAlive", (Object)true);
        this.bootstrap.setOption("tcpNoDelay", (Object)true);
        this.bootstrap.setOption("connectTimeoutMillis", (Object)this.getTimeout());
        final NettyHandler nettyHandler = new NettyHandler(this.getUrl(), this);
        this.bootstrap.setPipelineFactory(new ChannelPipelineFactory(){

            public ChannelPipeline getPipeline() {
                NettyCodecAdapter adapter = new NettyCodecAdapter(NettyClient.this.getCodec(), NettyClient.this.getUrl(), NettyClient.this);
                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("decoder", adapter.getDecoder());
                pipeline.addLast("encoder", adapter.getEncoder());
                pipeline.addLast("handler", (org.jboss.netty.channel.ChannelHandler)nettyHandler);
                return pipeline;
            }
        });
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    protected void doConnect() throws Throwable {
        block21 : {
            long start = System.currentTimeMillis();
            ChannelFuture future = this.bootstrap.connect((SocketAddress)this.getConnectAddress());
            try {
                boolean ret = future.awaitUninterruptibly((long)this.getConnectTimeout(), TimeUnit.MILLISECONDS);
                if (ret && future.isSuccess()) {
                    org.jboss.netty.channel.Channel newChannel = future.getChannel();
                    newChannel.setInterestOps(5);
                    try {
                        org.jboss.netty.channel.Channel oldChannel = this.channel;
                        if (oldChannel == null) break block21;
                        try {
                            if (logger.isInfoEnabled()) {
                                logger.info("Close old netty channel " + (Object)oldChannel + " on create new netty channel " + (Object)newChannel);
                            }
                            oldChannel.close();
                            break block21;
                        }
                        finally {
                            NettyChannel.removeChannelIfDisconnected(oldChannel);
                        }
                    }
                    finally {
                        if (this.isClosed()) {
                            try {
                                if (logger.isInfoEnabled()) {
                                    logger.info("Close new netty channel " + (Object)newChannel + ", because the client closed.");
                                }
                                newChannel.close();
                            }
                            finally {
                                this.channel = null;
                                NettyChannel.removeChannelIfDisconnected(newChannel);
                            }
                        } else {
                            this.channel = newChannel;
                        }
                    }
                }
                if (future.getCause() != null) {
                    throw new RemotingException(this, "client(url: " + this.getUrl() + ") failed to connect to server " + this.getRemoteAddress() + ", error message is:" + future.getCause().getMessage(), future.getCause());
                }
                throw new RemotingException((Channel)this, "client(url: " + this.getUrl() + ") failed to connect to server " + this.getRemoteAddress() + " client-side timeout " + this.getConnectTimeout() + "ms (elapsed: " + (System.currentTimeMillis() - start) + "ms) from netty client " + NetUtils.getLocalHost() + " using dubbo version " + Version.getVersion());
            }
            finally {
                if (!this.isConnected()) {
                    future.cancel();
                }
            }
        }
    }

    @Override
    protected void doDisConnect() throws Throwable {
        try {
            NettyChannel.removeChannelIfDisconnected(this.channel);
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
        org.jboss.netty.channel.Channel c = this.channel;
        if (c == null || !c.isConnected()) {
            return null;
        }
        return NettyChannel.getOrAddChannel(c, this.getUrl(), this);
    }

}


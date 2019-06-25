/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.jboss.netty.channel.ChannelHandler
 *  org.jboss.netty.channel.ChannelPipeline
 *  org.jboss.netty.channel.ChannelPipelineFactory
 *  org.jboss.netty.channel.Channels
 *  org.jboss.netty.handler.codec.http.HttpChunkAggregator
 *  org.jboss.netty.handler.codec.http.HttpRequestDecoder
 *  org.jboss.netty.handler.codec.http.HttpResponseEncoder
 *  org.jboss.netty.handler.execution.ExecutionHandler
 *  org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor
 *  org.jboss.resteasy.core.SynchronousDispatcher
 *  org.jboss.resteasy.plugins.server.netty.RequestDispatcher
 *  org.jboss.resteasy.plugins.server.netty.RequestHandler
 *  org.jboss.resteasy.plugins.server.netty.RestEasyHttpRequestDecoder
 *  org.jboss.resteasy.plugins.server.netty.RestEasyHttpRequestDecoder$Protocol
 *  org.jboss.resteasy.plugins.server.netty.RestEasyHttpResponseEncoder
 */
package com.alibaba.dubbo.rpc.protocol.rest;

import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.execution.ExecutionHandler;
import org.jboss.netty.handler.execution.OrderedMemoryAwareThreadPoolExecutor;
import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.plugins.server.netty.RequestDispatcher;
import org.jboss.resteasy.plugins.server.netty.RequestHandler;
import org.jboss.resteasy.plugins.server.netty.RestEasyHttpRequestDecoder;
import org.jboss.resteasy.plugins.server.netty.RestEasyHttpResponseEncoder;

public class ResteasyHttpServerPipelineFactory
implements ChannelPipelineFactory {
    private final ChannelHandler resteasyEncoder;
    private final ChannelHandler resteasyDecoder;
    private final ChannelHandler resteasyRequestHandler;
    private final ChannelHandler executionHandler;
    private final int maxRequestSize;

    public ResteasyHttpServerPipelineFactory(RequestDispatcher dispatcher, String root, int executorThreadCount, int maxRequestSize, boolean isKeepAlive) {
        this.resteasyDecoder = new RestEasyHttpRequestDecoder(dispatcher.getDispatcher(), root, this.getProtocol(), isKeepAlive);
        this.resteasyEncoder = new RestEasyHttpResponseEncoder(dispatcher);
        this.resteasyRequestHandler = new RequestHandler(dispatcher);
        this.executionHandler = executorThreadCount > 0 ? new ExecutionHandler((Executor)new OrderedMemoryAwareThreadPoolExecutor(executorThreadCount, 0L, 0L, 30L, TimeUnit.SECONDS, (ThreadFactory)new NamedThreadFactory("RestServerHandler"))) : null;
        this.maxRequestSize = maxRequestSize;
    }

    public ChannelPipeline getPipeline() throws Exception {
        ChannelPipeline pipeline = Channels.pipeline();
        pipeline.addLast("decoder", (ChannelHandler)new HttpRequestDecoder());
        pipeline.addLast("aggregator", (ChannelHandler)new HttpChunkAggregator(this.maxRequestSize));
        pipeline.addLast("resteasyDecoder", this.resteasyDecoder);
        pipeline.addLast("encoder", (ChannelHandler)new HttpResponseEncoder());
        pipeline.addLast("resteasyEncoder", this.resteasyEncoder);
        if (this.executionHandler != null) {
            pipeline.addLast("executionHandler", this.executionHandler);
        }
        pipeline.addLast("handler", this.resteasyRequestHandler);
        return pipeline;
    }

    protected RestEasyHttpRequestDecoder.Protocol getProtocol() {
        return RestEasyHttpRequestDecoder.Protocol.HTTP;
    }
}


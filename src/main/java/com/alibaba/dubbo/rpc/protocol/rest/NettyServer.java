/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.jboss.resteasy.spi.ResteasyDeployment
 */
package com.alibaba.dubbo.rpc.protocol.rest;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.protocol.rest.BaseRestServer;
import com.alibaba.dubbo.rpc.protocol.rest.RestNettyJaxrsServer;
import org.jboss.resteasy.spi.ResteasyDeployment;

public class NettyServer
extends BaseRestServer {
    private final RestNettyJaxrsServer server = new RestNettyJaxrsServer();

    @Override
    protected void doStart(URL url) {
        this.server.setPort(url.getPort());
        this.server.setKeepAlive(url.getParameter("keepalive", true));
        this.server.setExecutorThreadCount(url.getParameter("threads", 200));
        this.server.setIoWorkerCount(url.getParameter("iothreads", Constants.DEFAULT_IO_THREADS));
        this.server.setBacklog(url.getParameter("backlog", 512));
        this.server.start();
    }

    @Override
    public void stop() {
        this.server.stop();
    }

    @Override
    protected ResteasyDeployment getDeployment() {
        return this.server.getDeployment();
    }
}


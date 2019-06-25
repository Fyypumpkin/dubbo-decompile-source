/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.jboss.resteasy.plugins.server.sun.http.SunHttpJaxrsServer
 *  org.jboss.resteasy.spi.ResteasyDeployment
 */
package com.alibaba.dubbo.rpc.protocol.rest;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.protocol.rest.BaseRestServer;
import org.jboss.resteasy.plugins.server.sun.http.SunHttpJaxrsServer;
import org.jboss.resteasy.spi.ResteasyDeployment;

public class SunHttpServer
extends BaseRestServer {
    private final SunHttpJaxrsServer server = new SunHttpJaxrsServer();

    @Override
    protected void doStart(URL url) {
        this.server.setPort(url.getPort());
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


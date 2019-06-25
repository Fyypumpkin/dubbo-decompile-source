/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.jboss.resteasy.spi.ResteasyDeployment
 */
package com.alibaba.dubbo.rpc.protocol.rest;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.rpc.protocol.rest.BaseRestServer;
import org.jboss.resteasy.spi.ResteasyDeployment;

public class TjwsServer
extends BaseRestServer {
    @Override
    protected void doStart(URL url) {
        throw new UnsupportedOperationException("TJWS server is now unsupported");
    }

    @Override
    protected ResteasyDeployment getDeployment() {
        throw new UnsupportedOperationException("TJWS server is now unsupported");
    }

    @Override
    public void stop() {
        throw new UnsupportedOperationException("TJWS server is now unsupported");
    }
}


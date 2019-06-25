/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.protocol.rest;

import com.alibaba.dubbo.remoting.http.HttpBinder;
import com.alibaba.dubbo.rpc.protocol.rest.DubboHttpServer;
import com.alibaba.dubbo.rpc.protocol.rest.NettyServer;
import com.alibaba.dubbo.rpc.protocol.rest.RestServer;
import com.alibaba.dubbo.rpc.protocol.rest.SunHttpServer;

public class RestServerFactory {
    private HttpBinder httpBinder;

    public void setHttpBinder(HttpBinder httpBinder) {
        this.httpBinder = httpBinder;
    }

    public RestServer createServer(String name) {
        if ("servlet".equalsIgnoreCase(name) || "jetty".equalsIgnoreCase(name) || "jetty9".equals(name) || "tomcat".equalsIgnoreCase(name)) {
            return new DubboHttpServer(this.httpBinder);
        }
        if ("netty".equalsIgnoreCase(name)) {
            return new NettyServer();
        }
        if ("sunhttp".equalsIgnoreCase(name)) {
            return new SunHttpServer();
        }
        throw new IllegalArgumentException("Unrecognized server name: " + name);
    }
}


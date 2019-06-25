/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.http.jetty;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.http.HttpBinder;
import com.alibaba.dubbo.remoting.http.HttpHandler;
import com.alibaba.dubbo.remoting.http.HttpServer;
import com.alibaba.dubbo.remoting.http.jetty.JettyHttpServer;

public class JettyHttpBinder
implements HttpBinder {
    @Override
    public HttpServer bind(URL url, HttpHandler handler) {
        return new JettyHttpServer(url, handler);
    }
}


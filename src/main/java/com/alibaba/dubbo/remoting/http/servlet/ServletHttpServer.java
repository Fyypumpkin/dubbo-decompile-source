/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.http.servlet;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.http.HttpHandler;
import com.alibaba.dubbo.remoting.http.servlet.DispatcherServlet;
import com.alibaba.dubbo.remoting.http.support.AbstractHttpServer;

public class ServletHttpServer
extends AbstractHttpServer {
    public ServletHttpServer(URL url, HttpHandler handler) {
        super(url, handler);
        DispatcherServlet.addHttpHandler(url.getPort(), handler);
    }
}


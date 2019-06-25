/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.http.servlet;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Adaptive;
import com.alibaba.dubbo.remoting.http.HttpBinder;
import com.alibaba.dubbo.remoting.http.HttpHandler;
import com.alibaba.dubbo.remoting.http.HttpServer;
import com.alibaba.dubbo.remoting.http.servlet.ServletHttpServer;

public class ServletHttpBinder
implements HttpBinder {
    @Adaptive
    @Override
    public HttpServer bind(URL url, HttpHandler handler) {
        return new ServletHttpServer(url, handler);
    }
}


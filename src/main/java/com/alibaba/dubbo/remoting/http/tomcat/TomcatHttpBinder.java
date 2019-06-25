/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.http.tomcat;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.http.HttpBinder;
import com.alibaba.dubbo.remoting.http.HttpHandler;
import com.alibaba.dubbo.remoting.http.HttpServer;
import com.alibaba.dubbo.remoting.http.tomcat.TomcatHttpServer;

public class TomcatHttpBinder
implements HttpBinder {
    @Override
    public HttpServer bind(URL url, HttpHandler handler) {
        return new TomcatHttpServer(url, handler);
    }
}


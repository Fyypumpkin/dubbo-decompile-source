/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.http;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Adaptive;
import com.alibaba.dubbo.common.extension.SPI;
import com.alibaba.dubbo.remoting.http.HttpHandler;
import com.alibaba.dubbo.remoting.http.HttpServer;

@SPI(value="jetty")
public interface HttpBinder {
    @Adaptive(value={"server"})
    public HttpServer bind(URL var1, HttpHandler var2);
}


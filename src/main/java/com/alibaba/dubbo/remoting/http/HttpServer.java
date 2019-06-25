/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.http;

import com.alibaba.dubbo.common.Resetable;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.http.HttpHandler;
import java.net.InetSocketAddress;

public interface HttpServer
extends Resetable {
    public HttpHandler getHttpHandler();

    public URL getUrl();

    public InetSocketAddress getLocalAddress();

    public void close();

    public void close(int var1);

    public boolean isBound();

    public boolean isClosed();
}


/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  com.caucho.hessian.HessianException
 *  com.caucho.hessian.client.HessianConnectionException
 *  com.caucho.hessian.client.HessianConnectionFactory
 *  com.caucho.hessian.client.HessianProxyFactory
 *  com.caucho.hessian.io.HessianMethodSerializationException
 *  com.caucho.hessian.server.HessianSkeleton
 *  javax.servlet.ServletException
 *  javax.servlet.ServletInputStream
 *  javax.servlet.ServletOutputStream
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 */
package com.alibaba.dubbo.rpc.protocol.hessian;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.remoting.http.HttpBinder;
import com.alibaba.dubbo.remoting.http.HttpHandler;
import com.alibaba.dubbo.remoting.http.HttpServer;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.protocol.AbstractProxyProtocol;
import com.alibaba.dubbo.rpc.protocol.hessian.HttpClientConnectionFactory;
import com.caucho.hessian.HessianException;
import com.caucho.hessian.client.HessianConnectionException;
import com.caucho.hessian.client.HessianConnectionFactory;
import com.caucho.hessian.client.HessianProxyFactory;
import com.caucho.hessian.io.HessianMethodSerializationException;
import com.caucho.hessian.server.HessianSkeleton;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HessianProtocol
extends AbstractProxyProtocol {
    private final Map<String, HttpServer> serverMap = new ConcurrentHashMap<String, HttpServer>();
    private final Map<String, HessianSkeleton> skeletonMap = new ConcurrentHashMap<String, HessianSkeleton>();
    private HttpBinder httpBinder;

    public HessianProtocol() {
        super(HessianException.class);
    }

    public void setHttpBinder(HttpBinder httpBinder) {
        this.httpBinder = httpBinder;
    }

    @Override
    public int getDefaultPort() {
        return 80;
    }

    @Override
    protected <T> Runnable doExport(T impl, Class<T> type, URL url) throws RpcException {
        String addr = url.getIp() + ":" + url.getPort();
        HttpServer server = this.serverMap.get(addr);
        if (server == null) {
            server = this.httpBinder.bind(url, new HessianHandler());
            this.serverMap.put(addr, server);
        }
        final String path = url.getAbsolutePath();
        HessianSkeleton skeleton = new HessianSkeleton(impl, type);
        this.skeletonMap.put(path, skeleton);
        return new Runnable(){

            @Override
            public void run() {
                HessianProtocol.this.skeletonMap.remove(path);
            }
        };
    }

    @Override
    protected <T> T doRefer(Class<T> serviceType, URL url) throws RpcException {
        HessianProxyFactory hessianProxyFactory = new HessianProxyFactory();
        String client = url.getParameter("client", "jdk");
        if ("httpclient".equals(client)) {
            hessianProxyFactory.setConnectionFactory((HessianConnectionFactory)new HttpClientConnectionFactory());
        } else if (client != null && client.length() > 0 && !"jdk".equals(client)) {
            throw new IllegalStateException("Unsupported http protocol client=\"" + client + "\"!");
        }
        int timeout = url.getParameter("timeout", 1000);
        hessianProxyFactory.setConnectTimeout((long)timeout);
        hessianProxyFactory.setReadTimeout((long)timeout);
        return (T)hessianProxyFactory.create(serviceType, url.setProtocol("http").toJavaURL(), Thread.currentThread().getContextClassLoader());
    }

    @Override
    protected int getErrorCode(Throwable e) {
        if (e instanceof HessianConnectionException) {
            Class<?> cls;
            if (e.getCause() != null && SocketTimeoutException.class.equals(cls = e.getCause().getClass())) {
                return 2;
            }
            return 1;
        }
        if (e instanceof HessianMethodSerializationException) {
            return 5;
        }
        return super.getErrorCode(e);
    }

    @Override
    public void destroy() {
        super.destroy();
        for (String key : new ArrayList<String>(this.serverMap.keySet())) {
            HttpServer server = this.serverMap.remove(key);
            if (server == null) continue;
            try {
                if (this.logger.isInfoEnabled()) {
                    this.logger.info("Close hessian server " + server.getUrl());
                }
                server.close();
            }
            catch (Throwable t) {
                this.logger.warn(t.getMessage(), t);
            }
        }
    }

    private class HessianHandler
    implements HttpHandler {
        private HessianHandler() {
        }

        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            String uri = request.getRequestURI();
            HessianSkeleton skeleton = (HessianSkeleton)HessianProtocol.this.skeletonMap.get(uri);
            if (!request.getMethod().equalsIgnoreCase("POST")) {
                response.setStatus(500);
            } else {
                RpcContext.getContext().setRemoteAddress(request.getRemoteAddr(), request.getRemotePort());
                try {
                    skeleton.invoke((InputStream)request.getInputStream(), (OutputStream)response.getOutputStream());
                }
                catch (Throwable e) {
                    throw new ServletException(e);
                }
            }
        }
    }

}


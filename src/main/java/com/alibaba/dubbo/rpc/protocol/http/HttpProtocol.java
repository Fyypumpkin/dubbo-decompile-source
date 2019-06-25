/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  org.springframework.remoting.RemoteAccessException
 *  org.springframework.remoting.httpinvoker.HttpComponentsHttpInvokerRequestExecutor
 *  org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean
 *  org.springframework.remoting.httpinvoker.HttpInvokerRequestExecutor
 *  org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter
 *  org.springframework.remoting.httpinvoker.SimpleHttpInvokerRequestExecutor
 */
package com.alibaba.dubbo.rpc.protocol.http;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.http.HttpBinder;
import com.alibaba.dubbo.remoting.http.HttpHandler;
import com.alibaba.dubbo.remoting.http.HttpServer;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.protocol.AbstractProxyProtocol;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.remoting.RemoteAccessException;
import org.springframework.remoting.httpinvoker.HttpComponentsHttpInvokerRequestExecutor;
import org.springframework.remoting.httpinvoker.HttpInvokerProxyFactoryBean;
import org.springframework.remoting.httpinvoker.HttpInvokerRequestExecutor;
import org.springframework.remoting.httpinvoker.HttpInvokerServiceExporter;
import org.springframework.remoting.httpinvoker.SimpleHttpInvokerRequestExecutor;

public class HttpProtocol
extends AbstractProxyProtocol {
    public static final int DEFAULT_PORT = 80;
    private final Map<String, HttpServer> serverMap = new ConcurrentHashMap<String, HttpServer>();
    private final Map<String, HttpInvokerServiceExporter> skeletonMap = new ConcurrentHashMap<String, HttpInvokerServiceExporter>();
    private HttpBinder httpBinder;

    public HttpProtocol() {
        super(RemoteAccessException.class);
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
            server = this.httpBinder.bind(url, new InternalHandler());
            this.serverMap.put(addr, server);
        }
        HttpInvokerServiceExporter httpServiceExporter = new HttpInvokerServiceExporter();
        httpServiceExporter.setServiceInterface(type);
        httpServiceExporter.setService(impl);
        try {
            httpServiceExporter.afterPropertiesSet();
        }
        catch (Exception e) {
            throw new RpcException(e.getMessage(), (Throwable)e);
        }
        final String path = url.getAbsolutePath();
        this.skeletonMap.put(path, httpServiceExporter);
        return new Runnable(){

            @Override
            public void run() {
                HttpProtocol.this.skeletonMap.remove(path);
            }
        };
    }

    @Override
    protected <T> T doRefer(Class<T> serviceType, final URL url) throws RpcException {
        HttpInvokerProxyFactoryBean httpProxyFactoryBean = new HttpInvokerProxyFactoryBean();
        httpProxyFactoryBean.setServiceUrl(url.toIdentityString());
        httpProxyFactoryBean.setServiceInterface(serviceType);
        String client = url.getParameter("client");
        if (client == null || client.length() == 0 || "simple".equals(client)) {
            SimpleHttpInvokerRequestExecutor httpInvokerRequestExecutor = new SimpleHttpInvokerRequestExecutor(){

                protected void prepareConnection(HttpURLConnection con, int contentLength) throws IOException {
                    super.prepareConnection(con, contentLength);
                    con.setReadTimeout(url.getParameter("timeout", 1000));
                    con.setConnectTimeout(url.getParameter("connect.timeout", 3000));
                }
            };
            httpProxyFactoryBean.setHttpInvokerRequestExecutor((HttpInvokerRequestExecutor)httpInvokerRequestExecutor);
        } else if ("commons".equals(client)) {
            HttpComponentsHttpInvokerRequestExecutor httpInvokerRequestExecutor = new HttpComponentsHttpInvokerRequestExecutor();
            httpInvokerRequestExecutor.setReadTimeout(url.getParameter("connect.timeout", 3000));
            httpProxyFactoryBean.setHttpInvokerRequestExecutor((HttpInvokerRequestExecutor)httpInvokerRequestExecutor);
        } else if (client != null && client.length() > 0) {
            throw new IllegalStateException("Unsupported http protocol client " + client + ", only supported: simple, commons");
        }
        httpProxyFactoryBean.afterPropertiesSet();
        return (T)httpProxyFactoryBean.getObject();
    }

    @Override
    protected int getErrorCode(Throwable e) {
        if (e instanceof RemoteAccessException) {
            e = e.getCause();
        }
        if (e != null) {
            Class<?> cls = e.getClass();
            if (SocketTimeoutException.class.equals(cls)) {
                return 2;
            }
            if (IOException.class.isAssignableFrom(cls)) {
                return 1;
            }
            if (ClassNotFoundException.class.isAssignableFrom(cls)) {
                return 5;
            }
        }
        return super.getErrorCode(e);
    }

    private class InternalHandler
    implements HttpHandler {
        private InternalHandler() {
        }

        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            String uri = request.getRequestURI();
            HttpInvokerServiceExporter skeleton = (HttpInvokerServiceExporter)HttpProtocol.this.skeletonMap.get(uri);
            if (!request.getMethod().equalsIgnoreCase("POST")) {
                response.setStatus(500);
            } else {
                RpcContext.getContext().setRemoteAddress(request.getRemoteAddr(), request.getRemotePort());
                try {
                    skeleton.handleRequest(request, response);
                }
                catch (Throwable e) {
                    throw new ServletException(e);
                }
            }
        }
    }

}


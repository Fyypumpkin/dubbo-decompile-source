/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletConfig
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletException
 *  javax.servlet.ServletRequest
 *  javax.servlet.ServletResponse
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher
 *  org.jboss.resteasy.spi.ResteasyDeployment
 */
package com.alibaba.dubbo.rpc.protocol.rest;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.http.HttpBinder;
import com.alibaba.dubbo.remoting.http.HttpHandler;
import com.alibaba.dubbo.remoting.http.HttpServer;
import com.alibaba.dubbo.remoting.http.servlet.BootstrapListener;
import com.alibaba.dubbo.remoting.http.servlet.ServletManager;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.protocol.rest.BaseRestServer;
import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jboss.resteasy.plugins.server.servlet.HttpServletDispatcher;
import org.jboss.resteasy.spi.ResteasyDeployment;

public class DubboHttpServer
extends BaseRestServer {
    private final HttpServletDispatcher dispatcher = new HttpServletDispatcher();
    private final ResteasyDeployment deployment = new ResteasyDeployment();
    private HttpBinder httpBinder;
    private HttpServer httpServer;

    public DubboHttpServer(HttpBinder httpBinder) {
        this.httpBinder = httpBinder;
    }

    @Override
    protected void doStart(URL url) {
        this.httpServer = this.httpBinder.bind(url, new RestHandler());
        ServletContext servletContext = ServletManager.getInstance().getServletContext(url.getPort());
        if (servletContext == null) {
            servletContext = ServletManager.getInstance().getServletContext(-1234);
        }
        if (servletContext == null) {
            throw new RpcException("No servlet context found. If you are using server='servlet', make sure that you've configured " + BootstrapListener.class.getName() + " in web.xml");
        }
        servletContext.setAttribute(ResteasyDeployment.class.getName(), (Object)this.deployment);
        try {
            this.dispatcher.init((ServletConfig)new SimpleServletConfig(servletContext));
        }
        catch (ServletException e) {
            throw new RpcException((Throwable)e);
        }
    }

    @Override
    public void stop() {
        this.httpServer.close();
    }

    @Override
    protected ResteasyDeployment getDeployment() {
        return this.deployment;
    }

    private static class SimpleServletConfig
    implements ServletConfig {
        private final ServletContext servletContext;

        public SimpleServletConfig(ServletContext servletContext) {
            this.servletContext = servletContext;
        }

        public String getServletName() {
            return "DispatcherServlet";
        }

        public ServletContext getServletContext() {
            return this.servletContext;
        }

        public String getInitParameter(String s) {
            return null;
        }

        public Enumeration getInitParameterNames() {
            return new Enumeration(){

                @Override
                public boolean hasMoreElements() {
                    return false;
                }

                public Object nextElement() {
                    return null;
                }
            };
        }

    }

    private class RestHandler
    implements HttpHandler {
        private RestHandler() {
        }

        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            RpcContext.getContext().setRemoteAddress(request.getRemoteAddr(), request.getRemotePort());
            DubboHttpServer.this.dispatcher.service((ServletRequest)request, (ServletResponse)response);
        }
    }

}


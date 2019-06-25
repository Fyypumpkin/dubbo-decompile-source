/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletConfig
 *  javax.servlet.ServletException
 *  javax.servlet.http.HttpServlet
 *  javax.servlet.http.HttpServletRequest
 *  javax.servlet.http.HttpServletResponse
 *  org.apache.cxf.Bus
 *  org.apache.cxf.bus.extension.ExtensionManagerBus
 *  org.apache.cxf.endpoint.Client
 *  org.apache.cxf.endpoint.Server
 *  org.apache.cxf.frontend.ClientProxy
 *  org.apache.cxf.frontend.ClientProxyFactoryBean
 *  org.apache.cxf.frontend.ServerFactoryBean
 *  org.apache.cxf.interceptor.Fault
 *  org.apache.cxf.transport.Conduit
 *  org.apache.cxf.transport.DestinationFactory
 *  org.apache.cxf.transport.http.DestinationRegistry
 *  org.apache.cxf.transport.http.HTTPConduit
 *  org.apache.cxf.transport.http.HTTPTransportFactory
 *  org.apache.cxf.transport.http.HttpDestinationFactory
 *  org.apache.cxf.transport.servlet.ServletController
 *  org.apache.cxf.transport.servlet.ServletDestinationFactory
 *  org.apache.cxf.transports.http.configuration.HTTPClientPolicy
 */
package com.alibaba.dubbo.rpc.protocol.webservice;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.remoting.http.HttpBinder;
import com.alibaba.dubbo.remoting.http.HttpHandler;
import com.alibaba.dubbo.remoting.http.HttpServer;
import com.alibaba.dubbo.remoting.http.servlet.DispatcherServlet;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.protocol.AbstractProxyProtocol;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.cxf.Bus;
import org.apache.cxf.bus.extension.ExtensionManagerBus;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.frontend.ClientProxyFactoryBean;
import org.apache.cxf.frontend.ServerFactoryBean;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.cxf.transport.http.DestinationRegistry;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transport.http.HTTPTransportFactory;
import org.apache.cxf.transport.http.HttpDestinationFactory;
import org.apache.cxf.transport.servlet.ServletController;
import org.apache.cxf.transport.servlet.ServletDestinationFactory;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

public class WebServiceProtocol
extends AbstractProxyProtocol {
    public static final int DEFAULT_PORT = 80;
    private final Map<String, HttpServer> serverMap = new ConcurrentHashMap<String, HttpServer>();
    private final ExtensionManagerBus bus = new ExtensionManagerBus();
    private final HTTPTransportFactory transportFactory = new HTTPTransportFactory((Bus)this.bus);
    private HttpBinder httpBinder;

    public WebServiceProtocol() {
        super(Fault.class);
        this.bus.setExtension((Object)new ServletDestinationFactory(), HttpDestinationFactory.class);
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
        HttpServer httpServer = this.serverMap.get(addr);
        if (httpServer == null) {
            httpServer = this.httpBinder.bind(url, new WebServiceHandler());
            this.serverMap.put(addr, httpServer);
        }
        final ServerFactoryBean serverFactoryBean = new ServerFactoryBean();
        serverFactoryBean.setAddress(url.getAbsolutePath());
        serverFactoryBean.setServiceClass(type);
        serverFactoryBean.setServiceBean(impl);
        serverFactoryBean.setBus((Bus)this.bus);
        serverFactoryBean.setDestinationFactory((DestinationFactory)this.transportFactory);
        serverFactoryBean.create();
        return new Runnable(){

            @Override
            public void run() {
                serverFactoryBean.destroy();
            }
        };
    }

    @Override
    protected <T> T doRefer(Class<T> serviceType, URL url) throws RpcException {
        ClientProxyFactoryBean proxyFactoryBean = new ClientProxyFactoryBean();
        proxyFactoryBean.setAddress(url.setProtocol("http").toIdentityString());
        proxyFactoryBean.setServiceClass(serviceType);
        proxyFactoryBean.setBus((Bus)this.bus);
        Object ref = proxyFactoryBean.create();
        Client proxy = ClientProxy.getClient((Object)ref);
        HTTPConduit conduit = (HTTPConduit)proxy.getConduit();
        HTTPClientPolicy policy = new HTTPClientPolicy();
        policy.setConnectionTimeout((long)url.getParameter("connect.timeout", 3000));
        policy.setReceiveTimeout((long)url.getParameter("timeout", 1000));
        conduit.setClient(policy);
        return (T)ref;
    }

    @Override
    protected int getErrorCode(Throwable e) {
        if (e instanceof Fault) {
            e = e.getCause();
        }
        if (e instanceof SocketTimeoutException) {
            return 2;
        }
        if (e instanceof IOException) {
            return 1;
        }
        return super.getErrorCode(e);
    }

    private class WebServiceHandler
    implements HttpHandler {
        private volatile ServletController servletController;

        private WebServiceHandler() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void handle(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            if (this.servletController == null) {
                DispatcherServlet httpServlet = DispatcherServlet.getInstance();
                if (httpServlet == null) {
                    response.sendError(500, "No such DispatcherServlet instance.");
                    return;
                }
                WebServiceHandler webServiceHandler = this;
                synchronized (webServiceHandler) {
                    if (this.servletController == null) {
                        this.servletController = new ServletController(WebServiceProtocol.this.transportFactory.getRegistry(), httpServlet.getServletConfig(), (HttpServlet)httpServlet);
                    }
                }
            }
            RpcContext.getContext().setRemoteAddress(request.getRemoteAddr(), request.getRemotePort());
            this.servletController.invoke(request, response);
        }
    }

}


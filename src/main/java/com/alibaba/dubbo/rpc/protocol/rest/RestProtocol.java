/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.ws.rs.ProcessingException
 *  javax.ws.rs.WebApplicationException
 *  org.apache.http.HeaderElement
 *  org.apache.http.HeaderIterator
 *  org.apache.http.HttpResponse
 *  org.apache.http.client.HttpClient
 *  org.apache.http.conn.ClientConnectionManager
 *  org.apache.http.conn.ConnectionKeepAliveStrategy
 *  org.apache.http.impl.client.DefaultHttpClient
 *  org.apache.http.impl.conn.PoolingClientConnectionManager
 *  org.apache.http.message.BasicHeaderElementIterator
 *  org.apache.http.params.HttpConnectionParams
 *  org.apache.http.params.HttpParams
 *  org.apache.http.protocol.HttpContext
 *  org.jboss.resteasy.client.jaxrs.ClientHttpEngine
 *  org.jboss.resteasy.client.jaxrs.ResteasyClient
 *  org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder
 *  org.jboss.resteasy.client.jaxrs.ResteasyWebTarget
 *  org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine
 *  org.jboss.resteasy.util.GetRestful
 */
package com.alibaba.dubbo.rpc.protocol.rest;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.remoting.http.HttpBinder;
import com.alibaba.dubbo.remoting.http.servlet.BootstrapListener;
import com.alibaba.dubbo.remoting.http.servlet.ServletManager;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.ServiceClassHolder;
import com.alibaba.dubbo.rpc.protocol.AbstractProxyProtocol;
import com.alibaba.dubbo.rpc.protocol.rest.RestServer;
import com.alibaba.dubbo.rpc.protocol.rest.RestServerFactory;
import com.alibaba.dubbo.rpc.protocol.rest.RpcContextFilter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import javax.servlet.ServletContext;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import org.apache.http.HeaderElement;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.jboss.resteasy.client.jaxrs.ClientHttpEngine;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.client.jaxrs.engines.ApacheHttpClient4Engine;
import org.jboss.resteasy.util.GetRestful;

public class RestProtocol
extends AbstractProxyProtocol {
    private static final int DEFAULT_PORT = 80;
    private final Map<String, RestServer> servers = new ConcurrentHashMap<String, RestServer>();
    private final RestServerFactory serverFactory = new RestServerFactory();
    private final List<ResteasyClient> clients = Collections.synchronizedList(new LinkedList());
    private volatile ConnectionMonitor connectionMonitor;

    public RestProtocol() {
        super(WebApplicationException.class, ProcessingException.class);
    }

    public void setHttpBinder(HttpBinder httpBinder) {
        this.serverFactory.setHttpBinder(httpBinder);
    }

    @Override
    public int getDefaultPort() {
        return 80;
    }

    @Override
    protected <T> Runnable doExport(T impl, Class<T> type, URL url) throws RpcException {
        String addr = url.getIp() + ":" + url.getPort();
        Class<T> implClass = ServiceClassHolder.getInstance().popServiceClass();
        RestServer server = this.servers.get(addr);
        if (server == null) {
            server = this.serverFactory.createServer(url.getParameter("server", "jetty"));
            server.start(url);
            this.servers.put(addr, server);
        }
        String contextPath = this.getContextPath(url);
        if ("servlet".equalsIgnoreCase(url.getParameter("server", "jetty"))) {
            ServletContext servletContext = ServletManager.getInstance().getServletContext(-1234);
            if (servletContext == null) {
                throw new RpcException("No servlet context found. Since you are using server='servlet', make sure that you've configured " + BootstrapListener.class.getName() + " in web.xml");
            }
            String webappPath = servletContext.getContextPath();
            if (StringUtils.isNotEmpty(webappPath)) {
                if (!contextPath.startsWith(webappPath = webappPath.substring(1))) {
                    throw new RpcException("Since you are using server='servlet', make sure that the 'contextpath' property starts with the path of external webapp");
                }
                if ((contextPath = contextPath.substring(webappPath.length())).startsWith("/")) {
                    contextPath = contextPath.substring(1);
                }
            }
        }
        final Class<T> resourceDef = GetRestful.getRootResourceClass((Class)implClass) != null ? implClass : type;
        server.deploy(resourceDef, impl, contextPath);
        final RestServer s = server;
        return new Runnable(){

            @Override
            public void run() {
                s.undeploy(resourceDef);
            }
        };
    }

    @Override
    protected <T> T doRefer(Class<T> serviceType, URL url) throws RpcException {
        if (this.connectionMonitor == null) {
            this.connectionMonitor = new ConnectionMonitor();
        }
        PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager();
        connectionManager.setMaxTotal(url.getParameter("connections", 20));
        connectionManager.setDefaultMaxPerRoute(url.getParameter("connections", 20));
        this.connectionMonitor.addConnectionManager((ClientConnectionManager)connectionManager);
        DefaultHttpClient httpClient = new DefaultHttpClient((ClientConnectionManager)connectionManager);
        httpClient.setKeepAliveStrategy(new ConnectionKeepAliveStrategy(){

            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                BasicHeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator("Keep-Alive"));
                while (it.hasNext()) {
                    HeaderElement he = it.nextElement();
                    String param = he.getName();
                    String value = he.getValue();
                    if (value == null || !param.equalsIgnoreCase("timeout")) continue;
                    return Long.parseLong(value) * 1000L;
                }
                return 30000L;
            }
        });
        HttpParams params = httpClient.getParams();
        HttpConnectionParams.setConnectionTimeout((HttpParams)params, (int)url.getParameter("timeout", 1000));
        HttpConnectionParams.setSoTimeout((HttpParams)params, (int)url.getParameter("timeout", 1000));
        HttpConnectionParams.setTcpNoDelay((HttpParams)params, (boolean)true);
        HttpConnectionParams.setSoKeepalive((HttpParams)params, (boolean)true);
        ApacheHttpClient4Engine engine = new ApacheHttpClient4Engine((HttpClient)httpClient);
        ResteasyClient client = new ResteasyClientBuilder().httpEngine((ClientHttpEngine)engine).build();
        this.clients.add(client);
        client.register(RpcContextFilter.class);
        for (String clazz : Constants.COMMA_SPLIT_PATTERN.split(url.getParameter("extension", ""))) {
            if (StringUtils.isEmpty(clazz)) continue;
            try {
                client.register(Thread.currentThread().getContextClassLoader().loadClass(clazz.trim()));
            }
            catch (ClassNotFoundException e) {
                throw new RpcException("Error loading JAX-RS extension class: " + clazz.trim(), (Throwable)e);
            }
        }
        ResteasyWebTarget target = client.target("http://" + url.getHost() + ":" + url.getPort() + "/" + this.getContextPath(url));
        return (T)target.proxy(serviceType);
    }

    @Override
    protected int getErrorCode(Throwable e) {
        return super.getErrorCode(e);
    }

    @Override
    public void destroy() {
        super.destroy();
        if (this.connectionMonitor != null) {
            this.connectionMonitor.shutdown();
        }
        for (Map.Entry<String, RestServer> entry : this.servers.entrySet()) {
            try {
                if (this.logger.isInfoEnabled()) {
                    this.logger.info("Closing the rest server at " + entry.getKey());
                }
                entry.getValue().stop();
            }
            catch (Throwable t) {
                this.logger.warn("Error closing rest server", t);
            }
        }
        this.servers.clear();
        if (this.logger.isInfoEnabled()) {
            this.logger.info("Closing rest clients");
        }
        for (ResteasyClient client : this.clients) {
            try {
                client.close();
            }
            catch (Throwable t) {
                this.logger.warn("Error closing rest client", t);
            }
        }
        this.clients.clear();
    }

    protected String getContextPath(URL url) {
        int pos = url.getPath().lastIndexOf("/");
        return pos > 0 ? url.getPath().substring(0, pos) : "";
    }

    protected class ConnectionMonitor
    extends Thread {
        private volatile boolean shutdown;
        private final List<ClientConnectionManager> connectionManagers = Collections.synchronizedList(new LinkedList());

        protected ConnectionMonitor() {
        }

        public void addConnectionManager(ClientConnectionManager connectionManager) {
            this.connectionManagers.add(connectionManager);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         * Enabled aggressive block sorting
         * Enabled unnecessary exception pruning
         * Enabled aggressive exception aggregation
         */
        @Override
        public void run() {
            try {
                while (!this.shutdown) {
                    ConnectionMonitor connectionMonitor = this;
                    synchronized (connectionMonitor) {
                        this.wait(1000L);
                        for (ClientConnectionManager connectionManager : this.connectionManagers) {
                            connectionManager.closeExpiredConnections();
                            connectionManager.closeIdleConnections(30L, TimeUnit.SECONDS);
                        }
                    }
                }
                return;
            }
            catch (InterruptedException ex) {
                this.shutdown();
            }
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        public void shutdown() {
            this.shutdown = true;
            this.connectionManagers.clear();
            ConnectionMonitor connectionMonitor = this;
            synchronized (connectionMonitor) {
                this.notifyAll();
            }
        }
    }

}


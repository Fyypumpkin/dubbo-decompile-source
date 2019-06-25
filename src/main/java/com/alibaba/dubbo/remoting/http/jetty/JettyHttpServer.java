/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  org.mortbay.jetty.Connector
 *  org.mortbay.jetty.HandlerContainer
 *  org.mortbay.jetty.Server
 *  org.mortbay.jetty.handler.ContextHandler
 *  org.mortbay.jetty.handler.ContextHandler$SContext
 *  org.mortbay.jetty.nio.SelectChannelConnector
 *  org.mortbay.jetty.servlet.Context
 *  org.mortbay.jetty.servlet.ServletHandler
 *  org.mortbay.jetty.servlet.ServletHolder
 *  org.mortbay.log.Log
 *  org.mortbay.log.Logger
 *  org.mortbay.log.StdErrLog
 *  org.mortbay.thread.QueuedThreadPool
 *  org.mortbay.thread.ThreadPool
 */
package com.alibaba.dubbo.remoting.http.jetty;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.remoting.http.HttpHandler;
import com.alibaba.dubbo.remoting.http.servlet.DispatcherServlet;
import com.alibaba.dubbo.remoting.http.servlet.ServletManager;
import com.alibaba.dubbo.remoting.http.support.AbstractHttpServer;
import javax.servlet.ServletContext;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.HandlerContainer;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.log.Log;
import org.mortbay.log.Logger;
import org.mortbay.log.StdErrLog;
import org.mortbay.thread.QueuedThreadPool;
import org.mortbay.thread.ThreadPool;

public class JettyHttpServer
extends AbstractHttpServer {
    private static final com.alibaba.dubbo.common.logger.Logger logger = LoggerFactory.getLogger(JettyHttpServer.class);
    private Server server;
    private URL url;

    public JettyHttpServer(URL url, HttpHandler handler) {
        super(url, handler);
        this.url = url;
        Log.setLog((Logger)new StdErrLog());
        Log.getLog().setDebugEnabled(false);
        DispatcherServlet.addHttpHandler(url.getPort(), handler);
        int threads = url.getParameter("threads", 200);
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setDaemon(true);
        threadPool.setMaxThreads(threads);
        threadPool.setMinThreads(threads);
        SelectChannelConnector connector = new SelectChannelConnector();
        if (!url.isAnyHost() && NetUtils.isValidLocalHost(url.getHost())) {
            connector.setHost(url.getHost());
        }
        connector.setPort(url.getPort());
        this.server = new Server();
        this.server.setThreadPool((ThreadPool)threadPool);
        this.server.addConnector((Connector)connector);
        ServletHandler servletHandler = new ServletHandler();
        ServletHolder servletHolder = servletHandler.addServletWithMapping(DispatcherServlet.class, "/*");
        servletHolder.setInitOrder(2);
        Context context = new Context((HandlerContainer)this.server, "/", 1);
        context.setServletHandler(servletHandler);
        ServletManager.getInstance().addServletContext(url.getPort(), (ServletContext)context.getServletContext());
        try {
            this.server.start();
        }
        catch (Exception e) {
            throw new IllegalStateException("Failed to start jetty server on " + url.getAddress() + ", cause: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        super.close();
        ServletManager.getInstance().removeServletContext(this.url.getPort());
        if (this.server != null) {
            try {
                this.server.stop();
            }
            catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }
        }
    }
}


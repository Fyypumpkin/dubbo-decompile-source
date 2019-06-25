/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  javax.servlet.Servlet
 *  javax.servlet.ServletContext
 *  org.apache.catalina.Context
 *  org.apache.catalina.LifecycleException
 *  org.apache.catalina.connector.Connector
 *  org.apache.catalina.startup.Tomcat
 */
package com.alibaba.dubbo.remoting.http.tomcat;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.remoting.http.HttpHandler;
import com.alibaba.dubbo.remoting.http.servlet.DispatcherServlet;
import com.alibaba.dubbo.remoting.http.servlet.ServletManager;
import com.alibaba.dubbo.remoting.http.support.AbstractHttpServer;
import java.io.File;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;

public class TomcatHttpServer
extends AbstractHttpServer {
    private static final Logger logger = LoggerFactory.getLogger(TomcatHttpServer.class);
    private final Tomcat tomcat;
    private final URL url;

    public TomcatHttpServer(URL url, HttpHandler handler) {
        super(url, handler);
        this.url = url;
        DispatcherServlet.addHttpHandler(url.getPort(), handler);
        String baseDir = new File(System.getProperty("java.io.tmpdir")).getAbsolutePath();
        this.tomcat = new Tomcat();
        this.tomcat.setBaseDir(baseDir);
        this.tomcat.setPort(url.getPort());
        this.tomcat.getConnector().setProperty("maxThreads", String.valueOf(url.getParameter("threads", 200)));
        this.tomcat.getConnector().setProperty("maxConnections", String.valueOf(url.getParameter("accepts", -1)));
        this.tomcat.getConnector().setProperty("URIEncoding", "UTF-8");
        this.tomcat.getConnector().setProperty("connectionTimeout", "60000");
        this.tomcat.getConnector().setProperty("maxKeepAliveRequests", "-1");
        this.tomcat.getConnector().setProtocol("org.apache.coyote.http11.Http11NioProtocol");
        Context context = this.tomcat.addContext("/", baseDir);
        Tomcat.addServlet((Context)context, (String)"dispatcher", (Servlet)new DispatcherServlet());
        context.addServletMapping("/*", "dispatcher");
        ServletManager.getInstance().addServletContext(url.getPort(), context.getServletContext());
        try {
            this.tomcat.start();
        }
        catch (LifecycleException e) {
            throw new IllegalStateException("Failed to start tomcat server at " + url.getAddress(), (Throwable)e);
        }
    }

    @Override
    public void close() {
        super.close();
        ServletManager.getInstance().removeServletContext(this.url.getPort());
        try {
            this.tomcat.stop();
        }
        catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
    }
}


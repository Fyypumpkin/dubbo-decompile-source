/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.mortbay.jetty.Connector
 *  org.mortbay.jetty.Handler
 *  org.mortbay.jetty.Server
 *  org.mortbay.jetty.nio.SelectChannelConnector
 *  org.mortbay.jetty.servlet.FilterHolder
 *  org.mortbay.jetty.servlet.ServletHandler
 *  org.mortbay.jetty.servlet.ServletHolder
 */
package com.alibaba.dubbo.container.jetty;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.container.Container;
import com.alibaba.dubbo.container.page.PageServlet;
import com.alibaba.dubbo.container.page.ResourceFilter;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;

public class JettyContainer
implements Container {
    private static final Logger logger = LoggerFactory.getLogger(JettyContainer.class);
    public static final String JETTY_PORT = "dubbo.jetty.port";
    public static final String JETTY_DIRECTORY = "dubbo.jetty.directory";
    public static final String JETTY_PAGES = "dubbo.jetty.page";
    public static final int DEFAULT_JETTY_PORT = 8080;
    SelectChannelConnector connector;

    @Override
    public void start() {
        String serverPort = ConfigUtils.getProperty(JETTY_PORT);
        int port = serverPort == null || serverPort.length() == 0 ? 8080 : Integer.parseInt(serverPort);
        this.connector = new SelectChannelConnector();
        this.connector.setPort(port);
        ServletHandler handler = new ServletHandler();
        String resources = ConfigUtils.getProperty(JETTY_DIRECTORY);
        if (resources != null && resources.length() > 0) {
            FilterHolder resourceHolder = handler.addFilterWithMapping(ResourceFilter.class, "/*", 0);
            resourceHolder.setInitParameter("resources", resources);
        }
        ServletHolder pageHolder = handler.addServletWithMapping(PageServlet.class, "/*");
        pageHolder.setInitParameter("pages", ConfigUtils.getProperty(JETTY_PAGES));
        pageHolder.setInitOrder(2);
        Server server = new Server();
        server.addConnector((Connector)this.connector);
        server.addHandler((Handler)handler);
        try {
            server.start();
        }
        catch (Exception e) {
            throw new IllegalStateException("Failed to start jetty server on " + NetUtils.getLocalHost() + ":" + port + ", cause: " + e.getMessage(), e);
        }
    }

    @Override
    public void stop() {
        try {
            if (this.connector != null) {
                this.connector.close();
                this.connector = null;
            }
        }
        catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
    }
}


/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 *  javax.servlet.ServletContextEvent
 *  javax.servlet.ServletContextListener
 */
package com.alibaba.dubbo.remoting.http.servlet;

import com.alibaba.dubbo.remoting.http.servlet.ServletManager;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class BootstrapListener
implements ServletContextListener {
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletManager.getInstance().addServletContext(-1234, servletContextEvent.getServletContext());
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        ServletManager.getInstance().removeServletContext(-1234);
    }
}


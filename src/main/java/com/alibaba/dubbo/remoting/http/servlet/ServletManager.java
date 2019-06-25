/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  javax.servlet.ServletContext
 */
package com.alibaba.dubbo.remoting.http.servlet;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.ServletContext;

public class ServletManager {
    public static final int EXTERNAL_SERVER_PORT = -1234;
    private static final ServletManager instance = new ServletManager();
    private final Map<Integer, ServletContext> contextMap = new ConcurrentHashMap<Integer, ServletContext>();

    public static ServletManager getInstance() {
        return instance;
    }

    public void addServletContext(int port, ServletContext servletContext) {
        this.contextMap.put(port, servletContext);
    }

    public void removeServletContext(int port) {
        this.contextMap.remove(port);
    }

    public ServletContext getServletContext(int port) {
        return this.contextMap.get(port);
    }
}

